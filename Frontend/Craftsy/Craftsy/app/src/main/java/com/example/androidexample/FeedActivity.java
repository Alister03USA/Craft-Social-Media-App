package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class FeedActivity extends BaseActivity {

    private RecyclerView recyclerViewFeed;
    private FeedAdapter feedAdapter;
    private final List<FeedItem> feedList = new ArrayList<>();

    private static final String TAG = "FeedActivity";
    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080/feed";

    private String loggedInUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        recyclerViewFeed = findViewById(R.id.recyclerViewFeed);
        recyclerViewFeed.setLayoutManager(new LinearLayoutManager(this));

        // ✅ Get username passed from login or previous activity
        loggedInUsername = getIntent().getStringExtra("username");
        if (loggedInUsername == null || loggedInUsername.trim().isEmpty()) {
            loggedInUsername = "Fuji"; // fallback
        }

        // 🔹 "Add Post" button
        findViewById(R.id.btnAddPost).setOnClickListener(v -> {
            Intent intent = new Intent(this, FeedCRUDActivity.class);
            intent.putExtra("mode", "add");
            intent.putExtra("username", loggedInUsername);
            startActivity(intent);
        });

        // 🔹 Adapter with Edit/Delete listeners
        feedAdapter = new FeedAdapter(this, feedList, "feed");
        recyclerViewFeed.setAdapter(feedAdapter);

        // 🔹 Bottom navigation bar
        setupBottomNavigation(R.id.bottom_navigation);

        Log.d(TAG, "FeedActivity created for: " + loggedInUsername);
        loadFeed(loggedInUsername);
    }

    /** 🟩 Auto-refresh whenever you return to this screen */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Refreshing feed for user: " + loggedInUsername);
        loadFeed(loggedInUsername);
    }

    /** Load feed data from backend */
    private void loadFeed(String username) {
        final String url = BASE_URL + "/" + username;

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET, url, null,
                this::handleFeedResponse,
                error -> {
                    String detail = (error.networkResponse != null)
                            ? "HTTP " + error.networkResponse.statusCode
                            : "Network error";
                    Log.e(TAG, "Feed load failed: " + detail, error);
                    Toast.makeText(this, "Failed to load feed", Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    /** Parse backend JSON into FeedItem list */
    private void handleFeedResponse(JSONArray arr) {
        try {
            feedList.clear();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                JSONObject userObj = o.optJSONObject("user");

                String u = userObj != null ? userObj.optString("username", "") : "";
                String name = o.optString("projectName", "");
                String desc = o.optString("projectDesc", "");
                String type = o.optString("projectType", "");
                String supplies = o.optString("supplies", "");
                String visibility = o.optString("visibility", "");
                String date = o.optString("date", "");

                feedList.add(new FeedItem(u, name, desc, type, supplies, visibility, date));
            }
            feedAdapter.notifyDataSetChanged();
            Log.d(TAG, "Feed reloaded with " + feedList.size() + " posts");
        } catch (JSONException e) {
            Log.e(TAG, "Parse error", e);
        }
    }

    /** Edit button → open FeedCRUDActivity (prefilled mode) */
    private void onEditClicked(FeedItem item) {
        Intent intent = new Intent(this, FeedCRUDActivity.class);
        intent.putExtra("mode", "edit");
        intent.putExtra("username", loggedInUsername);
        intent.putExtra("projectName", item.getProjectName());
        intent.putExtra("projectDesc", item.getProjectDesc());
        intent.putExtra("projectType", item.getProjectType());
        intent.putExtra("supplies", item.getSupplies());
        intent.putExtra("visibility", item.getVisibility());
        startActivity(intent);
    }

    /** Delete button → open FeedCRUDActivity (prefilled for confirmation) */
    private void onDeleteClicked(FeedItem item) {
        Intent intent = new Intent(this, FeedCRUDActivity.class);
        intent.putExtra("mode", "delete");
        intent.putExtra("username", loggedInUsername);
        intent.putExtra("projectName", item.getProjectName());
        startActivity(intent);
    }
}