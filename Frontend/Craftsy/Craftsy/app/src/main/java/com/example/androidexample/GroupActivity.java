package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class GroupActivity extends AppCompatActivity {

    private RecyclerView groupRecyclerView;
    private FloatingActionButton groupAddPostButton;
    private GroupPostAdapter postAdapter;
    private final List<GroupPostModel> posts = new ArrayList<>();
    private static final int CREATE_POST_REQUEST = 101;

    private long groupId;
    private String groupName;
    private String username;

    private WebSocketManager wsManager;
    private boolean wsInitialized = false; // ✅ Prevent duplicate connections

    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        groupRecyclerView = findViewById(R.id.groupRecyclerView);
        groupAddPostButton = findViewById(R.id.groupAddPostButton);

        groupRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        postAdapter = new GroupPostAdapter(posts);
        groupRecyclerView.setAdapter(postAdapter);

        groupName = getIntent().getStringExtra("groupName");
        username = SessionManager.getInstance().getLoggedInUsername();

        if (username == null) {
            Log.w("GroupActivity", "No logged-in user found. Finishing.");
            finish();
            return;
        }

        findViewById(R.id.groupBackBtn).setOnClickListener(v -> finish());

        fetchGroupId();

        groupAddPostButton.setOnClickListener(v -> {
            Intent i = new Intent(this, CreateGroupPostActivity.class);
            i.putExtra("groupId", groupId);
            i.putExtra("groupName", groupName);
            startActivityForResult(i, CREATE_POST_REQUEST);

        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() { finish(); }
        });
    }

    private void fetchGroupId() {
        if (wsInitialized) return; // ✅ Stop reconnects when returning to this screen

        String url = BASE_URL + "/groupId/" + groupName;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        groupId = response.getLong("groupId");
                        Log.d("GroupActivity", "Group ID: " + groupId);

                        if (!wsInitialized) {
                            initWebSocket();
                            wsInitialized = true; // ✅ Mark WebSocket as connected once
                        }

                        loadPosts();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.e("GroupActivity", "Failed to get groupId: " + error.getMessage())
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void initWebSocket() {
        String wsUrl = "ws://coms-3090-028.class.las.iastate.edu:8080/ws/groupMessage/"
                + groupId + "/" + username;

        wsManager = WebSocketManager.getInstance();
        wsManager.connect(wsUrl);

        wsManager.setListener(new WebSocketManager.WebSocketListener() {

            @Override
            public void onMessage(String message) {
                runOnUiThread(() -> {
                    try {
                        int separatorIndex = message.indexOf(": ");
                        if (separatorIndex > 0) {
                            String sender = message.substring(0, separatorIndex).trim();
                            String content = message.substring(separatorIndex + 2).trim();

                            String mediaUrl = null;
                            Long messageId = null;

                            // Handle image messages with messageId broadcast
                            if (content.startsWith("uploaded an image:")) {
                                // Expecting format: "uploaded an image:{messageId}:{filename}"
                                String[] parts = content.substring("uploaded an image:".length()).split(":", 2);
                                if (parts.length == 2) {
                                    messageId = Long.parseLong(parts[0].trim());
                                    String fileName = parts[1].trim();
                                    mediaUrl = BASE_URL + "/groupMessage/image/" + messageId;
                                    content = null; // no text to display
                                }
                            }

                            posts.add(new GroupPostModel(sender, content, messageId, mediaUrl));
                            postAdapter.notifyItemInserted(posts.size() - 1);
                            groupRecyclerView.scrollToPosition(posts.size() - 1);
                        } else {
                            Log.w("WebSocketParse", "Unexpected format: " + message);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onOpen() {
                Log.d("WebSocket", "Connected");
            }

            @Override
            public void onClose(String reason) {
                Log.d("WebSocket", "Closed: " + reason);
            }

            @Override
            public void onError(Exception ex) {
                Log.e("WebSocket", "Error", ex);
            }
        });
    }


    private void loadPosts() {
        String url = BASE_URL + "/groupMessage/" + username + "/" + groupId + "/history";
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    posts.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            JSONObject senderObj = obj.getJSONObject("sender");
                            String sender = senderObj.getString("username");

                            String content = obj.optString("message", null);
                            Long messageId = obj.has("id") ? obj.getLong("id") : null;
                            String mediaUrl = obj.optString("mediaUrl", null);

                            posts.add(new GroupPostModel(sender, content, messageId, mediaUrl));

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    postAdapter.notifyDataSetChanged();
                    if (!posts.isEmpty())
                        groupRecyclerView.scrollToPosition(posts.size() - 1);
                },
                error -> Log.e("GroupActivity", "Failed to load posts", error)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CREATE_POST_REQUEST && resultCode == RESULT_OK && data != null) {
            if (data.hasExtra("textMessage")) {
                String message = data.getStringExtra("textMessage");
                if (wsManager != null) {
                    wsManager.sendMessage(message);
                }
            }
            // images already handled separately by adapter
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wsManager != null) {
            wsManager.disconnect();
            wsInitialized = false;
        }
    }

}

