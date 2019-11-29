package com.example.progetto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
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


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.MalformedInputException;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class MainActivity extends AppCompatActivity{
    // Constant used to identify the number of the permission asked
    private static final int MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 1;
    private MapView mapView;
    private MapboxMap mapboxMap;

    private LocationCallback locationCallback;
    private boolean requestingLocationUpdates = true;

    // Variable used for current location
    private FusedLocationProviderClient fusedLocationClient;

    //private Location lastKnownLocation;

    private RequestQueue requestQueue;
    private SharedPreferences.Editor editor;
    private LocationComponent locationComponent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, "pk.eyJ1Ijoidml0YWxlZWMiLCJhIjoiY2szNzBpZmxxMDZ3cjNoamxtemlkY3hoaCJ9.a_b71-bIkpNdQklD3mTKFw");
        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        //Verifying if the user has given the permissions

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Permission were never asked
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
        }

        //Permission are granted
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //Retrieve last current position, in case current position is not working
        //fusedLocationClient.getLastLocation().addOnSuccessListener(this, this); //TO DO: se errore, milano

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    //Current position is not working, setting camera on last location
                    Log.d("Location update", "Setting last position, no new locations received");
                    CameraPosition position = new CameraPosition.Builder()
                            .target(new LatLng(45.283828,09.105340))
                            .zoom(15)
                            .tilt(30)
                            .build();
                    Log.d("Camera Position", "Map setted on last location");
                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Log.d("Location update", "New location received" + location.toString());
                }
            }
        };

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap MBMap) {
                mapboxMap = MBMap;
                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        // Map is set up and the style has loaded. Now you can add data or make other map adjustments.
                        // TO DO: change style (?)
                        enableLocationComponent(style);
                    }
                });
            }
        });
    }

    private void enableLocationComponent(Style style) {
        LocationComponent locationComponent = mapboxMap.getLocationComponent();

        // Location component options are used to style the user location
        LocationComponentOptions locationComponentOptions = LocationComponentOptions.builder(getApplicationContext())
                //.layerBelow(layerId)                                                                             // TO DO: play with layers (2, current pos e monster)
                .bearingTintColor(Color.blue(5))
                .build();

        LocationComponentActivationOptions locationComponentActivationOptions = LocationComponentActivationOptions
                .builder(getApplicationContext(), style)
                .locationComponentOptions(locationComponentOptions)
                .build();

        locationComponent.activateLocationComponent(locationComponentActivationOptions);

        // Enable to make component visible
        locationComponent.setLocationComponentEnabled(true);

        // Set the component's camera mode
        locationComponent.setCameraMode(CameraMode.TRACKING_GPS_NORTH);

        // Set the component's render mode
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
                    // TO DO: Close the application or show a message
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

    //NON USIAMO L'ULTIMA POSIZIONE PERCHE' SE SI SPEGNE IL GPS VIENE RIPULITA LA CACHE E QUINDI NON HO PIU' DATI SULL'ULTIMA POSIZIONE
/*@Override
public void onSuccess(Location location) {
    this.lastKnownLocation = location;
    if (location != null) {
        Log.d("Last Location", "Last known location" + location.toString());
    } else {
        Log.d("Last Location", "Last known location not available");
    }
}*/

}