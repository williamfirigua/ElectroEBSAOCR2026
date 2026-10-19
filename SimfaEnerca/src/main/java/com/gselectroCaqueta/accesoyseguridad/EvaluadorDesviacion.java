package com.gselectroCaqueta.accesoyseguridad;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Evaluación de desviaciones significativas (Res. CREG 105 007 + analítica EBSA) para el medidor 1 (activa).
 *
 * El consumo se evalúa normalizado al período de facturación: consumo * (30 * periodicidad) / días transcurridos,
 * donde los días son periodos completos de 24 horas entre la lectura anterior y la actual y la periodicidad son
 * los meses del ciclo (1 mensual, 3 trimestral). Las bandas de lm_suscriptor_dsvcion vienen en esa misma escala
 * (en rutas trimestrales equivalen a 3 meses). El consumo que se factura cuando la decisión es REAL sigue siendo
 * el consumo medido, sin normalizar.
 *
 * Sin dependencias de Android para poder probarla con JUnit.
 */
public final class EvaluadorDesviacion {

    public static final int DIAS_BASE = 30;
    /** Días por mes de periodicidad por encima de los cuales la fecha anterior se considera no confiable (p. ej. fecha por defecto). */
    public static final int DIAS_MAXIMOS_POR_MES = 100;
    private static final long MS_DIA = 24L * 60 * 60 * 1000;
    private static final String[] FORMATOS_FECHA = {"dd/MM/yyyy HH:mm:ss", "dd/MM/yyyy HH:mm", "dd/MM/yyyy"};
    /** Hora que se asume cuando la fecha de la lectura anterior no la trae. */
    private static final long MEDIODIA_MS = 12L * 60 * 60 * 1000;

    public enum Decision {
        /** Cuenta estacionaria o nueva: se liquida por diferencia (paso 2/3 → 8). */
        REAL_ESTACIONARIA_O_NUEVA,
        /** Dentro de la banda de la resolución (paso 4 → 8). */
        REAL_BANDA_RESOLUCION,
        /** Por debajo de la banda de la resolución: no se cobra promedio. */
        REAL_BAJO_BANDA,
        /** Fuera de la resolución pero no supera el consumo de subsistencia (5a → 9). */
        REAL_SUBSISTENCIA,
        /** Cambio de clase, nivel de tensión o normalización (5b → 9). */
        REAL_CAMBIOS,
        /** Dentro de la banda de tipo de uso, ciclo y clase (6a → 9). */
        REAL_BANDA_TIPO_USO,
        /** Dentro del mínimo y máximo de los últimos 12 meses (6b → 9). */
        REAL_BANDA_ANUAL,
        /** Fuera de todas las bandas: promedio, observación 18 y remisión a inspección (paso 7). */
        PROMEDIO;

        public boolean cobraPromedio() {
            return this == PROMEDIO;
        }
    }

    /** Valores de lm_suscriptor_dsvcion para la cuenta. Un límite <= 0 en subsistencia o tipo de uso se toma como no definido. */
    public static final class Bandas {
        final String lqdaCsmo;
        final double liRes, lsRes, lsSubs, liAnual, lsAnual, liTpoUs, lsTpoUs;
        final boolean cambioClase, cambioNivel, normalizacion;

        public Bandas(String lqdaCsmo, double liRes, double lsRes, double lsSubs, double liAnual, double lsAnual,
                      double liTpoUs, double lsTpoUs, boolean cambioClase, boolean cambioNivel, boolean normalizacion) {
            this.lqdaCsmo = lqdaCsmo == null ? "" : lqdaCsmo.trim().toUpperCase(Locale.ROOT);
            this.liRes = liRes;
            this.lsRes = lsRes;
            this.lsSubs = lsSubs;
            this.liAnual = liAnual;
            this.lsAnual = lsAnual;
            this.liTpoUs = liTpoUs;
            this.lsTpoUs = lsTpoUs;
            this.cambioClase = cambioClase;
            this.cambioNivel = cambioNivel;
            this.normalizacion = normalizacion;
        }
    }

