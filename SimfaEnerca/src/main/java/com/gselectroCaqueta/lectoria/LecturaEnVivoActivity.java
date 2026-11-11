package com.gselectroCaqueta.lectoria;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;

import com.google.common.util.concurrent.ListenableFuture;
import com.gselectroCaqueta.accesoyseguridad.CamaraLecturaActivity;
import com.gselectroCaqueta.accesoyseguridad.R;
import com.gselectroCaqueta.accesoyseguridad.VariablesGlobales;
import com.gsutil.Utils;
import com.lecturaia.lector.Decodificador;
import com.lecturaia.lector.Estabilizador;
import com.lecturaia.lector.Imagen;
import com.lecturaia.lector.Resultado;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Lectura del medidor en vivo con el modelo propio (reemplaza la cámara OCR de ML Kit).
 *
 * <ol>
 *   <li><b>Leyendo:</b> cada cuadro pasa por el modelo (≈8 por segundo). Cuando el mismo valor sale en 3 cuadros
 *       seguidos ({@link Estabilizador}) se confirma, se toma la foto y se pasa a Confirmar.</li>
 *   <li><b>Confirmar:</b> la lectura entera (el decimal no cuenta) queda en el campo; el técnico acepta o corrige.</li>
 *   <li><b>No lee:</b> si en ~8 s no hay lectura, dice por qué y ofrece tomar la foto y digitar a mano.</li>
 * </ol>
 *
 * Entrada y salida iguales a {@link CamaraLecturaActivity} (mismos extras), así MenuDeLiquidacion no cambia:
 * la foto queda en {@code EXTRA_NOMBRE_IMAGEN} y la lectura en {@code EXTRA_LECTURA_OCR} (vacía = digitar a mano).
 * Si el modelo no carga, abre la cámara OCR vieja con el mismo pedido. Todo funciona sin internet.
 */
public class LecturaEnVivoActivity extends AppCompatActivity {

    public static final String EXTRA_NOMBRE_IMAGEN = CamaraLecturaActivity.EXTRA_NOMBRE_IMAGEN;
    public static final String EXTRA_LECTURA_OCR = CamaraLecturaActivity.EXTRA_LECTURA_OCR;
    /** Opcionales: encabezado y lectura anterior (para mostrar el consumo al confirmar). */
    public static final String EXTRA_TITULO = "extra_titulo";
    public static final String EXTRA_SUBTITULO = "extra_subtitulo";
    public static final String EXTRA_LECTURA_ANTERIOR = "extra_lectura_anterior";

    private static final String TAG = "LecturaEnVivo";
    private static final int PERMISO_CAMARA = 42;
    private static final long INTERVALO_MS = 120;          // ≈8 cuadros/s como máximo
    private static final long SIN_LECTURA_MS = 8000;       // tiempo antes de ofrecer digitar a mano
    private static final int CUADROS_PARA_CONFIRMAR = 3;
    private static final Size RESOLUCION = new Size(1080, 1440);
    private static final int OSCURO = 55, CUADROS_OSCUROS = 4;
    private static final long PAUSA_ENFOQUE_MS = 2000;

    private final ExecutorService analisis = Executors.newSingleThreadExecutor();
    private final ExecutorService trabajo = Executors.newSingleThreadExecutor();
    private final Estabilizador estabilizador = new Estabilizador(CUADROS_PARA_CONFIRMAR, 0.5, 8);

    private String nombreImagen = "";
    private String lecturaAnterior = "";

    // Pantalla
    private FrameLayout raiz;
    private PreviewView vista;
    private SuperposicionView superposicion;
    private TextView estado, tipoVisto, textoRacha;
    private LinearLayout panelLectura, filaDigitos, avisoNoLee, panelBotones;
    private TextView avisoTitulo, avisoTexto;
    private ProgressBar barraRacha;
    private Button botonLuz;
    private LinearLayout panelConfirmar;
    private ImageView fotoConfirmar;
    private TextView estadoFoto, ayudaLectura, txtAnterior, txtConsumo;
    private EditText campoLectura;
    private Button botonAceptar;

    // Cámara y lectura (las variables del análisis solo se tocan en el hilo de análisis)
    private androidx.camera.core.Camera camara;
    private ImageCapture captura;
    private volatile boolean pausado = true;   // hasta que cargue el modelo
    private volatile boolean capturando;
    private volatile boolean linterna, luzManual;
    private long ultimoAnalisis, inicioLectura, ultimoEnfoque;
    private int oscurosSeguidos, racha;
    private String ultimaClave;
    private volatile boolean avisoVisible;

