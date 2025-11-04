package com.example.androidexample;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONException;
import org.json.JSONObject;

public class CreateReviewActivity extends AppCompatActivity {

    private ImageView[] stars = new ImageView[5];
    private int selectedRating = 0;
    private EditText reviewInput;
    private Button submitReviewButton, backToPatternButton;

    private String username;
    private String patternName;

    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080"; // local backend

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_review);

        // ✅ Get logged-in username from SessionManager
        SessionManager session = SessionManager.getInstance();
        username = session.getLoggedInUsername();
        if (username == null || username.isEmpty()) {
            username = "testUser"; // fallback
        }

        stars[0] = findViewById(R.id.star1);
        stars[1] = findViewById(R.id.star2);
        stars[2] = findViewById(R.id.star3);
        stars[3] = findViewById(R.id.star4);
        stars[4] = findViewById(R.id.star5);

        reviewInput = findViewById(R.id.reviewInput);
        submitReviewButton = findViewById(R.id.submitReviewButton);
        backToPatternButton = findViewById(R.id.backToPatternButton);

        // ✅ Get pattern name from previous screen
        patternName = getIntent().getStringExtra("patternName");

        // Interactive stars
        for (int i = 0; i < stars.length; i++) {
            int index = i;
            stars[i].setOnClickListener(v -> {
                selectedRating = index + 1;
                highlightStars(selectedRating);
                submitReviewButton.setEnabled(true);
            });
        }

        backToPatternButton.setOnClickListener(v -> finish());

        submitReviewButton.setOnClickListener(v -> {
            if (selectedRating == 0) {
                Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
            } else {
                sendReviewToBackend(selectedRating, reviewInput.getText().toString());
            }
        });
    }

    private void highlightStars(int count) {
        for (int i = 0; i < stars.length; i++) {
            stars[i].setImageResource(i < count ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
        }
    }

    private void sendReviewToBackend(int rating, String reviewText) {
        // Backend endpoint uses comments
        String url = BASE_URL + "/patterns/" + username + "/" + patternName + "/comment";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("text", reviewText);
            jsonBody.put("rating", rating);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, url, jsonBody,
                response -> {
                    Toast.makeText(this, "Review submitted!", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

}
