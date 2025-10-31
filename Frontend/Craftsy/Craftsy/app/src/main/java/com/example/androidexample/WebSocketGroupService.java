package com.example.androidexample;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;

/**
 * WebSocket service for real-time group posts (chat/feed updates).
 */
public class WebSocketGroupService extends Service {

    private static final String TAG = "WebSocketGroupService";
    private static final String CHANNEL_ID = "GroupWebSocketChannel";

    private WebSocketClient webSocketClient;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(2, buildNotification("Group WebSocket Service Running..."));
        connectWebSocket();
    }

    private void connectWebSocket() {
        try {
            String username = SessionManager.getInstance().getLoggedInUsername();
            if (username == null || username.isEmpty()) {
                Log.w(TAG, "No logged-in user. WebSocket will not connect.");
                return;
            }

            // ✅ Update this to your server URL
            // Using ws:// because it’s a WebSocket endpoint (not HTTP)
            String serverUrl = "ws://coms-3090-028.class.las.iastate.edu:8080/ws/groups/" + username;

            URI uri = new URI(serverUrl);
            webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    Log.d(TAG, "Connected to group WebSocket as " + username);
                }

                @Override
                public void onMessage(String message) {
                    Log.d(TAG, "Received group message: " + message);
                    try {
                        JSONObject json = new JSONObject(message);
                        String groupName = json.optString("groupName", "Unknown Group");
                        String author = json.optString("author", "Unknown");
                        String content = json.optString("content", "");
                        String imageUrl = json.optString("imageUrl", null);

                        // Send broadcast to update UI (GroupChatActivity)
                        Intent intent = new Intent("NEW_GROUP_MESSAGE");
                        intent.putExtra("groupName", groupName);
                        intent.putExtra("author", author);
                        intent.putExtra("content", content);
                        intent.putExtra("imageUrl", imageUrl);
                        sendBroadcast(intent);

                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing group message JSON", e);
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, "Group WebSocket closed: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "Group WebSocket error", ex);
                }
            };

            webSocketClient.connect();

        } catch (Exception e) {
            Log.e(TAG, "Failed to connect group WebSocket", e);
        }
    }

    /**
     * Send a message to the group.
     */
    public void sendGroupMessage(String groupName, String content, @Nullable String imageUrl) {
        try {
            if (webSocketClient != null && webSocketClient.isOpen()) {
                JSONObject json = new JSONObject();
                json.put("groupName", groupName);
                json.put("author", SessionManager.getInstance().getLoggedInUsername());
                json.put("content", content);
                if (imageUrl != null) json.put("imageUrl", imageUrl);

                webSocketClient.send(json.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error sending group message", e);
        }
    }

    private Notification buildNotification(String content) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Group Service")
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_message)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Group WebSocket Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Keeps the group WebSocket connection alive");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webSocketClient != null) {
            webSocketClient.close();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
