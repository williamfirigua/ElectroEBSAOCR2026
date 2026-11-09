package com.gselectroCaqueta.sync;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Un ciclo de envío: liquidaciones -> tracking -> LBL -> fotos (lo que factura primero).
 * Lo llama el servicio de sincronización cada cierto tiempo y cuando hay algo nuevo; no tiene hilos
 * propios ni dependencias de Android (se prueba en JVM contra la API real).
 *
 * Reglas, iguales para todo lo que se envía:
 *  - 200 aceptado/duplicado/guardado -> ENVIADO.
 *  - rechazado por el dato -> ERROR (no se reintenta solo; queda para revisión).
 *  - sin red, 429, 5xx -> se reprograma con espera creciente y se corta el ciclo (no tiene sentido
 *    seguir golpeando un servidor que no responde).
 *  - 401 -> se olvida el token, se intenta un login con la clave de la sesión si la hay; si no,
 *    REQUIERE_LOGIN. NADA se descarta por un problema de sesión.
 */
public class MotorSincronizacion {

    public enum Estado { COMPLETO, NADA_PENDIENTE, SIN_RED, SERVIDOR_NO_DISPONIBLE, REQUIERE_LOGIN }

    /** Datos para (re)autenticar. clave puede ser null si el técnico no ha digitado su clave en esta sesión de la app. */
    public interface Credenciales {
        String codOperador();

        String clave();

        String serialTerminal();

        String versionApp();
    }

    public interface AlmacenToken {
        String leer();

        void guardar(String token);

        void borrar();
    }

    /** Avisos hacia la app (marcar el índice de LBL, mostrar estado). Todo opcional. */
    public interface Observador {
        void archivoEnviado(ColaEnvio.Elemento e);

        void requiereLogin(String motivo);

        void registro(String mensaje);
    }

    public static final class Resumen {
        public Estado estado = Estado.NADA_PENDIENTE;
        public final Map<String, Integer> enviados = new LinkedHashMap<>();
        public final Map<String, Integer> errores = new LinkedHashMap<>();
        public String detalle;

        void suma(Map<String, Integer> m, String tipo, int n) {
            if (n > 0) m.put(tipo, (m.containsKey(tipo) ? m.get(tipo) : 0) + n);
        }

        @Override
        public String toString() {
            return estado + " enviados=" + enviados + " errores=" + errores + (detalle != null ? " " + detalle : "");
        }
    }

    static final int LOTE_LIQUIDACIONES = 50;
    static final int LOTE_TRACKING = 200;
    static final int LOTE_EVENTOS = 100;
    static final int ARCHIVOS_POR_CICLO = 30;
    private static final long ESPERA_BASE_MS = 30_000;
    private static final long ESPERA_MAX_MS = 30 * 60_000;

    private final ApiMovil api;
    private final ColaEnvio cola;
    private final AlmacenToken tokens;
    private final Credenciales credenciales;
    private final Observador observador;
    private final Random azar = new Random();

    public MotorSincronizacion(ApiMovil api, ColaEnvio cola, AlmacenToken tokens, Credenciales credenciales, Observador observador) {
        this.api = api;
        this.cola = cola;
        this.tokens = tokens;
        this.credenciales = credenciales;
        this.observador = observador;
    }

    /** Lo llama un solo hilo a la vez (el servicio lo garantiza). */
    public Resumen ejecutar(long ahoraMs) {
        Resumen r = new Resumen();
        if (!hayPendientes(ahoraMs)) {
            return r;
        }
        if (!asegurarToken(r)) {
            return r;
        }
        r.estado = Estado.COMPLETO;
        if (!liquidaciones(r, ahoraMs)) return r;
        // Después de las lecturas: un evento puede referirse a la lectura que se acaba de enviar.
        if (!eventos(r, ahoraMs)) return r;
        if (!tracking(r, ahoraMs)) return r;
        if (!archivos(r, ColaEnvio.LBL, ahoraMs)) return r;
        archivos(r, ColaEnvio.FOTO, ahoraMs);
        return r;
    }

    /** Donde se guarda lo que baja del servidor (bandeja y claves de codigos). Lo implementa la app. */
    public interface Receptor {
        /** Id del ultimo mensaje que ya tiene la terminal (0 = ninguno). */
        long ultimoMensaje();

        /** Mensajes nuevos, en orden de id. Cada uno: id, remitente, nombre, asunto, texto, enviado, leido. */
        void mensajes(List<Map<String, Object>> nuevos);

