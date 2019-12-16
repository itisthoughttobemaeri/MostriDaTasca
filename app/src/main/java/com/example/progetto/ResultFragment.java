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
                        Log.d("VolleyRequest", response.toString());
                        ImageView image = getActivity().findViewById(R.id.resultImage);
                        TextView result = getActivity().findViewById(R.id.result);
                        TextView LP = getActivity().findViewById(R.id.LP);
                        TextView XP = getActivity().findViewById(R.id.XP);
                        TextView pointsGained = getActivity().findViewById(R.id.pointsGained);
                        Button buttonConfirm = getActivity().findViewById(R.id.ok);
                        ImageView imageView2 = getActivity().findViewById(R.id.imageView2);
                        ImageView imageView3 = getActivity().findViewById(R.id.imageView3);

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

                            if (Model.getInstance().getLP()<LP_points) {
                                // The user picked up a candy
                                image.setImageResource(R.drawable.ic_success);
                                result.setText("You picked up the candy!");
                                pointsGained.setText( "You gained " + (LP_points - Model.getInstance().getLP()) + " LP, so now you have:");

                            } else if (Model.getInstance().getXP()<XP_points) {
                                // The user won against the monster
                                image.setImageResource(R.drawable.ic_win);
                                result.setText("You won the battle!");
                                pointsGained.setText( "You gained " + (XP_points - Model.getInstance().getXP()) + " XP and you lost some LP, so now you have:");

                            } else if (Model.getInstance().getLP()>LP_points) {
                                Log.d("Points", Integer.toString(LP_points));
                                // The user lost a battle
                                image.setImageResource(R.drawable.ic_game_over);
                                result.setText("You lost the battle...");
                                pointsGained.setText( "You lost " + (Model.getInstance().getLP() - LP_points) + " LP, so now you have:");

                            } else if (Model.getInstance().getLP()==LP_points || Model.getInstance().getXP()==XP_points) {
                                // The user didn't earn anything
                                result.setText("You didn't lose neither earn anything...");
                                pointsGained.setVisibility(View.GONE);

                            }
                        }

                        buttonConfirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d("ResultFragment", "Go back clicked");
                                Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                            }
                        });

                        Model.getInstance().setXP(XP_points);
                        Model.getInstance().setLP(LP_points);
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
