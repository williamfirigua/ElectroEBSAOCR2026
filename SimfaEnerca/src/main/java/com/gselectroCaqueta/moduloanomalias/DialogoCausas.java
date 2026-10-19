package com.gselectroCaqueta.moduloanomalias;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.gselectroCaqueta.accesoyseguridad.R;

import java.io.File;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Selección de la causa de no lectura en un diálogo sobre la misma pantalla de liquidación.
 * <p>
 * Reemplaza a la Activity {@link ModuloDeAnomalias} (startActivityForResult 6676): no hay cambio de
 * pantalla, la lista se lee de CAUSA_NL.TXT una sola vez por sesión y se filtra mientras se digita.
 * <ul>
 *   <li>Digitar números filtra por código; digitar letras filtra por descripción (sin tildes).</li>
 *   <li>Un toque selecciona; un segundo toque sobre la misma causa, "Aceptar" o la tecla Listo la confirman.</li>
 *   <li>Lo que se devuelve es idéntico a lo que devolvía ModuloDeAnomalias: el CODIGO tal como viene en
 *       el archivo (3 caracteres) si se eligió de la lista, o {@code " " + digitado} si se digitó un código
 *       que no está en la lista (MenuDeLiquidacion lo valida contra la tabla, como antes).</li>
 * </ul>
 */
public final class DialogoCausas {

    public interface Callback {
        /** @param snumero mismo valor que el extra "snumero" que devolvía ModuloDeAnomalias. */
        void alElegir(String snumero);

        void alCancelar();
    }

    /** Una causa del catálogo. */
    static final class Causa {
        final String codigo;       // tal cual en el archivo (3 caracteres)
        final String codigoLimpio; // sin espacios
        final String descripcion;
        final String descripcionBusqueda;

        Causa(String codigo, String descripcion) {
            this.codigo = codigo;
            this.codigoLimpio = sinCerosIzquierda(codigo);
            this.descripcion = descripcion.trim();
            this.descripcionBusqueda = normalizar(this.descripcion);
        }

        @Override
        public String toString() {
            return codigoLimpio + " - " + descripcion;
        }
    }

    // Catálogo en memoria; se relee solo si CAUSA_NL.TXT cambia (fecha o tamaño).
    private static List<Causa> catalogo = Collections.emptyList();
    private static String catalogoRuta;
    private static long catalogoModificado = -1;
    private static long catalogoTamano = -1;

    private DialogoCausas() {
    }

    static synchronized List<Causa> cargarCatalogo(String rutaCausas) {
        File f = new File(rutaCausas);
        if (rutaCausas.equals(catalogoRuta) && f.lastModified() == catalogoModificado && f.length() == catalogoTamano) {
            return catalogo;
        }
        List<Causa> lista = new ArrayList<>();
        AnomaliaDeNoLectura tabla = new AnomaliaDeNoLectura();
        if (tabla.abrir_AnomaliaDeNoLectura(rutaCausas)) {
            try {
                for (int i = 1; i <= tabla.getTotal_AnomaliaDeNoLectura(); i++) {
                    tabla.lectura_AnomaliaDeNoLectura(i);
                    String codigo = tabla.getanomaliaDeNoLectura_CODIGO();
                    if (codigo == null || codigo.trim().isEmpty()) continue;
                    lista.add(new Causa(codigo.toUpperCase(Locale.ROOT), tabla.getanomaliaDeNoLectura_DESCRIPCION()));
                }
            } finally {
                tabla.Cerrar_AnomaliaDeNoLectura();
            }
        }
        catalogo = Collections.unmodifiableList(lista);
        catalogoRuta = rutaCausas;
        catalogoModificado = f.lastModified();
        catalogoTamano = f.length();
        return catalogo;
    }

    static String normalizar(String s) {
        String n = Normalizer.normalize(s == null ? "" : s, Normalizer.Form.NFD);
        return n.replaceAll("\\p{M}+", "").toUpperCase(Locale.ROOT).trim();
    }

    /** "009" -> "9", "000" -> "0": CAUSA_NL.TXT guarda los codigos con ceros a la izquierda. */
    static String sinCerosIzquierda(String s) {
        return AnomaliaDeNoLectura.normalizarCodigo(s);
    }

    private static boolean soloDigitos(String s) {
        if (s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) if (!Character.isDigit(s.charAt(i))) return false;
        return true;
    }

