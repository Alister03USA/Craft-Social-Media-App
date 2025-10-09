package com.example.craftsy.FollowingFollowers.Entity;

import com.example.craftsy.SignUpDelete.Entity.Users;
import jakarta.persistence.*;

/**
 * Follow entity represents a follower-following relationship
 * between users, with a status of PENDING or ACCEPTED.
 */
@Entity
@Table(name = "follow", uniqueConstraints = {@UniqueConstraint(columnNames = {"follower_id", "following_id"})})
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "follower_id", nullable = false)
    private Users follower;

    @ManyToOne
    @JoinColumn(name = "following_id", nullable = false)
    private Users following;

    @Column(nullable = false)
    private String status = "PENDING"; // "PENDING" or "ACCEPTED"

    // Default constructor
    public Follow() {}

    // Full constructor
    public Follow(Users follower, Users following, String status) {
        this.follower = follower;
        this.following = following;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Users getFollower() {
        return follower;
    }

    public void setFollower(Users follower) {
        this.follower = follower;
    }

    public Users getFollowing() {
        return following;
    }

    public void setFollowing(Users following) {
        this.following = following;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
