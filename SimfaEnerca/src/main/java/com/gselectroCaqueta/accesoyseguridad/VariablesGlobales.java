package com.gselectroCaqueta.accesoyseguridad;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.gselectroCaqueta.modulobluetooth.btPrintFile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;

public class VariablesGlobales {
    // iniciacion de variables globales

    public static final int REQUEST_CONNECT_DEVICE = 3;
    public static final int REQUEST_COMUNICACIONES = 3030;
    public static final int REQUEST_ENABLE_BT = 4;
    public static final String WS_NAMESPACE = "http://tempuri.org/";
    public static final String WS_URL = "http://200.21.4.68:85/ServicioLecturasHuila.asmx";
    public static final String WS_VALIDAR_CONEXION = "http://tempuri.org/VALIDAR_CONEXION";
    public static final String WS_METHOD_NAME = "VALIDAR_CONEXION";
    public static final String WS_REGISTRAR_PROGRAMACION_ENRUTADOR = "REGISTRAR_PROGRAMACION_ENRUTADOR";
    public static final String WS_OBTENER_LONGITUD_ARCHIVO = "OBTENER_LONGITUD_ARCHIVO";
    public static final String WS_ENVIAR_DEL_SERVIDOR_AL_PDA = "ENVIAR_DEL_SERVIDOR_AL_PDA";
    public static final String WS_BORRAR_ARCHIVO_DEL_SERVIDOR = "BorrarArchivoDelServidor";
    public static String ultimaNovedad = "0000";
    public static int habilitadaimpresora = 0;
    public static int rangoinicialimpresion;
    public static int rangofinalimpresion;
    public static int opcionmenuseleccion;
    public static int activarcamarafotografica;
    public static int cierreforzadoruta;
    public static String datodebusqueda;
    public static String tipoDeRuta;
    public static int tipodebusqueda;
    public static int intcontroltexto = 1;//Ax guarda el tamaño max de textview de lectura
    // Inicio variables globales que permiten hacer los contes del sistema
    // actual
    public static int registroactual;
    public static int pos_registrador;
    public static int totallecturas = 0;
    public static int totalcausasnolectura = 0;
    public static int totalnuevos = 0;
    public static int totalinformes = 0;
    //
    public static int totalprediosleidos = 0;
    public static int totalpredioscomentarios = 0;
    public static int totalprediosliquidados = 0;
    public static int totalprediosimpresos = 0;
    public static int totalprediosnofacturados = 0;
    public static int totalimpresiones = 0;
    public static int totalregistrosleidos = 0;
    public static String nombreLectorPDA = "";
    // Servicio de Impresion bluetooth
    public static btPrintFile btPrintService = null;
    public static boolean bDiscoveryStarted = false;
    public static BluetoothAdapter mBluetoothAdapter = null;
    public static String remoteDevice;
    // Adicionada en la version para android: direccion mac de la impresora
    // detectada
    public static String printerMacAddress = "";
    public static String administrador = "912";
    public static String consumoauditoria = "50";
    public static String distanciagps = "200";
    public static String nrodias = "20";
    public static String fechainicial = "20171201";
    public static String fechafinal = "20200130";
    public static String obligafotos = "0";
    public static String obligabarras = "0";
    public static String maximoregaenviar = "20";
    public static String maximovalorentrega = "1000000";
    public static String minimovalorentrega = "1000000";
    public static String urlGlobal = "";
    public static int imprimirSoloPostal = 0;
    public static Date fechahoraGPS = new Date();
    public static String activar_validar_horario = "0";
    public static String directorioactual = "";
    public static String fechaproceso = "";
    public static String directorioBackUp = ""; //Ax: aqui guardara los Backups, si no existe sera igua al: directorioactual
    // Name of the connected device
    static String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    static ArrayAdapter<String> mConversationArrayAdapter;
    //nueva
    static String removableStoragePath = "";
    // serie de variables de impresion

    //serie de variables de impresion
    //lineas de impresion
    public int L1  = 0;
    public int L2  = 0;
    public int L3  = 0;
    public int L4  = 0;
    public int L5  = 0;
    public int L6  = 0;
    public int L7  = 0;
    public int L8  = 0;
    public int L9  = 30;
    public int L10 = 175;
    public int L11 = 260;
    public int L12 = 305;
    public int L13 = 327;
    public int L14 = 377;
    public int L15 = 402;
    public int L16 = 430;
    public int L17 = 456;
    public int L18 = 514;
    public int L19 = 536;
    public int L20 = 561;
    public int L21 = 677;
    //nuevas lineas para programar
    public int L22  = 735;
    public int L23  = 757;
    public int L24  = 777;
    public int L25  = 797;

    //fn nuevas lineas
    //public int L26no= 758;//q paso con esta
    public int L26 = 765;
    public int L27 = 790;
    public int L28 = 810;
    public int L29 = 830;
    public int L30 = 850;
    public int L31 = 875;
    public int L32 = 860;
    public int L33 = 885;
    public int L34 = 1540;
    public int L35 = 1595;
    public int L36 = 1620;
    public int L37 = 1650;
    public int L38 = 1675;
    public int L39 = 1695;

    public int L40 = 1800;
    public int L41 = 1850;
    public int L42 = 1880;
    public int L43 = 2165;

    public int L44 = 245;

    public int L45 = 635;
    public int L46 = 655;
    public int L47  = 1695;
    public int L48 = 0;
    public int L49 = 0;
    public int L50 = 0;
    public int L51 = 0;
    public int L52 = 0;
    public int L53 = 0;
    public int L54 = 0;
    public int L55 = 895;
    public int L56 = 900;

    public int L57 = 980;
    public int L58 = 1000;
    public int L59 = 1020;
    public int LB60 = 1040;
    public int LB60_1 = 1040;
    public int L61 = 1065;
    public int L62 = 1090;
    public int L63 = 1115;
    public int L64 = 1200;
    public int L65 = 1220;
    //nuevos campo para la facturador
    public int L66 = 650;
    public int L67 = 837;
    public int L68 = 858;
    public int L69 = 1530;
    public int L70 = 1560;
    public int L71 = 1592;
    public int L72 = 1625;
    public int L73 = 1660;
    public int L73_1 = 1700;
    public int L74 = 1373;
    public int L75 = 1402;
    public int L76 = 1428;
    public int L76_1 = 690;
    public int L76_2 = 1800;


    //nuevas posiciones para el formulario de caqueta
    //Variables de Impresion
    public String V1 = "! 0 200 200 2720 1";//32 2195
    public String V2 = "LABEL";//ojo tiene Label
    public String V3 = "CONTRAST 0";
    public String V4 = "TONE ";
    public String V5 = "SPEED 5";// Ma: AQUI IBA EL SPEED 5
    public String V6 = "PAGE-WIDTH 803";
    public String V7 = "BAR-NONE";
    public String V8 = "POSTFEED 0";//nuevo la velocidad
    public String V9 = ";// PAGE 0000000008032200";

    //Ma: Para hacer codigos QR

//    public String V6 = "B QR 10 100 M 2 U 6";
//    public String V7 = "M0A,https://www.cafevalparaiso.com.co/menu/";
//    public String V8 = "ENDQR";

    public String V10= "T 7 0 570  ";
    public String V11 = "T 5 1 590  ";
    public String V12 = "T 7 0 20   ";//antes 10
    public String V13 = "T 7 0 20   ";//antes 10
    public String V14 = "T 7 0 20   ";//antes 10
    public String V15 = "T 7 0 20   ";//antes 10
    public String V16 = "T 7 0 20   ";//antes 10
    public String V17 = "T 7 0 20   ";//antes 10
    public String V18 = "T 7 0 20   ";//antes 10
    public String V19 = "T 7 0 20   ";//antes 10

    public String V20 = "LINE ";
    public String V21 = "LINE ";
    public String V22 = "LINE ";
    public String V23 = "LINE ";
    public String V24 = "LINE ";
    public String V25 = "LINE ";
    public String V26 = "LINE ";



    public String V27 = "T 0 2 60   ";
    public String V28 = "T 0 2 20   ";
    public String V29 = "T 0 2 14   ";
    public String V30 = "T 7 0 10   ";//antes 10
    public String V31 = "T 7 0 120  ";
    public String V32 = "T 7 0 210  ";
    public String V33 = "T 7 0 320  ";
    public String V34 = "T 7 0 430  ";
    public String V35 = "T 7 0 488  ";
    public String V36 = "T 7 0 620  ";
    public String V37 = "T 7 0 10   ";
    public String V38 = "T 7 0 10   ";
    public String V39 = "T 7 0 40   ";
    public String V40 = "T 7 0 40   ";
    public String V41 = "T 7 0 40   ";
    public String V42 = "T 7 0 40   "; //retiro para incluir en los liquidacor
    public String V43 = "T 7 0 40   ";//retiro para incluir en los liquidacor
    public String V44 = "T 7 0 40   ";
    public String V45 = "T 7 0 40   ";
    public String V46 = "T 7 0 460  "; // CODIGO DE CALIDAD
    public String V47 = "T 7 0 40   ";

