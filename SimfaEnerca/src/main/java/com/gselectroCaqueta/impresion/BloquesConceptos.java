package com.gselectroCaqueta.impresion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reparte los conceptos liquidados en los tres bloques de la factura oficial
 * (DETALLE DE LA FACTURA, GESTIÓN CARTERA EBSA, CONCEPTOS EXTERNOS) y calcula sus totales.
 * <p>
 * La clasificación debe venir del catálogo ({@code des_conceptos.bloque_factura}, exportado
 * en {@code DES_CONC.TXT} columna DATO1). Mientras EBSA no lo llene, el
 * {@link ClasificadorPorCodigo} cubre los códigos conocidos de la conciliación del ciclo 11.
 * El código de la móvil nunca debe decidir el bloque por sí mismo: eso es un dato de negocio.
 */
public final class BloquesConceptos {

    public enum Bloque {
        PERIODO('P'), CARTERA('C'), EXTERNO('E');

        public final char letra;

        Bloque(char letra) { this.letra = letra; }

        public static Bloque porLetra(char c) {
            for (Bloque b : values()) if (b.letra == Character.toUpperCase(c)) return b;
            return null;
        }
    }

    public interface Clasificador {
        Bloque clasificar(DatosFactura.Concepto c);
    }

    /**
     * Primero la letra del catálogo; si no viene, la tabla de códigos conocida; si tampoco,
     * PERIODO (que es donde un concepto desconocido hace menos daño: suma al total del período
     * y queda a la vista).
     */
    public static final class ClasificadorPorCodigo implements Clasificador {
        private static final Map<Integer, Bloque> CONOCIDOS = new HashMap<>();

        static {
            // Período (energía y sus ajustes)
            for (int c : new int[]{21, 31, 17, 18, 517, 518, 607, 608, 16, 613, 509, 626, 627, 980, 850, 988, 992, 989})
                CONOCIDOS.put(c, Bloque.PERIODO);
            // Gestión cartera (saldos, intereses, pagos, financiación)
            for (int c : new int[]{0, 1, 2, 3, 4, 5, 7, 8, 12, 110, 112, 710, 900, 903})
                CONOCIDOS.put(c, Bloque.CARTERA);
            // Externos (alumbrado público y convenios municipales)
            for (int c : new int[]{51, 212, 241, 261, 271, 403, 407, 408})
                CONOCIDOS.put(c, Bloque.EXTERNO);
        }

        @Override
        public Bloque clasificar(DatosFactura.Concepto c) {
            Bloque delCatalogo = c.bloque == '\0' || c.bloque == ' ' ? null : Bloque.porLetra(c.bloque);
            if (delCatalogo != null) return delCatalogo;
            Bloque conocido = CONOCIDOS.get(c.codigo);
            return conocido != null ? conocido : Bloque.PERIODO;
        }
    }

    public static final class Resultado {
        public final List<DatosFactura.Concepto> periodo;
        public final List<DatosFactura.Concepto> cartera;
        public final List<DatosFactura.Concepto> externos;
        public final long totalPeriodo;
        public final long totalCartera;
        public final long totalExternos;

        Resultado(List<DatosFactura.Concepto> periodo, List<DatosFactura.Concepto> cartera, List<DatosFactura.Concepto> externos) {
            this.periodo = Collections.unmodifiableList(periodo);
            this.cartera = Collections.unmodifiableList(cartera);
            this.externos = Collections.unmodifiableList(externos);
            this.totalPeriodo = suma(periodo);
            this.totalCartera = suma(cartera);
            this.totalExternos = suma(externos);
        }

        /** Total a pagar de la sección de energía (sin aseo), como la factura oficial. */
        public long totalPagar() {
            return totalPeriodo + totalCartera + totalExternos;
        }

        private static long suma(List<DatosFactura.Concepto> l) {
            long s = 0;
            for (DatosFactura.Concepto c : l) s += c.valor;
            return s;
        }
    }

    private BloquesConceptos() {}

    /** Los conceptos en cero no se imprimen (misma regla que la móvil hoy). */
    public static Resultado agrupar(List<DatosFactura.Concepto> conceptos, Clasificador clasificador) {
        List<DatosFactura.Concepto> p = new ArrayList<>(), c = new ArrayList<>(), e = new ArrayList<>();
        for (DatosFactura.Concepto x : conceptos) {
            if (x.valor == 0) continue;
            switch (clasificador.clasificar(x)) {
                case CARTERA: c.add(x); break;
                case EXTERNO: e.add(x); break;
                default: p.add(x);
            }
        }
        return new Resultado(p, c, e);
    }

    /**
     * Texto de una fila: descripción a la izquierda y valor alineado a la derecha, en una
     * sola cadena para fuentes de paso fijo. Si hay más conceptos que filas disponibles,
     * los sobrantes se agrupan en "Otros conceptos" para que el total impreso siga siendo
     * la suma de lo que se ve.
     */
    public static List<String> filas(List<DatosFactura.Concepto> conceptos, int maxFilas, int anchoDesc, int anchoValor) {
        List<String> out = new ArrayList<>(maxFilas);
        int visibles = conceptos.size() <= maxFilas ? conceptos.size() : maxFilas - 1;
        for (int i = 0; i < visibles; i++) {
            DatosFactura.Concepto c = conceptos.get(i);
            out.add(fila(c.descripcion, c.valor, anchoDesc, anchoValor));
        }
        if (conceptos.size() > maxFilas) {
            long otros = 0;
            for (int i = visibles; i < conceptos.size(); i++) otros += conceptos.get(i).valor;
            out.add(fila("Otros conceptos", otros, anchoDesc, anchoValor));
        }
        return out;
    }

    public static String fila(String descripcion, long valor, int anchoDesc, int anchoValor) {
        return Formatos.izquierda(descripcion.trim(), anchoDesc) + " " + Formatos.derecha(Formatos.moneda(valor), anchoValor);
    }
}
