package com.gselectroCaqueta.lectoria;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import androidx.exifinterface.media.ExifInterface;

import com.lecturaia.lector.Imagen;

import java.io.IOException;

/** Convierte fotos de Android a {@link Imagen} (BGR), respetando la orientación EXIF de la cámara. */
public final class BitmapImagen {
    private BitmapImagen() {
    }

    /**
     * Lee una foto del disco. {@code ladoMaximo} limita la memoria (p. ej. 2048): las fotos de los
     * celulares actuales pasan de 12 MP y el modelo no gana nada con más de ~2000 px de lado.
     */
    public static Imagen leer(String ruta, int ladoMaximo) throws IOException {
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(ruta, op);
        if (op.outWidth <= 0 || op.outHeight <= 0) throw new IOException("No se pudo leer la foto: " + ruta);
        int muestreo = 1;
        while (Math.max(op.outWidth, op.outHeight) / (muestreo * 2) >= ladoMaximo) muestreo *= 2;
        op = new BitmapFactory.Options();
        op.inSampleSize = muestreo;
        op.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bmp = BitmapFactory.decodeFile(ruta, op);
        if (bmp == null) throw new IOException("No se pudo decodificar la foto: " + ruta);
        bmp = aplicarOrientacion(bmp, ruta);
        try {
            return desde(bmp);
        } finally {
            bmp.recycle();
        }
    }

    public static Imagen desde(Bitmap bmp) {
        int w = bmp.getWidth(), h = bmp.getHeight();
        int[] px = new int[w * h];
        bmp.getPixels(px, 0, w, 0, 0, w, h);
        return Imagen.desdeRgb(px, w, h);
    }

    private static Bitmap aplicarOrientacion(Bitmap bmp, String ruta) throws IOException {
        int o = new ExifInterface(ruta).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        float grados;
        switch (o) {
            case ExifInterface.ORIENTATION_ROTATE_90: grados = 90; break;
            case ExifInterface.ORIENTATION_ROTATE_180: grados = 180; break;
            case ExifInterface.ORIENTATION_ROTATE_270: grados = 270; break;
            default: return bmp;
        }
        Matrix m = new Matrix();
        m.postRotate(grados);
        Bitmap r = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);
        if (r != bmp) bmp.recycle();
        return r;
    }
}
