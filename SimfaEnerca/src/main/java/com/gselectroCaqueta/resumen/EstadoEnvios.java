package com.gselectroCaqueta.resumen;

import android.content.Context;

import com.gselectroCaqueta.sync.ColaEnvio;
import com.gselectroCaqueta.sync.ColaEnvioSqlite;
import com.gselectroCaqueta.sync.ConfigSync;

/**
 * Foto de la cola de envío (modo REST) para el menú principal y el resumen. En modo SOAP la cola no se usa y
 * solo se informa el modo. Son conteos sobre el índice (tipo, estado, ...): se puede llamar en cualquier
 * hilo, pero las pantallas lo hacen en segundo plano junto con el resumen.
 */
public final class EstadoEnvios {

    public final boolean modoRest;
    public final int liquidacionesPendientes, liquidacionesRechazadas, liquidacionesEnviadas;
    public final int fotosPendientes, fotosRechazadas;
    public final int lblPendientes, lblRechazados;
    public final int trackingPendientes;

    private EstadoEnvios(boolean modoRest, ColaEnvio c) {
        this.modoRest = modoRest;
        liquidacionesPendientes = c == null ? 0 : c.contar(ColaEnvio.LIQUIDACION, ColaEnvio.PENDIENTE);
        liquidacionesRechazadas = c == null ? 0 : c.contar(ColaEnvio.LIQUIDACION, ColaEnvio.ERROR);
        liquidacionesEnviadas = c == null ? 0 : c.contar(ColaEnvio.LIQUIDACION, ColaEnvio.ENVIADO);
        fotosPendientes = c == null ? 0 : c.contar(ColaEnvio.FOTO, ColaEnvio.PENDIENTE);
        fotosRechazadas = c == null ? 0 : c.contar(ColaEnvio.FOTO, ColaEnvio.ERROR);
        lblPendientes = c == null ? 0 : c.contar(ColaEnvio.LBL, ColaEnvio.PENDIENTE);
        lblRechazados = c == null ? 0 : c.contar(ColaEnvio.LBL, ColaEnvio.ERROR);
        trackingPendientes = c == null ? 0 : c.contar(ColaEnvio.TRACKING, ColaEnvio.PENDIENTE);
    }

    public static EstadoEnvios leer(Context ctx) {
        boolean rest = ConfigSync.modoRest(ctx);
        return new EstadoEnvios(rest, rest ? ColaEnvioSqlite.de(ctx) : null);
    }

    /** Lo que impide borrar la ruta: lecturas, fotos o LBL sin enviar o rechazados (el tracking no cuenta). */
    public int pendientesDeRuta() {
        return liquidacionesPendientes + liquidacionesRechazadas + fotosPendientes + fotosRechazadas
                + lblPendientes + lblRechazados;
    }

    public int rechazados() {
        return liquidacionesRechazadas + fotosRechazadas + lblRechazados;
    }

    /** Una línea para la tarjeta de envíos. */
    public String resumenCorto() {
        if (!modoRest) return "Modo SOAP: los envíos se hacen desde la liquidación";
        if (pendientesDeRuta() == 0) return "Todo enviado (" + liquidacionesEnviadas + " lecturas)";
        StringBuilder b = new StringBuilder();
        agregar(b, liquidacionesPendientes, "lecturas por enviar");
        agregar(b, fotosPendientes, "fotos");
        agregar(b, lblPendientes, "facturas (LBL)");
        agregar(b, rechazados(), "rechazados");
        return b.toString();
    }

    /** Detalle para mensajes de confirmación. */
    public String detalle() {
        if (!modoRest) return "Modo SOAP";
        return "Lecturas: " + liquidacionesPendientes + " por enviar, " + liquidacionesRechazadas + " rechazadas\n"
                + "Fotos: " + fotosPendientes + " por enviar, " + fotosRechazadas + " rechazadas\n"
                + "Facturas (LBL): " + lblPendientes + " por enviar, " + lblRechazados + " rechazadas";
    }

    private static void agregar(StringBuilder b, int n, String que) {
        if (n <= 0) return;
        if (b.length() > 0) b.append(" · ");
        b.append(n).append(' ').append(que);
    }
}
