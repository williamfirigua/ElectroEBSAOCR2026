package com.gselectroCaqueta.accesoyseguridad;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.gselectroCaqueta.comunicaciones.Comunicaciones;
import com.gselectroCaqueta.moduloLicencia.ActLicencia;
import com.gselectroCaqueta.modulobluetooth.DeviceListActivity;
import com.gselectroCaqueta.modulobluetooth.btPrintFile;
import com.gselectroCaqueta.modulobluetooth.msgTypes;
import com.gselectroCaqueta.moduloreimpresion.ModuloReimpresion;
import com.gselectroCaqueta.tablas.TablaAforadores;
import com.gselectroCaqueta.tablas.TablaEncabezado;
import com.gsutil.Utils;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class GuiAcceso extends AppCompatActivity {

    private static final String TAG = "GuiAcceso - btprint";
    public static Boolean impresora1vez = false; //Ax: tal vez no es necesario
    public static String macAdress = ""; //Ax: guarda la mac actual
    //final File sdCard = Environment.getExternalStorageDirectory(); Se comenta por version en 11
    // The Handler that gets information back from the VariablesGlobales.btPrintService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case msgTypes.MESSAGE_STATE_CHANGE:
                    Bundle bundle = msg.getData();
                    int status = bundle.getInt("state");

                    switch (msg.arg1) {
                        case btPrintFile.STATE_CONNECTED:
                            //addLog("connected to: " + VariablesGlobales.mConnectedDeviceName);
                            //if( VariablesGlobales.mConversationArrayAdapter==null)break;//Ax: aqui se revienta TODO: revisar
                            VariablesGlobales.mConversationArrayAdapter.clear();
                            Log.i(TAG, "handleMessage: STATE_CONNECTED: " + VariablesGlobales.mConnectedDeviceName);
                            break;
                        case btPrintFile.STATE_CONNECTING:
                            addLog("connecting...");
                            Log.i(TAG, "handleMessage: STATE_CONNECTING: " + VariablesGlobales.mConnectedDeviceName);
                            break;
                        case btPrintFile.STATE_LISTEN:
                            addLog("connection ready");
                            Log.i(TAG, "handleMessage: STATE_LISTEN");
                            break;
                        case btPrintFile.STATE_IDLE:
                            addLog("STATE_NONE");
                            Log.i(TAG, "handleMessage: STATE_NONE: not connected");
                            break;
                        case btPrintFile.STATE_DISCONNECTED:
                            addLog("disconnected");
                            Log.i(TAG, "handleMessage: STATE_DISCONNECTED");
                            break;
                    }
                    break;
                case msgTypes.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string
                    // from the buffer
                    String writeMessage = new String(writeBuf);
                    VariablesGlobales.mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case msgTypes.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string
                    // from the valid bytes
                    // in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    VariablesGlobales.mConversationArrayAdapter.add(VariablesGlobales.mConnectedDeviceName + ":  "
                            + readMessage);
                    addLog("recv>>>" + readMessage);
                    break;
                case msgTypes.MESSAGE_DEVICE_NAME:
                    // save the connected
                    // device's name
                    VariablesGlobales.mConnectedDeviceName = msg.getData().getString(msgTypes.DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to " + VariablesGlobales.mConnectedDeviceName,
                            Toast.LENGTH_SHORT).show();
                    break;
                case msgTypes.MESSAGE_TOAST:

                    Toast.makeText(getApplicationContext(), msg.getData().getString(msgTypes.TOAST), Toast.LENGTH_LONG)
                            .show();
                    // myToast(msg.getData().getString(msgTypes.TOAST));
                    Log.i(TAG, "handleMessage: TOAST: " + msg.getData().getString(msgTypes.TOAST));
                    addLog(msg.getData().getString(msgTypes.TOAST));
                    break;
                case msgTypes.MESSAGE_INFO:
                    addLog(msg.getData().getString(msgTypes.INFO));
                    // mLog.append(msg.getData().getString(msgTypes.INFO));
                    // mLog.refreshDrawableState();
                    String s = msg.getData().getString(msgTypes.INFO);
                    if (s.length() == 0)
                        s = String.format("int: %i" + msg.getData().getInt(msgTypes.INFO));
                    Log.i(TAG, "handleMessage: INFO: " + s);
                    break;
            }
        }
    };
    public Utils utils = new Utils();
    public int contt = 0;
    TextView txtRemoteDevice;
    Button btnIngresar;
    ImageButton btnSalir;
    ImageButton btnComunicar;
    ImageButton btnAcercaDe;
    ImageButton btnImprime;
    ImageButton btnBluetooth;
    ImageButton btnBateria;
    ImageButton btnInfo;

    Boolean conn = true; //Ax: guarda estado si la impesora para permitir uso de botones
    Boolean sdDisponible;
    Boolean sdAccesoEscritura;
    public static boolean esMayor9 = false; //Indica si la version es 10 u otra

    TextView txtCodInterno;
    String codigoSalida = "432";
    String claveSalida = "56789";
    String nivelOperador = "";
    String NombreOperador = "";
    String archivocargado = "";
    String telephoneSerialNumber = "";
    String mensajeDeAlerta = "";

    TextView txtClave;
    TextView txtCodigo;
    TextView txtNombreUsuario;
    TextView txtFecha;
    TextView txtCondicion;
    private double currentVersionCode;
    int aforadorClaveCorrecto = 0;

    TablaAforadores tablaAforadores = new TablaAforadores();
    TablaEncabezado tablaEncabezado = new TablaEncabezado();
    VariablesGlobales variables = new VariablesGlobales();
    ImageView fotoUsuario;
    File logfile; //Ax: Es para los log de error
    File logPrint; //Ax: Es para guardar la de mac de impresora bluetooth
    LocationManager mlocManager;
    MyLocationListener mlocListener;
    //*** Agregado para 11 ***
    Context ctx;
    String pathsd = "";
    Thread myThread;

    String msg_val = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gui_acceso);
        ctx = this;


        Runnable myRunnableThread = new Reloj();
        myThread = new Thread(myRunnableThread);
        myThread.start();

        String externalpath = ctx.getExternalFilesDir(null).getParent();
        String hardcoding = "/Android/data/";//Ax: todo: cambiar este hardcoding

        if (externalpath.contains(hardcoding)) {
            externalpath = externalpath.substring(0, externalpath.indexOf(hardcoding));
        }


        VariablesGlobales.directorioactual = externalpath;

        ObtenerDirectorioBackup();

        verificarArchivosAmayuscula();

        logfile = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/LOGEVENTOS.LOG");
        logPrint = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/PRINTER.LOG");

        btnIngresar = (Button) findViewById(R.id.btnIngresar);
        btnSalir = (ImageButton) findViewById(R.id.btnSalir);
        btnComunicar = (ImageButton) findViewById(R.id.btnComunicar2);
        btnAcercaDe = (ImageButton) findViewById(R.id.btnAcercaDe);
        btnImprime = (ImageButton) findViewById(R.id.btnImprime);
        btnBluetooth = (ImageButton) findViewById(R.id.btnBluetooth);
        txtRemoteDevice = (TextView) findViewById(R.id.txtRemoteDevice);
        btnBateria = (ImageButton) findViewById(R.id.btnBateria);
        btnInfo = (ImageButton) findViewById(R.id.btnInfo);

        btnSalir.setEnabled(false);
        btnIngresar.setEnabled(false);
        btnIngresar.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
        txtCodInterno = (TextView) findViewById(R.id.txtCodInterno);
        txtCodigo = (TextView) findViewById(R.id.txtUsername);
        txtClave = (TextView) findViewById(R.id.txtPassword);
        txtNombreUsuario = (TextView) findViewById(R.id.txtNombreUsuario);
        txtCondicion= (TextView) findViewById(R.id.txtCondicion);
        txtCodigo.setText("");
        txtClave.setText("");
        txtClave.setEnabled(false);

       // telephoneSerialNumber = getSerialNumber();// txtCodInterno.setText(getSerialNumber());
        esMayor9 = true;

        fotoUsuario = (ImageView) findViewById(R.id.fotoUsuario);

        Bundle bundle = getIntent().getExtras();

        if (getIntent().getStringExtra("valida") != null) { // kim validacion de licencia

            telephoneSerialNumber = bundle.getString("PhoneImei");

        } else {
            Bundle bundlex = new Bundle();
            bundlex.putString("telephoneSerialNumber", telephoneSerialNumber);
            bundlex.putBoolean("esMayor9", esMayor9);
            Intent miIntent = new Intent();
            miIntent.setClass(this, ActLicencia.class);
            miIntent.putExtras(bundlex);
            startActivity(miIntent);
            finish();
        }
        txtCodInterno.setText(telephoneSerialNumber);

        try {
            PackageInfo pckginfo = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
            currentVersionCode = Double.parseDouble(pckginfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        cargarImagenUsuario("aforador");//Si en la carpeta aforador existe esta plantilla, la carga

        txtFecha =  (TextView)findViewById(R.id.txtFecha);

        testSD();
        File dir;
        variables.obtenerRutaMemoriaExterna(externalpath);
        if (new File(VariablesGlobales.removableStoragePath).isDirectory()) {
            Log.e("error3", "*********si lo es");
        } else {
            Log.e("error3", "*********no lo es");
        }

//        **************** Leer path para sd *******************************
        File dirs[] = getExternalCacheDirs(); //Ma: Se obtiene el cache los directorios externos
        if (dirs.length > 1){
            pathsd = dirs[1].getAbsolutePath(); //Ma: Se selecciona el segundo, por default el sistema siempre pone en primero el almacenamineto interno del dispositivo
            String hardcoding2 = "/Android/data/";//M: Se selecciona hasta aqui ya que el path Arroja /storage/9016-4EF8/Android/data/com.my.application/files/miArchivo. y se necesita es el exterior /storage/9016-4EF8
            if (pathsd.contains(hardcoding2)) {
                pathsd = pathsd.substring(0, pathsd.indexOf(hardcoding2));//Ma: Se substrae de toda la cadena  hasta el exterior /storage/9016-4EF8
            }
            File file = new File(pathsd, "/Ejemplo/Config.txt"); //Ma: Se procede a crear el archivo file para ser leido o guardado
        }else {
            Log.e("","No se encuentra la memeoria SD");
        }

//        **************** Fin Leer path para sd *******************************
        //----------------------------------------------------------------------------------------------
        mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mlocListener = new MyLocationListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, mlocListener);
        //---------------------------------------------------------------------------------------------


        if (sdDisponible && sdAccesoEscritura) {

            dir = new File(VariablesGlobales.getDirectorioactual() + "/DCIM/FOTOGRAFIASL");
            dir.mkdirs();

            dir = new File(VariablesGlobales.getDirectorioactual() + "/AFORADORES");
            dir.mkdirs();

            dir = new File(VariablesGlobales.getDirectorioactual() + "/DATOSDESALIDA");
            dir.mkdirs();

            dir = new File(VariablesGlobales.getDirectorioactual() + "/DATOSDEENTRADA");
            dir.mkdirs();

            dir = new File(VariablesGlobales.getDirectorioactual() + "/LBLS");
            dir.mkdirs();


            File file = new File(VariablesGlobales.getDirectorioactual() + "/ARCHIVOSCARGA.CFI");

            try {
                if (!file.exists()) {
                    file.createNewFile();

                    String ruta = "D:\\ENRUTADOR_EBSA\\ \r\n" +
                            "ZIP SI                                 \r\n" +
                            "   Cic@@@@@@\\C!!!!!!!\\CLIENTE.TXT \r\n" +
                            "   Cic@@@@@@\\C!!!!!!!\\GENERAL.TXT \r\n" +
                            "   Cic@@@@@@\\C!!!!!!!\\MEDIDOR.TXT \r\n" +
                            "   Cic@@@@@@\\C!!!!!!!\\REGISTRO.TXT \r\n" +
                            "   Cic@@@@@@\\C!!!!!!!\\IDCLIENTES.TXT \r\n" +
                            "   Cic@@@@@@\\C!!!!!!!\\CLIENTE.SDA  \r\n" +
                            "   Cic@@@@@@\\C!!!!!!!\\CO_COBRO.SDA   \r\n" +
                            "   Cic@@@@@@\\C!!!!!!!\\MEDIDOR.SDA \r\n" +
                            "   Cic@@@@@@\\C!!!!!!!\\REGISTRO.SDA \r\n" +
                            "\\Cic@@@@@@\\DatosSoporte\\ACTIVID.TXT \r\n" +
                            "\\Cic@@@@@@\\DatosSoporte\\CAUSAS_NL.TXT \r\n" +
                            "\\Cic@@@@@@\\DatosSoporte\\CLASE_SE.TXT \r\n" +
                            "\\Cic@@@@@@\\DatosSoporte\\COMENTAR.TXT \r\n" +
                            "\\Cic@@@@@@\\DatosSoporte\\CONVENIO.TXT \r\n" +
                            "\\Cic@@@@@@\\DatosSoporte\\DES_CONC.TXT \r\n" +
                            "\\Cic@@@@@@\\DatosSoporte\\DES_TARI.TXT \r\n" +
                            "\\Cic@@@@@@\\DatosSoporte\\EST_CLIE.TXT \r\n" +
                            "\\Cic@@@@@@\\DatosSoporte\\LECTOR.TXT  \r\n" +
                            "\\Cic@@@@@@\\DatosSoporte\\MENSAJES.TXT \r\n" +
                            "\\Cic@@@@@@\\DatosSoporte\\MUNICIP.TXT \r\n" +
                            "\\Cic@@@@@@\\DatosSoporte\\RANGOS.TXT \r\n" +
                            "\\Cic@@@@@@\\DatosSoporte\\TARIFAS.TXT \r\n" +
                            "\\Cic@@@@@@\\DatosSoporte\\MARCAS.TXT \r\n" +
                            "\\Cic@@@@@@\\DatosSoporte\\AFOROS.TXT \r\n" +
                            "\\Cic@@@@@@\\DatosSoporte\\IDNOVEDADES.TXT \r\n" +
                            "\\Cic@@@@@@\\DatosSoporte\\FORMATO_CORTA.CPCL \r\n" +
                            "\\Cic@@@@@@\\DatosSoporte\\FORMATO_LARGA.CPCL";

                    utils.WriteLine(file, ruta);
                }
            } catch (Exception ex) {
            }
        }

        btnIngresar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (null != txtCodigo.getText() && !txtCodigo.getText().toString().trim().equals("") && (Long.parseLong(codigoSalida.trim()) == Long.parseLong(txtCodigo.getText().toString().trim())) && (null != txtClave.getText() && !txtClave.getText().toString().trim().equals("") && Long.parseLong(claveSalida.trim()) == Long.parseLong(txtClave.getText().toString().trim())) || nivelOperador.equals("S") || nivelOperador.equals("A") || aforadorClaveCorrecto == 1) {

                    ingresoPrincipal();
                    txtCodigo.setText("");
                    txtClave.setText("");
                    btnSalir.setEnabled(false);
                    btnIngresar.setEnabled(false);
                    btnIngresar.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                    Log.e("error", "boton 1");

                } else {
                    mensajes("Acceso denegado.", 1000);
                    txtNombreUsuario.setText("No existe el Usuario");
                    System.out.println("Acceso Denegado.");
                }
            }
        });

        txtCodigo.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {//7900

                if (actionId == 5 || actionId == 6) { //555
                    aforadorClaveCorrecto = 0;

                    String codigo = txtCodigo.getText().toString().trim();

                    if (codigo.length() > 0) {

                        if (Long.parseLong(codigo) == Long.parseLong(codigoSalida)) {//se sale
                            // HardCoded by test!!!!

                            // txtClave.setText("");//12282702

                            //nuevo__________________________________________________________________________________
                            tablaEncabezado.setArchivo_TablaEncabezado(VariablesGlobales.getDirectorioactual() + "/DATOSDEENTRADA/GENERAL.TXT");
                            File general = new File(tablaEncabezado.getArchivo_TablaEncabezado());
                            if (general.exists()) {
                                if (tablaEncabezado.abrir_TablaEncabezado(tablaEncabezado.getArchivo_TablaEncabezado())) {

                                    tablaEncabezado.lectura_TablaEncabezado(1);

                                    //VariablesGlobales.setAdministrador(tablaEncabezado.getTablaEncabezado_administrador());  //[enerca] se dejan los valores predeterminados de la clase
                                    // VariablesGlobales.setTotalimpresiones(tablaEncabezado.);
                                    VariablesGlobales.setConsumoauditoria(tablaEncabezado.getTablaEncabezado_tiempoauditoria());
                                    VariablesGlobales.setDistanciagps(tablaEncabezado.getTablaEncabezado_distanciagps());
                                    VariablesGlobales.setNrodias(tablaEncabezado.getTablaEncabezado_nrodias());
                                    VariablesGlobales.setFechainicial(tablaEncabezado.getTablaEncabezado_fechainicial());
                                    VariablesGlobales.setFechafinal(tablaEncabezado.getTablaEncabezado_fechafinal());
                                    VariablesGlobales.setObligafotos(tablaEncabezado.getTablaEncabezado_obligafotos());
                                    Log.e("error", "obliga foto " + "yyy " + tablaEncabezado.getTablaEncabezado_obligafotos());
                                    VariablesGlobales.setObligabarras("0");//tablaEncabezado.getTablaEncabezado_obligabarras()
                                    Log.e("error", "obliga barra " + "xxx " + tablaEncabezado.getTablaEncabezado_obligabarras());
                                    VariablesGlobales.setMaximoregaenviar(tablaEncabezado.getTablaEncabezado_maximoregaenviar());
                                    VariablesGlobales.setTipoDeRuta(tablaEncabezado.getTablaEncabezado_tiporuta());
                                    VariablesGlobales.EvaluarCritica40 = Integer.parseInt(tablaEncabezado.getTablaEncabezado_obligabarras().trim());
                                    Log.e("error2", "++++obliga obliga40 " + "zzz " + tablaEncabezado.getTablaEncabezado_obligabarras());

                                    // VariablesGlobales.diasvence = Integer.parseInt(tablaEncabezado.getTablaEncabezado_diasdecorte());
                                    // VariablesGlobales.diascorte = Integer.parseInt(tablaEncabezado.getTablaEncabezado_diasdesuspencion());

                                }
                                //VariablesGlobales.EvaluarCritica40 = 1;

                                // txtClave.requestFocus();
                            }
                            VariablesGlobales.diasvence = 7;
                            VariablesGlobales.diascorte = 8;
                            txtClave.setEnabled(true);
                            txtClave.requestFocus();

                        } else {
                            validarCodigoUsuario();
                            txtClave.requestFocus();
                        }
                    }
                }
                return false;
            }
        });

        txtClave.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int keyCode, KeyEvent event) {

                String codigo = txtCodigo.getText().toString().trim().length() > 0 ? txtCodigo.getText().toString().trim() : "0";
                String clave = txtClave.getText().toString().trim().length() > 0 ? txtClave.getText().toString().trim() : "0";

                if (codigo.length() > 0 && clave.length() > 0) {

                    if (Long.parseLong(clave) > 0) {

                        String claveFormateada = String.format("%11s", txtClave.getText().toString().trim());
                        txtClave.setText(claveFormateada);

                        if ((Long.parseLong(codigoSalida.trim()) == Long.parseLong(codigo)) && (Long.parseLong(claveSalida.trim()) == Long.parseLong(clave))) {

                            // se deveria crear el archivo de administracion
                            // crearArchivoAdministrado();
                            nivelOperador = "A";
                            btnIngresar.setEnabled(true);
                            btnIngresar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF6A00")));
                            btnSalir.setEnabled(true);

                        } else {

                            File file = new File(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/LECTOR.TXT");
                            if (file.exists()) {
                                procesoVerificacionClave();
                            }else {
                                mostrarDialogoAlerta("Advertencia", "Dispositivo no tiene Archivos\n Cargados... Favor Cargar Datos");
                            }
                        }
                    } else {

                        btnIngresar.setEnabled(true);
                        btnIngresar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF6A00")));
                        btnSalir.setEnabled(true);
                    }

                } else {

                    Log.e("error", "boton 2");
                    btnIngresar.setEnabled(false);
                    btnIngresar.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                    btnSalir.setEnabled(false);
                }

                return true;
            }
        });
        btnSalir.setEnabled(true);
        btnSalir.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnAcercaDe.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                /*File directory = new File(VariablesGlobales.directorioactual + "/DCIM/FOTOGRAFIASL");
                File[] fotos = directory.listFiles();
                for (int i = 0; i < fotos.length; i++)
                {
                    Log.e("INFO", "NombreFotos:" + fotos[i].getName());
                }*/
                /*double latitude = 3.40477, longitude = -76.550315;
                int latSeconds = (int) Math.round(latitude * 3600);
                int latDegrees = latSeconds / 3600;
                latSeconds = Math.abs(latSeconds % 3600);
                int latMinutes = latSeconds / 60;
                latSeconds %= 60;

                int longSeconds = (int) Math.round(longitude * 3600);
                int longDegrees = longSeconds / 3600;
                longSeconds = Math.abs(longSeconds % 3600);
                int longMinutes = longSeconds / 60;
                longSeconds %= 60;
                String latDegree = latDegrees >= 0 ? "N" : "S";
                String lonDegrees = longDegrees >= 0 ? "E" : "W";

                Log.e("INFO",Math.abs(latDegrees) + "°" + latMinutes + "'" + latSeconds
                        + "\"" + latDegree +" "+ Math.abs(longDegrees) + "°" + longMinutes
                        + "'" + longSeconds + "\"" + lonDegrees);*/
                String mensaje = "Sistema Movil de Facturacion \n" + "de Servicios Publicos Version \n" + " ZQ521 " + currentVersionCode  + " \n" + "Derechos Reservados GS&S \n"
                        + "Abril 24 /2026.Ad11_1";//22

                mostrarDialogoAlerta("Informacion", mensaje);
            }

        });

        btnImprime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                MostrarAlertDialog("Test o Reimpresion", "Desea realizar test de impresion[SI] \n" +
                        "Desea reimprimir factura[NO]");
            }
        });

        // Get local Bluetooth adapter
        VariablesGlobales.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (VariablesGlobales.mBluetoothAdapter == null) {
            mensajes("Bluetooth no esta disponible", 1000);
            btnBluetooth.setEnabled(false);
        }

        btnBluetooth.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (!conn) return;
                startDiscovery();
            }
        });

        btnBateria.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                verificarBattery();
            }
        });

        btnInfo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                File file = new File(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/NOMBRE");
                String mensaje = "";

                if (file.exists()) {

                    nombreArchivo(2);
                    mensaje = "Ruta Cargada: " + "Codigo Ruta" + " \n " + "Codigo Lector - Descripcion Lector\n";
                    mostrarDialogoAlerta("Informacion", mensajeDeAlerta);

                } else {

                    mostrarDialogoAlerta("Advertencia", "Dispositivo no tiene Archivos\n Cargados... Favor Cargar Datos");
                }

                txtCodigo.setText("");
                txtClave.setText("");
                txtCodigo.requestFocus();
            }
        });

        btnComunicar.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                iniciarPanelComunicaciones();
            }
        });
        //    Log.e("error","ruta externa "+Environment.getExternalStorageDirectory());
        validarGPS();

        if (Environment.isExternalStorageManager()) {
            preNombreArchivo(0);
        }

    }//end onCreate



    public static String getPhoneHour() {

        Date dt = new Date();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        String formatteHour = df.format(dt.getTime());
        return formatteHour;
    }



    private void verificarArchivosAmayuscula() {
        //-----------------------------------------------------------------------------------------
        File directorio = new File(VariablesGlobales.directorioactual);
        String[] lista = directorio.list();

        String[] filex = {"DATOSDESALIDA", "FOTOGRAFIASL", "ACUERDOS.TXT", "AFORADORES", "DATOSDEENTRADA", "DIRECCIONEIP.TXT", "IMPRESION.LOG", "VALORESFORMATO.LOG", "ARCHIVOSCARGA.CFI", "ADMINISTADORDEPDA.TXT"};//Son los archivos ubicados en el path

        todoMayuscula(lista, filex, directorio);

        File directorio2 = new File(VariablesGlobales.directorioactual + "/DATOSDEENTRADA");
        String[] lista2 = directorio2.list();

        String[] files = {"LOGEVENTOS.LOG", "PRINTER.LOG", "NOMBRE", "ACUERDOS.TXT", "CATEGORIASINF.TXT", "CAUSAS.TXT", "CENSO.TXT", "ESTADOS.TXT", "NOTIFICACIONES.TXT", "OBSERVA.TXT", "ADMINIST.TXT", "ADMINISTADORDEPDA.TXT", "ENVIOSGPRS.SDA", "LECTURAADMINISTRADA.TXT", "ACTIVID.TXT", "CAUSA_NL.TXT", "CLASE_SE.TXT", "CLIENTE.TXT", "CONVENIO.TXT", "DES_CONC.TXT", "DES_TARI.TXT", "EST_CLIE.TXT", "FECHAACONFIGURAR.TXT", "FESTIVOS.TXT", "GENERAL.TXT", "LECTOR.TXT", "MEDIDOR.TXT", "MUNICIP.TXT", "RANGOS.TXT", "REGISTRO.TXT", "TARIFAS.TXT", "FORMATO_CORTA.CPCL", "FORMATO_LARGA.CPCL"};//Son los archivos ubicados en el path};//Son los archivos ubicados en el path

        todoMayuscula(lista2, files, directorio2);

        File directorio3 = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA");
        String[] lista3 = directorio3.list();

        String[] files3 = {"AFOROS.SDA", "AUDITORIASUPERVISOR.SDA", "CERTIFICADOS.SDA", "CLIENTE.SDA", "CO_COBRO.SDA", "CO_COBROHILOS.SDA", "CUENTASGPS.SDA", "DESCARGA", "ENVIOSGPRS.SDA", "LOGEVENTOS.LOG", "MEDIDOR.SDA", "MENSAJES.TXT", "MENSAJESCOPY.TXT", "NOVEDADES.SDA", "PRINTER.LOG", "REGISTRO.SDA", "VALIDADORIMPRESION.SDA"};//Son los archivos ubicados en el path

        todoMayuscula(lista3, files3, directorio3);
        //-----------------------------------------------------------------------------------------
    }

    private String formatNegativo(String x, int tam) {

        if (x.contains("-")) {
            x = String.format("%" + tam + "s", (x.replace("-", ""))).replace(" ", "0");
            x = "-" + x.substring(1);
        } else {
            x = String.format("%" + tam + "s", x).replace(" ", "0");
        }
        return x;
    }

