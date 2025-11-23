package com.example.androidexample;

import java.util.List;

public class BoardModel {

    public long id;
    public String boardName;
    public String description;
    public String dateCreated;

    public List<Pattern> patterns;
    public List<FeedItem> projects;
    public List<TutorialItem> tutorials;

    public long getId() { return id; }
    public String getBoardName() { return boardName; }
    public String getDescription() { return description; }
    public String getDateCreated() { return dateCreated; }

    public List<Pattern> getPatterns() { return patterns; }
    public List<FeedItem> getProjects() { return projects; }
    public List<TutorialItem> getTutorials() { return tutorials; }
}