package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.material.appbar.MaterialToolbar;

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
    private static final String IMAGE_BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080/uploads/"; //  where images are served

    private String loggedInUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        MaterialToolbar toolbar = findViewById(R.id.feedToolbar);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_notifications) {
                Intent intent = new Intent(this, NotificationCenterActivity.class);
                intent.putExtra("username", loggedInUsername);
                startActivity(intent);
                return true;
            }
            return false;
        });



        recyclerViewFeed = findViewById(R.id.recyclerViewFeed);
        recyclerViewFeed.setLayoutManager(new LinearLayoutManager(this));

        loggedInUsername = getIntent().getStringExtra("username");
        if (loggedInUsername == null || loggedInUsername.trim().isEmpty()) {
            loggedInUsername = "Fuji"; // This needs to change
        }

        findViewById(R.id.btnAddPost).setOnClickListener(v -> {
            Intent intent = new Intent(this, FeedCRUDActivity.class);
            intent.putExtra("mode", "add");
            intent.putExtra("username", loggedInUsername);
            startActivity(intent);
        });

        feedAdapter = new FeedAdapter(this, feedList, "feed", loggedInUsername);
        recyclerViewFeed.setAdapter(feedAdapter);

        setupBottomNavigation(R.id.myFeed);

        Log.d(TAG, "FeedActivity created for: " + loggedInUsername);
        loadFeed(loggedInUsername);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFeed(loggedInUsername);
    }

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

                //  Get first image path if available
                String imageUrl = null;
                JSONArray imagesArr = o.optJSONArray("images");
                if (imagesArr != null && imagesArr.length() > 0) {
                    JSONObject imgObj = imagesArr.getJSONObject(0);
                    String filePath = imgObj.optString("filePath", "");
                    if (filePath != null && !filePath.isEmpty()) {
                        // Convert backend file path to accessible URL
                        imageUrl = IMAGE_BASE_URL + filePath.substring(filePath.lastIndexOf("/") + 1);
                    }
                }

                feedList.add(new FeedItem(u, name, desc, type, supplies, visibility, date, imageUrl));
            }
            feedAdapter.notifyDataSetChanged();
            Log.d(TAG, "Feed reloaded with " + feedList.size() + " posts");
        } catch (JSONException e) {
            Log.e(TAG, "Parse error", e);
        }
    }
}