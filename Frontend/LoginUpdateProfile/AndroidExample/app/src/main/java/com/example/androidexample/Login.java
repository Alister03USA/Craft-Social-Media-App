package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class Login extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.login_username_edt);
        passwordEditText = findViewById(R.id.login_password_edt);
        loginButton = findViewById(R.id.login_login_btn);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(Login.this, "Please enter both fields", Toast.LENGTH_SHORT).show();
                    return;
                }


                String url = "https://58c924ed-451f-4e7c-9c30-3a2a290e88fe.mock.pstmn.io/login";



                JSONObject body = new JSONObject();
                try {
                    body.put("username", username);
                    body.put("password", password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        url,
                        body,
                        response -> {
                            try {
                                // Adjust to whatever your backend actually sends
                                boolean success = response.getBoolean("success");
                                if (success) {
                                    Toast.makeText(Login.this, "Login successful!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(Login.this, UserProfile.class));
                                    finish();
                                } else {
                                    Toast.makeText(Login.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        },
                        error -> {
                            if (error.networkResponse != null && error.networkResponse.data != null) {
                                String errorBody = new String(error.networkResponse.data);
                                Toast.makeText(Login.this, "Server error: " + errorBody, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(Login.this, "Network error: " + error.toString(), Toast.LENGTH_LONG).show();
                            }
                        }
                );


                Volley.newRequestQueue(Login.this).add(request);
            }
        });
    }
}