    private String lecturaIA = "";
    private double confianzaIA;
    private String version = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        nombreImagen = texto(getIntent().getStringExtra(EXTRA_NOMBRE_IMAGEN));
        lecturaAnterior = texto(getIntent().getStringExtra(EXTRA_LECTURA_ANTERIOR)).replaceAll("[^0-9]", "");
        setContentView(construirPantalla(texto(getIntent().getStringExtra(EXTRA_TITULO)),
                texto(getIntent().getStringExtra(EXTRA_SUBTITULO))));

        estado.setText("Preparando el lector…");
        analisis.execute(() -> {
            boolean ok;
            try {
                ok = ModeloIA.obtener(getApplicationContext()) != null;
                version = texto(ModeloIA.versionInstalada(getApplicationContext()));
            } catch (Throwable e) {
                Log.e(TAG, "No cargó el modelo", e);
                ok = false;
            }
            final boolean listo = ok;
            runOnUiThread(() -> {
                if (isFinishing()) return;
                if (!listo) {
                    abrirCamaraVieja();
                    return;
                }
                reiniciarLectura();
            });
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            iniciarCamara();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISO_CAMARA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int codigo, @NonNull String[] permisos, @NonNull int[] resultados) {
        super.onRequestPermissionsResult(codigo, permisos, resultados);
        if (codigo == PERMISO_CAMARA && resultados.length > 0 && resultados[0] == PackageManager.PERMISSION_GRANTED) {
            iniciarCamara();
        } else {
            Toast.makeText(this, "Sin permiso de cámara.", Toast.LENGTH_LONG).show();
            cancelar();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        analisis.shutdown();
        trabajo.shutdown();
    }

    @Override
    public void onBackPressed() {
        if (panelConfirmar.getVisibility() == View.VISIBLE) {
            reiniciarLectura();
        } else {
            cancelar();
        }
    }

    /** Sin modelo: la cámara OCR de antes atiende el mismo pedido y su respuesta va directo a quien abrió esta. */
    private void abrirCamaraVieja() {
        log("IA sin modelo: se abre la camara OCR anterior");
        Intent i = new Intent(this, CamaraLecturaActivity.class);
        i.putExtra(CamaraLecturaActivity.EXTRA_NOMBRE_IMAGEN, nombreImagen);
        i.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        startActivity(i);
        finish();
    }

    // ------------------------------------------------------------------ cámara

    private void iniciarCamara() {
        final ListenableFuture<ProcessCameraProvider> futuro = ProcessCameraProvider.getInstance(this);
        futuro.addListener(() -> {
            try {
                ProcessCameraProvider proveedor = futuro.get();
                Preview previa = new Preview.Builder().setTargetResolution(RESOLUCION).build();
                previa.setSurfaceProvider(vista.getSurfaceProvider());
                ImageAnalysis analizador = new ImageAnalysis.Builder()
                        .setTargetResolution(RESOLUCION)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                analizador.setAnalyzer(analisis, this::analizar);
                captura = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();
                proveedor.unbindAll();
                camara = proveedor.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, previa, analizador, captura);
                botonLuz.setEnabled(camara.getCameraInfo().hasFlashUnit());
                vista.setOnTouchListener((v, e) -> {
                    if (e.getAction() == android.view.MotionEvent.ACTION_UP && camara != null) {
                        MeteringPoint p = vista.getMeteringPointFactory().createPoint(e.getX(), e.getY());
                        camara.getCameraControl().startFocusAndMetering(new FocusMeteringAction.Builder(p).build());
                        v.performClick();
                    }
                    return true;
                });
            } catch (Exception e) {
                Log.e(TAG, "Cámara", e);
                estado.setText("No se pudo abrir la cámara.");
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /** Hilo de análisis. Siempre cierra el cuadro. */
    private void analizar(ImageProxy cuadro) {
        try {
            long ahora = SystemClock.elapsedRealtime();
            if (pausado || capturando || ahora - ultimoAnalisis < INTERVALO_MS) return;
            ultimoAnalisis = ahora;
            Imagen img = YuvImagen.desde(cuadro);
            Resultado r = ModeloIA.leer(getApplicationContext(), img);
            if (r == null) return;
            Estabilizador.Valor confirmado = estabilizador.agregar(r, ahora);

            String clave = r.tieneLectura() && r.lectura.indexOf('?') < 0 ? r.lectura + "|" + r.decimales : null;
            racha = clave != null && clave.equals(ultimaClave) ? racha + 1 : (clave != null ? 1 : 0);
            ultimaClave = clave;

            int brillo = brillo(img);
            ajustarCamara(img, r, ahora, brillo);
            superposicion.mostrar(r.esquinas, img.ancho, img.alto, "",
                    confirmado != null ? SuperposicionView.VERDE : r.tieneLectura() ? SuperposicionView.AMARILLO : SuperposicionView.GRIS);
            mostrarCuadro(r);

            if (confirmado != null && !pausado) {
                pausado = true;
                lecturaIA = confirmado.entera();
                confianzaIA = confirmado.confianzaMax;
                runOnUiThread(() -> tomarFoto(true));
                return;
            }
            if (!avisoVisible && racha == 0 && ahora - inicioLectura > SIN_LECTURA_MS) {
                avisoVisible = true;
                final String[] motivo = motivoNoLee(r, brillo);
                runOnUiThread(() -> mostrarNoLee(motivo[0], motivo[1]));
            }
        } catch (Throwable e) {
            Log.e(TAG, "Análisis", e);
        } finally {
            cuadro.close();
        }
    }

    private void mostrarCuadro(final Resultado r) {
        runOnUiThread(() -> {
            if (pausado) return;
            barraRacha.setProgress(Math.min(racha, CUADROS_PARA_CONFIRMAR));
            textoRacha.setText(Math.min(racha, CUADROS_PARA_CONFIRMAR) + " de " + CUADROS_PARA_CONFIRMAR);
            if (!r.esMedidor() || r.esquinas == null) {
                estado.setText("Apunte al contador del medidor");
                return;
            }
            estado.setText(r.tieneLectura() ? "Leyendo… mantenga el celular quieto" : "Veo el medidor; acérquese al contador");
            tipoVisto.setText(r.tipo);
            if (r.tieneLectura()) {
                panelLectura.setVisibility(View.VISIBLE);
                pintarDigitos(r);
            }
        });
    }

    private void pintarDigitos(Resultado r) {
        filaDigitos.removeAllViews();
        boolean hayDecimal = false;
        for (Decodificador.Digito d : r.digitos) {
            TextView t = new TextView(this);
            t.setText(String.valueOf(d.valor));
            t.setGravity(Gravity.CENTER);
            t.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
            GradientDrawable fondo = new GradientDrawable();
            fondo.setCornerRadius(dp(6));
            if (d.decimal) {
                hayDecimal = true;
                fondo.setColor(Color.TRANSPARENT);
                fondo.setStroke(dp(2), Color.rgb(154, 163, 176), dp(5), dp(4));
                t.setTextColor(Color.rgb(154, 163, 176));
            } else {
                fondo.setColor(Color.WHITE);
                t.setTextColor(Color.BLACK);
            }
            t.setBackground(fondo);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(38), dp(50));
            lp.setMarginEnd(dp(5));
            filaDigitos.addView(t, lp);
        }
        if (hayDecimal) {
            TextView nota = textoView(12, color(R.color.lec_tecla_ayuda));
            nota.setText("decimal\nno cuenta");
            filaDigitos.addView(nota);
        }
    }

    /** Lo que haría un técnico con experiencia: luz si está oscuro, enfocar el contador si lo ve y no lo lee. */
    private void ajustarCamara(Imagen img, Resultado r, long ahora, int brillo) {
        final androidx.camera.core.Camera cam = camara;
        if (cam == null) return;
        if (!luzManual && !linterna && cam.getCameraInfo().hasFlashUnit()) {
            oscurosSeguidos = brillo < OSCURO ? oscurosSeguidos + 1 : 0;
            if (oscurosSeguidos >= CUADROS_OSCUROS) ponerLuz(true);
        }
        final double[][] e = r.esquinas;
        boolean leeMal = !r.tieneLectura() || r.confianza < 0.8;
        if (e == null || !r.esMedidor() || !leeMal || ahora - ultimoEnfoque < PAUSA_ENFOQUE_MS) return;
        ultimoEnfoque = ahora;
        double cx = 0, cy = 0;
        for (double[] p : e) {
            cx += p[0] / 4;
            cy += p[1] / 4;
        }
        final double fx = cx, fy = cy;
        final int ancho = img.ancho, alto = img.alto;
        runOnUiThread(() -> {
            float s = Math.min((float) vista.getWidth() / ancho, (float) vista.getHeight() / alto);
            float ox = (vista.getWidth() - ancho * s) / 2f, oy = (vista.getHeight() - alto * s) / 2f;
            MeteringPoint punto = vista.getMeteringPointFactory().createPoint(ox + (float) fx * ancho * s, oy + (float) fy * alto * s);
            cam.getCameraControl().startFocusAndMetering(new FocusMeteringAction.Builder(punto).build());
        });
    }

    private void ponerLuz(final boolean encendida) {
        if (camara == null) return;
        linterna = encendida;
        camara.getCameraControl().enableTorch(encendida);
        runOnUiThread(() -> botonLuz.setText(encendida ? "Apagar luz" : "Linterna"));
    }

    private static String[] motivoNoLee(Resultado r, int brillo) {
        if (brillo < OSCURO) return new String[]{"Está muy oscuro", "Prenda la linterna o acérquese con más luz."};
        if (r == null || !r.esMedidor() || r.esquinas == null) {
            return new String[]{"No encuentro el medidor", "Apunte la cámara al contador, a un palmo de distancia."};
        }
        if ("ilegible".equals(r.tipo)) {
            return new String[]{"No logro leer el contador", "Hay reflejo o el vidrio está sucio. Incline un poco el celular o límpielo."};
        }
        return new String[]{"No logro leer el contador", "Acérquese un poco, enfoque tocando la pantalla y manténgalo quieto."};
    }

    /** Brillo medio 0..255 con una muestra de ~2.000 píxeles. */
    static int brillo(Imagen img) {
        byte[] d = img.datos;
        int px = img.ancho * img.alto, paso = Math.max(1, px / 2000), n = 0;
        long suma = 0;
        for (int i = 0; i < px; i += paso, n++) {
            int k = i * 3;
            suma += (d[k] & 0xFF) + (d[k + 1] & 0xFF) + (d[k + 2] & 0xFF);
        }
        return n == 0 ? 255 : (int) (suma / (3L * n));
    }

    // ------------------------------------------------------------------ foto

    /** {@code conLectura} = la IA confirmó (pasa a Confirmar); false = foto para digitar a mano. */
    private void tomarFoto(final boolean conLectura) {
        if (captura == null || capturando) return;
        if (nombreImagen.isEmpty()) {
            Toast.makeText(this, "No se indicó dónde guardar la foto.", Toast.LENGTH_LONG).show();
            return;
        }
        capturando = true;
        pausado = true;
        estado.setText("Tomando la foto…");
        final File tmp = new File(getCacheDir(), "lectura_ia_" + System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions op = new ImageCapture.OutputFileOptions.Builder(tmp).build();
        captura.takePicture(op, trabajo, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults r) {
                final Bitmap miniatura = guardarFoto(tmp);
                //noinspection ResultOfMethodCallIgnored
                tmp.delete();
                runOnUiThread(() -> {
                    capturando = false;
                    if (miniatura == null) {
                        Toast.makeText(LecturaEnVivoActivity.this, "No se pudo guardar la foto. Intente de nuevo.", Toast.LENGTH_LONG).show();
                        reiniciarLectura();
                    } else if (conLectura) {
                        mostrarConfirmar(miniatura);
                    } else {
                        log("IA " + version + " | foto para digitar a mano");
                        devolver("");
                    }
                });
            }

            @Override
            public void onError(@NonNull ImageCaptureException e) {
                Log.e(TAG, "takePicture", e);
                runOnUiThread(() -> {
                    capturando = false;
                    Toast.makeText(LecturaEnVivoActivity.this, "Error tomando la foto. Intente de nuevo.", Toast.LENGTH_LONG).show();
                    reiniciarLectura();
                });
            }
        });
    }

    /**
     * La foto queda en la ruta que pidió la liquidación, derecha (sin depender del EXIF) y en JPEG 85, como la
     * dejaba la cámara OCR. Devuelve una miniatura para la pantalla, o null si falló.
     */
    private Bitmap guardarFoto(File tmp) {
        try {
            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(tmp.getAbsolutePath(), op);
            int muestreo = 1;
            while (Math.max(op.outWidth, op.outHeight) / (muestreo * 2) >= 2560) muestreo *= 2;
            op = new BitmapFactory.Options();
            op.inSampleSize = muestreo;
            Bitmap bmp = BitmapFactory.decodeFile(tmp.getAbsolutePath(), op);
            if (bmp == null) return null;
            int o = new ExifInterface(tmp.getAbsolutePath()).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            float grados = o == ExifInterface.ORIENTATION_ROTATE_90 ? 90 : o == ExifInterface.ORIENTATION_ROTATE_180 ? 180
                    : o == ExifInterface.ORIENTATION_ROTATE_270 ? 270 : 0;
            if (grados != 0) {
                Matrix m = new Matrix();
                m.postRotate(grados);
                Bitmap r = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);
                if (r != bmp) bmp.recycle();
                bmp = r;
            }
            File destino = new File(nombreImagen);
            if (destino.getParentFile() != null) //noinspection ResultOfMethodCallIgnored
                destino.getParentFile().mkdirs();
            try (FileOutputStream fos = new FileOutputStream(destino)) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 85, fos);
                fos.getFD().sync();
            }
            int ancho = 720, alto = Math.max(1, Math.round(720f * bmp.getHeight() / bmp.getWidth()));
            Bitmap mini = Bitmap.createScaledBitmap(bmp, ancho, alto, true);
            if (mini != bmp) bmp.recycle();
            return mini;
        } catch (Exception e) {
            Log.e(TAG, "guardarFoto", e);
            return null;
        }
    }

