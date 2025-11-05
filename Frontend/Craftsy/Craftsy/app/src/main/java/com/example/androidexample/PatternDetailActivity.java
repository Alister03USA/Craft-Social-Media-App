package com.example.androidexample;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PatternDetailActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";
    private static final String IMAGE_BASE_URL = BASE_URL + "/uploads/";

    private ImageView detailImage;
    private TextView detailName, detailTypeDifficulty, detailDescription, averageRatingText;
    private RatingBar detailRatingBar;
    private RecyclerView reviewsList;
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
        reviewsList.setLayoutManager(new LinearLayoutManager(this));
        reviewsList.setNestedScrollingEnabled(false);
        createReviewButton = findViewById(R.id.createReviewButton);

        // Get pattern name from Intent and username from Session
        Intent intent = getIntent();
        patternTitle = intent.getStringExtra("patternName");
        SessionManager session = SessionManager.getInstance();
        username = session.getLoggedInUsername();



        // Buttons
        backButton.setOnClickListener(v -> finish());
        createReviewButton.setOnClickListener(v -> {
            Intent addReviewIntent = new Intent(this, CreateReviewActivity.class);
            addReviewIntent.putExtra("patternName", patternTitle);
            startActivity(addReviewIntent);
        });

        fetchPatternDetails();
    }


    private void fetchPatternDetails() {

        // ✅ FIXED: Correct endpoint order
        String url = BASE_URL + "/patterns/" + username + "/" + patternTitle;

        final ReviewsAdapter[] adapterWrapper = new ReviewsAdapter[1];

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        detailName.setText(response.optString("patternName", ""));
                        String type = response.optString("patternType", "N/A");
                        String difficulty = response.optString("difficulty", "N/A");
                        detailTypeDifficulty.setText(type + " • " + difficulty);

                        detailDescription.setText(response.optString("description", ""));
                        float rating = (float) response.optDouble("rating", 0);
                        detailRatingBar.setRating(rating);
                        averageRatingText.setText(String.format("%.1f avg", rating));

                        String imgUrl = getFirstImagePath(response.optJSONArray("images"));
                        if (!imgUrl.isEmpty()) Glide.with(this).load(imgUrl).into(detailImage);
                        else detailImage.setImageResource(R.drawable.craftsy_image_placeholder);

                        // Reviews
                        JSONArray commentsArray = response.optJSONArray("comments");
                        List<Review> reviews = new ArrayList<>();
                        if (commentsArray != null) {
                            for (int i = 0; i < commentsArray.length(); i++) {
                                JSONObject obj = commentsArray.getJSONObject(i);
                                reviews.add(new Review(
                                        obj.optLong("id"),
                                        obj.optString("text", ""),
                                        obj.optString("date", ""),
                                        obj.optInt("likes", 0),
                                        obj.has("rating") ? obj.optInt("rating") : null
                                ));
                            }
                        }

                        adapterWrapper[0] = new ReviewsAdapter(
                                this,
                                reviews,
                                review -> {
                                    // ✅ FIXED: Correct like endpoint order
                                    String likeUrl = BASE_URL + "/patterns/" +
                                            username + "/" + patternTitle + "/" + review.id + "/like";

                                    JsonObjectRequest likeReq = new JsonObjectRequest(
                                            Request.Method.PUT, likeUrl, null,
                                            r -> {
                                                review.likes++;
                                                adapterWrapper[0].notifyDataSetChanged();
                                            },
                                            err -> Toast.makeText(this, "Failed to like review", Toast.LENGTH_SHORT).show()
                                    );

                                    VolleySingleton.getInstance(this).addToRequestQueue(likeReq);
                                }
                        );

                        reviewsList.setAdapter(adapterWrapper[0]);

                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing data", Toast.LENGTH_LONG).show();
                    }
                },
                error -> Toast.makeText(this, "Could not load pattern", Toast.LENGTH_LONG).show()
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }


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
