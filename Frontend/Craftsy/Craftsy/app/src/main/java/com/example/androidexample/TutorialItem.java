package com.example.androidexample;

public class TutorialItem {
    private long id;
    private String title;
    private String description;
    private String category;
    private String fileURL;
    private String filePath;
    private String username;

    // New: like count (for leaderboard / sorting)
    private long likeCount;

    public TutorialItem(long id,
                        String title,
                        String description,
                        String category,
                        String fileURL,
                        String filePath,
                        String username) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.fileURL = fileURL;
        this.filePath = filePath;
        this.username = username;
        this.likeCount = 0; // default when not provided
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getFileURL() {
        return fileURL;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getUsername() {
        return username;
    }

    // Setter for dynamic username fetch
    public void setUsername(String username) {
        this.username = username;
    }

    // New: likeCount getter / setter
    public long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(long likeCount) {
        this.likeCount = likeCount;
    }

    @Override
    public String toString() {
        return "TutorialItem{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", username='" + username + '\'' +
                ", likeCount=" + likeCount +
                '}';
    }
}