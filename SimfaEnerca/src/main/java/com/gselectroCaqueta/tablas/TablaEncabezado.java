package com.gselectroCaqueta.tablas;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
/// <summary>
/// Descripcion breve del archivo a generar...tablaEncabezado
/// </summary>

public class TablaEncabezado { //Ax esta es TABLAENCABEZADO
    static final int LONGITUD_REGISTRO = 142;//47;
    String sep = ";";
    String tablaEncabezado_ciclo;
    String tablaEncabezado_DPTO;
    String tablaEncabezado_MUNICIPIO;
    String tablaEncabezado_LECTOR;
    String tablaEncabezado_CLAVELECTOR;
    String tablaEncabezado_TERMINAL;
    String tablaEncabezado_IMPRESORA;
    String tablaEncabezado_fechaproceso;

    String tablaEncabezado_diasdecorte;
    String tablaEncabezado_diasdesuspencion;

    String tablaEncabezado_administrador;
    String tablaEncabezado_tiempoauditoria;
    String tablaEncabezado_distanciagps;
    String tablaEncabezado_nrodias;
    String tablaEncabezado_fechainicial;
    String tablaEncabezado_fechafinal;
    String tablaEncabezado_obligafotos;
    String tablaEncabezado_obligabarras;
    String tablaEncabezado_maximoregaenviar;
    String tablaEncabezado_tiporuta;


    String tablaEncabezado_CRNL;
    //String tablaEncabezado_general_obliga_hora_inicial;
    BufferedReader fin;
    byte[] byteArray;
    int total_TablaEncabezado;
    int ultimo_TablaEncabezado;
    int encontro_TablaEncabezado;
    //String buscar_TablaEncabezado;
    String texto;
    RandomAccessFile rFile;
    private String archivo_TablaEncabezado;

    public String gettablaEncabezado_CICLO() {
        return tablaEncabezado_ciclo;
    }

    public void settablaEncabezado_ciclo(String tablaEncabezado_ciclo) {
        this.tablaEncabezado_ciclo = tablaEncabezado_ciclo;
    }

    public String gettablaEncabezado_DPTO() {
        return tablaEncabezado_DPTO;
    }

    public void settablaEncabezado_DPTO(String tablaEncabezado_DPTO) {
        this.tablaEncabezado_DPTO = tablaEncabezado_DPTO;
    }

    public String gettablaEncabezado_MUNICIPIO() {
        return tablaEncabezado_MUNICIPIO;
    }

    public void settablaEncabezado_MUNICIPIO(String tablaEncabezado_MUNICIPIO) {
        this.tablaEncabezado_MUNICIPIO = tablaEncabezado_MUNICIPIO;
    }

    public String gettablaEncabezado_LECTOR() {
        return tablaEncabezado_LECTOR;
    }

    public void settablaEncabezado_LECTOR(String tablaEncabezado_LECTOR) {
        this.tablaEncabezado_LECTOR = tablaEncabezado_LECTOR;
    }

    public String gettablaEncabezado_CLAVELECTOR() {
        return tablaEncabezado_CLAVELECTOR;
    }

    public void settablaEncabezado_CLAVELECTOR(String tablaEncabezado_CLAVELECTOR) {
        this.tablaEncabezado_CLAVELECTOR = tablaEncabezado_CLAVELECTOR;
    }

    public String gettablaEncabezado_TERMINAL() {
        return tablaEncabezado_TERMINAL;
    }

    public void settablaEncabezado_TERMINAL(String tablaEncabezado_TERMINAL) {
        this.tablaEncabezado_TERMINAL = tablaEncabezado_TERMINAL;
    }

    public String gettablaEncabezado_IMPRESORA() {
        return tablaEncabezado_IMPRESORA;
    }

    public void settablaEncabezado_IMPRESORA(String tablaEncabezado_IMPRESORA) {
        this.tablaEncabezado_IMPRESORA = tablaEncabezado_IMPRESORA;
    }

    public String gettablaEncabezado_FECHAPROCESO() {
        return tablaEncabezado_fechaproceso;
    }

    public void settablaEncabezado_fechaproceso(String tablaEncabezado_fechaproceso) {
        this.tablaEncabezado_fechaproceso = tablaEncabezado_fechaproceso;
    }

    public String gettablaEncabezado_CRNL() {
        return tablaEncabezado_CRNL;
    }

