package com.example.androidexample;

public class NotificationItem {
    private int id;           // Add this
    private String title;
    private String message;

    // Update constructor to include id
    public NotificationItem(int id, String title, String message) {
        this.id = id;
        this.title = title;
        this.message = message;
    }

    // Getter for id
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }
}
