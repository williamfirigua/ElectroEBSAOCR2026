package com.gselectroCaqueta.tablas;

import android.util.Log;

import com.gselectroCaqueta.accesoyseguridad.VariablesGlobales;
import com.gsutil.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/// <summary>
/// S_REGIST (REGISTRO.SDA) - registro fijo de 185 bytes.
/// - Acceso a disco serializado: lectura, escritura y busquedas son synchronized sobre la instancia,
///   asi un seek de otro hilo nunca puede desviar una escritura.
/// - escribir_TablaRegistroSalida verifica en disco que la posicion sea de la misma cuenta y consecutivo
///   antes de escribir; si no coincide NO escribe y deja constancia en LOGEVENTOS.LOG.
/// - Indice en memoria (cuenta + consecutivo -> posicion) construido al abrir: las busquedas pasan
///   de leer hasta 10.000 registros por cuenta a una consulta directa.
/// </summary>
public class TablaRegistroSalida { //Ax esta es S_REGIST, ya

    private static final int LONGITUD_REGISTRO = 185; // CUENTA 10, MARCA 10. Verificado contra REGISTRO.SDA
    // writeBytes escribe 1 byte por caracter (Latin-1): se lee con el mismo juego para que sea simetrico.
    private static final Charset LATIN1 = Charset.forName("ISO-8859-1");
    private static final String TAG = "REGISTRO";

    public LineNumberReader rdr; // ya no se usa: se conserva por compatibilidad
    String sep = ";";
    String tablaRegistroSalida_cliente;
    String tablaRegistroSalida_contador;
    String tablaRegistroSalida_anio;
    String tablaRegistroSalida_mes;
    String tablaRegistroSalida_cuenta;
    String tablaRegistroSalida_marca;
    String tablaRegistroSalida_nrocontador;
    String tablaRegistroSalida_consecutivo;
    String tablaRegistroSalida_lecturatomada;
    String tablaRegistroSalida_consumotomado;
    String tablaRegistroSalida_consumocalculado;
    String tablaRegistroSalida_causadenolectura;
    String tablaRegistroSalida_comentario1;
    String tablaRegistroSalida_comentario2;
    String tablaRegistroSalida_informe;
    String tablaRegistroSalida_intentos;
    String tablaRegistroSalida_modificaciones;
    String tablaRegistroSalida_lecturamodificada1;
    String tablaRegistroSalida_lecturamodificada2;
    String tablaRegistroSalida_fechalectura;
    String tablaRegistroSalida_horalectura;
    String tablaRegistroSalida_leido;
    String tablaRegistroSalida_fin;

    // Buffers reutilizables: se reservan una sola vez (no un byte[] nuevo por cada lectura).
    private final byte[] bufferLectura = new byte[LONGITUD_REGISTRO];
    private final byte[] bufferVerificacion = new byte[LONGITUD_REGISTRO];
    byte[] byteArray = bufferLectura; // compatibilidad con codigo que lo referencie
    String texto;

    String archivo_TablaRegistroSalida;
    int ultimo_TablaRegistroSalida;
    public int encontro_TablaRegistroSalida;
    String buscar_TablaRegistroSalida;
    RandomAccessFile rFile;
    private int total_TablaRegistroSalida;

    // Indices en memoria (se reconstruyen en cada apertura).
    private final Map<String, Integer> indiceCuentaConsecutivo = new HashMap<String, Integer>();
    private final Map<String, Integer> indiceCuenta = new HashMap<String, Integer>();

    Utils utils = new Utils();
    VariablesGlobales variables = new VariablesGlobales();

    public static int getLongitudRegistro() {
        return LONGITUD_REGISTRO;
    }

    // ------------------------------------------------------------------ getters / setters

