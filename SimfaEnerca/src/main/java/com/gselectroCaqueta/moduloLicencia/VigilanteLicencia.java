package com.gselectroCaqueta.moduloLicencia;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Vigila la licencia mientras la app está en pantalla, sin importar en qué pantalla esté el técnico.
 *
 * - Cada vez que una pantalla vuelve al frente revisa la licencia guardada (vencimiento, reloj, retiro).
 * - Mientras haya una pantalla al frente, cada CADA_MS consulta al servidor (licencia/estado, una petición
 *   pequeña). Si la oficina liberó o bloqueó el celular, o el técnico quedó inactivo, la licencia se borra y
 *   la app vuelve a la pantalla de licencia.
 * - Excepción: las pantallas de una liquidación en curso (PROTEGIDAS) no se cortan para no dañar un registro;
 *   el retiro se aplica en cuanto el técnico sale a una pantalla que no esté en esa lista.
 *
 * Se registra en EbsaApp (Application).
 */
public final class VigilanteLicencia implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = "VigilanteLicencia";
    /** Consulta al servidor mientras la app está al frente. */
    private static final long CADA_MS = 2 * 60_000L;
    /** Una provisional consulta con activar (más pesado y deja bitácora): más espaciado. */
    private static final long CADA_PROVISIONAL_MS = 30 * 60_000L;

    /** Pantallas de la liquidación de un registro: no se cortan a mitad. */
    private static final Set<String> PROTEGIDAS = new HashSet<>(Arrays.asList(
            "MenuDeLiquidacion", "CamaraLecturaActivity", "ModuloCaptNovLect", "ModuloFirma",
            "ModuloAdicionarCertificado", "ModuloCambioDigitos", "AlertaCuentaD", "DeviceListActivity"));

    private final Handler ui = new Handler(Looper.getMainLooper());
    private Activity actual;
    private long ultimoIntento;
    private volatile boolean consultando;

    private final Runnable tic = new Runnable() {
        @Override
        public void run() {
            consultarServidor();
            ui.postDelayed(this, 30_000L);
        }
    };

    @Override
    public void onActivityResumed(Activity a) {
        actual = a;
        ui.removeCallbacks(tic);
        if (a instanceof ActLicencia) return;
        if (!protegida(a) && !LicenciaTerminal.exigir(a)) return;
        ui.post(tic);
    }

    @Override
    public void onActivityPaused(Activity a) {
        if (actual == a) {
            actual = null;
            ui.removeCallbacks(tic);
        }
    }

    private void consultarServidor() {
        final Activity a = actual;
        if (a == null || consultando || !hayRed(a)) return;
        LicenciaTerminal lic = new LicenciaTerminal(a);
        LicenciaTerminal.Revision rv = lic.revisar();
        if (!rv.vigente()) {
            aplicar();
            return;
        }
        long ahora = System.currentTimeMillis();
        long cada = rv.licencia.provisional ? CADA_PROVISIONAL_MS : CADA_MS;
        if (ahora - ultimoIntento < cada && ahora >= ultimoIntento) return;
        ultimoIntento = ahora;
        consultando = true;
        final String base = LicenciaTerminal.baseApi(a);
        final Context app = a.getApplicationContext();
        new Thread(() -> {
            try {
                LicenciaTerminal.Respuesta r = new LicenciaTerminal(app).revisarEnServidor(base);
                if (r.retirada) ui.post(this::aplicar);
            } catch (Throwable t) {
                Log.w(TAG, "revisión: " + t);
            } finally {
                consultando = false;
            }
        }, "licencia-vigilante").start();
    }

    /** La licencia ya no vale: se sale de la pantalla actual salvo que sea de una liquidación en curso. */
    private void aplicar() {
        Activity a = actual;
        if (a == null || a instanceof ActLicencia || protegida(a)) return;
        LicenciaTerminal.exigir(a);
    }

    private static boolean protegida(Activity a) {
        return PROTEGIDAS.contains(a.getClass().getSimpleName());
    }

    private static boolean hayRed(Context c) {
        try {
            ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
            return nc != null && nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    public void onActivityCreated(Activity a, Bundle b) {
    }

    @Override
    public void onActivityStarted(Activity a) {
    }

    @Override
    public void onActivityStopped(Activity a) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity a, Bundle b) {
    }

    @Override
    public void onActivityDestroyed(Activity a) {
    }
}
