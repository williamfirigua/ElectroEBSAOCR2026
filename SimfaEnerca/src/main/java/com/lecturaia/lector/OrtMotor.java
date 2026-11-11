package com.lecturaia.lector;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

import java.nio.FloatBuffer;
import java.util.Collections;

/**
 * Motor con ONNX Runtime. Dependencia en Android (build.gradle de la app):
 * <pre>implementation 'com.microsoft.onnxruntime:onnxruntime-android:VERSION'</pre>
 * con VERSION = la misma que usa el PC (hoy 1.31.1) o la más reciente publicada.
 * (en escritorio: com.microsoft.onnxruntime:onnxruntime). La API es la misma en ambos.
 *
 * Los modelos se cargan desde bytes (p. ej. leídos de la carpeta de la app), así cada versión
 * nueva se copia sin recompilar. Las sesiones de ONNX Runtime son seguras para usar entre hilos.
 */
public final class OrtMotor implements Motor {
    private final OrtEnvironment env;
    private final OrtSession detector;
    private final OrtSession lector;
    private final String version;

    public OrtMotor(byte[] detectorOnnx, byte[] lectorOnnx, String version) throws OrtException {
        this.env = OrtEnvironment.getEnvironment();
        OrtSession.SessionOptions op = new OrtSession.SessionOptions();
        op.setIntraOpNumThreads(2);   // segundo plano: no acaparar el celular
        this.detector = env.createSession(detectorOnnx, op);
        this.lector = env.createSession(lectorOnnx, op);
        this.version = version;
    }

    @Override
    public float[][] detectar(float[] tensor) throws OrtException {
        long[] forma = {1, 3, Procesamiento.LADO_DETECTOR, Procesamiento.LADO_DETECTOR};
        try (OnnxTensor x = OnnxTensor.createTensor(env, FloatBuffer.wrap(tensor), forma);
             OrtSession.Result r = detector.run(Collections.singletonMap("imagen", x))) {
            float[][] tipo = (float[][]) r.get(0).getValue();       // (1, 6)
            float[][] esquinas = (float[][]) r.get(1).getValue();   // (1, 8)
            return new float[][]{tipo[0], esquinas[0]};
        }
    }

    @Override
    public SalidaLector leer(float[] tensor) throws OrtException {
        long[] forma = {1, 1, Procesamiento.ALTO_LECTOR, Procesamiento.ANCHO_LECTOR};
        try (OnnxTensor x = OnnxTensor.createTensor(env, FloatBuffer.wrap(tensor), forma);
             OrtSession.Result r = lector.run(Collections.singletonMap("recorte", x))) {
            float[][][] logits = (float[][][]) r.get(0).getValue();  // (1, T, C)
            float[] decimales = null;
            if (r.size() > 1) {                                      // modelos viejos: salida "decimales" (1, 4)
                decimales = ((float[][]) r.get(1).getValue())[0];
            }
            return new SalidaLector(logits[0], decimales);
        }
    }

    @Override
    public String version() {
        return version;
    }

    @Override
    public void close() throws OrtException {
        detector.close();
        lector.close();
    }
}
