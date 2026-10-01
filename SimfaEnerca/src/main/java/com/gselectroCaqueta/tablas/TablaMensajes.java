package com.gselectroCaqueta.tablas;

import com.gselectroCaqueta.accesoyseguridad.VariablesGlobales;
import com.gsutil.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
/// <summary>
/// Descripcion breve del archivo a generar...tablaMensajes
/// Estructura: codigo|descripcion|dato1|dato2|dato3
/// codigo = municipio + tipo de mensaje (ej. "15001A" = municipio 15001,
/// tipo A; "15001T" = mismo municipio, tipo T). Es la parametrizacion de
/// mensajes por municipio que confirmo EBSA (campo CDGOMNSJE en LM_LISTA).
/// </summary>

public class TablaMensajes { //Ax ya , es MENSAJES

    // --- Anchos medidos directo de MENSAJES.TXT ---
    // codigo(6)+descripcion(250)+dato1(1)+dato2(1)+dato3(1) + 5 separadores "|"
    // = 264 bytes de datos + 2 bytes de fin de linea (CR LF) = 266 por registro.
    static final int LONGITUD_REGISTRO = 266;

    String sep = "|";
    String tablaMensajes_CODIGO;
    String tablaMensajes_DESCRIPCION;
    String tablaMensajes_DATO1;
    String tablaMensajes_DATO2;
    String tablaMensajes_DATO3;

    // Buffer reusable: se reserva UNA sola vez y se sobreescribe en cada
    // lectura, en vez de crear un byte[] nuevo por cada registro leido
    // durante la busqueda binaria.
    private final byte[] bufferRegistro = new byte[LONGITUD_REGISTRO];
    int total_TablaMensajes;
    int ultimo_TablaMensajes;
    public int encontro_TablaMensajes;
    String buscar_TablaMensajes;
    RandomAccessFile rFile;
    Utils utils = new Utils();//Ax log y utilidades
    private String archivo_TablaMensajes;

    public String gettablaMensajes_CODIGO() {
        return tablaMensajes_CODIGO;
    }

    public void settablaMensajes_CODIGO(String tablaMensajes_CODIGO) {
        this.tablaMensajes_CODIGO = tablaMensajes_CODIGO;
    }

    public String gettablaMensajes_DESCRIPCION() {
        return tablaMensajes_DESCRIPCION;
    }

    public void settablaMensajes_DESCRIPCION(String tablaMensajes_DESCRIPCION) {
        this.tablaMensajes_DESCRIPCION = tablaMensajes_DESCRIPCION;
    }

    public String gettablaMensajes_DATO1() {
        return tablaMensajes_DATO1;
    }

    public void settablaMensajes_DATO1(String tablaMensajes_DATO1) {
        this.tablaMensajes_DATO1 = tablaMensajes_DATO1;
    }

    public String gettablaMensajes_DATO2() {
        return tablaMensajes_DATO2;
    }

    public void settablaMensajes_DATO2(String tablaMensajes_DATO2) {
        this.tablaMensajes_DATO2 = tablaMensajes_DATO2;
    }

    public String gettablaMensajes_DATO3() {
        return tablaMensajes_DATO3;
    }

    public void settablaMensajes_DATO3(String tablaMensajes_DATO3) {
        this.tablaMensajes_DATO3 = tablaMensajes_DATO3;
    }

