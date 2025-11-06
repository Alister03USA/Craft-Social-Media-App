package com.example.androidexample;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class CommentsActivity extends AppCompatActivity {

    private RecyclerView commentRecycler;
    private EditText commentInput;
    private ImageButton sendBtn;
    private CommentAdapter adapter;
    private List<CommentModel> comments = new ArrayList<>();

    private long messageId;
    private long groupId;
    private String username;

    private WebSocketManager wsManager;
    private boolean wsInitialized = false;

    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        messageId = getIntent().getLongExtra("messageId", -1);
        groupId = getIntent().getLongExtra("groupId", -1);
        username = SessionManager.getInstance().getLoggedInUsername();

        if (username == null || messageId == -1 || groupId == -1) {
            Log.e("CommentsActivity", "Invalid parameters. Closing.");
            finish();
            return;
        }

        commentRecycler = findViewById(R.id.commentRecycler);
        commentInput = findViewById(R.id.commentInput);
        sendBtn = findViewById(R.id.commentSendBtn);

        adapter = new CommentAdapter(comments);
        commentRecycler.setLayoutManager(new LinearLayoutManager(this));
        commentRecycler.setAdapter(adapter);

        loadComments();

        if (!wsInitialized) initWebSocket();

        sendBtn.setOnClickListener(v -> addComment());
    }

    private void loadComments() {
        String url = BASE_URL + "/groupMessage/" + messageId + "/comments";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    comments.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject c = response.getJSONObject(i);
                            JSONObject sender = c.getJSONObject("sender");
                            comments.add(new CommentModel(
                                    sender.getString("username"),
                                    c.getString("comment")
                            ));
                        } catch (Exception ignore) {}
                    }
                    adapter.notifyDataSetChanged();
                    if (!comments.isEmpty())
                        commentRecycler.scrollToPosition(comments.size() - 1);
                },
                error -> Log.e("CommentsActivity", "Failed to load comments", error)
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

                            if (message.contains("[replying to @")) {
                                // Optional: extract the username being replied to
                                int start = message.indexOf("[replying to @");
                                int end = message.indexOf("]", start);
                                String replyToUsername = message.substring(start + 13, end); // just the username
                                String contentOnly = message.substring(0, start).trim();

                                // add as comment if replying to the current messageId
                                // unfortunately backend doesn’t include the ID, so you may need to match by sender+content
                                comments.add(new CommentModel(sender, contentOnly));
                                adapter.notifyItemInserted(comments.size() - 1);
                                commentRecycler.scrollToPosition(comments.size() - 1);
                            }

                        } else {
                            Log.w("WebSocketParse", "Unexpected format: " + message);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onOpen() { Log.d("WebSocket", "Connected to comments WS"); }

            @Override
            public void onClose(String reason) { Log.d("WebSocket", "Comments WS closed: " + reason); }

            @Override
            public void onError(Exception ex) { Log.e("WebSocket", "Comments WS error", ex); }
        });

        wsInitialized = true;
    }

    private void addComment() {
        String text = commentInput.getText().toString().trim();
        if (text.isEmpty()) return;

        String url = BASE_URL + "/comment/" + username + "/" + messageId + "/create";
        JSONObject body = new JSONObject();
        try { body.put("comment", text); } catch (JSONException ignored) {}

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
                response -> commentInput.setText(""),
                error -> Log.e("CommentsActivity", "Failed to add comment", error)
        );
        VolleySingleton.getInstance(this).addToRequestQueue(request);

        if (wsManager != null) wsManager.sendMessage("REPLY:" + messageId + ":" + text);

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
