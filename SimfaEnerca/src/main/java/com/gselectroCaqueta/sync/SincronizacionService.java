package com.gselectroCaqueta.sync;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.gselectroCaqueta.accesoyseguridad.AlmacenEstado;
import com.gselectroCaqueta.accesoyseguridad.BandejaMensajes;
import com.gselectroCaqueta.accesoyseguridad.CodigoSupervisor;
import com.gselectroCaqueta.accesoyseguridad.VariablesGlobales;
import com.gsutil.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Despacha la cola (ebsa_sync.db) a /api/v1 en segundo plano: liquidaciones, tracking, LBL y fotos, en ese
 * orden. Toda la lógica de envío está en {@link MotorSincronizacion}; aquí solo hay ciclo de vida, cuándo
 * correr y avisos.
 *
 * Cuándo corre:
 *  - al liquidar / tomar foto / imprimir ({@link #solicitar}), al volver la red (NetworkCallback) y cuando
 *    vence el reintento más cercano de la cola; latido cada 15 min como red de seguridad;
 *  - el tracking solo lo "despierta" ({@link #despertar}) cada N puntos, sin arrancarlo (Android 12 no deja
 *    iniciar un foreground service desde segundo plano).
 *
 * Cuándo para: cuando MenuDeLiquidacion se cierra ({@link #alSalir}) o el sistema lo reinicia sin Activity
 * (START_STICKY), se detiene en cuanto la cola queda vacía. Con pendientes sigue vivo hasta despacharlos,
 * como el servicio de Lecturas.
 *
 * Un solo hilo (HandlerThread): nunca hay dos ciclos del motor a la vez.
 */
public class SincronizacionService extends Service {

    private static final String TAG = "EbsaSync";
    private static final String CANAL = "ebsa_sync";
    private static final int NOTIF_ID = 3002;

    private static final String ACCION_SINCRONIZAR = "com.gselectroCaqueta.sync.SINCRONIZAR";
    private static final String ACCION_VACIAR = "com.gselectroCaqueta.sync.VACIAR";

    private static final long LATIDO_MS = 15 * 60_000L;
    /** Con la liquidacion abierta, la bandeja de mensajes se consulta cada 3 minutos (una peticion GET corta). */
    private static final long RECEPCION_MS = 3 * 60_000L;
    private static final long MINIMA_ESPERA_MS = 5_000L;
    private static final long DESPERTAR_MS = 3_000L; // agrupa varios avisos seguidos en un solo ciclo
    private static final long DIA_MS = 24 * 3600_000L;
    /** Enviados se conservan para diagnóstico; tracking pendiente sin despachar en 3 días se descarta. */
    private static final long RETENCION_ENVIADOS_MS = 15 * DIA_MS;
    private static final long RETENCION_TRACKING_PENDIENTE_MS = 3 * DIA_MS;

    private static volatile SincronizacionService activo;

    private HandlerThread hilo;
    private Handler h;
    private ColaEnvioSqlite cola;
    private ConfigSync config;
    private MotorSincronizacion motor;
    private String baseMotor;
    private volatile boolean salirCuandoVacio;
    private ConnectivityManager.NetworkCallback red;
    private long ultimaPurga;
    private long ultimaRecepcion;
    private final Utils utils = new Utils();
    private File logfile;

    private final Runnable ciclo = new Runnable() {
        @Override
        public void run() {
            ejecutarCiclo();
        }
    };

    // ------------------------------------------------------------------ API para la app

    /** Desde una Activity en primer plano: arranca el servicio si no está y corre un ciclo ya. */
    public static void solicitar(Context ctx) {
        SincronizacionService s = activo;
        if (s != null) {
            s.salirCuandoVacio = false;
            s.programar(0);
            return;
        }
        Intent i = new Intent(ctx, SincronizacionService.class).setAction(ACCION_SINCRONIZAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ctx.startForegroundService(i);
        else ctx.startService(i);
    }

    /**
     * Fuera de la liquidación (GuiAcceso): despacha lo que haya quedado y se detiene. Si ya está corriendo
     * para la liquidación, solo corre un ciclo.
     */
    public static void vaciarCola(Context ctx) {
        SincronizacionService s = activo;
        if (s != null) {
            s.programar(0);
            return;
        }
        Intent i = new Intent(ctx, SincronizacionService.class).setAction(ACCION_VACIAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ctx.startForegroundService(i);
        else ctx.startService(i);
    }

    /** Desde otro servicio (tracking): solo si ya está corriendo. Lo encolado se envía igual en el próximo ciclo. */
    public static void despertar() {
        SincronizacionService s = activo;
        if (s != null) s.programar(DESPERTAR_MS);
    }

    /** MenuDeLiquidacion.onDestroy: termina lo pendiente y se detiene. */
    public static void alSalir() {
        SincronizacionService s = activo;
        if (s != null) {
            s.salirCuandoVacio = true;
            s.programar(0);
        }
    }

    public static boolean corriendo() {
        return activo != null;
    }

    // ------------------------------------------------------------------ ciclo de vida

    @Override
    public void onCreate() {
        super.onCreate();
        crearCanal();
        startForeground(NOTIF_ID, notificacion("Sincronización activa"));
        logfile = resolverLog();
        cola = ColaEnvioSqlite.de(this);
        config = new ConfigSync(this);
        hilo = new HandlerThread("ebsa-sync");
        hilo.start();
        h = new Handler(hilo.getLooper());
        registrarRed();
        activo = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Contrato FGS: startForeground antes de cualquier rama (si no, el sistema mata el proceso a los 5 s).
        startForeground(NOTIF_ID, notificacion("Sincronización activa"));
        // intent null = reinicio START_STICKY sin la app abierta: vaciar la cola y parar.
        salirCuandoVacio = intent == null || ACCION_VACIAR.equals(intent.getAction());
        programar(0);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        activo = null;
        if (red != null) {
            try {
                ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).unregisterNetworkCallback(red);
            } catch (Exception ignored) {
            }
        }
        if (h != null) h.removeCallbacksAndMessages(null);
        if (hilo != null) hilo.quitSafely();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // ------------------------------------------------------------------ ciclo

    private void programar(long enMs) {
        Handler hh = h;
        if (hh == null) return;
        hh.removeCallbacks(ciclo);
        hh.postDelayed(ciclo, Math.max(0, enMs));
    }

    private void ejecutarCiclo() {
        long ahora = System.currentTimeMillis();
        try {
            purgar(ahora);
            String base = config.baseApi();
            if (base.isEmpty()) {
                log("sin URL de API configurada (se toma al abrir la liquidación)");
                if (salirCuandoVacio) stopSelf();
                else programar(LATIDO_MS);
                return;
            }
            MotorSincronizacion m = motor(base);
            if (config.tokenDeOtroOperador()) cambioDeOperador(m, ahora);

            MotorSincronizacion.Resumen r = m.ejecutar(ahora);
            if (r.estado != MotorSincronizacion.Estado.NADA_PENDIENTE) log("ciclo " + r);
            recibir(m, System.currentTimeMillis(), r);
            siguiente(r, System.currentTimeMillis());
        } catch (Throwable t) {
            // Nada de lo que pase aquí puede tumbar la app (el servicio vive en el mismo proceso).
            log("ciclo error: " + t);
            programar(LATIDO_MS);
        }
    }

    private void siguiente(MotorSincronizacion.Resumen r, long ahora) {
        long prox = cola.proximoIntento();
        actualizarNotificacion(r);
        if (prox < 0) {
            if (salirCuandoVacio) {
                log("cola vacía: servicio detenido");
                stopSelf();
            } else {
                programar(RECEPCION_MS); // liquidacion abierta: el proximo latido revisa la bandeja
            }
            return;
        }
        if (r != null && r.estado == MotorSincronizacion.Estado.REQUIERE_LOGIN) {
            // Sin clave no hay nada que hacer hasta que el técnico ingrese (GuiAcceso lo vuelve a arrancar).
            if (salirCuandoVacio) stopSelf();
            else programar(LATIDO_MS);
            return;
        }
        long espera;
        if (prox > ahora) {
            espera = Math.max(prox - ahora, MINIMA_ESPERA_MS);
        } else {
            // Quedan listos: si este ciclo avanzó son más lotes y se sigue ya; si no, no girar en vacío.
            boolean avanzo = r != null && !r.enviados.isEmpty();
            espera = avanzo ? 0 : 30_000L;
        }
        programar(Math.min(espera, LATIDO_MS));
    }

    /**
     * Otro técnico ingresó en la terminal. El token del anterior sigue vigente hasta que el nuevo haga login
     * (el login revoca las sesiones del serial), así que primero se despacha con él lo encolado: el tracking
     * se asigna al operador del token y no puede quedar a nombre del técnico nuevo. Lo que no salga se
     * descarta para no atribuirle a uno el recorrido del otro.
     */
    private void cambioDeOperador(MotorSincronizacion m, long ahora) {
        MotorSincronizacion.Resumen r = m.ejecutar(ahora);
        log("cambio de operador, despacho con la sesión anterior: " + r);
        int quedan = cola.contar(ColaEnvio.TRACKING, ColaEnvio.PENDIENTE);
        if (quedan > 0) {
            int n = cola.descartarPendientesViejos(ColaEnvio.TRACKING, System.currentTimeMillis() + 1);
            log("cambio de operador: " + n + " puntos de tracking del técnico anterior descartados");
        }
        config.borrar();
        // La bandeja y las claves de codigos eran del tecnico anterior.
        new BandejaMensajes(getFilesDir()).vaciar();
        new CodigoSupervisor(AlmacenEstado.de(this, "codigos_supervisor")).olvidarClaves();
    }

    /**
     * Bandeja de mensajes y claves de codigos de supervisor: solo con la liquidacion abierta, cada RECEPCION_MS,
     * y no si el ciclo acaba de quedar sin red o sin sesion (no tiene sentido insistir en el mismo latido).
     */
    private void recibir(MotorSincronizacion m, long ahora, MotorSincronizacion.Resumen r) {
        if (salirCuandoVacio || ahora - ultimaRecepcion < RECEPCION_MS) return;
        if (r != null && (r.estado == MotorSincronizacion.Estado.SIN_RED || r.estado == MotorSincronizacion.Estado.REQUIERE_LOGIN)) return;
        ultimaRecepcion = ahora;
        try {
            m.recibir(new ReceptorApp(), ahora);
        } catch (Throwable t) {
            log("bandeja: " + t);
        }
    }

    private final class ReceptorApp implements MotorSincronizacion.Receptor {
        private final BandejaMensajes bandeja = new BandejaMensajes(getFilesDir());
        private final CodigoSupervisor codigos = new CodigoSupervisor(AlmacenEstado.de(SincronizacionService.this, "codigos_supervisor"));

        @Override
        public long ultimoMensaje() {
            return bandeja.ultimoId();
        }

        @Override
        public void mensajes(List<Map<String, Object>> nuevos) {
            bandeja.agregar(nuevos);
        }

        @Override
        public long clavesDescargadasEn() {
            return codigos.descargadasEn();
        }

        @Override
        public void claves(List<Map<String, Object>> claves, long ahoraMs) {
            List<CodigoSupervisor.Clave> lista = new ArrayList<>();
            for (Map<String, Object> c : claves) {
                lista.add(new CodigoSupervisor.Clave(Json.texto(c, "supervisor"), Json.texto(c, "nombre"), String.valueOf(Json.texto(c, "clave"))));
            }
            codigos.guardarClaves(lista, ahoraMs);
        }
    }

    private MotorSincronizacion motor(String base) {
        if (motor == null || !base.equals(baseMotor)) {
            motor = new MotorSincronizacion(new ApiMovil(base), cola, config, config, new Observador());
            baseMotor = base;
        }
        return motor;
    }

    private void purgar(long ahora) {
        if (ahora - ultimaPurga < DIA_MS / 4) return;
        ultimaPurga = ahora;
        int e = cola.purgarEnviados(ahora - RETENCION_ENVIADOS_MS);
        int t = cola.descartarPendientesViejos(ColaEnvio.TRACKING, ahora - RETENCION_TRACKING_PENDIENTE_MS);
        if (e > 0 || t > 0) log("purga: " + e + " enviados, " + t + " puntos de tracking vencidos sin enviar");
    }

    private void registrarRed() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return;
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            red = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    Handler hh = h;
                    if (hh == null) return;
                    hh.post(new Runnable() {
                        @Override
                        public void run() {
                            // Volvió la señal: lo que esperaba backoff por falta de red sale ya.
                            cola.adelantarReprogramados();
                            programar(2_000);
                        }
                    });
                }
            };
            cm.registerDefaultNetworkCallback(red);
        } catch (Exception e) {
            red = null;
            log("sin NetworkCallback: " + e);
        }
    }

    // ------------------------------------------------------------------ avisos

    private final class Observador implements MotorSincronizacion.Observador {
        @Override
        public void archivoEnviado(ColaEnvio.Elemento e) {
            // El índice LBLS/L... y DATOSDEENTRADA/F... no se reescriben desde aquí: los escribe el hilo de la
            // liquidación y una reescritura concurrente los corrompería. En modo REST la cola es la fuente de
            // verdad de qué se envió.
        }

        @Override
        public void requiereLogin(String motivo) {
            log("requiere ingreso del técnico: " + motivo);
        }

        @Override
        public void registro(String mensaje) {
            log(mensaje);
        }
    }

    private void actualizarNotificacion(MotorSincronizacion.Resumen r) {
        int liq = cola.contar(ColaEnvio.LIQUIDACION, ColaEnvio.PENDIENTE);
        int arch = cola.contar(ColaEnvio.LBL, ColaEnvio.PENDIENTE) + cola.contar(ColaEnvio.FOTO, ColaEnvio.PENDIENTE);
        int err = cola.contar(ColaEnvio.LIQUIDACION, ColaEnvio.ERROR);
        String texto;
        if (r != null && r.estado == MotorSincronizacion.Estado.REQUIERE_LOGIN && esProblemaDeTerminal(r.detalle)) {
            // No lo arregla el técnico con su clave: es la asignación en la web (Operadores / Móviles).
            texto = "Terminal no habilitada para este operador, avise al supervisor (" + liq + " lecturas guardadas)";
        } else if (r != null && r.estado == MotorSincronizacion.Estado.REQUIERE_LOGIN) {
            texto = "Ingrese de nuevo con su clave para enviar (" + liq + " lecturas pendientes)";
        } else if (liq + arch == 0) {
            texto = err > 0 ? "Al día · " + err + " lecturas rechazadas por el servidor" : "Al día";
        } else {
            String estado = r == null ? "" : r.estado == MotorSincronizacion.Estado.SIN_RED ? " · sin señal"
                    : r.estado == MotorSincronizacion.Estado.SERVIDOR_NO_DISPONIBLE ? " · servidor no disponible" : "";
            texto = "Pendientes: " + liq + " lecturas, " + arch + " archivos" + estado;
        }
        try {
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) nm.notify(NOTIF_ID, notificacion(texto));
        } catch (Exception ignored) {
        }
    }

    private static boolean esProblemaDeTerminal(String detalle) {
        return detalle != null && (detalle.startsWith("terminal_") || detalle.startsWith("operador_sin_terminal"));
    }

    private void crearCanal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(CANAL, "Envío de facturación", NotificationManager.IMPORTANCE_LOW);
            ch.setDescription("Envío al servidor de lecturas, fotos, LBL y recorrido");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }

    private Notification notificacion(String texto) {
        return new NotificationCompat.Builder(this, CANAL)
                .setContentTitle("Envío de facturación")
                .setContentText(texto)
                .setSmallIcon(android.R.drawable.stat_sys_upload)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private File resolverLog() {
        try {
            String dir = VariablesGlobales.directorioactual;
            if (dir != null && !dir.trim().isEmpty()) {
                File f = new File(dir + "/DATOSDESALIDA/LOGEVENTOS.LOG");
                File parent = f.getParentFile();
                if (parent != null && (parent.exists() || parent.mkdirs())) return f;
            }
        } catch (Throwable ignored) {
        }
        try {
            File base = getExternalFilesDir(null);
            if (base != null) return new File(base, "LOGEVENTOS.LOG");
        } catch (Throwable ignored) {
        }
        return null;
    }

    private void log(String m) {
        Log.i(TAG, m);
        if (logfile != null) {
            try {
                utils.Log(logfile, "[SincronizacionService]" + m);
            } catch (Throwable ignored) {
            }
        }
    }
}