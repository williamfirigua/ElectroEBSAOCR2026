package com.gselectroCaqueta.sync;

import com.gselectroCaqueta.tablas.TablaCobrosRealizados;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Lee de CO_COBRO.SDA, en solo lectura y con instancia propia (no comparte puntero con la liquidación ni con
 * el hilo SOAP), los conceptos de una cuenta por posición, igual que EnviarAlServidorFacturacion.
 */
public final class LectorCoCobro implements ColectorLiquidacion.LectorConceptos {

    private final File archivo;

    public LectorCoCobro(File archivo) {
        this.archivo = archivo;
    }

    @Override
    public List<Map<String, Object>> leer(int primero, int cantidad) throws IOException {
        long registros = archivo.length() / TablaCobrosRealizados.LONGITUD_REGISTRO;
        if (primero < 1 || primero + cantidad - 1 > registros) {
            throw new IOException("conceptos " + primero + "+" + cantidad + " fuera de " + archivo.getName() + " (" + registros + " registros)");
        }
        TablaCobrosRealizados t = new TablaCobrosRealizados();
        if (!Boolean.TRUE.equals(t.abrirSoloLectura_TablaCobrosRealizados(archivo.getAbsolutePath()))) {
            throw new IOException("no se pudo abrir " + archivo);
        }
        try {
            List<Map<String, Object>> r = new ArrayList<>(cantidad);
            for (int z = primero; z < primero + cantidad; z++) {
                t.lectura_TablaCobrosRealizados(z);
                r.add(ItemLiquidacion.concepto(t));
            }
            return r;
        } finally {
            t.Cerrar_TablaCobrosRealizados();
        }
    }
}
