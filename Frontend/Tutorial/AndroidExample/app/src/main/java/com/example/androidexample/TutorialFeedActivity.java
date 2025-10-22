package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
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

public class TutorialFeedActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TutorialAdapter adapter;
    private final ArrayList<TutorialItem> list = new ArrayList<>();
    private EditText searchInput;
    private ImageButton searchButton;

    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080/tutorial/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial_feed);

        recyclerView = findViewById(R.id.tutorialRecyclerView);
        searchInput = findViewById(R.id.searchTutorial);
        searchButton = findViewById(R.id.searchTutorialBtn);

        adapter = new TutorialAdapter(list, this::openDetail);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        searchButton.setOnClickListener(v -> performSearch());

        // initial mock load
        fetch(BASE_URL + "search?query=");
    }

    private void performSearch() {
        String query = searchInput.getText().toString().trim();
        fetch(BASE_URL + "search?query=" + query);
    }

    private void fetch(String url) {
        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null,
                this::parseResults,
                e -> Toast.makeText(this, "Fetch failed", Toast.LENGTH_SHORT).show());
        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void parseResults(JSONArray arr) {
        list.clear();
        try {
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                list.add(new TutorialItem(
                        o.optLong("id"), o.optString("title"),
                        o.optString("description"),
                        o.optString("category"),
                        o.optString("fileURL")));
            }
        } catch (JSONException e) { e.printStackTrace(); }
        adapter.notifyDataSetChanged();
    }

    private void openDetail(long id) {
        Intent i = new Intent(this, TutorialDetailActivity.class);
        i.putExtra("tutorial_id", id);
        startActivity(i);
    }
}