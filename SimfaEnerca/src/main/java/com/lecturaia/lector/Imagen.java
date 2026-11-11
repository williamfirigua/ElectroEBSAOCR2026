package com.lecturaia.lector;

/**
 * Imagen de 8 bits en memoria, con los canales en orden B, G, R (como OpenCV en el PC), o de un
 * solo canal (gris). Se usa este orden para que el preprocesamiento sea idéntico al de Python.
 *
 * En Android se crea desde un Bitmap con {@code BitmapImagen.desde(bitmap)}; en escritorio o en
 * pruebas, con {@link #desdeRgb(int[], int, int)}.
 */
public final class Imagen {
    public final int ancho;
    public final int alto;
    public final int canales;      // 3 = BGR, 1 = gris
    public final byte[] datos;     // fila por fila, píxel por píxel, canal por canal

    public Imagen(int ancho, int alto, int canales, byte[] datos) {
        if (ancho <= 0 || alto <= 0) throw new IllegalArgumentException("Imagen vacía");
        if (canales != 1 && canales != 3) throw new IllegalArgumentException("Solo 1 o 3 canales");
        if (datos.length != ancho * alto * canales) throw new IllegalArgumentException("Tamaño de datos inválido");
        this.ancho = ancho;
        this.alto = alto;
        this.canales = canales;
        this.datos = datos;
    }

    public static Imagen nueva(int ancho, int alto, int canales) {
        return new Imagen(ancho, alto, canales, new byte[ancho * alto * canales]);
    }

    /** Desde píxeles empaquetados 0xAARRGGBB (lo que entregan Bitmap.getPixels y BufferedImage.getRGB). */
    public static Imagen desdeRgb(int[] argb, int ancho, int alto) {
        byte[] d = new byte[ancho * alto * 3];
        for (int i = 0, j = 0; i < argb.length; i++, j += 3) {
            int p = argb[i];
            d[j] = (byte) (p & 0xFF);             // B
            d[j + 1] = (byte) ((p >> 8) & 0xFF);  // G
            d[j + 2] = (byte) ((p >> 16) & 0xFF); // R
        }
        return new Imagen(ancho, alto, 3, d);
    }

    public int get(int x, int y, int c) {
        return datos[(y * ancho + x) * canales + c] & 0xFF;
    }

    public void set(int x, int y, int c, int v) {
        datos[(y * ancho + x) * canales + c] = (byte) v;
    }
}
