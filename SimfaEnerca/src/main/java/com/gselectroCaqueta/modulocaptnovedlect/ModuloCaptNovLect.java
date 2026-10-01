package com.gselectroCaqueta.modulocaptnovedlect;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.gselectroCaqueta.accesoyseguridad.MenuDeLiquidacion;
import com.gselectroCaqueta.accesoyseguridad.R;
import com.gselectroCaqueta.accesoyseguridad.VariablesGlobales;
import com.gselectroCaqueta.tablas.Novedades;
import com.gsutil.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ModuloCaptNovLect extends AppCompatActivity {

    Novedades miNovedad = new Novedades();
    public VariablesGlobales VariablGlobal = new VariablesGlobales();

    TextView lblmensajenovedad;
    TextView lblanomaliactual;
    TextView lbldocumentonovedad;
    EditText txtcmbdescausal;

    EditText txtobservacion;
    EditText txtcuenta;
    EditText txtrutaactual;
    EditText txtrutaanterior;
    EditText txtusuario;
    EditText txtcedula;
    EditText txtdir;
    EditText txtbarrio;
    EditText txtMedidor;
    EditText txtvalorfact;
    EditText txtconsumofacturado;
    EditText txtcmbconexion;
    EditText txtcmbtipomedidor;
    EditText txtcmbclaseservicio;
    EditText txtcmbcorriente;
    EditText txtcmbmarca;
    EditText txtrutapropuesta;
    EditText txtlecturaennovedad;
    EditText txtanno;

    EditText txtclaseservicio;
    EditText txtrutaposterior;
    EditText txtmarca;
    EditText txtconexion;
    EditText txtsellotapa;
    EditText txtsellobornera;
    EditText txtsellogabinete;
    EditText txtk;
    EditText txtw;
    EditText txtcorriente;
    EditText txttipomedidor;
    EditText txttelefono;

    AlertDialog levelDialog;
    ImageButton imgbtnhablar;
    ImageButton imgbtnmenuanomalia;
    ImageButton imgbtncmbconexion;
    ImageButton imgbtncmbtipomedidor;
    ImageButton imgbtncmbclaseservicio;
    ImageButton imgbtncmbcorriente;
    ImageButton imgbtncmbmarca;

    ImageButton bntgrabar;

    public String[] Op_cmbdescausal;
    public String[] Op_cmbdescausal_2;
    public String[] Op_cmbconexion;
    public String[] Op_cmbtipomedidor;
    public String[] Op_cmbclaseservicio;
    public String[] Op_cmbcorriente;
    public String[] Op_cmbmarca;

    String lecturaActual;
    String lectorActual;
    String ciclo;
    String mes;
    String anno;
    String path;
    String archivo;
    String modeloCiclo;
    String causalNovedad = "0";
    String causalNovedad2 = "";
    String causalNovedad3 = "";
    String fecha;
    String cadenafaltante;
    String desCausalNovedad = "";
    String tipoNovedad = "0";
    String caracter;
    String serialPDA = "000000000000000";

    static final int RECOGNIZE_SPEECH_ACTIVITY = 1;
    int fuepromediado;
    int realizarAforos = 0;
    int selecionMarca = 0;
    int grabado = 0;

    TableLayout tabla;// Tabla datos usuario
    TableRow.LayoutParams layoutFila;
    TableRow fila;

    Resources resource;

    Utils utils = new Utils();//Ax log y utilidades
    public File logfile; //Ax: Es para los log de error

    // kim
    String parametroMenu = "";
    String rutaact = "";

    //-----------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capturar_novedad_lect);

        lblmensajenovedad = (TextView) findViewById(R.id.lblmensajenovedad);
        lblanomaliactual = (TextView) findViewById(R.id.lblanomaliactual);
        lbldocumentonovedad = (TextView) findViewById(R.id.lbldocumentonovedad);
        txtcmbdescausal = (EditText) findViewById(R.id.txtcmbdescausal);

        txtobservacion = (EditText) findViewById(R.id.txtobservacion);
        txtcuenta = (EditText) findViewById(R.id.txtcuenta);
        txtrutaactual = (EditText) findViewById(R.id.txtrutaactual);
        txtrutaanterior = (EditText) findViewById(R.id.txtrutaanterior);
        txtusuario = (EditText) findViewById(R.id.txtusuario);
        txtcedula = (EditText) findViewById(R.id.txtcedula);
        txtdir = (EditText) findViewById(R.id.txtdir);
        txtbarrio = (EditText) findViewById(R.id.txtbarrio);
        txtMedidor = (EditText) findViewById(R.id.txtMedidor);
        txtvalorfact = (EditText) findViewById(R.id.txtvalorfact);
        txtconsumofacturado = (EditText) findViewById(R.id.txtconsumofacturado);
        txtcmbconexion = (EditText) findViewById(R.id.txtcmbconexion);
        txtcmbtipomedidor = (EditText) findViewById(R.id.txtcmbtipomedidor);
        txtcmbclaseservicio = (EditText) findViewById(R.id.txtcmbclaseservicio);
        txtcmbcorriente = (EditText) findViewById(R.id.txtcmbcorriente);
        txtcmbmarca = (EditText) findViewById(R.id.txtcmbmarca);
        txtrutapropuesta = (EditText) findViewById(R.id.txtrutapropuesta);
        txtlecturaennovedad = (EditText) findViewById(R.id.txtlecturaennovedad);
        txtanno = (EditText) findViewById(R.id.txtanno);

        txtclaseservicio = (EditText) findViewById(R.id.txtclaseservicio);
        txtrutaposterior = (EditText) findViewById(R.id.txtrutaposterior);
        txtmarca = (EditText) findViewById(R.id.txtmarca);
        txtconexion = (EditText) findViewById(R.id.txtconexion);
        txtsellotapa = (EditText) findViewById(R.id.txtsellotapa);
        txtsellobornera = (EditText) findViewById(R.id.txtsellobornera);
        txtsellogabinete = (EditText) findViewById(R.id.txtsellogabinete);
        txtk = (EditText) findViewById(R.id.txtk);
        txtw = (EditText) findViewById(R.id.txtw);
        txtcorriente = (EditText) findViewById(R.id.txtcorriente);
        txttipomedidor = (EditText) findViewById(R.id.txttipomedidor);
        txttelefono = (EditText) findViewById(R.id.txttelefono);

        bntgrabar = (ImageButton) findViewById(R.id.bntgrabar);
        imgbtnmenuanomalia = (ImageButton) findViewById(R.id.imgbtnmenuanomalia);
        imgbtncmbconexion = (ImageButton) findViewById(R.id.imgbtncmbconexion);
        imgbtncmbtipomedidor = (ImageButton) findViewById(R.id.imgbtncmbtipomedidor);
        imgbtncmbclaseservicio = (ImageButton) findViewById(R.id.imgbtncmbclaseservicio);
        imgbtncmbcorriente = (ImageButton) findViewById(R.id.imgbtncmbcorriente);
        imgbtncmbmarca = (ImageButton) findViewById(R.id.imgbtncmbmarca);
        imgbtnhablar = (ImageButton) findViewById(R.id.imgbtnhablar);

        tabla = (TableLayout) findViewById(R.id.tbldatosusuario);
        tabla.setPadding(0, 0, 0, 0);
        layoutFila = new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT);//(int w, int h, float initWeight)

        Bundle bundle = getIntent().getExtras();

        lblanomaliactual.setText(bundle.getString("observacion").trim());

        lbldocumentonovedad.setText(bundle.getString("docnovedad").trim());
        String codigo = bundle.getString("codigo").trim();
        txtcuenta.setText(codigo);
        Log.e("error", "nombre nov " + bundle.getString("nombre").trim());
        txtusuario.setText(bundle.getString("nombre").trim());
        txtdir.setText(bundle.getString("dir1").trim());
        txtbarrio.setText(bundle.getString("barrio").trim());
        String ruta = bundle.getString("ruta").trim();
        txtMedidor.setText(bundle.getString("medidor").trim());
        lecturaActual = bundle.getString("lectura").trim();

        if (lecturaActual.trim().length() == 0) {
            lecturaActual = "0";
        }
        lectorActual = bundle.getString("lector").trim();
        ciclo = bundle.getString("ciclo").trim();
        mes = bundle.getString("periodo").trim();
        anno = bundle.getString("anno").trim();
        path = bundle.getString("path").trim();
        archivo = bundle.getString("archivo").trim();
        lblmensajenovedad.setText(bundle.getString("critica").trim());
        modeloCiclo = bundle.getString("ModeloCiclo").trim();
        serialPDA = bundle.getString("serial").trim();

        DecimalFormat formats = new DecimalFormat("###,###.##");
        txtvalorfact.setText(formats.format(Double.parseDouble(bundle.getString("valorfacturado"))));
        txtconsumofacturado.setText(bundle.getString("consumofacturado"));
        fuepromediado = bundle.getInt("fuepromediado");


        // kim
        parametroMenu = bundle.getString("MenuPrincipal");

        if (bundle.getString("MenuPrincipal") != null) {
            if (parametroMenu.equals("SI")) {
                txtcuenta.setText("");
                txtcuenta.setEnabled(true);

            }
        }

        if (!codigo.trim().equals("0")) {
            txtrutaactual.setText(ruta.trim());
            txtrutaanterior.setText("");
            rutaact = ruta.trim();
        } else {
            txtrutaactual.setEnabled(true);
            txtrutaanterior.setText(ruta.trim());
            txtrutaactual.setText("0");
            rutaact = "0";
        }

        resource = this.getResources();

        caracter = "\r\n";
       // serialPDA = getSerialNumber();

        LlenarListaCliente("Ciclo", ciclo);
        LlenarListaCliente("Anno", anno);
        LlenarListaCliente("Periodo", mes);
        LlenarListaCliente("Lector", lectorActual);
        LlenarListaCliente("Archivo", archivo);
        LlenarListaCliente("Cod Obser.", txtobservacion.getText().toString());
        LlenarListaCliente("Observacion", lblanomaliactual.getText().toString());
        LlenarListaCliente("Modelo Ciclo", modeloCiclo.trim());

        ModuloNovedadLectura_Load();

        if (codigo.trim().equals("0"))
            txtcuenta.setEnabled(true);

        if (codigo.trim().equals("999999999")) {
            txtcuenta.setEnabled(true);
            //txtcuenta.setFocusable(true);
        }

        txtobservacion.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        txtbarrio.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        txtusuario.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        txtdir.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        logfile = new File(path + "/DATOSDESALIDA/LOGEVENTOS.LOG"); //Ax: para escribir log de eventos

        txtobservacion.addTextChangedListener(new TextWatcher() { //Ax: Listener para controlar la entrada de texto en el informe

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {

                bntgrabar.setEnabled(true);
                txtobservacion.setEnabled(true);
                /* String c = txtobservacion.getText().toString().trim();

                if (c.length() > 10) {
                    bntgrabar.setEnabled(true);
                } else {
                    bntgrabar.setEnabled(false);
                }

                if (lblmensajenovedad.getText().toString().trim().toUpperCase() == "NOVEDAD ENTREGA") {
                    bntgrabar.setEnabled(true);
                }*/
            }
        });

        txtobservacion.setOnFocusChangeListener(new View.OnFocusChangeListener() { //Ax: cuando pierde focus
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {

                    String c = txtobservacion.getText().toString().trim();

                    if (c.trim().length() < 10) {
                        mensajes("La Observacion debe tener mas de 10 caracteres");
                     //   bntgrabar.setEnabled(false);
                        return;
                    }

                    if (c.contains(";") || c.contains(",")) {
                        c = c.replace(",", "").replace(";", "");
                        txtobservacion.setText(c);
                    }

                }
            }
        });

        txtlecturaennovedad.setOnFocusChangeListener(new View.OnFocusChangeListener() { //Ax: cuando pierde focus
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {

                    String c = txtlecturaennovedad.getText().toString().trim();

                    try {

                        // execute when loses focus
                        if (utils.parseStringToDouble(lecturaActual.replace('-', '0')) != utils.parseStringToDouble(c) && utils.parseStringToDouble(c) != 9999999 && !modeloCiclo.trim().equals("E") && lblanomaliactual.getText().toString().substring(0, 2).equals("88")) {

                            if (!lblmensajenovedad.getText().toString().trim().toUpperCase().equals("NOVEDAD ENTREGA")) {
                                mensajes("Lectura Diferente a la digitada en el proceso de lectura debe incluir la misma");
                                //txtlecturaennovedad.requestFocus();
                                return;
                            }
                        }
                    } catch (Exception ex) {
                    }

                    if (c.equals("") && obligaLectura()) {
                        txtlecturaennovedad.setText("0");
                    }
                    return;
                }
            }
        });

        imgbtnhablar.setOnClickListener(new View.OnClickListener() { ///8888888
            public void onClick(View v) {
                Intent intentActionRecognizeSpeech = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

                intentActionRecognizeSpeech.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "es-MX");
                try {
                    startActivityForResult(intentActionRecognizeSpeech, RECOGNIZE_SPEECH_ACTIVITY);
                } catch (ActivityNotFoundException a) {
                    mensajes("Error con Microfono");
                }
            }
        });


        imgbtnmenuanomalia.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialogEscogerLista(Op_cmbdescausal, "Seleccione novedad real", "llenarlistacliente");
            }
        });

        imgbtncmbconexion.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialogEscogerLista(Op_cmbconexion, "Conexion del Medidor", "conexionmedidor");
            }
        });

        imgbtncmbtipomedidor.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialogEscogerLista(Op_cmbtipomedidor, "Tipo de medidor", "tipomedidor");
            }
        });

        imgbtncmbclaseservicio.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialogEscogerLista(Op_cmbclaseservicio, "Seleccion Clase de Servicio", "seleccionclaseservicio");
            }
        });

        imgbtncmbcorriente.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialogEscogerLista(Op_cmbcorriente, "Valor Corriente Medidor", "valorcorrientemedidor");
            }
        });

        imgbtncmbmarca.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialogEscogerLista(Op_cmbmarca, "Marca del medidor actual", "marcadelmedidoractual");
            }
        });

        txtcmbdescausal.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialogEscogerLista(Op_cmbdescausal, "comentario", "comentario");//txtcmbtipomedidor
            }
        });

        bntgrabar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (v != null) {
                    InputMethodManager inm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inm.hideSoftInputFromWindow(v.getWindowToken(),0);
                }
                if (obligaLectura())
                    Guardar();//99999
            }
        });

        //txtlecturaennovedad.requestFocus();

    }//End Oncreate

    @Override
    public void onBackPressed() {
        mensajes("Debe completar los campos y guardar");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RECOGNIZE_SPEECH_ACTIVITY:

                if (resultCode == Activity.RESULT_OK && null != data) {
                    ArrayList<String> speech = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String strSpeech2Text = txtobservacion.getText().toString();
                    //EditText donde vas a mostrar el texto capturado
                    txtobservacion.setText(strSpeech2Text + " " + speech.get(0));
                }
                break;
            default:
                break;
        }
    }

    private boolean obligaLectura() {
        String msg = lblmensajenovedad.getText().toString().trim();

        if (msg.equals("CUENTA NUEVA") || msg.equals("NOVEDAD ESPECIAL") || msg.equals("NOVEDAD ENTREGA")) {
            return true;
        } else {
            if (txtlecturaennovedad.getText().toString().trim().isEmpty()) {
                mensajes("Debe llenar el primer campo: LECTURA TOMADA");
                return false;
            } else {
                return true;
            }
        }
    }

