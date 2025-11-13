package com.example.craftsy.Challenges.Entity;

import com.example.craftsy.SignUpDelete.Entity.Users;
import jakarta.persistence.*;
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

    private LocalDate startDate;
    private LocalDate endDate;

    private int rewardPoints = 10; // default reward points

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

    public void setRewardPoints(int rewardPoints) {
        this.rewardPoints = rewardPoints;
    }

    public int getRewardPoints() {
        return rewardPoints;
    }



}
