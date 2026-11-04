package com.gselectroCaqueta.impresion;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Nota de factura retenida: el "Soporte de no entrega de factura al usuario" que el técnico llenaba a mano en
 * el talonario cuando recoge una factura (por ejemplo, metió la lectura en la cuenta que no era).
 * <p>
 * Mismos campos que el talonario: cuenta, fecha, motivo (contador incorrecto, mal estado del contador, consumo
 * excesivo u otros con su texto), lectura actual, teléfono de contacto y el día desde el que se entrega la
 * factura corregida. El número lo pone la terminal (consecutivo por lector) en vez del preimpreso.
 * <p>
 * Se imprime con la plantilla {@value #PLANTILLA} de DatosSoporte, editable en la web como las demás; si la
 * terminal aún no la tiene, con {@link #PLANTILLA_DEFECTO}, que es la misma versión 01.
 */
public final class NotaRetencion {

    public static final String PLANTILLA = "NOTA_RETENCION.CPCL";

    public static final String CONTADOR_INCORRECTO = "contador_incorrecto";
    public static final String MAL_ESTADO = "mal_estado_contador";
    public static final String CONSUMO_EXCESIVO = "consumo_excesivo";
    public static final String OTROS = "otros";

    /** Código → texto del talonario, en el orden del formato. */
    public static final String[][] MOTIVOS = {
            {CONTADOR_INCORRECTO, "Contador incorrecto"},
            {MAL_ESTADO, "Mal estado del contador"},
            {CONSUMO_EXCESIVO, "Consumo excesivo"},
            {OTROS, "Otros"},
    };

    public String consecutivo = "";
    public String cuenta = "";
    public String codCuenta = "";
    public String nombre = "";
    public String direccion = "";
    public String medidor = "";
    public long fechaMs;
    public String motivo = "";
    public String otros = "";
    /** Lectura que escribe el técnico en la nota (la real del medidor). */
    public String lectura = "";
    /** Lectura que quedó grabada en la terminal para la cuenta (la equivocada, si fue error de lectura). */
    public String lecturaSistema = "";
    public String telefono = "";
    /** dd/MM/yyyy o vacío. */
    public String fechaEntrega = "";
    public String lectorCodigo = "";
    public String lectorNombre = "";

    /** null si la nota está completa; si no, el primer problema, para mostrarlo al técnico. */
    public String validar(long hoyMs) {
        if (textoMotivo(motivo) == null) return "Seleccione el motivo.";
        if (OTROS.equals(motivo) && otros.trim().isEmpty()) return "Escriba el motivo en Otros.";
        if (otros.trim().length() > 40) return "El texto de Otros admite 40 caracteres.";
        if (!lectura.trim().matches("\\d{1,10}")) return "Escriba la lectura actual del medidor (solo números).";
        String tel = telefono.trim();
        if (!tel.isEmpty() && !tel.matches("\\d{7,10}")) return "El teléfono debe tener entre 7 y 10 dígitos.";
        if (!fechaEntrega.trim().isEmpty()) {
            Date f = fecha(fechaEntrega.trim());
            if (f == null) return "La fecha de entrega debe ser dd/mm/aaaa.";
            if (f.getTime() < inicioDelDia(hoyMs)) return "La fecha de entrega no puede ser anterior a hoy.";
        }
        return null;
    }

    public static String textoMotivo(String codigo) {
        for (String[] m : MOTIVOS) if (m[0].equals(codigo)) return m[1];
        return null;
    }

    /** Claves {nota.*} para la plantilla. Las de las casillas valen "X" o "" (la línea vacía se omite). */
    public Map<String, String> contexto() {
        Map<String, String> c = new LinkedHashMap<>();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(fechaMs);
        c.put("nota.consecutivo", consecutivo);
        c.put("nota.cuenta", cuenta);
        c.put("nota.nombre", nombre);
        c.put("nota.direccion", direccion);
        c.put("nota.medidor", medidor);
        c.put("nota.dia", String.format(Locale.ROOT, "%02d", cal.get(Calendar.DAY_OF_MONTH)));
        c.put("nota.mes", String.format(Locale.ROOT, "%02d", cal.get(Calendar.MONTH) + 1));
        c.put("nota.anio", String.valueOf(cal.get(Calendar.YEAR)));
        c.put("nota.fecha", new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ROOT).format(new Date(fechaMs)));
        for (String[] m : MOTIVOS) c.put("nota.x_" + m[0], m[0].equals(motivo) ? "X" : "");
        c.put("nota.motivo", textoMotivo(motivo) == null ? "" : textoMotivo(motivo));
        c.put("nota.otros", OTROS.equals(motivo) ? otros.trim() : "");
        c.put("nota.lectura", lectura.trim());
        c.put("nota.telefono", telefono.trim());
        c.put("nota.fecha_entrega", fechaEntrega.trim());
        c.put("nota.lector", lectorNombre.trim());
        c.put("nota.lector_codigo", lectorCodigo.trim());
        return c;
    }

    /** Datos del evento factura_retenida. */
    public Map<String, Object> datosEvento() {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("consecutivo", consecutivo);
        d.put("motivo", motivo);
        d.put("otros", OTROS.equals(motivo) ? otros.trim() : "");
        d.put("lectura", lectura.trim());
        d.put("lectura_sistema", lecturaSistema.trim());
        d.put("medidor", medidor);
        d.put("telefono", telefono.trim());
        d.put("fecha_entrega", fechaEntrega.trim());
        d.put("lector_codigo", lectorCodigo.trim());
        return d;
    }

    /** Línea de DATOSDESALIDA/RETENIDAS.SDA (respaldo en la terminal, se recupera con los planos). */
    public String lineaSda() {
        return String.join("|", consecutivo, cuenta, codCuenta,
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT).format(new Date(fechaMs)),
                motivo, sinSeparador(otros.trim()), lectura.trim(), lecturaSistema.trim(), medidor, telefono.trim(),
                fechaEntrega.trim(), lectorCodigo.trim()) + "|";
    }

    /** CPCL de la nota con la plantilla instalada (o la de defecto). */
    public Plantilla.Resultado renderizar(File dirPlantillas, AjustesImpresora ajustes) throws IOException {
        File f = new File(dirPlantillas, PLANTILLA);
        Plantilla pl = f.isFile()
                ? Plantilla.cargar(f)
                : Plantilla.cargar(PLANTILLA, new ByteArrayInputStream(PLANTILLA_DEFECTO.getBytes(StandardCharsets.UTF_8)));
        return pl.renderizar(contexto(), ajustes == null ? AjustesImpresora.ninguno() : ajustes);
    }

    private static String sinSeparador(String s) {
        return s.replace('|', '/').replace('\r', ' ').replace('\n', ' ');
    }

    private static Date fecha(String ddmmyyyy) {
        SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy", Locale.ROOT);
        f.setLenient(false);
        try {
            return f.parse(ddmmyyyy);
        } catch (ParseException e) {
            return null;
        }
    }

    private static long inicioDelDia(long ms) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ms);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    /**
     * Versión 01 de NOTA_RETENCION.CPCL: el texto del talonario "Soporte de no entrega de factura al usuario".
     * La web instala la misma como versión inicial (database/sql/caidas/nota_retencion.sql); de ahí en adelante
     * se ajusta en la web y llega con los planos.
     */
    public static final String PLANTILLA_DEFECTO = String.join("\n",
            ";@ ============================================================================================",
            ";@ NOTA_RETENCION.CPCL - Soporte de no entrega de factura al usuario (factura retenida). Version 01.",
            ";@ Texto del talonario fisico. Papel preimpreso (logo y NIT en el rollo): sin PCX.",
            ";@ ============================================================================================",
            ";@SET vacias=omitir",
            "! 0 200 200 900 1",
            "LABEL",
            "PAGE-WIDTH 816",
            "SCALE-TEXT DINBOLD.TTF 11 13 303 10 SOPORTE DE NO ENTREGA DE",
            "SCALE-TEXT DINBOLD.TTF 11 13 303 42 FACTURA AL USUARIO",
            "SCALE-TEXT-DER DINBOLD.TTF 11 13 786 10 No. {nota.consecutivo}",
            "SCALE-TEXT-DER DINCOND.TTF 9 11 786 46 {nota.dia}/{nota.mes}/{nota.anio}",
            "LINE 30 152 786 152 3",
            "SCALE-TEXT DINBOLD.TTF 11 13 30 166 CUENTA No.",
            "SCALE-TEXT DINBOLD.TTF 12 14 230 164 {nota.cuenta}",
            "SCALE-TEXT DINCOND.TTF 9 10 30 206 {nota.nombre}",
            "SCALE-TEXT DINCOND.TTF 9 10 30 234 {nota.direccion}",
            "SCALE-TEXT DINCOND.TTF 8 10 30 276 Le informamos que el día de hoy visitamos su inmueble para facturar el servicio de",
            "SCALE-TEXT DINCOND.TTF 8 10 30 302 energía del respectivo periodo, lamentablemente su factura no pudo ser entregada",
            "SCALE-TEXT DINCOND.TTF 8 10 30 328 por el siguiente motivo:",
            "BOX 30 366 62 398 2",
            "SCALE-TEXT DINBOLD.TTF 10 11 38 368 {nota.x_contador_incorrecto}",
            "SCALE-TEXT DINCOND.TTF 9 10 76 370 CONTADOR INCORRECTO",
            "BOX 420 366 452 398 2",
            "SCALE-TEXT DINBOLD.TTF 10 11 428 368 {nota.x_consumo_excesivo}",
            "SCALE-TEXT DINCOND.TTF 9 10 466 370 CONSUMO EXCESIVO",
            "BOX 30 412 62 444 2",
            "SCALE-TEXT DINBOLD.TTF 10 11 38 414 {nota.x_mal_estado_contador}",
            "SCALE-TEXT DINCOND.TTF 9 10 76 416 MAL ESTADO DEL CONTADOR",
            "BOX 420 412 452 444 2",
            "SCALE-TEXT DINBOLD.TTF 10 11 428 414 {nota.x_otros}",
            "SCALE-TEXT DINCOND.TTF 9 10 466 416 OTROS",
            "SCALE-TEXT DINCOND.TTF 9 10 466 448 {nota.otros}",
            "SCALE-TEXT DINCOND.TTF 9 10 30 490 LECTURA ACTUAL",
            "BOX 220 482 400 520 2",
            "SCALE-TEXT DINBOLD.TTF 10 12 232 488 {nota.lectura}",
            "SCALE-TEXT DINCOND.TTF 9 10 420 490 TELÉFONO DE CONTACTO",
            "SCALE-TEXT DINBOLD.TTF 9 10 640 490 {nota.telefono}",
            "SCALE-TEXT DINCOND.TTF 8 10 30 546 La factura corregida le será entregada después del día",
            "SCALE-TEXT DINBOLD.TTF 9 10 560 544 {nota.fecha_entrega}",
            "SCALE-TEXT DINCOND.TTF 8 10 30 580 Ofrecemos disculpas por los inconvenientes presentados y ante cualquier inquietud",
            "SCALE-TEXT DINCOND.TTF 8 10 30 606 favor acercarse a los centros de servicio de la EBSA.",
            "SCALE-TEXT DINCOND.TTF 8 9 30 740 {nota.lector}",
            "LINE 30 776 380 776 1",
            "SCALE-TEXT DINBOLD.TTF 8 9 30 784 FIRMA LECTOR",
            "LINE 436 776 786 776 1",
            "SCALE-TEXT DINBOLD.TTF 8 9 436 784 FIRMA USUARIO",
            "FORM",
            "PRINT");
}
