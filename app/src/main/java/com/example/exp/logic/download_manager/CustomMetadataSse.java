package com.example.exp.logic.download_manager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.annotation.RequiresApi;
import androidx.annotation.WorkerThread;
import androidx.core.app.NotificationCompat;
import androidx.media3.common.util.UnstableApi;

import com.example.exp.R;
import com.here.oksse.OkSse;
import com.here.oksse.ServerSentEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Objects;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CustomMetadataSse extends Service {

    private static final String logTag = "CustomMetadataSseService";

    public static HashMap<String, ServerSentEvent> sseList = new HashMap<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(logTag,"Service Destroyed");
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onCreate() {
        super.onCreate();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("Please Wait Download Service Started")
                .setContentTitle("Downloading Items")
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();


        startForeground((int) (Math.random()*Math.pow(10,5)), notification);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(NotificationManager notificationManager){
        String id = ((Math.random()*Math.pow(10,5))+1)+"";
        String Name = "Background Download Service";

        NotificationChannel channel = new NotificationChannel(id, Name, NotificationManager.IMPORTANCE_HIGH);
        notificationManager.createNotificationChannel(channel);
        return id;
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        DownloadManagerUtil util = DownloadManagerUtil.getInstance();


        Bundle data = intent.getExtras();
        assert data != null;
        String id = data.getString("id");

        try {
            getMetadata(id,(title)->{
                boolean firstTime = Objects.requireNonNull(DownloadManagerUtil.runtimeValues.get(id)).getAsBoolean("firstTime");
                Objects.requireNonNull(DownloadManagerUtil.runtimeValues.get(id)).put("firstTime",false);

                String station = Objects.requireNonNull(DownloadManagerUtil.runtimeValues.get(id)).getAsString("station");
                String titlecheck = "Unknown Title "+((long)(Math.random()*Math.pow(10,5)));

                if (!title.isEmpty()) {
                    titlecheck = title.replaceAll("[^\\s.\\w-]","_");
                }

                Objects.requireNonNull(DownloadManagerUtil.runtimeValues.get(id)).put("title", titlecheck);

                if (!firstTime) {
                    Timer t = new java.util.Timer();
                    String finalTitlecheck = titlecheck;
                    t.schedule(
                            new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    util.internalDownload(id, finalTitlecheck, station, false);
                                    t.cancel();
                                }
                            },
                            5000
                    );
                }else {
                    util.internalDownload(id, titlecheck, station, true);
                }
            });
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return Service.START_STICKY;
    }

    public interface messageCallback{
         void callme(String Title);
    }


    public static void getMetadata(String channelId,messageCallback callback) throws InterruptedException {

        OkHttpClient client = new OkHttpClient.Builder().webSocketCloseTimeout(0,TimeUnit.SECONDS).connectTimeout(0,TimeUnit.SECONDS).retryOnConnectionFailure(true).readTimeout(0, TimeUnit.SECONDS).build();
        OkSse okSse = new OkSse(client);


        String url = "https://api.zeno.fm/mounts/metadata/subscribe/"+channelId;
        Request request = new Request.Builder().url(url).build();

        Log.i(logTag,"Requesting a SSE connection to "+url);

        ServerSentEvent sse = okSse.newServerSentEvent(request,
                new ServerSentEvent.Listener() {
                    @Override
                    public void onOpen(ServerSentEvent sse, Response response) {
                        Log.i(logTag,"Opened Connection");
                    }

                    @Override
                    public void onMessage(ServerSentEvent sse, String id, String event, String message) {
                        try {
                            Log.i(logTag,id);
                            Log.i(logTag,event);
                            Log.i(logTag,message);
                            JSONObject jsObj = new JSONObject(message);
                            callback.callme(jsObj.getString("streamTitle"));

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @WorkerThread
                    @Override
                    public void onComment(ServerSentEvent sse, String comment) {
                        Log.i(logTag,"Comment Received "+comment);
                    }

                    @WorkerThread
                    @Override
                    public boolean onRetryTime(ServerSentEvent sse, long milliseconds) {
                        return true; // True to use the new retry time received by SSE
                    }
                    @WorkerThread
                    @Override
                    public boolean onRetryError(ServerSentEvent sse, Throwable throwable, Response response) {
                        Log.e(logTag,"Closed error "+throwable);
                        return true; // True to retry, false otherwise
                    }
                    @WorkerThread
                    @Override
                    public void onClosed(ServerSentEvent sse) {
                        Log.e(logTag,"Closed connection");
                    }

                    @Override
                    public Request onPreRetry(ServerSentEvent sse, Request originalRequest) {
                        Log.i(logTag,"Pre Retry");
                        return originalRequest;
                    }

                });

        sseList.put(channelId,sse);
    }


}