    public void settablaEncabezado_CRNL(String tablaEncabezado_CRNL) {
        this.tablaEncabezado_CRNL = tablaEncabezado_CRNL;
    }

    public String getTablaEncabezado_diasdecorte() {
        return tablaEncabezado_diasdecorte;
    }

    public void setTablaEncabezado_diasdecorte(String tablaEncabezado_diasdecorte) {
        this.tablaEncabezado_diasdecorte = tablaEncabezado_diasdecorte;
    }

    public String getTablaEncabezado_diasdesuspencion() {
        return tablaEncabezado_diasdesuspencion;
    }

    public void setTablaEncabezado_diasdesuspencion(String tablaEncabezado_diasdesuspencion) {
        this.tablaEncabezado_diasdesuspencion = tablaEncabezado_diasdesuspencion;
    }

    public String getTablaEncabezado_administrador() {
        return tablaEncabezado_administrador;
    }

    public void setTablaEncabezado_administrador(String tablaEncabezado_administrador) {
        this.tablaEncabezado_administrador = tablaEncabezado_administrador;
    }

    public String getTablaEncabezado_tiempoauditoria() {
        return tablaEncabezado_tiempoauditoria;
    }

    public void setTablaEncabezado_tiempoauditoria(String tablaEncabezado_tiempoauditoria) {
        this.tablaEncabezado_tiempoauditoria = tablaEncabezado_tiempoauditoria;
    }

    public String getTablaEncabezado_distanciagps() {
        return tablaEncabezado_distanciagps;
    }

    public void setTablaEncabezado_distanciagps(String tablaEncabezado_distanciagps) {
        this.tablaEncabezado_distanciagps = tablaEncabezado_distanciagps;
    }

    public String getTablaEncabezado_nrodias() {
        return tablaEncabezado_nrodias;
    }

    public void setTablaEncabezado_nrodias(String tablaEncabezado_nrodias) {
        this.tablaEncabezado_nrodias = tablaEncabezado_nrodias;
    }

    public String getTablaEncabezado_fechainicial() {
        return tablaEncabezado_fechainicial;
    }

    public void setTablaEncabezado_fechainicial(String tablaEncabezado_fechainicial) {
        this.tablaEncabezado_fechainicial = tablaEncabezado_fechainicial;
    }

    public String getTablaEncabezado_fechafinal() {
        return tablaEncabezado_fechafinal;
    }

    public void setTablaEncabezado_fechafinal(String tablaEncabezado_fechafinal) {
        this.tablaEncabezado_fechafinal = tablaEncabezado_fechafinal;
    }

    public String getTablaEncabezado_obligafotos() {
        return tablaEncabezado_obligafotos;
    }

    public void setTablaEncabezado_obligafotos(String tablaEncabezado_obligafotos) {
        this.tablaEncabezado_obligafotos = tablaEncabezado_obligafotos;
    }

    public String getTablaEncabezado_obligabarras() {
        return tablaEncabezado_obligabarras;
    }

    public void setTablaEncabezado_obligabarras(String tablaEncabezado_obligabarras) {
        this.tablaEncabezado_obligabarras = tablaEncabezado_obligabarras;
    }

    public String getTablaEncabezado_maximoregaenviar() {
        return tablaEncabezado_maximoregaenviar;
    }

    public void setTablaEncabezado_maximoregaenviar(String tablaEncabezado_maximoregaenviar) {
        this.tablaEncabezado_maximoregaenviar = tablaEncabezado_maximoregaenviar;
    }

    public String getTablaEncabezado_tiporuta() {
        return tablaEncabezado_tiporuta;
    }

    public void setTablaEncabezado_tiporuta(String tablaEncabezado_tiporuta) {
        this.tablaEncabezado_tiporuta = tablaEncabezado_tiporuta;
    }

