package com.gselectroCaqueta.tablas;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.os.Environment;
/// <summary>
/// Descripci�n breve del archivo a generar...tablaAforadores
/// </summary>

public class TablaAforadores {
    String sep = ";";
    String tablaAforadores_CODIGO;
    String tablaAforadores_CLAVE;
    String tablaAforadores_DESCRIPCION;
    String tablaAforadores_ESTADO;
    String tablaAforadores_CAMPO1;
    String tablaAforadores_CAMPO2;
    String tablaAforadores_CRNL;

    private int _fileSize;

    BufferedReader fin;
    byte[] byteArray;
    private String archivo_TablaAforadores;
    static final int LONGITUD_REGISTRO = 91;
    int total_TablaAforadores;
    int ultimo_TablaAforadores;
    private int encontro_TablaAforadores;
    String buscar_TablaAforadores;
    String texto;
    // File ruta_sd = new File('C:\\Users\\SGlobal\\Documents\\PruebasJava\\CensoSalida.txt');
    RandomAccessFile rFile;

    public String gettablaAforadores_CODIGO() {
        return tablaAforadores_CODIGO;
    }

    public void settablaAforadores_CODIGO(String tablaAforadores_CODIGO) {
        this.tablaAforadores_CODIGO = tablaAforadores_CODIGO;
    }

    public String gettablaAforadores_CLAVE() {
        return tablaAforadores_CLAVE;
    }

    public void settablaAforadores_CLAVE(String tablaAforadores_CLAVE) {
        this.tablaAforadores_CLAVE = tablaAforadores_CLAVE;
    }

    public String gettablaAforadores_DESCRIPCION() {
        return tablaAforadores_DESCRIPCION;
    }

    public void settablaAforadores_DESCRIPCION(String tablaAforadores_DESCRIPCION) {
        this.tablaAforadores_DESCRIPCION = tablaAforadores_DESCRIPCION;
    }

    public String gettablaAforadores_ESTADO() {
        return tablaAforadores_ESTADO;
    }

    public void settablaAforadores_ESTADO(String tablaAforadores_ESTADO) {
        this.tablaAforadores_ESTADO = tablaAforadores_ESTADO;
    }

    public String gettablaAforadores_CAMPO1() {
        return tablaAforadores_CAMPO1;
    }

    public void settablaAforadores_CAMPO1(String tablaAforadores_CAMPO1) {
        this.tablaAforadores_CAMPO1 = tablaAforadores_CAMPO1;
    }

    public String gettablaAforadores_CAMPO2() {
        return tablaAforadores_CAMPO2;
    }

    public void settablaAforadores_CAMPO2(String tablaAforadores_CAMPO2) {
        this.tablaAforadores_CAMPO2 = tablaAforadores_CAMPO2;
    }

    public String gettablaAforadores_CRNL() {
        return tablaAforadores_CRNL;
    }

    public void settablaAforadores_CRNL(String tablaAforadores_CRNL) {
        this.tablaAforadores_CRNL = tablaAforadores_CRNL;
    }

