package com.gselectroCaqueta.impresion;

import com.gselectroCaqueta.tablas.TablaClienteSalida;
import com.gselectroCaqueta.tablas.TablaCobrosRealizados;
import com.gselectroCaqueta.tablas.TablaDescripcionConceptos;
import com.gselectroCaqueta.tablas.TablaEntradaClientes;
import com.gselectroCaqueta.tablas.TablaMedidorEntrada;
import com.gselectroCaqueta.tablas.TablaMensajes;
import com.gselectroCaqueta.tablas.TablaMunicipios;
import com.gselectroCaqueta.tablas.TablaRegistroDeEntrada;
import com.gselectroCaqueta.tablas.TablaRegistroSalida;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Lee las tablas de la móvil y arma un {@link DatosFactura}. Es la única clase del paquete
 * acoplada a {@code com.gselectroCaqueta.tablas}.
 * <p>
 * Recorre medidores, registros y cobros con las mismas tablas compartidas que usa
 * {@code MenuDeLiquidacion}; como todas ellas guardan "el registro actual", al terminar las
 * tablas de medidor/registro/cobros quedan posicionadas en el último leído, igual que hoy
 * tras {@code generarArchivoTextoImpresion()}. La tabla de clientes de entrada y de salida
 * deben venir ya posicionadas en la cuenta a imprimir.
 */
public final class AdaptadorMovil {

    private final TablaEntradaClientes cliente;
    private final TablaClienteSalida clienteSalida;
    private final TablaMedidorEntrada medidor;
    private final TablaRegistroDeEntrada registro;
    private final TablaRegistroSalida registroSalida;
    private final TablaCobrosRealizados cobros;
    private final TablaDescripcionConceptos descripciones;
    private final TablaMunicipios municipios;
    /** MENSAJES.TXT (lm_lista CDGOMNSJE): mensaje por municipio+tipo. Opcional: null si no está abierta. */
    private final TablaMensajes mensajes;

    public AdaptadorMovil(TablaEntradaClientes cliente, TablaClienteSalida clienteSalida,
                          TablaMedidorEntrada medidor, TablaRegistroDeEntrada registro,
                          TablaRegistroSalida registroSalida, TablaCobrosRealizados cobros,
                          TablaDescripcionConceptos descripciones, TablaMunicipios municipios) {
        this(cliente, clienteSalida, medidor, registro, registroSalida, cobros, descripciones, municipios, null);
    }

    /**
     * @param mensajes {@code TablaMensajes} ya abierta sobre {@code DATOSDEENTRADA/MENSAJES.TXT}, o null.
     *                 Con ella, el mensaje de interés se resuelve por {@code CDGOMNSJE} (caída 18,
     *                 "15087A" = municipio DANE + tipo A/T) cuando {@code Informacionadicional} viene vacío.
     */
    public AdaptadorMovil(TablaEntradaClientes cliente, TablaClienteSalida clienteSalida,
                          TablaMedidorEntrada medidor, TablaRegistroDeEntrada registro,
                          TablaRegistroSalida registroSalida, TablaCobrosRealizados cobros,
                          TablaDescripcionConceptos descripciones, TablaMunicipios municipios,
                          TablaMensajes mensajes) {
        this.cliente = cliente;
        this.clienteSalida = clienteSalida;
        this.medidor = medidor;
        this.registro = registro;
        this.registroSalida = registroSalida;
        this.cobros = cobros;
        this.descripciones = descripciones;
        this.municipios = municipios;
        this.mensajes = mensajes;
    }

    /**
     * @param tipoLectura descripción de la causal ya resuelta por la móvil ({@code causadesc}):
     *                    "Toma Exitosa", "Inmueble Sin Servicio"…
     * @param version     línea de versión del pie ("Version 26.01.14.A-11 Lect.1… - ZQ-521")
     */
    public DatosFactura leer(String tipoLectura, String version) {
        DatosFactura d = new DatosFactura();

        d.cuenta = t(cliente.gettablaEntradaClientes_Cuenta());
        d.documentoEquivalente = t(cliente.gettablaEntradaClientes_Nrofactura());
        d.anio = entero(cliente.gettablaEntradaClientes_anio());
        d.mes = entero(cliente.gettablaEntradaClientes_mes());
        d.mesArchivo = t(cliente.gettablaEntradaClientes_mes());
        d.nombre = t(cliente.gettablaEntradaClientes_Nombre());
        d.direccion = t(cliente.gettablaEntradaClientes_Direccion());
        d.municipio = nombreMunicipio(cliente.gettablaEntradaClientes_Municipio());
        d.mesesPeriodo = Math.max(1, Math.min(3, entero(cliente.gettablaEntradaClientes_Bimestral())));
        d.tipoLectura = tipoLectura == null ? "" : tipoLectura.trim();
        d.fechaHoraGeneracion = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ROOT).format(new Date());
        d.version = version == null ? "" : version;
        d.lector = t(clienteSalida.gettablaClienteSalida_LECTOR());

