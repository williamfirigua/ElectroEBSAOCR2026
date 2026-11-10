package com.gselectroCaqueta.accesoyseguridad;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Metas de consumo (ahorro y exceso): copia del Caqueta (ProcesarEXCESOyAHORRO), con la misma logica.
 *
 * Solo corre con APLICAMETA = "SI", consumo mayor que cero y sin causal de no lectura:
 *   - consumo menor que Metames90:  ahorro = Metames - consumo, con tope Meta30 si viene.
 *                                   Solo se informa: NO descuenta, igual que en el Caqueta.
 *   - si no, consumo mayor que Metames110: excedente = consumo - Metames110;
 *                                   valor = excedente x min(CU x FactorMeta, CRO_Meta), redondeado al peso.
 *                                   Se cobra en el concepto CONCEPTO_EXCESO.
 *
 * Igual que alla, a proposito: un campo en blanco vale 0. Asi, Metames110 en blanco o en 0 cobra TODO el
 * consumo como exceso; CRO_Meta o FactorMeta en blanco o en 0 dejan el kWh del exceso en $0. Las metas se
 * truncan a entero (parseStringToInteger).
 *
 * Distinto de alla: un texto que no es numero tumbaba la liquidacion de la cuenta, aqui cuenta como 0; y un
 * ahorro que da negativo o 0 (Metames en blanco o menor que el consumo) queda en 0: alla se mandaba negativo.
 * Todo lo que puede ser un error de datos lo informa revisar(), sin cambiar el calculo.
 */
public final class MetasConsumo {

    /**
     * Concepto PROVISIONAL del exceso. En el Caqueta es el 275, que en EBSA ya es alumbrado publico.
     * Tiene que ser el mismo de config/metas.php (concepto_exceso) en la web.
     */
    public static final int CONCEPTO_EXCESO = 997;

    /** kWh de ahorro. Nunca negativo. */
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
     * @param valorKwh el CU con que se liquida la energia: en el Caqueta VALORUNIDADREFERENCIA, en EBSA MDDOR1VLORKWH
     */
    public static MetasConsumo calcular(String aplicaMeta, double consumo, String causaNoLectura,
                                        String metames, String metames90, String metames110, String meta30,
                                        String factorMeta, String croMeta, double valorKwh) {
        if (!aplica(aplicaMeta)) return NINGUNA;
        if (consumo <= 0) return NINGUNA;
        String causa = causaNoLectura == null ? "" : causaNoLectura.trim();
        if (causa.equals("0")) causa = "";
        if (!causa.isEmpty()) return NINGUNA;

        if (consumo < entero(metames90)) {
            int ahorro = entero(metames) - (int) consumo;
            if (!vacio(meta30) && ahorro > entero(meta30)) ahorro = entero(meta30);
            return ahorro > 0 ? new MetasConsumo(ahorro, 0, 0) : NINGUNA;
        }
        if (consumo > entero(metames110)) {
            double valorKwhMeta = valorKwh * numero(factorMeta);
            double tope = numero(croMeta);
            if (valorKwhMeta > tope) valorKwhMeta = tope;
            int excede = (int) consumo - entero(metames110);
            return new MetasConsumo(0, excede, redondearPeso(excede * valorKwhMeta));
        }
        return NINGUNA;
    }

    /**
     * Posibles errores en los datos de la meta de una cuenta. No cambian el calculo: solo se informan
     * (log de la terminal; la web los cuenta al distribuir). Lista vacia si la cuenta no tiene meta.
     */
    public static List<String> revisar(String aplicaMeta, String metames, String metames90, String metames110,
                                       String meta30, String factorMeta, String croMeta) {
        List<String> a = new ArrayList<>();
        String ap = aplicaMeta == null ? "" : aplicaMeta.trim().toUpperCase();
        if (!ap.isEmpty() && !ap.equals("SI") && !ap.equals("NO")) {
            a.add("APLICAMETA = '" + aplicaMeta.trim() + "' no es SI ni NO: no se aplica la meta");
        }
        if (!ap.equals("SI")) return a;

        String[][] campos = {{"Metames", metames}, {"Metames90", metames90}, {"Metames110", metames110},
                {"Meta30", meta30}, {"FactorMeta", factorMeta}, {"CRO_Meta", croMeta}};
        for (String[] c : campos) {
            if (!vacio(c[1]) && !esNumero(c[1])) a.add(c[0] + " = '" + c[1].trim() + "' no es un numero: se toma como 0");
        }
        if (entero(metames110) <= 0) a.add("Metames110 en blanco o en 0: TODO el consumo se cobra como exceso");
        if (entero(metames90) <= 0) a.add("Metames90 en blanco o en 0: no puede haber ahorro");
        if (numero(factorMeta) <= 0) a.add("FactorMeta en blanco o en 0: el exceso vale $0");
        if (numero(croMeta) <= 0) a.add("CRO_Meta en blanco o en 0: el tope deja el exceso en $0");
        if (entero(metames90) > 0 && entero(metames) <= 0) a.add("Metames en blanco o en 0 con Metames90 lleno: el ahorro daria negativo y queda en 0");
        if (entero(metames90) > 0 && entero(metames110) > 0 && entero(metames90) > entero(metames110)) {
            a.add("Metames90 (" + entero(metames90) + ") mayor que Metames110 (" + entero(metames110) + "): banda invertida");
        }
        if (entero(metames) > 0 && entero(metames90) > 0 && entero(metames110) > 0
                && (entero(metames) < entero(metames90) || entero(metames) > entero(metames110))) {
            a.add("Metames (" + entero(metames) + ") por fuera de la banda " + entero(metames90) + "-" + entero(metames110));
        }
        if (vacio(meta30)) a.add("Meta30 en blanco: el ahorro no tiene tope");
        return a;
    }

    /** Para el JSON de la lectura (lectura.metas): la web lo guarda en regis_lecturas. */
    public static Map<String, Object> json(String ahorro, String excedente, String valor) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("consumo_ahorro", (long) numero(ahorro));
        m.put("consumo_excedente", (long) numero(excedente));
        m.put("valor_consumo_adicional", (long) numero(valor));
        return m;
    }

    static boolean aplica(String aplicaMeta) {
        return aplicaMeta != null && aplicaMeta.trim().equalsIgnoreCase("SI");
    }

    static boolean vacio(String s) {
        return s == null || s.trim().isEmpty();
    }

    static boolean esNumero(String s) {
        String t = normal(s);
        if (t.isEmpty()) return false;
        try {
            double v = Double.parseDouble(t);
            return !Double.isNaN(v) && !Double.isInfinite(v);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** "0,8", ".8", " 120 " -> numero; en blanco o no numerico -> 0. */
    static double numero(String s) {
        return esNumero(s) ? Double.parseDouble(normal(s)) : 0;
    }

    /** Como parseStringToInteger del Caqueta: trunca los decimales. */
    static int entero(String s) {
        return (int) numero(s);
    }

    private static String normal(String s) {
        if (s == null) return "";
        String t = s.trim().replace(",", ".");
        if (t.startsWith(".")) t = "0" + t;
        return t;
    }

    /** Igual que VariablesGlobales.ejecutarAjusteUnidades: medio peso o mas sube. */
    static long redondearPeso(double pesos) {
        long entero = (long) pesos;
        double dec = pesos - entero;
        if (dec < 0) return dec <= -0.5 ? entero - 1 : entero;
        return dec >= 0.5 ? entero + 1 : entero;
    }
}
