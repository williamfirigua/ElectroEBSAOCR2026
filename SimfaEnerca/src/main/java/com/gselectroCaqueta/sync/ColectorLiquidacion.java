package com.gselectroCaqueta.sync;

import com.gselectroCaqueta.tablas.EnvioGPS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Junta las líneas de ENVIOGPRS que produce la liquidación de UNA cuenta (una por registrador) y las
 * encola como ítems JSON cuando la cuenta termina. Replica las reglas del envío SOAP
 * (EnviarAlServidorFacturacion):
 * <ul>
 *   <li>las líneas sin IDCONTADOR no se envían;</li>
 *   <li>los conceptos de CO_COBRO [PRIMERCONCEPTO, PRIMERCONCEPTO + NROCONCEPTOS) van una sola vez por
 *       cuenta (aquí, en el primer ítem); en rutas de lectura ("L") no hay conceptos.</li>
 * </ul>
 * Sin dependencias de Android. No es thread-safe: lo usa el hilo de la liquidación.
 */
public final class ColectorLiquidacion {

    /** Lee de CO_COBRO.SDA los conceptos [primero, primero + cantidad). */
    public interface LectorConceptos {
        List<Map<String, Object>> leer(int primero, int cantidad) throws Exception;
    }

    private final List<Map<String, Object>> lecturas = new ArrayList<>();
    private int primerConcepto;
    private int nroConceptos;
    private String cuenta;

    /** Llamar justo después de escribir la línea en ENVIOSGPRS.SDA (el objeto se reutiliza después). */
    public void agregar(EnvioGPS e) {
        if (texto(e.getEnvioGPS_IDCONTADOR()).isEmpty()) return;
        if (lecturas.isEmpty()) {
            primerConcepto = entero(e.getEnvioGPS_PRIMERCONCEPTO());
            nroConceptos = entero(e.getEnvioGPS_NROCONCEPTOS());
            cuenta = sinCeros(ItemLiquidacion.codCuenta(e.getEnvioGPS_CUENTA(), e.getEnvioGPS_DIGITOCHEQUEO())); // como CO_COBRO: cuenta + dígito
        }
        lecturas.add(ItemLiquidacion.lectura(e));
    }

    public boolean vacio() {
        return lecturas.isEmpty();
    }

    public void descartar() {
        lecturas.clear();
    }

    /**
     * Encola lo acumulado y vacía el colector.
     *
     * @param conConceptos false en rutas de lectura (tipoDeRuta "L")
     * @param avisos       recibe el detalle si los conceptos no se pudieron leer (la lectura se encola igual:
     *                     perderla sería peor; el aviso queda en el log para cargarlos por respaldo)
     * @return cantidad de ítems encolados
     */
    public int encolar(ColaEnvio cola, LectorConceptos lector, boolean conConceptos, List<String> avisos) {
        if (lecturas.isEmpty()) return 0;
        List<Map<String, Object>> conceptos = Collections.emptyList();
        if (conConceptos && nroConceptos > 0 && primerConcepto > 0) {
            conceptos = leerConReintento(lector, avisos);
        }
        int n = 0;
        try {
            for (Map<String, Object> l : lecturas) {
                String uid = UUID.randomUUID().toString();
                cola.encolar(ColaEnvio.LIQUIDACION, uid, ItemLiquidacion.armar(uid, l, n == 0 ? conceptos : null), null);
                n++;
            }
        } finally {
            lecturas.clear();
        }
        return n;
    }

    private List<Map<String, Object>> leerConReintento(LectorConceptos lector, List<String> avisos) {
        Exception ultimo = null;
        for (int intento = 0; intento < 2; intento++) {
            try {
                List<Map<String, Object>> c = lector.leer(primerConcepto, nroConceptos);
                String ajena = cuentaAjena(c);
                if (ajena != null) {
                    // Posiciones corridas en CO_COBRO: mandar cobros de otra cuenta es peor que no mandarlos.
                    ultimo = new IllegalStateException("el concepto es de la cuenta " + ajena + ", no de " + cuenta);
                    break;
                }
                if (c.size() == nroConceptos) return c;
                ultimo = new IllegalStateException("se esperaban " + nroConceptos + " conceptos y se leyeron " + c.size());
            } catch (Exception e) {
                ultimo = e;
            }
        }
        if (avisos != null) {
            avisos.add("cuenta " + cuenta + ": conceptos " + primerConcepto + "+" + nroConceptos + " no enviados: " + ultimo);
        }
        return Collections.emptyList();
    }

    private String cuentaAjena(List<Map<String, Object>> conceptos) {
        for (Map<String, Object> c : conceptos) {
            String de = sinCeros(String.valueOf(c.get("cuenta")));
            if (!de.equals(cuenta)) return de;
        }
        return null;
    }

    private static String sinCeros(String s) {
        String t = texto(s);
        int i = 0;
        while (i < t.length() - 1 && t.charAt(i) == '0') i++;
        return t.substring(i);
    }

    private static String texto(String s) {
        return s == null ? "" : s.trim();
    }

    private static int entero(String s) {
        try {
            return Integer.parseInt(texto(s));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
