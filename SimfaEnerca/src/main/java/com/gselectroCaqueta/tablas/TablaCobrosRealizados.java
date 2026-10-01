package com.gselectroCaqueta.tablas;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
/// <summary>
/// Descripcion breve del archivo a generar...tablaCobrosRealizados
/// </summary>

public class TablaCobrosRealizados { //Ax ya es: S_COBRO
    static final int LONGITUD_REGISTRO = 113;//112 -- Nrocuentacliente de 9 a 10 digitos (2026)
    String sep = ";";
    String tablaCobrosRealizados_Nrocuentacliente;
    String tablaCobrosRealizados_Conceptodecobro;
    String tablaCobrosRealizados_Indicadoractividad;
    String tablaCobrosRealizados_Valor;
    String tablaCobrosRealizados_saldopendiente;
    String tablaCobrosRealizados_cuotaspendientes;
    String tablaCobrosRealizados_idconcepto;
    String tablaCobrosRealizados_idconvenio;
    String tablaCobrosRealizados_primerconvenio;
    String tablaCobrosRealizados_nroconvenios;
    String tablaCobrosRealizados_porcentaje;
    String tablaCobrosRealizados_rangominimo;
    String tablaCobrosRealizados_rangomaximo;
    String tablaCobrosRealizados_periodofacturado;
    String tablaCobrosRealizados_FIN;
    BufferedReader fin;
    byte[] byteArray;
    int total_TablaCobrosRealizados;
    int ultimo_TablaCobrosRealizados;
    int encontro_TablaCobrosRealizados;
    String buscar_TablaCobrosRealizados;
    String texto;
    RandomAccessFile rFile;
    private int _fileSize;
    private String archivo_TablaCobrosRealizados;

    public String gettablaCobrosRealizados_NROCUENTACLIENTE() {
        return tablaCobrosRealizados_Nrocuentacliente;
    }

    public void settablaCobrosRealizados_Nrocuentacliente(String tablaCobrosRealizados_Nrocuentacliente) {
        this.tablaCobrosRealizados_Nrocuentacliente = tablaCobrosRealizados_Nrocuentacliente;
    }

    public String gettablaCobrosRealizados_CONCEPTODECOBRO() {
        return tablaCobrosRealizados_Conceptodecobro;
    }

    public void settablaCobrosRealizados_Conceptodecobro(String tablaCobrosRealizados_Conceptodecobro) {
        this.tablaCobrosRealizados_Conceptodecobro = tablaCobrosRealizados_Conceptodecobro;
    }

    public String gettablaCobrosRealizados_INDICADORACTIVIDAD() {
        return tablaCobrosRealizados_Indicadoractividad;
    }

    public void settablaCobrosRealizados_Indicadoractividad(String tablaCobrosRealizados_Indicadoractividad) {
        this.tablaCobrosRealizados_Indicadoractividad = tablaCobrosRealizados_Indicadoractividad;
    }

    public String gettablaCobrosRealizados_VALOR() {
        return tablaCobrosRealizados_Valor;
    }

    public void settablaCobrosRealizados_Valor(String tablaCobrosRealizados_Valor) {
        this.tablaCobrosRealizados_Valor = tablaCobrosRealizados_Valor;
    }

    public String gettablaCobrosRealizados_SALDOPENDIENTE() {
        return tablaCobrosRealizados_saldopendiente;
    }

    public void settablaCobrosRealizados_saldopendiente(String tablaCobrosRealizados_saldopendiente) {
        this.tablaCobrosRealizados_saldopendiente = tablaCobrosRealizados_saldopendiente;
    }

    public String gettablaCobrosRealizados_CUOTASPENDIENTES() {
        return tablaCobrosRealizados_cuotaspendientes;
    }

    public void settablaCobrosRealizados_cuotaspendientes(String tablaCobrosRealizados_cuotaspendientes) {
        this.tablaCobrosRealizados_cuotaspendientes = tablaCobrosRealizados_cuotaspendientes;
    }

    public String gettablaCobrosRealizados_IDCONCEPTO() {
        return tablaCobrosRealizados_idconcepto;
    }

    public void settablaCobrosRealizados_idconcepto(String tablaCobrosRealizados_idconcepto) {
        this.tablaCobrosRealizados_idconcepto = tablaCobrosRealizados_idconcepto;
    }

    public String gettablaCobrosRealizados_IDCONVENIO() {
        return tablaCobrosRealizados_idconvenio;
    }

    public void settablaCobrosRealizados_idconvenio(String tablaCobrosRealizados_idconvenio) {
        this.tablaCobrosRealizados_idconvenio = tablaCobrosRealizados_idconvenio;
    }

    public String gettablaCobrosRealizados_PRIMERCONVENIO() {
        return tablaCobrosRealizados_primerconvenio;
    }

    public void settablaCobrosRealizados_primerconvenio(String tablaCobrosRealizados_primerconvenio) {
        this.tablaCobrosRealizados_primerconvenio = tablaCobrosRealizados_primerconvenio;
    }

