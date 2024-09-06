package com.example.exp.ui.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class Menu_download_adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class downloadViewStats extends RecyclerView.ViewHolder{

        public downloadViewStats(@NonNull View itemView) {
            super(itemView);
        }

    }

    public class downloadViewItemDownloading extends RecyclerView.ViewHolder{

        public downloadViewItemDownloading(@NonNull View itemView) {
            super(itemView);
        }

    }

}
