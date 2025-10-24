package com.example.androidexample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class TutorialDetailActivity extends AppCompatActivity {

    private static final String TAG = "TutorialDetailActivity";
    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";

    private TextView titleTv, metaTv, descTv;
    private VideoView videoView;
    private WebView webView;
    private ProgressBar progress;
    private ImageView imageView;
    private MaterialButton btnBack, btnEdit, btnDelete;

    private String title, description, category, fileUrl, filePath, username;
    private long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial_detail);

        titleTv = findViewById(R.id.detailTitle);
        metaTv = findViewById(R.id.detailMeta);
        descTv = findViewById(R.id.detailDesc);
        videoView = findViewById(R.id.detailVideo);
        webView = findViewById(R.id.detailWeb);
        imageView = findViewById(R.id.detailImage);
        progress = findViewById(R.id.detailProgress);
        btnBack = findViewById(R.id.btnBack);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);

        Intent intent = getIntent();
        id = intent.getLongExtra("id", -1);
        title = intent.getStringExtra("title");
        description = intent.getStringExtra("description");
        category = intent.getStringExtra("category");
        fileUrl = intent.getStringExtra("fileUrl");
        filePath = intent.getStringExtra("filePath");
        username = intent.getStringExtra("username");

        Log.d(TAG, "onCreate: id=" + id + ", title=" + title + ", fileUrl=" + fileUrl + ", filePath=" + filePath);

        titleTv.setText(title);
        metaTv.setText("@" + (username == null ? "Unknown" : username) + " • " + category);
        descTv.setText(description == null || description.isEmpty() ? "No description available" : description);

        showMedia();
        btnBack.setOnClickListener(v -> finish());
    }

    private void showMedia() {
        Log.d(TAG, "showMedia() called, fileUrl=" + fileUrl);

        progress.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.GONE);
        webView.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);

        try {
            String source = null;
            if (fileUrl != null && !fileUrl.isEmpty()) {
                if (fileUrl.startsWith("/tutorial/")) {
                    source = BASE_URL + fileUrl;
                    Log.d(TAG, "Detected local backend path: " + source);
                } else {
                    source = fileUrl;
                    Log.d(TAG, "Detected external URL: " + source);
                }
            }

            if (source == null) {
                Log.e(TAG, "No valid media source found");
                progress.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageResource(R.drawable.ic_post_placeholder);
                return;
            }

            // ✅ Case 1: YouTube
            if (source.contains("youtube.com") || source.contains("youtu.be")) {
                Log.d(TAG, "Loading YouTube in WebView: " + source);
                webView.setVisibility(View.VISIBLE);
                webView.getSettings().setJavaScriptEnabled(true);
                webView.setWebViewClient(new WebViewClient());
                webView.loadUrl(source);
                progress.setVisibility(View.GONE);
            }
            // ✅ Case 2: backend/local .mp4 stream
            else if (source.startsWith(BASE_URL)) {
                Log.d(TAG, "Attempting to play backend video: " + source);
                videoView.setVisibility(View.VISIBLE);
                MediaController controller = new MediaController(this);
                controller.setAnchorView(videoView);
                videoView.setMediaController(controller);

                // 👇 Fix: use setVideoPath() instead of setVideoURI()
                videoView.setVideoPath(source);

                videoView.setOnPreparedListener(mp -> {
                    progress.setVisibility(View.GONE);
                    videoView.start();
                });

                videoView.setOnErrorListener((mp, what, extra) -> {
                    Log.e(TAG, "Video playback error: what=" + what + ", extra=" + extra);
                    progress.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setImageResource(R.drawable.ic_post_placeholder);
                    return true;
                });
            }
            // ✅ Case 3: fallback external .mp4 or invalid file
            else if (source.endsWith(".mp4")) {
                Log.d(TAG, "Playing external mp4: " + source);
                videoView.setVisibility(View.VISIBLE);
                MediaController controller = new MediaController(this);
                controller.setAnchorView(videoView);
                videoView.setMediaController(controller);
                videoView.setVideoPath(source);
                videoView.setOnPreparedListener(mp -> {
                    progress.setVisibility(View.GONE);
                    videoView.start();
                });
            }
            else {
                Log.d(TAG, "Unknown type, showing placeholder.");
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageResource(R.drawable.ic_post_placeholder);
                progress.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error displaying media", e);
            progress.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageResource(R.drawable.ic_post_placeholder);
        }
    }
}