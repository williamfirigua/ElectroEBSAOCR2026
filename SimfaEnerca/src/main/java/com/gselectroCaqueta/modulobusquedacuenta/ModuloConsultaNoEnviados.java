package com.gselectroCaqueta.modulobusquedacuenta;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.gselectroCaqueta.accesoyseguridad.R;
import com.gselectroCaqueta.accesoyseguridad.VariablesGlobales;
import com.gselectroCaqueta.sync.ApiMovil;
import com.gselectroCaqueta.sync.ColaEnvio;
import com.gselectroCaqueta.sync.ColaEnvioSqlite;
import com.gselectroCaqueta.sync.ConfigSync;
import com.gselectroCaqueta.sync.Json;
import com.gselectroCaqueta.sync.MotorSincronizacion;
import com.gselectroCaqueta.sync.SincronizacionService;
import com.gselectroCaqueta.tablas.EnvioGPS;

import androidx.appcompat.app.AlertDialog;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ModuloConsultaNoEnviados extends AppCompatActivity {

    public EnvioGPS misenvios = new EnvioGPS();

    Button btnCerrarNe2;

    ImageButton btnAtras1;
    ImageButton btnAdelante1;
    ImageButton btnPrimero1;
    ImageButton btnUltimo1;

    Resources rs;

    TextView lblpos;
    EditText txtregnoenviados;

    TableLayout tabla;
    TableLayout cabecera;
    TableRow.LayoutParams layoutFila;
    TableRow.LayoutParams layoutCuenta;
    TableRow.LayoutParams layoutContador;

    String path;
    String serialPDA = "000000000000000";
    String rutaEnvioGPS;
    String rutaEnvioGPSser;

    int totEnvioGPS = 0;//Ax: cuenta la cantidad de registros en EnvioGPS
    int totEnvioGPSser = 0;//Ax: cuenta la cantidad de registros en EnvioGPSSerial
    int total = 0;//Ax: suma de los registros de EnvioGPS

    int registroactual = 1; //Ax: para llenar la tabla de arriba antes de los conteos y no influir en otras cosas.
    int registrolimite = 20; //Ax: limite de paginacion.

    // Envío por API REST: lo no enviado está en la cola (ebsa_sync.db), no en ENVIOGPRS<serial>.SDA, que en
    // ese modo no se genera; ENVIOSGPRS.SDA queda completo como respaldo.
    boolean modoRest;
    ColaEnvioSqlite cola;
    Button btnSubirRespaldo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modulo_consultnoenviads);

        rs = this.getResources();

        Bundle bundle = getIntent().getExtras();
        path = bundle.getString("directorioactual");
        serialPDA = bundle.getString("serial");// getSerialNumber();

        tabla = (TableLayout) findViewById(R.id.tblcuentacontador);
        cabecera = (TableLayout) findViewById(R.id.tblcabecera);
        layoutFila = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        layoutCuenta = new TableRow.LayoutParams(200, TableRow.LayoutParams.WRAP_CONTENT);
        layoutContador = new TableRow.LayoutParams(200, TableRow.LayoutParams.WRAP_CONTENT);

        btnAdelante1 = (ImageButton) findViewById(R.id.btnAdelanteNe2);
        btnAtras1 = (ImageButton) findViewById(R.id.btnAtrasNe2);
        btnPrimero1 = (ImageButton) findViewById(R.id.btnPrimeroNe2);
        btnUltimo1 = (ImageButton) findViewById(R.id.btnUltimoNe2);

        txtregnoenviados = (EditText) findViewById(R.id.txtregnoenviados);
        lblpos = (TextView) findViewById(R.id.lblpos);

        btnCerrarNe2 = (Button) findViewById(R.id.btnCerrarNe2);

        btnCerrarNe2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                volveraLiquidacion();
            }
        });

        btnAtras1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                atrasadelante("tras");
            }
        });

        btnAdelante1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                atrasadelante("delante");
            }
        });

        btnPrimero1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                atrasadelante("primero");
            }
        });

        btnUltimo1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                atrasadelante("ultimo");
            }
        });

        modoRest = ConfigSync.modoRest(this);
        if (modoRest) {
            prepararModoRest();
            cargarRest();
        } else {
            ModuloConsultaNoEnviados_Load();
        }

    }//End onCreate

    public void volveraLiquidacion() {//Ax: envia datos a la actividad que la llamo y se cierra

        Intent iBackActivity = new Intent(this, com.gselectroCaqueta.accesoyseguridad.MenuDeLiquidacion.class);
        setResult(Activity.RESULT_OK, iBackActivity);
        finish();
    }

    @Override
    public void onBackPressed() {
        mensajes("inhabilitado");
    }

    private void ModuloConsultaNoEnviados_Load() {

        try {
            File fileenvios = new File(path + "/DATOSDESALIDA/ENVIOGPRS" + serialPDA + ".SDA");
            File RenameOri = new File(path + "/DATOSDESALIDA/COPIAGPRS" + serialPDA + ".SDA");
            if (RenameOri.exists() && !fileenvios.exists()){
                File Original = new File(path + "/DATOSDESALIDA/ENVIOGPRS" + serialPDA + ".SDA");
                RenameOri.renameTo(Original);
            }
            rutaEnvioGPS = path + "/DATOSDESALIDA/ENVIOSGPRS.SDA";
//mensajes("QUE LLEGO "+rutaEnvioGPS);
            if (misenvios.abrir_EnvioGPS(rutaEnvioGPS)) {
                totEnvioGPS = misenvios.total_EnvioGPS;
            }
            misenvios.Cerrar_EnvioGPS();

            rutaEnvioGPSser = path + "/DATOSDESALIDA/ENVIOGPRS" + serialPDA + ".SDA";
//mensajes("QUE LLEGO2 "+rutaEnvioGPSser);
            if (misenvios.abrir_EnvioGPS(rutaEnvioGPSser)) {
                totEnvioGPSser = misenvios.total_EnvioGPS;
            }
            misenvios.Cerrar_EnvioGPS();

            total = totEnvioGPS + totEnvioGPSser;
//mensajes("QUE SUMA ESTO "+totEnvioGPS + "/"+totEnvioGPSser);
            if (total == 0) {

                File file = new File(rutaEnvioGPSser);

                if (file.exists()) {
                    if (file.length() < 4) { //Ax: un tamaño de 4 son espacios o dos cracteres max.
                        file.delete();
                    }
                }
                mensajes("No hay Registros para enviar al Servidor");
                finish();
            }
            txtregnoenviados.setText(total + "");
            agregarCabecera();
            LeerNoEnviados();

        } catch (Exception ex) {
            finish();
        }
    }

    private void LeerNoEnviados() {
        if (modoRest) {
            leerRest();
            return;
        }

        tabla.removeAllViews();
        tabla.refreshDrawableState();

        try {
            for (int i = 1; i <= registrolimite; i++) {

                if (registroactual <= totEnvioGPS) { //Ax: Leer primer archivo

                    if (misenvios.abrir_EnvioGPS(rutaEnvioGPS)) {
                        totEnvioGPS = misenvios.total_EnvioGPS;
                        misenvios.lectura_EnvioGPS_Tipo2(registroactual);
                        llenarTabla(misenvios.getEnvioGPS_CUENTA(), misenvios.getEnvioGPS_NROCONTADOR());
                        registroactual++;
                    }
                } else {
                    if (misenvios.archivo_EnvioGPS.equals(rutaEnvioGPS)) {
                        misenvios.Cerrar_EnvioGPS();
                    }

                    if (registroactual <= (totEnvioGPS + totEnvioGPSser)) { //Ax: Leer segundo archivo

                        if (misenvios.abrir_EnvioGPS(rutaEnvioGPSser)) {
                            totEnvioGPSser = misenvios.total_EnvioGPS;
                            misenvios.lectura_EnvioGPS_Tipo2((registroactual - totEnvioGPS));
                            llenarTabla(misenvios.getEnvioGPS_CUENTA(), misenvios.getEnvioGPS_NROCONTADOR());
                            registroactual++;
                        }
                    } else {
                        misenvios.Cerrar_EnvioGPS();
                    }
                }
            }
            total = totEnvioGPS + totEnvioGPSser;

            if (total < 1) {
                lblpos.setText("0/0");
            } else {
                lblpos.setText((registroactual - 1) + "/" + total);
            }
        } catch (Exception ex) {
            finish();
        }
    }

    //Ax: cambia de numero de registro (botones atras,adelante,primero,ultimo) para luego llenar la TableLayout
    private void atrasadelante(String va) {

        switch (va) {
            case "primero":
                registroactual = 1;
                LeerNoEnviados();
                break;
            case "delante": //Adelante
                if ((registroactual + 20) == total) {
                    registroactual = total - 19;
                }

                if (registroactual >= total) {
                    return;
                }
                LeerNoEnviados();
                break;
            case "tras"://Atras
                registroactual = (registroactual - 40);
                if (registroactual < 1) {
                    registroactual = 1;
                }
                LeerNoEnviados();
                break;
            case "ultimo":
                registroactual = total - 19;
                LeerNoEnviados();
                break;
        }
    }

    public void llenarTabla(String cuenta, String nrocontador) {

        TableRow fila;
        TextView txtCuenta = new TextView(this);
        TextView txtNroContador = new TextView(this);

        fila = new TableRow(this);
        fila.setLayoutParams(layoutFila);

        txtCuenta.setText(cuenta);
        txtCuenta.setGravity(Gravity.LEFT);
        txtCuenta.setTextAppearance(this, R.style.etiqueta);
        txtCuenta.setBackgroundResource(R.drawable.tabla_celda);
        txtCuenta.setLayoutParams(layoutCuenta);

        txtNroContador.setText(nrocontador);
        txtNroContador.setGravity(Gravity.LEFT);
        txtNroContador.setTextAppearance(this, R.style.etiqueta);
        txtNroContador.setBackgroundResource(R.drawable.tabla_celda);
        txtNroContador.setLayoutParams(layoutContador);

        fila.addView(txtCuenta);
        fila.addView(txtNroContador);
        tabla.addView(fila);
    }

    public void agregarCabecera() {

        TableRow fila;
        TextView txtCuenta = new TextView(this);
        TextView txtNroContador = new TextView(this);

        fila = new TableRow(this);
        fila.setLayoutParams(layoutFila);

        txtCuenta.setText("CUENTA");
        txtCuenta.setGravity(Gravity.CENTER_HORIZONTAL);
        txtCuenta.setTextAppearance(this, R.style.etiqueta);
        txtCuenta.setBackgroundResource(R.drawable.tabla_celda_cabecera);
        txtCuenta.setLayoutParams(layoutCuenta);

        txtNroContador.setText("CONTADOR");
        txtNroContador.setGravity(Gravity.CENTER_HORIZONTAL);
        txtNroContador.setTextAppearance(this, R.style.etiqueta);
        txtNroContador.setBackgroundResource(R.drawable.tabla_celda_cabecera);
        txtNroContador.setLayoutParams(layoutContador);

        fila.addView(txtCuenta);
        fila.addView(txtNroContador);
        cabecera.addView(fila);
    }

