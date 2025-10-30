package com.example.androidexample;

public class MessageItem {
    private final String sender;
    private final String text;
    private final boolean isMine;

    public MessageItem(String sender, String text, boolean isMine) {
        this.sender = sender;
        this.text = text;
        this.isMine = isMine;
    }
    public String getSender() { return sender; }
    public String getText() { return text; }
    public boolean isMine() { return isMine; }
}