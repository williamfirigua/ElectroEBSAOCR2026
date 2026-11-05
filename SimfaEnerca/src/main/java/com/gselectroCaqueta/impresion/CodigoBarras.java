package com.gselectroCaqueta.impresion;

/**
 * Contenido del código GS1-128 del cupón de pago, según las tres facturas oficiales revisadas:
 * <pre>
 *   (415) 7709998000483            GLN de EBSA
 *   (8020) 1000 + cuenta a 10 dígitos    referencia de pago, 14 dígitos
 *   (3900) total a pagar a 10 dígitos    incluye aseo cuando la factura es larga
 * </pre>
 * La móvil generaba hasta ahora GLN 7709998008083 y la cuenta sin el prefijo 1000. Las dos
 * GLN tienen dígito de control válido, así que la vigente la define EBSA: por eso la GLN y el
 * prefijo son parámetros de la plantilla ({@code ean.gln}, {@code ean.prefijo_ref}) y no constantes.
 * <p>
 * (8020) es un AI de longitud variable y GS1 exige un FNC1 entre su dato y (3900). En CPCL el FNC1
 * se escribe como el byte 134 (0x86) dentro del dato: es exactamente lo que hacía la móvil al partir
 * la línea del código en la columna 59 e inyectar el byte 134 entre las dos mitades. Aquí va
 * embebido en el dato ({@link #FNC1}), por lo que el .LOG debe escribirse y enviarse byte a byte
 * (ISO-8859-1), nunca reinterpretado como UTF-8.
 */
public final class CodigoBarras {

    /** FNC1 de CPCL para UCCEAN128: byte 0x86. Como char Latin-1 es U+0086. */
    public static final char FNC1 = '\u0086';

    private CodigoBarras() {}

    public static String gs1128(String gln, String prefijoRef, String cuenta, long totalPagar) {
        return gs1128(gln, prefijoRef, cuenta, totalPagar, true);
    }

    public static String gs1128(String gln, String prefijoRef, String cuenta, long totalPagar, boolean conFnc1) {
        return "415" + gln + "8020" + referencia(prefijoRef, cuenta) + (conFnc1 ? String.valueOf(FNC1) : "")
                + "3900" + Formatos.derecha(Long.toString(Math.max(0, totalPagar)), 10).replace(' ', '0');
    }

    public static String legible(String gln, String prefijoRef, String cuenta, long totalPagar) {
        return "(415)" + gln + "(8020)" + referencia(prefijoRef, cuenta) + "(3900)" + Formatos.derecha(Long.toString(Math.max(0, totalPagar)), 10).replace(' ', '0');
    }

    static String referencia(String prefijoRef, String cuenta) {
        String c = cuenta == null ? "" : cuenta.trim();
        return prefijoRef + Formatos.derecha(c, 10).replace(' ', '0');
    }

    /**
     * Cuenta (sin ceros a la izquierda) del código leído del cupón: 415 + GLN(13) + 8020 + referencia [FNC1]
     * + 3900 + valor(10). La referencia termina en la cuenta a 10 dígitos (con o sin el prefijo 1000).
     * Acepta lo que entregan los lectores: con "]C1", con GS o con los AI entre paréntesis.
     * null si no es un cupón de pago.
     */
    public static String cuentaDesdeCupon(String leido) {
        if (leido == null) return null;
        String x = leido.trim();
        if (x.length() >= 3 && x.charAt(0) == ']') x = x.substring(3);
        StringBuilder b = new StringBuilder(x.length());
        for (int i = 0; i < x.length(); i++) {
            char c = x.charAt(i);
            if (c >= '0' && c <= '9') b.append(c);
        }
        String d = b.toString();
        if (d.length() < 16 + 4 + 1 || !d.startsWith("415") || !d.startsWith("8020", 16)) return null;
        String ref = d.substring(20);
        if (ref.length() > 14 && ref.startsWith("3900", ref.length() - 14)) ref = ref.substring(0, ref.length() - 14);
        if (ref.isEmpty()) return null;
        String cuenta = ref.length() > 10 ? ref.substring(ref.length() - 10) : ref;
        int i = 0;
        while (i < cuenta.length() - 1 && cuenta.charAt(i) == '0') i++;
        cuenta = cuenta.substring(i);
        return cuenta.equals("0") ? null : cuenta;
    }

    /** Dígito de control GS1 (GLN, GTIN); útil para validar un GLN configurado en la plantilla. */
    public static boolean digitoControlValido(String codigo) {
        if (codigo == null || !codigo.matches("\\d{8,18}")) return false;
        int suma = 0;
        int n = codigo.length() - 1;
        for (int i = 0; i < n; i++) {
            int d = codigo.charAt(i) - '0';
            boolean peso3 = (n - 1 - i) % 2 == 0;
            suma += peso3 ? d * 3 : d;
        }
        return (10 - suma % 10) % 10 == codigo.charAt(n) - '0';
    }
}
