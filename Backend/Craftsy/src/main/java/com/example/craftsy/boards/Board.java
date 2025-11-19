package com.example.craftsy.boards;

import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.Tutorial.Entity.Tutorial;
import com.example.craftsy.feed.Feed;
import com.example.craftsy.patterns.Patterns;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "board")
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String boardName;

    private String description;

    private LocalDateTime dateCreated;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "username",
            nullable = false,
            referencedColumnName = "username"
    )
    private Users user;

    @ManyToMany
    @JoinTable(
            name = "boardPatterns",
            joinColumns = @JoinColumn(name = "board_id"),
            inverseJoinColumns = @JoinColumn(name = "pattern_id")
    )
    private List<Patterns> patterns;

    @ManyToMany
    @JoinTable(
            name = "boardProjects",
            joinColumns = @JoinColumn(name = "board_id"),
            inverseJoinColumns = @JoinColumn(name = "feed_id")
    )
    private List<Feed> projects;

    @ManyToMany
    @JoinTable(
            name = "boardTutorials",
            joinColumns = @JoinColumn(name = "board_id"),
            inverseJoinColumns = @JoinColumn(name = "tutorial_id")
    )
    private List<Tutorial> tutorials;

    public Board() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBoardName() {
        return boardName;
    }

    public void setBoardName(String boardName) {
        this.boardName = boardName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public List<Patterns> getPatterns() {
        return patterns;
    }

    public void setPatterns(List<Patterns> patterns) {
        this.patterns = patterns;
    }

    public List<Feed> getProjects() {
        return projects;
    }

    public void setProjects(List<Feed> projects) {
        this.projects = projects;
    }

    public List<Tutorial> getTutorials() {
        return tutorials;
    }

    public void setTutorials(List<Tutorial> tutorials) {
        this.tutorials = tutorials;
    }

    public void addPattern(Patterns pattern){
        this.patterns.add(pattern);
    }

    public void removePattern(Patterns pattern){
        this.patterns.remove(pattern);
    }

    public void addProject(Feed project){
        this.projects.add(project);
    }

    public void removeProject(Feed project){
        this.projects.remove(project);
    }

    public void addTutorial(Tutorial tutorial){
        this.tutorials.add(tutorial);
    }

    public void removeTutorial(Tutorial tutorial){
        this.tutorials.remove(tutorial);
    }
}
