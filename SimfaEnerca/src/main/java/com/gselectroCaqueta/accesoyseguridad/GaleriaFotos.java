package com.gselectroCaqueta.accesoyseguridad;

import android.app.Activity;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.LruCache;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.gselectroCaqueta.sync.ColaEnvio;
import com.gselectroCaqueta.sync.ColaEnvioSqlite;
import com.gselectroCaqueta.sync.ConfigSync;
import com.gselectroCaqueta.sync.EventoMovil;
import com.gselectroCaqueta.sync.SincronizacionService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Galería de fotos del celular (bloque D). Lista las fotos de DCIM/FOTOGRAFIASL por cuenta, con su estado:
 * <ul>
 *   <li><b>Sin enviar</b>: se puede borrar (sale de la cola de envío y del índice F de la ruta).</li>
 *   <li><b>Enviada</b>: solo se anula, con código y clave del supervisor (LECTOR.TXT). Va al servidor como evento
 *       foto_anulada: la web la marca y conserva el archivo. En el celular la foto queda, marcada como anulada.</li>
 * </ul>
 * El estado sale de la cola de envío (API); por SOAP, del índice F (X = sin enviar, Y = enviada). Una foto que no
 * está en ninguno se trata como enviada (la cola borra lo enviado después de 15 días).
 */
public class GaleriaFotos extends Activity {

    private static final int SIN_ENVIAR = 0;
    private static final int ENVIADA = 1;
    private static final int ANULADA = 2;

    private static final class Foto {
        final File archivo;
        final String cuenta;
        final String mes;
        final String anno;
        int estado;

        Foto(File archivo, String cuenta, String mes, String anno, int estado) {
            this.archivo = archivo;
            this.cuenta = cuenta;
            this.mes = mes;
            this.anno = anno;
            this.estado = estado;
        }
    }

    private final List<Foto> todas = new ArrayList<>();
    private final List<Foto> visibles = new ArrayList<>();
    private final ExecutorService hilo = Executors.newFixedThreadPool(2);
    private final Handler ui = new Handler(Looper.getMainLooper());
    private final LruCache<String, Bitmap> miniaturas = new LruCache<String, Bitmap>(8 * 1024 * 1024) {
        @Override
        protected int sizeOf(String k, Bitmap b) {
            return b.getByteCount();
        }
    };
    private Adaptador adaptador;
    private TextView txtResumen;
    private EditText txtBuscar;
    private AlmacenEstado anuladas;

    /** Toques más seguidos que esto sobre la lista se ignoran (un doble toque abría dos diálogos). */
    private static final long ESPERA_TOQUE_MS = 600;
    /** Diálogo de la foto abierto: la lista no abre otro hasta que se cierre (onDismiss). */
    private boolean fotoAbierta;
    /** Diálogo de borrar / anular abierto: no se abre otro igual con un segundo toque. */
    private boolean accionAbierta;
    private long ultimoToque;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        anuladas = AlmacenEstado.de(this, "fotos_anuladas");
        int pad = dp(12);

        // Presentación con la paleta de sol de la lectura v2 (colors_lectura.xml).
        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setBackgroundColor(getColor(R.color.lec_superficie));
        raiz.setPadding(pad, dp(4), pad, 0);

        LinearLayout cabecera = new LinearLayout(this);
        cabecera.setOrientation(LinearLayout.HORIZONTAL);
        cabecera.setGravity(Gravity.CENTER_VERTICAL);
        ImageButton volver = new ImageButton(this);
        volver.setImageResource(R.drawable.ic_nav_back);
        volver.setImageTintList(android.content.res.ColorStateList.valueOf(getColor(R.color.lec_texto)));
        volver.setBackgroundResource(R.drawable.bg_lec_icono);
        volver.setContentDescription("Volver");
        volver.setOnClickListener(v -> finish());
        LinearLayout.LayoutParams lpVolver = new LinearLayout.LayoutParams(dp(44), dp(44));
        lpVolver.setMarginStart(-dp(8));
        lpVolver.setMarginEnd(dp(4));
        cabecera.addView(volver, lpVolver);

        LinearLayout textos = new LinearLayout(this);
        textos.setOrientation(LinearLayout.VERTICAL);
        TextView titulo = new TextView(this);
        titulo.setText("Galería de fotos");
        titulo.setTextSize(20);
        titulo.setTypeface(Typeface.DEFAULT_BOLD);
        titulo.setTextColor(getColor(R.color.lec_texto));
        textos.addView(titulo);

