package com.example.androidexample;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class UserProfile extends AppCompatActivity {

    private EditText displayName, username, bio, email, password;
    private Spinner gender;
    private String loggedInUsername;
    private String loggedInPassword; // passed from login

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loggedInUsername = getIntent().getStringExtra("username");
        loggedInPassword = getIntent().getStringExtra("password");

        showProfileView();
    }

    /** VIEW MODE **/
    private void showProfileView() {
        setContentView(R.layout.activity_user_profile);
        fetchProfileForView();

        Button editButton = findViewById(R.id.login_login_btn);
        editButton.setOnClickListener(v -> showEditProfile());
    }

    private void fetchProfileForView() {
        //  Reuse login endpoint for GET info
        String url = "http://coms-3090-028.class.las.iastate.edu:8080/login/"
                + loggedInUsername + "/" + loggedInPassword;

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    TextView usernameTv = findViewById(R.id.username);
                    TextView displayNameTv = findViewById(R.id.displayName);
                    TextView bioTv = findViewById(R.id.bio);
                    TextView followersTv = findViewById(R.id.followersCount);
                    TextView followingTv = findViewById(R.id.followingCount);

                    usernameTv.setText(loggedInUsername);
                    displayNameTv.setText("Display: " + loggedInUsername);
                    bioTv.setText(response);

                    followersTv.setText("0 Followers");
                    followingTv.setText("0 Following");
                },
                error -> Toast.makeText(this,
                        "Failed to load profile info", Toast.LENGTH_LONG).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    /** EDIT MODE **/
    private void showEditProfile() {
        setContentView(R.layout.activity_user_profile_edit);

        displayName = findViewById(R.id.edit_display_name);
        username = findViewById(R.id.edit_username);
        bio = findViewById(R.id.edit_bio);
        email = findViewById(R.id.edit_email);
        password = findViewById(R.id.edit_password);
        gender = findViewById(R.id.spinner_gender);

        username.setText(loggedInUsername);

        Button saveButton = findViewById(R.id.btn_save_profile);
        saveButton.setOnClickListener(v -> saveProfile());
    }

    /** SAVE PROFILE **/
    private void saveProfile() {
        String name = displayName.getText().toString().trim();
        String user = username.getText().toString().trim();
        String biography = bio.getText().toString().trim();
        String mail = email.getText().toString().trim();
        String pass = password.getText().toString().trim();
        String craftType = gender.getSelectedItem().toString(); // reuse for craftSpecialties

        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Username and password are required!", Toast.LENGTH_LONG).show();
            return;
        }

        // ✅ Match exactly what backend entity expects (LoginEditUser.java)
        JSONObject profileData = new JSONObject();
        try {
            profileData.put("displayName", name);
            profileData.put("username", user);
            profileData.put("bio", biography);
            profileData.put("craftSpecialties", craftType);
            profileData.put("email", mail);
            profileData.put("password", pass);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = "http://coms-3090-028.class.las.iastate.edu:8080/user/" + loggedInUsername;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                profileData,
                response -> {
                    Toast.makeText(this,
                            "Profile saved successfully!",
                            Toast.LENGTH_SHORT).show();
                    showProfileView();
                },
                error -> {
                    String message = "Server error saving profile";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        message = new String(error.networkResponse.data);
                    }
                    Toast.makeText(this,
                            "Error saving profile: " + message,
                            Toast.LENGTH_LONG).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }
}