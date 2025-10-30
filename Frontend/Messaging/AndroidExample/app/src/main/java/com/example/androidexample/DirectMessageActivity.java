package com.example.androidexample;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class DirectMessageActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private MessageAdapter adapter;
    private final List<MessageItem> messages = new ArrayList<>();
    private WebSocketClient wsClient;
    private EditText input;
    private String username = "Fuji";
    private String convoId;
    private String otherUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direct_message);

        recycler = findViewById(R.id.messageRecycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessageAdapter(messages, username);
        recycler.setAdapter(adapter);

        convoId = getIntent().getStringExtra("convoId");
        otherUser = getIntent().getStringExtra("otherUser");

        ((android.widget.TextView) findViewById(R.id.chatTitle)).setText(otherUser);

        input = findViewById(R.id.inputMessage);
        ImageButton send = findViewById(R.id.btnSend);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        connectSocket();

        send.setOnClickListener(v -> {
            String text = input.getText().toString().trim();
            if (!text.isEmpty() && wsClient != null && wsClient.isOpen()) {
                wsClient.send(text);
                messages.add(new MessageItem(username, text, true));
                adapter.notifyItemInserted(messages.size() - 1);
                recycler.scrollToPosition(messages.size() - 1);
                input.setText("");
            }
        });
    }

    private void connectSocket() {
        try {
            String socketUrl = "ws://coms-3090-028.class.las.iastate.edu:8080/chat/" + convoId + "/" + username;
            wsClient = new WebSocketClient(new URI(socketUrl)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("✅ WebSocket connected");
                }

                @Override
                public void onMessage(String message) {
                    runOnUiThread(() -> {
                        messages.add(new MessageItem(otherUser, message, false));
                        adapter.notifyItemInserted(messages.size() - 1);
                        recycler.scrollToPosition(messages.size() - 1);
                    });
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("⚠️ WebSocket closed: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                }
            };
            wsClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wsClient != null) {
            try {
                wsClient.close();
            } catch (Exception ignored) {}
        }
    }
}