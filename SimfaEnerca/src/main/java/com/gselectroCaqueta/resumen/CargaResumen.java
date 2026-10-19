package com.gselectroCaqueta.resumen;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.gselectroCaqueta.accesoyseguridad.VariablesGlobales;
import com.gselectroCaqueta.moduloanomalias.AnomaliaDeNoLectura;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Calcula el resumen de la ruta fuera del hilo de la pantalla y lo entrega en el hilo principal.
 * Lo usan MenuPrincipal y ResumenEstadistico: un solo cálculo y una sola definición de los conteos.
 */
public final class CargaResumen {

    public interface Listo {
        void alTerminar(Resultado r);
    }

    public static final class Resultado {
        /** null si no hay ruta cargada o no se pudo leer (ver error). */
        public final ResumenRuta resumen;
        /** Lo actualiza MenuPrincipal al ir a borrar (la cola cambia en segundo plano). */
        public volatile EstadoEnvios envios;
        /** "0011" del archivo NOMBRE, vacío si no hay ruta. */
        public final String ciclo;
        /** "01010010" (municipio + sección + división) del archivo NOMBRE. */
        public final String ruta;
        public final String municipio;
        public final String error;

        Resultado(ResumenRuta resumen, EstadoEnvios envios, String ciclo, String ruta, String municipio, String error) {
            this.resumen = resumen;
            this.envios = envios;
            this.ciclo = ciclo;
            this.ruta = ruta;
            this.municipio = municipio;
            this.error = error;
        }

        public boolean hayRuta() {
            return resumen != null;
        }
    }

    // Un solo hilo: dos pantallas pidiendo a la vez no leen los archivos en paralelo.
    private static final ExecutorService HILO = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "resumen-ruta");
        t.setDaemon(true);
        return t;
    });
    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());

    private CargaResumen() {
    }

    public static void cargar(Context ctx, String dirTrabajo, Listo listo) {
        final Context app = ctx.getApplicationContext();
        HILO.execute(() -> {
            Resultado r = calcular(app, dirTrabajo);
            PRINCIPAL.post(() -> listo.alTerminar(r));
        });
    }

    static Resultado calcular(Context app, String dirTrabajo) {
        EstadoEnvios envios;
        try {
            envios = EstadoEnvios.leer(app);
        } catch (RuntimeException e) {
            envios = null; // la cola no debe impedir ver el resumen
        }
        File dir = new File(dirTrabajo);
        String[] nombre = leerNombre(new File(dir, "DATOSDEENTRADA/NOMBRE"));
        try {
            Map<String, String> causas = ResumenRuta.cargarCausas(new File(dir, "DATOSDEENTRADA/CAUSA_NL.TXT"));
            String hoy = new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date());
            ResumenRuta r = ResumenRuta.calcular(dir, hoy, causas);
            String municipio = descripcionMunicipio(new File(dir, "DATOSDEENTRADA/MUNICIP.TXT"), r.getMunicipio());
            return new Resultado(r, envios, nombre[0], nombre[1], municipio, null);
        } catch (IOException | RuntimeException e) {
            return new Resultado(null, envios, nombre[0], nombre[1], "", e.getMessage() == null ? e.toString() : e.getMessage());
        }
    }

    /**
     * Deja en VariablesGlobales lo que la liquidación espera del resumen (mismos significados que antes):
     * registroactual es el CLIENTE donde abre (MenuDeLiquidacion.ejecutarResumen lo usa como índice de cliente),
     * 0 si la ruta está terminada; totalprediosnofacturados y totalprediosleidos son clientes.
     * Llamar en el hilo principal y solo con la pantalla en primer plano (la liquidación lleva estos contadores).
     */
    public static void publicarGlobales(ResumenRuta r, boolean conRegistroActual) {
        VariablesGlobales.totallecturas = r.getRegistrosProcesados();
        VariablesGlobales.totalregistrosleidos = r.getRegistrosProcesados();
        VariablesGlobales.totalcausasnolectura = r.getRegistrosConCausa();
        VariablesGlobales.totalnuevos = 0;
        VariablesGlobales.totalinformes = r.getRegistrosConInforme();
        VariablesGlobales.totalpredioscomentarios = r.getRegistrosConComentario();
        VariablesGlobales.totalprediosleidos = r.getClientesProcesados();
        VariablesGlobales.totalprediosimpresos = r.getClientesImpresos();
        VariablesGlobales.totalprediosliquidados = r.getClientesSinImprimir();
        VariablesGlobales.totalprediosnofacturados = r.getClientesPendientes();
        if (conRegistroActual) VariablesGlobales.registroactual = r.getPrimerClientePendiente();
    }

    /** {ciclo, ruta}; vacíos si no hay archivo NOMBRE. */
    private static String[] leerNombre(File f) {
        if (!f.isFile() || f.length() < 12) return new String[]{"", ""};
        byte[] b = new byte[12];
        try (RandomAccessFile r = new RandomAccessFile(f, "r")) {
            r.readFully(b);
            String linea = new String(b, StandardCharsets.ISO_8859_1);
            return new String[]{linea.substring(0, 4).trim(), linea.substring(4, 12).trim()};
        } catch (IOException e) {
            return new String[]{"", ""};
        }
    }

    /**
     * Descripción en MUNICIP.TXT (registro de 100 bytes: código 3, descripción en 4..96, como TablaMunicipios).
     * Lineal y en solo lectura: el archivo tiene pocas filas y la búsqueda binaria de TablaMunicipios falla con
     * códigos no numéricos.
     */
    private static String descripcionMunicipio(File f, String codigo) {
        if (!f.isFile() || codigo == null || codigo.isEmpty()) return "";
        final int largo = 100;
        String buscado = AnomaliaDeNoLectura.normalizarCodigo(codigo);
        byte[] b = new byte[largo];
        try (RandomAccessFile r = new RandomAccessFile(f, "r")) {
            long n = r.length() / largo;
            for (long i = 0; i < n; i++) {
                r.readFully(b);
                String c = new String(b, 0, 3, StandardCharsets.UTF_8);
                if (AnomaliaDeNoLectura.normalizarCodigo(c).equals(buscado)) {
                    return new String(b, 4, 93, StandardCharsets.UTF_8).trim();
                }
            }
        } catch (IOException ignored) {
            // solo informativo
        }
        return "";
    }
}
