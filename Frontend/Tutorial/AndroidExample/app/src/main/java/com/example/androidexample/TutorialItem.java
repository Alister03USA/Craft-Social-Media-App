package com.example.androidexample;

public class TutorialItem {
    private long id;
    private String title;
    private String description;
    private String category;
    private String fileURL;
    private String username;

    public TutorialItem(long id, String title, String description, String category, String fileURL, String username) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.fileURL = fileURL;
        this.username = username;
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getFileURL() { return fileURL; }
    public String getUsername() { return username; }
}