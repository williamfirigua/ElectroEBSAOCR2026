package com.gselectroCaqueta.moduloLicencia;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.media.MediaDrm;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;

import com.gselectroCaqueta.accesoyseguridad.BuildConfig;
import com.gselectroCaqueta.comunicaciones.BDComunicaciones;
import com.gselectroCaqueta.comunicaciones.CrudComunicaciones;
import com.gselectroCaqueta.sync.ApiMovil;
import com.gselectroCaqueta.sync.Json;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Licencia de la terminal (bloque B). Reemplaza el Config.txt con el serial.
 *
 * - En línea: el técnico digita su código o su usuario, la app manda sus identificadores (Widevine y
 *   ANDROID_ID, ya en SHA-256), marca, modelo, Android y la firma del APK a /api/v1/licencia/activar y el
 *   servidor devuelve la licencia firmada con ECDSA P-256: "EBSA1.base64url(json).base64url(firma DER)".
 *   Aquí se verifica con la clave pública embebida; nadie puede fabricar una sin la clave privada del servidor.
 * - Sin señal: la app muestra un código de solicitud de 8 dígitos, la oficina lo digita en la web y dicta la
 *   respuesta (HMAC con el secreto compartido). Sale una licencia PROVISIONAL de máximo 3 días, guardada con
 *   HMAC; con la primera señal la app se activa en línea.
 * - Se guarda en almacenamiento privado y en Documents/EBSA/licencia.lic: si se desinstala la app la copia de
 *   Documents la recupera; si el equipo se restablece, el técnico vuelve a digitar su código y el servidor lo
 *   reconoce por sus identificadores.
 */
public final class LicenciaTerminal {

    private static final String TAG = "Licencia";
    private static final Charset UTF8 = Charset.forName("UTF-8");

    /** Clave pública del servidor (storage/app/licencias/licencia_publica.pem). */
    private static final String CLAVE_PUBLICA =
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE5Ny0nhJ/rNUN9Qobc5tOJeJBuspl"
                    + "mKDj2SdfaHUjmicKyNjRgSC2vLCBN4lYnM0o9vH4QMb9cp5Ou5XbzAHcMg==";

    private static final UUID WIDEVINE = new UUID(0xEDEF8BA979D64ACEL, 0xA3C827DCD51D21EDL);
    private static final String PREFIJO_FIRMADA = "EBSA1";
    private static final String PREFIJO_PROVISIONAL = "EBSAP";
    private static final long DIA_MS = 24L * 3600_000L;
    private static final int DIAS_PROVISIONAL = 3;
    /** Tolerancia para el reloj: si la hora retrocede más que esto respecto a la última vista, es manipulación. */
    private static final long TOLERANCIA_RELOJ_MS = 2L * 3600_000L;

    private static final String PREFS = "EbsaLicencia";
    private static final String K_LICENCIA = "licencia";
    private static final String K_HORA_MAX = "hora_max";
    private static final String K_USUARIOS = "usuarios";
    private static final String K_RETIRO = "retiro";
    private static final String K_REVISADA_EN = "revisada_en";
    private static final String ARCHIVO = "licencia.lic";

    /** Resultado de revisar la licencia guardada. */
    public enum Estado { VIGENTE, SIN_LICENCIA, INVALIDA, OTRO_EQUIPO, VENCIDA, PROVISIONAL_VENCIDA, RELOJ_ATRASADO }

    /** Datos de una licencia ya verificada. */
    public static final class Licencia {
        public String codMovil = "";
        public String operador = "";
        public String usuario = "";
        public String nombre = "";
        public String tipo = "";
        public String vence = "";      // yyyy-MM-dd, vacío = sin vencimiento
        public String wid = "";
        public String aid = "";
        public boolean provisional;
        public long provisionalHasta;   // ms, solo provisional
    }

    public static final class Revision {
        public final Estado estado;
        public final Licencia licencia;

        Revision(Estado estado, Licencia licencia) {
            this.estado = estado;
            this.licencia = licencia;
        }

