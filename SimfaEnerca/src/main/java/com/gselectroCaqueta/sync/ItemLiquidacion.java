package com.gselectroCaqueta.sync;

import com.gselectroCaqueta.tablas.EnvioGPS;
import com.gselectroCaqueta.tablas.TablaCobrosRealizados;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Arma el ítem JSON de POST /api/v1/liquidaciones/lote con los MISMOS valores que se acaban de escribir
 * en el ENVIOGPRS (el objeto EnvioGPS después de agregar la línea) y los conceptos de la cuenta en
 * CO_COBRO.SDA. El servidor aplica a estos valores las mismas reglas que al plano, así que la base
 * queda igual llegue la lectura por JSON o por la carga de respaldo.
 */
public final class ItemLiquidacion {

    private ItemLiquidacion() {
    }

    /** regis_lecturas.cod_cuenta, como el servidor: cuenta + dígito de chequeo a 3 con ceros. */
    public static String codCuenta(String cuenta, String digito) {
        String d = t(digito);
        while (d.length() < 3) d = "0" + d;
        return t(cuenta) + d;
    }

    /** '' o A = activa, R = reactiva, cualquier otro = tercer registrador (misma regla del servidor y del SOAP). */
    public static String indicador(String nroContadorDb) {
        String i = t(nroContadorDb).toUpperCase();
        if (i.isEmpty() || i.equals("A")) return "A";
        return i.equals("R") ? "R" : "3";
    }

    /**
     * @param uid       client_uid (UUID) generado al liquidar; se guarda con el ítem en la cola
     * @param e         EnvioGPS con la línea recién agregada
     * @param conceptos conceptos de la cuenta (vacío para el segundo registrador de la misma cuenta)
     */
    public static String armar(String uid, EnvioGPS e, List<Map<String, Object>> conceptos) {
        return armar(uid, lectura(e), conceptos);
    }

