package com.gselectroCaqueta.impresion;

import java.util.List;
import java.util.Locale;

/**
 * Formateadores que se aplican a un valor desde la plantilla: {@code {clave|formato|formato2}}.
 * <p>
 * Todos reciben y devuelven texto. Los numéricos toleran los dos separadores decimales
 * que circulan en los planos de EBSA ("920.7726" y "324,56"); la salida monetaria usa
 * punto de miles y sin decimales, como la factura oficial ("$ 396.852", "-$ 1").
 */
public final class Formatos {

    private static final String[] MESES = {
            "ENE", "FEB", "MAR", "ABR", "MAY", "JUN", "JUL", "AGO", "SEP", "OCT", "NOV", "DIC"};

    private Formatos() {}

    /** Aplica en orden la cadena de formatos extraída del placeholder. */
    public static String aplicar(String valor, List<String> formatos) {
        String v = valor == null ? "" : valor;
        for (String f : formatos) {
            v = aplicarUno(v, f);
        }
        return v;
    }

    static String aplicarUno(String v, String formato) {
        int sep = formato.indexOf(':');
        String nombre = sep < 0 ? formato : formato.substring(0, sep);
        String arg = sep < 0 ? "" : formato.substring(sep + 1);
        switch (nombre) {
            case "moneda":    return esVacio(v) ? "" : moneda(numero(v));
            case "monedaAng": return esVacio(v) ? "" : monedaAngulos(numero(v));
            case "entero":    return esVacio(v) ? "" : String.valueOf(Math.round(numero(v)));
            case "dec":       return esVacio(v) ? "" : decimal(numero(v), Integer.parseInt(arg));
            case "fecha":     return fecha(v, arg);
            case "izq":       return izquierda(v, Integer.parseInt(arg));
            case "der":       return derecha(v, Integer.parseInt(arg));
            case "centro":    return centro(v, Integer.parseInt(arg));
            case "max":       return v.length() <= Integer.parseInt(arg) ? v : v.substring(0, Integer.parseInt(arg));
            case "mayus":     return v.toUpperCase(Locale.ROOT);
            case "trim":      return v.trim();
            case "vacioSi0":  return esVacio(v) || numero(v) == 0 ? "" : v;
            case "guionSiVacio": return esVacio(v) ? "-" : v;
            case "sinCeros":  return sinCerosIzquierda(v);
            default:
                throw new IllegalArgumentException("Formato desconocido en la plantilla: '" + formato + "'");
        }
    }

    /** "0000023806269400" → "23806269400"; "0" se conserva. */
    public static String sinCerosIzquierda(String v) {
        String t = v == null ? "" : v.trim();
        int i = 0;
        while (i < t.length() - 1 && t.charAt(i) == '0') i++;
        return t.substring(i);
    }

    public static boolean esVacio(String v) {
        return v == null || v.trim().isEmpty() || v.trim().equals("-");
    }

