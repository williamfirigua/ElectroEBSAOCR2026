package com.gselectroCaqueta.moduloMapas;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

/**
 * Abstrae toda la lógica de ubicación usando FusedLocationProviderClient.
 *
 * Uso típico en una Activity:
 *
 *   LocationHelper locationHelper = new LocationHelper(this);
 *
 *   // Una sola vez (rápido, puede ser null si GPS está frío):
 *   locationHelper.getLastKnownLocation(callback);
 *
 *   // Seguimiento continuo (onResume / onPause):
 *   locationHelper.startLocationUpdates(callback);
 *   locationHelper.stopLocationUpdates();
 */
public final class LocationHelper {

    // ──────────────────────────────────────────────────────────────────────────
    // Interfaz pública de callback
    // ──────────────────────────────────────────────────────────────────────────

    public interface OnLocationResult {
        /** Se llama cuando se obtiene una ubicación válida. */
        void onLocationResolved(Location location);

        /** Se llama cuando no se pudo obtener la ubicación. */
        void onLocationError(String reason);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Constantes de configuración
    // ──────────────────────────────────────────────────────────────────────────

    private static final String TAG = "LocationHelper";

    /** Intervalo deseado entre actualizaciones (5 segundos). */
    private static final long UPDATE_INTERVAL_MS = 5_000L;

    /** Intervalo mínimo aceptable — evita saturar el GPS. */
    private static final long FASTEST_INTERVAL_MS = 2_000L;

    /** Desplazamiento mínimo en metros para emitir un update. */
    private static final float MIN_DISPLACEMENT_METERS = 5f;

    // ──────────────────────────────────────────────────────────────────────────
    // Estado interno
    // ──────────────────────────────────────────────────────────────────────────

    private final FusedLocationProviderClient fusedClient;
    private final Context context;

    /** Referencia al callback de updates continuos para poder removerlo. */
    private com.google.android.gms.location.LocationCallback internalCallback;

    // ──────────────────────────────────────────────────────────────────────────
    // Constructor
    // ──────────────────────────────────────────────────────────────────────────

    public LocationHelper(@NonNull Context context) {
        // ApplicationContext para evitar memory leaks si se guarda la referencia
        this.context     = context.getApplicationContext();
        this.fusedClient = LocationServices.getFusedLocationProviderClient(this.context);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // API pública
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Obtiene la última ubicación conocida por el sistema.
     *
     * Es el método más rápido pero puede retornar null si:
     *  - El GPS nunca se activó en este dispositivo.
     *  - El dispositivo acaba de reiniciarse (GPS frío).
     *
     * En ese caso, combinarlo con startLocationUpdates() para esperar el primer fix.
     */
    public void getLastKnownLocation(@NonNull OnLocationResult callback) {
        if (!hasPermission()) {
            callback.onLocationError("Permiso de ubicación no concedido.");
            return;
        }

        //noinspection MissingPermission — verificado línea arriba
        fusedClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        Log.d(TAG, "Last known: "
                                + location.getLatitude() + ", " + location.getLongitude());
                        callback.onLocationResolved(location);
                    } else {
                        Log.w(TAG, "Last known location es null — GPS frío o desactivado.");
                        callback.onLocationError(
                                "Ubicación no disponible aún. Verificá que el GPS esté activo."
                        );
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener lastLocation", e);
                    callback.onLocationError("Error al obtener ubicación: " + e.getMessage());
                });
    }

    /**
     * Inicia actualizaciones periódicas de ubicación.
     *
     * Llamar en onResume() de la Activity.
     * IMPORTANTE: siempre llamar stopLocationUpdates() en onPause() para evitar
     * consumo de batería innecesario cuando la pantalla no está visible.
     *
     * @param callback receptor de cada nueva ubicación válida.
     */
    public void startLocationUpdates(@NonNull OnLocationResult callback) {
        if (!hasPermission()) {
            callback.onLocationError("Permiso de ubicación no concedido.");
            return;
        }

        // Si ya hay un listener activo, detenerlo primero para evitar duplicados
        stopLocationUpdates();

        LocationRequest request = buildLocationRequest();

        internalCallback = new com.google.android.gms.location.LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                Location location = result.getLastLocation();
                if (location != null) {
                    Log.d(TAG, "Update recibido: "
                            + location.getLatitude() + ", " + location.getLongitude());
                    callback.onLocationResolved(location);
                }
            }
        };

        //noinspection MissingPermission — verificado línea arriba
        fusedClient.requestLocationUpdates(
                request,
                internalCallback,
                Looper.getMainLooper()
        ).addOnFailureListener(e -> {
            Log.e(TAG, "Error al iniciar location updates", e);
            callback.onLocationError("No se pudo iniciar el seguimiento: " + e.getMessage());
        });
    }

    /**
     * Detiene las actualizaciones periódicas de ubicación.
     *
     * Siempre llamar en onPause() o onDestroy() de la Activity.
     * Es seguro llamar este método aunque no haya updates activos.
     */
    public void stopLocationUpdates() {
        if (internalCallback != null) {
            fusedClient.removeLocationUpdates(internalCallback)
                    .addOnSuccessListener(unused ->
                            Log.d(TAG, "Location updates detenidos correctamente."))
                    .addOnFailureListener(e ->
                            Log.w(TAG, "No se pudo detener updates", e));
            internalCallback = null;
        }
    }

    /**
     * Verifica si al menos uno de los permisos de ubicación está concedido.
     * FINE_LOCATION da precisión GPS; COARSE_LOCATION da precisión de red.
     */
    public boolean hasPermission() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers privados
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Construye el LocationRequest.
     *
     * PRIORITY_BALANCED_POWER_ACCURACY: usa red WiFi + GPS.
     * Buen balance entre precisión (~10-30m) y consumo de batería.
     *
     * Si necesitás precisión máxima (campo abierto, trackers), cambiá a:
     *   LocationRequest.PRIORITY_HIGH_ACCURACY
     */
    @SuppressWarnings("deprecation") // LocationRequest.create() compatible con play-services-location 21.x y compileSdk 32
    private LocationRequest buildLocationRequest() {
        return LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_INTERVAL_MS)
                .setSmallestDisplacement(MIN_DISPLACEMENT_METERS);
    }

}