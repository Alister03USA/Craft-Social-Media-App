package com.example.androidexample;

public class GroupPostModel {
    private final String username;
    private final String content;
    private final Long messageId;
    private final String mediaUrl;

    public GroupPostModel(String username, String content, Long messageId, String mediaUrl) {
        this.username = username;
        this.content = content;
        this.messageId = messageId;
        this.mediaUrl = mediaUrl;
    }

    public String getUsername() { return username; }
    public String getContent() { return content; }
    public Long getMessageId() { return messageId; }
    public String getMediaUrl() { return mediaUrl; }
}

