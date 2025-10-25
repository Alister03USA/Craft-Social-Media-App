package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity1 extends AppCompatActivity {

    private RecyclerView chatRecycler;
    private ChatAdapter adapter;
    private List<ChatMessage> chatMessages = new ArrayList<>();

    private EditText messageInput;
    private Button sendBtn;
    private TextView usernameHeader, connectionStatus;
    private ImageView backBtn;

    private String currentUser = "bob";
    private String chatPartner = "mary";

    private static final String TAG = "ChatActivity1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat1);

        chatRecycler = findViewById(R.id.chatRecycler);
        messageInput = findViewById(R.id.messageInput);
        sendBtn = findViewById(R.id.sendMessageBtn);
        usernameHeader = findViewById(R.id.usernameHeader);
        connectionStatus = findViewById(R.id.connectionStatus);
        backBtn = findViewById(R.id.backBtn);

        adapter = new ChatAdapter(chatMessages);
        chatRecycler.setLayoutManager(new LinearLayoutManager(this));
        chatRecycler.setAdapter(adapter);

        usernameHeader.setText(chatPartner);

        sendBtn.setOnClickListener(v -> {
            String msg = messageInput.getText().toString().trim();
            if (!msg.isEmpty()) {
                sendWebSocketMessage(msg);
                chatMessages.add(new ChatMessage(currentUser, msg, true));
                adapter.notifyItemInserted(chatMessages.size() - 1);
                chatRecycler.scrollToPosition(chatMessages.size() - 1);
                messageInput.setText("");
            }
        });

        backBtn.setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        });
    }

    private void sendWebSocketMessage(String msg) {
        try {
            JSONObject json = new JSONObject();
            json.put("sender", currentUser);
            json.put("receiver", chatPartner);
            json.put("body", msg);

            Intent i = new Intent("SendWebSocketMessage");
            i.putExtra("key", "chat1");
            i.putExtra("message", json.toString());
            LocalBroadcastManager.getInstance(this).sendBroadcast(i);
            Log.d(TAG, "Sent JSON message: " + json);
        } catch (Exception e) {
            Log.e(TAG, "Error sending message: " + e.getMessage());
        }
    }

    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String key = intent.getStringExtra("key");
            if ("chat1".equals(key) || "chat2".equals(key)) {
                String message = intent.getStringExtra("message");

                try {
                    String sender = "System";
                    String body = message;

                    // Check if message looks like JSON
                    if (message.trim().startsWith("{") && message.trim().endsWith("}")) {
                        JSONObject outer = new JSONObject(message);
                        sender = outer.optString("sender", sender);
                        body = outer.optString("body", body);

                        // ✅ unwrap nested JSON (actual chat content)
                        if (body.startsWith("{") && body.endsWith("}")) {
                            JSONObject inner = new JSONObject(body);
                            sender = inner.optString("sender", sender);
                            String receiver = inner.optString("receiver", "");
                            body = inner.optString("body", body);

                            // ✅ Only show message if it's for me or from me
                            if (receiver.equalsIgnoreCase(currentUser) || sender.equalsIgnoreCase(currentUser)) {

                                // ✅ Only show “System” for messages I RECEIVE, not ones I SEND
                                boolean isMine = sender.equalsIgnoreCase(currentUser);

                                if (!isMine) {
                                    // show "System" label for received messages
                                    chatMessages.add(new ChatMessage("System", sender + ": " + body, false));
                                } else {
                                    // show clean chat bubble for my messages only
                                    chatMessages.add(new ChatMessage(sender, body, true));
                                }

                                adapter.notifyItemInserted(chatMessages.size() - 1);
                                chatRecycler.scrollToPosition(chatMessages.size() - 1);
                                Log.d(TAG, "Displayed: " + sender + " → " + body);
                            }
                        }
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Failed to parse message safely: " + message, e);
                }
            }
        }
    };

    private final BroadcastReceiver statusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String key = intent.getStringExtra("key");
            if ("chat1".equals(key)) {
                boolean connected = intent.getBooleanExtra("connected", false);
                connectionStatus.setText(connected ? "🟢 Connected" : "🔴 Disconnected");
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                messageReceiver, new IntentFilter("WebSocketMessageReceived"));
        LocalBroadcastManager.getInstance(this).registerReceiver(
                statusReceiver, new IntentFilter("WebSocketStatus"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(statusReceiver);
    }
}