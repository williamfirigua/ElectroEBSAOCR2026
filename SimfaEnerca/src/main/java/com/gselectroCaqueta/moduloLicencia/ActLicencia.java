package com.gselectroCaqueta.moduloLicencia;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gselectroCaqueta.accesoyseguridad.GuiAcceso;
import com.gselectroCaqueta.accesoyseguridad.R;
import com.gselectroCaqueta.comunicaciones.BDComunicaciones;
import com.gselectroCaqueta.comunicaciones.CrudComunicaciones;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;


public class ActLicencia extends AppCompatActivity implements View.OnClickListener {
    String telephoneSerialNumber = "";
    EditText serial;
    EditText Codigo;
    Button validar;
    Context ctx;
    CrudComunicaciones crudComuni;
    final File sdCard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
    File file;// = new File(sdCard.getAbsolutePath(), "/Android/data/Config.txt");
    String control_serial = "P";
    String DatosEncriptados = "DCRFVTGBYH";
    DecimalFormat numF = new DecimalFormat("#,###");
    boolean esMayor9;

    private static final int CODIGO_PERMISOS_CAMARA = 1, CODIGO_PERMISOS_ALMACENAMIENTO = 2, CODIGO_PERMISOS_TELEFONO = 3, CODIGO_PERMISOS_UBICACION = 4, CODIGO_PERMISOS_BLUETOOHSCN = 5,
            CODIGO_PERMISOS_BLUETOOHCNT = 6, CODIGO_PERMISOS_IMAGENES = 7, CODIGO_COMUNICACION = 8, CODIGO_FULL_WRITE = 9;

