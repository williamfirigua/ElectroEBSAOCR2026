package com.gselectroCaqueta.modulocaptnovedlect;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.gselectroCaqueta.accesoyseguridad.MenuDeLiquidacion;
import com.gselectroCaqueta.accesoyseguridad.R;
import com.gselectroCaqueta.accesoyseguridad.VariablesGlobales;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ModuloAdicionarVerificacion extends AppCompatActivity {

    TextView txtavCliclo;
    TextView txtavMes;
    TextView txtavAnio;
    TextView txtavCuenta;
    TextView txtavHora;
    TextView txtavFecha;
    TextView txtavConta;
    TextView txtavSector;
    TextView txtavmunicipio;
    TextView txtavMun;

    EditText txtavUsuario;
    EditText txtavDir;
    EditText txtavLector;
    EditText txtavContador;
    EditText txtavTelefono;
    EditText txtavLectura;
    EditText txtavobservacion;
    EditText txtavBarrio;
    EditText txtavInspector;

    RadioButton rbtnavEntregaronSI;
    RadioButton rbtnavDificilSI;
    RadioButton rbtnavOportunaSI;
    RadioButton rbtnavEtiquetaSI;
    RadioButton rbtnavPortaSI;

    Button btnavretornar;

    String numeroDeSatelitesGPS = "0";
    String altitudReportadaGPS = "0";
    String fechayHoraReportadaGPS = "0";
    String velocidadGPS = "0";
    String Predio = "";
    String AppPath = "";
    String Longitud = "0.0";
    String Latitud = "0.0";
    String amd = "";
    String hm = "";
    boolean salirse = false;
    Thread myThread; //Ax: publico para poder detenerlo

    int conteo = 0;
    String[] armaCuentasGps = new String[6];//Ax. guarada datos para la funcion guardarcoordenadas

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionar_verificacion);

        txtavCliclo = (TextView) findViewById(R.id.txtavCliclo);
        txtavMes = (TextView) findViewById(R.id.txtavMes);
        txtavAnio = (TextView) findViewById(R.id.txtavAnio);
        txtavCuenta = (TextView) findViewById(R.id.txtavCuenta);
        txtavHora = (TextView) findViewById(R.id.txtavHora);
        txtavFecha = (TextView) findViewById(R.id.txtavFecha);
        txtavConta = (TextView) findViewById(R.id.txtavConta);
        txtavSector = (TextView) findViewById(R.id.txtavSector);
        txtavmunicipio = (TextView) findViewById(R.id.txtavmunicipio);
        txtavMun = (TextView) findViewById(R.id.txtavMun);

        txtavContador = (EditText) findViewById(R.id.txtavContador);
        txtavUsuario = (EditText) findViewById(R.id.txtavUsuario);
        txtavDir = (EditText) findViewById(R.id.txtavDir);
        txtavLector = (EditText) findViewById(R.id.txtavLector);
        txtavTelefono = (EditText) findViewById(R.id.txtavTelefono);
        txtavLectura = (EditText) findViewById(R.id.txtavLectura);
        txtavobservacion = (EditText) findViewById(R.id.txtavobservacion);
        txtavBarrio = (EditText) findViewById(R.id.txtavBarrio);
        txtavInspector = (EditText) findViewById(R.id.txtavInspector);

        rbtnavEntregaronSI = (RadioButton) findViewById(R.id.rbtnavEntregaronSI);
        rbtnavDificilSI = (RadioButton) findViewById(R.id.rbtnavDificilSI);
        rbtnavOportunaSI = (RadioButton) findViewById(R.id.rbtnavOportunaSI);
        rbtnavEtiquetaSI = (RadioButton) findViewById(R.id.rbtnavEtiquetaSI);
        rbtnavPortaSI = (RadioButton) findViewById(R.id.rbtnavPortaSI);

        btnavretornar = (Button) findViewById(R.id.btnavretornar);

        Bundle bundle = getIntent().getExtras();

        String temp = bundle.getString("ciclo").trim();
        armaCuentasGps[5] = temp;
        txtavCliclo.setText(temp);

        temp = bundle.getString("cuenta").trim();
        armaCuentasGps[4] = temp;
        txtavCuenta.setText(temp);

        temp = bundle.getString("mes").trim();
        armaCuentasGps[1] = temp;
        txtavMes.setText(temp);

        temp = bundle.getString("anio").trim();
        armaCuentasGps[0] = temp;
        txtavAnio.setText(temp);

        temp = bundle.getString("nrocontador").trim();
        armaCuentasGps[3] = temp;

        temp = bundle.getString("contador").trim();
        armaCuentasGps[2] = temp;

        txtavUsuario.setText(bundle.getString("nombre").trim());
        txtavDir.setText(bundle.getString("direccion").trim());
        txtavLector.setText(bundle.getString("lector").trim());
        Predio = "CensoSupervisor";
        AppPath = bundle.getString("directorioactual".trim());

        Calendar calendar = Calendar.getInstance();
        String Hora = String.format("%1$2s", calendar.get(Calendar.HOUR_OF_DAY)).replace(" ", "0");
        String Minutos = String.format("%1$2s", calendar.get(Calendar.MINUTE)).replace(" ", "0");
        String Segundos = String.format("%1$2s", calendar.get(Calendar.SECOND)).replace(" ", "0");
        txtavHora.setText(Hora + ":" + Minutos + ":" + Segundos);

        String anno1 = String.format("%1$2s", calendar.get(Calendar.YEAR)).replace(" ", "0");
        String mes1 = String.format("%1$2s", calendar.get(Calendar.MONTH) + 1).replace(" ", "0");
        String dia = String.format("%1$2s", calendar.get(Calendar.DAY_OF_MONTH)).replace(" ", "0");
        txtavFecha.setText(dia + "/" + mes1 + "/" + anno1);

        txtavConta.setText(bundle.getString("numero").trim());
        txtavContador.setText(bundle.getString("numero").trim());
        txtavSector.setText(bundle.getString("sector").trim());
        txtavMun.setText(bundle.getString("municipio").trim());
        txtavmunicipio.setText(bundle.getString("sect").trim());

        Longitud = bundle.getString("longitud".trim());
        Latitud = bundle.getString("latitud".trim());

        Runnable myRunnableThread = new Reloj();
        myThread = new Thread(myRunnableThread);
        myThread.start();

        btnavretornar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (ValidarDatos()) {
                    GrabarDatos();
                    getSendData();
                } else {
                    mensajes("Faltan Datos Por Tomar");
                }
            }
        });

        LocationManager locationmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        LocationListener mlocListener = new UsarGPS();

        locationmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, mlocListener);

        if (!locationmanager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mensajes("GPS No esta activo, se debe activar!");
        }
    }//End Oncreate

    @Override
    public void onBackPressed() {
        mensajes("Debe completar los campos y guardar");
    }

    public void muestraPosicionActual(Location loc) {

        if (loc == null) {// Si no se encuentra localizacion
            Longitud = "0.0";
            Latitud = "0.0";
            velocidadGPS = "0";
        } else {// Si se encuentra, se mostrara la latitud y longitud
            Longitud = String.valueOf(loc.getLatitude());//Ax: antes Latitud
            Latitud = String.valueOf(loc.getLongitude());//Ax: antes Longitud
            velocidadGPS = String.valueOf(loc.getSpeed());
            numeroDeSatelitesGPS = "" + loc.getExtras().getInt("satellites");
            altitudReportadaGPS = String.valueOf(loc.getAltitude());

            Date date = new Date(loc.getTime());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            //Ax. ojo los meses se cuentan desde 0
            fechayHoraReportadaGPS = calendar.get(Calendar.DAY_OF_MONTH) + "/" + (1 + (int) calendar.get(Calendar.MONTH)) + "/" + calendar.get(Calendar.YEAR) + " " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND);

            VariablesGlobales.fechahoraGPS = date;
        }
    }

    public void getSendData() { //Ax: envia datos a la actividad que la llamo y cierra esta

        try {
            if (myThread.isAlive()) { //Ax: al salir se trata de parar el hilo, si no se activa 'salir'
                myThread.interrupt();
            }
        } catch (Exception ex) {
            salirse = true;
        }

        Intent iBackActivity = new Intent(this, MenuDeLiquidacion.class);
        setResult(RESULT_OK, iBackActivity);
        finish();
    }

    private boolean ValidarDatos() {
        String cadena = "";

        if (txtavTelefono.getText().toString().trim().equals(""))
            cadena += "Telefono\n";

        if (txtavLectura.getText().toString().trim().equals(""))
            cadena += "LECTURA\n";

        if (txtavobservacion.getText().toString().trim().equals(""))
            cadena += "OBSERVACION\n";

        if (!cadena.trim().equals("")) {
            mensajes("Faltan: " + cadena);
            return false;
        } else
            return true;
    }

    private void GrabarDatos() {
        File fileName = new File(AppPath + "/DATOSDESALIDA/AUDITORIASUPERVISOR.SDA");
        String texto = "";
        texto += String.format("%1$4s", txtavCliclo.getText().toString().trim()).replace(" ", "0") + ";";
        texto += String.format("%1$4s", txtavAnio.getText().toString().trim()).replace(" ", "0") + ";";
        texto += String.format("%1$2s", txtavMes.getText().toString().trim()).replace(" ", "0") + ";";
        texto += String.format("%1$9s", txtavCuenta.getText().toString().trim()).replace(" ", "0") + ";";
        texto += String.format("%1$-48s", txtavUsuario.getText().toString().trim()).replace(" ", " ") + ";";
        texto += String.format("%1$-64s", txtavDir.getText().toString().trim()).replace(" ", " ") + ";";
        texto += String.format("%1$10s", txtavFecha.getText().toString().trim()).replace(" ", "0") + ";";
        texto += String.format("%1$8s", txtavHora.getText().toString().trim()).replace(" ", "0") + ";";
        texto += String.format("%1$-20s", txtavTelefono.getText().toString().trim()).replace(" ", " ") + ";";
        texto += String.format("%1$-30s", txtavBarrio.getText().toString().trim()).replace(" ", " ") + ";";
        texto += String.format("%1$10s", txtavLector.getText().toString().trim()).replace(" ", " ") + ";";
        texto += String.format("%1$16s", txtavContador.getText().toString().trim()).replace(" ", " ") + ";";
        texto += String.format("%1$10s", txtavLectura.getText().toString().trim()).replace(" ", "0") + ";";
        if (rbtnavEntregaronSI.isChecked())
            texto += "SI;";
        else
            texto += "NO;";

        if (rbtnavDificilSI.isChecked())
            texto += "SI;";
        else
            texto += "NO;";

        if (rbtnavEntregaronSI.isChecked())
            texto += "SI;";
        else
            texto += "NO;";

        if (rbtnavEtiquetaSI.isChecked())
            texto += "SI;";
        else
            texto += "NO;";

        if (rbtnavPortaSI.isChecked())
            texto += "SI;";
        else
            texto += "NO;";

        texto += String.format("%1$10s", txtavInspector.getText().toString().trim()).replace(" ", " ");
        texto += ";NO;";
        texto += String.format("%1$-200s", txtavobservacion.getText().toString().trim()).replace(" ", " ") + ";";
        texto += String.format("%1$-20s", Latitud.trim()).replace(" ", " ") + ";";
        texto += String.format("%1$-20s", Longitud.trim()).replace(" ", " ") + ";";
        texto += "\r\n";

        try {
            if (!fileName.exists()) fileName.createNewFile();

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName, true));//append true
            bufferedWriter.write(texto);
            bufferedWriter.close();

        } catch (Exception e) {
            mensajes("Error al crear archivo! consulte con admin.");
            return;
        }
    }

    private void tomarFechaSistema() {

        Calendar cal = new GregorianCalendar();
        Date date = cal.getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        String formatteDate = df.format(date);

        amd = formatteDate;

        Date dt = new Date();
        SimpleDateFormat hf = new SimpleDateFormat("HHmmss");
        String formatteHour = hf.format(dt.getTime());

        hm = formatteHour;
    }

    /**
     * Escribe el archivo de texto para el caso de supervisor (censo) y sus posiciones
     *
     * @param msg armaCuentasGps:  [0] año ,[1] mes ,[2] contador [3] medidor  ,[4] cuenta,[5] ciclo
     */
    private void guardar_Coordenadas(String msg) {

        String texto;

        try {
            if (Latitud.trim().length() > 12) {
                Latitud = Latitud.trim().substring(0, 12);
            }
            if (Longitud.trim().length() > 12)
                Longitud = Longitud.trim().substring(0, 12);

            tomarFechaSistema();

            texto = String.format("%1$9s", armaCuentasGps[4]) + ";" +
                    String.format("%1$12s", Latitud.trim()) + ";" +
                    String.format("%1$12s", Longitud.trim()) + ";" +
                    String.format("%1$2s", numeroDeSatelitesGPS.trim()) + ";" +
                    amd + ";" +
                    hm + ";" +
                    String.format("%1$-20s", fechayHoraReportadaGPS.trim()) + ";" +
                    String.format("%1$-12s", altitudReportadaGPS.trim()) + ";" +
                    String.format("%1$-3s", armaCuentasGps[5]) + ";" +
                    armaCuentasGps[0] + ";" +
                    String.format("%1$2s", armaCuentasGps[1].trim()).replace(" ", "0") + ";" +
                    String.format("%1$-5s", armaCuentasGps[2]) + ";" +
                    String.format("%1$-16s", armaCuentasGps[3]) + ";" +
                    String.format("%1$11s", txtavLector.getText().toString().trim()) + ";" +
                    String.format("%1$12s", velocidadGPS.trim()) + ";" +
                    String.format("%1$-200s", msg) + ";" +
                    "X" + ";\r\n";

            File file = new File(AppPath + "/DATOSDESALIDA/CUENTASGPS.SDA");

            if (!file.exists()) {
                file.createNewFile();
            }
            RandomAccessFile writer = new RandomAccessFile(file, "rw");
            writer.seek(writer.length());
            writer.writeBytes(texto);
            writer.close();
        } catch (Exception ex) {
            mensajes("error en guardarCoordenadas" + ex.toString());
        }
    }

    public void doWork() {
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    if (salirse) return;
                    conteo++;
                    if (conteo == 300) { //Ax: cada 5   Min graba
                        String msg = "Supervisor Aun en actividad Censo, " + conteo / 60 + " minutos";
                        conteo = 0;
                        guardar_Coordenadas(msg);
                    }
                } catch (Exception e) {
                }
            }
        });
    }

    private void mensajes(String msg) {
        Toast toast = Toast.makeText(ModuloAdicionarVerificacion.this, msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 10, 50);
        toast.show();
    }

    private class UsarGPS implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            muestraPosicionActual(loc);
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    class Reloj implements Runnable {
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    doWork();
                    Thread.sleep(1000); // Pausa de 1 Segundo
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                }
            }
        }
    }
}