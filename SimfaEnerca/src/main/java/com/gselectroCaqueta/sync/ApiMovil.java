package com.gselectroCaqueta.sync;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Cliente de /api/v1 con HttpURLConnection (en Android corre sobre OkHttp interno; sin dependencias).
 * No reintenta ni decide nada: devuelve código + cuerpo y el motor decide. Todo el tráfico es JSON en
 * UTF-8, menos la subida de archivos (multipart).
 */
public class ApiMovil {

    /** No se pudo conectar (sin señal, DNS, servidor apagado): no tiene sentido seguir el ciclo. */
    public static final int SIN_RED = -1;
    /** Conectó pero la transferencia se cortó o el servidor no respondió a tiempo: reintentar ese elemento. */
    public static final int TRANSFERENCIA_FALLIDA = -2;

    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final int CONECTAR_MS = 10_000;
    private static final int LEER_MS = 45_000;
    private static final int LEER_ARCHIVO_MS = 90_000;
    /** CO_COBRO completo pesa 5-17 MB y el servidor procesa miles de líneas: margen amplio. */
    private static final int LEER_PLANOS_MS = 300_000;
    private static final byte[] CRLF = {'\r', '\n'};

    /** Respuesta HTTP. codigo = SIN_RED si no hubo conexión (sin señal, DNS, timeout). */
    public static final class Respuesta {
        public final int codigo;
        public final Map<String, Object> cuerpo;
        public final String error;

        public Respuesta(int codigo, Map<String, Object> cuerpo, String error) {
            this.codigo = codigo;
            this.cuerpo = cuerpo;
            this.error = error;
        }

        public boolean ok() {
            return codigo == 200;
        }

        public String campo(String clave) {
            return Json.texto(cuerpo, clave);
        }

        @Override
        public String toString() {
            return codigo + (error != null ? " " + error : "") + (cuerpo != null ? " " + Json.escribir(cuerpo) : "");
        }
    }

    private final String base; // http://host:914/api/v1/
    private volatile String token;

    /** @param baseApi cadenaURLapi de la app (http://host:puertoApi/api/); se le agrega v1/. */
    public ApiMovil(String baseApi) {
        String b = baseApi.trim();
        if (!b.endsWith("/")) b += "/";
        if (!b.endsWith("v1/")) b += "v1/";
        this.base = b;
    }

    public String base() {
        return base;
    }

    public void token(String token) {
        this.token = token;
    }

    public String token() {
        return token;
    }

    // ------------------------------------------------------------------ endpoints

    public Respuesta ping() {
        return json("GET", "ping", null, false, LEER_MS);
    }

    public Respuesta login(String codOperador, String clave, String serial, String version) {
        Map<String, Object> b = new LinkedHashMap<>();
        b.put("cod_operador", codOperador);
        b.put("clave", clave);
        b.put("serial_terminal", serial);
        b.put("version_app", version);
        return json("POST", "auth/login", Json.escribir(b), false, LEER_MS);
    }

    /** @param itemsJson ítems ya serializados (como se guardaron en la cola) */
    public Respuesta enviarLiquidaciones(Iterable<String> itemsJson) {
        StringBuilder sb = new StringBuilder("{\"items\":[");
        boolean primero = true;
        for (String i : itemsJson) {
            if (!primero) sb.append(',');
            sb.append(i);
            primero = false;
        }
        sb.append("]}");
        return json("POST", "liquidaciones/lote", sb.toString(), true, LEER_MS);
    }

    public Respuesta enviarUbicaciones(Iterable<String> puntosJson) {
        StringBuilder sb = new StringBuilder("{\"ubicaciones\":[");
        boolean primero = true;
        for (String p : puntosJson) {
            if (!primero) sb.append(',');
            sb.append(p);
            primero = false;
        }
        sb.append("]}");
        return json("POST", "tracking/ubicaciones", sb.toString(), true, LEER_MS);
    }

    public Respuesta configTracking() {
        return json("GET", "tracking/config", null, true, LEER_MS);
    }

