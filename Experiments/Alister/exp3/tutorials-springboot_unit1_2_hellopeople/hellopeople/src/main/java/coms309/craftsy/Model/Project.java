package coms309.craftsy.Model;

import java.util.List;

public class Project {
    private String id;
    private String title;
    private String description;
    private String creatorUsername;

    public Project() {}

    public Project(String id, String title, String description, String creatorUsername, List<String> tags) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.creatorUsername = creatorUsername;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCreatorUsername() { return creatorUsername; }
    public void setCreatorUsername(String creatorUsername) { this.creatorUsername = creatorUsername; }

}
