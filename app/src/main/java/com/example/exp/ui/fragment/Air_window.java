package com.example.exp.ui.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.exp.logic.player.PlayerContent;
import com.example.exp.logic.player.PlayerService;
import com.example.exp.R;
import com.example.exp.ui.adapter.Air_adapter;
import com.google.common.util.concurrent.MoreExecutors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class Air_window extends Fragment {

    private Air_adapter rcView ;
    public Context ctx;
    public JSONArray jsonFile;
    public BufferedReader bfr;
    public RecyclerView recyclerView;
    private PlayerService player;
    List<MediaItem> mditm = null;
    ArrayList<PlayerContent> plc;

    public static Air_window getInstance() {
        Air_window fragment = new Air_window();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx=this.getContext();

        try {
            jsonFile = new JSONArray(readFile(R.raw.air));
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_air, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.player = new PlayerService(ctx, view.findViewById(R.id.player_view_air) ,  view.findViewById(R.id.title_air));


        setButtonsAndListners(view);

        recyclerView = view.findViewById(R.id.main_air_view);
        recyclerView.setLayoutManager(new GridLayoutManager(ctx,2));
        recyclerView.addItemDecoration(new DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL));
        recyclerView.addItemDecoration(new DividerItemDecoration(ctx, DividerItemDecoration.HORIZONTAL));


    }

    @OptIn(markerClass = UnstableApi.class)
    public MediaMetadata setPlayerContents(JSONObject meta) throws JSONException {
        MediaMetadata.Builder mdt = new MediaMetadata.Builder();
        mdt.setArtist(meta.getString("name"));
        mdt.setTitle("Radio is Playing");
//      mdt.setDisplayTitle("display title");
//      mdt.setAlbumTitle(meta.Title);
//      mdt.setGenre(meta.Genre);
//      mdt.setDescription(meta.Discription);
        mdt.setArtworkUri(Uri.parse(meta.getString("image")));
        return mdt.build();
    }

    public void setButtonsAndListners(View view){
        int part = 50;
        int total = getDataParts(jsonFile,part);
        ViewGroup parent = view.findViewById(R.id.page_air);

        for (int i = 0; i < total; i++) {
            Button bt = new Button(ctx);
            bt.setText("P:"+ (i + 1));
            bt.setTag(i);
            bt.setBackground(ContextCompat.getDrawable(ctx,R.drawable.button_draw));
            parent.addView(bt);

            bt.setOnClickListener((v)->{
                clickButton(v,part);
            });

            int finalI = i;
            this.player.getControllerFuture().addListener(() ->{
                if (finalI ==0) {
                    bt.performClick();
                }
            }, MoreExecutors.directExecutor());


        }

    }

    public void clickButton(View v,int part){
        try {
            player.getPlayer().clearMediaItems();
            this.mditm =new ArrayList<MediaItem>();
            this.plc = new ArrayList<PlayerContent>();
            selectButton(v);
            setRecyclerview(v,part,jsonFile);

            this.player.getControllerFuture().addListener(() ->{
                player.getPlayer().addMediaItems(mditm);
                this.player.setDataSet(plc);
            }, MoreExecutors.directExecutor());

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    public void addMediaItems(JSONArray file,int index) throws JSONException {
        DataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory();

//      HlsMediaSource hlsMediaSourc = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri));
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(file.getJSONObject(index).getString("live_url")));
        MediaItem tef = mediaSource.getMediaItem().buildUpon().setMediaMetadata(setPlayerContents(file.getJSONObject(index))).setMediaId(Integer.toString(index)).build();
        tef.buildUpon().setLiveConfiguration(new MediaItem.LiveConfiguration.Builder().setTargetOffsetMs(100).build()).build();

        mditm.add(tef);
        PlayerContent pl = new PlayerContent(Integer.toString(index),file.getJSONObject(index).getString("name"),file.getJSONObject(index).getString("image"),file.getJSONObject(index).getString("image"),"","","",file.getJSONObject(index).getString("name"),file.getJSONObject(index).getString("live_url"),"");
        plc.add(pl);
    }

    public void selectButton(View v){
        ViewGroup parent = (ViewGroup) v.getParent();

        for (int i = 0; i < parent.getChildCount(); i++) {
            if ((int)v.getTag()!=i){
                parent.getChildAt(i).setScaleY(1);
                parent.getChildAt(i).setScaleX(1);
                parent.getChildAt(i).setElevation(0);
                parent.getChildAt(i).setBackground(ContextCompat.getDrawable(ctx,R.drawable.button_draw));
        }else{
                parent.getChildAt((int)v.getTag()).setElevation(50);
                parent.getChildAt((int)v.getTag()).setScaleY(1.2f);
                parent.getChildAt((int)v.getTag()).setScaleX(1.2f);
                parent.getChildAt((int)v.getTag()).setBackground(ContextCompat.getDrawable(ctx, R.drawable.button_select));
            }}
    }

    public void setRecyclerview(View v,int p,JSONArray a) throws JSONException {
        int start = ((int)(v.getTag())*p);
        int end = Math.min((start + p), a.length());
        JSONArray r = new JSONArray();

        for (int i = start; i < end; i++) {
            r.put(a.get(i));
            addMediaItems(jsonFile,i);
        }

        rcView = new Air_adapter(ctx,r,player);
        recyclerView.setAdapter(this.rcView);
    }


    public int getDataParts(JSONArray data, int part){
        int totals = data.length();
        if ((totals%part)==0){
            return (totals/part);
        }else{
            return ((totals/part)+1);
        }
    }


    public String readFile(int id) throws IOException
    {
        bfr = null;
        bfr = new BufferedReader(new InputStreamReader(ctx.getResources().openRawResource(id), StandardCharsets.UTF_8));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = bfr.readLine()) != null)
        {
            content.append(line);
        }
        return content.toString();
    }



    public void closeAll() throws IOException {
        if (bfr != null) {
            bfr.close();
        }
        if (rcView != null) {
            rcView.destroyPlayer();
        }
        player=null;
        jsonFile=null;
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            this.closeAll();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}