package com.example.androidexample;

public class TutorialItem {
    private long id;
    private String title;
    private String description;
    private String category;
    private String fileUrl;
    private String filePath;
    private String username;

    public TutorialItem(long id, String title, String description, String category,
                        String fileUrl, String filePath, String username) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.fileUrl = fileUrl;
        this.filePath = filePath;
        this.username = username;
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getFileUrl() { return fileUrl; }
    public String getFilePath() { return filePath; }
    public String getUsername() { return username; }
}