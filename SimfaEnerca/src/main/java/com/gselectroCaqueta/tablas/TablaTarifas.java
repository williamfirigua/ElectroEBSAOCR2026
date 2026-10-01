package com.gselectroCaqueta.tablas;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
/// <summary>
/// Descripcion breve del archivo a generar...tablaTarifas
/// Estructura (2026): id|anno|mes|codtarifa|KWH|VLORKWH|VLORRKWH
/// id es la concatenacion de anno+mes+codtarifa+KWH -- se usa como llave
/// de busqueda binaria en vez de codtarifa solo, porque codtarifa se repite
/// una vez por cada tramo de KWH.
/// </summary>

public class TablaTarifas { //Ax ya , es TARIFA

    // --- Anchos de cada campo, medidos directo del plano TARIFAS.TXT ---
    // id(17) + anno(4) + mes(2) + codtarifa(3) + KWH(8) + VLORKWH(12) + VLORRKWH(12)
    // + 7 separadores "|" = 65 bytes de datos + 2 bytes de fin de linea (CR LF)
    // = 67 bytes reales por registro en el plano.
    static final int LONGITUD_REGISTRO = 67;

    String sep = "|";
    String tablaTarifas_Id;
    String tablaTarifas_Anno;
    String tablaTarifas_Mes;
    String tablaTarifas_Codtarifa;
    String tablaTarifas_KWH;
    String tablaTarifas_Vlorkwh;
    String tablaTarifas_Vlorrkwh;

    BufferedReader fin;
    // Buffer reusable: se reserva UNA sola vez y se sobreescribe en cada
    // lectura, en vez de crear un byte[] nuevo (y una String completa del
    // archivo) por cada registro leido durante la busqueda binaria.
    private final byte[] bufferRegistro = new byte[LONGITUD_REGISTRO];
    int total_TablaTarifas;
    int ultimo_TablaTarifas;
    public int encontro_TablaTarifas;
    String buscar_TablaTarifas;
    RandomAccessFile rFile;
    private String archivo_TablaTarifas;

    public String gettablaTarifas_ID() {
        return tablaTarifas_Id;
    }

    public void settablaTarifas_Id(String tablaTarifas_Id) {
        this.tablaTarifas_Id = tablaTarifas_Id;
    }

    public String gettablaTarifas_ANNO() {
        return tablaTarifas_Anno;
    }

    public void settablaTarifas_Anno(String tablaTarifas_Anno) {
        this.tablaTarifas_Anno = tablaTarifas_Anno;
    }

    public String gettablaTarifas_MES() {
        return tablaTarifas_Mes;
    }

    public void settablaTarifas_Mes(String tablaTarifas_Mes) {
        this.tablaTarifas_Mes = tablaTarifas_Mes;
    }

    public String gettablaTarifas_CODTARIFA() {
        return tablaTarifas_Codtarifa;
    }

    public void settablaTarifas_Codtarifa(String tablaTarifas_Codtarifa) {
        this.tablaTarifas_Codtarifa = tablaTarifas_Codtarifa;
    }

    public String gettablaTarifas_KWH() {
        return tablaTarifas_KWH;
    }

    public void settablaTarifas_KWH(String tablaTarifas_KWH) {
        this.tablaTarifas_KWH = tablaTarifas_KWH;
    }

    public String gettablaTarifas_VLORKWH() {
        return tablaTarifas_Vlorkwh;
    }

    public void settablaTarifas_Vlorkwh(String tablaTarifas_Vlorkwh) {
        this.tablaTarifas_Vlorkwh = comaPunto(tablaTarifas_Vlorkwh);
    }

    public String gettablaTarifas_VLORRKWH() {
        return tablaTarifas_Vlorrkwh;
    }

    public void settablaTarifas_Vlorrkwh(String tablaTarifas_Vlorrkwh) {
        this.tablaTarifas_Vlorrkwh = comaPunto(tablaTarifas_Vlorrkwh);
    }

    public String comaPunto(String s) { //Ax: se crea ya que las comas provocan excepcion parseo doble
        if (s.contains(",")) {
            s = s.replace(",", ".");
        }
        return s;
    }

