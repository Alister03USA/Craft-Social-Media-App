package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class Login extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button signupButton;

    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080/login/";

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

        String url = BASE_URL + username + "/" + password;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        // Extract info from JSON
                        String displayName = response.optString("displayName", username);
                        String bio = response.optString("bio", "");
                        String email = response.optString("email", "");
                        String craftSpecialties = response.optString("craftSpecialties", "");

                        // Save to singleton
                        SessionManager session = SessionManager.getInstance();
                        session.setLoggedInUsername(username);
                        session.setDisplayName(displayName);
                        session.setBio(bio);
                        session.setEmail(email);
                        session.setCraftSpecialties(craftSpecialties);
                        session.setPassword(password);
                        session.settargetUser("alister_gan");

                        Toast.makeText(this, "Welcome " + displayName + "!", Toast.LENGTH_SHORT).show();

                        // Navigate to next activity
                        Intent intent = new Intent(Login.this, UserProfile.class);
                        startActivity(intent);
                        finish();

                    } catch (Exception e) {
                        Toast.makeText(this, "Login parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    String msg = "Login failed: ";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        msg += new String(error.networkResponse.data);
                    } else {
                        msg += error.getMessage();
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}