    public String gettablaRegistroSalida_CLIENTE() { return tablaRegistroSalida_cliente; }
    public void settablaRegistroSalida_cliente(String v) { this.tablaRegistroSalida_cliente = v; }
    public String gettablaRegistroSalida_CONTADOR() { return tablaRegistroSalida_contador; }
    public void settablaRegistroSalida_contador(String v) { this.tablaRegistroSalida_contador = v; }
    public String gettablaRegistroSalida_ANIO() { return tablaRegistroSalida_anio; }
    public void settablaRegistroSalida_anio(String v) { this.tablaRegistroSalida_anio = v; }
    public String gettablaRegistroSalida_MES() { return tablaRegistroSalida_mes; }
    public void settablaRegistroSalida_mes(String v) { this.tablaRegistroSalida_mes = v; }
    public String gettablaRegistroSalida_CUENTA() { return tablaRegistroSalida_cuenta; }
    public void settablaRegistroSalida_cuenta(String v) { this.tablaRegistroSalida_cuenta = v; }
    public String gettablaRegistroSalida_MARCA() { return tablaRegistroSalida_marca; }
    public void settablaRegistroSalida_marca(String v) { this.tablaRegistroSalida_marca = v; }
    public String gettablaRegistroSalida_NROCONTADOR() { return tablaRegistroSalida_nrocontador; }
    public void settablaRegistroSalida_nrocontador(String v) { this.tablaRegistroSalida_nrocontador = v; }
    public String gettablaRegistroSalida_CONSECUTIVO() { return tablaRegistroSalida_consecutivo; }
    public void settablaRegistroSalida_consecutivo(String v) { this.tablaRegistroSalida_consecutivo = v; }
    public String gettablaRegistroSalida_LECTURATOMADA() { return tablaRegistroSalida_lecturatomada; }
    public void settablaRegistroSalida_lecturatomada(String v) { this.tablaRegistroSalida_lecturatomada = v; }
    public String gettablaRegistroSalida_CONSUMOTOMADO() { return tablaRegistroSalida_consumotomado; }
    public void settablaRegistroSalida_consumotomado(String v) { this.tablaRegistroSalida_consumotomado = v; }
    public String gettablaRegistroSalida_CONSUMOCALCULADO() { return tablaRegistroSalida_consumocalculado; }
    public void settablaRegistroSalida_consumocalculado(String v) { this.tablaRegistroSalida_consumocalculado = v; }
    public String gettablaRegistroSalida_CAUSADENOLECTURA() { return tablaRegistroSalida_causadenolectura; }
    public void settablaRegistroSalida_causadenolectura(String v) { this.tablaRegistroSalida_causadenolectura = v; }
    public String gettablaRegistroSalida_COMENTARIO1() { return tablaRegistroSalida_comentario1; }
    public void settablaRegistroSalida_comentario1(String v) { this.tablaRegistroSalida_comentario1 = v; }
    public String gettablaRegistroSalida_COMENTARIO2() { return tablaRegistroSalida_comentario2; }
    public void settablaRegistroSalida_comentario2(String v) { this.tablaRegistroSalida_comentario2 = v; }
    public String gettablaRegistroSalida_INFORME() { return tablaRegistroSalida_informe; }
    public void settablaRegistroSalida_informe(String v) { this.tablaRegistroSalida_informe = v; }
    public String gettablaRegistroSalida_INTENTOS() { return tablaRegistroSalida_intentos; }
    public void settablaRegistroSalida_intentos(String v) { this.tablaRegistroSalida_intentos = v; }
    public String gettablaRegistroSalida_MODIFICACIONES() { return tablaRegistroSalida_modificaciones; }
    public void settablaRegistroSalida_modificaciones(String v) { this.tablaRegistroSalida_modificaciones = v; }
    public String gettablaRegistroSalida_LECTURAMODIFICADA1() { return tablaRegistroSalida_lecturamodificada1; }
    public void settablaRegistroSalida_lecturamodificada1(String v) { this.tablaRegistroSalida_lecturamodificada1 = v; }
    public String gettablaRegistroSalida_LECTURAMODIFICADA2() { return tablaRegistroSalida_lecturamodificada2; }
    public void settablaRegistroSalida_lecturamodificada2(String v) { this.tablaRegistroSalida_lecturamodificada2 = v; }
    public String gettablaRegistroSalida_FECHALECTURA() { return tablaRegistroSalida_fechalectura; }
    public void settablaRegistroSalida_fechalectura(String v) { this.tablaRegistroSalida_fechalectura = v; }
    public String gettablaRegistroSalida_HORALECTURA() { return tablaRegistroSalida_horalectura; }
    public void settablaRegistroSalida_horalectura(String v) { this.tablaRegistroSalida_horalectura = v; }
    public String gettablaRegistroSalida_LEIDO() { return tablaRegistroSalida_leido; }
    public void settablaRegistroSalida_leido(String v) { this.tablaRegistroSalida_leido = v; }
    public String gettablaRegistroSalida_FIN() { return tablaRegistroSalida_fin; }
    public void settablaRegistroSalida_fin(String v) { this.tablaRegistroSalida_fin = v; }
    public String getArchivo_TablaRegistroSalida() { return archivo_TablaRegistroSalida; }
    public void setArchivo_TablaRegistroSalida(String v) { this.archivo_TablaRegistroSalida = v; }
    public synchronized int getTotal_TablaRegistroSalida() { return total_TablaRegistroSalida; }
    public synchronized void setTotal_TablaRegistroSalida(int v) { this.total_TablaRegistroSalida = v; }