    public int CodigoIntent = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_licencia);
        ctx = this;
        crudComuni = new CrudComunicaciones(this);
        serial = (EditText) findViewById(R.id.serial);
        Codigo = (EditText) findViewById(R.id.codigo);
        validar = (Button) findViewById(R.id.btnValidar);
        validar.setOnClickListener(this);

        comprobarPermisos(1);
        setParams();
        gestionarLicencia();

    }// On create

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (CodigoIntent == CODIGO_FULL_WRITE) {
                //gestionarLicencia();
                comprobarPermisos(3);
            }
        }
    });

    public void gestionarLicencia() {
        try {
            file = new File(sdCard, "Config.txt");

            Bundle bundle = getIntent().getExtras();
            telephoneSerialNumber = bundle.getString("telephoneSerialNumber");

            if (file.exists()) {
                serial.setText(telephoneSerialNumber);
                try {
                    FileInputStream fIn = new FileInputStream(file);
                    InputStreamReader archivo = new InputStreamReader(fIn);
                    BufferedReader br = new BufferedReader(archivo);
                    String linea = br.readLine();
                    String dt = "";
                    String Cdesenc = "";
                    control_serial = "I";

                    while (linea != null) {
                        //se hace n split por ; linea por linea
                        dt = dt + linea;
                        linea = br.readLine();
                        Cdesenc = Desencriptar(dt);

                        Intent miIntent = new Intent();
                        Bundle bundleg = new Bundle();
                        bundleg.putString("valida", "SI");
                        bundleg.putString("PhoneImei", Cdesenc);
                        miIntent.setClass(this, GuiAcceso.class);
                        miIntent.putExtras(bundleg);
                        startActivity(miIntent);
                        finish();
                        //  int resultado = Procesar_Serial(serial.getText().toString().trim(), Double.parseDouble(Cdesenc.trim()), control_serial);

                    }

                    br.close();
                    archivo.close();

                } catch (Exception e) {

                }

            } else {
            }
        } catch (Exception ex) {
            Log.e("ERROR", "[ActLicencia]gestionarLicencia()|Error -> " + ex.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CODIGO_PERMISOS_CAMARA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    comprobarPermisos(2);
                }
                break;

            case CODIGO_PERMISOS_ALMACENAMIENTO:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    comprobarPermisos(3);
                }
                break;

            case CODIGO_PERMISOS_TELEFONO:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    comprobarPermisos(4);
                }
                break;

            case CODIGO_PERMISOS_UBICACION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    comprobarPermisos(5);
                }
                break;

            case CODIGO_PERMISOS_BLUETOOHSCN:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    comprobarPermisos(6);
                }
                break;

            case CODIGO_PERMISOS_BLUETOOHCNT:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    comprobarPermisos(7);
                }
                break;

            case CODIGO_PERMISOS_IMAGENES:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    comprobarPermisos(2); // Luego de imágenes, va almacenamiento si aplica
                }
                break;
        }
    }

    private boolean comprobarPermisos(int permiso) {
        try {
            switch (permiso) {
                case 1:
                    int estadoPermisoCamara = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
                    if (estadoPermisoCamara != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CODIGO_PERMISOS_CAMARA);
                    } else {
                        comprobarPermisos(2);
                    }
                    break;
                case 2:
                    if (Build.VERSION.SDK_INT >= 30) { // Android 11 o superior
                        if (!Environment.isExternalStorageManager()) {
                            CodigoIntent = CODIGO_FULL_WRITE;
                            try {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                                intent.setData(Uri.parse("package:" + getPackageName()));
                                activityResultLauncher.launch(intent);
                            } catch (Exception e) {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                                activityResultLauncher.launch(intent);
                            }
                        } else {
                            comprobarPermisos(3);
                        }
                    } else { // Android 10 o menor
                        int estadoPermisoAlmacenamiento = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        if (estadoPermisoAlmacenamiento != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CODIGO_PERMISOS_ALMACENAMIENTO);
                        } else {
                            comprobarPermisos(3);
                        }
                    }
                    break;
                case 3:
                    int estadoPermisoTelefono = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
                    if (estadoPermisoTelefono != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, CODIGO_PERMISOS_TELEFONO);
                    } else {
                        comprobarPermisos(4);
                    }
                    break;
                case 4:
                    int estadoPermisoUbicacion = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
                    if (estadoPermisoUbicacion != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, CODIGO_PERMISOS_UBICACION);
                    } else {
                        comprobarPermisos(5);
                    }
                    break;
                case 5:
                    if (Build.VERSION.SDK_INT >= 31) {
                        int estadoPermisobthscan = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN);
                        if (estadoPermisobthscan != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, CODIGO_PERMISOS_BLUETOOHSCN);
                        } else {
                            comprobarPermisos(6);
                        }
                    } else {
                        comprobarPermisos(7);
                    }
                    break;
                case 6:
                    int estadoPermisobthadm = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT);
                    if (estadoPermisobthadm != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, CODIGO_PERMISOS_BLUETOOHCNT);
                    } else {
                        comprobarPermisos(7);
                    }
                    break;
                case 7:
                    // Todos los permisos concedidos
                    Log.e("PERMISOS", "Todos los permisos concedidos. Puedes continuar.");
                    gestionarLicencia();
                    break;
            }
        } catch (Exception ex) {
            Log.e("Tag Permisos", "Error: " + ex);
            alertMensaje("Error al Comprobar Permisos: " + ex);
        }
        return false;
    }
    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CODIGO_PERMISOS_CAMARA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    comprobarPermisos(2);
                }
                break;

            case CODIGO_PERMISOS_ALMACENAMIENTO:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    comprobarPermisos(3);
                }
                break;

            case CODIGO_PERMISOS_TELEFONO:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    comprobarPermisos(4);
                }
                break;

            case CODIGO_PERMISOS_UBICACION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    comprobarPermisos(5);
                }
                break;

            case CODIGO_PERMISOS_BLUETOOHSCN:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    comprobarPermisos(6);
                }
                break;

            case CODIGO_PERMISOS_BLUETOOHCNT:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    comprobarPermisos(7);
                }
                break;
        }
    }
    private boolean comprobarPermisos(int permiso) {
        try {
            switch (permiso) {
                case 1:
                    int estadoPermisoCamara = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
                    if (estadoPermisoCamara != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CODIGO_PERMISOS_CAMARA);
                    } else {
                        comprobarPermisos(2);
                    }
                    break;
                case 2:
                    int estadoPermisoAlmacenamiento = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (estadoPermisoAlmacenamiento != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CODIGO_PERMISOS_ALMACENAMIENTO);
                    } else {
                        comprobarPermisos(3);
                    }
                    break;
                case 3:
                    int estadoPermisoTelefono = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
                    if (estadoPermisoTelefono != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, CODIGO_PERMISOS_TELEFONO);
                    } else {
                        comprobarPermisos(4);
                    }
                    break;
                case 4:
                    int estadoPermisoUbicacion = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
                    if (estadoPermisoUbicacion != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, CODIGO_PERMISOS_UBICACION);
                    } else {
                        comprobarPermisos(5);
                    }
                    break;
                case 5:
                    if(Build.VERSION.SDK_INT > 30){
                        Log.e("INFO","COMPRUEBO PERMISO MAYOR A 30");
                        int estadoPermisobthscan = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN);
                        if (estadoPermisobthscan != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, CODIGO_PERMISOS_BLUETOOHSCN);
                        } else {
                            comprobarPermisos(6);
                        }
                    }else{
                        Log.e("INFO","NO COMPRUEBO PERMISO MAYOR A 30");
                        comprobarPermisos(7);

                    }
                    break;
                case 6:
                    int estadoPermisobthadm = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT);
                    if (estadoPermisobthadm != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, CODIGO_PERMISOS_BLUETOOHCNT);
                    } else {
                        comprobarPermisos(7);
                    }
                    break;
                case 7:
                    if (!Environment.isExternalStorageManager()){
                        try {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                            intent.addCategory("android.intent.category.DEFAULT");
                            intent.setData(Uri.parse(String.format("package:%s",new Object[]{getApplicationContext().getPackageName()})));
                            activityResultLauncher.launch(intent);
                        }catch (Exception e){
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                            activityResultLauncher.launch(intent);
                        }
                    }else{
                    }
                    break;
            }
        } catch (Exception ex) {
            Log.e("Tag Permisos","Error: "+ex);
            alertMensaje("Error al Comprobar Permisos: "+ex);
        }
        return false;
    }*/

