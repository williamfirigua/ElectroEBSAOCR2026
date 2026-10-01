package com.gselectroCaqueta.moduloanomalias;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gselectroCaqueta.accesoyseguridad.MenuDeLiquidacion;
import com.gselectroCaqueta.accesoyseguridad.R;
import com.gselectroCaqueta.accesoyseguridad.VariablesGlobales;

public class ModuloDeAnomalias extends AppCompatActivity {

    final static String ACTIVITY_INFO = "com.globalsolutions.accesoyseguridad.MenuDeLiquidacion";
    TextView txtAnomalia;
    EditText txtNumAnomalia;
    ListView listadoAnomaliasNoLectura;
    AnomaliaDeNoLectura anomaliaDeNoLectura = new AnomaliaDeNoLectura();
    ArrayAdapter<String> item;
    Button btnvol;
    String anomaliaSeleccionada = "";

    private OnItemClickListener itemClickListener = new OnItemClickListener() {

        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {


            String cc = ((TextView) v).getText().toString().toUpperCase();
            Log.e("errora","Selecciona novedad "+cc.substring(0, 3));
            if(cc.substring(0, 3).trim().equals("9") || cc.substring(0, 3).trim().equals("15")){
                anomaliaSeleccionada = cc.substring(0, 3);
                MostrarAlertDialog("Causa no lectura","La Anomalia seleccionada es correcta? "+cc);
            }
            else {
                getSendData(cc.substring(0, 3));
            }
//            txtAnomalia.setText(cc);
//            txtNumAnomalia.setText("");
            //txtAnomalia.setText(((TextView) v).getText().toString().toUpperCase());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modulo_de_anomalias);

        btnvol = (Button)findViewById(R.id.btnvol);
        txtAnomalia = (TextView) findViewById(R.id.txtAnomalia);
        txtNumAnomalia = (EditText) findViewById(R.id.txtNumAnomalia);//Ax: Creado para capturar manualmente numero de anomalia
        listadoAnomaliasNoLectura = (ListView) findViewById(R.id.listadoAnomaliasNoLectura);
        item = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        listadoAnomaliasNoLectura.setOnItemClickListener(itemClickListener);
        anomaliaDeNoLectura.setArchivo_AnomaliaDeNoLectura(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/CAUSA_NL.TXT");
        llenarListadoAnomalias();
        VariablesGlobales.datodebusqueda = "";
        txtAnomalia.requestFocus();
        txtAnomalia.setText("");

//        txtAnomalia.setOnClickListener(new OnClickListener() {//7803
//
//            @Override
//            public void onClick(View v) {
////				VariablesGlobales.datodebusqueda = txtAnomalia.getText().toString().substring(0,3);
////	            finish();
//                //Creo y asigno la información a enviar.
//                String stemp=txtAnomalia.getText().toString().trim();//Ax: se comprueba cadena llena y se va con datos a la otra actividad
//                if(stemp != null && !stemp.isEmpty()){
//                    getSendData(txtAnomalia.getText().toString().substring(0, 3));
//                }
//            }
//        });

        btnvol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        txtNumAnomalia.setOnFocusChangeListener(new View.OnFocusChangeListener() {//Ax: borrar el texto en focus para que ingresen numero
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                txtNumAnomalia.setText("");
            }
        });

        txtNumAnomalia.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                String stemp = txtNumAnomalia.getText().toString().trim();
                if (actionId == 6 && stemp != null && !stemp.isEmpty()) {//Ax: se comprueba cadena llena y se va con datos a la otra actividad
                    getSendData(" " + stemp);//La de texto viene con un espacio adelante
                }
                return false;
            }
        });
    }

    public void getSendData(String snumero) { //Ax. envia datos a la actividad que la llamo y cierra esta

        Intent iBackActivity = new Intent(this, MenuDeLiquidacion.class);
        iBackActivity.putExtra("snumero", snumero);
        setResult(RESULT_OK, iBackActivity);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.modulo_de_anomalias, menu);
        return true;
    }

    private void llenarListadoAnomalias() {
        try {
            if (anomaliaDeNoLectura.abrir_AnomaliaDeNoLectura(anomaliaDeNoLectura.getArchivo_AnomaliaDeNoLectura())) {
                listadoAnomaliasNoLectura.setAdapter(null);
                for (int i = 1; i <= anomaliaDeNoLectura.getTotal_AnomaliaDeNoLectura(); i++) {
                    anomaliaDeNoLectura.lectura_AnomaliaDeNoLectura(i);

                    item.add(anomaliaDeNoLectura.getanomaliaDeNoLectura_CODIGO() + "-" + anomaliaDeNoLectura.getanomaliaDeNoLectura_DESCRIPCION().trim() + ".");
                }
                anomaliaDeNoLectura.Cerrar_AnomaliaDeNoLectura();
            }
            listadoAnomaliasNoLectura.setAdapter(item);
        } catch (Exception e) {
            {
                Toast.makeText(getApplicationContext(), "Modulo de listar \nAnomalias con Problemas", Toast.LENGTH_LONG).show();
                return;
            }
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
                getSendData(anomaliaSeleccionada);

            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }
}
