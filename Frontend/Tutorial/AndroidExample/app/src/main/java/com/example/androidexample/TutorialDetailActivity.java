package com.example.androidexample;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TutorialDetailActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080/tutorial/";
    private EditText titleInput, descInput, categoryInput;
    private VideoView videoView;
    private Button deleteBtn, editBtn;
    private String tutorialId;
    private String username;
    private String fileUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial_detail);

        titleInput = findViewById(R.id.tutorialTitleInput);
        descInput = findViewById(R.id.tutorialDescriptionInput);
        categoryInput = findViewById(R.id.tutorialCategoryInput);
        videoView = findViewById(R.id.videoPreview);
        editBtn = findViewById(R.id.editTutorialBtn);
        deleteBtn = findViewById(R.id.deleteTutorialBtn);

        tutorialId = getIntent().getStringExtra("tutorialId");
        username = getIntent().getStringExtra("username");
        if (username == null) username = "Fuji";

        fetchTutorialDetails();

        deleteBtn.setOnClickListener(v -> deleteTutorial());
        editBtn.setOnClickListener(v -> editTutorial());
    }

    private void fetchTutorialDetails() {
        String url = BASE_URL + tutorialId;
        Log.d("DETAIL", "Fetching details from: " + url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.has("fileUrl")) {
                            fileUrl = response.getString("fileUrl");
                        } else if (response.has("fileURL")) {
                            fileUrl = response.getString("fileURL");
                        } else {
                            fileUrl = null;
                        }

                        titleInput.setText(response.optString("title", ""));
                        descInput.setText(response.optString("description", ""));
                        categoryInput.setText(response.optString("category", ""));

                        if (fileUrl != null && !fileUrl.isEmpty()) {
                            playVideo(fileUrl);
                        } else {
                            Toast.makeText(this, "No video URL found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("DETAIL", "Parse error", e);
                        Toast.makeText(this, "Error loading tutorial details", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("DETAIL", "Fetch failed", error);
                    Toast.makeText(this, "Failed to load tutorial", Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void playVideo(String url) {
        try {
            videoView.setVideoURI(Uri.parse(url));
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);
            videoView.start();
        } catch (Exception e) {
            Log.e("DETAIL", "Video load error", e);
            Toast.makeText(this, "Unable to play video", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteTutorial() {
        String url = BASE_URL + tutorialId;
        Log.d("DETAIL", "Deleting tutorial: " + url);

        StringRequest request = new StringRequest(Request.Method.DELETE, url,
                response -> {
                    Toast.makeText(this, "Tutorial deleted successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    Log.e("DETAIL", "Delete failed", error);
                    Toast.makeText(this, "Failed to delete tutorial", Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void editTutorial() {
        String url = BASE_URL + tutorialId;

        StringRequest request = new StringRequest(Request.Method.PUT, url,
                response -> {
                    Toast.makeText(this, "Tutorial updated!", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Log.e("DETAIL", "Edit failed", error);
                    Toast.makeText(this, "Failed to update tutorial", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("title", titleInput.getText().toString());
                params.put("description", descInput.getText().toString());
                params.put("category", categoryInput.getText().toString());
                return params;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}