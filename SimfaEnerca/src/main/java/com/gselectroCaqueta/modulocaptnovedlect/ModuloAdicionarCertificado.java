package com.gselectroCaqueta.modulocaptnovedlect;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
                    return;
                }
                if (ValidarDatos()) {
                    GrabarDatos();
                    getSendData();
                } else {
                    mensajes("Faltan Datos Por Tomar");
                }
            }
        });

        btnacfirma.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                abrirFirma();
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
            }
        }
    }

    private void mensajes(String msg) {
        Toast toast = Toast.makeText(ModuloAdicionarCertificado.this, msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 10, 50);
        toast.show();
    }
}