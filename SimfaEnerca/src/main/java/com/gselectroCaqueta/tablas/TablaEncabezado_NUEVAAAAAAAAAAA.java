package com.gselectroCaqueta.tablas;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
/// <summary>
/// Descripcion breve del archivo a generar...tablaEncabezado
/// </summary>

public class TablaEncabezado_NUEVAAAAAAAAAAA { //Ax ya???   TABLAENCABEZADO //victor dce dejar la del huila

    static final int LONGITUD_REGISTRO = 41;
    String sep = ";";

    String tablaEncabezado_ciclo;
    String tablaEncabezado_DPTO;
    String tablaEncabezado_MUNICIPIO;
    String tablaEncabezado_LECTOR;
    String tablaEncabezado_CLAVELECTOR;
    String tablaEncabezado_TERMINAL;
    String tablaEncabezado_IMPRESORA;
    String tablaEncabezado_fechaproceso;
    String tablaEncabezado_CRNL;

    BufferedReader fin;
    byte[] byteArray;
    int total_TablaEncabezado;
    int ultimo_TablaEncabezado;
    int encontro_TablaEncabezado;
    String buscar_TablaEncabezado;
    String texto;
    RandomAccessFile rFile;
    private String archivo_TablaEncabezado;

    public String gettablaEncabezado_CICLO() {
        return tablaEncabezado_ciclo;
    }

    public void settablaEncabezado_ciclo(String tablaEncabezado_ciclo) {
        this.tablaEncabezado_ciclo = tablaEncabezado_ciclo;
    }

    public String gettablaEncabezado_DPTO() {
        return tablaEncabezado_DPTO;
    }

    public void settablaEncabezado_DPTO(String tablaEncabezado_DPTO) {
        this.tablaEncabezado_DPTO = tablaEncabezado_DPTO;
    }

    public String gettablaEncabezado_MUNICIPIO() {
        return tablaEncabezado_MUNICIPIO;
    }

    public void settablaEncabezado_MUNICIPIO(String tablaEncabezado_MUNICIPIO) {
        this.tablaEncabezado_MUNICIPIO = tablaEncabezado_MUNICIPIO;
    }

    public String gettablaEncabezado_LECTOR() {
        return tablaEncabezado_LECTOR;
    }

    public void settablaEncabezado_LECTOR(String tablaEncabezado_LECTOR) {
        this.tablaEncabezado_LECTOR = tablaEncabezado_LECTOR;
    }

    public String gettablaEncabezado_CLAVELECTOR() {
        return tablaEncabezado_CLAVELECTOR;
    }

    public void settablaEncabezado_CLAVELECTOR(String tablaEncabezado_CLAVELECTOR) {
        this.tablaEncabezado_CLAVELECTOR = tablaEncabezado_CLAVELECTOR;
    }

    public String gettablaEncabezado_TERMINAL() {
        return tablaEncabezado_TERMINAL;
    }

    public void settablaEncabezado_TERMINAL(String tablaEncabezado_TERMINAL) {
        this.tablaEncabezado_TERMINAL = tablaEncabezado_TERMINAL;
    }

    public String gettablaEncabezado_IMPRESORA() {
        return tablaEncabezado_IMPRESORA;
    }

    public void settablaEncabezado_IMPRESORA(String tablaEncabezado_IMPRESORA) {
        this.tablaEncabezado_IMPRESORA = tablaEncabezado_IMPRESORA;
    }

    public String gettablaEncabezado_FECHAPROCESO() {
        return tablaEncabezado_fechaproceso;
    }

    public void settablaEncabezado_fechaproceso(String tablaEncabezado_fechaproceso) {
        this.tablaEncabezado_fechaproceso = tablaEncabezado_fechaproceso;
    }

    public String gettablaEncabezado_CRNL() {
        return tablaEncabezado_CRNL;
    }

    public void settablaEncabezado_CRNL(String tablaEncabezado_CRNL) {
        this.tablaEncabezado_CRNL = tablaEncabezado_CRNL;
    }