        /** Hora (ms) de la ultima descarga de claves; 0 = nunca. */
        long clavesDescargadasEn();

        /** Claves del tecnico: supervisor, nombre, clave (hex). */
        void claves(List<Map<String, Object>> claves, long ahoraMs);
    }

    /** Las claves cambian solo si se crea o cambia un supervisor: basta revisarlas cada 6 horas. */
    static final long CADA_CLAVES_MS = 6 * 60 * 60_000L;

    /**
     * Baja lo que la oficina tiene para el tecnico: mensajes nuevos y, cada 6 horas, las claves de los codigos de
     * supervisor. No envia nada (lo que el tecnico escribe o lee va como evento por la cola). Sin red o sin sesion
     * no hace nada: se intenta en el proximo latido.
     *
     * @return mensajes nuevos recibidos (-1 si no se pudo consultar)
     */
    public int recibir(Receptor receptor, long ahoraMs) {
        Resumen r = new Resumen();
        if (!asegurarToken(r)) return -1;
        int nuevos = 0;
        long desde = receptor.ultimoMensaje();
        for (int vuelta = 0; vuelta < 5; vuelta++) { // hasta 250 mensajes por latido
            ApiMovil.Respuesta resp = api.mensajes(desde);
            if (resp.codigo == 401) {
                sesionPerdida(r, resp);
                return -1;
            }
            if (resp.codigo == 404) return 0; // servidor sin el endpoint todavia
            if (!resp.ok()) return -1;
            List<Map<String, Object>> lote = new ArrayList<>();
            for (Object o : Json.lista(resp.cuerpo, "mensajes")) {
                if (o instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> m = (Map<String, Object>) o;
                    lote.add(m);
                }
            }
            if (lote.isEmpty()) break;
            receptor.mensajes(lote);
            nuevos += lote.size();
            long ultimo = desde;
            for (Map<String, Object> m : lote) {
                try {
                    ultimo = Math.max(ultimo, Long.parseLong(Json.texto(m, "id")));
                } catch (NumberFormatException ignored) {
                }
            }
            if (ultimo <= desde) break;
            desde = ultimo;
        }
        if (ahoraMs - receptor.clavesDescargadasEn() >= CADA_CLAVES_MS) {
            ApiMovil.Respuesta resp = api.clavesCodigo();
            if (resp.ok()) {
                List<Map<String, Object>> claves = new ArrayList<>();
                for (Object o : Json.lista(resp.cuerpo, "claves")) {
                    if (o instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> m = (Map<String, Object>) o;
                        claves.add(m);
                    }
                }
                receptor.claves(claves, ahoraMs);
            }
        }
        if (nuevos > 0) log("mensajes nuevos: " + nuevos);
        return nuevos;
    }

    /**
     * Carga de respaldo (ENVIOSGPRS.SDA + CO_COBRO.SDA) con la misma sesión del envío normal. Para el botón
     * "Subir respaldo" de la consulta de no enviados; no toca la cola.
     *
     * @return la respuesta del servidor; codigo 0 si no hubo sesión (r.estado = REQUIERE_LOGIN y r.detalle)
     */
    public ApiMovil.Respuesta subirRespaldo(File envio, File cobros, Resumen r) {
        if (!asegurarToken(r)) {
            return new ApiMovil.Respuesta(0, null, r.detalle);
        }
        ApiMovil.Respuesta resp = api.subirPlanos(envio, cobros);
        if (resp.codigo == 401) {
            tokens.borrar();
            api.token(null);
            if (!login(r)) {
                return new ApiMovil.Respuesta(0, null, r.detalle);
            }
            resp = api.subirPlanos(envio, cobros);
        }
        return resp;
    }

    private boolean hayPendientes(long ahoraMs) {
        for (String t : new String[]{ColaEnvio.LIQUIDACION, ColaEnvio.EVENTO, ColaEnvio.TRACKING, ColaEnvio.LBL, ColaEnvio.FOTO}) {
            if (!cola.pendientes(t, 1, ahoraMs).isEmpty()) return true;
        }
        return false;
    }

    // ------------------------------------------------------------------ sesión

    private boolean asegurarToken(Resumen r) {
        String t = tokens.leer();
        if (t != null && !t.isEmpty()) {
            api.token(t);
            return true;
        }
        return login(r);
    }

