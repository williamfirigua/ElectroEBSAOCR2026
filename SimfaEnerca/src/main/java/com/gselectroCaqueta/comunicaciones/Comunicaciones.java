package com.gselectroCaqueta.comunicaciones;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import androidx.core.content.res.ResourcesCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gselectroCaqueta.accesoyseguridad.R;
import com.gselectroCaqueta.accesoyseguridad.VariablesGlobales;
import com.gselectroCaqueta.tablas.TablaClienteSalida;
import com.gselectroCaqueta.tablas.TablaRegistroSalida;
import com.gsutil.AsyncResponse;
import com.gsutil.DatosWS;
import com.gsutil.Utils;
import com.gsutil.UtilsNet;
import com.gsutil.WSSoap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

public class Comunicaciones extends AppCompatActivity implements ConfigurarWEBFragment.OnHeadSelectedListener, BackupFotosFragment.OnHeadSelectedListener, AsyncResponse {

    public final int REQUEST_CODE = 6878;//Ax para enviar a actividad
    public String URL;
    ImageView imageView2;
    Button btnCargar;
    // Button btnBackupFotos;
    EditText txtCiclo;
    EditText txtMunicipio;
    EditText txtSeccion;
    EditText txtDivision;
    ImageView iconWsOn;//Axx
    ImageView iconWsOff;//Axx
    long tamLongitRegD = 0;
    long tamFilasArchivoD = 0;
    String serialPDA = "000";
    String datoIP = "";
    String nivelOperador;
    String rutaAdministrador = "";

    String Ciclo = "0000";//Ax se guardan en vez de capturar de txt...
    String Municipio = "000";
    String Seccion = "000";
    String Division = "0";
    String paginaWs = "";

    int trama = 50; //Ax: Por defecto
    int puerto = 85; //Ax: Por defecto
    int tamanoArchivo = 0; //Ax: tamano obtenido de lo que hay que bajar o tambien de lo que se va a enviar(descarga)

    AlertDialog levelDialog;

    String metodo = ""; //Ax: lleva el metodo a ejecutar en Asyntask
    String banderaMet2llamo = ""; //Ax: metodo que llamo a metodo tamanoarchivo
    boolean banderaWsOcupado = false; //Ax: es para inhabilitar acciones mientras se eejcta asyntask
    boolean pruebawsok = false; //Ax: se envia a la nueva ventana para no probar 2 veces ws
    DatosWS wsoap = new DatosWS("", "", "", 0);//Axx
    Utils util = new Utils();
    File logfile; //Ax: Es para los log de error
    TextView txtInfo;
    TextView lblTxtArchivo;
    String NombreZip = ""; //Ax: archivo zip por recibir
    String ArchivoAEnviarRecibir = ""; //Ax: Ruta servidor del archivo zip por recibir
    String RutaAGrabar = ""; //Ax: Ruta dispositivo del archivo zip por recibir
    String esComprimido = "";
    String tipodecarga = ""; //Ax: "cargai"  ,"cargaf",  descargai
    List<String> zips; //Ax: para guardar las fotos por enviar
    String serial = "";
    String ruta = "";
    boolean validaEnvio = false;
    String RutaAdministrador = "";
    private String TAG = "Comunicaciones";
    //kim
    private Context context;
    private Autoupdater updater;
    //private ProgressDialog dialog = null;
    private double currentVersionCode;
    DecimalFormat decformt = new DecimalFormat("#.##");
    public int cantfotos;
    public int cantNofotos;
    public int[] posicfotocont;
    public int[] posicLblCont;

