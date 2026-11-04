package com.gselectroCaqueta.accesoyseguridad;

import android.content.Context;

/**
 * Foto que el sistema exigió y todavía no se ha tomado (EBSA no factura sin registro fotográfico).
 * <p>
 * Mientras haya una pendiente, la terminal no cambia de cliente por ningún camino (siguiente, anterior,
 * búsqueda, lista, mapa, último leído) ni se sale de la liquidación. Solo se libera de dos formas:
 * <ol>
 *   <li>la cámara devuelve una foto con contenido ({@link #cumplir()});</li>
 *   <li>un supervisor la autoriza con su código y clave (queda como evento y en RESUMENTIEMPO).</li>
 * </ol>
 * El estado se guarda en disco con commit(): si la app se cierra o el equipo se reinicia con la foto
 * pendiente, al volver a la liquidación se regresa a esa cuenta y se vuelve a pedir.
 * <p>
 * Si el flujo de la lectura intenta pasar al siguiente cliente mientras la cámara está abierta (la
 * impresión y el avance siguen en segundo plano), el avance se guarda como diferido y se ejecuta
 * cuando la foto se cumple o se autoriza.
 * <p>
 * La misma compuerta sirve para la muestra de control de la factura impresa (foto de la factura o código
 * de barras): otra instancia con su propio almacén ({@link #de(Context, String)}) y el dato esperado en
 * {@link Pendiente#extra}.
 */
public final class FotoObligatoria {

    /** Lo pendiente; null = nada. */
    public static final class Pendiente {
        public final String cuenta;
        /** cod_cuenta del servidor (cuenta + dígito), para el evento. */
        public final String codCuenta;
        public final String anno;
        public final String mes;
        /** Nombre base con que se pidió la foto (cuenta_mes): se reutiliza al reintentar. */
        public final String nombre;
        /** Registro de la cuenta en la tabla de entrada (VariablesGlobales.registroactual). */
        public final int registro;
        public final String motivo;
        public final long desdeMs;
        /** Dato propio de quien exige (la muestra guarda "tipo|codigo esperado"); "" si no hay. */
        public final String extra;

        Pendiente(String cuenta, String codCuenta, String anno, String mes, String nombre, int registro, String motivo, long desdeMs,
                  String extra) {
            this.cuenta = cuenta;
            this.codCuenta = codCuenta;
            this.anno = anno;
            this.mes = mes;
            this.nombre = nombre;
            this.registro = registro;
            this.motivo = motivo;
            this.desdeMs = desdeMs;
            this.extra = extra;
        }
    }

    public static final int AVANCE_NINGUNO = 0;
    public static final int AVANCE_ADELANTE = 1;
    public static final int AVANCE_ATRAS = 2;

    private static final String K_CUENTA = "cuenta";
    private static final String K_COD_CUENTA = "cod_cuenta";
    private static final String K_ANNO = "anno";
    private static final String K_MES = "mes";
    private static final String K_NOMBRE = "nombre";
    private static final String K_REGISTRO = "registro";
    private static final String K_MOTIVO = "motivo";
    private static final String K_DESDE = "desde";
    private static final String K_AVANCE = "avance";
    private static final String K_EXTRA = "extra";

    private final AlmacenEstado almacen;

    public FotoObligatoria(AlmacenEstado almacen) {
        this.almacen = almacen;
    }

    public static FotoObligatoria de(Context ctx) {
        return de(ctx, "foto_obligatoria");
    }

    /** Compuerta con su propio almacén (otra exigencia que no debe pisar a la foto del medidor). */
    public static FotoObligatoria de(Context ctx, String nombreAlmacen) {
        return new FotoObligatoria(AlmacenEstado.de(ctx, nombreAlmacen));
    }

    /** Registra la foto exigida. Si ya había una pendiente de la misma cuenta, conserva su hora de inicio. */
    public void exigir(String cuenta, String codCuenta, String anno, String mes, String nombre, int registro,
                       String motivo, long ahoraMs) {
        exigir(cuenta, codCuenta, anno, mes, nombre, registro, motivo, ahoraMs, "");
    }

    public void exigir(String cuenta, String codCuenta, String anno, String mes, String nombre, int registro,
                       String motivo, long ahoraMs, String extra) {
        Pendiente previa = pendiente();
        long desde = previa != null && previa.cuenta.equals(t(cuenta)) ? previa.desdeMs : ahoraMs;
        almacen.guardar(K_CUENTA, t(cuenta));
        almacen.guardar(K_COD_CUENTA, t(codCuenta));
        almacen.guardar(K_ANNO, t(anno));
        almacen.guardar(K_MES, t(mes));
        almacen.guardar(K_NOMBRE, t(nombre));
        almacen.guardar(K_REGISTRO, String.valueOf(registro));
        almacen.guardar(K_MOTIVO, t(motivo));
        almacen.guardar(K_DESDE, String.valueOf(desde));
        almacen.guardar(K_EXTRA, t(extra));
    }

    public Pendiente pendiente() {
        String cuenta = almacen.leer(K_CUENTA);
        if (cuenta == null || cuenta.isEmpty()) return null;
        return new Pendiente(cuenta, t(almacen.leer(K_COD_CUENTA)), t(almacen.leer(K_ANNO)), t(almacen.leer(K_MES)), t(almacen.leer(K_NOMBRE)),
                entero(almacen.leer(K_REGISTRO), -1), t(almacen.leer(K_MOTIVO)), largo(almacen.leer(K_DESDE)), t(almacen.leer(K_EXTRA)));
    }

    public boolean hayPendiente() {
        return pendiente() != null;
    }

    /** La foto se tomó (o el supervisor autorizó): libera la navegación. Devuelve el avance diferido. */
    public int cumplir() {
        int avance = avanceDiferido();
        almacen.borrar(K_CUENTA, K_COD_CUENTA, K_ANNO, K_MES, K_NOMBRE, K_REGISTRO, K_MOTIVO, K_DESDE, K_AVANCE, K_EXTRA);
        return avance;
    }

    /**
     * El flujo quiso pasar de cliente con la foto pendiente: se hará al cumplirla. Se conserva el primero:
     * el flujo prueba "adelante y si no, atrás", y el segundo intento no debe pisar al primero.
     */
    public void diferirAvance(int direccion) {
        if (avanceDiferido() != AVANCE_NINGUNO) return;
        if (direccion == AVANCE_ADELANTE || direccion == AVANCE_ATRAS) {
            almacen.guardar(K_AVANCE, String.valueOf(direccion));
        }
    }

    public int avanceDiferido() {
        return entero(almacen.leer(K_AVANCE), AVANCE_NINGUNO);
    }

    private static String t(String s) {
        return s == null ? "" : s.trim();
    }

    private static int entero(String s, int defecto) {
        try {
            return s == null ? defecto : Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return defecto;
        }
    }

    private static long largo(String s) {
        try {
            return s == null ? 0L : Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
