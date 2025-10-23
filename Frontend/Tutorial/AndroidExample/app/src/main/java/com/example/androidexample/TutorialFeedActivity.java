package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a grid of tutorials belonging to a specific user.
 * When clicked, each tutorial opens TutorialDetailActivity for detailed view.
 */
public class TutorialFeedActivity extends AppCompatActivity {

    private RecyclerView tutorialRecyclerView;
    private TutorialAdapter tutorialAdapter;
    private ProgressBar progressBar;
    private List<TutorialItem> tutorialList;

    // Replace with your COMS309 server URL
    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080/tutorial/user/";
    private String username = "Fuji"; // Replace with logged-in user if available

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial_feed);

        // Initialize UI components
        tutorialRecyclerView = findViewById(R.id.tutorialRecyclerView);
        progressBar = findViewById(R.id.progressBar);

        // Initialize list and adapter
        tutorialList = new ArrayList<>();
        tutorialAdapter = new TutorialAdapter(tutorialList, item -> {
            Intent intent = new Intent(TutorialFeedActivity.this, TutorialDetailActivity.class);
            intent.putExtra("tutorialId", item.getId());
            intent.putExtra("username", item.getUsername());
            startActivity(intent);
        });

        tutorialRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        tutorialRecyclerView.setAdapter(tutorialAdapter);

        // Fetch data from backend
        fetchTutorials();
    }

    /**
     * Fetches tutorials for the given username from the backend.
     */
    private void fetchTutorials() {
        progressBar.setVisibility(View.VISIBLE);
        String url = BASE_URL + username;

        Log.d("TutorialFeedActivity", "Fetching tutorials from: " + url);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    parseTutorials(response);
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("TutorialFeedActivity", "Fetch failed: " + error.toString());
                    Toast.makeText(this, "Failed to fetch tutorials", Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    /**
     * Parses the JSON array response and updates RecyclerView.
     */
    private void parseTutorials(JSONArray response) {
        try {
            tutorialList.clear();

            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);

                long id = obj.getLong("id");
                String title = obj.optString("title", "Untitled");
                String description = obj.optString("description", "");
                String category = obj.optString("category", "");
                String fileURL = obj.optString("fileURL", "");
                String username = obj.optString("username", this.username);

                TutorialItem item = new TutorialItem(id, title, description, category, fileURL, username);
                tutorialList.add(item);
            }

            if (tutorialList.isEmpty()) {
                Toast.makeText(this, "No tutorials found", Toast.LENGTH_SHORT).show();
            }

            tutorialAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            Log.e("TutorialFeedActivity", "Parsing error", e);
            Toast.makeText(this, "Error parsing tutorials", Toast.LENGTH_SHORT).show();
        }
    }
}