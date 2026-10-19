package com.gselectroCaqueta.modulobusquedacuenta;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.gselectroCaqueta.accesoyseguridad.R;
import com.gselectroCaqueta.tablas.TablaEntradaClientes;
import com.gselectroCaqueta.tablas.TablaMedidorEntrada;

import java.io.File;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Búsqueda de cliente en un solo campo, en un diálogo sobre la liquidación.
 * <p>
 * Reemplaza a la Activity {@link ModuloBusquedaCuenta} (botones Contador/Cuenta/Ruta/Dirección/Nombre y
 * startActivityForResult). Lo digitado se busca a la vez en medidor, cuenta, nombre y dirección; la ruta no
 * se busca. El índice (MEDIDOR.TXT + CLIENTE.TXT) se arma una vez por sesión en segundo plano y se reutiliza
 * mientras los archivos no cambien; el filtrado es en memoria.
 * <p>
 * El resultado se devuelve con la misma semántica que usaba MenuDeLiquidacion.buscarCuentaMedidor:
 * <ul>
 *   <li>coincidencia por medidor: {@code tipo = 1}, {@code dato = primer registro del medidor};</li>
 *   <li>coincidencia por cuenta, nombre o dirección: {@code tipo = 5}, {@code dato = posición del cliente}.</li>
 * </ul>
 */
public final class DialogoBusqueda {

    public interface Callback {
        /** @param dato valor para VariablesGlobales.datodebusqueda; @param tipo para VariablesGlobales.tipodebusqueda */
        void alElegir(String dato, int tipo);
    }

    static final int TIPO_POR_MEDIDOR = 1;
    static final int TIPO_POR_CLIENTE = 5;
    private static final int MAX_RESULTADOS = 60;
    private static final int MIN_CARACTERES = 3;
    private static final long ESPERA_TECLEO_MS = 200;

    /** Un medidor de la ruta con los datos de su cliente. */
    static final class Entrada {
        final String medidor;
        final String cuenta;
        final String nombre;
        final String direccion;
        final String primerRegistro;
        final String cliente;
        final String medidorBusq;
        final String cuentaBusq;
        final String nombreBusq;
        final String direccionBusq;

        Entrada(String medidor, String cuenta, String nombre, String direccion, String primerRegistro, String cliente) {
            this.medidor = medidor.trim();
            this.cuenta = cuenta.trim();
            this.nombre = nombre.trim();
            this.direccion = direccion.trim();
            this.primerRegistro = primerRegistro.trim();
            this.cliente = cliente.trim();
            this.medidorBusq = sinCeros(this.medidor);
            this.cuentaBusq = sinCeros(this.cuenta);
            this.nombreBusq = normalizar(this.nombre);
            this.direccionBusq = normalizar(this.direccion);
        }
    }

    /** Resultado: la entrada y por qué campo coincidió. */
    static final class Resultado {
        final Entrada e;
        final int tipo;
        final String motivo;

        Resultado(Entrada e, int tipo, String motivo) {
            this.e = e;
            this.tipo = tipo;
            this.motivo = motivo;
        }
    }

    // ---------------------------------------------------------------- índice en memoria

