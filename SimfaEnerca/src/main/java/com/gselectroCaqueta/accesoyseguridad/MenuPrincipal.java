package com.gselectroCaqueta.accesoyseguridad;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.gselectroCaqueta.comunicaciones.Comunicaciones;
import com.gselectroCaqueta.resumen.CargaResumen;
import com.gselectroCaqueta.resumen.EstadoEnvios;
import com.gselectroCaqueta.resumen.ResumenRuta;
import com.gselectroCaqueta.sync.SincronizacionService;
import com.gsutil.Utils;
import com.gsutil.WSSoap;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Menú principal: operador, avance de la ruta, estado de los envíos y acciones.
 *
 * Los conteos salen de {@link CargaResumen} (una pasada en segundo plano, la misma definición que el resumen) y
 * se recalculan cada vez que la pantalla vuelve al frente; antes se calculaban una sola vez en onCreate, en el
 * hilo de la pantalla, y al volver de la liquidación "Eliminar datos" decidía con conteos viejos.
 *
 * Se retiró el código sin uso (fotos de novedades y certificados, envío SOAP de aforos/novedades, GPS propio):
 * nada lo invocaba y EBSA no usa novedades ni aforos. Antes, sin permiso de ubicación, onCreate salía antes de
 * conectar los botones y el menú quedaba sin respuesta.
 */
public class MenuPrincipal extends AppCompatActivity {

    public final int RESUMEN_REQUEST_CODE = 3203;
    private static final long VIGILAR_ENVIO_MS = 3000;
    private static final long SEGUIR_ENVIO_MS = 45000;

    private final Utils utils = new Utils();
    private final Handler ui = new Handler(Looper.getMainLooper());
    public File logfile;
    private BDifNull bdl;

    private String aforador = "";
    private String nivelOperador = "";
    private String terminal = "";
    private String nombreOperador = "Administrador";

    // DIRECCIONEIP.TXT / ArchivosCarga.cfi (verificación de borrado contra el servidor SOAP)
    private String URL = "";
    private String paginaWs = "";
    private String esComprimido = "";
    private String rutaAdministrador = "";

    // Archivo NOMBRE
    private String Ciclo = "0000";
    private String Municipio = "000";
    private String Seccion = "000";
    private String Division = "0";

    private TextView txtNombre, txtOperador, txtVersion, txtRuta, txtPrg;
    private TextView txtAvance, txtMenuUltima, txtEnvios, txtInsignia, txtTrabajando;
    private View panelAvance;
    private ProgressBar barAvance, progresoMenu;

    private CargaResumen.Resultado resultado;
    private boolean enPrimerPlano;
    private boolean verificandoBorrado;
    private boolean calculando;
    /** Hasta cuándo seguir mirando la cola después de "Enviar" (los conteos bajan mientras el servicio envía). */
    private long seguirEnvioHasta;

