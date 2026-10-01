package com.gselectroCaqueta.modulochat;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.gselectroCaqueta.accesoyseguridad.MenuDeLiquidacion;
import com.gselectroCaqueta.accesoyseguridad.R;
import com.gsutil.AsyncResponse;
import com.gsutil.DatosWS;
import com.gsutil.WSSoap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class Insertar extends AppCompatActivity implements AsyncResponse, View.OnClickListener {

    final File sdCard = Environment.getExternalStorageDirectory();
    String metodo = "";
    int procesandoenvioenHilos = 0; //VAR GLOBAL
    String URL = "";
    String paginaWs = "";
    DatosWS wsoap = new DatosWS("", "", "", 0);//Axx
    ListView usuario = null;
    EditText asunto = null;
    EditText mensaje = null;
    Button enviar = null;
    Button recibidos = null;
    Button volver = null;
    String[] todo = null;
    List<String> arrayA = new ArrayList<String>();
    String operario = "";
    String list = "";
    String[] codigo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insertar);
        usuario = (ListView) findViewById(R.id.usuario);
        asunto = (EditText) findViewById(R.id.asunto);
        mensaje = (EditText) findViewById(R.id.mensaje);
        enviar = (Button) findViewById(R.id.enviar);
        recibidos = (Button) findViewById(R.id.recibidos);
        volver = (Button) findViewById(R.id.volver);
        enviar.setOnClickListener(this);
        recibidos.setOnClickListener(this);
        volver.setOnClickListener(this);
        usuario.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);

        Bundle bundle = getIntent().getExtras();
        operario = bundle.getString("operario");
        URL = bundle.getString("url");
        paginaWs = bundle.getString("pagws");
        Log.e("operario", URL + " " + paginaWs);
        //lectura de archivo plano para llenar la lista de usuarios
        File file = new File(sdCard.getAbsolutePath(), "/DATOSDEENTRADA/LECTOR.TXT");

        if (file.exists()) {
            try {
                FileInputStream fIn = new FileInputStream(file);
                InputStreamReader archivo = new InputStreamReader(fIn);
                BufferedReader br = new BufferedReader(archivo);
                String linea = br.readLine();
                while (linea != null) {
                    todo = linea.split(",");
                    linea = br.readLine();
                    //se crea un arraylist para asignarle los datos que se recuperan del archivo plano
                    arrayA.add(todo[0] + "-" + todo[2]);

                }
                br.close();
                archivo.close();

            } catch (IOException e) {
                Log.e("error", e.getMessage());

            }

            ArrayAdapter<String> data = new ArrayAdapter<String>(this, R.layout.fila, R.id.titulo, arrayA);
            //Le asigno ese adapter a la lista
            usuario.setAdapter(data);

        } else {
            Log.e("error", "no existe");
        }


        wsoap.delegate = this;//Axx Ax: tarea asincrona para enviar datos aqui a processFinish


    }



    @Override
    public void onClick(View v) {

        Intent miIntent = new Intent();
        if (v.getId() == R.id.enviar) {

            //Valida la logintud de caracteres
            int longi = mensaje.getText().toString().length() + asunto.getText().toString().length();
            //se recuperan los item selecionados del listview
            SparseBooleanArray seleccionados = usuario.getCheckedItemPositions();
            for (int i = 0; i <= usuario.getAdapter().getCount(); i++) {
                if (seleccionados.get(i)) {
                    //se hace el split de los item para guardar solo el codigo
                    codigo = usuario.getAdapter().getItem(i).toString().split("-");
                    list += codigo[0] + ";";
                }

            }
            if (longi >= 200) {
                alertMensaje("Solo se aceptan 200 caracteres");
            }
            //valida que el asunto no este vacio
            else if (asunto.getText().toString().equals("")) {
                alertMensaje("Debe ingresr el asunto");

            }
            //valida que el campo mensaje no este vacio
            else if (mensaje.getText().toString().equals("")) {
                alertMensaje("Debe ingresr el mensaje");

            }
            //se valida que la lista de usuario seleccionados no este vacia
            else if (list == "") {
                alertMensaje("Debe seleccionar un suario");
            } else {
                insertarMen();
            }
        }
        if (v.getId() == R.id.recibidos) {
            // se crea un intent que nos va a llevar a la actividad
            miIntent.setClass(this, Recibidos.class);
            startActivity(miIntent);

        }
        if (v.getId() == R.id.volver) {
            // se crea un intent que nos va a llevar a la actividad
            miIntent.setClass(this, MenuDeLiquidacion.class);
            setResult(Activity.RESULT_OK, miIntent);
            finish();
        }

    }

    public void insertarMen() {
        try {

            metodo = "CHATINSERTMENSAJE";
            AsyncCallWS task = new AsyncCallWS();
            task.execute("");
        } catch (Exception exc) {
            //  estadoRespuesta("Facturacion " + infoClienteEntrada.gettablaEntradaClientes_CUENTA() + " / " + exc.toString());
        }
    }

    public void processFinish(String output) {

    }

    public String chatInsertarMensajes(String remitente, String lista) {

        String respuestaWeb = "";
        WSSoap wsoaps = new WSSoap(URL, paginaWs);

        String mens = asunto.getText().toString() + "," + mensaje.getText().toString();


        try {
            //[!] Ax: verifica Si existe Directorio Archivo en server ------------------------------------------------
            respuestaWeb = wsoaps.chatInsertarMensajes("INSERTAR_MENSAJES", remitente, lista, mens, "E");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("operario", e.getMessage());
        }

        //limpiar();
        return respuestaWeb;
    }

    public void limpiar() {
        mensaje.setText("");
        asunto.setText("");
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
                            }
                        });
        AlertDialog alert = alertConfCreado.create();
        alert.show();
    }

    private class AsyncCallWS extends AsyncTask<String, Integer, Void> {

        // ProgressDialog pDialog;
        public String respuesta_;

        @Override
        protected Void doInBackground(String... params) {

            try {
                switch (metodo) {

                    case "CHATINSERTMENSAJE":
                        respuesta_ = chatInsertarMensajes(operario, list);
                        break;
                }

            } catch (Exception e) {
                Log.e("operario", e.getMessage());
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
                case "CHATINSERTMENSAJE":

                    int res = respuesta_.indexOf("12", 0);
                    Log.e("respuesta", res + " " + respuesta_);
                    if (res >= 1) {
                        limpiar();
                        alertMensaje("su mensaje a sido enviado");
                    } else {
                        limpiar();
                        alertMensaje("Error en el envio");
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
