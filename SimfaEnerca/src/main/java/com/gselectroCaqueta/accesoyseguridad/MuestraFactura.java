package com.gselectroCaqueta.accesoyseguridad;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Muestra aleatoria de control de la factura impresa: alrededor del 5 % de las cuentas de cada ruta, mitad con
 * foto de la factura y mitad con lectura del código de barras del cupón.
 * <p>
 * La elección sale de un hash (SHA-256) de la ruta, el período y la cuenta: es la misma si la app se cierra o
 * la cuenta se reimprime, el técnico no la puede anticipar por el orden de la ruta, y la oficina la puede
 * recalcular. Por ser aleatoria por cuenta el porcentaje real de una ruta oscila alrededor del 5 % (en 700
 * cuentas, entre 25 y 45 aproximadamente).
 */
public final class MuestraFactura {

    public static final String FOTO = "foto";
    public static final String BARRAS = "barras";
    public static final int PORCENTAJE = 5;

    /** Sufijo de la foto de la factura (cuenta_mesaño_1F.jpg): no se confunde con las fotos del medidor. */
    public static final String SUFIJO_FOTO = "F";

    private static final Pattern BARRAS_CUPON = Pattern.compile("(?m)^B\\s+UCCEAN128\\s+.*\\s(\\S+)\\s*$");

    private MuestraFactura() {
    }

    /** FOTO, BARRAS o null si la cuenta no cae en la muestra. */
    public static String tipo(String ruta, String anno, String mes, String cuenta) {
        return tipo(ruta, anno, mes, cuenta, PORCENTAJE);
    }

    static String tipo(String ruta, String anno, String mes, String cuenta, int porcentaje) {
        long n = hash("EBSA-MUESTRA|" + t(ruta) + "|" + t(anno) + "|" + t(mes) + "|" + t(cuenta));
        if (n % 10000 >= porcentaje * 100L) return null;
        return (n / 10000) % 2 == 0 ? FOTO : BARRAS;
    }

    /**
     * Dígitos del código de barras del cupón tal como quedó en el CPCL impreso (primera línea
     * {@code B UCCEAN128 ...}); "" si la factura no lo trae.
     */
    public static String barrasDelCpcl(String cpcl) {
        if (cpcl == null) return "";
        Matcher m = BARRAS_CUPON.matcher(cpcl);
        return m.find() ? soloDigitos(m.group(1)) : "";
    }

    /**
     * El código leído corresponde al impreso. Se comparan solo los dígitos: los lectores entregan el GS1-128
     * con o sin "]C1", con el FNC1 como GS (0x1D) o con los AI entre paréntesis.
     */
    public static boolean coincide(String leido, String esperado) {
        String e = soloDigitos(esperado);
        return !e.isEmpty() && e.equals(soloDigitos(quitarIdentificador(leido)));
    }

    public static String soloDigitos(String s) {
        if (s == null) return "";
        StringBuilder b = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= '0' && c <= '9') b.append(c);
        }
        return b.toString();
    }

    /** "]C1..." (identificador de simbología AIM): el 1 no es dato. */
    private static String quitarIdentificador(String s) {
        if (s == null) return "";
        String x = s.trim();
        return x.length() >= 3 && x.charAt(0) == ']' ? x.substring(3) : x;
    }

    private static long hash(String s) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
            return ((d[0] & 0xFFL) << 24) | ((d[1] & 0xFFL) << 16) | ((d[2] & 0xFFL) << 8) | (d[3] & 0xFFL);
        } catch (Exception e) {
            return Long.MAX_VALUE; // sin SHA-256 (no pasa en Android): ninguna cuenta en la muestra
        }
    }

    private static String t(String s) {
        return s == null ? "" : s.trim();
    }
}