    // ------------------------------------------------------------------ apertura / cierre

    public synchronized Boolean abrir_TablaRegistroSalida(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.length() == 0) {
            return false;
        }
        cerrarSilencioso();
        try {
            rFile = new RandomAccessFile(nombreArchivo, "rw");
        } catch (FileNotFoundException e) {
            Log.e(TAG, "al abrir " + nombreArchivo + ": " + e.getMessage());
            return false;
        }
        archivo_TablaRegistroSalida = nombreArchivo;
        try {
            total_TablaRegistroSalida = (int) (rFile.length() / LONGITUD_REGISTRO);
            construirIndices();
            return true;
        } catch (IOException ex) {
            Log.e(TAG, "al abrir1 " + ex.getMessage());
            return false;
        }
    }

    public synchronized void Cerrar_TablaRegistroSalida() {
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

    /** Recorre el archivo UNA vez en bloques grandes y arma los indices cuenta+consecutivo -> posicion. */
    private void construirIndices() throws IOException {
        indiceCuentaConsecutivo.clear();
        indiceCuenta.clear();
        if (total_TablaRegistroSalida == 0) return;

        final int registrosPorBloque = 512;
        byte[] bloque = new byte[registrosPorBloque * LONGITUD_REGISTRO];
        rFile.seek(0);
        int posicion = 1;
        while (posicion <= total_TablaRegistroSalida) {
            int registros = Math.min(registrosPorBloque, total_TablaRegistroSalida - posicion + 1);
            rFile.readFully(bloque, 0, registros * LONGITUD_REGISTRO);
            for (int r = 0; r < registros; r++) {
                int base = r * LONGITUD_REGISTRO;
                String cuenta = new String(bloque, base + 20, 10, LATIN1);
                String consecutivo = new String(bloque, base + 59, 3, LATIN1);
                indexar(cuenta, consecutivo, posicion + r);
            }
            posicion += registros;
        }
    }

    private void indexar(String cuenta, String consecutivo, int posicion) {
        String llave = llave(cuenta, consecutivo);
        if (!indiceCuentaConsecutivo.containsKey(llave)) indiceCuentaConsecutivo.put(llave, posicion);
        String c = normalizar(cuenta);
        if (!indiceCuenta.containsKey(c)) indiceCuenta.put(c, posicion);
    }

    private static String llave(String cuenta, String consecutivo) {
        return normalizar(cuenta) + "#" + normalizar(consecutivo);
    }

    /** Quita espacios y ceros a la izquierda en valores numericos ("012" == "12"). */
    private static String normalizar(String valor) {
        if (valor == null) return "";
        String v = valor.trim();
        if (v.length() == 0) return v;
        for (int i = 0; i < v.length(); i++) {
            if (!Character.isDigit(v.charAt(i))) return v;
        }
        int i = 0;
        while (i < v.length() - 1 && v.charAt(i) == '0') i++;
        return v.substring(i);
    }

    // ------------------------------------------------------------------ lectura

    public synchronized void posicion_TablaRegistroSalida(int registro, int tamano) {
        try {
            rFile.seek((long) (registro - 1) * tamano);
        } catch (IOException e) {
            Log.e(TAG, "seek " + registro + ": " + e.getMessage());
        }
    }

    /** Lee el registro indicado. Si no se puede leer completo, los campos NO se tocan y se registra el error. */
    public synchronized void lectura_TablaRegistroSalida(int registro) {
        encontro_TablaRegistroSalida = 0;
        if (rFile == null || registro < 1 || registro > total_TablaRegistroSalida) {
            Log.e(TAG, "lectura fuera de rango o archivo cerrado: " + registro + " de " + total_TablaRegistroSalida);
            return;
        }
        try {
            rFile.seek((long) (registro - 1) * LONGITUD_REGISTRO);
            rFile.readFully(bufferLectura);
        } catch (IOException e) {
            Log.e(TAG, "lectura fallida en posicion " + registro + ": " + e.getMessage());
            return;
        }
        decodificar(bufferLectura, 0);
        ultimo_TablaRegistroSalida = registro;
    }


    /** Decodifica los campos de un registro que empieza en 'base' dentro de 'b' (Latin-1, sin String intermedio). */
    private void decodificar(byte[] b, int base) {
        texto = null;
        tablaRegistroSalida_cliente = new String(b, base + 0, 5, LATIN1);
        tablaRegistroSalida_contador = new String(b, base + 6, 5, LATIN1);
        tablaRegistroSalida_anio = new String(b, base + 12, 4, LATIN1);
        tablaRegistroSalida_mes = new String(b, base + 17, 2, LATIN1);
        tablaRegistroSalida_cuenta = new String(b, base + 20, 10, LATIN1);
        tablaRegistroSalida_marca = new String(b, base + 31, 10, LATIN1);
        tablaRegistroSalida_nrocontador = new String(b, base + 42, 16, LATIN1);
        tablaRegistroSalida_consecutivo = new String(b, base + 59, 3, LATIN1);
        tablaRegistroSalida_lecturatomada = new String(b, base + 63, 10, LATIN1);
        tablaRegistroSalida_consumotomado = new String(b, base + 74, 10, LATIN1);
        tablaRegistroSalida_consumocalculado = new String(b, base + 85, 10, LATIN1);
        tablaRegistroSalida_causadenolectura = new String(b, base + 96, 3, LATIN1);
        tablaRegistroSalida_comentario1 = new String(b, base + 100, 3, LATIN1);
        tablaRegistroSalida_comentario2 = new String(b, base + 104, 3, LATIN1);
        tablaRegistroSalida_informe = new String(b, base + 108, 30, LATIN1);
        tablaRegistroSalida_intentos = new String(b, base + 139, 1, LATIN1);
        tablaRegistroSalida_modificaciones = new String(b, base + 141, 1, LATIN1);
        tablaRegistroSalida_lecturamodificada1 = new String(b, base + 143, 10, LATIN1);
        tablaRegistroSalida_lecturamodificada2 = new String(b, base + 154, 10, LATIN1);
        tablaRegistroSalida_fechalectura = new String(b, base + 165, 8, LATIN1);
        tablaRegistroSalida_horalectura = new String(b, base + 174, 6, LATIN1);
        tablaRegistroSalida_leido = new String(b, base + 181, 1, LATIN1);
        tablaRegistroSalida_fin = "\r\n";
    }

    // ------------------------------------------------------------------ escritura

    /**
     * Escritura normal. Solo escribe si en disco, en esa posicion, esta la MISMA cuenta y consecutivo
     * que trae el registro en memoria. posicion == total + 1 agrega al final (BACKUP).
     */
    public synchronized boolean escribir_TablaRegistroSalida(int posicion) {
        if (rFile == null) {
            registrarBloqueo(posicion, "archivo cerrado");
            return false;
        }
        if (posicion == total_TablaRegistroSalida + 1) {
            return escribirEnPosicion(posicion);
        }
        if (posicion < 1 || posicion > total_TablaRegistroSalida) {
            registrarBloqueo(posicion, "fuera de rango (total " + total_TablaRegistroSalida + ")");
            return false;
        }
        try {
            rFile.seek((long) (posicion - 1) * LONGITUD_REGISTRO);
            rFile.readFully(bufferVerificacion);
        } catch (IOException e) {
            registrarBloqueo(posicion, "no se pudo verificar: " + e.getMessage());
            return false;
        }
        String cuentaDisco = new String(bufferVerificacion, 20, 10, LATIN1);
        String consecDisco = new String(bufferVerificacion, 59, 3, LATIN1);
        if (!normalizar(cuentaDisco).equals(normalizar(tablaRegistroSalida_cuenta))
                || !normalizar(consecDisco).equals(normalizar(tablaRegistroSalida_consecutivo))) {
            registrarBloqueo(posicion, "en disco esta " + cuentaDisco.trim() + "/" + consecDisco.trim());
            return false;
        }
        return escribirEnPosicion(posicion);
    }

    /** SOLO para repararRegistrador(): escribe sin verificar porque cambia la cuenta a proposito. */
    public synchronized boolean escribirForzado_TablaRegistroSalida(int posicion) {
        if (rFile == null || posicion < 1 || posicion > total_TablaRegistroSalida + 1) {
            registrarBloqueo(posicion, "escritura forzada fuera de rango");
            return false;
        }
        Log.w(TAG, "Escritura forzada en posicion " + posicion + " cuenta " + tablaRegistroSalida_cuenta);
        return escribirEnPosicion(posicion);
    }

    private boolean escribirEnPosicion(int posicion) {
        rellenar_TablaRegistroSalida();
        String linea = tablaRegistroSalida_cliente + sep + tablaRegistroSalida_contador + sep + tablaRegistroSalida_anio + sep + tablaRegistroSalida_mes + sep + tablaRegistroSalida_cuenta + sep
                + tablaRegistroSalida_marca + sep + tablaRegistroSalida_nrocontador + sep + tablaRegistroSalida_consecutivo + sep + tablaRegistroSalida_lecturatomada + sep
                + tablaRegistroSalida_consumotomado + sep + tablaRegistroSalida_consumocalculado + sep + tablaRegistroSalida_causadenolectura + sep + tablaRegistroSalida_comentario1 + sep
                + tablaRegistroSalida_comentario2 + sep + tablaRegistroSalida_informe + sep + tablaRegistroSalida_intentos + sep + tablaRegistroSalida_modificaciones + sep
                + tablaRegistroSalida_lecturamodificada1 + sep + tablaRegistroSalida_lecturamodificada2 + sep + tablaRegistroSalida_fechalectura + sep + tablaRegistroSalida_horalectura + sep
                + tablaRegistroSalida_leido + sep + tablaRegistroSalida_fin;

        if (linea.length() != LONGITUD_REGISTRO) {
            escribirEvento("CADENA DAÑADA REGISTRADORES (longitud " + linea.length() + ", posicion " + posicion + "):\r\n" + linea);
            return false;
        }
        try {
            rFile.seek((long) (posicion - 1) * LONGITUD_REGISTRO); // seek y write dentro del mismo bloque synchronized
            rFile.write(linea.getBytes(LATIN1));
            if (posicion > total_TablaRegistroSalida) total_TablaRegistroSalida = posicion;
            invalidarBloque();
            indiceCuentaConsecutivo.put(llave(tablaRegistroSalida_cuenta, tablaRegistroSalida_consecutivo), posicion);
            String c = normalizar(tablaRegistroSalida_cuenta);
            if (!indiceCuenta.containsKey(c)) indiceCuenta.put(c, posicion);
            return true;
        } catch (IOException ioe) {
            Log.e(TAG, "escritura fallida en posicion " + posicion + ": " + ioe.getMessage());
            return false;
        }
    }

    public synchronized void rellenar_TablaRegistroSalida() {
        tablaRegistroSalida_consecutivo = String.format("%-3s", tablaRegistroSalida_consecutivo);
        tablaRegistroSalida_lecturatomada = String.format("%10s", tablaRegistroSalida_lecturatomada);
        tablaRegistroSalida_consumotomado = String.format("%10s", tablaRegistroSalida_consumotomado).replace(" ", "0");
        tablaRegistroSalida_consumocalculado = String.format("%-10s", tablaRegistroSalida_consumocalculado);
        tablaRegistroSalida_causadenolectura = String.format("%3s", tablaRegistroSalida_causadenolectura);
        tablaRegistroSalida_comentario1 = String.format("%-3s", tablaRegistroSalida_comentario1);
        tablaRegistroSalida_comentario2 = String.format("%-3s", tablaRegistroSalida_comentario2);
        tablaRegistroSalida_informe = String.format("%-30s", tablaRegistroSalida_informe);
        tablaRegistroSalida_intentos = String.format("%-1s", tablaRegistroSalida_intentos);
        tablaRegistroSalida_modificaciones = String.format("%-1s", tablaRegistroSalida_modificaciones);
        tablaRegistroSalida_lecturamodificada1 = String.format("%-10s", tablaRegistroSalida_lecturamodificada1);
        tablaRegistroSalida_lecturamodificada2 = String.format("%-10s", tablaRegistroSalida_lecturamodificada2);
        tablaRegistroSalida_fechalectura = String.format("%-8s", tablaRegistroSalida_fechalectura);
        tablaRegistroSalida_horalectura = String.format("%-6s", tablaRegistroSalida_horalectura);
        tablaRegistroSalida_leido = String.format("%-1s", tablaRegistroSalida_leido);
        tablaRegistroSalida_fin = "\r\n";
    }

    private void registrarBloqueo(int posicion, String motivo) {
        escribirEvento("[BLOQUEADO] cuenta " + (tablaRegistroSalida_cuenta == null ? "" : tablaRegistroSalida_cuenta.trim())
                + " consec " + (tablaRegistroSalida_consecutivo == null ? "" : tablaRegistroSalida_consecutivo.trim())
                + " intento escribir en posicion " + posicion + " -> " + motivo);
    }

    private void escribirEvento(String mensaje) {
        Log.e(TAG, mensaje);
        try {
            utils.EscribirLinea(new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/LOGEVENTOS.LOG"), mensaje);
        } catch (Exception e) {
            Log.e(TAG, "no se pudo escribir LOGEVENTOS: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ busquedas (por indice)

    /** Deja cargado el registro encontrado (igual que antes) y encontro = posicion, o 0. */
    private void cargarEncontrado(Integer posicion) {
        if (posicion == null) {
            encontro_TablaRegistroSalida = 0;
            return;
        }
        lectura_TablaRegistroSalida(posicion);
        encontro_TablaRegistroSalida = posicion;
    }

    public synchronized void BuscarSecuencialSregistrosConConcecutivo(String codigo, String consecutivo) {
        cargarEncontrado(indiceCuentaConsecutivo.get(llave(codigo, consecutivo)));
    }

    public synchronized void BuscarSecuencialSregistrosConConcecutivo2(String codigo, String contador) {
        cargarEncontrado(indiceCuentaConsecutivo.get(llave(codigo, contador)));
    }

    public synchronized void buscarSecuencial_TablaRegistroSalida_cuenta(String codigo1) {
        cargarEncontrado(indiceCuenta.get(normalizar(codigo1)));
    }

    /** Busca por consecutivo solamente (no es unico): recorrido secuencial, como el original. */
    public synchronized void buscarSecuencial_TablaRegistroSalida(String codigo) {
        encontro_TablaRegistroSalida = 0;
        String buscado = normalizar(codigo);
        for (int i = 1; i <= total_TablaRegistroSalida; i++) {
            lectura_TablaRegistroSalida(i);
            if (buscado.equals(normalizar(tablaRegistroSalida_consecutivo))) {
                encontro_TablaRegistroSalida = i;
                return;
            }
        }
    }

    // ------------------------------------------------------------------ lectura por bloque (resumen)

    private static final int REGISTROS_POR_BLOQUE = 64;           // ~11.8 KB por bloque (memoria fija, similar al buffer de 8 KB del BufferedReader original)
    private byte[] bloqueCache;                                   // null = modo bloque inactivo
    private int bloqueInicio = -1;                                // primer registro del bloque cargado
    private int bloqueCantidad = 0;

    /** Activa la lectura por bloques para recorridos largos (resumenes). Usa el mismo archivo abierto. */
    public synchronized void inicializaBloque() {
        bloqueCache = new byte[REGISTROS_POR_BLOQUE * LONGITUD_REGISTRO];
        bloqueInicio = -1;
        bloqueCantidad = 0;
    }

    /**
     * Lee el registro x desde el bloque en memoria; si x no esta en el bloque, carga el bloque que lo contiene.
     * Acepta cualquier x (adelante, atras o salteado) y siempre ve lo ultimo escrito en disco.
     */
    public synchronized void lectura_TablaRegistroSalidaII(int x) {
        encontro_TablaRegistroSalida = 0;
        if (bloqueCache == null) {            // no se llamo inicializaBloque: se activa con el bloque pequeno
            inicializaBloque();
        }
        if (rFile == null || x < 1 || x > total_TablaRegistroSalida) {
            Log.e(TAG, "lecturaII fuera de rango o archivo cerrado: " + x + " de " + total_TablaRegistroSalida);
            return;
        }
        if (x < bloqueInicio || x >= bloqueInicio + bloqueCantidad) {
            int cantidad = Math.min(REGISTROS_POR_BLOQUE, total_TablaRegistroSalida - x + 1);
            try {
                rFile.seek((long) (x - 1) * LONGITUD_REGISTRO);
                rFile.readFully(bloqueCache, 0, cantidad * LONGITUD_REGISTRO);
            } catch (IOException e) {
                bloqueInicio = -1;
                bloqueCantidad = 0;
                Log.e(TAG, "lecturaII fallida en bloque desde " + x + ": " + e.getMessage());
                return;
            }
            bloqueInicio = x;
            bloqueCantidad = cantidad;
        }
        decodificarEstadistica(bloqueCache, (x - bloqueInicio) * LONGITUD_REGISTRO);
        ultimo_TablaRegistroSalida = x;
    }

    public synchronized void terminaBloque() {
        bloqueCache = null;
        bloqueInicio = -1;
        bloqueCantidad = 0;
    }

    /**
     * Solo los 11 campos que usa la estadistica (mismos que la version original de lectura_TablaRegistroSalidaII).
     * Los demas campos NO se tocan: quien llame este metodo solo debe usar estos 11.
     */
    private void decodificarEstadistica(byte[] b, int base) {
        tablaRegistroSalida_cliente = new String(b, base, 5, LATIN1);
        tablaRegistroSalida_contador = new String(b, base + 6, 5, LATIN1);
        tablaRegistroSalida_cuenta = new String(b, base + 20, 10, LATIN1);
        tablaRegistroSalida_nrocontador = new String(b, base + 42, 16, LATIN1);
        tablaRegistroSalida_lecturatomada = new String(b, base + 63, 10, LATIN1);
        tablaRegistroSalida_causadenolectura = new String(b, base + 96, 3, LATIN1);
        tablaRegistroSalida_comentario1 = new String(b, base + 100, 3, LATIN1);
        tablaRegistroSalida_informe = new String(b, base + 108, 30, LATIN1);
        tablaRegistroSalida_fechalectura = new String(b, base + 165, 8, LATIN1);
        tablaRegistroSalida_horalectura = new String(b, base + 174, 6, LATIN1);
        tablaRegistroSalida_leido = new String(b, base + 181, 1, LATIN1);
    }

    /** Cualquier escritura invalida el bloque en memoria para que la siguiente lectura vea el dato nuevo. */
    private void invalidarBloque() {
        bloqueInicio = -1;
        bloqueCantidad = 0;
    }
}
