package com.gselectroCaqueta.accesoyseguridad;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.gselectroCaqueta.comunicaciones.BDComunicaciones;
import com.gselectroCaqueta.comunicaciones.CrudComunicaciones;
import com.gsutil.UtilsNet;
import com.gsutil.WSSoap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FechayHora extends AppCompatActivity {

    public String metodo;
    ToggleButton gps;
    ToggleButton modoAvion;
    ToggleButton Red;
    ToggleButton Bateria;
    String URL = "";
    String paginaWs = "";
    TextView ip;
    Button btnPruebaWs;
    TextView macP;
    TextView ip2;

    private static boolean isAirplaneModeOn(Context context) {

        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) != 0;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fechay_hora);

        gps = (ToggleButton) findViewById(R.id.tGps);
        modoAvion = (ToggleButton) findViewById(R.id.tMavion);
        Red = (ToggleButton) findViewById(R.id.tRed);
        Bateria = (ToggleButton) findViewById(R.id.tBateria);
        ip = (TextView) findViewById(R.id.txvIp);
        btnPruebaWs = (Button) findViewById(R.id.btnPruebaWs);
        macP = (TextView) findViewById(R.id.txtMac);
        ip2 = (TextView) findViewById(R.id.txvIp2);
        // Button btnRegresarf = (Button) findViewById(R.id.btnRegresarfecha);
        ProbarConn();
        verificarBattery();
        if (!getParamsWs()) {
            Toast.makeText(getApplicationContext(), "No hay ip configurada", Toast.LENGTH_LONG).show();
        }
        if (!macPrinter()) {
            Toast.makeText(getApplicationContext(), "No hay Mac", Toast.LENGTH_LONG).show();
        }
        LocationManager locationmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        LocationListener mlocListener = new FechayHora.UsarGPS();

        //  locationmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, mlocListener);

        if (!locationmanager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            gps.setTextOff("Gps off");
            gps.setChecked(false);

        } else {
            gps.setTextOn("Gps on");
            gps.setChecked(true);
        }

        if (isAirplaneModeOn(getApplicationContext())) {
            modoAvion.setTextOn("M.avion on");
            modoAvion.setChecked(true);
        } else {
            modoAvion.setTextOff("M.avion off");
            modoAvion.setChecked(false);
        }

        findViewById(R.id.btnPruebaWs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProbarWs();
            }
        });

        //Ax: para regresar
        findViewById(R.id.btnRegresarfecha).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                // startActivity(new Intent(FechayHora.this, com.globalsolutions.accesoyseguridad.MenuPrincipal.class));
            }
        });
    }

    public void ProbarConn() {
        metodo = "HAY_CONEXION";

        try {
            FechayHora.AsyncCallRed task = new FechayHora.AsyncCallRed();
            task.execute("");

        } catch (Exception ex) {

        }
    }

    public void ProbarWs() {
        metodo = "VALIDAR_CONEXION";

        try {
            FechayHora.AsyncCallRed task = new FechayHora.AsyncCallRed();
            task.execute("");

        } catch (Exception ex) {

        }
    }

    private void verificarBattery() {

        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if (level == -1 || scale == -1) {


            Bateria.setTextOff("B." + Float.toString(50.0f) + "%");
            Bateria.setChecked(false);

        } else {

            Bateria.setTextOn("B." + Float.toString(((float) level / (float) scale) * 100.0f) + "%");
            Bateria.setChecked(true);
        }

    }

    public boolean getParamsWs() {
        try {
            CrudComunicaciones crudComuni = new CrudComunicaciones(this);
            BDComunicaciones bdc = crudComuni.getParams();
            if (bdc != null) {
                URL = "http://" + bdc.getURL();
                paginaWs = bdc.getPaginaWs();
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }
    /*private boolean seleccionUrl() {
        try {
            String nombreArchivo = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DIRECCIONEIP.TXT";
            File file = new File(nombreArchivo);

            if (!file.exists()) return false;

            FileReader stream3 = new FileReader(nombreArchivo);
            BufferedReader reader = new BufferedReader(stream3);
            String linea = "";
            String datoIP = "";
            int cont = 0;
            while ((linea = reader.readLine()) != null) {
                datoIP = linea;
                if (datoIP.substring(datoIP.length() - 1, datoIP.length()).trim().equals("A")) {
                    URL = "http://" + datoIP.substring(0, 15).trim() + ":" + datoIP.substring(16, 20).trim();
                    paginaWs = datoIP.substring(64, 157).trim();
                    cont++;
                    //  break;
                }

                if (datoIP.substring(datoIP.length() - 1, datoIP.length()).trim().equals("I")) {
                    String datoIP2 = "http://" + datoIP.substring(0, 15).trim() + ":" + datoIP.substring(16, 20).trim();
                    datoIP2 = datoIP2.replace(" ", "");
                    ip2.setText(" " + datoIP2 + " Inactiva");

                }
            }

            reader.close();

            if (cont < 1) return false;

            URL = URL.replace(" ", "");
            ip.setText(URL + " Activa");
            return true;

        } catch (IOException e) {
            e.printStackTrace();

        }
        return false;
    }*/

    private boolean macPrinter() {
        try {
            String nombreArchivo = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DATOSDESALIDA/PRINTER.LOG";
            File file = new File(nombreArchivo);

            if (!file.exists()) return false;

            FileReader stream3 = new FileReader(nombreArchivo);
            BufferedReader reader = new BufferedReader(stream3);
            String linea = "";
            String datos = "";

            while ((linea = reader.readLine()) != null) {

                datos = linea;

            }

            reader.close();

            macP.setText(" Mac Impresora: " + datos);
            return true;

        } catch (IOException e) {
            e.printStackTrace();

        }
        return false;
    }

    private class UsarGPS implements LocationListener {


        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    private class AsyncCallRed extends AsyncTask<String, Integer, Void> {


        public String respuesta_;

        @Override
        protected Void doInBackground(String... params) {

            try {
                switch (metodo) {

                    case "HAY_CONEXION":
                        UtilsNet utilnet = new UtilsNet();
                        boolean Internet = utilnet.hayInternet(getApplicationContext());
                        if (!Internet) {
                            respuesta_ = "NO";

                        } else {
                            respuesta_ = "SI";
                        }
                        break;
                    case "VALIDAR_CONEXION":
                        WSSoap wsoap = new WSSoap(URL, paginaWs);
                        respuesta_ = wsoap.verificarWs("VALIDAR_CONEXION");

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
                case "HAY_CONEXION":

                    if (respuesta_.equals("SI")) {
                        Red.setTextOn("Red on");
                        Red.setChecked(true);
                    } else {
                        Red.setTextOff("Red off");
                        Red.setChecked(false);
                    }
                    break;
                case "VALIDAR_CONEXION":
                    try {
                        Integer.parseInt(respuesta_);
                        Toast.makeText(getApplicationContext(), "Conexion con Webservice OK", Toast.LENGTH_LONG).show();

                    } catch (NumberFormatException e) {

                        Toast.makeText(getApplicationContext(), "Error de conexion con Webservice\n" + respuesta_, Toast.LENGTH_LONG).show();
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
}
