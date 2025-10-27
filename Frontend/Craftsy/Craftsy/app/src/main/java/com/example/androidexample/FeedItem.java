package com.example.androidexample;

public class FeedItem {
    private String username;
    private String projectName;
    private String projectType;
    private String supplies;
    private String projectDesc;
    private String visibility;
    private String date;
    private String imageUrl;

    public FeedItem(String username, String projectName, String projectType,
                    String supplies, String projectDesc, String visibility,
                    String date, String imageUrl) {
        this.username = username;
        this.projectName = projectName;
        this.projectType = projectType;
        this.supplies = supplies;
        this.projectDesc = projectDesc;
        this.visibility = visibility;
        this.date = date;
        this.imageUrl = imageUrl;
    }

    // Legacy constructor for older code
    public FeedItem(String username, String projectName, String projectType,
                    String supplies, String projectDesc, String visibility, String date) {
        this(username, projectName, projectType, supplies, projectDesc, visibility, date, null);
    }

    public String getUsername() { return username; }
    public String getProjectName() { return projectName; }
    public String getProjectType() { return projectType; }
    public String getSupplies() { return supplies; }
    public String getProjectDesc() { return projectDesc; }
    public String getVisibility() { return visibility; }
    public String getDate() { return date; }
    public String getImageUrl() { return imageUrl; }
}