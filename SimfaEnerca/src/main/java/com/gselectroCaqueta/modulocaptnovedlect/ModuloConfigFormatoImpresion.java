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

        Bundle bundle = getIntent().getExtras();

        AppPath = bundle.getString("directorioactual");
        Indicador = bundle.getInt("indicador");
        ESImpresora521= bundle.getInt("indicadorModeloImpresora");

        LlenarValoresOriginales();

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
                txtcitemperatura.setText("0");
                return;
            }

            if (Integer.parseInt(txtcicorrer.getText().toString().trim()) < -200 || Integer.parseInt(txtcicorrer.getText().toString().trim()) > 200) {
                mensajes("Error en el valor por correr");
                txtcicorrer.setText("0");
                return;
            }
        } catch (Exception e) {
            mensajes("Error en el valor");
            return;
        }
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

    private void mensajes(String msg) {
        Toast toast = Toast.makeText(ModuloConfigFormatoImpresion.this, msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 10, 50);
        toast.show();
    }
}