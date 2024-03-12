package uk.co.arlodev.testapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;
import java.util.function.Consumer;

public class MyLocation {
    LocationManager locationManager;
    Context context;

    public final String[] neededPermissions = new String[] {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    public boolean permissionsMissing() {
        return context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }

    public MyLocation(@NonNull Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    private boolean ValidLocation(Location location) {
        if (location == null) return false;

        long now = Calendar.getInstance().getTimeInMillis();
        long elapsed_ms = now - location.getTime();

        return elapsed_ms <= 5 * 60 * 1_000;
    }

    @SuppressLint("MissingPermission")
    public void getLocation(Consumer<Location> callback) {
        // Never call callback without permissions
        if (permissionsMissing()) return;

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
                null, context.getMainExecutor(), l -> { if (l != null) callback.accept(l); });
    }

    public void getLatLng(Consumer<LatLng> callback) {
        getLocation(l -> callback.accept(new LatLng(l.getLatitude(), l.getLongitude())));
    }

    public void getMyCameraUpdate(Consumer<CameraUpdate> callback) {
        getLatLng(ll -> callback.accept(CameraUpdateFactory.newLatLngZoom(ll, 15)));
    }
}