    // ------------------------------------------------------------------ estados

    private void reiniciarLectura() {
        panelConfirmar.setVisibility(View.GONE);
        avisoNoLee.setVisibility(View.GONE);
        panelBotones.setVisibility(View.VISIBLE);
        panelLectura.setVisibility(View.GONE);
        estado.setText("Apunte al contador del medidor");
        barraRacha.setProgress(0);
        textoRacha.setText("0 de " + CUADROS_PARA_CONFIRMAR);
        analisis.execute(() -> {             // en el hilo de análisis: sin carreras con los cuadros
            estabilizador.limpiar();
            racha = 0;
            ultimaClave = null;
            inicioLectura = SystemClock.elapsedRealtime();
            avisoVisible = false;
            pausado = false;
        });
    }

    private void mostrarNoLee(String titulo, String texto) {
        if (pausado) return;
        avisoTitulo.setText(titulo);
        avisoTexto.setText(texto);
        avisoNoLee.setVisibility(View.VISIBLE);
    }

    private void mostrarConfirmar(Bitmap miniatura) {
        fotoConfirmar.setImageBitmap(miniatura);
        campoLectura.setText(lecturaIA);
        campoLectura.setSelection(campoLectura.getText().length());
        ayudaLectura.setText(confianzaIA >= 0.98
                ? "Leída por la IA con confianza alta. Si no es la que ve en el medidor, corríjala aquí."
                : "Leída por la IA. Revísela contra el medidor antes de aceptar.");
        avisoNoLee.setVisibility(View.GONE);
        panelConfirmar.setVisibility(View.VISIBLE);
        actualizarConsumo();
    }

