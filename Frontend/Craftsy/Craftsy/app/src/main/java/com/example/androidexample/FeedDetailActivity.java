package com.example.androidexample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.androidexample.SelectBoardDialog;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * FeedDetailActivity — identical to feed detail, with comments + like toggle.
 */
public class FeedDetailActivity extends AppCompatActivity {

    private static final String TAG = "FeedDetailActivity";
    private static final String BASE = "http://coms-3090-028.class.las.iastate.edu:8080";

    private ImageView imageProject;
    private TextView textProjectName, textUsername, textProjectDesc, textProjectType, textSupplies, textVisibility, textDate;
    private LinearLayout commentsContainer;
    private EditText commentInput;
    private Button postCommentButton;

    private String username;
    private String projectName;
    private long imageId = -1L;

    private HashMap<Long, Boolean> likedComments = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_detail);

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

        postCommentButton.setOnClickListener(v -> {
            String text = commentInput.getText().toString().trim();
            if (!text.isEmpty()) postComment(text);
            else Toast.makeText(this, "Enter a comment", Toast.LENGTH_SHORT).show();
        });
        Button btnSave = findViewById(R.id.btnSaveFeedProject);
        btnSave.setOnClickListener(v -> {
            SelectBoardDialog dialog = new SelectBoardDialog(FeedDetailActivity.this, board -> {
                addProjectToBoard(board.getId(), projectName);
            });
            dialog.show();
        });
        ImageButton btnBack = findViewById(R.id.btnBackFeedDetail);
        btnBack.setOnClickListener(v -> finish());
    }
    private void addProjectToBoard(long boardId, String projectName) {
        String url = BASE + "/board/" + boardId + "/project/" + username + "/" + projectName;

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                null,
                response -> Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(this, "Error saving project", Toast.LENGTH_SHORT).show()
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }
    private void fetchUserFeed(String user) {
        String url = BASE + "/feed/" + user;
        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET, url, null,
                resp -> {
                    for (int i = 0; i < resp.length(); i++) {
                        JSONObject obj = resp.optJSONObject(i);
                        if (obj != null && projectName.equalsIgnoreCase(obj.optString("projectName", ""))) {
                            bindPost(obj);
                            bindComments(obj);
                            return;
                        }
                    }
                },
                err -> Log.e(TAG, "❌ Feed fetch failed", err)
        );
        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void bindPost(JSONObject obj) {
        textProjectName.setText(projectName);
        textUsername.setText("@" + username);
        textProjectDesc.setText(obj.optString("projectDesc", ""));
        textProjectType.setText("Type: " + obj.optString("projectType", ""));
        textSupplies.setText("Supplies: " + obj.optString("supplies", ""));
        textVisibility.setText("Visibility: " + obj.optString("visibility", ""));
        textDate.setText("Date: " + obj.optString("date", ""));

        JSONArray images = obj.optJSONArray("images");
        if (images != null && images.length() > 0) {
            JSONObject first = images.optJSONObject(0);
            imageId = first != null ? first.optLong("id", -1) : -1;
        }
        if (imageId > 0) fetchImageMetaAndLoad(imageId);
        else imageProject.setImageResource(R.drawable.ic_post_placeholder);
    }

    private void fetchImageMetaAndLoad(long id) {
        String metaUrl = BASE + "/images/" + id;
        JsonObjectRequest metaReq = new JsonObjectRequest(
                Request.Method.GET, metaUrl, null,
                meta -> {
                    String filePath = meta.optString("filePath", "");
                    String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
                    ArrayList<String> tries = new ArrayList<>();
                    tries.add(BASE + "/uploads/" + fileName);
                    tries.add(BASE + "/images/" + id + "/download");
                    tries.add(BASE + "/images/" + id);
                    tryLoadBitmapSequentially(tries, 0);
                },
                err -> {
                    Log.e(TAG, "❌ Image meta fetch failed", err);
                    imageProject.setImageResource(R.drawable.ic_post_placeholder);
                });
        VolleySingleton.getInstance(this).addToRequestQueue(metaReq);
    }

    private void tryLoadBitmapSequentially(ArrayList<String> urls, int idx) {
        if (idx >= urls.size()) {
            imageProject.setImageResource(R.drawable.ic_post_placeholder);
            return;
        }
        String url = urls.get(idx);
        new Thread(() -> {
            Bitmap bmp = null;
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setDoInput(true);
                conn.connect();
                if (!"application/json".equals(conn.getContentType())) {
                    try (InputStream in = conn.getInputStream()) {
                        bmp = BitmapFactory.decodeStream(in);
                    }
                }
            } catch (Exception ignored) {}
            Bitmap finalBmp = bmp;
            runOnUiThread(() -> {
                if (finalBmp != null) imageProject.setImageBitmap(finalBmp);
                else tryLoadBitmapSequentially(urls, idx + 1);
            });
        }).start();
    }

    private void bindComments(JSONObject post) {
        commentsContainer.removeAllViews();
        likedComments.clear();
        JSONArray comments = post.optJSONArray("comments");
        if (comments == null || comments.length() == 0) {
            TextView none = new TextView(this);
            none.setText("No comments yet.");
            commentsContainer.addView(none);
            return;
        }
        for (int i = 0; i < comments.length(); i++) {
            JSONObject c = comments.optJSONObject(i);
            if (c != null) addCommentRow(c);
        }
    }

    private void addCommentRow(JSONObject c) {
        View row = getLayoutInflater().inflate(R.layout.comment_item, commentsContainer, false);
        TextView text = row.findViewById(R.id.commentText);
        TextView likes = row.findViewById(R.id.likeCount);
        Button likeBtn = row.findViewById(R.id.likeButton);

        long id = c.optLong("id", -1);
        int likeCount = c.optInt("likes", 0);
        boolean liked = c.optBoolean("liked", false);

        text.setText(c.optString("text", ""));
        likes.setText(likeCount + " likes");
        likedComments.put(id, liked);
        likeBtn.setText(liked ? "Unlike" : "Like");

        likeBtn.setOnClickListener(v -> toggleLike(id, likeBtn, likes));
        commentsContainer.addView(row);
    }

    /** Backend now handles toggle logic */
    private void toggleLike(long commentId, Button likeBtn, TextView likesView) {
        if (commentId <= 0) return;
        boolean currentlyLiked = likedComments.getOrDefault(commentId, false);
        String action = currentlyLiked ? "unlike" : "like";
        String url = BASE + "/feed/" + username + "/" + projectName + "/" + commentId;
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.PUT, url, null,
                r -> {
                    // Update local like state and UI without refetching entire feed
                    likedComments.put(commentId, !currentlyLiked);
                    int currentLikes = 0;
                    try {
                        String likesText = likesView.getText().toString();
                        currentLikes = Integer.parseInt(likesText.split(" ")[0]);
                    } catch (Exception ignored) {}
                    int newLikes = currentlyLiked ? currentLikes - 1 : currentLikes + 1;
                    if (newLikes < 0) newLikes = 0;
                    likesView.setText(newLikes + " likes");
                    likeBtn.setText(!currentlyLiked ? "Unlike" : "Like");
                },
                err -> Log.e(TAG, "❌ Like toggle failed", err)
        );
        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void postComment(String text) {
        try {
            JSONObject body = new JSONObject();
            body.put("text", text);
            String url = BASE + "/feed/" + username + "/" + projectName + "/comment";
            JsonObjectRequest req = new JsonObjectRequest(
                    Request.Method.POST, url, body,
                    r -> {
                        commentInput.setText("");
                        fetchUserFeed(username);
                    },
                    err -> Log.e(TAG, "❌ Comment post failed", err)
            );
            VolleySingleton.getInstance(this).addToRequestQueue(req);
        } catch (Exception e) {
            Log.e(TAG, "❌ JSON build error", e);
        }
    }
}