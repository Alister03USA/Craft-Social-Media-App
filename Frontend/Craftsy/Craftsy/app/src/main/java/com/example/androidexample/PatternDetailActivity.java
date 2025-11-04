package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PatternDetailActivity extends AppCompatActivity {

    private static final String IMAGE_BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080/uploads/";

    private ImageView detailImage;
    private TextView detailName, detailTypeDifficulty, detailDescription, averageRatingText;
    private RatingBar detailRatingBar;
    private ListView reviewsList;
    private Button backButton, createReviewButton;

    private String username;
    private String patternTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern_detail);

        // Initialize views
        backButton = findViewById(R.id.backButton);
        detailImage = findViewById(R.id.detailImage);
        detailName = findViewById(R.id.detailName);
        detailTypeDifficulty = findViewById(R.id.detailTypeDifficulty);
        detailDescription = findViewById(R.id.detailDescription);
        detailRatingBar = findViewById(R.id.detailRatingBar);
        averageRatingText = findViewById(R.id.averageRatingText);
        reviewsList = findViewById(R.id.reviewsList);
        createReviewButton = findViewById(R.id.createReviewButton);

        // Get intent data
        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        patternTitle = intent.getStringExtra("patternName");

        // Back button
        backButton.setOnClickListener(v -> finish());

        // Create review button
        createReviewButton.setOnClickListener(v -> {
            Intent addReviewIntent = new Intent(this, CreateReviewActivity.class);
            addReviewIntent.putExtra("patternName", patternTitle);
            addReviewIntent.putExtra("username", username);
            startActivity(addReviewIntent);
        });

        // Fetch pattern details
        fetchPatternDetails(username, patternTitle);
    }

    private void fetchPatternDetails(String username, String patternTitle) {
        String url = "http://coms-3090-028.class.las.iastate.edu:8080/patterns/" + username + "/" + patternTitle;

        // Adapter holder for lambda
        final ReviewsAdapter[] adapterHolder = new ReviewsAdapter[1];

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        // Pattern details
                        String name = response.getString("patternName");
                        String type = response.optString("patternType", "N/A");
                        String difficulty = response.optString("difficulty", "N/A");
                        String description = response.optString("description", "");
                        float rating = (float) response.optDouble("rating", 0.0);

                        detailName.setText(name);
                        detailTypeDifficulty.setText(type + " • " + difficulty);
                        detailDescription.setText(description);
                        detailRatingBar.setRating(rating);
                        averageRatingText.setText(String.format("%.1f avg", rating));

                        // Load first image
                        String imageUrl = getFirstImagePath(response.optJSONArray("images"));
                        if (!imageUrl.isEmpty()) {
                            Glide.with(this).load(imageUrl).into(detailImage);
                        }

                        // Parse reviews
                        JSONArray commentsArray = response.optJSONArray("comments");
                        List<Review> reviews = new ArrayList<>();
                        if (commentsArray != null) {
                            for (int i = 0; i < commentsArray.length(); i++) {
                                JSONObject comment = commentsArray.getJSONObject(i);
                                Long id = comment.optLong("id");
                                String text = comment.optString("text", "");
                                String date = comment.optString("date", "");
                                int likes = comment.optInt("likes", 0);
                                Integer commentRating = comment.has("rating") ? comment.optInt("rating") : null;

                                reviews.add(new Review(id, text, date, likes, commentRating));
                            }
                        }

                        // Initialize adapter
                        adapterHolder[0] = new ReviewsAdapter(this, reviews, review -> {
                            String likeUrl = "http://coms-3090-028.class.las.iastate.edu:8080/patterns/"
                                    + username + "/" + patternTitle + "/" + review.id + "/like";

                            JsonObjectRequest likeRequest = new JsonObjectRequest(
                                    Request.Method.PUT, likeUrl, null,
                                    resp -> {
                                        review.likes++;
                                        adapterHolder[0].notifyDataSetChanged();
                                    },
                                    err -> Toast.makeText(this, "Failed to like review", Toast.LENGTH_SHORT).show()
                            );

                            VolleySingleton.getInstance(this).addToRequestQueue(likeRequest);
                        });

                        reviewsList.setAdapter(adapterHolder[0]);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing pattern data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error fetching pattern: " + error.getMessage(), Toast.LENGTH_LONG).show()
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    // Returns the first image URL from backend images array
    private String getFirstImagePath(JSONArray images) {
        if (images != null && images.length() > 0) {
            JSONObject img = images.optJSONObject(0);
            if (img != null) {
                String path = img.optString("filePath", "");
                if (!path.isEmpty()) {
                    return IMAGE_BASE_URL + path.substring(path.lastIndexOf("/") + 1);
                }
            }
        }
        return "";
    }
}
