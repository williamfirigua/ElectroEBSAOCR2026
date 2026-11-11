package com.lecturaia.lector;

import java.util.Arrays;

/**
 * Preprocesamiento idéntico al de {@code lectura_ia/modelo/comun.py} (Python + OpenCV):
 * letterbox, tensor del detector, enderezado por perspectiva y tensor del lector.
 *
 * Replica los detalles de OpenCV que cambian los números: redimensionado INTER_AREA, gris con
 * coeficientes enteros, interpolación bicúbica de OpenCV 5 con bordes replicados, y el redondeo "al par" de Python. Las diferencias que quedan son de ±1 nivel de gris
 * en algunos píxeles, sin efecto en la lectura (lo verifica PruebaParidad).
 */
public final class Procesamiento {
    private Procesamiento() {
    }

    public static final int LADO_DETECTOR = 320;
    public static final int ALTO_LECTOR = 64;
    public static final int ANCHO_LECTOR = 320;
    private static final float[] MEDIA = {0.485f, 0.456f, 0.406f};   // ImageNet, orden RGB
    private static final float[] DESV = {0.229f, 0.224f, 0.225f};

    /** Resultado de {@link #letterbox}: imagen cuadrada y la escala aplicada. */
    public static final class Letterbox {
        public final Imagen imagen;
        public final double escala;

        Letterbox(Imagen imagen, double escala) {
            this.imagen = imagen;
            this.escala = escala;
        }
    }

    /** round() de Python: mitades al par. */
    static long redondearPar(double x) {
        return (long) Math.rint(x);
    }

    // ------------------------------------------------------------------ detector
    public static Letterbox letterbox(Imagen img, int lado) {
        double escala = (double) lado / Math.max(img.alto, img.ancho);
        int nw = (int) Math.max(1, redondearPar(img.ancho * escala));
        int nh = (int) Math.max(1, redondearPar(img.alto * escala));
        Imagen lienzo = Imagen.nueva(lado, lado, 3);
        Arrays.fill(lienzo.datos, (byte) 114);
        Imagen red = redimensionarArea(img, nw, nh);
        for (int y = 0; y < nh; y++) {
            System.arraycopy(red.datos, y * nw * 3, lienzo.datos, y * lado * 3, nw * 3);
        }
        return new Letterbox(lienzo, escala);
    }

    /** (1, 3, lado, lado) en orden RGB, normalizado como ImageNet. */
    public static float[] tensorDetector(Imagen lb) {
        int n = lb.ancho * lb.alto;
        float[] t = new float[3 * n];
        for (int i = 0; i < n; i++) {
            for (int c = 0; c < 3; c++) {                     // c: 0=R 1=G 2=B
                int v = lb.datos[i * 3 + (2 - c)] & 0xFF;     // la imagen está en BGR
                t[c * n + i] = ((v / 255.0f) - MEDIA[c]) / DESV[c];
            }
        }
        return t;
    }

    /** Esquinas del detector (normalizadas al cuadro de 320) a la foto original, normalizadas 0..1 y recortadas. */
    public static double[][] esquinasDesdeLetterbox(float[] esq, int ancho, int alto, double escala, int lado) {
        double[][] r = new double[4][2];
        for (int i = 0; i < 4; i++) {   // numpy hace los dos primeros pasos en float32
            float x = (esq[2 * i] * lado) / (float) escala;
            float y = (esq[2 * i + 1] * lado) / (float) escala;
            r[i][0] = Math.min(1.0, Math.max(0.0, (double) x / ancho));
            r[i][1] = Math.min(1.0, Math.max(0.0, (double) y / alto));
        }
        return r;
    }

    /**
     * Abre el cuadro a lo largo de los dígitos ({@code margenX} del ancho a cada lado), igual que
     * comun.ampliar_esquinas en Python (en float32). Evita que el lector pierda el primer dígito
     * cuando el detector deja el cuadro justo. El margen de cada modelo viene en modelo.json.
     */
    public static double[][] ampliarEsquinas(double[][] e, double margenX) {
        float[][] p = new float[4][2];
        for (int i = 0; i < 4; i++) {
            p[i][0] = (float) e[i][0];
            p[i][1] = (float) e[i][1];
        }
        if (margenX != 0) {
            float m = (float) margenX;
            float ex = ((p[1][0] - p[0][0]) + (p[2][0] - p[3][0])) / 2f;
            float ey = ((p[1][1] - p[0][1]) + (p[2][1] - p[3][1])) / 2f;
            float dx = m * ex, dy = m * ey;
            p[0][0] -= dx; p[0][1] -= dy; p[3][0] -= dx; p[3][1] -= dy;
            p[1][0] += dx; p[1][1] += dy; p[2][0] += dx; p[2][1] += dy;
        }
        double[][] r = new double[4][2];
        for (int i = 0; i < 4; i++) {
            r[i][0] = Math.min(1f, Math.max(0f, p[i][0]));
            r[i][1] = Math.min(1f, Math.max(0f, p[i][1]));
        }
        return r;
    }

