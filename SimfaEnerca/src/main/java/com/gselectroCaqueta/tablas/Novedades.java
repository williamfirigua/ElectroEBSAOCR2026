package com.gselectroCaqueta.tablas;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
/// <summary>
/// Descripcion breve del archivo a generar...novedades
/// </summary>

public class Novedades {
    String sep = ";";
    String novedades_numeroacta;
    String novedades_ciclo;
    String novedades_codobservacion;
    String novedades_observacion;
    String novedades_fecha;
    String novedades_cuenta;
    String novedades_nombreusuario;
    String novedades_cedula;
    String novedades_direccion;
    String novedades_barrio;
    String novedades_telefono;
    String novedades_rutaanterior;
    String novedades_rutaactual;
    String novedades_rutapropuesta;
    String novedades_rutaposterior;
    String novedades_marca;
    String novedades_contador;
    String novedades_conexion;
    String novedades_anno;
    String novedades_k;
    String novedades_w;
    String novedades_corriente;
    String novedades_tipomedidor;
    String novedades_lectura;
    String novedades_sellotapa;
    String novedades_sellobornera;
    String novedades_sellogabinete;
    String novedades_claseservicio;
    String novedades_comentario;
    String novedades_lector;
    String novedades_mes;
    String novedades_annoperiodo;
    String novedades_tiponovedad;
    String novedades_causalnovedad;
    String novedades_descausalnovedad;
    String novedades_CRNL;

    public String getnovedades_NUMEROACTA() {
        return novedades_numeroacta;
    }

    public void setnovedades_numeroacta(String novedades_numeroacta) {
        this.novedades_numeroacta = novedades_numeroacta;
    }

    public String getnovedades_CICLO() {
        return novedades_ciclo;
    }

    public void setnovedades_ciclo(String novedades_ciclo) {
        this.novedades_ciclo = novedades_ciclo;
    }

    public String getnovedades_CODOBSERVACION() {
        return novedades_codobservacion;
    }

    public void setnovedades_codobservacion(String novedades_codobservacion) {
        this.novedades_codobservacion = novedades_codobservacion;
    }

    public String getnovedades_OBSERVACION() {
        return novedades_observacion;
    }

    public void setnovedades_observacion(String novedades_observacion) {
        this.novedades_observacion = novedades_observacion;
    }

    public String getnovedades_FECHA() {
        return novedades_fecha;
    }

    public void setnovedades_fecha(String novedades_fecha) {
        this.novedades_fecha = novedades_fecha;
    }

    public String getnovedades_CUENTA() {
        return novedades_cuenta;
    }

    public void setnovedades_cuenta(String novedades_cuenta) {
        this.novedades_cuenta = novedades_cuenta;
    }

    public String getnovedades_NOMBREUSUARIO() {
        return novedades_nombreusuario;
    }

    public void setnovedades_nombreusuario(String novedades_nombreusuario) {
        this.novedades_nombreusuario = novedades_nombreusuario;
    }

    public String getnovedades_CEDULA() {
        return novedades_cedula;
    }

    public void setnovedades_cedula(String novedades_cedula) {
        this.novedades_cedula = novedades_cedula;
    }

    public String getnovedades_DIRECCION() {
        return novedades_direccion;
    }

    public void setnovedades_direccion(String novedades_direccion) {
        this.novedades_direccion = novedades_direccion;
    }

    public String getnovedades_BARRIO() {
        return novedades_barrio;
    }

    public void setnovedades_barrio(String novedades_barrio) {
        this.novedades_barrio = novedades_barrio;
    }

    public String getnovedades_TELEFONO() {
        return novedades_telefono;
    }

    public void setnovedades_telefono(String novedades_telefono) {
        this.novedades_telefono = novedades_telefono;
    }

    public String getnovedades_RUTAANTERIOR() {
        return novedades_rutaanterior;
    }

    public void setnovedades_rutaanterior(String novedades_rutaanterior) {
        this.novedades_rutaanterior = novedades_rutaanterior;
    }

