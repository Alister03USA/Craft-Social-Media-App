package com.example.androidexample;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * FeedCRUDActivity — unified Add, Edit, and Delete post screen.
 * - Username is auto-fetched from logged-in session (intent extra)
 * - Add/Edit/Delete work based on the mode passed in the intent.
 */
public class FeedCRUDActivity extends AppCompatActivity {

    private EditText editProjectName, editDesc, editType, editSupplies, editVisibility;
    private Button buttonSave, buttonDelete, buttonBack;

    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080/feed";
    private String loggedInUsername; // Automatically filled from session intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_crud);

        // Bind UI elements
        editProjectName = findViewById(R.id.editProjectName);
        editDesc = findViewById(R.id.editDesc);
        editType = findViewById(R.id.editType);
        editSupplies = findViewById(R.id.editSupplies);
        editVisibility = findViewById(R.id.editVisibility);

        buttonSave = findViewById(R.id.buttonSave);
        buttonDelete = findViewById(R.id.buttonDelete);
        buttonBack = findViewById(R.id.buttonBack);

        // Get data from Intent
        String mode = getIntent().getStringExtra("mode");
        loggedInUsername = getIntent().getStringExtra("username"); // Comes from logged-in user

        String projectName = getIntent().getStringExtra("projectName");
        String desc = getIntent().getStringExtra("projectDesc");
        String type = getIntent().getStringExtra("projectType");
        String supplies = getIntent().getStringExtra("supplies");
        String visibility = getIntent().getStringExtra("visibility");

        // Back button → return to FeedActivity
        buttonBack.setOnClickListener(v -> finish());

        // Mode logic
        if ("edit".equals(mode)) {
            // Prefill existing data
            editProjectName.setText(projectName);
            editDesc.setText(desc);
            editType.setText(type);
            editSupplies.setText(supplies);
            editVisibility.setText(visibility);

            // Prevent changing project name
            editProjectName.setEnabled(false);

            buttonSave.setText("Update Post");
            buttonDelete.setEnabled(false);

            buttonSave.setOnClickListener(v -> updateFeed(projectName));

        } else if ("delete".equals(mode)) {
            // Prefill minimal info for confirmation
            editProjectName.setText(projectName);
            editProjectName.setEnabled(false);
            editDesc.setEnabled(false);
            editType.setEnabled(false);
            editSupplies.setEnabled(false);
            editVisibility.setEnabled(false);

            buttonSave.setEnabled(false);
            buttonDelete.setText("Confirm Delete");

            buttonDelete.setOnClickListener(v -> deleteFeed(projectName));

        } else {
            // Default = add mode
            buttonDelete.setEnabled(false);
            buttonSave.setText("Add Post");

            buttonSave.setOnClickListener(v -> createFeed());
        }
    }

    /** POST /feed/{username} — Add new post */
    private void createFeed() {
        if (loggedInUsername == null || loggedInUsername.trim().isEmpty()) {
            Toast.makeText(this, "Session error: username missing. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String projectName = editProjectName.getText().toString().trim();
        if (projectName.isEmpty()) {
            Toast.makeText(this, "Project name cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject json = new JSONObject();
        try {
            json.put("projectName", projectName);
            json.put("projectDesc", editDesc.getText().toString());
            json.put("projectType", editType.getText().toString());
            json.put("supplies", editSupplies.getText().toString());
            json.put("visibility", editVisibility.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = BASE_URL + "/" + loggedInUsername;
        Log.d("FeedCRUDActivity", "POST " + url);

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST, url, json,
                response -> {
                    Toast.makeText(this, " Post added successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    Log.e("FeedCRUDActivity", "Add failed", error);
                    Toast.makeText(this, "Failed to add post", Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    /** PUT /feed/{username}/{projectName} — Update existing post */
    private void updateFeed(String projectName) {
        JSONObject json = new JSONObject();
        try {
            json.put("projectDesc", editDesc.getText().toString());
            json.put("projectType", editType.getText().toString());
            json.put("supplies", editSupplies.getText().toString());
            json.put("visibility", editVisibility.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = BASE_URL + "/" + loggedInUsername + "/" + projectName;
        Log.d("FeedCRUDActivity", "PUT " + url);

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.PUT, url, json,
                response -> {
                    Toast.makeText(this, " Post updated!", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    Log.e("FeedCRUDActivity", "Update failed", error);
                    Toast.makeText(this, "Failed to update post", Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    /** DELETE /feed/{username}/{projectName} — Delete post */
    private void deleteFeed(String projectName) {
        String url = BASE_URL + "/" + loggedInUsername + "/" + projectName;
        Log.d("FeedCRUDActivity", "DELETE " + url);

        com.android.volley.toolbox.StringRequest req = new com.android.volley.toolbox.StringRequest(
                Request.Method.DELETE, url,
                response -> {
                    Toast.makeText(this, "🗑️ Post deleted", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    Log.e("FeedCRUDActivity", "Delete failed", error);
                    Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }
}