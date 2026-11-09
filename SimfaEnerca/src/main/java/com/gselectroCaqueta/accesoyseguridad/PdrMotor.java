package com.gselectroCaqueta.accesoyseguridad;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Navegacion por pasos (PDR, proyecto GPS fase 2): estima la posicion desde el ultimo GPS bueno contando pasos
 * con el acelerometro y la direccion con el vector de rotacion, para zonas sin GPS ni red.
 *
 * <ul>
 *   <li><b>Ancla:</b> solo fixes GPS con precision de 15 m o menos y a menos de 2,2 m/s (a pie).</li>
 *   <li><b>Pasos:</b> picos del acelerometro. No pide el permiso ACTIVITY_RECOGNITION.</li>
 *   <li><b>Direccion:</b> proyeccion horizontal de (eje superior - eje de la pantalla), asi sirve con el telefono
 *       plano, inclinado o casi vertical; + declinacion magnetica del lugar + desvio aprendido con el rumbo del GPS.</li>
 *   <li><b>Postura:</b> rumbo confiable con la parte superior entre -15 y 75 grados, de lado menos de 45 y la
 *       pantalla hacia arriba o hacia el tecnico. Hasta 4 pasos dudosos siguen con el ultimo rumbo; mas que eso es
 *       bolsillo: la posicion se queda y el error crece un largo de paso por paso.</li>
 *   <li><b>Calibracion:</b> largo de paso (0,35 a 1,10 m) aprendido caminando con GPS y guardado entre jornadas;
 *       desvio de la brujula aprendido en la jornada, con el telefono en la mano, descartando diferencias de mas de 60.</li>
 *   <li><b>Error:</b> precision del ancla + max(8 % de lo caminado, 0,5 m/s con pasos o 1,5 m/s sin pasos) +
 *       bolsillo + huecos de sensores (1,5 m/s).</li>
 *   <li><b>No estima</b> si el ancla tiene mas de 5 minutos, si se caminaron mas de 200 m o si el ultimo GPS iba a
 *       mas de 2,2 m/s (bicicleta, moto).</li>
 *   <li><b>Una instancia por proceso:</b> conserva el ancla al salir y volver a la pantalla. El estado se guarda cada
 *       ~5 s y se retoma al abrir (dentro de los 5 min), sumando el tiempo sin sensores al error.</li>
 *   <li><b>Validacion en sombra:</b> con GPS, cada ~40 m o 90 s compara su estimacion con el GPS real y lo escribe
 *       en files/pdr_validacion.csv (solo tramos a pie con el telefono en la mano).</li>
 * </ul>
 * Los sensores corren en un HandlerThread propio: la pantalla de lectura e impresion no se ve afectada.
 */
public final class PdrMotor implements SensorEventListener {

    private static final String TAG = "PdrMotor";
    private static final String PREFS = "pdr_estado";

    static final float ANCLA_PRECISION_MAX = 15f;
    static final float VELOCIDAD_VEHICULO = 2.2f;
    static final long VIGENCIA_MS = 5L * 60_000L;
    static final double DISTANCIA_MAX_M = 200;
    static final double ERROR_PROPORCIONAL = 0.08;
    static final double ERROR_CON_PASOS_MPS = 0.5;
    static final double ERROR_SIN_PASOS_MPS = 1.5;
    static final double ERROR_HUECO_MPS = 1.5;
    static final long HUECO_MS = 2000L;
    static final int PASOS_DUDOSOS_MAX = 4;
    static final double PASO_MIN = 0.35, PASO_MAX = 1.10, PASO_INICIAL = 0.70;
    static final float CAMINANDO_MIN = 0.7f;
    static final double SESGO_MAX_GRADOS = 60;
    static final double SOMBRA_DISTANCIA_M = 40;
    static final long SOMBRA_TIEMPO_MS = 90_000L;
    static final long GUARDAR_CADA_MS = 5_000L;

    // Deteccion de pasos (magnitud de la aceleracion, m/s2)
    private static final float PICO_MIN = 10.6f;
    private static final float VALLE_MAX = 9.2f;
    private static final long ENTRE_PASOS_MIN_MS = 250L;

    private static PdrMotor instancia;

