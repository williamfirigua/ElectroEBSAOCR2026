package com.gselectroCaqueta.tablas;

import android.util.Log;

import com.gselectroCaqueta.accesoyseguridad.VariablesGlobales;
import com.gsutil.Utils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;

/// <summary>
/// Utilidades comunes a los planos de longitud fija de salida.
/// </summary>
public final class UtilPlanos {

    /** writeBytes escribe 1 byte por caracter (Latin-1): se lee con el mismo juego para que sea simetrico. */
    static final Charset LATIN1 = Charset.forName("ISO-8859-1");

    private static final Utils UTILS = new Utils();

    private UtilPlanos() {
    }

    /** Quita espacios y ceros a la izquierda en valores numericos ("012" == "12"). */
    static String normalizar(String valor) {
        if (valor == null) return "";
        String v = valor.trim();
        if (v.length() == 0) return v;
        for (int i = 0; i < v.length(); i++) {
            if (!Character.isDigit(v.charAt(i))) return v;
        }
        int i = 0;
        while (i < v.length() - 1 && v.charAt(i) == '0') i++;
        return v.substring(i);
    }

    static boolean mismoValor(String a, String b) {
        return normalizar(a).equals(normalizar(b));
    }

    /** Deja constancia en LOGEVENTOS.LOG (y en logcat). Nunca lanza excepciones. */
    static void evento(String tag, String mensaje) {
        // Un bloqueo es una escritura fuera de lugar: se anota quién la pidió para encontrarla en el flujo.
        String m = mensaje.startsWith("[BLOQUEADO") ? mensaje + " | desde " + origen() : mensaje;
        Log.e(tag, m);
        try {
            // Con fecha y salto de línea, como el resto de LOGEVENTOS (antes quedaba pegado a la línea siguiente).
            UTILS.Log(new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/LOGEVENTOS.LOG"), m);
        } catch (Exception e) {
            Log.e(tag, "no se pudo escribir LOGEVENTOS: " + e.getMessage());
        }
    }

    /** Los primeros métodos de la app (fuera de tablas/) en la pila: quién llamó a la escritura. */
    static String origen() {
        StringBuilder sb = new StringBuilder();
        int n = 0;
        for (StackTraceElement e : new Throwable().getStackTrace()) {
            String c = e.getClassName();
            if (!c.startsWith("com.gselectroCaqueta") || c.startsWith("com.gselectroCaqueta.tablas.")) continue;
            if (n > 0) sb.append(" < ");
            sb.append(c.substring(c.lastIndexOf('.') + 1)).append('.').append(e.getMethodName()).append(':').append(e.getLineNumber());
            if (++n == 5) break;
        }
        return n == 0 ? "?" : sb.toString();
    }

    // ------------------------------------------------------------------ formato de los planos

    /** Mensaje para el tecnico del ultimo plano rechazado por formato (null si ninguno desde que se limpio). */
    public static volatile String ultimoErrorFormato;

    /**
     * Verifica que el plano sea de registros fijos de {@code largo} bytes: longitud multiplo de largo y el
     * primer y el ultimo registro terminan en \r\n. Antes un archivo generado con otra longitud (p. ej.
     * CLIENTE.SDA de 252) se aceptaba y cada lectura/escritura quedaba montada sobre dos registros.
     * No mueve el puntero de forma util: quien abre hace seek antes de cada lectura.
     *
     * @return null si esta bien; si no, el mensaje para el tecnico (queda tambien en ultimoErrorFormato y en LOGEVENTOS).
     */
    public static String errorFormato(RandomAccessFile f, String archivo, int largo) {
        String motivo = null;
        long longitud = -1;
        try {
            longitud = f.length();
            if (longitud % largo != 0) {
                motivo = "la longitud no es multiplo del registro (sobran " + (longitud % largo) + " bytes)";
            } else if (longitud > 0 && (!terminaEnCrLf(f, largo) || !terminaEnCrLf(f, longitud))) {
                motivo = "el primer o el ultimo registro no termina en CR LF";
            }
        } catch (IOException e) {
            motivo = "no se pudo verificar: " + e.getMessage();
        }
        if (motivo == null) return null;
        String nombre = archivo == null ? "?" : new File(archivo).getName();
        evento("FORMATO", "[FORMATO] " + archivo + ": registro esperado de " + largo + " bytes, longitud " + longitud + " -> " + motivo + "\r\n");
        String mensaje = "El archivo " + nombre + " tiene un formato distinto (registro de " + largo + " bytes esperado). Vuelva a cargar la ruta.";
        ultimoErrorFormato = mensaje;
        return mensaje;
    }

    /** Igual, para un archivo que no esta abierto. null tambien si no existe o no se puede abrir (eso lo maneja quien llama). */
    public static String errorFormato(File archivo, int largo) {
        if (archivo == null || !archivo.isFile()) return null;
        try (RandomAccessFile f = new RandomAccessFile(archivo, "r")) {
            return errorFormato(f, archivo.getAbsolutePath(), largo);
        } catch (IOException e) {
            return null;
        }
    }

    private static boolean terminaEnCrLf(RandomAccessFile f, long finRegistro) throws IOException {
        f.seek(finRegistro - 2);
        return f.read() == '\r' && f.read() == '\n';
    }

    // ------------------------------------------------------------------ bloqueo por cuentas desalineadas

    /**
     * Lo pone MenuDeLiquidacion.leerInformacionUsuario cuando CLIENTE.SDA, MEDIDOR.SDA, REGISTRO.SDA, REGISTRO.TXT y
     * CLIENTE.TXT no coinciden en la cuenta (y no se pudo reparar). Mientras tenga texto, escribir_* de CLIENTE.SDA,
     * MEDIDOR.SDA y REGISTRO.SDA se niegan. Se limpia en la siguiente lectura de cuenta.
     */
    private static volatile String bloqueoEscrituras;

    public static void bloquearEscrituras(String motivo) {
        bloqueoEscrituras = motivo == null ? "" : motivo;
    }

    public static void liberarEscrituras() {
        bloqueoEscrituras = null;
    }

    public static boolean escriturasBloqueadas() {
        return bloqueoEscrituras != null;
    }

    /** true (y deja constancia) si la escritura debe negarse por cuentas desalineadas. */
    static boolean rechazarPorBloqueo(String tag, String plano, String cuenta, int posicion) {
        String motivo = bloqueoEscrituras;
        if (motivo == null) return false;
        evento(tag, "[BLOQUEADO] cuentas desalineadas (" + motivo + "): no se escribe " + plano + " cuenta "
                + (cuenta == null ? "" : cuenta.trim()) + " posicion " + posicion + "\r\n");
        return true;
    }

    /** fsync antes de cerrar un plano escrito: sin esto un apagon puede dejar la factura impresa sin registro en disco. */
    static void sincronizar(String tag, RandomAccessFile f) {
        try {
            f.getFD().sync();
        } catch (IOException e) {
            Log.e(tag, "sync al cerrar: " + e.getMessage());
        }
    }
}
