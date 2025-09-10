package com.example.androidexample;

import static androidx.core.content.ContextCompat.startActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class Sweater extends AppCompatActivity{
    private TextView linkText;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sweater);

        // Link the TextView
        linkText = findViewById(R.id.link);

        // Set click listener to open a URL
        linkText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://www.mamainastitch.com/wp-content/uploads/2017/11/Crocheted-Free-Sweater-Pattern.jpg"; // replace with your sweater pattern URL
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });
    }}