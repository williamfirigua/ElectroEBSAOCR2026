package com.gsutil;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Bitácora de texto que no bloquea a quien escribe.
 * <p>
 * Reemplaza el patrón "abrir RandomAccessFile, seek al final, writeBytes, cerrar" por línea, que en
 * la liquidación se repetía ~50 veces por factura en el hilo de la pantalla. Aquí cada línea se
 * encola con su marca de tiempo en milisegundos (tomada al encolar, no al escribir) y un único hilo
 * la escribe en un {@link BufferedWriter} que permanece abierto; se hace flush cuando la cola queda
 * vacía, así una caída de la app pierde como mucho las líneas en vuelo.
 * <p>
 * Escribe en ISO-8859-1 para conservar el mismo contenido byte a byte que {@code writeBytes}.
 */
public final class BitacoraAsincrona {

    private static final String TAG = "BitacoraAsincrona";
    private static final Charset LATIN1 = Charset.forName("ISO-8859-1");

    private final ExecutorService hilo;
    private final ThreadLocal<SimpleDateFormat> formato = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
        }
    };

    // Solo se tocan desde el hilo de la bitácora.
    private BufferedWriter escritor;
    private String rutaAbierta;
    /** Líneas encoladas y aún no escritas; se hace flush cuando llega a 0 (fin de la ráfaga). */
    private final java.util.concurrent.atomic.AtomicInteger enCola = new java.util.concurrent.atomic.AtomicInteger();

    public BitacoraAsincrona(final String nombreHilo) {
        hilo = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, nombreHilo);
            t.setDaemon(true);
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        });
    }

    /**
     * Encola {@code texto} para {@code archivo}. La ruta se evalúa por línea porque el directorio de
     * trabajo puede cambiar al cambiar de ciclo; si cambia, se cierra el archivo anterior.
     */
    public void escribir(final File archivo, final String texto) {
        final String linea = formato.get().format(new Date()) + "|" + texto + "\r\n";
        enCola.incrementAndGet();
        try {
            hilo.execute(() -> escribirEnHilo(archivo, linea));
        } catch (java.util.concurrent.RejectedExecutionException e) {
            enCola.decrementAndGet();
            Log.w(TAG, "bitacora cerrada, se descarta: " + texto);
        }
    }

    private void escribirEnHilo(File archivo, String linea) {
        boolean ultima = enCola.decrementAndGet() == 0;
        try {
            String ruta = archivo.getAbsolutePath();
            // exists(): si borraron DATOSDESALIDA (cambio de ciclo) se reabre en vez de escribir a un archivo borrado.
            if (escritor == null || !ruta.equals(rutaAbierta) || !archivo.exists()) {
                cerrarEscritor();
                File dir = archivo.getParentFile();
                if (dir != null && !dir.isDirectory()) dir.mkdirs();
                escritor = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(archivo, true), LATIN1), 8192);
                rutaAbierta = ruta;
            }
            escritor.write(linea);
            if (ultima) escritor.flush();
        } catch (IOException e) {
            Log.e(TAG, "escribiendo " + archivo + ": " + e.getMessage());
            cerrarEscritor();
        }
    }

    private void cerrarEscritor() {
        if (escritor == null) return;
        try {
            escritor.close();
        } catch (IOException e) {
            Log.e(TAG, "cerrando " + rutaAbierta + ": " + e.getMessage());
        }
        escritor = null;
        rutaAbierta = null;
    }

    /** Hace flush de lo pendiente; no detiene la bitácora. Llamar en onPause/onDestroy. */
    public void vaciar() {
        try {
            hilo.execute(() -> {
                if (escritor == null) return;
                try {
                    escritor.flush();
                } catch (IOException e) {
                    Log.e(TAG, "flush: " + e.getMessage());
                }
            });
        } catch (java.util.concurrent.RejectedExecutionException ignored) {
        }
    }
}