    public String V48 = "T 7 2 506  ";
    public String V49 = "T 7 0 320  ";
    public String V50 = "T 7 0 320  ";
    public String V51 = "T 7 0 155  "; //Ma: 320
    public String V52 = "T 7 0 355  ";//modificar aqui Ma: 536
    public String V53 = "T 7 2 540  ";
    public String V54 = "T 7 0 560  ";
    public String V55 = "T 7 0 26   ";//mensaje1
    public String V56 = "T 7 0 26   ";
    public String V57 = "T 7 0 26   ";
    public String V58 = "T 7 0 120  ";
    public String V59 = "T 7 0 120  ";
    public String B60 =   "B UCCEAN128 1 3 90 100 ";
    public String B60_1 = "B UCCEAN128 1 3 30 100 ";

    public String V61 = "T 7 0 100  ";
    public String V62 = "T 7 0 150  ";
    public String V63 = "T 7 0 150  ";
    public String V64 = "T 7 0 150  ";
    public String V65 = "T 7 2 590  ";

    //nuevos lineas
    public String V66 = "T 7 0 180  ";
    public String V67 = "T 7 0 25   ";
    public String V68 = "T 7 0 15   ";

    public String V69 = "T 7 0 80  ";
    public String V70 = "T 7 0 80 ";//before T 7 0 280
    public String V71 = "T 7 0 80 ";//before T 7 0 80
    public String V72 = "T 7 0 80 ";//before T 7 0 80
    public String V73 = "T 7 0 80 ";//before T 7 0 80
    public String V73_1 = "T 7 0 80 ";//before T 7 0 80

    public String V74 = "T 7 0 350  ";
    public String V75 = "T 7 0 125  ";
    public String V76   = "T 7 0 125  ";//Ma: movi 210
    public String V76_1 = "T 7 1 500  ";

    public String V76_2 = "T 0 0 30  ";


    public String V77 = "PCX 0 0 !<fscaq.pcx";

    public String V78 = ";// FORM";

    public String V79 = "PRINT";






    String globalturno;
    String globaloperario;
    String globalanomalia;
    String nombregeneral;
    String nombrepredio;
    int globalregistroactual;
    String globalcentrocosto;
    int impresionenlote;
    int contadoractual;
    int clienteactual;
    int haciaadelante = 1;
    int haciaatras = 0;
    int direcciondelectura = 1;
    int aplicafecha = 0;
    String mesactual = "";
    String terminal = "";
    String impresora = "";
    String impresoraAnalitica = "";
    String estado = "";
    int modosupervisor = 0;
    int tipoimpresora = 0;
    double ultimotiempo = 1;
    String mesenformato;
    int ultimoregistro = 0;
    String medidoranterior = "";
    String nuevafecha;
    String formatofinanciero = "";
    int tienedeuda = 0;
    int barrido = 0;
    int nveces = 0;
    double consumoafacturar = 0;
    double valorconsumo = 0;
    double valorconsumo2 = 0;
    String consumoenergiaactual = "";
    String valorfacturadoenergia = "";
    int diashoy = 0;
    double consumoactual = 0;
    double lactual;
    double cactual;
    double lect2 = -1;
    double lect3 = -1;
    String amd = "";
    String hm = "";
    String fechahoy = "";
    int numdigitos;
    // creacion de las variables totales para el resume del proyecto
    int cuentamalajustada = 0;
    double pesosenergia = 0;
    double totalconsumoperiodo = 0;
    // fin variables principales para conteos
    // Fin Variables totalizadoras
    // <summary>
    double pesosenergiareactiva = 0;
    double facturapreimpresa = 1;
    int diasdecobro;
    String versionpc;
    int espc = 0;
    int activatransmision = 0;
    int limiteimpresion = 1;
    Context ctx;


    public static int EvaluarCritica40 = 0;
    public static int diasvence = 0;
    public static int diascorte = 0;

    public static String coorTempLongitud = "0.0";
    public static String coorTempLatitud = "0.0";

    public static String getActivar_validar_horario() {
        return activar_validar_horario;
    }

    public static void setActivar_validar_horario(String activar_validar_horario) {
        VariablesGlobales.activar_validar_horario = activar_validar_horario;
    }

    public static String getUltimaNovedad() {
        return ultimaNovedad;
    }

    public static void setUltimaNovedad(String ultimaNovedad) {
        VariablesGlobales.ultimaNovedad = ultimaNovedad;
    }

    public static int getHabilitadaimpresora() {
        return habilitadaimpresora;
    }

    public static void setHabilitadaimpresora(int habilitadaimpresora) {
        VariablesGlobales.habilitadaimpresora = habilitadaimpresora;
    }

    public static int getRangoinicialimpresion() {
        return rangoinicialimpresion;
    }

    public static void setRangoinicialimpresion(int rangoinicialimpresion) {
        VariablesGlobales.rangoinicialimpresion = rangoinicialimpresion;
    }

    public static int getRangofinalimpresion() {
        return rangofinalimpresion;
    }

    public static void setRangofinalimpresion(int rangofinalimpresion) {
        VariablesGlobales.rangofinalimpresion = rangofinalimpresion;
    }

    public static int getOpcionmenuseleccion() {
        return opcionmenuseleccion;
    }

    public static void setOpcionmenuseleccion(int opcionmenuseleccion) {
        VariablesGlobales.opcionmenuseleccion = opcionmenuseleccion;
    }

    public static int getActivarcamarafotografica() {
        return activarcamarafotografica;
    }

    public static void setActivarcamarafotografica(int activarcamarafotografica) {
        VariablesGlobales.activarcamarafotografica = activarcamarafotografica;
    }

    public static int getCierreforzadoruta() {
        return cierreforzadoruta;
    }

    public static void setCierreforzadoruta(int cierreforzadoruta) {
        VariablesGlobales.cierreforzadoruta = cierreforzadoruta;
    }

    public static String getDatodebusqueda() {
        return datodebusqueda;
    }

    public static void setDatodebusqueda(String datodebusqueda) {
        VariablesGlobales.datodebusqueda = datodebusqueda;
    }

    public static String getTipoDeRuta() {
        return tipoDeRuta;
    }

    public static void setTipoDeRuta(String tipoDeRuta) {
        VariablesGlobales.tipoDeRuta = tipoDeRuta;
    }

    public static int getTipodebusqueda() {
        return tipodebusqueda;
    }

    public static void setTipodebusqueda(int tipodebusqueda) {
        VariablesGlobales.tipodebusqueda = tipodebusqueda;
    }

    public static int getRegistroactual() {
        return registroactual;
    }

    public static void setRegistroactual(int registroactual) {
        VariablesGlobales.registroactual = registroactual;
    }

    public static int getTotallecturas() {
        return totallecturas;
    }

    public static void setTotallecturas(int totallecturas) {
        VariablesGlobales.totallecturas = totallecturas;
    }

    public static int getTotalcausasnolectura() {
        return totalcausasnolectura;
    }

    public static void setTotalcausasnolectura(int totalcausasnolectura) {
        VariablesGlobales.totalcausasnolectura = totalcausasnolectura;
    }

    public static int getTotalnuevos() {
        return totalnuevos;
    }

    public static void setTotalnuevos(int totalnuevos) {
        VariablesGlobales.totalnuevos = totalnuevos;
    }

    public static int getTotalinformes() {
        return totalinformes;
    }

    public static void setTotalinformes(int totalinformes) {
        VariablesGlobales.totalinformes = totalinformes;
    }

    public static int getTotalprediosleidos() {
        return totalprediosleidos;
    }

    public static void setTotalprediosleidos(int totalprediosleidos) {
        VariablesGlobales.totalprediosleidos = totalprediosleidos;
    }

    public static int getTotalpredioscomentarios() {
        return totalpredioscomentarios;
    }

    public static void setTotalpredioscomentarios(int totalpredioscomentarios) {
        VariablesGlobales.totalpredioscomentarios = totalpredioscomentarios;
    }

    public static int getTotalprediosliquidados() {
        return totalprediosliquidados;
    }

    public static void setTotalprediosliquidados(int totalprediosliquidados) {
        VariablesGlobales.totalprediosliquidados = totalprediosliquidados;
    }

    public static int getTotalprediosimpresos() {
        return totalprediosimpresos;
    }

    public static void setTotalprediosimpresos(int totalprediosimpresos) {
        VariablesGlobales.totalprediosimpresos = totalprediosimpresos;
    }

    public static int getTotalprediosnofacturados() {
        return totalprediosnofacturados;
    }