//    // Captura numero IMEI del telefono
//    public String getSerialNumber() {
//
//        TelephonyManager tManager = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
//        String deviceIMEI = tManager.getDeviceId();
//        return deviceIMEI;
//    }

    // ------------------------------------------------------------------ modo REST (cola)

    private void prepararModoRest() {
        cola = ColaEnvioSqlite.de(this);
        findViewById(R.id.filaAccionesRest).setVisibility(View.VISIBLE);
        btnSubirRespaldo = (Button) findViewById(R.id.btnSubirRespaldo);

        findViewById(R.id.btnEnviarAhora).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SincronizacionService.solicitar(ModuloConsultaNoEnviados.this);
                mensajes("Enviando lo pendiente en segundo plano");
            }
        });
        findViewById(R.id.btnReintentarRechazados).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int n = 0;
                for (String t : new String[]{ColaEnvio.LIQUIDACION, ColaEnvio.LBL, ColaEnvio.FOTO, ColaEnvio.TRACKING}) {
                    n += cola.reintentarErrores(t);
                }
                if (n > 0) SincronizacionService.solicitar(ModuloConsultaNoEnviados.this);
                mensajes(n > 0 ? n + " rechazados vuelven a enviarse" : "No hay rechazados");
                cargarRest();
            }
        });
        btnSubirRespaldo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subirRespaldo();
            }
        });
    }

    private void cargarRest() {
        int pendientes = cola.contar(ColaEnvio.LIQUIDACION, ColaEnvio.PENDIENTE);
        int rechazadas = cola.contar(ColaEnvio.LIQUIDACION, ColaEnvio.ERROR);
        int archivos = cola.contar(ColaEnvio.FOTO, ColaEnvio.PENDIENTE) + cola.contar(ColaEnvio.LBL, ColaEnvio.PENDIENTE);
        total = pendientes + rechazadas;
        txtregnoenviados.setText(total + (rechazadas > 0 ? " (" + rechazadas + " rech.)" : "") + " · archivos " + archivos);
        if (cabecera.getChildCount() == 0) agregarCabecera();
        ((TextView) ((TableRow) cabecera.getChildAt(0)).getChildAt(1)).setText("ESTADO");
        registroactual = 1;
        leerRest();
        if (total == 0) mensajes("No hay lecturas pendientes de envío");
    }

    private void leerRest() {
        tabla.removeAllViews();
        List<ColaEnvioSqlite.NoEnviado> filas = cola.noEnviados(ColaEnvio.LIQUIDACION, registroactual - 1, registrolimite);
        for (ColaEnvioSqlite.NoEnviado f : filas) {
            String cuenta = "?";
            try {
                Map<String, Object> l = (Map<String, Object>) Json.objeto(f.elemento.datos).get("lectura");
                cuenta = Json.texto(l, "cod_cuenta") + " " + Json.texto(l, "indicador");
            } catch (RuntimeException ignored) {
            }
            String estado;
            if (f.estado == ColaEnvio.ERROR) {
                String m = f.motivo == null ? "" : f.motivo;
                estado = "Rechazada: " + (m.length() > 60 ? m.substring(0, 60) + "…" : m);
            } else {
                estado = f.elemento.intentos > 0 ? "Pendiente (" + f.elemento.intentos + " intentos)" : "Pendiente";
            }
            llenarTabla(cuenta, estado);
        }
        registroactual += filas.size();
        lblpos.setText(total < 1 ? "0/0" : (registroactual - 1) + "/" + total);
    }

    /** POST /api/v1/liquidaciones/planos con ENVIOSGPRS.SDA y CO_COBRO.SDA; lo ya recibido sale duplicado. */
    private void subirRespaldo() {
        final File envio = new File(path + "/DATOSDESALIDA/ENVIOSGPRS.SDA");
        final File cobros = new File(path + "/DATOSDESALIDA/CO_COBRO.SDA");
        if (!envio.isFile() || envio.length() == 0) {
            mensajes("No hay ENVIOSGPRS.SDA para subir");
            return;
        }
        final ConfigSync cfg = new ConfigSync(this);
        if (cfg.baseApi().isEmpty()) {
            mensajes("Abra primero la liquidación para tomar la URL de la API");
            return;
        }
        btnSubirRespaldo.setEnabled(false);
        mensajes("Subiendo respaldo…");
        new Thread(new Runnable() {
            @Override
            public void run() {
                MotorSincronizacion.Resumen r = new MotorSincronizacion.Resumen();
                MotorSincronizacion motor = new MotorSincronizacion(new ApiMovil(cfg.baseApi()), cola, cfg, cfg, null);
                final ApiMovil.Respuesta resp = motor.subirRespaldo(envio, cobros, r);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnSubirRespaldo.setEnabled(true);
                        mostrarResultadoRespaldo(resp);
                    }
                });
            }
        }).start();
    }

    private void mostrarResultadoRespaldo(ApiMovil.Respuesta resp) {
        String texto;
        if (resp.ok()) {
            texto = "Líneas: " + Json.texto(resp.cuerpo, "lineas")
                    + "\nNuevas: " + Json.texto(resp.cuerpo, "aceptados")
                    + "\nYa estaban en el servidor: " + Json.texto(resp.cuerpo, "duplicados")
                    + "\nRechazadas: " + Json.texto(resp.cuerpo, "rechazados");
        } else if (resp.codigo == 0) {
            texto = "Sin sesión: " + resp.error + "\nIngrese de nuevo con su clave.";
        } else if (resp.codigo == ApiMovil.SIN_RED) {
            texto = "Sin conexión con el servidor. Intente cuando tenga señal.";
        } else {
            texto = "El servidor no recibió el respaldo (" + resp + ")";
        }
        new AlertDialog.Builder(this).setTitle("Respaldo de lecturas").setMessage(texto).setPositiveButton("Aceptar", null).show();
    }

    private void mensajes(String msg) {
        //Toast.makeText(MenuDeLiquidacion.this, msg, Toast.LENGTH_LONG).show();
        Toast toast = Toast.makeText(ModuloConsultaNoEnviados.this, msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 10, 50);
        toast.show();
    }
}