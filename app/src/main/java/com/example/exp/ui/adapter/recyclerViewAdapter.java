package com.example.exp.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.exp.PlayerContent;
import com.example.exp.PlayerService;
import com.example.exp.R;
import com.example.exp.Singleton;
import com.example.exp.Player;
import com.example.exp.radio;
import com.example.exp.volleyRequestData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;


public class recyclerViewAdapter extends RecyclerView.Adapter<recyclerViewAdapter.ViewHolder> {
    private final ArrayList<PlayerContent> data;
    private final Context ctx;
    private final PlayerService player;
    private final View sView;
    private final volleyRequestData rq ;
    public int saltid = 2752;

    public recyclerViewAdapter (ArrayList<PlayerContent> data , Context context, PlayerView playerview, View tis) {
        this.data = data;
        this.ctx = context;
        this.sView = tis;
        this.rq = new volleyRequestData(this.ctx);
        this.player = new PlayerService(context,playerview,sView);
    }

    @NonNull
    @Override
    public recyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.card, parent, false);
        return new ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.imgView.setVisibility(View.INVISIBLE);
        holder.textView.setText(this.data.get(position).getChannel());
        holder.itemView.setId(saltid+position);
        setTheLogo(this.data.get(position).getLogo(),holder);
    }

    @SuppressLint("ResourceType")
    public void setTheLogo(String uri, ViewHolder holder){
        rq.getImage(uri,(res)->{
            if (res != null) {
                holder.imgView.setImageBitmap(res);
                holder.imgView.setVisibility(View.VISIBLE);
            }else {
                holder.imgView.setImageResource(R.raw.k);
            }
        });
    }


    @Override
    public int getItemCount() {
        return this.data.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final ImageView imgView;

        public ViewHolder(View view) {
            super(view);

            this.textView = view.findViewById(R.id.r_name);
            this.imgView = view.findViewById(R.id.rd_logo);

            view.setOnClickListener((v)->{
                try {
                    setCards(v.getId()-saltid,this);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            });

        }


    public void setCards(int position,ViewHolder holder) throws JSONException {

                        if (data.get(position)!=null) {
                            Object[] tagSet = {data.get(position).getQ_url(), position, data.get(position).getStreamUrl(), data.get(position),true};

                            holder.itemView.setTag(tagSet);
                            player.setCurrentItem(holder);

                            player.StartPlay(data.get(position).getStreamUrl(), data.get(position),holder);
                        }
        }
    }



    public void destroyPlayer(){
        player.discardService();
    }


}