    private void actualizarConsumo() {
        String l = campoLectura.getText().toString().trim();
        botonAceptar.setText(l.isEmpty() ? "Aceptar" : "Aceptar " + l);
        if (lecturaAnterior.isEmpty()) {
            txtAnterior.setVisibility(View.GONE);
            txtConsumo.setVisibility(View.GONE);
            return;
        }
        txtAnterior.setText("Anterior " + lecturaAnterior);
        String consumo = "—";
        try {
            if (!l.isEmpty()) {
                long c = Long.parseLong(l) - Long.parseLong(lecturaAnterior);
                consumo = c >= 0 ? String.format(Locale.ROOT, "%d kWh", c) : "menor que la anterior";
            }
        } catch (NumberFormatException ignored) {
            // lectura muy larga: sin consumo
        }
        txtConsumo.setText("Consumo " + consumo);
    }

    private void aceptar() {
        String l = campoLectura.getText().toString().trim();
        if (!l.matches("^[0-9]+$")) {
            Toast.makeText(this, "Digite la lectura (solo números).", Toast.LENGTH_SHORT).show();
            return;
        }
        log(String.format(Locale.ROOT, "IA %s | lectura IA %s | confianza %.4f | %s %s", version, lecturaIA, confianzaIA,
                l.equals(lecturaIA) ? "aceptada" : "corregida a", l.equals(lecturaIA) ? "" : l));
        devolver(l);
    }

