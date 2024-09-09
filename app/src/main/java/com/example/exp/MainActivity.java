package com.example.exp;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.DialogFragment;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.offline.Download;
import androidx.media3.exoplayer.offline.DownloadManager;

import com.example.exp.logic.download_manager.DownloadManagerUtil;
import com.example.exp.logic.download_manager.DownloadServices;
import com.example.exp.logic.singleton.CacheSingleton;
import com.example.exp.ui.fragment.Air_window;
import com.example.exp.ui.fragment.Download_window;
import com.example.exp.ui.fragment.Menu_window;
import com.example.exp.ui.fragment.Radio_window;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


@UnstableApi
public class MainActivity extends AppCompatActivity {


    public String apiID = null;
    public DownloadManagerUtil dms;
    private FloatingActionButton menuBtn;
    private FrameLayout menuFrame;
    private ConstraintLayout mainParent;
    private FrameLayout fragmentHolder;


    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_sview), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            System.out.println(systemBars.left+"/"+systemBars.top+"/"+systemBars.right+"/"+systemBars.bottom);

            return insets;
        });

        mainParent=findViewById(R.id.main_sview);
        menuBtn=findViewById(R.id.menubase_btn);
        menuFrame=findViewById(R.id.menubase_util);
        fragmentHolder = findViewById(R.id.dview);

        dms = DownloadManagerUtil.getInstance();
        dms.setCtx(getApplicationContext());
        dms.setParentActivity(this);
        dms.initialize();


        TypedValue typedValue = new TypedValue();
        this.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary,typedValue,true);
        getWindow().setStatusBarColor(getResources().getColor(typedValue.resourceId, this.getTheme()));

        setupMenu();

        TabLayout tabl = findViewById(R.id.tab_l);
        for (int tabs = 0; tabs < tabl.getTabCount(); tabs++) {
            TabLayout.Tab tabi = tabl.getTabAt(tabs);
            if (tabi != null) {
                tabi.view.setTag(tabs);
                tabi.view.setOnClickListener(v->{
                    try {
                        clickedButton(v);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }

        tabl.getTabAt(0).view.performClick();

    }

    @OptIn(markerClass = UnstableApi.class)
    public void clickedButton(View view) throws IOException {

        if (view.getTag()!=null) {

            findViewById(R.id.sbar).setVisibility(View.GONE);

            if (Integer.parseInt(view.getTag().toString())==0) {
//
                if (getSupportFragmentManager().findFragmentByTag("radioZen") == null) {
                    getApiIdAndStartRadio();
                }


            } else if (Integer.parseInt(view.getTag().toString())==1) {
                if (getSupportFragmentManager().findFragmentByTag("radioAir") == null) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.dview, Air_window.getInstance(),"radioAir")
                            .commit();
                }
            } else if (Integer.parseInt(view.getTag().toString())==2) {

                Download_window dwnFrag = Download_window.getInstance();
                if (getSupportFragmentManager().findFragmentByTag("downloads") == null) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.dview, dwnFrag,"downloads")
                            .commit();
                }
            }

        }

    }

    public void getApiIdAndStartRadio() {
        if (apiID==null){

            new Thread(() -> {
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();

                Request request = new Request.Builder()
                        .addHeader("Accept","*/*")
                        .url("https://zeno.fm/radio/hits-of-kishore-kumar/")
                        .method("GET",null)
                        .build();

                try(Response resp = client.newCall(request).execute()){
                    final Pattern pattern = Pattern.compile("_buildManifest.js\" defer=\"\"></script><script src=\"/_next/static/(.+?)/_ssgManifest.js\" defer=\"\"></script>", Pattern.DOTALL);
                    final Matcher matcher = pattern.matcher(resp.body().string());
                    matcher.find();
                    apiID = matcher.group(0).split("/_next/static/")[1].split("/")[0];
                    resp.body().close();
                    startRadio();
                }catch (IOException e){
                   // Toast.makeText(getApplicationContext(), "Can`t Connect To The Server! Try Later", Toast.LENGTH_SHORT).show();
                    System.out.println(e);
                }
            }).start();
    }else {
            startRadio();
        }
    }

    public void startRadio(){
        Radio_window rad = Radio_window.getInstance();
        rad.setApiId(apiID);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.dview, rad,"radioZen")
                .commit();
    }

    public void setupMenu(){

        menuBtn.setOnClickListener((view)->{
            Menu_window inst = Menu_window.getInstance(menuFrame.getWidth(), menuFrame.getHeight());
            inst.setCtx(getApplicationContext());
            inst.setStyle(DialogFragment.STYLE_NO_FRAME, 0);
            inst.show(getSupportFragmentManager(),"menuWindow");
        });

        CacheSingleton.getInstance().getDownloadManager().addListener(new DownloadManager.Listener() {
            @OptIn(markerClass = ExperimentalBadgeUtils.class)
            @Override
            public void onDownloadChanged(DownloadManager downloadManager, Download download, @Nullable Exception finalException) {
                DownloadManager.Listener.super.onDownloadChanged(downloadManager, download, finalException);
                if (download.state==Download.STATE_DOWNLOADING){
                    Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.floatingbtn);
                    menuBtn.startAnimation(animation);
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ArrayList<String> channels = DownloadManagerUtil.getInstance().getChannels();
        for (int i = 0; i < channels.size(); i++) {
            DownloadManagerUtil.getInstance().stopDownload(channels.get(i));
        }

        CacheSingleton.getInstance().destroySingleton();
    }
}