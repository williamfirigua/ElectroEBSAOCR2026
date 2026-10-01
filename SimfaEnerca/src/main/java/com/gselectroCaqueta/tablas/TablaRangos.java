package com.gselectroCaqueta.tablas;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
/// <summary>
/// Descripcion breve del archivo a generar...tablaRangos
/// </summary>

public class TablaRangos { //Ax no la encontre
    static final int LONGITUD_REGISTRO = 38; //38
    String sep = ";";
    String tablaRangos_CODRANGO;
    String tablaRangos_RANGOINICIAL;
    String tablaRangos_RANGOFINAL;
    String tablaRangos_LEVE;
    String tablaRangos_MEDIO;
    String tablaRangos_ALTO;
    String tablaRangos_CLASE_SERVICIO;
    String tablaRangos_CRNL;
    BufferedReader fin;
    byte[] byteArray;
    int total_TablaRangos;
    int ultimo_TablaRangos;
    String buscar_TablaRangos;
    String texto;

    RandomAccessFile rFile;
    private String archivo_TablaRangos;
    private int encontro_TablaRangos;

    public String gettablaRangos_CODRANGO() {
        return tablaRangos_CODRANGO;
    }

    public void settablaRangos_CODRANGO(String tablaRangos_CODRANGO) {
        this.tablaRangos_CODRANGO = tablaRangos_CODRANGO;
    }

    public String gettablaRangos_RANGOINICIAL() {
        return tablaRangos_RANGOINICIAL;
    }

    public void settablaRangos_RANGOINICIAL(String tablaRangos_RANGOINICIAL) {
        this.tablaRangos_RANGOINICIAL = tablaRangos_RANGOINICIAL;
    }

    public String gettablaRangos_RANGOFINAL() {
        return tablaRangos_RANGOFINAL;
    }

    public void settablaRangos_RANGOFINAL(String tablaRangos_RANGOFINAL) {
        this.tablaRangos_RANGOFINAL = tablaRangos_RANGOFINAL;
    }

    public String gettablaRangos_LEVE() {
        return tablaRangos_LEVE;
    }

    public void settablaRangos_LEVE(String tablaRangos_LEVE) {
        this.tablaRangos_LEVE = tablaRangos_LEVE;
    }

    public String gettablaRangos_MEDIO() {
        return tablaRangos_MEDIO;
    }

    public void settablaRangos_MEDIO(String tablaRangos_MEDIO) {
        this.tablaRangos_MEDIO = tablaRangos_MEDIO;
    }

    public String gettablaRangos_ALTO() {
        return tablaRangos_ALTO;
    }

    public void settablaRangos_ALTO(String tablaRangos_ALTO) {
        this.tablaRangos_ALTO = tablaRangos_ALTO;
    }

    public String gettablaRangos_CLASE_SERVICIO() {
        return tablaRangos_CLASE_SERVICIO;
    }

    public void settablaRangos_CLASE_SERVICIO(String tablaRangos_CLASE_SERVICIO) {
        this.tablaRangos_CLASE_SERVICIO = tablaRangos_CLASE_SERVICIO;
    }

    public String gettablaRangos_CRNL() {
        return tablaRangos_CRNL;
    }

    public void settablaRangos_CRNL(String tablaRangos_CRNL) {
        this.tablaRangos_CRNL = tablaRangos_CRNL;
    }

