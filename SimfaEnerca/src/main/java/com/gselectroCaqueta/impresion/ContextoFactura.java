package com.gselectroCaqueta.impresion;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Construye el diccionario {@code clave → texto} que resuelve la plantilla.
 * Aquí vive todo el cálculo de presentación; la plantilla solo posiciona y formatea.
 * <p>
 * <b>Claves numéricas</b>: {@code {n}} es la columna n de {@code lm_suscriptor_ru} tal como la
 * numera el Excel del diseñador (fila "Identificación en Diseño de Caída de Datos"), cruda.
 * <p>
 * <b>Claves con nombre</b> (todas ya formateadas salvo que se indique):
 * <pre>
 * cuenta, doc_equivalente, mes_servicio (JUN/2026), nombre, direccion, municipio
 * periodo_desde, periodo_hasta (dd/MM/yy), tipo_liquidacion, tipo_lectura, fecha_generacion
 * barra.1..7.valor | .rotulo | .y0 | .y_valor   histograma (1..5 históricas, 6 ACTUAL, 7 PROMEDIO)
 * lect_ant_activa, lect_act_activa, factor_activa, consumo_activa, prefacturado_activa, total_activa
 * lect_ant_reactiva, lect_act_reactiva, factor_reactiva, consumo_reactiva, total_reactiva   ("" si no hay)
 * nivel_tension, circuito_nodo, grupo, estrato, clase_servicio, propiedad_activos, iua, carga, ruta,
 * med_activa, med_reactiva        (las claves sin fuente no se definen: imprimen el marcador "???")
 * liq.1..3.tipo | .kwh | .periodo | .vunit | .kwh_sub | .pct | .venta | .subsidio   y   liq.1..3 (fila compuesta)
 * mensaje.1..N
 * costo.g .t .d .cv .pr .r .cf .cu, costo_diario (solo si la fuente lo trae)
 * calidad.1..3 (fila compuesta: Mes Diu Dium Diug Fiu Fium Fiug)
 * foes.consumo .vunit .total .factura
 * financiacion, cuotas_ptes, ultimo_pago, fecha_ultimo_pago
 * detalle.1..9, cartera.1..7, externos.1..5        (filas compuestas, vacías si no hay)
 * total_periodo, cartera.total, externos.total, total_base_iva, iva, total_pagar (sin aseo),
 * total_aseo, total_cupon (con aseo)
 * pago_oportuno, suspension, fecha_vencimiento (dd MMM/yyyy)
 * ean128, ean128_legible, qr_url, version
 * </pre>
 */
public final class ContextoFactura {

    public static final int MAX_DETALLE = 9;
    public static final int MAX_CARTERA = 7;
    public static final int MAX_EXTERNOS = 5;
    public static final int MAX_LIQ = 3;
    public static final int MAX_CALIDAD = 3;

    private static final String FECHA_CORTA = "dd/MM/yy";
    private static final String FECHA_LARGA = "dd MMM/yyyy";

    private ContextoFactura() {}

    public static Map<String, String> construir(DatosFactura d, Parametros p) {
        return construir(d, p, new BloquesConceptos.ClasificadorPorCodigo());
    }

    public static Map<String, String> construir(DatosFactura d, Parametros p, BloquesConceptos.Clasificador clasificador) {
        Map<String, String> c = new LinkedHashMap<>(256);

        caidasDerivadas(d, c);
        for (Map.Entry<Integer, String> e : d.caidas.entrySet()) {
            c.put(String.valueOf(e.getKey()), e.getValue() == null ? "" : e.getValue().trim());
        }

        identificacion(d, c);
        periodo(d, c);
        histograma(d, p, c);
        medidores(d, c);
        datosTecnicos(d, c);
        BloquesConceptos.Resultado bloques = conceptos(d, p, clasificador, c);
        liquidacion(d, c);
        mensaje(d, p, c);
        costos(d, c, bloques);
        calidad(d, c);
        foesYComercial(d, c);
        pie(d, p, c, bloques);
        return c;
    }

    // ------------------------------------------------------------------ caídas del Excel

