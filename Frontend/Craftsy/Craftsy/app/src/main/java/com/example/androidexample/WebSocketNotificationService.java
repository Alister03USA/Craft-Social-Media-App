package com.example.androidexample;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.app.ActivityManager;
import java.util.List;
import android.content.Context;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;

public class WebSocketNotificationService extends Service {

    private static final String TAG = "WebSocketService";
    private static final String CHANNEL_ID = "WebSocketNotifications";
    private WebSocketClient webSocketClient;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        connectWebSocket();
    }

    private boolean isAppInForeground() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : processes) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                    processInfo.processName.equals(getPackageName())) {
                return true;
            }
        }
        return false;
    }

    private void connectWebSocket() {
        try {
            String username = SessionManager.getInstance().getLoggedInUsername();
            if (username == null || username.isEmpty()) {
                Log.w(TAG, "No logged-in user found. WebSocket will not connect.");
                return;
            }

            String serverUrl = "ws://coms-3090-028.class.las.iastate.edu:8080/ws/notifications/" + username;
            URI uri = new URI(serverUrl);

            webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    Log.d(TAG, "WebSocket Connected for user: " + username);
                }

                @Override
                public void onMessage(String message) {
                    Log.d(TAG, "Received: " + message);
                    try {
                        JSONObject json = new JSONObject(message);

                        int id = json.optInt("id", -1);
                        String title = json.optString("title", "Notification");
                        String body = json.optString("message", "");

                        if (isAppInForeground()) {
                            // Only notify the app UI
                            Intent intent = new Intent("NEW_NOTIFICATION");
                            intent.putExtra("id", id);
                            intent.putExtra("title", title);
                            intent.putExtra("message", body);
                            sendBroadcast(intent);
                        } else {
                            // Show Android system notification
                            showNotification(title, body, id);
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing notification JSON", e);
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, "WebSocket Closed: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "WebSocket Error", ex);
                }
            };

            webSocketClient.connect();

        } catch (Exception e) {
            Log.e(TAG, "Failed to connect WebSocket", e);
        }
    }

    private void showNotification(String title, String message, int notificationId) {
        Intent intent = new Intent(this, NotificationCenterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL) // vibrate + sound
                .setFullScreenIntent(pendingIntent, true) // heads-up / top notification
                .build();

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        int finalId = (notificationId != -1) ? notificationId : (int) System.currentTimeMillis();
        manager.notify(finalId, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "WebSocket Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Push notifications from WebSocket backend");
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
