package uk.co.arlodev.testapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;
import java.util.function.Consumer;

import uk.co.arlodev.testapp.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap map;
    LocationManager locationManager;
    Vehicles vehicles;

    private final String[] neededPermissions = new String[] {
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private boolean permissionsGranted() {
        return checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        uk.co.arlodev.testapp.databinding.ActivityMapsBinding binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        vehicles = new Vehicles(() -> {
            Log.i("Vehicles", vehicles.xmlStr);
            runOnUiThread(() -> {
                findViewById(R.id.RefreshProgress).setVisibility(View.INVISIBLE);
                findViewById(R.id.RefreshIcon).setVisibility(View.VISIBLE);
            });
        });
    }

    public void refresh(View v) {
        findViewById(R.id.RefreshIcon).setVisibility(View.INVISIBLE);
        findViewById(R.id.RefreshProgress).setVisibility(View.VISIBLE);
        vehicles.reload();
    }

    public boolean ValidLocation(Location location) {
        if (location == null) return false;

        long now = Calendar.getInstance().getTimeInMillis();
        long elapsed_ms = now - location.getTime();

        return elapsed_ms <= 5 * 60 * 1_000;
    }

    @SuppressLint("MissingPermission")
    public void getLocation(Consumer<Location> callback) {
        // Never call callback without permissions
        if (!permissionsGranted()) return;

        // Get last known location
        Location location = locationManager.getLastKnownLocation(LocationManager.FUSED_PROVIDER);

        // If a valid location
        if (ValidLocation(location)) {
            // Call and end
            callback.accept(location);
            return;
        }
        // If null or too old

        // Request a new location,
        // Giving this the callback
        locationManager.getCurrentLocation(LocationManager.FUSED_PROVIDER,
            null, getMainExecutor(), l -> { if (l != null) callback.accept(l); });
    }

    public void getLatLng(Consumer<LatLng> callback) {
        getLocation(l -> callback.accept(new LatLng(l.getLatitude(), l.getLongitude())));
    }

    public void getMyCameraUpdate(Consumer<CameraUpdate> callback) {
        getLatLng(ll -> callback.accept(CameraUpdateFactory.newLatLngZoom(ll, 15)));
    }

    public void myLocation(View v) {
        getMyCameraUpdate(cameraUpdate -> map.animateCamera(cameraUpdate));
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        UiSettings ui = map.getUiSettings();
        ui.setZoomControlsEnabled(false);
        ui.setCompassEnabled(true);
        ui.setMyLocationButtonEnabled(false);
        ui.setRotateGesturesEnabled(false);
        ui.setTiltGesturesEnabled(false);

        if (!permissionsGranted()) {
            requestPermissions(neededPermissions, PermissionInfo.PROTECTION_NORMAL);
            return;
        }
        map.setMyLocationEnabled(true);
        getMyCameraUpdate(cameraUpdate -> map.animateCamera(cameraUpdate));
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (!permissionsGranted()) {
            return;
        }
        map.setMyLocationEnabled(true);
        getMyCameraUpdate(cameraUpdate -> map.animateCamera(cameraUpdate));
    }
}