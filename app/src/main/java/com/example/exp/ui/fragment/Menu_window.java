package com.example.exp.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.exp.R;
import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

public class Menu_window extends DialogFragment {

    public static int w = 0;
    public static int h = 0;
    public Context ctx ;
    private View parent ;

    public Menu_window() {
    }


    public static Menu_window getInstance(int width, int height) {
        Menu_window fragment = new Menu_window();
        w=width;
        h=height;
        return fragment;
    }

    public void setCtx(Context ctx) {
        this.ctx = ctx;
    }

    public Context getCtx() {
        return ctx;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Objects.requireNonNull(this.getDialog()).setCanceledOnTouchOutside(true);
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        parent=view;

        view.setLayoutParams(new FrameLayout.LayoutParams(w,h));
        setTabListners();
    }

    private void setTabListners() {
        TabLayout tabs = parent.findViewById(R.id.menu_tab);
        setTheDownloadMenuFragment();
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()){
                    case (0):{
                        setTheDownloadMenuFragment();
                       break;
                    }
                    case (1):{
                        setTheFavMenuFragment();
                        break;
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    private void setTheDownloadMenuFragment() {
        if (getChildFragmentManager().findFragmentByTag("downloadMenuFragment")==null) {
            Download_menu_window inst = Download_menu_window.getInstance();
            inst.setCtx(ctx);
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.menu_inside_frame,inst , "downloadMenuFragment").commit();
        }
    }

    private void setTheFavMenuFragment() {
        if (getChildFragmentManager().findFragmentByTag("favMenuFragment") == null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.menu_inside_frame, Fav_menu_window.getInstance(), "favMenuFragment").commit();
        }
    }

}