//    // Storage Permissions
//    private static final int REQUEST_EXTERNAL_STORAGE = 1;
//    private static String[] PERMISSIONS_STORAGE = {
//            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE
//    };
//
//    /**
//     * Checks if the app has permission to write to device storage
//     *
//     * If the app does not has permission then the user will be prompted to grant permissions
//     *
//     * @param activity
//     */
//    public static void verifyStoragePermissions(Activity activity) {
//        // Check if we have write permission
//        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//
//        if (permission != PackageManager.PERMISSION_GRANTED) {
//            // We don't have permission so prompt the user
//            ActivityCompat.requestPermissions(
//                    activity,
//                    PERMISSIONS_STORAGE,
//                    REQUEST_EXTERNAL_STORAGE
//            );
//        }
//    }

//    public String getSerialNumber() {
//
//        TelephonyManager tManager = (TelephonyManager) getBaseContext().getSystemService(this.TELEPHONY_SERVICE);
//        String deviceIMEI = tManager.getDeviceId();
//        return deviceIMEI;
//    }

    public int Procesar_Serial(String Serial, Double valor_entrada, String controlador) {

        telephoneSerialNumber = Serial;
        double valor_calculado = 0;
        double valor_recibido = 0;
        String letra = "";
        int res = 0;
        Intent miIntent = new Intent();

        try {

            double Numero = 0;
            double Numero2 = 0;

            for (int i = 0; i < Serial.length(); i++) {

                letra = Serial.substring(i, i + 1);
                valor_recibido = Letras_Serial(letra);

                if (valor_recibido == 0) //se dara algo con el valor
                {

                    Numero = Double.parseDouble(letra);
                    Numero2 = Double.parseDouble(letra + letra);

                    if (i < 6) {
                        valor_recibido = Math.pow(Numero, Numero);//Convert.ToInt32(letra.PadLeft(i + 1, '3'));

                    } else {
                        valor_recibido = Math.pow(Numero2, 6);
                        // MessageBox.Show("Valor " + Numero2.ToString() + "=" + valor_recibido.ToString());
                    }

                }
                valor_calculado += valor_recibido;
            }

            int ConversionP = String.valueOf(numF.format(valor_calculado)).indexOf(".");
            int ConversionC = String.valueOf(numF.format(valor_calculado)).indexOf(",");
            String Cambio = "";

            if (ConversionP >= 0) {
                Cambio = ".";
            }
            if (ConversionC >= 0) {
                Cambio = ",";
            }

            if (controlador.trim().toUpperCase().equals("V")) {

                alertMensaje("Este es su codigo de acceso " + String.format("%-40s", numF.format(valor_calculado)).replace(Cambio, "").trim());

            } else if (String.format("%-40s", numF.format(valor_calculado)).replace(Cambio, "").trim().equals(String.format("%-40s", numF.format(valor_entrada)).replace(Cambio, "").trim()) && controlador.trim().toUpperCase().equals("I")) {

                Serial = Encriptar(telephoneSerialNumber);

                try {

                    if (!file.exists()) {
                        FileOutputStream FileOut = new FileOutputStream(file);
                        OutputStreamWriter osw = new OutputStreamWriter(FileOut);
                        osw.write(Serial);
                        osw.flush();
                        osw.close();
                        Log.e("bien", "Los datos fueron grabados correctamente");

                    } else {
                    }

                    Bundle bundleg = new Bundle();
                    bundleg.putString("valida", "SI");
                    bundleg.putString("PhoneImei", telephoneSerialNumber);
                    miIntent.setClass(this, GuiAcceso.class);
                    miIntent.putExtras(bundleg);
                    startActivity(miIntent);
                    finish();
                } catch (Exception e) {
                    Log.e("valor", "no se proceso " + e.getMessage());
                }
                res = 1;
                return (res);
            } else {
                finish();
            }
            return (res);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("valor", "no se proceso " + e.getMessage());

            res = 0;
            return (res);
        }

    }

    public int Letras_Serial(String letra) {
        int res = 0;
        try {
            if (letra.toUpperCase() == "Z" || letra.toUpperCase() == "X" || letra.toUpperCase() == "C" || letra.toUpperCase() == "V") {
                res = 563289;
                return (res);
            }
            if (letra.toUpperCase() == "B" || letra.toUpperCase() == "N" || letra.toUpperCase() == "M" || letra.toUpperCase() == "Ñ") {
                res = 654987;
                return (res);
            } else {
                if (letra.toUpperCase() == "L" || letra.toUpperCase() == "K" || letra.toUpperCase() == "J" || letra.toUpperCase() == "H") {
                    res = 354982;
                    return (res);
                } else if (letra.toUpperCase() == "G" || letra.toUpperCase() == "F" || letra.toUpperCase() == "D" || letra.toUpperCase() == "S") {
                    res = 852147;
                    return (res);
                } else if (letra.toUpperCase() == "A" || letra.toUpperCase() == "Q" || letra.toUpperCase() == "W" || letra.toUpperCase() == "E" || letra.toUpperCase() == "R") {
                    res = 963258;

                    return (res);
                } else if (letra.toUpperCase() == "T" || letra.toUpperCase() == "Y" || letra.toUpperCase() == "U" || letra.toUpperCase() == "I" || letra.toUpperCase() == "O" || letra.toUpperCase() == "P") {
                    res = 325698;
                    return (res);
                } else {
                    return (res);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return (res);
        }


        // return (res);

    }
    // ====== Compat con tu CrudComunicaciones (por si lo necesitas en el futuro) ======
    public void setParams() {
        try {
            BDComunicaciones bdcini = crudComuni.getParamsbyId(1);
            if (bdcini == null) {
                BDComunicaciones bdc = new BDComunicaciones();
                bdc.setId(1);
                bdc.setURL("192.168.1.254:836");
                bdc.setPaginaWs("/demoenergia.asmx");   // tu SOAP, no usado aquí
                bdc.setEstado("A");
                bdc.setRutaAdministrador("D:/DEMOENRUTADOR_PEREIRA");
                bdc.setPuertoApi(914);
                crudComuni.setCofigUrl(bdc);

                bdc.setId(2);
                bdc.setURL("186.117.240.195:836");
                bdc.setPaginaWs("/demoenergia.asmx");
                bdc.setEstado("I");
                bdc.setRutaAdministrador("D:/DEMOENRUTADOR_PEREIRA");
                bdc.setPuertoApi(914);
                crudComuni.setCofigUrl(bdc);
            }
        } catch (Exception ignored) {
        }
    }

    /*public void getParamsWs() {
        try {
            BDComunicaciones bdc = crudComuni.getParams();
            if (bdc != null) {
                URL = "http://" + bdc.getURL();
                paginaWs = bdc.getPaginaWs();
            }
        } catch (Exception ignored) {
        }
    }*/

    public String Desencriptar(String entrada) {
        String DatosEncriptados = "DCRFVTGBYH";
        int i = 0;
        int j = 0;
        String salida = "";
        String alfabeto = "*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ " + "\n" + "\r";

        String sub1 = "QAZWSX" + DatosEncriptados + "NUJM IKOLP*>" + "+=,<-;.:/908?172634[|@" + "\n" + "\r";

        //DCRFVTGBYH
        for (i = 0; i <= entrada.length() - 1; i++) {
            for (j = 0; j <= sub1.length() - 1; j++) {
                if (entrada.substring(i, i + 1).equals(sub1.substring(j, j + 1))) {

                    salida += alfabeto.substring(j, j + 1);
                }
            }
        }
        return (salida);
    }

    public String Encriptar(String entrada) {
        String DatosEncriptados = "DCRFVTGBYH";
        int i = 0;
        int j = 0;
        String salida = "";
        String alfabeto = "*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ " + "\n" + "\r";
        String sub1 = "QAZWSX" + DatosEncriptados + "NUJM IKOLP*>" + "+=,<-;.:/908?172634|[@" + "\n" + "\r";

        //DCRFVTGBYH
        for (i = 0; i <= entrada.length() - 1; i++) {
            for (j = 0; j <= alfabeto.length() - 1; j++) {

                if (entrada.substring(i, i + 1).equals(alfabeto.substring(j, j + 1))) {
                    salida += sub1.substring(j, j + 1);
                }
            }
        }
        return (salida);
    }

    public void alertMensaje(String mensaje) {
        Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        v.vibrate(500);
        AlertDialog.Builder alertConfCreado = new AlertDialog.Builder(this);
        alertConfCreado.setIcon(R.drawable.ic_launcher1);
        alertConfCreado
                .setMessage(mensaje)
                .setTitle("Alerta")
                .setCancelable(false)
                .setPositiveButton("Terminar",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                finish();
                            }
                        });
        AlertDialog alert = alertConfCreado.create();
        alert.show();
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btnValidar) {
            String TxtCod = Codigo.getText().toString().trim();

            if (TxtCod.length() > 0) {
                if (TxtCod.equals("9004312571966")) {
                    control_serial = "V";
                    int resultado = Procesar_Serial(serial.getText().toString().trim(), Double.parseDouble(Codigo.getText().toString().trim()), control_serial);

                    if (resultado == 0) {
                        //finish();
                    } else {
                    }
                } else {
                    control_serial = "I";
                    int resultado = Procesar_Serial(serial.getText().toString().trim(), Double.parseDouble(Codigo.getText().toString().trim()), control_serial);

                }
            }

        }
    }
}
