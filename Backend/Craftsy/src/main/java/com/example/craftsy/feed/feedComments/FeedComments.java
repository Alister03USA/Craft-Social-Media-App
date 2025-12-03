package com.example.craftsy.feed.feedComments;

import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.feed.Feed;
import com.example.craftsy.patterns.Patterns;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedComments")
public class FeedComments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "feed_id", nullable = false)
    @JsonBackReference
    private Feed feed;

    @Column(nullable = false)
    private String text;

    private int likes;

    @Column(nullable = false)
    private LocalDateTime date;

    @ManyToOne
    @JoinColumn(
            name = "user_id",
            nullable = false,
            referencedColumnName = "id"
    )
    private Users user;

    public FeedComments() {
    }

    public Feed getFeed() {
        return feed;
    }

    public void setFeed(Feed feed) {
        this.feed = feed;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }
}
