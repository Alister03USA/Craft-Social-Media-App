package com.example.androidexample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * FeedDetailActivity — displays details of a selected feed post
 * Now also shows the uploaded image at the top of the screen.
 */
public class FeedDetailActivity extends AppCompatActivity {

    private TextView textProjectName, textUsername, textProjectDesc, textProjectType, textSupplies, textVisibility, textDate;
    private ImageView imageProject;
    private static final String TAG = "FeedDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_detail);

        // 🔹 Bind UI elements
        imageProject = findViewById(R.id.imageProject);
        textProjectName = findViewById(R.id.textProjectName);
        textUsername = findViewById(R.id.textUsername);
        textProjectDesc = findViewById(R.id.textProjectDesc);
        textProjectType = findViewById(R.id.textProjectType);
        textSupplies = findViewById(R.id.textSupplies);
        textVisibility = findViewById(R.id.textVisibility);
        textDate = findViewById(R.id.textDate);

        // 🔹 Get extras from intent
        String username = getIntent().getStringExtra("username");
        String projectName = getIntent().getStringExtra("projectName");
        String projectDesc = getIntent().getStringExtra("projectDesc");
        String projectType = getIntent().getStringExtra("projectType");
        String supplies = getIntent().getStringExtra("supplies");
        String visibility = getIntent().getStringExtra("visibility");
        String date = getIntent().getStringExtra("date");
        String imageUrl = getIntent().getStringExtra("imageUrl");

        // 🔹 Fill data
        textProjectName.setText(projectName);
        textUsername.setText("@" + username);
        textProjectDesc.setText(projectDesc);
        textProjectType.setText("Type: " + projectType);
        textSupplies.setText("Supplies: " + supplies);
        textVisibility.setText("Visibility: " + visibility);
        textDate.setText("Date: " + date);

        // 🔹 Load image manually (no Glide)
        if (imageUrl != null && !imageUrl.isEmpty()) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                imageProject.setImageBitmap(bitmap);
            } catch (Exception e) {
                Log.e(TAG, "Image load failed: " + e.getMessage());
                imageProject.setImageResource(R.drawable.ic_post_placeholder);
            }
        } else {
            imageProject.setImageResource(R.drawable.ic_post_placeholder);
        }
    }
}