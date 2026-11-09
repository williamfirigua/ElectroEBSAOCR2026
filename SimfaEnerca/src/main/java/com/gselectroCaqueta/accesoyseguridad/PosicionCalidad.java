package com.gselectroCaqueta.accesoyseguridad;

import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.SystemClock;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Posicion de la pantalla de lectura (proyecto GPS, fases 0 y 1).
 *
 * <p>Recibe los fixes del GPS satelital y de la ubicacion fusionada (red / wifi) y decide cual se muestra y se
 * guarda, sin esperar a un fix mejor: la lectura usa la posicion de ese momento.</p>
 * <ul>
 *   <li>Se elige la de menor error efectivo = precision + 1,5 m/s x antiguedad.</li>
 *   <li>La de red solo entra si el GPS lleva mas de 30 s callado o si su precision es de 100 m o menos.</li>
 *   <li>Cambiar de fuente (GPS a red o al reves) exige que la nueva sea al menos 20 % mejor (histeresis).</li>
 *   <li>Sin nada fresco, sirve la ultima conocida de hasta 10 minutos (fuente ULTIMA_CONOCIDA).</li>
 * </ul>
 * <p>Ademas arma la calidad de la posicion que viaja en la liquidacion (columnas gps_* del servidor): precision,
 * altitud, satelites usados, antiguedad, fuente, ubicacion simulada y el promedio de los fixes frente al medidor.</p>
 *
 * <p>Se usa desde el hilo principal (callbacks de ubicacion y guardado de la lectura).</p>
 */
final class PosicionCalidad {

    static final String FUENTE_GPS = "GPS";
    static final String FUENTE_RED = "RED";
    static final String FUENTE_PDR = "PDR";
    static final String FUENTE_ULTIMA = "ULTIMA_CONOCIDA";
    static final String FUENTE_SIN_POSICION = "SIN_POSICION";
    static final String FUENTE_CIERRE_RUTA = "CIERRE_RUTA";
    static final String FUENTE_REGISTRO_PREVIO = "REGISTRO_PREVIO";

    /** El GPS se considera callado si no entrega fix en este tiempo. */
    static final long GPS_CALLADO_MS = 30_000L;
    /** Una posicion de mas de esto ya no es "de ahora": cuenta como ultima conocida. */
    static final long FRESCA_MS = 2L * 60_000L;
    /** Ultima conocida aceptable para no dejar la cuenta sin coordenada. */
    static final long ULTIMA_CONOCIDA_MS = 10L * 60_000L;
    /** Precision maxima para aceptar la ubicacion de red mientras el GPS sigue hablando. */
    static final float RED_PRECISION_MAX = 100f;
    /** Error que suma cada segundo de antiguedad (el tecnico camina). */
    static final double DEGRADACION_MPS = 1.5;
    /** Para cambiar de fuente la nueva debe tener menos del 80 % del error de la actual. */
    static final double HISTERESIS = 0.8;
    /** Precision supuesta cuando el fix no trae precision. */
    static final float PRECISION_DESCONOCIDA = 50f;

    // Promedio frente al medidor (fase 1)
    static final long VENTANA_PROMEDIO_MS = 60_000L;
    static final float VELOCIDAD_QUIETO_MPS = 0.8f;
    static final float RADIO_PROMEDIO_M = 10f;
    static final float PRECISION_PROMEDIO_MAX = 30f;

    private Location ultimoGps;
    private Location ultimaRed;
    private Location elegida;
    private String fuenteElegida;
    private final ArrayDeque<Location> fixesGps = new ArrayDeque<>();
    /** Satelites usados en el ultimo fix (GnssStatus); -1 = desconocido. */
    private volatile int satelitesUsados = -1;
    /** Navegacion por pasos (fase 2); null si el equipo no tiene los sensores o no se activo. */
    private PdrMotor pdr;

    void usarPdr(PdrMotor motor) {
        pdr = motor;
    }

    static long ahora() {
        return SystemClock.elapsedRealtime();
    }

    static long edadMs(Location l, long ahora) {
        long t = l.getElapsedRealtimeNanos() / 1_000_000L;
        if (t <= 0) return Long.MAX_VALUE / 4; // sin hora: se trata como muy vieja
        return Math.max(0L, ahora - t);
    }

