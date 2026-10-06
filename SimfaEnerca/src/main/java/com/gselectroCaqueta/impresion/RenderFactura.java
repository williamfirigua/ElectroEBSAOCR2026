package com.gselectroCaqueta.impresion;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Fachada: elige la plantilla, arma el contexto, renderiza y escribe el {@code .LOG} que el
 * servicio Bluetooth existente ({@code procedimientoDeImpresionPagina}) ya sabe enviar.
 * <p>
 * Reemplaza a {@code crearArchivoImpresion1/420} + {@code generarArchivoTextoImpresion} +
 * {@code procedimientoUnionArchivos}. El nombre del archivo se conserva
 * ({@code LBLS/<cuenta>_<año>_<mes>.LOG}) para que el índice {@code L<ciclo><mun>…} y la
 * reimpresión sigan funcionando sin cambios.
 * <p>
 * La salida se escribe en ISO-8859-1 (un byte por carácter, como hacía {@code writeBytes}):
 * el archivo debe enviarse a la impresora byte a byte, sin pasarlo por un lector de texto.
 */
public final class RenderFactura {

    public static final String PLANTILLA_CORTA = "FORMATO_CORTA.CPCL";
    public static final String PLANTILLA_LARGA = "FORMATO_LARGA.CPCL";
    /** Latin-1: cada char es un byte. Así el FNC1 (U+0086) sale como 0x86 y las tildes como en CP1252. */
    private static final Charset LATIN1 = Charset.forName("ISO-8859-1");

    private final File dirPlantillas;
    private final File dirSalida;
    private final BloquesConceptos.Clasificador clasificador;

    public RenderFactura(File dirPlantillas, File dirSalida) {
        this(dirPlantillas, dirSalida, new BloquesConceptos.ClasificadorPorCodigo());
    }

    public RenderFactura(File dirPlantillas, File dirSalida, BloquesConceptos.Clasificador clasificador) {
        this.dirPlantillas = dirPlantillas;
        this.dirSalida = dirSalida;
        this.clasificador = clasificador;
    }

    public static final class Salida {
        public final File archivo;
        public final String cpcl;
        public final Set<String> faltantes;
        public final String plantilla;
        /** true si la cuenta pedía la plantilla larga y se imprimió con la corta por no existir aquella. */
        public final boolean degradada;

        Salida(File archivo, String cpcl, Set<String> faltantes, String plantilla, boolean degradada) {
            this.archivo = archivo;
            this.cpcl = cpcl;
            this.faltantes = Collections.unmodifiableSet(faltantes);
            this.plantilla = plantilla;
            this.degradada = degradada;
        }
    }

    /**
     * Renderiza y escribe el .LOG. Las claves faltantes no detienen la impresión; se devuelven
     * para la bitácora. Si la plantilla larga no está instalada, se usa la corta y se marca
     * {@link Salida#degradada}: en terreno es preferible entregar la factura de energía con el
     * cupón correcto (el total del cupón sí incluye el aseo) a no entregar nada.
     */
    public Salida generar(DatosFactura d) throws IOException {
        return generar(d, AjustesImpresora.ninguno());
    }

    /**
     * Igual que {@link #generar(DatosFactura)} aplicando los ajustes de la impresora del
     * terminal ({@code ValoresFormato.log}). Si hay una variante de plantilla para el modelo
     * configurado ({@code FORMATO_CORTA_RW420.CPCL}) se usa esa; si no, la plantilla base
     * con el corrimiento X/Y, que es lo que hoy distingue una impresora de otra.
     */
    public Salida generar(DatosFactura d, AjustesImpresora ajustes) throws IOException {
        AjustesImpresora aj = ajustes == null ? AjustesImpresora.ninguno() : ajustes;
        String nombre = nombrePlantilla(d);
        File archivoPlantilla = plantillaParaModelo(nombre, aj.modelo());
        boolean degradada = false;
        if (!archivoPlantilla.isFile() && PLANTILLA_LARGA.equals(nombre)) {
            nombre = PLANTILLA_CORTA;
            archivoPlantilla = plantillaParaModelo(nombre, aj.modelo());
            degradada = true;
        }
        if (!archivoPlantilla.isFile()) {
            throw new IOException("No existe la plantilla de impresion: " + archivoPlantilla.getAbsolutePath());
        }
        Plantilla pl = Plantilla.cargar(archivoPlantilla);
        Map<String, String> ctx = ContextoFactura.construir(d, pl.parametros(), clasificador);
        Plantilla.Resultado r = pl.renderizar(ctx, aj);

        File log = archivoLog(dirSalida, d);
        if (!dirSalida.isDirectory() && !dirSalida.mkdirs()) {
            throw new IOException("No se pudo crear " + dirSalida.getAbsolutePath());
        }
        try (OutputStream out = new FileOutputStream(log)) {
            out.write(r.cpcl().getBytes(LATIN1));
        }
        return new Salida(log, r.cpcl(), r.faltantes(), archivoPlantilla.getName(), degradada);
    }

    public static String nombrePlantilla(DatosFactura d) {
        return d.tieneAseo ? PLANTILLA_LARGA : PLANTILLA_CORTA;
    }

    /** {@code FORMATO_CORTA.CPCL} + modelo RW420 → {@code FORMATO_CORTA_RW420.CPCL} si existe; si no, la base. */
    private File plantillaParaModelo(String nombreBase, AjustesImpresora.Modelo modelo) {
        File base = new File(dirPlantillas, nombreBase);
        if (modelo == null || modelo.sufijoPlantilla.isEmpty()) return base;
        int punto = nombreBase.lastIndexOf('.');
        String variante = punto < 0
                ? nombreBase + modelo.sufijoPlantilla
                : nombreBase.substring(0, punto) + modelo.sufijoPlantilla + nombreBase.substring(punto);
        File f = new File(dirPlantillas, variante);
        return f.isFile() ? f : base;
    }

    public static File archivoLog(File dir, DatosFactura d) {
        String mes = d.mesArchivo == null || d.mesArchivo.trim().isEmpty() ? String.valueOf(d.mes) : d.mesArchivo.trim();
        return new File(dir, d.cuenta.trim() + "_" + d.anio + "_" + mes + ".LOG");
    }
}
