package com.gselectroCaqueta.modulocaptnovedlect;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gselectroCaqueta.accesoyseguridad.MenuDeLiquidacion;
import com.gselectroCaqueta.accesoyseguridad.R;
import com.gsutil.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.util.Calendar;

public class ModuloAdicionarCertificado extends AppCompatActivity {

    public final int FIRMA_REQUEST_CODE = 5671;//Ax para Recibir de la actividad de firma

    TextView txtacCliclo;
    TextView txtacMes;
    TextView txtacAnio;
    TextView txtacCuenta;
    TextView txtacHora;
    TextView txtacFecha;

    EditText txtacUsuario;
    EditText txtacDir;
    EditText txtacTelefono;
    EditText txtacRecibe;
    EditText txtacLector;
    EditText txtacCedula;

    Button btnacfirma;
    Button btnacreotornar;

    File logfile;

    String Predio = "";
    String AppPath = "";
    String parametroMenu = "";

    boolean hafirmado = false;
    String ruta = "";
    Utils utils = new Utils();//Ax log y utilidades

    // Presentacion: errores en linea y tarjeta de firma
    TextView txtErrorTel, txtErrorRecibe, txtErrorCuenta, txtEstadoFirma;
    View tarjetaFirma;
    ImageView imgFirma;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionar_certificado);

        txtacCliclo = (TextView) findViewById(R.id.txtacCliclo);
        txtacMes = (TextView) findViewById(R.id.txtacMes);
        txtacAnio = (TextView) findViewById(R.id.txtacAnio);
        txtacCuenta = (TextView) findViewById(R.id.txtacCuenta);
        txtacHora = (TextView) findViewById(R.id.txtacHora);
        txtacFecha = (TextView) findViewById(R.id.txtacFecha);

        txtacCedula = (EditText) findViewById(R.id.txtacCedula);
        txtacUsuario = (EditText) findViewById(R.id.txtacUsuario);
        txtacDir = (EditText) findViewById(R.id.txtacDir);
        txtacLector = (EditText) findViewById(R.id.txtacLector);
        txtacTelefono = (EditText) findViewById(R.id.txtacTelefono);
        txtacRecibe = (EditText) findViewById(R.id.txtacRecibe);

        btnacfirma = (Button) findViewById(R.id.btnacfirma);
        btnacreotornar = (Button) findViewById(R.id.btnacreotornar);

        txtErrorTel = (TextView) findViewById(R.id.lex_txtErrorTel);
        txtErrorRecibe = (TextView) findViewById(R.id.lex_txtErrorRecibe);
        txtErrorCuenta = (TextView) findViewById(R.id.lex_txtErrorCuenta);
        txtEstadoFirma = (TextView) findViewById(R.id.lex_txtEstadoFirma);
        tarjetaFirma = findViewById(R.id.lex_tarjetaFirma);
        imgFirma = (ImageView) findViewById(R.id.lex_imgFirma);
        limpiarErrorAlEscribir(txtacTelefono, txtErrorTel);
        limpiarErrorAlEscribir(txtacRecibe, txtErrorRecibe);
        limpiarErrorAlEscribir(txtacCuenta, txtErrorCuenta);

        Bundle bundle = getIntent().getExtras();

        txtacCliclo.setText(bundle.getString("ciclo").trim());
        txtacCuenta.setText(bundle.getString("cuenta").trim());
        txtacMes.setText(bundle.getString("mes").trim());
        txtacAnio.setText(bundle.getString("anio").trim());
        txtacUsuario.setText(bundle.getString("nombre").trim());
        txtacDir.setText(bundle.getString("direccion").trim());
        txtacLector.setText(bundle.getString("lector").trim());
        txtacCedula.setText(bundle.getString("cedula").trim());

        Predio = bundle.getString("Certificado").trim();
        AppPath = bundle.getString("directorioactual").trim();


        // KIM
        parametroMenu = bundle.getString("MenuPrincipal");

        if (bundle.getString("MenuPrincipal") != null) {
            if (parametroMenu.equals("SI")) {
                txtacCuenta.setEnabled(true);
                txtacUsuario.setEnabled(true);
                txtacCuenta.requestFocus();
            }
        } else {
            txtacTelefono.requestFocus();
        }

        logfile = new File(AppPath + "/DATOSDESALIDA/LOGEVENTOS.LOG");

        Calendar calendar = Calendar.getInstance();
        String Hora = String.format("%1$2s", calendar.get(Calendar.HOUR_OF_DAY)).replace(" ", "0");
        String Minutos = String.format("%1$2s", calendar.get(Calendar.MINUTE)).replace(" ", "0");
        String Segundos = String.format("%1$2s", calendar.get(Calendar.SECOND)).replace(" ", "0");
        txtacHora.setText(Hora + ":" + Minutos + ":" + Segundos);

        String anno1 = String.format("%1$4s", calendar.get(Calendar.YEAR)).replace(" ", "0");
        String mes1 = String.format("%1$2s", calendar.get(Calendar.MONTH) + 1).replace(" ", "0");
        String dia = String.format("%1$2s", calendar.get(Calendar.DAY_OF_MONTH)).replace(" ", "0");
        txtacFecha.setText(dia + "/" + mes1 + "/" + anno1);

        btnacreotornar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (!hafirmado) {
                    mensajes("Falta la firma");
                    marcarFirmaFaltante();
                    return;
                }
                if (ValidarDatos()) {
                    GrabarDatos();
                    getSendData();
                } else {
                    mensajes("Faltan Datos Por Tomar");
                    marcarFaltantes();
                }
            }
        });

        btnacfirma.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                abrirFirma();
            }

        });
        // Toda la tarjeta de firma abre la firma (mismo camino que el boton)
        tarjetaFirma.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                btnacfirma.performClick();
            }
        });

    }//End Oncreate

    @Override
    public void onBackPressed() {
        mensajes("Debe completar los campos y guardar");
    }

    public void abrirFirma() {
        Bundle bundle = new Bundle();
        bundle.putString("directorioactual", "" + AppPath);
        bundle.putString("cuenta", txtacCuenta.getText().toString().trim());
        bundle.putString("rutafirma", ruta);
        bundle.putBoolean("firmaEst", hafirmado);
        Intent i = new Intent(this, ModuloFirma.class);
        i.putExtras(bundle);
        if (txtacCuenta.getText().toString().trim().equals("")) {
            mensajes("Debe ingresar numero de cuenta");
            txtErrorCuenta.setVisibility(View.VISIBLE);
        } else {
            startActivityForResult(i, FIRMA_REQUEST_CODE);
        }
    }

    public void getSendData() { //Ax. envia datos a la actividad que la llamo y cierra esta

        Intent iBackActivity = new Intent(this, MenuDeLiquidacion.class);
        setResult(RESULT_OK, iBackActivity);
        finish();
    }

    private boolean ValidarDatos() {
        String cadena = "";

        if (txtacTelefono.getText().toString().trim().equals(""))
            cadena += "Telefono\n";

        if (txtacRecibe.getText().toString().trim().equals(""))
            cadena += "quien recibe\n";

        if (txtacCuenta.getText().toString().trim().equals(""))
            cadena += "Numero de cuenta\n";

        if (cadena.trim() != "") {
            mensajes("Faltan: " + cadena);
            return false;
        } else
            return true;
    }

    private String AbrirCertificado() {//Ax: creado por problemas en abre extension uppercase o lowercase

        String x = AppPath + "/DATOSDESALIDA/CERTIFICADOS.SDA";
        String y = AppPath + "/DATOSDESALIDA/CERTIFICADOS.SDA";

        try {

            File z = new File(x);

            if (!z.exists()) {
                z.createNewFile();
            }
            RandomAccessFile w = new RandomAccessFile(x, "rw");//esta parte del codigo revienta si esta mal la extension
            int fileSize = (int) w.length();
            byte[] byteArray = new byte[fileSize];
            w.readFully(byteArray, 0, fileSize);
            String texto = new String(byteArray);
            w.close();

            return x;
        } catch (Exception ex) {
            return y;
        }
    }

    private void GrabarDatos() {
        File fileName = new File(AbrirCertificado());
        String texto = "";
        texto += String.format("%1$-4s", txtacCliclo.getText().toString().trim()).replace(" ", " ") + ";";
        texto += String.format("%1$-4s", txtacAnio.getText().toString().trim()).replace(" ", " ") + ";";
        texto += String.format("%1$-2s", txtacMes.getText().toString().trim()).replace(" ", " ") + ";";
        texto += String.format("%1$-9s", txtacCuenta.getText().toString().trim()).replace(" ", "0") + ";";
        texto += String.format("%1$-48s", txtacUsuario.getText().toString().trim()).replace(" ", " ") + ";";
        texto += String.format("%1$-64s", txtacDir.getText().toString().trim()).replace(" ", " ") + ";";
        texto += String.format("%1$-10s", txtacFecha.getText().toString().trim()).replace(" ", "0") + ";";
        texto += String.format("%1$-8s", txtacHora.getText().toString().trim()).replace(" ", "0") + ";";
        texto += String.format("%1$-20s", txtacTelefono.getText().toString().trim()).replace(" ", " ") + ";";
        texto += String.format("%1$-30s", txtacRecibe.getText().toString().trim()).replace(" ", " ") + ";";
        texto += String.format("%1$10s", txtacLector.getText().toString().trim()).replace(" ", " ") + ";";
        texto += String.format("%1$13s", txtacCedula.getText().toString().trim()).replace(" ", " ") + ";";
        texto += "NO;";
        texto += "\r\n";

        try {
            if (!fileName.exists()) {
                fileName.createNewFile();
            }

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName, true));//append true
            bufferedWriter.write(texto);
            bufferedWriter.close();

        } catch (Exception ex) {
            utils.Log(logfile, "[ModuloAdicionarCertificado]GrabarDatos(); " + ex);
            mensajes("Error al crear archivo! consulte con admin.");
            return;
        }
    }

    /**
     * Funcion que se recibe de una actividad datos
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == FIRMA_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                try {
                    hafirmado = data.getBooleanExtra("firmo", false);
                    ruta = data.getStringExtra("ruta");
                } catch (Exception ex) {
                }
                mostrarEstadoFirma();
            }
        }
    }

    /** Solo presentacion: marca en rojo los obligatorios vacios (mismas condiciones de ValidarDatos). */
    private void marcarFaltantes() {
        boolean faltaTel = txtacTelefono.getText().toString().trim().equals("");
        boolean faltaRecibe = txtacRecibe.getText().toString().trim().equals("");
        boolean faltaCuenta = txtacCuenta.getText().toString().trim().equals("");
        txtErrorTel.setVisibility(faltaTel ? View.VISIBLE : View.GONE);
        txtacTelefono.setBackgroundResource(faltaTel ? R.drawable.lex_campo_error : R.drawable.lex_campo);
        txtErrorRecibe.setVisibility(faltaRecibe ? View.VISIBLE : View.GONE);
        txtacRecibe.setBackgroundResource(faltaRecibe ? R.drawable.lex_campo_error : R.drawable.lex_campo);
        txtErrorCuenta.setVisibility(faltaCuenta ? View.VISIBLE : View.GONE);
        if (faltaTel) txtacTelefono.requestFocus();
        else if (faltaRecibe) txtacRecibe.requestFocus();
    }

    private void marcarFirmaFaltante() {
        tarjetaFirma.setBackgroundResource(R.drawable.lex_tarjeta_error);
        txtEstadoFirma.setText("Falta la firma");
        txtEstadoFirma.setTextColor(getResources().getColor(R.color.lec_error, getTheme()));
    }

    /** Solo presentacion: tarjeta de firma vacia o con la vista previa de la firma guardada. */
    private void mostrarEstadoFirma() {
        if (hafirmado) {
            tarjetaFirma.setBackgroundResource(R.drawable.lex_tarjeta_ok);
            txtEstadoFirma.setText("✓ Firma tomada");
            txtEstadoFirma.setTextColor(getResources().getColor(R.color.lec_ok, getTheme()));
            btnacfirma.setText("Firmar de nuevo");
            try {
                BitmapFactory.Options op = new BitmapFactory.Options();
                op.inSampleSize = 4;
                Bitmap b = BitmapFactory.decodeFile(ruta, op);
                if (b != null) {
                    imgFirma.setImageTintList(null);
                    imgFirma.setPadding(2, 2, 2, 2);
                    imgFirma.setImageBitmap(b);
                }
            } catch (Exception ignored) {
            }
        } else {
            tarjetaFirma.setBackgroundResource(R.drawable.lex_tarjeta);
            txtEstadoFirma.setText("Sin firma");
            txtEstadoFirma.setTextColor(getResources().getColor(R.color.lec_texto, getTheme()));
            btnacfirma.setText("Tomar firma");
        }
    }

    private void limpiarErrorAlEscribir(final TextView campo, final TextView error) {
        campo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (error.getVisibility() == View.VISIBLE && s.toString().trim().length() > 0) {
                    error.setVisibility(View.GONE);
                    if (campo != txtacCuenta) campo.setBackgroundResource(R.drawable.lex_campo);
                }
            }
        });
    }

    private void mensajes(String msg) {
        Toast toast = Toast.makeText(ModuloAdicionarCertificado.this, msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 10, 50);
        toast.show();
    }
}