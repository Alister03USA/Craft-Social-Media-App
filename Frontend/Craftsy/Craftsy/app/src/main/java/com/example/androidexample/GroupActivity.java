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

/**
 * Activity responsible for displaying the posts in a specific group.
 *
 * This Activity handles:
 *
 *     Loading the group ID from the backend
 *     Fetching and displaying previous posts
 *     Opening WebSocket connections for real-time messages
 *     Allowing users to create new posts using another Activity
 * @author Quinn Weidenaar
 */
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
    private boolean wsInitialized = false;

    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";

    /**
     * Initializes the UI, loads passed data, sets listeners, and triggers retrieval
     * of the group ID followed by loading posts and connecting to WebSocket.
     *
     * @param savedInstanceState saved instance state bundle if activity is recreated
     */
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
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    /**
     * Sends a request to the backend to retrieve the group's ID based on the group name,
     * initializes the WebSocket once the ID is received, and loads all existing posts.
     */
    private void fetchGroupId() {
        if (wsInitialized) return;

        String url = BASE_URL + "/groupId/" + groupName;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        groupId = response.getLong("groupId");
                        Log.d("GroupActivity", "Group ID: " + groupId);

                        if (!wsInitialized) {
                            initWebSocket();
                            wsInitialized = true;
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

    /**
     * Initializes the WebSocket for real-time group messages and defines the listener
     * for incoming events such as message receiving, errors, and connection updates.
     */
    private void initWebSocket() {

        String wsUrl = "ws://coms-3090-028.class.las.iastate.edu:8080/ws/groupMessage/"
                + groupId + "/" + username;

        wsManager = WebSocketManager.getInstance();
        wsManager.connect(wsUrl);

        wsManager.setListener(new WebSocketManager.WebSocketListener() {

            /**
             * Called when a new WebSocket message arrives.
             * Parses the message, extracts media if present, and updates the UI list.
             *
             * @param message the incoming message sent through WebSocket
             */
            @Override
            public void onMessage(String message) {
                runOnUiThread(() -> {
                    try {
                        if (message.startsWith("REPLY:")) return;

                        int separatorIndex = message.indexOf(": ");
                        if (separatorIndex > 0) {
                            String sender = message.substring(0, separatorIndex).trim();
                            String content = message.substring(separatorIndex + 2).trim();

                            String mediaUrl = null;
                            Long messageId = null;

                            while (content.contains("[image:")) {
                                int start = content.indexOf("[image:") + 7;
                                int end = content.indexOf("]", start);
                                if (end > start) {
                                    String idStr = content.substring(start, end);
                                    messageId = Long.parseLong(idStr);
                                    mediaUrl = BASE_URL + "/groupMessage/image/" + messageId;
                                    content = content.replace("[image:" + messageId + "]", "").trim();
                                } else break;
                            }

                            if (content == null || content.isEmpty() ||
                                    content.matches(".*uploaded an image.*") ||
                                    content.matches(".*Image sent.*"))
                                return;

                            posts.add(new GroupPostModel(
                                    sender,
                                    content.isEmpty() ? null : content,
                                    messageId,
                                    mediaUrl,
                                    groupId
                            ));

                            postAdapter.notifyItemInserted(posts.size() - 1);
                            groupRecyclerView.scrollToPosition(posts.size() - 1);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            /**
             * Invoked when WebSocket connection is opened.
             */
            @Override
            public void onOpen() {
                Log.d("WebSocket", "Connected");
            }

            /**
             * Invoked when WebSocket connection is closed.
             *
             * @param reason explanation for disconnection
             */
            @Override
            public void onClose(String reason) {
                Log.d("WebSocket", "Closed: " + reason);
            }

            /**
             * Invoked when WebSocket encounters an error.
             *
             * @param ex the exception raised during WebSocket operations
             */
            @Override
            public void onError(Exception ex) {
                Log.e("WebSocket", "Error", ex);
            }
        });
    }

    /**
     * Fetches all previous posts for the group using a REST API call.
     * Filters out replies, parses media attachments, and populates the post list.
     */
    private void loadPosts() {
        String url = BASE_URL + "/groupMessage/" + username + "/" + groupId + "/history";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    posts.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);

                            JSONObject replyTo = obj.optJSONObject("replyToMessage");
                            if (replyTo != null) continue;

                            JSONObject senderObj = obj.getJSONObject("sender");
                            String sender = senderObj.getString("username");

                            String content = obj.optString("message", "");
                            Long messageId = null;
                            String mediaUrl = null;

                            while (content.contains("[image:")) {
                                int start = content.indexOf("[image:") + 7;
                                int end = content.indexOf("]", start);
                                if (end > start) {
                                    String idStr = content.substring(start, end);
                                    messageId = Long.parseLong(idStr);
                                    mediaUrl = BASE_URL + "/groupMessage/image/" + messageId;
                                    content = content.replace("[image:" + messageId + "]", "").trim();
                                } else break;
                            }

                            if ((content == null || content.isEmpty()) && mediaUrl == null) continue;
                            if (mediaUrl == null && (content.matches(".*Image sent.*")
                                    || content.matches(".*uploaded an image.*"))) continue;

                            posts.add(new GroupPostModel(
                                    sender,
                                    content.isEmpty() ? null : content,
                                    messageId,
                                    mediaUrl,
                                    groupId
                            ));

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

    /**
     * Receives the result of the CreateGroupPostActivity.
     * If text message is returned, it is forwarded through the WebSocket.
     *
     * @param requestCode the code identifying the request
     * @param resultCode  the result code returned by the child Activity
     * @param data        additional data sent back from the Activity
     */
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
        }
    }

    /**
     * Cleans up the WebSocket connection when the Activity is destroyed.
     * Prevents memory leaks and reconnection attempts when navigating back.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wsManager != null) {
            wsManager.disconnect();
            wsInitialized = false;
        }
    }
}
