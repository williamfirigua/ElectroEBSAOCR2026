package com.gselectroCaqueta.tablas;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
/// <summary>
/// Descripcion breve del archivo a generar...tablaConvenios
/// </summary>

public class TablaConvenios { //Ax ya
    String sep = ";";
    String tablaConvenios_Ano;
    String tablaConvenios_Mes;
    String tablaConvenios_DEPTO;
    String tablaConvenios_MUNICIPIO;
    String tablaConvenios_CLASE;
    String tablaConvenios_ESTRATO;
    String tablaConvenios_UBICACION;
    String tablaConvenios_CONCEPTO;
    String tablaConvenios_TIPOVALOR;
    String tablaConvenios_TIPOPORCENTUAL;
    String tablaConvenios_VALOR;
    String tablaConvenios_RANGOMINIMO;
    String tablaConvenios_RANGOMAXIMO;
    String tablaConvenios_TOPEMINIMO;
    String tablaConvenios_TOPEMAXIMO;
    String tablaConvenios_CRNL;

    public String gettablaConvenios_ANO() {
        return tablaConvenios_Ano;
    }

    public void settablaConvenios_Ano(String tablaConvenios_Ano) {
        this.tablaConvenios_Ano = tablaConvenios_Ano;
    }

    public String gettablaConvenios_MES() {
        return tablaConvenios_Mes;
    }

    public void settablaConvenios_Mes(String tablaConvenios_Mes) {
        this.tablaConvenios_Mes = tablaConvenios_Mes;
    }

    public String gettablaConvenios_DEPTO() {
        return tablaConvenios_DEPTO;
    }

    public void settablaConvenios_DEPTO(String tablaConvenios_DEPTO) {
        this.tablaConvenios_DEPTO = tablaConvenios_DEPTO;
    }

    public String gettablaConvenios_MUNICIPIO() {
        return tablaConvenios_MUNICIPIO;
    }

    public void settablaConvenios_MUNICIPIO(String tablaConvenios_MUNICIPIO) {
        this.tablaConvenios_MUNICIPIO = tablaConvenios_MUNICIPIO;
    }

    public String gettablaConvenios_CLASE() {
        return tablaConvenios_CLASE;
    }

    public void settablaConvenios_CLASE(String tablaConvenios_CLASE) {
        this.tablaConvenios_CLASE = tablaConvenios_CLASE;
    }

    public String gettablaConvenios_ESTRATO() {
        return tablaConvenios_ESTRATO;
    }

    public void settablaConvenios_ESTRATO(String tablaConvenios_ESTRATO) {
        this.tablaConvenios_ESTRATO = tablaConvenios_ESTRATO;
    }

    public String gettablaConvenios_UBICACION() {
        return tablaConvenios_UBICACION;
    }

    public void settablaConvenios_UBICACION(String tablaConvenios_UBICACION) {
        this.tablaConvenios_UBICACION = tablaConvenios_UBICACION;
    }

    public String gettablaConvenios_CONCEPTO() {
        return tablaConvenios_CONCEPTO;
    }

    public void settablaConvenios_CONCEPTO(String tablaConvenios_CONCEPTO) {
        this.tablaConvenios_CONCEPTO = tablaConvenios_CONCEPTO;
    }

    public String gettablaConvenios_TIPOVALOR() {
        return tablaConvenios_TIPOVALOR;
    }

    public void settablaConvenios_TIPOVALOR(String tablaConvenios_TIPOVALOR) {
        this.tablaConvenios_TIPOVALOR = tablaConvenios_TIPOVALOR;
    }

    public String gettablaConvenios_TIPOPORCENTUAL() {
        return tablaConvenios_TIPOPORCENTUAL;
    }

    public void settablaConvenios_TIPOPORCENTUAL(String tablaConvenios_TIPOPORCENTUAL) {
        this.tablaConvenios_TIPOPORCENTUAL = tablaConvenios_TIPOPORCENTUAL;
    }

    public String gettablaConvenios_VALOR() {
        return tablaConvenios_VALOR;
    }

    public void settablaConvenios_VALOR(String tablaConvenios_VALOR) {
        this.tablaConvenios_VALOR = tablaConvenios_VALOR;
    }

