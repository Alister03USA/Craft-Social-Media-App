package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class GroupFeedActivity extends BaseActivity {

    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";
    private RecyclerView recyclerView;
    private GroupAdapter adapter;
    private List<GroupModel> groupList = new ArrayList<>();
    private List<GroupModel> filteredList = new ArrayList<>();
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_feed);

        setupBottomNavigation(R.id.nav_group);

        recyclerView = findViewById(R.id.groupRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new GroupAdapter(filteredList, group -> {
            // GroupFeedActivity adapter click
            Intent intent = new Intent(GroupFeedActivity.this, GroupActivity.class);
            intent.putExtra("groupName", group.getName());
            intent.putExtra("groupId", group.getId()); // pass id
            startActivity(intent);

        });
        recyclerView.setAdapter(adapter);

        searchView = findViewById(R.id.searchGroups);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterGroups(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterGroups(newText);
                return true;
            }
        });

        fetchUserGroups();

        // ✅ FAB for creating groups
        FloatingActionButton fab = findViewById(R.id.addGroupFab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(GroupFeedActivity.this, CreateGroupActivity.class);
            startActivity(intent);
        });
    }

    private void fetchUserGroups() {
        String username = SessionManager.getInstance().getLoggedInUsername();
        Log.d("GroupFeed", "Fetching groups for: " + username); // <-- add this
        String url = BASE_URL  +"/" + username + "/groups";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d("GroupFeed", "Response: " + response.toString());
                    try {
                        JSONArray groupsArray = response.getJSONArray("groups");
                        groupList.clear();
                        for (int i = 0; i < groupsArray.length(); i++) {
                            JSONObject obj = groupsArray.getJSONObject(i);
                            groupList.add(new GroupModel(
                                    obj.optLong("id"),
                                    obj.optString("groupName"),
                                    obj.optString("description")
                            ));
                        }
                        filteredList.clear();
                        filteredList.addAll(groupList);
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        Log.e("GroupFeed", "JSON parse error", e);
                    }
                },
                error -> Log.e("GroupFeed", "Volley error", error)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }


    private void filterGroups(String query) {
        filteredList.clear();
        if (query == null || query.isEmpty()) {
            filteredList.addAll(groupList);
        } else {
            for (GroupModel group : groupList) {
                if (group.getName().toLowerCase().contains(query.toLowerCase()) ||
                        group.getDescription().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(group);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}
