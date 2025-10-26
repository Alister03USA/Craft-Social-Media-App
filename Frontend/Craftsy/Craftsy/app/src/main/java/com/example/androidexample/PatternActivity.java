package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;

import java.util.ArrayList;
import java.util.List;

public class PatternActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private PatternAdapter adapter;
    private List<Pattern> patterns = new ArrayList<>();

    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080/patterns";
    private static final String TAG = "PatternActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern);

        // Setup bottom navigation
        setupBottomNavigation(R.id.pattern);

        // ✅ Toolbar setup for top-right "Create" button
        Toolbar toolbar = findViewById(R.id.patternsToolbar);
        setSupportActionBar(toolbar);
        setTitle("Patterns");

        // RecyclerView setup
        recyclerView = findViewById(R.id.patternsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PatternAdapter(this, patterns);
        recyclerView.setAdapter(adapter);

        fetchPatterns();
    }

    // ✅ Inflate the top-right "Create Pattern" menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pattern_feed_menu, menu);
        return true;
    }

    // ✅ Handle the "Create Pattern" button click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_create_pattern) {
            SessionManager session = SessionManager.getInstance();
            String username = session.getLoggedInUsername();
            if (username == null || username.isEmpty()) {
                username = "testUser"; // fallback
                Log.w(TAG, "No logged-in username found. Using fallback: testUser");
            }

            Intent intent = new Intent(this, CreatePatternActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ✅ Fetch patterns from backend using SessionManager username
    private void fetchPatterns() {
        SessionManager session = SessionManager.getInstance();
        String username = session.getLoggedInUsername();
        if (username == null || username.isEmpty()) {
            username = "testUser"; // fallback if not logged in
            Log.w(TAG, "No logged-in username found. Using fallback: testUser");
        }

        String url = BASE_URL + "/" + username;
        Log.d(TAG, "Fetching patterns from: " + url);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    patterns.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);

                            // 🔹 Extract image if present (backend uses images list)
                            String imageUrl = "";
                            JSONArray imagesArray = obj.optJSONArray("images");
                            if (imagesArray != null && imagesArray.length() > 0) {
                                JSONObject firstImage = imagesArray.getJSONObject(0);
                                imageUrl = firstImage.optString("imageUrl", "");
                            }

                            Pattern pattern = new Pattern(
                                    obj.getInt("id"),
                                    obj.optString("patternName", "Untitled"),
                                    obj.getJSONObject("user").optString("username", "Unknown"),
                                    obj.optString("patternType", "N/A"),
                                    (float) obj.optDouble("rating", 0.0),
                                    imageUrl,
                                    obj.optString("patternLink", ""),
                                    obj.optString("difficulty", "N/A"),
                                    obj.optString("description", ""),
                                    obj.optString("supplies", ""),
                                    obj.optString("date", "")
                            );

                            patterns.add(pattern);
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parsing error", e);
                        }
                    }
                    adapter.notifyDataSetChanged();
                },
                error -> {
                    Log.e(TAG, "Volley error while fetching patterns", error);

                    if (error.networkResponse != null) {
                        Log.e(TAG, "Status Code: " + error.networkResponse.statusCode);
                        Log.e(TAG, "Response Data: " + new String(error.networkResponse.data));
                    } else {
                        Log.e(TAG, "No network response (possibly timeout or no connection)");
                    }

                    Toast.makeText(
                            PatternActivity.this,
                            "Error fetching patterns: " + error.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}
