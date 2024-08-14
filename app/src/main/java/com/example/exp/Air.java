package com.example.exp;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.exp.ui.adapter.airadapter;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class Air extends Fragment {

    private airadapter rcView ;
    public Context ctx;
    public JSONArray jsonFile;
    public BufferedReader bfr;
    public RecyclerView recyclerView;

    public static Air getInstance() {
        Air fragment = new Air();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx=this.getContext();

        try {
            jsonFile = new JSONArray(readFile(R.raw.air));
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_air, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setButtonsAndListners(view);
        recyclerView = view.findViewById(R.id.main_air_view);
        recyclerView.setLayoutManager(new GridLayoutManager(ctx,2));
        recyclerView.addItemDecoration(new DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL));
        recyclerView.addItemDecoration(new DividerItemDecoration(ctx, DividerItemDecoration.HORIZONTAL));

    }

    public void setButtonsAndListners(View view){
        int part = 50;
        int total = getDataParts(jsonFile,part);
        ViewGroup parent = view.findViewById(R.id.page_air);

        for (int i = 0; i < total; i++) {
            Button bt = new Button(ctx);
            bt.setText("P:"+ (i + 1));
            bt.setTag(i);
            parent.addView(bt);

            bt.setOnClickListener((v)->{
                try {
                    selectButton(v);
                    setRecyclerview(v,part,jsonFile);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            });

        }

    }

    public void selectButton(View v){
        ViewGroup parent = (ViewGroup) v.getParent();

        for (int i = 0; i < parent.getChildCount(); i++) {
            if ((int)v.getTag()!=i){
                parent.getChildAt(i).setScaleY(1);
                parent.getChildAt(i).setScaleX(1);
                parent.getChildAt(i).setElevation(0);
                parent.getChildAt(i).setBackground(ContextCompat.getDrawable(ctx,R.drawable.button_draw));
        }else{
                parent.getChildAt((int)v.getTag()).setElevation(50);
                parent.getChildAt((int)v.getTag()).setScaleY(1.5f);
                parent.getChildAt((int)v.getTag()).setScaleX(1.5f);
                parent.getChildAt((int)v.getTag()).setBackground(ContextCompat.getDrawable(ctx, R.drawable.button_select));
            }}
    }

    public void setRecyclerview(View v,int p,JSONArray a) throws JSONException {
        int start = ((int)(v.getTag())*p);
        int end = Math.min((start + p), a.length());
        JSONArray r = new JSONArray();
        for (int i = start; i < end; i++) {
            r.put(a.get(i));
        }
        rcView = new airadapter(ctx,r);
        recyclerView.setAdapter(this.rcView);
    }


    public int getDataParts(JSONArray data, int part){
        int totals = data.length();
        if ((totals%part)==0){
            Toast.makeText(ctx, "Lo bahi", Toast.LENGTH_SHORT).show();
            return (totals/part);
        }else{
            return ((totals/part)+1);
        }
    }


    public String readFile(int id) throws IOException
    {
        bfr = null;
        bfr = new BufferedReader(new InputStreamReader(ctx.getResources().openRawResource(id), "UTF-8"));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = bfr.readLine()) != null)
        {
            content.append(line);
        }
        return content.toString();
    }

    public void closeAll() throws IOException {
        if (bfr != null) {
            bfr.close();
        }
    }

}