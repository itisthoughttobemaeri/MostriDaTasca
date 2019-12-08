package com.example.progetto;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

public class ResultFragment extends Fragment {
    private int id;
    private RequestQueue requestQueue;

    public ResultFragment(int id){
        this.id = id;
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle bundle) {
        return layoutInflater.inflate(R.layout.fragment_mapobject_result, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());

        String url = "https://ewserver.di.unimi.it/mobicomp/mostri/fighteat.php";

        String json = null;
        try {
            json = "{'session_id':" + Model.getInstance().getId().getString("session_id") + ", 'target_id': '" + id + "'}";
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject jsonObject = null;
        Log.d("Json", "Mapobject requested with session_id: " + json);
        try {
            jsonObject = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest JSONRequest_result = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("VolleyRequest", "Success");
                        ImageView image = getActivity().findViewById(R.id.resultImage);
                        TextView result = getActivity().findViewById(R.id.result);
                        TextView LP = getActivity().findViewById(R.id.LP);
                        TextView XP = getActivity().findViewById(R.id.XP);
                        TextView pointsGained = getActivity().findViewById(R.id.pointsGained);
                        Button buttonConfirm = getActivity().findViewById(R.id.ok);

                        String userStatus = null;
                        try {
                            userStatus = response.getString("Died");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        int LP_points = 0;
                        try {
                            LP_points = Integer.parseInt(response.getString("LP"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        int XP_points = 0;
                        try {
                            XP_points = Integer.parseInt(response.getString("XP"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        if (userStatus.equals("true")) {
                            // The user died
                            image.setImageResource(R.drawable.ic_tombstone);
                            result.setText("You died");
                        }
                        else {
                            // The user survived
                            if (Model.getInstance().getLP()<LP_points) {
                                // The user picked up a candy

                            } else if (Model.getInstance().getXP()<XP_points) {
                                // The user won against the monster

                            } else if (Model.getInstance().getLP()>LP_points) {
                                // The user lost a battle

                            } else if (Model.getInstance().getLP()==LP_points || Model.getInstance().getXP()==XP_points) {
                                // The user didn't earn anything
                            }
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
        requestQueue.add(JSONRequest_result);
        Log.d("VolleyQueue", "Result request added");
    }
}
