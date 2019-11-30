package com.example.progetto;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;


public class Adapter extends RecyclerView.Adapter<ViewHolder> {
    private LayoutInflater inflater;

    public Adapter(Context context){
        this.inflater = LayoutInflater.from(context);
    }

    // Method called when a new row is created
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup vg, int viewType) {
        // Inflating row layout
        View view = inflater.inflate(R.layout.ranking_row, vg, false );
        return new ViewHolder(view);
    }

    // Method called to populate the row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        Log.d("Adapter", "onBindViewHolder method called");
        User[] users = Model.getInstance().getUsers();
        holder.setText(users[position], ++position);
    }

    // Method called to know how many elements
    @Override
    public int getItemCount(){
        return 20;
    }
}
