package com.vdusar.radien.logic.singleton;

import android.content.Context;

import androidx.media3.common.util.UnstableApi;
import androidx.media3.database.DatabaseProvider;
import androidx.media3.database.StandaloneDatabaseProvider;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.cache.Cache;
import androidx.media3.datasource.cache.CacheDataSource;
import androidx.media3.datasource.cache.NoOpCacheEvictor;
import androidx.media3.datasource.cache.SimpleCache;
import androidx.media3.exoplayer.offline.Download;
import androidx.media3.exoplayer.offline.DownloadManager;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

@UnstableApi
public class CacheSingleton {
    private static CacheSingleton instance = new CacheSingleton();
    private DatabaseProvider databaseProvider;
    private Cache downloadCache;
    private DefaultHttpDataSource.Factory dataSourceFactory;
    private Executor downloadExecutor;
    private Context ctx;
    private DownloadManager dM;
    private DataSource.Factory cacheDataSourceFactory;

    private CacheSingleton(){

    }


    public void setCacheData(){
        databaseProvider = new StandaloneDatabaseProvider(ctx);
        downloadCache = new SimpleCache(new File(ctx.getFilesDir().getAbsolutePath()+"/data/"), new NoOpCacheEvictor(), databaseProvider);
        dataSourceFactory = new DefaultHttpDataSource.Factory();
        downloadExecutor =  Runnable::run;
        dM = new DownloadManager(ctx, databaseProvider, downloadCache, dataSourceFactory, downloadExecutor);

        cacheDataSourceFactory =
                new CacheDataSource.Factory()
                        .setCache(downloadCache)
                        .setCacheWriteDataSinkFactory(null); // Disable writing.
    }

    public int getCurrentDownloading(){
        int totalDownloading = 0;
        List<Download> downloads = this.getDownloadManager().getCurrentDownloads();

        for (int i = 0; i < downloads.size(); i++) {
            if(downloads.get(i).state == Download.STATE_DOWNLOADING){
                totalDownloading++;
            }
        }
        return totalDownloading;
    }

    public int getTotalDownloads(){
        return this.getDownloadManager().getCurrentDownloads().size();
    }

    public String getTotalDownloadSize(){
        long size =0;
        for (int i = 0; i < this.getDownloadManager().getCurrentDownloads().size(); i++) {
            size+=this.getDownloadManager().getCurrentDownloads().get(i).getBytesDownloaded();
        }
        return new DecimalFormat("0.00").format(size / 1000000.00) + " Mb";
    }

    public String getSizeofDownload(int index){
        String size = "null";
        if (index < this.getDownloadManager().getCurrentDownloads().size()){
            size = new DecimalFormat("0.00").format(this.getDownloadManager().getCurrentDownloads().get(index).getBytesDownloaded() / 1000000.00) + " Mb";
        }
        return size;
    }

    public List<Download> getTotalDownloadingDownloads(){
     List<Download> downloads = new ArrayList<>();
        for (int i = 0; i < this.getDownloadManager().getCurrentDownloads().size(); i++) {
            if ((this.getDownloadManager().getCurrentDownloads().get(i).state)==Download.STATE_DOWNLOADING){
                downloads.add((this.getDownloadManager().getCurrentDownloads().get(i)));
            }
        }
        return downloads;
    }

    public DataSource.Factory getCacheDataSourceFactory() {
        return cacheDataSourceFactory;
    }

    public static CacheSingleton getInstance() {
        return instance;
    }

    public DownloadManager getDownloadManager() {
        return dM;
    }

    public void setCtx(Context context) {
        this.ctx = context;
    }

    public Context getCtx() {
        return ctx;
    }

    public Cache getDownloadCache() {
        return downloadCache;
    }

    public DatabaseProvider getDatabaseProvider() {
        return databaseProvider;
    }

    public DefaultHttpDataSource.Factory getDataSourceFactory() {
        return dataSourceFactory;
    }

    public Executor getDownloadExecutor() {
        return downloadExecutor;
    }

    public void destroySingleton(){
        instance=null;
        downloadCache.release();
        dM.release();
    }
}
