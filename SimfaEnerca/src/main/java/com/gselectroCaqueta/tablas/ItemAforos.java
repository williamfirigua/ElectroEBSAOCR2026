package com.gselectroCaqueta.tablas;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ItemAforos {
    static final int LONGITUD_REGISTRO = 47;
    public int total_ItemAforos;
    String sep = ";";
    String ItemAforos_codigoaforo;
    String ItemAforos_descreipcion;
    String ItemAforos_CRNL;
    BufferedReader fin;
    byte[] byteArray;
    String archivo_ItemAforos;
    int ultimo_ItemAforos;
    int encontro_ItemAforos;
    String buscar_ItemAforos;
    String texto;
    RandomAccessFile rFile;

    public String getItemAforos_CODIGOAFORO() {
        return ItemAforos_codigoaforo;
    }

    public void setItemAforos_codigoaforo(String ItemAforos_codigoaforo) {
        this.ItemAforos_codigoaforo = ItemAforos_codigoaforo;
    }

    public String getItemAforos_DESCREIPCION() {
        return ItemAforos_descreipcion;
    }

    public void setItemAforos_descreipcion(String ItemAforos_descreipcion) {
        this.ItemAforos_descreipcion = ItemAforos_descreipcion;
    }

    public String getItemAforos_CRNL() {
        return ItemAforos_CRNL;
    }

    public void setItemAforos_CRNL(String ItemAforos_CRNL) {
        this.ItemAforos_CRNL = ItemAforos_CRNL;
    }

    public Boolean abrir_ItemAforos(String nombreArchivo) {
        if (nombreArchivo.length() == 0) {
            return false;
        }
        try {
            rFile = new RandomAccessFile(nombreArchivo, "rw"); //C:/Users/SGlobal/Documents/Pruebas Java/CensoSalida.txt
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        archivo_ItemAforos = nombreArchivo;
        return abrir(archivo_ItemAforos);
    }

    private Boolean abrir(String nombre_archivo) {
        try {
            int fileSize = (int) rFile.length();
            byteArray = new byte[fileSize];
            rFile.readFully(byteArray, 0, fileSize);
            texto = new String(byteArray);
            total_ItemAforos = fileSize / LONGITUD_REGISTRO;
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void Cerrar_ItemAforos() {
        // cerrar el archivo abierto
        try {
            rFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean escribir_ItemAforos(int posicion) {
        posicion_ItemAforos(posicion, LONGITUD_REGISTRO);
        rellenar_ItemAforos();
        String texto = ItemAforos_codigoaforo + sep + ItemAforos_descreipcion + sep + ItemAforos_CRNL;
        try {
            if (null != rFile && (texto.length() == LONGITUD_REGISTRO)) {
                rFile.writeBytes(texto);
                System.out.println("Los Datos fueron grabados correctamente");
                return true;
            } else {
                System.out.println("Se presento problema al escribir en el archivo; longitud Errada: tamaño texto: " + texto.length());
                return false;
            }
        } catch (IOException ioe) {
            System.out.println("Se presento problema al escribir en el archivo Longitud Errada: " + LONGITUD_REGISTRO);
            return false;
        }
    }

    public void rellenar_ItemAforos() {
        try {
            ItemAforos_codigoaforo = String.format("%-3s", ItemAforos_codigoaforo);
            ItemAforos_descreipcion = String.format("%-40s", ItemAforos_descreipcion);
            ItemAforos_CRNL = "\r\n";//= String.format("%-2s", ItemAforos_CRNL);
        } catch (Exception e) {
            System.out.println("Se presento problema al escribir en el archivo ItemAforos.dat..");
            e.printStackTrace();
        }
        return;
    }

    public void posicion_ItemAforos(int registro, int tamaño) {
        try {
            rFile.seek((registro - 1) * tamaño);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Escribir los registros atual en el archivo en posicion
    public void lectura_ItemAforos(int registro) {
        String[] campos;
        encontro_ItemAforos = 0;
        posicion_ItemAforos(registro, LONGITUD_REGISTRO);
        try {
            int fileSize = (int) rFile.length();
            byteArray = new byte[fileSize];
            rFile.readFully(byteArray, 0, LONGITUD_REGISTRO);
            texto = new String(byteArray);
            setItemAforos_codigoaforo(texto.substring(0, 3));
            setItemAforos_descreipcion(texto.substring(4, 44));
            setItemAforos_CRNL(texto.substring(45, 47));
            ultimo_ItemAforos = registro;
            //fin estructura
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void buscarSecuencial_ItemAforos(String codigo) {
        encontro_ItemAforos = 0;
        for (int i = 0; i < (int) (total_ItemAforos); i++) {
            lectura_ItemAforos(i + 1);
            if (Integer.parseInt(codigo.trim()) == Integer.parseInt(ItemAforos_codigoaforo.trim())) {
                encontro_ItemAforos = i + 1;
                posicion_ItemAforos(i + 1, LONGITUD_REGISTRO);
                i = total_ItemAforos + 10;
            } else {
                encontro_ItemAforos = 0;
            }
        }
        return;
    }
}