//    public void getSendData(String snumero) { //Ax. envia datos a la actividad que la llamo y cierra esta
//
//        Intent iBackActivity = new Intent(this, MenuDeLiquidacion.class);
//        iBackActivity.putExtra("snumero", snumero);
//        setResult(RESULT_OK, iBackActivity);
//        finish();
//    }

    public void Guardar() {

        if (txtlecturaennovedad.getText().toString().trim().equals("")) {
            txtlecturaennovedad.setText("0");
        }

        if (txtrutaposterior.getText().toString().trim().equals("")) {
            txtrutaposterior.setText("0");
        }

        if (txtrutaanterior.getText().toString().trim().equals("")) {
            txtrutaanterior.setText("0");
        }

        if (txtrutapropuesta.getText().toString().trim().equals("")) {
            txtrutapropuesta.setText("0");
        }

        if (txtcedula.getText().toString().trim().equals("")) {
            txtcedula.setText("0");
        }

        if (txtcuenta.getText().toString().trim().equals("")) {
            txtcuenta.setText("0");
        }

        if (!ValidarDatosAGrabar() || modeloCiclo.trim().equals("EC")) {
            Grabar();
            getSendData();
        } else {
            mensajes("FALTA INFORMACION EN:\n" + cadenafaltante + "\n....SON OBLIGATORIOS");
        }
    }

    public void getSendData() { //Ax. envia datos a la actividad que la llamo y cierra esta

        Intent iBackActivity = new Intent(this, MenuDeLiquidacion.class);
        setResult(RESULT_OK, iBackActivity);
        finish();
    }

    public boolean ValidarDatosAGrabar() {

        cadenafaltante = "";

        if (txtcuenta.getText().toString().trim().equals(""))
            cadenafaltante += "Falta Cuenta,";
        /*if (txtusuario.getText().toString().trim().equals(""))
            cadenafaltante += "Usuario,";
        if (txtdir.getText().toString().trim().equals(""))
            cadenafaltante += "Direccion,";
        if (txtclaseservicio.getText().toString().trim().equals(""))
            cadenafaltante += "Clase Servicio,";
        if (txtMedidor.getText().toString().trim().equals(""))
            cadenafaltante += "Medidor,";*/

        if (txtlecturaennovedad.getText().toString().trim().equals(""))
            if (!lblmensajenovedad.getText().toString().trim().toUpperCase().equals("NOVEDAD ENTREGA"))
                cadenafaltante += "Lectura,";
            else
                txtlecturaennovedad.setText("0");

      //  if (desCausalNovedad.trim().equals(""))
      //      cadenafaltante += "Se Requiere la novedad,";

        Log.e("errora",txtobservacion.getText().toString().length()+" data "+txtobservacion.getText().toString());
        if(txtobservacion.getText().toString().length() < 10){
            cadenafaltante += "Se Requiere la novedad, Por lo menos 10 caracteres ";
        }

        if (fuepromediado == 1) {
            // if (realizarAforos == 0)
            //    cadenafaltante += "Se Obligan Aforos,";
        }

        if (lblmensajenovedad.getText().toString().trim().equals("CUENTA NUEVA"))

            if (txtMedidor.getText().toString().trim().equals("") || txtmarca.getText().toString().trim().equals("") || txtconexion.getText().toString().trim().equals("") || txtanno.getText().toString().trim().equals("") || txtw.getText().toString().trim().equals("") || txtk.getText().toString().trim().equals("") || txtcorriente.getText().toString().trim().equals("") || txttipomedidor.getText().toString().trim().equals(""))
                //    cadenafaltante += " ES REQUERIDA LA INFORMACION DEL MEDIDOR NUEVO,";

                if (parametroMenu != null) {
                    if (parametroMenu.equals("SI")) {
                        if (txtcuenta.getText().toString().equals("") || txtcuenta.getText().toString().equals("0")) {
                            //   cadenafaltante += "SE REQUIERE NUMERO DE CUENTA";
                        }
                    }
                }

        if (cadenafaltante.trim().equals("")) {
            return (false);
        } else
            return (true);
    }

    public void Grabar() {

        RegistrarNovedadPredio(grabado);

        if (grabado == 0)
            mensajes("PROBLEMAS AL GRABAR DATOS...");
    }

    private int RegistrarNovedadPredio(int grabar) {
        try {
            File miNovArchnov = new File(AbrirNovedad());

            if (miNovedad.abrir_Novedades(miNovArchnov.getAbsolutePath())) {

                LLenarDatosAlaBase();

                if (miNovedad.encontro_Novedades == 0) {
                    miNovedad.total_Novedades++;
                    miNovedad.ultimo_Novedades = miNovedad.total_Novedades;
                    miNovedad.encontro_Novedades = miNovedad.ultimo_Novedades;
                }
                miNovedad.posicion_Novedades(miNovedad.ultimo_Novedades, miNovedad.LONGITUD_REGISTRO);
                miNovedad.escribir_Novedades(miNovedad.ultimo_Novedades);
                miNovedad.Cerrar_Novedades();
                grabado = 1;
            } else {
                mensajes("ARCHIVO DE NOVEDAD DE LA RUTA NO EXISTE ");
                return (0);
            }
        } catch (Exception ex) {
            mensajes("ERROR DE ESTRUCTURAS EN NOVEDADES, ALERTA DE ERROR" + ex.getMessage().substring(0, 10));
            utils.Log(logfile, "[ModuloCaptNovLect] RegistrarNovedadPredio() ; " + ex.getMessage());
            finish();
            //return (0);
        }
        return (1);
    }

    private void LLenarDatosAlaBase() {
        try {
            String direccion_y_Enviado = String.format("%1$-32s", txtdir.getText().toString().trim()).substring(0, 31); //Ax: antes (0.31)  y "%1$-32s";
            direccion_y_Enviado = direccion_y_Enviado + "X";//Ax: Annade  'X' (No enviado) a lo ultimo de la direccion para despues usarlo y saber si fue enviado o no

            miNovedad.setnovedades_numeroacta(lbldocumentonovedad.getText().toString().trim());
            miNovedad.setnovedades_ciclo(ciclo);
            miNovedad.setnovedades_codobservacion(lblanomaliactual.getText().toString().substring(0, 2));
            miNovedad.setnovedades_observacion(lblanomaliactual.getText().toString().trim());
            miNovedad.setnovedades_fecha(fecha);
            miNovedad.setnovedades_cuenta(txtcuenta.getText().toString().trim());
            Log.e("error", txtusuario.getText().toString().trim() + " retun especiales " + VariablGlobal.remplazarCaracteresEspeciales(txtusuario.getText().toString().trim()));
            miNovedad.setnovedades_nombreusuario(VariablGlobal.remplazarCaracteresEspeciales(txtusuario.getText().toString().trim()));
            miNovedad.setnovedades_cedula(txtcedula.getText().toString().trim());
            miNovedad.setnovedades_direccion(direccion_y_Enviado);
            miNovedad.setnovedades_barrio(txtbarrio.getText().toString().trim());
            miNovedad.setnovedades_telefono(txttelefono.getText().toString().trim());
            miNovedad.setnovedades_rutaanterior(txtrutaanterior.getText().toString().trim());
            miNovedad.setnovedades_rutaactual(txtrutaactual.getText().toString().trim());
            miNovedad.setnovedades_rutapropuesta(txtrutapropuesta.getText().toString().trim());
            miNovedad.setnovedades_rutaposterior(txtrutaposterior.getText().toString().trim());
            miNovedad.setnovedades_marca(txtmarca.getText().toString().trim());
            miNovedad.setnovedades_contador(txtMedidor.getText().toString().trim());
            miNovedad.setnovedades_conexion(txtconexion.getText().toString().trim());
            miNovedad.setnovedades_anno(txtanno.getText().toString().trim());
            miNovedad.setnovedades_k(txtk.getText().toString().trim());
            miNovedad.setnovedades_w(txtw.getText().toString().trim());
            miNovedad.setnovedades_corriente(txtcorriente.getText().toString().trim());
            miNovedad.setnovedades_tipomedidor(txttipomedidor.getText().toString().trim());
            miNovedad.setnovedades_lectura(String.format("%1$7s", txtlecturaennovedad.getText().toString().trim()).replace(" ", "0"));
            miNovedad.setnovedades_sellotapa(rutaact);
            miNovedad.setnovedades_sellobornera(txtsellobornera.getText().toString().trim());
            miNovedad.setnovedades_sellogabinete(txtsellogabinete.getText().toString().trim());
            miNovedad.setnovedades_claseservicio(txtclaseservicio.getText().toString().trim());
            txtobservacion.setText(txtobservacion.getText().toString().replace(',', '.'));
            miNovedad.setnovedades_comentario(VariablGlobal.remplazarCaracteresEspeciales(txtobservacion.getText().toString().trim()));
            miNovedad.setnovedades_lector(lectorActual);
            miNovedad.setnovedades_mes(mes);
            miNovedad.setnovedades_anno(txtanno.getText().toString().trim());
            miNovedad.setnovedades_annoperiodo(anno);
            miNovedad.setnovedades_tiponovedad(tipoNovedad.trim());
            miNovedad.setnovedades_causalnovedad(causalNovedad.trim());
            miNovedad.setnovedades_descausalnovedad(String.format("%1$2s", desCausalNovedad.trim()).replace(" ", "0"));
            miNovedad.setnovedades_CRNL(caracter);
            VariablGlobal.setUltimaNovedad(String.format("%1$2s", causalNovedad.trim()).replace(" ", "0") + String.format("%1$2s", desCausalNovedad.trim()).replace(" ", "0"));

        } catch (Exception ex) {
            mensajes("Error 991 Problema en reemplazar");
            utils.Log(logfile, "[ModuloCaptNovLect] LLenarDatosAlaBase() ; " + ex.getMessage());
        }
    }

    public void Aforos() {

        try {
            String cuenta = txtcuenta.getText().toString().trim();

            if (cuenta.equals("0") || cuenta.equals("")) {
                if (lbldocumentonovedad.getText().toString().trim().length() > 9) {
                    cuenta = lbldocumentonovedad.getText().toString().trim().substring(0, 9);
                }
            }

            Bundle bundle = new Bundle();

            bundle.putString("documento", lbldocumentonovedad.getText().toString());
            bundle.putString("ciclo", ciclo);
            bundle.putString("mes", mes);
            bundle.putString("anno", anno);
            bundle.putString("lector", lectorActual);
            bundle.putString("cuenta", cuenta);
            bundle.putString("directorio", path);
            bundle.putString("archivo", archivo);

            // Intent i = new Intent(this, ModuloActualizaAforos.class);
            //  i.putExtras(bundle);
            //  startActivity(i);

            realizarAforos = 1;
        } catch (Exception ex) {
            mensajes("Problemas en Proceso de Aforos");
            utils.Log(logfile, "[ModuloCaptNovLect] Aforos() ; " + ex.getMessage());
        }
    }

    public void ModuloNovedadLectura_Load() {

        Limpia_textos();
        // imgbtnmenugrupoanomalia.setEnabled(false);
        // txtcmbgrupodescausal.setEnabled(false);

        String msgnov = lblmensajenovedad.getText().toString().trim();

        // txtcmbgrupodescausal.setText(msgnov);

        if (msgnov.equals("CONSUMO CON ANOMALIA"))
            causalNovedad = "01";
        else if (msgnov.equals("CONSUMO CERO"))
            causalNovedad = "02";
        else if (msgnov.equals("CONSUMO ALTO"))
            causalNovedad = "03";
        else if (msgnov.equals("CONSUMO BAJO"))
            causalNovedad = "04";
        else if (msgnov.equals("NOVEDAD ENTREGA"))
            causalNovedad = "06";//noveda varios
        else {
            causalNovedad = "05";//noveda varios
            causalNovedad2 = "07";
            causalNovedad3 = "08";
            //  imgbtnmenugrupoanomalia.setEnabled(true);
            //  txtcmbgrupodescausal.setEnabled(true);
            //  txtcmbgrupodescausal.setText("");
        }

        AsignarValoresACombos();
        BuscarDocumentoNovedad();
        realizarAforos = 0;
    }

    private void Limpia_textos() {

        txtrutapropuesta.setText("");
        txtclaseservicio.setText("R");
        txtrutaposterior.setText("");
        txtconexion.setText("");
        txtsellobornera.setText("");
        txtsellogabinete.setText("");
        txtk.setText("");
        txtw.setText("");
        txtcorriente.setText("");
        txttipomedidor.setText("A");
        txtcorriente.setText("15-60");

        Calendar calendar = Calendar.getInstance();
        String Hora = String.format("%1$2s", calendar.get(Calendar.HOUR_OF_DAY) + "").replace(" ", "0");
        String Minutos = String.format("%1$2s", calendar.get(Calendar.MINUTE) + "").replace(" ", "0");
        String segundos = String.format("%1$2s", calendar.get(Calendar.SECOND) + "").replace(" ", "0");

        String temp = lbldocumentonovedad.getText().toString().trim() + Hora + Minutos + segundos;
        lbldocumentonovedad.setText(temp);

        String Dia = (calendar.get(Calendar.DAY_OF_MONTH) + "").trim();
        String mes = ((calendar.get(Calendar.MONTH) + 1) + "").trim();//Ax: +1 empieza desde 0
        String ano = (calendar.get(Calendar.YEAR) + "").trim();

        if (Dia.length() < 2)
            Dia = "0" + Dia;
        if (mes.length() < 2)
            mes = "0" + mes;
        if (ano.length() < 4)
            ano = "20" + ano;

        fecha = Dia + "/" + mes + "/" + ano + " " + Hora + ":" + Minutos + ":" + segundos; //repeticiones

        txttelefono.setText("");

        if (lblanomaliactual.getText().toString().substring(0, 2).equals("77")) {
            txtusuario.setText("");
            txtcedula.setText("");
            txtdir.setText("");
            txtrutaactual.setEnabled(true);
            txtMedidor.setText("0");
        } else
            txtrutaactual.setEnabled(false);
    }

    private void AsignarValoresACombos() {

        try {
            String temp = "B | Bifasica" + ";M | MonoFasica" + ";T | Trifasica ";

            Op_cmbconexion = temp.split(";");

            temp = "A | Activa" + ";R | Reactiva";

            Op_cmbtipomedidor = temp.split(";");

            temp = "R  | Residencial" + ";C  | Comercial" + ";I  | Industrial";

            Op_cmbclaseservicio = temp.split(";");

            temp = "15-60" + ";20-100" + ";10-60" + ";20-80" + ";1.5-10" + ";1.5-10" + ";1.5-6"
                    + ";10-20" + ";10-30" + ";10-40" + ";10-50" + ";15-100" + ";15-30" + ";15-40"
                    + ";15-45" + ";2.5-10" + ";20-120" + ";20-60" + ";30-90" + ";40-160" + ";5-10"
                    + ";5-15" + ";5-30" + ";5-6" + ";50-100" + ";50-150" + ";50-75";

            Op_cmbcorriente = temp.split(";");

            temp = "05  | OTRAS ANOMALIAS" + ";07  | NOVEDAD DE SERVICIO" + ";08  | ??";

            Op_cmbdescausal_2 = temp.split(";");

            temp = "";
            //llenar combos con archivos planos
            temp = "";
            FileReader r = null;
            File Archivo_marcas = new File(path + "/DATOSDEENTRADA/MARCAS.TXT");

            try {
                if (Archivo_marcas.exists()) {

                    r = new FileReader(Archivo_marcas);
                    BufferedReader reader = new BufferedReader(r);
                    String linea = "";

                    while ((linea = reader.readLine()) != null) {

                        String[] data = linea.replace("|", ";").split(";");
                        temp += data[0] + "|" + data[1] + ";";

                      /*  if (temp.length() < 1) {
                            linea = linea.substring(0, linea.lastIndexOf(";") - 1);
                            temp = temp + linea.trim().replace(";", " | ");
                        } else {
                            linea = linea.substring(0, linea.lastIndexOf(";") - 1);
                            linea = linea.trim().replace(";", " | ");
                            temp = temp + ";" + linea;
                        }*/
                    }
                    Op_cmbmarca = temp.split(";");

                } else {
                    mensajes("NO EXISTE ARCHIVO \n" + Archivo_marcas + " \nINFORMAR AL SUPERVISOR!!");
                    txtcmbmarca.setText("");
                    txtcmbmarca.setEnabled(true);
                }
            } catch (IOException e) {
                throw new RuntimeException("Leer marcas.txt, " + e.getMessage());
            } finally {
                try {
                    if (r != null) r.close();
                } catch (IOException e) {
                }
            }

        } catch (Exception ex) {
            Log.e("error", " llenar combps " + ex.getMessage());
            mensajes("Carga de menus desplegables..." + "\n Error de estructura");
            utils.Log(logfile, "[ModuloCaptNovLect] AsignarValoresACombos() ; " + ex.getMessage());
            finish();
        }
        //Ax: cmbTipo no se usa

        //  LlenarCodNovedades();

        AsignarIdNovedad(causalNovedad, causalNovedad2, causalNovedad3);
    }

    private void LlenarCodNovedades() {
        Log.e("error", "textos LlenarCodNovedades ");

        try {
            String txt = "";
            if (lblmensajenovedad.getText().toString().trim().toUpperCase().equals("CONSUMO ALTO")
                    || lblmensajenovedad.getText().toString().trim().toUpperCase().equals("CONSUMO MUY ALTO")
                    || lblmensajenovedad.getText().toString().trim().toUpperCase().equals("CONSUMO PROMEDIADO X ALTO")) {
                txt = "0101 MAYOR USO DEL SERVICIO          "
                        + "|0102 CONSUMO DE DOS O MAS FAMILIAS   "
                        + "|0103 CAMBIO DE CONTADOR              "
                        + "|0104 INMUEBLE HABITADO POR TEMPORADA "
                        + "|0105 CUENTA NUEVA                    "
                        + "|0106 INTEGRADOR                      "
                        + "|0107 INMUEBLE EN REMODELACION        "
                        + "|0108 LECTURA MAYOR FACTURADA         "
                        + "|0109 MEDIDOR FRENADO O DANNADO       "
                        + "|0127 ERROR ORIGINADO POR INSPECTOR    "
                        + "|0128 USUARIO NO PERMITE TOMAR LECTURA "
                        + "|0199 OTROS (Especificar)             ";

            } else if (lblmensajenovedad.getText().toString().trim().toUpperCase().equals("CONSUMO CERO")) {
                txt = "0203 CAMBIO DE CONTADOR              "
                        + "|0204 INMUEBLE HABITADO POR TEMPORADA "
                        + "|0206 INTEGRADOR                      "
                        + "|0207 INMUEBLE EN REMODELACION        "
                        + "|0208 ANOMALIA EN EL INMUEBLE         "
                        + "|0209 MEDIDOR FRENADO O DANNADO       "
                        + "|0210 INMUEBLE DESHABILITADO          "
                        + "|0211 SERVICIO SUSPENDIDO             "
                        + "|0213 POSIBLE FRAUDE                  "
                        + "|0227 ERROR ORIGINADO POR INSPECTOR    "
                        + "|0228 USUARIO NO PERMITE TOMAR LECTURA "
                        + "|0299 Otros (Especificar)             ";
            } else if (lblmensajenovedad.getText().toString().trim().toUpperCase().equals("CONSUMO BAJO") || lblmensajenovedad.getText().toString().trim().toUpperCase().equals("CONSUMO MUY BAJO")) {
                txt = "0303 CAMBIO DE CONTADOR              "
                        + "|0304 INMUEBLE HABITADO POR TEMPORADA "
                        + "|0306 INTEGRADOR                      "
                        + "|0307 INMUEBLE EN REMODELACION        "
                        + "|0308 ANOMALIA EN EL INMUEBLE         "
                        + "|0309 MEDIDOR FRENADO DANNADO         "
                        + "|0310 INMUEBLE DESHABITADO            "
                        + "|0311 SERVICIO SUSPENDIDO             "
                        + "|0312 MENOR USO DEL SERVICIO          "
                        + "|0327 ERROR ORIGINADO POR INSPECTOR    "
                        + "|0328 USUARIO NO PERMITE TOMAR LECTURA "
                        + "|0399 Otros (Especificar)             ";
            } else {
                if (lblmensajenovedad.getText().toString().trim().toUpperCase().equals("NOVEDAD ENTREGA")) {
                    txt = "0401 ENTREGA EN VECINO               "
                            + "|0402 ENTREGA EN EL CABILDO           "
                            + "|0403 CONSUMO CORRECTO AL COBRO       "
                            + "|0499 Otros (Especificar en observacion)";
                } else {
                    txt = "0503 CAMBIO DE CONTADOR               "
                            + "|0505 CUENTA NUEVA                     "
                            + "|0513 POSIBLE FRAUDE                   "
                            + "|0514 INMUEBLE CON SERV. SIN LEGALIZAR "
                            + "|0515 INMUEBLE CON MEDIDOR ROTO        "
                            + "|0516 INMUEBLE SIN CONTADOR HAY CONSUMO"
                            + "|0517 RUTA DESUBICADA                 "
                            + "|0518 INMUEBLE DEMOLIDO                "
                            + "|0519 INMUEBLE ABANDONADO              "
                            + "|0520 CONTADOR SIN SELLOS              "
                            + "|0521 INMUEBLE O DIRECCION NO EXISTE   "
                            + "|0522 CUENTA REPETIDA                  "
                            + "|0523 NOVEDAD DE SERVICIO              "
                            + "|0524 PERDIDA DE REGISTRO              "
                            + "|0525 ACTUALIZACION DE DATOS           "
                            + "|0526 CAUSAL 14 SIN CONSUMO            "
                            + "|0527 ERROR ORIGINADO POR INSPECTOR    "
                            + "|0528 USUARIO NO PERMITE TOMAR LECTURA "
                            + "|0599 OTROS (Especificar)              ";
                }
            }
            // Log.e("error","textos nov real "+txt);
            Op_cmbdescausal = txt.split("\\|");

        } catch (Exception ex) {
            //  Log.e("error","textos nov real1 "+ex.getMessage());
            utils.Log(logfile, "[ModuloCaptNovLect] LlenarCodNovedades() ; " + ex.getMessage());
        }
    }

    private void AsignarIdNovedad(String codigo, String codigo2, String codigo3) {

        String cadenaleida = "";
        File fileName = new File(path + "/DATOSDEENTRADA/IDNOVEDADES.TXT");
        int contar = 0;
        String temp = "";

        if (fileName.exists()) {

            FileReader r = null;
            try {
                r = new FileReader(fileName);
                BufferedReader reader = new BufferedReader(r);
                String linea = "";

                while ((linea = reader.readLine()) != null) {

                    if (linea.length() >= 36) {
                        Log.e("error", codigo.trim() + "- codigo -" + linea.substring(0, 2).trim() + "-");
                        if (codigo.trim().equals(linea.substring(0, 2).trim())) {//antes && (codigo2.trim().equals("") && (codigo3.trim().equals("")))) {
                            Log.e("error", "son iguales ");

                           /* if (temp.equals("")) {
                                temp = (linea.substring(0, 4) + linea.substring(5, 45));
                            } else {*/
                            if (contar >= 1) {
                                temp = temp + "|" + (linea.substring(0, 4) +" "+ linea.substring(5, 50));
                            } else {
                                temp = temp + "" + (linea.substring(0, 4) + " " + linea.substring(5, 50));
                            }
                            ++contar;

                            //  }
                        } /*else {
                            if (codigo.trim().equals(linea.substring(0, 2).trim()) || (codigo2.trim().equals(linea.substring(0, 2).trim()) && (codigo3.trim().equals("")))) {
                                ++contar;

                                if (temp.equals("")) {
                                    temp = (linea.substring(0, 4) + linea.substring(5, 45));
                                } else {
                                    temp = temp + "]" + (linea.substring(0, 4) + linea.substring(5, 45));
                                }
                            } else {
                                if (codigo.trim().equals(linea.substring(0, 2).trim()) || (codigo2.trim().equals(linea.substring(0, 2).trim()) || (codigo3.trim().equals(linea.substring(0, 2).trim())))) {
                                    ++contar;

                                    if (temp.equals("")) {
                                        temp = (linea.substring(0, 4) + linea.substring(5, 45));
                                    } else {
                                        temp = temp + "]" + (linea.substring(0, 4) + linea.substring(5, 45));
                                    }
                                }
                            }
                        }*/
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Leer IDNOVEDADES.txt, " + e.getMessage());
            } finally {
                try {
                    if (r != null) r.close();
                } catch (IOException e) {
                }
            }
        }
        if (contar == 0) {

            temp = "03 CAMBIO DE CONTADOR                                "
                    + "|05 CUENTA NUEVA                                     "
                    + "|13 POSIBLE FRAUDE                                   "
                    + "|17 RUTA DES UBICADA                                 "
                    + "|28 USUARIO NO PERMITE TOMAR LECTURA                 "
                    + "|99 OTROS (EXPECIFICAR)                              "
                    + "|95 MEDIDOR EN LABORATORIO                           "
                    + "|96 SERVICIO SUSPENDIDO                              "
                    + "|97 CASA DESHABITADA                                 "
                    + "|98 SIN ACOMETIDA                                    "
                    + "|99 OTROS (EXPECIFICAR)                              ";

           /* if (temp.equals("")) {
                temp = causalNovedad + "99 NO EXISTEN NOVEDADES AL INDICADOR";
            } else {
                temp = temp + "]" + causalNovedad + "99 NO EXISTEN NOVEDADES AL INDICADOR";
            }*/
        }

        Op_cmbdescausal = temp.split("\\|");

        // Op_cmbdescausal = null;
        // Op_cmbdescausal = temp.split("]");

        if (Op_cmbdescausal == null || Op_cmbdescausal.length == 0) {
            LlenarCodNovedades();
        }
    }

    private String AbrirNovedad() {//Ax: creado por problemas en ramdomaccesfile no abre extension uppercase o lowercase

        String x = path + "/DATOSDESALIDA/NOVEDADES.SDA";
        String y = path + "/DATOSDESALIDA/NOVEDADES.SDA";

        try {
            File z = new File(x);

            if (!z.exists()) {
                z.createNewFile();
            }

            if (miNovedad.abrir_Novedades(y)) {
                miNovedad.Cerrar_Novedades();
                return x;
            } else {
                return y;
            }
        } catch (Exception ex) {
            return y;
        }
    }

    private void BuscarDocumentoNovedad() {

        File miNovArchnov = new File(AbrirNovedad());

        try {

            if (miNovedad.abrir_Novedades(miNovArchnov.getAbsolutePath())) {
                if (!txtcuenta.getText().toString().equals("0") && !txtcuenta.getText().toString().trim().equals("")) {
                    miNovedad.buscar_Novedades = txtcuenta.getText().toString();
                    miNovedad.buscarSecuencial_novedades(miNovedad.buscar_Novedades, lbldocumentonovedad.getText().toString());
                    if (miNovedad.encontro_Novedades > 0) {
                        IniciarDatosTextosModulo();
                    }
                }
                miNovedad.Cerrar_Novedades();
            } else {
                mensajes("NO EXISTE ARCHIVO DE NOVEDADES\n" + miNovArchnov.getAbsolutePath() + "\nALERTA DE ERROR");
                return;
            }
        } catch (Exception ex) {
            mensajes("ERROR DE ESTRUCTURA EN EL ARCHIVO DE NOVEDADES\n" + miNovArchnov.getAbsolutePath() + "\nALERTA DE ERROR");
            finish();
        }
    }

    private void IniciarDatosTextosModulo() {
        tabla.removeAllViews();
        tabla.refreshDrawableState();

        ciclo = miNovedad.getnovedades_CICLO().trim();
        LlenarListaCliente("Ciclo", ciclo);
        mes = miNovedad.getnovedades_MES().trim();
        LlenarListaCliente("Mes", mes);
        anno = miNovedad.getnovedades_ANNOPERIODO().trim();
        LlenarListaCliente("Annio", anno);
        fecha = miNovedad.getnovedades_FECHA().trim();
        LlenarListaCliente("Fecha", fecha);

        lectorActual = miNovedad.getnovedades_LECTOR().trim();
        LlenarListaCliente("Lector", lectorActual);

        txtcuenta.setText(miNovedad.getnovedades_CUENTA().trim());
        txtbarrio.setText(miNovedad.getnovedades_BARRIO().trim());
        txtcedula.setText(miNovedad.getnovedades_CEDULA().trim());
        txtusuario.setText(miNovedad.getnovedades_NOMBREUSUARIO().trim());
        txtdir.setText(miNovedad.getnovedades_DIRECCION().substring(0, 31).trim());
        txtrutaanterior.setText(miNovedad.getnovedades_RUTAANTERIOR().trim());
        txtrutaactual.setText(miNovedad.getnovedades_RUTAACTUAL().trim());
        txtrutapropuesta.setText(miNovedad.getnovedades_RUTAPROPUESTA().trim());
        txtclaseservicio.setText(miNovedad.getnovedades_CLASESERVICIO().trim());
        txtrutaposterior.setText(miNovedad.getnovedades_RUTAPOSTERIOR().trim());
        txtMedidor.setText(miNovedad.getnovedades_CONTADOR().trim());
        txtmarca.setText(miNovedad.getnovedades_MARCA().trim());
        txtconexion.setText(miNovedad.getnovedades_CONEXION().trim());
        txtsellotapa.setText(miNovedad.getnovedades_SELLOTAPA().trim());
        txtsellobornera.setText(miNovedad.getnovedades_SELLOBORNERA().trim());
        txtsellogabinete.setText(miNovedad.getnovedades_SELLOGABINETE().trim());
        txtk.setText(miNovedad.getnovedades_K().trim());
        txtw.setText(miNovedad.getnovedades_W().trim());
        txtcorriente.setText(miNovedad.getnovedades_CORRIENTE().trim());
        txttipomedidor.setText(miNovedad.getnovedades_TIPOMEDIDOR().trim());
        txtobservacion.setText(miNovedad.getnovedades_COMENTARIO().trim());
        txttelefono.setText(miNovedad.getnovedades_TELEFONO().trim());
        lblanomaliactual.setText(miNovedad.getnovedades_OBSERVACION());
    }

    public void AlertDialogEscogerLista(final String[] msgs, String titulo, final String llamado) {

        ContextThemeWrapper cw = new ContextThemeWrapper(this, R.style.AlertDialogTheme); //Ax: el alert dialog toma el 'estilo' (tamaño de letra) desde Styles xml
        AlertDialog.Builder builder = new AlertDialog.Builder(cw);
        builder.setTitle(titulo);

        builder.setSingleChoiceItems(msgs, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {

                try {
                    switch (llamado) {

                        case "llenarlistagrupocliente": //Ax: Primer menu grupo Anomalia
                            //  txtcmbgrupodescausal.setText(Op_cmbdescausal_2[item]);
                            txtcmbdescausal.setText("");
                            AsignarIdNovedad(Op_cmbdescausal_2[item].substring(0, 2), "", "");
                            break;

                        case "llenarlistacliente": //Ax:  segundo menu Anomalia //**99
                            txtcmbdescausal.setText(Op_cmbdescausal[item]);
                            desCausalNovedad = Op_cmbdescausal[item].substring(3, 4);
                            causalNovedad = Op_cmbdescausal[item].substring(0, 2);
                            String textoObs = txtobservacion.getText().toString();
                            txtobservacion.setText(textoObs + txtcmbdescausal.getText().toString());
                            txtobservacion.setEnabled(true);
                            imgbtnhablar.setEnabled(true);
                            imgbtnhablar.setClickable(true);
                            //txtobservacion.requestFocus();
                            break;

                        case "marcadelmedidoractual": //Ax: tercer menu marca

                            if (selecionMarca == 0) {
                                selecionMarca = 1;
                            } else {
                                selecionMarca = 0;
                            }
                            txtmarca.setText(Op_cmbmarca[item].substring(0, 3));
                            txtcmbmarca.setText(Op_cmbmarca[item]);
                            //imgbtncmbconexion.requestFocus();
                            break;

                        case "conexionmedidor": //Ax: cuarto menu Conexion del Medidor
                            txtconexion.setText(Op_cmbconexion[item].substring(0, 1));
                            txtcmbconexion.setText(Op_cmbconexion[item]);
                            txtanno.requestFocus();
                            break;

                        case "valorcorrientemedidor": //Ax: quinto menu Valor corriente
                            txtcorriente.setText(Op_cmbcorriente[item]);
                            txtcmbcorriente.setText(Op_cmbcorriente[item]);
                            break;

                        case "tipomedidor": //Ax: sexto menu tipo de medidor
                            txttipomedidor.setText(Op_cmbtipomedidor[item].substring(0, 2));
                            txtcmbtipomedidor.setText(Op_cmbtipomedidor[item]);
                            //txtlecturaennovedad.requestFocus();
                            break;

                        case "seleccionclaseservicio":
                            txtclaseservicio.setText(Op_cmbclaseservicio[item].substring(0, 1));
                            txtcmbclaseservicio.setText(Op_cmbclaseservicio[item]);
                            txtMedidor.requestFocus();
                            break;
                        case "comentario":
                            desCausalNovedad = Op_cmbdescausal[item].substring(3, 4);
                            txtcmbdescausal.setText(Op_cmbdescausal[item]);
                            Log.e("error", txtcmbdescausal.getText().toString() + " ingresa " + Op_cmbdescausal[item]);
                            String textoObs2 = txtobservacion.getText().toString();
                            txtobservacion.setText(textoObs2 + txtcmbdescausal.getText().toString());
                            break;

                    }
                } catch (Exception ex) {
                    mensajes("PROBLEMAS EN SELECCION DE" + llamado);
                }
                dialog.dismiss();
            }
        });
        levelDialog = builder.create();
        levelDialog.setCancelable(false);
        levelDialog.setCanceledOnTouchOutside(false);
        levelDialog.show();
    }

    public void AlertDialogEscogerSiNo(String titulo, String mensaje, final String NombreMetodo) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(ModuloCaptNovLect.this);

        builder.setTitle(titulo);
        builder.setMessage(mensaje);
        // builder.setIcon(R.drawable.ic_launcher);

        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                switch (NombreMetodo) {
                    case "Aforos":
                        Aforos();
                        break;
                }
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                switch (NombreMetodo) {
                    case "Aforos":
                        break;
                }
            }
        });

        builder.create().show();
    }

    public void LlenarListaCliente(String campo, String valor) {

        TextView txtCampo = new TextView(this);
        TextView txtValor = new TextView(this);
        txtCampo.setTextSize(16);
        txtValor.setTextSize(16);
        txtCampo.setBackgroundResource(R.drawable.bg_bordecomun);
        txtValor.setBackgroundResource(R.drawable.bg_bordecomun);
        txtCampo.setPadding(2, 2, 2, 2);
        txtValor.setPadding(2, 2, 2, 2);

        txtCampo.setText(campo);
        if (campo.equals("Ciclo")) {
            txtCampo.setEnabled(true);
        }
        if (valor.length() > 24) {
            valor = valor.substring(0, 24);
        }
        txtValor.setText(valor);

        txtValor.setGravity(Gravity.LEFT);
        txtCampo.setGravity(Gravity.LEFT);

        fila = new TableRow(this);
        fila.setBackgroundColor(resource.getColor(R.color.grayBlueSoft));

        fila.addView(txtCampo);
        fila.addView(txtValor);
        tabla.addView(fila);
    }

//    // Captura numero IMEI del telefono
//    public String getSerialNumber() {
//
//        TelephonyManager tManager = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
//        String deviceIMEI = tManager.getDeviceId();
//        return deviceIMEI;
//    }

    private void mensajes(String msg) {
        Toast toast = Toast.makeText(ModuloCaptNovLect.this, msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 10, 50);
        toast.show();
    }
}
