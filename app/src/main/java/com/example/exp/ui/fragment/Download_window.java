package com.example.exp.ui.fragment;

import android.content.ContentValues;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.fragment.app.Fragment;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.exp.R;
import com.example.exp.logic.download_manager.DownloadManagerUtil;
import com.example.exp.logic.player.PlayerService;
import com.example.exp.ui.adapter.Download_adapter;

import java.util.ArrayList;
import java.util.HashMap;


public class Download_window extends Fragment {

    private PlayerService pS;


    public Download_window() {}

    public static Download_window getInstance(){
        return  new Download_window();
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

        PlayerView playerUi = view.findViewById(R.id.player_view_download);
        pS = new PlayerService(getContext(), playerUi, null);

        RecyclerView recyclerView = view.findViewById(R.id.downloadRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(new Download_adapter(getChannelData(),getContext(),pS));
    }

    @OptIn(markerClass = UnstableApi.class)
    private HashMap<Integer, ArrayList<ContentValues>> getChannelData(){
        ArrayList<String> v = DownloadManagerUtil.getInstance().getChannels();
        HashMap<Integer, ArrayList<ContentValues>> data = new HashMap<>();

        for (int i = 0; i < v.size(); i++) {
            data.put(i, DownloadManagerUtil.getInstance().getChannelDownloads(v.get(i)));
        }

        return data;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        pS.discardService();
    }
}