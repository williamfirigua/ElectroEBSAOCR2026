package com.gselectroCaqueta.impresion;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.gselectroCaqueta.accesoyseguridad.R;
import com.gselectroCaqueta.accesoyseguridad.VariablesGlobales;
import com.gselectroCaqueta.sync.DescargaRuta;

import java.io.File;

/**
 * Preparar impresora: enviar a la impresora el lenguaje, las fuentes y los PCX que define la oficina
 * (Caída de datos > Recursos) e imprimir la hoja de comprobación. No necesita ruta cargada: el supervisor lo hace
 * desde el ingreso antes de entregar los equipos; si no hay sesión con el servidor pide código y clave.
 * Se abre desde el ingreso (Test de impresión) y desde Config. Preimpreso. Nada es editable.
 */
public class PrepararImpresoraActivity extends AppCompatActivity {

    private Button enviar, comprobar;
    private ProgressBar progreso;
    private TextView estado;
    private volatile boolean ocupado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preparar_impresora);
        enviar = findViewById(R.id.pi_btnEnviar);
        comprobar = findViewById(R.id.pi_btnComprobar);
        progreso = findViewById(R.id.pi_progreso);
        estado = findViewById(R.id.pi_txtEstado);

        String mac = VariablesGlobales.printerMacAddress == null ? "" : VariablesGlobales.printerMacAddress.trim();
        boolean asociada = VariablesGlobales.btPrintService != null && VariablesGlobales.btPrintService.tieneImpresoraAsociada();
        ((TextView) findViewById(R.id.pi_txtImpresora)).setText(asociada
                ? (mac.isEmpty() ? "Asociada" : mac)
                : "Ninguna. Asocie la impresora (botón Bluetooth del ingreso) y vuelva aquí.");

        String ultimo = PreparacionImpresora.ultimoEnvio(dir());
        estado.setText(ultimo == null ? "Este celular todavía no le ha enviado valores a una impresora." : "Último envío: " + ultimo);

        enviar.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Enviar valores a la impresora")
                .setMessage("La impresora debe estar encendida, cerca y sin imprimir. Tarda unos segundos; no la apague mientras recibe.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Enviar", (d, w) -> enviarValores())
                .show());
        comprobar.setOnClickListener(v -> imprimirComprobacion());
    }

    private File dir() {
        return new File(VariablesGlobales.directorioactual == null ? "" : VariablesGlobales.directorioactual);
    }

    private void enviarValores() {
        trabajar("Preparando...", () -> {
            PreparacionImpresora.Resultado r = PreparacionImpresora.preparar(getApplicationContext(), dir(),
                    VariablesGlobales.btPrintService, texto -> runOnUiThread(() -> estado.setText(texto)));
            runOnUiThread(() -> {
                terminar(r);
                if (r.necesitaSesion) pedirSesion();
                else if (r.ok) ofrecerComprobacion();
            });
        });
    }

    private void imprimirComprobacion() {
        trabajar("Imprimiendo la comprobación...", () -> {
            PreparacionImpresora.Resultado r = PreparacionImpresora.imprimirComprobacion(dir(), VariablesGlobales.btPrintService);
            runOnUiThread(() -> terminar(r));
        });
    }

    private void ofrecerComprobacion() {
        new AlertDialog.Builder(this)
                .setTitle("Valores enviados")
                .setMessage("¿Imprimir la hoja de comprobación para ver que las fuentes y las imágenes quedaron en la impresora?")
                .setNegativeButton("Ahora no", null)
                .setPositiveButton("Imprimir", (d, w) -> imprimirComprobacion())
                .show();
    }

    /** Sin sesión con el servidor (p. ej. desde el ingreso, sin ruta): código y clave de quien prepara la impresora. */
    private void pedirSesion() {
        DescargaRuta dr = new DescargaRuta(getApplicationContext());
        LinearLayout caja = new LinearLayout(this);
        caja.setOrientation(LinearLayout.VERTICAL);
        int p = (int) (16 * getResources().getDisplayMetrics().density);
        caja.setPadding(p, p / 2, p, 0);
        final EditText codigo = new EditText(this);
        codigo.setHint("Código");
        codigo.setText(dr.codigoSugerido());
        final EditText clave = new EditText(this);
        clave.setHint("Clave");
        clave.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        caja.addView(codigo);
        caja.addView(clave);
        new AlertDialog.Builder(this)
                .setTitle("Ingreso al servidor")
                .setMessage("Para bajar los valores de la impresora digite su código y su clave de la web.")
                .setView(caja)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Ingresar", (d, w) -> {
                    final String c = codigo.getText().toString().trim(), k = clave.getText().toString().trim();
                    trabajar("Ingresando...", () -> {
                        String falla = new DescargaRuta(getApplicationContext()).iniciarSesion(c, k);
                        runOnUiThread(() -> {
                            if (falla != null) {
                                terminar(null);
                                estado.setText(falla);
                                estado.setTextColor(getResources().getColor(R.color.lec_error));
                            } else {
                                ocupado = false;
                                enviarValores();
                            }
                        });
                    });
                })
                .show();
    }

    private void trabajar(String mensaje, Runnable tarea) {
        if (ocupado) return;
        ocupado = true;
        enviar.setEnabled(false);
        comprobar.setEnabled(false);
        progreso.setVisibility(View.VISIBLE);
        estado.setTextColor(getResources().getColor(R.color.lec_texto2));
        estado.setText(mensaje);
        new Thread(tarea, "preparar-impresora").start();
    }

    private void terminar(PreparacionImpresora.Resultado r) {
        ocupado = false;
        enviar.setEnabled(true);
        comprobar.setEnabled(true);
        progreso.setVisibility(View.GONE);
        if (r != null) {
            estado.setText(r.mensaje);
            estado.setTextColor(getResources().getColor(r.ok ? R.color.lec_ok : R.color.lec_error));
        }
    }
}
