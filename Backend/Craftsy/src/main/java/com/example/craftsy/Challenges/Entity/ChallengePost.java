package com.example.craftsy.Challenges.Entity;

import com.example.craftsy.SignUpDelete.Entity.Users;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "challenge_posts")
public class ChallengePost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "media_url")
    private String mediaUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @Column(name = "like_count")
    private Integer likeCount = 0;

    @Column(name = "comment_count")
    private Integer commentCount = 0;


    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChallengePostComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChallengePostLike> likes = new ArrayList<>();

    public List<ChallengePostComment> getComments() {
        return comments;
    }

    public void setComments(List<ChallengePostComment> comments) {
        this.comments = comments;
    }

    public List<ChallengePostLike> getLikes() {
        return likes;
    }

    public void setLikes(List<ChallengePostLike> likes) {
        this.likes = likes;
    }


    // Getters and setters
    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public Users getUser() {
        return user;
    }

    public void setChallenge(Challenge challenge) {
        this.challenge = challenge;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Integer getCommentCount() {
        return commentCount;

    }

}