//    public double ejecutarAjusteUnidades(double pesos) {
//        long auxiliar = (long) pesos;
//        double partedecimal;
//        partedecimal = pesos - (double) auxiliar;
//        if (partedecimal < (double) 0.0) {
//            if (partedecimal <= (double) -0.50)
//                pesos = (double) auxiliar - 1;
//            else
//                pesos = (double) auxiliar;
//        } else {
//            if (partedecimal >= (double) 0.50)
//                pesos = (double) auxiliar + 1;
//            else
//                pesos = (double) auxiliar;
//        }
//        return (pesos);
//    }

    private void todoMayuscula(String[] lista, String[] filex, File directorio) { //Ax: esto es para telefonos tipo bv6000 u otros donde fallan los nombres y extensiones, se pasa tod0 a mayusculas (posiblemente RandomAccesfile falla por nombres)
        try {

            for (int i = 0; i < lista.length; i++) {
                for (int k = 0; k < filex.length; k++) {
                    if (lista[i].trim().toUpperCase().equals(filex[k])) { //'CARPETA' == 'CARPETA'

                        if (!lista[i].trim().equals(filex[k])) { //Aqui se quita el uppercase para comparar  'CarpeTa' == 'CARPETA'

                            File file = new File(directorio.getAbsolutePath(), lista[i].trim());
                            File file2 = new File(directorio.getAbsolutePath(), "_" + filex[k]);
                            File file3 = new File(directorio.getAbsolutePath(), filex[k]);
                            file.renameTo(file3);

                            if (file.renameTo(file2)) {

                                if (file.exists()) {
                                    file.delete();
                                }

                                if (file2.exists()) {

                                    file2.renameTo(file3);

                                    if (file2.exists()) {
                                        file2.delete();
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception ex) {
            mensajes("Hay archivos cargados con nombre en minusculas", 2000);
        }
    }

    //Ax: verifica el % de estado de la bateria
    private void verificarBattery() {

        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if (level == -1 || scale == -1) {
            mensajes("BATERIA \n\n    " + "\uD83D\uDD0B" + "\n\n" + 50.0f + "%", 1000);
        }

        mensajes("BATERIA \n\n    " + "\uD83D\uDD0B" + "\n\n" + ((float) level / (float) scale) * 100.0f + "%", 1000);
    }

    private void cargarImagenUsuario(String codigo) {

        try {
            String externalpath = ctx.getExternalFilesDir(null).getParent();
            String hardcoding = "/Android/data/";//Ax: todo: cambiar este hardcoding

            if (externalpath.contains(hardcoding)) {
                externalpath = externalpath.substring(0, externalpath.indexOf(hardcoding));
            }
            File imgFile = new File(externalpath + "/AFORADORES/" + codigo + ".JPG");

            if (imgFile.exists()) {

                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                ImageView myImage = (ImageView)findViewById(R.id.fotoUsuario);

                myImage.setImageBitmap(myBitmap);

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //Ax: trata de obtener la carpeta de Backup en otra SD, sino en otras rutas
    private void ObtenerDirectorioBackup() {

        try {

            VariablesGlobales.directorioBackUp = VariablesGlobales.directorioactual + "/DCIM/SIMFABACKUP/";

          /*  File storageDir = new File(VariablesGlobales.directorioactual.substring(0, VariablesGlobales.directorioactual.lastIndexOf("/")));
            String[] dirList = null;
            //xxy += storageDir.getAbsolutePath() + " _ ";
            if (storageDir.isDirectory()) {
                dirList = storageDir.list(); //Lista de los posibles directorios como sdcard0 y sdcard1
            }

            File nn = new File("/storage/");

            if (nn.isDirectory()) {
                dirList = storageDir.list(); //Lista de los posibles directorios como sdcard0 y sdcard1
            }
            //& null ?
            //xxy+=dirList.length + " _ ";

            File n1 = null;
            File n2 = null;
            File n3 = null;

            if (dirList == null) {
                dirList = nn.list();//B023-4FA7    //  emulated   //  self

                String x1 = "/storage/" + dirList[0];
                String x2 = "/storage/" + dirList[1] + "/0";
                //  String x3 = "/storage/" + dirList[2]+"/0";

//                final File a1 = Environment.getDataDirectory();//   /data
//                final String a3 = Environment.getExternalStorageState();//mounted
//                final File a4 = Environment.getRootDirectory();//   /system
//                final boolean a5 = Environment.isExternalStorageEmulated();// true

                n1 = new File(x1);
                n2 = new File(x2);
                //   n3 = new File(x3);

                dirList = n1.list();

                for(String i:dirList){
                    String pp= x1+i;
                    utils.Log(new File(pp), "testpp");
                }

                dirList = null;
                dirList = n2.list();
//                File[] n4 = new File[28];
//                n4= n2.listFiles();
                dirList = null;
                //   dirList = n3.list();
            }

            //        for (String k : dirList) {

//                if (!(storageDir.getName().toString() + "/" + k).equals(VariablesGlobales.directorioactual)) {
//                    VariablesGlobales.directorioBackUp = storageDir.getName().toString() + "/" + k + "/DCIM/SysBackup/"; //emulated/emulated/DCIM/SysBackup/
//                    break;//;;
//                }
            //  }

            //Ax: es mas facil recorrer la tarjet verificando si existe
*/
   /*         String p1 = VariablesGlobales.directorioBackUp + "/test1.txt";
            utils.Log(new File(p1), "test1");

            File xxx = new File(n1 + "/Prueba/");
            xxx.mkdir();
            xxx.mkdirs();

            String p2 = n1.getAbsolutePath() + "/DCIM/algo.txt";
            utils.Log(new File(p2), "test2");

            File filebck;

            filebck = new File(VariablesGlobales.directorioBackUp);
            filebck.mkdir();
            filebck.mkdirs();
            String h = VariablesGlobales.directorioBackUp + "/algo.txt";
            utils.Log(new File(h), "aaaaaaaaaaa");

            String dd = "/storage/sdcard2/back1/algo.txt";
            utils.Log(new File(dd), "aaaaaaaaaaa");



            /*
            if (!filebck.exists()) {
                if (!filebck.mkdirs()) {
                    VariablesGlobales.directorioBackUp = VariablesGlobales.directorioactual + "/DCIM/SysBackup/";
                    filebck = new File(VariablesGlobales.directorioBackUp);
                }
            }

            if (!filebck.exists()) {

                if (!filebck.mkdirs()) {
                    VariablesGlobales.directorioBackUp = VariablesGlobales.directorioactual + "/DatosDeEntrada/SysBackup/";
                    filebck = new File(VariablesGlobales.directorioBackUp);
                }
            }

            if (!filebck.exists()) {
                if (!filebck.mkdirs()) {
                    VariablesGlobales.directorioBackUp = VariablesGlobales.directorioactual + "/DatosDeEntrada/";
                }
            }

            File nuevo;
            nuevo = new File(VariablesGlobales.directorioBackUp, "systest.txt");

            if (nuevo.exists())
                nuevo.delete();//;;

            if (!nuevo.createNewFile()) {
                VariablesGlobales.directorioBackUp = VariablesGlobales.directorioactual + "/DatosDeEntrada/SysBackup/";
                filebck = new File(VariablesGlobales.directorioBackUp);

                if (!filebck.exists()) {
                    if (!filebck.mkdirs()) {
                        VariablesGlobales.directorioBackUp = VariablesGlobales.directorioactual + "/DatosDeEntrada/";
                    }
                }

                nuevo = new File(VariablesGlobales.directorioBackUp, "systest.txt");

                if (!nuevo.createNewFile())
                    Toast.makeText(this, "No se pudo crear carpeta Backup \n contacte al administrador", Toast.LENGTH_SHORT).show();
            }

            //ojo aqui y en el catch se debe poner variable globales = bakcup

*/
        } catch (Exception e) {
            VariablesGlobales.directorioBackUp = VariablesGlobales.directorioactual + "/DATOSDESALIDA/";
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.gui_acceso, menu);
        return true;
    }

    // maneja los dispositivos escaneados
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case VariablesGlobales.REQUEST_COMUNICACIONES:
                verificarArchivosAmayuscula();
                break;

            case VariablesGlobales.REQUEST_CONNECT_DEVICE:
                addLog("onActivityResult: requestCode==REQUEST_CONNECT_DEVICE");
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    addLog("resultCode==OK");
                    // Get the device MAC address
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    String mensajedes = data.getExtras().getString(DeviceListActivity.MENSAJE_DESASOCIAR);

                    if (mensajedes.equals("SI")) {
                        Desasociar();
                    } else {
                        addLog("onActivityResult: got device=" + address);
                        // Get the BLuetoothDevice object
                        BluetoothDevice device = VariablesGlobales.mBluetoothAdapter.getRemoteDevice(address);
                        txtRemoteDevice.setText(device.getAddress());
                        // txtConectadoCon.setVisibility(TextView.VISIBLE);
                        VariablesGlobales.printerMacAddress = device.getAddress();

                        // Attempt to connect to the device
                        addLog("onActivityResult: connecting device...");
                        // VariablesGlobales.btPrintService.connect(device);
                        connectToDevice(device);
                    }
                }
                VariablesGlobales.bDiscoveryStarted = false;
                break;
            case VariablesGlobales.REQUEST_ENABLE_BT:
                addLog("requestCode==REQUEST_ENABLE_BT");
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(TAG, "onActivityResult: resultCode==OK");
                    // Bluetooth is now enabled, so set up a chat session
                    Log.i(TAG, "onActivityResult: starting setupComm()...");
                    setupComm();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(TAG, "onActivityResult: BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will automatically handle clicks on the Home/Up button, so long as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void startDiscovery() {

        if (VariablesGlobales.bDiscoveryStarted)
            return;

        VariablesGlobales.bDiscoveryStarted = true;

        String conectadoa = "";

        if (VariablesGlobales.btPrintService.getState() != btPrintFile.STATE_DISCONNECTED && VariablesGlobales.btPrintService.getState() != btPrintFile.STATE_IDLE) {

            conectadoa = utils.ReadLine(logPrint);
        }

        Bundle bundle = new Bundle();
        bundle.putString("conectadoa", conectadoa);

        // Lanza DeviceListActivity para ver dispositivos y escanear
        Intent serverIntent = new Intent(this, DeviceListActivity.class);

        serverIntent.putExtras(bundle);

        startActivityForResult(serverIntent, VariablesGlobales.REQUEST_CONNECT_DEVICE);
    }

    public void addLog(String s) {
        Log.d(TAG, s);
    }

    private void Desasociar() {

        try { //Ax: esta parte trata de cerrar cualquier conexion, pero puede que no exista tal conexion
            VariablesGlobales.btPrintService.stop();
        } catch (Exception ex) {
        }
    }

    /***
     * Trata de verificar el estatus de la impresora para ver si se conecta, anula o no hace nada
     */
    private void VerificarPreconexionImpresora() { //Aqui trata de conectarse, desde Onstart(primera vez)

        if (VariablesGlobales.bDiscoveryStarted) {
            return;
        }

        if (VariablesGlobales.btPrintService.getState() == btPrintFile.STATE_DISCONNECTED || VariablesGlobales.btPrintService.getState() == btPrintFile.STATE_IDLE || VariablesGlobales.habilitadaimpresora == 0) {
            conexionForzadaImpresora();
            return;
        } else if (VariablesGlobales.btPrintService.getState() == btPrintFile.STATE_CONNECTING) {
            return;
        }

        if (!VariablesGlobales.prueba_impresora()) { //Prueba de impresora envia un caracter al la impresora y devuelve falso si falla

            try { //Ax: esta parte trata de cerrar cualquier conexion, pero puede que no exista tal conexion
                VariablesGlobales.btPrintService.stop();
                //setConnectState(btPrintFile.STATE_DISCONNECTED);
            } catch (Exception ex) {
            }
            conexionForzadaImpresora();
        } else {
            return;
        }
    }

    /***
     * Ax: trata de conectar a la impresora, obteniendo la mac guardada en archivo log
     */
    private void conexionForzadaImpresora() {

        String printer = utils.ReadLine(logPrint);

        if (!printer.equals("")) { //Ax: hay algun texto en el log

            try {
                conn = false;
                BluetoothDevice device = VariablesGlobales.mBluetoothAdapter.getRemoteDevice(printer);
                VariablesGlobales.printerMacAddress = device.getAddress();
                connectToDevice(device);

            } catch (Exception ex) {
                conn = true;
                utils.Log(logfile, "[GuiAcceso] conexionForzadaImpresora()" + ex.getMessage());
                VariablesGlobales.bDiscoveryStarted = false;
                txtRemoteDevice.setText("Error de conexion impresora");
            }
        } else {
            if (VariablesGlobales.btPrintService.getState() == btPrintFile.STATE_DISCONNECTED || VariablesGlobales.btPrintService.getState() == btPrintFile.STATE_IDLE || VariablesGlobales.habilitadaimpresora == 0) {
                conn = true;
            }
            txtRemoteDevice.setText("No hay Impresora asociada");
            mensajes("No hay Impresora asociada", 1000);
        }
    }

    // Validar el usuario programado para la lectura
    private void validarCodigoUsuario() {

        String fecha = getPhoneDate();

        if (Integer.parseInt(fecha.trim()) < 20170606) {
            mensajes("Fecha del sistema desactualizada: ", 1000);
            File fechador = new File(VariablesGlobales.getDirectorioactual() + "/DATOSDEENTRADA/FECHADOR.TXT");

            if (fechador.exists()) {
                fechador.delete();
            }
        }
        File archivoGeneral = new File(VariablesGlobales.getDirectorioactual() + "/DATOSDEENTRADA/LECTOR.TXT");

        if (archivoGeneral.exists()) {

            nombreArchivo(1);

            tablaEncabezado.setArchivo_TablaEncabezado(VariablesGlobales.getDirectorioactual() + "/DATOSDEENTRADA/GENERAL.TXT");
            tablaAforadores.setArchivo_TablaAforadores(VariablesGlobales.getDirectorioactual() + "/DATOSDEENTRADA/LECTOR.TXT");

            if (tablaEncabezado.abrir_TablaEncabezado(tablaEncabezado.getArchivo_TablaEncabezado())) {

                tablaEncabezado.lectura_TablaEncabezado(1);

                VariablesGlobales.setAdministrador(tablaEncabezado.getTablaEncabezado_administrador());  //[enerca] se dejan los valores predeterminados de la clase
                // VariablesGlobales.setTotalimpresiones(tablaEncabezado.);
                VariablesGlobales.setConsumoauditoria(tablaEncabezado.getTablaEncabezado_tiempoauditoria());
                VariablesGlobales.setDistanciagps(tablaEncabezado.getTablaEncabezado_distanciagps());
                VariablesGlobales.setNrodias(tablaEncabezado.getTablaEncabezado_nrodias());
                VariablesGlobales.setFechainicial(tablaEncabezado.getTablaEncabezado_fechainicial());
                VariablesGlobales.setFechafinal(tablaEncabezado.getTablaEncabezado_fechafinal());
                VariablesGlobales.setObligafotos(tablaEncabezado.getTablaEncabezado_obligafotos());
                Log.e("error", "obliga foto " + "yyy " + tablaEncabezado.getTablaEncabezado_obligafotos());
                VariablesGlobales.setObligabarras("0");//tablaEncabezado.getTablaEncabezado_obligabarras()
                Log.e("error", "obliga barra " + "xxx " + tablaEncabezado.getTablaEncabezado_obligabarras());
                VariablesGlobales.setMaximoregaenviar(tablaEncabezado.getTablaEncabezado_maximoregaenviar());
                VariablesGlobales.setTipoDeRuta(tablaEncabezado.getTablaEncabezado_tiporuta());
                VariablesGlobales.EvaluarCritica40 = Integer.parseInt(tablaEncabezado.getTablaEncabezado_obligabarras().trim());
                Log.e("error2", "obliga obliga40 " + "zzz " + tablaEncabezado.getTablaEncabezado_obligabarras());

                Log.e("INFO","fecha-proceso: " + tablaEncabezado.gettablaEncabezado_FECHAPROCESO().trim());
                VariablesGlobales.fechaproceso =  tablaEncabezado.gettablaEncabezado_FECHAPROCESO().trim();


                VariablesGlobales.diasvence = Integer.parseInt(tablaEncabezado.getTablaEncabezado_diasdecorte());
                VariablesGlobales.diascorte = Integer.parseInt(tablaEncabezado.getTablaEncabezado_diasdesuspencion());

                if (VariablesGlobales.getMinimovalorentrega() == null) {
                    VariablesGlobales.setMinimovalorentrega("0");
                }

                File archivoNombre = new File(VariablesGlobales.getDirectorioactual() + "/DATOSDEENTRADA/NOMBRE");

                if (!archivoNombre.exists()) {

                    try {

                        RandomAccessFile rFile = new RandomAccessFile(archivoNombre, "rw");

                        String generalDpto = tablaEncabezado.gettablaEncabezado_DPTO().trim();
                        String.format("%+3s", generalDpto);
                        String generalMunicipio = tablaEncabezado.gettablaEncabezado_MUNICIPIO().trim();
                        String.format("%+3s", generalMunicipio);

                        rFile.writeBytes("!!!!C" + generalDpto + generalMunicipio + "??     .");
                        rFile.close();
                        nombreArchivo(1);

                    } catch (FileNotFoundException e) {

                        e.printStackTrace();
                    } catch (IOException e) {

                        e.printStackTrace();
                    }
                }

                if (!tablaAforadores.abrir_TablaAforadores(tablaAforadores.getArchivo_TablaAforadores())) {
                    mensajes("No existe Archivo LECTOR.TXT...\nCargue Datos...", 1000);
                    tablaEncabezado.Cerrar_TablaEncabezado();
                    return;
                }

                if(!verificarConfSistema(tablaEncabezado.gettablaEncabezado_FECHAPROCESO().trim())){
                    utils.Log(logfile, "[GuiAcceso] verificarConfSistema() | " + msg_val);
                    mostrarDialogoAlerta("Advertencia", msg_val);
                    return;
                }

                tablaAforadores.buscarSecuencial_TablaAforadores(txtCodigo.getText().toString().trim());

                if (tablaAforadores.getEncontro_TablaAforadores() > 0) {

                    //[enerca] if (!VariablesGlobales.tipoDeRuta.trim().equals("E")) {

                    if (tablaAforadores.gettablaAforadores_ESTADO().equals("L")) {
                        if (Long.parseLong(txtCodigo.getText().toString().trim()) != Long.parseLong(tablaEncabezado.gettablaEncabezado_LECTOR().trim())) {
                            mensajes("Lector No Programado Intente con Otros", 1000);
                            tablaEncabezado.Cerrar_TablaEncabezado();
                            tablaAforadores.Cerrar_TablaAforadores();
                            txtCodigo.setText("");
                            return;
                        }
                    }
                    //}
                    // aqui nos damos cuenta que si existe el operador en la base de datos controlar el que esta realmente esta programado para la ruta

                    nivelOperador = tablaAforadores.gettablaAforadores_ESTADO().trim();
                    txtNombreUsuario.setText(tablaAforadores.gettablaAforadores_DESCRIPCION().trim());
                    VariablesGlobales.setNombreLectorPDA(tablaAforadores.gettablaAforadores_DESCRIPCION());
                    txtClave.setEnabled(true);
                    // txtClave.requestFocus();

                } else {

                    nivelOperador = "";
                    txtClave.setEnabled(false);
                    txtNombreUsuario.setText("Aforador/Super.. No Existe");
                    Toast.makeText(getApplicationContext(), "LECTOR PROGRAMADO \nNO EXISTE..." + txtCodigo.getText(), Toast.LENGTH_LONG).show();
                    txtCodigo.setText("");
                    txtClave.setText("");
                    txtCodigo.requestFocus();
                }
                tablaEncabezado.Cerrar_TablaEncabezado();
                tablaAforadores.Cerrar_TablaAforadores();

            } else {

                Toast.makeText(getApplicationContext(), "No pudo leer archivo " + tablaEncabezado.getArchivo_TablaEncabezado(), Toast.LENGTH_LONG).show();
                txtCodigo.setText("");
                txtClave.setText("");
                tablaEncabezado.Cerrar_TablaEncabezado();
            }
        } else {
            mostrarDialogoAlerta("Advertencia", "Dispositivo no tiene Archivos\n Cargados... Favor Cargar Datos");
            txtClave.setText("12282702");
            txtClave.setEnabled(true);
        }
    }
    //Ax: Archivo necesario para entrar a comunicaciones
    private void preNombreArchivo(Integer Alerta) {

        File archivoNombre = new File(VariablesGlobales.getDirectorioactual() + "/DATOSDEENTRADA/NOMBRE");
        txtCondicion.setText(" | Archivo:" + "CicXXXX" + " " + "Csecxxxx.ddd");
        if (!archivoNombre.exists() && archivoNombre.length() < 1) {
            if (Alerta>0)
                mostrarDialogoAlerta("Advertencia", "Dispositivo no tiene Archivos\n Cargados... Favor Cargar Datos: ");

            btnInfo.setImageResource(R.drawable.protecciondedatos1);
            return;
        }

        try {
            RandomAccessFile rFile = new RandomAccessFile(archivoNombre, "rw");
            int fileSize = (int) rFile.length();
            byte[] byteArray = new byte[fileSize];
            rFile.readFully(byteArray, 0, fileSize);
            String archivo_cargado = new String(byteArray);
//| Archivo: AAAAMMRRR LXXXXXXX.000 txtcondicion
            String NombreArchivos_0 = archivo_cargado.substring(4, 12);//cambia 5, 14
            txtCondicion.setText(" | Carga: CIC" + archivo_cargado.substring(0, 4).trim() + " Rut. " + NombreArchivos_0);
            rFile.close();
            btnInfo.setImageResource(R.drawable.protecciondedatos);
        } catch (Exception ex) {
            utils.Log(logfile, "[GuiAcceso]preNombreArchivo()|ERROR -> " + ex.getMessage());
            mensajes("Error Procesando Archivo Nombre! \n" + ex.getMessage(), 1000);
        }
    }
    // Metodo para validar el archivo cargado en la terminal
    private void nombreArchivo(int tipo) {

        File archivoNombre = new File(VariablesGlobales.getDirectorioactual() + "/DATOSDEENTRADA/NOMBRE");
        String archivo_cargado = "";

        if (archivoNombre.exists() && archivoNombre.length() > 1) {

            try {

                RandomAccessFile rFile = new RandomAccessFile(archivoNombre, "rw");
                int fileSize = (int) rFile.length();
                byte[] byteArray = new byte[fileSize];
                rFile.readFully(byteArray, 0, fileSize);
                archivo_cargado = new String(byteArray);
                variables.setNombregeneral(VariablesGlobales.getDirectorioactual() + "/DATOSDEENTRADA/GENERAL.TXT");

                archivocargado = archivo_cargado.substring(4, 12);
                txtCondicion.setText("Archivo: CIC"+archivo_cargado.substring(0, 4)+" - "+archivocargado);
                mensajeDeAlerta = "Ruta Cargada: " + archivocargado;
                rFile.close();

                if (tipo == 1) {

                    return;
                }

                File fileGeneral = new File(variables.getNombregeneral());
                if (fileGeneral.exists()) {

                    tablaEncabezado.setArchivo_TablaEncabezado(variables.getNombregeneral());
                    tablaAforadores.setArchivo_TablaAforadores(VariablesGlobales.getDirectorioactual() + "/DATOSDEENTRADA/LECTOR.TXT");

                    if (tablaEncabezado.abrir_TablaEncabezado(tablaEncabezado.getArchivo_TablaEncabezado())) {

                        tablaEncabezado.lectura_TablaEncabezado(1);

                        if (!tablaAforadores.abrir_TablaAforadores(tablaAforadores.getArchivo_TablaAforadores())) {

                            mensajeDeAlerta = "\nNo existe tabla Lector...";
                            tablaEncabezado.Cerrar_TablaEncabezado();
                            return;
                        }

                        tablaAforadores.buscarSecuencial_TablaAforadores(tablaEncabezado.gettablaEncabezado_LECTOR());

                        if (tablaAforadores.getEncontro_TablaAforadores() > 0) {

                            mensajeDeAlerta += "\n" + tablaAforadores.gettablaAforadores_CODIGO() + " - " + tablaAforadores.gettablaAforadores_DESCRIPCION();
                        } else {

                            mensajeDeAlerta += "\nAforador en la lista\nNo Existe...";
                        }

                        tablaEncabezado.Cerrar_TablaEncabezado();
                        tablaAforadores.Cerrar_TablaAforadores();
                    }

                } else {

                    Toast.makeText(getApplicationContext(), "No Hay Ruta Cargada", Toast.LENGTH_LONG).show();
                }

            } catch (FileNotFoundException e) {

                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error Procesando Nombre de Archivo! \n FileNotFoundException", Toast.LENGTH_LONG).show();

            } catch (IOException e) {

                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error Procesando Nombre de Archivo! \n IOException", Toast.LENGTH_LONG).show();
            }

        } else {
            mostrarDialogoAlerta("Advertencia", "Dispositivo no tiene Archivos\n Cargados... Favor Cargar Datos");
            mensajeDeAlerta += "No Hay Ruta Cargada";
        }
    }

    private String getPhoneDate() {

        Calendar cal = new GregorianCalendar();
        Date date = cal.getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        String formatteDate = df.format(date);
        return formatteDate;
    }

    public void testSD() {

        String estado = Environment.getExternalStorageState();

        if (estado.equals(Environment.MEDIA_MOUNTED)) {
            sdDisponible = true;
            sdAccesoEscritura = true;
        } else if (estado.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            sdDisponible = true;
            sdAccesoEscritura = false;
        } else {
            sdDisponible = false;
            sdAccesoEscritura = false;
        }
    }

 //   public String getSerialNumber() {

//        TelephonyManager tManager = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
//        String deviceIMEI = tManager.getDeviceId();
//        return deviceIMEI;

//        String imei = "";
//        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

//        if (Build.VERSION.SDK_INT >= 26) {//Build.VERSION_CODES.O;
//            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
//                if (telephonyManager != null) {
//                    try {
//                        imei = telephonyManager.getImei();
//                        if (telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
//                             imei = telephonyManager.getMeid();
//                        } else if (telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
//                            imei = telephonyManager.getImei();
//                            // imei = telephonyManager.getDeviceId();
//                        }
//
//                        esMayor9 = true;
//                    } catch (Exception e) {
//                        esMayor9 = true;
//                        imei = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
//                    }
//                }
//            } else {
//                ActivityCompat.requestPermissions(GuiAcceso.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1010);
//            }
//        } else {
//            if (ActivityCompat.checkSelfPermission(GuiAcceso.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
//                if (telephonyManager != null) {
//                    imei = telephonyManager.getDeviceId();
//                }
//            } else {
//                ActivityCompat.requestPermissions(GuiAcceso.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1010);
//            }
//     //   }
//        return imei;
 //   }

    private boolean verificarConfSistema(String fechasis){//96330325
        boolean airplane_conex, date_act;
        try {
            // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            airplane_conex = Settings.System.getInt(this.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;

            SimpleDateFormat ftsis = new SimpleDateFormat("dd/MM/yyyy");
            Date datesis = ftsis.parse(fechasis);
            date_act = new Date().after(datesis);
            Log.e("INFO","airplane_conex: " + airplane_conex + " | date_act: " + date_act);
            if(airplane_conex && !date_act){// Modo avion esta encendido y la fecha del sistema esta atrazada frente a la del proceso
                msg_val = "El modo avion se encuentra activo y la fecha del sistema esta desactualizada, comuniquese con el supervisor";
                return false;
            }
            if(airplane_conex){// Modo avion esta encendido
                msg_val = "El modo avion se encuentra activo, comuniquese con el supervisor";
                return false;
            }
            if(!date_act){// La fecha del sistema esta atrazada frente a la del proceso
                msg_val = "La fecha del sistema esta desactualizada, comuniquese con el supervisor";

                return true;//false

            }
            return true;
        }catch (Exception ex){
            Log.e("ERROR","verificarConfSistema() | Error -> " + ex.getMessage());
            msg_val = "Error al verificar las variables del sistema por favor comuniquese con el supervisor";
            utils.Log(logfile, "[GuiAcceso] verificarConfSistema() | Error -> " + ex.getMessage());
            return false;
        }
    }
    private void mostrarDialogoAlerta(String titulo, String mensaje) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titulo);
        builder.setMessage(mensaje);

        builder.setIcon(R.drawable.ic_launcher1);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.i("Dialogo Acerca De", "Boton Ok pulsado");
            }
        });
        builder.show();
    }

    private boolean crearArchivoAdministrado() {

        String fecha = getPhoneDate();
        String hora = getPhoneHour();

        if (Long.parseLong(hora.substring(0, 2) + hora.substring(3, 5)) < 900) {

            return false;
        }

        File nombreArchivo = new File(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/LECTURAADMINISTRADA.TXT");
        if (nombreArchivo.exists()) {
            // Esto no entra en el proceso de administracion
            nombreArchivo.delete();
        }

        try {
            String x = nombreArchivo.getAbsoluteFile().toString();

            RandomAccessFile archivoAdministrado = new RandomAccessFile(nombreArchivo, "rw");
            archivoAdministrado.writeBytes(fecha);
            archivoAdministrado.close();
            // txtCodigo.setText("");
            // txtClave.setText("");
            txtClave.setEnabled(false);
            Toast.makeText(getApplicationContext(), "EQUIPO AUTORIZADO PARA LECTURAS POSTERIORES A LAS 9 AM", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void procesoVerificacionClave() {

        try {
            String cadenaClave = tablaAforadores.gettablaAforadores_CLAVE(); // variables.desencapsular(tablaAforadores.gettablaAforadores_CLAVE(), datosEncriptados);
            Integer claveDigitada = Integer.parseInt(txtClave.getText().toString().trim());

            if (claveDigitada == Integer.parseInt(cadenaClave.trim()) && tablaAforadores.gettablaAforadores_ESTADO().equals("A")) {
                Log.e("error", "boton 4");
                txtClave.setEnabled(false);
                aforadorClaveCorrecto = 1;
                nivelOperador = tablaAforadores.gettablaAforadores_ESTADO();
                btnSalir.setEnabled(true);
                btnIngresar.setEnabled(true);
                btnIngresar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF6A00")));
                btnSalir.requestFocus();
                Toast.makeText(getApplicationContext(), "Ingreso como Administrador", Toast.LENGTH_LONG).show();

                // crearArchivoAdministrado();
            } else {

                // Buscar la clave en el archivo CLAVES.CSV para asi mirar si
                // inicializa administrativamente ese registro
                File claves = new File(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/CLAVES.CSV	");

                if (claves.exists()) {

                    // Todo: implementar
                }

                //antes S
                if (claveDigitada == Long.parseLong(cadenaClave.trim()) || claveDigitada == Long.parseLong(claveSalida) || nivelOperador.equals("S")) {

                    txtClave.setEnabled(false);
                    aforadorClaveCorrecto = 1;
                    btnSalir.setEnabled(true);
                    btnIngresar.setEnabled(true);
                    btnIngresar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF6A00")));
                    btnSalir.requestFocus();
                } else {

                    txtClave.setText("");
                    txtClave.setEnabled(false);
                    txtCodigo.setText("");
                    txtNombreUsuario.setText("Clave incorrecta para el: ");

                    Toast.makeText(getApplicationContext(), "CLAVE INCORRECTA DEL USUARIO...", Toast.LENGTH_LONG).show();
                    btnSalir.setEnabled(false);
                    Log.e("error", "boton 3");
                    btnIngresar.setEnabled(false);
                    btnIngresar.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                    return;
                }
                cargarImagenUsuario("" + txtCodigo.getText().toString().trim());
            }
        } catch (Exception ex) {
            mensajes("Error con la Clave del lector... consulte con el Administrador...", 1000);
        }
    }

    private void ingresoPrincipal() {

        variables.setGlobaloperario(txtCodigo.getText().toString());
        NombreOperador = txtNombreUsuario.getText().toString();
        Bundle bundle = new Bundle();
        bundle.putString("codigoUsuario", txtCodigo.getText().toString());
        bundle.putString("nivelOperador", nivelOperador);
        bundle.putString("IMEI", txtCodInterno.getText().toString());
        bundle.putString("NombreOperador", NombreOperador);
        bundle.putString("fecha", txtFecha.getText().toString());

        if (!(archivocargado.equals("")) && !(archivocargado == null)) {

            bundle.putString("archivocargado", archivocargado);

        }
        Intent i = new Intent(this, MenuPrincipal.class);
        i.putExtras(bundle);

        startActivity(i);
    }

    void connectToDevice(BluetoothDevice _device) {
        if (_device != null) {
            String dirmac = _device.getAddress();
            VariablesGlobales.btPrintService.connect(_device);

            macAdress = dirmac;
            utils.WriteLine(logPrint, dirmac); //escribe en log la mac
            // VariablesGlobales.btPrintService.getState()
        } else {
            addLog("unknown remote device!");
        }
    }

    void connectToDevice() {
        String remote = txtRemoteDevice.getText().toString();
        if (remote.length() == 0)
            return;
        if (VariablesGlobales.btPrintService.getState() == btPrintFile.STATE_CONNECTED) {
            VariablesGlobales.btPrintService.stop();
            //setConnectState(btPrintFile.STATE_DISCONNECTED);
            return;
        }

        String sMacAddr = remote;
        if (sMacAddr.contains(":") == false && sMacAddr.length() == 12) {
            // If the MAC address only contains hex digits without the
            // ":" delimiter, then add ":" to the MAC address string.
            char[] cAddr = new char[17];

            for (int i = 0, j = 0; i < 12; i += 2) {
                sMacAddr.getChars(i, i + 2, cAddr, j);
                j += 2;
                if (j < 17) {
                    cAddr[j++] = ':';
                }
            }

            sMacAddr = new String(cAddr);
        }

        BluetoothDevice device;
        try {
            device = VariablesGlobales.mBluetoothAdapter.getRemoteDevice(sMacAddr);
        } catch (Exception e) {

            Toast.makeText(getApplicationContext(), "Invalid BT MAC address", Toast.LENGTH_LONG).show();
            // myToast("Invalid BT MAC address");
            device = null;
        }

        if (device != null) {
            addLog("connecting to " + sMacAddr);
            VariablesGlobales.btPrintService.connect(device);
        } else {
            addLog("unknown remote device!");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (myThread.isAlive()) {
            myThread.interrupt();
        }
        // Stop the Bluetooth chat services
        if (VariablesGlobales.btPrintService != null){
            VariablesGlobales.btPrintService.stop();
        }
    }

    private void setupComm() {
        // Initialize the array adapter for the conversation thread
        VariablesGlobales.mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.activity_txt_remote_device);//R.id.txtRemoteDevice Ax: todo: revisar esto , antes apuntaba a txt...se le creo este louttx...indefinida su funcion
        Log.d(TAG, "setupComm()");
        VariablesGlobales.btPrintService = new btPrintFile(this, mHandler);
        if (VariablesGlobales.btPrintService == null)
            Log.e(TAG, "VariablesGlobales.btPrintService init() failed");
        /*
         * // Initialize the array adapter for the conversation thread
         * VariablesGlobales.mConversationArrayAdapter = new
         * ArrayAdapter<String>(this, R.layout.message); mConversationView =
         * (ListView) findViewById(R.id.in);
         * mConversationView.setAdapter(VariablesGlobales
         * .mConversationArrayAdapter);
         *
         * // Initialize the compose field with a listener for the return key
         * mOutEditText = (EditText) findViewById(R.id.edit_text_out);
         * mOutEditText.setOnEditorActionListener(mWriteListener);
         *
         * // Initialize the send button with a listener that for click events
         * mSendButton = (Button) findViewById(R.id.button_send);
         * mSendButton.setOnClickListener(new OnClickListener() { public void
         * onClick(View v) { // Send a message using content of the edit text
         * widget TextView view = * (TextView) findViewById(R.id.edit_text_out);
         * String message = view.getText().toString(); sendMessage(message); }
         * });
         *
         * // Initialize the BluetoothChatService to perform bluetooth
         * connections mChatService = new BluetoothChatService(this, mHandler);
         *
         * // Initialize the buffer for outgoing messages mOutStringBuffer = new
         * StringBuffer("");
         */
    }

    @Override
    public void onStart() {
        super.onStart();

        if (VariablesGlobales.mBluetoothAdapter != null) {
            // If BT is not on, request that it be enabled. setupChat() will then be called during onActivityResult
            if (!VariablesGlobales.mBluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, VariablesGlobales.REQUEST_ENABLE_BT);
                // Otherwise, setup the comm session
            } else {
                if (VariablesGlobales.btPrintService == null)
                    setupComm();
                // setupChat();
            }
        }
    }

    public void ImpresoraStatus() {

        switch (VariablesGlobales.btPrintService.getState()) {
            case btPrintFile.STATE_DISCONNECTED:
                txtRemoteDevice.setText("DESCONECTADA");//
                conn = true;
                break;
            case btPrintFile.STATE_CONNECTED:
                txtRemoteDevice.setText("CONECTADO " + macAdress);
                conn = true;
                break;
            case btPrintFile.STATE_CONNECTING:
                conn = false;
                txtRemoteDevice.setText("CONECTANDO..... ");
                break;
            case btPrintFile.STATE_IDLE:
                txtRemoteDevice.setText("DESCONECTADA");
                conn = true;
                break;
            case btPrintFile.STATE_LISTEN:
                txtRemoteDevice.setText("CONECTADO a " + macAdress);
                break;
            default:
                txtRemoteDevice.setText("OBTENIENDO STATUS...");
                conn = true;
                break;
        }
    }

    public void doWork() {
        runOnUiThread(new Runnable() {
            public void run() {
                try {

                    Calendar calendar = Calendar.getInstance();

                    String fecha = calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR);
                    int hours = calendar.get(Calendar.HOUR_OF_DAY);
                    int minutes = calendar.get(Calendar.MINUTE);
                    int seconds = calendar.get(Calendar.SECOND);

                    String mins = "" + minutes;
                    String secs = "" + seconds;

                    if (minutes < 10) {

                        mins = "0" + mins;
                    }
                    if (seconds < 10) {

                        secs = "0" + secs;
                    }

                    String curTime = fecha + " - " + hours + ":" + mins + ":" + secs;
                    txtFecha.setText(curTime);

                    ++contt;
                    if (contt > 6) {
                        contt = 0;

                        if (!impresora1vez) { //Esta variable debe ser estatica para permanencer al regreso de alguna actividad
                            impresora1vez = true;
                            VerificarPreconexionImpresora(); //Ax: trata de conectarse a la impresora si existe log impresora
                        }
                        ImpresoraStatus();
                    }

                } catch (Exception e) {
                }
            }
        });
    }

    private void iniciarPanelComunicaciones() {

        Bundle bundle = new Bundle();

        bundle.putString("NIVEL_OPERADOR", nivelOperador);
        bundle.putString("CODIGO_INTERNO_PDA", txtCodInterno.getText().toString().trim());

        Intent comunicaciones = new Intent(this, Comunicaciones.class);
        comunicaciones.putExtras(bundle);
        //  startActivity(comunicaciones);
        startActivityForResult(comunicaciones, VariablesGlobales.REQUEST_COMUNICACIONES);


        String dirEjecutable = VariablesGlobales.directorioactual;
        File file = new File(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/ADMINIST.TXT"); //claves prohibiciones

        if (file.exists()) {
            File file2 = new File(dirEjecutable + "/ADMINISTADORDEPDA.TXT");

            if (file2.exists()) {

                file2.delete();
            }

            VariablesGlobales.copyFile(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/ADMINIST.TXT", dirEjecutable + "/ADMINISTADORDEPDA.TXT", true);
        }
    }

    // SetGravity No funciona Con versiones de API mayores a 30
    private void mensajes(String msg, int dur) {

        Toast toast = Toast.makeText(GuiAcceso.this, msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 10, 150);
        toast.show();
    }

    class Reloj implements Runnable {
        // @Override
        public void run() {
            while (!myThread.currentThread().isInterrupted()) {
                try {
                    doWork();
                    myThread.sleep(1000); // Pause of 1 Second
                } catch (InterruptedException e) {
                    myThread.currentThread().interrupt();
                } catch (Exception e) {
                }
            }
        }
    }

    private class MyLocationListener implements LocationListener {


        @Override
        public void onLocationChanged(Location loc) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    }


    public void validarGPS() {

        if (!mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            androidx.appcompat.app.AlertDialog.Builder alertConfCreado = new androidx.appcompat.app.AlertDialog.Builder(this);
            alertConfCreado
                    .setMessage("Debe activar GPS para continuar")
                    .setTitle("Alerta")
                    .setCancelable(false)
                    .setPositiveButton("Terminar",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                    finish();
                                }
                            });
            androidx.appcompat.app.AlertDialog alert = alertConfCreado.create();
            alert.show();
        }
    }


    public void MostrarAlertDialog(String titulo, String mensaje) { //Crea un alertDialog con si-no y espera hasta un clic SI o No

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titulo);
        builder.setMessage(mensaje);
        builder.setIcon(R.drawable.ic_launcher1);
        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (!conn) return;

                if (VariablesGlobales.prueba_cabeza() > 0) {

                    return;
                } else if (VariablesGlobales.prueba_cabeza() == 0) {
                    mensajes("No se pudo imprimir, Conectando...", 1000);
                    VerificarPreconexionImpresora();
                    return;
                }
                mensajes("Problemas enviando a la impresora ", 500);
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent reimpresion = new Intent(getApplicationContext(), ModuloReimpresion.class);
                //startActivity(reimpresion);
                //finish();
                startActivityForResult(reimpresion, 111);


            }
        });

        builder.create().show();
    }

}
