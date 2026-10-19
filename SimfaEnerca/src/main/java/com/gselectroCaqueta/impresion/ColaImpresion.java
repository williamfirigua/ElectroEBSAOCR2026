package com.gselectroCaqueta.impresion;

import android.util.Log;

import com.gselectroCaqueta.modulobluetooth.btPrintFile;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Cola única de envío a la impresora Bluetooth.
 * <p>
 * La liquidación encola el CPCL ya renderizado y vuelve de inmediato; un solo hilo lo escribe
 * al socket. Al ser un único hilo, las facturas y cartas salen en el orden en que se encolaron
 * y nunca se intercalan bytes de dos trabajos.
 * <p>
 * No hay pausas entre bloques: RFCOMM ya tiene control de flujo y {@code OutputStream.write}
 * bloquea este hilo (no el de la pantalla) mientras el buffer de la impresora está lleno.
 * Los bloques de {@link #TAMANO_BLOQUE} solo acotan cuánto se retiene en memoria del stack BT.
 */
public final class ColaImpresion {

    private static final String TAG = "ColaImpresion";
    static final int TAMANO_BLOQUE = 4096;
    /** Limite para reconectar antes de imprimir (Android por si solo espera ~9 s con la impresora apagada). */
    static final long TIEMPO_RECONEXION_MS = 4000;

    /** Resultado de un trabajo; se invoca en el hilo de la cola. */
    public interface Callback {
        void alTerminar(boolean ok, String detalle);
    }

    private static final AtomicInteger pendientes = new AtomicInteger();
    /** Ultima vez (elapsedRealtime/nanoTime en ms) que salieron bytes hacia la impresora. */
    private static volatile long ultimoEnvioMs = 0;
    /** Si la impresora recibio algo hace menos de esto, se considera despierta. */
    static final long INACTIVIDAD_PARA_DESPERTAR_MS = 8000;
    /**
     * Consulta SGD de Zebra sin efecto en el papel: la impresora la contesta (el hilo de lectura del servicio
     * descarta la respuesta) y no imprime nada. No se usa un CRLF suelto porque en modo line_print la impresora
     * lo tomaria como un renglon en blanco y avanzaria papel.
     */
    private static final byte[] DESPERTAR = "! U1 getvar \"device.friendly_name\"\r\n"
            .getBytes(java.nio.charset.Charset.forName("ISO-8859-1"));

    private static long ahoraMs() {
        return System.nanoTime() / 1_000_000;
    }

    /**
     * Despierta el enlace antes de imprimir. Medido en la ZQ520: la PRIMERA factura tras un rato sin enviar
     * pasa ~2 s con el icono de recepcion antes de imprimir (el enlace Bluetooth y la impresora entran en
     * ahorro de energia); la segunda sale mas rapido. Se llama cuando el tecnico empieza a digitar la lectura:
     * una consulta SGD (no imprime nada) reactiva el enlace mientras termina de digitar,
     * y si el enlace estaba caido la cola reconecta ahi mismo, no al imprimir. No hace nada si hubo envio
     * reciente o si ya hay trabajo en cola.
     */
    public static void despertar(final btPrintFile servicio) {
        if (servicio == null || pendientes.get() > 0) return;
        if (ahoraMs() - ultimoEnvioMs < INACTIVIDAD_PARA_DESPERTAR_MS) return;
        ultimoEnvioMs = ahoraMs(); // evita encolar varios despertares seguidos
        pendientes.incrementAndGet();
        HILO.execute(() -> {
            try {
                if (servicio.getState() != btPrintFile.STATE_CONNECTED
                        && !servicio.reconectarSincrono(TIEMPO_RECONEXION_MS)) {
                    return;
                }
                synchronized (btPrintFile.BLOQUEO_ESCRITURA) {
                    servicio.write(DESPERTAR);
                }
                ultimoEnvioMs = ahoraMs();
            } catch (Throwable e) {
                Log.w(TAG, "despertar: " + e);
            } finally {
                pendientes.decrementAndGet();
            }
        });
    }

    private static final ExecutorService HILO = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "cola-impresion");
        t.setDaemon(true);
        return t;
    });

    private ColaImpresion() {
    }

    /**
     * Espera hasta {@code maxMs} a que termine lo encolado. Llamar antes de detener o desconectar el
     * servicio Bluetooth para no cortar una factura que todavía está saliendo.
     *
     * @return true si la cola quedó vacía.
     */
    public static boolean esperarVacia(long maxMs) {
        if (pendientes.get() == 0) return true;
        try {
            HILO.submit(() -> { }).get(maxMs, java.util.concurrent.TimeUnit.MILLISECONDS);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "la cola no se vacio en " + maxMs + " ms: " + e);
            return false;
        }
    }

    /** Trabajos encolados que aún no terminan de escribirse (para el indicador "Imprimiendo..."). */
    public static int pendientes() {
        return pendientes.get();
    }

    /**
     * Encola {@code datos} para enviarlos completos a {@code servicio}.
     *
     * @return false si no se pudo encolar (servicio nulo o datos vacíos); true si quedó en cola.
     */
    public static boolean encolar(final btPrintFile servicio, final byte[] datos, final String etiqueta, final Callback callback) {
        if (servicio == null || datos == null || datos.length == 0) return false;
        pendientes.incrementAndGet();
        HILO.execute(() -> {
            boolean ok = false;
            String detalle = etiqueta;
            long t0 = System.nanoTime();
            try {
                // Enlace caido (impresora apagada, lejos...): se reconecta aqui, fuera de la pantalla, con
                // limite de tiempo. No se reintenta la impresion por cuenta propia: si falla, decide el tecnico.
                if (servicio.getState() != btPrintFile.STATE_CONNECTED
                        && !servicio.reconectarSincrono(TIEMPO_RECONEXION_MS)) {
                    detalle = etiqueta + ": la impresora no responde (encendida y cerca?)";
                    return;
                }
                // El trabajo completo bajo el candado: nada se intercala entre sus bloques.
                synchronized (btPrintFile.BLOQUEO_ESCRITURA) {
                    for (int desde = 0; desde < datos.length; desde += TAMANO_BLOQUE) {
                        int hasta = Math.min(datos.length, desde + TAMANO_BLOQUE);
                        byte[] bloque = new byte[hasta - desde];
                        System.arraycopy(datos, desde, bloque, 0, bloque.length);
                        if (!servicio.write(bloque)) {
                            detalle = etiqueta + ": falló la escritura en el byte " + desde + " de " + datos.length;
                            return;
                        }
                    }
                }
                ok = true;
                ultimoEnvioMs = ahoraMs();
                detalle = etiqueta + ": " + datos.length + " bytes en " + ((System.nanoTime() - t0) / 1_000_000) + " ms";
            } catch (Throwable e) {
                detalle = etiqueta + ": " + e;
                Log.e(TAG, detalle, e);
            } finally {
                pendientes.decrementAndGet();
                if (callback != null) {
                    try {
                        callback.alTerminar(ok, detalle);
                    } catch (Throwable e) {
                        Log.e(TAG, "callback", e);
                    }
                }
            }
        });
        return true;
    }
}
