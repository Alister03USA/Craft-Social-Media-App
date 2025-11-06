package com.example.androidexample;

public class GroupPostModel {
    private final String username;
    private final String content;
    private final Long messageId;
    private final String mediaUrl;
    private final long groupId; // Added this field

    public GroupPostModel(String username, String content, Long messageId, String mediaUrl, long groupId) {
        this.username = username;
        this.content = content;
        this.messageId = messageId;
        this.mediaUrl = mediaUrl;
        this.groupId = groupId; // Initialize it
    }

    public String getUsername() { return username; }
    public String getContent() { return content; }
    public Long getMessageId() { return messageId; }
    public String getMediaUrl() { return mediaUrl; }
    public long getGroupId() { return groupId; } // Getter for groupId
}
