package com.gselectroCaqueta.accesoyseguridad;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gselectroCaqueta.comunicaciones.Comunicaciones;
import com.gselectroCaqueta.tablas.EnvioGPS;
import com.gselectroCaqueta.tablas.Generica;
import com.gselectroCaqueta.tablas.TablaClienteSalida;
import com.gselectroCaqueta.tablas.TablaEntradaClientes;
import com.gselectroCaqueta.tablas.TablaRegistroSalida;
import com.gsutil.Utils;
import com.gsutil.WSSoap;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import io.realm.Realm;

public class MenuPrincipal extends AppCompatActivity {

    private static int TAKE_PICTURE = 1;
    public final int ADICIONARNOVDAD_REQUEST_CODE = 6674;
    public final int ADICINCERTIFCAD_REQUEST_CODE = 6680;
    //-------------------------------------------------------
    //-------------------------------------------------------
    public final int RESUMEN_REQUEST_CODE = 3203; //Ax: numero para activity for requiest
    public File logfile;
    public String[] Archivos_CPCAN;
    Utils util = new Utils();
    String aforador = "";
    String nivelOperador = "";
    String terminal = "";
    String archivoCargado = "";
  //  TextView txtArchivoCargado;
    VariablesGlobales variables = new VariablesGlobales();
    ImageButton btnIniciarLecturas;
    ImageButton btnUtilidades;
    ImageButton btnRetornar;
    ImageButton btnEliminarInfo;
    ImageButton btnMenuComunicar;
   // ImageButton btnFotos;

    String nombrefoto = "";
    String nombredelaImagen = "";
    Utils utils = new Utils();
    String fecha = "";
    String lactitud = "";
    String longitud = "";
    int tipoFotoDigital = 0;
    String mensajeNovedad = "";
    int procesandoenvioenHilos_CPCAN = 0;
    String metodo = "";
    String URL = "";
    String paginaWs = "";
    String esComprimido = "";
    String Ciclo = "0000";
    String Municipio = "000";
    String Seccion = "000";
    String Division = "0";
    String rutaAdministrador = "";
    int trama = 100;
    int vecesImagen = 0;
    boolean banderaNovedad = false;
    boolean banderaCertificado = false;
    int capRegistroActual = 0;
    int contCert = 1;
    int contNov = 1;
    private String name = "";
    String dataTomoLec = "";
    String NombreZip = "";
    String ArchivoAEnviarRecibir = "";
    BDifNull bdl;


