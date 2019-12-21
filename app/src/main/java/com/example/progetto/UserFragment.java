package com.example.progetto;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;


public class UserFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle bundle) {
        return layoutInflater.inflate(R.layout.fragment_user, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textView_name = getActivity().findViewById(R.id.user_name);
        TextView textView_lp = getActivity().findViewById(R.id.user_lp_points);
        TextView textView_xp = getActivity().findViewById(R.id.user_xp_points);
        //imageView = getActivity().findViewById(R.id.user_image);
        textView_name.setText(Model.getInstance().getUsername());
        textView_lp.setText(Model.getInstance().getLP() + "");
        textView_xp.setText(Model.getInstance().getXP() + "");
        /*
        byte[] byteArray = Base64.decode(Model.getInstance().getImage(), Base64.DEFAULT);
        Bitmap decodedImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        imageView.setImageBitmap(decodedImage);
        */
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                Log.d("UserFragment", "Go back clicked");
                Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

        Button button = getActivity().findViewById(R.id.edit);
        button.setOnClickListener(new View.OnClickListener(){
              @Override
              public void onClick(View v) {
                  Log.d("ButtonEdit", "Edit button clicked");
                  TextView tv = getActivity().findViewById(R.id.user_name);
                  final String username = tv.getText().toString();
                  // TODO: get image as well

                  // Getting the id to build the string to POST
                  String id = null;
                  try {
                      id = Model.getInstance().getId().getString("session_id");
                  } catch (JSONException e) {
                      e.printStackTrace();
                  }
                  String json = "{'session_id':" + id + ", 'username':" + username + "', 'image':'noImage'}";

                  // Building json object
                  JSONObject jsonObject = null;
                  try {
                      jsonObject = new JSONObject(json);
                  } catch (JSONException e) {
                      e.printStackTrace();
                  }

                  String url = "https://ewserver.di.unimi.it/mobicomp/mostri/setprofile.php";

                  JsonObjectRequest JSONRequest_user_edit = new JsonObjectRequest(
                          Request.Method.POST,
                          url,
                          jsonObject,
                          new Response.Listener<JSONObject>() {
                              @Override
                              public void onResponse(JSONObject response) {
                                  Log.d("VolleyJson", "Server is working");
                                  Model.getInstance().setUsername(username);
                                  // TODO: with image as well
                                  Log.d("ButtonEdit", "New username " + username);
                                  Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
                                  startActivity(intent);
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
                  Model.getInstance().getRequestQueue(getActivity().getApplicationContext()).add(JSONRequest_user_edit);
                  Log.d("VolleyQueue", "Set profile request added");
              }
        });
    }
}
