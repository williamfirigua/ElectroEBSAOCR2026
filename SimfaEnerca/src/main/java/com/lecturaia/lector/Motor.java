package com.lecturaia.lector;

/**
 * Ejecuta los dos modelos .onnx. La implementación real es {@code OrtMotor} (ONNX Runtime, en
 * Android o escritorio); las pruebas usan un motor que devuelve salidas grabadas desde Python.
 */
public interface Motor extends AutoCloseable {
    /** Salidas del detector para una imagen (1,3,320,320): [0] = logits de tipo (6), [1] = esquinas (8). */
    float[][] detectar(float[] tensor) throws Exception;

    /** Salidas del lector para un recorte (1,1,64,320): logits [T][C]; y, en modelos viejos, los de decimales. */
    SalidaLector leer(float[] tensor) throws Exception;

    /** Versión del modelo (nombre de la carpeta o la que diga modelo.json). */
    String version();

    final class SalidaLector {
        public final float[][] logits;
        public final float[] decimalesViejos;   // null en modelos actuales

        public SalidaLector(float[][] logits, float[] decimalesViejos) {
            this.logits = logits;
            this.decimalesViejos = decimalesViejos;
        }
    }
}
