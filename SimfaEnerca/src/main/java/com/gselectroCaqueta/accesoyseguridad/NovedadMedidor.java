package com.gselectroCaqueta.accesoyseguridad;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Novedad de terreno "medidor encontrado": un medidor que el técnico ve y que no corresponde a la cuenta o no
 * está en la ruta. Se registra con marca, número, lectura y observación, más foto y posición obligatorias
 * (viaja como evento novedad_medidor; la cuenta en pantalla solo sirve de referencia para ubicar el ciclo).
 * <p>
 * Queda en {@link AlmacenEstado} desde que se digita hasta que la foto vuelve con contenido: si la app se cierra
 * en medio, al volver se pide la foto o se descarta.
 */
public final class NovedadMedidor {

    public static final String SUFIJO_FOTO = "N";
    public static final int MAX_MARCA = 12, MAX_NUMERO = 16, MAX_LECTURA = 10, MAX_OBSERVACION = 120;

    public String marca = "", numero = "", lectura = "", observacion = "";
    public String cuentaReferencia = "", codCuentaReferencia = "", anno = "", mes = "";
    public Double lat, lng, precision;
    public long posicionEdadSeg = -1;
    public String proveedor = "";
    public long creadaMs;

    /** null si está completa; si no, qué falta. */
    public String validar() {
        if (t(numero).isEmpty()) return "Digite el numero del medidor encontrado";
        if (t(numero).length() > MAX_NUMERO) return "El numero admite " + MAX_NUMERO + " caracteres";
        if (t(marca).length() > MAX_MARCA) return "La marca admite " + MAX_MARCA + " caracteres";
        if (t(lectura).isEmpty()) return "Digite la lectura del medidor encontrado";
        if (!t(lectura).matches("\\d{1," + MAX_LECTURA + "}")) return "La lectura debe ser numerica (hasta " + MAX_LECTURA + " digitos)";
        if (t(observacion).length() > MAX_OBSERVACION) return "La observacion admite " + MAX_OBSERVACION + " caracteres";
        if (lat == null || lng == null) return "Sin posicion GPS: espere la senal antes de registrar la novedad";
        return null;
    }

    /** Datos del evento novedad_medidor. */
    public Map<String, Object> datos(String foto) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("tipo", "medidor_encontrado");
        d.put("marca", t(marca).toUpperCase());
        d.put("numero", t(numero).toUpperCase());
        d.put("lectura", t(lectura));
        d.put("observacion", t(observacion));
        d.put("cuenta_referencia", t(cuentaReferencia));
        d.put("foto", t(foto));
        d.put("lat", lat);
        d.put("lng", lng);
        if (precision != null) d.put("precision", precision);
        if (posicionEdadSeg >= 0) d.put("posicion_edad_seg", posicionEdadSeg);
        d.put("proveedor", t(proveedor));
        return d;
    }

    /** Línea de respaldo en DATOSDESALIDA (separada por ';', sin ';' dentro de los textos). */
    public String lineaSda(String foto, String fechaHora) {
        return String.join(";", t(cuentaReferencia), s(numero).toUpperCase(), s(marca).toUpperCase(), t(lectura), s(observacion),
                String.valueOf(lat), String.valueOf(lng), t(foto), t(fechaHora)) + ";\r\n";
    }

    // ------------------------------------------------------------------ persistencia

    private static final String[] CLAVES = {"marca", "numero", "lectura", "observacion", "cuenta", "cod_cuenta", "anno", "mes",
            "lat", "lng", "precision", "edad", "proveedor", "creada"};

    public void guardar(AlmacenEstado a) {
        String[] v = {marca, numero, lectura, observacion, cuentaReferencia, codCuentaReferencia, anno, mes,
                n(lat), n(lng), n(precision), String.valueOf(posicionEdadSeg), proveedor, String.valueOf(creadaMs)};
        for (int i = 0; i < CLAVES.length; i++) a.guardar(CLAVES[i], t(v[i]));
    }

    /** La novedad pendiente de foto, o null. */
    public static NovedadMedidor leer(AlmacenEstado a) {
        String numero = a.leer("numero");
        if (numero == null || numero.trim().isEmpty()) return null;
        NovedadMedidor n = new NovedadMedidor();
        n.marca = t(a.leer("marca"));
        n.numero = t(numero);
        n.lectura = t(a.leer("lectura"));
        n.observacion = t(a.leer("observacion"));
        n.cuentaReferencia = t(a.leer("cuenta"));
        n.codCuentaReferencia = t(a.leer("cod_cuenta"));
        n.anno = t(a.leer("anno"));
        n.mes = t(a.leer("mes"));
        n.lat = d(a.leer("lat"));
        n.lng = d(a.leer("lng"));
        n.precision = d(a.leer("precision"));
        n.posicionEdadSeg = (long) (d(a.leer("edad")) == null ? -1 : d(a.leer("edad")));
        n.proveedor = t(a.leer("proveedor"));
        Double c = d(a.leer("creada"));
        n.creadaMs = c == null ? 0 : c.longValue();
        return n;
    }

    public static void borrar(AlmacenEstado a) {
        a.borrar(CLAVES);
    }

    private static String t(String s) {
        return s == null ? "" : s.trim();
    }

    /** Texto libre sin el separador de la línea de respaldo. */
    private static String s(String x) {
        return t(x).replace(';', ',').replace('\r', ' ').replace('\n', ' ');
    }

    private static String n(Double v) {
        return v == null ? "" : String.valueOf(v);
    }

    private static Double d(String s) {
        try {
            return s == null || s.trim().isEmpty() ? null : Double.valueOf(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
