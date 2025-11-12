package com.example.craftsy.PointsSystem.Entity;

import com.example.craftsy.SignUpDelete.Entity.Users;
import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "user_points")
public class UserPoints {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private Users user;

    @Column(name = "total_points", nullable = false)
    private Integer totalPoints = 0;

    @Column(name = "current_tier")
    private String currentTier = "BEGINNER";  // BEGINNER, INTERMEDIATE, EXPERT, ADVANCED, CHAMPION

    @Column(name = "posts_count")
    private Integer postsCount = 0;

    @Column(name = "tutorials_count")
    private Integer tutorialsCount = 0;

    @Column(name = "comments_count")
    private Integer commentsCount = 0;

    @Column(name = "last_updated")
    private Date lastUpdated = new Date();

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Users getUser() { return user; }
    public void setUser(Users user) { this.user = user; }

    public Integer getTotalPoints() { return totalPoints; }
    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
        updateTier();
    }

    public String getCurrentTier() { return currentTier; }
    public void setCurrentTier(String currentTier) { this.currentTier = currentTier; }

    public Integer getPostsCount() { return postsCount; }
    public void setPostsCount(Integer postsCount) { this.postsCount = postsCount; }

    public Integer getTutorialsCount() { return tutorialsCount; }
    public void setTutorialsCount(Integer tutorialsCount) { this.tutorialsCount = tutorialsCount; }

    public Integer getCommentsCount() { return commentsCount; }
    public void setCommentsCount(Integer commentsCount) { this.commentsCount = commentsCount; }

    public Date getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Date lastUpdated) { this.lastUpdated = lastUpdated; }

    // Auto-update tier based on points
    private void updateTier() {
        if (totalPoints >= 300) {
            currentTier = "Champion";
        }
         else if (totalPoints >= 200) {
            currentTier = "Expert";
        } else if (totalPoints >= 100) {
            currentTier = "Intermediate";
        } else {
            currentTier = "Beginner";
        }
    }

    // Helper method to add points
    public void addPoints(Integer points) {
        this.totalPoints += points;
        this.lastUpdated = new Date();
        updateTier();
    }
}