package com.example.progetto;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;


public class UserFragment extends Fragment {
    private ImageView imageView;
    TextView textView_name;
    TextView textView_lp;
    TextView textView_xp;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle bundle) {
        return layoutInflater.inflate(R.layout.fragment_user, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        textView_name = getActivity().findViewById(R.id.user_name);
        textView_lp = getActivity().findViewById(R.id.user_lp_points);
        textView_xp = getActivity().findViewById(R.id.user_xp_points);
        imageView = getActivity().findViewById(R.id.user_image);

        String url = "https://ewserver.di.unimi.it/mobicomp/mostri/getprofile.php";

        // This request was added to make sure a user offline got his data once back online

        JsonObjectRequest JSONRequest_user_edit = new JsonObjectRequest(
                Request.Method.POST,
                url,
                Model.getInstance().getId(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String s = response.getString("username");
                            Log.d("GetUser", response.toString());
                            Model.getInstance().setLP(Integer.parseInt(response.getString("lp")));
                            Model.getInstance().setXP(Integer.parseInt(response.getString("xp")));
                            Model.getInstance().setUsername(response.getString("username"));
                            Model.getInstance().setImage(response.getString("img"));

                            textView_name.setText(Model.getInstance().getUsername());
                            textView_lp.setText(Model.getInstance().getLP() + "");
                            textView_xp.setText(Model.getInstance().getXP() + "");

                            if (Model.getInstance().getImage() == null || Model.getInstance().getImage().equals("null"))
                                imageView.setImageResource(R.drawable.ic_student);
                            else {
                                Log.d("ImageConversion", Model.getInstance().getImage());
                                byte[] byteArray = Base64.decode(Model.getInstance().getImage(), Base64.DEFAULT);
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
                        new InternetDialog().show(getActivity().getSupportFragmentManager(), "dialog");
                    }
                }
        );
        Model.getInstance().getRequestQueue(getActivity().getApplicationContext()).add(JSONRequest_user_edit);
        Log.d("VolleyQueue", "Edit profile request added");


        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                Log.d("UserFragment", "Go back clicked");

                // Start request to set information on the server (only the username) -- let's see

                TextView tv = view.findViewById(R.id.user_name);
                final String username = tv.getText().toString();
                Log.d("ButtonEdit", "Username new is:" + username);

                // Getting the id to build the string to POST
                String id = null;
                try {
                    id = Model.getInstance().getId().getString("session_id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String json = "{'session_id':" + id + ", 'username':'" + username + "', 'img':'" + Model.getInstance().getImage() + "'}";

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
                                // Image saved in the Model onActivityResult (where there are controls)
                                Log.d("ButtonEdit", "New username " + username);
                                Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                                Log.d("ImageConversion", "The image was refused by the server or internet connection is not enabled");
                                Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                            }
                        }
                );
                Model.getInstance().getRequestQueue(getActivity().getApplicationContext()).add(JSONRequest_user_edit);
                Log.d("VolleyQueue", "Edit profile request added");
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

        Button button_image = view.findViewById(R.id.change_image);
        button_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Picking the image from camera
                if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("Gallery", "Permissions were not asked");
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 10);
                } else {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 10);
                }
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == 10) {
            Uri imageUri = data.getData();

            Bitmap bitmap = null;
            try {
                bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getActivity().getContentResolver(), imageUri));
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

            if (encodedImage.length() < 137000) {
                Log.d("EncodedImage", encodedImage);
                Model.getInstance().setImage(encodedImage);
                imageView.setImageURI(imageUri);
            } else {
                // Toast
                Toast toast = Toast.makeText(getActivity().getApplicationContext(), "The image is too big!", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
