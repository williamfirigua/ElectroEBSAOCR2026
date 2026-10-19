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

    /** Plantilla ya parseada + la marca del archivo con la que se leyó. */
    private static final class EntradaCache {
        final long modificado;
        final long tamano;
        final Plantilla plantilla;

        EntradaCache(long modificado, long tamano, Plantilla plantilla) {
            this.modificado = modificado;
            this.tamano = tamano;
            this.plantilla = plantilla;
        }
    }

    private static final Map<String, EntradaCache> CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Carga la plantilla una sola vez por sesión. Se vuelve a leer solo si el archivo cambió
     * (fecha o tamaño), así una plantilla nueva copiada al equipo se toma sin reiniciar la app.
     * La instancia es inmutable, por eso se puede compartir entre facturas e hilos.
     */
    public static Plantilla cargar(File archivo) throws IOException {
        String llave = archivo.getAbsolutePath();
        long modificado = archivo.lastModified();
        long tamano = archivo.length();
        EntradaCache e = CACHE.get(llave);
        if (e != null && e.modificado == modificado && e.tamano == tamano) return e.plantilla;
        Plantilla p;
        try (InputStream in = new FileInputStream(archivo)) {
            p = cargar(archivo.getName(), in);
        }
        CACHE.put(llave, new EntradaCache(modificado, tamano, p));
        return p;
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
        return renderizar(contexto, ajustes, parametros.vacias(), false);
    }

    /**
     * Mapa para el diseñador: cada texto de la plantilla se imprime como su etiqueta ({@code [11]},
     * {@code [detalle.3]}) en su posición; coordenadas, barras y códigos se resuelven con el contexto.
     * Ninguna línea se omite, así el archivo sale en el mismo orden y con las mismas líneas que la
     * plantilla (sin los comentarios {@code ;@}).
     */
    public Resultado renderizarMapa(Map<String, String> contexto, AjustesImpresora ajustes) {
        return renderizar(contexto, ajustes, Parametros.Vacias.ETIQUETA, true);
    }

    private Resultado renderizar(Map<String, String> contexto, AjustesImpresora ajustes,
                                 Parametros.Vacias vacias, boolean todoEtiqueta) {
        StringBuilder salida = new StringBuilder(lineas.size() * 48);
        Set<String> faltantes = new LinkedHashSet<>();
        String faltante = parametros.faltante();
        AjustesImpresora aj = ajustes == null ? AjustesImpresora.ninguno() : ajustes;

        for (String linea : lineas) {
            Matcher m = PLACEHOLDER.matcher(linea);
            if (!m.find()) {
                salida.append(aj.aplicar(linea)).append("\r\n");
                continue;
            }
            boolean texto = esComandoDeTexto(linea);
            int inicioTexto = texto ? inicioDelTexto(linea) : Integer.MAX_VALUE;
            StringBuffer sb = new StringBuffer(linea.length() + 32);
            boolean algunDato = false;
            do {
                String clave = m.group(1);
                boolean enTexto = m.start() >= inicioTexto;
                String valor = contexto.get(clave);
                String formateado;
                if (enTexto && todoEtiqueta) {
                    formateado = etiqueta(clave, formatos(m.group(2)));
                } else if (valor == null) {
                    faltantes.add(clave);
                    formateado = faltante;
                } else {
                    formateado = Formatos.aplicar(valor, formatos(m.group(2)));
                    if (enTexto && vacias == Parametros.Vacias.ETIQUETA && formateado.trim().isEmpty()) {
                        formateado = etiqueta(clave, formatos(m.group(2)));
                    }
                }
                if (!formateado.trim().isEmpty()) algunDato = true;
                m.appendReplacement(sb, Matcher.quoteReplacement(formateado));
            } while (m.find());
            m.appendTail(sb);

            if (!algunDato && esComandoConDato(linea)) {
                // Un B (código de barras) sin dato siempre se omite: la impresora se queda esperando.
                if (vacias == Parametros.Vacias.OMITIR || !texto) continue;
            }
            salida.append(aj.aplicar(alinear(sb.toString()))).append("\r\n");
        }
        return new Resultado(salida.toString(), faltantes);
    }

    /** Etiqueta visible de una clave en el mapa del diseñador: [11], [detalle.3]. */
    static String etiqueta(String clave) {
        return "[" + clave + "]";
    }

    /**
     * Etiqueta rellenada al ancho de la plantilla ({@code izq:N}/{@code der:N}) para que ocupe lo mismo
     * que el dato. Nunca se recorta: si la etiqueta es más larga que el campo, se ve completa.
     */
    static String etiqueta(String clave, List<String> formatos) {
        String e = etiqueta(clave);
        for (String f : formatos) {
            int dos = f.indexOf(':');
            if (dos < 0) continue;
            String nombre = f.substring(0, dos);
            int ancho;
            try { ancho = Integer.parseInt(f.substring(dos + 1).trim()); } catch (NumberFormatException ex) { continue; }
            StringBuilder relleno = new StringBuilder();
            for (int i = e.length(); i < ancho; i++) relleno.append(' ');
            if ("izq".equals(nombre)) e = e + relleno;
            else if ("der".equals(nombre)) e = relleno + e;
        }
        return e;
    }

    private static boolean esComandoDeTexto(String linea) {
        return tokensAntesDelTexto(linea) > 0;
    }

    /**
     * Cuántos tokens van antes del texto: {@code T f s x y texto} → 5;
     * {@code SCALE-TEXT fuente ancho alto x y texto} → 6. 0 si no es un comando de texto.
     */
    private static int tokensAntesDelTexto(String linea) {
        if (linea.startsWith("T ") || linea.startsWith("VT ") || linea.startsWith("TR ") || linea.startsWith("TEXT ")
                || linea.startsWith("T90 ") || linea.startsWith("T180 ") || linea.startsWith("T270 ")) return 5;
        if (linea.startsWith("SCALE-TEXT") || linea.startsWith("VSCALE-TEXT") || linea.startsWith("SCALE-TO-FIT")) return 6;
        return 0;
    }

    /** Índice donde empieza el texto (después de las coordenadas). */
    private static int inicioDelTexto(String linea) {
        int antes = tokensAntesDelTexto(linea);
        int tokens = 0, i = 0, n = linea.length();
        while (i < n && tokens < antes) {
            while (i < n && linea.charAt(i) == ' ') i++;
            while (i < n && linea.charAt(i) != ' ') i++;
            tokens++;
        }
        if (i < n && linea.charAt(i) == ' ') i++;   // un solo separador: los espacios del dato se respetan
        return i;
    }

    private static boolean esComandoConDato(String linea) {
        return tokensAntesDelTexto(linea) > 0 || linea.startsWith("B ") || linea.startsWith("VB ");
    }

    /**
     * {@code SCALE-TEXT-DER fuente ancho alto xDerecho y texto}: la x es el borde DERECHO del texto.
     * {@code SCALE-TEXT-CEN …}: la x es el CENTRO. Se convierten a {@code SCALE-TEXT} midiendo el texto
     * con {@link FuenteMetricas} (DIN no es monoespaciada, así los valores quedan alineados en columna).
     * No son comandos CPCL: la impresora nunca los recibe.
     */
    static String alinear(String linea) {
        boolean der = linea.startsWith("SCALE-TEXT-DER ");
        boolean cen = linea.startsWith("SCALE-TEXT-CEN ");
        if (!der && !cen) return linea;
        String[] t = linea.split(" ", 7);
        if (t.length < 6) return linea;
        String texto = t.length == 7 ? t[6] : "";
        try {
            double anchoPt = Double.parseDouble(t[2]);
            int x = Integer.parseInt(t[4]);
            int w = FuenteMetricas.ancho(t[1], anchoPt, texto);
            int nx = Math.max(0, der ? x - w : x - w / 2);
            return "SCALE-TEXT " + t[1] + " " + t[2] + " " + t[3] + " " + nx + " " + t[5] + " " + texto;
        } catch (NumberFormatException e) {
            return "SCALE-TEXT" + linea.substring(14);
        }
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