        public boolean vigente() {
            return estado == Estado.VIGENTE;
        }

        public String mensaje() {
            switch (estado) {
                case SIN_LICENCIA:
                    return "El equipo no tiene licencia.";
                case INVALIDA:
                    return "La licencia guardada no es válida.";
                case OTRO_EQUIPO:
                    return "La licencia guardada es de otro equipo.";
                case VENCIDA:
                    return "La licencia venció el " + (licencia != null ? licencia.vence : "") + ".";
                case PROVISIONAL_VENCIDA:
                    return "La licencia provisional venció: active el equipo con señal.";
                case RELOJ_ATRASADO:
                    return "La fecha del equipo está atrasada. Corrija la fecha y la hora (automáticas).";
                default:
                    return "";
            }
        }
    }

    private final Context ctx;
    private final SharedPreferences prefs;

    public LicenciaTerminal(Context ctx) {
        this.ctx = ctx.getApplicationContext();
        this.prefs = this.ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    // ------------------------------------------------------------------ identidad del equipo

    /** SHA-256 del ID de Widevine (sobrevive a desinstalar; cambia al restablecer el equipo). "" si no hay. */
    public String idWidevine() {
        if (cacheWid == null) cacheWid = leerWidevine();
        return cacheWid;
    }

    private static volatile String cacheWid;
    private static volatile String cacheAid;

    private static String leerWidevine() {
        MediaDrm drm = null;
        try {
            drm = new MediaDrm(WIDEVINE);
            byte[] id = drm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID);
            return id == null || id.length == 0 ? "" : sha256Hex(id);
        } catch (Throwable t) {
            Log.w(TAG, "sin Widevine: " + t);
            return "";
        } finally {
            if (drm != null) {
                try {
                    if (Build.VERSION.SDK_INT >= 28) drm.close();
                    else drm.release();
                } catch (Throwable ignored) {
                }
            }
        }
    }

    /** SHA-256 del ANDROID_ID (por firma del APK; sobrevive a desinstalar). "" si no hay. */
    @SuppressLint("HardwareIds")
    public String idAndroid() {
        if (cacheAid == null) cacheAid = leerAndroidId(ctx);
        return cacheAid;
    }

    @SuppressLint("HardwareIds")
    private static String leerAndroidId(Context ctx) {
        try {
            String id = Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
            return id == null || id.trim().isEmpty() ? "" : sha256Hex(id.trim().getBytes(UTF8));
        } catch (Throwable t) {
            return "";
        }
    }

