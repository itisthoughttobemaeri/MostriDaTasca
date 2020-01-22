package com.example.progetto;

import android.content.Intent;
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
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

public class MapObjectFragment extends Fragment {
    private int id;
    private ImageView image;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle bundle) {
        id = Integer.parseInt(getArguments().getString("id"));
        return layoutInflater.inflate(R.layout.fragment_mapobject_element, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        Button yes = view.findViewById(R.id.confirm);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("ElementFragment", "Confirm clicked");
                // Create new fragment
                Bundle bundle = new Bundle();
                bundle.putInt("id", id);
                Log.d("IDtoPass", id + "");
                ResultFragment resultFragment = new ResultFragment();
                resultFragment.setArguments(bundle);
                ((MainActivity) getActivity()).addFragment(resultFragment);
            }
        });

        Button no = getActivity().findViewById(R.id.deny);
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("ElementFragment", "Deny clicked");
                // Go back to map
                Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        TextView points = getActivity().findViewById(R.id.user_lp_points);
        TextView fight = getActivity().findViewById(R.id.fight);
        TextView name = getActivity().findViewById(R.id.name);
        TextView size = getActivity().findViewById(R.id.size);
        ImageView image_points = getActivity().findViewById(R.id.points_object);
        image = getActivity().findViewById(R.id.image);

        ShownObject element = Model.getInstance().getShownObjectById(id);
        size.setText(element.getSize());
        name.setText(element.getName());

        // Setting information

        if (element.getType().equals("MO")) {
            if (element.getSize().equals("S")) {
                image_points.setImageResource(R.drawable.favorite);
                points.setText("1");
            } else if (element.getSize().equals("M")) {
                points.setText("3");
                image_points.setImageResource(R.drawable.favorite);
            }
            else {
                points.setText("10");
                image_points.setImageResource(R.drawable.favorite);
            }
        } else {
            if (element.getSize().equals("S")) {
                points.setText("0-50");
                image_points.setImageResource(R.drawable.heart2);
            } else if (element.getSize().equals("M")) {
                points.setText("25-75");
                image_points.setImageResource(R.drawable.heart2);
            }
            else {
                points.setText("50-100");
                image_points.setImageResource(R.drawable.heart2);
            }
        }

        if ( ((MainActivity) getActivity()).isDistanceObjectOk(id) ) {
            if (element.getType().equals("MO")) {
                fight.setText("Wanna fight this monster?");
                yes.setVisibility(yes.VISIBLE);
                no.setVisibility(no.VISIBLE);
            } else {
                fight.setText("Wanna grab this candy?");
                yes.setVisibility(yes.VISIBLE);
                no.setVisibility(no.VISIBLE);
            }
        }
        else {
            if (element.getType().equals("MO")) {
                fight.setText("You are too far to fight this!");
            } else {
                fight.setText("You are too far to grab this!");
            }
            no.setVisibility(no.VISIBLE);
            yes.setVisibility(yes.GONE);
        }

        if (element.getType().equals("CA") && Model.getInstance().getLP() == 100) {
            fight.setText("You have enough life points!");
            no.setVisibility(no.VISIBLE);
            yes.setVisibility(yes.GONE);
        }

        if (element.getImage() == null) {
            JsonObjectRequest JSONRequest_image_download = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("ImageModel", "Success image download (first&last time)");
                            String encodedImage = null;
                            try {
                                encodedImage = response.getString("img");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            image.setImageBitmap(decodedByte);

                            Model.getInstance().getShownObjectById(id).setImage(encodedImage);
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
            Model.getInstance().getRequestQueue(getActivity().getApplicationContext()).add(JSONRequest_image_download);
            Log.d("VolleyQueue", "Image request added");
        } else {
            Log.d("ImageModel", "Using image in the model");
            byte[] decodedString = Base64.decode(element.getImage(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            image.setImageBitmap(decodedByte);
        }
    }
}
