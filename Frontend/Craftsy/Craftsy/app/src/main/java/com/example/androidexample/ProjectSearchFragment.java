package com.example.androidexample;

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
 * Project search tab with full debug logging.
 */
public class ProjectSearchFragment extends Fragment implements SearchableTab {

    private static final String TAG = "ProjectSearchFragment";
    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080/search/project?query=";

    private RecyclerView recyclerView;
    private SearchAdapter adapter;
    private ArrayList<SearchItem> projectList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_project_search, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewProject);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new SearchAdapter(getContext(), projectList, "Projects");
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

        String url = BASE_URL + query;
        Log.d(TAG, "Fetching projects from URL: " + url);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
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

                Log.d(TAG, "Parsed: username=" + username + ", name=" + name + ", desc=" + desc);
                projectList.add(new SearchItem("Project", name, desc, username));
            }
            Log.d(TAG, "✅ Total projects parsed: " + projectList.size());
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e(TAG, "❌ Error parsing JSON", e);
        }
    }
}