    //nuevas variables caqueta envio de archivo
    int RegistrosLeidos = 0;
    int CausasLeidas = 0;
    Utils utils = new Utils();//Ax log y utilidades
    String externalpath = "";
    Context ctx;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comunicaciones); //Ax: antes era: (R.layout.activity_main);
        ctx = this;
        Bundle bundle = getIntent().getExtras();

        serialPDA = bundle.getString("CODIGO_INTERNO_PDA");
        nivelOperador = bundle.getString("NIVEL_OPERADOR");

        txtCiclo = (EditText) findViewById(R.id.txtCiclo);
        txtMunicipio = (EditText) findViewById(R.id.txtMunicipio);
        txtSeccion = (EditText) findViewById(R.id.txtSeccion);
        txtDivision = (EditText) findViewById(R.id.txtDivision);
        iconWsOff = (ImageView) findViewById(R.id.iconwebserviceoff);
        iconWsOn = (ImageView) findViewById(R.id.iconwebserviceon);

        Button btnTraerRuta = (Button) findViewById(R.id.btnTraerRuta);
        Button btnRecibirDatos = (Button) findViewById(R.id.btnRecibirDatos);
        Button btnEnviarDatos = (Button) findViewById(R.id.btnEnviarDatos);
        Button btnValidaEnvio = (Button) findViewById(R.id.btnValidaEnvio);
        Button btnEnviarFotos = (Button) findViewById(R.id.btnEnviarFotos);
        Button btnBackupFotos = (Button) findViewById(R.id.btnBackupFotos);

        Button btnConfigurarWeb = (Button) findViewById(R.id.btnConfigurarWeb);
        Button btnBorrarBackupFotos = (Button) findViewById(R.id.btnBorrarBackupFotos);

        txtInfo = (TextView) findViewById(R.id.txtInfo);
        lblTxtArchivo = (TextView) findViewById(R.id.lblTxtArchivo);

        //txtCiclo.setFilters(new InputFilter[] {new InputFilter.AllCaps()});//pasa texto a mayusculas

        wsoap.delegate = this;//Axx Ax: tarea asincrona para enviar datos aqui a processFinish

        verificarArchivos();
       // ProbarConn();  //Ax: comunicaciones es el unico que comprueba primero la conexion, las otras actividades no.
        ProbarWS();
        SetIconButtons();

        //kim version de codigo
        try {
            PackageInfo pckginfo = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
            currentVersionCode = Double.parseDouble(pckginfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        externalpath = ctx.getExternalFilesDir(null).getParent();
        String hardcoding = "/Android/data/";//Ax: todo: cambiar este hardcoding

        if (externalpath.contains(hardcoding)) {
            externalpath = externalpath.substring(0, externalpath.indexOf(hardcoding));
        }

        serial = serialPDA;// getSerialNumber();
        ruta = externalpath + "/download/Simfa" + decformt.format(currentVersionCode + 0.1) + ".apk";

        logfile = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/LOGEVENTOS.LOG");

//        if (nivelOperador != "A" && nivelOperador != "S") {
//            // TODO: txtdescripcion.Enabled = false;
//            // txtuno.Enabled = false;
//            // txtdos.Enabled = false;
//            // txttres.Enabled = false;
//            // txtcuatro.Enabled = false;
//            // cmdaceptar.Enabled = false;
//        }


        btnTraerRuta.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ValidarVersion();
                //TraerRuta();
            }
        });

        btnEnviarFotos.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EnviarFotosMensaje();
            }
        });

        btnBorrarBackupFotos.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                borraBackupFotos();
            }
        });

        btnBackupFotos.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                crearBackupFotos();
            }
        });

        btnValidaEnvio.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                validaEnvio = true;
                validarEnvio();
            }
        });

        btnRecibirDatos.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                RecibirDatos();
            }
        });

        btnEnviarDatos.setOnClickListener(new View.OnClickListener() {//78
            public void onClick(View v) {
                EnviarDatosMensaje1();
            }
        });

        btnConfigurarWeb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent myIntent = new Intent(Comunicaciones.this, com.gselectroCaqueta.comunicaciones.Configurar_webIp.class);
                myIntent.putExtra("nivelOperador", nivelOperador);
                myIntent.putExtra("pruebawsok", pruebawsok);
                myIntent.putExtra("trama", trama + "");
                Comunicaciones.this.startActivityForResult(myIntent, REQUEST_CODE);
            }
        });

        //Ax: para regresar a main
        findViewById(R.id.btnRegresar).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                salir();
            }
        });

        //kim: validar version de apk
        findViewById(R.id.btnValidarVersion).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //   comenzarActualizar();
                validarV();
            }
        });

        leeTomoLectura();
        //Log.e("error",RegistrosLeidos+" datos "+CausasLeidas);
    }

    private void salir() {
        //Ax: comentado genera problemas conexion printer
        //startActivity(new Intent(Comunicaciones.this, com.globalsolutions.accesoyseguridad.GuiAcceso.class));
        this.finish();
    }

    //Ax. para mensajes generales
    private void mensajes(String msg) {
        // Toast.makeText(Comunicaciones.this, msg, Toast.LENGTH_LONG).show();
        Toast toast = Toast.makeText(Comunicaciones.this, msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 10, 50);
        toast.show();
    }

    //Ax: recibe el resultado ejecutado de la clase async task del metodo onPostExecute(result).
    public void processFinish(String output) {

    }

    //Ax: coloca iconos pequeños a los botones
    private void SetIconButtons() {

        try {

            Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.btnindocs42, null);
            Button btn = (Button) findViewById(R.id.btnRecibirDatos);
            drawable.setBounds(0, 0, 40, 40);
            btn.setCompoundDrawables(drawable, null, null, null);

            Drawable drawable2 = ResourcesCompat.getDrawable(getResources(), R.drawable.traerruta, null);
            Button btn2 = (Button) findViewById(R.id.btnTraerRuta);
            drawable2.setBounds(0, 0, 40, 40);
            btn2.setCompoundDrawables(drawable2, null, null, null);

//            Drawable drawable3 = ResourcesCompat.getDrawable(getResources(), R.drawable.borrarbckpfotos, null);
//            Button btn3 = (Button) findViewById(R.id.btnBorrarBackupFotos);
//            drawable3.setBounds(0, 0, 40, 40);
//            btn3.setCompoundDrawables(drawable3, null, null, null);

            Drawable drawable4 = ResourcesCompat.getDrawable(getResources(), R.drawable.enviarfotos, null);
            Button btn4 = (Button) findViewById(R.id.btnEnviarFotos);
            drawable4.setBounds(0, 0, 40, 40);
            btn4.setCompoundDrawables(drawable4, null, null, null);

            Drawable drawable5 = ResourcesCompat.getDrawable(getResources(), R.drawable.enviardatos, null);
            Button btn5 = (Button) findViewById(R.id.btnEnviarDatos);
            drawable5.setBounds(0, 0, 40, 40);
            btn5.setCompoundDrawables(drawable5, null, null, null);

            Drawable drawable6 = ResourcesCompat.getDrawable(getResources(), R.drawable.validarenvio, null);
            Button btn6 = (Button) findViewById(R.id.btnValidaEnvio);
            drawable6.setBounds(0, 0, 40, 40);
            btn6.setCompoundDrawables(drawable6, null, null, null);

        } catch (Exception ex) {
            mensajes("Error al cargar iconos");
        }
    }

    private void verificarArchivos() {  //Axx:? no tengo idea que trataba de hacer no coincide con el codigo original se deja

//        String rutaAGrabar = "";
//
//        int respuestaWeb = -10;
//
//        String archivo_nuevos = VariablesGlobales.directorioactual + "/DatosDeEntrada/Nombre";

        String nombreArchivo = VariablesGlobales.directorioactual + "/ARCHIVOSCARGA.CFI";
        File file = new File(nombreArchivo);

        //Ax: no existe "ArchivosCarga.cfi" advertir y no hacer nada
        if (!file.exists()) {

            banderaWsOcupado = true;//tratar de bloquear toda funcion
            txtInfo.setText("No hay Archivo de Soporte...\n ArchivosCarga.cfi");
            mensajes("No hay Archivo de Soporte...\n //ArchivosCarga.cfi");
            return;
        }

        cargarArchivoNombre();

        int contador = 0;
        try {
            FileReader r = new FileReader(nombreArchivo);
            BufferedReader reader = new BufferedReader(r);
            String linea;

            while ((linea = reader.readLine()) != null) {

                if (contador < 1) { // tomar la ruta del sistema administrativo
                    rutaAdministrador = linea.trim();

                    if (!rutaAdministrador.contains(":")) {
                        mensajes("No hay Archivo de Soporte...\n //ArchivosCarga.cfi");
                        btnCargar.setEnabled(true);
                        return;
                    }
                }

                if (contador == 1) { // Ax: sin control de error
                    esComprimido = linea.trim();

                    if (esComprimido.trim().equals("ZIP SI")) {

                        esComprimido = "1";
                    } else {

                        esComprimido = "0";
                    }
                }
                contador++;
            }
            r.close();
        } catch (IOException e) {

            e.printStackTrace();
        }
        getParamsWs();
        //seleccionUrl();
    }

    //Ax: Lee y carga del archivo "Nombre" si existe
    private void cargarArchivoNombre() {

        String linea = "";
        String archivo_nombre = VariablesGlobales.directorioactual + "/DATOSDEENTRADA/NOMBRE";
        File file = new File(archivo_nombre);

        if (file.exists()) {//Ax: se comprueba y carga si existe

            try {
                FileReader r = new FileReader(archivo_nombre);
                BufferedReader reader = new BufferedReader(r);

                while ((linea = reader.readLine()) != null) {
                    if (linea.trim() == "") {
                        continue;
                    } else break;
                }

                linea = linea.substring(0, 12).trim();

                Ciclo = linea.substring(0, 4);

                try {
                   //se comenta int x = Integer.parseInt(Ciclo);//Ax: Si tiene caracteres raros fallara

                    Municipio = linea.substring(5, 8);
                    Seccion = linea.substring(8, 11);
                    Division = linea.substring(11, 12);

                    txtCiclo.setText(Ciclo);
                    txtMunicipio.setText(Municipio);
                    txtSeccion.setText(Seccion);
                    txtDivision.setText(Division);

                    //Axx????   nombreArchivo = Ciclo + "C" + Municipio + Seccion + Division + "     .N";

                    // Toast.makeText(getApplicationContext(), "La terminal ya esta \ncargada debe Borrar o Mover\n datos y cargar la nueva Ruta si\n es lo que requiere", Toast.LENGTH_LONG).show();
                    return;

                } catch (Exception e) {
                    Log.e("error","nombre "+e.getMessage());
                    Ciclo = "0000";
                    Municipio = "000";
                    Seccion = "000";
                    Division = "0";
                }
            } catch (Exception ex) {
                mensajes("Problema con la carga de:\n Archivo: Ciclo, Municipio...etc.");
                //Toast.makeText(getApplicationContext(), "Problema con la carga de:\n Archivo: Ciclo, Municipio...etc.", Toast.LENGTH_LONG).show();
            }
        }

        txtCiclo.setText(Ciclo);
        txtMunicipio.setText(Municipio);
        txtSeccion.setText(Seccion);
        txtDivision.setText(Division);

        String directorioSalida = VariablesGlobales.directorioactual;
    }

    //Ax: crea el archivo "Nombre" si la carga fue exitosa
    private void crearArchivoNombre() {

        String archivo_nombre = VariablesGlobales.directorioactual + "/DATOSDEENTRADA/NOMBRE";
        File file = new File(archivo_nombre);

        if (file.exists()) file.delete();

        try {
            FileWriter writer = new FileWriter(file);

            String x = (txtCiclo.getText().toString().trim()).replace(" ", "0");
            Ciclo = String.format("%1$4s", x);
            Municipio = String.format("%1$3s", txtMunicipio.getText().toString().trim()).replace(" ", "0");
            Seccion = String.format("%1$3s", txtSeccion.getText().toString().trim()).replace(" ", "0");
            Division = String.format("%1$1s", txtDivision.getText().toString().trim()).replace(" ", "0");

            writer.append(Ciclo + "C" + Municipio + Seccion + Division);
            writer.flush();
            writer.close();

        } catch (Exception e) {

            mensajes("Error al crear \nNombre");
            file.delete();
        }
    }

    public void getParamsWs() {
        try {
            CrudComunicaciones crudComuni = new CrudComunicaciones(this);
            BDComunicaciones bdc = crudComuni.getParams();
            if (bdc != null) {
                URL = "http://" + bdc.getURL();
                paginaWs = bdc.getPaginaWs();
            }
        } catch (Exception ignored) {
        }
    }

    // nuevo proceso para capturar la ippublica
    /*private void seleccionUrl() {
        try {
            String nombreArchivo = VariablesGlobales.directorioactual + "/DIRECCIONEIP.TXT";
            File file = new File(nombreArchivo);

            if (!file.exists()) {
                RandomAccessFile writer2 = new RandomAccessFile(file, "rw");

                String ip_puerto = "186.117.240.195;8080;000                 ;GPRS                ;/caqueta.asmx                                                                                                 ;A\r\n" +
                                   "186.117.240.195;8081;000                 ;GPRS                ;/caqueta.asmx                                                                                                 ;I";

                writer2.writeBytes(ip_puerto);
                writer2.close();
            }

            if (file.exists()) {

                FileReader stream3 = new FileReader(nombreArchivo);
                BufferedReader reader = new BufferedReader(stream3);
                String linea = "";
                int cont = 0;

                while ((linea = reader.readLine()) != null) {

                    datoIP = linea;

                    if (datoIP.substring(datoIP.length() - 1, datoIP.length()).trim().equals("A")) {
                        URL = "http://" + datoIP.substring(0, 15).trim() + ":" + datoIP.substring(16, 20).trim();
                        paginaWs = datoIP.substring(64, 157).trim();
                        cont++;
                        break;
                    }
                }

                if (cont < 1) {
                    file.delete();
                    seleccionUrl();
                    return;
                }
                reader.close();
            }
            URL = URL.replace(" ", "");

        } catch (IOException e) {
            e.printStackTrace();
            mensajes("Error, verificar Archivo de Configuracion IP");
        }
    }*/

    @Override
    public void onArticleSelected(View v) {

        int id = v.getId();

        switch (id) {
            case R.id.btnPruebaWS:
                imageView2.setImageDrawable(getResources().getDrawable(R.drawable.redactiva));
                // TODO: PENDIENTE VERIFICACION DEL OPTIMO FUNCIONAMIENTO DE
                // ESTA
                // RUTINA Y REEMPLAZO DEL METODO OBSOLETO
                // ConfigurarWEBFragment bkf =
                // (ConfigurarWEBFragment)getSupportFragmentManager().getFragments().get(1);
                // if(bkf.esVisibleRed()){
                //
                // imageView2.setImageDrawable(getResources().getDrawable(R.drawable.redactiva));
                // }
                // else{
                //
                // imageView2.setImageDrawable(getResources().getDrawable(R.drawable.redinativa));
                // }

                break;

            case R.id.cmdAceptar:

                break;

            default:
                break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                try {
                    trama = Integer.parseInt(data.getStringExtra("trama"));
                    puerto = Integer.parseInt(data.getStringExtra("puerto"));
                    pruebawsok = data.getBooleanExtra("pruebawsok", false);
                    URL = data.getStringExtra("url");
                    paginaWs = data.getStringExtra("paginaWs");

                } catch (Exception ex) {
                    mensajes("Error obteniendo info\n de Configurar IP");
                }
            }
        }
    }

    public void ProbarWS() {

        if (banderaWsOcupado) return; //Ax: si hay una conexion abierta no hace nada

        metodo = "VALIDAR_CONEXION";

        try {
            AsyncCallWS task = new AsyncCallWS();
            task.execute("");
            banderaWsOcupado = true;

        } catch (Exception ex) {
            mensajes(ex.getMessage());
        }
    }

    public void ProbarConn() {

        if (banderaWsOcupado) return; //Ax: si hay una conexion abierta no hace nada

        metodo = "HAY_CONEXION";

        try {
            AsyncCallWS task = new AsyncCallWS();
            task.contexto = this; //adjuntar el contexto
            task.execute("");
            banderaWsOcupado = true;

        } catch (Exception ex) {
            mensajes(ex.getMessage());
        }
    }

    public boolean ExisteNombre() {

        String archivo_nombre = VariablesGlobales.directorioactual + "/DATOSDEENTRADA/NOMBRE";
        File file = new File(archivo_nombre);

        if (file.exists()) {
            mensajes("La terminal ya esta cargada \ndebe Borrar o Mover datos \ny cargar la nueva Ruta si\n es lo que requiere");
            return true;
        }
        return false;
    }

    public void TraerRuta() {

        if (banderaWsOcupado) return;

        if (!pruebawsok) {
            mensajes("No hay conexion con el Servicio web");
            return;
        }

        if (ExisteNombre()) return;

        try {
            metodo = "RutasProgramadas";
            Ciclo = String.format("%1$4s", txtCiclo.getText().toString().trim()).replace(" ", "0");

            txtCiclo.setText(Ciclo);

            AsyncCallWS task = new AsyncCallWS();
            task.execute("");//Ax: continua aqui, en AsyncCallWS.doinback...
            banderaWsOcupado = true;//Bandera

        } catch (Exception ex) {
            mensajes("Error al traer ruta");
            banderaWsOcupado = false;//Bandera
        }
    }

    public void RecibirDatos() { //Ax: Se ejecuta totalmente despues de la comprobacion alertdialod en RecibirDatosRun

        if (banderaWsOcupado) return;

        if (!pruebawsok) {
            mensajes("No hay conexion con el Servicio web");
            return;
        }

        if (ExisteNombre()) return;

        AlertDialogEscoger("Alerta de transmision de datos!", "Desea cargar Archivos?", "RecibirDatos2");
    }

    public void RecibirDatos2() {

        try {

            Ciclo = String.format("%1$4s", txtCiclo.getText().toString().trim()).replace(" ", "0");
            Municipio = String.format("%1$3s", txtMunicipio.getText().toString().trim()).replace(" ", "0");
            Seccion = String.format("%1$3s", txtSeccion.getText().toString().trim()).replace(" ", "0");
            Division = String.format("%1$1s", txtDivision.getText().toString().trim()).replace(" ", "0");

        } catch (Exception ex) {
            mensajes("error al capturar Ciclo, Municip,Secc. y Div.");
            util.Log(logfile, "[Comunicaciones]RecibirDatos2(): " + ex.getMessage());
            return;
        }

        NombreZip = Municipio + Seccion + Division + ".ZIP";
        ArchivoAEnviarRecibir = rutaAdministrador.trim() + "CIC" + Ciclo + "\\C" + NombreZip;
        RutaAGrabar = VariablesGlobales.directorioactual + "/DATOSDEENTRADA/" + "C" + NombreZip;

        lblTxtArchivo.setText("C" + NombreZip);
        txtCiclo.setText(Ciclo);
        txtMunicipio.setText(Municipio);
        txtSeccion.setText(Seccion);
        txtDivision.setText(Division);

        WsGetTamanoArchivo(NombreZip, ArchivoAEnviarRecibir, RutaAGrabar);
        banderaMet2llamo = "RecibirDatos3";
    }

    public void RecibirDatos3(String resp) {

        metodo = "ENVIAR_DEL_SERVIDOR_AL_PDA";
        AsyncCallWS task = new AsyncCallWS();
        task.execute("");//Ax: continua aqui, en AsyncCallWS.doinback...
        banderaWsOcupado = true;//Bandera conexion ocupada
    }

    public void RegistProgramacEnrut() {

        try {
            metodo = "REGISTRAR_PROGRAMACION_ENRUTADOR";
            AsyncCallWS task = new AsyncCallWS();
            task.execute("");//Ax: continua aqui, en AsyncCallWS.doinback...
            banderaWsOcupado = true;//Bandera conexion ocupada

        } catch (Exception ex) {
            mensajes(ex.getMessage());
            banderaWsOcupado = false;//Bandera conexion ocupada
            banderaMet2llamo = "";
        }
    }

    public void EnviarDatosMensaje1() {

        if (banderaWsOcupado) return;

        File file = new File(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/NOMBRE");
        File file2 = new File(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/LEIDO.TXT");



        if (!file.exists()) {
            mensajes("Terminal NO tiene\n ruta cargada");
            return;
        }

        //validar
        //nuevo para crear archivo EnviosGPRSHilos desde ENVIOGPRS" + serialPDA
        String EnviosGprsda = VariablesGlobales.directorioactual + "/DATOSDESALIDA/ENVIOGPRS" + serialPDA + ".SDA";
        String EnviosGprsHilos = VariablesGlobales.directorioactual + "/DATOSDESALIDA/EnviosGPRSHilos.SDA";


            File Archivo3 = new File(EnviosGprsHilos);
            if (Archivo3.exists())
                Archivo3.delete();
            //copiar a EnviosGPRSHilos para el envio
            VariablesGlobales.copyFile(EnviosGprsda, EnviosGprsHilos, false);


       /* if (!file2.exists()) {
            AlertDialogEscoger("Alerta de transmision de datos!", "LAS RUTAS NO HAN SIDO LEIDAS\n" + "EN SU TOTALIDAD, DESEA CONTINUAR?", "EnviarDatosMensaje2");
            return;
        } else {*/
            EnviarDatosMensaje2();
       // }
    }

    public void EnviarDatosMensaje2() {

        File file = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/ENVIOSGPRS.SDA");

        if (!file.exists()) {
            AlertDialogEscoger("Alerta de transmision de datos!", "ALERTA! HAY RUTAS SIN\n" + "ENVIAR AL SERVIDOR, DESEA CONTINUAR?", "EnviarDatosMensaje3");
            return;
        } else {
            EnviarDatosMensaje3();
        }
    }

    public void EnviarDatosMensaje3() {
        AlertDialogEscoger("Alerta de transmision de datos!", "Desea enviar los datos ?\n" + "", "EnviarDatosServer");
        return;
    }

    public void EnviarDatosServer() { //858

        txtInfo.setText("");
        NombreZip = "D" + Municipio + Seccion + Division + ".ZIP";

        RutaAGrabar = VariablesGlobales.directorioactual + "/DATOSDESALIDA/";
        banderaMet2llamo = "EnviarDatosServer";//Bandera puesta, ya que otros metodos invocan este RegistProgramacEnrut
        tipodecarga = "descargai";

        try {
            metodo = "EnviarDatosServer";
            AsyncCallWS task = new AsyncCallWS();
            task.execute("");
            banderaWsOcupado = true;

        } catch (Exception ex) {
            mensajes(ex.getMessage());
            banderaWsOcupado = false;//Bandera conexion ocupada
            banderaMet2llamo = "";
            util.Log(logfile, "[Comunicaciones]EnviarDatosServer(): " + ex.getMessage());
        }
    }

    public String EnviarDatosServerAsync() {

        try {

            WSSoap wsoap = new WSSoap(URL, paginaWs);

            String rutaCiclo = rutaAdministrador.trim() + "CIC" + Ciclo + "\\";
            String rutaDescargaZip = rutaAdministrador.trim() + "CIC" + Ciclo + "\\DESCARGASENVIOGPRS" + "\\";
            ArchivoAEnviarRecibir = rutaAdministrador.trim() + "CIC" + Ciclo + "\\D" + Municipio + Seccion + Division + "\\"; //D:ENRUTADOR_X/CIC0809/C1851011/ DESCARGASENVIOGPRS

            metodo = "VerificarSiexiste_Directorio_Archivo";
            String respuesta = wsoap.VerificarSiexisteDirectorioArchivo(metodo, rutaDescargaZip, NombreZip);//Ax: Se comprueba si existe la ruta destino remota

            if (!respuesta.equals("true")) {
                return "Error No se ha encontrado destino en el servidor!";
            }
            //Ax: Se registra un Programacion Enrutados Descargai
            //Log.e("error",serialPDA+"-"+rutaCiclo+" datos "+tipodecarga+"-"+tamanoArchivo+"-"+rutaDescargaZip + NombreZip+"-"+(Municipio + Seccion + Division)+"-"+RegistrosLeidos+"-"+CausasLeidas);
            metodo = "REGISTRAR_PROGRAMACION_ENRUTADOR";
            respuesta = wsoap.RegistrarProgramEnrutadosr(metodo, rutaCiclo, tipodecarga, (tamanoArchivo + ""), (serialPDA + ""), rutaDescargaZip + NombreZip, (Municipio + Seccion + Division),RegistrosLeidos,CausasLeidas);
            //Log.e("error","registro1 "+respuesta);

            if (!respuesta.equals("1")) {
                return "Error No se ha podido registrar Descargai";
            }

            if (Integer.parseInt(esComprimido) == 1) { //Ax: Se procede a crear el ZIP

                ArrayList filestoZip = new ArrayList();
                File f = new File(RutaAGrabar);//Ax: ruta donde buscar los archivos
                File[] files = f.listFiles();//array de Files de la carpeta
                //validar
                for (int i = 0; i < files.length; i++) {

                    File file = files[i];
                    //Log.e("error","nombre archivo "+file.getName());

                    if (!file.isDirectory() && file.getName().toUpperCase().endsWith(".SDA") && !file.getName().toUpperCase().equals("ENVIOGPRS" + serialPDA + ".SDA") || file.getName().equals("TOMOLECTURA.TXT") || file.getName().equals("LOGEVENTOS.LOG")) {
                        filestoZip.add(file);
                    }
                }

                String comprimir = util.CreaZip(RutaAGrabar + NombreZip, filestoZip);
                Log.e("error","respuesta comprime "+comprimir);
                if (!comprimir.contains("✓")) {
                    return "Error Comprimiendo \n Archivos por enviar";
                }
            }

            String hash = util.Md5Hash(RutaAGrabar + NombreZip);

            tamanoArchivo = (int) (new File(RutaAGrabar + NombreZip).length());

            metodo = "Terminal_ToServerReceive";
            respuesta = wsoap.TerminalToServerReceive(metodo, rutaDescargaZip, Integer.parseInt(esComprimido), RutaAGrabar + NombreZip, trama);//Enviar Datos al servidor

            if (!(respuesta.equals("4") || respuesta.equals("1"))) {
                return "Error Enviar Archivo al servidor";
            }
            tipodecarga = "descargaf";

            //Ax: Se registra un Programacion Enrutados Descargaf
            metodo = "REGISTRAR_PROGRAMACION_ENRUTADOR";
            respuesta = wsoap.RegistrarProgramEnrutadosr(metodo, rutaCiclo, tipodecarga, (tamanoArchivo + ""), (serialPDA + ""), rutaDescargaZip + NombreZip, (Municipio + Seccion + Division),RegistrosLeidos,CausasLeidas);

            Log.e("error","registro "+respuesta);
            if (!respuesta.equals("1")) {
                return "Error No se ha podido registrar Descargaf";
            }
            metodo = "Descomprime";
            respuesta = wsoap.Descomprimir(metodo, ArchivoAEnviarRecibir, rutaDescargaZip + NombreZip, hash, serialPDA);

            if (!respuesta.equals("true")) {
                return " Error descomprimir remoto, intentelo de nuevo";
            }

            File file = new File(RutaAGrabar + NombreZip);
            if (file.exists()) file.delete();


        } catch (Exception ex) {
            txtInfo.setText("Error al enviar archivos al servidor, intente de nuevo");
            util.Log(logfile, "[Comunicaciones]EnviarDatosServerAsync(): " + ex.getMessage());
        }
        metodo = "EnviarDatosServer";
        return "Proceso subir archivos Exitoso! ✓";
    }

    public void EnviarFotosMensaje() {

        RutaAGrabar = VariablesGlobales.directorioactual + "/DCIM/FOTOGRAFIASL";

        File f = new File(RutaAGrabar);

        if (!f.exists()) {

            if (!f.mkdirs()) {
                mensajes("No se pudo crear carpeta de fotografias");
                txtInfo.setText("No se pudo crear carpeta de fotografias");
            }
            mensajes("No existe carpeta de fotografias");
            txtInfo.setText("No existe carpeta de fotografias");
            return;
        }

        if (banderaWsOcupado) return;

        AlertDialogEscoger("Alerta de transmision de datos!", "Comenzara un envio masivo de fotos, esto puede tomar tiempo \nDesea enviar las fotos?", "EnviarFotos");
        return;
    }

    /***
     * Busca la ruta de backup y hace una copia de la carpeta de fotografias al backup
     */
    private void crearBackupFotos() {


        List<String> ListaFotos = new ArrayList<String>(); //Ax: Contendra lista de solo  archivos JPG,PNG

        File pathFotos = new File(VariablesGlobales.directorioactual + "/DCIM/FOTOGRAFIASL");//Ax: ruta donde buscar los archivos

        try {
            File[] files = pathFotos.listFiles();//array de Archivos y carpetas de la ruta

            for (int i = 0; i < files.length; i++) {

                File file = files[i];

                if (!file.isDirectory() && file.getName().toUpperCase().endsWith(".JPG") || !file.isDirectory() && file.getName().toUpperCase().endsWith(".PNG")) {
                    ListaFotos.add(file.getAbsolutePath());
                }
            }

            if (files.length < 1) {
                mensajes(" Nada para backup ");
                return;
            }

            int cont = 0;

            if (ListaFotos.size() > 0) {

                File pathFotosBck = new File(VariablesGlobales.directorioBackUp + "/DCIM/FOTOGRAFIASL");

                if (!pathFotosBck.exists()) {
                    pathFotosBck.mkdirs();
                }

                for (String fullPath : ListaFotos) {
                    String fb = fullPath.replace(VariablesGlobales.directorioactual, VariablesGlobales.directorioBackUp).replace("//", "/");
                    if (!util.CrearCopiaSin(fullPath, fb)) {
                        cont++;
                    }
                }
            }

            if (cont > 0) {
                mensajes(cont + " archivos problematicos");
            } else {
                mensajes(" Backup Finalizado ");
            }

        } catch (Exception ex) {
            mensajes("Error al crear Backup Fotos");
        }
    }

    /***
     * Busca la ruta de backup fotografias y la borra
     */
    private void borraBackupFotos() {

        try {
            int cont = 0;
            File pathFotosBck = new File(VariablesGlobales.directorioBackUp + "/DCIM/FOTOGRAFIASL");

            if (pathFotosBck.isDirectory()) {
                String[] children = pathFotosBck.list();
                for (int i = 0; i < children.length; i++) {
                    if (!new File(pathFotosBck, children[i]).delete()) {
                        cont++;
                    }
                }
                if (children.length < 1) {
                    mensajes(" Nada por borrar ");
                    return;
                }
            }
            File pathLbls = new File(VariablesGlobales.directorioactual + "/LBLS");
            if (pathLbls.isDirectory()) {
                String[] childrenlbls = pathLbls.list();
                for (int i = 0; i < childrenlbls.length; i++) {
                    if (!new File(pathLbls, childrenlbls[i]).delete()) {
                        cont++;
                    }
                }
                if (childrenlbls.length < 1) {
                    mensajes(" Nada por borrar ");
                    return;
                }
            }

            if (cont > 0) {
                mensajes(cont + " archivos problematicos");
            } else {
                mensajes(" Borrado Finalizado ");
            }

        } catch (Exception ex) {
            mensajes("Error al crear Backup Fotos");
        }
    }

    public void EnviarFotos() {  //**********************************************************************

        try {
            String respuesta;
            int cantFotos = 20;
            int contadorZipFotos = 1;
            NombreZip = VariablesGlobales.directorioactual + "/DATOSDEENTRADA/" +  "FOTOS" + Municipio + Seccion + Division + ".ZIP";//No tiene el .ZIP

            zips = new ArrayList<String>(); //Ax: Contendra solo lista de archivos ZIP
            List<String> fotos = new ArrayList<String>(); //Ax: Contendra lista de solo  archivos JPG
            //File f = new File(RutaAGrabar);//Ax: ruta donde buscar los archivos
            int desde = 51;
            int hasta = 52;

            FileReader r = new FileReader(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/" + "F" + Ciclo + Municipio + Seccion + Division); //Ax: se recorre el archivo para capturar las no eviadas o en envio
            BufferedReader reader = new BufferedReader(r);
            String linea;
            int conteo = 0;
            int conteoPos = 0;
            int conteono = 0;
            posicfotocont = new int[cantFotos + 1];//Ax: va a guardar la posicion real de la linea

            while ((linea = reader.readLine()) != null) {
                conteo++;
                if (linea.substring(desde, hasta).equals("X") || linea.substring(desde, hasta).equals("_")) {
                    String nombref = linea.substring(0, 50).trim();
                    String fechaHora = "" , latitudr = "", longitudr = "";
                    File fotop = new File(VariablesGlobales.directorioactual + "/DCIM/FOTOGRAFIASL/" + nombref);
                    File cuentasgps = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/CUENTASGPS.SDA");
                    if (fotop.length() > 100000){
                        if(cuentasgps.exists()){
                            String readacount = "";
                            FileReader stream3 = new FileReader(VariablesGlobales.directorioactual + "/DATOSDESALIDA/CUENTASGPS.SDA");
                            BufferedReader readers = new BufferedReader(stream3);
                            String cuentaf = nombref.substring(0,9);
                            while ((readacount = readers.readLine()) != null) {
                                String[] dato = readacount.split(";");
                                if(cuentaf.equals(dato[0].trim())){
                                    Log.e("INFO","fechaHora: " + dato[6].replace(" ","-") + "latitudr: " + dato[1].trim() + "longitudr: " + dato[2].trim());
                                    fechaHora = dato[6].replace(" ","-");
                                    latitudr = dato[1].trim();
                                    longitudr = dato[2].trim();
                                }
                            }
                            readers.close();
                            utils.ReduceImagen2(VariablesGlobales.directorioactual + "/DCIM/FOTOGRAFIASL/"+nombref,fechaHora + " C: " + latitudr + " : " + longitudr);
                            continue;
                        }
                    }

                    if (fotop.exists()) {
                        conteoPos++;
                        if (conteoPos <= 20) {
                            fotos.add(VariablesGlobales.directorioactual + "/DCIM/FOTOGRAFIASL/" + nombref);
                            posicfotocont[conteoPos] = conteo;
                        }
                    }else{
                        cantNofotos++;
                    }
                }
            }
            r.close();
            cantfotos = conteoPos;

            if (fotos.size() > 0) {

                int i = 1;
                ArrayList filestoZip = new ArrayList(); //Contiene los archivos por comprimir

                for (String x : fotos) { //Recorrer cada jpg encontrado para añadirlo

                    filestoZip.add(new File(x));

                    /*if (filestoZip.size() == cantFotos || i == fotos.size()) {//Si el arreglo llega a el limite de fotos por comprimir en una tanda

                        String nombrearchivozip;

                        if (contadorZipFotos <= 9) {
                            nombrearchivozip = RutaAGrabar + NombreZip + contadorZipFotos + ".ZIP"; //Ej: /sdcard01/Fotogra.../FOTOS....

                        } else { //Ax: si el munero es mas grande que 10 se cambia el ultimo numero del nombre por una letra
                            nombrearchivozip = sufijo(contadorZipFotos);
                            nombrearchivozip = RutaAGrabar + NombreZip + nombrearchivozip + ".ZIP";
                        }
                        contadorZipFotos++;

                        //zips.add(nombrearchivozip);//Guarda nombre de los zips, esto sera usado en el envio asincrono //Ax: error no deberia ir despues de crear?

                        respuesta = util.CreaZip(nombrearchivozip, filestoZip);

                        if (!respuesta.contains("✓")) {
                            mensajes("Error Comprimiendo \n Archivos por enviar");
                            txtInfo.setText("Error Comprimiendo \n Archivos por enviar");
                            return;
                        } else
                            zips.add(nombrearchivozip);//Guarda nombre de los zips, esto sera usado en el envio asincrono

                        filestoZip = new ArrayList();
                    }
                    i++;*/
                }//end for

                respuesta = utils.CreaZip(NombreZip, filestoZip);
                Log.e("error", "envio fotos " + respuesta);
                if (!respuesta.contains("✓")) {
                    txtInfo.setText("Error Comprimiendo Archivos por enviar");
                    return;
                }

                txtInfo.setText("");
                // ArchivoAEnviarRecibir = rutaAdministrador.trim() + "CIC" + CicloReal + "\\";

                try {
                    metodo = "EnviarFotosServer";
                    AsyncCallWS task = new AsyncCallWS();
                    task.execute("");
                    banderaWsOcupado = true;

                } catch (Exception ex) {
                    mensajes(ex.getMessage());
                    banderaWsOcupado = false;//Bandera conexion ocupada
                    banderaMet2llamo = "";
                    util.Log(logfile, "[Comunicaciones] EnviarFotosServer(): " + ex.getMessage());
                }

            } else {
                mensajes("No hay fotografia(s) para enviar");
                txtInfo.setText("No hay Fotografia(s) para enviar");
                Log.e("error3","entra a validar0 "+Ciclo.substring(1,3) );

                EnviarLbls();
//                if(Integer.parseInt(Ciclo.substring(1,3)) == 0 || Integer.parseInt(Ciclo.substring(1,3)) == 24 || Integer.parseInt(Ciclo.substring(1,3)) == 1 || Integer.parseInt(Ciclo.substring(1,3)) == 25
//                        || Integer.parseInt(Ciclo.substring(1,3)) == 6 || Integer.parseInt(Ciclo.substring(1,3)) == 23 || Integer.parseInt(Ciclo.substring(1,3)) == 10 || Integer.parseInt(Ciclo.substring(1,3)) == 11
//                        || Integer.parseInt(Ciclo.substring(1,3)) == 30 || Integer.parseInt(Ciclo.substring(1,3)) == 31 || Integer.parseInt(Ciclo.substring(1,3)) == 32 || Integer.parseInt(Ciclo.substring(1,3)) == 57) {
//                Log.e("error3","entra a validar "+Integer.parseInt(Ciclo) );
//                }

            }
        } catch (Exception ex) {
            mensajes("Error en el envio de fotos \n " + ex.getMessage());
            util.Log(logfile, "[Comunicaciones] EnviarFotosServer().: " + ex.getMessage());
        }
    }

    public void actualizaFotos() {
        try {
            int desde = 51;
            int hasta = 52;
            String path = VariablesGlobales.directorioactual + "/DATOSDEENTRADA/F" + Ciclo + Municipio + Seccion + Division;
            FileReader r = new FileReader(path); //Ax: se recorre el archivo para actualizar las eviadas
            File fileName = new File(path + "_temp");
            BufferedReader reader = new BufferedReader(r);
            String linea;
            int conteo = 0;

            while ((linea = reader.readLine()) != null) {
                conteo++;

                for (int i = 0; i < posicfotocont.length; i++) {
                    if (conteo == posicfotocont[i]) {
                        linea = linea.substring(0, desde) + "Y" + linea.substring(hasta);
                        continue;
                    }
                }
                util.EscribirLinea(fileName, linea + "\r\n");
            }
            r.close();

            File fpath = new File(path);
            try {
                if (reader != null) reader.close();

                fpath.delete();
            } catch (Exception ex) {
                mensajes("Error en la actualizacion de fotos.. \n " + ex.getMessage());
                util.Log(logfile, "[Comunicaciones] actualizaFotos()..: " + ex.getMessage());
            }
            fileName.renameTo(fpath);

        } catch (Exception ex) {
            mensajes("Error en la actualizacion de fotos \n " + ex.getMessage());
            util.Log(logfile, "[Comunicaciones] actualizaFotos().: " + ex.getMessage());
        }
    }
    public String EnviarFotosServerAsync() {//888

        try {
            WSSoap wsoap = new WSSoap(URL, paginaWs);
            File file = new File(NombreZip);
            String hash = utils.Md5Hash(NombreZip);
            ArchivoAEnviarRecibir = rutaAdministrador.trim() + "CIC" + Ciclo + "\\C" + Municipio + Seccion + Division + "\\"; //D:ENRUTADOR_X/CIC0809/C1851011/

            //String hash = util.Md5Hash(zip);

            if (hash.trim().equals("")) return "Error de hash";

            //File file = new File(zip);
            tamanoArchivo = (int) file.length();
            //Log.e("error", file.getName()+" directorio fotos "+ArchivoAEnviarRecibir + "\\FOTOGRAFIAS");

            metodo = "VerificarSiexiste_Directorio_Archivo";
            String respuesta = wsoap.VerificarSiexisteDirectorioArchivo(metodo, ArchivoAEnviarRecibir, file.getName());//Ax: Se comprueba si existe la ruta destino remota

            if (!respuesta.equals("true")) {
                util.Log(logfile, "[Comunicaciones]EnviarFotosServerAsync(VerificarSiexiste_Directorio_Archivo): " + respuesta);
                return "Error No se ha encontrado destino en el servidor!";
            }

            metodo = "Terminal_ToServerReceive";
            respuesta = wsoap.TerminalToServerReceive(metodo, ArchivoAEnviarRecibir, Integer.parseInt(esComprimido), NombreZip, trama);//Enviar Datos al servidor

            if (!(respuesta.equals("4") || respuesta.equals("1"))) {
                util.Log(logfile, "[Comunicaciones]EnviarFotosServerAsync(Terminal_ToServerReceive): " + respuesta);
                if (respuesta.contains("timeout") || respuesta.contains("Connection reset")){
                    return "Se cayo la conexion un momento re intente el envio";
                }
                return "Error Enviar Archivo al servidor";
            }

            metodo = "Descomprime";
            respuesta = wsoap.Descomprimir(metodo, ArchivoAEnviarRecibir + "FOTOGRAFIAS", ArchivoAEnviarRecibir + file.getName(), hash, serialPDA);

            if (!respuesta.equals("true")) {
                util.Log(logfile, "[Comunicaciones]EnviarFotosServerAsync(Descomprime): " + respuesta);
                return " Error descomprimir remoto,\n Intentelo de nuevo \n" + respuesta;
            }
            actualizaFotos();
            file.delete();
        } catch (Exception ex) {
            txtInfo.setText("Error al enviar fotos al servidor, intente de nuevo");
            util.Log(logfile, "[Comunicaciones]EnviarFotosServerAsync(): " + ex.getMessage());
        }
        metodo = "EnviarFotosServer";
        return "Proceso subir Fotos Exitoso! ✓";
    }

    public String sufijo(int num) {
        String st = "";

        switch (num) {
            case 10:
                st = "A";
                break;
            case 11:
                st = "B";
                break;
            case 12:
                st = "C";
                break;
            case 13:
                st = "D";
                break;
            case 14:
                st = "E";
                break;
            case 15:
                st = "F";
                break;
            case 16:
                st = "G";
                break;
            case 17:
                st = "H";
                break;
            case 18:
                st = "I";
                break;
            case 19:
                st = "J";
                break;
            case 20:
                st = "K";
                break;
            case 21:
                st = "L";
                break;
            case 22:
                st = "M";
                break;
            case 23:
                st = "N";
                break;
            case 24:
                st = "0";
                break;
            case 25:
                st = "P";
                break;
            case 26:
                st = "Q";
                break;
            case 27:
                st = "R";
                break;
            case 28:
                st = "S";
                break;
            case 29:
                st = "T";
                break;
            case 30:
                st = "U";
                break;
            case 31:
                st = "V";
                break;
            case 32:
                st = "X";
                break;
            case 33:
                st = "Y";
                break;
            default:
                if (num > 33)
                    st = "Z";
                break;
        }
        return st;
    }

    private void validarEnvio() {

        if (banderaWsOcupado) return;

        txtInfo.setText("");
        esComprimido = "0";
        NombreZip = "D" + Municipio + Seccion + Division + ".ZIP";
        ArchivoAEnviarRecibir = rutaAdministrador.trim() + "CIC" + Ciclo + "\\" + "D" + Municipio + Seccion + Division + "\\" + "REGISTRO.SDA";//archivo por buscar en el server
        banderaMet2llamo = "validarEnvio";//Bandera puesta, ya que otros metodos invocan este RegistProgramacEnrut

        try {
            TablaRegistroSalida infoRegistroSalida = new TablaRegistroSalida();
            infoRegistroSalida.setArchivo_TablaRegistroSalida(VariablesGlobales.directorioactual + "/DATOSDESALIDA/REGISTRO.SDA");
            infoRegistroSalida.abrir_TablaRegistroSalida(infoRegistroSalida.getArchivo_TablaRegistroSalida());
            tamLongitRegD = infoRegistroSalida.getLongitudRegistro();
            tamFilasArchivoD = infoRegistroSalida.getTotal_TablaRegistroSalida();
            infoRegistroSalida.Cerrar_TablaRegistroSalida();

            metodo = "OBTENER_LONGITUD_ARCHIVO_2";

            AsyncCallWS task = new AsyncCallWS();
            task.execute("");//Ax: continua aqui, en AsyncCallWS.doinback...
            banderaWsOcupado = true;//Bandera conexion ocupada

        } catch (Exception ex) {
            mensajes("OBTENER_LONGITUD_ARCHIVO_2 " + "Error al Obtener Tamano de Archivo");
            banderaWsOcupado = false;//Bandera conexion ocupada
            banderaMet2llamo = "";
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialogEscoger("Alerta de cierre!", "¡¡ SE INTERRUMPIRAN PROCESOS \nDE CARGA o DESCARGA !!", "salir");
    }

    public void WsGetTamanoArchivo(String nombre, String archivoARecibir, String rutaAGrabar) {

        try {
            metodo = "OBTENER_LONGITUD_ARCHIVO";

            AsyncCallWS task = new AsyncCallWS();
            task.execute("");//Ax: continua aqui, en AsyncCallWS.doinback...
            banderaWsOcupado = true;//Bandera conexion ocupada

        } catch (Exception ex) {
            mensajes("Error al Obtener Tamano de Archivo");
            banderaWsOcupado = false;//Bandera conexion ocupada
            banderaMet2llamo = "";
        }
    }

    public void setNombreArgs(String args) {

        try {
            lblTxtArchivo.setText(args);
            Ciclo = txtCiclo.getText().toString();
            Municipio = args.substring(1, 4);
            Seccion = args.substring(4, 7);
            Division = args.substring(7, 8);

            txtCiclo.setText(Ciclo);
            txtMunicipio.setText(Municipio);
            txtSeccion.setText(Seccion);
            txtDivision.setText(Division);

            txtInfo.setText("Ruta cargada ✓");
            mensajes("Ruta cargada estatus:ok");

        } catch (Exception ex) {
            mensajes(ex.getMessage());
        }
        return;
    }

    public void AlertDialogEscoger(final String[] msgs) {

        AlertDialog.Builder builder = new AlertDialog.Builder(Comunicaciones.this);
        builder.setTitle("Escoger ruta");

        builder.setSingleChoiceItems(msgs, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {

                switch (item) {
                    case 0:
                        setNombreArgs(msgs[0]);
                        break;
                    case 1:
                        setNombreArgs(msgs[1]);
                        break;
                    case 2:
                        setNombreArgs(msgs[2]);
                        break;
                    case 3:
                        setNombreArgs(msgs[3]);
                        break;
                    default:
                        setNombreArgs(msgs[item]);
                        //mensajes("Error o Excede items por esocger \n contacte admin.");
                }
                dialog.dismiss();
            }
        });
        levelDialog = builder.create();
        levelDialog.setCancelable(false);
        levelDialog.setCanceledOnTouchOutside(false);
        levelDialog.show();
    }

    public void AlertDialogEscoger(String titulo, final String mensaje, final String method) {
        new AlertDialog.Builder(Comunicaciones.this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (method) {
                            case "RecibirDatos2":
                                RecibirDatos2();
                                break;
                            case "EnviarDatosMensaje2":
                                EnviarDatosMensaje2();
                                break;
                            case "EnviarDatosMensaje3":
                                EnviarDatosMensaje3();
                                break;
                            case "EnviarDatosServer":
                                EnviarDatosServer();
                                break;
                            case "EnviarFotos":
                                EnviarFotos();
                                break;
                            case "salir":
                                salir();
                                break;
                        }
                    }
                })
                .create()
                .show();
    }

    // copia de pericion de archivos original
    private int tamannoArchivoCargado() {

        TablaClienteSalida tablaPredio = new TablaClienteSalida();

        if (tablaPredio.abrir_TablaClienteSalida(VariablesGlobales.directorioactual + "/DATOSDESALIDA/CLIENTE.SDA")) {
            tablaPredio.Cerrar_TablaClienteSalida();
            return tablaPredio.getTotal_TablaClienteSalida();
        } else {
            mensajes("Archivo no Existe " + VariablesGlobales.directorioactual + "/DATOSDESALIDA/CLIENTE.SDA");
            //Toast.makeText(getApplicationContext(), "Archivo no Existe " + VariablesGlobales.directorioactual + "/DatosDeSalida/Cliente.sda", Toast.LENGTH_LONG).show();
        }
        return 0;
    }

    // proceso de cambiar fecha del con el sistema central
    private void modificacionFechaHoraSistema() {

        // DirectorioActual + "\\DatosDeEntrada
        String fecha_tomada = "";
        String nombreArchivo = VariablesGlobales.directorioactual + "/DATOSDEENTRADA/FECHAACONFIGURAR.TXT";
        String textoLeido = "";

        File file;
        try {
            file = new File(nombreArchivo);

            if (file.exists()) {
                file = new File(nombreArchivo);
                FileReader stream1 = new FileReader(nombreArchivo);
                BufferedReader reader1 = new BufferedReader(stream1);

                // FileStream stream1 = new FileStream(NombreArchivo,
                // FileMode.Open, FileAccess.Read);
                // StreamReader reader1 = new StreamReader(stream1);

                fecha_tomada = reader1.readLine();
                reader1.close();
                textoLeido = fecha_tomada.substring(6, 10) + fecha_tomada.substring(3, 8) + fecha_tomada.substring(0, 2) + fecha_tomada.substring(11, 13)
                        + fecha_tomada.substring(14, 16) + fecha_tomada.substring(17, 19);
                if (Long.parseLong(textoLeido) < 20120624000000L) {
                    file.delete();
                    mensajes("Fecha a Registrar CON ERROR");
                    //Toast.makeText(getApplicationContext(), "Fecha a Registrar CON ERROR", Toast.LENGTH_LONG).show();
                    return;
                }
                // no deberia de borrar solo copiarla tambien como fechador
                file.delete();
            } else {

                return;
            }

            nombreArchivo = VariablesGlobales.directorioactual + "/FECHADOR.TXT";
            file = new File(nombreArchivo);
            file.delete();

            RandomAccessFile writer2 = new RandomAccessFile(file, "rw");
            writer2.writeBytes(textoLeido);
            writer2.close();
        } catch (NumberFormatException e) {

            e.printStackTrace();

        } catch (FileNotFoundException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();
        }

        GregorianCalendar t = new GregorianCalendar(Integer.valueOf(fecha_tomada.substring(6, 10)), Integer.valueOf(fecha_tomada.substring(3, 5)),
                Integer.valueOf(fecha_tomada.substring(0, 2)), Integer.valueOf(fecha_tomada.substring(11, 13)),
                Integer.valueOf(fecha_tomada.substring(14, 16)), Integer.valueOf(fecha_tomada.substring(17, 19)));

        // TODO: ClaseEspecial.TIEMPODELSISTEMA st = new
        // ClaseEspecial.TIEMPODELSISTEMA();
        // st.FromDateTime(t);
        // ClaseEspecial.SetLocalTime(ref st);
        // MessageBox.Show("El Sistema Actualizo la Fecha y La Hora");
        file = new File(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/FECHAACONFIGURAR.TXT");
        file.delete();

    }

    /**
     * kim
     * Codigo que se va a ejecutar una vez terminado de bajar los datos.
     */
    private void comenzarActualizar() {
        //kim
        //Para tener el contexto mas a mano.
        context = this;
        //Creamos el Autoupdater.
        updater = new Autoupdater(this);
        //Ponemos a correr el ProgressBar.

        //Crea mensaje con datos de versión.
        String msj = "Nueva Version: ";
        msj += "\nDesea Actualizar?";
        //Crea ventana de alerta.
        androidx.appcompat.app.AlertDialog.Builder dialog1 = new androidx.appcompat.app.AlertDialog.Builder(context);
        dialog1.setMessage(msj);
        //Establece el boton de Aceptar y que hacer si se selecciona.
        dialog1.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                updater.setCurrentVersionCode(currentVersionCode);
                updater.InstallNewVersion(null);
            }
        });

        //Muestra la ventana esperando respuesta.
        dialog1.show();
    }

    public void validarV() {
        try {
            metodo = "ValidarVersionSIMFACT";
            AsyncCallWS task = new AsyncCallWS();
            task.execute("");
        } catch (Exception exc) {

        }
    }

