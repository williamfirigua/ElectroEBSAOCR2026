package com.gselectroCaqueta.accesoyseguridad;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.gsutil.Utils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * TrackingLocationService — Rastreo GPS en segundo plano para FACTURACIÓN (Caquetá).
 *
 * Versión adaptada del servicio original de lecturas:
 *  - Paquete: com.gselectroCaqueta.accesoyseguridad
 *  - Utils:   com.gsutil.Utils
 *  - SIN dependencia de LecturaSyncService ni del endpoint zona-trabajo.
 *  - Ventana horaria y URL base QUEMADAS como constantes (ver bloque CONFIG).
 *  - Log resiliente: si VariablesGlobales.directorioactual no está disponible
 *    (p.ej. reinicio START_STICKY sin Activity), cae a getExternalFilesDir() y,
 *    en último caso, omite el log a archivo sin tumbar el servicio.
 *
 * Se arranca/detiene directamente desde MenuDeLiquidacion (Activity):
 *   TrackingLocationService.iniciar(this, codOperador);   // en onCreate
 *   TrackingLocationService.detener(this);                // en onDestroy
 *
 * Envío: POST {apiUrl}tracking/ubicaciones   (apiUrl = cadenaURLapi, termina en /api/)
 *   Body: { "cod_operador": <int>, "ubicaciones": [ {lat,lng,precision,timestamp}, ... ] }
 */
public class TrackingLocationService extends Service {

    private static final String TAG        = "TrackingFacturacion";
    private static final String CHANNEL_ID = "tracking_facturacion_channel";
    private static final int    NOTIF_ID   = 3001;

    // ════════════════════════════════════════════════════════════════════════
    //  CONFIG — ventana horaria QUEMADA (sin endpoint zona-trabajo).
    //  La URL base NO se quema: llega por parámetro desde getParamsWs()
    //  (cadenaURLapi), persistida en prefs para sobrevivir reinicios START_STICKY.
    // ════════════════════════════════════════════════════════════════════════

    /** Ventana horaria de rastreo (hora local 24h). Fuera de ella el GPS se pausa. */
    private static final int WINDOW_START_HOUR = 5;   // inclusive
    private static final int WINDOW_END_HOUR   = 20;  // exclusivo

    // ════════════════════════════════════════════════════════════════════════

    private static final String PREFS_NAME       = "TrackingFacturacionPrefs";
    private static final String KEY_COD_OPERADOR = "tracking_cod_operador";
    private static final String KEY_API_URL      = "tracking_api_url";

    // Intervalo de muestreo y envío
    private static final long UPDATE_INTERVAL_MS     = 60_000L;   // pedir GPS cada 60 s
    private static final long FASTEST_INTERVAL_MS    = 30_000L;
    private static final int  MAX_BATCH_SIZE         = 10;        // envío inmediato al llenar
    private static final long BATCH_SEND_INTERVAL_MS = 300_000L;  // envío periódico cada 5 min

    // Tope de la cola en memoria: ante offline prolongado conserva las ubicaciones
    // más recientes y descarta las antiguas (evita crecimiento ilimitado / OOM).
    private static final int MAX_QUEUE_SIZE = 500;

    // Cliente HTTP único y reutilizado (pool de conexiones/hilos compartido).
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    // Estado
    private final List<JSONObject> ubicacionesPendientes = new ArrayList<>();
    private volatile boolean isTracking = false;
    private int codOperador = 0;
    private String apiUrl = "";

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Handler handler;

    private final Utils utils = new Utils();
    private File logfile; // puede quedar null si no hay ruta disponible (logging a archivo se omite)

