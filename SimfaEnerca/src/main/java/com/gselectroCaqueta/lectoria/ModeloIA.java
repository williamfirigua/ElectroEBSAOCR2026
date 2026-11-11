package com.gselectroCaqueta.lectoria;

import android.content.Context;
import android.util.Log;

import com.lecturaia.lector.Imagen;
import com.lecturaia.lector.JsonPlano;
import com.lecturaia.lector.LectorMedidores;
import com.lecturaia.lector.OrtMotor;
import com.lecturaia.lector.Resultado;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Modelo del lector IA (detector.onnx + lector.onnx + modelo.json), sin depender de la app.
 *
 * <pre>
 *   files/lector_ia/&lt;versión&gt;/detector.onnx, lector.onnx, modelo.json
 *   files/lector_ia/actual.txt          nombre de la carpeta en uso
 * </pre>
 *
 * - El APK trae {@code assets/lector_ia/modelo_<versión>.zip}: la primera vez se instala solo, sin red.
 * - Instalar = extraer a una carpeta temporal, verificar, renombrarla a la versión y cambiar
 *   {@code actual.txt} (escrito aparte y renombrado): nunca queda un modelo a medias.
 * - Nunca baja de versión (las versiones vAAAAMMDD_HHMM[_mNN] ordenan como texto).
 * - Si algo falla sigue el modelo anterior.
 * - {@link #instalarZip(Context, File, String)} queda para cuando el servidor entregue un modelo más nuevo.
 */
public final class ModeloIA {
    private static final String TAG = "ModeloIA";
    static final String CARPETA_ASSETS = "lector_ia";
    private static final String ACTUAL = "actual.txt";

    private static LectorMedidores lector;
    private static String versionCargada;
    private static boolean revisadoEmbebido;

    private ModeloIA() {
    }

    public static File raiz(Context ctx) {
        return new File(ctx.getFilesDir(), "lector_ia");
    }

    /** Instala el embebido (si hace falta) y carga el modelo en un hilo aparte; no bloquea a quien llama. */
    public static void prepararEnSegundoPlano(final Context ctx) {
        final Context app = ctx.getApplicationContext();
        Thread t = new Thread(() -> {
            try {
                obtener(app);
            } catch (Throwable e) {
                Log.w(TAG, "No se pudo preparar el modelo", e);
            }
        }, "modelo-ia");
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    /** El lector cargado (lo carga la primera vez), o null si no hay modelo instalado. */
    public static synchronized LectorMedidores obtener(Context ctx) throws Exception {
        if (lector != null) return lector;
        asegurarEmbebido(ctx);
        File dir = carpetaActual(ctx);
        if (dir == null) return null;
        File det = new File(dir, "detector.onnx"), lec = new File(dir, "lector.onnx");
        if (!det.isFile() || !lec.isFile()) return null;
        String version = dir.getName();
        double margen = 0;
        File meta = new File(dir, "modelo.json");
        if (meta.isFile()) {
            String json = new String(leer(meta), "UTF-8");
            Double m = JsonPlano.numero(json, "margen_x");
            if (m != null) margen = m;
            String v = JsonPlano.texto(json, "version");
            if (v != null && !v.isEmpty()) version = v;
        }
        lector = new LectorMedidores(new OrtMotor(leer(det), leer(lec), version), margen);
        versionCargada = version;
        return lector;
    }

    /** Lee un cuadro o foto. Null si no hay modelo. Sincronizado con {@link #liberar()}. */
    public static synchronized Resultado leer(Context ctx, Imagen img) throws Exception {
        LectorMedidores l = obtener(ctx);
        return l == null ? null : l.leer(img);
    }

    /** Versión instalada en disco (sin cargar el modelo), o null. */
    public static synchronized String versionInstalada(Context ctx) {
        if (versionCargada != null) return versionCargada;
        File dir = carpetaActual(ctx);
        return dir == null ? null : dir.getName();
    }

    /** Cierra las sesiones de ONNX Runtime (decenas de MB nativos). Se vuelven a cargar al leer. */
    public static synchronized void liberar() {
        if (lector != null) {
            try {
                lector.close();
            } catch (Exception e) {
                Log.w(TAG, "Cerrando el modelo", e);
            }
        }
        lector = null;
        versionCargada = null;
    }

    /**
     * Instala un modelo_*.zip bajado del servidor. Verifica el sha256 (si viene), nunca baja de versión
     * y, si algo falla, deja el modelo que había. Devuelve true si quedó instalada una versión nueva.
     */
    public static synchronized boolean instalarZip(Context ctx, File zip, String sha256) {
        try {
            if (sha256 != null && !sha256.trim().isEmpty() && !sha256.trim().equalsIgnoreCase(sha256De(zip))) {
                Log.w(TAG, "sha256 no coincide: " + zip.getName());
                return false;
            }
            String nombre = zip.getName();
            String porNombre = nombre.startsWith("modelo_") && nombre.endsWith(".zip")
                    ? nombre.substring("modelo_".length(), nombre.length() - ".zip".length()) : null;
            try (InputStream in = new FileInputStream(zip)) {
                return instalar(ctx, in, porNombre);
            }
        } catch (Exception e) {
            Log.w(TAG, "No se pudo instalar " + zip, e);
            return false;
        }
    }

    // ------------------------------------------------------------------ interno

    /** Una vez por proceso: instala el modelo del APK si no hay ninguno o si el del APK es más nuevo. */
    private static void asegurarEmbebido(Context ctx) {
        if (revisadoEmbebido) return;
        revisadoEmbebido = true;
        try {
            String zip = zipEmbebido(ctx);
            if (zip == null) return;
            String version = zip.substring("modelo_".length(), zip.length() - ".zip".length());
            if (!esMasNueva(version, versionInstalada(ctx))) return;
            try (InputStream in = ctx.getAssets().open(CARPETA_ASSETS + "/" + zip)) {
                instalar(ctx, in, version);
            }
        } catch (Exception e) {
            Log.w(TAG, "No se pudo instalar el modelo embebido", e);
        }
    }

    private static boolean instalar(Context ctx, InputStream zip, String versionPorNombre) throws IOException {
        File raiz = raiz(ctx);
        if (!raiz.isDirectory() && !raiz.mkdirs()) throw new IOException("No se pudo crear " + raiz);
        File tmp = new File(raiz, ".tmp_" + System.currentTimeMillis());
        try {
            if (!tmp.mkdirs()) throw new IOException("No se pudo crear " + tmp);
            extraer(zip, tmp);
            if (!new File(tmp, "detector.onnx").isFile() || !new File(tmp, "lector.onnx").isFile()) {
                throw new IOException("El zip no trae detector.onnx y lector.onnx");
            }
            String version = versionPorNombre;
            File meta = new File(tmp, "modelo.json");
            if (meta.isFile()) {
                String v = JsonPlano.texto(new String(leer(meta), "UTF-8"), "version");
                if (v != null && !v.isEmpty()) version = v;
            }
            if (version == null || !version.matches("v[0-9A-Za-z_]+")) throw new IOException("Versión inválida: " + version);
            if (!esMasNueva(version, versionInstalada(ctx))) return false;

            File destino = new File(raiz, version);
            if (destino.exists()) borrar(destino);   // instalación vieja incompleta de la misma versión
            if (!tmp.renameTo(destino)) throw new IOException("No se pudo renombrar " + tmp);

            File puntero = new File(raiz, ACTUAL + ".tmp");
            try (OutputStream o = new FileOutputStream(puntero)) {
                o.write(version.getBytes("UTF-8"));
                ((FileOutputStream) o).getFD().sync();
            }
            if (!puntero.renameTo(new File(raiz, ACTUAL))) throw new IOException("No se pudo activar " + version);
            liberar();
            limpiarViejas(raiz, version);
            Log.i(TAG, "Modelo instalado: " + version);
            return true;
        } finally {
            if (tmp.exists()) borrar(tmp);
        }
    }

    /** Solo los tres archivos esperados, sin carpetas: un nombre raro en el zip no escribe fuera. */
    private static void extraer(InputStream in, File destino) throws IOException {
        ZipInputStream z = new ZipInputStream(in);
        ZipEntry e;
        byte[] buf = new byte[64 * 1024];
        while ((e = z.getNextEntry()) != null) {
            String n = new File(e.getName()).getName();
            if (e.isDirectory() || !(n.equals("detector.onnx") || n.equals("lector.onnx") || n.equals("modelo.json"))) continue;
            try (FileOutputStream o = new FileOutputStream(new File(destino, n))) {
                int k;
                while ((k = z.read(buf)) > 0) o.write(buf, 0, k);
                o.getFD().sync();
            }
        }
    }

    /** Deja la versión en uso y la anterior más nueva (por si hay que volver); borra el resto. */
    private static void limpiarViejas(File raiz, String actual) {
        File[] hijos = raiz.listFiles();
        if (hijos == null) return;
        String anterior = null;
        for (File f : hijos) {
            String n = f.getName();
            if (f.isDirectory() && n.startsWith("v") && n.compareTo(actual) < 0 && (anterior == null || n.compareTo(anterior) > 0)) anterior = n;
        }
        for (File f : hijos) {
            String n = f.getName();
            if (f.isDirectory() && n.startsWith("v") && !n.equals(actual) && !n.equals(anterior)) borrar(f);
        }
    }

    private static File carpetaActual(Context ctx) {
        File raiz = raiz(ctx);
        File puntero = new File(raiz, ACTUAL);
        try {
            if (!puntero.isFile()) return null;
            String v = new String(leer(puntero), "UTF-8").trim();
            File dir = new File(raiz, v);
            return v.isEmpty() || !dir.isDirectory() ? null : dir;
        } catch (IOException e) {
            return null;
        }
    }

    private static String zipEmbebido(Context ctx) throws IOException {
        String[] nombres = ctx.getAssets().list(CARPETA_ASSETS);
        String mejor = null;
        if (nombres == null) return null;
        for (String n : nombres) {
            if (n.startsWith("modelo_") && n.endsWith(".zip") && (mejor == null || n.compareTo(mejor) > 0)) mejor = n;
        }
        return mejor;
    }

    public static boolean esMasNueva(String candidata, String instalada) {
        if (candidata == null || candidata.isEmpty()) return false;
        return instalada == null || candidata.compareTo(instalada) > 0;
    }

    private static String sha256De(File f) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] buf = new byte[64 * 1024];
        try (InputStream in = new FileInputStream(f)) {
            int k;
            while ((k = in.read(buf)) > 0) md.update(buf, 0, k);
        }
        StringBuilder s = new StringBuilder();
        for (byte b : md.digest()) s.append(String.format(Locale.ROOT, "%02x", b));
        return s.toString();
    }

    static byte[] leer(File f) throws IOException {
        byte[] b = new byte[(int) f.length()];
        try (InputStream in = new FileInputStream(f)) {
            int n = 0;
            while (n < b.length) {
                int k = in.read(b, n, b.length - n);
                if (k < 0) throw new IOException("Archivo incompleto: " + f);
                n += k;
            }
        }
        return b;
    }

    private static void borrar(File f) {
        File[] hijos = f.listFiles();
        if (hijos != null) for (File h : hijos) borrar(h);
        //noinspection ResultOfMethodCallIgnored
        f.delete();
    }
}
