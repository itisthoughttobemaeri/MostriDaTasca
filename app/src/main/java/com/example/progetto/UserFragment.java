package com.example.progetto;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
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

import org.json.JSONException;
import org.json.JSONObject;

public class UserFragment extends Fragment {

    private RequestQueue requestQueue;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle bundle) {
        return layoutInflater.inflate(R.layout.fragment_user, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());

        String url = "https://ewserver.di.unimi.it/mobicomp/mostri/getprofile.php";

        JsonObjectRequest JSONRequest_ranking_download = new JsonObjectRequest(
                Request.Method.POST,
                url,
                Model.getInstance().getId(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("VolleyJson", (String )response.getString("img"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        // Handle JSON data
                        TextView textView_name = getActivity().findViewById(R.id.user_name);
                        TextView textView_lp = getActivity().findViewById(R.id.user_lp);
                        TextView textView_xp = getActivity().findViewById(R.id.user_xp);
                        ImageView imageView = getActivity().findViewById(R.id.user_image);
                        try {
                            String s = response.getString("username");
                            textView_name.setText(s);
                            s = response.getString("lp");
                            textView_lp.setText(s);
                            s = response.getString("xp");
                            textView_xp.setText(s);
                            if (response.getString("image").equals("null")) {
                                imageView.setImageResource(R.drawable.ic_unicorn);
                            }
                            else {
                                s = response.getString("image");
                                byte[] byteArray = Base64.decode(s, Base64.DEFAULT);
                                Bitmap decodedImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                                imageView.setImageBitmap(decodedImage);
                            }
                        } catch (JSONException e) {
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
        Log.d("VolleyQueue", "User request added");
    }
}
