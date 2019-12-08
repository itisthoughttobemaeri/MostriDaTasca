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

public class MapObjectFragment extends Fragment {
    private int id;
    private RequestQueue requestQueue;

    public MapObjectFragment(JsonElement jsonElement){
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        Gson gson = new Gson();
        id = Integer.parseInt(jsonObject.get("id").toString());
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle bundle) {
        return layoutInflater.inflate(R.layout.fragment_mapobject_element, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());

        String url = "https://ewserver.di.unimi.it/mobicomp/mostri/getimage.php";

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

        JsonObjectRequest JSONRequest_image_download = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("VolleyRequest", "Success");
                        ImageView image = getActivity().findViewById(R.id.image);
                        TextView points = getActivity().findViewById(R.id.LP);
                        TextView type = getActivity().findViewById(R.id.type);
                        TextView fight = getActivity().findViewById(R.id.fight);
                        TextView name = getActivity().findViewById(R.id.name);
                        TextView size = getActivity().findViewById(R.id.size);

                        ShownObject element = Model.getInstance().getShownObjectById(id);
                        size.setText(element.getSize());
                        name.setText(element.getName());

                        String encodedImage = null;
                        try {
                            encodedImage = response.getString("img");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        image.setImageBitmap(decodedByte);


                        // Setting information

                        if (element.getType().equals("MO")) {
                            type.setText("Monster");
                            if (element.getSize().equals("S")) {
                                points.setText("1 XP");
                            } else if (element.getSize().equals("M")) {
                                points.setText("3 XP");
                            }
                            else {
                                points.setText("10 XP");
                            }
                        } else {
                            type.setText("Candy");
                            if (element.getSize().equals("S")) {
                                points.setText("0-50 LP");
                            } else if (element.getSize().equals("M")) {
                                points.setText("25-75 LP");
                            }
                            else {
                                points.setText("50-100 LP");
                            }
                        }

                        if ( new MainActivity().isDistanceObjectOk(id) ) {
                            Button yes = getActivity().findViewById(R.id.confirm);
                            Button no = getActivity().findViewById(R.id.deny);
                            if (element.getType().equals("MO")) {
                                fight.setText("Wanna fight this monster?");
                                yes.setVisibility(yes.VISIBLE);
                                no.setVisibility(no.VISIBLE);
                            } else {
                                fight.setText("Wanna grab this candy?");
                                yes.setVisibility(yes.VISIBLE);
                            }
                        }
                        else {
                            if (element.getType().equals("MO")) {
                                fight.setText("You are too far to fight this monster");
                            } else {
                                fight.setText("You are too far to grab this candy");
                            }
                            Button no = getActivity().findViewById(R.id.deny);
                            no.setVisibility(no.VISIBLE);
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
        requestQueue.add(JSONRequest_image_download);
        Log.d("VolleyQueue", "Image request added");
    }

    public void onClick (View view) {
        switch (view.getId()) {
            case R.id.confirm :
                Log.d("MapObject", "Grab candy/fight monster");
                // Create new fragment
            case R.id.deny :
                Log.d("MapObject", "Refused to fight/went back");
                // Go back to map
        }
    }
}
