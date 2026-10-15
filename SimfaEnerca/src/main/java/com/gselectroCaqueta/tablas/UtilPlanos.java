package com.gselectroCaqueta.tablas;

import android.util.Log;

import com.gselectroCaqueta.accesoyseguridad.VariablesGlobales;
import com.gsutil.Utils;

import java.io.File;
import java.nio.charset.Charset;

/// <summary>
/// Utilidades comunes a los planos de longitud fija de salida.
/// </summary>
final class UtilPlanos {

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
        Log.e(tag, mensaje);
        try {
            UTILS.EscribirLinea(new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/LOGEVENTOS.LOG"), mensaje);
        } catch (Exception e) {
            Log.e(tag, "no se pudo escribir LOGEVENTOS: " + e.getMessage());
        }
    }
}
