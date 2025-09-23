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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Start in VIEW mode
        showProfileView();
    }

    private void showProfileView() {
        setContentView(R.layout.activity_user_profile);

        // Find "Edit User" button
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

        Button saveButton = findViewById(R.id.btn_save_profile);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfile();
            }
        });
    }

    private void saveProfile() {
        // Grab values from edit layout
        EditText displayName = findViewById(R.id.edit_display_name);
        EditText username = findViewById(R.id.edit_username);
        EditText bio = findViewById(R.id.edit_bio);
        Spinner gender = findViewById(R.id.spinner_gender);

        String name = displayName.getText().toString().trim();
        String user = username.getText().toString().trim();
        String biography = bio.getText().toString().trim();
        String gen = gender.getSelectedItem().toString();

        // Build JSON body
        JSONObject profileData = new JSONObject();
        try {
            profileData.put("displayName", name);
            profileData.put("username", user);
            profileData.put("bio", biography);
            profileData.put("gender", gen);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Replace with your backend endpoint
        String url = "http://10.0.2.2:8080/api/profile/update";

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
                        // Go back to profile view after success
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

        // Add request to VolleySingleton queue
        //VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}
