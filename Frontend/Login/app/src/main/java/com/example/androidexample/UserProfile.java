package com.example.androidexample;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class UserProfile extends AppCompatActivity {

    // Edit mode fields
    private EditText displayName, username, bio, email, phone, password, birthday;
    private Spinner gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Start in VIEW mode
        showProfileView();
    }

    /** VIEW MODE **/
    private void showProfileView() {
        setContentView(R.layout.activity_user_profile);

        // Fetch profile data from backend for view
        fetchProfileForView();

        // "Edit User" button
        Button editButton = findViewById(R.id.login_login_btn);
        editButton.setOnClickListener(v -> showEditProfile());
    }

    private void fetchProfileForView() {
        String url = "https://339e3baf-7060-4ab8-8b4c-fd31c13daeaa.mock.pstmn.io/userprofile";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        TextView usernameTv = findViewById(R.id.username);
                        TextView displayNameTv = findViewById(R.id.displayName);
                        TextView bioTv = findViewById(R.id.bio);
                        TextView followersTv = findViewById(R.id.followersCount);
                        TextView followingTv = findViewById(R.id.followingCount);

                        usernameTv.setText(response.optString("username", ""));
                        displayNameTv.setText(response.optString("displayName", ""));
                        bioTv.setText(response.optString("bio", ""));

                        // Update follower/following count if available
                        int followers = response.optInt("followers", 0);
                        int following = response.optInt("following", 0);
                        followersTv.setText(followers + " Followers");
                        followingTv.setText(following + " Following");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(UserProfile.this,
                        "Failed to load profile: " + error.getMessage(),
                        Toast.LENGTH_LONG).show()
        );

        VolleySingleton.getInstance(UserProfile.this).addToRequestQueue(request);
    }

    /** EDIT MODE **/
    private void showEditProfile() {
        setContentView(R.layout.activity_user_profile_edit);

        // Bind inputs
        displayName = findViewById(R.id.edit_display_name);
        username = findViewById(R.id.edit_username);
        bio = findViewById(R.id.edit_bio);
        gender = findViewById(R.id.spinner_gender);
        email = findViewById(R.id.edit_email);
        phone = findViewById(R.id.edit_phone);
        password = findViewById(R.id.edit_password);
        birthday = findViewById(R.id.edit_birthday);

        // Fetch existing profile from backend and prefill all fields
        fetchProfileForEdit();

        Button saveButton = findViewById(R.id.btn_save_profile);
        saveButton.setOnClickListener(v -> saveProfile());
    }

    private void fetchProfileForEdit() {
        String url = "https://339e3baf-7060-4ab8-8b4c-fd31c13daeaa.mock.pstmn.io/userprofile";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        displayName.setText(response.optString("displayName", ""));
                        username.setText(response.optString("username", ""));
                        bio.setText(response.optString("bio", ""));
                        email.setText(response.optString("email", ""));
                        phone.setText(response.optString("phone", ""));
                        birthday.setText(response.optString("birthday", ""));
                        password.setText(response.optString("password", "")); // prefill password

                        // Set spinner to correct gender value
                        String genderValue = response.optString("gender", "");
                        for (int i = 0; i < gender.getCount(); i++) {
                            if (gender.getItemAtPosition(i).toString().equalsIgnoreCase(genderValue)) {
                                gender.setSelection(i);
                                break;
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(UserProfile.this,
                                "Error loading profile data", Toast.LENGTH_LONG).show();
                    }
                },
                error -> Toast.makeText(UserProfile.this,
                        "Failed to load profile: " + error.getMessage(),
                        Toast.LENGTH_LONG).show()
        );

        VolleySingleton.getInstance(UserProfile.this).addToRequestQueue(request);
    }

    /** SAVE PROFILE **/
    private void saveProfile() {
        String name = displayName.getText().toString().trim();
        String user = username.getText().toString().trim();
        String biography = bio.getText().toString().trim();
        String gen = gender.getSelectedItem().toString();
        String mail = email.getText().toString().trim();
        String phoneNum = phone.getText().toString().trim();
        String pass = password.getText().toString().trim();
        String bday = birthday.getText().toString().trim();

        // Required fields: username & password
        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Username and password are required!", Toast.LENGTH_LONG).show();
            return;
        }

        JSONObject profileData = new JSONObject();
        try {
            profileData.put("displayName", name);
            profileData.put("username", user);
            profileData.put("bio", biography);
            profileData.put("gender", gen);
            profileData.put("email", mail);
            profileData.put("phone", phoneNum);
            profileData.put("password", pass);
            profileData.put("birthday", bday);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = "https://339e3baf-7060-4ab8-8b4c-fd31c13daeaa.mock.pstmn.io/update";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                profileData,
                response -> {
                    Toast.makeText(UserProfile.this,
                            "Profile saved successfully!",
                            Toast.LENGTH_SHORT).show();
                    // Return to view mode and refresh profile info
                    showProfileView();
                },
                error -> Toast.makeText(UserProfile.this,
                        "Error: " + error.getMessage(),
                        Toast.LENGTH_LONG).show()
        );

        VolleySingleton.getInstance(UserProfile.this).addToRequestQueue(request);
    }
}
