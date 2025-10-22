package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button openFeed = findViewById(R.id.openFeed);
        Button openUpload = findViewById(R.id.openUpload);

        openFeed.setOnClickListener(v ->
                startActivity(new Intent(this, TutorialFeedActivity.class)));

        openUpload.setOnClickListener(v ->
                startActivity(new Intent(this, TutorialUploadActivity.class)));
    }
}