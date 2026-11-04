package com.gselectroCaqueta.accesoyseguridad;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Cómo recorre el técnico la ruta: la materia prima del reordenamiento.
 * <ul>
 *   <li><b>Secuencia</b>: cada cuenta leída (o con causal) con la cuenta leída antes, la posición de las dos en
 *       el archivo de la ruta (orden de CLIENTE), el salto, los segundos entre una y otra y cómo llegó la
 *       pantalla a la cuenta (siguiente, anterior, búsqueda, último leído, primero/último).</li>
 *   <li><b>Búsqueda</b>: desde qué cuenta se buscó, por qué criterio, qué se encontró y cuánto tardó. Si la
 *       cuenta encontrada se lee, su secuencia lleva el id de la búsqueda.</li>
 *   <li><b>Contenedor</b>: lo leído entre "Iniciar contenedor" y "Cerrar" es un solo banco de medidores.</li>
 * </ul>
 * Todo el estado vive en {@link AlmacenEstado}: sobrevive a un cierre de la app, así que la primera lectura
 * después de reiniciar sigue sabiendo cuál fue la anterior (antes "último medidor leído" llegaba vacío).
 * Las posiciones son índices del archivo CLIENTE de la ruta, que viene en el orden del itinerario.
 */
public final class SecuenciaCampo {

    public static final String LLEGADA_INICIO = "inicio";
    public static final String LLEGADA_SIGUIENTE = "siguiente";
    public static final String LLEGADA_ANTERIOR = "anterior";
    public static final String LLEGADA_BUSQUEDA = "busqueda";
    public static final String LLEGADA_ULTIMO_LEIDO = "ultimo_leido";
    public static final String LLEGADA_PRIMERO = "primero";
    public static final String LLEGADA_ULTIMO = "ultimo";

    /** Mínimo de cuentas para que un contenedor tenga sentido. */
    public static final int MIN_CUENTAS_CONTENEDOR = 2;

    /** Una cuenta leída, con lo necesario para ubicarla. */
    public static final class Punto {
        public final String cuenta;
        public final String codCuenta;
        public final int posicion;
        public final String ruta;
        public final String medidor;
        public final long ms;
        public final Double lat, lng, alt;

        public Punto(String cuenta, String codCuenta, int posicion, String ruta, String medidor, long ms,
                     Double lat, Double lng, Double alt) {
            this.cuenta = t(cuenta);
            this.codCuenta = t(codCuenta);
            this.posicion = posicion;
            this.ruta = t(ruta);
            this.medidor = t(medidor);
            this.ms = ms;
            this.lat = lat;
            this.lng = lng;
            this.alt = alt;
        }
    }

    private static final String U_CUENTA = "ult.cuenta", U_COD = "ult.cod", U_POS = "ult.pos", U_MEDIDOR = "ult.medidor", U_MS = "ult.ms";
    private static final String L_TIPO = "lleg.tipo", L_CUENTA = "lleg.cuenta", L_MS = "lleg.ms", L_BUSQ = "lleg.busqueda",
            L_CRITERIO = "lleg.criterio";
    /** B_N: busquedas terminadas (encontradas, fallidas o canceladas) desde la ultima lectura. */
    private static final String B_DESDE = "bus.desde", B_DESDE_POS = "bus.desde_pos", B_MS = "bus.ms", B_N = "bus.n";
    private static final String C_ID = "cont.id", C_MS = "cont.ms", C_CUENTAS = "cont.cuentas", C_LAT = "cont.lat", C_LNG = "cont.lng";

    private final AlmacenEstado almacen;

    public SecuenciaCampo(AlmacenEstado almacen) {
        this.almacen = almacen;
    }

    // ------------------------------------------------------------------ llegada a una cuenta

    /** La pantalla quedó en esta cuenta por este camino. Una búsqueda deja su id con {@link #terminarBusqueda}. */
    public void llegada(String tipo, String cuenta, long ms) {
        almacen.guardar(L_TIPO, t(tipo));
        almacen.guardar(L_CUENTA, t(cuenta));
        almacen.guardar(L_MS, String.valueOf(ms));
        almacen.borrar(L_BUSQ, L_CRITERIO);
    }

    // ------------------------------------------------------------------ búsquedas

