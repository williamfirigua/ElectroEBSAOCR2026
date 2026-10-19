package com.gselectroCaqueta.tablas;

import android.util.Log;

import com.gsutil.Utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.RandomAccessFile;

import static com.gselectroCaqueta.tablas.UtilPlanos.LATIN1;

/// <summary>
/// S_CLIENT (CLIENTE.SDA de salida) - registro fijo de 263 bytes (incluye TOTALFACTURAASEO).
/// - Buffer reutilizable + readFully + Latin-1, acceso synchronized.
/// - Antes de escribir verifica en disco que la posicion sea de la MISMA cuenta.
/// - lectura_TablaClienteSalidaII (estadistica) sigue leyendo solo CUENTA e INDFACTURACION, sin crear buffers.
/// </summary>
public class TablaClienteSalida {
    private static final String TAG = "CLIENTE_SALIDA";
    static final int LONGITUD_REGISTRO = 252 + 11; // +11 por TOTALFACTURAASEO. Verificado contra CLIENTE.SDA

    String sep = ";";
    String tablaClienteSalida_ANIO;
    String tablaClienteSalida_MES;
    String tablaClienteSalida_CUENTA;
    String tablaClienteSalida_ACTIVIDAD;
    String tablaClienteSalida_INDFACTURACION;
    String tablaClienteSalida_NROIMPRESIONES;
    String tablaClienteSalida_IMPRESORA;
    String tablaClienteSalida_LECTOR;
    String tablaClienteSalida_TERMINAL;
    String tablaClienteSalida_VALORFACTURADO;
    String tablaClienteSalida_CONTRIBUCIONENERGIA;
    private String tablaClienteSalida_CONSUMOFACTURADO;
    String tablaClienteSalida_HORAIMPRESION;
    String tablaClienteSalida_FECHAIMPRESION;
    String tablaClienteSalida_Nromedidores;
    String tablaClienteSalida_primermedidor;
    String tablaClienteSalida_consumo1;
    String tablaClienteSalida_consumo2;
    String tablaClienteSalida_consumo3;
    String tablaClienteSalida_valor1;
    String tablaClienteSalida_valor2;
    String tablaClienteSalida_valor3;
    String tablaClienteSalida_fechavence;
    String tablaClienteSalida_fechacorte;
    String tablaClienteSalida_nombrefoto;
    String tablaClienteSalida_distanciacalculada;
    String tablaClienteSalida_VALORFACTURADOASEO;
    String tablaClienteSalida_FIN;

    BufferedReader fin;                                         // sin uso, se conserva por compatibilidad
    public LineNumberReader rdr;                                // sin uso, se conserva por compatibilidad
    private final byte[] bufferLectura = new byte[LONGITUD_REGISTRO];
    private final byte[] bufferVerificacion = new byte[LONGITUD_REGISTRO];
    private final byte[] bufferEstadistica = new byte[26];      // solo hasta INDFACTURACION (bytes 0..25)
    byte[] byteArray = bufferLectura;
    String archivo_TablaClienteSalida;
    private int total_TablaClienteSalida;
    int ultimo_TablaClienteSalida;
    int encontro_TablaClienteSalida;
    String buscar_TablaClienteSalida;
    String texto;
    RandomAccessFile rFile;
    Utils utils = new Utils();

    // ------------------------------------------------------------------ getters / setters (sin cambios de nombre)

