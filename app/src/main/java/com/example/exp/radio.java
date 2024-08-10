package com.example.exp;


import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ProgressBar;


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
    ProgressBar progBar ;


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

        this.progBar=findViewById(R.id.loadbar_radio);

        TypedValue typedValue = new TypedValue();
        this.getTheme().resolveAttribute(com.google.android.material.R.attr.colorSurfaceContainerHigh,typedValue,true);

        getWindow().setStatusBarColor(getResources().getColor(typedValue.resourceId, this.getTheme()));


        rq.findCountryRadios(100,1,"India", (result) -> {
            try {
                Position = 0;
                progBar.setProgress(0);
                progBar.setVisibility(View.VISIBLE);
                progBar.setMax(result.length());

                dataSet = new ArrayList<>();

                for (int i = 0; i < result.length(); i++) {
                    loopAndSaveChannels(result,i);
                }

            } catch (JSONException | IOException e) {
                throw new RuntimeException(e);
            }
        });

    }


    public void loopAndSaveChannels(JSONArray stations,int index) throws JSONException, IOException {

            String url_f = "https://zeno.fm/_next/data/ZyoucVrauhoqKBWqBepDH/radio/" + stations.getJSONObject(index).getString("url").split("/")[4] + ".json";
            rq.sendRequestObj(url_f, (result) -> {
                try {
                    if (result != null) {
                        JSONObject b_j = result.getJSONObject("pageProps").getJSONObject("station");
                        JSONObject b_o = result.getJSONObject("pageProps").getJSONObject("meta");

                        String uri_t = (b_j.getString("streamURL"));
                        PlayerContent plc = new PlayerContent(b_j.getString("name"), b_j.getString("logo"), b_j.getString("background"), b_o.getString("description"), b_j.getJSONArray("languages"), b_j.getString("genre"), stations.getJSONObject(index).getString("name"), uri_t,stations.getJSONObject(index).getString("url"));
                        dataSet.add(plc);
                    }

                    Position++;
                    progBar.setProgress(Position);

                    if (Position==(stations.length()-1)){
                        progBar.setVisibility(View.GONE);
                        updateUi(dataSet);
                    }

                } catch (JSONException e) {
                    System.out.println(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    protected void onDestroy() {
        super.onDestroy();
        if (rcView != null) {
            rcView.destroyPlayer();
        }
    }

    public void updateUi(ArrayList<PlayerContent> resp) throws JSONException, IOException {
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

