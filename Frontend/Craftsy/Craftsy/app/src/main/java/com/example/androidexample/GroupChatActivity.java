package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class GroupChatActivity extends BaseActivity {

    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";
    private String groupName;
    private GroupPostAdapter postAdapter;
    private final List<GroupPostModel> posts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        groupName = getIntent().getStringExtra("groupName");

        Toolbar toolbar = findViewById(R.id.groupToolbar);
        toolbar.setTitle(groupName);
        setSupportActionBar(toolbar);

        // Enable the toolbar back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        RecyclerView recyclerView = findViewById(R.id.chatRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postAdapter = new GroupPostAdapter(posts);
        recyclerView.setAdapter(postAdapter);

        FloatingActionButton addPostButton = findViewById(R.id.addPostButton);
        addPostButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateGroupPostActivity.class);
            intent.putExtra("groupName", groupName);
            startActivity(intent);
        });

        setupBottomNavigation(R.id.nav_group);
        loadGroupPosts();
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Return to previous activity
        onBackPressed();
        return true;
    }

    private void loadGroupPosts() {
        // TODO: implement backend fetch here
    }
}
