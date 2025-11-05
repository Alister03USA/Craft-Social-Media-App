package com.example.androidexample;

public class CommentModel {
    private final String username;
    private final String comment;

    public CommentModel(String username, String comment) {
        this.username = username;
        this.comment = comment;
    }

    public String getUsername() { return username; }
    public String getComment() { return comment; }
}
