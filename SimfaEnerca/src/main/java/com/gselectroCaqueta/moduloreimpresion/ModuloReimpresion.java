package com.gselectroCaqueta.moduloreimpresion;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gselectroCaqueta.accesoyseguridad.R;
import com.gselectroCaqueta.accesoyseguridad.VariablesGlobales;
import com.gselectroCaqueta.comunicaciones.BDComunicaciones;
import com.gselectroCaqueta.comunicaciones.CrudComunicaciones;
import com.gsutil.Utils;
import com.gsutil.WSSoap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Calendar;

public class ModuloReimpresion extends AppCompatActivity {

    Button btnVolver;
    Button btnTraerArchivo;
    Button btnImprimir;
    EditText txtCuenta;
    EditText txtAno;
    EditText txtMes;

    String rutaAdministrador = "";
    String esComprimido = "";
    String datoIP = "";
    public String URL;
    String paginaWs = "";
    String NombreZip = "";
    String cuenta = "";
    String ano = "";
    String mes = "";
    String ArchivoAEnviarRecibir = "";
    String RutaAGrabar = "";
    String metodo = "";
    int tamanoArchivo = 0;
    int trama = 50;
    Utils util = new Utils();
    public File logfile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modulo_reimpresion);
        btnVolver = (Button)findViewById(R.id.btnVolver);
        btnTraerArchivo = (Button)findViewById(R.id.btnTraerArchivo);
        btnImprimir = (Button)findViewById(R.id.btnImprimir);
        txtCuenta = (EditText) findViewById(R.id.txtCuenta);
        txtAno = (EditText) findViewById(R.id.txtAno);
        txtMes = (EditText) findViewById(R.id.txtMes);

        logfile = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/LOGEVENTOS.LOG");

        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnTraerArchivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recibirArchivo1();
            }
        });

        btnImprimir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (VariablesGlobales.habilitadaimpresora == 0) {
                    mensajes("Alerta de Impresora\n" + "La impresora se encuentra\n" + "Deshabilitada");
                }
                else
                procedimientoDeImpresionPagina(0);
            }
        });

        verificarArchivo();
    }

    private void verificarArchivo() {  //Axx:? no tengo idea que trataba de hacer no coincide con el codigo original se deja

        String nombreArchivo = VariablesGlobales.directorioactual + "/ARCHIVOSCARGA.CFI";
        File file = new File(nombreArchivo);

        //Ax: no existe "ArchivosCarga.cfi" advertir y no hacer nada
        if (!file.exists()) {

            mensajes("No hay Archivo de Soporte...\n //ArchivosCarga.cfi");
            return;
        }

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

    /*private void seleccionUrl() {
        try {
            String nombreArchivo = VariablesGlobales.directorioactual + "/DIRECCIONEIP.TXT";
            File file = new File(nombreArchivo);

            if (!file.exists()) {
                RandomAccessFile writer2 = new RandomAccessFile(file, "rw");

                String ip_puerto = "000.000.000.000;8080;000                 ;GPRS                ;/caqueta.asmx                                                                                                 ;A\r\n" +
                        "000.000.000.000;8081;000                 ;GPRS                ;/caqueta.asmx                                                                                                 ;I";

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


    private void mensajes(String msg) {
        Toast toast = Toast.makeText(ModuloReimpresion.this, msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 10, 50);
        toast.show();
    }

    public void recibirArchivo1(){

        cuenta = txtCuenta.getText().toString().trim();
        ano =  txtAno.getText().toString().trim();
        mes = txtMes.getText().toString().trim();

        NombreZip = cuenta+"_" + ano + "_" + mes+ ".LOG"; //+ ".ZIP";
        ArchivoAEnviarRecibir = rutaAdministrador.trim() + "Backuplbl"  + "\\" + NombreZip;
        RutaAGrabar = VariablesGlobales.directorioactual + "/LBLS/"  + NombreZip;
        WsGetTamanoArchivo(NombreZip, ArchivoAEnviarRecibir, RutaAGrabar);

    }

    public void recibirArchivo2(){

        metodo = "ENVIAR_DEL_SERVIDOR_AL_PDA";
        AsyncCallWS task = new AsyncCallWS();
        task.execute("");//Ax: continua aqui, en AsyncCallWS.doinback...

    }

    public void WsGetTamanoArchivo(String nombre, String archivoARecibir, String rutaAGrabar) {

        try {
            metodo = "OBTENER_LONGITUD_ARCHIVO";

            AsyncCallWS task = new AsyncCallWS();
            task.execute("");//Ax: continua aqui, en AsyncCallWS.doinback...

        } catch (Exception ex) {
            Log.e("error","longitud4 "+ex.getMessage());
            mensajes("Error al Obtener Tamano de Archivo");
           }
    }


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

                    case "OBTENER_LONGITUD_ARCHIVO":
                        Log.e("error","longitud d "+metodo+"-"+ArchivoAEnviarRecibir+"-"+esComprimido);
                        respuesta = wsoap.ObtenerLongitudArchivo(metodo, ArchivoAEnviarRecibir, Integer.parseInt(esComprimido), "0", "0");
                        break;
                    case "ENVIAR_DEL_SERVIDOR_AL_PDA":
                        respuesta = wsoap.ObtenerArchivo(metodo, ArchivoAEnviarRecibir, RutaAGrabar, trama, tamanoArchivo);
                        break;


                }

            } catch (Exception e) {
                respuesta = "Error, valide conexion con Ws";

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            pDialog.dismiss();
            String msg = "";
            boolean bul = true;

            switch (metodo) {


                case "OBTENER_LONGITUD_ARCHIVO":
                    metodo = "";
                    try {
                        int x = Integer.parseInt(respuesta);
                        tamanoArchivo = x;
                        Log.e("error","longitud3 "+respuesta);
                        if (x > 0) { //Ax: Puede responder -1,-2-0
                           recibirArchivo2();
                                    break;

                        } else {
                            msg = "Error al obtener Longitud de archivo.";
                            tamanoArchivo = 0;
                        }

                    } catch (NumberFormatException ex) {
                        msg = respuesta;
                        tamanoArchivo = 0;
                    }
                    break;
                case "ENVIAR_DEL_SERVIDOR_AL_PDA":

                    if (respuesta.length() == 1) {//"1" bajados sin borrado "2" bajado y borrado
                        msg = "Datos cargados ✓;   " + "; ";

                    //    String zipMsg = util.DescomprimeZip(RutaAGrabar, "", tamanoArchivo, true); //Ax: descomprimir archivos

                    //    msg += zipMsg + "; ";

                      //  if (zipMsg.contains("Descomprime ✓")) {

                            //Axx: aqui iba el cambio de hora y fecha, se salta este paso (pendiente todo)
                            //String movarch = util.MoverArchivosPorTipo(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/", VariablesGlobales.directorioactual + "/DATOSDESALIDA/", ".SDA");//Ax: Mover Archivos Por extension
                            //msg += movarch + "; ";

                    //    }

                    } else {
                        msg = "Error: " + respuesta;
                    }
                    //metodo = "";
                    break;


                default:
                    msg = respuesta;

                    if (respuesta.contains("✓")) {
                      }
                    break;
            }

            if (!msg.isEmpty()) {
                mensajes(msg);
            }
            //banderaMet2llamo = "";
            respuesta = "";
        }

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(ModuloReimpresion.this);
            pDialog.setMessage("cargando...");
            pDialog.setCancelable(false);
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
        }
    }

    private int procedimientoDeImpresionPagina(int dato) {
        Log.e("errror", "entra a imprimir");
        String imprimira;
        String enviara2;
        String enviara3 = "";
        String codigobarras1 = "";
        String codigobarras2 = "";
        String codigobarras3 = "";
        String codigobarras4 = "";
        String fileName;
        int tamanno = 59;
        int limiteBarras = 60;//84


        File file = new File(VariablesGlobales.directorioactual + "/LBLS/"+NombreZip);

        if (!file.exists()) {
            mensajes("No existe el archivo de Impresion" + file.getAbsolutePath());
            return 0;
        }

        try {

            if (dato == 0) {
                fileName = VariablesGlobales.directorioactual + "/LBLS/"+NombreZip;
                //antes+ "/DatosDeEntrada/imprimir.log";

                FileReader leerImp = new FileReader(fileName);
                BufferedReader reader = new BufferedReader(leerImp);
                String linea;

                int contar = 1;
                imprimira = "";

                while ((linea = reader.readLine()) != null) {

                    if (contar == limiteBarras) {
                        enviara2 = linea;
                        codigobarras1 = enviara2.substring(0, tamanno);
                        codigobarras2 = enviara2.substring(tamanno, enviara2.length()) + "\r\n";
//                        codigobarras3 = enviara2.substring(0, tamanno);
//                        codigobarras4 = enviara2.substring(tamanno, enviara2.length()  ) + "\r\n";
                    } else if (contar == limiteBarras + 1 && tamanno == 59) {
                        enviara2 = linea;
                        codigobarras3 = enviara2.substring(0, tamanno);
                        codigobarras4 = enviara2.substring(tamanno, enviara2.length()) + "\r\n";
                    } else if (contar < limiteBarras)
                        imprimira += linea + "\r\n";
                    else
                        enviara3 += linea + "\r\n";

                    ++contar;
                }

                reader.close();

                if (imprimira.length() > 0 || enviara3.length() > 0 || codigobarras1.length() > 0 || codigobarras3.length() > 0) {
                    Log.e("error", "puerto 1");
                    enviar_al_puerto(imprimira, enviara3, codigobarras1, codigobarras2, codigobarras3, codigobarras4);
                    codigobarras1 = "";
                    codigobarras2 = "";
                    codigobarras3 = "";
                    codigobarras4 = "";
                    enviara3 = "";
                }
            } else {
                fileName = VariablesGlobales.directorioactual + "/LBLS/"+NombreZip;
                //antes+ "/DATOSDEENTRADA/IMPRIMIR.LOG";
                RandomAccessFile reader = new RandomAccessFile(fileName, "rw");

                int contar = 0;
                imprimira = "";
                codigobarras1 = "";
                codigobarras2 = "";
                codigobarras3 = "";
                codigobarras4 = "";
                enviara3 = " ";
                reader.seek(0);

                for (int i = 1; i < reader.length(); i++) {
                    imprimira += reader.readLine() + "\r\n";
                    ++contar;
                    if (contar >= 10) {
                        Log.e("error", "puerto 2");
                        enviar_al_puerto(imprimira, enviara3, codigobarras1, codigobarras2, codigobarras3, codigobarras4);
                        contar = 0;
                        imprimira = "";
                        codigobarras1 = "";
                        codigobarras2 = "";
                        codigobarras3 = "";
                        codigobarras4 = "";
                        enviara3 = "";
                    }
                    i = i + reader.readLine().length();
                }

                reader.close();

                if (imprimira.length() > 0 || enviara3.length() > 0 || codigobarras1.length() > 0) {
                    Log.e("error", "puerto 3");
                    enviar_al_puerto(imprimira, enviara3, codigobarras1, codigobarras2, codigobarras3, codigobarras4);
                    codigobarras1 = "";
                    codigobarras2 = "";
                    codigobarras3 = "";
                    codigobarras4 = "";
                    enviara3 = " ";
                }
            }
            Calendar calendar2 = Calendar.getInstance();


        } catch (Exception e) {
            Log.e("error", "no imprime " + e.getMessage());
            util.Log(logfile, "procedimientoDeImpresionPagina " + e.getMessage());
            mensajes("Error en envio de Impresion de la Factura");
        }
        return 1;
    }


    private void enviar_al_puerto(String imprime, String imprime2, String barras1, String barras2, String barras3, String barras4) {

        int i;
        try {
            byte[] outputData;
            byte[] outputData2 = new byte[1];
            outputData2[0] = (byte) 134;

            int tamano = imprime.length();
            String trozo;

            for (i = 0; i <= imprime.length(); ) {
                if (tamano >= 35)
                    trozo = imprime.substring(i, (i + 35));
                else
                    trozo = imprime.substring(i, (i + tamano));
                outputData = trozo.getBytes();

                tamano -= 35;
                i = i + 35;
                VariablesGlobales.btPrintService.write(outputData);
            }
            if (tamano > 0) {
                i = i - 35;
                outputData = imprime.substring(i, (i + tamano)).getBytes();
                VariablesGlobales.btPrintService.write(outputData);
            }
            if (barras1.length() > 0) {
                outputData = barras1.getBytes();
                VariablesGlobales.btPrintService.write(outputData);
                // TODO VariablesGlobales.btPrintService.write(outputData2, 0,
                // 1);
                VariablesGlobales.btPrintService.write(outputData2);
                outputData = barras2.getBytes();
                VariablesGlobales.btPrintService.write(outputData);
            }
            if (barras3.length() > 0) {
                outputData = barras3.getBytes();
                VariablesGlobales.btPrintService.write(outputData);
                // TODO VariablesGlobales.btPrintService.write(outputData2, 0,
                // 1);
                VariablesGlobales.btPrintService.write(outputData2);
                outputData = barras4.getBytes();
                VariablesGlobales.btPrintService.write(outputData);
            }

            // envio cadena dos del sitema donde esta el cupon
            byte[] outputData3;

            tamano = imprime2.length();

            for (i = 0; i <= imprime2.length(); ) {
                if (tamano >= 35)
                    trozo = imprime2.substring(i, (i + 35));
                else
                    trozo = imprime2.substring(i, (i + tamano));

                outputData3 = trozo.getBytes();
                tamano -= 35;
                i = i + 35;

                VariablesGlobales.btPrintService.write(outputData3);
            }

            if (tamano > 0) {
                i = i - 35;

                outputData3 = imprime2.substring(i, (i + tamano)).getBytes();

                VariablesGlobales.btPrintService.write(outputData3);
            }
        } catch (Exception ex) {
            mensajes("Problemas enviando a la impresora " + ex.toString());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();

    }
}
