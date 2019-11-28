package com.example.progetto;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonElement;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.MalformedInputException;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class MainActivity extends AppCompatActivity {

    private RequestQueue requestQueue;
    private SharedPreferences.Editor editor;

    // TO DO: move requests to splash screen

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestQueue = Volley.newRequestQueue(this);

        SharedPreferences sharedPreferences = getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();

        // TO DO: verify 1st app execution

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

                                // TO DO: handle 1st run & next executions of the application
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
            String session_id = sharedPreferences.getString("session_id", null);
            try {
                doRequests(new JSONObject(
                        "{session_id:" + session_id + "}"
                ));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    // Method used to call the request to refresh the objects and the ranking

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
                            Log.d("VolleyJson", shownObjects[0].toString());
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
                            Log.d("VolleyJson", users[0].toString());
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
