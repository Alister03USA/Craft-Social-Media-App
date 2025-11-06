package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class SignupActivity extends AppCompatActivity {

    private EditText etUsername, etEmail, etPassword, etConfirmPassword;
    private Button btnSignup, btnGoToDelete;

    // Adjust port based on backend (8080 or 8443 for HTTPS)
    private static final String SIGNUP_URL = "http://coms-3090-028.class.las.iastate.edu:8080/users/signup";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignup = findViewById(R.id.btnSignup);
        btnGoToDelete = findViewById(R.id.btnGoToDelete);

        btnSignup.setOnClickListener(v -> signupUser());

        btnGoToDelete.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, DeleteActivity.class);
            startActivity(intent);
        });
    }

    private void signupUser() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();


        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_LONG).show();
            return;
        }
        JSONObject body = new JSONObject();
        try {
            body.put("username", username);
            body.put("displayName", username); //just for testing
            body.put("email", email);
            body.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, SIGNUP_URL, body,
                response ->{
                    Toast.makeText(this, "Signup successful!", Toast.LENGTH_SHORT).show();
                    // Open UserProfile after successful signup
                    Intent intent = new Intent(SignupActivity.this, Login.class);
                    startActivity(intent);
                    finish();
                },error -> {
                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;
                        String message = new String(error.networkResponse.data);

                        // Match backend password rejection
                        if (statusCode == 400 && message.contains("Password too weak")) {
                            Toast.makeText(this,
                                    "Password too weak! Must be at least 8 characters, contain uppercase, lowercase, and a number.",
                                    Toast.LENGTH_LONG).show();
                        } else if (statusCode == 400) {
                            Toast.makeText(this, "Bad request: " + message, Toast.LENGTH_LONG).show();
                        } else if (statusCode >= 500) {
                            Toast.makeText(this, "Server error: " + message, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Unexpected error (" + statusCode + "): " + message, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this, "Signup failed: " + error.toString(), Toast.LENGTH_LONG).show();
                    }
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}
