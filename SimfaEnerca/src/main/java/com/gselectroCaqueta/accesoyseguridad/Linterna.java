package com.gselectroCaqueta.accesoyseguridad;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;

/**
 * Linterna del equipo (flash de la cámara trasera) con CameraManager.setTorchMode: no abre la cámara, así
 * que no choca con la app de cámara ni con CamaraLecturaActivity. Aun así se apaga antes de abrir la cámara
 * (la cámara toma el flash y la linterna quedaría en un estado inconsistente) y al salir de la pantalla.
 * <p>
 * El estado real lo informa el sistema (TorchCallback): si otra app o la cámara la apaga, el botón lo refleja.
 */
public final class Linterna {

    public interface Observador {
        void cambio(boolean encendida);
    }

    private final CameraManager camaras;
    private final String idCamara;
    private final Observador observador;
    private boolean encendida;

    private final CameraManager.TorchCallback callback = new CameraManager.TorchCallback() {
        @Override
        public void onTorchModeChanged(String cameraId, boolean enabled) {
            if (cameraId.equals(idCamara)) {
                encendida = enabled;
                if (observador != null) observador.cambio(enabled);
            }
        }

        @Override
        public void onTorchModeUnavailable(String cameraId) {
            if (cameraId.equals(idCamara)) {
                encendida = false;
                if (observador != null) observador.cambio(false);
            }
        }
    };

    public Linterna(Context ctx, Observador observador) {
        this.camaras = (CameraManager) ctx.getSystemService(Context.CAMERA_SERVICE);
        this.idCamara = buscarCamaraConFlash(camaras);
        this.observador = observador;
        if (camaras != null && idCamara != null) {
            camaras.registerTorchCallback(callback, null);
        }
    }

    public boolean disponible() {
        return idCamara != null;
    }

    public boolean encendida() {
        return encendida;
    }

    /** @return false si el equipo no tiene flash o el sistema no lo dejó cambiar (cámara en uso). */
    public boolean alternar() {
        return poner(!encendida);
    }

    public void apagar() {
        if (encendida) poner(false);
    }

    /** Apaga y deja de escuchar. Llamar en onDestroy. */
    public void liberar() {
        apagar();
        if (camaras != null && idCamara != null) {
            camaras.unregisterTorchCallback(callback);
        }
    }

    private boolean poner(boolean on) {
        if (camaras == null || idCamara == null) return false;
        try {
            camaras.setTorchMode(idCamara, on);
            encendida = on;
            return true;
        } catch (CameraAccessException | IllegalArgumentException e) {
            return false;
        }
    }

    private static String buscarCamaraConFlash(CameraManager m) {
        if (m == null) return null;
        try {
            String alternativa = null;
            for (String id : m.getCameraIdList()) {
                CameraCharacteristics c = m.getCameraCharacteristics(id);
                Boolean flash = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                if (flash == null || !flash) continue;
                Integer lente = c.get(CameraCharacteristics.LENS_FACING);
                if (lente != null && lente == CameraCharacteristics.LENS_FACING_BACK) return id;
                if (alternativa == null) alternativa = id;
            }
            return alternativa;
        } catch (CameraAccessException | RuntimeException e) {
            return null;
        }
    }
}
