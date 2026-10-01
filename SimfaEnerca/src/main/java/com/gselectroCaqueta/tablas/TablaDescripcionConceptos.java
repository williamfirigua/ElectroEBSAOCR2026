package com.gselectroCaqueta.tablas;

import com.gselectroCaqueta.accesoyseguridad.VariablesGlobales;
import com.gsutil.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
/// <summary>
/// Descripcion breve del archivo a generar...tablaDescripcionConceptos
/// Estructura nueva: codigo|descripcion|tipo|dato1|dato2|dato3|dato4|dato5|dato6|puntero
/// dato1..dato6 y puntero son campos nuevos del plano (2026). Se dejan
/// mapeados a nivel estructural; su logica de negocio esta pendiente de
/// identificar -- ver Bitacora de Mesa Tecnica.
/// </summary>

public class TablaDescripcionConceptos {//Ax ya  es D_CONCEP

    // --- Anchos medidos directo de DES_CONC.TXT ---
    // codigo(3)+descripcion(50)+tipo(1)+dato1(1)+dato2(3)+dato3(1)+dato4(6)
    // +dato5(4)+dato6(3)+puntero(5) + 10 separadores "|" = 87 bytes de datos
    // + 2 bytes de fin de linea (CR LF) = 89 bytes reales por registro.
    static final int LONGITUD_REGISTRO = 89;

    String sep = "|";
    String tablaDescripcionConceptos_CODIGO;
    String tablaDescripcionConceptos_DESCRIPCION;
    String tablaDescripcionConceptos_TIPO;
    String tablaDescripcionConceptos_DATO1;
    String tablaDescripcionConceptos_DATO2;
    String tablaDescripcionConceptos_DATO3;
    String tablaDescripcionConceptos_DATO4;
    String tablaDescripcionConceptos_DATO5;
    String tablaDescripcionConceptos_DATO6;
    String tablaDescripcionConceptos_PUNTERO;

    BufferedReader fin;
    // Buffer reusable: se reserva UNA sola vez y se sobreescribe en cada
    // lectura, en vez de crear un byte[] nuevo por cada registro leido
    // durante la busqueda binaria.
    private final byte[] bufferRegistro = new byte[LONGITUD_REGISTRO];
    int total_TablaDescripcionConceptos;
    int ultimo_TablaDescripcionConceptos;
    public int encontro_TablaDescripcionConceptos;
    String buscar_TablaDescripcionConceptos;
    RandomAccessFile rFile;
    Utils utils = new Utils();//Ax log y utilidades
    private String archivo_TablaDescripcionConceptos;

    public String gettablaDescripcionConceptos_CODIGO() {
        return tablaDescripcionConceptos_CODIGO;
    }

    public void settablaDescripcionConceptos_CODIGO(String tablaDescripcionConceptos_CODIGO) {
        this.tablaDescripcionConceptos_CODIGO = tablaDescripcionConceptos_CODIGO;
    }

    public String gettablaDescripcionConceptos_DESCRIPCION() {
        return tablaDescripcionConceptos_DESCRIPCION;
    }

    public void settablaDescripcionConceptos_DESCRIPCION(String tablaDescripcionConceptos_DESCRIPCION) {
        this.tablaDescripcionConceptos_DESCRIPCION = tablaDescripcionConceptos_DESCRIPCION;
    }

    public String gettablaDescripcionConceptos_TIPO() {
        return tablaDescripcionConceptos_TIPO;
    }

    public void settablaDescripcionConceptos_TIPO(String tablaDescripcionConceptos_TIPO) {
        this.tablaDescripcionConceptos_TIPO = tablaDescripcionConceptos_TIPO;
    }

    public String gettablaDescripcionConceptos_DATO1() {
        return tablaDescripcionConceptos_DATO1;
    }

    public void settablaDescripcionConceptos_DATO1(String tablaDescripcionConceptos_DATO1) {
        this.tablaDescripcionConceptos_DATO1 = tablaDescripcionConceptos_DATO1;
    }

