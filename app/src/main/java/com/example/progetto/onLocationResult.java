package com.example.progetto;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

public class onLocationResult extends LocationCallback {

    private MapboxMap mapboxMap;
    private Location lastKnownLocation;
    private Location locationUpdate;

    public onLocationResult(MapboxMap mapboxMap, Location location, Location locationUpdate) {
        this.mapboxMap = mapboxMap;
        this.lastKnownLocation = location;
        this.locationUpdate = locationUpdate;
    }

    @Override
    public void onLocationResult(LocationResult locationResult) {
        if (locationResult == null) {
            // Current position is not working, setting camera on last location
            Log.d("LocationUpdate", "Setting last position, no new locations received");
            CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()))
                    .zoom(16)
                    .tilt(60)
                    .build();
            Log.d("CameraPosition", "Map setted on last location");
            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
            return;
        }
        for (Location l : locationResult.getLocations()) {
            Log.d("LocationUpdate", "New location received" + l.toString());
            //setLastLocationUpdate(l);
        }
    }
}
