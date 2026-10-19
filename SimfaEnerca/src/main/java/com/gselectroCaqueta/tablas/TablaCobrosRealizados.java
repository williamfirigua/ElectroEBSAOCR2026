package com.gselectroCaqueta.tablas;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import static com.gselectroCaqueta.tablas.UtilPlanos.LATIN1;

/// <summary>
/// S_COBRO (COBROS de salida) - registro fijo de 113 bytes.
/// - Buffer reutilizable + readFully + Latin-1, acceso synchronized.
/// - Antes de escribir verifica en disco que la posicion sea de la MISMA cuenta.
///   (No se verifica el concepto porque el liquidador lo cambia a proposito: 607/608, vacio -> "0".)
/// </summary>
public class TablaCobrosRealizados { //Ax ya es: S_COBRO
    private static final String TAG = "COBROS_SALIDA";
    public static final int LONGITUD_REGISTRO = 113; // Nrocuentacliente de 10 digitos (2026)

    String sep = ";";
    String tablaCobrosRealizados_Nrocuentacliente;
    String tablaCobrosRealizados_Conceptodecobro;
    String tablaCobrosRealizados_Indicadoractividad;
    String tablaCobrosRealizados_Valor;
    String tablaCobrosRealizados_saldopendiente;
    String tablaCobrosRealizados_cuotaspendientes;
    String tablaCobrosRealizados_idconcepto;
    String tablaCobrosRealizados_idconvenio;
    String tablaCobrosRealizados_primerconvenio;
    String tablaCobrosRealizados_nroconvenios;
    String tablaCobrosRealizados_porcentaje;
    String tablaCobrosRealizados_rangominimo;
    String tablaCobrosRealizados_rangomaximo;
    String tablaCobrosRealizados_periodofacturado;
    String tablaCobrosRealizados_FIN;

    BufferedReader fin;                                         // sin uso, se conserva por compatibilidad
    private final byte[] bufferLectura = new byte[LONGITUD_REGISTRO];
    private final byte[] bufferVerificacion = new byte[LONGITUD_REGISTRO];
    byte[] byteArray = bufferLectura;
    int total_TablaCobrosRealizados;
    int ultimo_TablaCobrosRealizados;
    int encontro_TablaCobrosRealizados;
    String buscar_TablaCobrosRealizados;
    String texto;
    RandomAccessFile rFile;
    private String archivo_TablaCobrosRealizados;

    // ------------------------------------------------------------------ getters / setters (sin cambios de nombre)