    public Boolean abrir_TablaRangos(String nombreArchivo) {
        if (nombreArchivo.length() == 0) {
            return false;
        }
        try {
            rFile = new RandomAccessFile(nombreArchivo, "rw"); // C:/Users/SGlobal/Documents/Pruebas Java/CensoSalida.txt
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        setArchivo_TablaRangos(nombreArchivo);
        return abrir(getArchivo_TablaRangos());
    }

    private Boolean abrir(String nombre_archivo) {
        try {
            int fileSize = (int) rFile.length();
            byteArray = new byte[fileSize];
            rFile.readFully(byteArray, 0, fileSize);
            texto = new String(byteArray);
            total_TablaRangos = fileSize / LONGITUD_REGISTRO;
            return true;
        } catch (Exception ex) {
            // Log.e('Ficheros', 'Error al leer fichero desde tarjeta SD');
            ex.printStackTrace();
            return false;
        }
    }

    public void Cerrar_TablaRangos() {
        // cerrar el archivo abierto
        try {
            rFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean escribir_TablaRangos(int posicion) {
        posicion_TablaRangos(posicion, LONGITUD_REGISTRO);
        rellenar_TablaRangos();
        String texto = tablaRangos_CODRANGO + sep + tablaRangos_RANGOINICIAL + sep + tablaRangos_RANGOFINAL + sep + tablaRangos_LEVE + sep + tablaRangos_MEDIO + sep + tablaRangos_ALTO + sep
                + tablaRangos_CLASE_SERVICIO + sep + tablaRangos_CRNL;
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

    public void rellenar_TablaRangos() {
        try {
            tablaRangos_CODRANGO = String.format("%-2s", tablaRangos_CODRANGO);
            tablaRangos_RANGOINICIAL = String.format("%-8s", tablaRangos_RANGOINICIAL);
            tablaRangos_RANGOFINAL = String.format("%-8s", tablaRangos_RANGOFINAL);
            tablaRangos_LEVE = String.format("%-3s", tablaRangos_LEVE);
            tablaRangos_MEDIO = String.format("%-3s", tablaRangos_MEDIO);
            tablaRangos_ALTO = String.format("%-3s", tablaRangos_ALTO);
            tablaRangos_CLASE_SERVICIO = String.format("%-2s", tablaRangos_CLASE_SERVICIO);
            tablaRangos_CRNL = String.format("%-2s", tablaRangos_CRNL);
        } catch (Exception e) {
            System.out.println("Se presento problema al escribir en el archivo tablaRangos.dat..");
            e.printStackTrace();
        }

        return;
    }

    public void posicion_TablaRangos(int registro, int tamano) {
        try {
            rFile.seek((registro - 1) * tamano);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Escribir los registros atual en el archivo en posicion
    public void lectura_TablaRangos(int registro) {
        String[] campos;
        setEncontro_TablaRangos(0);
        posicion_TablaRangos(registro, LONGITUD_REGISTRO);
        try {
            int fileSize = (int) rFile.length();
            byteArray = new byte[fileSize];
            rFile.readFully(byteArray, 0, LONGITUD_REGISTRO);
            texto = new String(byteArray);
            settablaRangos_CODRANGO(texto.substring(0, 2));
            settablaRangos_RANGOINICIAL(texto.substring(3, 11));
            settablaRangos_RANGOFINAL(texto.substring(12, 20));
            settablaRangos_LEVE(texto.substring(21, 24));
            settablaRangos_MEDIO(texto.substring(25, 28));
            settablaRangos_ALTO(texto.substring(29, 32));
            settablaRangos_CLASE_SERVICIO(texto.substring(33, 35));
            settablaRangos_CRNL(texto.substring(36, 38));
            ultimo_TablaRangos = registro;
            // fin estructura
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void buscarSecuencial_TablaRangos(String codigo) {
        setEncontro_TablaRangos(0);
        for (int i = 0; i < (int) (total_TablaRangos); i++) {
            lectura_TablaRangos(i + 1);
            if (Double.parseDouble(codigo.trim()) >= Double.parseDouble(tablaRangos_RANGOINICIAL.trim()) && Double.parseDouble(codigo.trim()) <= Double.parseDouble(tablaRangos_RANGOFINAL.trim())) {
                setEncontro_TablaRangos(i + 1);
                posicion_TablaRangos(i + 1, LONGITUD_REGISTRO);
                i = total_TablaRangos + 10;
            } else {
                setEncontro_TablaRangos(0);
            }
        }
        return;
    }

    public void buscarbinario_TablaRangos(String codigo) {
        int salir = 0;
        int i = 0;
        int t = total_TablaRangos;
        int b = 0;
        lectura_TablaRangos(1);
        String uno = codigo.trim();
        String otro = tablaRangos_RANGOINICIAL.trim();
        if (otro.equals(uno)) {
            posicion_TablaRangos(i + 1, LONGITUD_REGISTRO);
            setEncontro_TablaRangos(1);
        } else {
            lectura_TablaRangos(total_TablaRangos);
            otro = tablaRangos_RANGOINICIAL.trim().equals("") ? "0" : tablaRangos_RANGOINICIAL.trim();
            if ((Integer.parseInt(uno)) > (Integer.parseInt(otro))) {
                setEncontro_TablaRangos(0);
            } else {
                while (salir == 0) {
                    i = (b + t) / 2;
                    lectura_TablaRangos(i + 1);
                    otro = tablaRangos_RANGOINICIAL.trim();
                    if (otro.equals(uno)) {
                        setEncontro_TablaRangos(i);
                        posicion_TablaRangos(i + 1, LONGITUD_REGISTRO);
                        salir = 1;
                    } else {
                        if (b == i) {
                            lectura_TablaRangos(i + 2);
                            otro = tablaRangos_RANGOINICIAL.trim();
                            if (uno == otro) {
                                setEncontro_TablaRangos(i);
                                posicion_TablaRangos(i + 1, LONGITUD_REGISTRO);
                            } else {
                                setEncontro_TablaRangos(0);
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

    public String getArchivo_TablaRangos() {
        return archivo_TablaRangos;
    }

    public void setArchivo_TablaRangos(String archivo_TablaRangos) {
        this.archivo_TablaRangos = archivo_TablaRangos;
    }

    public int getEncontro_TablaRangos() {
        return encontro_TablaRangos;
    }

    public void setEncontro_TablaRangos(int encontro_TablaRangos) {
        this.encontro_TablaRangos = encontro_TablaRangos;
    }

}
