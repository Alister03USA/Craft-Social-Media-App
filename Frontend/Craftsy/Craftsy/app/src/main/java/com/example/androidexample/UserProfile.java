package com.example.androidexample;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.json.JSONException;

import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.Request;
import android.content.Intent;


import androidx.appcompat.app.AppCompatActivity;

public class UserProfile extends BaseActivity {

    private EditText displayName, username, bio, email, password, craftSpecialties, followersTv,followingTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showProfileView();

    }
    /** ------------------- FETCH FOLLOWERS / FOLLOWING COUNTS ------------------- **/
    private void fetchFollowersAndFollowing(String username) {
        // Followers count
        String followersUrl = "http://coms-3090-028.class.las.iastate.edu:8080/" + username + "/followers";
        JsonArrayRequest followersRequest = new JsonArrayRequest(
                Request.Method.GET,
                followersUrl,
                null,
                response -> {
                    TextView followersTv = findViewById(R.id.followersCount);
                    followersTv.setText(response.length() + " Followers");
                },
                error -> {
                    TextView followersTv = findViewById(R.id.followersCount);
                    followersTv.setText("0 Followers");
                }
        );
        VolleySingleton.getInstance(this).addToRequestQueue(followersRequest);

        // Following count
        String followingUrl = "http://coms-3090-028.class.las.iastate.edu:8080/" + username + "/following";
        JsonArrayRequest followingRequest = new JsonArrayRequest(
                Request.Method.GET,
                followingUrl,
                null,
                response -> {
                    TextView followingTv = findViewById(R.id.followingCount);
                    followingTv.setText(response.length() + " Following");
                },
                error -> {
                    TextView followingTv = findViewById(R.id.followingCount);
                    followingTv.setText("0 Following");
                }
        );
        VolleySingleton.getInstance(this).addToRequestQueue(followingRequest);
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
        TextView craftSpecialtiesTv = findViewById(R.id.CraftSpecialties);

        SessionManager session = SessionManager.getInstance();

        String usernameValue = session.getLoggedInUsername();
        String displayNameValue = session.getDisplayName();
        if (displayNameValue == null || displayNameValue.isEmpty()) displayNameValue = usernameValue;

        usernameTv.setText(usernameValue);
        displayNameTv.setText(displayNameValue);
        String bio = session.getBio();
        String craftSpecialties = session.getCraftSpecialties();

        if (bio == null || bio.equals("null") || bio.isEmpty()) {
            bioTv.setVisibility(View.GONE);
        } else {
            bioTv.setVisibility(View.VISIBLE);
            bioTv.setText(bio);
        }

        if (craftSpecialties == null || craftSpecialties.equals("null") || craftSpecialties.isEmpty()) {
            craftSpecialtiesTv.setVisibility(View.GONE);
        } else {
            craftSpecialtiesTv.setVisibility(View.VISIBLE);
            craftSpecialtiesTv.setText(craftSpecialties);
        }
        fetchFollowersAndFollowing(usernameValue);






        Button editButton = findViewById(R.id.editProfile);
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
        Button logout = findViewById(R.id.logout);
        logout.setOnClickListener(v -> {
            session.logout(); // (you’ll add this method if not already there)

            // Navigate to LoginActivity
            Intent intent = new Intent(UserProfile.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
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
