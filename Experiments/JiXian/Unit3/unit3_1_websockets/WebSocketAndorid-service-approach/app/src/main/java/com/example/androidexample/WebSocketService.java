package com.example.androidexample;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocketService
 * ----------------
 * Demonstrates maintaining multiple active WebSocket connections
 * using an Android background Service. It handles:
 *   • Lifecycle (connect, reconnect, close)
 *   • Incoming/outgoing message flow via LocalBroadcastManager
 *   • JSON message parsing
 *   • Broadcasting connection status for UI feedback
 */
public class WebSocketService extends Service {

    private static final String TAG = "WebSocketService";
    private final Map<String, WebSocketClient> webSockets = new HashMap<>();
    private static final long RECONNECT_DELAY = 4000;

    public WebSocketService() {}

    // -----------------------------------------------------------------------
    // Lifecycle
    // -----------------------------------------------------------------------

    @Override
    public void onCreate() {
        super.onCreate();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(messageReceiver, new IntentFilter("SendWebSocketMessage"));
        Log.d(TAG, "Service created and broadcast receiver registered.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if ("CONNECT".equals(action)) {
                String url = intent.getStringExtra("url");
                String key = intent.getStringExtra("key");
                connectWebSocket(key, url);
            } else if ("DISCONNECT".equals(action)) {
                String key = intent.getStringExtra("key");
                disconnectWebSocket(key);
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (WebSocketClient client : webSockets.values()) {
            try { client.close(); } catch (Exception ignored) {}
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
        Log.d(TAG, "Service destroyed and all WebSockets closed.");
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    // -----------------------------------------------------------------------
    // Connection logic
    // -----------------------------------------------------------------------

    private void connectWebSocket(String key, String url) {
        try {
            URI serverUri = URI.create(url);
            Log.d(TAG, "Attempting connection to: " + url);

            WebSocketClient client = new WebSocketClient(serverUri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    Log.d(TAG, key + " → Connected");
                    broadcastStatus(key, true);
                    sendSystemMessage(key, "🟢 Connected to server");
                }

                @Override
                public void onMessage(String message) {
                    Log.d(TAG, key + " → Received raw: " + message);

                    String formattedMessage = message;

                    // Try to format any JSON-looking message
                    try {
                        if (message.trim().startsWith("{") && message.trim().endsWith("}")) {
                            JSONObject json = new JSONObject(message);
                            String sender = json.optString("sender", "Unknown");
                            String body = json.optString("body", message);
                            formattedMessage = sender + ": " + body;
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Message is not valid JSON: " + e.getMessage());
                    }

                    // Broadcast cleaned message
                    Intent intent = new Intent("WebSocketMessageReceived");
                    intent.putExtra("key", key);
                    intent.putExtra("message", formattedMessage);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, key + " → Closed (" + reason + ")");
                    broadcastStatus(key, false);
                    sendSystemMessage(key, "🔴 Disconnected: " + reason);

                    // Attempt auto-reconnect
                    new Handler(getMainLooper()).postDelayed(() -> {
                        Log.d(TAG, key + " → Reconnecting...");
                        connectWebSocket(key, url);
                    }, RECONNECT_DELAY);
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, key + " → Error: " + ex.getMessage());
                    sendSystemMessage(key, "⚠️ Error: " + ex.getMessage());
                }
            };

            client.connect();
            webSockets.put(key, client);

        } catch (Exception e) {
            Log.e(TAG, "connectWebSocket exception: " + e.getMessage());
        }
    }

    private void disconnectWebSocket(String key) {
        if (webSockets.containsKey(key)) {
            try {
                webSockets.get(key).close();
                sendSystemMessage(key, "🔴 Connection closed manually.");
            } catch (Exception e) {
                Log.e(TAG, "disconnectWebSocket error: " + e.getMessage());
            }
        }
    }

    // -----------------------------------------------------------------------
    // Outgoing messages (from Activities)
    // -----------------------------------------------------------------------

    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String key = intent.getStringExtra("key");
            String message = intent.getStringExtra("message");
            WebSocketClient socket = webSockets.get(key);

            if (socket != null && socket.isOpen()) {
                try {
                    // Wrap outgoing messages as JSON
                    JSONObject json = new JSONObject();
                    json.put("sender", "AndroidClient");
                    json.put("body", message);
                    socket.send(json.toString());
                    Log.d(TAG, key + " → Sent JSON: " + json);
                } catch (Exception e) {
                    Log.e(TAG, "send message error: " + e.getMessage());
                }
            } else {
                sendSystemMessage(key, "⚠️ Cannot send: socket not connected");
            }
        }
    };

    // -----------------------------------------------------------------------
    // Helper utilities
    // -----------------------------------------------------------------------

    /** Notify activities about connection status */
    private void broadcastStatus(String key, boolean connected) {
        Intent i = new Intent("WebSocketStatus");
        i.putExtra("key", key);
        i.putExtra("connected", connected);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);
    }

    /** Send system or debug message to the UI */
    private void sendSystemMessage(String key, String message) {
        Intent intent = new Intent("WebSocketMessageReceived");
        intent.putExtra("key", key);
        intent.putExtra("message", message);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
}