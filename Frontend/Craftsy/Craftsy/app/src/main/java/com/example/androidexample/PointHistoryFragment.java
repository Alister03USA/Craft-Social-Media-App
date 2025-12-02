package com.example.androidexample;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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

public class PointHistoryFragment extends Fragment {

    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";

    private RecyclerView rvHistory;
    private ProgressBar progressBar;
    private PointHistoryAdapter adapter;
    private ArrayList<PointHistoryItem> historyList = new ArrayList<>();

    private String username;

    public PointHistoryFragment() {}

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_point_history, container, false);

        rvHistory = view.findViewById(R.id.rvPointHistory);
        progressBar = view.findViewById(R.id.progressHistory);

        username = SessionManager.getInstance().getLoggedInUsername();

        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PointHistoryAdapter(historyList);
        rvHistory.setAdapter(adapter);

        loadHistory();

        return view;
    }

    private void loadHistory() {
        progressBar.setVisibility(View.VISIBLE);

        String url = BASE_URL + "/points/" + username + "/history";
        Log.d("PointHistory", "GET " + url);

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    parseHistory(response);
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed to load history", Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(getContext()).addToRequestQueue(req);
    }

    private void parseHistory(JSONArray arr) {
        historyList.clear();

        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.optJSONObject(i);
            if (obj == null) continue;

            PointHistoryItem item = new PointHistoryItem(
                    obj.optLong("id"),
                    obj.optInt("points"),
                    obj.optString("action"),
                    obj.optString("referenceId"),
                    obj.optString("date")
            );

            historyList.add(item);
        }

        adapter.notifyDataSetChanged();
    }
}