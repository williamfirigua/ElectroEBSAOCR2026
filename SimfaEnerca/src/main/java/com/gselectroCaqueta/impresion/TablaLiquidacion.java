package com.gselectroCaqueta.impresion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tabla "Tipo / Consumo kWh / Período / Valor unitario / kWh subsidiados / % / Valor venta /
 * Subsidio-Contribución": una fila por mes del período facturado, del más reciente al más viejo.
 * <p>
 * Regla verificada contra la factura oficial (cuenta 1034008100, 431 kWh trimestrales):
 * el consumo se parte en tercios enteros y el residuo va a los meses más recientes
 * (144, 144, 143); cada fila redondea su valor por separado (132.591 + 132.591 + 131.670 =
 * 396.852, que es el concepto 21 cobrado; el producto directo daría 396.853).
 * <p>
 * Para que la tabla nunca contradiga el detalle, la suma de las filas se concilia con el
 * valor realmente cobrado (concepto 21) y con el subsidio/contribución cobrado (17/18):
 * si difieren por redondeo, la diferencia se carga a la fila más antigua.
 * <p>
 * Si la cuenta tiene medidor de reactiva se agrega una fila REACTIVA al final (período más
 * reciente): kWh = reactiva facturada (CONSUMO3, solo el excedente que se cobra), valor
 * unitario MDDOR2VLORKWH, venta = concepto 31. El subsidio/contribución de la reactiva no
 * tiene concepto propio: el móvil lo suma en el 17/18 junto con el de la activa. Por eso,
 * con reactiva con porcentaje (MDDOR2SBSDIOCNTRBCION != 0), las filas de activa calculan su
 * contribución sin conciliar y la REACTIVA
 * toma el resto del 17/18: la tabla suma lo cobrado y la reactiva muestra lo que de verdad
 * se le cargó.
 */
public final class TablaLiquidacion {

    private TablaLiquidacion() {}

    public static final class Fila {
        public final String tipo;
        public final int kwh;
        public final int anio;
        public final int mes;
        public final double valorUnitario;
        public final int kwhSubsidiados;
        public final double porcentaje;
        public final long valorVenta;
        public final long subsidio;

        Fila(String tipo, int kwh, int anio, int mes, double valorUnitario, int kwhSubsidiados,
             double porcentaje, long valorVenta, long subsidio) {
            this.tipo = tipo;
            this.kwh = kwh;
            this.anio = anio;
            this.mes = mes;
            this.valorUnitario = valorUnitario;
            this.kwhSubsidiados = kwhSubsidiados;
            this.porcentaje = porcentaje;
            this.valorVenta = valorVenta;
            this.subsidio = subsidio;
        }

        public String periodo() {
            return Formatos.formatear(anio, mes, 1, "MM/yyyy");
        }
    }

    /** Datos de la reactiva facturada; null si la cuenta no tiene medidor de reactiva. */
    public static final class Reactiva {
        final int kwh;
        final double valorKwh;
        final double porcentaje;
        final long valorCobrado;

        /**
         * @param kwh          reactiva facturada (CONSUMO3)
         * @param valorKwh     MDDOR2VLORKWH
         * @param porcentaje   MDDOR2SBSDIOCNTRBCION
         * @param valorCobrado concepto 31 cobrado
         */
        public Reactiva(int kwh, double valorKwh, double porcentaje, long valorCobrado) {
            this.kwh = Math.max(0, kwh);
            this.valorKwh = valorKwh;
            this.porcentaje = porcentaje;
            this.valorCobrado = valorCobrado;
        }
    }

    /** Solo activa. */
    public static List<Fila> construir(int consumo, int meses, int anio, int mes, double valorKwh,
                                       double porcentaje, int kwhSubsistencia,
                                       long totalVentaCobrado, long totalSubsidioCobrado) {
        return construir(consumo, meses, anio, mes, valorKwh, porcentaje, kwhSubsistencia,
                totalVentaCobrado, totalSubsidioCobrado, null);
    }

