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
    /** Marca de aseo que pone quien llena el DTO (CUENTA_ASEO, indicador A/Y). Ver {@link #hayAseo()}. */
    public boolean tieneAseo;
    /** Total de aseo ya acumulado por quien llena el DTO; se usa solo si no hay conceptos en el bloque 4. */
    public long totalAseo;

    /** Avisos de lectura para la bitácora (p. ej. cobro y catálogo no coinciden en el bloque). No detienen la impresión. */
    public final List<String> avisos = new ArrayList<>();

    /** true si la cuenta lleva aseo: por la marca o porque algún cobro con valor vino en el bloque 4. */
    public boolean hayAseo() {
        if (tieneAseo) return true;
        for (Concepto c : conceptos) if (c.bloque == 4 && c.valor != 0) return true;
        return false;
    }

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
        /**
         * Bloque de la factura según el cobro ({@code CO_COBRO.SDA} NROCONVENIOS, reutilizado para esto):
         * 1 detalle de la factura, 2 gestión cartera EBSA, 3 conceptos externos, 4 aseo; 0 si no viene.
         */
        public final int bloque;
        /** Bloque según el catálogo ({@code DES_CONC.TXT} DATO1); 0 si no viene. Respaldo y diagnóstico. */
        public final int bloqueCatalogo;
        /** Orden de impresión dentro del bloque ({@code DES_CONC.TXT} DATO2); 0 = sin orden (va al final, en orden de archivo). */
        public final int orden;

        public Concepto(int codigo, String descripcion, long valor, int bloque, int bloqueCatalogo, int orden) {
            this.codigo = codigo;
            this.descripcion = descripcion == null ? "" : descripcion;
            this.valor = valor;
            this.bloque = bloque;
            this.bloqueCatalogo = bloqueCatalogo;
            this.orden = orden;
        }

        /** Compatibilidad: bloque como letra del catálogo viejo (P/C/E/A) o '\0'. */
        public Concepto(int codigo, String descripcion, long valor, char letraBloque) {
            this(codigo, descripcion, valor, 0, bloqueDe(String.valueOf(letraBloque)), 0);
        }

        /**
         * Interpreta un indicador de bloque como viene en los planos: dígito 1..4, o letra
         * P/C/E (período, cartera, externo) y A/Y (aseo) del esquema anterior. Otra cosa → 0.
         */
        public static int bloqueDe(String s) {
            if (s == null) return 0;
            String v = s.trim().toUpperCase();
            if (v.isEmpty()) return 0;
            char c = v.charAt(0);
            switch (c) {
                case '1': case 'P': return 1;
                case '2': case 'C': return 2;
                case '3': case 'E': return 3;
                case '4': case 'A': case 'Y': return 4;
                default: return 0;
            }
        }
    }
}
