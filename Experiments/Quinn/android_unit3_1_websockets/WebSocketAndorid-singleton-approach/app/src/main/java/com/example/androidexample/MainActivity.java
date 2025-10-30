package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    private Button connectBtn, connectBtn2, backBtn, backBtn2;
    private Button deleteChat1Btn, deleteChat2Btn;
    private EditText serverEtx, usernameEtx, serverEtx2, usernameEtx2;
    private LinearLayout chat1Layout, chat2Layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        connectBtn = findViewById(R.id.connectBtn);
        connectBtn2 = findViewById(R.id.connectBtn2);
        backBtn = findViewById(R.id.backBtn);
        backBtn2 = findViewById(R.id.backBtn2);
        serverEtx = findViewById(R.id.serverEdt);
        usernameEtx = findViewById(R.id.unameEdt);
        serverEtx2 = findViewById(R.id.serverEdt2);
        usernameEtx2 = findViewById(R.id.unameEdt2);

        chat1Layout = findViewById(R.id.chat1Layout);
        chat2Layout = findViewById(R.id.chat2Layout);
        deleteChat1Btn = findViewById(R.id.deleteChat1Btn);
        deleteChat2Btn = findViewById(R.id.deleteChat2Btn);

        // Delete Chat Listeners
        deleteChat1Btn.setOnClickListener(v -> {
            chat1Layout.setVisibility(View.GONE);
            serverEtx.setText("");
            usernameEtx.setText("");
        });

        deleteChat2Btn.setOnClickListener(v -> {
            chat2Layout.setVisibility(View.GONE);
            serverEtx2.setText("");
            usernameEtx2.setText("");
        });

        // Connect button listeners
        connectBtn.setOnClickListener(view -> {
            String serverUrl = "ws://10.0.2.2:9090";
            if (!serverUrl.isEmpty()) {
                WebSocketManager1.getInstance().connectWebSocket(serverUrl);
                startActivity(new Intent(this, ChatActivity1.class));
            }
        });

        connectBtn2.setOnClickListener(view -> {
            String serverUrl = "ws://10.0.2.2:9090";
            if (!serverUrl.isEmpty()) {
                WebSocketManager2.getInstance().connectWebSocket(serverUrl);
                startActivity(new Intent(this, ChatActivity2.class));
            }
        });

        // Back button listeners
        backBtn.setOnClickListener(view -> startActivity(new Intent(this, ChatActivity1.class)));
        backBtn2.setOnClickListener(view -> startActivity(new Intent(this, ChatActivity2.class)));
    }
}