        leerMedidores(d);
        leerDatosTecnicos(d);
        leerTarifaYCostos(d);
        leerCalidad(d);
        leerFoesYComercial(d);
        leerConceptos(d);
        leerFechasPago(d);
        llenarCaidas(d);
        return d;
    }

    // ------------------------------------------------------------------ medidores y lecturas

    private void leerMedidores(DatosFactura d) {
        int nMedidores = Math.max(1, entero(cliente.gettablaEntradaClientes_Nrodemedidores()));
        int primerMedidor = entero(cliente.gettablaEntradaClientes_primermedidor());
        boolean activaLeida = false, reactivaLeida = false;

        for (int i = 0; i < nMedidores && !(activaLeida && reactivaLeida); i++) {
            medidor.lectura_TablaMedidorEntrada(primerMedidor + i);
            int nRegistros = Math.max(1, entero(medidor.gettablaMedidorEntrada_Nroregistros()));
            int primerRegistro = entero(medidor.gettablaMedidorEntrada_primerregistro());
            String serie = t(medidor.gettablaMedidorEntrada_NUMero());
            String fechaAnterior = t(medidor.gettablaMedidorEntrada_Fechalectanterior());

            for (int j = 0; j < nRegistros; j++) {
                registro.lectura_TablaRegistroDeEntrada(primerRegistro + j);
                registroSalida.lectura_TablaRegistroSalida(primerRegistro + j);
                boolean reactiva = "R".equals(t(registro.gettablaRegistroDeEntrada_tipoenergia()));

                DatosFactura.Medidor m = new DatosFactura.Medidor();
                m.serie = serie;
                m.lecturaAnterior = t(registro.gettablaRegistroDeEntrada_Lecturaanterior());
                m.lecturaActual = t(registroSalida.gettablaRegistroSalida_LECTURATOMADA());
                m.factor = t(registro.gettablaRegistroDeEntrada_Factormultipicacion());
                // Prefacturado (caída 58, MDDOR1CNSMOACMLDO): el exportador lo deja en "consumoreal"
                // de REGISTRO.TXT. Vacío o no numérico → null → la plantilla imprime "???".
                m.prefacturado = enteroOpcional(registro.gettablaRegistroDeEntrada_consumoreal());

                if (reactiva && !reactivaLeida) {
                    m.consumo = (int) Math.round(Formatos.numero(clienteSalida.gettablaClienteSalida_CONSUMO3()));
                    d.reactiva = m;
                    reactivaLeida = true;
                } else if (!reactiva && !activaLeida) {
                    m.consumo = (int) Math.round(Formatos.numero(clienteSalida.gettablaClienteSalida_CONSUMO2()));
                    d.activa = m;
                    // MEDIDOR.TXT CUENTA_ASEO (lm_aseo_maestro): la cuenta tiene aseo aunque hoy no vengan cobros del bloque 4
                    if (!t(medidor.gettablaMedidorEntrada_CUENTA_ASEO()).isEmpty()) d.tieneAseo = true;
                    d.consumoActual = m.consumo;
                    d.fechaLecturaAnterior = fechaAnterior;
                    d.fechaLecturaTomada = t(registroSalida.gettablaRegistroSalida_FECHALECTURA());
                    d.promedio = (int) Math.round(Formatos.numero(
                            Formatos.esVacio(registro.gettablaRegistroDeEntrada_PRMDIO())
                                    ? registro.gettablaRegistroDeEntrada_Consumopromediocliente()
                                    : registro.gettablaRegistroDeEntrada_PRMDIO()));
                    d.kwhSubsistencia = (int) Math.round(Formatos.numero(registro.gettablaRegistroDeEntrada_LS_CONS_SUBS()));
                    leerHistorico(d);
                    activaLeida = true;
                }
            }
        }
    }

    /** periodo1/ano1/consumo1 es el más reciente (CNSMOANTRIOR1); se conserva ese orden. */
    private void leerHistorico(DatosFactura d) {
        d.anteriores.clear();
        String[][] filas = {
                {registro.gettablaRegistroDeEntrada_ano1(), registro.gettablaRegistroDeEntrada_periodo1(), registro.gettablaRegistroDeEntrada_consumo1()},
                {registro.gettablaRegistroDeEntrada_ano2(), registro.gettablaRegistroDeEntrada_periodo2(), registro.gettablaRegistroDeEntrada_consumo2()},
                {registro.gettablaRegistroDeEntrada_ano3(), registro.gettablaRegistroDeEntrada_periodo3(), registro.gettablaRegistroDeEntrada_consumo3()},
                {registro.gettablaRegistroDeEntrada_ano4(), registro.gettablaRegistroDeEntrada_periodo4(), registro.gettablaRegistroDeEntrada_consumo4()},
                {registro.gettablaRegistroDeEntrada_ano5(), registro.gettablaRegistroDeEntrada_periodo5(), registro.gettablaRegistroDeEntrada_consumo5()},
                {registro.gettablaRegistroDeEntrada_ano6(), registro.gettablaRegistroDeEntrada_periodo6(), registro.gettablaRegistroDeEntrada_consumo6()},
        };
        for (String[] f : filas) {
            // En el plano de EBSA el campo "ano" trae el mes ("03/") y "periodo" trae el año ("26"):
            // se decide por el valor, no por el nombre, para que sirva en los dos sentidos.
            int a = entero(f[0].replace("/", "")), p = entero(f[1].replace("/", ""));
            if (a == 0 || p == 0) continue;
            int anio = a > 12 ? a : p;
            int mes = a > 12 ? p : a;
            if (mes < 1 || mes > 12) continue;
            d.anteriores.add(new DatosFactura.PeriodoConsumo(anio < 100 ? 2000 + anio : anio, mes, (int) Math.round(Formatos.numero(f[2]))));
        }
    }

    // ------------------------------------------------------------------ bloques de datos

    private void leerDatosTecnicos(DatosFactura d) {
        d.nivelTension = t(cliente.gettablaEntradaClientes_Niveldetension());
        d.nodo = t(cliente.gettablaEntradaClientes_Nodoconexion());
        d.circuito = t(cliente.gettablaEntradaClientes_Nodocircuito());
        d.iua = t(cliente.gettablaEntradaClientes_nombrecircuito());
        d.grupo = t(cliente.gettablaEntradaClientes_grupo());
        d.estrato = t(cliente.gettablaEntradaClientes_Estrato());
        d.claseServicio = t(cliente.gettablaEntradaClientes_Clasedeservicio());
        d.cargaInstalada = t(cliente.gettablaEntradaClientes_cargainstalada());
        d.ruta = t(cliente.gettablaEntradaClientes_Ruta());
        d.propiedadActivos = "";   // sin fuente en esta versión: imprime "???"
        d.mensaje = t(cliente.gettablaEntradaClientes_Informacionadicional());
        if (d.mensaje.isEmpty()) {
            d.codigoMensaje = t(cliente.gettablaEntradaClientes_CDGOMNSJE());
            d.mensaje = mensajePorCodigo(d.codigoMensaje);
        }
    }

    /**
     * Mensaje de interés por código {@code CDGOMNSJE} en MENSAJES.TXT: el central ya decidió el tipo
     * (A = al día, T = en mora) por cuenta, así que aquí solo se busca. Código vacío, tabla ausente o
     * código no encontrado → "" (la plantilla omite las líneas; queda en la bitácora por {@code codigoMensaje}).
     */
    private String mensajePorCodigo(String codigo) {
        if (mensajes == null || codigo.isEmpty()) return "";
        mensajes.buscarbinario_TablaMensajes(codigo);
        if (mensajes.encontro_TablaMensajes == 0) return "";
        // buscarbinario deja cargado el registro que comparó; se verifica que sea el pedido
        if (!codigo.equals(t(mensajes.gettablaMensajes_CODIGO()))) return "";
        return t(mensajes.gettablaMensajes_DESCRIPCION());
    }

    private void leerTarifaYCostos(DatosFactura d) {
        d.valorKwh = Formatos.numero(cliente.gettablaEntradaClientes_MDDOR1VLORKWH());
        d.porcentajeSubsidio = Formatos.numero(cliente.gettablaEntradaClientes_MDDOR1SBSDIOCNTRBCION());
        d.costoG = t(cliente.gettablaEntradaClientes_CSTOG());
        d.costoT = t(cliente.gettablaEntradaClientes_CSTOT());
        d.costoPR = t(cliente.gettablaEntradaClientes_CSTOPR());
        d.costoD = t(cliente.gettablaEntradaClientes_CSTOD());
        d.costoR = t(cliente.gettablaEntradaClientes_CSTOR());
        d.costoCV = t(cliente.gettablaEntradaClientes_CSTOCV());
        d.costoCF = t(cliente.gettablaEntradaClientes_CSTOCF());
        d.costoCU = t(cliente.gettablaEntradaClientes_CU());
        d.totalBaseIva = Math.round(Formatos.numero(cliente.gettablaEntradaClientes_TTBSIVA()));
        d.iva = 0;   // sin regla definida por EBSA
        d.costoDiario = null;   // sin fuente en esta versión: imprime "???"
    }

    private void leerCalidad(DatosFactura d) {
        agregarCalidad(d, cliente.gettablaEntradaClientes_MES_1(), cliente.gettablaEntradaClientes_DIU_1(), cliente.gettablaEntradaClientes_DIUM_1(),
                cliente.gettablaEntradaClientes_DIUG_1(), cliente.gettablaEntradaClientes_FIU_1(), cliente.gettablaEntradaClientes_FIUM_1(), cliente.gettablaEntradaClientes_FIUG_1());
        agregarCalidad(d, cliente.gettablaEntradaClientes_MES_2(), cliente.gettablaEntradaClientes_DIU_2(), cliente.gettablaEntradaClientes_DIUM_2(),
                cliente.gettablaEntradaClientes_DIUG_2(), cliente.gettablaEntradaClientes_FIU_2(), cliente.gettablaEntradaClientes_FIUM_2(), cliente.gettablaEntradaClientes_FIUG_2());
        agregarCalidad(d, cliente.gettablaEntradaClientes_MES_3(), cliente.gettablaEntradaClientes_DIU_3(), cliente.gettablaEntradaClientes_DIUM_3(),
                cliente.gettablaEntradaClientes_DIUG_3(), cliente.gettablaEntradaClientes_FIU_3(), cliente.gettablaEntradaClientes_FIUM_3(), cliente.gettablaEntradaClientes_FIUG_3());
    }

    private static void agregarCalidad(DatosFactura d, String mes, String diu, String dium, String diug, String fiu, String fium, String fiug) {
        if (Formatos.esVacio(mes)) return;
        DatosFactura.Calidad q = new DatosFactura.Calidad();
        q.mes = t(mes); q.diu = t(diu); q.dium = t(dium); q.diug = t(diug); q.fiu = t(fiu); q.fium = t(fium); q.fiug = t(fiug);
        d.calidad.add(q);
    }

    private void leerFoesYComercial(DatosFactura d) {
        d.foesConsumo = t(cliente.gettablaEntradaClientes_consumofoes());
        d.foesValorUnitario = t(cliente.gettablaEntradaClientes_VLORKWFOES());
        d.foesTotal = t(cliente.gettablaEntradaClientes_valorfoes());
        d.foesFactura = t(cliente.gettablaEntradaClientes_facturafoes());
        d.financiacionValor = t(cliente.gettablaEntradaClientes_FNNCCIONVLOR());
        d.financiacionCuotasPendientes = t(cliente.gettablaEntradaClientes_FNNCCIONCTASPNDNTES());
        d.ultimoPagoValor = t(cliente.gettablaEntradaClientes_Valorultimopago());
        d.ultimoPagoFecha = t(cliente.gettablaEntradaClientes_Fechaultimopago());
    }

    /**
     * Misma lectura que {@code generarTextosConceptos}: INDICADORACTIVIDAD 'D' resta, 'X' es
     * informativo (no se imprime ni suma). El bloque de la factura (1 detalle, 2 cartera, 3 externos,
     * 4 aseo) viene en NROCONVENIOS de CO_COBRO.SDA, que el exportador reutiliza para eso; el catálogo
     * DES_CONC.TXT aporta la descripción (por el puntero IDCONCEPTO), su propio bloque (DATO1, respaldo
     * y diagnóstico) y el orden de impresión dentro del bloque (DATO2). 'A'/'Y' en el indicador es la
     * marca de aseo del esquema anterior: se respeta como bloque 4 ('Y' resta).
     */
    private void leerConceptos(DatosFactura d) {
        int n = entero(cliente.gettablaEntradaClientes_nrocobros());
        int puntero = entero(cliente.gettablaEntradaClientes_primercobro());
        boolean catalogoAbierto = descripciones.abrir_TablaDescripcionConceptos(descripciones.getArchivo_TablaDescripcionConceptos());
        try {
            for (int k = 0; k < n; k++) {
                cobros.lectura_TablaCobrosRealizados(puntero + k);
                String ind = t(cobros.gettablaCobrosRealizados_INDICADORACTIVIDAD());
                if ("X".equals(ind)) continue;
                long valor = Math.round(Formatos.numero(cobros.gettablaCobrosRealizados_VALOR().replace("-", "")));
                boolean resta = "D".equals(ind) || "Y".equals(ind);
                int codigo = entero(cobros.gettablaCobrosRealizados_CONCEPTODECOBRO());
                int bloque = DatosFactura.Concepto.bloqueDe(cobros.gettablaCobrosRealizados_NROCONVENIOS());
                if ("A".equals(ind) || "Y".equals(ind)) bloque = 4;

                String descripcion = descripcionConcepto(codigo);
                int bloqueCatalogo = 0, orden = 0;
                if (catalogoAbierto && descripciones.encontro_TablaDescripcionConceptos > 0) {
                    bloqueCatalogo = DatosFactura.Concepto.bloqueDe(descripciones.gettablaDescripcionConceptos_DATO1());
                    Integer o = enteroOpcional(descripciones.gettablaDescripcionConceptos_DATO2());
                    orden = o == null ? 0 : o;
                }
                if (bloque == 4 || (bloque == 0 && bloqueCatalogo == 4)) d.tieneAseo = true;
                if (bloque != 0 && bloqueCatalogo != 0 && bloque != bloqueCatalogo) {
                    d.avisos.add("concepto " + codigo + " bloque cobro=" + bloque + " catalogo=" + bloqueCatalogo);
                }
                d.conceptos.add(new DatosFactura.Concepto(codigo, descripcion, resta ? -valor : valor, bloque, bloqueCatalogo, orden));
            }
        } finally {
            if (catalogoAbierto) descripciones.Cerrar_TablaDescripcionConceptos();
        }
    }

    private String descripcionConcepto(int codigo) {
        descripciones.encontro_TablaDescripcionConceptos = 0;
        int id = entero(cobros.gettablaCobrosRealizados_IDCONCEPTO());
        if (id > 0) descripciones.lectura_TablaDescripcionConceptos(id);
        if (descripciones.encontro_TablaDescripcionConceptos > 0) {
            return t(descripciones.gettablaDescripcionConceptos_DESCRIPCION());
        }
        switch (codigo) {
            case 21:  return "Valor factura periodo";
            case 607:
            case 608: return "Ajuste Decena";
            case 17:
            case 517: return "Subsidio";
            case 18:
            case 518: return "Contribucion";
            default:  return "Concepto " + codigo;
        }
    }

    private void leerFechasPago(DatosFactura d) {
        d.fechaPagoOportuno = primeroNoVacio(clienteSalida.gettablaClienteSalida_FECHAVENCE(), cliente.gettablaEntradaClientes_fechavence());
        d.fechaSuspension = primeroNoVacio(clienteSalida.gettablaClienteSalida_FECHACORTE(), cliente.gettablaEntradaClientes_fechacorte());
    }

    /**
     * Caídas que solo existen en la tabla de entrada y no tienen campo propio en DatosFactura.
     * Las demás ({11}, {16}, {42}…) las deriva ContextoFactura de los campos tipados.
     */
    private void llenarCaidas(DatosFactura d) {
        d.caidas.put(13, t(cliente.gettablaEntradaClientes_Estadodelservicio()));
        d.caidas.put(138, t(cliente.gettablaEntradaClientes_FCHAINCIOAGNDA()));
        d.caidas.put(139, t(cliente.gettablaEntradaClientes_FCHAFINAGNDA()));
        d.caidas.put(248, t(cliente.gettablaEntradaClientes_COSTO_D()));
    }

    // ------------------------------------------------------------------ utilidades

    private String nombreMunicipio(String codigo) {
        String cod = t(codigo);
        if (municipios == null || cod.isEmpty()) return cod;
        if (municipios.abrir_TablaMunicipios(municipios.getArchivo_TablaMunicipios())) {
            try {
                municipios.buscarbinario_TablaMunicipios(cod);
                String desc = municipios.gettablaMunicipios_DESCRIPCION();
                if (desc != null && !desc.trim().isEmpty()) return desc.trim();
            } finally {
                municipios.Cerrar_TablaMunicipios();
            }
        }
        return cod;
    }

    private static String primeroNoVacio(String a, String b) {
        return !Formatos.esVacio(a) ? a.trim() : (b == null ? "" : b.trim());
    }

    private static String t(String s) {
        return s == null ? "" : s.trim();
    }

    private static int entero(String s) {
        return (int) Math.round(Formatos.numero(s));
    }

    /** Como {@link #entero} pero vacío o no numérico devuelve null: "sin dato" se distingue de 0. */
    private static Integer enteroOpcional(String s) {
        if (Formatos.esVacio(s)) return null;
        try {
            return (int) Math.round(Double.parseDouble(s.trim().replace(',', '.')));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
