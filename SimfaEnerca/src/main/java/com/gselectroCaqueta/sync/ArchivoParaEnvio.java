package com.gselectroCaqueta.sync;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Encola fotos y LBL para POST /api/v1/archivos. Los metadatos van en la cola (no se recalculan al enviar:
 * para entonces la Activity puede estar en otra ruta). El SHA-256 lo calcula ApiMovil al subir.
 *
 * uid = tipo|periodo|nombre|tamaño|fecha de modificación: el mismo archivo encolado dos veces es uno solo; si
 * se reimprime y el LBL cambia, es otro elemento y el servidor lo reemplaza.
 */
public final class ArchivoParaEnvio {

    private ArchivoParaEnvio() {
    }

    /**
     * @param ruta municipio + sección + división (obligatoria para LBL; en fotos se guarda como referencia)
     * @return false si el archivo no existe o el nombre no trae la cuenta
     */
    public static boolean encolar(ColaEnvio cola, String tipo, File archivo, String anno, String mes, String ciclo, String ruta) {
        if (!ColaEnvio.FOTO.equals(tipo) && !ColaEnvio.LBL.equals(tipo)) throw new IllegalArgumentException(tipo);
        if (archivo == null || !archivo.isFile()) return false;
        String nombre = archivo.getName();
        String cuenta = cuentaDe(nombre);
        if (cuenta.isEmpty()) return false;

        Map<String, Object> d = new LinkedHashMap<>();
        d.put("tipo", tipo);
        d.put("nombre", nombre);
        d.put("anno", t(anno));
        d.put("mes", t(mes));
        d.put("ciclo", t(ciclo));
        d.put("cod_cuenta", cuenta);
        d.put("ruta", t(ruta));
        String uid = tipo + "|" + t(anno) + t(mes) + t(ciclo) + "|" + nombre + "|" + archivo.length() + "|" + archivo.lastModified();
        cola.encolar(tipo, uid, Json.escribir(d), archivo.getAbsolutePath());
        return true;
    }

    /** Fotos (cuenta_MAAAA_n.jpg) y LBL (cuenta_AAAA_M.LOG) empiezan con la cuenta hasta el primer '_'. */
    static String cuentaDe(String nombre) {
        int i = nombre.indexOf('_');
        String c = i > 0 ? nombre.substring(0, i).trim() : "";
        for (int k = 0; k < c.length(); k++) {
            if (!Character.isDigit(c.charAt(k))) return "";
        }
        return c.length() >= 4 && c.length() <= 13 ? c : "";
    }

    private static String t(String s) {
        return s == null ? "" : s.trim();
    }
}