    private boolean login(Resumen r) {
        String clave = credenciales == null ? null : credenciales.clave();
        if (clave == null || clave.isEmpty()) {
            return pedirLogin(r, "sin_sesion");
        }
        ApiMovil.Respuesta resp = api.login(credenciales.codOperador(), clave, credenciales.serialTerminal(), credenciales.versionApp());
        if (resp.ok() && resp.campo("token") != null) {
            tokens.guardar(resp.campo("token"));
            api.token(resp.campo("token"));
            return true;
        }
        if (resp.codigo == 401 || resp.codigo == 403 || resp.codigo == 422) {
            String motivo = resp.campo("error");
            String detalle = resp.campo("detalle");
            return pedirLogin(r, (motivo != null ? motivo : "login_rechazado") + (detalle != null ? " | " + detalle : ""));
        }
        sinServicio(r, resp);
        return false; // sin token no se puede seguir, aunque el error sea solo del servidor
    }

    private boolean pedirLogin(Resumen r, String motivo) {
        r.estado = Estado.REQUIERE_LOGIN;
        r.detalle = motivo;
        if (observador != null) observador.requiereLogin(motivo);
        return false;
    }

    /** 401 en medio del ciclo: el token dejó de servir (vencido, reemplazado, operador inactivo). */
    private boolean sesionPerdida(Resumen r, ApiMovil.Respuesta resp) {
        tokens.borrar();
        api.token(null);
        String motivo = resp.campo("error");
        log("sesión no válida: " + motivo);
        // Una sola vez por ciclo: si el login con la clave de la sesión funciona, se sigue en el próximo ciclo.
        if (login(r)) {
            r.estado = Estado.COMPLETO;
            r.detalle = "sesion_renovada";
        }
        return false;
    }

    /**
     * Sin red: se corta el ciclo (nada más va a salir). Error del servidor en UN endpoint (500 en tracking,
     * disco lleno en archivos...): solo ese tipo espera el reintento; los demás tipos siguen saliendo.
     */
    private boolean sinServicio(Resumen r, ApiMovil.Respuesta resp) {
        boolean sinRed = resp.codigo == ApiMovil.SIN_RED;
        r.estado = sinRed ? Estado.SIN_RED : Estado.SERVIDOR_NO_DISPONIBLE;
        r.detalle = resp.toString();
        log("sin servicio: " + resp);
        return !sinRed;
    }

    // ------------------------------------------------------------------ liquidaciones

    private boolean liquidaciones(Resumen r, long ahoraMs) {
        while (true) {
            List<ColaEnvio.Elemento> lote = cola.pendientes(ColaEnvio.LIQUIDACION, LOTE_LIQUIDACIONES, ahoraMs);
            if (lote.isEmpty()) return true;

            List<String> items = new ArrayList<>(lote.size());
            Map<String, ColaEnvio.Elemento> porUid = new HashMap<>();
            for (ColaEnvio.Elemento e : lote) {
                items.add(e.datos);
                porUid.put(e.uid, e);
            }
            ApiMovil.Respuesta resp = api.enviarLiquidaciones(items);

            if (resp.codigo == 401) return sesionPerdida(r, resp);
            if (resp.codigo == 422) {
                // Sobre mal armado (error de la app). Se marcan para revisión en vez de reintentar sin fin.
                for (ColaEnvio.Elemento e : lote) cola.marcarError(e.id, "lote_invalido: " + resp.campo("detalle"), ahoraMs);
                r.suma(r.errores, ColaEnvio.LIQUIDACION, lote.size());
                continue;
            }
            if (!resp.ok()) {
                reprogramar(lote, ahoraMs, resp.toString());
                return sinServicio(r, resp);
            }

            List<Long> enviados = new ArrayList<>();
            for (Object o : Json.lista(resp.cuerpo, "resultados")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> res = (Map<String, Object>) o;
                ColaEnvio.Elemento e = porUid.remove(Json.texto(res, "client_uid"));
                if (e == null) continue;
                String estado = Json.texto(res, "estado");
                if ("aceptado".equals(estado) || "duplicado".equals(estado)) {
                    enviados.add(e.id);
                } else {
                    String motivo = Json.texto(res, "motivo") + ": " + Json.texto(res, "detalle");
                    cola.marcarError(e.id, motivo, ahoraMs);
                    log("liquidación " + e.uid + " rechazada por el servidor: " + motivo);
                    r.suma(r.errores, ColaEnvio.LIQUIDACION, 1);
                }
            }
            cola.marcarEnviados(enviados, ahoraMs);
            r.suma(r.enviados, ColaEnvio.LIQUIDACION, enviados.size());
            // Sin resultado (no debería pasar): se reintenta luego.
            if (!porUid.isEmpty()) reprogramar(new ArrayList<>(porUid.values()), ahoraMs, "sin_resultado");
        }
    }

