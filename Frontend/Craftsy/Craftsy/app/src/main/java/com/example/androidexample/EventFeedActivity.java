package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity that displays a feed of all events retrieved from the backend.
 * Supports searching events by name and navigating to event details.
 * @author Quinn Weidenaar
 */
public class EventFeedActivity extends BaseActivity {

    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";
    private RecyclerView recyclerView;
    private EventsAdapter adapter;
    private List<EventModel> events = new ArrayList<>();
    private List<EventModel> filtered = new ArrayList<>();
    private SearchView searchView;

    /**
     * Initializes the event feed UI, sets up the RecyclerView and search functionality,
     * and loads all available events from the backend.
     *
     * @param savedInstanceState previously saved state, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_feed);
        setupBottomNavigation(R.id.nav_my_profile);

        recyclerView = findViewById(R.id.eventFeedRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventsAdapter(this, filtered);
        recyclerView.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });

        fetchAllEvents();
    }

    /**
     * Sends a GET request to the backend to retrieve the full list of events.
     * Updates the RecyclerView once data is loaded.
     */
    private void fetchAllEvents() {
        String url = BASE_URL + "/event/search/";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    events.clear();
                    parseEventArray(response, events);
                    filtered.clear();
                    filtered.addAll(events);
                    adapter.notifyDataSetChanged();
                },
                error -> Log.e("Feed", "Error fetching events", error)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    /**
     * Filters the displayed event list by checking whether the event name
     * contains the query text (case-insensitive).
     *
     * @param query the search input string
     */
    private void filter(String query) {
        filtered.clear();
        for (EventModel e : events) {
            if (e.getName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(e);
            }
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * Parses a JSON array of event objects and adds them to the provided list.
     *
     * @param array the JSON array containing event data
     * @param list  the list to populate with parsed EventModel objects
     */
    private void parseEventArray(JSONArray array, List<EventModel> list) {
        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                list.add(EventModel.fromJson(obj));
            }
        } catch (Exception e) {
            Log.e("Feed", "Parse error", e);
        }
    }
}
