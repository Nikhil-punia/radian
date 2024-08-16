package com.example.exp;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.net.Uri;

import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.OptIn;

import androidx.fragment.app.FragmentManager;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;

import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;

import androidx.recyclerview.widget.RecyclerView;
import com.example.exp.ui.adapter.recyclerViewAdapter;
import com.google.common.util.concurrent.MoreExecutors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Radio extends Fragment {
    private static final String TAG = "log";
    private static Radio frg;
    private volleyRequestData rq ;
    private recyclerViewAdapter rcView ;
    private int Position = 0;
    private PlayerService player;
    ArrayList<PlayerContent> dataSet;
    ProgressBar progBar ;
    private Player playerg;
    List<MediaItem> mediaItm = null;
    View v = null;
    Context ctx =null;
    public ViewGroup pager;
    private RecyclerView recyclerView;


    public static Radio getInstance() {
        frg = new Radio();
        return frg;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void loopAndSaveChannels(JSONArray stations,int index) throws JSONException, IOException {

        String url_f = "https://zeno.fm/_next/data/ZyoucVrauhoqKBWqBepDH/radio/" + stations.getJSONObject(index).getString("url").split("/")[4] + ".json";

            rq.sendRequestObj(url_f, (result) -> {
                try {

                    if (!frg.isVisible()) {
                        rq.cancelAllRequest();
                    }

                    if (result != null && dataSet != null) {
                        JSONObject b_j = result.getJSONObject("pageProps").getJSONObject("station");
                        JSONObject b_o = result.getJSONObject("pageProps").getJSONObject("meta");

                        String uri_t = (b_j.getString("streamURL"));

                        PlayerContent plc = new PlayerContent(b_j.getString("name"), b_j.getString("logo"), b_j.getString("background"), b_o.getString("description"), b_j.getJSONArray("languages"), b_j.getString("genre"), stations.getJSONObject(index).getString("name"), uri_t, stations.getJSONObject(index).getString("url"));
                        dataSet.add(plc);
                        addMediaItems(plc);
                    }

                    Position++;
                    progBar.setProgress(Position);

                    if (Position == (stations.length() - 1)) {
                        progBar.setVisibility(View.GONE);
                        updateUi(dataSet);
                    }

                } catch (JSONException e) {
                    System.out.println(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

    }


    public void updateUi(ArrayList<PlayerContent> resp) throws JSONException, IOException {

        this.player.getControllerFuture().addListener(() ->{
            this.player.getPlayer().clearMediaItems();
            this.player.getPlayer().addMediaItems(mediaItm);
            this.player.setDataSet(resp);
        }, MoreExecutors.directExecutor());


        rcView = new recyclerViewAdapter(resp,ctx,this.player);
        recyclerView.setAdapter(this.rcView);

    }

    @OptIn(markerClass = UnstableApi.class)
    public void addMediaItems(PlayerContent data){
        DataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory();
//      HlsMediaSource hlsMediaSourc = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri));

        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(data.getStreamUrl()));
        MediaItem tef = mediaSource.getMediaItem().buildUpon().setMediaMetadata(setPlayerContents(data)).setMediaId(Integer.toString(dataSet.size()-1)).build();

        mediaItm.add(tef);

    }

    @OptIn(markerClass = UnstableApi.class)
    public MediaMetadata setPlayerContents(PlayerContent meta){
        MediaMetadata.Builder mdt = new MediaMetadata.Builder();
        mdt.setArtist(meta.getChannel());
//      mdt.setDisplayTitle("display title");
        mdt.setAlbumTitle(meta.Title);
        mdt.setGenre(meta.Genre);
        mdt.setDescription(meta.Discription);
        mdt.setArtworkUri(Uri.parse(meta.Background_Url));
        return mdt.build();
    }

    public Object getComponents(int id){
        return v.findViewById(id);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (rcView != null) {
            rcView.destroyPlayer();
        }
        player=null;
        playerg=null;
        dataSet=null;
        mediaItm=null;
        rq.cancelAllRequest();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_radio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ctx=this.getContext();
        rq = new volleyRequestData(ctx);
        v=view;
        this.pager=v.findViewById(R.id.pager_main);
        this.player = new PlayerService(ctx, v.findViewById(R.id.player_view_m) , v.findViewById(R.id.title_main2));

        this.progBar=v.findViewById(R.id.loadbar_radio);

        recyclerView = v.findViewById(R.id.rcl_view);
        recyclerView.setLayoutManager(new GridLayoutManager(ctx,2));
        recyclerView.addItemDecoration(new DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL));
        recyclerView.addItemDecoration(new DividerItemDecoration(ctx, DividerItemDecoration.HORIZONTAL));
        setPager(50,50);

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

    public void setPager(int pages,int result_on){

        for (int i = 0; i < pages; i++) {
            Button bt = new Button(ctx);
            bt.setText("P:"+(i+1));
            bt.setTag(i);
            bt.setBackground(ContextCompat.getDrawable(ctx,R.drawable.button_draw));
            pager.addView(bt);

            int finalI = i+1;
            bt.setOnClickListener((v)->{
                this.mediaItm =new ArrayList<MediaItem>();
                this.rq.cancelAllRequest();
                selectButton(v);

                rq.findCountryRadios(result_on, finalI,"India", (result) -> {
                    try {
                        Position = 0;
                        progBar.setProgress(0);
                        progBar.setVisibility(View.VISIBLE);
                        progBar.setMax(result.length());

                        dataSet = new ArrayList<>();

                        for (int g = 0; g < result.length(); g++) {
                            loopAndSaveChannels(result,g);
                        }

                    } catch (JSONException | IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            });

            int finals = i;
            this.player.getControllerFuture().addListener(() ->{
                if (finals ==0) {
                    bt.performClick();
                }
            }, MoreExecutors.directExecutor());

        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}