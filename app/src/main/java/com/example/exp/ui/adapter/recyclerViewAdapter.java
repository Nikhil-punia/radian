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
import com.example.exp.R;
import com.example.exp.Singleton;
import com.example.exp.Player;
import com.example.exp.radio;
import com.example.exp.volleyRequestData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Function;


public class recyclerViewAdapter extends RecyclerView.Adapter<recyclerViewAdapter.ViewHolder> {
    private final JSONArray data;
    private final Context ctx;
    private final Player player;
    private final View sView;
    private final volleyRequestData rq ;
    public int saltid = 2752;

    public recyclerViewAdapter (JSONArray data , Context context, PlayerView playerview, View tis) {
        this.data = data;
        this.ctx = context;
        this.sView = tis;
        this.rq = new volleyRequestData(this.ctx);
        this.player = new Player(context,playerview,sView);
    }

    @NonNull
    @Override
    public recyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.card, parent, false);
        return new ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            holder.imgView.setVisibility(View.INVISIBLE);
            holder.textView.setText(this.data.getJSONObject(position).getString("name"));
            holder.itemView.setId(saltid+position);
            setTheLogo(this.data.getJSONObject(position).getString("logo"),holder);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
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
        return this.data.length();
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
                    if(sView.findViewById((Integer) v.getId()-1)!=null){
                        setCards(v.getId()-saltid-1,this,"b");
                    }
                    if(sView.findViewById((Integer) v.getId()+1)!=null){
                        setCards(v.getId()-saltid+1,this,"f");
                    }
                    setCards(v.getId()-saltid,this,null);

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    public void setCards(int position,ViewHolder holder,String setMI) throws JSONException {
        String url_f = "https://zeno.fm/_next/data/ZyoucVrauhoqKBWqBepDH/radio/"+ this.data.getJSONObject(position).getString("url").split("/")[4]+".json";
            rq.sendRequestObj(url_f, (result) -> {
                try {
                    if (result != null) {
                        JSONObject b_j = result.getJSONObject("pageProps").getJSONObject("station");
                        JSONObject b_o = result.getJSONObject("pageProps").getJSONObject("meta");

                        String uri_t = (b_j.getString("streamURL"));
                        PlayerContent plc = new PlayerContent(b_j.getString("name"), b_j.getString("logo"), b_j.getString("background"), b_o.getString("description"), b_j.getJSONArray("languages"), b_j.getString("genre"),this.data.getJSONObject(position).getString("name"),uri_t);

                        if (this.data.get(position)!=null) {
                            Object[] tagSet = {this.data.getJSONObject(position).getString("url"), position, uri_t, plc,true};
                            holder.itemView.setTag(tagSet);

                            player.setCurrentItem(holder);

                            if (setMI == null) {
                                player.StartPlay(uri_t, plc,holder);
                            }

                        }
                    } else {
                            this.data.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, 1);
                    }
                } catch (JSONException e) {
                    System.out.println(e);
                }
            });
        }



    public void destroyPlayer(){
        player.clearMediaNotificaton();
        player.releasePlayer();
        player.releaseSession();
    }


}
