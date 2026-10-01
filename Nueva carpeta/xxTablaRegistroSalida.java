package com.gselectroCaqueta.tablas;

import android.util.Log;

import com.gselectroCaqueta.accesoyseguridad.VariablesGlobales;
import com.gsutil.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.RandomAccessFile;
/// <summary>
/// Descripcion breve del archivo a generar...tablaRegistroSalida
/// </summary>

public class TablaRegistroSalida { //Ax esta es S_REGIST, ya
    private static final int LONGITUD_REGISTRO = 186;//186   191
    public LineNumberReader rdr;
    String sep = ";";
    String tablaRegistroSalida_cliente;
    String tablaRegistroSalida_contador;
    String tablaRegistroSalida_anio;
    String tablaRegistroSalida_mes;
    String tablaRegistroSalida_cuenta;
    String tablaRegistroSalida_marca;
    String tablaRegistroSalida_nrocontador;
    String tablaRegistroSalida_consecutivo;
    String tablaRegistroSalida_lecturatomada;
    String tablaRegistroSalida_consumotomado;
    String tablaRegistroSalida_consumocalculado;
    String tablaRegistroSalida_causadenolectura;
    String tablaRegistroSalida_comentario1;
    String tablaRegistroSalida_comentario2;
    String tablaRegistroSalida_informe;
    String tablaRegistroSalida_intentos;
    String tablaRegistroSalida_modificaciones;
    String tablaRegistroSalida_lecturamodificada1;
    String tablaRegistroSalida_lecturamodificada2;
    String tablaRegistroSalida_fechalectura;
    String tablaRegistroSalida_horalectura;

    String tablaRegistroSalida_leido;
    String tablaRegistroSalida_fin;
    BufferedReader fin;
    byte[] byteArray;
    String archivo_TablaRegistroSalida;
    int ultimo_TablaRegistroSalida;
    public int encontro_TablaRegistroSalida;
    String buscar_TablaRegistroSalida;
    String texto;
    // File ruta_sd = Environment.getExternalStorageDirectory();
    // File ruta_sd = new
    // File('C:\\Users\\SGlobal\\Documents\\PruebasJava\\CensoSalida.txt');
    RandomAccessFile rFile;
    private int _fileSize;
    private int total_TablaRegistroSalida;
    Utils utils = new Utils();

    VariablesGlobales variables = new VariablesGlobales();

    public static int getLongitudRegistro() {
        return LONGITUD_REGISTRO;
    }

    public String gettablaRegistroSalida_CLIENTE() {
        return tablaRegistroSalida_cliente;
    }

    public void settablaRegistroSalida_cliente(String tablaRegistroSalida_cliente) {
        this.tablaRegistroSalida_cliente = tablaRegistroSalida_cliente;
    }

    public String gettablaRegistroSalida_CONTADOR() {
        return tablaRegistroSalida_contador;
    }

    public void settablaRegistroSalida_contador(String tablaRegistroSalida_contador) {
        this.tablaRegistroSalida_contador = tablaRegistroSalida_contador;
    }

    public String gettablaRegistroSalida_ANIO() {
        return tablaRegistroSalida_anio;
    }

    public void settablaRegistroSalida_anio(String tablaRegistroSalida_anio) {
        this.tablaRegistroSalida_anio = tablaRegistroSalida_anio;
    }

    public String gettablaRegistroSalida_MES() {
        return tablaRegistroSalida_mes;
    }

    public void settablaRegistroSalida_mes(String tablaRegistroSalida_mes) {
        this.tablaRegistroSalida_mes = tablaRegistroSalida_mes;
    }

    public String gettablaRegistroSalida_CUENTA() {
        return tablaRegistroSalida_cuenta;
    }

    public void settablaRegistroSalida_cuenta(String tablaRegistroSalida_cuenta) {
        this.tablaRegistroSalida_cuenta = tablaRegistroSalida_cuenta;
    }

    public String gettablaRegistroSalida_MARCA() {
        return tablaRegistroSalida_marca;
    }

