package com.example.craftsy.Challenges.Entity;

import com.example.craftsy.SignUpDelete.Entity.Users;
import jakarta.persistence.*;
import org.antlr.v4.runtime.misc.IntSet;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String type; // daily, weekly, seasonal
    private String category;


    private String description;

    private LocalDate startDate;
    private LocalDate endDate;


    private boolean isActive = true;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private Users createdBy;

    @ManyToMany
    @JoinTable(
            name = "challenge_participants",
            joinColumns = @JoinColumn(name = "challenge_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<Users> participants = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "challenge_completed_users",
            joinColumns = @JoinColumn(name = "challenge_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<Users> completedUsers = new ArrayList<>();


    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChallengePost> posts = new ArrayList<>();

    public List<ChallengePost> getPosts() {
        return posts;
    }

    public void setPosts(List<ChallengePost> posts) {
        this.posts = posts;
    }


    // Getters & Setters
    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }


    public void setCreatedBy(Users createdBy) {
        this.createdBy = createdBy;
    }

    public Users getCreatedBy() {
        return createdBy;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void setParticipants(List<Users> participants) {
        this.participants = participants;
    }

    public List<Users> getParticipants() {
        return participants;
    }


    public void setDescription(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }




    public List<Users> getCompletedUsers() {
        return completedUsers;
    }
}
