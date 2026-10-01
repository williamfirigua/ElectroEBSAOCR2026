package com.gselectroCaqueta.moduloGPS;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gselectroCaqueta.accesoyseguridad.MenuDeLiquidacion;
import com.gselectroCaqueta.accesoyseguridad.R;

public class ActGPS extends AppCompatActivity implements View.OnClickListener {

    TextView txvcoor = null;
    TextView txcvcoordenadas = null;
    Button salir = null;
    String latitudC = "";
    String longitudC = "";
    TextView txvres = null;
    TextView txvcli = null;
    TextView txvusu = null;
    String latitudU = "";
    String longitudU = "";
    private ProgressDialog dialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_gps);
        txvcoor = (TextView) findViewById(R.id.txvcoor);
        txcvcoordenadas = (TextView) findViewById(R.id.txcvcoordenadas);
        txvres = (TextView) findViewById(R.id.txvres);
        txvcli = (TextView) findViewById(R.id.txvcli);
        txvusu = (TextView) findViewById(R.id.txvusu);
        salir = (Button) findViewById(R.id.salir);
        salir.setOnClickListener(this);
        Bundle bundle = getIntent().getExtras();
        latitudC = bundle.getString("lactitud");
        longitudC = bundle.getString("longitud");
        Log.e("coordenadas", latitudC + " " + longitudC);
        runService();
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.salir) {


            Intent iBack = new Intent(this, MenuDeLiquidacion.class);
            setResult(RESULT_OK, iBack);
            finish();
        }
    }

    public double evaluarDistanciaAlPredio(double Latitud1, double Longitud1, double Latitud2, double Longitud2) {

///Ax        double xx = Latitud2;
///Ax        double yy = Longitud2;

        double degtorad = 0.01745329;
        double radtodeg = 57.29577951;
        double dlong;
        double dvalue;
        double dd;
        double miles;
        double km;
        double finalValue;
        dlong = Longitud1 - Longitud2;
        dvalue = (Math.sin(Latitud1 * degtorad) * Math.sin(Latitud2 * degtorad)) +
                (Math.cos(Latitud1 * degtorad) * Math.cos(Latitud2 * degtorad)
                        * Math.cos(dlong * degtorad));
        dd = Math.acos(dvalue) * radtodeg;
        miles = dd * 69.16;
        km = dd * 111.302;
        //finalValue = Math.round(km * 100.0) / 100.0;

        //Toast.makeText(this,"Se encuentra a una distancia de "+String.valueOf(finalValue).trim()+" mts",Toast.LENGTH_LONG).show();

        return km * 1000;
    }

    public void setLocation(Location loc) {
        //Obtener la direcci—n de la calle a partir de la latitud y la longitud
        if (loc.getLatitude() != 0.0 && loc.getLongitude() != 0.0) {

            try {
                txvcli.setText("Coordenadas Cliente");
                txvusu.setText("Coordenadas Usuario");
                latitudU = String.valueOf(loc.getLatitude());
                longitudU = String.valueOf(loc.getLongitude());

                if (latitudC.length() > 10) {
                    latitudC = latitudC.substring(0, 10);
                }
///Ax               else {
///Ax                   latitudC = latitudC;
///Ax               }
                if (longitudC.length() > 10) {
                    longitudC = longitudC.substring(0, 10);
                }
///Ax                else {
///Ax                    longitudC = longitudC;
///Ax                }
                if (latitudU.length() > 10) {
                    latitudU = latitudU.substring(0, 10);
                }
///Ax                else{
///Ax                    latitudU =latitudU;
///Ax                }
                if (longitudU.length() > 10) {
                    longitudU = longitudU.substring(0, 10);
                }
///Ax                else {
///Ax                    longitudU = longitudU;
///Ax                }
                txvcoor.setText(latitudC + "," + longitudC);
                txcvcoordenadas.setText(latitudU + "," + longitudU);
                dialog.dismiss();

///Ax                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
///Ax                List<Address> list = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
///Ax
///Ax                if (!list.isEmpty()) {
///Ax                    Address address = list.get(0);
///Ax                    //  messageTextView.setText("Mi direcci—n es: \n" + address.getAddressLine(0));
///Ax                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            String[] coordenadas = txcvcoordenadas.getText().toString().split(",");
            int distanciaC;
            String lacNS = "";
            String lonOE = "";
            int latitudCliSN = latitudC.indexOf("-");
            int latitudUsuSN = coordenadas[0].indexOf("-");
            int longitudCliSN = longitudC.indexOf("-");
            int longitudUsuSN = coordenadas[1].indexOf("-");

            if (latitudCliSN == -1 && latitudUsuSN == -1) {
                lacNS = " hacia el Sur";
            } else if (latitudCliSN != -1 && latitudUsuSN != -1) {
                lacNS = " hacia el Norte";
            } else if (latitudCliSN == -1 && latitudUsuSN != -1) {
                lacNS = " hacia el Sur";
            } else if (latitudCliSN != -1 && latitudUsuSN == -1) {
                lacNS = " hacia el Norte";
            }

            if (longitudCliSN == -1 && longitudUsuSN == -1) {
                lonOE = " con Oeste";
            } else if (longitudCliSN != -1 && longitudUsuSN != -1) {
                lonOE = " con Este";
            } else if (longitudCliSN == -1 && longitudUsuSN != -1) {
                lonOE = " con Oeste";
            } else if (longitudCliSN != -1 && longitudUsuSN == -1) {
                lonOE = " con Este";
            }

            distanciaC = (int) evaluarDistanciaAlPredio(Double.parseDouble(coordenadas[0]), Double.parseDouble(coordenadas[1]), Double.parseDouble(latitudC), Double.parseDouble(longitudC));
            txvres.setText("Se encuentra a una distancia de " + String.valueOf(distanciaC).trim() + " mts" + lacNS + lonOE);
        }
    }

    private void runService() {
        dialog = new ProgressDialog(this);
        dialog.setIcon(R.drawable.ic_launcher1);
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.setTitle("Espere Por Favor");
        dialog.setMessage("Calculado Posicion");
        dialog.show();

        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        MyLocationListener mlocListener = new MyLocationListener();
///Ax        mlocListener.setMainActivity(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //   ActivityCompat#requestPermissions
            //   here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //   int[] grantResults)
            //   to handle the case where the user grants the permission. See the documentation
            //   for ActivityCompat#requestPermissions for more details.
            return;
        }
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, mlocListener);

        if (!mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(getApplicationContext(), "GPS No esta activo", Toast.LENGTH_LONG).show();
        }
    }

    private class MyLocationListener implements LocationListener {
///Ax        ActGPS mainActivity;
///Ax
///Ax        public ActGPS getMainActivity() {
///Ax            return mainActivity;
///Ax        }
///Ax
///Ax        public void setMainActivity(ActGPS mainActivity) {
// /           this.mainActivity = mainActivity;
///Ax        }

        @Override
        public void onLocationChanged(Location loc) {
            // Este mŽtodo se ejecuta cada vez que el GPS recibe nuevas coordenadas
            // debido a la detecci—n de un cambio de ubicacion
///Ax            loc.getLatitude();
///Ax            loc.getLongitude();
///Ax            String Text = loc.getLatitude() + "-" + loc.getLongitude();
            //  txvcoor.setText(Text);
            ///Ax        this.mainActivity.setLocation(loc);
            setLocation(loc);
        }

        @Override
        public void onProviderDisabled(String provider) {
            // Este mŽtodo se ejecuta cuando el GPS es desactivado
            txvcoor.setText("GPS Desactivado");
        }

        @Override
        public void onProviderEnabled(String provider) {
            // Este mŽtodo se ejecuta cuando el GPS es activado
            txvcoor.setText("GPS Activado");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // Este mŽtodo se ejecuta cada vez que se detecta un cambio en el
            // status del proveedor de localizaci—n (GPS)
            // Los diferentes Status son:
            // OUT_OF_SERVICE -> Si el proveedor esta fuera de servicio
            // TEMPORARILY_UNAVAILABLE -> Temp˜ralmente no disponible pero se
            // espera que este disponible en breve
            // AVAILABLE -> Disponible
        }
    }
}