    /** Hay una búsqueda abierta por el técnico que todavía no termina. */
    public boolean busquedaEnCurso() {
        return !t(almacen.leer(B_MS)).isEmpty();
    }

    /** El técnico abrió la búsqueda estando en esta cuenta. */
    public void iniciarBusqueda(String desdeCuenta, int desdePosicion, long ms) {
        almacen.guardar(B_DESDE, t(desdeCuenta));
        almacen.guardar(B_DESDE_POS, String.valueOf(desdePosicion));
        almacen.guardar(B_MS, String.valueOf(ms));
    }

    /**
     * Resultado de la búsqueda. Si encontró, la llegada a la cuenta queda como búsqueda con este uid.
     *
     * @return datos del evento "busqueda"
     */
    public Map<String, Object> terminarBusqueda(String uid, String criterio, String dato, boolean encontrada,
                                                String cuentaEncontrada, int posicionEncontrada, long ms) {
        Map<String, Object> d = new LinkedHashMap<>();
        long inicio = largo(almacen.leer(B_MS), 0L);
        int desdePos = entero(almacen.leer(B_DESDE_POS), -1);
        d.put("desde_cuenta", t(almacen.leer(B_DESDE)));
        d.put("desde_posicion", desdePos);
        d.put("criterio", t(criterio));
        d.put("dato", t(dato));
        d.put("encontrada", encontrada);
        if (encontrada) {
            d.put("cuenta", t(cuentaEncontrada));
            d.put("posicion", posicionEncontrada);
            if (desdePos >= 0) d.put("salto", posicionEncontrada - desdePos);
        }
        d.put("segundos", inicio > 0 && ms >= inicio ? (ms - inicio) / 1000 : null);
        almacen.borrar(B_DESDE, B_DESDE_POS, B_MS);
        almacen.guardar(B_N, String.valueOf(entero(almacen.leer(B_N), 0) + 1));

        if (encontrada) {
            llegada(LLEGADA_BUSQUEDA, cuentaEncontrada, ms);
            almacen.guardar(L_BUSQ, t(uid));
            almacen.guardar(L_CRITERIO, t(criterio));
        }
        return d;
    }

    // ------------------------------------------------------------------ lecturas

    /** Medidor de la última cuenta leída (ENVIOGPRS "último medidor leído"); "" si no hay. */
    public String ultimoMedidor() {
        return t(almacen.leer(U_MEDIDOR));
    }

    /**
     * La cuenta quedó leída (o con causal). Devuelve los datos del evento "secuencia" y la deja como la última.
     * null si es la misma cuenta que la anterior (reproceso o reimpresión: no es un paso del recorrido).
     */
    public Map<String, Object> registrarLectura(Punto p, boolean conCausal) {
        String cuentaAnt = t(almacen.leer(U_CUENTA));
        if (!cuentaAnt.isEmpty() && cuentaAnt.equals(p.cuenta)) return null;

        int posAnt = entero(almacen.leer(U_POS), -1);
        long msAnt = largo(almacen.leer(U_MS), 0L);
        boolean mismaJornada = msAnt > 0 && dia(msAnt).equals(dia(p.ms)) && p.ms >= msAnt;

        Map<String, Object> d = new LinkedHashMap<>();
        d.put("posicion", p.posicion);
        d.put("ruta", p.ruta);
        d.put("medidor", p.medidor);
        d.put("con_causal", conCausal);
        d.put("cuenta_anterior", cuentaAnt);
        d.put("cod_cuenta_anterior", t(almacen.leer(U_COD)));
        d.put("posicion_anterior", posAnt);
        d.put("medidor_anterior", ultimoMedidor());
        d.put("misma_jornada", mismaJornada);
        if (mismaJornada) {
            d.put("salto", posAnt >= 0 ? p.posicion - posAnt : null);
            d.put("segundos_desde_anterior", (p.ms - msAnt) / 1000);
        }

        // La llegada vale solo si es la de esta cuenta (si no, el técnico volvió por otro camino no registrado).
        boolean llegadaPropia = p.cuenta.equals(t(almacen.leer(L_CUENTA)));
        d.put("llegada", llegadaPropia ? t(almacen.leer(L_TIPO)) : "");
        String busqueda = llegadaPropia ? t(almacen.leer(L_BUSQ)) : "";
        if (!busqueda.isEmpty()) {
            d.put("busqueda_uid", busqueda);
            d.put("busqueda_criterio", t(almacen.leer(L_CRITERIO)));
        }
        d.put("busquedas", entero(almacen.leer(B_N), 0));
        if (llegadaPropia) {
            long msLlegada = largo(almacen.leer(L_MS), 0L);
            if (msLlegada > 0 && p.ms >= msLlegada) d.put("segundos_en_cuenta", (p.ms - msLlegada) / 1000);
        }
        if (p.alt != null) d.put("altitud", p.alt);

        String contenedor = t(almacen.leer(C_ID));
        if (!contenedor.isEmpty()) {
            d.put("contenedor_id", contenedor);
            List<String> cuentas = cuentasContenedor();
            if (!cuentas.contains(p.codCuenta)) {
                cuentas.add(p.codCuenta);
                almacen.guardar(C_CUENTAS, String.join(",", cuentas));
            }
        }

        almacen.guardar(U_CUENTA, p.cuenta);
        almacen.guardar(U_COD, p.codCuenta);
        almacen.guardar(U_POS, String.valueOf(p.posicion));
        almacen.guardar(U_MEDIDOR, p.medidor);
        almacen.guardar(U_MS, String.valueOf(p.ms));
        almacen.borrar(B_N);
        return d;
    }

