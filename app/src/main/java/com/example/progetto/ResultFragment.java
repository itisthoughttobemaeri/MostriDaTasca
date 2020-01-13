package com.example.progetto;

import android.content.Intent;
import android.os.Bundle;
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

import org.json.JSONException;
import org.json.JSONObject;

public class ResultFragment extends Fragment {
    private int id;
    private ImageView image;
    private TextView result;
    private TextView LP;
    private TextView XP;
    private TextView pointsGained;
    private ImageView imageView2;
    private ImageView imageView3;

    public ResultFragment(int id){
        this.id = id;
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle bundle) {
        return layoutInflater.inflate(R.layout.fragment_mapobject_result, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        image = view.findViewById(R.id.resultImage);
        result = view.findViewById(R.id.result);
        LP = view.findViewById(R.id.LP);
        XP = view.findViewById(R.id.XP);
        pointsGained = view.findViewById(R.id.pointsGained);
        Button buttonConfirm = view.findViewById(R.id.ok);
        imageView2 = view.findViewById(R.id.imageView2);
        imageView3 = view.findViewById(R.id.imageView3);

        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("ResultFragment", "Go back clicked");
                Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        JsonObjectRequest JSONRequest_result = new JsonObjectRequest(
            Request.Method.POST,
            url,
            jsonObject,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("VolleyRequest", response.toString());


                    Boolean userStatus = null;
                    try {
                        userStatus = response.getBoolean("died");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    int LP_points = 0;
                    try {
                        LP_points = Integer.parseInt(response.getString("lp"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    int XP_points = 0;
                    try {
                        XP_points = Integer.parseInt(response.getString("xp"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (userStatus) {
                        // The user died
                        image.setImageResource(R.drawable.ic_tombstone);
                        result.setText("You died...");
                        pointsGained.setVisibility(View.GONE);
                        LP.setVisibility(View.GONE);
                        XP.setVisibility(View.GONE);
                        imageView2.setVisibility(View.GONE);
                        imageView3.setVisibility(View.GONE);
                    }
                    else {
                        // The user survived
                        LP.setText(Integer.toString(LP_points));
                        XP.setText(Integer.toString(XP_points));

                        // Model points are the past points
                        if (Model.getInstance().getLP()<LP_points) {
                            // The user picked up a candy
                            image.setImageResource(R.drawable.ic_donation);
                            result.setText("You chose a good one!");
                            pointsGained.setText( "You gained " + (LP_points - Model.getInstance().getLP()) + "LP, so now you have:");

                        } else if (Model.getInstance().getXP()<XP_points) {
                            // The user won against the monster
                            image.setImageResource(R.drawable.ic_win);
                            result.setText("You are invincible!");
                            pointsGained.setText( "You gained " + (XP_points - Model.getInstance().getXP()) + "XP and you lost " +(Model.getInstance().getLP() - LP_points) + "LP, so now you have:");

                        } else if (Model.getInstance().getLP()==LP_points || Model.getInstance().getXP()==XP_points) {
                            // The user didn't earn anything
                            image.setImageResource(R.drawable.ic_okay);
                            result.setText("You didn't lose neither earn anything...");
                            pointsGained.setVisibility(View.GONE);

                        }
                    }

                    Model.getInstance().setXP(XP_points);
                    Model.getInstance().setLP(LP_points);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    new InternetDialog().show(getActivity().getSupportFragmentManager(), "dialog");
                }
            }
        );
        Model.getInstance().getRequestQueue(getActivity().getApplicationContext()).add(JSONRequest_result);
        Log.d("VolleyQueue", "Result request added");
    }
}
