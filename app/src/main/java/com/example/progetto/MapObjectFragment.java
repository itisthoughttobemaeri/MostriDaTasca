package com.example.progetto;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
                        TextView points = getActivity().findViewById(R.id.points);
                        TextView type = getActivity().findViewById(R.id.type);
                        TextView fight = getActivity().findViewById(R.id.fight);
                        TextView name = getActivity().findViewById(R.id.name);
                        TextView size = getActivity().findViewById(R.id.size);

                        ShownObject element = Model.getInstance().getShownObjectById(id);
                        size.setText(element.getSize());
                        name.setText(element.getName());

                        if (element.getType().equals("MO")) {
                            type.setText("Monster");
                        } else {
                            type.setText("Candy");
                        }
                        // TO DO : finish
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
}
