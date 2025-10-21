package com.example.androidexample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PatternDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern_detail);

        ImageView imageView = findViewById(R.id.detailImage);
        TextView nameView = findViewById(R.id.detailName);
        TextView typeView = findViewById(R.id.detailType);
        TextView difficultyView = findViewById(R.id.detailDifficulty);
        TextView descriptionView = findViewById(R.id.detailDescription);
        TextView suppliesView = findViewById(R.id.detailSupplies);
        RatingBar ratingBar = findViewById(R.id.detailRating);

        String name = getIntent().getStringExtra("patternName");
        String type = getIntent().getStringExtra("patternType");
        String difficulty = getIntent().getStringExtra("difficulty");
        float rating = getIntent().getFloatExtra("rating", 0);
        String imageUrl = getIntent().getStringExtra("image");
        String description = getIntent().getStringExtra("description");
        String supplies = getIntent().getStringExtra("supplies");

        nameView.setText(name);
        typeView.setText("Type: " + type);
        difficultyView.setText("Difficulty: " + difficulty);
        descriptionView.setText(description);
        suppliesView.setText("Supplies: " + supplies);
        ratingBar.setRating(rating);

        // Load image manually
        new Thread(() -> {
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);

                new Handler(Looper.getMainLooper()).post(() -> imageView.setImageBitmap(bitmap));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
