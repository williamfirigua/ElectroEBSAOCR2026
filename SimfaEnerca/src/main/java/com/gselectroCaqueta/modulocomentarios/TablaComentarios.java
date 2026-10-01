package com.gselectroCaqueta.modulocomentarios;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
/// <summary>
/// Descripcion breve del archivo a generar...tablaComentarios
/// </summary>

public class TablaComentarios { //Ax Ya

    static final int LONGITUD_REGISTRO = 50;
    String sep = ";";
    String tablaComentarios_CODIGO;
    String tablaComentarios_CODIGO2;
    String tablaComentarios_DESCRIPCION;
    String tablaComentarios_IMPRESIONENLAFACTURA;
    String tablaComentarios_CRNL;
    BufferedReader fin;
    byte[] byteArray;
    String archivo_TablaComentarios;
    int total_TablaComentarios;
    int ultimo_TablaComentarios;
    int encontro_TablaComentarios;
    String buscar_TablaComentarios;
    String texto;

    RandomAccessFile rFile;

    public String gettablaComentarios_CODIGO() {
        return tablaComentarios_CODIGO;
    }

    public void settablaComentarios_CODIGO(String tablaComentarios_CODIGO) {
        this.tablaComentarios_CODIGO = tablaComentarios_CODIGO;
    }

    public String gettablaComentarios_CODIGO2() {
        return tablaComentarios_CODIGO2;
    }

    public void settablaComentarios_CODIGO2(String tablaComentarios_CODIGO2) {
        this.tablaComentarios_CODIGO2 = tablaComentarios_CODIGO2;
    }

    public String gettablaComentarios_DESCRIPCION() {
        return tablaComentarios_DESCRIPCION;
    }

    public void settablaComentarios_DESCRIPCION(String tablaComentarios_DESCRIPCION) {
        this.tablaComentarios_DESCRIPCION = tablaComentarios_DESCRIPCION;
    }

    public String gettablaComentarios_IMPRESIONENLAFACTURA() {
        return tablaComentarios_IMPRESIONENLAFACTURA;
    }

    public void settablaComentarios_IMPRESIONENLAFACTURA(String tablaComentarios_IMPRESIONENLAFACTURA) {
        this.tablaComentarios_IMPRESIONENLAFACTURA = tablaComentarios_IMPRESIONENLAFACTURA;
    }

    public String gettablaComentarios_CRNL() {
        return tablaComentarios_CRNL;
    }

    public void settablaComentarios_CRNL(String tablaComentarios_CRNL) {
        this.tablaComentarios_CRNL = tablaComentarios_CRNL;
    }

    public String getArchivo_TablaComentarios() {
        return archivo_TablaComentarios;
    }

    public void setArchivo_TablaComentarios(String archivo_TablaComentarios) {
        this.archivo_TablaComentarios = archivo_TablaComentarios;
    }

    public int getTotal_TablaComentarios() {
        return total_TablaComentarios;
    }

    public void setTotal_TablaComentarios(int total_TablaComentarios) {
        this.total_TablaComentarios = total_TablaComentarios;
    }

