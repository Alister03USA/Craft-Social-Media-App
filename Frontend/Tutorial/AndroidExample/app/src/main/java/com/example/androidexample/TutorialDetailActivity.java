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
 * Displays a single tutorial with Edit & Delete support.
 * Author: Ji Xian Fu (frontend)
 */
public class TutorialDetailActivity extends AppCompatActivity {

    private static final String TAG = "TutorialDetailActivity";
    private static final String BASE_URL =
            "http://coms-3090-028.class.las.iastate.edu:8080/tutorial";

    private TextView titleText, descText, categoryText, usernameText;
    private WebView webView;
    private ImageButton btnBack;
    private Button btnEdit, btnDelete;
    private ProgressDialog progressDialog;

    private long tutorialId = -1L;      // must be long
    private String title, description, category, fileUrl, username;
    private final String currentUser = "Fuji";

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

        // Always read id as long, matching backend BIGINT
        tutorialId = getIntent().getLongExtra("id", -1L);
        title = getIntent().getStringExtra("title");
        description = getIntent().getStringExtra("description");
        category = getIntent().getStringExtra("category");
        fileUrl = getIntent().getStringExtra("fileUrl");
        username = getIntent().getStringExtra("username");

        Log.d(TAG, "Loaded tutorialId=" + tutorialId + " | title=" + title);

        titleText.setText(title);
        descText.setText(description);
        categoryText.setText(category);
        usernameText.setText(username != null ? "By: " + username : "By: Unknown");

        btnBack.setOnClickListener(v -> finish());

        // Show buttons only if current user == author
//        if (username != null && username.equalsIgnoreCase(currentUser)) {
//            btnEdit.setVisibility(View.VISIBLE);
//            btnDelete.setVisibility(View.VISIBLE);
//        } else {
//            btnEdit.setVisibility(View.GONE);
//            btnDelete.setVisibility(View.GONE);
//        }

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditTutorialActivity.class);
            intent.putExtra("tutorialId", tutorialId);   // ✅ consistent key
            intent.putExtra("title", title);
            intent.putExtra("description", description);
            intent.putExtra("category", category);
            startActivity(intent);
        });

        btnDelete.setOnClickListener(v -> showDeleteDialog());

        setupWebView();
    }

    /** Displays both YouTube and MP4 tutorials properly */
    /** Displays MP4s directly; opens YouTube in app/browser safely */
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

        // --- Handle YouTube links ---
        if (normalizedUrl.contains("youtube.com") || normalizedUrl.contains("youtu.be")) {

            // Extract the video ID (works for both shorts & normal)
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
                Log.e(TAG, "❌ Failed to extract YouTube video ID", e);
            }

            // Build thumbnail + "Watch on YouTube" fallback
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

        // --- Handle local uploaded MP4 files ---
        if (normalizedUrl.startsWith("/tutorial/")) {
            String fullPath = BASE_URL.replace("/tutorial", "") + normalizedUrl;
            Log.d(TAG, "Playing MP4: " + fullPath);

            String html = "<!DOCTYPE html><html><body style='margin:0;padding:0;background-color:black;'>"
                    + "<video width='100%' height='250' controls>"
                    + "<source src='" + fullPath + "' type='video/mp4'>"
                    + "Your browser does not support video playback."
                    + "</video></body></html>";

            webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
            return;
        }

        // --- Fallback for other URLs ---
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
            Log.e(TAG, "❌ Delete aborted: tutorialId=" + tutorialId);
            return;
        }

        progressDialog.setMessage("Deleting tutorial...");
        progressDialog.show();

        StringRequest deleteRequest = new StringRequest(
                Request.Method.DELETE,
                BASE_URL + "/" + tutorialId,
                response -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Tutorial deleted successfully!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "✅ Deleted tutorial " + tutorialId);
                    finish();
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Delete failed: " + error, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "❌ Delete error", error);
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(deleteRequest);
    }
}