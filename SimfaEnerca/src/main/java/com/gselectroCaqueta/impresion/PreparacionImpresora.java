package com.gselectroCaqueta.impresion;

import android.content.Context;

import com.gselectroCaqueta.modulobluetooth.btPrintFile;
import com.gselectroCaqueta.sync.ApiMovil;
import com.gselectroCaqueta.sync.DescargaRuta;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Botón "Enviar valores a la impresora" (Config. Preimpreso). El técnico no elige nada: lo decide la web
 * (Caída de datos > Recursos) y el celular solo lo aplica.
 * <ol>
 *   <li>Pide a la web el lenguaje y la lista de archivos (GET impresora/config).</li>
 *   <li>Baja los archivos que no tenga o que cambiaron (SHA-256) a {@code <dir>/RECURSOS_IMPRESORA}.</li>
 *   <li>Arma un solo trabajo: el lenguaje por SGD ({@code ! U1 setvar "device.languages" "line_print"}) y cada
 *       archivo con {@code ! CISDFCRC16} (así se guarda un archivo en la memoria de una Zebra portátil),
 *       con el mismo nombre con que lo nombra la plantilla (DINCOND.TTF, EBSA.PCX...).</li>
 *   <li>Lo manda por la cola de impresión (nada se intercala con una factura) y espera a que salga.</li>
 * </ol>
 * Corre en un hilo de fondo: hace red y espera la cola.
 */
public final class PreparacionImpresora {

    public static final String CARPETA = "RECURSOS_IMPRESORA";
    public static final String REGISTRO = "ImpresoraPreparada.log";
    private static final Charset LATIN1 = Charset.forName("ISO-8859-1");
    private static final Pattern NOMBRE = Pattern.compile("^[A-Za-z0-9_.\\-]{1,64}$");
    private static final Pattern SGD = Pattern.compile("^[A-Za-z0-9_.\\-]{1,64}$");
    private static final long ESPERA_COLA_MS = 180_000;

    public interface Avance {
        void paso(String texto);
    }

    public static final class Resultado {
        public final boolean ok;
        public final String mensaje;

        Resultado(boolean ok, String mensaje) {
            this.ok = ok;
            this.mensaje = mensaje;
        }
    }

    private PreparacionImpresora() {
    }

