package com.example.progetto;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.LongSparseArray;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import android.graphics.Canvas;
import android.graphics.Color;


import android.graphics.drawable.VectorDrawable;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.print.PrintAttributes;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, Style.OnStyleLoaded, OnSuccessListener<Location> {
    private MapView mapView;
    private MapboxMap mapboxMap;

    // Constant used to identify the number of the permission asked
    private static final int MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 1;

    private LocationCallback locationCallback;
    private boolean requestingLocationUpdates = true;

    // Variable used for current location

    private FusedLocationProviderClient fusedLocationClient;

    private Location lastLocationUpdate;
    private Location lastKnownLocation;

    private SharedPreferences.Editor editor;
    private LocationComponent locationComponent;

    private SymbolManager symbolManager;
    private Style mapStyle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, "pk.eyJ1Ijoidml0YWxlZWMiLCJhIjoiY2szNzBpZmxxMDZ3cjNoamxtemlkY3hoaCJ9.a_b71-bIkpNdQklD3mTKFw");
        setContentView(R.layout.activity_main);

        // Verifying if the user has given the permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("LocationPermission", "Permissions were not asked");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
        }
        Log.d("LocationPermission", "Permissions are granted");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Retrieve last current position, in case current position is not working
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    // Current position is not working, setting camera on last location
                    Log.d("LocationUpdate", "Setting last position, no new locations received");
                    CameraPosition position = new CameraPosition.Builder()
                            .target(new LatLng(45.283828, 09.105340))
                            .zoom(16)
                            .tilt(60)
                            .build();
                    Log.d("CameraPosition", "Map setted on last location");
                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
                    return;
                }
                for (Location l : locationResult.getLocations()) {
                    Log.d("LocationUpdate", "New location received" + l.toString());
                    lastLocationUpdate = l;
                }
            }
        };

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        setPointsInformation();

        final Button user_button = findViewById(R.id.user_button);
        user_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("FabMenu", "User clicked");
                UserFragment userFragment = new UserFragment();
                Log.d("FabMenu", "User fragment created");
                addFragment(userFragment);
            }
        });

        final Button ranking_button = findViewById(R.id.ranking_button);
        ranking_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("FabMenu", "Ranking clicked");
                RankingFragment rankingFragment = new RankingFragment();
                Log.d("FabMenu", "Ranking fragment created");
                addFragment(rankingFragment);
            }
        });

        final Button question_button = findViewById(R.id.question_button);
        question_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("FabMenu", "Info clicked");
                InfoFragment infoFragment = new InfoFragment();
                Log.d("FabMenu", "Info fragment created");
                addFragment(infoFragment);
            }
        });


        final Button fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("FabMenu", "Clicked");
                if (user_button.getVisibility() == View.GONE) {
                    fab.setBackground(getDrawable(R.drawable.ic_button));
                    user_button.setVisibility(View.VISIBLE);
                    ranking_button.setVisibility(View.VISIBLE);
                    question_button.setVisibility(View.VISIBLE);
                }
                else {
                    fab.setBackground(getDrawable(R.drawable.ic_add));
                    user_button.setVisibility(View.GONE);
                    ranking_button.setVisibility(View.GONE);
                    question_button.setVisibility(View.GONE);
                }
            }
        });

    }

    // Method used to request user permission

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permission, int[] grantResults) {
        switch (requestCode) {
            // Switch could be avoided since we have just a permission request
            case MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("LocationRequest", "Now the permission is granted");
                } else {
                    Log.d("LocationRequest", "Permission is not granted");
                    // TODO: Close the application or show a message, dialog fragment
                }
                return;
            }
        }
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
        mapStyle = style;
        // Map is set up and the style has loaded. Now you can add data or make other map adjustments.
        Log.d("StyleLoaded", "Style adjustments with UiSettings done");
        enableLocationComponent(style);
        UiSettings uiSettings = mapboxMap.getUiSettings();
        uiSettings.setAllGesturesEnabled(true);
        uiSettings.setCompassEnabled(true);
        uiSettings.setAttributionGravity(Gravity.CENTER|Gravity.BOTTOM);
        uiSettings.setLogoGravity(Gravity.CENTER|Gravity.BOTTOM);
        addImagesToStyle(style);
        addObjectsToMap(style);

        // Method to update markers
        callApi();
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

    // Method to set last known location when location is not available

    @Override
    public void onSuccess(Location location) {
        if (location != null) {
            Log.d("LastLocation", "Last known location" + location.toString());
            lastKnownLocation = location;
        } else {
            Log.d("LastLocation", "Last known location not available (Duomo di Milano)");
            lastKnownLocation = new Location("");
            lastKnownLocation.setLongitude(9.191383);
            lastKnownLocation.setLatitude(45.464211);
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
            Log.d("Fragment", "Fragment added full");

    }

    // Method found on the internet to create a bitmap (since resource decoder returns null)

    private static Bitmap getBitmap(VectorDrawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }

    public boolean isDistanceObjectOk(int id) {
        ShownObject c = Model.getInstance().getShownObjectById(id);
        double distance = distance(lastLocationUpdate.getLatitude(), lastLocationUpdate.getLongitude(), c.getLat(), c.getLon(), "K" );
        //double distance = 0.03;
        if (distance <= 0.05) {
            return true;
        }
        return false;
    }

    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        }
        else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            if (unit.equals("K")) {
                dist = dist * 1.609344;
            } else if (unit.equals("N")) {
                dist = dist * 0.8684;
            }
            return (dist);
        }
    }

    private void setPointsInformation() {
        TextView textView_xp = findViewById(R.id.xp_points);
        TextView textView_lp = findViewById(R.id.lp_points);
        textView_lp.setText(Model.getInstance().getLP() + "");
        textView_xp.setText(Model.getInstance().getXP() + "");
    }

    private void addImagesToStyle(Style style) {
        // Adding candy images to the style to later show them on the map

        VectorDrawable vectorDrawable = (VectorDrawable) getDrawable(R.drawable.ic_candy);
        Bitmap bitmap = getBitmap(vectorDrawable);
        style.addImage("candy", bitmap);

        vectorDrawable = (VectorDrawable) getDrawable(R.drawable.ic_candy3);
        bitmap = getBitmap(vectorDrawable);
        style.addImage("candy3", bitmap);

        vectorDrawable = (VectorDrawable) getDrawable(R.drawable.ic_candy2);
        bitmap = getBitmap(vectorDrawable);
        style.addImage("candy2", bitmap);

        // Adding monsters images to the style to later show them on the map

        vectorDrawable = (VectorDrawable) getDrawable(R.drawable.ic_octopus);
        bitmap = getBitmap(vectorDrawable);
        style.addImage("octopus", bitmap);

        vectorDrawable = (VectorDrawable) getDrawable(R.drawable.ic_dragon);
        bitmap = getBitmap(vectorDrawable);
        style.addImage("dragon", bitmap);

        vectorDrawable = (VectorDrawable) getDrawable(R.drawable.ic_dragon_fly);
        bitmap = getBitmap(vectorDrawable);
        style.addImage("dragonfly", bitmap);
    }

    private void addObjectsToMap(Style style) {
        // Adding objects to the map

        ShownObject[] mapObjects = Model.getInstance().getShownObjects();
        symbolManager = new SymbolManager(mapView, mapboxMap, style);

        // Allow icons overlap
        symbolManager.setIconAllowOverlap(true);
        symbolManager.getIconIgnorePlacement();
        Log.d("Symbol", "Symbol manager created");

        Gson gson = new Gson();

        // Creating symbols
        for (int i = 0; i<mapObjects.length; i++) {
            // Adding id data as json element to each symbol
            JsonObject element = gson.fromJson("{'id': " + mapObjects[i].getId() + "}", JsonObject.class);
            if ("CA".equals(mapObjects[i].getType())) {
                switch (mapObjects[i].getSize()) {
                    case "L" :
                        Symbol symbol = symbolManager.create(new SymbolOptions()
                                .withLatLng(new LatLng(mapObjects[i].getLat(), mapObjects[i].getLon()))
                                .withIconImage("candy")
                                .withData(element)
                        );
                        break;
                    case "M" :
                        Symbol symbol1 = symbolManager.create(new SymbolOptions()
                                .withLatLng(new LatLng(mapObjects[i].getLat(), mapObjects[i].getLon()))
                                .withIconImage("candy3")
                                .withData(element)
                        );
                        break;
                    case "S" :
                        Symbol symbol2 = symbolManager.create(new SymbolOptions()
                                .withLatLng(new LatLng(mapObjects[i].getLat(), mapObjects[i].getLon()))
                                .withIconImage("candy2")
                                .withData(element)
                        );
                        break;
                }
            }
            else {
                switch (mapObjects[i].getSize()) {
                    case "L":
                        Symbol symbol = symbolManager.create(new SymbolOptions()
                                .withLatLng(new LatLng(mapObjects[i].getLat(), mapObjects[i].getLon()))
                                .withIconImage("dragon")
                                .withData(element)
                        );
                        break;
                    case "M":
                        Symbol symbol1 = symbolManager.create(new SymbolOptions()
                                .withLatLng(new LatLng(mapObjects[i].getLat(), mapObjects[i].getLon()))
                                .withIconImage("dragonfly")
                                .withData(element)
                        );
                        break;
                    case "S":
                        Symbol symbol2 = symbolManager.create(new SymbolOptions()
                                .withLatLng(new LatLng(mapObjects[i].getLat(), mapObjects[i].getLon()))
                                .withIconImage("octopus")
                                .withData(element)
                        );
                        break;
                }
            }
        }

        symbolManager.addClickListener(new OnSymbolClickListener() {
            @Override
            public void onAnnotationClick(Symbol symbol) {
                Log.d("Symbol", "Symbol clicked");
                MapObjectFragment mapObjectFragment = new MapObjectFragment(symbol.getData());
                addFragment(mapObjectFragment);
            }
        });
    }

    private void callApi() {
        final String url = "https://ewserver.di.unimi.it/mobicomp/mostri/getmap.php";
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Log.d("Running", "Running every x millisec");
                JsonObjectRequest JSONRequest_data_update = new JsonObjectRequest(
                        Request.Method.POST,
                        url,
                        Model.getInstance().getId(),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d("VolleyJson", "Server is refreshing");
                                // Handle JSON data
                                try {
                                    Gson gson = new Gson();
                                    JSONArray mapObjects = response.getJSONArray("mapobjects");

                                    // JSON data converted into array

                                    ShownObject[] shownObjects = gson.fromJson(mapObjects.toString(), ShownObject[].class);
                                    Log.d("VolleyJson", "This is the first object from the server (refreshing)" + shownObjects[0].toString());
                                    Model.getInstance().refreshShownObjects(shownObjects);

                                    // Deleting all the symbols

                                    List<Symbol> symbols = new ArrayList<>();
                                    LongSparseArray<Symbol> symbolArray = symbolManager.getAnnotations();
                                    for (int i = 0; i < symbolArray.size(); i++) {
                                        symbols.add(symbolArray.valueAt(i));
                                    }
                                    symbolManager.delete(symbols);

                                    // Re-adding symbols

                                    addObjectsToMap(mapStyle);

                                }
                                catch (JSONException e){
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                                // TO DO: handle error 401 & 400
                            }
                        }
                );
                Model.getInstance().getRequestQueue(getApplicationContext()).add(JSONRequest_data_update);
                handler.postDelayed(this, 10000);
            }
        };
        handler.post(runnable);
    }

}