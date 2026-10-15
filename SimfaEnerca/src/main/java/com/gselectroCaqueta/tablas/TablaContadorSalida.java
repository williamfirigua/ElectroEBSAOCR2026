package com.gselectroCaqueta.tablas;

import android.util.Log;

import com.gsutil.Utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import static com.gselectroCaqueta.tablas.UtilPlanos.LATIN1;

/// <summary>
/// S_CONTAD (MEDIDOR.SDA de salida) - registro fijo de 112 bytes.
/// - Ya no carga el archivo completo en memoria (antes: ~1,1 MB al abrir y otro ~1,1 MB en CADA lectura).
/// - Buffer reutilizable + readFully + Latin-1.
/// - Acceso synchronized y verificacion de cuenta + numero de medidor antes de escribir.
/// </summary>
public class TablaContadorSalida {
    private static final String TAG = "MEDIDOR_SALIDA";
    static final int LONGITUD_REGISTRO = 112; // CUENTA 10, MARCA 10, NROregistroS 1. Verificado contra MEDIDOR.SDA

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

    BufferedReader fin;                                         // sin uso, se conserva por compatibilidad
    private final byte[] bufferLectura = new byte[LONGITUD_REGISTRO];
    private final byte[] bufferVerificacion = new byte[LONGITUD_REGISTRO];
    byte[] byteArray = bufferLectura;
    private String archivo_TablaContadorSalida;
    int total_TablaContadorSalida;
    int ultimo_TablaContadorSalida;
    int encontro_TablaContadorSalida;
    String buscar_TablaContadorSalida;
    String texto;
    RandomAccessFile rFile;
    Utils utils = new Utils();

    // ------------------------------------------------------------------ getters / setters (sin cambios de nombre)

    public String gettablaContadorSalida_CUENTA() { return tablaContadorSalida_Cuenta; }
    public void settablaContadorSalida_Cuenta(String v) { this.tablaContadorSalida_Cuenta = v; }
    public String gettablaContadorSalida_MARCA() { return tablaContadorSalida_MARCA; }
    public void settablaContadorSalida_MARCA(String v) { this.tablaContadorSalida_MARCA = v; }
    public String gettablaContadorSalida_NUMERO() { return tablaContadorSalida_NUMero; }
    public void settablaContadorSalida_NUMero(String v) { this.tablaContadorSalida_NUMero = v; }
    public String gettablaContadorSalida_FECHALECTURA() { return tablaContadorSalida_Fechalectura; }
    public void settablaContadorSalida_Fechalectura(String v) { this.tablaContadorSalida_Fechalectura = v; }
    public String gettablaContadorSalida_HORALECTURA() { return tablaContadorSalida_HORALECTURA; }
    public void settablaContadorSalida_HORALECTURA(String v) { this.tablaContadorSalida_HORALECTURA = v; }
    public String gettablaContadorSalida_ULTIMOMEDIDORLEIDO() { return tablaContadorSalida_ULTIMOMEDIDORLEIDO; }
    public void settablaContadorSalida_ULTIMOMEDIDORLEIDO(String v) { this.tablaContadorSalida_ULTIMOMEDIDORLEIDO = v; }
    public String gettablaContadorSalida_MARCAMEDIDORLEIDO() { return tablaContadorSalida_MARCAMEDIDORLEIDO; }
    public void settablaContadorSalida_MARCAMEDIDORLEIDO(String v) { this.tablaContadorSalida_MARCAMEDIDORLEIDO = v; }
    public String gettablaContadorSalida_NROREGISTROS() { return tablaContadorSalida_NROregistroS; }
    public void settablaContadorSalida_NROregistroS(String v) { this.tablaContadorSalida_NROregistroS = v; }
    public String gettablaContadorSalida_PRIMERREGISTRO() { return tablaContadorSalida_PRIMERREGISTRO; }
    public void settablaContadorSalida_PRIMERREGISTRO(String v) { this.tablaContadorSalida_PRIMERREGISTRO = v; }
    public String gettablaContadorSalida_CLIENTE() { return tablaContadorSalida_CLIENTE; }
    public void settablaContadorSalida_CLIENTE(String v) { this.tablaContadorSalida_CLIENTE = v; }
    public String gettablaContadorSalida_TIEMPO() { return tablaContadorSalida_TIEMPO; }
    public void settablaContadorSalida_TIEMPO(String v) { this.tablaContadorSalida_TIEMPO = v; }
    public String gettablaContadorSalida_FUEPROMEDIADO() { return tablaContadorSalida_FUEPROMEDIADO; }
    public void settablaContadorSalida_FUEPROMEDIADO(String v) { this.tablaContadorSalida_FUEPROMEDIADO = v; }
    public String gettablaContadorSalida_ESTADOCRITICA() { return tablaContadorSalida_ESTADOCRITICA; }
    public void settablaContadorSalida_ESTADOCRITICA(String v) { this.tablaContadorSalida_ESTADOCRITICA = v; }
    public String gettablaContadorSalida_FIN() { return tablaContadorSalida_FIN; }
    public void settablaContadorSalida_FIN(String v) { this.tablaContadorSalida_FIN = v; }
    public String getArchivo_TablaContadorSalida() { return archivo_TablaContadorSalida; }
    public void setArchivo_TablaContadorSalida(String v) { this.archivo_TablaContadorSalida = v; }