    /**
     * @param consumo          kWh liquidados en el período
     * @param meses            1, 2 o 3
     * @param anio             año del período facturado (fila más reciente)
     * @param mes              mes del período facturado (fila más reciente)
     * @param valorKwh         MDDOR1VLORKWH
     * @param porcentaje       MDDOR1SBSDIOCNTRBCION (negativo = subsidio, positivo = contribución)
     * @param kwhSubsistencia  consumo de subsistencia mensual (LS_CONS_SUBS); 0 si no aplica
     * @param totalVentaCobrado    concepto 21 cobrado, o 0 para no conciliar
     * @param totalSubsidioCobrado concepto 17/18 cobrado con signo (activa + reactiva), o 0 para no conciliar
     * @param reactiva             reactiva facturada, o null si la cuenta no tiene
     */
    public static List<Fila> construir(int consumo, int meses, int anio, int mes, double valorKwh,
                                       double porcentaje, int kwhSubsistencia,
                                       long totalVentaCobrado, long totalSubsidioCobrado, Reactiva reactiva) {
        int n = Math.max(1, Math.min(3, meses));
        int base = Math.max(0, consumo) / n;
        int resto = Math.max(0, consumo) - base * n;

        List<Fila> filas = new ArrayList<>(n + 1);
        long sumaVenta = 0, sumaSubsidio = 0;
        int a = anio, m = mes;
        for (int i = 0; i < n; i++) {
            int kwh = base + (i < resto ? 1 : 0);
            long venta = Math.round(kwh * valorKwh);
            int kwhSub = porcentaje == 0 ? 0 : (kwhSubsistencia > 0 ? Math.min(kwh, kwhSubsistencia) : kwh);
            long sub = porcentaje == 0 ? 0 : Math.round(kwhSub * valorKwh * porcentaje / 100.0);
            filas.add(new Fila("ACTIVA", kwh, a, m, valorKwh, kwhSub, porcentaje, venta, sub));
            sumaVenta += venta;
            sumaSubsidio += sub;
            if (--m == 0) { m = 12; a--; }
        }

        // Con reactiva, el 17/18 también trae la contribución de la reactiva: la activa no se concilia
        // contra él (le cargaría lo de la reactiva) y el resto va a la fila REACTIVA.
        // Si la reactiva no tiene porcentaje, no lleva contribución y la activa se concilia como siempre.
        boolean reactivaConContribucion = reactiva != null && reactiva.porcentaje != 0;
        boolean conciliarSubsidio = totalSubsidioCobrado != 0 && !reactivaConContribucion;
        if (totalVentaCobrado != 0 && sumaVenta != totalVentaCobrado
                || conciliarSubsidio && sumaSubsidio != totalSubsidioCobrado) {
            Fila u = filas.get(n - 1);
            long ventaU = totalVentaCobrado != 0 ? u.valorVenta + (totalVentaCobrado - sumaVenta) : u.valorVenta;
            long subU = conciliarSubsidio ? u.subsidio + (totalSubsidioCobrado - sumaSubsidio) : u.subsidio;
            filas.set(n - 1, new Fila(u.tipo, u.kwh, u.anio, u.mes, u.valorUnitario, u.kwhSubsidiados, u.porcentaje, ventaU, subU));
        }

        if (reactiva != null) {
            long venta = reactiva.valorCobrado != 0 ? reactiva.valorCobrado : Math.round(reactiva.kwh * reactiva.valorKwh);
            long sub = !reactivaConContribucion ? 0
                    : totalSubsidioCobrado != 0 ? totalSubsidioCobrado - sumaSubsidio
                    : Math.round(venta * reactiva.porcentaje / 100.0);
            int kwhSub = reactiva.porcentaje == 0 ? 0 : reactiva.kwh;
            filas.add(new Fila("REACTIVA", reactiva.kwh, anio, mes, reactiva.valorKwh, kwhSub,
                    reactiva.porcentaje, venta, sub));
        }
        return Collections.unmodifiableList(filas);
    }
}
