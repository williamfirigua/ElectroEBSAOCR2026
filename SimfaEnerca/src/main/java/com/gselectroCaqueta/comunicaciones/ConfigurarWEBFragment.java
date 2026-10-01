package com.gselectroCaqueta.comunicaciones;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.gselectroCaqueta.accesoyseguridad.R;
import com.gselectroCaqueta.accesoyseguridad.VariablesGlobales;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ConfigurarWEBFragment extends Fragment {

    public String URL;
    public Boolean redVisible;
    OnHeadSelectedListener mCallBack;
    Button btnPruebaWS;
    Button cmdAceptar;
    CheckBox chkIP1;
    CheckBox chkIP2;
    EditText txtUno;
    EditText txtDos;
    EditText txtTres;
    EditText txtCuatro;
    EditText txtPuerto;
    EditText txtValorTrama;
    EditText txtDescripcion;
    TextView lblTransmite;
    String datoIP = "";
    String mensaje = "";
    String valorRetorno = "";
    String strTxtCiclo;
    private String TAG = "ConfigurarWEBFragment";
    private Boolean chk1;
    private Boolean chk2;
    OnClickListener buttonListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

            int id = v.getId();

            switch (id) {

                case R.id.cmdAceptar:

                    grabarIPPublica();

                    break;

                case R.id.btnPruebaWS:

                    AsyncCallWS task = new AsyncCallWS();
                    // Call execute
                    task.execute();

                    break;
                default:
                    break;
            }
            mCallBack.onArticleSelected(v);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.configurar_web_layout, container, false);

        btnPruebaWS = (Button) view.findViewById(R.id.btnPruebaWS);
        cmdAceptar = (Button) view.findViewById(R.id.cmdAceptar);
        chkIP1 = (CheckBox) view.findViewById(R.id.chkIP1);
        chkIP2 = (CheckBox) view.findViewById(R.id.chkIP2);

        txtUno = (EditText) view.findViewById(R.id.txtUno);
        txtDos = (EditText) view.findViewById(R.id.txtDos);
        txtTres = (EditText) view.findViewById(R.id.txtTres);
        txtCuatro = (EditText) view.findViewById(R.id.txtCuatro);
        txtPuerto = (EditText) view.findViewById(R.id.txtPuerto);
        txtValorTrama = (EditText) view.findViewById(R.id.txtValorTrama);
        txtDescripcion = (EditText) view.findViewById(R.id.txtDescripcion);
        lblTransmite = (TextView) view.findViewById(R.id.lblTransmite);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        btnPruebaWS.setOnClickListener(buttonListener);
        cmdAceptar.setOnClickListener(buttonListener);

        // txtUno.setText(VariablesGlobales.WS_URL.substring(7,10));
        // txtDos.setText(VariablesGlobales.WS_URL.substring(11,13));
        // txtTres.setText(VariablesGlobales.WS_URL.substring(14,15));
        // txtCuatro.setText(VariablesGlobales.WS_URL.substring(16,18));
        // txtPuerto.setText(VariablesGlobales.WS_URL.substring(19,21));
        // txtDescripcion.setText(VariablesGlobales.WS_URL.substring(22));
    }

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);

        Activity a = null;

        if (context instanceof Activity) {

            a = (Activity) context;
        }

        // Nos aseguramos que la actividad contenedora halla recibido la
        // interfaz de retrollamada
        // Sino lanzamos una excepcion.
        try {

            mCallBack = (OnHeadSelectedListener) a;
        } catch (ClassCastException e) {

            throw new ClassCastException(a.toString() + " debe implementar OnHeadSelectedListener");
        }
    }

    private void grabarIPPublica() {

        String ip1 = "";
        String ip2 = "";
        String nombreArchivo = VariablesGlobales.directorioactual + "/DIRECCIONEIP.TXT";
        File file = new File(nombreArchivo);

        // OJO AQUI Load DE LAS DOS REDES

        try {
            if (file.exists()) {

                FileReader stream3 = new FileReader(nombreArchivo);
                BufferedReader reader = new BufferedReader(stream3);
                String linea = "";

                while ((linea = reader.readLine()) != null) {

                    if (ip1.trim().equals("")) {
                        ip1 = linea;
                    } else {
                        ip2 = linea;
                    }
                }

                reader.close();
                file.delete();
            }

            RandomAccessFile writer2 = new RandomAccessFile(nombreArchivo, "rw");

            String ip_puerto = "";
            if (ip2.equals("")) {
                ip2 = ip1;
            }

            ip_puerto = String.format("%1$3s", txtUno.getText().toString()) + "." + String.format("%1$3s", txtDos.getText().toString()) + "."
                    + String.format("%1$3s", txtTres.getText().toString()) + "." + String.format("%1$3s", txtCuatro.getText().toString()) + ";"
                    + String.format("%1$4s", txtPuerto.getText()) + ";"
                    // + String.format("%1$20s", txtCiclo.getText()) + ";"
                    + "WIFI                " + ";" + String.format("%1$115s", txtDescripcion.getText()) + ";A;\r\n";

            ip1 = ip_puerto;
            // ip2 = ip2.substring(0, ip2.length() - 2) + "I;";

            writer2.writeBytes(ip1);
            writer2.writeBytes(ip2);
            writer2.close();

        } catch (FileNotFoundException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }

        seleccionUrl();

    }

    private void seleccionUrl() {
        try {
            String nombreArchivo = VariablesGlobales.directorioactual + "/DIRECCIONEIP.TXT";
            File file = new File(nombreArchivo);

            // http://200.21.4.68:85/ServicioLecturasHuila.asmx
            if (!file.exists()) {
                RandomAccessFile writer2 = new RandomAccessFile(file, "rw");

                //String ip_puerto = "200." + " 21." + "  4." + " 68" + ";" + "85  ;" + "000                 " + ";" + "GPRS                " + ";"
                String ip_puerto = "186." + " 82." + "207." + " 35" + ";" + "93  ;" + "000                 " + ";" + "GPRS                " + ";"
                        + "/ServicioLecturasHuilX.asmx                                                                                        " + ";A";

                writer2.writeBytes(ip_puerto);

                // crear la segunda linea de la ip wifi
                ip_puerto = "190." + "144." + " 234" + " 45" + ";" + "85  ;" + "000                 " + ";" + "GPRS                " + ";"
                        + "/ServicioLecturasHuilX.asmx                                                                                        " + ";I";

                writer2.writeBytes(ip_puerto);

                writer2.close();
            }

            if (file.exists()) {

                FileReader stream3 = new FileReader(nombreArchivo);
                BufferedReader reader = new BufferedReader(stream3);
                String linea = "";

                int contar = 0;

                while ((linea = reader.readLine()) != null) {

                    datoIP = linea;

                    if (datoIP.substring(datoIP.length() - 2, datoIP.length() - 1).trim().equals("A")) {
                        if (contar == 0) {
                            chk2 = false;
                            chk1 = true;
                        } else {
                            chk2 = true;
                            chk1 = false;
                        }
                        break;
                    } else {
                        if (contar == 0) {
                            chk2 = true;
                            chk1 = false;
                        }
                    }
                    contar++;
                }
                reader.close();
            }
            URL = "http://" + datoIP.substring(0, 15).trim() + ":" + datoIP.substring(16, 20).trim();

            if (datoIP.length() > 178) {

                URL += "/" + datoIP.substring(131, 157).trim();
            } else {

                URL += "/" + datoIP.substring(63, 157).trim();
            }

            URL = URL.replace(" ", "");
        } catch (IOException e) {
            e.printStackTrace();
            // MessageBox.Show("Verificar Archivo de Configuracion IP");
        }

    }

    // nuevo proceso para capturar la ippublica

    private String pruebaWebService() {

        try {
            // btnCargar.setEnabled(true);
            // btnDescargar.setEnabled(true);
            seleccionUrl();

            // Crear peticion
            SoapObject request = new SoapObject(VariablesGlobales.WS_NAMESPACE, VariablesGlobales.WS_METHOD_NAME);
            // Crear envoltura */
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            // Configuracion objeto SOAP
            envelope.setOutputSoapObject(request);
            // Crear objeto de llamada HTTP
            // PRUEBA HttpTransportSE androidHttpTransport = new
            // HttpTransportSE(
            // VariablesGlobales.WS_URL);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

            try {
                // Llamar web service
                androidHttpTransport.call(VariablesGlobales.WS_VALIDAR_CONEXION, envelope);
                // Obtener respuesta
                SoapObject response = (SoapObject) envelope.getResponse();
                // Asignar valor obtenido
                valorRetorno = response.getProperty(0).toString();

            } catch (Exception e) {
                e.printStackTrace();
                return "error";
            }

            if (null == valorRetorno || valorRetorno.trim().equals("")) {
                // MessageBox.Show("Usuario no Existe");
                mensaje = "El sistema NO tiene conexion";
                // redactiva.Visible = false;
                // redinativa.setVisibility(ImageView.VISIBLE);
                redVisible = false;
            } else {
                // MessageBox.Show("El sistema tiene coneccion a la Base de datos");
                // lblTransmite.setText("El sistema con conexion");
                mensaje = "El sistema con conexion, respuesta = " + valorRetorno;
                // redactiva.Visible = true;
                // redinativa.setVisibility(ImageView.INVISIBLE);
                redVisible = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
            // MessageBox.Show("No hay SW\n"+URL);
            lblTransmite.setText("Servicio WEB ERRADO");
            // redactiva.Visible = false;
            // redinativa.setVisibility(ImageView.VISIBLE);
            redVisible = false;
            return "error";
        }
        return ("ok");
    }

    // fin captura de la ip publica

    public boolean esVisibleRed() {
        return redVisible;
    }

    // AsyncTask para gestionar las peticiones al WS

    // La activida contenedora debe implementar esta interface
    public interface OnHeadSelectedListener {

        public void onArticleSelected(View v);
    }

    private class AsyncCallWS extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            Log.i(TAG, "doInBackground");
            return pruebaWebService();
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, "onPostExecute");

            if (result.equals("error")) {

                lblTransmite.setText(result);
                return;
            } else {
                lblTransmite.setText(mensaje);

                chkIP1.setChecked(chk1);
                chkIP2.setChecked(chk2);

                txtUno.setText(datoIP.substring(0, 3));
                txtDos.setText(datoIP.substring(4, 7));
                txtTres.setText(datoIP.substring(8, 11));
                txtCuatro.setText(datoIP.substring(12, 15));
                txtPuerto.setText(datoIP.substring(16, 20));
                if (datoIP.length() > 178) {

                    txtDescripcion.setText(datoIP.substring(131, 157).trim());
                } else {

                    txtDescripcion.setText(datoIP.substring(63, 157).trim());
                }
            }
        }

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "onPreExecute");
            lblTransmite.setText("Solicitando respuesta...");
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.i(TAG, "onProgressUpdate");
        }

    }
}