package com.example.androidexample;

public class PointHistoryItem {

    private long id;
    private int points;
    private String action;
    private String referenceId;
    private String date;

    public PointHistoryItem(long id, int points, String action, String referenceId, String date) {
        this.id = id;
        this.points = points;
        this.action = action;
        this.referenceId = referenceId;
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public int getPoints() {
        return points;
    }

    public String getAction() {
        return action;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public String getDate() {
        return date;
    }
}