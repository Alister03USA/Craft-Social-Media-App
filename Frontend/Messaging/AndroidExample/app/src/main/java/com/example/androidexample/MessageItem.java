package com.example.androidexample;

public class MessageItem {
    private final String sender;
    private final String content;
    private final String timestamp;

    public MessageItem(String sender, String content, String timestamp) {
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
    }
    public String getSender() { return sender; }
    public String getContent() { return content; }
    public String getTimestamp() { return timestamp; }
}