    public String gettablaDescripcionConceptos_DATO2() {
        return tablaDescripcionConceptos_DATO2;
    }

    public void settablaDescripcionConceptos_DATO2(String tablaDescripcionConceptos_DATO2) {
        this.tablaDescripcionConceptos_DATO2 = tablaDescripcionConceptos_DATO2;
    }

    public String gettablaDescripcionConceptos_DATO3() {
        return tablaDescripcionConceptos_DATO3;
    }

    public void settablaDescripcionConceptos_DATO3(String tablaDescripcionConceptos_DATO3) {
        this.tablaDescripcionConceptos_DATO3 = tablaDescripcionConceptos_DATO3;
    }

    public String gettablaDescripcionConceptos_DATO4() {
        return tablaDescripcionConceptos_DATO4;
    }

    public void settablaDescripcionConceptos_DATO4(String tablaDescripcionConceptos_DATO4) {
        this.tablaDescripcionConceptos_DATO4 = tablaDescripcionConceptos_DATO4;
    }

    public String gettablaDescripcionConceptos_DATO5() {
        return tablaDescripcionConceptos_DATO5;
    }

    public void settablaDescripcionConceptos_DATO5(String tablaDescripcionConceptos_DATO5) {
        this.tablaDescripcionConceptos_DATO5 = tablaDescripcionConceptos_DATO5;
    }

    public String gettablaDescripcionConceptos_DATO6() {
        return tablaDescripcionConceptos_DATO6;
    }

    public void settablaDescripcionConceptos_DATO6(String tablaDescripcionConceptos_DATO6) {
        this.tablaDescripcionConceptos_DATO6 = tablaDescripcionConceptos_DATO6;
    }

    public String gettablaDescripcionConceptos_PUNTERO() {
        return tablaDescripcionConceptos_PUNTERO;
    }

    public void settablaDescripcionConceptos_PUNTERO(String tablaDescripcionConceptos_PUNTERO) {
        this.tablaDescripcionConceptos_PUNTERO = tablaDescripcionConceptos_PUNTERO;
    }

