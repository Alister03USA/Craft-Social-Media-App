package com.example.androidexample;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.content.Intent;
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

public class FeedActivity extends AppCompatActivity {

    private static final String TAG = "FeedActivity";
    private static final String BASE_URL = "http://10.90.72.69:8080/feed/alister_gan";
    private RecyclerView recyclerView;
    private FeedAdapter feedAdapter;
    private List<FeedItem> feedList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        feedAdapter = new FeedAdapter(feedList);
        recyclerView.setAdapter(feedAdapter);

        findViewById(R.id.btnManageFeed).setOnClickListener(v -> {
            Intent intent = new Intent(FeedActivity.this, FeedCRUDActivity.class);
            startActivity(intent);
        });

        loadFeed();
    }

    private void loadFeed() {
        Log.d(TAG, "Requesting feed from: " + BASE_URL);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, BASE_URL, null,
                response -> {
                    try {
                        feedList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            JSONObject userObj = obj.getJSONObject("user");

                            FeedItem item = new FeedItem(
                                    userObj.getString("username"),
                                    obj.optString("projectName", ""),
                                    obj.optString("projectDesc", ""),
                                    obj.optString("projectType", ""),
                                    obj.optString("supplies", ""),
                                    obj.optString("visibility", ""),
                                    obj.optString("date", "")
                            );
                            feedList.add(item);
                        }

                        feedAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "Loaded " + feedList.size() + " posts", Toast.LENGTH_SHORT).show();

                    } catch (JSONException e) {
                        Toast.makeText(this, "Parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Parse error", e);
                    }
                },
                error -> {
                    Toast.makeText(this, "Error loading feed: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Volley error", error);
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}