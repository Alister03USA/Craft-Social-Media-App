package com.example.androidexample;

public class TutorialItem {
    private final long id;
    private final String title, description, category, fileUrl;
    public TutorialItem(long id, String t, String d, String c, String f) {
        this.id=id; title=t; description=d; category=c; fileUrl=f;
    }
    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getFileUrl() { return fileUrl; }
}