package com.example.androidexample;

public class TutorialItem {
    private long id;
    private String title;
    private String description;
    private String category;
    private String fileURL;
    private String filePath;
    private String username;

    public TutorialItem(long id, String title, String description, String category,
                        String fileURL, String filePath, String username) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.fileURL = fileURL;
        this.filePath = filePath;
        this.username = username;
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getFileURL() { return fileURL; }
    public String getFilePath() { return filePath; }
    public String getUsername() { return username; }

    // Added setter for dynamic username fetch
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "TutorialItem{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}