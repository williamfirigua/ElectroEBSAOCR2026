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

    /**
     * @param consumo          kWh liquidados en el período
     * @param meses            1, 2 o 3
     * @param anio             año del período facturado (fila más reciente)
     * @param mes              mes del período facturado (fila más reciente)
     * @param valorKwh         MDDOR1VLORKWH
     * @param porcentaje       MDDOR1SBSDIOCNTRBCION (negativo = subsidio, positivo = contribución)
     * @param kwhSubsistencia  consumo de subsistencia mensual (LS_CONS_SUBS); 0 si no aplica
     * @param totalVentaCobrado    concepto 21 cobrado, o 0 para no conciliar
     * @param totalSubsidioCobrado concepto 17/18 cobrado con signo, o 0 para no conciliar
     */
    public static List<Fila> construir(int consumo, int meses, int anio, int mes, double valorKwh,
                                       double porcentaje, int kwhSubsistencia,
                                       long totalVentaCobrado, long totalSubsidioCobrado) {
        int n = Math.max(1, Math.min(3, meses));
        int base = Math.max(0, consumo) / n;
        int resto = Math.max(0, consumo) - base * n;

        List<Fila> filas = new ArrayList<>(n);
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

        if (totalVentaCobrado != 0 && sumaVenta != totalVentaCobrado
                || totalSubsidioCobrado != 0 && sumaSubsidio != totalSubsidioCobrado) {
            Fila u = filas.get(n - 1);
            long ventaU = totalVentaCobrado != 0 ? u.valorVenta + (totalVentaCobrado - sumaVenta) : u.valorVenta;
            long subU = totalSubsidioCobrado != 0 ? u.subsidio + (totalSubsidioCobrado - sumaSubsidio) : u.subsidio;
            filas.set(n - 1, new Fila(u.tipo, u.kwh, u.anio, u.mes, u.valorUnitario, u.kwhSubsidiados, u.porcentaje, ventaU, subU));
        }
        return Collections.unmodifiableList(filas);
    }
}
