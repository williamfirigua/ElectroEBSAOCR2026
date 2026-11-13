package com.gselectroCaqueta.accesoyseguridad;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

/**
 * Vista invisible dentro de dlg_alerta_hoja: al quedar en pantalla pega su dialogo abajo y a todo el ancho, para
 * que todos los AlertDialog de la app (cualquiera que use el tema CauDialogo) salgan como hoja. Solo presentacion.
 */
public class HojaAbajo extends View {

    public HojaAbajo(Context context, AttributeSet attrs) {
        super(context, attrs);
        setVisibility(GONE);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        View raiz = getRootView();
        ViewGroup.LayoutParams lp = raiz.getLayoutParams();
        if (!(lp instanceof WindowManager.LayoutParams)) return;
        WindowManager.LayoutParams w = (WindowManager.LayoutParams) lp;
        if (w.gravity == Gravity.BOTTOM && w.width == ViewGroup.LayoutParams.MATCH_PARENT) return;
        w.gravity = Gravity.BOTTOM;
        w.width = ViewGroup.LayoutParams.MATCH_PARENT;
        try {
            WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            if (wm != null) wm.updateViewLayout(raiz, w);
        } catch (RuntimeException ignored) {
            // Si la ventana ya no esta, el dialogo se queda centrado: no afecta lo que hace.
        }
    }
}
