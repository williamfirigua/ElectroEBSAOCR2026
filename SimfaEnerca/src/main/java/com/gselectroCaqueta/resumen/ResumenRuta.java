package com.gselectroCaqueta.resumen;

import com.gselectroCaqueta.moduloanomalias.AnomaliaDeNoLectura;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Estado de la ruta cargada, calculado en UNA pasada secuencial por REGISTRO.SDA y CLIENTE.SDA.
 *
 * Reemplaza los recorridos que hacían MenuPrincipal.valcantidad() y ResumenEstadistico.resumenGlobal(): lectura
 * registro por registro con seek en el hilo de la pantalla, un Log.e por registro y conteos distintos en cada
 * pantalla. Sin dependencias de Android: se prueba en la JVM.
 *
 * Definiciones (una sola para toda la app):
 * - Registro procesado: tiene LEIDO, causa o lectura ("0" cuenta como vacío, como en la liquidación).
 *   Pendiente: los tres vacíos.
 * - Registro con anomalía: tiene causa de no lectura (CAUSADENOLECTURA), con o sin lectura.
 * - Cliente procesado: INDFACTURACION de CLIENTE.SDA F (impresa) o K (liquidada sin imprimir), o L (solo
 *   lectura) / otro sin registradores pendientes. Es lo que la liquidación compara contra el total de CLIENTE.TXT.
 * - Crítica: el LEIDO que escribe la liquidación (3 normal, 1 baja, 2 igual a la anterior, 7 alta,
 *   8 negativa / giro, 5 y 4 causa sin lectura).
 */
public final class ResumenRuta {

    public static final int LONGITUD_REGISTRO = 185;
    public static final int LONGITUD_CLIENTE_SDA = 263;
    public static final int LONGITUD_CLIENTE_TXT = 1847;

    // REGISTRO.SDA (mismos desplazamientos que TablaRegistroSalida.decodificar)
    static final int R_CLIENTE = 0, R_CLIENTE_L = 5;
    static final int R_CUENTA = 20, R_CUENTA_L = 10;
    static final int R_MEDIDOR = 42, R_MEDIDOR_L = 16;
    static final int R_LECTURA = 63, R_LECTURA_L = 10;
    static final int R_CAUSA = 96, R_CAUSA_L = 3;
    static final int R_COMENTARIO = 100, R_COMENTARIO_L = 3;
    static final int R_INFORME = 108, R_INFORME_L = 30;
    static final int R_FECHA = 165, R_FECHA_L = 8;
    static final int R_HORA = 174, R_HORA_L = 6;
    static final int R_LEIDO = 181;
    // CLIENTE.SDA
    static final int C_INDFACTURACION = 24;

    static final Charset LATIN1 = Charset.forName("ISO-8859-1");

    public enum Filtro { TODAS, PROCESADAS, PENDIENTES, ANOMALIAS }

    public enum Critica {
        PENDIENTE("Pendiente"),
        NORMAL("Normal"),
        BAJA("Consumo bajo"),
        IGUAL("Igual a la anterior"),
        ALTA("Consumo alto"),
        NEGATIVA("Negativa / giro"),
        SIN_LECTURA("Causa sin lectura"),
        PROCESADA("Procesada");

        public final String etiqueta;

        Critica(String etiqueta) {
            this.etiqueta = etiqueta;
        }

        static Critica de(byte leido) {
            switch (leido) {
                case '3': return NORMAL;
                case '1': return BAJA;
                case '2': return IGUAL;
                case '7': return ALTA;
                case '8': return NEGATIVA;
                case '4':
                case '5': return SIN_LECTURA;
                default: return PROCESADA;
            }
        }
    }

    /** Una causa de no lectura con cuántos registros la tienen. */
    public static final class ConteoCausa {
        public final String codigo;
        public final String descripcion;
        public final int cantidad;

        ConteoCausa(String codigo, String descripcion, int cantidad) {
            this.codigo = codigo;
            this.descripcion = descripcion;
            this.cantidad = cantidad;
        }

        @Override
        public String toString() {
            return codigo + (descripcion.isEmpty() ? "" : " - " + descripcion) + " (" + cantidad + ")";
        }
    }