    public String gettablaCobrosRealizados_NROCONVENIOS() {
        return tablaCobrosRealizados_nroconvenios;
    }

    public void settablaCobrosRealizados_nroconvenios(String tablaCobrosRealizados_nroconvenios) {
        this.tablaCobrosRealizados_nroconvenios = tablaCobrosRealizados_nroconvenios;
    }

    public String gettablaCobrosRealizados_PORCENTAJE() {
        return tablaCobrosRealizados_porcentaje;
    }

    public void settablaCobrosRealizados_porcentaje(String tablaCobrosRealizados_porcentaje) {
        this.tablaCobrosRealizados_porcentaje = tablaCobrosRealizados_porcentaje;
    }

    public String gettablaCobrosRealizados_RANGOMINIMO() {
        return tablaCobrosRealizados_rangominimo;
    }

    public void settablaCobrosRealizados_rangominimo(String tablaCobrosRealizados_rangominimo) {
        this.tablaCobrosRealizados_rangominimo = tablaCobrosRealizados_rangominimo;
    }

    public String gettablaCobrosRealizados_RANGOMAXIMO() {
        return tablaCobrosRealizados_rangomaximo;
    }

    public void settablaCobrosRealizados_rangomaximo(String tablaCobrosRealizados_rangomaximo) {
        this.tablaCobrosRealizados_rangomaximo = tablaCobrosRealizados_rangomaximo;
    }

    public String gettablaCobrosRealizados_PERIODOFACTURADO() {
        return tablaCobrosRealizados_periodofacturado;
    }

    public void settablaCobrosRealizados_periodofacturado(String tablaCobrosRealizados_periodofacturado) {
        this.tablaCobrosRealizados_periodofacturado = tablaCobrosRealizados_periodofacturado;
    }

    public String gettablaCobrosRealizados_FIN() {
        return tablaCobrosRealizados_FIN;
    }

    public void settablaCobrosRealizados_FIN(String tablaCobrosRealizados_FIN) {
        this.tablaCobrosRealizados_FIN = tablaCobrosRealizados_FIN;
    }