    public static void setTotalprediosnofacturados(int totalprediosnofacturados) {
        VariablesGlobales.totalprediosnofacturados = totalprediosnofacturados;
    }

    public static int getTotalimpresiones() {
        return totalimpresiones;
    }

    public static void setTotalimpresiones(int totalimpresiones) {
        VariablesGlobales.totalimpresiones = totalimpresiones;
    }

    public static int getTotalregistrosleidos() {
        return totalregistrosleidos;
    }

    public static void setTotalregistrosleidos(int totalregistrosleidos) {
        VariablesGlobales.totalregistrosleidos = totalregistrosleidos;
    }

    public static String getNombreLectorPDA() {
        return nombreLectorPDA;
    }

    public static void setNombreLectorPDA(String nombreLectorPDA) {
        VariablesGlobales.nombreLectorPDA = nombreLectorPDA;
    }

    public static String getAdministrador() {
        return administrador;
    }

    public static void setAdministrador(String administrador) {
        VariablesGlobales.administrador = administrador;
    }

    public static String getConsumoauditoria() {
        return consumoauditoria;
    }

    public static void setConsumoauditoria(String consumoauditoria) {
        VariablesGlobales.consumoauditoria = consumoauditoria;
    }

    public static String getDistanciagps() {
        return distanciagps;
    }

    public static void setDistanciagps(String distanciagps) {
        VariablesGlobales.distanciagps = distanciagps;
    }

    public static String getNrodias() {
        return nrodias;
    }

    public static void setNrodias(String nrodias) {
        VariablesGlobales.nrodias = nrodias;
    }

    public static String getFechainicial() {
        return fechainicial;
    }

    public static void setFechainicial(String fechainicial) {
        VariablesGlobales.fechainicial = fechainicial;
    }

    public static String getFechafinal() {
        return fechafinal;
    }

    public static void setFechafinal(String fechafinal) {
        VariablesGlobales.fechafinal = fechafinal;
    }

    public static String getObligafotos() {
        return obligafotos;
    }

    public static void setObligafotos(String obligafotos) {
        VariablesGlobales.obligafotos = obligafotos;
    }

    public static String getObligabarras() {
        return obligabarras;
    }

    public static void setObligabarras(String obligabarras) {
        VariablesGlobales.obligabarras = obligabarras;
    }

    public static String getMaximoregaenviar() {
        return maximoregaenviar;
    }

    public static void setMaximoregaenviar(String maximoregaenviar) {
        VariablesGlobales.maximoregaenviar = maximoregaenviar;
    }

    public static String getMaximovalorentrega() {
        return maximovalorentrega;
    }

    public static void setMaximovalorentrega(String maximovalorentrega) {
        VariablesGlobales.maximovalorentrega = maximovalorentrega;
    }

    public static String getMinimovalorentrega() {
        return minimovalorentrega;
    }

    public static void setMinimovalorentrega(String minimovalorentrega) {
        VariablesGlobales.minimovalorentrega = minimovalorentrega;
    }

    public static String getUrlGlobal() {
        return urlGlobal;
    }

    public static void setUrlGlobal(String urlGlobal) {
        VariablesGlobales.urlGlobal = urlGlobal;
    }

    public static int getImprimirSoloPostal() {
        return imprimirSoloPostal;
    }

    public static void setImprimirSoloPostal(int imprimirSoloPostal) {
        VariablesGlobales.imprimirSoloPostal = imprimirSoloPostal;
    }

    public static Date getFechahoraGPS() {
        return fechahoraGPS;
    }

    public static void setFechahoraGPS(Date fechahoraGPS) {
        VariablesGlobales.fechahoraGPS = fechahoraGPS;
    }

    public static String getDirectorioactual() {
        return directorioactual;
    }



    //Ax: envia una prueba para saber si la impresora esta activa
    public static boolean prueba_impresora() {

//        if (habilitadaimpresora < 1) {
//            return false;
//        }

        byte[] outputData;
        String trozo = "";
        outputData = trozo.getBytes();

        if (btPrintService.write(outputData)) {
            return true;
        }
        return false;
    }

