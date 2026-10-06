package com.gselectroCaqueta.impresion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Plantilla CPCL con placeholders.
 * <p>
 * La plantilla es el archivo que entrega el diseñador, con dos añadidos:
 * <ul>
 *   <li>{@code {clave}} o {@code {clave|formato|formato2}} en cualquier línea. La clave es
 *       un número de caída del Excel ({@code {11}} = CNTA) o un nombre calculado
 *       ({@code {costo_diario}}). Ver {@link ContextoFactura} para la lista.</li>
 *   <li>{@code ;@SET clave=valor} para parámetros de geometría o negocio que el motor
 *       necesita (línea base de las barras, GLN, ancho del mensaje). Son comentarios CPCL,
 *       así que el archivo sigue siendo imprimible tal cual.</li>
 * </ul>
 * El motor no hace aritmética ni decide posiciones: sustituye y formatea. Todo cálculo
 * vive en {@link ContextoFactura}. Así un cambio de diseño es un cambio de archivo, no de APK.
 * <p>
 * Reglas de salida:
 * <ul>
 *   <li>Las líneas {@code ;@...} no se envían a la impresora.</li>
 *   <li>Una línea {@code T} o {@code B} que tenía placeholders y cuyo dato queda vacío se
 *       omite (si {@code omitir_vacias=1}, que es el valor por defecto). Un {@code B} con
 *       dato vacío haría fallar la impresora; un {@code T} vacío solo gasta bytes por Bluetooth.</li>
 *   <li>Fin de línea CRLF, como exige CPCL.</li>
 * </ul>
 */
public final class Plantilla {

    /** {clave} o {clave|fmt|fmt:arg}. La clave admite letras, dígitos, '_' y '.'. */
    static final Pattern PLACEHOLDER = Pattern.compile("\\{([A-Za-z0-9_.]+)((?:\\|[^{}|]+)*)\\}");
    private static final String DIRECTIVA_SET = ";@SET";

    private final List<String> lineas;
    private final Parametros parametros;
    private final String nombre;

    private Plantilla(String nombre, List<String> lineas, Parametros parametros) {
        this.nombre = nombre;
        this.lineas = Collections.unmodifiableList(lineas);
        this.parametros = parametros;
    }

    public static Plantilla cargar(File archivo) throws IOException {
        try (InputStream in = new FileInputStream(archivo)) {
            return cargar(archivo.getName(), in);
        }
    }

    /** La plantilla se lee en UTF-8 (así la guarda el diseñador); la salida se escribe en CP1252. */
    public static Plantilla cargar(String nombre, InputStream in) throws IOException {
        List<String> lineas = new ArrayList<>();
        Map<String, String> set = new LinkedHashMap<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        String l;
        while ((l = br.readLine()) != null) {
            if (l.startsWith("﻿")) l = l.substring(1);
            if (l.startsWith(DIRECTIVA_SET)) {
                String resto = l.substring(DIRECTIVA_SET.length()).trim();
                int eq = resto.indexOf('=');
                if (eq <= 0) throw new IOException(nombre + ": directiva mal formada: " + l);
                set.put(resto.substring(0, eq).trim(), resto.substring(eq + 1).trim());
                continue;
            }
            if (l.startsWith(";@")) continue; // otras directivas reservadas: se ignoran
            lineas.add(l);
        }
        return new Plantilla(nombre, lineas, new Parametros(set));
    }

    public Parametros parametros() {
        return parametros;
    }

    public String nombre() {
        return nombre;
    }

    /** Claves que la plantilla usa; sirve para validar una plantilla nueva contra el contexto. */
    public Set<String> clavesUsadas() {
        Set<String> claves = new LinkedHashSet<>();
        for (String l : lineas) {
            Matcher m = PLACEHOLDER.matcher(l);
            while (m.find()) claves.add(m.group(1));
        }
        return claves;
    }

    /**
     * Resuelve la plantilla con el contexto. Una clave ausente se imprime como el parámetro
     * {@code faltante} ("???" por defecto) y se reporta en {@link Resultado#faltantes()}: la
     * factura se imprime igual, el hueco se ve en papel y queda en la bitácora. Una clave
     * presente con valor vacío es un dato legítimamente vacío (reactiva, FOES) y no se marca.
     */
    public Resultado renderizar(Map<String, String> contexto) {
        return renderizar(contexto, AjustesImpresora.ninguno());
    }

    /**
     * Igual que {@link #renderizar(Map)} y además aplica los ajustes de la impresora del
     * terminal (TONE, corrimiento X/Y) a cada línea ya resuelta. Se aplica después de
     * sustituir porque una coordenada puede venir de una clave ({@code {barra.1.y0}}).
     */
    public Resultado renderizar(Map<String, String> contexto, AjustesImpresora ajustes) {
        StringBuilder salida = new StringBuilder(lineas.size() * 48);
        Set<String> faltantes = new LinkedHashSet<>();
        boolean omitir = parametros.omitirVacias();
        String faltante = parametros.faltante();
        AjustesImpresora aj = ajustes == null ? AjustesImpresora.ninguno() : ajustes;

        for (String linea : lineas) {
            Matcher m = PLACEHOLDER.matcher(linea);
            if (!m.find()) {
                salida.append(aj.aplicar(linea)).append("\r\n");
                continue;
            }
            StringBuffer sb = new StringBuffer(linea.length() + 32);
            boolean algunDato = false;
            do {
                String clave = m.group(1);
                String valor = contexto.get(clave);
                String formateado;
                if (valor == null) {
                    faltantes.add(clave);
                    formateado = faltante;
                } else {
                    formateado = Formatos.aplicar(valor, formatos(m.group(2)));
                }
                if (!formateado.trim().isEmpty()) algunDato = true;
                m.appendReplacement(sb, Matcher.quoteReplacement(formateado));
            } while (m.find());
            m.appendTail(sb);

            if (omitir && !algunDato && esComandoConDato(linea)) continue;
            salida.append(aj.aplicar(sb.toString())).append("\r\n");
        }
        return new Resultado(salida.toString(), faltantes);
    }

    private static boolean esComandoConDato(String linea) {
        return linea.startsWith("T ") || linea.startsWith("B ") || linea.startsWith("VT ")
                || linea.startsWith("TR ") || linea.startsWith("TEXT ");
    }

    private static List<String> formatos(String grupo) {
        if (grupo == null || grupo.isEmpty()) return Collections.emptyList();
        List<String> f = new ArrayList<>(Arrays.asList(grupo.substring(1).split("\\|")));
        f.removeIf(String::isEmpty);
        return f;
    }

    /** Texto CPCL final más las claves que no estaban en el contexto. */
    public static final class Resultado {
        private final String cpcl;
        private final Set<String> faltantes;

        Resultado(String cpcl, Set<String> faltantes) {
            this.cpcl = cpcl;
            this.faltantes = Collections.unmodifiableSet(faltantes);
        }

        public String cpcl() { return cpcl; }
        public Set<String> faltantes() { return faltantes; }
        public boolean completo() { return faltantes.isEmpty(); }
    }
}
