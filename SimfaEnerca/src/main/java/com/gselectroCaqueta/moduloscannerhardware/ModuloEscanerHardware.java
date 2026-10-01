package com.gselectroCaqueta.moduloscannerhardware;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gselectroCaqueta.accesoyseguridad.MenuDeLiquidacion;
import com.gselectroCaqueta.accesoyseguridad.R;

public class ModuloEscanerHardware extends AppCompatActivity {

    String contador;
    String scaneado = "";
    EditText txtScan;
    Button btnReintentar;
    Button btnSalvar;
    boolean delmenu = false;
    private static ModuloEscanerHardware instancemehw = null;
    public static final String SOFTWARE_SCANKEY = "unitech.scanservice.software_scankey";

    public static ModuloEscanerHardware instance() {
        return instancemehw;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner_hardaware);

        instancemehw = this;

        txtScan = (EditText) findViewById(R.id.txtScan);
        btnReintentar = (Button) findViewById(R.id.btnReintentar);
        btnSalvar = (Button) findViewById(R.id.btnSalvar);

        Bundle bundle = getIntent().getExtras();
        delmenu = bundle.getBoolean("menu");
        contador = bundle.getString("contador");

        if (contador == null || contador.trim().equals(""))
            contador = "0000";

        contador = contador.trim();

        btnReintentar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                scanner();
            }
        });

        btnSalvar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (!delmenu)
                    comprobar();
            }
        });

        scanner();
        txtScan.requestFocus();

    }//End Oncreate

    @Override
    public void onBackPressed() {
        if (!delmenu) {
            mensajes("Debe escanear el codigo de barras o 4 numeros...");
        } else {
            finish();
        }
    }

    public void scanner() {

        try {
            Bundle bundlexs = new Bundle();
            bundlexs.putBoolean("scan", true);
            Intent mIntent = new Intent().setAction(SOFTWARE_SCANKEY).putExtras(bundlexs);
            sendBroadcast(mIntent);

        } catch (Exception ex) {
            //utils.Log(logfile, "[MenuDeLiquidacion]startBarCodeReader(); " + ex.getMessage());
        }
    }

    private void comprobar() { //Ax: 4 ultimos digitos del medidor

        String txt = txtScan.getText().toString().trim();
        if (txt.length() != 4) {
            return;
        }
        if (contador.substring(contador.length() - 4, contador.length()).equals(txtScan.getText().toString().trim())) {
            scaneado = txt;
            getSendData();
        }else
            mensajes("Numero incorrecto");
    }

    public void setScannedInfo(String x) {
        txtScan.setText(x);

        if (x.length() < 9) return;

        scaneado = x;
        getSendData();
    }

    public void getSendData() { //Ax. envia datos a la actividad que la llamo y cierra esta

        Intent iBackActivity = new Intent(this, MenuDeLiquidacion.class);
        iBackActivity.putExtra("barcode", scaneado);
        setResult(RESULT_OK, iBackActivity);
        finish();
    }

    private void mensajes(String msg) {
        Toast toast = Toast.makeText(ModuloEscanerHardware.this, msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 10, 50);
        toast.show();
    }
}