package com.example.androidexample;

public class Review {
    public Long id;
    public String text;
    public String date;
    public int likes;
    public Integer rating;

    public Review(Long id, String text, String date, int likes, Integer rating) {
        this.id = id;
        this.text = text;
        this.date = date;
        this.likes = likes;
        this.rating = rating;
    }
}