    // ------------------------------------------------------------------ por registro (índice 0 = posición 1)
    private int registros;
    private int[] cliente;
    private byte[] leido;
    private boolean[] pendiente;
    private String[] causa;      // normalizada (sin ceros a la izquierda); null si no tiene
    private String[] cuenta;     // sin ceros a la izquierda
    private String[] medidor;    // sin ceros a la izquierda

    // ------------------------------------------------------------------ totales
    private int registrosProcesados;
    private int registrosConCausa;
    private int registrosConComentario;
    private int registrosConInforme;
    private final int[] porCritica = new int[Critica.values().length];
    private List<ConteoCausa> causas = Collections.emptyList();

    private int totalClientes;
    private int clientesImpresos;
    private int clientesSinImprimir;
    private int clientesSoloLectura;
    private int clientesOtroEstado;
    private int clientesProcesados;
    private int clientesIncompletos;
    private boolean[] conRegistroPendiente = new boolean[0];

    private int primerRegistroPendiente;
    private int primerClientePendiente;

    private String primeraLectura = "";
    private String ultimaLectura = "";
    private int procesadosHoy;

    private String anioPeriodo = "";
    private String mesPeriodo = "";
    private String municipio = "";

    private ResumenRuta() {
    }

    /**
     * @param dirTrabajo   carpeta de trabajo (la que contiene DATOSDEENTRADA y DATOSDESALIDA)
     * @param hoyAAAAMMDD  fecha de hoy para contar lo procesado en el día
     * @param descripciones código normalizado -> descripción de la causa (ver {@link #cargarCausas(File)}); puede ser vacío
     * @throws IOException si falta REGISTRO.SDA o CLIENTE.SDA o no se pueden leer
     */
    public static ResumenRuta calcular(File dirTrabajo, String hoyAAAAMMDD, Map<String, String> descripciones) throws IOException {
        File registro = new File(dirTrabajo, "DATOSDESALIDA/REGISTRO.SDA");
        File clienteSda = new File(dirTrabajo, "DATOSDESALIDA/CLIENTE.SDA");
        File clienteTxt = new File(dirTrabajo, "DATOSDEENTRADA/CLIENTE.TXT");
        if (!registro.isFile()) throw new IOException("No existe " + registro.getName() + ": no hay ruta cargada");
        if (!clienteSda.isFile()) throw new IOException("No existe DATOSDESALIDA/" + clienteSda.getName());

        ResumenRuta r = new ResumenRuta();
        r.leerClientes(clienteSda);
        r.leerPeriodo(clienteTxt);
        r.leerRegistros(registro, hoyAAAAMMDD == null ? "" : hoyAAAAMMDD, descripciones == null ? Collections.<String, String>emptyMap() : descripciones);
        return r;
    }

    /** Catálogo de CAUSA_NL.TXT (código normalizado -> descripción). Vacío si no existe. */
    public static Map<String, String> cargarCausas(File causaNl) {
        Map<String, String> m = new HashMap<>();
        if (causaNl == null || !causaNl.isFile()) return m;
        AnomaliaDeNoLectura t = new AnomaliaDeNoLectura();
        if (!t.abrir_AnomaliaDeNoLectura(causaNl.getAbsolutePath())) return m;
        try {
            for (int i = 1; i <= t.getTotal_AnomaliaDeNoLectura(); i++) {
                t.lectura_AnomaliaDeNoLectura(i);
                String codigo = AnomaliaDeNoLectura.normalizarCodigo(t.getanomaliaDeNoLectura_CODIGO());
                if (!codigo.isEmpty() && !m.containsKey(codigo)) {
                    m.put(codigo, limpiar(t.getanomaliaDeNoLectura_DESCRIPCION()));
                }
            }
        } finally {
            t.Cerrar_AnomaliaDeNoLectura();
        }
        return m;
    }

    // ------------------------------------------------------------------ lectura de archivos

    /** INDFACTURACION por cliente; cliente[i] de REGISTRO.SDA apunta aquí (1 = primer registro). */
    private byte[] indicador = new byte[0];

