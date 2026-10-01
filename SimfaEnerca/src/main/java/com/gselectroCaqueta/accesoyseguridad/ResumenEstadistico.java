package com.gselectroCaqueta.accesoyseguridad;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.gselectroCaqueta.tablas.EnvioGPS;
import com.gselectroCaqueta.tablas.TablaClienteSalida;
import com.gselectroCaqueta.tablas.TablaEntradaClientes;
import com.gselectroCaqueta.tablas.TablaMunicipios;
import com.gselectroCaqueta.tablas.TablaRegistroSalida;
import com.gsutil.Utils;

import java.io.File;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ResumenEstadistico extends Activity {

    public View.OnClickListener TablaListener; //Ax: escucha para capturar clic en rows de la tabla generada
    public int ClickedRow;//Ax: guarda el ID (cliente) al dar clic a una fila.
    public EnvioGPS misenvios = new EnvioGPS();
    ImageButton btnCerrar;
    ListView list;
    int CheckRadio = 1;
    String directorio;
    String aforador;
    String terminal;
    String archivoCargado;
    //  ListView list;
    String nombrePredio;
    String ultimaRutaSeleccionada = "";
    ScrollView scroll;
    TablaEntradaClientes tablaEntradaCliente = new TablaEntradaClientes();
    TablaClienteSalida infoClienteSalida = new TablaClienteSalida();
    TablaRegistroSalida infoRegistroSalida = new TablaRegistroSalida();
    TablaMunicipios municipio = new TablaMunicipios();
    TableLayout tabla;
    TableLayout cabecera;
    TableRow.LayoutParams layoutFila;
    TableRow.LayoutParams layoutRuta;
    TableRow.LayoutParams layoutNroMedidor;
    TableRow.LayoutParams layoutDireccion;
    TableRow.LayoutParams layoutNombre;// 200
    TableRow.LayoutParams layoutLectura;// 100
    TableRow.LayoutParams layoutAnomalia;// 60
    TableRow.LayoutParams layoutComentario;// 70
    TableRow.LayoutParams layoutInforme;// 150
    TableRow.LayoutParams layoutId;// 40
    TableRow fila;
    TextView txtRuta;
    TextView txtNroMedidor;
    TextView txtDireccion;
    TextView txtNombre;
    TextView txtLectura;
    TextView txtAnomalia;
    TextView txtComentario;
    TextView txtInforme;
    TextView txtId;
    int UltimoRegistro = 1; //Ax: para llenar la tabla de arriba antes de los conteos y no influir en otras cosas.
    int Ultimo = 0; //Ax: para llenar la tabla de arriba antes de los conteos y no influir en otras cosas.
    RadioGroup GrupoOpciones;
    TextView idLeidos;
    TextView txtNombreRuta;
    TextView txtFecha;
    TextView txtEstado;
    TextView txtTotal;
    TextView txtLeidos;
    TextView txtPendientes;

    ImageButton btnAtras1;
    ImageButton btnAdelante1;
    ImageButton btnPrimero1;
    ImageButton btnUltimo1;
    Resources rs;
    ArrayAdapter<String> item;
    Map<Integer, String> map = new HashMap<Integer, String>();//Ax: para guardar el id y el color de los row llenados
    boolean nofromclick = false; //Ax: indica si el regreso proviene de doble clic en tabla o boton
    //File logfile; //Ax: Es para los log de error
    int VarGlobregistroactual = 1; //Ax: para llenar la tabla de arriba antes de los conteos y no influir en otras cosas.
    private int MAX_FILAS = 1;
  //  Button btnCerrar2;
    public File logfile;
    Utils utils = new Utils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resumen_estadistico);

        rs = this.getResources();
        tabla = (TableLayout) findViewById(R.id.tablaResumen);
        cabecera = (TableLayout) findViewById(R.id.cabecera);
        layoutFila = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        layoutRuta = new TableRow.LayoutParams(290, TableRow.LayoutParams.WRAP_CONTENT);
        layoutNroMedidor = new TableRow.LayoutParams(230, TableRow.LayoutParams.WRAP_CONTENT);
        layoutDireccion = new TableRow.LayoutParams(500, TableRow.LayoutParams.WRAP_CONTENT);
        layoutNombre = new TableRow.LayoutParams(500, TableRow.LayoutParams.WRAP_CONTENT);
        layoutLectura = new TableRow.LayoutParams(200, TableRow.LayoutParams.WRAP_CONTENT);
        layoutAnomalia = new TableRow.LayoutParams(190, TableRow.LayoutParams.WRAP_CONTENT);
        layoutComentario = new TableRow.LayoutParams(220, TableRow.LayoutParams.WRAP_CONTENT);
        layoutInforme = new TableRow.LayoutParams(500, TableRow.LayoutParams.WRAP_CONTENT);
        layoutId = new TableRow.LayoutParams(100, TableRow.LayoutParams.WRAP_CONTENT);

        btnAdelante1 = (ImageButton) findViewById(R.id.btnAdelante1);
        btnAtras1 = (ImageButton) findViewById(R.id.btnAtras1);
        btnPrimero1 = (ImageButton) findViewById(R.id.btnPrimero1);
        btnUltimo1 = (ImageButton) findViewById(R.id.btnUltimo1);
       // btnCerrar2 = (Button) findViewById(R.id.btnCerrar2);

        scroll = (ScrollView) findViewById(R.id.scroll);
        //  list = (ListView) findViewById(R.id.list);
        GrupoOpciones = findViewById(R.id.GrupoOpciones);
        list = (ListView) findViewById(R.id.list);
        txtNombreRuta = (TextView) findViewById(R.id.txtNombreRuta);
        txtFecha = (TextView) findViewById(R.id.txtFecha);
        txtEstado = (TextView) findViewById(R.id.txtEstado);
        txtTotal = (TextView) findViewById(R.id.txtTotal);
        txtLeidos = (TextView) findViewById(R.id.txtLeidos);
        idLeidos= (TextView) findViewById(R.id.idLeidos);
        txtPendientes = (TextView) findViewById(R.id.txtPendientes);


        item = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        btnCerrar = (ImageButton) findViewById(R.id.btnCerrar);
        list = (ListView) findViewById(R.id.list);

        Bundle bundle = getIntent().getExtras();
        aforador = bundle.getString("aforador");
        terminal = bundle.getString("terminal");
        directorio = bundle.getString("directorioActual");
        nombrePredio = bundle.getString("nombrePredio");
        archivoCargado = nombrePredio;
        txtNombreRuta.setText("Ruta: "+nombrePredio);
        nombreArchivo();
        //logfile = new File(VariablesGlobales.directorioactual + "/DatosDeSalida/logEventos.log");
        logfile = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/LOGEVENTOS.LOG");
        File registro = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/REGISTRO.SDA");

        if (!registro.exists()) {

            Toast.makeText(getApplicationContext(), "No hay Ruta Cargada Para:\n" + nombrePredio, Toast.LENGTH_LONG).show();
            return;
        }

        realizarConteo();
        CheckRadio = 1;
        GrupoOpciones.check(R.id.OptTodas);

        GrupoOpciones.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.OptTodas:
                        CheckRadio = 1;
                        UltimoRegistro = 1;
                        VarGlobregistroactual = 1;
                        llenarTabla2(VarGlobregistroactual);
                        Toast.makeText(ResumenEstadistico.this,"Selecciono Todas",Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.OptProcesadas:
                        CheckRadio = 2;
                        UltimoRegistro = 1;
                        VarGlobregistroactual = 1;
                        llenarTabla2(VarGlobregistroactual);
                        Toast.makeText(ResumenEstadistico.this,"Selecciono Procesadas",Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.OptNoprocesada:
                        CheckRadio = 3;
                        UltimoRegistro = 1;
                        VarGlobregistroactual = 1;
                        llenarTabla2(VarGlobregistroactual);
                        Toast.makeText(ResumenEstadistico.this,"Selecciono No Procesadas",Toast.LENGTH_SHORT).show();
                        break;

                }
            }
        });
        btnCerrar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                nofromclick = true;
                ingresoMenuPrincipal();
            }
        });

        btnAtras1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                atrasadelante("tras");
            }
        });

        btnAdelante1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                atrasadelante("delante");
            }
        });

        btnPrimero1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                atrasadelante("primero");
            }
        });

        btnUltimo1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                atrasadelante("ultimo");
            }
        });

     /*   btnCerrar2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                nofromclick = true;
                ingresoMenuPrincipal();
            }
        });*/


        TablaListener = new View.OnClickListener() {

            public void onClick(View v) {

                try {
                    int ClickedRow_old = ClickedRow; //Ax: guardo el id anterior
                    ClickedRow = v.getId(); //Ax: Guardo el id actual

                    v.setBackgroundColor(Color.rgb(249, 241, 74)); //Ax: Subrayo la seleccionada en rojo

                    if (ClickedRow == ClickedRow_old) { //Ax: hubo dos clic de la misma fila
                        ingresoMenuPrincipal();
                    } else {
                        // Ax: Devuelvo el color a la fila anterior, para ello busco su id en un map
                        TableRow tableRow = (TableRow) findViewById(ClickedRow_old);

                        int ent = 0;
                        for (Map.Entry<Integer, String> e : map.entrySet()) {

                            if (e.getKey() == ClickedRow_old) {
                                ent++;
                                switch (e.getValue()) {
                                    case "green":
                                        tableRow.setBackgroundColor(rs.getColor(R.color.greenPopsoft));
                                        break;
                                    case "orange":
                                        tableRow.setBackgroundColor(rs.getColor(R.color.orangePopsoft));
                                        break;
                                    case "white":
                                        tableRow.setBackgroundColor(Color.WHITE);
                                        break;
                                    default:
                                        tableRow.setBackgroundColor(Color.WHITE);
                                        break;
                                }
                            }
                        }
                        if (ent < 1) {
                            tableRow.setBackgroundColor(Color.WHITE);
                        }
                    }

                } catch (Exception ex) {
                }
            }
        };
        ajustarPantalla();
        atrasadelante("tras");//Ax: ojo! El llamado inutil aquí, a esta función, es Necesario para desbloquear el scrollview y que permita dar clic a las filas!!!
    }//end Oncreate

    /**
     * Obtiene tamaño de la pantalla y ajusta el scroll y la lista para que se ajusten
     */
    private void ajustarPantalla() { //Ax: ajustar el tamaño de los dos scrolls
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;//854
        //int width = displaymetrics.widthPixels;//480
        if(height >= 1100){
            //ViewGroup.LayoutParams params = scroll.getLayoutParams();
            //params.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.5);
            //scroll.setLayoutParams(params);
            scroll.getLayoutParams().height = 700;//Telefonos grandes 230 en xml 400
            scroll.requestLayout();
            list.getLayoutParams().height = 3620;//Telefonos grandes 200 en xml 320
            list.requestLayout();

            return;
        }
        if (height >= 854) {
            scroll.getLayoutParams().height = 280;//Telefonos grandes 230 en xml
            scroll.requestLayout();
            list.getLayoutParams().height = 3240;//Telefonos grandes 200 en xml
            list.requestLayout();
        }
    }
    private boolean nombreArchivo() {

        File archivoNombre = new File(VariablesGlobales.getDirectorioactual() + "/DATOSDEENTRADA/NOMBRE");
        String archivo_cargado;
        txtNombreRuta.setText("Ruta: No esta Cargada");
        String Hora12 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        txtFecha.setText("Fecha: "+Hora12 );
        if (archivoNombre.exists() && archivoNombre.length() > 20) {
            try {

                RandomAccessFile rFile = new RandomAccessFile(archivoNombre, "rw");
                int fileSize = (int) rFile.length();
                byte[] byteArray = new byte[fileSize];
                rFile.readFully(byteArray, 0, fileSize);
                archivo_cargado = new String(byteArray);
                rFile.close();
                txtNombreRuta = (TextView) findViewById(R.id.txtNombreRuta);
                txtFecha = (TextView) findViewById(R.id.txtFecha);
                txtNombreRuta.setText("Ruta: "+archivo_cargado.substring(0, 4)+" - "+archivo_cargado.substring(4, 12));
                return true;
            } catch (Exception ex) {


            }
        } else {

        }
        return false;
    }
    private void realizarConteo() {

        infoRegistroSalida.setArchivo_TablaRegistroSalida(directorio + "/DATOSDESALIDA/REGISTRO.SDA");
        tablaEntradaCliente.setArchivo_TablaEntradaClientes(directorio + "/DATOSDEENTRADA/CLIENTE.TXT");
        infoClienteSalida.setArchivo_TablaClienteSalida(directorio + "/DATOSDESALIDA/CLIENTE.SDA");
        municipio.setArchivo_TablaMunicipios(directorio + "/DATOSDEENTRADA/MUNICIP.TXT");

        infoRegistroSalida.inicializaBloque();
        //  tablaEntradaCliente.inicializaBloque();
        infoClienteSalida.inicializaBloque();

        File registroSalida = new File(infoRegistroSalida.getArchivo_TablaRegistroSalida());
        File entradaCliente = new File(tablaEntradaCliente.getArchivo_TablaEntradaClientes());
        File clienteSalida = new File(infoClienteSalida.getArchivo_TablaClienteSalida());

        if (registroSalida.exists() && entradaCliente.exists() && clienteSalida.exists()) {

            if (!infoRegistroSalida.abrir_TablaRegistroSalida(infoRegistroSalida.getArchivo_TablaRegistroSalida())) {

                return;
            }

            if (!tablaEntradaCliente.abrir_TablaEntradaClientes(tablaEntradaCliente.getArchivo_TablaEntradaClientes())) {

                infoRegistroSalida.Cerrar_TablaRegistroSalida();
                return;
            }

            if (!infoClienteSalida.abrir_TablaClienteSalida(infoClienteSalida.getArchivo_TablaClienteSalida())) {

                infoRegistroSalida.Cerrar_TablaRegistroSalida();
                tablaEntradaCliente.Cerrar_TablaEntradaClientes();
                return;
            }

            if (!municipio.abrir_TablaMunicipios(municipio.getArchivo_TablaMunicipios())) {

                infoRegistroSalida.Cerrar_TablaRegistroSalida();
                tablaEntradaCliente.Cerrar_TablaEntradaClientes();
                infoClienteSalida.Cerrar_TablaClienteSalida();
                return;
            }

            resumenGlobal();
            //  estadisticaCausas();

            agregarCabecera();
            llenarTabla2(VarGlobregistroactual);//Ax nuevo llenado
            ValFechaSistema();
        } else {

            Toast.makeText(getApplicationContext(), "Problemas al Abrir Ruta Cargada", Toast.LENGTH_LONG).show();
        }
    }
    public void ValFechaSistema(){
        try{
            Calendar calendar = Calendar.getInstance();
            int mesSis = calendar.get(Calendar.MONTH) + 1;
            int anoSis = calendar.get(Calendar.YEAR);
            int anoCli = Integer.parseInt(tablaEntradaCliente.gettablaEntradaClientes_anio().trim());
            int mesCli = Integer.parseInt(tablaEntradaCliente.gettablaEntradaClientes_mes().trim());
            int valmes = 0;
            int valano = 0;
            if (mesCli > mesSis){
                valmes = mesCli - mesSis;
            }else{
                valmes = mesSis - mesCli;
            }
            if (anoCli > anoSis){
                valano = anoCli - anoSis;
            }else{
                valano = anoSis - anoCli;
            }
            if (valmes == 1 && valano == 0 && mesCli < mesSis){

            }else if ((valmes >= 1 && valmes != 11  && valano >= 0) || (valmes != 11 && valano >= 1)){
                String fechasis = "Fecha Sistema: " + String.valueOf(mesSis) + "/" + String.valueOf(anoSis);
                String fechacli = "Fecha Cliente: " + String.valueOf(mesCli) + "/" + String.valueOf(anoCli);
                mensajeAlertDialog("La fecha de su terminal esta descuadrada frente al proceso que va a realizar o el proceso a realizar sea uno pasado.\n"+ fechasis +"\n"+ fechacli +"\nDesea Continuar?.", "valfechasistema", "SI", "NO",fechasis,fechacli);
            }

        }catch (Exception e){
            Log.e("TAG ValFechaSistema","Error: " + e);
            utils.Log(logfile, "[ResumenEstadistico]ValFechaSistema();" + e);
        }

    }
    private void mensajeAlertDialog(String msg, String metodo, String yes, String nou , String fechasis, String fechaCli) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(msg);
            builder.setPositiveButton(yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    switch (metodo) {
                        case "valfechasistema":
                            utils.Log(logfile, "[ResumenEstadistico]ValFechaSistema(); Decidio proseguir sabiendo que la fecha esta desactualizada o el proceso es uno pasado \n " + fechasis + "," + fechaCli);
                            break;
                    }
                }
            });
            builder.setNegativeButton(nou, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    switch (metodo) {
                        case "valfechasistema":
                            btnCerrar.setEnabled(false);
                         //   btnCerrar2.setEnabled(false);
                            break;

                    }
                }
            });

            builder.create().show();
        } catch (Exception ex) {
        }
    }
    private void salir() {
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_OK, resultIntent);
        this.finish();
    }

    private int validarEstadoRegistro(int registroactual) {

        infoRegistroSalida.lectura_TablaRegistroSalidaII(registroactual);

        if (infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA().trim().length() != 0 && !infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA().trim().equals("0")
                && infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim().equals("")) {
            return (1);
        }

        if (!infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim().equals("")) {
            return (2);
        }
        Log.e("INFO","validarEstadoRegistro| Cuenta: " + infoRegistroSalida.gettablaRegistroSalida_CUENTA() + " | posicion: " + registroactual +" | estado: 0");
        return 0;
    }
    private void mensajeT(String msg, int dur) { //Ax: 1 segundo: 1000

        final Toast toast = Toast.makeText(ResumenEstadistico.this, msg, dur);
        //toast.setGravity(Gravity.TOP, 10, 170);
        toast.setDuration(dur);
        toast.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, dur);
    }
    private void resumenGlobal() {

        int C_1 = 0;
        int C_2 = 0;
        int C_3 = 0;
        int C_4 = 0;
        int C_7 = 0;
        int C_11 = 0;
        int C_12 = 0;
        int C_14 = 0;
        int C_15 = 0;
        int C_16 = 0;
        int C_17 = 0;
        int C_19 = 0;
        int C_20 = 0;
        int C_26 = 0;
        int C_27 = 0;
        int C_41 = 0;
        int C_42 = 0;
        int C_43 = 0;
        int C_44 = 0;
        int C_45 = 0;
        int C_46 = 0;
        int C_47 = 0;
        int C_48 = 0;
        int C_49 = 0;
        int C_50 = 0;
        int C_51 = 0;
        int C_52 = 0;
        int C_53 = 0;
        int C_54 = 0;
        int C_60 = 0;
        int C_62 = 0;
        int C_69 = 0;
        int C_99 = 0;
        int C_otras = 0;
        //-----------------------------------
        String horaini = "000000";
        String horafin = "000000";
        String RegistrosEnviados;
        int resultado;
        int ultimaCuentaNoLeida = 0;
        VariablesGlobales.totallecturas = 0;
        VariablesGlobales.totalcausasnolectura = 0;
        VariablesGlobales.totalnuevos = 0;
        VariablesGlobales.totalinformes = 0;
        VariablesGlobales.totalregistrosleidos = 0;
        VariablesGlobales.totalprediosleidos = 0;
        VariablesGlobales.totalpredioscomentarios = 0;
        VariablesGlobales.totalprediosliquidados = 0;
        VariablesGlobales.totalprediosimpresos = 0;
        VariablesGlobales.totalprediosnofacturados = 0;
        VariablesGlobales.registroactual = 1;
        // infoRegistroSalida.lectura_TablaRegistroSalida(VariablesGlobales.registroactual); //
        int clientesConLectura = 0;
        ultimaRutaSeleccionada = "";
        String miultimomedidor = "";
        Log.e("error",VariablesGlobales.registroactual+" +++entra aqui++++++++++++++++++++++"+infoRegistroSalida.getTotal_TablaRegistroSalida());
        while (VariablesGlobales.registroactual <= infoRegistroSalida.getTotal_TablaRegistroSalida()) {

            Log.e("error","entra aqui++++++++++++++++++++++ registroactual| " +VariablesGlobales.registroactual);
            resultado = validarEstadoRegistro(VariablesGlobales.registroactual);
            int causa=0;
            if (!infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim().equals(""))
            {
                causa = Integer.parseInt(infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim());
            }
            if (resultado == 2) {
                //contar las causas como dicen
                switch (String.format("%02d", causa)) {
                    case "01":
                        C_1++;
                        break;
                    case "02":
                        C_2++;
                        break;
                    case "03":
                        C_3++;
                        break;
                    case "04":
                        C_4++;
                        break;
                    case "08":
                        C_7++;
                        break;
                    case "09":
                        C_11++;
                        break;
                    case "10":
                        C_12++;
                        break;
                    case "14":
                        C_14++;
                        break;
                    case "15":
                        C_15++;
                        break;
                    case "16":
                        C_16++;
                        break;
                    case "17":
                        C_17++;
                        break;
                    case "19":
                        C_19++;
                        break;
                    case "20":
                        C_20++;
                        break;
                    case "21":
                        C_26++;
                        break;
                    case "22":
                        C_27++;
                        break;
                    case "25":
                        C_41++;
                        break;
                    case "31":
                        C_42++;
                        break;
                    case "37":
                        C_43++;
                        break;
                    case "94":
                        C_44++;
                        break;
                    case "45":
                        C_45++;
                        break;
                    case "46":
                        C_46++;
                        break;
                    case "47":
                        C_47++;
                        break;
                    case "48":
                        C_48++;
                        break;
                    case "49":
                        C_49++;
                        break;
                    case "50":
                        C_50++;
                        break;
                    case "51":
                        C_51++;
                        break;
                    case "52":
                        C_52++;
                        break;
                    case "53":
                        C_53++;
                        break;
                    case "54":
                        C_54++;
                        break;
                    case "60":
                        C_60++;
                        break;
                    case "62":
                        C_62++;
                        break;
                    case "69":
                        C_69++;
                        break;
                    case "99":
                        C_99++;
                        break;
                    default:
                        C_otras++;
                        break;
                }
            }

            Log.e("INFO","APUNTADOR CLIENTE: " + infoRegistroSalida.gettablaRegistroSalida_CLIENTE() + " | CUENTA REGISTRADOR: " + infoRegistroSalida.gettablaRegistroSalida_CUENTA());
            // tablaEntradaCliente.lectura_TablaEntradaClientesII(Integer.parseInt(infoRegistroSalida.gettablaRegistroSalida_CLIENTE()));
            infoClienteSalida.lectura_TablaClienteSalidaII(Integer.parseInt(infoRegistroSalida.gettablaRegistroSalida_CLIENTE()));
            Log.e("INFO","CLIENTE INDFACT: " + infoClienteSalida.gettablaClienteSalida_INDFACTURACION() + " CUENTA CLIENTE| " + infoClienteSalida.gettablaClienteSalida_CUENTA());

            // EjecutarProcesoDeFoto TODO: pendiente migracion, preguntar si es     //Ax: TODO: ValidaRequiereFoto

//            if (!ultimaRutaSeleccionada.equals(tablaEntradaCliente.gettablaEntradaClientes_MUNICIPIO() + tablaEntradaCliente.gettablaEntradaClientes_RUTA().substring(0, 3))) {
//                ultimaRutaSeleccionada = tablaEntradaCliente.gettablaEntradaClientes_MUNICIPIO() + tablaEntradaCliente.gettablaEntradaClientes_RUTA().substring(0, 3);
//            }

            horaini = String.format("%1$6s", infoRegistroSalida.gettablaRegistroSalida_HORALECTURA().trim()).replace(" ", "0");

            if (resultado == 1) {

                VariablesGlobales.totalregistrosleidos++;
                VariablesGlobales.totallecturas++;
            } else if (resultado == 2) {

                VariablesGlobales.totalregistrosleidos++;
                VariablesGlobales.totalcausasnolectura++;
                VariablesGlobales.totallecturas++;
            } else {
                if (ultimaCuentaNoLeida == 0)
                    ultimaCuentaNoLeida = Integer.parseInt(infoRegistroSalida.gettablaRegistroSalida_CLIENTE());
                Log.e("INFO","ultimaCuentaNoLeida| " +ultimaCuentaNoLeida + " |resultado: " + resultado);
            }

            VariablesGlobales.registroactual++;

            if (!infoRegistroSalida.gettablaRegistroSalida_COMENTARIO1().trim().equals(""))
                VariablesGlobales.totalpredioscomentarios++;

            if (!infoRegistroSalida.gettablaRegistroSalida_INFORME().trim().equals(""))
                VariablesGlobales.totalinformes++;

            // contar nuevos item a partir de clientes salida
            /*if (!miultimomedidor.equals(infoRegistroSalida.gettablaRegistroSalida_CUENTA())) {
                continue;
            }

            miultimomedidor = infoRegistroSalida.gettablaRegistroSalida_CUENTA();*/

            switch (infoClienteSalida.gettablaClienteSalida_INDFACTURACION()) {
                case "F":
                    VariablesGlobales.totalprediosleidos++;
                    VariablesGlobales.totalprediosimpresos++;
                    break;

                case "K":
                    VariablesGlobales.totalprediosleidos++;
                    VariablesGlobales.totalprediosliquidados++;
                    break;

                case "L":
                    if(resultado == 0){
                        VariablesGlobales.totalprediosnofacturados++;
                    }else{
                        VariablesGlobales.totalprediosleidos++;
                        clientesConLectura++;
                    }
                    break;

                case " ":
                    VariablesGlobales.totalprediosnofacturados++;
                    break;
            }
        }

        Log.e("error","cli 1 "+infoRegistroSalida.gettablaRegistroSalida_CLIENTE());
        tablaEntradaCliente.lectura_TablaEntradaClientesII(Integer.parseInt(infoRegistroSalida.gettablaRegistroSalida_CLIENTE()));
        infoClienteSalida.lectura_TablaClienteSalidaII(Integer.parseInt(infoRegistroSalida.gettablaRegistroSalida_CLIENTE()));

        if (!ultimaRutaSeleccionada.equals(tablaEntradaCliente.gettablaEntradaClientes_Municipio() + tablaEntradaCliente.gettablaEntradaClientes_Ruta().substring(0, 3))) {
            ultimaRutaSeleccionada = tablaEntradaCliente.gettablaEntradaClientes_Municipio() + tablaEntradaCliente.gettablaEntradaClientes_Ruta().substring(0, 3);
        }
        Log.e("INFO","gettablaEntradaClientes_CLIENTE: " + tablaEntradaCliente.gettablaEntradaClientes_Cliente());
        Log.e("INFO","gettablaEntradaClientes_CLIENTE: " + tablaEntradaCliente.gettablaEntradaClientes_Municipio());

        municipio.buscarbinario_TablaMunicipios(tablaEntradaCliente.gettablaEntradaClientes_Municipio());
        item.add("Municipio            : " + tablaEntradaCliente.gettablaEntradaClientes_Municipio() + " " + municipio.gettablaMunicipios_DESCRIPCION().trim());//TODO: no existe el campo descripcion del municipio (TablaEntradaCliente.ECLIENTEDESMUNICIPIO)

        item.add("Total Cliente       : " + tablaEntradaCliente.getTotal_TablaEntradaClientes());

        txtTotal.setText("" +tablaEntradaCliente.getTotal_TablaEntradaClientes());

        if (VariablesGlobales.totalprediosnofacturados > 0) {
            item.add("Clientes por leer    : " + VariablesGlobales.totalprediosnofacturados);
            txtPendientes.setText("" + (tablaEntradaCliente.getTotal_TablaEntradaClientes()-VariablesGlobales.totalprediosleidos) );
        } else {
            item.add("Clientes por leer    : " + "0");
            txtPendientes.setText("" + "0");
        }
        File filebck = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/BKENVIOSGPRS.SDA");
        if (filebck.exists()) {

            misenvios.archivo_EnvioGPS = VariablesGlobales.directorioactual + "/DATOSDESALIDA/BKENVIOSGPRS.SDA";

            if (misenvios.abrir_EnvioGPS(misenvios.archivo_EnvioGPS)) {
                RegistrosEnviados = misenvios.total_EnvioGPS + "";
                misenvios.Cerrar_EnvioGPS();

                item.add("Registros Enviados al Server: " + RegistrosEnviados);
            }
        }
        txtLeidos.setText("" + VariablesGlobales.getTotalregistrosleidos());
        item.add("Total Registros     : " + infoRegistroSalida.getTotal_TablaRegistroSalida());//Totalsregist.ToString());
        item.add("Registradores Leidos: " + VariablesGlobales.getTotalregistrosleidos());//.Totalregistrosleidos.ToString());

        item.add("== Desglose Facturacion ==");
        item.add("Con Factura Impresa  : " + VariablesGlobales.totalprediosimpresos);
        item.add("Error solo Liquidado : " + VariablesGlobales.totalprediosliquidados);

        item.add("Solo tomo Lectura    : " + clientesConLectura);

        item.add("Clientes sin Facturar: " + VariablesGlobales.totalprediosnofacturados);

        //item.add("Registros Sin Leer  : " + Convert.ToString(Convert.ToInt32(InfoRegistroSalidac.Totalsregist) - VariablesGlobales.totalregistrosleidos));
        item.add("== Desglose Informacion Especial ==");
        item.add("Con novedad sin lect.: " + VariablesGlobales.totalcausasnolectura);
        item.add("Con Novedad y lectura: " + VariablesGlobales.totalpredioscomentarios);
        item.add("Informes Reportados  : " + VariablesGlobales.totalinformes);
        item.add("Hora Inicial         : " + horaini);
        item.add("Hora Final           : " + horafin);
        item.add("== Desglose Anomalias ==");

        //String nombreDeArchivo = VariablesGlobales.directorioactual + "/DatosDeEntrada/Resumen.txt";

        if (VariablesGlobales.totalprediosnofacturados == 0) {

            Toast.makeText(getApplicationContext(), "Proceso de Factucacion concluido debes descargar y cargar datos nuevos", Toast.LENGTH_LONG).show();
            VariablesGlobales.registroactual = 0;
        } else {
            VariablesGlobales.registroactual = ultimaCuentaNoLeida;

        }
        list.setAdapter(item);

        //-----------------------------------------------------
        String CADENA = "Conciliacion Causas....";
        item.add(CADENA);
        //modelo anterior CADENA += "\nCausas A: " + A;

        if (C_1 > 0)
            item.add("Causas 1: " + C_1);
        if (C_2 > 0)
            item.add("Causas 2: " + C_2);
        if (C_3 > 0)
            item.add("Causas 3: " + C_3);
        if (C_4 > 0)
            item.add("Causas 4: " + C_4);
        if (C_7 > 0)
            item.add("Causas 7: " + C_7);
        if (C_11 > 0)
            item.add("Causas11: " + C_11);
        if (C_12 > 0)
            item.add("Causas12: " + C_12);
        if (C_14 > 0)
            item.add("Causas14: " + C_14);
        if (C_15 > 0)
            item.add("Causas15: " + C_15);
        if (C_16 > 0)
            item.add("Causas16: " + C_16);
        if (C_17 > 0)
            item.add("Causas17: " + C_17);
        if (C_19 > 0)
            item.add("Causas19: " + C_19);
        if (C_20 > 0)
            item.add("Causas20: " + C_20);
        if (C_26 > 0)
            item.add("Causas26: " + C_26);
        if (C_27 > 0)
            item.add("Causas27: " + C_27);
        if (C_41 > 0)
            item.add("Causas41: " + C_41);
        if (C_42 > 0)
            item.add("Causas42: " + C_42);
        if (C_43 > 0)
            item.add("Causas43: " + C_43);
        ///NO SE USAN
        if (C_45 > 0)
            item.add("Causas44: " + C_45);
        if (C_46 > 0)
            item.add("Causas45: " + C_46);
        if (C_47 > 0)
            item.add("Causas47: " + C_47);
        if (C_48 > 0)
            item.add("Causas48: " + C_48);
        if (C_49 > 0)
            item.add("Causas49: " + C_49);
        if (C_50 > 0)
            item.add("Causas50: " + C_50);
        if (C_51 > 0)
            item.add("Causas51: " + C_51);
        //DATOS QUE NO ESTAN CODIFICADO
        if (C_52 > 0)
            item.add("Causas52: " + C_52);
        if (C_53 > 0)
            item.add("Causas 53: " + C_53);
        if (C_54 > 0)
            item.add("Causas 54: " + C_54);
        if (C_60 > 0)
            item.add("Causas 60: " + C_60);
        if (C_62 > 0)
            item.add("Causas 62: " + C_62);
        if (C_69 > 0)
            item.add("Causas69: " + C_69);
        if (C_99 > 0)
            item.add("Causas99: " + C_99);
        if (C_otras > 0)
            item.add("Causas OTRAS: " + C_otras);
        //-----------------------------------------------------

        infoRegistroSalida.terminaBloque();
        //  tablaEntradaCliente.terminaBloque();
        infoClienteSalida.terminaBloque();
    }

    //Ax: cambia de numero de registro (botones atras,adelante,primero,ultimo) para luego llenar la TableLayout
    private void atrasadelante(String va) {

        switch (va) {
            case "primero":
                VarGlobregistroactual = 1;
                llenarTabla2(VarGlobregistroactual);
                break;
            case "delante": //Adelante
                VarGlobregistroactual += 20;
                if ((VarGlobregistroactual) > infoRegistroSalida.getTotal_TablaRegistroSalida()) {//este if lo puso victor
                    VarGlobregistroactual = infoRegistroSalida.getTotal_TablaRegistroSalida() - 19;
                }
                llenarTabla2(VarGlobregistroactual);
                break;
            case "tras"://Atras
                VarGlobregistroactual = (VarGlobregistroactual - 20);
                if (VarGlobregistroactual < 1) {
                    VarGlobregistroactual = 1;
                }
                llenarTabla2(VarGlobregistroactual);
                break;
            case "ultimo":
                VarGlobregistroactual = infoRegistroSalida.getTotal_TablaRegistroSalida() - 19;
                llenarTabla2(VarGlobregistroactual);
                break;
        }
    }

    //Ax. llena la TableLayout en determinada posicion de a 20
    private void llenarTabla(int pos) {

        tabla.removeAllViews();
        tabla.refreshDrawableState();

        for (int cont = 1; cont <= 20; cont++) {

            if (pos > infoRegistroSalida.getTotal_TablaRegistroSalida()) {
                // pos = infoRegistroSalida.getTotal_TablaRegistroSalida();
                break;
            }

            infoRegistroSalida.lectura_TablaRegistroSalida(pos);
            tablaEntradaCliente.lectura_TablaEntradaClientesII(Integer.parseInt(infoRegistroSalida.gettablaRegistroSalida_CLIENTE()));
            infoClienteSalida.lectura_TablaClienteSalida(Integer.parseInt(infoRegistroSalida.gettablaRegistroSalida_CLIENTE()));
            llenarListaResumen();
            pos++;
        }

        VarGlobregistroactual = pos;
    }

    private void llenarTabla2(int pos) {

        tabla.removeAllViews();
        tabla.refreshDrawableState();
        int contlineas = 1;
        VarGlobregistroactual = pos;
        if (Ultimo == 0) {
            for (int cont = pos; cont < infoRegistroSalida.getTotal_TablaRegistroSalida(); cont++) {

                //if (contlineas > 20 && contlineas < 22 && Ultimo == 0) {
                if (contlineas > 20 ) {
                    break;
                }

                infoRegistroSalida.lectura_TablaRegistroSalida(cont);
                tablaEntradaCliente.lectura_TablaEntradaClientesII(Integer.parseInt(infoRegistroSalida.gettablaRegistroSalida_CLIENTE()));
                infoClienteSalida.lectura_TablaClienteSalida(Integer.parseInt(infoRegistroSalida.gettablaRegistroSalida_CLIENTE()));

                if (CheckRadio == 1) {// Todas
                    UltimoRegistro++;
                    contlineas++;

                    llenarListaResumen2(cont);

                } else if (CheckRadio == 2 && !infoRegistroSalida.gettablaRegistroSalida_LEIDO().trim().equals("")) {// Procesadas
                    UltimoRegistro++;
                    contlineas++;

                    llenarListaResumen2(cont);

                } else if (CheckRadio == 3 && infoRegistroSalida.gettablaRegistroSalida_LEIDO().trim().equals("")) {// No Procesadas
                    UltimoRegistro++;
                    contlineas++;

                    llenarListaResumen2(cont);

                }
                pos = cont;
                if (cont >= infoRegistroSalida.getTotal_TablaRegistroSalida()) {

                    break;
                }
            }

        }else {
            for (int x = pos;x <= infoRegistroSalida.getTotal_TablaRegistroSalida(); x++){
                infoRegistroSalida.lectura_TablaRegistroSalida(x);

                tablaEntradaCliente.lectura_TablaEntradaClientesII(Integer.parseInt(infoRegistroSalida.gettablaRegistroSalida_CLIENTE()));
                infoClienteSalida.lectura_TablaClienteSalida(Integer.parseInt(infoRegistroSalida.gettablaRegistroSalida_CLIENTE()));

                llenarListaResumen2(x);
            }
        }

    }


    public void llenarListaResumen2(int pos) {

        String color = "white";
        fila = new TableRow(this);
        fila.setLayoutParams(layoutFila);
        fila.setClickable(true);
        fila.setOnClickListener(TablaListener);
        // =========================
        // 🔥 OBTENER VALORES
        // =========================
        String lecturaStr = infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA().trim();
        String leido = infoRegistroSalida.gettablaRegistroSalida_LEIDO().trim();
        String causa = infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim();

        int lectom = 0;
        try {
            lectom = Integer.parseInt(infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA().trim());
        } catch (Exception ex) {
        }


        // =========================
        // 🎯 CLASIFICACIÓN VISUAL
        // =========================
        int lectura = 0;
        try {
            lectura = Integer.parseInt(lecturaStr);
        } catch (Exception ignored) {
        }
        String icono = "⚪"; // default
        int bgColor = Color.GRAY;
        if (leido.equals("5")) {
            //if (VariablesGlobales.getTipoDeRuta().substring(0,1).equals("L")) {
                icono = "🚫"; // no lectura
                bgColor = rs.getColor(R.color.orangePopsoft);
                color = "orange";
           // }
           /* else {
                icono = "✅"; // normal
                bgColor = rs.getColor(R.color.greenPop);
                color = "green";
            }*/
        }
        else
        {
            if ((!lecturaStr.equals("") && !leido.equals(""))) {

                if (leido.equals("1")) {
                    icono = "🔻"; // negativa
                    bgColor = Color.parseColor("#F1574B"); // azul claro
                    color = "blue";
                } else if (leido.equals("9") || leido.equals("2") ) {
                    icono = "⚠️";      // Bajo consumo / sospechosa
                    bgColor = Color.parseColor("#FFFF00");
                    color = "yellow";

                } else if (leido.equals("7")) {
                    icono = "🔺"; // alta
                    bgColor = Color.parseColor("#FFEBEE"); // rojo suave
                    color = "red";
                } else if (leido.equals("8")) {
                    icono = "🔄";      // Giro de medidor
                    bgColor = Color.parseColor("#B3E5FC"); // Azul claro
                    color = "cyan";

                } else if (leido.equals("IGUAL")) {

                    icono = "\uD83D\uDFF0";      // Igual a la lectura anterior
                    bgColor = Color.parseColor("#E0E0E0"); // Gris claro
                    color = "gray";
                } else {
                    icono = "✅"; // normal
                    bgColor = rs.getColor(R.color.greenPopsoft);
                    color = "green";
                }

            } else
            {
                if (!leido.trim().equals("")) {
                    icono = "📘"; // procesado sin lectura
                    bgColor = rs.getColor(R.color.bluePop);
                    color = "blue";
                }
            }
        }
        txtRuta = new TextView(this);
        txtNroMedidor = new TextView(this);
        txtDireccion = new TextView(this);
        txtNombre = new TextView(this);
        txtLectura = new TextView(this);
        txtAnomalia = new TextView(this);
        txtComentario = new TextView(this);
        txtInforme = new TextView(this);
        txtId = new TextView(this);

        txtDireccion.setText(icono+" |"+tablaEntradaCliente.gettablaEntradaClientes_Direccion().substring(0, 25));
        // txtDireccion.setText(infoRegistroSalida.gettablaRegistroSalida_DIRECCION().substring(0, 30));
        txtDireccion.setGravity(Gravity.LEFT);
        txtDireccion.setTextAppearance(this, R.style.etiqueta);
        txtDireccion.setBackgroundResource(R.drawable.tabla_celda);
        txtDireccion.setLayoutParams(layoutDireccion);

        txtNroMedidor.setText(infoRegistroSalida.gettablaRegistroSalida_NROCONTADOR().trim().replaceFirst("^0*", ""));//rx
        txtNroMedidor.setGravity(Gravity.LEFT);
        txtNroMedidor.setTextAppearance(this, R.style.etiqueta);
        txtNroMedidor.setBackgroundResource(R.drawable.tabla_celda);
        txtNroMedidor.setLayoutParams(layoutNroMedidor);

        txtRuta.setText(tablaEntradaCliente.gettablaEntradaClientes_Ruta());
        txtRuta.setGravity(Gravity.LEFT);
        txtRuta.setTextAppearance(this, R.style.etiqueta);
        txtRuta.setBackgroundResource(R.drawable.tabla_celda);
        txtRuta.setLayoutParams(layoutRuta);
        String NombreCliente =tablaEntradaCliente.gettablaEntradaClientes_Nombre().trim();

        if (NombreCliente.length() > 20) {
            NombreCliente = NombreCliente.substring(0, 20);
        }

        txtNombre.setText(NombreCliente);
        txtNombre.setGravity(Gravity.LEFT);
        txtNombre.setTextAppearance(this, R.style.etiqueta);
        txtNombre.setBackgroundResource(R.drawable.tabla_celda);
        txtNombre.setLayoutParams(layoutNombre);

        txtLectura.setText(infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA());
        txtLectura.setGravity(Gravity.CENTER_HORIZONTAL);
        txtLectura.setTextAppearance(this, R.style.etiqueta);
        txtLectura.setBackgroundResource(R.drawable.tabla_celda);
        txtLectura.setBackgroundColor(bgColor);
        txtLectura.setLayoutParams(layoutLectura);

        txtAnomalia.setText(infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA());
        txtAnomalia.setGravity(Gravity.CENTER_HORIZONTAL);
        txtAnomalia.setTextAppearance(this, R.style.etiqueta);
        txtAnomalia.setBackgroundResource(R.drawable.tabla_celda);
        txtAnomalia.setBackgroundColor(bgColor);
        txtAnomalia.setLayoutParams(layoutAnomalia);

        txtComentario.setText(infoRegistroSalida.gettablaRegistroSalida_COMENTARIO1());
        txtComentario.setGravity(Gravity.CENTER_HORIZONTAL);
        txtComentario.setTextAppearance(this, R.style.etiqueta);
        txtComentario.setBackgroundResource(R.drawable.tabla_celda);
        txtComentario.setLayoutParams(layoutComentario);

        txtInforme.setText(infoRegistroSalida.gettablaRegistroSalida_INFORME().substring(0, 30));



        txtInforme.setGravity(Gravity.LEFT);
        txtInforme.setTextAppearance(this, R.style.etiqueta);
        txtInforme.setBackgroundResource(R.drawable.tabla_celda);
        txtInforme.setLayoutParams(layoutInforme);

        int id = pos;//Integer.parseInt(infoRegistroSalida.gettablaRegistroSalida_NROCONTADOR().trim()); se desborda

        txtId.setText(id + "");//Ax: antes txtId.setText(tablaEntradaCliente.gettablaEntradaClientes_CONSECUTIVO());
        txtId.setGravity(Gravity.CENTER_HORIZONTAL);
        txtId.setTextAppearance(this, R.style.etiqueta);
        txtId.setBackgroundResource(R.drawable.tabla_celda);
        txtId.setLayoutParams(layoutId);

        fila.setId(id); //Ax: id para capturar en un Listener de la tabla el numero para si dan clic ir a liquidacion
        map.put(id, color);//Ax: Guardo el id y color para jugar con estos colores en el listener de rows ya que en ejecucion no puedo capturar getbackgrondcolor

        fila.addView(txtDireccion);
        fila.addView(txtNroMedidor);
        fila.addView(txtRuta);
        fila.addView(txtNombre);
        fila.addView(txtLectura);
        fila.addView(txtAnomalia);
        fila.addView(txtComentario);
        fila.addView(txtInforme);
        fila.addView(txtId);
        tabla.addView(fila);
    }

    public void llenarListaResumen() {

        String color = "white";

        fila = new TableRow(this);
        fila.setLayoutParams(layoutFila);
        fila.setClickable(true);
        fila.setOnClickListener(TablaListener);

        int lectom = 0;
        try {
            lectom = Integer.parseInt(infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA().trim());
        } catch (Exception c) {
        }

        if (!infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA().trim().equals("") && lectom > 0 && !infoClienteSalida.gettablaClienteSalida_INDFACTURACION().trim().equals("")) {
            fila.setBackgroundColor(rs.getColor(R.color.greenPopsoft));
            color = "green";
        } else if (!infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA().trim().equals("") && lectom > 0 && infoClienteSalida.gettablaClienteSalida_INDFACTURACION().trim().equals("")){
            if (!infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim().equals("") && infoClienteSalida.gettablaClienteSalida_INDFACTURACION().trim().equals("")) {
                fila.setBackgroundColor(rs.getColor(R.color.redPopHard));
                color = "red";
            }
        }else {
            if (!infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim().equals("") && !infoClienteSalida.gettablaClienteSalida_INDFACTURACION().trim().equals("")) {
                fila.setBackgroundColor(rs.getColor(R.color.orangePopsoft));
                color = "orange";
            }
        }

        txtRuta = new TextView(this);
        txtNroMedidor = new TextView(this);
        txtDireccion = new TextView(this);
        txtNombre = new TextView(this);
        txtLectura = new TextView(this);
        txtAnomalia = new TextView(this);
        txtComentario = new TextView(this);
        txtInforme = new TextView(this);
        txtId = new TextView(this);

        txtDireccion.setText(tablaEntradaCliente.gettablaEntradaClientes_Direccion().substring(0, 25));
        txtDireccion.setGravity(Gravity.CENTER_HORIZONTAL);
        txtDireccion.setTextAppearance(this, R.style.etiqueta);
        txtDireccion.setBackgroundResource(R.drawable.tabla_celda);
        txtDireccion.setLayoutParams(layoutDireccion);

        txtNroMedidor.setText(infoRegistroSalida.gettablaRegistroSalida_NROCONTADOR().trim());
        txtNroMedidor.setGravity(Gravity.CENTER_HORIZONTAL);
        txtNroMedidor.setTextAppearance(this, R.style.etiqueta);
        txtNroMedidor.setBackgroundResource(R.drawable.tabla_celda);
        txtNroMedidor.setLayoutParams(layoutNroMedidor);

        txtRuta.setText(tablaEntradaCliente.gettablaEntradaClientes_Ruta());
        txtRuta.setGravity(Gravity.CENTER_HORIZONTAL);
        txtRuta.setTextAppearance(this, R.style.etiqueta);
        txtRuta.setBackgroundResource(R.drawable.tabla_celda);
        txtRuta.setLayoutParams(layoutRuta);

        txtNombre.setText(tablaEntradaCliente.gettablaEntradaClientes_Nombre().substring(0, 20));
        txtNombre.setGravity(Gravity.CENTER_HORIZONTAL);
        txtNombre.setTextAppearance(this, R.style.etiqueta);
        txtNombre.setBackgroundResource(R.drawable.tabla_celda);
        txtNombre.setLayoutParams(layoutNombre);

        txtLectura.setText(infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA());
        txtLectura.setGravity(Gravity.CENTER_HORIZONTAL);
        txtLectura.setTextAppearance(this, R.style.etiqueta);
        txtLectura.setBackgroundResource(R.drawable.tabla_celda);
        txtLectura.setLayoutParams(layoutLectura);

        txtAnomalia.setText(infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA());
        txtAnomalia.setGravity(Gravity.CENTER_HORIZONTAL);
        txtAnomalia.setTextAppearance(this, R.style.etiqueta);
        txtAnomalia.setBackgroundResource(R.drawable.tabla_celda);
        txtAnomalia.setLayoutParams(layoutAnomalia);

        txtComentario.setText(infoRegistroSalida.gettablaRegistroSalida_COMENTARIO1());
        txtComentario.setGravity(Gravity.CENTER_HORIZONTAL);
        txtComentario.setTextAppearance(this, R.style.etiqueta);
        txtComentario.setBackgroundResource(R.drawable.tabla_celda);
        txtComentario.setLayoutParams(layoutComentario);

        txtInforme.setText(infoRegistroSalida.gettablaRegistroSalida_INFORME().substring(0, 30));
        txtInforme.setGravity(Gravity.CENTER_HORIZONTAL);
        txtInforme.setTextAppearance(this, R.style.etiqueta);
        txtInforme.setBackgroundResource(R.drawable.tabla_celda);
        txtInforme.setLayoutParams(layoutInforme);

        int id = Integer.parseInt(infoRegistroSalida.gettablaRegistroSalida_CLIENTE().trim());

        txtId.setText(id + "");//Ax: antes txtId.setText(tablaEntradaCliente.gettablaEntradaClientes_CONSECUTIVO());
        txtId.setGravity(Gravity.CENTER_HORIZONTAL);
        txtId.setTextAppearance(this, R.style.etiqueta);
        txtId.setBackgroundResource(R.drawable.tabla_celda);
        txtId.setLayoutParams(layoutId);

        fila.setId(id); //Ax: id para capturar en un Listener de la tabla el numero para si dan clic ir a liquidacion
        map.put(id, color);//Ax: Guardo el id y color para jugar con estos colores en el listener de rows ya que en ejecucion no puedo capturar getbackgrondcolor

        fila.addView(txtDireccion);
        fila.addView(txtNroMedidor);
        fila.addView(txtRuta);
        fila.addView(txtNombre);
        fila.addView(txtLectura);
        fila.addView(txtAnomalia);
        fila.addView(txtComentario);
        fila.addView(txtInforme);
        fila.addView(txtId);
        tabla.addView(fila);
    }

    public void agregarCabecera() {

        TableRow fila;
        TextView txtRuta;
        TextView txtNroMedidor;
        TextView txtDireccion;
        TextView txtNombre;
        TextView txtLectura;
        TextView txtAnomalia;
        TextView txtComentario;
        TextView txtInforme;
        TextView txtId;

        fila = new TableRow(this);
        fila.setLayoutParams(layoutFila);

        txtRuta = new TextView(this);
        txtNroMedidor = new TextView(this);
        txtDireccion = new TextView(this);
        txtNombre = new TextView(this);
        txtLectura = new TextView(this);
        txtAnomalia = new TextView(this);
        txtComentario = new TextView(this);
        txtInforme = new TextView(this);
        txtId = new TextView(this);

        txtRuta.setText(rs.getString(R.string.ruta));
        txtRuta.setGravity(Gravity.CENTER_HORIZONTAL);
        txtRuta.setTextAppearance(this, R.style.etiqueta);
        txtRuta.setBackgroundResource(R.drawable.tabla_celda_cabecera);
        txtRuta.setLayoutParams(layoutRuta);

        txtNroMedidor.setText(rs.getString(R.string.nroMedidor));
        txtNroMedidor.setGravity(Gravity.CENTER_HORIZONTAL);
        txtNroMedidor.setTextAppearance(this, R.style.etiqueta);
        txtNroMedidor.setBackgroundResource(R.drawable.tabla_celda_cabecera);
        txtNroMedidor.setLayoutParams(layoutNroMedidor);

        txtDireccion.setText(rs.getString(R.string.direccion));
        txtDireccion.setGravity(Gravity.CENTER_HORIZONTAL);
        txtDireccion.setTextAppearance(this, R.style.etiqueta);
        txtDireccion.setBackgroundResource(R.drawable.tabla_celda_cabecera);
        txtDireccion.setLayoutParams(layoutDireccion);

        txtNombre.setText(rs.getString(R.string.nombre));
        txtNombre.setGravity(Gravity.CENTER_HORIZONTAL);
        txtNombre.setTextAppearance(this, R.style.etiqueta);
        txtNombre.setBackgroundResource(R.drawable.tabla_celda_cabecera);
        txtNombre.setLayoutParams(layoutNombre);

        txtLectura.setText(rs.getString(R.string.lectura));
        txtLectura.setGravity(Gravity.CENTER_HORIZONTAL);
        txtLectura.setTextAppearance(this, R.style.etiqueta);
        txtLectura.setBackgroundResource(R.drawable.tabla_celda_cabecera);
        txtLectura.setLayoutParams(layoutLectura);

        txtAnomalia.setText(rs.getString(R.string.anomalia));
        txtAnomalia.setGravity(Gravity.CENTER_HORIZONTAL);
        txtAnomalia.setTextAppearance(this, R.style.etiqueta);
        txtAnomalia.setBackgroundResource(R.drawable.tabla_celda_cabecera);
        txtAnomalia.setLayoutParams(layoutAnomalia);

        txtComentario.setText(rs.getString(R.string.comentario));
        txtComentario.setGravity(Gravity.CENTER_HORIZONTAL);
        txtComentario.setTextAppearance(this, R.style.etiqueta);
        txtComentario.setBackgroundResource(R.drawable.tabla_celda_cabecera);
        txtComentario.setLayoutParams(layoutComentario);

        txtInforme.setText(rs.getString(R.string.informe));
        txtInforme.setGravity(Gravity.CENTER_HORIZONTAL);
        txtInforme.setTextAppearance(this, R.style.etiqueta);
        txtInforme.setBackgroundResource(R.drawable.tabla_celda_cabecera);
        txtInforme.setLayoutParams(layoutInforme);

        txtId.setText(rs.getString(R.string.id));
        txtId.setGravity(Gravity.CENTER_HORIZONTAL);
        txtId.setTextAppearance(this, R.style.etiqueta);
        txtId.setBackgroundResource(R.drawable.tabla_celda_cabecera);
        txtId.setLayoutParams(layoutId);

        fila.addView(txtDireccion);
        fila.addView(txtNroMedidor);
        fila.addView(txtRuta);
        fila.addView(txtNombre);
        fila.addView(txtLectura);
        fila.addView(txtAnomalia);
        fila.addView(txtComentario);
        fila.addView(txtInforme);
        fila.addView(txtId);
        cabecera.addView(fila);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.resumen_estadistico, menu);
        return true;
    }

    private void ingresoMenuPrincipal() {//Ax. envia datos a la actividad que la llamo, cierra esta y trata de abrir en la otra liquid.

        if (nofromclick) {
            ClickedRow = -1;
        }
        Log.e("error","llama metodo. "+ClickedRow);
        Intent iBackActivity = new Intent(this, MenuPrincipal.class);
        iBackActivity.putExtra("fila", ClickedRow + "");
        setResult(RESULT_OK, iBackActivity);
        finish();
    }
}
