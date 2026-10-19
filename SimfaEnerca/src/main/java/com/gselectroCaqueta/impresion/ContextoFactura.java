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
 * detalle.1..9, cartera.1..7, externos.1..5, aseo.1..6   (filas compuestas, vacías si no hay; bloques 1..4)
 * total_periodo, cartera.total, externos.total, aseo.total, total_base_iva, iva, total_pagar (sin aseo),
 * total_aseo, total_cupon (con aseo)
 * pago_oportuno, suspension, fecha_vencimiento (dd MMM/yyyy)
 * ean128, ean128_legible, qr_url, version
 * </pre>
 */
public final class ContextoFactura {

    public static final int MAX_DETALLE = 9;
    public static final int MAX_CARTERA = 7;
    public static final int MAX_EXTERNOS = 5;
    public static final int MAX_ASEO = 6;
    public static final int MAX_LIQ = 3;
    public static final int MAX_CALIDAD = 3;

    private static final String FECHA_CORTA = "dd/MM/yy";
    private static final String FECHA_LARGA = "dd MMM/yyyy";

    private ContextoFactura() {}

    public static Map<String, String> construir(DatosFactura d, Parametros p) {
        return construir(d, p, new BloquesConceptos.ClasificadorPorCodigo(p.conceptoBloqueFuente()));
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
        aseo(d, p, c, bloques);
        desviacion(d, p, c);
        pie(d, p, c, bloques);
        parrafos(p, c);
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
        BloquesConceptos.Resultado r = BloquesConceptos.agrupar(d.conceptos, cl, p.conceptoOrdenCatalogo());
        int ad = p.conceptoAnchoDesc(), av = p.conceptoAnchoValor();
        filas("detalle", BloquesConceptos.filas(r.periodo, MAX_DETALLE, ad, av), MAX_DETALLE, c);
        filas("cartera", BloquesConceptos.filas(r.cartera, MAX_CARTERA, ad, av), MAX_CARTERA, c);
        filas("externos", BloquesConceptos.filas(r.externos, MAX_EXTERNOS, ad, av), MAX_EXTERNOS, c);
        filas("aseo", BloquesConceptos.filas(r.aseo, MAX_ASEO, ad, av), MAX_ASEO, c);
        // Descripción y valor por separado, para fuentes proporcionales (SCALE-TEXT + SCALE-TEXT-DER)
        // Filas que tiene la plantilla por bloque (;@SET concepto.filas.cartera=5); lo que no cabe va a "Otros conceptos".
        columnas("detalle", r.periodo, p.conceptoFilas("detalle", MAX_DETALLE), c);
        columnas("cartera", r.cartera, p.conceptoFilas("cartera", MAX_CARTERA), c);
        columnas("externos", r.externos, p.conceptoFilas("externos", MAX_EXTERNOS), c);
        columnas("aseo", r.aseo, p.conceptoFilas("aseo", MAX_ASEO), c);
        c.put("total_periodo", Formatos.moneda(r.totalPeriodo));
        c.put("cartera.total", Formatos.moneda(r.totalCartera));
        c.put("externos.total", Formatos.moneda(r.totalExternos));
        c.put("total_base_iva", Formatos.moneda(d.totalBaseIva));
        c.put("iva", Formatos.moneda(d.iva));
        c.put("total_pagar", Formatos.moneda(r.totalPagar()));
        boolean hayAseo = tieneAseo(d, r);
        c.put("aseo.total", hayAseo ? Formatos.moneda(totalAseo(d, r)) : "");
        c.put("total_aseo", hayAseo ? Formatos.moneda(totalAseo(d, r)) : "");
        c.put("total_cupon", Formatos.moneda(r.totalPagar() + totalAseo(d, r)));
        return r;
    }

    /** Hay aseo si el adaptador lo marcó (CUENTA_ASEO, indicador A/Y) o si algún cobro vino en el bloque 4. */
    static boolean tieneAseo(DatosFactura d, BloquesConceptos.Resultado r) {
        return d.hayAseo() || !r.aseo.isEmpty();
    }

    /** Total de aseo: la suma del bloque 4 si vino desagregado; si no, el acumulado que trajo el adaptador. */
    static long totalAseo(DatosFactura d, BloquesConceptos.Resultado r) {
        if (!tieneAseo(d, r)) return 0;
        return r.aseo.isEmpty() ? d.totalAseo : r.totalAseo;
    }

