package com.example.exp.logic.download_manager;

import static androidx.media3.common.util.Assertions.checkNotNull;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.util.NotificationUtil;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.database.DatabaseProvider;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.cache.Cache;
import androidx.media3.exoplayer.offline.Download;
import androidx.media3.exoplayer.offline.DownloadManager;
import androidx.media3.exoplayer.offline.DownloadNotificationHelper;
import androidx.media3.exoplayer.scheduler.Scheduler;

import com.example.exp.R;
import com.example.exp.logic.singleton.CacheSingleton;
import com.example.exp.ui.fragment.Download_window;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.Executor;

@UnstableApi
public class DownloadServices extends androidx.media3.exoplayer.offline.DownloadService {

    private DatabaseProvider databaseProvider;
    private Cache downloadCache;
    private DefaultHttpDataSource.Factory dataSourceFactory;
    private Executor downloadExecutor;
    private Context ctx;

    public DownloadServices() {
        super((int) (Math.random()*Math.pow(10,5)),1000,"channel_id", R.string.download_noti_name,R.string.download_noti_disc);
    }



    @NonNull
    @Override
    protected DownloadManager getDownloadManager() {
        return CacheSingleton.getInstance().getDownloadManager();
    }

    @Nullable
    @Override
    protected Scheduler getScheduler() {
        return null;
    }

    @NonNull
    @Override
    protected Notification getForegroundNotification(List<Download> downloads, int notMetRequirements) {
            NotificationUtil.createNotificationChannel(CacheSingleton.getInstance().getCtx(), "channel_id", R.string.download_noti_name, R.string.download_noti_disc, NotificationUtil.IMPORTANCE_DEFAULT);
            DownloadNotificationHelper downloadNotificationHelper = new DownloadNotificationHelper(CacheSingleton.getInstance().getCtx(), "channel_id");
        int totalDownloading = 0;
        int totalCompleted = 0;
        long totalDownloadSize = 0;

        if (!downloads.isEmpty()) {

            for (int i = 0; i <downloads.size(); i++) {

                if(downloads.get(i).state == Download.STATE_DOWNLOADING){
                    totalDownloading++;
                }

                System.out.println(downloads.get(i).state);

                if(downloads.get(i).stopReason == Download.STATE_COMPLETED){
                    totalCompleted++;
                }
                totalDownloadSize += downloads.get(i).getBytesDownloaded();
            }

        }

            Notification nt = downloadNotificationHelper.buildProgressNotification(
                        this,
                        R.mipmap.ic_launcher,
                        null,
                        "Download Started \b Downloading : "+totalDownloading+",\b Completed : "+totalCompleted+",\b Total Size : "+new DecimalFormat("0.00").format(totalDownloadSize / 1000000.00) + " Mb",
                        downloads,
                        notMetRequirements);

            if (!downloads.isEmpty()) {
                for (int i = 0; i <downloads.size(); i++) {
                    System.out.println("Download no : "+i+" " + new DecimalFormat("0.00").format(downloads.get(i).getBytesDownloaded() / 1000000.00) + " Mb");
                }
            }

            if (Download_window.getInstance().dsA!=null) {
                Download_window.getInstance().setStatusA(Download_window.getInstance().dsA);
            }

            return nt;
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.O) {
            CharSequence name = "Radio Downloads"; // User-visible name of the channel
            String description = "What is downloading "; // Description of the channel
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("channel_id", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}