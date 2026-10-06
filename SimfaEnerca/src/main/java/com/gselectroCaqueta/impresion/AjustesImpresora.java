package com.gselectroCaqueta.impresion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ajustes propios de la impresora del terminal: intensidad (TONE), corrimiento en X/Y y modelo.
 * <p>
 * Son los mismos valores que la pantalla {@code ModuloConfigFormatoImpresion} guarda en
 * {@code ValoresFormato.log}; el archivo no cambia de formato, así la pantalla sigue
 * funcionando tal cual. Una línea por valor:
 * <pre>
 *   1  intensidad (TONE)            "Temperatura" en la pantalla
 *   2  corrimiento vertical (dy)    "Mover líneas"; positivo = hacia abajo
 *   3  gap/bar sense                 sin uso: la plantilla trae su BAR-SENSE
 *   4  dos barras                    sin uso: la plantilla decide cuántos códigos lleva
 *   5  modelo                        1 = ZQ-521, 0 = RW-420
 *   6  corrimiento horizontal (dx)   opcional; positivo = hacia la derecha
 * </pre>
 * La plantilla sigue con las posiciones del diseñador; el corrimiento se aplica al renderizar,
 * a toda coordenada de {@code T}, {@code LINE}, {@code BOX}, {@code B}, {@code PCX}. Es lo que
 * hacía {@code crearArchivoImpresion1/420} sumando {@code Valortexto2} a cada {@code L<n>},
 * pero sin tener una copia de las coordenadas dentro del APK.
 * <p>
 * Inmutable. {@link #ninguno()} deja la plantilla intacta (pruebas, bosquejos).
 */
public final class AjustesImpresora {

    public static final String ARCHIVO = "ValoresFormato.log";

    public enum Modelo {
        ZQ521(""), RW420("_RW420");

        /** Sufijo con el que se busca una variante de plantilla: FORMATO_CORTA_RW420.CPCL. */
        public final String sufijoPlantilla;

        Modelo(String sufijo) { this.sufijoPlantilla = sufijo; }
    }

    private static final AjustesImpresora NINGUNO = new AjustesImpresora(null, 0, 0, Modelo.ZQ521);

    // Comandos CPCL con coordenadas. Solo se toca el prefijo numérico; el dato se deja intacto.
    private static final Pattern TEXTO = Pattern.compile(
            "^(T|TR|VT|T90|T180|T270|TEXT|VTEXT)\\s+(\\S+)\\s+(\\S+)\\s+(-?\\d+)\\s+(-?\\d+)(.*)$");
    private static final Pattern LINEA = Pattern.compile(
            "^(LINE|L|INVERSE-LINE|IL|BOX)\\s+(-?\\d+)\\s+(-?\\d+)\\s+(-?\\d+)\\s+(-?\\d+)(.*)$");
    private static final Pattern BARRA = Pattern.compile(
            "^(B|VB|BARCODE|VBARCODE)\\s+(?!QR\\b)(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(-?\\d+)\\s+(-?\\d+)(.*)$");
    private static final Pattern QR = Pattern.compile(
            "^(B|VB|BARCODE|VBARCODE)\\s+(QR)\\s+(-?\\d+)\\s+(-?\\d+)(.*)$");
    private static final Pattern PCX = Pattern.compile("^(PCX)\\s+(-?\\d+)\\s+(-?\\d+)(.*)$");
    private static final Pattern TONE = Pattern.compile("^TONE\\s+-?\\d+\\s*$");
    /** Solo la cabecera de etiqueta {@code ! offset hres vres alto cantidad}; no los {@code ! U1 ...}. */
    private static final Pattern CABECERA = Pattern.compile("^(!\\s+\\d+\\s+\\d+\\s+\\d+\\s+)(\\d+)(\\s+\\d+\\s*)$");

    private final Integer tone;
    private final int dx;
    private final int dy;
    private final Modelo modelo;

    /** @param tone intensidad para la línea TONE; {@code null} respeta la de la plantilla. */
    public AjustesImpresora(Integer tone, int dx, int dy, Modelo modelo) {
        this.tone = tone;
        this.dx = dx;
        this.dy = dy;
        this.modelo = modelo == null ? Modelo.ZQ521 : modelo;
    }

    public static AjustesImpresora ninguno() {
        return NINGUNO;
    }

    /**
     * Lee {@code ValoresFormato.log}. Si el archivo no existe o una línea no es numérica, ese
     * valor queda en su defecto (sin corrimiento, TONE de la plantilla, ZQ-521): una
     * configuración dañada no debe impedir imprimir.
     */
    public static AjustesImpresora leer(File archivo) {
        if (archivo == null || !archivo.isFile()) return NINGUNO;
        List<String> lineas = new ArrayList<>(6);
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(archivo), StandardCharsets.ISO_8859_1))) {
            String l;
            while ((l = br.readLine()) != null && lineas.size() < 6) lineas.add(l.trim());
        } catch (IOException e) {
            return NINGUNO;
        }
        Integer tone = enteroOpcional(lineas, 0);
        int dy = valor(enteroOpcional(lineas, 1), 0);
        Integer modeloFlag = enteroOpcional(lineas, 4);
        Modelo modelo = modeloFlag != null && modeloFlag == 0 ? Modelo.RW420 : Modelo.ZQ521;
        int dx = valor(enteroOpcional(lineas, 5), 0);
        return new AjustesImpresora(tone, dx, dy, modelo);
    }

    public Integer tone()   { return tone; }
    public int dx()         { return dx; }
    public int dy()         { return dy; }
    public Modelo modelo()  { return modelo; }

    /** true si no altera ninguna línea: el motor se ahorra el recorrido. */
    public boolean esNeutro() {
        return tone == null && dx == 0 && dy == 0;
    }

    /**
     * Aplica los ajustes a una línea CPCL ya resuelta (sin placeholders). Devuelve la misma
     * instancia si la línea no lleva coordenadas. Las coordenadas nunca quedan negativas.
     */
    public String aplicar(String linea) {
        if (esNeutro() || linea.isEmpty()) return linea;
        char c = linea.charAt(0);
        if (c == 'T' || c == 'V') {
            Matcher m = TEXTO.matcher(linea);
            if (m.matches()) {
                return m.group(1) + ' ' + m.group(2) + ' ' + m.group(3) + ' '
                        + x(m.group(4)) + ' ' + y(m.group(5)) + m.group(6);
            }
            if (c == 'T' && tone != null && TONE.matcher(linea).matches()) {
                return "TONE " + tone;
            }
            if (c == 'V') {
                Matcher q = QR.matcher(linea);
                if (q.matches()) return q.group(1) + ' ' + q.group(2) + ' ' + x(q.group(3)) + ' ' + y(q.group(4)) + q.group(5);
                Matcher b = BARRA.matcher(linea);
                if (b.matches()) return barra(b);
            }
            return linea;
        }
        if (c == 'L' || c == 'I') {
            Matcher m = LINEA.matcher(linea);
            if (m.matches()) {
                return m.group(1) + ' ' + x(m.group(2)) + ' ' + y(m.group(3)) + ' '
                        + x(m.group(4)) + ' ' + y(m.group(5)) + m.group(6);
            }
            return linea;
        }
        if (c == 'B') {
            Matcher m = LINEA.matcher(linea); // BOX
            if (m.matches()) {
                return m.group(1) + ' ' + x(m.group(2)) + ' ' + y(m.group(3)) + ' '
                        + x(m.group(4)) + ' ' + y(m.group(5)) + m.group(6);
            }
            Matcher q = QR.matcher(linea);
            if (q.matches()) return q.group(1) + ' ' + q.group(2) + ' ' + x(q.group(3)) + ' ' + y(q.group(4)) + q.group(5);
            Matcher b = BARRA.matcher(linea);
            if (b.matches()) return barra(b);
            return linea;
        }
        if (c == 'P') {
            Matcher m = PCX.matcher(linea);
            if (m.matches()) return m.group(1) + ' ' + x(m.group(2)) + ' ' + y(m.group(3)) + m.group(4);
            return linea;
        }
        if (c == '!' && dy > 0) {
            // Si todo baja dy puntos, el alto de la etiqueta crece lo mismo para que nada se corte.
            Matcher m = CABECERA.matcher(linea);
            if (m.matches()) return m.group(1) + (Integer.parseInt(m.group(2)) + dy) + m.group(3);
        }
        return linea;
    }

    private String barra(Matcher b) {
        return b.group(1) + ' ' + b.group(2) + ' ' + b.group(3) + ' ' + b.group(4) + ' ' + b.group(5) + ' '
                + x(b.group(6)) + ' ' + y(b.group(7)) + b.group(8);
    }

    private String x(String v) { return String.valueOf(Math.max(0, Integer.parseInt(v) + dx)); }
    private String y(String v) { return String.valueOf(Math.max(0, Integer.parseInt(v) + dy)); }

    private static Integer enteroOpcional(List<String> lineas, int i) {
        if (i >= lineas.size() || lineas.get(i).isEmpty()) return null;
        try {
            return Integer.parseInt(lineas.get(i));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int valor(Integer v, int porDefecto) {
        return v == null ? porDefecto : v;
    }

    @Override
    public String toString() {
        return "AjustesImpresora{tone=" + (tone == null ? "plantilla" : tone)
                + ", dx=" + dx + ", dy=" + dy + ", modelo=" + modelo + '}';
    }
}
