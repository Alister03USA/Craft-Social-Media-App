package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private Button connectBtn1, connectBtn2, backBtn1, backBtn2;
    private EditText serverEtx1, usernameEtx1, serverEtx2, usernameEtx2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Stop any lingering WebSocket connections when reopening app
        stopService(new Intent(this, WebSocketService.class));

        connectBtn1 = findViewById(R.id.connectBtn);
        connectBtn2 = findViewById(R.id.connectBtn2);
        backBtn1 = findViewById(R.id.backBtn);
        backBtn2 = findViewById(R.id.backBtn2);
        serverEtx1 = findViewById(R.id.serverEdt);
        usernameEtx1 = findViewById(R.id.unameEdt);
        serverEtx2 = findViewById(R.id.serverEdt2);
        usernameEtx2 = findViewById(R.id.unameEdt2);

        // Chat 1 (left user)
        connectBtn1.setOnClickListener(view -> {
            String serverUrl = serverEtx1.getText().toString().trim();
            String username = usernameEtx1.getText().toString().trim();
            if (serverUrl.isEmpty() || username.isEmpty()) {
                usernameEtx1.setError("Enter username and server URL");
                return;
            }

            String fullUrl = serverUrl + username;
            stopService(new Intent(this, WebSocketService.class));

            Intent serviceIntent = new Intent(this, WebSocketService.class);
            serviceIntent.setAction("CONNECT");
            serviceIntent.putExtra("key", "chat1");
            serviceIntent.putExtra("url", fullUrl);
            startService(serviceIntent);

            Intent intent = new Intent(this, ChatActivity1.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        // Chat 2 (right user)
        connectBtn2.setOnClickListener(view -> {
            String serverUrl = serverEtx2.getText().toString().trim();
            String username = usernameEtx2.getText().toString().trim();
            if (serverUrl.isEmpty() || username.isEmpty()) {
                usernameEtx2.setError("Enter username and server URL");
                return;
            }

            String fullUrl = serverUrl + username;
            stopService(new Intent(this, WebSocketService.class));

            Intent serviceIntent = new Intent(this, WebSocketService.class);
            serviceIntent.setAction("CONNECT");
            serviceIntent.putExtra("key", "chat2");
            serviceIntent.putExtra("url", fullUrl);
            startService(serviceIntent);

            Intent intent = new Intent(this, ChatActivity2.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        backBtn1.setOnClickListener(v -> startActivity(new Intent(this, ChatActivity1.class)));
        backBtn2.setOnClickListener(v -> startActivity(new Intent(this, ChatActivity2.class)));
    }
}