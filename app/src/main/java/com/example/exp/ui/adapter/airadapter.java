package com.example.exp.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.exp.R;
import com.example.exp.volleyRequestData;

import org.json.JSONArray;
import org.json.JSONException;

public class airadapter  extends RecyclerView.Adapter<airadapter.ViewHolder> {

    private static JSONArray data = null;
    private static volleyRequestData rq = null;
    private final Context ctx;
    private int index = 0;

    public airadapter(Context context, JSONArray d){
        data=d;
        this.ctx=context;
        rq = new volleyRequestData(this.ctx);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.air, parent, false);
        try {
            return new ViewHolder(rowItem);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            holder.name.setText(data.getJSONObject(position).getString("name"));
            rq.getImage(data.getJSONObject(position).getString("image"),(res)->{
                if (res != null) {
                    holder.logo.setImageBitmap(Bitmap.createScaledBitmap(res,300,300,false));
                    holder.logo.setVisibility(View.VISIBLE);
                }else {
                    holder.logo.setImageResource(R.drawable.side_nav_bar);
                }
            });
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView name;
        private final ImageView logo;

        public ViewHolder(View view) throws JSONException {
            super(view);

            this.name = view.findViewById(R.id.air_name);
            this.logo = view.findViewById(R.id.air_logo);

            view.setOnClickListener((v)->{

            });
        }
    }

    @Override
    public int getItemCount() {
        return data.length();
    }
}
