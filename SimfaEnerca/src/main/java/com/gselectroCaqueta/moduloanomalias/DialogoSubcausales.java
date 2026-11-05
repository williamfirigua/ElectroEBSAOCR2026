package com.gselectroCaqueta.moduloanomalias;

import android.app.Activity;

import androidx.appcompat.app.AlertDialog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Subcausales de la causal elegida. Las define la oficina en la web (Causales &gt; editar) y llegan en
 * DATOSDEENTRADA/SUBCAUSAS.TXT, una por línea: {@code codigo_causal|DESCRIPCION|}.
 * <p>
 * Si la causal tiene subcausales, el técnico escoge una y su descripción queda como informe de la lectura (el
 * mismo campo INFORME de 30 caracteres que se escribía a mano). Sin subcausales (o sin el archivo) se pide el
 * informe escrito como siempre. Cancelar (o atrás) cancela la causal, igual que cancelar el informe.
 */
public final class DialogoSubcausales {

    public static final String ARCHIVO = "SUBCAUSAS.TXT";

    public interface Callback {
        /** @param descripcion ya normalizada para el informe (mayúsculas, sin tildes, hasta 30). */
        void alElegir(String descripcion);

        void alCancelar();
    }

    private DialogoSubcausales() {
    }

    /** Descripciones de la causal, en el orden del archivo. Vacía si no tiene o no se puede leer. */
    public static List<String> de(File dirEntrada, String codigoCausal) {
        List<String> r = new ArrayList<>();
        String buscado = AnomaliaDeNoLectura.normalizarCodigo(codigoCausal == null ? "" : codigoCausal);
        if (buscado.isEmpty()) return r;
        File f = new File(dirEntrada, ARCHIVO);
        if (!f.isFile()) return r;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = in.readLine()) != null) {
                if (linea.startsWith("﻿")) linea = linea.substring(1);
                String[] c = linea.split("\\|", -1);
                if (c.length < 2) continue;
                if (!AnomaliaDeNoLectura.normalizarCodigo(c[0].trim()).equals(buscado)) continue;
                String d = DialogoInforme.normalizar(c[1]);
                if (!d.isEmpty() && !r.contains(d)) r.add(d);
            }
        } catch (Exception ignorado) {
            // sin subcausales: se pide el informe escrito
        }
        return r;
    }

    public static void mostrar(Activity activity, String titulo, final List<String> subcausales, final Callback callback) {
        final String[] items = subcausales.toArray(new String[0]);
        final boolean[] resuelto = {false};
        AlertDialog d = new AlertDialog.Builder(activity)
                .setTitle(titulo)
                .setItems(items, (dialog, which) -> {
                    resuelto[0] = true;
                    callback.alElegir(items[which]);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    resuelto[0] = true;
                    callback.alCancelar();
                })
                .create();
        d.setOnCancelListener(dialog -> {
            if (!resuelto[0]) {
                resuelto[0] = true;
                callback.alCancelar();
            }
        });
        d.setCanceledOnTouchOutside(false);
        d.show();
    }
}
