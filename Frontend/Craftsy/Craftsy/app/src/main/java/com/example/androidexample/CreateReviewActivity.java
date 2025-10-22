package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
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

    private String username = "testUser"; // Replace with logged-in user
    private String patternName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_review);

        stars[0] = findViewById(R.id.star1);
        stars[1] = findViewById(R.id.star2);
        stars[2] = findViewById(R.id.star3);
        stars[3] = findViewById(R.id.star4);
        stars[4] = findViewById(R.id.star5);

        reviewInput = findViewById(R.id.reviewInput);
        submitReviewButton = findViewById(R.id.submitReviewButton);
        backToPatternButton = findViewById(R.id.backToPatternButton);

        patternName = getIntent().getStringExtra("patternName");

        // Interactive stars
        for (int i = 0; i < stars.length; i++) {
            int index = i;
            stars[i].setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER ||
                        event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
                    highlightStars(index + 1);
                }
                return false;
            });

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
        String url = "https://fdfe903c-6cbc-44e4-9457-0888ef0861b2.mock.pstmn.io/patterns/testUser/ChunkyBlanket/reviews";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", username);
            jsonBody.put("rating", rating);
            jsonBody.put("text", reviewText);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, url, jsonBody,
                response -> {
                    Toast.makeText(this, "Review submitted!", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}
