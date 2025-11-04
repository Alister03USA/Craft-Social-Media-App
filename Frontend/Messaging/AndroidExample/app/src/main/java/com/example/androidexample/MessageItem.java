package com.example.androidexample;

import java.util.HashMap;
import java.util.Map;

public class MessageItem {
    private final long id; // message id
    private final String sender;
    private final String content;
    private final String timestamp; // backend returns ISO
    private final Long replyTo; // nullable (for replies)
    private final Long imageId; // nullable
    private final String imageUrl; // nullable
    private final Map<String, Integer> reactions = new HashMap<>();

    // Normal text message constructor
    public MessageItem(long id, String sender, String content, String timestamp, Long replyTo) {
        this(id, sender, content, timestamp, replyTo, null, null);
    }

    // Extended constructor for image messages
    public MessageItem(long id, String sender, String content, String timestamp,
                       Long replyTo, Long imageId, String imageUrl) {
        this.id = id;
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
        this.replyTo = replyTo;
        this.imageId = imageId;
        this.imageUrl = imageUrl;
    }

    public long getId() { return id; }
    public String getSender() { return sender; }
    public String getContent() { return content; }
    public String getTimestamp() { return timestamp; }
    public Long getReplyTo() { return replyTo; }
    public Map<String, Integer> getReactions() { return reactions; }
    public Long getImageId() { return imageId; }
    public String getImageUrl() { return imageUrl; }

    public boolean hasImage() {
        return imageId != null && imageUrl != null && !imageUrl.isEmpty();
    }
}