package com.gselectroCaqueta.sync;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Evento de campo para POST /api/v1/eventos/lote. Lo que pasa en la terminal y no es una lectura
 * (desbloqueo de foto por supervisor y, en las fases siguientes, búsquedas, contenedores, novedades y
 * validación de la factura) viaja como evento: una cola, un endpoint y una tabla para todos.
 * <pre>
 *   {"client_uid": "...", "tipo": "foto_desbloqueo", "cod_cuenta": "611523234", "anno": "2026", "mes": "10",
 *    "ocurrido_en": "2026-10-30T10:15:02.120-05:00", "lat": 5.53, "lng": -73.36, "datos": {...}}
 * </pre>
 * El operador y la terminal los pone el servidor desde el token. Reenviar el mismo client_uid no duplica.
 */
public final class EventoMovil {

    /** Foto obligatoria no tomada: el supervisor autorizó seguir. datos: supervisor, motivo, foto. */
    public static final String FOTO_DESBLOQUEO = "foto_desbloqueo";
    /** Cuenta leída (o con causal): paso del recorrido con la cuenta anterior, el salto y cómo llegó. */
    public static final String SECUENCIA = "secuencia";
    /** Búsqueda del técnico: desde qué cuenta, criterio, resultado y segundos. */
    public static final String BUSQUEDA = "busqueda";
    /** Banco de medidores: cuentas leídas entre "Iniciar contenedor" y "Cerrar". */
    public static final String CONTENEDOR = "contenedor";
    /** Nota de factura retenida (soporte de no entrega de factura al usuario). */
    public static final String FACTURA_RETENIDA = "factura_retenida";
    /** Muestra de control de la factura impresa: foto o codigo de barras (MuestraFactura). */
    public static final String VALIDACION_FACTURA = "validacion_factura";
    /** Novedad de terreno "medidor encontrado": marca, numero, lectura, observacion, foto y posicion. */
    public static final String NOVEDAD_MEDIDOR = "novedad_medidor";
    /** Mensaje del tecnico a la oficina (llega a su supervisor). */
    public static final String MENSAJE_TECNICO = "mensaje_tecnico";
    /** Ids de los mensajes de la oficina que el tecnico abrio en la bandeja. */
    public static final String MENSAJE_LEIDO = "mensaje_leido";

    private EventoMovil() {
    }

    public static String nuevoUid() {
        return UUID.randomUUID().toString();
    }

    /**
     * @param lat,lng posición vigente o null si no hay (no se inventa 0,0)
     * @param datos   campos propios del tipo; null = objeto vacío
     */
    public static String armar(String uid, String tipo, String codCuenta, String anno, String mes,
                                long ocurridoMs, Double lat, Double lng, Map<String, Object> datos) {
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("client_uid", uid);
        e.put("tipo", tipo);
        e.put("cod_cuenta", t(codCuenta));
        e.put("anno", t(anno));
        e.put("mes", t(mes));
        e.put("ocurrido_en", iso(ocurridoMs));
        if (lat != null && lng != null && !(lat == 0d && lng == 0d)) {
            e.put("lat", lat);
            e.put("lng", lng);
        }
        e.put("datos", datos == null ? new LinkedHashMap<String, Object>() : datos);
        return Json.escribir(e);
    }

    static String iso(long ms) {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ROOT).format(new Date(ms));
    }

    private static String t(String s) {
        return s == null ? "" : s.trim();
    }
}