    public static synchronized PdrMotor de(Context ctx) {
        if (instancia == null) instancia = new PdrMotor(ctx.getApplicationContext());
        return instancia;
    }

    private final Context ctx;
    private final SensorManager sensores;
    private HandlerThread hilo;
    private Handler handler;
    private boolean activo;

    // ---- ancla y desplazamiento (metros este / norte desde el ancla)
    private boolean hayAncla;
    private double anclaLat, anclaLon;
    private float anclaPrecision;
    private long anclaPared;          // System.currentTimeMillis del ancla
    private long anclaElapsed;        // elapsedRealtime del ancla
    private boolean anclaVehiculo;
    private double dx, dy, caminado;
    private int pasos;
    private double errorBolsillo, errorHuecos;
    private float declinacion;

    // ---- calibracion
    private double largoPaso = PASO_INICIAL;
    private double sesgoBrujula;      // grados, se suma al rumbo
    private int muestrasSesgo;

    // ---- sensores
    private final float[] matriz = new float[9];
    private boolean hayRumbo;
    private double rumboCrudo;        // grados desde el norte magnetico (sin declinacion ni sesgo)
    private boolean posturaOk;
    private double ultimoRumboBueno = Double.NaN;
    private int dudososSeguidos;
    private float filtrada = SensorManager.GRAVITY_EARTH;
    private boolean subiendo;
    private long ultimoPasoMs;
    private long ultimoEventoMs;
    private long ultimoGuardado;

    // ---- validacion en sombra (tramo propio, independiente del ancla)
    private boolean sombraActiva;
    private double sombraLat, sombraLon, sombraDx, sombraDy;
    private long sombraInicio;
    private int sombraPasos;
    private boolean sombraEnMano;

    private PdrMotor(Context ctx) {
        this.ctx = ctx;
        this.sensores = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        largoPaso = limitar(p.getFloat("largo_paso", (float) PASO_INICIAL), PASO_MIN, PASO_MAX);
        retomarEstado(p);
    }

    // ================================================================== ciclo de vida