    public String getnovedades_RUTAACTUAL() {
        return novedades_rutaactual;
    }

    public void setnovedades_rutaactual(String novedades_rutaactual) {
        this.novedades_rutaactual = novedades_rutaactual;
    }

    public String getnovedades_RUTAPROPUESTA() {
        return novedades_rutapropuesta;
    }

    public void setnovedades_rutapropuesta(String novedades_rutapropuesta) {
        this.novedades_rutapropuesta = novedades_rutapropuesta;
    }

    public String getnovedades_RUTAPOSTERIOR() {
        return novedades_rutaposterior;
    }

    public void setnovedades_rutaposterior(String novedades_rutaposterior) {
        this.novedades_rutaposterior = novedades_rutaposterior;
    }

    public String getnovedades_MARCA() {
        return novedades_marca;
    }

    public void setnovedades_marca(String novedades_marca) {
        this.novedades_marca = novedades_marca;
    }

    public String getnovedades_CONTADOR() {
        return novedades_contador;
    }

    public void setnovedades_contador(String novedades_contador) {
        this.novedades_contador = novedades_contador;
    }

    public String getnovedades_CONEXION() {
        return novedades_conexion;
    }

    public void setnovedades_conexion(String novedades_conexion) {
        this.novedades_conexion = novedades_conexion;
    }

    public String getnovedades_ANNO() {
        return novedades_anno;
    }

    public void setnovedades_anno(String novedades_anno) {
        this.novedades_anno = novedades_anno;
    }

    public String getnovedades_K() {
        return novedades_k;
    }

    public void setnovedades_k(String novedades_k) {
        this.novedades_k = novedades_k;
    }

    public String getnovedades_W() {
        return novedades_w;
    }

    public void setnovedades_w(String novedades_w) {
        this.novedades_w = novedades_w;
    }

    public String getnovedades_CORRIENTE() {
        return novedades_corriente;
    }

    public void setnovedades_corriente(String novedades_corriente) {
        this.novedades_corriente = novedades_corriente;
    }

    public String getnovedades_TIPOMEDIDOR() {
        return novedades_tipomedidor;
    }

    public void setnovedades_tipomedidor(String novedades_tipomedidor) {
        this.novedades_tipomedidor = novedades_tipomedidor;
    }

    public String getnovedades_LECTURA() {
        return novedades_lectura;
    }

    public void setnovedades_lectura(String novedades_lectura) {
        this.novedades_lectura = novedades_lectura;
    }

    public String getnovedades_SELLOTAPA() {
        return novedades_sellotapa;
    }

    public void setnovedades_sellotapa(String novedades_sellotapa) {
        this.novedades_sellotapa = novedades_sellotapa;
    }

    public String getnovedades_SELLOBORNERA() {
        return novedades_sellobornera;
    }

    public void setnovedades_sellobornera(String novedades_sellobornera) {
        this.novedades_sellobornera = novedades_sellobornera;
    }

    public String getnovedades_SELLOGABINETE() {
        return novedades_sellogabinete;
    }

    public void setnovedades_sellogabinete(String novedades_sellogabinete) {
        this.novedades_sellogabinete = novedades_sellogabinete;
    }

    public String getnovedades_CLASESERVICIO() {
        return novedades_claseservicio;
    }

    public void setnovedades_claseservicio(String novedades_claseservicio) {
        this.novedades_claseservicio = novedades_claseservicio;
    }

    public String getnovedades_COMENTARIO() {
        return novedades_comentario;
    }

    public void setnovedades_comentario(String novedades_comentario) {
        this.novedades_comentario = novedades_comentario;
    }

    public String getnovedades_LECTOR() {
        return novedades_lector;
    }

    public void setnovedades_lector(String novedades_lector) {
        this.novedades_lector = novedades_lector;
    }

    public String getnovedades_MES() {
        return novedades_mes;
    }

    public void setnovedades_mes(String novedades_mes) {
        this.novedades_mes = novedades_mes;
    }

    public String getnovedades_ANNOPERIODO() {
        return novedades_annoperiodo;
    }