    // Abre el archivo UNA vez. lectura_TablaMensajes ya NO vuelve a abrirlo
    // ni a releerlo completo -- solo hace seek+read puntual.
    public Boolean abrir_TablaMensajes(String nombreArchivo) {
        if (nombreArchivo.length() == 0) {
            return false;
        }
        try {
            rFile = new RandomAccessFile(nombreArchivo, "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        setArchivo_TablaMensajes(nombreArchivo);
        return abrir();
    }

    // Ya NO lee el archivo completo a memoria -- solo calcula cuantos
    // registros hay, a partir del tamano del archivo.
    private Boolean abrir() {
        try {
            int fileSize = (int) rFile.length();
            total_TablaMensajes = fileSize / LONGITUD_REGISTRO;
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void Cerrar_TablaMensajes() {
        try {
            rFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void posicion_TablaMensajes(int registro, int tamano) {
        try {
            rFile.seek((long) (registro - 1) * tamano);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Lee el registro en la posicion indicada usando el buffer reusable, y
    // separa sus campos leyendo directo de los bytes.
    // IMPORTANTE: requiere haber llamado abrir_TablaMensajes() antes -- ya
    // no reabre ni relee el archivo por cada llamada.
    public int lectura_TablaMensajes(int registro) {
        encontro_TablaMensajes = 1;
        posicion_TablaMensajes(registro, LONGITUD_REGISTRO);
        try {
            rFile.readFully(bufferRegistro);
            settablaMensajes_CODIGO(new String(bufferRegistro, 0, 6, StandardCharsets.UTF_8));
            settablaMensajes_DESCRIPCION(new String(bufferRegistro, 7, 250, StandardCharsets.UTF_8));
            settablaMensajes_DATO1(new String(bufferRegistro, 258, 1, StandardCharsets.UTF_8));
            settablaMensajes_DATO2(new String(bufferRegistro, 260, 1, StandardCharsets.UTF_8));
            settablaMensajes_DATO3(new String(bufferRegistro, 262, 1, StandardCharsets.UTF_8));
            ultimo_TablaMensajes = registro;
            // fin estructura
        } catch (Exception e) {
            encontro_TablaMensajes = 0;
            utils.Log(new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/LOGEVENTOS.LOG"), "lectura_TablaMensajes|ERROR|" + e.getMessage());
            e.printStackTrace();
            return -2;
        }
        return 1;
    }

    // Busqueda binaria por "codigo". A diferencia de TablaDescripcionConceptos,
    // aqui el codigo es ALFANUMERICO (municipio + tipo, ej. "15001A"), asi
    // que se compara como String (compareTo), NUNCA con Integer.parseInt --
    // eso reventaria en el primer registro con letra.
    public void buscarbinario_TablaMensajes(String codigo) {
        int salir = 0;
        int i;
        int t = total_TablaMensajes;
        int b = 0;

        if (lectura_TablaMensajes(1) == -2) {
            encontro_TablaMensajes = 0;
            return;
        }
        String uno = codigo.trim();
        String otro = tablaMensajes_CODIGO.trim();

        if (otro.equals(uno)) {
            posicion_TablaMensajes(1, LONGITUD_REGISTRO);
            encontro_TablaMensajes = 1;
            return;
        }

        if (lectura_TablaMensajes(total_TablaMensajes) == -2) {
            encontro_TablaMensajes = 0;
            return;
        }
        otro = tablaMensajes_CODIGO.trim();
        if (uno.compareTo(otro) > 0) {
            encontro_TablaMensajes = 0;
            return;
        }

        while (salir == 0) {
            i = (b + t) / 2;
            if (lectura_TablaMensajes(i + 1) == -2) {
                encontro_TablaMensajes = 0;
                return;
            }
            otro = tablaMensajes_CODIGO.trim();
            if (otro.equals(uno)) {
                encontro_TablaMensajes = i;
                posicion_TablaMensajes(i + 1, LONGITUD_REGISTRO);
                salir = 1;
            } else if (b == i) {
                lectura_TablaMensajes(i + 2);
                otro = tablaMensajes_CODIGO.trim();
                if (otro.equals(uno)) {
                    encontro_TablaMensajes = i;
                    posicion_TablaMensajes(i + 1, LONGITUD_REGISTRO);
                } else {
                    encontro_TablaMensajes = 0;
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

    public String getArchivo_TablaMensajes() {
        return archivo_TablaMensajes;
    }

    public void setArchivo_TablaMensajes(String archivo_TablaMensajes) {
        this.archivo_TablaMensajes = archivo_TablaMensajes;
    }

}