    /**
     * Un archivo (foto o LBL) con sus campos; calcula el SHA-256 que exige el servidor.
     *
     * Se envía con Content-Length fijo, NO chunked: PHP detrás de FastCGI (IIS, Apache con mod_fcgid, algunos
     * proxies) no recibe cuerpos chunked y la petición se queda esperando hasta el timeout. El tamaño se calcula
     * antes y el archivo se transmite por streaming (no se carga en memoria).
     */
    public Respuesta subirArchivo(File archivo, Map<String, String> campos) {
        try {
            Map<String, String> todos = new LinkedHashMap<>(campos);
            todos.put("sha256", sha256(archivo));
            Map<String, File> archivos = new LinkedHashMap<>();
            archivos.put("archivo", archivo);
            return multipart("archivos", todos, archivos, LEER_ARCHIVO_MS);
        } catch (IOException e) {
            return new Respuesta(TRANSFERENCIA_FALLIDA, null, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /**
     * Carga de respaldo: ENVIOSGPRS.SDA (+ CO_COBRO.SDA) a POST /liquidaciones/planos. El servidor descarta
     * lo que ya llegó por JSON (sale "duplicado"), así que subirlo es seguro aunque casi todo ya esté.
     */
    public Respuesta subirPlanos(File envio, File cobros) {
        Map<String, File> archivos = new LinkedHashMap<>();
        archivos.put("envio", envio);
        if (cobros != null && cobros.isFile()) archivos.put("cobros", cobros);
        return multipart("liquidaciones/planos", new LinkedHashMap<String, String>(), archivos, LEER_PLANOS_MS);
    }

    /**
     * multipart/form-data con Content-Length fijo, NO chunked: PHP detrás de FastCGI (IIS, Apache con
     * mod_fcgid, algunos proxies) no recibe cuerpos chunked y la petición se queda esperando hasta el timeout.
     * El tamaño se calcula antes y los archivos van por streaming (no se cargan en memoria).
     */
    private Respuesta multipart(String ruta, Map<String, String> campos, Map<String, File> archivos, int leerMs) {
        String limite = "----ebsa" + UUID.randomUUID().toString().replace("-", "");
        HttpURLConnection c = null;
        try {
            StringBuilder cab = new StringBuilder();
            for (Map.Entry<String, String> e : campos.entrySet()) cab.append(parte(limite, e.getKey(), e.getValue()));
            byte[] inicio = cab.toString().getBytes(UTF8);
            Map<String, byte[]> cabeceras = new LinkedHashMap<>();
            long largo = inicio.length;
            for (Map.Entry<String, File> a : archivos.entrySet()) {
                byte[] h = ("--" + limite + "\r\nContent-Disposition: form-data; name=\"" + a.getKey() + "\"; filename=\""
                        + a.getValue().getName().replace("\"", "") + "\"\r\nContent-Type: application/octet-stream\r\n\r\n").getBytes(UTF8);
                cabeceras.put(a.getKey(), h);
                largo += h.length + a.getValue().length() + 2; // + CRLF tras el archivo
            }
            byte[] fin = ("--" + limite + "--\r\n").getBytes(UTF8);
            largo += fin.length;

            c = abrir("POST", ruta, true, leerMs);
            c.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + limite);
            c.setDoOutput(true);
            c.setFixedLengthStreamingMode(largo);
            if (!conectar(c)) return new Respuesta(SIN_RED, null, "sin conexión con " + base);
            try (OutputStream o = c.getOutputStream()) {
                o.write(inicio);
                byte[] buf = new byte[16 * 1024];
                for (Map.Entry<String, File> a : archivos.entrySet()) {
                    o.write(cabeceras.get(a.getKey()));
                    try (InputStream in = new FileInputStream(a.getValue())) {
                        int n;
                        while ((n = in.read(buf)) > 0) o.write(buf, 0, n);
                    }
                    o.write(CRLF);
                }
                o.write(fin);
            }
            return respuesta(c);
        } catch (java.net.HttpRetryException e) {
            // Con streaming el JDK no deja leer el cuerpo de un 401: se informa el código, que es lo que importa.
            return new Respuesta(e.responseCode(), null, "HttpRetryException");
        } catch (IOException e) {
            return new Respuesta(TRANSFERENCIA_FALLIDA, null, e.getClass().getSimpleName() + ": " + e.getMessage());
        } finally {
            if (c != null) c.disconnect();
        }
    }

    /** Abre el socket; falla aquí = no hay red / servidor inalcanzable (distinto de un servidor lento). */
    private static boolean conectar(HttpURLConnection c) {
        try {
            c.connect();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // ------------------------------------------------------------------ transporte

    private Respuesta json(String metodo, String ruta, String cuerpo, boolean conToken, int leerMs) {
        HttpURLConnection c = null;
        try {
            c = abrir(metodo, ruta, conToken, leerMs);
            if (cuerpo != null) {
                c.setDoOutput(true);
                c.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            }
            if (!conectar(c)) return new Respuesta(SIN_RED, null, "sin conexión con " + base);
            if (cuerpo != null) {
                // Sin modo streaming a propósito: con él, un 401 deja el cuerpo de error inaccesible en
                // algunas implementaciones (HttpRetryException) y se pierde el motivo. Los lotes son chicos.
                try (OutputStream o = c.getOutputStream()) {
                    o.write(cuerpo.getBytes(UTF8));
                }
            }
            return respuesta(c);
        } catch (IOException e) {
            // Ya había conexión: el servidor tardó más que el timeout o se cortó a mitad. El elemento se
            // reintenta; el servidor descarta duplicados, así que reenviar es seguro.
            return new Respuesta(TRANSFERENCIA_FALLIDA, null, e.getClass().getSimpleName() + ": " + e.getMessage());
        } finally {
            if (c != null) c.disconnect();
        }
    }

    private HttpURLConnection abrir(String metodo, String ruta, boolean conToken, int leerMs) throws IOException {
        HttpURLConnection c = (HttpURLConnection) new URL(base + ruta).openConnection();
        c.setRequestMethod(metodo);
        c.setConnectTimeout(CONECTAR_MS);
        c.setReadTimeout(leerMs);
        c.setUseCaches(false);
        c.setRequestProperty("Accept", "application/json");
        String t = token;
        if (conToken && t != null) c.setRequestProperty("Authorization", "Bearer " + t);
        return c;
    }

    private static Respuesta respuesta(HttpURLConnection c) throws IOException {
        int codigo = c.getResponseCode();
        InputStream in = codigo >= 400 ? c.getErrorStream() : c.getInputStream();
        String texto = in == null ? "" : leerTodo(in);
        Map<String, Object> cuerpo = null;
        String error = null;
        if (!texto.isEmpty()) {
            try {
                cuerpo = Json.objeto(texto);
            } catch (RuntimeException e) {
                // proxy, página de error de IIS, etc.
                error = "respuesta no JSON: " + (texto.length() > 120 ? texto.substring(0, 120) : texto);
            }
        }
        return new Respuesta(codigo, cuerpo, error);
    }

    private static String parte(String limite, String nombre, String valor) {
        return "--" + limite + "\r\nContent-Disposition: form-data; name=\"" + nombre + "\"\r\n\r\n"
                + (valor == null ? "" : valor) + "\r\n";
    }

    private static String leerTodo(InputStream in) throws IOException {
        try (InputStream i = in) {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = i.read(buf)) > 0) b.write(buf, 0, n);
            return new String(b.toByteArray(), UTF8);
        }
    }

    public static String sha256(File f) throws IOException {
        try (InputStream in = new FileInputStream(f)) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[16 * 1024];
            int n;
            while ((n = in.read(buf)) > 0) md.update(buf, 0, n);
            StringBuilder sb = new StringBuilder(64);
            for (byte x : md.digest()) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
    }
}
