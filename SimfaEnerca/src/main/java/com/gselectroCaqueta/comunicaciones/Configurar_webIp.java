package com.gselectroCaqueta.comunicaciones;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gselectroCaqueta.accesoyseguridad.R;
import com.gselectroCaqueta.accesoyseguridad.VariablesGlobales;
import com.gsutil.AsyncResponse;
import com.gsutil.Utils;
import com.gsutil.WSSoap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Configurar_webIp extends AppCompatActivity implements AsyncResponse {

    public String URL;
    public String URL1;
    public String URL2;
    public String paginaWs;
    public String paginaWs1;
    public String paginaWs2;
    CheckBox chkIp1;
    CheckBox chkIp2;
    EditText txtIpWs1;
    EditText txtIpWs2;
    EditText txtIpWs3;
    EditText txtIpWs4;
    EditText txtDescServWeb;
    EditText txtPuerto;
    EditText txtTramas;
    Button btnGrabarIp;
    Button btnPruebaWs;
    ImageView iconWebSoff;
    ImageView iconWebSon;
    TextView lblTransmite;
    String trama = "10"; //Ax: Por defecto
    String puerto = "85";
    private String nivelOperador;
    private Boolean pruebawsok;
    private Boolean chk1;
    private Boolean chk2;
    Toast toast = null;
    Utils utils = new Utils();//Ax log y utilidades
    File logfile; //Ax: Es para los log de error

    public int puertoAPI;
    EditText edtxPuertoAPI;

    Context ctx;
    String externalpath = "";
    CrudComunicaciones crudComuni;
    EditText txtdirWs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configurar_web_ip);
        ctx = this;

        externalpath = ctx.getExternalFilesDir(null).getParent();
        String hardcoding = "/Android/data/";//Ax: todo: cambiar este hardcoding

        if (externalpath.contains(hardcoding)) {
            externalpath = externalpath.substring(0, externalpath.indexOf(hardcoding));
        }

        logfile = new File(VariablesGlobales.directorioactual + "/LECTURAMEDIDORES/LOGEVENTOS.LOG");
        Intent intent = getIntent();
        nivelOperador = intent.getStringExtra("nivelOperador"); //Ax: capturar variable de la actividad anterior
        pruebawsok = false;//intent.getBooleanExtra("pruebawsok", false);
        trama = "";//intent.getStringExtra("trama");

        chkIp1 = (CheckBox) findViewById(R.id.chkIp1);
        chkIp2 = (CheckBox) findViewById(R.id.chkIp2);
        txtdirWs = (EditText) findViewById(R.id.txtdirWs);
        btnGrabarIp = (Button) findViewById(R.id.btnGrabarIp);
        btnPruebaWs = (Button) findViewById(R.id.btnPruebaWs);
        lblTransmite = (TextView) findViewById(R.id.lblTransmite);
        iconWebSon = (ImageView) findViewById(R.id.iconwebserviceon);
        txtDescServWeb = (EditText) findViewById(R.id.txtDescServWeb);
        edtxPuertoAPI = (EditText) findViewById(R.id.edtxPuertoAPI);
        iconWebSoff = (ImageView) findViewById(R.id.iconwebserviceoff);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (!nivelOperador.equals("432") && !nivelOperador.equals("S") && !nivelOperador.equals("A")) {// != "A"
            txtDescServWeb.setEnabled(false);
            btnGrabarIp.setEnabled(false);
            txtdirWs.setEnabled(false);
            edtxPuertoAPI.setEnabled(false);
            /*txtIpWs1.setEnabled(false);
            txtIpWs2.setEnabled(false);
            txtIpWs3.setEnabled(false);
            txtIpWs4.setEnabled(false);
            txtTramas.setEnabled(false);*/
        }

        getParamsWs(0);

        //mostrarDatos();

        if (!pruebawsok) { //Ax: para no repetir prueba de conn
            iconWebSon.setVisibility(View.INVISIBLE);
            iconWebSoff.setVisibility(View.VISIBLE);
        } else {
            iconWebSoff.setVisibility(View.INVISIBLE);
            iconWebSon.setVisibility(View.VISIBLE);
        }


        findViewById(R.id.btnRegresar1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent data = new Intent();//Regresarle datos a counicaciones
                data.putExtra("trama", trama);
                data.putExtra("pruebawsok", pruebawsok);
                data.putExtra("puerto", puerto);
                data.putExtra("url", URL);
                data.putExtra("paginaWs", paginaWs);
                setResult(RESULT_OK, data);
                finish();
            }
        });

        findViewById(R.id.btnGrabarIp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateparams();
            }
        });

        findViewById(R.id.btnPruebaWs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chkIp1.isChecked() || chkIp2.isChecked()) {
                    btnPruebaWs.setEnabled(false);
                    AsyncCallWS task = new AsyncCallWS();
                    task.execute("");
                }else{
                    mensajeT("Chequee una casilla",1000);
                }
            }
        });

        chkIp1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                                              @Override
                                              public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                  /*if (chkIp1.isChecked()) {
                                                      chk2 = false;
                                                      chk1 = true;
                                                      URL = URL1;
                                                      paginaWs = paginaWs1;
                                                      enableDisableMobileData();
                                                      mostrarDatos();
                                                  }*/

                                                  if (chkIp1.isChecked()) {
                                                      if (chkIp2.isChecked()) {
                                                          chkIp2.setChecked(false);
                                                      }
                                                      getParamsWs(1);
                                                  }
                                              }
                                          }
        );

        chkIp2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                                              @Override
                                              public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                                                  /*if (chkIp2.isChecked()) {
                                                      chk2 = true;
                                                      chk1 = false;
                                                      URL = URL2;
                                                      paginaWs = paginaWs2;
                                                      enableDisableMobileData();
                                                      mostrarDatos();
                                                  }*/
                                                  if (chkIp2.isChecked()) {
                                                      if (chkIp1.isChecked()) {
                                                          chkIp1.setChecked(false);
                                                      }
                                                      getParamsWs(2);
                                                  }
                                              }
                                          }
        );

    }

    public boolean updateparams() {
        try {
            BDComunicaciones bdc = new BDComunicaciones();
            int id;
            String dirws = txtdirWs.getText().toString().replace("http://", "").trim();
            String pgws = txtDescServWeb.getText().toString().trim();
            int papi = Integer.parseInt(edtxPuertoAPI.getText().toString().trim());
            if (chkIp1.isChecked() && !dirws.equals("") && !pgws.equals("")) {
                id = 1;
                bdc.setURL(dirws);
                bdc.setPaginaWs(pgws);
                bdc.setEstado("A");
                bdc.setPuertoApi(papi);
                crudComuni.updateParams(bdc, id);
                crudComuni.updateStatus(2, "I");
                URL = "http://" + dirws;
                paginaWs = pgws;
                Toast.makeText(this, "PARAMETROS ESTABLECIDOS CORRECTAMENTE", Toast.LENGTH_LONG).show();
                return true;
            } else if (chkIp2.isChecked() && !dirws.equals("") && !pgws.equals("")) {
                id = 2;
                bdc.setURL(dirws);
                bdc.setPaginaWs(pgws);
                bdc.setEstado("A");
                bdc.setPuertoApi(papi);
                crudComuni.updateParams(bdc, id);
                crudComuni.updateStatus(1, "I");
                URL = "http://" + dirws;
                paginaWs = pgws;
                Toast.makeText(this, "PARAMETROS ESTABLECIDOS CORRECTAMENTE", Toast.LENGTH_LONG).show();
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            String[] time = getTimeError().split("\\|");
            utils.Log(logfile, "[ConfigWIP]getParamsWs()|" + ex.getMessage() + "|" + time[0] + "|" + time[1] + "|");
            return false;
        }
    }

    public boolean getParamsWs(int id) {
        try {
            if (id == 0) {
                BDComunicaciones bdc = crudComuni.getParams();
                if (bdc != null) {
                    if (bdc.getId() == 1) {
                        chk2 = false;
                        chk1 = true;
                    } else {
                        chk2 = true;
                        chk1 = false;
                    }
                    URL = "http://" + bdc.getURL();
                    paginaWs = bdc.getPaginaWs();
                    puertoAPI = bdc.getPuertoApi();
                    chkIp1.setChecked(chk1);
                    chkIp2.setChecked(chk2);
                    txtdirWs.setText(URL);
                    txtDescServWeb.setText(String.valueOf(paginaWs));
                    edtxPuertoAPI.setText(String.valueOf(puertoAPI));
                    return true;
                } else {
                    return false;
                }
            } else {
                BDComunicaciones bdc = crudComuni.getParamsbyId(id);
                if (bdc != null) {
                    if (id == 1) {
                        chk2 = false;
                        chk1 = true;
                    } else {
                        chk2 = true;
                        chk1 = false;
                    }
                    URL = "http://" + bdc.getURL();
                    paginaWs = bdc.getPaginaWs();
                    puertoAPI = bdc.getPuertoApi();
                    chkIp1.setChecked(chk1);
                    chkIp2.setChecked(chk2);
                    txtdirWs.setText(URL);
                    txtDescServWeb.setText(String.valueOf(paginaWs));
                    edtxPuertoAPI.setText(String.valueOf(puertoAPI));
                } else {
                    return false;
                }
            }
        } catch (Exception ex) {
            String[] time = getTimeError().split("\\|");
            utils.Log(logfile, "[ConfigWIP]getParamsWs()|" + ex.getMessage() + "|" + time[0] + "|" + time[1] + "|");
            return false;
        }
        return true;
    }

    private String getTimeError() {
        try {
            Calendar cal = new GregorianCalendar();
            Date date = cal.getTime();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String formatteDate = df.format(date);

            Date dt = new Date();
            SimpleDateFormat hf = new SimpleDateFormat("HH:mm:ss");
            String formatteHour = hf.format(dt.getTime());

            return formatteDate + "|" + formatteHour;
        } catch (Exception e) {
            utils.Log(logfile, "[ConfigWIP]getTimeError()|" + e + "|" + "2023-01-05" + "|" + "09:27:15" + "|");
            return "2023-01-05" + "|" + "09:27:15";
        }
    }
    /*private void seleccionUrl(int chk) { //Ax: el parámetro permite saber que check ha sido escogido para escribirlo 1 ó 2
        try {
            String nombreArchivo = VariablesGlobales.directorioactual + "/DIRECCIONEIP.TXT";
            File file = new File(nombreArchivo);

            if (!file.exists()) {
                RandomAccessFile writer2 = new RandomAccessFile(file, "rw");

                String ip_puerto = "200.21.186.74;83;000                 ;GPRS                ;/caqueta.asmx                                                                                                 ;A\r\n" +
                        "000.000.000.000;8081;000                 ;GPRS                ;/caqueta.asmx                                                                                                 ;I";

                writer2.writeBytes(ip_puerto);
                writer2.close();
            }

            if (file.exists()) {

                FileReader stream3 = new FileReader(nombreArchivo);
                BufferedReader reader = new BufferedReader(stream3);
                int cont = 0;
                String datoIP;

                while ((datoIP = reader.readLine()) != null) {

                    if (datoIP.substring(datoIP.length() - 1, datoIP.length()).trim().equals("A")) {

                        if (cont < 1) {
                            chk2 = false;
                            chk1 = true;
                            URL1 = "http://" + datoIP.substring(0, 15).trim() + ":" + datoIP.substring(16, 20).trim();
                            paginaWs1 = datoIP.substring(64, 157).trim();
                            chkIp1.setChecked(chk1);
                            chkIp2.setChecked(chk2);
                        } else {
                            chk2 = true;
                            chk1 = false;
                            URL2 = "http://" + datoIP.substring(0, 15).trim() + ":" + datoIP.substring(16, 20).trim();
                            paginaWs2 = datoIP.substring(64, 157).trim();
                            chkIp1.setChecked(chk1);
                            chkIp2.setChecked(chk2);
                        }
                    }
                    if (datoIP.substring(datoIP.length() - 1, datoIP.length()).trim().equals("I")) {

                        if (cont < 1) {
                            URL1 = "http://" + datoIP.substring(0, 15).trim() + ":" + datoIP.substring(16, 20).trim();
                            paginaWs1 = datoIP.substring(64, 157).trim();
                        } else {
                            URL2 = "http://" + datoIP.substring(0, 15).trim() + ":" + datoIP.substring(16, 20).trim();
                            paginaWs2 = datoIP.substring(64, 157).trim();
                        }
                    }
                    cont++;
                }

                if (cont < 1) {//no existen 2 lineas
                    file.delete();
                    seleccionUrl(0);
                    return;
                }
                reader.close();
            }

            URL1 = URL1.replace(" ", "");
            URL2 = URL2.replace(" ", "");

            if (chk1) {
                URL = URL1;
                paginaWs = paginaWs1;
            } else {
                URL = URL2;
                paginaWs = paginaWs2;
            }

        } catch (IOException e) {
            e.printStackTrace();
            mensajeT("Error, verificar Archivo de Configuracion IP", 1000);
        }
    }

    private void mostrarDatos() {

        chkIp1.setChecked(chk1);
        chkIp2.setChecked(chk2);

        try {
            String temp = URL.replace("http://", "").replace(":", ".");
            String[] segmentos = temp.split("\\.");

            txtIpWs1.setText(segmentos[0]);
            txtIpWs2.setText(segmentos[1]);
            txtIpWs3.setText(segmentos[2]);
            txtIpWs4.setText(segmentos[3]);
            txtPuerto.setText(segmentos[4]);

            txtTramas.setText(trama + "");
            txtDescServWeb.setText(paginaWs);

        } catch (Exception ex) {
            mensajeT("Error al mostrar datos\n" + ex.getMessage(),1000);
        }
    }

    private void grabarDatos() {

        try {
            if (chkIp1.isChecked() || chkIp2.isChecked()) {

                String ip = String.format("%1$3s", txtIpWs1.getText().toString().trim()) + "." + String.format("%1$3s", txtIpWs2.getText().toString().trim()) + "."
                        + String.format("%1$3s", txtIpWs3.getText().toString().trim()) + "." + String.format("%1$3s", txtIpWs4.getText().toString().trim()) + ";"
                        + String.format("%1$4s", txtPuerto.getText().toString().trim()) + ";"
                        // + String.format("%1$20s", txtCiclo.getText()) + ";"
                        + "000                 " + ";" + "GPRS                " + ";" +
                        String.format("%1$-109s", "/" + txtDescServWeb.getText().toString().trim()) + ";A";

                String url = ip.substring(0, 15).trim();

                if (Patterns.IP_ADDRESS.matcher(url.replace(" ", "")).matches()) {//Ax. comprueba ip valida

                    trama = txtTramas.getText().toString().trim();
                    puerto = txtPuerto.getText().toString().trim();
                    URL = ip;//Temporalmente URL lleva toda esa cadena

                    if (chkIp1.isChecked()) {
                        chk1 = true;
                        chk2 = false;
                        // seleccionUrl(1);
                    } else {
                        chk1 = false;
                        chk2 = true;
                        //  seleccionUrl(2);
                    }

                    String nombreArchivo = VariablesGlobales.directorioactual + "/DIRECCIONEIP.TXT";
                    FileReader stream = new FileReader(nombreArchivo);
                    BufferedReader reader = new BufferedReader(stream);
                    int cont = 0;
                    String datoIP, temp = "";

                    while ((datoIP = reader.readLine()) != null) {

                        if (cont == 0) {
                            if (chk1) temp = ip.replace(";I", ";A") + "\r\n";
                            else temp = datoIP.replace(";A", ";I") + "\r\n";
                        }

                        if (cont == 1) {
                            if (chk2) temp += ip.replace(";I", ";A");
                            else temp += datoIP.replace(";A", ";I");
                        }
//                        if (chk1 && cont == 1) {
//                            temp = ip + "\r\n" + datoIP.replace(";A",";I");
//                        }
//                        if (chk2 && cont == 0) {
//                            temp = datoIP.replace(";A",";I") + "\r\n" + ip;
//                        }
                        cont++;
                    }

                    utils.WriteLine(new File(nombreArchivo),temp);
                    seleccionUrl(1);
                    mostrarDatos();
                    return;
                }
                mensajeT("IP incorrecta", 1000);
                lblTransmite.setText("IP incorrecta");
                return;
            }
            mensajeT("Chequee una casilla", 1000);
        } catch (Exception ex) {
            mensajeT("ERROR AL GUEARDAR", 1000);
        }
    }*/

    public void processFinish(String x) {
    }

    private class AsyncCallWS extends AsyncTask<String, Integer, Void> {
        public String respuesta;
        ProgressDialog pDialog;
        int x = 0;

        @Override
        protected Void doInBackground(String... params) {
            // Log.i(TAG, "doInBackground");

            WSSoap wsoap = new WSSoap(URL, paginaWs);
            respuesta = wsoap.verificarWs("VALIDAR_CONEXION");

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            pDialog.dismiss();

            try {
                Integer.parseInt(respuesta);
                iconWebSoff.setVisibility(View.INVISIBLE);
                iconWebSon.setVisibility(View.VISIBLE);
                pruebawsok = true;
                lblTransmite.setText("Conexion con Webservice OK");

            } catch (NumberFormatException e) {
                iconWebSon.setVisibility(View.INVISIBLE);
                iconWebSoff.setVisibility(View.VISIBLE);
                pruebawsok = false;
                lblTransmite.setText("Error de conexion con Webservice\n" + respuesta);
            }

            btnPruebaWs.setEnabled(true);
        }

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(Configurar_webIp.this);
            pDialog.setMessage("Probando conexion...");
            pDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
        }
    }

    private void mensajeT(String msg, int dur) { //Ax: 1 segundo: 1000

        toast = Toast.makeText(Configurar_webIp.this, msg, dur);
        toast.setGravity(Gravity.TOP, 10, 170);
        toast.setDuration(dur);
        toast.show();

    }

    public void enableDisableMobileData() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity"));
        startActivity(intent);
    }
}
