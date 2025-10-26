package com.example.craftsy.patterns.patternsComments;

import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.patterns.Patterns;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Table(name = "patternsComments")
public class PatternsComments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(
            name = "patternName", //name of the foreign key column in the 'patternsComments' table
            nullable = false, //must have username column
            referencedColumnName = "patternName" //column name in the 'patterns' table to reference
    )
    @JsonBackReference
    private Patterns pattern;

    private String text;

    private int likes;

    @Column(nullable = false)
    private LocalDateTime date;

    private Integer rating;

    public PatternsComments() {
    }

    public Patterns getPattern() {
        return pattern;
    }

    public void setPattern(Patterns pattern) {
        this.pattern = pattern;
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

    public Integer getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
