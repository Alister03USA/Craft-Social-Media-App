package com.example.androidexample;

public class NotificationItem {

    private int id;
    private String title;
    private String message;

    // NEW FIELDS
    private String type; // "follow_request", "info", etc.
    private String senderUsername;

    public NotificationItem(int id, String title, String message, String type, String senderUsername) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.senderUsername = senderUsername;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public String getSenderUsername() {
        return senderUsername;
    }
}
