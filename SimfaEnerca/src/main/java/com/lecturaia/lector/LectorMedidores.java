package com.lecturaia.lector;

/**
 * Lectura completa de una foto, igual a {@code lectura_ia/modelo/inferencia.py}:
 *
 * <pre>
 *   foto -> letterbox 320 -> detector.onnx -> tipo + 4 esquinas
 *        -> enderezado (perspectiva) -> recorte 64 de alto -> lector.onnx -> dígitos (entero/decimal)
 * </pre>
 *
 * Uso en Android (en un hilo de fondo, nunca en el hilo de la interfaz):
 * <pre>
 *   LectorMedidores lector = new LectorMedidores(new OrtMotor(detectorOnnx, lectorOnnx, "v20261104_1948"));
 *   Resultado r = lector.leer(BitmapImagen.desde(bitmap));
 * </pre>
 * Es seguro compartir una instancia entre hilos si el Motor lo es (OrtMotor lo es).
 */
public final class LectorMedidores implements AutoCloseable {
    public static final String[] TIPOS = {"rodillos", "agujas", "lcd", "led", "no_medidor", "ilegible"};

    private final Motor motor;
    private final double margenX;

    public LectorMedidores(Motor motor) {
        this(motor, 0.0);
    }

    /** {@code margenX}: margen a cada lado al recortar con las esquinas del detector (modelo.json: margen_x). */
    public LectorMedidores(Motor motor, double margenX) {
        this.motor = motor;
        this.margenX = margenX;
    }

    public String version() {
        return motor.version();
    }

    static boolean conLectura(String tipo) {
        return "rodillos".equals(tipo) || "agujas".equals(tipo) || "lcd".equals(tipo) || "led".equals(tipo);
    }

    /** Tipo de registrador y esquinas (normalizadas en la foto original). */
    public Deteccion detectar(Imagen foto) throws Exception {
        Procesamiento.Letterbox lb = Procesamiento.letterbox(foto, Procesamiento.LADO_DETECTOR);
        float[][] salida = motor.detectar(Procesamiento.tensorDetector(lb.imagen));
        float[] p = Decodificador.softmax(salida[0]);
        int k = Decodificador.argmax(p, 0, p.length);
        double[][] esq = Procesamiento.esquinasDesdeLetterbox(salida[1], foto.ancho, foto.alto, lb.escala,
                Procesamiento.LADO_DETECTOR);
        return new Deteccion(TIPOS[k], p[k], esq);
    }

    /** Lee un recorte ya enderezado (64 de alto). */
    public Decodificador.Lectura leerRecorte(Imagen recorte) throws Exception {
        Motor.SalidaLector s = motor.leer(Procesamiento.tensorLector(recorte));
        Decodificador.Lectura l = Decodificador.decodificar(s.logits);
        int dec = l.decimales;
        if (dec < 0) {   // modelo viejo: salida aparte con cuántos decimales
            dec = s.decimalesViejos == null ? 0 : Decodificador.argmax(s.decimalesViejos, 0, s.decimalesViejos.length);
        }
        dec = Math.min(dec, Math.max(0, l.texto.length() - 1));
        return new Decodificador.Lectura(l.texto, l.digitos, l.confianza, dec);
    }

    /** Lectura completa de una foto. */
    public Resultado leer(Imagen foto) throws Exception {
        return leer(foto, null);
    }

    /** Con {@code esquinas} dadas se omite la ubicación del detector (sirve para medir solo el lector). */
    public Resultado leer(Imagen foto, double[][] esquinas) throws Exception {
        Deteccion d = detectar(foto);
        double[][] esq = esquinas != null ? esquinas : Procesamiento.ampliarEsquinas(d.esquinas, margenX);
        if (!conLectura(d.tipo) && esquinas == null) {
            return new Resultado(d.tipo, d.confianza, null, "", 0, 0.0, null, motor.version());
        }
        Imagen recorte;
        try {
            recorte = Procesamiento.rectificar(foto, esq, Procesamiento.ALTO_LECTOR);
        } catch (IllegalArgumentException e) {   // esquinas degeneradas
            return new Resultado(d.tipo, d.confianza, esq, "", 0, 0.0, null, motor.version());
        }
        Decodificador.Lectura l = leerRecorte(recorte);
        return new Resultado(d.tipo, d.confianza, esq, l.texto, l.decimales, l.confianza, l.digitos, motor.version());
    }

    @Override
    public void close() throws Exception {
        motor.close();
    }

    /** Resultado del detector. */
    public static final class Deteccion {
        public final String tipo;
        public final double confianza;
        public final double[][] esquinas;

        Deteccion(String tipo, double confianza, double[][] esquinas) {
            this.tipo = tipo;
            this.confianza = confianza;
            this.esquinas = esquinas;
        }
    }
}