    /** {prefijo}.N.desc y {prefijo}.N.valor; si no caben, la última fila agrupa el resto en "Otros conceptos". */
    private static void columnas(String prefijo, List<DatosFactura.Concepto> l, int max, Map<String, String> c) {
        int visibles = l.size() <= max ? l.size() : max - 1;
        for (int i = 1; i <= max; i++) {
            String desc = "", valor = "";
            if (i <= visibles) {
                desc = l.get(i - 1).descripcion.trim();
                valor = Formatos.moneda(l.get(i - 1).valor);
            } else if (i == max && l.size() > max) {
                long otros = 0;
                for (int k = visibles; k < l.size(); k++) otros += l.get(k).valor;
                desc = "Otros conceptos";
                valor = Formatos.moneda(otros);
            }
            c.put(prefijo + "." + i + ".desc", desc);
            c.put(prefijo + "." + i + ".valor", valor);
        }
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
        List<String> lineas = FuenteMetricas.conoce(p.mensajeFuente())
                ? partirPorAncho(d.mensaje, p.mensajeFuente(), p.mensajePuntos(), p.mensajeAnchoPx(), p.mensajeLineas())
                : partirPalabras(d.mensaje, p.mensajeAncho(), p.mensajeLineas());
        for (int i = 1; i <= p.mensajeLineas(); i++) {
            c.put("mensaje." + i, i <= lineas.size() ? lineas.get(i - 1) : "");
        }
    }