    /**
     * Convierte texto a número tolerando "1.234,56", "1234.56", "324,56", " 920.7726 ".
     * Vacío o inválido devuelve 0: en impresión un dato ausente es un cero, no un error.
     */
    public static double numero(String s) {
        if (s == null) return 0;
        String t = s.trim();
        if (t.isEmpty() || t.equals("-")) return 0;
        boolean punto = t.indexOf('.') >= 0, coma = t.indexOf(',') >= 0;
        if (punto && coma) {
            t = t.replace(".", "").replace(',', '.');
        } else if (coma) {
            t = t.replace(',', '.');
        }
        try {
            return Double.parseDouble(t);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static long redondear(double v) {
        return Math.round(v);
    }

    /** "$ 396.852", "$ 0", "-$ 1". */
    public static String moneda(double v) {
        long r = Math.round(v);
        String miles = miles(Math.abs(r));
        return (r < 0 ? "-$ " : "$ ") + miles;
    }

    /** Estilo heredado de la móvil para negativos: "<$ 35.130>". */
    public static String monedaAngulos(double v) {
        long r = Math.round(v);
        return r < 0 ? "<$ " + miles(-r) + ">" : "$ " + miles(r);
    }

    public static String miles(long n) {
        String s = Long.toString(Math.abs(n));
        StringBuilder sb = new StringBuilder();
        int c = 0;
        for (int i = s.length() - 1; i >= 0; i--) {
            sb.append(s.charAt(i));
            if (++c % 3 == 0 && i > 0) sb.append('.');
        }
        if (n < 0) sb.append('-');
        return sb.reverse().toString();
    }

    /** Decimal con coma, n decimales: 920.7726 → "920,7726"; 0 → "0,00". */
    public static String decimal(double v, int n) {
        return String.format(Locale.ROOT, "%." + n + "f", v).replace('.', ',');
    }

    public static String izquierda(String v, int ancho) {
        if (v.length() >= ancho) return v.substring(0, ancho);
        StringBuilder sb = new StringBuilder(v);
        while (sb.length() < ancho) sb.append(' ');
        return sb.toString();
    }

    public static String derecha(String v, int ancho) {
        if (v.length() >= ancho) return v.substring(v.length() - ancho);
        StringBuilder sb = new StringBuilder();
        for (int i = v.length(); i < ancho; i++) sb.append(' ');
        return sb.append(v).toString();
    }

    public static String centro(String v, int ancho) {
        if (v.length() >= ancho) return v.substring(0, ancho);
        int izq = (ancho - v.length()) / 2;
        return izquierda(derecha(v, v.length() + izq), ancho);
    }

    // ---------------------------------------------------------------- fechas

    /**
     * Reformatea una fecha. Entrada tolerada: "yyyyMMdd", "dd/MM/yyyy", "dd/MM/yyyy h:mm:ss p. m.",
     * "yyyy-MM-dd", "d/MM/yyyy". Salida según patrón con tokens yyyy, yy, MMM (letras en
     * mayúscula), MM, dd; cualquier otro carácter se copia. Si la entrada no se reconoce,
     * se devuelve tal cual: nunca inventa una fecha.
     */
    public static String fecha(String entrada, String patron) {
        int[] ymd = descomponer(entrada);
        if (ymd == null) return entrada == null ? "" : entrada.trim();
        return formatear(ymd[0], ymd[1], ymd[2], patron.isEmpty() ? "dd/MM/yyyy" : patron);
    }

    public static String formatear(int y, int m, int d, String patron) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < patron.length()) {
            if (patron.startsWith("yyyy", i)) { sb.append(String.format(Locale.ROOT, "%04d", y)); i += 4; }
            else if (patron.startsWith("yy", i)) { sb.append(String.format(Locale.ROOT, "%02d", y % 100)); i += 2; }
            else if (patron.startsWith("MMM", i)) { sb.append(mesLetras(m)); i += 3; }
            else if (patron.startsWith("MM", i)) { sb.append(String.format(Locale.ROOT, "%02d", m)); i += 2; }
            else if (patron.startsWith("dd", i)) { sb.append(String.format(Locale.ROOT, "%02d", d)); i += 2; }
            else { sb.append(patron.charAt(i)); i++; }
        }
        return sb.toString();
    }

    public static String mesLetras(int m) {
        return m >= 1 && m <= 12 ? MESES[m - 1] : "???";
    }

    /** Devuelve {año, mes, día} o null si no reconoce el formato. */
    public static int[] descomponer(String entrada) {
        if (entrada == null) return null;
        String t = entrada.trim();
        if (t.isEmpty()) return null;
        try {
            if (t.length() >= 8 && t.substring(0, 8).matches("\\d{8}")) {
                return new int[]{Integer.parseInt(t.substring(0, 4)), Integer.parseInt(t.substring(4, 6)), Integer.parseInt(t.substring(6, 8))};
            }
            if (t.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
                return new int[]{Integer.parseInt(t.substring(0, 4)), Integer.parseInt(t.substring(5, 7)), Integer.parseInt(t.substring(8, 10))};
            }
            if (t.matches("\\d{1,2}/\\d{1,2}/\\d{4}.*")) {
                String[] p = t.split("[ T]")[0].split("/");
                return new int[]{Integer.parseInt(p[2]), Integer.parseInt(p[1]), Integer.parseInt(p[0])};
            }
        } catch (RuntimeException e) {
            return null;
        }
        return null;
    }

    /** Días entre dos fechas (cualquiera de los formatos tolerados); 0 si alguna no se reconoce. */
    public static int diasEntre(String desde, String hasta) {
        int[] a = descomponer(desde), b = descomponer(hasta);
        if (a == null || b == null) return 0;
        return (int) (diaJuliano(b[0], b[1], b[2]) - diaJuliano(a[0], a[1], a[2]));
    }

    /** Número de día juliano (algoritmo de Fliegel–Van Flandern), suficiente para restar fechas. */
    static long diaJuliano(int y, int m, int d) {
        long a = (14 - m) / 12;
        long yy = y + 4800 - a;
        long mm = m + 12 * a - 3;
        return d + (153 * mm + 2) / 5 + 365 * yy + yy / 4 - yy / 100 + yy / 400 - 32045;
    }
}
