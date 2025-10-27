package com.example.androidexample;

public class ChatMessage {

    private String sender;
    private String message;
    private boolean isSent;

    public ChatMessage(String sender, String message, boolean isSent) {
        this.sender = sender;
        this.message = message;
        this.isSent = isSent;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSent() {  // ✅ renamed
        return isSent;
    }

    public void setSent(boolean sent) {
        isSent = sent;
    }
}