    // Abre el archivo UNA vez. lectura_TablaDescripcionConceptos ya NO vuelve
    // a abrirlo ni a releerlo completo -- solo hace seek+read puntual.
    public Boolean abrir_TablaDescripcionConceptos(String nombreArchivo) {
        if (nombreArchivo.length() == 0) {
            return false;
        }
        try {
            rFile = new RandomAccessFile(nombreArchivo, "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        setArchivo_TablaDescripcionConceptos(nombreArchivo);
        return abrir();
    }

    // Ya NO lee el archivo completo a memoria -- solo calcula cuantos
    // registros hay, a partir del tamano del archivo.
    private Boolean abrir() {
        try {
            int fileSize = (int) rFile.length();
            total_TablaDescripcionConceptos = fileSize / LONGITUD_REGISTRO;
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void Cerrar_TablaDescripcionConceptos() {
        try {
            rFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void posicion_TablaDescripcionConceptos(int registro, int tamano) {
        try {
            rFile.seek((long) (registro - 1) * tamano);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Lee el registro en la posicion indicada usando el buffer reusable, y
    // separa sus campos leyendo directo de los bytes.
    // IMPORTANTE: requiere haber llamado abrir_TablaDescripcionConceptos()
    // antes -- ya no reabre ni relee el archivo por cada llamada.
    public int lectura_TablaDescripcionConceptos(int registro) {
        encontro_TablaDescripcionConceptos = 1;
        posicion_TablaDescripcionConceptos(registro, LONGITUD_REGISTRO);
        try {
            rFile.readFully(bufferRegistro);
            settablaDescripcionConceptos_CODIGO(new String(bufferRegistro, 0, 3, StandardCharsets.UTF_8));
            settablaDescripcionConceptos_DESCRIPCION(new String(bufferRegistro, 4, 50, StandardCharsets.UTF_8));
            settablaDescripcionConceptos_TIPO(new String(bufferRegistro, 55, 1, StandardCharsets.UTF_8));
            settablaDescripcionConceptos_DATO1(new String(bufferRegistro, 57, 1, StandardCharsets.UTF_8));
            settablaDescripcionConceptos_DATO2(new String(bufferRegistro, 59, 3, StandardCharsets.UTF_8));
            settablaDescripcionConceptos_DATO3(new String(bufferRegistro, 63, 1, StandardCharsets.UTF_8));
            settablaDescripcionConceptos_DATO4(new String(bufferRegistro, 65, 6, StandardCharsets.UTF_8));
            settablaDescripcionConceptos_DATO5(new String(bufferRegistro, 72, 4, StandardCharsets.UTF_8));
            settablaDescripcionConceptos_DATO6(new String(bufferRegistro, 77, 3, StandardCharsets.UTF_8));
            settablaDescripcionConceptos_PUNTERO(new String(bufferRegistro, 81, 5, StandardCharsets.UTF_8));
            ultimo_TablaDescripcionConceptos = registro;
            // fin estructura
        } catch (Exception e) {
            encontro_TablaDescripcionConceptos = 0;
            utils.Log(new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/LOGEVENTOS.LOG"), "lectura_TablaDescripcionConceptos|ERROR|" + e.getMessage());
            e.printStackTrace();
            return -2;
        }
        return 1;
    }

    public void buscarbinario_TablaDescripcionConceptos(String codigo) {
        int salir = 0;
        int i;
        int t = total_TablaDescripcionConceptos;
        int b = 0;

        if (lectura_TablaDescripcionConceptos(1) == -2) {
            encontro_TablaDescripcionConceptos = 0;
            return;
        }
        String uno = codigo.trim();
        String otro = tablaDescripcionConceptos_PUNTERO.trim();

        if (otro.equals(uno)) {
            posicion_TablaDescripcionConceptos(1, LONGITUD_REGISTRO);
            encontro_TablaDescripcionConceptos = 1;
            return;
        }

        if (lectura_TablaDescripcionConceptos(total_TablaDescripcionConceptos) == -2) {
            encontro_TablaDescripcionConceptos = 0;
            return;
        }
        otro = tablaDescripcionConceptos_PUNTERO.trim().equals("") ? "0" : tablaDescripcionConceptos_PUNTERO.trim();
        if ((Integer.parseInt(uno)) > (Integer.parseInt(otro))) {
            encontro_TablaDescripcionConceptos = 0;
            return;
        }

        while (salir == 0) {
            i = (b + t) / 2;
            if (lectura_TablaDescripcionConceptos(i + 1) == -2) {
                encontro_TablaDescripcionConceptos = 0;
                return;
            }
            otro = tablaDescripcionConceptos_PUNTERO.trim();
            if (otro.equals(uno)) {
                encontro_TablaDescripcionConceptos = i;
                posicion_TablaDescripcionConceptos(i + 1, LONGITUD_REGISTRO);
                salir = 1;
            } else if (b == i) {
                lectura_TablaDescripcionConceptos(i + 2);
                otro = tablaDescripcionConceptos_PUNTERO.trim();
                if (otro.equals(uno)) {
                    encontro_TablaDescripcionConceptos = i;
                    posicion_TablaDescripcionConceptos(i + 1, LONGITUD_REGISTRO);
                } else {
                    encontro_TablaDescripcionConceptos = 0;
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

    public String getArchivo_TablaDescripcionConceptos() {
        return archivo_TablaDescripcionConceptos;
    }

    public void setArchivo_TablaDescripcionConceptos(String archivo_TablaDescripcionConceptos) {
        this.archivo_TablaDescripcionConceptos = archivo_TablaDescripcionConceptos;
    }

}
