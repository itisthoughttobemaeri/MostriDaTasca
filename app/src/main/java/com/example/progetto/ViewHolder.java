package com.example.progetto;

import android.media.Image;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


public class ViewHolder extends RecyclerView.ViewHolder{
    private TextView number;
    private TextView name;
    private ImageView image;

    public ViewHolder(View itemView){
        super(itemView);
        Log.d("ViewHolder", "Constructor called");
        image = itemView.findViewById(R.id.image);
        number = itemView.findViewById(R.id.number);
        name = itemView.findViewById(R.id.username);
    }

    public void setText(User user, int rank){
        // Handling ranking
        Log.i("ViewHolder", user.toString());
        number.setText(Integer.toString(rank) + "°");
        name.setText(user.getUsername());
    }
}

