package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PatternDetailActivity extends AppCompatActivity {

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

        backButton = findViewById(R.id.backButton);
        detailImage = findViewById(R.id.detailImage);
        detailName = findViewById(R.id.detailName);
        detailTypeDifficulty = findViewById(R.id.detailTypeDifficulty);
        detailDescription = findViewById(R.id.detailDescription);
        detailRatingBar = findViewById(R.id.detailRatingBar);
        averageRatingText = findViewById(R.id.averageRatingText);
        reviewsList = findViewById(R.id.reviewsList);
        createReviewButton = findViewById(R.id.createReviewButton);

        // Retrieve data
        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        patternTitle = intent.getStringExtra("patternName");

        backButton.setOnClickListener(v -> finish());

        createReviewButton.setOnClickListener(v -> {
            Intent addReviewIntent = new Intent(this, CreateReviewActivity.class);
            addReviewIntent.putExtra("patternName", patternTitle);
            addReviewIntent.putExtra("username", username); // 🔹 UPDATED for backend
            startActivity(addReviewIntent);
        });

        fetchPatternDetails(username, patternTitle);
    }

    private void fetchPatternDetails(String username, String patternTitle) {
        // 🔹 UPDATED for backend
        String url = "http://coms-3090-028.class.las.iastate.edu:8080/patterns/" + username + "/" + patternTitle;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
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

                        // 🔹 Your backend returns comments under "comments"
                        JSONArray reviewsArray = response.optJSONArray("comments");
                        List<String> reviews = new ArrayList<>();

                        if (reviewsArray != null) {
                            for (int i = 0; i < reviewsArray.length(); i++) {
                                JSONObject reviewObj = reviewsArray.getJSONObject(i);
                                String text = reviewObj.optString("text", "");
                                String date = reviewObj.optString("date", "");
                                int likes = reviewObj.optInt("likes", 0);
                                String formattedReview = text + "\n" + date + " • ❤️ " + likes;
                                reviews.add(formattedReview);
                            }
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                this,
                                android.R.layout.simple_list_item_1,
                                reviews
                        );
                        reviewsList.setAdapter(adapter);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing pattern data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(
                        this,
                        "Error fetching pattern: " + error.getMessage(),
                        Toast.LENGTH_LONG
                ).show()
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}
