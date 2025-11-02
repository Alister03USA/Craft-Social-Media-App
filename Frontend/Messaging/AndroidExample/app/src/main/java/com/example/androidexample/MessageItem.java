package com.example.androidexample;

import java.util.HashMap;
import java.util.Map;

public class MessageItem {
    private final long id; // message id
    private final String sender;
    private final String content;
    private final String timestamp; // backend returns ISO
    private final Long replyTo; // nullable (for replies)
    private final Map<String, Integer> reactions = new HashMap<>();

    public MessageItem(long id, String sender, String content, String timestamp, Long replyTo) {
        this.id = id;
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
        this.replyTo = replyTo;
    }

    public long getId() { return id; }
    public String getSender() { return sender; }
    public String getContent() { return content; }
    public String getTimestamp() { return timestamp; }
    public Long getReplyTo() { return replyTo; }
    public Map<String, Integer> getReactions() { return reactions; }
}