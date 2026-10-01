package com.gselectroCaqueta.tablas;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
/// <summary>
/// Descripcion breve del archivo a generar...tablaCertificacion
/// </summary>

public class TablaCertificacion {
    static final int LONGITUD_REGISTRO = 239;
    String sep = ";";
    String tablaCertificacion_ciclo;
    String tablaCertificacion_anno;
    String tablaCertificacion_mes;
    String tablaCertificacion_cuenta;
    String tablaCertificacion_nombreusuario;
    String tablaCertificacion_direccion;
    String tablaCertificacion_fecha;
    String tablaCertificacion_hora;
    String tablaCertificacion_telefono;
    String tablaCertificacion_recibe;
    String tablaCertificacion_lector;
    String tablaCertificacion_cedula;
    String tablaCertificacion_enviado;
    String tablaCertificacion_CRNL;
    BufferedReader fin;
    byte[] byteArray;
    int ultimo_TablaCertificacion;
    int encontro_TablaCertificacion;
    String buscar_TablaCertificacion;
    String texto;
    // File ruta_sd = Environment.getExternalStorageDirectory();
    // File ruta_sd = new File('C:\\Users\\SGlobal\\Documents\\PruebasJava\\CensoSalida.txt');
    RandomAccessFile rFile;
    private String archivo_TablaCertificacion;
    private int total_TablaCertificacion;

    public String gettablaCertificacion_CICLO() {
        return tablaCertificacion_ciclo;
    }

    public void settablaCertificacion_ciclo(String tablaCertificacion_ciclo) {
        this.tablaCertificacion_ciclo = tablaCertificacion_ciclo;
    }

    public String gettablaCertificacion_ANNO() {
        return tablaCertificacion_anno;
    }

    public void settablaCertificacion_anno(String tablaCertificacion_anno) {
        this.tablaCertificacion_anno = tablaCertificacion_anno;
    }

    public String gettablaCertificacion_MES() {
        return tablaCertificacion_mes;
    }

    public void settablaCertificacion_mes(String tablaCertificacion_mes) {
        this.tablaCertificacion_mes = tablaCertificacion_mes;
    }

    public String gettablaCertificacion_CUENTA() {
        return tablaCertificacion_cuenta;
    }

    public void settablaCertificacion_cuenta(String tablaCertificacion_cuenta) {
        this.tablaCertificacion_cuenta = tablaCertificacion_cuenta;
    }

    public String gettablaCertificacion_NOMBREUSUARIO() {
        return tablaCertificacion_nombreusuario;
    }

    public void settablaCertificacion_nombreusuario(String tablaCertificacion_nombreusuario) {
        this.tablaCertificacion_nombreusuario = tablaCertificacion_nombreusuario;
    }

    public String gettablaCertificacion_DIRECCION() {
        return tablaCertificacion_direccion;
    }

    public void settablaCertificacion_direccion(String tablaCertificacion_direccion) {
        this.tablaCertificacion_direccion = tablaCertificacion_direccion;
    }

    public String gettablaCertificacion_FECHA() {
        return tablaCertificacion_fecha;
    }

    public void settablaCertificacion_fecha(String tablaCertificacion_fecha) {
        this.tablaCertificacion_fecha = tablaCertificacion_fecha;
    }

    public String gettablaCertificacion_HORA() {
        return tablaCertificacion_hora;
    }

    public void settablaCertificacion_hora(String tablaCertificacion_hora) {
        this.tablaCertificacion_hora = tablaCertificacion_hora;
    }

    public String gettablaCertificacion_TELEFONO() {
        return tablaCertificacion_telefono;
    }

    public void settablaCertificacion_telefono(String tablaCertificacion_telefono) {
        this.tablaCertificacion_telefono = tablaCertificacion_telefono;
    }

    public String gettablaCertificacion_RECIBE() {
        return tablaCertificacion_recibe;
    }

    public void settablaCertificacion_recibe(String tablaCertificacion_recibe) {
        this.tablaCertificacion_recibe = tablaCertificacion_recibe;
    }

    public String gettablaCertificacion_LECTOR() {
        return tablaCertificacion_lector;
    }

    public void settablaCertificacion_lector(String tablaCertificacion_lector) {
        this.tablaCertificacion_lector = tablaCertificacion_lector;
    }

