package com.gselectroCaqueta.sync;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Carga de la ruta por la API (bloque C). Reemplaza Traer Ruta + Recibir Datos del SOAP:
 *
 * 1. {@link #rutas()}: las rutas asignadas al técnico (sin digitar el ciclo) y el modo que fijó Global
 *    (api | soap).
 * 2. {@link #descargar}: baja el zip a DATOSDEENTRADA/C{mmm}{sss}{d}.ZIP. Si la señal se cae, lo bajado
 *    queda en un .part y la siguiente vez sigue desde ahí (Range). Al terminar compara el SHA-256 con el que
 *    mandó el servidor; si la ruta se volvió a generar entretanto, empieza de cero.
 * 3. {@link #eventoCargada} / {@link #eventoFinalizada}: encolan el aviso para el seguimiento de la web
 *    (rutasactivas). Van por la cola de eventos, así que llegan aunque ahora no haya señal, y también se
 *    usan cuando la ruta se cargó por el SOAP.
 *
 * Todo corre en un hilo de fondo; nada aquí toca la pantalla.
 */
public final class DescargaRuta {

    /** Una ruta asignada al técnico, como la manda GET /rutas. */
    public static final class Ruta {
        public String codCycle, carpeta, ruta, municipio, seccion, division, estadoNombre, asignadaEn, cargadaEn;
        public int estado, descargas;
        public Integer registros;

        /** Texto para la lista de la pantalla. */
        public String descripcion() {
            StringBuilder sb = new StringBuilder();
            sb.append(ruta).append("  ·  ciclo ").append(carpeta);
            if (registros != null) sb.append("  ·  ").append(registros).append(" reg.");
            if (estado >= 2) sb.append("\n(ya cargada").append(cargadaEn != null ? " " + cargadaEn : "").append(")");
            return sb.toString();
        }
    }

    /** Respuesta de la lista. */
    public static final class Lista {
        public final boolean ok;
        public final String modo;       // api | soap
        public final List<Ruta> rutas;
        public final String mensaje;
        /** Falta (o no sirvió) el código y la clave del técnico: la pantalla los pide. */
        public boolean necesitaSesion;

        Lista(boolean ok, String modo, List<Ruta> rutas, String mensaje) {
            this.ok = ok;
            this.modo = modo;
            this.rutas = rutas;
            this.mensaje = mensaje;
        }
    }

    /** Resultado de la descarga. */
    public static final class Resultado {
        public final boolean ok;
        public final File zip;
        public final long bytes;
        public final String mensaje;

        Resultado(boolean ok, File zip, long bytes, String mensaje) {
            this.ok = ok;
            this.zip = zip;
            this.bytes = bytes;
            this.mensaje = mensaje;
        }
    }

    private static final String PREFS = "EbsaDescargaRuta";
    private static final int INTENTOS = 6;

    private final Context ctx;
    private final ConfigSync config;
    private final ApiMovil api;

    /** La última falla de sesión fue por código/clave (o porque nunca se digitaron): hay que pedirlos. */
    private boolean faltaSesion;

    public DescargaRuta(Context ctx) {
        this.ctx = ctx.getApplicationContext();
        this.config = new ConfigSync(ctx);
        completarConfig();
        this.api = new ApiMovil(config.baseApi());
    }

    /**
     * Sin ruta cargada el técnico todavía no pasó por la liquidación, que es donde se guardaban la URL, el serial
     * y la versión para la API: se toman de la licencia y de la configuración de comunicaciones.
     */
    private void completarConfig() {
        try {
            String base = config.baseApi().isEmpty() ? com.gselectroCaqueta.moduloLicencia.LicenciaTerminal.baseApi(ctx) : "";
            String serial = "";
            String version = "";
            com.gselectroCaqueta.moduloLicencia.LicenciaTerminal lic = new com.gselectroCaqueta.moduloLicencia.LicenciaTerminal(ctx);
            if (config.serialTerminal().isEmpty()) {
                com.gselectroCaqueta.moduloLicencia.LicenciaTerminal.Revision rv = lic.revisar();
                if (rv.vigente()) serial = rv.licencia.codMovil;
            }
            if (config.versionApp().isEmpty()) version = lic.versionApp();
            config.configurar(base, serial, version);
        } catch (Throwable ignored) {
        }
    }

    /** La última operación falló porque no hay sesión (faltan código y clave, o no coinciden). */
    public boolean faltaSesion() {
        return faltaSesion;
    }

    /** Código del dueño de la licencia de este celular (para sugerirlo al pedir la clave). */
    public String codigoSugerido() {
        try {
            com.gselectroCaqueta.moduloLicencia.LicenciaTerminal.Revision rv = new com.gselectroCaqueta.moduloLicencia.LicenciaTerminal(ctx).revisar();
            if (rv.vigente() && !rv.licencia.operador.isEmpty()) return rv.licencia.operador;
        } catch (Throwable ignored) {
        }
        return config.codOperador();
    }

    /**
     * Inicia la sesión de la API con lo que digitó el técnico (el servidor valida la clave contra la web; sin ruta
     * cargada el celular no tiene con qué validarla). El usuario EBSA se traduce al código si se conoce.
     *
     * @return null si quedó la sesión; si no, el motivo para mostrar
     */
    public String iniciarSesion(String codigoOUsuario, String clave) {
        String cod = codigoOUsuario == null ? "" : codigoOUsuario.trim();
        try {
            String traducido = new com.gselectroCaqueta.moduloLicencia.LicenciaTerminal(ctx).codigoDeUsuario(cod);
            if (traducido != null) cod = traducido;
        } catch (Throwable ignored) {
        }
        ConfigSync.recordarSesion(ctx, cod, clave);
        return renovarSesion();
    }

    // ------------------------------------------------------------------ lista

    public Lista rutas() {
        if (config.baseApi().isEmpty()) return new Lista(false, "api", new ArrayList<>(), "No hay URL de la API configurada (Configurar Web/IP).");
        String falla = asegurarSesion();
        if (falla != null) {
            Lista l = new Lista(false, "api", new ArrayList<>(), falla);
            l.necesitaSesion = faltaSesion;
            return l;
        }
        ApiMovil.Respuesta r = api.rutas();
        if (r.codigo == 401) {
            String f2 = renovarSesion();
            if (f2 != null) {
                Lista l = new Lista(false, "api", new ArrayList<>(), f2);
                l.necesitaSesion = faltaSesion;
                return l;
            }
            r = api.rutas();
        }
        if (r.codigo == ApiMovil.SIN_RED) return new Lista(false, "api", new ArrayList<>(), "Sin conexión con el servidor.");
        if (!r.ok() || r.cuerpo == null) return new Lista(false, "api", new ArrayList<>(), "El servidor no respondió la lista de rutas (" + r.codigo + ").");

        List<Ruta> rutas = new ArrayList<>();
        Object lista = r.cuerpo.get("rutas");
        if (lista instanceof List) {
            for (Object o : (List<?>) lista) {
                if (!(o instanceof Map)) continue;
                @SuppressWarnings("unchecked") Map<String, Object> m = (Map<String, Object>) o;
                Ruta x = new Ruta();
                x.codCycle = Json.texto(m, "cod_cycle");
                x.carpeta = Json.texto(m, "carpeta");
                x.ruta = Json.texto(m, "ruta");
                x.municipio = Json.texto(m, "municipio");
                x.seccion = Json.texto(m, "seccion");
                x.division = Json.texto(m, "division");
                x.estadoNombre = Json.texto(m, "estado_nombre");
                x.asignadaEn = Json.texto(m, "asignada_en");
                x.cargadaEn = Json.texto(m, "cargada_en");
                x.estado = entero(Json.texto(m, "estado"), 0);
                x.descargas = entero(Json.texto(m, "descargas"), 0);
                String reg = Json.texto(m, "registros");
                x.registros = reg == null ? null : entero(reg, 0);
                if (x.codCycle != null && x.ruta != null && x.carpeta != null) rutas.add(x);
            }
        }
        String modo = "soap".equalsIgnoreCase(r.campo("modo")) ? "soap" : "api";
        return new Lista(true, modo, rutas, null);
    }

    // ------------------------------------------------------------------ descarga

    /**
     * @param destino DATOSDEENTRADA/C{mmm}{sss}{d}.ZIP (el mismo nombre que dejaba el SOAP)
     */
    public Resultado descargar(Ruta ruta, File destino, ApiMovil.Progreso progreso) {
        String falla = asegurarSesion();
        if (falla != null) return new Resultado(false, null, 0, falla);

        File parte = new File(destino.getPath() + ".part");
        SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String clave = ruta.codCycle + "|" + ruta.ruta;
        String shaGuardado = p.getString(clave, null);
        if (shaGuardado == null && parte.exists()) parte.delete(); // un .part sin huella no se sabe de qué es

        String ultimoError = null;
        for (int intento = 0; intento < INTENTOS; intento++) {
            long desde = parte.exists() ? parte.length() : 0;
            ApiMovil.Tramo t = api.bajarPaquete(ruta.codCycle, ruta.ruta, parte, desde, progreso);

            if (t.codigo == 401) {
                String e = renovarSesion();
                if (e != null) return new Resultado(false, null, 0, e);
                continue;
            }
            if (t.codigo == ApiMovil.SIN_RED) return new Resultado(false, null, 0, "Sin señal. Lo descargado se conserva: vuelva a intentar y sigue donde quedó.");
            if (t.codigo == 403 || t.codigo == 409) return new Resultado(false, null, 0, t.error != null ? t.error : "El servidor no entregó la ruta (" + t.codigo + ").");
            if (t.codigo == 416) { // ya estaba completo (o el paquete cambió y es más corto)
                if (t.sha256 != null && t.sha256.equals(shaGuardado) && parte.length() == t.total) {
                    return terminar(parte, destino, t.sha256, p, clave);
                }
                parte.delete();
                p.edit().remove(clave).apply();
                shaGuardado = null;
                continue;
            }
            if (t.codigo == 200 || t.codigo == 206) {
                if (t.sha256 != null && shaGuardado != null && !t.sha256.equals(shaGuardado) && t.codigo == 206) {
                    // La ruta se volvió a generar mientras tanto: lo bajado es de la versión vieja.
                    parte.delete();
                    p.edit().putString(clave, t.sha256).apply();
                    shaGuardado = t.sha256;
                    continue;
                }
                if (t.sha256 != null && !t.sha256.equals(shaGuardado)) {
                    p.edit().putString(clave, t.sha256).apply();
                    shaGuardado = t.sha256;
                }
                if (t.total > 0 && parte.length() >= t.total) return terminar(parte, destino, shaGuardado, p, clave);
                ultimoError = "La descarga se cortó";
                continue; // se cortó a mitad: sigue desde lo bajado
            }
            // TRANSFERENCIA_FALLIDA u otro: lo bajado se conserva, se vuelve a pedir desde ahí.
            ultimoError = t.error;
            try {
                Thread.sleep(1500L * (intento + 1));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return new Resultado(false, null, 0, "No se completó la descarga" + (ultimoError != null ? " (" + ultimoError + ")" : "")
                + ". Lo descargado se conserva: vuelva a intentar y sigue donde quedó.");
    }

    private Resultado terminar(File parte, File destino, String sha, SharedPreferences p, String clave) {
        try {
            String real = ApiMovil.sha256(parte);
            if (sha != null && !sha.equalsIgnoreCase(real)) {
                parte.delete();
                p.edit().remove(clave).apply();
                return new Resultado(false, null, 0, "El archivo llegó dañado; vuelva a descargar.");
            }
        } catch (java.io.IOException e) {
            return new Resultado(false, null, 0, "No se pudo verificar el archivo: " + e.getMessage());
        }
        if (destino.exists()) destino.delete();
        if (!parte.renameTo(destino)) return new Resultado(false, null, 0, "No se pudo guardar el archivo de la ruta.");
        p.edit().remove(clave).apply();
        return new Resultado(true, destino, destino.length(), null);
    }

    // ------------------------------------------------------------------ eventos

    /** La ruta quedó en el celular. metodo = api | soap. */
    public static void eventoCargada(Context ctx, String codCycle, String carpeta, String ruta, String metodo, long bytes) {
        encolar(ctx, "ruta_cargada", codCycle, carpeta, ruta, metodo, bytes);
    }

    /** El técnico finalizó la ruta (Enviar Datos completo). */
    public static void eventoFinalizada(Context ctx, String carpeta, String ruta) {
        encolar(ctx, "ruta_finalizada", null, carpeta, ruta, "soap", -1);
    }

    private static void encolar(Context ctx, String tipo, String codCycle, String carpeta, String ruta, String metodo, long bytes) {
        try {
            Map<String, Object> d = new LinkedHashMap<>();
            if (codCycle != null) d.put("cod_cycle", codCycle);
            d.put("carpeta", carpeta == null ? "" : carpeta.trim());
            d.put("ruta", ruta == null ? "" : ruta.trim());
            d.put("metodo", metodo);
            if (bytes >= 0) d.put("bytes", bytes);
            String uid = EventoMovil.nuevoUid();
            ColaEnvioSqlite.de(ctx).encolar(ColaEnvio.EVENTO, uid,
                    EventoMovil.armar(uid, tipo, "", "", "", System.currentTimeMillis(), null, null, d), null);
            SincronizacionService.solicitar(ctx);
        } catch (Throwable ignored) {
            // El seguimiento nunca debe impedir trabajar la ruta.
        }
    }

    // ------------------------------------------------------------------ bloque E: lo que quedaba por SOAP

    /** Resultado de una operación con el servidor para las pantallas. */
    public static final class Operacion {
        public final boolean ok;
        /** La API no respondió o no tiene el servicio (servidor sin actualizar): seguir con el SOAP de siempre. */
        public final boolean probarSoap;
        public final String mensaje;
        /** Motivo del servidor en un rechazo (no_hay_actualizacion, ciclo_no_existe...), o null. */
        public final String motivo;
        public final Map<String, Object> cuerpo;

        Operacion(boolean ok, boolean probarSoap, String mensaje, String motivo, Map<String, Object> cuerpo) {
            this.ok = ok;
            this.probarSoap = probarSoap;
            this.mensaje = mensaje;
            this.motivo = motivo;
            this.cuerpo = cuerpo;
        }

        public String campo(String clave) {
            Object v = cuerpo == null ? null : cuerpo.get(clave);
            return v == null ? null : String.valueOf(v);
        }
    }

    private interface Llamada {
        ApiMovil.Respuesta hacer();
    }

    /** La llamada con sesión (la renueva una vez si el servidor responde 401). */
    private Operacion conSesion(Llamada llamada) {
        if (config.baseApi().isEmpty()) return new Operacion(false, true, "No hay URL de la API configurada.", null, null);
        String falla = asegurarSesion();
        if (falla != null) return new Operacion(false, true, falla, null, null);
        ApiMovil.Respuesta r = llamada.hacer();
        if (r.codigo == 401) {
            String f2 = renovarSesion();
            if (f2 != null) return new Operacion(false, true, f2, null, null);
            r = llamada.hacer();
        }
        if (r.ok() && r.cuerpo != null) return new Operacion(true, false, null, null, r.cuerpo);
        String detalle = r.campo("detalle");
        String motivo = r.campo("motivo");
        String msg = r.codigo == ApiMovil.SIN_RED ? "Sin conexión con el servidor."
                : "El servidor respondió " + r.codigo + (detalle != null ? ": " + detalle : (r.error != null ? ": " + r.error : ""));
        // 422 = el dato no sirve (el SOAP tampoco lo arreglaría); todo lo demás (red, 404 de un servidor sin
        // actualizar, 5xx) prueba el SOAP.
        return new Operacion(false, r.codigo != 422, msg, motivo, r.cuerpo);
    }

    /** Enviar Datos de fin de ruta por la API. carpeta = la del ciclo (0116), ruta = C + mun + sec + div. */
    public Operacion cerrarRuta(File zip, String carpeta, String ruta, int leidos, int causas) {
        return conSesion(() -> api.cerrarRuta(zip, carpeta, ruta, leidos, causas));
    }

    /** Lenguaje y archivos (fuentes, PCX) que se le mandan a la impresora (Config. Preimpreso). */
    public Operacion configImpresora() {
        return conSesion(api::configImpresora);
    }

    /** Estado de la ruta en el servidor (Borrar ruta y Validar envío). */
    public Operacion estadoRuta(String carpeta, String ruta) {
        return conSesion(() -> api.estadoRuta(carpeta, ruta));
    }

    /**
     * Baja un archivo (LBL para reimprimir, APK nuevo) a {@code destino}: por partes con Range si la conexión se
     * corta, verifica el SHA-256 si el servidor lo da y solo al final reemplaza el destino.
     */
    public Operacion bajar(String rutaApi, File destino, ApiMovil.Progreso progreso) {
        if (config.baseApi().isEmpty()) return new Operacion(false, true, "No hay URL de la API configurada.", null, null);
        String falla = asegurarSesion();
        if (falla != null) return new Operacion(false, true, falla, null, null);
        File dir = destino.getParentFile();
        if (dir != null && !dir.isDirectory()) dir.mkdirs();
        File parte = new File(destino.getPath() + ".part");
        if (parte.exists()) parte.delete();
        boolean renovada = false;
        ApiMovil.Tramo t = null;
        for (int intento = 0; intento < 6; intento++) {
            long desde = parte.isFile() ? parte.length() : 0;
            t = api.bajarArchivo(rutaApi, parte, desde, progreso);
            if (t.codigo == 401 && !renovada) {
                renovada = true;
                String f2 = renovarSesion();
                if (f2 != null) return new Operacion(false, true, f2, null, null);
                continue;
            }
            if (t.codigo == 200 || t.codigo == 206) {
                if (t.total > 0 && parte.length() < t.total) continue; // se cortó: sigue desde donde quedó
                try {
                    if (t.sha256 != null && !t.sha256.trim().isEmpty() && !t.sha256.trim().equalsIgnoreCase(ApiMovil.sha256(parte))) {
                        parte.delete();
                        continue;
                    }
                } catch (java.io.IOException e) {
                    parte.delete();
                    return new Operacion(false, false, "No se pudo verificar el archivo: " + e.getMessage(), null, null);
                }
                if (destino.exists()) destino.delete();
                if (!parte.renameTo(destino)) return new Operacion(false, false, "No se pudo guardar " + destino.getName(), null, null);
                return new Operacion(true, false, null, null, null);
            }
            if (t.codigo == ApiMovil.TRANSFERENCIA_FALLIDA) continue;
            break;
        }
        if (parte.exists()) parte.delete();
        if (t == null) return new Operacion(false, true, "Sin respuesta del servidor.", null, null);
        if (t.codigo == ApiMovil.SIN_RED) return new Operacion(false, true, "Sin conexión con el servidor.", null, null);
        return new Operacion(false, t.codigo != 422, "El servidor respondió " + t.codigo + (t.error != null ? ": " + t.error : ""),
                t.codigo == 404 || t.codigo == 422 ? t.error : null, null);
    }

    // ------------------------------------------------------------------ sesión

    /** null si hay token (o se consiguió); si no, el motivo para mostrar. */
    private String asegurarSesion() {
        String t = config.leer();
        if (t != null && !t.isEmpty() && !config.tokenDeOtroOperador()) {
            api.token(t);
            return null;
        }
        return renovarSesion();
    }

    private String renovarSesion() {
        faltaSesion = false;
        config.borrar();
        api.token(null);
        String clave = config.clave();
        if (clave == null || clave.isEmpty() || config.codOperador().isEmpty()) {
            faltaSesion = true;
            return "Digite su código y su clave para descargar la ruta.";
        }
        ApiMovil.Respuesta r = api.login(config.codOperador(), clave, config.serialTerminal(), config.versionApp());
        if (r.ok() && r.campo("token") != null) {
            config.guardar(r.campo("token"));
            api.token(r.campo("token"));
            return null;
        }
        if (r.codigo == ApiMovil.SIN_RED) return "Sin conexión con el servidor.";
        String d = r.campo("detalle");
        String e = r.campo("error");
        if ("credenciales_invalidas".equals(e)) {
            faltaSesion = true;
            return "El código " + config.codOperador() + " y la clave no coinciden con los de la web.";
        }
        if ("bloqueado_temporalmente".equals(e)) {
            return "Demasiados intentos con clave errada: espere unos minutos.";
        }
        return "El servidor no aceptó la sesión" + (d != null ? ": " + d : "") + " (" + r.codigo + (e != null ? " " + e : "") + ").";
    }

    private static int entero(String v, int defecto) {
        try {
            return v == null ? defecto : (int) Double.parseDouble(v.trim());
        } catch (NumberFormatException e) {
            return defecto;
        }
    }
}
