package com.gselectroCaqueta.lectoria;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

/**
 * Dibuja sobre la vista de la cámara el cuadro que encontró el modelo y lo que leyó.
 * La vista previa debe estar en FIT_CENTER y con la misma proporción que el cuadro analizado
 * (ver {@link LecturaEnVivoActivity}), así las coordenadas coinciden.
 */
public final class SuperposicionView extends View {
    public static final int VERDE = Color.rgb(47, 191, 113);
    public static final int AMARILLO = Color.rgb(240, 163, 58);
    public static final int GRIS = Color.rgb(154, 163, 174);

    private final Paint trazo = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint texto = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fondo = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path camino = new Path();

    private double[][] esquinas;
    private int anchoCuadro, altoCuadro;
    private String etiqueta;
    private int color = GRIS;

    public SuperposicionView(Context ctx) {
        super(ctx);
        float d = getResources().getDisplayMetrics().density;
        trazo.setStyle(Paint.Style.STROKE);
        trazo.setStrokeWidth(3 * d);
        texto.setTextSize(18 * d);
        texto.setColor(Color.WHITE);
        fondo.setColor(Color.argb(170, 0, 0, 0));
    }

    /** Desde cualquier hilo. {@code esquinas} normalizadas 0..1 sobre el cuadro analizado (o null). */
    public void mostrar(double[][] esquinas, int anchoCuadro, int altoCuadro, String etiqueta, int color) {
        this.esquinas = esquinas;
        this.anchoCuadro = anchoCuadro;
        this.altoCuadro = altoCuadro;
        this.etiqueta = etiqueta;
        this.color = color;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        double[][] e = esquinas;
        if (e == null || anchoCuadro <= 0 || altoCuadro <= 0) return;
        // FIT_CENTER: el cuadro completo cabe en la vista, centrado
        float s = Math.min((float) getWidth() / anchoCuadro, (float) getHeight() / altoCuadro);
        float ox = (getWidth() - anchoCuadro * s) / 2f, oy = (getHeight() - altoCuadro * s) / 2f;
        camino.reset();
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            float x = ox + (float) e[i][0] * anchoCuadro * s, y = oy + (float) e[i][1] * altoCuadro * s;
            if (i == 0) camino.moveTo(x, y); else camino.lineTo(x, y);
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
        }
        camino.close();
        trazo.setColor(color);
        c.drawPath(camino, trazo);
        if (etiqueta != null && !etiqueta.isEmpty()) {
            float w = texto.measureText(etiqueta), h = texto.getTextSize();
            float ty = Math.max(h + 8, minY - 10);
            c.drawRect(minX - 6, ty - h - 4, minX + w + 6, ty + 8, fondo);
            c.drawText(etiqueta, minX, ty, texto);
        }
    }
}
