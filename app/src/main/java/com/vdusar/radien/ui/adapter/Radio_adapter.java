package com.vdusar.radien.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.offline.Download;
import androidx.media3.exoplayer.offline.DownloadManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vdusar.radien.R;
import com.vdusar.radien.logic.download_manager.DownloadManagerUtil;
import com.vdusar.radien.logic.player.PlayerContent;
import com.vdusar.radien.logic.player.PlayerService;
import com.vdusar.radien.logic.singleton.CacheSingleton;
import com.vdusar.radien.logic.volley.volleyRequestData;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;


public class Radio_adapter extends RecyclerView.Adapter<Radio_adapter.ViewHolder> {
    private ArrayList<PlayerContent> data;
    private final Context ctx;
    private PlayerService player;
    private final volleyRequestData rq ;
    public static int saltid = 2752;
    public static int saltIdDownBtn = 3752;
    public ViewGroup parentToAll;
    private final HashMap<String, Integer> channelBtnDrawable = new HashMap<>();
    private final HashMap<String, Integer> channelPosition = new HashMap<>();
    private final HashMap<String, Bitmap> channelBitmaps = new HashMap<>();

    @OptIn(markerClass = UnstableApi.class)
    public Radio_adapter(ArrayList<PlayerContent> data , Context context, PlayerService player) {
        this.data = data;
        this.ctx = context;

        this.rq = new volleyRequestData(this.ctx);
        this.player = player;

        for (int i = 0; i < data.size(); i++) {
            String id =(this.data.get(i).streamUrl.split("/")[3]);
            if (DownloadManagerUtil.getRuntimeValues().get(id)==null) {
                channelBtnDrawable.put((this.data.get(i).streamUrl.split("/")[3]), R.drawable.baseline_download_for_offline_24);
            }else {
                channelBtnDrawable.put((this.data.get(i).streamUrl.split("/")[3]), R.drawable.baseline_downloading_24);
            }
        }

        for (int i = 0; i < data.size(); i++) {
            channelPosition.put((this.data.get(i).streamUrl.split("/")[3]),i);
        }

        setChannelBitmap();

        CacheSingleton.getInstance().getDownloadManager().addListener(new DownloadManager.Listener() {
            @Override
            public void onDownloadChanged(@NonNull DownloadManager downloadManager, @NonNull Download download, @Nullable Exception finalException) {
                DownloadManager.Listener.super.onDownloadChanged(downloadManager, download, finalException);

                if (download.state== Download.STATE_DOWNLOADING) {
                    String id = download.request.uri.toString().split("/")[3];
                    channelBtnDrawable.remove(id);
                    channelBtnDrawable.put(id,R.drawable.baseline_downloading_24);
                    if (channelPosition.get(id)!=null) {
                        notifyItemChanged(channelPosition.get(id));
                    }
                }

                if ((download.state==Download.STATE_STOPPED) || (download.state==Download.STATE_FAILED)){
                    String id = download.request.uri.toString().split("/")[3];
                    channelBtnDrawable.remove(id);
                    channelBtnDrawable.put(id, R.drawable.baseline_download_for_offline_24);
                    if (channelPosition.get(id)!=null) {
                        notifyItemChanged(channelPosition.get(id));
                    }
                }

            }

        });
    }

    private void setChannelBitmap() {
        for (int i = 0; i < this.data.size(); i++) {
            String url = this.data.get(i).Logo;
            String id = this.data.get(i).streamUrl.split("/")[3];
            if ((!url.isEmpty()) && (channelBitmaps.get(id)==null)){
                rq.getImage(url, (resp) -> {
                    channelBitmaps.put(id, resp);
                });

            }
        }
    }