    private final Runnable vigilarEnvio = new Runnable() {
        @Override
        public void run() {
            if (!enPrimerPlano) return;
            boolean enviando = SincronizacionService.corriendo();
            if (enviando || System.currentTimeMillis() < seguirEnvioHasta) {
                refrescar();
                ui.postDelayed(this, VIGILAR_ENVIO_MS);
            } else {
                seguirEnvioHasta = 0;
                actualizarTrabajo();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal);
        bdl = new BDifNull(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            aforador = valor(bundle.getString("codigoUsuario"));
            nivelOperador = valor(bundle.getString("nivelOperador"));
            terminal = valor(bundle.getString("IMEI"));
            String nombre = bundle.getString("NombreOperador");
            if (nombre != null && !nombre.trim().isEmpty()) nombreOperador = nombre.trim();
        }
        logfile = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/LOGEVENTOS.LOG");

        txtNombre = findViewById(R.id.txt_nombre);
        txtOperador = findViewById(R.id.txtOperador);
        txtVersion = findViewById(R.id.txtVersion);
        txtRuta = findViewById(R.id.txtRuta);
        txtPrg = findViewById(R.id.txtPrg);
        txtAvance = findViewById(R.id.txtAvanceMenu);
        txtMenuUltima = findViewById(R.id.txtMenuUltima);
        txtEnvios = findViewById(R.id.txtEnvios);
        txtInsignia = findViewById(R.id.txtInsigniaEnvios);
        txtTrabajando = findViewById(R.id.txtTrabajando);
        panelAvance = findViewById(R.id.panelAvance);
        barAvance = findViewById(R.id.barAvanceMenu);
        progresoMenu = findViewById(R.id.progresoMenu);

        txtNombre.setText(nombreOperador);
        txtOperador.setText(textoOperador());
        txtVersion.setText(textoVersion());

        findViewById(R.id.btnIniciarLecturas).setOnClickListener(v -> procesoLecturas());
        findViewById(R.id.cardAforador).setOnClickListener(v -> procesoLecturas());
        findViewById(R.id.btnUtilidades).setOnClickListener(v -> utilidades());
        findViewById(R.id.btnComunica).setOnClickListener(v -> iniciarPanelComunicaciones());
        findViewById(R.id.btnElimnarInfo).setOnClickListener(v -> ejecutarBorradoDeArchivos());
        findViewById(R.id.btnRetornar).setOnClickListener(v -> retornar());
        findViewById(R.id.btnAyuda).setOnClickListener(v -> verEstadoEnvios());

        seleccionUrl();
        CargarArchivoCarga();
        cargarImagenUsuario(aforador);
    }

    @Override
    protected void onResume() {
        super.onResume();
        enPrimerPlano = true;
        refrescar();
        // Si el servicio ya está enviando (p. ej. lo arrancó el ingreso), mostrarlo y seguir los conteos.
        if (SincronizacionService.corriendo()) ui.postDelayed(vigilarEnvio, VIGILAR_ENVIO_MS);
    }

    @Override
    protected void onPause() {
        enPrimerPlano = false;
        ui.removeCallbacks(vigilarEnvio);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        ui.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    // ------------------------------------------------------------------ estado de la ruta y de los envíos

    private void refrescar() {
        resultado = null; // "Eliminar datos" espera el cálculo nuevo, no decide con el anterior
        cargarArchivoNombre();
        calculando = true;
        actualizarTrabajo();
        CargaResumen.cargar(this, VariablesGlobales.directorioactual, this::alCargar);
    }

    /** Barra y texto de "el sistema está trabajando": calcula, envía o verifica con el servidor. */
    private void actualizarTrabajo() {
        String texto = null;
        if (verificandoBorrado) texto = "Verificando con el servidor…";
        else if (SincronizacionService.corriendo() || seguirEnvioHasta > System.currentTimeMillis()) texto = "Enviando información al servidor…";
        else if (calculando) texto = "Actualizando conteos de la ruta…";
        progresoMenu.setVisibility(texto == null ? View.INVISIBLE : View.VISIBLE);
        txtTrabajando.setVisibility(texto == null ? View.GONE : View.VISIBLE);
        if (texto != null) txtTrabajando.setText(texto);
    }

    private void alCargar(CargaResumen.Resultado r) {
        if (isFinishing() || isDestroyed()) return;
        resultado = r;
        calculando = false;
        actualizarTrabajo();

        // Solo con el menú al frente: si la liquidación ya abrió encima, ella lleva estos contadores.
        if (enPrimerPlano && r.hayRuta()) CargaResumen.publicarGlobales(r.resumen, false);

        txtPrg.setText(r.ciclo.isEmpty() ? "----" : "CIC" + r.ciclo);
        if (r.ciclo.isEmpty()) {
            txtRuta.setText("Sin ruta cargada");
        } else {
            txtRuta.setText(r.ruta + (r.municipio.isEmpty() ? "" : " · " + r.municipio));
        }

        if (r.hayRuta()) {
            ResumenRuta s = r.resumen;
            panelAvance.setVisibility(View.VISIBLE);
            barAvance.setProgress(s.getAvance());
            txtAvance.setText("Leídos " + s.getClientesProcesados() + " de " + s.getTotalClientes()
                    + " · Pendientes " + s.getClientesPendientes() + " · Anomalías " + s.getRegistrosConCausa());
            String ultima = s.getUltimaLectura();
            txtMenuUltima.setText(s.getAvance() + "% de avance"
                    + (s.getProcesadosHoy() > 0 ? " · " + s.getProcesadosHoy() + " hoy" : "")
                    + (ultima.length() >= 12 ? " · última " + ultima.substring(8, 10) + ":" + ultima.substring(10, 12) : "")
                    + (s.getClientesPendientes() == 0 ? " · ruta terminada" : ""));
        } else {
            panelAvance.setVisibility(View.GONE);
            if (!r.ciclo.isEmpty() && r.error != null) txtRuta.setText("No se pudo leer la ruta");
        }
        pintarEnvios(r.envios);
    }

    private void pintarEnvios(EstadoEnvios e) {
        if (e == null) {
            txtEnvios.setText("Envíos: no se pudo leer la cola");
            txtEnvios.setTextColor(0xFF607D8B);
            txtInsignia.setVisibility(View.GONE);
            return;
        }
        int pendientes = e.pendientesDeRuta();
        txtEnvios.setText("Envíos: " + e.resumenCorto());
        txtEnvios.setTextColor(!e.modoRest ? 0xFF607D8B : e.rechazados() > 0 ? 0xFFC62828 : pendientes > 0 ? 0xFFEF6C00 : 0xFF2E7D32);
        if (e.modoRest && pendientes > 0) {
            txtInsignia.setText(pendientes > 999 ? "999+" : String.valueOf(pendientes));
            txtInsignia.setVisibility(View.VISIBLE);
        } else {
            txtInsignia.setVisibility(View.GONE);
        }
    }

    /** Botón Envíos (antes "Ayuda", con un texto fijo): estado real de la cola y las recomendaciones de siempre. */
    private void verEstadoEnvios() {
        EstadoEnvios e = null;
        try {
            e = EstadoEnvios.leer(this);
        } catch (RuntimeException ignored) {
            // se muestra sin conteos
        }
        if (e != null) pintarEnvios(e);
        StringBuilder m = new StringBuilder();
        if (e == null) {
            m.append("No se pudo leer la cola de envío.\n");
        } else if (!e.modoRest) {
            m.append("Modo SOAP: los envíos se hacen desde la liquidación (menú Envíos).\n");
        } else {
            m.append(e.detalle()).append('\n')
                    .append("Lecturas ya enviadas: ").append(e.liquidacionesEnviadas).append('\n');
            if (e.trackingPendientes > 0) m.append("Puntos de recorrido por enviar: ").append(e.trackingPendientes).append('\n');
        }
        m.append("\nRECOMENDACIONES:\n")
                .append("- Su información debe ser transmitida al servidor.\n")
                .append("- Verifique su conexión a internet.\n")
                .append("- Los rechazados se revisan en la liquidación, menú Envíos.\n")
                .append("\nSOPORTE: en caso de inconsistencias comuníquese con su supervisor o el área técnica.\n")
                .append("SIM-FA - Gestión de Facturación Móvil · ").append(VariablesGlobales.versionApp);

        AlertDialog.Builder b = new AlertDialog.Builder(this)
                .setTitle("Estado de los envíos")
                .setMessage(m)
                .setNegativeButton("Cerrar", null);
        if (e != null && e.modoRest && (e.pendientesDeRuta() > 0 || e.trackingPendientes > 0)) {
            b.setPositiveButton("Enviar ahora", (d, w) -> enviarAhora());
        }
        b.show();
    }

    private void enviarAhora() {
        SincronizacionService.vaciarCola(this);
        seguirEnvioHasta = System.currentTimeMillis() + SEGUIR_ENVIO_MS;
        actualizarTrabajo();
        ui.removeCallbacks(vigilarEnvio);
        ui.postDelayed(vigilarEnvio, VIGILAR_ENVIO_MS);
    }

    // ------------------------------------------------------------------ navegación

    private void iniciarPanelComunicaciones() {
        Bundle bundle = new Bundle();
        bundle.putString("NIVEL_OPERADOR", nivelOperador);
        bundle.putString("CODIGO_INTERNO_PDA", aforador.trim());
        Intent comunicaciones = new Intent(this, Comunicaciones.class);
        comunicaciones.putExtras(bundle);
        startActivity(comunicaciones);
    }

    private void utilidades() {
        startActivity(new Intent(this, FechayHora.class));
    }

    private void retornar() {
        finish();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_P:
                procesoLecturas();
                return true;
            case KeyEvent.KEYCODE_U:
                utilidades();
                return true;
            case KeyEvent.KEYCODE_E:
                ejecutarBorradoDeArchivos();
                return true;
            case KeyEvent.KEYCODE_R:
                retornar();
                return true;
            case KeyEvent.KEYCODE_C:
                iniciarPanelComunicaciones();
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    private void procesoLecturas() {
        String hoy = new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date());
        if (hoy.compareTo("20151211") < 0) {
            Toast.makeText(this, "Fecha desactualizada, ajústela en Utilitarios: " + hoy, Toast.LENGTH_LONG).show();
            return;
        }
        if (!new File(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/NOMBRE").exists()) {
            Toast.makeText(this, "Dispositivo no tiene información de trabajo... Cargar información", Toast.LENGTH_LONG).show();
            return;
        }
        if (new File(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/LEIDO.TXT").exists()) {
            Toast.makeText(this, "Proceso ya está leído..\nDescargue la información para su backup", Toast.LENGTH_LONG).show();
        }
        if (new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/DESCARGA").exists()) {
            Toast.makeText(this, "Ruta ya está descargada: borre datos y cargue una nueva ruta", Toast.LENGTH_LONG).show();
        }

        Bundle b = new Bundle();
        b.putString("aforador", aforador);
        b.putString("directorioActual", VariablesGlobales.directorioactual);
        b.putString("nombrePredio", Ciclo + Municipio + Seccion + Division);
        b.putString("terminal", terminal);
        b.putString("nivelOperador", nivelOperador);
        Intent intent = new Intent(this, ResumenEstadistico.class);
        intent.putExtras(b);
        startActivityForResult(intent, RESUMEN_REQUEST_CODE);
    }

    /** ResumenEstadistico devuelve en "fila" el cliente donde abrir la liquidación (-1 = primer pendiente). */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != RESUMEN_REQUEST_CODE || resultCode != RESULT_OK || data == null) return;

        String filaid = data.getStringExtra("fila");
        if (filaid == null) filaid = "-1";
        CrudifNull[] ced = bdl.obtnerdatos();
        if (ced.length > 0) {
            bdl.actualizarData(bdl.obtenerdatabyid(1), filaid, aforador, "001", VariablesGlobales.directorioactual, nivelOperador);
        } else {
            bdl.guardarDatos(1, terminal, filaid, aforador, "001", VariablesGlobales.directorioactual, nivelOperador);
        }

        Bundle bundle = new Bundle();
        bundle.putString("fila", filaid);
        bundle.putString("operario", aforador);
        bundle.putString("terminal", terminal);
        bundle.putString("impresora", "001");
        bundle.putString("path", VariablesGlobales.directorioactual);
        bundle.putString("nivelOperador", nivelOperador);
        Intent i = new Intent(this, MenuDeLiquidacion.class);
        i.putExtras(bundle);
        startActivity(i);
    }

    // ------------------------------------------------------------------ eliminar datos

    void ejecutarBorradoDeArchivos() {
        if (resultado == null) {
            Toast.makeText(this, "Calculando el estado de la ruta, intente de nuevo en un momento", Toast.LENGTH_SHORT).show();
            return;
        }
        if (verificandoBorrado) return;
        boolean admin = esAdministrador();
        try {
            resultado.envios = EstadoEnvios.leer(this); // la cola cambia en segundo plano: leerla ahora
        } catch (RuntimeException ignored) {
            // se decide con la última lectura
        }

        if (!admin && VariablesGlobales.totalprediosnofacturados > 0) {
            utils.MensajeTime("LA RUTA NO SE HA LEIDO EN SU TOTALIDAD, FALTAN " + VariablesGlobales.totalprediosnofacturados
                    + " CLIENTES POR FACTURAR", "CLIENTES SIN FACTURAR!", this, 8);
            return;
        }
        EstadoEnvios e = resultado.envios;
        if (!admin && e != null && e.modoRest && e.pendientesDeRuta() > 0) {
            new AlertDialog.Builder(this)
                    .setTitle("Faltan envíos")
                    .setMessage("No puede borrar: hay información sin enviar o rechazada por el servidor.\n\n" + e.detalle()
                            + "\n\nLos rechazados se revisan en la liquidación, menú Envíos.")
                    .setPositiveButton("Enviar ahora", (d, w) -> enviarAhora())
                    .setNegativeButton("Cerrar", null)
                    .show();
            return;
        }
        if (admin) {
            validacionesElimina();
        } else {
            validarEnvio();
        }
    }

    private boolean esAdministrador() {
        return nivelOperador.trim().equals("A");
    }

    /** Pregunta al servidor SOAP si la ruta ya se descargó (no debe existir TOMOLECTURA.TXT pendiente). */
    private void validarEnvio() {
        esComprimido = "0";
        final String archivo = rutaAdministrador.trim() + "CIC" + Ciclo + "\\" + "D" + Municipio + Seccion + Division + "\\" + "TOMOLECTURA.TXT";
        final String tomoLectura = leeTomoLectura();
        final String url = URL, pagina = paginaWs;
        final String ruta = Municipio + Seccion + Division;
        final String ciclo = Ciclo;
        verificandoBorrado = true;
        actualizarTrabajo();
        new Thread(() -> {
            String respuesta;
            try {
                respuesta = new WSSoap(url, pagina).ObtenerLongitudArchivo("OBTENER_LONGITUD_ARCHIVO", archivo,
                        Integer.parseInt(esComprimido), ciclo + "-" + tomoLectura + "-B", ruta);
            } catch (Exception ex) {
                respuesta = null;
                utils.Log(logfile, "[MenuPrincipal]validarEnvio(): " + ex);
            }
            final String r = respuesta;
            ui.post(() -> alVerificarServidor(r));
        }, "verificar-borrado").start();
    }

    private void alVerificarServidor(String respuesta) {
        verificandoBorrado = false;
        if (isFinishing() || isDestroyed()) return;
        actualizarTrabajo();
        if (respuesta != null && respuesta.trim().equals("PUEDE BORRAR")) {
            validacionesElimina();
            return;
        }
        String mensaje;
        if (respuesta != null && esEntero(respuesta)) {
            mensaje = "No puede borrar. Primero descargue la ruta.";
        } else {
            // Antes un error de conexión no mostraba nada: el botón parecía no hacer nada.
            mensaje = "No se pudo verificar con el servidor que la ruta esté descargada"
                    + (respuesta == null || respuesta.trim().isEmpty() ? "." : ":\n" + respuesta.trim())
                    + "\nRevise la conexión e intente de nuevo.";
        }
        new AlertDialog.Builder(this).setTitle("Eliminar datos").setMessage(mensaje).setPositiveButton("Entendido", null).show();
    }

    public void validacionesElimina() {
        if (VariablesGlobales.totalprediosnofacturados > 0 && !esAdministrador()) {
            utils.MensajeTime("LA RUTA NO SE HA LEIDO EN SU TOTALIDAD, FALTAN CLIENTES POR FACTURAR", "CLIENTES SIN FACTURAR!", this, 8);
            return;
        }
        crearArchivoO();

        EstadoEnvios e = resultado == null ? null : resultado.envios;
        boolean sinEnviar;
        String detalle = "";
        if (e != null && e.modoRest) {
            // En modo REST ENVIOSGPRS.SDA siempre existe (es el respaldo completo): manda la cola.
            sinEnviar = e.pendientesDeRuta() > 0;
            detalle = "\n\n" + e.detalle();
        } else {
            sinEnviar = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/ENVIOSGPRS.SDA").exists();
        }
        if (sinEnviar) {
            mensajesAlert("Alerta de eliminacion", "SU TERMINAL TIENE REGISTROS SIN ENVIAR. DEBE RETORNAR A LECTURAS Y ENVIARLOS." + detalle
                    + "\n\n¿Desea regresar?", 1);
        } else {
            mensajesAlert("Alerta de eliminacion", "CONFIRMA QUE BORRARA LOS ARCHIVOS", 4);
        }
    }

    public void crearArchivoO() {
        File desc = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/O" + Ciclo + Municipio + Seccion + Division + ".SDA");
        utils.EscribirLinea(desc, "O" + Ciclo + Municipio + Seccion + Division);
    }

    /** Cadena de confirmaciones (mismo orden que antes): 1 -> 2 -> 3/4 -> 5 = borrar. */
    private void mensajesAlert(String titulo, String msg, final int opc) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titulo);
        builder.setMessage(msg);
        builder.setPositiveButton("SI", (dialog, which) -> {
            dialog.dismiss();
            if (opc == 3) mensajesAlert("Alerta de eliminacion", "CONFIRMA QUE BORRARA LOS ARCHIVOS", 4);
            else if (opc == 4) mensajesAlert("Alerta de eliminacion", " RE-confirma\n que borrara los archivos", 5);
            else if (opc == 5) ejecutarBorradoArchivos2();
        });
        builder.setNegativeButton("NO", (dialog, which) -> {
            dialog.dismiss();
            if (opc == 1) {
                mensajesAlert("Alerta de eliminacion", "LE CONFIRMO QUE SU\n TERMINAL TIENE\n REGISTROS\n SIN ENVIAR DEBE\n RETORNAR\n A LECTURAS Y ENVIARLOS", 2);
            } else if (opc == 2) {
                if (VariablesGlobales.totalprediosnofacturados > 0) {
                    mensajesAlert("Alerta de eliminacion", "RUTA NO SE HA LEIDO EN SU TOTALIDAD\n desea borrar la informacion? ", 3);
                } else {
                    mensajesAlert("Alerta de eliminacion", "CONFIRMA QUE BORRARA LOS ARCHIVOS", 4);
                }
            }
        });
        builder.create().show();
    }

