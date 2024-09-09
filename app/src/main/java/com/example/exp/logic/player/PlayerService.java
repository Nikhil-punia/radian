package com.example.exp.logic.player;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.session.MediaController;
import androidx.media3.session.SessionToken;
import androidx.media3.ui.PlayerView;

import com.example.exp.logic.volley.volleyRequestData;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class PlayerService {

    private Player playerExo = null;
    private final PlayerView playUi;
    private final Context ctx;
    private final TextView otherUi;
    private final volleyRequestData rq;
    private ImageView artWork;
    public int currIndex ;
    private final ListenableFuture<MediaController> controllerFuture;
    private ArrayList<PlayerContent> dataSet;

    @OptIn(markerClass = UnstableApi.class)
    public  PlayerService(Context context, PlayerView ui, TextView titleUi){

        this.playUi=ui;
        this.ctx=context;
        this.otherUi = titleUi;
        this.rq = new volleyRequestData(this.ctx);
        this.dataSet=null;
        this.initializeArtWork();
        SessionToken sessionToken = new SessionToken(this.ctx, new ComponentName(this.ctx, MediaSesService.class));
        this.controllerFuture = new MediaController.Builder(this.ctx, sessionToken).buildAsync();

        controllerFuture.addListener(() -> {
            try {
                this.playerExo = controllerFuture.get();
                this.setPlayerListener(this.playerExo);
                this.playUi.setPlayer(this.playerExo);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, MoreExecutors.directExecutor());

    }

    public void setDataSet(ArrayList<PlayerContent> plc) {
        this.dataSet=plc;
    }
    public Player getPlayer() {
        return playerExo;
    }
    public ListenableFuture<MediaController> getControllerFuture() {
        return controllerFuture;
    }

    @OptIn(markerClass = UnstableApi.class)
    public void setPlayerListener(Player player){
        player.addListener(
                new androidx.media3.common.Player.Listener() {
                    @SuppressLint("ResourceType")

                    @Override
                    public void onMediaMetadataChanged(@NonNull MediaMetadata mediaMetadata) {
                        Player.Listener.super.onMediaMetadataChanged(mediaMetadata);
                        Object ty = mediaMetadata.title;

                        if (ty != null && dataSet!=null ) {
                            if (dataSet.get(playerExo.getCurrentMediaItemIndex()) != null) {
                                setArtWork(dataSet.get(playerExo.getCurrentMediaItemIndex()).Background_Url);
                                currIndex = playerExo.getCurrentMediaItemIndex();
                            }
                        }

                        if (ty!=null){
                            setPlayingTitle(ty.toString());
                        }else {
                            setPlayingTitle("Unknown Title");
                        }
                    }

                    @Override
                    public void onPlayerError(@NonNull PlaybackException error) {
                        Player.Listener.super.onPlayerError(error);
                        if (error.errorCode==2004){
                            if (playerExo.hasNextMediaItem()) {
                                startPlay(playerExo.getNextMediaItemIndex());
                                Toast.makeText(ctx, "Error Playing , Playing Next", Toast.LENGTH_SHORT).show();
                            }else{
                                startPlay(playerExo.getPreviousMediaItemIndex());
                                Toast.makeText(ctx, "Error Playing , Playing Previous", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }) ;
    }

    @OptIn(markerClass = UnstableApi.class)
    public void startPlay(int index){
        if (playerExo.isPlaying()){
            Toast.makeText(ctx, "Playing", Toast.LENGTH_SHORT).show();
        }
        playerExo.seekToDefaultPosition(index);
        playerExo.prepare();
        playerExo.play();
    }

    @OptIn(markerClass = UnstableApi.class)
    public void initializeArtWork(){
        ViewGroup prt = ((ViewGroup)(playUi.getChildAt(0)));
//      View g = playUi.findViewById(androidx.media3.ui.R.id.exo_artwork);
        this.artWork = new ImageView(ctx);
        prt.addView(artWork,3);
    }


    public void setPlayingTitle(String Title){
        if (otherUi!=null) {
            otherUi.setVisibility(View.VISIBLE);
            otherUi.setText(Title);
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

    public void discardService(){
        if (playerExo != null) {
            playerExo.clearMediaItems();
            playerExo.release();
            MediaController.releaseFuture(controllerFuture);
        }
    }


}

