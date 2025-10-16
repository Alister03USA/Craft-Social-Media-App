package com.example.androidexample;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        setupBottomNavigation(R.id.myFeed);

        recyclerViewFeed = findViewById(R.id.recyclerViewFeed);
        recyclerViewFeed.setLayoutManager(new LinearLayoutManager(this));
        feedAdapter = new FeedAdapter(this, feedList);
        recyclerViewFeed.setAdapter(feedAdapter);

        // username from Login -> UserProfile -> FeedActivity
        String username = getIntent().getStringExtra("username");
        if (username == null || username.trim().isEmpty()) {
            // last-resort fallback so the screen never crashes on null
            username = "katiekeck";
        }
        Log.d(TAG, "Loading for username=" + username);

        loadFeed(username);
    }

    private void loadFeed(String username) {
        final String url = BASE_URL + "/" + username;
        Log.d(TAG, "GET " + url);

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET, url, null,
                this::handleFeedResponse,
                error -> {
                    String detail = "no networkResponse";
                    if (error != null && error.networkResponse != null) {
                        try {
                            detail = "HTTP " + error.networkResponse.statusCode +
                                    " : " + new String(error.networkResponse.data, "UTF-8");
                        } catch (Exception ignored) {}
                    }
                    Log.e(TAG, "Feed request failed: " + detail, error);
                    Toast.makeText(this, "Failed to load feed: " + detail, Toast.LENGTH_LONG).show();
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void handleFeedResponse(JSONArray arr) {
        try {
            feedList.clear();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);

                // nested user obj
                String u = "";
                JSONObject user = o.optJSONObject("user");
                if (user != null) {
                    u = user.optString("username", "");
                }

                String name  = o.optString("projectName",  o.optString("project_name", ""));
                String desc  = o.optString("projectDesc",  o.optString("project_desc", ""));
                String type  = o.optString("projectType",  o.optString("project_type", ""));
                String supp  = o.optString("supplies", "");
                String vis   = o.optString("visibility", "");
                String date  = o.optString("date", "");

                feedList.add(new FeedItem(u, name, desc, type, supp, vis, date));
            }
            feedAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Loaded " + feedList.size() + " posts", Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            Log.e(TAG, "parse error", e);
            Toast.makeText(this, "Invalid feed JSON", Toast.LENGTH_SHORT).show();
        }
    }
}