    public static final class Resultado {
        public final Decision decision;
        public final double consumoNormalizado;
        public final int dias;

        Resultado(Decision decision, double consumoNormalizado, int dias) {
            this.decision = decision;
            this.consumoNormalizado = consumoNormalizado;
            this.dias = dias;
        }
    }

    private EvaluadorDesviacion() {
    }

    /**
     * Periodos completos de 24 h entre la lectura anterior y la actual. Si la fecha anterior no trae hora se asumen
     * las 12:00:00. Devuelve los días base del período (30 * periodicidad, equivale a no normalizar) si la fecha
     * no es válida, si el resultado no es positivo o si supera {@link #DIAS_MAXIMOS_POR_MES} por mes del período.
     */
    public static int diasTranscurridos(String fechaLecturaAnterior, Date fechaLecturaActual, int periodicidadMeses) {
        int meses = periodicidadValida(periodicidadMeses);
        int diasBase = DIAS_BASE * meses;
        if (fechaLecturaAnterior == null || fechaLecturaActual == null) {
            return diasBase;
        }
        String texto = fechaLecturaAnterior.trim();
        for (String formato : FORMATOS_FECHA) {
            SimpleDateFormat sdf = new SimpleDateFormat(formato, Locale.ROOT);
            sdf.setLenient(false);
            try {
                Date anterior = sdf.parse(texto);
                long inicio = formato.length() == 10 ? anterior.getTime() + MEDIODIA_MS : anterior.getTime();
                long dias = (fechaLecturaActual.getTime() - inicio) / MS_DIA;
                return dias > 0 && dias <= (long) DIAS_MAXIMOS_POR_MES * meses ? (int) dias : diasBase;
            } catch (ParseException ignored) {
                // siguiente formato
            }
        }
        return diasBase;
    }

    /** Ruta mensual. */
    public static int diasTranscurridos(String fechaLecturaAnterior, Date fechaLecturaActual) {
        return diasTranscurridos(fechaLecturaAnterior, fechaLecturaActual, 1);
    }

    private static int periodicidadValida(int periodicidadMeses) {
        return periodicidadMeses > 0 ? periodicidadMeses : 1;
    }

    public static double normalizar(double consumo, int dias, int periodicidadMeses) {
        return dias > 0 ? consumo * DIAS_BASE * periodicidadValida(periodicidadMeses) / dias : consumo;
    }

    public static Resultado evaluar(double consumoMedido, int dias, int periodicidadMeses, Bandas b) {
        double c = normalizar(consumoMedido, dias, periodicidadMeses);
        return new Resultado(decidir(c, b), c, dias);
    }

    /** Ruta mensual. */
    public static Resultado evaluar(double consumoMedido, int dias, Bandas b) {
        return evaluar(consumoMedido, dias, 1, b);
    }

    static Decision decidir(double c, Bandas b) {
        if ("E".equals(b.lqdaCsmo) || "N".equals(b.lqdaCsmo)) {
            return Decision.REAL_ESTACIONARIA_O_NUEVA;
        }
        if (c >= b.liRes && c <= b.lsRes) {
            return Decision.REAL_BANDA_RESOLUCION;
        }
        if (c < b.liRes) {
            return Decision.REAL_BAJO_BANDA;
        }
        if (b.lsSubs <= 0 || c <= b.lsSubs) {
            return Decision.REAL_SUBSISTENCIA;
        }
        if (b.cambioClase || b.cambioNivel || b.normalizacion) {
            return Decision.REAL_CAMBIOS;
        }
        if (b.lsTpoUs <= 0 || (c >= b.liTpoUs && c <= b.lsTpoUs)) {
            return Decision.REAL_BANDA_TIPO_USO;
        }
        if (c >= b.liAnual && c <= b.lsAnual) {
            return Decision.REAL_BANDA_ANUAL;
        }
        return Decision.PROMEDIO;
    }
}
