package com.gselectroCaqueta.impresion;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Todo lo que una factura necesita para imprimirse, ya leído de las tablas de la móvil
 * y liquidado. Es un objeto de transferencia: campos públicos, sin lógica. Quien lo llena
 * es {@link AdaptadorMovil}; quien lo consume es {@link ContextoFactura}.
 * <p>
 * Convenciones: fechas como texto en cualquiera de los formatos que tolera
 * {@link Formatos#descomponer} (de preferencia {@code yyyyMMdd}); dinero en pesos como
 * {@code long}; los valores de concepto llevan signo (un descuento es negativo).
 * Los números de caída del Excel (columna de {@code lm_suscriptor_ru}) se guardan además
 * crudos en {@link #caidas} para que la plantilla pueda pedir {@code {n}} directamente.
 */
public final class DatosFactura {

    // ---- identificación
    public String cuenta = "";
    public String documentoEquivalente = "";   // NMROFCTRA (14)
    public int anio;
    public int mes;
    /** Mes tal como viene en CLIENTE.TXT ("6" u "06"): se usa en el nombre del .LOG para no romper la reimpresión. */
    public String mesArchivo = "";
    public String nombre = "";
    public String direccion = "";
    public String municipio = "";

    // ---- período y lectura
    public String fechaLecturaAnterior = "";   // MEDIDOR.Fechalectanterior
    public String fechaLecturaTomada = "";     // salida FECHALECTURA
    public String fechaHoraGeneracion = "";    // momento de impresión, "dd/MM/yyyy HH:mm:ss"
    public int mesesPeriodo = 1;               // 1 mensual, 2 bimestral, 3 trimestral
    public String tipoLectura = "";            // descripción de la causal: "Toma Exitosa"

    // ---- histórico (índice 0 = CNSMOANTRIOR1, el más reciente)
    public final List<PeriodoConsumo> anteriores = new ArrayList<>();
    public int consumoActual;                  // consumo liquidado (activa)
    public int promedio;                       // MDDOR1PRMDIO (45)

    // ---- medidores
    public Medidor activa = new Medidor();
    public Medidor reactiva;                   // null si no hay

    // ---- datos técnicos
    public String nivelTension = "";
    public String circuito = "";
    public String nodo = "";
    public String grupo = "";
    public String estrato = "";
    public String claseServicio = "";
    public String propiedadActivos = "";
    public String iua = "";                    // NMBRENDO (32)
    public String cargaInstalada = "";
    public String ruta = "";

    // ---- tarifa de energía para la tabla de liquidación
    public double valorKwh;                    // MDDOR1VLORKWH (50)
    public double porcentajeSubsidio;          // MDDOR1SBSDIOCNTRBCION (49), negativo = subsidio
    public int kwhSubsistencia;                // LS_CONS_SUBS de la desviación, por mes

    // ---- información de interés
    public String mensaje = "";                // MSGE (218)
    public String codigoMensaje = "";          // CDGOMNSJE (18): "15087A" municipio DANE + tipo A/T, clave de MENSAJES.TXT

    // ---- costos unitarios (89..96)
    public String costoG = "", costoT = "", costoPR = "", costoD = "", costoR = "", costoCV = "", costoCF = "", costoCU = "";
    /** "Costo diario" de la factura oficial. null = EBSA no ha definido la fuente ni la fórmula (se imprime "???"). */
    public String costoDiario;

    // ---- indicadores de calidad (una entrada por MES_n con dato)
    public final List<Calidad> calidad = new ArrayList<>();

    // ---- FOES
    public String foesConsumo = "", foesValorUnitario = "", foesTotal = "", foesFactura = "";

    // ---- información comercial
    public String financiacionValor = "", financiacionCuotasPendientes = "", ultimoPagoValor = "", ultimoPagoFecha = "";

    // ---- conceptos liquidados (energía, cartera, externos; sin aseo)
    public final List<Concepto> conceptos = new ArrayList<>();
    public long totalBaseIva;
    public long iva;

    // ---- aseo (factura larga). 0 y vacío en la corta.
    public boolean tieneAseo;
    public long totalAseo;

    // ---- fechas de pago
    public String fechaPagoOportuno = "";
    public String fechaSuspension = "";

    // ---- pie
    public String version = "";
    public String lector = "";

    /** Valores crudos por número de caída del Excel, para {@code {n}} en la plantilla. */
    public final Map<Integer, String> caidas = new LinkedHashMap<>();

    public static final class PeriodoConsumo {
        public final int anio;
        public final int mes;
        public final int consumo;

        public PeriodoConsumo(int anio, int mes, int consumo) {
            this.anio = anio;
            this.mes = mes;
            this.consumo = consumo;
        }
    }

    public static final class Medidor {
        public String serie = "";
        public String lecturaAnterior = "";
        public String lecturaActual = "";
        public String factor = "";
        public int consumo;
        /** MDDOR1CNSMOACMLDO (58). null = la fuente no lo trae todavía (se imprime "???"). */
        public Integer prefacturado;
    }

    public static final class Calidad {
        public String mes = "", diu = "", dium = "", diug = "", fiu = "", fium = "", fiug = "";
    }

    public static final class Concepto {
        public final int codigo;
        public final String descripcion;
        public final long valor;               // con signo
        /** 'P' período, 'C' cartera, 'E' externo; '\0' si el catálogo no lo trae. */
        public final char bloque;

        public Concepto(int codigo, String descripcion, long valor, char bloque) {
            this.codigo = codigo;
            this.descripcion = descripcion == null ? "" : descripcion;
            this.valor = valor;
            this.bloque = bloque;
        }
    }
}
