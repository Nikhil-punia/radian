package com.example.exp;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.exp.ui.adapter.airadapter;
import com.example.exp.ui.adapter.recyclerViewAdapter;


public class Air extends Fragment {

    private airadapter rcView ;
    public Context ctx;

    public static Air getInstance() {
        Air fragment = new Air();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx=this.getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_air, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String[] data = {"ht","rh","yj","gj","ht","ej"};

        rcView = new airadapter();
        RecyclerView recyclerView = view.findViewById(R.id.main_air_view);
        recyclerView.setLayoutManager(new GridLayoutManager(ctx,2));
        recyclerView.setAdapter(this.rcView);
        recyclerView.addItemDecoration(new DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL));
        recyclerView.addItemDecoration(new DividerItemDecoration(ctx, DividerItemDecoration.HORIZONTAL));

    }
}