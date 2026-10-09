package com.gselectroCaqueta.impresion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reparte los conceptos liquidados en los cuatro bloques de la factura oficial y calcula sus totales:
 * <pre>
 *   1  DETALLE DE LA FACTURA
 *   2  GESTIÓN CARTERA EBSA
 *   3  CONCEPTOS EXTERNOS
 *   4  ASEO                      (solo en la factura larga; su total entra al cupón)
 * </pre>
 * El bloque es un dato de negocio que viene en los planos, no lo decide la móvil:
 * <ul>
 *   <li>por cobro: {@code CO_COBRO.SDA} NROCONVENIOS, reutilizado para esto ({@link DatosFactura.Concepto#bloque});</li>
 *   <li>por catálogo: {@code DES_CONC.TXT} DATO1 ({@link DatosFactura.Concepto#bloqueCatalogo}).</li>
 * </ul>
 * {@link Fuente} dice cuál manda cuando difieren. El mapa por código es el último respaldo y solo
 * aplica a conceptos que no traen bloque por ninguna de las dos vías.
 * <p>
 * Dentro de cada bloque los conceptos van por {@link DatosFactura.Concepto#orden} (DATO2 del catálogo:
 * energía, reactiva, subsidio, contribución… y "Ajuste decena" al final, como la factura oficial); los
 * que no traen orden quedan después, en el orden del archivo. Con {@code ordenarPorCatalogo=false} se
 * respeta el orden del archivo.
 */
public final class BloquesConceptos {

    public enum Bloque {
        PERIODO(1), CARTERA(2), EXTERNO(3), ASEO(4);

        public final int numero;

        Bloque(int numero) { this.numero = numero; }

        public static Bloque porNumero(int n) {
            for (Bloque b : values()) if (b.numero == n) return b;
            return null;
        }
    }

    /** Qué indicador manda cuando el cobro y el catálogo no coinciden. */
    public enum Fuente {
        /** NROCONVENIOS del cobro primero; DATO1 del catálogo si no viene. (Definición del equipo EBSA.) */
        COBRO,
        /** DATO1 del catálogo primero; NROCONVENIOS si no viene. */
        CATALOGO;

        public static Fuente de(String s) {
            return s != null && s.trim().equalsIgnoreCase("catalogo") ? CATALOGO : COBRO;
        }
    }

    public interface Clasificador {
        Bloque clasificar(DatosFactura.Concepto c);
    }

    /**
     * Bloque del plano (cobro o catálogo según {@link Fuente}); si ninguno viene, la tabla de códigos
     * conocidos; si tampoco, PERIODO, que es donde un concepto desconocido hace menos daño: suma al
     * total del período y queda a la vista.
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

        private final Fuente fuente;

        public ClasificadorPorCodigo() { this(Fuente.COBRO); }

        public ClasificadorPorCodigo(Fuente fuente) { this.fuente = fuente == null ? Fuente.COBRO : fuente; }

        @Override
        public Bloque clasificar(DatosFactura.Concepto c) {
            int primero = fuente == Fuente.COBRO ? c.bloque : c.bloqueCatalogo;
            int segundo = fuente == Fuente.COBRO ? c.bloqueCatalogo : c.bloque;
            Bloque b = Bloque.porNumero(primero);
            if (b == null) b = Bloque.porNumero(segundo);
            if (b == null) b = CONOCIDOS.get(c.codigo);
            return b != null ? b : Bloque.PERIODO;
        }
    }

    public static final class Resultado {
        public final List<DatosFactura.Concepto> periodo;
        public final List<DatosFactura.Concepto> cartera;
        public final List<DatosFactura.Concepto> externos;
        public final List<DatosFactura.Concepto> aseo;
        public final long totalPeriodo;
        public final long totalCartera;
        public final long totalExternos;
        public final long totalAseo;

        Resultado(List<DatosFactura.Concepto> periodo, List<DatosFactura.Concepto> cartera,
                  List<DatosFactura.Concepto> externos, List<DatosFactura.Concepto> aseo) {
            this.periodo = Collections.unmodifiableList(periodo);
            this.cartera = Collections.unmodifiableList(cartera);
            this.externos = Collections.unmodifiableList(externos);
            this.aseo = Collections.unmodifiableList(aseo);
            this.totalPeriodo = suma(periodo);
            this.totalCartera = suma(cartera);
            this.totalExternos = suma(externos);
            this.totalAseo = suma(aseo);
        }

        /** Total a pagar de la sección de energía (sin aseo), como la factura oficial. */
        public long totalPagar() {
            return totalPeriodo + totalCartera + totalExternos;
        }

        /** Total del cupón que se queda el banco: energía + aseo. */
        public long totalCupon() {
            return totalPagar() + totalAseo;
        }

        /** Conceptos cuyo bloque del cobro y del catálogo vienen ambos y no coinciden: para la bitácora. */
        public List<DatosFactura.Concepto> discrepancias() {
            List<DatosFactura.Concepto> out = new ArrayList<>();
            for (List<DatosFactura.Concepto> l : Arrays.asList(periodo, cartera, externos, aseo)) {
                for (DatosFactura.Concepto c : l) {
                    if (c.bloque != 0 && c.bloqueCatalogo != 0 && c.bloque != c.bloqueCatalogo) out.add(c);
                }
            }
            return out;
        }

        private static long suma(List<DatosFactura.Concepto> l) {
            long s = 0;
            for (DatosFactura.Concepto c : l) s += c.valor;
            return s;
        }
    }

    private BloquesConceptos() {}

    public static Resultado agrupar(List<DatosFactura.Concepto> conceptos, Clasificador clasificador) {
        return agrupar(conceptos, clasificador, true);
    }

    /** Los conceptos en cero no se imprimen (misma regla que la móvil hoy). */
    public static Resultado agrupar(List<DatosFactura.Concepto> conceptos, Clasificador clasificador, boolean ordenarPorCatalogo) {
        List<DatosFactura.Concepto> p = new ArrayList<>(), c = new ArrayList<>(), e = new ArrayList<>(), a = new ArrayList<>();
        for (DatosFactura.Concepto x : conceptos) {
            if (x.valor == 0) continue;
            switch (clasificador.clasificar(x)) {
                case CARTERA: c.add(x); break;
                case EXTERNO: e.add(x); break;
                case ASEO:    a.add(x); break;
                default:      p.add(x);
            }
        }
        if (ordenarPorCatalogo) {
            // Estable: los que traen orden van primero por ese orden; los demás conservan el orden del archivo.
            Comparator<DatosFactura.Concepto> porOrden = Comparator.comparingInt(x -> x.orden > 0 ? x.orden : Integer.MAX_VALUE);
            p.sort(porOrden); c.sort(porOrden); e.sort(porOrden); a.sort(porOrden);
        }
        return new Resultado(p, c, e, a);
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