    // ------------------------------------------------------------------ eventos

    /** Igual que las liquidaciones: un resultado por evento (aceptado | duplicado | rechazado). */
    private boolean eventos(Resumen r, long ahoraMs) {
        while (true) {
            List<ColaEnvio.Elemento> lote = cola.pendientes(ColaEnvio.EVENTO, LOTE_EVENTOS, ahoraMs);
            if (lote.isEmpty()) return true;

            List<String> eventos = new ArrayList<>(lote.size());
            Map<String, ColaEnvio.Elemento> porUid = new HashMap<>();
            for (ColaEnvio.Elemento e : lote) {
                eventos.add(e.datos);
                porUid.put(e.uid, e);
            }
            ApiMovil.Respuesta resp = api.enviarEventos(eventos);

            if (resp.codigo == 401) return sesionPerdida(r, resp);
            if (resp.codigo == 404) {
                // Servidor sin el endpoint todavía: quedan en la cola y se reintentan, sin frenar lo demás.
                reprogramar(lote, ahoraMs, "eventos_no_disponible");
                log("eventos: el servidor no tiene /eventos/lote; se reintenta luego");
                return true;
            }
            if (resp.codigo == 422) {
                for (ColaEnvio.Elemento e : lote) cola.marcarError(e.id, "lote_invalido: " + resp.campo("detalle"), ahoraMs);
                r.suma(r.errores, ColaEnvio.EVENTO, lote.size());
                continue;
            }
            if (!resp.ok()) {
                reprogramar(lote, ahoraMs, resp.toString());
                return sinServicio(r, resp);
            }

            List<Long> enviados = new ArrayList<>();
            for (Object o : Json.lista(resp.cuerpo, "resultados")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> res = (Map<String, Object>) o;
                ColaEnvio.Elemento e = porUid.remove(Json.texto(res, "client_uid"));
                if (e == null) continue;
                String estado = Json.texto(res, "estado");
                if ("aceptado".equals(estado) || "duplicado".equals(estado)) {
                    enviados.add(e.id);
                } else {
                    String motivo = Json.texto(res, "motivo") + ": " + Json.texto(res, "detalle");
                    cola.marcarError(e.id, motivo, ahoraMs);
                    log("evento " + e.uid + " rechazado por el servidor: " + motivo);
                    r.suma(r.errores, ColaEnvio.EVENTO, 1);
                }
            }
            cola.marcarEnviados(enviados, ahoraMs);
            r.suma(r.enviados, ColaEnvio.EVENTO, enviados.size());
            if (!porUid.isEmpty()) reprogramar(new ArrayList<>(porUid.values()), ahoraMs, "sin_resultado");
        }
    }

    // ------------------------------------------------------------------ tracking

