package com.gselectroCaqueta.accesoyseguridad;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;  // ← Agrega este import

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.Settings;
import android.text.InputFilter;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.gselectroCaqueta.comunicaciones.BDComunicaciones;
import com.gselectroCaqueta.comunicaciones.CrudComunicaciones;
import com.gselectroCaqueta.comunicaciones.SoapArrays;
import com.gselectroCaqueta.moduloMapas.MapsActivity;
import com.gselectroCaqueta.moduloMapas.NavigationHelper;
import com.gselectroCaqueta.moduloanomalias.AlertaCuentaD;
import com.gselectroCaqueta.moduloanomalias.AnomaliaDeNoLectura;
import com.gselectroCaqueta.moduloanomalias.ModuloDeAnomalias;
import com.gselectroCaqueta.modulobluetooth.DeviceListActivity;
import com.gselectroCaqueta.modulobluetooth.btPrintFile;
import com.gselectroCaqueta.modulobluetooth.msgTypes;
import com.gselectroCaqueta.modulobusquedacuenta.ModuloBusquedaCuenta;
import com.gselectroCaqueta.modulobusquedacuenta.ModuloConsultaNoEnviados;
import com.gselectroCaqueta.modulocambiodigitos.ModuloCambioDigitos;
import com.gselectroCaqueta.modulocaptnovedlect.ModuloAdicionarCertificado;
import com.gselectroCaqueta.modulocaptnovedlect.ModuloCaptNovLect;
import com.gselectroCaqueta.modulocaptnovedlect.ModuloConfigFormatoImpresion;
import com.gselectroCaqueta.moduloscannerhardware.ModuloEscanerHardware;
import com.gselectroCaqueta.moduloscannerhardware.ModuloScannerKT40Q;
import com.gselectroCaqueta.tablas.EnvioGPS;
import com.gselectroCaqueta.tablas.Generica;
import com.gselectroCaqueta.tablas.TablaClaseServicio;
import com.gselectroCaqueta.tablas.TablaClienteSalida;
import com.gselectroCaqueta.tablas.TablaCobrosRealizados;
import com.gselectroCaqueta.tablas.TablaContadorSalida;
import com.gselectroCaqueta.tablas.TablaConvenios;
import com.gselectroCaqueta.tablas.TablaDescripcionConceptos;
import com.gselectroCaqueta.tablas.TablaEncabezado;
import com.gselectroCaqueta.tablas.TablaEntradaClientes;
import com.gselectroCaqueta.tablas.TablaFestivos;
import com.gselectroCaqueta.tablas.TablaMedidorEntrada;
import com.gselectroCaqueta.tablas.TablaMensajes;
import com.gselectroCaqueta.tablas.TablaMunicipios;
import com.gselectroCaqueta.tablas.TablaRangos;
import com.gselectroCaqueta.tablas.TablaRegistroDeEntrada;
import com.gselectroCaqueta.tablas.TablaRegistroSalida;
import com.gselectroCaqueta.tablas.TablaTarifas;
import com.gselectroCaqueta.impresion.AdaptadorMovil;
import com.gselectroCaqueta.impresion.AjustesImpresora;
import com.gselectroCaqueta.impresion.DatosFactura;
import com.gselectroCaqueta.impresion.RenderFactura;
import com.gsutil.AsyncResponse;
import com.gsutil.DatosWS;
import com.gsutil.Printzpl;
import com.gsutil.Utils;
import com.gsutil.UtilsNet;
import com.gsutil.WSSoap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;

public class MenuDeLiquidacion extends AppCompatActivity implements AsyncResponse, View.OnClickListener {

    private static final String TAG = "Menu Liquidacion - btprint";
    ImageButton imagenPrinter;
    private static int TAKE_PICTURE = 1;
    private AudioManager audiom;
    Spinner spinnerMenu;
    TablaEncabezado tablaEncabezado = new TablaEncabezado();
    public final int ADICINCERTIFCAD_REQUEST_CODE = 6680;//Ax para Recibir de la actividad del Modulo de Adicionar Verificacion
    public final int ADICINRVERFCION_REQUEST_CODE = 6679;//Ax para Recibir de la actividad del Modulo de Adicionar Verificacion
    public final int ADICIONARNOVDAD_REQUEST_CODE = 6674;//Ax para Recibir de la actividad del Modulo de Novedad Lectura
    public final int ANOMALIA_REQUEST_CODE = 6676;//Ax para enviar a actividad Anomalias, ESTE NUMERO ES ARBITRARIO
    public final int BUSQUEDA_REQUEST_CODE = 6678;//Ax para Recibir de la actividad de busqueda cerrada, ESTE NUMERO ES ARBITRARIO
    public final int CAPT_COMENTARIO_REQUEST_CODE = 6675;//Ax para Recibir de la actividad del menu capturar comentario cerrada
    public final int CHATINSETAR_REQUEST_CODE = 6682;
    public final int CONFIGFORMATIMP_REQUEST_CODE = 6681;//Ax para Recibir de la actividad del Modulo de Configuracion Formato Impresion
    public final int CONSULTNONVIADS_REQUEST_CODE = 6677;//Ax para Recibir de la actividad del Modulo de Consulta No Enviados
    public final int SELECCIONDELOTE_REQUEST_CODE = 6672;//Ax para Recibir de la actividad del Modulo de Sel lote
    public final int SCANNER_TECHDATA_REQUEST_CODE = 6683;//Ax para Recibir de la actividad del Modulo de Sel lote
    public final int ALERTACUENTADIRECTA_REQUEST_CODE = 6685;
    public final int CAMBIODIGITOS_REQUEST_CODE = 6686;
    public final int CAMARA_LECTURA_REQUEST_CODE = 6687; // OCR lectura de medidor
//se crean nuevas variablles para valorizar los consumos por los meces que aplicara el convenio esto por los trimestrales

 double PesosEnergiaConvenio=0;
 double PesosEnergiaContrConvenio=0;
 double PesosEnergiaReactConvenio=0;
 double PesosEnergiaReactContrConvenio=0;
    // ── OCR ──────────────────────────────────────────────────────────────────
    // true  → la siguiente foto es del medidor: se lanzará CamaraLecturaActivity
    // false → foto normal (causal, informe, supervisor) sin OCR
    boolean esFotoMedidor = false;
    boolean yaTomoFotoOcr = false;   // ← AGREGAR
    String  lecturaOcrCapturada  = ""; // lectura que devolvió el OCR para comparar
    boolean fotoOcrObligatoria   = false; // MODIFICACIONES==1: foto OCR obligatoria
    // ── Validación de proximidad al predio ───────────────────────────────────
    private static final int UBI_NO_VALIDADO     = 0;
    private static final int UBI_DENTRO_RANGO    = 1;
    private static final int UBI_FUERA_RANGO     = 2;
    private static final int UBI_SIN_COORDENADAS = 3;
    private static final int UBI_SIN_GPS         = 4;
    private int estadoUbicacion = UBI_NO_VALIDADO;
    private float  distanciaActualPredioMetros = -1f;
    private int intentosLecturaFueraRango = 0;  // contador de intentos fuera de rango
    private static final float RADIO_PERMITIDO_METROS = 100f;
    private static final int MAX_INTENTOS_FUERA_RANGO = 3;
    // ─────────────────────────────────────────────────────────────────────────

    public final int PERMISSION_EXTERNAL_STORAGE = 2442;
    public int[] posicfotocont;
    public int[] posicLblCont;

    public EnvioGPS misenvios = new EnvioGPS();
    public EnvioGPS MisEnviosHilos = new EnvioGPS();
    public File logfile; //Ax: Es para los log de error
    public String nombreDeArchivo = "";
    public String[] Archivos_CPCAN; //ver ...envios aforos, novedades etc
    public TablaCobrosRealizados InfoCobrosHilos = new TablaCobrosRealizados();
    AnomaliaDeNoLectura anomaliaDeLectura = new AnomaliaDeNoLectura();
    ApuntadorCliente miApuntador = new ApuntadorCliente();
    AsyncCallChat taskChat = null;
    DatosWS wsoap = new DatosWS("", "", "", 0);
    NotificationCompat.Builder notificacion;
    int ArchivosCorruptos = 0;
    boolean reimpresion_factura = false; //indica si retorna del MostrarAlertDialog cuando es llamado de reimpresiondefactura
    boolean modoRetorna = false; //indica si retorna del MostrarAlertDialog con un si o un no.
    boolean verificaBarras = false; //Variable para verificar barra impresa en Rural.
    Boolean activaLectorBarras = true;
    boolean banderaChat = false;
    boolean banderaWsOcupado = false; //Ax: es para inhabilitar acciones mientras se eejcta asyntask
    Boolean conn = true; //Ax: guarda estado si la impresora para permitir uso de botones
    boolean controlEdit = false;
    Boolean imprimirFactura = true;
    boolean noactforesult = true; //Ax: me indica si vengo del activityresult (MODULORESPUESTA_REQUEST_CODE)
    boolean procesandoenvioenHilos_chat = false;
    Boolean prt = true; //Ax: solo se usa para dar efecto de conectando... a impresora
    boolean salirse = false;
    boolean ventanaConsumo = false;
    boolean fotoynovedad = false;
    Boolean variableTomarDatosLectura = false;
    //DecimalFormat formato = new DecimalFormat("#,###,###");// Formato para utilizar en las impresiones de valores en moneda
    double pesosenergiatrimestra = 0;
    double subsidiocontribucionreactiva = 0;
    double valorconsumo = 0;
    double valorLiquidacionEmpleado = 0;
    double valorrango1;
    double valorreferenciareactiva = 0;
    double Valor1ParaConvenio = 0;
    double Valor2ParaConvenio = 0;
    double Pesos_energia_trimestra = 0;
    double TotalAseo = 0;

    boolean medidorCero = false;
    //double deudaAnterior = 0; //variable para imprimir inmediato
    String comandoconceptosAseo="";
    File logPrint; //Ax: Es para guardar la de mac de impresora bluetooth
    ImageButton btnActivaScanner, btnAnomalia, btnBuscar,btnTomarFoto;
    ImageButton btnAdelante;
    ImageButton btnAtras;
    ImageButton btnFotografia;
    ImageButton btnPrimero;
    ImageButton btnUltimo;

    ImageView imagenChat;
    ImageView imagenLiquid_1;
    ImageView imagenLiquid_2;
    ImageView imagenLiquid_3;
    ImageView imagenRed;

    public boolean tryAlL = false;
    int conttryall = 0;
    public static int contocup = 0;
    int alterno;
    String cod_cuentaUlt_foto = "999999999";
    String mesUlt_foto = "99";
    String nrocontadordb = "";
    int apuntadortarifaactiva = 1;
    int apuntadortarifaCT = 0;
    int apuntadortarifareactiva = 0;
    int banderaadicionarNovedad = 0; //Ax: para saber que ya se contesto un alert dialog
    int banderaCuentaContratada = 0; //Ax: para saber que ya se contesto un alert dialog
    int capRegistroActual = 0;//Captura registro actual que se esta procesando, para los que regresan de otras actividades
    int cc_indlectura;//Ax: Para recuperar datos de procesarlectura
    int cc_primero;//Ax: Para recuperar datos de procesarlectura
    int conexionGPRSActiva = 1;
    int contadorChat = 0;
    int contadorCuentasGPS = 0; //Ax: cuenta 5 min para enviar este archivo(CuentasGPS) solo para supervisores
    int contadorCuentasGPS_346 = 1800; //Ax: cuenta 30,40 y60 min por si un supervisor no ha hecho nada
    int contadorCuentasGPS_ctrl = 0; //Ax: cuenta 30,40 y60 min por si un supervisor no ha hecho nada
    // int diassinrecargo, diasconrecargo;
    int encenderEstadoActualGPS = 1;
    int ErroresDeEnvio = 0;
    int exitoImagen = 0;//Ax: = 1 si la imagen se tomó
    int fuePromediado = 0;// ESTA VARIABLE NOS INDICARA SI SE PROMEDIO AL MOMENTO DE IMPRIMIR LA FACTURA POR SU CRITOCA ALTA
    int indicadorManual = 0;
    int llamoatomarfoto = 0;
    int finalizolectura = 0;
    int leyoCoordenadas = 0;
    int moverGrafico = 0;
    int msgCorto = 1000;//200

    // The Handler that gets information back from the VariablesGlobales.btPrintService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case msgTypes.MESSAGE_STATE_CHANGE:
                    //Bundle bundle = msg.getData();
                    setConnectState(msg.arg1);

                    switch (msg.arg1) {
                        case btPrintFile.STATE_CONNECTED:
                            addLog("connected to: " + VariablesGlobales.mConnectedDeviceName);
                            VariablesGlobales.mConversationArrayAdapter.clear();
                            break;
                        case btPrintFile.STATE_CONNECTING:
                            addLog("connecting...");
                            break;
                        case btPrintFile.STATE_LISTEN:
                            addLog("connection ready");
                            break;
                        case btPrintFile.STATE_IDLE:
                            addLog("STATE_NONE");
                            break;
                        case btPrintFile.STATE_DISCONNECTED:
                            addLog("disconnected");
                            break;
                    }
                    break;
                case msgTypes.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    VariablesGlobales.mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case msgTypes.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    VariablesGlobales.mConversationArrayAdapter.add(VariablesGlobales.mConnectedDeviceName + ":  " + readMessage);
                    addLog("recv>>>" + readMessage);
                    break;
                case msgTypes.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    VariablesGlobales.mConnectedDeviceName = msg.getData().getString(msgTypes.DEVICE_NAME);
                    mensajeT("Connected to " + VariablesGlobales.mConnectedDeviceName, msgCorto);
                    updateConnectButton(false);

                    break;
                case msgTypes.MESSAGE_TOAST:
                    mensajeT(msg.getData().getString(msgTypes.TOAST), msgCorto);
                    addLog(msg.getData().getString(msgTypes.TOAST));
                    break;
                case msgTypes.MESSAGE_INFO:
                    addLog(msg.getData().getString(msgTypes.INFO));
                    String s = msg.getData().getString(msgTypes.INFO);
                    if (s.length() == 0)
                        s = String.format("int: %i" + msg.getData().getInt(msgTypes.INFO));
                    break;
            }
        }
    };
    int msgLargo = 1700;//1000
    int msgMedio = 1400;//500
    int noavanzalinea = 0;
    int predio_temporal = 1;
    int procesandoenvioenHilos = 0;
    int procesandoenvioenHilos_CPCAN = 0;
    int reimprimirxerror = 0;
    int sinBarSence = 0;
    int EsImpresora521 = 1;
    int tiempoInactivolaImpresora = 0;
    int tipoFotoDigital = 0;
    int trama = 100;
    int vecesImagen = 0;//Ax: Captura la cantidad de reintentos de tomar una foto
    Resources rs;
    ScrollView scroll; //Ax: para cambiar el tamaño del scrollview

    private String namePhoto;
    String RutaZip = ""; //Ax: Nombre del zip a enviar al servidor
    String nombredelaImagen_2 = "";
    String usarScannerHardware = ""; //indica si se debe usar el scanner LASER del techdata PA700 o se usa la camara del cel.
    String PCODIGO = "";
    String NROCONTADOR = "";
    String IDCONTADOR = "";
    String LECTURAFACTURADA = "";
    String CONSUMOLIQUIDADO = "";
    String CAUSANOLECTURA = "";
    String CuentaDiferencias = "";
    String altitudReportadaGPS = "0";
    String cadenaconceptosimpresos = "";
    String causadenolectura1 = "";
    String causadenolectura2 = "";
    String causadenolectura3 = "";
    String causadenolectura4 = "";
    String causadenolectura5 = "";
    String causal = "";//Captura causales de lectura que regresan de otras actividades
    String cc_causaact;//Ax: Para recuperar datos de procesarlectura
    String cc_lecturaact;//Ax: Para recuperar datos de procesarlectura
    String Ciclo = "0000";//Ax: cargados desde archivo nombre
    String criticaPDA1 = "";
    String criticaPDA2 = "";
    String criticaPDA3 = "";
    String criticaPDA4 = "";
    String criticaPDA5 = "";
    String digitos1 = "";
    String digitos2 = "";
    String digitos3 = "";
    String digitos4 = "";
    String digitos5 = "";
    String Division = "0";
    String esComprimido = "";
    String fechaanteriorenformato = "";
    //    String fechapago1, fechapago2;
//    String fechapago1formato = "";
//    String fechapago2formato = "";
    String fechayHoraReportadaGPS = getPhoneDate() + " " + getPhoneHour();
    String idContador1 = "";
    String idContador2 = "";
    String idContador3 = "";
    String idContador4 = "";
    String idContador5 = "";
    String impresora = "";
    String intentos1 = "";
    String intentos2 = "";
    String intentos3 = "";
    String intentos4 = "";
    String intentos5 = "";
    String latitudActualGpg = "0.00";
    String lector = "";
    String lecturaAnterior1 = "";
    String lecturaAnterior2 = "";
    String lecturaAnterior3 = "";
    String lecturaAnterior4 = "";
    String lecturaAnterior5 = "";
    String lecturaModificada11 = "";
    String lecturaModificada12 = "";
    String lecturaModificada13 = "";
    String lecturaModificada14 = "";
    String lecturaModificada15 = "";
    String lecturaModificada21 = "";
    String lecturaModificada22 = "";
    String lecturaModificada23 = "";
    String lecturaModificada24 = "";
    String lecturaModificada25 = "";
    String lecturaTomada1 = "";
    String lecturaTomada2 = "";
    String lecturaTomada3 = "";
    String lecturaTomada4 = "";
    String lecturaTomada5 = "";
    String longitudActualGpg = "0.00";
    String mensajeNovedad = "";
    String mesenformato = "";
    String metodo = ""; //Ax: lleva el metodo a ejecutar en Asyntask
    String verificaBarrasCuenta = "";
    String Municipio = "000";
    String MODO = "";
    String nivelOperador = "";
    String nombredelaImagen = "";//Ax: Captura el nombre del archivo de imagen
    String nombrefoto = ""; //Ax: lleva el nombre de la foto que se creara para despues mandarla a optimizar
    String nroContador1 = "";
    String nroContador2 = "";
    String nroContador3 = "";
    String nroContador4 = "";
    String nroContador5 = "";
    String numeroDeSatelitesGPS = "0";
    String paginaWs = "";
    String RutaAdministrador = "";
    String EsComprimido = "";
    String cadenaURLapi = "";
    int puertoAPI = 0;
    String periodoactual = "";
    String respuesta = "";
    String rutaAdministrador = "";
    String rutaCargadaPDA = "";
    String Seccion = "000";
    String serialPDA = "000000000000000";
    String terminal = "";
    //String tipoDeMedidor = "S";
    String ultimaCriticaLectura = "  ";
    // String ultimaLatitud = "75.012";// esperar que me manden las coordenada
    String ultimaLongitud = "0.12";
    String URL = "";
    String valor_alumbradopublico = "0";
    String valor_energia = "0";
    String valorLeidoFactura = "0";
    String velocidadGPS = "0";
    String TipoMedida1 = "";
    String TipoMedida2 = "";
    String TipoMedida3 = "";
    String TipoMedida4 = "";
    String TipoMedida5 = "";
    String[] arma_cuentasGps = new String[6];//Ax. guarda datos para la funcion guardarcoordenadas
    String lecturaT = "";
    String nombreBackup = "";

    TablaClaseServicio tablaClaseServicio = new TablaClaseServicio();
    TablaClienteSalida infoClienteSalida = new TablaClienteSalida();
    TablaCobrosRealizados infoCobrosLiquidados = new TablaCobrosRealizados();
    TablaContadorSalida medidorSalida = new TablaContadorSalida();
    TablaConvenios tablaConvenio = new TablaConvenios();
    TablaDescripcionConceptos descripcionConcepto = new TablaDescripcionConceptos();
    TablaEntradaClientes infoClienteEntrada = new TablaEntradaClientes();
    TablaFestivos misFestivos = new TablaFestivos();
    TablaMedidorEntrada infoMedidorEntrada = new TablaMedidorEntrada();
    TablaMunicipios municipio = new TablaMunicipios();
    TablaRegistroDeEntrada infoRegistroEntrada = new TablaRegistroDeEntrada();
    TablaRegistroSalida infoRegistroSalida = new TablaRegistroSalida();
    TablaTarifas tablaTarifa = new TablaTarifas();
    TableLayout tablaResumen;
    TableRow fila;
    TableRow.LayoutParams layoutCampo;
    TableRow.LayoutParams layoutFila;
    TableRow.LayoutParams layoutValor;
    // TextView lblHoraFechaActual;
    TextView lblInfoMedida;
    //TextView lblLectura;
    TextView lblpuntero;
    //TextView txtInformeEscrito;
    TextView txtLatitud;
    EditText txtElectura;//TextView txtLecturaActual;
    TextView txtLongitud;
    TextView txtDistancia;
    TextView txtMedidorCuenta;
    Thread myThread; //Ax: publico para poder detenerlo
    //Toolbar toolbar;
    Utils utils = new Utils();//Ax log y utilidades
    VariablesGlobales variables = new VariablesGlobales();

    int requierenovedad = 0;
    private NotificationManager nm;
    private String name = "";

    String reporteEstadoCritica = "";
    TablaRangos misRangos = new TablaRangos();
    private static final int NOTIF_ALERTA_ID = 0;

    String PromedioCliente1 = "0";
    String PromedioCliente2 = "0";
    String PromedioCliente3 = "0";
    String PromedioCliente4 = "0";
    int TieneCausal40 = 0;

    String ULTIMOMEDIDORLEIDO = "";
    String ultimaMARCALEIDO = "";
    String fecha_corte = "";
    String fecha_vencimiento = "";
    String costoservicio = "";

    //String fecha_anterior_en_formato = "";
    String MensajeFoes = "";
    String aseo = " ";
    String alumbrado = "               ";
    String periodofacturado = "";
    String subsidiocontribucion = "";
    String LiquidacionConsumo1 = "     1234";
    String LiquidacionConsumo2 = "     56789";
    String LiquidacionConsumo3 = "    123123";
    String LiquidacionConsumo4 = "22222     ";
    String SubsidioAseo = "0";
    int TieneConcepto51 = 0;
    String comandoconceptos = "";
    String comandocartera = "ZZZZZZZZ";
    String comandofinanciacion = "";
    //double valorconsumo = 0;
    double sumarbloqueenergia = 0;

    String MunAcuerdo = "";
    String NroAcuerdo = "";
    String DirAcuerdo = "";
    String TelAcuerdo = "";

    public String anosperiodos = "";
    public String mesesperiodos = "";
    public String consumosperiodos = "";
    //iniciar creacion de barras
    String causadesc2;

    int dias = 0;
    int dias2 = 0;
    int LeyoActiva = 0;

    int contadorEnvioCuentasGPS = 0;

    String terminalImei = "";
    String fechaSatelite = getPhoneDate();
    String variableResumenLiquidacion = "";
    String causaTem = "";
    boolean validarCausaAR = false;
    boolean activaCritica = false;
    int indicadorReactivo40 = 0;

    //nueva variable para tarea mapa kim
    public final int MAPA = 6700;
    String valfamilia = "1";
    int filaseleccionada = 0;
    Keyboard mKeyboard;
    KeyboardView mKeyboardView;
    public File logEnvio = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/LOGENVIO.LOG");
    BDifNull bDifNull;

    //------ Teclado Nuevo ---------
    CardView Boton0;
    CardView Boton1;
    CardView Boton2;
    CardView Boton3;
    CardView Boton4;
    CardView Boton5;
    CardView Boton6;
    CardView Boton7;
    CardView Boton8;
    CardView Boton9;
    ImageView Delete;
    TextView Enter;

    SparseArray<String> sparseArray = new SparseArray<>();
    StringBuilder code = new StringBuilder();
    //------- Fin Teclado Nuevo ----

    int NroEnvios = 0;
    int vecesRea = 0;

    String coordenadaLongitud = "";
    String coordenadaLatitud = "";
    String CodCuenta = "";
    String IndRegis = "";
    String FechayHoraLectura = "";
    String FechaGps = "";
    String HoraGps = "";
    Dialog dialogloading = null;
    String CuentasProblema = "";
    int Noleidas = 0;
    int LeidasNoenviadas = 0;

    int ValidandoNoEnv = 0;
    Context ctx;
    String msg_val = "";
    int entrega_carta = 0;
    String msgAdmon = "";
    private Uri imageUri;
    int CodigoIntent = 0;
    String nameForIntent = "";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_de_liquidacion);
        bDifNull = new BDifNull(this);
        ctx = this;
//        toolbar = (Toolbar) findViewById(R.id.toolbar2);
//        setSupportActionBar(toolbar);

        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificacion = new NotificationCompat.Builder(this);

        scroll = (ScrollView) findViewById(R.id.scroll); //Ax: vista para cambiar el tamaño del scroll
        final View activityRootView = findViewById(R.id.panelLecturas1);

        rs = this.getResources();
        tablaResumen = (TableLayout) findViewById(R.id.tablaResumen);
        tablaResumen.setStretchAllColumns(true);
        tablaResumen.setStretchAllColumns(true);
        layoutFila = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        layoutCampo = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.0f);
        layoutValor = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 3.0f);

//        btnActivaScanner = (ImageButton) findViewById(R.id.btnActivaScanner);
//        btnFotografia = (ImageButton) findViewById(R.id.btnFotografia);
        btnAnomalia = (ImageButton) findViewById(R.id.btnAnomalia);
        btnTomarFoto = (ImageButton) findViewById(R.id.btnTomarFoto);
        btnBuscar = (ImageButton) findViewById(R.id.btnBuscar);

        // txtInformeEscrito = (TextView) findViewById(R.id.txtInformeEscrito);
        // lblLectura = (TextView) findViewById(R.id.lblLectura);
        spinnerMenu = (Spinner) findViewById(R.id.spinnerMenu);
        txtMedidorCuenta = (TextView) findViewById(R.id.txtMedidorCuenta);
        lblInfoMedida = (TextView) findViewById(R.id.lblInfoMedida);
        txtElectura = (EditText) findViewById(R.id.txtElectura);
        lblpuntero = (TextView) findViewById(R.id.lblpuntero);
        txtLatitud = (TextView) findViewById(R.id.txtLatitud);
        txtLongitud = (TextView) findViewById(R.id.txtLongitud);
        txtDistancia = (TextView) findViewById(R.id.txtDistancia);
        // lblLectura.setFocusable(false);
        lblInfoMedida.setFocusable(false);
        lblpuntero.setFocusable(false);
        // txtInformeEscrito.setFocusable(false);
        txtMedidorCuenta.setFocusable(false);

        imagenChat = (ImageView) findViewById(R.id.imagenChat);
        imagenLiquid_1 = (ImageView) findViewById(R.id.imagenLiquid_1);
        imagenLiquid_2 = (ImageView) findViewById(R.id.imagenLiquid_2);
        imagenLiquid_3 = (ImageView) findViewById(R.id.imagenLiquid_3);

        imagenPrinter = (ImageButton) findViewById(R.id.imagenPrinter);
        imagenRed = (ImageView) findViewById(R.id.imagenRed);

        btnAdelante = (ImageButton) findViewById(R.id.btnAdelante);
        btnAtras = (ImageButton) findViewById(R.id.btnAtras);
        btnPrimero = (ImageButton) findViewById(R.id.btnPrimero);
        btnUltimo = (ImageButton) findViewById(R.id.btnUltimo);

        imagenRed.setImageResource(R.drawable.redinativa); //Ax: colocar el icono de red inactiva

        //------ Teclado Nuevo ------
        Boton0 = (CardView) findViewById(R.id.Boton0);
        Boton1 = (CardView) findViewById(R.id.Boton1);
        Boton2 = (CardView) findViewById(R.id.Boton2);
        Boton3 = (CardView) findViewById(R.id.Boton3);
        Boton4 = (CardView) findViewById(R.id.Boton4);
        Boton5 = (CardView) findViewById(R.id.Boton5);
        Boton6 = (CardView) findViewById(R.id.Boton6);
        Boton7 = (CardView) findViewById(R.id.Boton7);
        Boton8 = (CardView) findViewById(R.id.Boton8);
        Boton9 = (CardView) findViewById(R.id.Boton9);
        Delete = (ImageView) findViewById(R.id.delete);
        Enter = (TextView) findViewById(R.id.Btn_ok);

        Boton0.setOnClickListener(this);
        Boton1.setOnClickListener(this);
        Boton2.setOnClickListener(this);
        Boton3.setOnClickListener(this);
        Boton4.setOnClickListener(this);
        Boton5.setOnClickListener(this);
        Boton6.setOnClickListener(this);
        Boton7.setOnClickListener(this);
        Boton8.setOnClickListener(this);
        Boton9.setOnClickListener(this);
        Delete.setOnClickListener(this);

        sparseArray.put(R.id.Boton0, "0");
        sparseArray.put(R.id.Boton1, "1");
        sparseArray.put(R.id.Boton2, "2");
        sparseArray.put(R.id.Boton3, "3");
        sparseArray.put(R.id.Boton4, "4");
        sparseArray.put(R.id.Boton5, "5");
        sparseArray.put(R.id.Boton6, "6");
        sparseArray.put(R.id.Boton7, "7");
        sparseArray.put(R.id.Boton8, "8");
        sparseArray.put(R.id.Boton9, "9");


        //------ Fin Teclado Nuevo ---------------------------------

//        mKeyboard = new Keyboard(this, R.xml.keyboard);
//        mKeyboardView = (KeyboardView) findViewById(R.id.keyboardview);
//        mKeyboardView.setKeyboard(mKeyboard);
//        mKeyboardView.setPreviewEnabled(false);
//        mKeyboardView.setOnKeyboardActionListener(mOnKeyboardActionListener);
//        mKeyboardView.setVisibility(View.VISIBLE);
//        mKeyboardView.setEnabled(true);
        String androidId = "";


        // lblHoraFechaActual = (TextView) findViewById(R.id.lblHoraFechaActual);

        wsoap.delegate = this;//Axx Ax: tarea asincrona para enviar datos aqui a processFinish

        Runnable myRunnableThread = new Reloj3();
        myThread = new Thread(myRunnableThread);
        myThread.start();
        Bundle bundle = getIntent().getExtras();
        Log.e("TAG VALISNULL", "fila " + bundle.getString("fila"));
        Log.e("TAG VALISNULL", "operario " + bundle.getString("operario"));
        Log.e("TAG VALISNULL", "terminal " + bundle.getString("terminal"));
        Log.e("TAG VALISNULL", "impresora " + bundle.getString("impresora"));
        Log.e("TAG VALISNULL", "path " + bundle.getString("path"));
        Log.e("TAG VALISNULL", "nivelOperador " + bundle.getString("nivelOperador"));

        if (bundle.getString("fila") != null && bundle.getString("operario") != null && bundle.getString("terminal") != null && bundle.getString("impresora") != null &&
                bundle.getString("path") != null && bundle.getString("nivelOperador") != null) {
            filaseleccionada = Integer.parseInt(bundle.getString("fila")); //Ax: La fila que tal vez fue seleccionada en resumen, si no, es -1

            if (filaseleccionada > 0) {
                VariablesGlobales.registroactual = filaseleccionada;
            }

            variables.setGlobaloperario(bundle.getString("operario"));
            lector = bundle.getString("operario");
            terminalImei = bundle.getString("terminal");
            impresora = bundle.getString("impresora");
            VariablesGlobales.directorioactual = bundle.getString("path");

            nivelOperador = bundle.getString("nivelOperador");//kim
            serialPDA = terminalImei;// getSerialNumber();
            terminal = terminalImei;
        }
        if (serialPDA == null || lector == null) {
            Log.e("Tag null ", "Entra a es nulo");
            esNulo();
        }


        String externalpath = ctx.getExternalFilesDir(null).getParent();
        String hardcoding = "/Android/data/";//Ax: todo: cambiar este hardcoding

        if (externalpath.contains(hardcoding)) {
            externalpath = externalpath.substring(0, externalpath.indexOf(hardcoding));
        }


        VariablesGlobales.directorioactual = externalpath;

        dias = variables.diasvence;
        dias2 = variables.diascorte;

        logfile = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/LOGEVENTOS.LOG");
        logPrint = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/PRINTER.LOG");
        if (!cargarArchivoNombre()) {
            mensajeT("Problema con la carga de Archivo: Nombre", msgLargo);
            return;
        }

        if (CargarArchivoCarga()) {
            mensajeT("No hay Archivo de Soporte...\n //ArchivosCarga.cfi", msgLargo);
        }

        if (!getParamsWs()) {
            String g = "NO hay IP configurada! Realice ajustes";
            mensajeT(g, msgLargo);
            // txtInformeEscrito.setText(g);
        }

        File ffile = new File(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/" + "F" + Ciclo + Municipio + Seccion + Division);
        if (!ffile.exists()) {
            try {
                ffile.createNewFile();
            } catch (IOException e) {
                mensajeT("No se pudo crear el archivo F", msgLargo);
                utils.Log(logfile, "[menuDeLiquidacionLoad()]: Error: " + e);
            }
        }
//        if (!crearvaloresImp())
//            mensajeT("No se creo archivo de valoresImpresion...", msgLargo);

        menuDeLiquidacionLoad();

        // imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

//        btnActivaScanner.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {1117786031
//
//                startBarCodeReader(true, "");
//            }
//        });
//
//        btnFotografia.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//
//                capturaImagenFotografica();
//            }
//        });
        Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int posCursor = txtElectura.length();
                if (posCursor > 0) {
                    txtElectura.setText(txtElectura.getText().delete(posCursor - 1, posCursor));
                    code.setLength(txtElectura.length());
//                    txtLecturaActual.setSelection(posCursor - 1);
                }
            }
        });

        Enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtLecturaActualKeyPress(5);

                /*if (conttryall < 3) {
                    conttryall++;
                    tryAll();//activar codigo
                }*/
                code.setLength(0);
            }
        });
        btnBuscar.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                opcionMenu(6);
            }
        });

        btnAnomalia.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                code.setLength(0);
                opcionMenu(1);
            }
        });

        btnTomarFoto.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                code.setLength(0);
                opcionMenu(11);
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_menu, new String[]{" MENÚ... \u25bc "
                , "Capturar Anomalía"
                , "Reimprimir Factura"
                , "Anterior Predio"
                , "Capturar Novedad Lect"
                , "Modificar Nro Digitos"
                , "Buscar Cliente"
                , "Des/Habilitar Impresora"
                , "Des/Asociar Impresora"
                , "Config. Preimpreso Fact"
                , "Estadistica Del Proceso"
                , "Tomar Foto"
                , "Utilidades"
                , "Enviar Facturacion Gprs"
                , "Procesar Reenvio Gprs"
                , "Evaluar Noenviadas Gprs"
                //, "Reconstruir Envio Cuenta"
                , "Liquidación Automática"
                , "Enviar Fotos"
                , "Activar Wifi"
                , "Mapa"
                , "Envio Registros"
                , "Volver"
        });
        spinnerMenu.setAdapter(adapter);

        spinnerMenu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                opcionMenu(position);
                spinnerMenu.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        spinnerMenu.setSelection(0);
//
//        txtLecturaActual.addTextChangedListener(new TextWatcher() {
//
//            public void afterTextChanged(Editable s) {
//                //se comenta para usar el unitech  el laser lector de codigo de barras
//                //MenuDeLiquidacion.this.txtLecturaActual.requestFocus();
//            }
//
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                if (controlEdit && indicadorManual == 0) {
//                    txtLecturaActual.requestFocus();
//                }
//            }
//
//            public void onTextChanged(CharSequence xs, int start, int before, int count) {
//
//                if (indicadorManual == 0) {
//                    if (xs.length() < 1) return;
//
//                    String s = xs.toString();
//
//                    if (s.trim().contains("-") || s.contains(" ") || s.trim().contains(".") || s.trim().contains("N")) { //Ax: caracteres de teclado usados como alternativa para menus y demás
//                        txtLecturaActualKeyDown(s);
//                        txtLecturaActual.setText("");
//                        return;
//                    } else {
//                        if (s.trim().length() == 1) { //Ax: aqui evita entrar a el loop una y otra vez solo entra cuando >0
//                            txtLecturaActualKeyPress(9999);
//                        }
//                    }
//
//                    if (s.length() > VariablesGlobales.intcontroltexto)//intcontroltexto se llena en otro lado y depende de lo que se va a liquidar
//                        txtLecturaActual.setText(s.substring(0, VariablesGlobales.intcontroltexto));//Ax ojo cambiar esto, posible error
//
//                    EditText etext = (EditText) findViewById(R.id.txtLecturaActual);//Ax: Coloca el cursor al final
//                    etext.setSelection(etext.getText().length());
//                }
//            }
//        });
//
//        txtLecturaActual.setOnEditorActionListener(new OnEditorActionListener() {
//
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//
//                if (indicadorManual == 0) {
//                    if (actionId == 6 || actionId == 5) { //Ax "tecla ENTER" telefono chino ax = 5 otro = 6
//                        txtLecturaActualKeyDown("");//Estos dos metodos estan tambien mas arriba debido a que en c# se captura evento de teclado, aqui se captura evento de teclado por separado
//                        txtLecturaActualKeyPress(actionId);
//
//                        if (conttryall < 3) {
//                            conttryall++;
//                            tryAll();//activar codigo
//                        }
//                    }
//                }
//                return false;
//            }
//        });

//        txtInformeEscrito.setOnKeyListener(new OnKeyListener() {
//
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//
//                if (event.getAction() == KeyEvent.ACTION_DOWN)
//                    mensajeKeyPress(keyCode, event);
//                return false;
//            }
//        });

        btnAdelante.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (indicadorManual == 0)
                    txtElectura.setText("");
                abrirArchivosDeFacturacion();
                avanzarRegistro();
                visualizarInformacionCliente(0);
                cerrarArchivosFacturacion();
                variables.impresora = "   ";
            }
        });

        btnAtras.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (indicadorManual == 0)
                    txtElectura.setText("");
                abrirArchivosDeFacturacion();
                retrocedeRegistro();
                visualizarInformacionCliente(0);
                cerrarArchivosFacturacion();
                variables.impresora = "   ";
            }
        });

        btnPrimero.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (indicadorManual == 0)
                    txtElectura.setText("");
                abrirArchivosDeFacturacion();
                irPrimerRegistro();
                visualizarInformacionCliente(0);
                cerrarArchivosFacturacion();
                variables.impresora = "   ";
            }
        });

        btnUltimo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (indicadorManual == 0)
                    txtElectura.setText("");
                abrirArchivosDeFacturacion();
                irUltimoRegistro();
                visualizarInformacionCliente(0);
                cerrarArchivosFacturacion();
                variables.impresora = "   ";
            }
        });

        // Altura del scroll proporcional a la pantalla
        android.util.DisplayMetrics dm = new android.util.DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        // Reservar 40% de la pantalla para el scroll, minimo 80dp, maximo 400dp
        int scrollH = (int)(dm.heightPixels * 0.30f);
        int minH = (int)(80 * dm.density);
        int maxH = (int)(400 * dm.density);
        if (scrollH < minH) scrollH = minH;
        if (scrollH > maxH) scrollH = maxH;
        scroll.getLayoutParams().height = scrollH;
        scroll.requestLayout();

//        //Ax: detecta si el teclado virtual(softkey) esta presente y aumenta el Scrollview.
//        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                int heightview = activityRootView.getHeight();
//
//                if (heightview < 250) {
//                    scroll.getLayoutParams().height = 60;//Para Telefonos pequeños
//                    scroll.requestLayout();
//                } else if (heightview < 400) {
//                    scroll.getLayoutParams().height = 200;//Para Telefonos pequeños
//                    scroll.requestLayout();
//                } else if (heightview < 650) {
//                    scroll.getLayoutParams().height = 340;
//                    scroll.requestLayout();
//
//                } else if (heightview > 700) {
//                    scroll.getLayoutParams().height = 700;
//                    scroll.requestLayout();
//                } else if (heightview > 651) {
//                    scroll.getLayoutParams().height = 420;//Para Telefonos pequeños
//                    scroll.requestLayout();
//                }
//            }
//        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // mensajeT("GPS No esta activo", msgLargo);
        }
        // ===== RASTREO GPS (tracking) =====
        try {
            if (getParamsWs() && cadenaURLapi != null && !cadenaURLapi.isEmpty()) {
                int codOperador = 0;
                try {
                    codOperador = Integer.parseInt(lector.trim());
                } catch (NumberFormatException e) {
                    utils.Log(logfile, "[MenuDeLiquidacion] tracking | cod_operador no numérico: " + lector);
                }

                boolean permisoFine = ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

                if (codOperador > 0 && permisoFine) {
                    TrackingLocationService.iniciar(this, codOperador, cadenaURLapi);
                } else {
                    utils.Log(logfile, "[MenuDeLiquidacion] tracking no iniciado | codOp="
                            + codOperador + " permisoFine=" + permisoFine);
                }
            } else {
                utils.Log(logfile, "[MenuDeLiquidacion] tracking | getParamsWs() falló o cadenaURLapi vacía");
            }
        } catch (Exception e) {
            utils.Log(logfile, "[MenuDeLiquidacion] tracking ERROR -> " + e.getMessage());
        }

        LocationManager locationmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener mlocListener = new UsarGPS();
        locationmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, mlocListener);

        if (!locationmanager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mensajeT("GPS No esta activo", msgLargo);
            encenderEstadoActualGPS = 0;
        }
        View thisview = this.getCurrentFocus();
        if (thisview != null) {
            InputMethodManager inm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inm.hideSoftInputFromWindow(thisview.getWindowToken(), 0);
        }


        getInfoCel();
        ObtenerDirectorioBackup();
        imagenRed.setImageResource(R.drawable.redactiva);
        // imagenChat.setImageResource(R.drawable.chat_msg_no);  //COMENTADO CHAT 0991

        imagenPrinter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!conn) return; //Si se esta conectado

                abrirArchivosDeFacturacion(); //Ax:?

                if (!VariablesGlobales.bDiscoveryStarted) {

                    String conectadoa = "";

                    if (VariablesGlobales.btPrintService.getState() != btPrintFile.STATE_DISCONNECTED && VariablesGlobales.btPrintService.getState() != btPrintFile.STATE_IDLE) {
                        conectadoa = utils.ReadLine(logPrint);
                    }
                    VariablesGlobales.bDiscoveryStarted = true;
                    // Lanza DeviceListActivity para ver dispositivos y escanear

                    Bundle bundlei = new Bundle();
                    bundlei.putString("conectadoa", conectadoa);
                    Intent serverIntent = new Intent(getApplicationContext(), DeviceListActivity.class);
                    serverIntent.putExtras(bundlei);

                    startActivityForResult(serverIntent, VariablesGlobales.REQUEST_CONNECT_DEVICE);
                }
                Log.e("error", "verifica victor 1... " + VariablesGlobales.registroactual);
                infoRegistroSalida.lectura_TablaRegistroSalida(VariablesGlobales.registroactual);
                visualizarInformacionCliente(0);
                cerrarArchivosFacturacion();
            }
        });

        //tryAll();
        apagarWifi();
//        closeKeyboard();
        // lecturaActual();
    }// end_onCreate

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            TrackingLocationService.detener(this);
        } catch (Exception e) {
            utils.Log(logfile, "[MenuDeLiquidacion]onDestroy() tracking -> " + e.getMessage());
        }
    }

    @Override
    public void onClick(View view) {
        String cadena = sparseArray.get(view.getId());
        code.append(cadena);
        txtElectura.setText(code);
    }

    @Override
    public void onBackPressed() {
        // Log.e("error", "hace el back 1");
        try {
            if (myThread.isAlive()) {
                myThread.interrupt();
            }
        } catch (Exception ex) {
            salirse = true;
        }
        // Log.e("error", "hace el back 2");
        finish();
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//
//        View view = this.getCurrentFocus();
//        if (view != null) {
//            view.clearFocus();
//            openKeyboard(view);
//        }
//
//    }
//    public void closeKeyboard(){
//        View view = this.getCurrentFocus();
//        if (view != null){
//            view.clearFocus();
//            InputMethodManager inm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//            inm.hideSoftInputFromWindow(view.getWindowToken(),0);
//        }
//    }

    public void openKeyboard(View v) {
        mKeyboardView.setVisibility(View.VISIBLE);
        mKeyboardView.setEnabled(true);
        if (v != null)
            ((InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private KeyboardView.OnKeyboardActionListener mOnKeyboardActionListener = new KeyboardView.OnKeyboardActionListener() {

        @Override
        public void onKey(int primaryCode, int[] keyCodes) {
        }

        @Override
        public void onPress(int arg0) {
        }

        @Override
        public void onRelease(int primaryCode) {

            playSound(primaryCode);
            switch (primaryCode) {

                case -1:
                    int posCursor = txtElectura.getSelectionStart();
                    if (posCursor > 0) {
                        txtElectura.setText(txtElectura.getText().delete(posCursor - 1, posCursor));
                        txtElectura.setSelection(posCursor - 1);
                    }
                    break;
                case 100: //Ax: Enter
                    txtLecturaActualKeyPress(5);

                    /*if (conttryall < 3) {
                        conttryall++;
                        tryAll();//activar codigo
                    }*/
                    break;
                default:
                    if (medidorCero) {
                        txtLecturaActualKeyPress(0);
                    } else {
                        int posCurs0r = txtElectura.getSelectionStart();
                        if (posCurs0r != txtElectura.length()) {
                            txtElectura.getText().insert(txtElectura.getSelectionStart(), "" + primaryCode);
                            txtElectura.setSelection(posCurs0r + 1);
                        } else {
                            txtElectura.setText(txtElectura.getText().toString() + primaryCode);
                            txtElectura.setSelection(txtElectura.length());//Ax: Coloca el cursor al final
                        }
                    }
                    break;
            }
        }

        @Override
        public void onText(CharSequence text) {
        }

        @Override
        public void swipeDown() {
        }

        @Override
        public void swipeLeft() {
        }

        @Override
        public void swipeRight() {
        }

        @Override
        public void swipeUp() {
        }
    };

    private void playSound(int keyCode) {

        audiom = (AudioManager) getSystemService(AUDIO_SERVICE);

        switch (keyCode) {

            case -1:
                audiom.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_UP, 1f);
                break;
            case 100:
                audiom.playSoundEffect(AudioManager.FX_KEYPRESS_INVALID, 1f);
                break;
            default:
                audiom.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 1f);
        }
    }

    public void esNulo() {
        try {
            Log.e("TAG NULO", "Entra a nulo");
            CrudifNull crudifNull = bDifNull.obtenerdatabyid(1);
            filaseleccionada = Integer.parseInt(crudifNull.getFila());
            if (filaseleccionada > 0) {
                VariablesGlobales.registroactual = filaseleccionada;
            }
            variables.setGlobaloperario(crudifNull.getOperario());
            tomarLector();
            lector = crudifNull.getOperario();
            lector = variables.getGlobaloperario();
            terminalImei = crudifNull.getImei().trim();
            serialPDA = terminalImei;
            terminal = terminalImei;
            impresora = crudifNull.getImpresora();
            VariablesGlobales.directorioactual = crudifNull.getPath();
            nivelOperador = crudifNull.getNivelOperador();

            Log.e("TAG ESNULO", "fila " + filaseleccionada);
            Log.e("TAG ESNULO", "operario " + lector);
            Log.e("TAG ESNULO", "terminal " + terminal);
            Log.e("TAG ESNULO", "impresora " + impresora);
            Log.e("TAG ESNULO", "path " + VariablesGlobales.directorioactual);
            Log.e("TAG ESNULO", "nivelOperador " + nivelOperador);


        } catch (Exception e) {
            Log.e("TAG ESNULO", String.valueOf(e));
        }

        //finish();

    }

    private boolean verificarConfSistema(String fechasis) {//96330325
        boolean airplane_conex, date_act;
        try {
            // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            airplane_conex = Settings.System.getInt(this.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;

            SimpleDateFormat ftsis = new SimpleDateFormat("dd/MM/yyyy");
            Date datesis = ftsis.parse(fechasis);
            date_act = new Date().after(datesis);
            Log.e("INFO", "airplane_conex: " + airplane_conex + " | date_act: " + date_act);
            if (airplane_conex && !date_act) {// Modo avion esta encendido y la fecha del sistema esta atrazada frente a la del proceso
                msg_val = "El modo avion se encuentra activo y la fecha del sistema esta desactualizada, comuniquese con el supervisor";
                return false;
            }
            if (airplane_conex) {// Modo avion esta encendido
                msg_val = "El modo avion se encuentra activo, comuniquese con el supervisor";
                return false;
            }
            if (!date_act) {// La fecha del sistema esta atrazada frente a la del proceso
                msg_val = "La fecha del sistema esta desactualizada, comuniquese con el supervisor";
                return false;
            }
            return true;
        } catch (Exception ex) {
            Log.e("ERROR", "verificarConfSistema() | Error -> " + ex.getMessage());
            msg_val = "Error al verificar las variables del sistema por favor comuniquese con el supervisor";
            utils.Log(logfile, "[MenuDeLiquidacion] verificarConfSistema() | Error -> " + ex.getMessage());
            return false;
        }
    }

    public static String getPhoneHour() {

        Date dt = new Date();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        String formatteHour = df.format(dt.getTime());
        return formatteHour;
    }

    public void startBarCodeReader(boolean delMeenu, String contador) { //Booleano indica si viene llamado desde menu

        if (usarScannerHardware.equals("PA700") || usarScannerHardware.equals("KT40Q")) {
            scannerTechData(delMeenu, contador);
            return;
        }

        try {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);//ONE_D_CODE_TYPES
            integrator.setPrompt("CAPTURAR CODIGO DE BARRAS");
            integrator.setCameraId(0);  // Use a specific camera of the device
            integrator.setBeepEnabled(false);
            integrator.initiateScan();
        } catch (Exception ex) {
            verificaBarras = false;
            utils.Log(logfile, "[MenuDeLiquidacion]startBarCodeReader(); " + ex.getMessage());
        }
    }

    private void scannerTechData(boolean delMeenu, String contador) { //Usar el scaneer de td

        try {

            switch (usarScannerHardware) {
                case "PA700":
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("menu", delMeenu);
                    bundle.putString("contador", contador);
                    Intent scan = new Intent(this, ModuloEscanerHardware.class);
                    scan.putExtras(bundle);
                    startActivityForResult(scan, SCANNER_TECHDATA_REQUEST_CODE);
                    break;
                case "KT40Q":
                    Intent scan2 = new Intent(this, ModuloScannerKT40Q.class);
                    startActivityForResult(scan2, SCANNER_TECHDATA_REQUEST_CODE);
                    break;
            }
        } catch (Exception ex) {
            utils.Log(logfile, "[MenuDeLiquidacion]scannerTechData(); " + ex.getMessage());
        }
    }

    public void verificaBarrasRural(String barras) {

        try {
            verificaBarras = false;

            if (barras.length() >= 40) {
                File f = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/BARRASLEIDAS.SDA");
                String Cadena = verificaBarrasCuenta + ";" + String.format("%1$-60s", barras.trim()) + ";\r\n";

                utils.EscribirLinea(f, Cadena);
            } else {
                mensajeT("LECTURA DE SCANER NO VALIDO O\n NO CORRESPONDE A LA ESTRUCTURA", msgLargo);
            }

        } catch (Exception ex) {
            utils.Log(logfile, "[MenuDeLiquidacion]verificaBarrasRural(); " + ex.getMessage());
        }
        verificaBarrasCuenta = "";
    }

    //Ax: trata de obtener la carpeta de Backup en otra SD, sino en otras rutas
    private void ObtenerDirectorioBackup() {
        try {
            String carpetas = Ciclo + Municipio + Seccion + Division;

            if (carpetas.trim().length() == 0) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM_dd_HH_mm_ss");
                carpetas = simpleDateFormat.format(new Date());
            }

            VariablesGlobales.directorioBackUp = VariablesGlobales.directorioactual + "/DCIM/SIMFABACKUP/" + carpetas + "/";

            File file = new File(VariablesGlobales.directorioBackUp);
            if (!file.exists()) file.mkdirs();

            File filetest = new File(VariablesGlobales.directorioBackUp + "FILETEST.SDA");

            if (!filetest.exists()) {
                if (!utils.EscribirLinea(filetest, carpetas)) { //Ax: si falla la escritura el backup cambia  TODO: usar clase que escribe en SD
                    VariablesGlobales.directorioBackUp = VariablesGlobales.directorioactual + "/DATOSDESALIDA/BACKUP/";
                    new File(VariablesGlobales.directorioBackUp).mkdirs();
                }
            }
            metodo = "comprobarBorradoBackup";
            TaskHelper.execute(new AsyncCallWS(), metodo);
        } catch (Exception e) {
            VariablesGlobales.directorioBackUp = VariablesGlobales.directorioactual + "/DATOSDESALIDA/BACKUP/";
            utils.Log(logfile, "[ObtenerDirectorioBackup()]: Error: " + e);
        }
    }


    private void menuDeLiquidacionLoad() {

        tiempoInactivolaImpresora = 0;

        try {
            if (VariablesGlobales.totalprediosnofacturados == 0) {
                btnUltimo.setEnabled(false);
                btnPrimero.setEnabled(false);
                btnAdelante.setEnabled(false);
                btnAtras.setEnabled(false);
            }
            File file = new File(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/NOMBRE");
            RandomAccessFile rFile = new RandomAccessFile(file, "r");
            byte[] byteArray;
            int fileSize = (int) rFile.length();
            byteArray = new byte[fileSize];
            rFile.readFully(byteArray, 0, fileSize);
            rutaCargadaPDA = new String(byteArray);

            variables.setNombregeneral(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/GENERAL.TXT");
            rutaCargadaPDA = rutaCargadaPDA.substring(4, 12);
            rFile.close();

            VariablesGlobales.setActivarcamarafotografica(1);
            // txtInformeEscrito.setText("");
            // variables.registroactual = 1;
            abrirArchivosPrincipales();

            txtElectura.setEnabled(true);

            cambiarValoresimpresion();

            limpiarMedidores();

            abrirArchivosDeFacturacion();
            infoRegistroEntrada.lectura_TablaRegistroDeEntrada(1);
            infoClienteEntrada.lectura_TablaEntradaClientes(1);
            VariablesGlobales.setTipoDeRuta(infoClienteEntrada.gettablaEntradaClientes_Clasedeservicio().trim());//tipo de ruta se saca aqui ya que en acceso no se hizo
            //nombre del archivo backup
            nombreBackup = String.format("%1$4s", infoClienteEntrada.gettablaEntradaClientes_Ciclo().trim()).replace(" ", "0") + String.format("%1$3s", infoClienteEntrada.gettablaEntradaClientes_Municipio().trim()).replace(" ", "0")
                    + String.format("%1$4s", infoClienteEntrada.gettablaEntradaClientes_anio().trim()).replace(" ", "0") + String.format("%1$2s", infoClienteEntrada.gettablaEntradaClientes_mes().trim()).replace(" ", "0");
            // Log.e("error", " nombreBackup " + nombreBackup);
            cerrarArchivosFacturacion();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            mensajeT("Error en menuDeLiquidacionLoad " + e.getMessage(), msgLargo);
            utils.Log(logfile, "[menuDeLiquidacionLoad()]: Error: " + e);

        } catch (IOException e) {
            e.printStackTrace();
            mensajeT("Error en menuDeLiquidacionLoad " + e.getMessage(), msgLargo);
            utils.Log(logfile, "[menuDeLiquidacionLoad()]: Error: " + e);
        }

        File file = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/VALIDADORIMPRESION.SDA");
        if (file.exists()) {

            try {
                RandomAccessFile rFile = new RandomAccessFile(file, "r");
                byte[] byteArray;
                int fileSize = (int) rFile.length();
                byteArray = new byte[fileSize];
                rFile.readFully(byteArray, 0, fileSize);
                String datoLeido = new String(byteArray);
                rFile.close();

                if (datoLeido != null && !datoLeido.equals("")) {
                    mensajeT("Existe un Registro sin procesar se Inicia Proceso de Liquidacion" + datoLeido, msgMedio);

                    proseguirLiquidacion(datoLeido.substring(0, 10), parseStringToInteger(datoLeido.substring(11, 17).trim()));
                } else {
                    file.delete();
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                mensajeT("Error en menuDeLiquidacionLoad " + e.getMessage(), msgLargo);
                utils.Log(logfile, "[menuDeLiquidacionLoad()]: Error: " + e);

            } catch (IOException e) {
                e.printStackTrace();
                mensajeT("Error en menuDeLiquidacionLoad " + e.getMessage(), msgLargo);
                utils.Log(logfile, "[menuDeLiquidacionLoad()]: Error: " + e);
            }
        }
    }

    private void proseguirLiquidacion(String cuenta, Integer apuntador) {
        try {
            // realizar lo de reimpresion de la cuenta y el envio de una ves tambien
            int alterno;
            abrirArchivosDeFacturacion();
            for (; ; ) {
                alterno = VariablesGlobales.registroactual;
                VariablesGlobales.registroactual = apuntador;

                leerInformacionUsuario(1);
                visualizarInformacionCliente(0);
                if (!infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("L")
                        && !infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("N")
                        && !infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("E")) {
                    procesoDeLiquidacionEImpresion(1);

                }
                break;
            }

            if (!infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("L")
                    && !infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("N")
                    && !infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("E")) {

                if (infoClienteEntrada.gettablaEntradaClientes_Clasedeservicio().trim().equals("IC")) {
                    leerInformacionUsuario(3);

                    nroContador1 = infoRegistroEntrada.gettablaRegistroDeEntrada_nrocontador();//.gettablaRegistroDeEntrada_NROCONTADOR();
                    //[enerca] idContador1 = infoRegistroEntrada.gettablaRegistroDeEntrada_contador();
                    lecturaTomada1 = infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA();
                    causadenolectura1 = String.format("%1$2s", infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim());
                    intentos1 = infoRegistroSalida.gettablaRegistroSalida_INTENTOS();
                    lecturaModificada11 = String.format("%1$10s", infoRegistroSalida.gettablaRegistroSalida_LECTURAMODIFICADA1().trim());
                    lecturaModificada21 = String.format("%1$10s", infoRegistroSalida.gettablaRegistroSalida_LECTURAMODIFICADA2().trim());
                    digitos1 = String.format("%1$1s", infoRegistroEntrada.gettablaRegistroDeEntrada_Digitos());//.gettablaRegistroDeEntrada_Digitos().trim());
                    criticaPDA1 = infoRegistroSalida.gettablaRegistroSalida_LEIDO();
                    lecturaAnterior1 = infoRegistroEntrada.gettablaRegistroDeEntrada_Lecturaanterior();//.gettablaRegistroDeEntrada_LECTURAANTERIOR();
                    if (indicadorManual == 0)
                        mensajeT("Esta cuenta es un Cliente IC", msgCorto);

                    nroContador2 = "";
                    idContador2 = "";
                    lecturaTomada2 = "";
                    causadenolectura2 = "";
                    intentos2 = "";
                    lecturaModificada12 = "";
                    lecturaModificada22 = "";
                    digitos2 = "";
                    criticaPDA2 = "";
                    lecturaAnterior2 = "";
                } else {
                    procesoDeLiquidacionEImpresion(0);
                }

            } else {
                if (infoClienteEntrada.gettablaEntradaClientes_Clasedeservicio().trim().equals("IC")) {

                    leerInformacionUsuario(3);
                    nroContador1 = infoRegistroEntrada.gettablaRegistroDeEntrada_nrocontador();//.gettablaRegistroDeEntrada_NROCONTADOR();
                    //[enerca] idContador1 = infoRegistroEntrada.gettablaRegistroDeEntrada_contador();
                    lecturaTomada1 = infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA();
                    causadenolectura1 = String.format("%1$2s", infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim());
                    intentos1 = infoRegistroSalida.gettablaRegistroSalida_INTENTOS();
                    lecturaModificada11 = String.format("%1$10s", infoRegistroSalida.gettablaRegistroSalida_LECTURAMODIFICADA1().trim());
                    lecturaModificada21 = String.format("%1$10s", infoRegistroSalida.gettablaRegistroSalida_LECTURAMODIFICADA2().trim());
                    digitos1 = String.format("%1$2s", infoRegistroEntrada.gettablaRegistroDeEntrada_Digitos()); //.gettablaRegistroDeEntrada_Digitos().trim());
                    criticaPDA1 = infoRegistroSalida.gettablaRegistroSalida_LEIDO();
                    lecturaAnterior1 = infoRegistroEntrada.gettablaRegistroDeEntrada_Lecturaanterior();//.gettablaRegistroDeEntrada_LECTURAANTERIOR();

                    if (indicadorManual == 0)
                        mensajeT("Esta cuenta es un Cliente IC", msgCorto);

                    nroContador2 = "";
                    idContador2 = "";
                    lecturaTomada2 = "";
                    causadenolectura2 = "";
                    intentos2 = "";
                    lecturaModificada12 = "";
                    lecturaModificada22 = "";
                    digitos2 = "";
                    criticaPDA2 = "";
                    lecturaAnterior2 = "";
                }
            }

            apuntadortarifaCT = 0;
            apuntadortarifareactiva = 0;

            String nombreArchivoCob1 = VariablesGlobales.directorioactual + "/DATOSDESALIDA/CO_COBRO.SDA";
            String nombreArchivoCobH = VariablesGlobales.directorioactual + "/DATOSDESALIDA/CO_COBROHILOS.SDA";

            File archivoCob1 = new File(nombreArchivoCob1);
            File archivoCobH = new File(nombreArchivoCobH);

            if (procesandoenvioenHilos == 0) {
                if (archivoCobH.exists())
                    archivoCobH.delete();

                VariablesGlobales.copyFile(nombreArchivoCob1, nombreArchivoCobH, false);
            }
            envioFacturacionHilos();

            limpiarMedidores();

            variables.impresora = "   ";
            ultimaCriticaLectura = "  ";
            VariablesGlobales.ultimaNovedad = "0000";

            valor_energia = "0";
            valor_alumbradopublico = "0";

            VariablesGlobales.registroactual = alterno;
            leerInformacionUsuario(1);
            visualizarInformacionCliente(0);
            cerrarArchivosFacturacion();

            File file = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/VALIDADORIMPRESION.SDA");
            file.delete();

        } catch (Exception ex) {
            mensajeT("Error en proseguirLiquidacion " + ex.getMessage(), msgLargo);
            utils.Log(logfile, "proseguirLiquidacion(): Error: " + ex);
        }

    }

    private void envioFacturacionHilos() {

        String nuevoEnviosGprsda = VariablesGlobales.directorioactual + "/DATOSDESALIDA/ENVIOGPRS" + serialPDA + ".SDA";
        String EnviosGprsda = VariablesGlobales.directorioactual + "/DATOSDESALIDA/ENVIOSGPRS.SDA";
        // Log.e("error", "existe estearchivo " + nuevoEnviosGprsda);
        File file_nuevoEnviosGprsda = new File(nuevoEnviosGprsda);
        File file_EnviosGprsda = new File(EnviosGprsda);
        Log.e("errorse", "entra a envioFacturacionHilos " + procesandoenvioenHilos);
        try {
            if (procesandoenvioenHilos == 0) {
                if (!file_nuevoEnviosGprsda.exists()) {
                    if (file_EnviosGprsda.exists()) {
                        file_EnviosGprsda.renameTo(file_nuevoEnviosGprsda);//Ax: aqui  borra file_EnviosGprsda
                    } else {
                        return;
                    }
                } else {
                    if (file_EnviosGprsda.exists())
                        GuardarEnvioHilos();
                }

                try {
                    // Log.e("error", "existe estearchivo 2 " + nuevoEnviosGprsda);
                    if (procesandoenvioenHilos_chat) {

                        contadorChat = 0;
                        taskChat.cancel(true);
                        //matar
                    }
                    Log.e("errorse", "ENVIARFACTURACIONCOLECCION ");
                    metodo = "ENVIARFACTURACIONCOLECCION"; //Ax: esta parte de la facturacion en hilos es manejeda en una tarea paralela
//                    AsyncCallWS task = new AsyncCallWS();
//                    task.execute("");//Ax: continua aqui, en AsyncCallWS.doinback...
                    TaskHelper.execute(new AsyncCallWS(), metodo);

                    //HiloEnvioFacturacion = new Thread(new ThreadStart(EnviarFacturacionColeccion));//Thread HiloEnvioFacturacion; //HiloEnvioFacturacion.Start();
                } catch (Exception exc) {
                    Log.e("errorse", "error hilos " + exc.getMessage());
                    utils.Log(logfile, "[MenuDeLiquidacion]envioFacturacionHilos, Facturacion " + infoClienteEntrada.gettablaEntradaClientes_Cuenta() + " / " + exc.toString());
                }
            } else {
                mensajeT("El sistema esta en proceso de envio de datos por HILOS al servidor,\n No se permite realizar este proceso", msgMedio);
            }
        } catch (Exception exc1) {
            utils.Log(logfile, "[MenuDeLiquidacion]envioFacturacionHilos, Facturacion error moviendo archivos" + infoClienteEntrada.gettablaEntradaClientes_Cuenta() + " / " + exc1.toString());
        }
    }

    //creacion de copia de envio a envio hilos
    private void GuardarEnvioHilos() { //Ax: comprobar si escribe bien
        String envioGPRSerial = VariablesGlobales.directorioactual + "/DATOSDESALIDA/ENVIOGPRS" + serialPDA + ".SDA"; //Ax: si tiene datos,escribe ultima linea
        String envioGPRS = VariablesGlobales.directorioactual + "/DATOSDESALIDA/ENVIOSGPRS.SDA";//Ax. lo que ahy aqui lo escribe en el de arriba

        File file_envioGPRS = new File(envioGPRS);

        try {
            if (!utils.CrearCopiaSin(envioGPRS, envioGPRSerial)) { //sin sobreescribir
                mensajeT("No se crea copia de envioGPRSerial", msgCorto);//To do trow
            }
            file_envioGPRS.delete();
        } catch (Exception e) {
            utils.Log(logfile, "[MenuDeLiquidacion] GuardarEnvioHilos; " + e.getMessage());
            mensajeT("Error en GuardarEnvioHilos " + e.getMessage(), msgLargo);
            return;
        }
    }

    private void EnviarFacturacionColeccion() {
        Log.e("errorse", "entra a EnviarFacturacionColeccion");
        if (indicadorManual == 0) {
            if (procesandoenvioenHilos == 0) {
                procesandoenvioenHilos = 1;
                while (true) {
                    try {
                        EnviarAlServidorFacturacion();
                    } catch (Exception mens) {
                        utils.Log(logfile, "[MenuDeLiquidacion]envioFacturacionHilos, EnviarFacturacionColeccion " + infoClienteEntrada.gettablaEntradaClientes_Cuenta() + " / " + mens.toString());
                    }
                    procesandoenvioenHilos = 0;
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                    }
                    break;
                }
            }
        }
        return;// RespuestaProceso;
    }

    public void CrearArchivoEnvio(String ArchivoNombre) {
        try {
            File Archivo = new File(ArchivoNombre);

            if (!Archivo.exists()) {
                mensajeT("No existe el archivo de envio", msgMedio);
            }
            File ArchivoEnv = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/ENVIOPART" + serialPDA + ".SDA");
            if (!ArchivoEnv.exists()) {
                ArchivoEnv.createNewFile();
            }
            File CopiaGPRS = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/COPIAGPRS" + serialPDA + ".SDA");
            if (!CopiaGPRS.exists()) {
                CopiaGPRS.createNewFile();
            }

            FileReader stream3 = new FileReader(ArchivoNombre);
            BufferedReader reader = new BufferedReader(stream3);
            String linea = "";
            String dato;
            int cont = 0;
            int maximoregs = parseStringToInteger(variables.maximoregaenviar.trim());
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(ArchivoEnv));
            OutputStreamWriter osw1 = new OutputStreamWriter(new FileOutputStream(CopiaGPRS));

            while ((linea = reader.readLine()) != null) {
                dato = linea + "\r\n";
                cont++;
                if (cont <= maximoregs) {
                    osw.write(dato);
                }
                if (cont > maximoregs) {
                    osw1.write(dato);
                }
            }
            osw.flush();
            osw.close();
            osw1.flush();
            osw1.close();
            reader.close();

//            File RenameOri = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/COPIAGPRS" + serialPDA + ".SDA");
            if (Archivo.exists()) {
                Archivo.delete();
                File Original = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/ENVIOGPRS" + serialPDA + ".SDA");
                ArchivoEnv.renameTo(Original);
//                File Original = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/ENVIOGPRS" + serialPDA + ".SDA");
//                RenameOri.renameTo(Original);
            }


        } catch (Exception e) {
            Log.e("Tag", "CrearArchivoEnvio|Error: " + e);
            utils.Log(logfile, "CrearArchivoEnvio()| Error:" + e);
        }
    }

    //este es la realidad del envio de la informacion al servidor toda la facturacion
    private void EnviarAlServidorFacturacion() { //7800
        try {
            Log.e("errorse", "entra a EnviarAlServidorFacturacion");

            int conceptoactual;
            int nroconceptos;
            int enviosreales = 0;
            int abrioarchivo = 0;
            String ultimomedidordb = "";
            ErroresDeEnvio = 0;

            //nuevo procedimiento para guardar el archivo de resultados sin que se espere la guardada de foto y demas
            int EnviarXMaximo = 0;
            String ArchivoEnvio = VariablesGlobales.directorioactual + "/DATOSDESALIDA/ENVIOGPRS" + serialPDA + ".SDA";
            MisEnviosHilos.archivo_EnvioGPS = ArchivoEnvio;

            if (MisEnviosHilos.abrir_EnvioGPS(MisEnviosHilos.archivo_EnvioGPS)) {
                MisEnviosHilos.Cerrar_EnvioGPS();
            }

            if (MisEnviosHilos.total_EnvioGPS >= Integer.parseInt(variables.maximoregaenviar.trim()) && Integer.parseInt(variables.maximoregaenviar.trim()) > 0) {
                EnviarXMaximo = 1;
                File CopiaGPRS = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/COPIAGPRS" + serialPDA + ".SDA");
                if (!CopiaGPRS.exists()) {
                    CrearArchivoEnvio(ArchivoEnvio);
                    MisEnviosHilos.archivo_EnvioGPS = VariablesGlobales.directorioactual + "/DATOSDESALIDA/ENVIOGPRS" + serialPDA + ".SDA";
                }
                if (NroEnvios == 0) {
                    NroEnvios = Math.round(MisEnviosHilos.total_EnvioGPS / parseStringToInteger(variables.maximoregaenviar.trim()));
                }
            }

            //se comenta porq se pega el envio
            //if (conexionGPRSActiva == 1 || EnviarXMaximo == 1) {

            //utils.Log(logfile, "[MenuDeLiquidacion]EnviarAlServidorFacturacion();;;" + "primer proceso");

            InfoCobrosHilos.setArchivo_TablaCobrosRealizados(VariablesGlobales.directorioactual + "/DATOSDESALIDA/CO_COBROHILOS.SDA");//usa
            //validar si no existe copiar cobros a esta localizacion
            File file_CobroHilos = new File(InfoCobrosHilos.getArchivo_TablaCobrosRealizados());

            if (!file_CobroHilos.exists()) {
                utils.CrearCopia(VariablesGlobales.directorioactual + "/DATOSDESALIDA/CO_COBRO.SDA", file_CobroHilos.getAbsolutePath());
            }

            File file_cobroHilosSerial = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/COBROSSALIDA" + serialPDA + ".SDA");//Ax: nuevo nombre

            if (!file_cobroHilosSerial.exists()) {
                try {
                    file_cobroHilosSerial.createNewFile();
                } catch (Exception e) {
                    Log.e("errorse", "error cobross " + e.getMessage());
                }
            }


            if (MisEnviosHilos.abrir_EnvioGPS(MisEnviosHilos.archivo_EnvioGPS)) {
                abrioarchivo = 1;
                if (MisEnviosHilos.total_EnvioGPS > 0) {

                    try {
                        if (InfoCobrosHilos.abrir_TablaCobrosRealizados(InfoCobrosHilos.getArchivo_TablaCobrosRealizados())) {
                            abrioarchivo = 2;
                            int TotalEnviados = 0;
                            RandomAccessFile rFile;

                            while (TotalEnviados < MisEnviosHilos.total_EnvioGPS) {

                                TotalEnviados++;
                                MisEnviosHilos.lectura_EnvioGPS(TotalEnviados);

                                if (!MisEnviosHilos.getEnvioGPS_IDCONTADOR().trim().equals("")) {
                                    enviosreales++;
                                    Log.e("INFO", "cuenta: " + MisEnviosHilos.getEnvioGPS_CUENTA().trim());

                                    if (!ultimomedidordb.equals(MisEnviosHilos.getEnvioGPS_CUENTA().trim())) { //llenar la coleccion de conceptos cobrados
                                        conceptoactual = parseStringToInteger(MisEnviosHilos.getEnvioGPS_PRIMERCONCEPTO());
                                        nroconceptos = parseStringToInteger(MisEnviosHilos.getEnvioGPS_NROCONCEPTOS().trim());
                                        variables.cuentamalajustada = 0;

                                        if (!variables.tipoDeRuta.equals("L")) { ///enerca
                                            //---------------- Ax: lee y saca de cobros a archivo 'desde' conceptoactual-->'hasta' nroconceptos
                                            int concact = conceptoactual;
                                            int nrocon = nroconceptos + concact;
                                            String strLine;
                                            int cont = 1;

                                            if (nroconceptos > 0) {

                            /*FileInputStream fstream = new FileInputStream(InfoCobrosHilos.getArchivo_TablaCobrosRealizados());
                            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
                            rFile = new RandomAccessFile(file_cobroHilosSerial, "rw");

                            while ((strLine = br.readLine()) != null) {
                                if (cont >= concact) {
                                    if (concact >= nrocon) break;
                                    strLine = strLine + "\r\n";
                                    rFile.writeBytes(strLine);
                                    concact++;
                                }
                                cont++;
                            }
                            rFile.close();*/

                                                rFile = new RandomAccessFile(file_cobroHilosSerial, "rw");
                                                for (int z = concact; z < nrocon; z++) {
                                                    Log.e("INFO", "Cobro Nro: " + z);
                                                    InfoCobrosHilos.lectura_TablaCobrosRealizados(z);
                                                    strLine = "";
                                                    strLine = InfoCobrosHilos.gettablaCobrosRealizados_NROCUENTACLIENTE() + ";" +
                                                            InfoCobrosHilos.gettablaCobrosRealizados_CONCEPTODECOBRO() + ";" +
                                                            InfoCobrosHilos.gettablaCobrosRealizados_INDICADORACTIVIDAD() + ";" +
                                                            InfoCobrosHilos.gettablaCobrosRealizados_VALOR() + ";" +
                                                            InfoCobrosHilos.gettablaCobrosRealizados_SALDOPENDIENTE() + ";" +
                                                            InfoCobrosHilos.gettablaCobrosRealizados_CUOTASPENDIENTES() + ";" +
                                                            InfoCobrosHilos.gettablaCobrosRealizados_IDCONCEPTO() + ";" +
                                                            InfoCobrosHilos.gettablaCobrosRealizados_IDCONVENIO() + ";" +
                                                            InfoCobrosHilos.gettablaCobrosRealizados_PRIMERCONVENIO() + ";" +
                                                            InfoCobrosHilos.gettablaCobrosRealizados_NROCONVENIOS() + ";" +
                                                            InfoCobrosHilos.gettablaCobrosRealizados_PORCENTAJE() + ";" +
                                                            InfoCobrosHilos.gettablaCobrosRealizados_RANGOMINIMO() + ";" +
                                                            InfoCobrosHilos.gettablaCobrosRealizados_RANGOMAXIMO() + ";" +
                                                            InfoCobrosHilos.gettablaCobrosRealizados_PERIODOFACTURADO() + ";" + "\r\n";
                                                    rFile.seek(rFile.length());
                                                    rFile.writeBytes(strLine);
                                                    // Log.e("errorse","linea " +strLine);

                                                }
                                                rFile.close();

                                            }
                                        }
                                        ultimomedidordb = MisEnviosHilos.getEnvioGPS_CUENTA().trim();
                                    }
                                }
                                //fin de llenado de conceptos
                            }

                            if (enviosreales > 0) {//---------------------------------------------------------------------------------------------------- A
                                //nuevo proceso de enviar todos los datos del arreglo

                                if (TotalEnviados >= MisEnviosHilos.total_EnvioGPS) {
                                    MisEnviosHilos.Cerrar_EnvioGPS();
                                    InfoCobrosHilos.Cerrar_TablaCobrosRealizados();
                                    abrioarchivo = 0;
                                }

                                try {//----------------------------------------------------------------------------------------------------------------------------------------
                                    File nombreZipPorCrear;
                                    nombreZipPorCrear = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/" + "Tpl" + (Municipio + Seccion + Division) + ".zip");

                                    String ciclo0 = String.format("%1$4s", Ciclo).replace(" ", "0");
                                    ArrayList filestoZip = new ArrayList();

                                    filestoZip.add(new File(MisEnviosHilos.archivo_EnvioGPS));
                                    filestoZip.add(new File(file_cobroHilosSerial.getAbsolutePath()));

                                    /*if (variables.tipoDeRuta.equals("L")) { //Archivos de facturacion, si --> comprimir cobros

                                        filestoZip.add(new File(MisEnviosHilos.archivo_EnvioGPS));
                                    } else {
                                        filestoZip.add(new File(MisEnviosHilos.archivo_EnvioGPS));
                                        filestoZip.add(new File(file_cobroHilosSerial.getAbsolutePath()));
                                    }*/

                                    String msgTemp = "";

                                    //  for (int i = 0; i < 4; i++) {// 4 intentos de envio

                                    //Ax: rutaAGrabarTraer = Esta ruta se saca del archivo ArchivosCarga.cfi o ArchivosDesCarga.cfi  son iguales +  + @"CIC" + ciclo
                                    String msg = EnviarArchivo((rutaAdministrador + "CIC" + ciclo0), nombreZipPorCrear, filestoZip, trama, true);//to do: traer la trama desde inicio

                                    File f = new File(MisEnviosHilos.archivo_EnvioGPS);
                                    if (!msg.equals("ok")) { //Si falló
                                        File CopiaFile = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/COPIAGPRS" + serialPDA + ".SDA");
                                        if (CopiaFile.exists()) {
                                            File Original = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/ENVIOGPRS" + serialPDA + ".SDA");
                                            FileReader stream3 = new FileReader(VariablesGlobales.directorioactual + "/DATOSDESALIDA/COPIAGPRS" + serialPDA + ".SDA");
                                            BufferedReader reader = new BufferedReader(stream3);
                                            String linea = "";
                                            String dato;
                                            int cont = 0;
                                            RandomAccessFile writer = new RandomAccessFile(Original, "rw");//1117
                                            while ((linea = reader.readLine()) != null) {
                                                dato = linea + "\r\n";
                                                writer.seek(writer.length());
                                                writer.writeBytes(dato);
                                            }
                                            writer.close();
                                            reader.close();
                                            CopiaFile.delete();
                                        }
                                        NroEnvios = 0;

                                        if (!msgTemp.equals(msg)) {
                                            utils.Log(logfile, "[MenuDeLiquidacion]EnviarArchivo() msg :" + msg);
                                        }
                                        msgTemp = msg;

                                    } else { //Envio Exitoso!

                                        GuardarBackupEnvio();

                                        if (f.exists()) f.delete();
                                        File RenameOri = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/COPIAGPRS" + serialPDA + ".SDA");
                                        if (RenameOri.exists() && !f.exists()) {
                                            File Original = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/ENVIOGPRS" + serialPDA + ".SDA");
                                            RenameOri.renameTo(Original);
                                        }

                                        // estadoRespuesta("envio ok " + infoClienteEntrada.gettablaEntradaClientes_Cuenta() + " / " + "En." + (MisEnviosHilos.total_EnvioGPS + "") + "\n" + msg);

                                    }
                                    //  }

                                    if (nombreZipPorCrear.exists()) {
                                        nombreZipPorCrear.delete();
                                    }

                                    if (file_cobroHilosSerial != null && file_cobroHilosSerial.exists()) { //Ax: Según victor puedo borrar este archivo
                                        file_cobroHilosSerial.delete();
                                    }

                                    vecesRea++;
                                    if (vecesRea <= NroEnvios) {
                                        EnviarAlServidorFacturacion();
                                    }

                                } catch (Exception ex) {
                                    utils.Log(logfile, "[MenuDeLiquidacion]EnviarAlServidorFacturacion();" + "_" + infoClienteEntrada.gettablaEntradaClientes_Cuenta() + "/" + ex.getMessage());
                                }
                            }//------------------------------------------------------------------------------------------------------------------- A
                        }
                    } catch (Exception ex) {
                        utils.Log(logfile, "[MenuDeLiquidacion]EnviarAlServidorFacturacion();" + infoClienteEntrada.gettablaEntradaClientes_Cuenta() + " / " + ex.getMessage());
                    }
                }//Fin ...hay registros para enviar al servidor

            } //Fin ...si pudo abrir el archivo y realizo el envio de registros

            //  }//esta activa la coleccion para enviar datos
            if (abrioarchivo == 2) {
                MisEnviosHilos.Cerrar_EnvioGPS();
                InfoCobrosHilos.Cerrar_TablaCobrosRealizados();

            } else if (abrioarchivo == 1) {
                MisEnviosHilos.Cerrar_EnvioGPS();
            }
            procesandoenvioenHilos = 0;
            vecesRea = 0;
            NroEnvios = 0;
            return;

        } catch (Exception ex) {
            Log.e("Tag", "EnviarAlServidorFacturacion|Error: " + ex.getMessage());
            utils.Log(logfile, "EnviarAlServidorFacturacion()| Error:" + ex.getMessage());
        }
    }

    /**
     * Comprime,y envia al servidor archivos y manda descomprimir, devuelve mensaje
     *
     * @param rutaAGrabarTraer  : se saca del archivo ArchivosCarga.cfi o ArchivosDesCarga.cfi  + @"CIC" + ciclo
     * @param nombreZipPorCrear : nombre del zip aun no creado para enviar
     * @param ArchivosAEnviar   : Array de la ruta full de los archivos por comprimir
     * @param trama             : usada en clase de envíos al sever
     * @param esEnvioCobro      : true define si son archivos de 'envios' o 'cobro', flase: los demas
     * @return
     */
    public String EnviarArchivo(String rutaAGrabarTraer, File nombreZipPorCrear, ArrayList ArchivosAEnviar, int trama, boolean esEnvioCobro) { //7800
        String respuestaWeb;

        //[!] Ax: comprimir los archivos -------------------------------------------------------------------------------
        respuestaWeb = utils.CreaZip(nombreZipPorCrear.getAbsolutePath(), ArchivosAEnviar);

        if (!respuestaWeb.contains("✓")) return (respuestaWeb);

        if (nombreZipPorCrear.length() <= 0) return "Error comprimiendo archivo";

        try {
            WSSoap wsoaps = new WSSoap(URL, paginaWs);
            String hash = utils.Md5Hash(nombreZipPorCrear.getAbsolutePath());

            try {
                //[!] Ax: verifica Si existe Directorio Archivo en server ------------------------------------------------
                respuestaWeb = wsoaps.VerificarSiexisteDirectorioArchivo("VerificarSiexiste_Directorio_Archivo", rutaAGrabarTraer, nombreZipPorCrear.getName());
                // Log.e("respu", rutaAGrabarTraer + " " + nombreZipPorCrear);
                if (!respuestaWeb.equals("true"))
                    return "Sin Conexion o Sin directorio de trabajo:\n Ruta a traer| " + rutaAGrabarTraer + " " + respuestaWeb;

            } catch (Exception ex) {
                return "Sin Conexion o Sin directorio de trabajo.:\n Ruta a traer| " + rutaAGrabarTraer + " " + respuestaWeb;
            }
            //[!] Ax: envia archivo al server ----------------------------------------------------------------------------
            respuestaWeb = wsoaps.TerminalToServerReceive("Terminal_ToServerReceive", rutaAGrabarTraer, parseStringToInteger(esComprimido), nombreZipPorCrear.getAbsolutePath(), trama);

            if (!(respuestaWeb.equals("4") || respuestaWeb.equals("1")))
                return "Error Enviar Archivo Terminal_ToServerReceive: " + respuestaWeb;

            String serial = String.format("%1$15s", serialPDA).replace(" ", "0");//Serial de 15

            //[!] Ax: envia orden de descomprimir en el server
            metodo = "Descomprime";
            respuestaWeb = wsoaps.Descomprimir(metodo, rutaAGrabarTraer + "\\" + "DESCARGASENVIOGPRS", rutaAGrabarTraer + "\\" + nombreZipPorCrear.getName(), hash, serialPDA);
            //Log.e("error"," descomprime "+respuestaWeb);

            if (!respuestaWeb.equals("true")) {
                return "Error Directorio Donde Descomprimir: " + respuestaWeb;
            }

            respuestaWeb = (rutaAGrabarTraer.substring(rutaAGrabarTraer.lastIndexOf("\\") + 1, rutaAGrabarTraer.length())) + ""; // Se convierte en :  @"CIC" + ciclo + \DescargasEnvioGPRS
            rutaAGrabarTraer = rutaAGrabarTraer.replace(respuestaWeb, "");//Ax: Se convierte en : c:\\enrutadorsimfa  (ojo lowercase)

            respuestaWeb = wsoaps.InsertarDatosTablasTemporales("InsertarDatosTemporales", rutaAGrabarTraer, respuestaWeb + "\\DESCARGASENVIOGPRS", serialPDA, "1");

            String cadena = rutaAGrabarTraer + "," + respuestaWeb + "\\DESCARGASENVIOGPRS" + "," + serialPDA + "," + "1";
            String resp = respuestaWS(respuestaWeb, cadena); //Se envia a evaluacion el mensaje del ws
            if (!resp.trim().equals("OK")) {
                return resp;
            }

        } catch (Exception ex) {
            utils.Log(logfile, "[MenuDeLiquidacion]EnviarArchivo().;" + ex.getMessage());
            return "Problemas EnviarArchivo()" + ex.getMessage();
        }
        banderaWsOcupado = false;
        return "ok";
    }

    private String respuestaWS(String response, String cadena) {

        try {
            if (!response.contains("|")) {
                utils.Log(logfile, "[MenuDeLiquidacion]respuestaWS().;" + response + " |cadena:" + cadena);
                return response;
                //mensajeT("Error al insertar: \n" + response, msgLargo);
            }
            //response = ENVIOGPRS:1,errores:0|COBROSSALIDA:0,errores:0|NOVEDADES:0,errores:0|AFOROS:0,errores:0|AUDITORIASUPERVISOR:0,errores:0|CERTIFICADOS:0,errores:0|CUENTASGPS:0,errores:0
            String mensaje = "";
            String[] rw = response.split("\\|");

            for (String st : rw) {

                String[] ss = st.split(",");
                String xx = ss[1].substring(ss[1].indexOf(":") + 1);
                if (!xx.equals("0")) {
                    mensaje += st + " - ";
                }
            }
            if (!mensaje.equals("")) {
                utils.Log(logfile, "[MenuDeLiquidacion]respuestaWS().;" + mensaje);
                return "[MenuDeLiquidacion]respuestaWS().;" + mensaje;
            }
            return "OK";
        } catch (Exception ex) {
            utils.Log(logfile, "[MenuDeLiquidacion]respuestaWS();" + "Error:" + ex.getMessage());
            return "[MenuDeLiquidacion]respuestaWS();" + "Error:" + ex.getMessage();
        }
    }

    //nuevo modelo de envio por arreglos directos a la base de datos. hacer un backup del archivo enviado
    private void GuardarBackupEnvio() {

        String nuevoEnvioGPRS = VariablesGlobales.directorioactual + "/DATOSDESALIDA/ENVIOGPRS" + serialPDA + ".SDA";
        String backupEnvioGPRS = VariablesGlobales.directorioactual + "/DATOSDESALIDA/BKENVIOSGPRS.SDA";

        utils.CrearCopiaSin(nuevoEnvioGPRS, backupEnvioGPRS); //Crea una copis sin sobreescribir

        File file_backupEnvioGPRS_2 = new File(VariablesGlobales.directorioBackUp + "BKENVIOSGPRS.SDA");

        File file_backupEnvioGPRS = new File(backupEnvioGPRS);

        if (file_backupEnvioGPRS_2.exists()) {
            file_backupEnvioGPRS_2.delete();
        }

        // file_backupEnvioGPRS.renameTo(file_backupEnvioGPRS_2);//Ax: aqui si se desaparece el primero
        utils.CrearCopia(file_backupEnvioGPRS.getAbsolutePath(), file_backupEnvioGPRS_2.getAbsolutePath());

        File fco_cobro_IPSM = new File(VariablesGlobales.directorioBackUp + "CO_COBRO.SDA");

        if (fco_cobro_IPSM.exists())
            fco_cobro_IPSM.delete();

        File fco_cobro = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/CO_COBRO.SDA");
        utils.CrearCopia(fco_cobro.getAbsolutePath(), fco_cobro_IPSM.getAbsolutePath());

        File fNovedades_IPSM = new File(VariablesGlobales.directorioBackUp + "NOVEDADES.SDA");

        if (fNovedades_IPSM.exists())
            fNovedades_IPSM.delete();

        File fNovedades = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/NOVEDADES.SDA");

        if (fNovedades.exists())
            utils.CrearCopia(fNovedades.getAbsolutePath(), fNovedades_IPSM.getAbsolutePath());

        File fAforos_IPSM = new File(VariablesGlobales.directorioBackUp + "AFOROS.SDA");

        if (fAforos_IPSM.exists())
            fAforos_IPSM.delete();

        File fAforos = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/AFOROS.SDA");

        if (fAforos.exists())
            utils.CrearCopia(fAforos.getAbsolutePath(), fAforos_IPSM.getAbsolutePath());
    }

    private void procesoDeLiquidacionEImpresion(int procesoReimpresion) {
        // Log.e("error", "entra a proceso de liq ");
        int variarConsumo = 0;
        //String sMenAnt = txtInformeEscrito.getText().toString();
        //txtInformeEscrito.setText("Ejecutando Liquidacion");
        tomarFechaSistema("", "");
        infoClienteSalida.settablaClienteSalida_FECHAIMPRESION(variables.amd);
        infoClienteSalida.settablaClienteSalida_HORAIMPRESION(variables.hm);
        infoClienteSalida.escribir_TablaClienteSalida(variables.clienteactual);

        if (infoClienteSalida.gettablaClienteSalida_INDFACTURACION().equals("N")) {
            tomarFechaSistema(infoClienteSalida.gettablaClienteSalida_FECHAIMPRESION(), infoClienteSalida.gettablaClienteSalida_HORAIMPRESION());
            infoClienteSalida.settablaClienteSalida_FECHAIMPRESION(variables.amd);
            infoClienteSalida.settablaClienteSalida_HORAIMPRESION(variables.hm);

            if (indicadorManual == 0)
                mensajeT("Alerta para Usuario" + "\nEl Cliente Actual\n" + "No se le Entrega Factura en Sitio\n", msgLargo);

            // txtInformeEscrito.setText("");
            return;
        }

        if (infoClienteSalida.gettablaClienteSalida_INDFACTURACION().equals(" ")) {

            if (indicadorManual == 0)
                mensajeT("Alerta de Liquidacion \n" + "La cuenta Actual\n" + "No tiene Lectura para procesar\n" + infoClienteEntrada.gettablaEntradaClientes_Cuenta(), msgLargo);

            // txtInformeEscrito.setText(sMenAnt);
            return;
        }

        imprimirFactura = true;

        if (infoClienteSalida.gettablaClienteSalida_INDFACTURACION().equals("L")) {

            if (Integer.parseInt(infoClienteEntrada.gettablaEntradaClientes_nrocobros().trim()) == 0) {

                if (indicadorManual == 0) {
                    mensajeT("Alerta en Liquidacion Cuenta\n" + "Cliente no se factura\n" + "por falta de conceptos de cobro\n", msgLargo);
                }
                escribirTablasSalida();
                return;
            }
            long resultado = validarEstadoCliente(variables.clienteactual, false, "L");

            if (resultado == 0) {
                infoClienteSalida.lectura_TablaClienteSalida(variables.clienteactual);

                if (MODO.equals("")) {
                }

                infoClienteSalida.lectura_TablaClienteSalida(variables.clienteactual);


                variarConsumo = 1;
                infoClienteSalida.settablaClienteSalida_fechavence(infoClienteEntrada.gettablaEntradaClientes_fechavence()); //[enerca] se añade
                infoClienteSalida.settablaClienteSalida_fechacorte(infoClienteEntrada.gettablaEntradaClientes_fechacorte()); //[enerca] se añade
                Log.e("error", "ind fact 5 " + "ejecutarLiquidacionCuenta()");

                variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";ejecutarLiquidacionCuenta ;" + getPhoneDate() + "-" + getPhoneHour();
                escribeResumenTiempo(variableResumenLiquidacion + " " + " Entra a ejecutarLiquidacionCuenta()");
                if (ejecutarLiquidacionCuenta() > 0) {
                    Log.e("error", "ind fact 5");
                    infoClienteSalida.settablaClienteSalida_INDFACTURACION("K");
                }
                Log.e("error", "verifica victor 2... " + VariablesGlobales.registroactual);
                infoRegistroSalida.lectura_TablaRegistroSalida(VariablesGlobales.registroactual);
                infoClienteSalida.escribir_TablaClienteSalida(variables.clienteactual);
            }
            infoClienteSalida.escribir_TablaClienteSalida(variables.clienteactual);
        }

        if (VariablesGlobales.habilitadaimpresora == 0 && indicadorManual == 0) {
            mensajeT("Alerta de Impresora\n" + "La impresora se encuentra\n" + "Deshabilitada", msgLargo);
            return;//descomentar
        }

        if (infoClienteSalida.gettablaClienteSalida_INDFACTURACION().equals("F")) {

            if (procesoReimpresion == 3) { //Ax: 3 es porque viene del menu desplegable (reimprimir_factura) y es para que no entre a éste Alert  Dialog de abajo

                tomarFechaSistema(infoClienteSalida.gettablaClienteSalida_FECHAIMPRESION(), infoClienteSalida.gettablaClienteSalida_HORAIMPRESION());
                infoClienteSalida.settablaClienteSalida_FECHAIMPRESION(variables.amd);
                infoClienteSalida.settablaClienteSalida_HORAIMPRESION(variables.hm);

                if (generarArchivoTextoImpresion() > 0) {

                    // txtInformeEscrito.setText("Generando la impresion");
                    infoClienteSalida.lectura_TablaClienteSalida(variables.clienteactual);
                   /* if (Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO().trim()) > 5000
                            && Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO().trim()) > Double
                            .parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediocliente().trim()) * 3)
                        imprimirFactura = false;*/

                    // NUEVO PROCESO SI DESEA IMPRIMIRLA YA QUE ES UNA POSTAL PRO DEBE DE ESTAR LIQUIDADA ASI QUE HACER AQUI LA PREGUNTA
                    if (infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("P")) {
                        mensajeT("Esta es una cuenta Postal Se imprimira", msgMedio);
                        imprimirFactura = true;
                    }
                    // Log.e("error", "si imprime 1");
                    if (imprimirFactura == true) {
                        // Log.e("errorf","factura3 "+procesoImpresionFactura(0));
                        // Toast.makeText(getApplicationContext(), "fac 3", Toast.LENGTH_SHORT).show();

                        if (procesoImpresionFactura(0) > 0) {

                            int numeroImpresiones = 0;
                            if (!infoClienteSalida.gettablaClienteSalida_NROIMPRESIONES().trim().equals("")) {
                                numeroImpresiones = parseStringToInteger(infoClienteSalida.gettablaClienteSalida_NROIMPRESIONES().trim());
                                infoClienteSalida.settablaClienteSalida_NROIMPRESIONES("0");
                            }

                            if (numeroImpresiones < 9) {
                                ++numeroImpresiones;
                                infoClienteSalida.settablaClienteSalida_NROIMPRESIONES("" + (numeroImpresiones));
                            }
                            tomarFechaSistema(infoClienteSalida.gettablaClienteSalida_FECHAIMPRESION(),
                                    infoClienteSalida.gettablaClienteSalida_HORAIMPRESION());
                            infoClienteSalida.settablaClienteSalida_FECHAIMPRESION(variables.amd);
                            infoClienteSalida.settablaClienteSalida_HORAIMPRESION(variables.hm);
                        }
                    } else
                        mensajeT("No Se imprime factura por consumo excesivo: " + infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO(), msgLargo);

                    infoClienteSalida.escribir_TablaClienteSalida(variables.clienteactual);

                    variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";procesoImpresionFactura ;" + getPhoneDate() + "-" + getPhoneHour();
                    escribeResumenTiempo(variableResumenLiquidacion + " " + " Fin a procesoImpresionFactura()");

                    apuntadortarifaactiva = 0;
                    apuntadortarifareactiva = 0;
                    apuntadortarifaCT = 0;
                }
            } else {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Mensaje de Reimpresion");
                builder.setMessage("Cliente ya se encuentra facturado.\nDesea Reimprimir la Factura?");

                builder.setIcon(R.drawable.ic_launcher1);

                builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        tomarFechaSistema(infoClienteSalida.gettablaClienteSalida_FECHAIMPRESION(),
                                infoClienteSalida.gettablaClienteSalida_HORAIMPRESION());
                        infoClienteSalida.settablaClienteSalida_FECHAIMPRESION(variables.amd);
                        infoClienteSalida.settablaClienteSalida_HORAIMPRESION(variables.hm);
                        variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";Inicia Gen. Arc Fac;"
                                + getPhoneDate() + " " + getPhoneHour();
                        escribeResumenTiempo(variableResumenLiquidacion);
                        variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";generarArchivoTextoImpresion ;" + getPhoneDate() + "-" + getPhoneHour();
                        escribeResumenTiempo(variableResumenLiquidacion + " " + " Entra a generarArchivoTextoImpresion()");

                        if (generarArchivoTextoImpresion() > 0) {

                            // txtInformeEscrito.setText("Generando la impresion");
                            infoClienteSalida.lectura_TablaClienteSalida(variables.clienteactual);
                            if (Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO().trim()) > 5000
                                    && Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO().trim()) > Double
                                    .parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediocliente().trim()) * 3)
                                imprimirFactura = false;
                            // else
                            // NUEVO PROCESO SI DESEA IMPRIMIRLA YA QUE ES UNA
                            // POSTAL PRO DEBE DE ESTAR LIQUIDADA ASI QUE HACER AQUI
                            // LA PREGUNTA
                            if (infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("P")) {
                                mensajeT("Esta es una cuenta Postal Se imprimira", msgMedio);
                                imprimirFactura = true;
                            }

                            if (imprimirFactura == true) {
                                //  Log.e("errorf","factura4 "+procesoImpresionFactura(0));
                                //   Toast.makeText(getApplicationContext(), "fac 4", Toast.LENGTH_SHORT).show();
                                if (procesoImpresionFactura(0) > 0) {

                                    infoClienteSalida.settablaClienteSalida_INDFACTURACION("F");
                                    int numeroImpresiones = 0;
                                    if (!infoClienteSalida.gettablaClienteSalida_NROIMPRESIONES().trim().equals(""))
                                        numeroImpresiones = parseStringToInteger(infoClienteSalida.gettablaClienteSalida_NROIMPRESIONES().trim());

                                    if (numeroImpresiones < 9) {
                                        ++numeroImpresiones;
                                        infoClienteSalida.settablaClienteSalida_NROIMPRESIONES("" + (numeroImpresiones));
                                    }
                                    tomarFechaSistema(infoClienteSalida.gettablaClienteSalida_FECHAIMPRESION(),
                                            infoClienteSalida.gettablaClienteSalida_HORAIMPRESION());
                                    infoClienteSalida.settablaClienteSalida_FECHAIMPRESION(variables.amd);
                                    infoClienteSalida.settablaClienteSalida_HORAIMPRESION(variables.hm);
                                }
                            } else
                                mensajeT("No Se imprime factura por consumo excesivo: " + infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO(), msgLargo);

                            infoClienteSalida.escribir_TablaClienteSalida(variables.clienteactual);

                            apuntadortarifaactiva = 0;
                            apuntadortarifareactiva = 0;
                            apuntadortarifaCT = 0;
                        }
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.show();
            }
        } else if (infoClienteSalida.gettablaClienteSalida_INDFACTURACION().equals("K")) {

            if (variables.impresionenlote > 0) {
                return;
            }


            //Ax: nuevo 'if' tomado de enerca
            String temp = infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO().trim();
            double temp1 = 0;
            String MensajeAlerta = "";

            if (!temp.equals("")) {
                temp1 = parseStringToDouble(temp);
            }

            if ((temp1) - parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediocliente()) > 30 && parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediocliente()) > 0 && infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim().equals("") && apuntadortarifareactiva == 0) {

                double consumoPromedio = parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediocliente()) * 8;

                if (parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO()) > consumoPromedio) {

                    if (indicadorManual == 0) {
                        temp1 = 0;
                        temp = infoRegistroEntrada.gettablaRegistroDeEntrada_Lecturaanterior().trim();
                        if (!temp.equals("")) {
                            temp1 = parseStringToDouble(temp);
                        }

                        double temp2 = 0;
                        temp = infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA().trim();
                        if (!temp.equals("")) {
                            temp2 = parseStringToDouble(temp);
                        }

                        if ((temp1) < (temp2))
                            MensajeAlerta = "Problemas con el Consumo Excesivo \n" + "Anomalia entregada " + medidorSalida.gettablaContadorSalida_ESTADOCRITICA() + "\n"
                                    + infoRegistroEntrada.gettablaRegistroDeEntrada_Lecturaanterior() + "--" + infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA()
                                    + "\n para un consumo de:" + infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO();
                        else {
                            double resta = parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Lecturaanterior().trim()) - parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Lecturaanterior().trim());
                            MensajeAlerta = "Problemas con Lectura Inferior a la Actual \n" + "Anomalia Entregada " + medidorSalida.gettablaContadorSalida_ESTADOCRITICA() + "\n"
                                    + infoRegistroEntrada.gettablaRegistroDeEntrada_Lecturaanterior() + "--" + infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA() + "=" +
                                    String.format("%1$-10s", resta)
                                    + "\n Liquidara promedio de:" + infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO();
                        }
                        // Log.e("error", "foto y novedad 1");
                        //se comenta   fotoynovedad = true; //indica si foto y novedad se deben hacer en  procesarLectura3();
                    }
                    apuntadortarifaactiva = 0;
                    apuntadortarifareactiva = 0;

                    if (indicadorManual == 0 && !infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().equals("L")) {

                        MostrarAlertDialog("ALERTA DE IMPRESION", "Desea Imprimir la Factura? con:\n" + MensajeAlerta, "procesoDeLiquidacionEImpresion20");
                        ventanaConsumo = true;// Ax: indica que debo esperar respuesta para ir a procesarLecuras3
                        return;
                    }
                }
            }

            if (parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediocliente()) == 0) {

                if (parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumo1()) > 0 ||
                        parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumo2()) > 0 ||
                        parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumo3()) > 0 ||
                        parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumo4()) > 0 ||
                        parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumo5()) > 0 ||
                        parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumo6()) > 0) {

                    if (infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO().trim().equals(""))
                        infoClienteSalida.settablaClienteSalida_CONSUMOFACTURADO("0");

                    if ((parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO().trim()) - parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediocliente())) > 500 && infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim().equals("")) {

                        if (indicadorManual == 0) {
                            MensajeAlerta = "Verificar Cliente por Promedios en cero\n" + "Evaluacion de Anomalia " + medidorSalida.gettablaContadorSalida_ESTADOCRITICA() + "\n"
                                    + infoRegistroEntrada.gettablaRegistroDeEntrada_Lecturaanterior() + "--" + infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA()
                                    + "\n su Consumo es mayor a 30=> :" + infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO();

                            mensajeT(MensajeAlerta, msgLargo);


                            // Log.e("error", "foto y novedad 2");
                            //secomenta fotoynoeda  fotoynovedad = true;
                        }
                        apuntadortarifaactiva = 0;
                        apuntadortarifareactiva = 0;

                        if (indicadorManual == 0 && !infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().equals("L")) {

                            MostrarAlertDialog("ALERTA DE IMPRESION", "Desea Imprimir la Factura? con:\n" + MensajeAlerta, "procesoDeLiquidacionEImpresion20");
                            ventanaConsumo = true;// Ax: indica que debo esperar respuesta para ir a procesarLecuras3
                            return;
                        }
                    }
                } else {
                    if (infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO().trim().equals(""))
                        infoClienteSalida.settablaClienteSalida_CONSUMOFACTURADO("0");

                    if (indicadorManual == 0) {
                        if ((parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO().trim()) - parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediocliente())) > 250 && infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim().equals("")) {

                            if (!infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().equals("L")) {
                                MensajeAlerta = "Verificar Factura por \ncliente con Historial \nde consumos en Cero\n" + medidorSalida.gettablaContadorSalida_ESTADOCRITICA() + "\n"
                                        + infoRegistroEntrada.gettablaRegistroDeEntrada_Lecturaanterior() + "--" + infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA()
                                        + "\n Consumo mayor a 250=> :" + infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO();
                                mensajeT(MensajeAlerta, msgLargo);
                            }


                            // Log.e("error", "foto y novedad 3");
                            //secomenta fotoynoeda fotoynovedad = true; // ejecutarProcesoDeFoto();    adicionarNovedad(1);
                        }
                    }
                }
            }

            tomarFechaSistema(infoClienteSalida.gettablaClienteSalida_FECHAIMPRESION(), infoClienteSalida.gettablaClienteSalida_HORAIMPRESION());
            infoClienteSalida.settablaClienteSalida_FECHAIMPRESION(variables.amd);
            infoClienteSalida.settablaClienteSalida_HORAIMPRESION(variables.hm);
            //  txtInformeEscrito.setText("Imprimiendo Factura...");

            if (generarArchivoTextoImpresion() > 0) {
                if (indicadorManual == 0)
                    ProcesoEnviaAImpresion(imprimirFactura);
                infoClienteSalida.settablaClienteSalida_INDFACTURACION("F");
                //infoClienteSalida.escribir_TablaClienteSalida(variables.clienteactual);
            }
            infoClienteSalida.escribir_TablaClienteSalida(variables.clienteactual);
        }
        // txtInformeEscrito.setText("");
    }

    private void procesoDeLiquidacionEImpresion2() {

        tomarFechaSistema(infoClienteSalida.gettablaClienteSalida_FECHAIMPRESION(), infoClienteSalida.gettablaClienteSalida_HORAIMPRESION());
        infoClienteSalida.settablaClienteSalida_FECHAIMPRESION(variables.amd);
        infoClienteSalida.settablaClienteSalida_HORAIMPRESION(variables.hm);
        // txtInformeEscrito.setText("Imprimiendo Factura...");
        //tal vez enerca doble medidor escribirTablasSalida();

        variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";generarArchivoTextoImpresion ;" + getPhoneDate() + "-" + getPhoneHour();
        escribeResumenTiempo(variableResumenLiquidacion + " " + " Entra a procesoDeLiquidacionEImpresion2()");
        if (generarArchivoTextoImpresion() > 0) {
            ProcesoEnviaAImpresion(imprimirFactura);
        }
        procesarLectura3(); //El proceso procesoDeLiquidacionEImpresion originalmente era llamado por procesarLecturas2, como se interrumpio, pasa a procesarLecturas3
    }

    private void procesoDeLiquidacionEImpresion3() { //solo para reimpresion

        reimpresion_factura = false;
        tomarFechaSistema(infoClienteSalida.gettablaClienteSalida_FECHAIMPRESION(), infoClienteSalida.gettablaClienteSalida_HORAIMPRESION());
        infoClienteSalida.settablaClienteSalida_FECHAIMPRESION(variables.amd);
        infoClienteSalida.settablaClienteSalida_HORAIMPRESION(variables.hm);
        //  txtInformeEscrito.setText("Imprimiendo Factura...");

        if (generarArchivoTextoImpresion() > 0) {
            ProcesoEnviaAImpresion(imprimirFactura);
        }

        // IDENTIFICAR SI LA CUENTA ESTA EN UN ESTADO DE LECTURA // Y SE PUEDE IMPRIR Y PROCESAR PARA ENVIAR
        if (infoClienteSalida.gettablaClienteSalida_INDFACTURACION().trim().equals("L")) {
            guardarEnvioGPRS(1);// se mandaria a guardar en el archivo de envio
        }

        VariablesGlobales.registroactual = alterno;
        leerInformacionUsuario(1);
        visualizarInformacionCliente(0);
        cerrarArchivosFacturacion();
        limpiarMedidores();
    }

    //crear por aparte el proceso de imprimir realmente la factura

    private void ProcesoEnviaAImpresion(boolean Imprimir) {
        // Log.e("error2", imprimirFactura + " entra a proceso enviar a impre " + Imprimir);

        // if (Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO().trim()) > 5000 && Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO().trim()) > Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediocliente().trim()) * 3)
        //     Imprimir = false;
        // Log.e("error", "si imprime 2");
        if (imprimirFactura == Imprimir) {
            //  Log.e("errorf","factura5 "+procesoImpresionFactura(0));
            //  Toast.makeText(getApplicationContext(), "fac 5", Toast.LENGTH_SHORT).show();
            if (procesoImpresionFactura(0) == 1) {
                // Log.e("error", "ind fact 6");
                infoClienteSalida.settablaClienteSalida_INDFACTURACION("F");
                Integer NroImpresiones = 0;
                if (infoClienteSalida.gettablaClienteSalida_NROIMPRESIONES().trim().length() == 0) {
                    NroImpresiones = 0;
                    infoClienteSalida.settablaClienteSalida_NROIMPRESIONES("0");
                } else {
                    NroImpresiones = parseStringToInteger(infoClienteSalida.gettablaClienteSalida_NROIMPRESIONES().trim());
                }

                if (NroImpresiones < 9)//>!infoClienteSalida.gettablaClienteSalida_NROIMPRESIONES().trim().equals("9"))
                {
                    NroImpresiones++;
                    infoClienteSalida.settablaClienteSalida_NROIMPRESIONES(("" + NroImpresiones).trim());//"" + parseStringToInteger(infoClienteSalida.gettablaClienteSalida_NROIMPRESIONES()) + 1).trim());

                }
                tomarFechaSistema(infoClienteSalida.gettablaClienteSalida_FECHAIMPRESION(),
                        infoClienteSalida.gettablaClienteSalida_HORAIMPRESION());
                infoClienteSalida.settablaClienteSalida_FECHAIMPRESION(variables.amd);
                infoClienteSalida.settablaClienteSalida_HORAIMPRESION(variables.hm);
            }
        } else {
            mensajeT("No Se imprime factura por consumo excesivo: " + parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO().trim()), msgLargo);
            // Toast.makeText(getApplicationContext(), "No Se imprime factura por consumo excesivo: " + parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO().trim()), Toast.LENGTH_LONG).show();

        }
        apuntadortarifaactiva = 0;
        apuntadortarifareactiva = 0;
        apuntadortarifaCT = 0;

        infoClienteSalida.escribir_TablaClienteSalida(variables.clienteactual);
    }

    /**
     * Envía a la impresora Bluetooth el label de retención cuando la factura
     * no se imprime por consumo excesivo o crítica 40.
     * Usa el mismo canal (btPrintService.write) que procedimientoDeImpresionPagina.
     */
    boolean yaimprimioRetiene = false;
    private void imprimirLabelRetencion() {
        try {
            if(yaimprimioRetiene) return;
            if (VariablesGlobales.habilitadaimpresora == 0 || indicadorManual == 1) return;
            if (VariablesGlobales.btPrintService == null) return;
            if (VariablesGlobales.btPrintService.getState() != btPrintFile.STATE_CONNECTED) return;

            String nombre   = infoClienteEntrada.gettablaEntradaClientes_Nombre().trim();
            String direccion = infoClienteEntrada.gettablaEntradaClientes_Direccion().trim();
            String cuenta   = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim();
            String nrofactura  = infoClienteEntrada.gettablaEntradaClientes_Nrofactura() .trim();

            // Truncar para que no desborde el label
            if (nombre.length()   > 40) nombre   = nombre.substring(0, 40);
            if (direccion.length() > 40) direccion = direccion.substring(0, 40);

            //este proceso se debe quitar con el tiempo
            variables.amd = infoClienteEntrada.gettablaEntradaClientes_mes();//medidorSalida.gettablaContadorSalida_FECHALECTURA()
            Log.e("error", "colilla20 " + variables.amd);
            if (variables.amd.trim().length() == 0) {
                variables.amd = medidorSalida.gettablaContadorSalida_FECHALECTURA();
            }
            convertirFormatoDeFecha(variables.amd, variables.fechahoy, mesenformato, "AMD");

            String fecha = variables.mesenformato.replace(" ","");

            String label = "! 0 200 200 1150 1\r\n"+
                            "JOURNAL\r\n" +
                            "CONTRAST 0\r\n" +
                            "TONE 20\r\n" +
                            "SPEED 5\r\n" +
                            "POSTFEED 0\r\n" +
                            "PAGE-WIDTH 816\r\n" +
                            "BAR-NONE\r\n" +
                            "PCX 7 1 !<desvia.pcx\r\n" +
                            "T 5 1 590  10 " + cuenta + "\r\n" +
                            "T 7 0 635  55 " + nrofactura + "\r\n" +
                            "T 7 0 590  90 " + fecha + "\r\n" +
                            "T 7 0 140  165 " + nombre + "\r\n" +
                            "T 7 0 150  195 " + direccion + "\r\n" +
                            "PRINT\r\n";
            String trozo = "";
            int tamano = label.length();
            byte[] outputData;
            int i = 0;

            for (i = 0; i <= label.length(); ) {
                if (tamano >= 35)
                    trozo = label.substring(i, (i + 35));
                else
                    trozo = label.substring(i, (i + tamano));
                outputData = trozo.getBytes();

                tamano -= 35;
                i = i + 35;
                /*if (!VariablesGlobales.btPrintService.write(outputData)) {
                    return (-1);
                }*/
                VariablesGlobales.btPrintService.write(outputData);
            }
            yaimprimioRetiene = true;

        } catch (Exception e) {
            yaimprimioRetiene = false;
            utils.Log(logfile, "[MenuDeLiquidacion]imprimirLabelRetencion(): " + e.getMessage());
        }
    }

    // =====================================================================================
    // Impresion EBSA con plantilla CPCL (paquete com.gselectroCaqueta.impresion).
    // Reemplaza a generarArchivoTextoImpresion() + procedimientoUnionArchivos() cuando la
    // plantilla FORMATO_CORTA.CPCL esta instalada en DATOSDEENTRADA. Sin plantilla, la movil
    // sigue imprimiendo con el mecanismo anterior (Impresion.log + IMPRIMIR.TXT).
    // =====================================================================================

    /** El formato nuevo se activa por la sola presencia de la plantilla en DATOSDEENTRADA. */
    private boolean formatoEbsaActivo() {
        return new File(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/" + RenderFactura.PLANTILLA_CORTA).isFile();
    }

    /**
     * Escribe LBLS/<cuenta>_<anio>_<mes>.LOG con el mismo nombre que hoy, asi
     * procedimientoDeImpresionPagina(), la reimpresion y el indice L<ciclo> no cambian.
     *
     * @return 1 si el archivo quedo escrito; 0 si no (la causa queda en LOGEVENTOS.LOG)
     */
    private int generarFacturaConPlantilla() {
        try {
            // MENSAJES.TXT (DatosSoporte): mensaje de interes por CDGOMNSJE ("15087A" = municipio DANE + A/T).
            // Si el archivo no esta, el adaptador sigue sin mensaje (la plantilla omite esas lineas).
            TablaMensajes mensajes = new TablaMensajes();
            if (!mensajes.abrir_TablaMensajes(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/MENSAJES.TXT")) {
                mensajes = null;
            }
            try {
                AdaptadorMovil adaptador = new AdaptadorMovil(
                        infoClienteEntrada, infoClienteSalida, infoMedidorEntrada, infoRegistroEntrada,
                        infoRegistroSalida, infoCobrosLiquidados, descripcionConcepto, municipio, mensajes);

                String version = "Version 26.01.14.A-11 Lect.1" + infoClienteSalida.gettablaClienteSalida_LECTOR().trim()
                        + " Cont.APCSoluciones - " + (EsImpresora521 == 1 ? "ZQ-521" : "RW-420");

                DatosFactura datos = adaptador.leer(descripcionTipoLectura(), version);
                if (datos.mensaje.isEmpty() && !datos.codigoMensaje.isEmpty()) {
                    utils.Log(logfile, "[MenuDeLiquidacion]generarFacturaConPlantilla(); cuenta " + datos.cuenta
                            + " sin mensaje de interes: codigo " + datos.codigoMensaje + " no esta en MENSAJES.TXT"
                            + (mensajes == null ? " (archivo no abierto)" : ""));
                }

                RenderFactura render = new RenderFactura(
                        new File(VariablesGlobales.directorioactual + "/DATOSDEENTRADA"),
                        new File(VariablesGlobales.directorioactual + "/LBLS"));
                // Intensidad, corrimiento X/Y y modelo que guarda ModuloConfigFormatoImpresion (ValoresFormato.log).
                AjustesImpresora ajustes = AjustesImpresora.leer(
                        new File(VariablesGlobales.directorioactual + "/" + AjustesImpresora.ARCHIVO));
                RenderFactura.Salida salida = render.generar(datos, ajustes);

                if (!salida.faltantes.isEmpty()) {
                    utils.Log(logfile, "[MenuDeLiquidacion]generarFacturaConPlantilla(); cuenta " + datos.cuenta
                            + " claves sin dato en " + salida.plantilla + ": " + salida.faltantes);
                }
                if (salida.degradada) {
                    utils.Log(logfile, "[MenuDeLiquidacion]generarFacturaConPlantilla(); cuenta " + datos.cuenta
                            + " con aseo impresa con FORMATO_CORTA porque FORMATO_LARGA.CPCL no esta instalada");
                }
                registrarEnIndiceLbl(salida.archivo.getName());
                escribeResumenTiempo("Factura con plantilla " + salida.plantilla + " " + ajustes + "|" + VariablesGlobales.registroactual);
                return 1;
            } finally {
                if (mensajes != null) mensajes.Cerrar_TablaMensajes();
            }
        } catch (Exception e) {
            utils.Log(logfile, "[MenuDeLiquidacion]generarFacturaConPlantilla(); " + e.getMessage());
            return 0;
        }
    }

    /**
     * Texto de "Tipo lectura" de la factura ("Toma Exitosa", "Inmueble Sin Servicio"...).
     * Misma resolucion que hace generarTextosMedidor() para causadesc, sin el prefijo " 0: ".
     */
    private String descripcionTipoLectura() {
        String causa = infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim();
        if (causa.isEmpty() || causa.equals("0")) return "Toma Exitosa";
        if (anomaliaDeLectura.abrir_AnomaliaDeNoLectura(anomaliaDeLectura.getArchivo_AnomaliaDeNoLectura())) {
            anomaliaDeLectura.buscarbinario_AnomaliaDeNoLectura(causa);
            anomaliaDeLectura.Cerrar_AnomaliaDeNoLectura();
            if (anomaliaDeLectura.getEncontro_AnomaliaDeNoLectura() > 0) {
                return anomaliaDeLectura.getanomaliaDeNoLectura_DESCRIPCION().trim();
            }
        }
        return "Causal " + causa;
    }

    /** Mismo registro en LBLS/L<ciclo><mun><sec><div> que hace procedimientoUnionArchivos(). */
    private void registrarEnIndiceLbl(String nombreLog) throws IOException {
        File indice = new File(VariablesGlobales.directorioactual + "/LBLS/" + "L" + Ciclo + Municipio + Seccion + Division);
        if (!indice.exists()) indice.createNewFile();
        BufferedReader r = new BufferedReader(new FileReader(indice));
        try {
            String l;
            while ((l = r.readLine()) != null) {
                if (l.length() >= 50 && l.substring(0, 50).trim().equals(nombreLog)) return;
            }
        } finally {
            r.close();
        }
        utils.EscribirLinea(indice, String.format("%1$-50s", nombreLog) + ";X\r\n");
    }

    private int procesoImpresionFactura(int dato) {
        // Log.e("error2", "procesoImpresionFactura");

        infoClienteSalida.lectura_TablaClienteSalida(variables.clienteactual);

        double promedio = parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediocliente());
        // Log.e("error", "si imprime 3");
        if (((Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2()) + Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO3()) > 35000)) && (Double.parseDouble(PromedioCliente1) + Double.parseDouble(PromedioCliente2) + Double.parseDouble(PromedioCliente3)) * 3 < Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2()) + Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO3())) {
            if (!MODO.equals("AUTO"))
                mensajeOk("NO SE IMPRIME POR: Consumo Medidores muy alto favor verificar cuenta..!!" + parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO3()) + parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2()) + " \nde un Promedio " + parseStringToDouble(PromedioCliente1) + parseStringToDouble(PromedioCliente2) + Double.parseDouble(PromedioCliente3), "");
           // imprimirLabelRetencion();
           // return (0);
        } else if (MODO.equals("")) {
            // Log.e("error2", "critica1 " + TieneCausal40);
            //nuevo proceso que retiene la facturacion si es criticado como 40
            if (TieneCausal40 != 0) {
                mensajeOk("Factura no se Imprime por Critica 40\n" +
                        "Cod. Cunta     : " + infoRegistroEntrada.gettablaRegistroDeEntrada_CUENTA() +
                        "\nLectura Ant  : " + infoRegistroEntrada.gettablaRegistroDeEntrada_Lecturaanterior() +
                        "\nLect.Tomada  : " + infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA() +
                        "\nConsumo Calc.: " + (parseStringToDouble(infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA()) - parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Lecturaanterior())) +
                        "\nLiqui.Factura: " + infoClienteSalida.gettablaClienteSalida_VALORFACTURADO() +
                        "\nVersion App  : " + "2026-01-14-1", "");
            //    imprimirLabelRetencion();
             //   return (0);
            }//retener factura cuando es rutal y el consumo es exagerado de la siguiente manera
            else {
                // Log.e("error2", infoClienteSalida.getTablaClienteSalida_CONSUMOFACTURADO() + "critica2 " + variables.EvaluarCritica40 + "-" + (Double.parseDouble(PromedioCliente1) + Double.parseDouble(PromedioCliente2) + Double.parseDouble(PromedioCliente3)));
                if (!infoClienteSalida.gettablaClienteSalida_INDFACTURACION().trim().equals("F")) {
                    if (variables.EvaluarCritica40 == 0 || (variables.EvaluarCritica40 == 1 && (Double.parseDouble(PromedioCliente1) + Double.parseDouble(PromedioCliente2) + Double.parseDouble(PromedioCliente3)) == 0)
                            || (variables.EvaluarCritica40 == 1 && parseStringToDouble(infoClienteSalida.getTablaClienteSalida_CONSUMOFACTURADO().trim()) > (Double.parseDouble(PromedioCliente1) + Double.parseDouble(PromedioCliente2) + Double.parseDouble(PromedioCliente3)) * 5)) {
                        double ConsumoFacturadoAlto = Double.parseDouble(infoClienteSalida.getTablaClienteSalida_CONSUMOFACTURADO());//Convert.ToDouble(InfoClienteSalida.SCLIENTCONSUMO1)+
                        if (ConsumoFacturadoAlto > 2000 &&
                                ConsumoFacturadoAlto > (Double.parseDouble(PromedioCliente1) + Double.parseDouble(PromedioCliente2) + Double.parseDouble(PromedioCliente3)) * 5) {//&& !infoRegistroEntrada.gettablaRegistroDeEntrada_TIPOENERGIA().trim().equals("R")
                            mensajeOk("Factura  se Imprime\nCON CONSUMO EXCESIVO\n" +
                                    "Lectura Ant: " + infoRegistroEntrada.gettablaRegistroDeEntrada_Lecturaanterior() + "\nLect.Tomada: " + infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA() + "\nConsumo Calc. " + (Double.parseDouble(infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA()) - Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Lecturaanterior())) + "\nLiqui.Factura " + infoClienteSalida.gettablaClienteSalida_VALORFACTURADO(), "");
                          //  imprimirLabelRetencion();
                         //   return (0);

                        }
                    }
                }

            }
            // Log.e("error", "entra a imp");
            if (formatoEbsaActivo()) {
                if (generarFacturaConPlantilla() == 0) {
                    mensajeT("No se pudo generar la factura\ncon la plantilla EBSA.\nRevise LOGEVENTOS.LOG", msgMedio);
                    return 0;
                }
            } else {
                procedimientoUnionArchivos();
            }
            reimprimirxerror = 0;
            if (indicadorManual == 0)
                if (VariablesGlobales.habilitadaimpresora == 0 && indicadorManual == 0) {
                    mensajeT("Alerta de Impresora\n" + "La impresora se encuentra\n" + "Deshabilitada", msgLargo);
                    return 0;//descomentar
                } else
                    procedimientoDeImpresionPagina(0);//descomentar
            // Log.e("errror", "procedimientoDeImpresionPagina1");
            return (0);
//            }


         /*   procedimientoUnionArchivos();
           if (procedimientoDeImpresionPagina(0) == 0) {
                mensajeOk("LA IMPRESORA ESTA APAGADA.. PRENDALA PARA ENVIAR DE NUEVO");
                tiempoInactivolaImpresora = 0;*/

              /*  if (procedimientoDeImpresionPagina(0) == 0) {

                    return (0);
                }*/
            //  }
        } else {
//            File dir = new File("c:/Facturacion");
//            if (!dir.exists()) {
            //  procedimientoUnionArchivos();
            // reimprimirxerror = 0;
            //if (indicadorManual == 0)
            // procedimientoDeImpresionPagina(0);
//            }
        }
        return (1);
    }

    /**
     * Envia un archivo CPCL tal cual esta en disco, byte a byte, en bloques de 20 renglones con la
     * misma pausa que enviar_al_puerto(). No depende de numeros de linea ni de juego de caracteres:
     * el formato con plantilla puede tener cualquier cantidad de lineas y trae el FNC1 (byte 0x86)
     * del codigo de barras ya embebido en el dato. Reemplaza, solo para ese formato, el envio por
     * renglones de procedimientoDeImpresionPagina(), que corta las lineas 60 y 61 en la columna 59.
     */
    private boolean enviarArchivoCpcl(File archivo) throws IOException, InterruptedException {
        if (VariablesGlobales.btPrintService == null
                || VariablesGlobales.btPrintService.getState() != btPrintFile.STATE_CONNECTED) {
            mensajeT("La impresora no esta conectada", msgLargo);
            return false;
        }
        byte[] datos = new byte[(int) archivo.length()];
        RandomAccessFile raf = new RandomAccessFile(archivo, "r");
        try {
            raf.readFully(datos);
        } finally {
            raf.close();
        }
        final int renglonesPorBloque = 20;
        int inicio = 0, renglones = 0;
        for (int i = 0; i < datos.length; i++) {
            if (datos[i] == '\n' && ++renglones == renglonesPorBloque) {
                escribirBloqueImpresora(datos, inicio, i + 1);
                inicio = i + 1;
                renglones = 0;
            }
        }
        if (inicio < datos.length) {
            escribirBloqueImpresora(datos, inicio, datos.length);
        }
        return true;
    }

    private void escribirBloqueImpresora(byte[] datos, int desde, int hasta) throws InterruptedException {
        byte[] bloque = new byte[hasta - desde];
        System.arraycopy(datos, desde, bloque, 0, bloque.length);
        VariablesGlobales.btPrintService.write(bloque);
        Thread.sleep(50);
    }

    private int procedimientoDeImpresionPagina(int dato) {
        // Log.e("errror", "entra a imprimir");
        String imprimira;
        String enviara2;
        String enviara3 = "";
        String codigobarras1 = "";
        String codigobarras2 = "";
        String codigobarras3 = "";
        String codigobarras4 = "";
        String fileName;
        int tamanno = 59;
        int limiteBarras = 60;//84
        int tamanoBloque = 20;//60


        File file = new File(VariablesGlobales.directorioactual + "/LBLS/" +
                infoRegistroSalida.gettablaRegistroSalida_CUENTA().trim() + "_" + infoClienteEntrada.gettablaEntradaClientes_anio().trim() + "_" +
                infoClienteEntrada.gettablaEntradaClientes_mes().trim() + ".LOG");

        if (!file.exists()) {
            mensajeT("No existe el archivo de Impresion" + file.getAbsolutePath(), msgLargo);
            return 0;
        }

        try {
            tiempoInactivolaImpresora = 0;

            if (formatoEbsaActivo()) {
                // formato con plantilla: el archivo va completo y sin tocar
                if (!enviarArchivoCpcl(file)) return 0;
            } else
            if (dato == 0) {
                fileName = VariablesGlobales.directorioactual + "/LBLS/" +
                        infoRegistroSalida.gettablaRegistroSalida_CUENTA().trim() + "_" + infoClienteEntrada.gettablaEntradaClientes_anio().trim() + "_" +
                        infoClienteEntrada.gettablaEntradaClientes_mes().trim() + ".LOG";
                //antes+ "/DatosDeEntrada/imprimir.log";

                FileReader leerImp = new FileReader(fileName);
                BufferedReader reader = new BufferedReader(leerImp);
                String linea;

                int contar = 1;
                imprimira = "";
                int contar2 = 0;

                while ((linea = reader.readLine()) != null) {

                    if (contar == limiteBarras) {
                        enviara2 = linea;
                        codigobarras1 = enviara2.substring(0, tamanno);
                        codigobarras2 = enviara2.substring(tamanno, enviara2.length()) + "\r\n";
//                        codigobarras3 = enviara2.substring(0, tamanno);
//                        codigobarras4 = enviara2.substring(tamanno, enviara2.length()  ) + "\r\n";
                    } else if (contar == limiteBarras + 1 && tamanno == 59) {
                        enviara2 = linea;
                        codigobarras3 = enviara2.substring(0, tamanno);
                        codigobarras4 = enviara2.substring(tamanno, enviara2.length()) + "\r\n";
                    } else if (contar < limiteBarras)
                        imprimira += linea + "\r\n";
                    else
                        enviara3 += linea + "\r\n";


                    ++contar2;
                    if (contar2 >= tamanoBloque) {
                        enviar_al_puerto(imprimira, enviara3, codigobarras1, codigobarras2, codigobarras3, codigobarras4);
                        Thread.sleep(50);
                        contar2 = 0;
                        imprimira = "";
                        codigobarras1 = "";
                        codigobarras2 = "";
                        codigobarras3 = "";
                        codigobarras4 = "";
                        enviara3 = "";
                    }
                    ++contar;
                }

                reader.close();

                if (imprimira.length() > 0 || enviara3.length() > 0 || codigobarras1.length() > 0 || codigobarras3.length() > 0) {
                    // Log.e("error", "puerto 1");
                    enviar_al_puerto(imprimira, enviara3, codigobarras1, codigobarras2, codigobarras3, codigobarras4);
                    Thread.sleep(50);
                    imprimira = "";
                    codigobarras1 = "";
                    codigobarras2 = "";
                    codigobarras3 = "";
                    codigobarras4 = "";
                    enviara3 = "";
                }
            } else {
                fileName = VariablesGlobales.directorioactual + "/LBLS/" +
                        infoRegistroSalida.gettablaRegistroSalida_CUENTA().trim() + "_" + infoClienteEntrada.gettablaEntradaClientes_anio().trim() + "_" +
                        infoClienteEntrada.gettablaEntradaClientes_mes().trim() + ".LOG";
                //antes+ "/DATOSDEENTRADA/IMPRIMIR.LOG";
                RandomAccessFile reader = new RandomAccessFile(fileName, "rw");

                int contar = 0;
                imprimira = "";
                codigobarras1 = "";
                codigobarras2 = "";
                codigobarras3 = "";
                codigobarras4 = "";
                enviara3 = " ";
                reader.seek(0);

                for (int i = 1; i < reader.length(); i++) {
                    imprimira += reader.readLine() + "\r\n";
                    ++contar;
                    if (contar >= 60) {
                        // Log.e("error", "puerto 2");
                        enviar_al_puerto(imprimira, enviara3, codigobarras1, codigobarras2, codigobarras3, codigobarras4);
                        Thread.sleep(50);
                        contar = 0;
                        imprimira = "";
                        codigobarras1 = "";
                        codigobarras2 = "";
                        codigobarras3 = "";
                        codigobarras4 = "";
                        enviara3 = "";
                    }
                    i = i + reader.readLine().length();
                }

                reader.close();

                if (imprimira.length() > 0 || enviara3.length() > 0 || codigobarras1.length() > 0) {
                    Log.e("error", "puerto 3");
                    enviar_al_puerto(imprimira, enviara3, codigobarras1, codigobarras2, codigobarras3, codigobarras4);
                    Thread.sleep(50);
                    codigobarras1 = "";
                    codigobarras2 = "";
                    codigobarras3 = "";
                    codigobarras4 = "";
                    enviara3 = " ";
                }
            }
            Calendar calendar2 = Calendar.getInstance();

            tiempoInactivolaImpresora = (calendar2.get(Calendar.HOUR_OF_DAY) * 3600) + (calendar2.get(Calendar.MINUTE) * 60)
                    + (calendar2.get(Calendar.SECOND));

        } catch (Exception e) {
            // Log.e("error", "no imprime " + e.getMessage());
            utils.Log(logfile, "procedimientoDeImpresionPagina " + e.getMessage());
            mensajeT("Error en envio de Impresion de la Factura", msgLargo);
        }
        return 1;
    }

    private void enviar_al_puerto(String imprime, String imprime2, String barras1, String barras2, String barras3, String barras4) {

        int i;
        try {
            byte[] outputData;
            byte[] outputData2 = new byte[1];
            outputData2[0] = (byte) 134;

            int tamano = imprime.length();
            /*String trozo;

            for (i = 0; i <= imprime.length(); ) {
                if (tamano >= 35) {
                    trozo = imprime.substring(i, (i + 35));
                }else {
                    trozo = imprime.substring(i, (i + tamano));
                }
                outputData = trozo.getBytes();

                tamano -= 35;
                i = i + 35;
                VariablesGlobales.btPrintService.write(outputData);
            }
            if (tamano > 0) {
                i = i - 35;
                outputData = imprime.substring(i, (i + tamano)).getBytes();
                VariablesGlobales.btPrintService.write(outputData);
            }*/
            if (tamano > 0) {
                outputData = imprime.getBytes();
                VariablesGlobales.btPrintService.write(outputData);
            }
            if (barras1.length() > 0) {
                outputData = barras1.getBytes();
                VariablesGlobales.btPrintService.write(outputData);
                // TODO VariablesGlobales.btPrintService.write(outputData2, 0,
                // 1);
                VariablesGlobales.btPrintService.write(outputData2);
                outputData = barras2.getBytes();
                VariablesGlobales.btPrintService.write(outputData);
            }
            if (barras3.length() > 0) {
                outputData = barras3.getBytes();
                VariablesGlobales.btPrintService.write(outputData);
                // TODO VariablesGlobales.btPrintService.write(outputData2, 0,
                // 1);
                VariablesGlobales.btPrintService.write(outputData2);
                outputData = barras4.getBytes();
                VariablesGlobales.btPrintService.write(outputData);
            }

            // envio cadena dos del sitema donde esta el cupon
            byte[] outputData3;

            tamano = imprime2.length();

            /*for (i = 0; i <= imprime2.length(); ) {
                if (tamano >= 35)
                    trozo = imprime2.substring(i, (i + 35));
                else
                    trozo = imprime2.substring(i, (i + tamano));

                outputData3 = trozo.getBytes();
                tamano -= 35;
                i = i + 35;

                VariablesGlobales.btPrintService.write(outputData3);
            }

            if (tamano > 0) {
                i = i - 35;

                outputData3 = imprime2.substring(i, (i + tamano)).getBytes();

                VariablesGlobales.btPrintService.write(outputData3);
            }*/

            if (tamano > 0) {
                outputData3 = imprime2.getBytes();
                VariablesGlobales.btPrintService.write(outputData3);
            }
        } catch (Exception ex) {
            mensajeT("Problemas enviando a la impresora " + ex.toString(), msgLargo);
        }
    }

    private void procedimientoUnionArchivos() {//17639023
        int mesesatraso = 1;
        if (Double.parseDouble(infoClienteEntrada.gettablaEntradaClientes_Bimestral().trim()) > 1)
            mesesatraso = 2;

        // proceso si fue facturado deberia impresar el proceso de creacion de
        // archivos donde creo que cuando manda a procesar la impresion
        String nombreArchivoImp = VariablesGlobales.directorioactual + "/Impresion.log";
        String nombreArchivoProcesado = VariablesGlobales.directorioactual + "/DatosDeEntrada/imprimir.txt";

        String valortextoImp;
        String valortextoLog;

        File archivoImp = new File(nombreArchivoImp);
        File archivoProcesado = new File(nombreArchivoProcesado);

        if (archivoImp.exists() && archivoProcesado.exists()) {

            String archivoLog = VariablesGlobales.directorioactual + "/LBLS/" +
                    infoRegistroSalida.gettablaRegistroSalida_CUENTA().trim() + "_" + infoClienteEntrada.gettablaEntradaClientes_anio().trim() + "_" +
                    infoClienteEntrada.gettablaEntradaClientes_mes().trim() + ".LOG";
            //antes + "/DATOSDEENTRADA/IMPRIMIR.LOG";
            // Log.e("errora", " archivlo name " + infoRegistroSalida.gettablaRegistroSalida_CUENTA().trim() + "_" + infoClienteEntrada.gettablaEntradaClientes_anio().trim() + "_" +
            //   infoClienteEntrada.gettablaEntradaClientes_mes().trim() + ".LOG");

            if (indicadorManual == 1) // nuevo para cuando se le da automatico si esta en 1
                archivoLog = VariablesGlobales.directorioactual + "/LBLS/" +
                        infoRegistroSalida.gettablaRegistroSalida_CUENTA().trim() + "_" + infoClienteEntrada.gettablaEntradaClientes_anio().trim() + "_" +
                        infoClienteEntrada.gettablaEntradaClientes_mes().trim() + ".LOG";

            File fileArchivoLog = new File(archivoLog);

            if (fileArchivoLog.exists())
                fileArchivoLog.delete();

            try {
                RandomAccessFile escribirLog = new RandomAccessFile(fileArchivoLog, "rw");
                RandomAccessFile leerLog = new RandomAccessFile(archivoProcesado, "rw");

                valortextoLog = leerLog.readLine();
                int contar = 1;

                String linea = "";

                try {
                    FileReader leerImp = new FileReader(archivoImp);
                    BufferedReader br = new BufferedReader(leerImp);

                    while ((linea = br.readLine()) != null) {
                        valortextoImp = linea;
                        valortextoLog = "";

                        if ((contar > 9 && contar <= 80)) {//79
                            valortextoLog = leerLog.readLine();

                          /*  if (contar == 61) {
                                escribirLog.writeBytes(valortextoImp + " " + valortextoLog.trim() + "\r\n");
                                contar++;
                                valortextoImp = br.readLine();//reader.ReadLine();
                            }*/
                        }
                        //if (contar <= 80) {//79
                        //  escribirLog.writeBytes(valortextoImp  + valortextoLog + "\r\n");

                        //} else {
                        //   escribirLog.writeBytes(valortextoImp.trim()+ "\r\n");
                        //}

                       /* if (contar > 99) {
                            escribirLog.writeBytes(valortextoImp + "\r\n");
                        } else {*/
                        //   Log.e("error", "union " + contar + valortextoImp + "-" + valortextoLog);
                        //if (contar == 82 || contar == 83) {
                        //    escribirLog.writeBytes(valortextoImp + valortextoLog + "\r\n");
                        //} else {
                        //    escribirLog.writeBytes(valortextoImp + " " + valortextoLog + "\r\n");
                        //}
                        if (contar < 84) {
                            if (contar == 81 || contar == 82 || contar < 10) {
                                Log.e("INFO", "Print: " + valortextoImp + "\r\n");
                                escribirLog.writeBytes(valortextoImp + "\r\n");
                            } else {
                                Log.e("INFO", "Print: " + valortextoImp + " " + valortextoLog + "\r\n");
                                escribirLog.writeBytes(valortextoImp + " " + valortextoLog + "\r\n");
                            }
                        }
                        //  }

                        contar++;
                    }
                    leerImp.close();

                    // Escritura del archivo L para el Envio de los LBLS
                    File impresiones = new File(VariablesGlobales.directorioactual + "/LBLS/" + "L" + Ciclo + Municipio + Seccion + Division);
                    // Log.e("errora", fileArchivoLog.getName() + " archivlo name " + archivoLog);
                    if (!impresiones.exists()) {
                        impresiones.createNewFile();
                    }
                    FileReader stream3 = new FileReader(VariablesGlobales.directorioactual + "/LBLS/" + "L" + Ciclo + Municipio + Seccion + Division);
                    BufferedReader reader = new BufferedReader(stream3);
                    String linealbl = "";
                    String dato;
                    int cont = 0;
                    String linecompare = "", linetowirte = "";

                    while ((linealbl = reader.readLine()) != null) {
                        dato = linealbl;
                        linecompare = dato.substring(0, 50).trim();
                        linetowirte = archivoLog.substring(archivoLog.lastIndexOf("/") + 1, archivoLog.length());
                        if (linecompare.equals(linetowirte)) {
                            cont++;
                        }
                    }
                    reader.close();
                    if (cont == 0) {
                        utils.EscribirLinea(impresiones, String.format("%1$-50s", archivoLog.substring(archivoLog.lastIndexOf("/") + 1, archivoLog.length())) + ";X\r\n");
                    }

                } catch (Exception e) {
                    utils.Log(logfile, "[MenuDeLiquidacion]procedimientoUnionArchivos(); " + e.getMessage());
                    // Log.e("error", "no crea archivo2 " + e.getMessage());
                    System.out.println("Excepcion leyendo fichero " + archivoImp + ": " + e);
                }

                leerLog.close();
                escribirLog.close();

            } catch (FileNotFoundException e) {
                utils.Log(logfile, "[MenuDeLiquidacion]procedimientoUnionArchivos(); " + e.getMessage());

                // Log.e("error", "no crea archivo1 " + e.getMessage());
                e.printStackTrace();

            } catch (IOException e) {
                utils.Log(logfile, "[MenuDeLiquidacion]procedimientoUnionArchivos(); " + e.getMessage());
                // Log.e("error", "no crea archivo " + e.getMessage());
                e.printStackTrace();
            }
        } else
            mensajeT("Archivo no existe: " + archivoImp + "\n o " + archivoProcesado, msgLargo);
    }

    protected int generarArchivoTextoImpresion() {

        String comando;
        leerInformacionUsuario(3);
        if (indicadorManual == 1) {
            return 1;
        }
        variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";generarArchivoTextoImpresion ;" + getPhoneDate() + "-" + getPhoneHour();
        escribeResumenTiempo(variableResumenLiquidacion + " " + " Entra a generarArchivoTextoImpresion()");

        if (infoMedidorEntrada.gettablaMedidorEntrada_Fechalectanterior().trim().equals("") || infoMedidorEntrada.gettablaMedidorEntrada_Fechalectanterior().length() < 10) {
            Calendar cal = new GregorianCalendar();
            cal.add(cal.MONTH, -1);
            Date date = cal.getTime();
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            String formatteDate = df.format(date);
            infoMedidorEntrada.settablaMedidorEntrada_Fechalectanterior(formatteDate);
        }

        convertirFormatoDeFecha(infoMedidorEntrada.gettablaMedidorEntrada_Fechalectanterior(), fechaanteriorenformato, mesenformato, "D/M/A");

        comando = "Creacion impresion\r\n";

        generarArchivoCabecera(comando);
        generarTextoInformacionBasica();


        if (variables.evaluarConServicio(infoMedidorEntrada.gettablaMedidorEntrada_Tipomedidor().trim()) == 1) {

            if (!generarTextosMedidor())
                return 0;

            //    if (parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Bimestral().trim()) != 3)//== 1
            generarTextosHistoricoConsumos();
            //    else
            //        generarTextosHistoricoConsumostrimestral();

        } else {
            int espacios2 = (int) 7 - (infoClienteSalida.gettablaClienteSalida_VALORFACTURADO().trim().length() / 2);
            String sp2 = "";
            String formatopesos22 = String.format("%1$" + espacios2 + "s", sp2) + "$ " + formateo(parseStringToDouble(infoClienteSalida.gettablaClienteSalida_VALORFACTURADO().trim()));
            comando = formatopesos22;
            escribaTextoDeImpresion(comando);
            escribirLineaEnBlanco(16);
        }

        generarTextosSubsidioContribuciones();

        generarTextosConceptos(0);
//arreglar ebsa infoMedidorEntrada.gettablaMedidorEntrada_Fechalectanterior()
        convertirFormatoDeFecha("26/08/2026", fechaanteriorenformato, mesenformato, "D/M/A");
        fechaanteriorenformato = variables.fechahoy;

        //   generarTextosInformacionAdicional(fechaanteriorenformato);

        convertirFormatoDeFecha(variables.amd, fechaanteriorenformato, mesenformato, "AMD");
        generarTextosColillaFactura();
        variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";generarArchivoTextoImpresion ;" + getPhoneDate() + "-" + getPhoneHour();
        escribeResumenTiempo(variableResumenLiquidacion + " " + " FIM a generarArchivoTextoImpresion()");

        // generarTextosVarios();
        return 1;
    }

    void generarTextosVarios() {

        String comando = String.format("%-18s", infoClienteEntrada.gettablaEntradaClientes_Ruta().trim()).replace(' ', ' ') + infoClienteEntrada.gettablaEntradaClientes_Estrato().trim() + "\r\n" +
                infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim();
        escribaTextoDeImpresion(comando);

        String cadenanivel;
        switch (parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Niveldetension())) {
            case 1:
                cadenanivel = "1 Secundaria    ";
                break;
            case 2:
                cadenanivel = "2 Primaria      ";
                break;
            case 3:
                cadenanivel = "3 Subtransmision";
                break;
            default:
            case 4:
                cadenanivel = "4 Transmision   ";
                break;
        }
        comando = cadenanivel.substring(0, 15) + "   " + infoClienteEntrada.gettablaEntradaClientes_cargainstalada().trim();
        escribaTextoDeImpresion(comando);
        // Log.e("error", variables.desencapsular(infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA()) + " lectura t " + infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA());
        comando = String.format("%1$-10s", parseStringToInteger(infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA().trim())) + " " + infoRegistroEntrada.gettablaRegistroDeEntrada_Lecturaanterior().substring(0, 8) + "    " +
                infoRegistroEntrada.gettablaRegistroDeEntrada_Factormultipicacion().substring(0, 5) + "   "; //.PadRight(10, ' ')...
        if (infoRegistroEntrada.gettablaRegistroDeEntrada_tipoenergia().equals("R"))
            comando += String.format("%1$-6s", (int) (parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO()) - parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2())));//PadRight(6, ' ')
        else
            comando += String.format("%1$-6s", (int) (variables.ejecutarAjusteUnidades(parseStringToDouble(infoRegistroSalida.gettablaRegistroSalida_CONSUMOTOMADO()))));

        escribaTextoDeImpresion(comando);

        comando = String.valueOf(infoClienteEntrada.gettablaEntradaClientes_nombrecircuito()).substring(0, 16);// infoClienteEntrada.E_CLIENT_NODO_CIRCUITO;
        escribaTextoDeImpresion(comando);
        // generarTextosBarras();
        comando =  infoRegistroEntrada.gettablaRegistroDeEntrada_ano1() + infoRegistroEntrada.gettablaRegistroDeEntrada_periodo1() + "\r\n" +
                     infoRegistroEntrada.gettablaRegistroDeEntrada_ano2() + infoRegistroEntrada.gettablaRegistroDeEntrada_periodo2() + "\r\n" +
                     infoRegistroEntrada.gettablaRegistroDeEntrada_ano3() + infoRegistroEntrada.gettablaRegistroDeEntrada_periodo3() + "\r\n" +
                     infoRegistroEntrada.gettablaRegistroDeEntrada_ano4() + infoRegistroEntrada.gettablaRegistroDeEntrada_periodo4() + "\r\n" +
                     infoRegistroEntrada.gettablaRegistroDeEntrada_ano5() + infoRegistroEntrada.gettablaRegistroDeEntrada_periodo5() + "\r\n" +
                     infoRegistroEntrada.gettablaRegistroDeEntrada_ano6() + infoRegistroEntrada.gettablaRegistroDeEntrada_periodo6(); //PadLeft(2, '0')....
        escribaTextoDeImpresion(comando);
        comando = "\r\n" + "\r\n" + "\r\n" + "\r\n" + "\r\n" + "";
        escribaTextoDeImpresion(comando);

        comando = infoRegistroEntrada.gettablaRegistroDeEntrada_consumo1() + "\r\n" +
                infoRegistroEntrada.gettablaRegistroDeEntrada_consumo2() + "\r\n" +
                infoRegistroEntrada.gettablaRegistroDeEntrada_consumo3() + "\r\n" +
                infoRegistroEntrada.gettablaRegistroDeEntrada_consumo4() + "\r\n" +
                infoRegistroEntrada.gettablaRegistroDeEntrada_consumo5() + "\r\n" +
                infoRegistroEntrada.gettablaRegistroDeEntrada_consumo6() + "\r\n";
        if (parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Bimestral()) != 3)
            comando += variables.ejecutarAjusteUnidades(parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediocliente().trim())) + "";
        else
            comando += variables.ejecutarAjusteUnidades(parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediocliente().trim())) + "";

        escribaTextoDeImpresion(comando);
    }

    private void generarTextosBarras() {

        leerArchivoFormato();
        String consumo;
        String comando;
        int i, numlineas = 7;
        int topegraficaconsumo = 790 + moverGrafico;
        double consmax = 0;
        int X;
        double valorincremento;

        consumo = infoRegistroEntrada.gettablaRegistroDeEntrada_consumo1() + infoRegistroEntrada.gettablaRegistroDeEntrada_consumo2() +
                infoRegistroEntrada.gettablaRegistroDeEntrada_consumo3() + infoRegistroEntrada.gettablaRegistroDeEntrada_consumo4() +
                infoRegistroEntrada.gettablaRegistroDeEntrada_consumo5() + infoRegistroEntrada.gettablaRegistroDeEntrada_consumo6();

        if (parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO().trim()) < 99999)
            consumo = consumo + String.format("%1$5s", parseStringToInteger(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO().trim())); //.ToString().Trim().PadLeft(5, ' ')
        else
            consumo = consumo + "99999";

        for (i = 0; i <= 6; i++) {
            int x = i * 5;
            if (parseStringToDouble(consumo.substring(x, x + 5)) > consmax)
                consmax = parseStringToDouble(consumo.substring(x, x + 5));
        }

        if (consmax > 0)
            valorincremento = 120 / consmax;
        else
            valorincremento = 1;

        for (i = 0; i <= numlineas - 1; i++) {
            int y = i * 5;
            X = (int) (topegraficaconsumo - (parseStringToDouble(consumo.substring(y, y + 5)) * valorincremento));

            if (i < 6)
                comando = String.valueOf(15 + 15 + (37 * i)) + " " + String.valueOf(X) + " " + String.valueOf(32 + 15 + (37 * i)) + " " + String.valueOf(topegraficaconsumo) + " 2";
            else
                comando = String.valueOf(15 + 15 + (37 * i) + 26) + " " + String.valueOf(X) + " " + String.valueOf(32 + 15 + (37 * i) + 26) + " " + String.valueOf(topegraficaconsumo) + " 2";

            escribaTextoDeImpresion(comando);
        }
    }



    int generarTextosConceptos(int segundapagina) {
        int cantidadconceptos;
        int punteroconceptos;
        int totalconceptosvalidos = 0;
        int totalconceptosvalidosAseo = 0;
        String fpesos;
        double valor;
        double valormasconceptos = 0;
        String comando;
        comandoconceptosAseo ="";
        String fpesos1 = " ";
        double valorfinanciacion;
        sumarbloqueenergia = 0;
        double acumuladootros = 0;
        double acumuladootrosAseo = 0;

        double sumaAlumbrado = 0;
        comandoconceptos = "";
        comandocartera = "";
        comandofinanciacion = "";
        TotalAseo = 0;
        escribeResumenTiempo(  "Inicia proceso de Impresion de conceptos|" + VariablesGlobales.registroactual);
        variables.formatofinanciero = " ";
        punteroconceptos = parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_primercobro());
        cantidadconceptos = parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_nrocobros());
        variables.valorfacturadoenergia = "";
        variables.tienedeuda = 0;

        variables.valorconsumo = 0.00;
        valorconsumo = 0;
        int contarconceptos = 0;

        variables.valorconsumo = 0;
        aseo = "";
        periodofacturado = "";
        costoservicio = "";
        subsidiocontribucion = "";
        MensajeFoes = "";
        TieneConcepto51 = 0;
        alumbrado = "         0";
        SubsidioAseo = "0";
//se debe de implementar lo de aseo igual que en calarca
        infoCobrosLiquidados.abrir_TablaCobrosRealizados(infoCobrosLiquidados.getArchivo_TablaCobrosRealizados()); //ax new
        while (cantidadconceptos > 0) {
            --cantidadconceptos;


            ++contarconceptos;
            descripcionConcepto.encontro_TablaDescripcionConceptos = 0;
            infoCobrosLiquidados.lectura_TablaCobrosRealizados(punteroconceptos++);
            valor = parseStringToDouble(infoCobrosLiquidados.gettablaCobrosRealizados_VALOR().trim().equals("") ? "0" : infoCobrosLiquidados.gettablaCobrosRealizados_VALOR());

            if (infoCobrosLiquidados.gettablaCobrosRealizados_VALOR().trim().length() == 0)
                infoCobrosLiquidados.settablaCobrosRealizados_Valor("         0");
            valor = Double.parseDouble(infoCobrosLiquidados.gettablaCobrosRealizados_VALOR().replace("-", "0"));
            if (Integer.parseInt(infoCobrosLiquidados.gettablaCobrosRealizados_CONCEPTODECOBRO().trim()) == 0)
                variables.tienedeuda = 1;

            if (infoCobrosLiquidados.gettablaCobrosRealizados_IDCONCEPTO().trim().equals("")) {
                if (descripcionConcepto.abrir_TablaDescripcionConceptos(descripcionConcepto.getArchivo_TablaDescripcionConceptos())) {
                    descripcionConcepto.lectura_TablaDescripcionConceptos(1);
                    descripcionConcepto.Cerrar_TablaDescripcionConceptos();
                }
            } else if (parseStringToInteger(infoCobrosLiquidados.gettablaCobrosRealizados_IDCONCEPTO()) > 0) {
                if (descripcionConcepto.abrir_TablaDescripcionConceptos(descripcionConcepto.getArchivo_TablaDescripcionConceptos())) {
                    descripcionConcepto.lectura_TablaDescripcionConceptos(parseStringToInteger(infoCobrosLiquidados.gettablaCobrosRealizados_IDCONCEPTO()));
                    descripcionConcepto.Cerrar_TablaDescripcionConceptos();
                }
            }
            if (descripcionConcepto.encontro_TablaDescripcionConceptos == 0) {
                descripcionConcepto.settablaDescripcionConceptos_DESCRIPCION("COBRO RELIQUIDADO"); // Esto es porque no esta encontrando la desc concepto
            }


            if (infoCobrosLiquidados.gettablaCobrosRealizados_INDICADORACTIVIDAD().trim().equals("X")) {

                //METODO DE ESCRIBIR LA FINANCIACION AL ARCHIVO
                variables.formatofinanciero = "$ " + String.format("%1$8s", parseStringToInteger(infoCobrosLiquidados.gettablaCobrosRealizados_VALOR().trim())) + "  $ " + String.format("%1$8s", infoCobrosLiquidados.gettablaCobrosRealizados_RANGOMINIMO().trim()) + "  "
                        + " $  " + infoCobrosLiquidados.gettablaCobrosRealizados_SALDOPENDIENTE() + "    $  " + infoCobrosLiquidados.gettablaCobrosRealizados_CUOTASPENDIENTES() + "    $ " + infoCobrosLiquidados.gettablaCobrosRealizados_RANGOMAXIMO();

                comandocartera = variables.formatofinanciero;
                //EscribaTextoDeImpresion(Variables.Formatofinanciero);

                valorfinanciacion = valor;

                fpesos = "$" + String.format("%1$14s", formateo(valorfinanciacion));
                fpesos1 = "$" + String.format("%1$14s", formateo(valorfinanciacion));

            }


            if (infoCobrosLiquidados.gettablaCobrosRealizados_INDICADORACTIVIDAD().trim().equals("F")) {

                //METODO DE ESCRIBIR LA FINANCIACION AL ARCHIVO
                //   if (cantidadconceptos + 1 == Convert.ToInt32(InfoClienteEntrada.ECLIENTNRODECONCEPTOS) || cantidadconceptos + 1 == Convert.ToInt32(InfoClienteEntrada.ECLIENTNRODECONCEPTOS) - 1)
                //    {
                if (infoCobrosLiquidados.gettablaCobrosRealizados_SALDOPENDIENTE().trim().equals(""))
                    infoCobrosLiquidados.settablaCobrosRealizados_saldopendiente("0");

                if (infoCobrosLiquidados.gettablaCobrosRealizados_CUOTASPENDIENTES().trim().equals(""))
                    infoCobrosLiquidados.settablaCobrosRealizados_cuotaspendientes("0");

                if (infoCobrosLiquidados.gettablaCobrosRealizados_VALOR().trim().equals(""))
                    infoCobrosLiquidados.settablaCobrosRealizados_Valor("0");


                variables.formatofinanciero = "  $  " + infoCobrosLiquidados.gettablaCobrosRealizados_RANGOMINIMO().trim() + "    " + infoCobrosLiquidados.gettablaCobrosRealizados_PORCENTAJE()
                        + "     $  " + infoCobrosLiquidados.gettablaCobrosRealizados_VALOR() + "   " + infoCobrosLiquidados.gettablaCobrosRealizados_CUOTASPENDIENTES() + "          $ "
                        + (parseStringToInteger(infoCobrosLiquidados.gettablaCobrosRealizados_SALDOPENDIENTE().trim().replace(",", ".")));
                comandofinanciacion = variables.formatofinanciero;


                if (parseStringToDouble(infoCobrosLiquidados.gettablaCobrosRealizados_SALDOPENDIENTE()) > 0)
                    valorfinanciacion = Double.parseDouble(infoCobrosLiquidados.gettablaCobrosRealizados_SALDOPENDIENTE().trim()) - Double.parseDouble(infoCobrosLiquidados.gettablaCobrosRealizados_VALOR().trim());
                else
                    valorfinanciacion = valor;

                fpesos = "$" + String.format("%1$14s", formateo(valorfinanciacion));
                fpesos1 = "$" + String.format("%1$14s", formateo(valor));

            }


            fpesos = "$" + String.format("%1$12s", formateo(valor));


            //se cambia la condicion porque ya llega dos veces
            //aqui seria comparar con los conceptos que estan en los camvenios
            //se quita el hecho de ejecutar la impresion por aparte del alumbrado publico
           /* if (infoCobrosLiquidados.gettablaCobrosRealizados_IDCONVENIO().trim().equals("1")) {
                //es energia alumbrado
                sumaAlumbrado = sumaAlumbrado + valor;
                fpesos = "$" + String.format("%1$12s", formateo(sumaAlumbrado));
                alumbrado = fpesos;
                TieneConcepto51 = 1;
            } else {*/
                    if (Integer.parseInt(descripcionConcepto.gettablaDescripcionConceptos_CODIGO().trim()) == 21 || Integer.parseInt(descripcionConcepto.gettablaDescripcionConceptos_CODIGO().trim()) == 31) {
                        variables.valorfacturadoenergia = fpesos;
                    }
                    if (infoCobrosLiquidados.gettablaCobrosRealizados_INDICADORACTIVIDAD().trim().equals("D")) {
                        sumarbloqueenergia = sumarbloqueenergia - valor;
                    }
                    else {
                        if (infoCobrosLiquidados.gettablaCobrosRealizados_INDICADORACTIVIDAD().trim().equals("C"))
                            sumarbloqueenergia += valor;
                    }

                    if (Integer.parseInt(infoCobrosLiquidados.gettablaCobrosRealizados_CONCEPTODECOBRO().trim()) == 607 || (Integer.parseInt(infoCobrosLiquidados.gettablaCobrosRealizados_CONCEPTODECOBRO().trim()) == 608)) {
                        if (infoCobrosLiquidados.gettablaCobrosRealizados_INDICADORACTIVIDAD().trim().equals("D"))
                            comando = "Ajuste al Peso              <" + fpesos + ">";//+"\r\n";
                        else
                            comando = "Ajuste al Peso               " + fpesos;//+"\r\n";

                    } else if (Integer.parseInt(infoCobrosLiquidados.gettablaCobrosRealizados_IDCONCEPTO().trim()) > 0) {
                        if ((Integer.parseInt(infoCobrosLiquidados.gettablaCobrosRealizados_CONCEPTODECOBRO().trim()) == 110) && (valor == 0))
                            comando = "";//+"\r\n";
                        else {
                            if ((infoCobrosLiquidados.gettablaCobrosRealizados_INDICADORACTIVIDAD().trim().equals("D")))//SE ELIMINA YA QUE NO SE HACE NADA CON EL || (infoCobrosLiquidados.SCOBROINDICADORACTIVIDAD[0] == 'F'))
                            {
                                comando = descripcionConcepto.gettablaDescripcionConceptos_DESCRIPCION().substring(0,25) + "     <" + fpesos + ">";//+"\r\n";
                            } else {

                                if (valor >= 0) {
                                        comando = descripcionConcepto.gettablaDescripcionConceptos_DESCRIPCION().substring(0,25) + "      " + fpesos;//+"\r\n" ;
                                } else {
                                        comando = descripcionConcepto.gettablaDescripcionConceptos_DESCRIPCION().substring(0,25) + "     <" + fpesos + ">";//+"\r\n";
                                }
                            }
                        }
                    } else {
                        if (infoCobrosLiquidados.gettablaCobrosRealizados_INDICADORACTIVIDAD().trim().equals("D") || (valor < 0))
                            comando = "CREDITO SIN DESCRIP.     <" + fpesos + ">";//+"\r\n";
                        else
                            comando = "CREDITO SIN DESCRIP.     " + fpesos;//+"\r\n";

                    }

                    //nuevo para sumar los registros de aseo
                    if (infoCobrosLiquidados.gettablaCobrosRealizados_INDICADORACTIVIDAD().trim().equals("A") ||
                          infoCobrosLiquidados.gettablaCobrosRealizados_INDICADORACTIVIDAD().trim().equals("Y")){

                        if (infoCobrosLiquidados.gettablaCobrosRealizados_INDICADORACTIVIDAD().trim().equals("A") ) {
                            TotalAseo = TotalAseo + Double.parseDouble(infoCobrosLiquidados.gettablaCobrosRealizados_VALOR().replace("-", "0"));
                        }else
                        {
                            TotalAseo = TotalAseo - Double.parseDouble(infoCobrosLiquidados.gettablaCobrosRealizados_VALOR());
                        }

                    }

                    if (!infoCobrosLiquidados.gettablaCobrosRealizados_INDICADORACTIVIDAD().trim().equals("F")
                       && !infoCobrosLiquidados.gettablaCobrosRealizados_INDICADORACTIVIDAD().trim().equals("X")
                       && !infoCobrosLiquidados.gettablaCobrosRealizados_INDICADORACTIVIDAD().trim().equals("A")
                       && !infoCobrosLiquidados.gettablaCobrosRealizados_INDICADORACTIVIDAD().trim().equals("Y")) {
                            if (totalconceptosvalidos < 6) {//6 eran con arreglo eran 8
                                if (valor == 0 ) {
                                    //no se imprimr el concepto en cero
                                } else {
                                    comandoconceptos += comando + "\r\n";
                                    totalconceptosvalidos++;
                                }
                            } else {
                                acumuladootros = acumuladootros + valor;
                            }
                    }
                    else
                    {
                        if (infoCobrosLiquidados.gettablaCobrosRealizados_INDICADORACTIVIDAD().trim().equals("A")
                            || infoCobrosLiquidados.gettablaCobrosRealizados_INDICADORACTIVIDAD().trim().equals("Y")) {
                            if (totalconceptosvalidosAseo < 6) {//6 eran con arreglo eran 8
                                if (valor == 0 ) {
                                    //no se imprimr el concepto en cero
                                } else {
                                    comandoconceptosAseo += comando + "\r\n";
                                    totalconceptosvalidosAseo++;
                                }
                            } else {
                                acumuladootrosAseo = acumuladootrosAseo + valor;
                            }


                        }

                    }

                }

            //}//se quita el cierre del bloque que independizaba solo el alumbrado publico




        // Log.e("error", "comandocartera " + comandocartera);
        escribaTextoDeImpresion(comandocartera);
        // Log.e("error", "comandofinanciacion " + comandofinanciacion);
        escribaTextoDeImpresion(comandofinanciacion);
        int sub = 0;
        if (comandoconceptos.length() > 0) {
            sub = comandoconceptos.length() - 2;
            //  // Log.e("error","comandoconceptos "+comandoconceptos);
            escribaTextoDeImpresion(comandoconceptos.substring(0, sub));

        }

        if (acumuladootros > 0) {
            fpesos = "$" + String.format("%1$12s", formateo(acumuladootros));
            comando = "Otros Conceptos                        " + fpesos;//+"\r\n";
            //    // Log.e("error",comando+" conce1 ");
            escribaTextoDeImpresion(comando);
            totalconceptosvalidos++;
        }

        if (acumuladootrosAseo > 0) {
            fpesos = "$" + String.format("%1$12s", formateo(acumuladootros));
            comandoconceptosAseo += "Otros Conceptos Aseo     " + fpesos;//+"\r\n";
            //    // Log.e("error",comando+" conce1 ");
            totalconceptosvalidosAseo++;
        }

        while (totalconceptosvalidosAseo < 6) {//eran 7 con arreglo son 9
            totalconceptosvalidosAseo++;
            comandoconceptosAseo += "este aseo .." +totalconceptosvalidosAseo+ "\r\n";

        }

        while (totalconceptosvalidos < 7) {//eran 7 con arreglo son 9
            totalconceptosvalidos++;
            escribirLineaEnBlanco(1);

        }
        //se incluyen las dos lineas que ya no se imprimen por presentadion
        comando = ". . . \r\n .";
        //  // Log.e("error",comando+" conce2 ");
        escribaTextoDeImpresion(comando);

        if (sumarbloqueenergia < 0)
            //fpesos = "<" + String.format("%1$13s", formateo(sumarbloqueenergia * (-1))) + ">";
            fpesos = String.format("%1$13s", "0");
        else
            fpesos = String.format("%1$14s", formateo(sumarbloqueenergia));

        comando = fpesos;//+"\r\n";
        //  // Log.e("error",comando+" conce2 ");
        escribaTextoDeImpresion(comando);
        escribeResumenTiempo(  "Finaliza Impresion de conceptos cobrados |" + VariablesGlobales.registroactual);
        // // Log.e("error","comandoc "+comando);

        return (1);
    }

    int generarTextosHistoricoConsumostrimestral() {
        //CAMBIAR PARA EBSA
        /*
        noavanzalinea = 0;
        String fpesos;
        String comando;
        variables.valorconsumo = 0;
        valorrango1 = 0;
        double consumo = 0;

        variables.valorconsumo2 = 0;
        cadenaconceptosimpresos = "";
        int consumo_promedio = (int) (parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO()) / 3);

        int numerorango = 2;
        int contarlineas = 0;

        for (int y = 1; y <= 3; y++) {
            if (y == 1)
                consumo = parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO()) - (consumo_promedio * 2);
            else
                consumo = consumo_promedio;

            tablaTarifa.lectura_TablaTarifas(parseStringToInteger(infoRegistroEntrada.gettablaRegistroDeEntrada_APUNTADORTARIFA()) - numerorango);
            --numerorango;

            if (parseStringToDouble(tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1()) == 0) {
                //tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA() = reemplazarDatos(tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA());
                double temp = consumo * parseStringToDouble(tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA());
                temp = variables.ejecutarAjusteUnidades(temp);//Ax nuevo
                fpesos = "$" + String.format("%1$12s", formateo(temp)); // "$" + String.Format("{0:#,###,###;###,###;0.##}", temp).ToString().PadLeft(12, ' ');
                valorrango1 += consumo * parseStringToDouble(tablaTarifa.gettablaTarifas_VALORUNIDAD1());


                comando = String.format("%1$-7s", (int) consumo) + "    " + String.format("%1$-10s", tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA().trim()).substring(0, 7) + "= " + fpesos;

                if (contarlineas < 3)
                    escribaTextoDeImpresion(comando + "\r\n");
                else
                    cadenaconceptosimpresos += comando + "\r\n";

                variables.valorconsumo2 += consumo * parseStringToDouble(tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA());
                ++contarlineas;
            } else if (parseStringToDouble(tablaTarifa.gettablaTarifas_CONSUMOMAXIMO2()) == 0) {
                if (consumo > parseStringToDouble(tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1())) {

                    valorrango1 += parseStringToDouble(tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1()) * parseStringToDouble(tablaTarifa.gettablaTarifas_VALORUNIDAD1());
                    double temp2 = parseStringToDouble(tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1()) * parseStringToDouble(tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA());
                    fpesos = String.format("%1$12s", formateo(temp2));//String.Format("{0:#,###,###;###,###;0.##}", temp2).ToString().PadLeft(12, ' ');

                    comando = String.format("%1$-7s", tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1().trim()) + "    " + tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA().substring(0, 8) + "= " + fpesos; //.PadRight(7, ' ')..

                    temp2 = (consumo - parseStringToDouble(tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1())) * parseStringToDouble(tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA());
                    fpesos = String.format("%1$12s", formateo(temp2));//String.Format("{0:#,###,###;###,###;0.##}", TEMP2).ToString().PadLeft(12, ' ');

                    temp2 = consumo - parseStringToDouble(tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1());
                    comando += "\r\n" + String.format("%1$-7s", (int) temp2) + "    " + String.format("%1$-10s", tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA().trim()).substring(0, 8) + "= " + fpesos; //Convert.ToString(temp2).PadRight(7, ' ')....PadRight(10, ' ')

                    if (contarlineas < 3)
                        escribaTextoDeImpresion(comando);
                    else
                        cadenaconceptosimpresos += comando + "\r\n" + "\r\n";

                    variables.valorconsumo2 += (parseStringToDouble(tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1()) * parseStringToDouble(tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA())) + (consumo -
                            parseStringToDouble(tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1())) * parseStringToDouble(tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA());
                    ++contarlineas;
                } else {
                    valorrango1 += consumo * parseStringToDouble(tablaTarifa.gettablaTarifas_VALORUNIDAD1());
                    double temp3 = (consumo * parseStringToDouble(tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA()));
                    fpesos = String.format("%1$12s", formateo(temp3));//String.Format("{0:#,###,###;###,###;0.##}", temp3).ToString().PadLeft(12, ' ');
                    comando = String.format("%1$-7s", (int) consumo) + "    " + String.format("%1$-10s", tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA().trim()).substring(0, 7) + "= " + fpesos;//PadRight(7, ' ').....PadRight(10, ' ')

                    if (contarlineas < 3)
                        escribaTextoDeImpresion(comando + "\r\n");
                    else
                        cadenaconceptosimpresos += comando + "\r\n" + "\r\n";

                    variables.valorconsumo2 += consumo * parseStringToDouble(tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA());
                    ++contarlineas;
                }
            }
        }

        if (contarlineas >= 3) {
            if (contarlineas == 3)
                cadenaconceptosimpresos += " \r\n \r\n";
            else if (contarlineas == 4)
                cadenaconceptosimpresos += " \r\n";

            noavanzalinea = 1;
        }
          */
        return (1);
    }

    private void generarTextosSubsidioContribuciones() {

        String formatocenergia;
        double contribucionenergia;
        double porcentaje;
        double valorconsumo;
        double totalreal;
        String formatopesos3;
        String comando;

        escribeResumenTiempo(  "Inicia proceso de Impresion de subcidio contribucion|" + VariablesGlobales.registroactual);

        contribucionenergia = parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONTRIBUCIONENERGIA());

        buscarPrecioFacturado();
        valorconsumo = (float) (variables.totalconsumoperiodo);

        /*
        se apaga para ebsa
        if (apuntadortarifaactiva > 0)
            tablaTarifa.lectura_TablaTarifas(apuntadortarifaactiva);
        else
            tablaTarifa.lectura_TablaTarifas(1);
*/
        if (noavanzalinea == 0) {
            // escribaTextoDeImpresion("-");
        }
        noavanzalinea = 0;
        if (valorconsumo == 0) {

            variables.valorfacturadoenergia = "$" + String.format("%1$10s", formateo(variables.valorconsumo2));//String.Format("{0:#,###,###;###,###;0.##}", parseStringToDouble(variables.valorconsumo2)).ToString().PadLeft(20, ' ');
            // comando = "                  " + variables.valorfacturadoenergia;
            //escribaTextoDeImpresion(comando);
            //formatopesos3 = String.format("%1$20s",formateo(variables.valorconsumo2));//String.Format("{0:$###.##0;($###.##0);Zero}", parseStringToDouble(variables.valorconsumo2));
            formatopesos3 = "$" + String.format("%1$10s", formateo(variables.valorconsumo2));//String.Format("{0:#,###,###;###,###;0.##}", parseStringToDouble(variables.valorconsumo2)).ToString().PadLeft(20, ' ');
            comando = "          " + formatopesos3;
            // Log.e("error",comando+" subsidio 1");
            escribaTextoDeImpresion(comando);
            // comando = "                                      " + formatopesos3;
            // escribaTextoDeImpresion(comando);
            totalreal = valorconsumo;
            formatopesos3 = "$" + String.format("%1$10s", formateo(totalreal));
            comando = formatopesos3;
            //  Log.e("error",comando+" subsidio 2");
            escribaTextoDeImpresion(comando);
            return;
        }
        comando = "                  " + variables.valorfacturadoenergia;
        if (contribucionenergia == 0) {

            variables.valorfacturadoenergia = String.format("%1$10s", formateo(variables.valorconsumo2));//String.Format("{0:#,###,###;###,###;0.##}", parseStringToDouble(variables.valorconsumo2)).ToString().PadLeft(20, ' ');
            formatocenergia = "            ";
            comando = " 00";

            // escribaTextoDeImpresion(comando);
            // comando = " ";
            totalreal = variables.valorconsumo2;
        } else if (contribucionenergia < 0) {
            contribucionenergia = -1 * contribucionenergia;
            totalreal = valorrango1 + contribucionenergia;
            formatocenergia = "$" + String.format("%1$10s", formateo(contribucionenergia));//String.Format("{0:#,###,###;###,###;0.##}", contribucionenergia).ToString().PadLeft(9, ' ');

            //porcentaje = contribucionenergia * 100 / totalreal;
            // Log.e("error", porcentaje + "-" + valorrango1 + "-" + contribucionenergia + " subsidio 1");

            variables.valorfacturadoenergia = "$" + String.format("%1$10s", formateo(variables.valorconsumo2));//String.Format("{0:#,###,###;###,###;0.##}", parseStringToDouble(variables.valorconsumo2)).ToString().PadLeft(20, ' ');

            comando = "" + infoClienteEntrada.gettablaEntradaClientes_MDDOR1SBSDIOCNTRBCION().substring(0,6) + " " + "  <" + formatocenergia + ">";//+"\r\n";
            totalreal = (variables.valorconsumo2) - contribucionenergia;

        } else {

            totalreal = valorrango1 - contribucionenergia;
            variables.valorfacturadoenergia = String.format("%1$10s", formateo(variables.valorconsumo2));//String.Format("{0:#,###,###;###,###;0.##}", parseStringToDouble(variables.valorconsumo2)).ToString().PadLeft(20, ' ');
            formatocenergia = String.format("%1$10s", formateo(contribucionenergia));//String.Format("{0:#,###,###;###,###;0.##}", contribucionenergia).ToString().PadLeft(9, ' ');

            //porcentaje = contribucionenergia * 100 / totalreal;

            // Log.e("error", porcentaje + "-" + formateo(valorrango1) + "-" + contribucionenergia + " subsidio 2");

            comando = "  " + infoClienteEntrada.gettablaEntradaClientes_MDDOR1SBSDIOCNTRBCION() + "    " + formatocenergia;// porcentaje.ToString("#,##0.00;(#,##0.00);Zero").Trim().PadLeft(46, ' ')
            totalreal = variables.valorconsumo2 + contribucionenergia;
        }

        escribaTextoDeImpresion(comando);
        //totalreal = variables.ejecutarAjusteUnidades(valorconsumo);//ejecutarAjusteEntero
        formatopesos3 = String.format("%1$10s", formateo(totalreal));//String.Format("{0:#,###,###;###,###;0.##}", totalreal).ToString().PadLeft(20, ' ');
        comando = "$" + formatopesos3;
        // Log.e("error", comando + " subsidio 4");
        escribaTextoDeImpresion(comando);
        escribeResumenTiempo(  "Finaliza Inicia proceso de Impresion de subsidio contribucion|" + VariablesGlobales.registroactual);
    }

    private int buscarPrecioFacturado() {

        int cantidadconceptos;
        int punteroconceptos;
        infoCobrosLiquidados.abrir_TablaCobrosRealizados(infoCobrosLiquidados.getArchivo_TablaCobrosRealizados());//ax nuevo

        punteroconceptos = parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_primercobro().trim());
        cantidadconceptos = parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_nrocobros().trim());
        variables.valorfacturadoenergia = "";
        variables.totalconsumoperiodo = 0;
        variables.valorconsumo = 0.00;
        while (cantidadconceptos > 0) {
            --cantidadconceptos;
            infoCobrosLiquidados.lectura_TablaCobrosRealizados(punteroconceptos++);
            if (infoCobrosLiquidados.gettablaCobrosRealizados_CONCEPTODECOBRO().equals(" 21  ")) {
                variables.totalconsumoperiodo = variables.totalconsumoperiodo
                        + Double.parseDouble(infoCobrosLiquidados.gettablaCobrosRealizados_VALOR().trim());
                variables.consumoenergiaactual = infoCobrosLiquidados.gettablaCobrosRealizados_VALOR();
                break;
            }
        }

        infoCobrosLiquidados.Cerrar_TablaCobrosRealizados();
        return 1;
    }

    int generarTextosHistoricoConsumos() {

        String fpesos = "";
        String fpesos1 = "";
        String comando = "";
        int apuntartarifa1 = 1;
        int apuntartarifa2 = 0;
        double VariableLiquidacionConsumo = 0;
        LiquidacionConsumo1 = "     ";//activa linea1
        LiquidacionConsumo2 = "     ";//reactiva 1
        LiquidacionConsumo3 = "     ."; // activa linea 4
        LiquidacionConsumo4 = "     .."; //reactiva linea 2
        variables.valorconsumo2 = 0;
//CAMBIAR PARA EBSA
        /*
        valorrango1 = 0;
        noavanzalinea = 0;
        variables.valorconsumo = 0;
        valorrango1 = 0;

        if (parseStringToDouble(infoClienteEntrada.gettablaEntradaClientes_NRODEMEDIDORES().trim()) > 1 || parseStringToInteger(infoMedidorEntrada.gettablaMedidorEntrada_Nroregistros().trim()) > 1) {


            infoMedidorEntrada.lectura_TablaMedidorEntrada(parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_PRIMERMEDIDOR()));


            VariablesGlobales.registroactual = parseStringToInteger(infoMedidorEntrada.gettablaMedidorEntrada_primerregistro());
            infoRegistroEntrada.lectura_TablaRegistroDeEntrada(VariablesGlobales.registroactual);
            apuntartarifa1 = parseStringToInteger(infoRegistroEntrada.gettablaRegistroDeEntrada_APUNTADORTARIFA());
            if (infoRegistroEntrada.gettablaRegistroDeEntrada_TIPOENERGIA().trim().equals("R")) {
                apuntartarifa2 = apuntartarifa1;

            }

            infoRegistroEntrada.lectura_TablaRegistroDeEntrada(VariablesGlobales.registroactual + 1);
            if (apuntartarifa2 == 0) {
                apuntartarifa2 = parseStringToInteger(infoRegistroEntrada.gettablaRegistroDeEntrada_APUNTADORTARIFA());

            } else
                apuntartarifa1 = parseStringToInteger(infoRegistroEntrada.gettablaRegistroDeEntrada_APUNTADORTARIFA());



        } else {
            VariablesGlobales.registroactual = parseStringToInteger(infoMedidorEntrada.gettablaMedidorEntrada_primerregistro());
            infoRegistroEntrada.lectura_TablaRegistroDeEntrada(VariablesGlobales.registroactual);
            apuntartarifa1 = parseStringToInteger(infoRegistroEntrada.gettablaRegistroDeEntrada_APUNTADORTARIFA());

        }

        tablaTarifa.lectura_TablaTarifas(apuntartarifa1);

        if (tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1().trim().length() == 0)
            tablaTarifa.settablaTarifas_Consumomaximo1("0");
        if (tablaTarifa.gettablaTarifas_CONSUMOMAXIMO2().trim().length() == 0)
            tablaTarifa.settablaTarifas_Consumomaximo2("0");


        if (parseStringToDouble(tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1()) == 0) {
            tablaTarifa.settablaTarifas_Valorunidadreferencia(reemplazarDatos(tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA()));


            double temp = (parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2()) * parseStringToDouble(tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA()));// + ((parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO()) - parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2())) * valorreferenciareactiva);
            temp = variables.ejecutarAjusteUnidades(temp);
            fpesos = String.format("%1$12s", formateo(temp));

            valorrango1 = (parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2())
                    * parseStringToDouble(tablaTarifa.gettablaTarifas_VALORUNIDAD1()));


            comando = tablaTarifa.gettablaTarifas_COSTO1().replace(".", ",") + "\r\n"
                    + tablaTarifa.gettablaTarifas_COSTO2().replace(".", ",") + "\r\n" +
                    tablaTarifa.gettablaTarifas_COSTO3().replace(".", ",")
                    + "\r\n" + tablaTarifa.gettablaTarifas_COSTO4().replace(".", ",")
                    + "\r\n" + tablaTarifa.gettablaTarifas_COSTO5().replace(".", ",") + " " + tablaTarifa.gettablaTarifas_COSTO6().replace(".", ",") + "  " + tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA().trim().replace(".", ",");

            escribaTextoDeImpresion(comando);

            variables.valorconsumo2 += Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2()) * Double.parseDouble(tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA());


            VariableLiquidacionConsumo = Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2()) * Double.parseDouble(tablaTarifa.gettablaTarifas_VALORUNIDAD1());

            fpesos = String.format("%1$10s", formateo(variables.ejecutarAjusteUnidades(VariableLiquidacionConsumo)));


            LiquidacionConsumo1 = String.format("%1$6s", parseStringToInteger(infoClienteSalida.gettablaClienteSalida_CONSUMO2().trim())) + " * " +
                    tablaTarifa.gettablaTarifas_VALORUNIDAD1().replace(".", ",") + " = " + fpesos;


            if (Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO3()) > 0) {
                tablaTarifa.lectura_TablaTarifas(apuntartarifa2);

                if (tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1().trim().length() == 0)
                    tablaTarifa.settablaTarifas_Consumomaximo1("0");

                if (tablaTarifa.gettablaTarifas_CONSUMOMAXIMO2().trim().length() == 0)
                    tablaTarifa.settablaTarifas_Consumomaximo2("0");

                variables.valorconsumo2 += Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO3()) * Double.parseDouble(tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA());

                double liquidaReactiva = Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO3()) * Double.parseDouble(tablaTarifa.gettablaTarifas_VALORUNIDAD1());

                fpesos = String.format("%1$10s", formateo(variables.ejecutarAjusteUnidades(liquidaReactiva)));

                LiquidacionConsumo2 = String.format("%1$6s", parseStringToInteger(infoClienteSalida.gettablaClienteSalida_CONSUMO3())).replace(".", ",") + " * " +
                        tablaTarifa.gettablaTarifas_VALORUNIDAD1().replace(".", ",") + " = " + fpesos;
            }

        } else if (parseStringToDouble(tablaTarifa.gettablaTarifas_CONSUMOMAXIMO2()) == 0) {

            if (parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO()) > parseStringToDouble(tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1())) {


                valorrango1 = parseStringToDouble(tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1()) * parseStringToDouble(tablaTarifa.gettablaTarifas_VALORUNIDAD1());

                double temp4 = parseStringToDouble(tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1()) * parseStringToDouble(tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA());

                fpesos = String.format("%1$12s", formateo(valorrango1));// String.Format("{0:#,###,###;###,###;0.##}", ....PadLeft(12, ' ')
                comando = String.format("%1$-7s", tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1().trim()) + "    " + String.format("%1$7s", tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA().trim()) + " = " + fpesos;//.PadRight(7, ' ') ....PadLeft(7, ' ')


                temp4 = (parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO()) - parseStringToDouble(tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1())) * parseStringToDouble(tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA());
                fpesos = String.format("%1$12s", formateo(valorrango1));//String.Format("{0:#,###,###;###,###;0.##}", .ToString().PadLeft(12, ' ');

                LiquidacionConsumo1 = String.format("%1$6s", parseStringToDouble(tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1().trim())).replace(".", ",") + " * " +
                        tablaTarifa.gettablaTarifas_VALORUNIDAD1().replace(".", ",") + " = " + fpesos;

                if (parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO3()) > parseStringToDouble(tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1())) {
                    tablaTarifa.lectura_TablaTarifas(apuntartarifa2);


                    if (tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1().trim().length() == 0)
                        tablaTarifa.settablaTarifas_Consumomaximo1("0");
                    if (tablaTarifa.gettablaTarifas_CONSUMOMAXIMO2().trim().length() == 0)
                        tablaTarifa.settablaTarifas_Consumomaximo2("0");

                    fpesos1 = String.format("%1$10s", (variables.ejecutarAjusteUnidades(valorrango1)));


                    LiquidacionConsumo2 = String.format("%1$10s", parseStringToDouble(tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1().trim())).replace(".", ",") + " * " +
                            tablaTarifa.gettablaTarifas_VALORUNIDAD1().replace(".", ",") + " = " + fpesos1;
                }
                fpesos1 = "$" + String.format("%1$10s", formateo((parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2()) - parseStringToDouble(tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1())) * parseStringToDouble(tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA())));
                comando = tablaTarifa.gettablaTarifas_COSTO1().replace(".", ",") + "\r\n"
                        + tablaTarifa.gettablaTarifas_COSTO2().replace(".", ",") + "\r\n" +
                        tablaTarifa.gettablaTarifas_COSTO3().replace(".", ",")
                        + "\r\n" + tablaTarifa.gettablaTarifas_COSTO4().replace(".", ",")
                        + "\r\n" + tablaTarifa.gettablaTarifas_COSTO5().replace(".", ",") + " " + tablaTarifa.gettablaTarifas_COSTO6().replace(".", ",") + "  " + tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA().trim().replace(".", ",");
                escribaTextoDeImpresion(comando);
                variables.valorconsumo2 += (parseStringToDouble(tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1()) * parseStringToDouble(tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA())) +
                        ((parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2()) -
                                parseStringToDouble(tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1())) *
                                parseStringToDouble(tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA()));

                double liquidaactiva = ((Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2()) -
                        Double.parseDouble(tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1())) *
                        Double.parseDouble(tablaTarifa.gettablaTarifas_VALORUNIDAD2()));

                fpesos1 = String.format("%1$10s", formateo(liquidaactiva));

                LiquidacionConsumo3 = String.format("%1$10s", parseStringToDouble(infoClienteSalida.getTablaClienteSalida_CONSUMOFACTURADO()) -
                        parseStringToDouble(tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1())).replace(".", ",") + " * " +
                        tablaTarifa.gettablaTarifas_VALORUNIDAD2().replace(".", ",") + " = " + fpesos1;

                if (parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO3()) > parseStringToDouble(tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1())) {
                    tablaTarifa.lectura_TablaTarifas(apuntartarifa2);

                    if (parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO3()) > parseStringToDouble(tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1())) {
                        if (tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1().trim().length() == 0)
                            tablaTarifa.settablaTarifas_Consumomaximo1("0");
                        if (tablaTarifa.gettablaTarifas_CONSUMOMAXIMO2().trim().length() == 0)
                            tablaTarifa.settablaTarifas_Consumomaximo2("0");


                        variables.valorconsumo2 += (parseStringToDouble(tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1()) * parseStringToDouble(tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA())) +
                                ((parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO3()) -
                                        parseStringToDouble(tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1())) *
                                        parseStringToDouble(tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA()));


                        double liquidaRactiva2 = (parseStringToDouble(tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1()) * parseStringToDouble(tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA())) +
                                ((parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO3()) -
                                        parseStringToDouble(tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1())) *
                                        parseStringToDouble(tablaTarifa.gettablaTarifas_VALORUNIDAD2()));

                        fpesos1 = String.format("%1$10s", formateo(liquidaRactiva2));
                        LiquidacionConsumo4 = String.format("%1$6s", (parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO3()) -
                                parseStringToDouble(tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1()))).replace(".", ",") + " * " +
                                tablaTarifa.gettablaTarifas_VALORUNIDAD2().replace(".", ",") + " = " + fpesos1;
                    } else {
                        if (tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1().trim().length() == 0)
                            tablaTarifa.settablaTarifas_Consumomaximo1("0");
                        if (tablaTarifa.gettablaTarifas_CONSUMOMAXIMO2().trim().length() == 0)
                            tablaTarifa.settablaTarifas_Consumomaximo2("0");


                        variables.valorconsumo2 += (parseStringToDouble(tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1()) * parseStringToDouble(tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA())) +
                                ((parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO3()) -
                                        parseStringToDouble(tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1())) *
                                        parseStringToDouble(tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA()));


                        double liquidaRactiva2 = (parseStringToDouble(tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1()) * parseStringToDouble(tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA())) +
                                ((parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO3()) -
                                        parseStringToDouble(tablaTarifa.gettablaTarifas_CONSUMOMAXIMO1())) *
                                        parseStringToDouble(tablaTarifa.gettablaTarifas_VALORUNIDAD1()));


                    }

                }


            } else {


                valorrango1 = parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO()) * parseStringToDouble(tablaTarifa.gettablaTarifas_VALORUNIDAD1());

                double temp6 = (parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO()) * parseStringToDouble(tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA()));
                fpesos = String.format("%1$18s", formateo(temp6));

                comando = tablaTarifa.gettablaTarifas_COSTO1().replace(".", ",") + "\r\n"
                        + tablaTarifa.gettablaTarifas_COSTO2().replace(".", ",") + "\r\n" +
                        tablaTarifa.gettablaTarifas_COSTO3().replace(".", ",")
                        + "\r\n" + tablaTarifa.gettablaTarifas_COSTO4().replace(".", ",")
                        + "\r\n" + tablaTarifa.gettablaTarifas_COSTO5().replace(".", ",") + " " + tablaTarifa.gettablaTarifas_COSTO6().replace(".", ",") + " " + tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA().trim().replace(".", ",");

                escribaTextoDeImpresion(comando);

                variables.valorconsumo2 = parseStringToDouble(infoClienteSalida.getTablaClienteSalida_CONSUMOFACTURADO()) * parseStringToDouble(tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA());


                double liquidaConsumo = parseStringToDouble(infoClienteSalida.getTablaClienteSalida_CONSUMOFACTURADO()) * parseStringToDouble(tablaTarifa.gettablaTarifas_VALORUNIDAD1());

                fpesos1 = String.format("%1$10s", formateo(variables.ejecutarAjusteUnidades(liquidaConsumo)));


                LiquidacionConsumo1 = String.format("%1$6s", (int) Double.parseDouble(infoClienteSalida.getTablaClienteSalida_CONSUMOFACTURADO().trim())).replace(".", ",") + " * " +
                        tablaTarifa.gettablaTarifas_VALORUNIDAD1().replace(".", ",") + " = " + fpesos1;

            }
        }
*/
        //aqui debemos mirar la reorganizacion de la impresion









        comando = infoClienteEntrada.gettablaEntradaClientes_CSTOG().substring(0,6) +" "+ "\r\n"+
                infoClienteEntrada.gettablaEntradaClientes_CSTOT().substring(0,6) +" " + "\r\n" +
                infoClienteEntrada.gettablaEntradaClientes_CSTOPR().substring(0,6) +" "
                + "\r\n" + infoClienteEntrada.gettablaEntradaClientes_CSTOD().substring(0,6) +" "
                + "\r\n" + infoClienteEntrada.gettablaEntradaClientes_CSTOR().substring(0,6) +" " + infoClienteEntrada.gettablaEntradaClientes_CSTOCV().substring(0,6) +" " + " "
                + infoClienteEntrada.gettablaEntradaClientes_CSTOCF().substring(0,6) +" "+ infoClienteEntrada.gettablaEntradaClientes_CU();

        escribaTextoDeImpresion(comando);

        return (1);
    }

    /***
     * Ax: Recibe un doble que devuelve como cadena formateada con punto separador de miles y sin decimales
     * @param d
     * @return ejemplo  12456089.13 --> 12.456.089
     */
    private String formateo(double d) {

        NumberFormat n = NumberFormat.getNumberInstance(Locale.GERMAN);
        n.setMaximumFractionDigits(0);
        return n.format(d);
    }

    private String reemplazarDatos(String cadena) { //Ax: se comenta porque hay error en parseo doble por las comas 12,23
        return cadena;
    }

    private void generarArchivoCabecera(String textoImpresion) {

        String nombreDeArchivoImpresion = VariablesGlobales.directorioactual + "/DatosDeEntrada/imprimir.txt";

        File file = new File(nombreDeArchivoImpresion);

        if (file.exists())
            file.delete();

        try {
            RandomAccessFile escribirImpresion = new RandomAccessFile(nombreDeArchivoImpresion, "rw");
            escribirImpresion.seek(escribirImpresion.length());
            escribirImpresion.writeBytes(textoImpresion);
            escribirImpresion.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean generarTextosMedidor() {

        int contador, apuntador, procesos;
        String causadesc;
        String comando = "";
        int TieneMasDeUnMedidor = 0;
        int MEDIDORESIMPRESOS = 0;
        escribeResumenTiempo(  "Inicia generar textos medidor|" + VariablesGlobales.registroactual);
        contador = parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Nrodemedidores().trim());
        apuntador = parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_primermedidor().trim());
        // Log.e("error2", "critica 9");
        medidorSalida.lectura_TablaContadorSalida(apuntador);
        infoMedidorEntrada.lectura_TablaMedidorEntrada(apuntador);
        procesos = 0;
        causadesc = " 0: Toma exitosa.";
        causadesc2 = " 0: Toma exitosa.";

        if (infoRegistroSalida.gettablaRegistroSalida_COMENTARIO2().equals("003"))
            causadesc = " 0: Toma con Novedad. Se Visitara";
        if (infoRegistroEntrada.gettablaRegistroDeEntrada_tipoDesviacion().trim().equals("P")) {
            causadesc = " 0: Toma EstudioPromediado";
        }
        if (infoRegistroSalida.gettablaRegistroSalida_COMENTARIO2().equals("001"))
            causadesc = " 0: Toma con Analisis Alto";
        if (infoRegistroSalida.gettablaRegistroSalida_COMENTARIO2().equals("002"))
            causadesc = " 0: Toma Subsistencia Alto";
        if (infoRegistroSalida.gettablaRegistroSalida_COMENTARIO2().equals("010") ||
                infoRegistroSalida.gettablaRegistroSalida_COMENTARIO2().equals("011") ||
                infoRegistroSalida.gettablaRegistroSalida_COMENTARIO2().equals("010"))
            causadesc = " 0: Toma Exitosa BAJO";


        if (contador > 1)
            TieneMasDeUnMedidor = contador;

        while (contador > 0) {
            procesos++;
            medidorSalida.lectura_TablaContadorSalida(apuntador);
            infoMedidorEntrada.lectura_TablaMedidorEntrada(apuntador);


            if (parseStringToInteger(infoMedidorEntrada.gettablaMedidorEntrada_idcortado().trim()) == 1) {
                if (parseStringToInteger(infoMedidorEntrada.gettablaMedidorEntrada_obsercorte()) > 0) {
                    if (anomaliaDeLectura.abrir_AnomaliaDeNoLectura(anomaliaDeLectura.getArchivo_AnomaliaDeNoLectura())) {
                        anomaliaDeLectura.buscarbinario_AnomaliaDeNoLectura(infoMedidorEntrada.gettablaMedidorEntrada_obsercorte().trim());
                        anomaliaDeLectura.Cerrar_AnomaliaDeNoLectura();
                    }

                    if (anomaliaDeLectura.getEncontro_AnomaliaDeNoLectura() > 0) {
                        if (TieneMasDeUnMedidor != 1)
                            causadesc = anomaliaDeLectura.getanomaliaDeNoLectura_CODIGO() + "  " + anomaliaDeLectura.getanomaliaDeNoLectura_DESCRIPCION().substring(0, 25);
                        else
                            causadesc2 = anomaliaDeLectura.getanomaliaDeNoLectura_CODIGO() + "  " + anomaliaDeLectura.getanomaliaDeNoLectura_DESCRIPCION().substring(0, 25);

                    } else {
                        if (!infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim().equals("0")) {
                            causadesc = infoMedidorEntrada.gettablaMedidorEntrada_obsercorte() + " Verificar tabla Causas";
                        }
                    }
                }
            } else {
                // Log.e("error2", "critica " + infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim());
                if (infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim().length() > 0) {
                    if (infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim().equals("40"))
                        TieneCausal40 = 1;

                    if (parseStringToInteger(infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim()) > 0) {
                        if (anomaliaDeLectura.abrir_AnomaliaDeNoLectura(anomaliaDeLectura.getArchivo_AnomaliaDeNoLectura())) {
                            anomaliaDeLectura.buscarbinario_AnomaliaDeNoLectura(infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA());
                            anomaliaDeLectura.Cerrar_AnomaliaDeNoLectura();
                        }

                        if (anomaliaDeLectura.getEncontro_AnomaliaDeNoLectura() > 0) {
                            if (TieneMasDeUnMedidor != 1)
                                causadesc = anomaliaDeLectura.getanomaliaDeNoLectura_CODIGO() + "  " + anomaliaDeLectura.getanomaliaDeNoLectura_DESCRIPCION().substring(0, 25);
                            else
                                causadesc2 = anomaliaDeLectura.getanomaliaDeNoLectura_CODIGO() + "  " + anomaliaDeLectura.getanomaliaDeNoLectura_DESCRIPCION().substring(0, 25);
                        } else {
                            if (!infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim().equals("0")) {
                                if (infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim().equals("28")) {
                                    causadesc = infoMedidorEntrada.gettablaMedidorEntrada_obsercorte() + "  PARA VERIFICAR      ";
                                } else {
                                    causadesc = infoMedidorEntrada.gettablaMedidorEntrada_obsercorte() + "  Verificar tabla     ";
                                }
                            }
                        }
                    }
                }
                TieneMasDeUnMedidor--;
            }

            int numeroregistros = 0;
            int TieneMasDeDosMedidores = 0;
            if (Integer.parseInt(infoClienteEntrada.gettablaEntradaClientes_Nrodemedidores()) > 1) {
                numeroregistros = Integer.parseInt(infoClienteEntrada.gettablaEntradaClientes_Nrodemedidores());
                TieneMasDeDosMedidores = 2;
            } else {
                infoRegistroSalida.lectura_TablaRegistroSalida(parseStringToInteger(infoMedidorEntrada.gettablaMedidorEntrada_primerregistro()));
                // Log.e("error3", "medidor 1 " + infoMedidorEntrada.gettablaMedidorEntrada_primerregistro());
                infoRegistroEntrada.lectura_TablaRegistroDeEntrada(parseStringToInteger(infoMedidorEntrada.gettablaMedidorEntrada_primerregistro()));
                numeroregistros = Integer.parseInt(infoMedidorEntrada.gettablaMedidorEntrada_Nroregistros().trim());
            }
            //para mejorar por el efecto de dos medidores
            int registrosverificados = 0;
            int imprimicontadores = 0;
            String ConsumoPromedioAImprimir = "";

            while (numeroregistros > 0) {

                if (infoRegistroEntrada.gettablaRegistroDeEntrada_tipoDesviacion().trim().equals("N")) {
                    ConsumoPromedioAImprimir = infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediocliente().trim();
                } else {
                    ConsumoPromedioAImprimir = infoRegistroEntrada.gettablaRegistroDeEntrada_promedioNormalizado().trim();
                }
                if (TieneMasDeDosMedidores > 1) {
                    infoMedidorEntrada.lectura_TablaMedidorEntrada(Integer.parseInt(infoClienteEntrada.gettablaEntradaClientes_primermedidor()) + registrosverificados);
                    infoRegistroSalida.lectura_TablaRegistroSalida(parseStringToInteger(infoMedidorEntrada.gettablaMedidorEntrada_primerregistro()));
                    // Log.e("error3", "medidor 2 " + infoMedidorEntrada.gettablaMedidorEntrada_primerregistro());
                    infoRegistroEntrada.lectura_TablaRegistroDeEntrada(parseStringToInteger(infoMedidorEntrada.gettablaMedidorEntrada_primerregistro()));

                } else {
                    infoRegistroSalida.lectura_TablaRegistroSalida(parseStringToInteger(infoMedidorEntrada.gettablaMedidorEntrada_primerregistro()) + registrosverificados);
                    // Log.e("error3", "medidor 3 " + infoMedidorEntrada.gettablaMedidorEntrada_primerregistro() + registrosverificados);
                    infoRegistroEntrada.lectura_TablaRegistroDeEntrada(parseStringToInteger(infoMedidorEntrada.gettablaMedidorEntrada_primerregistro()) + registrosverificados);
                }
                if (numeroregistros <= 2) {
                    if (Integer.parseInt(infoMedidorEntrada.gettablaMedidorEntrada_idcortado().trim()) == 0) {
                        if (infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA().trim().equals("")) {
                            if (!infoClienteSalida.gettablaClienteSalida_CONSUMO2().trim().equals("")) {
                                // Log.e("error",infoClienteSalida.gettablaClienteSalida_CONSUMO2()+" datos "+infoRegistroEntrada.gettablaRegistroDeEntrada_LECTURAANTERIOR());
                                infoRegistroSalida.settablaRegistroSalida_lecturatomada(String.valueOf(parseStringToInteger(infoRegistroEntrada.gettablaRegistroDeEntrada_Lecturaanterior()) + parseStringToInteger(infoClienteSalida.gettablaClienteSalida_CONSUMO2().trim())));
                                variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";DOS REGISTRADORES ;" + getPhoneDate() + "-" + getPhoneHour();
                                escribeResumenTiempo(variableResumenLiquidacion + " " + " DOS REGISTRADORES: Posicion Actual|" + VariablesGlobales.registroactual + " |" + (Integer.parseInt(infoMedidorEntrada.gettablaMedidorEntrada_primerregistro()) + registrosverificados));
                                infoRegistroSalida.escribir_TablaRegistroSalida(Integer.parseInt(infoMedidorEntrada.gettablaMedidorEntrada_primerregistro()) + registrosverificados);
                            }
                        }
                        lecturaTomada1 = infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA();
                    } else {
                        lecturaTomada1 = infoMedidorEntrada.gettablaMedidorEntrada_lecturacorte();
                    }
                    //if (Integer.parseInt(InfoMedidorEntrada.ECONTADNROREGISTROS)>1 && InfoRegistroEntrada.EREGISTTIPOENERGIA=="R")
                    if (infoRegistroEntrada.gettablaRegistroDeEntrada_tipoenergia().equals("R")) {
                        if (!infoMedidorEntrada.gettablaMedidorEntrada_Tipomedidor().trim().equals("X")) {
                            //hacer la forzada para ingresar las medidas de la reactiva en su posicion
                            if (infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim().length() == 0 || infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim().equals("7"))//90
                            {
                                //Modificado para validar lo del consumo multiple con activa y reactiva
                                //cambio para incluir la lectura anterior=   MedidorSalida.marca,MedidorSalida.numero,Convert.ToDouble(eregistr.lecturaanterior),Convert.ToDouble(sregistr.lecturatomada),
                                comando = String.format("%1$-12s", infoRegistroEntrada.gettablaRegistroDeEntrada_nrocontador().trim()) + "  " +
                                        infoRegistroEntrada.gettablaRegistroDeEntrada_MARCA().substring(0, 3) + "    " +
                                        infoRegistroEntrada.gettablaRegistroDeEntrada_tipoenergia() + "   " +
                                        infoRegistroEntrada.gettablaRegistroDeEntrada_Lecturaanterior().substring(0, 8) + "  " +
                                        String.format("%1$-8s", String.format("%.0f", Double.parseDouble(lecturaTomada1))) + " " +
                                        String.format("%1$-5s", ConsumoPromedioAImprimir.trim()) + "  " +
                                        infoRegistroEntrada.gettablaRegistroDeEntrada_Factormultipicacion().substring(0, 3) + "    " +
                                        String.format("%.0f", Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO3().trim()));
                                // Log.e("error", "comando rea0 " + comando);
                            } else {

                                comando = String.format("%1$-12s", infoRegistroEntrada.gettablaRegistroDeEntrada_nrocontador().trim()) + "  " +
                                        infoRegistroEntrada.gettablaRegistroDeEntrada_MARCA().substring(0, 3) + "    " +
                                        infoRegistroEntrada.gettablaRegistroDeEntrada_tipoenergia() + "   " +
                                        infoRegistroEntrada.gettablaRegistroDeEntrada_Lecturaanterior().substring(0, 8) + "  " +
                                        "0        " +
                                        String.format("%1$-5s", ConsumoPromedioAImprimir.trim()) + "  " +
                                        infoRegistroEntrada.gettablaRegistroDeEntrada_Factormultipicacion().substring(0, 3) + "    " +
                                        String.format("%.0f", Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO3().trim()));
                                // Log.e("error", "comando rea1 " + comando);

                            }
                        } else {
                            //if (infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().trim().equals("21") || infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().trim().equals("31"))
                            //    comando = "TIENE    MEDIDOR    RETIRADO                         " + String.format("%1$7s", "" + Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumoreal().trim()));//+"\r\n";
                            //else
                                comando = "TIENE    MEDIDOR    RETIRADO                         " + String.format("%1$7s", "" + Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumoreal()));//+"\r\n";

                        }
                        //Validar nuevo para cuenta de hospital
                        if (contador < 2) {
                            infoRegistroSalida.lectura_TablaRegistroSalida(parseStringToInteger(infoMedidorEntrada.gettablaMedidorEntrada_primerregistro()) + 1);
                            // Log.e("error3", "medidor 4 " + infoMedidorEntrada.gettablaMedidorEntrada_primerregistro() + 1);
                            infoRegistroEntrada.lectura_TablaRegistroDeEntrada(parseStringToInteger(infoMedidorEntrada.gettablaMedidorEntrada_primerregistro()) + 1);
                        }

                        if (Integer.parseInt(infoMedidorEntrada.gettablaMedidorEntrada_idcortado().trim()) == 0)
                            lecturaTomada1 = infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA();
                        else
                            lecturaTomada1 = infoMedidorEntrada.gettablaMedidorEntrada_lecturacorte();

                    } else {
                        //proceso para imprimir la activa antes de la reactiva
                        //										MessageBox.Show("Prueba4");
                        if (!infoMedidorEntrada.gettablaMedidorEntrada_Tipomedidor().trim().equals("X")) {
                            //hacer la forzada para ingresar las medidas de la reactiva en su posicion
                            if (infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim().length() == 0 || infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim().equals("7"))//90
                            {
                                //									MessageBox.Show("Prueba5 "+InfoClienteSalida.SREGISTCONSUMOTOMADO+"..."+lecttomada);
                                //cambio para incluir la lectura anterior=   MedidorSalida.marca,MedidorSalida.numero,Convert.ToDouble(eregistr.lecturaanterior),Convert.ToDouble(sregistr.lecturatomada),


                                comando = String.format("%1$-12s", infoRegistroEntrada.gettablaRegistroDeEntrada_nrocontador().trim()) + "  " +
                                        infoRegistroEntrada.gettablaRegistroDeEntrada_MARCA().substring(0, 3) + "    " +
                                        infoRegistroEntrada.gettablaRegistroDeEntrada_tipoenergia() + "   " +
                                        infoRegistroEntrada.gettablaRegistroDeEntrada_Lecturaanterior().substring(0, 8) + "  " +
                                        String.format("%1$-8s", String.format("%.0f", Double.parseDouble(lecturaTomada1))) + " " +
                                        String.format("%1$-5s", ConsumoPromedioAImprimir.trim()) + "  " +
                                        infoRegistroEntrada.gettablaRegistroDeEntrada_Factormultipicacion().substring(0, 3) + "    " +
                                        String.format("%.0f", Double.parseDouble(infoRegistroSalida.gettablaRegistroSalida_CONSUMOTOMADO().trim()));
                                // Log.e("error", "comando rea2 " + comando);

                                //									MessageBox.Show("Prueba5.1");
                            } else {
                                //									MessageBox.Show("Prueba6");
                                comando = String.format("%1$-12s", infoRegistroEntrada.gettablaRegistroDeEntrada_nrocontador().trim()) + "  " +
                                        infoRegistroEntrada.gettablaRegistroDeEntrada_MARCA().substring(0, 3) + "    " +
                                        infoRegistroEntrada.gettablaRegistroDeEntrada_tipoenergia() + "   " +
                                        infoRegistroEntrada.gettablaRegistroDeEntrada_Lecturaanterior().substring(0, 8) + "  " +
                                        "0       " + "  " +
                                        String.format("%1$-5s", ConsumoPromedioAImprimir.trim()) + "  " +
                                        infoRegistroEntrada.gettablaRegistroDeEntrada_Factormultipicacion().substring(0, 3) + "    " +
                                        String.format("%.0f", Double.parseDouble(infoRegistroSalida.gettablaRegistroSalida_CONSUMOTOMADO().trim()));
                                // Log.e("error", "comando rea3 " + comando);

                            }


                        } else {
                            //													MessageBox.Show("Prueba7");
                            //if (infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().trim().equals("21") || infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().trim().equals("31"))
                            //    comando = "TIENE    MEDIDOR    RETIRADO                         " + String.format("%1$8s", "" + Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumoreal().trim()));//+"\r\n";
                            //else
                                comando = "TIENE    MEDIDOR    RETIRADO                         " + String.format("%1$8s", "" + Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumoreal().trim()));//+"\r\n";
                        }

                    }

                }
                //	MessageBox.Show("Prueba8");

                if (MEDIDORESIMPRESOS < 2) {
                    // Log.e("error",comando+" med 1");
                    escribaTextoDeImpresion(comando);
                    MEDIDORESIMPRESOS++;
                }
                ++registrosverificados;
                --numeroregistros;
                imprimicontadores++;

            }

            if (MEDIDORESIMPRESOS < 2) {
                if (registrosverificados < 2) {
                    --contador;
                    ++apuntador;
                } else
                    contador = 0;
            } else
                contador = 0;


            //  --contador;
            //  ++apuntador;
        }

        //imprime la activa
        if (MEDIDORESIMPRESOS == 1)
            escribirLineaEnBlanco(1);
        else if (MEDIDORESIMPRESOS == 0) {
            escribirLineaEnBlanco(2);
        }

        ///proceso de inpresion de historia de consumos
        contador = Integer.parseInt(infoClienteEntrada.gettablaEntradaClientes_Nrodemedidores());
        apuntador = Integer.parseInt(infoClienteEntrada.gettablaEntradaClientes_primermedidor());
        procesos = 0;
        //MessageBox.Show("prueba 1");
        //descomentar
        escribeResumenTiempo(  "Fin cabecera y arranca los historiales|" + VariablesGlobales.registroactual);
        consumos_historicos();
        comando = mesesperiodos + "    " + causadesc;
        // Log.e("error",comando+" med 2");
        escribaTextoDeImpresion(comando);
        //nueva linea para los años del periodo
        comando = anosperiodos;
        // Log.e("error",comando+" med 3");
        escribaTextoDeImpresion(comando);
        if (Integer.parseInt(infoClienteEntrada.gettablaEntradaClientes_Nrodemedidores()) > 1)
            comando = consumosperiodos.replace(".", ",");//+ "    " + causadesc2
        else
            comando = consumosperiodos.replace(".", ",");

        // Log.e("error",comando+" med 4");
        escribaTextoDeImpresion(comando);
        escribeResumenTiempo(  "Termina los Historiales|" + VariablesGlobales.registroactual);

        return (true);
    }

    private void escribirLineaEnBlanco(int cnt) {

        String comando;
        while (cnt > 0) {
            cnt--;
            comando = " ";
            escribaTextoDeImpresion(comando);
        }
    }

    private void escribaTextoDeImpresion(String textoImpresion) {

        textoImpresion += "\r\n";

        String nombreDeArchivoImpresion = VariablesGlobales.directorioactual + "/DATOSDEENTRADA/IMPRIMIR.TXT";

//        File directory = new File("c:/facturacion");
//
//        if (directory.exists())
//            nombreDeArchivoImpresion = VariablesGlobales.directorioactual + "/DatosDeEntrada/"
//                    + infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ".txt";

        try {
            RandomAccessFile escribirImpresion = new RandomAccessFile(nombreDeArchivoImpresion, "rw");
            escribirImpresion.seek(escribirImpresion.length());
            escribirImpresion.writeBytes(textoImpresion);
            escribirImpresion.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void generarTextoInformacionBasica() {
        int dias_ahora;
        String fecha_tomada = "";
        fecha_vencimiento = "";
        fecha_corte = "";
        int salir = 0;

        String cadenaestrato12;
        double totalprestacion = 0;
        TieneCausal40 = 0;

        //preguntar por saltos de linea
        if (!infoClienteSalida.gettablaClienteSalida_INDFACTURACION().equals("L") || !infoClienteSalida.gettablaClienteSalida_INDFACTURACION().equals("")) {
            Calendar calendar = new GregorianCalendar();
            Date today = calendar.getTime();
            Log.e("error", "entra 1. ");
            //  dias_ahora = parseStringToInteger(infoClienteEntrada.getTablaEntradaClientes_diasvencimiento().trim().equals("") ? "0" : infoClienteEntrada.getTablaEntradaClientes_diasvencimiento().trim());// String.format("%1$2s", (""+parseStringToInteger(infoClienteEntrada.getTablaEntradaClientes_diasvencimiento().trim())));
            dias_ahora = dias;
            Log.e("error", "entra 1.1 " + dias);

         /*   if (dias_ahora == 0) {
                if (parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Bimestral().trim()) == 1)
                    dias_ahora = 7;
                else
                    dias_ahora = 20;
            }*/
            int dias_habiles = 0;
            Date fecha_Buscar = today;//sumarRestarDiasFecha(today, dias_ahora);
            while (salir == 0) {
                fecha_Buscar = sumarRestarDiasFecha(fecha_Buscar, 1);//antes dias_ahora
                Log.e("error", "entra 1.2 " + fecha_Buscar);

                Calendar calendar2 = Calendar.getInstance();

                calendar2.setTime(fecha_Buscar);

                String mes = "" + (calendar2.get(Calendar.MONTH) + 1);
                String dia = "" + calendar2.get(Calendar.DAY_OF_MONTH);

                if (dia.length() == 1) {
                    dia = "0" + dia;
                }

                if (mes.length() == 1) {
                    mes = "0" + mes;
                }

                fecha_tomada = "" + calendar2.get(Calendar.YEAR) + mes + dia;
                Log.e("error", "entra 1.3 " + fecha_tomada);

                // Log.e("error", fecha_Buscar + " fecha tomada " + fecha_tomada);
                if (!abrirFestivos(fecha_tomada)) {
                    dias_habiles++;
                    dias_ahora++;
                    if ((dias - 2) == dias_habiles)
                        salir = 1;
                } else {
                    // ++dias_ahora;
                }
                // }
            }

            Log.e("error", "entra 1.4 " + fecha_tomada);
            fecha_vencimiento = fecha_tomada.trim();
            salir = 0;
            dias_ahora = dias_ahora + 1;
            Date fecha_Buscar2 = fecha_Buscar;
            //    fecha_Buscar = sumarRestarDiasFecha(today, (dias_ahora-1));

            while (salir == 0) {

                fecha_Buscar = sumarRestarDiasFecha(fecha_Buscar, 1);
                Log.e("error", "entra 2.1 " + fecha_Buscar);
                // fecha_Buscar = sumarRestarDiasFecha(fecha_Buscar2, dias_ahora);

                Calendar calendar3 = Calendar.getInstance();
                calendar3.setTime(fecha_Buscar);

                String mes = "" + (calendar3.get(Calendar.MONTH) + 1);
                String dia = "" + calendar3.get(Calendar.DAY_OF_MONTH);

                if (dia.length() == 1) {
                    dia = "0" + dia;
                }
                if (mes.length() == 1) {
                    mes = "0" + mes;
                }

                fecha_tomada = "" + calendar3.get(Calendar.YEAR) + mes + dia;
                Log.e("error", "entra 2.2 " + fecha_tomada);

                // Log.e("error", "fecha tomada2 " + fecha_tomada);
                if (!abrirFestivos(fecha_tomada))
                    salir = 1;
                //else
                //  ++dias_ahora;
            }

            fecha_corte = fecha_tomada.trim();
            Log.e("error", "entra 2.3 " + fecha_tomada);

            infoClienteSalida.settablaClienteSalida_fechavence(fecha_vencimiento.substring(6) + "/" + fecha_vencimiento.substring(4, 6) + "/" + fecha_vencimiento.substring(0, 4));
            infoClienteSalida.settablaClienteSalida_fechacorte(fecha_corte.substring(6) + "/" + fecha_corte.substring(4, 6) + "/" + fecha_corte.substring(0, 4));
            infoClienteSalida.escribir_TablaClienteSalida(variables.clienteactual);
        } else {
            Log.e("error", "entra 3 ");
            fecha_vencimiento = infoClienteSalida.gettablaClienteSalida_FECHAVENCE().substring(6, 10) + infoClienteSalida.gettablaClienteSalida_FECHAVENCE().substring(3, 5) + infoClienteSalida.gettablaClienteSalida_FECHAVENCE().substring(0, 2);
            fecha_corte = infoClienteSalida.gettablaClienteSalida_FECHACORTE().substring(6, 10) + infoClienteSalida.gettablaClienteSalida_FECHACORTE().substring(3, 5) + infoClienteSalida.gettablaClienteSalida_FECHACORTE().substring(0, 2);

        }


        String cadenanivel;
        String comando;

        if (municipio.abrir_TablaMunicipios(municipio.getArchivo_TablaMunicipios())) {
            municipio.buscarbinario_TablaMunicipios(infoClienteEntrada.gettablaEntradaClientes_Municipio());//validar sino cambiarlo
            municipio.Cerrar_TablaMunicipios();
        }

        if (tablaClaseServicio.abrir_TablaClaseServicio(tablaClaseServicio.getArchivo_TablaClaseServicio())) {
            tablaClaseServicio.buscarSecuencial_TablaClaseServicio(infoClienteEntrada.gettablaEntradaClientes_Clasedeservicio());
            tablaClaseServicio.Cerrar_TablaClaseServicio();
        }

        if (variables.facturapreimpresa > 0)
            comando = tablaClaseServicio.gettablaClaseServicio_DESCRIPCION().substring(0, 15);//+"\r\n";
        else
            comando = tablaClaseServicio.gettablaClaseServicio_DESCRIPCION().substring(0, 17);//+"\r\n";

        escribaTextoDeImpresion(comando);
        //nuevo ajuste de la presentacion en la factura ojo
        ///incluir periodo en ves de telefono y quisas cambiar referencia por solo cuenta
        //esto es nuevo
        variables.amd = infoClienteEntrada.gettablaEntradaClientes_mes();//medidorSalida.gettablaContadorSalida_FECHALECTURA()
        Log.e("error", "basico 12-- " + variables.amd);
        if (variables.amd.trim().length() == 0) {
            variables.amd = medidorSalida.gettablaContadorSalida_FECHALECTURA();
        }

        convertirFormatoDeFecha(variables.amd, variables.fechahoy, mesenformato, "AMD");


        //comando = variables.mesenformato + "                     " + formatopesos2;//+"\r\n";
        //fin de lo nuevo

        if (infoClienteEntrada.gettablaEntradaClientes_Nrofactura().trim().length() < 15) {
          /*  comando = infoClienteEntrada.gettablaEntradaClientes_Nrofactura().substring(0, 5) + "-" + infoClienteEntrada.gettablaEntradaClientes_Nrofactura().substring(5, 15) + "\r\n" +
                      infoClienteEntrada.gettablaEntradaClientes_Nombre().substring(0, 30) + " Tel.:";//+"\r\n";*/
            comando = "       " + infoClienteEntrada.gettablaEntradaClientes_Nrofactura().substring(1, 15) + "\r\n" +
                    infoClienteEntrada.gettablaEntradaClientes_Nombre().substring(0, 30) + "             " + variables.mesenformato;//+"\r\n";
        } else {
         /*   comando = infoClienteEntrada.gettablaEntradaClientes_Nrofactura().substring(0, 6) + "-" + infoClienteEntrada.gettablaEntradaClientes_Nrofactura().substring(6, 15) + "\r\n" +
                    infoClienteEntrada.gettablaEntradaClientes_Nombre().substring(0, 30) + " Tel.:";//+"\r\n";*/
            comando = "       " + infoClienteEntrada.gettablaEntradaClientes_Nrofactura().substring(1, 15) + "\r\n" +
                    infoClienteEntrada.gettablaEntradaClientes_Nombre().substring(0, 30) + "             " + variables.mesenformato;//+"\r\n";

        }
        //MessageBox.Show("PRUEBA 3");
        escribaTextoDeImpresion(comando);

        if (variables.facturapreimpresa > 0)
            comando = infoClienteEntrada.gettablaEntradaClientes_Direccion().substring(0, 30);//+"\r\n";
        else
            comando = infoClienteEntrada.gettablaEntradaClientes_Direccion().substring(0, 30);//+"\r\n";
        escribaTextoDeImpresion(comando);


        switch (parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Estrato().trim())) {
            case 1:
                cadenaestrato12 = "1 B.Bajo";
                break;
            case 2:
                cadenaestrato12 = "2 Bajo  ";
                break;
            case 3:
                cadenaestrato12 = "3 M.Bajo";
                break;
            default:
            case 4:
                cadenaestrato12 = "4 Medio ";
                break;
            case 5:
                cadenaestrato12 = "5 M.Alto";
                break;
            case 6:
                cadenaestrato12 = "        ";
                break;
        }
//cambiar esto para que sepresente la factura aqui y reducir laruta

        String NroFactura = "";
        String RutaPrincipal = infoClienteEntrada.gettablaEntradaClientes_Ruta().substring(0, 3) + "-" + infoClienteEntrada.gettablaEntradaClientes_Ruta().substring(infoClienteEntrada.gettablaEntradaClientes_Ruta().length() - 9, infoClienteEntrada.gettablaEntradaClientes_Ruta().length());
        if (infoClienteEntrada.gettablaEntradaClientes_Nrofactura().trim().length() < 15) {
            NroFactura = infoClienteEntrada.gettablaEntradaClientes_Nrofactura().substring(0, 5) + "" + infoClienteEntrada.gettablaEntradaClientes_Nrofactura().substring(5, 15);

        } else {
            NroFactura = infoClienteEntrada.gettablaEntradaClientes_Nrofactura().substring(0, 6) + "" + infoClienteEntrada.gettablaEntradaClientes_Nrofactura().substring(6, 15);


        }

        if (variables.facturapreimpresa > 0) {
//            comando = infoClienteEntrada.gettablaEntradaClientes_Cuenta() + "  " + infoClienteEntrada.gettablaEntradaClientes_Ruta() + "   "
            //                  + municipio.gettablaMunicipios_DESCRIPCION().substring(0, 15) + "  " + infoClienteEntrada.gettablaEntradaClientes_Ciclo() + "     " + cadenaestrato12;//+"\r\n";
            comando = NroFactura + " " + RutaPrincipal + " "
                    + municipio.gettablaMunicipios_DESCRIPCION().substring(0, 15) + "  " + infoClienteEntrada.gettablaEntradaClientes_Ciclo() + "   " + cadenaestrato12;//+"\r\n";

        } else {
            //        comando = infoClienteEntrada.gettablaEntradaClientes_Cuenta() + "  " + infoClienteEntrada.gettablaEntradaClientes_Ruta() + "   "
            //              + municipio.gettablaMunicipios_DESCRIPCION().substring(0, 15) + "  " + infoClienteEntrada.gettablaEntradaClientes_Ciclo() + "     " + cadenaestrato12;//+"\r\n";
            comando = NroFactura + " " + RutaPrincipal + " "
                    + municipio.gettablaMunicipios_DESCRIPCION().substring(0, 15) + "  " + infoClienteEntrada.gettablaEntradaClientes_Ciclo() + "   " + cadenaestrato12;//+"\r\n";

        }
        escribaTextoDeImpresion(comando);

        if (!infoMedidorEntrada.gettablaMedidorEntrada_Tipomedidor().equals("X")) {

            //cambio para incluir la lectura anterior=   MedidorSalida.marca,MedidorSalida.numero,Convert.ToDouble(eregistr.lecturaanterior),Convert.ToDouble(sregistr.lecturatomada),

                comando = infoClienteEntrada.gettablaEntradaClientes_Nodoconexion() + "    " + infoClienteEntrada.gettablaEntradaClientes_Nodocircuito().substring(0, 8) + "        " + infoClienteEntrada.gettablaEntradaClientes_grupo().substring(0, 7) +
                        "                " + infoClienteEntrada.gettablaEntradaClientes_cargainstalada();//+"\r\n";

        } else {

                comando = infoClienteEntrada.gettablaEntradaClientes_Nodoconexion() + " " +
                        infoClienteEntrada.gettablaEntradaClientes_Nodocircuito().substring(0, 8) + "  " +
                        infoClienteEntrada.gettablaEntradaClientes_grupo() + "  " + //verificar
                        infoClienteEntrada.gettablaEntradaClientes_cargainstalada()
                        + "      " +
                        "00000000"+" " +
                        "00000000"+  " " +
                        "00000000";

        }

        escribaTextoDeImpresion(comando);
        //modificado victor
        /*CAMBIAR PARA EBSA
        if (tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA() == null || tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA().equals("null")) {
            tablaTarifa.lectura_TablaTarifas(parseStringToInteger(infoRegistroEntrada.gettablaRegistroDeEntrada_APUNTADORTARIFA().trim()));
        }
        totalprestacion = Double.parseDouble(tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA().trim().equals("") ? "0" : tablaTarifa.gettablaTarifas_VALORUNIDADREFERENCIA().trim());

        */
        //if (infoClienteEntrada.getTablaEntradaClientes_LugarAcuerdo().trim().equals("")) {
            comando = infoClienteEntrada.gettablaEntradaClientes_Desmaximoadmisible().replace(".", ",") + "   " + infoClienteEntrada.gettablaEntradaClientes_Descalculado().substring(0, 10).replace(".", ",") + "   "
                    + infoClienteEntrada.gettablaEntradaClientes_Fescalculado().substring(0, 10).replace(".", ",") + "  " + infoClienteEntrada.gettablaEntradaClientes_Fesmaximoadmisible().replace(".", ",") + "    " +
                    infoClienteEntrada.gettablaEntradaClientes_Fesmaximoadmisible().substring(0, 6).replace(".", ",") + " " + totalprestacion;//+"\r\n";
        /*} else {
            comando = "Acuerdo "+ "       " +
                    String.format("%1$8s", String.valueOf(totalprestacion));

        }*/
        escribaTextoDeImpresion(comando.replace(".", ","));

        Log.e("INFO", "variables.amd_1: " + variables.amd + " | variables.fechahoy:" + variables.fechahoy + " | mesenformato: " + mesenformato);
        convertirFormatoDeFecha(variables.amd, variables.fechahoy, mesenformato, "AMD");
        Log.e("INFO", "variables.amd_2: " + variables.amd + " | variables.fechahoy:" + variables.fechahoy + " | mesenformato: " + mesenformato);

        variables.nuevafecha = variables.amd.length() >= 6 ? variables.amd.substring(0, 6) : variables.amd;
        if (infoMedidorEntrada.gettablaMedidorEntrada_Fechalectanterior().trim().equals(""))
            infoMedidorEntrada.settablaMedidorEntrada_Fechalectanterior("01/01/2024");

        variables.amd = infoMedidorEntrada.gettablaMedidorEntrada_Fechalectanterior();

        convertirFormatoDeFecha(infoMedidorEntrada.gettablaMedidorEntrada_Fechalectanterior(), variables.fechahoy, mesenformato, "D/M/A");
        comando = variables.fechahoy.substring(0, 7) + variables.fechahoy.substring(9, 11) + " a ";
        // Log.e("error", "fecha lec anterior " + comando);

        variables.amd = medidorSalida.gettablaContadorSalida_FECHALECTURA().substring(6, 8) + "/" + medidorSalida.gettablaContadorSalida_FECHALECTURA().substring(4, 6) + "/" + medidorSalida.gettablaContadorSalida_FECHALECTURA().substring(0, 4);
        convertirFormatoDeFecha(variables.amd, fechaanteriorenformato, mesenformato, "D/M/A");
        comando = comando + variables.fechahoy.substring(0, 7) + variables.fechahoy.substring(9, 11);
        // Log.e("error", "fecha lec " + comando);

        int dias = (int) (variables.calcularNumerodeDias(infoMedidorEntrada.gettablaMedidorEntrada_Fechalectanterior().substring(6, 10) + infoMedidorEntrada.gettablaMedidorEntrada_Fechalectanterior().substring(3, 5) + infoMedidorEntrada.gettablaMedidorEntrada_Fechalectanterior().substring(0, 2)));
        // Log.e("error", "fecha dias " + variables.diashoy + "-" + dias);
        dias = variables.diashoy - dias;
        // Log.e("error", "fecha dias " + dias);

        //antes dias
        comando = comando + "      " + "  " + "           " +
                fecha_vencimiento.substring(6, 8) + "/" + fecha_vencimiento.substring(4, 6) + "/" + fecha_vencimiento.substring(0, 4) +
                "   " + fecha_corte.substring(6, 8) + "/" + fecha_corte.substring(4, 6) + "/" + fecha_corte.substring(0, 4);//+"\r\n";


        escribaTextoDeImpresion(comando);

    }

    int convertirFormatoDeFecha(String amd, String buff, String mesenformato, String tipo) { //Ax: todo :  inseguro,   revisar
        String mes;
        variables.mesenformato = "";

        try {

            if (tipo.equals("AMD") && amd.length() == 8) {
                //buff = variables.amd.substring(4, 6);
                mes = variables.amd.substring(4, 6);
            } else if (tipo.equals("AMD") && amd.length() > 8) {
                mes = amd.substring(3, 5);
            } else if (tipo.equals("D/M/A")) {
                mes = amd.substring(3, 5);
            } else {
                Log.e("error", "toma valor de [" + amd + "]");
                mes = variables.amd.substring(0, 2);//ax ojo!  mes puede ser 20
                Log.e("error", "toma valor en 2 de [" + mes + "]");
            }

            switch (parseStringToInteger(mes)) {
                case 1:
                    buff = "/ENE/";
                    variables.mesenformato = "ENERO/";
                    break;
                case 2:
                    buff = "/FEB/";
                    variables.mesenformato = "FEBRERO   /";
                    break;
                case 3:
                    buff = "/MAR/";
                    variables.mesenformato = "MARZO     /";
                    break;
                case 4:
                    buff = "/ABR/";
                    variables.mesenformato = "ABRIL     /";
                    break;
                case 5:
                    buff = "/MAY/";
                    variables.mesenformato = "MAYO      /";
                    break;
                case 6:
                    buff = "/JUN/";
                    variables.mesenformato = "JUNIO     /";
                    break;
                case 7:
                    buff = "/JUL/";
                    variables.mesenformato = "JULIO     /";
                    break;
                case 8:
                    buff = "/AGO/";
                    variables.mesenformato = "AGOSTO    /";
                    break;
                case 9:
                    buff = "/SEP/";
                    variables.mesenformato = "SEPTIEMBRE/";
                    break;
                case 10:
                    buff = "/OCT/";
                    variables.mesenformato = "OCTUBRE   /";
                    break;
                case 11:
                    buff = "/NOV/";
                    variables.mesenformato = "NOVIEMBRE /";
                    break;
                case 12:
                    buff = "/DIC/";
                    variables.mesenformato = "DICIEMBRE /";
                    break;
                default:
                    // buff = variables.amd.substring(4, 2);

                    return 0;
            }
        } catch (Exception ex) {
            ex.getStackTrace();
        }

        if (tipo.equals("AMD")) {
            if (amd.length() == 2) {
                //variables.fechahoy = variables.amd.substring(6, 8) + buff + variables.amd.substring(0, 4); // Se extraen los campos de annio, mes y dia de la variable amd
                variables.mesenformato = variables.mesenformato + "   " + infoClienteEntrada.gettablaEntradaClientes_anio(); // Se extraen los campos de annio, mes y dia de la variable amd
            } else {
                variables.fechahoy = variables.amd.substring(6, 8) + buff + variables.amd.substring(0, 4); // Se extraen los campos de annio, mes y dia de la variable amd
                variables.mesenformato = variables.mesenformato + "   " + variables.amd.substring(4, 6); // Se extraen los campos de annio, mes y dia de la variable amd

            }
        } else if (tipo.equals("D/M/A")) {
            variables.fechahoy = amd.substring(0, 2) + buff + amd.substring(6, 10); // Se extraen los campos de annio, mes y dia de la variable amd

            variables.mesenformato = variables.mesenformato + "   " + amd.substring(8, 10); // Se extraen los campos de annio, mes y dia de la variable amd

        } else {
            variables.fechahoy = variables.amd.substring(6, 8) + buff + variables.amd.substring(0, 4); // Se extraen los campos de annio, mes y dia de la variable amd

            mesenformato = mesenformato + "   " + variables.amd.substring(4, 6); // Se extraen los campos de annio, mes y dia de la variable amd
        }
        return 1;
    }


    private String IncluirDatosEnModoAutomatico() {//78000

        //MODO = "AUTO";
        int contar = 1;
        int controlliquidacion = 1;
        procesandoenvioenHilos_CPCAN = 1;//evita envios
        procesandoenvioenHilos = 1;//evita envios
        conexionGPRSActiva = 1;//evita envios
        salirse = true;//evita el dowork

        // Log.e("error", modoRetorna + " automatico " + MODO);

        try {
            File ArchivoResumen = new File(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/RESUMEN_REGISTRO.TXT");

            if (!ArchivoResumen.exists()) {

                File ArchivoRutaBackup = new File(VariablesGlobales.directorioactual + "/BACKUPLECTURAS" + nombreBackup + ".SDA");

                if (ArchivoRutaBackup.exists()) {

                    utils.CrearCopia(ArchivoRutaBackup.getAbsolutePath(), ArchivoResumen.getAbsolutePath());
                }
            }

            // Log.e("error", "nombre archivo " + ArchivoResumen);
            if (ArchivoResumen.exists()) {

                FileReader stream3 = new FileReader(ArchivoResumen);
                BufferedReader reader = new BufferedReader(stream3);
                String linea;

                abrirArchivosDeFacturacion();

                String txt_Lectura_Actual = "";
                String separador =";";
                while ((linea = reader.readLine()) != null) {

                    indicadorManual = 1;

                    if (MODO == "MANU" && !modoRetorna) {
                        MostrarAlertDialog("ALERTA!", "¿Continuar Proceso? \n Lectura Auto", "liquidacionautomatica");
                    }

                    if (txt_Lectura_Actual.trim().length() > 0)
                        controlliquidacion = parseStringToInteger(txt_Lectura_Actual.trim());
                    //   Log.e("error",contar+" data automatico "+controlliquidacion);
                    if (contar >= controlliquidacion) {

                        if (linea.contains(";")) {
                            separador = ";";
                        } else if (linea.contains("|")) {
                            separador = "\\|";
                        } else {
                            continue;
                            //   throw new Exception("Separador desconocido.");
                        }
                        String[] arr = linea.split(separador,-1);
                        /*PCODIGO = arr[4];

                        IDCONTADOR = arr[7];
                        LECTURAFACTURADA = arr[8];
                        CONSUMOLIQUIDADO = arr[17];
                        CAUSANOLECTURA = arr[11];*/

                 /*       PCODIGO = arr[4] ;//+ String.format("%1$3s", arr[7]).replace(" ", "0")
                        NROCONTADOR = arr[6];
                        IDCONTADOR = arr[7];
                        LECTURAFACTURADA = arr[8];//11
                        CONSUMOLIQUIDADO = arr[32];//31
                        CAUSANOLECTURA = arr[10];//12*/

                        PCODIGO = arr[4] ;//+ String.format("%1$3s", arr[7]).replace(" ", "0")
                        NROCONTADOR = arr[5];
                        IDCONTADOR = arr[6];
                        LECTURAFACTURADA = arr[7];//11
                        CAUSANOLECTURA = arr[8];//12
                        CONSUMOLIQUIDADO = arr[9];//31




//cambiar la busqueda por ahora
                        CuentaDiferencias = PCODIGO.trim(); //tomar la lectura y enviarla a proceso de liquidar factura

                        if ((!LECTURAFACTURADA.trim().equals("0") && !LECTURAFACTURADA.trim().equals("")) || !(String.format("%1$2s", CAUSANOLECTURA.trim()).replace(" ", "0")).equals("00")) {
                            // Log.e("error", PCODIGO + " encontroxxxx " + IDCONTADOR);
                           // infoRegistroSalida.BuscarSecuencialSregistrosConConcecutivo(PCODIGO, IDCONTADOR);
                            infoRegistroSalida.BuscarSecuencialSregistrosConConcecutivo2(PCODIGO, IDCONTADOR);//NROCONTADOR
                            if (infoRegistroSalida.encontro_TablaRegistroSalida > 0) {
                                //   Log.e("errora","encontro "+ infoRegistroSalida.encontro_TablaRegistroSalida);
                                // Log.e("error", "encontro0 " + infoRegistroSalida.encontro_TablaRegistroSalida);

                                VariablesGlobales.registroactual = infoRegistroSalida.encontro_TablaRegistroSalida;//Convert.ToInt32(InfoMedidorEntrada.ECONTADPRIMERREGISTRO);
                                leerInformacionUsuario(1);
                                visualizarInformacionCliente(0);

                                //primero si la cadena es mayor a cero se ejecuta

                                if (!LECTURAFACTURADA.trim().equals("0") || !(String.format("%1$2s", CAUSANOLECTURA.trim()).replace(" ", "0")).equals("00")) {
                                    if (LECTURAFACTURADA.trim().equals(""))
                                        LECTURAFACTURADA="0";

                                    txt_Lectura_Actual = parseStringToInteger(LECTURAFACTURADA.trim()) + "";
                                    variableTomarDatosLectura = false;

                                    if (validarEstadoRegistro(VariablesGlobales.registroactual, "") == 0) {

                                        if ((String.format("%1$2s", CAUSANOLECTURA.trim()).replace(" ", "0")).equals("00") ) {

                                            indicadorManual = 1;
                                            procesarLectura(1, 0, txt_Lectura_Actual.trim(), "XY");
                                            //    indicadorManual = 0;

                                        } else {

                                            if (!(String.format("%1$2s", CAUSANOLECTURA.trim()).replace(" ", "0")).equals("00") && !(String.format("%1$2s", CAUSANOLECTURA.trim()).replace(" ", "0")).equals("02")) {

                                                indicadorManual = 1;
                                                if (ingresarAnomaliaNoLectura(0, CAUSANOLECTURA.trim()) == 1) {
                                                    procesarLectura(0, 0, "", CAUSANOLECTURA.trim());
                                                }
                                                //     indicadorManual = 0;
                                            }
                                        }
                                    }

                                    variableTomarDatosLectura = false;
                                    txt_Lectura_Actual = "";
                                }
                            }
                        }
//                        else {
//                            txtInformeEscrito.setText("No Existe " + PCODIGO + "-" + IDCONTADOR + "-" + contar);
//                        }
                    }
                    ++contar;
                }
                reader.close();
                cerrarArchivosFacturacion();

            } else {
                //mensajeT("No Existe el Archivo: " + ArchivoResumen, msgLargo);
                return "INFO|No existe el archivo";
            }
        } catch (IOException ex) {
            // Log.e("error", "error automatico " + ex.getMessage());
            utils.Log(logfile, "[MenuDeLiquidacion]IncluirDatosEnModoAutomatico(); " + ex.getMessage());
            return "ERROR| Error -> " + ex.getMessage();
        }
        //mensajeT("Proceso Automatico concluido! ", msgLargo);
        salirse = false;//evita el dowork
        indicadorManual = 0;//evita envios
        procesandoenvioenHilos_CPCAN = 0;
        procesandoenvioenHilos = 0;
        conexionGPRSActiva = 0;
        return "INFO|OK";
    }

    private String IncluirDatosEnModoAutomatico2() {//78000

        //MODO = "AUTO";
        int contar = 1;
        int controlliquidacion = 1;
        procesandoenvioenHilos_CPCAN = 1;//evita envios
        procesandoenvioenHilos = 1;//evita envios
        conexionGPRSActiva = 1;//evita envios
        salirse = true;//evita el dowork

        Log.e("INFO", "IncluirDatosEnModoAutomatico2| paso 1 punto");

        try {
            File ArchivoResumen = new File(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/RESUMEN_ENVIOSGPRS.TXT");

            if (!ArchivoResumen.exists()) {

                File ArchivoRutaBackup = new File(VariablesGlobales.directorioactual + "/BACKUPLECTURAS" + nombreBackup + ".SDA");

                if (ArchivoRutaBackup.exists()) {

                    utils.CrearCopia(ArchivoRutaBackup.getAbsolutePath(), ArchivoResumen.getAbsolutePath());
                }
            }

            if (ArchivoResumen.exists()) {

                FileReader stream3 = new FileReader(ArchivoResumen);
                BufferedReader reader = new BufferedReader(stream3);
                String linea;

                abrirArchivosDeFacturacion();

                String txt_Lectura_Actual = "";

                Log.e("INFO", "IncluirDatosEnModoAutomatico2| paso 2 punto");

                String separador =";";



                while ((linea = reader.readLine()) != null) {

                    indicadorManual = 1;
                    /*if (MODO == "MANU" && !modoRetorna) {
                        MostrarAlertDialog("ALERTA!", "¿Continuar Proceso? \n Lectura Auto", "liquidacionautomatica_2");
                    }*/

                    if (linea.contains(";")) {
                        separador = ";";
                    } else if (linea.contains("|")) {
                        separador = "\\|";
                    } else {
                        continue;
                     //   throw new Exception("Separador desconocido.");
                    }

                    if (txt_Lectura_Actual.trim().length() > 0)
                        controlliquidacion = parseStringToInteger(txt_Lectura_Actual.trim());
                    //   Log.e("error",contar+" data automatico "+controlliquidacion);
                    if (contar >= controlliquidacion) {
                        // linea = linea.replace(";", "|");
                        //String[] arr = linea.split("[;|]");
                        String[] arr = linea.split(separador);

                        PCODIGO = arr[6] + String.format("%1$3s", arr[7]).replace(" ", "0");
                        NROCONTADOR = arr[8];
                        IDCONTADOR = arr[9];
                        LECTURAFACTURADA = arr[10];//11
                        CAUSANOLECTURA = arr[11];//12
                        CONSUMOLIQUIDADO = arr[32];//31


                        CuentaDiferencias = PCODIGO.trim(); //tomar la lectura y enviarla a proceso de liquidar factura

                        if ((!LECTURAFACTURADA.trim().equals("0") && !LECTURAFACTURADA.trim().equals("")) || !(String.format("%1$2s", CAUSANOLECTURA.trim()).replace(" ", "0")).equals("00")) {
                            infoRegistroSalida.BuscarSecuencialSregistrosConConcecutivo(PCODIGO, IDCONTADOR);

                            if (infoRegistroSalida.encontro_TablaRegistroSalida > 0) {
                                // Log.e("error", "encontro " + infoRegistroSalida.encontro_TablaRegistroSalida);
                                VariablesGlobales.registroactual = infoRegistroSalida.encontro_TablaRegistroSalida;//Convert.ToInt32(InfoMedidorEntrada.ECONTADPRIMERREGISTRO);

                                Log.e("INFO", "IncluirDatosEnModoAutomatico2| paso 3 punto");
                                leerInformacionUsuario(1);
                                Log.e("INFO", "IncluirDatosEnModoAutomatico2| paso 4 punto");
                                visualizarInformacionCliente(0);
                                Log.e("INFO", "IncluirDatosEnModoAutomatico2| paso 4 punto");

                                //primero si la cadena es mayor a cero se ejecuta

                                if (!LECTURAFACTURADA.trim().equals("0") || !(String.format("%1$2s", CAUSANOLECTURA.trim()).replace(" ", "0")).equals("00")) {
                                    txt_Lectura_Actual = parseStringToInteger(LECTURAFACTURADA.trim()) + "";
                                    variableTomarDatosLectura = false;

                                    if (validarEstadoRegistro(VariablesGlobales.registroactual, "") == 0) {

                                        if ((String.format("%1$2s", CAUSANOLECTURA.trim()).replace(" ", "0")).equals("00") || CAUSANOLECTURA.trim().equals("40")) {

                                            indicadorManual = 1;
                                            procesarLectura(1, 0, txt_Lectura_Actual.trim(), "XY");
                                            //    indicadorManual = 0;

                                        } else {

                                            if (!(String.format("%1$2s", CAUSANOLECTURA.trim()).replace(" ", "0")).equals("00")) {

                                                indicadorManual = 1;
                                                if (ingresarAnomaliaNoLectura(0, CAUSANOLECTURA.trim()) == 1) {
                                                    procesarLectura(0, 0, "", CAUSANOLECTURA.trim());
                                                }
                                                //     indicadorManual = 0;
                                            }
                                        }
                                    }

                                    variableTomarDatosLectura = false;
                                    txt_Lectura_Actual = "";
                                }
                            }
                        }
//                        else {
//                            txtInformeEscrito.setText("No Existe " + PCODIGO + "-" + IDCONTADOR + "-" + contar);
//                        }
                    }
                    ++contar;
                }
                reader.close();
                cerrarArchivosFacturacion();

            } else {
                //mensajeT("No Existe el Archivo: " + ArchivoResumen, msgLargo);
                return "INFO|No Existe el Archivo";

            }
        } catch (Exception ex) {
            Log.e("ERROR", "[MenuDeLiquidacion]IncluirDatosEnModoAutomatico(); " + ex.getMessage());
            // Log.e("error", "error automatico " + ex.getMessage());
            utils.Log(logfile, "[MenuDeLiquidacion]IncluirDatosEnModoAutomatico(); " + ex.getMessage());
            return "ERROR|Error -> " + ex.getMessage();
        }
        //mensajeT("Proceso Automatico concluido! ", msgLargo);
        salirse = false;//evita el dowork
        indicadorManual = 0;//evita envios
        procesandoenvioenHilos_CPCAN = 0;
        procesandoenvioenHilos = 0;
        conexionGPRSActiva = 0;
        return "INFO|OK";
    }

    private String IncluirDatosEnModoAutomatico3() {//78000

        //MODO = "AUTO";
        int contar = 1;
        int controlliquidacion = 1;
        procesandoenvioenHilos_CPCAN = 1;//evita envios
        procesandoenvioenHilos = 1;//evita envios
        conexionGPRSActiva = 1;//evita envios
        salirse = true;//evita el dowork

        Log.e("INFO", "IncluirDatosEnModoAutomatico2| paso 1 punto");

        try {
            File ArchivoResumen = new File(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/RESUMEN_TABLADB.TXT");

            if (!ArchivoResumen.exists()) {

                File ArchivoRutaBackup = new File(VariablesGlobales.directorioactual + "/BACKUPLECTURAS" + nombreBackup + ".SDA");

                if (ArchivoRutaBackup.exists()) {

                    utils.CrearCopia(ArchivoRutaBackup.getAbsolutePath(), ArchivoResumen.getAbsolutePath());
                }
            }

            if (ArchivoResumen.exists()) {

                FileReader stream3 = new FileReader(ArchivoResumen);
                BufferedReader reader = new BufferedReader(stream3);
                String linea;

                abrirArchivosDeFacturacion();

                String txt_Lectura_Actual = "";

                Log.e("INFO", "IncluirDatosEnModoAutomatico2| paso 2 punto");
                String separador =";";
                while ((linea = reader.readLine()) != null) {

                    indicadorManual = 1;
                    if (linea.contains(";")) {
                        separador = ";";
                    } else if (linea.contains("|")) {
                        separador = "\\|";
                    } else {
                        continue;
                        //   throw new Exception("Separador desconocido.");
                    }
                    if (txt_Lectura_Actual.trim().length() > 0)
                        controlliquidacion = parseStringToInteger(txt_Lectura_Actual.trim());
                    //   Log.e("error",contar+" data automatico "+controlliquidacion);
                    if (contar >= controlliquidacion) {
                        // linea = linea.replace(";", "|");
                        String[] arr = linea.split(separador);

                        PCODIGO = arr[6] + String.format("%1$3s", arr[7]).replace(" ", "0");
                        NROCONTADOR = arr[8];
                        IDCONTADOR = arr[9];
                        LECTURAFACTURADA = arr[11];//11
                        CAUSANOLECTURA = arr[12];//12
                        CONSUMOLIQUIDADO = arr[31];//31


                        CuentaDiferencias = PCODIGO.trim(); //tomar la lectura y enviarla a proceso de liquidar factura

                        if ((!LECTURAFACTURADA.trim().equals("0") && !LECTURAFACTURADA.trim().equals("")) || !(String.format("%1$2s", CAUSANOLECTURA.trim()).replace(" ", "0")).equals("00")) {
                            infoRegistroSalida.BuscarSecuencialSregistrosConConcecutivo(PCODIGO, IDCONTADOR);

                            if (infoRegistroSalida.encontro_TablaRegistroSalida > 0) {
                                // Log.e("error", "encontro " + infoRegistroSalida.encontro_TablaRegistroSalida);
                                VariablesGlobales.registroactual = infoRegistroSalida.encontro_TablaRegistroSalida;//Convert.ToInt32(InfoMedidorEntrada.ECONTADPRIMERREGISTRO);

                                Log.e("INFO", "IncluirDatosEnModoAutomatico2| paso 3 punto");
                                leerInformacionUsuario(1);
                                Log.e("INFO", "IncluirDatosEnModoAutomatico2| paso 4 punto");
                                visualizarInformacionCliente(0);
                                Log.e("INFO", "IncluirDatosEnModoAutomatico2| paso 4 punto");

                                //primero si la cadena es mayor a cero se ejecuta

                                if (!LECTURAFACTURADA.trim().equals("0") || !(String.format("%1$2s", CAUSANOLECTURA.trim()).replace(" ", "0")).equals("00")) {
                                    txt_Lectura_Actual = parseStringToInteger(LECTURAFACTURADA.trim()) + "";
                                    variableTomarDatosLectura = false;

                                    if (validarEstadoRegistro(VariablesGlobales.registroactual, "") == 0) {

                                        if ((String.format("%1$2s", CAUSANOLECTURA.trim()).replace(" ", "0")).equals("00")|| CAUSANOLECTURA.trim().equals("40")) {

                                            indicadorManual = 1;
                                            procesarLectura(1, 0, txt_Lectura_Actual.trim(), "XY");
                                            //    indicadorManual = 0;

                                        } else {

                                            if (!(String.format("%1$2s", CAUSANOLECTURA.trim()).replace(" ", "0")).equals("00")) {

                                                indicadorManual = 1;
                                                if (ingresarAnomaliaNoLectura(0, CAUSANOLECTURA.trim()) == 1) {
                                                    procesarLectura(0, 0, "", CAUSANOLECTURA.trim());
                                                }
                                                //     indicadorManual = 0;
                                            }
                                        }
                                    }

                                    variableTomarDatosLectura = false;
                                    txt_Lectura_Actual = "";
                                }
                            }
                        }
//                        else {
//                            txtInformeEscrito.setText("No Existe " + PCODIGO + "-" + IDCONTADOR + "-" + contar);
//                        }
                    }
                    ++contar;
                }
                reader.close();
                cerrarArchivosFacturacion();

            } else {
                //mensajeT("No Existe el Archivo: " + ArchivoResumen, msgLargo);
                return "INFO|No Existe el Archivo";

            }
        } catch (Exception ex) {
            Log.e("ERROR", "[MenuDeLiquidacion]IncluirDatosEnModoAutomatico(); " + ex.getMessage());
            // Log.e("error", "error automatico " + ex.getMessage());
            utils.Log(logfile, "[MenuDeLiquidacion]IncluirDatosEnModoAutomatico(); " + ex.getMessage());
            return "ERROR|Error -> " + ex.getMessage();
        }
        //mensajeT("Proceso Automatico concluido! ", msgLargo);
        salirse = false;//evita el dowork
        indicadorManual = 0;//evita envios
        procesandoenvioenHilos_CPCAN = 0;
        procesandoenvioenHilos = 0;
        conexionGPRSActiva = 0;
        return "INFO|OK";
    }
    private int ejecutarLiquidacionCuenta() {
        // Log.e("error", "entra a liquidar cuenta ");
        String[] consumos = new String[10];
        String[] rangos = new String[10];
        double consumoenergia = Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO().trim());
        variables.pesosenergia = 0;
        variables.pesosenergiareactiva = 0;
        subsidiocontribucionreactiva = 0;
        valorreferenciareactiva = 0;

        //liquidarConsumoConTarifas(int consumo, int consumoExtra, double valorReferenciaPorKwh,
        //String anio, String mes, String codtarifa,
        //double[] resultado) {

        if (variables.evaluarConServicio(infoMedidorEntrada.gettablaMedidorEntrada_Tipomedidor().trim()) == 1) {//LO CAMBIO PARA QUE EL SISTEMA REALICE SU TAREA
            if (apuntadortarifareactiva > 0) {  //se evalua la existencia si hay tarifa reactiva y se procesa

                if (Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO1().trim()) < Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2().trim()) / 2) {
                    consumoenergia = Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2().trim());

                    if (parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Bimestral().trim()) == 1) {
                        //ejecutarLiquidacionMes(consumoenergia, "", consumos, rangos, variables.pesosenergia, apuntadortarifaactiva, infoClienteSalida.gettablaClienteSalida_CONTRIBUCIONENERGIA());
                        liquidarConsumoConTarifas(consumoenergia, parseStringToInteger(infoRegistroEntrada.gettablaRegistroDeEntrada_VALORSUBSISTENCIA().trim()), Double.parseDouble(infoClienteEntrada.gettablaEntradaClientes_MDDOR1VLORKWH().trim()),
                                infoClienteEntrada.gettablaEntradaClientes_anio().trim(), infoRegistroEntrada.gettablaRegistroDeEntrada_MES().trim(), ""+apuntadortarifaactiva, variables.pesosenergia) ;

                    } else {
                        liquidarConsumoTrimestralConTarifas((int) consumoenergia, parseStringToInteger(infoRegistroEntrada.gettablaRegistroDeEntrada_VALORSUBSISTENCIA().trim()), parsearDecimal(infoClienteEntrada.gettablaEntradaClientes_MDDOR1VLORKWH().trim()),
                                infoClienteEntrada.gettablaEntradaClientes_anio().trim(), infoRegistroEntrada.gettablaRegistroDeEntrada_MES().trim(), ""+apuntadortarifaactiva) ;
                        //ejecutarLiquidacionTrimestral(consumoenergia, "", consumos, rangos, variables.pesosenergia, apuntadortarifaactiva, infoClienteSalida.gettablaClienteSalida_CONTRIBUCIONENERGIA()); // Antes ejecutarLiquidacionBimensual
                    }
                } else {


                    if (parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Bimestral().trim()) == 1) {
                        consumoenergia = Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO3().trim());

                        // ejecutarLiquidacionMesReactiva(consumoenergia, "", consumos, rangos, variables.pesosenergiareactiva, apuntadortarifareactiva, infoClienteSalida.gettablaClienteSalida_CONTRIBUCIONENERGIA());
                        liquidarConsumoConTarifasReactiva(consumoenergia, parseStringToInteger(infoRegistroEntrada.gettablaRegistroDeEntrada_VALORSUBSISTENCIA().trim()), parsearDecimal(infoClienteEntrada.gettablaEntradaClientes_MDDOR2VLORKWH().trim()),
                                infoClienteEntrada.gettablaEntradaClientes_anio().trim(), infoRegistroEntrada.gettablaRegistroDeEntrada_MES().trim(), ""+apuntadortarifareactiva, variables.pesosenergia);


                        consumoenergia = Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2().trim());

                        liquidarConsumoConTarifas(consumoenergia, parseStringToInteger(infoRegistroEntrada.gettablaRegistroDeEntrada_VALORSUBSISTENCIA().trim()), parsearDecimal(infoClienteEntrada.gettablaEntradaClientes_MDDOR1VLORKWH().trim()),
                                infoClienteEntrada.gettablaEntradaClientes_anio().trim(), infoRegistroEntrada.gettablaRegistroDeEntrada_MES().trim(), ""+apuntadortarifaactiva, variables.pesosenergia) ;
                        //ejecutarLiquidacionMes(consumoenergia, "", consumos, rangos, variables.pesosenergia, apuntadortarifaactiva, infoClienteSalida.gettablaClienteSalida_CONTRIBUCIONENERGIA());
                    }
                    else {
                        consumoenergia = Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO3().trim());

                        // ejecutarLiquidacionMesReactiva(consumoenergia, "", consumos, rangos, variables.pesosenergiareactiva, apuntadortarifareactiva, infoClienteSalida.gettablaClienteSalida_CONTRIBUCIONENERGIA());
                        liquidarConsumoTrimestralConTarifasReactiva((int) consumoenergia, parseStringToInteger(infoRegistroEntrada.gettablaRegistroDeEntrada_VALORSUBSISTENCIA().trim()), parsearDecimal(infoClienteEntrada.gettablaEntradaClientes_MDDOR2VLORKWH().trim()),
                                infoClienteEntrada.gettablaEntradaClientes_anio().trim(), infoRegistroEntrada.gettablaRegistroDeEntrada_MES().trim(), ""+apuntadortarifareactiva);


                        consumoenergia = Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2().trim());

                        liquidarConsumoTrimestralConTarifas((int) consumoenergia, parseStringToInteger(infoRegistroEntrada.gettablaRegistroDeEntrada_VALORSUBSISTENCIA().trim()), parsearDecimal(infoClienteEntrada.gettablaEntradaClientes_MDDOR1VLORKWH().trim()),
                                infoClienteEntrada.gettablaEntradaClientes_anio().trim(), infoRegistroEntrada.gettablaRegistroDeEntrada_MES().trim(), ""+apuntadortarifaactiva) ;

                        //ejecutarLiquidacionTrimestral(consumoenergia, "", consumos, rangos, variables.pesosenergia, apuntadortarifaactiva, infoClienteSalida.gettablaClienteSalida_CONTRIBUCIONENERGIA()); // Antes ejecutarLiquidacionBimensual
                    }
                }
            } else {

                if (parseStringToDouble(infoClienteEntrada.gettablaEntradaClientes_Bimestral()) == 1 || parseStringToDouble(infoClienteEntrada.gettablaEntradaClientes_Bimestral())== 3) {

                    if (parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Bimestral().trim()) == 1) {

                        liquidarConsumoConTarifas(consumoenergia, parseStringToInteger(infoRegistroEntrada.gettablaRegistroDeEntrada_VALORSUBSISTENCIA().trim()), parsearDecimal(infoClienteEntrada.gettablaEntradaClientes_MDDOR1VLORKWH().trim()),
                        infoClienteEntrada.gettablaEntradaClientes_anio().trim(), infoRegistroEntrada.gettablaRegistroDeEntrada_MES().trim(), ""+apuntadortarifaactiva, variables.pesosenergia) ;
                    }
                    else {
                        liquidarConsumoTrimestralConTarifas((int) consumoenergia, parseStringToInteger(infoRegistroEntrada.gettablaRegistroDeEntrada_VALORSUBSISTENCIA().trim()), parsearDecimal(infoClienteEntrada.gettablaEntradaClientes_MDDOR1VLORKWH().trim()),
                                infoClienteEntrada.gettablaEntradaClientes_anio().trim(), infoRegistroEntrada.gettablaRegistroDeEntrada_MES().trim(), ""+apuntadortarifaactiva) ;
                        //evaluar ejecutarLiquidacionTrimestral(consumoenergia, "", consumos, rangos, variables.pesosenergia, apuntadortarifaactiva, infoClienteSalida.gettablaClienteSalida_CONTRIBUCIONENERGIA()); // Antes ejecutarLiquidacionBimensual
                    }
                }
                else {
                    //es aqui donde debemos realizar algo para esos clientes el nuevo proceso para liquidar los dos medidoresojo
                    double acumulapesos = 0;
                    double AcumulaSubscontri = 0;
                    variables.pesosenergia = 0;

                    if (parseStringToDouble(infoClienteSalida.gettablaClienteSalida_VALOR1()) > 0) {
                        consumoenergia = parseStringToDouble(infoClienteSalida.gettablaClienteSalida_VALOR1());
                        if (parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Bimestral()) == 1) {
                            //ejecutarLiquidacionMes(consumoenergia, "", consumos, rangos, variables.pesosenergia, apuntadortarifaactiva, infoClienteSalida.gettablaClienteSalida_CONTRIBUCIONENERGIA());
                            liquidarConsumoConTarifas(consumoenergia, parseStringToInteger(infoRegistroEntrada.gettablaRegistroDeEntrada_VALORSUBSISTENCIA().trim()), parsearDecimal(infoClienteEntrada.gettablaEntradaClientes_MDDOR1VLORKWH().trim()),
                                    infoClienteEntrada.gettablaEntradaClientes_anio().trim(), infoRegistroEntrada.gettablaRegistroDeEntrada_MES().trim(), ""+apuntadortarifaactiva, variables.pesosenergia) ;

                        }
                        else {
                            liquidarConsumoTrimestralConTarifas((int) consumoenergia, parseStringToInteger(infoRegistroEntrada.gettablaRegistroDeEntrada_VALORSUBSISTENCIA().trim()), parsearDecimal(infoClienteEntrada.gettablaEntradaClientes_MDDOR1VLORKWH().trim()),
                                    infoClienteEntrada.gettablaEntradaClientes_anio().trim(), infoRegistroEntrada.gettablaRegistroDeEntrada_MES().trim(), ""+apuntadortarifaactiva) ;
                            //ejecutarLiquidacionTrimestral(consumoenergia, "", consumos, rangos, variables.pesosenergia, apuntadortarifaactiva, infoClienteSalida.gettablaClienteSalida_CONTRIBUCIONENERGIA()); // Antes ejecutarLiquidacionBimensual
                        }
                        acumulapesos += variables.pesosenergia;
                        AcumulaSubscontri += parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONTRIBUCIONENERGIA());
                    }

                    variables.pesosenergia = 0;

                    if (parseStringToDouble(infoClienteSalida.gettablaClienteSalida_VALOR2()) > 0) {
                        consumoenergia = parseStringToDouble(infoClienteSalida.gettablaClienteSalida_VALOR2());
                        if (parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Bimestral()) == 1) {
                            liquidarConsumoConTarifas(consumoenergia, parseStringToInteger(infoRegistroEntrada.gettablaRegistroDeEntrada_VALORSUBSISTENCIA().trim()), parsearDecimal(infoClienteEntrada.gettablaEntradaClientes_MDDOR1VLORKWH().trim()),
                                    infoClienteEntrada.gettablaEntradaClientes_anio().trim(), infoRegistroEntrada.gettablaRegistroDeEntrada_MES().trim(), ""+apuntadortarifaactiva, variables.pesosenergia) ;
                            //ejecutarLiquidacionMes(consumoenergia, "", consumos, rangos, variables.pesosenergia, apuntadortarifaactiva, infoClienteSalida.gettablaClienteSalida_CONTRIBUCIONENERGIA());
                        }
                        else {
                            liquidarConsumoTrimestralConTarifas((int) consumoenergia, parseStringToInteger(infoRegistroEntrada.gettablaRegistroDeEntrada_VALORSUBSISTENCIA().trim()), parsearDecimal(infoClienteEntrada.gettablaEntradaClientes_MDDOR1VLORKWH().trim()),
                                    infoClienteEntrada.gettablaEntradaClientes_anio().trim(), infoRegistroEntrada.gettablaRegistroDeEntrada_MES().trim(), ""+apuntadortarifaactiva) ;
                        //    ejecutarLiquidacionTrimestral(consumoenergia, "", consumos, rangos, variables.pesosenergia, apuntadortarifaactiva, infoClienteSalida.gettablaClienteSalida_CONTRIBUCIONENERGIA()); // Antes ejecutarLiquidacionBimensual
                        }
                        acumulapesos += variables.pesosenergia;
                        AcumulaSubscontri += parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONTRIBUCIONENERGIA());
                    }

                    variables.pesosenergia = 0;
                    subsidiocontribucionreactiva = 0;
                    if (parseStringToDouble(infoClienteSalida.gettablaClienteSalida_VALOR3()) > 0) {
                        consumoenergia = parseStringToDouble(infoClienteSalida.gettablaClienteSalida_VALOR3());
                        if (parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Bimestral()) == 1) {
                            liquidarConsumoConTarifas(consumoenergia, parseStringToInteger(infoRegistroEntrada.gettablaRegistroDeEntrada_VALORSUBSISTENCIA().trim()), parsearDecimal(infoClienteEntrada.gettablaEntradaClientes_MDDOR1VLORKWH().trim()),
                                    infoClienteEntrada.gettablaEntradaClientes_anio().trim(), infoRegistroEntrada.gettablaRegistroDeEntrada_MES().trim(), ""+apuntadortarifaactiva, variables.pesosenergia) ;
                            //ejecutarLiquidacionMes(consumoenergia, "", consumos, rangos, variables.pesosenergia, apuntadortarifaactiva, infoClienteSalida.gettablaClienteSalida_CONTRIBUCIONENERGIA());
                        }
                        else {
                            //ejecutarLiquidacionTrimestral(consumoenergia, "", consumos, rangos, variables.pesosenergia, apuntadortarifaactiva, infoClienteSalida.gettablaClienteSalida_CONTRIBUCIONENERGIA()); // Antes ejecutarLiquidacionBimensual
                           // liquidarConsumoConTarifas
                            liquidarConsumoTrimestralConTarifas((int) consumoenergia, parseStringToInteger(infoRegistroEntrada.gettablaRegistroDeEntrada_VALORSUBSISTENCIA().trim()), parsearDecimal(infoClienteEntrada.gettablaEntradaClientes_MDDOR1VLORKWH().trim()),
                                    infoClienteEntrada.gettablaEntradaClientes_anio().trim(), infoRegistroEntrada.gettablaRegistroDeEntrada_MES().trim(), ""+apuntadortarifaactiva) ;
                        }
                            acumulapesos += variables.pesosenergia;
                        AcumulaSubscontri += parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONTRIBUCIONENERGIA());

                    }
                    variables.pesosenergia = acumulapesos;

                    if (AcumulaSubscontri < 0)
                        infoClienteSalida.settablaClienteSalida_CONTRIBUCIONENERGIA((int) AcumulaSubscontri + "");
                    else
                        infoClienteSalida.settablaClienteSalida_CONTRIBUCIONENERGIA((int) AcumulaSubscontri + "");
            }
            }
        }

        variables.pesosenergia = variables.ejecutarAjusteUnidades(variables.pesosenergia);
        variables.pesosenergiareactiva = variables.ejecutarAjusteUnidades(variables.pesosenergiareactiva);
        variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";ejecutarReliquidacionConceptos ;" + getPhoneDate() + "-" + getPhoneHour();
        escribeResumenTiempo(variableResumenLiquidacion + " " + " Entra a ejecutarReliquidacionConceptos()");

        ejecutarReliquidacionConceptos(variables.pesosenergia);
        variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";ejecutarReliquidacionConceptos ;" + getPhoneDate() + "-" + getPhoneHour();
        escribeResumenTiempo(variableResumenLiquidacion + " " + " FIN a ejecutarReliquidacionConceptos()");
        return 1;
    }



    /**
     * NUCLEO de la busqueda/calculo de tarifa, SIN aplicar el ajuste al peso
     * (variables.ejecutarAjusteUnidades) todavia. Deja los valores RAW (con
     * decimales) en 'resultado', para que el llamador decida CUANDO redondear:
     *   - liquidarConsumoConTarifas (mensual) redondea de inmediato, porque
     *     solo hay un valor.
     *   - liquidarConsumoTrimestralConTarifas ACUMULA los 3 valores RAW de los
     *     3 meses primero, y redondea una sola vez al final -- asi no se
     *     pierden 1-2 pesos por redondear 3 veces por separado.
     *
     * IMPORTANTE: asume que la tabla de tarifas YA esta abierta (no abre ni
     * cierra el archivo) -- eso es responsabilidad de quien llama, para poder
     * abrir una sola vez en el caso trimestral en vez de 3.
     *
     * @param resultado arreglo de salida, tamano 2:
     *                    resultado[0] = valor de consumo RAW (sin redondear)
     *                    resultado[1] = subsidio/contribucion RAW (sin redondear)
     * @return 1 si encontro el consumo en la tabla; 0 si tuvo que usar el
     *         fallback (no encontrado)
     */
    private int calcularValorTarifaRaw(double consumo, double consumoSubsistencia, double valorReferenciaPorKwh,
                                       String anio, String mes, String codtarifa,
                                       double[] resultado, String Tipo) {

        resultado[0] = 0;
        resultado[1] = 0;

        if (consumoSubsistencia < 0) consumoSubsistencia = 0;

        long consumoParaTabla;
        long consumoExcedente;
        if (consumo > consumoSubsistencia) {
            consumoParaTabla = Math.round(consumoSubsistencia);
            consumoExcedente = Math.round(consumo - consumoSubsistencia);
        } else {
            consumoParaTabla = Math.round(consumo);
            consumoExcedente = 0;
        }

        if (infoClienteEntrada.gettablaEntradaClientes_MDDOR1SBSDIOCNTRBCION().trim().equals("")) {
            infoClienteEntrada.settablaEntradaClientes_MDDOR1SBSDIOCNTRBCION("0");
            infoClienteEntrada.settablaEntradaClientes_MDDOR2SBSDIOCNTRBCION("0");
        }

        if (consumoSubsistencia == 0) {
            double valorConsumoRaw = valorReferenciaPorKwh * consumo;
            double subsidioRaw =0;
            if (Tipo.equals("A"))
                subsidioRaw = parsearDecimal(infoClienteEntrada.gettablaEntradaClientes_MDDOR1SBSDIOCNTRBCION()) * valorConsumoRaw / 100;
            else
                subsidioRaw = parsearDecimal(infoClienteEntrada.gettablaEntradaClientes_MDDOR2SBSDIOCNTRBCION()) * valorConsumoRaw / 100;
            resultado[0] = valorConsumoRaw;
            resultado[1] = subsidioRaw;
            return 1;
        }

        String idBuscar = construirIdTarifa(anio, mes, codtarifa, consumoParaTabla);
        tablaTarifa.buscarbinario_TablaTarifas(idBuscar);

        if (tablaTarifa.encontro_TablaTarifas == 0) {
            Log.e("liquidacion", "No se encontro el consumo en TablaTarifas: " + idBuscar);

            double valorConsumoRaw = valorReferenciaPorKwh * consumo;

            String idMaximo = construirIdTarifa(anio, mes, codtarifa, 99999999);
            tablaTarifa.buscarbinario_TablaTarifas(idMaximo);

            double subsidioRaw;
            if (tablaTarifa.encontro_TablaTarifas > 0) {
                double diferencia = parsearDecimal(tablaTarifa.gettablaTarifas_VLORKWH().trim())
                        - parsearDecimal(tablaTarifa.gettablaTarifas_VLORRKWH().trim());
                subsidioRaw = consumo * diferencia;
            } else {
                if (Tipo.equals("A"))
                subsidioRaw = parsearDecimal(infoClienteEntrada.gettablaEntradaClientes_MDDOR1SBSDIOCNTRBCION())
                        * consumo * valorReferenciaPorKwh / 100;
                else
                    subsidioRaw = parsearDecimal(infoClienteEntrada.gettablaEntradaClientes_MDDOR2SBSDIOCNTRBCION())
                            * consumo * valorReferenciaPorKwh / 100;

            }

            resultado[0] = valorConsumoRaw;
            resultado[1] = subsidioRaw;
            return 0;
        }

        double valorNominal = parsearDecimal(tablaTarifa.gettablaTarifas_VLORKWH().trim());
        double valorReal = parsearDecimal(tablaTarifa.gettablaTarifas_VLORRKWH().trim());

        double subsidioRaw = valorNominal - valorReal;
        double valorConsumoRaw = valorReal + (valorReferenciaPorKwh * consumoExcedente);

        resultado[0] = valorConsumoRaw;
        resultado[1] = subsidioRaw;
        return 1;
    }


    /**
     * Liquidacion MENSUAL (la que ya tenias funcionando) -- ahora implementada
     * sobre calcularValorTarifaRaw, aplicando el ajuste al peso UNA vez, igual
     * que antes. El comportamiento externo no cambia para quien ya la usa.
     */
    private int liquidarConsumoConTarifas(double consumo, double consumoSubsistencia, double valorReferenciaPorKwh,
                                          String anio, String mes, String codtarifa,
                                          double resultadoNoUsado) {

        Pesos_energia_trimestra = 0;
        variables.pesosenergia = 0;
        int abriotarifas = 0;

        try {
            if (tablaTarifa.abrir_TablaTarifas(tablaTarifa.getArchivo_TablaTarifas())) {
                abriotarifas = 1;

                double[] raw = new double[2];
                int r = calcularValorTarifaRaw(consumo, consumoSubsistencia, valorReferenciaPorKwh,
                        anio, mes, codtarifa, raw,"A");

                variables.pesosenergia = variables.ejecutarAjusteUnidades(raw[0]);
                infoClienteSalida.settablaClienteSalida_CONTRIBUCIONENERGIA(
                        Integer.toString((int) variables.ejecutarAjusteUnidades(raw[1] + subsidiocontribucionreactiva)));

                tablaTarifa.Cerrar_TablaTarifas();
                return r;
            } else {
                mensajeT("No se puede abrir tabla de tarifas", msgMedio);
            }
        } catch (Exception e) {
            mensajeT("Problemas procesando Tabla Tarifas", msgMedio);
            String[] time = getTimeError().split("\\|");
            utils.Log(logfile, infoClienteEntrada.gettablaEntradaClientes_Cuenta() + "|" + Ciclo.substring(6, 9) + "|" +
                    infoClienteEntrada.gettablaEntradaClientes_mes() + "|" + infoClienteEntrada.gettablaEntradaClientes_anio() + "|" +
                    serialPDA + "|" + "[MenuDeLiquidacion]liquidarConsumoConTarifas()|" + e.getMessage() + "|" + time[0] + "|" + time[1] + "|");
            if (abriotarifas == 1) {
                tablaTarifa.Cerrar_TablaTarifas();
            }
            return -1;
        }

        return 1;
    }


    /**
     * Reparte un consumo trimestral total en 3 valores enteros por mes que
     * suman exactamente el total, repartiendo el residuo de la division entre
     * 3 de forma pareja.
     *   158 / 3 = 52 residuo 2  -> [53, 53, 52]
     *    91 / 3 = 30 residuo 1  -> [31, 30, 30]
     */
    private int[] repartirConsumoTrimestral(int consumoTotal) {
        int base = consumoTotal / 3;
        int residuo = consumoTotal % 3;
        int[] meses = new int[3];
        for (int i = 0; i < 3; i++) {
            meses[i] = base + (i < residuo ? 1 : 0);
        }
        return meses;
    }


    /**
     * Liquidacion TRIMESTRAL sobre calcularValorTarifaRaw: abre la tabla de
     * tarifas UNA sola vez, hace las 3 busquedas mensuales, ACUMULA los 3
     * valores sin redondear, y aplica el ajuste al peso UNA sola vez sobre el
     * total -- para no perder 1-2 pesos redondeando 3 veces por separado.
     *
     * @param consumoTotal          consumo TOTAL del trimestre (entero, kWh)
     * @param consumoSubsistencia   limite de subsistencia MENSUAL (igual para los 3 meses)
     * @param valorReferenciaPorKwh tarifa de referencia por kWh (igual para los 3 meses)
     * @param anios                 arreglo de 3: ano de cada uno de los 3 meses del trimestre
     * @param meses                 arreglo de 3: mes de cada uno de los 3 meses del trimestre
     * @param codtarifa             codigo de tarifa del cliente
     * @return 1 si los 3 meses encontraron tarifa en tabla; 0 si alguno uso
     *         fallback; -1 si hubo una excepcion real
     */
    private int liquidarConsumoTrimestralConTarifas(int consumoTotal, double consumoSubsistencia,
                                                    double valorReferenciaPorKwh,
                                                    String anios, String meses, String codtarifa) {//String[] anios, String[] meses, String codtarifa

        int abriotarifas = 0;
        PesosEnergiaConvenio=0;
        PesosEnergiaContrConvenio=0;

        int NumeroConvenios=0;
        try {
            if (!tablaTarifa.abrir_TablaTarifas(tablaTarifa.getArchivo_TablaTarifas())) {
                mensajeT("No se puede abrir tabla de tarifas", msgMedio);
                return -1;
            }

            if (!infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO1CNCPTO().trim().equals(""))
            {
                NumeroConvenios++;
            }
            if (!infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO2CNCPTO().trim().equals(""))
            {
                NumeroConvenios++;
            }
            if (!infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO3CNCPTO().trim().equals(""))
            {
                NumeroConvenios++;
            }

            abriotarifas = 1;
            if (NumeroConvenios==3 && consumoTotal<3)
            {
                NumeroConvenios = consumoTotal;
            }

            int[] consumoPorMes = repartirConsumoTrimestral(consumoTotal);
            double[] raw = new double[2];

            double totalRawValor = 0;
            double totalRawSubsidio = 0;
            int peorResultado = 1;

            for (int i = 0; i < 3; i++) {
                int r = calcularValorTarifaRaw(consumoPorMes[i], consumoSubsistencia, valorReferenciaPorKwh,
                        anios, meses, codtarifa, raw,"A");
                if (r < peorResultado)
                {
                    peorResultado = r;
                }
                totalRawValor += variables.ejecutarAjusteUnidades(raw[0]);
                totalRawSubsidio += raw[1];//variables.ejecutarAjusteUnidades() para igualar no ajustan el subs

                if (NumeroConvenios>0)
                {
                    if (i < NumeroConvenios)
                    {
                        PesosEnergiaConvenio = totalRawValor;
                        PesosEnergiaContrConvenio = totalRawSubsidio;
                    }
                }

            }

            PesosEnergiaContrConvenio= variables.ejecutarAjusteUnidades(PesosEnergiaContrConvenio);

            variables.pesosenergia = variables.ejecutarAjusteUnidades(totalRawValor);
            infoClienteSalida.settablaClienteSalida_CONTRIBUCIONENERGIA(
                    Integer.toString((int) variables.ejecutarAjusteUnidades(totalRawSubsidio + subsidiocontribucionreactiva)));

            tablaTarifa.Cerrar_TablaTarifas();
            return peorResultado;

        } catch (Exception e) {
            mensajeT("Problemas procesando Tabla Tarifas", msgMedio);
            String[] time = getTimeError().split("\\|");
            utils.Log(logfile, infoClienteEntrada.gettablaEntradaClientes_Cuenta() + "|" + Ciclo.substring(6, 9) + "|" +
                    infoClienteEntrada.gettablaEntradaClientes_mes() + "|" + infoClienteEntrada.gettablaEntradaClientes_anio() + "|" +
                    serialPDA + "|" + "[MenuDeLiquidacion]liquidarConsumoTrimestralConTarifas()|" + e.getMessage() + "|" + time[0] + "|" + time[1] + "|");
            if (abriotarifas == 1) {
                tablaTarifa.Cerrar_TablaTarifas();
            }
            return -1;
        }
    }

    private int liquidarConsumoTrimestralConTarifasReactiva(int consumoTotal, double consumoSubsistencia,
                                                    double valorReferenciaPorKwh,
                                                    String anios, String meses, String codtarifa) {//String[] anios, String[] meses, String codtarifa

        int abriotarifas = 0;
        PesosEnergiaReactConvenio=0;
        PesosEnergiaReactContrConvenio=0;

        int NumeroConvenios=0;
        try {
            if (!tablaTarifa.abrir_TablaTarifas(tablaTarifa.getArchivo_TablaTarifas())) {
                mensajeT("No se puede abrir tabla de tarifas", msgMedio);
                return -1;
            }

            if (!infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO1CNCPTO().trim().equals(""))
            {
                NumeroConvenios++;
            }
            if (!infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO2CNCPTO().trim().equals(""))
            {
                NumeroConvenios++;
            }
            if (!infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO3CNCPTO().trim().equals(""))
            {
                NumeroConvenios++;
            }

            abriotarifas = 1;
            if (NumeroConvenios>1 && consumoTotal<3)
            {
                NumeroConvenios = 1;
            }

            int[] consumoPorMes = repartirConsumoTrimestral(consumoTotal);
            double[] raw = new double[2];
            double totalRawValor = 0;
            double totalRawSubsidio = 0;
            int peorResultado = 1;
            for (int i = 0; i < 3; i++) {
                int r = calcularValorTarifaRaw(consumoPorMes[i], consumoSubsistencia, valorReferenciaPorKwh,
                        anios, meses, codtarifa, raw,"R");
                if (r < peorResultado) peorResultado = r;
                totalRawValor += variables.ejecutarAjusteUnidades(raw[0]);
                totalRawSubsidio += raw[1];//variables.ejecutarAjusteUnidades() para igualar no ajustan el subs

                if (NumeroConvenios>0)
                {
                    if (i < NumeroConvenios)
                    {
                        PesosEnergiaReactConvenio = totalRawValor;
                        PesosEnergiaReactContrConvenio = totalRawSubsidio;
                    }
                }

            }
            PesosEnergiaReactContrConvenio  = variables.ejecutarAjusteUnidades(PesosEnergiaReactContrConvenio);
            variables.pesosenergiareactiva = variables.ejecutarAjusteUnidades(totalRawValor);
            subsidiocontribucionreactiva = variables.ejecutarAjusteUnidades(totalRawSubsidio);
            tablaTarifa.Cerrar_TablaTarifas();
            return peorResultado;
        } catch (Exception e) {
            mensajeT("Problemas procesando Tabla Tarifas", msgMedio);
            String[] time = getTimeError().split("\\|");
            utils.Log(logfile, infoClienteEntrada.gettablaEntradaClientes_Cuenta() + "|" + Ciclo.substring(6, 9) + "|" +
                    infoClienteEntrada.gettablaEntradaClientes_mes() + "|" + infoClienteEntrada.gettablaEntradaClientes_anio() + "|" +
                    serialPDA + "|" + "[MenuDeLiquidacion]liquidarConsumoTrimestralConTarifas()Reactiva|" + e.getMessage() + "|" + time[0] + "|" + time[1] + "|");
            if (abriotarifas == 1) {
                tablaTarifa.Cerrar_TablaTarifas();
            }
            return -1;
        }
    }











    /**
     * Liquida el consumo de energia contra la nueva estructura de TablaTarifas
     * (id = ANO+MES+CODTARIFA+KWH; VLORKWH = valor pleno de referencia,
     * VLORRKWH = valor real con subsidio/contribucion ya aplicado por EBSA).
     *
     * UNA SOLA busqueda en la tabla: se busca (consumo - consumoExtra), es decir
     * el consumo real o el tope de subsistencia, lo que corresponda. El
     * consumoExtra (lo que pasa de la subsistencia) NO se busca en la tabla:
     * se liquida multiplicandolo directo por 'valorReferenciaPorKwh', que ya
     * viene calculado por el llamador.
     *
     * Ejemplo (el que diste): consumo=200, subsistencia=130 => consumoExtra=70.
     *   1) busca en tabla con 130 -> trae VLORKWH y VLORRKWH de esa fila.
     *   2) valorConsumoTotal = VLORKWH(130) + (70 * valorReferenciaPorKwh)
     *   3) subsidioContribucion = VLORRKWH(130) - VLORKWH(130)  [solo de esa fila]
     *
     * Reemplaza el nucleo de busqueda por tramos de la vieja ejecutarLiquidacionMes
     * (aquella logica de consumosporrangos/valoresporrangos ya no aplica).
     *
     * NOTA: esto NO reemplaza las reglas especiales de clase de servicio "IQ"
     * (division entre familias) ni "EM" (empleados, tope 280 kWh) que ya tenias
     * en ejecutarLiquidacionMes -- esas siguen siendo responsabilidad del
     * llamador.
     *
     * PARAMETROS QUE ME ENVIAS:
     * @param consumo               KWHCLIENTE -- consumo real total del cliente en el periodo (kWh)
     * @param consumoExtra          la parte de 'consumo' que ya calculaste como excedente
     *                              sobre la subsistencia; 0 si el cliente no la supera
     * @param valorReferenciaPorKwh la tarifa de referencia por kWh para liquidar el
     *                              excedente, YA CALCULADA por ti (no la busco yo)
     * @param anio                  ano del periodo a liquidar (necesario para armar el id)
     * @param mes                   mes del periodo a liquidar (necesario para armar el id)
     * @param codtarifa             codigo de tarifa del cliente
     * @param resultado             arreglo de salida, tamano 2 (esto no me lo envias, lo lleno yo):
     *                                resultado[0] = valor total de consumo liquidado
     *                                resultado[1] = valor de subsidio/contribucion
     *                                (positivo = subsidio a favor del cliente,
     *                                 negativo = contribucion a cargo del cliente)
     * @return 1 si liquido correctamente; 0 si no encontro el consumo buscado
     *         en la tabla (revisar LOGEVENTOS.LOG)
     */

    /*

   private int liquidarConsumoConTarifasanterior(double consumo, double consumoSubsistencia, double valorReferenciaPorKwh,

                                          String anio, String mes, String codtarifa,
                                          double resultado) {

        Pesos_energia_trimestra = 0;
        variables.pesosenergia = 0;//valor_total
        double subsidioContribucion = 0;
        int abriotarifas=0;

        try {
            if (tablaTarifa.abrir_TablaTarifas(tablaTarifa.getArchivo_TablaTarifas())) {
                abriotarifas=1;

                if (consumoSubsistencia < 0)
                    consumoSubsistencia = 0;

                // 'consumoSubsistencia' es el LIMITE de subsistencia (el tope), NO el excedente.
                // 'consumo' es el consumo real del cliente.
                //
                //  - Si consumo <= subsistencia: se busca en la tabla el consumo REAL,
                //    tal cual (ejemplo: real=100, subsistencia=130 -> se busca 100).
                //  - Si consumo  > subsistencia: se busca en la tabla el TOPE de
                //    subsistencia (no el consumo real), y lo que sobra por encima del
                //    tope (consumoExcedente) se liquida aparte, a la tarifa de
                //    referencia (ejemplo: real=200, subsistencia=130 -> se busca 130,
                //    y 70 de excedente se suma multiplicado por valorReferenciaPorKwh).
                    long consumoParaTabla;
                    long consumoExcedente;
                    if (consumo > consumoSubsistencia) {
                        consumoParaTabla = Math.round(consumoSubsistencia);
                        consumoExcedente = Math.round(consumo - consumoSubsistencia);
                    } else {
                        consumoParaTabla = Math.round(consumo);
                        consumoExcedente = 0;
                    }

                    if (consumoSubsistencia==0)
                    {

                        if (infoClienteEntrada.gettablaEntradaClientes_MDDOR1SBSDIOCNTRBCION().trim().equals("")) {
                            infoClienteEntrada.settablaEntradaClientes_MDDOR1SBSDIOCNTRBCION("0");
                            infoClienteEntrada.settablaEntradaClientes_MDDOR2SBSDIOCNTRBCION("0");
                        }
                        variables.pesosenergia = variables.ejecutarAjusteUnidades(valorReferenciaPorKwh * consumo);
                        subsidioContribucion = parsearDecimal(infoClienteEntrada.gettablaEntradaClientes_MDDOR1SBSDIOCNTRBCION()) * variables.pesosenergia/100;
                        infoClienteSalida.settablaClienteSalida_CONTRIBUCIONENERGIA(Integer.toString((int) variables.ejecutarAjusteUnidades(subsidioContribucion)));
                        tablaTarifa.Cerrar_TablaTarifas();
                        return 1;

                    }


                    String idBuscar = construirIdTarifa(anio, mes, codtarifa, consumoParaTabla);
                    tablaTarifa.buscarbinario_TablaTarifas(idBuscar);

                    if (tablaTarifa.encontro_TablaTarifas == 0) {
                        Log.e("liquidacion", "No se encontro el consumo en TablaTarifas: " + idBuscar);
                        if (infoClienteEntrada.gettablaEntradaClientes_MDDOR1SBSDIOCNTRBCION().trim().equals("")) {
                            infoClienteEntrada.settablaEntradaClientes_MDDOR1SBSDIOCNTRBCION("0");
                            infoClienteEntrada.settablaEntradaClientes_MDDOR2SBSDIOCNTRBCION("0");
                        }
                        // Fallback: si no aparece en la tabla, se liquida todo el consumo
                        // real a la tarifa de referencia, para no dejar al cliente en $0.
                        variables.pesosenergia = variables.ejecutarAjusteUnidades(valorReferenciaPorKwh * consumo);

                        //EL VALOR REAL DE LIQUIDACION DEL SUBSIDIO SON LAS QUE VIENEN EN LA TABLA AQUI PERO CREO QUE LAS DEMAS SON LAS QUE CONTRIBUYEN Y ESAS SE DEBEN DE BUSCAR O NO TOMAR LA
                        idBuscar = construirIdTarifa(anio, mes, codtarifa, 99999999);
                        tablaTarifa.buscarbinario_TablaTarifas(idBuscar);

                        if (tablaTarifa.encontro_TablaTarifas > 0) {
                            //aqui ceria restando una de la otra sacaria la diferencia y de ahi multiplicar para el la contribucion o la otra es multiplicar por el valor real
                            double diferencia = parsearDecimal(tablaTarifa.gettablaTarifas_VLORKWH().trim())-parsearDecimal(tablaTarifa.gettablaTarifas_VLORRKWH().trim());
                            subsidioContribucion = consumo * diferencia;

                        }
                        else
                        {
                            subsidioContribucion = parsearDecimal(infoClienteEntrada.gettablaEntradaClientes_MDDOR1SBSDIOCNTRBCION()) * consumo * valorReferenciaPorKwh/100;
                        }
                        //la otra es multiplicar por el multiplicar el valor directamente por ese valor de subcontri
                        //subsidioContribucion = parsearDecimal(infoClienteEntrada.gettablaEntradaClientes_MDDOR1SBSDIOCNTRBCION()) * consumo;

                        infoClienteSalida.settablaClienteSalida_CONTRIBUCIONENERGIA(Integer.toString((int) variables.ejecutarAjusteUnidades(subsidioContribucion)));

                        tablaTarifa.Cerrar_TablaTarifas();
                        return 0;

                    }
                    // VLORKWH  = valor NOMINAL / pleno, sin ajuste de subsidio/contribucion.
                    // VLORRKWH = valor REAL, el que el cliente realmente paga (ya con el
                    //            ajuste de subsidio/contribucion aplicado por EBSA).
                    double valorNominal = parsearDecimal(tablaTarifa.gettablaTarifas_VLORKWH().trim());
                    double valorReal = parsearDecimal(tablaTarifa.gettablaTarifas_VLORRKWH().trim());
                    // negativo = subsidiado, positivo = contribuyente
                    subsidioContribucion = variables.ejecutarAjusteUnidades(valorNominal - valorReal);
                    // Total a cobrar: el valor real (VLORRKWH) de la parte topada en
                    // subsistencia, mas el excedente (si lo hay) liquidado a la tarifa de
                    // referencia. Confirmado: VLORRKWH es el total real a cobrar; VLORKWH
                    // es el valor con el subsidio aplicado, usado solo para sacar la
                    // diferencia de subsidio/contribucion.
                    double valorConsumoTotal = valorReal + (valorReferenciaPorKwh * consumoExcedente);
                    variables.pesosenergia = variables.ejecutarAjusteUnidades(valorConsumoTotal);//resultado[0]
                    subsidiocontribucion = subsidiocontribucion + subsidiocontribucionreactiva;
                    infoClienteSalida.settablaClienteSalida_CONTRIBUCIONENERGIA(Integer.toString((int) variables.ejecutarAjusteUnidades(subsidioContribucion)));

                tablaTarifa.Cerrar_TablaTarifas();
            } else {
                mensajeT("No se puede abrir tabla de tarifas", msgMedio);
            }
            } catch (Exception e) {
                mensajeT("Problemas procesando Tabla Tarifas", msgMedio);
                String[] time = getTimeError().split("\\|");
                utils.Log(logfile, infoClienteEntrada.gettablaEntradaClientes_Cuenta() + "|" + Ciclo.substring(6, 9) + "|" + infoClienteEntrada.gettablaEntradaClientes_mes() + "|" + infoClienteEntrada.gettablaEntradaClientes_anio() + "|" +
                        serialPDA + "|" + "[MenuDeLiquidacion]AbrirLeeryCerrarTarifas()|" + e.getMessage() + "|" + time[0] + "|" + time[1] + "|");
            if (abriotarifas==1)
            {    tablaTarifa.Cerrar_TablaTarifas();}

                return -1;
        }

        return 1;

    }
*/

    private int liquidarConsumoConTarifasReactiva(double consumo, double consumoSubsistencia, double valorReferenciaPorKwh,
                                          String anio, String mes, String codtarifa,
                                          double resultado) {


        variables.pesosenergiareactiva = 0;
        subsidiocontribucionreactiva =0;
        Pesos_energia_trimestra = 0;

        int abriotarifas=0;

        try {
            if (tablaTarifa.abrir_TablaTarifas(tablaTarifa.getArchivo_TablaTarifas())) {
                abriotarifas=1;

                if (consumoSubsistencia < 0)
                    consumoSubsistencia = 0;
                long consumoParaTabla;
                long consumoExcedente;
                if (consumo > consumoSubsistencia) {
                    consumoParaTabla = Math.round(consumoSubsistencia);
                    consumoExcedente = Math.round(consumo - consumoSubsistencia);
                } else {
                    consumoParaTabla = Math.round(consumo);
                    consumoExcedente = 0;
                }

                if (consumoSubsistencia==0)
                {
                    if (infoClienteEntrada.gettablaEntradaClientes_MDDOR2SBSDIOCNTRBCION().trim().equals("")) {
                        infoClienteEntrada.settablaEntradaClientes_MDDOR2SBSDIOCNTRBCION("0");
                    }
                    variables.pesosenergiareactiva = variables.ejecutarAjusteUnidades(valorReferenciaPorKwh * consumo);
                    subsidiocontribucionreactiva = parsearDecimal(infoClienteEntrada.gettablaEntradaClientes_MDDOR2SBSDIOCNTRBCION()) * variables.pesosenergia/100;

                    tablaTarifa.Cerrar_TablaTarifas();
                    return 1;

                }


                String idBuscar = construirIdTarifa(anio, mes, codtarifa, consumoParaTabla);
                tablaTarifa.buscarbinario_TablaTarifas(idBuscar);

                if (tablaTarifa.encontro_TablaTarifas == 0) {
                    Log.e("liquidacion", "No se encontro el consumo en TablaTarifas: " + idBuscar);
                    if (infoClienteEntrada.gettablaEntradaClientes_MDDOR2SBSDIOCNTRBCION().trim().equals("")) {
                        infoClienteEntrada.settablaEntradaClientes_MDDOR2SBSDIOCNTRBCION("0");
                    }
                    // Fallback: si no aparece en la tabla, se liquida todo el consumo
                    // real a la tarifa de referencia, para no dejar al cliente en $0.
                    variables.pesosenergiareactiva = variables.ejecutarAjusteUnidades(valorReferenciaPorKwh * consumo);

                    //EL VALOR REAL DE LIQUIDACION DEL SUBSIDIO SON LAS QUE VIENEN EN LA TABLA AQUI PERO CREO QUE LAS DEMAS SON LAS QUE CONTRIBUYEN Y ESAS SE DEBEN DE BUSCAR O NO TOMAR LA
                    idBuscar = construirIdTarifa(anio, mes, codtarifa, 99999999);
                    tablaTarifa.buscarbinario_TablaTarifas(idBuscar);

                    if (tablaTarifa.encontro_TablaTarifas > 0) {
                        //aqui ceria restando una de la otra sacaria la diferencia y de ahi multiplicar para el la contribucion o la otra es multiplicar por el valor real
                        double diferencia = parsearDecimal(tablaTarifa.gettablaTarifas_VLORKWH().trim())-parsearDecimal(tablaTarifa.gettablaTarifas_VLORRKWH().trim());
                        subsidiocontribucionreactiva = consumo * diferencia;

                    }
                    else
                    {
                        subsidiocontribucionreactiva = parsearDecimal(infoClienteEntrada.gettablaEntradaClientes_MDDOR2SBSDIOCNTRBCION()) * consumo * valorReferenciaPorKwh/100;
                    }
                    tablaTarifa.Cerrar_TablaTarifas();
                    return 1;

                }
                double valorNominal = parsearDecimal(tablaTarifa.gettablaTarifas_VLORKWH().trim());
                double valorReal = parsearDecimal(tablaTarifa.gettablaTarifas_VLORRKWH().trim());
                subsidiocontribucionreactiva = variables.ejecutarAjusteUnidades(valorNominal - valorReal);
                double valorConsumoTotal = valorReal + (valorReferenciaPorKwh * consumoExcedente);
                variables.pesosenergiareactiva = variables.ejecutarAjusteUnidades(valorConsumoTotal);//resultado[0]

                tablaTarifa.Cerrar_TablaTarifas();
            } else {
                mensajeT("No se puede abrir tabla de tarifas", msgMedio);
            }
        } catch (Exception e) {
            mensajeT("Problemas procesando Tabla Tarifas", msgMedio);
            String[] time = getTimeError().split("\\|");
            utils.Log(logfile, infoClienteEntrada.gettablaEntradaClientes_Cuenta() + "|" + Ciclo.substring(6, 9) + "|" + infoClienteEntrada.gettablaEntradaClientes_mes() + "|" + infoClienteEntrada.gettablaEntradaClientes_anio() + "|" +
                    serialPDA + "|" + "[MenuDeLiquidacion]AbrirLeeryCerrarTarifas()|" + e.getMessage() + "|" + time[0] + "|" + time[1] + "|");
            if (abriotarifas==1)
            {    tablaTarifa.Cerrar_TablaTarifas();}

            return -1;
        }

        return 1;

    }


    /**
     * Convierte a double un valor que puede venir como texto con coma decimal
     * (ej. "130,5", igual que maneja comaPunto() en TablaTarifas). Usalo si
     * 'consumo' o 'consumoExtra' te llegan como String desde otra tabla, ANTES
     * de pasarlos a liquidarConsumoConTarifas.
     *
     * Ejemplo: double consumo = parsearDecimal(infoRegistro.getConsumoTexto());
     */
    private double parsearDecimal(String valor) {
        if (valor == null || valor.trim().isEmpty()) return 0;
        return Double.parseDouble(valor.trim().replace(",", "."));
    }
    /**
     * Arma el id compuesto (ANO+MES+CODTARIFA+KWH, 17 caracteres) que usa
     * buscarbinario_TablaTarifas, con ceros a la izquierda en cada segmento.
     */
    private String construirIdTarifa(String anio, String mes, String codtarifa, long kwh) {
        int anioNum = Integer.parseInt(anio.trim());
        int mesNum = Integer.parseInt(mes.trim());
        int codNum = Integer.parseInt(codtarifa.trim());
        return String.format("%04d%02d%03d%08d", anioNum, mesNum, codNum, kwh);
    }

    double acobrarAseo = 0;
    private int ejecutarReliquidacionConceptos(double pesosenergia) {

        double valorconcepto, pesosajuste;
        char[] actividad;
        int conceptoactual;
        int nroconceptos;
        int escriba;
        int tienenI = 0;
        double valor21 = 0;
        double valor51 = 0;
        //nuevas
        double valor55 = 0;
        double valor110 = 0;
        double valor505 = 0;
        double valor710 = 0;
        double CostoDistribucion = 0;
        double SumatoriaDifADeuda = 0;
        int liquidounafinanciacion = 0;

        double acobrar = 0;

        acobrarAseo = 0;

        double acobrarsindeuda = 0;
        variables.pesosenergia = pesosenergia;
        int codconcepto;

        int SeidentificaDeuda = 0;//nuevo
        // int liquidounafinanciacionSayco = 0;

        // int existe820 = 0;

        conceptoactual = parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_primercobro().trim());
        nroconceptos = parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_nrocobros().trim());
        variables.cuentamalajustada = 0;
        infoCobrosLiquidados.abrir_TablaCobrosRealizados(infoCobrosLiquidados.getArchivo_TablaCobrosRealizados());

        // Log.e("errora", " data nroC " + nroconceptos);
        while (nroconceptos > 0) {
            infoCobrosLiquidados.lectura_TablaCobrosRealizados(conceptoactual);

            //nuevo
            if (infoCobrosLiquidados.gettablaCobrosRealizados_VALOR().trim().contains("-")) {
                infoCobrosLiquidados.settablaCobrosRealizados_Valor(infoCobrosLiquidados.gettablaCobrosRealizados_VALOR().trim().replace("-", ""));

            }

            if (!infoCobrosLiquidados.gettablaCobrosRealizados_NROCUENTACLIENTE().equals(infoClienteEntrada.gettablaEntradaClientes_Cuenta())) {
                mensajeT("Problemas de Archivo de Entrada:\n" + "Los Conceptos de Cobro\n" + "no son de cliente\n" + "que se esta Facturando\n" + infoCobrosLiquidados.gettablaCobrosRealizados_NROCUENTACLIENTE() + "-" + infoClienteEntrada.gettablaEntradaClientes_Cuenta(), msgLargo);
            }

            if (infoCobrosLiquidados.gettablaCobrosRealizados_SALDOPENDIENTE().trim().length() == 0
                    || infoCobrosLiquidados.gettablaCobrosRealizados_SALDOPENDIENTE().trim().equals(".00"))
                infoCobrosLiquidados.settablaCobrosRealizados_saldopendiente("0,00");
            if (infoCobrosLiquidados.gettablaCobrosRealizados_VALOR().trim().length() == 0
                    || infoCobrosLiquidados.gettablaCobrosRealizados_VALOR().trim().equals(".00"))
                infoCobrosLiquidados.settablaCobrosRealizados_Valor("0");

            if (infoCobrosLiquidados.gettablaCobrosRealizados_INDICADORACTIVIDAD().equals("F"))
                valorconcepto = Double.parseDouble(infoCobrosLiquidados.gettablaCobrosRealizados_VALOR().trim());
            else
                valorconcepto = parseStringToDouble(infoCobrosLiquidados.gettablaCobrosRealizados_VALOR().trim());


            if (infoCobrosLiquidados.gettablaCobrosRealizados_INDICADORACTIVIDAD().equals("X")) //nuevo
                SeidentificaDeuda = 1;//nuevo


            valorconcepto = Double.parseDouble(infoCobrosLiquidados.gettablaCobrosRealizados_VALOR().trim());

            actividad = infoCobrosLiquidados.gettablaCobrosRealizados_INDICADORACTIVIDAD().toCharArray();
            escriba = 1;

            if (infoCobrosLiquidados.gettablaCobrosRealizados_CONCEPTODECOBRO().trim().equals(""))
                infoCobrosLiquidados.settablaCobrosRealizados_Conceptodecobro("0");

            codconcepto = parseStringToInteger(infoCobrosLiquidados.gettablaCobrosRealizados_CONCEPTODECOBRO().trim());
            // Log.e("errora", "concepto " + codconcepto);


            switch (codconcepto) {




                case 21:
                    valorconcepto = variables.pesosenergia;
                    valor_energia = variables.pesosenergia + "";
                    valor21 += valorconcepto;
                    // Log.e("error", valor_energia + " ***************concepto1-" + valorconcepto);
                    break;

                case 31:
                    valorconcepto = variables.pesosenergiareactiva;
                    valor21 += valorconcepto;
                    break;

                case 517://nuevo modelo para lo que ese subsidio contribucion veo es el 17 pero el 18 no existe como descripcion cual es realmente
                case 518://nuevo modelo para lo que ese subsidio contribucion
                case 18:
                case 17:
                    if (infoClienteSalida.gettablaClienteSalida_CONTRIBUCIONENERGIA().trim().equals(""))
                    {
                        valorconcepto = 0;
                    }
                    else
                    {

                        valorconcepto = Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONTRIBUCIONENERGIA().trim().replace("-",""));
                        if (Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONTRIBUCIONENERGIA().trim())>0 ) {
                             actividad[0] = 'C';
                        }
                         else {
                            actividad[0] = 'D';
                        }
                    }

                    valor21 += valorconcepto;
                    break;

                case 509:
                    //llenar las variables para que el sistema rehaga los valores de este concepto con
                    valorconcepto = Double.parseDouble(infoCobrosLiquidados.gettablaCobrosRealizados_VALOR().trim());
                    valorconcepto = variables.ejecutarAjusteUnidades(valorconcepto);
                    //el proceso debe de ser descontando este valor al sistem para que se ejecute lo que se requiere
                    if (valor21 <= valorconcepto) {
                        infoRegistroSalida.lectura_TablaRegistroSalida(VariablesGlobales.registroactual);
                        infoRegistroSalida.settablaRegistroSalida_informe("Devol Aprt Dp Guaj ");
                        variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";Concepto 509 ;" + getPhoneDate() + "-" + getPhoneHour();
                        escribeResumenTiempo(variableResumenLiquidacion + " " + " Concepto 509 : Posicion Actual|" + VariablesGlobales.registroactual);
                        infoRegistroSalida.escribir_TablaRegistroSalida(VariablesGlobales.registroactual);
                        infoCobrosLiquidados.settablaCobrosRealizados_saldopendiente(infoCobrosLiquidados.gettablaCobrosRealizados_VALOR());
                        infoCobrosLiquidados.escribir_TablaCobrosRealizados(conceptoactual);
                        //MessageBox.Show("Paso por aqui 1 "+valor21.ToString()+" <= "+valor_concepto.ToString());
                        valorconcepto = valor21;
                        valor21 = 0;
                        valorconcepto = variables.ejecutarAjusteUnidades(valorconcepto);
                    } else {
                        valor21 = valor21 - valorconcepto;
                    }
                    break;
                case 626:
                case 627://queda este fundamento de la 627 aplicando igual que el 626
                    //llenar las variables para que el sistema rehaga los valores de este concepto con
                    valorconcepto = Double.parseDouble(infoCobrosLiquidados.gettablaCobrosRealizados_VALOR().trim());
                    valorconcepto = variables.ejecutarAjusteUnidades(valorconcepto);
                    //el proceso debe de ser descontando este valor al sistem para que se ejecute lo que se requiere
                    if (valor21 <= valorconcepto) {
                        infoRegistroSalida.lectura_TablaRegistroSalida(VariablesGlobales.registroactual);
                        infoRegistroSalida.settablaRegistroSalida_informe("DESCUENTO MES FOES ");
                        variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";Concepto 626-627 ;" + getPhoneDate() + "-" + getPhoneHour();
                        escribeResumenTiempo(variableResumenLiquidacion + " " + " Concepto 626 : Posicion Actual|" + VariablesGlobales.registroactual);
                        infoRegistroSalida.escribir_TablaRegistroSalida(VariablesGlobales.registroactual);
                        infoCobrosLiquidados.settablaCobrosRealizados_saldopendiente(infoCobrosLiquidados.gettablaCobrosRealizados_VALOR());
                        infoCobrosLiquidados.escribir_TablaCobrosRealizados(conceptoactual);
                        //MessageBox.Show("Paso por aqui 1 "+valor21.ToString()+" <= "+valor_concepto.ToString());
                        valorconcepto = valor21;
                        valor21 = 0;
                        valorconcepto = variables.ejecutarAjusteUnidades(valorconcepto);
                    } else {
                        valor21 = valor21 - valorconcepto;
                    }
                    break;
                case 903://queda este fundamento de la 903 es como un pago anticipado hay que preguntar cual es su verdadero orden si llega a ser mayor que el valor de los kw consumidos
                    //llenar las variables para que el sistema rehaga los valores de este concepto con
                    valorconcepto = Double.parseDouble(infoCobrosLiquidados.gettablaCobrosRealizados_VALOR().trim());
                    valorconcepto = variables.ejecutarAjusteUnidades(valorconcepto);
                    //el proceso debe de ser descontando este valor al sistem para que se ejecute lo que se requiere
                    if (valor21 <= valorconcepto) {
                        infoRegistroSalida.lectura_TablaRegistroSalida(VariablesGlobales.registroactual);
                        infoRegistroSalida.settablaRegistroSalida_informe("PAGO ANTICIPADOMES ");
                        variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";Concepto 903;" + getPhoneDate() + "-" + getPhoneHour();
                        escribeResumenTiempo(variableResumenLiquidacion + " " + " Concepto 903 : Posicion Actual|" + VariablesGlobales.registroactual);
                        infoRegistroSalida.escribir_TablaRegistroSalida(VariablesGlobales.registroactual);
                        infoCobrosLiquidados.settablaCobrosRealizados_saldopendiente(infoCobrosLiquidados.gettablaCobrosRealizados_VALOR());
                        infoCobrosLiquidados.escribir_TablaCobrosRealizados(conceptoactual);
                        //MessageBox.Show("Paso por aqui 1 "+valor21.ToString()+" <= "+valor_concepto.ToString());
                        valorconcepto = valor21;
                        valor21 = 0;
                        valorconcepto = variables.ejecutarAjusteUnidades(valorconcepto);
                    } else {
                        valor21 = valor21 - valorconcepto;
                    }
                    break;
                case 980://queda este fundamento de la 980 es como un pago anticipado hay que preguntar cual es su verdadero orden si llega a ser mayor que el valor de los kw consumidos
                    //llenar las variables para que el sistema rehaga los valores de este concepto con
                    valorconcepto = Double.parseDouble(infoCobrosLiquidados.gettablaCobrosRealizados_VALOR().trim());
                    valorconcepto = variables.ejecutarAjusteUnidades(valorconcepto);
                    //el proceso debe de ser descontando este valor al sistem para que se ejecute lo que se requiere
                    if (valor21 <= valorconcepto) {
                        infoRegistroSalida.lectura_TablaRegistroSalida(VariablesGlobales.registroactual);
                        infoRegistroSalida.settablaRegistroSalida_informe("PAGO 980 FOES ");
                        variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";Concepto 980;" + getPhoneDate() + "-" + getPhoneHour();
                        escribeResumenTiempo(variableResumenLiquidacion + " " + " Concepto 980 : Posicion Actual|" + VariablesGlobales.registroactual);
                        infoRegistroSalida.escribir_TablaRegistroSalida(VariablesGlobales.registroactual);
                        infoCobrosLiquidados.settablaCobrosRealizados_saldopendiente(infoCobrosLiquidados.gettablaCobrosRealizados_VALOR());
                        infoCobrosLiquidados.escribir_TablaCobrosRealizados(conceptoactual);
                        //MessageBox.Show("Paso por aqui 1 "+valor21.ToString()+" <= "+valor_concepto.ToString());
                        valorconcepto = valor21;
                        valor21 = 0;
                        valorconcepto = variables.ejecutarAjusteUnidades(valorconcepto);
                    } else {
                        valor21 = valor21 - valorconcepto;
                    }
                    break;
                case 988://queda este fundamento de la 980 es como un pago anticipado hay que preguntar cual es su verdadero orden si llega a ser mayor que el valor de los kw consumidos
                    //llenar las variables para que el sistema rehaga los valores de este concepto con
                    valorconcepto = Double.parseDouble(infoCobrosLiquidados.gettablaCobrosRealizados_VALOR().trim());
                    valorconcepto = variables.ejecutarAjusteUnidades(valorconcepto);
                    //el proceso debe de ser descontando este valor al sistem para que se ejecute lo que se requiere
                    if (valor21 <= valorconcepto) {
                        infoRegistroSalida.lectura_TablaRegistroSalida(VariablesGlobales.registroactual);
                        infoRegistroSalida.settablaRegistroSalida_informe("PAGO 988 FOES ");
                        variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";Concepto 988;" + getPhoneDate() + "-" + getPhoneHour();
                        escribeResumenTiempo(variableResumenLiquidacion + " " + " Concepto 988 : Posicion Actual|" + VariablesGlobales.registroactual);
                        infoRegistroSalida.escribir_TablaRegistroSalida(VariablesGlobales.registroactual);
                        infoCobrosLiquidados.settablaCobrosRealizados_saldopendiente(infoCobrosLiquidados.gettablaCobrosRealizados_VALOR());
                        infoCobrosLiquidados.escribir_TablaCobrosRealizados(conceptoactual);
                        //MessageBox.Show("Paso por aqui 1 "+valor21.ToString()+" <= "+valor_concepto.ToString());
                        valorconcepto = valor21;
                        valor21 = 0;
                        valorconcepto = variables.ejecutarAjusteUnidades(valorconcepto);
                    } else {
                        valor21 = valor21 - valorconcepto;
                    }
                    break;
                case 992:
                    //llenar las variables para que el sistema rehaga los valores de este concepto con
                    valorconcepto = Double.parseDouble(infoCobrosLiquidados.gettablaCobrosRealizados_VALOR().trim());
                    valorconcepto = variables.ejecutarAjusteUnidades(valorconcepto);
                    //el proceso debe de ser descontando este valor al sistem para que se ejecute lo que se requiere
                    if (valor21 <= valorconcepto) {
                        infoRegistroSalida.lectura_TablaRegistroSalida(VariablesGlobales.registroactual);
                        infoRegistroSalida.settablaRegistroSalida_informe("DIU - FIU Periodos Anteriores ");
                        variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";Concepto 992 ;" + getPhoneDate() + "-" + getPhoneHour();
                        escribeResumenTiempo(variableResumenLiquidacion + " " + " Concepto 992 : Posicion Actual|" + VariablesGlobales.registroactual);
                        infoRegistroSalida.escribir_TablaRegistroSalida(VariablesGlobales.registroactual);
                        infoCobrosLiquidados.settablaCobrosRealizados_saldopendiente(infoCobrosLiquidados.gettablaCobrosRealizados_VALOR());
                        infoCobrosLiquidados.escribir_TablaCobrosRealizados(conceptoactual);
                        //MessageBox.Show("Paso por aqui 1 "+valor21.ToString()+" <= "+valor_concepto.ToString());
                        valorconcepto = valor21;
                        valor21 = 0;
                        valorconcepto = variables.ejecutarAjusteUnidades(valorconcepto);
                    } else {
                        valor21 = valor21 - valorconcepto;
                    }
                    break;

                case 110:
                    valor110 += valorconcepto;
                    break;

                case 607:
                case 608:

                    if (acobrar < 0) {
                        acobrar = 0;
                        valorconcepto = 0;
                    } else {
                       /* if ((acobrarsindeuda < 10) && (tienenI == (int) 1) && (acobrarsindeuda > 0)) {

                            String ac = Integer.toString((int) acobrarsindeuda); // Ax: (int) explicacion abajo
                            pesosajuste = parseStringToInteger(ac.substring(ac.length() - 1, ac.length()));// pesosajuste = parseStringToInteger(Double.toString(acobrarsindeuda).trim().substring(Double.toString(acobrarsindeuda).trim().length() - 1, 1));
                            valorconcepto = (double) 10 - pesosajuste;
                            infoCobrosLiquidados.settablaCobrosRealizados_Conceptodecobro("607  ");
                            actividad[0] = 'C';
                        } else {*/
                            acobrar = variables.ejecutarAjusteUnidades(acobrar);//EjecutarAjusteEntero Ax:  ejecutarAjusteUnidades ajuste quita los decimales, pero puede dejar un cero 234.00256 -->  234.0
                            String ac = Integer.toString((int) acobrar); // se quita en cero que dejo la anterior funcion
                            pesosajuste = parseStringToInteger(ac.substring(ac.length() - 1, ac.length()));
                            if (pesosajuste >= 5) {
                                valorconcepto = (double) 10 - pesosajuste;
                                infoCobrosLiquidados.settablaCobrosRealizados_Conceptodecobro("608  ");
                                actividad[0] = 'C';
                            } else {
                                valorconcepto = pesosajuste;
                                infoCobrosLiquidados.settablaCobrosRealizados_Conceptodecobro("607  ");
                                infoCobrosLiquidados.settablaCobrosRealizados_idconcepto("000");
                                infoCobrosLiquidados.settablaCobrosRealizados_Indicadoractividad("D");
                                actividad[0] = 'D';
                            }
                        //}
                    }
                    // Log.e("error", acobrar + "***************concepto3-" + valorconcepto);
                    break;

                case 613:
                    if (parsearDecimal(infoClienteSalida.gettablaClienteSalida_CONTRIBUCIONENERGIA().trim())>=0)
                        valorconcepto =calcularSubsidioEmpleado(variables.pesosenergia);
                    else
                        valorconcepto = calcularSubsidioEmpleado(variables.pesosenergia + parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONTRIBUCIONENERGIA().trim()));

                        valorconcepto = variables.ejecutarAjusteUnidades(valorconcepto);
                    break;

                case 710:
                    valor710 += valorconcepto;
                    // Log.e("error", "***************concepto2-" + valorconcepto);
                    break;

                default:
                    // Log.e("error", "reliquida no entra1 " + apuntadortarifaactiva);
                    // Log.e("error5", infoCobrosLiquidados.gettablaCobrosRealizados_VALOR() + "-" + infoCobrosLiquidados.gettablaCobrosRealizados_PORCENTAJE() + "concepto " + codconcepto);
                    if (infoCobrosLiquidados.gettablaCobrosRealizados_IDCONVENIO().equals("1")) {
                        // Log.e("error", "reliquida entra1 " + apuntadortarifaactiva);
                      //  if (apuntadortarifaactiva > 0) {
                            //mirar para ebsa
                           // tablaTarifa.lectura_TablaTarifas(apuntadortarifaactiva);
                       // }

                     //  cambiar para ebsa


                       // Double pesosenergiaConSubContri = pesosenergia + Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONTRIBUCIONENERGIA());


                        //validar este vlor y esta liquidacion
                        // alerta esto es lo que indica diego que si el cliente es una cuenta tipo
                        //Variables.Pesosenergia
                        // Log.e("error1", pesosenergia + " ejecutar liquidacion1 " + infoClienteSalida.gettablaClienteSalida_CONTRIBUCIONENERGIA() + "-" + codconcepto);

                        //valorconcepto = EjecutarLiquidacionConvenio(pesosenergia, pesosenergia + (Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONTRIBUCIONENERGIA()) * (-1)));
                        //valorconcepto = EjecutarLiquidacionConvenio(pesosenergia, variables.pesosenergia);

                        //toca mirar como es el cobro valorizado versus cobro de la liquidacion en pllemo, no si si el valorizado implica sumarle cuando es contribucion

                        if (parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Bimestral()) == 1)
                        {
                            valorconcepto = ejecutarLiquidacionConvenio(variables.pesosenergia, variables.pesosenergia, infoCobrosLiquidados.gettablaCobrosRealizados_CONCEPTODECOBRO().trim());
                        }
                        else
                        {

                            valorconcepto = ejecutarLiquidacionConvenio(PesosEnergiaConvenio, PesosEnergiaConvenio, infoCobrosLiquidados.gettablaCobrosRealizados_CONCEPTODECOBRO().trim());
                         //algo diferente cuando es solo un kw
                        if (parseStringToDouble(infoClienteSalida.getTablaClienteSalida_CONSUMOFACTURADO()) > 0 && parseStringToDouble(infoClienteSalida.getTablaClienteSalida_CONSUMOFACTURADO()) < 2)
                        {
                            if (!infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO1CNCPTO().trim().equals("") && !infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO2CNCPTO().trim().equals(""))
                            {
                                valorconcepto = valorconcepto  * 2;
                            }

                        }
                            PesosEnergiaReactConvenio=0;
                            PesosEnergiaReactContrConvenio=0;
                            PesosEnergiaConvenio=0;
                            PesosEnergiaContrConvenio=0;

                        }
                        //verificar si el consumo menor a los periodos a liquidar


                        // Log.e("error5", "concepto 51 calculado " + valorconcepto);

                        valor_alumbradopublico = variables.ejecutarAjusteUnidades(valorconcepto) + "";
                        //ojo con este valor de conceptosolo si se toma el valor del alumbrado
                        //  valor51 = Variables.EjecutarAjusteUnidades(valorconcepto);

                    } else {
                        if (infoCobrosLiquidados.gettablaCobrosRealizados_IDCONVENIO().equals("4")) {
                            valorconcepto = ejecutarLiquidacionOtros(variables.pesosenergia);

                        }//nuevo para sobre tasa energia
                        else {
                           /* if (codconcepto == 51 && (parseStringToInteger(infoCobrosLiquidados.gettablaCobrosRealizados_PORCENTAJE().trim()) == 98) && (parseStringToInteger(infoCobrosLiquidados.gettablaCobrosRealizados_VALOR().trim()) > 0)) {
                            valorconcepto = parseStringToDouble(infoCobrosLiquidados.gettablaCobrosRealizados_VALOR().trim());
                            // Log.e("error5", "concepto 51 sobre tasa " + valorconcepto);
                        } else*/
                            escriba = 0;
                        }
                    }

                    if ((Integer.parseInt(infoCobrosLiquidados.gettablaCobrosRealizados_CONCEPTODECOBRO().trim()) == 820) || ((Integer.parseInt(infoCobrosLiquidados.gettablaCobrosRealizados_CONCEPTODECOBRO().trim()) == 810)))
                        valorconcepto = 0;

                    break;
            }

            if (escriba > 0) {

                if (valorconcepto >= 0) {
                    valorconcepto = variables.ejecutarAjusteUnidades(valorconcepto);
                    infoCobrosLiquidados.settablaCobrosRealizados_Valor(("" + (int) valorconcepto).trim());
                } else
                    infoCobrosLiquidados.settablaCobrosRealizados_Valor(("" + (int) valorconcepto).trim());

                infoCobrosLiquidados.escribir_TablaCobrosRealizados(conceptoactual);
            }
            // Log.e("error1", "codConcepto1 " + codconcepto + "-" + valorconcepto);
            switch (actividad[0]) {
                default:
                    break;
                case 'C':
                    // case 'F':
                    // Log.e("error1", acobrar + " codConcepto ***************" + codconcepto);
                    if ((codconcepto != 0) && (codconcepto != 1) && (codconcepto != 2) && (codconcepto != 3) && (codconcepto != 4) && (codconcepto != 9) && (codconcepto != 8) && (codconcepto != 5))
                        SumatoriaDifADeuda += valorconcepto;

                    //if ((codconcepto != 8))
                        acobrar += valorconcepto;

                    // Log.e("error1", acobrar + " 0codConcepto ***************" + codconcepto);

                    if ((codconcepto != 0) && (codconcepto != 1) && (codconcepto != 2) && (codconcepto != 3) && (codconcepto != 4) && (codconcepto != 9) && (codconcepto != 8) && (codconcepto != 5))
                        acobrarsindeuda += variables.ejecutarAjusteUnidades(valorconcepto);
                    else
                        tienenI = 1;
                    // Log.e("error1", acobrar + "***************concepto5-" + valorconcepto);

                    break;
                case 'A':
                    acobrarAseo += valorconcepto;
                    break;

                case 'D':
                    // case 'F':
                    if ((codconcepto != 0) && (codconcepto != 1) && (codconcepto != 2) && (codconcepto != 3) && (codconcepto != 4) && (codconcepto != 9) && (codconcepto != 5))
                        SumatoriaDifADeuda -= valorconcepto;
                    acobrar -= variables.ejecutarAjusteUnidades(valorconcepto);
                    if ((codconcepto != 0) && (codconcepto != 1) && (codconcepto != 2) && (codconcepto != 3) && (codconcepto != 4) && (codconcepto != 9) && (codconcepto != 8))
                        acobrarsindeuda -= variables.ejecutarAjusteUnidades(valorconcepto);
                    else
                        tienenI = 1;
                    // Log.e("error1", acobrar + "***************concepto0-" + valorconcepto);

                    break;
                case 'Y':
                    acobrarAseo -= valorconcepto;
                    break;

            }
            --nroconceptos;
            ++conceptoactual;
        }
        infoCobrosLiquidados.Cerrar_TablaCobrosRealizados();

        // Log.e("error1", "***********conceptos " + acobrar + "-" + infoCobrosLiquidados.gettablaCobrosRealizados_RANGOMAXIMO().trim());
        if (acobrar >= 0) {//ax?
          /*  if (liquidounafinanciacion == 1) {
                infoClienteSalida.settablaClienteSalida_VALORFACTURADO(("" + (int) variables.ejecutarAjusteUnidades(acobrar + Double.parseDouble(infoCobrosLiquidados.gettablaCobrosRealizados_RANGOMAXIMO().trim()))));
            } else {*/
                infoClienteSalida.settablaClienteSalida_VALORFACTURADO(("" + (int) variables.ejecutarAjusteUnidades(acobrar)).trim());
          //  }
        } else {
         /*   if (liquidounafinanciacion == 1) {
                infoClienteSalida.settablaClienteSalida_VALORFACTURADO(("" + (int) variables.ejecutarAjusteUnidades(acobrar + Double.parseDouble(infoCobrosLiquidados.gettablaCobrosRealizados_RANGOMAXIMO().trim()))));
            } else {*/
                //infoClienteSalida.settablaClienteSalida_VALORFACTURADO(("" + (int) variables.ejecutarAjusteUnidades(acobrar)).trim());
            infoClienteSalida.settablaClienteSalida_VALORFACTURADO("0");
           // }
        }
        //necesito que me modifiquen la estructura de esta tabla cliente salida
        infoClienteSalida.settablaClienteSalida_VALORFACTURADOASEO(("" + (int) variables.ejecutarAjusteUnidades(acobrarAseo)).trim());
        //if (Double.parseDouble(infoClienteSalida.gettablaClienteSalida_VALORFACTURADO()) < 0)
        //    infoClienteSalida.settablaClienteSalida_VALORFACTURADO("0");



        return 1;
    }

    /**
     * Liquida el convenio del cliente segun la nueva estructura: la info de
     * hasta 3 convenios llega embebida directo en el medidor (CNVNIO1..CNVNIO3),
     * en vez de una tabla aparte indexada por primerconvenio/nroconvenios.
     *
     * SUPUESTO A CONFIRMAR (tal como lo describiste, todavia pendiente de
     * levantar la parametrizacion exacta con EBSA): se busca el UNICO convenio
     * (de los 3 posibles) cuyo CNCPTO coincide con 'conceptoBuscado', y se
     * liquida SOLO con ese -- ya no se suma ni se reparte entre "trimestral"
     * dividiendo pesosEnergia entre nroconvenios como hacia el codigo viejo.
     * Si esto cambia cuando definas la parametrizacion real, la parte que hay
     * que tocar es SOLO ejecutarLiquidacionConvenio (el "cual escoger");
     * calcularUnConvenio (el "como liquidar ese uno") no deberia cambiar.
     *
     * @param pesosEnergia            igual al Pesos_energia de la version anterior
     * @param pesosEnergiaValorizado  igual al Pesos_enerviaValorizado de la version anterior
     * @param codConcepto             el codigo de concepto que esta procesando el llamador
     *                                (el mismo con el que ya identifica que se trata de un
     *                                convenio) -- se compara contra CNVNIO1..3CNCPTO para
     *                                decidir cual de los 3 convenios ejecutar
     * @return el valor liquidado del convenio, o 0 si el cliente no tiene ningun
     *         convenio con ese concepto, o si su consumo no cae en el rango de ese convenio
     */
    double ejecutarLiquidacionConvenio(double pesosEnergia, double pesosEnergiaValorizado, String codConcepto) {

        variables.pesosenergia = pesosEnergia;


        String concepto1 = infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO1CNCPTO().trim();
        String concepto2 = infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO2CNCPTO().trim();
        String concepto3 = infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO3CNCPTO().trim();
        String buscado = codConcepto.trim();

        int numeroConvenio;
        if (concepto1.equals(buscado) && !concepto2.equals(buscado) && !concepto3.equals(buscado)) {
            numeroConvenio = 1;
        } else if (concepto2.equals(buscado)&& concepto2.equals(buscado) && !concepto3.equals(buscado)) {
            numeroConvenio = 2;
        } else if (concepto3.equals(buscado) && concepto2.equals(buscado) && concepto3.equals(buscado)) {
            numeroConvenio = 3;
        } else {
            return 0; // el cliente no tiene ningun convenio con ese concepto
        }

        return calcularUnConvenio(numeroConvenio, pesosEnergia, pesosEnergiaValorizado);
    }

    /**
     * Calcula el valor de UN convenio puntual (1, 2 o 3), leyendo sus 8 campos
     * directo del medidor. Misma logica F/P y R/V que ya tenias en la version
     * vieja (tabla aparte), solo que ahora corre UNA sola vez -- no hay bucle
     * sumando topes de varios convenios ni reparto por bimestral/trimestral.
     */
    private double calcularUnConvenio(int numeroConvenio, double pesosEnergia, double pesosEnergiaValorizado) {

        String tpovlor, tpoprcntje, vlorTxt, rngomnmoTxt, rngomxmoTxt, tpemnmoTxt, tpemxmoTxt;
        double SumavlorTxt=0.0;

        switch (numeroConvenio) {
            case 1:
                tpovlor = infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO1TPOVLOR().trim();
                tpoprcntje = infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO1TPOPRCNTJE().trim();
                if (infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO1VLOR().trim().equals(""))
                    vlorTxt ="0";
                else
                     vlorTxt = infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO1VLOR().trim().replace(",",".");
                SumavlorTxt = parsearDecimal(vlorTxt);
                rngomnmoTxt = infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO1RNGOMNMO().trim();
                rngomxmoTxt = infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO1RNGOMXMO().trim();
                tpemnmoTxt = infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO1TPEMNMO().trim();
                tpemxmoTxt = infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO1TPEMXMO().trim();
                break;
            case 2:
                tpovlor = infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO2TPOVLOR().trim();
                tpoprcntje = infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO2TPOPRCNTJE().trim();
/*                if (infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO1VLOR().trim().equals(""))
                    vlorTxt ="0";
                else
                    vlorTxt = infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO1VLOR().trim().replace(",",".");
                SumavlorTxt = parsearDecimal(vlorTxt);*/

                if (infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO2VLOR().trim().equals(""))
                    vlorTxt ="0";
                else
                    vlorTxt = infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO2VLOR().trim().replace(",",".");

                SumavlorTxt += parsearDecimal(vlorTxt);

                rngomnmoTxt = infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO2RNGOMNMO().trim();
                rngomxmoTxt = infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO2RNGOMXMO().trim();
                tpemnmoTxt = infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO2TPEMNMO().trim();
                tpemxmoTxt = infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO2TPEMXMO().trim();
                break;
            case 3:
                tpovlor = infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO3TPOVLOR().trim();
                tpoprcntje = infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO3TPOPRCNTJE().trim();
             /*   if (infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO1VLOR().trim().equals(""))
                    vlorTxt ="0";
                else
                    vlorTxt = infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO1VLOR().trim().replace(",",".");
                SumavlorTxt = parsearDecimal(vlorTxt);

                if (infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO2VLOR().trim().equals(""))
                    vlorTxt ="0";
                else
                    vlorTxt = infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO2VLOR().trim().replace(",",".");
                SumavlorTxt += parsearDecimal(vlorTxt);*/

                if (infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO3VLOR().trim().equals(""))
                    vlorTxt ="0";
                else
                vlorTxt = infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO3VLOR().trim().replace(",",".");

                SumavlorTxt += parsearDecimal(vlorTxt);

                rngomnmoTxt = infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO3RNGOMNMO().trim();
                rngomxmoTxt = infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO3RNGOMXMO().trim();
                tpemnmoTxt = infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO3TPEMNMO().trim();
                tpemxmoTxt = infoMedidorEntrada.gettablaMedidorEntrada_CNVNIO3TPEMXMO().trim();
                break;
            default:
                return 0;
        }

        double rangominimo = Double.parseDouble(rngomnmoTxt);
        double rangomaximo = Double.parseDouble(rngomxmoTxt);
        double topeminimo = Double.parseDouble(tpemnmoTxt);
        double topemaximo = Double.parseDouble(tpemxmoTxt);
        double valor = SumavlorTxt; // parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Bimestral());//dividirlo por el numero de meses
// se reemplasa Double.parseDouble(vlorTxt.contains(",") ? vlorTxt.replace(",", ".") : vlorTxt)

        double consumoenergia = Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO().trim());

        // Sin rango definido: se liquida el valor fijo del convenio, tal cual.
        if ((rangominimo == 0) && (rangomaximo == 0)) {

           return valor * numeroConvenio;
            //antes se hacia sumatoria para procesar trimstral
        }

        // El consumo del cliente no cae en el rango de este convenio -> no aplica.
        if (!((consumoenergia == 0) || (consumoenergia >= rangominimo && consumoenergia <= rangomaximo))) {
            return 0;
        }

        if (tpovlor.equals("F")) {
            if (valor < topeminimo) return
                    topeminimo * numeroConvenio;
            if (valor > topemaximo) return
                    topemaximo * numeroConvenio;
            return valor;
        }

        if (tpovlor.equals("P")) {
            double temporal;
            if (tpoprcntje.equals("R")) {
                temporal = pesosEnergiaValorizado * valor / 100.0;
            } else if (tpoprcntje.equals("V")) {
                temporal = pesosEnergia * valor / 100.0;
            } else {
                return 0;
            }

            if (temporal > topemaximo) return topemaximo;
            if (temporal < topeminimo) return topeminimo;
            return variables.ejecutarAjusteUnidades(temporal);
        }

        return 0;
    }

    double ejecutarLiquidacionOtros(double pesosenergia) {
        double valor, temporal;
        variables.pesosenergia = pesosenergia;
        valor = Double.parseDouble(infoCobrosLiquidados.gettablaCobrosRealizados_PORCENTAJE().trim());
        temporal = variables.pesosenergia * valor / (double) 100;
        return (temporal);
    }
/*
    double ejecutarSubsidioEmpleado(double pesosenergia) {
        infoCobrosLiquidados.settablaCobrosRealizados_rangomaximo(reemplazarDatos(infoCobrosLiquidados.gettablaCobrosRealizados_RANGOMAXIMO()));
        infoCobrosLiquidados.settablaCobrosRealizados_rangomaximo("" + (variables.ejecutarAjusteUnidades(Double.parseDouble(infoCobrosLiquidados.gettablaCobrosRealizados_RANGOMAXIMO().trim()))));

        if (pesosenergia > (250 * parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Bimestral().trim()))) {
            if (infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim().equals("999999998")) {
                if (Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO().trim()) <= 350 * Integer       .parseInt(infoClienteEntrada.gettablaEntradaClientes_Bimestral().trim())) {
                    return (pesosenergia - (250 * parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Bimestral().trim())));

                } else
                    return (valorLiquidacionEmpleado);
            } else if (Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO().trim()) <= 250 * Integer      .parseInt(infoClienteEntrada.gettablaEntradaClientes_Bimestral().trim())) {
                return (pesosenergia - (250 * parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Bimestral().trim())));

            } else
        return (valorLiquidacionEmpleado);

        } else
            return (pesosenergia);
    }
  */

    double calcularSubsidioEmpleado(double pesosEnergia) { //si tiene subsidio quitarlo

        // necesitaria aqui entender lo de del verdadero tope maximo
        //
        double periodosLiquidados = parsearDecimal(infoClienteEntrada.gettablaEntradaClientes_Bimestral().trim());
        double rangoMaximo = 600 * parsearDecimal(infoClienteEntrada.gettablaEntradaClientes_MDDOR1VLORKWH()) * periodosLiquidados;
        //double rangoMinimo = parsearDecimal(infoCobrosLiquidados.gettablaCobrosRealizados_RANGOMINIMO().trim());;//variables.ejecutarAjusteUnidades( parsearDecimal(infoCobrosLiquidados.gettablaCobrosRealizados_RANGOMAXIMO().trim()));
        infoCobrosLiquidados.settablaCobrosRealizados_rangomaximo(""+variables.ejecutarAjusteUnidades(rangoMaximo));

        double rangoMinimo = 2 * periodosLiquidados;

        boolean esEmpleadoEstratoAlto =  infoClienteEntrada.gettablaEntradaClientes_EMPLDO().trim().equals("S")  && Integer.parseInt(infoClienteEntrada.gettablaEntradaClientes_Estrato().trim()) >= 5;

        if (pesosEnergia > rangoMaximo) {
            if (esEmpleadoEstratoAlto) {
                double contribucionEnergia = parsearDecimal(infoClienteSalida.gettablaClienteSalida_CONTRIBUCIONENERGIA().trim());
                return rangoMaximo - rangoMinimo ;//llega sin contribucion llega plena - contribucionEnergia
            } else {
                return rangoMaximo - rangoMinimo;
            }
        }

        if (pesosEnergia <= rangoMinimo) {
            return 0;
        } else {
            if (esEmpleadoEstratoAlto) {
                double contribucionEnergia = parsearDecimal(infoClienteSalida.gettablaClienteSalida_CONTRIBUCIONENERGIA().trim());
                return pesosEnergia  ;//- rangoMinimo
            } else {
                return pesosEnergia ;//- rangoMinimo
            }
        }
    }



    private long validarEstadoCliente(int clienteactual, boolean usarValidEstdRegNew, String tipovalidacion) { //Ax: usarValidEstdRegNew parametro para usar ValidarEstadoRegistroNUEVO

        int contadores;
        int medactual;
        int resultado = 0;
        String cadmedact;
        String cadnromed;

        cadnromed = infoClienteEntrada.gettablaEntradaClientes_Nrodemedidores();

        cadmedact = infoClienteEntrada.gettablaEntradaClientes_primermedidor();

        contadores = parseStringToInteger(cadnromed);
        medactual = parseStringToInteger(cadmedact);
        while (contadores > 0) {
            contadores--;

            //nuevo, validar para AR
            // Log.e("error2", "-" + validarCausaAR + "-" + infoClienteEntrada.gettablaEntradaClientes_Cuenta() + " causa temporal1 " + infoRegistroEntrada.gettablaRegistroDeEntrada_TIPOENERGIA());

            if (validarCausaAR) {
                // Log.e("error2", "critica40 7 ");

                if (infoRegistroEntrada.gettablaRegistroDeEntrada_tipoenergia().trim().equals("A")) {
                    causaTem = infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA();
                }

                if (infoRegistroEntrada.gettablaRegistroDeEntrada_tipoenergia().trim().equals("R") && causaTem.trim().equals("40")) {
                    infoRegistroSalida.settablaRegistroSalida_causadenolectura(causaTem);
                    variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";validarEstadoCliente;" + getPhoneDate() + "-" + getPhoneHour();
                    escribeResumenTiempo(variableResumenLiquidacion + " " + "validarEstadoCliente: Posicion Actual|" + VariablesGlobales.registroactual);
                    infoRegistroSalida.escribir_TablaRegistroSalida(VariablesGlobales.registroactual);

                }
                validarCausaAR = false;
            }

            // Log.e("error2", validarCausaAR + " causa temporal " + causaTem);

            resultado = evaluarEstadoMedidor(medactual, usarValidEstdRegNew, tipovalidacion);

            if (resultado > 0)
                return (resultado);

            ++medactual;
        }
        return 0;
    }

    private int evaluarEstadoMedidor(int numero, boolean usarValidEstdRegNew, String tipovalidacion) { //Ax: usarValidEstdRegNew parametro para usar ValidarEstadoRegistroNUEVO

        int registros;
        int reginicial;
        String cadregact;
        String cadnroreg;

        infoMedidorEntrada.lectura_TablaMedidorEntrada(numero);

        cadnroreg = infoMedidorEntrada.gettablaMedidorEntrada_Nroregistros().trim();
        cadregact = infoMedidorEntrada.gettablaMedidorEntrada_primerregistro().trim();

        registros = parseStringToInteger(cadnroreg.trim().equals("") ? "0" : cadnroreg);
        reginicial = parseStringToInteger(cadregact.trim().equals("") ? "0" : cadregact);

        while (registros > 0) {

            registros--;

            if (usarValidEstdRegNew) {
                if (validarEstadoRegistroNuevo(reginicial) == 0) {
                    return (reginicial);
                }
            } else {
                if (validarEstadoRegistro(reginicial, tipovalidacion) == 0) {
                    return (reginicial);
                }
            }
            ++reginicial;
        }
        return 0;
    }

    private int validarEstadoRegistro(int numero, String tipovalidacion) {

        //descomentar infoClienteEntrada.abrir_TablaEntradaClientes(infoClienteEntrada.getArchivo_TablaEntradaClientes());
        Log.e("INFO", "validarEstadoRegistro|numero: " + numero + "| variables.clienteactual : " + variables.clienteactual);
        infoClienteEntrada.lectura_TablaEntradaClientes(variables.clienteactual);
        infoRegistroSalida.lectura_TablaRegistroSalida(numero);
        infoRegistroEntrada.lectura_TablaRegistroDeEntrada(numero);
        /*CAMBIAR PARA EBSA
        if (infoRegistroEntrada.gettablaRegistroDeEntrada_Tipomedida().trim().equals("CT")) {
            apuntadortarifaCT = parseStringToInteger(infoRegistroEntrada.gettablaRegistroDeEntrada_APUNTADORTARIFA().trim());
        } else */
        if (infoRegistroEntrada.gettablaRegistroDeEntrada_tipoenergia().equals("R")) {
            apuntadortarifareactiva = parseStringToInteger(infoRegistroEntrada.gettablaRegistroDeEntrada_tarifaliquidacion().trim());
        } else {
            apuntadortarifaactiva = parseStringToInteger(infoRegistroEntrada.gettablaRegistroDeEntrada_tarifaliquidacion().trim());
        }

        if (infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA().trim().length() != 0 && !infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA().trim().equals("0")) {
            //  Log.e("error",variableTomarDatosLectura+" entra validar 1 "+infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA());
            if (variableTomarDatosLectura)
                llenarRegistrosLeidos();
            return 1;

        }
        if (infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim().length() != 0 && !infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim().equals("0")) {
            //  Log.e("error",variableTomarDatosLectura+" entra validar 2 "+infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA());
            if (variableTomarDatosLectura)
                llenarRegistrosLeidos();
            return 2;
        }

//        if (tipovalidacion.equals("E")) {
//            procesarEntregaFacturas(1, numero);
//            return 2;
//        }
        // nuevo procedimiento para impedir que lean los directos
        if (evaluarCargaInstalada(0) > 0 && infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().equals("L")) {
            // Log.e("error",variableTomarDatosLectura+" entra validar 3 "+infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA());
            if (variableTomarDatosLectura)
                llenarRegistrosLeidos();
            return 1;

        }
        return 0;
    }

    private int validarEstadoRegistroNuevo(int numero) {//666

        infoClienteEntrada.lectura_TablaEntradaClientes(variables.clienteactual);
        //nuevo para leer el archivo del cliente de salida

        infoRegistroSalida.lectura_TablaRegistroSalida(numero);
        infoRegistroEntrada.lectura_TablaRegistroDeEntrada(numero);

        if (infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA().trim().length() != 0 && !infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA().trim().equals("0")) {
            //esto se tendra que cambiar por una metodologia de gaurdar liquidacon antes o recorrr medidas antes de enviar
            llenarRegistrosLeidos();
            return 1;

        }
        if (infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim().length() != 0 && !infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim().equals("0")) {
            llenarRegistrosLeidos();
            return 2;
        }
        return (0);
    }

    private void llenarRegistrosLeidos() {

        if (nroContador1.trim().equals("")) {
            nroContador1 = infoRegistroEntrada.gettablaRegistroDeEntrada_nrocontador();
            idContador1 = infoRegistroSalida.gettablaRegistroSalida_CONSECUTIVO();
            lecturaTomada1 = infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA();
            causadenolectura1 = String.format("%1$2s", infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim());
            intentos1 = infoRegistroSalida.gettablaRegistroSalida_INTENTOS();
            lecturaModificada11 = String.format("%1$10s", infoRegistroSalida.gettablaRegistroSalida_LECTURAMODIFICADA1().trim());
            lecturaModificada21 = String.format("%1$10s", infoRegistroSalida.gettablaRegistroSalida_LECTURAMODIFICADA2().trim());
            digitos1 = String.format("%1$1s", infoRegistroEntrada.gettablaRegistroDeEntrada_Digitos().trim());
            criticaPDA1 = infoRegistroSalida.gettablaRegistroSalida_LEIDO();
            lecturaAnterior1 = infoRegistroEntrada.gettablaRegistroDeEntrada_Lecturaanterior();
            //se comenta en el codigo de EC no esta TipoMedida1 = infoRegistroEntrada...gettablaRegistroDeEntrada_Tipomedida();//tipoDeMedidor
            if (infoRegistroEntrada.gettablaRegistroDeEntrada_tipoDesviacion().equals("N"))
                PromedioCliente1 = infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediocliente();
            else
                PromedioCliente1 = infoRegistroEntrada.gettablaRegistroDeEntrada_promedioNormalizado();

        } else {
            if (nroContador2.trim().equals("")) {// && (parseStringToInteger(infoMedidorEntrada.gettablaMedidorEntrada_Nroregistros().trim()) > 1 || parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_NRODEMEDIDORES().trim()) > 1)) {
                nroContador2 = infoRegistroEntrada.gettablaRegistroDeEntrada_nrocontador();
                idContador2 = infoRegistroEntrada.gettablaRegistroDeEntrada_contador();
                lecturaTomada2 = infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA();

                // Log.e("error2", "critica " + causadenolectura1);
                if (infoRegistroEntrada.gettablaRegistroDeEntrada_tipoenergia().trim().equals("R") && causadenolectura1.trim().equals("40")) {
                    causadenolectura2 = causadenolectura1;
                } else {
                    causadenolectura2 = String.format("%1$2s", infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim());
                }

                intentos2 = infoRegistroSalida.gettablaRegistroSalida_INTENTOS();
                lecturaModificada12 = String.format("%1$10s", infoRegistroSalida.gettablaRegistroSalida_LECTURAMODIFICADA1().trim());
                lecturaModificada22 = String.format("%1$10s", infoRegistroSalida.gettablaRegistroSalida_LECTURAMODIFICADA2().trim());
                digitos2 = String.format("%1$1s", infoRegistroEntrada.gettablaRegistroDeEntrada_Digitos().trim());
                criticaPDA2 = infoRegistroSalida.gettablaRegistroSalida_LEIDO();
                lecturaAnterior2 = infoRegistroEntrada.gettablaRegistroDeEntrada_Lecturaanterior();
                // TipoMedida2= infoRegistroEntrada...gettablaRegistroDeEntrada_Tipomedida();
                PromedioCliente2 = infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediocliente();
                if (infoRegistroEntrada.gettablaRegistroDeEntrada_tipoDesviacion().equals("N"))
                    PromedioCliente2 = infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediocliente();
                else
                    PromedioCliente2 = infoRegistroEntrada.gettablaRegistroDeEntrada_promedioNormalizado();
            } else {
                if (nroContador3.trim().equals("")) {// && (parseStringToInteger(infoMedidorEntrada.gettablaMedidorEntrada_Nroregistros().trim()) > 1 || parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_NRODEMEDIDORES().trim()) > 1)) {
                    nroContador3 = infoRegistroEntrada.gettablaRegistroDeEntrada_nrocontador();
                    idContador3 = infoRegistroEntrada.gettablaRegistroDeEntrada_contador();
                    lecturaTomada3 = infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA();
                    causadenolectura3 = String.format("%1$2s", infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim());
                    intentos3 = infoRegistroSalida.gettablaRegistroSalida_INTENTOS();
                    lecturaModificada13 = String.format("%1$10s", infoRegistroSalida.gettablaRegistroSalida_LECTURAMODIFICADA1().trim());
                    lecturaModificada23 = String.format("%1$10s", infoRegistroSalida.gettablaRegistroSalida_LECTURAMODIFICADA2().trim());
                    digitos3 = String.format("%1$1s", infoRegistroEntrada.gettablaRegistroDeEntrada_Digitos().trim());
                    criticaPDA3 = infoRegistroSalida.gettablaRegistroSalida_LEIDO();
                    lecturaAnterior3 = infoRegistroEntrada.gettablaRegistroDeEntrada_Lecturaanterior();
                    // TipoMedida3 = infoRegistroEntrada...gettablaRegistroDeEntrada_Tipomedida();

                    if (infoRegistroEntrada.gettablaRegistroDeEntrada_tipoDesviacion().equals("N"))
                        PromedioCliente3 = infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediocliente();
                    else
                        PromedioCliente3 = infoRegistroEntrada.gettablaRegistroDeEntrada_promedioNormalizado();
                } else {
                    // if (nroContador4.trim().equals("") && (parseStringToInteger(infoMedidorEntrada.gettablaMedidorEntrada_Nroregistros().trim()) > 1 || parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_NRODEMEDIDORES().trim()) > 1)) {
                    nroContador4 = infoRegistroEntrada.gettablaRegistroDeEntrada_nrocontador();
                    idContador4 = infoRegistroEntrada.gettablaRegistroDeEntrada_contador();
                    lecturaTomada4 = infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA();
                    causadenolectura4 = String.format("%1$2s", infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim());
                    intentos4 = infoRegistroSalida.gettablaRegistroSalida_INTENTOS();
                    lecturaModificada14 = String.format("%1$10s",
                            infoRegistroSalida.gettablaRegistroSalida_LECTURAMODIFICADA1().trim());
                    lecturaModificada24 = String.format("%1$10s",
                            infoRegistroSalida.gettablaRegistroSalida_LECTURAMODIFICADA2().trim());
                    digitos4 = String.format("%1$1s", infoRegistroEntrada.gettablaRegistroDeEntrada_Digitos().trim());
                    criticaPDA4 = infoRegistroSalida.gettablaRegistroSalida_LEIDO();
                    lecturaAnterior4 = infoRegistroEntrada.gettablaRegistroDeEntrada_Lecturaanterior();
                    if (infoRegistroEntrada.gettablaRegistroDeEntrada_tipoDesviacion().equals("N"))
                        PromedioCliente4 = infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediocliente();
                    else
                        PromedioCliente4 = infoRegistroEntrada.gettablaRegistroDeEntrada_promedioNormalizado();

                    // TipoMedida4 = infoRegistroEntrada...gettablaRegistroDeEntrada_Tipomedida();

                } /*else {
                        nroContador5 = infoRegistroEntrada..gettablaRegistroDeEntrada_nrocontador();
                        //[enerca] idContador5 = infoRegistroEntrada.gettablaRegistroDeEntrada_contador();
                        lecturaTomada5 = infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA();
                        causadenolectura5 = String.format("%1$2s", infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim());
                        intentos5 = infoRegistroSalida.gettablaRegistroSalida_INTENTOS();
                        lecturaModificada15 = String.format("%1$10s",
                                infoRegistroSalida.gettablaRegistroSalida_LECTURAMODIFICADA1().trim());
                        lecturaModificada25 = String.format("%1$10s",
                                infoRegistroSalida.gettablaRegistroSalida_LECTURAMODIFICADA2().trim());
                        digitos5 = String.format("%1$1s", infoRegistroEntrada.gettablaRegistroDeEntrada_Digitos().trim());
                        criticaPDA5 = infoRegistroSalida.gettablaRegistroSalida_LEIDO();
                        lecturaAnterior5 = infoRegistroEntrada.gettablaRegistroDeEntrada_Lecturaanterior();
                        TipoMedida5 = infoRegistroEntrada...gettablaRegistroDeEntrada_Tipomedida();
                    }
                }*/
            }
        }
    }

    private int evaluarCargaInstalada(double consumoafacturar) {// [Ma] por aca se pudo meter

        if ((infoMedidorEntrada.gettablaMedidorEntrada_Tipomedidor().trim().equals("X")) || Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumoreal().trim()) > 0)//( Convert.ToInt32(InfoMedidorEntrada.E_CONTAD_ID_CORTADO) == 1)
        {
            if (Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumoreal().trim()) > 0) {
                variables.consumoactual = Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumoreal());
            }

            return (1);
        }
        return (0);
    }

    private int escribirTablasSalida() {
        variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";escribirTablasSalida;" + getPhoneDate() + "-" + getPhoneHour();
        escribeResumenTiempo(variableResumenLiquidacion + " " + "escribirTablasSalida: Posicion Actual|" + VariablesGlobales.registroactual);

        infoRegistroSalida.escribir_TablaRegistroSalida(VariablesGlobales.registroactual);
        infoClienteSalida.escribir_TablaClienteSalida(variables.clienteactual);
        medidorSalida.escribir_TablaContadorSalida(variables.contadoractual);
        return 1;
    }

    private void tomarFechaSistema(String amd, String hm) {

        Calendar cal = new GregorianCalendar();
        Date date = cal.getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        String formatteDate = df.format(date);

        variables.amd = formatteDate;

        Date dt = new Date();
        SimpleDateFormat hf = new SimpleDateFormat("HHmmss");
        String formatteHour = hf.format(dt.getTime());

        variables.hm = formatteHour;
    }

    private void visualizarInformacionCliente(int estado) {

        if (indicadorManual == 1) {
            Log.e("INFO", "Retorno porque el inidicador manual esta en 1");
            return;
        }

        int estadolectura;
        String puntero = "";
        String mensajepantalla = "";
        String lecturaofacturacion = "F";
        String implote;
        validarUbicacionContraPredioActual();
        borrarListaInformacionCliente();

        if (variables.impresionenlote == 1)
            implote = "IL";
        else
            implote = "";

        switch (estado) {
            case 0:
                if (parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_nrocobros().trim()) > 0) {
                    lecturaofacturacion = "L";
                }

                mensajepantalla = (variables.contadoractual - parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_primermedidor().trim()) + 1)
                        + "-" + infoClienteEntrada.gettablaEntradaClientes_Nrodemedidores()  + "- >" + lecturaofacturacion + "-" + implote;

                if ((variables.direcciondelectura == variables.haciaadelante)) {
                    puntero = "=>>";
                } else {
                    puntero = "<<=";
                }
                break;
            case 1:// Proceso Verificacion
                puntero = "VERIFICA";
                break;
            case 2:// Proceso Lecturas
                puntero = "LECTURA";
                break;
            case 3:// Proceso Finalizar
                puntero = "CONCLUIDO";
                break;
            case 4:// proceso bloqueo:
                puntero = "TERMINADO";
                break;
        }

        arma_cuentasGps[0] = infoClienteEntrada.gettablaEntradaClientes_anio().trim();
        arma_cuentasGps[1] = infoClienteEntrada.gettablaEntradaClientes_mes().trim();
        String temp = infoRegistroEntrada.gettablaRegistroDeEntrada_nrocontador().trim();
        arma_cuentasGps[2] = temp;

        lblpuntero.setText("[" + puntero + " " + VariablesGlobales.registroactual + " / "
                + infoMedidorEntrada.getTotal_TablaMedidorEntrada() + "]");

        // lblLectura.setText("");


        temp = infoClienteEntrada.gettablaEntradaClientes_Ruta().trim();
        arma_cuentasGps[3] = temp;

        txtMedidorCuenta.setText("M: " + infoRegistroEntrada.gettablaRegistroDeEntrada_nrocontador().trim() + "|Ind: " + infoRegistroSalida.gettablaRegistroSalida_CONSECUTIVO().trim());

        tablaResumen.refreshDrawableState();
        llenarListaCliente("DIRECCION", infoClienteEntrada.gettablaEntradaClientes_Direccion().trim());
        llenarListaCliente("NOMBRE", infoClienteEntrada.gettablaEntradaClientes_Nombre().substring(0, 47).trim());

        temp = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim();
        llenarListaCliente("CUENTA", temp);
        arma_cuentasGps[4] = temp;
        temp = infoClienteEntrada.gettablaEntradaClientes_Ciclo().trim();

        if (temp.length() > 3) {
            arma_cuentasGps[5] = temp.substring(0, 3);
        } else {
            arma_cuentasGps[5] = temp;
        }

        llenarListaCliente("RUTA", infoClienteEntrada.gettablaEntradaClientes_Ruta().trim());


     // if (parseStringToInteger(temp.trim()) == 83 || parseStringToInteger(temp.trim()) == 80 || parseStringToInteger(temp.trim()) == 81 || parseStringToInteger(temp.trim()) == 82 || parseStringToInteger(temp.trim()) == 60 || parseStringToInteger(temp.trim()) == 74 || infoRegistroEntrada.gettablaRegistroDeEntrada_tipoenergia().equals("R"))
      //    llenarListaCliente("LECTURA ANTERIOR", infoRegistroEntrada.gettablaRegistroDeEntrada_Lecturaanterior().replace(",", "."));

        llenarListaCliente("CLASE SERVICIO", infoClienteEntrada.gettablaEntradaClientes_Clasedeservicio());
        llenarListaCliente("SENTIDO RUTA", "[" + mensajepantalla + lblpuntero.getText());
        if (infoRegistroEntrada.gettablaRegistroDeEntrada_tipoenergia().equals("R")) {
            lblInfoMedida.setTextColor(Color.rgb(154, 154, 0));
            lblInfoMedida.setText("RE-ACTIVA");
        } else {
            lblInfoMedida.setTextColor(Color.rgb(0, 154, 0));
            lblInfoMedida.setText("ACTIVA");
        }

        if (terminal.trim().equals("354379550180590") || terminal.trim().equals("351007491579959") || terminal.trim().equals("351007492166830")
        || terminal.trim().equals("358767141019390") || terminal.trim().equals("868995078034487")
                || terminal.trim().equals("868995078034628")
        ) {
            int Maximo= parseStringToInteger(infoRegistroEntrada.gettablaRegistroDeEntrada_Lecturaanterior().trim())+ (parseStringToInteger(infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediocliente().trim()));
            llenarListaCliente("PARA LEER", infoRegistroEntrada.gettablaRegistroDeEntrada_Lecturaanterior().trim() + "-" + infoRegistroEntrada.gettablaRegistroDeEntrada_Factormultipicacion().trim() + "-" + (Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediocliente().trim())/Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Factormultipicacion().trim()))+ "  "+ Maximo);
            llenarListaCliente("NUEVOS CAMP", infoRegistroEntrada.gettablaRegistroDeEntrada_LQDA_CSMO() + "-" + infoRegistroEntrada.gettablaRegistroDeEntrada_PRMDIO().trim() + "-" + infoRegistroEntrada.gettablaRegistroDeEntrada_LI_BNDA_RES().trim() + "-" + infoRegistroEntrada.gettablaRegistroDeEntrada_LS_BNDA_RES());
        }

        llenarListaCliente("TIPO MEDIDA", infoRegistroEntrada.gettablaRegistroDeEntrada_Tipomedida());// infoMedidorEntrada.ECONTADTIPOMEDIDOR.trim());
        llenarListaCliente("TIPO DESVIA", infoRegistroEntrada.gettablaRegistroDeEntrada_tipoDesviacion());// infoMedidorEntrada.ECONTADTIPOMEDIDOR.trim());
        //[enerca] if (infoClienteEntrada.gettablaEntradaClientes_OBLIGANOVEDAD().trim().equals("S"))
        // llenarListaCliente("ALERTA", infoClienteEntrada.gettablaEntradaClientes_MENSAJENOVEDAD());

        //[enerca] if (!infoClienteEntrada.gettablaEntradaClientes_MENSAJENOVEDAD().substring(0, 1).equals("V")
        // && !infoClienteEntrada.gettablaEntradaClientes_MENSAJENOVEDAD().substring(0, 1).equals(" "))
        //llenarListaCliente("ALERTA", infoClienteEntrada.gettablaEntradaClientes_MENSAJENOVEDAD());

        if (infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("L"))
            llenarListaCliente("OBSERVAC. ANT.", infoMedidorEntrada.gettablaMedidorEntrada_obsercorte() + " Proc." + "LECTURAS");
        else if (infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("E"))
            llenarListaCliente("OBSERVAC. ANT.", infoMedidorEntrada.gettablaMedidorEntrada_obsercorte() + " Proc." + "ENTREGAS");
        else
            llenarListaCliente("OBSERVAC. ANT.", infoMedidorEntrada.gettablaMedidorEntrada_obsercorte() + " Proc." + "FACTURACION");

        // llenarListaCliente("Observacion Ant",
        // infoMedidorEntrada.ECONTADOBSERCORTE+" Proc."+);
        llenarListaCliente("CONTADORES", mensajepantalla);
        // lblLectura.setText("");

        estadolectura = validarEstadoRegistro(VariablesGlobales.registroactual, "L");

        if (estadolectura == 1) {
            llenarListaCliente("LECTURA", infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA().trim());
        } else {
            if (estadolectura == 2) {
                if (anomaliaDeLectura.abrir_AnomaliaDeNoLectura(anomaliaDeLectura.getArchivo_AnomaliaDeNoLectura())) {
                    anomaliaDeLectura.setEncontro_AnomaliaDeNoLectura(0);
                    anomaliaDeLectura.buscarbinario_AnomaliaDeNoLectura(infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA());
                    anomaliaDeLectura.Cerrar_AnomaliaDeNoLectura();
                }

                llenarListaCliente("ANOMALIA NO LECT", infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA() + "->"
                        + anomaliaDeLectura.getanomaliaDeNoLectura_DESCRIPCION());
            }
        }

        if (infoRegistroEntrada.gettablaRegistroDeEntrada_Digitos().trim().equals("") || infoRegistroEntrada.gettablaRegistroDeEntrada_Digitos().trim().equals("0")) {
            VariablesGlobales.intcontroltexto = 6;
        } else {
            VariablesGlobales.intcontroltexto = parseStringToInteger(infoRegistroEntrada.gettablaRegistroDeEntrada_Digitos().trim());
        }

        if (!infoRegistroSalida.gettablaRegistroSalida_INFORME().trim().equals("")) {

            puntero = infoRegistroSalida.gettablaRegistroSalida_INFORME();
            llenarListaCliente("INFORME", infoRegistroSalida.gettablaRegistroSalida_INFORME().trim());
            Log.e("INFO", "INFORME|" + infoRegistroSalida.gettablaRegistroSalida_INFORME().trim() + "|FIN");
            if (infoRegistroSalida.gettablaRegistroSalida_INFORME().contains("CAMBIO DIGITOS")) {
                VariablesGlobales.intcontroltexto = parseStringToInteger(infoRegistroSalida.gettablaRegistroSalida_INFORME().trim().substring(infoRegistroSalida.gettablaRegistroSalida_INFORME().trim().indexOf(":") + 1, infoRegistroSalida.gettablaRegistroSalida_INFORME().trim().length()));
                txtElectura.setFilters(new InputFilter[]{new InputFilter.LengthFilter(VariablesGlobales.intcontroltexto)});
            }
        } else {
            Log.e("INFO", "max length: " + parseStringToInteger(infoRegistroEntrada.gettablaRegistroDeEntrada_Digitos().trim()));
            if (parseStringToInteger(infoRegistroEntrada.gettablaRegistroDeEntrada_Digitos().trim()) != 0) {
                txtElectura.setFilters(new InputFilter[]{new InputFilter.LengthFilter(parseStringToInteger(infoRegistroEntrada.gettablaRegistroDeEntrada_Digitos().trim()))});
            }
        }
        llenarListaCliente("DIGITOS", infoRegistroEntrada.gettablaRegistroDeEntrada_Digitos().trim());
      /*  if (infoClienteEntrada.gettablaEntradaClientes_NN2().trim().equals("1") ) {
            llenarListaCliente("OBLIGA GPS", "SE DEBE LEER A MENOS DE " + VariablesGlobales.getDistanciagps());
        }
        if (infoClienteEntrada.getTablaEntradaClientes_NN4().trim().equals("1") ) {
            llenarListaCliente("OBLIGA OCR", "SE DEBE LEER X OCR");
        }*/
        if (!infoRegistroSalida.gettablaRegistroSalida_COMENTARIO1().trim().equals("")) {
            llenarListaCliente("COMENTARIO", infoRegistroSalida.gettablaRegistroSalida_COMENTARIO1().trim());
        }

        llenarListaCliente("MARCA", infoRegistroEntrada.gettablaRegistroDeEntrada_MARCA().trim());
        if (infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("L"))
            llenarListaCliente("PROCESO", "SOLO LECTURA");
        else if (infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("E"))
            llenarListaCliente("PROCESO", "Solo Entrega");
        else
            llenarListaCliente("ESTADO PROCESO", "FACTURACION");

        // txtInformeEscrito.setText("Menu Liquidacion");

        // double cpc = parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediocliente().trim()) * parseStringToDouble(infoClienteEntrada.gettablaEntradaClientes_Bimestral());//Para enerca, ya que consumopromediocliente llega trimestral
        // lblLectura.setText("" + Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Lecturaanterior().trim().replace(",", ".")) + cpc);

        imagenLiquid_1.setImageResource(android.R.color.transparent);

        if (infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("P")) {
            imagenLiquid_1.setImageResource(R.drawable.imagen_postal);
            lblInfoMedida.setTextColor(Color.rgb(0, 0, 255));
            lblInfoMedida.setText("POSTAL");

       // } else if (infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().trim().equals("99") || infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().trim().equals("97")) {
       //     lblInfoMedida.setTextColor(Color.rgb(255, 128, 0));
        //    lblInfoMedida.setText("EN LABORATORIO");//DIRECTO
        } else if (infoMedidorEntrada.gettablaMedidorEntrada_Tipomedidor().trim().equals("X")
                || infoMedidorEntrada.gettablaMedidorEntrada_Tipomedidor().trim().equals("Z")
                || infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("S")) {
            imagenLiquid_1.setImageResource(R.drawable.imagen_contratada);
            lblInfoMedida.setTextColor(Color.rgb(255, 128, 0));
            lblInfoMedida.setText("PROVISIONAL");//DIRECTO
            txtElectura.setText("0");
        } else if (infoClienteEntrada.gettablaEntradaClientes_Clasedeservicio().trim().equals("IC")) {
            imagenLiquid_1.setImageResource(R.drawable.imagen_integrador);
            lblInfoMedida.setTextColor(Color.rgb(255, 0, 128));
            lblInfoMedida.setText("INTEGRA.");
        } else if (infoRegistroEntrada.gettablaRegistroDeEntrada_Tipomedida().trim().equals("CT")) {
            imagenLiquid_1.setImageResource(R.drawable.imagen_testigo);
            lblInfoMedida.setTextColor(Color.rgb(102, 0, 204));
            lblInfoMedida.setText("TESTIGO");
        } else if (infoMedidorEntrada.gettablaMedidorEntrada_idcortado().trim().equals("1")) {
            imagenLiquid_1.setImageResource(R.drawable.imagen_cortado);
            lblInfoMedida.setTextColor(Color.rgb(255, 0, 0));
            lblInfoMedida.setText("CORTADO");
        } else if (infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("N")) {
            // no imprime factura
        } else {
            imagenLiquid_1.setImageResource(android.R.color.transparent);
        }

        //[enerca]if (infoClienteEntrada.gettablaEntradaClientes_OBLIGANOVEDAD().trim().equals("S"))
        // imagenLiquid_2.setImageResource(R.drawable.imagen_verificarpredio);
        // else
        // imagenLiquid_2.setImageResource(android.R.color.transparent);

        if (VariablesGlobales.habilitadaimpresora == 0) {
            imagenPrinter.setImageResource(R.drawable.impresora_ko);

        } else {
            if (EsImpresora521 == 1)
                imagenPrinter.setImageResource(R.drawable.impresora_ok521);//.impresora_ok);
            else
                imagenPrinter.setImageResource(R.drawable.impresora_ok420);//.impresora_ok);
            //imagenPrinter.setImageResource(R.drawable.impresora_ok);

        }

        if (infoRegistroSalida.gettablaRegistroSalida_NROCONTADOR().trim().length() != 0 && infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim().length() == 0) {

            if ((infoRegistroSalida.gettablaRegistroSalida_NROCONTADOR().trim()).replaceFirst("^0*", "").equals("")) { //rx
                medidorCero = true;
            } else {
                medidorCero = false;
            }
        }
    }

    private void llenarListaCliente(String campo, String valor) {

        TextView txtCampo;
        TextView txtValor;

        fila = new TableRow(this);
        fila.setLayoutParams(layoutFila);

        txtCampo = new TextView(this);
        txtValor = new TextView(this);

        txtCampo.setText(campo);
        txtCampo.setGravity(Gravity.LEFT);
        txtCampo.setTextAppearance(this, R.style.etiqueta);
        txtCampo.setBackgroundResource(R.drawable.tabla_celda);
        txtCampo.setLayoutParams(layoutCampo);

        txtValor.setText(valor);
        txtValor.setGravity(Gravity.LEFT);
        txtValor.setTextAppearance(this, R.style.campo);
        txtValor.setBackgroundResource(R.drawable.tabla_celda);
        txtValor.setLayoutParams(layoutValor);

        fila.addView(txtCampo);
        fila.addView(txtValor);
        tablaResumen.addView(fila);
    }

    private void borrarListaInformacionCliente() {
        if (tablaResumen != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tablaResumen.removeAllViews();
                }
            });
        } else {
            Log.e("borrarLista", "tablaResumen es null -- revisar donde se inicializa");
        }
    }

    private int leerInformacionUsuario(int tipo) {

        switch (tipo) {
            case 1:
                Log.e("INFO", "inicia validar cliente: " + VariablesGlobales.registroactual);
                infoRegistroEntrada.lectura_TablaRegistroDeEntrada(VariablesGlobales.registroactual);
                variables.contadoractual = parseStringToInteger(infoRegistroEntrada.gettablaRegistroDeEntrada_contador().trim());
                variables.clienteactual = parseStringToInteger(infoRegistroEntrada.gettablaRegistroDeEntrada_cliente().trim());
                infoClienteEntrada.lectura_TablaEntradaClientes(variables.clienteactual);
                infoMedidorEntrada.lectura_TablaMedidorEntrada(variables.contadoractual);
                Log.e("INFO", "inicia validar cliente 2: " + variables.clienteactual);
                break;
            case 2:
                infoMedidorEntrada.lectura_TablaMedidorEntrada(variables.contadoractual);
                variables.clienteactual = parseStringToInteger(infoMedidorEntrada.gettablaMedidorEntrada_cliente().trim());
                VariablesGlobales.registroactual = parseStringToInteger(infoMedidorEntrada.gettablaMedidorEntrada_primerregistro().trim());
                infoRegistroEntrada.lectura_TablaRegistroDeEntrada(VariablesGlobales.registroactual);
                infoClienteEntrada.lectura_TablaEntradaClientes(variables.clienteactual);
                break;
            case 3:
                infoClienteEntrada.lectura_TablaEntradaClientes(variables.clienteactual);
                variables.contadoractual = parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_primermedidor().trim());
                infoMedidorEntrada.lectura_TablaMedidorEntrada(variables.contadoractual);
                VariablesGlobales.registroactual = parseStringToInteger(infoMedidorEntrada.gettablaMedidorEntrada_primerregistro().trim());
                infoRegistroEntrada.lectura_TablaRegistroDeEntrada(VariablesGlobales.registroactual);
                break;
        }

        // Log.e("error", VariablesGlobales.registroactual + " apuntadores " + variables.clienteactual);
        infoRegistroSalida.lectura_TablaRegistroSalida(VariablesGlobales.registroactual);
        infoClienteSalida.lectura_TablaClienteSalida(variables.clienteactual);
        medidorSalida.lectura_TablaContadorSalida(variables.contadoractual);
        Log.e("INFO", "inicia leyo salidas 1: " + variables.clienteactual);

        /** Una idea para las provisionales **/
        /*if ((infoRegistroEntrada.gettablaRegistroDeEntrada_tipoenergia().equals("R") || infoRegistroEntrada.gettablaRegistroDeEntrada_tipoenergia().equals("A")) && parseStringToInteger(infoMedidorEntrada.gettablaMedidorEntrada_Nroregistros().trim()) == 2) {
            VariablesGlobales.pos_registrador = parseStringToInteger(infoMedidorEntrada.gettablaMedidorEntrada_primerregistro().trim()) + 1;
        }else{
            VariablesGlobales.pos_registrador = parseStringToInteger(infoMedidorEntrada.gettablaMedidorEntrada_primerregistro().trim());
        }*/

        /** Actualmente funcionando en terreno **/
        /*if (infoRegistroEntrada.gettablaRegistroDeEntrada_tipoenergia().equals("R") && parseStringToInteger(infoMedidorEntrada.gettablaMedidorEntrada_Nroregistros().trim()) == 2) {
            VariablesGlobales.pos_registrador = parseStringToInteger(infoMedidorEntrada.gettablaMedidorEntrada_primerregistro().trim()) + 1;
        }else{
            VariablesGlobales.pos_registrador = parseStringToInteger(infoMedidorEntrada.gettablaMedidorEntrada_primerregistro().trim());
        }*/
        cod_cuentaUlt_foto = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim();
        mesUlt_foto = infoClienteEntrada.gettablaEntradaClientes_mes().trim();
        nrocontadordb = infoRegistroEntrada.gettablaRegistroDeEntrada_Digitos().trim();

        int conceptoactual = parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_primercobro().trim());
        int nroconceptos = parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_nrocobros().trim());
        infoCobrosLiquidados.abrir_TablaCobrosRealizados(infoCobrosLiquidados.getArchivo_TablaCobrosRealizados());
        int concuerda = 0;

        for (int x = 0; x < nroconceptos; x++) {
            infoCobrosLiquidados.lectura_TablaCobrosRealizados(conceptoactual);
            if (infoClienteEntrada.gettablaEntradaClientes_Cuenta().equals(infoCobrosLiquidados.gettablaCobrosRealizados_NROCUENTACLIENTE())) {
                concuerda++;
            }
            conceptoactual++;
        }
        Log.e("INFO", "inicia leyo conceptos 1: " + variables.clienteactual);

        validarUbicacionContraPredioActual();
        Log.e("INFO","leerInformacionUsuario() MODO: " + MODO);
        if(MODO.equals("MANU") || MODO.isEmpty()){
            // Verificar si este cliente requiere foto OCR obligatoria
            if (infoClienteSalida.gettablaClienteSalida_INDFACTURACION().trim().equals("")) {
                verificarFotoObligatoriaAlCargarCliente();
            }
        }
        if (infoClienteSalida.gettablaClienteSalida_CUENTA().trim().equals(medidorSalida.gettablaContadorSalida_CUENTA().trim())
                && infoClienteSalida.gettablaClienteSalida_CUENTA().trim().equals(infoRegistroSalida.gettablaRegistroSalida_CUENTA().trim())
                && infoClienteSalida.gettablaClienteSalida_CUENTA().trim().equals(infoRegistroEntrada.gettablaRegistroDeEntrada_CUENTA().trim())
                && infoClienteSalida.gettablaClienteSalida_CUENTA().trim().equals(infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim())
                && infoClienteSalida.gettablaClienteSalida_CUENTA().trim().equals(infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim())
                && concuerda == nroconceptos) {
            Log.e("INFO", "todo ok en leer planos 1: " + variables.clienteactual);
            return 1;
        } else {
            /*utils.Log(logfile, "[MenuDeLiquidacion]leerInformacionUsuario(): " + "Alerta archivos corruptos\n" + "El Codigo de Cuenta son Diferentes-\n" + "Verificar Archivos de Entrada.\n" + "infoClienteEntrada.cuenta: " +
                    infoClienteEntrada.gettablaEntradaClientes_Cuenta() + "\n " + "infoClienteSalida.cuenta: " + infoClienteSalida.gettablaClienteSalida_CUENTA() +
                    "\n " + "medidorSalida.cuenta: " + medidorSalida.gettablaContadorSalida_CUENTA() + "\n " + "infoRegistroSalida.cuenta: "
                    + infoRegistroSalida.gettablaRegistroSalida_CUENTA() + "\n " + "infoRegistroEntrada.cuenta: " + infoRegistroEntrada.gettablaRegistroDeEntrada_CUENTA() + "\n" +
                    "Concepto cobro.cuenta: " + infoCobrosLiquidados.gettablaCobrosRealizados_NROCUENTACLIENTE());
            if (ValidandoNoEnv == 0) {
                mensajeT("Alerta archivos corruptos\n" + "El Codigo de Cuenta son Diferentes-\n" + "Verificar Archivos de Entrada.\n" + "infoClienteEntrada.cuenta: " +
                        infoClienteEntrada.gettablaEntradaClientes_Cuenta() + "\n " + "infoClienteSalida.cuenta: " + infoClienteSalida.gettablaClienteSalida_CUENTA() +
                        "\n " + "medidorSalida.cuenta: " + medidorSalida.gettablaContadorSalida_CUENTA() + "\n " + "infoRegistroSalida.cuenta: "
                        + infoRegistroSalida.gettablaRegistroSalida_CUENTA() + "\n " + "infoRegistroEntrada.cuenta: " + infoRegistroEntrada.gettablaRegistroDeEntrada_CUENTA() + "\n" +
                        "Concepto cobro.cuenta: " + infoCobrosLiquidados.gettablaCobrosRealizados_NROCUENTACLIENTE(), 6000);
            }

            return 0;*/
            //Log.e("INFO","ReparaRegis|Me: " + infoMedidorEntrada.gettablaMedidorEntrada_cliente().trim());
            int resultado1 = cualEsDiferente(infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim(), infoClienteSalida.gettablaClienteSalida_CUENTA().trim(),
                    infoMedidorEntrada.gettablaMedidorEntrada_Cuenta().trim(), medidorSalida.gettablaContadorSalida_CUENTA().trim(),
                    infoRegistroEntrada.gettablaRegistroDeEntrada_CUENTA().trim(), infoRegistroSalida.gettablaRegistroSalida_CUENTA().trim());
            Log.e("INFO", "ReparaRegis|CS y conS: " + infoClienteSalida.gettablaClienteSalida_CUENTA().trim() + ".equals(" + medidorSalida.gettablaContadorSalida_CUENTA().trim() +
                    "&& CS y rs " + infoClienteSalida.gettablaClienteSalida_CUENTA().trim() + ".equals(" + infoRegistroSalida.gettablaRegistroSalida_CUENTA().trim() +
                    "&& cs y re " + infoClienteSalida.gettablaClienteSalida_CUENTA().trim() + ".equals(" + infoRegistroEntrada.gettablaRegistroDeEntrada_CUENTA().trim() +
                    "&& cs y clE" + infoClienteSalida.gettablaClienteSalida_CUENTA().trim() + ".equals(" + infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() +
                    "&& cs y ce " + infoClienteSalida.gettablaClienteSalida_CUENTA().trim() + ".equals(" + infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() +
                    "&&" + concuerda + "==" + nroconceptos + " Diferente es " + resultado1);

            infoRegistroSalida.settablaRegistroSalida_informe("debe recuperar INFO ReparaRegis|CS y conS: " + infoClienteSalida.gettablaClienteSalida_CUENTA().trim() + ".equals(" + medidorSalida.gettablaContadorSalida_CUENTA().trim() +
                    "&& CS y rs " + infoClienteSalida.gettablaClienteSalida_CUENTA().trim() + ".equals(" + infoRegistroSalida.gettablaRegistroSalida_CUENTA().trim() +
                            "&& cs y re " + infoClienteSalida.gettablaClienteSalida_CUENTA().trim() + ".equals(" + infoRegistroEntrada.gettablaRegistroDeEntrada_CUENTA().trim() +
                            "&& cs y clE" + infoClienteSalida.gettablaClienteSalida_CUENTA().trim() + ".equals(" + infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() +
                            "&& cs y ce " + infoClienteSalida.gettablaClienteSalida_CUENTA().trim() + ".equals(" + infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() +
                            "&&" + concuerda + "==" + nroconceptos + " Diferente es " + resultado1);

            int respRep = repararRegistrador(resultado1);
            return respRep;
        }
    }

    public int repararRegistrador(int PlanoMalo) {
        try {
            if (infoClienteSalida.gettablaClienteSalida_INDFACTURACION().trim().equals("") && infoClienteSalida.gettablaClienteSalida_HORAIMPRESION().trim().equals("")
                    && !infoRegistroSalida.gettablaRegistroSalida_CUENTA().trim().equals(infoClienteSalida.gettablaClienteSalida_CUENTA().trim())) {
                VariablesGlobales.pos_registrador = parseStringToInteger(infoMedidorEntrada.gettablaMedidorEntrada_primerregistro());
                /*Log.e("INFO","ReparaRegis|cliente: " + infoMedidorEntrada.gettablaMedidorEntrada_cliente().trim());
                Log.e("INFO","ReparaRegis|primermed: " + infoClienteEntrada.gettablaEntradaClientes_PRIMERMEDIDOR().trim());
                Log.e("INFO","ReparaRegis|cuenta: " + infoClienteSalida.gettablaClienteSalida_CUENTA().trim());
                Log.e("INFO","ReparaRegis|marca: " + infoRegistroEntrada.getTablaRegistroDeEntrada_apuntadorregistro());
                Log.e("INFO","ReparaRegis|contador: " + infoRegistroEntrada..gettablaRegistroDeEntrada_nrocontador());*/
                variables.totalprediosleidos = variables.totalprediosleidos - 1;
                VariablesGlobales.registroactual = VariablesGlobales.pos_registrador;
                String[] time = getTimeError().split("\\|");
                utils.Log(logfile, infoClienteEntrada.gettablaEntradaClientes_Cuenta() + "|" + Ciclo.substring(0, 3) + "|" + infoClienteEntrada.gettablaEntradaClientes_mes() + "|" + infoClienteEntrada.gettablaEntradaClientes_anio() + "|" +
                        serialPDA + "|" + "[MenuDeLiquidacion]repararRegistrador()|" + "Se repara registrador " + "|" + time[0] + "|" + time[1] + "|");

                infoRegistroSalida.settablaRegistroSalida_cliente(infoMedidorEntrada.gettablaMedidorEntrada_cliente().trim());
                infoRegistroSalida.settablaRegistroSalida_contador(infoClienteEntrada.gettablaEntradaClientes_primermedidor().trim());
                infoRegistroSalida.settablaRegistroSalida_cuenta(infoClienteSalida.gettablaClienteSalida_CUENTA().trim());
                //alerta que podemos hacer aqui
                // infoRegistroSalida.settablaRegistroSalida_marca(infoRegistroEntrada.getTablaRegistroDeEntrada_apuntadorregistro());
                infoRegistroSalida.settablaRegistroSalida_nrocontador(infoRegistroEntrada.gettablaRegistroDeEntrada_nrocontador());
                infoRegistroSalida.settablaRegistroSalida_marca(infoRegistroEntrada.gettablaRegistroDeEntrada_MARCA());
                infoRegistroSalida.settablaRegistroSalida_consecutivo(infoRegistroEntrada.gettablaRegistroDeEntrada_contador());
                infoRegistroSalida.settablaRegistroSalida_consumotomado("0");
                infoRegistroSalida.settablaRegistroSalida_intentos("0");
                infoRegistroSalida.settablaRegistroSalida_leido("");
                infoRegistroSalida.escribir_TablaRegistroSalida(VariablesGlobales.pos_registrador);//repara registrador
                infoRegistroSalida.lectura_TablaRegistroSalida(VariablesGlobales.pos_registrador);
                ArchivosCorruptos = 0;
                return 1;
            } else {
                if (PlanoMalo == 5)//nos indica que el plano que se daño es registros salida y se podria recuperar
                {
                    VariablesGlobales.pos_registrador = parseStringToInteger(infoMedidorEntrada.gettablaMedidorEntrada_primerregistro());
                    infoRegistroSalida.settablaRegistroSalida_cliente(infoMedidorEntrada.gettablaMedidorEntrada_cliente().trim());
                    infoRegistroSalida.settablaRegistroSalida_contador(infoClienteEntrada.gettablaEntradaClientes_primermedidor().trim());
                    infoRegistroSalida.settablaRegistroSalida_cuenta(infoClienteSalida.gettablaClienteSalida_CUENTA().trim());
                    infoRegistroSalida.settablaRegistroSalida_nrocontador(infoRegistroEntrada.gettablaRegistroDeEntrada_nrocontador());
                    infoRegistroSalida.settablaRegistroSalida_marca(infoRegistroEntrada.gettablaRegistroDeEntrada_MARCA());
                    infoRegistroSalida.settablaRegistroSalida_consecutivo(infoRegistroEntrada.gettablaRegistroDeEntrada_contador());
                    infoRegistroSalida.settablaRegistroSalida_consumotomado(infoClienteSalida.getTablaClienteSalida_CONSUMOFACTURADO().trim());
                    //sacar la lectura asi:
                    int LecturatomadaNueva = parseStringToInteger(infoClienteSalida.getTablaClienteSalida_CONSUMOFACTURADO()) + parseStringToInteger(infoRegistroEntrada.gettablaRegistroDeEntrada_Lecturaanterior().trim());
                    infoRegistroSalida.settablaRegistroSalida_lecturatomada("" + LecturatomadaNueva);
                    infoRegistroSalida.settablaRegistroSalida_intentos("1");
                    infoRegistroSalida.settablaRegistroSalida_leido("3");
                    infoRegistroSalida.settablaRegistroSalida_fechalectura(infoClienteSalida.gettablaClienteSalida_FECHAIMPRESION());
                    infoRegistroSalida.settablaRegistroSalida_horalectura(infoClienteSalida.gettablaClienteSalida_HORAIMPRESION());
                    infoRegistroSalida.settablaRegistroSalida_informe("RECUPERADO DE LA MOVIL GSS");

                    infoRegistroSalida.escribir_TablaRegistroSalida(VariablesGlobales.pos_registrador);//repara registrador
                    infoRegistroSalida.lectura_TablaRegistroSalida(VariablesGlobales.pos_registrador);
                    ArchivosCorruptos = 0;
                    return 1;
                } else {
                    ArchivosCorruptos = 1;
                    String[] time = getTimeError().split("\\|");
                    utils.Log(logfile, infoClienteEntrada.gettablaEntradaClientes_Cuenta() + "|" + Ciclo.substring(0, 3) + "|" + infoClienteEntrada.gettablaEntradaClientes_mes() + "|" + infoClienteEntrada.gettablaEntradaClientes_anio() + "|" +
                            serialPDA + "|" + "[MenuDeLiquidacion]leerInformacionUsuario()|" + "Alerta archivos corruptos\n" + "El Codigo de Cuenta son Diferentes-\n" + "Verificar Archivos de Entrada.\n" + "infoClienteEntrada.cuenta: " +
                            infoClienteEntrada.gettablaEntradaClientes_Cuenta() + "\n " + "infoClienteSalida.cuente: " + infoClienteSalida.gettablaClienteSalida_CUENTA() +
                            "\n " + "medidorSalida.cuenta: " + medidorSalida.gettablaContadorSalida_CUENTA() + "\n " + "infoRegistroSalida.cuenta: "
                            + infoRegistroSalida.gettablaRegistroSalida_CUENTA() + "\n " + "infoRegistroEntrada.cuenta: " + infoRegistroEntrada.gettablaRegistroDeEntrada_CUENTA() + "|" + time[0] + "|" + time[1] + "|");
                    if (ValidandoNoEnv == 0) {
                        mensajeT("Alerta archivos corruptos\n" + "El Codigo de Cuenta son Diferentes-\n" + "Verificar Archivos de Entrada.\n" + "infoClienteEntrada.cuenta: " +
                                infoClienteEntrada.gettablaEntradaClientes_Cuenta() + "\n " + "infoClienteSalida.cuente: " + infoClienteSalida.gettablaClienteSalida_CUENTA() +
                                "\n " + "medidorSalida.cuenta: " + medidorSalida.gettablaContadorSalida_CUENTA() + "\n " + "infoRegistroSalida.cuenta: "
                                + infoRegistroSalida.gettablaRegistroSalida_CUENTA() + "\n " + "infoRegistroEntrada.cuenta: " + infoRegistroEntrada.gettablaRegistroDeEntrada_CUENTA(), 10000);
                    }
                    return 0;
                }
            }
        } catch (Exception ex) {
            String[] time = getTimeError().split("\\|");
            utils.Log(logfile, infoClienteEntrada.gettablaEntradaClientes_Cuenta() + "|" + Ciclo.substring(0, 3) + "|" + infoClienteEntrada.gettablaEntradaClientes_mes() + "|" + infoClienteEntrada.gettablaEntradaClientes_anio() + "|" +
                    serialPDA + "|" + "[MenuDeLiquidacion]repararRegistrador()|" + "Error al intentar reparar registrador: " + ex.getMessage() + "|" + time[0] + "|" + time[1] + "|");
            // return 0;
        }
        return 0;
    }

    public int cualEsDiferente(String v1, String v2, String v3, String v4, String v5, String v6) {
        String[] valores = {v1, v2, v3, v4, v5, v6};
        HashMap<String, Integer> conteo = new HashMap<>();
        Log.e("INFO", "valores 1: " + v1 + "&& v2 " + v2 + "&& v3 " + v3 +
                "&& v4 " + v4 + " && v5 " + v5 + "&& v6 " + v6);

        for (String valor : valores) {
            conteo.put(valor, conteo.getOrDefault(valor, 0) + 1);
        }

        String valorDiferente = null;
        for (String valor : conteo.keySet()) {
            if (conteo.get(valor) == 1) {
                valorDiferente = valor;
                break;
            }
        }

        if (valorDiferente != null) {
            for (int i = 0; i < valores.length; i++) {
                if (valores[i].equals(valorDiferente)) {
                    return i;
                }
            }
        }

        return -1;
    }

    private String getTimeError() {
        try {
            Calendar cal = new GregorianCalendar();
            Date date = cal.getTime();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String formatteDate = df.format(date);

            Date dt = new Date();
            SimpleDateFormat hf = new SimpleDateFormat("HH:mm:ss");
            String formatteHour = hf.format(dt.getTime());

            return formatteDate + "|" + formatteHour;
        } catch (Exception e) {
            utils.Log(logfile, infoClienteEntrada.gettablaEntradaClientes_Cuenta() + "|" + Ciclo.substring(6, 9) + "|" + infoClienteEntrada.gettablaEntradaClientes_mes() + "|" + infoClienteEntrada.gettablaEntradaClientes_anio() + "|" +
                    serialPDA + "|" + "[MenuDeLiquidacion]getTimeError()|" + e + "|" + "2023-01-05" + "|" + "09:27:15" + "|");
            return "2023-01-05" + "|" + "09:27:15";
        }
    }

    /**
     * Ax: Obetiene informacin del cel para saber modlo y ctivar sacnners de hardawre
     */
    public void getInfoCel() { //Ax Obtiene  el modelo de TechData, para luego activar el scanner

        String s = android.os.Build.MODEL + " " + android.os.Build.PRODUCT;
        if (s.contains("PA700")) {
            usarScannerHardware = "PA700";
        }

        if (s.toUpperCase().contains("KT40Q")) {
            usarScannerHardware = "KT40Q";
        }
        //
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) { //Ax: propio de android.support.v7.widget.Toolbar. Desde  res...menu...main.xml // Inflar el menu; esto adiciona items a la actionBar si existe
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

    public void opcionMenu(int item) { //tools

        txtElectura.setText("");

        switch (item) {
            case 1: // Capturar Anomalía
                code.setLength(0);
                if (autorizacion()) {
                    capturarAnomaliaTerreno();
                }
                break;
            case 2: // Reimprimir Factura
                code.setLength(0);
                // nuevo metodo para reimprimir la factura sin que tenga que ir a a la opcion ir ultima cuenta leida
                abrirArchivosDeFacturacion();

                alterno = VariablesGlobales.registroactual;
                if (infoClienteSalida.gettablaClienteSalida_INDFACTURACION().trim().equals("") && infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim().equals("") && infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA().trim().equals("")) {
                    VariablesGlobales.registroactual = variables.ultimoregistro;
                }//1117493705

                if (infoClienteSalida.gettablaClienteSalida_INDFACTURACION().trim().equals("") && (!infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim().equals("") || !infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA().trim().equals(""))) {
                    rebuildData();
                } else {
                    // Log.e("error", VariablesGlobales.registroactual + " registros " + variables.ultimoregistro);
                    leerInformacionUsuario(1);
                    visualizarInformacionCliente(0);
                    if (infoClienteEntrada.gettablaEntradaClientes_Clasedeservicio().trim().equals("IC") || infoClienteEntrada.gettablaEntradaClientes_Clasedeservicio().trim().equals("MC")) {
                        mensajeT("ESTA CUENTA ES MACRO MEDIDA NO SE GENERA LIQUIDACION NI FACTURA", msgLargo);
                    } else {
                        MostrarAlertDialog("ALERTA DE IMPRESION", "Desea Reimprimir el Cliente?", "reimpresiondefactura");
                    }
                }
                break;
            case 3: // Anterior Predio
                code.setLength(0);
                visualizarPredioLeido();
                break;
            case 4: // Capturar Novedad Lect
                code.setLength(0);
                mensajeNovedad = "CUENTA NUEVA";
                adicionarNovedad(1, 0);
                break;
            case 5: // Modificar Nro Digitos
                cambiodigitos();
                break;
            case 6: // Buscar Cliente
                code.setLength(0);
                ejecutarBusquedaCliente();
                break;
            case 7: // Des/Habilitar Impresora
                code.setLength(0);
                if (!conn) break;

                abrirArchivosDeFacturacion();

                if (VariablesGlobales.habilitadaimpresora == 0) { //Ax: puede que solo se haya deshabilitado

                    String conectadoa = "";

                    ImpresoraStatusMsg("Impresora habilitada ?");
                    VariablesGlobales.habilitadaimpresora = 1;
                    if (EsImpresora521 == 1)
                        imagenPrinter.setImageResource(R.drawable.impresora_ok521);//.impresora_ok);
                    else
                        imagenPrinter.setImageResource(R.drawable.impresora_ok420);//.impresora_ok);
                    //imagenPrinter.setImageResource(R.drawable.impresora_ok);


                } else { //Ax: solo la deshabilito
                    ImpresoraStatusMsg("Impresora Deshabilitada! x");
                    VariablesGlobales.habilitadaimpresora = 0;
                    imagenPrinter.setImageResource(R.drawable.impresora_ko);
                }

                infoRegistroSalida.lectura_TablaRegistroSalida(VariablesGlobales.registroactual);
                visualizarInformacionCliente(0);
                cerrarArchivosFacturacion();
                break;
            case 8: // Des/Asociar Impresora
                code.setLength(0);
                if (!conn) break; //Si se esta conectado

                abrirArchivosDeFacturacion(); //Ax:?

                if (!VariablesGlobales.bDiscoveryStarted) {

                    String conectadoa = "";

                    if (VariablesGlobales.btPrintService.getState() != btPrintFile.STATE_DISCONNECTED && VariablesGlobales.btPrintService.getState() != btPrintFile.STATE_IDLE) {
                        conectadoa = utils.ReadLine(logPrint);
                    }
                    VariablesGlobales.bDiscoveryStarted = true;
                    // Lanza DeviceListActivity para ver dispositivos y escanear

                    Bundle bundlei = new Bundle();
                    bundlei.putString("conectadoa", conectadoa);
                    Intent serverIntent = new Intent(this, DeviceListActivity.class);
                    serverIntent.putExtras(bundlei);

                    startActivityForResult(serverIntent, VariablesGlobales.REQUEST_CONNECT_DEVICE);
                }

                infoRegistroSalida.lectura_TablaRegistroSalida(VariablesGlobales.registroactual);
                visualizarInformacionCliente(0);
                cerrarArchivosFacturacion();
                break;
            case 9: // Configurar Preimpreso Fact
                code.setLength(0);
                Bundle bundlei = new Bundle();
                bundlei.putString("directorioactual", VariablesGlobales.directorioactual);
                bundlei.putInt("indicador", sinBarSence);
                bundlei.putInt("indicadorModeloImpresora", EsImpresora521);
                Intent i = new Intent(this, ModuloConfigFormatoImpresion.class);
                i.putExtras(bundlei);
                startActivityForResult(i, CONFIGFORMATIMP_REQUEST_CODE);
                break;
            case 10: // Estadistica Del Proceso
                try {
                    code.setLength(0);
                    Bundle bundle2 = new Bundle();
                    bundle2.putString("aforador", lector);
                    bundle2.putString("directorioActual", VariablesGlobales.directorioactual);
                    bundle2.putString("nombrePredio", variables.getNombrepredio());
                    bundle2.putString("terminal", terminal);
                    Intent intent = new Intent(this, ResumenEstadistico.class);
                    intent.putExtras(bundle2);
                    startActivity(intent);

                } catch (Exception e) {
                    mensajeT("Error al iniciar actividad de Resumen Estadistico" + e.getMessage(), msgLargo);
                }
                break;
            case 11: // Tomar Foto
                code.setLength(0);
                capturaImagenFotografica();
                break;
            case 12: // Utilidades
                try {
                    code.setLength(0);
                    Intent myIntent = new Intent(this, FechayHora.class);
                    startActivity(myIntent);
                } catch (Exception ex) {
                    mensajeT("Error al iniciar actividad de Utilidades" + ex.getMessage(), msgLargo);
                }
                break;
            case 13: // Enviar Facturacion Gprs
                code.setLength(0);
                if (procesandoenvioenHilos == 0) {

                    Bundle bundlex = new Bundle();
                    bundlex.putString("directorioactual", VariablesGlobales.directorioactual);
                    bundlex.putString("serial", serialPDA);
                    Intent it = new Intent(this, ModuloConsultaNoEnviados.class);
                    it.putExtras(bundlex);
                    // Log.e("error", "enviogprs10 " + VariablesGlobales.directorioactual);
                    startActivityForResult(it, CONSULTNONVIADS_REQUEST_CODE);
                } else {
                    mensajeT("El sistema esta en proceso de envio de datos por HILOS al servidor,\n No se permite realizar este proceso", msgMedio);
                }
                break;
            case 14: // Procesar Reenvio Gprs
                code.setLength(0);
                File Archivo1 = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/ENVIOSGPRS.SDA");
                File Archivo2 = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/ENVIOGPRS" + serialPDA + ".SDA");
                if (procesandoenvioenHilos == 0) {
                    if (Archivo1.exists() || Archivo2.exists()) {
                        //ojo con esto esta funcionando mal
                        if (Archivo1.length() == 0) Archivo1.delete();
                        if (Archivo2.length() == 0) Archivo2.delete();

                        mensajeT("Aun hay archivos por enviar de EnviosGPRS, \n intente enviarlos primero", msgLargo);
                    } else {
                        File ArchivoBK = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/BKENVIOSGPRS.SDA");

                        if (ArchivoBK.exists()) {
                            try {
                                if (utils.CrearCopia(ArchivoBK.getAbsolutePath(), Archivo1.getAbsolutePath())) {

                                    if (ArchivoBK.delete()) {

                                        envioFacturacionHilos(); //Ax: nuevo, se envia de una para evitar posibles errores
                                        mensajeT("Se da comienzo al reenvio de informacion", msgLargo);
                                    } else {
                                        if (ArchivoBK.exists()) Archivo1.delete();
                                        mensajeT("Error en la copia, intente de nuevo", msgLargo);
                                    }  //utils.ModificarArchivo(ArchivoBK.getAbsolutePath(), Archivo1.getAbsolutePath(), "M", 293, 294);//Ax: ya no se usa, no se deben enviar M's en esta reconstruccion, el ws verifica si ya existe
                                } else {
                                    mensajeT("Error al crear Copia", msgLargo);
                                }
                            } catch (Exception ex) {
                                mensajeT("Error no se pudo hacer copia de Backup... " + ex, msgLargo);
                            }
                        } else
                            mensajeT("No existe BKEnviosGPRS para realizar este proceso", msgLargo);
                    }
                } else {
                    mensajeT("El sistema esta en proceso de envio de datos por HILOS al servidor,\n No se permite realizar este proceso", msgMedio);
                }
                break;
            case 15: // Evaluar Noenviadas Gprs
                code.setLength(0);
                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                builder2.setTitle("ALERTA EVALUAR ENVIOS");
                builder2.setMessage("DESEA EVALUAR SI TODAS SUS LECTURAS FUERON ENVIADAS? ");

                builder2.setIcon(R.drawable.ic_launcher1);

                builder2.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        myThread.interrupt();
                        VariablesGlobales.btPrintService.stop();
                        mensajeAdm(MenuDeLiquidacion.this); // No enviados
                        metodo = "ValidarNoEnviados";
                        msgAdmon = String.format("%-42s", "VALIDANDO REGISTROS NO ENVIADOS");
                        TaskHelper.execute(new AsyncCallWS(), metodo);
                    }
                });
                builder2.show();                // fin del proceso de reimpresion
                limpiarMedidores();
                break;
           /* case 16:// Reconstruir Envio cuenta GPRS
                code.setLength(0);

                break;*/

            case 16: // Liquidación Automática
                code.setLength(0);
                MostrarAlertDialog("ALERTA!", "DESEA INICIAR PROCESO \n *** OPCIONES ***\n1. RESUMEN_ENVIOSGPRS.TXT\n2. RESUMEN_REGISTRO.TXT\n3. RESUMEN_TABLADB.TXT \n[SI]AUTOMATICO \n[NO] MANUAL", "liquidacionautomatica");
                break;
            case 17: // Enviar Fotos
                code.setLength(0);
                if (procesandoenvioenHilos == 0) {
                    EnviarFotos();
                    //aqui enviar fotos
                } else {
                    mensajeT("El sistema esta en proceso de envio de datos por HILOS al servidor,\n No se permite realizar este proceso", msgMedio);
                }
                break;
            case 18: // Activar Wifi
                code.setLength(0);
                try {
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                    if (wifiManager.isWifiEnabled()) {
                        wifiManager.setWifiEnabled(false);
                        Toast.makeText(getApplicationContext(), "Se ha Apagado el WIFI (X)", Toast.LENGTH_SHORT).show();
                    } else {
                        wifiManager.setWifiEnabled(true);
                        Toast.makeText(getApplicationContext(), "Se ha ENCENDIDO el WIFI (✓)", Toast.LENGTH_SHORT).show();
                    }


                } catch (Exception ex) {
                    mensajeT("Error al Activar WIFI \n" + ex.getMessage(), msgLargo);
                }
                break;
            case 19: // Mapa 5970916
                code.setLength(0);
                /*if (!infoClienteEntrada.gettablaEntradaClientes_INFORMACIONADICIONAL().substring(213, 233).trim().equals("")) {
                    Bundle bundleM = new Bundle();
                    bundleM.putString("lon", infoClienteEntrada.gettablaEntradaClientes_INFORMACIONADICIONAL().substring(213, 233).trim());
                    bundleM.putString("lat", infoClienteEntrada.gettablaEntradaClientes_INFORMACIONADICIONAL().substring(234, 254).trim());
                    Intent itM = new Intent(this, MapsActivity.class);
                    itM.putExtras(bundleM);
                    startActivityForResult(itM, MAPA);
                } else {
                    Toast.makeText(getApplicationContext(), "Esta cuenta no tiene coordenadas registradas", Toast.LENGTH_SHORT).show();
                }*/
                String latDest = infoClienteEntrada.gettablaEntradaClientes_cordenaday().trim();
                String lonDest = infoClienteEntrada.gettablaEntradaClientes_cordenadax().trim();

                if (!latDest.isEmpty() && !lonDest.isEmpty()) {
                    try {
                        double lat = Double.parseDouble(latDest);
                        double lon = Double.parseDouble(lonDest);
                        // Lanza Waze → Maps → MapsActivity (fallback automático)
                        NavigationHelper.navigateTo(this, lat, lon);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Las coordenadas de esta cuenta son inválidas.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Esta cuenta no tiene coordenadas registradas.", Toast.LENGTH_SHORT).show();
                }
                break;
            case 20: // envio registros
                code.setLength(0);
//                AlertDialog.Builder builder3 = new AlertDialog.Builder(MenuDeLiquidacion.this);
//                builder3.setTitle("ALERTA ENVIO REGISTROS");
//                builder3.setMessage("DESEA REALIZAR EL ENVIO DE LOS REGISTROS? ");
//
//                builder3.setIcon(R.drawable.ic_launcher1);
//
//                builder3.setPositiveButton("Si", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        borradoEnvios();
//                        ValidarNoEnviados();
//                        envioFacturacionHilos();
//                        crearArchivoO();
//                    }
//                });
//                builder3.show();                // fin del proceso de reimpresion
//                limpiarMedidores();
                break;
            case 21: // Volver
                code.setLength(0);
                try {
                    if (myThread.isAlive()) {
                        myThread.interrupt();
                    }
                } catch (Exception ex) {
                    salirse = true;
                }
                // Log.e("error", "hace el back 2");
                finish();
                break;
        }
    }

    public void cambiodigitos() {
        try {
            code.setLength(0);
            VariablesGlobales.datodebusqueda = "";

            Bundle bundle = new Bundle();

            bundle.putString("DIRECTORIOACTUAL", "" + VariablesGlobales.getDirectorioactual());
            bundle.putInt("NUMEROREGISTROACTUAL", VariablesGlobales.getRegistroactual());

            Intent di = new Intent(MenuDeLiquidacion.this, ModuloCambioDigitos.class);
            di.putExtras(bundle);
            startActivityForResult(di, CAMBIODIGITOS_REQUEST_CODE);

        } catch (Exception e) {
            utils.Log(logfile, "[MenuDeLiquidacion]cambiodigitos: " + e.getMessage());
        }
    }

    public void rebuildData() {
        try {
            limpiarMedidores();
            if (infoClienteSalida.gettablaClienteSalida_INDFACTURACION().trim().equals("")) {
                //Clientes Salida
                infoClienteSalida.settablaClienteSalida_LECTOR(String.format("%1$10s", lector));
                infoClienteSalida.settablaClienteSalida_TERMINAL(String.format("%1$15s", terminal));
                infoClienteSalida.settablaClienteSalida_IMPRESORA(String.format("%1$15s", impresora));
                infoClienteSalida.settablaClienteSalida_INDFACTURACION("L");
                infoClienteSalida.settablaClienteSalida_fechavence(infoClienteEntrada.gettablaEntradaClientes_fechavence()); //[enerca] se añade
                infoClienteSalida.settablaClienteSalida_fechacorte(infoClienteEntrada.gettablaEntradaClientes_fechacorte()); //[enerca] se añade
                infoClienteSalida.settablaClienteSalida_FECHAIMPRESION(variables.amd);
                infoClienteSalida.settablaClienteSalida_HORAIMPRESION(variables.hm);
                infoClienteSalida.escribir_TablaClienteSalida(variables.clienteactual);
                //Medidor Salida
                medidorSalida.settablaContadorSalida_Fechalectura(variables.amd);
                medidorSalida.settablaContadorSalida_HORALECTURA(variables.hm);
                medidorSalida.settablaContadorSalida_ESTADOCRITICA(infoRegistroSalida.gettablaRegistroSalida_LEIDO());
                medidorSalida.settablaContadorSalida_ULTIMOMEDIDORLEIDO("0");


                double t2 = (Double.parseDouble(variables.hm.substring(0, 2)) * 3600 + Double.parseDouble(variables.hm.substring(2, 4)) * 60 + Double.parseDouble(variables.hm.substring(4, 6)));

                if (variables.ultimotiempo == 1) {
                    medidorSalida.settablaContadorSalida_TIEMPO(parseStringToInteger((("40") + "").trim()) + "");
                    variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";Escribio 40 aqui  7;" + getPhoneDate() + "-" + getPhoneHour();
                    escribeResumenTiempo(variableResumenLiquidacion + " " + "Ecbribio en tiempo inicial 40 aqui 7|" + VariablesGlobales.registroactual);

                } else if (variables.ultimotiempo > t2) {
                    medidorSalida.settablaContadorSalida_TIEMPO(parseStringToInteger(((variables.ultimotiempo - t2) + "").trim()) + "");
                } else {
                    medidorSalida.settablaContadorSalida_TIEMPO(parseStringToInteger(((t2 - variables.ultimotiempo) + "").trim()) + "");
                }
                medidorSalida.escribir_TablaContadorSalida(variables.contadoractual);
            }
            procesarLectura2();//Reimpresion
        } catch (Exception ex) {
            utils.Log(logfile, "[MenuDeLiquidacion]rebuildData: " + ex.getMessage());
        }
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) { //Ax: propio de android.support.v7.widget.Toolbar              tools
//        // Handle action bar item clicks here. The action bar will // automatically handle clicks on the Home/Up button, so long // as you specify a parent activity in AndroidManifest.xml.
//        txtElectura.setText("");//Ax: borrar lectura si se ha escogido algun item del menu
//        int id = item.getItemId();
//
//        switch (id) {
//            case R.id.capturar_anomalia:
//
//                if (autorizacion()) {
//                    capturarAnomaliaTerreno();
//                }
//                break;
//
//            case R.id.reimprimir_factura:
//
//                // nuevo metodo para reimprimir la factura sin que tenga que ir a a la opcion ir ultima cuenta leida
//                abrirArchivosDeFacturacion();
//
//                alterno = VariablesGlobales.registroactual;
//                if (infoClienteSalida.gettablaClienteSalida_INDFACTURACION().trim().equals(""))
//                    VariablesGlobales.registroactual = variables.ultimoregistro;
//
//                // Log.e("error", VariablesGlobales.registroactual + " registros " + variables.ultimoregistro);
//                leerInformacionUsuario(1);
//                visualizarInformacionCliente(0);
//
//                MostrarAlertDialog("ALERTA DE IMPRESION", "Desea Reimprimir el Cliente?", "reimpresiondefactura");
//
//                break;
//            case R.id.anterior_predio_leido: // ultimo predio leido
//                visualizarPredioLeido();
//                break;
//         /*   case R.id.capturar_comentario:
//                // incluir comentarios
//                abrirArchivosDeFacturacion();
//                if (validarEstadoRegistro(VariablesGlobales.registroactual, "L") == 0) {
//
//                    try {
//                        Bundle bundle = new Bundle();
//
//                        bundle.putString("DIRECTORIOACTUAL", "" + VariablesGlobales.getDirectorioactual());
//                        bundle.putInt("NUMEROREGISTROACTUAL", VariablesGlobales.getRegistroactual());
//
//                        Intent i = new Intent(this, PanelComentarios.class);
//                        i.putExtras(bundle);
//
//                        //startActivity(i);
//                        startActivityForResult(i, CAPT_COMENTARIO_REQUEST_CODE);
//
//                    } catch (Exception e) {
//                        mensajeT("Error al iniciar actividad de capturar comentario" + e.getMessage(), msgLargo);
//                        // Toast.makeText(getApplicationContext(), "Error al iniciar actividad de capturar comentario" + e.getMessage(), Toast.LENGTH_LONG).show();
//                    }
//
//                } else
//                    mensajeT("Alerta de Comentario\n" + "Cuenta ya esta\n" + "Facturada en el sistema\n", msgMedio);
//                //Toast.makeText(getApplicationContext(), "Alerta de Comentario\n" + "Cuenta ya esta\n" + "Facturada en el sistema\n", Toast.LENGTH_LONG).show();
//                infoRegistroSalida.lectura_TablaRegistroSalida(VariablesGlobales.registroactual);
//                visualizarInformacionCliente(0);
//                cerrarArchivosFacturacion();
//                break;*/
//            case R.id.capturar_novedad_lect:
//                mensajeNovedad = "CUENTA NUEVA";
//                // Log.e("error", "novedad 1");
//                adicionarNovedad(1, 0);
//                break;
//            case R.id.modificar_nro_digitos:
//                // proceso de cambio de digitos
//                VariablesGlobales.datodebusqueda = "";
//
//                Bundle bundle = new Bundle();
//
//                bundle.putString("DIRECTORIOACTUAL", "" + VariablesGlobales.getDirectorioactual());
//                bundle.putInt("NUMEROREGISTROACTUAL", VariablesGlobales.getRegistroactual());
//
//                Intent di = new Intent(this, ModuloCambioDigitos.class);
//                di.putExtras(bundle);
//
//                startActivity(di);
//
//                abrirArchivosDeFacturacion();
//                infoRegistroSalida.lectura_TablaRegistroSalida(VariablesGlobales.registroactual);
//                if (!VariablesGlobales.datodebusqueda.trim().equals("")) {
//                    infoRegistroSalida.settablaRegistroSalida_informe("CAMBIO DIGITOS " + VariablesGlobales.datodebusqueda.trim());
//                    infoRegistroSalida.escribir_TablaRegistroSalida(VariablesGlobales.registroactual);
//                }
//
//                infoRegistroSalida.lectura_TablaRegistroSalida(VariablesGlobales.registroactual);
//                visualizarInformacionCliente(0);
//                cerrarArchivosFacturacion();
//                break;
//            case R.id.buscar_cliente:    // realizar la busqueda del cliente
//                ejecutarBusquedaCliente();
//                break;
//            case R.id.habilitar_impresora:// Ax: solo la deshabilita o habilita, si no esta conectada trata de conectarse
//
//                if (!conn) break;
//
//                abrirArchivosDeFacturacion();
//
//                if (VariablesGlobales.habilitadaimpresora == 0) { //Ax: puede que solo se haya deshabilitado
//
//                    String conectadoa = "";
//
//                    ImpresoraStatusMsg("Impresora habilitada ?");
//                    VariablesGlobales.habilitadaimpresora = 1;
//                    imagenPrinter.setImageResource(R.drawable.impresora);
//
//                } else { //Ax: solo la deshabilito
//                    ImpresoraStatusMsg("Impresora Deshabilitada! x");
//                    VariablesGlobales.habilitadaimpresora = 0;
//                    imagenPrinter.setImageResource(R.drawable.imagen_prin_apagada);
//                }
//
//                infoRegistroSalida.lectura_TablaRegistroSalida(VariablesGlobales.registroactual);
//                visualizarInformacionCliente(0);
//                cerrarArchivosFacturacion();
//                break;
//
//            case R.id.cambiar_impresora: //Ax: antes puerto_comunicacion  //Ax: todo
//
//                if (!conn) break; //Si se esta conectado
//
//                abrirArchivosDeFacturacion(); //Ax:?
//
//                if (!VariablesGlobales.bDiscoveryStarted) {
//
//                    String conectadoa = "";
//
//                    if (VariablesGlobales.btPrintService.getState() != btPrintFile.STATE_DISCONNECTED && VariablesGlobales.btPrintService.getState() != btPrintFile.STATE_IDLE) {
//                        conectadoa = utils.ReadLine(logPrint);
//                    }
//                    VariablesGlobales.bDiscoveryStarted = true;
//                    // Lanza DeviceListActivity para ver dispositivos y escanear
//
//                    Bundle bundlei = new Bundle();
//                    bundlei.putString("conectadoa", conectadoa);
//                    Intent serverIntent = new Intent(this, DeviceListActivity.class);
//                    serverIntent.putExtras(bundlei);
//
//                    startActivityForResult(serverIntent, VariablesGlobales.REQUEST_CONNECT_DEVICE);
//                }
//
//                infoRegistroSalida.lectura_TablaRegistroSalida(VariablesGlobales.registroactual);
//                visualizarInformacionCliente(0);
//                cerrarArchivosFacturacion();
//                break;
////            case R.id.ejecurar_impresion_lote:     ENERCA NO
////
////                // Impresion en Lotes
////                VariablesGlobales.rangoinicialimpresion = 0;
////                VariablesGlobales.rangofinalimpresion = 0;
////                VariablesGlobales.imprimirSoloPostal = 0;
////
////                bundle = new Bundle();
////                bundle.putInt("TOTALCLIENTES", infoClienteEntrada.getTotal_TablaEntradaClientes());
////                bundle.putString("DIRECTORIOACTUAL", VariablesGlobales.directorioactual);
////
////                Intent intent_ModuloSeleccionLote = new Intent(this, ModuloSeleccionLote.class);
////                intent_ModuloSeleccionLote.putExtras(bundle);
////
////                startActivityForResult(intent_ModuloSeleccionLote, SELECCIONDELOTE_REQUEST_CODE);
////                break;
//            case R.id.configurar_preImpreso_fact:  //Configuracion lineas de impresion
//
//                Bundle bundlei = new Bundle();
//                bundlei.putString("directorioactual", VariablesGlobales.directorioactual);
//                bundlei.putInt("indicador", sinBarSence);
//                Intent i = new Intent(this, ModuloConfigFormatoImpresion.class);
//                i.putExtras(bundlei);
//                startActivityForResult(i, CONFIGFORMATIMP_REQUEST_CODE);
//                break;
//
//            case R.id.estadistica_proceso:
//                try {
//                    Bundle bundle2 = new Bundle();
//                    bundle2.putString("aforador", lector);
//                    bundle2.putString("directorioActual", VariablesGlobales.directorioactual);
//                    bundle2.putString("nombrePredio", variables.getNombrepredio());
//                    bundle2.putString("terminal", terminal);
//                    Intent intent = new Intent(this, ResumenEstadistico.class);
//                    intent.putExtras(bundle2);
//                    startActivity(intent);
//
//                } catch (Exception e) {
//                    mensajeT("Error al iniciar actividad de Resumen Estadistico" + e.getMessage(), msgLargo);
//                }
//                break;
//
//            //case R.id.ayuda_ubicacion_GPS: //[enerca] No mandan coordenadas se deshabilita
//            //Bundle bundleg = new Bundle();
//            // bundleg.putString("lactitud", infoClienteEntrada.gettablaEntradaClientes_CORDENADAX().trim());
//            // bundleg.putString("longitud", infoClienteEntrada.gettablaEntradaClientes_CORDENADAY().trim());
//            // Intent inten = new Intent(this, ActGPS.class);
//            // inten.putExtras(bundleg);
//            //startActivityForResult(inten, ACTGPS_REQUEST_CODE);
//            //break;
//
//            case R.id.utilidades:
//                try {
//                    Intent myIntent = new Intent(this, FechayHora.class);
//                    startActivity(myIntent);
//                } catch (Exception ex) {
//                    mensajeT("Error al iniciar actividad de Utilidades" + ex.getMessage(), msgLargo);
//                }
//                break;
//            case R.id.enviar_facturacion_gprs:
//                //validar conexion y mandar registros en recamara
//                // Log.e("error", "enviogprs11 ");
//                if (procesandoenvioenHilos == 0) {
//
//                    Bundle bundlex = new Bundle();
//                    bundlex.putString("directorioactual", VariablesGlobales.directorioactual);
//                    Intent it = new Intent(this, ModuloConsultaNoEnviados.class);
//                    it.putExtras(bundlex);
//                    // Log.e("error", "enviogprs10 " + VariablesGlobales.directorioactual);
//                    startActivityForResult(it, CONSULTNONVIADS_REQUEST_CODE);
//                } else {
//                    mensajeT("El sistema esta en proceso de envio de datos po HILOS al servidor,\n No se permite realizar este proceso", msgMedio);
//                }
//                break;
//
////            case R.id.mensajes:   //Ax: se une con la dfe abajo //ENERCA NO
////                //mensajeT("FUNCION INHABILITADA");
////                Bundle bundlex = new Bundle();
////                bundlex.putString("operario", lector);
////                bundlex.putString("url", URL);
////                bundlex.putString("pagws", paginaWs);
////                Intent it = new Intent(this, Insertar.class);
////                it.putExtras(bundlex);
////                startActivityForResult(it, CHATINSETAR_REQUEST_CODE);
////                break;
//
//            case R.id.procesar_reenvio_gprs: //Ax: copia backup y lo convierte en enviogprs //788
//                // Log.e("error", "enviogprs1 ");
//                File Archivo1 = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/ENVIOSGPRS.SDA");
//                File Archivo2 = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/ENVIOGPRS" + serialPDA + ".SDA");
//
//                if (Archivo1.exists() || Archivo2.exists()) {
////ojo con esto esta funcionando mal
//                    if (Archivo1.length() == 0) Archivo1.delete();
//                    if (Archivo2.length() == 0) Archivo2.delete();
//
//                    mensajeT("Aun hay archivos por enviar de EnviosGPRS, \n intente enviarlos primero", msgLargo);
//                } else {
//                    File ArchivoBK = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/BKENVIOSGPRS.SDA");
//
//                    if (ArchivoBK.exists()) {
//                        try {
//                            if (utils.CrearCopia(ArchivoBK.getAbsolutePath(), Archivo1.getAbsolutePath())) {
//
//                                if (ArchivoBK.delete()) {
//
//                                    envioFacturacionHilos(); //Ax: nuevo, se envia de una para evitar posibles errores
//                                    mensajeT("Copia terminada, puede reintentar envio ", msgLargo);
//                                } else {
//                                    if (ArchivoBK.exists()) Archivo1.delete();
//                                    mensajeT("Error en la copia, intente de nuevo", msgLargo);
//                                }  //utils.ModificarArchivo(ArchivoBK.getAbsolutePath(), Archivo1.getAbsolutePath(), "M", 293, 294);//Ax: ya no se usa, no se deben enviar M's en esta reconstruccion, el ws verifica si ya existe
//                            } else {
//                                mensajeT("Error al crear Copia", msgLargo);
//                            }
//                        } catch (Exception ex) {
//                            mensajeT("Error no se pudo hacer copia de Backup... " + ex, msgLargo);
//                        }
//                    } else
//                        mensajeT("No existe BKEnviosGPRS para realizar este proceso", msgLargo);
//                }
//                break;
//
//            case R.id.evaluar_noEnviadas_gprs: //EVALUAR NO ENVIADAS GRPS //7811
//
//                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
//                builder2.setTitle("ALERTA EVALUAR ENVIOS");
//                builder2.setMessage("DESEA EVALUAR SI TODAS SUS LECTURAS FUERON ENVIADAS? ");
//
//                builder2.setIcon(R.drawable.ic_launcher1);
//
//                builder2.setPositiveButton("Si", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        ValidarNoEnviados();
//                    }
//                });
//                builder2.show();                // fin del proceso de reimpresion
//                limpiarMedidores();
//                break;
//
////            case R.id.censo_Por_Supervision: //CENSO POR SUPERVISION
////                if (nivelOperador.equals("S") && nombreRuta().contains("99999"))
////                    CensoSupervisor();
////                break;
//
////            case R.id.documento_Certificado: //DOCUMENTO CERTIFICADO   ENERCA NO
////
////                if (infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("E") || (infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("P"))) {
////                    ejecutarCertificacion(0);
////                } else {
////                    mensajeT("Este proceso es solo para Entregas Certificadas", msgMedio);
////                }
////                break;
//
//            case R.id.liquidacion_automatica:
//
//                MostrarAlertDialog("ALERTA!", "DESEA INICIAR PROCESO \n[SI]AUTOMATICO \n[NO] MANUAL", "liquidacionautomatica");
//                break;
//
////            case R.id.certificar_Postal: //CERTIFICAR POSTAL
////
////                if (VariablesGlobales.tipoDeRuta.trim().equals("E") || VariablesGlobales.tipoDeRuta.trim().equals("F")) {
////
////                    if (infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("P")) {
////                        ejecutarCertificacion(0);
////                    } else {
////                        mensajeT("Este proceso es solo para Entregas Certificadas", msgMedio);
////                    }
////                } else {
////                    mensajeT("La cuenta no es Postal..", msgMedio);
////                }
////                break;
//
//            //nuevo para enviar las fotos
//            case R.id.enviar_fotos:
//
//                if (procesandoenvioenHilos == 0) {
//                    EnviarFotos();
//                    //aqui enviar fotos
//                } else {
//                    mensajeT("El sistema esta en proceso de envio de datos por HILOS al servidor,\n No se permite realizar este proceso", msgMedio);
//                }
//                break;
//
//            case R.id.Activar_WIFI:
//                try {
//                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//
//                    if (wifiManager.isWifiEnabled()) {
//                        wifiManager.setWifiEnabled(false);
//                        Toast.makeText(getApplicationContext(), "Se ha Apagado el WIFI (X)", Toast.LENGTH_SHORT).show();
//                    } else {
//                        wifiManager.setWifiEnabled(true);
//                        Toast.makeText(getApplicationContext(), "Se ha ENCENDIDO el WIFI (✓)", Toast.LENGTH_SHORT).show();
//                    }
//
//
//                } catch (Exception ex) {
//                    mensajeT("Error al Activar WIFI \n" + ex.getMessage(), msgLargo);
//                }
//                break;
//
//            //nuevo para mostrar el mapa
//            case R.id.mapa:
//                if (!infoClienteEntrada.gettablaEntradaClientes_INFORMACIONADICIONAL().substring(213, 233).trim().equals("")) {
//                    Bundle bundleM = new Bundle();
//                    bundleM.putString("lon", infoClienteEntrada.gettablaEntradaClientes_INFORMACIONADICIONAL().substring(213, 233).trim());
//                    bundleM.putString("lat", infoClienteEntrada.gettablaEntradaClientes_INFORMACIONADICIONAL().substring(234, 254).trim());
//                    Intent itM = new Intent(this, MapsActivity.class);
//                    itM.putExtras(bundleM);
//                    startActivityForResult(itM, MAPA);
//                } else {
//                    Toast.makeText(getApplicationContext(), "Esta cuenta no tiene coordenadas registradas", Toast.LENGTH_SHORT).show();
//                }
//                //  aqui
//                break;
//            case R.id.volver:
//                try {
//                    if (myThread.isAlive()) {
//                        myThread.interrupt();
//                    }
//                } catch (Exception ex) {
//                    salirse = true;
//                }
//                // Log.e("error", "hace el back 2");
//                finish();
//                break;
//
//            case R.id.tomar_foto:
//                capturaImagenFotografica();
//                break;
//        }
//        return super.onOptionsItemSelected(item); //tools
//    }

    private void GuardarBackup() {

        try {

            File fileName;
            // Log.e("error3", "ruta file1 " + variables.removableStoragePath);
            if (!variables.removableStoragePath.equals("") && new File(variables.removableStoragePath).isDirectory()) {
                fileName = new File(variables.removableStoragePath + "/BACKUPLECTURAS" + nombreBackup + ".SDA");
            } else {
                fileName = new File(VariablesGlobales.directorioBackUp + "/BACKUPLECTURAS" + nombreBackup + ".SDA");
            }


            if (!fileName.exists()) {
                fileName.createNewFile();
            }
            // Log.e("error3", "ruta file " + fileName.getAbsolutePath());
            infoRegistroSalida.Cerrar_TablaRegistroSalida();
            infoRegistroSalida.setArchivo_TablaRegistroSalida(fileName.getAbsolutePath());
            infoRegistroSalida.abrir_TablaRegistroSalida(infoRegistroSalida.getArchivo_TablaRegistroSalida());
            int totalRegistros = infoRegistroSalida.getTotal_TablaRegistroSalida() + 1;
            variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";GuardarBackup;" + getPhoneDate() + "-" + getPhoneHour();
            escribeResumenTiempo(variableResumenLiquidacion + " " + "GuardarBackup: Posicion Actual|" + VariablesGlobales.registroactual + " | totalregistros: " + totalRegistros);
            infoRegistroSalida.escribir_TablaRegistroSalida(totalRegistros);
            infoRegistroSalida.Cerrar_TablaRegistroSalida();

            infoRegistroSalida.setArchivo_TablaRegistroSalida(VariablesGlobales.directorioactual + "/DATOSDESALIDA/REGISTRO.SDA");
            infoRegistroSalida.abrir_TablaRegistroSalida(infoRegistroSalida.getArchivo_TablaRegistroSalida());
        } catch (Exception ex) {
            // Log.e("error3", "error file " + ex.getMessage());
            utils.Log(logfile, "[MenuDeLiquidacion]GuardarBackup: " + ex.getMessage());
        }
    }

    /**
     * Ax. depende de los parametros inicia el envio de Aforos,Certificado,etc.
     *
     * @param aforos:      true indica si se debe hacer el proceso de creacion de archivos y envio de aforos
     * @param novedades:   true indica si se debe hacer el proceso de creacion de archivos y envio
     * @param certificado: true indica si se debe hacer el proceso de creacion de archivos y envio
     * @param censo:       true indica si se debe hacer el proceso de creacion de archivos y envio
     * @param cuentas:     true indica si se debe hacer el proceso de creacion de archivos y envio
     */
    public void envioHilos_CertPostal_Censo_Aforos_Novedades(boolean aforos, boolean novedades, boolean certificado, boolean censo, boolean cuentas) {

        Archivos_CPCAN = new String[]{"", "", "", "", ""};
        int cont = 0;

        if (aforos) {
            String in = VariablesGlobales.directorioactual + "/DATOSDESALIDA/AFOROS.SDA";
            String out = VariablesGlobales.directorioactual + "/DATOSDESALIDA/AFOROS" + serialPDA + ".SDA";

            if (Enviar_CertPostal_Censo_Aforos_Novedades_1(in, out, 98, 99)) {
                Archivos_CPCAN[0] = out;
                cont++;
            }
        }

        if (novedades) {

            String in = VariablesGlobales.directorioactual + "/DATOSDESALIDA/NOVEDADES.SDA";
            String out = VariablesGlobales.directorioactual + "/DATOSDESALIDA/NOVEDADES" + serialPDA + ".SDA";
            if (Enviar_CertPostal_Censo_Aforos_Novedades_1(in, out, 180, 181)) {
                Archivos_CPCAN[1] = out;
                cont++;
            }
        }

        if (certificado) {
            String in = VariablesGlobales.directorioactual + "/DATOSDESALIDA/CERTIFICADOS.SDA";
            String out = VariablesGlobales.directorioactual + "/DATOSDESALIDA/CERTIFICADOS" + serialPDA + ".SDA";
            if (Enviar_CertPostal_Censo_Aforos_Novedades_1(in, out, 234, 236)) {
                Archivos_CPCAN[2] = out;
                cont++;
            }
        }

        if (censo) {
            String in = VariablesGlobales.directorioactual + "/DATOSDESALIDA/AUDITORIASUPERVISOR.SDA";
            String out = VariablesGlobales.directorioactual + "/DATOSDESALIDA/AUDITORIASUPERVISOR" + serialPDA + ".SDA";
            if (Enviar_CertPostal_Censo_Aforos_Novedades_1(in, out, 274, 276)) {
                Archivos_CPCAN[3] = out;
                cont++;
            }
        }

        if (cuentas) {
            String in = VariablesGlobales.directorioactual + "/DATOSDESALIDA/CUENTASGPS.SDA";
            String out = VariablesGlobales.directorioactual + "/DATOSDESALIDA/CUENTASGPS" + serialPDA + ".SDA";
            if (Enviar_CertPostal_Censo_Aforos_Novedades_1(in, out, 350, 351)) {
                Archivos_CPCAN[4] = out;
                cont++;
            }
        }

        if (cont < 1) {

            if (procesandoenvioenHilos_CPCAN == 1) {
                //se comenta por peticion del cliente  mensajeT("No hay archivos por enviar, envio ocupado", msgCorto);
            } else {
                //se comenta por peticion del cliente   mensajeT("No hay archivos por enviar", msgCorto);
            }
            Archivos_CPCAN = null;
            return;
        }

        if (comprobarconexiones(true)) {
            if (procesandoenvioenHilos_chat) {
                contadorChat = 0;
                taskChat.cancel(true);
            }
            procesandoenvioenHilos_CPCAN = 1;
            metodo = "ENVIOCERTPOSTLCENSOAFORONOVEDADS"; //Ax: esta parte de la facturacion en hilos es manejeda en una tarea paralela
//            AsyncCallWS task = new AsyncCallWS();
//            task.execute("");//Ax: continua aqui, en AsyncCallWS.doinback...
            TaskHelper.execute(new AsyncCallWS(), metodo);
        }

    }

    public boolean comprobarconexiones(boolean mostrarmsg) {
        // Log.e("error", conexionGPRSActiva + " variables " + procesandoenvioenHilos_CPCAN + " " + procesandoenvioenHilos);
        if (conexionGPRSActiva == 0 || procesandoenvioenHilos_CPCAN == 1 || procesandoenvioenHilos == 1 || banderaWsOcupado) {

            if (!mostrarmsg) return false;

//            if (conexionGPRSActiva == 0) {
//                //se comenta por peticion     mensajeT("conexion GPRS Inactiva", msgCorto);
//            } else {
//                //se comenta por peticion      mensajeT("Otros envios en proceso", msgCorto);
//            }
            return false;
        }
        return true;
    }

    /**
     * //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
     * Ax: Se procede a leer el archivo (uno a la vez) solicitado Censo supervisor o Aforos o Certificacion Postal o Novedades...luego se verifican cuales lineas no se han enviado y se crea una copia que sera comprimida y enviada al server
     *
     * @param ArchivoEntrada :archivo original del cual se creará una copia para enviar al server
     * @param ArchivoEnviado :archivo copiado con serial el cual se enviará al server
     * @param desde          : posicion inicial del campo que in-dica si el registro fue enviado o no.
     * @param hasta          : posicion final del campo que indica si el registro fue enviado o no.
     */
    private boolean Enviar_CertPostal_Censo_Aforos_Novedades_1(String ArchivoEntrada, String ArchivoEnviado, int desde, int hasta) {//Ax: este metodo se cambio por la clase 'generica' lee-> crea-> sobreescribe-> envia-> sobreescribe-> borra

        try {

            Generica gen = new Generica();
            gen.setOriginalFile(ArchivoEntrada);
            gen.setNuevoFile(ArchivoEnviado);
            gen.desde = desde; //Posicion inicial del campo "enviado" para buscar caractter que define envio
            gen.hasta = hasta; //Posicion fin del campo "enviado"...
            String msg;

            if (ArchivoEntrada.toLowerCase().contains("aforos") || ArchivoEntrada.toLowerCase().contains("novedades") || ArchivoEntrada.toLowerCase().contains("cuentas")) {//Ax: los archivos son novedades o aforos deben tener en ese campo '_' lo que indica que no han sido enviados, lo otros tienen 'NO' o 'EE'
                msg = gen.WriteFileByPos("X", "_", true); //Busca 'X' y sobreescribe '_' y crea archivo por enviar. '_' es estado temporal enviando...
            } else {
                msg = gen.WriteFileByPos("NO", "EE", true); //Busca 'No' y sobreescribe 'EE' y crea archivo por enviar. EE es estado temporal enviando...
            }
            // Log.e("error", "novedades " + msg);
            if (msg == null) return false;

            if (msg.equals("1")) {
                return true; //Se creo y puede enviar
            } else {

                if (gen.nuevoFile.length() < 4) {
                    gen.nuevoFile.delete();//Ax: revisar, a veces no se borra
                }

                if (!msg.equals("")) {
                    utils.Log(logfile, "Falla en la clase generica " + msg); //Escribir algun error en log
                }
                procesandoenvioenHilos_CPCAN = 0;
            }

        } catch (Exception ex) {
            // Log.e("error", "error envio nov " + ex.getMessage());
            mensajeT("Problemas al crear archivo" + ArchivoEnviado, msgLargo);
            utils.Log(logfile, "Enviar_CertPostal_Censo_Aforos_Novedades_1; " + ex.getMessage());
            procesandoenvioenHilos_CPCAN = 0;
        }
        return false;
    }

    //Parte 2 llamada desde la tarea asincrona
    private String Enviar_CertPostal_Censo_Aforos_Novedades_2() {//Ax: Envio al Ws

        try {
            String ciclo0 = String.format("%1$4s", Ciclo).replace(" ", "0");
            ArrayList filestoZip = new ArrayList();

            for (String doc : Archivos_CPCAN) {

                if (!doc.equals("")) {
                    File fdoc = new File(doc);
                    filestoZip.add(fdoc);
                }
            }

            File nombreZipPorCrear;
            nombreZipPorCrear = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/" + "Tpl" + (Municipio + Seccion + Division) + ".zip");

            String resp = EnviarArchivo((rutaAdministrador + "CIC" + ciclo0), nombreZipPorCrear, filestoZip, trama, false); //Aqui se va a otro metodo que llama varios metodos del WS

            nombreZipPorCrear.delete();

            for (String doc : Archivos_CPCAN) {
                if (!doc.equals("")) {
                    File fdoc = new File(doc);
                    fdoc.delete();
                }
            }

            if (!resp.equals("ok")) { //Si hubo fallo...
                utils.Log(logfile, "[Enviar_CertPostal_Censo_Aforos_Novedades_2].:" + resp);
            }

            metodo = "ENVIOCERTPOSTLCENSOAFORONOVEDADS"; //esto se hace para que en el onpostejecute valla a esta que fue la inicial

            return resp;

        } catch (Exception ex) {
            utils.Log(logfile, "Enviar_CertPostal_Censo_Aforos_Novedades_2; " + ex.getMessage());
            procesandoenvioenHilos_CPCAN = 0;
            Archivos_CPCAN = null;
        }
        return "";
    }

    private void Enviar_CertPostal_Censo_Aforos_Novedades_3() {//Ax: Solo si envio exitoso, se procede a sobreescribir el original y borrar envio

        Generica gen = new Generica();
        String temp = "";

        try {
            for (String doc : Archivos_CPCAN) {

                if (doc.equals("")) continue;

                int desde = 0;
                int hasta = 0;

                if (doc.toLowerCase().contains("aforos")) {
                    desde = 98;
                    hasta = 99;
                    temp += " Aforos.\n ";
                } else if (doc.toLowerCase().contains("novedades")) {
                    desde = 180;
                    hasta = 181;
                    temp += " Novedades.\n ";

                } else if (doc.toLowerCase().contains("certificado")) {
                    desde = 234;
                    hasta = 236;
                    temp += " Certificados.\n ";
                } else if (doc.toLowerCase().contains("auditoriasupervisor")) {
                    desde = 274;
                    hasta = 276;
                    temp += " Auditoria Supervisor.\n ";
                } else if (doc.toLowerCase().contains("cuentas")) {
                    desde = 350;
                    hasta = 351;
                    temp += " cuentasGPS.\n ";
                } else {
                    continue;
                }
                //se comenta por peticion del cliente    mensajeT("Se realizo Envio:\n" + temp, msgCorto);

                gen.setOriginalFile(doc.replace(serialPDA, ""));
                gen.setNuevoFile(doc);
                gen.desde = desde;
                gen.hasta = hasta;
                String msg;

                if (doc.toLowerCase().contains("aforos") || doc.toLowerCase().contains("novedades") || doc.toLowerCase().contains("cuentas")) {//Ax: los archivos son novedades o aforos deben ntener en ese campo '_' lo que indica que no han sido enviados, lo otros tienen NO o EE
                    msg = gen.WriteFileByPos("_", "Y", false); //Busca '_' y sobreescribe 'Y' y No crea archivo por enviar
                } else {
                    msg = gen.WriteFileByPos("EE", "SI", false); //Busca 'EE' y sobreescribe 'SI' y No crea archivo por enviar
                }

                if (gen.nuevoFile.exists()) {//Si se creo copia o existe archivo, se procede a borrar
                    gen.nuevoFile.delete();
                }

                if (msg != null && !msg.equals("1")) {
                    utils.Log(logfile, "Enviar_CertPostal_Censo_Aforos_Novedades_3:. " + msg); //Escribir algun error en log
                }
            }
        } catch (Exception ex) {
            //se comenta por peticion del cliente     mensajeT("Problemas al sobreescribir" + gen.getOriginalFile(), msgLargo);
            utils.Log(logfile, "Enviar_CertPostal_Censo_Aforos_Novedades_3; " + gen.getOriginalFile() + " ; " + ex.getMessage());
        }
        procesandoenvioenHilos_CPCAN = 0;
        Archivos_CPCAN = null;
    }
    /*private String AvanzarLecturas(){
        String resp = "";
        try{
            Log.e("INFO","llamoatomarfoto: " + llamoatomarfoto + "|exitoImagen: " + exitoImagen + "|finalizolectura: " + finalizolectura);
            if((llamoatomarfoto == 1 && exitoImagen == 1 && finalizolectura == 1) || (llamoatomarfoto == 0 && exitoImagen == 0 && finalizolectura == 1)){
                if ((variables.direcciondelectura == variables.haciaadelante)) {
                    if (avanzarRegistro() == 0) {
                        retrocedeRegistro();
                    }
                } else {
                    if (retrocedeRegistro() == 0) {
                        avanzarRegistro();
                    }
                }

                variables.numdigitos = 0;

                if (indicadorManual == 0) {
                    if (VariablesGlobales.totalprediosleidos == infoClienteEntrada.getTotal_TablaEntradaClientes()) {
                        mensajeT("AAA. Proceso de Lecturas concluido ", msgLargo);
                        visualizarInformacionCliente(3);
                    } else
                        visualizarInformacionCliente(0);
                }
                finalizolectura = 0;
                llamoatomarfoto = 0;
                exitoImagen = 0;

                fotoynovedad = false;
                ventanaConsumo = false;

                limpiarMedidores();
                if (indicadorManual == 0) {
                    txtElectura.setText("");
                }
            }
        }catch (Exception ex){
            Log.e("INFO","AvanzarLecturas|Error -> " + ex.getMessage());
            resp = "AvanzarLecturas|Error|" + ex.getMessage();
        }
        return resp;
    }*/

    //validar lo leido contra el backup de enviados y si no esta ahi reproducir dicho registro
    private String ValidarNoEnviados() {
        try {
            Log.e("INFO", "entra a ValidarNoEnviados1");
            String msg = "";
            File Archivo1 = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/ENVIOSGPRS.SDA");

            if (Archivo1.exists()) {
                Log.e("INFO", "length: " + Archivo1.length());
                if (Archivo1.length() == 0) {
                    Archivo1.delete();
                } else {
                    dialogloading.dismiss();
                    /*mensajeT("Aun Existen Registros sin Enviar\nPor favor enviarlos antes de esta verificacion\n"
                            + VariablesGlobales.directorioactual + "/DATOSDESALIDA/ENVIOSGPRS.SDA", msgLargo);*/
                    msg = "Aun Existen Registros sin Enviar\nPor favor enviarlos antes de esta verificacion\n"
                            + VariablesGlobales.directorioactual + "/DATOSDESALIDA/ENVIOSGPRS.SDA";
                    return msg;
                }
            }

            //File Archivo2 = new File(VariablesGlobales.directorioactual + "/DatosDeSalida/EnviosGPRSHilos.SDA");
            File Archivo2 = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/ENVIOGPRS" + serialPDA + ".SDA");

            if (Archivo2.exists()) {
                dialogloading.dismiss();
                /*mensajeT("Aun Existen Registros sin Enviar\nPor favor enviarlos antes de esta verificacion\n"
                        + VariablesGlobales.directorioactual + "/DATOSDESALIDA/ENVIOGPRS" + serialPDA + ".SDA", msgLargo);*/
                msg = "Aun Existen Registros sin Enviar\nPor favor enviarlos antes de esta verificacion\n"
                        + VariablesGlobales.directorioactual + "/DATOSDESALIDA/ENVIOGPRS" + serialPDA + ".SDA";
                return msg;
            }

            File ArchivoNoEnviadosF = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/BKENVIOSGPRS.SDA");

            if (!ArchivoNoEnviadosF.exists()) {
                try {
                    /*mensajeT("Aun No Existe Backup de Envios...Se buscara si hay cuentas leidas sin enviar", msgMedio);*/

                    ArchivoNoEnviadosF.createNewFile();
                } catch (Exception e) {
                    dialogloading.dismiss();
                    utils.Log(logfile, "[MenuDeLiquidacion]ValidarNoEnviados(), error Creando " + ArchivoNoEnviadosF.getAbsolutePath());
                    //mensajeT("Error al crear BkEnviosGPRS.", msgCorto);
                    msg = "Error al crear BkEnviosGPRS.";
                    return msg;
                }
            }
            Noleidas = 0;
            LeidasNoenviadas = 0;
            ValidandoNoEnv = 1;

            misenvios.archivo_EnvioGPS = ArchivoNoEnviadosF.getAbsolutePath();
            limpiarMedidores();

            if (misenvios.abrir_EnvioGPS(misenvios.archivo_EnvioGPS)) {
                abrirArchivosDeFacturacion();
                int NumRegistro = VariablesGlobales.registroactual;
                CuentasProblema = "";
                VariablesGlobales.registroactual = 1;
                while (VariablesGlobales.registroactual <= infoRegistroSalida.getTotal_TablaRegistroSalida()) {

                    int leerinfo = leerInformacionUsuario(1);
                    if (leerinfo == 0) {
                        msg = "Alerta archivos corruptos\n" + "El Codigo de Cuenta son Diferentes-\n" + "Verificar Archivos de Entrada.\n" + "infoClienteEntrada.cuenta: " +
                                infoClienteEntrada.gettablaEntradaClientes_Cuenta() + "\n " + "infoClienteSalida.cuente: " + infoClienteSalida.gettablaClienteSalida_CUENTA() +
                                "\n " + "medidorSalida.cuenta: " + medidorSalida.gettablaContadorSalida_CUENTA() + "\n " + "infoRegistroSalida.cuenta: "
                                + infoRegistroSalida.gettablaRegistroSalida_CUENTA() + "\n " + "infoRegistroEntrada.cuenta: " + infoRegistroEntrada.gettablaRegistroDeEntrada_CUENTA();
                        return msg;
                    }
                    if (!infoRegistroSalida.gettablaRegistroSalida_FECHALECTURA().trim().equals("")) {
                        //posiblemente esta lo compararemos tambien en los registros de del cliente salida que este lleno toodo sino lo reportamos que esta a medio liquidar
                        if (!infoClienteSalida.gettablaClienteSalida_INDFACTURACION().trim().equals("")) {
                            //buscar en backup y procesar de nuevo
                            if (!infoClienteSalida.gettablaClienteSalida_INDFACTURACION().trim().equals("F")) {
                                //esta cuenta esta diferente y se requiere imprimir
                                if (!infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("L") && !infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("N") && !infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("E")) {
                                    //mensajeT("Esta cuenta: " + infoClienteSalida.gettablaClienteSalida_CUENTA() + " no se ha Liquidado  ni Facturado Se procesara lo que falta...\n VERIFIQUE QUE LA IMPRESORA ESTA ENCENDIDA", msgLargo);
                                    //no hacerlo ya que si no liquido fue porque tenia razon de critica o buscar manera de indicar proceso finalizado victor LiquidarEImprimir();
                                }
                            }
                            Log.e("INFO", "ValidarNoEnviados | Cuenta: " + infoRegistroSalida.gettablaRegistroSalida_CUENTA() + " | Consecutivo: " + infoRegistroSalida.gettablaRegistroSalida_CONSECUTIVO());
                            // Log.e("Buscar error", "Buscar la cuenta " + infoRegistroSalida.gettablaRegistroSalida_CUENTA());
                            misenvios.BuscarSecuencial_EnvioGPS(infoRegistroSalida.gettablaRegistroSalida_CUENTA(), infoRegistroSalida.gettablaRegistroSalida_NROCONTADOR(), infoRegistroSalida.gettablaRegistroSalida_CONSECUTIVO());
                            buscarcuentasgps(infoRegistroSalida.gettablaRegistroSalida_CUENTA(), infoRegistroSalida.gettablaRegistroSalida_CONSECUTIVO());
                            //.substring(0, 6
                            Log.e("INFO", "ValidarNoEnviados | encontro_EnvioGPS: " + misenvios.encontro_EnvioGPS);

                            // Log.e("Buscar error", String.format("Buscar la cuenta " + infoRegistroSalida.gettablaRegistroSalida_CUENTA() + "La encontro en: " + misenvios.encontro_EnvioGPS));//String.format("",
                            if (misenvios.encontro_EnvioGPS == 0) {

                                LeidasNoenviadas++;
                                misenvios.Cerrar_EnvioGPS();
                                //este es el proceso de entrar a grabr la informacion en el archivo de envios ya que no se han enviado
                                nroContador1 = infoRegistroEntrada.gettablaRegistroDeEntrada_nrocontador();
                                idContador1 = infoRegistroSalida.gettablaRegistroSalida_CONSECUTIVO();
                                lecturaTomada1 = infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA();

                                String x = String.format("%1$2s", infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim()).replace(" ", "0");
                                causadenolectura1 = x;

                                x = String.format("%1$1s", infoRegistroSalida.gettablaRegistroSalida_INTENTOS().trim()).replace(" ", "0");
                                intentos1 = x;

                                x = String.format("%1$10s", parseStringToInteger(infoRegistroSalida.gettablaRegistroSalida_LECTURAMODIFICADA1().trim())).replace(" ", "0");
                                lecturaModificada11 = x;

                                x = String.format("%1$10s", parseStringToInteger(infoRegistroSalida.gettablaRegistroSalida_LECTURAMODIFICADA2().trim())).replace(" ", "0");
                                lecturaModificada21 = x;

                                x = String.format("%1$1s", infoRegistroEntrada.gettablaRegistroDeEntrada_Digitos().trim()).replace(" ", "0");
                                digitos1 = x;

                                criticaPDA1 = infoRegistroSalida.gettablaRegistroSalida_LEIDO();
                                lecturaAnterior1 = infoRegistroEntrada.gettablaRegistroDeEntrada_Lecturaanterior();
                                nrocontadordb = infoRegistroEntrada.gettablaRegistroDeEntrada_Digitos().trim();
                                TipoMedida1 = infoRegistroEntrada.gettablaRegistroDeEntrada_Tipomedida();
                                Log.e("INFO", "ValidarNoEnviados | va a entrar a guardarDatosAEnviarNuevo ");
                                guardarDatosAEnviarNuevo(nroContador1, idContador1, lecturaTomada1, causadenolectura1, nrocontadordb, lecturaModificada11, lecturaModificada21, digitos1, criticaPDA1, lecturaAnterior1, 0);//,infoClienteEntrada.gettablaEntradaClientes_Cuenta(),TipoMedida1);

                                //abrir de nuevo el archivo enviados backup
                                misenvios.archivo_EnvioGPS = ArchivoNoEnviadosF.getAbsolutePath();
                                misenvios.abrir_EnvioGPS(misenvios.archivo_EnvioGPS);

                            }
                        } else {
                            //almacenar cuentas problema
                            CuentasProblema += infoRegistroSalida.gettablaRegistroSalida_CUENTA() + "/";
                        }
                    } else
                        Noleidas++;
                    VariablesGlobales.registroactual++;
                }
                misenvios.Cerrar_EnvioGPS();
                VariablesGlobales.registroactual = NumRegistro;

            }
            limpiarMedidores();
            msg = "VALIDACION REALIZADA";
            return msg;
        } catch (Exception e) {
            utils.Log(logfile, "[MenuDeLiquidacion]ValidaNoEnviados|ERROR|" + e.getMessage());
            return "ERROR: " + e.getMessage();
        }

    }

    public void mensajeAdm(Activity activity) {

        dialogloading = new Dialog(activity);
        dialogloading.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogloading.setCancelable(false);
        dialogloading.setContentView(R.layout.loading_activity);
        TextView textView = dialogloading.findViewById(R.id.txt_loadAct);
        textView.setText(msgAdmon);//"EVALUANDO ESTADISTICAS
        dialogloading.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialogloading.show();
    }

    public void buscarcuentasgps(String Cuenta, String indregis) {
        try {
            File fileGps = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/CUENTASGPS.SDA");
            FileReader stream3 = new FileReader(fileGps);
            BufferedReader reader = new BufferedReader(stream3);
            String linea;

            while ((linea = reader.readLine()) != null) {
                linea = linea.replace(";", "|");
                String[] arr = linea.split("\\|");

                IndRegis = arr[11].trim();
                CodCuenta = arr[0].trim();
                if ((IndRegis.equals(indregis.trim())) || (CodCuenta.equals(Cuenta.trim()))) {
                    coordenadaLatitud = arr[1];
                    coordenadaLongitud = arr[2];
                    FechayHoraLectura = arr[6].trim();
                    FechaGps = arr[4].trim();
                    HoraGps = arr[5].trim();
                    break;
                }
            }

        } catch (Exception e) {
            Log.e("ERROR", "[BuscarCuentasGps()] Error: " + e.getMessage());
            utils.Log(logfile, "[BuscarCuentasGps()] Error: " + e.getMessage());
        }
    }

    //nuevo proceso de reimprimir en la recuperacion
    private void LiquidarEImprimir() {
        int alterno;

        for (; ; ) {
            alterno = VariablesGlobales.registroactual;
            VariablesGlobales.registroactual = alterno;

            leerInformacionUsuario(1);
            visualizarInformacionCliente(0);

            procesoDeLiquidacionEImpresion(1);
            break;
        }
        variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";Recupero cuenta a Imprimio;" + getPhoneDate() + " " + getPhoneHour();
        escribeResumenTiempo(variableResumenLiquidacion);

        VariablesGlobales.registroactual = alterno;
        leerInformacionUsuario(1);
        visualizarInformacionCliente(0);
    }

    public void capturaImagenFotografica() {
        //abrirArchivosDeFacturacion();
        // esto si vamos a registrar si la foto se tomo o no se tomo
        Log.e("INFO","[MenuDeLiquidacion](capturaImagenFotografica)cod_cuentaUlt_foto: " + infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim());
        // Desde el menú siempre se permite nueva foto, incluso si ya tomó OCR
        // Desde flujo interno (fotoOcrObligatoria) NO se resetea para no crear bucle
        if (!fotoOcrObligatoria) {
            yaTomoFotoOcr = false;
            Log.e("INFO","1 - SETEA EL VALOR DE yaTomoFotoOcr: " + yaTomoFotoOcr);
        }
        ejecutarProcesoDeFoto(cod_cuentaUlt_foto.trim() + "_" + mesUlt_foto.trim(), 1, tipoFotoDigital, 0, "", 1);
        // Log.e("error", "entra en este1");
        //cerrarArchivosFacturacion();
    }

    public void capturaImagenFotograficaII() {
        ejecutarProcesoDeFoto(cod_cuentaUlt_foto.trim() + "_" + mesUlt_foto.trim(), 1, tipoFotoDigital, 0, "", 1);
    }

    private void visualizarPredioLeido() {

        if (variables.ultimoregistro < 1) {
            mensajeT("No existe un ultimo predio leido", msgMedio);
            return;
        }

        abrirArchivosDeFacturacion();

        alterno = VariablesGlobales.registroactual;
        VariablesGlobales.registroactual = variables.ultimoregistro;
        leerInformacionUsuario(1);
        visualizarInformacionCliente(0);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alerta de Liquidacion Carga Instalada");
        builder.setMessage("Desea Reprocesar el Cliente?");

        builder.setIcon(R.drawable.ic_launcher1);

        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                cerrarArchivosFacturacion();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                VariablesGlobales.registroactual = alterno;
                leerInformacionUsuario(1);
                visualizarInformacionCliente(0);
                cerrarArchivosFacturacion();
            }
        });
        builder.show();
    }

    private void abrirArchivosDeFacturacion() {

        infoClienteEntrada.abrir_TablaEntradaClientes(infoClienteEntrada.getArchivo_TablaEntradaClientes());
        infoRegistroEntrada.abrir_TablaRegistroDeEntrada(infoRegistroEntrada.getArchivo_TablaRegistroDeEntrada());
        infoMedidorEntrada.abrir_TablaMedidorEntrada(infoMedidorEntrada.getArchivo_TablaMedidorEntrada());
        infoRegistroSalida.abrir_TablaRegistroSalida(infoRegistroSalida.getArchivo_TablaRegistroSalida());
        medidorSalida.abrir_TablaContadorSalida(medidorSalida.getArchivo_TablaContadorSalida());
        infoClienteSalida.abrir_TablaClienteSalida(infoClienteSalida.getArchivo_TablaClienteSalida());
    }

    private void cerrarArchivosFacturacion() {

        //infoCobrosLiquidados.Cerrar_TablaCobrosRealizados();
        infoClienteEntrada.Cerrar_TablaEntradaClientes();
        infoRegistroEntrada.Cerrar_TablaRegistroDeEntrada();
        infoMedidorEntrada.Cerrar_TablaMedidorEntrada();

        infoRegistroSalida.Cerrar_TablaRegistroSalida();
        medidorSalida.Cerrar_TablaContadorSalida();
        infoClienteSalida.Cerrar_TablaClienteSalida();
    }

    private void abrirArchivosPrincipales() {

        String mensaje = "";

        //Ax: cargar rutas de archivos y salir si falta uno solo!
        infoRegistroEntrada.setArchivo_TablaRegistroDeEntrada(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/REGISTRO.TXT");
        anomaliaDeLectura.setArchivo_AnomaliaDeNoLectura(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/CAUSA_NL.TXT");
        tablaTarifa.setArchivo_TablaTarifas(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/TARIFAS.TXT");
        tablaConvenio.setArchivo_TablaConvenios(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/CONVENIO.TXT");
        descripcionConcepto.setArchivo_TablaDescripcionConceptos(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/DES_CONC.TXT");
        infoMedidorEntrada.setArchivo_TablaMedidorEntrada(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/MEDIDOR.TXT");
        infoClienteEntrada.setArchivo_TablaEntradaClientes(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/CLIENTE.TXT");
        // misRangos.setArchivo_TablaRangos(VariablesGlobales.directorioactual + "/DatosDeEntrada/RANGOS.TXT");
        infoRegistroSalida.setArchivo_TablaRegistroSalida(VariablesGlobales.directorioactual + "/DATOSDESALIDA/REGISTRO.SDA");
        medidorSalida.setArchivo_TablaContadorSalida(VariablesGlobales.directorioactual + "/DATOSDESALIDA/MEDIDOR.SDA");
        infoClienteSalida.setArchivo_TablaClienteSalida(VariablesGlobales.directorioactual + "/DATOSDESALIDA/CLIENTE.SDA");
        infoCobrosLiquidados.setArchivo_TablaCobrosRealizados(VariablesGlobales.directorioactual + "/DATOSDESALIDA/CO_COBRO.SDA");
        misRangos.setArchivo_TablaRangos(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/RANGOS.TXT");

        int faltanarchivos = 0;
        File file = new File(infoRegistroEntrada.getArchivo_TablaRegistroDeEntrada());

        if (file.exists()) {
            file = new File(infoRegistroSalida.getArchivo_TablaRegistroSalida());
            if (file.exists()) {
                file = new File(infoClienteEntrada.getArchivo_TablaEntradaClientes());
                if (file.exists()) {
                    file = new File(infoClienteSalida.getArchivo_TablaClienteSalida());
                    if (file.exists()) {
                        file = new File(medidorSalida.getArchivo_TablaContadorSalida());
                        if (file.exists()) {
                            file = new File(infoMedidorEntrada.getArchivo_TablaMedidorEntrada());
                            if (file.exists()) {
                                file = new File(infoCobrosLiquidados.getArchivo_TablaCobrosRealizados());
                                if (file.exists()) {
                                    file = new File(anomaliaDeLectura.getArchivo_AnomaliaDeNoLectura());
                                    if (file.exists()) {
                                        file = new File(tablaTarifa.getArchivo_TablaTarifas());
                                        if (file.exists()) {
                                            file = new File(descripcionConcepto.getArchivo_TablaDescripcionConceptos());
                                            if (file.exists()) {
                                                file = new File(tablaConvenio.getArchivo_TablaConvenios());
                                                if (file.exists()) {
                                                    if (leerArchivosSecundarios() == 1) {

                                                        abrirArchivosDeFacturacion();
                                                        inicializarVariablesSistema();

                                                        seleccionMesFacturacion();

                                                        ejecutarResumen(3);

                                                        variables.numdigitos = 0;
                                                        visualizarInformacionCliente(0);//
                                                        variables.direcciondelectura = variables.haciaadelante;
                                                        faltanarchivos = 1;
                                                        cerrarArchivosFacturacion(); //Ax* preguntar victor por los TODO
                                                    } else {
                                                        mensaje = "Faltan Archivos de \nTrabajo Verificar";
                                                        try {
                                                            // if
                                                            // (serialPort1.IsOpen)
                                                            // {
                                                            // serialPort1.Close();
                                                            // }
                                                        } catch (Exception e) {
                                                            mensajeT("No disponibilidad de puerto", msgMedio);
                                                            // Toast.makeText(getApplicationContext(), "No disponibilidad de puerto", Toast.LENGTH_LONG).show();
                                                        }
                                                        cerrarProcesoFacturacion();
                                                    }
                                                } else
                                                    mensaje = "No existe:\n" + tablaConvenio.getArchivo_TablaConvenios();
                                            } else
                                                mensaje = "No existe:\n" + descripcionConcepto.getArchivo_TablaDescripcionConceptos();
                                        } else
                                            mensaje = "No existe:\n" + tablaTarifa.getArchivo_TablaTarifas();

                                    } else
                                        mensaje = "No existe:\n" + anomaliaDeLectura.getArchivo_AnomaliaDeNoLectura();
                                } else
                                    mensaje = "No existe:\n" + infoCobrosLiquidados.getArchivo_TablaCobrosRealizados();
                            } else
                                mensaje = "No existe:\n" + infoMedidorEntrada.getArchivo_TablaMedidorEntrada();
                        } else
                            mensaje = "No existe:\n" + medidorSalida.getArchivo_TablaContadorSalida();
                    } else
                        mensaje = "No existe:\n" + infoClienteSalida.getArchivo_TablaClienteSalida();
                } else
                    mensaje = "No existe:\n" + infoClienteEntrada.getArchivo_TablaEntradaClientes();
            } else
                mensaje = "No existe:\n" + infoRegistroSalida.getArchivo_TablaRegistroSalida();
        } else
            mensaje = "No existe:" + infoRegistroEntrada.getArchivo_TablaRegistroDeEntrada();

        if (faltanarchivos == 0) {
            mensajeT(mensaje + " \n Debe Cargar Archivos...", msgLargo);
            cerrarProcesoFacturacion();
        }
        return;
    }

    private void ejecutarResumen(int TipoResumen) {

        variables.clienteactual = VariablesGlobales.registroactual;

        if (VariablesGlobales.registroactual == 0) {
            // VariablesGlobales.registroactual = 1;
            variables.clienteactual = 1;
        }

        leerInformacionUsuario(3);
        visualizarInformacionCliente(3);
        apuntadortarifareactiva = 0;
        apuntadortarifaCT = 0;
    }

    private void seleccionMesFacturacion() {

        infoClienteEntrada.lectura_TablaEntradaClientes(1);
        String mesActual = variables.convertirMesLetras(infoClienteEntrada.gettablaEntradaClientes_mes());
        variables.mesactual = mesActual + " " + infoClienteEntrada.gettablaEntradaClientes_anio() + " " + mesActual + " " + infoClienteEntrada.gettablaEntradaClientes_anio();
    }

    private void inicializarVariablesSistema() {

        variables.mesenformato = "";
        variables.fechahoy = "";
        variables.terminal = "001";
        variables.impresora = "   ";
        variables.modosupervisor = 0;
        variables.tipoimpresora = 1;
        variables.ultimotiempo = 1;
        variables.barrido = 1;

        tomarFechaSistema(variables.amd, variables.hm);
        convertirFormatoDeFecha(variables.amd, variables.fechahoy, variables.mesenformato, "AMD");
        variables.diashoy = (int) variables.calcularNumerodeDias(variables.amd);
        VariablesGlobales.totalimpresiones = 0;
    }

    private int leerArchivosSecundarios() {

        tablaClaseServicio.setArchivo_TablaClaseServicio(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/CLASE_SE.TXT");// Ax: Abreviaturas
        municipio.setArchivo_TablaMunicipios(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/MUNICIP.TXT");

        File file = new File(tablaClaseServicio.getArchivo_TablaClaseServicio());

        if (file.exists()) {
            file = new File(municipio.getArchivo_TablaMunicipios());
            if (file.exists())
                return 1;
            else

                mensajeT("Archivo No existe: " + municipio.getArchivo_TablaMunicipios(), msgLargo);
        } else
            mensajeT("Archivo No existe: " + tablaClaseServicio.getArchivo_TablaClaseServicio(), msgLargo);
        // Toast.makeText(getApplicationContext(), "Archivo No existe: " + tablaClaseServicio.getArchivo_TablaClaseServicio(), Toast.LENGTH_LONG).show();
        return 0;
    }

    private void cerrarProcesoFacturacion() {

        try {
            cerrarArchivosFacturacion();

            utils.MensajeTime("PROCESO CERRADO...Debe enviar lo faltante", "Enviar faltantes", this, 6);

            final File g = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/Leido.txt");
            if (!g.exists()) {
                utils.EscribirLinea(g, " ");
            }

            if (procesandoenvioenHilos == 1) {
                mensajeT("El sistema esta en proceso de envio de datos al servidor no se puede salir del sistema", msgCorto);
                return;
            }

            File archivo1 = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/EnviosGPRS.SDA");

            if (archivo1.length() > 0) {

                if (comprobarconexiones(false)) {
                    envioFacturacionHilos();
                }

            } else {
                archivo1.delete();
            }

            envioHilos_CertPostal_Censo_Aforos_Novedades(true, true, true, true, true); //Se hace envio de  "novedades", "aforos", "censo" "certificado",

        } catch (Exception e) {
            mensajeT("Problema cerrando Formulario de Lecturas", msgMedio);
            e.printStackTrace();
        }
    }

    /**
     * Retorna la fecha actual en formato dd/MM/yyyy
     */
    private String getPhoneDate() {

        Calendar cal = new GregorianCalendar();
        Date date = cal.getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy"); //Ax. mes aparentemente ok
        String formatteDate = df.format(date);

        return formatteDate;
    }

    private void limpiarMedidores() {

        nroContador1 = "";
        idContador1 = "";
        lecturaTomada1 = "";
        causadenolectura1 = "";
        intentos1 = "";
        lecturaModificada11 = "";
        lecturaModificada21 = "";
        digitos1 = "";
        criticaPDA1 = "";
        lecturaAnterior1 = "";

        TipoMedida1 = "";
        TipoMedida2 = "";
        TipoMedida3 = "";
        TipoMedida4 = "";
        TipoMedida5 = "";

        nroContador2 = "";
        idContador2 = "";
        lecturaTomada2 = "";
        causadenolectura2 = "";
        intentos2 = "";
        lecturaModificada12 = "";
        lecturaModificada22 = "";
        digitos2 = "";
        criticaPDA2 = "";
        lecturaAnterior2 = "";

        nroContador3 = "";
        idContador3 = "";
        lecturaTomada3 = "";
        causadenolectura3 = "";
        intentos3 = "";
        lecturaModificada13 = "";
        lecturaModificada23 = "";
        digitos3 = "";
        criticaPDA3 = "";
        lecturaAnterior3 = "";

        nroContador4 = "";
        idContador4 = "";
        lecturaTomada4 = "";
        causadenolectura4 = "";
        intentos4 = "";
        lecturaModificada14 = "";
        lecturaModificada24 = "";
        digitos4 = "";
        criticaPDA4 = "";
        lecturaAnterior4 = "";

        nroContador5 = "";
        idContador5 = "";
        lecturaTomada5 = "";
        causadenolectura5 = "";
        intentos5 = "";
        lecturaModificada15 = "";
        lecturaModificada25 = "";
        digitos5 = "";
        criticaPDA5 = "";
        lecturaAnterior5 = "";
    }

    /*private void txtLecturaActualKeyDown(String action) { //Ax: anteriormente (int actionId, KeyEvent event)  //String action

        String texto = txtElectura.getText().toString().trim();

        if (texto.length() == 0 || texto.equals(".") || texto.equals("-") || texto.equals("N")) {//Antes solo ""

            if (lblpuntero.getText().toString().trim().substring(1, 9).equals("CONCLUIDO")) {

                if (null != action && action.contains(" ")) {//Ax: anteriormente (null != event && event.getKeyCode() == KeyEvent.KEYCODE_M || event.getKeyCode() == KeyEvent.KEYCODE_MINUS)//(null != action && action.contains(" "))

                    txtElectura.setText("");
                    visualizarInformacionCliente(3);
                    return;
                }
                visualizarInformacionCliente(4);
                txtElectura.setText("");
                return;
            }
            if (lblpuntero.getText().toString().trim().substring(1, 9).equals("TERMINADO")) {
                visualizarInformacionCliente(3);
                txtElectura.setText("");

                nombreDeArchivo = VariablesGlobales.directorioactual + "/DatosDeEntrada/Leido.txt";

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Alerta Seleccion Terminacion");
                builder.setMessage("Ya termino de realizar\n la Facturacion? " + VariablesGlobales.totalprediosleidos + " == "
                        + infoClienteEntrada.getTotal_TablaEntradaClientes());

                builder.setIcon(R.drawable.ic_launcher1);

                builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        try {
                            RandomAccessFile rFile = new RandomAccessFile(nombreDeArchivo, "rw");
                            rFile.close();

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        cerrarProcesoFacturacion();
                        return;
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // Toast.makeText(getApplicationContext(), "Boton No pulsado", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.show();
            }

            if (action.contains("N")) { //Ax: Si no hay nada en DIGITAR LECTURA y se presiona (N), se despliega el menu
                getSupportActionBar().openOptionsMenu();
            }

            if (action.contains(" ")) { //Ax: Si no hay nada en DIGITAR LECTURA y se presiona espacio(_), se abre captura Imagenu
                // capturaImagenFotografica();
            }

            if (action.contains(".")) {//Ax: Si no hay nada en DIGITAR LECTURA y se presiona punto(.), se va acaptura de anomalia
                if (autorizacion()) {
                    capturarAnomaliaTerreno();
                }
            }

            if (action.contains("-")) {//Ax: Si no hay nada en DIGITAR LECTURA y se presiona guion (-), se  ejecuta Busqueda
                ejecutarBusquedaCliente();
            }
        }
    }*/

    private void capturarAnomaliaTerreno() {

        AnomaliaDeNoLectura anomaliaDeNoLectura = new AnomaliaDeNoLectura();
        anomaliaDeNoLectura.setArchivo_AnomaliaDeNoLectura(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/CAUSA_NL.TXT");

        try {
            abrirArchivosDeFacturacion();

            //if (Variables.EvaluarConServicio(InfoMedidorEntrada.ECONTADTIPOMEDIDOR.Trim()) == 0
            //        || EvaluarCargaInstalada(Variables.Consumoactual) > 0
            //        || InfoMedidorEntrada.ECONTADIDCORTADO.Trim().PadLeft(1, '0') == "1"
            //        || InfoRegistroEntrada.EREGISTCRITICA1.Trim() == "99" || InfoRegistroEntrada.EREGISTCRITICA1.Trim() == "97")//nuevo con critica
            //
            //if (validarEstadoRegistro(VariablesGlobales.registroactual, "L") == 0
            //        || infoClienteSalida.gettablaClienteSalida_INDFACTURACION().trim().equals("")
            //        || infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().equals("L")) {

            if (evaluarCargaInstalada(0) > 0 ||
                    infoMedidorEntrada.gettablaMedidorEntrada_Tipomedidor().trim().equals("Z") ||
                    !infoRegistroSalida.gettablaRegistroSalida_LEIDO().trim().equals("") ||
                    infoMedidorEntrada.gettablaMedidorEntrada_idcortado().trim().equals("1")

                    ) {

                if (!infoRegistroSalida.gettablaRegistroSalida_LEIDO().trim().equals(""))
                    mensajeT("Cuenta ya esta Leida y Facturada \n" + "Prohibido\n" + "Modificar Factura\n", msgLargo);
                    //  Toast.makeText(getApplicationContext(), "Cuenta ya esta Leida y Facturada \n" + "Prohibido\n" + "Modificar Factura\n", Toast.LENGTH_LONG).show();
                else
                    mensajeT("A las cuentas Directas o Contratadas no se le incluye novedad en Lecturas solo indique continuar", msgMedio);
                //Toast.makeText(getApplicationContext(), "A las cuentas Directas o Contratadas no se le incluye novedad en Lecturas solo indique continuar", Toast.LENGTH_LONG).show();
                cerrarArchivosFacturacion();
                return;
            }

            VariablesGlobales.datodebusqueda = "";

            try {
                //Ax: Abre el modulo y se identifica con el ID (ANOMALIA_REQUEST_CODE), que mas adelante se recupera valor en 'onActivityResult' para procesar la anomalia
                Intent intent = new Intent(MenuDeLiquidacion.this, ModuloDeAnomalias.class);
                intent.putExtra("Algo", "");
                startActivityForResult(intent, ANOMALIA_REQUEST_CODE);

            } catch (Exception e) {
                mensajeT("Error al iniciar actividad de Anomalias" + e.getMessage(), msgLargo);
                //  Toast.makeText(getApplicationContext(), "Error al iniciar actividad de Anomalias" + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            cerrarArchivosFacturacion();

        } catch (Exception e) {
            mensajeT("Error con el controlador en causa de no lectura", msgLargo);
            //Toast.makeText(getApplicationContext(), "Error con el controlador en causa de no lectura", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void procesarAnomaliaSeleccionada(String anomalia) {
        causal = "";
        VariablesGlobales.datodebusqueda = anomalia.substring(0, 3).trim();

        if (VariablesGlobales.datodebusqueda.trim().length() > 0) {
            if (ingresarAnomaliaNoLectura(0, VariablesGlobales.datodebusqueda.trim()) == 1) {
                yaTomoFotoOcr = false;
                procesarLectura(0, 0, "", VariablesGlobales.datodebusqueda.trim());
            } else {
                mensajeT("Anomalia de \n" + "Lectura no existe en la tabla\n", msgLargo);
            }


        }
    }

    //Ax...
    public void ProcesarAnomalias() {
        abrirArchivosDeFacturacion();
        procesarAnomaliaSeleccionada(causal);
        causal = "";
        cerrarArchivosFacturacion();
    }

    //Ax: metodo para lanzar AlertDialog (que es asincrono) y post-ejecutar algun otro metodo con el parametro 'metodo'
    public void MostrarAlertDialog(String titulo, String mensaje, final String NombreMetodo) { //Crea un alertDialog con si-no y espera hasta un clic SI o No

        final AlertDialog.Builder builder = new AlertDialog.Builder(MenuDeLiquidacion.this);

        builder.setTitle(titulo);
        builder.setMessage(mensaje);
        builder.setIcon(R.drawable.ic_launcher1);

        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                switch (NombreMetodo) { //Esto es para reusar este metodo 'MostrarAlertDialog' con mas llamados en 'NombreMetodo'
                    case "ProcesarAnomalias":
                        ProcesarAnomalias();
                        break;

                    case "ProcesarLectura":
                        procesarLectura(1, 0, txtElectura.getText().toString().trim(), "XY");
                        activaLectorBarras = true;
                        cerrarArchivosFacturacion();
                        break;

                    case "procesarLectura2": //Para Cuenta Contratada
                        banderaCuentaContratada = 1;
                        procesarLectura(cc_indlectura, cc_primero, cc_lecturaact, cc_causaact);
                        banderaCuentaContratada = 0;
                        break;

                    case "adicionarNovedad":
                        banderaadicionarNovedad = 1;
                        // Log.e("error", "novedad 2");
                        adicionarNovedad(2, 0);
                        banderaadicionarNovedad = 0;
                        break;

                    case "validarconexion":
                        conexionGPRSActiva = 1;
                        // Log.e("error", "enviargprs");
                        envioFacturacionHilos();
                        break;

                    case "ImpresionLote":
                        GeneracionImpresionEnLote(VariablesGlobales.rangoinicialimpresion, VariablesGlobales.rangofinalimpresion);
                        infoRegistroSalida.lectura_TablaRegistroSalida(VariablesGlobales.registroactual);
                        visualizarInformacionCliente(0);
                        cerrarArchivosFacturacion();
                        break;

                    case "procesoDeLiquidacionEImpresion20":
                        ventanaConsumo = false;
                        if (reimpresion_factura) {//indica que este llamado se realizo desde menu 'reimprimir factura' y no desde 'procesarLectura'
                            procesoDeLiquidacionEImpresion3();
                        } else {
                            procesoDeLiquidacionEImpresion2();
                        }

                        break;

                    case "liquidacionautomatica":
                        MODO = "AUTO";
                        myThread.interrupt();
                        VariablesGlobales.btPrintService.stop();
                        msgAdmon = String.format("%-42s", "LIQUIDACION AUTOMATICA");
                        mensajeAdm(MenuDeLiquidacion.this);
                        //metodo = "liquidacionautomatica1";
                       // 1. RESUMEN_ENVIOSGPRS.TXT
                       // 2. RESUMEN_REGISTRO.TXT
                       // 3. RESUMEN_TABLADB.TXT

                        File ArchivoResumen = new File(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/RESUMEN_ENVIOSGPRS.TXT");
                        if (ArchivoResumen.exists()) {
                            //IncluirDatosEnModoAutomatico2(); // del menu
                            metodo = "liquidacionautomatica2";// del menu
                        } else {
                            //IncluirDatosEnModoAutomatico();
                            ArchivoResumen = new File(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/RESUMEN_REGISTRO.TXT");
                            if (ArchivoResumen.exists()) {
                                metodo = "liquidacionautomatica1";// del men
                            }   else {
                                metodo = "liquidacionautomatica3";// del men
                            }
                        }
                        TaskHelper.execute(new AsyncCallWS(), metodo);
                        break;

                    case "liquidacionautomatica_2":
                        modoRetorna = true;
                        IncluirDatosEnModoAutomatico();
                        break;

                    case "reimpresiondefactura":

                        if (!infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("L")
                                && !infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("N")
                                && !infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("E")) {

//                            if (infoClienteSalida.gettablaClienteSalida_INDFACTURACION().trim().equals("L")) {
//                                EsSoloLectura = 1;
//                            }
                            limpiarMedidores();
                            tiempoInactivolaImpresora = 0;
                            reimpresion_factura = true;
                            procesoDeLiquidacionEImpresion(3);

//                            if (EsSoloLectura == 1) {
//                                guardarEnvioGPRS(1);// se mandaria a guardar en el archivo de envio
//                            }
                        } else
                            mensajeT("Predio NO se Imprime validar con el supervisor", msgLargo);

//                        VariablesGlobales.registroactual = alterno;
//                        leerInformacionUsuario(1);
//                        visualizarInformacionCliente(0);
//                        cerrarArchivosFacturacion();
//                        // fin del proceso de reimpresion
//                        limpiarMedidores();
                        break;
                }
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                switch (NombreMetodo) {
                    case "ProcesarAnomalias":
                        causal = "";
                        break;
                    case "ProcesarLectura":
                        activaLectorBarras = true;
                        cerrarArchivosFacturacion();
                        break;
                    case "procesarLectura2": //Para Cuenta Contratada
                        banderaCuentaContratada = -1;
                        procesarLectura(cc_indlectura, cc_primero, cc_lecturaact, cc_causaact);
                        banderaCuentaContratada = 0;
                        break;
                    case "adicionarNovedad":
                        banderaadicionarNovedad = -1;
                        // Log.e("error", "novedad 3");
                        adicionarNovedad(1, 0);
                        banderaadicionarNovedad = 0;
                        break;
                    case "validarconexion":
                        conexionGPRSActiva = 0;
                        break;
                    case "ImpresionLote":
                        infoRegistroSalida.lectura_TablaRegistroSalida(VariablesGlobales.registroactual);
                        visualizarInformacionCliente(0);
                        cerrarArchivosFacturacion();
                        break;
                    case "procesoDeLiquidacionEImpresion20":
                        ventanaConsumo = false;

                        if (reimpresion_factura) {
                            reimpresion_factura = false;
                            VariablesGlobales.registroactual = alterno; // Ojo aqui podria pasar algo raro
                            leerInformacionUsuario(1);
                            visualizarInformacionCliente(0);
                            cerrarArchivosFacturacion();
                            limpiarMedidores();
                        }

                        procesarLectura3();
                        break;
                    case "liquidacionautomatica":
                        MODO = "MANU";
                        File ArchivoResumen = new File(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/RESUMENLECTURAS2.TXT");
                        if (ArchivoResumen.exists()) {
                            IncluirDatosEnModoAutomatico2();
                        } else {
                            IncluirDatosEnModoAutomatico();
                        }
                        break;

                    case "liquidacionautomatica_2":
                        modoRetorna = false;
                        break;

                    case "reimpresiondefactura":
                        reimpresion_factura = false;
                        VariablesGlobales.registroactual = alterno;
                        leerInformacionUsuario(1);
                        visualizarInformacionCliente(0);
                        cerrarArchivosFacturacion();
                        limpiarMedidores();
                        break;
                }
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                dialogInterface.dismiss();
                switch (NombreMetodo) {
                    case "ProcesarAnomalias":
                        causal = "";
                        break;
                    case "ProcesarLectura":
                        activaLectorBarras = true;
                        cerrarArchivosFacturacion();
                        break;
                    case "procesarLectura2": //Para Cuenta Contratada
                        banderaCuentaContratada = -1;
                        procesarLectura(cc_indlectura, cc_primero, cc_lecturaact, cc_causaact);
                        banderaCuentaContratada = 0;
                        break;
                    case "adicionarNovedad":
                        banderaadicionarNovedad = -1;
                        // Log.e("error", "novedad 3");
                        adicionarNovedad(1, 0);
                        banderaadicionarNovedad = 0;
                        break;
                    case "validarconexion":
                        conexionGPRSActiva = 0;
                        break;
                    case "ImpresionLote":
                        infoRegistroSalida.lectura_TablaRegistroSalida(VariablesGlobales.registroactual);
                        visualizarInformacionCliente(0);
                        cerrarArchivosFacturacion();
                        break;
                    case "procesoDeLiquidacionEImpresion20":
                        ventanaConsumo = false;

                        if (reimpresion_factura) {
                            reimpresion_factura = false;
                            VariablesGlobales.registroactual = alterno;// Ojo aqui podria hacer algo raro
                            leerInformacionUsuario(1);
                            visualizarInformacionCliente(0);
                            cerrarArchivosFacturacion();
                            limpiarMedidores();
                        }

                        procesarLectura3();
                        break;
                    case "liquidacionautomatica":
                        MODO = "MANU";
                        File ArchivoResumen = new File(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/RESUMENLECTURAS2.TXT");
                        if (ArchivoResumen.exists()) {
                            IncluirDatosEnModoAutomatico2();
                        } else {
                            IncluirDatosEnModoAutomatico();
                        }
                        break;

                    case "liquidacionautomatica_2":
                        modoRetorna = false;
                        break;

                    case "reimpresiondefactura":
                        reimpresion_factura = false;
                        VariablesGlobales.registroactual = alterno;
                        leerInformacionUsuario(1);
                        visualizarInformacionCliente(0);
                        cerrarArchivosFacturacion();
                        limpiarMedidores();
                        break;
                }
            }
        });
//        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialogInterface) {
//                dialogInterface.dismiss();
//                switch (NombreMetodo){
//                    case "procesoDeLiquidacionEImpresion20":
//                        ventanaConsumo = false;
//
//                        if (reimpresion_factura) {
//                            reimpresion_factura = false;
//                            VariablesGlobales.registroactual = alterno;
//                            leerInformacionUsuario(1);
//                            visualizarInformacionCliente(0);
//                            cerrarArchivosFacturacion();
//                            limpiarMedidores();
//                        }
//
//                        procesarLectura3();
//                        break;
//                }
//            }
//        });

        builder.create().show();
    }

    private int ingresarAnomaliaNoLectura(int consumoafacturar, String causaact) {

        int registros;
        int regactual;
        String causa2 = "";
        double totalconsumo;
        double promreal;
        variables.consumoactual = 0;
        if (infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO().trim().equals(""))
            infoClienteSalida.settablaClienteSalida_CONSUMOFACTURADO("0");

        totalconsumo = Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumo1().trim())
                + Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumo2().trim())
                + Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumo3().trim())
                + Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumo4().trim())
                + Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumo5().trim())
                + Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumo6().trim());

        promreal = (double) ((int) (totalconsumo / (double) 6));

        if ((totalconsumo - (promreal * (double) 6)) > 0)
            ++promreal;

        if (parseStringToInteger(infoRegistroSalida.gettablaRegistroSalida_INTENTOS()) > 5) {

            if (indicadorManual == 0) {
                mensajeT("Numero de Intentos\n ya fueron ejecutadas\nAcceso no permitido", msgMedio);
            }
            return (0);
        }

        if (infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim().length() > 0) {
            if (indicadorManual == 0) {
                mensajeT("El cliente fue liquidado por\nAnomalia de No Lectura", msgLargo);
            }
            return (0);
        }

        if (indicadorManual == 1) {

            String causa = "";

            if (parseStringToInteger(causaact) > 0) {
                causa = causaact;
            }

            if (anomaliaDeLectura.abrir_AnomaliaDeNoLectura(anomaliaDeLectura.getArchivo_AnomaliaDeNoLectura())) {
                {
                    anomaliaDeLectura.buscarbinario_AnomaliaDeNoLectura(causa);
                }

                if (anomaliaDeLectura.getEncontro_AnomaliaDeNoLectura() == 0) {
                    if (indicadorManual == 0) {
                        mensajeT("Anomalia no existe en la Tabla\n" + causa, msgLargo);
                    }
                    return (0);
                }
            }
        }


        tomarFechaSistema(medidorSalida.gettablaContadorSalida_FECHALECTURA(), medidorSalida.gettablaContadorSalida_HORALECTURA());
        medidorSalida.settablaContadorSalida_Fechalectura(variables.amd);
        medidorSalida.settablaContadorSalida_HORALECTURA(variables.hm);

        infoRegistroSalida.lectura_TablaRegistroSalida(VariablesGlobales.registroactual);
        infoRegistroSalida.settablaRegistroSalida_causadenolectura(anomaliaDeLectura.getanomaliaDeNoLectura_CODIGO());
        infoRegistroSalida.settablaRegistroSalida_lecturatomada("0000000000");
        variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";ingresarAnomaliaNoLectura;" + getPhoneDate() + "-" + getPhoneHour();
        escribeResumenTiempo(variableResumenLiquidacion + " " + "ingresarAnomaliaNoLectura: Posicion Actual|" + VariablesGlobales.registroactual);
        infoRegistroSalida.escribir_TablaRegistroSalida(VariablesGlobales.registroactual);
        tomarFechaSistema(medidorSalida.gettablaContadorSalida_FECHALECTURA(), medidorSalida.gettablaContadorSalida_HORALECTURA());
        infoClienteSalida.settablaClienteSalida_CONSUMOFACTURADO("" + ((parseStringToInteger(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO()) + 0)));
        infoClienteSalida.settablaClienteSalida_INDFACTURACION("L");
        variables.estado = "5";

        // Log.e("error", "anomailia " + anomaliaDeLectura.getanomaliaDeNoLectura_FACTURAPROMEDIO().charAt(0));
        if (infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO().trim().equals(""))
            infoClienteSalida.settablaClienteSalida_CONSUMOFACTURADO("0");

        switch (anomaliaDeLectura.getanomaliaDeNoLectura_FACTURAPROMEDIO().charAt(0)) {  //enerca no usa CT
            case 'L':

                infoClienteSalida.settablaClienteSalida_CONSUMOFACTURADO("0");
              /*  if (infoRegistroEntrada...gettablaRegistroDeEntrada_Tipomedida().trim().equals("CT"))
                    infoClienteSalida.settablaClienteSalida_consumo3("0");
                else*/
                if (!infoRegistroEntrada.gettablaRegistroDeEntrada_tipoenergia().trim().equals("R"))
                    infoClienteSalida.settablaClienteSalida_consumo2("0");
                else
                    infoClienteSalida.settablaClienteSalida_consumo1("0");

                break;
            case 'C':
                // Log.e("error", " ingresa anomailia ");

                if (infoClienteEntrada.gettablaEntradaClientes_cargainstalada().trim().length() == 0)
                    infoClienteEntrada.settablaEntradaClientes_cargainstalada("0");
                if (infoClienteEntrada.gettablaEntradaClientes_dato().trim().length() == 0)
                    infoClienteEntrada.settablaEntradaClientes_dato("0");

                double carga = variables.ejecutarAjusteUnidades(Double.parseDouble(infoClienteEntrada.gettablaEntradaClientes_cargainstalada().trim()));
                infoClienteEntrada.settablaEntradaClientes_cargainstalada("" + (int) carga);

                if (parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Bimestral().trim()) == 1) {
                    if (String.format("%1$1s", infoMedidorEntrada.gettablaMedidorEntrada_idcortado().trim()).equals("1")) {

                        if (!infoRegistroEntrada.gettablaRegistroDeEntrada_Tipomedida().trim().equals("CT")) {
                            infoClienteSalida.settablaClienteSalida_CONSUMOFACTURADO("" + parseStringToInteger("" + (Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO().trim()) + variables.ejecutarAjusteUnidades(Double.parseDouble(infoMedidorEntrada.gettablaMedidorEntrada_lecturacorte().trim())))));
                        }

                        infoRegistroSalida.settablaRegistroSalida_consumotomado("" + parseStringToInteger("" + variables.ejecutarAjusteUnidades((Double.parseDouble(infoMedidorEntrada.gettablaMedidorEntrada_lecturacorte().trim())))));
                    } else {
                        if (!infoRegistroEntrada.gettablaRegistroDeEntrada_Tipomedida().trim().equals("CT"))
                            infoClienteSalida.settablaClienteSalida_CONSUMOFACTURADO("" + parseStringToInteger("" + (Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO().replace(",", ".").trim()) + variables.ejecutarAjusteUnidades(Double.parseDouble(infoClienteEntrada.gettablaEntradaClientes_cargainstalada().replace(",", ".").trim()) * Double.parseDouble(infoClienteEntrada.gettablaEntradaClientes_dato().replace(",", ".").trim()) / 100))));

                        infoRegistroSalida.settablaRegistroSalida_consumotomado("" + parseStringToInteger("" + variables.ejecutarAjusteUnidades((Double.parseDouble(infoClienteEntrada.gettablaEntradaClientes_cargainstalada().replace(",", ".").trim()) * Double.parseDouble(infoClienteEntrada.gettablaEntradaClientes_dato().replace(",", "").trim()) / 100))));
                    }

                    //  if (infoRegistroEntrada...gettablaRegistroDeEntrada_Tipomedida().trim().equals("CT"))
                    //      infoClienteSalida.settablaClienteSalida_consumo3("" + parseStringToInteger(infoRegistroSalida.gettablaRegistroSalida_CONSUMOTOMADO()));
                    //  else
                    if (!infoRegistroEntrada.gettablaRegistroDeEntrada_tipoenergia().trim().equals("R"))
                        infoClienteSalida.settablaClienteSalida_consumo2("" + parseStringToInteger(infoRegistroSalida.gettablaRegistroSalida_CONSUMOTOMADO()));
                    else
                        infoClienteSalida.settablaClienteSalida_consumo1("" + parseStringToInteger(infoRegistroSalida.gettablaRegistroSalida_CONSUMOTOMADO()));
                } else {
                    if (infoMedidorEntrada.gettablaMedidorEntrada_idcortado().trim().equals("1")) {
                        if (!infoRegistroEntrada.gettablaRegistroDeEntrada_Tipomedida().trim().equals("CT"))
                            infoClienteSalida.settablaClienteSalida_CONSUMOFACTURADO("" + parseStringToInteger((Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO().trim()) + variables.ejecutarAjusteUnidades(Double.parseDouble(infoMedidorEntrada.gettablaMedidorEntrada_lecturacorte().trim()))) + ""));
                        infoRegistroSalida.settablaRegistroSalida_consumotomado("" + parseStringToInteger("" + variables.ejecutarAjusteUnidades(
                                (Double.parseDouble(infoMedidorEntrada.gettablaMedidorEntrada_lecturacorte().trim())))));
                    } else {
                        if (!infoRegistroEntrada.gettablaRegistroDeEntrada_Tipomedida().trim().equals("CT"))
                            infoClienteSalida.settablaClienteSalida_CONSUMOFACTURADO("" + parseStringToInteger((Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO().trim()) + variables.ejecutarAjusteUnidades((Double.parseDouble(infoClienteEntrada.gettablaEntradaClientes_cargainstalada().trim()) * Double.parseDouble(infoClienteEntrada.gettablaEntradaClientes_dato().trim()) / 100))) + ""));//se retira * parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Bimestral())
                        infoRegistroSalida.settablaRegistroSalida_consumotomado("" + parseStringToInteger("" + variables.ejecutarAjusteUnidades(((Double.parseDouble(infoClienteEntrada.gettablaEntradaClientes_cargainstalada().trim()) * Double.parseDouble(infoClienteEntrada.gettablaEntradaClientes_dato().trim()) / 100)))));//se retira * parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Bimestral())
                    }

                    if (!infoRegistroEntrada.gettablaRegistroDeEntrada_tipoenergia().trim().equals("R"))
                        infoClienteSalida.settablaClienteSalida_consumo2("" + parseStringToInteger(infoRegistroSalida.gettablaRegistroSalida_CONSUMOTOMADO()) * parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Bimestral().trim()));
                    else
                        infoClienteSalida.settablaClienteSalida_consumo1("" + parseStringToInteger(infoRegistroSalida.gettablaRegistroSalida_CONSUMOTOMADO())* parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Bimestral().trim()));
                }
                break;
            case 'M':
                double promedio = variables.ejecutarAjusteUnidades(Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediocliente().trim()));
                infoRegistroEntrada.settablaRegistroDeEntrada_Consumopromediocliente("" + promedio);

                if (parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Bimestral().trim()) == 1) {

                    if (infoMedidorEntrada.gettablaMedidorEntrada_idcortado().trim().equals("1")) {
                        if (!infoRegistroEntrada.gettablaRegistroDeEntrada_Tipomedida().trim().equals("CT"))
                            infoClienteSalida.settablaClienteSalida_CONSUMOFACTURADO("" + parseStringToInteger((Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO().trim()) + variables.ejecutarAjusteUnidades(Double.parseDouble(infoMedidorEntrada.gettablaMedidorEntrada_lecturacorte().trim()))) + ""));

                        infoRegistroSalida.settablaRegistroSalida_consumotomado("" + parseStringToInteger("" + variables.ejecutarAjusteUnidades(
                                (Double.parseDouble(infoMedidorEntrada.gettablaMedidorEntrada_lecturacorte().trim())))));
                    } else {
                        if (!infoRegistroEntrada.gettablaRegistroDeEntrada_Tipomedida().trim().equals("CT"))
                            infoClienteSalida.settablaClienteSalida_CONSUMOFACTURADO("" + parseStringToInteger((Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO().trim()) + Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediocliente().trim())) + ""));
                        infoRegistroSalida.settablaRegistroSalida_consumotomado("" + parseStringToInteger(infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediocliente()));
                    }
                    //  if (infoRegistroEntrada...gettablaRegistroDeEntrada_Tipomedida().trim().equals("CT"))
                    //     infoClienteSalida.settablaClienteSalida_consumo3("" + parseStringToInteger(infoRegistroSalida.gettablaRegistroSalida_CONSUMOTOMADO()));
                    // else
                    if (!infoRegistroEntrada.gettablaRegistroDeEntrada_tipoenergia().trim().equals("R"))
                        infoClienteSalida.settablaClienteSalida_consumo2("" + parseStringToInteger(infoRegistroSalida.gettablaRegistroSalida_CONSUMOTOMADO()));
                    else
                        infoClienteSalida.settablaClienteSalida_consumo1("" + parseStringToInteger(infoRegistroSalida.gettablaRegistroSalida_CONSUMOTOMADO()));
                } else {
                    if (infoMedidorEntrada.gettablaMedidorEntrada_idcortado().trim().equals("1")) {
                        if (!infoRegistroEntrada.gettablaRegistroDeEntrada_Tipomedida().trim().equals("CT"))
                            infoClienteSalida.settablaClienteSalida_CONSUMOFACTURADO("" + parseStringToInteger((Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO().trim()) + variables.ejecutarAjusteUnidades(Double.parseDouble(infoMedidorEntrada.gettablaMedidorEntrada_lecturacorte().trim()))) + ""));
                        infoRegistroSalida.settablaRegistroSalida_consumotomado("" + parseStringToInteger("" + variables.ejecutarAjusteUnidades(
                                (Double.parseDouble(infoMedidorEntrada.gettablaMedidorEntrada_lecturacorte().trim())))));
                    } else {
                        if (!infoRegistroEntrada.gettablaRegistroDeEntrada_Tipomedida().trim().equals("CT"))
                            infoClienteSalida.settablaClienteSalida_CONSUMOFACTURADO("" + parseStringToInteger((Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO().trim()) + Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediocliente().trim())) + ""));//* parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Bimestral())
                        infoRegistroSalida.settablaRegistroSalida_consumotomado("" + parseStringToInteger("" + (Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediocliente().trim())* parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Bimestral().trim()))));// * * parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Bimestral())

                    }
                    String datover=  infoRegistroSalida.gettablaRegistroSalida_CONSUMOTOMADO()+"-- "+infoClienteEntrada.gettablaEntradaClientes_Bimestral();
                    if (!infoRegistroEntrada.gettablaRegistroDeEntrada_tipoenergia().trim().equals("R"))
                        infoClienteSalida.settablaClienteSalida_consumo2("" + parseStringToInteger(infoRegistroSalida.gettablaRegistroSalida_CONSUMOTOMADO()));//* parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Bimestral().trim())
                    else
                        infoClienteSalida.settablaClienteSalida_consumo1("" + parseStringToInteger(infoRegistroSalida.gettablaRegistroSalida_CONSUMOTOMADO()));//* parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Bimestral().trim())
                }
                break;
            case 'P':
                double promediosector = variables.ejecutarAjusteUnidades(
                        Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediosector().trim()));
                infoRegistroEntrada.settablaRegistroDeEntrada_Consumopromediosector("" + promediosector);
                if (parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Bimestral().trim()) == 1) {
                    if (infoMedidorEntrada.gettablaMedidorEntrada_idcortado().trim().equals("1")) {
                        if (!infoRegistroEntrada.gettablaRegistroDeEntrada_Tipomedida().trim().equals("CT"))

                            infoClienteSalida.settablaClienteSalida_CONSUMOFACTURADO("" + parseStringToInteger((Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO().trim()) + variables.ejecutarAjusteUnidades(Double.parseDouble(infoMedidorEntrada.gettablaMedidorEntrada_lecturacorte().trim()))) + ""));
                        infoRegistroSalida.settablaRegistroSalida_consumotomado("" + parseStringToInteger("" + variables.ejecutarAjusteUnidades(
                                (Double.parseDouble(infoMedidorEntrada.gettablaMedidorEntrada_lecturacorte().trim())))));
                    } else {
                        if (!infoRegistroEntrada.gettablaRegistroDeEntrada_Tipomedida().trim().equals("CT"))

                            infoClienteSalida.settablaClienteSalida_CONSUMOFACTURADO("" + parseStringToInteger((Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO().trim()) + Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediosector().trim())) + ""));
                        infoRegistroSalida.settablaRegistroSalida_consumotomado("" + parseStringToInteger(
                                infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediosector()));
                    }

                    if (!infoRegistroEntrada.gettablaRegistroDeEntrada_tipoenergia().trim().equals("R"))
                        infoClienteSalida.settablaClienteSalida_consumo2("" + parseStringToInteger(infoRegistroSalida.gettablaRegistroSalida_CONSUMOTOMADO()));
                    else
                        infoClienteSalida.settablaClienteSalida_consumo1("" + parseStringToInteger(infoRegistroSalida.gettablaRegistroSalida_CONSUMOTOMADO()));

                } else {
                    if (infoMedidorEntrada.gettablaMedidorEntrada_idcortado().trim().equals("1")) {
                        if (!infoRegistroEntrada.gettablaRegistroDeEntrada_Tipomedida().trim().equals("CT"))

                            infoClienteSalida.settablaClienteSalida_CONSUMOFACTURADO("" + parseStringToInteger((Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO().trim()) + variables.ejecutarAjusteUnidades(Double.parseDouble(infoMedidorEntrada.gettablaMedidorEntrada_lecturacorte()))) + ""));
                        infoRegistroSalida.settablaRegistroSalida_consumotomado("" + parseStringToInteger("" + variables.ejecutarAjusteUnidades(
                                (Double.parseDouble(infoMedidorEntrada.gettablaMedidorEntrada_lecturacorte().trim())))));
                    } else {
                        infoClienteSalida.settablaClienteSalida_CONSUMOFACTURADO("" + parseStringToInteger(((Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO().trim()) + Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediosector().trim()))) + ""));// * * parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Bimestral())
                        infoRegistroSalida.settablaRegistroSalida_consumotomado("" + parseStringToInteger("" + (Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediosector().trim())* parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Bimestral().trim()))));//* parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Bimestral())

                    }
                    if (!infoRegistroEntrada.gettablaRegistroDeEntrada_tipoenergia().trim().equals("R"))
                        infoClienteSalida.settablaClienteSalida_consumo2("" + parseStringToInteger(infoRegistroSalida.gettablaRegistroSalida_CONSUMOTOMADO()));//* parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Bimestral().trim())
                    else
                        infoClienteSalida.settablaClienteSalida_consumo1("" + parseStringToInteger(infoRegistroSalida.gettablaRegistroSalida_CONSUMOTOMADO()));//* parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Bimestral().trim())
                }

                break;
            case 'N':

                if (infoMedidorEntrada.gettablaMedidorEntrada_idcortado().trim().equals("1")) {
                    if (!infoRegistroEntrada.gettablaRegistroDeEntrada_Tipomedida().trim().equals("CT"))
                        infoClienteSalida.settablaClienteSalida_CONSUMOFACTURADO("" + parseStringToInteger(variables.ejecutarAjusteUnidades(Double.parseDouble(infoMedidorEntrada.gettablaMedidorEntrada_lecturacorte().trim())) + ""));
                    infoRegistroSalida.settablaRegistroSalida_consumotomado("" + parseStringToInteger("" + variables
                            .ejecutarAjusteUnidades((Double.parseDouble(infoMedidorEntrada.gettablaMedidorEntrada_lecturacorte().trim())))));
                } else {
                    if (!infoRegistroEntrada.gettablaRegistroDeEntrada_Tipomedida().trim().equals("CT"))
                        infoClienteSalida.settablaClienteSalida_CONSUMOFACTURADO("" + parseStringToInteger(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO()));
                }
                if (infoRegistroEntrada.gettablaRegistroDeEntrada_Tipomedida().equals("CT"))
                    infoClienteSalida.settablaClienteSalida_consumo3("" + parseStringToInteger(infoRegistroSalida.gettablaRegistroSalida_CONSUMOTOMADO()));
                else if (!infoRegistroEntrada.gettablaRegistroDeEntrada_tipoenergia().trim().equals("R"))
                    infoClienteSalida.settablaClienteSalida_consumo2("" + parseStringToInteger(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO()));
                else
                    infoClienteSalida.settablaClienteSalida_consumo1("" + parseStringToInteger(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO()));

                break;
        }
        tomarFechaSistema(infoRegistroSalida.gettablaRegistroSalida_FECHALECTURA(),
                infoRegistroSalida.gettablaRegistroSalida_HORALECTURA());
        infoRegistroSalida.settablaRegistroSalida_fechalectura(variables.amd);
        infoRegistroSalida.settablaRegistroSalida_horalectura(variables.hm);
        VariablesGlobales.totalcausasnolectura++;
        return 1;
    }

    private void ejecutarBusquedaCliente() {

        try {
            VariablesGlobales.datodebusqueda = "";
            VariablesGlobales.tipodebusqueda = 0;

            //Ax: abre intent ModuloBusquedaCuenta y cuando vuelve se identifica con el ID (BUSQUEDA_REQUEST_CODE)
            Intent intent = new Intent(MenuDeLiquidacion.this, ModuloBusquedaCuenta.class);
            startActivityForResult(intent, BUSQUEDA_REQUEST_CODE);

//            if (!VariablesGlobales.datodebusqueda.equals("")) // realizar Busqueda
//                buscarCuentaMedidor(VariablesGlobales.datodebusqueda.trim(), 0);

        } catch (Exception e) {
            mensajeT("Problema en el modulo de busqueda", msgMedio);
            //Toast.makeText(getApplicationContext(), "Problema en el modulo de busqueda", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private int buscarCuentaMedidor(String medidor, int modeloBusqueda) {

        int ultimoregistro;

        abrirArchivosDeFacturacion();

        ultimoregistro = VariablesGlobales.registroactual;

        if (VariablesGlobales.tipodebusqueda == 2) {  //Ax: se cambia esta logica ya que la clase modulo de busqueda no es igual a la original
            // incluir aqui que se encontro el registro del la cuenta
            if (!buscarApuntadorCliente(medidor)) {
                //infoClienteEntrada.buscarSecuencialeclient(medidor, 1);
                infoMedidorEntrada.setEncontro_TablaMedidorEntrada(0);//infoClienteEntrada.getTotal_TablaEntradaClientes()
            } else {
                infoMedidorEntrada.setEncontro_TablaMedidorEntrada(parseStringToInteger(miApuntador.getapuntadorCliente_APUNTADOR()));
            }
        } else {
            infoMedidorEntrada.setEncontro_TablaMedidorEntrada(Integer.parseInt(VariablesGlobales.datodebusqueda));// infoClienteEntrada.getEncontro_TablaEntradaClientes()
        }

        if (infoMedidorEntrada.getEncontro_TablaMedidorEntrada() > 0) {
            if (VariablesGlobales.tipodebusqueda == 1) {//Ax: antes VariablesGlobales.tipodebusqueda != 2.
                //[Ma] Analizar este posicionamiento
                VariablesGlobales.registroactual = infoMedidorEntrada.getEncontro_TablaMedidorEntrada();//parseStringToInteger(infoMedidorEntrada.gettablaMedidorEntrada_primerregistro());
                leerInformacionUsuario(1);
            } else {
                //[Ma] sera que leera otro cliente?
                variables.clienteactual = infoMedidorEntrada.getEncontro_TablaMedidorEntrada();
                leerInformacionUsuario(3);
            }

            visualizarInformacionCliente(0);
            //imagenNoExistePredio.setVisibility(ImageView.INVISIBLE);
            cerrarArchivosFacturacion();
            // planear toda la entrega por este lado
            if (infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("E") && !variables.impresora.equals("LE4") && !variables.impresora.trim().equals("") && !variables.impresora.trim().equals("001")) {
                realizarEntregaPredio();
                //  txtInformeEscrito.setText("Entregado => " + medidor);
                variables.impresora = "   ";
            }
        } else {
            VariablesGlobales.registroactual = ultimoregistro; //[Ma] analizar esta posicion
            leerInformacionUsuario(1);
            variables.impresora = "   ";

            //  txtInformeEscrito.setText("No existe: " + medidor);
            mensajeT("No existe: " + medidor, msgMedio);
            //imagenNoExistePredio.setVisibility(ImageView.VISIBLE);
            cerrarArchivosFacturacion();
        }
        return (0);
    }

    private boolean buscarApuntadorCliente(String apuntador) { // buscar el codigo o apuntador por el nombre de cuenta
        if (apuntador.length() >= 9) {
            if (apuntador.length() > 9)
                apuntador = apuntador.substring(0, 9);

            miApuntador.archivo_ApuntadorCliente = VariablesGlobales.directorioactual + "/DatosDeEntrada/IDClientes.TXT";
            File file = new File(miApuntador.archivo_ApuntadorCliente);

            if (file.exists()) {
                if (miApuntador.abrir_ApuntadorCliente(miApuntador.archivo_ApuntadorCliente)) {
                    miApuntador.buscarbinario_ApuntadorCliente(apuntador);
                    if (miApuntador.encontro_ApuntadorCliente > 0) {
                        miApuntador.Cerrar_ApuntadorCliente();
                        return true;
                    }
                    miApuntador.Cerrar_ApuntadorCliente();
                }
            }
        }
        return false;
    }

    private boolean realizarEntregaPredio() { // REALIZAR ENTREGA INDEPENDIENTE

        if (!autorizacion()) {
            txtElectura.setText("");
            return false;
        }

        // /se debe de incluir aqui el proceso si es entrega
      /*se comenta  if (infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("E") || VariablesGlobales.getTipoDeRuta().trim().equals("E")) {
            // realizar proceso de entrega
            if (!variables.impresora.substring(0, 2).equals("LE") && infoRegistroSalida.gettablaRegistroSalida_COMENTARIO1().trim().equals("")) {
                mensajeT("NO SE HA LEIDO \nEL CODIGO DE BARRAS\n" + VariablesGlobales.obligabarras, msgMedio);
                txtLecturaActual.setText("");
                txtLecturaActual.requestFocus();
                return false;
            }

            activaLectorBarras = false;
            abrirArchivosDeFacturacion();

            if (validarEstadoRegistro(VariablesGlobales.registroactual, "L") == 0) {

                txtLecturaActual.setText(reemplazarDatos(txtLecturaActual.getText().toString()));

                validarEstadoCliente(variables.clienteactual, false, "E");
                procesarLectura(0, 0, "", "99");
                banderaCuentaContratada = 0;
            } else
                mensajeT("Error De ENTREGA\n" + "Cliente ya ENTREGADO\n" + "PROSIGA CON LA PROXIMA CUENTA", msgLargo);

            activaLectorBarras = true;
            cerrarArchivosFacturacion();
            txtLecturaActual.setText("");
            txtLecturaActual.requestFocus();
            return true;
        }*/
        return false;
    }

    private int irUltimoRegistro() {
        int tmpreg = VariablesGlobales.registroactual;

        VariablesGlobales.registroactual = infoRegistroEntrada.getTotal_TablaRegistroDeEntrada() + 1;

        if (retrocedeRegistro() == 0) {
            VariablesGlobales.registroactual = tmpreg;
            if (indicadorManual == 0) {
                txtElectura.setEnabled(true);
            }
            return 0;
        }
        if (indicadorManual == 0) {
            txtElectura.setEnabled(true);
        }
        return (1);
    }

    private int irPrimerRegistro() {

        int tmpreg = VariablesGlobales.registroactual;

        VariablesGlobales.registroactual = 0;

        if (avanzarRegistro() == 0) {
            VariablesGlobales.registroactual = tmpreg;
            if (indicadorManual == 0) {
                txtElectura.setEnabled(true);
            }
            return (0);
        }

        if (indicadorManual == 0) {
            txtElectura.setEnabled(true);
        }
        return (1);
    }

    @SuppressLint("NewApi")
    private void txtLecturaActualKeyPress(int actionId) {

        if (!txtElectura.getText().toString().matches("^[0-9]*$")) {
            txtElectura.setText("");
            return;
        }

        if (lblpuntero.getText().toString().trim().substring(1, 9).equals("CONCLUIDO")) {
            visualizarInformacionCliente(4);
            txtElectura.setText("");
            txtElectura.setEnabled(true);
            return;
        }
        Log.e("INFO", "fecha-proceso: " + VariablesGlobales.fechaproceso);

       /* if (!verificarConfSistema(VariablesGlobales.fechaproceso) && !lector.trim().equals("432")) {
            utils.Log(logfile, "[MenuDeLiquidacion] verificarConfSistema() | " + msg_val);
            mostrarDialogoAlerta("Advertencia", msg_val);
            return;
        }*/

        if (lblpuntero.getText().toString().trim().substring(1, 9).equals("TERMINADO")) {
            visualizarInformacionCliente(3);
            txtElectura.setText("");

            final File nombreDeArchivo = new File(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/LEIDO.TXT");

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Alerta Seleccion Terminacion");
            builder.setMessage("Ya termino de realizar\n la Facturacion? " + VariablesGlobales.totalprediosleidos + " == "
                    + infoClienteEntrada.getTotal_TablaEntradaClientes());

            builder.setIcon(R.drawable.ic_launcher1);

            builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    try {
                        RandomAccessFile writer = new RandomAccessFile(nombreDeArchivo, "rw");
                        writer.close();

                        cerrarProcesoFacturacion();
                        return;

                    } catch (FileNotFoundException e) {

                        e.printStackTrace();

                    } catch (IOException e) {

                        e.printStackTrace();
                    }
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.show();
        }

        if (!autorizacion()) {
            txtElectura.setText("");
            return;
        }

        // /se debe de incluir aqui el proceso si es entrega
        if (VariablesGlobales.getTipoDeRuta().trim().equals("E")) {//infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("E")

//            abrirArchivosDeFacturacion();  //Ax esto se paso en el onactivityforesult
//            infoRegistroSalida.lectura_TablaRegistroSalida(VariablesGlobales.registroactual);
//            cerrarArchivosFacturacion();

            // realizar proceso de entrega
          /*secomenta  if (!variables.impresora.substring(0, 2).equals("LE") && infoRegistroSalida.gettablaRegistroSalida_COMENTARIO1().trim().equals("")) {// Ax: [CaptCom01]
                mensajeT("NO SE HA LEIDO \nEL CODIGO DE BARRAS\n" + VariablesGlobales.obligabarras, msgMedio);
                // Toast.makeText(getApplicationContext(), "NO SE HA LEIDO \nEL CODIGO DE BARRAS\n" + VariablesGlobales.obligabarras, Toast.LENGTH_LONG).show();
                txtLecturaActual.setText("");
                txtLecturaActual.requestFocus();
                return;
            }*/

            activaLectorBarras = false;
            abrirArchivosDeFacturacion();

            if (validarEstadoRegistro(VariablesGlobales.registroactual, "L") == 0) {

                valorLeidoFactura = "" + (Double.parseDouble(VariablesGlobales.minimovalorentrega.trim()) + 1);//
                validarEstadoCliente(variables.clienteactual, false, "E");
                if (!puedeContinuarConTomaLectura()) return;
                yaTomoFotoOcr = true;
                Log.e("INFO","2 - SETEA EL VALOR DE yaTomoFotoOcr: " + yaTomoFotoOcr);
                procesarLectura(0, 0, "", "99");
                if (ventanaConsumo) return;
            } else {
                mensajeT("Error De ENTREGA\n" + "Cliente ya ENTREGADO\n" + "PROSIGA CON LA PROXIMA CUENTA", msgMedio);
            }

            activaLectorBarras = true;
            cerrarArchivosFacturacion();
            txtElectura.setText("");
            return;
        } else {
            //String Dato = txtLecturaActual.getText().toString();
            if (txtElectura.getText().toString().length() == 0)// || txtLecturaActual.getText().toString().length() == 1
            {
                abrirArchivosDeFacturacion();
                if ((!infoClienteSalida.gettablaClienteSalida_INDFACTURACION().equals(" ")) && (!infoClienteSalida.gettablaClienteSalida_INDFACTURACION().equals("L")) && !infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().equals("L")) {
                    mensajeT("Error al Facturar\n" + "Cliente liquidado\n" + "y facturado\n" + "no se puede tomar lectura\n", msgLargo);
                    // Toast.makeText(getApplicationContext(), "Error al Facturar\n" + "Cliente liquidado\n" + "y facturado\n" + "no se puede tomar lectura\n", Toast.LENGTH_SHORT).show();

                    txtElectura.setText("");
                    // e.Handled = true;
                    cerrarArchivosFacturacion();
                    return;
                }
                if (variables.evaluarConServicio(infoMedidorEntrada.gettablaMedidorEntrada_Tipomedidor().trim()) == 0 || evaluarCargaInstalada(variables.consumoactual) > 0 || String.format("%1$1s", infoMedidorEntrada.gettablaMedidorEntrada_idcortado().trim()).equals("1")) {
                    if (txtElectura.getText().equals("")) {
                        txtElectura.setText("");
                        activaLectorBarras = false;
                        String titulo = ""; //cv001
                        String mensaje = "";

                        if (indicadorManual == 0) // nuevo para cuando se le da automatico si esta en 1
                        {
                            if (String.format("%1$1s", infoMedidorEntrada.gettablaMedidorEntrada_idcortado().trim()).equals("1")) {
                                titulo = "ALERTA DE RETIRADOS";
                                mensaje = "MEDIDOR RETIRADO\n" + "[SI] LIQUIDAR MEDIDA\n[NO]APLICAR NOVEDAD?";
                            } else {
                                titulo = "ALERTA DE CTA. DIRECTAS";
                                mensaje = "CARGA DIRECTA\n" + "[SI] LIQUIDAR MEDIDA \n [NO] PROCESAR NOVEDAD";
                            }
                        }

                        // Log.e("error", "cta directa2");
                        MostrarAlertDialog(titulo, mensaje, "ProcesarLectura");
                    }
                    return;
                }
                cerrarArchivosFacturacion();

            } else {

                if (actionId == 6 || actionId == 5) {//Axx 5  6  //Ax. antes tenia adiionado ...&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER 555

                    // AQUI DEBEMOS PONER EL CONTROL DEL CODIGO DE BARRAS
                    // Log.e("error", "obliga fotos " + variables.impresora + " xxx " + VariablesGlobales.obligabarras);
                    if (VariablesGlobales.obligabarras.trim().equals("1") && !variables.impresora.substring(0, 2).equals("LE")
                            && infoRegistroSalida.gettablaRegistroSalida_COMENTARIO1().trim().equals("")) {
                        mensajeT("NO SE HA LEIDO \nEL CODIGO DE BARRAS\n" + VariablesGlobales.obligabarras, msgMedio);
                        txtElectura.setText("");
                        return;
                    }

                    activaLectorBarras = false;
                    abrirArchivosDeFacturacion();
                    // Log.e("error"," llenar val "+validarEstadoRegistro(VariablesGlobales.registroactual, "L"));

                    if (validarEstadoRegistro(VariablesGlobales.registroactual, "L") == 0) {

                        txtElectura.setText(reemplazarDatos(txtElectura.getText().toString()));
                        //Kimberly nuevo
                        if (variables.evaluarConServicio(infoMedidorEntrada.gettablaMedidorEntrada_Tipomedidor().trim()) == 0 || evaluarCargaInstalada(variables.consumoactual) > 0 || String.format("%1$1s", infoMedidorEntrada.gettablaMedidorEntrada_idcortado().trim()).equals("1")
                                ) {//mirar para ebsa || infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().trim().equals("99") || infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().trim().equals("97")
                            if (indicadorManual == 0) //nuevo para cuando se le da automatico si esta en 1
                            {
                                lecturaT = "0"; //txtElectura.getText().toString().trim();
                                Intent inte = new Intent();


                                    if (infoMedidorEntrada.gettablaMedidorEntrada_idcortado().trim().equals("1")) {
                                        //  alertasCuentasD("MEDIDOR RETIRADO\n" + "[SI] LIQUIDAR MEDIDA\n[NO]APLICAR NOVEDAD?");
                                        inte.putExtra("mensaje", "MEDIDOR RETIRADO\n" + "[SI] LIQUIDAR MEDIDA\n[NO]APLICAR NOVEDAD?");
                                        inte.setClass(this, AlertaCuentaD.class);
                                        startActivityForResult(inte, ALERTACUENTADIRECTA_REQUEST_CODE);
                                        // MostrarAlertDialog("ALERTA DE RETIRASOS", "MEDIDOR RETIRADO\n" + "[SI] LIQUIDAR MEDIDA\n[NO]APLICAR NOVEDAD?", "procesarLectura2");
                                        // RespuestaPreguntaPro = MessageBox.Show("MEDIDOR RETIRADO\n" + "[SI] LIQUIDAR MEDIDA\n[NO]APLICAR NOVEDAD?", "ALERTA DE RETIRASOS", MessageBoxButtons.YesNo, MessageBoxIcon.Question, MessageBoxDefaultButton.Button2);
                                    } else {
                                        // alertasCuentasD("CARGA DIRECTA\n" + "[SI] LIQUIDAR MEDIDA \n \n[NO] PROCESAR NOVEDAD");
                                        inte.putExtra("mensaje", "CARGA DIRECTA\n" + "[SI] LIQUIDAR MEDIDA \n \n[NO] PROCESAR NOVEDAD");
                                        inte.setClass(this, AlertaCuentaD.class);
                                        startActivityForResult(inte, ALERTACUENTADIRECTA_REQUEST_CODE);
                                    }
                                    variables.estado = "3";



                            }

                           /* if (infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim().equals("") && !infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().trim().equals("99") && !infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().trim().equals("97"))
                                if (infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().trim().equals("26") || infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().trim().equals("21") || infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().trim().equals("28") || infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().trim().equals("31")) {

                                    infoRegistroSalida.settablaRegistroSalida_causadenolectura(infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().trim());


                                } else if (!String.format("%1$2s", infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().trim()).replace(" ", "0").equals("07")
                                        && !String.format("%1$2s", infoRegistroEntrada.getTablaRegistroDeEntrada_critica2().trim()).replace(" ", "0").equals("07")
                                        && !String.format("%1$2s", infoRegistroEntrada.getTablaRegistroDeEntrada_critica3().trim()).replace(" ", "0").equals("07")
                                        && !String.format("%1$2s", infoRegistroEntrada.getTablaRegistroDeEntrada_critica4().trim()).replace(" ", "0").equals("07")
                                        && !String.format("%1$2s", infoRegistroEntrada.getTablaRegistroDeEntrada_critica5().trim()).replace(" ", "0").equals("07")) {
                                    infoRegistroSalida.settablaRegistroSalida_causadenolectura("7");
                                }

                            variables.lactual = 0;
                            //variables.estado = "3";
                            variables.nveces = 1;
                            Log.e("error", "ind fact 1");
                            infoClienteSalida.settablaClienteSalida_INDFACTURACION("L");

                            VariablesGlobales.totallecturas++;
                            infoRegistroSalida.settablaRegistroSalida_lecturatomada(String.format("%1$10s", txtLecturaActual.getText().toString().trim()).replace(" ", "0"));
                            Log.e("error", "lectura tomada " + infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA());
                            infoRegistroSalida.settablaRegistroSalida_leido(variables.estado);*/
                        } else {
                            if (!puedeContinuarConTomaLectura()) return;

                            // Si foto OCR obligatoria y aún no se tomó → lanzar OCR
                            if (fotoOcrObligatoria && !yaTomoFotoOcr) {
                                mensajeT("La foto del medidor es obligatoria.\nSe abrirá la cámara para capturar la lectura.", msgMedio);
                                capturaImagenFotografica();
                                return;
                            }

                            // Si el operario modificó la lectura respecto al OCR → guardar OCR en lecturamodificada1
                            String lecturaIngresada = txtElectura.getText().toString().trim();
                            if (!lecturaOcrCapturada.isEmpty()) {
                                try {
                                    long ocr    = Long.parseLong(lecturaOcrCapturada);
                                    long manual = Long.parseLong(lecturaIngresada);
                                    if (ocr != manual) {
                                        infoRegistroSalida.settablaRegistroSalida_lecturamodificada1(
                                                String.format("%1$10s", lecturaOcrCapturada));
                                    }
                                } catch (NumberFormatException ignored) {}
                            }

                            // Solo marcar yaTomoFotoOcr=true si vino del OCR (foto)
                            // Lectura digitada manualmente debe pasar por validaciones normales
                            if (!lecturaOcrCapturada.isEmpty()) {
                                yaTomoFotoOcr = true;
                                Log.e("INFO","3 - SETEA yaTomoFotoOcr=true (OCR)");
                            } else {
                                yaTomoFotoOcr = false;
                                Log.e("INFO","3 - SETEA yaTomoFotoOcr=false (manual)");
                            }
                            procesarLectura(1, 0, lecturaIngresada, "XY");
                        }
                        //-----------------------------------------------------------
                        // Log.e("error", "ventana consumo " + ventanaConsumo);
                        if (ventanaConsumo) return;
                    } else
                        mensajeT("Error De Facturacion\n" + "Cliente ya Facturado\n" + "Reimprima o prosiga con el proceso \n" + "de facturacion \n", msgLargo);

                    activaLectorBarras = true;

                    cerrarArchivosFacturacion();
                    txtElectura.setText("");
                    return;
                }
                boolean isDec = false;
                int nroDec = 0;

                for (int i = 0; i < txtElectura.getText().length(); i++) {
                    if (txtElectura.getText().charAt(i) == '.') {
                        isDec = true;
                        txtElectura.setMaxEms(txtElectura.getMaxEms() + 1);
                    }

                    if (isDec && nroDec++ >= 2) {
                        return;
                    }
                }
            }
        }
    }

    // procesos de validacion de las 9 de la mannana con el retorno del falso verdadero
    public boolean yaEnvioFotografiaActual = false;

    private int procesarLectura(int indlectura, int primero, String lecturaact, String causaact) {

        try {
            int DebeTomarFotografia = 0;
            yaEnvioFotografiaActual = false;
            int tomacausa = 0;
            variables.estado = "3";
            variables.consumoactual = 0;
            double t2;
            String horaactual;
            int nroRegistros = 0;
            variables.impresoraAnalitica = "";
            tomarLector();
            valfamilia = infoClienteEntrada.gettablaEntradaClientes_nrofamilias().trim();
            if (valfamilia.equals("0") || valfamilia.equals("")) {
                valfamilia = "1";
            }

            variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";PROCESAR LECTURA-1 ;" + getPhoneDate() + "-" + getPhoneHour() +
                    "-" + lecturaact;
            escribeResumenTiempo(variableResumenLiquidacion);


            tomarFechaSistema(medidorSalida.gettablaContadorSalida_FECHALECTURA(), medidorSalida.gettablaContadorSalida_HORALECTURA());
            medidorSalida.settablaContadorSalida_Fechalectura(variables.amd);
            medidorSalida.settablaContadorSalida_HORALECTURA(variables.hm);

            horaactual = medidorSalida.gettablaContadorSalida_HORALECTURA().substring(0, 4);

            if ((parseStringToInteger(horaactual) < 100) || (parseStringToInteger(horaactual) > 2350)) {
                if (variables.espc == 0) {
                  /*  if (infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("E"))
                        mensajeT("Error Horario \n" + "Hora no permitida para ENTREGAR facturas\n" + "Consulte con el Supervisor\n", msgLargo);
                    else
                        mensajeT("Error Horario Facturacion\n" + "Hora para el proceso no Permitida\n" + "Consulte con el Supervisor\n", msgLargo);
                    return (0);*/
                }
            }

            // variables.diashoy = (int) (variables.calcularNumerodeDias(medidorSalida.gettablaContadorSalida_FECHALECTURA()));

            if (variables.diasdecobro == 0)
                variables.diasdecobro = 1;

            if (indlectura == 1) {

                if (variables.evaluarConServicio(infoMedidorEntrada.gettablaMedidorEntrada_Tipomedidor().trim()) == 0 ||
                        evaluarCargaInstalada(variables.consumoactual) > 0 ||
                        String.format("%1$1s", infoMedidorEntrada.gettablaMedidorEntrada_idcortado().trim()).equals("1"))
                {
                    // Ax. esta parte se cambio de lugar (antes de llamar a este metodo)//Buscar cv001
                    if (indicadorManual == 0) //nuevo para cuando se le da automatico si esta en 1
                    {
                            variables.estado = "3";
                    }
                    /*esto parece ser que se va a procear la liquidacion por ser medidor directo
                    /*if (infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim().equals("") && !infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().trim().equals("99") && !infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().trim().equals("97"))
                        if (infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().trim().equals("26") || infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().trim().equals("21") || infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().trim().equals("28") || infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().trim().equals("31")) {

                            infoRegistroSalida.settablaRegistroSalida_causadenolectura(infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().trim());//[Ma] AQUI ASIGNA EL 28


                        } else if (!String.format("%1$2s", infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().trim()).replace(" ", "0").equals("07")
                                && !String.format("%1$2s", infoRegistroEntrada.getTablaRegistroDeEntrada_critica2().trim()).replace(" ", "0").equals("07")
                                && !String.format("%1$2s", infoRegistroEntrada.getTablaRegistroDeEntrada_critica3().trim()).replace(" ", "0").equals("07")
                                && !String.format("%1$2s", infoRegistroEntrada.getTablaRegistroDeEntrada_critica4().trim()).replace(" ", "0").equals("07")
                                && !String.format("%1$2s", infoRegistroEntrada.getTablaRegistroDeEntrada_critica5().trim()).replace(" ", "0").equals("07")) {
                            infoRegistroSalida.settablaRegistroSalida_causadenolectura("7");
                        }*/

                    variables.lactual = 0;

                    variables.nveces = 1;

                    //validar antes no estaba
                    infoClienteSalida.settablaClienteSalida_INDFACTURACION("L");
                    infoClienteSalida.settablaClienteSalida_HORAIMPRESION(variables.hm);

                } else {
                    //  Log.e("error",indicadorManual+" evaluar "+evaluarLecturaRegistro(primero, variables.nveces, variables.lactual, variables.estado, lecturaact, variables.consumoactual));
                    Log.e("errors", "evaluar registro " + indicadorManual);

                    if (!infoRegistroEntrada.gettablaRegistroDeEntrada_LQDA_CSMO().trim().equals("R") || infoRegistroEntrada.gettablaRegistroDeEntrada_LQDA_CSMO().trim().equals("")) {//estaba en N
                        if (evaluarLecturaRegistro(primero, variables.nveces, variables.lactual, variables.estado, lecturaact, variables.consumoactual) == 0)
                            if (indicadorManual == 0) {// nuevo para cuando se le da automatico si esta en 1
                                Log.e("errors", "evaluar registro1 " + indicadorManual);
                                txtElectura.setText("");
                                txtElectura.setEnabled(true);
                                if (!yaTomoFotoOcr) {
                                    Log.e("errors", "entra al return 0 - yaTomoFotoOcr " +yaTomoFotoOcr);
                                    return 0;
                                }
                            }
                    } else {
                        if (evaluarLecturaAnalitica(primero, variables.nveces, variables.lactual, variables.estado, lecturaact, variables.consumoactual) == 0)
                            if (indicadorManual == 0) {// nuevo para cuando se le da automatico si esta en 1
                                Log.e("errors", "evaluar registro1 " + indicadorManual);
                                txtElectura.setText("");
                                txtElectura.setEnabled(true);
                                if (!yaTomoFotoOcr) {
                                    Log.e("errors", "entra al return 0 - yaTomoFotoOcr " +yaTomoFotoOcr);
                                    return 0;
                                }
                            }

                    }

                    //validar antes no estaba
                    infoClienteSalida.settablaClienteSalida_HORAIMPRESION(variables.hm);
                    infoClienteSalida.settablaClienteSalida_INDFACTURACION("L");
                    //que pasa si quitamos este swiche
                    switch (variables.estado) {
                        case "0":
                            break;
                        case "1":
                            break;
                        case "2":
                        case "6":
                            break;
                        default:
                            break;
                    }
                    variables.nveces++;
                }

                VariablesGlobales.totallecturas++;
                infoRegistroSalida.settablaRegistroSalida_lecturatomada(String.format("%1$10s", parseStringToInteger(lecturaact.trim())).replace(" ", "0"));
                Log.e("error", lecturaact.trim() + " lectura tomada " + infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA());
                infoRegistroSalida.settablaRegistroSalida_leido(variables.estado);
            } else {

                if ((!infoClienteSalida.gettablaClienteSalida_INDFACTURACION().equals(" ")) && (!infoClienteSalida.gettablaClienteSalida_INDFACTURACION().equals("L"))) {

                    if (indicadorManual == 0) {// nuevo para cuando se le da automatico si esta en 1
                        mensajeT("Error en facturacio\nCuenta ya esta registrada\ncomo facturada", msgLargo);
                        // Toast.makeText(getApplicationContext(), "Error en facturacio\nCuenta ya esta registrada\ncomo facturada",                                Toast.LENGTH_LONG).show();
                        return 0;
                    }
                }
/*
                if (variables.evaluarConServicio(infoMedidorEntrada.gettablaMedidorEntrada_Tipomedidor().trim()) == 0 ||
                        evaluarCargaInstalada(variables.consumoactual) > 0 ||
                        String.format("%1$1s", infoMedidorEntrada.gettablaMedidorEntrada_idcortado().trim()).equals("1")) {


                    if (infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim().equals("") && infoRegistroEntrada.gettablaRegistroDeEntrada_tipoDesviacion().equals("N"))
                        if (infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().trim().equals("26") || infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().trim().equals("21")
                                || infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().trim().equals("28") || infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().trim().equals("31")) {

                            infoRegistroSalida.settablaRegistroSalida_causadenolectura(infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().trim()); // [Ma]
                        } else                            if (!String.format("%1$2s", infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().trim()).replace(" ", "0").equals("07")
                                && !String.format("%1$2s", infoRegistroEntrada.getTablaRegistroDeEntrada_critica2().trim()).replace(" ", "0").equals("07")
                                && !String.format("%1$2s", infoRegistroEntrada.getTablaRegistroDeEntrada_critica3().trim()).replace(" ", "0").equals("07")
                                && !String.format("%1$2s", infoRegistroEntrada.getTablaRegistroDeEntrada_critica4().trim()).replace(" ", "0").equals("07")
                                && !String.format("%1$2s", infoRegistroEntrada.getTablaRegistroDeEntrada_critica5().trim()).replace(" ", "0").equals("07")){
                            infoRegistroSalida.settablaRegistroSalida_causadenolectura("7");
                        }


                    variables.estado = "4";
                    ++variables.totalcausasnolectura;
                    infoRegistroSalida.settablaRegistroSalida_lecturatomada("0000000000");
                    Log.e("error", "ind fact 2");
                    infoClienteSalida.settablaClienteSalida_INDFACTURACION("L");
                } else {*/
                    if (causaact != null && causaact.equals("XY")) {
                        if (indicadorManual == 0) { // nuevo para cuando se le da automatico si esta en 1
                            mensajeT("Tiene Procesos de Anomalia debe salir", msgLargo);
                            Log.e("errors", "entra al return 1");
                            return 0;
                        }
                    } else {
                        variables.estado = "5";
                        tomacausa = 1;

                    }
                //}
                variables.nveces++;
                VariablesGlobales.totallecturas++;
                infoRegistroSalida.settablaRegistroSalida_leido(variables.estado);
            }

            variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";PROCESAR LECTURA-1.1 ;" + getPhoneDate() + "-" + getPhoneHour() +
                    "-" + lecturaact;
            escribeResumenTiempo(variableResumenLiquidacion);

            infoRegistroSalida.settablaRegistroSalida_intentos(("" + variables.nveces).trim());
            tomarFechaSistema(medidorSalida.gettablaContadorSalida_FECHALECTURA(), medidorSalida.gettablaContadorSalida_HORALECTURA());
            infoRegistroSalida.settablaRegistroSalida_fechalectura(variables.amd);
            infoRegistroSalida.settablaRegistroSalida_horalectura(variables.hm);


            medidorSalida.settablaContadorSalida_Fechalectura(variables.amd);
            medidorSalida.settablaContadorSalida_HORALECTURA(variables.hm);

            //nuevo para critica 40 validar
            //nuevo validar para que prevalesca la activa
            nroRegistros = parseStringToInteger(infoMedidorEntrada.gettablaMedidorEntrada_Nroregistros().trim().equals("") ? "0" : infoMedidorEntrada.gettablaMedidorEntrada_Nroregistros().trim());

            if (medidorSalida.gettablaContadorSalida_ESTADOCRITICA().trim().equals("") || (nroRegistros > 1 && infoRegistroEntrada.gettablaRegistroDeEntrada_tipoenergia().trim().equals("A"))) {
                medidorSalida.settablaContadorSalida_ESTADOCRITICA(variables.estado);
            } else {
                if (parseStringToInteger(medidorSalida.gettablaContadorSalida_ESTADOCRITICA().trim()) < 6 || parseStringToInteger(medidorSalida.gettablaContadorSalida_ESTADOCRITICA().trim()) > 8) {
                    medidorSalida.settablaContadorSalida_ESTADOCRITICA(variables.estado);
                }
            }

            Log.e("error", variables.medidoranterior.trim() + " medidores info " + infoMedidorEntrada.gettablaMedidorEntrada_NUMero().trim() + "-");
            if (infoMedidorEntrada.gettablaMedidorEntrada_NUMero().trim().equals(variables.medidoranterior.trim())) {
                medidorSalida.settablaContadorSalida_ULTIMOMEDIDORLEIDO(variables.medidoranterior);
                variables.medidoranterior = infoMedidorEntrada.gettablaMedidorEntrada_NUMero();
            }
            t2 = (Double.parseDouble(variables.hm.substring(0, 2)) * 3600 + Double.parseDouble(variables.hm.substring(2, 4)) * 60 + Double.parseDouble(variables.hm.substring(4, 6)));

            if (variables.ultimotiempo == 1) {
                medidorSalida.settablaContadorSalida_TIEMPO(parseStringToInteger((("30") + "").trim()) + "");
            } else if (variables.ultimotiempo > t2) {
                medidorSalida.settablaContadorSalida_TIEMPO(parseStringToInteger(((variables.ultimotiempo - t2) + "").trim()) + "");
            } else {
                medidorSalida.settablaContadorSalida_TIEMPO(parseStringToInteger(((t2 - variables.ultimotiempo) + "").trim()) + "");
            }

            variables.ultimotiempo = t2;

            if (infoClienteSalida.gettablaClienteSalida_CONSUMO1().trim().equals(""))
                infoClienteSalida.settablaClienteSalida_consumo1("0");

            if (infoClienteSalida.gettablaClienteSalida_CONSUMO2().trim().equals(""))
                infoClienteSalida.settablaClienteSalida_consumo2("0");

            if (infoClienteSalida.gettablaClienteSalida_CONSUMO3().trim().equals(""))
                infoClienteSalida.settablaClienteSalida_consumo3("0");

            if (infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO().trim().equals(""))
                infoClienteSalida.settablaClienteSalida_CONSUMOFACTURADO("0");

            if (tomacausa == 0) {

                if (infoRegistroEntrada.gettablaRegistroDeEntrada_Factormultipicacion().trim().length() == 0)
                    infoRegistroEntrada.settablaRegistroDeEntrada_Factormultipicacion("1");

                if (String.format("%1$1s", infoMedidorEntrada.gettablaMedidorEntrada_idcortado().trim()).equals("1") &&
                         infoRegistroEntrada.gettablaRegistroDeEntrada_tipoDesviacion().equals("N")) {//infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().trim().equals("26"))
/* cambiar para ebsa
                    if (String.format("%1$2s", infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().trim()).equals("99") ||
                            String.format("%1$2s", infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().trim()).equals("21") ||
                            String.format("%1$2s", infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().trim()).equals("31") ||
                            String.format("%1$2s", infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().trim()).equals("97")) {

                        if ((String.format("%1$2s", infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().trim()).equals("21") ||
                                String.format("%1$2s", infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().trim()).equals("31"))
                                && Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumoreal()) > 0
                        )

                        {
                            infoRegistroSalida.settablaRegistroSalida_consumotomado("" + parseStringToInteger(infoRegistroEntrada.gettablaRegistroDeEntrada_consumoreal().trim()));
                            infoRegistroSalida.settablaRegistroSalida_consumocalculado(infoRegistroEntrada.gettablaRegistroDeEntrada_consumoreal().trim());
                            variables.consumoactual = Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumoreal());
                        } else {
                            variables.consumoactual = 0;
                            infoRegistroSalida.settablaRegistroSalida_consumocalculado("0");
                            infoRegistroSalida.settablaRegistroSalida_consumotomado("0");
                        }


                    } else {

                        infoRegistroSalida.settablaRegistroSalida_consumotomado(String.format("%1$8s", infoMedidorEntrada.gettablaMedidorEntrada_lecturacorte().trim()).replace(" ", "0"));
                        infoRegistroSalida.settablaRegistroSalida_consumocalculado(String.format("%1$8s", infoMedidorEntrada.gettablaMedidorEntrada_lecturacorte().trim()).replace(" ", "0"));

                        variables.consumoactual = parseStringToDouble(String.format("%1$10s", infoRegistroEntrada.gettablaRegistroDeEntrada_consumoreal().trim()).replace(" ", "0"));
                    }*/
                    variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";PROCESAR LECTURA-1.2 ;" + getPhoneDate() + "-" + getPhoneHour() +
                            "-" + lecturaact;
                    escribeResumenTiempo(variableResumenLiquidacion + " " + " Entra a procesarLiquicadionCortado()");
                    procesarLiquicadionCortado();
                    escribeResumenTiempo(variableResumenLiquidacion + " " + " Fin de procesarLiquicadionCortado()");

                } else {
                   /*mirar para ebsa
                    if ((infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().equals("99") ||
                            infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().equals("21") ||
                            infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().equals("31") ||
                            infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().equals("97")
                    ) && infoRegistroEntrada.gettablaRegistroDeEntrada_tipoDesviacion().equals("N")) {

                        if ((infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().equals("21") ||
                                infoRegistroEntrada.getTablaRegistroDeEntrada_critica1().equals("31"))
                                && Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumoreal()) > 0) {
                            infoRegistroSalida.settablaRegistroSalida_consumotomado(infoRegistroEntrada.gettablaRegistroDeEntrada_consumoreal().trim());
                            infoRegistroSalida.settablaRegistroSalida_consumocalculado(infoRegistroEntrada.gettablaRegistroDeEntrada_consumoreal().trim());
                            variables.consumoactual = Double.parseDouble(String.format("%1$10s", infoRegistroEntrada.gettablaRegistroDeEntrada_consumoreal().trim()));

                        } else {
                            variables.consumoactual = 0;
                            infoRegistroSalida.settablaRegistroSalida_consumocalculado("0");
                            infoRegistroSalida.settablaRegistroSalida_consumotomado("0");
                        }

                    } else {

                        infoRegistroSalida.settablaRegistroSalida_consumotomado("" + (int) (variables.consumoactual * parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Factormultipicacion().trim())));
                    }*/
                    infoRegistroSalida.settablaRegistroSalida_consumotomado("" + (int) (variables.consumoactual * parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Factormultipicacion().trim())));
                    //Log.e("error", "inicia ejecutarsumatoriae 3");
                    variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";PROCESAR LECTURA-1.3 ;" + getPhoneDate() + "-" + getPhoneHour() +
                            "-" + lecturaact;
                    escribeResumenTiempo(variableResumenLiquidacion + " " + " Entra a ejecutarSumatoriaConsumoACobrar()");
                    ejecutarSumatoriaConsumoACobrar();
                    escribeResumenTiempo(variableResumenLiquidacion + " " + " Fin de ejecutarSumatoriaConsumoACobrar()");
                    //Log.e("error", "inicia finaliza sumatoria 3.1");
                }
            }

            infoClienteSalida.settablaClienteSalida_LECTOR(String.format("%1$10s", lector));
            infoClienteSalida.settablaClienteSalida_TERMINAL(String.format("%1$15s", terminal));
            infoClienteSalida.settablaClienteSalida_IMPRESORA(String.format("%1$15s", variables.impresoraAnalitica));
            Log.e("error", "escribe 3");
            variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";PROCESAR LECTURA-1.4 ;" + getPhoneDate() + "-" + getPhoneHour() +
                    "-" + lecturaact;
            escribeResumenTiempo(variableResumenLiquidacion + " " + " Entra a escribirTablasSalida()");
            escribirTablasSalida();
            escribeResumenTiempo(variableResumenLiquidacion + " " + " Fin de escribirTablasSalida()");
            variables.nveces = 0;

            imagenLiquid_3.setImageResource(android.R.color.transparent);

            if (infoClienteSalida.gettablaClienteSalida_INDFACTURACION().trim().equals("")) {
                // infoClienteSalida.settablaClienteSalida_INDFACTURACION("L");
                //  escribirTablasSalida();
            }

            variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";PROCESAR LECTURA-1.5 ;" + getPhoneDate() + "-" + getPhoneHour() +
                    "-" + lecturaact;
            escribeResumenTiempo(variableResumenLiquidacion + " " + " Entra a evaluarDistancia()");
            evaluarDistancia();
            escribeResumenTiempo(variableResumenLiquidacion + " " + " Fin de evaluarDistancia()");

            cod_cuentaUlt_foto = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim();
            mesUlt_foto = infoClienteEntrada.gettablaEntradaClientes_mes().trim();

            if (infoRegistroSalida.gettablaRegistroSalida_LEIDO().equals("2") && (infoRegistroEntrada.gettablaRegistroDeEntrada_Tipomedida().trim().equals("CT"))) {

                if (!infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("E"))

                    if (indicadorManual == 0)

                        if (!yaEnvioFotografiaActual) {
                            yaEnvioFotografiaActual = true;
                            //ejecutarProcesoDeFoto(infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + "_" + infoClienteEntrada.gettablaEntradaClientes_mes().trim(), 1, tipoFotoDigital, 1, "", 1);
                            Log.e("error", "entra en este2");
                            DebeTomarFotografia = 1;
                            Log.e("error", "ingresa aqi E2");
                        }
                // nuevo si entra por aqui y es un consumo bajo
               /* if (parseStringToInteger(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO()) < parseStringToInteger(VariablesGlobales.consumoauditoria) && parseStringToInteger(VariablesGlobales.consumoauditoria) > 0) {

                    if (!infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("E"))
                        requierenovedad = 1;
                }*/

            } else {
                // proceso nuevo de novedades fotos para entrega
                // validar nuevo para las anomalias q piden lectura
                if (!infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim().equals("") ||
                        !infoRegistroSalida.gettablaRegistroSalida_LEIDO().equals("3") ||

                        !infoRegistroSalida.gettablaRegistroSalida_COMENTARIO1().trim().equals("") ||
                        infoRegistroEntrada.gettablaRegistroDeEntrada_Tipomedida().trim().equals("X")

                ) {
                    // nuevo proceso si la factura es superior al valor indicado se reporta para evaluarlo

                    if (indicadorManual == 0) //nuevo para cuando se le da automatico si esta en 1
                    {
                        mensajeNovedad = "";

                        if (MODO.equals("")) {
                            if (!yaEnvioFotografiaActual) {
                                yaEnvioFotografiaActual = true;
                                DebeTomarFotografia = 1;
                                Log.e("error", "entra en este3");
                                //ejecutarProcesoDeFoto(infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + "_" + infoClienteEntrada.gettablaEntradaClientes_mes().trim(), 1, tipoFotoDigital, 1, "", 1);
                            }
                        }
                        if ((!infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim().equals("")
                                && infoRegistroSalida.gettablaRegistroSalida_LEIDO().equals("4"))
                                || infoRegistroSalida.gettablaRegistroSalida_LEIDO().equals("5")) {
                            //criticada por alto al indicar el consumo muy alto
                            if (fuePromediado == 0)
                                mensajeNovedad = "CONSUMO CON ANOMALIA";
                            else
                                mensajeNovedad = "CONSUMO PROMEDIADO X ALTO";
                        } else if (infoRegistroSalida.gettablaRegistroSalida_LEIDO().equals("2"))
                            mensajeNovedad = "CONSUMO CERO";
                        else if (infoRegistroSalida.gettablaRegistroSalida_LEIDO().equals("7") || infoRegistroSalida.gettablaRegistroSalida_LEIDO().equals("8"))
                            mensajeNovedad = "CONSUMO ALTO";
                        else if (infoRegistroSalida.gettablaRegistroSalida_LEIDO().equals("1"))
                            mensajeNovedad = "CONSUMO BAJO";

                        if (MODO.equals("")) {
                            Log.e("error", "requiere novedad 1 " + String.format("%1$2s", infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim()).replace(" ", "0"));
                            if (!String.format("%1$2s", infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim()).replace(" ", "0").equals("07"))
                                adicionarNovedad(2, fuePromediado);
                            Log.e("error", "requiere novedad 1");
                            //se comenta   requierenovedad = 1;
                        }
                    }


                } else
                    // fin e inicio del proceso entrega a certificar
                  /*if (infoClienteEntrada.getTablaEntradaClientes_obliganovedad().trim().equals("S"))
                        if (MODO.equals("")) {
                            Log.e("error", "requiere novedad 2 " + String.format("%1$2s", infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim()).replace(" ", "0"));
                            if (!String.format("%1$2s", infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim()).replace(" ", "0").equals("07"))//&&
                                adicionarNovedad(2, fuePromediado);
                            Log.e("error", "requiere novedad 2");
                            //se comenta requierenovedad = 1;
                        }*/
                if (variables.obligafotos.trim().equals("1") )// || infoClienteEntrada.getTablaEntradaClientes_obliganovedad().trim().equals("S")
                    if (MODO.equals("")) {
                        if (!yaEnvioFotografiaActual) {
                            yaEnvioFotografiaActual = true;
                            Log.e("error", "entra en este4");
                            DebeTomarFotografia = 1;
                            //ejecutarProcesoDeFoto(infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + "_" + infoClienteEntrada.gettablaEntradaClientes_mes().trim(), 1, tipoFotoDigital, 1, "", 1);
                        }
                    }
            }


            if (DebeTomarFotografia == 1) {
                yaTomoFotoOcr = true;
                Log.e("INFO","4 - SETEA EL VALOR DE yaTomoFotoOcr: " + yaTomoFotoOcr);
                ejecutarProcesoDeFoto(infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + "_" + infoClienteEntrada.gettablaEntradaClientes_mes().trim(), 1, tipoFotoDigital, 1, "", 1);
            }
            variableTomarDatosLectura = true;

            //nuevo
            variables.totalregistrosleidos++;

            variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";PROCESAR LECTURA-1.6 ;" + getPhoneDate() + "-" + getPhoneHour() +
                    "-" + lecturaact;
            escribeResumenTiempo(variableResumenLiquidacion + " " + " Entra a tomoLectura()");
            tomoLectura();
            escribeResumenTiempo(variableResumenLiquidacion + " " + " Fin de tomoLectura()");
            //descomentar ULTIMOMEDIDORLEIDO = infoRegistroEntrada.gettablaRegistroDeEntrada_nrocontador();
            // ultimaMARCALEIDO = infoRegistroEntrada.gettablaRegistroDeEntrada_MARCA();


            variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";PROCESAR LECTURA-1.7 ;" + getPhoneDate() + "-" + getPhoneHour() +
                    "-" + lecturaact;
            escribeResumenTiempo(variableResumenLiquidacion + " " + " Entra a limpiarMedidores()");
            limpiarMedidores();
            escribeResumenTiempo(variableResumenLiquidacion + " " + " Fin de limpiarMedidores()");
            procesarLectura2();

        } catch (NumberFormatException ex) {
            variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";ERROR PROCESAR LECTURA-1.8 ;" + getPhoneDate() + "-" + getPhoneHour() +
                    "-" + lecturaact;
            escribeResumenTiempo(variableResumenLiquidacion);
            Log.e("error", "Procesar Lectura " + ex.getMessage());
            utils.Log(logfile, "[MenuDeLiquidacion]procesar lectura( ), " + ex.getMessage());
            mensajeT("Error al Procesar Lectura con anomalias", msgLargo);
            noactforesult = true;

            limpiarMedidores();
            txtElectura.setText("");

        } catch (Exception e) {
            variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";ERROR PROCESAR LECTURA-1.9 ;" + getPhoneDate() + "-" + getPhoneHour() +
                    "-" + lecturaact;
            escribeResumenTiempo(variableResumenLiquidacion);
            Log.e("error", "Procesar Lectura1 " + e.getMessage());
            utils.Log(logfile, "[MenuDeLiquidacion]procesar lectura( ). " + e.getMessage());
            mensajeT("Error al Procesar Lectura con anomalias.", msgLargo);
            noactforesult = true;

            limpiarMedidores();
            txtElectura.setText("");
        }
        return (0);
    }

    private int procesarLectura2() {

        try {
            variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";PROCESAR LECTURA-2_1 ;" + getPhoneDate() + "-" + getPhoneHour();
            escribeResumenTiempo(variableResumenLiquidacion);
            long resultado = 0;
            if (requierenovedad == 1) {
                Log.e("error", "novedad 4");
                variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";PROCESAR LECTURA-2_2 ;" + getPhoneDate() + "-" + getPhoneHour();
                escribeResumenTiempo(variableResumenLiquidacion + " " + " Entra a adicionarNovedad()");
                adicionarNovedad(2, fuePromediado);
                escribeResumenTiempo(variableResumenLiquidacion + " " + " Fin de adicionarNovedad()");
            }
            requierenovedad = 0;

            //nueva variable validar y quita en caso de ser necesario
            validarCausaAR = true;

            variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";PROCESAR LECTURA-2_3 ;" + getPhoneDate() + "-" + getPhoneHour();
            escribeResumenTiempo(variableResumenLiquidacion + " " + " Entra a validarEstadoCliente()");

            resultado = validarEstadoCliente(variables.clienteactual, false, "L");
            variableTomarDatosLectura = false;


            if (resultado == 0 || infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("E")) {

                if (!infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim().equals("")) {
                    //se comenta   fotoynovedad = true;
                }


                valorLeidoFactura = "0";
                VariablesGlobales.totalprediosleidos++;

                File archivoValidador = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/VALIDADORIMPRESION.SDA");

                if (archivoValidador.exists()) {
                    archivoValidador.delete();
                }

                try {
                    RandomAccessFile rFile = new RandomAccessFile(archivoValidador, "rw");
                    rFile.writeBytes(infoClienteEntrada.gettablaEntradaClientes_Cuenta() + ";" + String.format("%1$6s", ("" + VariablesGlobales.registroactual).trim()));
                    rFile.close();
                } catch (IOException ioe) {

                    try {
                        if (archivoValidador.exists())
                            archivoValidador.delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                fuePromediado = 0;

                if (!infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("L") && !infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("N")
                        && !infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("E") && !nombreRuta().equals("9999999")) {

                    if (Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2().trim()) > 0 && Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO1().trim()) > 0) {

                        if (Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO1().trim()) < Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2().trim()) / 2) {
                            infoClienteSalida.settablaClienteSalida_CONSUMOFACTURADO("" + parseStringToInteger("" + variables.ejecutarAjusteUnidades(parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2().trim()))));
                        } else {
                            infoClienteSalida.settablaClienteSalida_CONSUMOFACTURADO("" + parseStringToInteger("" + variables.ejecutarAjusteUnidades(parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO1().trim()) - ((parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2().trim()) / 2)) + parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2().trim()))));
                        }
                    } else {
                        if (!infoRegistroEntrada.gettablaRegistroDeEntrada_Tipomedida().trim().equals("CT")) {
                            infoClienteSalida.settablaClienteSalida_CONSUMOFACTURADO("" + parseStringToInteger(infoClienteSalida.gettablaClienteSalida_CONSUMO2().trim()));
                        } else {
                            // infoClienteSalida.settablaClienteSalida_CONSUMOFACTURADO(infoClienteSalida.gettablaClienteSalida_CONSUMO3().trim());
                        }
                    }

                    //validar nuevo
                    Log.e("errora", "comentario" + infoRegistroSalida.gettablaRegistroSalida_COMENTARIO1().trim() + "-");
                    if (!infoRegistroSalida.gettablaRegistroSalida_COMENTARIO1().trim().equals("")) {
                        infoRegistroSalida.settablaRegistroSalida_causadenolectura(infoRegistroSalida.gettablaRegistroSalida_COMENTARIO1());
                        variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";procesarLectura2;" + getPhoneDate() + "-" + getPhoneHour();
                        escribeResumenTiempo(variableResumenLiquidacion + " " + "procesarLectura2: Posicion Actual|" + VariablesGlobales.registroactual);
                        infoRegistroSalida.escribir_TablaRegistroSalida(VariablesGlobales.registroactual);
                    }

                    infoClienteSalida.escribir_TablaClienteSalida(variables.clienteactual);

                    if (infoClienteEntrada.gettablaEntradaClientes_Clasedeservicio().trim().equals("IC") || infoClienteEntrada.gettablaEntradaClientes_Clasedeservicio().trim().equals("MC")) {
                        leerInformacionUsuario(3);
                        nroContador1 = infoRegistroEntrada.gettablaRegistroDeEntrada_nrocontador();
                        idContador1 = infoRegistroSalida.gettablaRegistroSalida_CONSECUTIVO();
                        lecturaTomada1 = infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA();
                        causadenolectura1 = String.format("%1$2s", infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim());
                        intentos1 = infoRegistroSalida.gettablaRegistroSalida_INTENTOS();
                        lecturaModificada11 = String.format("%1$10s", infoRegistroSalida.gettablaRegistroSalida_LECTURAMODIFICADA1().trim());
                        lecturaModificada21 = String.format("%1$10s", infoRegistroSalida.gettablaRegistroSalida_LECTURAMODIFICADA2().trim());
                        digitos1 = String.format("%1$1s", infoRegistroEntrada.gettablaRegistroDeEntrada_Digitos().trim());
                        criticaPDA1 = infoRegistroSalida.gettablaRegistroSalida_LEIDO();
                        lecturaAnterior1 = infoRegistroEntrada.gettablaRegistroDeEntrada_Lecturaanterior();

                        if (indicadorManual == 0) {
                            mensajeT("ESTA CUENTA ES MACRO MEDIDA NO SE GENERA LIQUIDACION NI FACTURA", msgLargo);
                        }

                        nroContador2 = "";
                        idContador2 = "";
                        lecturaTomada2 = "";
                        causadenolectura2 = "";
                        intentos2 = "";
                        lecturaModificada12 = "";
                        lecturaModificada22 = "";
                        digitos2 = "";
                        criticaPDA2 = "";
                        lecturaAnterior2 = "";

                        nroContador3 = "";
                        idContador3 = "";
                        lecturaTomada3 = "";
                        causadenolectura3 = "";
                        intentos3 = "";
                        lecturaModificada13 = "";
                        lecturaModificada23 = "";
                        digitos3 = "";
                        criticaPDA3 = "";
                        lecturaAnterior3 = "";

                        nroContador4 = "";
                        idContador4 = "";
                        lecturaTomada4 = "";
                        causadenolectura4 = "";
                        intentos4 = "";
                        lecturaModificada14 = "";
                        lecturaModificada24 = "";
                        digitos4 = "";
                        criticaPDA4 = "";
                        lecturaAnterior4 = "";

                    } else {
                        Log.e("error", "entra a tratar de imprimir ");
                        variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";PROCESAR LECTURA-2_4 ;" + getPhoneDate() + "-" + getPhoneHour();
                        escribeResumenTiempo(variableResumenLiquidacion + " " + " Entra a procesoDeLiquidacionEImpresion()");
                        procesoDeLiquidacionEImpresion(0);
                        escribeResumenTiempo(variableResumenLiquidacion + " " + " Fin de procesoDeLiquidacionEImpresion()");
                    }
                }

                Log.e("errors", "entra al return 2");
                if (ventanaConsumo) return (0);
                procesarLectura3();//procesarlectura2

            } else {
                variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";PROCESAR LECTURA-2_5 else;" + getPhoneDate() + "-" + getPhoneHour();
                escribeResumenTiempo(variableResumenLiquidacion + " " + " Entra a visualizarInformacionCliente() registro actual: " + resultado);

                VariablesGlobales.registroactual = (int) (resultado); //[Ma] podria perderse aqui?
                leerInformacionUsuario(1);
                if (VariablesGlobales.totalprediosleidos == infoClienteEntrada.getTotal_TablaEntradaClientes()) {
                    mensajeT("BBB. Proceso de Lecturas concluido", msgLargo);
                    visualizarInformacionCliente(3);
                } else
                    visualizarInformacionCliente(0);
            }


        } catch (NumberFormatException ex) {
            variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";ERROR PROCESAR LECTURA-2_6 ;" + getPhoneDate() + "-" + getPhoneHour();
            escribeResumenTiempo(variableResumenLiquidacion);
            Log.e("error", "procesarlectura2 1 " + ex.getMessage());
            utils.Log(logfile, "[MenuDeLiquidacion]procesar lectura2(), " + ex.getMessage());
            mensajeT("Error Procesar Lectura, con anomalias", msgLargo);
            noactforesult = true;
            ventanaConsumo = false;
            fotoynovedad = false;
        } catch (Exception e) {
            variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";ERROR PROCESAR LECTURA-2.7;" + getPhoneDate() + "-" + getPhoneHour();
            escribeResumenTiempo(variableResumenLiquidacion);
            Log.e("error", "procesarlectura2 2 " + e.getMessage());
            utils.Log(logfile, "[MenuDeLiquidacion]procesar lectura2() " + e.getMessage());
            mensajeT("Error Procesar Lectura, con anomalias", msgLargo);
            noactforesult = true;
            ventanaConsumo = false;
            fotoynovedad = false;
        }
        limpiarMedidores();
        if (indicadorManual == 0) {
            txtElectura.setText("");
        }
        return (0);
    }

    private int procesarLectura3() {

        try {

            variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";PROCESAR LECTURA-3 ;" + getPhoneDate() + "-" + getPhoneHour();
            escribeResumenTiempo(variableResumenLiquidacion + "  Entra a procesarLectura3");

            variables.ultimoregistro = VariablesGlobales.registroactual;
            Log.e("error", " registro.1.0. " + variables.ultimoregistro);

            verificaBarrasCuenta = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim();
            if (indicadorManual == 0) {

                if (VariablesGlobales.habilitadaimpresora != 0) {
                    if (infoClienteEntrada.gettablaEntradaClientes_tipoultimopago().trim().equals("R")) { //Ax: si es rural activar lector de codigo de barras
                        verificaBarras = true;
                        variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";PROCESAR LECTURA-3 ;" + getPhoneDate() + "-" + getPhoneHour();
                        escribeResumenTiempo(variableResumenLiquidacion + " " + " Entra a startBarCodeReader()");
                        startBarCodeReader(false, infoRegistroSalida.gettablaRegistroSalida_NROCONTADOR());
                        escribeResumenTiempo(variableResumenLiquidacion + " " + " Fin de startBarCodeReader()");
                    }
                }
            }

            //Proceso de foto y anomalia que viene desde  procesoDeLiquidacionEImpresion
            if (fotoynovedad) {

                if (indicadorManual == 0) {
                    fotoynovedad = false;
                    Log.e("error", "novedad 5");
                    variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";PROCESAR LECTURA-3 ;" + getPhoneDate() + "-" + getPhoneHour();
                    escribeResumenTiempo(variableResumenLiquidacion + " " + " Entra a adicionarNovedad()");
                    adicionarNovedad(2, 0);
                    escribeResumenTiempo(variableResumenLiquidacion + " " + " Fin de adicionarNovedad()");
                    cod_cuentaUlt_foto = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim();
                    mesUlt_foto = infoClienteEntrada.gettablaEntradaClientes_mes().trim();
                    if (!yaEnvioFotografiaActual) {
                        yaEnvioFotografiaActual = true;
                        yaTomoFotoOcr = true;
                        Log.e("INFO","5 - SETEA EL VALOR DE yaTomoFotoOcr: " + yaTomoFotoOcr);
                        Log.e("error", "entra en este5");

                        variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";PROCESAR LECTURA-3 ;" + getPhoneDate() + "-" + getPhoneHour();
                        escribeResumenTiempo(variableResumenLiquidacion + " " + " Entra a ejecutarProcesoDeFoto()");
                        ejecutarProcesoDeFoto(infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + "_" + infoClienteEntrada.gettablaEntradaClientes_mes().trim(), 1, tipoFotoDigital, 0, "", 1);
                        escribeResumenTiempo(variableResumenLiquidacion + " " + " Fin de ejecutarProcesoDeFoto()");
                    }
                }
            } else {
                if (VariablesGlobales.obligafotos.trim().equals("1") || infoClienteEntrada.gettablaEntradaClientes_tipoultimopago().trim().equals("R")) {
                    if (indicadorManual == 0)
                        Log.e("error", "entra aqui 6.o");
                    //   ejecutarProcesoDeFoto(infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + "_" + infoClienteEntrada.gettablaEntradaClientes_mes().trim(), 1, tipoFotoDigital, 0, "", 1);
                }


            }
            //--------------------------------------------------------------------------
            File archivoValidador = new File(VariablesGlobales.directorioactual + "/DatosDeSalida/ValidadorImpresion.SDA");

            apuntadortarifaCT = 0;
            apuntadortarifareactiva = 0;
            variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";PROCESAR LECTURA-3 ;" + getPhoneDate() + "-" + getPhoneHour();
            escribeResumenTiempo(variableResumenLiquidacion + " " + " Entra a limpiarMedidores()");
            limpiarMedidores();
            escribeResumenTiempo(variableResumenLiquidacion + " " + " Fin de limpiarMedidores()");

            validarEstadoCliente(variables.clienteactual, true, "L");


            String fechalectura = "";
            String altitudReportada = "";

            if (VariablesGlobales.coorTempLatitud.trim().equals("")) {
                VariablesGlobales.coorTempLatitud = "0.0";
            }
            if (VariablesGlobales.coorTempLongitud.trim().equals("")) {
                VariablesGlobales.coorTempLongitud = "0.0";
            }

            if (VariablesGlobales.coorTempLongitud.length() > 16) {
                VariablesGlobales.coorTempLongitud = VariablesGlobales.coorTempLongitud.substring(0, 16);
            }
            if (VariablesGlobales.coorTempLatitud.length() > 16) {
                VariablesGlobales.coorTempLatitud = VariablesGlobales.coorTempLatitud.substring(0, 16);
            }
            if (!infoRegistroSalida.gettablaRegistroSalida_FECHALECTURA().trim().equals("")) {//Axx: revisar esto!
                fechalectura = infoRegistroSalida.gettablaRegistroSalida_FECHALECTURA().substring(6, 8) + "/" +
                        infoRegistroSalida.gettablaRegistroSalida_FECHALECTURA().substring(4, 6) + "/" +
                        infoRegistroSalida.gettablaRegistroSalida_FECHALECTURA().substring(0, 4) + " " +
                        infoRegistroSalida.gettablaRegistroSalida_HORALECTURA().substring(0, 2) + ":" +
                        infoRegistroSalida.gettablaRegistroSalida_HORALECTURA().substring(2, 4) + ":" +
                        infoRegistroSalida.gettablaRegistroSalida_HORALECTURA().substring(4, 6);
            } else {

                Calendar calendar = Calendar.getInstance();

                String dia = calendar.get(Calendar.DAY_OF_MONTH) + "";
                String anno = calendar.get(Calendar.YEAR) + "";
                String mes = (calendar.get(Calendar.MONTH) + 1) + "";
                String hora = calendar.get(Calendar.HOUR) + "";
                String min = calendar.get(Calendar.MINUTE) + "";
                String sec = calendar.get(Calendar.SECOND) + "";

                dia = String.format("%1$2s", dia).replace(" ", "0");
                anno = String.format("%1$4s", anno).replace(" ", "0");
                mes = String.format("%1$2s", mes).replace(" ", "0");
                hora = String.format("%1$2s", hora).replace(" ", "0");
                min = String.format("%1$2s", min).replace(" ", "0");
                sec = String.format("%1$2s", sec).replace(" ", "0");

                fechalectura = dia + "/" + mes + "/" + anno + " " + hora + ":" + min + ":" + sec;
            }

            if (altitudReportadaGPS.trim().length() > 11) {
                altitudReportada = altitudReportadaGPS.trim().substring(0, 11);
            } else {
                altitudReportada = altitudReportadaGPS.trim();
            }

            variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";PROCESAR LECTURA-3 ;" + getPhoneDate() + "-" + getPhoneHour();
            escribeResumenTiempo(variableResumenLiquidacion + " " + " Entra a crea CUENTASGPS()");
            //**** CUENTASGPS.SDA arma_cuentasGps:  [0] año ,[1] mes ,[2] contador [3] medidor  ,[4] cuenta,[5] ciclo
            if (indicadorManual == 0) {
                try {
                    escribeResumenTiempo(variableResumenLiquidacion + " " + " Entra a crea CUENTASGPS()");
                    String texto = String.format("%1$9s", infoClienteSalida.gettablaClienteSalida_CUENTA()) + ";" +
                            String.format("%1$20s", VariablesGlobales.coorTempLatitud) + ";" +
                            String.format("%1$20s", VariablesGlobales.coorTempLongitud) + ";" +
                            String.format("%1$2s", numeroDeSatelitesGPS.trim()) + ";" +
                            variables.amd + ";" +
                            variables.hm + ";" +
                            String.format("%1$-19s", fechalectura) + ";" +
                            String.format("%1$-12s", altitudReportada) + ";" +
                            String.format("%1$-3s", arma_cuentasGps[5]) + ";" +
                            arma_cuentasGps[0] + ";" +
                            String.format("%1$2s", arma_cuentasGps[1].trim()).replace(" ", "0") + ";" +
                            String.format("%1$-5s", infoRegistroSalida.gettablaRegistroSalida_CLIENTE()) + ";" +
                            String.format("%1$-16s", Ciclo + Municipio + Seccion + Division) + ";" +
                            String.format("%1$11s", lector.trim()) + ";" +
                            String.format("%1$12s", infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA()) + ";" +
                            String.format("%1$3s", infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA()) + ";" +
                            String.format("%1$10s", infoRegistroSalida.gettablaRegistroSalida_CONSUMOTOMADO()) + ";" +
                            String.format("%1$30s", infoRegistroSalida.gettablaRegistroSalida_INFORME()) + ";" +
                            String.format("%1$1s", infoRegistroSalida.gettablaRegistroSalida_INTENTOS()) + ";" +
                            String.format("%1$10s", infoRegistroSalida.gettablaRegistroSalida_LECTURAMODIFICADA1()) + ";" +
                            String.format("%1$10s", infoRegistroSalida.gettablaRegistroSalida_LECTURAMODIFICADA2()) + ";" +
                            String.format("%1$1s", infoRegistroSalida.gettablaRegistroSalida_LEIDO()) + ";" +
                            String.format("%1$10s", infoClienteSalida.gettablaClienteSalida_VALORFACTURADO()) + ";" +
                            String.format("%1$10s", infoClienteSalida.gettablaClienteSalida_CONTRIBUCIONENERGIA()) + ";" +
                            String.format("%1$10s", infoClienteSalida.getTablaClienteSalida_CONSUMOFACTURADO()) + ";" +
                            String.format("%1$10s", infoClienteSalida.gettablaClienteSalida_CONSUMO2()) + ";" +
                            String.format("%1$-95s", " ") + ";" +
                            "X" + ";\r\n";

                    File file = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/CUENTASGPS.SDA");

                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    RandomAccessFile writer = new RandomAccessFile(file, "rw");
                    writer.seek(writer.length());
                    writer.writeBytes(texto);
                    writer.close();

                    escribeResumenTiempo(variableResumenLiquidacion + " " + " Fin de crea CUENTASGPS()");
                } catch (Exception ex) {
                    utils.Log(logfile, "[MenuDeLiquidacion]CUENTASGPS() " + ex.getMessage());
                }
            }


            //****
            variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";PROCESAR LECTURA-3 ;" + getPhoneDate() + "-" + getPhoneHour();
            escribeResumenTiempo(variableResumenLiquidacion + " " + " Entra a guardarEnvioGPRSNuevo()");
            guardarEnvioGPRSNuevo(1);
            escribeResumenTiempo(variableResumenLiquidacion + " " + " Fin de guardarEnvioGPRSNuevo()");

            if (indicadorManual == 0) {// proceso especial para mover el archivo de archivo de cobros
                String nombreArchivoCobro = VariablesGlobales.directorioactual + "/DATOSDESALIDA/CO_COBRO.SDA";
                String nombreArchivoCobH = VariablesGlobales.directorioactual + "/DATOSDESALIDA/CO_COBROHILOS.SDA";

                File archivoCobH = new File(nombreArchivoCobH);

                if (procesandoenvioenHilos == 0) {
                    if (archivoCobH.exists())
                        archivoCobH.delete();

                    VariablesGlobales.copyFile(nombreArchivoCobro, nombreArchivoCobH, false);
                }
                variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";PROCESAR LECTURA-3 ;" + getPhoneDate() + "-" + getPhoneHour();
                escribeResumenTiempo(variableResumenLiquidacion + " " + " Entra a envioFacturacionHilos()");
                envioFacturacionHilos();
                escribeResumenTiempo(variableResumenLiquidacion + " " + " Fin de envioFacturacionHilos()");
            }

            limpiarMedidores();

            ULTIMOMEDIDORLEIDO = infoRegistroEntrada.gettablaRegistroDeEntrada_nrocontador();
            ultimaMARCALEIDO = infoRegistroEntrada.gettablaRegistroDeEntrada_MARCA();

            variables.impresora = "   ";
            ultimaCriticaLectura = "  ";
            VariablesGlobales.ultimaNovedad = "0000";
            valor_energia = "0";
            valor_alumbradopublico = "0";

            if (archivoValidador.exists()) { // nuevo proceso de borrado si se imprimio lo ultimo
                archivoValidador.delete();
            }

            if (VariablesGlobales.totalprediosleidos >= 10) {
                try {
                    File archivoLeidos = new File(VariablesGlobales.directorioactual + "/DatosDeEntrada/LeyoMasDe10.TXT");
                    if (!archivoLeidos.exists()) {
                        RandomAccessFile rFile = new RandomAccessFile(archivoLeidos, "rw");
                        rFile.writeBytes(getPhoneDate());
                        rFile.close();
                    }
                } catch (Exception e) {
                }
            }

            //aqui nuevo metodo para validar
            variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";PROCESAR LECTURA-3 ;" + getPhoneDate() + "-" + getPhoneHour();
           /* escribeResumenTiempo(variableResumenLiquidacion + " " + " Entra a escribirReactiva40()");
            escribirReactiva40();
            escribeResumenTiempo(variableResumenLiquidacion + " " + " Fin de escribirReactiva40()");*/
            //------------------------------

            // se incluye un nuevo metodo para certificar los cleintes con postales
            if (VariablesGlobales.tipoDeRuta.equals("E") || VariablesGlobales.tipoDeRuta.equals("F")) {
                if (infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("P") && !nombreRuta().equals("9999999")) {
                    if (indicadorManual == 0) ejecutarCertificacion(0);
                }
            }

            if (indicadorManual == 0) {
                variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";PROCESAR LECTURA-3 ;" + getPhoneDate() + "-" + getPhoneHour();
                escribeResumenTiempo(variableResumenLiquidacion + " " + " Entra a GuardarBackup()");
                GuardarBackup();
                escribeResumenTiempo(variableResumenLiquidacion + " " + " Fin de GuardarBackup()");
            }


            if ((infoRegistroSalida.gettablaRegistroSalida_COMENTARIO2().equals("003")
                    || infoClienteSalida.gettablaClienteSalida_IMPRESORA().trim().equals("CONS. ALTO DV3")) && indicadorManual == 0) {
                entrega_carta = 1;
                variableResumenLiquidacion = "Entra a entregar carta por" + infoRegistroSalida.gettablaRegistroSalida_COMENTARIO2() + "||" + infoClienteSalida.gettablaClienteSalida_IMPRESORA().trim();
                escribeResumenTiempo(variableResumenLiquidacion);

            }
            variableResumenLiquidacion = "----------------------------------------------------------------------------------";
            escribeResumenTiempo(variableResumenLiquidacion);
            /* AQUI ES DONDE CAMBIA DE CLIENTE*/
            if (entrega_carta == 0) {

                if ((variables.direcciondelectura == variables.haciaadelante)) {
                    if (avanzarRegistro() == 0) {
                        retrocedeRegistro();
                    }
                } else {
                    if (retrocedeRegistro() == 0) {
                        avanzarRegistro();
                    }
                }

                variables.numdigitos = 0;

                if (indicadorManual == 0) {
                    if (VariablesGlobales.totalprediosleidos == infoClienteEntrada.getTotal_TablaEntradaClientes()) {
                        mensajeT("AAA. Proceso de Lecturas concluido ", msgLargo);
                        visualizarInformacionCliente(3);
                    } else
                        visualizarInformacionCliente(0);
                }

            } else {
                //msgEntregaCarta("MENSAJE DE ADM","DEBE ENTREGAR CARTA AL USUARIO");
                mensajeAdmCnf(MenuDeLiquidacion.this, "DEBE ENTREGAR CARTA AL USUARIO", "       ANALITICA CARTA                   ", 1);
                imprimirLabelRetencion();
            }
            //----------------------------------------------------------------------------------
            /*if ((variables.direcciondelectura == variables.haciaadelante)) {
                if (avanzarRegistro() == 0) {
                    retrocedeRegistro();
                }
            } else {
                if (retrocedeRegistro() == 0) {
                    avanzarRegistro();
                }
            }

            variables.numdigitos = 0;

            if (indicadorManual == 0) {
                if (VariablesGlobales.totalprediosleidos == infoClienteEntrada.getTotal_TablaEntradaClientes()) {
                    mensajeT("AAA. Proceso de Lecturas concluido ", msgLargo);
                    visualizarInformacionCliente(3);
                } else
                    visualizarInformacionCliente(0);
            }*/



            /*finalizolectura = 1;
            variableResumenLiquidacion = "----------------------------------------------------------------------------------";
            escribeResumenTiempo(variableResumenLiquidacion);
            /*metodo = "AvanzarLecturas";
            TaskHelper.execute(new AsyncCallWS(), metodo);*/
            //AvanzarLecturas();//procesarLectura3

        } catch (NumberFormatException ex) {
            variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";ERROR PROCESAR LECTURA-3 ;" + getPhoneDate() + "-" + getPhoneHour();
            escribeResumenTiempo(variableResumenLiquidacion);
            Log.e("error", "procesarlectura3 1 " + ex.getMessage());
            utils.Log(logfile, "[MenuDeLiquidacion]procesar lectura3(), " + ex.getMessage());
            mensajeT("Error Procesar Lectura, con anomalias", msgLargo);
            noactforesult = true;
        } catch (Exception e) {
            variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";ERROR PROCESAR LECTURA-3.1 ;" + getPhoneDate() + "-" + getPhoneHour();
            escribeResumenTiempo(variableResumenLiquidacion);
            Log.e("error", "procesarlectura3 2 " + e.getMessage());
            utils.Log(logfile, "[MenuDeLiquidacion]procesar lectura3() " + e.getMessage());
            mensajeT("Error Procesar Lectura, con anomalias", msgLargo);
            noactforesult = true;
        }

        /*fotoynovedad = false;
        ventanaConsumo = false;

        limpiarMedidores();
        if (indicadorManual == 0) {
            txtElectura.setText("");
        }*/
        return (0);
    }

    //Ax: Devuelve double de String para evitar tanto try cacth en el codigo
    public double parseStringToDouble(String x) {
        try {
            double y = Double.parseDouble(x.trim().replace(",", "."));
            return y;
        } catch (NumberFormatException e) {
            utils.Log(logfile, "[MenuDeLiquidacion]parseStringToDouble: " + e.getMessage());
            throw new RuntimeException("Error.Double parseo " + x);
        } catch (Exception e) {
            utils.Log(logfile, "[MenuDeLiquidacion]parseStringToDouble: " + e.getMessage());
            throw new RuntimeException("Error.Double parseo " + x);
        }
    }

    //Ax: Devuelve entero de String para evitar tanto try cacth en el codigo (todo, usar en utils ya esta)
    public int parseStringToInteger(String x) {
        try {
            if (x.contains(".")) {
                int y = (int) parseStringToDouble(x);
                return y;
            }
            int y = Integer.parseInt(x.trim());
            return y;
        } catch (NumberFormatException e) {
            Log.e("error", "error parseo1 " + e.getMessage());
            utils.Log(logfile, x + " [MenuDeLiquidacion].parseStringToInteger1: " + e.getMessage());
            throw new RuntimeException("Error.Integer parseo " + x);
        } catch (Exception e) {
            Log.e("error", "error parseo2 " + e.getMessage());
            utils.Log(logfile, "[MenuDeLiquidacion]parseStringToInteger2: " + e.getMessage());
            throw new RuntimeException("Error.Integer parseo " + x);
        }
    }

    private int retrocedeRegistro() {
        int tmpreg = VariablesGlobales.registroactual - 1;
        code.setLength(0);
        yaimprimioRetiene = false;
        yaTomoFotoOcr = false;
        Log.e("INFO","6 - SETEA EL VALOR DE yaTomoFotoOcr: " + yaTomoFotoOcr);
        lecturaOcrCapturada = "";
        fotoOcrObligatoria  = false;
        resetValidacionUbicacion();
        LeyoActiva = 0;
        ContadorFoto = 0;
        variables.direcciondelectura = variables.haciaatras;
        variables.lactual = 0;
        variables.consumoactual = 0;
        variables.nveces = 0;
        imagenLiquid_3.setImageResource(android.R.color.transparent);

        while (tmpreg > 0) {
            if (validarEstadoRegistro(tmpreg, "L") == 0) {
                VariablesGlobales.registroactual = tmpreg;
                leerInformacionUsuario(1);
                txtElectura.setEnabled(true);
                return 1;
            }
            tmpreg--;
        }
        txtElectura.setEnabled(true);
        leerInformacionUsuario(1);
        return 0;
    }

    private int avanzarRegistro() {
        int tmpreg = VariablesGlobales.registroactual + 1;
        code.setLength(0);
        ContadorFoto = 0;
        LeyoActiva = 0;
        yaimprimioRetiene = false;
        yaTomoFotoOcr = false;
        Log.e("INFO","7 - SETEA EL VALOR DE yaTomoFotoOcr: " + yaTomoFotoOcr);
        lecturaOcrCapturada = "";
        fotoOcrObligatoria  = false;
        resetValidacionUbicacion();
        variables.direcciondelectura = variables.haciaadelante;
        variables.lactual = 0;
        variables.consumoactual = 0;
        imagenLiquid_3.setImageResource(android.R.color.transparent);
        variables.nveces = 0;
        variables.cactual = 0;
        while (tmpreg <= infoRegistroEntrada.getTotal_TablaRegistroDeEntrada()) {
            Log.e("error", "avanza registro a " + tmpreg);
            if (validarEstadoRegistro(tmpreg, "L") == 0) {
                VariablesGlobales.registroactual = tmpreg;
                leerInformacionUsuario(1);
                if (indicadorManual == 0) {
                    txtElectura.setEnabled(true);
                }
                return (1);
            }
            ++tmpreg;
        }
        if (indicadorManual == 0) {
            txtElectura.setEnabled(true);
        }
        return (0);
    }

    public void ejecutarCertificacion(int tipo) {

        if (tipo == 0) {
            Bundle bundlei = new Bundle();
            bundlei.putString("ciclo", infoClienteEntrada.gettablaEntradaClientes_Ciclo());
            bundlei.putString("cuenta", infoClienteEntrada.gettablaEntradaClientes_Cuenta());
            bundlei.putString("mes", infoClienteEntrada.gettablaEntradaClientes_mes());
            bundlei.putString("anio", infoClienteEntrada.gettablaEntradaClientes_anio());
            bundlei.putString("nombre", infoClienteEntrada.gettablaEntradaClientes_Nombre());
            bundlei.putString("direccion", infoClienteEntrada.gettablaEntradaClientes_Direccion());
            bundlei.putString("lector", lector);
            bundlei.putString("Certificado", "Certificado");
            bundlei.putString("directorioactual", VariablesGlobales.directorioactual);
            bundlei.putString("cedula", infoClienteEntrada.gettablaEntradaClientes_Nitocedula());

            Intent i = new Intent(this, ModuloAdicionarCertificado.class);
            i.putExtras(bundlei);
            startActivityForResult(i, ADICINCERTIFCAD_REQUEST_CODE);
        }
    }

    private void guardarEnvioGPRS(int i) {

        if (!nroContador1.trim().equals("")) {

            guardarDatosAEnviarNuevo(nroContador1, idContador1, lecturaTomada1, causadenolectura1, nrocontadordb, lecturaModificada11,
                    lecturaModificada21, digitos1, criticaPDA1,
                    lecturaAnterior1, misenvios.total_EnvioGPS);//, infoClienteEntrada.gettablaEntradaClientes_Cuenta(),TipoMedida1);
        }

        if (!nroContador2.trim().equals("")) {

            guardarDatosAEnviarNuevo(nroContador2, idContador2, lecturaTomada2, causadenolectura2, nrocontadordb, lecturaModificada12,
                    lecturaModificada22, digitos2, criticaPDA2,
                    lecturaAnterior2, misenvios.total_EnvioGPS);//, infoClienteEntrada.gettablaEntradaClientes_Cuenta(),TipoMedida2);
        }

        if (!nroContador3.trim().equals("")) {

            guardarDatosAEnviarNuevo(nroContador3, idContador3, lecturaTomada3, causadenolectura3, nrocontadordb, lecturaModificada13,
                    lecturaModificada23, digitos3, criticaPDA3,
                    lecturaAnterior3, misenvios.total_EnvioGPS);//, infoClienteEntrada.gettablaEntradaClientes_Cuenta(),TipoMedida3);
        }
        if (!nroContador4.trim().equals("")) {
            guardarDatosAEnviarNuevo(nroContador4, idContador4, lecturaTomada4, causadenolectura4, nrocontadordb, lecturaModificada14,
                    lecturaModificada24, digitos4, criticaPDA4,
                    lecturaAnterior4, misenvios.total_EnvioGPS);//, infoClienteEntrada.gettablaEntradaClientes_Cuenta(),TipoMedida4);
        }
       /* if (!nroContador5.trim().equals("")) {

            guardarDatosAEnviarNuevo(nroContador5, idContador5, lecturaTomada5, causadenolectura5, intentos5, lecturaModificada15,
                    lecturaModificada25, digitos5, criticaPDA5,
                    lecturaAnterior5, misenvios.total_EnvioGPS, infoClienteEntrada.gettablaEntradaClientes_Cuenta(),TipoMedida5);
        }*/
    }

    private void guardarEnvioGPRSNuevo(int procedimiento) {
        try {
            if (!nroContador1.trim().equals("")) {
                guardarDatosAEnviarNuevo(nroContador1, idContador1, lecturaTomada1, causadenolectura1,
                        nrocontadordb, lecturaModificada11, lecturaModificada21, digitos1,
                        criticaPDA1, lecturaAnterior1, misenvios.total_EnvioGPS);//, infoClienteEntrada.gettablaEntradaClientes_Cuenta(),TipoMedida1);
            }

            if (!nroContador2.trim().equals("")) {

                guardarDatosAEnviarNuevo(nroContador2, idContador2, lecturaTomada2, causadenolectura2,
                        nrocontadordb, lecturaModificada12, lecturaModificada22, digitos2,
                        criticaPDA2, lecturaAnterior2, misenvios.total_EnvioGPS);//, infoClienteEntrada.gettablaEntradaClientes_Cuenta(),TipoMedida2);
            }

            if (!nroContador3.trim().equals("")) {

                guardarDatosAEnviarNuevo(nroContador3, idContador3, lecturaTomada3, causadenolectura3,
                        nrocontadordb, lecturaModificada13, lecturaModificada23, digitos3,
                        criticaPDA3, lecturaAnterior3, misenvios.total_EnvioGPS);//, infoClienteEntrada.gettablaEntradaClientes_Cuenta(),TipoMedida3);
            }
            if (!nroContador4.trim().equals("")) {
                guardarDatosAEnviarNuevo(nroContador4, idContador4, lecturaTomada4, causadenolectura4, nrocontadordb, lecturaModificada14,
                        lecturaModificada24, digitos4, criticaPDA4,
                        lecturaAnterior4, misenvios.total_EnvioGPS);//, infoClienteEntrada.gettablaEntradaClientes_Cuenta(),TipoMedida4);
            }

        } catch (Exception ex) {
            Log.e("ERROR", "[MenuDeLiquidacion] guardarEnvioGPRSNuevo(): " + ex);

        }
    }

    private void guardarDatosAEnviarNuevo(String NroContador, String IdContador, String
            LecturaTomada, String causadenolectura, String Intentos, String LecturaModificada1, String
                                                  LecturaModificada2,
                                          String Digitos, String criticaPDA, String lecturaanterior, int procedimiento)//, String cuenta, String tipomedida)
    {
        try {
            variables.datodebusqueda = "";
            tomarLector();
            Log.e("INFO", "guardarDatosAEnviarNuevo | infoClienteSalida.lector: " + infoClienteSalida.gettablaClienteSalida_LECTOR().trim());
            if (!infoClienteSalida.gettablaClienteSalida_LECTOR().trim().equals("")) {

                if (infoRegistroSalida.gettablaRegistroSalida_HORALECTURA().trim().equals("") || infoRegistroSalida.gettablaRegistroSalida_HORALECTURA().trim().equals("000000")) {
                    Calendar calen = Calendar.getInstance();
                    String hora = calen.get(Calendar.HOUR) + "";
                    String min = calen.get(Calendar.MINUTE) + "";
                    String sec = calen.get(Calendar.SECOND) + "";
                    hora = String.format("%1$2s", hora).replace(" ", "0");
                    min = String.format("%1$2s", min).replace(" ", "0");
                    sec = String.format("%1$2s", sec).replace(" ", "0");
                    infoRegistroSalida.settablaRegistroSalida_horalectura(hora + min + sec);
                    variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";guardarDatosAEnviarNuevo;" + getPhoneDate() + "-" + getPhoneHour();
                    escribeResumenTiempo(variableResumenLiquidacion + " " + "guardarDatosAEnviarNuevo: Posicion Actual|" + VariablesGlobales.registroactual);
                    infoRegistroSalida.escribir_TablaRegistroSalida(VariablesGlobales.registroactual);
                }

                misenvios.setEnvioGPS_ciclo(infoClienteEntrada.gettablaEntradaClientes_Ciclo());
                misenvios.setEnvioGPS_mununicipio(infoClienteEntrada.gettablaEntradaClientes_Municipio());
                misenvios.setEnvioGPS_seccion(infoClienteEntrada.gettablaEntradaClientes_sector());
                misenvios.setEnvioGPS_departamento("15");
                misenvios.setEnvioGPS_anno(infoClienteEntrada.gettablaEntradaClientes_anio());
                misenvios.setEnvioGPS_mes(infoClienteEntrada.gettablaEntradaClientes_mes());
                misenvios.setEnvioGPS_cuenta(infoClienteEntrada.gettablaEntradaClientes_Cuenta().substring(0, 7));
                misenvios.setEnvioGPS_NroContador(NroContador);

                if (IdContador.trim().equals("")) {
                    misenvios.setEnvioGPS_idcontador("0");
                } else {
                    misenvios.setEnvioGPS_idcontador(IdContador);// InfoRegistroEntrada.EREGISTCONCECUTIVO;
                }

                misenvios.setEnvioGPS_lecturatomada(LecturaTomada); //InfoRegistroSalida.SREGISTLECTURATOMADA;
                misenvios.setEnvioGPS_causadenolectura(causadenolectura);//InfoRegistroSalida.SREGISTCAUSADENOLECTURA.Trim().PadLeft(2, '0');
                if (infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim().equals("")) {
                    misenvios.setEnvioGPS_descanomalia("LECTURA EXITOSA");



                } else {
                    anomaliaDeLectura.setEncontro_AnomaliaDeNoLectura(0);
                    //nuevo para cambiar lacausa 40 por la 18solo para registrarlo

                    String temp40 = infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO().trim();
                    temp40 = String.format("%1$10s", temp40).replace(" ", "0");

//SE HACE EL AJUSTE PARA LAS CUENTAS NO NORMALIZADAS QUE SE RETIENEN CON CONSUMO ALTO MAYOR A 1000KV NOSE FACTURO PERO SE IDENTIFICA
                   /* if (causadenolectura == "40" && parseStringToInteger(temp40) > 1000) {
                        misenvios.setEnvioGPS_causadenolectura("18");
                    }*/
                    if (anomaliaDeLectura.abrir_AnomaliaDeNoLectura(anomaliaDeLectura.getArchivo_AnomaliaDeNoLectura())) {
                        anomaliaDeLectura.Buscarbinariocausanl(infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim());
                        anomaliaDeLectura.Cerrar_AnomaliaDeNoLectura();
                    }

                    if (anomaliaDeLectura.getEncontro_AnomaliaDeNoLectura() > 0) {
                        String temp = anomaliaDeLectura.getanomaliaDeNoLectura_DESCRIPCION().trim();
                        temp = String.format("%1$-50s", temp).substring(0, 30);
                        misenvios.setEnvioGPS_descanomalia(acents(temp));
                    } else
                        misenvios.setEnvioGPS_descanomalia("Causa Sin Descripcion");
                }
                // Combinar sufijo de ubicación + sufijo OCR (máx 30 chars total)
                String sufijUbicacion = obtenerSufijoUbicacion();
                String sufijOcr       = obtenerSufijoOcr();
                String sufijo = sufijUbicacion.isEmpty() ? sufijOcr
                        : sufijOcr.isEmpty()       ? sufijUbicacion
                        : sufijUbicacion + " " + sufijOcr;

                if (!sufijo.isEmpty()) {
                    String descActual = misenvios.getEnvioGPS_DESCANOMALIA().trim();
                    int espacioDesc   = 30 - sufijo.length() - 1;
                    if (espacioDesc > 0 && descActual.length() > espacioDesc)
                        descActual = descActual.substring(0, espacioDesc);
                    String descFinal = espacioDesc > 0
                            ? descActual + " " + sufijo
                            : sufijo;
                    misenvios.setEnvioGPS_descanomalia(
                            String.format("%1$-30s", descFinal).substring(0, 30));
                }

                String temp1 = infoRegistroSalida.gettablaRegistroSalida_COMENTARIO1().trim();
                temp1 = String.format("%1$2s", temp1).replace(" ", "0");

                misenvios.setEnvioGPS_comentario(temp1);

                misenvios.setEnvioGPS_desccomentario("Version 26.01.14.1");//"se cambia el24.11.29.1 29 del 22 Con Comentario";variables.datodebusqueda
                misenvios.setEnvioGPS_MARCALEIDO("Vers260114.1");//ultimaMARCALEIDO.trim()

                if (!infoRegistroSalida.gettablaRegistroSalida_FECHALECTURA().trim().equals("")) {//Axx: revisar esto!
                    misenvios.setEnvioGPS_fechayhoralectura(
                            infoRegistroSalida.gettablaRegistroSalida_FECHALECTURA().substring(6, 8) + "/" +
                                    infoRegistroSalida.gettablaRegistroSalida_FECHALECTURA().substring(4, 6) + "/" +
                                    infoRegistroSalida.gettablaRegistroSalida_FECHALECTURA().substring(0, 4) + " " +
                                    infoRegistroSalida.gettablaRegistroSalida_HORALECTURA().substring(0, 2) + ":" +
                                    infoRegistroSalida.gettablaRegistroSalida_HORALECTURA().substring(2, 4) + ":" +
                                    infoRegistroSalida.gettablaRegistroSalida_HORALECTURA().substring(4, 6));
                } else {

                    Calendar calendar = Calendar.getInstance();

                    String dia = calendar.get(Calendar.DAY_OF_MONTH) + "";
                    String anno = calendar.get(Calendar.YEAR) + "";
                    String mes = (calendar.get(Calendar.MONTH) + 1) + "";
                    String hora = calendar.get(Calendar.HOUR) + "";
                    String min = calendar.get(Calendar.MINUTE) + "";
                    String sec = calendar.get(Calendar.SECOND) + "";

                    dia = String.format("%1$2s", dia).replace(" ", "0");
                    anno = String.format("%1$4s", anno).replace(" ", "0");
                    mes = String.format("%1$2s", mes).replace(" ", "0");
                    hora = String.format("%1$2s", hora).replace(" ", "0");
                    min = String.format("%1$2s", min).replace(" ", "0");
                    sec = String.format("%1$2s", sec).replace(" ", "0");

                    misenvios.setEnvioGPS_fechayhoralectura(dia + "/" + mes + "/" + anno + " " + hora + ":" + min + ":" + sec);
                }

                if (infoClienteSalida.gettablaClienteSalida_LECTOR().trim().equals("")) {
                    tomarLector();
                    infoClienteSalida.settablaClienteSalida_LECTOR(String.format("%1$10s", lector));
                    misenvios.setEnvioGPS_CodLector(variables.getGlobaloperario());
                } else {
                    misenvios.setEnvioGPS_CodLector(infoClienteSalida.gettablaClienteSalida_LECTOR());
                }

                misenvios.setEnvioGPS_primermedidor(infoClienteSalida.gettablaClienteSalida_PRIMERMEDIDOR());
                misenvios.setEnvioGPS_HoraImpresion(medidorSalida.gettablaContadorSalida_TIEMPO());//Ma: Toca preguntarle a victor si es el de clienteSalida.HoraImpresion porque eso quemado no deberia
                misenvios.setEnvioGPS_NombreArchivo(rutaCargadaPDA);


                if (VariablesGlobales.coorTempLatitud.trim().equals("")) {
                    VariablesGlobales.coorTempLatitud = "0.0";
                }
                if (VariablesGlobales.coorTempLongitud.trim().equals("")) {
                    VariablesGlobales.coorTempLongitud = "0.0";
                }

                if (VariablesGlobales.coorTempLongitud.length() > 16) {
                    VariablesGlobales.coorTempLongitud = VariablesGlobales.coorTempLongitud.substring(0, 16);
                }
                if (VariablesGlobales.coorTempLatitud.length() > 16) {
                    VariablesGlobales.coorTempLatitud = VariablesGlobales.coorTempLatitud.substring(0, 16);
                }
                if (!coordenadaLatitud.equals("") && !coordenadaLongitud.equals("")) {
                    misenvios.setEnvioGPS_Latitud(coordenadaLatitud.replace(",", "."));// txtX.Text;
                    misenvios.setEnvioGPS_Longitud(coordenadaLongitud.replace(",", "."));// txtY.Text;
                } else {
                    misenvios.setEnvioGPS_Latitud(VariablesGlobales.coorTempLatitud.replace(",", "."));// txtX.Text;
                    misenvios.setEnvioGPS_Longitud(VariablesGlobales.coorTempLongitud.replace(",", "."));// txtY.Text;
                }
                coordenadaLongitud = "";
                coordenadaLatitud = "";

                misenvios.setEnvioGPS_NroSatelites(numeroDeSatelitesGPS);

                misenvios.setEnvioGPS_Distancia(infoClienteSalida.gettablaClienteSalida_DISTANCIACALCULADA());//[enerca]  misenvios.setEnvioGPS_Distancia(infoClienteSalida.gettablaClienteSalida_DISTANCIACALCULADA());
                misenvios.setEnvioGPS_FechaHoraSatelite(fechayHoraReportadaGPS);
                misenvios.setEnvioGPS_AltitudSatelite("" + parseStringToInteger(altitudReportadaGPS));
                misenvios.setEnvioGPS_Terminal(terminal);

                if (VariablesGlobales.ultimaNovedad.equals("0000")) {
                    misenvios.setEnvioGPS_IndicadorNovedad("N");// "Ind Nov";
                } else {
                    misenvios.setEnvioGPS_IndicadorNovedad("S");// "Ind Nov";
                }

                if (!variables.impresora.substring(0, 2).equals("LE")) {
                    misenvios.setEnvioGPS_Indcodbarras("N"); //"Ind_CodBa";
                } else {
                    misenvios.setEnvioGPS_Indcodbarras("S"); //"Ind_CodBa";
                }

                misenvios.setEnvioGPS_Intentos(infoRegistroSalida.gettablaRegistroSalida_INTENTOS());
                misenvios.setEnvioGPS_criticapda(criticaPDA);// InfoRegistroSalida.SREGISTLEIDO;
                misenvios.setEnvioGPS_ValorFacturado(infoClienteSalida.gettablaClienteSalida_VALORFACTURADO());

                String temp2 = infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO().trim();
                temp2 = String.format("%1$10s", temp2).replace(" ", "0");
                misenvios.setEnvioGPS_ConsumoFacturado("" + parseStringToInteger(temp2));

                misenvios.setEnvioGPS_SubContribucion("" + parseStringToInteger(infoClienteSalida.gettablaClienteSalida_CONTRIBUCIONENERGIA()));

                if (Double.parseDouble(infoClienteEntrada.gettablaEntradaClientes_Nrodemedidores()) == 1 || (Double.parseDouble(infoClienteSalida.gettablaClienteSalida_VALOR2()) == 0 && Double.parseDouble(infoClienteSalida.gettablaClienteSalida_VALOR3()) == 0)) {

                    String temp3 = infoClienteSalida.gettablaClienteSalida_CONSUMO2().trim();
                    temp3 = String.format("%1$10s", parseStringToInteger(temp3)).replace(" ", "0");
                    misenvios.setEnvioGPS_Consumo1(temp3);

                    temp3 = infoClienteSalida.gettablaClienteSalida_CONSUMO1().trim();
                    temp3 = String.format("%1$10s", parseStringToInteger(temp3)).replace(" ", "0");
                    misenvios.setEnvioGPS_Consumo2(temp3);

                    temp3 = infoClienteSalida.gettablaClienteSalida_CONSUMO3().trim();
                    temp3 = String.format("%1$10s", parseStringToInteger(temp3)).replace(" ", "0");
                    misenvios.setEnvioGPS_Consumo3(temp3);
                } else {

                    String temp3 = infoClienteSalida.gettablaClienteSalida_VALOR1().trim();
                    temp3 = String.format("%1$10s", parseStringToInteger(temp3)).replace(" ", "0");
                    misenvios.setEnvioGPS_Consumo1(temp3);

                    temp3 = infoClienteSalida.gettablaClienteSalida_VALOR2().trim();
                    temp3 = String.format("%1$10s", parseStringToInteger(temp3)).replace(" ", "0");
                    misenvios.setEnvioGPS_Consumo2(temp3);

                    temp3 = infoClienteSalida.gettablaClienteSalida_VALOR3().trim();
                    temp3 = String.format("%1$10s", parseStringToInteger(temp3)).replace(" ", "0");
                    misenvios.setEnvioGPS_Consumo3(temp3);
                }
                //faltaria un consumo tres que es el de CT contarores testigos
                misenvios.setEnvioGPS_NroConceptos(infoClienteEntrada.gettablaEntradaClientes_nrocobros());

                if (infoClienteSalida.gettablaClienteSalida_INDFACTURACION().trim().equals(""))
                    misenvios.setEnvioGPS_IndFacturacion("N");
                else
                    misenvios.setEnvioGPS_IndFacturacion(infoClienteSalida.gettablaClienteSalida_INDFACTURACION());

                String temp4 = infoClienteEntrada.gettablaEntradaClientes_Nrofactura().trim();
                temp4 = String.format("%1$15s", temp4).replace(" ", "0");
                misenvios.setEnvioGPS_NroFactura(temp4);

                temp4 = infoClienteSalida.gettablaClienteSalida_FECHAVENCE().trim();
                temp4 = String.format("%1$8s", temp4).replace(" ", "0");

                if (temp4.equals("00000000")) {

                    Calendar calenda = Calendar.getInstance();

                    String dia = calenda.get(Calendar.DAY_OF_MONTH) + "";
                    String anno = calenda.get(Calendar.YEAR) + "";
                    String mes = (calenda.get(Calendar.MONTH) + 1) + "";

                    dia = String.format("%1$2s", dia).replace(" ", "0");
                    anno = String.format("%1$4s", anno).replace(" ", "0");
                    mes = String.format("%1$2s", mes).replace(" ", "0");

                    misenvios.setEnvioGPS_FechaVence(dia + "/" + mes + "/" + anno);

                    misenvios.setEnvioGPS_FechaCorte(dia + "/" + mes + "/" + anno);
                } else {

                    String temp5 = infoClienteSalida.gettablaClienteSalida_FECHAVENCE().trim();
                    temp5 = String.format("%1$8s", temp5).replace(" ", "0");
                    misenvios.setEnvioGPS_FechaVence(temp5);

                    temp5 = infoClienteSalida.gettablaClienteSalida_FECHACORTE().trim();
                    temp5 = String.format("%1$8s", temp5).replace(" ", "0");
                    misenvios.setEnvioGPS_FechaCorte(temp5);
                }

                if (lecturaModificada11.trim().length() > 9)
                    lecturaModificada11 = lecturaModificada11.substring(4, 9);
                misenvios.setEnvioGPS_LecturaModificada1(lecturaModificada11);// InfoRegistroSalida.SREGISTLECTURAMODIFICADA1.Trim().PadLeft(10, '0');
                if (lecturaModificada21.trim().length() > 9)
                    lecturaModificada21 = lecturaModificada21.substring(4, 9);
                misenvios.setEnvioGPS_LecturaModificada2(lecturaModificada21);// InfoRegistroSalida.SREGISTLECTURAMODIFICADA2.Trim().PadLeft(10, '0');

                misenvios.setEnvioGPS_Digitos(Digitos);// InfoRegistroEntrada.EREGISTNROENTEROS.Trim().PadLeft(1, '0');
                misenvios.setEnvioGPS_digitochequeo(infoRegistroSalida.gettablaRegistroSalida_CUENTA().substring(7, 10));
                misenvios.setEnvioGPS_Procesado("N");

                misenvios.setEnvioGPS_NumeroConceptos(infoClienteEntrada.gettablaEntradaClientes_nrocobros());
                misenvios.setEnvioGPS_PrimerConcepto(infoClienteEntrada.gettablaEntradaClientes_primercobro());

                misenvios.setEnvioGPS_CritiaSIEC(ultimaCriticaLectura);//ultimaCriticaLectura
                misenvios.setEnvioGPS_CodNovedad(variables.ultimaNovedad);

                String temp6 = lecturaanterior.replace(",", ".").trim();
                temp6 = String.format("%1$10s", parseStringToInteger(temp6)).replace(" ", "0");
                misenvios.setEnvioGPS_LecturaAnterior(temp6);

                misenvios.setEnvioGPS_EstadoEnvio("N");

                //NUEVOS DATOS PARA ENVIAR AL SERVIDOR
                //NUEVOS DATOS EN LA COLECCION
                misenvios.setEnvioGPS_MARCACONTADOR(infoRegistroSalida.gettablaRegistroSalida_MARCA());
                misenvios.setEnvioGPS_ULTIMOMEDIDORLEIDO(ULTIMOMEDIDORLEIDO.trim());

                misenvios.setEnvioGPS_OBSERVACION1(infoRegistroSalida.gettablaRegistroSalida_COMENTARIO2().trim());
                misenvios.setEnvioGPS_CLASESERVICIO(infoClienteEntrada.gettablaEntradaClientes_claseagrupacion().trim());

                misenvios.setEnvioGPS_NMEDIDORES(infoClienteEntrada.gettablaEntradaClientes_Nrodemedidores());
                misenvios.setEnvioGPS_NREGISTRADORES(infoMedidorEntrada.gettablaMedidorEntrada_Nroregistros());

                misenvios.setEnvioGPS_IDREGISTRO(infoClienteEntrada.gettablaEntradaClientes_Lugarultimopago().trim());
                Log.e("INFO","guardarDatosAEnviarNuevo | nrocontadordb: " + nrocontadordb);
                Log.e("INFO","guardarDatosAEnviarNuevo | InfoENtradanrocontadordb" + infoRegistroEntrada.gettablaRegistroDeEntrada_Digitos());
                misenvios.setEnvioGPS_NROCONTADORDB(Intentos);
                //nuevo campo
                misenvios.setEnvioGPS_ValorFacturadoASEO("" + (int) acobrarAseo);
                misenvios.setEnvioGPS_CRNL("\r\n");

                misenvios.archivo_EnvioGPS = variables.directorioactual + "/DATOSDESALIDA/ENVIOSGPRS.SDA";

                File fileeg = new File(misenvios.archivo_EnvioGPS);

                if (!fileeg.exists()) {
                    try {
                        fileeg.createNewFile();
                    } catch (Exception e) {
                        Log.e("ERROR", "[MenuDeLiquidacion]guardarDatosAEnviarNuevo() | no crea gprs " + e.getMessage());
                        utils.Log(logfile, "[MenuDeLiquidacion]guardarDatosAEnviarNuevo(), No se pudo crear:" + fileeg.getAbsolutePath() + " | Error -> " + e.getMessage());
                        e.printStackTrace();
                    }
                }


                //crear el protocolo para este otro recurso
                try {

                /* Ma:Para complementar indicativos para armar envio ya que el nro de impresiones no se esta usando
                infoClienteSalida.settablaClienteSalida_NROIMPRESIONES("1");
                escribirTablasSalida();*/


                    if (misenvios.abrir_EnvioGPS(misenvios.archivo_EnvioGPS)) {

                        misenvios.escribir_EnvioGPS(misenvios.total_EnvioGPS + 1);
                        misenvios.total_EnvioGPS++;

                        misenvios.Cerrar_EnvioGPS();
                        //crea la copia de seguridad del archivo de lecturas
                        misenvios.archivo_EnvioGPS = VariablesGlobales.directorioBackUp + "CopiaRespaldoLecturas.sda";

                        File filegps = new File(misenvios.archivo_EnvioGPS);

                        if (!filegps.exists()) {
                            try {
//                            filegps.mkdirs();
                                filegps.createNewFile();
                            } catch (Exception ep) {

                                if (ValidandoNoEnv == 0) {
                                    mensajeT("No se Pudo crear CopiaRespaldoLecturas\n " + ep.getMessage(), msgCorto);
                                }
                                // Toast.makeText(getApplicationContext(), "No se Pudo crear CopiaRespaldoLecturas\n " + ep.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                        if (misenvios.abrir_EnvioGPS(misenvios.archivo_EnvioGPS)) {
                            misenvios.escribir_EnvioGPS(misenvios.total_EnvioGPS + 1);
                            misenvios.Cerrar_EnvioGPS();
                        }
                        //crear el protocolo para este otro recurso

                    } else {
                        utils.Log(logfile, "[MenuDeLiquidacion]guardarDatosAEnviarNuevo();No se pudo Leer:" + fileeg.getAbsolutePath());

                        if (ValidandoNoEnv == 0) {
                            mensajeT(("No se pudo registrar en el archivo de datos " + misenvios.archivo_EnvioGPS), msgMedio);
                        }
                        //Toast.makeText(getApplicationContext(), ("No se pudo registrar en el archivo de datos " + misenvios.archivo_EnvioGPS), Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    utils.Log(logfile, "[MenuDeLiquidacion]guardarDatosAEnviarNuevo();" + e.getMessage());
                    if (ValidandoNoEnv == 0) {
                        mensajeT(("Problema Abriendo\n Guardando respaldo envios"), msgMedio);
                    }
                    //Toast.makeText(getApplicationContext(), ("Problema Abriendo\n Guardando respaldo envios"), Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception ex) {
            utils.Log(logfile, "[guardarDatosAEnviarNuevo()] Error: " + ex);
        }
    }

    /**
     * Devuelve el código de ubicación a concatenar en descanomalia.
     * Vacío si la lectura fue dentro del rango normal (no requiere trazabilidad).
     * Máximo 6 chars para dejar espacio a la descripción original.
     */
    private String obtenerSufijoUbicacion() {
        // Si no es obligatorio no se registra nada
        if (!VariablesGlobales.distanciagps.trim().equals("1")) return "";
        if (estadoUbicacion == UBI_SIN_GPS) {
            return "SNGPS";
        } else if (estadoUbicacion == UBI_SIN_COORDENADAS) {
            return "SNCOR";
        } else if (estadoUbicacion == UBI_FUERA_RANGO) {
            if (intentosLecturaFueraRango >= MAX_INTENTOS_FUERA_RANGO) {
                return "SNFUR";
            }
        }
        return "";
    }

    /**
     * Sufijo de trazabilidad OCR para descanomalia.
     * MODLEC → el operario modificó la lectura respecto a lo que detectó el OCR.
     * Vacío si no hubo OCR o si la lectura no fue modificada.
     */
    private String obtenerSufijoOcr() {
        if (lecturaOcrCapturada.isEmpty()) return "";
        String lecturaTomada = infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA().trim();
        // Comparar sin ceros a la izquierda
        try {
            long ocr     = Long.parseLong(lecturaOcrCapturada.trim());
            long tomada  = Long.parseLong(lecturaTomada.trim());
            if (ocr != tomada) return "MODLEC";
        } catch (NumberFormatException e) {
            if (!lecturaOcrCapturada.trim().equals(lecturaTomada)) return "MODLEC";
        }
        return "";
    }

    private String acents(String mns) {
        mns = mns.replace("�", ".").replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u").replace("ñ", "n").replace("Ñ", "N").replace("Á", "A").replace("É", "E").replace("Í", "I").replace("Ó", "O").replace("Ú", "U").replace("ü", "u").replace("Ü", "U");
        return mns;
    }

    private String nombreRuta() {

        File archivo1 = new File(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/NOMBRE");
        String archivocargado = "";

        if (archivo1.exists()) {
            try {

                RandomAccessFile rFile = new RandomAccessFile(archivo1, "r");
                byte[] byteArray;
                int fileSize = (int) rFile.length();
                byteArray = new byte[fileSize];
                rFile.readFully(byteArray, 0, fileSize);
                archivocargado = new String(byteArray);

                if (archivocargado.length() >= 12)
                    archivocargado = archivocargado.substring(5, 12);//esta en 7 pero debe se 12

                rFile.close();

            } catch (FileNotFoundException e) {

                e.printStackTrace();

            } catch (IOException e) {

                e.printStackTrace();
            }
        }
        return archivocargado;
    }

    private void setRespuesta(String string) {
        respuesta = string;
    }

    private void adicionarNovedad(int tipo, int fuepromediado) {

        variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";NOVEDAD ;" + getPhoneDate() + "-" + getPhoneHour();
        escribeResumenTiempo(variableResumenLiquidacion);

        String causalCuentaNueva;
        String causalCuenta;

        String cuentanueva = infoRegistroSalida.gettablaRegistroSalida_CUENTA();

        if (tipo == 1) { //Ax: solo el menu envia 1
            causalCuenta = "77";
            causalCuentaNueva = "Cuenta Nueva...............";

        } else {
            causalCuenta = "88";
            causalCuentaNueva = "Novedad incluida al cliente";
        }

        // SI EL SISTEMA ES UNA NOVEDAD DE ENTREGA TOMAR EL CODIGO DE ESTA
        if (infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("E"))
            mensajeNovedad = "NOVEDAD ENTREGA";

        VariablesGlobales.ultimaNovedad = "0000";
        String archivo = "NOVEDADES.SDA";
        VariablesGlobales.ultimaNovedad = "0000";
        try {
            Bundle bundle = new Bundle();
            bundle.putString("observacion", causalCuenta + " " + causalCuentaNueva);
            bundle.putString("docnovedad", cuentanueva);
            bundle.putString("codigo", cuentanueva);
            Log.e("error", "nombre " + infoClienteEntrada.gettablaEntradaClientes_Nombre().trim());
            bundle.putString("nombre", infoClienteEntrada.gettablaEntradaClientes_Nombre().trim());//antes .substring(0, 47)
            bundle.putString("dir1", infoClienteEntrada.gettablaEntradaClientes_Direccion());
            bundle.putString("barrio", "CAQUETA");
            bundle.putString("ruta", infoClienteEntrada.gettablaEntradaClientes_Ruta());
            bundle.putString("medidor", infoRegistroSalida.gettablaRegistroSalida_NROCONTADOR());
            bundle.putString("lectura", infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA());
            bundle.putString("lector", lector.trim());
            bundle.putString("ciclo", infoClienteEntrada.gettablaEntradaClientes_Ciclo());
            bundle.putString("periodo", infoClienteEntrada.gettablaEntradaClientes_mes());
            bundle.putString("anno", infoClienteEntrada.gettablaEntradaClientes_anio());
            bundle.putString("path", variables.directorioactual);
            bundle.putString("archivo", archivo);
            bundle.putString("critica", mensajeNovedad);
            bundle.putString("ModeloCiclo", "F");
            bundle.putString("serial", serialPDA);
            bundle.putString("valorfacturado", infoClienteSalida.gettablaClienteSalida_VALORFACTURADO());
            //bundle.putString("consumopredio",infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediocliente());
            bundle.putString("consumofacturado", infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO());
            bundle.putString("claseservicio", infoClienteEntrada.gettablaEntradaClientes_Clasedeservicio());
            bundle.putInt("fuepromediado", fuepromediado);

            Intent i = new Intent(this, ModuloCaptNovLect.class);
            i.putExtras(bundle);
            startActivityForResult(i, ADICIONARNOVDAD_REQUEST_CODE);

        } catch (Exception e) {
            mensajeT("Error al iniciar actividad de Modulo Capt Nov Lect" + e.getMessage(), msgLargo);
        }
        //Ax: el resto que habia aqui esta en activityforresult
    }


    private void cambiarValoresimpresion() {
        String archivoFormato = VariablesGlobales.directorioactual + "/ValoresFormato.log";
        String valortexto1 = "70";
        String valortexto2 = "0";
        String valortexto3 = "0";
        int DosBarras = 0;
        int ImpresoraRW420 = 0;
        File file = new File(archivoFormato);
        RandomAccessFile archivoLineas;

        try {
            if (!file.exists()) {
                archivoLineas = new RandomAccessFile(file, "rw");
                archivoLineas.seek(archivoLineas.length());
                archivoLineas.writeBytes(valortexto1 + "\r\n");
                archivoLineas.writeBytes(valortexto2 + "\r\n");
                archivoLineas.writeBytes(valortexto3 + "\r\n");
                archivoLineas.writeBytes(DosBarras + "" + "\r\n");
                archivoLineas.writeBytes(ImpresoraRW420 + "" + "\r\n");
                archivoLineas.close();
            }

            // si existe sacar los calores requeridos del archivo guardado
            //else {
            RandomAccessFile leerArchivo = new RandomAccessFile(file, "rw");

            leerArchivo.seek(0);
            valortexto1 = leerArchivo.readLine().trim();
            // leerArchivo.seek(valortexto1.length());
            valortexto2 = leerArchivo.readLine().trim();
            moverGrafico = parseStringToInteger(valortexto2);
            // leerArchivo.seek(valortexto2.length());
            sinBarSence = parseStringToInteger(leerArchivo.readLine());


            try {
                DosBarras = Integer.parseInt(leerArchivo.readLine());
                ImpresoraRW420 = parseStringToInteger(leerArchivo.readLine());
                EsImpresora521 = ImpresoraRW420;
            } catch (Exception e) {
                if (!file.exists()) {
                    archivoLineas = new RandomAccessFile(file, "rw");
                    archivoLineas.seek(archivoLineas.length());
                    archivoLineas.writeBytes(valortexto1 + "\r\n");
                    archivoLineas.writeBytes(valortexto2 + "\r\n");
                    archivoLineas.writeBytes(valortexto3 + "\r\n");
                    archivoLineas.writeBytes(DosBarras + "" + "\r\n");
                    archivoLineas.writeBytes(ImpresoraRW420 + "" + "\r\n");
                    archivoLineas.close();
                }
                DosBarras = 1;


            }
            leerArchivo.close();

            //}
        } catch (NumberFormatException e) {

            e.printStackTrace();
        } catch (FileNotFoundException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }

        if (EsImpresora521 == 1) {
            imagenPrinter.setImageResource(R.drawable.impresora_ok521);//.impresora_ok);
            variables.crearArchivoImpresion1(valortexto1, valortexto2, DosBarras);
        } else {
            imagenPrinter.setImageResource(R.drawable.impresora_ok420);//.impresora_ok);
            variables.crearArchivoImpresion420(valortexto1, valortexto2, DosBarras);
        }
    }


    private void ejecutarProcesoDeFoto(String nombre, int guardainforme, int D2Digital, int directorio, String tipoProceso, int veces) {
        llamoatomarfoto = 1;

        // Determinar si esta foto corresponde a la lectura del medidor
        // tipoProceso == "" indica foto de medidor; "S" es foto de supervisor/novedad
        esFotoMedidor = (tipoProceso != null && tipoProceso.trim().equals("") && !yaTomoFotoOcr);

        // Foto OCR obligatoria si MODIFICACIONES == "1"
        fotoOcrObligatoria = infoRegistroSalida.gettablaRegistroSalida_MODIFICACIONES().trim().equals("1");

        try {
            vecesImagen = veces;
            int cont = 1;

            variableResumenLiquidacion = nombre + ";ENTRA A TOMAR FOTO ;" + getPhoneDate() + "-" + getPhoneHour();
            escribeResumenTiempo(variableResumenLiquidacion);


            if (variables.espc == 0) {

                activaLectorBarras = false;
                String directorioF = "/DCIM/FOTOGRAFIASL/";

                try {
                    String existeFoto;
                    String nombreImagen = "";

                    String dirTrabajo = VariablesGlobales.directorioactual;

                    Calendar calendar = Calendar.getInstance();
                    File foto;

                    try {
                        if (infoClienteEntrada.gettablaEntradaClientes_Clasedeservicio().trim().equals("IC")
                                || infoRegistroEntrada.gettablaRegistroDeEntrada_Tipomedida().trim().equals("CT")) {

                            existeFoto = dirTrabajo + directorioF + nombre + calendar.get(Calendar.YEAR) + "_4" + tipoProceso + ".jpg";
                            foto = new File(existeFoto.trim());

                            if (foto.exists()) {
                                nombreImagen = nombre + calendar.get(Calendar.YEAR) + "_5" + tipoProceso;
                            } else
                                nombreImagen = nombre + calendar.get(Calendar.YEAR) + "_4" + tipoProceso;
                        } else {
                            //nombre = cuenta + mes
                            existeFoto = dirTrabajo + directorioF + nombre + calendar.get(Calendar.YEAR) + "_" + cont + tipoProceso;
                            foto = new File(existeFoto.trim() + ".jpg");

                            if (foto.exists()) {
                                Log.e("INFO", "EXISTE: " + existeFoto.trim() + ".jpg");
                                while (foto.exists()) {

                                    cont++;

                                    existeFoto = dirTrabajo + directorioF + nombre + calendar.get(Calendar.YEAR) + "_" + cont + tipoProceso;
                                    foto = new File(existeFoto.trim() + ".jpg");

                                    if (!foto.exists()) {
                                        Log.e("INFO", "GUARDA NOMBRE FOTO: " + existeFoto.trim() + ".jpg");
                                        nombreImagen = nombre + calendar.get(Calendar.YEAR) + "_" + cont + tipoProceso;
                                    }
                                }

                            } else {
                                Log.e("INFO", "NO EXISTE: " + existeFoto.trim() + ".jpg");
                                nombreImagen = nombre + calendar.get(Calendar.YEAR) + "_" + cont + tipoProceso;
                            }
                        }

                        nameForIntent = nombreImagen + ".jpg";
                        nombreImagen = dirTrabajo + directorioF + nombreImagen + ".jpg";
                        //Log.e("error", "nombref " + nombreImagen);

                        VariablesGlobales.activarcamarafotografica = 1;

                       /* if (infoRegistroSalida.gettablaRegistroSalida_INFORME().trim().equals(""))
                            infoRegistroSalida.settablaRegistroSalida_informe("DEBE TOMAR FOTOGRAFIA");

                        //Log.e("error", "escribe 4");
                        escribirTablasSalida();*/

                        String dirEjecutable = VariablesGlobales.directorioactual;

                        int salir = 0;
                        File nombreDeArchivo = new File(dirEjecutable + "/ImagenACapturar.txt");

                        if (nombreDeArchivo.exists())
                            nombreDeArchivo.delete();


                        if (!nombreDeArchivo.exists()) {
                            RandomAccessFile writer2 = new RandomAccessFile(nombreDeArchivo, "rw");
                            writer2.writeBytes(nombreImagen.trim() + ".jpg");
                            writer2.close();
                        }

                        nombredelaImagen = nombreImagen.trim(); //Ax: variable global para guardar nombre de imagen para el metodo II
                        nombredelaImagen_2 = nombredelaImagen;
                        namePhoto = nombredelaImagen;
                        capRegistroActual = VariablesGlobales.registroactual;//Ax: variable global que captura registro actual

                     /*   if (infoRegistroSalida.gettablaRegistroSalida_INFORME().trim().equals("")) {// || infoRegistroSalida.gettablaRegistroSalida_INFORME().trim().equals("DEBE TOMAR FOTOGRAFIA")
                            infoRegistroSalida.settablaRegistroSalida_informe("DEBE TOMAR FOTOGRAFIA"); //"CON FOTOGRAFIA ");//Ax: cuando se toma foto no se puede escribir "con fotografia"
                        }
                        // Log.e("error", "escribe 5");
                        escribirTablasSalida();*/

                        ejecutarProcesoDeFotoII(veces);//va a tomar foto mientras el codigo continua

                        // Log.e("error", "escribe 6");
                        escribirTablasSalida();
                    } catch (Exception e) {
                        mensajeT("PROBLEMA EN ESCRIBIR \nLA FOTOGRAFIA... " + e, msgMedio);
                        //Toast.makeText(getApplicationContext(), "PROBLEMA EN ESCRIBIR \nLA FOTOGRAFIA... " + e, Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    mensajeT("ERROR.. Proceso Modulo de Fotografias para TPL", msgMedio);
                }

                VariablesGlobales.activarcamarafotografica = 0;
                activaLectorBarras = true;
            }

        } catch (Exception e) {
            utils.Log(logfile, "[MenuDeLiquidacion]ejecutarProcesoDeFoto() Error: " + e.getMessage());
        }
    }


    //Ax este metodo se ejecuta la primera en 'ejecutarProcesoDeFoto' y de nuevo cuando la camara se cierra (LLamado desde onActivityResult )
    //veces es la cantidad de reintentos de tomar foto
    Integer ContadorFoto = 0;

    /*private void ejecutarProcesoDeFotoII(int veces) {
        try {

            if (veces > 0) {

                Log.e("INFO", "nombredelaImagen: " + nombredelaImagen);
                Uri output = Uri.fromFile(new File(nombredelaImagen));
                File x = new File(nombredelaImagen);
                vecesImagen--;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Log.e("INFO", "ingresasusp|SDK MAYOR |entra a foto: " + nameForIntent);
                    nwdispatchTakePictureIntent();
                    nombrefoto = VariablesGlobales.directorioactual + "/DCIM/FOTOGRAFIASL/" + x.getName();
                    Log.e("INFO", "ingresasusp|nombreFoto " + nombrefoto);
                } else {
                    Log.e("INFO", "ingresasusp|SDK MENOR | entra a foto: " + nombredelaImagen);

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    int code = TAKE_PICTURE;
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, output);
                    startActivityForResult(intent, code);
                    nombrefoto = output.getPath();
                }

            } else {
                vecesImagen = 0;
                if (exitoImagen > 0) { //la foto si existe (aparentemente)
                    try {
                        metodo = "ProcesaFotosAsync";
                        TaskHelper.execute(new AsyncCallWS(), metodo);
                    } catch (Exception ex) {
                        utils.Log(logfile, "ejecutarProcesoDeFotoII() " + ex.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            utils.Log(logfile, "ERROR: Proceso Modulo de Fotografias II" + e.getMessage());
            mensajeT("ERROR: Proceso Modulo de Fotografias II", msgMedio);
            // Toast.makeText(getApplicationContext(), "ERROR: Proceso Modulo de Fotografias II", Toast.LENGTH_LONG).show();
        }
    }*/
    private void ejecutarProcesoDeFotoII(int veces) {
        try {

            if (veces > 0) {

                Log.e("INFO", "nombredelaImagen: " + nombredelaImagen);
                Uri output = Uri.fromFile(new File(nombredelaImagen));
                File x = new File(nombredelaImagen);
                vecesImagen--;

                if (esFotoMedidor) {
                    // ── MODO OCR: lanzar Activity intermedia de lectura ──────────
                    Log.e("INFO", "esFotoMedidor=true | lanzando CamaraLecturaActivity");
                    Intent intentOcr = new Intent(this, CamaraLecturaActivity.class);
                    intentOcr.putExtra(CamaraLecturaActivity.EXTRA_NOMBRE_IMAGEN, nombredelaImagen.trim());
                    startActivityForResult(intentOcr, CAMARA_LECTURA_REQUEST_CODE);
                    // nombrefoto se actualizará en onActivityResult cuando vuelva
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    // ── MODO NORMAL (foto causal/informe/supervisor) ─────────────
                    Log.e("INFO", "ingresasusp|SDK MAYOR |entra a foto: " + nameForIntent);
                    nwdispatchTakePictureIntent();
                    nombrefoto = VariablesGlobales.directorioactual + "/DCIM/FOTOGRAFIASL/" + x.getName();
                    Log.e("INFO", "ingresasusp|nombreFoto " + nombrefoto);
                } else {
                    Log.e("INFO", "ingresasusp|SDK MENOR | entra a foto: " + nombredelaImagen);

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    int code = TAKE_PICTURE;
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, output);
                    startActivityForResult(intent, code);
                    nombrefoto = output.getPath();
                }

            } else {
                vecesImagen = 0;
                if (exitoImagen > 0) { //la foto si existe (aparentemente)
                    try {
                        metodo = "ProcesaFotosAsync";
                        TaskHelper.execute(new AsyncCallWS(), metodo);
                    } catch (Exception ex) {
                        utils.Log(logfile, "ejecutarProcesoDeFotoII() " + ex.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            utils.Log(logfile, "ERROR: Proceso Modulo de Fotografias II" + e.getMessage());
            mensajeT("ERROR: Proceso Modulo de Fotografias II", msgMedio);
            // Toast.makeText(getApplicationContext(), "ERROR: Proceso Modulo de Fotografias II", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * esto cambio...................actualizar
     * Ax es llamado desde ejecutarProcesoDeFotoII cuando la foto haya sido tomada correctamente, entonces pasa a tomar
     * una segunda foto. "foto2" se activa para ir al metodo II y se desactiva en el III, lo inverso pasa con "foto2_1".
     * "foto2_1" controla para donde se dirige en OnactivityResult
     */
//    private void ejecutarProcesoDeFotoIII() { //int veces
//        try {
//
//            if (veces > 0) {
//                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                int code = TAKE_PICTURE;
//
//                Uri output = Uri.fromFile(new File(nombredelaImagen_2.replace(".jpg", "b.jpg")));
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, output);
//                vecesImagen--;
//                startActivityForResult(intent, code);
//
//                nombrefoto = output.getPath();
//            } else {
//                vecesImagen = 0;
////                foto2 = false;
////                foto2a = false;
//
//                if (exitoImagen > 0) { //la foto si existe (aparentemente)
//
//                    try {
//                        // String[] val2 = sfoto2.split(";");//
//                        String mensajefoto = utils.ReduceImagen2(nombrefoto, fechafoto(), txtLatitud.getText().toString(), txtLongitud.getText().toString(), logfile);
//
//                        if (!mensajefoto.trim().equals("")) {
//                            utils.Log(logfile, "[MenuDeLiquidacion]ejecutarProcesoDeFotoIII();" + mensajefoto);
//                        }
//                        //Ax: en este punto se procede a hacer envio de la foto, se crea el archivo F y se procede a enviar
//                        File foto = new File(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/" + "F" + Ciclo + Municipio + Seccion + Division);
//                        Log.e("error", "nombrefoto2 " + nombredelaImagen.trim());
//
//                        File validaFoto2 = new File(nombrefoto.trim());
//                        if (!nombrefoto.trim().isEmpty() && !validaFoto2.isDirectory()) {
//                            //escribe algo como: 102236_89999_20180301_115323_06.jpg    ;X
//                            Log.e("error", "nombre foto III " + nombrefoto);
//                            if (!utils.EscribirLinea(foto, String.format("%1$-50s", nombrefoto.substring(nombrefoto.lastIndexOf("/") + 1, nombrefoto.length())) + ";X\r\n")) { //Aqui llena el archivo de fotos con el nombre de la foto tomada//'X'es no enviada
//                                utils.Log(logfile, "[MenuDeLiquidacion]procesarimagen(). Problema Grabando Nombre de Foto" + mensajefoto);
//                                mensajeT("Problema Grabando Nombre de Foto", msgMedio);
//                            }
////                            else {
////                                 EnviarFotos();
////                            }
//                        } else {
//                            mensajeT("Error al guardar nombre de foto", msgMedio);
//                        }
//
//                    } catch (Exception ex) {
//                        mensajeT("ERROR: Proceso Modulo de Fotografia III\n" + ex.getMessage(), msgCorto);
//                        utils.Log(logfile, "[MenuDeLiquidacion]ejecutarProcesoDeFotoIII().;" + ex.getMessage());
//                    } finally {
//                        exitoImagen = 0;
//                        VariablesGlobales.activarcamarafotografica = 0;
//                    }
//                }
//                nombredelaImagen = "";
//                nombredelaImagen_2 = "";
////                foto2 = false;
////                foto2a = false;
//            }
//        } catch (Exception e) {
//            mensajeT("ERROR: Proceso Modulo de Fotografia III", msgCorto);
//            nombredelaImagen = "";
//            nombredelaImagen_2 = "";
////            foto2 = false;
////            foto2a = false;
//        }
//    }
    public String ejecutarProcesoDeFotoIII() {
        String return_msg = " ✓ | ";
        String nombretempfoto = "";

        try {
            if (nombrefoto.trim().isEmpty()) {
                return " | | ";

            } else {
                nombretempfoto = new File(nombrefoto).getName();
                nombretempfoto = nombretempfoto.substring(0, nombretempfoto.indexOf("_")); //Ax: sacar cuenta del nombre de la foto
            }
            Utils utils2 = new Utils();
            String mensajefoto = utils2.ReduceImagen2(nombrefoto, getPhoneDate() + "-" + getPhoneHour() + " C: " + txtLatitud.getText().toString() + " : " + txtLongitud.getText().toString());
            //String mensajefoto = utils2.reduceImagen(nombrefoto, getPhoneDate() + "-" + getPhoneHour() + " C: " + txtLatitud.getText().toString() + " : " + txtLongitud.getText().toString(), 640, 40_000, 60_000);
            File nombrefotoFile = new File(nombrefoto);

            if (!mensajefoto.trim().equals("")) {
                nombrefotoFile.delete();
                utils.Log(logfile, mensajefoto + ", cuenta " + nombretempfoto + ", Registro:" + capRegistroActual + ", Foto:" + nombrefoto);
                return_msg = " |" + nombretempfoto + "| ";
            } else {

                //Ax: en este punto se procede a hacer envio de la foto, se crea el archivo F y se procede a enviar
                File foto = new File(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/" + "F" + Ciclo + Municipio + Seccion + Division);
                //File foto = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/" + "F" + NombreArchivos);

                if (!nombrefoto.trim().isEmpty()) {
                    if (nombrefotoFile.length() > 100000) {
                        utils2.ReduceImagen2(nombrefoto, "");//Ax: intento de bajarle a algunas fotos que se escapan al proceso de reduccion
                    }
                    if (!utils.EscribirLinea(foto, String.format("%1$-50s", nombrefoto.substring(nombrefoto.lastIndexOf("/") + 1, nombrefoto.length())) + ";X\r\n")) { //Aqui llena el archivo de fotos con el nombre de la foto tomada//'X'es no enviada
                        utils.Log(logfile, "[MenuDeLiquidacion]procesarimagen(). Problema Grabando Nombre de Foto" + mensajefoto);
                        //mensajeT("Problema Grabando Nombre de Foto", msgMedio);
                        return_msg = " |" + nombretempfoto + "|Problema Grabando Nombre de Foto";
                    } else {
                        /*metodo = "AvanzarLecturas";
                        TaskHelper.execute(new AsyncCallWS(), metodo);*/

                        EnviarFotos();
                    }
//                    String cadenaFoto = String.format("%11s", idFoto) + ";" + String.format("%9s", cuentaFoto) + ";" + String.format("%1$-50s", nombrefoto.substring(nombrefoto.lastIndexOf("/") + 1, nombrefoto.length())) + ";\r\n";
//                    if (!utils2.leerArchivoFotos(foto, nombrefoto.substring(nombrefoto.lastIndexOf("/") + 1, nombrefoto.length()))) {
//                        utils2.EscribirLinea(new File(archivoEFotos + serialPDA + ".SDA"), cadenaFoto);
//
//                        if (!utils2.EscribirLinea(foto, String.format("%1$-20s", nombrefoto.substring(nombrefoto.lastIndexOf("/") + 1, nombrefoto.length())) + ";X\r\n")) { //Aqui llena el archivo de fotos con el nombre de la foto tomada//'X'es no enviada
//                            utils.Log(logfile, "Problema Grabando Nombre de Foto" + mensajefoto);
//                            return_msg = " |" + nombretempfoto;
//                        }
//                    }

                    utils.CrearCopia(nombrefotoFile.getAbsolutePath(), VariablesGlobales.directorioBackUp + nombrefotoFile.getName());

                } else {
                    return_msg = " |" + nombretempfoto + "| ";
                }
            }
        } catch (Exception ex) {
            utils.Log(logfile, "ejecutarProcesoDeFoto3 " + ex.getMessage());
            return_msg = " |" + nombretempfoto + "| " + ex.getMessage();
        }
        return return_msg;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - NUEVA FORMA PARA TOMAR LA FOTO
    private void nwdispatchTakePictureIntent() {
        imageUri = createImageUri(nameForIntent);

        if (imageUri != null) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            CodigoIntent = TAKE_PICTURE;
            activityResultLauncher.launch(takePictureIntent);
        } else {
            Toast.makeText(this, "No se pudo crear el archivo para la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    private Uri createImageUri(String fileName) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/FOTOGRAFIASL");

        ContentResolver resolver = getContentResolver();
        return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    int resultado = result.getResultCode();
                    Intent data = result.getData();
                    try {

                        if (CodigoIntent == TAKE_PICTURE) {//Respuesta Tomar fotos en liq.
                            if (resultado == RESULT_OK) {

                                vecesImagen = 0;
                                exitoImagen = 1;
                                //AvanzarLecturas();
                                ejecutarProcesoDeFotoII(vecesImagen);//Ax: idealmente se tomo la foto

                            } else {//Ax: No se tomo foto
                                Log.e("error", "resul foto nook " + vecesImagen);
                                vecesImagen = 0;
                                //if (vecesImagen > 0) {
                                //    mensajeT("No se tomo foto, pruebe de nuevo", msgMedio);
                                // Toast.makeText(getApplicationContext(), "No se tomo foto, pruebe de nuevo", Toast.LENGTH_LONG).show();
                                //}
                                //Log.d("heightDiff C", "No se tomo foto, veces:" + vecesImagen);
                                //vecesImagen = 1;
                                //ejecutarProcesoDeFotoII(vecesImagen);
                            }
                        }
                    } catch (Exception ex) {
                        Log.e("ERROR", "[MenuDeLiquidacion]onActivityResultLauncher|ERROR|" + ex.getMessage());
                        utils.Log(logfile, "[MenuDeLiquidacion]onActivityResultLauncher()| Error -> " + ex.getMessage());
                    }
                }
            });

    //----------------------------------------------------------------------------------------------------------------PROCESO DEENVIODEFOTOS NUEVO
    //revisa el archivo F, por los no enviados y envia de a 50
    public void EnviarFotos() {
        try {
            String respuesta;
            int cantFotos = 20;

            RutaZip = VariablesGlobales.directorioactual + "/DATOSDEENTRADA/" + "FOTOS" + Municipio + Seccion + Division + ".ZIP";
            List<String> fotos = new ArrayList<String>(); //Ax: Contendra lista de solo  archivos JPG
            int desde = 51;
            int hasta = 52;

            String g = VariablesGlobales.directorioactual + "/DATOSDEENTRADA/" + "F" + Ciclo + Municipio + Seccion + Division;
            if (!new File(g).exists()) {
                return;
            }

            FileReader r = new FileReader(g); //Ax: se recorre el archivo para capturar las no eviadas o en envio
            BufferedReader reader = new BufferedReader(r);
            String linea;
            int conteo = 0;
            int conteoPos = 0;
            posicfotocont = new int[cantFotos + 1];//Ax: va a guardar la posicion real de la linea
            String Ultimafoto = "";
            /*while ((linea = reader.readLine()) != null) {
                conteo++;
                if (linea.substring(desde, hasta).equals("X") || linea.substring(desde, hasta).equals("_")) {

                    File fotop = new File(VariablesGlobales.directorioactual + "/DCIM/FOTOGRAFIASL/" + linea.substring(0, 50).trim());// ax: cambiar a FOTOGRAFIASL

                    // Manda a reprocesar las imagenes si hay alguna que se haya escapado a la reduccion anterior
                    if (fotop.length() > 100000){
                        utils.ReduceImagen2(nombrefoto,getPhoneDate() + "-" + getPhoneHour() + " C: " + txtLatitud.getText().toString() + " : " + txtLongitud.getText().toString());
                        continue;
                    }
                    if (fotop.exists() && !fotop.isDirectory()) {
                        conteoPos++;
                        if (conteoPos == 20) {
                            break;
                        }
                        if (!linea.equals(Ultimafoto)) {
                            Ultimafoto = linea.trim();
                            fotos.add(VariablesGlobales.directorioactual + "/DCIM/FOTOGRAFIASL/" + linea.substring(0, 50).trim());
                            posicfotocont[conteoPos] = conteo;
                        } else {
                            --conteo;
                            --conteoPos;
                        }
                    }
                }
            }
            r.close();*/
            while ((linea = reader.readLine()) != null) {
                conteo++;
                if (linea.substring(desde, hasta).equals("X") || linea.substring(desde, hasta).equals("_")) {

                    File fotop = new File(VariablesGlobales.directorioactual + "/DCIM/FOTOGRAFIASL/" + linea.substring(0, 50).trim());
                    // Manda a reprocesar las imagenes si hay alguna que se haya escapado a la reduccion anterior
                    if (fotop.length() > 100000) {
                        utils.ReduceImagen2(nombrefoto, getPhoneDate() + "-" + getPhoneHour() + " C: " + txtLatitud.getText().toString() + " : " + txtLongitud.getText().toString());
                        continue;
                    }

                    if (fotop.exists()) {
                        conteoPos++;
                        if (conteoPos == 20) {
                            break;
                        }

                        fotos.add(VariablesGlobales.directorioactual + "/DCIM/FOTOGRAFIASL/" + linea.substring(0, 50).trim());
                        posicfotocont[conteoPos] = conteo;
                    }
                }
            }
            r.close();

            if (fotos.size() > 0) {
                ArrayList filestoZip = new ArrayList(); //Contiene los archivos por comprimir

                for (String x : fotos) { //Recorrer cada jpg encontrado para añadirlo
                    filestoZip.add(new File(x));
                }
                respuesta = utils.CreaZip(RutaZip, filestoZip);

                Log.e("error", "envio fotos :" + respuesta);
                if (!respuesta.contains("✓")) {
                    mensajeT("Error Comprimiendo \n Archivos por enviar", msgLargo);
                    return;
                }

                Log.e("error", procesandoenvioenHilos_CPCAN + " envio fotos1 " + comprobarconexiones(true));
                if (!comprobarconexiones(true)) return;

                if (procesandoenvioenHilos_chat) {
                    contadorChat = 0;
                    taskChat.cancel(true);
                }

                procesandoenvioenHilos_CPCAN = 1;
                try {
                    metodo = "EnviarFotosServer";
//                    AsyncCallWS task = new AsyncCallWS();
//                    task.execute("");
                    TaskHelper.execute(new AsyncCallWS(), metodo);
                    banderaWsOcupado = true;

                } catch (Exception ex) {
                    mensajeT(ex.getMessage(), msgLargo);
                    banderaWsOcupado = false;//Bandera conexion ocupada
                    utils.Log(logfile, "[Comunicaciones] EnviarFotosServer(): " + ex.getMessage());
                    procesandoenvioenHilos_CPCAN = 0;
                }

            } else {
                mensajeT("No hay fotografia(s) para enviar", msgLargo);
            }
        } catch (Exception ex) {
            mensajeT("Error en el envio de fotos \n " + ex.getMessage(), msgLargo);
            Log.e("INFO","[MenuDeLiquidacion] EnviarFotosServer()- Error en el envio de fotos \n " + ex.getMessage());
            utils.Log(logfile, "[MenuDeLiquidacion] EnviarFotosServer()-: " + ex.getMessage());
        }
    }

    public void actualizaFotos() { //Recorre el archivo de fotos para actualizar los que si se fueron "Y"
        try {
            int desde = 51;
            int hasta = 52;
            String path = VariablesGlobales.directorioactual + "/DATOSDEENTRADA/F" + Ciclo + Municipio + Seccion + Division;
            FileReader r = new FileReader(path); //Ax: se recorre el archivo para actualizar las eviadas
            File fileName = new File(path + "_temp");
            BufferedReader reader = new BufferedReader(r);
            String linea;
            int conteo = 0;

            while ((linea = reader.readLine()) != null) {
                conteo++;

                for (int i = 0; i < posicfotocont.length; i++) {
                    if (conteo == posicfotocont[i]) {
                        linea = linea.substring(0, desde) + "Y" + linea.substring(hasta);
                        continue;
                    }
                }
                utils.EscribirLinea(fileName, linea + "\r\n");
            }
            r.close();

            File fpath = new File(path);
            try {
                if (reader != null) reader.close();

                fpath.delete();
            } catch (Exception ex) {
                mensajeT("Error en la actualizacion de fotos.. \n " + ex.getMessage(), msgLargo);
                utils.Log(logfile, "[Comunicaciones] actualizaFotos()..: " + ex.getMessage());
            }
            fileName.renameTo(fpath);

        } catch (Exception ex) {
            mensajeT("Error en la actualizacion de fotos \n " + ex.getMessage(), msgLargo);
            utils.Log(logfile, "[Comunicaciones] actualizaFotos().: " + ex.getMessage());
        }
    }

    /***
     * Ax: Trata de armar el zip, comprobar carpetas del server, enviar zip, y descomprimir remoto, luego borra el zip del cel.
     *
     * @return
     */
    public String EnviarFotosServerAsync() {

        File file = new File(RutaZip);

        try {
            WSSoap wsoap = new WSSoap(URL, paginaWs);
            String ArchivoAEnviarRecibir = rutaAdministrador.trim() + "CIC" + Ciclo + "\\C" + Municipio + Seccion + Division + "\\"; //D:ENRUTADOR_X/CIC0809/C1851011/
            String hash = utils.Md5Hash(RutaZip);
            Log.e("error", ArchivoAEnviarRecibir + " directorio fotos " + file.getName());
            String nombremetodo = "VerificarSiexiste_Directorio_Archivo";//No usar la variable "metodo" pues es global
            String respuesta = wsoap.VerificarSiexisteDirectorioArchivo(nombremetodo, ArchivoAEnviarRecibir, file.getName());//Ax: Se comprueba si existe la ruta destino remota

            if (!respuesta.equals("true")) {
                utils.Log(logfile, "[MenuDeLiquidacion]EnviarFotosServerAsync(): " + "Envio de Fotos, No se ha encontrado destino en el servidor!");
                return "Envio de Fotos, \n No se ha encontrado destino en el servidor!";
            }

            nombremetodo = "Terminal_ToServerReceive";
            respuesta = wsoap.TerminalToServerReceive(nombremetodo, ArchivoAEnviarRecibir, parseStringToInteger(esComprimido), RutaZip, trama);//Enviar Datos al servidor

            if (!(respuesta.equals("4") || respuesta.equals("1"))) {
                utils.Log(logfile, "[MenuDeLiquidacion]EnviarFotosServerAsync(): " + "Envio de Fotos,No se pudo Enviar Archivo al servidor");
                return "Envio de Fotos, \n No se pudo Enviar Archivo al servidor";
            }

            metodo = "Descomprime";
            respuesta = wsoap.Descomprimir(metodo, ArchivoAEnviarRecibir + "\\FOTOGRAFIAS", ArchivoAEnviarRecibir + file.getName(), hash, serialPDA);

            if (!respuesta.equals("true")) {
                utils.Log(logfile, "[MenuDeLiquidacion]EnviarFotosServerAsync(): " + "Envio de Fotos, No se pudo descomprimir remoto");
                return "Envio de Fotos, \n No se pudo descomprimir remoto";
            }

            actualizaFotos();
            file.delete();
        } catch (Exception ex) {
            utils.Log(logfile, "[MenuDeLiquidacion]EnviarFotosServerAsync(): " + ex.getMessage());
            return "Error al enviar fotos al servidor\n, intente de nuevo";
        } finally {
            banderaWsOcupado = false;//Bandera conexion ocupada
            procesandoenvioenHilos_CPCAN = 0;
        }
        metodo = "EnviarFotosServer";
        return "Proceso de Fotos Exitoso! ✓";
    }
    //----------------------------------------------------------------------------------------------------------------

    private void ejecutarSumatoriaConsumoACobrar() {

        if (infoClienteSalida.gettablaClienteSalida_CONSUMO2().trim().equals(""))
            infoClienteSalida.settablaClienteSalida_consumo2("0");
        if (infoClienteSalida.gettablaClienteSalida_CONSUMO1().trim().equals(""))
            infoClienteSalida.settablaClienteSalida_consumo1("0");
        if (infoClienteSalida.gettablaClienteSalida_CONSUMO3().trim().equals(""))
            infoClienteSalida.settablaClienteSalida_consumo3("0");

        if (infoRegistroEntrada.gettablaRegistroDeEntrada_Factormultipicacion().trim().length() == 0)
            infoRegistroEntrada.settablaRegistroDeEntrada_Factormultipicacion("1");

        //donde meter esto para que el sistema detecte a los consumos altos normalizados detecte y no deje imprimir y adicionalmente  muestre aviso de entregar carta
        if ((infoRegistroSalida.gettablaRegistroSalida_COMENTARIO2().equals("003")
                || infoClienteSalida.gettablaClienteSalida_IMPRESORA().trim().equals("CONS. ALTO DV3"))) {
            //aqui cabe laidea de que so es menor a 1000kw no seguarde elindicador 40¡¡?
            if (infoRegistroEntrada.gettablaRegistroDeEntrada_tipoenergia().trim().equals("A")) {
                activaCritica = true;
                infoRegistroSalida.settablaRegistroSalida_causadenolectura("18");
                //mirar si lo dejas aqui o lo retiramos
                variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";Escribio 18 aqui  5;" + getPhoneDate() + "-" + getPhoneHour();

                escribeResumenTiempo(variableResumenLiquidacion + " " + "Escbribio consumo alto desviacion aqui 5|" + VariablesGlobales.registroactual);

                infoRegistroSalida.escribir_TablaRegistroSalida(VariablesGlobales.registroactual);
            }
        }

        //finalizar nueva metodologia


        if (infoRegistroEntrada.gettablaRegistroDeEntrada_tipoenergia().equals("R")) {
            Log.e("error2", "valor consumo 1 " + variables.consumoactual + "-" + infoClienteSalida.gettablaClienteSalida_CONSUMO1());
            infoClienteSalida.settablaClienteSalida_consumo1("" + parseStringToInteger("" + (variables.ejecutarAjusteUnidades(parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO1()) + (variables.consumoactual * parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Factormultipicacion()))))));
            infoRegistroSalida.settablaRegistroSalida_consumotomado(infoClienteSalida.gettablaClienteSalida_CONSUMO1());
            variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";ejecutarSumatoriaConsumoACobrar;" + getPhoneDate() + "-" + getPhoneHour();
            escribeResumenTiempo(variableResumenLiquidacion + " " + "ejecutarSumatoriaConsumoACobrar: Posicion Actual|" + VariablesGlobales.registroactual);
            infoRegistroSalida.escribir_TablaRegistroSalida(VariablesGlobales.registroactual);
        } else {
            // Log.e("error", "inicia finaliza sumatoria 3.17");
            LeyoActiva = 1;
            infoClienteSalida.settablaClienteSalida_consumo2("" + parseStringToInteger((variables.ejecutarAjusteUnidades(parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2()) +
                    (variables.consumoactual * parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Factormultipicacion())))) + ""));

            variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";ejecutarSumatoriaConsumoACobrar else;" + getPhoneDate() + "-" + getPhoneHour();
            escribeResumenTiempo(variableResumenLiquidacion + " " + "ejecutarSumatoriaConsumoACobrar else: Posicion Actual|" + VariablesGlobales.registroactual);
            infoRegistroSalida.settablaRegistroSalida_consumotomado("" + (int) (variables.consumoactual * parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Factormultipicacion())));
            infoRegistroSalida.escribir_TablaRegistroSalida(VariablesGlobales.registroactual);

            if (parseStringToDouble(infoClienteEntrada.gettablaEntradaClientes_Nrodemedidores()) > 1) {
                //mirar las otras opciones de donde tomar los valores a cobrar
                //Variables.EvaluarConServicio(InfoMedidorEntrada.ECONTADTIPOMEDIDOR.Trim());
                evaluarCargaInstalada(variables.consumoactual);
                if (String.format("%1$1s", infoMedidorEntrada.gettablaMedidorEntrada_idcortado().trim()).replace(" ", "0").equals("1")) {
                    variables.consumoactual = parseStringToDouble(infoMedidorEntrada.gettablaMedidorEntrada_lecturacorte());
                }

                //   Log.e("error", "inicia finaliza sumatoria 3.19");
                if (parseStringToDouble(infoClienteSalida.gettablaClienteSalida_VALOR1()) == 0 || (parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumo1()) == 0
                        && parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumo2()) == 0
                        && parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumo3()) == 0
                        && parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumo4()) == 0
                        && parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumo5()) == 0)) {


                    infoClienteSalida.settablaClienteSalida_valor1(parseStringToDouble(infoClienteSalida.gettablaClienteSalida_VALOR1()) + (variables.consumoactual * parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Factormultipicacion())) + "");
                } else {
                    if (parseStringToDouble(infoClienteSalida.gettablaClienteSalida_VALOR2()) == 0 || parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Nrodemedidores()) == 2) {
                        if ((parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumo1()) == 0
                                && parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumo2()) == 0
                                && parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumo3()) == 0
                                && parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumo4()) == 0
                                && parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumo5()) == 0))
                            infoClienteSalida.settablaClienteSalida_valor1("" + parseStringToDouble(infoClienteSalida.gettablaClienteSalida_VALOR1()) + (variables.consumoactual * parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Factormultipicacion())));
                        else
                            infoClienteSalida.settablaClienteSalida_valor2("" + (variables.consumoactual * parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Factormultipicacion())));
                    } else
                        infoClienteSalida.settablaClienteSalida_valor3("" + (variables.consumoactual * parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Factormultipicacion())));
                }

            }
        }

        Log.e("error2", "data a validar " + infoClienteSalida.gettablaClienteSalida_CONSUMO2().trim() + "-" + infoClienteSalida.gettablaClienteSalida_CONSUMO1().trim() + "-" + infoRegistroEntrada.gettablaRegistroDeEntrada_tipoenergia().equals("R")
                + LeyoActiva);

        if ((parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2().trim()) > 0 && parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO1().trim()) > 0) ||
                (parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO1().trim()) > 0 && !infoRegistroEntrada.gettablaRegistroDeEntrada_tipoenergia().equals("R")) ||
                (LeyoActiva == 1 && parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO1().trim()) > 0)) {
            Log.e("error2", "entra en consumo3 1" + infoClienteSalida.gettablaClienteSalida_CONSUMO1() + "--" + infoClienteSalida.gettablaClienteSalida_CONSUMO2());

            if (parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO1()) < parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2()) / 2) {
                infoClienteSalida.setTablaClienteSalida_CONSUMOFACTURADO("" + parseStringToInteger("" + variables.ejecutarAjusteUnidades(parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2().trim()))));
            } else {
                Log.e("error2", "entra en consumo3 2");
                infoClienteSalida.setTablaClienteSalida_CONSUMOFACTURADO("" + parseStringToInteger("" + variables.ejecutarAjusteUnidades(parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO1()) - ((parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2()) / 2)) + parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2()))));
                Log.e("error2", "entra en consumo3 2.1");

                if (parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO3()) == 0) {
                    infoClienteSalida.settablaClienteSalida_consumo3("" + parseStringToInteger((variables.ejecutarAjusteUnidades(parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO1()) - ((parseStringToDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2()) / 2)))) + ""));
                }
            }
        } else {

            infoClienteSalida.setTablaClienteSalida_CONSUMOFACTURADO("" + parseStringToInteger("" + variables.ejecutarAjusteUnidades(parseStringToDouble(infoClienteSalida.getTablaClienteSalida_CONSUMOFACTURADO()) + (variables.consumoactual * parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Factormultipicacion())))));
        }

        //---------------------------------------------------------------------------------------------------
    }


    private void evaluarDistancia() {

        // SECLAUSURA Y SE PONE LA FUNCION POR MEDIDOR ANTES DE LIQUIDAR EL ARCHIVO
        if (indicadorManual == 0) {
            if (encenderEstadoActualGPS == 1) {
                if (txtLatitud.getText().toString().trim().equals("0.00") || txtLatitud.getText().toString().trim().equals("0") || txtLatitud.getText().toString().trim().equals("")) {
                    capturarCoordenadasPredio();
                }
                if (txtLongitud.getText().toString().trim().equals("0.00") || txtLongitud.getText().toString().trim().equals("0") || txtLongitud.getText().toString().trim().equals("")) {
                    capturarCoordenadasPredio();
                }
                ultimaLongitud = txtLongitud.getText().toString();
                variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";Finaliza gps;" + getPhoneDate();
                escribeResumenTiempo(variableResumenLiquidacion);

                reportarDistancia();// proceso para enviar el reporte a la base de datos

                variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";Finaliza Distancia;" + getPhoneDate();
                escribeResumenTiempo(variableResumenLiquidacion);
            }
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Verifica al cargar cada cliente si la foto OCR es obligatoria (MODIFICACIONES==1).
     * Si es obligatoria y aún no se tomó:
     *  - Muestra alerta al técnico
     *  - Bloquea campo de lectura, anomalía y menú
     *  - Deja habilitado solo el botón de tomar foto
     * Si ya se tomó o no es obligatoria, restaura la UI normal.
     */
    private void verificarFotoObligatoriaAlCargarCliente() {

        fotoOcrObligatoria = false;//"1";//infoClienteEntrada.getTablaEntradaClientes_NN4().trim().equals("1");
        Log.e("INFO","[MenuDeLiquidacion]verificarFotoObligatoriaAlCargarCliente() modificaa: " + infoRegistroSalida.gettablaRegistroSalida_MODIFICACIONES().trim());
        Log.e("INFO","[MenuDeLiquidacion]verificarFotoObligatoriaAlCargarCliente() Cliente: " + infoRegistroSalida.gettablaRegistroSalida_CUENTA().trim());
        Log.e("INFO","[MenuDeLiquidacion]verificarFotoObligatoriaAlCargarCliente() fotoOcrObligatoria: " + fotoOcrObligatoria);
        Log.e("INFO","[MenuDeLiquidacion]verificarFotoObligatoriaAlCargarCliente() yaTomoFotoOcr: " + yaTomoFotoOcr);
        if (fotoOcrObligatoria && !yaTomoFotoOcr) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Foto obligatoria");
            builder.setMessage("Esta cuenta requiere captura de lectura por fotografía.\nAl presionar TOMAR FOTO se abrirá la cámara.");
            builder.setCancelable(false);
            builder.setPositiveButton("TOMAR FOTO", (dialog, which) -> capturaImagenFotografica());
            builder.setNegativeButton("AHORA NO", null);
            builder.show();
        }
    }

    private void resetValidacionUbicacion() {
        estadoUbicacion = UBI_NO_VALIDADO;
        distanciaActualPredioMetros = -1f;
        intentosLecturaFueraRango = 0;  // reset al cambiar de predio
    }

    private void validarUbicacionContraPredioActual() {
        // Si ya se validó para este predio, no recalcular
        // El reset solo ocurre en avanzarRegistro/retrocedeRegistro
        if (estadoUbicacion != UBI_NO_VALIDADO) return;

        // Si la validación de distancia no es obligatoria, permitir sin restricción
        //if (!VariablesGlobales..distanciagps.trim().equals("1")) {
      /*  if (infoClienteEntrada.gettablaEntradaClientes_NN2().trim().equals("") || infoClienteEntrada.gettablaEntradaClientes_NN2().trim().equals("0")) {
            estadoUbicacion = UBI_DENTRO_RANGO;
            return;
        }*/

        if (encenderEstadoActualGPS == 0) {
            estadoUbicacion = UBI_SIN_GPS;
            return;
        }

        // Coordenadas del predio
        // cordenadax = Longitud, cordenaday = Latitud
        String cxStr = infoClienteEntrada.gettablaEntradaClientes_cordenadax().trim();
        String cyStr = infoClienteEntrada.gettablaEntradaClientes_cordenaday().trim();
        double lonPredio = parseStringToDouble(cxStr.isEmpty() ? "0" : cxStr);
        double latPredio = parseStringToDouble(cyStr.isEmpty() ? "0" : cyStr);

        if (latPredio == 0 && lonPredio == 0) {
            estadoUbicacion = UBI_SIN_COORDENADAS;
            return;
        }

        // Ubicación actual del dispositivo
        // coorTempLatitud tiene Longitud real, coorTempLongitud tiene Latitud real (invertido en muestraPosicionActual)
        String latStr = VariablesGlobales.coorTempLongitud.trim(); // ← esta es la Latitud real
        String lonStr = VariablesGlobales.coorTempLatitud.trim();  // ← esta es la Longitud real
        double latActual = parseStringToDouble(latStr.isEmpty() ? "0" : latStr);
        double lonActual = parseStringToDouble(lonStr.isEmpty() ? "0" : lonStr);
        Log.e("errorDistPredio", latPredio + " data long =" + lonPredio + " Coord x2 =" +
                latActual+" Coordy2 = "+lonActual);

        if (latActual == 0 && lonActual == 0) {
            estadoUbicacion = UBI_SIN_GPS;
            return;
        }

        // Calcular distancia
        distanciaActualPredioMetros = (float) variables.evaluarDistanciaAlPredio(
                latActual, lonActual,    // dispositivo
                latPredio, lonPredio   // predio
        );

        //distancia = (int) variables.evaluarDistanciaAlPredio(parseStringToDouble(VariablesGlobales.coorTempLongitud), parseStringToDouble(VariablesGlobales.coorTempLatitud), parseStringToDouble(infoClienteEntrada.getTablaEntradaClientes_cordenaday().trim()), parseStringToDouble(infoClienteEntrada.getTablaEntradaClientes_cordenadax().trim()));

        if (distanciaActualPredioMetros <= RADIO_PERMITIDO_METROS) {
            estadoUbicacion = UBI_DENTRO_RANGO;
        } else {
            estadoUbicacion = UBI_FUERA_RANGO;
        }
    }
    private boolean puedeContinuarConTomaLectura() {
        if (estadoUbicacion == UBI_DENTRO_RANGO
                || estadoUbicacion == UBI_SIN_COORDENADAS
                || estadoUbicacion == UBI_SIN_GPS) {
            return true;
        } else if (estadoUbicacion == UBI_FUERA_RANGO) {
            intentosLecturaFueraRango++;
            if (intentosLecturaFueraRango >= MAX_INTENTOS_FUERA_RANGO) {
                return true;
            } else {
                int intentosRestantes = MAX_INTENTOS_FUERA_RANGO - intentosLecturaFueraRango;
                mensajeT("Debe estar a menos de " + (int) RADIO_PERMITIDO_METROS
                        + " metros del predio.\n"
                        + "Distancia actual: " + (int) distanciaActualPredioMetros + " metros.\n"
                        + "Intentos restantes: " + intentosRestantes, msgMedio);
                return false;
            }
        } else {
            // UBI_NO_VALIDADO → intentar validar ahora
            validarUbicacionContraPredioActual();
            if (estadoUbicacion == UBI_DENTRO_RANGO
                    || estadoUbicacion == UBI_SIN_COORDENADAS
                    || estadoUbicacion == UBI_SIN_GPS) {
                return true;
            }
            mensajeT("La ubicación aún no ha sido validada.\n"
                    + "Espere a que el GPS obtenga señal.", msgMedio);
            return false;
        }
    }
    //------------------------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Escribe el archivo de texto para el caso de supervisor (censo) y sus posiciones
     *
     * @param msg arma_cuentasGps:  [0] año ,[1] mes ,[2] contador [3] medidor  ,[4] cuenta,[5] ciclo
     */
    private void guardarCoordenadas(String msg) {

        String texto;

        try {
//            if (VariablesGlobales.coorTempLongitud.length() > 12) {
//                VariablesGlobales.coorTempLongitud = VariablesGlobales.coorTempLongitud.substring(0, 12);
//            }
//            if (VariablesGlobales.coorTempLatitud.length() > 12) {
//                VariablesGlobales.coorTempLatitud = VariablesGlobales.coorTempLatitud.substring(0, 12);
//            }


            /*segun la tabla posicionamiento la estructura es esta
             * serialPDA.PadRight(15, ' ') +  cuenta.PadLeft(9, '0') +
             * fecha + String.format("%1$12s", VariablesGlobales.coorTempLatitud) +
             * String.format("%1$12s", VariablesGlobales.coorTempLongitud) +
             * String.format("%1$2s", numeroDeSatelitesGPS.trim()) +
             * String.format("%1$-20s", fechayHoraReportadaGPS.trim()) +
             * "N"
             * */
            //Ma: Aqui se guardaba lo que era el archivo CUENTASGPS.SDA

//            tomarFechaSistema("", "");

//            texto = String.format("%1$9s", arma_cuentasGps[4]) + ";" +
//                    String.format("%1$12s", VariablesGlobales.coorTempLatitud) + ";" +
//                    String.format("%1$12s", VariablesGlobales.coorTempLongitud) + ";" +
//                    String.format("%1$2s", numeroDeSatelitesGPS.trim()) + ";" +
//                    variables.amd + ";" +
//                    variables.hm + ";" +
//                    String.format("%1$-20s", fechayHoraReportadaGPS.trim()) + ";" +
//                    String.format("%1$-12s", altitudReportadaGPS.trim()) + ";" +
//                    String.format("%1$-3s", arma_cuentasGps[5]) + ";" +
//                    arma_cuentasGps[0] + ";" +
//                    String.format("%1$2s", arma_cuentasGps[1].trim()).replace(" ", "0") + ";" +
//                    String.format("%1$-5s", arma_cuentasGps[2]) + ";" +
//                    String.format("%1$-16s", Ciclo + Municipio + Seccion + Division) + ";" +
//                    String.format("%1$11s", lector.trim()) + ";" +
//
//                    String.format("%1$12s", velocidadGPS.trim()) + ";" +
//                    String.format("%1$-200s", msg) + ";" +
//                    "X" + ";\r\n";
//
//            if (VariablesGlobales.coorTempLatitud.trim().equals("0.0") && VariablesGlobales.coorTempLongitud.trim().equals("0.0")) {
//                return;
//            }
//
            metodo = "ENVIOPOSICIONAMIENTO";
//            AsyncCallWS task = new AsyncCallWS();
//            task.execute("");
            TaskHelper.execute(new AsyncCallWS(), metodo);
//            File file = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/CUENTASGPS.SDA");
//
//            if (!file.exists()) {
//                file.createNewFile();
//            }
//            RandomAccessFile writer = new RandomAccessFile(file, "rw");
//            writer.seek(writer.length());
//            writer.writeBytes(texto);
//            writer.close();
        } catch (Exception ex) {
            utils.Log(logfile, "[MenuDeLiquidacion]guardarCoordenadas" + ex.toString());
        }
    }

    private int reportarDistancia() {

        String longitud;
        String latitud;
        try {
            Log.e("error", infoClienteEntrada.gettablaEntradaClientes_cordenadax().trim() + " data long0 " + infoClienteEntrada.gettablaEntradaClientes_cordenaday().trim());

            if (infoClienteEntrada.gettablaEntradaClientes_cordenadax().trim().equals(""))
                infoClienteEntrada.settablaEntradaClientes_cordenadax("0");
            if (infoClienteEntrada.gettablaEntradaClientes_cordenaday().trim().equals(""))
                infoClienteEntrada.settablaEntradaClientes_cordenaday("0");

            if (VariablesGlobales.coorTempLongitud.trim().equals("") || VariablesGlobales.coorTempLongitud.trim().equals("00"))//00 antes era "Desconocida"
                longitud = "0";
            else
                longitud = VariablesGlobales.coorTempLongitud.trim();

            if (VariablesGlobales.coorTempLatitud.trim().equals("") || VariablesGlobales.coorTempLatitud.trim().equals("00"))//antes era "Desconocida"
                latitud = "0";
            else
                latitud = VariablesGlobales.coorTempLatitud.trim();


            // proceso nuevo para validar las coordenadas mejorar
            int distancia = 999;
            Log.e("error", latitud + " data long " + longitud);
            distancia = (int) variables.evaluarDistanciaAlPredio(parseStringToDouble(latitud), parseStringToDouble(longitud), parseStringToDouble(infoClienteEntrada.gettablaEntradaClientes_cordenadax().trim()), parseStringToDouble(infoClienteEntrada.gettablaEntradaClientes_cordenaday().trim()));//[enerca]  parseStringToDouble(infoClienteEntrada.gettablaEntradaClientes_CORDENADAX().trim()), parseStringToDouble(infoClienteEntrada.gettablaEntradaClientes_CORDENADAY().trim()));

            infoClienteSalida.settablaClienteSalida_distanciacalculada(Integer.toString(distancia).trim());
            infoClienteSalida.escribir_TablaClienteSalida(variables.clienteactual);

            Log.e("error", distancia + " data long1 " + infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim() + "-" + parseStringToInteger(infoRegistroSalida.gettablaRegistroSalida_INTENTOS().trim()));
            if (distancia > 250) {
                if ((Double.parseDouble(infoClienteEntrada.gettablaEntradaClientes_cordenadax().trim()) > 0)
                        && (parseStringToInteger(infoRegistroSalida.gettablaRegistroSalida_INTENTOS().trim()) <= 2)
                        && (infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim().equals(""))) {
                    //  variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim()
                    //  + ";Ubicacion Incorrecta del Lector;" + getPhoneDate() + " " + getPhoneHour();
                    //  escribeResumenTiempo(variableResumenLiquidacion);
                    mensajeT("ESTA CUENTA SE REGISTRO SIN ESTAR UBICADO EN EL PREDIO\nSE RECODIFICARA LA UBICACION \n DEL  PREDIO", msgMedio);
                    //   Toast.makeText(getApplicationContext(), "ESTA CUENTA SE REGISTRO SIN ESTAR UBICADO EN EL PREDIO\nSE RECODIFICARA LA UBICACION \n DEL  PREDIO", Toast.LENGTH_LONG).show();
                    return 0;
                }
            }

        } catch (Exception e) {
            mensajeT("No Hay coordenadas iniciales", msgCorto);
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    private void capturarCoordenadasPredio() {

        if (encenderEstadoActualGPS == 1) {

            if (txtLatitud.getText().toString().trim().equals("0") || txtLatitud.getText().toString().trim().equals("0.00")) {
                txtLatitud.setText(longitudActualGpg.trim().replace(",", "."));
            }

            if (txtLongitud.getText().toString().trim().equals("0") || txtLongitud.getText().toString().trim().equals("0.00"))
                txtLongitud.setText(latitudActualGpg.trim().replace(",", "."));

            if (txtLatitud.getText().toString().trim().length() > 12) {
                txtLatitud.setText(txtLatitud.getText().toString().trim().substring(0, 12));
            }
            if (txtLongitud.getText().toString().trim().length() > 12)
                txtLongitud.setText(txtLongitud.getText().toString().trim().substring(0, 12));

        } else {

            txtLatitud.setText("0.00");
            txtLongitud.setText("0.00");
            numeroDeSatelitesGPS = "0";
            velocidadGPS = "0";
            //  Calendar calendar = Calendar.getInstance();
            //  fechayHoraReportadaGPS = calendar.get(Calendar.DAY_OF_MONTH) + "/" + (1 + (int) (calendar.get(Calendar.MONTH))) + "/" + calendar.get(Calendar.YEAR) + " " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND);
        }
    }

    private void procesarLiquicadionCortado() {

        if (infoRegistroEntrada.gettablaRegistroDeEntrada_Factormultipicacion().trim().length() == 0)
            infoRegistroEntrada.settablaRegistroDeEntrada_Factormultipicacion("1");

        variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";procesarLiquicadionCortado ;" + getPhoneDate() + "-" + getPhoneHour();
        escribeResumenTiempo(variableResumenLiquidacion + " " + "procesarLiquicadionCortado: Posicion Actual|" + VariablesGlobales.registroactual);

        if (infoRegistroEntrada.gettablaRegistroDeEntrada_Tipomedida().trim().equals("CT")) {
            // infoClienteSalida.SCLIENTCONSUMO3 =
            // Convert.ToString(variables.EjecutarAjusteUnidades(Double.parseDouble(infoClienteSalida.SCLIENTCONSUMO3)
            // + (variables.consumoactual))).trim();
            infoClienteSalida.settablaClienteSalida_consumo3("" + parseStringToInteger(Double.toString(variables.ejecutarAjusteUnidades(
                    Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO3().trim()) + (variables.consumoactual)))));
            infoRegistroSalida.escribir_TablaRegistroSalida(VariablesGlobales.registroactual);
        } else if (infoRegistroEntrada.gettablaRegistroDeEntrada_tipoenergia().equals("R")) {
            infoClienteSalida.settablaClienteSalida_consumo1("" + parseStringToInteger(Double.toString(variables.ejecutarAjusteUnidades(
                    Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO1().trim()) + (variables.consumoactual)))));
            infoRegistroSalida.settablaRegistroSalida_consumotomado("" + parseStringToInteger(infoClienteSalida.gettablaClienteSalida_CONSUMO1()));
            infoRegistroSalida.escribir_TablaRegistroSalida(VariablesGlobales.registroactual);
        } else {
            infoClienteSalida.settablaClienteSalida_consumo2("" + parseStringToInteger(Double.toString(variables.ejecutarAjusteUnidades(
                    Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2().trim()) + (variables.consumoactual)))));
            infoRegistroSalida.settablaRegistroSalida_consumotomado("" + parseStringToInteger(
                    Double.toString((variables.consumoactual
                            * Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Factormultipicacion().trim())))));
            infoRegistroSalida.escribir_TablaRegistroSalida(VariablesGlobales.registroactual);
        }

        if (parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Nrodemedidores()) > 1) {
            // mirar las otras opciones de donde tomar los valores a cobrar
            variables.evaluarConServicio(infoMedidorEntrada.gettablaMedidorEntrada_Tipomedidor().trim());
            evaluarCargaInstalada(variables.consumoactual);
            if (String.format("%1$1s", infoMedidorEntrada.gettablaMedidorEntrada_idcortado().trim()).equals("1")) {
                variables.consumoactual = Double.parseDouble(infoMedidorEntrada.gettablaMedidorEntrada_lecturacorte().trim());
            }

            if (Double.parseDouble(infoClienteSalida.gettablaClienteSalida_VALOR1().trim()) == 0
                    || (Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumo1().trim()) == 0
                    && Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumo2().trim()) == 0
                    && Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumo3().trim()) == 0
                    && Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumo4().trim()) == 0
                    && Long.parseLong(infoRegistroEntrada.gettablaRegistroDeEntrada_consumo5().trim()) == 0)) {

                infoClienteSalida
                        .settablaClienteSalida_valor1("" + parseStringToInteger(
                                Double.toString(
                                        Double.parseDouble(infoClienteSalida.gettablaClienteSalida_VALOR1()
                                                .trim())
                                                + (variables.consumoactual
                                                * Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Factormultipicacion().trim())))));
            } else {
                if (Double.parseDouble(infoClienteSalida.gettablaClienteSalida_VALOR2().trim()) == 0
                        || parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Nrodemedidores().trim()) == 2) {
                    if ((Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumo1().trim()) == 0
                            && Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumo2().trim()) == 0
                            && Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumo3().trim()) == 0
                            && Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_consumo4().trim()) == 0
                            && Long.parseLong(infoRegistroEntrada.gettablaRegistroDeEntrada_consumo5().trim()) == 0))
                        infoClienteSalida.settablaClienteSalida_valor1("" + parseStringToInteger(Double.toString(Double
                                .parseDouble(infoClienteSalida.gettablaClienteSalida_VALOR1().trim()) + (variables.consumoactual * Double
                                .parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Factormultipicacion().trim())))));
                    else
                        infoClienteSalida.settablaClienteSalida_valor2("" + parseStringToInteger(Double.toString(
                                (variables.consumoactual
                                        * Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Factormultipicacion().trim())))));
                } else
                    infoClienteSalida
                            .settablaClienteSalida_valor3("" + parseStringToInteger(Double
                                    .toString((variables.consumoactual * Double
                                            .parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Factormultipicacion().trim())))
                                    .trim()));
            }
        }

        if (Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2().trim()) > 0
                && Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO1().trim()) > 0) {

            if (Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO1().trim()) < Double
                    .parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2().trim()) / 2)
                infoClienteSalida.settablaClienteSalida_CONSUMOFACTURADO("" + parseStringToInteger("" + (variables.ejecutarAjusteUnidades(Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2().trim())))));
            else
                infoClienteSalida.settablaClienteSalida_CONSUMOFACTURADO("" + parseStringToInteger("" + (variables.ejecutarAjusteUnidades(Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO1().trim()) - ((Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2().trim()) / 2)) + Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2().trim())))));

        } else if (!infoRegistroEntrada.gettablaRegistroDeEntrada_MARCA().trim().equals("CT"))
            infoClienteSalida.settablaClienteSalida_CONSUMOFACTURADO("" + parseStringToInteger("" + (variables.ejecutarAjusteUnidades(Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMOFACTURADO().trim()) + (variables.consumoactual)))));
    }

    private int evaluarLecturaRegistro(int primero, int N_veces, double l_actual, String
            Estado, String lectura_act, double c_actual) {
        String UltimacuentaTres = "";

        try {
            int tresvecesdiferente = 0;
            int diales, contador_diales;
            double potencia = 0;
            long l_anterior, promedio, l_cadena;
            String cadena = "";
            int giro_del_registro;
            double diferencia;
            if (variables.nveces <= 0)
                if (infoRegistroSalida.gettablaRegistroSalida_INTENTOS().equals(" "))
                    variables.nveces = 0;
                else
                    variables.nveces = parseStringToInteger(infoRegistroSalida.gettablaRegistroSalida_INTENTOS());
            if (variables.nveces >= 9) {
                mensajeT("INTENTO DE LECTURAS LLEGO A SU LIMITE", msgMedio);
                //  Toast.makeText(getApplicationContext(), "INTENTO DE LECTURAS LLEGO A SU LIMITE", Toast.LENGTH_LONG).show();
                return (0);
            }

            String promedio1 = infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediocliente().trim();// variables.DESENCAPSULAR(infoRegistroEntrada.EREGISTCONSUMOPROMEDIOCLIENTE,
            // variables.CadenaEncapsulada)
            String lecturaAnterior1 = infoRegistroEntrada.gettablaRegistroDeEntrada_Lecturaanterior().trim().replace(",", ".");// variables.DESENCAPSULAR(infoRegistroEntrada.EREGISTLECTURAANTERIOR,
            // variables.CadenaEncapsulada);

            if (promedio1.trim().equals(""))
                promedio1 = "       0";

            if (lecturaAnterior1.trim().equals(""))
                lecturaAnterior1 = "       0";

            l_anterior = Long.parseLong("" + parseStringToInteger(lecturaAnterior1.trim()));
            //promedio = Long.parseLong(promedio1);
            promedio = Math.round(Double.parseDouble(promedio1.trim().replace(",", "."))) ;/// Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Factormultipicacion().trim()));
            if (variables.numdigitos == 0) {
                if (!infoRegistroEntrada.gettablaRegistroDeEntrada_Digitos().trim().equals(""))
                    diales = parseStringToInteger(infoRegistroEntrada.gettablaRegistroDeEntrada_Digitos());
                else
                    diales = 5;
            } else {
                diales = variables.numdigitos;
                if (infoRegistroSalida.gettablaRegistroSalida_INFORME().trim().equals(""))
                    infoRegistroSalida.settablaRegistroSalida_informe("CAMBIO DE DIGITOS " + diales);
            }
            if (diales == 0) {
                mensajeT("MAL NUMERO ENTEROS", msgMedio);
                // Toast.makeText(getApplicationContext(), "MAL NUMERO ENTEROS", Toast.LENGTH_LONG).show();
                diales = 6;
            }
            reporteEstadoCritica = "";
            potencia = 1;
            contador_diales = diales;
            while (contador_diales > 0) {
                contador_diales--;
                potencia = potencia * 10;
            }
            visualizarInformacionCliente(0);
            //diferencia = (double) 0;
            if (variables.nveces < 10) {
                if (lectura_act.length() == 0) // la lectura no debe ser cero
                {
                    cadena = "0";// lectura_act;
                } else {
                    if (variables.nveces <  9) {
                        variables.nveces++;
                    }
                    cadena = lectura_act;
                }
                primero = 0;
                // activar un sonido o un lec aun no
                l_cadena = Long.parseLong(cadena);
                variables.lactual = parseStringToDouble(cadena);

                if (infoRegistroSalida.gettablaRegistroSalida_LECTURAMODIFICADA1().trim().equals("") || infoRegistroSalida.gettablaRegistroSalida_LECTURAMODIFICADA1().trim().equals("0"))// variables.DESENCAPSULAR(infoRegistroSalida.SREGISTLECTURAMODIFICADA2,
                // variables.CadenaEncapsulada)
                {
                    infoRegistroSalida.settablaRegistroSalida_lecturamodificada1(String.format("%1$10s", cadena.trim()));
                    Log.e("error", "escribe 7");
                    escribirTablasSalida();
                } else {
                    if (infoRegistroSalida.gettablaRegistroSalida_LECTURAMODIFICADA2().trim().equals("") || infoRegistroSalida.gettablaRegistroSalida_LECTURAMODIFICADA2().trim().equals("0"))// variables.DESENCAPSULAR(infoRegistroSalida.SREGISTLECTURAMODIFICADA2,
                    // variables.CadenaEncapsulada)
                    {
                        //infoRegistroSalida.settablaRegistroSalida_lecturamodificada2(infoRegistroSalida.gettablaRegistroSalida_LECTURAMODIFICADA1().trim());
                        infoRegistroSalida.settablaRegistroSalida_lecturamodificada2(String.format("%1$10s", cadena.trim()));
                        Log.e("error", "escribe 8");
                        escribirTablasSalida();
                    }
                }

                // procurar que el consumo a evaluar sea correcto
                if (variables.lactual < l_anterior) {
                    variables.cactual = potencia + variables.lactual - l_anterior;
                } else {
                    variables.cactual = variables.lactual - l_anterior;
                }
                extraerPuntoDecimal();
                variables.consumoactual = variables.cactual;
                Log.e("error2", "consumo " + variables.consumoactual + "-" + promedio);
                int diferencia2 = 0;
                if (promedio > 0) {
                    Log.e("error2", "consumo " + variables.consumoactual + "-" + promedio);
                    diferencia2 = (int) ((variables.consumoactual * Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Factormultipicacion().trim())) - promedio);
                    diferencia2 = (int) (diferencia2 * 100 / promedio);
                } else {
                    diferencia2 = (int) (999999);
                }

                int estadoCritica = 0;

                Log.e("error2", "diferencia2 " + diferencia2);
                if (diferencia2 >= 0 && diferencia2 <= 30) {
                    estadoCritica = 1;
                    variables.impresoraAnalitica = "CONSUMONORMAL N";
                } else if (diferencia2 >= -50 && diferencia2 <= -0.5) {
                    variables.impresoraAnalitica = "CONSUMONORMAL N";
                    estadoCritica = 1;
                } else if (diferencia2 > 31) {
                    variables.impresoraAnalitica = "CONSUMOALTO N";
                    estadoCritica = 2;
                } else {
                    variables.impresoraAnalitica = "CONSUMOBAJO N";
                    estadoCritica = 3;
                }

                ultimaCriticaLectura = "  ";
                if (promedio > 0) {
                    if (misRangos.abrir_TablaRangos(misRangos.getArchivo_TablaRangos())) {
                        misRangos.setEncontro_TablaRangos(0);
                        misRangos.buscarSecuencial_TablaRangos(("" + diferencia2).trim());// variables.Consumo_actual.ToString().trim());
                        Log.e("error2", "critica 1 " + misRangos.getEncontro_TablaRangos());
                        if (misRangos.getEncontro_TablaRangos() >= 1) {
                            ultimaCriticaLectura = misRangos.gettablaRangos_CODRANGO();
                            estadoCritica = parseStringToInteger(misRangos.gettablaRangos_LEVE().trim());
                        }
                        misRangos.Cerrar_TablaRangos();
                    }
                } else {
                    //if (Variables.Lactual == l_anterior)
                    //    EstadoCritica = 2;
                    //else
                    Log.e("error2", "critica 2 " + variables.consumoactual);
                    if (variables.consumoactual > 2000)
                        estadoCritica = 2;
                    else
                        estadoCritica = 1;

                }


                // procurar que el consumo a evaluar sea correcto
                if (variables.lactual < l_anterior) {
                    // Regla SIEC (ciclo 11: 151 de 153 cuentas): lectura menor a la anterior => 1 mes de promedio.
                    // SIEC no liquida giro de registro.
                    if (indicadorManual == 0 && variables.nveces < 2) {        // el lector confirma una vez
                        variables.lect2 = variables.lactual;
                        mensajeT("LECTURA MENOR A LA ANTERIOR. CONFIRME", msgCorto);
                        imagenLiquid_3.setImageResource(R.drawable.flechasatras);
                        return 0;
                    }
                    variables.cactual = promedioMensualSinFactor();             // se multiplica por el factor en la sumatoria
                    variables.consumoactual = variables.cactual;
                    variables.impresoraAnalitica = "LECT. MENOR P1";
                    variables.estado = "8";
                    variables.lect2 = -1;
                    variables.lect3 = -1;
                    escribirTablasSalida();
                    return 1;
                }



                if (variables.lactual == l_anterior) {
                    if (variables.nveces < 9) {
                        variables.nveces++;
                    }

                    predio_temporal = 1;
                    if ((variables.lect2 == variables.lactual  && variables.nveces >= 2))// ||

                    {
                        reporteEstadoCritica = "LECTURA IGUALES ";
                        variables.estado = "2";
                        Log.e("error", "escribe 9");
                        escribirTablasSalida();

                        // nuevo para solo visualizar el mensaje
                        variables.consumoactual = 0;
                        imagenLiquid_3.setImageResource(android.R.color.transparent);

                        predio_temporal = VariablesGlobales.registroactual;
                        variables.lect2 = -1;
                        variables.lect3 = -1;

                        return (1);
                    } else {
                        imagenLiquid_3.setImageResource(R.drawable.imagen_lecturasiguales);

                        if (variables.lect2 == -1)
                            variables.lect2 = variables.lactual;
                        else {
                            variables.lect3 = variables.lect2;
                            variables.lect2 = variables.lactual;
                        }
                        return (0);
                    }
                }
                giro_del_registro = 0;
                if (variables.lactual < l_anterior) {
                    giro_del_registro = 1;
                    variables.lactual += potencia;
                }
                variables.cactual = variables.lactual - l_anterior;
                extraerPuntoDecimal();
                variables.consumoactual = variables.cactual;
                int estadosanteriores = 0;

                if (estadoCritica == 1 ) {
                    estadosanteriores = 1;

                    int correcto = 0;

                        correcto = 1;


                    if (correcto == 1) {
                        mensajeT("CONSUMO NORMAL!", msgCorto);
                        if (giro_del_registro != 0)
                            variables.lactual -= potencia;


                        variables.estado = "3";
                        variables.lect2 = -1;
                        variables.lect3 = -1;
                        imagenLiquid_3.setImageResource(android.R.color.transparent);

                        return (1);  // CONSUMO NORMAL
                    }

                } else if (estadoCritica == 3) {
                    variables.estado = "1"; // lectura muy baja
                    if (variables.lect2 == variables.lactual  || (variables.nveces >= 2)) {
                        reporteEstadoCritica = "CONSUMO BAJO";
                        Log.e("error", "escribe 10");
                        escribirTablasSalida();
                        variables.lect2 = -1;
                        variables.lect3 = -1;
                        imagenLiquid_3.setImageResource(android.R.color.transparent);

                        return (1);
                    } else {
                        imagenLiquid_3.setImageResource(R.drawable.imagen_consumobajo);
                    }
                    if (giro_del_registro > 0)
                        variables.lactual -= potencia;
                } else
                // mostrar las desviaciones graves
                {
                    Log.e("error2", "valor desv. grave 2 " + promedio + "-" + variables.consumoactual + "-" + variables.cactual);

                    reporteEstadoCritica = "CONSUMO ALTO";
                    mensajeT("DESV. GRAVE X DEBAJO!", msgCorto);
                  //  if (variables.cactual>2000) {
                  //      variables.cactual = promedioMensualSinFactor();

                  //      variables.consumoactual = variables.cactual;
                  //  }
                    if (estadoCritica == 2) {
                        variables.estado = "7";
                        mensajeT("DESV. MUY GRAVE X ENCIMA!", msgCorto);
                        reporteEstadoCritica = "CONSUMO ALTO";
                    }
                }

                if (estadoCritica == 2) {
                    imagenLiquid_3.setImageResource(R.drawable.imagen_consumoalto);

                    variables.estado = "7";
                } else if (estadoCritica == 3) {                     // Consumo Bajo lectura menor a la anterior
                    imagenLiquid_3.setImageResource(R.drawable.imagen_consumobajo);
                }

                if (variables.lect2 == variables.lactual  && (variables.nveces >= 2)) {
                    if (estadoCritica == 2) {
                        reporteEstadoCritica = "CONSUMO ALTO";
                        Log.e("error", "escribe 11");
                        escribirTablasSalida();
                        imagenLiquid_3.setImageResource(R.drawable.imagen_consumoalto);
                        if (infoRegistroEntrada.gettablaRegistroDeEntrada_tipoenergia().equals("R"))
                        {
                            variables.cactual =  (Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediocliente().trim())
                                                 * Double.parseDouble(infoClienteEntrada.gettablaEntradaClientes_Bimestral().trim()) / Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Factormultipicacion().trim()));
                        }

                    }
                    if (giro_del_registro > 0)
                        variables.lactual -= potencia;
                    if (variables.lactual < l_anterior)
                        variables.estado = "8"; // consumo negativo.... posible
                    // fraude

                    variables.lect2 = -1;
                    variables.lect3 = -1;

                    return (1);
                }
                if (lectura_act.length() > 0) {
                    if (variables.nveces >= 2) {

                        notificacion.setSmallIcon(android.R.drawable.stat_sys_warning);
                        notificacion.setContentTitle("Alerta");
                        notificacion.setTicker("Alerta!!!");
                        notificacion.setContentText("* ULTIMO INTENTO *");
                        nm.notify(NOTIF_ALERTA_ID, notificacion.build());
                        mensajeT("* ULTIMOS INTENTOS *", msgCorto);
                        //Toast.makeText(getApplicationContext(), "* ULTIMO INTENTO *", Toast.LENGTH_SHORT).show();

                    } else if (l_cadena < diales) {
                        mensajeT("REVISE NRO RUEDAS", msgCorto);
                        //  Toast.makeText(getApplicationContext(), "REVISE NRO RUEDAS", Toast.LENGTH_SHORT).show();

                        notificacion.setSmallIcon(android.R.drawable.stat_sys_warning);
                        notificacion.setContentTitle("Alerta");
                        notificacion.setTicker("Alerta!!!");
                        notificacion.setContentText("REVISE NRO RUEDAS");
                        nm.notify(NOTIF_ALERTA_ID, notificacion.build());

                    } else {
                        if (estadosanteriores == 1) {
                            mensajeT("VERIFIQUE  LECTURA POR CONSUMOS ANTERIORES ALTOS", msgLargo);
                        } else {
                            mensajeT("VERIFIQUE  LECTURA", msgCorto);
                        }
//                        notificacion.setSmallIcon(android.R.drawable.stat_sys_warning);
//                        notificacion.setContentTitle("Alerta");
//                        notificacion.setTicker("Alerta!!!");
//                        notificacion.setContentText("VERIFIQUE  LECTURA");
//                        nm.notify(NOTIF_ALERTA_ID, notificacion.build());
                    }
                }
                if (variables.lect2 == -1)
                    variables.lect2 = variables.lactual;
                else
                // if (variables.lect3 == -1)
                {
                    variables.lect3 = variables.lect2;
                    variables.lect2 = variables.lactual;
                }
            } else {
                return (1);
            }
        } catch (Exception e) {
            Log.e("error", "critica " + e.getMessage());
            mensajeT("Problemas en el procedimiento de Critica", msgLargo);
            // Toast.makeText(getApplicationContext(), "Problemas en el procedimiento de Critica", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        return 0;
    }
    private double promedioMensualSinFactor() {
        double prom = parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediocliente().trim());
        double factor = parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Factormultipicacion().trim());
        return factor > 0 ? prom / factor : prom;
    }
    //se modifica esta analitica para maximo dos intentos de lecturas la otra se deja igual
    private int evaluarLecturaAnalitica(int primero, int N_veces, double l_actual, String Estado, String lectura_act, double c_actual) {
        String UltimacuentaTres = "";
        int tresvecesdiferente = 0;
        int diales, contador_diales;
        double potencia = 0;
        long l_anterior, promedio, l_cadena;
        String cadena = "";
        int giro_del_registro;
        double diferencia;
        try {
            if (variables.nveces <= 0)
                if (infoRegistroSalida.gettablaRegistroSalida_INTENTOS().equals(" "))
                    variables.nveces = 0;
                else
                    variables.nveces = parseStringToInteger(infoRegistroSalida.gettablaRegistroSalida_INTENTOS());
            if (variables.nveces >= 9) {
                mensajeT("INTENTO DE LECTURAS LLEGO A SU LIMITE", msgMedio);
                //  Toast.makeText(getApplicationContext(), "INTENTO DE LECTURAS LLEGO A SU LIMITE", Toast.LENGTH_LONG).show();
                return (0);
            }

            String promedio1 = infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediocliente().trim();// variables.DESENCAPSULAR(infoRegistroEntrada.EREGISTCONSUMOPROMEDIOCLIENTE,
            // variables.CadenaEncapsulada)
            String lecturaAnterior1 = infoRegistroEntrada.gettablaRegistroDeEntrada_Lecturaanterior().trim().replace(",", ".");// variables.DESENCAPSULAR(infoRegistroEntrada.EREGISTLECTURAANTERIOR,
            // variables.CadenaEncapsulada);

            if (promedio1.trim().equals(""))
                promedio1 = "       0";

            if (lecturaAnterior1.trim().equals(""))
                lecturaAnterior1 = "       0";

            l_anterior = Long.parseLong("" + parseStringToInteger(lecturaAnterior1));
            promedio = Long.parseLong(promedio1);
            if (variables.numdigitos == 0) {
                if (!infoRegistroEntrada.gettablaRegistroDeEntrada_Digitos().trim().equals(""))
                    diales = parseStringToInteger(infoRegistroEntrada.gettablaRegistroDeEntrada_Digitos());
                else
                    diales = 5;
            } else {
                diales = variables.numdigitos;
                if (infoRegistroSalida.gettablaRegistroSalida_INFORME().trim().equals(""))
                    infoRegistroSalida.settablaRegistroSalida_informe("CAMBIO DE DIGITOS " + diales);
            }
            if (diales == 0) {
                mensajeT("MAL NUMERO ENTEROS", msgMedio);
                // Toast.makeText(getApplicationContext(), "MAL NUMERO ENTEROS", Toast.LENGTH_LONG).show();
                diales = 6;
            }
            reporteEstadoCritica = "";
            potencia = 1;
            contador_diales = diales;
            while (contador_diales > 0) {
                contador_diales--;
                potencia = potencia * 10;
            }
            visualizarInformacionCliente(0);
            //diferencia = (double) 0;
            if (variables.nveces < 10) {
                if (lectura_act.length() == 0) // la lectura no debe ser cero
                {
                    cadena = "0";// lectura_act;
                } else {
                    if (variables.nveces < 9) {
                        variables.nveces++;
                    }
                    cadena = lectura_act;
                }
                primero = 0;
                // activar un sonido o un lec aun no
                l_cadena = Long.parseLong(cadena);
                variables.lactual = parseStringToDouble(cadena);


                if (infoRegistroSalida.gettablaRegistroSalida_LECTURAMODIFICADA1().trim().equals("") || infoRegistroSalida.gettablaRegistroSalida_LECTURAMODIFICADA1().trim().equals("0"))// variables.DESENCAPSULAR(infoRegistroSalida.SREGISTLECTURAMODIFICADA2,
                // variables.CadenaEncapsulada)
                {
                    infoRegistroSalida.settablaRegistroSalida_lecturamodificada1(String.format("%1$10s", cadena.trim()));
                    Log.e("error", "escribe 7");
                    escribirTablasSalida();
                } else if (infoRegistroSalida.gettablaRegistroSalida_LECTURAMODIFICADA2().trim().equals("") || infoRegistroSalida.gettablaRegistroSalida_LECTURAMODIFICADA2().trim().equals("0"))// variables.DESENCAPSULAR(infoRegistroSalida.SREGISTLECTURAMODIFICADA2,
                // variables.CadenaEncapsulada)
                {
                    //infoRegistroSalida.settablaRegistroSalida_lecturamodificada2(infoRegistroSalida.gettablaRegistroSalida_LECTURAMODIFICADA1().trim());
                    infoRegistroSalida.settablaRegistroSalida_lecturamodificada2(String.format("%1$10s", cadena.trim()));
                    Log.e("error", "escribe 8");
                    escribirTablasSalida();
                }


                // procurar que el consumo a evaluar sea correcto
                if (variables.lactual < l_anterior) {
                    variables.cactual = potencia + variables.lactual - l_anterior;
                } else {
                    variables.cactual = variables.lactual - l_anterior;
                }

                extraerPuntoDecimal();


                variables.consumoactual = variables.cactual;
                Log.e("error2", "consumo " + variables.consumoactual + "-" + promedio);

                int diferencia2 = 0;
                Log.e("error2", "consumo " + variables.consumoactual + "-" + promedio);
                int diasAnalitica = (int) (variables.calcularNumerodeDias(infoMedidorEntrada.gettablaMedidorEntrada_Fechalectanterior().substring(6, 10) + infoMedidorEntrada.gettablaMedidorEntrada_Fechalectanterior().substring(3, 5) + infoMedidorEntrada.gettablaMedidorEntrada_Fechalectanterior().substring(0, 2)));
                diasAnalitica = variables.diashoy - diasAnalitica;


                diferencia2 = (int) ((variables.consumoactual * Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Factormultipicacion().trim())));// - promedio
                variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";diferencia ;" + formateo(diferencia2) + "- dias " + diasAnalitica;
                escribeResumenTiempo(variableResumenLiquidacion);
                if (diasAnalitica > 0) {
                    /*diferencia = diferencia2 / diasAnalitica;
                    diferencia = diferencia * 30;*/

                    // ESTO ES PARA CALCULAR LA DIFERENCIA EN LLANO si tien e sentido listo hago prueba
                    //Aqui difvide teniendo en cuenta los decimales y los ajusta a 2 despues de realizar la operacion
                    //diasAnalitica este dato se cambiaria a los 30 dias normales
                    diasAnalitica = 30;
                    BigDecimal consumoDivididoDias = new BigDecimal(diferencia2).divide(new BigDecimal(diasAnalitica), 2, RoundingMode.HALF_UP);
                    BigDecimal result = consumoDivididoDias.multiply(new BigDecimal(30));//Aqui multplica por 30
                    diferencia = result.doubleValue();
                    Log.e("INFO", " CALCULO A 30 dias: " + diferencia);
                    //result vendria siendo lo que usted llama diferencia
                    // se recompone la analitica en elconsumo dia todo serapor 30dias

                } else {
                    diferencia = diferencia2;
                }
//DIFERENCIA ES IGUAL A LA REGLA DEL CONSUMO POR DIA MULTIPLICADO POR 30 DIAS

                variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";diferencia cambio ;" + formateo(diferencia) + "- dias " + diasAnalitica;
                escribeResumenTiempo(variableResumenLiquidacion);

                int estadoCritica = 0;

                    /*Algoritmo de sitio
                    0. Las que no tienen diferencia de lectura no se hace análisis de desviación
                    1. Se toma la lectura
                    2. si es estacional, o es nueva, cobrar como está en el campo de tipoDesviación será 'N'
                    2.1 Si tiene una investigación pendiente se debe cobrar por promedio: En el campo tipoDesviación 'P'
                    3. si está fuera de la resolución, pero está dentro de la validación anual o dentro del rango de
                    subsistencia y residencialidad facturar con una observación que signifique que no no está desviada
                    por el análisis de la empresa, Dejar nota que stá desviado pero por análisis de la empresa no se va a estudiar
                    4. Si está fuera de la resolución, también fuera de la analítica de la empresa, imprimir el recibo con
                    observación de desviacion y cobro por promedio (promerio normalizado), además se debe imprimir una
                    notificación para dejarle al usuario indicando que la cuenta va a ser visitada
                    5. Finalmente se imprime la factura con toma exitosa y se carga al SIEC.*/

                double lsTpoUs = parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_LS_BDA_TPO_US().trim());
                double lsAnual = parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_LS_BNDA_ANUAL().trim());

                Log.e("error2", "diferencia2 " + diferencia);
                if ((diferencia >= parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_LI_BNDA_RES().trim())
                        && diferencia <= parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_LS_BNDA_RES().trim())) && diferencia != 0) {
                    estadoCritica = 1;

                    variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";entro por normal ;" + formateo(diferencia) + "- con estos datos " +
                            infoRegistroEntrada.gettablaRegistroDeEntrada_LI_BNDA_RES().trim() + "--" + infoRegistroEntrada.gettablaRegistroDeEntrada_LS_BNDA_RES().trim();
                    escribeResumenTiempo(variableResumenLiquidacion);

                    //por que entra por aqui

                    variables.impresoraAnalitica = "CONSUMONORMAL R";
                    //nuevo marcar para que procedamos con el comentario 2 la nueva novedad del sistema;
                    infoRegistroSalida.settablaRegistroSalida_comentario2("000");
                    if (infoRegistroEntrada.gettablaRegistroDeEntrada_LQDA_CSMO().trim().equals("P")) {
                        infoRegistroSalida.settablaRegistroSalida_comentario2("912");
                    }
                    escribirTablasSalida();
                } else if (diferencia > parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_LS_BNDA_RES().trim())) {
                    variables.impresoraAnalitica = "CONS. ALTO DV1";
                    infoRegistroSalida.settablaRegistroSalida_comentario2("001");
                    //escribirTablasSalida();
                    if (diferencia > parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_LS_CONS_SUBS()) &&
                            parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_LS_CONS_SUBS().trim()) > 0) {
                        infoRegistroSalida.settablaRegistroSalida_comentario2("005");
                        variables.impresoraAnalitica = "CONS. ALTO Da5";

                        //diferencia > parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_LS_BDA_TPO_US().trim()))
                        if (infoRegistroEntrada.gettablaRegistroDeEntrada_CMBIO_CLSE().equals("0") &&
                                infoRegistroEntrada.gettablaRegistroDeEntrada_CMBIO_NVEL().equals("0") &&
                                infoRegistroEntrada.gettablaRegistroDeEntrada_NRMLZACION().equals("0")) {
                            infoRegistroSalida.settablaRegistroSalida_comentario2("005");
                            variables.impresoraAnalitica = "CONS. ALTO Db4";

                            //SERIA INCLUIR AQUI LA TERCERA DELIMITACION Y ES SI SUPERA EL 200 POR CIENTO DEL PROMEDIO ANUAL REPORTADO EN PROMEDIO NORMALIZADO
                            //se le quita el subir el 200% del promedio ya que se les presentaban muchas criticas * 2
                            //if (parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_PRMDIO().trim()) == 0) {
                            if (diferencia > lsTpoUs && lsTpoUs > 0 && diferencia > lsAnual) {
                                infoRegistroSalida.settablaRegistroSalida_comentario2("003");
                                variables.impresoraAnalitica = "CONS. ALTO DV3";
                                //toca consultar porque aqui en el consumo alto se cobra el promedio de uun mes para los trimestrales
                                //variables.consumoactual = (Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediocliente().trim()) / Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Factormultipicacion().trim()));// - promedio
                                //NIEVO CAMBIO PARECE QUE AQUI AL CRITICAR LOS INVERTICOS EN OCASIONES HACE LA MULTIPLICACION POR LA PERIOSIDAD PERO AQUI HAY QUE MIRAR SI ES SIMPRE
                                variables.cactual = (Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediocliente().trim())
                                        *Double.parseDouble(infoClienteEntrada.gettablaEntradaClientes_Bimestral().trim()) / Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Factormultipicacion().trim()));// - promedio

//
//                                variables.consumoactual = (Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_PRMDIO().trim()) / Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Factormultipicacion().trim()));// - promedio
                            }


                        } else {
                            variables.impresoraAnalitica = "CONS. ALTO DV2";
                            infoRegistroSalida.settablaRegistroSalida_comentario2("002");
                            //hay que hacer algo si el consumo real es demacisdo alto porque tabbine veo que no cobra como maximo 2000 kb, toca buscar que es lo que se hace && Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_PRMDIO().trim()) == 0
                            if ((diferencia > (2000 * parseStringToInteger(infoClienteEntrada.gettablaEntradaClientes_Bimestral().trim())))  )
                            {
                                //deberia de cambiar este concumo aqui QUITAR POR AHORA Y VALIDAR
                               // variables.cactual =  (Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediocliente().trim())
                               //         * Double.parseDouble(infoClienteEntrada.gettablaEntradaClientes_Bimestral().trim()) / Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Factormultipicacion().trim()));
                            }
                            else
                            {
                                //que pasa en este caso y encontar las diferencias que hace el sistema realmente

                            }
                        }

                    }
                    if (infoRegistroEntrada.gettablaRegistroDeEntrada_LQDA_CSMO().trim().equals("P")) {
                        infoRegistroSalida.settablaRegistroSalida_comentario2("913");
                    }
                    escribirTablasSalida();
                    estadoCritica = 2;
                } else {

                    infoRegistroSalida.settablaRegistroSalida_comentario2("010");

                    if (diferencia2 > parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_LI_BANDA_ANUAL().trim())) {
                        variables.impresoraAnalitica = "CONS. BAJOR DV2";
                        infoRegistroSalida.settablaRegistroSalida_comentario2("011");
                        if (diferencia2 > parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_LS_CONS_SUBS().trim())) {
                            if (parseStringToDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_LS_CONS_SUBS().trim()) > 0) {
                                infoRegistroSalida.settablaRegistroSalida_comentario2("012");
                            } else {
                                infoRegistroSalida.settablaRegistroSalida_comentario2("013");
                            }
                            variables.impresoraAnalitica = "CONS. BAJOR DV3";

                        }

                    }

                    if (infoRegistroEntrada.gettablaRegistroDeEntrada_LQDA_CSMO().trim().equals("P")) {
                        infoRegistroSalida.settablaRegistroSalida_comentario2("914");
                    }

                    escribirTablasSalida();
                    variables.impresoraAnalitica = "CONS. BAJOR DV1";
                    estadoCritica = 3;
                }


                ultimaCriticaLectura = "  ";
                //este proceso aqui se iria del la seccion
                //creeria que si querevos ver el efecto de invertido asi el consumo sea correcto se retorna para el cobro por promedio
                // procurar que el consumo a evaluar sea correcto

                if (variables.lactual < l_anterior) {
                    imagenLiquid_3.setImageResource(R.drawable.flechasprim);
                    // Regla SIEC (ciclo 11: 151 de 153 cuentas): lectura menor a la anterior => 1 mes de promedio.
                    // SIEC no liquida giro de registro.
                    if (indicadorManual == 0 && variables.nveces < 2) {        // el lector confirma una vez
                        variables.lect2 = variables.lactual;
                        mensajeT("LECTURA MENOR A LA ANTERIOR. CONFIRME", msgCorto);

                        return 0;
                    }
                    variables.cactual = promedioMensualSinFactor();             // se multiplica por el factor en la sumatoria
                    variables.consumoactual = variables.cactual;
                    variables.impresoraAnalitica = "LECT. MENOR P1";
                    variables.estado = "8";
                    variables.lect2 = -1;
                    variables.lect3 = -1;
                    escribirTablasSalida();
                    return 1;
                }
                //fin de lo que primero se quitaria

                if (variables.lactual == l_anterior) {
                    if (variables.nveces < 9) {
                        variables.nveces++;
                    }
                    predio_temporal = 1;
                    if ((variables.lect2 == variables.lactual  && variables.nveces >= 2))// ||
                    {
                        reporteEstadoCritica = "LECTURA IGUALES ";
                        variables.estado = "2";
                        Log.e("error", "escribe 9");
                        variables.impresoraAnalitica = "CONS. CERO DV1";
                        infoRegistroSalida.settablaRegistroSalida_comentario2("001");
                        escribirTablasSalida();

                        // nuevo para solo visualizar el mensaje
                        variables.consumoactual = 0;
                        imagenLiquid_3.setImageResource(android.R.color.transparent);

                        predio_temporal = VariablesGlobales.registroactual;
                        variables.lect2 = -1;
                        variables.lect3 = -1;

                        return (1);
                    } else {
                        imagenLiquid_3.setImageResource(R.drawable.imagen_lecturasiguales);

                        if (variables.lect2 == -1)
                            variables.lect2 = variables.lactual;
                        else {
                            //variables.lect3 = variables.lect2;
                            variables.lect2 = variables.lactual;
                        }
                        return (0);
                    }
                }
                giro_del_registro = 0;
                if (variables.lactual < l_anterior) {
                    giro_del_registro = 1;
                    variables.lactual += potencia;
                }
               // variables.cactual = variables.lactual - l_anterior;
                extraerPuntoDecimal();
                variables.consumoactual = variables.cactual;
                int estadosanteriores = 0;




                if (estadoCritica == 1) {
                    estadosanteriores = 1;

                    int correcto = 0;
                    correcto = 1;

                    if (correcto == 1) {
                        mensajeT("CONSUMO NORMAL!", msgCorto);
                        if (giro_del_registro != 0)
                            variables.lactual -= potencia;
                        variables.estado = "3";
                        variables.lect2 = -1;
                        variables.lect3 = -1;
                        imagenLiquid_3.setImageResource(android.R.color.transparent);

                        return (1);  // CONSUMO NORMAL
                    }

                } else if (estadoCritica == 3) {
                    variables.estado = "1"; // lectura muy baja
                    if ((variables.lect2 == variables.lactual)  || (variables.nveces >= 2)) {//&& variables.lect3 == variables.lactual)
                        reporteEstadoCritica = "CONSUMO BAJO";
                        Log.e("error", "escribe 10");
                        escribirTablasSalida();
                        variables.lect2 = -1;
                        variables.lect3 = -1;
                        imagenLiquid_3.setImageResource(android.R.color.transparent);

                        return (1);
                    } else {
                        imagenLiquid_3.setImageResource(R.drawable.imagen_consumobajo);
                    }
                    if (giro_del_registro > 0)
                        variables.lactual -= potencia;
                } else
                // mostrar las desviaciones graves
                {
                    Log.e("error2", "valor desv. grave 2 " + promedio + "-" + variables.consumoactual + "-" + variables.cactual);

                    reporteEstadoCritica = "CONSUMO ALTO";
                    if (giro_del_registro > 0)
                        mensajeT("DESV. GRAVE X DEBAJO! GIRO MED.", msgCorto);

                    if (estadoCritica == 2 && giro_del_registro == 0) {
                        variables.estado = "7";
                        mensajeT("DESV. MUY GRAVE X ENCIMA!", msgCorto);
                        reporteEstadoCritica = "CONSUMO ALTO";
                    }
                }

                if (estadoCritica == 2) {
                    imagenLiquid_3.setImageResource(R.drawable.imagen_consumoalto);

                    variables.estado = "7";
                } else if (estadoCritica == 3) {                     // Consumo Bajo lectura menor a la anterior
                    imagenLiquid_3.setImageResource(R.drawable.imagen_consumobajo);
                }

                if ((variables.lect2 == variables.lactual ) && (variables.nveces >= 2)) {//&& variables.lect3 == variables.lactual
                    if (estadoCritica == 2) {
                        reporteEstadoCritica = "CONSUMO ALTO";
                        Log.e("error", "escribe 11");
                        escribirTablasSalida();
                        imagenLiquid_3.setImageResource(R.drawable.imagen_consumoalto);
                    }
                    if (giro_del_registro > 0)
                        variables.lactual -= potencia;

                    if (variables.lactual < l_anterior)
                        variables.estado = "8"; // consumo negativo.... posible
                    // fraude

                    if (infoRegistroEntrada.gettablaRegistroDeEntrada_LQDA_CSMO().equals("P") ||
                            infoRegistroSalida.gettablaRegistroSalida_COMENTARIO2().equals("003")) {
                        Log.e("error vamos a cobrar promedio ", "Promedio para cobrar es" + infoRegistroEntrada.gettablaRegistroDeEntrada_promedioNormalizado().trim());
                        //a esto le falta algo ya que teemos otra variacion en observar como se comporta la trimestral
                        //preguntar aqui si aqui se cobra como minimo es un mes del promedio real
                        variables.consumoactual = (Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Consumopromediocliente().trim())
                                * Double.parseDouble(infoClienteEntrada.gettablaEntradaClientes_Bimestral().trim())
                                / Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Factormultipicacion().trim()));// - promedio
                        //variables.consumoactual = (Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_PRMDIO().trim()) / Double.parseDouble(infoRegistroEntrada.gettablaRegistroDeEntrada_Factormultipicacion().trim()));// - promedio

                    }
                    variables.lect2 = -1;
                    variables.lect3 = -1;

                    return (1);
                }
                if (lectura_act.length() > 0) {
                    if (variables.nveces >= 3) {

                        notificacion.setSmallIcon(android.R.drawable.stat_sys_warning);
                        notificacion.setContentTitle("Alerta");
                        notificacion.setTicker("Alerta!!!");
                        notificacion.setContentText("* ULTIMO INTENTO *");
                        nm.notify(NOTIF_ALERTA_ID, notificacion.build());
                        mensajeT("* ULTIMOS INTENTOS *", msgCorto);
                        //Toast.makeText(getApplicationContext(), "* ULTIMO INTENTO *", Toast.LENGTH_SHORT).show();

                    } else if (l_cadena < diales) {
                        mensajeT("REVISE NRO RUEDAS", msgCorto);
                        //  Toast.makeText(getApplicationContext(), "REVISE NRO RUEDAS", Toast.LENGTH_SHORT).show();

                        notificacion.setSmallIcon(android.R.drawable.stat_sys_warning);
                        notificacion.setContentTitle("Alerta");
                        notificacion.setTicker("Alerta!!!");
                        notificacion.setContentText("REVISE NRO RUEDAS");
                        nm.notify(NOTIF_ALERTA_ID, notificacion.build());

                    } else {
                        if (estadosanteriores == 1) {
                            mensajeT("VERIFIQUE  LECTURA POR CONSUMOS ANTERIORES ALTOS", msgLargo);
                        } else {
                            mensajeT("VERIFIQUE  LECTURA", msgCorto);
                        }
                    }
                }
                if (variables.lect2 == -1)
                    variables.lect2 = variables.lactual;
                else
                // if (variables.lect3 == -1)
                {
                   // variables.lect3 = variables.lect2;
                    variables.lect2 = variables.lactual;
                }
            } else {
                return (1);
            }
        } catch (Exception e) {
            Log.e("error", "critica " + e.getMessage());
            mensajeT("Problemas en el procedimiento de Critica", msgLargo);
            e.printStackTrace();
        }
        return 0;
    }


    private void extraerPuntoDecimal() {

        String cadena = ("" + variables.cactual).trim();
        short pos = -1;
        for (short i = 0; i < cadena.length(); i++) {
            if (cadena.substring(i, i + 1).equals(",")) {
                pos = i;
                i = (short) cadena.length();
            }
        }
        if (pos >= 0) {
            cadena = cadena.substring(0, pos + 2) + '\0';
            variables.cactual = parseStringToDouble(cadena);
        }
    }

//    private int procesarEntregaFacturas(int tipoEntrega, int numero) {
//
//        variables.ultimoregistro = VariablesGlobales.registroactual;
//        VariablesGlobales.registroactual = numero;
//        leerInformacionUsuario(1);
//
//        if (infoRegistroSalida.gettablaRegistroSalida_CAUSADENOLECTURA().trim().equals("")) {
//            infoRegistroSalida.settablaRegistroSalida_causadenolectura("99");
//        }
//
//        infoRegistroSalida.settablaRegistroSalida_lecturatomada("0000000000");
//
//        tomarFechaSistema("", "");
//        infoClienteSalida.settablaClienteSalida_FECHAIMPRESION(variables.amd);
//        infoClienteSalida.settablaClienteSalida_HORAIMPRESION(variables.hm);
//        infoRegistroSalida.settablaRegistroSalida_fechalectura(variables.amd);
//        infoRegistroSalida.settablaRegistroSalida_horalectura(variables.hm);
//        medidorSalida.settablaContadorSalida_Fechalectura(variables.amd);
//        medidorSalida.settablaContadorSalida_HORALECTURA(variables.hm);
//
//        infoClienteSalida.settablaClienteSalida_TERMINAL(terminal);
//
//        variables.estado = "5";
//        infoRegistroSalida.settablaRegistroSalida_leido(variables.estado);
//
//        infoRegistroSalida.settablaRegistroSalida_lecturatomada(String.format("%1$10s", infoRegistroSalida.gettablaRegistroSalida_LECTURATOMADA().trim()));
//        variables.estado = "5";
//        variables.nveces++;
//
//        infoRegistroSalida.settablaRegistroSalida_intentos(("" + variables.nveces).trim());
//
//        ultimaLatitud = txtLatitud.getText().toString();
//        ultimaLongitud = txtLongitud.getText().toString();
//
//        infoRegistroSalida.settablaRegistroSalida_leido(variables.estado);
//
//        infoClienteSalida.settablaClienteSalida_LECTOR(lector);
//        infoClienteSalida.settablaClienteSalida_TERMINAL(variables.estado);
//        infoClienteSalida.settablaClienteSalida_IMPRESORA(impresora);
//
//        escribirTablasSalida();
//        leerInformacionUsuario(1);
//
//        return 1;
//    }

    private boolean autorizacion() {

        Date dt = new Date();
        SimpleDateFormat df = new SimpleDateFormat("HHmm");
        String horaPuntual = df.format(dt.getTime());

        // identificar el numero de cuentas a validar antes de las 9:00
        int cuentasleidas = 1;

        if (VariablesGlobales.getActivar_validar_horario().trim().equals("0")) {
            return true;
        }

        if (infoClienteEntrada.gettablaEntradaClientes_Nombre().substring(47, 48).equals("U"))
            cuentasleidas = 10;

        if (VariablesGlobales.totalprediosleidos < cuentasleidas && parseStringToInteger(horaPuntual) > 900) {
            File file = new File(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/LEYOMASDE10.TXT");

            if (file.exists()) {
                return true;
            }
            try {
                SimpleDateFormat df2 = new SimpleDateFormat("yyyyMMdd");
                String fecha = df2.format(dt.getTime());

                File archivoAdmin2 = new File(VariablesGlobales.directorioactual + "/DatosDeEntrada/LECTURAADMINISTRADA.TXT");

                if (!archivoAdmin2.exists()) {
                    mensajeT("No Tiene Autorizacion\n Para trabajar en este Horario... \n Consulte con el Administrador", msgMedio);
                    // Toast.makeText(getApplicationContext(), "No Tiene Autorizacion\n Para trabajar en este Horario... \n Consulte con el Administrador", Toast.LENGTH_LONG).show();
                    return false;
                } else {
                    if (archivoAdmin2.exists()) {
                        RandomAccessFile archivoAdmin = new RandomAccessFile(archivoAdmin2, "r");
                        byte[] byteArray;
                        int fileSize = (int) archivoAdmin.length();
                        byteArray = new byte[fileSize];
                        archivoAdmin.readFully(byteArray, 0, fileSize);
                        String datosLeido = new String(byteArray);
                        archivoAdmin.close();

                        if (Double.parseDouble(datosLeido) == Double.parseDouble(fecha))
                            return true;
                        else {
                            mensajeT("Terminal con Autorizacion\n Desactualizada favor... \nAutorizar de nuevo", msgMedio);
                            // Toast.makeText(getApplicationContext(), "Terminal con Autorizacion\n Desactualizada favor... \nAutorizar de nuevo", Toast.LENGTH_LONG).show();
                            return false;
                        }
                    }
                }
            } catch (Exception e) {
                mensajeT("Problemas en Validacion de Horario Permitido", msgMedio);
                // Toast.makeText(getApplicationContext(), "Problemas en Validacion de Horario Permitido", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * Funcion que se ejecuta cuando concluye el intent en el que se solicita
     * una imagen ya sea de la c�mara o de la galeria o cuando concluye el
     * intent del BarCode Scanner
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_PICTURE) {//Respuesta Tomar fotos en liq.

            if (resultCode == RESULT_OK) {

                if (data != null) {

                    if (data.hasExtra("output")) {
                        name = data.getParcelableExtra("output");
                    }
                }
                new MediaScannerConnectionClient() {
                    private MediaScannerConnection msc = null;

                    {
                        msc = new MediaScannerConnection(getApplicationContext(), this);
                        msc.connect();
                    }

                    public void onMediaScannerConnected() {
                        msc.scanFile(name, null);
                    }

                    public void onScanCompleted(String path, Uri uri) {
                        //Log.d("heightDiff C", "uri:" + uri + " ,path: " + path);
                        msc.disconnect();
                    }
                };
                vecesImagen = 0;
                exitoImagen = 1;
                //AvanzarLecturas();
                ejecutarProcesoDeFotoII(vecesImagen);//Ax: idealmente se tomo la foto

            } else {//Ax: No se tomo foto
                Log.e("error", "resul foto nook " + vecesImagen);
                if (vecesImagen > 0) {
                    mensajeT("No se tomo foto, pruebe de nuevo", msgMedio);
                    // Toast.makeText(getApplicationContext(), "No se tomo foto, pruebe de nuevo", Toast.LENGTH_LONG).show();
                }
                //Log.d("heightDiff C", "No se tomo foto, veces:" + vecesImagen);
                vecesImagen = 1;
                ejecutarProcesoDeFotoII(vecesImagen);
            }
        } else if (requestCode == IntentIntegrator.REQUEST_CODE) {//Respuesta scaner camara

            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (scanResult != null) {
                Log.e("error", "request");
                String barcode = null;
                barcode = scanResult.getContents();

                if (barcode != null) {
                    mensajeT("Codigo leido: " + barcode, msgCorto);

                    if (verificaBarras) {//Fue llamado desde XXX   para verificar barras impresas en Rural.
                        verificaBarrasRural(barcode);
                    }
//                    else {
//                        setScannedInfo(barcode);//no se usa enerca
//                    }
                } else {
                    mensajeT("No se obtuvo ningun codigo...", msgCorto);
                }
            } else {
                mensajeT("No se obtuvo ningun codigo...", msgCorto);
            }
        } else if (requestCode == SCANNER_TECHDATA_REQUEST_CODE) {//Respuesta scaner techdata

            if (resultCode == RESULT_OK && data != null) {

                String barcode = data.getStringExtra("barcode");

                if (barcode != null || !barcode.isEmpty()) {

                    mensajeT("Codigo leido: " + barcode, msgCorto);

                    if (verificaBarras) {//Fue llamado desde XXX   para verificar barras impresas en Rural.
                        verificaBarrasRural(barcode);
                    }

                } else {
                    mensajeT("No se obtuvo ningun codigo...", msgCorto);
                }
            } else {
                mensajeT("No se obtuvo ningun codigo...", msgCorto);
            }
        } else if (requestCode == VariablesGlobales.REQUEST_CONNECT_DEVICE) {

            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {

                VariablesGlobales.habilitadaimpresora = 1;
                if (EsImpresora521 == 1)
                    imagenPrinter.setImageResource(R.drawable.impresora_ok521);//.impresora_ok);
                else
                    imagenPrinter.setImageResource(R.drawable.impresora_ok420);//.impresora_ok);
                //imagenPrinter.setImageResource(R.drawable.impresora_ok);

                addLog("resultCode==OK");
                // Get the device MAC address
                String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                String mensajedes = data.getExtras().getString(DeviceListActivity.MENSAJE_DESASOCIAR);
                //addLog("onActivityResult: got device=" + address);
                // Get the BLuetoothDevice object

                if (mensajedes.equals("SI")) {
                    Desasociar();
                } else {
                    BluetoothDevice device = VariablesGlobales.mBluetoothAdapter.getRemoteDevice(address);
                    VariablesGlobales.printerMacAddress = device.getAddress();

                    // Attempt to connect to the device
                    addLog("onActivityResult: connecting device...");
                    connectToDevice(device);
                }
            }
            VariablesGlobales.bDiscoveryStarted = false;
        } else if (requestCode == VariablesGlobales.REQUEST_ENABLE_BT) {

            addLog("requestCode==REQUEST_ENABLE_BT");
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                setupComm();
            } else {
                // User did not enable Bluetooth or an error occured
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (requestCode == CAPT_COMENTARIO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                abrirArchivosDeFacturacion();//Ax refresca los datos actuales que deberia hacer en [CaptCom01] para poder saber si se metio algun codigo de comentario
                infoRegistroSalida.lectura_TablaRegistroSalida(VariablesGlobales.registroactual);
                cerrarArchivosFacturacion();
            }
        } else if (requestCode == ANOMALIA_REQUEST_CODE) {
            medidorCero = false;
            if (resultCode == RESULT_OK) {

                causal = data.getStringExtra("snumero"); //Recupera el numero de anomalia

                if (anomaliaDeLectura.abrir_AnomaliaDeNoLectura(anomaliaDeLectura.getArchivo_AnomaliaDeNoLectura())) {
                    anomaliaDeLectura.buscarbinario_AnomaliaDeNoLectura(causal);
                    anomaliaDeLectura.Cerrar_AnomaliaDeNoLectura();
                }
                if (anomaliaDeLectura.getEncontro_AnomaliaDeNoLectura() == 0) {
                    causal = "";
                    mensajeT("Anomalia no existe en la Tabla\n" + causal, msgCorto);
                    // Toast.makeText(getApplicationContext(), "Anomalia no existe en la Tabla\n" + causal, Toast.LENGTH_LONG).show();
                } else {
                    // Ax: se ejecuta el alertdialog asincrono para que el codigo continue y salga de onActivityResult y se pueda ver la vista this
                    MostrarAlertDialog("Causas no lectura", "Acepta Anomalia seleccionada?\n" + causal + " " + anomaliaDeLectura.getanomaliaDeNoLectura_DESCRIPCION(), "ProcesarAnomalias");
                }
            }
        } else if (requestCode == BUSQUEDA_REQUEST_CODE) {

            if (resultCode == RESULT_OK && data != null) {
                try {
                    VariablesGlobales.datodebusqueda = data.getStringExtra("idcliente");

                    VariablesGlobales.tipodebusqueda = Integer.parseInt(data.getStringExtra("opcion"));

                    if (!VariablesGlobales.datodebusqueda.equals("")) // realizar Busqueda
                        buscarCuentaMedidor(VariablesGlobales.datodebusqueda.trim(), 0);
                } catch (Exception ex) {
                    utils.Log(logfile, "[MenuDeLiquidacion]BUSQUEDA_REQUEST_CODE. " + ex.toString());
                }
            }

        } else if (requestCode == ADICIONARNOVDAD_REQUEST_CODE) {//Modulo de Captura de Novedad de Lect

            if (resultCode == RESULT_OK && data != null) {//7855

                try {
                    mensajeNovedad = "";// Ax: anterior: envioHilosNovedades();

                    envioHilos_CertPostal_Censo_Aforos_Novedades(false, true, false, false, false); // Se envian "novedades", "aforos"

                } catch (Exception ex) {
                    utils.Log(logfile, "[MenuDeLiquidacion]ADICIONARNOVDAD_REQUEST_CODE. " + ex.toString());
                }
            }

            VariablesGlobales.opcionmenuseleccion = 0;//**-*

        } else if (requestCode == CONSULTNONVIADS_REQUEST_CODE) {//Modulo de ConsultaNoEnviados
            // Log.e("error", "CONSULTNONVIADS_REQUEST_CODE " + data);

            if (resultCode == RESULT_OK && data != null) {

                try {
                    //esto si vamos a registrar si la foto se tomo o no se tomo
                    abrirArchivosDeFacturacion();
                    ProbarWS();
                    //  Log.e("error", "trata envia. ");

                } catch (Exception ex) {
                    Log.e("error", "no envia " + ex.getMessage());
                    utils.Log(logfile, "[MenuDeLiquidacion]CONSULTNONVIADS_REQUEST_CODE. " + ex.toString());
                }
            }

        } else if (requestCode == SELECCIONDELOTE_REQUEST_CODE) {//Modulo de Seleccion de lote

            if (resultCode == RESULT_OK && data != null) {

                try {
                    if (VariablesGlobales.rangoinicialimpresion > 0 && VariablesGlobales.rangofinalimpresion > 0) {
                        // realizar proceso de impresion en lote
                        abrirArchivosDeFacturacion();

                        MostrarAlertDialog("IMPRIMIR RESUMEN", "Este Proceso Imprime el resumen\n" + "Clientes ya Facturados.\n" +
                                "Desea Continuar ?\n" + VariablesGlobales.rangoinicialimpresion + " al " + VariablesGlobales.rangofinalimpresion, "ImpresionLote");
                    }
                } catch (Exception ex) {
                    utils.Log(logfile, "[MenuDeLiquidacion]SELECCIONDELOTE_REQUEST_CODE. " + ex.toString());
                }
            }
        } else if (requestCode == ADICINRVERFCION_REQUEST_CODE) {//Modulo de Seleccion de adicion de verificacion (AuditoriaSupervisor)

            if (resultCode == RESULT_OK && data != null) {

                try {
                    if (contadorCuentasGPS_346 == 1800) {//30min
                        contadorCuentasGPS_346 = 600;//30min
                    } else if (contadorCuentasGPS_346 == 600) {//10min
                        contadorCuentasGPS_346 = 1200;//20min
                    } else {
                        contadorCuentasGPS_346 = 30;//30min
                    }
                    contadorCuentasGPS_ctrl = 0;
                    envioHilos_CertPostal_Censo_Aforos_Novedades(false, false, false, true, true);// Ax: Se envia  "censo" + cuentasGPS

                    abrirArchivosDeFacturacion();
                    Log.e("error", "entra en este7");
                    cod_cuentaUlt_foto = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim();
                    mesUlt_foto = infoClienteEntrada.gettablaEntradaClientes_mes().trim();
                    ejecutarProcesoDeFoto(infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + "_" + infoClienteEntrada.gettablaEntradaClientes_mes().trim(), 1, tipoFotoDigital, 0, "S", 1);
                    cerrarArchivosFacturacion();

                } catch (Exception ex) {
                    utils.Log(logfile, "[MenuDeLiquidacion]ADICINRVERFCION_REQUEST_CODE. " + ex.toString());
                }
            }

//            if (nivelOperador.equals("S")) {
//                esSupervisor = true;
//            }

        } else if (requestCode == ADICINCERTIFCAD_REQUEST_CODE) {//documento certificado

            if (resultCode == RESULT_OK && data != null) {

                try {
                    envioHilos_CertPostal_Censo_Aforos_Novedades(false, false, true, false, false);// Se envia  "certificado"
                } catch (Exception ex) {
                    utils.Log(logfile, "[MenuDeLiquidacion]ADICINCERTIFCAD_REQUEST_CODE. " + ex.toString());
                }
            }
        } else if (requestCode == CONFIGFORMATIMP_REQUEST_CODE) {//Configuracion Formato Impresion

            if (resultCode == RESULT_OK && data != null) {
                try {
                    cambiarValoresimpresion();
                } catch (Exception ex) {
                    utils.Log(logfile, "[MenuDeLiquidacion]CONFIGFORMATIMP_REQUEST_CODE. " + ex.toString());
                }
            }
        } else if (requestCode == CHATINSETAR_REQUEST_CODE) {//Chat mesajeria

            // imagenChat.setImageResource(R.drawable.chat_msg_no); ////COMENTADO CHAT 0991
            banderaChat = true; //Ax: true = leyo msg
        }
        //kimberly nuevo
        else if (requestCode == ALERTACUENTADIRECTA_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                banderaCuentaContratada = 1;
                abrirArchivosDeFacturacion();
                Log.e("error", "entra en activity on 0 " + lecturaT);
                procesarLectura(1, 0, lecturaT, "XY");
                //procesarLectura(cc_indlectura, cc_primero,  txtLecturaActual.getText().toString().trim(), cc_causaact);
                banderaCuentaContratada = 0;
                Log.e("error", "entra en activity on 1");
                //procesarLectura(0, 0, txtLecturaActual.getText().toString().trim(), "");
            }
        } else if (requestCode == CAMBIODIGITOS_REQUEST_CODE) {

            abrirArchivosDeFacturacion();
            infoRegistroSalida.lectura_TablaRegistroSalida(VariablesGlobales.registroactual);
            Log.e("INFO", "RESULTE DESPUES DEL CAMBIO DE DIGITOS: " + VariablesGlobales.intcontroltexto);
            variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";CAMBIO DIGITOS ;" + getPhoneDate() + "-" + getPhoneHour();
            escribeResumenTiempo(variableResumenLiquidacion + " " + " Entra a CAMBIO DIGITOS: " + VariablesGlobales.registroactual);

            if (VariablesGlobales.intcontroltexto != 0) {

                variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";CAMBIO DIGITOS ;" + getPhoneDate() + "-" + getPhoneHour();
                escribeResumenTiempo(variableResumenLiquidacion + " " + "CAMBIO DIGITOS: Posicion Actual|" + VariablesGlobales.registroactual);
                infoRegistroSalida.settablaRegistroSalida_informe("CAMBIO DIGITOS:" + VariablesGlobales.intcontroltexto);
                infoRegistroSalida.escribir_TablaRegistroSalida(VariablesGlobales.registroactual);
            }

            infoRegistroSalida.lectura_TablaRegistroSalida(VariablesGlobales.registroactual);
            visualizarInformacionCliente(0);
            cerrarArchivosFacturacion();
        } else if (requestCode == CAMARA_LECTURA_REQUEST_CODE) {
            // ── Retorno de CamaraLecturaActivity (OCR medidor) ─────────────────
            if (resultCode == RESULT_OK && data != null) {
                try {
                    esFotoMedidor = false;
                    yaTomoFotoOcr = true;
                    Log.e("INFO","8 - SETEA EL VALOR DE yaTomoFotoOcr: " + yaTomoFotoOcr);
                    String lecturaOcr = data.getStringExtra(CamaraLecturaActivity.EXTRA_LECTURA_OCR);
                    String fotoPath = data.getStringExtra(CamaraLecturaActivity.EXTRA_NOMBRE_IMAGEN);

                    Log.e("INFO", "CAMARA_LECTURA_REQUEST_CODE | lecturaOcr: " + lecturaOcr
                            + " | foto: " + fotoPath);

                    // Restaurar registro si cambió durante la captura
                    if (capRegistroActual != VariablesGlobales.registroactual) {
                        Log.e("INFO", "CAMARA_LECTURA: registro cambió. Restaurando " +
                                VariablesGlobales.registroactual + " → " + capRegistroActual);
                        VariablesGlobales.registroactual = capRegistroActual;
                        abrirArchivosDeFacturacion();
                        leerInformacionUsuario(0);
                    }

                    if (fotoPath != null && !fotoPath.trim().isEmpty()) {
                        nombrefoto  = fotoPath.trim();
                        exitoImagen = 1;
                    }
                    vecesImagen = 0;
                    ejecutarProcesoDeFotoII(vecesImagen);

                    if (lecturaOcr != null && !lecturaOcr.trim().isEmpty()) {
                        lecturaOcrCapturada = lecturaOcr.trim(); // guardar para comparar después
                        final String lecturaFinal = lecturaOcr.trim();
                        runOnUiThread(() -> {
                            txtElectura.setText(lecturaFinal);
                            txtLecturaActualKeyPress(6);
                        });
                    }
                    // Si OCR vacío → operario ingresa manualmente
                    // Si fotoOcrObligatoria=true → txtLecturaActualKeyPress bloqueará ingreso manual

                } catch (Exception ex) {
                    Log.e("ERROR", "[MenuDeLiquidacion]onActivityResult|CAMARA_LECTURA| " + ex.getMessage());
                    utils.Log(logfile, "[MenuDeLiquidacion]onActivityResult|CAMARA_LECTURA| " + ex.getMessage());
                }

            } else {
                esFotoMedidor = false;
                Log.e("INFO", "CAMARA_LECTURA_REQUEST_CODE | RESULT_CANCELED");
                // Si es obligatoria NO marcar yaTomoFotoOcr=true → deberá volver a intentar
                if (!fotoOcrObligatoria) {
                    yaTomoFotoOcr = true;
                    Log.e("INFO","9 - SETEA EL VALOR DE yaTomoFotoOcr: " + yaTomoFotoOcr);
                } else {
                    // foto obligatoria y canceló → mostrar diálogo de nuevo
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Foto obligatoria");
                    builder.setMessage("La foto del medidor es obligatoria para esta cuenta.\nDebe tomarla para poder registrar la lectura.");
                    builder.setCancelable(false);
                    builder.setPositiveButton("TOMAR FOTO", (dialog, which) -> capturaImagenFotografica());
                    builder.setNegativeButton("AHORA NO", null);
                    builder.show();
                }
                vecesImagen = 1;
                ejecutarProcesoDeFotoII(vecesImagen);
            }
        } else {
        }
    }

    private void mensajeKeyPress(int actionId, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_ESCAPE) {
//            txtInformeEscrito.setText("");
//            txtInformeEscrito.setEnabled(false);
            txtElectura.setEnabled(false);
            txtElectura.setVisibility(TextView.VISIBLE);
            txtElectura.setText("");
            return;
        }
        if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
//            if (txtInformeEscrito.getText().toString().trim().length() >= 3) {
//                infoRegistroSalida.settablaRegistroSalida_informe(txtInformeEscrito.getText().toString().trim());
//
//                tomarFechaSistema(medidorSalida.gettablaContadorSalida_FECHALECTURA(), medidorSalida.gettablaContadorSalida_HORALECTURA());
//
//                medidorSalida.settablaContadorSalida_Fechalectura(variables.amd);
//                medidorSalida.settablaContadorSalida_HORALECTURA(variables.hm);
//
//                Log.e("error", "escribe 12");
//                escribirTablasSalida();
//                txtInformeEscrito.setText(infoRegistroSalida.gettablaRegistroSalida_INFORME());
//            } else
//                txtInformeEscrito.setText("");
            //  txtInformeEscrito.setEnabled(false);
            txtElectura.setEnabled(true);
            txtElectura.setVisibility(TextView.VISIBLE);
            txtElectura.setText("");
            return;
        }
    }

    private void Desasociar() {

        try { //Ax: esta parte trata de cerrar cualquier conexion, pero puede que no exista tal conexion
            VariablesGlobales.btPrintService.stop();
        } catch (Exception ex) {
        }
        utils.WriteLine(logPrint, "");
    }

//    public void setScannedInfo(String barcode) {
//        try {
//
//            txtLecturaActual.requestFocus();
//            controlEdit = true;
//
//            if (barcode.length() > 0) {
//                String buscador = barcode;
//
//                if (buscador.length() >= 9) {
//
//                    if (buscador.length() >= 45 && buscador.length() <= 59) {
//                        if (buscador.length() >= 54 && buscador.length() <= 56) {
//
//                            valorLeidoFactura = buscador.substring(34, 44);
//                            buscador = buscador.substring(21, 30);
//                            variables.impresora = "LE3";
//
//                        } else {
//                            valorLeidoFactura = "0";
//                            buscador = buscador.substring(0, 9) + buscador.substring(31, 34);
//                            variables.impresora = "LE1";
//                        }
//                    } else {
//                        valorLeidoFactura = "" + (Double.parseDouble(VariablesGlobales.minimovalorentrega.trim()) + 1);//antes =0
//                        buscador = buscador.substring(0, 9);
//                        variables.impresora = "LE2";
//                        if (infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("E")) {
//
//                            activaLectorBarras = false;
//                            mensajeT("Debe leer el codigo de la Factura", msgMedio);
//                            activaLectorBarras = true;
//                            variables.impresora = "LE4";
//                        }
//                    }
//                } else {
//                    if (buscador.length() == 0) {
//                        activaLectorBarras = false;
//                        mensajeT("Codigo de Barras no Valido", msgCorto);
//                        activaLectorBarras = true;
//                        return;
//                    }
//                    variables.impresora = "   ";
//                }
//
//                VariablesGlobales.tipodebusqueda = 2;
//                activaLectorBarras = false;
//                buscarCuentaMedidor(buscador, 2);
//                activaLectorBarras = true;
//            }
//        } catch (Exception ex) {
//            utils.Log(logfile, "[MenuDeLiquidacion]setScannedInfo(), error Leyendo codigo de barras. " + ex.toString());
//            activaLectorBarras = false;
//            mensajeT("Codigo de Barras no Valido por excepcion", msgMedio);
//            //  Toast.makeText(getApplicationContext(), "Codigo de Barras no Valido por excepcion", Toast.LENGTH_LONG).show();
//            activaLectorBarras = true;
//        }
//    }

    public void muestraPosicionActual(Location loc) {
//        Log.e("error", "no toma posicion0 ");

        if (loc == null) {// Si no se encuentra localizacion, se mostrar // "Desconocida"
//            Log.e("error", "no toma posicion " + String.valueOf(loc.getLatitude()));
            txtLongitud.setText("0.0");//antes era "Desconocida"
            txtLatitud.setText("0.0");//antes era "Desconocida"
            velocidadGPS = "0";
        } else {// Si se encuentra, se mostrar� la latitud y longitud
//            Log.e("error", "toma posicion " + String.valueOf(loc.getLatitude()));
            txtLongitud.setText(String.valueOf(loc.getLatitude()));//Ax: antes txtLatitud
            txtLatitud.setText(String.valueOf(loc.getLongitude()));//Ax: antes txtLongitud
            velocidadGPS = String.valueOf(loc.getSpeed());
            numeroDeSatelitesGPS = "" + loc.getExtras().getInt("satellites");//Ax: sin probar!
            altitudReportadaGPS = String.valueOf(loc.getAltitude());
            fechayHoraReportadaGPS = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(loc.getTime());
            leyoCoordenadas = 1;

           /* if (txtLatitud.getText().toString().trim().length() > 12) {
                txtLatitud.setText(txtLatitud.getText().toString().trim().substring(0, 12));
            }
            if (txtLongitud.getText().toString().trim().length() > 12)
                txtLongitud.setText(txtLongitud.getText().toString().trim().substring(0, 12));*/

            if ((int) loc.getLatitude() != 0) {
                VariablesGlobales.coorTempLongitud = String.valueOf(loc.getLatitude());
            }

            if ((int) loc.getLongitude() != 0) {
                VariablesGlobales.coorTempLatitud = String.valueOf(loc.getLongitude());
            }

            if (VariablesGlobales.coorTempLongitud.length() > 16) {
                VariablesGlobales.coorTempLongitud = VariablesGlobales.coorTempLongitud.substring(0, 15);
            }

            if (VariablesGlobales.coorTempLatitud.length() > 16) {
                VariablesGlobales.coorTempLatitud = VariablesGlobales.coorTempLatitud.substring(0, 15);
            }
//este seria el punto donde sacar la distancia del predio a la coordenada actual del predio
            if ((int) loc.getLatitude() != 0 && !infoClienteEntrada.gettablaEntradaClientes_cordenadax().trim().equals("") &&
                    !infoClienteEntrada.gettablaEntradaClientes_cordenadax().trim().equals("0")
                    && !infoClienteEntrada.gettablaEntradaClientes_cordenadax().trim().equals("0.00"))
            {

                int distancia = 999;
                Log.e("errorDistancia", VariablesGlobales.coorTempLatitud + " data long " + VariablesGlobales.coorTempLongitud+" Coord x"+infoClienteEntrada.gettablaEntradaClientes_cordenadax()+" Coordy"+infoClienteEntrada.gettablaEntradaClientes_cordenaday());
                distancia = (int) variables.evaluarDistanciaAlPredio(parseStringToDouble(VariablesGlobales.coorTempLongitud), parseStringToDouble(VariablesGlobales.coorTempLatitud), parseStringToDouble(infoClienteEntrada.gettablaEntradaClientes_cordenaday().trim()), parseStringToDouble(infoClienteEntrada.gettablaEntradaClientes_cordenadax().trim()));
                txtDistancia.setText(Integer.toString(distancia).trim()+ " - Mts.");
                if (distancia < parseStringToInteger(VariablesGlobales.getDistanciagps()))
                {
                    txtDistancia.setBackgroundColor(Color.GREEN);
                    txtDistancia.setTextColor(Color.BLACK);

                }else {
                    txtDistancia.setBackgroundColor(Color.RED);
                    txtDistancia.setTextColor(Color.WHITE);
                }
            }



        }
    }

    private void generarTextosColillaFactura() {

        String nuevafecha;
        String formatopesos2;
        String comando = "";
        String formato_pesos_3;

        //imprimir aseo
        //Variables.Amd=E_contador.E_CONTAD_FECHA_LECT_ANTERIOR;
        variables.amd = medidorSalida.gettablaContadorSalida_FECHALECTURA().substring(6, 8) + "/" + medidorSalida.gettablaContadorSalida_FECHALECTURA().substring(4, 6) + "/" + medidorSalida.gettablaContadorSalida_FECHALECTURA().substring(0, 4);
        convertirFormatoDeFecha(variables.amd, fechaanteriorenformato, mesenformato, "D/M/A");
        Log.e("error", "data amd " + variables.amd);
        //nuevo Procedimiento para la presentacion de la fecha de aseo
        comando = "01" + variables.fechahoy.substring(2, variables.fechahoy.length()) + " al ";

        //nuevo para el cambio de la fecha del aseo por lo cual hay que tenerlo del 01 al ultimo dia por ahora el 30

        //se comenta int UltimoDia = (int) variables.calcularNumerodeDias(variables.amd);
        //comando=comando+Variables.Fecha_hoy;
        comando = comando + String.format("%1$2s", "30" + "").replace(" ", "0") + variables.fechahoy.substring(2, variables.fechahoy.length());
        //fin procedimiento
        //se clausura eso de abajo y se dejan enviando en blanco
        //comando = "  " +  "\r\n" + " $ " + String.format("%1$10s", costoservicio.trim()) + "\r\n" + subsidiocontribucion + "\r\n" + aseo;//+"\r\n";
//cambiar para ebsa
        String SubsContri = "         0";
        if (!infoClienteEntrada.gettablaEntradaClientes_MDDOR1SBSDIOCNTRBCION().trim().equals("0"))
            SubsContri = infoClienteEntrada.gettablaEntradaClientes_MDDOR1SBSDIOCNTRBCION();
        else
            SubsContri = infoClienteEntrada.gettablaEntradaClientes_MDDOR1SBSDIOCNTRBCION();
//cambiar para ebsa
  /*      if (infoClienteEntrada.getTablaEntradaClientes_TRC().contains("-")) {
            comando = infoClienteEntrada.getTablaEntradaClientes_TRA().replace(".", ",") + "                " + infoClienteEntrada.getTablaEntradaClientes_CVA().replace(".", ",") + "\r\n" +//+  "                "+"         0"fcs infoClienteEntrada..getTablaEntradaClientes_CVNA()
                    infoClienteEntrada.getTablaEntradaClientes_TRNA().replace(".", ",") + "                " + String.format("%10s", infoClienteEntrada.getTablaEntradaClientes_TRC().trim().replace(".", ",")) + "\r\n" +//+ "                "+infoClienteEntrada.getTablaEntradaClientes_CVA()
                    "..." + "\r\n" +
                    "...";//+"\r\n";
        } else {
            comando = infoClienteEntrada.getTablaEntradaClientes_TRA().replace(".", ",") + "                " + infoClienteEntrada.getTablaEntradaClientes_CVA().replace(".", ",") + "\r\n" +//+  "                "+"         0"fcs infoClienteEntrada..getTablaEntradaClientes_CVNA()
                    infoClienteEntrada.getTablaEntradaClientes_TRNA().replace(".", ",") + "                " + "         0" + "\r\n" +//+ "                "+infoClienteEntrada.getTablaEntradaClientes_CVA()
                    "..." + "\r\n" +
                    "...";//+"AQUI IBA EL SUBSIDIO PERO SE QUITA\r\n";
        }*/
        comando = "xxx1    " + "                " + "xxx2  " + "\r\n" +
                "xxx3  " + "                " + "xxx 4" + "\r\n" +
                "..." + "\r\n" +
                "...";


        Log.e("error", "colilla1 " + comando);
        escribaTextoDeImpresion(comando);


        if (TieneConcepto51 == 1)
            comando = "  " + alumbrado.substring(1, 13);
        else
            comando = "  " + "           0";

        Log.e("error", alumbrado.length() + " colilla2 " + comando);
        escribaTextoDeImpresion(comando);

        //formato_pesos_3 = String.Format("{0:#,###,###;###,###;0}", Convert.ToDouble(InfoClienteSalida.SCLIENTVALORFACTURADO)).ToString().PadLeft(14, ' ');
        //aqui cambie
        formato_pesos_3 = String.format("%1$10s", formateo(variables.valorconsumo2));
        comando = "     $" + formato_pesos_3;//+"\r\n";
        Log.e("error", "colilla3 " + comando);
        escribaTextoDeImpresion(comando);

        if (variables.tienedeuda == 0) {
            if (MensajeFoes == "") {
                comando = infoClienteEntrada.gettablaEntradaClientes_Informacionadicional().substring(0, 60) + "-";//InfoClienteEntrada.ECLIENTINFORMACIONADICIONAL.Substring(0, 60) + "-";//+"\r\n";
                // Log.e("error","colilla4 "+comando);
                escribaTextoDeImpresion(comando);
                comando = infoClienteEntrada.gettablaEntradaClientes_Informacionadicional().substring(60, 120);//InfoClienteEntrada.ECLIENTINFORMACIONADICIONAL.Substring(60, 60) + "-";//+"\r\n";
                // Log.e("error","colilla5 "+comando);
                escribaTextoDeImpresion(comando);
                comando = infoClienteEntrada.gettablaEntradaClientes_Informacionadicional().substring(120, 180);//InfoClienteEntrada.ECLIENTINFORMACIONADICIONAL.Substring(120, 60) + "";//+"\r\n";
                // Log.e("error","colilla6 "+comando);
                //escribaTextoDeImpresion(comando);
            } else {
                comando = MensajeFoes;
                // Log.e("error","colilla7 "+comando);
                escribaTextoDeImpresion(comando);

                comando = infoClienteEntrada.gettablaEntradaClientes_Informacionadicional().substring(0, 60) + "-";//+"\r\n";
                //  Log.e("error","colilla8 "+comando);
                escribaTextoDeImpresion(comando);
                comando = infoClienteEntrada.gettablaEntradaClientes_Informacionadicional().substring(60, 120) + "-";//+"\r\n";
                //Log.e("error","colilla9 "+comando);
                //escribaTextoDeImpresion(comando);

            }
            if (comando.trim().length() < 30) {
                if (infoRegistroSalida.gettablaRegistroSalida_COMENTARIO2().equals("003"))
                    comando = comando.trim() + " NOTA: Novedad Promediado  SE VISITARA";
                if (infoRegistroEntrada.gettablaRegistroDeEntrada_tipoDesviacion().trim().equals("P")) {
                    comando = comando.trim() + " NOTA: Predio Promediado, En Estudio";
                }
                if (infoRegistroSalida.gettablaRegistroSalida_COMENTARIO2().equals("001"))
                    comando = comando.trim() + " NOTA: consumo Alto NO se visitara";
                if (infoRegistroSalida.gettablaRegistroSalida_COMENTARIO2().equals("002"))
                    comando = comando.trim() + " NOTA: Novedad ALTA. NO SE VISITARA ";
                if (infoRegistroSalida.gettablaRegistroSalida_COMENTARIO2().equals("010") ||
                        infoRegistroSalida.gettablaRegistroSalida_COMENTARIO2().equals("011") ||
                        infoRegistroSalida.gettablaRegistroSalida_COMENTARIO2().equals("010"))
                    comando = comando.trim() + " NOTA: Desviacion Baja NO SE Visita";
            }
            escribaTextoDeImpresion(comando);
        } else {
            if (MensajeFoes == "") {
                comando = "SU CUENTA SE ENCUENTRA EN MORA, CANCELE INMEDIATAMENTE";//+"\r\n";
                // Log.e("error","colilla10 "+comando);
                escribaTextoDeImpresion(comando);
                comando = "ESTA FACTURA Y EVITE LA SUSPENSION DEL SERVICIO.";//+"\r\n";
                // Log.e("error","colilla11 "+comando);
                escribaTextoDeImpresion(comando);
                comando = "...";//+"\r\n";

                if (comando.trim().length() < 30) {
                    if (infoRegistroSalida.gettablaRegistroSalida_COMENTARIO2().equals("003"))
                        comando = comando.trim() + " NOTA: Novedad Promediado  SE VISITARA";
                    if (infoRegistroEntrada.gettablaRegistroDeEntrada_tipoDesviacion().trim().equals("P")) {
                        comando = comando.trim() + " NOTA: Predio Promediado, EN Estudio";
                    }
                    if (infoRegistroSalida.gettablaRegistroSalida_COMENTARIO2().equals("001"))
                        comando = comando.trim() + " NOTA: consumo Alto NO se visitara";
                    if (infoRegistroSalida.gettablaRegistroSalida_COMENTARIO2().equals("002"))
                        comando = comando.trim() + " NOTA: Novedad ALTA. NO SE VISITARA ";
                    if (infoRegistroSalida.gettablaRegistroSalida_COMENTARIO2().equals("010") ||
                            infoRegistroSalida.gettablaRegistroSalida_COMENTARIO2().equals("011") ||
                            infoRegistroSalida.gettablaRegistroSalida_COMENTARIO2().equals("010"))
                        comando = comando.trim() + " NOTA: Desviacion Baja NO SE Visita";
                }
                //  Log.e("error","colilla12 "+comando);
                escribaTextoDeImpresion(comando);
            } else {
                comando = MensajeFoes;
                // Log.e("error","colilla13 "+comando);
                escribaTextoDeImpresion(comando);
                comando = "SU CUENTA SE ENCUENTRA EN MORA, CANCELE INMEDIATAMENTE";//+"\r\n";
                // Log.e("error","colilla14 "+comando);
                escribaTextoDeImpresion(comando);
                comando = "ESTA FACTURA Y EVITE LA SUSPENSION DEL SERVICIO.";//+"\r\n";
                // Log.e("error","colilla15 "+comando);
                escribaTextoDeImpresion(comando);

            }
        }

        comando = infoClienteEntrada.gettablaEntradaClientes_Nombre().substring(0, 40) + " Ced:";//+"\r\n";
        Log.e("error", "colilla16 " + comando);
        escribaTextoDeImpresion(comando);

        variables.amd = fecha_vencimiento.substring(6, 8) + "/" + fecha_vencimiento.substring(4, 6) + "/" + fecha_vencimiento.substring(0, 4);
        convertirFormatoDeFecha(variables.amd, variables.fechahoy, mesenformato, "D/M/A");
        nuevafecha = variables.fechahoy;

        variables.amd = fecha_corte.substring(6, 8) + "/" + fecha_corte.substring(4, 6) + "/" + fecha_corte.substring(0, 4);
        convertirFormatoDeFecha(variables.amd, variables.fechahoy, mesenformato, "D/M/A");

        comando = "Fecha Vence:" + nuevafecha
                + "       Fecha Corte:" + variables.fechahoy;//+"\r\n";

        // Log.e("error","colilla17 "+comando);
        escribaTextoDeImpresion(comando);

        comando = "41577099980080838020" + String.format("%1$10s", infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim()).replace(" ", "0") + "3900" + String.format("%1$10s", infoClienteSalida.gettablaClienteSalida_VALORFACTURADO().trim()).replace(" ", "0");
        // Log.e("error","colilla18 "+comando);
        escribaTextoDeImpresion(comando);
        Log.e("error", "colilla19 " + comando);
        escribaTextoDeImpresion(comando);

        comando = "(415)7709998008083(8020)" + String.format("%1$10s", infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim()).replace(" ", "0") + "(3900)" + String.format("%1$10s", infoClienteSalida.gettablaClienteSalida_VALORFACTURADO().trim()).replace(" ", "0");//+"\r\n";
        // Log.e("error","colilla20 "+comando);
        escribaTextoDeImpresion(comando);

        //este proceso se debe quitar con el tiempo
        variables.amd = infoClienteEntrada.gettablaEntradaClientes_mes();//medidorSalida.gettablaContadorSalida_FECHALECTURA()
        Log.e("error", "colilla20 " + variables.amd);
        if (variables.amd.trim().length() == 0) {
            variables.amd = medidorSalida.gettablaContadorSalida_FECHALECTURA();
        }

        convertirFormatoDeFecha(variables.amd, variables.fechahoy, mesenformato, "AMD");

        if (sumarbloqueenergia < 0) {
            sumarbloqueenergia = sumarbloqueenergia * (-1);
            formatopesos2 = "$" + String.format("%1$12s", "<" + formateo(sumarbloqueenergia) + ">");
        } else {
            formatopesos2 = "$" + String.format("%1$12s", formateo(sumarbloqueenergia) + "");
        }


        comando = variables.mesenformato + "                     " + formatopesos2;//+"\r\n";
        Log.e("error", "colilla21 " + comando);
        escribaTextoDeImpresion(comando);

        if (infoClienteEntrada.gettablaEntradaClientes_Nrofactura().trim().length() < 15)
            comando = infoClienteEntrada.gettablaEntradaClientes_Nrofactura().substring(0, 5) + "-" + infoClienteEntrada.gettablaEntradaClientes_Nrofactura().substring(5, 15) + "                     " + aseo;//+"\r\n";
        else
            comando = infoClienteEntrada.gettablaEntradaClientes_Nrofactura().substring(0, 6) + "-" + infoClienteEntrada.gettablaEntradaClientes_Nrofactura().substring(6, 15) + "                     " + aseo;//+"\r\n";

        Log.e("error", "colilla22 " + comando);
        escribaTextoDeImpresion(comando);

        if (TieneConcepto51 == 1)
            comando = infoClienteEntrada.gettablaEntradaClientes_Cuenta() + "                            " + alumbrado;//+"\r\n";
        else
            comando = infoClienteEntrada.gettablaEntradaClientes_Cuenta() + "                            " + "         0";//+"\r\n";
        Log.e("error", "colilla23 " + comando);
        escribaTextoDeImpresion(comando);

        formatopesos2 = String.format("%1$12s", formateo(parseStringToDouble(infoClienteSalida.gettablaClienteSalida_VALORFACTURADO().trim())));
        comando = "$ " + formatopesos2;//+"\r\n";
        Log.e("error", "colilla24 " + comando);
        escribaTextoDeImpresion(comando);

        //fin proceso nuevo de impresion
        //generacion de los nuevos campos
        //nuevo campo 1 foes
        comando = infoClienteEntrada.gettablaEntradaClientes_consumofoes()+"    " +
                infoClienteEntrada.gettablaEntradaClientes_kwfoes() + "    " +
                infoClienteEntrada.gettablaEntradaClientes_valorfoes() + "    " +
                infoClienteEntrada.gettablaEntradaClientes_facturafoes().trim();//+"\r\n";
        Log.e("error", "colilla25 " + comando);
        escribaTextoDeImpresion(comando);

        //nuevo campo 2 y 3 liquida consumos crear variables
        comando = String.format("%1$-32s", LiquidacionConsumo1.trim()) + "" + LiquidacionConsumo2 + "\r\n" +
                String.format("%1$-32s", LiquidacionConsumo3.trim()) + "" + LiquidacionConsumo4;

        Log.e("error", LiquidacionConsumo1 + " colilla26 " + comando);
        escribaTextoDeImpresion(comando);
        try {

            //imprimir informacion de ASEO nuevos datos
            comando = "**imreime que1//";//String.format("%1$20s", infoClienteEntrada.gettablaEntradaClientes_perioperiodo_facturado().trim());//acomodar en el grafico
            Log.e("error", "colilla27 " + comando);
            //escribaTextoDeImpresion(comando);

            comando = "imprime que 2 ";//infoClienteEntrada.getTablaEntradaClientes_CFT() + "       " + infoClienteEntrada.gettablaEntradaClientes_TRLU().replace(".", ",") + "                " + infoClienteEntrada.getTablaEntradaClientes_TAFNA();
            Log.e("error", "colilla28 " + comando);
            //escribaTextoDeImpresion(comando);

            comando = "" + "imprime que 3";//infoClienteEntrada.getTablaEntradaClientes_CVNA();//
            Log.e("error", "colilla29 " + comando);
            //escribaTextoDeImpresion(comando);

            comando = "" + "imprime que 4";//infoClienteEntrada.getTablaEntradaClientes_VBA();
            Log.e("error", "colilla30 " + comando);
            //escribaTextoDeImpresion(comando);

            comando = "imprime que 5";//infoClienteEntrada.getTablaEntradaClientes_TRBL().replace(".", ",");
            Log.e("error", "colilla31 " + comando);
            //escribaTextoDeImpresion(comando);

            comando = "imprime que 6";//String.format("%10s", infoClienteEntrada.getTablaEntradaClientes_TRRA().replace(".", ",")) + "                " + SubsContri;//toca mover+"                         "+SubsContri;//sera que aqui va el total del aseo
            Log.e("error", "colilla31 " + comando);
            //escribaTextoDeImpresion(comando);
            int sub = 0;
            if (comandoconceptosAseo.length() > 0) {
                sub = comandoconceptosAseo.length() - 2;
            }
            escribaTextoDeImpresion(comandoconceptosAseo.substring(0,sub));

            //nuevos campos
            SeleccionarAcuerdo();


            comando = NroAcuerdo;//"       .......         .......";
            Log.e("error", "colilla32 " + comando);
            escribaTextoDeImpresion(comando);
            comando = " era acuerdo      .....                .....";
            Log.e("error", "colilla33 " + comando);
            escribaTextoDeImpresion(comando);

            comando = " DirAcuerdo salio...";// "  ........";
            Log.e("error", "colilla34 " + comando);
            escribaTextoDeImpresion(comando);

            //cambiar para imprimir solo el valor de aseo//formateo(parseStringToDouble(infoClienteSalida.gettablaClienteSalida_VALORFACTURADO())
//mirar para ebsa
            //if (infoClienteEntrada.getTablaEntradaClientes_TRC().contains("-")) {
                TotalAseo += 0;//parseStringToDouble(infoClienteEntrada.getTablaEntradaClientes_TRC().replace("-", ""));
          //  }
            formatopesos2 = String.format("%1$12s", formateo(TotalAseo));
            comando = "  $ " + formatopesos2;//+"\r\n";
            Log.e("error", "colilla35 " + comando);
            escribaTextoDeImpresion(comando);
            Log.e("error", "colilla36 " + comando);
            //escribaTextoDeImpresion("Version 21.12.17.1-Android Lect. " + infoClienteSalida.gettablaClienteSalida_LECTOR().trim() + " Cont.ElecroRedes");//se cambia a el 22
            if (EsImpresora521 == 1)
                escribaTextoDeImpresion("Version 26.01.14.A-11 Lect.1" + infoClienteSalida.gettablaClienteSalida_LECTOR().trim() + " Cont.APCSoluciones  - ZQ-521");//ersion 24.11.29.A-11 Lect
            else
                escribaTextoDeImpresion("Version 26.01.14.A-11 Lect.1" + infoClienteSalida.gettablaClienteSalida_LECTOR().trim() + " Cont.APCSoluciones  - RW-420");

        } catch (Exception e) {
            Log.e("error", "colilla " + e.getMessage());
            variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";ERROR EN COLILLA ;" + e.getMessage().toString();
            escribeResumenTiempo(variableResumenLiquidacion + " " + " Finalizando Colilla: ");

        }

    }

    void GeneracionImpresionEnLote(int RangoInicial, int RangoFinal) {
        int contador = RangoInicial;
        int temporal = variables.clienteactual;

        if (VariablesGlobales.habilitadaimpresora == 0) {
            mensajeT("Alerta De Impresora\n" + "El Proceso de Impresion\n" + "Se encuentra Deshabilitado", msgMedio);
            return;
        }
        try {
            while (contador <= RangoFinal) {
                infoClienteSalida.lectura_TablaClienteSalida(contador);
                if (infoClienteSalida.gettablaClienteSalida_INDFACTURACION().equals("K")) {
                    variables.clienteactual = contador;
                    leerInformacionUsuario(3);
                    if (VariablesGlobales.imprimirSoloPostal == 1) {
                        if (infoClienteEntrada.gettablaEntradaClientes_Facturadomiciliada().trim().equals("P")) {
                            if (generarArchivoTextoImpresion() > 0) {
                                mensajeT("Se generara el TXT del Cliente .." + infoClienteSalida.gettablaClienteSalida_CUENTA(), msgMedio);
                                //  Toast.makeText(getApplicationContext(), "fac 1", Toast.LENGTH_SHORT).show();
                                // Log.e("errorf","factura1 "+procesoImpresionFactura(0));
                                if (procesoImpresionFactura(0) > 0) {
                                    Log.e("error", "ind fact 3");
                                    infoClienteSalida.settablaClienteSalida_INDFACTURACION("F");
                                    infoClienteSalida.settablaClienteSalida_NROIMPRESIONES("1");
                                    tomarFechaSistema("", "");
                                    infoClienteSalida.settablaClienteSalida_FECHAIMPRESION(variables.amd);
                                    infoClienteSalida.settablaClienteSalida_HORAIMPRESION(variables.hm);
                                    infoClienteSalida.escribir_TablaClienteSalida(variables.clienteactual);
                                }
                            }
                        }
                    } else if (generarArchivoTextoImpresion() > 0) {
                        mensajeT("Se generara el TXT del Cliente .." + infoClienteSalida.gettablaClienteSalida_CUENTA(), msgCorto);
                        //   Log.e("errorf","factura2 "+procesoImpresionFactura(0));
                        //   Toast.makeText(getApplicationContext(), "fac 2", Toast.LENGTH_SHORT).show();

                        if (procesoImpresionFactura(0) > 0) {
                            Log.e("error", "ind fact 4");
                            infoClienteSalida.settablaClienteSalida_INDFACTURACION("F");

                            infoClienteSalida.settablaClienteSalida_NROIMPRESIONES("1");
                            tomarFechaSistema(infoClienteSalida.gettablaClienteSalida_FECHAIMPRESION(), infoClienteSalida.gettablaClienteSalida_HORAIMPRESION());
                            infoClienteSalida.settablaClienteSalida_FECHAIMPRESION(variables.amd);
                            infoClienteSalida.settablaClienteSalida_HORAIMPRESION(variables.hm);
                            infoClienteSalida.escribir_TablaClienteSalida(variables.clienteactual);
                        }
                    }
                }
                ++contador;
            }
            mensajeT("Proceso de impresion en Lote Finalizado", msgCorto);
        } catch (Exception ex) {
            mensajeT("Proceso de impresion en lote con Problemas", msgMedio);
            utils.Log(logfile, "[MenuDeLiquidacion]GeneracionImpresionEnLote" + ex.getMessage());
        }
        variables.clienteactual = temporal;
        leerInformacionUsuario(3);
        visualizarInformacionCliente(0);
    }

    public void addLog(String s) {
        if (TAG.length() > 22)
            Log.d(TAG.substring(0, 22), s);
    }

    private void setupComm() {
        // Initialize the array adapter for the conversation thread

        VariablesGlobales.mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.activity_txt_remote_device);
        //Log.d(TAG, "setupComm()");
        VariablesGlobales.btPrintService = new btPrintFile(this, mHandler);
        if (VariablesGlobales.btPrintService == null) {
            if (TAG.length() > 22) {
                Log.e(TAG.substring(0, 22), "btPrintService init() fail");
            }
        }
    }

    void setConnectState(Integer iState) {
        switch (iState) {
            case btPrintFile.STATE_CONNECTED:
                updateConnectButton(true);
                break;
            case btPrintFile.STATE_DISCONNECTED:
                updateConnectButton(false);
                break;
            case btPrintFile.STATE_CONNECTING:
                addLog("connecting...");
                break;
            case btPrintFile.STATE_LISTEN:
                addLog("listening...");
                break;
            case btPrintFile.STATE_IDLE:
                addLog("state none");
                break;
            default:
                addLog("unknown state var " + iState.toString());
        }
    }

    void updateConnectButton(boolean bConnected) {
        if (bConnected) {
            VariablesGlobales.habilitadaimpresora = 1;

            if (EsImpresora521 == 1)
                imagenPrinter.setImageResource(R.drawable.impresora_ok521);//.impresora_ok);
            else
                imagenPrinter.setImageResource(R.drawable.impresora_ok420);//.impresora_ok);
            //imagenPrinter.setImageResource(R.drawable.impresora_ok);

        } else {
            VariablesGlobales.habilitadaimpresora = 0;
        }
    }

    void connectToDevice(BluetoothDevice _device) {
        if (_device != null) {
            addLog("connecting to " + _device.getAddress());
            VariablesGlobales.btPrintService.connect(_device);

            utils.WriteLine(logPrint, _device.getAddress()); //escribe en log la mac
        } else {
            addLog("unknown remote device!");
        }
    }

    //    void connectToDevice() {
//        String remote = txtRemoteDevice2.getText().toString();
//        if (remote.length() == 0)
//            return;
//        if (VariablesGlobales.btPrintService.getState() == btPrintFile.STATE_CONNECTED) {
//            VariablesGlobales.btPrintService.stop();
//            setConnectState(btPrintFile.STATE_DISCONNECTED);
//            return;
//        }
//
//        String sMacAddr = remote;
//        if (sMacAddr.contains(":") == false && sMacAddr.length() == 12) {
//            // If the MAC address only contains hex digits without the
//            // ":" delimiter, then add ":" to the MAC address string.
//            char[] cAddr = new char[17];
//
//            for (int i = 0, j = 0; i < 12; i += 2) {
//                sMacAddr.getChars(i, i + 2, cAddr, j);
//                j += 2;
//                if (j < 17) {
//                    cAddr[j++] = ':';
//                }
//            }
//            sMacAddr = new String(cAddr);
//        }
//
//        BluetoothDevice device;
//        try {
//            device = VariablesGlobales.mBluetoothAdapter.getRemoteDevice(sMacAddr);
//        } catch (Exception e) {
//            mensajeT("Invalid BT MAC address", msgCorto);
//            // Toast.makeText(getApplicationContext(), "Invalid BT MAC address", Toast.LENGTH_LONG).show();
//            // myToast("Invalid BT MAC address");
//            device = null;
//        }
//
//        if (device != null) {
//            addLog("connecting to " + sMacAddr);
//            VariablesGlobales.btPrintService.connect(device);
//        } else {
//            addLog("unknown remote device!");
//        }
//    }

    public void doWork() {
        runOnUiThread(new Runnable() {
            public void run() {
                try {

                    if (salirse) return;

                    if (banderaWsOcupado) contocup++;
                    else contocup = 0;

                    if (contocup == 19) {
                        contocup = 0;
                        banderaWsOcupado = false;
                    }

                    contadorCuentasGPS++;

                    if (contadorCuentasGPS == 50) { //300 5min
                        //Log.e("error", "entra al hilo");
                        contadorCuentasGPS = 0;
                        // contadorCuentasGPS_ctrl += 60;//5min
                        // contadorCuentasGPS_ctrl = 0;
                        verificaSupervisor();
                    }
                    //    }

                    //envio cuentasGPS preguntar
                    contadorEnvioCuentasGPS++;
                    if (contadorEnvioCuentasGPS == 80) {
                        //Log.e("error", "entra al hilo enviar cuentas GPS");
                        contadorEnvioCuentasGPS = 0;
                        envioHilos_CertPostal_Censo_Aforos_Novedades(false, false, false, false, true); // Se envian "novedades", "aforos"
                    }


//                    //COMENTADO CHAT 0991
//                    if (contadorChat == 120) {
//                        if (comprobarconexiones(false)) {
//                            contadorChat = 0;
//                            procesandoenvioenHilos_chat = true;
//                            chatAsyncall();
//                        }
//                    }

                    //txtElectura.setText(txtElectura.getText());
                    //}
//                    String curTime = fecha + " - " + hours + ":" + mins + ":" + secs;
//                    lblHoraFechaActual.setText(curTime);
                    ImpresoraStatus();
                } catch (Exception e) {
                }
            }
        });
    }

    private String fechafoto() {
        Calendar calendar = Calendar.getInstance();

        String fecha = calendar.get(Calendar.DAY_OF_MONTH) + "/" + (1 + (int) calendar.get(Calendar.MONTH)) + "/"
                + calendar.get(Calendar.YEAR);
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);

        String mins = "" + minutes;
        String secs = "" + seconds;

        if (minutes < 10) {
            mins = "0" + mins;
        }
        if (seconds < 10) {
            secs = "0" + secs;
        }
        return fecha + " - " + hours + ":" + mins + ":" + secs;
    }

//    /**
//     * ATTENTION: This was auto-generated to implement the App Indexing API.
//     * See https://g.co/AppIndexing/AndroidStudio for more information.
//     */
//    public Action getIndexApiAction() {
//        Thing object = new Thing.Builder()
//                .setName("MenuDeLiquidacion Page") // TODO: Define a title for the content shown.
//                // TODO: Make sure this auto-generated URL is correct.
//                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
//                .build();
//        return new Action.Builder(Action.TYPE_VIEW)
//                .setObject(object)
//                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
//                .build();
//    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    //Ax: recibe el resultado ejecutado de la clase async task del metodo onPostExecute(result).
    public void processFinish(String output) {
    }

    public void ProbarWS() {

        if (banderaWsOcupado) {
            mensajeT("Envios en proceso, espere 1 min.", msgLargo);
            return;
        } //Ax: si hay una conexion abierta no hace nada

        try {
            metodo = "VALIDAR_CONEXION";
//            AsyncCallWS task = new AsyncCallWS();
//            task.execute("");
            TaskHelper.execute(new AsyncCallWS(), metodo);
            banderaWsOcupado = true;

        } catch (Exception ex) {
            Log.e("error", "probarws " + ex.getMessage());
            mensajeT(ex.getMessage(), msgLargo);
        }
    }

    /*public boolean getParamsWs() {
        try {
            CrudComunicaciones crudComuni = new CrudComunicaciones(this);
            BDComunicaciones bdc = crudComuni.getParams();
            if (bdc != null) {
                URL = "http://" + bdc.getURL();
                paginaWs = bdc.getPaginaWs();
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }*/

    public boolean getParamsWs() {
        try {
            CrudComunicaciones crudComuni = new CrudComunicaciones(this);
            BDComunicaciones bdc = crudComuni.getParams();
            if (bdc != null) {
                URL = "http://" + bdc.getURL();
                paginaWs = bdc.getPaginaWs();
                RutaAdministrador = bdc.getRutaAdministrador();
                puertoAPI = bdc.getPuertoApi();
                EsComprimido = "1";
                cadenaURLapi = URL.substring(0, URL.lastIndexOf(":") + 1) + puertoAPI + "/api/";

                Log.e("INFO", "RutaAdministrador" + RutaAdministrador);
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            String[] time = getTimeError().split("\\|");
            utils.Log(logfile, "[MenuDeLiquidacion]getParamsWs()|" + ex.getMessage() + "|" + time[0] + "|" + time[1] + "|");
            return false;
        }
    }
    // Ax:  este metodo se debe meter en clase y cambiar por el original. todo
    /*private boolean seleccionUrl() {
        try {
            String nombreArchivo = VariablesGlobales.directorioactual + "/DIRECCIONEIP.TXT";
            File file = new File(nombreArchivo);

            if (!file.exists()) return false;

            FileReader stream3 = new FileReader(nombreArchivo);
            BufferedReader reader = new BufferedReader(stream3);
            String linea = "";
            String datoIP;
            int cont = 0;

            while ((linea = reader.readLine()) != null) {

                datoIP = linea;

                if (datoIP.substring(datoIP.length() - 1, datoIP.length()).trim().equals("A")) {
                    URL = "http://" + datoIP.substring(0, 15).trim() + ":" + datoIP.substring(16, 20).trim();
                    paginaWs = datoIP.substring(64, 157).trim();
                    cont++;
                    break;
                }
            }

            reader.close();

            if (cont < 1) return false;

            URL = URL.replace(" ", "");

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            mensajeT("Error, verificar Configuracion IP", msgMedio);
        }
        return false;
    }
*/
    //Ma: Crea los valores para la impresion de factura Temp, etc...
    private boolean crearvaloresImp() {
        try {
            String nombreArchivo = VariablesGlobales.directorioactual + "/ValoresFormato.log";
            File file = new File(nombreArchivo);

            if (!file.exists()) file.createNewFile();

            String texto = "200\r\n0  \r\n0\r\n0\r\n0\r\n";
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, true));
            bufferedWriter.write(texto);
            bufferedWriter.close();
            return true;
        } catch (Exception e) {
            Log.e("ERROR", "crearvaloresImp| Error: " + e);
            utils.Log(logfile, "crearvaloresImp| Error: " + e);
        }
        return false;
    }

    //Ax: Lee y carga del archivo "Nombre" si existe
    private boolean cargarArchivoNombre() {

        String linea = "";
        String archivo_nombre = VariablesGlobales.directorioactual + "/DATOSDEENTRADA/NOMBRE";
        File file = new File(archivo_nombre);

        if (!file.exists()) return false;

        try {
            FileReader r = new FileReader(archivo_nombre);
            BufferedReader reader = new BufferedReader(r);

            while ((linea = reader.readLine()) != null) {
                if (linea.trim().equals("")) {
                    continue;
                } else break;
            }

            linea = linea.substring(0, 12).trim();

            Ciclo = linea.substring(0, 4);

            try {
                Municipio = linea.substring(5, 8);
                Seccion = linea.substring(8, 11);
                Division = linea.substring(11, 12);
                return true;

            } catch (Exception e) {
                return false;
            }
        } catch (Exception ex) {
            return false;// "Problema con la carga de:\n Archivo: Ciclo, Municipio...etc."
        }
    }

    private boolean CargarArchivoCarga() {

        String nombreArchivo = VariablesGlobales.directorioactual + "/ArchivosCarga.cfi";
        int contador = 0;
        try {
            FileReader r = new FileReader(nombreArchivo);
            BufferedReader reader = new BufferedReader(r);
            String linea;

            while ((linea = reader.readLine()) != null) {

                if (contador < 1) { // tomar la ruta del sistema administrativo
                    rutaAdministrador = linea.trim();

                    if (!rutaAdministrador.contains(":")) {
                        mensajeT("No hay Archivo de Soporte...\n //ArchivosCarga.cfi", msgLargo);
                        return false;
                    }
                }

                if (contador == 1) { // Ax: sin control de error
                    esComprimido = linea.trim();

                    if (esComprimido.trim().equals("ZIP SI")) {

                        esComprimido = "1";
                    } else {

                        esComprimido = "0";
                    }
                }
                contador++;
            }
            r.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

//    // Captura numero IMEI del telefono
//    public String getSerialNumber() {
//
//        TelephonyManager tManager = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
//        String deviceIMEI = tManager.getDeviceId();
//        return deviceIMEI;
//    }

    private void mensajeT(String msg, int dur) { //Ax: 1 segundo: 1000
//        Toast toast = Toast.makeText(MenuDeLiquidacion.this, msg, dur);
//        toast.setGravity(Gravity.TOP, 10, 170);
//        toast.show();

        if (indicadorManual == 0) {

            final Toast toast = Toast.makeText(MenuDeLiquidacion.this, msg, dur);
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
    }

    //Ax: muestra mensaje en pantalla con boton 'ok' pero no hace nada
    public void mensajeOk(String msg, final String metodos) {
        if (indicadorManual == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(msg)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            switch (metodos) {
                                case "retomaFoto":
                                    //capturaImagenFotografica();
                                    capturaImagenFotograficaII();
                                    break;
                            }
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void mostrarDialogoAlerta(String titulo, String mensaje) {
        if (indicadorManual == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(titulo);
            builder.setMessage(mensaje);

            builder.setIcon(R.drawable.ic_launcher1);

            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Log.i("Dialogo Acerca De", "Boton Ok pulsado");
                }
            });
            builder.show();
        }
    }

    public void ImpresoraStatus() {

        if (VariablesGlobales.habilitadaimpresora < 1) {
            imagenPrinter.setImageResource(R.drawable.impresora_ko);
            // return;
        }

        switch (VariablesGlobales.btPrintService.getState()) {
            case btPrintFile.STATE_DISCONNECTED:
                imagenPrinter.setImageResource(R.drawable.impresora_ko);
                conn = true;
                break;
            case btPrintFile.STATE_CONNECTED:

                if (EsImpresora521 == 1)
                    imagenPrinter.setImageResource(R.drawable.impresora_ok521);//.impresora_ok);
                else
                    imagenPrinter.setImageResource(R.drawable.impresora_ok420);//.impresora_ok);
                //imagenPrinter.setImageResource(R.drawable.impresora_ok);

                conn = true;
                break;
            case btPrintFile.STATE_CONNECTING:
                if (prt) {
                    prt = false;
                    imagenPrinter.setImageResource(android.R.color.transparent);

                } else {
                    prt = true;
                    if (EsImpresora521 == 1)
                        imagenPrinter.setImageResource(R.drawable.impresora_ok521);//.impresora_ok);
                    else
                        imagenPrinter.setImageResource(R.drawable.impresora_ok420);//.impresora_ok);
                    //imagenPrinter.setImageResource(R.drawable.impresora_ok);


                }
                conn = false;
                // txtRemoteDevice.setText("CONECTANDO..... ");
                break;
            case btPrintFile.STATE_IDLE:
                imagenPrinter.setImageResource(R.drawable.impresora_ko);
                conn = true;
                break;
            case btPrintFile.STATE_LISTEN:


                if (EsImpresora521 == 1)
                    imagenPrinter.setImageResource(R.drawable.impresora_ok521);//.impresora_ok);
                else
                    imagenPrinter.setImageResource(R.drawable.impresora_ok420);//.impresora_ok);
                //imagenPrinter.setImageResource(R.drawable.impresora_ok);

                //  txtRemoteDevice.setText("CONECTADO a " + macAdress);
                break;
        }
    }

    public void ImpresoraStatusMsg(String msg) {

        switch (VariablesGlobales.btPrintService.getState()) {

            case btPrintFile.STATE_DISCONNECTED:
                mensajeT(msg + "\n Estado: Desconectada", msgCorto);
                break;
            case btPrintFile.STATE_CONNECTED:
                mensajeT(msg + "\n Estado: Conectada", msgCorto);
                break;
            case btPrintFile.STATE_CONNECTING:
                mensajeT(msg + "\n Estado: Conectando...", msgCorto);
                break;
            case btPrintFile.STATE_IDLE:
                mensajeT(msg + "\n Estado: Inactiva", msgCorto);
                break;
            case btPrintFile.STATE_LISTEN:
                mensajeT(msg + "\n Estado: Escuchando", msgCorto);
                break;
        }
    }

    /***
     * Ax: trata de conectar a la impresora, obteniendo la mac guardada en archivo log
     */
    private void conexionForzadaImpresora() {

        String printer = utils.ReadLine(logPrint);

        if (!printer.equals("")) { //Ax: hay algun texto en el log

            try {
                conn = false;
                BluetoothDevice device = VariablesGlobales.mBluetoothAdapter.getRemoteDevice(printer);
                VariablesGlobales.printerMacAddress = device.getAddress();
                connectToDevice(device);

            } catch (Exception ex) {
                conn = true;
                utils.Log(logfile, "[MenuLiquidacion] conexionForzadaImpresora()" + ex.getMessage());
                VariablesGlobales.bDiscoveryStarted = false;

            }
        } else {
            if (VariablesGlobales.btPrintService.getState() == btPrintFile.STATE_DISCONNECTED || VariablesGlobales.btPrintService.getState() == btPrintFile.STATE_IDLE || VariablesGlobales.habilitadaimpresora == 0) {
                conn = true;
            }
            mensajeT("No hay Impresora asociada", msgMedio);
        }
    }

    public void verificaSupervisor() {
        if (indicadorManual == 0) {
            if (contadorCuentasGPS_ctrl == contadorCuentasGPS_346) {
                String msg = "Supervisor Aun en actividad Liquidacion " + contadorCuentasGPS_346 / 60 + " minutos";
                guardarCoordenadas(msg);
            /*if (esSupervisor) {
                esSupervisor = false; //evitar dowork lo levante
                //  CensoSupervisor();
            }*/
            } else {
                guardarCoordenadas("");
            }
        }
    }

//    public void chatAsyncall() {
//
//        try {
//            metodo = "CHATRECIBE";
//            taskChat = new AsyncCallChat();
//            taskChat.execute("");
//        } catch (Exception ex) {
//            mensajeT("Ha ocurrido un error al Recibir mensajes", msgCorto);
//            utils.Log(logfile, "[MenuDeLiquidacion]chatAsyncall()" + ex.getMessage());
//        }
//    }

    public String chatRecibirMensajes(String operario) {

        String respuestaWeb = "";
        if (indicadorManual == 0) {
            WSSoap wsoaps = new WSSoap(URL, paginaWs);

            try {
                respuestaWeb = wsoaps.chatRecibirMensajes("RETORNAR_MENSAJES", operario);
                separador(respuestaWeb);
            } catch (Exception ex) {
                utils.Log(logfile, "[MenuDeLiquidacion]chatRecibirMensajes()" + ex.getMessage());
            }
        }
        return respuestaWeb;
    }

    public void separador(String cadena) {

        File tarjeta = Environment.getExternalStorageDirectory();
        File file = new File(tarjeta.getAbsolutePath(), "/DatosDeSalida/Mensajes.txt");
        String d = "";
        int in = 0;
        cadena = cadena.substring(8, cadena.length());

        for (int i = 0; i < cadena.length(); i++) {

            int c = cadena.indexOf("{", in);
            int cc = cadena.indexOf("}", c);

            if (c == -1 || cc == -1) {
                break;
            } else {

                d += cadena.substring(c + 1, cc) + "0" + "\n";
                Log.e("bien", "sobbreescribe " + d);
                c += 1;
                cc += 1;
                in += c;
            }
        }

        if (file.exists()) {
            try {
                int r = d.indexOf("88888888888", 0);
                if (r == -1) {
                    FileWriter TextOut = new FileWriter(file, true);
                    TextOut.write("\n" + d);
                    TextOut.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {

            try {

                int r = d.indexOf("88888888888", 0);
                if (r == -1) {

                    OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file));
                    osw.write(d);
                    osw.flush();
                    osw.close();
                    Log.e("bien", "Los datos fueron grabados correctamente");
                }

            } catch (IOException ioe) {
                Log.e("error", "Los datos no fueron grabados correctamente");
            }
        }
    }

//    // para celular unitech con el laser para leer codigo de barras
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        super.onKeyDown(keyCode, event);
//        Log.e("error", "entra al evento tecla " + keyCode + " " + event);
//        if (keyCode == 245) {
//            controlEdit = false;
//            editLector.requestFocus();
//            if (!editLector.getText().toString().equals("")) {
//                editLector.setText("");
//            }
//
//            Log.e("error", "entra al evento");
//
//            return true;
//        }
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            finish();
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        super.onKeyUp(keyCode, event);
//        Log.e("error", "entra al evento keyUp");
//        if (keyCode == 245) {
//            // editLector.requestFocus();
//            if (event.getAction() == MotionEvent.ACTION_UP) {//release button
//                Bundle bundle = new Bundle();
//                bundle.putBoolean("scan", true);
//                Intent mIntent = new Intent().setAction(SOFTWARE_SCANKEY).putExtras(bundle);
//                sendBroadcast(mIntent);
//            }
//            return true;
//        } else {
//            return false;
//        }
//    }

    class Reloj3 implements Runnable {
        // @Override
        public void run() {
            while (!myThread.currentThread().isInterrupted()) {
                try {
                    if (indicadorManual == 0) {
                        doWork();
                    }
                    myThread.sleep(3000); // Pause of 1 Second
                } catch (InterruptedException e) {
                    myThread.currentThread().interrupt();
                } catch (Exception e) {
                }
            }
        }
    }

    //Ax: permite llamado asincrono para ejecutar conexion al Ws
    private class AsyncCallWS extends AsyncTask<String, Integer, Void> {

        public String respuesta_ = "";
        ProgressDialog pDialog;
        public String asyncResponse = "";
        public String asynCallFrom = "";

        @Override
        protected Void doInBackground(String... params) {

            asynCallFrom = params[0];

            //nuevo para que el operador de administrativo no envie datos al servidor
            if (nivelOperador.trim().equals("A") || indicadorManual == 1) {
                return null;
            }


            try {
                switch (asynCallFrom) {

                    case "GOGO":
                        IncluirDatosEnModoAutomatico();
                        break;

                    case "comprobarBorradoBackup":
                        comprobarBorradoBackup();
                        break;

                    case "ENVIARFACTURACIONCOLECCION":
                        EnviarFacturacionColeccion();
                        break;

                    case "ENVIOCERTPOSTLCENSOAFORONOVEDADS":
                        respuesta_ = Enviar_CertPostal_Censo_Aforos_Novedades_2();
                        break;

                    case "VALIDAR_CONEXION":
                        //  Log.e("error", "valida conexion " + metodo);
                        WSSoap wsoapp = new WSSoap(URL, paginaWs);
                        respuesta_ = wsoapp.verificarWs(metodo);
                        break;

                    case "ProcesaFotosAsync":
                        asyncResponse = ejecutarProcesoDeFotoIII();
                        break;

                    case "EnviarFotosServer":
                        respuesta_ = EnviarFotosServerAsync();
                        break;

                    case "EnviarLblsServer":
                        respuesta_ = EnviarLblsServerAsync();
                        break;

                    case "tryAll":
                        WSSoap wsoappx = new WSSoap(URL, paginaWs);
                        //respuesta_ = wsoappx.tryAll("RetornoVigencia", serialPDA);
                        break;

                    case "ENVIOPOSICIONAMIENTO":
                        envioPosicionamiento();
                        break;

                    case "ValidarNoEnviados":
                        respuesta_ = ValidarNoEnviados();
                        break;
                    case "liquidacionautomatica1":
                        respuesta_ = IncluirDatosEnModoAutomatico(); // del menu
                        //respuesta_ = ValidarNoEnviados();
                        break;
                    case "liquidacionautomatica2":
                        respuesta_ = IncluirDatosEnModoAutomatico2(); // del menu
                        //respuesta_ = ValidarNoEnviados();
                    case "liquidacionautomatica3":
                        respuesta_ = IncluirDatosEnModoAutomatico3(); // del menu
                        //respuesta_ = ValidarNoEnviados();
                        break;

//                    case "AvanzarLecturas":
//                        respuesta_ = AvanzarLecturas();
//                        break;
                }

            } catch (Exception e) {
                respuesta_ = "Error, valide conexion con Ws";
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            String msg = "";

            switch (metodo) {

                case "EnviarFotosServer":
                    //msg = respuesta_;
                    if (!respuesta_.equals("Proceso de Fotos Exitoso! ✓") || !respuesta_.contains("✓")) {
                        mensajeT("NO SE PUDO ENVIAR FOTOGRAFIAS", msgCorto);
                    }
                    procesandoenvioenHilos_CPCAN = 0;
                    procesandoenvioenHilos = 0;
                    Log.e("error3", "ciclo " + Ciclo.substring(1, 3));
                    if (parseStringToInteger(Ciclo.substring(1, 3)) == 0 || parseStringToInteger(Ciclo.substring(1, 3)) == 24 || parseStringToInteger(Ciclo.substring(1, 3)) == 1 || parseStringToInteger(Ciclo.substring(1, 3)) == 25
                            || parseStringToInteger(Ciclo.substring(1, 3)) == 6 || parseStringToInteger(Ciclo.substring(1, 3)) == 23 || parseStringToInteger(Ciclo.substring(1, 3)) == 10 || parseStringToInteger(Ciclo.substring(1, 3)) == 11
                            || parseStringToInteger(Ciclo.substring(1, 3)) == 30 || parseStringToInteger(Ciclo.substring(1, 3)) == 31 || parseStringToInteger(Ciclo.substring(1, 3)) == 32 || parseStringToInteger(Ciclo.substring(1, 3)) == 57) {
                        enviarLbls();
                    }
                    break;

                case "EnviarLblsServer":
                    procesandoenvioenHilos_CPCAN = 0;
                    procesandoenvioenHilos = 0;
                    envioFacturacionHilos();
                    break;

                case "tryAll":
                    // Log.e("error", "tryall post " + respuesta_);
                    /*tryAll(respuesta_);*/
                    banderaWsOcupado = false;
                    break;

                case "ENVIOCERTPOSTLCENSOAFORONOVEDADS":
                    Log.e("error", "entra a cambiar valor");
                    procesandoenvioenHilos_CPCAN = 0;
                    procesandoenvioenHilos_chat = false;
                    if (respuesta_.equals("ok")) {
                        EnviarFotos();
                        imagenRed.setImageResource(R.drawable.redactiva);
                        Enviar_CertPostal_Censo_Aforos_Novedades_3();
                    } else {

                        if (respuesta_.length() > 30) {
                            mensajeT("No hubo envio " + ";\n " + respuesta_.substring(0, 30), msgCorto);
                        } else {
                            mensajeT("No hubo envio " + ";\n " + respuesta_, msgCorto);
                        }
                    }
                    break;

                case "ProcesaFotosAsync":
                    //btnAdelante.performClick();
                    if (!asyncResponse.contains("✓") && asyncResponse.contains("|")) {
                        utils.Log(logfile, "[MenuDeLiquidacion]ProcesaFotosAsync(POS): " + asyncResponse);
                        String[] mss = asyncResponse.split("\\|");
                        VariablesGlobales.tipodebusqueda = 6;
                        VariablesGlobales.datodebusqueda = capRegistroActual + "";
                        Log.e("INFO", "mss length: " + mss.length);
                        if (mss.length > 1) {

                            /*if (!mss[1].trim().equals("") && mss[2].trim().equals("Problema Grabando Nombre de Foto")) {
                                mensajeOk("DEBE TOMAR FOTO DE NUEVO\n\n" + "Verifique Datos de la Cuenta: " + mss[1] , "retomaFoto");
                                //infoRegistroSalida.buscarSecuencial_TablaRegistroSalida_cuenta(mss[1]);
                                //buscarCuentaMedidor("", 0);
                            } else if(!mss[1].trim().equals("") && mss[2].trim().equals("Problema Grabando Nombre de Foto")){
                                mensajeOk("DEBE TOMAR FOTO DE NUEVO\n\n" + "Verifique Datos de la Cuenta: " + mss[1]
                                        + "\n Porfavor redirijase a la cuenta y realice la fotografia desde el menu de ayuda", "");
                            }else {
                                if (capRegistroActual > 0) {
                                    //buscarCuentaMedidor("", 0);
                                }
                            }*/
                            if (!mss[1].trim().equals("") && mss[2].trim().equals("Problema Grabando Nombre de Foto")) {
                                mensajeOk("DEBE TOMAR FOTO DE NUEVO\n\n" + "Verifique Datos de la Cuenta: " + mss[1], "retomaFoto");
                                //infoRegistroSalida.buscarSecuencial_TablaRegistroSalida_cuenta(mss[1]);
                                //buscarCuentaMedidor("", 0);
                            }
                        }
                    } else {
                        EnviarFotos();//* EnviarFotografias();
                    }
                    break;

                case "VALIDAR_CONEXION":
                    try {
                        Integer.parseInt(respuesta_);//genera excepcion si diferente de 1

                        imagenRed.setImageResource(R.drawable.redactiva);
                        MostrarAlertDialog("Alerta Seleccion Terminacion", "Conexion OK\n[SI] = ACTIVA EL ENVIO\n[NO] = DESACTIVA ENVIO \nAL SERVIDOR?", "validarconexion");

                    } catch (NumberFormatException e) {
                        // Log.e("error", "sin conexion " + e.getMessage());
                        imagenRed.setImageResource(R.drawable.redinativa);
                        mensajeT("NO HAY CONEXION\n CON EL SERVIDOR...\n SE INACTIVO EL\n ENVIO AUTOMATICO", msgCorto);
                        conexionGPRSActiva = 0;
                        cerrarArchivosFacturacion();
                    } catch (Exception e) {
                        // Log.e("error", "sin conexion2 " + e.getMessage());
                    }
                    procesandoenvioenHilos = 0;
                    banderaWsOcupado = false;
                    break;
                case "ValidarNoEnviados":
                    dialogloading.dismiss();
                    ValidandoNoEnv = 0;
                    Log.e("INFO", "ValidarNoEnviados| respuesta_:" + respuesta_);
                    Log.e("INFO", "ValidarNoEnviados| CuentasProblema:" + CuentasProblema);

                    if (respuesta_.contains("VALIDACION REALIZADA") || respuesta_.equals("VALIDACION REALIZADA")) {
                        if (!CuentasProblema.trim().equals("")) {
                            mensajeT("Cuentas Problema:\n" + CuentasProblema, msgLargo);
                        }
                        leerInformacionUsuario(1);
                        visualizarInformacionCliente(0);
                        cerrarArchivosFacturacion();


                        String NombreArchivoCob1 = VariablesGlobales.directorioactual + "/DATOSDESALIDA/CO_COBRO.SDA";
                        String NombreArchivoCobH = VariablesGlobales.directorioactual + "/DATOSDESALIDA/CO_COBROHILOS.SDA";

                        if (procesandoenvioenHilos == 0) {
                            File Archivo3 = new File(NombreArchivoCobH);
                            if (Archivo3.exists())
                                Archivo3.delete();
                            //copiar a cobros para el reembio
                            VariablesGlobales.copyFile(NombreArchivoCob1, NombreArchivoCobH, false);
                        }
                        Log.e("INFO", "PROCESO CONCLUIDO CON:\n" + "No Leidas: " + Noleidas + "\n" + "Recuperadas para Enviar: " + LeidasNoenviadas);
                        mensajeOk("PROCESO CONCLUIDO CON:\n" + "No Leidas: " + Noleidas + "\n" + "Recuperadas para Enviar: " + LeidasNoenviadas, "");
                    } else {
                        mensajeOk(respuesta_, "");
                    }
                    break;
                case "liquidacionautomatica1":
                case "liquidacionautomatica2":
                case "liquidacionautomatica3":
                    dialogloading.dismiss();
                    indicadorManual = 0;
                    String[] resp = {};
                    if (respuesta_.contains("|") && respuesta_.length() > 0) {
                        if (respuesta_.contains("INFO") || respuesta_.contains("ERROR")) {
                            resp = respuesta_.split("|");
                            mensajeT(resp[1], msgLargo);
                        }
                    } else {
                        mensajeT("Error al liquidar automaticamente comuniquese con el supervisor", msgLargo);
                    }
                    break;
//                case "AvanzarLecturas":
//
//                    String[] ms = null;
//                    Log.e("INFO","Respuesta: " + respuesta_);
//                    if (respuesta_.contains("|")) ms = respuesta_.split("\\|");
//
//                    if(respuesta_.contains("AvanzarLecturas|")){
//                        switch (ms[1]) {
//                            case "OK":
//                                avanzapost();
//                                break;
//                            case "ERROR":
//                                mensajeOk("Error avanzando de cliente porfavor contacteses con su supervisor","");
//                                break;
//                        }
//                    }
//                    break;
            }

            if (!msg.isEmpty()) {
                // txtInformeEscrito.setText(msg + " ! " + txtInformeEscrito.getText());
                mensajeT(msg, msgLargo);
            }
            respuesta_ = "";
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
        }
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

    private class AsyncCallChat extends AsyncTask<String, Integer, Void> {

        public String respuesta_;

        @Override
        protected Void doInBackground(String... params) {

            try {
                switch (metodo) {

                    case "CHATRECIBE":
                        if (isCancelled()) break;
                        UtilsNet utilnet = new UtilsNet();
                        boolean hayI = utilnet.hayInternet(getApplicationContext());
                        if (!hayI) {
                            respuesta_ = "SINCONEXION";

                        } else {
                            respuesta_ = chatRecibirMensajes(lector);

                        }
                        break;

                }

            } catch (Exception e) {
                respuesta_ = "Error, valide conexion con Ws";
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            switch (metodo) {
                case "CHATRECIBE":
                    procesandoenvioenHilos_chat = false;
                    if (respuesta_.equals("SINCONEXION")) {
                        imagenRed.setImageResource(R.drawable.redinativa);
                        break;
                    } else {
                    }

                    if (respuesta_.contains("No tiene Mensajes para leer")) {
                        imagenRed.setImageResource(R.drawable.redactiva);
                        if (banderaChat) {
                            //  imagenChat.setImageResource(R.drawable.chat_msg_no);//COMENTADO CHAT 0991
                        }
                    } else {
                        if (!banderaChat) {
                            //  imagenChat.setImageResource(R.drawable.chat_msg_si); //COMENTADO CHAT 0991
                        }
                        imagenRed.setImageResource(R.drawable.redactiva); //-1 si hay msg
                    }
                    break;
            }
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
        }
    }

    //nuevo metodo para leer el archivo ValoresFormato
    //y mover los graficos
    public void leerArchivoFormato() {
        try {
            String Valortexto2 = "";
            String ArchivoFormato = VariablesGlobales.getDirectorioactual() + "/ValoresFormato.log";
            File file = new File(ArchivoFormato);

            if (file.exists()) {

                FileReader r = new FileReader(file);
                BufferedReader reader = new BufferedReader(r);
                String linea = "";
                int cont = 0;

                while ((linea = reader.readLine()) != null) {
                    cont++;
                    if (cont == 2) {
                        Valortexto2 = linea.trim();
                    }
                }
                r.close();
            }
            moverGrafico = Integer.parseInt(Valortexto2);

        } catch (Exception e) {
        }

    }

    public void lecturaActual() {
        if (indicadorManual == 0)
            txtElectura.setText("");
//               avanza
        abrirArchivosDeFacturacion();
        avanzarRegistro();
        visualizarInformacionCliente(0);
        cerrarArchivosFacturacion();
        variables.impresora = "   ";
    }

    public void tryAll() {

        if (tryAlL) return;

        if (banderaWsOcupado) return; //Ax: si hay una conexion abierta no hace nada

        try {
            metodo = "tryAll";
//            AsyncCallWS task = new AsyncCallWS();
//            task.execute("");
            TaskHelper.execute(new AsyncCallWS(), metodo);
            banderaWsOcupado = true;

        } catch (Exception ex) {
            utils.Log(logfile, "tryAll" + ex.getMessage());
            banderaWsOcupado = false;
        }
    }

    private void tryAll(String rest) {

        banderaWsOcupado = false;
        Printzpl xmlrest = new Printzpl(serialPDA);
        try {

            if (rest.length() < 120) {
                if (xmlrest.isSerialPDA()) {
                    return;
                }
            }

            String gs = xmlrest.printerZpl(rest);

            if (gs.length() > 150) {
                gs = gs.substring(136, 146);
            }

            if (xmlrest.mPrint(gs) || !xmlrest.isSerialPDA()) {
                Intent intent = new Intent(getApplicationContext(), MenuDeLiquidacion.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("L", true);
                startActivity(intent);
                this.finish();
            } else {
                tryAlL = true;
            }

        } catch (Exception ex) {
            utils.Log(logfile, "tryAll" + ex.getMessage() + xmlrest.borrar + "...__");
            banderaWsOcupado = false;
        }
    }

    // Suma los dias recibidos a la fecha
    public Date sumarRestarDiasFecha(Date fecha, int dias) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fecha); // Configuramos la fecha que se recibe
        calendar.add(Calendar.DAY_OF_YEAR, dias); // numero de dias a anniadir, o
        // restar en caso de dias<0
        return calendar.getTime(); // Devuelve el objeto Date con los nuevos
        // dias annadidos
    }

    private boolean abrirFestivos(String fechavence) {

        misFestivos.setArchivo_TablaFestivos(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/FESTIVOS.TXT");
        File file = new File(misFestivos.getArchivo_TablaFestivos());

        if (file.exists() && file.length() > 0) {
            if (misFestivos.abrir_TablaFestivos(misFestivos.getArchivo_TablaFestivos())) {
                misFestivos.buscarbinario_TablaFestivos(fechavence);
                Log.e("error", "encontro fes " + misFestivos.getEncontro_TablaFestivos());
                if (misFestivos.getEncontro_TablaFestivos() > 0) {
                    misFestivos.Cerrar_TablaFestivos();

                    return true;
                }
                misFestivos.Cerrar_TablaFestivos();
            }
        }
        return false;
    }


    private void SeleccionarAcuerdo() {

        String CadenaTomada = "";
        //int contar = 1;
        //int controlliquidacion = 1;

        String ArchivoAcuerdos = VariablesGlobales.directorioactual + "/DATOSDEENTRADA/acuerdos.txt";
        Log.e("error", "entra a acuerdos0 ");

        try {
            File file = new File(ArchivoAcuerdos);

            if (file.exists()) {

                abrirArchivosDeFacturacion();
                FileReader leerImp = new FileReader(ArchivoAcuerdos);
                BufferedReader reader = new BufferedReader(leerImp);

                int cont = 0;
                CadenaTomada = "";
                int contador = 0;
                char[] r = {'|'};
                Log.e("error", "entra a acuerdos ");
                while ((CadenaTomada = reader.readLine()) != null) {
                    contador++;
                    Log.e("error", "entra a acuerdos1 " + CadenaTomada.replace("|", ";").split(";"));
                    CadenaTomada = CadenaTomada.replace("|", ";");
                    String[] arr = CadenaTomada.split(";");
                    int contadordepartes = arr.length;
                    for (int i = 0; i < contadordepartes; i++) {
                        if (i == 0)
                            MunAcuerdo = arr[i];
                        else if (i == 1)
                            NroAcuerdo = arr[i];
                        else if (i == 2)
                            DirAcuerdo = arr[i];
                        else if (i == 3)
                            TelAcuerdo = arr[i];
                        else
                            i = 100;

                    }
                    if (Integer.parseInt(String.format("%1$3s", MunAcuerdo.trim()).replace(" ", "0")) == Integer.parseInt(String.format("%1$3s", infoClienteEntrada.gettablaEntradaClientes_Municipio().trim()).replace(" ", "0"))) {
                        break;
                    } else {
                        MunAcuerdo = "";
                        NroAcuerdo = "";
                        DirAcuerdo = "";
                        TelAcuerdo = "";
                    }
                }

                reader.close();

            }

        } catch (IOException e) {
            Log.e("error", "archivo acuerdos " + e.getMessage());
            //  Toast.makeText(getApplicationContext(), "Error, lectura archivo acuerdos"+e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("error", "archivo acuerdos e " + e.getMessage());
            //  Toast.makeText(getApplicationContext(), "Error, lectura archivo acuerdos"+e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }


    private void consumos_historicos() {

        String consumo = "";
        String comando;
        int i, numlineas = 6, potencial = 10; //, mes=0
        int tope_grafica_consumo = 810;// + moverGrafico;
        String MesesFacturados;
        double consmax = 0;
        int X, pot = 0;
        double valorincremento = 0;

        /*consumo = infoRegistroEntrada.gettablaRegistroDeEntrada_CONSUMO1() + infoRegistroEntrada.gettablaRegistroDeEntrada_CONSUMO2()
                + infoRegistroEntrada.gettablaRegistroDeEntrada_CONSUMO3() + infoRegistroEntrada.gettablaRegistroDeEntrada_CONSUMO4()
                + infoRegistroEntrada.gettablaRegistroDeEntrada_CONSUMO5() + infoRegistroEntrada.gettablaRegistroDeEntrada_CONSUMO6();*/

        //talvez InfoRegistroSalida.SREGISTFECHALECTURA = Variables.Amd.Substring(6, 4) + Variables.Amd.Substring(3, 2) + Variables.Amd.Substring(0, 2);

        anosperiodos = "";
        mesesperiodos = "";
        consumosperiodos = "";

/* antes de ebsa
        mesesperiodos = variables.convertirMesLetras(infoRegistroEntrada.gettablaRegistroDeEntrada_periodo6().trim()) + "     ";
        mesesperiodos += variables.convertirMesLetras(infoRegistroEntrada.gettablaRegistroDeEntrada_periodo5().trim()) + "     ";
        mesesperiodos += variables.convertirMesLetras(infoRegistroEntrada.gettablaRegistroDeEntrada_periodo4().trim()) + "     ";
        mesesperiodos += variables.convertirMesLetras(infoRegistroEntrada.gettablaRegistroDeEntrada_periodo3().trim()) + "     ";
        mesesperiodos += variables.convertirMesLetras(infoRegistroEntrada.gettablaRegistroDeEntrada_periodo2().trim()) + "     ";
        mesesperiodos += variables.convertirMesLetras(infoRegistroEntrada.gettablaRegistroDeEntrada_periodo1().trim()) + "     ";
        mesesperiodos += variables.convertirMesLetras(infoRegistroSalida.gettablaRegistroSalida_FECHALECTURA().substring(4, 6)) + "   ";
*/
        mesesperiodos = infoRegistroEntrada.gettablaRegistroDeEntrada_periodo6().trim() + "     ";
        mesesperiodos += infoRegistroEntrada.gettablaRegistroDeEntrada_periodo5().trim() + "     ";
        mesesperiodos += infoRegistroEntrada.gettablaRegistroDeEntrada_periodo4().trim() + "     ";
        mesesperiodos += infoRegistroEntrada.gettablaRegistroDeEntrada_periodo3().trim() + "     ";
        mesesperiodos += infoRegistroEntrada.gettablaRegistroDeEntrada_periodo2().trim() + "     ";
        mesesperiodos += infoRegistroEntrada.gettablaRegistroDeEntrada_periodo1().trim() + "     ";
        mesesperiodos += infoRegistroSalida.gettablaRegistroSalida_FECHALECTURA().substring(4, 6) + "   ";

        /*anters
        anosperiodos = "-" + infoRegistroEntrada.gettablaRegistroDeEntrada_ano6().substring(2, 4) + "     -" + infoRegistroEntrada.gettablaRegistroDeEntrada_ano5().substring(2, 4) + "  " +
                "   -" + infoRegistroEntrada.gettablaRegistroDeEntrada_ano4().substring(2, 4) + "     -" + infoRegistroEntrada.gettablaRegistroDeEntrada_ano3().substring(2, 4) + "  " +
                "   -" + infoRegistroEntrada.gettablaRegistroDeEntrada_ano2().substring(2, 4) + "     -" + infoRegistroEntrada.gettablaRegistroDeEntrada_ano1().substring(2, 4) + "  " +
                "   -" + infoRegistroSalida.gettablaRegistroSalida_FECHALECTURA().substring(2, 4);
*/
        anosperiodos = "-" + infoRegistroEntrada.gettablaRegistroDeEntrada_ano6() + "     -" + infoRegistroEntrada.gettablaRegistroDeEntrada_ano5() + "  " +
                "   -" + infoRegistroEntrada.gettablaRegistroDeEntrada_ano4() + "     -" + infoRegistroEntrada.gettablaRegistroDeEntrada_ano3() + "  " +
                "   -" + infoRegistroEntrada.gettablaRegistroDeEntrada_ano2() + "     -" + infoRegistroEntrada.gettablaRegistroDeEntrada_ano1() + "  " +
                "   -" + infoRegistroSalida.gettablaRegistroSalida_FECHALECTURA().substring(2, 4);

        consumosperiodos = infoRegistroEntrada.gettablaRegistroDeEntrada_consumo6() + "   " + infoRegistroEntrada.gettablaRegistroDeEntrada_consumo5() +
                "   " + infoRegistroEntrada.gettablaRegistroDeEntrada_consumo4() + "   " + infoRegistroEntrada.gettablaRegistroDeEntrada_consumo3() +
                "   " + infoRegistroEntrada.gettablaRegistroDeEntrada_consumo2() + "   " + infoRegistroEntrada.gettablaRegistroDeEntrada_consumo1() + "   ";
        //InfoClienteSalida.SCLIENTCONSUMO2 cambiar por este valor la resentacion de la factura

        // alerta el porque se refleja este campo como el valor real liquidado
        // infoClienteSalida.SCLIENTCONSUMO2
        double consumoFacturado = Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2().trim());

        if (consumoFacturado < 99999) {
            if (Double.parseDouble(infoClienteSalida.gettablaClienteSalida_VALOR1().trim()) > 0 && (Double.parseDouble(infoClienteSalida.gettablaClienteSalida_VALOR1().trim()) != Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2())))
                consumosperiodos = consumosperiodos + String.format("%1$5s", (Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2().trim()) + Double.parseDouble(infoClienteSalida.gettablaClienteSalida_VALOR1().trim()))) + " Kwh";
            else
                consumosperiodos = consumosperiodos + String.format("%1$5s", infoClienteSalida.gettablaClienteSalida_CONSUMO2().trim().replace(".", ",")) + " Kwh";

        } else {
            consumosperiodos = consumosperiodos + "99999  Kwh";
        }

        consumo = infoRegistroEntrada.gettablaRegistroDeEntrada_consumo6() + infoRegistroEntrada.gettablaRegistroDeEntrada_consumo5() +
                infoRegistroEntrada.gettablaRegistroDeEntrada_consumo4() + infoRegistroEntrada.gettablaRegistroDeEntrada_consumo3() +
                infoRegistroEntrada.gettablaRegistroDeEntrada_consumo2() + infoRegistroEntrada.gettablaRegistroDeEntrada_consumo1();
        //InfoClienteSalida.SCLIENTCONSUMO2 cambiar por este valor la resentacion de la factura

        // alerta el porque se refleja este campo como el valor real liquidado
        // infoClienteSalida.SCLIENTCONSUMO2

        if (Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2().trim()) < 99999) {
            if (Double.parseDouble(infoClienteSalida.gettablaClienteSalida_VALOR1().trim()) > 0 && (Double.parseDouble(infoClienteSalida.gettablaClienteSalida_VALOR1().trim()) != Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2())))
                consumo = consumo + String.format("%1$5s", (Double.parseDouble(infoClienteSalida.gettablaClienteSalida_CONSUMO2().trim()) + Double.parseDouble(infoClienteSalida.gettablaClienteSalida_VALOR1().trim()))) + " Kwh";
            else
                consumo = consumo + String.format("%1$5s", infoClienteSalida.gettablaClienteSalida_CONSUMO2().trim().replace(".", ",")) + " Kwh";

        } else {
            consumo = consumo + "99999";
        }

        Log.e("error", consumo + " texto consumo ");
        for (i = 0; i <= 6; i++) {

            int y = i * 5;
            if (Double.parseDouble(consumo.substring(y, (y + 5))) > consmax)
                consmax = Double.parseDouble(consumo.substring(y, (y + 5)).trim());
        }

        if (consmax >= 100)
            potencial = 100;
        for (i = 1; pot < consmax; i++)
            pot = potencial * i;

        if (pot == 0)
            pot = 1;

//antes i = 0; i <= numlineas - 1; i++
        for (i = 0; i <= numlineas; i++) {
            X = (int) (tope_grafica_consumo - (Double.parseDouble(consumo.substring(i * 5, i * 5 + 5)) * 45) / pot);
            comando = (40 + (65 * i)) + " " + X + " " + (40 + (65 * i)) + " " + tope_grafica_consumo + " 15";
            escribaTextoDeImpresion(comando);
        }
        while (i++ <= numlineas)
            escribirLineaEnBlanco(1);
        // escribaTextoDeImpresion("");

       /* comando = variables.convertirMesLetras(String.format("%1$2s", infoClienteEntrada.gettablaEntradaClientes_mes().trim())) + "  "
                + String.format("%1$4s", (infoClienteSalida.getTablaClienteSalida_CONSUMOFACTURADO().trim().trim()));
        escribaTextoDeImpresion(comando);*/

    }

    public void tomoLectura() {
        if (indicadorManual == 0) {
            File archivoT = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/TOMOLECTURA.TXT");

            if (archivoT.exists()) {
                archivoT.delete();
            }

            try {
                RandomAccessFile rFile = new RandomAccessFile(archivoT, "rw");
                rFile.writeBytes(variables.totalregistrosleidos + "|" + variables.totalcausasnolectura + "|");
                rFile.close();
            } catch (IOException ioe) {

                try {
                    if (archivoT.exists())
                        archivoT.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void envioPosicionamiento() {
        if (indicadorManual == 0) {

            SoapArrays soapA = new SoapArrays();
            soapA.execute("enviarP", getApplicationContext(), URL, paginaWs, "POSICIONAMIENTO", serialPDA, arma_cuentasGps[4], fechayHoraReportadaGPS.trim(), VariablesGlobales.coorTempLatitud, VariablesGlobales.coorTempLongitud, numeroDeSatelitesGPS.trim(), fechaSatelite, lector, "", acentsCic(Ciclo), (Municipio + Seccion + Division));
        }

    }

    private String acentsCic(String mns) {
        mns = mns.replace("O", "").replace("N", "").replace("D", "");
        return mns;
    }


    private void escribeResumenTiempo(String texto) {

        texto += "\r\n";
        //Date d = new Date();
        //texto = texto + " " + d;
        File fileName = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/RESUMENTIEMPO.SDA");

        try {
            RandomAccessFile rFile = new RandomAccessFile(fileName, "rw");

            if (!fileName.exists()) {
                rFile.writeBytes(texto);
                rFile.close();
            } else {

                rFile.seek(rFile.length());
                rFile.writeBytes(texto);
                rFile.close();
            }

        } catch (FileNotFoundException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }


    public void enviarLbls() {
        try {
            Log.e("error3", "intenta enviar ");
            String respuesta;
            int cantLbls = 20;
            //int contadorZipFotos = 1;
            RutaZip = VariablesGlobales.directorioactual + "/LBLS/" + "LBLS" + Municipio + Seccion + Division + ".ZIP";
            List<String> lbls = new ArrayList<String>(); //Ax: Contendra lista de solo  archivos JPG
            int desde = 51;
            int hasta = 52;

            FileReader r = new FileReader(VariablesGlobales.directorioactual + "/LBLS/" + "L" + Ciclo + Municipio + Seccion + Division); //Ax: se recorre el archivo para capturar las no eviadas o en envio
            BufferedReader reader = new BufferedReader(r);
            String linea;
            int conteo = 0;
            int conteoPos = 0;
            posicLblCont = new int[cantLbls + 1];//Ax: va a guardar la posicion real de la linea
            String Ultimafoto = "";
            while ((linea = reader.readLine()) != null) {
                conteo++;
                if (linea.substring(desde, hasta).equals("X") || linea.substring(desde, hasta).equals("_")) {

                    File fotop = new File(VariablesGlobales.directorioactual + "/LBLS/" + linea.substring(0, 50).trim());

                    if (fotop.exists() && !fotop.isDirectory()) {
                        conteoPos++;
                        if (conteoPos == 20) {
                            break;
                        }
                        Log.e("envio", "lbl " + linea + "***" + Ultimafoto);
                        if (!linea.equals(Ultimafoto)) {
                            Log.e("envio", "lbl 2" + linea + "***" + Ultimafoto);
                            Ultimafoto = linea.trim();
                            lbls.add(VariablesGlobales.directorioactual + "/LBLS/" + linea.substring(0, 50).trim());
                            posicLblCont[conteoPos] = conteo;
                        } else {
                            Log.e("envio", "lbl 3" + linea + "***" + Ultimafoto);

                            --conteo;
                            --conteoPos;
                        }
                    }
                }
            }
            r.close();

            if (lbls.size() > 0) {
                ArrayList filestoZip = new ArrayList(); //Contiene los archivos por comprimir

                for (String x : lbls) { //Recorrer cada jpg encontrado para añadirlo


                    filestoZip.add(new File(x));
                }
                respuesta = utils.CreaZip(RutaZip, filestoZip);

                Log.e("errora", "envio lbl " + respuesta);
                if (!respuesta.contains("✓")) {
                    mensajeT("Error Comprimiendo \n Archivos por enviar", msgLargo);
                    return;
                }

                Log.e("errora", procesandoenvioenHilos_CPCAN + " envio lbl1 " + comprobarconexiones(true));
                if (!comprobarconexiones(true)) return;

                if (procesandoenvioenHilos_chat) {
                    contadorChat = 0;
                    taskChat.cancel(true);
                }

                procesandoenvioenHilos_CPCAN = 1;
                try {
                    metodo = "EnviarLblsServer";
//                    AsyncCallWS task = new AsyncCallWS();
//                    task.execute("");
                    TaskHelper.execute(new AsyncCallWS(), metodo);
                    banderaWsOcupado = true;

                } catch (Exception ex) {
                    mensajeT(ex.getMessage(), msgLargo);
                    banderaWsOcupado = false;//Bandera conexion ocupada
                    utils.Log(logfile, "[Comunicaciones] EnviarLblsServer(): " + ex.getMessage());
                    procesandoenvioenHilos_CPCAN = 0;
                }

            } else {
                mensajeT("No hay fotografia(s) para enviar", msgLargo);
            }
        } catch (Exception ex) {
            mensajeT("Error en el envio de fotos \n " + ex.getMessage(), msgLargo);
            utils.Log(logfile, "[Comunicaciones] EnviarLblsServer().: " + ex.getMessage());
        }
    }

    public String EnviarLblsServerAsync() {

        File file = new File(RutaZip);

        try {
            WSSoap wsoap = new WSSoap(URL, paginaWs);
            String ArchivoAEnviarRecibir = rutaAdministrador.trim() + "Backuplbl" + "\\"; //D:ENRUTADOR_X/CIC0809/C1851011/
            String hash = utils.Md5Hash(RutaZip);
            Log.e("errora", ArchivoAEnviarRecibir + " directorio lbls " + file.getName());
            String nombremetodo = "VerificarSiexiste_Directorio_Archivo";//No usar la variable "metodo" pues es global
            String respuesta = wsoap.VerificarSiexisteDirectorioArchivo(nombremetodo, ArchivoAEnviarRecibir, file.getName());//Ax: Se comprueba si existe la ruta destino remota

            if (!respuesta.equals("true")) {
                return "Envio de Lbls, \n No se ha encontrado destino en el servidor!";
            }

            nombremetodo = "Terminal_ToServerReceive";
            respuesta = wsoap.TerminalToServerReceive(nombremetodo, ArchivoAEnviarRecibir, parseStringToInteger(esComprimido), RutaZip, trama);//Enviar Datos al servidor

            if (!(respuesta.equals("4") || respuesta.equals("1"))) {
                return "Envio de Lbls, \n No se pudo Enviar Archivo al servidor";
            }

            metodo = "Descomprime";
            respuesta = wsoap.Descomprimir(metodo, ArchivoAEnviarRecibir, ArchivoAEnviarRecibir + file.getName(), hash, serialPDA);

            if (!respuesta.equals("true")) {
                return "Envio de Lbls, \n No se pudo descomprimir remoto";
            }

            actualizaLbls();
            file.delete();
        } catch (Exception ex) {
            utils.Log(logfile, "[MenuDeLiquidacion]EnviarLblsServer(): " + ex.getMessage());
            return "Error al enviar lbls al servidor\n, intente de nuevo";
        } finally {
            banderaWsOcupado = false;//Bandera conexion ocupada
            procesandoenvioenHilos_CPCAN = 0;
        }
        metodo = "EnviarLblsServer";
        return "Proceso de Lbls Exitoso! ✓";
    }

    public void actualizaLbls() { //Recorre el archivo de fotos para actualizar los que si se fueron "Y"
        try {
            int desde = 51;
            int hasta = 52;
            String path = VariablesGlobales.directorioactual + "/LBLS/L" + Ciclo + Municipio + Seccion + Division;
            FileReader r = new FileReader(path); //Ax: se recorre el archivo para actualizar las eviadas
            File fileName = new File(path + "_temp");
            BufferedReader reader = new BufferedReader(r);
            String linea;
            int conteo = 0;

            while ((linea = reader.readLine()) != null) {
                conteo++;

                for (int i = 0; i < posicLblCont.length; i++) {
                    if (conteo == posicLblCont[i]) {
                        linea = linea.substring(0, desde) + "Y" + linea.substring(hasta);
                        continue;
                    }
                }
                utils.EscribirLinea(fileName, linea + "\r\n");
            }
            r.close();

            File fpath = new File(path);
            try {
                if (reader != null) reader.close();

                fpath.delete();
            } catch (Exception ex) {
                mensajeT("Error en la actualizacion de Lbls.. \n " + ex.getMessage(), msgLargo);
                utils.Log(logfile, "[Comunicaciones] actualizaLbls()..: " + ex.getMessage());
            }
            fileName.renameTo(fpath);

        } catch (Exception ex) {
            mensajeT("Error en la actualizacion de Lbls \n " + ex.getMessage(), msgLargo);
            utils.Log(logfile, "[Comunicaciones] actualizaLbls().: " + ex.getMessage());
        }
    }


    /*public void escribirReactiva40() {

        if (indicadorReactivo40 != 0 && activaCritica) {
            variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";PROCESAR LECTURA-3 ;" + getPhoneDate() + "-" + getPhoneHour();
            escribeResumenTiempo(variableResumenLiquidacion + " " + " Entra a escribirReactiva40()|indicadorReactivo40: " + indicadorReactivo40 + " |activaCritica: " + activaCritica);
            Log.e("error2", "entra aqui1 ");
            String cuentaActual = infoRegistroSalida.gettablaRegistroSalida_CUENTA().trim();

            infoRegistroSalida.lectura_TablaRegistroSalida(indicadorReactivo40);
            if (cuentaActual.equals(infoRegistroSalida.gettablaRegistroSalida_CUENTA().trim())) {

                variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";escribirReactiva40 ;" + getPhoneDate() + "-" + getPhoneHour();
                escribeResumenTiempo(variableResumenLiquidacion + " " + "escribirReactiva40: Posicion Actual|" + VariablesGlobales.registroactual);
                infoRegistroSalida.settablaRegistroSalida_causadenolectura("40");
                infoRegistroSalida.escribir_TablaRegistroSalida(indicadorReactivo40);
                variableResumenLiquidacion = infoClienteEntrada.gettablaEntradaClientes_Cuenta().trim() + ";Escribio 40 aqui  6;" + getPhoneDate() + "-" + getPhoneHour();
                escribeResumenTiempo(variableResumenLiquidacion + " " + "Ecbribio 40 aqui 6|" + VariablesGlobales.registroactual);

            }
            activaCritica = false;
            indicadorReactivo40 = 0;
            infoRegistroSalida.lectura_TablaRegistroSalida(VariablesGlobales.registroactual);
        }
    }*/

    public void apagarWifi() {
        try {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            if (wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(false);
            }

        } catch (Exception ex) {
            mensajeT("Error al Activar WIFI \n" + ex.getMessage(), msgLargo);
        }
    }

    //create a file where the photo will be saved
    private File createImageFile() throws IOException {
        // Create an image file name

        // File storageDir = new File(Environment.getExternalStorageDirectory() + "/DCIM/FOTOGRAFIASL");//getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        // Log.e("error", namePhoto + " directorio " + storageDir + "/" + namePhoto + ".jpg");
        /*File image = File.createTempFile(
                namePhoto,  /* prefix
                ".jpg",         /* suffix
                storageDir      /* directory
        );*/
        // File image = new File(storageDir, "/" + namePhoto + ".jpg" /* directory */);
        File image = new File(namePhoto);

        // Save a file: path for use with ACTION_VIEW intents
        // currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    //take picture
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.gselectroCaqueta.accesoyseguridad.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, TAKE_PICTURE);
            }
        }
    }

    static class TaskHelper {

        public static <P, T extends AsyncTask<P, ?, ?>> void execute(T task) {
            execute(task, (P[]) null);
        }

        @SuppressLint("NewApi")
        public static <P, T extends AsyncTask<P, ?, ?>> void execute(T task, P... params) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
            } else {
                task.execute(params);
            }
        }
    }

    private void comprobarBorradoBackup() { //Trata de borrar backup de 3 dias atras
        try {
            File directory = new File(VariablesGlobales.directorioBackUp.substring(0, VariablesGlobales.directorioBackUp.lastIndexOf("BACKUP") + 6));

            if (!directory.exists()) return;

            File[] files = directory.listFiles();
            Calendar time = Calendar.getInstance();
            time.add(Calendar.DAY_OF_YEAR, -2);

            for (File file : files) {

                if (file.isDirectory()) {
                    File[] files2 = file.listFiles();
                    if (files2.length == 0) {
                        file.delete();
                    } else {
                        borradoBackups(time, files2);
                    }
                } else {
                    Date lastModified = new Date(file.lastModified());
                    if (lastModified.before(time.getTime())) {
                        file.delete();
                    }
                }
            }
        } catch (Exception ex) {
            utils.Log(logfile, "comprobarBorradoBackup(): " + ex.getMessage());
        }
    }

    private void borradoBackups(Calendar time, File[] files) {
        for (File file : files) {
            Date lastModified = new Date(file.lastModified());//            Date x=time.getTime();            x=x;
            if (lastModified.before(time.getTime())) {
                file.delete();
            }
        }
    }

    public void borradoEnvios() {
        Log.e("errorev", "entra a borradoEnvios1");
        File Archivo1 = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/ENVIOSGPRS.SDA");
        File Archivo2 = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/ENVIOGPRS" + serialPDA + ".SDA");
        File ArchivoNoEnviadosF = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/BKENVIOSGPRS.SDA");

        if (Archivo1.exists()) {
            Archivo1.delete();
        }
        if (Archivo2.exists()) {
            Archivo2.delete();
        }
        if (ArchivoNoEnviadosF.exists()) {
            ArchivoNoEnviadosF.delete();
        }
        Log.e("errorev", "entra a borradoEnvios2");

    }

    public void crearArchivoO() {
        File desc = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/O" + Ciclo + Municipio + Seccion + Division + ".SDA"); //Ax: EJ: O108010.009
        utils.EscribirLinea(desc, "O" + Ciclo + Municipio + Seccion + Division);
    }

    /**
     * Esta funcion se encarga de mostrar una advertencia con un diseño personalizado
     **/

    public void mensajeAdmCnf(Activity activity, String msg, String tittle, int color_tp) {

        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView text = (TextView) dialog.findViewById(R.id.txtMensaje);
        TextView text_tittle = (TextView) dialog.findViewById(R.id.tittle_msg);
        CardView card_view = (CardView) dialog.findViewById(R.id.cardview_dg);

        card_view.setBackgroundColor(Color.rgb(139, 228, 176)); // y este a un verde
        if (color_tp == 1) {
            entrega_carta = 0;
            card_view.setBackgroundColor(Color.rgb(178, 190, 229)); //Creo  que  este le cambia el color a azul
        }
        text_tittle.setText(tittle);
        text.setText(msg);

        Button dialogBtn_cancel = (Button) dialog.findViewById(R.id.btnClose);
        dialogBtn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (color_tp == 1) {
                    entrega_carta = 0;
                    abrirArchivosDeFacturacion();

                    if ((variables.direcciondelectura == variables.haciaadelante)) {
                        if (avanzarRegistro() == 0) {
                            retrocedeRegistro();
                        }
                    } else {
                        if (retrocedeRegistro() == 0) {
                            avanzarRegistro();
                        }
                    }

                    variables.numdigitos = 0;

                    if (indicadorManual == 0) {
                        if (VariablesGlobales.totalprediosleidos == infoClienteEntrada.getTotal_TablaEntradaClientes()) {
                            mensajeT("AAA. Proceso de Lecturas concluido ", msgLargo);
                            visualizarInformacionCliente(3);
                        } else
                            visualizarInformacionCliente(0);
                    }
                    cerrarArchivosFacturacion();
                }

            }
        });

        dialog.show();
    }

    //nuevo procedimiento
    public void tomarLector() {
        //nuevo__________________________________________________________________________________
        tablaEncabezado.setArchivo_TablaEncabezado(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/GENERAL.TXT");
        File general = new File(tablaEncabezado.getArchivo_TablaEncabezado());
        if (general.exists()) {
            if (tablaEncabezado.abrir_TablaEncabezado(tablaEncabezado.getArchivo_TablaEncabezado())) {

                tablaEncabezado.lectura_TablaEncabezado(1);
                variables.setGlobaloperario(tablaEncabezado.gettablaEncabezado_LECTOR());
                VariablesGlobales.setDistanciagps(tablaEncabezado.getTablaEncabezado_distanciagps());
                lector = variables.getGlobaloperario();
                //VariablesGlobales.setDistanciagps(tablaEncabezado.getTablaEncabezado_distanciagps());
                //VariablesGlobales.setNrodias(tablaEncabezado.getTablaEncabezado_nrodias());
                //VariablesGlobales.setFechainicial(tablaEncabezado.getTablaEncabezado_fechainicial());
                //VariablesGlobales.setFechafinal(tablaEncabezado.getTablaEncabezado_fechafinal());
                //VariablesGlobales.setObligafotos(tablaEncabezado.getTablaEncabezado_obligafotos());
                //VariablesGlobales.setObligabarras("0");//tablaEncabezado.getTablaEncabezado_obligabarras()
                //VariablesGlobales.setMaximoregaenviar(tablaEncabezado.getTablaEncabezado_maximoregaenviar());
                //VariablesGlobales.setTipoDeRuta(tablaEncabezado.getTablaEncabezado_tiporuta());
                //VariablesGlobales.EvaluarCritica40 = Integer.parseInt(tablaEncabezado.getTablaEncabezado_obligabarras().trim());
                tablaEncabezado.Cerrar_TablaEncabezado();
            }

        }
    }

//fin de lo nuevo
}