    static double errorEfectivo(Location l, long ahora) {
        float acc = l.hasAccuracy() ? l.getAccuracy() : PRECISION_DESCONOCIDA;
        return acc + DEGRADACION_MPS * (edadMs(l, ahora) / 1000.0);
    }

    static boolean esGps(Location l) {
        return l != null && LocationManager.GPS_PROVIDER.equals(l.getProvider());
    }

    static boolean esPdr(Location l) {
        return l != null && "pdr".equals(l.getProvider());
    }

    /** 0 GPS, 1 red, 2 pasos: para la histeresis entre fuentes. */
    private static int tipo(Location l) {
        return esGps(l) ? 0 : esPdr(l) ? 2 : 1;
    }

    static boolean valida(Location l) {
        if (l == null) return false;
        double la = l.getLatitude(), lo = l.getLongitude();
        if (!finito(la) || !finito(lo)) return false;
        if (la == 0 && lo == 0) return false;
        return Math.abs(la) <= 90 && Math.abs(lo) <= 180;
    }

    void satelitesUsados(int n) {
        satelitesUsados = n;
    }

    /** Un fix nuevo de cualquier fuente. */
    void alimentar(Location l) {
        if (!valida(l)) return;
        Location copia = new Location(l);
        if (esGps(copia)) {
            if (pdr != null) pdr.anclar(copia);
            ultimoGps = copia;
            fixesGps.addLast(copia);
            podar(ahora());
        } else {
            ultimaRed = copia;
        }
    }

    private void podar(long ahora) {
        Iterator<Location> it = fixesGps.iterator();
        while (it.hasNext()) {
            if (edadMs(it.next(), ahora) > VENTANA_PROMEDIO_MS) it.remove();
        }
        while (fixesGps.size() > 120) fixesGps.removeFirst();
    }

    /**
     * La posicion que se debe mostrar y guardar ahora, o null si no hay ninguna de los ultimos 10 minutos.
     */
    Location elegir() {
        long ahora = ahora();
        Location gps = ultimoGps != null && edadMs(ultimoGps, ahora) <= ULTIMA_CONOCIDA_MS ? ultimoGps : null;
        Location red = ultimaRed != null && edadMs(ultimaRed, ahora) <= ULTIMA_CONOCIDA_MS ? ultimaRed : null;
        boolean gpsCallado = gps == null || edadMs(gps, ahora) > GPS_CALLADO_MS;
        if (red != null && !gpsCallado) {
            float acc = red.hasAccuracy() ? red.getAccuracy() : PRECISION_DESCONOCIDA;
            if (acc > RED_PRECISION_MAX) red = null; // con GPS hablando, una red gruesa no compite
        }
        // Pasos: solo cuando el GPS lleva mas de 30 s callado.
        Location pasos = gpsCallado && pdr != null ? pdr.estimacion() : null;

        Location[] candidatas = {gps, red, pasos};
        Location mejor = null;
        double errMejor = Double.MAX_VALUE;
        for (Location l : candidatas) {
            if (l == null) continue;
            double e = errorEfectivo(l, ahora);
            if (e < errMejor) {
                errMejor = e;
                mejor = l;
            }
        }
        // Histeresis: si la fuente que estaba en pantalla sigue disponible, solo se cambia si la nueva es 20 % mejor.
        if (mejor != null && elegida != null && tipo(mejor) != tipo(elegida)) {
            Location misma = candidatas[tipo(elegida)];
            if (misma != null && errMejor >= HISTERESIS * errorEfectivo(misma, ahora)) mejor = misma;
        }
        elegida = mejor;
        if (mejor == null) {
            fuenteElegida = null;
        } else if (esPdr(mejor)) {
            fuenteElegida = FUENTE_PDR;
        } else if (edadMs(mejor, ahora) > FRESCA_MS) {
            fuenteElegida = FUENTE_ULTIMA;
        } else {
            fuenteElegida = esGps(mejor) ? FUENTE_GPS : FUENTE_RED;
        }
        return mejor;
    }

    String fuente() {
        return fuenteElegida;
    }

    /** Precision de la posicion elegida (m); NaN si no hay. */
    float precisionElegida() {
        return elegida != null && elegida.hasAccuracy() ? elegida.getAccuracy() : Float.NaN;
    }

