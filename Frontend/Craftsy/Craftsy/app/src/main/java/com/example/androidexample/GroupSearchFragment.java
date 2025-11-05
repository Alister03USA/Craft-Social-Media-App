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

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class GroupSearchFragment extends Fragment implements SearchableTab {

    private RecyclerView recyclerView;
    private SearchAdapter adapter;
    private List<SearchItem> groupList = new ArrayList<>();

    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_search, container, false);

        recyclerView = view.findViewById(R.id.groupSearchRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SearchAdapter(
                getContext(),
                groupList,
                "Groups",
                clickedItem -> {
                    Log.d("GroupSearch", "Clicked group: " + clickedItem.getTitle());

                    Intent intent = new Intent(getContext(), GroupDetailsActivity.class);
                    intent.putExtra("groupId", Long.parseLong(clickedItem.getUsername())); // ✅ username stores groupId
                    intent.putExtra("groupName", clickedItem.getTitle());
                    intent.putExtra("groupDescription", clickedItem.getDescription());
                    startActivity(intent);
                }
        );
        recyclerView.setAdapter(adapter);


        return view;
    }

    @Override
    public void refreshResults(String query) {
        fetchGroups(query);
    }

    private void fetchGroups(String query) {
        String url = BASE_URL + "/search/group?query=" + query;
        Log.d("GroupSearch", "Fetching groups from URL: " + url);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> handleGroupResponse(response),
                error -> Log.e("GroupSearch", "Error fetching groups", error)
        );

        VolleySingleton.getInstance(getContext()).addToRequestQueue(request);
    }

    private void handleGroupResponse(JSONArray response) {
        try {
            groupList.clear();
            Log.d("GroupSearch", "Groups response length: " + response.length());

            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);

                //  Match keys exactly as returned by backend
                String groupName = obj.optString("groupName", "Unnamed Group");
                String description = obj.optString("description", "");
                String owner = obj.optString("craft", ""); // or "memberCount"/"isPrivate" if you prefer

                Log.d("GroupSearch", "Parsed group: " + groupName);
                long groupId = obj.optLong("id", -1);

                SearchItem item = new SearchItem(
                        "group",
                        groupName,
                        description,
                        String.valueOf(groupId) // ✅ store ID so click listener can read it
                );
                groupList.add(item);
            }

            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e("GroupSearch", "Error parsing group JSON", e);
        }
    }

}
