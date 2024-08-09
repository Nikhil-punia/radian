package com.example.exp;


import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;


import androidx.activity.EdgeToEdge;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.util.UnstableApi;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;

import androidx.recyclerview.widget.RecyclerView;
import com.example.exp.ui.adapter.recyclerViewAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;


public class radio extends AppCompatActivity {


    private static final String TAG = "log";
    private final volleyRequestData rq = new volleyRequestData(this);
    private recyclerViewAdapter rcView ;
    private int Position = 0;
    ArrayList<PlayerContent> dataSet;


    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_radio);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_sview), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TypedValue typedValue = new TypedValue();
        this.getTheme().resolveAttribute(com.google.android.material.R.attr.colorSurfaceContainerHigh,typedValue,true);

        getWindow().setStatusBarColor(getResources().getColor(typedValue.resourceId, this.getTheme()));


        rq.findCountryRadios(100,1,"India", (result) -> {
            try {
                Position = 0;
                dataSet = new ArrayList<>();
                loopAndSaveChannels(result);
                updateUi(result);
            } catch (JSONException | IOException e) {
                throw new RuntimeException(e);
            }
        });

    }


    public void loopAndSaveChannels(JSONArray stations) throws JSONException {
        if (Position < stations.length()) {
            String url_f = "https://zeno.fm/_next/data/ZyoucVrauhoqKBWqBepDH/radio/" + stations.getJSONObject(Position).getString("url").split("/")[4] + ".json";
            rq.sendRequestObj(url_f, (result) -> {
                try {
                    if (result != null) {
                        JSONObject b_j = result.getJSONObject("pageProps").getJSONObject("station");
                        JSONObject b_o = result.getJSONObject("pageProps").getJSONObject("meta");

                        System.out.println("===================");

                        String uri_t = (b_j.getString("streamURL"));
                        PlayerContent plc = new PlayerContent(b_j.getString("name"), b_j.getString("logo"), b_j.getString("background"), b_o.getString("description"), b_j.getJSONArray("languages"), b_j.getString("genre"), stations.getJSONObject(Position).getString("name"), uri_t);
                        dataSet.add(plc);
                    }

                    Position++;
                    loopAndSaveChannels(stations);
                } catch (JSONException e) {
                    System.out.println(e);
                }
            });
        }else {
            System.out.println("-----------------------------------");
            System.out.println(dataSet);
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        if (rcView != null) {
            rcView.destroyPlayer();
        }

    }

    public void updateUi(JSONArray resp) throws JSONException, IOException {
        rcView = new recyclerViewAdapter(resp,this,findViewById(R.id.player_view_m),findViewById(R.id.main_sview));
        RecyclerView recyclerView = findViewById(R.id.rcl_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this,2));
        recyclerView.setAdapter(this.rcView);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL));
    }


    public Object getComponents(int id){
        return findViewById(id);
    }





}

