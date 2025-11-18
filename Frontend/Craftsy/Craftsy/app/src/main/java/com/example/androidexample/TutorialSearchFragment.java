package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays search results for Tutorials inside SearchActivity.
 * Allows opening TutorialDetailActivity and uploading new tutorials.
 */
public class TutorialSearchFragment extends Fragment implements SearchableTab {

    private RecyclerView recyclerView;
    private TutorialAdapter adapter;
    private final List<TutorialItem> tutorialList = new ArrayList<>();
    private static final String BASE_URL =
            "http://coms-3090-028.class.las.iastate.edu:8080/tutorial/search?query=";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tutorial_search, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewTutorials);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TutorialAdapter(requireContext(), tutorialList, item -> {
            Intent intent = new Intent(requireContext(), TutorialDetailActivity.class);
            intent.putExtra("id", item.getId());
            intent.putExtra("title", item.getTitle());
            intent.putExtra("description", item.getDescription());
            intent.putExtra("category", item.getCategory());
            intent.putExtra("fileUrl", item.getFileURL());
            intent.putExtra("username", item.getUsername());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // ✅ Upload button → TutorialUploadActivity
        FloatingActionButton uploadBtn = view.findViewById(R.id.btnUploadTutorial);
        uploadBtn.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), TutorialUploadActivity.class);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void refreshResults(String query) {
        if (query == null) return;
        if (query.trim().length() == 0) return;  // still blocks whitespace-only

        String viewer = SessionManager.getInstance().getLoggedInUsername();
        Log.d("TutorialSearch", "viewer = " + viewer);

        String url = BASE_URL + query + "&username=" + viewer;
        Log.d("TutorialSearch", "Fetching tutorials from " + url);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                this::handleResponse,
                error -> Log.e("TutorialSearch", "Error fetching tutorials", error)
        );

        VolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
    }

    public void updateTutorials(List<TutorialItem> newList) {
        if (tutorialList == null || adapter == null) return;
        tutorialList.clear();
        tutorialList.addAll(newList);
        adapter.notifyDataSetChanged();
        Log.d("TutorialSearchFragment", "✅ Updated tutorial list: " + newList.size());
    }

    private void handleResponse(JSONArray response) {
        try {
            tutorialList.clear();
            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);
                long id = obj.optLong("id", -1);
                String title = obj.optString("title", "Untitled");
                String description = obj.optString("description", "");
                String category = obj.optString("category", "");
                String username = obj.optString("username", "Unknown");
                String fileUrl = obj.optString("fileURL", "");
                tutorialList.add(new TutorialItem(id, title, description, category, fileUrl, "", username));
            }
            adapter.notifyDataSetChanged();
            Log.d("TutorialSearchFragment", "✅ Loaded " + response.length() + " tutorials");
        } catch (Exception e) {
            Log.e("TutorialSearchFragment", "❌ Error parsing tutorial JSON", e);
        }
    }
}