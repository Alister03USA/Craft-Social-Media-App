package com.example.androidexample;

public class ConversationItem {
    private final String convoId;
    private final String name;
    private final String lastMessage;
    private final String timestamp;

    public ConversationItem(String convoId, String name, String lastMessage, String timestamp) {
        this.convoId = convoId;
        this.name = name;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
    }

    public String getConvoId() { return convoId; }
    public String getName() { return name; }
    public String getLastMessage() { return lastMessage; }
    public String getTimestamp() { return timestamp; }
}