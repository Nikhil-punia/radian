package com.example.exp;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.NotificationCompat;
import androidx.media3.common.DeviceInfo;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Metadata;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.session.MediaController;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaStyleNotificationHelper;
import androidx.media3.session.SessionToken;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerControlView;
import androidx.media3.ui.PlayerView;

import com.example.exp.ui.adapter.recyclerViewAdapter;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class PlayerService {
    private Player playerg = null;
    private MediaSession mds = null;
    private final PlayerView playUi;
    private final Context ctx;
    private final TextView otherUi;
    private final volleyRequestData rq;
    private ImageView artWork;
    private final int notificId =  (int)(Math.random()*1000);


    private int salt;
    public int currIndex ;
    private String curTitle = null;
    private View currentItem;
    private ListenableFuture<MediaController> controllerFuture;
    private ArrayList<PlayerContent> dataSet=null;
    private ViewGroup parentToAll;

    private NotificationManager ntm;
    private NotificationCompat.Builder Nbuilder;


    @OptIn(markerClass = UnstableApi.class)
    public  PlayerService(Context context, PlayerView ui, TextView titleUi){
        this.playUi=ui;
        this.ctx=context;
        this.otherUi = titleUi;
        this.rq = new volleyRequestData(this.ctx);
        dataSet=null;

        this.initializeMediaNotification();
        this.initializeArtWork();

        SessionToken sessionToken = new SessionToken(this.ctx, new ComponentName(this.ctx, MediaSesService.class));
        this.controllerFuture = new MediaController.Builder(this.ctx, sessionToken).buildAsync();

        controllerFuture.addListener(() -> {
            try {
                this.playerg=(Player) controllerFuture.get();
                this.setPlayerListner(this.playerg);
                this.playUi.setPlayer(this.playerg);

            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, MoreExecutors.directExecutor());


    }

    public void setDataSet(ArrayList<PlayerContent> plc) {
        this.dataSet=plc;
    }

    public Player getPlayer() {
        return playerg;
    }

    public ListenableFuture<MediaController> getControllerFuture() {
        return controllerFuture;
    }

    @OptIn(markerClass = UnstableApi.class)
    public void setPlayerListner(Player player){

        player.addListener(
                new androidx.media3.common.Player.Listener() {
                    @SuppressLint("ResourceType")


                    @Override
                    public void onMediaMetadataChanged(MediaMetadata mediaMetadata) {
                        Player.Listener.super.onMediaMetadataChanged(mediaMetadata);
                        Object ty = mediaMetadata.title;

                        if (ty != null && dataSet!=null ) {
                            if (dataSet.get(playerg.getCurrentMediaItemIndex()) != null) {
                                setArtWork(dataSet.get(playerg.getCurrentMediaItemIndex()).Background_Url);
                                currIndex = playerg.getCurrentMediaItemIndex();
                                currentItem = parentToAll.findViewById(currIndex+salt);
                                setPlayingTitle(ty.toString());
                            }
                        }

                    }


                    @Override
                    public void onPlayerError(PlaybackException error) {
                        Player.Listener.super.onPlayerError(error);
                        if (error.errorCode==2004){
                            Toast.makeText(ctx, "Error Playing , Playing Next", Toast.LENGTH_SHORT).show();
                            if (playerg.hasNextMediaItem()) {
                                startPlay(playerg.getNextMediaItemIndex());
                            }else{
                                startPlay(playerg.getNextMediaItemIndex());
                            }
                        }
                    }


                }) ;
    }



    public void setParentToAll(ViewGroup parentToAll) {
        this.parentToAll = parentToAll;
    }

    public void setSalt(int salt) {
        this.salt = salt;
    }

    @OptIn(markerClass = UnstableApi.class)
    public void startPlay(int index){

        if (playerg.isPlaying()){
            Toast.makeText(ctx, "Playing", Toast.LENGTH_SHORT).show();
        }
        playerg.seekToDefaultPosition(index);
        playerg.prepare();
        playerg.play();
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

    @OptIn(markerClass = UnstableApi.class)
    public void initializeArtWork(){
        ViewGroup prt = ((ViewGroup)(((ViewGroup)playUi).getChildAt(0)));
        View g = playUi.findViewById(androidx.media3.ui.R.id.exo_artwork);
        this.artWork = new ImageView(ctx);
        prt.addView(artWork,3);
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
            otherUi.setVisibility(View.VISIBLE);
            this.curTitle = Title;
            otherUi.setText(Title);
            if (Title==null){
                otherUi.setText("Unknown Title");
            }
    }

    public void setArtWork(String url){
            rq.getImage(url, (resp) -> {
                if (resp != null) {
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
            playerg.clearMediaItems();
            playerg.release();
            MediaController.releaseFuture(controllerFuture);
        }
    }


}