    /**
     * Calidad de la posicion para la liquidacion (objeto gps del JSON). Solo valores finitos; lo que no se sabe
     * no se manda (el servidor deja NULL).
     */
    Map<String, Object> calidad() {
        Map<String, Object> m = new LinkedHashMap<>();
        long ahora = ahora();
        Location l = elegir();
        if (l == null) {
            m.put("fuente", FUENTE_SIN_POSICION);
            return m;
        }
        m.put("fuente", fuenteElegida);
        if (l.hasAccuracy()) poner(m, "precision_h", redondear(l.getAccuracy(), 2));
        if (Build.VERSION.SDK_INT >= 26 && l.hasVerticalAccuracy()) {
            poner(m, "precision_v", redondear(l.getVerticalAccuracyMeters(), 2));
        }
        if (l.hasAltitude()) poner(m, "altitud_m", redondear(l.getAltitude(), 2));
        if (esGps(l) && satelitesUsados >= 0) m.put("satelites_usados", satelitesUsados);
        m.put("edad_fix_seg", edadMs(l, ahora) / 1000L);
        m.put("simulada", simulada(l));
        agregarPromedio(m, ahora);
        return m;
    }

    /** Solo la fuente (cierre de ruta, registro previo): la coordenada no es la medida frente al predio. */
    static Map<String, Object> soloFuente(String fuente) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("fuente", fuente);
        return m;
    }

    @SuppressWarnings("deprecation")
    private static boolean simulada(Location l) {
        if (Build.VERSION.SDK_INT >= 31) return l.isMock();
        return l.isFromMockProvider();
    }

    /**
     * Promedio ponderado (1/precision^2) de los fixes GPS de los ultimos 60 s con el tecnico quieto, a menos de
     * 10 m del ultimo y con precision de 30 m o menos. Su precision es la mayor entre el error estadistico, la
     * mitad de la mejor precision y la dispersion: honesta, no optimista.
     */
    private void agregarPromedio(Map<String, Object> m, long ahora) {
        if (ultimoGps == null || edadMs(ultimoGps, ahora) > GPS_CALLADO_MS) return;
        podar(ahora);
        double sw = 0, sLat = 0, sLon = 0;
        float mejor = Float.MAX_VALUE;
        java.util.List<Location> usados = new java.util.ArrayList<>();
        for (Location f : fixesGps) {
            if (!f.hasAccuracy() || f.getAccuracy() <= 0 || f.getAccuracy() > PRECISION_PROMEDIO_MAX) continue;
            if (f.hasSpeed() && f.getSpeed() >= VELOCIDAD_QUIETO_MPS) continue;
            if (f.distanceTo(ultimoGps) > RADIO_PROMEDIO_M) continue;
            double w = 1.0 / (f.getAccuracy() * f.getAccuracy());
            sw += w;
            sLat += w * f.getLatitude();
            sLon += w * f.getLongitude();
            mejor = Math.min(mejor, f.getAccuracy());
            usados.add(f);
        }
        if (usados.size() < 2 || sw <= 0) return;
        double lat = sLat / sw, lon = sLon / sw;
        Location centro = new Location(LocationManager.GPS_PROVIDER);
        centro.setLatitude(lat);
        centro.setLongitude(lon);
        double sd2 = 0;
        for (Location f : usados) {
            double d = f.distanceTo(centro);
            sd2 += (1.0 / (f.getAccuracy() * f.getAccuracy())) * d * d;
        }
        double dispersion = Math.sqrt(sd2 / sw);
        double estadistico = Math.sqrt(1.0 / sw);
        double precision = Math.max(estadistico, Math.max(mejor / 2.0, dispersion));
        poner(m, "lat_promedio", redondear(lat, 7));
        poner(m, "lng_promedio", redondear(lon, 7));
        poner(m, "precision_promedio", redondear(precision, 2));
        m.put("n_fixes", usados.size());
    }

    private static void poner(Map<String, Object> m, String clave, double v) {
        if (finito(v)) m.put(clave, v);
    }

    private static boolean finito(double v) {
        return !Double.isNaN(v) && !Double.isInfinite(v);
    }

    private static double redondear(double v, int decimales) {
        if (!finito(v)) return v;
        double f = Math.pow(10, decimales);
        return Math.round(v * f) / f;
    }
}
