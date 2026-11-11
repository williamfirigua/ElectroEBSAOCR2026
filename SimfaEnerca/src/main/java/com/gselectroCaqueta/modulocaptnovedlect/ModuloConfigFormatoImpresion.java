package com.gselectroCaqueta.modulocaptnovedlect;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gselectroCaqueta.accesoyseguridad.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class ModuloConfigFormatoImpresion extends AppCompatActivity {

    Button btnciguardar;
    CheckBox chkbxcigap;

    EditText txtcicorrer;
    EditText txtcitemperatura;

    int Indicador = 0;
    int ESImpresora521 = 0;
    String AppPath;
    CheckBox chkbxdosbarras;
    CheckBox chkbxdosImpre;

    CheckBox chkbxzq521;
    CheckBox chkbxzw420;
    // Errores en linea (los mismos mensajes del Toast, bajo cada campo)
    TextView txtErrorTemp, txtErrorLineas;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_formato_impresion);

        chkbxcigap = (CheckBox) findViewById(R.id.chkbxcigap);
        btnciguardar = (Button) findViewById(R.id.btnciguardar);
        txtcicorrer = (EditText) findViewById(R.id.txtcicorrer);
        txtcitemperatura = (EditText) findViewById(R.id.txtcitemperatura);
        chkbxdosbarras   = (CheckBox) findViewById(R.id.chkbxdosbarras);
        chkbxdosImpre= (CheckBox) findViewById(R.id.chkbxdosImpre);

        chkbxzq521= (CheckBox) findViewById(R.id.chkbxzq521);
        chkbxzw420= (CheckBox) findViewById(R.id.chkbxrw420);
        txtErrorTemp = (TextView) findViewById(R.id.imp_txtErrorTemp);
        txtErrorLineas = (TextView) findViewById(R.id.imp_txtErrorLineas);

        // Botones - / + : solo cambian el numero del campo dentro del rango que acepta guardar()
        configurarPaso(R.id.imp_btnTempMenos, txtcitemperatura, -5, -30, 200);
        configurarPaso(R.id.imp_btnTempMas, txtcitemperatura, 5, -30, 200);
        // txtcicorrer acepta 3 caracteres (maxLength), por eso el minimo alcanzable con - es -99
        configurarPaso(R.id.imp_btnLineasMenos, txtcicorrer, -10, -99, 200);
        configurarPaso(R.id.imp_btnLineasMas, txtcicorrer, 10, -99, 200);

        Bundle bundle = getIntent().getExtras();

        AppPath = bundle.getString("directorioactual");
        Indicador = bundle.getInt("indicador");
        ESImpresora521= bundle.getInt("indicadorModeloImpresora");

        LlenarValoresOriginales();
        configurarEnvioImpresora();

        if (Indicador == 1)
            chkbxcigap.setChecked(true);
        else
            chkbxcigap.setChecked(false);

        if (ESImpresora521 == 1) {
            chkbxzq521.setChecked(true);
            chkbxzw420.setChecked(false);
        }
        else {
            chkbxzq521.setChecked(false);
            chkbxzw420.setChecked(true);
        }


        btnciguardar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (v != null) {
                    InputMethodManager inm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inm.hideSoftInputFromWindow(v.getWindowToken(),0);
                }
                guardar();
            }
        });
        chkbxzw420.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //tableTemp.setVisibility(View.VISIBLE);
                if (chkbxzw420.isChecked() ){
                    chkbxzw420.setChecked(true);
                    chkbxzq521.setChecked(false);
                }else{
                    chkbxzw420.setChecked(false);
                    chkbxzq521.setChecked(true);
                }
            }
        });
        chkbxzq521.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //tableTemp.setVisibility(View.VISIBLE);
                if (chkbxzq521.isChecked() ){
                    chkbxzw420.setChecked(false);
                    chkbxzq521.setChecked(true);
                }else{
                    chkbxzw420.setChecked(true);
                    chkbxzq521.setChecked(false);
                }
            }
        });

    }//End Oncreate

    public void LlenarValoresOriginales(){
        try {
            String Valortexto1 = "50";
            String ValorMoverGrafico = "0";
            int SinBarSence = 0;
            int NroBarras = 0;
            File ArchivoFormato = new File(AppPath + "/ValoresFormato.log");
            if (ArchivoFormato.exists()){
                FileReader r = new FileReader(ArchivoFormato);
                BufferedReader reader = new BufferedReader(r);
                String linea = "";
                int cont = 0;

                while ((linea = reader.readLine()) != null) {
                    cont++;
                    String formato = String.format("%1$1s",linea.trim()).replace(" ","0");
                    String formato2 = String.format("%1$3s", linea.trim()).replace(" ", "0");
                    if (cont == 1) {
                        Valortexto1 = linea.trim();
                    }else
                    if (cont == 2) {
                        ValorMoverGrafico = linea.trim();
                    }else
                    if (cont == 3) {
                        SinBarSence = Integer.parseInt(formato);
                    }else
                    if (cont == 4) {
                        NroBarras = Integer.parseInt(formato);
                    }
                }
                r.close();
            }
            Log.e("INFO","Valortexto1: " + Valortexto1 + " ,ValorMoverGrafico: " + ValorMoverGrafico + " ,SinBarSence: " + SinBarSence +
                    " ,NroBarras: " + NroBarras);
            txtcitemperatura.setText(Valortexto1);
            txtcicorrer.setText(ValorMoverGrafico);

            if (SinBarSence == 0) {
                chkbxcigap.setChecked(false);
            }else {
                chkbxcigap.setChecked(true);
            }

            if (NroBarras == 1) {
                chkbxdosbarras.setChecked(true);
            }else {
                chkbxdosbarras.setChecked(false);
            }

            if (ESImpresora521 == 1) {
                chkbxzq521.setChecked(true);
                chkbxzw420.setChecked(false);
            }
            else {
                chkbxzq521.setChecked(false);
                chkbxzw420.setChecked(true);
            }


        }catch (Exception e){
            Log.e("ERROR","FormatoImp| LlenarValores() Error: " + e);
            mensajes("Error al llenar los valores: " + e);
        }
    }

    @Override
    public void onBackPressed() {
        mensajes("Debe Guardar y salir");
    }

    public void volveraLiquidacion() {//Ax: envia datos a la actividad que la llamo y se cierra

        Intent iBackActivity = new Intent(this, com.gselectroCaqueta.accesoyseguridad.MenuDeLiquidacion.class);
        setResult(Activity.RESULT_OK, iBackActivity);
        finish();
    }

    public void guardar() {

        try {

            if (Integer.parseInt(txtcitemperatura.getText().toString().trim()) < (-30) || Integer.parseInt(txtcitemperatura.getText().toString().trim()) > 200) {
                mensajes("El Valor de la temp. no puede ser menor a -30");
                mostrarError(txtErrorTemp, "La temperatura va de -30 a 200");
                txtcitemperatura.setText("0");
                return;
            }

            if (Integer.parseInt(txtcicorrer.getText().toString().trim()) < -200 || Integer.parseInt(txtcicorrer.getText().toString().trim()) > 200) {
                mensajes("Error en el valor por correr");
                mostrarError(txtErrorLineas, "Las líneas van de -200 a 200");
                txtcicorrer.setText("0");
                return;
            }
        } catch (Exception e) {
            mensajes("Error en el valor");
            mostrarError(txtErrorTemp, "Revise los valores: solo números");
            return;
        }
        txtErrorTemp.setVisibility(View.GONE);
        txtErrorLineas.setVisibility(View.GONE);
        //-----------------------------------------------------------

        if (txtcitemperatura.getText().toString().trim().equals("")) {
            txtcitemperatura.setText("0");
        }

        if (txtcicorrer.getText().toString().trim().equals("")) {
            txtcicorrer.setText("70");
        }

        File ArchivoFormato = new File(AppPath + "/ValoresFormato.log");

        if (ArchivoFormato.exists()) {
            ArchivoFormato.delete();
        }

        String temp = Integer.parseInt(txtcitemperatura.getText().toString().trim()) + "";
        String corr = Integer.parseInt(txtcicorrer.getText().toString().trim()) + "";

        String texto = String.format("%1$-3s", temp).replace(" ", " ") + "\r\n";
        texto += String.format("%1$-3s", corr).replace(" ", " ") + "\r\n";

        if (chkbxcigap.isChecked()) {
            texto += "1" + "\r\n";
        } else {
            texto += "0" + "\r\n";
        }

        if (chkbxdosbarras.isChecked()) {
            texto += "1" + "\r\n";
        } else {
            texto += "0" + "\r\n";
        }

        /*if (chkbxdosImpre.isChecked()) {
            texto += "1" + "\r\n";
        } else {
            texto += "0" + "\r\n";
        }*/

        if (chkbxzq521.isChecked()) {
            texto += "1" + "\r\n";
        } else {
            texto += "0" + "\r\n";
        }


        try {
            if (!ArchivoFormato.exists())
                ArchivoFormato.createNewFile();

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(ArchivoFormato, true));
            bufferedWriter.write(texto);
            bufferedWriter.close();

            String chk = (chkbxcigap.isChecked()) ? "Si" : "No";
            String chk2 = (chkbxdosbarras.isChecked()) ? "Si" : "No";
            String chk3 = (chkbxzq521.isChecked()) ? "Si" : "No";

            if (chkbxzq521.isChecked()) {
                chk3 += "Se imprimira en la ZQ-521";
            } else {
                chk3 += "Se imprimira en la ZW-420";
            }

            mensajes("Valores Guardador: \nTemperatura = " + txtcitemperatura.getText().toString()
                    + "\nMover Lineas = " + txtcicorrer.getText().toString() + "\nImprimir Grafico? = " + chk
                    + "\n Imprimir dos barras? = "+chk2
                    + "\n "+chk3);

            volveraLiquidacion();
        } catch (Exception e) {
            mensajes("Error al crear archivo! consulte con admin.");
            return;
        }
    }

    /** Solo presentacion: suma "paso" al numero del campo sin salirse de [min, max]. */
    private void configurarPaso(int idBoton, final EditText campo, final int paso, final int min, final int max) {
        findViewById(idBoton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int valor;
                try {
                    valor = Integer.parseInt(campo.getText().toString().trim());
                } catch (Exception e) {
                    valor = 0;
                }
                valor = Math.max(min, Math.min(max, valor + paso));
                campo.setText(String.valueOf(valor));
                campo.setSelection(campo.getText().length());
            }
        });
    }

    private void mostrarError(TextView error, String msg) {
        txtErrorTemp.setVisibility(View.GONE);
        txtErrorLineas.setVisibility(View.GONE);
        error.setText(msg);
        error.setVisibility(View.VISIBLE);
    }

    private void mensajes(String msg) {
        Toast toast = Toast.makeText(ModuloConfigFormatoImpresion.this, msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 10, 50);
        toast.show();
    }
    // ------------------------------------------------------------------ Enviar valores a la impresora

    private volatile boolean enviandoImpresora = false;

    /**
     * Un solo botón: el lenguaje y los archivos (fuentes, PCX) los define la web (Caída de datos > Recursos);
     * el técnico no edita nada, solo los manda a la impresora asociada.
     */
    private void configurarEnvioImpresora() {
        final Button boton = (Button) findViewById(R.id.imp_btnEnviarImpresora);
        final android.widget.ProgressBar progreso = (android.widget.ProgressBar) findViewById(R.id.imp_progresoImpresora);
        final TextView estado = (TextView) findViewById(R.id.imp_txtEstadoImpresora);
        final File dir = new File(AppPath == null ? "" : AppPath);
        String ultimo = com.gselectroCaqueta.impresion.PreparacionImpresora.ultimoEnvio(dir);
        estado.setText(ultimo == null ? "Todavía no se han enviado valores desde este celular." : "Último envío: " + ultimo);
        boton.setOnClickListener(v -> {
            if (enviandoImpresora) return;
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Enviar valores a la impresora")
                    .setMessage("La impresora debe estar encendida, cerca y sin imprimir. Tarda unos segundos; no la apague mientras recibe.")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Enviar", (d, w) -> {
                        enviandoImpresora = true;
                        boton.setEnabled(false);
                        progreso.setVisibility(View.VISIBLE);
                        estado.setText("Preparando...");
                        new Thread(() -> {
                            com.gselectroCaqueta.impresion.PreparacionImpresora.Resultado r =
                                    com.gselectroCaqueta.impresion.PreparacionImpresora.preparar(getApplicationContext(), dir,
                                            com.gselectroCaqueta.accesoyseguridad.VariablesGlobales.btPrintService,
                                            texto -> runOnUiThread(() -> estado.setText(texto)));
                            runOnUiThread(() -> {
                                enviandoImpresora = false;
                                boton.setEnabled(true);
                                progreso.setVisibility(View.GONE);
                                estado.setText(r.mensaje);
                                estado.setTextColor(getResources().getColor(r.ok ? R.color.lec_ok : R.color.lec_error));
                            });
                        }, "valores-impresora").start();
                    })
                    .show();
        });
    }
}