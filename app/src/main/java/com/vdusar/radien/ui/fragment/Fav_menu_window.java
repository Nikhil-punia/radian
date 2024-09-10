package com.vdusar.radien.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.vdusar.radien.R;


public class Fav_menu_window extends Fragment {


    public Fav_menu_window() {
        // Required empty public constructor
    }


    public static Fav_menu_window getInstance() {
        Fav_menu_window fragment = new Fav_menu_window();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fav_menu_window, container, false);
    }
}