package com.example.progetto;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public class SplashScreen extends AppCompatActivity {
    private SharedPreferences.Editor editor;
    private Thread myThread;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private InternetDialog internetDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        // Checking Internet status
        connectivityManager = (ConnectivityManager)this.getSystemService(this.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();

        final SharedPreferences sharedPreferences = getSharedPreferences("SharedPreferences", 0);
        editor = sharedPreferences.edit();
        //editor.remove("session_id");
        editor.commit();
        Log.d("SessionID", "Session id is in the shared preferences: " + sharedPreferences.contains("session_id"));

        myThread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(3000);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };


        // The if statement is verifying the 1st execution of the app

        if (!sharedPreferences.contains("session_id")) {
            // First execution
            String url = "https://ewserver.di.unimi.it/mobicomp/mostri/register.php";

            JsonObjectRequest JSONRequest_user_setup = new JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("VolleyJson", "Server is working");
                            // Handle JSON data
                            try {
                                String s = (String) response.get("session_id");
                                Model.getInstance().setId(response);
                                Log.d("VolleyJson", s);
                                editor.putString("session_id", (String) response.get("session_id"));
                                editor.commit();
                                doSetProfile(response.getString("session_id"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            new InternetDialog().show(getSupportFragmentManager(), "dialog");
                            error.printStackTrace();
                            Log.d("Internet", "Should call next: myThread");
                            myThread.start();
                        }
                    }
            );
            Model.getInstance().getRequestQueue(getApplicationContext()).add(JSONRequest_user_setup);
            Log.d("VolleyQueue", "First request added");
        }
        else {
            Log.d("VolleyJson", "Already setted shared preferences");
            String session_id = sharedPreferences.getString("session_id", null);
            try {
                Model.getInstance().setId(new JSONObject("{session_id:" + session_id + "}"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("Internet", "Should call next: myThread");
            myThread.start();
            doGetUserRequest(Model.getInstance().getId());
        }
    }

    // Method used to call the request to initialize the username

    private void doSetProfile(String string) {
        // Json object must be session id
        String url = "https://ewserver.di.unimi.it/mobicomp/mostri/setprofile.php";
        String json = "{'session_id':" + string + ", 'username': 'player', 'img':'null'}";

        try {
            Model.getInstance().setId(new JSONObject("{session_id:" + string + "}"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest JSONRequest_user_update = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("VolleyJson", "Server is working");
                        doGetUserRequest(Model.getInstance().getId());
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
        Model.getInstance().getRequestQueue(getApplicationContext()).add(JSONRequest_user_update);
        Log.d("VolleyQueue", "Set profile request added");
    }


    // Method used to call the request to initialize the objects

    private void doMapRequest(final JSONObject jsonObject) {
        // Json object must be session id
        String url = "https://ewserver.di.unimi.it/mobicomp/mostri/getmap.php";

        JsonObjectRequest JSONRequest_data_download = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("VolleyJson", "Server is working");
                        // Handle JSON data
                        try {
                            //connectivityManager.unregisterNetworkCallback(networkCallback);
                            Gson gson = new Gson();
                            JSONArray mapObjects = response.getJSONArray("mapobjects");
                            // JSON data converted into array
                            ShownObject[] shownObjects = gson.fromJson(mapObjects.toString(), ShownObject[].class);
                            Log.d("VolleyJson", "This is the first object from the server" + shownObjects[0].toString());
                            Model.getInstance().refreshShownObjects(shownObjects);
                            myThread.start();
                        }
                        catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        new InternetDialog().show(getSupportFragmentManager(), "dialog");
                        error.printStackTrace();
                    }
                }
        );

        Model.getInstance().getRequestQueue(getApplicationContext()).add(JSONRequest_data_download);
        Log.d("VolleyQueue", "Map request added");
    }

    // Method used to call the request to save user points

    private void doGetUserRequest(JSONObject jsonObject) {
        // Json object must be session id
        String url = "https://ewserver.di.unimi.it/mobicomp/mostri/getprofile.php";

        JsonObjectRequest JSONRequest_user_data = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                    Log.d("VolleyJson", "Server is working");
                    try {
                        String s = response.getString("username");
                        Log.d("GetUser", response.toString());
                        Model.getInstance().setLP(Integer.parseInt(response.getString("lp")));
                        Model.getInstance().setXP(Integer.parseInt(response.getString("xp")));
                        Model.getInstance().setUsername(response.getString("username"));
                        Log.d("SessionID", "Username from server: " + response.getString("username"));
                        Model.getInstance().setImage(response.getString("img"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    doMapRequest(Model.getInstance().getId());
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
        Model.getInstance().getRequestQueue(getApplicationContext()).add(JSONRequest_user_data);
        Log.d("VolleyQueue", "Get profile request added");
    }
}