        txtResumen = new TextView(this);
        txtResumen.setTextSize(13);
        txtResumen.setTypeface(Typeface.DEFAULT_BOLD);
        txtResumen.setTextColor(getColor(R.color.lec_texto2));
        txtResumen.setFontFeatureSettings("tnum");
        textos.addView(txtResumen);
        cabecera.addView(textos, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        raiz.addView(cabecera);

        txtBuscar = new EditText(this);
        txtBuscar.setHint("Buscar por cuenta");
        txtBuscar.setSingleLine(true);
        txtBuscar.setInputType(InputType.TYPE_CLASS_NUMBER);
        txtBuscar.setBackgroundResource(R.drawable.bg_lec_campo);
        txtBuscar.setPadding(dp(12), 0, dp(12), 0);
        txtBuscar.setTextSize(15);
        txtBuscar.setTypeface(Typeface.DEFAULT_BOLD);
        txtBuscar.setTextColor(getColor(R.color.lec_texto));
        txtBuscar.setHintTextColor(getColor(R.color.lec_texto2));
        txtBuscar.setFontFeatureSettings("tnum");
        txtBuscar.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_lec_buscar, 0, 0, 0);
        txtBuscar.setCompoundDrawablePadding(dp(8));
        txtBuscar.setCompoundDrawableTintList(android.content.res.ColorStateList.valueOf(getColor(R.color.lec_texto)));
        txtBuscar.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {
            }

            public void onTextChanged(CharSequence s, int a, int b, int c) {
            }

