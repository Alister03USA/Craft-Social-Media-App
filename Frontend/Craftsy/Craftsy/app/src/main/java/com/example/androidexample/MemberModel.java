package com.example.androidexample;

public class MemberModel {
    private final long id;
    private final String username;
    private final String email;

    public MemberModel(long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
}
