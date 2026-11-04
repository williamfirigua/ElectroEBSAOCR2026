package com.gselectroCaqueta.accesoyseguridad;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Codigos de supervisor de un solo uso, validados en la terminal SIN senal. Mismo algoritmo que la web
 * (App\Movil\Mensajeria\CodigoSupervisor):
 * <pre>
 *   codigo  = truncado dinamico (RFC 4226) de HMAC-SHA256(clave_tecnico, cod_cuenta + "|" + ventana) % 10^6
 *   ventana = epoch / 900 s; se aceptan la anterior, la actual y la siguiente (reloj desfasado)
 * </pre>
 * clave_tecnico la baja el servicio de sincronizacion (GET /codigos/claves), una por supervisor de la empresa.
 * Un codigo usado se guarda y no vuelve a servir en este equipo; la oficina ve el uso con el evento.
 */
public final class CodigoSupervisor {

    public static final long VENTANA_MS = 900_000L;
    private static final String K_CLAVES = "claves", K_DESCARGA = "descarga", K_USADOS = "usados";
    private static final int MAX_USADOS = 300;

    public static final class Clave {
        public final String supervisor, nombre, claveHex;

        public Clave(String supervisor, String nombre, String claveHex) {
            this.supervisor = supervisor;
            this.nombre = nombre;
            this.claveHex = claveHex;
        }
    }

    private final AlmacenEstado almacen;

    public CodigoSupervisor(AlmacenEstado almacen) {
        this.almacen = almacen;
    }

    // ------------------------------------------------------------------ claves descargadas

    /** Guarda las claves: "supervisor\tnombre\tclave" por linea. */
    public void guardarClaves(List<Clave> claves, long ahoraMs) {
        StringBuilder b = new StringBuilder();
        for (Clave c : claves) {
            if (!c.claveHex.matches("[0-9a-fA-F]{64}")) continue;
            b.append(limpio(c.supervisor)).append('\t').append(limpio(c.nombre)).append('\t').append(c.claveHex.toLowerCase()).append('\n');
        }
        almacen.guardar(K_CLAVES, b.toString());
        almacen.guardar(K_DESCARGA, String.valueOf(ahoraMs));
    }

    public List<Clave> claves() {
        List<Clave> r = new ArrayList<>();
        String s = almacen.leer(K_CLAVES);
        if (s == null) return r;
        for (String linea : s.split("\n")) {
            String[] p = linea.split("\t", -1);
            if (p.length == 3) r.add(new Clave(p[0], p[1], p[2]));
        }
        return r;
    }

    public long descargadasEn() {
        try {
            String s = almacen.leer(K_DESCARGA);
            return s == null || s.isEmpty() ? 0 : Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** Cambio de tecnico: las claves son por tecnico. Se vuelven a bajar en el proximo latido. */
    public void olvidarClaves() {
        almacen.borrar(K_CLAVES, K_DESCARGA);
    }

    public boolean hayClaves() {
        return !claves().isEmpty();
    }

    // ------------------------------------------------------------------ validacion

    /**
     * Supervisor cuyo codigo coincide para esta cuenta, o null. No lo marca como usado: eso se hace con
     * {@link #marcarUsado} cuando la autorizacion se aplica.
     */
    public Clave validar(String codigo, String codCuenta, long ahoraMs) {
        String c = codigo == null ? "" : codigo.trim();
        if (!c.matches("\\d{6}") || yaUsado(c, codCuenta)) return null;
        long v = ahoraMs / VENTANA_MS;
        for (Clave k : claves()) {
            for (long w = v - 1; w <= v + 1; w++) {
                if (iguales(codigo(k.claveHex, codCuenta, w), c)) return k;
            }
        }
        return null;
    }

    public boolean yaUsado(String codigo, String codCuenta) {
        String u = almacen.leer(K_USADOS);
        return u != null && ("\n" + u).contains("\n" + limpio(codCuenta) + "|" + codigo.trim() + "\n");
    }

    public void marcarUsado(String codigo, String codCuenta) {
        String u = almacen.leer(K_USADOS);
        List<String> lineas = new ArrayList<>();
        if (u != null) for (String l : u.split("\n")) if (!l.isEmpty()) lineas.add(l);
        lineas.add(limpio(codCuenta) + "|" + codigo.trim());
        while (lineas.size() > MAX_USADOS) lineas.remove(0);
        StringBuilder b = new StringBuilder();
        for (String l : lineas) b.append(l).append('\n');
        almacen.guardar(K_USADOS, b.toString());
    }

    public static String codigo(String claveHex, String codCuenta, long ventana) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(hex(claveHex), "HmacSHA256"));
            byte[] h = mac.doFinal((limpio(codCuenta) + "|" + ventana).getBytes(StandardCharsets.UTF_8));
            int o = h[31] & 0x0F;
            int n = ((h[o] & 0x7F) << 24) | ((h[o + 1] & 0xFF) << 16) | ((h[o + 2] & 0xFF) << 8) | (h[o + 3] & 0xFF);
            String s = String.valueOf(n % 1_000_000);
            while (s.length() < 6) s = "0" + s;
            return s;
        } catch (Exception e) {
            return "--------"; // nunca coincide
        }
    }

    /** Comparacion en tiempo constante. */
    private static boolean iguales(String a, String b) {
        if (a.length() != b.length()) return false;
        int d = 0;
        for (int i = 0; i < a.length(); i++) d |= a.charAt(i) ^ b.charAt(i);
        return d == 0;
    }

    private static byte[] hex(String s) {
        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++) b[i] = (byte) Integer.parseInt(s.substring(2 * i, 2 * i + 2), 16);
        return b;
    }

    private static String limpio(String s) {
        return s == null ? "" : s.trim().replace('\t', ' ').replace('\n', ' ');
    }
}