    /**
     * Claves numéricas = columna de lm_suscriptor_ru según la fila "Identificación en Diseño de
     * Caída de Datos" del Excel. Solo se definen las que tienen fuente; una caída que no esté
     * aquí ni en {@link DatosFactura#caidas} imprime el marcador de faltante.
     */
    private static void caidasDerivadas(DatosFactura d, Map<String, String> c) {
        c.put("2", String.valueOf(d.anio));
        c.put("3", String.valueOf(d.mes));
        c.put("5", d.municipio.trim());
        c.put("6", d.fechaPagoOportuno.trim());
        c.put("7", d.fechaSuspension.trim());
        c.put("10", d.ruta.trim());
        c.put("11", d.cuenta.trim());
        c.put("12", String.valueOf(d.mesesPeriodo));
        c.put("14", d.documentoEquivalente.trim());
        c.put("16", d.nombre.trim());
        c.put("17", d.direccion.trim());
        siHay(c, "18", d.codigoMensaje);
        c.put("19", d.estrato.trim());
        c.put("20", d.claseServicio.trim());
        c.put("23", d.ultimoPagoFecha.trim());
        c.put("24", d.ultimoPagoValor.trim());
        c.put("29", d.cargaInstalada.trim());
        siHay(c, "30", d.nivelTension);
        c.put("31", d.nodo.trim());
        siHay(c, "32", d.iua);
        c.put("33", d.circuito.trim());
        c.put("34", d.grupo.trim());
        c.put("38", d.activa.serie.trim());
        c.put("41", enteroOTexto(d.activa.factor));
        c.put("42", enteroOTexto(d.activa.lecturaAnterior));
        c.put("43", enteroOTexto(d.activa.lecturaActual));
        c.put("45", String.valueOf(d.promedio));
        c.put("48", String.valueOf(d.activa.consumo));
        c.put("49", String.valueOf(d.porcentajeSubsidio));
        c.put("50", String.valueOf(d.valorKwh));
        c.put("53", d.fechaLecturaTomada.trim());
        if (d.activa.prefacturado != null) c.put("58", String.valueOf(d.activa.prefacturado));
        // segundo medidor: sin reactiva son datos legítimamente vacíos, no faltantes
        DatosFactura.Medidor r = d.reactiva;
        c.put("62", r == null ? "" : r.serie.trim());
        c.put("65", r == null ? "" : enteroOTexto(r.factor));
        c.put("66", r == null ? "" : enteroOTexto(r.lecturaAnterior));
        c.put("67", r == null ? "" : enteroOTexto(r.lecturaActual));
        c.put("72", r == null ? "" : String.valueOf(r.consumo));
        for (int i = 0; i < 6; i++) {
            if (i < d.anteriores.size()) {
                DatosFactura.PeriodoConsumo p = d.anteriores.get(i);
                c.put(String.valueOf(83 + i), String.valueOf(p.consumo));
                c.put(String.valueOf(157 + i), Formatos.formatear(p.anio, p.mes, 1, "MM/yy"));
            } else {
                c.put(String.valueOf(83 + i), "");
                c.put(String.valueOf(157 + i), "");
            }
        }
        c.put("89", d.costoG.trim()); c.put("90", d.costoT.trim()); c.put("91", d.costoPR.trim()); c.put("92", d.costoD.trim());
        c.put("93", d.costoR.trim()); c.put("94", d.costoCV.trim()); c.put("95", d.costoCF.trim()); c.put("96", d.costoCU.trim());
        c.put("134", d.financiacionValor.trim());
        c.put("136", d.financiacionCuotasPendientes.trim());
        c.put("140", d.foesConsumo.trim());
        c.put("141", d.foesTotal.trim());
        c.put("142", d.foesFactura.trim());
        c.put("165", d.foesValorUnitario.trim());
        c.put("218", d.mensaje.trim());
        c.put("219", String.valueOf(d.totalBaseIva));
    }

    // ------------------------------------------------------------------ secciones

    private static void identificacion(DatosFactura d, Map<String, String> c) {
        c.put("cuenta", d.cuenta.trim());
        c.put("doc_equivalente", d.documentoEquivalente.trim());
        c.put("mes_servicio", Formatos.formatear(d.anio, d.mes, 1, "MMM/yyyy"));
        c.put("nombre", d.nombre.trim());
        c.put("direccion", d.direccion.trim());
        c.put("municipio", d.municipio.trim());
    }

    private static void periodo(DatosFactura d, Map<String, String> c) {
        c.put("periodo_desde", Formatos.fecha(d.fechaLecturaAnterior, FECHA_CORTA));
        c.put("periodo_hasta", Formatos.fecha(d.fechaLecturaTomada, FECHA_CORTA));
        c.put("tipo_liquidacion", tipoLiquidacion(d.mesesPeriodo));
        c.put("tipo_lectura", d.tipoLectura.trim());
        c.put("fecha_generacion", d.fechaHoraGeneracion.trim());
    }