    public Boolean abrir_TablaAforadores(String nombreArchivo) {
        if (nombreArchivo.length() == 0) {
            return false;
        }
        try {
            rFile = new RandomAccessFile(nombreArchivo, "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        setArchivo_TablaAforadores(nombreArchivo);
        return abrir(getArchivo_TablaAforadores());
    }

    private Boolean abrir(String nombre_archivo) {
        try {
            _fileSize = (int) rFile.length();

            if(_fileSize>0) {
                byteArray = new byte[LONGITUD_REGISTRO];//Ax:  fileSize
                rFile.readFully(byteArray, 0, LONGITUD_REGISTRO);//Ax: fileSize
                // texto = new String(byteArray);
                total_TablaAforadores = _fileSize / LONGITUD_REGISTRO;
            }else{
                byteArray = new byte[_fileSize];
                rFile.readFully(byteArray, 0, _fileSize);
                total_TablaAforadores = 0;
            }

            return true;
        } catch (Exception ex) {
            // Log.e('Ficheros', 'Error al leer fichero desde tarjeta SD');
            ex.printStackTrace();
            return false;
        }
    }

    public void Cerrar_TablaAforadores() {
        // cerrar el archivo abierto
        try {
            rFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean escribir_TablaAforadores(int registro) {
        posicion_TablaAforadores(registro, LONGITUD_REGISTRO);
        rellenar_TablaAforadores();
        String texto = tablaAforadores_CODIGO + sep + tablaAforadores_CLAVE + sep + tablaAforadores_DESCRIPCION + sep + tablaAforadores_ESTADO + sep + tablaAforadores_CAMPO1 + sep
                + tablaAforadores_CAMPO2 + sep + tablaAforadores_CRNL;
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

    public void rellenar_TablaAforadores() {
        try {
            tablaAforadores_CODIGO = String.format("%-10s", tablaAforadores_CODIGO);
            tablaAforadores_CLAVE = String.format("%-12s", tablaAforadores_CLAVE);
            tablaAforadores_DESCRIPCION = String.format("%-30s", tablaAforadores_DESCRIPCION);
            tablaAforadores_ESTADO = String.format("%-1s", tablaAforadores_ESTADO);
            tablaAforadores_CAMPO1 = String.format("%-15s", tablaAforadores_CAMPO1);
            tablaAforadores_CAMPO2 = String.format("%-15s", tablaAforadores_CAMPO2);
            tablaAforadores_CRNL = String.format("%-2s", tablaAforadores_CRNL);
        } catch (Exception e) {
            System.out.println("Se presento problema al escribir en el archivo tablaAforadores.dat..");
            e.printStackTrace();
        }

        return;
    }

    public void posicion_TablaAforadores(int registro, int tamano) {
        try {
            rFile.seek((registro - 1) * tamano);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Escribir los registros atual en el archivo en posicion
    public void lectura_TablaAforadores(int registro) {
        //String[] campos;
        setEncontro_TablaAforadores(0);
        posicion_TablaAforadores(registro, LONGITUD_REGISTRO);
        try {
            //Ax: int fileSize = (int) rFile.length();
            byteArray = new byte[LONGITUD_REGISTRO];//Ax: _fileSize
            rFile.read(byteArray);//Ax: rFile.readFully(byteArray, 0, LONGITUD_REGISTRO);
            texto = new String(byteArray);
            settablaAforadores_CODIGO(texto.substring(0, 10));
            settablaAforadores_CLAVE(texto.substring(11, 23));
            settablaAforadores_DESCRIPCION(texto.substring(24, 54));
            settablaAforadores_ESTADO(texto.substring(55, 56));
            settablaAforadores_CAMPO1(texto.substring(57, 72));
            settablaAforadores_CAMPO2(texto.substring(73, 88));
            settablaAforadores_CRNL(texto.substring(89, 91));
            ultimo_TablaAforadores = registro;
            // fin estructura
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void buscarSecuencial_TablaAforadores(String codigo) {
        setEncontro_TablaAforadores(0);
        for (int i = 0; i < (int) (total_TablaAforadores); i++) {
            lectura_TablaAforadores(i + 1);
            long codIn = 0;
            if(tablaAforadores_CODIGO.trim().contains(" ") || tablaAforadores_CODIGO.trim().contains("-")){
                codIn = Long.parseLong(tablaAforadores_CODIGO.trim().replace(" ","").replace("-",""));
            }
            else{
                codIn = Long.parseLong(tablaAforadores_CODIGO.trim());
            }

            if (Long.parseLong(codigo.trim()) == codIn) {
                setEncontro_TablaAforadores(i + 1);
                posicion_TablaAforadores(i + 1, LONGITUD_REGISTRO);
                i = total_TablaAforadores + 10;
            } else {
                setEncontro_TablaAforadores(0);
            }
        }
        return;
    }

    public String getArchivo_TablaAforadores() {
        return archivo_TablaAforadores;
    }

    public void setArchivo_TablaAforadores(String archivo_TablaAforadores) {
        this.archivo_TablaAforadores = archivo_TablaAforadores;
    }

    public int getEncontro_TablaAforadores() {
        return encontro_TablaAforadores;
    }

    public void setEncontro_TablaAforadores(int encontro_TablaAforadores) {
        this.encontro_TablaAforadores = encontro_TablaAforadores;
    }

    // public void buscarbinario_TablaAforadores(String codigo)
    // {
    // int salir=0;
    // int i = 0;
    // int t = total_TablaAforadores;
    // int b = 0;
    // lectura_TablaAforadores(1);
    // String uno = codigo.trim();
    // String otro = .trim();
    // if (otro.equals(uno))
    // {
    // posicion_TablaAforadores(i+1,LONGITUD_REGISTRO);
    // encontro_TablaAforadores=1;
    // }
    // else
    // {
    // lectura_TablaAforadores(total_TablaAforadores);
    // otro = .trim().equals("")?"0":.trim();
    // if ((Integer.parseInt(uno)) > (Integer.parseInt(otro)))
    // {
    // encontro_TablaAforadores=0;
    // }
    // else
    // {
    // while(salir==0)
    // {
    // i = (b + t)/2;
    // lectura_TablaAforadores(i+1);
    // otro = .trim();
    // if (otro.equals(uno))
    // {
    // encontro_TablaAforadores=i;
    // posicion_TablaAforadores(i+1,LONGITUD_REGISTRO);
    // salir=1;
    // }
    // else
    // {
    // if (b == i)
    // {
    // lectura_TablaAforadores(i+2);
    // otro = .trim();
    // if (uno == otro)
    // {
    // encontro_TablaAforadores=i;
    // posicion_TablaAforadores(i+1,LONGITUD_REGISTRO);
    // }
    // else
    // {
    // encontro_TablaAforadores = 0;
    // }
    // salir=1;
    // }
    // else
    // {
    // if (Integer.parseInt(uno) > Integer.parseInt(otro))
    // {
    // b = i;
    // }
    // else
    // {
    // if (i-b==1)
    // {
    // b=i;
    // }
    // t = i;
    // }
    // }
    // }
    // }
    // }
    // }
    // }

    // public void buscarbinarioChar_TablaAforadores(String codigo)
    // {
    // int salir=0;
    // int i = 0;
    // int t = total_TablaAforadores;
    // int b = 0;
    // lectura_TablaAforadores(1);
    // String uno = codigo.toUpperCase().trim();
    // String otro = .toUpperCase().trim();
    // if (otro.compareTo(uno) == 0)
    // {
    // posicion_TablaAforadores(i+1,LONGITUD_REGISTRO);
    // encontro_TablaAforadores=1;
    // }
    // else {
    // lectura_TablaAforadores(total_TablaAforadores);
    // otro = .trim().equals("")?"0":.toUpperCase().trim();
    // if (uno.compareTo(otro) == 1 )
    // {
    // encontro_TablaAforadores = 0;
    // }
    // else
    // {
    // while(salir==0)
    // {
    // i = (b + t)/2;
    // lectura_TablaAforadores(i+1);
    // otro = .toUpperCase().trim();
    // if (uno.compareTo(otro) == 0)
    // {
    // encontro_TablaAforadores = i;
    // posicion_TablaAforadores(i+1,LONGITUD_REGISTRO);
    // salir=1;
    // }
    // else
    // {
    // if (b == i)
    // {
    // lectura_TablaAforadores(i+2);
    // otro = .toUpperCase().trim();
    // if (uno.compareTo(otro) == 0)
    // {
    // encontro_TablaAforadores=i;
    // posicion_TablaAforadores(i+1,LONGITUD_REGISTRO);
    // }
    // else
    // {
    // encontro_TablaAforadores=0;
    // }
    // salir=1;
    // }
    // else
    // {
    // if (uno.compareTo(otro) == 1)
    // {
    // b = i;
    // }
    // else
    // {
    // if (i-b==1)
    // {
    // b=i;
    // }
    // t = i;
    // }
    // }
    // }
    // }
    // }
    // }
    // }
}
