package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Project search tab — now opens FeedDetailActivity with full project details.
 */
public class ProjectSearchFragment extends Fragment implements SearchableTab {

    private static final String TAG = "ProjectSearchFragment";
    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080/search/project?query=";

    private RecyclerView recyclerView;
    private SearchAdapter adapter;
    private final ArrayList<SearchItem> projectList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_project_search, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewProject);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new SearchAdapter(getContext(), projectList, "Projects", item -> {
            Log.d(TAG, "Clicked project: " + item.getTitle() + " by " + item.getUsername());

            Intent intent = new Intent(requireContext(), FeedDetailActivity.class);
            intent.putExtra("username", item.getUsername());
            intent.putExtra("projectName", item.getTitle());
            intent.putExtra("projectDesc", item.getDescription());

            if (item.getExtrasMap() != null) {
                intent.putExtra("projectType", item.getExtrasMap().optString("projectType", ""));
                intent.putExtra("supplies", item.getExtrasMap().optString("supplies", ""));
                intent.putExtra("visibility", item.getExtrasMap().optString("visibility", ""));
                intent.putExtra("date", item.getExtrasMap().optString("date", ""));
                intent.putExtra("imageUrl", item.getExtrasMap().optString("imageUrl", ""));
            }

            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        Log.d(TAG, "ProjectSearchFragment initialized.");
        return view;
    }

    @Override
    public void refreshResults(String query) {
        if (query == null || query.trim().isEmpty()) {
            Log.d(TAG, "Empty query, skipping search.");
            return;
        }

        String url = BASE_URL + query.trim();
        Log.d(TAG, "Fetching projects from URL: " + url);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d(TAG, "✅ Project response received: " + response);
                    projectList.clear();
                    parseResults(response);
                },
                error -> {
                    Log.e(TAG, "❌ Volley error while fetching projects", error);
                    Toast.makeText(getContext(), "Failed to fetch projects", Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
    }

    private void parseResults(JSONArray arr) {
        try {
            Log.d(TAG, "Parsing JSON, length=" + arr.length());
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                Log.d(TAG, "Raw JSON object [" + i + "]: " + obj.toString());

                String username = obj.optString("username", "Unknown");
                String name = obj.optString("projectName", "No Project Name");
                String desc = obj.optString("projectDesc", "No Description");
                String projectType = obj.optString("projectType", "");
                String supplies = obj.optString("supplies", "");
                String visibility = obj.optString("visibility", "");
                String date = obj.optString("date", "");
                String imageUrl = obj.optString("projectPic", "");

                Log.d(TAG, "Parsed: " + name + " by " + username);

                // ✅ Store all project extras inside the SearchItem (in a JSON-style map)
                SearchItem item = new SearchItem("Project", name, desc, username);
                JSONObject extras = new JSONObject();
                extras.put("projectType", projectType);
                extras.put("supplies", supplies);
                extras.put("visibility", visibility);
                extras.put("date", date);
                extras.put("imageUrl", imageUrl);
                item.setExtrasMap(extras);

                projectList.add(item);
            }

            Log.d(TAG, "✅ Total projects parsed: " + projectList.size());
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e(TAG, "❌ Error parsing JSON", e);
        }
    }
}