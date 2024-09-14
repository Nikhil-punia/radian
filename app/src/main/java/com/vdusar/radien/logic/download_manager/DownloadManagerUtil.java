package com.vdusar.radien.logic.download_manager;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.offline.Download;
import androidx.media3.exoplayer.offline.DownloadRequest;

import com.vdusar.radien.R;
import com.vdusar.radien.logic.database_manager.DatabaseManagerUtil;
import com.vdusar.radien.logic.singleton.CacheSingleton;
import com.here.oksse.ServerSentEvent;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@UnstableApi
public class DownloadManagerUtil {

    private static Context ctx;
    private static DatabaseManagerUtil DBM;
    private static final String Download_State_Downloading = "d";
    private static final String Download_State_Completed= "c";
    private static final String Download_State_Stoped = "s";
    private static final String Download_Condition_Complete = "c";
    private static final String Download_Condition_Incomplete = "i";

    public static DownloadManagerUtil instance = new DownloadManagerUtil();
    public HashMap<String, View> uiViews = new HashMap<>();
    public HashMap<String, ServerSentEvent> sseList = CustomMetadataSse.sseList;
    public static AppCompatActivity parentActivity;
    private static final String logTag = "DownloadManagerUtil";
    public static HashMap<String, ContentValues> runtimeValues = new HashMap<>();
    private final List<AlertDialog> dialogList = new ArrayList<>();
    private final HashMap<String, Intent> serviceList =  new HashMap<>();
    public static int MAX_DOWNLOADS = 3;
    public static String NOTIFICATION_CHANNEL_NAME = "Radio_channel_download";


    public static HashMap<String, ContentValues> getRuntimeValues() {
        return runtimeValues;
    }

    public interface downloadDialog{
        void callmeTrue();
        void callmeFalse();
    }



    @OptIn(markerClass = UnstableApi.class)
    public DownloadManagerUtil() {

    }

    public void setParentActivity(AppCompatActivity parentActivity) {
        DownloadManagerUtil.parentActivity = parentActivity;
    }

    public void setCtx(Context context) {
        ctx = context;
    }

    public static DownloadManagerUtil getInstance() {
        return instance;
    }



    public void initialize(){
        CacheSingleton.getInstance().setCtx(ctx);
        CacheSingleton.getInstance().setCacheData();
        DBM = new DatabaseManagerUtil(ctx);
    }

    public void startDownload(String url, String station, View ui) throws InterruptedException {
        String id = url.split("/")[3];

        uiViews.put(id,ui);


        ContentValues v = new ContentValues();
        v.put("id",id);
        v.put("station",station);
        v.put("firstTime",true);

        runtimeValues.put(id,v);

        if (sseList.get(id)==null){
            startTheMetadataService(id);
        }else {
            Log.i(logTag,"Already Given A Listener");
        }


    }



    @OptIn(markerClass = UnstableApi.class)
    public void startDs(String id,String cId){
        DownloadRequest downloadRequest = new DownloadRequest.Builder(id, Uri.parse("https://stream.zeno.fm/"+cId)).setCustomCacheKey(id).build();
        DownloadServices.sendAddDownload(parentActivity.getApplicationContext(), DownloadServices.class, downloadRequest,true);
    }

    @OptIn(markerClass = UnstableApi.class)
    public static void stopDs(String id){
        DownloadServices.sendSetStopReason(parentActivity.getApplicationContext(), DownloadServices.class, id, Download.STATE_COMPLETED,true);
    }

    @OptIn(markerClass = UnstableApi.class)
    public void clearDs(String id){
        DownloadServices.sendRemoveDownload(parentActivity.getApplicationContext(), DownloadServices.class, id, true);
    }

