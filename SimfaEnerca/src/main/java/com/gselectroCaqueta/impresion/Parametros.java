package com.gselectroCaqueta.impresion;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parámetros declarados en la plantilla con líneas {@code ;@SET clave=valor}.
 * <p>
 * Son comentarios CPCL válidos (empiezan por ';'), así que la plantilla sigue siendo
 * un archivo que la impresora acepta tal cual. El motor los lee antes de resolver los
 * placeholders y los expone aquí con valores por defecto para que una plantilla vieja
 * no rompa una versión nueva del motor.
 */
public final class Parametros {

    private final Map<String, String> valores;

    public Parametros(Map<String, String> valores) {
        this.valores = Collections.unmodifiableMap(new LinkedHashMap<>(valores));
    }

    public static Parametros vacios() {
        return new Parametros(Collections.<String, String>emptyMap());
    }

    public String texto(String clave, String porDefecto) {
        String v = valores.get(clave);
        return v == null || v.trim().isEmpty() ? porDefecto : v.trim();
    }

    public int entero(String clave, int porDefecto) {
        String v = valores.get(clave);
        if (v == null || v.trim().isEmpty()) return porDefecto;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Parametro '" + clave + "' no es entero: " + v, e);
        }
    }

    public boolean bandera(String clave, boolean porDefecto) {
        String v = valores.get(clave);
        if (v == null || v.trim().isEmpty()) return porDefecto;
        return "1".equals(v.trim()) || "true".equalsIgnoreCase(v.trim()) || "si".equalsIgnoreCase(v.trim());
    }

    public Map<String, String> todos() {
        return valores;
    }

    // ---- claves conocidas (documentadas en FORMATO_CORTA.cpcl) ----

    /** Línea base (Y inferior) de las barras del histograma. */
    public int barrasBase()         { return entero("barras.base", 450); }
    /** Alto máximo en puntos de la barra más alta. */
    public int barrasAltoMax()      { return entero("barras.alto", 100); }
    /** Formato del rótulo de período bajo cada barra histórica. */
    public String barrasRotulo()    { return texto("barras.rotulo", "MMM/yy"); }
    /** Cuántos puntos por encima del tope de la barra va el valor impreso. */
    public int barrasValorDy()      { return entero("barras.valor_dy", 22); }
    /** Ancho de línea al que se parte el mensaje de interés (campo 218). */
    public int mensajeAncho()       { return entero("mensaje.ancho", 60); }
    /** Máximo de líneas del mensaje. */
    public int mensajeLineas()      { return entero("mensaje.lineas", 4); }
    /** Ancho de la descripción en las filas de conceptos. */
    public int conceptoAnchoDesc()  { return entero("concepto.ancho_desc", 26); }
    /** Ancho del valor en las filas de conceptos (alineado a la derecha). */
    public int conceptoAnchoValor() { return entero("concepto.ancho_valor", 12); }
    /** Quién decide el bloque de un concepto: {@code cobro} (NROCONVENIOS de CO_COBRO.SDA, por defecto) o {@code catalogo} (DATO1 de DES_CONC.TXT). */
    public BloquesConceptos.Fuente conceptoBloqueFuente() { return BloquesConceptos.Fuente.de(texto("concepto.bloque", "cobro")); }
    /** Orden dentro del bloque: {@code catalogo} (DATO2 de DES_CONC.TXT, por defecto) o {@code archivo} (como vienen en CO_COBRO.SDA). */
    public boolean conceptoOrdenCatalogo() { return !"archivo".equalsIgnoreCase(texto("concepto.orden", "catalogo")); }
    /** GLN de EBSA para el AI (415) del código GS1-128. */
    public String gln()             { return texto("ean.gln", "7709998000483"); }
    /** Prefijo del AI (8020) antes de la cuenta a 10 dígitos. */
    public String eanPrefijoRef()   { return texto("ean.prefijo_ref", "1000"); }
    /** Si es 1, el dato del GS1-128 lleva el FNC1 (byte 0x86) entre (8020) y (3900). */
    public boolean eanFnc1()        { return bandera("ean.fnc1", true); }
    /** URL del código QR. */
    public String qrUrl()           { return texto("qr.url", "https://www.ebsa.com.co/sitio/pagina/pagos/"); }
    /** Si es 1, las líneas T cuyo texto queda vacío no se envían a la impresora. */
    public boolean omitirVacias()   { return bandera("omitir_vacias", true); }
    /** Texto que se imprime cuando una clave no existe en el contexto: visible en papel durante las pruebas. */
    public String faltante()        { return texto("faltante", "???"); }
}
