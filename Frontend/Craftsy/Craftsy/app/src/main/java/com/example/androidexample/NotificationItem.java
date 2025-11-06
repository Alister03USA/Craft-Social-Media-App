package com.example.androidexample;

public class NotificationItem {

    private int id; // notification ID
    private String title;
    private String message;

    // NEW FIELDS
    private String type; // "follow_request", "join_request", "general", etc.
    private String senderUsername;
    private int referenceId; // The ID of the related object (e.g., join request ID)

    public NotificationItem(int id, String title, String message, String type, String senderUsername, int referenceId) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.senderUsername = senderUsername;
        this.referenceId = referenceId;
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

    public int getReferenceId() {
        return referenceId;
    }
}
