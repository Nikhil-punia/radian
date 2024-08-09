package com.example.exp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.view.View;

public class first extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_first);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_sview), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//        Intent intent = getIntent();
//        String Text = intent.getStringExtra(MainActivity.MSG);
//
//        TextView textView = findViewById(R.id.showText);
//        textView.setText(Text);

    }

    public void moveToNavActivity(View v){
       Intent i = new Intent(this, navactivity.class);
       startActivity(i);
    }


}