    public String gettablaConvenios_RANGOMINIMO() {
        return tablaConvenios_RANGOMINIMO;
    }

    public void settablaConvenios_RANGOMINIMO(String tablaConvenios_RANGOMINIMO) {
        this.tablaConvenios_RANGOMINIMO = tablaConvenios_RANGOMINIMO;
    }

    public String gettablaConvenios_RANGOMAXIMO() {
        return tablaConvenios_RANGOMAXIMO;
    }

    public void settablaConvenios_RANGOMAXIMO(String tablaConvenios_RANGOMAXIMO) {
        this.tablaConvenios_RANGOMAXIMO = tablaConvenios_RANGOMAXIMO;
    }

    public String gettablaConvenios_TOPEMINIMO() {
        return tablaConvenios_TOPEMINIMO;
    }

    public void settablaConvenios_TOPEMINIMO(String tablaConvenios_TOPEMINIMO) {
        this.tablaConvenios_TOPEMINIMO = tablaConvenios_TOPEMINIMO;
    }

    public String gettablaConvenios_TOPEMAXIMO() {
        return tablaConvenios_TOPEMAXIMO;
    }

    public void settablaConvenios_TOPEMAXIMO(String tablaConvenios_TOPEMAXIMO) {
        this.tablaConvenios_TOPEMAXIMO = tablaConvenios_TOPEMAXIMO;
    }

    public String gettablaConvenios_CRNL() {
        return tablaConvenios_CRNL;
    }

    public void settablaConvenios_CRNL(String tablaConvenios_CRNL) {
        this.tablaConvenios_CRNL = tablaConvenios_CRNL;
    }

    BufferedReader fin;
    byte[] byteArray;
    private String archivo_TablaConvenios;
    static final int LONGITUD_REGISTRO = 94;
    int total_TablaConvenios;
    int ultimo_TablaConvenios;
    int encontro_TablaConvenios;
    String buscar_TablaConvenios;
    String texto;
    // File ruta_sd = Environment.getExternalStorageDirectory();
    // File ruta_sd = new File('C:\\Users\\SGlobal\\Documents\\PruebasJava\\CensoSalida.txt');
    RandomAccessFile rFile;

