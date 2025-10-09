package com.example.androidexample;

public class Pattern {
    private int id;
    private String imageUrl;
    private float rating;

    public Pattern(int id, String imageUrl, float rating) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.rating = rating;
    }

    public int getId() { return id; }
    public String getImageUrl() { return imageUrl; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
}