    public void settablaRegistroSalida_marca(String tablaRegistroSalida_marca) {
        this.tablaRegistroSalida_marca = tablaRegistroSalida_marca;
    }

    public String gettablaRegistroSalida_NROCONTADOR() {
        return tablaRegistroSalida_nrocontador;
    }

    public void settablaRegistroSalida_nrocontador(String tablaRegistroSalida_nrocontador) {
        this.tablaRegistroSalida_nrocontador = tablaRegistroSalida_nrocontador;
    }

    public String gettablaRegistroSalida_CONSECUTIVO() {
        return tablaRegistroSalida_consecutivo;
    }

    public void settablaRegistroSalida_consecutivo(String tablaRegistroSalida_consecutivo) {
        this.tablaRegistroSalida_consecutivo = tablaRegistroSalida_consecutivo;
    }

    public String gettablaRegistroSalida_LECTURATOMADA() {
        return tablaRegistroSalida_lecturatomada;
    }

    public void settablaRegistroSalida_lecturatomada(String tablaRegistroSalida_lecturatomada) {
        this.tablaRegistroSalida_lecturatomada = tablaRegistroSalida_lecturatomada;
    }

    public String gettablaRegistroSalida_CONSUMOTOMADO() {
        return tablaRegistroSalida_consumotomado;
    }

    public void settablaRegistroSalida_consumotomado(String tablaRegistroSalida_consumotomado) {
        this.tablaRegistroSalida_consumotomado = tablaRegistroSalida_consumotomado;
    }

    public String gettablaRegistroSalida_CONSUMOCALCULADO() {
        return tablaRegistroSalida_consumocalculado;
    }

    public void settablaRegistroSalida_consumocalculado(String tablaRegistroSalida_consumocalculado) {
        this.tablaRegistroSalida_consumocalculado = tablaRegistroSalida_consumocalculado;
    }

    public String gettablaRegistroSalida_CAUSADENOLECTURA() {
        return tablaRegistroSalida_causadenolectura;
    }

    public void settablaRegistroSalida_causadenolectura(String tablaRegistroSalida_causadenolectura) {
        this.tablaRegistroSalida_causadenolectura = tablaRegistroSalida_causadenolectura;
    }

    public String gettablaRegistroSalida_COMENTARIO1() {
        return tablaRegistroSalida_comentario1;
    }

    public void settablaRegistroSalida_comentario1(String tablaRegistroSalida_comentario1) {
        this.tablaRegistroSalida_comentario1 = tablaRegistroSalida_comentario1;
    }

    public String gettablaRegistroSalida_COMENTARIO2() {
        return tablaRegistroSalida_comentario2;
    }

    public void settablaRegistroSalida_comentario2(String tablaRegistroSalida_comentario2) {
        this.tablaRegistroSalida_comentario2 = tablaRegistroSalida_comentario2;
    }

    public String gettablaRegistroSalida_INFORME() {
        return tablaRegistroSalida_informe;
    }

    public void settablaRegistroSalida_informe(String tablaRegistroSalida_informe) {
        this.tablaRegistroSalida_informe = tablaRegistroSalida_informe;
    }

    public String gettablaRegistroSalida_INTENTOS() {
        return tablaRegistroSalida_intentos;
    }

    public void settablaRegistroSalida_intentos(String tablaRegistroSalida_intentos) {
        this.tablaRegistroSalida_intentos = tablaRegistroSalida_intentos;
    }

    public String gettablaRegistroSalida_MODIFICACIONES() {
        return tablaRegistroSalida_modificaciones;
    }

    public void settablaRegistroSalida_modificaciones(String tablaRegistroSalida_modificaciones) {
        this.tablaRegistroSalida_modificaciones = tablaRegistroSalida_modificaciones;
    }

    public String gettablaRegistroSalida_LECTURAMODIFICADA1() {
        return tablaRegistroSalida_lecturamodificada1;
    }

