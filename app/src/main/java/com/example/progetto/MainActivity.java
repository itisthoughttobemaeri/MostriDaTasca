package com.example.progetto;

import androidx.annotation.NonNull;
import androidx.annotation.Px;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.android.volley.RequestQueue;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.UiSettings;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, Style.OnStyleLoaded, OnSuccessListener<Location> {
    // Constant used to identify the number of the permission asked
    private static final int MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 1;
    private MapView mapView;
    private MapboxMap mapboxMap;

    private LocationCallback locationCallback;
    private boolean requestingLocationUpdates = true;

    // Variable used for current location

    private FusedLocationProviderClient fusedLocationClient;

    private Location lastLocationUpdate;
    private Location lastKnownLocation;

    private RequestQueue requestQueue;
    private SharedPreferences.Editor editor;
    private LocationComponent locationComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, "pk.eyJ1Ijoidml0YWxlZWMiLCJhIjoiY2szNzBpZmxxMDZ3cjNoamxtemlkY3hoaCJ9.a_b71-bIkpNdQklD3mTKFw");
        setContentView(R.layout.activity_main);

        //Verifying if the user has given the permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("Permission", "Permissions were not asked");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
        }
        Log.d("Permission", "Permissions are granted");

        //Retrieve last current position, in case current position is not working
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    //Current position is not working, setting camera on last location
                    Log.d("Location update", "Setting last position, no new locations received");
                    CameraPosition position = new CameraPosition.Builder()
                            .target(new LatLng(45.283828,09.105340))
                            .zoom(16)
                            .tilt(60)
                            .build();
                    Log.d("Camera Position", "Map setted on last location");
                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
                    return;
                }
                for (Location l : locationResult.getLocations()) {
                    Log.d("Location update", "New location received" + l.toString());
                    lastLocationUpdate = l;
                }
            }
        };

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        FabSpeedDial fabSpeedDial = (FabSpeedDial) findViewById(R.id.fab_menu);
        fabSpeedDial.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                //menuItems
                if (R.id.fab_ranking == menuItem.getItemId()) {
                    Log.d("FabMenu", "Ranking clicked");
                    RankingFragment rankingFragment = new RankingFragment();
                    Log.d("FabMenu", "Ranking fragment created");
                    addFragment(rankingFragment);
                }
                else if (R.id.fab_user == menuItem.getItemId()) {
                    Log.d("FabMenu", "User clicked");
                    // TO DO: User fragment
                }
                else {
                    Log.d("FabMenu", "Other clicked");
                    // TO DO: Info application fragment
                }
                return true;
            }
        });

    }

    @Override
    public void onMapReady(@NonNull MapboxMap MBMap) {
        mapboxMap = MBMap;
        CameraPosition currentPosition = new CameraPosition.Builder()
                .target(new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude()))
                .zoom(16)
                .tilt(60)
                .build();
        Log.d("MapReady", "Map is ready and setted on current location");
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(currentPosition));
        mapboxMap.setStyle(Style.LIGHT, this);
    }

    @Override
    public void onStyleLoaded(@NonNull Style style) {
        // Map is set up and the style has loaded. Now you can add data or make other map adjustments.
        Log.d("StyleLoaded", "Style adjustments with UiSettings done");
        enableLocationComponent(style);
        UiSettings uiSettings = mapboxMap.getUiSettings();
        uiSettings.setCompassEnabled(true);
        uiSettings.setCompassFadeFacingNorth(false);
        uiSettings.setAllGesturesEnabled(true);
        uiSettings.setLogoGravity(Gravity.CENTER|Gravity.BOTTOM);
    }

    private void enableLocationComponent(Style style) {
        LocationComponent locationComponent = mapboxMap.getLocationComponent();
        // Location component options are used to style the user location
        LocationComponentOptions locationComponentOptions = LocationComponentOptions.builder(getApplicationContext())
                .bearingTintColor(Color.blue(5))
                .build();

        LocationComponentActivationOptions locationComponentActivationOptions = LocationComponentActivationOptions
                .builder(getApplicationContext(), style)
                .locationComponentOptions(locationComponentOptions)
                .build();

        locationComponent.activateLocationComponent(locationComponentActivationOptions);
        locationComponent.setLocationComponentEnabled(true);
        locationComponent.setCameraMode(CameraMode.TRACKING_GPS_NORTH);
        locationComponent.setRenderMode(RenderMode.COMPASS);
    }

    // Method used to request user permission

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permission, int[] grantResults) {
        switch (requestCode) {
            // Switch could be avoided since we have just a permission request
            case MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Location Request", "Now the permission is granted");
                    // Does nothing and returns to the onCreate method
                } else {
                    Log.d("Location Request", "Permission is not granted");
                    // TO DO: Close the application or show a message, dialog fragment
                }
                return;
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        if (requestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    // Method to receive location updates

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
        requestingLocationUpdates = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }


    @Override
    public void onSuccess(Location location) {
        if (location != null) {
            Log.d("Last Location", "Last known location" + location.toString());
            this.lastKnownLocation = location;
        } else {
            Log.d("Last Location", "Last known location not available (Duomo di Milano)");
            this.lastKnownLocation = new Location("");
            this.lastKnownLocation.setLatitude(45.464211);
            this.lastKnownLocation.setLongitude(9.191383);
        }
    }

    public void onClickCenter(View view) {
        Log.d("Center", "Center method called");
        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(lastLocationUpdate.getLatitude(), lastLocationUpdate.getLongitude()))
                .zoom(16)
                .tilt(60)
                .build();
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
        Log.d("Center", "Centered on user");
    }

    public void addFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.layout);
        frameLayout.removeAllViews();
        fragmentTransaction.replace(R.id.layout, fragment).addToBackStack(null).commit();
        Log.d("Fragment", "Fragment added");
    }
}