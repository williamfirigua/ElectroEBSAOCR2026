package com.gselectroCaqueta.accesoyseguridad;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.function.IntConsumer;

/**
 * Dialogos de la lectura con la paleta para el sol (R.style.CauDialogo, colors_lectura.xml): caja de contexto con la
 * cuenta, aviso ambar, campos de 48dp con su etiqueta encima y el error en linea debajo. Solo presentacion: quien lo
 * usa conserva sus botones, sus validaciones y lo que hace cada respuesta.
 */
public final class DialogosLectura {

    private static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT;
    private static final int WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;

    private DialogosLectura() {
    }

    /** AlertDialog de AppCompat con el tema de los dialogos de la lectura. */
    public static AlertDialog.Builder builder(Context ctx) {
        return new AlertDialog.Builder(ctx, R.style.CauDialogo);
    }

    public static int dp(Context ctx, float valor) {
        return Math.round(valor * ctx.getResources().getDisplayMetrics().density);
    }

    private static int color(Context ctx, int id) {
        return ContextCompat.getColor(ctx, id);
    }

    private static LinearLayout.LayoutParams fila(Context ctx, int margenArribaDp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(MATCH, WRAP);
        lp.topMargin = dp(ctx, margenArribaDp);
        return lp;
    }

    /** "TEXTO EN MAYUSCULAS" -> "Texto en mayusculas" (para datos que llegan en mayusculas sostenidas). */
    public static String oracion(String s) {
        if (s == null) return "";
        String t = s.trim();
        if (t.isEmpty()) return t;
        String min = t.toLowerCase(java.util.Locale.ROOT);
        return Character.toUpperCase(min.charAt(0)) + min.substring(1);
    }

    /** "Cta 403494850 · MED 1820456" sin las partes vacias. */
    public static String lineaCliente(String cuenta, String medidor) {
        StringBuilder b = new StringBuilder();
        if (cuenta != null && !cuenta.trim().isEmpty()) b.append("Cta ").append(cuenta.trim());
        if (medidor != null && !medidor.trim().isEmpty()) {
            if (b.length() > 0) b.append(" · ");
            b.append("MED ").append(medidor.trim());
        }
        return b.toString();
    }

    // ---------------------------------------------------------------- cuerpo