    private void leerClientes(File clienteSda) throws IOException {
        int n = (int) (clienteSda.length() / LONGITUD_CLIENTE_SDA);
        indicador = new byte[n];
        byte[] rec = new byte[LONGITUD_CLIENTE_SDA];
        try (DataInputStream in = abrir(clienteSda)) {
            for (int i = 0; i < n; i++) {
                in.readFully(rec);
                byte ind = rec[C_INDFACTURACION];
                indicador[i] = ind;
                switch (ind) {
                    case 'F': clientesImpresos++; break;
                    case 'K': clientesSinImprimir++; break;
                    case 'L': clientesSoloLectura++; break;
                    case ' ':
                    case 0: break;
                    default: clientesOtroEstado++; break;
                }
            }
        } catch (EOFException e) {
            throw new IOException("CLIENTE.SDA cortado", e);
        }
        clientesProcesados = clientesImpresos + clientesSinImprimir + clientesSoloLectura + clientesOtroEstado;
        totalClientes = n;
    }

    private void leerPeriodo(File clienteTxt) {
        if (!clienteTxt.isFile() || clienteTxt.length() < 25) return;
        // La liquidación compara contra el total de CLIENTE.TXT: si difiere de CLIENTE.SDA manda el de entrada.
        int n = (int) (clienteTxt.length() / LONGITUD_CLIENTE_TXT);
        if (n > 0) totalClientes = n;
        byte[] cab = new byte[25];
        try (DataInputStream in = abrir(clienteTxt)) {
            in.readFully(cab);
            anioPeriodo = new String(cab, 0, 4, LATIN1).trim();
            mesPeriodo = new String(cab, 5, 2, LATIN1).trim();
            municipio = new String(cab, 21, 3, LATIN1).trim();
        } catch (IOException ignored) {
            // solo encabezado informativo
        }
    }

    private void leerRegistros(File registro, String hoy, Map<String, String> descripciones) throws IOException {
        int n = (int) (registro.length() / LONGITUD_REGISTRO);
        registros = n;
        cliente = new int[n];
        leido = new byte[n];
        pendiente = new boolean[n];
        causa = new String[n];
        cuenta = new String[n];
        medidor = new String[n];
        conRegistroPendiente = new boolean[indicador.length];
        Map<String, int[]> conteo = new HashMap<>();
        byte[] rec = new byte[LONGITUD_REGISTRO];
        String minimo = null, maximo = null;

        try (DataInputStream in = abrir(registro)) {
            for (int i = 0; i < n; i++) {
                in.readFully(rec);
                cliente[i] = entero(rec, R_CLIENTE, R_CLIENTE_L);
                cuenta[i] = sinCeros(texto(rec, R_CUENTA, R_CUENTA_L));
                medidor[i] = sinCeros(texto(rec, R_MEDIDOR, R_MEDIDOR_L));
                byte l = rec[R_LEIDO];
                leido[i] = l;
                // Mismo criterio que MenuDeLiquidacion (validar registro leído): "0" vale como vacío.
                String lecturaTxt = texto(rec, R_LECTURA, R_LECTURA_L);
                String causaTxt = texto(rec, R_CAUSA, R_CAUSA_L);
                boolean conLectura = !lecturaTxt.isEmpty() && !lecturaTxt.equals("0");
                boolean conCausa = !causaTxt.isEmpty() && !causaTxt.equals("0");
                boolean conLeido = l != ' ' && l != 0;
                String c = conCausa ? AnomaliaDeNoLectura.normalizarCodigo(causaTxt) : "";

                if (!conLeido && !conCausa && !conLectura) {
                    pendiente[i] = true;
                    porCritica[Critica.PENDIENTE.ordinal()]++;
                    if (primerRegistroPendiente == 0) primerRegistroPendiente = i + 1;
                    if (primerClientePendiente == 0 && cliente[i] > 0) primerClientePendiente = cliente[i];
                    int ci = cliente[i] - 1;
                    if (ci >= 0 && ci < conRegistroPendiente.length) conRegistroPendiente[ci] = true;
                    continue;
                }

                registrosProcesados++;
                porCritica[(conLeido ? Critica.de(l) : (conCausa ? Critica.SIN_LECTURA : Critica.PROCESADA)).ordinal()]++;
                if (conCausa) {
                    causa[i] = c.intern(); // pocas decenas de códigos distintos
                    registrosConCausa++;
                    int[] k = conteo.get(causa[i]);
                    if (k == null) conteo.put(causa[i], k = new int[1]);
                    k[0]++;
                }
                if (!vacio(rec, R_COMENTARIO, R_COMENTARIO_L)) registrosConComentario++;
                if (!vacio(rec, R_INFORME, R_INFORME_L)) registrosConInforme++;

                String fecha = texto(rec, R_FECHA, R_FECHA_L);
                String hora = texto(rec, R_HORA, R_HORA_L);
                if (fecha.length() == 8 && soloDigitos(fecha)) {
                    if (fecha.equals(hoy)) procesadosHoy++;
                    String momento = fecha + (hora.length() == 6 && soloDigitos(hora) ? hora : "000000");
                    if (minimo == null || momento.compareTo(minimo) < 0) minimo = momento;
                    if (maximo == null || momento.compareTo(maximo) > 0) maximo = momento;
                }
            }
        } catch (EOFException e) {
            throw new IOException("REGISTRO.SDA cortado", e);
        }

        primeraLectura = minimo == null ? "" : minimo;
        ultimaLectura = maximo == null ? "" : maximo;

        // Un cliente con L (u otro estado distinto de F/K) y algún registrador sin leer sigue pendiente: la
        // liquidación escribe L tras cada registrador (cuentas con activa + reactiva leídas a medias).
        int incompletos = 0;
        for (int i = 0; i < indicador.length; i++) {
            byte ind = indicador[i];
            if (conRegistroPendiente[i] && ind != ' ' && ind != 0 && ind != 'F' && ind != 'K') incompletos++;
        }
        clientesProcesados -= incompletos;
        clientesIncompletos = incompletos;
        if (primerClientePendiente == 0) {
            // Sin registros pendientes puede quedar un cliente en blanco (p. ej. sin registradores): la
            // liquidación debe abrir en ese.
            for (int i = 0; i < indicador.length; i++) {
                if (indicador[i] == ' ' || indicador[i] == 0) {
                    primerClientePendiente = i + 1;
                    break;
                }
            }
        }

        List<ConteoCausa> lista = new ArrayList<>(conteo.size());
        for (Map.Entry<String, int[]> e : conteo.entrySet()) {
            String d = descripciones.get(e.getKey());
            lista.add(new ConteoCausa(e.getKey(), d == null ? "" : d, e.getValue()[0]));
        }
        Collections.sort(lista, (a, b) -> a.cantidad != b.cantidad ? b.cantidad - a.cantidad : compararCodigo(a.codigo, b.codigo));
        causas = Collections.unmodifiableList(lista);
    }

