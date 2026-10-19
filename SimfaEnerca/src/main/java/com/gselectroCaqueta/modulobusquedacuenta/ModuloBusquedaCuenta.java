package com.gselectroCaqueta.modulobusquedacuenta;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gselectroCaqueta.accesoyseguridad.R;
import com.gselectroCaqueta.accesoyseguridad.VariablesGlobales;
import com.gselectroCaqueta.tablas.IndiceBusquedaClientes;
import com.gsutil.Utils;

import java.io.File;

/**
 * Búsqueda de cliente, con el diseño y el comportamiento de la app de lecturas EMSA:
 * <ul>
 *   <li>criterio en chips: Medidor, Cuenta, Dirección, Nombre (la ruta se retiró);</li>
 *   <li>los resultados se filtran mientras se digita, sobre un índice en memoria
 *       ({@link IndiceBusquedaClientes}) que se arma en segundo plano y se reutiliza en la sesión;</li>
 *   <li>coincidencia en cualquier parte del campo ("4854" encuentra "0174854005"), sin tildes;</li>
 *   <li>un toque sobre el resultado vuelve a la liquidación.</li>
 * </ul>
 * El contrato con MenuDeLiquidacion (BUSQUEDA_REQUEST_CODE) no cambia: extras "idcliente" y "opcion".
 * opcion 1 = medidor (idcliente = primer registro); 6 = cuenta elegida de la lista, 4 = dirección,
 * 5 = nombre (idcliente = posición del cliente); 2 = cuenta completa digitada (búsqueda binaria allá).
 */
public class ModuloBusquedaCuenta extends AppCompatActivity {

    private static final int MAX_RESULTADOS = 30;
    private static final int MIN_CARACTERES = 3;
    private static final long ESPERA_TECLEO_MS = 150;
    private static final int LARGO_CUENTA = 10;

    private RadioButton rbContador, rbCuenta, rbDireccion, rbNombre;
    private EditText txtBuscar;
    private TextView txtTipoBusqueda;
    private TableLayout tabla;

    private final Utils utils = new Utils();
    private File logfile;

