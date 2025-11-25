package com.example.androidexample;

public class LeaderboardUser {
    public String username;
    public int totalPoints;
    public String tier;

    public LeaderboardUser(String username, int totalPoints, String tier) {
        this.username = username;
        this.totalPoints = totalPoints;
        this.tier = tier;
    }
}