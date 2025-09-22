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

    private EditText editTextUsername, editTextDisplayName, editTextBio, editTextProfilePic,
            editTextCraftSpecialities, editTextEmail, editTextPassword;
    private Button buttonSignup, buttonGoToDelete;

    private RequestQueue requestQueue;
    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080/users/signup";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Bind views
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextDisplayName = findViewById(R.id.editTextDisplayName);
        editTextBio = findViewById(R.id.editTextBio);
        editTextProfilePic = findViewById(R.id.editTextProfilePic);
        editTextCraftSpecialities = findViewById(R.id.editTextCraftSpecialities);
        editTextEmail = findViewById(R.id.editTextEmail);
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
        String displayName = editTextDisplayName.getText().toString().trim();
        String bio = editTextBio.getText().toString().trim();
        String profilePic = editTextProfilePic.getText().toString().trim();
        String craftSpecialities = editTextCraftSpecialities.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Username, Email, and Password are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build JSON body
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", username);
            jsonBody.put("displayName", displayName);
            jsonBody.put("bio", bio);
            jsonBody.put("profilePic", profilePic);
            jsonBody.put("craftSpecialities", craftSpecialities);
            jsonBody.put("followers", 0);   // default value
            jsonBody.put("following", 0);   // default value
            jsonBody.put("email", email);
            jsonBody.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                BASE_URL,
                jsonBody,
                response -> Toast.makeText(SignupActivity.this, "Signup successful!", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(SignupActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show()
        );

        requestQueue.add(request);
    }
}