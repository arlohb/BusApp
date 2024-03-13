package uk.co.arlodev.testapp;

import android.annotation.SuppressLint;
import android.content.pm.PermissionInfo;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;

import androidx.activity.EdgeToEdge;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.co.arlodev.testapp.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap map;
    Vehicles vehicles;
    MyLocation myLocation;
    List<Polygon> polygons = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);

        uk.co.arlodev.testapp.databinding.ActivityMapsBinding binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.constraint), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            MarginLayoutParams mlp = (MarginLayoutParams) v.getLayoutParams();
            mlp.topMargin = insets.top;
            v.setLayoutParams(mlp);

            return WindowInsetsCompat.CONSUMED;
        });

        myLocation = new MyLocation(getApplicationContext());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        vehicles = new Vehicles(() -> {
            runOnUiThread(() -> polygons.forEach(Polygon::remove));

            @ColorInt int fill = getColorAttr(com.google.android.material.R.attr.colorSecondary);

            double scale = 0.0006;
            LatLng[] points = new LatLng[] {
                new LatLng(-1,-2),
                new LatLng(-1,2),
                new LatLng(0,3),
                new LatLng(1,2),
                new LatLng(1,-2),
            };

            for (Vehicle vehicle : vehicles.vehicles) {
                LatLng location = vehicle.vehicleLocation;

                LatLng[] points2 = points.clone();
                for (int i = 0; i < points2.length; i++) {
                    Log.i("MapsActivity", vehicle.recordedAtTime.toString());
                    LatLng point = points2[i];
                    point = rotate(point, vehicle.bearing);
                    point = new LatLng(point.latitude, point.longitude / Math.abs(Math.cos(Math.toRadians(location.latitude))));
                    point = new LatLng(point.latitude * scale, point.longitude * scale);
                    point = new LatLng(point.latitude + location.latitude, point.longitude + location.longitude);
                    points2[i] = point;
                }

                runOnUiThread(() -> {
                    Polygon polygon = map.addPolygon(new PolygonOptions()
                        .addAll(Arrays.asList(points2))
                        .strokeWidth(0)
                        .fillColor(fill)
                        .clickable(true));
                    polygon.setTag(vehicle);
                    polygons.add(polygon);
                });
            }

            runOnUiThread(() -> {
                findViewById(R.id.RefreshProgress).setVisibility(View.INVISIBLE);
                findViewById(R.id.RefreshIcon).setVisibility(View.VISIBLE);
            });

            Log.i("MapsActivity", Integer.toString(vehicles.vehicles.size()));
        });
    }

    LatLng rotate(LatLng point, double degrees) {
        final double x = point.longitude;
        final double y = point.latitude;

        final double theta = Math.toRadians(degrees);
        final double cosT = Math.cos(theta);
        final double sinT = Math.sin(theta);

        final double x2 = x * cosT - y * sinT;
        final double y2 = x * sinT + y * cosT;

        return new LatLng(y2, x2);
    }

    private @ColorInt int getColorAttr(int id) {
        @ColorInt int fill;
        try (TypedArray ta = obtainStyledAttributes(new int[] { id })) {
            fill = ta.getColor(0, 0);
        }
        return fill;
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

        map.setOnPolygonClickListener((polygon) -> {
            Vehicle vehicle = (Vehicle)polygon.getTag();
            if (vehicle == null) return;
            Log.i("MapsActivity", vehicle.lineRef);
            Log.i("MapsActivity", vehicle.recordedAtTime.toString());
        });

        if (myLocation.permissionsMissing()) {
            requestPermissions(myLocation.neededPermissions, PermissionInfo.PROTECTION_NORMAL);
            return;
        }
        map.setMyLocationEnabled(true);
        myLocation.getMyCameraUpdate(map::animateCamera);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (myLocation.permissionsMissing()) {
            return;
        }
        map.setMyLocationEnabled(true);
        myLocation.getMyCameraUpdate(map::animateCamera);
    }
}