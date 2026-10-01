package com.gselectroCaqueta.accesoyseguridad;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CamaraLecturaActivity v4
 *
 * Recuadro ARRASTRABLE + REDIMENSIONABLE:
 *  - El operario arrastra el recuadro sobre la pantalla del medidor
 *  - Puede ajustar el alto tirando de la esquina inferior-derecha
 *  - El recuadro mantiene su posición al repetir foto
 *  - OCR solo analiza la región del recuadro
 */
public class CamaraLecturaActivity extends AppCompatActivity {

    public static final String EXTRA_NOMBRE_IMAGEN = "extra_nombre_imagen";
    public static final String EXTRA_LECTURA_OCR   = "extra_lectura_ocr";
    private static final String TAG = "CamaraLecturaActivity";

    private String  nombreImagen = "";
    private Uri     imagenUri    = null;
    private boolean capturando   = false;

    private PreviewView      previewView;
    private ImageCapture     imageCapture;
    private ExecutorService  cameraExecutor;
    private androidx.camera.core.Camera camera;       // referencia para controlar flash
    private boolean          flashEncendido = false;  // estado del toggle

    private FrameLayout      contenedor;
    private FrameLayout      panelCamara;
    private ScrollView       scrollResultado;
    private ImageView        imgResultado;
    private TextView         lblEstadoOcr;
    private EditText         txtLecturaOcr;
    private Button           btnConfirmar;
    private RecuadroGuiaView recuadroGuia;
    private android.widget.ProgressBar progressOcr;

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        nombreImagen = getIntent().getStringExtra(EXTRA_NOMBRE_IMAGEN);
        if (nombreImagen == null) nombreImagen = "";

        cameraExecutor = Executors.newSingleThreadExecutor();

        contenedor = new FrameLayout(this);
        contenedor.setBackgroundColor(Color.BLACK);

        buildPanelCamara();
        buildPanelResultado();

