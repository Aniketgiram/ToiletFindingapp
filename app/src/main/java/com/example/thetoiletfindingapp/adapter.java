package com.example.thetoiletfindingapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class adapter extends RecyclerView.Adapter<adapter.adapterHolder> {
    public List<String> data;

    public adapter(List<String> data){
        this.data = data;
    }

    @NonNull
    @Override
    public adapterHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View v= inflater.inflate(R.layout.listlayout_recycle,viewGroup,false);
        return new adapterHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull adapterHolder adapterHolder, int i) {
        String d = data.get(i);
        adapterHolder.txtData.setText(d);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class adapterHolder extends RecyclerView.ViewHolder{
        TextView txtData;
        public adapterHolder(View itemView){
            super(itemView);
            txtData = itemView.findViewById(R.id.information);
        }
    }
}
