package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows all tutorials fetched from backend and opens TutorialDetailActivity on click.
 * Author: Ji Xian Fu (Frontend)
 */
public class TutorialFeedActivity extends AppCompatActivity {

    private static final String TAG = "TutorialFeed";
    private static final String BASE_URL =
            "http://coms-3090-028.class.las.iastate.edu:8080/tutorial/search?query=";

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private EditText searchInput;
    private MaterialButton searchButton;
    private FloatingActionButton addTutorialButton;

    private final List<TutorialItem> tutorialList = new ArrayList<>();
    private TutorialAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial_feed);

        recyclerView = findViewById(R.id.tutorialRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        searchInput = findViewById(R.id.searchInput);
        searchButton = findViewById(R.id.searchButton);
        addTutorialButton = findViewById(R.id.addTutorialButton);

        adapter = new TutorialAdapter(this, tutorialList, item -> {
            Intent intent = new Intent(this, TutorialDetailActivity.class);
            intent.putExtra("id", item.getId());
            intent.putExtra("title", item.getTitle());
            intent.putExtra("description", item.getDescription());
            intent.putExtra("category", item.getCategory());
            intent.putExtra("fileUrl", item.getFileURL());   // ✅ correct key
            intent.putExtra("filePath", item.getFilePath());
            intent.putExtra("username", item.getUsername());

            Log.d(TAG, "Opening detail for: " + item.getTitle()
                    + " | id=" + item.getId()
                    + " | fileURL=" + item.getFileURL());

            startActivity(intent);
        });

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        fetchTutorials("");

        searchButton.setOnClickListener(v -> {
            String query = searchInput.getText().toString().trim();
            fetchTutorials(query);
        });

        addTutorialButton.setOnClickListener(v ->
                startActivity(new Intent(this, TutorialUploadActivity.class)));
    }

    /** Fetch tutorials from backend */
    private void fetchTutorials(String query) {
        progressBar.setVisibility(View.VISIBLE);
        String url = BASE_URL + query;
        Log.d(TAG, "Fetching tutorials from: " + url);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    tutorialList.clear();
                    parseResponse(response);
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Volley error: " + error.getMessage(), error);
                    Toast.makeText(this, "Failed to load tutorials", Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(this).add(request);
    }

    /** Parse backend JSON and populate list */
    private void parseResponse(JSONArray response) {
        try {
            Log.d(TAG, "Raw JSON response: " + response);
            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);

                long id = obj.optLong("id", -1);
                String title = obj.optString("title", "Untitled");
                String description = obj.optString("description", "No description");
                String category = obj.optString("category", "Uncategorized");
                String fileUrl = obj.optString("fileURL", null); // ✅ match backend key
                String username = obj.optString("username", "Unknown");

                tutorialList.add(new TutorialItem(id, title, description,
                        category, fileUrl, null, username));

                Log.d(TAG, "Added: " + title + " | id=" + id + " | fileURL=" + fileUrl);
            }

            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            Log.e(TAG, "JSON parse error", e);
        } finally {
            progressBar.setVisibility(View.GONE);
        }
    }
}