package com.gselectroCaqueta.accesoyseguridad;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.LruCache;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        anuladas = AlmacenEstado.de(this, "fotos_anuladas");
        int pad = dp(12);

        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setBackgroundColor(Color.WHITE);
        raiz.setPadding(pad, pad, pad, 0);

        TextView titulo = new TextView(this);
        titulo.setText("Galería de fotos");
        titulo.setTextSize(20);
        titulo.setTypeface(Typeface.DEFAULT_BOLD);
        titulo.setTextColor(0xFF263238);
        raiz.addView(titulo);

        txtResumen = new TextView(this);
        txtResumen.setTextColor(0xFF607D8B);
        txtResumen.setPadding(0, dp(2), 0, dp(8));
        raiz.addView(txtResumen);

        txtBuscar = new EditText(this);
        txtBuscar.setHint("Buscar cuenta");
        txtBuscar.setSingleLine(true);
        txtBuscar.setInputType(InputType.TYPE_CLASS_NUMBER);
        txtBuscar.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {
            }

            public void onTextChanged(CharSequence s, int a, int b, int c) {
            }

            public void afterTextChanged(Editable s) {
                filtrar();
            }
        });
        raiz.addView(txtBuscar);

        ListView lista = new ListView(this);
        adaptador = new Adaptador();
        lista.setAdapter(adaptador);
        lista.setOnItemClickListener((p, v, pos, id) -> abrir(visibles.get(pos)));
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
        txtResumen.setText(todas.size() + " fotos, " + sinEnviar + " sin enviar"
                + (q.isEmpty() ? "" : " · " + visibles.size() + " de la búsqueda"));
        adaptador.notifyDataSetChanged();
    }

    // ------------------------------------------------------------------ acciones

    private void abrir(Foto f) {
        ImageView img = new ImageView(this);
        img.setAdjustViewBounds(true);
        img.setScaleType(ImageView.ScaleType.FIT_CENTER);
        hilo.execute(() -> {
            Bitmap b = decodificar(f.archivo, 1080);
            ui.post(() -> img.setImageBitmap(b));
        });
        AlertDialog.Builder d = new AlertDialog.Builder(this)
                .setTitle("Cuenta " + f.cuenta + " · " + textoEstado(f.estado))
                .setMessage(f.archivo.getName() + "\n" + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(new Date(f.archivo.lastModified())))
                .setView(img)
                .setNegativeButton("Cerrar", null);
        if (f.estado == SIN_ENVIAR) {
            d.setPositiveButton("Borrar", (x, w) -> confirmarBorrar(f));
        } else if (f.estado == ENVIADA) {
            d.setPositiveButton("Anular", (x, w) -> pedirAnulacion(f));
        }
        d.show();
    }

    private void confirmarBorrar(Foto f) {
        new AlertDialog.Builder(this)
                .setTitle("Borrar foto")
                .setMessage("La foto " + f.archivo.getName() + " no se ha enviado. Se borra del celular y no se enviará.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Borrar", (x, w) -> borrar(f))
                .show();
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
        LinearLayout caja = new LinearLayout(this);
        caja.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(16);
        caja.setPadding(pad, pad / 2, pad, 0);
        final EditText txtCodigo = new EditText(this);
        txtCodigo.setHint("Código del supervisor");
        txtCodigo.setInputType(InputType.TYPE_CLASS_NUMBER);
        final EditText txtClave = new EditText(this);
        txtClave.setHint("Clave");
        txtClave.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        final EditText txtMotivo = new EditText(this);
        txtMotivo.setHint("Motivo");
        txtMotivo.setSingleLine(true);
        caja.addView(txtCodigo);
        caja.addView(txtClave);
        caja.addView(txtMotivo);

        final AlertDialog dlg = new AlertDialog.Builder(this)
                .setTitle("Anular foto enviada")
                .setMessage("La foto " + f.archivo.getName() + " ya está en el servidor. Queda marcada como anulada (no se borra) "
                        + "y la anulación registra al supervisor.")
                .setView(caja)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Anular", null)
                .create();
        dlg.setOnShowListener(d -> dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
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
        }));
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

    private static int colorEstado(int e) {
        return e == SIN_ENVIAR ? 0xFFE65100 : e == ANULADA ? 0xFFC62828 : 0xFF2E7D32;
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
            TextView txt;
            if (convertView == null) {
                fila = new LinearLayout(GaleriaFotos.this);
                fila.setOrientation(LinearLayout.HORIZONTAL);
                fila.setGravity(Gravity.CENTER_VERTICAL);
                fila.setPadding(0, dp(6), 0, dp(6));
                img = new ImageView(GaleriaFotos.this);
                img.setScaleType(ImageView.ScaleType.CENTER_CROP);
                img.setBackgroundColor(0xFFECEFF1);
                fila.addView(img, new LinearLayout.LayoutParams(dp(72), dp(72)));
                txt = new TextView(GaleriaFotos.this);
                txt.setPadding(dp(12), 0, 0, 0);
                txt.setTextColor(0xFF263238);
                fila.addView(txt, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                fila.setTag(new Object[]{img, txt});
            } else {
                fila = (LinearLayout) convertView;
            }
            Object[] t = (Object[]) fila.getTag();
            img = (ImageView) t[0];
            txt = (TextView) t[1];
            Foto f = visibles.get(pos);
            txt.setText(android.text.Html.fromHtml("<b>Cuenta " + f.cuenta + "</b><br>" + f.archivo.getName()
                    + "<br><font color='" + String.format("#%06X", colorEstado(f.estado) & 0xFFFFFF) + "'>" + textoEstado(f.estado) + "</font>",
                    android.text.Html.FROM_HTML_MODE_LEGACY));

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
