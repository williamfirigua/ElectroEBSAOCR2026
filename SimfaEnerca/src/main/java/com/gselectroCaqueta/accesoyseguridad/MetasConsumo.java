package com.gselectroCaqueta.accesoyseguridad;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Metas de consumo (ahorro y exceso), modelo heredado del Caqueta (ProcesarEXCESOyAHORRO).
 *
 * Solo corre con APLICAMETA = "SI", consumo mayor que cero y sin causal de no lectura:
 *   - consumo menor que Metames90:  ahorro = Metames - consumo, con tope Meta30 (si viene).
 *                                   Solo se informa: NO descuenta, igual que en el Caqueta.
 *   - consumo mayor que Metames110: excedente = consumo - Metames110;
 *                                   valor = excedente x min(CU x FactorMeta, CRO_Meta), redondeado al peso.
 *                                   Se cobra en el concepto CONCEPTO_EXCESO.
 *
 * Diferencias con el Caqueta, para no cobrar de mas con datos incompletos:
 *   - sin Metames90 / Metames110 (en blanco o 0) no hay ahorro / exceso (alla cobraba todo el consumo);
 *   - CRO_Meta en blanco o 0 no es tope (alla dejaba el kWh en 0);
 *   - un valor que no es numero cuenta como en blanco (alla tumbaba la liquidacion).
 */
public final class MetasConsumo {

    /**
     * Concepto PROVISIONAL del exceso. En el Caqueta es el 275, que en EBSA ya es alumbrado publico.
     * Tiene que ser el mismo de config/metas.php (concepto_exceso) en la web.
     */
    public static final int CONCEPTO_EXCESO = 997;

    public final int ahorroKwh;
    public final int excedenteKwh;
    public final long valorAdicional;

    private MetasConsumo(int ahorroKwh, int excedenteKwh, long valorAdicional) {
        this.ahorroKwh = ahorroKwh;
        this.excedenteKwh = excedenteKwh;
        this.valorAdicional = valorAdicional;
    }

    public static final MetasConsumo NINGUNA = new MetasConsumo(0, 0, 0);

    public boolean hayAlgo() {
        return ahorroKwh > 0 || excedenteKwh > 0;
    }

    /**
     * @param valorKwh el CU con que se liquida la energia (MDDOR1VLORKWH)
     */
    public static MetasConsumo calcular(String aplicaMeta, double consumo, String causaNoLectura,
                                        String metames, String metames90, String metames110, String meta30,
                                        String factorMeta, String croMeta, double valorKwh) {
        if (aplicaMeta == null || !aplicaMeta.trim().equalsIgnoreCase("SI")) return NINGUNA;
        if (consumo <= 0) return NINGUNA;
        String causa = causaNoLectura == null ? "" : causaNoLectura.trim();
        if (!causa.isEmpty() && !causa.equals("0")) return NINGUNA;

        int kwh = (int) consumo;
        double m90 = numero(metames90);
        double m110 = numero(metames110);

        if (m90 > 0 && consumo < m90) {
            int ahorro = (int) numero(metames) - kwh;
            double tope = numero(meta30);
            if (tope > 0 && ahorro > tope) ahorro = (int) tope;
            return ahorro > 0 ? new MetasConsumo(ahorro, 0, 0) : NINGUNA;
        }
        if (m110 > 0 && consumo > m110) {
            int excede = kwh - (int) m110;
            if (excede <= 0) return NINGUNA;
            double valorKwhMeta = valorKwh * numero(factorMeta);
            double tope = numero(croMeta);
            if (tope > 0 && valorKwhMeta > tope) valorKwhMeta = tope;
            return new MetasConsumo(0, excede, redondearPeso(excede * valorKwhMeta));
        }
        return NINGUNA;
    }

    /** Para el JSON de la lectura (lectura.metas): la web lo guarda en regis_lecturas. */
    public static Map<String, Object> json(String ahorro, String excedente, String valor) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("consumo_ahorro", (long) numero(ahorro));
        m.put("consumo_excedente", (long) numero(excedente));
        m.put("valor_consumo_adicional", (long) numero(valor));
        return m;
    }

    /** "0,8", ".8", " 120 " -> numero; en blanco o no numerico -> 0. */
    static double numero(String s) {
        if (s == null) return 0;
        String t = s.trim().replace(",", ".");
        if (t.isEmpty()) return 0;
        if (t.startsWith(".")) t = "0" + t;
        try {
            double v = Double.parseDouble(t);
            return Double.isNaN(v) || Double.isInfinite(v) ? 0 : v;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** Igual que VariablesGlobales.ejecutarAjusteUnidades: medio peso o mas sube. */
    static long redondearPeso(double pesos) {
        long entero = (long) pesos;
        double dec = pesos - entero;
        if (dec < 0) return dec <= -0.5 ? entero - 1 : entero;
        return dec >= 0.5 ? entero + 1 : entero;
    }
}
