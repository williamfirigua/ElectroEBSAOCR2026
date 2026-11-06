package com.gselectroCaqueta.modulocambiodigitos;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.gselectroCaqueta.accesoyseguridad.R;
import com.gselectroCaqueta.accesoyseguridad.VariablesGlobales;

public class ModuloCambioDigitos extends AppCompatActivity {

    String directorioActual = "";
    int registroActual = 1;

    TextView txtDigitos;
    // Presentacion: error en linea, ejemplo vivo y boton Guardar
    TextView txtError, txtCifras, txtEjemplo;
    Button btnGuardar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modulo_cambio_digitos);

        txtDigitos = (TextView) findViewById(R.id.txtdigitos);
        txtError = (TextView) findViewById(R.id.lex_txtError);
        txtCifras = (TextView) findViewById(R.id.lex_txtCifras);
        txtEjemplo = (TextView) findViewById(R.id.txtEjemplo);
        btnGuardar = (Button) findViewById(R.id.lex_btnGuardar);

        Bundle bundle = getIntent().getExtras();

        directorioActual = bundle.getString("DIRECTORIOACTUAL");
        registroActual = bundle.getInt("NUMEROREGISTROACTUAL");

        int hoy = VariablesGlobales.intcontroltexto;
        ((TextView) findViewById(R.id.lex_txtDigitosHoy)).setText("Hoy: " + hoy + " dígitos");
        ((TextView) findViewById(R.id.lex_txtSiCancela)).setText("Si cancela, la lectura sigue con " + hoy + " dígitos.");

        txtDigitos.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                guardarDigitos();
                return false;
            }
        });

        // Guardar: el mismo camino que la tecla del teclado
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarDigitos();
            }
        });

        // Cancelar: cerrar sin cambiar (lo mismo que dejar el campo vacio y volver)
        View.OnClickListener cancelar = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        };
        findViewById(R.id.lex_btnCancelar).setOnClickListener(cancelar);
        findViewById(R.id.lex_btnVolver).setOnClickListener(cancelar);

        findViewById(R.id.lex_btnMenos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pasoDigitos(-1);
            }
        });
        findViewById(R.id.lex_btnMas).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pasoDigitos(1);
            }
        });

        txtDigitos.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                txtError.setVisibility(View.GONE);
                mostrarEjemplo();
            }
        });
        mostrarEjemplo();
    }

    /**
     * Guarda la cantidad de digitos. Vacio o no numerico: no hace nada (como antes). Entre 4 y 8: guarda y cierra.
     * Fuera de rango: antes cerraba sin guardar; ahora muestra el error y deja corregir.
     */
    private void guardarDigitos() {
        String txt = txtDigitos.getText().toString().trim();

        if (txt.equals("")) {
            mostrarError("Escriba de 4 a 8 dígitos, o toque Cancelar.");
            return;
        }

        int n;
        try {
            n = Integer.parseInt(txt);
        } catch (Exception ex) {
            mostrarError("Escriba solo números, de 4 a 8.");
            return;
        }

        if (n > 3 && n < 9) {
            VariablesGlobales.intcontroltexto = n;
            finish();
        } else {
            mostrarError("Solo se permiten de 4 a 8 dígitos. No se guardó.");
        }
    }

    private void mostrarError(String msg) {
        txtError.setText(msg);
        txtError.setVisibility(View.VISIBLE);
    }

    /** Solo presentacion: - / + dentro de 4 a 8. */
    private void pasoDigitos(int paso) {
        int n;
        try {
            n = Integer.parseInt(txtDigitos.getText().toString().trim());
        } catch (Exception e) {
            n = VariablesGlobales.intcontroltexto;
        }
        n = Math.max(4, Math.min(8, n + paso));
        txtDigitos.setText(String.valueOf(n));
    }

    /** Solo presentacion: ejemplo de como se pedira la lectura con la cantidad escrita. */
    private void mostrarEjemplo() {
        int n = -1;
        try {
            n = Integer.parseInt(txtDigitos.getText().toString().trim());
        } catch (Exception ignored) {
        }
        if (n > 3 && n < 9) {
            String cifras = ("0" + "12345678").substring(0, n);
            txtCifras.setText(cifras);
            txtEjemplo.setText("Ejemplo: lectura " + cifras + " (" + n + " dígitos, con cero a la izquierda).");
            btnGuardar.setText("Guardar " + n + " dígitos");
        } else {
            txtCifras.setText("—");
            txtEjemplo.setText("Escriba entre 4 y 8 para ver el ejemplo.");
            btnGuardar.setText("Guardar");
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
