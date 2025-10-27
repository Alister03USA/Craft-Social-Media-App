package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity1 extends AppCompatActivity {

    private RecyclerView chatRecycler;
    private ChatAdapter adapter;
    private final List<ChatMessage> chatMessages = new ArrayList<>();

    private EditText messageInput;
    private Button sendBtn;
    private TextView connectionStatus;
    private ImageView backBtn;
    private String username;

    private static final String TAG = "ChatActivity1";
    private static final String PREF_NAME = "ChatHistory1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat1);

        chatRecycler = findViewById(R.id.chatRecycler);
        messageInput = findViewById(R.id.messageInput);
        sendBtn = findViewById(R.id.sendMessageBtn);
        connectionStatus = findViewById(R.id.connectionStatus);
        backBtn = findViewById(R.id.backBtn);

        username = getIntent().getStringExtra("username");
        if (username == null) username = "User";

        adapter = new ChatAdapter(chatMessages);
        chatRecycler.setLayoutManager(new LinearLayoutManager(this));
        chatRecycler.setAdapter(adapter);

        // Load saved chat immediately when activity opens
        loadChatHistory();

        sendBtn.setOnClickListener(v -> {
            String msg = messageInput.getText().toString().trim();
            if (!msg.isEmpty()) {
                sendWebSocketMessage(msg);
                addMessageToList("Me", msg, true);
                messageInput.setText("");
                saveChatHistory();
            }
        });

        backBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void sendWebSocketMessage(String msg) {
        try {
            JSONObject json = new JSONObject();
            json.put("sender", username);
            json.put("body", msg);

            Intent i = new Intent("SEND_MESSAGE");
            i.putExtra("key", "chat1");
            i.putExtra("message", json.toString());
            sendBroadcast(i);
            Log.d(TAG, "Sent JSON message: " + json);
        } catch (Exception e) {
            Log.e(TAG, "Error sending message: " + e.getMessage());
        }
    }

    // Receiver for incoming chat messages
    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!"chat1".equals(intent.getStringExtra("key"))) return;

            try {
                String raw = intent.getStringExtra("message");
                Log.d(TAG, "Raw received: " + raw);

                String senderPrefix = raw.contains(":") ? raw.substring(0, raw.indexOf(":")).trim() : "Server";
                String jsonPart = raw.contains(":") ? raw.substring(raw.indexOf(":") + 1).trim() : raw;

                JSONObject json = tryParseJSON(jsonPart);
                String bodyRaw = json.optString("body", jsonPart);
                JSONObject innerBody = tryParseJSON(bodyRaw);
                String messageBody = innerBody.optString("body", bodyRaw);

                if (messageBody.toLowerCase().contains("welcome")
                        || messageBody.toLowerCase().contains("joined")
                        || messageBody.toLowerCase().contains("connected")
                        || messageBody.toLowerCase().contains("disconnected"))
                    return;

                if (!senderPrefix.equals(username)) {
                    addMessageToList(senderPrefix, messageBody, false);
                    saveChatHistory();
                    Log.d(TAG, "Displayed message from " + senderPrefix + ": " + messageBody);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error handling message: ", e);
            }
        }
    };

    // Parse safely into JSON
    private JSONObject tryParseJSON(String data) {
        try {
            if (data != null && data.trim().startsWith("{") && data.trim().endsWith("}"))
                return new JSONObject(data);
        } catch (Exception ignored) {}
        return new JSONObject();
    }

    // Receiver for connection updates
    private final BroadcastReceiver statusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("chat1".equals(intent.getStringExtra("key"))) {
                boolean connected = intent.getBooleanExtra("connected", false);
                connectionStatus.setText(connected ? "🟢 Connected" : "🔴 Disconnected");

                // Reload saved messages after reconnect
                if (connected) {
                    Log.d(TAG, "Reconnected — reloading chat history");
                    loadChatHistory();
                }
            }
        }
    };

    private void addMessageToList(String sender, String message, boolean isSent) {
        chatMessages.add(new ChatMessage(sender, message, isSent));
        adapter.notifyItemInserted(chatMessages.size() - 1);
        chatRecycler.scrollToPosition(chatMessages.size() - 1);
    }

    private void saveChatHistory() {
        try {
            JSONArray jsonArray = new JSONArray();
            for (ChatMessage msg : chatMessages) {
                JSONObject obj = new JSONObject();
                obj.put("sender", msg.getSender());
                obj.put("message", msg.getMessage());
                obj.put("isSent", msg.isSent());
                jsonArray.put(obj);
            }
            SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            prefs.edit().putString("messages", jsonArray.toString()).apply();
        } catch (Exception e) {
            Log.e(TAG, "Failed to save chat history", e);
        }
    }

    private void loadChatHistory() {
        try {
            SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            String data = prefs.getString("messages", null);
            chatMessages.clear();
            if (data == null) return;

            JSONArray jsonArray = new JSONArray(data);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                chatMessages.add(new ChatMessage(
                        obj.getString("sender"),
                        obj.getString("message"),
                        obj.getBoolean("isSent")
                ));
            }
            adapter.notifyDataSetChanged();
            chatRecycler.scrollToPosition(chatMessages.size() - 1);
        } catch (Exception e) {
            Log.e(TAG, "Failed to load chat history", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(messageReceiver, new IntentFilter("WEBSOCKET_MESSAGE"));
        registerReceiver(statusReceiver, new IntentFilter("WebSocketStatus"));
        loadChatHistory(); // always refresh chat when returning
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(messageReceiver);
        unregisterReceiver(statusReceiver);
        saveChatHistory();
    }
}