    public void setnovedades_annoperiodo(String novedades_annoperiodo) {
        this.novedades_annoperiodo = novedades_annoperiodo;
    }

    public String getnovedades_TIPONOVEDAD() {
        return novedades_tiponovedad;
    }

    public void setnovedades_tiponovedad(String novedades_tiponovedad) {
        this.novedades_tiponovedad = novedades_tiponovedad;
    }

    public String getnovedades_CAUSALNOVEDAD() {
        return novedades_causalnovedad;
    }

    public void setnovedades_causalnovedad(String novedades_causalnovedad) {
        this.novedades_causalnovedad = novedades_causalnovedad;
    }

    public String getnovedades_DESCAUSALNOVEDAD() {
        return novedades_descausalnovedad;
    }

    public void setnovedades_descausalnovedad(String novedades_descausalnovedad) {
        this.novedades_descausalnovedad = novedades_descausalnovedad;
    }

    public String getnovedades_CRNL() {
        return novedades_CRNL;
    }

    public void setnovedades_CRNL(String novedades_CRNL) {
        this.novedades_CRNL =  "\r\n";
    }

    BufferedReader fin;
    byte[] byteArray;
    String archivo_Novedades;
    public static final int LONGITUD_REGISTRO = 648;
    public int total_Novedades;
    public int ultimo_Novedades;
    public int encontro_Novedades;
    public String buscar_Novedades;
    String texto;
    // File ruta_sd = Environment.getExternalStorageDirectory();
    // File ruta_sd = new File('C:\\Users\\SGlobal\\Documents\\PruebasJava\\CensoSalida.txt');
    RandomAccessFile rFile;

    public String getArchivo_Novedades() {
        return archivo_Novedades;
    }

    public void setArchivo_Novedades(String archivo_Novedades) {
        this.archivo_Novedades = archivo_Novedades;
    }

