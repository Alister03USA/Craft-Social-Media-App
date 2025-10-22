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
            startActivity(addReviewIntent);
        });

        fetchPatternDetails(username, patternTitle);
    }

    private void fetchPatternDetails(String username, String patternTitle) {
        String url = "https://fdfe903c-6cbc-44e4-9457-0888ef0861b2.mock.pstmn.io/patterns/quinn/sweater";
                //"https://yourbackendurl.com/patterns/" + username + "/" + patternTitle;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        String name = response.getString("patternName");
                        String type = response.getString("patternType");
                        String difficulty = response.getString("difficulty");
                        String description = response.getString("description");
                        float rating = (float) response.getDouble("rating");

                        detailName.setText(name);
                        detailTypeDifficulty.setText(type + " • " + difficulty);
                        detailDescription.setText(description);
                        detailRatingBar.setRating(rating);
                        averageRatingText.setText(String.format("%.1f avg", rating));

                        JSONArray reviewsArray = response.getJSONArray("comments");
                        List<String> reviews = new ArrayList<>();

                        for (int i = 0; i < reviewsArray.length(); i++) {
                            JSONObject reviewObj = reviewsArray.getJSONObject(i);
                            String user = reviewObj.getString("username");
                            String text = reviewObj.getString("text");
                            String date = reviewObj.getString("date");
                            String formattedReview = user + ": " + text + "\n" + date;
                            reviews.add(formattedReview);
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
