package com.example.androidexample;

/**
 * Represents a tutorial entity fetched from backend /tutorial/search or /user/{username}.
 * Matches exact backend JSON fields ("fileURL" etc.) and keeps full compatibility.
 */
public class TutorialItem {
    private long id;
    private String title;
    private String description;
    private String category;
    private String fileURL;   // ✅ match backend JSON key
    private String filePath;
    private String username;

    // --- Constructor ---
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

    // --- Getters ---
    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getFileURL() { return fileURL; }   // ✅ consistent getter
    public String getFilePath() { return filePath; }
    public String getUsername() { return username; }

    // --- Optional: Safe toString for debugging ---
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