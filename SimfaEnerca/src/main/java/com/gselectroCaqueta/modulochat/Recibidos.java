package com.gselectroCaqueta.modulochat;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.gselectroCaqueta.accesoyseguridad.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;


public class Recibidos extends AppCompatActivity implements View.OnClickListener {

    final File sdCard = Environment.getExternalStorageDirectory();
    public View.OnClickListener TablaListener;
    File file = new File(sdCard.getAbsolutePath(), "/DATOSDESALIDA/MENSAJES.TXT");
    Button volver = null;
    Button borrar = null;
    String[] datosF = null;
    TableLayout tabla = null;
    int FILAS, COLUMNAS;        // Filas y columnas de nuestra tabla
    ArrayList<TableRow> filas = new ArrayList<TableRow>();

    public static void EcribirFichero(File Ffichero, String SCadena) {

        try {
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(Ffichero));
            osw.write(SCadena);
            osw.flush();
            osw.close();
            Log.e("bien", "Los datos fueron grabados correctamente" + Ffichero + " " + SCadena);


        } catch (IOException ioe) {
            Log.e("error", "Los datos no fueron grabados correctamente");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recibidos);
        volver = (Button) findViewById(R.id.volver);
        borrar = (Button) findViewById(R.id.borrar);
        tabla = (TableLayout) findViewById(R.id.tabla);
        volver.setOnClickListener(this);
        borrar.setOnClickListener(this);

        if (file.exists()) {
            /*si el archivo existe llama el metodo
              agregarCabecera recibe un arreglo qe contiene los
              titulos*/
            agregarCabecera();

            try {
                //lectura de archivo plano
                FileReader stream3 = new FileReader(file);
                BufferedReader br = new BufferedReader(stream3);

              /*  FileInputStream fIn = new FileInputStream(file);
                InputStreamReader archivo = new InputStreamReader(fIn);
                BufferedReader br = new BufferedReader(archivo);*/
                String linea = "";
                String msj = "";
                int contador = 0;

                while ((linea = br.readLine()) != null) {
                    //se hace n split por ; linea por linea
                    Log.e("error long ", linea.length() + " ");

                    if (linea.length() != 0) {
                        datosF = linea.split(";");
                        if (datosF[4].trim().equals("0")) {
                            msj += linea + "1" + "\n";
                        } else {
                            msj += linea + "\n";
                        }

                        linea = br.readLine();
                        contador++;


                /*se hace el split necesario para mostrar solo
                los datos que se  requiere*/

                        String[] asnmen = datosF[2].split(",");
                        String[] dem = datosF[0].split("=");
                        String[] asu = asnmen[0].split("=");
                        String[] fec = datosF[3].split("=");
                        String[] para = datosF[1].split("=");
                /*se crea un ArrayList y se le agregan las pocisiones
                Luego se llama el metodo agregarFilaTabla*/
                        ArrayList<String> arrayF = new ArrayList<String>();
                        arrayF.add(dem[1]);
                        arrayF.add(asu[1]);
                        arrayF.add(asnmen[1]);
                        arrayF.add(fec[1]);
                        arrayF.add(para[1]);
                        agregarFilaTabla(arrayF, contador);
                    }

                }

                if (msj.length() != 0) {
                    EcribirFichero(file, msj);
                }


                br.close();

            } catch (IOException e) {
                Log.e("error", e.getMessage());
            }
        } else {
            Toast.makeText(getApplicationContext(), "No tiene mensajes", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.volver) {
            Intent returnInt = new Intent(this, Insertar.class);
            setResult(RESULT_OK, returnInt);
            finish();

        }

        if (v.getId() == R.id.borrar) {

            confirmaBorradoTodo("Desea borrar los mensajes");
        }
    }

    public void agregarCabecera() {
        TableRow.LayoutParams layoutCelda;
        TableRow fila = new TableRow(this);
        TableRow.LayoutParams layoutFila = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        fila.setLayoutParams(layoutFila);
        String[] arraycabecera = getResources().getStringArray(R.array.cabecera_tabla);

        //(R.array.cabecera_tabla)

        COLUMNAS = arraycabecera.length;

        for (int i = 0; i < arraycabecera.length; i++) {
            TextView texto = new TextView(this);
            layoutCelda = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            texto.setText(arraycabecera[i]);
            texto.setGravity(Gravity.CENTER_HORIZONTAL);
            texto.setTextAppearance(this, R.style.estilo_celda);
            texto.setBackgroundResource(R.drawable.tabla_celda_cabecera);
            texto.setLayoutParams(layoutCelda);
            // fila.setId(0);
            fila.addView(texto);
        }

        tabla.addView(fila);
        filas.add(fila);

        FILAS++;
    }

