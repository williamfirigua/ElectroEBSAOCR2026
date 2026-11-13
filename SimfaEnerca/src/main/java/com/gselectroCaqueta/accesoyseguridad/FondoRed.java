package com.gselectroCaqueta.accesoyseguridad;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

/**
 * Fondo del acceso: la red de distribucion (acc_fondo_red) a todo el ancho y pegada abajo. Toma solo el espacio
 * que le sobra a la pantalla (no pide alto propio), asi que en pantallas pequenas o con el teclado abierto se
 * recorta por arriba y nunca empuja ni tapa los campos. Decorativa.
 */
public class FondoRed extends View {

    /** Por debajo de este alto no vale la pena dibujar un pedazo. */
    private static final int ALTO_MINIMO_DP = 48;

    private final Drawable red;

    public FondoRed(Context context, AttributeSet attrs) {
        super(context, attrs);
        red = ContextCompat.getDrawable(context, R.drawable.acc_fondo_red);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth(), h = getHeight();
        if (red == null || w <= 0 || h < ALTO_MINIMO_DP * getResources().getDisplayMetrics().density) return;
        int altoRed = Math.round(w * (float) red.getIntrinsicHeight() / red.getIntrinsicWidth());
        red.setBounds(0, h - altoRed, w, h);
        red.draw(canvas);
    }
}
