package com.example.androidexample;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class UserProfile extends AppCompatActivity {

    private EditText displayName, username, bio, email, phone, password, birthday;
    private Spinner gender;
    private String oldPassword = ""; // Store fetched password so we can reuse if unchanged

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Start in view mode
        showProfileView();
    }

    private void showProfileView() {
        setContentView(R.layout.activity_user_profile);

        Button editButton = findViewById(R.id.login_login_btn);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfile();
            }
        });
    }

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

        // Fetch existing profile info
        fetchProfile();

        Button saveButton = findViewById(R.id.btn_save_profile);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfile();
            }
        });
    }

    private void fetchProfile() {
        String url = "https://339e3baf-7060-4ab8-8b4c-fd31c13daeaa.mock.pstmn.io/userprofile";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Pre-fill fields
                            displayName.setText(response.optString("displayName", ""));
                            username.setText(response.optString("username", ""));
                            bio.setText(response.optString("bio", ""));
                            email.setText(response.optString("email", ""));
                            phone.setText(response.optString("phone", ""));
                            birthday.setText(response.optString("birthday", ""));

                            // Save old password separately
                            oldPassword = response.optString("password", "");
                            password.setText(""); // Keep empty in UI for security

                            // Handle gender spinner
                            String genderValue = response.optString("gender", "");
                            String[] genderOptions = getResources().getStringArray(R.array.gender_options);
                            for (int i = 0; i < genderOptions.length; i++) {
                                if (genderOptions[i].equalsIgnoreCase(genderValue)) {
                                    gender.setSelection(i);
                                    break;
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(UserProfile.this,
                                "Failed to load profile: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                }
        );

        VolleySingleton.getInstance(UserProfile.this).addToRequestQueue(request);
    }

    private void saveProfile() {
        // Grab values
        String name = displayName.getText().toString().trim();
        String user = username.getText().toString().trim();
        String biography = bio.getText().toString().trim();
        String gen = gender.getSelectedItem().toString();
        String mail = email.getText().toString().trim();
        String phoneNum = phone.getText().toString().trim();
        String pass = password.getText().toString().trim();
        String bday = birthday.getText().toString().trim();

        // Use old password if user left blank
        if (pass.isEmpty()) {
            pass = oldPassword;
        }

        // Validate required fields
        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Username and password are required!", Toast.LENGTH_LONG).show();
            return;
        }

        // Build JSON
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

        // Send to backend
        String url = "https://339e3baf-7060-4ab8-8b4c-fd31c13daeaa.mock.pstmn.io/update";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                profileData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(UserProfile.this,
                                "Profile saved successfully!",
                                Toast.LENGTH_SHORT).show();
                        showProfileView();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(UserProfile.this,
                                "Error: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                }
        );

        VolleySingleton.getInstance(UserProfile.this).addToRequestQueue(request);
    }
}