    /** Enciende los sensores (al abrir la pantalla de lectura). */
    public synchronized void iniciar() {
        if (activo || sensores == null) return;
        Sensor acel = sensores.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor rot = sensores.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (acel == null || rot == null) return;
        hilo = new HandlerThread("pdr-sensores", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        hilo.start();
        handler = new Handler(hilo.getLooper());
        sensores.registerListener(this, acel, SensorManager.SENSOR_DELAY_GAME, handler);
        sensores.registerListener(this, rot, SensorManager.SENSOR_DELAY_GAME, handler);
        activo = true;
    }

    /** Apaga los sensores (al cerrar la pantalla). El ancla queda en memoria y en disco. */
    public synchronized void detener() {
        if (!activo) return;
        sensores.unregisterListener(this);
        guardarEstado();
        if (hilo != null) hilo.quitSafely();
        hilo = null;
        handler = null;
        activo = false;
    }

    // ================================================================== GPS

    /** Cada fix GPS real: ancla (si es bueno), calibra y cierra tramos de la validacion en sombra. */
    public synchronized void anclar(Location gps) {
        if (gps == null) return;
        long ahoraEl = SystemClock.elapsedRealtime();
        boolean vehiculo = gps.hasSpeed() && gps.getSpeed() > VELOCIDAD_VEHICULO;
        boolean caminando = gps.hasSpeed() && gps.getSpeed() >= CAMINANDO_MIN && !vehiculo;
        if (!gps.hasAccuracy() || gps.getAccuracy() > ANCLA_PRECISION_MAX) {
            if (vehiculo) anclaVehiculo = true;
            return;
        }
        // Desvio de la brujula: rumbo del GPS contra el rumbo de los sensores, caminando con el telefono en la mano.
        if (caminando && gps.hasBearing() && hayRumbo && posturaOk) {
            double dif = normalizar180(gps.getBearing() - (rumboCrudo + declinacion));
            if (Math.abs(dif) <= SESGO_MAX_GRADOS) {
                muestrasSesgo++;
                double a = muestrasSesgo < 10 ? 1.0 / muestrasSesgo : 0.1;
                sesgoBrujula = normalizar180(sesgoBrujula + a * normalizar180(dif - sesgoBrujula));
            }
        }
        cerrarTramoSombra(gps, caminando, vehiculo);

        hayAncla = true;
        anclaLat = gps.getLatitude();
        anclaLon = gps.getLongitude();
        anclaPrecision = gps.getAccuracy();
        anclaPared = System.currentTimeMillis();
        anclaElapsed = ahoraEl;
        anclaVehiculo = vehiculo;
        dx = dy = caminado = 0;
        pasos = 0;
        errorBolsillo = errorHuecos = 0;
        try {
            declinacion = new GeomagneticField((float) anclaLat, (float) anclaLon,
                    gps.hasAltitude() ? (float) gps.getAltitude() : 0f, anclaPared).getDeclination();
        } catch (Exception e) {
            declinacion = 0f;
        }
    }

    // ================================================================== estimacion

    /** La posicion estimada por pasos, o null si no hay una vigente. */
    public synchronized Location estimacion() {
        if (!hayAncla || anclaVehiculo) return null;
        long ahoraEl = SystemClock.elapsedRealtime();
        long edad = ahoraEl - anclaElapsed;
        if (edad < 0 || edad > VIGENCIA_MS || caminado > DISTANCIA_MAX_M) return null;
        double segundos = edad / 1000.0;
        // Hueco en curso: sensores callados (pantalla cerrada o app cerrada) desde el ultimo evento.
        double huecoActual = 0;
        long desdeEvento = ahoraEl - Math.max(ultimoEventoMs, anclaElapsed);
        if (desdeEvento > HUECO_MS) huecoActual = ERROR_HUECO_MPS * (desdeEvento / 1000.0);
        double porTiempo = (pasos > 0 ? ERROR_CON_PASOS_MPS : ERROR_SIN_PASOS_MPS) * segundos;
        double error = anclaPrecision + Math.max(ERROR_PROPORCIONAL * caminado, porTiempo)
                + errorBolsillo + errorHuecos + huecoActual;
        double[] ll = desplazar(anclaLat, anclaLon, dx, dy);
        Location l = new Location("pdr");
        l.setLatitude(ll[0]);
        l.setLongitude(ll[1]);
        l.setAccuracy((float) error);
        l.setTime(System.currentTimeMillis());
        l.setElapsedRealtimeNanos(ahoraEl * 1_000_000L);
        return l;
    }

    public synchronized boolean vigente() {
        return estimacion() != null;
    }

    // ================================================================== sensores

    @Override
    public void onSensorChanged(SensorEvent e) {
        synchronized (this) {
            long ahora = SystemClock.elapsedRealtime();
            if (ultimoEventoMs > 0 && ahora - ultimoEventoMs > HUECO_MS && hayAncla) {
                errorHuecos += ERROR_HUECO_MPS * ((ahora - ultimoEventoMs) / 1000.0);
            }
            ultimoEventoMs = ahora;
            if (e.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                actualizarRumbo(e.values);
            } else if (e.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                detectarPaso(e.values, ahora);
            }
            if (ahora - ultimoGuardado >= GUARDAR_CADA_MS) {
                ultimoGuardado = ahora;
                guardarEstado();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void actualizarRumbo(float[] v) {
        SensorManager.getRotationMatrixFromVector(matriz, v);
        // Columnas de la matriz: ejes del telefono en coordenadas del mundo (x este, y norte, z arriba).
        double topX = matriz[1], topY = matriz[4], topZ = matriz[7];
        double norX = matriz[2], norY = matriz[5], norZ = matriz[8];
        double ladoZ = matriz[6];
        double hx = topX - norX, hy = topY - norY;
        if (Math.hypot(hx, hy) < 0.2) {
            hayRumbo = false;
            posturaOk = false;
            return;
        }
        rumboCrudo = Math.toDegrees(Math.atan2(hx, hy));
        hayRumbo = true;
        double elevacionSuperior = Math.toDegrees(Math.asin(limitar(topZ, -1, 1)));
        double deLado = Math.abs(Math.toDegrees(Math.asin(limitar(ladoZ, -1, 1))));
        boolean pantallaOk = norZ > -0.2; // hacia arriba o hacia el tecnico, no hacia el suelo
        posturaOk = elevacionSuperior >= -15 && elevacionSuperior <= 75 && deLado < 45 && pantallaOk;
    }

    private void detectarPaso(float[] v, long ahora) {
        float mag = (float) Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        filtrada = 0.65f * filtrada + 0.35f * mag; // pasa la caminata (1,4-2,2 Hz) y quita el temblor
        if (filtrada > PICO_MIN) {
            subiendo = true;
        } else if (subiendo && filtrada < VALLE_MAX) {
            subiendo = false;
            if (ahora - ultimoPasoMs >= ENTRE_PASOS_MIN_MS) {
                ultimoPasoMs = ahora;
                registrarPaso();
            }
        }
    }

    private void registrarPaso() {
        if (!hayAncla || anclaVehiculo) return;
        double rumbo;
        boolean enMano;
        if (hayRumbo && posturaOk) {
            rumbo = rumboCrudo + declinacion + sesgoBrujula;
            ultimoRumboBueno = rumbo;
            dudososSeguidos = 0;
            enMano = true;
        } else if (!Double.isNaN(ultimoRumboBueno) && dudososSeguidos < PASOS_DUDOSOS_MAX) {
            dudososSeguidos++;
            rumbo = ultimoRumboBueno; // gesto breve de la mano: sigue con la ultima direccion buena
            enMano = true;
        } else {
            // Bolsillo, de lado o de cabeza: el paso cuenta, la posicion se queda y el error crece.
            dudososSeguidos++;
            pasos++;
            errorBolsillo += largoPaso;
            caminado += largoPaso;
            if (sombraActiva) sombraEnMano = false;
            return;
        }
        double r = Math.toRadians(rumbo);
        double ex = largoPaso * Math.sin(r), ny = largoPaso * Math.cos(r);
        dx += ex;
        dy += ny;
        caminado += largoPaso;
        pasos++;
        if (sombraActiva) {
            sombraDx += ex;
            sombraDy += ny;
            sombraPasos++;
            if (!enMano) sombraEnMano = false;
        }
    }

    // ================================================================== validacion en sombra

    private void cerrarTramoSombra(Location gps, boolean caminando, boolean vehiculo) {
        long ahora = SystemClock.elapsedRealtime();
        if (!sombraActiva) {
            iniciarSombra(gps, ahora);
            return;
        }
        float[] d = new float[1];
        Location.distanceBetween(sombraLat, sombraLon, gps.getLatitude(), gps.getLongitude(), d);
        boolean listo = d[0] >= SOMBRA_DISTANCIA_M || ahora - sombraInicio >= SOMBRA_TIEMPO_MS;
        if (!listo) return;
        if (vehiculo || !sombraEnMano || sombraPasos < 5) {
            iniciarSombra(gps, ahora); // tramo que no cumple (vehiculo, bolsillo, quieto): no cuenta
            return;
        }
        double[] est = desplazar(sombraLat, sombraLon, sombraDx, sombraDy);
        float[] err = new float[1];
        Location.distanceBetween(est[0], est[1], gps.getLatitude(), gps.getLongitude(), err);
        double recorrido = sombraPasos * largoPaso;
        double segundos = (ahora - sombraInicio) / 1000.0;
        double estimado = gps.getAccuracy() + Math.max(ERROR_PROPORCIONAL * recorrido, ERROR_CON_PASOS_MPS * segundos);
        // Largo de paso: distancia real del GPS / pasos, caminando en linea recta razonable.
        if (caminando && sombraPasos >= 8) {
            double observado = d[0] / sombraPasos;
            if (observado >= PASO_MIN && observado <= PASO_MAX) {
                largoPaso = limitar(0.8 * largoPaso + 0.2 * observado, PASO_MIN, PASO_MAX);
                ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putFloat("largo_paso", (float) largoPaso).apply();
            }
        }
        escribirCsv(String.format(Locale.US, "%s,%d,%.1f,%.1f,%.1f,%.1f,%.2f,%.1f",
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()),
                sombraPasos, d[0], err[0], estimado, segundos, largoPaso, sesgoBrujula));
        iniciarSombra(gps, ahora);
    }

    private void iniciarSombra(Location gps, long ahora) {
        sombraActiva = true;
        sombraLat = gps.getLatitude();
        sombraLon = gps.getLongitude();
        sombraDx = sombraDy = 0;
        sombraPasos = 0;
        sombraInicio = ahora;
        sombraEnMano = true;
    }

    private void escribirCsv(String linea) {
        final Handler h = handler;
        Runnable r = () -> {
            try {
                File dir = ctx.getExternalFilesDir(null);
                if (dir == null) return;
                File f = new File(dir, "pdr_validacion.csv");
                boolean nuevo = !f.exists();
                try (FileWriter w = new FileWriter(f, true)) {
                    if (nuevo) w.write("fecha,pasos,distancia_gps_m,error_real_m,error_estimado_m,segundos,largo_paso_m,sesgo_brujula\n");
                    w.write(linea + "\n");
                }
            } catch (Exception e) {
                Log.w(TAG, "csv: " + e.getMessage());
            }
        };
        if (h != null) h.post(r);
    }

    // ================================================================== estado entre reinicios

    private void guardarEstado() {
        SharedPreferences.Editor e = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();
        e.putBoolean("hay_ancla", hayAncla && !anclaVehiculo);
        if (hayAncla) {
            e.putLong("ancla_lat", Double.doubleToRawLongBits(anclaLat));
            e.putLong("ancla_lon", Double.doubleToRawLongBits(anclaLon));
            e.putFloat("ancla_precision", anclaPrecision);
            e.putLong("ancla_pared", anclaPared);
            e.putLong("dx", Double.doubleToRawLongBits(dx));
            e.putLong("dy", Double.doubleToRawLongBits(dy));
            e.putFloat("caminado", (float) caminado);
            e.putInt("pasos", pasos);
            e.putFloat("error_extra", (float) (errorBolsillo + errorHuecos));
            e.putFloat("declinacion", declinacion);
            e.putLong("guardado_pared", System.currentTimeMillis());
        }
        e.apply();
    }

    /** Al abrir: retoma la estimacion si no pasaron los 5 minutos y el reloj no esta atrasado. */
    private void retomarEstado(SharedPreferences p) {
        if (!p.getBoolean("hay_ancla", false)) return;
        long ahora = System.currentTimeMillis();
        long anclaP = p.getLong("ancla_pared", 0);
        long guardado = p.getLong("guardado_pared", 0);
        if (anclaP <= 0 || guardado <= 0 || ahora < guardado || ahora - anclaP > VIGENCIA_MS) {
            p.edit().putBoolean("hay_ancla", false).apply();
            return;
        }
        hayAncla = true;
        anclaLat = Double.longBitsToDouble(p.getLong("ancla_lat", 0));
        anclaLon = Double.longBitsToDouble(p.getLong("ancla_lon", 0));
        anclaPrecision = p.getFloat("ancla_precision", ANCLA_PRECISION_MAX);
        anclaPared = anclaP;
        anclaElapsed = SystemClock.elapsedRealtime() - (ahora - anclaP);
        dx = Double.longBitsToDouble(p.getLong("dx", 0));
        dy = Double.longBitsToDouble(p.getLong("dy", 0));
        caminado = p.getFloat("caminado", 0);
        pasos = p.getInt("pasos", 0);
        declinacion = p.getFloat("declinacion", 0);
        // El tiempo sin sensores (app cerrada o telefono apagado) suma al error.
        errorHuecos = p.getFloat("error_extra", 0) + ERROR_HUECO_MPS * ((ahora - guardado) / 1000.0);
        errorBolsillo = 0;
        ultimoEventoMs = SystemClock.elapsedRealtime();
        Log.i(TAG, "Estimacion retomada: ancla de hace " + ((ahora - anclaP) / 1000) + " s, " + pasos + " pasos");
    }

    // ================================================================== utilidades

    /** Desplaza (lat, lon) dx metros al este y dy al norte. */
    static double[] desplazar(double lat, double lon, double dx, double dy) {
        double r = 6_371_000.0;
        double nLat = lat + Math.toDegrees(dy / r);
        double nLon = lon + Math.toDegrees(dx / (r * Math.cos(Math.toRadians(lat))));
        return new double[]{nLat, nLon};
    }

    static double normalizar180(double g) {
        double x = g % 360;
        if (x > 180) x -= 360;
        if (x < -180) x += 360;
        return x;
    }

    static double limitar(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
