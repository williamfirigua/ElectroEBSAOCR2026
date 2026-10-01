package com.gselectroCaqueta.moduloMapas;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

/**
 * Maneja el lanzamiento de navegación turn-by-turn hacia un destino.
 *
 * Prioridad de lanzamiento:
 *   1. Waze (si está instalado)
 *   2. Google Maps (si está instalado)
 *   3. MapsActivity interno como fallback
 *
 * Cuando el usuario inicia la navegación con datos activos, Maps/Waze
 * descarga el tramo de ruta y continúa funcionando sin conexión.
 */
public final class NavigationHelper {

    private static final String PKG_MAPS = "com.google.android.apps.maps";
    private static final String PKG_WAZE = "com.waze";

    // Clase de utilidad — no instanciar
    private NavigationHelper() {}

    /**
     * Punto de entrada principal. Lanza la mejor app de navegación disponible.
     *
     * @param context  contexto de la Activity o Application
     * @param destLat  latitud del destino
     * @param destLon  longitud del destino
     */
    public static void navigateTo(Context context, double destLat, double destLon) {
        if (isPackageInstalled(context, PKG_WAZE)) {
            launchWaze(context, destLat, destLon);
        } else if (isPackageInstalled(context, PKG_MAPS)) {
            launchGoogleMaps(context, destLat, destLon);
        } else {
            launchInApp(context, destLat, destLon);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Lanzadores
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Lanza Google Maps directamente en modo navegación conduciendo (turn-by-turn).
     * El esquema google.navigation:q= evita la pantalla de búsqueda y va directo a la ruta.
     */
    private static void launchGoogleMaps(Context context, double lat, double lon) {
        Uri uri = Uri.parse("google.navigation:q=" + lat + "," + lon + "&mode=w");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage(PKG_MAPS);
        context.startActivity(intent);
    }

    /**
     * Lanza Waze directamente en modo navegación.
     * Si el URI falla (versión vieja de Waze), cae a Google Maps.
     */
    private static void launchWaze(Context context, double lat, double lon) {
        Uri uri = Uri.parse("waze://?ll=" + lat + "," + lon + "&navigate=yes");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage(PKG_WAZE);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            // Waze instalado pero URI no soportado → fallback Maps
            launchGoogleMaps(context, lat, lon);
        }
    }

    /**
     * Fallback in-app: abre MapsActivity cuando ninguna app de navegación está instalada.
     * Muestra ambos marcadores (posición actual y destino) en el mapa propio.
     */
    private static void launchInApp(Context context, double lat, double lon) {
        Toast.makeText(
                context,
                "No se encontró app de navegación. Mostrando mapa.",
                Toast.LENGTH_SHORT
        ).show();

        Intent intent = new Intent(context, MapsActivity.class);
        intent.putExtra(MapsActivity.EXTRA_LAT, String.valueOf(lat));
        intent.putExtra(MapsActivity.EXTRA_LON, String.valueOf(lon));
        context.startActivity(intent);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────

    private static boolean isPackageInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}