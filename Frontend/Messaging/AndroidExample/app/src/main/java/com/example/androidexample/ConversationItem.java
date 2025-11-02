package com.example.androidexample;

public class ConversationItem {
    private final String convoId;     // "D-xxxx" or "G-xxxx"
    private final String name;        // display name (other user or group)
    private final String lastMessage;
    private final String timestamp;   // ISO string or pretty

    private final boolean isGroup;

    public ConversationItem(String convoId, String name, String lastMessage, String timestamp) {
        this.convoId = convoId;
        this.name = name;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.isGroup = convoId != null && convoId.startsWith("G-");
    }

    public String getConvoId() { return convoId; }
    public String getName() { return name; }
    public String getLastMessage() { return lastMessage; }
    public String getTimestamp() { return timestamp; }
    public boolean isGroup() { return isGroup; }
}