    public void settablaRegistroSalida_lecturamodificada1(String tablaRegistroSalida_lecturamodificada1) {
        this.tablaRegistroSalida_lecturamodificada1 = tablaRegistroSalida_lecturamodificada1;
    }

    public String gettablaRegistroSalida_LECTURAMODIFICADA2() {
        return tablaRegistroSalida_lecturamodificada2;
    }

    public void settablaRegistroSalida_lecturamodificada2(String tablaRegistroSalida_lecturamodificada2) {
        this.tablaRegistroSalida_lecturamodificada2 = tablaRegistroSalida_lecturamodificada2;
    }

    public String gettablaRegistroSalida_FECHALECTURA() {
        return tablaRegistroSalida_fechalectura;
    }

    public void settablaRegistroSalida_fechalectura(String tablaRegistroSalida_fechalectura) {
        this.tablaRegistroSalida_fechalectura = tablaRegistroSalida_fechalectura;
    }

    public String gettablaRegistroSalida_HORALECTURA() {
        return tablaRegistroSalida_horalectura;
    }

    public void settablaRegistroSalida_horalectura(String tablaRegistroSalida_horalectura) {
        this.tablaRegistroSalida_horalectura = tablaRegistroSalida_horalectura;
    }

    public String gettablaRegistroSalida_LEIDO() {
        return tablaRegistroSalida_leido;
    }

    public void settablaRegistroSalida_leido(String tablaRegistroSalida_leido) {
        this.tablaRegistroSalida_leido = tablaRegistroSalida_leido;
    }

    public String gettablaRegistroSalida_FIN() {
        return tablaRegistroSalida_fin;
    }

    public void settablaRegistroSalida_fin(String tablaRegistroSalida_fin) {
        this.tablaRegistroSalida_fin = tablaRegistroSalida_fin;
    }

    public String getArchivo_TablaRegistroSalida() {
        return archivo_TablaRegistroSalida;
    }

    public void setArchivo_TablaRegistroSalida(String archivo_TablaRegistroSalida) {
        this.archivo_TablaRegistroSalida = archivo_TablaRegistroSalida;
    }

    public int getTotal_TablaRegistroSalida() {
        return total_TablaRegistroSalida;
    }

    public void setTotal_TablaRegistroSalida(int total_TablaRegistroSalida) {
        this.total_TablaRegistroSalida = total_TablaRegistroSalida;
    }



    public Boolean abrir_TablaRegistroSalida(String nombreArchivo) {
        if (nombreArchivo.length() == 0) {
            return false;
        }
        try {
            rFile = new RandomAccessFile(nombreArchivo, "rw");

        } catch (FileNotFoundException e) {
            Log.e("error","al abrir "+e.getMessage());
            e.printStackTrace();
        }
        archivo_TablaRegistroSalida = nombreArchivo;
        return abrir(archivo_TablaRegistroSalida);
    }

    private Boolean abrir(String nombre_archivo) {
        try {
            _fileSize = (int) rFile.length();//Ax: fileSize

            if (_fileSize > 0) {
                byteArray = new byte[LONGITUD_REGISTRO];//Ax:  fileSize
                rFile.readFully(byteArray, 0, LONGITUD_REGISTRO);//Ax: fileSize
                // texto = new String(byteArray);
                setTotal_TablaRegistroSalida(_fileSize / LONGITUD_REGISTRO);
            } else {
                byteArray = new byte[_fileSize];
                rFile.readFully(byteArray, 0, _fileSize);
                setTotal_TablaRegistroSalida(0);
            }

            return true;
        } catch (Exception ex) {
            Log.e("error","al abrir1 "+ex.getMessage());
            // Log.e('Ficheros', 'Error al leer fichero desde tarjeta SD');
            ex.printStackTrace();
            return false;
        }
    }

