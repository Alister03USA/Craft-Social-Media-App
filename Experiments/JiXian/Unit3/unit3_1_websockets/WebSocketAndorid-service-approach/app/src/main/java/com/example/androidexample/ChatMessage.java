package com.example.androidexample;

public class ChatMessage {
    private final String sender;
    private final String message;
    private final boolean isMine;

    public ChatMessage(String sender, String message, boolean isMine) {
        this.sender = sender;
        this.message = message;
        this.isMine = isMine;
    }

    public String getSender() { return sender; }
    public String getMessage() { return message; }
    public boolean isMine() { return isMine; }
}