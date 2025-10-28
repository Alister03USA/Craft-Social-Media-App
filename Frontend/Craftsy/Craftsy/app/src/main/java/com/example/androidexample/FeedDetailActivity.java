package com.example.androidexample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * FeedDetailActivity — identical to Feed page detail.
 * Displays a post, allows adding comments and liking comments.
 * Works from both Feed and ProjectSearch.
 */
public class FeedDetailActivity extends AppCompatActivity {

    private static final String TAG = "FeedDetailActivity";

    private TextView textProjectName, textUsername, textProjectDesc, textProjectType, textSupplies, textVisibility, textDate;
    private ImageView imageProject;
    private LinearLayout commentsContainer;
    private EditText commentInput;
    private Button postCommentButton;

    private String username, projectName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_detail);

        // Bind UI
        imageProject = findViewById(R.id.imageProject);
        textProjectName = findViewById(R.id.textProjectName);
        textUsername = findViewById(R.id.textUsername);
        textProjectDesc = findViewById(R.id.textProjectDesc);
        textProjectType = findViewById(R.id.textProjectType);
        textSupplies = findViewById(R.id.textSupplies);
        textVisibility = findViewById(R.id.textVisibility);
        textDate = findViewById(R.id.textDate);
        commentsContainer = findViewById(R.id.commentsContainer);
        commentInput = findViewById(R.id.commentInput);
        postCommentButton = findViewById(R.id.postCommentButton);

        username = getIntent().getStringExtra("username");
        projectName = getIntent().getStringExtra("projectName");

        if (username == null || projectName == null) {
            Toast.makeText(this, "Missing project data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchUserFeed(username);

        // Add comment listener
        postCommentButton.setOnClickListener(v -> {
            String commentText = commentInput.getText().toString().trim();
            if (!commentText.isEmpty()) {
                postComment(commentText);
            } else {
                Toast.makeText(this, "Enter a comment", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Fetch the user’s feed and find this project */
    private void fetchUserFeed(String username) {
        String url = "http://coms-3090-028.class.las.iastate.edu:8080/feed/" + username;
        Log.d(TAG, "Requesting: " + url);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> handleFeedResponse(response),
                error -> {
                    Log.e(TAG, "❌ Error fetching feed", error);
                    Toast.makeText(this, "Failed to load feed", Toast.LENGTH_SHORT).show();
                });
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void handleFeedResponse(JSONArray response) {
        try {
            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);
                String name = obj.optString("projectName", "");
                if (name.equalsIgnoreCase(projectName)) {
                    updateUI(obj);
                    fetchComments();
                    return;
                }
            }
            Toast.makeText(this, "Project not found", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "❌ Parse error", e);
        }
    }

    /** Populate project info */
    private void updateUI(JSONObject obj) {
        String projectDesc = obj.optString("projectDesc", "");
        String projectType = obj.optString("projectType", "");
        String supplies = obj.optString("supplies", "");
        String visibility = obj.optString("visibility", "");
        String date = obj.optString("date", "");
        String imageUrl = obj.optString("projectPic", null);

        textProjectName.setText(projectName);
        textUsername.setText("@" + username);
        textProjectDesc.setText(projectDesc);
        textProjectType.setText("Type: " + projectType);
        textSupplies.setText("Supplies: " + supplies);
        textVisibility.setText("Visibility: " + visibility);
        textDate.setText("Date: " + date);

        // Load image manually
        if (imageUrl != null && !imageUrl.isEmpty() && !"null".equals(imageUrl)) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                imageProject.setImageBitmap(bitmap);
            } catch (Exception e) {
                Log.e(TAG, "Image load failed: " + e.getMessage());
                imageProject.setImageResource(R.drawable.ic_post_placeholder);
            }
        } else {
            imageProject.setImageResource(R.drawable.ic_post_placeholder);
        }
    }

    /** Fetch project comments */
    private void fetchComments() {
        String url = "http://coms-3090-028.class.las.iastate.edu:8080/feed/" + username;
        Log.d(TAG, "Fetching comments for " + projectName);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    commentsContainer.removeAllViews();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject project = response.getJSONObject(i);
                            if (projectName.equalsIgnoreCase(project.optString("projectName"))) {
                                JSONArray comments = project.optJSONArray("feedComments");
                                if (comments != null) {
                                    for (int j = 0; j < comments.length(); j++) {
                                        JSONObject comment = comments.getJSONObject(j);
                                        addCommentToView(comment);
                                    }
                                }
                                break;
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "❌ Error parsing comment", e);
                        }
                    }
                },
                error -> Log.e(TAG, "❌ Failed to fetch comments", error));
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    /** Dynamically add comment view */
    private void addCommentToView(JSONObject comment) {
        try {
            String text = comment.optString("text", "");
            int likes = comment.optInt("likes", 0);
            long id = comment.optLong("id", -1);

            View commentView = getLayoutInflater().inflate(R.layout.comment_item, commentsContainer, false);

            TextView commentText = commentView.findViewById(R.id.commentText);
            TextView likeCount = commentView.findViewById(R.id.likeCount);
            Button likeButton = commentView.findViewById(R.id.likeButton);

            commentText.setText(text);
            likeCount.setText(likes + " likes");

            likeButton.setOnClickListener(v -> likeComment(id));

            commentsContainer.addView(commentView);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error adding comment view", e);
        }
    }

    /** Like a comment */
    private void likeComment(long commentId) {
        String url = "http://coms-3090-028.class.las.iastate.edu:8080/feed/" + username + "/" + projectName + "/" + commentId;
        Log.d(TAG, "Liking comment: " + url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, null,
                response -> fetchComments(),
                error -> Log.e(TAG, "❌ Failed to like comment", error));

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    /** Post a new comment */
    private void postComment(String text) {
        try {
            JSONObject body = new JSONObject();
            body.put("text", text);

            String url = "http://coms-3090-028.class.las.iastate.edu:8080/feed/" + username + "/" + projectName + "/comment";
            Log.d(TAG, "Posting comment to " + url);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
                    response -> {
                        Toast.makeText(this, "Comment added!", Toast.LENGTH_SHORT).show();
                        commentInput.setText("");
                        fetchComments();
                    },
                    error -> {
                        Log.e(TAG, "❌ Failed to post comment", error);
                        Toast.makeText(this, "Failed to add comment", Toast.LENGTH_SHORT).show();
                    });
            VolleySingleton.getInstance(this).addToRequestQueue(request);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error creating comment JSON", e);
        }
    }
}