    public void agregarFilaTabla(ArrayList<String> elementos, int contador) {


        TableRow.LayoutParams layoutCelda;
        TableRow.LayoutParams layoutFila = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        TableRow fila = new TableRow(this);
        fila.setLayoutParams(layoutFila);
        fila.setOnClickListener(TablaListener);


        for (int i = 0; i < elementos.size(); i++) {

            TextView texto = new TextView(this);
            texto.setText(String.valueOf(elementos.get(i)));
            texto.setGravity(Gravity.CENTER_HORIZONTAL);
            texto.setTextAppearance(this, R.style.estilo_celda);
            texto.setBackgroundResource(R.drawable.tabla_celda);
            layoutCelda = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            texto.setLayoutParams(layoutCelda);
            fila.setId(contador);
            fila.addView(texto);
        }

        tabla.addView(fila);
        filas.add(fila);

        FILAS++;

        fila.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Log.e("click", "hizo click" + v.getId() + " ");
                // borrarLinea(v.getId());
                String lin = "";

                // eliminarFila(v.getId()+1);
                TableRow contenedor = (TableRow) tabla.getChildAt(v.getId() + (int)1);
                for (int i = 0; i < contenedor.getChildCount(); i++) {
                    TextView EditTextv = (TextView) contenedor.getChildAt(2);
                    Log.e("Row  ", EditTextv.getText().toString());
                    lin = EditTextv.getText().toString();

                }
                confirmaBorrado("Desea borrar el mensaje", file, v.getId());
                Log.e("Row  ", v.getId() + " " + lin);

            }
        });
    }

    public void borrarT() {

        tabla.removeAllViews();
    }

    public void eliminarFila(int indicefilaeliminar) {
        //  if( indicefilaeliminar > 0 && indicefilaeliminar < FILAS )
        //{
        tabla.removeViewAt(indicefilaeliminar);
        FILAS--;
        //  }
    }

    public void EliminarRegistro(File FficheroAntiguo, int linBorrar) {

        int contador = 0;
        File FficheroNuevo = new File(Environment.getExternalStorageDirectory(), "/DATOSDESALIDA/MENSAJESCOPY.TXT");
        try {

            if (FficheroAntiguo.exists()) {

                BufferedReader Flee = new BufferedReader(new FileReader(FficheroAntiguo));
                String Slinea;

                while ((Slinea = Flee.readLine()) != null) {
                    contador++;

                    if (contador != linBorrar) {

                        Log.e("tot", linBorrar + " " + contador);
                        EcribirFichero(FficheroNuevo, Slinea);

                    } else {

                    }
                }

                FficheroAntiguo.delete();

                String SnomAntiguo = FficheroAntiguo.getName();

                FficheroNuevo.renameTo(FficheroAntiguo);

                Flee.close();
            } else {
                System.out.println("Fichero No Existe");
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        eliminarFila(linBorrar + 1);
    }

    public void confirmaBorrado(String mensaje, final File FficheroAntiguo, final int linBorrar) {
        final Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        v.vibrate(500);
        AlertDialog.Builder alertConfCreado = new AlertDialog.Builder(this);
        alertConfCreado.setIcon(R.drawable.ic_launcher1);
        alertConfCreado.setMessage(mensaje).setTitle("Alerta ")
                .setCancelable(false)
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        EliminarRegistro(FficheroAntiguo, linBorrar);
                    }
                });
        AlertDialog alert = alertConfCreado.create();
        alert.show();
    }

    public void confirmaBorradoTodo(String mensaje) {
        final Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        v.vibrate(500);
        AlertDialog.Builder alertConfCreado = new AlertDialog.Builder(this);
        alertConfCreado.setIcon(R.drawable.ic_launcher1);
        alertConfCreado.setMessage(mensaje).setTitle("Alerta ")
                .setCancelable(false)
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        file.delete();
                        borrarT();
                    }
                });
        AlertDialog alert = alertConfCreado.create();
        alert.show();
    }

}