    public String gettablaClienteSalida_ANIO() { return tablaClienteSalida_ANIO; }
    public void settablaClienteSalida_ANIO(String v) { this.tablaClienteSalida_ANIO = v; }
    public String gettablaClienteSalida_MES() { return tablaClienteSalida_MES; }
    public void settablaClienteSalida_MES(String v) { this.tablaClienteSalida_MES = v; }
    public String gettablaClienteSalida_CUENTA() { return tablaClienteSalida_CUENTA; }
    public void settablaClienteSalida_CUENTA(String v) { this.tablaClienteSalida_CUENTA = v; }
    public String gettablaClienteSalida_ACTIVIDAD() { return tablaClienteSalida_ACTIVIDAD; }
    public void settablaClienteSalida_ACTIVIDAD(String v) { this.tablaClienteSalida_ACTIVIDAD = v; }
    public String gettablaClienteSalida_INDFACTURACION() { return tablaClienteSalida_INDFACTURACION; }
    public void settablaClienteSalida_INDFACTURACION(String v) { this.tablaClienteSalida_INDFACTURACION = v; }
    public String gettablaClienteSalida_NROIMPRESIONES() { return tablaClienteSalida_NROIMPRESIONES; }
    public void settablaClienteSalida_NROIMPRESIONES(String v) { this.tablaClienteSalida_NROIMPRESIONES = v; }
    public String gettablaClienteSalida_IMPRESORA() { return tablaClienteSalida_IMPRESORA; }
    public void settablaClienteSalida_IMPRESORA(String v) { this.tablaClienteSalida_IMPRESORA = v; }
    public String gettablaClienteSalida_LECTOR() { return tablaClienteSalida_LECTOR; }
    public void settablaClienteSalida_LECTOR(String v) { this.tablaClienteSalida_LECTOR = v; }
    public String gettablaClienteSalida_TERMINAL() { return tablaClienteSalida_TERMINAL; }
    public void settablaClienteSalida_TERMINAL(String v) { this.tablaClienteSalida_TERMINAL = v; }
    public String gettablaClienteSalida_VALORFACTURADO() { return tablaClienteSalida_VALORFACTURADO; }
    public void settablaClienteSalida_VALORFACTURADO(String v) { this.tablaClienteSalida_VALORFACTURADO = v; }
    public String gettablaClienteSalida_CONTRIBUCIONENERGIA() { return tablaClienteSalida_CONTRIBUCIONENERGIA; }
    public void settablaClienteSalida_CONTRIBUCIONENERGIA(String v) { this.tablaClienteSalida_CONTRIBUCIONENERGIA = v; }
    public String gettablaClienteSalida_CONSUMOFACTURADO() { return getTablaClienteSalida_CONSUMOFACTURADO(); }
    public void settablaClienteSalida_CONSUMOFACTURADO(String v) { setTablaClienteSalida_CONSUMOFACTURADO(v); }
    public String getTablaClienteSalida_CONSUMOFACTURADO() { return tablaClienteSalida_CONSUMOFACTURADO; }
    public void setTablaClienteSalida_CONSUMOFACTURADO(String v) { this.tablaClienteSalida_CONSUMOFACTURADO = v; }
    public String gettablaClienteSalida_HORAIMPRESION() { return tablaClienteSalida_HORAIMPRESION; }
    public void settablaClienteSalida_HORAIMPRESION(String v) { this.tablaClienteSalida_HORAIMPRESION = v; }
    public String gettablaClienteSalida_FECHAIMPRESION() { return tablaClienteSalida_FECHAIMPRESION; }
    public void settablaClienteSalida_FECHAIMPRESION(String v) { this.tablaClienteSalida_FECHAIMPRESION = v; }
    public String gettablaClienteSalida_NROMEDIDORES() { return tablaClienteSalida_Nromedidores; }
    public void settablaClienteSalida_Nromedidores(String v) { this.tablaClienteSalida_Nromedidores = v; }
    public String gettablaClienteSalida_PRIMERMEDIDOR() { return tablaClienteSalida_primermedidor; }
    public void settablaClienteSalida_primermedidor(String v) { this.tablaClienteSalida_primermedidor = v; }
    public String gettablaClienteSalida_CONSUMO1() { return tablaClienteSalida_consumo1; }
    public void settablaClienteSalida_consumo1(String v) { this.tablaClienteSalida_consumo1 = v; }
    public String gettablaClienteSalida_CONSUMO2() { return tablaClienteSalida_consumo2; }
    public void settablaClienteSalida_consumo2(String v) { this.tablaClienteSalida_consumo2 = v; }
    public String gettablaClienteSalida_CONSUMO3() { return tablaClienteSalida_consumo3; }
    public void settablaClienteSalida_consumo3(String v) { this.tablaClienteSalida_consumo3 = v; }
    public String gettablaClienteSalida_VALOR1() { return tablaClienteSalida_valor1; }
    public void settablaClienteSalida_valor1(String v) { this.tablaClienteSalida_valor1 = v; }
    public String gettablaClienteSalida_VALOR2() { return tablaClienteSalida_valor2; }
    public void settablaClienteSalida_valor2(String v) { this.tablaClienteSalida_valor2 = v; }
    public String gettablaClienteSalida_VALOR3() { return tablaClienteSalida_valor3; }
    public void settablaClienteSalida_valor3(String v) { this.tablaClienteSalida_valor3 = v; }
    public String gettablaClienteSalida_FECHAVENCE() { return tablaClienteSalida_fechavence; }
    public void settablaClienteSalida_fechavence(String v) { this.tablaClienteSalida_fechavence = v; }
    public String gettablaClienteSalida_FECHACORTE() { return tablaClienteSalida_fechacorte; }
    public void settablaClienteSalida_fechacorte(String v) { this.tablaClienteSalida_fechacorte = v; }
    public String gettablaClienteSalida_NOMBREFOTO() { return tablaClienteSalida_nombrefoto; }
    public void settablaClienteSalida_nombrefoto(String v) { this.tablaClienteSalida_nombrefoto = v; }
    public String gettablaClienteSalida_DISTANCIACALCULADA() { return tablaClienteSalida_distanciacalculada; }
    public void settablaClienteSalida_distanciacalculada(String v) { this.tablaClienteSalida_distanciacalculada = v; }
    public String gettablaClienteSalida_VALORFACTURADOASEO() { return tablaClienteSalida_VALORFACTURADOASEO; }
    public void settablaClienteSalida_VALORFACTURADOASEO(String v) { this.tablaClienteSalida_VALORFACTURADOASEO = v; }
    public String gettablaClienteSalida_FIN() { return tablaClienteSalida_FIN; }
    public void settablaClienteSalida_FIN(String v) { this.tablaClienteSalida_FIN = v; }
    public String getArchivo_TablaClienteSalida() { return archivo_TablaClienteSalida; }
    public void setArchivo_TablaClienteSalida(String v) { this.archivo_TablaClienteSalida = v; }
    public synchronized int getTotal_TablaClienteSalida() { return total_TablaClienteSalida; }
    public synchronized void setTotal_TablaClienteSalida(int v) { this.total_TablaClienteSalida = v; }

