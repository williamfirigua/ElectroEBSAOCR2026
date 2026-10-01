package com.gselectroCaqueta.moduloseleccionlote;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.gselectroCaqueta.accesoyseguridad.MenuDeLiquidacion;
import com.gselectroCaqueta.accesoyseguridad.R;
import com.gselectroCaqueta.accesoyseguridad.VariablesGlobales;

public class ModuloSeleccionLote extends AppCompatActivity {

    int totalClientes = 0;
    TextView txtRangoInicial;
    TextView txtRangoFinal;
    Button btncerrarlote;
    CheckBox chkbxPostal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modulo_seleccion_lote);

        txtRangoInicial = (TextView) findViewById(R.id.txtRangoInicial);
        txtRangoFinal = (TextView) findViewById(R.id.txtRangoFinal);
        btncerrarlote = (Button) findViewById(R.id.btncerrarlote);
        chkbxPostal = (CheckBox) findViewById(R.id.chkbxPostal);

        Bundle bundle = getIntent().getExtras();

        // directorioActual = bundle.getString("DIRECTORIOACTUAL");
        totalClientes = bundle.getInt("TOTALCLIENTES");

        moduloSeleccionLote_Load();

        txtRangoFinal.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                int a = 0;
                if (actionId == 6) {
                    comprueba();
                }
                return false;
            }
        });

        txtRangoInicial.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                int b = 0;
                if (actionId == 6) {

                    if (!convertInt()) {
                        Toast.makeText(getApplicationContext(), "Rangos no estan bien definidos", Toast.LENGTH_LONG).show();
                        return false;
                    }

                    if (Integer.parseInt(txtRangoInicial.getText().toString().trim()) > 0) {
                        if (Integer.parseInt(txtRangoFinal.getText().toString().trim()) == 0)
                            txtRangoFinal.setText(("" + totalClientes).trim());

                        txtRangoFinal.setEnabled(true);
                        txtRangoFinal.requestFocus();

                    } else {
                        finish();
                    }
                }
                return false;
            }
        });

        btncerrarlote.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                comprueba();
            }
        });

    }

    public void retornar() {

        Intent iBackActivity = new Intent(this, MenuDeLiquidacion.class);
        setResult(RESULT_OK, iBackActivity);
        finish();
    }

    private boolean convertInt() {
        try {
            Integer.parseInt(txtRangoInicial.getText().toString().trim());
            Integer.parseInt(txtRangoFinal.getText().toString().trim());
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private void comprueba() {

        if (!convertInt()) {
            Toast.makeText(getApplicationContext(), "Rangos no estan bien definidos", Toast.LENGTH_LONG).show();
            return;
        }

        if (Integer.parseInt(txtRangoFinal.getText().toString().trim()) > 0) {
            if ((Integer.parseInt(txtRangoFinal.getText().toString().trim()) >= Integer.parseInt(txtRangoInicial.getText().toString().trim()))
                    && (Integer.parseInt(txtRangoFinal.getText().toString().trim())) <= totalClientes) {

                if (chkbxPostal.isChecked()) {
                    VariablesGlobales.imprimirSoloPostal = 1;
                } else {
                    VariablesGlobales.imprimirSoloPostal = 0;
                }

                VariablesGlobales.rangofinalimpresion = Integer.parseInt(txtRangoFinal.getText().toString().trim());
                VariablesGlobales.rangoinicialimpresion = Integer.parseInt(txtRangoInicial.getText().toString().trim());

                retornar();
            } else {

                Toast.makeText(getApplicationContext(), "Rango seleccionado no esta bien definido", Toast.LENGTH_LONG).show();
            }
        }
        txtRangoFinal.setText("");
        txtRangoFinal.setEnabled(false);
        txtRangoInicial.setText("");
        txtRangoInicial.requestFocus();
    }

    private void moduloSeleccionLote_Load() {
        VariablesGlobales.imprimirSoloPostal = 0;
        VariablesGlobales.rangoinicialimpresion = 0;
        VariablesGlobales.rangofinalimpresion = 0;
    }
}
