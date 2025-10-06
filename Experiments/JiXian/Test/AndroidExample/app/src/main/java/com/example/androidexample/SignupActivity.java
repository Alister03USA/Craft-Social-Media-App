package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class SignupActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;
    private Button buttonSignup, buttonGoToDelete;
    private RequestQueue requestQueue;

    // Use 10.0.2.2 for Android Emulator → host machine localhost
    private static final String BASE_URL = "http://10.0.2.2:8080/users/signup";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSignup = findViewById(R.id.buttonSignup);
        buttonGoToDelete = findViewById(R.id.buttonGoToDelete);

        requestQueue = Volley.newRequestQueue(this);

        buttonSignup.setOnClickListener(v -> signupUser());
        buttonGoToDelete.setOnClickListener(v ->
                startActivity(new Intent(SignupActivity.this, DeleteActivity.class)));
    }

    private void signupUser() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject body = new JSONObject();
        try {
            body.put("username", username);
            body.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                BASE_URL,
                body,
                response -> Toast.makeText(SignupActivity.this,
                        "Signup Success: " + username, Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(SignupActivity.this,
                        "Signup Failed: " + error.toString(), Toast.LENGTH_LONG).show()
        );

        requestQueue.add(request);
    }
}