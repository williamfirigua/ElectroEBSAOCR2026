package com.gselectroCaqueta.accesoyseguridad;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.gselectroCaqueta.resumen.CargaResumen;
import com.gselectroCaqueta.resumen.EstadoEnvios;
import com.gselectroCaqueta.resumen.LectorFilas;
import com.gselectroCaqueta.resumen.ResumenRuta;
import com.gsutil.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resumen de la ruta antes de liquidar.
 *
 * Contrato con MenuPrincipal (sin cambios): RESULT_OK con el extra "fila" abre la liquidación; "fila" es el
 * CLIENTE (índice en CLIENTE.TXT) porque MenuDeLiquidacion.ejecutarResumen lo usa como cliente, o -1 para abrir
 * en VariablesGlobales.registroactual (primer cliente pendiente, que se publica aquí). Atrás = RESULT_CANCELED.
 *
 * Antes la fila tocada devolvía la posición del registrador, que no coincide con el cliente cuando hay
 * cuentas con más de un registrador (activa + reactiva): se abría otra cuenta.
 *
 * Desde la liquidación (menú lateral, startActivity sin resultado) es solo consulta: no toca VariablesGlobales
 * (la liquidación usa registroactual como posición del registrador) y el botón principal vuelve a ella.
 */
public class ResumenEstadistico extends Activity {

    private static final long ESPERA_BUSQUEDA_MS = 250;

    private final Handler ui = new Handler(Looper.getMainLooper());
    private final Utils utils = new Utils();

    private String directorio;
    private File logfile;

    private TextView txtNombreRuta, txtAviso, txtTotal, txtLeidos, txtPendientes, txtAnomalias, txtAvance;
    private TextView txtConteoLista, txtVacio;
    private ProgressBar barAvance, progresoCarga;
    private RadioGroup grupoOpciones;
    private RadioButton optTodas, optProcesadas, optPendientes, optAnomalias;
    private Spinner spCausa;
    private EditText txtBuscar;
    private ListView lista;
    private Button btnContinuar, btnLiquidarSeleccion;
    private ImageButton btnDetalle;

    private CargaResumen.Resultado resultado;
    private ResumenRuta resumen;
    private LectorFilas lector;
    private final Map<String, String> descripciones = new HashMap<>();
    private final List<ResumenRuta.ConteoCausa> causasSpinner = new ArrayList<>();

    private ResumenRuta.Filtro filtro = ResumenRuta.Filtro.TODAS;
    private String causaElegida;
    private int[] visibles = new int[0];
    private int seleccion; // posición del registrador seleccionado (0 = ninguno)
    /** false si el lector no aceptó seguir con la fecha del equipo descuadrada. */
    private boolean puedeLiquidar = true;
    private boolean primeraCarga = true;
    /** Abierto desde la liquidación: nadie espera el resultado. */
    private boolean soloConsulta;

    private final FilasAdapter adaptador = new FilasAdapter();