    // ==================== CICLO DE VIDA ====================

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIF_ID, buildNotification("Rastreo GPS iniciado"));

        handler = new Handler(Looper.getMainLooper());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        logfile = resolveLogFile();
        logEvento("[onCreate] Ventana horaria quemada: "
                + WINDOW_START_HOUR + ":00 - " + WINDOW_END_HOUR + ":00");

        // Programar el envío periódico del batch
        handler.postDelayed(enviarBatchTask, BATCH_SEND_INTERVAL_MS);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // CONTRATO FGS: si llegamos por startForegroundService() hay que llamar a
        // startForeground() YA, antes de cualquier rama o return, o el sistema mata
        // el proceso (~5 s) con ForegroundServiceDidNotStartInTimeException.
        startForeground(NOTIF_ID, buildNotification("Rastreo GPS activo"));

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        if (intent != null && intent.hasExtra("cod_operador")) {
            codOperador = intent.getIntExtra("cod_operador", 0);
            apiUrl = intent.getStringExtra("api_url");
            prefs.edit()
                    .putInt(KEY_COD_OPERADOR, codOperador)
                    .putString(KEY_API_URL, apiUrl != null ? apiUrl : "")
                    .apply();
        } else {
            // Reinicio START_STICKY sin extras → recuperar de prefs
            codOperador = prefs.getInt(KEY_COD_OPERADOR, 0);
            apiUrl      = prefs.getString(KEY_API_URL, "");
        }

        if (codOperador > 0 && apiUrl != null && !apiUrl.isEmpty()) {
            if (isWithinWindowNow()) {
                iniciarRastreo();
            } else {
                long delay = millisUntilWindowOpens();
                handler.postDelayed(() -> { if (isWithinWindowNow()) iniciarRastreo(); }, delay);
            }
        } else {
            logEvento("[onStartCommand] datos inválidos | codOp=" + codOperador
                    + " url=" + apiUrl + " — deteniendo");
            stopSelf();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isTracking = false;
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        if (handler != null) handler.removeCallbacksAndMessages(null);
        logEvento("[onDestroy] Rastreo GPS detenido");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // ==================== RASTREO GPS ====================

    private void iniciarRastreo() {
        if (isTracking) {
            logEvento("[iniciarRastreo] Ya activo — ignorando");
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            logEvento("[iniciarRastreo] Sin permiso ACCESS_FINE_LOCATION — deteniendo");
            stopSelf();
            return;
        }

        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_INTERVAL_MS)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                if (result == null) return;
                for (Location location : result.getLocations()) {
                    procesarUbicacion(location);
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback,
                Looper.getMainLooper());

        isTracking = true;
        actualizarNotificacion("GPS activo");
        logEvento("[iniciarRastreo] GPS activado | cod_operador=" + codOperador);
    }

    private void pausarTracking() {
        if (!isTracking) return;
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        isTracking = false;
        actualizarNotificacion("GPS pausado (fuera de horario)");
        logEvento("[pausarTracking] Rastreo pausado");
    }

    private void procesarUbicacion(Location location) {
        if (!isWithinWindowNow()) {
            pausarTracking();
            return;
        }
        try {
            JSONObject punto = new JSONObject();
            punto.put("lat",       location.getLatitude());
            punto.put("lng",       location.getLongitude());
            punto.put("precision", location.getAccuracy());
            punto.put("timestamp",
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(new Date()));

            int pendientes;
            synchronized (ubicacionesPendientes) {
                ubicacionesPendientes.add(punto);
                pendientes = ubicacionesPendientes.size();
            }

            if (pendientes >= MAX_BATCH_SIZE) {
                enviarBatch();
            }
        } catch (Exception e) {
            logEvento("[procesarUbicacion] Error: " + e.getMessage());
        }
    }

    // ==================== ENVÍO DE UBICACIONES ====================

    private final Runnable enviarBatchTask = new Runnable() {
        @Override
        public void run() {
            if (isWithinWindowNow()) enviarBatch();
            handler.postDelayed(this, BATCH_SEND_INTERVAL_MS);
        }
    };

    private void enviarBatch() {
        final List<JSONObject> batch;
        synchronized (ubicacionesPendientes) {
            if (ubicacionesPendientes.isEmpty()) return;
            batch = new ArrayList<>(ubicacionesPendientes);
            ubicacionesPendientes.clear();
        }

        new Thread(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("cod_operador", codOperador);

                JSONArray array = new JSONArray();
                for (JSONObject u : batch) array.put(u);
                payload.put("ubicaciones", array);

                RequestBody body = RequestBody.create(
                        MediaType.parse("application/json"), payload.toString());

                String url = apiUrl + "tracking/ubicaciones";
                Request request = new Request.Builder().url(url).post(body).build();

                logEvento("[enviarBatch] Enviando " + batch.size() + " ubicaciones → " + url);

                // try-with-resources: cierra el Response pase lo que pase (evita fuga de conexión).
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        actualizarNotificacion("GPS activo (sincronizado)");
                        logEvento("[enviarBatch] OK " + batch.size() + " ubicaciones enviadas");
                    } else {
                        logEvento("[enviarBatch] Error HTTP " + response.code());
                        reencolar(batch);
                    }
                }
            } catch (Exception e) {
                logEvento("[enviarBatch] Excepción: " + e.getMessage());
                reencolar(batch);
            }
        }).start();
    }

    /**
     * Devuelve un batch fallido al frente de la cola para reintento, acotando el
     * tamaño total: conserva las ubicaciones más recientes y descarta las antiguas.
     */
    private void reencolar(List<JSONObject> batch) {
        synchronized (ubicacionesPendientes) {
            ubicacionesPendientes.addAll(0, batch);
            int exceso = ubicacionesPendientes.size() - MAX_QUEUE_SIZE;
            if (exceso > 0) {
                ubicacionesPendientes.subList(0, exceso).clear();
                logEvento("[reencolar] Cola llena, descartadas " + exceso + " ubicaciones antiguas");
            }
        }
    }

    // ==================== VENTANA HORARIA (QUEMADA) ====================

    private boolean isWithinWindowNow() {
        int hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return hora >= WINDOW_START_HOUR && hora < WINDOW_END_HOUR;
    }

    private long millisUntilWindowOpens() {
        Calendar now  = Calendar.getInstance();
        Calendar open = (Calendar) now.clone();
        open.set(Calendar.HOUR_OF_DAY, WINDOW_START_HOUR);
        open.set(Calendar.MINUTE, 0);
        open.set(Calendar.SECOND, 0);
        open.set(Calendar.MILLISECOND, 0);
        if (open.before(now)) open.add(Calendar.DAY_OF_MONTH, 1);
        long diff = open.getTimeInMillis() - now.getTimeInMillis();
        return diff > 0 ? diff : 60_000L; // mínimo 1 minuto
    }

    // ==================== NOTIFICACIONES ====================

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "Rastreo GPS Facturación",
                    NotificationManager.IMPORTANCE_LOW);
            ch.setDescription("Rastreo GPS del operario en segundo plano");
            NotificationManager mgr = getSystemService(NotificationManager.class);
            if (mgr != null) mgr.createNotificationChannel(ch);
        }
    }

    private Notification buildNotification(String texto) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Rastreo GPS")
                .setContentText(texto)
                .setSmallIcon(android.R.drawable.stat_sys_upload)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void actualizarNotificacion(String texto) {
        try {
            NotificationManager mgr =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (mgr != null) mgr.notify(NOTIF_ID, buildNotification(texto));
        } catch (Exception e) {
            logEvento("[actualizarNotificacion] " + e.getMessage());
        }
    }

    // ==================== LOG RESILIENTE ====================

    /**
     * Resuelve un archivo de log válido sin depender de un global que pueda no
     * estar inicializado (caso START_STICKY sin Activity). Orden de preferencia:
     *  1) VariablesGlobales.directorioactual/DATOSDESALIDA/LOGEVENTOS.LOG
     *  2) getExternalFilesDir(null)/LOGEVENTOS.LOG
     *  3) null  → se omite el log a archivo (solo Logcat).
     */
    private File resolveLogFile() {
        try {
            String dir = VariablesGlobales.directorioactual;
            if (dir != null && !dir.trim().isEmpty()) {
                File f = new File(dir + "/DATOSDESALIDA/LOGEVENTOS.LOG");
                File parent = f.getParentFile();
                if (parent != null && (parent.exists() || parent.mkdirs())) return f;
            }
        } catch (Throwable ignored) { /* VariablesGlobales no disponible: seguimos al fallback */ }

        try {
            File base = getExternalFilesDir(null);
            if (base != null) return new File(base, "LOGEVENTOS.LOG");
        } catch (Throwable ignored) { }

        return null;
    }

    private void logEvento(String msg) {
        Log.i(TAG, msg);
        if (logfile != null) {
            try { utils.Log(logfile, "[TrackingLocationService]" + msg); }
            catch (Throwable ignored) { /* nunca dejar que el logging tumbe el servicio */ }
        }
    }

    // ==================== API PÚBLICA ====================

    /**
     * Inicia el rastreo. Llamar desde MenuDeLiquidacion.onCreate() una vez que
     * 'lector' (operario) está disponible, getParamsWs() devolvió true (cadenaURLapi
     * lista) y los permisos de ubicación están concedidos.
     *
     * @param ctx         Contexto (la Activity puede pasar 'this')
     * @param codOperador Código del operario (numérico)
     * @param apiUrl      URL base REST terminada en "/api/" (cadenaURLapi)
     */
    public static void iniciar(Context ctx, int codOperador, String apiUrl) {
        Intent intent = new Intent(ctx, TrackingLocationService.class);
        intent.putExtra("cod_operador", codOperador);
        intent.putExtra("api_url",      apiUrl);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ctx.startForegroundService(intent);
        } else {
            ctx.startService(intent);
        }
        Log.i(TAG, "[iniciar] Servicio lanzado | cod_operador=" + codOperador + " | url=" + apiUrl);
    }

    /** Detiene el servicio. Llamar desde MenuDeLiquidacion.onDestroy(). */
    public static void detener(Context ctx) {
        ctx.stopService(new Intent(ctx, TrackingLocationService.class));
        Log.i(TAG, "[detener] Servicio detenido");
    }
}