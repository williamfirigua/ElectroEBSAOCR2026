package com.gselectroCaqueta.accesoyseguridad;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;

/**
 * Sacudir el celular (lectura v2): dos sacudones fuertes seguidos disparan la accion (prender / apagar la
 * linterna). Uno solo no basta, para que un golpe al caminar o al guardar el celular no la cambie, y despues
 * de disparar espera un momento antes de aceptar otra.
 */
final class DetectorSacudida implements SensorEventListener {

    interface Accion {
        void sacudida();
    }

    /** Fuerza minima de un sacudon, en g (1 g = el celular quieto). */
    private static final float UMBRAL_G = 2.6f;
    /** Los dos sacudones deben caer dentro de esta ventana. */
    private static final long VENTANA_MS = 800;
    /** Entre dos picos del mismo sacudon. */
    private static final long ENTRE_PICOS_MS = 120;
    /** Despues de disparar, no vuelve a disparar en este tiempo. */
    private static final long ESPERA_MS = 1500;

    private final SensorManager sensores;
    private final Sensor acelerometro;
    private final Accion accion;
    private long primerPico;
    private long ultimoPico;
    private int picos;
    private long ultimoDisparo;
    private boolean activo;

    DetectorSacudida(Context ctx, Accion accion) {
        this.sensores = (SensorManager) ctx.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        this.acelerometro = sensores == null ? null : sensores.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.accion = accion;
    }

    boolean disponible() {
        return acelerometro != null;
    }

    void iniciar() {
        if (activo || acelerometro == null) return;
        activo = sensores.registerListener(this, acelerometro, SensorManager.SENSOR_DELAY_UI);
    }

    void detener() {
        if (!activo) return;
        sensores.unregisterListener(this);
        activo = false;
        picos = 0;
    }

    @Override
    public void onSensorChanged(SensorEvent e) {
        float gx = e.values[0] / SensorManager.GRAVITY_EARTH;
        float gy = e.values[1] / SensorManager.GRAVITY_EARTH;
        float gz = e.values[2] / SensorManager.GRAVITY_EARTH;
        double g = Math.sqrt(gx * gx + gy * gy + gz * gz);
        if (g < UMBRAL_G) return;

        long ahora = SystemClock.elapsedRealtime();
        if (ahora - ultimoDisparo < ESPERA_MS) return;
        if (ahora - ultimoPico < ENTRE_PICOS_MS) return; // mismo sacudon
        if (picos == 0 || ahora - primerPico > VENTANA_MS) {
            primerPico = ahora;
            picos = 0;
        }
        ultimoPico = ahora;
        picos++;
        if (picos >= 2) {
            picos = 0;
            ultimoDisparo = ahora;
            accion.sacudida();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
