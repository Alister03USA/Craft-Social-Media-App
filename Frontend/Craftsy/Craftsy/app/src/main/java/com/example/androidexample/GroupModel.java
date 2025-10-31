package com.example.androidexample;

public class GroupModel {
    private final String name;
    private final String description;

    public GroupModel(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
}