    static String tipoLiquidacion(int meses) {
        switch (meses) {
            case 2: return "BIMESTRAL";
            case 3: return "TRIMESTRAL";
            default: return "MENSUAL";
        }
    }

    private static void histograma(DatosFactura d, Parametros p, Map<String, String> c) {
        List<Histograma.Barra> barras = Histograma.construir(d, p.barrasBase(), p.barrasAltoMax(), p.barrasRotulo());
        for (int i = 0; i < barras.size(); i++) {
            Histograma.Barra b = barras.get(i);
            String k = "barra." + (i + 1);
            c.put(k + ".valor", b.rotulo.isEmpty() ? "" : String.valueOf(b.valor));
            c.put(k + ".rotulo", b.rotulo);
            c.put(k + ".y0", String.valueOf(b.y0));
            c.put(k + ".y_valor", String.valueOf(b.y0 - p.barrasValorDy()));
        }
    }

    private static void medidores(DatosFactura d, Map<String, String> c) {
        medidor("activa", d.activa, c);
        medidor("reactiva", d.reactiva, c);
        c.put("med_activa", d.activa.serie.trim());
        c.put("med_reactiva", d.reactiva == null || d.reactiva.serie.trim().isEmpty() ? "-" : d.reactiva.serie.trim());
    }

    private static void medidor(String sufijo, DatosFactura.Medidor m, Map<String, String> c) {
        if (m == null) {
            for (String k : new String[]{"lect_ant_", "lect_act_", "factor_", "consumo_", "prefacturado_", "total_"}) c.put(k + sufijo, "");
            return;
        }
        c.put("lect_ant_" + sufijo, enteroOTexto(m.lecturaAnterior));
        c.put("lect_act_" + sufijo, enteroOTexto(m.lecturaActual));
        c.put("factor_" + sufijo, enteroOTexto(m.factor));
        c.put("consumo_" + sufijo, String.valueOf(m.consumo));
        if (m.prefacturado != null) {
            c.put("prefacturado_" + sufijo, m.prefacturado == 0 ? "" : String.valueOf(m.prefacturado));
            c.put("total_" + sufijo, String.valueOf(m.consumo + m.prefacturado));
        } else {
            // sin fuente: prefacturado queda sin clave (imprime "???") y el total es solo el consumo
            c.put("total_" + sufijo, String.valueOf(m.consumo));
        }
    }

    private static void datosTecnicos(DatosFactura d, Map<String, String> c) {
        siHay(c, "nivel_tension", d.nivelTension);
        siHay(c, "circuito_nodo", d.circuito.trim().isEmpty() && d.nodo.trim().isEmpty() ? "" : d.circuito.trim() + " - " + d.nodo.trim());
        c.put("grupo", d.grupo.trim());
        c.put("estrato", d.estrato.trim());
        c.put("clase_servicio", d.claseServicio.trim());
        siHay(c, "propiedad_activos", d.propiedadActivos);
        siHay(c, "iua", d.iua);
        c.put("carga", enteroOTexto(d.cargaInstalada));
        siHay(c, "ruta", d.ruta);
    }

    private static BloquesConceptos.Resultado conceptos(DatosFactura d, Parametros p, BloquesConceptos.Clasificador cl, Map<String, String> c) {
        BloquesConceptos.Resultado r = BloquesConceptos.agrupar(d.conceptos, cl);
        int ad = p.conceptoAnchoDesc(), av = p.conceptoAnchoValor();
        filas("detalle", BloquesConceptos.filas(r.periodo, MAX_DETALLE, ad, av), MAX_DETALLE, c);
        filas("cartera", BloquesConceptos.filas(r.cartera, MAX_CARTERA, ad, av), MAX_CARTERA, c);
        filas("externos", BloquesConceptos.filas(r.externos, MAX_EXTERNOS, ad, av), MAX_EXTERNOS, c);
        c.put("total_periodo", Formatos.moneda(r.totalPeriodo));
        c.put("cartera.total", Formatos.moneda(r.totalCartera));
        c.put("externos.total", Formatos.moneda(r.totalExternos));
        c.put("total_base_iva", Formatos.moneda(d.totalBaseIva));
        c.put("iva", Formatos.moneda(d.iva));
        c.put("total_pagar", Formatos.moneda(r.totalPagar()));
        c.put("total_aseo", d.tieneAseo ? Formatos.moneda(d.totalAseo) : "");
        c.put("total_cupon", Formatos.moneda(r.totalPagar() + (d.tieneAseo ? d.totalAseo : 0)));
        return r;
    }

