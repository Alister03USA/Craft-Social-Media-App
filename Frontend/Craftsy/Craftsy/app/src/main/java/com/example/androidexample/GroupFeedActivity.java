package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
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

        // ✅ Setup bottom navigation (must match BaseActivity)
        setupBottomNavigation(R.id.nav_group);

        // ✅ Setup RecyclerView
        recyclerView = findViewById(R.id.groupRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new GroupAdapter(filteredList, group -> {
            Intent intent = new Intent(GroupFeedActivity.this, GroupChatActivity.class);
            intent.putExtra("groupName", group.getName());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // ✅ Setup Search
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

        // ✅ Fetch user’s groups from backend
        fetchUserGroups();
    }

    private void fetchUserGroups() {
        String username = SessionManager.getInstance().getLoggedInUsername();
        String url = BASE_URL + "/user/" + username;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    groupList.clear();
                    try {
                        JSONArray groupsArray = response.getJSONArray("groups");
                        for (int i = 0; i < groupsArray.length(); i++) {
                            JSONObject obj = groupsArray.getJSONObject(i);
                            groupList.add(new GroupModel(
                                    obj.optString("groupName"),
                                    obj.optString("description")
                            ));
                        }

                        filteredList.clear();
                        filteredList.addAll(groupList);
                        adapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> error.printStackTrace()
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
