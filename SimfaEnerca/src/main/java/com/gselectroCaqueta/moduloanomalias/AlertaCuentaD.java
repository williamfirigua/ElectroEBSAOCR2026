package com.gselectroCaqueta.moduloanomalias;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.gselectroCaqueta.accesoyseguridad.MenuDeLiquidacion;
import com.gselectroCaqueta.accesoyseguridad.R;

public class AlertaCuentaD extends AppCompatActivity {

    Button btnNo;
    Button btnSi;
    TextView mensaje;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerta_cuenta_d);
        btnNo = (Button)findViewById(R.id.btnNo);
        btnSi = (Button)findViewById(R.id.btnSi);
        mensaje = (TextView)findViewById(R.id.mensaje);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mensaje.setText(bundle.getString("mensaje"));
        }

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent iBackActivity = new Intent(getApplicationContext(), MenuDeLiquidacion.class);
                iBackActivity.putExtra("Seleccion","NO");
                setResult(RESULT_OK, iBackActivity);
                finish();
            }
        });

        btnSi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent iBackActivity = new Intent(getApplicationContext(), MenuDeLiquidacion.class);
                iBackActivity.putExtra("Seleccion","SI");
                setResult(RESULT_OK, iBackActivity);
                finish();
            }
        });



    }

    @Override
    public void onBackPressed() {

    }
}