    // Abre el archivo UNA vez. A partir de aqui, lectura_TablaTarifas ya NO
    // vuelve a abrirlo ni a releerlo completo -- solo hace seek+read puntual.
    public Boolean abrir_TablaTarifas(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.length() == 0) {
            android.util.Log.e("TablaTarifas", "abrir_TablaTarifas: nombreArchivo vacio o null");
            return false;
        }
        try {
            rFile = new RandomAccessFile(nombreArchivo, "rw");
        } catch (FileNotFoundException e) {
            android.util.Log.e("TablaTarifas", "abrir_TablaTarifas: no se encontro el archivo '" + nombreArchivo + "'", e);
            return false;
        }
        setArchivo_TablaTarifas(nombreArchivo);
        return abrir();
    }

    // Ya NO lee el archivo completo a memoria -- solo calcula cuantos
    // registros hay, a partir del tamano del archivo. La lectura real de
    // cada registro se hace bajo demanda en lectura_TablaTarifas.
    private Boolean abrir() {
        try {
            int fileSize = (int) rFile.length();
            total_TablaTarifas = fileSize / LONGITUD_REGISTRO;
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void Cerrar_TablaTarifas() {
        try {
            rFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void posicion_TablaTarifas(int registro, int tamano) {
        try {
            rFile.seek((long) (registro - 1) * tamano);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Lee el registro en la posicion indicada usando el buffer reusable, y
    // separa sus campos leyendo directo de los bytes (sin pasar por una
    // String intermedia del registro completo).
    // IMPORTANTE: requiere haber llamado abrir_TablaTarifas() antes -- ya no
    // reabre ni relee el archivo por cada llamada.
    public int lectura_TablaTarifas(int registro) {
        encontro_TablaTarifas = 0;

        if (rFile == null) {
            android.util.Log.e("TablaTarifas", "lectura_TablaTarifas(" + registro + "): rFile es null -- "
                    + "no se llamo abrir_TablaTarifas() antes, o fallo silenciosamente.");
            return -2;
        }

        posicion_TablaTarifas(registro, LONGITUD_REGISTRO);
        try {
            rFile.readFully(bufferRegistro);
            settablaTarifas_Id(new String(bufferRegistro, 0, 17, StandardCharsets.UTF_8));
            settablaTarifas_Anno(new String(bufferRegistro, 18, 4, StandardCharsets.UTF_8));
            settablaTarifas_Mes(new String(bufferRegistro, 23, 2, StandardCharsets.UTF_8));
            settablaTarifas_Codtarifa(new String(bufferRegistro, 26, 3, StandardCharsets.UTF_8));
            settablaTarifas_KWH(new String(bufferRegistro, 30, 8, StandardCharsets.UTF_8));
            settablaTarifas_Vlorkwh(new String(bufferRegistro, 39, 12, StandardCharsets.UTF_8));
            settablaTarifas_Vlorrkwh(new String(bufferRegistro, 52, 12, StandardCharsets.UTF_8));
            ultimo_TablaTarifas = registro;
        } catch (Exception d) {
            android.util.Log.e("TablaTarifas", "lectura_TablaTarifas(" + registro + ") fallo: "
                    + d.getClass().getSimpleName() + " - " + d.getMessage()
                    + " | archivo=" + archivo_TablaTarifas
                    + " | total_TablaTarifas=" + total_TablaTarifas, d);
            return -2;
        }
        return 1;
    }

    // Busqueda binaria por "id" (anno+mes+codtarifa+KWH concatenados), que es
    // la llave real y unica del plano -- codtarifa solo NO alcanza, porque se
    // repite una vez por cada tramo de KWH de esa tarifa.
    public void buscarbinario_TablaTarifas(String idBuscado) {
        int salir = 0;
        int i;
        int t = total_TablaTarifas;
        int b = 0;

        if (lectura_TablaTarifas(1) == -2) {
            encontro_TablaTarifas = 0;
            return;
        }
        String uno = idBuscado.trim();
        String otro = tablaTarifas_Id.trim();

        if (otro.equals(uno)) {
            posicion_TablaTarifas(1, LONGITUD_REGISTRO);
            encontro_TablaTarifas = 1;
            return;
        }

        if (lectura_TablaTarifas(total_TablaTarifas) == -2) {
            encontro_TablaTarifas = 0;
            return;
        }
        otro = tablaTarifas_Id.trim();
        if (uno.compareTo(otro) > 0) {
            encontro_TablaTarifas = 0;
            return;
        }

        while (salir == 0) {
            i = (b + t) / 2;
            if (lectura_TablaTarifas(i + 1) == -2) {
                encontro_TablaTarifas = 0;
                return;
            }
            otro = tablaTarifas_Id.trim();
            if (otro.equals(uno)) {
                encontro_TablaTarifas = i;
                posicion_TablaTarifas(i + 1, LONGITUD_REGISTRO);
                salir = 1;
            } else if (b == i) {
                lectura_TablaTarifas(i + 2);
                otro = tablaTarifas_Id.trim();
                if (otro.equals(uno)) {
                    encontro_TablaTarifas = i;
                    posicion_TablaTarifas(i + 1, LONGITUD_REGISTRO);
                } else {
                    encontro_TablaTarifas = 0;
                }
                salir = 1;
            } else {
                if (uno.compareTo(otro) > 0) {
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

    public String getArchivo_TablaTarifas() {
        return archivo_TablaTarifas;
    }

    public void setArchivo_TablaTarifas(String archivo_TablaTarifas) {
        this.archivo_TablaTarifas = archivo_TablaTarifas;
    }

}