    public Boolean abrir_TablaEncabezado(String nombreArchivo) {
        if (nombreArchivo.length() == 0) {
            return false;
        }
        try {
            rFile = new RandomAccessFile(nombreArchivo, "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        setArchivo_TablaEncabezado(nombreArchivo);
        return abrir(getArchivo_TablaEncabezado());
    }

    private Boolean abrir(String nombre_archivo) {
        try {
            int fileSize = (int) rFile.length();
            byteArray = new byte[fileSize];
            rFile.readFully(byteArray, 0, fileSize);
            texto = new String(byteArray);
            total_TablaEncabezado = fileSize / LONGITUD_REGISTRO;
            return true;
        } catch (Exception ex) {
            // Log.e('Ficheros', 'Error al leer fichero desde tarjeta SD');
            ex.printStackTrace();
            return false;
        }
    }

    public void Cerrar_TablaEncabezado() {
        // cerrar el archivo abierto
        try {
            rFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean escribir_TablaEncabezado(int posicion) {
        posicion_TablaEncabezado(posicion, LONGITUD_REGISTRO);
        rellenar_TablaEncabezado();
        String texto = tablaEncabezado_ciclo + sep + tablaEncabezado_DPTO + sep + tablaEncabezado_MUNICIPIO + sep + tablaEncabezado_LECTOR + sep + tablaEncabezado_CLAVELECTOR + sep
                + tablaEncabezado_TERMINAL + sep + tablaEncabezado_IMPRESORA + sep + tablaEncabezado_fechaproceso  + sep + tablaEncabezado_CRNL;
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

    public void rellenar_TablaEncabezado() {
        try {
            tablaEncabezado_ciclo = String.format("%-2s", tablaEncabezado_ciclo);
            tablaEncabezado_DPTO = String.format("%-1s", tablaEncabezado_DPTO);
            tablaEncabezado_MUNICIPIO = String.format("%-3s", tablaEncabezado_MUNICIPIO);
            tablaEncabezado_LECTOR = String.format("%-10s", tablaEncabezado_LECTOR);
            tablaEncabezado_CLAVELECTOR = String.format("%-12s", tablaEncabezado_CLAVELECTOR);
            tablaEncabezado_TERMINAL = String.format("%-15s", tablaEncabezado_TERMINAL);
            tablaEncabezado_IMPRESORA = String.format("%-15s", tablaEncabezado_IMPRESORA);
            tablaEncabezado_fechaproceso = String.format("%-10s", tablaEncabezado_fechaproceso);
            tablaEncabezado_CRNL = String.format("%-1s", tablaEncabezado_CRNL);
        } catch (Exception e) {
            System.out.println("Se presento problema al escribir en el archivo tablaEncabezado.dat..");
            e.printStackTrace();
        }

        return;
    }

    public void posicion_TablaEncabezado(int registro, int tamano) {
        try {
            rFile.seek((registro - 1) * tamano);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Escribir los registros atual en el archivo en posicion
    public void lectura_TablaEncabezado(int registro) {
        String[] campos;
        encontro_TablaEncabezado = 0;
        posicion_TablaEncabezado(registro, LONGITUD_REGISTRO);
        try {
            int fileSize = (int) rFile.length();
            byteArray = new byte[fileSize];
            rFile.readFully(byteArray, 0, LONGITUD_REGISTRO);
            texto = new String(byteArray);
            settablaEncabezado_ciclo(texto.substring(0, 3));
            settablaEncabezado_DPTO(texto.substring(4, 7));
            settablaEncabezado_MUNICIPIO(texto.substring(8, 11));
            settablaEncabezado_LECTOR(texto.substring(12, 22));
            settablaEncabezado_CLAVELECTOR(texto.substring(23, 35));
            settablaEncabezado_TERMINAL(texto.substring(36, 51));
            settablaEncabezado_IMPRESORA(texto.substring(52, 67));
            settablaEncabezado_fechaproceso(texto.substring(68, 78));
            setTablaEncabezado_diasdecorte(texto.substring(79, 81));
            setTablaEncabezado_diasdesuspencion(texto.substring(82, 84));
            setTablaEncabezado_administrador(texto.substring(85, 96));
            setTablaEncabezado_tiempoauditoria(texto.substring(97, 103));
            setTablaEncabezado_distanciagps(texto.substring(104, 107));
            setTablaEncabezado_nrodias(texto.substring(108, 111));
            setTablaEncabezado_fechainicial(texto.substring(112, 120));
            setTablaEncabezado_fechafinal(texto.substring(121, 129));
            setTablaEncabezado_obligafotos(texto.substring(130, 131));
            setTablaEncabezado_obligabarras(texto.substring(132, 133));
            setTablaEncabezado_maximoregaenviar(texto.substring(134, 137));
            setTablaEncabezado_tiporuta(texto.substring(138, 139));

            settablaEncabezado_CRNL(texto.substring(140, 142));
            ultimo_TablaEncabezado = registro;
            // fin estructura
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getArchivo_TablaEncabezado() {
        return archivo_TablaEncabezado;
    }

    public void setArchivo_TablaEncabezado(String archivo_TablaEncabezado) {
        this.archivo_TablaEncabezado = archivo_TablaEncabezado;
    }

    // public void buscarSecuencial_TablaEncabezado(String codigo)
    // {
    // encontro_TablaEncabezado=0;
    // for (int i = 0; i<(int)(total_TablaEncabezado); i++)
    // {
    // lectura_TablaEncabezado(i+1);
    // if (Integer.parseInt(codigo.trim())==Integer.parseInt(tablaEncabezado_administrador.trim()))
    // {
    // encontro_TablaEncabezado = i+1;
    // posicion_TablaEncabezado(i+1, LONGITUD_REGISTRO);
    // i = total_TablaEncabezado + 10;
    // }
    // else
    // {
    // encontro_TablaEncabezado = 0;
    // }
    // }
    // return;
    // }

    // public void buscarbinario_TablaEncabezado(String codigo)
    // {
    // int salir=0;
    // int i = 0;
    // int t = total_TablaEncabezado;
    // int b = 0;
    // lectura_TablaEncabezado(1);
    // String uno = codigo.trim();
    // String otro = .trim();
    // if (otro.equals(uno))
    // {
    // posicion_TablaEncabezado(i+1,LONGITUD_REGISTRO);
    // encontro_TablaEncabezado=1;
    // }
    // else
    // {
    // lectura_TablaEncabezado(total_TablaEncabezado);
    // otro = .trim().equals("")?"0":.trim();
    // if ((Integer.parseInt(uno)) > (Integer.parseInt(otro)))
    // {
    // encontro_TablaEncabezado=0;
    // }
    // else
    // {
    // while(salir==0)
    // {
    // i = (b + t)/2;
    // lectura_TablaEncabezado(i+1);
    // otro = .trim();
    // if (otro.equals(uno))
    // {
    // encontro_TablaEncabezado=i;
    // posicion_TablaEncabezado(i+1,LONGITUD_REGISTRO);
    // salir=1;
    // }
    // else
    // {
    // if (b == i)
    // {
    // lectura_TablaEncabezado(i+2);
    // otro = .trim();
    // if (uno == otro)
    // {
    // encontro_TablaEncabezado=i;
    // posicion_TablaEncabezado(i+1,LONGITUD_REGISTRO);
    // }
    // else
    // {
    // encontro_TablaEncabezado = 0;
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
    //
    //
    //
    // public void buscarbinarioChar_TablaEncabezado(String codigo)
    // {
    // int salir=0;
    // int i = 0;
    // int t = total_TablaEncabezado;
    // int b = 0;
    // lectura_TablaEncabezado(1);
    // String uno = codigo.toUpperCase().trim();
    // String otro = .toUpperCase().trim();
    // if (otro.compareTo(uno) == 0)
    // {
    // posicion_TablaEncabezado(i+1,LONGITUD_REGISTRO);
    // encontro_TablaEncabezado=1;
    // }
    // else {
    // lectura_TablaEncabezado(total_TablaEncabezado);
    // otro = .trim().equals("")?"0":.toUpperCase().trim();
    // if (uno.compareTo(otro) == 1 )
    // {
    // encontro_TablaEncabezado = 0;
    // }
    // else
    // {
    // while(salir==0)
    // {
    // i = (b + t)/2;
    // lectura_TablaEncabezado(i+1);
    // otro = .toUpperCase().trim();
    // if (uno.compareTo(otro) == 0)
    // {
    // encontro_TablaEncabezado = i;
    // posicion_TablaEncabezado(i+1,LONGITUD_REGISTRO);
    // salir=1;
    // }
    // else
    // {
    // if (b == i)
    // {
    // lectura_TablaEncabezado(i+2);
    // otro = .toUpperCase().trim();
    // if (uno.compareTo(otro) == 0)
    // {
    // encontro_TablaEncabezado=i;
    // posicion_TablaEncabezado(i+1,LONGITUD_REGISTRO);
    // }
    // else
    // {
    // encontro_TablaEncabezado=0;
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