    public Boolean abrir_Novedades(String nombreArchivo) {
        if (nombreArchivo.length() == 0) {
            return false;
        }
        try {
            rFile = new RandomAccessFile( nombreArchivo, "rw"); // C:/Users/SGlobal/Documents/Pruebas Java/CensoSalida.txt
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        archivo_Novedades = nombreArchivo;
        return abrir(archivo_Novedades);
    }

    private Boolean abrir(String nombre_archivo) {
        try {
            int fileSize = (int) rFile.length();
            byteArray = new byte[fileSize];
            rFile.readFully(byteArray, 0, fileSize);
            texto = new String(byteArray);
            total_Novedades = fileSize / LONGITUD_REGISTRO;
            return true;
        } catch (Exception ex) {
            // Log.e('Ficheros', 'Error al leer fichero desde tarjeta SD');
            ex.printStackTrace();
            return false;
        }
    }

    public void Cerrar_Novedades() {
        // cerrar el archivo abierto
        try {
            rFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean escribir_Novedades(int posicion) {
        posicion_Novedades(posicion, LONGITUD_REGISTRO);
        rellenar_Novedades();
        String texto = novedades_numeroacta + sep + novedades_ciclo + sep + novedades_codobservacion + sep + novedades_observacion + sep + novedades_fecha + sep + novedades_cuenta + sep
                + novedades_nombreusuario + sep + novedades_cedula + sep + novedades_direccion + sep + novedades_barrio + sep + novedades_telefono + sep + novedades_rutaanterior + sep
                + novedades_rutaactual + sep + novedades_rutapropuesta + sep + novedades_rutaposterior + sep + novedades_marca + sep + novedades_contador + sep + novedades_conexion + sep
                + novedades_anno + sep + novedades_k + sep + novedades_w + sep + novedades_corriente + sep + novedades_tipomedidor + sep + novedades_lectura + sep + novedades_sellotapa + sep
                + novedades_sellobornera + sep + novedades_sellogabinete + sep + novedades_claseservicio + sep + novedades_comentario + sep + novedades_lector + sep + novedades_mes + sep
                + novedades_annoperiodo + sep + novedades_tiponovedad + sep + novedades_causalnovedad + sep + novedades_descausalnovedad + sep + novedades_CRNL;
      //  Log.e("error",LONGITUD_REGISTRO+" cad novedad "+texto);

        Log.e("error",LONGITUD_REGISTRO+" long novedad "+texto.length());
        try {
            if (null != rFile && (texto.length() == LONGITUD_REGISTRO)) {
                rFile.writeBytes(texto);
                System.out.println("Los Datos fueron grabados correctamente");
                return true;
            } else {
                System.out.println("Se presento problema al escribir en el archivo; longitud Errada: tamano texto: " + texto.length() + texto);
                return false;
            }
        } catch (IOException ioe) {
            System.out.println("Se presento problema al escribir en el archivo Longitud Errada: " + LONGITUD_REGISTRO);
            return false;
        }
    }

    public void rellenar_Novedades() {
        try {
            novedades_numeroacta = String.format("%-15s", novedades_numeroacta);
            novedades_ciclo = String.format("%-3s", novedades_ciclo);
            novedades_codobservacion = String.format("%-2s", novedades_codobservacion);
            novedades_observacion = String.format("%-30s", novedades_observacion);
            novedades_fecha = String.format("%-19s", novedades_fecha);
            novedades_cuenta = String.format("%-9s", novedades_cuenta);
            novedades_nombreusuario = String.format("%-48s", novedades_nombreusuario);
            novedades_cedula = String.format("%-15s", novedades_cedula);
            novedades_direccion = String.format("%-32s", novedades_direccion);
            novedades_barrio = String.format("%-32s", novedades_barrio);
            novedades_telefono = String.format("%-13s", novedades_telefono);
            novedades_rutaanterior = String.format("%-13s", novedades_rutaanterior);
            novedades_rutaactual = String.format("%-13s", novedades_rutaactual);
            novedades_rutapropuesta = String.format("%-13s", novedades_rutapropuesta);
            novedades_rutaposterior = String.format("%-13s", novedades_rutaposterior);
            novedades_marca = String.format("%-3s", novedades_marca);
            novedades_contador = String.format("%-16s", novedades_contador);
            novedades_conexion = String.format("%-1s", novedades_conexion);
            novedades_anno = String.format("%-4s", novedades_anno);
            novedades_k = String.format("%-7s", novedades_k);
            novedades_w = String.format("%-10s", novedades_w);
            novedades_corriente = String.format("%-10s", novedades_corriente);
            novedades_tipomedidor = String.format("%-10s", novedades_tipomedidor);
            novedades_lectura = String.format("%-7s", novedades_lectura);
            novedades_sellotapa = String.format("%-16s", novedades_sellotapa);
            novedades_sellobornera = String.format("%-16s", novedades_sellobornera);
            novedades_sellogabinete = String.format("%-16s", novedades_sellogabinete);
            novedades_claseservicio = String.format("%-2s", novedades_claseservicio);
            novedades_comentario = String.format("%-200s", novedades_comentario);
            novedades_lector = String.format("%-11s", novedades_lector);
            novedades_mes = String.format("%-2s", novedades_mes);
            novedades_annoperiodo = String.format("%-4s", novedades_annoperiodo);
            novedades_tiponovedad = String.format("%-2s", novedades_tiponovedad);
            novedades_causalnovedad = String.format("%-2s", novedades_causalnovedad);
            novedades_descausalnovedad = String.format("%-2s", novedades_descausalnovedad);
            novedades_CRNL ="\r\n";// String.format("%-2s", novedades_CRNL);
        } catch (Exception e) {
            System.out.println("Se presento problema al escribir en el archivo novedades.dat..");
            e.printStackTrace();
        }

        return;
    }

    public void posicion_Novedades(int registro, int tamano) {
        try {
            rFile.seek((registro - 1) * tamano);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Escribir los registros atual en el archivo en posicion
    public void lectura_Novedades(int registro) {
        String[] campos;
        encontro_Novedades = 0;
        posicion_Novedades(registro, LONGITUD_REGISTRO);
        try {
            int fileSize = (int) rFile.length();
            byteArray = new byte[fileSize];
            rFile.readFully(byteArray, 0, LONGITUD_REGISTRO);
            texto = new String(byteArray);
            setnovedades_numeroacta(texto.substring(0, 15));
            setnovedades_ciclo(texto.substring(16, 19));
            setnovedades_codobservacion(texto.substring(20, 22));
            setnovedades_observacion(texto.substring(23, 53));
            setnovedades_fecha(texto.substring(54, 73));
            setnovedades_cuenta(texto.substring(74, 83));
            setnovedades_nombreusuario(texto.substring(84, 132));
            setnovedades_cedula(texto.substring(133, 148));
            setnovedades_direccion(texto.substring(149, 181));//Ax: ojo este campo se usa para guardar enviado o no en su ultimo caracter, pero este 'objeto' no se usa en menuliquidacion, se usa generica
            setnovedades_barrio(texto.substring(182, 214));
            setnovedades_telefono(texto.substring(215, 228));
            setnovedades_rutaanterior(texto.substring(229, 242));
            setnovedades_rutaactual(texto.substring(243, 256));
            setnovedades_rutapropuesta(texto.substring(257, 270));
            setnovedades_rutaposterior(texto.substring(271, 284));
            setnovedades_marca(texto.substring(285, 288));
            setnovedades_contador(texto.substring(289, 305));
            setnovedades_conexion(texto.substring(306, 307));
            setnovedades_anno(texto.substring(308, 312));
            setnovedades_k(texto.substring(313, 320));
            setnovedades_w(texto.substring(321, 331));
            setnovedades_corriente(texto.substring(332, 342));
            setnovedades_tipomedidor(texto.substring(343, 353));
            setnovedades_lectura(texto.substring(354, 361));
            setnovedades_sellotapa(texto.substring(362, 378));
            setnovedades_sellobornera(texto.substring(379, 395));
            setnovedades_sellogabinete(texto.substring(396, 412));
            setnovedades_claseservicio(texto.substring(413, 415));//-------------
            setnovedades_comentario(texto.substring(416, 616));
            setnovedades_lector(texto.substring(617, 628));
            setnovedades_mes(texto.substring(629, 631));//-------------
            setnovedades_annoperiodo(texto.substring(632, 636));
            setnovedades_tiponovedad(texto.substring(637, 639));
            setnovedades_causalnovedad(texto.substring(640, 642));
            setnovedades_descausalnovedad(texto.substring(643, 645));//---------
            setnovedades_CRNL("\r\n");//texto.substring(646, 648)
            ultimo_Novedades = registro;
            // fin estructura
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void buscarSecuencial_novedades(String codigo, String nroActa) {
        encontro_Novedades = 0;
        for (int i = 0; i < (int) (total_Novedades); i++) {
            lectura_Novedades(i + 1);
            if (novedades_cuenta.trim() != "")
                if (codigo.trim().toUpperCase() == novedades_cuenta.trim().toUpperCase() && nroActa.trim().toUpperCase() == novedades_numeroacta.trim().toUpperCase()) {
                    encontro_Novedades = i + 1;
                    posicion_Novedades(i + 1, LONGITUD_REGISTRO);
                    i = ((int) (total_Novedades)) + 10;
                } else {
                    encontro_Novedades = 0;
                }

        }
        return;
    }

    public void buscarSecuencial_novedades(String codigo) {
        encontro_Novedades = 0;
        double cod = Double.parseDouble(codigo);
        for (int i = 0; i < (int) (total_Novedades); i++) {
            lectura_Novedades(i + 1);

            if (!novedades_cuenta.trim().equals(""))
                if (cod == Double.parseDouble(novedades_cuenta.trim())) {
                    encontro_Novedades = i + 1;
                    posicion_Novedades(i + 1, LONGITUD_REGISTRO);
                    i = ((int) (total_Novedades)) + 10;
                } else {
                    encontro_Novedades = 0;
                }
        }
        return;
    }


}
