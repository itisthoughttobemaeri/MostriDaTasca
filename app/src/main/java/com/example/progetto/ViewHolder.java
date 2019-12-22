package com.example.progetto;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


public class ViewHolder extends RecyclerView.ViewHolder{
    private TextView number;
    private TextView name;
    private ImageView image;
    private TextView points;
    private ImageView xp_image;

    public ViewHolder(View itemView){
        super(itemView);
        Log.d("ViewHolder", "Constructor called");
        image = itemView.findViewById(R.id.image);
        number = itemView.findViewById(R.id.number);
        name = itemView.findViewById(R.id.user_name);
        points = itemView.findViewById(R.id.rank_points);
        xp_image = itemView.findViewById(R.id.xp);
    }

    public void setText(User user, int rank){
        // Handling ranking
        Log.i("ViewHolder", user.toString());
        number.setText(rank + "°");
        xp_image.setImageResource(R.drawable.favorite);
        if (user.getUsername() == null || user.getUsername().equals("")) {
            name.setText("player");
        }
        else
            name.setText(user.getUsername());

        Log.d("UserXP", user.getXP() + "");
        points.setText(user.getXP() + "");
        if (rank == 1) {
            image.setImageResource(R.drawable.ic_cup1);
        }
        else if (rank == 2) {
            image.setImageResource(R.drawable.ic_cup2);
        }
        else if (rank == 3) {
            image.setImageResource(R.drawable.ic_cup3);
        }
        else {
            image.setImageResource(R.drawable.ic_medal);
        }
    }
}