    /** Como {@link #partirPalabras} pero midiendo cada línea con la fuente TTF real (puntos de impresora). */
    public static List<String> partirPorAncho(String texto, String fuente, double puntos, int anchoPx, int maxLineas) {
        List<String> out = new ArrayList<>(maxLineas);
        if (texto == null) return out;
        String linea = "";
        for (String palabra : texto.trim().split("\\s+")) {
            if (palabra.isEmpty()) continue;
            String prueba = linea.isEmpty() ? palabra : linea + " " + palabra;
            if (!linea.isEmpty() && FuenteMetricas.ancho(fuente, puntos, prueba) > anchoPx) {
                out.add(linea);
                if (out.size() == maxLineas) return out;
                linea = palabra;
            } else {
                linea = prueba;
            }
        }
        if (!linea.isEmpty() && out.size() < maxLineas) out.add(linea);
        return out;
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
            if (i > d.calidad.size()) {
                c.put("calidad." + i, "");
                for (String k : new String[]{"mes", "diu", "dium", "diug", "fiu", "fium", "fiug"}) c.put("calidad." + i + "." + k, "");
                continue;
            }
            DatosFactura.Calidad q = d.calidad.get(i - 1);
            // Por columna, para SCALE-TEXT: calidad.N.mes / .diu / .dium / .diug / .fiu / .fium / .fiug
            c.put("calidad." + i + ".mes", q.mes.trim());
            c.put("calidad." + i + ".diu", dec(q.diu, 2));
            c.put("calidad." + i + ".dium", dec(q.dium, 1));
            c.put("calidad." + i + ".diug", dec(q.diug, 1));
            c.put("calidad." + i + ".fiu", dec(q.fiu, 1));
            c.put("calidad." + i + ".fium", dec(q.fium, 1));
            c.put("calidad." + i + ".fiug", dec(q.fiug, 1));
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
        long totalCupon = bloques.totalPagar() + totalAseo(d, bloques);
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

    // ------------------------------------------------------------------ aseo (factura larga)

    /**
     * {As_n}: columna n de la estructura de aseo del Excel, tal cual llega (recortada).
     * Derivadas:
     * <pre>
     * aseo.vbna / aseo.cvna      residencial si viene, si no no residencial (As_15|As_16, As_13|As_14)
     * aseo.servicio .subsidio .aprovechamiento .incentivo .otros     desglose del bloque 4 por código
     * aseo.total                 Σ bloque 4 (VALOR A PAGAR ASEO)
     * aseo.punto_atencion        As_71 + " " + As_72
     * aseo.obs.1..N              As_73 partido por ancho (N = ;@SET aseo.obs.lineas, 5 por defecto)
     * </pre>
     */
    private static void aseo(DatosFactura d, Parametros p, Map<String, String> c, BloquesConceptos.Resultado r) {
        for (Map.Entry<Integer, String> e : d.aseo.entrySet()) {
            c.put("As_" + e.getKey(), e.getValue() == null ? "" : e.getValue().trim());
        }
        if (!tieneAseo(d, r)) return;
        // Con aseo pero sin estructura (cuenta sin CUENTA_ASEO en MEDIDOR.TXT): campos en blanco, no "???".
        for (int n = 1; n <= 74; n++) c.putIfAbsent("As_" + n, "");
        c.put("As_3", d.cuenta.trim());
        c.put("aseo.vbna", primeroConDato(d.aseo.get(15), d.aseo.get(16)));
        c.put("aseo.cvna", primeroConDato(d.aseo.get(13), d.aseo.get(14)));
        c.put("aseo.punto_atencion", (t(d.aseo.get(71)) + " " + t(d.aseo.get(72))).trim());

        java.util.Set<Integer> servicio = codigos(p.aseoCodigos("servicio", "460,461,462,480"));
        java.util.Set<Integer> subsidio = codigos(p.aseoCodigos("subsidio", "463"));
        java.util.Set<Integer> aprovech = codigos(p.aseoCodigos("aprovechamiento", "465"));
        java.util.Set<Integer> incentivo = codigos(p.aseoCodigos("incentivo", "466"));
        long sServ = 0, sSub = 0, sApr = 0, sInc = 0, sOtr = 0;
        for (DatosFactura.Concepto x : r.aseo) {
            if (servicio.contains(x.codigo)) sServ += x.valor;
            else if (subsidio.contains(x.codigo)) sSub += x.valor;
            else if (aprovech.contains(x.codigo)) sApr += x.valor;
            else if (incentivo.contains(x.codigo)) sInc += x.valor;
            else sOtr += x.valor;
        }
        c.put("aseo.servicio", Formatos.moneda(sServ));
        c.put("aseo.subsidio", Formatos.moneda(sSub));
        c.put("aseo.aprovechamiento", Formatos.moneda(sApr));
        c.put("aseo.incentivo", Formatos.moneda(sInc));
        c.put("aseo.otros", Formatos.moneda(sOtr));

        String obs = t(d.aseo.get(73));
        int maxObs = Math.max(1, p.entero("aseo.obs.lineas", 5));
        List<String> lineas = FuenteMetricas.conoce(p.mensajeFuente())
                ? partirPorAncho(obs, p.mensajeFuente(), p.mensajePuntos(), p.aseoObsAnchoPx(), maxObs)
                : partirPalabras(obs, 80, maxObs);
        for (int i = 1; i <= maxObs; i++) c.put("aseo.obs." + i, i <= lineas.size() ? lineas.get(i - 1) : "");
    }

    private static java.util.Set<Integer> codigos(String lista) {
        java.util.Set<Integer> s = new java.util.HashSet<>();
        for (String x : lista.split("[,; ]+")) {
            if (x.trim().isEmpty()) continue;
            try { s.add(Integer.parseInt(x.trim())); } catch (NumberFormatException ignored) { }
        }
        return s;
    }

    private static String primeroConDato(String a, String b) {
        return !t(a).isEmpty() ? t(a) : t(b);
    }

    private static String t(String s) {
        return s == null ? "" : s.trim();
    }

    // ------------------------------------------------------------------ notificación de desviación

    /**
     * regla_desviacion: la regla de analítica de EBSA por la que se exceptuó la visita (documento de EBSA
     * "Reglas de desviaciones", CREG 105 007 de 2024). Cada regla tiene su clave de texto en la plantilla
     * ({@code ;@SET desviacion.<clave>=Regla N: …}):
     * <pre>
     *   001  no supera el consumo de subsistencia (consumo &lt;= LS_CONS_SUBS) . subsistencia   (Regla 2)
     *   002  NRMLZACION / CMBIO_CLSE / CMBIO_NVEL en 1 (la primera por número) normalizacion  (Regla 3)
     *                                                                          clase          (Regla 4)
     *                                                                          nivel          (Regla 5)
     *   005  dentro de la banda por tipo de uso (consumo &lt;= LS_BDA_TPO_US) .. tipo_uso       (Regla 7)
     *        si no, dentro del máximo anual (consumo &lt;= LS_BNDA_ANUAL) ....... anual          (Regla 6)
     * </pre>
     * Es el mismo orden del algoritmo de EBSA: primero las reglas que no son de banda, después las de banda.
     * {@code ;@SET desviacion.<código>=} sigue sirviendo para fijar un texto por código. Sin texto: vacía (???).
     */
    private static void desviacion(DatosFactura d, Parametros p, Map<String, String> c) {
        String codigo = t(d.analitica);
        String regla = "";
        String clave = claveRegla(d, codigo);
        if (clave != null) regla = p.desvTexto(clave, "");
        if (regla.isEmpty() && !codigo.isEmpty()) regla = p.desvTexto(codigo, "");
        c.put("analitica", codigo);
        c.put("regla_clave", clave == null ? "" : clave);
        c.put("regla_desviacion", regla);
    }

    /** Clave de la regla que exceptuó la visita, o null si el código no es de excepción o ninguna regla cuadra. */
    static String claveRegla(DatosFactura d, String codigo) {
        switch (codigo) {
            case "001":
                // La móvil también deja 001 cuando no hay límite de subsistencia (LS_CONS_SUBS = 0):
                // ahí la Regla 2 no es la que exceptúa y no se afirma.
                return d.lsConsumoSubsistencia > 0 && d.consumoActual <= d.lsConsumoSubsistencia ? "subsistencia" : null;
            case "002":
                if ("1".equals(t(d.desvNormalizacion))) return "normalizacion";
                if ("1".equals(t(d.desvCambioClase))) return "clase";
                if ("1".equals(t(d.desvCambioNivel))) return "nivel";
                return null;
            case "005":
                double consumo = d.consumoActual;
                if (d.lsBandaTipoUso > 0 && consumo <= d.lsBandaTipoUso) return "tipo_uso";
                if (consumo <= d.lsBandaAnual) return "anual";
                return null;
            default:
                // Sin código (bosquejos, reimpresión antigua): por bandera, como antes.
                if (codigo.isEmpty()) {
                    if ("1".equals(t(d.desvNormalizacion))) return "normalizacion";
                    if ("1".equals(t(d.desvCambioClase))) return "clase";
                    if ("1".equals(t(d.desvCambioNivel))) return "nivel";
                }
                return null;
        }
    }

    // ------------------------------------------------------------------ párrafos de texto corrido

    private static final java.util.regex.Pattern PARRAFO = java.util.regex.Pattern.compile("parrafo\\.([A-Za-z0-9_]+)");
    private static final java.util.regex.Pattern CLAVE = java.util.regex.Pattern.compile("\\{([^{}|]+)\\}"); // "}" escapada: el regex ICU de Android no la acepta suelta

    /**
     * Texto corrido con datos (cartas): {@code ;@SET parrafo.regla=... debido a la {regla_desviacion}.} se arma
     * con las claves de la factura y se parte por ancho real en {@code parrafo.regla.1..N}. Una clave sin dato
     * queda como el faltante ({@code ???}), porque dentro de una frase no se puede omitir la línea.
     * Medidas comunes: {@code parrafo.fuente} (DINCOND.TTF), {@code parrafo.puntos} (7, el ancho del SCALE-TEXT),
     * {@code parrafo.ancho_px} (756), {@code parrafo.lineas} (8).
     */
    private static void parrafos(Parametros p, Map<String, String> c) {
        String fuente = p.texto("parrafo.fuente", "DINCOND.TTF");
        double puntos = Double.parseDouble(p.texto("parrafo.puntos", "7"));
        int ancho = p.entero("parrafo.ancho_px", 756);
        int lineas = p.entero("parrafo.lineas", 8);
        for (Map.Entry<String, String> e : p.todos().entrySet()) {
            java.util.regex.Matcher m = PARRAFO.matcher(e.getKey());
            if (!m.matches() || esMedidaDeParrafo(m.group(1))) continue;
            java.util.regex.Matcher k = CLAVE.matcher(e.getValue());
            StringBuffer texto = new StringBuffer();
            while (k.find()) {
                String v = t(c.get(k.group(1).trim()));
                k.appendReplacement(texto, java.util.regex.Matcher.quoteReplacement(v.isEmpty() ? p.faltante() : v));
            }
            k.appendTail(texto);
            List<String> partes = partirPorAncho(texto.toString(), fuente, puntos, ancho, lineas);
            for (int i = 0; i < lineas; i++) {
                c.put("parrafo." + m.group(1) + "." + (i + 1), i < partes.size() ? partes.get(i) : "");
            }
        }
    }

    private static boolean esMedidaDeParrafo(String nombre) {
        return nombre.equals("fuente") || nombre.equals("puntos") || nombre.equals("lineas") || nombre.equals("ancho_px");
    }
}
