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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

public class TutorialDetailActivity extends AppCompatActivity {

    private static final String TAG = "TutorialDetailActivity";
    private static final String BASE_URL =
            "http://coms-3090-028.class.las.iastate.edu:8080";

    private TextView titleText, descText, categoryText, usernameText;
    private WebView webView;
    private ImageButton btnBack, btnLikeTutorial;
    private Button btnEdit, btnDelete, btnSave;
    private ProgressDialog progressDialog;

    private TextView textTutorialLikes;

    private long tutorialId = -1L;
    private String title, description, category, fileUrl, uploaderUsername;

    private boolean isLiked = false;
    private long currentLikeCount = 0;

    private String loggedInUser;

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
        btnSave = findViewById(R.id.btnSaveTutorial);

        btnLikeTutorial = findViewById(R.id.btnLikeTutorial);
        textTutorialLikes = findViewById(R.id.textTutorialLikes);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        loggedInUser = SessionManager.getInstance().getLoggedInUsername();

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
        setupWebView();

        // Save to Board
        btnSave.setOnClickListener(v -> {
            SelectBoardDialog dialog = new SelectBoardDialog(TutorialDetailActivity.this,
                    board -> addTutorialToBoard(board.getId(), tutorialId));
            dialog.show();
        });

        btnDelete.setOnClickListener(v -> showDeleteDialog());

        // Like button
        btnLikeTutorial.setOnClickListener(v -> toggleLike());

        // Fetch current likes from backend
        fetchTutorialLikes();
    }

    private void addTutorialToBoard(long boardId, long tutorialId) {
        String url = BASE_URL + "/board/" + boardId + "/tutorial/" + tutorialId;

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                null,
                response -> Toast.makeText(this, "Tutorial saved", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show()
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void enforceEditDeletePermissions() {
        String loggedInUser = SessionManager.getInstance().getLoggedInUsername();

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

    private void setupWebView() {
        if (fileUrl == null || fileUrl.isEmpty()) {
            webView.setVisibility(android.view.View.GONE);
            return;
        }

        webView.setVisibility(android.view.View.VISIBLE);
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setMediaPlaybackRequiresUserGesture(false);

        String url = fileUrl;

        if (url.contains("youtube.com") || url.contains("youtu.be")) {
            String html = "<html><body><iframe width='100%' height='250' "
                    + "src='" + url + "' frameborder='0' allowfullscreen></iframe></body></html>";
            webView.loadData(html, "text/html", "utf-8");
            return;
        }

        if (url.startsWith("/tutorial/")) {
            String full = BASE_URL + url;

            String html = "<html><body>"
                    + "<video width='100%' height='250' controls>"
                    + "<source src='" + full + "' type='video/mp4'>"
                    + "</video></body></html>";

            webView.loadData(html, "text/html", "utf-8");
            return;
        }

        webView.loadUrl(url);
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

    // ------------------------------------------------------------
    // LIKE SYSTEM
    // ------------------------------------------------------------

    private void fetchTutorialLikes() {
        if (tutorialId <= 0) return;

        String url = BASE_URL + "/tutorial/" + tutorialId + "/totalLikes";

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                resp -> {
                    currentLikeCount = resp.optLong("totalLikes", 0);
                    textTutorialLikes.setText(currentLikeCount + " likes");
                },
                err -> Log.e(TAG, "Failed to fetch likes", err)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void toggleLike() {
        if (tutorialId <= 0 || loggedInUser == null) return;

        String url;

        if (isLiked) {
            // Dislike
            url = BASE_URL + "/tutorial/" + loggedInUser + "/dislike/" + tutorialId;
        } else {
            // Like
            url = BASE_URL + "/tutorial/" + loggedInUser + "/like/" + tutorialId;
        }

        JsonObjectRequest req = new JsonObjectRequest(
                isLiked ? Request.Method.DELETE : Request.Method.POST,
                url,
                null,
                resp -> {
                    isLiked = !isLiked;

                    if (isLiked) {
                        currentLikeCount++;
                        btnLikeTutorial.setImageResource(R.drawable.ic_heart_filled);
                    } else {
                        if (currentLikeCount > 0) currentLikeCount--;
                        btnLikeTutorial.setImageResource(R.drawable.ic_heart_outline);
                    }

                    textTutorialLikes.setText(currentLikeCount + " likes");
                },
                err -> Log.e(TAG, "Toggle tutorial like failed", err)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }
}