    //Ax: envia una prueba de impresion, se elimina la crecio y lectura de un archivo (textcabezal.txt)
    public static int prueba_cabeza()// (int donde)
    {
        if (habilitadaimpresora < 1) {
            return 0;
        }

        try {
            String texto = "! 0 200 200 560 1\r\n"
                    + "JOURNAL\r\n"// LABEL
                    + "CONTRAST 0\r\n" + "TONE 0\r\n" + "SPEED 5\r\n" + "PAGE-WIDTH 780\r\n" + "NONE-SENSE\r\n" + "POSTFEED 20\r\n"
                    + ";// PAGE 0000000007800560\r\n" + "LINE 449 154 449 220 341\r\n" + "LINE 0 22 0 88 271\r\n" + "LINE 0 0 0 24 793\r\n"
                    + "LINE 189 88 189 154 341\r\n" + "LINE 0 221 0 287 793\r\n" + "\r\n"// FORM
                    + "PRINT" + "\r\n";
            String trozo = "";
            int tamano = texto.length();
            byte[] outputData;
            int i = 0;

            for (i = 0; i <= texto.length(); ) {
                if (tamano >= 35)
                    trozo = texto.substring(i, (i + 35));
                else
                    trozo = texto.substring(i, (i + tamano));
                outputData = trozo.getBytes();

                tamano -= 35;
                i = i + 35;
                if (!btPrintService.write(outputData)) {
                    return (-1);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return (1);
    }

    public static void copyFile(String fileA, String fileB, boolean deleteFile) {

        InputStream inStream = null;
        OutputStream outStream = null;

        try {

            File afile = new File(fileA);
            File bfile = new File(fileB);

            inStream = new FileInputStream(afile);
            outStream = new FileOutputStream(bfile);

            byte[] buffer = new byte[1024];

            int length;
            // copy the file content in bytes
            while ((length = inStream.read(buffer)) > 0) {

                outStream.write(buffer, 0, length);

            }

            inStream.close();
            outStream.close();

            // delete the original file
            if (deleteFile) {
                afile.delete();
                System.out.println("File was moved successful!");
            } else {
                System.out.println("File is copied successful!");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // / <summary>
    public String getGlobalturno() {
        return globalturno;
    }

    public void setGlobalturno(String globalturno) {
        this.globalturno = globalturno;
    }

    public String getGlobaloperario() {
        return globaloperario;
    }

    public void setGlobaloperario(String globaloperario) {
        this.globaloperario = globaloperario;
    }

    public String getGlobalanomalia() {
        return globalanomalia;
    }

    public void setGlobalanomalia(String globalanomalia) {
        this.globalanomalia = globalanomalia;
    }

    public String getNombregeneral() {
        return nombregeneral;
    }

    public void setNombregeneral(String nombregeneral) {
        this.nombregeneral = nombregeneral;
    }

    public String getNombrepredio() {
        return nombrepredio;
    }

    public void setNombrepredio(String nombrepredio) {
        this.nombrepredio = nombrepredio;
    }

    public int getGlobalregistroactual() {
        return globalregistroactual;
    }

    public void setGlobalregistroactual(int globalregistroactual) {
        this.globalregistroactual = globalregistroactual;
    }

    public String getGlobalcentrocosto() {
        return globalcentrocosto;
    }

    public void setGlobalcentrocosto(String globalcentrocosto) {
        this.globalcentrocosto = globalcentrocosto;
    }

    public int getImpresionenlote() {
        return impresionenlote;
    }

    public void setImpresionenlote(int impresionenlote) {
        this.impresionenlote = impresionenlote;
    }

    public int getContadoractual() {
        return contadoractual;
    }

    public void setContadoractual(int contadoractual) {
        this.contadoractual = contadoractual;
    }

    public int getClienteactual() {
        return clienteactual;
    }

    public void setClienteactual(int clienteactual) {
        this.clienteactual = clienteactual;
    }

    public int getHaciaadelante() {
        return haciaadelante;
    }

    public void setHaciaadelante(int haciaadelante) {
        this.haciaadelante = haciaadelante;
    }

    public int getHaciaatras() {
        return haciaatras;
    }
    // public String V12 = "T 7 0 186";
    // public String V13 = "T 7 0 186";

    public void setHaciaatras(int haciaatras) {
        this.haciaatras = haciaatras;
    }

    public int getDirecciondelectura() {
        return direcciondelectura;
    }

    public void setDirecciondelectura(int direcciondelectura) {
        this.direcciondelectura = direcciondelectura;
    }

    public int getAplicafecha() {
        return aplicafecha;
    }

    public void setAplicafecha(int aplicafecha) {
        this.aplicafecha = aplicafecha;
    }

    public String getMesactual() {
        return mesactual;
    }

    public void setMesactual(String mesactual) {
        this.mesactual = mesactual;
    }

    public String getTerminal() {
        return terminal;
    }

    public void setTerminal(String terminal) {
        this.terminal = terminal;
    }

    public String getImpresora() {
        return impresora;
    }

    public void setImpresora(String impresora) {
        this.impresora = impresora;
    }

    public String getImpresoraAnalitica() {
        return impresoraAnalitica;
    }

    public void setImpresoraAnalitica(String impresoraAnalitica) {
        this.impresoraAnalitica = impresoraAnalitica;
    }


    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getModosupervisor() {
        return modosupervisor;
    }

    public void setModosupervisor(int modosupervisor) {
        this.modosupervisor = modosupervisor;
    }

    public int getTipoimpresora() {
        return tipoimpresora;
    }

    public void setTipoimpresora(int tipoimpresora) {
        this.tipoimpresora = tipoimpresora;
    }

    public double getUltimotiempo() {
        return ultimotiempo;
    }

    public void setUltimotiempo(double ultimotiempo) {
        this.ultimotiempo = ultimotiempo;
    }

    public String getMesenformato() {
        return mesenformato;
    }

    public void setMesenformato(String mesenformato) {
        this.mesenformato = mesenformato;
    }

    public int getUltimoregistro() {
        return ultimoregistro;
    }
    //

    public void setUltimoregistro(int ultimoregistro) {
        this.ultimoregistro = ultimoregistro;
    }

    public String getMedidoranterior() {
        return medidoranterior;
    }

    public void setMedidoranterior(String medidoranterior) {
        this.medidoranterior = medidoranterior;
    }
    // incluir en los liquidacor

    public String getNuevafecha() {
        return nuevafecha;
    }

    public void setNuevafecha(String nuevafecha) {
        this.nuevafecha = nuevafecha;
    }

    public String getFormatofinanciero() {
        return formatofinanciero;
    }

    public void setFormatofinanciero(String formatofinanciero) {
        this.formatofinanciero = formatofinanciero;
    }

    public int getTienedeuda() {
        return tienedeuda;
    }

    public void setTienedeuda(int tienedeuda) {
        this.tienedeuda = tienedeuda;
    }

    public int getBarrido() {
        return barrido;
    }

    public void setBarrido(int barrido) {
        this.barrido = barrido;
    }

    public int getNveces() {
        return nveces;
    }

    public void setNveces(int nveces) {
        this.nveces = nveces;
    }

    public double getConsumoafacturar() {
        return consumoafacturar;
    }

    public void setConsumoafacturar(double consumoafacturar) {
        this.consumoafacturar = consumoafacturar;
    }

    public String getConsumoenergiaactual() {
        return consumoenergiaactual;
    }

    public void setConsumoenergiaactual(String consumoenergiaactual) {
        this.consumoenergiaactual = consumoenergiaactual;
    }

    public int getDiashoy() {
        return diashoy;
    }

    public void setDiashoy(int diashoy) {
        this.diashoy = diashoy;
    }

    public double getConsumoactual() {
        return consumoactual;
    }

    public void setConsumoactual(double consumoactual) {
        this.consumoactual = consumoactual;
    }

    public double getLactual() {
        return lactual;
    }

    public void setLactual(double lactual) {
        this.lactual = lactual;
    }

    public double getCactual() {
        return cactual;
    }

    public void setCactual(double cactual) {
        this.cactual = cactual;
    }

    public double getLect2() {
        return lect2;
    }

    public void setLect2(double lect2) {
        this.lect2 = lect2;
    }

    public double getLect3() {
        return lect3;
    }

    public void setLect3(double lect3) {
        this.lect3 = lect3;
    }

    public String getAmd() {
        return amd;
    }

    public void setAmd(String amd) {
        this.amd = amd;
    }

    public String getHm() {
        return hm;
    }

    public void setHm(String hm) {
        this.hm = hm;
    }

    public String getFechahoy() {
        return fechahoy;
    }

    public void setFechahoy(String fechahoy) {
        this.fechahoy = fechahoy;
    }

    public int getNumdigitos() {
        return numdigitos;
    }

    public void setNumdigitos(int numdigitos) {
        this.numdigitos = numdigitos;
    }

    public int getCuentamalajustada() {
        return cuentamalajustada;
    }

    public void setCuentamalajustada(int cuentamalajustada) {
        this.cuentamalajustada = cuentamalajustada;
    }

    public double getPesosenergia() {
        return pesosenergia;
    }

    public void setPesosenergia(double pesosenergia) {
        this.pesosenergia = pesosenergia;
    }

    public double getTotalconsumoperiodo() {
        return totalconsumoperiodo;
    }

    public void setTotalconsumoperiodo(double totalconsumoperiodo) {
        this.totalconsumoperiodo = totalconsumoperiodo;
    }

    public double getPesosenergiareactiva() {
        return pesosenergiareactiva;
    }

    public void setPesosenergiareactiva(double pesosenergiareactiva) {
        this.pesosenergiareactiva = pesosenergiareactiva;
    }

    public double getFacturapreimpresa() {
        return facturapreimpresa;
    }

    public void setFacturapreimpresa(double facturapreimpresa) {
        this.facturapreimpresa = facturapreimpresa;
    }

    public int getDiasdecobro() {
        return diasdecobro;
    }

    public void setDiasdecobro(int diasdecobro) {
        this.diasdecobro = diasdecobro;
    }

    public int getEspc() {
        return espc;
    }

    public void setEspc(int espc) {
        this.espc = espc;
    }

    public int getActivatransmision() {
        return activatransmision;
    }

    public void setActivatransmision(int activatransmision) {
        this.activatransmision = activatransmision;
    }

    public int getLimiteimpresion() {
        return limiteimpresion;
    }

    public void setLimiteimpresion(int limiteimpresion) {
        this.limiteimpresion = limiteimpresion;
    }

    // / validacion de fechas
    public String convertirDiasAFecha(long dias, String fecha, String fechahoy) {

        // Se modifica este metodo con utilidades de Java, devuelve fechahoy
        // sumandole la cantidad que llega en dias
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, (int) dias);
        fecha = "" + calendar.get(Calendar.YEAR) + "" + calendar.get(Calendar.MONTH) + "" + calendar.get(Calendar.DAY_OF_MONTH);
        return fecha;
    }

    public long calcularNumerodeDias(String fechafuente) {
        String anno, mees, diia;
        int nroannos, nromeeses, nrodiias, correccionmes;
        long nrodias;

        anno = fechafuente.substring(0, 4);
        mees = fechafuente.substring(4, 6);
        diia = fechafuente.substring(6);
        nroannos = Integer.parseInt(anno);
        nromeeses = Integer.parseInt(mees);
        nrodiias = Integer.parseInt(diia);

        nrodias = (long) (nroannos - 2001) * 365 + ((nromeeses - 1) * 30) + nrodiias;

        if (nroannos >= 2000) {
            if (nroannos == 2000) {
                if (nromeeses > 2)
                    ++nrodias;
            } else
                ++nrodias;
        }

        if (nroannos >= 2004) {
            if (nroannos == 2004) {
                if (nromeeses > 2)
                    ++nrodias;
            } else
                ++nrodias;
        }

        if (nroannos >= 2008) {
            if (nroannos == 2008) {
                if (nromeeses > 2)
                    ++nrodias;
            } else
                ++nrodias;
        }

        switch (nromeeses) {
            default:
            case 1:
                correccionmes = 0;
                break;
            case 2:
                correccionmes = 1;
                break;
            case 3:
                correccionmes = -2;
                break;
            case 4:
                correccionmes = 0;
                break;
            case 5:
                correccionmes = 0;
                break;
            case 6:
                correccionmes = 1;
                break;
            case 7:
                correccionmes = 1;
                break;
            case 8:
                correccionmes = 2;
                break;
            case 9:
                correccionmes = 3;
                break;
            case 10:
                correccionmes = 3;
                break;
            case 11:
                correccionmes = 4;
                break;
            case 12:
                correccionmes = 4;
                break;
        }
        nrodias += correccionmes;

        return (nrodias);
    }

    public int evaluarConServicio(String variable) {
        if ((variable.equals("Z"))) {
            return (0);
        }
        return (1);
    }

    public double ejecutarAjusteUnidades(double pesos) {
        long auxiliar = (long) pesos;
        double partedecimal;
        partedecimal = pesos - (double) auxiliar;
        if (partedecimal < (double) 0.0) {
            if (partedecimal <= (double) -0.50)
                pesos = (double) auxiliar - 1;
            else
                pesos = (double) auxiliar;
        } else {
            if (partedecimal >= (double) 0.50)
                pesos = (double) auxiliar + 1;
            else
                pesos = (double) auxiliar;
        }
        return (pesos);
    }

    public String convertirMesLetras(String mes) {
        String MesActual = "NNN";

        try {
            switch (Integer.parseInt(String.format("%02d", Integer.parseInt(mes.trim()))))// (Integer.parseInt(Mes.trim().PadLeft(2,'0')))
            {
                case 1:
                    MesActual = "ENE";
                    break;
                case 2:
                    MesActual = "FEB";
                    break;
                case 3:
                    MesActual = "MAR";
                    break;
                case 4:
                    MesActual = "ABR";
                    break;
                case 5:
                    MesActual = "MAY";
                    break;
                case 6:
                    MesActual = "JUN";
                    break;
                case 7:
                    MesActual = "JUL";
                    break;
                case 8:
                    MesActual = "AGO";
                    break;
                case 9:
                    MesActual = "SEP";
                    break;
                case 10:
                    MesActual = "OCT";
                    break;
                case 11:
                    MesActual = "NOV";
                    break;
                case 12:
                    MesActual = "DIC";
                    break;
            }
        } catch (Exception e) {
            Calendar c = Calendar.getInstance();
            return (convertirMesLetras(c.get(Calendar.MONTH) + 1 + ""));
            //throw new RuntimeException("[VariablesGlobales] convertirMesLetras(): "+ e.getMessage()); //Ax: excepcion personalizada todo: se debe replicar en todo lado
        }
        return (MesActual);
    }

    // / fin validacion de fecha
    // / </summary>
    // / <param name="Tipo"></param>
    // / <param name="e"></param>
    // / <returns></returns>
    // funciones Especiales para el sistema
    public String encapsular(String cadenaOrigen) {
        int i = 0;
        int tamTrama = 0;
        String respuesta = "";
        String alfaNumerico = "*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ " + "\n" + "\r";
        String cadenaencapsulada =  "DCRFVTGBYH";
        String nuevaCadena = "QAZWSX" + cadenaencapsulada + "NUJM IKOLP*>" + "+=,<-;.:/908?172634|[@" + "\n" + "\r";

        for (i = 0; i < cadenaOrigen.length(); i++) {
            for (tamTrama = 0; tamTrama < alfaNumerico.length(); tamTrama++) {
                if (cadenaOrigen.substring(i, i + 1).equals(alfaNumerico.substring(tamTrama, tamTrama + 1))) {
                    respuesta += nuevaCadena.substring(tamTrama, tamTrama + 1);
                }
            }
        }
        return (respuesta);
    }

    public String desencapsular(String cadenaOrigen) {
        int i = 0;
        int tamTrama = 0;
        String respuesta = "";
        String alfaNuerico = "*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ " + "\n" + "\r";
        String cadenaencapsulada =  "DCRFVTGBYH";

        String nuevaCadena = "QAZWSXDCRFVTGBYHNUJM IKOLP*>" + "+=,<-;.:/908?172634[|@" + "\n" + "\r";

        for (i = 0; i < cadenaOrigen.length(); i++) {
            for (tamTrama = 0; tamTrama < nuevaCadena.length(); tamTrama++) {
                if (cadenaOrigen.substring(i, i + 1).equals(nuevaCadena.substring(tamTrama, tamTrama + 1))) {
                    respuesta += alfaNuerico.substring(tamTrama, tamTrama + 1);
                }
            }
        }
        return (respuesta);
    }

//    public Boolean validaTecla2(int tipo, KeyEvent e) {
//        if (tipo == 1) {
//            if (e.getKeyCode() >= 48 && e.getKeyCode() <= 57 || e.getKeyCode() == 8)
//                return (false);
//            else {
//                return (true);
//            }
//        } else
//            return (false);
//    }

//    public String getVersionpc() {
//        return versionpc;
//    }
//
//    public void setVersionpc(String versionpc) {
//        this.versionpc = versionpc;
//    }

    public void crearArchivoImpresion1(String Valortexto1, String Valortexto2, int TieneDosBarras) { //   Ax antes: CambiarValoresimpresion

        int restar=0;
        if (null == Valortexto1 || Valortexto1.trim().equals(""))
            Valortexto1 = "-30";
        if (null == Valortexto2 || Valortexto2.trim().equals(""))
            Valortexto2 = "0";

        String ArchivoFormato = directorioactual + "/ValoresFormato.log";

        File file = new File(ArchivoFormato);

        try {
            if (!file.exists()) {
                BufferedWriter archivoLineas = new BufferedWriter(new FileWriter(file));
                archivoLineas.write(Valortexto1);
                archivoLineas.write(Valortexto2);
                archivoLineas.write("1");
                archivoLineas.flush();
                archivoLineas.close();
            }
            //si existe sacar los calores requeridos del archivo guardado
            else {

                FileReader r = new FileReader(file);
                BufferedReader reader = new BufferedReader(r);
                String linea = "";
                int cont = 0;

                while ((linea = reader.readLine()) != null) {
                    cont++;
                    if (cont == 1) {
                        Valortexto1 = linea.trim();
                    } else if (cont == 2) {
                        Valortexto2 = linea.trim();
                    }
                }
                r.close();
            }

            //int valortexto1 = Integer.parseInt(Valortexto1);
            int valortexto2 = Integer.parseInt(Valortexto2);

            L1 = 0;
            L2 = 0;
            L3 = 0;
            L4 = 0;
            L5 = 0;
            L6 = 0;
            L7 = 0;
            L8 = 0;
            L8 = 0;
            L9 = 0;
            L10 = 155 + (restar) + Integer.parseInt(Valortexto2);
            L11 =  35+ (restar) + Integer.parseInt(Valortexto2);
            L12 = 120 + (restar)+ Integer.parseInt(Valortexto2);
            L13 = 155 + (restar)+ Integer.parseInt(Valortexto2);
            L14 = 240 + (restar)+ Integer.parseInt(Valortexto2);//antes
            L15 = 300 + (restar)+ Integer.parseInt(Valortexto2);
            L16 = 355 + (restar)+ Integer.parseInt(Valortexto2);
            L17 = 410 + (restar)+ Integer.parseInt(Valortexto2);//antes 390
            L18 = 495 + (restar)+ Integer.parseInt(Valortexto2);
            L19 = 535 + (restar)+ Integer.parseInt(Valortexto2);
            //vacio de las lineas
            L20 = 0;
            L21 = 0;
            L22 = 0;
            L23 = 0;
            L24 = 0;
            L25 = 0;
            L26 = 0;

            //desde la 20 a la 26

            L27 = 850 +(restar)+ Integer.parseInt(Valortexto2);//antes 729
            L28 = 850 +(restar)+ Integer.parseInt(Valortexto2);//antes 729
            L29 = 680 +(restar)+ Integer.parseInt(Valortexto2);//antes 707
            L30 = 635 +(restar)+ Integer.parseInt(Valortexto2);
            L31 = 635+(restar)+ Integer.parseInt(Valortexto2);
            L32 = 635 +(restar)+ Integer.parseInt(Valortexto2);
            L33 = 635 +(restar)+ Integer.parseInt(Valortexto2);
            L34 = 635 +(restar)+ Integer.parseInt(Valortexto2);
            L35 = 920 +(restar)+ Integer.parseInt(Valortexto2);
            L36 = 945 +(restar)+ Integer.parseInt(Valortexto2);
            L37 = 1025+(restar)+ Integer.parseInt(Valortexto2);//antes 1008
            L38 = 1110 +(restar)+ Integer.parseInt(Valortexto2);

            //Conceptos de Cobro
            L39 = 1190  +(restar)+ Integer.parseInt(Valortexto2);
            L40 = 1210 +(restar)+ Integer.parseInt(Valortexto2);
            L41 = 1230+(restar)+ Integer.parseInt(Valortexto2);
            L42 = 1250 +(restar)+ Integer.parseInt(Valortexto2);
            L43 = 1270+(restar)+ Integer.parseInt(Valortexto2);
            L44 = 1290+(restar)+ Integer.parseInt(Valortexto2);
            L45 = 1310+(restar)+ Integer.parseInt(Valortexto2);
            L46 = 1755+(restar)+ Integer.parseInt(Valortexto2);//CODIGO DE CALIDAD
            L47 = 1350+(restar)+ Integer.parseInt(Valortexto2);
            //quie es usted tptal conceptos
            L48 = 1330  +(restar)+ Integer.parseInt(Valortexto2);//antes 1295 Ma: 1267
///**********fin cobceptos
            L49 = 1645  + (restar)+ Integer.parseInt(Valortexto2);//antes 1490
            L50 = 1675 + (restar)+ Integer.parseInt(Valortexto2);//antes 1544

            L51 = 1695 +(restar)+ Integer.parseInt(Valortexto2);//antes 1569
            L52 = 1695 +(restar)+ Integer.parseInt(Valortexto2);//antes 1616

            L53 = 1470 +(restar)+ Integer.parseInt(Valortexto2);//antes 1423
            L54 = 895 + (restar)+ Integer.parseInt(Valortexto2);//antes 885
//mensajes
            L55 = 2110 +(restar)+ Integer.parseInt(Valortexto2);//1685mensaje 1  antes 1692 //1682 Ma: antes 1650
            L56 = 2130 +(restar)+ Integer.parseInt(Valortexto2);//1710 mensaje 2 antes 1712 1702 Ma: antes 1670
            L57 = 2150 +(restar)+ Integer.parseInt(Valortexto2);//1735 mensaje3  antes 1732 1722 Ma: antes 1690

            L58 = 2255 +(restar)+ Integer.parseInt(Valortexto2);//antes 1825
            L59 = 2290 +(restar)+ Integer.parseInt(Valortexto2);//antes 1850
            LB60 = 2361+(restar)+ Integer.parseInt(Valortexto2);//antes 1950 Ma:1910
            LB60_1 = 2364+(restar)+ Integer.parseInt(Valortexto2);//antes 1920 Ma:1880
            L61 = 2463+(restar)+ Integer.parseInt(Valortexto2);//antes 2050 2030 Ma:2000

            L62 = 2515 +(restar)+ Integer.parseInt(Valortexto2);//antes 2068 2058 Ma:2027
            L63 = 2540+(restar)+ Integer.parseInt(Valortexto2);//antes 2093  2083 Ma:2052
            L64 = 2565+(restar)+ Integer.parseInt(Valortexto2);//antes 2118 2108 Ma:2077
            L65 = 2585+(restar)+ Integer.parseInt(Valortexto2);//antes 2135  2125 Ma:2094
            //nuevas lineas
            L66 = 1475 + (restar)+ Integer.parseInt(Valortexto2);
            L67 = 1500 + (restar)+ Integer.parseInt(Valortexto2);
            L68 = 1525 + (restar)+ Integer.parseInt(Valortexto2);
//las seis lineas del nuevo formato de aseo
            L69 = 1550   + (restar)+ Integer.parseInt(Valortexto2);//antes 1515
            L70 = 1580   + (restar)+ Integer.parseInt(Valortexto2);//antes 1545
            L71 = 1610   + (restar)+ Integer.parseInt(Valortexto2);//antes 1575
            L72 = 1640   + (restar)+ Integer.parseInt(Valortexto2);//antes 1608
            L73 = 1670   + (restar)+ Integer.parseInt(Valortexto2);//antes 1637
            L73_1 = 1700 + (restar)+ Integer.parseInt(Valortexto2);//antes 1637
////****
            L74 =   1405 + (restar)+ Integer.parseInt(Valortexto2);
            L75 =   1460 + (restar)+ Integer.parseInt(Valortexto2);//antes 1410
            L76 =   1430 + (restar)+ Integer.parseInt(Valortexto2);

        //***total aseo
            L76_1 = 1695 + (restar)+ Integer.parseInt(Valortexto2);//antes
            L76_2 = 2650+(restar)+ Integer.parseInt(Valortexto2);//antes 2160 Ma:2140

            String ArchivoImprimir = directorioactual +  "/Impresion.log";
            File file2 = new File(ArchivoImprimir);

            if (file2.exists()) {
                file2.delete( );
            }

            BufferedWriter Archivoprint = new BufferedWriter(new FileWriter(file2));

            Archivoprint.write(V1);
            Archivoprint.write("\r\n");
            Archivoprint.write(V2);
            Archivoprint.write("\r\n");
            Archivoprint.write(";//"+V3);
            Archivoprint.write("\r\n");
            Archivoprint.write(";//"+V4 + Valortexto1);
            Archivoprint.write("\r\n");
            Archivoprint.write(";//"+V5);
            Archivoprint.write("\r\n");
            Archivoprint.write(V6);
            Archivoprint.write("\r\n");
            Archivoprint.write(V7);
            Archivoprint.write("\r\n");
            Archivoprint.write(V8); //variables finales para la impresion
            Archivoprint.write("\r\n");
            Archivoprint.write(V9);
            Archivoprint.write("\r\n");
            Archivoprint.write(V10 + String.format("%5s", L10+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V11 + String.format("%5s", L11+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V12 + String.format("%5s", L12+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V13 + String.format("%5s", L13+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V14 + String.format("%5s", L14+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V15 + String.format("%5s", L15+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V16 + String.format("%5s", L16+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V17 + String.format("%5s", L17+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V18 + String.format("%5s", L18+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V19 + String.format("%5s", L19+""));
            Archivoprint.write("\r\n");
            //inicio lineas de barras
            Archivoprint.write( V20);//";//" +
            Archivoprint.write("\r\n");
            Archivoprint.write( V21);//";//" +
            Archivoprint.write("\r\n");
            Archivoprint.write( V22);//";//" +
            Archivoprint.write("\r\n");
            Archivoprint.write( V23);//";//" +
            Archivoprint.write("\r\n");
            Archivoprint.write( V24);//";//" +
            Archivoprint.write("\r\n");
            Archivoprint.write( V25);//";//" +
            Archivoprint.write("\r\n");
            Archivoprint.write(V26);//";//" +
            Archivoprint.write("\r\n");
            //fin lineas de barras

            Archivoprint.write(V27 + String.format("%5s", L27+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V28 + String.format("%5s", L28+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V29 + String.format("%5s", L29+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V30 + String.format("%5s", L30+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V31 + String.format("%5s", L31+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V32 + String.format("%5s", L32+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V33 + String.format("%5s", L33+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V34 + String.format("%5s", L34+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V35 + String.format("%5s", L35+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V36 + String.format("%5s", L36+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V37 + String.format("%5s", L37+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V38 + String.format("%5s", L38+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V39 + String.format("%5s", L39+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V40 + String.format("%5s", L40+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V41 + String.format("%5s", L41+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V42 + String.format("%5s", L42+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V43 + String.format("%5s", L43+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V44 + String.format("%5s", L44+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V45 + String.format("%5s", L45+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V46 + String.format("%5s", L46+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V47 + String.format("%5s", L47+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V48 + String.format("%5s", L48+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V49 + String.format("%5s", L49+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V50 + String.format("%5s", L50+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V51 + String.format("%5s", L51+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V52 + String.format("%5s", L52+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V53 + String.format("%5s", L53+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V54 + String.format("%5s", L54+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V55 + String.format("%5s", L55+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V56 + String.format("%5s", L56+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V57 +  L57+"");
            Archivoprint.write("\r\n");
            Archivoprint.write(V58 + String.format("%5s", L58+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V59 + String.format("%5s", L59+""));
            Archivoprint.write("\r\n");
            if (TieneDosBarras == 1)
            {
               // LB60 = 1945 + 425+(restar) + Integer.parseInt(Valortexto2);//antes 1985 1955
               // LB60_1 = 1900 + 425+(restar) + Integer.parseInt(Valortexto2);//antes 1910

                LB60 = 1945 + 425 + (restar) + Integer.parseInt(Valortexto2);//antes 1985 1955
                LB60_1 = 1900 + 425 + (restar) + Integer.parseInt(Valortexto2);//antes 1910
                //         "B UCCEAN128 1 3 60 100 ";
                B60    = "B UCCEAN128 0 3 60  30 ";
                B60_1  = "B UCCEAN128 0 3 60 400 ";
                //Archivoprint.write(B60 + LB60.ToString().Trim().PadRight(5, ' '));
                //Archivoprint.write(B60_1 + LB60_1.ToString().Trim().PadRight(5, ' '));
            }
            else
            {

                B60 =   "B UCCEAN128 1 3 90 100 ";
                B60_1 = "B UCCEAN128 1 3 30 100 ";
            }


            Archivoprint.write(B60 +  String.format("%5s", LB60+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(B60_1 +  String.format("%5s", LB60_1+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V61 + String.format("%5s", L61+"") );
            Archivoprint.write("\r\n");
            Archivoprint.write(V62 +  String.format("%5s", L62+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V63 + String.format("%5s", L63+"") );
            Archivoprint.write("\r\n");
            Archivoprint.write(V64 +  String.format("%5s", L64+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V65 +  String.format("%5s", L65+""));
            Archivoprint.write("\r\n");
//nuevos campos
            Archivoprint.write(V66 +  String.format("%5s", L66+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V67 +  String.format("%5s", L67+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V68 +  String.format("%5s", L68+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V69 +  String.format("%5s", L69+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V70 +  String.format("%5s", L70+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V71 +  String.format("%5s", L71+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V72 +  String.format("%5s", L72+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V73 +  String.format("%5s", L73+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V73_1 +  String.format("%5s", L73_1+""));
            Archivoprint.write("\r\n");


            Archivoprint.write(V74 +  String.format("%5s", L74+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V75 +  String.format("%5s", L75+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V76 +  String.format("%5s", L76+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V76_1 +  String.format("%5s", L76_1+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V76_2 + String.format("%5s", L76_2+""));
            Archivoprint.write("\r\n");

            //f
            Archivoprint.write(V77);//;//" + V77
            Archivoprint.write("\r\n");
            Archivoprint.write(V78);
            Archivoprint.write("\r\n");
            Archivoprint.write(V79);
            Archivoprint.write("\r\n");

            Archivoprint.flush();
            Archivoprint.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("VariablesGlobales", e.toString());
        }
    }

    public void crearArchivoImpresion420(String Valortexto1, String Valortexto2, int TieneDosBarras) { //   Ax antes: CambiarValoresimpresion

        int restar=0;
        if (null == Valortexto1 || Valortexto1.trim().equals(""))
            Valortexto1 = "-30";
        if (null == Valortexto2 || Valortexto2.trim().equals(""))
            Valortexto2 = "0";

        String ArchivoFormato = directorioactual + "/ValoresFormato.log";

        File file = new File(ArchivoFormato);

        try {
            if (!file.exists()) {
                BufferedWriter archivoLineas = new BufferedWriter(new FileWriter(file));
                archivoLineas.write(Valortexto1);
                archivoLineas.write(Valortexto2);
                archivoLineas.write("1");
                archivoLineas.flush();
                archivoLineas.close();
            }
            //si existe sacar los calores requeridos del archivo guardado
            else {

                FileReader r = new FileReader(file);
                BufferedReader reader = new BufferedReader(r);
                String linea = "";
                int cont = 0;

                while ((linea = reader.readLine()) != null) {
                    cont++;
                    if (cont == 1) {
                        Valortexto1 = linea.trim();
                    } else if (cont == 2) {
                        Valortexto2 = linea.trim();
                    }
                }
                r.close();
            }

            //int valortexto1 = Integer.parseInt(Valortexto1);
            int valortexto2 = Integer.parseInt(Valortexto2);
            String V1 = "! 0 200 200 2680 1";//95
            L1 = 0;
            L2 = 0;
            L3 = 0;
            L4 = 0;
            L5 = 0;
            L6 = 0;
            L7 = 0;
            L8 = 0;
            L8 = 0;
            L9 = 0;
            L10 = 120 +(restar) + Integer.parseInt(Valortexto2);
            L11 = 17  +(restar) + Integer.parseInt(Valortexto2);
            L12 =  90 +(restar)+ Integer.parseInt(Valortexto2);
            L13 = 120 +(restar)+ Integer.parseInt(Valortexto2);
            L14 = 205 +(restar)+ Integer.parseInt(Valortexto2);//antes
            L15 = 265 +(restar)+ Integer.parseInt(Valortexto2);
            L16 = 320 +(restar)+ Integer.parseInt(Valortexto2);
            L17 = 375 +(restar)+ Integer.parseInt(Valortexto2);//antes 390
            L18 = 460 +(restar)+ Integer.parseInt(Valortexto2);
            L19 = 495 +(restar)+ Integer.parseInt(Valortexto2);
            //vacio de las lineas
            L20 = 0;
            L21 = 0;
            L22 = 0;
            L23 = 0;
            L24 = 0;
            L25 = 0;
            L26 = 0;

            //desde la 20 a la 26

            L27 = 715 +(restar)+ Integer.parseInt(Valortexto2);//antes 729
            L28 = 715 +(restar)+ Integer.parseInt(Valortexto2);//antes 729
            L29 = 693 +(restar)+ Integer.parseInt(Valortexto2);//antes 707
            L30 = 605 +(restar)+ Integer.parseInt(Valortexto2);
            L31 = 605 +(restar)+ Integer.parseInt(Valortexto2);
            L32 = 605 +(restar)+ Integer.parseInt(Valortexto2);
            L33 = 605 +(restar)+ Integer.parseInt(Valortexto2);
            L34 = 605 +(restar)+ Integer.parseInt(Valortexto2);
            L35 = 890 +(restar)+ Integer.parseInt(Valortexto2);
            L36 = 913 +(restar)+ Integer.parseInt(Valortexto2);
            L37 = 985 +(restar)+ Integer.parseInt(Valortexto2);//antes 1008
            L38 = 1075 +(restar)+ Integer.parseInt(Valortexto2);

            //Conceptos de Cobro
            L39 = 1165  +(restar)+ Integer.parseInt(Valortexto2);
            L40 = 1185 +(restar)+ Integer.parseInt(Valortexto2);
            L41 = 1205 +(restar)+ Integer.parseInt(Valortexto2);
            L42 = 1225 +(restar)+ Integer.parseInt(Valortexto2);
            L43 = 1245 +(restar)+ Integer.parseInt(Valortexto2);
            L44 = 1265 +(restar)+ Integer.parseInt(Valortexto2);
            L45 = 1285 +(restar)+ Integer.parseInt(Valortexto2);
            L46 = 1305 +(restar)+ Integer.parseInt(Valortexto2);
            L47 = 1325 +(restar)+ Integer.parseInt(Valortexto2);
            //quie es usted
            L48 = 1310 +(restar)+ Integer.parseInt(Valortexto2);//antes 1295

            L49 = 1620  + (restar)+ Integer.parseInt(Valortexto2);//antes 1490
            L50 = 1650 + (restar)+ Integer.parseInt(Valortexto2);//antes 1544

            L51 = 1670 +(restar)+ Integer.parseInt(Valortexto2);//antes 1569
            L52 = 1670 +(restar)+ Integer.parseInt(Valortexto2);//antes 1616

            L53 = 1440 +(restar)+ Integer.parseInt(Valortexto2);//antes 1423
            L54 = 862 +(restar)+ Integer.parseInt(Valortexto2);//antes 885
//mensaje
            L55 = 2085 +(restar)+ Integer.parseInt(Valortexto2);//1685mensaje 1  antes 1692 //1682
            L56 = 2115 +(restar)+ Integer.parseInt(Valortexto2);//1710 mensaje 2 antes 1712 1702
            L57 = 2145 +(restar)+ Integer.parseInt(Valortexto2);//1735 mensaje3  antes 1732 1722

            L58 = 2230  + (restar)+ Integer.parseInt(Valortexto2);//antes 1825
            L59 =  2265 + (restar)+ Integer.parseInt(Valortexto2);//antes 1850
            LB60 = 2340 + (restar)+ Integer.parseInt(Valortexto2);//antes 1950
            LB60_1 = 2410 +(restar)+ Integer.parseInt(Valortexto2);//antes 1920
            L61 = 2440 + (restar)+ Integer.parseInt(Valortexto2);//antes 2050 2030
            L62 = 2470 + (restar)+ Integer.parseInt(Valortexto2);//antes 2068 2058
            L63 = 2495 + (restar)+ Integer.parseInt(Valortexto2);//antes 2093  2083
            L64 = 2520 + (restar)+ Integer.parseInt(Valortexto2);//antes 2118 2108
            L65 = 2568 + (restar)+ Integer.parseInt(Valortexto2);//antes 2135  2125
            //nuevas lineas
            L66 = 645 + (restar)+ Integer.parseInt(Valortexto2);
            L67 = 800 + (restar)+ Integer.parseInt(Valortexto2);
            L68 = 825 + (restar)+ Integer.parseInt(Valortexto2);
//las seis lineas del nuevo formato de aseo
            L69 = 1525   + (restar)+ Integer.parseInt(Valortexto2);//antes 1515
            L70 = 1555   + (restar)+ Integer.parseInt(Valortexto2);//antes 1545
            L71 = 1585   + (restar)+ Integer.parseInt(Valortexto2);//antes 1575
            L72 = 1615   + (restar)+ Integer.parseInt(Valortexto2);//antes 1608
            L73 = 1645   + (restar)+ Integer.parseInt(Valortexto2);//antes 1637
            L73_1 = 1585 + (restar)+ Integer.parseInt(Valortexto2);//antes 1637

            L74 =   1390 + (restar)+ Integer.parseInt(Valortexto2);//1410
            L75 =   1440 + (restar)+ Integer.parseInt(Valortexto2);//antes 1410
            L76 =   1460 + (restar)+ Integer.parseInt(Valortexto2);
            L76_1 = 1670 + (restar)+ Integer.parseInt(Valortexto2);//antes
            L76_2 = 2600 + (restar)+ Integer.parseInt(Valortexto2);//antes 2160



            String ArchivoImprimir = directorioactual +  "/Impresion.log";
            File file2 = new File(ArchivoImprimir);

            if (file2.exists()) {
                file2.delete( );
            }

            BufferedWriter Archivoprint = new BufferedWriter(new FileWriter(file2));

            Archivoprint.write(V1);
            Archivoprint.write("\r\n");
            Archivoprint.write(V2);
            Archivoprint.write("\r\n");
            Archivoprint.write(V3);
            Archivoprint.write("\r\n");
            Archivoprint.write(V4 + Valortexto1);
            Archivoprint.write("\r\n");
            Archivoprint.write(V5);
            Archivoprint.write("\r\n");
            Archivoprint.write(V6);
            Archivoprint.write("\r\n");
            Archivoprint.write(V7);
            Archivoprint.write("\r\n");
            Archivoprint.write(";//"+V8); //variables finales para la impresion
            Archivoprint.write("\r\n");
            Archivoprint.write(V9);
            Archivoprint.write("\r\n");
            Archivoprint.write(V10 + String.format("%5s", L10+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V11 + String.format("%5s", L11+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V12 + String.format("%5s", L12+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V13 + String.format("%5s", L13+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V14 + String.format("%5s", L14+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V15 + String.format("%5s", L15+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V16 + String.format("%5s", L16+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V17 + String.format("%5s", L17+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V18 + String.format("%5s", L18+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V19 + String.format("%5s", L19+""));
            Archivoprint.write("\r\n");
            //inicio lineas de barras
            Archivoprint.write("//" + V20);
            Archivoprint.write("\r\n");
            Archivoprint.write("//" + V21);
            Archivoprint.write("\r\n");
            Archivoprint.write("//" + V22);
            Archivoprint.write("\r\n");
            Archivoprint.write("//" + V23);
            Archivoprint.write("\r\n");
            Archivoprint.write("//" + V24);
            Archivoprint.write("\r\n");
            Archivoprint.write(";//" + V25);
            Archivoprint.write("\r\n");
            Archivoprint.write(";//" + V26);
            Archivoprint.write("\r\n");
            //fin lineas de barras

            Archivoprint.write(V27 + String.format("%5s", L27+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V28 + String.format("%5s", L28+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V29 + String.format("%5s", L29+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V30 + String.format("%5s", L30+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V31 + String.format("%5s", L31+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V32 + String.format("%5s", L32+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V33 + String.format("%5s", L33+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V34 + String.format("%5s", L34+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V35 + String.format("%5s", L35+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V36 + String.format("%5s", L36+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V37 + String.format("%5s", L37+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V38 + String.format("%5s", L38+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V39 + String.format("%5s", L39+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V40 + String.format("%5s", L40+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V41 + String.format("%5s", L41+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V42 + String.format("%5s", L42+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V43 + String.format("%5s", L43+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V44 + String.format("%5s", L44+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V45 + String.format("%5s", L45+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V46 + String.format("%5s", L46+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V47 + String.format("%5s", L47+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V48 + String.format("%5s", L48+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V49 + String.format("%5s", L49+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V50 + String.format("%5s", L50+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V51 + String.format("%5s", L51+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V52 + String.format("%5s", L52+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V53 + String.format("%5s", L53+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V54 + String.format("%5s", L54+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V55 + String.format("%5s", L55+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V56 + String.format("%5s", L56+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V57 +  L57+"");
            Archivoprint.write("\r\n");
            Archivoprint.write(V58 + String.format("%5s", L58+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V59 + String.format("%5s", L59+""));
            Archivoprint.write("\r\n");
            if (TieneDosBarras == 1)
            {
                LB60 = 1945 + 425+(restar) + Integer.parseInt(Valortexto2);//antes 1985 1955
                LB60_1 = 1900 + 425+(restar) + Integer.parseInt(Valortexto2);//antes 1910
                //         "B UCCEAN128 1 3 60 100 ";
                B60    = "B UCCEAN128 0 3 60  30 ";
                B60_1  = "B UCCEAN128 0 3 60 400 ";
                //Archivoprint.write(B60 + LB60.ToString().Trim().PadRight(5, ' '));
                //Archivoprint.write(B60_1 + LB60_1.ToString().Trim().PadRight(5, ' '));
            }
            else
            {

                B60 =   "B UCCEAN128 1 3 90 100 ";
                B60_1 = "B UCCEAN128 1 3 30 100 ";
            }


            Archivoprint.write(B60 +  String.format("%5s", LB60+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(B60_1 +  String.format("%5s", LB60_1+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V61 + String.format("%5s", L61+"") );
            Archivoprint.write("\r\n");
            Archivoprint.write(V62 +  String.format("%5s", L62+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V63 + String.format("%5s", L63+"") );
            Archivoprint.write("\r\n");
            Archivoprint.write(V64 +  String.format("%5s", L64+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V65 +  String.format("%5s", L65+""));
            Archivoprint.write("\r\n");
//nuevos campos
            Archivoprint.write(V66 +  String.format("%5s", L66+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V67 +  String.format("%5s", L67+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V68 +  String.format("%5s", L68+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V69 +  String.format("%5s", L69+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V70 +  String.format("%5s", L70+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V71 +  String.format("%5s", L71+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V72 +  String.format("%5s", L72+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V73 +  String.format("%5s", L73+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V73_1 +  String.format("%5s", L73_1+""));
            Archivoprint.write("\r\n");


            Archivoprint.write(V74 +  String.format("%5s", L74+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V75 +  String.format("%5s", L75+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V76 +  String.format("%5s", L76+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V76_1 +  String.format("%5s", L76_1+""));
            Archivoprint.write("\r\n");
            Archivoprint.write(V76_2 + String.format("%5s", L76_2+""));
            Archivoprint.write("\r\n");

            //f
            Archivoprint.write(V77);//;//" + V77
            Archivoprint.write("\r\n");
            Archivoprint.write(V78);
            Archivoprint.write("\r\n");
            Archivoprint.write(V79);
            Archivoprint.write("\r\n");

            Archivoprint.flush();
            Archivoprint.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("VariablesGlobales", e.toString());
        }
    }

    // nuevas funciones
    public double evaluarDistanciaAlPredio(double Latitud1, double Longitud1, double Latitud2, double Longitud2) {

        double xx = Latitud2;
        double yy = Longitud2;

        double degtorad = 0.01745329;
        double radtodeg = 57.29577951;
        double dlong;
        double dvalue;
        double dd;
        double miles;
        double km;
        dlong = Longitud1 - Longitud2;
        dvalue = (Math.sin(Latitud1 * degtorad) * Math.sin(Latitud2 * degtorad)) + (Math.cos(Latitud1 * degtorad) * Math.cos(Latitud2 * degtorad) * Math.cos(dlong * degtorad));
        dd = Math.acos(dvalue) * radtodeg;
        miles = dd * 69.16;
        km = dd * 111.302;
        return km * 1000;
    }
    // DESPUÉS:
    /*public double evaluarDistanciaAlPredio(double Latitud1, double Longitud1,
                                           double Latitud2, double Longitud2) {
        if (Latitud1 == 0 && Longitud1 == 0) return 9999;
        if (Latitud2 == 0 && Longitud2 == 0) return 9999;

        float[] results = new float[1];
        android.location.Location.distanceBetween(
                Latitud1, Longitud1,
                Latitud2, Longitud2,
                results
        );
        return results[0]; // metros, precisión Vincenty
    }*/

    public static int getEvaluarCritica40() {
        return EvaluarCritica40;
    }

    public static void setEvaluarCritica40(int evaluarCritica40) {
        EvaluarCritica40 = evaluarCritica40;
    }

    public static int getDiasvence() {
        return diasvence;
    }

    public static void setDiasvence(int diasvence) {
        VariablesGlobales.diasvence = diasvence;
    }

    public static int getDiascorte() {
        return diascorte;
    }

    public static void setDiascorte(int diascorte) {
        VariablesGlobales.diascorte = diascorte;
    }

    public static String remplazarCaracteresEspeciales(String cadEntrada) {
        String original = "áàäéèëíìïóòöúùuñÁÀÄÉÈËÍÌÏÓÒÖÚÙÜÑçÇ�";
        String ascii = "aaaeeeiiiooouuunAAAEEEIIIOOOUUUNcCN";
        String cadSalida = cadEntrada;
        for (int i=0; i<original.length(); i++) {
            // Reemplazamos los caracteres especiales.
            cadSalida = cadSalida.replace(original.charAt(i), ascii.charAt(i));
        }//for i
        return cadSalida;
    }

    public void obtenerRutaMemoriaExterna(String hard){
        File fileList[] = new File(hard).listFiles();
        for (File file : fileList)
        {
            if(!file.getAbsolutePath().equalsIgnoreCase(Environment.getExternalStorageDirectory().getAbsolutePath()) && file.isDirectory() && file.canRead()){
                removableStoragePath = file.getAbsolutePath();
            }
            Log.e("error3",file.getAbsolutePath()+"-"+" ruta sd2 ");
        }
        Log.e("error3",removableStoragePath+"-"+" ruta sd2 ");

    }


}