package com.gselectroCaqueta.modulocaptnovedlect;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gselectroCaqueta.accesoyseguridad.R;

public class ModuloVerificaConsumo extends AppCompatActivity {

    Button btnconsumoguardar;

    EditText txtconsumofacturado;

    String nroContador;
    double consumoFact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verificar_consumo);

        btnconsumoguardar = (Button) findViewById(R.id.btnconsumoguardar);
        txtconsumofacturado = (EditText) findViewById(R.id.txtconsumofacturado);

        Bundle bundle = getIntent().getExtras();

        consumoFact = Double.parseDouble(bundle.getString("ConsumoFacturado").trim());
        nroContador = bundle.getString("NroContador").trim();

        btnconsumoguardar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                guardar();
            }
        });

    }//End Oncreate

    @Override
    public void onBackPressed() {
        mensajes("Debe digitar consumo para salir");
    }

    public void guardar() {

        try {
            String ent = txtconsumofacturado.getText().toString().trim();

            if (!ent.equals("")) {

                if ((int)consumoFact == Double.parseDouble(ent)) {
                    finish();
                    return;
                }

                if (ent.length() >= 3 && ent.equals(nroContador)) {
                    finish();
                    return;
                }
            }
            mensajes("NO Concuerda consumo.. Digite el correcto, verifique en la factura");

        } catch (Exception e) {
            mensajes("Error en el valor");
        }
    }

    private void mensajes(String msg) {
        Toast toast = Toast.makeText(ModuloVerificaConsumo.this, msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 10, 70);
        toast.show();
    }
}