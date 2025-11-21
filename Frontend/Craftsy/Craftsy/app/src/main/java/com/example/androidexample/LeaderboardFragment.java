package com.example.androidexample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class LeaderboardFragment extends Fragment {

    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";

    private ListView leaderboardList;
    private ArrayList<LeaderboardUser> users = new ArrayList<>();

    public LeaderboardFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);
        leaderboardList = view.findViewById(R.id.leaderboardList);

        loadLeaderboard();

        return view;
    }

    private void loadLeaderboard() {
        String url = BASE_URL + "/points/leaderboard";

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> parseLeaderboard(response),
                error -> Toast.makeText(getContext(), "Failed to load leaderboard", Toast.LENGTH_SHORT).show()
        );

        VolleySingleton.getInstance(getContext()).addToRequestQueue(req);
    }

    private void parseLeaderboard(JSONArray array) {
        users.clear();

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.optJSONObject(i);
            if (obj == null) continue;

            String username = obj.optString("username", "");
            int totalPoints = obj.optInt("totalPoints", 0);
            String tier = obj.optString("tier", "BEGINNER");

            users.add(new LeaderboardUser(username, totalPoints, tier));
        }

        LeaderboardAdapter adapter = new LeaderboardAdapter(getContext(), users);
        leaderboardList.setAdapter(adapter);
    }
}