    private static void filas(String prefijo, List<String> filas, int max, Map<String, String> c) {
        for (int i = 1; i <= max; i++) {
            c.put(prefijo + "." + i, i <= filas.size() ? filas.get(i - 1) : "");
        }
    }

    private static void liquidacion(DatosFactura d, Map<String, String> c) {
        long venta = valorConcepto(d, 21);
        long subsidio = valorConcepto(d, 17) + valorConcepto(d, 18) + valorConcepto(d, 517) + valorConcepto(d, 518);
        List<TablaLiquidacion.Fila> filas = TablaLiquidacion.construir(
                d.consumoActual, d.mesesPeriodo, d.anio, d.mes, d.valorKwh, d.porcentajeSubsidio,
                d.kwhSubsistencia, venta, subsidio);
        for (int i = 1; i <= MAX_LIQ; i++) {
            String k = "liq." + i;
            if (i > filas.size()) {
                for (String s : new String[]{"", ".tipo", ".kwh", ".periodo", ".vunit", ".kwh_sub", ".pct", ".venta", ".subsidio"}) c.put(k + s, "");
                continue;
            }
            TablaLiquidacion.Fila f = filas.get(i - 1);
            c.put(k + ".tipo", f.tipo);
            c.put(k + ".kwh", String.valueOf(f.kwh));
            c.put(k + ".periodo", f.periodo());
            c.put(k + ".vunit", "$ " + Formatos.decimal(f.valorUnitario, 4));
            c.put(k + ".kwh_sub", String.valueOf(f.kwhSubsidiados));
            c.put(k + ".pct", f.porcentaje == 0 ? "" : Formatos.decimal(Math.abs(f.porcentaje), 2) + "%");
            c.put(k + ".venta", Formatos.moneda(f.valorVenta));
            c.put(k + ".subsidio", Formatos.moneda(f.subsidio));
            c.put(k, Formatos.izquierda(f.tipo, 8)
                    + Formatos.derecha(String.valueOf(f.kwh), 6) + "  "
                    + Formatos.izquierda(f.periodo(), 9)
                    + Formatos.izquierda("$ " + Formatos.decimal(f.valorUnitario, 4), 12)
                    + Formatos.derecha(String.valueOf(f.kwhSubsidiados), 5) + "   "
                    + Formatos.derecha(c.get(k + ".pct"), 7) + "  "
                    + Formatos.derecha(Formatos.moneda(f.valorVenta), 12)
                    + Formatos.derecha(Formatos.moneda(f.subsidio), 11));
        }
    }

    private static long valorConcepto(DatosFactura d, int codigo) {
        long s = 0;
        for (DatosFactura.Concepto x : d.conceptos) if (x.codigo == codigo) s += x.valor;
        return s;
    }

    private static void mensaje(DatosFactura d, Parametros p, Map<String, String> c) {
        List<String> lineas = partirPalabras(d.mensaje, p.mensajeAncho(), p.mensajeLineas());
        for (int i = 1; i <= p.mensajeLineas(); i++) {
            c.put("mensaje." + i, i <= lineas.size() ? lineas.get(i - 1) : "");
        }
    }

    /** Parte por palabras sin cortar ninguna; lo que no cabe en las líneas disponibles se pierde. */
    public static List<String> partirPalabras(String texto, int ancho, int maxLineas) {
        List<String> out = new ArrayList<>(maxLineas);
        if (texto == null) return out;
        StringBuilder linea = new StringBuilder();
        for (String palabra : texto.trim().split("\\s+")) {
            if (palabra.isEmpty()) continue;
            if (linea.length() > 0 && linea.length() + 1 + palabra.length() > ancho) {
                out.add(linea.toString());
                linea.setLength(0);
                if (out.size() == maxLineas) return out;
            }
            if (linea.length() > 0) linea.append(' ');
            linea.append(palabra.length() > ancho ? palabra.substring(0, ancho) : palabra);
        }
        if (linea.length() > 0 && out.size() < maxLineas) out.add(linea.toString());
        return out;
    }

    private static void costos(DatosFactura d, Map<String, String> c, BloquesConceptos.Resultado bloques) {
        c.put("costo.g", dec2(d.costoG));
        c.put("costo.t", dec2(d.costoT));
        c.put("costo.d", dec2(d.costoD));
        c.put("costo.cv", dec2(d.costoCV));
        c.put("costo.pr", dec2(d.costoPR));
        c.put("costo.r", dec2(d.costoR));
        c.put("costo.cf", dec2(d.costoCF));
        c.put("costo.cu", "$ " + Formatos.decimal(Formatos.numero(d.costoCU), 4));
        if (d.costoDiario != null) c.put("costo_diario", Formatos.moneda(Formatos.numero(d.costoDiario)));
    }

