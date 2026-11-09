package com.gselectroCaqueta.accesoyseguridad;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Condiciones del equipo para tomar una lectura o una causal (EBSA). Si alguna falla, la toma se bloquea.
 * <ol>
 *   <li>Modo avion apagado.</li>
 *   <li>Ubicacion encendida (interruptor general y proveedor GPS, que es el que escucha la app). Con la
 *       ubicacion encendida pero sin señal la toma sigue: eso lo decide la validacion de distancia.</li>
 *   <li>Fecha y hora automaticas encendidas. Solo cuenta la fecha automatica: la zona horaria automatica
 *       no impide cambiar la fecha a mano.</li>
 *   <li>Reloj del equipo contra la hora del GPS, cuando hay posicion. Es la prueba que no se evita con los
 *       ajustes: la hora del satelite no depende del equipo.</li>
 *   <li>Fecha del equipo no anterior a la fecha de proceso del ciclo (GENERAL, dd/MM/yyyy).</li>
 * </ol>
 * Sin estado: cada llamada lee los ajustes del momento.
 */
public final class ControlEntornoTerminal {

    /** Diferencia tolerada entre el reloj del equipo y la hora del GPS. */
    static final long DESFASE_MAXIMO_MS = 10L * 60L * 1000L;
    /**
     * Por encima de esto la diferencia no es manipulacion sino un GPS con la falla del contador de semanas
     * (equipos viejos reportan fechas de hace ~19,6 años). En ese caso no se usa la hora del GPS.
     */
    static final long DESFASE_NO_CONFIABLE_MS = 365L * 24L * 60L * 60L * 1000L;

    /** Motivo del bloqueo: titulo, mensaje para el tecnico y pantalla de ajustes que lo corrige. */
    public static final class Bloqueo {
        public final String codigo;
        public final String titulo;
        public final String mensaje;
        public final String accionAjustes;

        Bloqueo(String codigo, String titulo, String mensaje, String accionAjustes) {
            this.codigo = codigo;
            this.titulo = titulo;
            this.mensaje = mensaje;
            this.accionAjustes = accionAjustes;
        }
    }

    private ControlEntornoTerminal() {
    }

    /**
     * @param ultimaPosicionGps ultima posicion recibida del proveedor GPS en esta sesion, o null si no hay.
     * @param fechaProceso      fecha de proceso del ciclo (dd/MM/yyyy); vacia o invalida no se valida.
     * @return null si se puede tomar la lectura; si no, el motivo.
     */
    public static Bloqueo evaluar(Context context, Location ultimaPosicionGps, String fechaProceso) {
        return evaluar(context, ultimaPosicionGps, fechaProceso, false);
    }

    /**
     * @param ubicacionPorPasos true si hay una estimacion por pasos vigente (PDR): con la ubicacion apagada la
     *                          lectura se guarda con esa estimacion en vez de bloquearse.
     */
    public static Bloqueo evaluar(Context context, Location ultimaPosicionGps, String fechaProceso, boolean ubicacionPorPasos) {
        if (modoAvionActivo(context)) {
            return new Bloqueo("MODO_AVION", "Modo avión activo",
                    "Desactive el modo avión para seguir con la toma de lecturas.",
                    Settings.ACTION_AIRPLANE_MODE_SETTINGS);
        }
        if (!ubicacionActiva(context) && !ubicacionPorPasos) {
            return new Bloqueo("SIN_UBICACION", "Ubicación desactivada",
                    "Active la ubicación (GPS) del equipo para seguir con la toma de lecturas.",
                    Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        }
        if (!fechaAutomaticaActiva(context)) {
            return new Bloqueo("FECHA_MANUAL", "Fecha y hora manuales",
                    "Active «Fecha y hora automáticas» en los ajustes del equipo. "
                            + "No se pueden tomar lecturas con el reloj puesto a mano.",
                    Settings.ACTION_DATE_SETTINGS);
        }
        long desfase = desfaseContraGps(ultimaPosicionGps);
        if (Math.abs(desfase) > DESFASE_MAXIMO_MS && Math.abs(desfase) < DESFASE_NO_CONFIABLE_MS) {
            return new Bloqueo("RELOJ_ALTERADO", "Reloj del equipo alterado",
                    "La hora del equipo difiere " + describirMinutos(desfase) + " de la hora del GPS. "
                            + "Las facturas deben salir con la hora real: active «Fecha y hora automáticas» y espere a que el equipo se sincronice.",
                    Settings.ACTION_DATE_SETTINGS);
        }
        if (fechaAnteriorAlProceso(fechaProceso)) {
            return new Bloqueo("FECHA_ATRASADA", "Fecha del equipo atrasada",
                    "La fecha del equipo es anterior a la fecha de proceso del ciclo (" + fechaProceso.trim() + "). "
                            + "Active «Fecha y hora automáticas».",
                    Settings.ACTION_DATE_SETTINGS);
        }
        return null;
    }

    static boolean modoAvionActivo(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

    static boolean ubicacionActiva(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (lm == null) {
            return false;
        }
        try {
            boolean general = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
                    ? lm.isLocationEnabled()
                    : lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            return general && lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** AUTO_TIME, o la variante "hora del GPS" de algunos fabricantes. La zona horaria no cuenta. */
    static boolean fechaAutomaticaActiva(Context context) {
        try {
            if (Settings.Global.getInt(context.getContentResolver(), Settings.Global.AUTO_TIME, 0) == 1) {
                return true;
            }
            return Settings.Global.getInt(context.getContentResolver(), "auto_time_gps", 0) == 1;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * Reloj del equipo menos la hora del GPS, en ms; 0 si no hay posicion GPS. La hora del fix se lleva al
     * momento actual con elapsedRealtime (reloj monotono que no cambia al tocar la fecha), asi que sirve
     * aunque el fix sea de hace un rato.
     */
    static long desfaseContraGps(Location posicion) {
        if (posicion == null || posicion.getTime() <= 0 || posicion.getElapsedRealtimeNanos() <= 0
                || !LocationManager.GPS_PROVIDER.equals(posicion.getProvider())) {
            return 0;
        }
        long edadMs = (SystemClock.elapsedRealtimeNanos() - posicion.getElapsedRealtimeNanos()) / 1_000_000L;
        if (edadMs < 0) {
            return 0;
        }
        long horaGpsAhora = posicion.getTime() + edadMs;
        return System.currentTimeMillis() - horaGpsAhora;
    }

    static boolean fechaAnteriorAlProceso(String fechaProceso) {
        if (fechaProceso == null || fechaProceso.trim().isEmpty()) {
            return false;
        }
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", Locale.ROOT);
        formato.setLenient(false);
        Date proceso;
        try {
            proceso = formato.parse(fechaProceso.trim());
        } catch (ParseException e) {
            return false;
        }
        if (proceso == null) {
            return false;
        }
        Calendar hoy = Calendar.getInstance();
        hoy.set(Calendar.HOUR_OF_DAY, 0);
        hoy.set(Calendar.MINUTE, 0);
        hoy.set(Calendar.SECOND, 0);
        hoy.set(Calendar.MILLISECOND, 0);
        return hoy.getTime().before(proceso);
    }

    private static String describirMinutos(long desfaseMs) {
        long minutos = Math.abs(desfaseMs) / 60_000L;
        String sentido = desfaseMs > 0 ? "adelantada" : "atrasada";
        if (minutos >= 1440) {
            return (minutos / 1440) + " día(s) (" + sentido + ")";
        }
        if (minutos >= 60) {
            return (minutos / 60) + " hora(s) (" + sentido + ")";
        }
        return minutos + " minutos (" + sentido + ")";
    }
}
