package com.example.exp.ui.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.offline.Download;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.exp.logic.singleton.CacheSingleton;
import com.example.exp.R;

import java.text.DecimalFormat;
import java.util.List;


public class Download_window extends Fragment {

    private Context ctx =null;
    public View parentToAll ;
    public LinearLayout dsA;
    public LinearLayout dsB;
    public static Download_window thisClass = new Download_window();

    public Download_window() {

    }

    public static Download_window getInstance(){
        return thisClass;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_download, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        ctx=this.getContext();

        parentToAll=view;
        dsA = view.findViewById(R.id.downloadStatus_a);
        dsB = view.findViewById(R.id.downloadStatus_b);
        setStatusA(dsA);

    }

    @OptIn(markerClass = UnstableApi.class)
    public void setStatusA(LinearLayout view){
        if (view.getChildCount()>0){
            view.removeAllViews();
        }
        float fontSize = 15;
        TextView downloadingNumber = new TextView(ctx);
        TextView totalDownloads = new TextView(ctx);
        TextView totalSize = new TextView(ctx);

        downloadingNumber.setTextSize(fontSize);
        totalDownloads.setTextSize(fontSize);
        totalSize.setTextSize(fontSize);
        downloadingNumber.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        totalDownloads.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        totalSize.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        view.addView(downloadingNumber);
        view.addView(totalDownloads);
        view.addView(totalSize);

        List<Download> downloads = CacheSingleton.getInstance().getDownloadManager().getCurrentDownloads();
        long size = 0;
        for (int i = 0; i < downloads.size(); i++) {

            TextView items = new TextView(ctx);
            items.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            items.setTextSize(fontSize);
            items.setText("Item : "+(i+1) +", Size : "+ CacheSingleton.getInstance().getSizeofDownload(i));

            if (downloads.get(i).state == Download.STATE_DOWNLOADING) {
                LinearLayout itemContainer = new LinearLayout(ctx);
                itemContainer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                itemContainer.setGravity(Gravity.CENTER_HORIZONTAL);
                itemContainer.setGravity(Gravity.CENTER_VERTICAL);
                ImageButton stopButton = new ImageButton(ctx);
                stopButton.setImageDrawable(AppCompatResources.getDrawable(ctx,R.drawable.baseline_stop_circle_24));
                itemContainer.addView(items);
                itemContainer.addView(stopButton);
                view.addView(itemContainer);
            }else {
                view.addView(items);
            }
            size+=downloads.get(i).getBytesDownloaded();
        }

        downloadingNumber.setText("Current Downloading : "+ CacheSingleton.getInstance().getCurrentDownloading());
        totalDownloads.setText("Total Downloads : "+downloads.size());
        totalSize.setText("Total Data Downloaded : "+ new DecimalFormat("0.00").format(size / 1000000.00) + " Mb");

    }


}