    public void ejecutarBorradoArchivos2() {
        copyFilestoBackUp();
        String zip = GeneraUnZIp();
        if (!zip.equals("OK")) {
            Toast.makeText(this, "NO SE PUEDE BORRAR HASTA NO \nPROCESAR EL ZIP DE RESPALDO...!! " + zip, Toast.LENGTH_LONG).show();
            return;
        }
        int fallidos = borrarArchivosDe(new File(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/"))
                + borrarArchivosDe(new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/"))
                + borrarArchivosDe(new File(VariablesGlobales.directorioactual + "/DCIM/FOTOGRAFIASL"));
        if (fallidos == 0) {
            Toast.makeText(this, "ARCHIVOS BORRADOS \nEN SU TOTALIDAD", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, fallidos + " ARCHIVOS NO SE PUDIERON BORRAR... REINICIE E INTENTE DE NUEVO", Toast.LENGTH_LONG).show();
            utils.Log(logfile, "[MenuPrincipal]ejecutarBorradoArchivos2(): " + fallidos + " archivos sin borrar");
        }
        refrescar();
    }

    /** Borra los archivos (no las subcarpetas) de la carpeta; devuelve cuántos no se pudieron borrar. */
    private static int borrarArchivosDe(File carpeta) {
        File[] archivos = carpeta.listFiles();
        if (archivos == null) return 0; // la carpeta no existe: nada que borrar
        int fallidos = 0;
        for (File f : archivos) {
            if (f.isFile() && !f.delete()) fallidos++;
        }
        return fallidos;
    }

    /**
     * Copia DATOSDESALIDA a DCIM/SIMFABACKUP/<ciclo+ruta>/ y purga las copias con más de 2 meses.
     * Antes: copiaba línea a línea con el juego de caracteres del equipo (dañaba Ñ y tildes de los planos de
     * ancho fijo), no creaba la carpeta destino (fallaba sin aviso), usaba directorioBackUp (que la liquidación
     * cambia a la carpeta de la ruta) y la cuenta de meses borraba copias de diciembre en enero.
     */
    public void copyFilestoBackUp() {
        File base = new File(VariablesGlobales.directorioactual + "/DCIM/SIMFABACKUP/");
        try {
            File[] copias = base.listFiles();
            if (copias != null) {
                Calendar hoy = Calendar.getInstance();
                int mesesHoy = hoy.get(Calendar.YEAR) * 12 + hoy.get(Calendar.MONTH);
                for (File c : copias) {
                    if (!c.isDirectory()) continue;
                    Calendar mod = Calendar.getInstance();
                    mod.setTimeInMillis(c.lastModified());
                    int meses = mod.get(Calendar.YEAR) * 12 + mod.get(Calendar.MONTH);
                    if (mesesHoy - meses > 2) {
                        Log.i("BACKUP", "borra copia de más de 2 meses: " + c.getName());
                        FileUtils.deleteDirectory(c);
                    }
                }
            }
            File destino = new File(base, Ciclo + Municipio + Seccion + Division);
            if (!destino.isDirectory() && !destino.mkdirs()) {
                utils.Log(logfile, "[copyFilestoBackUp] no se pudo crear " + destino);
                return;
            }
            File[] salida = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA").listFiles();
            if (salida == null) return;
            for (File f : salida) {
                if (f.isFile()) FileUtils.copyFile(f, new File(destino, f.getName()));
            }
        } catch (Exception e) {
            Log.e("BACKUP", "[copyFilestoBackUp]", e);
            utils.Log(logfile, "[copyFilestoBackUp]Error|:" + e);
        }
    }

    /** ZIP de respaldo de DATOSDESALIDA en DCIM/SIMFABACKUP (uno por día del mes). Sin cambios de criterio. */
    private String GeneraUnZIp() {
        String dia = String.format(Locale.US, "%02d", Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        String rutaLecturas = VariablesGlobales.directorioactual + "/DATOSDESALIDA/";
        String rutaBackup = VariablesGlobales.directorioactual + "/DCIM/SIMFABACKUP/";
        String nombreZip = "COPIACOMPLETASIMFA_" + Municipio + Seccion + Division + "_" + dia + ".ZIP";

        File dirBackup = new File(rutaBackup);
        if (!dirBackup.exists()) dirBackup.mkdirs();

        File[] files = new File(rutaLecturas).listFiles();
        if (files == null || files.length < 3) {
            Log.e("BACKUP", "No se genera backup. Solo existen " + (files == null ? 0 : files.length) + " archivos.");
            return "No se genera backup. Menos de 3 archivos.";
        }
        ArrayList<File> filestoZip = new ArrayList<>();
        for (File file : files) {
            if (file.isFile()) filestoZip.add(file);
        }
        File zip = new File(rutaBackup + nombreZip);
        if (zip.exists()) zip.delete();
        String comprimir = utils.CreaZip(rutaBackup + nombreZip, filestoZip);
        Log.i("BACKUP", "Respuesta comprimir: " + comprimir);
        return comprimir.contains("✓") ? "OK" : "Error comprimiendo archivos de respaldo.";
    }

    // ------------------------------------------------------------------ archivos de configuración

    private String leeTomoLectura() {
        File file = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/TOMOLECTURA.TXT");
        String ultima = "";
        if (!file.exists()) return ultima;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String linea;
            while ((linea = reader.readLine()) != null) ultima = linea;
        } catch (IOException e) {
            Log.e("MenuPrincipal", "no lee TOMOLECTURA.TXT", e);
        }
        return ultima;
    }

    private boolean seleccionUrl() {
        File file = new File(VariablesGlobales.directorioactual + "/DIRECCIONEIP.TXT");
        if (!file.exists()) return false;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (linea.length() >= 157 && linea.substring(linea.length() - 1).trim().equals("A")) {
                    URL = ("http://" + linea.substring(0, 15).trim() + ":" + linea.substring(16, 20).trim()).replace(" ", "");
                    paginaWs = linea.substring(64, 157).trim();
                    return true;
                }
            }
        } catch (IOException | RuntimeException e) {
            Log.e("MenuPrincipal", "DIRECCIONEIP.TXT", e);
            Toast.makeText(this, "Error, verificar Configuracion IP", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    private void CargarArchivoCarga() {
        File f = new File(VariablesGlobales.directorioactual + "/ArchivosCarga.cfi");
        if (!f.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
            String linea = reader.readLine();
            if (linea != null) {
                rutaAdministrador = linea.trim();
                if (!rutaAdministrador.contains(":")) {
                    Toast.makeText(this, "No hay Archivo de Soporte...\n //ArchivosCarga.cfi", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            linea = reader.readLine();
            if (linea != null) esComprimido = linea.trim().equals("ZIP SI") ? "1" : "0";
        } catch (IOException e) {
            Log.e("MenuPrincipal", "CargarArchivoCarga", e);
        }
    }

    /** Ciclo, municipio, sección y división del archivo NOMBRE (se usan al borrar y para el respaldo). */
    private void cargarArchivoNombre() {
        Ciclo = "0000";
        Municipio = "000";
        Seccion = "000";
        Division = "0";
        File file = new File(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/NOMBRE");
        if (!file.isFile() || file.length() < 12) return;
        byte[] b = new byte[12];
        try (RandomAccessFile r = new RandomAccessFile(file, "r")) {
            r.readFully(b);
            String linea = new String(b, StandardCharsets.ISO_8859_1);
            Ciclo = linea.substring(0, 4);
            Municipio = linea.substring(5, 8);
            Seccion = linea.substring(8, 11);
            Division = linea.substring(11, 12);
        } catch (IOException e) {
            Toast.makeText(this, "Problema con la carga de:\n Archivo: Ciclo, Municipio...etc.", Toast.LENGTH_LONG).show();
        }
    }

    private void cargarImagenUsuario(String codigo) {
        try {
            File imgFile = new File(VariablesGlobales.directorioactual + "/AFORADORES/" + codigo.trim() + ".JPG");
            if (!imgFile.exists()) return;
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inSampleSize = 4; // la tarjeta es de 64dp: no hace falta la foto completa en memoria
            Bitmap bmp = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), o);
            if (bmp != null) ((ImageView) findViewById(R.id.fotoAforador)).setImageBitmap(bmp);
        } catch (RuntimeException ex) {
            Log.e("MenuPrincipal", "foto del operador", ex);
        }
    }

    // ------------------------------------------------------------------ textos

    private String textoOperador() {
        String rol = esAdministrador() ? "Administrador" : nivelOperador.trim().equals("S") ? "Supervisor" : "Lector";
        String t = terminal.trim();
        String term = t.length() > 4 ? "…" + t.substring(t.length() - 4) : t;
        return "Código " + aforador.trim() + (term.isEmpty() ? "" : " · Terminal " + term) + " · " + rol;
    }

    /** Versión que maneja el equipo (la misma que va en ENVIOGPRS e impresión), no el versionName del APK. */
    private String textoVersion() {
        String v = VariablesGlobales.versionApp;
        return (v == null || v.isEmpty() ? "Facturación en sitio" : "Facturación en sitio · " + v);
    }

    private static String valor(String s) {
        return s == null ? "" : s;
    }

    private static boolean esEntero(String s) {
        String t = s.trim();
        if (t.isEmpty()) return false;
        for (int i = (t.charAt(0) == '-' ? 1 : 0); i < t.length(); i++) if (!Character.isDigit(t.charAt(i))) return false;
        return true;
    }
}
