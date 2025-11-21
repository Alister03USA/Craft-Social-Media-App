package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
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

/**
 * Activity responsible for displaying a feed of user-created patterns.
 * Fetches patterns from the backend using the logged-in user's username,
 * displays them in a RecyclerView, and provides UI options to create new patterns.
 * @author Quinn Weidenaar
 */
public class PatternActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private PatternAdapter adapter;
    private List<Pattern> patterns = new ArrayList<>();

    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080/patterns";
    private static final String TAG = "PatternActivity";

    /**
     * Called when the activity is first created.
     * Initializes UI components, sets up toolbar and bottom navigation,
     * and loads pattern data from backend.
     *
     * @param savedInstanceState previous saved instance state, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern);

        // Setup bottom navigation
        setupBottomNavigation(R.id.pattern);

        // Toolbar setup for top-right "Create" button
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

    /**
     * Inflates the top-right menu which contains the "Create Pattern" button.
     *
     * @param menu menu instance to populate
     * @return true if menu successfully created
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pattern_feed_menu, menu);
        return true;
    }

    /**
     * Handles toolbar menu interactions. Launches CreatePatternActivity when
     * the "Create Pattern" button is clicked.
     *
     * @param item selected menu item
     * @return true if handled, otherwise passes to superclass
     */
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

    /**
     * Fetches patterns for the logged-in user from the backend.
     * Populates the RecyclerView adapter with the results.
     */
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

                            Pattern pattern = new Pattern(
                                    i, // temporary ID
                                    obj.optString("patternName", "Untitled"),
                                    obj.getJSONObject("user").optString("username", "Unknown"),
                                    obj.optString("patternType", "N/A"),
                                    (float) obj.optDouble("rating", 0.0f),
                                    getFirstImagePath(obj.optJSONArray("images")),
                                    obj.optString("patternLink", ""),
                                    obj.optString("difficulty", "N/A"),
                                    obj.optString("description", ""),
                                    obj.optString("supplies", ""),
                                    obj.optString("date", "")
                            );

                            patterns.add(pattern);
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parsing error", e);
                            Log.d(TAG, "Raw response: " + response.toString());
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

    private static final String IMAGE_BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080/uploads/";

    /**
     * Extracts the first image file path from a JSONArray of images.
     *
     * @param images JSON array containing image metadata objects
     * @return the full URL of the first image, or an empty string if none found
     */
    private String getFirstImagePath(JSONArray images) {
        if (images != null && images.length() > 0) {
            JSONObject img = images.optJSONObject(0);
            if (img != null) {
                String path = img.optString("filePath", "");
                if (!path.isEmpty()) {
                    return IMAGE_BASE_URL + path.substring(path.lastIndexOf("/") + 1);
                }
            }
        }
        return "";
    }
}
