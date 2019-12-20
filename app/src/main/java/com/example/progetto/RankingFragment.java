package com.example.progetto;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class RankingFragment extends Fragment {

    private RequestQueue requestQueue;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle bundle) {
        View rootView = layoutInflater.inflate(R.layout.fragment_ranking, container, false);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());

        String url = "https://ewserver.di.unimi.it/mobicomp/mostri/ranking.php";

        JsonObjectRequest JSONRequest_ranking_download = new JsonObjectRequest(
                Request.Method.POST,
                url,
                Model.getInstance().getId(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("VolleyJson", "Server is working");
                        // Handle JSON data
                        try {
                            Gson gson = new Gson();
                            JSONArray ranking_array = response.getJSONArray("ranking");
                            // JSON data converted into array
                            User[] rankings = gson.fromJson(ranking_array.toString(), User[].class);
                            Log.d("VolleyJson", "This is the first user from the server" + rankings[0].toString());
                            Model.getInstance().refreshUsers(rankings);
                            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
                            Adapter adapter = new Adapter(getActivity().getApplicationContext());
                            recyclerView.setAdapter(adapter);
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
        Log.d("VolleyQueue", "Ranking request added");

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                Log.d("RankingFragment", "Go back clicked");
                Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }
}