    public void Cerrar_TablaRegistroSalida() {
        // cerrar el archivo abierto
        try {
            rFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean escribir_TablaRegistroSalida(int posicion) {

        posicion_TablaRegistroSalida(posicion, LONGITUD_REGISTRO);
        rellenar_TablaRegistroSalida();


        String texto = tablaRegistroSalida_cliente + sep + tablaRegistroSalida_contador + sep + tablaRegistroSalida_anio + sep + tablaRegistroSalida_mes + sep + tablaRegistroSalida_cuenta + sep
                + tablaRegistroSalida_marca + sep + tablaRegistroSalida_nrocontador + sep + tablaRegistroSalida_consecutivo + sep + tablaRegistroSalida_lecturatomada + sep
                + tablaRegistroSalida_consumotomado + sep + tablaRegistroSalida_consumocalculado + sep + tablaRegistroSalida_causadenolectura + sep + tablaRegistroSalida_comentario1 + sep
                + tablaRegistroSalida_comentario2 + sep + tablaRegistroSalida_informe + sep + tablaRegistroSalida_intentos + sep + tablaRegistroSalida_modificaciones + sep
                + tablaRegistroSalida_lecturamodificada1 + sep + tablaRegistroSalida_lecturamodificada2 + sep + tablaRegistroSalida_fechalectura + sep + tablaRegistroSalida_horalectura + sep
                + tablaRegistroSalida_leido + sep   + tablaRegistroSalida_fin;
        tablaRegistroSalida_lecturatomada = tablaRegistroSalida_lecturatomada;
      //  Log.e("error",texto +" escribe  ");

      //  Log.e("error",texto.length() +" escribe long "+LONGITUD_REGISTRO);
        try {
            if (null != rFile && (texto.length() == LONGITUD_REGISTRO)) {
                rFile.writeBytes(texto);
                System.out.println("Los Datos fueron grabados correctamente");
                return true;
            } else {
                File desc = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/LOGEVENTOS.LOG"); //Ax: EJ: O108010.009
                utils.EscribirLinea(desc,"CADENA DAÑADA REGISTRADORES:"+"\r\n" + texto);
                System.out.println("Se presento problema al escribir en el archivo; longitud Errada: tamano texto: " + texto.length() + texto);
                return false;
            }
        } catch (IOException ioe) {
            System.out.println("Se presento problema al escribir en el archivo Longitud Errada: " + LONGITUD_REGISTRO);
            return false;
        }
    }

    public void rellenar_TablaRegistroSalida() {
        try {
         //   Log.e("error","-"+tablaRegistroSalida_lecturatomada+" rellenar "+String.format("%10s", tablaRegistroSalida_lecturatomada).replace(" ","0")+"-"+String.format("%10s", tablaRegistroSalida_lecturatomada));

            tablaRegistroSalida_consecutivo = String.format("%-3s", tablaRegistroSalida_consecutivo);
            tablaRegistroSalida_lecturatomada = String.format("%10s", tablaRegistroSalida_lecturatomada);//.replace(" ","0");
            tablaRegistroSalida_consumotomado = String.format("%10s", tablaRegistroSalida_consumotomado).replace(" ","0");
            tablaRegistroSalida_consumocalculado = String.format("%-10s", tablaRegistroSalida_consumocalculado);
            tablaRegistroSalida_causadenolectura = String.format("%3s", tablaRegistroSalida_causadenolectura);
            tablaRegistroSalida_comentario1 = String.format("%-3s", tablaRegistroSalida_comentario1);
            tablaRegistroSalida_comentario2 = String.format("%-3s", tablaRegistroSalida_comentario2);
            tablaRegistroSalida_informe = String.format("%-30s", tablaRegistroSalida_informe);
            tablaRegistroSalida_intentos = String.format("%-1s", tablaRegistroSalida_intentos);
            tablaRegistroSalida_modificaciones = String.format("%-1s", tablaRegistroSalida_modificaciones);
            tablaRegistroSalida_lecturamodificada1 = String.format("%-10s", tablaRegistroSalida_lecturamodificada1);
            tablaRegistroSalida_lecturamodificada2 = String.format("%-10s", tablaRegistroSalida_lecturamodificada2);
            tablaRegistroSalida_fechalectura = String.format("%-8s", tablaRegistroSalida_fechalectura);
            tablaRegistroSalida_horalectura = String.format("%-6s", tablaRegistroSalida_horalectura);
            tablaRegistroSalida_leido = String.format("%-1s", tablaRegistroSalida_leido);

            tablaRegistroSalida_fin = String.format("%-2s", tablaRegistroSalida_fin);


        } catch (Exception e) {
            System.out.println("Se presento problema al escribir en el archivo tablaRegistroSalida.dat..");
            e.printStackTrace();
        }

        return;
    }

    public void posicion_TablaRegistroSalida(int registro, int tamano) {
        try {
            rFile.seek((registro - 1) * tamano);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Escribir los registros atual en el archivo en posicion
    public void lectura_TablaRegistroSalida(int registro) {
        //String[] campos;
        encontro_TablaRegistroSalida = 0;
        posicion_TablaRegistroSalida(registro, LONGITUD_REGISTRO);
        try {
            //Ax: int fileSize = (int) rFile.length();
            byteArray = new byte[LONGITUD_REGISTRO];//Ax: _fileSize
            rFile.read(byteArray);//Ax: rFile.readFully(byteArray, 0, LONGITUD_REGISTRO);
            texto = new String(byteArray);
            settablaRegistroSalida_cliente(texto.substring(0, 5));
            settablaRegistroSalida_contador(texto.substring(6, 11));
            settablaRegistroSalida_anio(texto.substring(12, 16));
            settablaRegistroSalida_mes(texto.substring(17, 19));
            settablaRegistroSalida_cuenta(texto.substring(20, 29));//4
            settablaRegistroSalida_marca(texto.substring(30, 42));
            settablaRegistroSalida_nrocontador(texto.substring(43, 59));//6
            settablaRegistroSalida_consecutivo(texto.substring(60,63));//6///7
            settablaRegistroSalida_lecturatomada(texto.substring(64,74));//77//8
            settablaRegistroSalida_consumotomado(texto.substring(75,85));//88//9
            settablaRegistroSalida_consumocalculado(texto.substring(86,96));//99//10
            settablaRegistroSalida_causadenolectura(texto.substring(97,100));//103
            settablaRegistroSalida_comentario1(texto.substring(101,104));//107
            settablaRegistroSalida_comentario2(texto.substring(105,108));//111
            settablaRegistroSalida_informe(texto.substring(109,139));//142
            settablaRegistroSalida_intentos(texto.substring(140,141));//144
            settablaRegistroSalida_modificaciones(texto.substring(142,143));//146
            settablaRegistroSalida_lecturamodificada1(texto.substring(144,154));//157
            settablaRegistroSalida_lecturamodificada2(texto.substring(155,165));//168
            settablaRegistroSalida_fechalectura(texto.substring(166,174));//177
            settablaRegistroSalida_horalectura(texto.substring(175,181));//184
            settablaRegistroSalida_leido(texto.substring(182,183));//186
            settablaRegistroSalida_fin(texto.substring(184,186));

            settablaRegistroSalida_lecturatomada(gettablaRegistroSalida_LECTURATOMADA());

            ultimo_TablaRegistroSalida = registro;
            // fin estructura
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //-------------------------------------------Ax : Bloque creado para Acelerar resumen....
    public void inicializaBloque() {
        try {
            File filepaths = new File(getArchivo_TablaRegistroSalida());
            rdr = new LineNumberReader(new FileReader(filepaths));
        } catch (Exception d) {
            d.printStackTrace();
        }
    }

    public void lectura_TablaRegistroSalidaII(int x) {
        try {
            encontro_TablaRegistroSalida = 0;

            // int tamano = (int) filepath.length();  // String  sb1 = "";

            for (String line = null; (line = rdr.readLine()) != null; ) {
                if (rdr.getLineNumber() >= x) {
                    //sb1=line;
                    settablaRegistroSalida_cliente(line.substring(0, 5));//Ax: ya cambio vict
                    settablaRegistroSalida_contador(line.substring(6,11));
                    settablaRegistroSalida_cuenta(line.substring(20, 29));
                    settablaRegistroSalida_nrocontador(line.substring(43,59));
                    settablaRegistroSalida_lecturatomada(line.substring(64,74));
                    settablaRegistroSalida_causadenolectura(line.substring(97,100));
                    settablaRegistroSalida_comentario1(line.substring(101,104));
                    settablaRegistroSalida_informe(line.substring(109,139));
                    settablaRegistroSalida_fechalectura(line.substring(166,174));
                    settablaRegistroSalida_horalectura(line.substring(175,181));
                    settablaRegistroSalida_leido(line.substring(182,183));
                    ultimo_TablaRegistroSalida = x;
                    return;
                }
            }
        } catch (Exception d) {
            Log.e("error","leyendo archivo "+d.getMessage());
            d.printStackTrace();
        }
    }

    public void terminaBloque() {
        try {
            rdr.close();
        } catch (Exception d) {
            d.printStackTrace();
        }
    }
    //---------------------------------------------------------------------------------------

    public void buscarSecuencial_TablaRegistroSalida(String codigo) { //Ax : creado para acelerar resumen....probando
        encontro_TablaRegistroSalida = 0;
        for (int i = 0; i < (int) (getTotal_TablaRegistroSalida()); i++) {
            lectura_TablaRegistroSalida(i + 1);
            if (Integer.parseInt(codigo.trim()) == Integer.parseInt(tablaRegistroSalida_consecutivo.trim())) {
                encontro_TablaRegistroSalida = i + 1;
                posicion_TablaRegistroSalida(i + 1, LONGITUD_REGISTRO);
                i = getTotal_TablaRegistroSalida() + 10;
            } else {
                encontro_TablaRegistroSalida = 0;
            }
        }
        return;
    }

    public void BuscarSecuencialSregistrosConConcecutivo(String codigo, String consecutivo) {
        encontro_TablaRegistroSalida = 0;
        long cod = Integer.parseInt(codigo.trim());
        long con = Integer.parseInt(consecutivo.trim());
        for (int i = 1; i <= getTotal_TablaRegistroSalida(); i++) {
            lectura_TablaRegistroSalida(i);
         //   Log.e("errora",con+"-"+Long.parseLong(String.format("%1$3s", tablaRegistroSalida_consecutivo.trim()).replace(" ", "0"))+"-lectura concecutivo -"+cod+"-"+tablaRegistroSalida_cuenta);


            if (cod == Long.parseLong(tablaRegistroSalida_cuenta.trim()) && Long.parseLong(String.format("%1$3s", tablaRegistroSalida_consecutivo.trim()).replace(" ", "0")) == con) {
              //  Log.e("errort",Long.parseLong(String.format("%1$3s", tablaRegistroSalida_consecutivo.trim()).replace(" ", "0"))+"-"+con+"buscar "+tablaRegistroSalida_cuenta);
                encontro_TablaRegistroSalida = i;
                posicion_TablaRegistroSalida(i, LONGITUD_REGISTRO);
                break;
            } else {
                encontro_TablaRegistroSalida = 0;
            }
        }
        return;
    }

    public void buscarSecuencial_TablaRegistroSalida_cuenta(String codigo1) {
        encontro_TablaRegistroSalida = 0;
        inicializaBloque();
        int codigo = Integer.parseInt(codigo1);

        for (int i = 0; i < total_TablaRegistroSalida; i++) {
            lectura_TablaRegistroSalidaII(i + 1);

            if (codigo == Integer.parseInt(tablaRegistroSalida_cuenta.trim())) {
                encontro_TablaRegistroSalida=(i + 1);
                posicion_TablaRegistroSalida(i + 1, LONGITUD_REGISTRO);
                i = getTotal_TablaRegistroSalida() + 10;
                return;
            }
        }
        encontro_TablaRegistroSalida=(0);
        terminaBloque();
    }

}
