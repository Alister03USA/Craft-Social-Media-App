package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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

public class TutorialSearchFragment extends Fragment implements SearchableTab {

    private RecyclerView recyclerView;
    private TutorialAdapter adapter;
    private final List<TutorialItem> tutorialList = new ArrayList<>();

    private static final String BASE_SEARCH_URL =
            "http://coms-3090-028.class.las.iastate.edu:8080/tutorial/search?query=";

    private static final String BASE_LEADERBOARD_URL =
            "http://coms-3090-028.class.las.iastate.edu:8080/tutorial/leaderboard";

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

        // Upload tutorial button
        FloatingActionButton uploadBtn = view.findViewById(R.id.btnUploadTutorial);
        uploadBtn.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), TutorialUploadActivity.class);
            startActivity(intent);
        });

        // Sort by most liked button
        Button btnSort = view.findViewById(R.id.btnSortMostLiked);
        btnSort.setOnClickListener(v -> fetchLeaderboard());

        return view;
    }

    // Search Refresh
    @Override
    public void refreshResults(String query) {
        if (query == null || query.trim().isEmpty()) return;

        String viewer = SessionManager.getInstance().getLoggedInUsername();
        String url = BASE_SEARCH_URL + query + "&username=" + viewer;

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

    // Convert search results into tutorialList
    private void handleResponse(JSONArray response) {
        try {
            tutorialList.clear();

            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);

                tutorialList.add(new TutorialItem(
                        obj.optLong("id", -1),
                        obj.optString("title", "Untitled"),
                        obj.optString("description", ""),
                        obj.optString("category", ""),
                        obj.optString("fileURL", ""),
                        "",
                        obj.optString("username", "Unknown")
                ));
            }

            adapter.notifyDataSetChanged();
            Log.d("TutorialSearchFragment", "Loaded " + response.length() + " tutorials");

        } catch (Exception e) {
            Log.e("TutorialSearchFragment", "Error parsing search results", e);
        }
    }

    // Update list externally
    public void updateTutorials(List<TutorialItem> newList) {
        tutorialList.clear();
        tutorialList.addAll(newList);
        adapter.notifyDataSetChanged();
    }

    // Fetch leaderboard: tutorials sorted by likes
    private void fetchLeaderboard() {
        Log.d("TutorialSearch", "Fetching leaderboard from " + BASE_LEADERBOARD_URL);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                BASE_LEADERBOARD_URL,
                null,
                res -> {
                    try {
                        tutorialList.clear();

                        for (int i = 0; i < res.length(); i++) {
                            JSONObject obj = res.getJSONObject(i);

                            TutorialItem item = new TutorialItem(
                                    obj.optLong("id"),
                                    obj.optString("title"),
                                    "", // leaderboard has no description
                                    obj.optString("category"),
                                    null,
                                    "",
                                    obj.optString("username")
                            );

                            item.setLikeCount(obj.optLong("likes"));
                            tutorialList.add(item);
                        }

                        adapter.notifyDataSetChanged();
                        Log.d("TutorialSearch", "Leaderboard loaded: " + tutorialList.size());

                    } catch (Exception e) {
                        Log.e("TutorialSearch", "Error mapping leaderboard", e);
                    }
                },
                error -> Log.e("TutorialSearch", "Leaderboard fetch error", error)
        );

        VolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
    }
}