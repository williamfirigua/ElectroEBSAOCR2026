package com.gselectroCaqueta.accesoyseguridad;

import android.app.Application;

import com.gselectroCaqueta.moduloLicencia.VigilanteLicencia;

/** Aplicación: registra el vigilante de la licencia (revisa en cualquier pantalla mientras la app está al frente). */
public class EbsaApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new VigilanteLicencia());
    }
}
