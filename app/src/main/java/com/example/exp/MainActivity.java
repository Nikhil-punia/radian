package com.example.exp;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.tabs.TabLayout;

import java.util.Objects;


public class MainActivity extends AppCompatActivity {


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

        TypedValue typedValue = new TypedValue();
        this.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary,typedValue,true);
        getWindow().setStatusBarColor(getResources().getColor(typedValue.resourceId, this.getTheme()));

        TabLayout tabl = findViewById(R.id.tab_l);

        for (int tabs = 0; tabs < tabl.getTabCount(); tabs++) {
            TabLayout.Tab tabi = tabl.getTabAt(tabs);
            if (tabi != null) {
                tabi.view.setTag(tabs);
                tabi.view.setOnClickListener(v->{
                    clickedButton(v);
                });
            }
        }

        tabl.getTabAt(0).view.performClick();

    }

    public void clickedButton(View view){

        if (view.getTag()!=null) {

            findViewById(R.id.sbar).setVisibility(View.GONE);

//            Intent inttenta = new Intent(this, first.class);

            if (Integer.parseInt(view.getTag().toString())==0) {
//
                if (getSupportFragmentManager().findFragmentByTag("radioz") == null) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.dview, Radio.getInstance(),"radioz")
                            .commit();
                }


            } else if (Integer.parseInt(view.getTag().toString())==1) {
                if (getSupportFragmentManager().findFragmentByTag("radioa") == null) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.dview, Air.getInstance(),"radioa")
                            .commit();
                }
            } else if (Integer.parseInt(view.getTag().toString())==2) {
//                inttenta = new Intent(this, third.class);
            }




        }

//        RecyclerView dynamic = findViewById(R.id.dview);


    }

    public void startRadio(View v){
        Intent inttenta = new Intent(this, radio.class);
        Toast.makeText(MainActivity.this, "Started Radio Centre", Toast.LENGTH_SHORT).show();

        TabLayout tabl = findViewById(R.id.tab_l);
        tabl.selectTab(tabl.getTabAt(0));

        startActivity(inttenta);
    }

}