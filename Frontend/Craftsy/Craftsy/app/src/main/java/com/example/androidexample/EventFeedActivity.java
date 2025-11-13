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

public class EventFeedActivity extends BaseActivity {

    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";
    private RecyclerView recyclerView;
    private EventsAdapter adapter;
    private List<EventModel> events = new ArrayList<>();
    private List<EventModel> filtered = new ArrayList<>();
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_feed);
        setupBottomNavigation(R.id.nav_my_profile);

        //searchView = findViewById(R.id.searchEvents);
        recyclerView = findViewById(R.id.eventFeedRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventsAdapter(this, filtered);
        recyclerView.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { filter(query); return true; }
            @Override
            public boolean onQueryTextChange(String newText) { filter(newText); return true; }
        });

        fetchAllEvents();
    }

    private void fetchAllEvents() {
        String url = BASE_URL + "/event/search/";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
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

    private void filter(String query) {
        filtered.clear();
        for (EventModel e : events) {
            if (e.getName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(e);
            }
        }
        adapter.notifyDataSetChanged();
    }

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
