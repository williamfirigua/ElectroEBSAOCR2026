package com.gselectroCaqueta.moduloLicencia;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.gselectroCaqueta.accesoyseguridad.GuiAcceso;
import com.gselectroCaqueta.accesoyseguridad.R;
import com.gselectroCaqueta.comunicaciones.BDComunicaciones;
import com.gselectroCaqueta.comunicaciones.CrudComunicaciones;

/**
 * Entrada de la app: permisos, licencia y paso a GuiAcceso.
 *
 * Con licencia vigente pasa directo (y si hay señal la revisa con el servidor: renueva la fecha general o la
 * retira si se desvinculó o bloqueó desde la web). Sin licencia pide el código o el usuario del técnico y se
 * activa en línea; sin señal muestra el código de solicitud para que la oficina dicte la respuesta.
 * Reemplaza el Config.txt con el serial (se borra cuando la licencia nueva queda activa).
 */
public class ActLicencia extends AppCompatActivity {

    private static final String TAG = "ActLicencia";
    private static final int CODIGO_PERMISOS_CAMARA = 1, CODIGO_PERMISOS_ALMACENAMIENTO = 2, CODIGO_PERMISOS_TELEFONO = 3,
            CODIGO_PERMISOS_UBICACION = 4, CODIGO_PERMISOS_BLUETOOHSCN = 5, CODIGO_PERMISOS_BLUETOOHCNT = 6;

    private LicenciaTerminal licencia;
    private final Handler ui = new Handler(Looper.getMainLooper());

    private TextView txtMensaje;
    private TextView txtEquipo;
    private TextView txtSolicitud;
    private EditText txtOperador;
    private EditText txtRespuesta;
    private Button btnActivar;
    private Button btnSinSenal;
    private LinearLayout panelActivar;
    private ProgressBar progreso;

    private boolean revisando;

