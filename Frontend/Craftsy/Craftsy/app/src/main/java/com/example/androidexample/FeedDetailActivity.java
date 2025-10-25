package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashSet;
import java.util.Set;

public class FeedDetailActivity extends AppCompatActivity {

    private static final String TAG = "FeedDetailActivity";

    private TextView textTitle, textUser, textDesc, textType, textSupplies, textMeta;
    private LinearLayout commentsContainer;
    private EditText editComment;
    private Button btnPostComment, btnBack;
    private String username, projectName, loggedInUser, fromScreen;
    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080/feed";
    private final Set<Long> likedComments = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_detail);

        Log.d(TAG, "onCreate: FeedDetailActivity started");

        textTitle = findViewById(R.id.textProjectName);
        textUser = findViewById(R.id.textUsername);
        textDesc = findViewById(R.id.textProjectDesc);
        textType = findViewById(R.id.textProjectType);
        textSupplies = findViewById(R.id.textSupplies);
        textMeta = findViewById(R.id.textMeta);
        commentsContainer = findViewById(R.id.commentsContainer);
        editComment = findViewById(R.id.editComment);
        btnPostComment = findViewById(R.id.btnPostComment);
        btnBack = findViewById(R.id.btnBack);

        username = getIntent().getStringExtra("username");
        projectName = getIntent().getStringExtra("projectName");
        loggedInUser = getIntent().getStringExtra("loggedInUser");
        fromScreen = getIntent().getStringExtra("from");

        Log.d(TAG, "Received Intent → from=" + fromScreen + ", username=" + username + ", projectName=" + projectName);

        if (username == null || projectName == null) {
            Toast.makeText(this, "Missing feed info!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        textUser.setText(username);
        textTitle.setText(projectName);
        textDesc.setText(getIntent().getStringExtra("projectDesc"));
        textType.setText(getIntent().getStringExtra("projectType"));
        textSupplies.setText(getIntent().getStringExtra("supplies"));
        textMeta.setText(getIntent().getStringExtra("visibility") + " • " + getIntent().getStringExtra("date"));

        textUser.setOnClickListener(v -> openUserProfile(username));

        btnBack.setOnClickListener(v -> handleBackNavigation());
        loadComments();
        btnPostComment.setOnClickListener(v -> addComment());
    }

    private void handleBackNavigation() {
        Log.d(TAG, "Back button clicked from=" + fromScreen);
        if ("search".equalsIgnoreCase(fromScreen)) {
            Intent intent = new Intent(this, SearchActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(this, FeedActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        handleBackNavigation();
    }

    private void openUserProfile(String tappedUsername) {
        Log.d(TAG, "User tapped on username: " + tappedUsername);
        Intent intent;
        if (tappedUsername.equalsIgnoreCase(loggedInUser)) {
            intent = new Intent(this, UserProfile.class);
        } else {
            intent = new Intent(this, OutsideUserProfile.class);
        }
        intent.putExtra("username", tappedUsername);
        startActivity(intent);
    }

    private void loadComments() {
        String url = BASE_URL + "/" + username;
        Log.d(TAG, "loadComments() → GET " + url);

        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    commentsContainer.removeAllViews();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject feedObj = response.getJSONObject(i);
                            if (feedObj.optString("projectName").equals(projectName)) {
                                JSONArray comments = feedObj.optJSONArray("comments");
                                if (comments == null || comments.length() == 0) {
                                    TextView tv = new TextView(this);
                                    tv.setText("No comments yet. Be the first!");
                                    commentsContainer.addView(tv);
                                } else {
                                    for (int j = 0; j < comments.length(); j++) {
                                        JSONObject c = comments.getJSONObject(j);
                                        long id = c.optLong("id");
                                        String text = c.optString("text", "(empty)");
                                        int likes = c.optInt("likes", 0);

                                        View commentView = getLayoutInflater().inflate(R.layout.item_comment, commentsContainer, false);
                                        TextView commentText = commentView.findViewById(R.id.commentText);
                                        TextView commentLikes = commentView.findViewById(R.id.commentLikes);
                                        Button likeButton = commentView.findViewById(R.id.likeButton);

                                        boolean isLiked = likedComments.contains(id);
                                        updateLikeButtonUI(likeButton, isLiked);

                                        commentText.setText("• " + text);
                                        commentLikes.setText("❤️ " + likes);

                                        likeButton.setOnClickListener(v -> {
                                            if (likedComments.contains(id)) {
                                                likedComments.remove(id);
                                                int newLikes = Math.max(0, likes - 1);
                                                commentLikes.setText("❤️ " + newLikes);
                                                updateLikeButtonUI(likeButton, false);
                                                Toast.makeText(this, "Unliked 💔", Toast.LENGTH_SHORT).show();
                                            } else {
                                                likeComment(id, commentLikes, likeButton);
                                                likedComments.add(id);
                                            }
                                        });

                                        commentsContainer.addView(commentView);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing comments", e);
                        }
                    }
                },
                error -> Log.e(TAG, "Failed to load comments", error)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void addComment() {
        String comment = editComment.getText().toString().trim();
        if (comment.isEmpty()) {
            Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject json = new JSONObject();
        try {
            json.put("text", comment);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = BASE_URL + "/" + username + "/" + projectName + "/comment";
        Log.d(TAG, "addComment() → POST " + url);

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, json,
                response -> {
                    Toast.makeText(this, "Comment added!", Toast.LENGTH_SHORT).show();
                    editComment.setText("");
                    loadComments();
                },
                error -> Log.e(TAG, "Failed to add comment", error)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void likeComment(long commentId, TextView commentLikes, Button likeButton) {
        String url = BASE_URL + "/" + username + "/" + projectName + "/" + commentId;
        Log.d(TAG, "likeComment() → PUT " + url);

        StringRequest req = new StringRequest(Request.Method.PUT, url,
                response -> {
                    Log.d(TAG, "Comment liked successfully!");
                    Toast.makeText(this, "Liked ❤️", Toast.LENGTH_SHORT).show();

                    String current = commentLikes.getText().toString().replace("❤️", "").trim();
                    int likes = Integer.parseInt(current);
                    commentLikes.setText("❤️ " + (likes + 1));
                    updateLikeButtonUI(likeButton, true);
                },
                error -> Log.e(TAG, "Failed to like comment", error)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void updateLikeButtonUI(Button button, boolean liked) {
        if (liked) {
            button.setText("Liked ❤️");
            button.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        } else {
            button.setText("Like ♡");
            button.setBackgroundColor(getResources().getColor(R.color.button_blue));
        }
    }
}