package com.gselectroCaqueta.moduloanomalias;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.lang.ref.WeakReference;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Informe escrito (EBSA): se pide al elegir una causa de no lectura y en las desviaciones (lectura que no es
 * consumo normal ni bajo), antes de la foto, y es obligatorio. Va en el campo INFORME de REGISTRO.SDA (posición 108, 30 caracteres).
 * <p>
 * El texto se normaliza a lo que admite el plano: mayúsculas, sin tildes ni eñe, sin el separador "|" ni saltos
 * de línea, y recortado a 30. "Cancelar" (o atrás) cancela la causal: sin informe no se aplica.
 * <p>
 * Dictado: el botón "Dictar" abre el reconocimiento de voz de Google (RecognizerIntent).
 * <ul>
 *   <li>Con internet: español de Colombia (es-CO), reconocimiento en línea.</li>
 *   <li>Sin internet: español de Estados Unidos (es-US) con el modelo sin conexión. Google no tiene es-CO sin
 *       conexión: pedirlo así respondía "La búsqueda por voz no está disponible". El paquete se descarga una vez
 *       por equipo (app de Google > Ajustes > Voz > Reconocimiento de voz sin conexión > Español (Estados Unidos)).</li>
 * </ul>
 * El texto reconocido se agrega al campo, donde el técnico lo puede corregir antes de aceptar. El resultado llega por onActivityResult de la Activity, que lo reenvía con {@link #alResultadoVoz}.
 */
public final class DialogoInforme {

    public static final int LARGO_INFORME = 30;
    public static final int MINIMO = 3;
    /** requestCode del dictado; la Activity debe reenviar ese resultado a {@link #alResultadoVoz}. */
    public static final int REQUEST_VOZ = 6690;

    /** Campo del diálogo abierto, para recibir el dictado. Débil: si el diálogo se cerró, se ignora. */
    private static WeakReference<EditText> campoAbierto = new WeakReference<>(null);

    public interface Callback {
        /** @param informe texto ya normalizado, entre {@link #MINIMO} y {@link #LARGO_INFORME} caracteres. */
        void alAceptar(String informe);

        void alCancelar();
    }

    private DialogoInforme() {
    }

    /** Informe de la causal (título fijo). */
    public static void mostrar(Activity activity, String causa, Callback callback) {
        mostrar(activity, "Informe de la causal", causa, callback);
    }

    /**
     * @param titulo   título del diálogo ("Informe de la causal", "Informe de la desviacion").
     * @param etiqueta qué se está informando (la causal o la desviación con su lectura).
     */
    public static void mostrar(Activity activity, String titulo, String etiqueta, Callback callback) {
        float densidad = activity.getResources().getDisplayMetrics().density;
        int margen = (int) (20 * densidad);

        LinearLayout contenedor = new LinearLayout(activity);
        contenedor.setOrientation(LinearLayout.VERTICAL);
        contenedor.setPadding(margen, (int) (8 * densidad), margen, 0);

        TextView rotulo = new TextView(activity);
        rotulo.setText(etiqueta);
        rotulo.setTextSize(15);
        contenedor.addView(rotulo);

        EditText campo = new EditText(activity);
        campo.setHint("Describa lo encontrado (obligatorio)");
        campo.setSingleLine(true);
        campo.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        campo.setImeOptions(EditorInfo.IME_ACTION_DONE);
        campo.setFilters(new InputFilter[]{new InputFilter.LengthFilter(LARGO_INFORME)});
        contenedor.addView(campo);

        TextView contador = new TextView(activity);
        contador.setTextSize(12);
        contador.setText("0/" + LARGO_INFORME);
        contenedor.addView(contador);

        Button dictar = new Button(activity);
        dictar.setText("\uD83C\uDFA4  Dictar");
        dictar.setAllCaps(false);
        dictar.setOnClickListener(v -> iniciarDictado(activity));
        contenedor.addView(dictar);
        campoAbierto = new WeakReference<>(campo);

        AlertDialog dialogo = new AlertDialog.Builder(activity)
                .setTitle(titulo)
                .setView(contenedor)
                .setCancelable(false)
                .setPositiveButton("Aceptar", null) // se valida antes de cerrar (ver setOnShowListener)
                .setNegativeButton("Cancelar", (d, w) -> {
                    campoAbierto = new WeakReference<>(null);
                    d.dismiss();
                    callback.alCancelar();
                })
                .create();

        dialogo.setOnShowListener(d -> {
            Button aceptar = dialogo.getButton(AlertDialog.BUTTON_POSITIVE);
            aceptar.setEnabled(false);
            campo.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    contador.setText(s.length() + "/" + LARGO_INFORME);
                    aceptar.setEnabled(normalizar(s.toString()).length() >= MINIMO);
                }
            });
            Runnable confirmar = () -> {
                String informe = normalizar(campo.getText().toString());
                if (informe.length() < MINIMO) {
                    campo.setError("Escriba al menos " + MINIMO + " caracteres");
                    return;
                }
                campoAbierto = new WeakReference<>(null);
                dialogo.dismiss();
                callback.alAceptar(informe);
            };
            aceptar.setOnClickListener(v -> confirmar.run());
            campo.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    confirmar.run();
                    return true;
                }
                return false;
            });
            campo.requestFocus();
        });

        dialogo.show();
        if (dialogo.getWindow() != null) {
            dialogo.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    }

    /** Si el ultimo dictado fue sin conexion: para orientar al tecnico si falla. */
    private static boolean ultimoSinConexion = false;

    private static void iniciarDictado(Activity activity) {
        boolean enLinea = hayInternet(activity);
        ultimoSinConexion = !enLinea;
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        if (enLinea) {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-CO");
        } else {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-US");
            intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
        }
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Dicte el informe");
        try {
            activity.startActivityForResult(intent, REQUEST_VOZ);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, "Este equipo no tiene reconocimiento de voz. Escriba el informe.", Toast.LENGTH_LONG).show();
        }
    }

    /** Hay red con salida a internet (wifi o datos). */
    private static boolean hayInternet(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            Network red = cm.getActiveNetwork();
            NetworkCapabilities cap = red == null ? null : cm.getNetworkCapabilities(red);
            return cap != null && cap.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    && cap.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        } catch (Exception e) {
            return false;
        }
    }

    /** Llamar desde onActivityResult con requestCode == {@link #REQUEST_VOZ}. */
    public static void alResultadoVoz(int resultCode, Intent data) {
        EditText campo = campoAbierto.get();
        if (campo == null) return;
        if (resultCode != Activity.RESULT_OK || data == null) {
            if (resultCode != Activity.RESULT_CANCELED) {
                Toast.makeText(campo.getContext(), ultimoSinConexion
                        ? "Sin internet: descargue el español (Estados Unidos) sin conexión en la app de Google > Ajustes > Voz, o escriba el informe."
                        : "No se pudo reconocer la voz. Intente de nuevo o escriba el informe.", Toast.LENGTH_LONG).show();
            }
            return;
        }
        ArrayList<String> textos = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        if (textos == null || textos.isEmpty() || textos.get(0) == null) return;
        String actual = campo.getText().toString().trim();
        String dictado = textos.get(0).trim().toUpperCase(Locale.ROOT);
        campo.setText(actual.isEmpty() ? dictado : actual + " " + dictado); // el LengthFilter recorta a 30
        campo.setSelection(campo.getText().length());
        if (actual.length() + 1 + dictado.length() > LARGO_INFORME) {
            Toast.makeText(campo.getContext(), "El informe admite " + LARGO_INFORME + " caracteres: se recorto.", Toast.LENGTH_SHORT).show();
        }
    }

    /** Mayúsculas, sin tildes ni eñe, sin "|" ni saltos de línea, espacios simples, máximo 30. */
    static String normalizar(String texto) {
        if (texto == null) return "";
        String t = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        t = t.toUpperCase(Locale.ROOT)
                .replace('|', ' ')
                .replaceAll("[\\r\\n\\t]", " ")
                .replaceAll("[^A-Z0-9 .,;:#/()+\\-]", "")
                .replaceAll(" +", " ")
                .trim();
        return t.length() > LARGO_INFORME ? t.substring(0, LARGO_INFORME).trim() : t;
    }
}
