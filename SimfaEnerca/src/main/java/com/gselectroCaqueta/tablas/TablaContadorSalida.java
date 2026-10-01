package com.gselectroCaqueta.tablas;

import com.gselectroCaqueta.accesoyseguridad.VariablesGlobales;
import com.gsutil.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
/// <summary>
/// Descripcion breve del archivo a generar...tablaContadorSalida
/// </summary>

public class TablaContadorSalida {
    String sep = ";";
    String tablaContadorSalida_Cuenta;
    String tablaContadorSalida_MARCA;
    String tablaContadorSalida_NUMero;
    String tablaContadorSalida_Fechalectura;
    String tablaContadorSalida_HORALECTURA;
    String tablaContadorSalida_ULTIMOMEDIDORLEIDO;
    String tablaContadorSalida_MARCAMEDIDORLEIDO;
    String tablaContadorSalida_NROregistroS;
    String tablaContadorSalida_PRIMERREGISTRO;
    String tablaContadorSalida_CLIENTE;
    String tablaContadorSalida_TIEMPO;
    String tablaContadorSalida_FUEPROMEDIADO;
    String tablaContadorSalida_ESTADOCRITICA;
    String tablaContadorSalida_FIN;

    public String gettablaContadorSalida_CUENTA() {
        return tablaContadorSalida_Cuenta;
    }

    public void settablaContadorSalida_Cuenta(String tablaContadorSalida_Cuenta) {
        this.tablaContadorSalida_Cuenta = tablaContadorSalida_Cuenta;
    }

    public String gettablaContadorSalida_MARCA() {
        return tablaContadorSalida_MARCA;
    }

    public void settablaContadorSalida_MARCA(String tablaContadorSalida_MARCA) {
        this.tablaContadorSalida_MARCA = tablaContadorSalida_MARCA;
    }

    public String gettablaContadorSalida_NUMERO() {
        return tablaContadorSalida_NUMero;
    }

    public void settablaContadorSalida_NUMero(String tablaContadorSalida_NUMero) {
        this.tablaContadorSalida_NUMero = tablaContadorSalida_NUMero;
    }

    public String gettablaContadorSalida_FECHALECTURA() {
        return tablaContadorSalida_Fechalectura;
    }

    public void settablaContadorSalida_Fechalectura(String tablaContadorSalida_Fechalectura) {
        this.tablaContadorSalida_Fechalectura = tablaContadorSalida_Fechalectura;
    }

    public String gettablaContadorSalida_HORALECTURA() {
        return tablaContadorSalida_HORALECTURA;
    }

    public void settablaContadorSalida_HORALECTURA(String tablaContadorSalida_HORALECTURA) {
        this.tablaContadorSalida_HORALECTURA = tablaContadorSalida_HORALECTURA;
    }

    public String gettablaContadorSalida_ULTIMOMEDIDORLEIDO() {
        return tablaContadorSalida_ULTIMOMEDIDORLEIDO;
    }

    public void settablaContadorSalida_ULTIMOMEDIDORLEIDO(String tablaContadorSalida_ULTIMOMEDIDORLEIDO) {
        this.tablaContadorSalida_ULTIMOMEDIDORLEIDO = tablaContadorSalida_ULTIMOMEDIDORLEIDO;
    }

    public String gettablaContadorSalida_MARCAMEDIDORLEIDO() {
        return tablaContadorSalida_MARCAMEDIDORLEIDO;
    }

    public void settablaContadorSalida_MARCAMEDIDORLEIDO(String tablaContadorSalida_MARCAMEDIDORLEIDO) {
        this.tablaContadorSalida_MARCAMEDIDORLEIDO = tablaContadorSalida_MARCAMEDIDORLEIDO;
    }

    public String gettablaContadorSalida_NROREGISTROS() {
        return tablaContadorSalida_NROregistroS;
    }

    public void settablaContadorSalida_NROregistroS(String tablaContadorSalida_NROregistroS) {
        this.tablaContadorSalida_NROregistroS = tablaContadorSalida_NROregistroS;
    }

    public String gettablaContadorSalida_PRIMERREGISTRO() {
        return tablaContadorSalida_PRIMERREGISTRO;
    }

    public void settablaContadorSalida_PRIMERREGISTRO(String tablaContadorSalida_PRIMERREGISTRO) {
        this.tablaContadorSalida_PRIMERREGISTRO = tablaContadorSalida_PRIMERREGISTRO;
    }

    public String gettablaContadorSalida_CLIENTE() {
        return tablaContadorSalida_CLIENTE;
    }

    public void settablaContadorSalida_CLIENTE(String tablaContadorSalida_CLIENTE) {
        this.tablaContadorSalida_CLIENTE = tablaContadorSalida_CLIENTE;
    }

    public String gettablaContadorSalida_TIEMPO() {
        return tablaContadorSalida_TIEMPO;
    }