    // ------------------------------------------------------------------ consultas

    /**
     * Posiciones (1..registros) que cumplen el filtro, en orden de ruta.
     *
     * @param soloCausa con ANOMALIAS, limita a esa causa (código normalizado); null = todas
     * @param texto     busca en cuenta y medidor (ignora ceros a la izquierda); null o vacío = sin búsqueda
     */
    public int[] filtrar(Filtro filtro, String soloCausa, String texto) {
        String q = texto == null ? "" : texto.trim().toUpperCase(Locale.ROOT);
        if (!q.isEmpty() && soloDigitos(q)) q = sinCeros(q);
        int[] r = new int[registros];
        int k = 0;
        for (int i = 0; i < registros; i++) {
            switch (filtro) {
                case PROCESADAS:
                    if (pendiente[i]) continue;
                    break;
                case PENDIENTES:
                    if (!pendiente[i]) continue;
                    break;
                case ANOMALIAS:
                    if (causa[i] == null || (soloCausa != null && !soloCausa.equals(causa[i]))) continue;
                    break;
                default:
                    break;
            }
            if (!q.isEmpty() && !cuenta[i].contains(q) && !medidor[i].toUpperCase(Locale.ROOT).contains(q)) continue;
            r[k++] = i + 1;
        }
        int[] exacto = new int[k];
        System.arraycopy(r, 0, exacto, 0, k);
        return exacto;
    }

    public int contar(Filtro filtro) {
        switch (filtro) {
            case PROCESADAS: return registrosProcesados;
            case PENDIENTES: return registros - registrosProcesados;
            case ANOMALIAS: return registrosConCausa;
            default: return registros;
        }
    }

    public int clienteDe(int posicion) {
        return cliente[posicion - 1];
    }

    public boolean esPendiente(int posicion) {
        return pendiente[posicion - 1];
    }

