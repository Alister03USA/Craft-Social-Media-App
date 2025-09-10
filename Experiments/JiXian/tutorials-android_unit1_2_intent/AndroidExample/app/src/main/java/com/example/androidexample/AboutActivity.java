package com.example.androidexample;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView aboutText = new TextView(this);
        aboutText.setText("This is the About Page for my modified Intent experiment.");
        aboutText.setTextSize(20);

        setContentView(aboutText);
    }
}
