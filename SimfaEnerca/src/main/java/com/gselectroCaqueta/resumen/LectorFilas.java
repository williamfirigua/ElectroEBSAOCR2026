package com.gselectroCaqueta.resumen;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Datos para mostrar una fila del resumen, leídos bajo demanda: la lista solo pide las filas visibles, así no
 * hay que cargar en memoria nombre y dirección de toda la ruta (CLIENTE.TXT pesa 1.847 bytes por cliente).
 * Cada fila cuesta dos lecturas posicionadas; las últimas {@value #CACHE} quedan en memoria para el scroll.
 *
 * Solo lectura ("r"): nunca escribe. Abierto desde la liquidación (consulta) puede mostrar una fila a medio
 * escribir; se corrige al volver a abrirla.
 */
public final class LectorFilas implements Closeable {

    private static final int CACHE = 160;
    // CLIENTE.TXT (mismos desplazamientos que TablaEntradaClientes.lectura_TablaEntradaClientesII)
    private static final int T_RUTA = 25, T_RUTA_L = 16;
    private static final int T_NOMBRE = 80, T_NOMBRE_L = 48;
    private static final int T_DIRECCION = 129, T_DIRECCION_L = 60;
    private static final int T_LEER = T_DIRECCION + T_DIRECCION_L;

    public static final class Fila {
        public final int posicion;
        public final int cliente;
        public final String cuenta;
        public final String medidor;
        /** Sin ceros a la izquierda; vacío si no hay lectura. */
        public final String lectura;
        public final String causa;
        public final String comentario;
        public final String informe;
        public final String nombre;
        public final String direccion;
        public final String ruta;

        Fila(int posicion, int cliente, String cuenta, String medidor, String lectura, String causa, String comentario,
             String informe, String nombre, String direccion, String ruta) {
            this.posicion = posicion;
            this.cliente = cliente;
            this.cuenta = cuenta;
            this.medidor = medidor;
            this.lectura = lectura;
            this.causa = causa;
            this.comentario = comentario;
            this.informe = informe;
            this.nombre = nombre;
            this.direccion = direccion;
            this.ruta = ruta;
        }
    }

    private final RandomAccessFile registro;
    private final RandomAccessFile clientes; // puede ser null si falta CLIENTE.TXT
    private final long totalRegistros;
    private final long totalClientes;
    private final byte[] bufRegistro = new byte[ResumenRuta.LONGITUD_REGISTRO];
    private final byte[] bufCliente = new byte[T_LEER];
    private final Map<Integer, Fila> cache = new LinkedHashMap<Integer, Fila>(CACHE, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Integer, Fila> e) {
            return size() > CACHE;
        }
    };

    public LectorFilas(File dirTrabajo) throws IOException {
        registro = new RandomAccessFile(new File(dirTrabajo, "DATOSDESALIDA/REGISTRO.SDA"), "r");
        totalRegistros = registro.length() / ResumenRuta.LONGITUD_REGISTRO;
        File txt = new File(dirTrabajo, "DATOSDEENTRADA/CLIENTE.TXT");
        RandomAccessFile c = null;
        long tc = 0;
        if (txt.isFile()) {
            c = new RandomAccessFile(txt, "r");
            tc = c.length() / ResumenRuta.LONGITUD_CLIENTE_TXT;
        }
        clientes = c;
        totalClientes = tc;
    }

    /** Fila de la posición (1..n) o null si no se pudo leer. */
    public synchronized Fila fila(int posicion) {
        Fila f = cache.get(posicion);
        if (f != null) return f;
        if (posicion < 1 || posicion > totalRegistros) return null;
        try {
            registro.seek((long) (posicion - 1) * ResumenRuta.LONGITUD_REGISTRO);
            registro.readFully(bufRegistro);
        } catch (IOException e) {
            return null;
        }
        byte[] b = bufRegistro;
        String sCliente = ResumenRuta.texto(b, ResumenRuta.R_CLIENTE, ResumenRuta.R_CLIENTE_L);
        int cli = ResumenRuta.soloDigitos(sCliente) ? Integer.parseInt(sCliente) : 0;
        String lectura = ResumenRuta.texto(b, ResumenRuta.R_LECTURA, ResumenRuta.R_LECTURA_L);
        if (!lectura.isEmpty() && ResumenRuta.soloDigitos(lectura)) lectura = ResumenRuta.sinCeros(lectura);

        String nombre = "", direccion = "", ruta = "";
        if (clientes != null && cli >= 1 && cli <= totalClientes) {
            try {
                clientes.seek((long) (cli - 1) * ResumenRuta.LONGITUD_CLIENTE_TXT);
                clientes.readFully(bufCliente);
                // CLIENTE.TXT se lee en UTF-8 en toda la app (TablaEntradaClientes)
                nombre = new String(bufCliente, T_NOMBRE, T_NOMBRE_L, StandardCharsets.UTF_8).trim();
                direccion = new String(bufCliente, T_DIRECCION, T_DIRECCION_L, StandardCharsets.UTF_8).trim();
                ruta = new String(bufCliente, T_RUTA, T_RUTA_L, StandardCharsets.UTF_8).trim();
            } catch (IOException ignored) {
                // se muestra la fila sin nombre ni dirección
            }
        }
        f = new Fila(posicion, cli,
                ResumenRuta.sinCeros(ResumenRuta.texto(b, ResumenRuta.R_CUENTA, ResumenRuta.R_CUENTA_L)),
                ResumenRuta.sinCeros(ResumenRuta.texto(b, ResumenRuta.R_MEDIDOR, ResumenRuta.R_MEDIDOR_L)),
                lectura,
                ResumenRuta.texto(b, ResumenRuta.R_CAUSA, ResumenRuta.R_CAUSA_L),
                ResumenRuta.texto(b, ResumenRuta.R_COMENTARIO, ResumenRuta.R_COMENTARIO_L),
                ResumenRuta.texto(b, ResumenRuta.R_INFORME, ResumenRuta.R_INFORME_L),
                nombre, direccion, ruta);
        cache.put(posicion, f);
        return f;
    }

    @Override
    public synchronized void close() {
        cache.clear();
        try {
            registro.close();
        } catch (IOException ignored) {
        }
        if (clientes != null) {
            try {
                clientes.close();
            } catch (IOException ignored) {
            }
        }
    }
}
