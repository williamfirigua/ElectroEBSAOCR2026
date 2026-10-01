package com.gselectroCaqueta.tablas;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
/// <summary>
/// Descripcion breve del archivo a generar...tablaFestivos
/// </summary>

public class TablaFestivos {//Ax ya, es TABLAFESTIVOS
    static final int LONGITUD_REGISTRO = 11;
    String sep = ";";
    String tablaFestivos_codigo;
    String tablaFestivos_crnl;
    BufferedReader fin;
    byte[] byteArray;
    int total_TablaFestivos;
    int ultimo_TablaFestivos;
    String buscar_TablaFestivos;
    String texto;
    // File ruta_sd = Environment.getExternalStorageDirectory();
    // File ruta_sd = new File('C:\\Users\\SGlobal\\Documents\\PruebasJava\\CensoSalida.txt');
    RandomAccessFile rFile;
    private String archivo_TablaFestivos;
    private int encontro_TablaFestivos;

    public String gettablaFestivos_CODIGO() {
        return tablaFestivos_codigo;
    }

    public void settablaFestivos_codigo(String tablaFestivos_codigo) {
        this.tablaFestivos_codigo = tablaFestivos_codigo;
    }

    public String gettablaFestivos_CRNL() {
        return tablaFestivos_crnl;
    }

    public void settablaFestivos_crnl(String tablaFestivos_crnl) {
        this.tablaFestivos_crnl = tablaFestivos_crnl;
    }

    public Boolean abrir_TablaFestivos(String nombreArchivo) {
        if (nombreArchivo.length() == 0) {
            return false;
        }
        try {
            rFile = new RandomAccessFile(nombreArchivo, "rw"); // C:/Users/SGlobal/Documents/Pruebas Java/CensoSalida.txt
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        setArchivo_TablaFestivos(nombreArchivo);
        return abrir(getArchivo_TablaFestivos());
    }

    private Boolean abrir(String nombre_archivo) {
        try {
            int fileSize = (int) rFile.length();
            byteArray = new byte[fileSize];
            rFile.readFully(byteArray, 0, fileSize);
            texto = new String(byteArray);
            total_TablaFestivos = fileSize / LONGITUD_REGISTRO;
            return true;
        } catch (Exception ex) {
            // Log.e('Ficheros', 'Error al leer fichero desde tarjeta SD');
            ex.printStackTrace();
            return false;
        }
    }

    public void Cerrar_TablaFestivos() {
        // cerrar el archivo abierto
        try {
            rFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean escribir_TablaFestivos(int posicion) {
        posicion_TablaFestivos(posicion, LONGITUD_REGISTRO);
        rellenar_TablaFestivos();
        String texto = tablaFestivos_codigo + sep + tablaFestivos_crnl;
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

    public void rellenar_TablaFestivos() {
        try {
            tablaFestivos_codigo = String.format("%-8s", tablaFestivos_codigo);
            tablaFestivos_crnl = String.format("%-2s", tablaFestivos_crnl);
        } catch (Exception e) {
            System.out.println("Se presento problema al escribir en el archivo tablaFestivos.dat..");
            e.printStackTrace();
        }

        return;
    }

    public void posicion_TablaFestivos(int registro, int tamano) {
        try {
            rFile.seek((registro - 1) * tamano);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Escribir los registros actual en el archivo en posicion
    public void lectura_TablaFestivos(int registro) {
        String[] campos;
        setEncontro_TablaFestivos(0);
        posicion_TablaFestivos(registro, LONGITUD_REGISTRO);
        try {
            int fileSize = (int) rFile.length();
            byteArray = new byte[fileSize];
            rFile.readFully(byteArray, 0, LONGITUD_REGISTRO);
            texto = new String(byteArray);
            settablaFestivos_codigo(texto.substring(0, 8));
            settablaFestivos_crnl(texto.substring(9, 11));
            ultimo_TablaFestivos = registro;
            // fin estructura
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void buscarSecuencial_TablaFestivos(String codigo) {
        setEncontro_TablaFestivos(0);
        for (int i = 0; i < (int) (total_TablaFestivos); i++) {
            lectura_TablaFestivos(i + 1);
            if (Integer.parseInt(codigo.trim()) == Integer.parseInt(tablaFestivos_codigo.trim())) {
                setEncontro_TablaFestivos(i + 1);
                posicion_TablaFestivos(i + 1, LONGITUD_REGISTRO);
                i = total_TablaFestivos + 10;
            } else {
                setEncontro_TablaFestivos(0);
            }
        }
        return;
    }

    public void buscarbinario_TablaFestivos(String codigo) {
        int salir = 0;
        int i = 0;
        int t = total_TablaFestivos;
        int b = 0;
        lectura_TablaFestivos(1);
        String uno = codigo.trim();
        String otro = tablaFestivos_codigo.trim();
        if (otro.equals(uno)) {
            posicion_TablaFestivos(i + 1, LONGITUD_REGISTRO);
            setEncontro_TablaFestivos(1);
        } else {
            lectura_TablaFestivos(total_TablaFestivos);
            otro = tablaFestivos_codigo.trim().equals("") ? "0" : tablaFestivos_codigo.trim();
            if ((Integer.parseInt(uno)) > (Integer.parseInt(otro))) {
                setEncontro_TablaFestivos(0);
            } else {
                while (salir == 0) {
                    i = (b + t) / 2;
                    lectura_TablaFestivos(i + 1);
                    if(tablaFestivos_codigo.trim().equals("")){
                        tablaFestivos_codigo = "00000000";
                    }
                    otro = tablaFestivos_codigo.trim();
                    if (otro.equals(uno)) {
                        setEncontro_TablaFestivos(i);
                        posicion_TablaFestivos(i + 1, LONGITUD_REGISTRO);
                        salir = 1;
                    } else {
                        if (b == i) {
                            lectura_TablaFestivos(i + 2);
                            otro = tablaFestivos_codigo.trim();
                            if (uno == otro) {
                                setEncontro_TablaFestivos(i);
                                posicion_TablaFestivos(i + 1, LONGITUD_REGISTRO);
                            } else {
                                setEncontro_TablaFestivos(0);
                            }
                            salir = 1;
                        } else {
                            if (Integer.parseInt(uno) > Integer.parseInt(otro)) {
                                b = i;
                            } else {
                                if (i - b == 1) {
                                    b = i;
                                }
                                t = i;
                            }
                        }
                    }
                }
            }
        }
    }

    public String getArchivo_TablaFestivos() {
        return archivo_TablaFestivos;
    }

    public void setArchivo_TablaFestivos(String archivo_TablaFestivos) {
        this.archivo_TablaFestivos = archivo_TablaFestivos;
    }

    public int getEncontro_TablaFestivos() {
        return encontro_TablaFestivos;
    }

    public void setEncontro_TablaFestivos(int encontro_TablaFestivos) {
        this.encontro_TablaFestivos = encontro_TablaFestivos;
    }
}
