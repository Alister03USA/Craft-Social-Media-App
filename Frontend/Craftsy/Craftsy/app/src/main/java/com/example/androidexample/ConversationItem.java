package com.example.androidexample;

public class ConversationItem {

    private final String convoId;
    private final String displayName;
    private final String username; // REAL USERNAME
    private final String lastMessage;
    private final String timestamp;

    private final boolean isGroup;
    private final long profileImageId;

    public ConversationItem(String convoId,
                            String displayName,
                            String username,
                            String lastMessage,
                            String timestamp,
                            long profileImageId) {

        this.convoId = convoId;
        this.displayName = displayName;
        this.username = username;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.isGroup = convoId != null && convoId.startsWith("G-");
        this.profileImageId = profileImageId;
    }

    public String getConvoId() { return convoId; }
    public String getDisplayName() { return displayName; }
    public String getUsername() { return username; }
    public String getLastMessage() { return lastMessage; }
    public String getTimestamp() { return timestamp; }
    public boolean isGroup() { return isGroup; }
    public long getProfileImageId() { return profileImageId; }
}