    public Boolean abrir_TablaEncabezado(String nombreArchivo) {
        if (nombreArchivo.length() == 0) {
            return false;
        }
        try {
            rFile = new RandomAccessFile(nombreArchivo, "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        setArchivo_TablaEncabezado(nombreArchivo);
        return abrir(getArchivo_TablaEncabezado());
    }

    private Boolean abrir(String nombre_archivo) {
        try {
            int fileSize = (int) rFile.length();
            byteArray = new byte[fileSize];
            rFile.readFully(byteArray, 0, fileSize);
            texto = new String(byteArray);
            total_TablaEncabezado = fileSize / LONGITUD_REGISTRO;
            return true;
        } catch (Exception ex) {
            // Log.e('Ficheros', 'Error al leer fichero desde tarjeta SD');
            ex.printStackTrace();
            return false;
        }
    }

    public void Cerrar_TablaEncabezado() {
        // cerrar el archivo abierto
        try {
            rFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean escribir_TablaEncabezado(int posicion) {
        posicion_TablaEncabezado(posicion, LONGITUD_REGISTRO);
        rellenar_TablaEncabezado();
        String texto = tablaEncabezado_ciclo + sep + tablaEncabezado_DPTO + sep + tablaEncabezado_MUNICIPIO + sep + tablaEncabezado_LECTOR + sep + tablaEncabezado_CLAVELECTOR + sep
                + tablaEncabezado_TERMINAL + sep + tablaEncabezado_IMPRESORA + sep + tablaEncabezado_fechaproceso + sep +  tablaEncabezado_CRNL;
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

    public void rellenar_TablaEncabezado() {
        try {
            tablaEncabezado_ciclo = String.format("%-2s", tablaEncabezado_ciclo);
            tablaEncabezado_DPTO = String.format("%-2s", tablaEncabezado_DPTO);
            tablaEncabezado_MUNICIPIO = String.format("%-3s", tablaEncabezado_MUNICIPIO);
            tablaEncabezado_LECTOR = String.format("%-3s", tablaEncabezado_LECTOR);
            tablaEncabezado_CLAVELECTOR = String.format("%-5s", tablaEncabezado_CLAVELECTOR);
            tablaEncabezado_TERMINAL = String.format("%-3s", tablaEncabezado_TERMINAL);
            tablaEncabezado_IMPRESORA = String.format("%-3s", tablaEncabezado_IMPRESORA);
            tablaEncabezado_fechaproceso = String.format("%-10s", tablaEncabezado_fechaproceso);
                       tablaEncabezado_CRNL = String.format("%-2s", tablaEncabezado_CRNL);
        } catch (Exception e) {
            System.out.println("Se presento problema al escribir en el archivo tablaEncabezado.dat..");
            e.printStackTrace();
        }

        return;
    }

    public void posicion_TablaEncabezado(int registro, int tamano) {
        try {
            rFile.seek((registro - 1) * tamano);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Escribir los registros atual en el archivo en posicion
    public void lectura_TablaEncabezado(int registro) {
        String[] campos;
        encontro_TablaEncabezado = 0;
        posicion_TablaEncabezado(registro, LONGITUD_REGISTRO);
        try {
            int fileSize = (int) rFile.length();
            byteArray = new byte[fileSize];
            rFile.readFully(byteArray, 0, LONGITUD_REGISTRO);
            texto = new String(byteArray);
            settablaEncabezado_ciclo(texto.substring(0, 2));
            settablaEncabezado_DPTO(texto.substring(3, 5));
            settablaEncabezado_MUNICIPIO(texto.substring(6, 9));
            settablaEncabezado_LECTOR(texto.substring(10, 13));
            settablaEncabezado_CLAVELECTOR(texto.substring(14, 19));
            settablaEncabezado_TERMINAL(texto.substring(20, 23));
            settablaEncabezado_IMPRESORA(texto.substring(24 ,27));
            settablaEncabezado_fechaproceso(texto.substring(28, 38));
            settablaEncabezado_CRNL(texto.substring(39, 41));
            ultimo_TablaEncabezado = registro;
            // fin estructura
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getArchivo_TablaEncabezado() {
        return archivo_TablaEncabezado;
    }

    public void setArchivo_TablaEncabezado(String archivo_TablaEncabezado) {
        this.archivo_TablaEncabezado = archivo_TablaEncabezado;
    }
}
