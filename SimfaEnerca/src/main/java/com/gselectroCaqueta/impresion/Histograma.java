package com.gselectroCaqueta.impresion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Las siete barras del gráfico de consumo, como las imprime la factura oficial de EBSA:
 * los cinco períodos anteriores más recientes (del más viejo al más nuevo), ACTUAL y PROMEDIO.
 * <p>
 * La escala es contra el máximo real de las siete, así la barra mayor siempre llega al
 * alto máximo. Reemplaza la escala "techo a la centena" de {@code consumos_historicos()}
 * de la móvil, con la que 250 kWh ocupaba el 83 % del alto.
 */
public final class Histograma {

    public static final int BARRAS = 7;
    public static final int HISTORICAS = 5;

    private Histograma() {}

    public static final class Barra {
        public final String rotulo;
        public final int valor;
        /** Y superior de la barra (la base es Y inferior). Igual a la base cuando el valor es 0. */
        public final int y0;

        Barra(String rotulo, int valor, int y0) {
            this.rotulo = rotulo;
            this.valor = valor;
            this.y0 = y0;
        }
    }

    public static List<Barra> construir(DatosFactura d, int base, int altoMax, String formatoRotulo) {
        List<String> rotulos = new ArrayList<>(BARRAS);
        List<Integer> valores = new ArrayList<>(BARRAS);

        // anteriores[0] es el más reciente; se toman los 5 primeros y se invierten para
        // imprimir de izquierda (viejo) a derecha (nuevo). Si hay menos de 5, se rellena
        // por la izquierda con barras vacías para que ACTUAL y PROMEDIO no se muevan.
        int n = Math.min(HISTORICAS, d.anteriores.size());
        for (int i = 0; i < HISTORICAS - n; i++) {
            rotulos.add("");
            valores.add(0);
        }
        for (int i = n - 1; i >= 0; i--) {
            DatosFactura.PeriodoConsumo p = d.anteriores.get(i);
            rotulos.add(Formatos.formatear(p.anio, p.mes, 1, formatoRotulo));
            valores.add(p.consumo);
        }
        rotulos.add("ACTUAL");
        valores.add(d.consumoActual);
        rotulos.add("PROMEDIO");
        valores.add(d.promedio);

        int max = 0;
        for (int v : valores) max = Math.max(max, v);

        List<Barra> barras = new ArrayList<>(BARRAS);
        for (int i = 0; i < BARRAS; i++) {
            int v = Math.max(0, valores.get(i));
            int alto = max == 0 ? 0 : (int) Math.round((double) v * altoMax / max);
            barras.add(new Barra(rotulos.get(i), valores.get(i), base - alto));
        }
        return Collections.unmodifiableList(barras);
    }
}