    public String gettablaCobrosRealizados_NROCUENTACLIENTE() { return tablaCobrosRealizados_Nrocuentacliente; }
    public void settablaCobrosRealizados_Nrocuentacliente(String v) { this.tablaCobrosRealizados_Nrocuentacliente = v; }
    public String gettablaCobrosRealizados_CONCEPTODECOBRO() { return tablaCobrosRealizados_Conceptodecobro; }
    public void settablaCobrosRealizados_Conceptodecobro(String v) { this.tablaCobrosRealizados_Conceptodecobro = v; }
    public String gettablaCobrosRealizados_INDICADORACTIVIDAD() { return tablaCobrosRealizados_Indicadoractividad; }
    public void settablaCobrosRealizados_Indicadoractividad(String v) { this.tablaCobrosRealizados_Indicadoractividad = v; }
    public String gettablaCobrosRealizados_VALOR() { return tablaCobrosRealizados_Valor; }
    public void settablaCobrosRealizados_Valor(String v) { this.tablaCobrosRealizados_Valor = v; }
    public String gettablaCobrosRealizados_SALDOPENDIENTE() { return tablaCobrosRealizados_saldopendiente; }
    public void settablaCobrosRealizados_saldopendiente(String v) { this.tablaCobrosRealizados_saldopendiente = v; }
    public String gettablaCobrosRealizados_CUOTASPENDIENTES() { return tablaCobrosRealizados_cuotaspendientes; }
    public void settablaCobrosRealizados_cuotaspendientes(String v) { this.tablaCobrosRealizados_cuotaspendientes = v; }
    public String gettablaCobrosRealizados_IDCONCEPTO() { return tablaCobrosRealizados_idconcepto; }
    public void settablaCobrosRealizados_idconcepto(String v) { this.tablaCobrosRealizados_idconcepto = v; }
    public String gettablaCobrosRealizados_IDCONVENIO() { return tablaCobrosRealizados_idconvenio; }
    public void settablaCobrosRealizados_idconvenio(String v) { this.tablaCobrosRealizados_idconvenio = v; }
    public String gettablaCobrosRealizados_PRIMERCONVENIO() { return tablaCobrosRealizados_primerconvenio; }
    public void settablaCobrosRealizados_primerconvenio(String v) { this.tablaCobrosRealizados_primerconvenio = v; }
    public String gettablaCobrosRealizados_NROCONVENIOS() { return tablaCobrosRealizados_nroconvenios; }
    public void settablaCobrosRealizados_nroconvenios(String v) { this.tablaCobrosRealizados_nroconvenios = v; }
    public String gettablaCobrosRealizados_PORCENTAJE() { return tablaCobrosRealizados_porcentaje; }
    public void settablaCobrosRealizados_porcentaje(String v) { this.tablaCobrosRealizados_porcentaje = v; }
    public String gettablaCobrosRealizados_RANGOMINIMO() { return tablaCobrosRealizados_rangominimo; }
    public void settablaCobrosRealizados_rangominimo(String v) { this.tablaCobrosRealizados_rangominimo = v; }
    public String gettablaCobrosRealizados_RANGOMAXIMO() { return tablaCobrosRealizados_rangomaximo; }
    public void settablaCobrosRealizados_rangomaximo(String v) { this.tablaCobrosRealizados_rangomaximo = v; }
    public String gettablaCobrosRealizados_PERIODOFACTURADO() { return tablaCobrosRealizados_periodofacturado; }
    public void settablaCobrosRealizados_periodofacturado(String v) { this.tablaCobrosRealizados_periodofacturado = v; }
    public String gettablaCobrosRealizados_FIN() { return tablaCobrosRealizados_FIN; }
    public void settablaCobrosRealizados_FIN(String v) { this.tablaCobrosRealizados_FIN = v; }
    public String getArchivo_TablaCobrosRealizados() { return archivo_TablaCobrosRealizados; }
    public void setArchivo_TablaCobrosRealizados(String v) { this.archivo_TablaCobrosRealizados = v; }

    // ------------------------------------------------------------------ apertura / cierre