    public String gettablaCertificacion_CEDULA() {
        return tablaCertificacion_cedula;
    }

    public void settablaCertificacion_cedula(String tablaCertificacion_cedula) {
        this.tablaCertificacion_cedula = tablaCertificacion_cedula;
    }

    public String gettablaCertificacion_ENVIADO() {
        return tablaCertificacion_enviado;
    }

    public void settablaCertificacion_enviado(String tablaCertificacion_enviado) {
        this.tablaCertificacion_enviado = tablaCertificacion_enviado;
    }

    public String gettablaCertificacion_CRNL() {
        return tablaCertificacion_CRNL;
    }

    public void settablaCertificacion_CRNL(String tablaCertificacion_CRNL) {
        this.tablaCertificacion_CRNL = tablaCertificacion_CRNL;
    }

    public Boolean abrir_TablaCertificacion(String nombreArchivo) {
        if (nombreArchivo.length() == 0) {
            return false;
        }
        try {
            rFile = new RandomAccessFile(nombreArchivo, "rw"); // C:/Users/SGlobal/Documents/Pruebas Java/CensoSalida.txt
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        setArchivo_TablaCertificacion(nombreArchivo);
        return abrir(getArchivo_TablaCertificacion());
    }

    private Boolean abrir(String nombre_archivo) {
        try {
            int fileSize = (int) rFile.length();
            byteArray = new byte[fileSize];
            rFile.readFully(byteArray, 0, fileSize);
            texto = new String(byteArray);
            setTotal_TablaCertificacion(fileSize / LONGITUD_REGISTRO);
            return true;
        } catch (Exception ex) {
            // Log.e('Ficheros', 'Error al leer fichero desde tarjeta SD');
            ex.printStackTrace();
            return false;
        }
    }

    public void Cerrar_TablaCertificacion() {
        // cerrar el archivo abierto
        try {
            rFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean escribir_TablaCertificacion(int posicion) {
        posicion_TablaCertificacion(posicion, LONGITUD_REGISTRO);
        rellenar_TablaCertificacion();
        String texto = tablaCertificacion_ciclo + sep + tablaCertificacion_anno + sep + tablaCertificacion_mes + sep + tablaCertificacion_cuenta + sep + tablaCertificacion_nombreusuario + sep
                + tablaCertificacion_direccion + sep + tablaCertificacion_fecha + sep + tablaCertificacion_hora + sep + tablaCertificacion_telefono + sep + tablaCertificacion_recibe + sep
                + tablaCertificacion_lector + sep + tablaCertificacion_cedula + sep + tablaCertificacion_enviado + sep + tablaCertificacion_CRNL;
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

    public void rellenar_TablaCertificacion() {
        try {
            tablaCertificacion_ciclo = String.format("%-4s", tablaCertificacion_ciclo);
            tablaCertificacion_anno = String.format("%-4s", tablaCertificacion_anno);
            tablaCertificacion_mes = String.format("%-2s", tablaCertificacion_mes);
            tablaCertificacion_cuenta = String.format("%-9s", tablaCertificacion_cuenta);
            tablaCertificacion_nombreusuario = String.format("%-48s", tablaCertificacion_nombreusuario);
            tablaCertificacion_direccion = String.format("%-64s", tablaCertificacion_direccion);
            tablaCertificacion_fecha = String.format("%-10s", tablaCertificacion_fecha);
            tablaCertificacion_hora = String.format("%-8s", tablaCertificacion_hora);
            tablaCertificacion_telefono = String.format("%-20s", tablaCertificacion_telefono);
            tablaCertificacion_recibe = String.format("%-30s", tablaCertificacion_recibe);
            tablaCertificacion_lector = String.format("%-10s", tablaCertificacion_lector);
            tablaCertificacion_cedula = String.format("%-13s", tablaCertificacion_cedula);
            tablaCertificacion_enviado = String.format("%-2s", tablaCertificacion_enviado);
            tablaCertificacion_CRNL = "\r\n";//= String.format("%-2s", tablaCertificacion_CRNL);
        } catch (Exception e) {
            System.out.println("Se presento problema al escribir en el archivo tablaCertificacion.dat..");
            e.printStackTrace();
        }

        return;
    }

    public void posicion_TablaCertificacion(int registro, int tamano) {
        try {
            rFile.seek((registro - 1) * tamano);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Escribir los registros atual en el archivo en posicion
    public void lectura_TablaCertificacion(int registro) {
        String[] campos;
        encontro_TablaCertificacion = 0;
        posicion_TablaCertificacion(registro, LONGITUD_REGISTRO);
        try {
            int fileSize = (int) rFile.length();
            byteArray = new byte[fileSize];
            rFile.readFully(byteArray, 0, LONGITUD_REGISTRO);
            texto = new String(byteArray);
            settablaCertificacion_ciclo(texto.substring(0, 4));
            settablaCertificacion_anno(texto.substring(5, 9));
            settablaCertificacion_mes(texto.substring(10, 12));
            settablaCertificacion_cuenta(texto.substring(13, 22));
            settablaCertificacion_nombreusuario(texto.substring(23, 71));
            settablaCertificacion_direccion(texto.substring(72, 136));
            settablaCertificacion_fecha(texto.substring(137, 147));
            settablaCertificacion_hora(texto.substring(148, 156));
            settablaCertificacion_telefono(texto.substring(157, 177));
            settablaCertificacion_recibe(texto.substring(178, 208));
            settablaCertificacion_lector(texto.substring(209, 219));
            settablaCertificacion_cedula(texto.substring(220, 233));
            settablaCertificacion_enviado(texto.substring(234, 236));
            settablaCertificacion_CRNL(texto.substring(237, 239));
            ultimo_TablaCertificacion = registro;
            // fin estructura
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void buscarSecuencial_TablaCertificacion(String codigo) {
        encontro_TablaCertificacion = 0;
        for (int i = 0; i < (int) (getTotal_TablaCertificacion()); i++) {
            lectura_TablaCertificacion(i + 1);
            if (Integer.parseInt(codigo.trim()) == Integer.parseInt(tablaCertificacion_cuenta.trim())) {
                encontro_TablaCertificacion = i + 1;
                posicion_TablaCertificacion(i + 1, LONGITUD_REGISTRO);
                i = getTotal_TablaCertificacion() + 10;
            } else {
                encontro_TablaCertificacion = 0;
            }
        }
        return;
    }

    public void buscarbinario_TablaCertificacion(String codigo) {
        int salir = 0;
        int i = 0;
        int t = getTotal_TablaCertificacion();
        int b = 0;
        lectura_TablaCertificacion(1);
        String uno = codigo.trim();
        String otro = tablaCertificacion_cuenta.trim();
        if (otro.equals(uno)) {
            posicion_TablaCertificacion(i + 1, LONGITUD_REGISTRO);
            encontro_TablaCertificacion = 1;
        } else {
            lectura_TablaCertificacion(getTotal_TablaCertificacion());
            otro = tablaCertificacion_cuenta.trim().equals("") ? "0" : tablaCertificacion_cuenta.trim();
            if ((Integer.parseInt(uno)) > (Integer.parseInt(otro))) {
                encontro_TablaCertificacion = 0;
            } else {
                while (salir == 0) {
                    i = (b + t) / 2;
                    lectura_TablaCertificacion(i + 1);
                    otro = tablaCertificacion_cuenta.trim();
                    if (otro.equals(uno)) {
                        encontro_TablaCertificacion = i;
                        posicion_TablaCertificacion(i + 1, LONGITUD_REGISTRO);
                        salir = 1;
                    } else {
                        if (b == i) {
                            lectura_TablaCertificacion(i + 2);
                            otro = tablaCertificacion_cuenta.trim();
                            if (uno == otro) {
                                encontro_TablaCertificacion = i;
                                posicion_TablaCertificacion(i + 1, LONGITUD_REGISTRO);
                            } else {
                                encontro_TablaCertificacion = 0;
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

    public int getTotal_TablaCertificacion() {
        return total_TablaCertificacion;
    }

    public void setTotal_TablaCertificacion(int total_TablaCertificacion) {
        this.total_TablaCertificacion = total_TablaCertificacion;
    }

    public String getArchivo_TablaCertificacion() {
        return archivo_TablaCertificacion;
    }

    public void setArchivo_TablaCertificacion(String archivo_TablaCertificacion) {
        this.archivo_TablaCertificacion = archivo_TablaCertificacion;
    }
}
