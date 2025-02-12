package com.vdusar.radien.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vdusar.radien.R;
import com.vdusar.radien.logic.player.PlayerService;
import com.vdusar.radien.logic.volley.volleyRequestData;

import org.json.JSONArray;
import org.json.JSONException;

public class Air_adapter extends RecyclerView.Adapter<Air_adapter.ViewHolder> {

    private static JSONArray data = null;
    private static volleyRequestData rq = null;
    private final Context ctx;
    private PlayerService player;

    public Air_adapter(Context context, JSONArray d, PlayerService player){
        data=d;
        this.ctx=context;
        this.player = player;
        rq = new volleyRequestData(this.ctx);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.air_card, parent, false);
        try {
            return new ViewHolder(rowItem);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.itemView.setTag(position);
        try {
            holder.name.setText(data.getJSONObject(position).getString("name"));
            rq.getImage(data.getJSONObject(position).getString("image"),(res)->{
                if (res != null) {
                    holder.logo.setImageBitmap(Bitmap.createScaledBitmap(res,400,400,false));
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

                player.startPlay((int)v.getTag());

            });
        }

    }

    public void destroyPlayer(){
        data=null;
        player.discardService();
        this.player=null;
    }

    @Override
    public int getItemCount() {
        return data.length();
    }
}
