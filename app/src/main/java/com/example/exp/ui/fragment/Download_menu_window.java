package com.example.exp.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.fragment.app.Fragment;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.offline.Download;

import com.example.exp.R;
import com.example.exp.logic.download_manager.DownloadManagerUtil;
import com.example.exp.logic.singleton.CacheSingleton;

public class Download_menu_window extends Fragment {

    private static Download_menu_window fragment;
    private Context ctx;

    public Download_menu_window() {
    }

    public void setCtx(Context ctx) {
        this.ctx = ctx;
    }

    public static Download_menu_window getInstance() {
        fragment = new Download_menu_window();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_download_menu_window, container, false);
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setDownloadStatus(view);
        setDownloadItems(view);


        final Handler handler = new Handler();
        final int delay = 1000; // 1000 milliseconds == 1 second

        handler.postDelayed(new Runnable() {
            public void run() {
                setDownloadStatus(view);
                setDownloadItems(view);

                if (fragment.isVisible()){
                    handler.postDelayed(this, delay);
                }
            }
        }, delay);

    }

    @OptIn(markerClass = UnstableApi.class)
    private void setDownloadItems(View viewParent) {
        LinearLayout view = viewParent.findViewById(R.id.menuDownloadItemsParent);
        for (int i = 0; i < CacheSingleton.getInstance().getTotalDownloads(); i++) {
            if ((CacheSingleton.getInstance().getDownloadManager().getCurrentDownloads().get(i).state) == Download.STATE_DOWNLOADING) {
                String id = CacheSingleton.getInstance().getDownloadManager().getCurrentDownloads().get(i).request.uri.toString().split("/")[3];
                if (view.getChildCount() > 0) {
                    boolean isToUpdate = false;
                    for (int j = 0; j < view.getChildCount(); j++) {
                        if (view.getChildAt(j).getTag().toString().equals(id)){
                            isToUpdate =true;
                            updateDownloadItems(viewParent,view.getChildAt(j),i);
                        }
                    }
                    if(!isToUpdate) {
                        addNewView(view, id, i);
                    }
                }else {
                    addNewView(view, id, i);
                }
            }
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void updateDownloadItems(View viewParent, View child,int downloadIndex) {
        String id = child.getTag().toString();
        TextView title = child.findViewById(R.id.downloadMenuItemTitle);
        TextView size = child.findViewById(R.id.downloadMenuItemSize);
        title.setText(getTitle(id));
        size.setText(CacheSingleton.getInstance().getSizeofDownload(downloadIndex));
    }

    @OptIn(markerClass = UnstableApi.class)
    private void addNewView(View view,String id,int i){
        if(getContext()!=null) {
            View v = LayoutInflater.from(getContext()).inflate(R.layout.download_menu_action_card, null);
            TextView title = v.findViewById(R.id.downloadMenuItemTitle);
            TextView size = v.findViewById(R.id.downloadMenuItemSize);
            ImageButton stopBtn = v.findViewById(R.id.dowloadMenuItemStop);
            v.setTag(id);
            stopBtn.setTag(id);
            title.setText(getTitle(id));
            size.setText(CacheSingleton.getInstance().getSizeofDownload(i));

            stopBtn.setOnClickListener((btn) -> {
                if (btn.getTag() != null) {
                    DownloadManagerUtil.getInstance().stopDownload(btn.getTag().toString());
                    ((LinearLayout) view.findViewById(R.id.menuDownloadItemsParent)).removeView(v);
                }
            });

            ((LinearLayout) view.findViewById(R.id.menuDownloadItemsParent)).addView(v);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private String getTitle(String id) {
        String title = "Unknown Title";

        if (DownloadManagerUtil.getRuntimeValues().get(id)!=null) {
            if (DownloadManagerUtil.getRuntimeValues().get(id).get("title")!=null) {
               title = DownloadManagerUtil.getRuntimeValues().get(id).getAsString("title");
            }
        }

        return title;
    }

    @OptIn(markerClass = UnstableApi.class)
    public void setDownloadStatus(View view){
        ((TextView)view.findViewById(R.id.mt1)).setText(CacheSingleton.getInstance().getCurrentDownloading()+"");
        ((TextView)view.findViewById(R.id.mt2)).setText(CacheSingleton.getInstance().getTotalDownloads()+"");
        ((TextView)view.findViewById(R.id.mt3)).setText(CacheSingleton.getInstance().getTotalDownloadSize());
    }
}
