package com.vdusar.radien.logic.player;


import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.core.app.NotificationCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Metadata;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaStyleNotificationHelper;
import androidx.media3.ui.PlayerView;

import com.vdusar.radien.R;
import com.vdusar.radien.logic.volley.volleyRequestData;
import com.vdusar.radien.ui.adapter.Radio_adapter;

public class Player extends Service {


        private final ExoPlayer playerg;
        private final PlayerView playUi;
        private final Context ctx;
        private final ImageView artWork;
        private final View otherUi;
        String metaurl = null;
        String curTitle = null;
        private final volleyRequestData rq;
        private final MediaSession mds;
        private Radio_adapter.ViewHolder currentItem;
        private NotificationCompat.Builder builder = null;
        private PlayerContent playData;
        private final NotificationManager ntm;
        private final int notificId = 5985;
        private final String mdsId = Integer.toString((int) (Math.random()*1000));

    @OptIn(markerClass = UnstableApi.class)
    public  Player(Context context, PlayerView ui, View otherUi){
            this.playUi=ui;
            this.ctx=context;
            this.otherUi = otherUi;
            this.rq = new volleyRequestData(this.ctx);

            ExoPlayer player = new ExoPlayer.Builder(ctx).build();
            MediaSession mediaSession = new MediaSession.Builder(context, player).setId(mdsId).build();

            NotificationManager notificationManager = (NotificationManager) this.ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            this.ntm=notificationManager;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel notificationChannel = new NotificationChannel("radio_1", "Radio Notification", importance);
                notificationManager.createNotificationChannel(notificationChannel);
                builder = new NotificationCompat.Builder(this.ctx, notificationChannel.getId());
            } else {
                builder = new NotificationCompat.Builder(this.ctx);
            }

            playUi.setArtworkDisplayMode(PlayerView.ARTWORK_DISPLAY_MODE_FILL);
            playUi.setPlayer(player);

            this.artWork = playUi.findViewById(androidx.media3.ui.R.id.exo_artwork);
            artWork.setVisibility(View.VISIBLE);

            player.addListener(
                new androidx.media3.common.Player.Listener() {

                    @SuppressLint("ResourceType")
                    @Override
                    public void onMetadata(Metadata metadata) {
                        MediaMetadata.Builder sample = new MediaMetadata.Builder();
                        metadata.get(0).populateMediaMetadata(sample);
                        Object ty = sample.build().title;

                        if (ty!=null) {
                            curTitle = ty.toString();
                            playerg.replaceMediaItem(playerg.getCurrentMediaItemIndex(),playerg.getCurrentMediaItem().buildUpon().setMediaMetadata(playerg.getMediaMetadata().buildUpon().setTitle(ty.toString()).build()).build());
                            TextView mt = otherUi.findViewById(R.id.title_main2);
                            mt.setText(ty.toString());
                        }

                        if (metaurl!=null) {
                            rq.getImage(metaurl,(resp)->{

                                if (resp!= null) {
                                    artWork.setImageBitmap(resp);
                                }else {
                                    artWork.setImageResource(R.drawable.side_nav_bar);
                                }

                                builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                        .setSmallIcon(R.drawable.baseline_radio_24)
                                        .setContentTitle(playData.getTitle())
                                        .setContentText(playData.getChannel())
                                        .setSilent(true)
                                        .setStyle(new MediaStyleNotificationHelper.MediaStyle(mediaSession)
                                                .setShowActionsInCompactView(1));


                                notificationManager.notify(notificId, builder.build());

                            });
                        }

                        artWork.setVisibility(View.VISIBLE);

                    }

                }) ;



        this.playerg = player;
        this.mds = mediaSession;
        }

    @OptIn(markerClass = UnstableApi.class)
    public  void StartPlay(String uri, PlayerContent meta, Radio_adapter.ViewHolder view){
            DataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory();
//          HlsMediaSource hlsMediaSourc = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri));
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri));

            if (playerg.isPlaying()){
                Toast.makeText(ctx, "Playing", Toast.LENGTH_SHORT).show();
            }

            this.metaurl=meta.Background_Url;
            this.currentItem = view;
            this.playData=meta;

            MediaItem tef = mediaSource.getMediaItem().buildUpon().setMediaMetadata(setPlayerContents(meta)).build();
            playerg.setMediaItem(tef);
            playerg.prepare();
            playerg.play();
    }

    public void clearMediaNotificaton(){
        if (this.ntm!=null){
            ntm.cancel(notificId);
        }
    }

    public void releaseSession(){
        if (mds != null) {
            this.mds.release();
        }
    }


    public String getTitle(){
            return this.curTitle;
        }

    @OptIn(markerClass = UnstableApi.class)
    public MediaMetadata setPlayerContents(PlayerContent meta){
        MediaMetadata.Builder mdt = playerg.getMediaMetadata().buildUpon();
        mdt.setAlbumTitle(meta.Title);
        mdt.setGenre(meta.Genre);
        mdt.setDescription(meta.Discription);
        mdt.setArtworkUri(Uri.parse(meta.Background_Url));
        return mdt.build();
    }

    public void releasePlayer(){
        if (playerg != null) {
            this.playerg.release();
        }
    }

    public void setCurrentItem(Radio_adapter.ViewHolder view){
        currentItem=view;
    }

    @OptIn(markerClass = UnstableApi.class)

    public void setNextMediaItem(String uris,PlayerContent meta,int mediaId){
        MediaItem nex =  MediaItem.fromUri(uris);
        // Todo Connect the Player with Card View With The Help of MediaId
        nex = nex.buildUpon().setMediaId(Integer.toString(mediaId)).setMediaMetadata(setPlayerContents(meta)).build();
        playerg.addMediaItem(mediaId,nex);
    }

    public void setPreviousMediaItem(String uris,PlayerContent meta,int mediaId){
        MediaItem pre =  MediaItem.fromUri(uris);
        // Todo Connect the Player with Card View With The Help of MediaId
        pre = pre.buildUpon().setMediaId(Integer.toString(mediaId)).setMediaMetadata(setPlayerContents(meta)).build();
        playerg.addMediaItem(mediaId,pre);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

