package com.example.craftsy.FollowingFollowers.Entity;

import com.example.craftsy.SignUpDelete.Entity.Users;
import jakarta.persistence.*;

/**
 * Follow entity represents a follower-following relationship
 * between users. The "accepted" field indicates whether
 * the follow request has been accepted (true) or is pending (false).
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

    /**
     * Indicates the status of the follow request.
     * false = pending, true = accepted
     */
    @Column(nullable = false)
    private boolean accepted = false; // default is pending

    // Default constructor
    public Follow() {}

    // Full constructor
    public Follow(Users follower, Users following, boolean accepted) {
        this.follower = follower;
        this.following = following;
        this.accepted = accepted;
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

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }
}