    @NonNull
    @Override
    public Radio_adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.radio_card, parent, false);
        this.parentToAll = parent;
        return new ViewHolder(rowItem);
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.downloadBtn.setTag(this.data.get(position));
        holder.downloadBtn.setId(saltIdDownBtn+position);
        holder.favBtn.setTag(this.data.get(position));
        holder.imgView.setVisibility(View.INVISIBLE);
        holder.textView.setText(this.data.get(position).getChannel());
        holder.itemView.setId(saltid+position);
        holder.itemView.setTag(data.get(position));
        String id = this.data.get(position).getStreamUrl().split("/")[3];

        setTheLogo(id,holder,position);

        if (channelBtnDrawable.get(id)!=null) {
            holder.downloadBtn.setImageDrawable(AppCompatResources.getDrawable(ctx, channelBtnDrawable.get(id)));
        }



    }

    @SuppressLint("ResourceType")
    public void setTheLogo(String id, ViewHolder holder,int position){
        if (channelBitmaps.get(id)!=null){
            holder.imgView.setImageBitmap(channelBitmaps.get(id));
            holder.imgView.setVisibility(View.VISIBLE);
        }else {
            rq.getImage(data.get(position).Logo, (resp) -> {
                channelBitmaps.put(id, resp);
                notifyItemChanged(position);
            });
        }
    }


    @Override
    public int getItemCount() {
        return this.data.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final ImageView imgView;
        private final ImageButton downloadBtn ;
        private final ImageButton favBtn;

        @OptIn(markerClass = UnstableApi.class)
        public ViewHolder(View view) {
            super(view);


//           String id = this.data.get(position).getStreamUrl().split("/")[3];

            this.textView = view.findViewById(R.id.air_name);
            this.imgView = view.findViewById(R.id.air_logo);
            this.downloadBtn = view.findViewById(R.id.downloadaction);
            this.favBtn = view.findViewById(R.id.favaction);

            view.setOnClickListener((v)->{
                try {
                    setCards(v.getId()-saltid,this);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            });


            downloadBtn.setOnClickListener((v)->{
                PlayerContent value = (PlayerContent) v.getTag();
                String url = value.getStreamUrl();
                String station = value.getTitle();

                if (DownloadManagerUtil.runtimeValues.get(url.split("/")[3])==null) {
                    if (DownloadManagerUtil.MAX_DOWNLOADS > CacheSingleton.getInstance().getCurrentDownloading()) {
                        try {
                            DownloadManagerUtil.getInstance().startDownload(url, station, v);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        Toast.makeText(ctx, "Max Download Limit Reached", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    DownloadManagerUtil.getInstance().stopDownload(url.split("/")[3]);

                }

            });

            favBtn.setOnClickListener((v)->{
                PlayerContent value = (PlayerContent) v.getTag();
                Toast.makeText(ctx, "Saved :"+value.getChannel(), Toast.LENGTH_SHORT).show();
            });



        }


    public void setCards(int position,ViewHolder holder) throws JSONException {
                        if (data.get(position)!=null) {
                            player.startPlay(position);
                         }
        }
    }



    public void destroyPlayer(){
        this.data=null;
        player.discardService();
        this.player=null;
    }


}

//CacheSingleton.getInstance().getDownloadManager().addListener(new DownloadManager.Listener() {
//    @Override
//    public void onDownloadChanged(@NonNull DownloadManager downloadManager, @NonNull Download download, @Nullable Exception finalException) {
//        DownloadManager.Listener.super.onDownloadChanged(downloadManager, download, finalException);
//
//        if (download.state== Download.STATE_DOWNLOADING) {
//
//            if (Objects.equals(((PlayerContent) downloadBtn.getTag()).streamUrl.split("/")[3], download.request.uri.toString().split("/")[3])) {
//                downloadBtn.setImageDrawable(AppCompatResources.getDrawable(ctx, R.drawable.baseline_downloading_24));
//            }
//        }
//        if (download.state==Download.STATE_STOPPED){
//            if (Objects.equals(((PlayerContent) downloadBtn.getTag()).streamUrl.split("/")[3], download.request.uri.toString().split("/")[3])) {
//                downloadBtn.setImageDrawable(AppCompatResources.getDrawable(ctx, R.drawable.baseline_download_for_offline_24));
//            }
//        }
//
//    }
//
//});