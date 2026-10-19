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
    public static final String PLANTILLA_DESVIACION = "NOTIFICACION_DESVIACION.CPCL";
    public static final String PLANTILLA_VISITA = "NOTIFICACION_VISITA.CPCL";
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

    /**
     * Mapa para el diseñador: mismo .LOG que {@link #generar} pero cada texto sale como su etiqueta
     * ({@code [11]}, {@code [detalle.3]}) y ninguna línea se omite. Barras y códigos usan los datos de
     * la cuenta. Se escribe como {@code <cuenta>_<año>_<mes>_MAPA.LOG} junto al .LOG normal.
     */
    public Salida generarMapa(DatosFactura d, AjustesImpresora ajustes) throws IOException {
        AjustesImpresora aj = ajustes == null ? AjustesImpresora.ninguno() : ajustes;
        String nombre = nombrePlantilla(d);
        File archivoPlantilla = plantillaParaModelo(nombre, aj.modelo());
        if (!archivoPlantilla.isFile()) archivoPlantilla = plantillaParaModelo(PLANTILLA_CORTA, aj.modelo());
        Plantilla pl = Plantilla.cargar(archivoPlantilla);
        Map<String, String> ctx = ContextoFactura.construir(d, pl.parametros(), clasificador);
        Plantilla.Resultado r = pl.renderizarMapa(ctx, aj);
        File base = archivoLog(dirSalida, d);
        File log = new File(dirSalida, base.getName().replace(".LOG", "_MAPA.LOG"));
        if (!dirSalida.isDirectory() && !dirSalida.mkdirs()) {
            throw new IOException("No se pudo crear " + dirSalida.getAbsolutePath());
        }
        try (OutputStream out = new FileOutputStream(log)) {
            out.write(r.cpcl().getBytes(LATIN1));
        }
        return new Salida(log, r.cpcl(), r.faltantes(), archivoPlantilla.getName(), false);
    }

    /**
     * Cartas que acompañan a la factura según la analítica de desviación ({@link DatosFactura#analitica}).
     * Cada plantilla declara sus códigos con {@code ;@SET carta.analitica=}; si no lo hace, vale el defecto:
     * <ul>
     *   <li>{@link #PLANTILLA_DESVIACION}: consumo alto con la visita exceptuada por regla → 002 (DV2).</li>
     *   <li>{@link #PLANTILLA_VISITA}: consumo alto que se visita y se cobra por promedio → 003 (DV3).</li>
     * </ul>
     * Gana la primera de la lista que esté instalada y tenga el código.
     */
    private static final String[][] CARTAS = {
            {PLANTILLA_DESVIACION, "002", "_DESV"},
            {PLANTILLA_VISITA, "003", "_VISITA"},
    };

    /**
     * Nombre de la plantilla de carta para un código de analítica, o null si ninguna instalada lo pide.
     * Lee solo los {@code ;@SET} de cada plantilla; se puede llamar antes de liquidar la impresión.
     */
    public String plantillaCarta(String analitica) throws IOException {
        String codigo = analitica == null ? "" : analitica.trim();
        if (codigo.isEmpty()) return null;
        for (String[] carta : CARTAS) {
            File f = new File(dirPlantillas, carta[0]);
            if (f.isFile() && Plantilla.cargar(f).parametros().cartaAnalitica(carta[1]).contains(codigo)) return carta[0];
        }
        return null;
    }

    /**
     * Carta para la analítica de la cuenta, escrita como {@code <cuenta>_<año>_<mes>_DESV.LOG} (o {@code _VISITA}).
     * null si ninguna plantilla aplica a {@link DatosFactura#analitica}.
     */
    public Salida generarCarta(DatosFactura d, AjustesImpresora ajustes) throws IOException {
        String plantilla = plantillaCarta(d.analitica);
        return plantilla == null ? null : generarCarta(d, plantilla, ajustes);
    }

    /** Carta de desviación ({@link #PLANTILLA_DESVIACION}) sin mirar la analítica: bosquejos y pruebas. null si no está instalada. */
    public Salida generarNotificacion(DatosFactura d, AjustesImpresora ajustes) throws IOException {
        return new File(dirPlantillas, PLANTILLA_DESVIACION).isFile() ? generarCarta(d, PLANTILLA_DESVIACION, ajustes) : null;
    }

    private Salida generarCarta(DatosFactura d, String plantilla, AjustesImpresora ajustes) throws IOException {
        String sufijo = "_CARTA";
        for (String[] carta : CARTAS) if (carta[0].equals(plantilla)) sufijo = carta[2];
        Plantilla pl = Plantilla.cargar(new File(dirPlantillas, plantilla));
        Map<String, String> ctx = ContextoFactura.construir(d, pl.parametros(), clasificador);
        Plantilla.Resultado r = pl.renderizar(ctx, ajustes == null ? AjustesImpresora.ninguno() : ajustes);
        File base = archivoLog(dirSalida, d);
        File log = new File(dirSalida, base.getName().replace(".LOG", sufijo + ".LOG"));
        if (!dirSalida.isDirectory() && !dirSalida.mkdirs()) {
            throw new IOException("No se pudo crear " + dirSalida.getAbsolutePath());
        }
        try (OutputStream out = new FileOutputStream(log)) {
            out.write(r.cpcl().getBytes(LATIN1));
        }
        return new Salida(log, r.cpcl(), r.faltantes(), plantilla, false);
    }

    public static String nombrePlantilla(DatosFactura d) {
        return d.hayAseo() ? PLANTILLA_LARGA : PLANTILLA_CORTA;
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
