package com.example.androidexample;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        TextView welcomeText = findViewById(R.id.welcome_text);
        Button backBtn = findViewById(R.id.back_btn);

        // Get the name from the Intent
        String username = getIntent().getStringExtra("username");
        welcomeText.setText("Welcome to Craftsy, " + username + "!");

        backBtn.setOnClickListener(v -> finish());
    }
}