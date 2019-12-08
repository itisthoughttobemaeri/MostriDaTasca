package com.example.progetto;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class SplashScreen extends AppCompatActivity {

    private RequestQueue requestQueue;
    private SharedPreferences.Editor editor;
    private Thread myThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        requestQueue = Volley.newRequestQueue(this);

        SharedPreferences sharedPreferences = getSharedPreferences("Shared Preferences", 0);
        editor = sharedPreferences.edit();
        //editor.remove("session_id");
        editor.commit();
        Log.d("If", Boolean.toString(!sharedPreferences.contains("session_id")));

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
                            error.printStackTrace();
                        }
                    }
            );
            requestQueue.add(JSONRequest_user_setup);
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
            doRequest(Model.getInstance().getId());
        }
    }


    // Method used to call the request to initialize the objects

    private void doRequest(final JSONObject jsonObject) {
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
                        error.printStackTrace();
                        // TO DO: handle error 401 & 400
                    }
                }
        );

        requestQueue.add(JSONRequest_data_download);
        Log.d("VolleyQueue", "Second request added");

        // TO DO: get profile with information
    }


    // Method used to call the request to initialize the username

    private void doSetProfile(String string) {
        // Json object must be session id
        String url = "https://ewserver.di.unimi.it/mobicomp/mostri/setprofile.php";
        String json = "{'session_id':" + string + ", 'username': 'Player', 'image': '' }";

        // TO DO : set image default

        Model.getInstance().setLP(100);
        Model.getInstance().setXP(0);

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
                        doRequest(Model.getInstance().getId());
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
        requestQueue.add(JSONRequest_user_update);
        Log.d("VolleyQueue", "Set profile request added");
    }
}