    private int opcion = IndiceBusquedaClientes.CAMPO_MEDIDOR;
    private String etiquetaTipoBusqueda = "Buscando por: Medidor";
    private IndiceBusquedaClientes indice;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable tareaFiltrar = this::filtrarEnVivo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modulo_busqueda_cuenta);

        rbContador = findViewById(R.id.rbContador);
        rbCuenta = findViewById(R.id.rbCuenta);
        rbDireccion = findViewById(R.id.rbDireccion);
        rbNombre = findViewById(R.id.rbNombre);
        RadioGroup rgBotones = findViewById(R.id.rgBotones);
        txtBuscar = findViewById(R.id.txtBuscar);
        txtTipoBusqueda = findViewById(R.id.txtTipoBusqueda);
        tabla = findViewById(R.id.tablaResumenDatos);
        Button btnvol = findViewById(R.id.btnvol);

        logfile = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/LOGEVENTOS.LOG");

        rgBotones.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbContador) {
                cambiarTipoEntrada(InputType.TYPE_CLASS_NUMBER, 16, "Buscando por: Medidor", IndiceBusquedaClientes.CAMPO_MEDIDOR);
            } else if (checkedId == R.id.rbCuenta) {
                cambiarTipoEntrada(InputType.TYPE_CLASS_NUMBER, LARGO_CUENTA, "Buscando por: Cuenta", IndiceBusquedaClientes.CAMPO_CUENTA);
            } else if (checkedId == R.id.rbDireccion) {
                cambiarTipoEntrada(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS, 60, "Buscando por: Dirección", IndiceBusquedaClientes.CAMPO_DIRECCION);
            } else if (checkedId == R.id.rbNombre) {
                cambiarTipoEntrada(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS, 48, "Buscando por: Nombre", IndiceBusquedaClientes.CAMPO_NOMBRE);
            }
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
                handler.removeCallbacks(tareaFiltrar);
                handler.postDelayed(tareaFiltrar, ESPERA_TECLEO_MS);
            }
        });

        txtBuscar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId != EditorInfo.IME_ACTION_DONE && actionId != EditorInfo.IME_ACTION_NEXT) return false;
            String valor = txtBuscar.getText().toString().trim();
            if (valor.isEmpty()) {
                VariablesGlobales.datodebusqueda = "0";
                finish();
                return true;
            }
            // Cuenta completa: como antes, se busca directo en la liquidación (IDClientes, binaria).
            if (opcion == IndiceBusquedaClientes.CAMPO_CUENTA && valor.length() == LARGO_CUENTA) {
                devolver(valor, IndiceBusquedaClientes.CAMPO_CUENTA);
                return true;
            }
            handler.removeCallbacks(tareaFiltrar);
            filtrarEnVivo();
            if (tabla.getChildCount() == 1) {
                tabla.getChildAt(0).performClick(); // un solo resultado: se elige
            } else if (tabla.getChildCount() == 0 && valor.length() >= MIN_CARACTERES && indice != null) {
                Toast.makeText(this, "No existe: " + valor, Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        btnvol.setOnClickListener(v -> finish());

        rbContador.setChecked(true);
        cargarIndiceEnSegundoPlano();
    }

    /**
     * Cambia el teclado del campo y obliga al IME a releerlo (en Android 13+ setRawInputType no refresca
     * el teclado ya visible si no se llama restartInput).
     */
    private void cambiarTipoEntrada(int inputType, int maxLength, String etiqueta, int nuevaOpcion) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        txtBuscar.setText("");
        txtBuscar.setInputType(inputType);
        txtBuscar.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(maxLength)});
        etiquetaTipoBusqueda = etiqueta;
        txtTipoBusqueda.setText(etiqueta);
        opcion = nuevaOpcion;
        handler.removeCallbacks(tareaFiltrar);
        tabla.removeAllViews();
        txtBuscar.requestFocus();
        if (imm != null) {
            imm.restartInput(txtBuscar);
            imm.showSoftInput(txtBuscar, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void cargarIndiceEnSegundoPlano() {
        final String dirEntrada = VariablesGlobales.directorioactual + "/DATOSDEENTRADA";
        txtTipoBusqueda.setText("Preparando búsqueda…");
        new Thread(() -> {
            try {
                final IndiceBusquedaClientes cargado = IndiceBusquedaClientes.obtener(dirEntrada);
                runOnUiThread(() -> {
                    if (isFinishing()) return;
                    indice = cargado;
                    txtTipoBusqueda.setText(etiquetaTipoBusqueda);
                    filtrarEnVivo(); // por si ya se había digitado algo
                });
            } catch (Exception e) {
                utils.Log(logfile, "[ModuloBusquedaCuenta] No se pudo construir el indice: " + e.getMessage());
                runOnUiThread(() -> {
                    txtTipoBusqueda.setText(etiquetaTipoBusqueda);
                    Toast.makeText(getApplicationContext(), "No se pudo preparar la búsqueda", Toast.LENGTH_LONG).show();
                });
            }
        }, "indice-busqueda").start();
    }

    /** Filtra en memoria y repinta (≈1 ms con miles de registros), en el hilo principal. */
    private void filtrarEnVivo() {
        tabla.removeAllViews();
        String consulta = txtBuscar.getText().toString().trim();
        if (indice == null) return; // aún cargando: filtrará al terminar
        if (consulta.length() < MIN_CARACTERES) {
            txtTipoBusqueda.setText(etiquetaTipoBusqueda);
            return;
        }
        int[] encontrados = indice.filtrar(consulta, opcion, MAX_RESULTADOS);
        for (int i : encontrados) agregarFila(i);
        int total = indice.totalCoincidencias();
        String resumen = total == 0 ? "Sin resultados"
                : total > encontrados.length ? "Mostrando " + encontrados.length + " de " + total + ": afine la búsqueda"
                : total + (total == 1 ? " resultado" : " resultados");
        txtTipoBusqueda.setText(etiquetaTipoBusqueda + "  ·  " + resumen);
    }

    /** Medidor y dirección en grande (lo que el técnico ve en la calle); nombre y cuenta abajo. */
    private void agregarFila(final int i) {
        View fila = getLayoutInflater().inflate(R.layout.item_busqueda, tabla, false);
        ((TextView) fila.findViewById(R.id.txtItemMedidor)).setText("MED " + indice.getMedidor(i));
        ((TextView) fila.findViewById(R.id.txtItemMotivo)).setText("");
        ((TextView) fila.findViewById(R.id.txtItemDireccion)).setText(indice.getDireccion(i));
        ((TextView) fila.findViewById(R.id.txtItemNombre)).setText(indice.getNombre(i) + "  ·  Cta " + indice.getCuenta(i));
        fila.setClickable(true);
        fila.setOnClickListener(v -> {
            v.setBackgroundColor(Color.rgb(255, 255, 153));
            if (opcion == IndiceBusquedaClientes.CAMPO_MEDIDOR) {
                devolver(String.valueOf(indice.getPrimerRegistro(i)), IndiceBusquedaClientes.CAMPO_MEDIDOR);
            } else {
                // 6 = cuenta ya ubicada aqui (no se repite la binaria); 4/5 = direccion/nombre. Todas por cliente.
                int op = opcion == IndiceBusquedaClientes.CAMPO_CUENTA ? 6 : opcion;
                devolver(String.valueOf(indice.getCliente(i)), op);
            }
        });
        tabla.addView(fila);
    }

    private void devolver(String idcliente, int op) {
        Intent datos = new Intent(this, com.gselectroCaqueta.accesoyseguridad.MenuDeLiquidacion.class);
        datos.putExtra("idcliente", idcliente);
        datos.putExtra("opcion", String.valueOf(op));
        setResult(Activity.RESULT_OK, datos);
        finish();
    }

    /** Punto único de salida: oculta el teclado para que la liquidación no lo herede. */
    @Override
    public void finish() {
        handler.removeCallbacks(tareaFiltrar);
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View foco = getCurrentFocus() != null ? getCurrentFocus() : txtBuscar;
        if (imm != null && foco != null) imm.hideSoftInputFromWindow(foco.getWindowToken(), 0);
        super.finish();
    }
}
