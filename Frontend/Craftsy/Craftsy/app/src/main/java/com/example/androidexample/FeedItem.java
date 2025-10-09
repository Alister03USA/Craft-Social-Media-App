package com.example.androidexample;

public class FeedItem {
    private String username;
    private String projectName;
    private String projectDesc;
    private String projectType;
    private String supplies;
    private String visibility;
    private String date;

    public FeedItem(String username, String projectName, String projectDesc,
                    String projectType, String supplies, String visibility, String date) {
        this.username = username;
        this.projectName = projectName;
        this.projectDesc = projectDesc;
        this.projectType = projectType;
        this.supplies = supplies;
        this.visibility = visibility;
        this.date = date;
    }

    public String getUsername() { return username; }
    public String getProjectName() { return projectName; }
    public String getProjectDesc() { return projectDesc; }
    public String getProjectType() { return projectType; }
    public String getSupplies() { return supplies; }
    public String getVisibility() { return visibility; }
    public String getDate() { return date; }
}