package com.example.androidexample;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;
import org.json.JSONException;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.Request;


import androidx.appcompat.app.AppCompatActivity;

public class UserProfile extends BaseActivity {

    private EditText displayName, username, bio, email, password, craftSpecialties;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showProfileView();

    }

    /** ------------------- VIEW MODE ------------------- **/
    private void showProfileView() {
        setContentView(R.layout.activity_user_profile);
        setupBottomNavigation(R.id.nav_my_profile);

        TextView usernameTv = findViewById(R.id.username);
        TextView displayNameTv = findViewById(R.id.displayName);
        TextView bioTv = findViewById(R.id.bio);
        TextView followersTv = findViewById(R.id.followersCount);
        TextView followingTv = findViewById(R.id.followingCount);
        TextView craftSpecialtyTv = findViewById(R.id.CraftSpecialties);

        SessionManager session = SessionManager.getInstance();

        String usernameValue = session.getLoggedInUsername();
        String displayNameValue = session.getDisplayName();
        if (displayNameValue == null || displayNameValue.isEmpty()) displayNameValue = usernameValue;

        usernameTv.setText(usernameValue);
        displayNameTv.setText(displayNameValue);
        bioTv.setText(session.getBio());
        craftSpecialtyTv.setText(session.getCraftSpecialties());
        //followersTv.setText(session.getFollowersCount() + " Followers");
        //followingTv.setText(session.getFollowingCount() + " Following");

        Button editButton = findViewById(R.id.login_login_btn);
        editButton.setOnClickListener(v -> showEditProfile());
    }

    /** ------------------- EDIT MODE ------------------- **/
    private void showEditProfile() {
        setContentView(R.layout.activity_user_profile_edit);


        displayName = findViewById(R.id.edit_display_name);
        username = findViewById(R.id.edit_username);
        bio = findViewById(R.id.edit_bio);
        email = findViewById(R.id.edit_email);
        password = findViewById(R.id.edit_password);
        craftSpecialties = findViewById(R.id.edit_craft_specialties);

        SessionManager session = SessionManager.getInstance();

        String usernameValue = session.getLoggedInUsername();
        String displayNameValue = session.getDisplayName();
        if (displayNameValue == null || displayNameValue.isEmpty()) displayNameValue = usernameValue;

        username.setText(usernameValue);
        displayName.setText(displayNameValue);
        bio.setText(session.getBio());
        email.setText(session.getEmail());
        password.setText(session.getPassword());
        craftSpecialties.setText(session.getCraftSpecialties());

        Button saveButton = findViewById(R.id.btn_save_profile);
        saveButton.setOnClickListener(v -> saveProfile());
    }

    /** ------------------- SAVE PROFILE ------------------- **/
    private void saveProfile() {
        String name = displayName.getText().toString().trim();
        String user = username.getText().toString().trim();
        String biography = bio.getText().toString().trim();
        String mail = email.getText().toString().trim();
        String pass = password.getText().toString().trim();
        String craftType = craftSpecialties.getText().toString().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Username and password are required!", Toast.LENGTH_LONG).show();
            return;
        }

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

        String url = "http://coms-3090-028.class.las.iastate.edu:8080/user/" + SessionManager.getInstance().getLoggedInUsername();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                profileData,
                response -> {
                    Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();

                    // Update SessionManager with new info
                    SessionManager session = SessionManager.getInstance();
                    session.setLoggedInUsername(user);
                    session.setDisplayName(name);
                    session.setBio(biography);
                    session.setEmail(mail);
                    session.setPassword(pass);
                    session.setCraftSpecialties(craftType);

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

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}