    // ------------------------------------------------------------------ contenedores

    public boolean contenedorActivo() {
        return !t(almacen.leer(C_ID)).isEmpty();
    }

    public String contenedorId() {
        return t(almacen.leer(C_ID));
    }

    /** cod_cuenta de lo leído desde que se inició el contenedor, en orden. */
    public List<String> cuentasContenedor() {
        String s = t(almacen.leer(C_CUENTAS));
        return s.isEmpty() ? new ArrayList<String>() : new ArrayList<>(Arrays.asList(s.split(",")));
    }

    public void iniciarContenedor(String id, long ms, Double lat, Double lng) {
        almacen.guardar(C_ID, t(id));
        almacen.guardar(C_MS, String.valueOf(ms));
        almacen.guardar(C_CUENTAS, "");
        if (lat != null && lng != null) {
            almacen.guardar(C_LAT, String.valueOf(lat));
            almacen.guardar(C_LNG, String.valueOf(lng));
        } else {
            almacen.borrar(C_LAT, C_LNG);
        }
    }

    /**
     * Cierra el contenedor en curso. Devuelve los datos del evento "contenedor", o null si no había uno.
     * El llamador decide qué hacer si tiene menos de {@link #MIN_CUENTAS_CONTENEDOR} cuentas.
     */
    public Map<String, Object> cerrarContenedor(long ms) {
        String id = contenedorId();
        if (id.isEmpty()) return null;
        long inicio = largo(almacen.leer(C_MS), 0L);
        List<String> cuentas = cuentasContenedor();

        Map<String, Object> d = new LinkedHashMap<>();
        d.put("contenedor_id", id);
        d.put("inicio", inicio);
        d.put("fin", ms);
        d.put("segundos", inicio > 0 && ms >= inicio ? (ms - inicio) / 1000 : null);
        d.put("cuentas", cuentas);
        d.put("n", cuentas.size());
        String lat = t(almacen.leer(C_LAT)), lng = t(almacen.leer(C_LNG));
        if (!lat.isEmpty() && !lng.isEmpty()) {
            d.put("lat_inicio", Double.valueOf(lat));
            d.put("lng_inicio", Double.valueOf(lng));
        }
        almacen.borrar(C_ID, C_MS, C_CUENTAS, C_LAT, C_LNG);
        return d;
    }

    // ------------------------------------------------------------------ apoyo

    private static String dia(long ms) {
        return new SimpleDateFormat("yyyyMMdd", Locale.ROOT).format(new Date(ms));
    }

    private static String t(String s) {
        return s == null ? "" : s.trim();
    }

    private static int entero(String s, int defecto) {
        try {
            return s == null || s.trim().isEmpty() ? defecto : Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return defecto;
        }
    }

    private static long largo(String s, long defecto) {
        try {
            return s == null || s.trim().isEmpty() ? defecto : Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            return defecto;
        }
    }
}