    public void settablaContadorSalida_TIEMPO(String tablaContadorSalida_TIEMPO) {
        this.tablaContadorSalida_TIEMPO = tablaContadorSalida_TIEMPO;
    }

    public String gettablaContadorSalida_FUEPROMEDIADO() {
        return tablaContadorSalida_FUEPROMEDIADO;
    }

    public void settablaContadorSalida_FUEPROMEDIADO(String tablaContadorSalida_FUEPROMEDIADO) {
        this.tablaContadorSalida_FUEPROMEDIADO = tablaContadorSalida_FUEPROMEDIADO;
    }

    public String gettablaContadorSalida_ESTADOCRITICA() {
        return tablaContadorSalida_ESTADOCRITICA;
    }

    public void settablaContadorSalida_ESTADOCRITICA(String tablaContadorSalida_ESTADOCRITICA) {
        this.tablaContadorSalida_ESTADOCRITICA = tablaContadorSalida_ESTADOCRITICA;
    }

    public String gettablaContadorSalida_FIN() {
        return tablaContadorSalida_FIN;
    }

    public void settablaContadorSalida_FIN(String tablaContadorSalida_FIN) {
        this.tablaContadorSalida_FIN = tablaContadorSalida_FIN;
    }

    BufferedReader fin;
    byte[] byteArray;
    private String archivo_TablaContadorSalida;
    static final int LONGITUD_REGISTRO = 112;//114 -- CUENTA 9->10 (+1), MARCA 12->10 (-2), NROregistroS 2->1 (-1). Verificado contra MEDIDOR.SDA
    int total_TablaContadorSalida;
    int ultimo_TablaContadorSalida;
    int encontro_TablaContadorSalida;
    String buscar_TablaContadorSalida;
    String texto;
    // File ruta_sd = Environment.getExternalStorageDirectory();
    // File ruta_sd = new File('C:\\Users\\SGlobal\\Documents\\PruebasJava\\CensoSalida.txt');
    RandomAccessFile rFile;
    Utils utils = new Utils();