    public void startTheMetadataService(String id){
            Intent wha = new Intent(parentActivity,CustomMetadataSse.class);
            serviceList.put(id,wha);
            wha.putExtra("id",id);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                parentActivity.startForegroundService(wha);
            }else {
                parentActivity.startService(wha);
            }

    }

    public void checkStopPrevious(String id,String title){
        ArrayList<ContentValues> v = DBM.getChannelDownloads(id);
        boolean[] condition = checkIsDownloadingAlready(id,title);

        if (!condition[0]) {
            if (!condition[1]) {
                checkAndStopPreviousDownload(v, id);
            }
        }else {
            checkAndStopPreviousDownload(v, id);
        }
    }

    public void internalDownload(String id,String title, String station,boolean firstDownload) {

        clearPreviousDialogs();
        ArrayList<ContentValues> v = DBM.getChannelDownloads(id);
        boolean[] condition = checkIsDownloadingAlready(id,title);

        String con ;

        if (firstDownload){
            con = Download_Condition_Incomplete;
        }else {
            con = Download_Condition_Complete;
        }

        // todo : condition 0 is to check the download is present but stopped
        // todo : condition 1 is to check the download is present and downloading

        if (!condition[0]) {
            if (!condition[1]) {

                // todo : method for initiation of a new download
                realDownload(id,station,title,con);

            }else {
                Log.i(logTag,"Downloading Already");
            }
        }else {
            askDownloadAgain(title, new downloadDialog() {
                @Override
                public void callmeTrue() {
                    Log.i(logTag,"Download Again signal sent");
                    realDownload(id,station,title,con);
                }

                @Override
                public void callmeFalse() {
                    Log.i(logTag,"Next Download signal sent");
                }
            });
        }
    }

    public void clearPreviousDialogs(){
        for (int i = 0; i < dialogList.size(); i++) {
            if(dialogList.get(i).isShowing()){
                dialogList.get(i).dismiss();
            }
        }
        dialogList.clear();
    }

    private void realDownload(String id, String station, String title, String condition){
        String randomId = id +"."+ (long) (Math.random()*Math.pow(10,10));
        Objects.requireNonNull(runtimeValues.get(id)).put("dId",randomId);

        DBM.createChannelTable(id);
        DBM.appendValueInTable(id, station, title ,randomId, Download_State_Downloading,condition);
        startDs(randomId,id);

        ArrayList<ContentValues> c = DBM.getChannelDownloads(id);
        for (int i = 0; i < c.size(); i++) {
            Log.i(logTag,c.get(i).get(DatabaseManagerUtil.Id_C_Name).toString());
            Log.i(logTag,c.get(i).get(DatabaseManagerUtil.Channel_C_Name).toString());
            Log.i(logTag,c.get(i).get(DatabaseManagerUtil.N_C_Title).toString());
            Log.i(logTag,c.get(i).get(DatabaseManagerUtil.DownloadId_C_Name).toString());
            Log.i(logTag,c.get(i).get(DatabaseManagerUtil.DownloadState_C_Name).toString());
            Log.i(logTag,c.get(i).get(DatabaseManagerUtil.DownloadCondition_C_Name).toString());
        }
    }

    private void askDownloadAgain(String title,downloadDialog callback) {
        new Handler(Looper.getMainLooper()).post(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);
            builder.setTitle("Download Already Exists").setMessage(title+" Already downloaded but stopped in between , you can download it again from the current time or wait for the next one ?");
            builder.setPositiveButton("Download Again", (dialog, id) -> {
                callback.callmeTrue();
            });
            builder.setNegativeButton("Download Next", (dialog, id) -> {
                callback.callmeFalse();
            });
            AlertDialog dialog = builder.create();
            dialog.setIcon(R.mipmap.ic_launcher);
            dialog.show();
            dialogList.add(dialog);
        });
    }


    public static void checkAndStopPreviousDownload(ArrayList<ContentValues> v, String cId) {
        if ((v!=null) && (!v.isEmpty())){
            int lastIndex = (v.size()-1);
            if (v.get(lastIndex).get(DatabaseManagerUtil.DownloadState_C_Name).toString().equals(Download_State_Downloading)) {
                if (!(Objects.requireNonNull(runtimeValues.get(cId)).getAsBoolean("firstTime"))){
                    String rowId = v.get(lastIndex).getAsString(DatabaseManagerUtil.Id_C_Name);
                    DBM.updateDownloadingState(Download_State_Completed,cId,rowId);
                    Log.i(logTag,"Downloaded : " + v.get(lastIndex).get(DatabaseManagerUtil.Id_C_Name) + " With Id : "+v.get(lastIndex).get(DatabaseManagerUtil.DownloadId_C_Name));
                    stopDs(v.get(lastIndex).get(DatabaseManagerUtil.DownloadId_C_Name).toString());
                }
            }
        }
    }

    private boolean[] checkIsDownloadingAlready(String id,String title){
        ArrayList<ContentValues> v = DBM.getChannelDownloads(id);
        boolean[] isDownloading = {false,false};
        if (v!=null) {
            for (int i = 0; i < v.size(); i++) {
                if (v.get(i).get(DatabaseManagerUtil.N_C_Title).toString().equals(title)) {
                    if (v.get(i).get(DatabaseManagerUtil.DownloadState_C_Name).toString().equals(Download_State_Stoped)) {
                        isDownloading[0] = true;
                    }
                    isDownloading[1] = true;
                }
            }
        }
        return isDownloading;
    }


    public void stopDownload(String channelId){
        ArrayList<ContentValues> c = DBM.getChannelDownloads(channelId);
        if (c != null ) {

            for (int i = 0; i < c.size(); i++) {
                if (c.get(i).get(DatabaseManagerUtil.DownloadState_C_Name).toString().equals(Download_State_Downloading)) {
                    String rowId = c.get(i).getAsString(DatabaseManagerUtil.Id_C_Name);
                    DBM.updateDownloadingState(Download_State_Stoped, channelId, rowId);
                    stopDs(c.get(i).get(DatabaseManagerUtil.DownloadId_C_Name).toString());
                    Log.i(logTag,"Stopping : " + c.get(i).get(DatabaseManagerUtil.Id_C_Name) + " With Id : " + c.get(i).get(DatabaseManagerUtil.DownloadId_C_Name));
                }
            }

            closeAndRemoveSSE(channelId);
            stopAndRemoveSSEService(channelId);
            removeRuntimevalues(channelId);
            removeUiView(channelId);
        }
    }

    public void stopRemoveAllDownloads(String channelId){
        stopDownload(channelId);
        removeAllDownloads(channelId);
    }

    public void removeAllDownloads(String channelId){
        ArrayList<ContentValues> c = DBM.getChannelDownloads(channelId);
        if (c!=null) {
            for (int i = 0; i < c.size(); i++) {
                clearDs(c.get(i).get(DatabaseManagerUtil.DownloadId_C_Name).toString());
            }
            DBM.removeChannelDownloads(channelId);
            Log.i(logTag,"Removed All Downloads");
        }
    }

    public void removeDownload(String channelId,String downloadId){
        ArrayList<ContentValues> c = DBM.getChannelDownloads(channelId);
        if (c!=null) {
            for (int i = 0; i < c.size(); i++) {
                if (c.get(i).get(DatabaseManagerUtil.DownloadId_C_Name).toString().equals(downloadId)) {
                    if (c.get(i).get(DatabaseManagerUtil.DownloadState_C_Name).toString().equals(Download_State_Downloading)) {
                        stopDownload(channelId);
                    }
                    Log.i(logTag,"Removing : " + downloadId);
                    clearDs(c.get(i).get(DatabaseManagerUtil.DownloadId_C_Name).toString());
                    DBM.removeDownloadFromDatabase(channelId, c.get(i).get(DatabaseManagerUtil.Id_C_Name).toString());
                }
            }
        }

    }

    public void removeUiView(String channelId){
        if (uiViews.get(channelId)!=null){
            uiViews.remove(channelId);
        }
    }

    public void closeAndRemoveSSE(String channelId){
        if (sseList!=null){
            if (sseList.get(channelId) != null) {
                Objects.requireNonNull(sseList.get(channelId)).close();
                sseList.remove(channelId);
            }
        }
    }

    public void stopAndRemoveSSEService(String channelId){
        if (serviceList.get(channelId)!=null){
            parentActivity.stopService(serviceList.get(channelId));
            serviceList.remove(channelId);
        }
    }

    public void removeRuntimevalues(String channelId){
        if (runtimeValues.get(channelId)!=null){
            runtimeValues.remove(channelId);
        }
    }

    public ArrayList<String> getChannels(){

        ArrayList<String> v = DBM.getAllChannel();
        ArrayList<String> n = new ArrayList<>();

        for (int i = 0; i < v.size(); i++) {
            if (!DownloadManagerUtil.getInstance().checkTableEmpty(v.get(i))) {
                n.add(v.get(i));
            }
        }

        return n;
    }

    public ArrayList<ContentValues> getChannelDownloads(String id){
        return DBM.getChannelDownloads(id);
    }

    public String convertSizeToMb(long size){
        return new DecimalFormat("0.00").format(size / 1000000.00) + " Mb";
    }

    public boolean checkTableEmpty(String id){
        return DBM.checkTableEmpty(id);
    }

}
