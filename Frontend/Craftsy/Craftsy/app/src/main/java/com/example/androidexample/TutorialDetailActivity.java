package com.example.androidexample;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

/**
 * Displays both YouTube and MP4 tutorials properly in a WebView.
 * Enforces: only the uploader can edit or delete the tutorial.
 */
public class TutorialDetailActivity extends AppCompatActivity {

    private static final String TAG = "TutorialDetailActivity";
    private static final String BASE_URL =
            "http://coms-3090-028.class.las.iastate.edu:8080";

    private TextView titleText, descText, categoryText, usernameText;
    private WebView webView;
    private ImageButton btnBack;
    private Button btnEdit, btnDelete;
    private ProgressDialog progressDialog;

    private long tutorialId = -1L;
    private String title, description, category, fileUrl, uploaderUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial_detail);

        titleText = findViewById(R.id.detailTitle);
        descText = findViewById(R.id.detailDescription);
        categoryText = findViewById(R.id.detailCategory);
        usernameText = findViewById(R.id.detailUsername);
        webView = findViewById(R.id.webView);
        btnBack = findViewById(R.id.btnBack);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        tutorialId = getIntent().getLongExtra("id", -1L);
        title = getIntent().getStringExtra("title");
        description = getIntent().getStringExtra("description");
        category = getIntent().getStringExtra("category");
        fileUrl = getIntent().getStringExtra("fileUrl");
        uploaderUsername = getIntent().getStringExtra("username");

        Log.d(TAG, "Loaded tutorialId=" + tutorialId + " | uploader=" + uploaderUsername);

        titleText.setText(title);
        descText.setText(description);
        categoryText.setText(category);
        usernameText.setText(uploaderUsername != null ? "By: " + uploaderUsername : "By: Unknown");

        btnBack.setOnClickListener(v -> finish());

        enforceEditDeletePermissions();

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditTutorialActivity.class);
            intent.putExtra("tutorialId", tutorialId);
            intent.putExtra("title", title);
            intent.putExtra("description", description);
            intent.putExtra("category", category);
            startActivity(intent);
        });

        btnDelete.setOnClickListener(v -> showDeleteDialog());

        setupWebView();
    }

    /**
     * Only show Edit/Delete buttons when the logged-in user is the uploader.
     */
    private void enforceEditDeletePermissions() {
        String loggedInUser = SessionManager.getInstance().getLoggedInUsername();

        Log.d(TAG, "loggedInUser=" + loggedInUser + " | uploader=" + uploaderUsername);

        if (loggedInUser == null || uploaderUsername == null) {
            btnEdit.setVisibility(android.view.View.GONE);
            btnDelete.setVisibility(android.view.View.GONE);
            return;
        }

        if (loggedInUser.equalsIgnoreCase(uploaderUsername)) {
            btnEdit.setVisibility(android.view.View.VISIBLE);
            btnDelete.setVisibility(android.view.View.VISIBLE);
        } else {
            btnEdit.setVisibility(android.view.View.GONE);
            btnDelete.setVisibility(android.view.View.GONE);
        }
    }

    /** Displays both YouTube and MP4 tutorials properly */
    private void setupWebView() {
        if (fileUrl == null || fileUrl.isEmpty()) {
            webView.setVisibility(android.view.View.GONE);
            return;
        }

        webView.setVisibility(android.view.View.VISIBLE);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        String normalizedUrl = fileUrl.trim();
        Log.d(TAG, "Loading tutorial URL: " + normalizedUrl);

        if (normalizedUrl.contains("youtube.com") || normalizedUrl.contains("youtu.be")) {
            String videoId = null;
            try {
                if (normalizedUrl.contains("watch?v=")) {
                    videoId = normalizedUrl.substring(normalizedUrl.indexOf("watch?v=") + 8);
                } else if (normalizedUrl.contains("shorts/")) {
                    videoId = normalizedUrl.substring(normalizedUrl.indexOf("shorts/") + 7);
                } else if (normalizedUrl.contains("youtu.be/")) {
                    videoId = normalizedUrl.substring(normalizedUrl.indexOf("youtu.be/") + 9);
                }
                if (videoId != null && videoId.contains("&")) {
                    videoId = videoId.substring(0, videoId.indexOf("&"));
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to extract YouTube video ID", e);
            }

            String thumbnail = videoId != null
                    ? "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg"
                    : "https://www.youtube.com";

            String html = "<!DOCTYPE html><html><body style='margin:0;padding:0;background:#000;'>"
                    + "<div style='position:relative;text-align:center;'>"
                    + "<img src='" + thumbnail + "' style='width:100%;max-height:250px;object-fit:cover;'/>"
                    + "<a href='" + normalizedUrl + "' "
                    + "style='position:absolute;top:0;left:0;width:100%;height:100%;display:flex;"
                    + "align-items:center;justify-content:center;color:white;"
                    + "background:rgba(0,0,0,0.3);text-decoration:none;font-size:18px;'>"
                    + "▶ Watch on YouTube</a>"
                    + "</div></body></html>";

            webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
            return;
        }

        if (normalizedUrl.startsWith("/tutorial/")) {
            String fullPath = BASE_URL + normalizedUrl;
            Log.d(TAG, "Playing MP4: " + fullPath);

            String html = "<!DOCTYPE html><html><body style='margin:0;padding:0;background-color:black;'>"
                    + "<video width='100%' height='250' controls>"
                    + "<source src='" + fullPath + "' type='video/mp4'>"
                    + "Your browser does not support video playback."
                    + "</video></body></html>";

            webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
            return;
        }

        if (normalizedUrl.startsWith("http")) {
            webView.loadUrl(normalizedUrl);
        }
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Tutorial")
                .setMessage("Are you sure you want to delete this tutorial?")
                .setPositiveButton("Delete", (dialog, which) -> deleteTutorial())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteTutorial() {
        if (tutorialId <= 0) {
            Toast.makeText(this, "Invalid tutorial ID", Toast.LENGTH_LONG).show();
            return;
        }

        progressDialog.setMessage("Deleting tutorial...");
        progressDialog.show();

        StringRequest deleteRequest = new StringRequest(
                Request.Method.DELETE,
                BASE_URL + "/tutorial/" + tutorialId,
                response -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Tutorial deleted successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Delete failed: " + error, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Delete error", error);
                }
        );
        VolleySingleton.getInstance(this).addToRequestQueue(deleteRequest);
    }
}