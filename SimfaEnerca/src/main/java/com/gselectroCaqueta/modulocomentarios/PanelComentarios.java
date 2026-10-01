package com.gselectroCaqueta.modulocomentarios;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gselectroCaqueta.accesoyseguridad.MenuPrincipal;
import com.gselectroCaqueta.accesoyseguridad.R;
import com.gselectroCaqueta.accesoyseguridad.VariablesGlobales;
import com.gselectroCaqueta.tablas.TablaRegistroSalida;

public class PanelComentarios extends AppCompatActivity {

    public String directorioActual = "";
    public int registroActual = 1;
    TextView txtComentario;
    EditText lblComentario;
    ListView listadoComentarios;
    TablaComentarios tablaComentarios = new TablaComentarios();
    TablaRegistroSalida infoRegistroSalida = new TablaRegistroSalida();
    //int respuesta = 0;
    String respuesta = "";

    ArrayAdapter<String> item;
    private OnItemClickListener itemClickListener = new OnItemClickListener() {

        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            txtComentario.setText(((TextView) v).getText().toString().toUpperCase());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel_comentarios);

        Bundle bundle = getIntent().getExtras();


        directorioActual = bundle.getString("DIRECTORIOACTUAL");
        registroActual = bundle.getInt("NUMEROREGISTROACTUAL");


        listadoComentarios = (ListView) findViewById(R.id.listadoComentarios);
        txtComentario = (TextView) findViewById(R.id.txtComentario);
        lblComentario = (EditText) findViewById(R.id.lblComentario);
        item = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        listadoComentarios.setOnItemClickListener(itemClickListener);
        tablaComentarios.setArchivo_TablaComentarios(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/COMENTAR.TXT");
        infoRegistroSalida.setArchivo_TablaRegistroSalida(VariablesGlobales.directorioactual + "/DATOSDESALIDA/REGISTRO.SDA");
        llenarListadoComentarios();
        txtComentario.requestFocus();
        txtComentario.setText("");

        txtComentario.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                String b = txtComentario.getText().toString().trim();

                if (b.length() < 1) return;
                else if (b.length() == 1) b = " " + b;

                ingresarComentarioLectura(b.substring(0, 2));
            }
        });

//        lblComentario.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void afterTextChanged(Editable s) {
//                // TODO Auto-generated method stub
//            }
//
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                // TODO Auto-generated method stub
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                busqueda();
//            }
//        });

        lblComentario.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == 6) {
                    busqueda();
                }
                return false;
            }
        });
    }

    //Ax trata de buscar la primera letra ingresada
    private void busqueda() {

        String coment = lblComentario.getText().toString().trim();

        if (coment.length() > 2 || coment.length() == 0) return;
        int conteo = 0;

        try {
            if (tablaComentarios.abrir_TablaComentarios(tablaComentarios.getArchivo_TablaComentarios())) {

                for (int i = 1; i <= tablaComentarios.getTotal_TablaComentarios(); i++) {
                    tablaComentarios.lectura_TablaComentarios(i);

                    if (tablaComentarios.gettablaComentarios_CODIGO().trim().toLowerCase().equals(coment.toLowerCase())) {
                        txtComentario.setText(tablaComentarios.gettablaComentarios_CODIGO() + "-" + tablaComentarios.gettablaComentarios_DESCRIPCION().trim() + ".");
                        conteo++;
                    }
                }
            }
            if (conteo < 1) {
                mensajes("¡No existe el codigo!");
                txtComentario.setText("");
                lblComentario.setText("");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            tablaComentarios.Cerrar_TablaComentarios();
        }
    }

    private void llenarListadoComentarios() {

        try {
            if (tablaComentarios.abrir_TablaComentarios(tablaComentarios.getArchivo_TablaComentarios())) {
                listadoComentarios.setAdapter(null);
                for (int i = 1; i <= tablaComentarios.getTotal_TablaComentarios(); i++) {
                    tablaComentarios.lectura_TablaComentarios(i);

                    item.add(tablaComentarios.gettablaComentarios_CODIGO() + "-" + tablaComentarios.gettablaComentarios_DESCRIPCION().trim() + ".");
                }
                tablaComentarios.Cerrar_TablaComentarios();
            }
            listadoComentarios.setAdapter(item);
        } catch (Exception e) {
            {
                mensajes("Hay problemas AL LLENAR LAS COMENTARIOS EN TERRENO");
            }
        }
    }

    private void ingresarComentarioLectura(String observacionAct) {

        try {
            respuesta = observacionAct;

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Comentario Lectura");
            builder.setMessage("Aceptar el Comentario\n?" + observacionAct + " " + tablaComentarios.tablaComentarios_DESCRIPCION);
            builder.setIcon(R.drawable.ic_launcher1);
            builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    if (infoRegistroSalida.abrir_TablaRegistroSalida(infoRegistroSalida.getArchivo_TablaRegistroSalida())) {
                        infoRegistroSalida.lectura_TablaRegistroSalida(registroActual);
                        infoRegistroSalida.settablaRegistroSalida_comentario1(respuesta.substring(0, 2));//(tablaComentarios.gettablaComentarios_CODIGO().trim());
                        infoRegistroSalida.escribir_TablaRegistroSalida(registroActual);
                        infoRegistroSalida.Cerrar_TablaRegistroSalida();
                        ;
                        regresoaMenuLiquidacion();
                    } else {
                        mensajes("Problemas Procesando el archivo de salida");
                    }
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    respuesta = "";
                    txtComentario.setText("");
                    lblComentario.setText("");
                }
            });

            builder.show();
            // }
        } catch (Exception e) {
            mensajes("Problemas Procesando el archivo de salida");
        }
        //return respuesta;
    }

    public void regresoaMenuLiquidacion() {
        Intent regreso = new Intent(this, MenuPrincipal.class);
        setResult(RESULT_OK, regreso);
        finish();
    }

    private void mensajes(String msg) {
        Toast toast = Toast.makeText(PanelComentarios.this, msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 10, 170);
        toast.show();
    }
}
