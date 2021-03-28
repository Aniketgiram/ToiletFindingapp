package com.example.thetoiletfindingapp;

import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

public class viewHolder extends RecyclerView.ViewHolder {

    public ImageView mimageView;

    public viewHolder(View itemView) {
        super(itemView);
        mimageView = itemView.findViewById(R.id.imageView);
    }
}