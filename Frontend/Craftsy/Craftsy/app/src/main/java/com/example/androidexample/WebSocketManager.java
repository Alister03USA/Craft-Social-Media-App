package com.example.androidexample;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.LinkedList;
import java.util.Queue;

public class WebSocketManager {

    private static WebSocketManager instance;
    private WebSocketClient webSocketClient;
    private static final String TAG = "WebSocketManager";

    // Queue messages if socket isn't open yet
    private final Queue<String> messageQueue = new LinkedList<>();
    private boolean isConnecting = false;

    // Listener interface
    public interface WebSocketListener {
        void onMessage(String message);
        void onOpen();
        void onClose(String reason);
        void onError(Exception ex);
    }

    private WebSocketListener listener;
    private String currentUrl;

    private WebSocketManager() {}

    public static WebSocketManager getInstance() {
        if (instance == null) {
            instance = new WebSocketManager();
        }
        return instance;
    }

    public void setListener(WebSocketListener listener) {
        this.listener = listener;
    }

    public boolean isConnected() {
        return webSocketClient != null && webSocketClient.isOpen();
    }

    public void connect(String serverUrl) {
        if (isConnected() || isConnecting) {
            Log.d(TAG, "Already connected or connecting.");
            return;
        }

        currentUrl = serverUrl;
        isConnecting = true;

        try {
            URI uri = new URI(serverUrl);
            webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    Log.d(TAG, "WebSocket Opened");
                    isConnecting = false;

                    // Send queued messages
                    while (!messageQueue.isEmpty()) {
                        sendMessage(messageQueue.poll());
                    }

                    // Notify listener on main thread
                    if (listener != null) {
                        new Handler(Looper.getMainLooper()).post(() -> listener.onOpen());
                    }
                }

                @Override
                public void onMessage(String message) {
                    Log.d(TAG, "Received: " + message);
                    if (listener != null) {
                        new Handler(Looper.getMainLooper()).post(() -> listener.onMessage(message));
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, "WebSocket Closed. Reason: " + reason);
                    webSocketClient = null;
                    isConnecting = false;
                    if (listener != null) {
                        new Handler(Looper.getMainLooper()).post(() -> listener.onClose(reason));
                    }
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "WebSocket Error: ", ex);
                    if (listener != null) {
                        new Handler(Looper.getMainLooper()).post(() -> listener.onError(ex));
                    }
                }
            };

            webSocketClient.connect();

        } catch (Exception e) {
            Log.e(TAG, "WebSocket connection failed", e);
            isConnecting = false;
            if (listener != null) {
                new Handler(Looper.getMainLooper()).post(() -> listener.onError(e));
            }
        }
    }

    public void sendMessage(String message) {
        if (isConnected()) {
            webSocketClient.send(message);
        } else {
            Log.d(TAG, "Socket not ready. Queuing message: " + message);
            messageQueue.add(message);
            // Optionally auto-connect if URL is known
            if (!isConnecting && currentUrl != null) {
                connect(currentUrl);
            }
        }
    }

    public void disconnect() {
        if (webSocketClient != null) {
            webSocketClient.close();
            webSocketClient = null;
        }
        isConnecting = false;
        messageQueue.clear();
    }
}
