package com.gselectroCaqueta.moduloMapas;


import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.gselectroCaqueta.accesoyseguridad.R;

/**
 * Muestra en el mapa:
 *  - Marcador rojo: destino recibido por extras (lat/lon de la cuenta).
 *  - Marcador azul: posición actual del dispositivo.
 *
 * El FAB "Navegar" lanza Maps o Waze en modo turn-by-turn hacia el destino.
 * Si no hay app de navegación instalada, permanece en esta pantalla.
 *
 * Extras requeridos:
 *   EXTRA_LAT → String con la latitud del destino
 *   EXTRA_LON → String con la longitud del destino
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    // ──────────────────────────────────────────────────────────────────────────
    // Constantes
    // ──────────────────────────────────────────────────────────────────────────

    private static final String TAG = "MapsActivity";

    /** Keys para los extras del Intent. */
    public static final String EXTRA_LAT = "lat";
    public static final String EXTRA_LON = "lon";

    /** Request code para el diálogo de permisos. */
    private static final int PERMISSION_REQUEST_CODE = 100;

    /** Zoom aplicado cuando solo se muestra un marcador. */
    private static final float DEFAULT_ZOOM = 16f;

    /** Padding en px alrededor de los bounds cuando se muestran dos marcadores. */
    private static final int BOUNDS_PADDING_PX = 150;

    // ──────────────────────────────────────────────────────────────────────────
    // Estado
    // ──────────────────────────────────────────────────────────────────────────

    private GoogleMap mMap;
    private LocationHelper locationHelper;

    private Marker markerCurrent;
    private Marker markerDestination;

    /** Coordenadas del destino, parseadas una sola vez en onCreate(). */
    private LatLng destination;

    // ──────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Parsear coordenadas del destino antes de continuar
        if (!parseDestinationExtras()) {
            Toast.makeText(this, "Coordenadas de destino inválidas.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        locationHelper = new LocationHelper(this);

        // Inicializar mapa
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // FAB de navegación
        View btnNavigate = findViewById(R.id.btn_navigate);
        if (btnNavigate != null) {
            btnNavigate.setOnClickListener(v ->
                    NavigationHelper.navigateTo(this, destination.latitude, destination.longitude)
            );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Actualizaciones continuas mientras el mapa está visible
        if (mMap != null && locationHelper.hasPermission()) {
            startContinuousTracking();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // CRÍTICO: detener updates para no drenar la batería en background
        locationHelper.stopLocationUpdates();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Mapa
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        // Colocar destino inmediatamente (no depende de permisos)
        placeDestinationMarker();

        // Resolver ubicación actual
        resolveCurrentLocation();
    }

    /**
     * Coloca el marcador rojo en el destino y centra la cámara.
     * Se ejecuta apenas el mapa está listo, sin esperar la ubicación actual.
     */
    private void placeDestinationMarker() {
        if (markerDestination != null) markerDestination.remove();

        markerDestination = mMap.addMarker(new MarkerOptions()
                .position(destination)
                .title("Destino")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        // Centra en destino mientras carga la ubicación actual
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destination, DEFAULT_ZOOM));
    }

    /**
     * Coloca o actualiza el marcador azul con la posición actual.
     * Si ya existe el marcador de destino, ajusta la cámara para mostrar ambos.
     */
    private void placeCurrentLocationMarker(Location location) {
        LatLng current = new LatLng(location.getLatitude(), location.getLongitude());

        if (markerCurrent != null) markerCurrent.remove();

        markerCurrent = mMap.addMarker(new MarkerOptions()
                .position(current)
                .title("Mi ubicación")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        fitBothMarkersInView();
    }

    /**
     * Ajusta la cámara para que ambos marcadores quepan en pantalla con padding.
     */
    private void fitBothMarkersInView() {
        if (markerCurrent == null || markerDestination == null) return;

        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(markerCurrent.getPosition())
                .include(markerDestination.getPosition())
                .build();

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, BOUNDS_PADDING_PX));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Ubicación
    // ──────────────────────────────────────────────────────────────────────────

    private void resolveCurrentLocation() {
        if (!locationHelper.hasPermission()) {
            requestLocationPermission();
            return;
        }

        showProgress(true);

        // Primero intenta la última ubicación conocida (respuesta inmediata)
        locationHelper.getLastKnownLocation(new LocationHelper.OnLocationResult() {
            @Override
            public void onLocationResolved(Location location) {
                showProgress(false);
                placeCurrentLocationMarker(location);
                // Continúa con updates en vivo para seguir el movimiento
                startContinuousTracking();
            }

            @Override
            public void onLocationError(String reason) {
                showProgress(false);
                Log.w(TAG, "Last known falló: " + reason + ". Esperando primer fix GPS...");
                // Si last known es null, esperamos el primer fix por updates continuos
                startContinuousTracking();
            }
        });
    }

    /**
     * Inicia el seguimiento continuo para actualizar el marcador azul en tiempo real.
     */
    private void startContinuousTracking() {
        locationHelper.startLocationUpdates(new LocationHelper.OnLocationResult() {
            @Override
            public void onLocationResolved(Location location) {
                placeCurrentLocationMarker(location);
            }

            @Override
            public void onLocationError(String reason) {
                Log.e(TAG, "Error en tracking continuo: " + reason);
            }
        });
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Permisos
    // ──────────────────────────────────────────────────────────────────────────

    private void requestLocationPermission() {
        // Solicitar FINE y COARSE en una sola llamada con un único requestCode
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                resolveCurrentLocation();
            } else {
                Toast.makeText(this,
                        "Se necesita el permiso de ubicación para mostrar tu posición.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Parsea las coordenadas del destino desde los extras del Intent.
     * @return false si los extras son nulos, vacíos o no son números válidos.
     */
    private boolean parseDestinationExtras() {
        String latStr = getIntent().getStringExtra(EXTRA_LAT);
        String lonStr = getIntent().getStringExtra(EXTRA_LON);

        if (latStr == null || lonStr == null || latStr.trim().isEmpty() || lonStr.trim().isEmpty()) {
            Log.e(TAG, "Extras de coordenadas nulos o vacíos.");
            return false;
        }

        try {
            double lat = Double.parseDouble(latStr.trim());
            double lon = Double.parseDouble(lonStr.trim());
            destination = new LatLng(lat, lon);
            return true;
        } catch (NumberFormatException e) {
            Log.e(TAG, "Coordenadas inválidas: lat=" + latStr + " lon=" + lonStr, e);
            return false;
        }
    }

    /** Muestra u oculta el spinner de carga de ubicación. */
    private void showProgress(boolean show) {
        View progress = findViewById(R.id.progress_location);
        if (progress != null) {
            progress.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}

