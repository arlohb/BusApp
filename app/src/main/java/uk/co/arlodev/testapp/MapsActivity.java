package uk.co.arlodev.testapp;

import android.annotation.SuppressLint;
import android.content.pm.PermissionInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;

import uk.co.arlodev.testapp.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap map;
    Vehicles vehicles;
    MyLocation myLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        uk.co.arlodev.testapp.databinding.ActivityMapsBinding binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        myLocation = new MyLocation(getApplicationContext());

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

    public void myLocation(View v) {
        myLocation.getMyCameraUpdate(cameraUpdate -> map.animateCamera(cameraUpdate));
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

        if (myLocation.permissionsMissing()) {
            requestPermissions(myLocation.neededPermissions, PermissionInfo.PROTECTION_NORMAL);
            return;
        }
        map.setMyLocationEnabled(true);
        myLocation.getMyCameraUpdate(cameraUpdate -> map.animateCamera(cameraUpdate));
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (myLocation.permissionsMissing()) {
            return;
        }
        map.setMyLocationEnabled(true);
        myLocation.getMyCameraUpdate(cameraUpdate -> map.animateCamera(cameraUpdate));
    }
}