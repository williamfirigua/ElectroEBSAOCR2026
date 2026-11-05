package com.gselectroCaqueta.sync;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;

import com.gselectroCaqueta.accesoyseguridad.VariablesGlobales;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Respaldo en la tarjeta SD (por si el celular se daña y la información no se ha sincronizado): con la SD se puede
 * sacar la ruta trabajada y seguir en otro equipo.
 *
 * Qué copia: los .SDA de DATOSDESALIDA, TOMOLECTURA.TXT y LOGEVENTOS.LOG, el NOMBRE de la ruta y las fotos de
 * DCIM/FOTOGRAFIASL. Solo lo que cambió (tamaño o fecha), en un hilo de baja prioridad, cada {@link #CADA_MS}
 * mientras la liquidación está abierta y una vez más al salir de ella: escribir en la SD no frena la pantalla.
 *
 * Dónde: carpeta propia de la app en la SD (Android/data/&lt;paquete&gt;/files/RESPALDO_EBSA/&lt;ruta&gt;/), que no
 * necesita permisos. Se lee conectando la SD a un PC. Sin SD no hace nada.
 */
public final class RespaldoSd {

    public static final long CADA_MS = 10 * 60_000L;
    private static final int MAX_FOTOS_POR_PASADA = 300;
    private static final String CARPETA = "RESPALDO_EBSA";
    private static final String PREFS = "respaldo_sd";
    private static final String TAG = "RespaldoSd";

    private static HandlerThread hilo;
    private static Handler h;
    private static Context app;
    private static boolean periodico;

    private static final Runnable pasada = new Runnable() {
        @Override
        public void run() {
            ejecutar();
            Handler hh = h;
            if (periodico && hh != null) hh.postDelayed(this, CADA_MS);
        }
    };

    private RespaldoSd() {
    }

    /** Liquidación abierta: una pasada ya y luego cada CADA_MS. */
    public static synchronized void iniciar(Context ctx) {
        asegurarHilo(ctx);
        periodico = true;
        h.removeCallbacks(pasada);
        h.post(pasada);
    }

    /** Al salir de la pantalla (o abrir la cámara): una pasada sin esperar el turno. No cambia el periódico. */
    public static synchronized void ahora(Context ctx) {
        asegurarHilo(ctx);
        h.removeCallbacks(pasada);
        h.post(pasada);
    }

    /** Liquidación cerrada: una última pasada y se detiene el periódico. */
    public static synchronized void detener(Context ctx) {
        asegurarHilo(ctx);
        periodico = false;
        h.removeCallbacks(pasada);
        h.post(pasada);
    }

    /** Texto para mostrar: "Respaldo en SD: 05/11 15:20 · 12 archivos" / "Sin tarjeta SD" / "". */
    public static String estado(Context ctx) {
        SharedPreferences p = ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        long cuando = p.getLong("cuando", 0);
        String error = p.getString("error", "");
        if (destino(ctx) == null) return "Sin tarjeta SD: no hay respaldo";
        if (cuando == 0) return "Respaldo en SD: todavía no";
        String t = new SimpleDateFormat("dd/MM HH:mm", Locale.US).format(new Date(cuando));
        return "Respaldo en SD: " + t + (error.isEmpty() ? "" : " (con errores: " + error + ")");
    }

    // ------------------------------------------------------------------ copia

    private static void asegurarHilo(Context ctx) {
        if (app == null) app = ctx.getApplicationContext();
        if (hilo == null) {
            hilo = new HandlerThread("ebsa-respaldo-sd", Process.THREAD_PRIORITY_BACKGROUND);
            hilo.start();
            h = new Handler(hilo.getLooper());
        }
    }

    /** Carpeta de la app en la SD extraíble montada, o null. */
    static File destino(Context ctx) {
        try {
            File[] dirs = ctx.getApplicationContext().getExternalFilesDirs(null);
            for (int i = 1; i < dirs.length; i++) { // 0 = memoria interna
                File d = dirs[i];
                if (d == null) continue;
                if (Environment.isExternalStorageRemovable(d)
                        && Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState(d))) {
                    return new File(d, CARPETA);
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static void ejecutar() {
        Context ctx = app;
        if (ctx == null) return;
        String trabajo = VariablesGlobales.directorioactual;
        if (trabajo == null || trabajo.trim().isEmpty()) return;
        File raiz = destino(ctx);
        if (raiz == null) return;
        File base = new File(trabajo);
        File salida = new File(base, "DATOSDESALIDA");
        if (!salida.isDirectory()) return;

        Contador c = new Contador();
        File dest = new File(raiz, nombreRuta(base));
        try {
            File[] archivos = salida.listFiles();
            if (archivos != null) {
                for (File f : archivos) {
                    String n = f.getName().toUpperCase(Locale.ROOT);
                    if (f.isFile() && (n.endsWith(".SDA") || n.equals("TOMOLECTURA.TXT") || n.equals("LOGEVENTOS.LOG"))) {
                        copiarSiCambio(f, new File(dest, "DATOSDESALIDA/" + f.getName()), c);
                    }
                }
            }
            File nombre = new File(base, "DATOSDEENTRADA/NOMBRE");
            if (nombre.isFile()) copiarSiCambio(nombre, new File(dest, "DATOSDEENTRADA/NOMBRE"), c);

            File fotos = new File(base, "DCIM/FOTOGRAFIASL");
            File[] lista = fotos.listFiles();
            if (lista != null) {
                int nuevas = 0;
                for (File f : lista) {
                    String n = f.getName().toLowerCase(Locale.ROOT);
                    if (!f.isFile() || !(n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png"))) continue;
                    File d = new File(dest, "FOTOGRAFIAS/" + f.getName());
                    if (d.isFile() && d.length() == f.length()) continue; // una foto no cambia: basta el tamaño
                    copiarSiCambio(f, d, c);
                    if (++nuevas >= MAX_FOTOS_POR_PASADA) break; // el resto en la próxima pasada
                }
            }
            escribirLeame(dest, c);
        } catch (Throwable t) {
            c.error = t.getClass().getSimpleName();
            Log.w(TAG, "respaldo", t);
        }
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putLong("cuando", System.currentTimeMillis())
                .putInt("copiados", c.copiados)
                .putString("error", c.error == null ? (c.fallidos > 0 ? c.fallidos + " sin copiar" : "") : c.error)
                .apply();
    }

    /** Carpeta por ruta: el NOMBRE de DATOSDEENTRADA (ruta cargada) o RUTA si no está. */
    private static String nombreRuta(File base) {
        File f = new File(base, "DATOSDEENTRADA/NOMBRE");
        try (InputStream in = new FileInputStream(f)) {
            byte[] b = new byte[64];
            int n = in.read(b);
            String s = n > 0 ? new String(b, 0, n, "ISO-8859-1").trim() : "";
            s = s.replaceAll("[^A-Za-z0-9_-]", "");
            if (!s.isEmpty()) return s.length() > 30 ? s.substring(0, 30) : s;
        } catch (IOException ignored) {
        }
        return "RUTA";
    }

    private static final class Contador {
        int copiados;
        int fallidos;
        String error;
    }

    /** Copia a un .tmp y renombra: en la SD nunca queda un archivo a medias con el nombre bueno. */
    private static void copiarSiCambio(File origen, File destino, Contador c) {
        if (destino.isFile() && destino.length() == origen.length() && destino.lastModified() >= origen.lastModified()) return;
        File dir = destino.getParentFile();
        if (dir != null && !dir.isDirectory() && !dir.mkdirs()) {
            c.fallidos++;
            return;
        }
        File tmp = new File(destino.getPath() + ".tmp");
        try (InputStream in = new FileInputStream(origen); OutputStream out = new FileOutputStream(tmp)) {
            byte[] buf = new byte[64 * 1024];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            out.flush();
            if (out instanceof FileOutputStream) ((FileOutputStream) out).getFD().sync();
        } catch (IOException e) {
            tmp.delete();
            c.fallidos++;
            return;
        }
        if (destino.exists() && !destino.delete()) {
            tmp.delete();
            c.fallidos++;
            return;
        }
        if (!tmp.renameTo(destino)) {
            tmp.delete();
            c.fallidos++;
            return;
        }
        destino.setLastModified(origen.lastModified());
        c.copiados++;
    }

    private static void escribirLeame(File dest, Contador c) {
        try {
            if (!dest.isDirectory()) dest.mkdirs();
            String t = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
            String texto = "Respaldo de la terminal EBSA\r\n"
                    + "Ultima copia: " + t + "\r\n"
                    + "Archivos copiados en esta pasada: " + c.copiados + (c.fallidos > 0 ? " (sin copiar: " + c.fallidos + ")" : "") + "\r\n"
                    + "DATOSDESALIDA: archivos .SDA de la ruta trabajada. FOTOGRAFIAS: fotos de las lecturas.\r\n"
                    + "Para seguir en otro equipo: cargar la misma ruta y reemplazar DATOSDESALIDA con estos archivos.\r\n";
            try (OutputStream o = new FileOutputStream(new File(dest, "LEAME.TXT"))) {
                o.write(texto.getBytes("ISO-8859-1"));
            }
        } catch (IOException ignored) {
        }
    }
}
