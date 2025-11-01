package com.example.androidexample;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads a full conversation + messages via:
 *   GET /messages/{convoId}  (returns Conversation JSON object with "messages":[...])
 * Live updates over:
 *   ws://coms-3090-028.class.las.iastate.edu:8080/chat/{convoId}/{username}
 */
public class DirectMessagingActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";
    private static final String WS_ROOT  = "ws://coms-3090-028.class.las.iastate.edu:8080/chat/";
    private static final String CURRENT_USER = "Fuji"; // TODO: bind to your auth/session

    private String convoId;
    private String chatName;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private EditText inputMessage;
    private ImageButton sendButton;

    private MessageAdapter adapter;
    private final List<MessageItem> messages = new ArrayList<>();

    private WebSocketClient socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direct_message);

        convoId  = getIntent().getStringExtra("convoId");
        chatName = getIntent().getStringExtra("chatName");
        if (chatName != null) setTitle(chatName);

        recyclerView  = findViewById(R.id.recyclerMessages);
        progressBar   = findViewById(R.id.progressBar);
        inputMessage  = findViewById(R.id.inputMessage);
        sendButton    = findViewById(R.id.sendButton);

        adapter = new MessageAdapter(messages, CURRENT_USER);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fetchConversation();
        connectSocket();

        sendButton.setOnClickListener(v -> {
            String txt = inputMessage.getText().toString().trim();
            if (txt.isEmpty()) return;
            sendOverSocket(txt);
            inputMessage.setText("");
        });
    }

    private void fetchConversation() {
        progressBar.setVisibility(View.VISIBLE);
        String url = BASE_URL + "/messages/" + convoId;
        Log.d("DirectMessage", "Requesting: " + url);

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                resp -> {
                    progressBar.setVisibility(View.GONE);
                    parseConversation(resp);
                },
                err -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("DirectMessage", "Volley error", err);
                    Toast.makeText(this, "Failed to load messages", Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void parseConversation(JSONObject convo) {
        try {
            messages.clear();
            JSONArray arr = convo.optJSONArray("messages");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject m = arr.getJSONObject(i);
                    String sender = m.optString("sender", "");
                    String text   = m.optString("text", "");
                    String ts     = m.optString("date", "");
                    messages.add(new MessageItem(sender, text, ts));
                }
            }
            adapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(Math.max(messages.size() - 1, 0));
        } catch (JSONException e) {
            Log.e("DirectMessage", "Parse error", e);
        }
    }

    private void connectSocket() {
        String ws = WS_ROOT + convoId + "/" + CURRENT_USER;
        try {
            socket = new WebSocketClient(new URI(ws)) {
                @Override public void onOpen(ServerHandshake handshakedata) {
                    Log.d("WebSocket", "Connected");
                }
                @Override public void onMessage(String message) {
                    // Backend currently echoes "username: text" or broadcasts events.
                    runOnUiThread(() -> {
                        String sender = "server";
                        String text   = message;
                        int idx = message.indexOf(": ");
                        if (idx > 0) {
                            sender = message.substring(0, idx);
                            text   = message.substring(idx + 2);
                        }
                        messages.add(new MessageItem(sender, text, "now"));
                        adapter.notifyItemInserted(messages.size() - 1);
                        recyclerView.scrollToPosition(messages.size() - 1);
                    });
                }
                @Override public void onClose(int code, String reason, boolean remote) {
                    Log.d("WebSocket", "Closed: " + reason);
                }
                @Override public void onError(Exception ex) {
                    Log.e("WebSocket", "Error", ex);
                }
            };
            socket.connect();
        } catch (Exception e) {
            Log.e("WebSocket", "Invalid WS URL: " + ws, e);
        }
    }

    private void sendOverSocket(String text) {
        if (socket != null && socket.isOpen()) {
            socket.send(text);
            messages.add(new MessageItem(CURRENT_USER, text, "now"));
            adapter.notifyItemInserted(messages.size() - 1);
            recyclerView.scrollToPosition(messages.size() - 1);
        } else {
            Toast.makeText(this, "Socket not connected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (socket != null) socket.close();
    }
}