            public void afterTextChanged(Editable s) {
                filtrar();
            }
        });
        LinearLayout.LayoutParams lpBuscar = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
        lpBuscar.setMargins(0, dp(6), 0, dp(6));
        raiz.addView(txtBuscar, lpBuscar);

        ListView lista = new ListView(this);
        lista.setDivider(null);
        lista.setDividerHeight(0);
        lista.setSelector(android.R.color.transparent);
        adaptador = new Adaptador();
        lista.setAdapter(adaptador);
        lista.setOnItemClickListener((p, v, pos, id) -> {
            if (pos < 0 || pos >= visibles.size() || fotoAbierta || accionAbierta) return;
            long ahora = SystemClock.elapsedRealtime();
            if (ahora - ultimoToque < ESPERA_TOQUE_MS) return;
            ultimoToque = ahora;
            abrir(visibles.get(pos));
        });
        raiz.addView(lista, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        setContentView(raiz);
        cargar();
    }

    @Override
    protected void onDestroy() {
        hilo.shutdownNow();
        super.onDestroy();
    }

    // ------------------------------------------------------------------ datos

    private File carpeta() {
        return new File(VariablesGlobales.directorioactual + "/DCIM/FOTOGRAFIASL/");
    }

    private void cargar() {
        txtResumen.setText("Leyendo fotos...");
        hilo.execute(() -> {
            List<Foto> r = new ArrayList<>();
            File[] archivos = carpeta().listFiles((d, n) -> n.toLowerCase(Locale.US).endsWith(".jpg"));
            if (archivos != null) {
                Arrays.sort(archivos, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
                boolean rest = ConfigSync.modoRest(this);
                Map<String, Integer> cola = rest ? ColaEnvioSqlite.de(this).estadosPorArchivo(ColaEnvio.FOTO) : new HashMap<>();
                Map<String, Boolean> indice = estadosIndice();
                for (File f : archivos) {
                    String nombre = f.getName();
                    String[] partes = partes(nombre);
                    int estado;
                    String anulada = anuladas.leer(nombre);
                    if (anulada != null && !anulada.isEmpty()) {
                        estado = ANULADA;
                    } else if (cola.containsKey(nombre)) {
                        estado = cola.get(nombre) == ColaEnvio.ENVIADO ? ENVIADA : SIN_ENVIAR;
                    } else if (!rest && Boolean.FALSE.equals(indice.get(nombre.toLowerCase(Locale.US)))) {
                        estado = SIN_ENVIAR;
                    } else {
                        estado = ENVIADA;
                    }
                    r.add(new Foto(f, partes[0], partes[1], partes[2], estado));
                }
            }
            ui.post(() -> {
                todas.clear();
                todas.addAll(r);
                filtrar();
            });
        });
    }

    /** cuenta_MAAAA_n[S].jpg -> {cuenta, mes, año}. */
    private static String[] partes(String nombre) {
        String[] p = nombre.split("_");
        String cuenta = p.length > 0 ? p[0] : "";
        String mes = "", anno = "";
        if (p.length > 1 && p[1].length() > 4) {
            anno = p[1].substring(p[1].length() - 4);
            mes = p[1].substring(0, p[1].length() - 4);
        }
        return new String[]{cuenta, mes, anno};
    }

    /** Índice F de las rutas (DATOSDEENTRADA/F*): nombre en minúscula -> true si ya se envió (Y), false si no (X). */
    private Map<String, Boolean> estadosIndice() {
        Map<String, Boolean> r = new HashMap<>();
        File[] indices = new File(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/").listFiles((d, n) -> n.startsWith("F") && !n.contains("."));
        if (indices == null) return r;
        for (File f : indices) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.ISO_8859_1))) {
                String l;
                while ((l = br.readLine()) != null) {
                    int i = l.lastIndexOf(';');
                    if (i <= 0) continue;
                    r.put(l.substring(0, i).trim().toLowerCase(Locale.US), l.substring(i + 1).trim().equalsIgnoreCase("Y"));
                }
            } catch (Exception ignorado) {
                // índice ilegible: esas fotos quedan como enviadas
            }
        }
        return r;
    }

    private void filtrar() {
        String q = txtBuscar.getText().toString().trim();
        visibles.clear();
        int sinEnviar = 0;
        for (Foto f : todas) {
            if (f.estado == SIN_ENVIAR) sinEnviar++;
            if (q.isEmpty() || f.cuenta.contains(q)) visibles.add(f);
        }
        txtResumen.setText(todas.size() + " fotos · " + sinEnviar + " sin enviar"
                + (q.isEmpty() ? "" : " · " + visibles.size() + " de la búsqueda"));
        adaptador.notifyDataSetChanged();
    }

    // ------------------------------------------------------------------ acciones

    /**
     * Diálogo de la foto. El archivo y la fecha van dentro de la vista (no con setMessage): con mensaje y vista a
     * la vez el AlertDialog no reparte el alto y la foto empujaba los botones (Borrar / Anular foto / Cerrar) fuera
     * de la pantalla. La foto tiene alto fijo (~42% de la pantalla, fitCenter) para que título, textos y botones
     * se vean siempre sin desplazar.
     */
    private void abrir(Foto f) {
        int altoFoto = Math.round(getResources().getDisplayMetrics().heightPixels * 0.42f);
        ImageView img = new ImageView(this);
        img.setAdjustViewBounds(true);
        img.setScaleType(ImageView.ScaleType.FIT_CENTER);
        img.setMaxHeight(altoFoto);
        img.setBackgroundResource(R.drawable.bg_gal_miniatura);
        img.setClipToOutline(true);
        hilo.execute(() -> {
            Bitmap b = decodificar(f.archivo, 1080);
            ui.post(() -> img.setImageBitmap(b));
        });
        LinearLayout marco = new LinearLayout(this);
        marco.setOrientation(LinearLayout.VERTICAL);
        marco.setPadding(dp(20), dp(2), dp(20), dp(4));
        TextView datos = new TextView(this);
        datos.setText(f.archivo.getName() + "\n" + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(new Date(f.archivo.lastModified())));
        datos.setTextSize(14);
        datos.setTextColor(getColor(R.color.lec_texto2));
        datos.setFontFeatureSettings("tnum");
        datos.setPadding(0, 0, 0, dp(8));
        marco.addView(datos);
        marco.addView(img, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, altoFoto));
        AlertDialog.Builder d = new AlertDialog.Builder(this, R.style.CauDialogo)
                .setTitle("Cuenta " + f.cuenta + " · " + textoEstado(f.estado))
                .setView(marco)
                .setNegativeButton("Cerrar", null);
        if (f.estado == SIN_ENVIAR) {
            d.setPositiveButton("Borrar", (x, w) -> confirmarBorrar(f));
        } else if (f.estado == ENVIADA) {
            d.setPositiveButton("Anular foto", (x, w) -> pedirAnulacion(f));
        }
        AlertDialog dlg = d.create();
        dlg.setOnDismissListener(x -> fotoAbierta = false);
        fotoAbierta = true;
        dlg.show();
        botonPeligro(dlg);
    }

    private void confirmarBorrar(Foto f) {
        if (accionAbierta) return; // segundo toque sobre Borrar
        final boolean[] hecho = {false};
        AlertDialog dlg = new AlertDialog.Builder(this, R.style.CauDialogo)
                .setTitle("Borrar foto")
                .setMessage("La foto " + f.archivo.getName() + " no se ha enviado. Se borra del celular y no se enviará.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Borrar", (x, w) -> {
                    if (hecho[0]) return; // doble toque: borrar una sola vez
                    hecho[0] = true;
                    borrar(f);
                })
                .create();
        dlg.setOnDismissListener(x -> accionAbierta = false);
        accionAbierta = true;
        dlg.show();
        botonPeligro(dlg);
    }

    /** Borrar / anular en rojo con borde (solo presentación). */
    private void botonPeligro(AlertDialog dlg) {
        Button b = dlg.getButton(AlertDialog.BUTTON_POSITIVE);
        if (b == null) return;
        b.setBackgroundResource(R.drawable.bg_gal_boton_peligro);
        b.setTextColor(getColor(R.color.lec_error));
    }

    /** Campo de la anulación con el borde negro de la lectura (solo presentación). */
    private void estiloCampo(EditText t) {
        t.setBackgroundResource(R.drawable.bg_lec_campo);
        t.setMinHeight(dp(56));
        t.setPadding(dp(16), 0, dp(16), 0);
        t.setTextSize(19);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setTextColor(getColor(R.color.lec_texto));
        t.setHintTextColor(getColor(R.color.lec_texto2));
    }

    private void borrar(Foto f) {
        String nombre = f.archivo.getName();
        try {
            ColaEnvioSqlite.de(this).quitarNoEnviado(ColaEnvio.FOTO, nombre);
        } catch (Exception ignorado) {
            // sin cola (SOAP)
        }
        quitarDelIndice(nombre);
        boolean ok = borrarArchivo(f.archivo);
        escribirResumen(f.cuenta + ";FOTO BORRADA EN GALERIA;" + ahora() + ";" + nombre);
        Toast.makeText(this, ok ? "Foto borrada" : "No se pudo borrar el archivo", Toast.LENGTH_LONG).show();
        cargar();
    }

    /** Borra por MediaStore (las fotos se crean así) y, si sigue, por archivo. */
    private boolean borrarArchivo(File f) {
        try {
            ContentResolver cr = getContentResolver();
            cr.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    MediaStore.Images.Media.DISPLAY_NAME + " = ? AND " + MediaStore.Images.Media.RELATIVE_PATH + " LIKE ?",
                    new String[]{f.getName(), "DCIM/FOTOGRAFIASL%"});
        } catch (Exception ignorado) {
            // se intenta por archivo
        }
        return !f.exists() || f.delete();
    }

    /** Quita la línea de la foto del índice F de la ruta (para que el envío por SOAP no la busque). */
    private void quitarDelIndice(String nombre) {
        File[] indices = new File(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/").listFiles((d, n) -> n.startsWith("F") && !n.contains("."));
        if (indices == null) return;
        for (File f : indices) {
            try {
                StringBuilder sb = new StringBuilder();
                boolean cambio = false;
                try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.ISO_8859_1))) {
                    String l;
                    while ((l = br.readLine()) != null) {
                        int i = l.lastIndexOf(';');
                        if (i > 0 && l.substring(0, i).trim().equalsIgnoreCase(nombre)) {
                            cambio = true;
                            continue;
                        }
                        sb.append(l).append("\r\n");
                    }
                }
                if (cambio) {
                    try (FileOutputStream out = new FileOutputStream(f, false)) {
                        out.write(sb.toString().getBytes(StandardCharsets.ISO_8859_1));
                    }
                }
            } catch (Exception ignorado) {
                // índice ilegible: se deja
            }
        }
    }

    private void pedirAnulacion(Foto f) {
        if (accionAbierta) return; // segundo toque sobre Anular foto
        LinearLayout caja = new LinearLayout(this);
        caja.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(20);
        caja.setPadding(pad, dp(4), pad, 0);
        final EditText txtCodigo = new EditText(this);
        txtCodigo.setHint("Código del supervisor");
        txtCodigo.setInputType(InputType.TYPE_CLASS_NUMBER);
        final EditText txtClave = new EditText(this);
        txtClave.setHint("Clave");
        txtClave.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        final EditText txtMotivo = new EditText(this);
        txtMotivo.setHint("Motivo");
        txtMotivo.setSingleLine(true);
        for (EditText t : new EditText[]{txtCodigo, txtClave, txtMotivo}) {
            estiloCampo(t);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, dp(10), 0, 0);
            caja.addView(t, lp);
        }

        final AlertDialog dlg = new AlertDialog.Builder(this, R.style.CauDialogo)
                .setTitle("Anular foto enviada")
                .setMessage("La foto " + f.archivo.getName() + " ya está en el servidor. Queda marcada como anulada (no se borra) "
                        + "y la anulación registra al supervisor.")
                .setView(caja)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Anular", null)
                .create();
        dlg.setOnShowListener(d -> {
            botonPeligro(dlg);
            dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (!dlg.isShowing()) return; // doble toque después de anular
                String motivo = txtMotivo.getText().toString().trim();
                if (motivo.isEmpty()) {
                    txtMotivo.setError("Escriba el motivo");
                    return;
                }
                ClaveSupervisor.Supervisor s = ClaveSupervisor.validar(
                        new File(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/LECTOR.TXT"),
                        txtCodigo.getText().toString().trim(), txtClave.getText().toString());
                if (s == null) {
                    txtClave.setText("");
                    txtClave.setError("Código o clave de supervisor incorrectos");
                    return;
                }
                dlg.dismiss();
                anular(f, s, motivo);
            });
        });
        dlg.setOnDismissListener(x -> accionAbierta = false);
        accionAbierta = true;
        dlg.show();
    }

    private void anular(Foto f, ClaveSupervisor.Supervisor s, String motivo) {
        String nombre = f.archivo.getName();
        anuladas.guardar(nombre, s.codigo + "|" + System.currentTimeMillis());
        escribirResumen(f.cuenta + ";FOTO ANULADA EN GALERIA;" + ahora() + ";" + nombre + ";supervisor=" + s.codigo + " " + s.nombre
                + ";motivo=" + motivo);
        boolean alServidor = false;
        try {
            if (ConfigSync.modoRest(this)) {
                Map<String, Object> datos = new LinkedHashMap<>();
                datos.put("nombre", nombre);
                datos.put("supervisor", s.codigo);
                datos.put("supervisor_nombre", s.nombre);
                datos.put("motivo", motivo);
                String uid = EventoMovil.nuevoUid();
                String cuenta = quitarCeros(f.cuenta);
                ColaEnvioSqlite.de(this).encolar(ColaEnvio.EVENTO, uid,
                        EventoMovil.armar(uid, EventoMovil.FOTO_ANULADA, cuenta, f.anno, f.mes, System.currentTimeMillis(), null, null, datos), null);
                SincronizacionService.solicitar(this);
                alServidor = true;
            }
        } catch (Exception e) {
            escribirResumen(f.cuenta + ";FOTO ANULADA SIN EVENTO;" + ahora() + ";" + e.getMessage());
        }
        Toast.makeText(this, alServidor ? "Foto anulada. Se avisa al servidor." : "Foto anulada en el celular.", Toast.LENGTH_LONG).show();
        cargar();
    }

    // ------------------------------------------------------------------ apoyo

    private static String quitarCeros(String cuenta) {
        int i = 0;
        while (i < cuenta.length() - 1 && cuenta.charAt(i) == '0') i++;
        return cuenta.substring(i);
    }

    private static String textoEstado(int e) {
        return e == SIN_ENVIAR ? "Sin enviar" : e == ANULADA ? "Anulada" : "Enviada";
    }

    /** Chip del estado: sin enviar en ámbar, anulada en rojo, enviada en verde (paleta de la lectura v2). */
    private static int fondoEstado(int e) {
        return e == SIN_ENVIAR ? R.drawable.bg_lec_chip_aviso : e == ANULADA ? R.drawable.bg_lec_chip_mal : R.drawable.bg_lec_chip_ok;
    }

    private static int textoChip(int e) {
        return e == SIN_ENVIAR ? R.color.lec_banner_nuevo_texto : R.color.lec_tecla_texto;
    }

    private static String ahora() {
        return new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date());
    }

    /** Deja la acción en RESUMENTIEMPO.SDA, como el resto de la liquidación. */
    private void escribirResumen(String linea) {
        try {
            File f = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/RESUMENTIEMPO.SDA");
            try (FileOutputStream out = new FileOutputStream(f, true)) {
                out.write((linea + "\r\n").getBytes(StandardCharsets.ISO_8859_1));
            }
        } catch (Exception ignorado) {
            // sin carpeta de salida (terminal sin ruta)
        }
    }

    private static Bitmap decodificar(File f, int lado) {
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(f.getAbsolutePath(), o);
            int escala = 1;
            while (Math.max(o.outWidth, o.outHeight) / (escala * 2) >= lado) escala *= 2;
            o.inJustDecodeBounds = false;
            o.inSampleSize = escala;
            return BitmapFactory.decodeFile(f.getAbsolutePath(), o);
        } catch (Exception | OutOfMemoryError e) {
            return null;
        }
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    private final class Adaptador extends BaseAdapter {
        @Override
        public int getCount() {
            return visibles.size();
        }

        @Override
        public Object getItem(int i) {
            return visibles.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            LinearLayout fila;
            ImageView img;
            TextView txtCuenta, txtArchivo, txtEstado;
            if (convertView == null) {
                fila = new LinearLayout(GaleriaFotos.this);
                fila.setOrientation(LinearLayout.HORIZONTAL);
                fila.setGravity(Gravity.CENTER_VERTICAL);
                fila.setMinimumHeight(dp(80));
                fila.setPadding(0, dp(8), 0, dp(8));
                fila.setBackgroundResource(R.drawable.bg_bus_item);
                img = new ImageView(GaleriaFotos.this);
                img.setScaleType(ImageView.ScaleType.CENTER_CROP);
                img.setBackgroundResource(R.drawable.bg_gal_miniatura);
                img.setClipToOutline(true);
                fila.addView(img, new LinearLayout.LayoutParams(dp(64), dp(64)));

                LinearLayout col = new LinearLayout(GaleriaFotos.this);
                col.setOrientation(LinearLayout.VERTICAL);
                col.setPadding(dp(12), 0, 0, 0);
                txtCuenta = new TextView(GaleriaFotos.this);
                txtCuenta.setTextSize(16);
                txtCuenta.setTypeface(Typeface.DEFAULT_BOLD);
                txtCuenta.setTextColor(getColor(R.color.lec_texto));
                txtCuenta.setFontFeatureSettings("tnum");
                col.addView(txtCuenta);
                txtArchivo = new TextView(GaleriaFotos.this);
                txtArchivo.setTextSize(13);
                txtArchivo.setTextColor(getColor(R.color.lec_texto2));
                txtArchivo.setFontFeatureSettings("tnum");
                txtArchivo.setSingleLine(true);
                txtArchivo.setEllipsize(android.text.TextUtils.TruncateAt.MIDDLE);
                col.addView(txtArchivo);
                txtEstado = new TextView(GaleriaFotos.this);
                txtEstado.setTextSize(12);
                txtEstado.setTypeface(Typeface.DEFAULT_BOLD);
                txtEstado.setGravity(Gravity.CENTER_VERTICAL);
                txtEstado.setPadding(dp(8), 0, dp(8), 0);
                LinearLayout.LayoutParams lpEstado = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(24));
                lpEstado.setMargins(0, dp(4), 0, 0);
                col.addView(txtEstado, lpEstado);
                fila.addView(col, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                fila.setTag(new Object[]{img, txtCuenta, txtArchivo, txtEstado});
            } else {
                fila = (LinearLayout) convertView;
            }
            Object[] t = (Object[]) fila.getTag();
            img = (ImageView) t[0];
            txtCuenta = (TextView) t[1];
            txtArchivo = (TextView) t[2];
            txtEstado = (TextView) t[3];
            Foto f = visibles.get(pos);
            txtCuenta.setText("Cuenta " + f.cuenta);
            txtArchivo.setText(f.archivo.getName() + " · "
                    + new SimpleDateFormat("dd/MM HH:mm", Locale.US).format(new Date(f.archivo.lastModified())));
            txtEstado.setText(textoEstado(f.estado));
            txtEstado.setBackgroundResource(fondoEstado(f.estado));
            txtEstado.setTextColor(getColor(textoChip(f.estado)));

            String clave = f.archivo.getAbsolutePath();
            img.setTag(clave);
            Bitmap b = miniaturas.get(clave);
            img.setImageBitmap(b);
            if (b == null) {
                final ImageView destino = img;
                hilo.execute(() -> {
                    Bitmap m = decodificar(f.archivo, 160);
                    if (m == null) return;
                    miniaturas.put(clave, m);
                    ui.post(() -> {
                        if (clave.equals(destino.getTag())) destino.setImageBitmap(m);
                    });
                });
            }
            return fila;
        }
    }
}