    private void devolver(String lectura) {
        Intent res = new Intent();
        res.putExtra(EXTRA_LECTURA_OCR, lectura);
        res.putExtra(EXTRA_NOMBRE_IMAGEN, nombreImagen);
        setResult(RESULT_OK, res);
        finish();
    }

    private void cancelar() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private void log(String msg) {
        try {
            String dir = VariablesGlobales.directorioactual;
            if (dir == null || dir.isEmpty()) return;
            new Utils().Log(new File(dir + "/DATOSDESALIDA/LOGEVENTOS.LOG"), "[LecturaEnVivo] " + msg);
        } catch (Throwable ignored) {
            // el log nunca detiene la lectura
        }
    }

    // ------------------------------------------------------------------ pantalla (sin XML)

    private View construirPantalla(String titulo, String subtitulo) {
        raiz = new FrameLayout(this);
        raiz.setBackgroundColor(Color.BLACK);

        vista = new PreviewView(this);
        vista.setScaleType(PreviewView.ScaleType.FIT_CENTER);   // así el cuadro dibujado coincide con el contador
        superposicion = new SuperposicionView(this);
        raiz.addView(vista, lleno());
        raiz.addView(superposicion, lleno());

        // Encabezado
        LinearLayout encabezado = new LinearLayout(this);
        encabezado.setOrientation(LinearLayout.VERTICAL);
        encabezado.setBackgroundColor(color(R.color.lec_banner));
        encabezado.setPadding(dp(16), dp(12), dp(16), dp(12));
        TextView t1 = textoView(18, color(R.color.lec_banner_texto));
        t1.setTypeface(Typeface.DEFAULT_BOLD);
        t1.setText(titulo.isEmpty() ? "Lectura del medidor" : titulo);
        encabezado.addView(t1);
        if (!subtitulo.isEmpty()) {
            TextView t2 = textoView(14, color(R.color.lec_tecla_ayuda));
            t2.setText(subtitulo);
            encabezado.addView(t2);
        }

        LinearLayout arriba = new LinearLayout(this);
        arriba.setOrientation(LinearLayout.VERTICAL);
        arriba.addView(encabezado);
        estado = textoView(16, Color.WHITE);
        estado.setGravity(Gravity.CENTER);
        estado.setPadding(dp(16), dp(8), dp(16), dp(8));
        estado.setBackground(redondeado(Color.argb(190, 0, 0, 0), 22));
        LinearLayout.LayoutParams lpEstado = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpEstado.gravity = Gravity.CENTER_HORIZONTAL;
        lpEstado.topMargin = dp(12);
        arriba.addView(estado, lpEstado);

        // Aviso "no lee"
        avisoNoLee = new LinearLayout(this);
        avisoNoLee.setOrientation(LinearLayout.VERTICAL);
        avisoNoLee.setBackground(redondeado(color(R.color.lec_aviso_cont), 14));
        avisoNoLee.setPadding(dp(16), dp(14), dp(16), dp(14));
        avisoTitulo = textoView(18, color(R.color.lec_aviso_texto));
        avisoTitulo.setTypeface(Typeface.DEFAULT_BOLD);
        avisoTexto = textoView(15, color(R.color.lec_aviso_texto));
        avisoNoLee.addView(avisoTitulo);
        avisoNoLee.addView(avisoTexto);
        LinearLayout filaAviso = new LinearLayout(this);
        filaAviso.setPadding(0, dp(10), 0, 0);
        Button digitar = boton("Tomar foto y digitar a mano", Color.BLACK, Color.WHITE, false);
        digitar.setOnClickListener(v -> tomarFoto(false));
        Button seguir = boton("Seguir intentando", color(R.color.lec_aviso_texto), Color.TRANSPARENT, true);
        seguir.setOnClickListener(v -> reiniciarLectura());
        filaAviso.addView(digitar, new LinearLayout.LayoutParams(0, dp(52), 1.3f));
        LinearLayout.LayoutParams lpSeguir = new LinearLayout.LayoutParams(0, dp(52), 1f);
        lpSeguir.setMarginStart(dp(8));
        filaAviso.addView(seguir, lpSeguir);
        avisoNoLee.addView(filaAviso);
        avisoNoLee.setVisibility(View.GONE);
        LinearLayout.LayoutParams lpAviso = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpAviso.setMargins(dp(16), dp(12), dp(16), 0);
        arriba.addView(avisoNoLee, lpAviso);
        raiz.addView(arriba, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP));