    /** SHA-256 del certificado con el que se firmó el APK (el servidor puede exigir una lista). */
    public String firmaApk() {
        try {
            PackageManager pm = ctx.getPackageManager();
            Signature[] firmas;
            if (Build.VERSION.SDK_INT >= 28) {
                PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_SIGNING_CERTIFICATES);
                firmas = pi.signingInfo.hasMultipleSigners() ? pi.signingInfo.getApkContentsSigners() : pi.signingInfo.getSigningCertificateHistory();
            } else {
                firmas = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_SIGNATURES).signatures;
            }
            return firmas == null || firmas.length == 0 ? "" : sha256Hex(firmas[firmas.length - 1].toByteArray());
        } catch (Throwable t) {
            return "";
        }
    }

    public String versionApp() {
        try {
            return ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
        } catch (Throwable t) {
            return "";
        }
    }

    /** Lo que se manda al servidor en activar y estado (sin el operador). */
    public Map<String, Object> datosEquipo() {
        Map<String, Object> d = new HashMap<>();
        d.put("id_widevine", idWidevine());
        d.put("id_android", idAndroid());
        d.put("marca", Build.MANUFACTURER == null ? "" : Build.MANUFACTURER.trim());
        d.put("modelo", Build.MODEL == null ? "" : Build.MODEL.trim());
        d.put("android", Build.VERSION.RELEASE);
        d.put("sdk", Build.VERSION.SDK_INT);
        d.put("firma", firmaApk());
        d.put("version_app", versionApp());
        return d;
    }

    // ------------------------------------------------------------------ revisión

    /**
     * Pantallas de entrada (menú principal): sin licencia vigente (vencida, retirada desde la web, provisional
     * vencida) se vuelve a la pantalla de licencia. No se usa dentro de la liquidación para no cortar un registro.
     *
     * @return true si la licencia está vigente y la pantalla puede seguir
     */
    public static boolean exigir(Activity a) {
        Revision rv = new LicenciaTerminal(a).revisar();
        if (rv.vigente()) return true;
        Toast.makeText(a, rv.mensaje(), Toast.LENGTH_LONG).show();
        Intent i = new Intent(a, ActLicencia.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        a.startActivity(i);
        a.finish();
        return false;
    }

    /** Revisa la licencia guardada: firma, equipo, vencimiento general, provisional y reloj. */
    public Revision revisar() {
        String texto = leerGuardada();
        if (texto == null || texto.isEmpty()) return new Revision(Estado.SIN_LICENCIA, null);
        Licencia l = decodificar(texto);
        if (l == null) return new Revision(Estado.INVALIDA, null);

        String wid = idWidevine();
        String aid = idAndroid();
        boolean coincide = (!wid.isEmpty() && wid.equals(l.wid)) || (!aid.isEmpty() && aid.equals(l.aid));
        if (!coincide) return new Revision(Estado.OTRO_EQUIPO, l);

        long ahora = System.currentTimeMillis();
        long max = prefs.getLong(K_HORA_MAX, 0);
        if (max > 0 && ahora + TOLERANCIA_RELOJ_MS < max) return new Revision(Estado.RELOJ_ATRASADO, l);
        if (ahora > max) prefs.edit().putLong(K_HORA_MAX, ahora).apply();

        if (l.provisional && ahora > l.provisionalHasta) return new Revision(Estado.PROVISIONAL_VENCIDA, l);
        if (vencida(l.vence)) return new Revision(Estado.VENCIDA, l);
        return new Revision(Estado.VIGENTE, l);
    }

    /** Hoy es posterior a la fecha general (bloqueo desde el día siguiente al vencimiento). */
    static boolean vencida(String vence) {
        if (vence == null || vence.trim().isEmpty()) return false;
        String hoy = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        return hoy.compareTo(vence.trim()) > 0;
    }

    /** Licencia que manda el servidor: se verifica antes de guardarla. */
    public Licencia guardarFirmada(String token) {
        Licencia l = verificarFirmada(token);
        if (l != null) guardar(token);
        return l;
    }

    // ------------------------------------------------------------------ servidor

    /** Resultado de hablar con el servidor. */
    public static final class Respuesta {
        public final boolean ok;
        /** No hubo conexión: no cambia nada de lo guardado. */
        public final boolean sinRed;
        /** El servidor retiró la licencia (revocada, bloqueada, otro equipo...): ya se borró. */
        public final boolean retirada;
        public final String error;
        public final String mensaje;
        public final Licencia licencia;

        Respuesta(boolean ok, boolean sinRed, boolean retirada, String error, String mensaje, Licencia licencia) {
            this.ok = ok;
            this.sinRed = sinRed;
            this.retirada = retirada;
            this.error = error;
            this.mensaje = mensaje;
            this.licencia = licencia;
        }
    }

    /** http://host:puertoApi/api/ con la configuración de Comunicaciones (la misma que usa la liquidación). */
    public static String baseApi(Context ctx) {
        try {
            BDComunicaciones bdc = new CrudComunicaciones(ctx).getParams();
            if (bdc == null || bdc.getURL() == null) return "";
            String url = "http://" + bdc.getURL().trim();
            return url.substring(0, url.lastIndexOf(":") + 1) + bdc.getPuertoApi() + "/api/";
        } catch (Throwable t) {
            Log.w(TAG, "sin URL de API: " + t);
            return "";
        }
    }

    /** Activación en línea con el código o el usuario del técnico. */
    public Respuesta activarEnLinea(String baseApi, String operador) {
        if (baseApi == null || baseApi.isEmpty()) return new Respuesta(false, true, false, "sin_url", "No hay URL del servidor configurada.", null);
        Map<String, Object> d = datosEquipo();
        d.put("operador", operador.trim());
        String legado = serialLegado();
        Licencia actual = decodificar(leerGuardada());
        if (actual != null && !actual.codMovil.isEmpty()) legado = actual.codMovil;
        if (legado != null) d.put("cod_movil_anterior", legado);
        return aplicar(new ApiMovil(baseApi).activarLicencia(d), false);
    }

    /**
     * Revisión con el servidor de la licencia guardada. Si es provisional, intenta la activación en línea con el
     * operador que la pidió. Con la respuesta renueva la licencia (fecha general nueva) o la borra si se retiró.
     */
    public Respuesta revisarEnServidor(String baseApi) {
        Licencia l = decodificar(leerGuardada());
        if (l == null) return new Respuesta(false, false, false, "sin_licencia", "El equipo no tiene licencia.", null);
        if (l.provisional) {
            Respuesta r = activarEnLinea(baseApi, l.operador);
            // Una provisional que el servidor rechaza (modelo, tope, vencidas) se mantiene hasta que venza:
            // así se acordó (máximo 3 días). Solo se borra si el equipo está bloqueado.
            return r;
        }
        if (baseApi == null || baseApi.isEmpty()) return new Respuesta(false, true, false, "sin_url", "No hay URL del servidor configurada.", l);
        Map<String, Object> d = datosEquipo();
        d.remove("marca");
        d.remove("modelo");
        d.put("cod_movil", l.codMovil);
        return aplicar(new ApiMovil(baseApi).estadoLicencia(d), true);
    }

    /** Hace falta revisar con el servidor (cada 6 horas; lo llama la sincronización). */
    public boolean tocaRevisar(long ahoraMs, long cadaMs) {
        long en = prefs.getLong(K_REVISADA_EN, 0);
        return en > ahoraMs || ahoraMs - en >= cadaMs;
    }

    /** Motivo del último retiro (para mostrarlo en la pantalla de licencia); se consume al leerlo. */
    public String tomarMotivoRetiro() {
        String m = prefs.getString(K_RETIRO, null);
        if (m != null) prefs.edit().remove(K_RETIRO).apply();
        return m;
    }

    private Respuesta aplicar(ApiMovil.Respuesta r, boolean esRevision) {
        if (r.codigo == ApiMovil.SIN_RED || r.codigo == ApiMovil.TRANSFERENCIA_FALLIDA) {
            return new Respuesta(false, true, false, "sin_red", "Sin conexión con el servidor.", null);
        }
        String error = r.campo("error");
        String detalle = r.campo("detalle");
        if (r.ok() && r.campo("licencia") != null) {
            Licencia l = guardarFirmada(r.campo("licencia"));
            if (l == null) return new Respuesta(false, false, false, "licencia_invalida", "El servidor envió una licencia que no se pudo verificar.", null);
            Object usuarios = r.cuerpo != null ? r.cuerpo.get("usuarios") : null;
            if (usuarios instanceof List) {
                try {
                    guardarUsuarios(new JSONArray(Json.escribir(usuarios)));
                } catch (Exception ignored) {
                }
            }
            prefs.edit().putLong(K_REVISADA_EN, System.currentTimeMillis()).apply();
            borrarLegado();
            return new Respuesta(true, false, false, null, null, l);
        }
        if (r.codigo >= 500 || r.codigo == 429 || r.codigo == 0) {
            return new Respuesta(false, true, false, "servidor", "El servidor no respondió (" + r.codigo + ").", null);
        }
        boolean retirar = "equipo_bloqueado".equals(error)
                || (esRevision && ("licencia_revocada".equals(error) || "equipo_no_coincide".equals(error)
                || "licencia_no_registrada".equals(error) || "firma_no_aceptada".equals(error) || "operador_inactivo".equals(error)));
        String msg = detalle != null ? detalle : (error != null ? error : "Respuesta " + r.codigo);
        if (retirar) {
            borrar();
            prefs.edit().putString(K_RETIRO, msg).putLong(K_REVISADA_EN, System.currentTimeMillis()).apply();
        }
        return new Respuesta(false, false, retirar, error, msg, null);
    }

    // ------------------------------------------------------------------ sin señal

    /** 8 dígitos que salen de los identificadores del equipo (los dicta el técnico a la oficina). */
    public String codigoSolicitud() {
        byte[] h = sha256(("EBSA-SOL|" + idWidevine() + "|" + idAndroid()).getBytes(UTF8));
        return truncar(h);
    }

    /** Respuesta que calcula la web (CodigoOffline::respuesta) para esta solicitud, operador y fecha. */
    static String respuestaEsperada(String solicitud, String codOperador, Date fecha) {
        String secreto = BuildConfig.LICENCIA_SECRETO_OFFLINE;
        if (secreto == null || secreto.isEmpty()) return null;
        String dia = new SimpleDateFormat("yyyyMMdd", Locale.US).format(fecha);
        return truncar(hmac(secreto.getBytes(UTF8), ("EBSA-LIC|" + solicitud + "|" + codOperador + "|" + dia).getBytes(UTF8)));
    }

    /**
     * Valida la respuesta dictada (vale la de hoy y la de ayer) y guarda una licencia provisional.
     *
     * @param codOperador código numérico (o usuario si la terminal ya conoce la lista de usuarios)
     * @return la licencia provisional, o null si la respuesta no corresponde
     */
    public Licencia activarSinSenal(String codOperador, String respuesta) {
        String cod = codigoDeUsuario(codOperador);
        if (cod == null) return null;
        String sol = codigoSolicitud();
        Calendar c = Calendar.getInstance();
        boolean ok = false;
        for (int i = 0; i < 2 && !ok; i++) {
            String esperada = respuestaEsperada(sol, cod, c.getTime());
            ok = esperada != null && esperada.equals(respuesta.trim());
            c.add(Calendar.DAY_OF_MONTH, -1);
        }
        if (!ok) return null;

        Licencia anterior = decodificar(leerGuardada());
        try {
            JSONObject o = new JSONObject();
            o.put("v", 1);
            // Sin señal no se conoce el cod_movil definitivo: se conserva el anterior o el del Config.txt.
            String codMovil = anterior != null && !anterior.codMovil.isEmpty() ? anterior.codMovil : serialLegado();
            if (codMovil == null) codMovil = codMovilCandidato();
            o.put("cod_movil", codMovil);
            o.put("operador", cod);
            o.put("usuario", "");
            o.put("nombre", "");
            o.put("tipo", "");
            o.put("wid", idWidevine());
            o.put("aid", idAndroid());
            o.put("vence", anterior != null ? anterior.vence : "");
            o.put("hasta", System.currentTimeMillis() + DIAS_PROVISIONAL * DIA_MS);
            byte[] json = o.toString().getBytes(UTF8);
            String token = PREFIJO_PROVISIONAL + "." + b64(json) + "." + b64(hmac(claveProvisional(), json));
            guardar(token);
            return decodificar(token);
        } catch (Exception e) {
            Log.e(TAG, "provisional: " + e);
            return null;
        }
    }

    /**
     * El mismo cod_movil que generaría el servidor (ServicioLicencias::nuevoCodMovil, sin colisiones): 15
     * dígitos que empiezan por 9. Solo para la provisional; la activación en línea trae el definitivo.
     */
    private String codMovilCandidato() {
        String semilla = !idWidevine().isEmpty() ? idWidevine() : idAndroid();
        byte[] h = sha256(("EBSA-MOVIL|" + semilla).getBytes(UTF8));
        long n = 0;
        for (int i = 0; i < 7; i++) n = (n << 8) | (h[i] & 0xff); // 56 bits
        n = (n >>> 4) % 100000000000000L;                          // primeros 13 hex = 52 bits
        return "9" + String.format(Locale.US, "%014d", n);
    }

    // ------------------------------------------------------------------ usuarios (login por usuario)

    /** Lista código/usuario que manda el servidor con la licencia. */
    public void guardarUsuarios(JSONArray usuarios) {
        if (usuarios != null) prefs.edit().putString(K_USUARIOS, usuarios.toString()).apply();
    }

    /**
     * Código del operador a partir de lo que digitó: si es numérico se devuelve igual; si es un usuario se busca
     * en la lista descargada (sin distinguir mayúsculas). null si es un usuario que la terminal no conoce.
     */
    public String codigoDeUsuario(String digitado) {
        String v = digitado == null ? "" : digitado.trim();
        if (v.isEmpty()) return null;
        if (v.matches("\\d+")) return v;
        try {
            JSONArray a = new JSONArray(prefs.getString(K_USUARIOS, "[]"));
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                if (v.equalsIgnoreCase(o.optString("usuario").trim())) return o.optString("codigo").trim();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    // ------------------------------------------------------------------ esquema anterior

    private File configLegado() {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Config.txt");
    }

    /** Serial del Config.txt del esquema anterior (ofuscado), o null si no existe. */
    public String serialLegado() {
        File f = configLegado();
        if (!f.isFile()) return null;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)))) {
            StringBuilder sb = new StringBuilder();
            String linea;
            while ((linea = br.readLine()) != null) sb.append(linea);
            String s = desofuscar(sb.toString()).trim();
            return s.matches("\\d{1,20}") ? s : null;
        } catch (Exception e) {
            return null;
        }
    }

    /** Ya hay licencia nueva: el Config.txt deja de usarse. */
    public void borrarLegado() {
        File f = configLegado();
        if (f.isFile() && !f.delete()) Log.w(TAG, "no se pudo borrar Config.txt");
    }

    private static String desofuscar(String entrada) {
        String alfabeto = "*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ \n\r";
        String sub = "QAZWSXDCRFVTGBYHNUJM IKOLP*>+=,<-;.:/908?172634[|@\n\r";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < entrada.length(); i++) {
            int j = sub.indexOf(entrada.charAt(i));
            if (j >= 0 && j < alfabeto.length()) sb.append(alfabeto.charAt(j));
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------ almacenamiento

    /** La licencia se retiró (revocada, bloqueada, otro equipo): se borra de todos lados. */
    public void borrar() {
        prefs.edit().remove(K_LICENCIA).apply();
        File f = archivoPublico();
        if (f.isFile() && !f.delete()) Log.w(TAG, "no se pudo borrar " + f);
    }

    private void guardar(String token) {
        prefs.edit().putString(K_LICENCIA, token).commit();
        try {
            File f = archivoPublico();
            File dir = f.getParentFile();
            if (dir != null && !dir.isDirectory()) dir.mkdirs();
            try (FileOutputStream o = new FileOutputStream(f)) {
                o.write(token.getBytes(UTF8));
            }
        } catch (Exception e) {
            Log.w(TAG, "no se pudo copiar la licencia a Documents: " + e);
        }
    }

    /** Privada primero; si no está (app reinstalada), la copia de Documents. */
    private String leerGuardada() {
        String t = prefs.getString(K_LICENCIA, null);
        if (t != null && !t.isEmpty()) return t;
        File f = archivoPublico();
        if (!f.isFile()) return null;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), UTF8))) {
            String linea = br.readLine();
            if (linea != null && decodificar(linea.trim()) != null) {
                prefs.edit().putString(K_LICENCIA, linea.trim()).apply();
                return linea.trim();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private File archivoPublico() {
        return new File(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "EBSA"), ARCHIVO);
    }

    // ------------------------------------------------------------------ formato

    private Licencia decodificar(String token) {
        if (token == null) return null;
        if (token.startsWith(PREFIJO_FIRMADA + ".")) return verificarFirmada(token);
        if (token.startsWith(PREFIJO_PROVISIONAL + ".")) return verificarProvisional(token);
        return null;
    }

    /** Verifica la firma ECDSA del servidor sobre los bytes exactos del JSON. */
    static Licencia verificarFirmada(String token) {
        try {
            String[] p = token.trim().split("\\.");
            if (p.length != 3 || !PREFIJO_FIRMADA.equals(p[0])) return null;
            byte[] json = deB64(p[1]);
            byte[] firma = deB64(p[2]);
            PublicKey clave = KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(Base64.decode(CLAVE_PUBLICA, Base64.DEFAULT)));
            java.security.Signature v = java.security.Signature.getInstance("SHA256withECDSA");
            v.initVerify(clave);
            v.update(json);
            if (!v.verify(firma)) return null;
            JSONObject o = new JSONObject(new String(json, UTF8));
            Licencia l = base(o);
            l.provisional = false;
            return l.codMovil.isEmpty() ? null : l;
        } catch (Exception e) {
            Log.w(TAG, "licencia no verificable: " + e);
            return null;
        }
    }

    private Licencia verificarProvisional(String token) {
        try {
            String[] p = token.trim().split("\\.");
            if (p.length != 3) return null;
            byte[] json = deB64(p[1]);
            if (!MessageDigest.isEqual(hmac(claveProvisional(), json), deB64(p[2]))) return null;
            JSONObject o = new JSONObject(new String(json, UTF8));
            Licencia l = base(o);
            l.provisional = true;
            l.provisionalHasta = Math.min(o.optLong("hasta", 0), System.currentTimeMillis() + DIAS_PROVISIONAL * DIA_MS + TOLERANCIA_RELOJ_MS);
            return l;
        } catch (Exception e) {
            return null;
        }
    }

    private static Licencia base(JSONObject o) {
        Licencia l = new Licencia();
        l.codMovil = o.optString("cod_movil", "").trim();
        l.operador = o.optString("operador", "").trim();
        l.usuario = o.optString("usuario", "").trim();
        l.nombre = o.optString("nombre", "").trim();
        l.tipo = o.optString("tipo", "").trim();
        l.vence = o.optString("vence", "").trim();
        l.wid = o.optString("wid", "").trim();
        l.aid = o.optString("aid", "").trim();
        return l;
    }

    /** Clave de la provisional: atada al secreto y al equipo (no se puede copiar a otro). */
    private byte[] claveProvisional() {
        if (BuildConfig.LICENCIA_SECRETO_OFFLINE == null || BuildConfig.LICENCIA_SECRETO_OFFLINE.isEmpty()) {
            throw new IllegalStateException("APK sin secreto de licencias");
        }
        return hmac(BuildConfig.LICENCIA_SECRETO_OFFLINE.getBytes(UTF8), ("EBSA-PROV|" + idWidevine() + "|" + idAndroid()).getBytes(UTF8));
    }

    // ------------------------------------------------------------------ utilidades

    /** Truncado dinámico RFC 4226, módulo 10^8 (igual que CodigoOffline.php). */
    private static String truncar(byte[] h) {
        int o = h[h.length - 1] & 0x0f;
        int n = ((h[o] & 0x7f) << 24) | ((h[o + 1] & 0xff) << 16) | ((h[o + 2] & 0xff) << 8) | (h[o + 3] & 0xff);
        return String.format(Locale.US, "%08d", n % 100000000);
    }

    private static byte[] hmac(byte[] clave, byte[] datos) {
        try {
            Mac m = Mac.getInstance("HmacSHA256");
            m.init(new SecretKeySpec(clave, "HmacSHA256"));
            return m.doFinal(datos);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static byte[] sha256(byte[] b) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(b);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String sha256Hex(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte x : sha256(b)) sb.append(String.format(Locale.US, "%02x", x & 0xff));
        return sb.toString();
    }

    private static String b64(byte[] b) {
        return Base64.encodeToString(b, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
    }

    private static byte[] deB64(String s) {
        return Base64.decode(s, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
    }
}
