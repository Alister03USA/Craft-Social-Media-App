package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView usernameTxt;
    private Button loginBtn, signupBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usernameTxt = findViewById(R.id.main_username_txt);
        loginBtn = findViewById(R.id.main_login_btn);
        signupBtn = findViewById(R.id.main_signup_btn);

        // Show username if passed back
        Intent intent = getIntent();
        if (intent.hasExtra("username")) {
            String name = intent.getStringExtra("username");
            usernameTxt.setText("Welcome, " + name + "!");
        }

        loginBtn.setOnClickListener(v -> {
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
        });

        signupBtn.setOnClickListener(v -> {
            Intent signupIntent = new Intent(MainActivity.this, SignupActivity.class);
            startActivity(signupIntent);
        });
    }
}