        // Abajo: lo que ve la IA + botones
        LinearLayout abajo = new LinearLayout(this);
        abajo.setOrientation(LinearLayout.VERTICAL);

        panelLectura = new LinearLayout(this);
        panelLectura.setOrientation(LinearLayout.VERTICAL);
        panelLectura.setBackground(redondeado(Color.argb(200, 0, 0, 0), 16));
        panelLectura.setPadding(dp(16), dp(12), dp(16), dp(12));
        LinearLayout filaTitulo = new LinearLayout(this);
        TextView laIaVe = textoView(15, color(R.color.lec_tecla_ayuda));
        laIaVe.setText("La IA ve");
        tipoVisto = textoView(14, color(R.color.lec_tecla_ayuda));
        tipoVisto.setGravity(Gravity.END);
        filaTitulo.addView(laIaVe, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        filaTitulo.addView(tipoVisto);
        panelLectura.addView(filaTitulo);
        filaDigitos = new LinearLayout(this);
        filaDigitos.setGravity(Gravity.CENTER_VERTICAL);
        filaDigitos.setPadding(0, dp(8), 0, dp(8));
        panelLectura.addView(filaDigitos);
        LinearLayout filaRacha = new LinearLayout(this);
        filaRacha.setGravity(Gravity.CENTER_VERTICAL);
        barraRacha = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        barraRacha.setMax(CUADROS_PARA_CONFIRMAR);
        barraRacha.setProgressTintList(android.content.res.ColorStateList.valueOf(SuperposicionView.VERDE));
        textoRacha = textoView(15, Color.WHITE);
        textoRacha.setTypeface(Typeface.DEFAULT_BOLD);
        textoRacha.setPadding(dp(10), 0, 0, 0);
        filaRacha.addView(barraRacha, new LinearLayout.LayoutParams(0, dp(10), 1f));
        filaRacha.addView(textoRacha);
        panelLectura.addView(filaRacha);
        panelLectura.setVisibility(View.GONE);
        LinearLayout.LayoutParams lpLectura = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpLectura.setMargins(dp(16), 0, dp(16), dp(12));
        abajo.addView(panelLectura, lpLectura);

        panelBotones = new LinearLayout(this);
        panelBotones.setBackgroundColor(color(R.color.lec_banner));
        panelBotones.setPadding(dp(16), dp(16), dp(16), dp(24));
        botonLuz = boton("Linterna", Color.WHITE, Color.TRANSPARENT, true);
        botonLuz.setEnabled(false);
        botonLuz.setOnClickListener(v -> {
            luzManual = true;
            ponerLuz(!linterna);
        });
        Button fotoDigitar = boton("Foto y digitar", Color.WHITE, Color.TRANSPARENT, true);
        fotoDigitar.setOnClickListener(v -> tomarFoto(false));
        panelBotones.addView(botonLuz, new LinearLayout.LayoutParams(0, dp(56), 1f));
        LinearLayout.LayoutParams lpFoto = new LinearLayout.LayoutParams(0, dp(56), 1f);
        lpFoto.setMarginStart(dp(12));
        panelBotones.addView(fotoDigitar, lpFoto);
        abajo.addView(panelBotones);
        raiz.addView(abajo, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM));

