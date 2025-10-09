package com.example.androidexample;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import android.content.Intent;
import android.widget.Button;


import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;
import com.android.volley.toolbox.JsonObjectRequest;


public class UserProfile extends AppCompatActivity {

    private EditText displayName, username, bio, email, password, craftSpecialties;
    private String loggedInUsername;
    private String loggedInPassword;
    private JSONObject userJson; // ✅ Store user info from login

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loggedInUsername = getIntent().getStringExtra("username");
        loggedInPassword = getIntent().getStringExtra("password");

        try {
            String userJsonString = getIntent().getStringExtra("user_json");
            userJson = new JSONObject(userJsonString);
        } catch (JSONException e) {
            Toast.makeText(this, "Error loading user info", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        showProfileView();
    }

    /** VIEW MODE **/
    private void showProfileView() {
        setContentView(R.layout.activity_user_profile);

        TextView usernameTv = findViewById(R.id.username);
        TextView displayNameTv = findViewById(R.id.displayName);
        TextView bioTv = findViewById(R.id.bio);
        TextView followersTv = findViewById(R.id.followersCount);
        TextView followingTv = findViewById(R.id.followingCount);
        TextView craftSpecialtyTv = findViewById(R.id.CraftSpecialties);

        //  Populate from JSON
        String usernameValue = getJsonString("username");
        String displayNameValue = getJsonString("displayName");

        //  If no display name, fall back to username
        if (displayNameValue == null || displayNameValue.isEmpty() || displayNameValue.equals("null")) {
            displayNameValue = usernameValue;
        }

        usernameTv.setText(usernameValue);
        displayNameTv.setText(displayNameValue);
        bioTv.setText(getJsonString("bio"));
        craftSpecialtyTv.setText(getJsonString("craftSpecialty"));
        followersTv.setText(getJsonCount("followers") + " Followers");
        followingTv.setText(getJsonCount("following") + " Following");

        Button editButton = findViewById(R.id.login_login_btn);
        editButton.setOnClickListener(v -> showEditProfile());

        //  Feed Button - Go to user's feed
        Button feedBtn = findViewById(R.id.buttonFeed);
        feedBtn.setOnClickListener(v -> {
            String u = userJson != null ? userJson.optString("username", "") : loggedInUsername;
            if (u == null || u.trim().isEmpty()) u = "katiekeck";
            Intent i = new Intent(UserProfile.this, FeedActivity.class);
            i.putExtra("username", u);
            startActivity(i);
        });
    }

    /** EDIT MODE **/
    private void showEditProfile() {
        setContentView(R.layout.activity_user_profile_edit);

        displayName = findViewById(R.id.edit_display_name);
        username = findViewById(R.id.edit_username);
        bio = findViewById(R.id.edit_bio);
        email = findViewById(R.id.edit_email);
        password = findViewById(R.id.edit_password);
        craftSpecialties = findViewById(R.id.edit_craft_specialties);

        // ✅ Prefill edit fields from JSON
        username.setText(getJsonString("username"));
        String displayNameValue = getJsonString("displayName");
        if (displayNameValue == null || displayNameValue.isEmpty() || displayNameValue.equals("null")) {
            displayNameValue = getJsonString("username");
        }
        displayName.setText(displayNameValue);

        bio.setText(getJsonString("bio"));
        email.setText(getJsonString("email"));
        password.setText(getJsonString("password"));
        // craftSpecialty: if your spinner uses index-based selection, you'll handle setting the selected item here

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
        String craftType = craftSpecialties.getText().toString().trim(); // EditText

        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Username and password are required!", Toast.LENGTH_LONG).show();
            return;
        }

        // Build JSON matching backend model
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
            Toast.makeText(this, "Error creating profile JSON", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://coms-3090-028.class.las.iastate.edu:8080/user/" + loggedInUsername;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                profileData,
                response -> {
                    // old-style behavior: store returned JSON and refresh view
                    Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();
                    userJson = response; // <-- exactly like your old code
                    // update loggedInUsername if backend returned a new username
                    loggedInUsername = userJson.optString("username", loggedInUsername);
                    showProfileView();
                },
                error -> {
                    String message = "Server error saving profile";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        message = new String(error.networkResponse.data);
                    }
                    Toast.makeText(this, "Error saving profile: " + message, Toast.LENGTH_LONG).show();
                }
        );

        // use the same pattern you used previously
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }




    /** Helper methods **/
    private String getJsonString(String key) {
        return userJson.optString(key, "");
    }

    private int getJsonCount(String key) {
        try {
            return userJson.has(key) && userJson.getJSONArray(key) != null
                    ? userJson.getJSONArray(key).length()
                    : 0;
        } catch (JSONException e) {
            return 0;
        }
    }
}