    private static final ExecutorService HILO = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "busqueda-clientes");
        t.setDaemon(true);
        return t;
    });

    private static List<Entrada> indice = Collections.emptyList();
    private static String indiceFirma;

    private static String firma(File medidores, File clientes) {
        return medidores.getAbsolutePath() + "|" + medidores.lastModified() + "|" + medidores.length()
                + "|" + clientes.lastModified() + "|" + clientes.length();
    }

    /** Arma (o reutiliza) el índice. Corre en el hilo de búsqueda, con instancias propias de las tablas. */
    static synchronized List<Entrada> cargarIndice(String dirEntrada) {
        File fMed = new File(dirEntrada, "MEDIDOR.TXT");
        File fCli = new File(dirEntrada, "CLIENTE.TXT");
        String f = firma(fMed, fCli);
        if (f.equals(indiceFirma)) return indice;

        List<Entrada> lista = new ArrayList<>();
        TablaMedidorEntrada med = new TablaMedidorEntrada();
        TablaEntradaClientes cli = new TablaEntradaClientes();
        boolean medAbierto = false;
        boolean cliAbierto = false;
        try {
            medAbierto = med.abrir_TablaMedidorEntrada(fMed.getAbsolutePath());
            cliAbierto = cli.abrir_TablaEntradaClientes(fCli.getAbsolutePath());
            if (medAbierto && cliAbierto) {
                int totalClientes = cli.getTotal_TablaEntradaClientes();
                for (int i = 1; i <= med.getTotal_TablaMedidorEntrada(); i++) {
                    med.lectura_TablaMedidorEntrada(i);
                    String posCliente = med.gettablaMedidorEntrada_cliente().trim();
                    String nombre = "";
                    String direccion = "";
                    int pc = aEntero(posCliente);
                    if (pc >= 1 && pc <= totalClientes) {
                        cli.lectura_TablaEntradaClientes(pc);
                        nombre = cli.gettablaEntradaClientes_Nombre();
                        direccion = cli.gettablaEntradaClientes_Direccion();
                    }
                    lista.add(new Entrada(med.gettablaMedidorEntrada_NUMero(), med.gettablaMedidorEntrada_Cuenta(),
                            nombre, direccion, med.gettablaMedidorEntrada_primerregistro(), posCliente));
                }
            }
        } finally {
            if (medAbierto) med.Cerrar_TablaMedidorEntrada();
            if (cliAbierto) cli.Cerrar_TablaEntradaClientes();
        }
        indice = Collections.unmodifiableList(lista);
        indiceFirma = f;
        return indice;
    }

    /** Precarga el índice en segundo plano (llamar al abrir la liquidación) para que la primera búsqueda sea inmediata. */
    public static void precargar(final String dirEntrada) {
        HILO.execute(() -> {
            try {
                cargarIndice(dirEntrada);
            } catch (Throwable ignored) {
            }
        });
    }

    // ---------------------------------------------------------------- filtro

    static String normalizar(String s) {
        String n = Normalizer.normalize(s == null ? "" : s, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return n.toUpperCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }

    static String sinCeros(String s) {
        String t = s == null ? "" : s.trim();
        int i = 0;
        while (i < t.length() - 1 && t.charAt(i) == '0') i++;
        return t.substring(i);
    }

    private static int aEntero(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return -1;
        }
    }

    private static boolean soloDigitos(String s) {
        if (s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) if (!Character.isDigit(s.charAt(i))) return false;
        return true;
    }

    /**
     * Números: medidor o cuenta (exacto primero, luego contiene). Texto: nombre o dirección; todas las
     * palabras digitadas deben aparecer ("CL 5 23" encuentra "CALLE 5 # 23-10" si contiene "CL", "5" y "23").
     */
    static List<Resultado> filtrar(List<Entrada> todas, String consulta) {
        String q = normalizar(consulta);
        List<Resultado> exactos = new ArrayList<>();
        List<Resultado> otros = new ArrayList<>();
        if (q.length() < MIN_CARACTERES) return exactos;
        if (soloDigitos(q)) {
            String n = sinCeros(q);
            for (Entrada e : todas) {
                if (e.medidorBusq.equals(n)) exactos.add(new Resultado(e, TIPO_POR_MEDIDOR, "Medidor"));
                else if (e.cuentaBusq.equals(n)) exactos.add(new Resultado(e, TIPO_POR_CLIENTE, "Cuenta"));
                else if (e.medidorBusq.contains(n)) otros.add(new Resultado(e, TIPO_POR_MEDIDOR, "Medidor"));
                else if (e.cuentaBusq.contains(n)) otros.add(new Resultado(e, TIPO_POR_CLIENTE, "Cuenta"));
                if (exactos.size() + otros.size() >= MAX_RESULTADOS * 2) break;
            }
        } else {
            String[] palabras = q.split(" ");
            for (Entrada e : todas) {
                if (contieneTodas(e.direccionBusq, palabras)) otros.add(new Resultado(e, TIPO_POR_CLIENTE, "Dirección"));
                else if (contieneTodas(e.nombreBusq, palabras)) otros.add(new Resultado(e, TIPO_POR_CLIENTE, "Nombre"));
                if (otros.size() >= MAX_RESULTADOS) break;
            }
        }
        exactos.addAll(otros);
        return exactos.size() > MAX_RESULTADOS ? new ArrayList<>(exactos.subList(0, MAX_RESULTADOS)) : exactos;
    }

    private static boolean contieneTodas(String texto, String[] palabras) {
        for (String p : palabras) if (!p.isEmpty() && !texto.contains(p)) return false;
        return true;
    }

    // ---------------------------------------------------------------- diálogo

    /** Muestra el diálogo (hilo UI). {@code dirEntrada} = directorio DATOSDEENTRADA de la ruta. */
    public static AlertDialog mostrar(final Activity activity, final String dirEntrada, final Callback callback) {
        View contenido = LayoutInflater.from(activity).inflate(R.layout.dialogo_busqueda, null, false);
        final EditText txt = contenido.findViewById(R.id.txtBusquedaUnica);
        final ListView lista = contenido.findViewById(R.id.listaBusqueda);
        final TextView estado = contenido.findViewById(R.id.txtEstadoBusqueda);

        final List<Resultado> resultados = new ArrayList<>();
        final BaseAdapter adaptador = new BaseAdapter() {
            @Override
            public int getCount() {
                return resultados.size();
            }

            @Override
            public Object getItem(int position) {
                return resultados.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView != null ? convertView
                        : LayoutInflater.from(activity).inflate(R.layout.item_busqueda, parent, false);
                Resultado r = resultados.get(position);
                ((TextView) v.findViewById(R.id.txtItemMedidor)).setText("MED " + r.e.medidor);
                ((TextView) v.findViewById(R.id.txtItemMotivo)).setText(r.motivo);
                ((TextView) v.findViewById(R.id.txtItemDireccion)).setText(r.e.direccion);
                ((TextView) v.findViewById(R.id.txtItemNombre)).setText(r.e.nombre + "  ·  Cta " + r.e.cuenta);
                return v;
            }
        };
        lista.setAdapter(adaptador);

        final AlertDialog dialogo = new AlertDialog.Builder(activity)
                .setTitle("Buscar cliente")
                .setView(contenido)
                .setNegativeButton("Cerrar", null)
                .create();

        final Handler ui = new Handler(Looper.getMainLooper());
        final Future<?>[] enCurso = {null};
        final int[] generacion = {0};

        final Runnable buscar = () -> {
            final String consulta = txt.getText().toString();
            final int gen = ++generacion[0];
            if (normalizar(consulta).length() < MIN_CARACTERES) {
                resultados.clear();
                adaptador.notifyDataSetChanged();
                estado.setText("Digite al menos " + MIN_CARACTERES + " caracteres: medidor, cuenta, nombre o dirección");
                return;
            }
            estado.setText("Buscando…");
            if (enCurso[0] != null) enCurso[0].cancel(false);
            enCurso[0] = HILO.submit(() -> {
                final List<Resultado> r = filtrar(cargarIndice(dirEntrada), consulta);
                ui.post(() -> {
                    if (gen != generacion[0] || !dialogo.isShowing()) return; // llegó una búsqueda más nueva
                    resultados.clear();
                    resultados.addAll(r);
                    adaptador.notifyDataSetChanged();
                    lista.setSelection(0);
                    estado.setText(r.isEmpty() ? "Sin resultados para \"" + consulta.trim() + "\""
                            : r.size() >= MAX_RESULTADOS ? "Primeros " + MAX_RESULTADOS + " resultados: afine la búsqueda"
                            : r.size() + (r.size() == 1 ? " resultado" : " resultados"));
                });
            });
        };

        txt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                ui.removeCallbacks(buscar);
                ui.postDelayed(buscar, ESPERA_TECLEO_MS);
            }
        });

        txt.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                ui.removeCallbacks(buscar);
                if (resultados.size() == 1) elegir(dialogo, resultados.get(0), callback);
                else buscar.run();
                return true;
            }
            return false;
        });

        lista.setOnItemClickListener((parent, view, position, id) -> elegir(dialogo, resultados.get(position), callback));

        dialogo.setOnDismissListener(d -> {
            ui.removeCallbacks(buscar);
            generacion[0]++;
        });

        estado.setText("Medidor, cuenta, nombre o dirección");
        dialogo.show();
        precargar(dirEntrada);
        txt.requestFocus();
        if (dialogo.getWindow() != null) {
            dialogo.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
                    | android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        return dialogo;
    }

    private static void elegir(AlertDialog dialogo, Resultado r, Callback callback) {
        dialogo.dismiss();
        String dato = r.tipo == TIPO_POR_MEDIDOR ? r.e.primerRegistro : r.e.cliente;
        callback.alElegir(String.valueOf(aEntero(dato)), r.tipo);
    }
}
