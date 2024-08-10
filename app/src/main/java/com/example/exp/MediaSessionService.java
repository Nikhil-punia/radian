package com.example.exp;

import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;

public class MediaSessionService extends androidx.media3.session.MediaSessionService {
    private MediaSession mediaSession = null;

    // Create your Player and MediaSession in the onCreate lifecycle event
    @Override
    public void onCreate() {
        super.onCreate();
        ExoPlayer player = new ExoPlayer.Builder(this).build();
        mediaSession = new MediaSession.Builder(this, player).build();
    }

    @Nullable
    @Override
    public MediaSession onGetSession(MediaSession.ControllerInfo controllerInfo) {
        return mediaSession;
    }

    // The user dismissed the app from the recent tasks
    @Override
    public void onTaskRemoved(@Nullable Intent rootIntent) {
            stopSelf();
    }

    // Remember to release the player and media session in onDestroy
    @Override
    public void onDestroy() {
        mediaSession.getPlayer().release();
        mediaSession.release();
        mediaSession = null;
        super.onDestroy();
    }
}
