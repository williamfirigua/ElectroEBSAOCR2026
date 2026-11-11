package com.lecturaia.lector;

import java.util.ArrayList;
import java.util.List;

/**
 * Decodificación CTC voraz, igual a comun.decodificar de Python.
 *
 * Clases del lector: 0 = blanco; 1..10 = dígito ENTERO 0-9; 11..20 = dígito DECIMAL 0-9.
 * (Modelos viejos de 11 clases: sin marca de decimal; los decimales salen de otra salida.)
 */
public final class Decodificador {
    private Decodificador() {
    }

    public static final String ALFABETO = "0123456789";
    static final float UMBRAL_ALTERNATIVA = 0.10f;

    /** Un dígito leído. */
    public static final class Digito {
        public final char valor;
        public final float confianza;
        public final List<Character> alternativas;
        public final boolean claro;        // confianza >= 0,85 y sin alternativas
        public final boolean decimal;

        Digito(char valor, float confianza, List<Character> alternativas, boolean decimal) {
            this.valor = valor;
            this.confianza = confianza;
            this.alternativas = alternativas;
            this.claro = confianza >= 0.85f && alternativas.isEmpty();
            this.decimal = decimal;
        }
    }

    /** Resultado de decodificar: texto con todos los dígitos, decimales (-1 si el modelo no los marca). */
    public static final class Lectura {
        public final String texto;
        public final List<Digito> digitos;
        public final double confianza;
        public final int decimales;

        Lectura(String texto, List<Digito> digitos, double confianza, int decimales) {
            this.texto = texto;
            this.digitos = digitos;
            this.confianza = confianza;
            this.decimales = decimales;
        }
    }

    static float[] softmax(float[] x) {
        float max = Float.NEGATIVE_INFINITY;
        for (float v : x) max = Math.max(max, v);
        float[] e = new float[x.length];
        double s = 0;
        for (int i = 0; i < x.length; i++) {
            e[i] = (float) Math.exp(x[i] - max);
            s += e[i];
        }
        for (int i = 0; i < x.length; i++) e[i] = (float) (e[i] / s);
        return e;
    }

    /** @param logits [T][C] tal como sale del lector para una imagen. */
    public static Lectura decodificar(float[][] logits) {
        int t = logits.length;
        int c = logits[0].length;
        int n = ALFABETO.length();
        boolean etiquetado = c == 1 + 2 * n;
        float[][] p = new float[t][];
        int[] mejor = new int[t];
        for (int i = 0; i < t; i++) {
            p[i] = softmax(logits[i]);
            mejor[i] = argmax(p[i], 0, c);
        }
        // tramos consecutivos de la misma clase no blanca
        List<int[]> tramos = new ArrayList<>();
        int previo = 0, ini = -1;
        for (int i = 0; i <= t; i++) {
            int cl = i < t ? mejor[i] : 0;
            if (cl != previo) {
                if (previo != 0 && ini >= 0) tramos.add(new int[]{previo, ini, i});
                ini = i;
            }
            previo = cl;
        }
        List<Digito> digitos = new ArrayList<>();
        StringBuilder texto = new StringBuilder();
        double conf = 1.0;
        for (int[] tr : tramos) {
            int cl = tr[0], a = tr[1], b = tr[2];
            int tMax = a;
            for (int i = a + 1; i < b; i++) if (p[i][cl] > p[tMax][cl]) tMax = i;
            float[] fila = p[tMax];
            float[] q = new float[n];
            for (int k = 0; k < n; k++) q[k] = fila[1 + k] + (etiquetado ? fila[1 + n + k] : 0f);
            int v = argmax(q, 0, n);
            List<Character> alt = new ArrayList<>();
            int[] orden = ordenDescendente(q);
            for (int j = 0; j < 3 && j < orden.length; j++) {
                int k = orden[j];
                if (k != v && q[k] >= UMBRAL_ALTERNATIVA) alt.add(ALFABETO.charAt(k));
            }
            boolean dec = etiquetado && fila[1 + n + v] > fila[1 + v];
            // Python redondea la confianza de cada dígito a 4 decimales y multiplica esas
            double cd = redondear4(q[v]);
            digitos.add(new Digito(ALFABETO.charAt(v), (float) cd, alt, dec));
            texto.append(ALFABETO.charAt(v));
            conf *= cd;
        }
        if (digitos.isEmpty()) conf = 0.0;
        int decimales = -1;
        if (etiquetado) {
            decimales = 0;
            for (int i = digitos.size() - 1; i >= 0 && digitos.get(i).decimal; i--) decimales++;
            decimales = Math.min(decimales, Math.max(0, digitos.size() - 1));
        }
        return new Lectura(texto.toString(), digitos, conf, decimales);
    }

    /** round(x, 4) de Python: redondeo exacto del valor binario, mitades al par. */
    static double redondear4(float x) {
        return new java.math.BigDecimal((double) x).setScale(4, java.math.RoundingMode.HALF_EVEN).doubleValue();
    }

    static int argmax(float[] x, int desde, int hasta) {
        int k = desde;
        for (int i = desde + 1; i < hasta; i++) if (x[i] > x[k]) k = i;
        return k;
    }

    /** Índices ordenados por valor descendente; en empate, el índice menor primero (como numpy argsort estable sobre -x). */
    private static int[] ordenDescendente(float[] x) {
        Integer[] idx = new Integer[x.length];
        for (int i = 0; i < x.length; i++) idx[i] = i;
        java.util.Arrays.sort(idx, (i, j) -> {
            int cmp = Float.compare(x[j], x[i]);
            return cmp != 0 ? cmp : Integer.compare(i, j);
        });
        int[] r = new int[x.length];
        for (int i = 0; i < x.length; i++) r[i] = idx[i];
        return r;
    }
}
