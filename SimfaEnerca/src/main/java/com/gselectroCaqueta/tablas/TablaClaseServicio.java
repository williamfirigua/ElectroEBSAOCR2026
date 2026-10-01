package com.gselectroCaqueta.tablas;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
/// <summary>
/// Descripcion breve del archivo a generar...tablaClaseServicio
/// </summary>

public class TablaClaseServicio { //Ax ya, es TABLACLASESERVICIO
    static final int LONGITUD_REGISTRO = 103;
    String sep = ";";
    String tablaClaseServicio_CODIGO;
    String tablaClaseServicio_DESCRIPCION;
    String tablaClaseServicio_CRNL;

    BufferedReader fin;
    byte[] byteArray;
    int total_TablaClaseServicio;
    int ultimo_TablaClaseServicio;
    int encontro_TablaClaseServicio;
    String buscar_TablaClaseServicio;
    String texto;
    // File ruta_sd = Environment.getExternalStorageDirectory();
    // File ruta_sd = new File('C:\\Users\\SGlobal\\Documents\\PruebasJava\\CensoSalida.txt');
    RandomAccessFile rFile;
    private String archivo_TablaClaseServicio;

    public String gettablaClaseServicio_CODIGO() {
        return tablaClaseServicio_CODIGO;
    }

    public void settablaClaseServicio_CODIGO(String tablaClaseServicio_CODIGO) {
        this.tablaClaseServicio_CODIGO = tablaClaseServicio_CODIGO;
    }

    public String gettablaClaseServicio_DESCRIPCION() {
        return tablaClaseServicio_DESCRIPCION;
    }

    public void settablaClaseServicio_DESCRIPCION(String tablaClaseServicio_DESCRIPCION) {
        this.tablaClaseServicio_DESCRIPCION = tablaClaseServicio_DESCRIPCION;
    }

    public String gettablaClaseServicio_CRNL() {
        return tablaClaseServicio_CRNL;
    }

    public void settablaClaseServicio_CRNL(String tablaClaseServicio_CRNL) {
        this.tablaClaseServicio_CRNL = tablaClaseServicio_CRNL;
    }

    public Boolean abrir_TablaClaseServicio(String nombreArchivo) {
        if (nombreArchivo.length() == 0) {
            return false;
        }
        try {
            rFile = new RandomAccessFile(nombreArchivo, "rw"); // C:/Users/SGlobal/Documents/Pruebas Java/CensoSalida.txt
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        setArchivo_TablaClaseServicio(nombreArchivo);
        return abrir(getArchivo_TablaClaseServicio());
    }

    private Boolean abrir(String nombre_archivo) {
        try {
            int fileSize = (int) rFile.length();
            byteArray = new byte[fileSize];
            rFile.readFully(byteArray, 0, fileSize);
            texto = new String(byteArray);
            total_TablaClaseServicio = fileSize / LONGITUD_REGISTRO;
            return true;
        } catch (Exception ex) {
            // Log.e('Ficheros', 'Error al leer fichero desde tarjeta SD');
            ex.printStackTrace();
            return false;
        }
    }

    public void Cerrar_TablaClaseServicio() {
        // cerrar el archivo abierto
        try {
            rFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean escribir_TablaClaseServicio(int posicion) {
        posicion_TablaClaseServicio(posicion, LONGITUD_REGISTRO);
        rellenar_TablaClaseServicio();
        String texto = tablaClaseServicio_CODIGO + sep + tablaClaseServicio_DESCRIPCION + sep + tablaClaseServicio_CRNL;
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

    public void rellenar_TablaClaseServicio() {
        try {
            tablaClaseServicio_CODIGO = String.format("%-2s", tablaClaseServicio_CODIGO);
            tablaClaseServicio_DESCRIPCION = String.format("%-97s", tablaClaseServicio_DESCRIPCION);
            tablaClaseServicio_CRNL = String.format("%-2s", tablaClaseServicio_CRNL);
        } catch (Exception e) {
            System.out.println("Se presento problema al escribir en el archivo tablaClaseServicio.dat..");
            e.printStackTrace();
        }

        return;
    }

    public void posicion_TablaClaseServicio(int registro, int tamano) {
        try {
            rFile.seek((registro - 1) * tamano);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Escribir los registros atual en el archivo en posicion
    public void lectura_TablaClaseServicio(int registro) {
        String[] campos;
        encontro_TablaClaseServicio = 0;
        posicion_TablaClaseServicio(registro, LONGITUD_REGISTRO);
        try {
            int fileSize = (int) rFile.length();
            byteArray = new byte[fileSize];
            rFile.readFully(byteArray, 0, LONGITUD_REGISTRO);
            texto = new String(byteArray);
            settablaClaseServicio_CODIGO(texto.substring(0, 2));
            settablaClaseServicio_DESCRIPCION(texto.substring(3, 100));
            settablaClaseServicio_CRNL(texto.substring(101, 103));
            ultimo_TablaClaseServicio = registro;
            // fin estructura
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void buscarSecuencial_TablaClaseServicio(String codigo) {
        encontro_TablaClaseServicio = 0;
        for (int i = 0; i < (int) (total_TablaClaseServicio); i++) {
            lectura_TablaClaseServicio(i + 1);
            if (codigo.trim().equals(tablaClaseServicio_CODIGO.trim())) {
                encontro_TablaClaseServicio = i + 1;
                posicion_TablaClaseServicio(i + 1, LONGITUD_REGISTRO);
                i = total_TablaClaseServicio + 10;
            } else {
                encontro_TablaClaseServicio = 0;
            }
        }
        return;
    }

    public void buscarbinario_TablaClaseServicio(String codigo) {
        int salir = 0;
        int i = 0;
        int t = total_TablaClaseServicio;
        int b = 0;
        lectura_TablaClaseServicio(1);
        String uno = codigo.trim();
        String otro = tablaClaseServicio_CODIGO.trim();
        if (otro.equals(uno)) {
            posicion_TablaClaseServicio(i + 1, LONGITUD_REGISTRO);
            encontro_TablaClaseServicio = 1;
        } else {
            lectura_TablaClaseServicio(total_TablaClaseServicio);
            otro = tablaClaseServicio_CODIGO.trim().equals("") ? "0" : tablaClaseServicio_CODIGO.trim();
            if ((Integer.parseInt(uno)) > (Integer.parseInt(otro))) {
                encontro_TablaClaseServicio = 0;
            } else {
                while (salir == 0) {
                    i = (b + t) / 2;
                    lectura_TablaClaseServicio(i + 1);
                    otro = tablaClaseServicio_CODIGO.trim();
                    if (otro.equals(uno)) {
                        encontro_TablaClaseServicio = i;
                        posicion_TablaClaseServicio(i + 1, LONGITUD_REGISTRO);
                        salir = 1;
                    } else {
                        if (b == i) {
                            lectura_TablaClaseServicio(i + 2);
                            otro = tablaClaseServicio_CODIGO.trim();
                            if (uno == otro) {
                                encontro_TablaClaseServicio = i;
                                posicion_TablaClaseServicio(i + 1, LONGITUD_REGISTRO);
                            } else {
                                encontro_TablaClaseServicio = 0;
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

    public String getArchivo_TablaClaseServicio() {
        return archivo_TablaClaseServicio;
    }

    public void setArchivo_TablaClaseServicio(String archivo_TablaClaseServicio) {
        this.archivo_TablaClaseServicio = archivo_TablaClaseServicio;
    }
}
