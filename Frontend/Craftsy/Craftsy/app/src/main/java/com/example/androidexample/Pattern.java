package com.example.androidexample;

public class Pattern {
    private long id;
    private String patternName;
    private String username;
    private String patternType;
    private float rating;
    private String patternImage;
    private String patternLink;
    private String difficulty;
    private String description;
    private String supplies;
    private String date;

    public Pattern(
            long id,
            String patternName,
            String username,
            String patternType,
            float rating,
            String patternImage,
            String patternLink,
            String difficulty,
            String description,
            String supplies,
            String date
    ) {
        this.id = id;
        this.patternName = patternName;
        this.username = username;
        this.patternType = patternType;
        this.rating = rating;
        this.patternImage = patternImage;
        this.patternLink = patternLink;
        this.difficulty = difficulty;
        this.description = description;
        this.supplies = supplies;
        this.date = date;
    }

    public Long getId() {
        return id;
    }
    public String getPatternName() { return patternName; }
    public String getUsername() { return username; }
    public String getPatternType() { return patternType; }
    public float getRating() { return rating; }
    public String getPatternImage() { return patternImage; }
    public String getPatternLink() { return patternLink; }
    public String getDifficulty() { return difficulty; }
    public String getDescription() { return description; }
    public String getSupplies() { return supplies; }
    public String getDate() { return date; }

    public void setRating(float rating) { this.rating = rating; }
}