    // ------------------------------------------------------------------ lector
    /** Enderezado del contador: mismas cuentas que comun.rectificar (alto de salida 64). */
    public static Imagen rectificar(Imagen img, double[][] esquinasNorm, int alto) {
        double[][] p = new double[4][2];
        for (int i = 0; i < 4; i++) {
            // numpy: esquinas a float32, producto en float64, resultado a float32
            p[i][0] = (float) ((double) (float) esquinasNorm[i][0] * img.ancho);
            p[i][1] = (float) ((double) (float) esquinasNorm[i][1] * img.alto);
        }
        double ancho = Math.max(dist(p[1], p[0]), dist(p[2], p[3]));
        double altoQ = Math.max(Math.max(dist(p[3], p[0]), dist(p[2], p[1])), 1.0);
        int salidaW = (int) Math.min(1024, Math.max(alto, redondearPar(alto * ancho / altoQ)));
        double[][] dst = {{0, 0}, {salidaW - 1, 0}, {salidaW - 1, alto - 1}, {0, alto - 1}};
        double[] m = transformacionPerspectiva(p, dst);
        return warpPerspectivaCubica(img, invertir3x3(m), salidaW, alto);
    }

    private static double dist(double[] a, double[] b) {
        // numpy calcula la norma en float32
        float dx = (float) (a[0] - b[0]);
        float dy = (float) (a[1] - b[1]);
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    /** (1, 64, 320) en [-1, 1]: gris, mismo alto, ancho proporcional, relleno a la derecha con la mediana del borde. */
    public static float[] tensorLector(Imagen recorte) {
        Imagen g = recorte.canales == 3 ? gris(recorte) : recorte;
        int nw = (int) Math.max(8, redondearPar((double) g.ancho * ALTO_LECTOR / g.alto));
        int destinoW = Math.min(nw, ANCHO_LECTOR);
        Imagen r = (g.ancho == destinoW && g.alto == ALTO_LECTOR) ? g : redimensionarArea(g, destinoW, ALTO_LECTOR);
        float[] t = new float[ALTO_LECTOR * ANCHO_LECTOR];
        int relleno = r.ancho < ANCHO_LECTOR ? medianaBordeDerecho(r) : 0;
        for (int y = 0; y < ALTO_LECTOR; y++) {
            for (int x = 0; x < ANCHO_LECTOR; x++) {
                int v = x < r.ancho ? r.get(x, y, 0) : relleno;
                t[y * ANCHO_LECTOR + x] = ((v / 255.0f) - 0.5f) / 0.5f;
            }
        }
        return t;
    }

    private static int medianaBordeDerecho(Imagen g) {
        int cols = Math.min(4, g.ancho);
        int[] v = new int[cols * g.alto];
        int k = 0;
        for (int y = 0; y < g.alto; y++) {
            for (int x = g.ancho - cols; x < g.ancho; x++) {
                v[k++] = g.get(x, y, 0);
            }
        }
        Arrays.sort(v);
        int n = v.length;
        double med = (n % 2 == 1) ? v[n / 2] : (v[n / 2 - 1] + v[n / 2]) / 2.0;
        return (int) med;                                    // int() de Python: trunca
    }

    /** BGR -> gris con los coeficientes enteros de OpenCV (Y = 0.299 R + 0.587 G + 0.114 B). */
    public static Imagen gris(Imagen img) {
        Imagen g = Imagen.nueva(img.ancho, img.alto, 1);
        for (int i = 0, n = img.ancho * img.alto; i < n; i++) {
            int b = img.datos[i * 3] & 0xFF;
            int gg = img.datos[i * 3 + 1] & 0xFF;
            int r = img.datos[i * 3 + 2] & 0xFF;
            g.datos[i] = (byte) ((b * 1868 + gg * 9617 + r * 4899 + (1 << 13)) >> 14);
        }
        return g;
    }

    // ------------------------------------------------------------------ redimensionado (cv2.INTER_AREA)
    private static final class Pesos {
        final int[] destino;
        final int[] origen;
        final float[] alfa;
        final int n;

        Pesos(int[] d, int[] s, float[] a, int n) {
            this.destino = d;
            this.origen = s;
            this.alfa = a;
            this.n = n;
        }
    }

    /** Tabla de pesos por área de OpenCV (computeResizeAreaTab). */
    private static Pesos pesosArea(int ssize, int dsize, double escala) {
        int max = dsize * ((int) Math.ceil(escala) + 2);
        int[] d = new int[max];
        int[] s = new int[max];
        float[] a = new float[max];
        int k = 0;
        for (int dx = 0; dx < dsize; dx++) {
            double fsx1 = dx * escala;
            double fsx2 = fsx1 + escala;
            double celda = Math.min(escala, ssize - fsx1);
            int sx1 = (int) Math.ceil(fsx1);
            int sx2 = (int) Math.floor(fsx2);
            sx2 = Math.min(sx2, ssize - 1);
            sx1 = Math.min(sx1, sx2);
            if (sx1 - fsx1 > 1e-3) {
                d[k] = dx;
                s[k] = sx1 - 1;
                a[k++] = (float) ((sx1 - fsx1) / celda);
            }
            for (int sx = sx1; sx < sx2; sx++) {
                d[k] = dx;
                s[k] = sx;
                a[k++] = (float) (1.0 / celda);
            }
            if (fsx2 - sx2 > 1e-3) {
                d[k] = dx;
                s[k] = sx2;
                a[k++] = (float) (Math.min(Math.min(fsx2 - sx2, 1.0), celda) / celda);
            }
        }
        return new Pesos(d, s, a, k);
    }

    public static Imagen redimensionarArea(Imagen img, int nw, int nh) {
        if (nw == img.ancho && nh == img.alto) {
            return new Imagen(nw, nh, img.canales, img.datos.clone());
        }
        double sx = (double) img.ancho / nw;
        double sy = (double) img.alto / nh;
        if (sx < 1 || sy < 1) {
            return redimensionarLineal(img, nw, nh);   // ampliar: no ocurre con fotos de cámara
        }
        int cn = img.canales;
        Pesos px = pesosArea(img.ancho, nw, sx);
        Pesos py = pesosArea(img.alto, nh, sy);
        float[] fila = new float[nw * cn];
        float[] suma = new float[nw * cn];
        Imagen out = Imagen.nueva(nw, nh, cn);
        int k = 0;
        for (int dy = 0; dy < nh; dy++) {
            Arrays.fill(suma, 0f);
            while (k < py.n && py.destino[k] == dy) {
                int syy = py.origen[k];
                float ay = py.alfa[k];
                Arrays.fill(fila, 0f);
                int base = syy * img.ancho * cn;
                for (int j = 0; j < px.n; j++) {
                    int di = px.destino[j] * cn;
                    int si = base + px.origen[j] * cn;
                    float ax = px.alfa[j];
                    for (int c = 0; c < cn; c++) {
                        fila[di + c] += (img.datos[si + c] & 0xFF) * ax;
                    }
                }
                for (int i = 0; i < suma.length; i++) {
                    suma[i] += fila[i] * ay;
                }
                k++;
            }
            int o = dy * nw * cn;
            for (int i = 0; i < suma.length; i++) {
                out.datos[o + i] = (byte) saturar((int) Math.rint(suma[i]));
            }
        }
        return out;
    }

    private static Imagen redimensionarLineal(Imagen img, int nw, int nh) {
        Imagen out = Imagen.nueva(nw, nh, img.canales);
        double sx = (double) img.ancho / nw;
        double sy = (double) img.alto / nh;
        for (int y = 0; y < nh; y++) {
            double fy = Math.max(0, (y + 0.5) * sy - 0.5);
            int y0 = Math.min((int) fy, img.alto - 1);
            int y1 = Math.min(y0 + 1, img.alto - 1);
            double wy = fy - y0;
            for (int x = 0; x < nw; x++) {
                double fx = Math.max(0, (x + 0.5) * sx - 0.5);
                int x0 = Math.min((int) fx, img.ancho - 1);
                int x1 = Math.min(x0 + 1, img.ancho - 1);
                double wx = fx - x0;
                for (int c = 0; c < img.canales; c++) {
                    double v = (img.get(x0, y0, c) * (1 - wx) + img.get(x1, y0, c) * wx) * (1 - wy)
                            + (img.get(x0, y1, c) * (1 - wx) + img.get(x1, y1, c) * wx) * wy;
                    out.set(x, y, c, saturar((int) Math.rint(v)));
                }
            }
        }
        return out;
    }

    private static int saturar(int v) {
        return v < 0 ? 0 : (v > 255 ? 255 : v);
    }

    // ------------------------------------------------------------------ perspectiva
    /** Homografía que lleva src[i] a dst[i] (cv2.getPerspectiveTransform). Devuelve 9 valores fila por fila. */
    public static double[] transformacionPerspectiva(double[][] src, double[][] dst) {
        double[][] a = new double[8][9];
        for (int i = 0; i < 4; i++) {
            double x = src[i][0], y = src[i][1], u = dst[i][0], v = dst[i][1];
            a[i] = new double[]{x, y, 1, 0, 0, 0, -x * u, -y * u, u};
            a[i + 4] = new double[]{0, 0, 0, x, y, 1, -x * v, -y * v, v};
        }
        for (int col = 0; col < 8; col++) {                       // Gauss con pivoteo parcial
            int piv = col;
            for (int r = col + 1; r < 8; r++) {
                if (Math.abs(a[r][col]) > Math.abs(a[piv][col])) piv = r;
            }
            double[] tmp = a[col];
            a[col] = a[piv];
            a[piv] = tmp;
            if (Math.abs(a[col][col]) < 1e-12) throw new IllegalArgumentException("Esquinas degeneradas");
            for (int r = 0; r < 8; r++) {
                if (r == col) continue;
                double f = a[r][col] / a[col][col];
                for (int c = col; c < 9; c++) a[r][c] -= f * a[col][c];
            }
        }
        double[] m = new double[9];
        for (int i = 0; i < 8; i++) m[i] = a[i][8] / a[i][i];
        m[8] = 1.0;
        return m;
    }

    static double[] invertir3x3(double[] m) {
        double a = m[0], b = m[1], c = m[2], d = m[3], e = m[4], f = m[5], g = m[6], h = m[7], i = m[8];
        double det = a * (e * i - f * h) - b * (d * i - f * g) + c * (d * h - e * g);
        if (Math.abs(det) < 1e-15) throw new IllegalArgumentException("Transformación no invertible");
        return new double[]{
            (e * i - f * h) / det, (c * h - b * i) / det, (b * f - c * e) / det,
            (f * g - d * i) / det, (a * i - c * g) / det, (c * d - a * f) / det,
            (d * h - e * g) / det, (b * g - a * h) / det, (a * e - b * d) / det};
    }

    /** Coeficientes bicúbicos de OpenCV (A = -0,75), calculados en float como él. */
    private static float[] coefCubicos(float t) {
        final float a = -0.75f;
        float[] c = new float[4];
        c[0] = ((a * (t + 1) - 5 * a) * (t + 1) + 8 * a) * (t + 1) - 4 * a;
        c[1] = ((a + 2) * t - (a + 3)) * t * t + 1;
        c[2] = ((a + 2) * (1 - t) - (a + 3)) * (1 - t) * (1 - t) + 1;
        c[3] = 1.f - c[0] - c[1] - c[2];
        return c;
    }

    /**
     * cv2.warpPerspective(INTER_CUBIC, BORDER_REPLICATE) de OpenCV 5 (el que usa el PC), con la matriz
     * ya invertida (destino -> origen). OpenCV 5 interpola con coordenadas continuas; verificado
     * píxel a píxel contra cv2 5.0.
     */
    public static Imagen warpPerspectivaCubica(Imagen src, double[] inv, int w, int h) {
        int cn = src.canales;
        Imagen out = Imagen.nueva(w, h, cn);
        double[] acum = new double[cn];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                double ww = inv[6] * x + inv[7] * y + inv[8];
                double sx = (inv[0] * x + inv[1] * y + inv[2]) / ww;
                double sy = (inv[3] * x + inv[4] * y + inv[5]) / ww;
                int ix = (int) Math.floor(sx);
                int iy = (int) Math.floor(sy);
                float[] cx = coefCubicos((float) (sx - ix));
                float[] cy = coefCubicos((float) (sy - iy));
                Arrays.fill(acum, 0);
                for (int j = 0; j < 4; j++) {
                    int yy = Math.min(Math.max(iy - 1 + j, 0), src.alto - 1);
                    for (int i = 0; i < 4; i++) {
                        int xx = Math.min(Math.max(ix - 1 + i, 0), src.ancho - 1);
                        float wgt = cx[i] * cy[j];
                        int base = (yy * src.ancho + xx) * cn;
                        for (int c = 0; c < cn; c++) acum[c] += (src.datos[base + c] & 0xFF) * wgt;
                    }
                }
                int o = (y * w + x) * cn;
                for (int c = 0; c < cn; c++) out.datos[o + c] = (byte) saturar((int) Math.rint(acum[c]));
            }
        }
        return out;
    }
}
