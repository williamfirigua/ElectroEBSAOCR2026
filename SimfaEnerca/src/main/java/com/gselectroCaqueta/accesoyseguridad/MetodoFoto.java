package com.gselectroCaqueta.accesoyseguridad;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Metodología de foto de una causal o de una crítica (bloque D). La define la oficina en la web
 * (Causales > editar y Tablas básicas > Críticas) y llega en DATOSDEENTRADA/FOTOMETODO.TXT (UTF-8):
 * <pre>
 *     tipo|codigo|metodo|fachada|medidor|factura|libres|instr_fachada|instr_medidor|instr_factura|instr_libre|
 *     causal|25|guiada|S|S|N|1|Toda la fachada|Medidor con el sello|||
 * </pre>
 * <ul>
 *   <li>guiada: pasos fachada y medidor antes de imprimir; factura (la recién impresa) después.</li>
 *   <li>libre: de 1 a {@link #libres} fotos; después de cada una se pregunta "¿Otra foto?".</li>
 *   <li>sin línea (o sin archivo): una foto, como siempre ({@link #buscar} devuelve null).</li>
 * </ul>
 * También responde si una causal obliga fotografía (columna 3 de CAUSA_NL.TXT, S/N).
 */
public final class MetodoFoto {

    public static final String ARCHIVO = "FOTOMETODO.TXT";
    public static final String CAUSAL = "causal";
    public static final String CRITICA = "critica";
    public static final String GUIADA = "guiada";
    public static final String LIBRE = "libre";

    public static final String FACHADA = "fachada";
    public static final String MEDIDOR = "medidor";
    public static final String FACTURA = "factura";

    public static final int MAX_LIBRES = 3;

    /** Un paso: etiqueta (fachada, medidor, factura, libre) y la instrucción que ve el técnico. */
    public static final class Paso {
        public final String etiqueta;
        public final String titulo;
        public final String instruccion;

        Paso(String etiqueta, String titulo, String instruccion) {
            this.etiqueta = etiqueta;
            this.titulo = titulo;
            this.instruccion = instruccion;
        }
    }

    public final String tipo;
    public final String codigo;
    public final String metodo;
    public final int libres;
    private final List<Paso> antes;
    private final Paso factura;

    private MetodoFoto(String tipo, String codigo, String metodo, int libres, List<Paso> antes, Paso factura) {
        this.tipo = tipo;
        this.codigo = codigo;
        this.metodo = metodo;
        this.libres = libres;
        this.antes = antes;
        this.factura = factura;
    }

    public boolean esLibre() {
        return LIBRE.equals(metodo);
    }

    /** Pasos antes de imprimir (guiada: fachada y/o medidor; libre: uno, el que se repite). */
    public List<Paso> pasosAntes() {
        return antes;
    }

    /** Paso de la factura impresa, o null. */
    public Paso pasoFactura() {
        return factura;
    }

    /** "causal|25" o "critica|7": lo que se guarda en la foto pendiente para volver a encontrar la metodología. */
    public String clave() {
        return tipo + "|" + codigo;
    }

    // ------------------------------------------------------------------ lectura

    /** Metodología de tipo + código, o null (= una foto). Nunca lanza: un archivo dañado es "sin metodología". */
    public static MetodoFoto buscar(File dirEntrada, String tipo, String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) return null;
        File f = new File(dirEntrada, ARCHIVO);
        if (!f.isFile()) return null;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = r.readLine()) != null) {
                String[] c = campos(linea);
                if (c.length < 7 || !c[0].equalsIgnoreCase(tipo) || !mismoCodigo(c[1], codigo)) continue;
                return desdeCampos(c);
            }
        } catch (Exception ignorado) {
            // sin metodología
        }
        return null;
    }

    /** Desde la clave guardada ("causal|25"). */
    public static MetodoFoto desdeClave(File dirEntrada, String clave) {
        if (clave == null) return null;
        int i = clave.indexOf('|');
        if (i <= 0) return null;
        return buscar(dirEntrada, clave.substring(0, i), clave.substring(i + 1));
    }

    /**
     * Columna "obliga fotografía" de la causal en CAUSA_NL.TXT ("  5|Sin Medidor ...|N|N|P|1|").
     * null si no está la causal o no se puede leer (quien llama mantiene lo de siempre: pide foto).
     */
    public static Boolean causalObligaFoto(File dirEntrada, String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) return null;
        File f = new File(dirEntrada, "CAUSA_NL.TXT");
        if (!f.isFile()) return null;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.ISO_8859_1))) {
            String linea;
            while ((linea = r.readLine()) != null) {
                String[] c = campos(linea);
                if (c.length < 3 || !mismoCodigo(c[0], codigo)) continue;
                String v = c[2].trim().toUpperCase();
                if (v.equals("S") || v.equals("1")) return Boolean.TRUE;
                if (v.equals("N") || v.equals("0")) return Boolean.FALSE;
                return null;
            }
        } catch (Exception ignorado) {
            // sin dato
        }
        return null;
    }

    static MetodoFoto desdeCampos(String[] c) {
        String tipo = c[0].trim().toLowerCase();
        String codigo = c[1].trim();
        String metodo = c[2].trim().toLowerCase();
        int libres = Math.max(1, Math.min(MAX_LIBRES, entero(c[6], 1)));
        String iFachada = campo(c, 7), iMedidor = campo(c, 8), iFactura = campo(c, 9), iLibre = campo(c, 10);
        if (LIBRE.equals(metodo)) {
            List<Paso> antes = Collections.singletonList(new Paso(LIBRE, "Foto",
                    iLibre.isEmpty() ? "Tome la foto que muestre la situación encontrada." : iLibre));
            return new MetodoFoto(tipo, codigo, LIBRE, libres, antes, null);
        }
        if (!GUIADA.equals(metodo)) return null;
        List<Paso> antes = new ArrayList<>();
        if (si(c[3])) antes.add(new Paso(FACHADA, "Fachada",
                iFachada.isEmpty() ? "Tome la fachada completa del predio con la nomenclatura visible." : iFachada));
        if (si(c[4])) antes.add(new Paso(MEDIDOR, "Medidor",
                iMedidor.isEmpty() ? "Tome el medidor de frente, con la lectura y el número de serie legibles." : iMedidor));
        Paso factura = si(c[5]) ? new Paso(FACTURA, "Factura impresa",
                iFactura.isEmpty() ? "Tome la factura recién impresa, completa y legible." : iFactura) : null;
        if (antes.isEmpty() && factura == null) return null;
        return new MetodoFoto(tipo, codigo, GUIADA, 1, Collections.unmodifiableList(antes), factura);
    }

    /** "05", " 5" y "5" son la misma causal; los no numéricos se comparan sin espacios ni mayúsculas. */
    static boolean mismoCodigo(String a, String b) {
        String x = a == null ? "" : a.trim(), y = b == null ? "" : b.trim();
        if (x.isEmpty() || y.isEmpty()) return false;
        try {
            return Integer.parseInt(x) == Integer.parseInt(y);
        } catch (NumberFormatException e) {
            return x.equalsIgnoreCase(y);
        }
    }

    private static String[] campos(String linea) {
        String l = linea.startsWith("﻿") ? linea.substring(1) : linea;
        return l.split("\\|", -1);
    }

    private static String campo(String[] c, int i) {
        return i < c.length ? c[i].trim() : "";
    }

    private static boolean si(String v) {
        String s = v == null ? "" : v.trim().toUpperCase();
        return s.equals("S") || s.equals("1");
    }

    private static int entero(String s, int defecto) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return defecto;
        }
    }
}