    /** Filtro: por código si se digitan números (coincidencia exacta primero), por descripción si no. */
    static List<Causa> filtrar(List<Causa> todas, String consulta) {
        String q = normalizar(consulta);
        if (q.isEmpty()) return new ArrayList<>(todas);
        List<Causa> exactas = new ArrayList<>();
        List<Causa> resto = new ArrayList<>();
        boolean porCodigo = soloDigitos(q);
        if (porCodigo) q = sinCerosIzquierda(q);
        for (Causa c : todas) {
            if (porCodigo) {
                if (c.codigoLimpio.equals(q)) exactas.add(c);
                else if (c.codigoLimpio.startsWith(q)) resto.add(c);
            } else if (c.descripcionBusqueda.contains(q)) {
                resto.add(c);
            }
        }
        exactas.addAll(resto);
        return exactas;
    }

    /**
     * Muestra el diálogo. Debe llamarse en el hilo UI.
     *
     * @param rutaCausas ruta de CAUSA_NL.TXT
     */
    public static AlertDialog mostrar(Activity activity, String rutaCausas, final Callback callback) {
        final List<Causa> todas = cargarCatalogo(rutaCausas);
        final List<Causa> visibles = new ArrayList<>(todas);

        View contenido = LayoutInflater.from(activity).inflate(R.layout.dialogo_causas, null, false);
        final EditText txtBuscar = contenido.findViewById(R.id.txtBuscarCausa);
        final ListView lista = contenido.findViewById(R.id.listaCausas);
        final TextView txtElegida = contenido.findViewById(R.id.txtCausaElegida);

        final ArrayAdapter<Causa> adaptador = new ArrayAdapter<>(activity, R.layout.item_causa, visibles);
        lista.setAdapter(adaptador);

        final Causa[] elegida = {null};
        final boolean[] respondido = {false};

        final AlertDialog dialogo = new AlertDialog.Builder(activity)
                .setTitle("Causa de no lectura")
                .setView(contenido)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Aceptar", null) // se reemplaza en onShow para no cerrar sin selección
                .create();

        final Runnable confirmar = () -> {
            String digitado = txtBuscar.getText().toString().trim();
            String snumero;
            if (elegida[0] != null) {
                snumero = elegida[0].codigo;
            } else if (visibles.size() == 1) {
                snumero = visibles.get(0).codigo;
            } else if (!digitado.isEmpty() && soloDigitos(digitado)) {
                // Codigo digitado que no esta en la lista: MenuDeLiquidacion lo valida (busqueda normalizada) y
                // si no existe lo informa; si existe usa el codigo del archivo.
                snumero = digitado;
            } else {
                txtElegida.setText(visibles.isEmpty() ? "Sin coincidencias" : "Toque una causa de la lista");
                return;
            }
            respondido[0] = true;
            dialogo.dismiss();
            callback.alElegir(snumero);
        };

        lista.setOnItemClickListener((parent, view, position, id) -> {
            Causa c = visibles.get(position);
            if (c == elegida[0]) { // segundo toque sobre la misma causa: confirmar
                confirmar.run();
                return;
            }
            elegida[0] = c;
            lista.setItemChecked(position, true);
            txtElegida.setText(c.toString());
        });

        txtBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                visibles.clear();
                visibles.addAll(filtrar(todas, s.toString()));
                adaptador.notifyDataSetChanged();
                lista.clearChoices();
                elegida[0] = null;
                // Coincidencia exacta por código: queda seleccionada para confirmar con un toque.
                String q = s.toString().trim();
                if (!visibles.isEmpty() && soloDigitos(q) && visibles.get(0).codigoLimpio.equals(sinCerosIzquierda(q))) {
                    elegida[0] = visibles.get(0);
                    lista.setItemChecked(0, true);
                    txtElegida.setText(elegida[0].toString());
                } else {
                    txtElegida.setText(visibles.isEmpty()
                            ? (soloDigitos(q) ? "Código " + q + " no está en la lista" : "Sin coincidencias")
                            : "Toque una causa o digite el código");
                }
                lista.setSelection(0);
            }
        });

        txtBuscar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                confirmar.run();
                return true;
            }
            return false;
        });

        dialogo.setOnShowListener(d -> {
            Button aceptar = dialogo.getButton(AlertDialog.BUTTON_POSITIVE);
            aceptar.setOnClickListener(v -> confirmar.run());
        });
        dialogo.setOnDismissListener(d -> {
            if (!respondido[0]) {
                respondido[0] = true;
                callback.alCancelar();
            }
        });

        if (todas.isEmpty()) {
            txtElegida.setText("No se pudo leer " + new File(rutaCausas).getName() + ". Digite el código.");
        }
        // Teclado oculto al abrir (lo normal es tocar la causa); si se toca el buscador, la ventana se
        // ajusta para que los botones no queden debajo del teclado.
        if (dialogo.getWindow() != null) {
            dialogo.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                    | android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        dialogo.show();
        return dialogo;
    }
}
