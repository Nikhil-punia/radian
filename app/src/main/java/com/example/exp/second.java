package com.example.exp;

import static java.security.AccessController.getContext;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.session.MediaController;
import androidx.media3.session.MediaStyleNotificationHelper;
import androidx.media3.session.SessionToken;
import androidx.media3.ui.PlayerView;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.ExecutionException;

public class second extends AppCompatActivity {

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_second);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_sview), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//        Intent intent = getIntent();
//        String Text = intent.getStringExtra(MainActivity.MSG);
//        TextView textView = findViewById(R.id.showText);
//        textView.setText(Text);

        PlayerView playerView = findViewById(R.id.player_view_m);

        SessionToken sessionToken =
                new SessionToken(this, new ComponentName(this, MediaSessionService.class));

        ListenableFuture<MediaController> controllerFuture =
                new MediaController.Builder(this, sessionToken).buildAsync();



//        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
//        NotificationCompat.Builder builder = null;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            int importance = NotificationManager.IMPORTANCE_DEFAULT;
//            NotificationChannel notificationChannel = new NotificationChannel("ID", "Name", importance);
//            notificationManager.createNotificationChannel(notificationChannel);
//            builder = new NotificationCompat.Builder(getApplicationContext(), notificationChannel.getId());
//        } else {
//            builder = new NotificationCompat.Builder(getApplicationContext());
//        }
//
//        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//                .setSmallIcon(R.drawable.baseline_radio_24)
//                .setContentTitle("Track title")
//                .setContentText("Artist - Album")
//                .setStyle(new MediaStyleNotificationHelper.MediaStyle(mediaSession)
//                        .setShowActionsInCompactView(1 /* #1: pause button */))
//                .setContentTitle("Wonderful music")
//                .setContentText("My Awesome Band")
////                .setLargeIcon(albumArtBitmap)
//                ;
//
//        notificationManager.notify(1, builder.build());

        controllerFuture.addListener(() -> {
            // Call controllerFuture.get() to retrieve the MediaController.
            // MediaController implements the Player interface, so it can be
            // attached to the PlayerView UI component.
            try {
                playerView.setPlayer(controllerFuture.get());
                MediaController playerg =  controllerFuture.get();
                DataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory();
//              HlsMediaSource hlsMediaSourc = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri));
                MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri("https://stream-149.zeno.fm/nhrtcxg09u8uv?zt=eyJhbGciOiJIUzI1NiJ9.eyJzdHJlYW0iOiJuaHJ0Y3hnMDl1OHV2IiwiaG9zdCI6InN0cmVhbS0xNDkuemVuby5mbSIsInJ0dGwiOjUsImp0aSI6Ik0yTW5SYjhXVFNPZld2RDM1c285bWciLCJpYXQiOjE3MjMyODYxMDEsImV4cCI6MTcyMzI4NjE2MX0.ZO55nfdzh1O5V4GvS_cLj4nRomy9WUeaFBxL0e3Ih8w"));

                MediaItem tef = mediaSource.getMediaItem();

                playerg.setMediaItem(tef);
                playerg.prepare();
                playerg.play();

            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, MoreExecutors.directExecutor());

    }
}