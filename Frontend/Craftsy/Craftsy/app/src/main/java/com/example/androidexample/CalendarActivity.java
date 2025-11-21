package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class CalendarActivity extends BaseActivity {

    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";
    private RecyclerView recyclerView;
    private EventsAdapter adapter;
    private List<EventModel> events = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        setupBottomNavigation(R.id.nav_my_profile);

        recyclerView = findViewById(R.id.recyclerEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventsAdapter(this, events);
        recyclerView.setAdapter(adapter);

        fetchUserEvents();
    }

    private void fetchUserEvents() {
        String username = SessionManager.getInstance().getLoggedInUsername();
        String url = BASE_URL + "/event/user/" + username;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    events.clear();
                    parseEventArray(response, events);
                    adapter.notifyDataSetChanged();
                },
                error -> Log.e("Calendar", "Error fetching events", error)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void parseEventArray(JSONArray array, List<EventModel> list) {
        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                list.add(EventModel.fromJson(obj));
            }
        } catch (Exception e) {
            Log.e("Calendar", "Parse error", e);
        }
    }
}