    public Boolean abrir_TablaConvenios(String nombreArchivo) {
        if (nombreArchivo.length() == 0) {
            return false;
        }
        try {
            rFile = new RandomAccessFile(nombreArchivo, "rw"); // C:/Users/SGlobal/Documents/Pruebas Java/CensoSalida.txt
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        setArchivo_TablaConvenios(nombreArchivo);
        return abrir(getArchivo_TablaConvenios());
    }

    private Boolean abrir(String nombre_archivo) {
        try {
            int fileSize = (int) rFile.length();
            byteArray = new byte[fileSize];
            rFile.readFully(byteArray, 0, fileSize);
            texto = new String(byteArray);
            total_TablaConvenios = fileSize / LONGITUD_REGISTRO;
            return true;
        } catch (Exception ex) {
            // Log.e('Ficheros', 'Error al leer fichero desde tarjeta SD');
            ex.printStackTrace();
            return false;
        }
    }

    public void Cerrar_TablaConvenios() {
        // cerrar el archivo abierto
        try {
            rFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean escribir_TablaConvenios(int posicion) {
        posicion_TablaConvenios(posicion, LONGITUD_REGISTRO);
        rellenar_TablaConvenios();
        String texto = tablaConvenios_Ano + sep + tablaConvenios_Mes + sep + tablaConvenios_DEPTO + sep + tablaConvenios_MUNICIPIO + sep + tablaConvenios_CLASE + sep + tablaConvenios_ESTRATO + sep
                + tablaConvenios_UBICACION + sep + tablaConvenios_CONCEPTO + sep + tablaConvenios_TIPOVALOR + sep + tablaConvenios_TIPOPORCENTUAL + sep + tablaConvenios_VALOR + sep
                + tablaConvenios_RANGOMINIMO + sep + tablaConvenios_RANGOMAXIMO + sep + tablaConvenios_TOPEMINIMO + sep + tablaConvenios_TOPEMAXIMO + sep + tablaConvenios_CRNL;
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

    public void rellenar_TablaConvenios() {
        try {
            tablaConvenios_Ano = String.format("%-4s", tablaConvenios_Ano);
            tablaConvenios_Mes = String.format("%-2s", tablaConvenios_Mes);
            tablaConvenios_DEPTO = String.format("%-2s", tablaConvenios_DEPTO);
            tablaConvenios_MUNICIPIO = String.format("%-3s", tablaConvenios_MUNICIPIO);
            tablaConvenios_CLASE = String.format("%-3s", tablaConvenios_CLASE);
            tablaConvenios_ESTRATO = String.format("%-1s", tablaConvenios_ESTRATO);
            tablaConvenios_UBICACION = String.format("%-1s", tablaConvenios_UBICACION);
            tablaConvenios_CONCEPTO = String.format("%-5s", tablaConvenios_CONCEPTO);
            tablaConvenios_TIPOVALOR = String.format("%-1s", tablaConvenios_TIPOVALOR);
            tablaConvenios_TIPOPORCENTUAL = String.format("%-1s", tablaConvenios_TIPOPORCENTUAL);
            tablaConvenios_VALOR = String.format("%-10s", tablaConvenios_VALOR);
            tablaConvenios_RANGOMINIMO = String.format("%-10s", tablaConvenios_RANGOMINIMO);
            tablaConvenios_RANGOMAXIMO = String.format("%-12s", tablaConvenios_RANGOMAXIMO);
            tablaConvenios_TOPEMINIMO = String.format("%-10s", tablaConvenios_TOPEMINIMO);
            tablaConvenios_TOPEMAXIMO = String.format("%-12s", tablaConvenios_TOPEMAXIMO);
            tablaConvenios_CRNL = String.format("%-2s", tablaConvenios_CRNL);
        } catch (Exception e) {
            System.out.println("Se presento problema al escribir en el archivo tablaConvenios.dat..");
            e.printStackTrace();
        }

        return;
    }

    public void posicion_TablaConvenios(int registro, int tamano) {
        try {
            rFile.seek((registro - 1) * tamano);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Escribir los registros atual en el archivo en posicion
    public void lectura_TablaConvenios(int registro) {
        String[] campos;
        abrir_TablaConvenios(getArchivo_TablaConvenios());
        encontro_TablaConvenios = 0;
        posicion_TablaConvenios(registro, LONGITUD_REGISTRO);
        try {
            int fileSize = (int) rFile.length();
            byteArray = new byte[fileSize];
            rFile.readFully(byteArray, 0, LONGITUD_REGISTRO);
            texto = new String(byteArray);
            settablaConvenios_Ano(texto.substring(0, 4));
            settablaConvenios_Mes(texto.substring(5, 7));
            settablaConvenios_DEPTO(texto.substring(8, 10));
            settablaConvenios_MUNICIPIO(texto.substring(11, 14));
            settablaConvenios_CLASE(texto.substring(15, 18));
            settablaConvenios_ESTRATO(texto.substring(19, 20));
            settablaConvenios_UBICACION(texto.substring(21, 22));
            settablaConvenios_CONCEPTO(texto.substring(23, 28));
            settablaConvenios_TIPOVALOR(texto.substring(29, 30));
            settablaConvenios_TIPOPORCENTUAL(texto.substring(31, 32));
            settablaConvenios_VALOR(texto.substring(33, 43));
            settablaConvenios_RANGOMINIMO(texto.substring(44, 54));
            settablaConvenios_RANGOMAXIMO(texto.substring(55, 67));
            settablaConvenios_TOPEMINIMO(texto.substring(68, 78));
            settablaConvenios_TOPEMAXIMO(texto.substring(79, 91));
            settablaConvenios_CRNL(texto.substring(92, 94));
            ultimo_TablaConvenios = registro;
            // fin estructura
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getArchivo_TablaConvenios() {
        return archivo_TablaConvenios;
    }

    public void setArchivo_TablaConvenios(String archivo_TablaConvenios) {
        this.archivo_TablaConvenios = archivo_TablaConvenios;
    }

}
