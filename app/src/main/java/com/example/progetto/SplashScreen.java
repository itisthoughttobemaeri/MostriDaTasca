package com.example.progetto;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        requestQueue = Volley.newRequestQueue(this);

        SharedPreferences sharedPreferences = getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();

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
                                Log.d("VolleyJson", s);
                                doRequests(response);

                                editor.putString("session_id", (String) response.get("session_id"));
                                editor.commit();

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
        } else {
            String session_id = sharedPreferences.getString("session_id", null);
            try {
                doRequests(new JSONObject(
                        "{session_id:" + session_id + "}"
                ));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Thread myThread = new Thread() {
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
        myThread.start();
    }

    // Method used to call the request to initialize the objects and the ranking

    private void doRequests(final JSONObject jsonObject) {
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

        url = "https://ewserver.di.unimi.it/mobicomp/mostri/ranking.php";

        JsonObjectRequest JSONRequest_ranking_download = new JsonObjectRequest(
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
                            JSONArray rankings = response.getJSONArray("ranking");
                            // JSON data converted into array
                            User[] users = gson.fromJson(rankings.toString(), User[].class);
                            Log.d("VolleyJson", "THis s the first classified user" + users[0].toString());
                            Model.getInstance().refreshUsers(users);
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
        requestQueue.add(JSONRequest_ranking_download);
        Log.d("VolleyQueue", "Third request added");
    }
}