    public Critica criticaDe(int posicion) {
        int i = posicion - 1;
        if (pendiente[i]) return Critica.PENDIENTE;
        byte l = leido[i];
        if (l != ' ' && l != 0) return Critica.de(l);
        return causa[i] != null ? Critica.SIN_LECTURA : Critica.PROCESADA;
    }

    /** Código normalizado de la causa o null. */
    public String causaDe(int posicion) {
        return causa[posicion - 1];
    }

    public String descripcionCausa(String codigo) {
        if (codigo == null) return "";
        for (ConteoCausa c : causas) if (c.codigo.equals(codigo)) return c.descripcion;
        return "";
    }

    public int getRegistros() { return registros; }
    public int getRegistrosProcesados() { return registrosProcesados; }
    public int getRegistrosPendientes() { return registros - registrosProcesados; }
    public int getRegistrosConCausa() { return registrosConCausa; }
    public int getRegistrosConComentario() { return registrosConComentario; }
    public int getRegistrosConInforme() { return registrosConInforme; }
    public int getPorCritica(Critica c) { return porCritica[c.ordinal()]; }
    public List<ConteoCausa> getCausas() { return causas; }

    public int getTotalClientes() { return totalClientes; }
    public int getClientesProcesados() { return clientesProcesados; }
    public int getClientesPendientes() { return Math.max(0, totalClientes - clientesProcesados); }
    public int getClientesImpresos() { return clientesImpresos; }
    public int getClientesSinImprimir() { return clientesSinImprimir; }
    public int getClientesSoloLectura() { return clientesSoloLectura; }
    public int getClientesOtroEstado() { return clientesOtroEstado; }
    /** Clientes con L (u otro) y algún registrador sin leer: cuentan como pendientes. */
    public int getClientesIncompletos() { return clientesIncompletos; }

    /** Posición del primer registro pendiente (0 = ninguno). */
    public int getPrimerRegistroPendiente() { return primerRegistroPendiente; }

    /** Cliente (índice en CLIENTE.TXT) donde debe abrir la liquidación (0 = ruta terminada). */
    public int getPrimerClientePendiente() { return primerClientePendiente; }

    /** AAAAMMDDhhmmss o vacío. */
    public String getPrimeraLectura() { return primeraLectura; }
    public String getUltimaLectura() { return ultimaLectura; }
    public int getProcesadosHoy() { return procesadosHoy; }

    public String getAnioPeriodo() { return anioPeriodo; }
    public String getMesPeriodo() { return mesPeriodo; }
    public String getMunicipio() { return municipio; }

    /** Porcentaje de clientes procesados (0..100). */
    public int getAvance() {
        return totalClientes <= 0 ? 0 : (int) Math.min(100, (clientesProcesados * 100L) / totalClientes);
    }

    // ------------------------------------------------------------------ utilidades

    private static DataInputStream abrir(File f) throws IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(f), 64 * 1024);
        return new DataInputStream(in);
    }

    static String texto(byte[] b, int desde, int largo) {
        return new String(b, desde, largo, LATIN1).trim();
    }

    private static boolean vacio(byte[] b, int desde, int largo) {
        for (int i = desde; i < desde + largo; i++) {
            if (b[i] != ' ' && b[i] != 0) return false;
        }
        return true;
    }

    private static int entero(byte[] b, int desde, int largo) {
        int v = 0;
        boolean alguno = false;
        for (int i = desde; i < desde + largo; i++) {
            byte c = b[i];
            if (c >= '0' && c <= '9') {
                v = v * 10 + (c - '0');
                alguno = true;
            } else if (c != ' ') {
                return 0;
            }
        }
        return alguno ? v : 0;
    }

    static String sinCeros(String s) {
        int i = 0;
        while (i < s.length() - 1 && s.charAt(i) == '0') i++;
        return s.substring(i);
    }

    static boolean soloDigitos(String s) {
        if (s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) if (!Character.isDigit(s.charAt(i))) return false;
        return true;
    }

    private static int compararCodigo(String a, String b) {
        if (soloDigitos(a) && soloDigitos(b) && a.length() < 10 && b.length() < 10) {
            return Integer.compare(Integer.parseInt(a), Integer.parseInt(b));
        }
        return a.compareTo(b);
    }

    private static String limpiar(String s) {
        return s == null ? "" : s.trim().replaceAll("\\s+", " ");
    }
}
