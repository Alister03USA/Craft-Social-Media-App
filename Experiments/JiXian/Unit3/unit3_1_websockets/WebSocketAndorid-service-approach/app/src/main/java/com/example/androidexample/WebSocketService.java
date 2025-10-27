package com.example.androidexample;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class WebSocketService extends Service {

    private static final String TAG = "WebSocketService";
    private final Map<String, WebSocketClient> sockets = new HashMap<>();
    private final Map<String, Boolean> connectionStatus = new HashMap<>();
    private final Handler handler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created and broadcast receiver registered.");
        registerReceiver(receiver, new IntentFilter("SEND_MESSAGE"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed and all WebSockets closed.");
        unregisterReceiver(receiver);
        for (WebSocketClient socket : sockets.values()) {
            try { socket.close(); } catch (Exception ignored) {}
        }
        sockets.clear();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Receiver for sending messages
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String key = intent.getStringExtra("key");
            String message = intent.getStringExtra("message");

            WebSocketClient socket = sockets.get(key);
            Boolean connected = connectionStatus.get(key);

            if (socket != null && connected != null && connected) {
                socket.send(message);
                Log.d(TAG, key + " → Sent JSON: " + message);
            } else {
                Log.w(TAG, key + " → Socket not open, reconnecting...");
                reconnect(key);
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "CONNECT".equals(intent.getAction())) {
            String key = intent.getStringExtra("key");
            String url = intent.getStringExtra("url");
            connectWebSocket(key, url);
        }
        return START_STICKY;
    }

    private void connectWebSocket(String key, String url) {
        try {
            URI uri = new URI(url);
            Log.d(TAG, "Attempting connection to: " + url);

            WebSocketClient socket = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    Log.d(TAG, key + " → Connected");
                    connectionStatus.put(key, true);
                    broadcastStatus(key, true);
                }

                @Override
                public void onMessage(String message) {
                    Log.d(TAG, key + " → Received raw: " + message);

                    // Handle "Username already exists"
                    if (message.contains("Username already exists")) {
                        Log.d(TAG, key + " → Username already exists, forcing reconnect...");
                        connectionStatus.put(key, false);
                        close();
                        handler.postDelayed(() -> WebSocketService.this.reconnect(key), 1500);
                        return;
                    }

                    Intent i = new Intent("WEBSOCKET_MESSAGE");
                    i.putExtra("key", key);
                    i.putExtra("message", message);
                    sendBroadcast(i);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, key + " → Closed (" + reason + ")");
                    connectionStatus.put(key, false);
                    broadcastStatus(key, false);
                    handler.postDelayed(() -> WebSocketService.this.reconnect(key), 1500);
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, key + " → Error: " + ex.getMessage());
                    connectionStatus.put(key, false);
                    broadcastStatus(key, false);
                    handler.postDelayed(() -> WebSocketService.this.reconnect(key), 1500);
                }
            };

            sockets.put(key, socket);
            connectionStatus.put(key, false);
            socket.connect();

        } catch (Exception e) {
            Log.e(TAG, "Connection failed: " + e.getMessage());
            handler.postDelayed(() -> reconnect(key), 2000);
        }
    }

    private void reconnect(String key) {
        WebSocketClient oldSocket = sockets.get(key);
        if (oldSocket != null) {
            try { oldSocket.close(); } catch (Exception ignored) {}
        }

        // Find the last used URL
        String lastUrl = null;
        if (oldSocket != null && oldSocket.getURI() != null) {
            lastUrl = oldSocket.getURI().toString();
        }

        if (lastUrl != null) {
            Log.d(TAG, key + " → Reconnecting...");
            connectWebSocket(key, lastUrl);
        }
    }

    private void broadcastStatus(String key, boolean connected) {
        Intent i = new Intent("WebSocketStatus");
        i.putExtra("key", key);
        i.putExtra("connected", connected);
        sendBroadcast(i);
    }
}