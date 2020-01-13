package com.example.progetto;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.LongSparseArray;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.VectorDrawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, Style.OnStyleLoaded, PermissionsListener {
    private MapView mapView;
    private MapboxMap mapboxMap;

    // Variable used for current location

    //private FusedLocationProviderClient fusedLocationClient;

    public Location location;

    private SymbolManager symbolManager;
    private Style mapStyle;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationListeningCallback locationListeningCallback;
    private final long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    private final long MAP_REFRESH_TIME_IN_MILLISECONDS = 10000;

    private Handler handler = new Handler();
    private Runnable runnable;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this, "pk.eyJ1Ijoidml0YWxlZWMiLCJhIjoiY2szNzBpZmxxMDZ3cjNoamxtemlkY3hoaCJ9.a_b71-bIkpNdQklD3mTKFw");
        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        setInformation();

        // With the PermissionsManager class, you can check whether the user has granted location permission
        permissionsManager = new PermissionsManager(this);
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);
        locationListeningCallback = new LocationListeningCallback(this);

        // Checking Internet status
        connectivityManager = (ConnectivityManager)this.getSystemService(this.CONNECTIVITY_SERVICE);
        networkCallback = new ConnectivityManager.NetworkCallback() {
            private InternetDialog internetDialog;

            @Override
            public void onAvailable(@NonNull Network network) {
                // Internet is available, do nothing
                Log.d("Internet", "Available");
                if (internetDialog != null)
                    internetDialog.dismiss();
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                Log.d("Internet", "Lost");
                internetDialog = new InternetDialog();
                internetDialog.show(getSupportFragmentManager(), "dialog");
            }
        };

        connectivityManager.registerDefaultNetworkCallback(networkCallback);


        final ImageView user_button = findViewById(R.id.user_map_image);
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
                if (ranking_button.getVisibility() == View.GONE) {
                    fab.setBackground(getDrawable(R.drawable.ic_button));
                    ranking_button.setVisibility(View.VISIBLE);
                    question_button.setVisibility(View.VISIBLE);
                }
                else {
                    fab.setBackground(getDrawable(R.drawable.ic_add));
                    ranking_button.setVisibility(View.GONE);
                    question_button.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            Log.d("Location", "Permission is granted");
            // Permission sensitive logic called here, such as activating the Maps SDK's LocationComponent to show the device's location
            enableLocationComponent(mapStyle);
        } else {
            Log.d("Location", "Permission is denied");
            PositionDialog positionDialog = new PositionDialog();
            positionDialog.show(getSupportFragmentManager(), "dialog");
        }
    }


    // Class used every time there's a change in the position

    public class LocationListeningCallback implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<MainActivity> activityWeakReference;
        private MainActivity mainActivity;
        LocationListeningCallback(MainActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
            mainActivity = activity;
        }
        @Override
        public void onSuccess(LocationEngineResult result) {
            // The LocationEngineCallback interface's method which fires when the device's location has changed.
            mainActivity.location = result.getLastLocation();
        }
        @Override
        public void onFailure(@NonNull Exception exception) {
            // The LocationEngineCallback interface's method which fires when the device's location can not be captured
        }
    }

    @Override
    public void onMapReady(@NonNull MapboxMap MBMap) {
        mapboxMap = MBMap;
        mapboxMap.setStyle(Style.LIGHT, this);

        CameraPosition currentPosition = new CameraPosition.Builder()
                .target(new LatLng(45.464211, 9.191383))
                .zoom(16)
                .tilt(60)
                .build();
        Log.d("MapReady", "Map is ready and setted on Milan location");
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(currentPosition));
    }

    @Override
    public void onStyleLoaded(@NonNull Style style) {
        mapStyle = style;
        // Map is set up and the style has loaded. Now you can add data or make other map adjustments.
        Log.d("StyleLoaded", "Style adjustments with UiSettings done");

        UiSettings uiSettings = mapboxMap.getUiSettings();
        uiSettings.setAllGesturesEnabled(true);
        uiSettings.setCompassEnabled(false);
        uiSettings.setLogoEnabled(false);
        uiSettings.setAttributionEnabled(false);
        addImagesToStyle(style);
        addObjectsToMap(style);

        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            Log.d("Location", "Permission was granted");
            enableLocationComponent(mapStyle);
        } else {
            Log.d("Location", "Asking for permission");
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }

        // Method to update markers
        callApi();
    }

    private void enableLocationComponent(Style style) {
        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_NO_POWER)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME)
                .build();
        locationEngine.requestLocationUpdates(request, locationListeningCallback, getMainLooper());
        locationEngine.getLastLocation(locationListeningCallback);
        LocationComponent locationComponent = mapboxMap.getLocationComponent();

        LocationComponentOptions locationComponentOptions = LocationComponentOptions.builder(getApplicationContext())
                .bearingTintColor(Color.red(5))
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
        Log.d("Runnable", "Restarting updates");
        handler.post(runnable);
        // Location Engine restarts automatically
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(locationListeningCallback);
        }
        if (handler != null) {
            Log.d("Runnable", "Removing updates");
            handler.removeCallbacks(runnable);
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(locationListeningCallback);
        }
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
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
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(locationListeningCallback);
        }
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    public void onClickCenter(View view) {
        Log.d("Center", "Center method called");
        CameraPosition position = new CameraPosition.Builder()
            .target(new LatLng(location.getLatitude(), location.getLongitude()))
            .zoom(16)
            .tilt(60)
            .build();
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
        Log.d("Center", "Camera centered on user");
    }

    public void addFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        FrameLayout frameLayout = findViewById(R.id.layout);
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
        //double distance = distance(lastLocationUpdate.getLatitude(), lastLocationUpdate.getLongitude(), c.getLat(), c.getLon(), "K" );
        double distance = 0.03;
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

    private void setInformation() {
        TextView textView_xp = findViewById(R.id.xp_points);
        TextView textView_lp = findViewById(R.id.lp_points);
        textView_lp.setText(Model.getInstance().getLP() + "");
        textView_xp.setText(Model.getInstance().getXP() + "");
        ImageView imageView = findViewById(R.id.user_map_image);

        if (Model.getInstance().getImage() == null || Model.getInstance().getImage().equals("null"))
            imageView.setImageResource(R.drawable.ic_student);
        else {
            byte[] byteArray = Base64.decode(Model.getInstance().getImage(), Base64.DEFAULT);
            Bitmap decodedImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            imageView.setImageBitmap(decodedImage);
        }

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
        if (mapObjects != null)
            for (int i = 0; i<mapObjects.length; i++) {
            // Adding id data as json element to each symbol
            JsonObject element = gson.fromJson("{'id': " + mapObjects[i].getId() + "}", JsonObject.class);
            if ("CA".equals(mapObjects[i].getType())) {
                switch (mapObjects[i].getSize()) {
                    case "L" :
                        symbolManager.create(new SymbolOptions()
                                .withLatLng(new LatLng(mapObjects[i].getLat(), mapObjects[i].getLon()))
                                .withIconImage("candy")
                                .withData(element)
                        );
                        break;
                    case "M" :
                        symbolManager.create(new SymbolOptions()
                                .withLatLng(new LatLng(mapObjects[i].getLat(), mapObjects[i].getLon()))
                                .withIconImage("candy3")
                                .withData(element)
                        );
                        break;
                    case "S" :
                        symbolManager.create(new SymbolOptions()
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
                        symbolManager.create(new SymbolOptions()
                                .withLatLng(new LatLng(mapObjects[i].getLat(), mapObjects[i].getLon()))
                                .withIconImage("dragon")
                                .withData(element)
                        );
                        break;
                    case "M":
                        symbolManager.create(new SymbolOptions()
                                .withLatLng(new LatLng(mapObjects[i].getLat(), mapObjects[i].getLon()))
                                .withIconImage("dragonfly")
                                .withData(element)
                        );
                        break;
                    case "S":
                        symbolManager.create(new SymbolOptions()
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
        runnable = new Runnable() {
            @Override
            public void run() {
                Log.d("Runnable", "Running every 10 seconds");
                JsonObjectRequest JSONRequest_data_update = new JsonObjectRequest(
                        Request.Method.POST,
                        url,
                        Model.getInstance().getId(),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d("VolleyJson", "Server is refreshing map objects");
                                // Handle JSON data
                                try {
                                    Gson gson = new Gson();
                                    JSONArray mapObjects = response.getJSONArray("mapobjects");

                                    // JSON data converted into array

                                    ShownObject[] shownObjects = gson.fromJson(mapObjects.toString(), ShownObject[].class);
                                    Log.d("VolleyJson", "[UPDATE ]This is the first object from the server" + shownObjects[0].toString());
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
                                new InternetDialog().show(getSupportFragmentManager(), "dialog");
                            }
                        }
                );
                Model.getInstance().getRequestQueue(getApplicationContext()).add(JSONRequest_data_update);
                handler.postDelayed(this, MAP_REFRESH_TIME_IN_MILLISECONDS);
            }
        };
        handler.post(runnable);
    }
}