package com.gselectroCaqueta.comunicaciones;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SoapArrays extends AsyncTask {

    public static final String WS_NAMESPACE = "http://tempuri.org/";
    public static String WS_METHOD_NAME;
    public static String WS_URL_PAGE = "";
    public static String WS_URL = "";
    public static String WS_PAGE = "";

    String imei = "";
    String cuenta = "";
    String fechahora = "";
    String coordenadax = "";
    String coordenaday = "";
    String numerosatelites = "";
    String fechasatelites = "";

    String lector = "";
    String tipoOperador = "";
    String ciclo = "";
    String ruta = "";

    String proceso = "";

    Context contexto;
    boolean control = false;
    String respuesta = "";


    @Override
    protected Object doInBackground(Object[] objects) {

        proceso = objects[0].toString();

        if (proceso.equals("enviarP")) {
            Log.e("INFO SOAPARRAY", "envia pos ");

            contexto = (Context) objects[1];
            WS_METHOD_NAME = objects[4].toString();
            WS_URL = objects[2].toString();
            WS_PAGE = objects[3].toString();
            WS_URL_PAGE = objects[2].toString() + "/" + objects[3].toString();

            Log.e("error",WS_URL+" data url "+WS_PAGE);
            imei = objects[5].toString();
            cuenta = objects[6].toString();
            fechahora = objects[7].toString();
            coordenadax = objects[8].toString();
            coordenaday = objects[9].toString();
            numerosatelites = objects[10].toString();
            fechasatelites = objects[11].toString();
            lector = objects[12].toString();
            ciclo = objects[14].toString();
            ruta = objects[15].toString();

            Log.e("INFO SOAPARRAY", "|" + imei+ "|" + cuenta+ "|" + fechahora+ "|" + coordenadax+ "|" + coordenaday+ "|" + numerosatelites+ "|" + fechasatelites+ "|" + lector+ "|" + respuesta+ "|" + ciclo+ "|" + ruta);

            // Log.e("error","datos "+WS_METHOD_NAME+" "+WS_URL_PAGE);

            // final MenuPrincipal load =  (MenuPrincipal)params[1];

            //datosS =   bdSusp.selectMinga_Drs_Suspensions6();


            SoapObject request = new SoapObject(WS_NAMESPACE, WS_METHOD_NAME);

            SimpleDateFormat parseador = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
            SimpleDateFormat formateador = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
            Date dateA = null;
            Date dateG = null;

           /* try {
                dateA = parseador.parse(datosS[i][2].replace("-", " "));
                //  dateG = formateador.parse(datosS[i][21].replace("-", " "));
                Log.e("error", "fecha parseada " + formateador.format(dateA));
            } catch (Exception e) {
                Log.e("error", "fecha parseada1 " + e.getMessage());

            }*/

            PropertyInfo p = new PropertyInfo(); // Añadir parametros
            p.setName("serialTerminal1");
            p.setValue(imei);
            p.setType(String.class);
            request.addProperty(p);

            PropertyInfo p2 = new PropertyInfo();
            p2.setName("codCuenta1");
            p2.setValue(cuenta);
            p2.setType(String.class);
            request.addProperty(p2);

            PropertyInfo p3 = new PropertyInfo();
            p3.setName("fechaHora1");
            p3.setValue(fechahora);//cambiar era para pruebas fechahora "23/04/2019 10:37:23"
            p3.setType(String.class);
            request.addProperty(p3);
            Log.e("error", "action date " + dateA);

            PropertyInfo p4 = new PropertyInfo();
            p4.setName("coordenadax1");
            p4.setValue(coordenadax.replace(".",","));
            p4.setType(String.class);
            request.addProperty(p4);

            PropertyInfo p5 = new PropertyInfo();
            p5.setName("coordenaday1");
            p5.setValue(coordenaday.replace(".",","));
            p5.setType(String.class);
            request.addProperty(p5);

            PropertyInfo p6 = new PropertyInfo();
            p6.setName("numeroSatelites1");
            p6.setValue(numerosatelites);
            p6.setType(String.class);
            request.addProperty(p6);

            PropertyInfo p7 = new PropertyInfo();
            p7.setName("fechaSatelite1");
            p7.setValue(fechasatelites);
            p7.setType(String.class);
            request.addProperty(p7);

            PropertyInfo p8 = new PropertyInfo();
            p8.setName("codoperador");
            p8.setValue(lector);
            p8.setType(String.class);
            request.addProperty(p8);

            PropertyInfo p9 = new PropertyInfo();
            p9.setName("tipooperador");
            p9.setValue(tipoOperador);
            p9.setType(String.class);
            request.addProperty(p9);

            PropertyInfo p10 = new PropertyInfo();
            p10.setName("ciclo1");
            p10.setValue(ciclo);
            p10.setType(String.class);
            request.addProperty(p10);

            PropertyInfo p11 = new PropertyInfo();
            p11.setName("ruta1");
            p11.setValue(ruta);
            p11.setType(String.class);
            request.addProperty(p11);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

            envelope.dotNet = true;

            envelope.setOutputSoapObject(request);

            HttpTransportSE httpTransport = new HttpTransportSE(WS_URL_PAGE);

            try {
                httpTransport.debug = true;

                httpTransport.call((WS_NAMESPACE + WS_METHOD_NAME), envelope);//  Enviando la petición al web service

                Object response = (Object) envelope.getResponse();
                respuesta = response.toString();
                Log.e("INFO SOAPARRAY", "entra true " + respuesta);


            } catch (IOException | XmlPullParserException e) {
                respuesta = e.getMessage();
                Log.e("INFO SOAPARRAY", "entra 2 " + e.getMessage());
            } catch (Exception e) {
                respuesta = e.getMessage();
                Log.e("INFO SOAPARRAY", "entra 3 " + e.getMessage());
            }


        }

        return null;
    }
}
