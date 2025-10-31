package com.example.androidexample;

public class GroupPostModel {
    private final String username;
    private final String text;
    private final String imageUri;

    public GroupPostModel(String username, String text, String imageUri) {
        this.username = username;
        this.text = text;
        this.imageUri = imageUri;
    }

    public String getUsername() { return username; }
    public String getText() { return text; }
    public String getImageUri() { return imageUri; }
}