    public Boolean abrir_TablaComentarios(String nombreArchivo) {
        if (nombreArchivo.length() == 0) {
            return false;
        }
        try {
            rFile = new RandomAccessFile(nombreArchivo, "rw"); // C:/Users/SGlobal/Documents/Pruebas Java/CensoSalida.txt
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        archivo_TablaComentarios = nombreArchivo;
        return abrir(archivo_TablaComentarios);
    }

    private Boolean abrir(String nombre_archivo) {
        try {
            int fileSize = (int) rFile.length();
            byteArray = new byte[fileSize];
            rFile.readFully(byteArray, 0, fileSize);
            texto = new String(byteArray);
            total_TablaComentarios = fileSize / LONGITUD_REGISTRO;
            return true;
        } catch (Exception ex) {
            // Log.e('Ficheros', 'Error al leer fichero desde tarjeta SD');
            ex.printStackTrace();
            return false;
        }
    }

    public void Cerrar_TablaComentarios() {
        // cerrar el archivo abierto
        try {
            rFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean escribir_TablaComentarios(int posicion) {
        posicion_TablaComentarios(posicion, LONGITUD_REGISTRO);
        rellenar_TablaComentarios();
        String texto = tablaComentarios_CODIGO + sep + tablaComentarios_CODIGO2 + sep + tablaComentarios_DESCRIPCION + sep + tablaComentarios_IMPRESIONENLAFACTURA + sep + tablaComentarios_CRNL;
        try {
            if (null != rFile && (texto.length() == LONGITUD_REGISTRO)) {
                rFile.writeBytes(texto);
                System.out.println("Los Datos fueron grabados correctamente");
                return true;
            } else {
                System.out.println("Se presento problema al escribir en el archivo; longitud Errada: tamano texto: " + texto.length());
                return false;
            }
        } catch (IOException ioe) {
            System.out.println("Se presento problema al escribir en el archivo Longitud Errada: " + LONGITUD_REGISTRO);
            return false;
        }
    }

    public void rellenar_TablaComentarios() {
        try {
            tablaComentarios_CODIGO = String.format("%-2s", tablaComentarios_CODIGO);
            tablaComentarios_CODIGO2 = String.format("%-4s", tablaComentarios_CODIGO2);
            tablaComentarios_DESCRIPCION = String.format("%-45s", tablaComentarios_DESCRIPCION);
            tablaComentarios_IMPRESIONENLAFACTURA = String.format("%-1s", tablaComentarios_IMPRESIONENLAFACTURA);
            tablaComentarios_CRNL = String.format("%-2s", tablaComentarios_CRNL);
        } catch (Exception e) {
            System.out.println("Se presento problema al escribir en el archivo tablaComentarios.dat..");
            e.printStackTrace();
        }

        return;
    }

    public void posicion_TablaComentarios(int registro, int tamano) {
        try {
            rFile.seek((registro - 1) * tamano);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Escribir los registros atual en el archivo en posicion
    public void lectura_TablaComentarios(int registro) {
        String[] campos;
        encontro_TablaComentarios = 0;
        posicion_TablaComentarios(registro, LONGITUD_REGISTRO);
        try {
            int fileSize = (int) rFile.length();
            byteArray = new byte[fileSize];
            rFile.readFully(byteArray, 0, LONGITUD_REGISTRO);
            texto = new String(byteArray);
            settablaComentarios_CODIGO(texto.substring(0, 2));
            settablaComentarios_CODIGO2(texto.substring(3, 4));
            settablaComentarios_DESCRIPCION(texto.substring(5, 45));
            settablaComentarios_IMPRESIONENLAFACTURA(texto.substring(46, 47));
            settablaComentarios_CRNL(texto.substring(48, 50));
            ultimo_TablaComentarios = registro;
            // fin estructura
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void buscarSecuencial_TablaComentarios(String codigo) {
        encontro_TablaComentarios = 0;
        for (int i = 0; i < (int) (total_TablaComentarios); i++) {
            lectura_TablaComentarios(i + 1);
            if (Integer.parseInt(codigo.trim()) == Integer.parseInt(tablaComentarios_CODIGO.trim())) {
                encontro_TablaComentarios = i + 1;
                posicion_TablaComentarios(i + 1, LONGITUD_REGISTRO);
                i = total_TablaComentarios + 10;
            } else {
                encontro_TablaComentarios = 0;
            }
        }
        return;
    }

    public void buscarbinario_TablaComentarios(String codigo) {
        int salir = 0;
        int i = 0;
        int t = total_TablaComentarios;
        int b = 0;
        lectura_TablaComentarios(1);
        String uno = codigo.trim();
        String otro = tablaComentarios_CODIGO.trim();
        if (otro.equals(uno)) {
            posicion_TablaComentarios(i + 1, LONGITUD_REGISTRO);
            encontro_TablaComentarios = 1;
        } else {
            lectura_TablaComentarios(total_TablaComentarios);
            otro = tablaComentarios_CODIGO.trim().equals("") ? "0" : tablaComentarios_CODIGO.trim();
            if ((Integer.parseInt(uno)) > (Integer.parseInt(otro))) {
                encontro_TablaComentarios = 0;
            } else {
                while (salir == 0) {
                    i = (b + t) / 2;
                    lectura_TablaComentarios(i + 1);
                    otro = tablaComentarios_CODIGO.trim();
                    if (otro.equals(uno)) {
                        encontro_TablaComentarios = i;
                        posicion_TablaComentarios(i + 1, LONGITUD_REGISTRO);
                        salir = 1;
                    } else {
                        if (b == i) {
                            lectura_TablaComentarios(i + 2);
                            otro = tablaComentarios_CODIGO.trim();
                            if (uno == otro) {
                                encontro_TablaComentarios = i;
                                posicion_TablaComentarios(i + 1, LONGITUD_REGISTRO);
                            } else {
                                encontro_TablaComentarios = 0;
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
}
