package com.gselectroCaqueta.accesoyseguridad;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * Fondo de la ventana del acceso: el grabado (acc_fondo_grabado) cubre toda la pantalla, centrado y pegado
 * abajo; lo que sobra se recorta por los lados o por arriba (cielo liso). Va en la ventana, no en el layout,
 * asi que no se mueve ni se deforma cuando el teclado achica el contenido.
 */
public class FondoGrabado extends Drawable {
    private static final int PAPEL = 0xFFF5EFE3;

    private final Bitmap grabado;
    private final Paint pincel = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
    private final Matrix matriz = new Matrix();

    public FondoGrabado(Resources res) {
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inJustDecodeBounds = true;
        op.inScaled = false;
        BitmapFactory.decodeResource(res, R.drawable.acc_fondo_grabado, op);
        // En pantallas pequenas no hace falta tener la imagen entera en memoria.
        int altoPantalla = Math.max(res.getDisplayMetrics().heightPixels, res.getDisplayMetrics().widthPixels);
        int muestra = 1;
        while (op.outHeight / (muestra * 2) >= altoPantalla) muestra *= 2;
        op.inJustDecodeBounds = false;
        op.inSampleSize = muestra;
        Bitmap b;
        try {
            b = BitmapFactory.decodeResource(res, R.drawable.acc_fondo_grabado, op);
        } catch (OutOfMemoryError e) {
            b = null;
        }
        grabado = b;
    }

    @Override
    protected void onBoundsChange(Rect r) {
        super.onBoundsChange(r);
        if (grabado == null || r.width() == 0 || r.height() == 0) return;
        float esc = Math.max(r.width() / (float) grabado.getWidth(), r.height() / (float) grabado.getHeight());
        matriz.reset();
        matriz.setScale(esc, esc);
        matriz.postTranslate(r.left + (r.width() - grabado.getWidth() * esc) / 2f,
                r.bottom - grabado.getHeight() * esc);
    }

    @Override
    public void draw(Canvas c) {
        c.drawColor(PAPEL);
        if (grabado != null) c.drawBitmap(grabado, matriz, pincel);
    }

    @Override
    public void setAlpha(int alpha) {
        pincel.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        pincel.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