//    public String getSerialNumber() {
//
//        TelephonyManager tManager = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
//        String deviceIMEI = tManager.getDeviceId();
//        return deviceIMEI;
//    }

    public String traerArchivo() {
        String respuestaWeb = "";

        try {
            WSSoap wsoaps = new WSSoap(URL, paginaWs);

            try {
                respuestaWeb = wsoaps.validarVersion("ValidarVersionSIMFACT", rutaAdministrador + "Android\\Simfa" + decformt.format(currentVersionCode + 0.1) + ".apk", serial);

            } catch (Exception ex) {
                return "Sin Conexion o Sin directorio \nde trabajo.:\n" + " " + respuestaWeb;
            }
            Log.e("INFO","ValidarVersionSIMFACT|respuestaWeb: " + respuestaWeb);

            respuestaWeb = wsoaps.ObtenerLongitudArchivo("OBTENER_LONGITUD_ARCHIVO", rutaAdministrador + "Android\\Simfa" + decformt.format(currentVersionCode + 0.1) + ".apk", 0, Ciclo, Municipio + Seccion + Division);

            int res = Integer.parseInt(respuestaWeb);
            Log.e("INFO","OBTENER_LONGITUD_ARCHIVO|respuestaWeb: " + respuestaWeb);

            if (res > 0) {
                 respuestaWeb = wsoaps.ObtenerArchivo("ENVIAR_DEL_SERVIDOR_AL_PDA", rutaAdministrador + "Android\\Simfa" + decformt.format(currentVersionCode + 0.1) + ".apk", ruta, trama, res);

               // respuestaWeb = wsoaps.ObtenerArchivo("obtenerArchivo", rutaAdministrador + "Android\\Simfa" + (currentVersionCode + 1) + ".ZIP", ruta, trama, res);

            }
            Log.e("INFO","ENVIAR_DEL_SERVIDOR_AL_PDA|respuestaWeb: " + respuestaWeb);
            Log.e("INFO","ruta: " + ruta);
            if (respuestaWeb.equals("2")||respuestaWeb.equals("1")) {
                //respuestaWeb = util.DescomprimeZip(ruta, "", res, true);
                respuestaWeb = "Descomprime ✓; borrar Zip ✓";
            }
        } catch (Exception ex) {
            Log.e("INFO","Problemas al traer el apk" + ex.getMessage());
            return "Problemas al traer el apk" + ex.getMessage();
        }
        return respuestaWeb;
    }

    public void Longitud() {

        File file = new File(VariablesGlobales.directorioactual, "/ArchivosDesCarga.cfi");
        if (file.exists()) {
            try {

                FileInputStream fIn = new FileInputStream(file);
                InputStreamReader archivo = new InputStreamReader(fIn);
                BufferedReader br = new BufferedReader(archivo);
                RutaAdministrador = br.readLine();

                RutaAdministrador = RutaAdministrador.trim() + "CIC" + Ciclo.trim().toUpperCase() + "\\D" + Municipio.trim() + Seccion.trim() + Division.trim() + "\\Cliente.sda";
                if (!RutaAdministrador.equals("")) {
                    try {
                        metodo = "OBTENER_LONGITUD";

                        AsyncCallWS task = new AsyncCallWS();
                        task.execute("");//Ax: continua aqui, en AsyncCallWS.doinback...
                        banderaWsOcupado = true;//Bandera conexion ocupada

                    } catch (Exception ex) {
                        mensajes("Error al Obtener Tamano de Archivo");
                        banderaWsOcupado = false;//Bandera conexion ocupada
                    }
                }
            } catch (IOException e) {
                Log.e("error", e.getMessage());
            }
        } else {
            mensajes("no existe el archivo");
        }
    }

    //Ax: permite llamado asincrono para ejecutar conexion al Ws
    private class AsyncCallWS extends AsyncTask<String, Integer, Void> {

        public String respuesta;
        public Context contexto;
        ProgressDialog pDialog;
        int x = 0;

        @Override
        protected Void doInBackground(String... params) {

            WSSoap wsoap = new WSSoap(URL, paginaWs);

            try {

                switch (metodo) {
                    case "EnviarDatosServer":
                        respuesta = EnviarDatosServerAsync();
                        break;

                    case "EnviarFotosServer":
                        respuesta = EnviarFotosServerAsync();
                        break;

                    case "EnviarLblsServer":
                        respuesta = EnviarLblsServerAsync();
                        break;


                    case "HAY_CONEXION":

                        UtilsNet utilnet = new UtilsNet();
                        boolean hayI = utilnet.hayInternet(contexto);

                        if (!hayI) {
                            respuesta = "Hay problemas con la CONEXION";
                        } else {
                            respuesta = "Conexion Ok";
                        }
                        break;

                    case "VALIDAR_CONEXION":
                        respuesta = wsoap.verificarWs(metodo);
                        break;

                    case "RutasProgramadas":
                        respuesta = wsoap.rutasProgramadas(metodo, serialPDA, rutaAdministrador + "CIC" + Ciclo); //Puede devolver 2 ó 3 rutas
                        Log.e("INFO","RutasProgramadas | metodo:" + metodo + " | serialPDA:" + serialPDA + " | rutaAdministrador:" + rutaAdministrador + "CIC" + Ciclo);
                        break;

                    case "OBTENER_LONGITUD_ARCHIVO":
                        respuesta = wsoap.ObtenerLongitudArchivo(metodo, ArchivoAEnviarRecibir, Integer.parseInt(esComprimido), Ciclo, Municipio + Seccion + Division);
                        Log.e("INFO","OBTENER_LONGITUD_ARCHIVO | metodo:" + metodo + " | ArchivoAEnviarRecibir:" + ArchivoAEnviarRecibir + " | esComprimido:" + esComprimido + " | Ciclo:" + Ciclo + " | ruta:" + Municipio + Seccion + Division);
                        break;

                    case "OBTENER_LONGITUD_ARCHIVO_2":
                        respuesta = wsoap.ObtenerLongitudArchivo("OBTENER_LONGITUD_ARCHIVO", ArchivoAEnviarRecibir, Integer.parseInt(esComprimido), Ciclo, Municipio + Seccion + Division);
                        Log.e("INFO","OBTENER_LONGITUD_ARCHIVO_2 | metodo:" + "OBTENER_LONGITUD_ARCHIVO" + " | ArchivoAEnviarRecibir:" + ArchivoAEnviarRecibir + " | esComprimido:" + esComprimido + " | Ciclo:" + Ciclo + " | ruta:" + Municipio + Seccion + Division);
                        break;

                    case "REGISTRAR_PROGRAMACION_ENRUTADOR"://Dirciclo,condicion,tamano,serial,rutarchivo,nombrearchivo

                        //Todo arreglar machete
                        int xx = ArchivoAEnviarRecibir.lastIndexOf("\\");
                        String remotepath = ArchivoAEnviarRecibir.substring(0, xx);
                        String archivrec = ArchivoAEnviarRecibir;
                        if (tipodecarga.equals("cargaf"))
                            archivrec = ArchivoAEnviarRecibir.substring(0, ArchivoAEnviarRecibir.length() - 4) + "\\CLIENTE.TXT";//Axx mejorar, el metodo pide carpeta en las fuentes originales, el zip se borra por eso se pide txt
                        respuesta = wsoap.RegistrarProgramEnrutadosr(metodo, remotepath, tipodecarga, (tamanoArchivo + ""), (serialPDA + ""), archivrec, (Municipio + Seccion + Division),RegistrosLeidos,CausasLeidas);
                        break;

                    case "ENVIAR_DEL_SERVIDOR_AL_PDA":
                        respuesta = wsoap.ObtenerArchivo(metodo, ArchivoAEnviarRecibir, RutaAGrabar, trama, tamanoArchivo);
                        Log.e("INFO","ENVIAR_DEL_SERVIDOR_AL_PDA | metodo:" + metodo + " | ArchivoAEnviarRecibir:" + ArchivoAEnviarRecibir + " | RutaAGrabar:" + RutaAGrabar + " | trama:" + trama + "tamanoArchivo" + tamanoArchivo);
                        break;
                    case "ValidarVersionSIMFACT":
                        respuesta = traerArchivo();
                        break;
                    case "OBTENER_LONGITUD":
                        respuesta = wsoap.ObtenerLongitudArchivo("OBTENER_LONGITUD_ARCHIVO", RutaAdministrador, Integer.parseInt(esComprimido), Ciclo.trim().toUpperCase(), Municipio.trim() + Seccion.trim() + Division.trim());
                        break;
                    case "VALIDAR_VERSION":
                        respuesta = wsoap.validarVersion("ValidarVersionSIMFACT", rutaAdministrador + "Android\\Simfa" + decformt.format(currentVersionCode + 0.1) + ".zip", serial);
                        break;

                }

            } catch (Exception e) {
                respuesta = "Error, valide conexion con Ws";
                banderaMet2llamo = "";
                pruebawsok = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            pDialog.dismiss();
            banderaWsOcupado = false;
            String msg = "";
            boolean bul = true;

            switch (metodo) {
                case "VALIDAR_CONEXION":
                    metodo = "";
                    try {
                        Integer.parseInt(respuesta);//genera excepcion si diferente de 1
                        iconWsOff.setVisibility(View.INVISIBLE);
                        iconWsOn.setVisibility(View.VISIBLE);
                        msg = "Conexion con Webservice ✓";
                        //  ValidarVersion();

                    } catch (NumberFormatException e) {
                        iconWsOff.setVisibility(View.VISIBLE);
                        iconWsOn.setVisibility(View.INVISIBLE);
                        bul = false; //Ws no respopndio
                        msg = "Error de conexion con Webservice";
                    }
                    break;

                case "RutasProgramadas":
                    Log.e("error","respuesta "+respuesta);
                    metodo = "";
                    if (respuesta.contains(";")) {

                        respuesta = respuesta.replace(" ", "").trim();
                        String[] resp = respuesta.split(";");

                        if (resp.length < 2) setNombreArgs(respuesta);
                        else AlertDialogEscoger(resp); //Ax:mostrar opcion

                        txtCiclo.setEnabled(false);
                        txtMunicipio.setEnabled(false);
                        txtSeccion.setEnabled(false);
                        txtDivision.setEnabled(false);

                    } else {
                        msg = "Server: " + respuesta;
                    }
                    break;

                case "OBTENER_LONGITUD_ARCHIVO":
                    metodo = "";
                    try {
                        int x = Integer.parseInt(respuesta);
                        tamanoArchivo = x;
                        if (x > 0) { //Ax: Puede responder -1,-2-0
                            switch (banderaMet2llamo) {
                                case "RecibirDatos3":
                                    tipodecarga = "cargai";
                                    //Ax: aqui corre otra tarea paralela
                                    RegistProgramacEnrut(); //Ax: primero va a REGISTRAR_PROGRAMACION_ENRUTADOR  //Aqui colocar lo de fotos?  !chEnviaFotos.Checked

                                    break;
                            }
                        } else {
                            msg = "Error al obtener Longitud de archivo.";
                            tamanoArchivo = 0;
                        }

                    } catch (NumberFormatException ex) {
                        msg = respuesta;
                        tamanoArchivo = 0;
                    }
                    break;

                case "REGISTRAR_PROGRAMACION_ENRUTADOR": //Este metodo crea un registro de inicio y fin de transacciones como auditoria

                    if (respuesta.equals("1")) {

                        switch (banderaMet2llamo) {
                            case "RecibirDatos3":
                                msg += txtInfo.getText() + "Reg. Prog. Enrutador ✓ " + tipodecarga + "; ";
                                RecibirDatos3(respuesta); //Ax: luego va a ENVIAR_DEL_SERVIDOR_AL_PDA //Otra tarea paralela
                                break;//
                            default:
                                break;
                        }

                    } else {
                        msg += respuesta + "; ";
                        banderaMet2llamo = "";
                    }
                    break;

                case "ENVIAR_DEL_SERVIDOR_AL_PDA":

                    if (respuesta.length() == 1) {//"1" bajados sin borrado "2" bajado y borrado
                        msg = "Datos cargados ✓;   " + "; ";

                        String zipMsg = util.DescomprimeZip(RutaAGrabar, "", tamanoArchivo, true); //Ax: descomprimir archivos

                        msg += zipMsg + "; ";

                        if (zipMsg.contains("Descomprime ✓")) {

                            //Axx: aqui iba el cambio de hora y fecha, se salta este paso (pendiente todo)

                            String movarch = util.MoverArchivosPorTipo(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/", VariablesGlobales.directorioactual + "/DATOSDESALIDA/", ".SDA");//Ax: Mover Archivos Por extension

                            msg += movarch + "; ";

                            if (movarch.equals("Mover ✓")) {

                                crearArchivoNombre();
                                tamanoArchivo = 0;
                                tipodecarga = "cargaf";
                                banderaMet2llamo = "";//Ax evito que vuelva a bajar datos

                                RegistProgramacEnrut(); //Ax: aqui corre otra tarea paralela
                            }
                        }

                    } else {
                        msg = "Error: " + respuesta;
                    }
                    //metodo = "";
                    break;

                case "EnviarDatosServer": //Ax: no funciona?
                    msg = respuesta;
                    if (respuesta.contains("✓")) {
                        iconWsOff.setVisibility(View.INVISIBLE);
                        iconWsOn.setVisibility(View.VISIBLE);
                    }
                    break;

                case "EnviarFotosServer": //Ax: no funciona?
                    msg = respuesta;
                    if (respuesta.contains("✓")) {
                        iconWsOff.setVisibility(View.INVISIBLE);
                        iconWsOn.setVisibility(View.VISIBLE);
                    }
                    int canttotal = cantfotos - cantNofotos;
                    if(canttotal > 0){
                        Log.e("INFO","Cant de fotos por env" + canttotal);
                        EnviarFotos();
                    }else{
                        EnviarLbls();
                    }
//                    if(Integer.parseInt(Ciclo.substring(1,3)) == 0 || Integer.parseInt(Ciclo.substring(1,3)) == 24 || Integer.parseInt(Ciclo.substring(1,3)) == 1 || Integer.parseInt(Ciclo.substring(1,3)) == 25
//                       || Integer.parseInt(Ciclo.substring(1,3)) == 6 || Integer.parseInt(Ciclo.substring(1,3)) == 23 || Integer.parseInt(Ciclo.substring(1,3)) == 10 || Integer.parseInt(Ciclo.substring(1,3)) == 11
//                       || Integer.parseInt(Ciclo.substring(1,3)) == 30 || Integer.parseInt(Ciclo.substring(1,3)) == 31 || Integer.parseInt(Ciclo.substring(1,3)) == 32 || Integer.parseInt(Ciclo.substring(1,3)) == 57){
//
//                    }
                    break;

                case "HAY_CONEXION":
                    msg = respuesta;

                    if (msg.toLowerCase().contains("ok")) {
                        ProbarWS();
                    }
                    break;

                case "ValidarVersionSIMFACT":
                    // msg = respuesta;
                    if (respuesta.contains("✓")) {
                        if (validaEnvio) {
                            validaEnvio = false;
                            mensajes("Primero Actualize la version ");

                        } else {
                            comenzarActualizar();
                        }
                    } else {
                        if (validaEnvio) {
                            validaEnvio = false;
                            Longitud();
                        } else {
                            mensajes("No hay actualizaciones ");
                        }
                    }
                    break;
                case "OBTENER_LONGITUD":
                    int res = Integer.parseInt(respuesta);
                    if (res > 0) {
                        mensajes("Archivos Registros.sda Encontrado con " + String.valueOf(res) + " bites");
                    } else {
                        mensajes("El archivo no existe");
                    }

                    break;

                case "OBTENER_LONGITUD_ARCHIVO_2": //Ax nuevo, anterior perdido

                    try {
                        long resp = Integer.parseInt(respuesta);

                        if (resp > 0) {

                            long tam = (int) (resp / tamLongitRegD);

                            if (tamFilasArchivoD == tam) {
                                mensajes("El envio Correcto! \n Archivo de " + tamFilasArchivoD + " Filas");
                            } else {
                                mensajes("El envio  No coincide, bites erroneos\n" + String.valueOf(resp));
                            }

                        } else {
                            mensajes("Envio no coincide, vuelva a enviar");
                        }
                    } catch (Exception ex) {
                        mensajes("Error no se pudo comprobar Envio");
                    }

                    break;

                case "VALIDAR_VERSION":
                    Log.e("error","valida version "+respuesta);
                    if(respuesta.equals("99")){
                        Toast.makeText(getApplicationContext(),"Ahi una nueva version Actualice!!! ",Toast.LENGTH_LONG).show();
                    }
                    else{
                        TraerRuta();
                    }
                break;

                default:
                    msg = respuesta;

                    if (respuesta.contains("✓")) {
                        iconWsOff.setVisibility(View.INVISIBLE);
                        iconWsOn.setVisibility(View.VISIBLE);
                    }
                    break;
            }

            if (!msg.isEmpty()) {
                txtInfo.setText(msg);
                mensajes(msg);
            }
            //banderaMet2llamo = "";
            respuesta = "";
            pruebawsok = bul; //Ws respopndio
        }

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(Comunicaciones.this);
            pDialog.setMessage("cargando...");
            pDialog.setCancelable(false);
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
        }
    }


    public void leeTomoLectura(){
        try {
            String nombreArchivo = VariablesGlobales.directorioactual + "/DATOSDESALIDA/TOMOLECTURA.TXT";
            String[] dato = null;
            File file = new File(nombreArchivo);

            if (file.exists()) {

                FileReader stream3 = new FileReader(nombreArchivo);
                BufferedReader reader = new BufferedReader(stream3);
                String linea = "";
                int cont = 0;

                while ((linea = reader.readLine()) != null) {

                    linea = linea.replace("|",";");
                    dato = linea.split(";");
                    int contadorderutas = dato.length;
                    for (int i = 0; i < contadorderutas; i++)
                    {

                        if (i == 0) {
                            RegistrosLeidos = Integer.parseInt(String.format("%1$10s", dato[i].trim()).replace(" ", "0"));
                        }else{
                            if (i == 1) {
                                CausasLeidas = Integer.parseInt(String.format("%1$10s", dato[i].trim()).replace(" ", "0"));
                            }else {
                                i = contadorderutas + 2;
                            }
                        }
                    }

                }

                reader.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
            mensajes("Error, verificar Archivo de Configuracion IP");
        }
    }

    public void ValidarVersion() {
        try {
            metodo = "VALIDAR_VERSION";
            AsyncCallWS task = new AsyncCallWS();
            task.execute("");
        } catch (Exception e) {
        }
    }


    public void EnviarLbls() {  //**********************************************************************
        Log.e("error3","intenta enviar ");

        try {
            String respuesta;
            int cantLbls = 50;
            int contadorZiplbls = 1;
            NombreZip = "LBLS" + Municipio + Seccion + Division;//No tiene el .ZIP

            zips = new ArrayList<String>();
            List<String> lbls = new ArrayList<String>();
            //File f = new File(RutaAGrabar);
            int desde = 51;
            int hasta = 52;

            FileReader r = new FileReader(VariablesGlobales.directorioactual + "/LBLS/" + "L" + Ciclo + Municipio + Seccion + Division); //Ax: se recorre el archivo para capturar las no eviadas o en envio
            BufferedReader reader = new BufferedReader(r);
            String linea;
            int conteo = 0;
            int conteoPos = 0;
            posicLblCont = new int[51];

            while ((linea = reader.readLine()) != null) {
                conteo++;
                if (linea.substring(desde, hasta).equals("X") || linea.substring(desde, hasta).equals("_")) {

                    File lblP = new File(VariablesGlobales.directorioactual + "/LBLS/" + linea.substring(0, 22).trim());

                    if (lblP.exists()) {
                        conteoPos++;
                        if (conteoPos == 50) {
                            break;
                        }

                        lbls.add(VariablesGlobales.directorioactual + "/LBLS/" + linea.substring(0, 22).trim());
                        posicLblCont[conteoPos] = conteo;
                    }
                }
            }
            r.close();

            if (lbls.size() > 0) {

                int i = 1;
                ArrayList filestoZip = new ArrayList(); //Contiene los archivos por comprimir

                for (String x : lbls) { //Recorrer cada jpg encontrado para añadirlo

                    filestoZip.add(new File(x));

                    if (filestoZip.size() == cantLbls || i == lbls.size()) {//Si el arreglo llega a el limite de fotos por comprimir en una tanda

                        String nombrearchivozip;

                        if (contadorZiplbls <= 9) {
                            nombrearchivozip = RutaAGrabar + NombreZip + contadorZiplbls + ".ZIP"; //Ej: /sdcard01/Fotogra.../FOTOS....

                        } else { //Ax: si el munero es mas grande que 10 se cambia el ultimo numero del nombre por una letra
                            nombrearchivozip = sufijo(contadorZiplbls);
                            nombrearchivozip = RutaAGrabar + NombreZip + nombrearchivozip + ".ZIP";
                        }
                        contadorZiplbls++;

                        //zips.add(nombrearchivozip);//Guarda nombre de los zips, esto sera usado en el envio asincrono //Ax: error no deberia ir despues de crear?

                        respuesta = util.CreaZip(nombrearchivozip, filestoZip);
                        Log.e("error3","comprime no "+respuesta);
                        if (!respuesta.contains("✓")) {
                            mensajes("Error Comprimiendo \n Archivos por enviar");
                            txtInfo.setText("Error Comprimiendo \n Archivos por enviar");
                            return;
                        } else{
                            zips.add(nombrearchivozip);//Guarda nombre de los zips, esto sera usado en el envio asincrono
                        }
                        filestoZip = new ArrayList();
                    }
                    i++;
                }//end for

                txtInfo.setText("");
                // ArchivoAEnviarRecibir = rutaAdministrador.trim() + "CIC" + CicloReal + "\\";

                try {
                    metodo = "EnviarLblsServer";
                    AsyncCallWS task = new AsyncCallWS();
                    task.execute("");
                    banderaWsOcupado = true;

                } catch (Exception ex) {
                    mensajes(ex.getMessage());
                    banderaWsOcupado = false;//Bandera conexion ocupada
                    banderaMet2llamo = "";
                    util.Log(logfile, "[Comunicaciones] EnviarLblsServer(): " + ex.getMessage());
                }

            } else {
                mensajes("No hay Lbl(s) para enviar");
                txtInfo.setText("No hay Lbl(s) para enviar");
            }
        } catch (Exception ex) {
            mensajes("Error en el envio de lbls \n " + ex.getMessage());
            util.Log(logfile, "[Comunicaciones] EnviarLblsServer().: " + ex.getMessage());
        }
    }

    public String EnviarLblsServerAsync() {//888

        try {
            WSSoap wsoap = new WSSoap(URL, paginaWs);

            ArchivoAEnviarRecibir = rutaAdministrador.trim() + "CIC" + Ciclo + "\\C" + Municipio + Seccion + Division + "\\" + "Backuplbl" + "\\"; //D:ENRUTADOR_X

            for (String zip : zips) { //Recorrer cada .zip creado para tratar de enviarlo

                String hash = util.Md5Hash(zip);

                if (hash.trim().equals("")) return "Error de hash";

                File file = new File(zip);
                tamanoArchivo = (int) file.length();
                Log.e("error", file.getName()+" directorio lbls "+ArchivoAEnviarRecibir + "\\LBLS");

                metodo = "VerificarSiexiste_Directorio_Archivo";
                String respuesta = wsoap.VerificarSiexisteDirectorioArchivo(metodo, ArchivoAEnviarRecibir, file.getName());//Ax: Se comprueba si existe la ruta destino remota

                if (!respuesta.equals("true")) {
                    return "Error No se ha encontrado destino en el servidor!";
                }

                metodo = "Terminal_ToServerReceive";
                respuesta = wsoap.TerminalToServerReceive(metodo, ArchivoAEnviarRecibir, Integer.parseInt(esComprimido), zip, trama);//Enviar Datos al servidor

                if (!(respuesta.equals("4") || respuesta.equals("1"))) {
                    return "Error Enviar Archivo al servidor";
                }

                metodo = "Descomprime";
                respuesta = wsoap.Descomprimir(metodo, ArchivoAEnviarRecibir , ArchivoAEnviarRecibir + file.getName(), hash, serialPDA);

                if (!respuesta.equals("true")) {
                    return " Error descomprimir remoto,\n Intentelo de nuevo \n" + respuesta;
                }
                actualizaLbls();
                file.delete();
            }
        } catch (Exception ex) {
            txtInfo.setText("Error al enviar lbls al servidor, intente de nuevo");
            util.Log(logfile, "[Comunicaciones]EnviarLblsServer(): " + ex.getMessage());
        }
        metodo = "EnviarLblsServer";
        return "Proceso subir Lbls Exitoso! ✓";
    }

    public void actualizaLbls() { //Recorre el archivo de fotos para actualizar los que si se fueron "Y"
        try {
            int desde = 51;
            int hasta = 52;
            String path = VariablesGlobales.directorioactual + "/LBLS/L" + Ciclo + Municipio + Seccion + Division;
            FileReader r = new FileReader(path); //Ax: se recorre el archivo para actualizar las eviadas
            File fileName = new File(path + "_temp");
            BufferedReader reader = new BufferedReader(r);
            String linea;
            int conteo = 0;

            while ((linea = reader.readLine()) != null) {
                conteo++;

                for (int i = 0; i < posicLblCont.length; i++) {
                    if (conteo == posicLblCont[i]) {
                        linea = linea.substring(0, desde) + "Y" + linea.substring(hasta);
                        continue;
                    }
                }
                util.EscribirLinea(fileName, linea + "\r\n");
            }
            r.close();

            File fpath = new File(path);
            try {
                if (reader != null) reader.close();

                fpath.delete();
            } catch (Exception ex) {
                util.Log(logfile, "[Comunicaciones] actualizaLbls()..: " + ex.getMessage());
            }
            fileName.renameTo(fpath);

        } catch (Exception ex) {
            util.Log(logfile, "[Comunicaciones] actualizaLbls().: " + ex.getMessage());
        }
    }


}