    public Boolean abrir_TablaContadorSalida(String nombreArchivo) {
        if (nombreArchivo.length() == 0) {
            return false;
        }
        try {
            rFile = new RandomAccessFile(nombreArchivo, "rw"); // C:/Users/SGlobal/Documents/Pruebas Java/CensoSalida.txt
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        setArchivo_TablaContadorSalida(nombreArchivo);
        return abrir(getArchivo_TablaContadorSalida());
    }

    private Boolean abrir(String nombre_archivo) {
        try {
            int fileSize = (int) rFile.length();
            byteArray = new byte[fileSize];
            rFile.readFully(byteArray, 0, fileSize);
            texto = new String(byteArray);
            total_TablaContadorSalida = fileSize / LONGITUD_REGISTRO;
            return true;
        } catch (Exception ex) {
            // Log.e('Ficheros', 'Error al leer fichero desde tarjeta SD');
            ex.printStackTrace();
            return false;
        }
    }

    public void Cerrar_TablaContadorSalida() {
        // cerrar el archivo abierto
        try {
            rFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean escribir_TablaContadorSalida(int posicion) {
        posicion_TablaContadorSalida(posicion, LONGITUD_REGISTRO);
        rellenar_TablaContadorSalida();
        String texto = tablaContadorSalida_Cuenta + sep + tablaContadorSalida_MARCA + sep + tablaContadorSalida_NUMero + sep + tablaContadorSalida_Fechalectura + sep + tablaContadorSalida_HORALECTURA
                + sep + tablaContadorSalida_ULTIMOMEDIDORLEIDO + sep + tablaContadorSalida_MARCAMEDIDORLEIDO + sep + tablaContadorSalida_NROregistroS + sep + tablaContadorSalida_PRIMERREGISTRO + sep
                + tablaContadorSalida_CLIENTE + sep + tablaContadorSalida_TIEMPO + sep + tablaContadorSalida_FUEPROMEDIADO + sep + tablaContadorSalida_ESTADOCRITICA + sep + tablaContadorSalida_FIN;
        try {
            if (null != rFile && (texto.length() == LONGITUD_REGISTRO)) {
                rFile.writeBytes(texto);
                System.out.println("Los Datos fueron grabados correctamente");
                return true;
            } else {
                File desc = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/LOGEVENTOS.LOG"); //Ax: EJ: O108010.009
                utils.EscribirLinea(desc,"CADENA DAÑADA MEDIDORES:"+"\r\n" + texto);
                System.out.println("Se presento problema al escribir en el archivo; longitud Errada: tamano texto: " + texto.length() + texto);
                return false;
            }
        } catch (IOException ioe) {
            System.out.println("Se presento problema al escribir en el archivo Longitud Errada: " + LONGITUD_REGISTRO);
            return false;
        }
    }

    public void rellenar_TablaContadorSalida() {
        try {
            tablaContadorSalida_Cuenta = String.format("%-10s", tablaContadorSalida_Cuenta);//era 9
            tablaContadorSalida_MARCA = String.format("%-10s", tablaContadorSalida_MARCA);//era 12
            tablaContadorSalida_NUMero = String.format("%-16s", tablaContadorSalida_NUMero);
            tablaContadorSalida_Fechalectura = String.format("%-8s", tablaContadorSalida_Fechalectura);
            tablaContadorSalida_HORALECTURA = String.format("%-6s", tablaContadorSalida_HORALECTURA);
            tablaContadorSalida_ULTIMOMEDIDORLEIDO = String.format("%-16s", tablaContadorSalida_ULTIMOMEDIDORLEIDO);
            tablaContadorSalida_MARCAMEDIDORLEIDO = String.format("%-12s", tablaContadorSalida_MARCAMEDIDORLEIDO);
            tablaContadorSalida_NROregistroS = String.format("%-1s", tablaContadorSalida_NROregistroS);//era 2
            tablaContadorSalida_PRIMERREGISTRO = String.format("%-5s", tablaContadorSalida_PRIMERREGISTRO);
            tablaContadorSalida_CLIENTE = String.format("%-5s", tablaContadorSalida_CLIENTE);
            tablaContadorSalida_TIEMPO = String.format("%-5s", tablaContadorSalida_TIEMPO);
            tablaContadorSalida_FUEPROMEDIADO = String.format("%-1s", tablaContadorSalida_FUEPROMEDIADO);
            tablaContadorSalida_ESTADOCRITICA = String.format("%-2s", tablaContadorSalida_ESTADOCRITICA);
            tablaContadorSalida_FIN = "\r\n";//el plano actual no lleva contenido despues de ESTADOCRITICA
        } catch (Exception e) {
            System.out.println("Se presento problema al escribir en el archivo tablaContadorSalida.dat..");
            e.printStackTrace();
        }

        return;
    }

    public void posicion_TablaContadorSalida(int registro, int tamano) {
        try {
            rFile.seek((registro - 1) * tamano);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Escribir los registros atual en el archivo en posicion
    public void lectura_TablaContadorSalida(int registro) {
        String[] campos;
        encontro_TablaContadorSalida = 0;
        posicion_TablaContadorSalida(registro, LONGITUD_REGISTRO);
        try {
            int fileSize = (int) rFile.length();
            byteArray = new byte[fileSize];
            rFile.readFully(byteArray, 0, LONGITUD_REGISTRO);
            texto = new String(byteArray);
            settablaContadorSalida_Cuenta(texto.substring(0, 10));//CUENTA ahora 10 digitos (era 9)
            settablaContadorSalida_MARCA(texto.substring(11, 21));//MARCA ahora 10 (era 12)
            settablaContadorSalida_NUMero(texto.substring(22, 38));
            settablaContadorSalida_Fechalectura(texto.substring(39, 47));
            settablaContadorSalida_HORALECTURA(texto.substring(48, 54));
            settablaContadorSalida_ULTIMOMEDIDORLEIDO(texto.substring(55, 71));
            settablaContadorSalida_MARCAMEDIDORLEIDO(texto.substring(72, 84));
            settablaContadorSalida_NROregistroS(texto.substring(85, 86));//NROregistroS ahora 1 (era 2)
            settablaContadorSalida_PRIMERREGISTRO(texto.substring(87, 92));
            settablaContadorSalida_CLIENTE(texto.substring(93, 98));
            settablaContadorSalida_TIEMPO(texto.substring(99, 104));
            settablaContadorSalida_FUEPROMEDIADO(texto.substring(105, 106));
            settablaContadorSalida_ESTADOCRITICA(texto.substring(107, 109));
            settablaContadorSalida_FIN("\r\n");//el plano actual no trae contenido despues de ESTADOCRITICA
            ultimo_TablaContadorSalida = registro;
            // fin estructura
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void buscarSecuencial_TablaContadorSalida(String codigo) {
        encontro_TablaContadorSalida = 0;
        for (int i = 0; i < (int) (total_TablaContadorSalida); i++) {
            lectura_TablaContadorSalida(i + 1);
            if (Integer.parseInt(codigo.trim()) == Integer.parseInt(tablaContadorSalida_Cuenta.trim())) {
                encontro_TablaContadorSalida = i + 1;
                posicion_TablaContadorSalida(i + 1, LONGITUD_REGISTRO);
                i = total_TablaContadorSalida + 10;
            } else {
                encontro_TablaContadorSalida = 0;
            }
        }
        return;
    }

    public String getArchivo_TablaContadorSalida() {
        return archivo_TablaContadorSalida;
    }

    public void setArchivo_TablaContadorSalida(String archivo_TablaContadorSalida) {
        this.archivo_TablaContadorSalida = archivo_TablaContadorSalida;
    }

}