        raiz.addView(construirConfirmar(titulo, subtitulo), lleno());
        return raiz;
    }

    private View construirConfirmar(String titulo, String subtitulo) {
        panelConfirmar = new LinearLayout(this);
        panelConfirmar.setOrientation(LinearLayout.VERTICAL);
        panelConfirmar.setBackgroundColor(color(R.color.lec_superficie));
        panelConfirmar.setClickable(true);   // no deja pasar toques a la cámara

        LinearLayout encabezado = new LinearLayout(this);
        encabezado.setOrientation(LinearLayout.VERTICAL);
        encabezado.setBackgroundColor(color(R.color.lec_banner));
        encabezado.setPadding(dp(16), dp(12), dp(16), dp(12));
        TextView t1 = textoView(18, color(R.color.lec_banner_texto));
        t1.setTypeface(Typeface.DEFAULT_BOLD);
        t1.setText(titulo.isEmpty() ? "Confirmar lectura" : titulo);
        encabezado.addView(t1);
        if (!subtitulo.isEmpty()) {
            TextView t2 = textoView(14, color(R.color.lec_tecla_ayuda));
            t2.setText(subtitulo);
            encabezado.addView(t2);
        }
        panelConfirmar.addView(encabezado);

        android.widget.ScrollView scroll = new android.widget.ScrollView(this);
        LinearLayout cuerpo = new LinearLayout(this);
        cuerpo.setOrientation(LinearLayout.VERTICAL);
        cuerpo.setPadding(dp(16), dp(16), dp(16), dp(16));

        fotoConfirmar = new ImageView(this);
        fotoConfirmar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        fotoConfirmar.setBackgroundColor(Color.rgb(59, 65, 72));
        fotoConfirmar.setContentDescription("Foto tomada del medidor");
        cuerpo.addView(fotoConfirmar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(200)));

        estadoFoto = textoView(15, color(R.color.lec_ok));
        estadoFoto.setTypeface(Typeface.DEFAULT_BOLD);
        estadoFoto.setText("✓ Foto guardada · la IA confirmó la lectura");
        estadoFoto.setPadding(0, dp(12), 0, dp(12));
        cuerpo.addView(estadoFoto);

        LinearLayout caja = new LinearLayout(this);
        caja.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable fondoCaja = redondeado(color(R.color.lec_primario_cont), 14);
        fondoCaja.setStroke(dp(2), color(R.color.lec_primario));
        caja.setBackground(fondoCaja);
        caja.setPadding(dp(16), dp(14), dp(16), dp(14));
        TextView etiqueta = textoView(15, color(R.color.lec_texto2));
        etiqueta.setTypeface(Typeface.DEFAULT_BOLD);
        etiqueta.setText("Lectura actual");
        caja.addView(etiqueta);
        campoLectura = new EditText(this);
        campoLectura.setInputType(InputType.TYPE_CLASS_NUMBER);
        campoLectura.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
        campoLectura.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        campoLectura.setTextSize(TypedValue.COMPLEX_UNIT_SP, 38);
        campoLectura.setLetterSpacing(0.15f);
        campoLectura.setGravity(Gravity.CENTER);
        campoLectura.setTextColor(Color.BLACK);
        campoLectura.setBackground(redondeado(Color.WHITE, 10));
        campoLectura.setContentDescription("Lectura actual");
        campoLectura.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable s) { actualizarConsumo(); }
        });
        LinearLayout.LayoutParams lpCampo = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(68));
        lpCampo.setMargins(0, dp(6), 0, dp(6));
        caja.addView(campoLectura, lpCampo);
        ayudaLectura = textoView(14, color(R.color.lec_texto2));
        caja.addView(ayudaLectura);
        cuerpo.addView(caja);

        LinearLayout filaConsumo = new LinearLayout(this);
        filaConsumo.setPadding(dp(4), dp(12), dp(4), 0);
        txtAnterior = textoView(16, color(R.color.lec_texto2));
        txtConsumo = textoView(16, color(R.color.lec_texto2));
        txtConsumo.setGravity(Gravity.END);
        filaConsumo.addView(txtAnterior, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        filaConsumo.addView(txtConsumo, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        cuerpo.addView(filaConsumo);
        scroll.addView(cuerpo);
        panelConfirmar.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        View linea = new View(this);
        linea.setBackgroundColor(color(R.color.lec_divisor));
        panelConfirmar.addView(linea, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));

        LinearLayout acciones = new LinearLayout(this);
        acciones.setOrientation(LinearLayout.VERTICAL);
        acciones.setPadding(dp(16), dp(12), dp(16), dp(24));
        botonAceptar = boton("Aceptar", Color.WHITE, color(R.color.lec_ok), false);
        botonAceptar.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        botonAceptar.setOnClickListener(v -> aceptar());
        acciones.addView(botonAceptar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(60)));
        LinearLayout fila = new LinearLayout(this);
        fila.setPadding(0, dp(10), 0, 0);
        Button otraVez = boton("Leer otra vez", Color.BLACK, Color.WHITE, true);
        otraVez.setOnClickListener(v -> reiniciarLectura());
        Button corregir = boton("Corregir", Color.BLACK, Color.WHITE, true);
        corregir.setOnClickListener(v -> {
            campoLectura.requestFocus();
            campoLectura.selectAll();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(campoLectura, InputMethodManager.SHOW_IMPLICIT);
        });
        fila.addView(otraVez, new LinearLayout.LayoutParams(0, dp(52), 1f));
        LinearLayout.LayoutParams lpCorregir = new LinearLayout.LayoutParams(0, dp(52), 1f);
        lpCorregir.setMarginStart(dp(10));
        fila.addView(corregir, lpCorregir);
        acciones.addView(fila);
        panelConfirmar.addView(acciones);

        panelConfirmar.setVisibility(View.GONE);
        return panelConfirmar;
    }

    // ------------------------------------------------------------------ utilidades de pantalla

    private Button boton(String texto, int colorTexto, int fondo, boolean contorno) {
        Button b = new Button(this);
        b.setText(texto);
        b.setAllCaps(false);
        b.setTextColor(colorTexto);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setMinHeight(dp(48));
        GradientDrawable g = redondeado(fondo, 12);
        if (contorno) g.setStroke(dp(2), colorTexto == Color.WHITE ? color(R.color.lec_tecla_ayuda) : color(R.color.lec_contorno));
        b.setBackground(g);
        b.setStateListAnimator(null);
        return b;
    }

    private GradientDrawable redondeado(int color, int radioDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radioDp));
        return g;
    }

    private TextView textoView(float sp, int color) {
        TextView t = new TextView(this);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setTextColor(color);
        return t;
    }

    private static FrameLayout.LayoutParams lleno() {
        return new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    private int color(int id) {
        return ContextCompat.getColor(this, id);
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private static String texto(String s) {
        return s == null ? "" : s.trim();
    }
}
