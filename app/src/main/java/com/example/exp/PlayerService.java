package com.example.exp;

import static androidx.media3.common.Player.COMMAND_SEEK_TO_NEXT;
import static androidx.media3.common.Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM;
import static androidx.media3.common.Player.COMMAND_SEEK_TO_PREVIOUS;
import static androidx.media3.common.Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.core.app.NotificationCompat;
import androidx.media3.common.ForwardingPlayer;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Metadata;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.session.MediaController;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;
import androidx.media3.session.MediaStyleNotificationHelper;
import androidx.media3.session.SessionToken;
import androidx.media3.ui.PlayerView;

import com.example.exp.ui.adapter.recyclerViewAdapter;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.ExecutionException;

public class PlayerService {
    private Player playerg = null;
    private MediaSession mds = null;
    private final PlayerView playUi;
    private final Context ctx;
    private final View otherUi;
    private final volleyRequestData rq;
    private ImageView artWork;
    private final int notificId =  (int)(Math.random()*1000);
    private String mdsId = Integer.toString((int) (Math.random()*1000));

    private String curTitle = null;
    private String metaurl = null;
    private recyclerViewAdapter.ViewHolder currentItem;
    private PlayerContent playData;
    private ListenableFuture<MediaController> controllerFuture;
    private MediaSession mediaSession;

    private NotificationManager ntm;
    private NotificationCompat.Builder Nbuilder;


    @OptIn(markerClass = UnstableApi.class)
    public  PlayerService(Context context, PlayerView ui, View otherUi){
        this.playUi=ui;
        this.ctx=context;
        this.otherUi = otherUi;
        this.rq = new volleyRequestData(this.ctx);

        this.initializeMediaNotification();
        this.initializeArtWork();

        SessionToken sessionToken = new SessionToken(this.ctx, new ComponentName(this.ctx, com.example.exp.MediaSessionService.class));
        this.controllerFuture = new MediaController.Builder(this.ctx, sessionToken).buildAsync();

        controllerFuture.addListener(() -> {
            try {
                this.playUi.setPlayer(controllerFuture.get());
                this.playerg=(Player) controllerFuture.get();

                this.setPlayerListner(playerg);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, MoreExecutors.directExecutor());


    }

    @OptIn(markerClass = UnstableApi.class)
    public void setPlayerListner(Player player){
        player.addListener(
                new androidx.media3.common.Player.Listener() {
                    @SuppressLint("ResourceType")
                    @Override
                    public void onPlaybackStateChanged(int playbackState) {
                        Player.Listener.super.onPlaybackStateChanged(playbackState);
                    }


                    @Override
                    public void onMediaMetadataChanged(MediaMetadata mediaMetadata) {
                        Player.Listener.super.onMediaMetadataChanged(mediaMetadata);

                        System.out.println(mediaMetadata.title);

                        Object ty = mediaMetadata.title;
                        if (ty != null) {
                            setPlayingTitle(ty.toString());

                            if (metaurl != null) {
                                setArtWork(metaurl);
                            }

                        }
                    }
                }) ;
    }


    @OptIn(markerClass = UnstableApi.class)
    public  void StartPlay(String uri, PlayerContent meta, recyclerViewAdapter.ViewHolder view){
        DataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory();
//      HlsMediaSource hlsMediaSourc = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri));
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

    @OptIn(markerClass = UnstableApi.class)
    public MediaMetadata setPlayerContents(PlayerContent meta){
        MediaMetadata.Builder mdt = new MediaMetadata.Builder();
        mdt.setArtist(meta.getChannel());
//        mdt.setDisplayTitle("display title");
        mdt.setAlbumTitle(meta.Title);
        mdt.setGenre(meta.Genre);
        mdt.setDescription(meta.Discription);
        mdt.setArtworkUri(Uri.parse(meta.Background_Url));
        return mdt.build();
    }


    @OptIn(markerClass = UnstableApi.class)

    public void initializeArtWork(){
        this.playUi.setArtworkDisplayMode(PlayerView.ARTWORK_DISPLAY_MODE_FILL);
        this.artWork = playUi.findViewById(androidx.media3.ui.R.id.exo_artwork);
        artWork.setVisibility(View.VISIBLE);
    }

    public void initializeMediaNotification(){
        NotificationManager notificationManager = (NotificationManager) this.ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        this.ntm=notificationManager;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel = new NotificationChannel("radio_1", "Radio Notification", importance);
            notificationManager.createNotificationChannel(notificationChannel);
            Nbuilder = new NotificationCompat.Builder(this.ctx, notificationChannel.getId());
        } else {
            Nbuilder = new NotificationCompat.Builder(this.ctx);
        }
    }

    public void setPlayingTitle(String Title){
            this.curTitle = Title;
            playerg.replaceMediaItem(playerg.getCurrentMediaItemIndex(),playerg.getCurrentMediaItem().buildUpon().setMediaMetadata(playerg.getMediaMetadata().buildUpon().setTitle(Title).build()).build());
            TextView mt = otherUi.findViewById(R.id.title_main2);
            mt.setText(Title);
    }

    public void setArtWork(String url){
        rq.getImage(url,(resp)->{
            if (resp!= null) {
                artWork.setImageBitmap(resp);
                artWork.setVisibility(View.VISIBLE);
            }
        });
    }

    @OptIn(markerClass = UnstableApi.class)
    public void setMediaNotification(PlayerContent data){
        Nbuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.baseline_radio_24)
                .setContentTitle(data.getTitle())
                .setContentText(data.getChannel())
                .setSilent(true)
                .setStyle(new MediaStyleNotificationHelper.MediaStyle(this.mds)
                        .setShowActionsInCompactView(1));
        ntm.notify(notificId, Nbuilder.build());
    }




    public void discardService(){
        if (playerg != null) {
            MediaController.releaseFuture(controllerFuture);
        }
    }

    public void setCurrentItem(recyclerViewAdapter.ViewHolder view){
        currentItem=view;
    }

}

