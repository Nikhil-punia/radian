package com.vdusar.radien.ui.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.cache.CacheSpan;
import androidx.media3.exoplayer.offline.Download;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.util.concurrent.MoreExecutors;
import com.vdusar.radien.R;
import com.vdusar.radien.logic.database_manager.DatabaseManagerUtil;
import com.vdusar.radien.logic.download_manager.DownloadManagerUtil;
import com.vdusar.radien.logic.player.PlayerService;
import com.vdusar.radien.logic.singleton.CacheSingleton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class Download_adapter extends RecyclerView.Adapter<Download_adapter.ChannelDownloads> {

    private final HashMap<Integer, ArrayList<ContentValues>> data ;
    private final Context ctx;
    private final PlayerService pS;
    private final ArrayList<String> channelList;
    private  ViewGroup parentToALL = null;
    private final String LOCAL_DOWNLOAD_FOLDER = Environment.DIRECTORY_MUSIC;

    @OptIn(markerClass = UnstableApi.class)
    public Download_adapter(HashMap<Integer, ArrayList<ContentValues>> d, Context context, PlayerService pl){

        this.data=d;
        ctx=context;
        pS=pl;
        this.channelList = DownloadManagerUtil.getInstance().getChannels();

        pS.getControllerFuture().addListener(() -> {
            pS.getPlayer().clearMediaItems();
        }, MoreExecutors.directExecutor());

    }



    @NonNull
    @Override
    public ChannelDownloads onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.download_card, parent, false);
        parentToALL=parent;
        return new ChannelDownloads(rowItem);
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onBindViewHolder(@NonNull ChannelDownloads holder, int position) {
        if (!Objects.requireNonNull(data.get(position)).isEmpty()) {
            String chName = Objects.requireNonNull(data.get(position)).get(0).getAsString(DatabaseManagerUtil.Channel_C_Name);
            holder.channelName.setText(chName);

            for (int i = 0; i < Objects.requireNonNull(data.get(position)).size(); i++) {
                try {
                    setDownloadItem(position,i,chName,holder);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void setDownloadItem(int position,int i,String chName,ChannelDownloads holder) throws IOException {
        String title = Objects.requireNonNull(data.get(position)).get(i).getAsString(DatabaseManagerUtil.N_C_Title);
        String downloadId = Objects.requireNonNull(data.get(position)).get(i).getAsString(DatabaseManagerUtil.DownloadId_C_Name);
        Download d = CacheSingleton.getInstance().getDownloadManager().getDownloadIndex().getDownload(downloadId);

        assert d != null;
        String downloadSize = DownloadManagerUtil.getInstance().convertSizeToMb(d.getBytesDownloaded());

        long t = (d.updateTimeMs - d.startTimeMs);
        String time = new DecimalFormat("0.00").format((t / 1000)) + " sec";

        View item = LayoutInflater.from(ctx).inflate(R.layout.download_item_card, null);
        setDownloadItemMargins(item);

        TextView titleName = item.findViewById(R.id.downloadTitle);
        TextView itemSize = item.findViewById(R.id.downloadSize);
        TextView itemTime = item.findViewById(R.id.downloadTime);
        ImageButton deleteItem = item.findViewById(R.id.downloadDelete);
        ImageButton localDownload = item.findViewById(R.id.downloadLocal);

        HashMap<String, Object> deleteTagData = new HashMap<>();
        HashMap<String, Object> localDownloadTagData = new HashMap<>();

        deleteTagData.put("parent",holder.channelItems);
        deleteTagData.put("item",item);
        deleteTagData.put("dId", downloadId);
        deleteTagData.put("holderPosition", position);
        deleteTagData.put("cId",d.request.uri.toString().split("/")[3]);

        localDownloadTagData.put("channelName",chName);
        localDownloadTagData.put("dId",downloadId);
        localDownloadTagData.put("title",title);

        deleteItem.setTag(deleteTagData);
        localDownload.setTag(localDownloadTagData);

        setLocalDownloadButton(localDownload);

        setDeleteButton(holder,deleteItem);
        setPlayer(i,position,d,item);

        titleName.setText(title);
        itemSize.setText(downloadSize);
        itemTime.setText(time);

/*        int c =getRandomColor(50);
      ((LinearLayout)(holder.channelName.getParent())).setBackgroundColor(c);
       ((LinearLayout)titleName.getParent()).setBackgroundColor(c);*/

        holder.channelItems.addView(item);
    }

    private int getRandomColor(int alpha){
        Random r = new Random();
        return Color.argb(alpha,r.nextInt(256),r.nextInt(256),r.nextInt(256));
    }

    private void setDownloadItemMargins(View v){
        FrameLayout.LayoutParams parms = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        parms.setMargins(15, 0, 15, 25);
        v.setLayoutParams(parms);
    }

    @OptIn(markerClass = UnstableApi.class)
    private void setPlayer(int currentLoop,int position,Download d,View v){
        int index = 0;

        for (int j = 0; j < (position + 1); j++) {
            if (j < position) {
                index += Objects.requireNonNull(data.get(j)).size();
            } else {
                index += currentLoop;
            }
        }

        v.setTag(index);

        this.pS.getControllerFuture().addListener(() -> {
            pS.getPlayer().addMediaItem(d.request.toMediaItem());
        }, MoreExecutors.directExecutor());

        v.setOnClickListener((view) -> {
            this.pS.getControllerFuture().addListener(() -> {
                if (view.getTag() != null) {
                    pS.startPlay((Integer) view.getTag());
                    pS.getPlayer().prepare();
                    pS.getPlayer().play();
                }

            }, MoreExecutors.directExecutor());
        });
    }

    @OptIn(markerClass = UnstableApi.class)
    private void setDeleteButton(ChannelDownloads holder,ImageButton deleteItem){
        deleteItem.setOnClickListener((v) -> {

            if (v.getTag() instanceof HashMap) {
                try {
                    HashMap<String, Object> tg = ((HashMap<String, Object>) v.getTag());

                    String cId = (String) tg.get("cId");
                    String dId = (String) tg.get("dId");
                    int channelPosition = (int) tg.get("holderPosition");
                    LinearLayout parent = (LinearLayout) tg.get("parent");
                    FrameLayout itemSingle = (FrameLayout) tg.get("item");

                    int hPos = holder.getAbsoluteAdapterPosition();

                    if (data.get(channelPosition) != null) {
                    int iInd = 0;
                    for (int j = 0; j < Objects.requireNonNull(parent).getChildCount(); j++) {
                        if (itemSingle == parent.getChildAt(j)) {
                            iInd = j;
                        }
                    }
                    Objects.requireNonNull(data.get(channelPosition)).remove(iInd);
                    DownloadManagerUtil.getInstance().removeDownload(cId, dId);

                    parent.removeView(itemSingle);


                    if (data.get(channelPosition).isEmpty()) {
                        data.remove(channelPosition);
                        notifyItemRemoved(hPos);
                    }
                }
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @OptIn(markerClass = UnstableApi.class)
    private void setLocalDownloadButton(ImageButton btn){
        btn.setOnClickListener((viewBtn)->{
            if (viewBtn.getTag()!=null) {

                if (viewBtn.getTag() instanceof HashMap) {
                    try {

                        HashMap<String, Object> tag = (HashMap<String, Object>) viewBtn.getTag();


                String chName = (String) tag.get("channelName");
                String downloadId = (String) tag.get("dId");
                String title = (String) tag.get("title");

                try {
                    assert downloadId != null;
                    String downloadTitle = title + " " + downloadId.split("\\.")[1] + ".mp3";


                    if(!checkFileExist(chName,downloadTitle)) {

                        File finalFile = getAppPublicMusicStorageDir(chName, downloadTitle);
                        FileOutputStream fos = new FileOutputStream(finalFile);
                        for (CacheSpan cacheSpan : CacheSingleton.getInstance().getDownloadCache().getCachedSpans(downloadId)) {
                            saveToLocalFolder(cacheSpan, fos);
                        }
                        Toast.makeText(ctx, "Downloaded Locally", Toast.LENGTH_SHORT).show();
                        fos.close();
                        addMusicToMediaStore(ctx, finalFile, chName, chName, downloadTitle, chName);
                        assert finalFile != null;
                        finalFile.delete();
                    }else {
                        Toast.makeText(ctx, "Downloaded Already Check Your Music Folder", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                    } catch (ClassCastException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }

    @OptIn(markerClass = UnstableApi.class)
    public void saveToLocalFolder(CacheSpan span,FileOutputStream fos) throws IOException {
        File f = span.file;
        FileInputStream fis = new FileInputStream(f);

        byte[] b = new byte[1024];
        int noOfBytesRead;
        while((noOfBytesRead = fis.read(b)) != -1){
            fos.write(b,0,noOfBytesRead);
        }
        fis.close();
    }



        public  void addMusicToMediaStore(Context context, File musicFile, String albumName, String artistName, String songTitle, String subdirectoryName){
            ContentValues values = new ContentValues();
            values.put(MediaStore.Audio.Media.TITLE, songTitle);
            values.put(MediaStore.Audio.Media.ALBUM, albumName);
            values.put(MediaStore.Audio.Media.DISPLAY_NAME, songTitle);
            values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg"); // Or the appropriate MIME type

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                values.put(MediaStore.Audio.Media.RELATIVE_PATH, LOCAL_DOWNLOAD_FOLDER + "/" + subdirectoryName);
                values.put(MediaStore.Audio.Media.IS_PENDING, 1);
            } else {
                File subdirectory = new File(Environment.getExternalStoragePublicDirectory(LOCAL_DOWNLOAD_FOLDER), subdirectoryName);
                if (!subdirectory.exists()) {
                    subdirectory.mkdirs();
                }
                values.put(MediaStore.Audio.Media.DATA, new File(subdirectory, musicFile.getName()).getAbsolutePath());
            }

            Uri uri = context.getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);


                try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
                     FileInputStream fileInputStream = new FileInputStream(musicFile)) {
                    if (outputStream != null) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = fileInputStream.read(buffer)) > 0) {
                            outputStream.write(buffer, 0, length);
                        }
                    }

                    values.clear();
                    values.put(MediaStore.Audio.Media.IS_PENDING, 0);
                    context.getContentResolver().update(uri, values, null, null);
                } catch (Exception e) {
                    // Handle exceptions
                    e.printStackTrace();
                }
        }


        public boolean checkFileExist(String dir,String file){
            boolean check = false;
            File dirt = new File(Environment.getExternalStoragePublicDirectory(LOCAL_DOWNLOAD_FOLDER),dir);
            if (dirt.exists()){
                File files = new File(dirt,file);
                if (files.exists()) {
                    check = true;
                }
            }
            return check;
        }

    @Nullable
    File getAppPublicMusicStorageDir(String dirName,String fileName) throws IOException {
        File file = new File(ctx.getApplicationContext().getExternalFilesDir(LOCAL_DOWNLOAD_FOLDER), dirName);
        file.mkdir();
        File f = new File(file,fileName);
        f.createNewFile();
        return f;
    }




    public class ChannelDownloads extends RecyclerView.ViewHolder{

        private final ImageView channelLogo;
        private final TextView channelName;
        private final LinearLayout channelItems;

        @OptIn(markerClass = UnstableApi.class)
        public ChannelDownloads(View itemView) {
            super(itemView);

            this.channelLogo = itemView.findViewById(R.id.downloadChannelLogo);
            this.channelName = itemView.findViewById(R.id.downloadChannelName);
            this.channelItems = itemView.findViewById(R.id.downloadChannelItems);

        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


}