    public synchronized Boolean abrir_TablaCobrosRealizados(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.length() == 0) return false;
        cerrarSilencioso();
        try {
            rFile = new RandomAccessFile(nombreArchivo, "rw");
            archivo_TablaCobrosRealizados = nombreArchivo;
            total_TablaCobrosRealizados = (int) (rFile.length() / LONGITUD_REGISTRO);
            return true;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "al abrir " + nombreArchivo + ": " + e.getMessage());
            return false;
        } catch (IOException e) {
            Log.e(TAG, "al abrir1 " + e.getMessage());
            return false;
        }
    }

    /**
     * Abre en modo solo lectura. Lo usa el hilo de envío para leer CO_COBRO.SDA directamente en vez de
     * una copia completa del archivo por factura: los registros son de longitud fija y la liquidación
     * solo agrega al final o reescribe en su sitio los de la cuenta en curso, así que los registros ya
     * referenciados por ENVIOGPRS no se mueven. {@code total} se fija al abrir.
     */
    public synchronized Boolean abrirSoloLectura_TablaCobrosRealizados(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.length() == 0) return false;
        cerrarSilencioso();
        try {
            rFile = new RandomAccessFile(nombreArchivo, "r");
            archivo_TablaCobrosRealizados = nombreArchivo;
            total_TablaCobrosRealizados = (int) (rFile.length() / LONGITUD_REGISTRO);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "al abrir (lectura) " + nombreArchivo + ": " + e.getMessage());
            return false;
        }
    }

    public synchronized void Cerrar_TablaCobrosRealizados() {
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

    public synchronized void posicion_TablaCobrosRealizados(int registro, int tamano) {
        try {
            rFile.seek((long) (registro - 1) * tamano);
        } catch (IOException e) {
            Log.e(TAG, "seek " + registro + ": " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ lectura

    public synchronized void lectura_TablaCobrosRealizados(int registro) {
        encontro_TablaCobrosRealizados = 0;
        if (rFile == null || registro < 1 || registro > total_TablaCobrosRealizados) {
            Log.e(TAG, "lectura fuera de rango o archivo cerrado: " + registro + " de " + total_TablaCobrosRealizados);
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
        tablaCobrosRealizados_Nrocuentacliente = new String(b, 0, 10, LATIN1);
        tablaCobrosRealizados_Conceptodecobro = new String(b, 11, 5, LATIN1);
        tablaCobrosRealizados_Indicadoractividad = new String(b, 17, 1, LATIN1);
        tablaCobrosRealizados_Valor = new String(b, 19, 10, LATIN1);
        tablaCobrosRealizados_saldopendiente = new String(b, 30, 10, LATIN1);
        tablaCobrosRealizados_cuotaspendientes = new String(b, 41, 2, LATIN1);
        tablaCobrosRealizados_idconcepto = new String(b, 44, 3, LATIN1);
        tablaCobrosRealizados_idconvenio = new String(b, 48, 1, LATIN1);
        tablaCobrosRealizados_primerconvenio = new String(b, 50, 5, LATIN1);
        tablaCobrosRealizados_nroconvenios = new String(b, 56, 2, LATIN1);
        tablaCobrosRealizados_porcentaje = new String(b, 59, 6, LATIN1);
        tablaCobrosRealizados_rangominimo = new String(b, 66, 10, LATIN1);
        tablaCobrosRealizados_rangomaximo = new String(b, 77, 10, LATIN1);
        tablaCobrosRealizados_periodofacturado = new String(b, 88, 22, LATIN1);
        tablaCobrosRealizados_FIN = new String(b, 111, 2, LATIN1);
        ultimo_TablaCobrosRealizados = registro;
    }

    // ------------------------------------------------------------------ escritura

    /** Solo escribe si en disco, en esa posicion, esta la misma cuenta. */
    public synchronized boolean escribir_TablaCobrosRealizados(int posicion) {
        if (!enRango(posicion)) return false;
        try {
            rFile.seek((long) (posicion - 1) * LONGITUD_REGISTRO);
            rFile.readFully(bufferVerificacion);
        } catch (IOException e) {
            bloqueo(posicion, "no se pudo verificar: " + e.getMessage());
            return false;
        }
        String cuentaDisco = new String(bufferVerificacion, 0, 10, LATIN1);
        if (!UtilPlanos.mismoValor(cuentaDisco, tablaCobrosRealizados_Nrocuentacliente)) {
            bloqueo(posicion, "en disco esta la cuenta " + cuentaDisco.trim());
            return false;
        }
        return escribirEnPosicion(posicion);
    }

    /** Escribe sin verificar (solo para reparaciones explicitas). */
    public synchronized boolean escribirForzado_TablaCobrosRealizados(int posicion) {
        if (!enRango(posicion)) return false;
        Log.w(TAG, "Escritura forzada en posicion " + posicion + " cuenta " + tablaCobrosRealizados_Nrocuentacliente);
        return escribirEnPosicion(posicion);
    }

    private boolean enRango(int posicion) {
        if (rFile == null || posicion < 1 || posicion > total_TablaCobrosRealizados) {
            bloqueo(posicion, "fuera de rango o archivo cerrado (total " + total_TablaCobrosRealizados + ")");
            return false;
        }
        return true;
    }

    private boolean escribirEnPosicion(int posicion) {
        rellenar_TablaCobrosRealizados();
        String linea = tablaCobrosRealizados_Nrocuentacliente + sep + tablaCobrosRealizados_Conceptodecobro + sep + tablaCobrosRealizados_Indicadoractividad + sep + tablaCobrosRealizados_Valor + sep
                + tablaCobrosRealizados_saldopendiente + sep + tablaCobrosRealizados_cuotaspendientes + sep + tablaCobrosRealizados_idconcepto + sep + tablaCobrosRealizados_idconvenio + sep
                + tablaCobrosRealizados_primerconvenio + sep + tablaCobrosRealizados_nroconvenios + sep + tablaCobrosRealizados_porcentaje + sep + tablaCobrosRealizados_rangominimo + sep
                + tablaCobrosRealizados_rangomaximo + sep + tablaCobrosRealizados_periodofacturado + sep + tablaCobrosRealizados_FIN;
        if (linea.length() != LONGITUD_REGISTRO) {
            UtilPlanos.evento(TAG, "CADENA DAÑADA COBROS (longitud " + linea.length() + ", posicion " + posicion + "):\r\n" + linea);
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
        UtilPlanos.evento(TAG, "[BLOQUEADO COBRO] cuenta " + (tablaCobrosRealizados_Nrocuentacliente == null ? "" : tablaCobrosRealizados_Nrocuentacliente.trim())
                + " concepto " + (tablaCobrosRealizados_Conceptodecobro == null ? "" : tablaCobrosRealizados_Conceptodecobro.trim())
                + " intento escribir en posicion " + posicion + " -> " + motivo);
    }

    public synchronized void rellenar_TablaCobrosRealizados() {
        tablaCobrosRealizados_Nrocuentacliente = String.format("%-10s", tablaCobrosRealizados_Nrocuentacliente);
        tablaCobrosRealizados_Conceptodecobro = String.format("%-5s", tablaCobrosRealizados_Conceptodecobro);
        tablaCobrosRealizados_Indicadoractividad = String.format("%-1s", tablaCobrosRealizados_Indicadoractividad);
        tablaCobrosRealizados_Valor = String.format("%10s", tablaCobrosRealizados_Valor);
        tablaCobrosRealizados_saldopendiente = String.format("%-10s", tablaCobrosRealizados_saldopendiente);
        tablaCobrosRealizados_cuotaspendientes = String.format("%-2s", tablaCobrosRealizados_cuotaspendientes);
        tablaCobrosRealizados_idconcepto = String.format("%-3s", tablaCobrosRealizados_idconcepto);
        tablaCobrosRealizados_idconvenio = String.format("%-1s", tablaCobrosRealizados_idconvenio);
        tablaCobrosRealizados_primerconvenio = String.format("%-5s", tablaCobrosRealizados_primerconvenio);
        tablaCobrosRealizados_nroconvenios = String.format("%-2s", tablaCobrosRealizados_nroconvenios);
        tablaCobrosRealizados_porcentaje = String.format("%-6s", tablaCobrosRealizados_porcentaje);
        tablaCobrosRealizados_rangominimo = String.format("%-10s", tablaCobrosRealizados_rangominimo);
        tablaCobrosRealizados_rangomaximo = String.format("%-10s", tablaCobrosRealizados_rangomaximo);
        tablaCobrosRealizados_periodofacturado = String.format("%-22s", tablaCobrosRealizados_periodofacturado);
        tablaCobrosRealizados_FIN = "\r\n";
    }

    // ------------------------------------------------------------------ busqueda

    public synchronized void buscarSecuencial_TablaCobrosRealizados(String codigo) {
        encontro_TablaCobrosRealizados = 0;
        String buscado = UtilPlanos.normalizar(codigo);
        for (int i = 1; i <= total_TablaCobrosRealizados; i++) {
            lectura_TablaCobrosRealizados(i);
            if (buscado.equals(UtilPlanos.normalizar(tablaCobrosRealizados_Nrocuentacliente))) {
                encontro_TablaCobrosRealizados = i;
                return;
            }
        }
    }
}
