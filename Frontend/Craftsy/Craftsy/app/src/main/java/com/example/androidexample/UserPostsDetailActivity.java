package com.example.androidexample;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

public class UserPostsDetailActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";

    private ImageView postImage;
    private TextView projectName, likesCount, commentsCount, postDescription;
    private long postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_posts_details);

        postImage = findViewById(R.id.postImage);
        projectName = findViewById(R.id.projectName);
        likesCount = findViewById(R.id.likesCount);
        commentsCount = findViewById(R.id.commentsCount);
        postDescription = findViewById(R.id.postDescription);

        postId = getIntent().getLongExtra("postId", -1);
        if (postId != -1) {
            fetchPostDetails(postId);
        } else {
            Toast.makeText(this, "No post selected", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchPostDetails(long postId) {
        String url = BASE_URL + "/posts/" + postId; // assuming endpoint for single post

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    projectName.setText(response.optString("projectName", "Untitled"));
                    likesCount.setText(response.optInt("likesCount", 0) + " Likes");

                    JSONArray comments = response.optJSONArray("comments");
                    commentsCount.setText((comments != null ? comments.length() : 0) + " Comments");

                    postDescription.setText(response.optString("description", ""));

                    // Load first image if exists
                    JSONArray images = response.optJSONArray("images");
                    if (images != null && images.length() > 0) {
                        JSONObject img = images.optJSONObject(0);
                        if (img != null) {
                            long imageId = img.optLong("id", -1);
                            if (imageId > 0) {
                                loadImageIntoView(postImage, imageId);
                            }
                        }
                    }
                },
                error -> Toast.makeText(this, "Failed to load post", Toast.LENGTH_SHORT).show()
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void loadImageIntoView(ImageView imageView, long imageId) {
        String url = BASE_URL + "/images/" + imageId;

        JsonObjectRequest metadataRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    String filePath = response.optString("filePath", "");
                    if (filePath != null && !filePath.isEmpty()) {
                        String filename = filePath.substring(filePath.lastIndexOf("/") + 1);
                        String fullUrl = BASE_URL + "/uploads/" + filename;

                        Glide.with(this)
                                .load(fullUrl)
                                .placeholder(R.drawable.ic_post_placeholder)
                                .error(R.drawable.ic_post_placeholder)
                                .centerCrop()
                                .into(imageView);
                    } else {
                        imageView.setImageResource(R.drawable.ic_post_placeholder);
                    }
                },
                error -> imageView.setImageResource(R.drawable.ic_post_placeholder)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(metadataRequest);
    }
}