    TablaEntradaClientes tablaEntradaCliente = new TablaEntradaClientes();
    TablaClienteSalida infoClienteSalida = new TablaClienteSalida();
    TablaRegistroSalida infoRegistroSalida = new TablaRegistroSalida();
    public EnvioGPS misenvios = new EnvioGPS();
    String ultimaRutaSeleccionada = "";
    TextView txt_nombre, txtRuta, txtPrg;
    ImageView fotoAforador;
    String NombreOperador = "Administrador";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal);
        bdl = new BDifNull(this);
        Bundle bundle = getIntent().getExtras();//pasar datos entre actividades

      //  txtArchivoCargado = (TextView) findViewById(R.id.txtArchivoCargado);

        archivoCargado = bundle.getString("archivocargado");
        aforador = bundle.getString("codigoUsuario");
        nivelOperador = bundle.getString("nivelOperador");
        terminal = bundle.getString("IMEI");
        NombreOperador = bundle.getString("NombreOperador");

      //  txtArchivoCargado.setText(archivoCargado);

        btnIniciarLecturas = (ImageButton) findViewById(R.id.btnIniciarLecturas);
        btnUtilidades = (ImageButton) findViewById(R.id.btnUtilidades);
        btnRetornar = (ImageButton) findViewById(R.id.btnRetornar);
        btnEliminarInfo = (ImageButton) findViewById(R.id.btnElimnarInfo);
        btnMenuComunicar = (ImageButton) findViewById(R.id.btnComunica);
        txt_nombre = (TextView) findViewById(R.id.txt_nombre);
        txtRuta = (TextView) findViewById(R.id.txtRuta);
        txtPrg = (TextView) findViewById(R.id.txtPrg);
        fotoAforador = (ImageView) findViewById(R.id.fotoAforador);

        txt_nombre.setText(NombreOperador);
        ImageButton btnAyuda;
       // btnFotos = (ImageButton) findViewById(R.id.btnFotos);
        fecha = bundle.getString("fecha");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationManager locationmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener mlocListener = new UsarGPS();

        locationmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, mlocListener);
        seleccionUrl();
        CargarArchivoCarga();
        cargarArchivoNombre();
        realizarConteo();
        Log.e("INFO","aforador: " + aforador);
        cargarImagenUsuario(aforador.trim());


        //-------------------------------------------

        btnIniciarLecturas.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                procesoLecturas();
            }
        });

        btnUtilidades.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                utilidades();
            }
        });

        btnRetornar.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                retornar();
            }
        });

        btnEliminarInfo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                ejecutarBorradoDeArchivos();
            }
        });

        btnMenuComunicar.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                iniciarPanelComunicaciones();
            }
        });

       /* btnFotos.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                ejecutarProcesoDeFoto("999999999" + "_" + calendar.get(Calendar.MONTH), 1, tipoFotoDigital, 0, "", 1);
            }
        });*/
        btnAyuda = (ImageButton) findViewById(R.id.btnAyuda);

        btnAyuda.setEnabled(true);
        btnAyuda.setClickable(true);
        btnAyuda.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {//por incluir el bloque del mensaje
                Log.e("CLICK", "BOTON PRESIONADO");
                VerMensajeriaEnvio();
            }
        });
    }

    private void iniciarPanelComunicaciones() {

        Bundle bundle = new Bundle();
        bundle.putString("NIVEL_OPERADOR", nivelOperador);
        bundle.putString("CODIGO_INTERNO_PDA", aforador.trim());
        Intent comunicaciones = new Intent(this, Comunicaciones.class);
        comunicaciones.putExtras(bundle);
        startActivity(comunicaciones);
    }
    private void VerMensajeriaEnvio() {
        String mensaje2 = "";
        try {
            //final String mensaje1;

                // ✅ Mensaje definido correctamente
                String mensaje1 =      "📡 IMPORTANTE VERIFIAR ESTADO DE ENVÍO DE INFORMACIÓN\n\n" +

                        "• Estado: Conexion " + ("✔ Información enviada correctamente O ❌ Pendiente de envío") + "\n" +
                        "• Registros pendientes: SON VERIFICABLES EN EL RESUMEN" + "\n\n" +

                        "ℹ️ RECOMENDACIONES:\n" +


                                "- Su información debe ser transmitida al servidor.\n" +
                                        "- Puede continuar con nuevas rutas lecturas sin inconvenientes.\n"+

                                "- Verifique su conexión a internet.\n" +
                                        "- Diríjase al menú de 'Envío de datos'.\n" +
                                        "- Asegúrese de enviar las fotografías pendientes.\n" +

                        "\n📌 SOPORTE:\n" +
                        "En caso de inconsistencias, comuníquese con su supervisor o el área técnica.\n" +
                        "Sistema SIM-FA - Gestión de Facturacion Móvil.";


                new AlertDialog.Builder(this)
                        .setTitle("Información del Sistema")
                        .setMessage(mensaje1)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setPositiveButton("ENTENDIDO", (d, w) -> d.dismiss())
                        .show();

             // ✅ cierre correcto del lambda
        }catch (Exception ex)
        {
            mensaje2 =
                    "📡 ESTADO DE ENVÍO\n\n" +
                            "✔ Información sincronizada con el servidor.\n" +
                            "📅 Última verificación: " +  "Hay un error de reconexion"+ "\n\n" +
                            "ℹ️ El sistema está listo para continuar operaciones.";//obtenerFechaActual()
            new AlertDialog.Builder(this)
                    .setTitle("Información del Sistema")
                    .setMessage(mensaje2)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setPositiveButton("ENTENDIDO", (d, w) -> d.dismiss())
                    .show();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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
                comunicaciones();
                return true;

            case KeyEvent.KEYCODE_B:
                bluetooth();
                return true;

            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    private void bluetooth() {

        Toast.makeText(getApplicationContext(), "Caracteristica no implementada..", Toast.LENGTH_LONG).show();
    }

    private void comunicaciones() {

        Toast.makeText(getApplicationContext(), "Caracteristica no implementada..", Toast.LENGTH_LONG).show();
    }

    private void retornar() {

        this.finish();
    }

    void ejecutarBorradoDeArchivos() {

      /*anterior  File file = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/ENVIOSGPRS.SDA");
        if (file.exists()) {
            mensajesAlert("Alerta de eliminacion", "ULTIMA CONFIRMACION, EXISTEN REGISTROS\nDE LECTURAS TERMINADAS \nVERIFIQUE QUE SE HAN ENVIADO. \n" + "DESEA REGRESAR?");
        } else {
            mensajesAlert("Alerta de eliminacion", "ULTIMA CONFIRMACION, ¿DESEA ELIMINAR LOS ARCHIVOS?" );
        }*/

        if (nivelOperador.trim().equals("A")) {
            validacionesElimina();
        } else {
            validarEnvio();
        }
    }

    private void utilidades() {
        try {

            Intent myIntent = new Intent(MenuPrincipal.this, FechayHora.class);
            MenuPrincipal.this.startActivity(myIntent);
//            if (nivelOperador.equals("S") || nivelOperador.equals("A")) {
//                Intent intent = new Intent();
//                intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.DateTimeSettingsSetupWizard"));
//                startActivity(intent);
            //  }
        } catch (Exception ex) {
            //ex.getMessage();
        }
    }

    public void ejecutarBorradoArchivos2() {
        try {

            copyFilestoBackUp();
             String EjecutaZip= GeneraUnZIp();
             if (!EjecutaZip.equals("OK"))
             {
                 Toast.makeText(getApplicationContext(), "NO SE PUEDE BORRAR HASTA NO \nPROCESAR EL ZIP DE RESPALDO...!! "+EjecutaZip , Toast.LENGTH_LONG).show();
                return;
             }

            File sourceDir = new File(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/");

            String[] txtList = sourceDir.list();
            File f;

            for (String sf : txtList) { //todo: Aqui no se borra el directorio BACKUP

                f = new File(sourceDir.getAbsolutePath() + "/" + sf);
                f.getAbsoluteFile().delete();
            }

            sourceDir = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/");

            String[] txtList2 = sourceDir.list();
            File g;

            for (String sf : txtList2) { //todo: Aqui no se borra el directorio BACKUP

                g = new File(sourceDir.getAbsolutePath() + "/" + sf);
                g.getAbsoluteFile().delete();
            }

            sourceDir = new File(VariablesGlobales.directorioactual + "/DCIM/FOTOGRAFIASL");

            String[] txtList3 = sourceDir.list();

            for (String sf : txtList3) { //todo: Aqui no se borra el directorio BACKUP

                f = new File(sourceDir.getAbsolutePath() + "/" + sf);
                f.getAbsoluteFile().delete();
            }
            Toast.makeText(getApplicationContext(), "ARCHIVOS BORRADOS \nEN SU TOTALIDAD", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "ERROR EN EL BORRADO... REINICIE E INTENTE DE NUEVO", Toast.LENGTH_LONG).show();
        }
    }
    public void copyFilestoBackUp(){
        try{
            //----------------------------------------------------------------------------------------------------------------------------- Borra BackUps Viejos
            File folderBackups = new File(VariablesGlobales.directorioBackUp);
            String[] archivosback = folderBackups.list();
            Calendar calendar = Calendar.getInstance();
            Date lastModificated = null;
            File childBackup = null;

            int anobck = 0,mesbck = 0,valmes = 0,valano = 0;
            int mesSis = calendar.get(Calendar.MONTH) + 1;
            int anoSis = calendar.get(Calendar.YEAR);

            for (int x = 0; x < folderBackups.listFiles().length; x++){
                childBackup = new File(VariablesGlobales.directorioBackUp + "/" +archivosback[x]);
                lastModificated = new Date(childBackup.lastModified());
                DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                anobck = Integer.parseInt(dateFormat.format(lastModificated).substring(0,4));
                mesbck = Integer.parseInt(dateFormat.format(lastModificated).substring(4,6));
                if (mesbck > mesSis){
                    valmes = mesbck - mesSis;
                }else{
                    valmes = mesSis - mesbck;
                }
                if (anobck > anoSis){
                    valano = anobck - anoSis;
                }else{
                    valano = anoSis - anobck;
                }
                if ((valmes != 10 && valmes != 0 && valmes != 1 && valano == 1) || (valmes != 2 && valmes != 0 && valmes != 1 && valano == 0)){
                    Log.e("INFO","Entra a borrar ciclos mayores a 2 meses");
                    FileUtils.deleteDirectory(childBackup);
                }
            }
            //-----------------------------------------------------------------------------------------------------------------------------
            String carpetas = Ciclo + Municipio + Seccion + Division;
            File filedirectory = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA");
            String[] archivos = filedirectory.list();
            int count = 0;
            for (int i = 0; i < filedirectory.listFiles().length; i++){
                FileReader stream3 = new FileReader(VariablesGlobales.directorioactual + "/DATOSDESALIDA/" + archivos[i]);
                BufferedReader reader = new BufferedReader(stream3);
                String linea = "";
                String dato;
                int cont = 0;
                Log.e("INFO","directoriobk: " + VariablesGlobales.directorioBackUp  + carpetas + "/" + archivos[i]);
                File copyFile = new File(VariablesGlobales.directorioBackUp  + carpetas + "/" + archivos[i]);
                if (!copyFile.exists()){
                    copyFile.createNewFile();
                }
                OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(copyFile));
                while ((linea = reader.readLine()) != null) {
                    dato = linea + "\r\n";
                    cont++;
                    osw.write(dato);
                }
                osw.flush();
                osw.close();
                reader.close();
            }
        }catch (Exception e){
            Log.e("ERROR","[copyFilestoBackUp]Error|:" + e);
            utils.Log(logfile,"[copyFilestoBackUp]Error|:" + e);
        }
    }
    private String GeneraUnZIp_old(){
        //nuevo para hacer un backup de todo si se nos quiere borrar sin la precaucion de haber enviado al servidor
        String RutaAGrabarX = VariablesGlobales.directorioactual + "/DATOSDESALIDA/";
        String RutaAGrabar = VariablesGlobales.directorioactual + "/DCIM/SIMFABACKUP/";
        String NombreZip = Ciclo+"D" + Municipio + Seccion + Division + ".ZIP";
        File Directorio = new File(RutaAGrabar);

        if (Directorio != null && Directorio.isDirectory()) {
            File[] archivos = Directorio.listFiles();
            if (archivos != null) {
                for (File archivo : archivos) {
                    if (archivo.isFile() && archivo.getName().endsWith(".ZIP")) {
                        archivo.delete(); // elimina el archivo ZIP
                    }
                }
            }
        }

        File nombreDeArchivo = new File(RutaAGrabar+NombreZip);

        if (nombreDeArchivo.exists())
            nombreDeArchivo.delete();

        ArrayList filestoZip = new ArrayList();
        File f = new File(RutaAGrabarX);//Ax: ruta donde buscar los archivos
        File[] files = f.listFiles();//array de Files de la carpeta
        //validar
        for (int i = 0; i < files.length; i++) {

            File file = files[i];
            //Log.e("error","nombre archivo "+file.getName());

            //if (!file.isDirectory() && file.getName().toUpperCase().endsWith(".SDA") && !file.getName().toUpperCase().equals("ENVIOGPRS" + serialPDA + ".SDA") || file.getName().equals("TOMOLECTURA.TXT") || file.getName().equals("LOGEVENTOS.LOG")) {
                filestoZip.add(file);
           // }
        }

        String comprimir = util.CreaZip(RutaAGrabar + NombreZip, filestoZip);
        Log.e("error","respuesta comprime "+comprimir);
        if (!comprimir.contains("✓")) {
           // mensajesAlert("Alerta de eliminacion", " ERROR AL COMPRIMIR\n Y GUARDAR UNA COPIA DE RESPALDO", 5);
            return "Error Comprimiendo \n Archivos por enviar";
        }
        return "OK";
    }


    private String GeneraUnZIp() {

        // Nuevo para hacer un backup de todo si se nos quiere borrar
        // sin la precaución de haber enviado al servidor.

        Calendar calenda = Calendar.getInstance();
        String dia = String.format("%02d", calenda.get(Calendar.DAY_OF_MONTH));

        String RutaAGrabarX = VariablesGlobales.directorioactual + "/DATOSDESALIDA/";
        String RutaAGrabar = VariablesGlobales.directorioactual + "/DCIM/SIMFABACKUP/";
        String NombreZip = "COPIACOMPLETASIMFA_"+Municipio + Seccion + Division + "_" + dia + ".ZIP";

        File DirectorioBackup = new File(RutaAGrabar);
        if (!DirectorioBackup.exists()) {
            DirectorioBackup.mkdirs();
        }

        //===========================================================
        // Validar que existan suficientes archivos para respaldar.
        // Si hay menos de 3 archivos NO se genera un nuevo respaldo
        // y se conserva el existente.
        //===========================================================

        File DirectorioLecturas = new File(RutaAGrabarX);
        File[] files = DirectorioLecturas.listFiles();

        if (files == null || files.length < 3) {
            Log.e("BACKUP", "No se genera backup. Solo existen "
                    + (files == null ? 0 : files.length) + " archivos.");

            return "No se genera backup. Menos de 3 archivos.";
        }

        //===========================================================
        // Construir la lista de archivos a comprimir
        //===========================================================

        ArrayList<File> filestoZip = new ArrayList<>();

        for (File file : files) {

            if (file.isFile()) {
                filestoZip.add(file);
            }

        }

        //===========================================================
        // Eliminar únicamente el backup del día actual.
        // Los demás respaldos permanecen.
        //===========================================================

        File nombreDeArchivo = new File(RutaAGrabar + NombreZip);

        if (nombreDeArchivo.exists()) {
            nombreDeArchivo.delete();
        }

        //===========================================================
        // Crear el ZIP
        //===========================================================

        String comprimir = utils.CreaZip(
                RutaAGrabar + NombreZip,
                filestoZip);

        Log.e("BACKUP", "Respuesta comprimir: " + comprimir);

        if (!comprimir.contains("✓")) {

            return "Error comprimiendo archivos de respaldo.";

        }

        return "OK";
    }



    private void mensajesAlert(String titulo, String msg, final int opc) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MenuPrincipal.this);

        builder.setTitle(titulo);
        builder.setMessage(msg);
        builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (opc == 1) {
                    dialog.dismiss();
                }
                if (opc == 2) {
                    dialog.dismiss();
                }
                if (opc == 3) {
                    mensajesAlert("Alerta de eliminacion", "CONFIRMA QUE BORRARA LOS ARCHIVOS", 4);
                    dialog.dismiss();
                }



                if (opc == 4) {
                    mensajesAlert("Alerta de eliminacion", " RE-confirma\n que borrara los archivos", 5);
                    dialog.dismiss();
                }
                if (opc == 5) {
                    ejecutarBorradoArchivos2();
                    dialog.dismiss();
                }
                if (opc == 6) {
                    dialog.dismiss();
                }
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (opc == 1) {
                    mensajesAlert("Alerta de eliminacion", "LE CONFIRMO QUE SU\n TERMINAL TIENE\n REGISTROS\n SIN ENVIAR DEBE\n RETORNAR\n A LECTURAS Y ENVIARLOS", 2);
                    dialog.dismiss();
                }
                if (opc == 2) {
                    if (VariablesGlobales.totalprediosnofacturados > 0) {
                        mensajesAlert("Alerta de eliminacion", "RUTA NO SE HA LEIDO EN SU TOTALIDAD\n desea borrar la informacion? ", 3);
                    } else {
                        mensajesAlert("Alerta de eliminacion", "CONFIRMA QUE BORRARA LOS ARCHIVOS", 4);
                    }
                    dialog.dismiss();
                }
                if (opc == 3) {
                    dialog.dismiss();
                }
                if (opc == 4) {
                    dialog.dismiss();
                }
                if (opc == 5) {
                    dialog.dismiss();
                }
                if (opc == 6) {
                    dialog.dismiss();
                }
                ///do
            }
        });

        builder.create().show();
    }

    private void procesoLecturas() {

        // if (nivelOperador.equals("A")) {
        //   Toast.makeText(getApplicationContext(), "Proceso no lo puede ejecutar el Administrador", Toast.LENGTH_LONG).show();
        //   return;
        //}
        Calendar cal = new GregorianCalendar();
        Date date = cal.getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        String fecha = df.format(date);

        if (Integer.parseInt(fecha.trim()) < 20151211) {

            Toast.makeText(this, "Fecha Desactualizada ..ir a Utilidades: " + fecha.trim() + "\nFecha Inicial a Leer: " + VariablesGlobales.fechainicial,
                    Toast.LENGTH_SHORT).show();
            // Todo: utilizar servicio de ajuste de fecha y hora del sistema,
            // validar creacion
            return;
        }
        File fileNombre = new File(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/NOMBRE");
        if (!fileNombre.exists()) {

            Toast.makeText(getApplicationContext(), "Dispositivo no tiene Informacion de Trabajo... Cargar Informacion", Toast.LENGTH_LONG).show();
            return;
        } else {
            inicializaProcesoLecturas();
            return;
        }
    }


    private void inicializaProcesoLecturas() {

        String archivo1 = "";
        String archivo2 = "";

        File fileLeido = new File(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/LEIDO.TXT");
        if (fileLeido.exists()) {
            Toast.makeText(getApplicationContext(), "Proceso ya Esta Leido..\nDescargue... la informacion para su Backup", Toast.LENGTH_LONG).show();
            // return;
        }

        File fileDescarga = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/DESCARGA");
        if (fileDescarga.exists()) {
            Toast.makeText(getApplicationContext(), "Ruta ya Esta Descargada Borre datos y Cargue una nueva ruta", Toast.LENGTH_LONG).show();
            // return;
        }

        File archivo_1 = new File(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/NOMBRE");
        String archivocargado = "";
        byte[] byteArray;
        if (archivo_1.exists()) {
            try {

                RandomAccessFile rFile = new RandomAccessFile(archivo_1, "rw");
                int fileSize = (int) rFile.length();
                byteArray = new byte[fileSize];
                rFile.readFully(byteArray, 0, fileSize);
                archivocargado = new String(byteArray);
              //  txtArchivoCargado.setText("Ciclo: " +archivocargado.substring(0, 4)+" Archivo: " + archivocargado.substring(4, 12));

                txtRuta.setText("" + archivocargado.substring(4, 12));
                txtPrg.setText("CIC"+archivocargado.substring(0,4));
                rFile.close();

            } catch (FileNotFoundException e) {

                e.printStackTrace();

            } catch (IOException e) {

                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Dispositivo sin Informacion", Toast.LENGTH_LONG).show();
            return;
        }

        variables.setNombrepredio(archivo2);

        try {
            Bundle bundle2 = new Bundle();
            bundle2.putString("aforador", aforador);
            bundle2.putString("directorioActual",
                    VariablesGlobales.directorioactual);
            bundle2.putString("nombrePredio", variables.getNombrepredio());
            bundle2.putString("terminal", terminal);

            bundle2.putString("nivelOperador", nivelOperador);
            //Ax: Abre el modulo y se identifica con el ID (ANOMALIA_REQUEST_CODE), que mas adelante se recupera valor en 'onActivityResult'
            Intent intent = new Intent(MenuPrincipal.this, ResumenEstadistico.class);
            // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtras(bundle2);
            startActivityForResult(intent, RESUMEN_REQUEST_CODE);

        } catch (Exception e) {
            // Toast.makeText(getApplicationContext(), "Error al iniciar actividad de Resumen Estadistico" + e.getMessage(),
            //         Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Ax: se ejecuta cuando concluye el intent ResumenEstadistico y va al codigo RESUMEN_REQUEST_CODE
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESUMEN_REQUEST_CODE) {

            if (resultCode == RESULT_OK) {

                String filaid = data.getStringExtra("fila");
                Log.e("TAG FILAID","Dato fila: "+filaid);
                CrudifNull[] ced = bdl.obtnerdatos();
                for (int i = 0; i < ced.length; i++){
                    Log.d("Resultados",String.valueOf(ced[i].getId()));
                }
                if (ced.length > 0){
                    CrudifNull cedbyid = bdl.obtenerdatabyid(1);
                    bdl.actualizarData(cedbyid,filaid,aforador,"001",VariablesGlobales.directorioactual,nivelOperador);
                }else{
                    bdl.guardarDatos(1,terminal,filaid,aforador,"001",VariablesGlobales.directorioactual,nivelOperador);
                    //crudCedula.setCedula(Integer.parseInt(input.getText().toString()));
                    Log.d("Insert","guaradado");
                }

                Bundle bundle = new Bundle();
                bundle.putString("fila", filaid);
                bundle.putString("operario", aforador);
                bundle.putString("terminal", terminal);
                bundle.putString("impresora", "001");
                bundle.putString("path", VariablesGlobales.directorioactual);
                bundle.putString("nivelOperador", nivelOperador);

                Intent i = new Intent(this, MenuDeLiquidacion.class);
                // i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtras(bundle);
                startActivity(i);
            } else {
                Toast.makeText(getApplicationContext(), "Ha salido de Resumen Estadistico", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == TAKE_PICTURE) {
            if (resultCode == RESULT_OK) {

                if (data != null) {

                    if (data.hasExtra("output")) {
                        name = data.getParcelableExtra("output");
                    }
                }
                new MediaScannerConnection.MediaScannerConnectionClient() {
                    private MediaScannerConnection msc = null;

                    {
                        msc = new MediaScannerConnection(getApplicationContext(), this);
                        msc.connect();
                    }

                    public void onMediaScannerConnected() {
                        msc.scanFile(name, null);
                    }

                    public void onScanCompleted(String path, Uri uri) {
                        //Log.d("heightDiff C", "uri:" + uri + " ,path: " + path);
                        msc.disconnect();
                    }
                };

                // vecesImagen = 0;
                ejecutarProcesoDeFotoII(vecesImagen);
                if (banderaNovedad) {
                    if (contNov == 2) {
                        contNov = 1;
                        Calendar calendar = Calendar.getInstance();
                        ejecutarProcesoDeFoto("999999999" + "_" + calendar.get(Calendar.MONTH), 1, tipoFotoDigital, 0, "", 1);
                    } else {
                        envioHilos_CertPostal_Censo_Aforos_Novedades(true, true, false, false);
                    }
                }
                if (banderaCertificado) {
                    if (contCert == 2) {
                        contCert = 1;
                        Calendar calendar = Calendar.getInstance();
                        ejecutarProcesoDeFoto("999999999" + "_" + calendar.get(Calendar.MONTH), 1, tipoFotoDigital, 0, "", 1);
                    } else {
                        envioHilos_CertPostal_Censo_Aforos_Novedades(false, false, true, false);
                    }
                }

            } else {
                ejecutarProcesoDeFotoII(vecesImagen);
            }
        } else if (requestCode == ADICIONARNOVDAD_REQUEST_CODE) {

            if (resultCode == RESULT_OK && data != null) {

                try {
                    mensajeNovedad = "";
                    banderaNovedad = true;
                    contNov++;
                    Calendar calendar = Calendar.getInstance();
                    ejecutarProcesoDeFoto("999999999" + "_" + calendar.get(Calendar.MONTH), 1, tipoFotoDigital, 0, "", 1);


                } catch (Exception ex) {
                    utils.Log(logfile, "[MenuDeLiquidacion]ADICIONARNOVDAD_REQUEST_CODE. " + ex.toString());
                }
            }

            VariablesGlobales.opcionmenuseleccion = 0;//**-*


        } else if (requestCode == ADICINCERTIFCAD_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {

                try {
                    banderaCertificado = true;
                    contCert++;
                    Calendar calendar = Calendar.getInstance();
                    ejecutarProcesoDeFoto("999999999" + "_" + calendar.get(Calendar.MONTH), 1, tipoFotoDigital, 0, "", 1);

                } catch (Exception ex) {
                    utils.Log(logfile, "[MenuDeLiquidacion]ADICINCERTIFCAD_REQUEST_CODE. " + ex.toString());
                }
            }
        }

    }

    private void ejecutarProcesoDeFoto(String nombre, int guardainforme, int D2Digital, int directorio, String tipoProceso, int veces) {

        vecesImagen = veces;

        String directorioF = "/DCIM/FOTOGRAFIASL";
        int cont = 1;
        try {
            String existeFoto = "";
            String nombreImagen = "";
            String dirTrabajo = VariablesGlobales.directorioactual;

            File file = new File("/sdcard");

            if (file.exists())
                dirTrabajo = "/sdcard";

            Calendar calendar = Calendar.getInstance();
            File foto;

            try {

                existeFoto = dirTrabajo + directorioF + nombre + calendar.get(Calendar.YEAR) + "_" + cont + "" + tipoProceso;
                foto = new File(existeFoto.trim() + ".jpg");

                if (foto.exists()) {
                    while (foto.exists()) {
                        cont++;

                        existeFoto = dirTrabajo + directorioF + nombre + calendar.get(Calendar.YEAR) + "_" + cont + "" + tipoProceso;
                        foto = new File(existeFoto.trim() + ".jpg");

                        if (file.exists()) {

                            nombreImagen = nombre + calendar.get(Calendar.YEAR) + "_" + cont + "" + tipoProceso;
                        } else {

                            nombreImagen = nombre + calendar.get(Calendar.YEAR) + "_" + cont + "" + tipoProceso;
                        }
                    }
                } else {

                    nombreImagen = nombre + calendar.get(Calendar.YEAR) + "_" + cont + "" + tipoProceso;
                }


                String nombreImagen2 = nombreImagen;
                nombreImagen = dirTrabajo + directorioF + nombreImagen + ".jpg";

                String dirEjecutable = VariablesGlobales.directorioactual;

                int salir = 0;
                File nombreDeArchivo = new File(dirEjecutable + "/ImagenACapturar.txt");

                if (nombreDeArchivo.exists())
                    nombreDeArchivo.delete();

                if (D2Digital == 0) {

                    if (!nombreDeArchivo.exists()) {
                        RandomAccessFile writer2 = new RandomAccessFile(nombreDeArchivo, "rw");
                        writer2.writeBytes(nombreImagen.trim() + ".jpg");
                        writer2.close();
                    }

                    File archivosCarga = new File(dirEjecutable + "/ArchivosCarga.cfi");
                    if (archivosCarga.exists()) {

                        archivosCarga = new File("/IPSM/ArchivosCarga.cfi");//todo backup cambiar

                        if (archivosCarga.exists())

                            archivosCarga.delete();

                        VariablesGlobales.copyFile(dirEjecutable + "/ArchivosCarga.cfi", "/IPSM/ArchivosCarga.cfi", true);//todo backup cambiar
                    }

                    nombredelaImagen = nombreImagen.trim();
                    capRegistroActual = VariablesGlobales.registroactual;
                    ejecutarProcesoDeFotoII(veces);

                } else {
                    Toast.makeText(getApplicationContext(), "Proceso a color esta en desarrollo", Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "PROBLEMA EN ESCRIBIR \nLA FOTOGRAFIA... " + e, Toast.LENGTH_LONG).show();

            }

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "ERROR.. Proceso Modulo de Fotografias para TPL", Toast.LENGTH_LONG).show();
        }

    }

    private void ejecutarProcesoDeFotoII(int veces) {
        try {

            if (veces > 0) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                int code = TAKE_PICTURE;

                Uri output = Uri.fromFile(new File(nombredelaImagen));
                intent.putExtra(MediaStore.EXTRA_OUTPUT, output);
                vecesImagen--;
                startActivityForResult(intent, code);
                nombrefoto = output.getPath();

            } else {
                vecesImagen = 0;
                nombredelaImagen = "";
            }
            try {

                String mensajefoto = utils.ReduceImagen2(nombrefoto, fecha + " c: " + lactitud + ' ' + longitud);

                if (!mensajefoto.trim().equals("")) {
                    utils.Log(logfile, "[MenuDeLiquidacion]ejecutarProcesoDeFotoII();" + mensajefoto);
                }

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "ERROR: Proceso Modulo de Fotografias II", Toast.LENGTH_LONG).show();

            } finally {

            }

            // }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "ERROR: Proceso Modulo de Fotografias II", Toast.LENGTH_LONG).show();

        }
    }

    public void muestraPosicionActual(Location loc) {

        if (loc == null) {
            longitud = "0.0";
            lactitud = "0.0";
        } else {
            lactitud = String.valueOf(loc.getLatitude());
            longitud = String.valueOf(loc.getLongitude());

        }
    }

    public void envioHilos_CertPostal_Censo_Aforos_Novedades(boolean aforos, boolean novedades, boolean certificado, boolean censo) {

        Archivos_CPCAN = new String[]{"", "", "", ""};
        int cont = 0;

        if (aforos) {
            String in = VariablesGlobales.directorioactual + "/DATOSDESALIDA/AFOROS.SDA";
            String out = VariablesGlobales.directorioactual + "/DATOSDESALIDA/AFOROS" + terminal + ".SDA";

            if (Enviar_CertPostal_Censo_Aforos_Novedades_1(in, out, 98, 99)) {
                Archivos_CPCAN[0] = out;
                cont++;
            }
        }

        if (novedades) {

            String in = VariablesGlobales.directorioactual + "/DATOSDESALIDA/NOVEDADES.SDA";
            String out = VariablesGlobales.directorioactual + "/DATOSDESALIDA/NOVEDADES" + terminal + ".SDA";
            if (Enviar_CertPostal_Censo_Aforos_Novedades_1(in, out, 180, 181)) {
                Archivos_CPCAN[1] = out;
                cont++;
            }
        }

        if (certificado) {
            String in = VariablesGlobales.directorioactual + "/DATOSDESALIDA/CERTIFICADOS.SDA";
            String out = VariablesGlobales.directorioactual + "/DATOSDESALIDA/CERTIFICADOS" + terminal + ".SDA";
            if (Enviar_CertPostal_Censo_Aforos_Novedades_1(in, out, 234, 236)) {
                Archivos_CPCAN[2] = out;
                cont++;
            }
        }

        if (cont < 1) {

            if (procesandoenvioenHilos_CPCAN == 1) {
                Toast.makeText(getApplicationContext(), "No hay archivos por enviar, envio ocupado", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "No hay archivos por enviar", Toast.LENGTH_LONG).show();
            }
            Archivos_CPCAN = null;
            return;
        }

        procesandoenvioenHilos_CPCAN = 1;
        metodo = "ENVIOCERTPOSTLCENSOAFORONOVEDADS";
        AsyncCallWS task = new AsyncCallWS();
        task.execute("");
    }

    private boolean Enviar_CertPostal_Censo_Aforos_Novedades_1(String ArchivoEntrada, String ArchivoEnviado, int desde, int hasta) {

        try {

            Generica gen = new Generica();
            gen.setOriginalFile(ArchivoEntrada);
            gen.setNuevoFile(ArchivoEnviado);
            gen.desde = desde;
            gen.hasta = hasta;
            String msg = "";

            if (ArchivoEntrada.toLowerCase().contains("aforos") || ArchivoEntrada.toLowerCase().contains("novedades")) {
                msg = gen.WriteFileByPos("X", "_", true);
            } else {
                msg = gen.WriteFileByPos("NO", "EE", true);
            }

            if (msg == null) return false;

            if (msg.equals("1")) {
                return true;
            } else {

                if (gen.nuevoFile.length() < 4) {
                    gen.nuevoFile.delete();
                }

                if (msg != "") {
                    utils.Log(logfile, "Falla en la clase generica " + msg);
                }
                procesandoenvioenHilos_CPCAN = 0;
            }

        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "Problemas al crear archivo" + ArchivoEnviado, Toast.LENGTH_LONG).show();
            utils.Log(logfile, "Enviar_CertPostal_Censo_Aforos_Novedades_1; " + ex.getMessage());
            procesandoenvioenHilos_CPCAN = 0;
        }
        return false;
    }

    private String Enviar_CertPostal_Censo_Aforos_Novedades_2() {

        try {
            String ciclo0 = String.format("%1$4s", Ciclo).replace(" ", "0");
            ArrayList filestoZip = new ArrayList();

            for (String doc : Archivos_CPCAN) {

                if (doc != "") {
                    File fdoc = new File(doc);
                    filestoZip.add(fdoc);
                }
            }

            File nombreZipPorCrear;
            nombreZipPorCrear = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/" + "Tpl" + (Municipio + Seccion + Division) + ".zip");

            String resp = EnviarArchivo((rutaAdministrador + "CIC" + ciclo0), nombreZipPorCrear, filestoZip, trama, false);

            nombreZipPorCrear.delete();

            for (String doc : Archivos_CPCAN) {
                if (doc != "") {
                    File fdoc = new File(doc);
                    fdoc.delete();
                }
            }

            if (!resp.equals("ok")) {
                utils.Log(logfile, "[Enviar_CertPostal_Censo_Aforos_Novedades_2].:" + resp);
            }

            return resp;

        } catch (Exception ex) {
            utils.Log(logfile, "Enviar_CertPostal_Censo_Aforos_Novedades_2; " + ex.getMessage());
            procesandoenvioenHilos_CPCAN = 0;
            Archivos_CPCAN = null;
        }
        return "";
    }

    public String EnviarArchivo(String rutaAGrabarTraer, File nombreZipPorCrear, ArrayList ArchivosAEnviar, int trama, boolean esEnvioCobro) {
        String respuestaWeb = "";

        respuestaWeb = utils.CreaZip(nombreZipPorCrear.getAbsolutePath(), ArchivosAEnviar);

        if (!respuestaWeb.contains("✓")) return (respuestaWeb);

        if (nombreZipPorCrear.length() <= 0) return "Error comprimiendo archivo";

        try {
            WSSoap wsoaps = new WSSoap(URL, paginaWs);

            try {
                respuestaWeb = wsoaps.VerificarSiexisteDirectorioArchivo("VerificarSiexiste_Directorio_Archivo", rutaAGrabarTraer, nombreZipPorCrear.getName());
                Log.e("respu1", "zzzz " + respuestaWeb + " " + rutaAGrabarTraer + " " + nombreZipPorCrear.getName());
                if (!respuestaWeb.equals("true"))
                    return "Sin Conexion o Sin directorio \nde trabajo:\n" + rutaAGrabarTraer + " " + respuestaWeb;

            } catch (Exception ex) {
                return "Sin Conexion o Sin directorio \nde trabajo.:\n" + rutaAGrabarTraer + " " + respuestaWeb;
            }

            respuestaWeb = wsoaps.TerminalToServerReceive("Terminal_ToServerReceive", rutaAGrabarTraer, parseStringToInteger(esComprimido), nombreZipPorCrear.getAbsolutePath(), trama);

            if (!(respuestaWeb.equals("4") || respuestaWeb.equals("1")))
                return "Error Enviar Archivo Terminal_ToServerReceive: " + respuestaWeb;

            String serial = String.format("%1$15s", terminal).replace(" ", "0");

            respuestaWeb = wsoaps.DirectorioDescomprimir("ProcesarDescomprimidoPC", "Tpl" + serial + rutaAGrabarTraer + "\\" + "DescargasEnvioGPRS", rutaAGrabarTraer + "\\" + nombreZipPorCrear.getName(), trama);

            if (!respuestaWeb.equals("true")) {
                return "Error Directorio Donde Descomprimir " + respuestaWeb;
            }

            respuestaWeb = (rutaAGrabarTraer.substring(rutaAGrabarTraer.lastIndexOf("\\") + 1, rutaAGrabarTraer.length())) + "";
            rutaAGrabarTraer = rutaAGrabarTraer.replace(respuestaWeb, "");

            respuestaWeb = wsoaps.InsertarDatosTablasTemporales("InsertarDatosTemporales", rutaAGrabarTraer.toLowerCase(), respuestaWeb + "\\DescargasEnvioGPRS", terminal, "1");

            respuestaWeb = respuestaWS(respuestaWeb, esEnvioCobro, ArchivosAEnviar);

            if (!respuestaWeb.equals("")) {
                utils.Log(logfile, "[MenuDeLiquidacion]EnviarArchivo()Respuesta WS con Errores: " + respuestaWeb);
            }

        } catch (Exception ex) {
            utils.Log(logfile, "[MenuDeLiquidacion]EnviarArchivo().;" + ex.getMessage());
            return "Problemas EnviarArchivo()" + ex.getMessage();
        }
        return "ok";
    }

    private boolean seleccionUrl() {
        try {
            String nombreArchivo = VariablesGlobales.directorioactual + "/DIRECCIONEIP.TXT";
            File file = new File(nombreArchivo);

            if (!file.exists()) return false;

            FileReader stream3 = new FileReader(nombreArchivo);
            BufferedReader reader = new BufferedReader(stream3);
            String linea = "";
            String datoIP;
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

            reader.close();

            if (cont < 1) return false;

            URL = URL.replace(" ", "");

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Error, verificar Configuracion IP", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    private String respuestaWS(String response, boolean esEnvioCobro, List<File> ArchivosxEnviar) {

        try {
            if (!response.contains("|")) {
                return "No hubo respuesta del WebService";
            }
            String mensaje = "";
            String[] responseWS = response.split("|");

            if (!esEnvioCobro) {

                if (ArchivosxEnviar == null) {
                    return "_";
                }

                int aforos = 0;
                int novedades = 0;
                int certificado = 0;
                int auditoriasupervisor = 0;

                for (File f : ArchivosxEnviar) {

                    String doc = f.getName();

                    if (doc == "") continue;

                    if (doc.toLowerCase().contains("aforos")) {
                        aforos++;
                    } else if (doc.toLowerCase().contains("novedades")) {
                        novedades++;
                    } else if (doc.toLowerCase().contains("certificado")) {
                        certificado++;
                    } else if (doc.toLowerCase().contains("auditoriasupervisor")) {
                        auditoriasupervisor++;
                    } else {
                        continue;
                    }
                }

                for (String res : responseWS) {

                    if (res.toLowerCase().contains("novedades")) {
                        if (res.toLowerCase().contains("noexiste")) {
                            if (novedades > 0) {
                                mensaje += "Novedades(inconsistencia): Server = 0 App = " + novedades + "; ";
                            }
                        }

                        if (res.toLowerCase().contains(";")) {
                            String[] temp = res.split(";");
                            if (!temp[2].equals("0")) {
                                mensaje += "Novedades(Insercion): de " + temp[1] + " fallan " + temp[2] + ", App " + novedades + ";";
                            }
                        }
                    } else if (res.toLowerCase().contains("aforos")) {
                        if (res.toLowerCase().contains("noexiste")) {
                            if (aforos > 0) {
                                mensaje += "Aforos(inconsistencia): Server = 0 App = " + aforos + "; ";
                            }
                        }

                        if (res.toLowerCase().contains(";")) {
                            String[] temp = res.split(";");
                            if (!temp[2].equals("0")) {
                                mensaje += "Aforos(Insercion): de " + temp[1] + " fallan " + temp[2] + ", App " + aforos + ";";
                            }
                        }
                    } else if (res.toLowerCase().contains("censosupervisor")) {

                        if (res.toLowerCase().contains("noexiste")) {
                            if (auditoriasupervisor > 0) {
                                mensaje += "Censosupervisor(inconsistencia): Server = 0 App = " + auditoriasupervisor + "; ";
                            }
                        }

                        if (res.toLowerCase().contains(";")) {
                            String[] temp = res.split(";");
                            if (!temp[2].equals("0")) {
                                mensaje += "Censosupervisor(Insercion): de " + temp[1] + " fallan " + temp[2] + ", App " + auditoriasupervisor + ";";
                            }
                        }
                    } else if (res.toLowerCase().contains("certificacion")) {

                        if (res.toLowerCase().contains("noexiste")) {
                            if (certificado > 0) {
                                mensaje += "Certificacion(inconsistencia): Server = 0 App = " + certificado + "; ";
                            }
                        }

                        if (res.toLowerCase().contains(";")) {
                            String[] temp = res.split(";");
                            if (!temp[2].equals("0")) {
                                mensaje += "Certificacion(Insercion): de " + temp[1] + " fallan " + temp[2] + ", App " + certificado + ";";
                            }
                        }
                    } else {
                        continue;
                    }
                }
                return mensaje;

            } else {
                int envio = 0;
                int cobros = 0;

                for (File f : ArchivosxEnviar) {

                    String doc = f.getName();

                    if (doc == "") continue;

                    if (doc.toLowerCase().contains("envio")) {
                        envio++;
                    } else if (doc.toLowerCase().contains("cobros")) {
                        cobros++;
                    } else {
                        continue;
                    }
                }


                for (String res : responseWS) {

                    if (res.toLowerCase().contains("envio")) {
                        if (res.toLowerCase().contains("noexiste")) {
                            if (envio > 0) {
                                mensaje += "Novedades(inconsistencia): Server = 0 App = " + envio + "; ";
                            }
                        }

                        if (res.toLowerCase().contains(";")) {
                            String[] temp = res.split(";");
                            if (!temp[2].equals("0")) {
                                mensaje += "Novedades(Insercion): de " + temp[1] + " fallan " + temp[2] + ", App " + envio + ";";
                            }
                        }
                    } else if (res.toLowerCase().contains("cobros") || res.toLowerCase().contains("conceptos")) {
                        if (res.toLowerCase().contains("noexiste")) {
                            if (cobros > 0) {
                                mensaje += "Novedades(inconsistencia): Server = 0 App = " + cobros + "; ";
                            }
                        }

                        if (res.toLowerCase().contains(";")) {
                            String[] temp = res.split(";");
                            if (!temp[2].equals("0")) {
                                mensaje += "Novedades(Insercion): de " + temp[1] + " fallan " + temp[2] + ", App " + cobros + ";";
                            }
                        }
                    }
                }
                return mensaje;
            }
        } catch (Exception ex) {
            utils.Log(logfile, "[MenuDeLiquidacion]respuestaWS();" + "Error:" + ex.getMessage());
        }
        return "";
    }

    public int parseStringToInteger(String x) {
        try {
            if (x.contains(".")) {
                int y = (int) parseStringToDouble(x);
                return y;
            }
            int y = Integer.parseInt(x.trim());
            return y;
        } catch (NumberFormatException e) {
            utils.Log(logfile, "[MenuDeLiquidacion]parseStringToInteger: " + e.getMessage());
            throw new RuntimeException("Error.Integer parseo " + x);
        } catch (Exception e) {
            utils.Log(logfile, "[MenuDeLiquidacion]parseStringToInteger: " + e.getMessage());
            throw new RuntimeException("Error.Integer parseo " + x);
        }
    }

    public double parseStringToDouble(String x) {
        try {
            double y = Double.parseDouble(x.trim());
            return y;
        } catch (NumberFormatException e) {
            utils.Log(logfile, "[MenuDeLiquidacion]parseStringToDouble: " + e.getMessage());
            throw new RuntimeException("Error.Double parseo " + x);
        } catch (Exception e) {
            utils.Log(logfile, "[MenuDeLiquidacion]parseStringToDouble: " + e.getMessage());
            throw new RuntimeException("Error.Double parseo " + x);
        }
    }

    private void Enviar_CertPostal_Censo_Aforos_Novedades_3() {

        Generica gen = new Generica();
        String temp = "";

        try {
            for (String doc : Archivos_CPCAN) {

                if (doc == "") continue;

                int desde = 0;
                int hasta = 0;

                if (doc.toLowerCase().contains("aforos")) {
                    desde = 98;
                    hasta = 99;
                    temp += " Aforos.\n ";
                } else if (doc.toLowerCase().contains("novedades")) {
                    desde = 180;
                    hasta = 181;
                    temp += " Novedades.\n ";

                } else if (doc.toLowerCase().contains("certificado")) {
                    desde = 234;
                    hasta = 236;
                    temp += " Certificados.\n ";
                } else if (doc.toLowerCase().contains("auditoriasupervisor")) {
                    desde = 274;
                    hasta = 276;
                    temp += " Auditoria Supervisor.\n ";
                } else {
                    continue;
                }
                Toast.makeText(getApplicationContext(), "Se realizo Envio:\n" + temp, Toast.LENGTH_LONG).show();

                gen.setOriginalFile(doc.replace(terminal, ""));
                gen.setNuevoFile(doc);
                gen.desde = desde;
                gen.hasta = hasta;
                String msg;

                if (doc.toLowerCase().contains("aforos") || doc.toLowerCase().contains("novedades")) {

                    msg = gen.WriteFileByPos("_", "Y", false);
                } else {
                    msg = gen.WriteFileByPos("EE", "SI", false);
                }

                if (gen.nuevoFile.exists()) {
                    gen.nuevoFile.delete();
                }

                if (msg != null && !msg.equals("1")) {
                    utils.Log(logfile, "Enviar_CertPostal_Censo_Aforos_Novedades_3:. " + msg);
                }
            }
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "Problemas al sobreescribir" + gen.getOriginalFile(), Toast.LENGTH_LONG).show();
            utils.Log(logfile, "Enviar_CertPostal_Censo_Aforos_Novedades_3; " + gen.getOriginalFile() + " ; " + ex.getMessage());
        }
        procesandoenvioenHilos_CPCAN = 0;
        Archivos_CPCAN = null;
    }

    private boolean CargarArchivoCarga() {

        String nombreArchivo = VariablesGlobales.directorioactual + "/ArchivosCarga.cfi";
        int contador = 0;
        try {
            FileReader r = new FileReader(nombreArchivo);
            BufferedReader reader = new BufferedReader(r);
            String linea = "";

            while ((linea = reader.readLine()) != null) {

                if (contador < 1) {
                    rutaAdministrador = linea.trim();

                    if (!rutaAdministrador.contains(":")) {
                        Toast.makeText(getApplicationContext(), "No hay Archivo de Soporte...\n //ArchivosCarga.cfi", Toast.LENGTH_LONG).show();
                        return false;
                    }
                }

                if (contador == 1) {
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
        } catch (Exception e) {//IOException e
            Log.e("INFO","CargarArchivoCarga|Error: " + e.getMessage());
        }
        return false;
    }

    private class UsarGPS implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            muestraPosicionActual(loc);
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

    private class AsyncCallWS extends AsyncTask<String, Integer, Void> {


        public String respuesta_;

        @Override
        protected Void doInBackground(String... params) {

            try {
                switch (metodo) {

                    case "ENVIOCERTPOSTLCENSOAFORONOVEDADS":
                        respuesta_ = Enviar_CertPostal_Censo_Aforos_Novedades_2();
                        break;

                    case "OBTENER_LONGITUD_ARCHIVO_2":
                        WSSoap wsoaps = new WSSoap(URL, paginaWs);
                        //agregar nuevos parametros
                        Log.e("error", "nuevos parametros " + dataTomoLec + "-");
                        Log.e("error", Municipio + Seccion + Division + "-parametros-" + ArchivoAEnviarRecibir + "-" + Ciclo + "-" + dataTomoLec + "-B");
                        respuesta_ = wsoaps.ObtenerLongitudArchivo("OBTENER_LONGITUD_ARCHIVO", ArchivoAEnviarRecibir, Integer.parseInt(esComprimido), Ciclo + "-" + dataTomoLec + "-B", Municipio + Seccion + Division);
                        break;

                }

            } catch (Exception e) {
                respuesta_ = "Error, valide conexion con Ws";
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            String msg = "";
            boolean bul = true;


            switch (metodo) {
                case "ENVIOCERTPOSTLCENSOAFORONOVEDADS":
                    if (respuesta_.equals("ok")) {
                        banderaNovedad = false;
                        banderaCertificado = false;
                        Enviar_CertPostal_Censo_Aforos_Novedades_3();
                    } else {
                        procesandoenvioenHilos_CPCAN = 0;

                        utils.Log(logfile, "BORRAR ESTO " + respuesta_);

                        if (respuesta_.length() > 30) {

                            Toast.makeText(getApplicationContext(), "No hubo envio " + ";\n " + respuesta_.substring(0, 30), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "No hubo envio " + ";\n " + respuesta_, Toast.LENGTH_LONG).show();
                        }

                    }
                    break;
                case "OBTENER_LONGITUD_ARCHIVO_2": //Ax nuevo, anterior perdido
                    Log.e("error", "respuesta " + respuesta_);
                    try {
                        long resp = Integer.parseInt(respuesta_);

                        Toast.makeText(getApplicationContext(), "No puede borrar. Primero descargue la ruta ", Toast.LENGTH_LONG).show();

                    } catch (Exception ex) {
                        if (respuesta_.equals("PUEDE BORRAR")) {
                            validacionesElimina();
                        }
                    }

                    break;

            }

        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
        }
    }

    private void validarEnvio() {


        esComprimido = "0";
        NombreZip = "D" + Municipio + Seccion + Division + ".ZIP";
        ArchivoAEnviarRecibir = rutaAdministrador.trim() + "CIC" + Ciclo + "\\" + "D" + Municipio + Seccion + Division + "\\" + "TOMOLECTURA.TXT";//archivo por buscar en el server

        try {
            leeTomoLectura();
            metodo = "OBTENER_LONGITUD_ARCHIVO_2";

            AsyncCallWS task = new AsyncCallWS();
            task.execute("");

        } catch (Exception ex) {
            utils.Log(logfile, "[OBTENER_LONGITUD_ARCHIVO_2]Error al Obtener Tamano de Archivo" + ex.getMessage());
        }
    }

    public void leeTomoLectura() {
        try {
            String nombreArchivo = VariablesGlobales.directorioactual + "/DATOSDESALIDA/TOMOLECTURA.TXT";
            File file = new File(nombreArchivo);

            if (file.exists()) {

                FileReader stream3 = new FileReader(nombreArchivo);
                BufferedReader reader = new BufferedReader(stream3);
                String linea = "";

                while ((linea = reader.readLine()) != null) {
                    dataTomoLec = linea;
                }

                reader.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("error", "no lee archivo tomolectura");
        }
    }

    public void validacionesElimina() {

        File desc = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/O" + Ciclo + Municipio + Seccion + Division + ".SDA"); //Ax: EJ: O108010.009

        if (VariablesGlobales.totalprediosnofacturados > 0 && !nivelOperador.trim().equals("A")){
            utils.MensajeTime("LA RUTA NO SE HA LEIDO EN SU TOTALIDAD, FALTAN CLIENTES POR FACTURAR", "CLIENTES SIN FACTURAR!", this, 8);
            return;
        }
        crearArchivoO();

        if (!nivelOperador.trim().equals("A")) {
            if ((!desc.exists() && desc.length() < 1) ) {//|| (fotosFaltantes() > 0)
            utils.MensajeTime("AUN NO HA ENVIADO LOS REGISTROS\n IR A LIQUIDACION EN... MENÚ OPCION ENVIO REGISTROS ", "FALTAN ENVÍOS!", this, 8);
            return;
            }
        }

        File file = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/ENVIOSGPRS.SDA");

        if (file.exists()) {
            mensajesAlert("Alerta de eliminacion", "SU TERMINAL TIENE\n REGISTROS\n SIN ENVIAR DEBE\n RETORNAR\n A LECTURAS Y ENVIARLOS", 1);
            //   mensajesAlert("Alerta de eliminacion", "ULTIMA CONFIRMACION, EXISTEN REGISTROS\nDE LECTURAS TERMINADAS \nVERIFIQUE QUE SE HAN ENVIADO. \n" + "DESEA REGRESAR?");
        }else {
            mensajesAlert("Alerta de eliminacion", "CONFIRMA QUE BORRARA LOS ARCHIVOS", 4);
        }

//        if (file.exists()) {
//            mensajesAlert("Alerta de eliminacion", "SU TERMINAL TIENE\n REGISTROS\n SIN ENVIAR DEBE\n RETORNAR\n A LECTURAS Y ENVIARLOS", 1);
//            //   mensajesAlert("Alerta de eliminacion", "ULTIMA CONFIRMACION, EXISTEN REGISTROS\nDE LECTURAS TERMINADAS \nVERIFIQUE QUE SE HAN ENVIADO. \n" + "DESEA REGRESAR?");
//        } else if (VariablesGlobales.totalprediosnofacturados > 0) {
//            mensajesAlert("Alerta de eliminacion", "RUTA NO SE HA LEIDO EN SU TOTALIDAD\n desea borrar la informacion? ", 3);
//            //  mensajesAlert("Alerta de eliminacion", "ULTIMA CONFIRMACION, ¿DESEA ELIMINAR LOS ARCHIVOS?" );
//        } else {
//            mensajesAlert("Alerta de eliminacion", "CONFIRMA QUE BORRARA LOS ARCHIVOS", 4);
//        }
    }
    public void crearArchivoO(){
        File desc = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/O" + Ciclo + Municipio + Seccion + Division + ".SDA"); //Ax: EJ: O108010.009
        utils.EscribirLinea(desc, "O" + Ciclo + Municipio + Seccion + Division);
    }

    private void realizarConteo() {
        try {

            infoRegistroSalida.setArchivo_TablaRegistroSalida(VariablesGlobales.directorioactual + "/DATOSDESALIDA/REGISTRO.SDA");
            tablaEntradaCliente.setArchivo_TablaEntradaClientes(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/CLIENTE.TXT");
            infoClienteSalida.setArchivo_TablaClienteSalida(VariablesGlobales.directorioactual + "/DATOSDESALIDA/CLIENTE.SDA");

            infoRegistroSalida.inicializaBloque();
            //  tablaEntradaCliente.inicializaBloque();
            infoClienteSalida.inicializaBloque();

            File registroSalida = new File(infoRegistroSalida.getArchivo_TablaRegistroSalida());
            File entradaCliente = new File(tablaEntradaCliente.getArchivo_TablaEntradaClientes());
            File clienteSalida = new File(infoClienteSalida.getArchivo_TablaClienteSalida());

            if (registroSalida.exists() && entradaCliente.exists() && clienteSalida.exists()) {

                if (!infoRegistroSalida.abrir_TablaRegistroSalida(infoRegistroSalida.getArchivo_TablaRegistroSalida())) {

                    return;
                }

                if (!tablaEntradaCliente.abrir_TablaEntradaClientes(tablaEntradaCliente.getArchivo_TablaEntradaClientes())) {

                    infoRegistroSalida.Cerrar_TablaRegistroSalida();
                    return;
                }

                if (!infoClienteSalida.abrir_TablaClienteSalida(infoClienteSalida.getArchivo_TablaClienteSalida())) {

                    infoRegistroSalida.Cerrar_TablaRegistroSalida();
                    tablaEntradaCliente.Cerrar_TablaEntradaClientes();
                    return;
                }

                valcantidad();
                //  estadisticaCausas();

            } else {

                Toast.makeText(getApplicationContext(), "Problemas al Abrir Ruta Cargada", Toast.LENGTH_LONG).show();
            }

        }catch (Exception ex){
            utils.Log(logfile,"[MenuPrincipal]realizarConteo()|Error" + ex.getMessage());
        }
    }

    private void valcantidad() {
        try {

            //-----------------------------------
            String horaini = "000000";
            String horafin = "000000";
            String RegistrosEnviados;
            int resultado;
            int ultimaCuentaNoLeida = 0;
            VariablesGlobales.totallecturas = 0;
            VariablesGlobales.totalcausasnolectura = 0;
            VariablesGlobales.totalnuevos = 0;
            VariablesGlobales.totalinformes = 0;
            VariablesGlobales.totalregistrosleidos = 0;
            VariablesGlobales.totalprediosleidos = 0;
            VariablesGlobales.totalpredioscomentarios = 0;
            VariablesGlobales.totalprediosliquidados = 0;
            VariablesGlobales.totalprediosimpresos = 0;
            VariablesGlobales.totalprediosnofacturados = 0;
            VariablesGlobales.registroactual = 1;
            // infoRegistroSalida.lectura_TablaRegistroSalida(VariablesGlobales.registroactual); //
            int clientesConLectura = 0;
            ultimaRutaSeleccionada = "";
            String miultimomedidor = "";
            Log.e("error",VariablesGlobales.registroactual+" +++entra aqui++++++++++++++++++++++"+infoRegistroSalida.getTotal_TablaRegistroSalida());
            while (VariablesGlobales.registroactual <= infoRegistroSalida.getTotal_TablaRegistroSalida()) {

                Log.e("error","entra aqui++++++++++++++++++++++");
                resultado = validarEstadoRegistro(VariablesGlobales.registroactual);

                // tablaEntradaCliente.lectura_TablaEntradaClientesII(Integer.parseInt(infoRegistroSalida.gettablaRegistroSalida_CLIENTE()));
                infoClienteSalida.lectura_TablaClienteSalidaII(Integer.parseInt(infoRegistroSalida.gettablaRegistroSalida_CLIENTE()));

                // EjecutarProcesoDeFoto TODO: pendiente migracion, preguntar si es     //Ax: TODO: ValidaRequiereFoto

//            if (!ultimaRutaSeleccionada.equals(tablaEntradaCliente.gettablaEntradaClientes_MUNICIPIO() + tablaEntradaCliente.gettablaEntradaClientes_RUTA().substring(0, 3))) {
//                ultimaRutaSeleccionada = tablaEntradaCliente.gettablaEntradaClientes_MUNICIPIO() + tablaEntradaCliente.gettablaEntradaClientes_RUTA().substring(0, 3);
//            }

                horaini = String.format("%1$6s", infoRegistroSalida.gettablaRegistroSalida_HORALECTURA().trim()).replace(" ", "0");

                if (resultado == 1) {

                    VariablesGlobales.totalregistrosleidos++;
                    VariablesGlobales.totallecturas++;
                } else if (resultado == 2) {

                    VariablesGlobales.totalregistrosleidos++;
                    VariablesGlobales.totalcausasnolectura++;
                    VariablesGlobales.totallecturas++;
                } else {

                    if (ultimaCuentaNoLeida == 0)
                        ultimaCuentaNoLeida = Integer.parseInt(infoRegistroSalida.gettablaRegistroSalida_CLIENTE());
                }

                VariablesGlobales.registroactual++;

                if (!infoRegistroSalida.gettablaRegistroSalida_COMENTARIO1().trim().equals(""))
                    VariablesGlobales.totalpredioscomentarios++;

                if (!infoRegistroSalida.gettablaRegistroSalida_INFORME().trim().equals(""))
                    VariablesGlobales.totalinformes++;

                // contar nuevos item a partir de clientes salida
                if (miultimomedidor.equals(infoRegistroSalida.gettablaRegistroSalida_CUENTA())) {
                    continue;
                }

                miultimomedidor = infoRegistroSalida.gettablaRegistroSalida_CUENTA();

                switch (infoClienteSalida.gettablaClienteSalida_INDFACTURACION()) {
                    case "F":
                        VariablesGlobales.totalprediosleidos++;
                        VariablesGlobales.totalprediosimpresos++;
                        break;

                    case "K":
                        VariablesGlobales.totalprediosleidos++;
                        VariablesGlobales.totalprediosliquidados++;
                        break;

                    case "L":
                        VariablesGlobales.totalprediosleidos++;
                        clientesConLectura++;
                        break;

                    case " ":
                        VariablesGlobales.totalprediosnofacturados++;
                        break;
                }
            }

            Log.e("error","cli 1 "+infoRegistroSalida.gettablaRegistroSalida_CLIENTE());
            tablaEntradaCliente.lectura_TablaEntradaClientesII(Integer.parseInt(infoRegistroSalida.gettablaRegistroSalida_CLIENTE()));
            infoClienteSalida.lectura_TablaClienteSalidaII(Integer.parseInt(infoRegistroSalida.gettablaRegistroSalida_CLIENTE()));

            if (!ultimaRutaSeleccionada.equals(tablaEntradaCliente.gettablaEntradaClientes_Municipio() + tablaEntradaCliente.gettablaEntradaClientes_Ruta().substring(0, 3))) {
                ultimaRutaSeleccionada = tablaEntradaCliente.gettablaEntradaClientes_Municipio() + tablaEntradaCliente.gettablaEntradaClientes_Ruta().substring(0, 3);
            }
            Log.e("error","cli 1 "+tablaEntradaCliente.gettablaEntradaClientes_Cliente());

            if (VariablesGlobales.totalprediosnofacturados > 0) {
                Log.e("INFO","Clientes por leer    : " + VariablesGlobales.totalprediosnofacturados);
            } else {
                Log.e("INFO", "Clientes por leer    : " + "0");
            }

            infoRegistroSalida.terminaBloque();
            infoClienteSalida.terminaBloque();

        }catch (Exception ex){
            mensajesAlert("Alerta","Error al realizar conteo de archivos, contactese con el administrador",6);
            utils.Log(logfile,"[MenuPrincipal]valcantidad()|ERROR|" + ex.getMessage());
        }
    }
    private int validarEstadoRegistro(int registroactual) {

        infoRegistroSalida.lectura_TablaRegistroSalidaII(registroactual);

        if (infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA().trim().length() != 0 && !infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA().trim().equals("0")
                && infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim().equals("")) {
            return (1);
        }

        if (!infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim().equals("")) {
            return (2);
        }
        return 0;
    }


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

                txtRuta.setText(""+linea.substring(4, 12));
                txtPrg.setText("CIC"+Ciclo);

             //   txtArchivoCargado.setText("Ciclo: " +Ciclo+" Archivo: " + linea.substring(4, 12));
                try {
                    //se comenta int x = Integer.parseInt(Ciclo);//Ax: Si tiene caracteres raros fallara

                    Municipio = linea.substring(5, 8);
                    Seccion = linea.substring(8, 11);
                    Division = linea.substring(11, 12);

                    //Axx????   nombreArchivo = Ciclo + "C" + Municipio + Seccion + Division + "     .N";

                    // Toast.makeText(getApplicationContext(), "La terminal ya esta \ncargada debe Borrar o Mover\n datos y cargar la nueva Ruta si\n es lo que requiere", Toast.LENGTH_LONG).show();
                    return;

                } catch (Exception e) {
                    Log.e("error", "nombre " + e.getMessage());
                    Ciclo = "0000";
                    Municipio = "000";
                    Seccion = "000";
                    Division = "0";
                }
            } catch (Exception ex) {
                Toast.makeText(getApplicationContext(), "Problema con la carga de:\n Archivo: Ciclo, Municipio...etc.", Toast.LENGTH_LONG).show();
                //Toast.makeText(getApplicationContext(), "Problema con la carga de:\n Archivo: Ciclo, Municipio...etc.", Toast.LENGTH_LONG).show();
            }
        }

        String directorioSalida = VariablesGlobales.directorioactual;
    }
    private void cargarImagenUsuario(String codigo) {
        try {
            File imgFile = new File(VariablesGlobales.directorioactual + "/AFORADORES/" + codigo + ".JPG");
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                ImageView myImage = (ImageView) findViewById(R.id.fotoAforador);
                myImage.setImageBitmap(myBitmap);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
