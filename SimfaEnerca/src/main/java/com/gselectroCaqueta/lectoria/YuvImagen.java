package com.gselectroCaqueta.lectoria;

import androidx.camera.core.ImageProxy;

import com.lecturaia.lector.Imagen;

import java.nio.ByteBuffer;

/**
 * Cuadro de CameraX (YUV_420_888) a {@link Imagen} BGR, ya derecho (aplica la rotación del cuadro).
 *
 * Usa la conversión BT.601 de rango completo, la misma de los JPEG de la cámara, para que el modelo
 * vea los colores como en las fotos con las que se entrenó. {@code paso} = 2 toma uno de cada dos
 * píxeles (cuadros de más de 1600 px de lado).
 */
public final class YuvImagen {
    private YuvImagen() {
    }

    // Buffers reutilizados entre cuadros: el análisis corre siempre en el mismo hilo.
    private static byte[] by, bu, bv;

    public static synchronized Imagen desde(ImageProxy img) {     // buffers compartidos: un hilo a la vez
        int w = img.getWidth(), h = img.getHeight();
        int paso = Math.max(w, h) > 1600 ? 2 : 1;
        return convertir(img, w, h, img.getImageInfo().getRotationDegrees(), paso);
    }

    private static Imagen convertir(ImageProxy img, int w, int h, int rot, int paso) {
        ImageProxy.PlaneProxy[] pl = img.getPlanes();
        by = copiar(pl[0].getBuffer(), by);
        bu = copiar(pl[1].getBuffer(), bu);
        bv = copiar(pl[2].getBuffer(), bv);
        return convertir(by, bu, bv, pl[0].getRowStride(), pl[0].getPixelStride(), pl[1].getRowStride(),
                pl[1].getPixelStride(), w, h, rot, paso);
    }

    /** La conversión en sí, sin Android (se prueba en el PC). */
    static Imagen convertir(byte[] by, byte[] bu, byte[] bv, int ryStride, int pyStride, int ruvStride, int puvStride,
                            int w, int h, int rot, int paso) {

        int sw = w / paso, sh = h / paso;                       // tamaño antes de rotar
        boolean gira = rot == 90 || rot == 270;
        int ow = gira ? sh : sw, oh = gira ? sw : sh;
        byte[] out = new byte[ow * oh * 3];
        for (int sy = 0; sy < sh; sy++) {
            int y0 = sy * paso;
            int filaY = y0 * ryStride, filaUV = (y0 >> 1) * ruvStride;
            for (int sx = 0; sx < sw; sx++) {
                int x0 = sx * paso;
                int Y = by[filaY + x0 * pyStride] & 0xFF;
                int iuv = filaUV + (x0 >> 1) * puvStride;
                int U = (bu[iuv] & 0xFF) - 128, V = (bv[iuv] & 0xFF) - 128;
                // Punto fijo 16.16: R = Y + 1,402 V; G = Y - 0,344136 U - 0,714136 V; B = Y + 1,772 U
                int r = Y + ((91881 * V + 32768) >> 16);
                int g = Y - ((22554 * U + 46802 * V + 32768) >> 16);
                int b = Y + ((116130 * U + 32768) >> 16);
                int ox, oy;
                switch (rot) {
                    case 90:  ox = sh - 1 - sy; oy = sx; break;
                    case 180: ox = sw - 1 - sx; oy = sh - 1 - sy; break;
                    case 270: ox = sy; oy = sw - 1 - sx; break;
                    default:  ox = sx; oy = sy;
                }
                int k = (oy * ow + ox) * 3;
                out[k] = (byte) (b < 0 ? 0 : b > 255 ? 255 : b);
                out[k + 1] = (byte) (g < 0 ? 0 : g > 255 ? 255 : g);
                out[k + 2] = (byte) (r < 0 ? 0 : r > 255 ? 255 : r);
            }
        }
        return new Imagen(ow, oh, 3, out);
    }

    private static byte[] copiar(ByteBuffer b, byte[] destino) {
        b.rewind();
        int n = b.remaining();
        if (destino == null || destino.length < n) destino = new byte[n];
        b.get(destino, 0, n);
        return destino;
    }
}