    private static int entero(String v) {
        try {
            return Integer.parseInt(v.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private boolean tracking(Resumen r, long ahoraMs) {
        while (true) {
            List<ColaEnvio.Elemento> lote = cola.pendientes(ColaEnvio.TRACKING, LOTE_TRACKING, ahoraMs);
            if (lote.isEmpty()) return true;
            List<String> puntos = new ArrayList<>(lote.size());
            for (ColaEnvio.Elemento e : lote) puntos.add(e.datos);

            ApiMovil.Respuesta resp = api.enviarUbicaciones(puntos);
            if (resp.codigo == 401) return sesionPerdida(r, resp);
            if (resp.codigo == 422) {
                for (ColaEnvio.Elemento e : lote) cola.marcarError(e.id, "lote_invalido: " + resp.campo("detalle"), ahoraMs);
                r.suma(r.errores, ColaEnvio.TRACKING, lote.size());
                continue;
            }
            if (!resp.ok()) {
                reprogramar(lote, ahoraMs, resp.toString());
                return sinServicio(r, resp);
            }
            // Solo se borra con la confirmación del servidor: HTTP 200 y ok:true en el cuerpo. Un 200 sin ok
            // (proxy, página de error) no cuenta como recibido.
            if (!"true".equalsIgnoreCase(String.valueOf(resp.campo("ok")))) {
                reprogramar(lote, ahoraMs, "sin_confirmacion " + resp);
                return sinServicio(r, resp);
            }
            // El servidor guarda todos los válidos y descarta reenvíos: el lote entero queda enviado. Lo que no
            // guardó (formato o más viejo que su retención) lo informa en total_descartadas: se deja constancia.
            String descartadas = resp.campo("total_descartadas");
            if (descartadas != null && !descartadas.equals("0")) {
                log("tracking: el servidor descartó " + descartadas + " de " + lote.size() + " puntos (vencidos "
                        + resp.campo("vencidos") + "); ejemplo " + lote.get(0).datos);
            } else {
                String validos = resp.campo("validos");
                if (validos != null && !validos.equals(String.valueOf(lote.size()))) {
                    log("tracking: el servidor descartó " + (lote.size() - entero(validos)) + " de " + lote.size()
                            + " puntos por formato; ejemplo " + lote.get(0).datos);
                }
            }
            List<Long> ids = new ArrayList<>(lote.size());
            for (ColaEnvio.Elemento e : lote) ids.add(e.id);
            cola.marcarEnviados(ids, ahoraMs);
            r.suma(r.enviados, ColaEnvio.TRACKING, ids.size());
        }
    }

    // ------------------------------------------------------------------ archivos

    private boolean archivos(Resumen r, String tipo, long ahoraMs) {
        for (ColaEnvio.Elemento e : cola.pendientes(tipo, ARCHIVOS_POR_CICLO, ahoraMs)) {
            File f = new File(e.archivo == null ? "" : e.archivo);
            if (!f.isFile()) {
                cola.marcarError(e.id, "archivo_no_existe: " + e.archivo, ahoraMs);
                log(tipo + " " + f.getName() + " rechazado: archivo_no_existe en la terminal (" + e.archivo + ")");
                r.suma(r.errores, tipo, 1);
                continue;
            }
            Map<String, String> campos = new LinkedHashMap<>();
            for (Map.Entry<String, Object> c : Json.objeto(e.datos).entrySet()) {
                campos.put(c.getKey(), c.getValue() == null ? "" : String.valueOf(c.getValue()));
            }
            ApiMovil.Respuesta resp = api.subirArchivo(f, campos);

            if (resp.codigo == 401) return sesionPerdida(r, resp);
            if (resp.ok()) {
                List<Long> ids = new ArrayList<>(1);
                ids.add(e.id);
                cola.marcarEnviados(ids, ahoraMs);
                r.suma(r.enviados, tipo, 1);
                if (observador != null) observador.archivoEnviado(e);
                continue;
            }
            if (resp.codigo == 422) {
                if (Boolean.TRUE.equals(resp.cuerpo == null ? null : resp.cuerpo.get("reintentar"))) {
                    reprogramar(single(e), ahoraMs, "hash_no_coincide");
                } else {
                    String motivo = resp.campo("motivo") + ": " + resp.campo("detalle");
                    cola.marcarError(e.id, motivo, ahoraMs);
                    log(tipo + " " + f.getName() + " rechazado por el servidor: " + motivo + " | respuesta " + resp + " | datos " + e.datos);
                    r.suma(r.errores, tipo, 1);
                }
                continue;
            }
            log(tipo + " " + f.getName() + " no enviado, se reintenta: " + resp);
            reprogramar(single(e), ahoraMs, resp.toString());
            return sinServicio(r, resp);
        }
        return true;
    }

    // ------------------------------------------------------------------ apoyo

    private void reprogramar(List<ColaEnvio.Elemento> lote, long ahoraMs, String motivo) {
        int intentos = 0;
        List<Long> ids = new ArrayList<>(lote.size());
        for (ColaEnvio.Elemento e : lote) {
            ids.add(e.id);
            intentos = Math.max(intentos, e.intentos);
        }
        cola.reprogramar(ids, ahoraMs + espera(intentos), motivo);
    }

    /** 30 s, 1 min, 2 min... hasta 30 min, con ±20 % para que los equipos no reintenten todos a la vez. */
    long espera(int intentos) {
        long base = ESPERA_BASE_MS << Math.min(intentos, 6);
        base = Math.min(base, ESPERA_MAX_MS);
        return (long) (base * (0.8 + 0.4 * azar.nextDouble()));
    }

    private static List<ColaEnvio.Elemento> single(ColaEnvio.Elemento e) {
        List<ColaEnvio.Elemento> l = new ArrayList<>(1);
        l.add(e);
        return l;
    }

    private void log(String m) {
        if (observador != null) observador.registro(m);
    }
}
