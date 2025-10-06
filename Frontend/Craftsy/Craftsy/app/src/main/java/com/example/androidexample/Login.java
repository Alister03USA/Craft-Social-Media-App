package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class Login extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button signupButton;

    private static final String LOGIN_URL = "http://coms-3090-028.class.las.iastate.edu:8080/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.login_username_edt);
        passwordEditText = findViewById(R.id.login_password_edt);
        loginButton = findViewById(R.id.login_login_btn);
        signupButton = findViewById(R.id.login_signup_btn);

        loginButton.setOnClickListener(v -> attemptLogin());
        signupButton.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    private void attemptLogin() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both fields", Toast.LENGTH_SHORT).show();
            return;
        }

        //  Build URL with query params for GET request
        String url = "http://coms-3090-028.class.las.iastate.edu:8080/login/"
                + username + "/" + password;

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    String msg = response.trim();
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

                    if (msg.toLowerCase().contains("successfully")) {
                        Intent intent = new Intent(Login.this, UserProfile.class);
                        intent.putExtra("username", username);
                        startActivity(intent);
                        finish();
                    }
                },
                error -> {
                    String err = (error.networkResponse != null && error.networkResponse.data != null)
                            ? new String(error.networkResponse.data)
                            : error.toString();
                    Toast.makeText(this, "Login failed: " + err, Toast.LENGTH_LONG).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }
}