        setContentView(contenedor);
        iniciarCamara();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) cameraExecutor.shutdown();
    }

    @Override
    public void onBackPressed() { cancelar(); }

    // ─────────────────────────────────────────────────────────────────────────
    //  Panel 1 — cámara
    // ─────────────────────────────────────────────────────────────────────────

    private void buildPanelCamara() {
        panelCamara = new FrameLayout(this);
        panelCamara.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        previewView = new PreviewView(this);
        previewView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        previewView.setScaleType(PreviewView.ScaleType.FILL_CENTER);

        recuadroGuia = new RecuadroGuiaView(this);
        recuadroGuia.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Barra inferior
        LinearLayout barra = new LinearLayout(this);
        barra.setOrientation(LinearLayout.VERTICAL);
        barra.setGravity(Gravity.CENTER);
        barra.setBackgroundColor(Color.argb(210, 0, 0, 0));
        barra.setPadding(32, 16, 32, 28);
        FrameLayout.LayoutParams barraLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        barraLp.gravity = Gravity.BOTTOM;
        barra.setLayoutParams(barraLp);

        TextView hint = new TextView(this);
        hint.setText("Mueva el recuadro sobre la pantalla del medidor\n"
                + "Tire de la esquina \u25E2 para ajustar el alto");
        hint.setTextSize(12f);
        hint.setTextColor(Color.parseColor("#FFEB3B"));
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0, 0, 0, 12);

        Button btnCap = new Button(this);
        btnCap.setText("CAPTURAR LECTURA");
        btnCap.setTextSize(16f);
        btnCap.setTextColor(Color.WHITE);
        btnCap.setBackgroundColor(Color.parseColor("#E65100"));
        LinearLayout.LayoutParams lpCap = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpCap.setMargins(0, 0, 0, 8);
        btnCap.setLayoutParams(lpCap);
        btnCap.setOnClickListener(v -> tomarFoto());

        Button btnCancel = new Button(this);
        btnCancel.setText("CANCELAR");
        btnCancel.setTextSize(13f);
        btnCancel.setTextColor(Color.parseColor("#AAAAAA"));
        btnCancel.setBackgroundColor(Color.TRANSPARENT);
        btnCancel.setOnClickListener(v -> cancelar());

        barra.addView(hint);

        // Fila con botón flash a la izquierda
        LinearLayout filaFlash = new LinearLayout(this);
        filaFlash.setOrientation(LinearLayout.HORIZONTAL);
        filaFlash.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lpFila = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpFila.setMargins(0, 0, 0, 8);
        filaFlash.setLayoutParams(lpFila);

        Button btnFlash = new Button(this);
        btnFlash.setText("⚡ FLASH OFF");
        btnFlash.setTextSize(13f);
        btnFlash.setTextColor(Color.WHITE);
        btnFlash.setBackgroundColor(Color.parseColor("#424242"));
        LinearLayout.LayoutParams lpFlash = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnFlash.setLayoutParams(lpFlash);
        btnFlash.setOnClickListener(v -> {
            flashEncendido = !flashEncendido;
            if (camera != null) {
                camera.getCameraControl().enableTorch(flashEncendido);
            }
            btnFlash.setText(flashEncendido ? "⚡ FLASH ON" : "⚡ FLASH OFF");
            btnFlash.setBackgroundColor(flashEncendido
                    ? Color.parseColor("#F9A825")  // amarillo cuando encendido
                    : Color.parseColor("#424242")); // gris cuando apagado
        });

        filaFlash.addView(btnFlash);
        barra.addView(filaFlash);
        barra.addView(btnCap);
        barra.addView(btnCancel);

        panelCamara.addView(previewView);
        panelCamara.addView(recuadroGuia);
        panelCamara.addView(barra);

        contenedor.addView(panelCamara);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Panel 2 — resultado
    // ─────────────────────────────────────────────────────────────────────────

    private void buildPanelResultado() {
        scrollResultado = new ScrollView(this);
        scrollResultado.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        scrollResultado.setBackgroundColor(Color.parseColor("#1A1A2E"));
        scrollResultado.setVisibility(View.GONE);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setPadding(32, 40, 32, 40);

        imgResultado = new ImageView(this);
        LinearLayout.LayoutParams imgLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0);
        imgLp.weight = 1;
        imgLp.setMargins(0, 0, 0, 24);
        imgResultado.setLayoutParams(imgLp);
        imgResultado.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imgResultado.setAdjustViewBounds(true);

        lblEstadoOcr = new TextView(this);
        lblEstadoOcr.setText("Analizando imagen...");
        lblEstadoOcr.setTextSize(14f);
        lblEstadoOcr.setTextColor(Color.parseColor("#4CAF50"));
        lblEstadoOcr.setGravity(Gravity.CENTER);
        lblEstadoOcr.setPadding(0, 0, 0, 8);

        progressOcr = new android.widget.ProgressBar(this);
        progressOcr.setIndeterminate(true);
        LinearLayout.LayoutParams lpProg = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpProg.gravity = Gravity.CENTER_HORIZONTAL;
        lpProg.setMargins(0, 0, 0, 16);
        progressOcr.setLayoutParams(lpProg);
        progressOcr.setVisibility(View.GONE);

        TextView titulo = new TextView(this);
        titulo.setText("Lectura extraída:");
        titulo.setTextSize(14f);
        titulo.setTextColor(Color.parseColor("#AAAAAA"));
        titulo.setPadding(0, 0, 0, 4);

        txtLecturaOcr = new EditText(this);
        txtLecturaOcr.setInputType(InputType.TYPE_CLASS_NUMBER);
        txtLecturaOcr.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        txtLecturaOcr.setTextSize(36f);
        txtLecturaOcr.setTextColor(Color.WHITE);
        txtLecturaOcr.setBackgroundColor(Color.argb(80, 255, 255, 255));
        txtLecturaOcr.setGravity(Gravity.CENTER);
        txtLecturaOcr.setPadding(24, 20, 24, 20);
        LinearLayout.LayoutParams lpEdit = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpEdit.setMargins(0, 0, 0, 32);
        txtLecturaOcr.setLayoutParams(lpEdit);

        btnConfirmar = new Button(this);
        btnConfirmar.setText("CONFIRMAR LECTURA");
        btnConfirmar.setTextSize(16f);
        btnConfirmar.setTextColor(Color.WHITE);
        btnConfirmar.setBackgroundColor(Color.parseColor("#388E3C"));
        LinearLayout.LayoutParams lpConf = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpConf.setMargins(0, 0, 0, 12);
        btnConfirmar.setLayoutParams(lpConf);
        btnConfirmar.setOnClickListener(v -> confirmarLectura());

        Button btnRep = new Button(this);
        btnRep.setText("REPETIR FOTO");
        btnRep.setTextSize(14f);
        btnRep.setTextColor(Color.WHITE);
        btnRep.setBackgroundColor(Color.parseColor("#1565C0"));
        LinearLayout.LayoutParams lpRep = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpRep.setMargins(0, 0, 0, 12);
        btnRep.setLayoutParams(lpRep);
        btnRep.setOnClickListener(v -> volverACamara());

        Button btnCan = new Button(this);
        btnCan.setText("CANCELAR");
        btnCan.setTextSize(13f);
        btnCan.setTextColor(Color.parseColor("#AAAAAA"));
        btnCan.setBackgroundColor(Color.parseColor("#424242"));
        btnCan.setOnClickListener(v -> cancelar());

        layout.addView(imgResultado);
        layout.addView(lblEstadoOcr);
        layout.addView(progressOcr);
        layout.addView(titulo);
        layout.addView(txtLecturaOcr);
        layout.addView(btnConfirmar);
        layout.addView(btnRep);
        layout.addView(btnCan);

        scrollResultado.addView(layout);
        contenedor.addView(scrollResultado);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  CameraX
    // ─────────────────────────────────────────────────────────────────────────

    private void iniciarCamara() {
        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();
                provider.unbindAll();
                camera = provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA,
                        preview, imageCapture);
            } catch (Exception e) {
                Log.e(TAG, "iniciarCamara: " + e.getMessage());
                Toast.makeText(this, "Error iniciando cámara.", Toast.LENGTH_LONG).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void tomarFoto() {
        if (imageCapture == null || capturando) return;
        capturando = true;

        // Feedback visual inmediato — el operario sabe que se registró su toque
        lblEstadoOcr.setText(""); // limpiar label previo si existe

        android.content.ContentValues cv = new android.content.ContentValues();
        cv.put(MediaStore.Images.Media.DISPLAY_NAME, "ocr_" + System.currentTimeMillis());
        cv.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        ImageCapture.OutputFileOptions opts = new ImageCapture.OutputFileOptions
                .Builder(getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)
                .build();

        imageCapture.takePicture(opts, cameraExecutor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults out) {
                        imagenUri = out.getSavedUri();
                        runOnUiThread(() -> {
                            capturando = false;
                            mostrarPanelResultado();
                            procesarFotoTomada();
                        });
                    }
                    @Override
                    public void onError(@NonNull ImageCaptureException e) {
                        Log.e(TAG, "takePicture: " + e.getMessage());
                        runOnUiThread(() -> {
                            capturando = false;
                            Toast.makeText(CamaraLecturaActivity.this,
                                    "Error tomando foto.", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  OCR
    // ─────────────────────────────────────────────────────────────────────────

    private void procesarFotoTomada() {
        if (imagenUri == null) return;
        lblEstadoOcr.setText("Analizando imagen...");
        btnConfirmar.setEnabled(false);
        progressOcr.setVisibility(View.VISIBLE);

        // Todo el procesamiento pesado en hilo de fondo
        cameraExecutor.execute(() -> {
            try {
                InputStream is = getContentResolver().openInputStream(imagenUri);
                Bitmap original = BitmapFactory.decodeStream(is);
                if (is != null) is.close();

                if (original == null) {
                    runOnUiThread(() -> {
                        progressOcr.setVisibility(View.GONE);
                        lblEstadoOcr.setText("Error leyendo la foto. Ingrese la lectura.");
                        btnConfirmar.setEnabled(true);
                    });
                    return;
                }

                // Recortar según posición actual del recuadro
                float[] p = recuadroGuia.getProporciones();
                Bitmap recortado = recortarBitmap(original, p[0], p[1], p[2], p[3]);

                // Escalar el recorte a máximo 800px de ancho antes de preprocesar
                // Evita iterar millones de píxeles en fotos de alta resolución
                Bitmap recortadoEscalado = escalarSiNecesario(recortado, 800);

                // Mostrar foto con recuadro dibujado (en UI thread)
                Bitmap conRecuadro = dibujarRecuadroEnFoto(original, p);
                runOnUiThread(() -> imgResultado.setImageBitmap(conRecuadro));

                // Guardar en destino si se indicó
                if (!nombreImagen.trim().isEmpty()) {
                    guardarEnDestino(original);
                }

                // Preparar 3 variantes de preprocesamiento — se intentan en cascada
                // Variante 1: original (rodillos analógicos, displays de buen contraste)
                // Variante 2: invertido+estirado (LCD azul/gris oscuro: Genesis II, Israel)
                // Variante 3: solo estirado (dígitos claros sobre fondo oscuro)
                int anchoSinKwh = (int)(recortadoEscalado.getWidth() * 0.85f);
                int altoRec = recortadoEscalado.getHeight();
                if (anchoSinKwh < 10) anchoSinKwh = recortadoEscalado.getWidth();

                Bitmap var1 = Bitmap.createBitmap(preprocesarOriginal(recortadoEscalado),
                        0, 0, anchoSinKwh, altoRec);
                Bitmap var2 = Bitmap.createBitmap(preprocesarInvertidoEstirado(recortadoEscalado),
                        0, 0, anchoSinKwh, altoRec);
                Bitmap var3 = Bitmap.createBitmap(preprocesarEstirado(recortadoEscalado),
                        0, 0, anchoSinKwh, altoRec);

                TextRecognizer rec = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

                // Intento 1 — imagen original
                rec.process(InputImage.fromBitmap(var1, 0))
                        .addOnSuccessListener(vt1 -> {
                            String lec1 = extraerLectura(vt1);
                            Log.d(TAG, "OCR intento1: [" + lec1 + "]");
                            if (!lec1.isEmpty()) {
                                runOnUiThread(() -> mostrarLecturaOcr(lec1));
                            } else {
                                // Intento 2 — invertido + estirado (LCD azul/gris)
                                rec.process(InputImage.fromBitmap(var2, 0))
                                        .addOnSuccessListener(vt2 -> {
                                            String lec2 = extraerLectura(vt2);
                                            Log.d(TAG, "OCR intento2: [" + lec2 + "]");
                                            if (!lec2.isEmpty()) {
                                                runOnUiThread(() -> mostrarLecturaOcr(lec2));
                                            } else {
                                                // Intento 3 — solo estirado
                                                rec.process(InputImage.fromBitmap(var3, 0))
                                                        .addOnSuccessListener(vt3 -> {
                                                            String lec3 = extraerLectura(vt3);
                                                            Log.d(TAG, "OCR intento3: [" + lec3 + "]");
                                                            if (!lec3.isEmpty()) {
                                                                runOnUiThread(() -> mostrarLecturaOcr(lec3));
                                                            } else {
                                                                // Los 3 intentos fallaron
                                                                // Mostrar recorte ampliado para facilitar ingreso manual
                                                                final Bitmap recorteAmpliado = Bitmap.createScaledBitmap(
                                                                        recortadoEscalado,
                                                                        recortadoEscalado.getWidth() * 2,
                                                                        recortadoEscalado.getHeight() * 2,
                                                                        true);
                                                                runOnUiThread(() -> {
                                                                    progressOcr.setVisibility(View.GONE);
                                                                    // Mostrar el recorte ampliado para que el operario
                                                                    // pueda leer los dígitos fácilmente
                                                                    imgResultado.setImageBitmap(recorteAmpliado);
                                                                    imgResultado.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                                                    txtLecturaOcr.setText("");
                                                                    txtLecturaOcr.setEnabled(true);
                                                                    txtLecturaOcr.requestFocus();
                                                                    lblEstadoOcr.setText(
                                                                            "Ingrese la lectura manualmente.");
                                                                    btnConfirmar.setEnabled(true);
                                                                });
                                                            }
                                                        })
                                                        .addOnFailureListener(e3 -> runOnUiThread(() -> mostrarLecturaOcr("")));
                                            }
                                        })
                                        .addOnFailureListener(e2 -> runOnUiThread(() -> mostrarLecturaOcr("")));
                            }
                        })
                        .addOnFailureListener(e1 -> runOnUiThread(() -> mostrarLecturaOcr("")));
            } catch (Exception e) {
                Log.e(TAG, "procesarFotoTomada: " + e.getMessage());
                runOnUiThread(() -> {
                    progressOcr.setVisibility(View.GONE);
                    lblEstadoOcr.setText("Error procesando foto. Ingrese manualmente.");
                    btnConfirmar.setEnabled(true);
                });
            }
        });
    }

    /** Muestra el resultado OCR en UI — llamado desde cualquier intento de la cascada */
    private void mostrarLecturaOcr(String lectura) {
        progressOcr.setVisibility(View.GONE);
        if (!lectura.isEmpty()) {
            txtLecturaOcr.setText(lectura);
            lblEstadoOcr.setText("Lectura detectada. Verifique y confirme.");
        } else {
            txtLecturaOcr.setText("");
            lblEstadoOcr.setText("No se detectó lectura. Ingrese manualmente.");
        }
        btnConfirmar.setEnabled(true);
    }

    /** Escala el bitmap para que el lado mayor no supere maxPx. Evita procesar fotos de alta res. */
    private Bitmap escalarSiNecesario(Bitmap src, int maxPx) {
        int w = src.getWidth(), h = src.getHeight();
        if (w <= maxPx) return src;
        float factor = (float) maxPx / w;
        int newH = Math.max(1, (int)(h * factor));
        return Bitmap.createScaledBitmap(src, maxPx, newH, true);
    }

    private Bitmap recortarBitmap(Bitmap src, float l, float t, float r, float b) {
        int w = src.getWidth(), h = src.getHeight();
        int x = (int)(w * l), y = (int)(h * t);
        int aw = (int)(w * (r - l)), ah = (int)(h * (b - t));
        if (aw < 50) aw = 50;
        if (ah < 20) ah = 20;
        if (x + aw > w) aw = w - x;
        if (y + ah > h) ah = h - y;
        if (x < 0) x = 0; if (y < 0) y = 0;
        return Bitmap.createBitmap(src, x, y, aw, ah);
    }

    private Bitmap dibujarRecuadroEnFoto(Bitmap src, float[] p) {
        Bitmap copia = src.copy(Bitmap.Config.ARGB_8888, true);
        Canvas c = new Canvas(copia);
        Paint pt = new Paint();
        pt.setColor(Color.parseColor("#FF5722"));
        pt.setStyle(Paint.Style.STROKE);
        pt.setStrokeWidth(Math.max(6f, copia.getWidth() * 0.008f));
        c.drawRect(copia.getWidth() * p[0], copia.getHeight() * p[1],
                copia.getWidth() * p[2], copia.getHeight() * p[3], pt);
        return copia;
    }

    private void guardarEnDestino(Bitmap bmp) {
        try {
            File f = new File(nombreImagen.trim());
            if (f.getParentFile() != null) f.getParentFile().mkdirs();
            java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
            bmp.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, "guardarEnDestino: " + e.getMessage());
        }
    }

    /**
     * Extrae la lectura del OCR.
     * - Normaliza O→0 I→1 S→5 B→8
     * - Descarta potencias de 10 (escala del medidor)
     * - Prioriza 5 dígitos > 6 > 4 > 3
     * - Intenta unir bloques numéricos adyacentes separados por punto decimal
     *   (ej: ML Kit lee "00047" y "182" por separado → une a "00047182" → toma "47")
     */
    private String extraerLectura(Text vt) {
        Log.e(TAG, "OCR raw: [" + vt.getText() + "]");
        for (Text.TextBlock block : vt.getTextBlocks()) {
            Log.e(TAG, "OCR bloque raw: [" + block.getText() + "] Y=" +
                    (block.getBoundingBox() != null ? block.getBoundingBox().centerY() : 0));
        }

        List<CandidatoOcr> lista = new ArrayList<>();

        // Recolectar bloques con su posición Y
        for (Text.TextBlock block : vt.getTextBlocks()) {
            android.graphics.Rect bbox = block.getBoundingBox();
            int blockY = bbox != null ? bbox.centerY() : 0;

            StringBuilder digitosBloque = new StringBuilder();
            for (Text.Line line : block.getLines()) {
                String tx = line.getText().trim()
                        .replace("O","0").replace("o","0")
                        .replace("D","0").replace("Q","0")
                        .replace("I","1").replace("l","1").replace("|","1")
                        .replace("S","5").replace("s","5")
                        .replace("B","8")
                        .replace("G","6")
                        .replace("Z","2").replace("z","2");

                String txSinEsp = tx.replace(" ","");

                // Capturar parte entera de cualquier número con punto/coma decimal
                // Regex: secuencia de dígitos opcionalmente seguida de separador + 1-3 decimales
                // Grupo 1 = solo enteros (descarta decimales siempre)
                Matcher m = Pattern.compile("(\\d{1,8})(?:[.,]\\d{1,3})?").matcher(txSinEsp);
                while (m.find()) {
                    String num = m.group(1);
                    if (num.length() >= 2) { // mínimo 2 para evitar dígitos sueltos
                        lista.add(new CandidatoOcr(num, blockY));
                    }
                }

                // Acumular dígitos del bloque para unir fragmentos separados por espacio
                // (displays LCD que ML Kit parte: "0047" + "82" → "004782")
                for (char c : txSinEsp.toCharArray()) {
                    if (Character.isDigit(c)) digitosBloque.append(c);
                }
            }

            // Candidato con dígitos del bloque unidos — útil para displays LCD
            String digBloque = digitosBloque.toString();
            if (digBloque.length() >= 3 && digBloque.length() <= 10) {
                lista.add(new CandidatoOcr(digBloque, blockY));
            }
        }

        if (lista.isEmpty()) return "";

        // Filtrar candidatos inválidos
        List<CandidatoOcr> filtrados = new ArrayList<>();
        for (CandidatoOcr c : lista) {
            if (c.numero.length() < 3) continue;       // menos de 3 dígitos → ruido
            if (c.numero.length() > 8) continue;       // más de 8 → imposible en medidor
            if (esPotenciaDe10(c.numero)) continue;    // 10, 100, 1000 → escala del medidor
            filtrados.add(c);
        }
        if (filtrados.isEmpty()) filtrados = new ArrayList<>(lista);

        // Scoring:
        // 1. POSICIÓN Y — el display siempre está arriba en el recorte
        //    Dividir imagen en tercios: superior (0-33%) vale +300, medio (33-66%) vale +100
        //    Inferior (código de barras, serial, texto técnico) vale +0
        // 2. LONGITUD — sin favoritismo fijo, pero penalizar extremos (2 o 8 dígitos)
        //    porque la mayoría de medidores tienen 4-6 dígitos enteros

        // Calcular altura total aproximada de la imagen a partir de los Y de los bloques
        int maxY = 0;
        for (CandidatoOcr c : filtrados) if (c.blockY > maxY) maxY = c.blockY;
        if (maxY == 0) maxY = 1;

        String mejor = "";
        int mejorScore = -1;
        for (CandidatoOcr c : filtrados) {
            // Score por posición vertical (más arriba = mejor)
            float proporcionY = (float) c.blockY / maxY; // 0=arriba, 1=abajo
            int scorePosY;
            if      (proporcionY < 0.35f) scorePosY = 400; // tercio superior
            else if (proporcionY < 0.65f) scorePosY = 150; // tercio medio
            else                          scorePosY = 0;   // tercio inferior (ruido)

            // Score por longitud — favorecer 4-6 dígitos sin ser rígido
            int len = c.numero.length();
            int scoreLen;
            if      (len == 5) scoreLen = 200;
            else if (len == 6) scoreLen = 180;
            else if (len == 4) scoreLen = 170;
            else if (len == 7) scoreLen = 150;
            else if (len == 3) scoreLen = 100;
            else               scoreLen = 50;

            int score = scorePosY + scoreLen;
            Log.d(TAG, "Candidato: " + c.numero + " Y=" + c.blockY + " propY=" + proporcionY + " score=" + score);
            if (score > mejorScore) { mejorScore = score; mejor = c.numero; }
        }

        // Quitar ceros a la izquierda
        try { mejor = String.valueOf(Long.parseLong(mejor)); }
        catch (NumberFormatException ignored) {}
        return mejor;
    }

    /**
     * Calcula luminancia promedio del bitmap (muestreado para velocidad).
     */
    private int calcularLumPromedio(Bitmap bmp) {
        int w = bmp.getWidth(), h = bmp.getHeight();
        int muestras = 0, sumaLum = 0;
        int paso = Math.max(1, w / 20);
        for (int x = 0; x < w; x += paso) {
            for (int y = 0; y < h; y += paso) {
                int p = bmp.getPixel(x, y);
                int r = (p >> 16) & 0xFF;
                int g = (p >>  8) & 0xFF;
                int b =  p        & 0xFF;
                sumaLum += (int)(0.299f * r + 0.587f * g + 0.114f * b);
                muestras++;
            }
        }
        return muestras > 0 ? sumaLum / muestras : 128;
    }

    /**
     * Variante 1 — imagen original sin modificar.
     * Mejor para: rodillos analógicos (MaxMeter, CHINT), displays de buen contraste.
     */
    private Bitmap preprocesarOriginal(Bitmap original) {
        int lum = calcularLumPromedio(original);
        Log.d(TAG, "preprocesar ORIGINAL lumPromedio=" + lum);
        if (lum < 60) {
            // Rodillo muy oscuro (METREX) → solo invertir
            return invertirBitmap(original);
        }
        return original;
    }

    /**
     * Variante 2 — invertir + estiramiento de histograma.
     * Mejor para: displays LCD azul/gris oscuro (Genesis II, Israel).
     * Convierte a escala de grises, invierte, luego estira el rango al máximo contraste.
     */
    private Bitmap preprocesarInvertidoEstirado(Bitmap original) {
        int w = original.getWidth(), h = original.getHeight();
        Log.d(TAG, "preprocesar INVERTIDO+ESTIRADO");

        // Paso 1: convertir a luminancia
        int[] lums = new int[w * h];
        int minL = 255, maxL = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int p = original.getPixel(x, y);
                int r = (p >> 16) & 0xFF;
                int g = (p >>  8) & 0xFF;
                int b =  p        & 0xFF;
                int lum = (int)(0.299f * r + 0.587f * g + 0.114f * b);
                lums[y * w + x] = lum;
                if (lum < minL) minL = lum;
                if (lum > maxL) maxL = lum;
            }
        }

        // Paso 2: estiramiento de histograma + inversión
        Bitmap resultado = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        int rango = maxL - minL;
        if (rango < 1) rango = 1;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int lum = lums[y * w + x];
                // Estirar al rango 0-255
                int estirado = (lum - minL) * 255 / rango;
                // Invertir (dígitos oscuros → claros, fondo claro → oscuro)
                int inv = 255 - estirado;
                resultado.setPixel(x, y, 0xFF000000 | (inv << 16) | (inv << 8) | inv);
            }
        }
        return resultado;
    }

    /**
     * Variante 3 — solo estiramiento de histograma sin invertir.
     * Para displays donde los dígitos son más claros que el fondo.
     */
    private Bitmap preprocesarEstirado(Bitmap original) {
        int w = original.getWidth(), h = original.getHeight();
        Log.d(TAG, "preprocesar ESTIRADO");

        int[] lums = new int[w * h];
        int minL = 255, maxL = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int p = original.getPixel(x, y);
                int r = (p >> 16) & 0xFF;
                int g = (p >>  8) & 0xFF;
                int b =  p        & 0xFF;
                int lum = (int)(0.299f * r + 0.587f * g + 0.114f * b);
                lums[y * w + x] = lum;
                if (lum < minL) minL = lum;
                if (lum > maxL) maxL = lum;
            }
        }

        Bitmap resultado = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        int rango = maxL - minL;
        if (rango < 1) rango = 1;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int lum = lums[y * w + x];
                int estirado = (lum - minL) * 255 / rango;
                resultado.setPixel(x, y, 0xFF000000 | (estirado << 16) | (estirado << 8) | estirado);
            }
        }
        return resultado;
    }

    private Bitmap invertirBitmap(Bitmap original) {
        int w = original.getWidth(), h = original.getHeight();
        Bitmap inv = original.copy(Bitmap.Config.ARGB_8888, true);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int p = inv.getPixel(x, y);
                inv.setPixel(x, y, 0xFF000000
                        | ((255 - ((p >> 16) & 0xFF)) << 16)
                        | ((255 - ((p >>  8) & 0xFF)) <<  8)
                        | ((255 - ( p        & 0xFF))));
            }
        }
        return inv;
    }

    private boolean esPotenciaDe10(String num) {
        try {
            long v = Long.parseLong(num);
            if (v <= 0) return false;
            while (v % 10 == 0) v /= 10;
            return v == 1;
        } catch (NumberFormatException e) { return false; }
    }

    private static class CandidatoOcr {
        final String numero; final int blockY;
        CandidatoOcr(String n, int y) { numero = n; blockY = y; }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Navegación
    // ─────────────────────────────────────────────────────────────────────────

    private void mostrarPanelResultado() {
        // Apagar flash al salir del preview
        if (camera != null && flashEncendido) {
            camera.getCameraControl().enableTorch(false);
        }
        panelCamara.setVisibility(View.GONE);
        scrollResultado.setVisibility(View.VISIBLE);
    }

    private void volverACamara() {
        limpiarUri();
        txtLecturaOcr.setText("");
        scrollResultado.setVisibility(View.GONE);
        panelCamara.setVisibility(View.VISIBLE);
        // Restaurar estado del flash si estaba encendido
        if (camera != null && flashEncendido) {
            camera.getCameraControl().enableTorch(true);
        }
    }

    private void confirmarLectura() {
        String lec = txtLecturaOcr.getText().toString().trim();
        if (lec.isEmpty()) {
            Toast.makeText(this, "Ingrese la lectura.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!lec.matches("^[0-9]+$")) {
            Toast.makeText(this, "Solo números.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent res = new Intent();
        res.putExtra(EXTRA_LECTURA_OCR,   lec);
        res.putExtra(EXTRA_NOMBRE_IMAGEN,  nombreImagen);
        setResult(RESULT_OK, res);
        finish();
    }

    private void cancelar() {
        limpiarUri();
        setResult(RESULT_CANCELED);
        finish();
    }

    private void limpiarUri() {
        if (imagenUri != null) {
            try { getContentResolver().delete(imagenUri, null, null); }
            catch (Exception ignored) {}
            imagenUri = null;
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  RecuadroGuiaView — overlay arrastrable y redimensionable
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Overlay sobre el preview con recuadro interactivo.
     *
     * GESTOS:
     *  - Dedo dentro del recuadro  → MUEVE el recuadro completo
     *  - Dedo en esquina inf-derecha (zona naranja ▶) → REDIMENSIONA el alto
     *
     * El ancho siempre ocupa casi toda la pantalla (no se modifica)
     * para no cortar dígitos laterales.
     */
    public static class RecuadroGuiaView extends View {

        // Proporciones por defecto (relativas 0..1)
        // Recuadro más delgado por defecto → obliga al operario a ajustarlo
        // sobre la fila exacta de dígitos, evitando capturar texto circundante
        private float gL = 0.01f;   // left — casi al borde para no cortar dígito izquierdo
        private float gT = 0.32f;   // top
        private float gR = 0.99f;   // right
        private float gB = 0.43f;   // bottom — solo 11% de alto, una fila de dígitos

        private static final float MIN_ALTO  = 0.04f;
        private static final float MIN_ANCHO = 0.30f;

        private static final int MODO_NADA   = 0;
        private static final int MODO_MOVER  = 1;
        private static final int MODO_RESIZE = 2;
        private int modo = MODO_NADA;

        private float offX, offY;

        private Paint pOscuro, pBorde, pEsquina, pLabel, pAyuda, pResize;

        public RecuadroGuiaView(Context ctx) { super(ctx); init(); }
        public RecuadroGuiaView(Context ctx, AttributeSet a) { super(ctx, a); init(); }

        private void init() {
            pOscuro = new Paint();
            pOscuro.setColor(Color.argb(150, 0, 0, 0));
            pOscuro.setStyle(Paint.Style.FILL);

            pBorde = new Paint();
            pBorde.setColor(Color.parseColor("#FF5722"));
            pBorde.setStyle(Paint.Style.STROKE);
            pBorde.setStrokeWidth(4f);
            pBorde.setAntiAlias(true);

            pEsquina = new Paint(pBorde);
            pEsquina.setStrokeWidth(10f);
            pEsquina.setStrokeCap(Paint.Cap.ROUND);

            pLabel = new Paint();
            pLabel.setColor(Color.parseColor("#FF5722"));
            pLabel.setTextSize(36f);
            pLabel.setTextAlign(Paint.Align.CENTER);
            pLabel.setAntiAlias(true);
            pLabel.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);

            pAyuda = new Paint();
            pAyuda.setColor(Color.argb(190, 255, 255, 255));
            pAyuda.setTextSize(25f);
            pAyuda.setTextAlign(Paint.Align.CENTER);
            pAyuda.setAntiAlias(true);

            pResize = new Paint();
            pResize.setColor(Color.parseColor("#FF5722"));
            pResize.setStyle(Paint.Style.FILL);
            pResize.setAntiAlias(true);
        }

        /** Devuelve [left, top, right, bottom] como proporciones 0..1 */
        public float[] getProporciones() {
            return new float[]{ gL, gT, gR, gB };
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            int w = getWidth(), h = getHeight();
            if (w == 0 || h == 0) return false;

            float left   = w * gL, top    = h * gT;
            float right  = w * gR, bottom = h * gB;
            float x = ev.getX(), y = ev.getY();

            // Zona de resize: cuadrado de 80dp en esquina inferior-derecha
            float rz = 80f * getResources().getDisplayMetrics().density;

            switch (ev.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if (x >= right - rz && y >= bottom - rz) {
                        modo = MODO_RESIZE;
                        offX = x - right;
                        offY = y - bottom;
                    } else if (x >= left && x <= right && y >= top && y <= bottom) {
                        modo = MODO_MOVER;
                        offX = x - left;
                        offY = y - top;
                    } else {
                        modo = MODO_NADA;
                    }
                    return true;

                case MotionEvent.ACTION_MOVE:
                    if (modo == MODO_MOVER) {
                        float aw = gR - gL, ah = gB - gT;
                        float nl = Math.max(0.01f, Math.min((x - offX) / w, 1f - aw - 0.01f));
                        float nt = Math.max(0.02f, Math.min((y - offY) / h, 1f - ah - 0.02f));
                        gL = nl; gT = nt; gR = nl + aw; gB = nt + ah;
                        invalidate();
                    } else if (modo == MODO_RESIZE) {
                        float nr = Math.max(gL + MIN_ANCHO, Math.min((x - offX) / w, 0.99f));
                        float nb = Math.max(gT + MIN_ALTO,  Math.min((y - offY) / h, 0.97f));
                        gR = nr; gB = nb;
                        invalidate();
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    modo = MODO_NADA;
                    return true;
            }
            return false;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int w = getWidth(), h = getHeight();
            if (w == 0 || h == 0) return;

            float L = w * gL, T = h * gT, R = w * gR, B = h * gB;

            // Spotlight
            canvas.drawRect(0, 0, w, T, pOscuro);
            canvas.drawRect(0, B, w, h, pOscuro);
            canvas.drawRect(0, T, L, B, pOscuro);
            canvas.drawRect(R, T, w, B, pOscuro);

            // Borde
            canvas.drawRect(L, T, R, B, pBorde);

            // Esquinas
            float eq = Math.min(w, h) * 0.06f;
            canvas.drawLine(L, T, L + eq, T, pEsquina);
            canvas.drawLine(L, T, L, T + eq, pEsquina);
            canvas.drawLine(R, T, R - eq, T, pEsquina);
            canvas.drawLine(R, T, R, T + eq, pEsquina);
            canvas.drawLine(L, B, L + eq, B, pEsquina);
            canvas.drawLine(L, B, L, B - eq, pEsquina);
            canvas.drawLine(R, B, R - eq, B, pEsquina);
            canvas.drawLine(R, B, R, B - eq, pEsquina);

            // Etiqueta
            canvas.drawText("PANTALLA DEL MEDIDOR", (L + R) / 2f, T - 14f, pLabel);

            // Texto interior
            canvas.drawText("Mueva el recuadro sobre los digitos",
                    (L + R) / 2f, (T + B) / 2f + pAyuda.getTextSize() / 2f, pAyuda);

            // Triángulo de resize en esquina inferior-derecha
            float rz = 36f;
            android.graphics.Path tri = new android.graphics.Path();
            tri.moveTo(R - rz, B);
            tri.lineTo(R, B);
            tri.lineTo(R, B - rz);
            tri.close();
            canvas.drawPath(tri, pResize);

            // Flecha diagonal blanca dentro del triángulo
            Paint arw = new Paint(pEsquina);
            arw.setColor(Color.WHITE);
            arw.setStrokeWidth(4f);
            canvas.drawLine(R - rz + 10f, B - 10f, R - 10f, B - rz + 10f, arw);
        }
    }
}