package com.example.androidexample;

/**
 * Model representing one search result item.
 */
public class SearchItem {

    private String type;
    private String title;
    private String description;
    private String username;
    private String extra; // optional: for project type, tutorial category, etc.
    private org.json.JSONObject extrasMap;

    public org.json.JSONObject getExtrasMap() {
        return extrasMap;
    }

    public void setExtrasMap(org.json.JSONObject extrasMap) {
        this.extrasMap = extrasMap;
    }
    // Full constructor
    public SearchItem(String type, String title, String description, String username) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.username = username;
    }

    // Basic constructor
    public SearchItem(String type, String title, String description) {
        this.type = type;
        this.title = title;
        this.description = description;
    }

    // Getters
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getUsername() { return username; }
    public String getExtra() { return extra; }

    // Setters
    public void setType(String type) { this.type = type; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setUsername(String username) { this.username = username; }
    public void setExtra(String extra) { this.extra = extra; }
}