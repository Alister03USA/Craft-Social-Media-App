package com.example.androidexample;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Main search page for Users, Groups, Projects, and Tutorials.
 * - Handles switching between tabs
 * - Passes queries to fragments
 * - Directly supports tutorial search
 */
public class SearchActivity extends BaseActivity {

    private EditText searchInput;
    private Button searchButton;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private SearchPagerAdapter pagerAdapter;

    private String currentQuery = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_tabs);
        setupBottomNavigation(R.id.nav_search);

        searchInput = findViewById(R.id.searchInput);
        searchButton = findViewById(R.id.searchButton);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        pagerAdapter = new SearchPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Users"); break;
                case 1: tab.setText("Groups"); break;
                case 2: tab.setText("Projects"); break;
                case 3: tab.setText("Tutorials"); break;
            }
        }).attach();

        // 🔹 Auto-search when switching tabs
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(@NonNull TabLayout.Tab tab) {
                triggerSearch(currentQuery);
            }
            @Override public void onTabUnselected(@NonNull TabLayout.Tab tab) {}
            @Override public void onTabReselected(@NonNull TabLayout.Tab tab) {
                triggerSearch(currentQuery);
            }
        });

        // 🔹 Handle Search button click
        searchButton.setOnClickListener(v -> {
            currentQuery = searchInput.getText().toString().trim();
            if (!currentQuery.isEmpty()) {
                triggerSearch(currentQuery);
            }
        });
    }

    /**
     * Send search query to current visible tab fragment.
     * If on "Tutorials" tab, performs fetch directly using /tutorial/search.
     */
    private void triggerSearch(String query) {
        int position = viewPager.getCurrentItem();

        // If we're in the Tutorials tab, handle search manually
        if (position == 3) {
            fetchTutorials(query);
            return;
        }

        // Otherwise send search query to active fragment (Users, Groups, Projects)
        String tag = "f" + position;
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment instanceof SearchableTab) {
            ((SearchableTab) fragment).refreshResults(query);
        }
    }

    /**
     * ✅ Fetch Tutorials from backend
     * GET /tutorial/search?query={text}
     */
    private void fetchTutorials(String query) {
        String url = "http://coms-3090-028.class.las.iastate.edu:8080/tutorial/search?query=" + query;
        Log.d("TutorialSearch", "Fetching tutorials from URL: " + url);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> handleTutorialResponse(response),
                error -> Log.e("TutorialSearch", "❌ Error fetching tutorials", error)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    /**
     *  Parse backend tutorial response and update RecyclerView.
     */
    private void handleTutorialResponse(JSONArray response) {
        try {
            Log.d("TutorialSearch", "✅ Tutorial response received: " + response.length() + " items");

            List<TutorialItem> tutorialResults = new ArrayList<>();

            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);
                long id = obj.optLong("id", -1);
                String title = obj.optString("title", "Untitled");
                String description = obj.optString("description", "");
                String category = obj.optString("category", "");
                String username = obj.optString("username", "Unknown");
                String fileUrl = obj.optString("fileURL", "");

                tutorialResults.add(new TutorialItem(id, title, description, category, fileUrl, "", username));
            }

            // ✅ Pass results to the TutorialSearchFragment
            Fragment currentFragment = getSupportFragmentManager()
                    .findFragmentByTag("f" + viewPager.getCurrentItem());

            if (currentFragment instanceof TutorialSearchFragment) {
                ((TutorialSearchFragment) currentFragment).updateTutorials(tutorialResults);
                Log.d("TutorialSearch", "✅ Sent " + tutorialResults.size() + " results to fragment");
            } else {
                Log.w("TutorialSearch", "⚠️ Current fragment is not TutorialSearchFragment");
            }

        } catch (Exception e) {
            Log.e("TutorialSearch", "❌ Error parsing tutorial JSON", e);
        }
    }
}