    private final ActivityResultLauncher<Intent> permisoArchivos = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> comprobarPermisos(3));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_licencia);
        licencia = new LicenciaTerminal(this);

        txtMensaje = findViewById(R.id.txtMensajeLicencia);
        txtEquipo = findViewById(R.id.txtEquipoLicencia);
        txtSolicitud = findViewById(R.id.txtSolicitudLicencia);
        txtOperador = findViewById(R.id.txtOperadorLicencia);
        txtRespuesta = findViewById(R.id.txtRespuestaLicencia);
        btnActivar = findViewById(R.id.btnActivarLicencia);
        btnSinSenal = findViewById(R.id.btnSinSenalLicencia);
        panelActivar = findViewById(R.id.panelActivar);
        progreso = findViewById(R.id.prgLicencia);

        txtEquipo.setText(Build.MANUFACTURER + " " + Build.MODEL + " · Android " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")");
        btnActivar.setOnClickListener(v -> activarEnLinea());
        btnSinSenal.setOnClickListener(v -> activarSinSenal());
        txtOperador.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                activarEnLinea();
                return true;
            }
            return false;
        });

        setParams();
        comprobarPermisos(1);
    }

    // ------------------------------------------------------------------ licencia

    /** Todos los permisos concedidos (hace falta el de archivos para leer la copia de Documents). */
    private void gestionarLicencia() {
        if (revisando) return;
        LicenciaTerminal.Revision rv = licencia.revisar();
        if (rv.estado == LicenciaTerminal.Estado.SIN_LICENCIA || rv.estado == LicenciaTerminal.Estado.INVALIDA) {
            String motivo = licencia.tomarMotivoRetiro();
            mostrarFormulario(motivo != null ? motivo : rv.mensaje());
            return;
        }
        if (!hayRed()) {
            continuarCon(rv);
            return;
        }
        // Con señal: el servidor renueva la licencia (o la retira) antes de entrar.
        ocupado(true, "Revisando la licencia con el servidor...");
        final String base = LicenciaTerminal.baseApi(this);
        new Thread(() -> {
            LicenciaTerminal.Respuesta r = licencia.revisarEnServidor(base);
            ui.post(() -> {
                ocupado(false, null);
                if (r.retirada) {
                    mostrarFormulario(r.mensaje);
                } else {
                    continuarCon(licencia.revisar());
                }
            });
        }, "licencia-revision").start();
    }

    private void continuarCon(LicenciaTerminal.Revision rv) {
        if (rv.vigente()) {
            entrar(rv.licencia);
        } else {
            mostrarFormulario(rv.mensaje());
        }
    }

    private void activarEnLinea() {
        String op = txtOperador.getText().toString().trim();
        if (op.isEmpty()) {
            txtOperador.setError("Digite el código o el usuario");
            return;
        }
        if (!hayRed()) {
            alerta("Sin señal", "El equipo no tiene conexión. Use la activación sin señal: dicte el código de solicitud a la oficina.");
            return;
        }
        ocupado(true, "Activando...");
        final String base = LicenciaTerminal.baseApi(this);
        new Thread(() -> {
            LicenciaTerminal.Respuesta r = licencia.activarEnLinea(base, op);
            ui.post(() -> {
                ocupado(false, null);
                if (r.ok) {
                    entrar(r.licencia);
                } else {
                    mostrarFormulario(r.mensaje);
                    alerta("No se pudo activar", r.mensaje);
                }
            });
        }, "licencia-activar").start();
    }

    private void activarSinSenal() {
        String op = txtOperador.getText().toString().trim();
        String resp = txtRespuesta.getText().toString().trim();
        if (op.isEmpty()) {
            txtOperador.setError("Digite el código del técnico");
            return;
        }
        if (!resp.matches("\\d{8}")) {
            txtRespuesta.setError("Son 8 dígitos");
            return;
        }
        if (licencia.codigoDeUsuario(op) == null) {
            txtOperador.setError("Sin señal digite el código numérico");
            return;
        }
        LicenciaTerminal.Licencia l = licencia.activarSinSenal(op, resp);
        if (l == null) {
            alerta("Código incorrecto", "La respuesta no corresponde a esta solicitud y a este técnico. Verifique con la oficina.");
            return;
        }
        continuarCon(licencia.revisar());
    }

    private void entrar(LicenciaTerminal.Licencia l) {
        Bundle b = new Bundle();
        b.putString("valida", "SI");
        b.putString("PhoneImei", l.codMovil);
        Intent i = new Intent(this, GuiAcceso.class);
        i.putExtras(b);
        startActivity(i);
        finish();
    }

    private void mostrarFormulario(String mensaje) {
        txtMensaje.setText(mensaje == null || mensaje.isEmpty() ? "El equipo no tiene licencia." : mensaje);
        txtSolicitud.setText("Código de solicitud: " + licencia.codigoSolicitud()
                + "\nDíctelo a la oficina junto con el código del técnico.");
        panelActivar.setVisibility(View.VISIBLE);
    }

    private void ocupado(boolean si, String mensaje) {
        revisando = si;
        progreso.setVisibility(si ? View.VISIBLE : View.GONE);
        btnActivar.setEnabled(!si);
        btnSinSenal.setEnabled(!si);
        if (mensaje != null) txtMensaje.setText(mensaje);
    }

    private boolean hayRed() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
            return nc != null && nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        } catch (Throwable t) {
            return false;
        }
    }

    private void alerta(String titulo, String mensaje) {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.gss_logo40x40)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("Aceptar", (DialogInterface d, int w) -> d.dismiss())
                .show();
    }

    // ------------------------------------------------------------------ permisos

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean concedido = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        if (!concedido) {
            txtMensaje.setText("La aplicación necesita todos los permisos para funcionar. Cierre y abra la app para concederlos.");
            return;
        }
        switch (requestCode) {
            case CODIGO_PERMISOS_CAMARA:
                comprobarPermisos(2);
                break;
            case CODIGO_PERMISOS_ALMACENAMIENTO:
                comprobarPermisos(3);
                break;
            case CODIGO_PERMISOS_TELEFONO:
                comprobarPermisos(4);
                break;
            case CODIGO_PERMISOS_UBICACION:
                comprobarPermisos(5);
                break;
            case CODIGO_PERMISOS_BLUETOOHSCN:
                comprobarPermisos(6);
                break;
            case CODIGO_PERMISOS_BLUETOOHCNT:
                comprobarPermisos(7);
                break;
        }
    }

    private void comprobarPermisos(int permiso) {
        try {
            switch (permiso) {
                case 1:
                    if (!concedido(Manifest.permission.CAMERA)) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CODIGO_PERMISOS_CAMARA);
                    } else {
                        comprobarPermisos(2);
                    }
                    break;
                case 2:
                    if (Build.VERSION.SDK_INT >= 30) {
                        if (!Environment.isExternalStorageManager()) {
                            try {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                                intent.setData(Uri.parse("package:" + getPackageName()));
                                permisoArchivos.launch(intent);
                            } catch (Exception e) {
                                permisoArchivos.launch(new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
                            }
                        } else {
                            comprobarPermisos(3);
                        }
                    } else if (!concedido(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CODIGO_PERMISOS_ALMACENAMIENTO);
                    } else {
                        comprobarPermisos(3);
                    }
                    break;
                case 3:
                    if (!concedido(Manifest.permission.READ_PHONE_STATE)) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, CODIGO_PERMISOS_TELEFONO);
                    } else {
                        comprobarPermisos(4);
                    }
                    break;
                case 4:
                    if (!concedido(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, CODIGO_PERMISOS_UBICACION);
                    } else {
                        comprobarPermisos(5);
                    }
                    break;
                case 5:
                    if (Build.VERSION.SDK_INT >= 31 && !concedido(Manifest.permission.BLUETOOTH_SCAN)) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, CODIGO_PERMISOS_BLUETOOHSCN);
                    } else {
                        comprobarPermisos(6);
                    }
                    break;
                case 6:
                    if (Build.VERSION.SDK_INT >= 31 && !concedido(Manifest.permission.BLUETOOTH_CONNECT)) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, CODIGO_PERMISOS_BLUETOOHCNT);
                    } else {
                        comprobarPermisos(7);
                    }
                    break;
                case 7:
                    gestionarLicencia();
                    break;
            }
        } catch (Exception ex) {
            Log.e(TAG, "permisos: " + ex);
            alerta("Permisos", "Error al comprobar permisos: " + ex);
        }
    }

    private boolean concedido(String permiso) {
        return ContextCompat.checkSelfPermission(this, permiso) == PackageManager.PERMISSION_GRANTED;
    }

    // ------------------------------------------------------------------ comunicaciones por defecto

    /** URL de los servidores la primera vez que corre la app (se cambian en Configurar Web/IP). */
    public void setParams() {
        try {
            CrudComunicaciones crudComuni = new CrudComunicaciones(this);
            BDComunicaciones bdcini = crudComuni.getParamsbyId(1);
            if (bdcini == null) {
                BDComunicaciones bdc = new BDComunicaciones();
                bdc.setId(1);
                bdc.setURL("162.215.135.191:3630");
                bdc.setPaginaWs("/demoenergia.asmx");
                bdc.setEstado("A");
                bdc.setRutaAdministrador("D:/ENRUTADOR_EBSA");
                bdc.setPuertoApi(3629);
                CrudComunicaciones.setCofigUrl(bdc);

                bdc.setId(2);
                bdc.setURL("186.117.240.195:836");
                bdc.setPaginaWs("/demoenergia.asmx");
                bdc.setEstado("I");
                bdc.setRutaAdministrador("D:/ENRUTADOR_EBSA");
                bdc.setPuertoApi(914);
                CrudComunicaciones.setCofigUrl(bdc);
            }
        } catch (Exception ignored) {
        }
    }
}