    public static Resultado preparar(Context ctx, File dirTrabajo, btPrintFile servicio, Avance avance) {
        if (servicio == null || !servicio.tieneImpresoraAsociada()) {
            return new Resultado(false, "No hay impresora asociada. Asóciela primero (menú Impresión > Asociar impresora).");
        }
        avance.paso("Consultando el servidor...");
        DescargaRuta api = new DescargaRuta(ctx);
        DescargaRuta.Operacion op = api.configImpresora();
        if (!op.ok || op.cuerpo == null) {
            return new Resultado(false, "No se pudo leer la configuración del servidor. " + (op.mensaje != null ? op.mensaje : ""));
        }

        // Valores SGD (lenguaje). Solo claves y valores simples: van entre comillas en el comando.
        List<String[]> sgd = new ArrayList<>();
        Object listaSgd = op.cuerpo.get("sgd");
        if (listaSgd instanceof List) {
            for (Object o : (List<?>) listaSgd) {
                if (!(o instanceof Map)) continue;
                Object c = ((Map<?, ?>) o).get("clave"), v = ((Map<?, ?>) o).get("valor");
                if (c != null && v != null && SGD.matcher(c.toString()).matches() && SGD.matcher(v.toString()).matches()) {
                    sgd.add(new String[]{c.toString(), v.toString()});
                }
            }
        }

        // Archivos: se bajan solo si faltan o cambiaron.
        File carpeta = new File(dirTrabajo, CARPETA);
        if (!carpeta.isDirectory() && !carpeta.mkdirs()) {
            return new Resultado(false, "No se pudo crear la carpeta " + carpeta.getPath());
        }
        List<File> archivos = new ArrayList<>();
        Object listaArch = op.cuerpo.get("archivos");
        if (listaArch instanceof List) {
            for (Object o : (List<?>) listaArch) {
                if (!(o instanceof Map)) continue;
                Object n = ((Map<?, ?>) o).get("nombre"), h = ((Map<?, ?>) o).get("sha256");
                if (n == null || !NOMBRE.matcher(n.toString()).matches()) continue;
                String nombre = n.toString().toUpperCase(Locale.ROOT);
                String sha = h == null ? "" : h.toString().trim();
                File f = new File(carpeta, nombre);
                if (!(f.isFile() && !sha.isEmpty() && sha.equalsIgnoreCase(sha256(f)))) {
                    avance.paso("Bajando " + nombre + "...");
                    DescargaRuta.Operacion b = api.bajar("impresora/archivo?nombre=" + nombre, f, null);
                    if (!b.ok) return new Resultado(false, "No se pudo bajar " + nombre + ". " + (b.mensaje != null ? b.mensaje : ""));
                }
                archivos.add(f);
            }
        }
        if (sgd.isEmpty() && archivos.isEmpty()) {
            return new Resultado(false, "El servidor no tiene nada para la impresora.");
        }

        // Un solo trabajo: primero el lenguaje, después los archivos.
        ByteArrayOutputStream trabajo = new ByteArrayOutputStream();
        StringBuilder resumen = new StringBuilder();
        try {
            for (String[] s : sgd) {
                trabajo.write(setvar(s[0], s[1]));
                resumen.append(s[0]).append(" = ").append(s[1]).append('\n');
            }
            for (File f : archivos) {
                byte[] datos = leer(f);
                trabajo.write(cisdf(f.getName(), datos));
                resumen.append(f.getName()).append(" (").append(datos.length).append(" bytes)\n");
            }
        } catch (IOException e) {
            return new Resultado(false, "No se pudo leer un archivo: " + e.getMessage());
        }

        avance.paso("Enviando a la impresora (" + (trabajo.size() / 1024) + " KB)...");
        final boolean[] ok = {false};
        final String[] detalle = {""};
        final CountDownLatch fin = new CountDownLatch(1);
        boolean encolado = ColaImpresion.encolar(servicio, trabajo.toByteArray(), "valores de impresora", (exito, d) -> {
            ok[0] = exito;
            detalle[0] = d;
            fin.countDown();
        });
        if (!encolado) return new Resultado(false, "No se pudo enviar a la impresora.");
        try {
            if (!fin.await(ESPERA_COLA_MS, TimeUnit.MILLISECONDS)) {
                return new Resultado(false, "La impresora no terminó de recibir. Revise que esté encendida y cerca, y vuelva a intentar.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new Resultado(false, "Se interrumpió el envío.");
        }
        if (!ok[0]) return new Resultado(false, "Falló el envío: " + detalle[0]);

        String cuando = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        registrar(dirTrabajo, cuando + "\n" + resumen);
        return new Resultado(true, "Listo (" + cuando + "):\n" + resumen.toString().trim());
    }

    /** Lo último que se mandó (para mostrarlo en la pantalla), o null. */
    public static String ultimoEnvio(File dirTrabajo) {
        File f = new File(dirTrabajo, REGISTRO);
        if (!f.isFile()) return null;
        try {
            return new String(leer(f), Charset.forName("UTF-8")).trim();
        } catch (IOException e) {
            return null;
        }
    }

    /** {@code ! U1 setvar "clave" "valor"} */
    static byte[] setvar(String clave, String valor) {
        return ("! U1 setvar \"" + clave + "\" \"" + valor + "\"\r\n").getBytes(LATIN1);
    }

    /**
     * Guarda un archivo en la impresora (Zebra portátil, CPCL):
     * {@code ! CISDFCRC16 / CRC / NOMBRE / tamaño en 8 hex / checksum / datos}. CRC y checksum en 0000 = sin
     * verificación (el Bluetooth ya la hace).
     */
    static byte[] cisdf(String nombre, byte[] datos) throws IOException {
        ByteArrayOutputStream o = new ByteArrayOutputStream(datos.length + 64);
        o.write(("! CISDFCRC16\r\n0000\r\n" + nombre.toUpperCase(Locale.ROOT) + "\r\n"
                + String.format(Locale.ROOT, "%08X", datos.length) + "\r\n0000\r\n").getBytes(LATIN1));
        o.write(datos);
        return o.toByteArray();
    }

    private static void registrar(File dir, String texto) {
        try (FileOutputStream o = new FileOutputStream(new File(dir, REGISTRO))) {
            o.write(texto.getBytes(Charset.forName("UTF-8")));
        } catch (IOException ignorado) {
            // solo es para mostrar el último envío
        }
    }

    private static String sha256(File f) {
        try {
            return ApiMovil.sha256(f);
        } catch (IOException e) {
            return "";
        }
    }

    private static byte[] leer(File f) throws IOException {
        ByteArrayOutputStream o = new ByteArrayOutputStream((int) Math.max(0, f.length()));
        try (InputStream in = new FileInputStream(f)) {
            byte[] b = new byte[8192];
            int n;
            while ((n = in.read(b)) > 0) o.write(b, 0, n);
        }
        return o.toByteArray();
    }
}