    // ------------------------------------------------------------------ apertura / cierre

    public synchronized Boolean abrir_TablaClienteSalida(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.length() == 0) return false;
        cerrarSilencioso();
        try {
            rFile = new RandomAccessFile(nombreArchivo, "rw");
            archivo_TablaClienteSalida = nombreArchivo;
            total_TablaClienteSalida = (int) (rFile.length() / LONGITUD_REGISTRO);
            return true;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "al abrir " + nombreArchivo + ": " + e.getMessage());
            return false;
        } catch (IOException e) {
            Log.e(TAG, "al abrir1 " + e.getMessage());
            return false;
        }
    }

    public synchronized void Cerrar_TablaClienteSalida() {
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

    public synchronized void posicion_TablaClienteSalida(int registro, int tamano) {
        try {
            if (registro == 0) {
                Log.e(TAG, "posicion 0 solicitada; se usa 1 (revisar el llamador)");
                registro = 1;
            }
            rFile.seek((long) (registro - 1) * tamano);
        } catch (IOException e) {
            Log.e(TAG, "seek " + registro + ": " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ lectura

    public synchronized void lectura_TablaClienteSalida(int registro) {
        encontro_TablaClienteSalida = 0;
        if (registro == 0) {
            Log.e(TAG, "lectura en posicion 0 solicitada; se usa 1 (revisar el llamador)");
            registro = 1; // mismo comportamiento que la version anterior
        }
        if (rFile == null || registro < 1 || registro > total_TablaClienteSalida) {
            Log.e(TAG, "lectura fuera de rango o archivo cerrado: " + registro + " de " + total_TablaClienteSalida);
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
        tablaClienteSalida_ANIO = new String(b, 0, 4, LATIN1);
        tablaClienteSalida_MES = new String(b, 5, 2, LATIN1);
        tablaClienteSalida_CUENTA = new String(b, 8, 10, LATIN1);
        tablaClienteSalida_ACTIVIDAD = new String(b, 19, 4, LATIN1);
        tablaClienteSalida_INDFACTURACION = new String(b, 24, 1, LATIN1);
        tablaClienteSalida_NROIMPRESIONES = new String(b, 26, 1, LATIN1);
        tablaClienteSalida_IMPRESORA = new String(b, 28, 15, LATIN1);
        tablaClienteSalida_LECTOR = new String(b, 44, 10, LATIN1);
        tablaClienteSalida_TERMINAL = new String(b, 55, 15, LATIN1);
        tablaClienteSalida_VALORFACTURADO = new String(b, 71, 10, LATIN1);
        tablaClienteSalida_CONTRIBUCIONENERGIA = new String(b, 82, 10, LATIN1);
        tablaClienteSalida_CONSUMOFACTURADO = new String(b, 93, 10, LATIN1);
        tablaClienteSalida_HORAIMPRESION = new String(b, 104, 6, LATIN1);
        tablaClienteSalida_FECHAIMPRESION = new String(b, 111, 8, LATIN1);
        tablaClienteSalida_Nromedidores = new String(b, 120, 1, LATIN1);
        tablaClienteSalida_primermedidor = new String(b, 122, 5, LATIN1);
        tablaClienteSalida_consumo1 = new String(b, 128, 10, LATIN1);
        tablaClienteSalida_consumo2 = new String(b, 139, 10, LATIN1);
        tablaClienteSalida_consumo3 = new String(b, 150, 10, LATIN1);
        tablaClienteSalida_valor1 = new String(b, 161, 10, LATIN1);
        tablaClienteSalida_valor2 = new String(b, 172, 10, LATIN1);
        tablaClienteSalida_valor3 = new String(b, 183, 10, LATIN1);
        tablaClienteSalida_fechavence = new String(b, 194, 12, LATIN1);
        tablaClienteSalida_fechacorte = new String(b, 207, 12, LATIN1);
        tablaClienteSalida_nombrefoto = new String(b, 220, 18, LATIN1);
        tablaClienteSalida_distanciacalculada = new String(b, 239, 10, LATIN1);
        tablaClienteSalida_VALORFACTURADOASEO = new String(b, 250, 10, LATIN1);
        tablaClienteSalida_FIN = "\r\n";
        ultimo_TablaClienteSalida = registro;
    }

    // ------------------------------------------------------------------ lectura para estadistica (solo 2 campos)

    public synchronized void inicializaBloque() {
        // Ya no se abre un segundo lector: la lectura II usa el mismo archivo y un buffer de 26 bytes.
    }

    /**
     * Solo CUENTA e INDFACTURACION (estadistica). Lee 26 bytes, no el registro completo.
     * Los demas campos NO se tocan: no escribir despues de esta lectura (el candado lo bloquearia igual).
     */
    public synchronized void lectura_TablaClienteSalidaII(int x) {
        encontro_TablaClienteSalida = 0;
        if (rFile == null || x < 1 || x > total_TablaClienteSalida) {
            Log.e(TAG, "lecturaII fuera de rango o archivo cerrado: " + x + " de " + total_TablaClienteSalida);
            return;
        }
        try {
            rFile.seek((long) (x - 1) * LONGITUD_REGISTRO);
            rFile.readFully(bufferEstadistica);
        } catch (IOException e) {
            Log.e(TAG, "lecturaII fallida en posicion " + x + ": " + e.getMessage());
            return;
        }
        tablaClienteSalida_CUENTA = new String(bufferEstadistica, 8, 10, LATIN1);
        tablaClienteSalida_INDFACTURACION = new String(bufferEstadistica, 24, 1, LATIN1);
        ultimo_TablaClienteSalida = x;
    }

    public synchronized void terminaBloque() {
        // Nada que cerrar: la lectura II usa el archivo ya abierto.
    }

    // ------------------------------------------------------------------ escritura

    /** Solo escribe si en disco, en esa posicion, esta la misma cuenta. */
    public synchronized boolean escribir_TablaClienteSalida(int posicion) {
        if (!enRango(posicion)) return false;
        try {
            rFile.seek((long) (posicion - 1) * LONGITUD_REGISTRO);
            rFile.readFully(bufferVerificacion);
        } catch (IOException e) {
            bloqueo(posicion, "no se pudo verificar: " + e.getMessage());
            return false;
        }
        String cuentaDisco = new String(bufferVerificacion, 8, 10, LATIN1);
        if (!UtilPlanos.mismoValor(cuentaDisco, tablaClienteSalida_CUENTA)) {
            bloqueo(posicion, "en disco esta la cuenta " + cuentaDisco.trim());
            return false;
        }
        return escribirEnPosicion(posicion);
    }

    /** Escribe sin verificar (solo para reparaciones explicitas). */
    public synchronized boolean escribirForzado_TablaClienteSalida(int posicion) {
        if (!enRango(posicion)) return false;
        Log.w(TAG, "Escritura forzada en posicion " + posicion + " cuenta " + tablaClienteSalida_CUENTA);
        return escribirEnPosicion(posicion);
    }

    private boolean enRango(int posicion) {
        if (rFile == null || posicion < 1 || posicion > total_TablaClienteSalida) {
            bloqueo(posicion, "fuera de rango o archivo cerrado (total " + total_TablaClienteSalida + ")");
            return false;
        }
        return true;
    }

    private boolean escribirEnPosicion(int posicion) {
        rellenar_TablaClienteSalida();
        String linea = tablaClienteSalida_ANIO + sep + tablaClienteSalida_MES + sep + tablaClienteSalida_CUENTA + sep + tablaClienteSalida_ACTIVIDAD + sep + tablaClienteSalida_INDFACTURACION + sep
                + tablaClienteSalida_NROIMPRESIONES + sep + tablaClienteSalida_IMPRESORA + sep + tablaClienteSalida_LECTOR + sep + tablaClienteSalida_TERMINAL + sep
                + tablaClienteSalida_VALORFACTURADO + sep + tablaClienteSalida_CONTRIBUCIONENERGIA + sep + tablaClienteSalida_CONSUMOFACTURADO + sep + tablaClienteSalida_HORAIMPRESION + sep
                + tablaClienteSalida_FECHAIMPRESION + sep + tablaClienteSalida_Nromedidores + sep + tablaClienteSalida_primermedidor + sep + tablaClienteSalida_consumo1 + sep
                + tablaClienteSalida_consumo2 + sep + tablaClienteSalida_consumo3 + sep + tablaClienteSalida_valor1 + sep + tablaClienteSalida_valor2 + sep + tablaClienteSalida_valor3 + sep
                + tablaClienteSalida_fechavence + sep + tablaClienteSalida_fechacorte + sep + tablaClienteSalida_nombrefoto + sep + tablaClienteSalida_distanciacalculada + sep
                + tablaClienteSalida_VALORFACTURADOASEO + sep + tablaClienteSalida_FIN;
        if (linea.length() != LONGITUD_REGISTRO) {
            UtilPlanos.evento(TAG, "CADENA DAÑADA CLIENTE (longitud " + linea.length() + ", posicion " + posicion + "):\r\n" + linea);
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
        UtilPlanos.evento(TAG, "[BLOQUEADO CLIENTE] cuenta " + (tablaClienteSalida_CUENTA == null ? "" : tablaClienteSalida_CUENTA.trim())
                + " intento escribir en posicion " + posicion + " -> " + motivo+"\r\n");
    }

    public synchronized void rellenar_TablaClienteSalida() {
        tablaClienteSalida_ANIO = String.format("%-4s", tablaClienteSalida_ANIO);
        tablaClienteSalida_MES = String.format("%-2s", tablaClienteSalida_MES);
        tablaClienteSalida_CUENTA = String.format("%-10s", tablaClienteSalida_CUENTA);
        tablaClienteSalida_ACTIVIDAD = String.format("%-4s", tablaClienteSalida_ACTIVIDAD);
        tablaClienteSalida_INDFACTURACION = String.format("%-1s", tablaClienteSalida_INDFACTURACION);
        tablaClienteSalida_NROIMPRESIONES = String.format("%-1s", tablaClienteSalida_NROIMPRESIONES);
        tablaClienteSalida_IMPRESORA = String.format("%-15s", tablaClienteSalida_IMPRESORA);
        tablaClienteSalida_LECTOR = String.format("%-10s", tablaClienteSalida_LECTOR);
        tablaClienteSalida_TERMINAL = String.format("%-15s", tablaClienteSalida_TERMINAL);
        tablaClienteSalida_VALORFACTURADO = String.format("%-10s", tablaClienteSalida_VALORFACTURADO);
        tablaClienteSalida_CONTRIBUCIONENERGIA = String.format("%-10s", tablaClienteSalida_CONTRIBUCIONENERGIA);
        tablaClienteSalida_CONSUMOFACTURADO = String.format("%-10s", tablaClienteSalida_CONSUMOFACTURADO);
        tablaClienteSalida_HORAIMPRESION = String.format("%-6s", tablaClienteSalida_HORAIMPRESION);
        tablaClienteSalida_FECHAIMPRESION = String.format("%-8s", tablaClienteSalida_FECHAIMPRESION);
        tablaClienteSalida_Nromedidores = String.format("%-1s", tablaClienteSalida_Nromedidores);
        tablaClienteSalida_primermedidor = String.format("%-5s", tablaClienteSalida_primermedidor);
        tablaClienteSalida_consumo1 = String.format("%-10s", tablaClienteSalida_consumo1);
        tablaClienteSalida_consumo2 = String.format("%-10s", tablaClienteSalida_consumo2);
        tablaClienteSalida_consumo3 = String.format("%-10s", tablaClienteSalida_consumo3);
        tablaClienteSalida_valor1 = String.format("%-10s", tablaClienteSalida_valor1);
        tablaClienteSalida_valor2 = String.format("%-10s", tablaClienteSalida_valor2);
        tablaClienteSalida_valor3 = String.format("%-10s", tablaClienteSalida_valor3);
        tablaClienteSalida_fechavence = String.format("%-12s", tablaClienteSalida_fechavence);
        tablaClienteSalida_fechacorte = String.format("%-12s", tablaClienteSalida_fechacorte);
        tablaClienteSalida_nombrefoto = String.format("%-18s", tablaClienteSalida_nombrefoto);
        tablaClienteSalida_distanciacalculada = String.format("%-10s", tablaClienteSalida_distanciacalculada);
        tablaClienteSalida_VALORFACTURADOASEO = String.format("%-10s", tablaClienteSalida_VALORFACTURADOASEO);
        tablaClienteSalida_FIN = "\r\n";
    }

    // ------------------------------------------------------------------ busqueda (por LECTOR, como la original)

    public synchronized void buscarSecuencial_TablaClienteSalida(String codigo) {
        encontro_TablaClienteSalida = 0;
        String buscado = UtilPlanos.normalizar(codigo);
        for (int i = 1; i <= total_TablaClienteSalida; i++) {
            lectura_TablaClienteSalida(i);
            if (buscado.equals(UtilPlanos.normalizar(tablaClienteSalida_LECTOR))) {
                encontro_TablaClienteSalida = i;
                return;
            }
        }
    }
}