    /** "324,56"; un cero se imprime "0" como en la factura oficial ("CF: 0"). */
    private static String dec2(String v) {
        double n = Formatos.numero(v);
        return n == 0 ? "0" : Formatos.decimal(n, 2);
    }

    private static void calidad(DatosFactura d, Map<String, String> c) {
        for (int i = 1; i <= MAX_CALIDAD; i++) {
            if (i > d.calidad.size()) { c.put("calidad." + i, ""); continue; }
            DatosFactura.Calidad q = d.calidad.get(i - 1);
            // Orden impreso en la factura oficial: Mes Diu Dium Diug Fiu Fium Fiug
            c.put("calidad." + i, Formatos.izquierda(q.mes.trim(), 4)
                    + Formatos.derecha(dec(q.diu, 2), 6) + Formatos.derecha(dec(q.dium, 1), 6) + Formatos.derecha(dec(q.diug, 1), 6)
                    + Formatos.derecha(dec(q.fiu, 1), 6) + Formatos.derecha(dec(q.fium, 1), 6) + Formatos.derecha(dec(q.fiug, 1), 6));
        }
    }

    private static String dec(String v, int n) {
        return Formatos.esVacio(v) ? "" : Formatos.decimal(Formatos.numero(v), n);
    }

    private static void foesYComercial(DatosFactura d, Map<String, String> c) {
        c.put("foes.consumo", vacioSiCero(d.foesConsumo));
        c.put("foes.vunit", vacioSiCero(d.foesValorUnitario));
        c.put("foes.total", Formatos.esVacio(d.foesTotal) || Formatos.numero(d.foesTotal) == 0 ? "" : Formatos.moneda(Formatos.numero(d.foesTotal)));
        c.put("foes.factura", d.foesFactura.trim());
        c.put("financiacion", Formatos.esVacio(d.financiacionValor) || Formatos.numero(d.financiacionValor) == 0 ? "" : Formatos.moneda(Formatos.numero(d.financiacionValor)));
        c.put("cuotas_ptes", vacioSiCero(d.financiacionCuotasPendientes));
        c.put("ultimo_pago", Formatos.esVacio(d.ultimoPagoValor) ? "" : Formatos.moneda(Formatos.numero(d.ultimoPagoValor)));
        c.put("fecha_ultimo_pago", Formatos.fecha(d.ultimoPagoFecha, "dd/MM/yyyy"));
    }

    private static void pie(DatosFactura d, Parametros p, Map<String, String> c, BloquesConceptos.Resultado bloques) {
        c.put("pago_oportuno", Formatos.fecha(d.fechaPagoOportuno, FECHA_LARGA));
        c.put("fecha_vencimiento", Formatos.fecha(d.fechaPagoOportuno, FECHA_LARGA));
        c.put("suspension", Formatos.fecha(d.fechaSuspension, FECHA_LARGA));
        long totalCupon = bloques.totalPagar() + (d.tieneAseo ? d.totalAseo : 0);
        c.put("ean128", CodigoBarras.gs1128(p.gln(), p.eanPrefijoRef(), d.cuenta, totalCupon, p.eanFnc1()));
        c.put("ean128_legible", CodigoBarras.legible(p.gln(), p.eanPrefijoRef(), d.cuenta, totalCupon));
        c.put("qr_url", p.qrUrl());
        c.put("version", d.version.trim());
        c.put("lector", d.lector.trim());
    }

    // ------------------------------------------------------------------ utilidades

    /** Define la clave solo si hay dato: sin clave, la plantilla imprime el marcador de faltante. */
    private static void siHay(Map<String, String> c, String clave, String valor) {
        if (!Formatos.esVacio(valor)) c.put(clave, valor.trim());
    }

    /** "20746.0" → "20746"; texto no numérico se deja como está. */
    private static String enteroOTexto(String v) {
        if (Formatos.esVacio(v)) return "";
        String t = v.trim();
        return t.matches("-?\\d+([.,]\\d+)?") ? String.valueOf(Math.round(Formatos.numero(t))) : t;
    }

    private static String vacioSiCero(String v) {
        return Formatos.esVacio(v) || Formatos.numero(v) == 0 ? "" : enteroOTexto(v);
    }
}