    /** Cuerpo vertical con el mismo margen lateral del titulo (24dp). */
    public static LinearLayout cuerpo(Context ctx) {
        LinearLayout l = new LinearLayout(ctx);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(ctx, 24), dp(ctx, 4), dp(ctx, 24), dp(ctx, 8));
        return l;
    }

    /** El cuerpo dentro de un ScrollView, para pantallas pequenas o con el teclado abierto. */
    public static ScrollView desplazable(View contenido) {
        ScrollView s = new ScrollView(contenido.getContext());
        s.addView(contenido);
        return s;
    }

    /** Parrafo principal: 16sp negro. */
    public static TextView texto(LinearLayout cuerpo, CharSequence s) {
        Context ctx = cuerpo.getContext();
        TextView t = new TextView(ctx);
        t.setText(s);
        t.setTextColor(color(ctx, R.color.lec_texto));
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        t.setLineSpacing(0, 1.15f);
        cuerpo.addView(t, fila(ctx, cuerpo.getChildCount() == 0 ? 0 : 12));
        return t;
    }

    /** Texto secundario: 13sp. */
    public static TextView nota(LinearLayout cuerpo, CharSequence s) {
        Context ctx = cuerpo.getContext();
        TextView t = new TextView(ctx);
        t.setText(s);
        t.setTextColor(color(ctx, R.color.lec_texto2));
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        cuerpo.addView(t, fila(ctx, cuerpo.getChildCount() == 0 ? 0 : 10));
        return t;
    }

    /** Subtitulo de un grupo (p. ej. "Como corregirlo"): 15sp negrita. */
    public static TextView subtitulo(LinearLayout cuerpo, CharSequence s) {
        Context ctx = cuerpo.getContext();
        TextView t = new TextView(ctx);
        t.setText(s);
        t.setTextColor(color(ctx, R.color.lec_texto));
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        cuerpo.addView(t, fila(ctx, cuerpo.getChildCount() == 0 ? 0 : 16));
        return t;
    }

    /**
     * Caja de contexto (azul claro): primera linea en negrita (cuenta y medidor), segunda normal (direccion).
     * Las lineas vacias no se pintan; si ambas lo estan no agrega nada.
     */
    public static View contexto(LinearLayout cuerpo, String linea1, String linea2) {
        boolean hay1 = linea1 != null && !linea1.trim().isEmpty();
        boolean hay2 = linea2 != null && !linea2.trim().isEmpty();
        if (!hay1 && !hay2) return null;
        Context ctx = cuerpo.getContext();
        LinearLayout caja = new LinearLayout(ctx);
        caja.setOrientation(LinearLayout.VERTICAL);
        caja.setBackgroundResource(R.drawable.dlg_contexto);
        caja.setPadding(dp(ctx, 14), dp(ctx, 10), dp(ctx, 14), dp(ctx, 10));
        if (hay1) {
            TextView t = new TextView(ctx);
            t.setText(linea1.trim());
            t.setTextColor(color(ctx, R.color.lec_texto));
            t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            caja.addView(t);
        }
        if (hay2) {
            TextView t = new TextView(ctx);
            t.setText(linea2.trim());
            t.setTextColor(color(ctx, R.color.lec_texto2));
            t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            caja.addView(t);
        }
        cuerpo.addView(caja, fila(ctx, cuerpo.getChildCount() == 0 ? 0 : 12));
        return caja;
    }

    /** Caja de datos "etiqueta: valor" (p. ej. primero y ultimo medidor de un contenedor). */
    public static View datos(LinearLayout cuerpo, List<String[]> filas) {
        if (filas == null || filas.isEmpty()) return null;
        Context ctx = cuerpo.getContext();
        LinearLayout caja = new LinearLayout(ctx);
        caja.setOrientation(LinearLayout.VERTICAL);
        caja.setBackgroundResource(R.drawable.dlg_contexto);
        caja.setPadding(dp(ctx, 14), dp(ctx, 8), dp(ctx, 14), dp(ctx, 8));
        for (String[] f : filas) {
            LinearLayout r = new LinearLayout(ctx);
            r.setOrientation(LinearLayout.HORIZONTAL);
            r.setPadding(0, dp(ctx, 2), 0, dp(ctx, 2));
            TextView e = new TextView(ctx);
            e.setText(f[0]);
            e.setTextColor(color(ctx, R.color.lec_texto2));
            e.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            r.addView(e, new LinearLayout.LayoutParams(dp(ctx, 88), WRAP));
            TextView v = new TextView(ctx);
            v.setText(f.length > 1 ? f[1] : "");
            v.setTextColor(color(ctx, R.color.lec_texto));
            v.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            r.addView(v, new LinearLayout.LayoutParams(0, WRAP, 1f));
            caja.addView(r);
        }
        cuerpo.addView(caja, fila(ctx, cuerpo.getChildCount() == 0 ? 0 : 12));
        return caja;
    }

    /** Franja ambar con el icono de aviso (p. ej. "Se lo llevo al ultimo cliente leido"). */
    public static TextView aviso(LinearLayout cuerpo, CharSequence s) {
        Context ctx = cuerpo.getContext();
        TextView t = new TextView(ctx);
        t.setText(s);
        t.setTextColor(color(ctx, R.color.lec_aviso_texto));
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setBackgroundResource(R.drawable.bg_lec_aviso);
        t.setGravity(Gravity.CENTER_VERTICAL);
        t.setPadding(dp(ctx, 12), dp(ctx, 10), dp(ctx, 12), dp(ctx, 10));
        t.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_lec_alerta, 0, 0, 0);
        t.setCompoundDrawablePadding(dp(ctx, 10));
        t.setCompoundDrawableTintList(ColorStateList.valueOf(color(ctx, R.color.lec_aviso_texto)));
        cuerpo.addView(t, fila(ctx, cuerpo.getChildCount() == 0 ? 0 : 12));
        return t;
    }

    /** Estado vacio o de informacion: titulo en negrita y detalle, sobre la caja azul clara. */
    public static View estado(LinearLayout cuerpo, String titulo, String detalle) {
        return contexto(cuerpo, titulo, detalle);
    }

    // ---------------------------------------------------------------- campos

    /** Error en linea (rojo, con icono), oculto hasta que se use. */
    public static TextView error(LinearLayout cuerpo) {
        TextView t = nuevoError(cuerpo.getContext());
        cuerpo.addView(t, fila(cuerpo.getContext(), 8));
        return t;
    }

    private static TextView nuevoError(Context ctx) {
        TextView t = new TextView(ctx);
        t.setTextColor(color(ctx, R.color.lec_error));
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setGravity(Gravity.CENTER_VERTICAL);
        t.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_lec_alerta, 0, 0, 0);
        t.setCompoundDrawablePadding(dp(ctx, 8));
        t.setCompoundDrawableTintList(ColorStateList.valueOf(color(ctx, R.color.lec_error)));
        t.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        t.setVisibility(View.GONE);
        return t;
    }

    /** Muestra (o con null/"" oculta) un error en linea suelto. */
    public static void mostrarError(TextView error, CharSequence msg) {
        if (error == null) return;
        boolean hay = msg != null && msg.length() > 0;
        error.setText(hay ? msg : "");
        error.setVisibility(hay ? View.VISIBLE : View.GONE);
    }

    /**
     * Campo de 48dp (como CauCampo: blanco, borde negro, 16sp negrita) con su etiqueta de 13sp negrita encima y el
     * error en linea debajo. Todo va en un grupo que se puede ocultar con {@link #grupo(EditText)}.
     *
     * @param opcional agrega "(opcional)" a la etiqueta.
     * @param max      longitud maxima; 0 = sin limite.
     */
    public static EditText campo(LinearLayout cuerpo, String etiqueta, boolean opcional, String hint, int tipo, int max) {
        final Context ctx = cuerpo.getContext();
        LinearLayout grupo = new LinearLayout(ctx);
        grupo.setOrientation(LinearLayout.VERTICAL);

        LinearLayout filaEtiqueta = new LinearLayout(ctx);
        filaEtiqueta.setOrientation(LinearLayout.HORIZONTAL);
        filaEtiqueta.setGravity(Gravity.BOTTOM);
        TextView lbl = new TextView(ctx);
        SpannableStringBuilder sb = new SpannableStringBuilder(etiqueta);
        sb.setSpan(new StyleSpan(Typeface.BOLD), 0, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (opcional) sb.append(" (opcional)");
        lbl.setText(sb);
        lbl.setTextColor(color(ctx, R.color.lec_texto));
        lbl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        filaEtiqueta.addView(lbl, new LinearLayout.LayoutParams(0, WRAP, 1f));
        TextView lado = new TextView(ctx);
        lado.setTextColor(color(ctx, R.color.lec_texto2));
        lado.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        lado.setVisibility(View.GONE);
        filaEtiqueta.addView(lado, new LinearLayout.LayoutParams(WRAP, WRAP));
        grupo.addView(filaEtiqueta);

        final EditText e = new EditText(ctx);
        e.setBackgroundResource(R.drawable.bg_lec_campo);
        e.setMinHeight(dp(ctx, 48));
        e.setPadding(dp(ctx, 14), dp(ctx, 10), dp(ctx, 14), dp(ctx, 10));
        e.setTextColor(color(ctx, R.color.lec_texto));
        e.setHintTextColor(color(ctx, R.color.lec_texto2));
        e.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        e.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        e.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        if (hint != null) e.setHint(hint);
        e.setInputType(tipo);
        if (max > 0) e.setFilters(new InputFilter[]{new InputFilter.LengthFilter(max)});
        lbl.setLabelFor(View.generateViewId());
        e.setId(lbl.getLabelFor());
        grupo.addView(e, fila(ctx, 4));

        final TextView err = nuevoError(ctx);
        grupo.addView(err, fila(ctx, 6));

        e.setTag(R.id.dlg_error, err);
        e.setTag(R.id.dlg_lado, lado);
        e.setTag(R.id.dlg_grupo, grupo);
        e.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                limpiarError(e);
            }
        });

        cuerpo.addView(grupo, fila(ctx, cuerpo.getChildCount() == 0 ? 4 : 14));
        return e;
    }

    /** Etiqueta, campo y error del campo, para ocultarlos juntos. */
    public static View grupo(EditText campo) {
        Object g = campo.getTag(R.id.dlg_grupo);
        return g instanceof View ? (View) g : campo;
    }

    /** Texto a la derecha de la etiqueta (p. ej. "Intento 2 de 3"). */
    public static void textoLado(EditText campo, CharSequence s) {
        Object o = campo.getTag(R.id.dlg_lado);
        if (!(o instanceof TextView)) return;
        TextView lado = (TextView) o;
        lado.setText(s);
        lado.setVisibility(s == null || s.length() == 0 ? View.GONE : View.VISIBLE);
    }

    /** Contador "25 / 120" a la derecha de la etiqueta. */
    public static void conContador(final EditText campo, final int max) {
        textoLado(campo, campo.length() + " / " + max);
        campo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                textoLado(campo, s.length() + " / " + max);
            }
        });
    }

    /** Error en linea bajo el campo y borde rojo; si el campo no lo armo este helper, usa setError. */
    public static void mostrarError(EditText campo, CharSequence msg) {
        if (campo == null) return;
        Object o = campo.getTag(R.id.dlg_error);
        if (!(o instanceof TextView)) {
            campo.setError(msg);
            return;
        }
        mostrarError((TextView) o, msg);
        campo.setBackgroundResource(R.drawable.dlg_campo_error);
        if (campo.isShown()) campo.requestFocus();
    }

    public static void limpiarError(EditText campo) {
        Object o = campo.getTag(R.id.dlg_error);
        if (!(o instanceof TextView) || ((TextView) o).getVisibility() != View.VISIBLE) return;
        mostrarError((TextView) o, null);
        campo.setBackgroundResource(R.drawable.bg_lec_campo);
    }

    // ---------------------------------------------------------------- opciones

    /**
     * Selector de dos opciones (segmentado, como los criterios de la busqueda). Llama a alCambiar con 0 o 1 al
     * armarlo (opcion 0) y en cada cambio.
     */
    public static RadioGroup segmentado(LinearLayout cuerpo, String opcion0, String opcion1, final IntConsumer alCambiar) {
        Context ctx = cuerpo.getContext();
        RadioGroup g = new RadioGroup(ctx);
        g.setOrientation(RadioGroup.HORIZONTAL);
        final RadioButton a = opcionSegmento(ctx, opcion0);
        final RadioButton b = opcionSegmento(ctx, opcion1);
        RadioGroup.LayoutParams lpa = new RadioGroup.LayoutParams(0, dp(ctx, 48), 1f);
        lpa.setMarginEnd(dp(ctx, 8));
        g.addView(a, lpa);
        g.addView(b, new RadioGroup.LayoutParams(0, dp(ctx, 48), 1f));
        g.check(a.getId());
        g.setOnCheckedChangeListener((grupo, id) -> {
            if (alCambiar != null) alCambiar.accept(id == b.getId() ? 1 : 0);
        });
        cuerpo.addView(g, fila(ctx, cuerpo.getChildCount() == 0 ? 0 : 14));
        if (alCambiar != null) alCambiar.accept(0);
        return g;
    }

    private static RadioButton opcionSegmento(Context ctx, String texto) {
        RadioButton r = new RadioButton(ctx);
        r.setId(View.generateViewId());
        r.setButtonDrawable(null);
        r.setBackgroundResource(R.drawable.bg_bus_chip);
        r.setTextColor(ContextCompat.getColorStateList(ctx, R.color.bus_chip_texto));
        r.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        r.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        r.setGravity(Gravity.CENTER);
        r.setPadding(dp(ctx, 8), 0, dp(ctx, 8), 0);
        r.setText(texto);
        return r;
    }

    /** Opcion de una lista (RadioButton de 48dp, 16sp) para un RadioGroup vertical. */
    public static RadioButton opcionLista(Context ctx, String texto) {
        RadioButton r = new RadioButton(ctx);
        r.setText(texto);
        r.setTextColor(color(ctx, R.color.lec_texto));
        r.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        r.setMinHeight(dp(ctx, 48));
        r.setButtonTintList(ColorStateList.valueOf(color(ctx, R.color.lec_primario)));
        r.setPadding(dp(ctx, 8), 0, 0, 0);
        return r;
    }

    // ---------------------------------------------------------------- mensajes de la oficina

    /** Tarjeta de un mensaje: chip "Nuevo", asunto en negrita, "nombre · fecha" y el texto. */
    public static View tarjetaMensaje(LinearLayout cuerpo, boolean nuevo, String asunto, String meta, String texto) {
        Context ctx = cuerpo.getContext();
        LinearLayout t = new LinearLayout(ctx);
        t.setOrientation(LinearLayout.VERTICAL);
        t.setBackgroundResource(nuevo ? R.drawable.dlg_tarjeta_nueva : R.drawable.bg_lec_tarjeta);
        t.setPadding(dp(ctx, 14), dp(ctx, 12), dp(ctx, 14), dp(ctx, 12));

        LinearLayout cab = new LinearLayout(ctx);
        cab.setOrientation(LinearLayout.HORIZONTAL);
        cab.setGravity(Gravity.CENTER_VERTICAL);
        if (nuevo) {
            TextView chip = new TextView(ctx);
            chip.setText("Nuevo");
            chip.setTextColor(color(ctx, R.color.lec_banner_nuevo_texto));
            chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            chip.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            chip.setBackgroundResource(R.drawable.bg_lec_chip_aviso);
            chip.setPadding(dp(ctx, 10), dp(ctx, 2), dp(ctx, 10), dp(ctx, 2));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(WRAP, WRAP);
            lp.setMarginEnd(dp(ctx, 8));
            cab.addView(chip, lp);
        }
        TextView as = new TextView(ctx);
        as.setText(asunto);
        as.setTextColor(color(ctx, R.color.lec_texto));
        as.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        as.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        cab.addView(as, new LinearLayout.LayoutParams(0, WRAP, 1f));
        t.addView(cab);

        TextView m = new TextView(ctx);
        m.setText(meta);
        m.setTextColor(color(ctx, R.color.lec_texto2));
        m.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        t.addView(m, fila(ctx, 4));

        TextView x = new TextView(ctx);
        x.setText(texto);
        x.setTextColor(color(ctx, R.color.lec_texto));
        x.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        x.setLineSpacing(0, 1.15f);
        t.addView(x, fila(ctx, 6));

        cuerpo.addView(t, fila(ctx, cuerpo.getChildCount() == 0 ? 0 : 10));
        return t;
    }

    // ---------------------------------------------------------------- botones

    /** Pinta en rojo (accion que descarta) un boton del dialogo ya mostrado. */
    public static void peligro(AlertDialog dlg, int cual) {
        Button b = dlg.getButton(cual);
        if (b == null) return;
        b.setBackgroundResource(R.drawable.dlg_boton_peligro);
        b.setTextColor(color(dlg.getContext(), R.color.lec_tecla_texto));
    }
}