    /** Ítem a partir de una lectura ya capturada con {@link #lectura(EnvioGPS)}. */
    public static String armar(String uid, Map<String, Object> lectura, List<Map<String, Object>> conceptos) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("client_uid", uid);
        item.put("lectura", lectura);
        item.put("conceptos", conceptos == null ? new ArrayList<Map<String, Object>>() : conceptos);
        return Json.escribir(item);
    }

    /**
     * Copia de los valores del EnvioGPS en este instante. Hay que tomarla justo después de escribir la línea:
     * el mismo objeto se reutiliza para el siguiente registrador de la cuenta.
     */
    public static Map<String, Object> lectura(EnvioGPS e) {
        Map<String, Object> l = new LinkedHashMap<>();
        l.put("ciclo", t(e.getEnvioGPS_CICLO()));
        l.put("municipio", t(e.getEnvioGPS_MUNUNICIPIO()));
        l.put("seccion", t(e.getEnvioGPS_SECCION()));
        l.put("departamento", t(e.getEnvioGPS_DEPARTAMENTO()));
        l.put("anno", t(e.getEnvioGPS_ANNO()));
        l.put("mes", t(e.getEnvioGPS_MES()));
        l.put("cod_cuenta", codCuenta(e.getEnvioGPS_CUENTA(), e.getEnvioGPS_DIGITOCHEQUEO()));
        l.put("nro_contador", t(e.getEnvioGPS_NROCONTADOR()));
        l.put("id_contador", t(e.getEnvioGPS_IDCONTADOR()));
        l.put("id_registrador", t(e.getEnvioGPS_IDREGISTRO()));
        l.put("indicador", indicador(e.getEnvioGPS_NROCONTADORDB()));
        l.put("lectura", t(e.getEnvioGPS_LECTURATOMADA()));
        l.put("causa_no_lectura", t(e.getEnvioGPS_CAUSADENOLECTURA()));
        l.put("desc_anomalia", t(e.getEnvioGPS_DESCANOMALIA()));
        l.put("comentario", t(e.getEnvioGPS_COMENTARIO()));
        l.put("desc_comentario", t(e.getEnvioGPS_DESCCOMENTARIO()));
        l.put("fecha_lectura", t(e.getEnvioGPS_FECHAYHORALECTURA()));
        l.put("cod_lector", t(e.getEnvioGPS_CODLECTOR()));
        l.put("primer_medidor", t(e.getEnvioGPS_PRIMERMEDIDOR()));
        l.put("hora_impresion", t(e.getEnvioGPS_HORAIMPRESION()));
        l.put("nombre_archivo", t(e.getEnvioGPS_NOMBREARCHIVO()));
        l.put("terminal", t(e.getEnvioGPS_TERMINAL()));
        l.put("indicador_novedad", t(e.getEnvioGPS_INDICADORNOVEDAD()));
        l.put("ind_cod_barras", t(e.getEnvioGPS_INDCODBARRAS()));
        l.put("intentos", t(e.getEnvioGPS_INTENTOS()));
        l.put("critica_pda", t(e.getEnvioGPS_CRITICAPDA()));
        l.put("valor_facturado", t(e.getEnvioGPS_VALORFACTURADO()));
        l.put("consumo_facturado", t(e.getEnvioGPS_CONSUMOFACTURADO()));
        l.put("subsidio_contribucion", t(e.getEnvioGPS_SUBCONTRIBUCION()));
        l.put("consumo1", t(e.getEnvioGPS_CONSUMO1()));
        l.put("consumo2", t(e.getEnvioGPS_CONSUMO2()));
        l.put("consumo3", t(e.getEnvioGPS_CONSUMO3()));
        l.put("nro_conceptos", t(e.getEnvioGPS_NROCONCEPTOS()));
        l.put("ind_facturacion", t(e.getEnvioGPS_INDFACTURACION()));
        l.put("nro_factura", t(e.getEnvioGPS_NROFACTURA()));
        l.put("fecha_vence", t(e.getEnvioGPS_FECHAVENCE()));
        l.put("fecha_corte", t(e.getEnvioGPS_FECHACORTE()));
        l.put("lectura_corregida1", t(e.getEnvioGPS_LECTURAMODIFICADA1()));
        l.put("lectura_corregida2", t(e.getEnvioGPS_LECTURAMODIFICADA2()));
        l.put("digitos", t(e.getEnvioGPS_DIGITOS()));
        l.put("critica_siec", t(e.getEnvioGPS_CRITIASIEC()));
        l.put("cod_novedad", t(e.getEnvioGPS_CODNOVEDAD()));
        l.put("lectura_anterior", t(e.getEnvioGPS_LECTURAANTERIOR()));
        l.put("marca_contador", t(e.getEnvioGPS_MARCACONTADOR()));
        l.put("ultimo_medidor_leido", t(e.getEnvioGPS_ULTIMOMEDIDORLEIDO()));
        l.put("marca_leido", t(e.getEnvioGPS_MARCALEIDO()));
        l.put("observacion", t(e.getEnvioGPS_OBSERVACION1()));
        l.put("clase_servicio", t(e.getEnvioGPS_CLASESERVICIO()));
        l.put("numero_medidores", t(e.getEnvioGPS_NMEDIDORES()));
        l.put("numero_registradores", t(e.getEnvioGPS_NREGISTRADORES()));
        l.put("valor_aseo", t(e.getEnvioGPS_VALORFACTURADOASEO()));

        Map<String, Object> gps = new LinkedHashMap<>();
        gps.put("longitud", t(e.getEnvioGPS_LONGITUD()));
        gps.put("latitud", t(e.getEnvioGPS_LATITUD()));
        gps.put("satelites", t(e.getEnvioGPS_NROSATELITES()));
        gps.put("distancia", t(e.getEnvioGPS_DISTANCIA()));
        gps.put("altitud", t(e.getEnvioGPS_ALTITUDSATELITE()));
        gps.put("fecha", t(e.getEnvioGPS_FECHAHORASATELITE()));
        l.put("gps", gps);
        return l;
    }

    /** Un concepto desde el registro de CO_COBRO.SDA ya leído (lectura_TablaCobrosRealizados). */
    public static Map<String, Object> concepto(TablaCobrosRealizados c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("cuenta", t(c.gettablaCobrosRealizados_NROCUENTACLIENTE()));
        m.put("concepto", t(c.gettablaCobrosRealizados_CONCEPTODECOBRO()));
        m.put("indicador_actividad", t(c.gettablaCobrosRealizados_INDICADORACTIVIDAD()));
        m.put("valor", t(c.gettablaCobrosRealizados_VALOR()));
        m.put("saldo_pendiente", t(c.gettablaCobrosRealizados_SALDOPENDIENTE()));
        m.put("cuotas_pendientes", t(c.gettablaCobrosRealizados_CUOTASPENDIENTES()));
        m.put("id_concepto", t(c.gettablaCobrosRealizados_IDCONCEPTO()));
        m.put("id_convenio", t(c.gettablaCobrosRealizados_IDCONVENIO()));
        m.put("primer_convenio", t(c.gettablaCobrosRealizados_PRIMERCONVENIO()));
        m.put("nro_convenios", t(c.gettablaCobrosRealizados_NROCONVENIOS()));
        m.put("porcentaje", t(c.gettablaCobrosRealizados_PORCENTAJE()));
        m.put("rango_minimo", t(c.gettablaCobrosRealizados_RANGOMINIMO()));
        m.put("rango_maximo", t(c.gettablaCobrosRealizados_RANGOMAXIMO()));
        m.put("periodo_facturado", t(c.gettablaCobrosRealizados_PERIODOFACTURADO()));
        return m;
    }

    private static String t(String s) {
        return s == null ? "" : s.trim();
    }
}