    private final Runnable buscar = this::aplicarFiltro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resumen_estadistico);

        Bundle extras = getIntent().getExtras();
        directorio = extras != null ? extras.getString("directorioActual") : null;
        if (directorio == null || directorio.isEmpty()) directorio = VariablesGlobales.directorioactual;
        logfile = new File(directorio + "/DATOSDESALIDA/LOGEVENTOS.LOG");

        txtNombreRuta = findViewById(R.id.txtNombreRuta);
        txtAviso = findViewById(R.id.txtAviso);
        txtTotal = findViewById(R.id.txtTotal);
        txtLeidos = findViewById(R.id.txtLeidos);
        txtPendientes = findViewById(R.id.txtPendientes);
        txtAnomalias = findViewById(R.id.txtAnomalias);
        txtAvance = findViewById(R.id.txtAvance);
        txtConteoLista = findViewById(R.id.txtConteoLista);
        txtVacio = findViewById(R.id.txtVacio);
        barAvance = findViewById(R.id.barAvance);
        progresoCarga = findViewById(R.id.progresoCarga);
        grupoOpciones = findViewById(R.id.GrupoOpciones);
        optTodas = findViewById(R.id.OptTodas);
        optProcesadas = findViewById(R.id.OptProcesadas);
        optPendientes = findViewById(R.id.OptNoprocesada);
        optAnomalias = findViewById(R.id.OptAnomalias);
        spCausa = findViewById(R.id.spCausa);
        txtBuscar = findViewById(R.id.txtBuscar);
        lista = findViewById(R.id.listaRegistros);
        btnContinuar = findViewById(R.id.btnCerrar);
        btnLiquidarSeleccion = findViewById(R.id.btnLiquidarSeleccion);
        btnDetalle = findViewById(R.id.btnDetalle);

        soloConsulta = getCallingActivity() == null;
        lista.setAdapter(adaptador);
        habilitar(btnContinuar, false);
        habilitar(btnLiquidarSeleccion, false);
        if (soloConsulta) {
            btnLiquidarSeleccion.setVisibility(View.GONE);
            btnContinuar.setText("Volver a la liquidación");
        }

        findViewById(R.id.btnVolver).setOnClickListener(v -> volver());
        btnDetalle.setOnClickListener(v -> mostrarDetalle());
        btnContinuar.setOnClickListener(v -> {
            if (soloConsulta) volver();
            else devolver(-1);
        });
        btnLiquidarSeleccion.setOnClickListener(v -> abrirSeleccion());

        grupoOpciones.setOnCheckedChangeListener((g, id) -> {
            if (id == R.id.OptProcesadas) filtro = ResumenRuta.Filtro.PROCESADAS;
            else if (id == R.id.OptNoprocesada) filtro = ResumenRuta.Filtro.PENDIENTES;
            else if (id == R.id.OptAnomalias) filtro = ResumenRuta.Filtro.ANOMALIAS;
            else filtro = ResumenRuta.Filtro.TODAS;
            spCausa.setVisibility(filtro == ResumenRuta.Filtro.ANOMALIAS && !causasSpinner.isEmpty() ? View.VISIBLE : View.GONE);
            aplicarFiltro();
        });

        spCausa.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String nueva = position <= 0 || position > causasSpinner.size() ? null : causasSpinner.get(position - 1).codigo;
                if (nueva == null ? causaElegida != null : !nueva.equals(causaElegida)) {
                    causaElegida = nueva;
                    aplicarFiltro();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
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
                ui.removeCallbacks(buscar);
                ui.postDelayed(buscar, ESPERA_BUSQUEDA_MS);
            }
        });

        lista.setOnItemClickListener((parent, view, index, id) -> tocarFila(index));

        CargaResumen.cargar(this, directorio, this::alCargar);
    }

    @Override
    protected void onDestroy() {
        ui.removeCallbacksAndMessages(null);
        if (lector != null) lector.close();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        volver();
    }

    // ------------------------------------------------------------------ carga

    private void alCargar(CargaResumen.Resultado r) {
        if (isFinishing() || isDestroyed()) return;
        resultado = r;
        progresoCarga.setVisibility(View.GONE);
        txtNombreRuta.setText(textoRuta(r));

        if (!r.hayRuta()) {
            mostrarAviso("No hay ruta cargada" + (r.error == null ? "" : ": " + r.error));
            txtVacio.setText("Cargue una ruta desde Comunicaciones.");
            txtVacio.setVisibility(View.VISIBLE);
            return;
        }
        resumen = r.resumen;
        try {
            lector = new LectorFilas(new File(directorio));
        } catch (IOException e) {
            mostrarAviso("No se pudo abrir la ruta: " + e.getMessage());
            return;
        }
        for (ResumenRuta.ConteoCausa c : resumen.getCausas()) descripciones.put(c.codigo, c.descripcion);

        // Lo que la liquidación espera encontrar (cliente donde abrir y contadores). Desde la liquidación no:
        // ella lleva los contadores y registroactual es allí una posición de registrador.
        if (!soloConsulta) CargaResumen.publicarGlobales(resumen, true);

        pintarIndicadores();
        prepararCausas();
        aplicarFiltro();
        btnDetalle.setEnabled(true);
        habilitar(btnContinuar, true);
        if (!soloConsulta) validarFechaSistema();

        if (resumen.getClientesPendientes() == 0) {
            mostrarAviso("Proceso de facturación concluido: envíe la información y cargue datos nuevos.");
            Toast.makeText(this, "Proceso de facturación concluido debes descargar y cargar datos nuevos", Toast.LENGTH_LONG).show();
        }
    }

    private void pintarIndicadores() {
        txtTotal.setText(String.valueOf(resumen.getTotalClientes()));
        txtLeidos.setText(String.valueOf(resumen.getClientesProcesados()));
        txtPendientes.setText(String.valueOf(resumen.getClientesPendientes()));
        txtAnomalias.setText(String.valueOf(resumen.getRegistrosConCausa()));
        barAvance.setProgress(resumen.getAvance());

        StringBuilder b = new StringBuilder();
        b.append(resumen.getAvance()).append("% de los clientes");
        if (resumen.getProcesadosHoy() > 0) b.append(" · ").append(resumen.getProcesadosHoy()).append(" hoy");
        if (!resumen.getUltimaLectura().isEmpty()) b.append(" · última ").append(hora(resumen.getUltimaLectura()));
        txtAvance.setText(b);

        optTodas.setText("Todas " + resumen.contar(ResumenRuta.Filtro.TODAS));
        optProcesadas.setText("Procesadas " + resumen.contar(ResumenRuta.Filtro.PROCESADAS));
        optPendientes.setText("Pendientes " + resumen.contar(ResumenRuta.Filtro.PENDIENTES));
        optAnomalias.setText("Anomalías " + resumen.contar(ResumenRuta.Filtro.ANOMALIAS));
    }

    private void prepararCausas() {
        causasSpinner.clear();
        causasSpinner.addAll(resumen.getCausas());
        List<String> items = new ArrayList<>(causasSpinner.size() + 1);
        items.add("Todas las causas (" + resumen.getRegistrosConCausa() + ")");
        for (ResumenRuta.ConteoCausa c : causasSpinner) items.add(c.toString());
        ArrayAdapter<String> a = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCausa.setAdapter(a);
    }

    private void aplicarFiltro() {
        if (resumen == null) return;
        visibles = resumen.filtrar(filtro, filtro == ResumenRuta.Filtro.ANOMALIAS ? causaElegida : null,
                txtBuscar.getText().toString());
        adaptador.notifyDataSetChanged();

        int indiceSeleccion = indiceDe(seleccion);
        if (indiceSeleccion < 0) {
            seleccion = 0;
            lista.clearChoices();
            actualizarBotonSeleccion();
        } else {
            lista.setItemChecked(indiceSeleccion, true);
        }

        txtConteoLista.setText(visibles.length + (visibles.length == 1 ? " registrador" : " registradores"));
        if (visibles.length == 0) {
            txtVacio.setText(txtBuscar.getText().length() > 0 ? "Sin coincidencias" : "No hay registradores en este filtro");
            txtVacio.setVisibility(View.VISIBLE);
        } else {
            txtVacio.setVisibility(View.GONE);
        }

        if (primeraCarga) {
            primeraCarga = false;
            int i = indiceDe(resumen.getPrimerRegistroPendiente());
            if (i > 0) lista.setSelection(Math.max(0, i - 1)); // deja ver el anterior al primero pendiente
        }
    }

    private int indiceDe(int posicion) {
        if (posicion <= 0) return -1;
        // visibles está en orden de ruta: búsqueda binaria
        int lo = 0, hi = visibles.length - 1;
        while (lo <= hi) {
            int m = (lo + hi) >>> 1;
            if (visibles[m] < posicion) lo = m + 1;
            else if (visibles[m] > posicion) hi = m - 1;
            else return m;
        }
        return -1;
    }

    // ------------------------------------------------------------------ selección y salida

    private void tocarFila(int index) {
        if (index < 0 || index >= visibles.length) return;
        int posicion = visibles[index];
        if (posicion == seleccion && !soloConsulta) { // segundo toque sobre la misma fila: abrir (como antes)
            abrirSeleccion();
            return;
        }
        seleccion = posicion;
        lista.setItemChecked(index, true);
        actualizarBotonSeleccion();
    }

    private void actualizarBotonSeleccion() {
        LectorFilas.Fila f = seleccion > 0 && lector != null ? lector.fila(seleccion) : null;
        habilitar(btnLiquidarSeleccion, f != null && puedeLiquidar);
        btnLiquidarSeleccion.setText(f == null ? "Ir a la seleccionada" : "Ir a " + f.cuenta);
    }

    private void abrirSeleccion() {
        if (seleccion <= 0 || resumen == null) return;
        int cliente = resumen.clienteDe(seleccion);
        if (cliente <= 0) {
            Toast.makeText(this, "El registrador no tiene cliente asociado", Toast.LENGTH_LONG).show();
            return;
        }
        devolver(cliente);
    }

    private void devolver(int clienteOMenosUno) {
        if (!puedeLiquidar) {
            Toast.makeText(this, "La fecha del equipo no corresponde al proceso: ajústela en Utilitarios", Toast.LENGTH_LONG).show();
            return;
        }
        Intent datos = new Intent(this, MenuPrincipal.class);
        datos.putExtra("fila", String.valueOf(clienteOMenosUno));
        setResult(RESULT_OK, datos);
        finish();
    }

    private void volver() {
        setResult(RESULT_CANCELED);
        finish();
    }

    // ------------------------------------------------------------------ fecha del equipo

    /**
     * El mes del equipo debe ser el del periodo o el siguiente (se factura después del cierre). Antes se
     * comparaban diferencias absolutas de mes y año por separado: avisaba con el periodo de diciembre en enero
     * y no avisaba con un periodo once meses atrás del mismo año.
     */
    private void validarFechaSistema() {
        int anio, mes;
        try {
            anio = Integer.parseInt(resumen.getAnioPeriodo());
            mes = Integer.parseInt(resumen.getMesPeriodo());
        } catch (NumberFormatException e) {
            utils.Log(logfile, "[ResumenEstadistico]validarFechaSistema(); periodo no numérico en CLIENTE.TXT");
            return;
        }
        Calendar hoy = Calendar.getInstance();
        int mesSis = hoy.get(Calendar.MONTH) + 1;
        int anioSis = hoy.get(Calendar.YEAR);
        int diferencia = (anioSis * 12 + mesSis) - (anio * 12 + mes);
        if (diferencia == 0 || diferencia == 1) return;

        final String fechaSis = "Fecha sistema: " + mesSis + "/" + anioSis;
        final String fechaCli = "Fecha cliente: " + mes + "/" + anio;
        new AlertDialog.Builder(this)
                .setTitle("Fecha descuadrada")
                .setMessage("La fecha de su terminal está descuadrada frente al proceso que va a realizar o el proceso es uno pasado.\n"
                        + fechaSis + "\n" + fechaCli + "\n¿Desea continuar?")
                .setCancelable(false)
                .setPositiveButton("SÍ", (d, w) -> utils.Log(logfile,
                        "[ResumenEstadistico]ValFechaSistema(); Decidio proseguir sabiendo que la fecha esta desactualizada o el proceso es uno pasado \n " + fechaSis + "," + fechaCli))
                .setNegativeButton("NO", (d, w) -> {
                    puedeLiquidar = false;
                    habilitar(btnContinuar, false);
                    habilitar(btnLiquidarSeleccion, false);
                    mostrarAviso("Fecha del equipo descuadrada (" + mesSis + "/" + anioSis + " frente a " + mes + "/" + anio
                            + "): ajústela en Utilitarios para liquidar.");
                })
                .show();
    }

    // ------------------------------------------------------------------ detalle

    private void mostrarDetalle() {
        if (resumen == null) return;
        final LinearLayout cont = new LinearLayout(this);
        cont.setOrientation(LinearLayout.VERTICAL);
        int p = dp(16);
        cont.setPadding(p, dp(8), p, dp(8));
        final AlertDialog[] dialogo = new AlertDialog[1];

        seccion(cont, "Clientes");
        fila(cont, "Total", resumen.getTotalClientes());
        fila(cont, "Procesados", resumen.getClientesProcesados() + " (" + resumen.getAvance() + "%)");
        fila(cont, "Pendientes", resumen.getClientesPendientes());
        fila(cont, "Factura impresa", resumen.getClientesImpresos());
        fila(cont, "Liquidada sin imprimir", resumen.getClientesSinImprimir());
        fila(cont, "Solo lectura", resumen.getClientesSoloLectura());
        if (resumen.getClientesIncompletos() > 0) {
            filaColor(cont, "Con registradores sin leer", String.valueOf(resumen.getClientesIncompletos()), 0xFFEF6C00);
        }
        if (resumen.getClientesOtroEstado() > 0) fila(cont, "Otro estado", resumen.getClientesOtroEstado());

        seccion(cont, "Registradores");
        fila(cont, "Total", resumen.getRegistros());
        fila(cont, "Procesados", resumen.getRegistrosProcesados());
        fila(cont, "Pendientes", resumen.getRegistrosPendientes());
        fila(cont, "Con anomalía", resumen.getRegistrosConCausa());
        fila(cont, "Con observación", resumen.getRegistrosConComentario());
        fila(cont, "Con informe", resumen.getRegistrosConInforme());

        seccion(cont, "Crítica de la lectura");
        for (ResumenRuta.Critica c : ResumenRuta.Critica.values()) {
            if (c == ResumenRuta.Critica.PENDIENTE) continue;
            int n = resumen.getPorCritica(c);
            if (n > 0 || c == ResumenRuta.Critica.NORMAL) filaColor(cont, c.etiqueta, String.valueOf(n), colorDe(c));
        }

        seccion(cont, "Anomalías por causa");
        if (resumen.getCausas().isEmpty()) {
            nota(cont, "Sin anomalías registradas.");
        } else {
            nota(cont, "Toque una causa para ver sus registradores.");
            for (final ResumenRuta.ConteoCausa c : resumen.getCausas()) {
                View v = filaColor(cont, c.codigo + (c.descripcion.isEmpty() ? "" : " · " + c.descripcion),
                        String.valueOf(c.cantidad), 0xFFE65100);
                v.setBackgroundResource(android.R.drawable.list_selector_background);
                v.setOnClickListener(x -> {
                    if (dialogo[0] != null) dialogo[0].dismiss();
                    verCausa(c.codigo);
                });
            }
        }

        seccion(cont, "Tiempos");
        fila(cont, "Primera lectura", fechaHora(resumen.getPrimeraLectura()));
        fila(cont, "Última lectura", fechaHora(resumen.getUltimaLectura()));
        fila(cont, "Procesados hoy", resumen.getProcesadosHoy());

        seccion(cont, "Envíos");
        EstadoEnvios e = resultado == null ? null : resultado.envios;
        if (e == null) {
            nota(cont, "No se pudo leer la cola de envío.");
        } else if (!e.modoRest) {
            nota(cont, e.resumenCorto());
        } else {
            fila(cont, "Lecturas enviadas", e.liquidacionesEnviadas);
            fila(cont, "Lecturas por enviar", e.liquidacionesPendientes);
            fila(cont, "Fotos por enviar", e.fotosPendientes);
            fila(cont, "Facturas (LBL) por enviar", e.lblPendientes);
            if (e.rechazados() > 0) filaColor(cont, "Rechazados por el servidor", String.valueOf(e.rechazados()), 0xFFC62828);
        }

        ScrollView scroll = new ScrollView(this);
        scroll.addView(cont);
        dialogo[0] = new AlertDialog.Builder(this)
                .setTitle("Resumen detallado")
                .setView(scroll)
                .setPositiveButton("Cerrar", null)
                .show();
    }

    private void verCausa(String codigo) {
        causaElegida = codigo;
        for (int i = 0; i < causasSpinner.size(); i++) {
            if (causasSpinner.get(i).codigo.equals(codigo)) {
                spCausa.setSelection(i + 1);
                break;
            }
        }
        if (grupoOpciones.getCheckedRadioButtonId() != R.id.OptAnomalias) {
            grupoOpciones.check(R.id.OptAnomalias); // dispara aplicarFiltro
        } else {
            aplicarFiltro();
        }
    }

    private void seccion(LinearLayout cont, String titulo) {
        TextView t = new TextView(this);
        t.setText(titulo);
        t.setTextSize(15);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setTextColor(0xFF1565C0);
        t.setPadding(0, dp(14), 0, dp(4));
        cont.addView(t);
    }

    private void nota(LinearLayout cont, String texto) {
        TextView t = new TextView(this);
        t.setText(texto);
        t.setTextSize(13);
        t.setTextColor(0xFF78909C);
        t.setPadding(0, dp(2), 0, dp(2));
        cont.addView(t);
    }

    private void fila(LinearLayout cont, String etiqueta, Object valor) {
        filaColor(cont, etiqueta, String.valueOf(valor), 0xFF263238);
    }

    private View filaColor(LinearLayout cont, String etiqueta, String valor, int colorValor) {
        LinearLayout f = new LinearLayout(this);
        f.setOrientation(LinearLayout.HORIZONTAL);
        f.setPadding(0, dp(5), 0, dp(5));
        TextView a = new TextView(this);
        a.setText(etiqueta);
        a.setTextSize(14);
        a.setTextColor(0xFF455A64);
        f.addView(a, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView b = new TextView(this);
        b.setText(valor);
        b.setTextSize(14);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setTextColor(colorValor);
        b.setGravity(Gravity.END);
        b.setPadding(dp(8), 0, 0, 0);
        f.addView(b, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        cont.addView(f);
        return f;
    }

    // ------------------------------------------------------------------ lista

    private final class FilasAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return visibles.length;
        }

        @Override
        public Object getItem(int index) {
            return visibles[index];
        }

        @Override
        public long getItemId(int index) {
            return visibles[index];
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int index, View convertView, ViewGroup parent) {
            Vista v;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_registro_resumen, parent, false);
                v = new Vista(convertView);
                convertView.setTag(v);
            } else {
                v = (Vista) convertView.getTag();
            }
            v.pintar(visibles[index]);
            return convertView;
        }
    }

    private final class Vista {
        final View franja;
        final android.widget.ImageView icono;
        final TextView cuenta, lectura, nombre, direccion, medidor, estado, causa, informe;
        final GradientDrawable fondoEstado = new GradientDrawable();

        Vista(View raiz) {
            franja = raiz.findViewById(R.id.vFranja);
            icono = raiz.findViewById(R.id.imgCritica);
            cuenta = raiz.findViewById(R.id.txtCuenta);
            lectura = raiz.findViewById(R.id.txtLectura);
            nombre = raiz.findViewById(R.id.txtNombre);
            direccion = raiz.findViewById(R.id.txtDireccion);
            medidor = raiz.findViewById(R.id.txtMedidor);
            estado = raiz.findViewById(R.id.txtEstado);
            causa = raiz.findViewById(R.id.txtCausa);
            informe = raiz.findViewById(R.id.txtInforme);
            fondoEstado.setCornerRadius(dp(10));
            estado.setBackground(fondoEstado);
        }

        void pintar(int posicion) {
            ResumenRuta.Critica c = resumen.criticaDe(posicion);
            int color = colorDe(c);
            franja.setBackgroundColor(color);
            int res = iconoDe(c);
            if (res == 0) {
                icono.setVisibility(View.GONE);
            } else {
                icono.setImageResource(res);
                icono.setVisibility(View.VISIBLE);
            }
            estado.setText(c.etiqueta);
            estado.setTextColor(color);
            fondoEstado.setColor(Color.argb(36, Color.red(color), Color.green(color), Color.blue(color)));

            LectorFilas.Fila f = lector == null ? null : lector.fila(posicion);
            if (f == null) {
                cuenta.setText("Registro " + posicion);
                lectura.setText("");
                nombre.setText("No se pudo leer");
                direccion.setText("");
                medidor.setText("");
                causa.setVisibility(View.GONE);
                informe.setVisibility(View.GONE);
                return;
            }
            cuenta.setText("Cuenta " + f.cuenta);
            lectura.setText(c == ResumenRuta.Critica.PENDIENTE || f.lectura.isEmpty() ? "" : "Lect. " + f.lectura);
            nombre.setText(f.nombre.isEmpty() ? "(sin nombre)" : f.nombre);
            direccion.setText(f.ruta.isEmpty() ? f.direccion : f.direccion + " · Ruta " + f.ruta);
            medidor.setText("Medidor " + f.medidor + "  ·  #" + posicion);

            String cod = resumen.causaDe(posicion);
            if (cod != null) {
                String d = descripciones.get(cod);
                causa.setText("Causa " + cod + (d == null || d.isEmpty() ? "" : " · " + d));
                causa.setVisibility(View.VISIBLE);
            } else {
                causa.setVisibility(View.GONE);
            }
            String obs = f.comentario.isEmpty() ? "" : "Obs. " + f.comentario;
            String inf = f.informe.isEmpty() ? obs : (obs.isEmpty() ? f.informe : obs + " · " + f.informe);
            if (inf.isEmpty()) {
                informe.setVisibility(View.GONE);
            } else {
                informe.setText(inf);
                informe.setVisibility(View.VISIBLE);
            }
        }
    }

    // ------------------------------------------------------------------ utilidades

    /** Los mismos íconos que pone la liquidación (evaluarLecturaRegistro) en la barra al detectar la desviación. */
    static int iconoDe(ResumenRuta.Critica c) {
        switch (c) {
            case ALTA: return R.drawable.imagen_consumoalto;
            case BAJA: return R.drawable.imagen_consumobajo;
            case IGUAL: return R.drawable.imagen_lecturasiguales;
            case NEGATIVA: return R.drawable.flechasatras;
            default: return 0;
        }
    }

    static int colorDe(ResumenRuta.Critica c) {
        switch (c) {
            case NORMAL: return 0xFF2E7D32;
            case BAJA: return 0xFFF9A825;
            case IGUAL: return 0xFF607D8B;
            case ALTA: return 0xFFC62828;
            case NEGATIVA: return 0xFF6A1B9A;
            case SIN_LECTURA: return 0xFFEF6C00;
            case PROCESADA: return 0xFF1565C0;
            default: return 0xFF9E9E9E;
        }
    }

    private static String textoRuta(CargaResumen.Resultado r) {
        if (r.ciclo.isEmpty() && r.ruta.isEmpty()) return "Ruta no cargada";
        StringBuilder b = new StringBuilder("CIC").append(r.ciclo).append(" · Ruta ").append(r.ruta);
        if (!r.municipio.isEmpty()) b.append(" · ").append(r.municipio);
        if (r.resumen != null && !r.resumen.getMesPeriodo().isEmpty()) {
            b.append(" · ").append(r.resumen.getMesPeriodo()).append('/').append(r.resumen.getAnioPeriodo());
        }
        return b.toString();
    }

    private void mostrarAviso(String texto) {
        txtAviso.setText(texto);
        txtAviso.setVisibility(View.VISIBLE);
    }

    private static void habilitar(Button b, boolean si) {
        b.setEnabled(si);
        b.setAlpha(si ? 1f : 0.4f);
    }

    /** AAAAMMDDhhmmss -> hh:mm */
    private static String hora(String m) {
        return m.length() < 12 ? "" : m.substring(8, 10) + ":" + m.substring(10, 12);
    }

    /** AAAAMMDDhhmmss -> dd/mm/aaaa hh:mm */
    private static String fechaHora(String m) {
        if (m.length() < 12) return "—";
        return m.substring(6, 8) + "/" + m.substring(4, 6) + "/" + m.substring(0, 4) + " " + hora(m);
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
