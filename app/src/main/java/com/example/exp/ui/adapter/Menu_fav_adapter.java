package com.example.exp.ui.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class Menu_fav_adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

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

    public class menuFavIndex extends RecyclerView.ViewHolder{

        public menuFavIndex(@NonNull View itemView) {
            super(itemView);
        }

    }

    public class menuFavChannels extends RecyclerView.ViewHolder{

        public menuFavChannels(@NonNull View itemView) {
            super(itemView);
        }

    }

}
