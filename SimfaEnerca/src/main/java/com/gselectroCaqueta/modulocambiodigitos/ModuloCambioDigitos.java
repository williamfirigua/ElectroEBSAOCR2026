package com.gselectroCaqueta.modulocambiodigitos;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.gselectroCaqueta.accesoyseguridad.R;
import com.gselectroCaqueta.accesoyseguridad.VariablesGlobales;

public class ModuloCambioDigitos extends AppCompatActivity {

    String directorioActual = "";
    int registroActual = 1;

    TextView txtDigitos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modulo_cambio_digitos);

        txtDigitos = (TextView) findViewById(R.id.txtdigitos);

        Bundle bundle = getIntent().getExtras();

        directorioActual = bundle.getString("DIRECTORIOACTUAL");
        registroActual = bundle.getInt("NUMEROREGISTROACTUAL");

        txtDigitos.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                String txt = txtDigitos.getText().toString().trim();

                if (txt.equals("")) return false;

                try {
                    if (Integer.parseInt(txt) > 3 && Integer.parseInt(txtDigitos.getText().toString().trim()) < 9) {
                        VariablesGlobales.intcontroltexto = Integer.parseInt(txtDigitos.getText().toString().trim());
                    }
                } catch (Exception ex) {
                    return false;
                }

                finish();
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