    // ------------------------------------------------------------------ apertura / cierre

    public synchronized Boolean abrir_TablaContadorSalida(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.length() == 0) return false;
        cerrarSilencioso();
        try {
            rFile = new RandomAccessFile(nombreArchivo, "rw");
            archivo_TablaContadorSalida = nombreArchivo;
            total_TablaContadorSalida = (int) (rFile.length() / LONGITUD_REGISTRO); // ya NO se lee el archivo completo
            return true;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "al abrir " + nombreArchivo + ": " + e.getMessage());
            return false;
        } catch (IOException e) {
            Log.e(TAG, "al abrir1 " + e.getMessage());
            return false;
        }
    }

    public synchronized void Cerrar_TablaContadorSalida() {
        cerrarSilencioso();
    }

    private void cerrarSilencioso() {
        if (rFile == null) return;
        try {
            rFile.close();
        } catch (IOException e) {
            Log.e(TAG, "al cerrar " + e.getMessage());
        }
        rFile = null;
    }

    public synchronized void posicion_TablaContadorSalida(int registro, int tamano) {
        try {
            rFile.seek((long) (registro - 1) * tamano);
        } catch (IOException e) {
            Log.e(TAG, "seek " + registro + ": " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ lectura

    public synchronized void lectura_TablaContadorSalida(int registro) {
        encontro_TablaContadorSalida = 0;
        if (rFile == null || registro < 1 || registro > total_TablaContadorSalida) {
            Log.e(TAG, "lectura fuera de rango o archivo cerrado: " + registro + " de " + total_TablaContadorSalida);
            return;
        }
        try {
            rFile.seek((long) (registro - 1) * LONGITUD_REGISTRO);
            rFile.readFully(bufferLectura);
        } catch (IOException e) {
            Log.e(TAG, "lectura fallida en posicion " + registro + ": " + e.getMessage());
            return;
        }
        byte[] b = bufferLectura;
        texto = null;
        tablaContadorSalida_Cuenta = new String(b, 0, 10, LATIN1);
        tablaContadorSalida_MARCA = new String(b, 11, 10, LATIN1);
        tablaContadorSalida_NUMero = new String(b, 22, 16, LATIN1);
        tablaContadorSalida_Fechalectura = new String(b, 39, 8, LATIN1);
        tablaContadorSalida_HORALECTURA = new String(b, 48, 6, LATIN1);
        tablaContadorSalida_ULTIMOMEDIDORLEIDO = new String(b, 55, 16, LATIN1);
        tablaContadorSalida_MARCAMEDIDORLEIDO = new String(b, 72, 12, LATIN1);
        tablaContadorSalida_NROregistroS = new String(b, 85, 1, LATIN1);
        tablaContadorSalida_PRIMERREGISTRO = new String(b, 87, 5, LATIN1);
        tablaContadorSalida_CLIENTE = new String(b, 93, 5, LATIN1);
        tablaContadorSalida_TIEMPO = new String(b, 99, 5, LATIN1);
        tablaContadorSalida_FUEPROMEDIADO = new String(b, 105, 1, LATIN1);
        tablaContadorSalida_ESTADOCRITICA = new String(b, 107, 2, LATIN1);
        tablaContadorSalida_FIN = "\r\n";
        ultimo_TablaContadorSalida = registro;
    }

    // ------------------------------------------------------------------ escritura

    /** Solo escribe si en disco, en esa posicion, esta la misma cuenta y el mismo numero de medidor. */
    public synchronized boolean escribir_TablaContadorSalida(int posicion) {
        if (!enRango(posicion)) return false;
        try {
            rFile.seek((long) (posicion - 1) * LONGITUD_REGISTRO);
            rFile.readFully(bufferVerificacion);
        } catch (IOException e) {
            bloqueo(posicion, "no se pudo verificar: " + e.getMessage());
            return false;
        }
        String cuentaDisco = new String(bufferVerificacion, 0, 10, LATIN1);
        String numeroDisco = new String(bufferVerificacion, 22, 16, LATIN1);
        if (!UtilPlanos.mismoValor(cuentaDisco, tablaContadorSalida_Cuenta)
                || !numeroDisco.trim().equals(tablaContadorSalida_NUMero == null ? "" : tablaContadorSalida_NUMero.trim())) {
            bloqueo(posicion, "en disco esta " + cuentaDisco.trim() + "/" + numeroDisco.trim());
            return false;
        }
        return escribirEnPosicion(posicion);
    }

    /** Escribe sin verificar (solo para reparaciones explicitas). */
    public synchronized boolean escribirForzado_TablaContadorSalida(int posicion) {
        if (!enRango(posicion)) return false;
        Log.w(TAG, "Escritura forzada en posicion " + posicion + " cuenta " + tablaContadorSalida_Cuenta);
        return escribirEnPosicion(posicion);
    }

    private boolean enRango(int posicion) {
        if (rFile == null || posicion < 1 || posicion > total_TablaContadorSalida) {
            bloqueo(posicion, "fuera de rango o archivo cerrado (total " + total_TablaContadorSalida + ")");
            return false;
        }
        return true;
    }

    private boolean escribirEnPosicion(int posicion) {
        rellenar_TablaContadorSalida();
        String linea = tablaContadorSalida_Cuenta + sep + tablaContadorSalida_MARCA + sep + tablaContadorSalida_NUMero + sep + tablaContadorSalida_Fechalectura + sep + tablaContadorSalida_HORALECTURA
                + sep + tablaContadorSalida_ULTIMOMEDIDORLEIDO + sep + tablaContadorSalida_MARCAMEDIDORLEIDO + sep + tablaContadorSalida_NROregistroS + sep + tablaContadorSalida_PRIMERREGISTRO + sep
                + tablaContadorSalida_CLIENTE + sep + tablaContadorSalida_TIEMPO + sep + tablaContadorSalida_FUEPROMEDIADO + sep + tablaContadorSalida_ESTADOCRITICA + sep + tablaContadorSalida_FIN;
        if (linea.length() != LONGITUD_REGISTRO) {
            UtilPlanos.evento(TAG, "CADENA DAÑADA MEDIDORES (longitud " + linea.length() + ", posicion " + posicion + "):\r\n" + linea);
            return false;
        }
        try {
            rFile.seek((long) (posicion - 1) * LONGITUD_REGISTRO);
            rFile.write(linea.getBytes(LATIN1));
            return true;
        } catch (IOException e) {
            Log.e(TAG, "escritura fallida en posicion " + posicion + ": " + e.getMessage());
            return false;
        }
    }

    private void bloqueo(int posicion, String motivo) {
        UtilPlanos.evento(TAG, "[BLOQUEADO MEDIDOR] cuenta " + (tablaContadorSalida_Cuenta == null ? "" : tablaContadorSalida_Cuenta.trim())
                + " medidor " + (tablaContadorSalida_NUMero == null ? "" : tablaContadorSalida_NUMero.trim())
                + " intento escribir en posicion " + posicion + " -> " + motivo);
    }

    public synchronized void rellenar_TablaContadorSalida() {
        tablaContadorSalida_Cuenta = String.format("%-10s", tablaContadorSalida_Cuenta);
        tablaContadorSalida_MARCA = String.format("%-10s", tablaContadorSalida_MARCA);
        tablaContadorSalida_NUMero = String.format("%-16s", tablaContadorSalida_NUMero);
        tablaContadorSalida_Fechalectura = String.format("%-8s", tablaContadorSalida_Fechalectura);
        tablaContadorSalida_HORALECTURA = String.format("%-6s", tablaContadorSalida_HORALECTURA);
        tablaContadorSalida_ULTIMOMEDIDORLEIDO = String.format("%-16s", tablaContadorSalida_ULTIMOMEDIDORLEIDO);
        tablaContadorSalida_MARCAMEDIDORLEIDO = String.format("%-12s", tablaContadorSalida_MARCAMEDIDORLEIDO);
        tablaContadorSalida_NROregistroS = String.format("%-1s", tablaContadorSalida_NROregistroS);
        tablaContadorSalida_PRIMERREGISTRO = String.format("%-5s", tablaContadorSalida_PRIMERREGISTRO);
        tablaContadorSalida_CLIENTE = String.format("%-5s", tablaContadorSalida_CLIENTE);
        tablaContadorSalida_TIEMPO = String.format("%-5s", tablaContadorSalida_TIEMPO);
        tablaContadorSalida_FUEPROMEDIADO = String.format("%-1s", tablaContadorSalida_FUEPROMEDIADO);
        tablaContadorSalida_ESTADOCRITICA = String.format("%-2s", tablaContadorSalida_ESTADOCRITICA);
        tablaContadorSalida_FIN = "\r\n";
    }

    // ------------------------------------------------------------------ busqueda

    public synchronized void buscarSecuencial_TablaContadorSalida(String codigo) {
        encontro_TablaContadorSalida = 0;
        String buscado = UtilPlanos.normalizar(codigo);
        for (int i = 1; i <= total_TablaContadorSalida; i++) {
            lectura_TablaContadorSalida(i);
            if (buscado.equals(UtilPlanos.normalizar(tablaContadorSalida_Cuenta))) {
                encontro_TablaContadorSalida = i;
                return;
            }
        }
    }
}