    public Boolean abrir_TablaCobrosRealizados(String nombreArchivo) {
        if (nombreArchivo.length() == 0) {
            return false;
        }
        try {
            rFile = new RandomAccessFile(nombreArchivo, "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        setArchivo_TablaCobrosRealizados(nombreArchivo);
        return abrir(getArchivo_TablaCobrosRealizados());
    }

    private Boolean abrir(String nombre_archivo) {
        try {
            _fileSize = (int) rFile.length();

            if (_fileSize > 0) {
                byteArray = new byte[LONGITUD_REGISTRO];//Ax:  fileSize
                rFile.readFully(byteArray, 0, LONGITUD_REGISTRO);//Ax: fileSize
                // texto = new String(byteArray);
                total_TablaCobrosRealizados = _fileSize / LONGITUD_REGISTRO;
            } else {
                byteArray = new byte[_fileSize];
                rFile.readFully(byteArray, 0, _fileSize);
                total_TablaCobrosRealizados = 0;
            }
            return true;
        } catch (Exception ex) {
            // Log.e('Ficheros', 'Error al leer fichero desde tarjeta SD');
            ex.printStackTrace();
            return false;
        }
    }

    public void Cerrar_TablaCobrosRealizados() {
        // cerrar el archivo abierto
        try {
            rFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean escribir_TablaCobrosRealizados(int posicion) {
        posicion_TablaCobrosRealizados(posicion, LONGITUD_REGISTRO);
        rellenar_TablaCobrosRealizados();
        String texto = tablaCobrosRealizados_Nrocuentacliente + sep + tablaCobrosRealizados_Conceptodecobro + sep + tablaCobrosRealizados_Indicadoractividad + sep + tablaCobrosRealizados_Valor + sep
                + tablaCobrosRealizados_saldopendiente + sep + tablaCobrosRealizados_cuotaspendientes + sep + tablaCobrosRealizados_idconcepto + sep + tablaCobrosRealizados_idconvenio + sep
                + tablaCobrosRealizados_primerconvenio + sep + tablaCobrosRealizados_nroconvenios + sep + tablaCobrosRealizados_porcentaje + sep + tablaCobrosRealizados_rangominimo + sep
                + tablaCobrosRealizados_rangomaximo + sep + tablaCobrosRealizados_periodofacturado + sep + tablaCobrosRealizados_FIN;
        try {
            Log.e("error",LONGITUD_REGISTRO+" longitud co_cobro "+texto.length());
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

    public void rellenar_TablaCobrosRealizados() {
        try {
            tablaCobrosRealizados_Nrocuentacliente = String.format("%-10s", tablaCobrosRealizados_Nrocuentacliente);//era 9
            tablaCobrosRealizados_Conceptodecobro = String.format("%-5s", tablaCobrosRealizados_Conceptodecobro);
            tablaCobrosRealizados_Indicadoractividad = String.format("%-1s", tablaCobrosRealizados_Indicadoractividad);
            tablaCobrosRealizados_Valor = String.format("%10s", tablaCobrosRealizados_Valor);//.replace(" ","0");
            tablaCobrosRealizados_saldopendiente = String.format("%-10s", tablaCobrosRealizados_saldopendiente);
            tablaCobrosRealizados_cuotaspendientes = String.format("%-2s", tablaCobrosRealizados_cuotaspendientes);
            tablaCobrosRealizados_idconcepto = String.format("%-3s", tablaCobrosRealizados_idconcepto);
            tablaCobrosRealizados_idconvenio = String.format("%-1s", tablaCobrosRealizados_idconvenio);
            tablaCobrosRealizados_primerconvenio = String.format("%-5s", tablaCobrosRealizados_primerconvenio);
            tablaCobrosRealizados_nroconvenios = String.format("%-2s", tablaCobrosRealizados_nroconvenios);
            tablaCobrosRealizados_porcentaje = String.format("%-6s", tablaCobrosRealizados_porcentaje);
            tablaCobrosRealizados_rangominimo = String.format("%-10s", tablaCobrosRealizados_rangominimo);
            tablaCobrosRealizados_rangomaximo = String.format("%-10s", tablaCobrosRealizados_rangomaximo);
            tablaCobrosRealizados_periodofacturado = String.format("%-22s", tablaCobrosRealizados_periodofacturado);
            tablaCobrosRealizados_FIN = "\r\n";//= String.format("%-2s", tablaCobrosRealizados_FIN);
        } catch (Exception e) {
            System.out.println("Se presento problema al escribir en el archivo tablaCobrosRealizados.dat..");
            e.printStackTrace();
        }

        return;
    }

    public void posicion_TablaCobrosRealizados(int registro, int tamano) {
        try {
            rFile.seek((registro - 1) * tamano);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Escribir los registros atual en el archivo en posicion
    public void lectura_TablaCobrosRealizados(int registro) {
        //String[] campos;
        encontro_TablaCobrosRealizados = 0;
        posicion_TablaCobrosRealizados(registro, LONGITUD_REGISTRO);
        try {
            //Ax: int fileSize = (int) rFile.length();
            byteArray = new byte[LONGITUD_REGISTRO];//Ax: fileSize
            rFile.read(byteArray);//Ax: rFile.readFully(byteArray, 0, LONGITUD_REGISTRO);
            texto = new String(byteArray);
            settablaCobrosRealizados_Nrocuentacliente(texto.substring(0, 10));//Nrocuentacliente ahora 10 digitos (era 9)
            settablaCobrosRealizados_Conceptodecobro(texto.substring(11, 16));
            settablaCobrosRealizados_Indicadoractividad(texto.substring(17, 18));
            settablaCobrosRealizados_Valor(texto.substring(19, 29));
            settablaCobrosRealizados_saldopendiente(texto.substring(30, 40));
            settablaCobrosRealizados_cuotaspendientes(texto.substring(41, 43));
            settablaCobrosRealizados_idconcepto(texto.substring(44, 47));
            settablaCobrosRealizados_idconvenio(texto.substring(48, 49));
            settablaCobrosRealizados_primerconvenio(texto.substring(50, 55));
            settablaCobrosRealizados_nroconvenios(texto.substring(56, 58));
            settablaCobrosRealizados_porcentaje(texto.substring(59, 65));
            settablaCobrosRealizados_rangominimo(texto.substring(66, 76));
            settablaCobrosRealizados_rangomaximo(texto.substring(77, 87));
            settablaCobrosRealizados_periodofacturado(texto.substring(88, 110));
            settablaCobrosRealizados_FIN(texto.substring(111, 113));
            ultimo_TablaCobrosRealizados = registro;
            // fin estructura
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void buscarSecuencial_TablaCobrosRealizados(String codigo) {
        encontro_TablaCobrosRealizados = 0;
        for (int i = 0; i < (int) (total_TablaCobrosRealizados); i++) {
            lectura_TablaCobrosRealizados(i + 1);
            if (Integer.parseInt(codigo.trim()) == Integer.parseInt(tablaCobrosRealizados_Nrocuentacliente.trim())) {
                encontro_TablaCobrosRealizados = i + 1;
                posicion_TablaCobrosRealizados(i + 1, LONGITUD_REGISTRO);
                i = total_TablaCobrosRealizados + 10;
            } else {
                encontro_TablaCobrosRealizados = 0;
            }
        }
        return;
    }

    public String getArchivo_TablaCobrosRealizados() {
        return archivo_TablaCobrosRealizados;
    }

    public void setArchivo_TablaCobrosRealizados(String archivo_TablaCobrosRealizados) {
        this.archivo_TablaCobrosRealizados = archivo_TablaCobrosRealizados;
    }
}
