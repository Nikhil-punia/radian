package com.example.exp;

import android.app.PendingIntent;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.PlayerNotificationManager;

@UnstableApi
class DescriptionAdapter implements
        PlayerNotificationManager.MediaDescriptionAdapter {


    @NonNull
    @Override
    public CharSequence getCurrentContentTitle(Player player) {
        return player.getCurrentMediaItem().mediaMetadata.title;
    }

    @Nullable
    @Override
    public PendingIntent createCurrentContentIntent(Player player) {
        return null;
    }

    @Nullable
    @Override
    public CharSequence getCurrentContentText(Player player) {
        return null;
    }

    @Nullable
    @Override
    public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
        return null;
    }
}