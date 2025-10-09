package com.example.androidexample;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.androidexample.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * FeedCRUDActivity — working Add, Update, and Delete Feed posts
 * using correct JSON and URLs matching the backend FeedController.
 */
public class FeedCRUDActivity extends AppCompatActivity {

    private EditText editUsername, editProjectName, editDesc, editType, editSupplies, editVisibility;
    private Button buttonAdd, buttonUpdate, buttonDelete;

    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080/feed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_crud);

        editUsername = findViewById(R.id.editUsername);
        editProjectName = findViewById(R.id.editProjectName);
        editDesc = findViewById(R.id.editDesc);
        editType = findViewById(R.id.editType);
        editSupplies = findViewById(R.id.editSupplies);
        editVisibility = findViewById(R.id.editVisibility);

        buttonAdd = findViewById(R.id.buttonAdd);
        buttonUpdate = findViewById(R.id.buttonUpdate);
        buttonDelete = findViewById(R.id.buttonDelete);

        buttonAdd.setOnClickListener(v -> createFeed());
        buttonUpdate.setOnClickListener(v -> updateFeed());
        buttonDelete.setOnClickListener(v -> deleteFeed());
    }

    /** POST /feed/{username} */
    private void createFeed() {
        String username = editUsername.getText().toString().trim();
        if (username.isEmpty()) {
            Toast.makeText(this, "Enter username to post.", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject json = new JSONObject();
        try {
            json.put("projectName", editProjectName.getText().toString());
            json.put("projectDesc", editDesc.getText().toString());
            json.put("projectType", editType.getText().toString());
            json.put("supplies", editSupplies.getText().toString());
            json.put("visibility", editVisibility.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = BASE_URL + "/" + username;
        Log.d("FeedCRUDActivity", "POST " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                json,
                response -> Toast.makeText(this, " Post added successfully!", Toast.LENGTH_SHORT).show(),
                error -> handleError("POST", error)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    /** PUT /feed/{username}/{projectName} */
    private void updateFeed() {
        String username = editUsername.getText().toString().trim();
        String projectName = editProjectName.getText().toString().trim();
        if (username.isEmpty() || projectName.isEmpty()) {
            Toast.makeText(this, "Enter username and project name to update.", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject json = new JSONObject();
        try {
            if (!editDesc.getText().toString().isEmpty())
                json.put("projectDesc", editDesc.getText().toString());
            if (!editType.getText().toString().isEmpty())
                json.put("projectType", editType.getText().toString());
            if (!editSupplies.getText().toString().isEmpty())
                json.put("supplies", editSupplies.getText().toString());
            if (!editVisibility.getText().toString().isEmpty())
                json.put("visibility", editVisibility.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = BASE_URL + "/" + username + "/" + projectName;
        Log.d("FeedCRUDActivity", "PUT " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                json,
                response -> Toast.makeText(this, " Post updated successfully!", Toast.LENGTH_SHORT).show(),
                error -> handleError("PUT", error)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    /** DELETE /feed/{username}/{projectName} */
    private void deleteFeed() {
        String username = editUsername.getText().toString().trim();
        String projectName = editProjectName.getText().toString().trim();

        if (username.isEmpty() || projectName.isEmpty()) {
            Toast.makeText(this, "Enter username and project name to delete.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "/" + username + "/" + projectName;
        Log.d("FeedCRUDActivity", "DELETE " + url);

        com.android.volley.toolbox.StringRequest request = new com.android.volley.toolbox.StringRequest(
                Request.Method.DELETE,
                url,
                response -> {
                    Log.d("FeedCRUDActivity", "Server response: " + response);
                    Toast.makeText(this, "🗑️ " + response, Toast.LENGTH_SHORT).show();
                },
                error -> handleError("DELETE", error)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    /** Handles Volley network errors gracefully */
    private void handleError(String method, com.android.volley.VolleyError error) {
        int status = (error.networkResponse != null) ? error.networkResponse.statusCode : -1;
        Log.e("FeedCRUDActivity", method + " error: " + status, error);

        String message;
        switch (status) {
            case 404: message = "Not Found (Check username/project name)"; break;
            case 500: message = "Server Error (Check backend logs)"; break;
            case -1:  message = "Connection Error (Check network or URL)"; break;
            default:  message = "Error " + status;
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}