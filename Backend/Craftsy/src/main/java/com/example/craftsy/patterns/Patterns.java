package com.example.craftsy.patterns;

import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.patterns.patternsComments.PatternsComments;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "patterns")
public class Patterns {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "username", //name of the foreign key column in the 'projects' table
            nullable = false, //must have username column
            referencedColumnName = "username" //column name in the 'users' table to reference
    )
    private Users user;

    @Column(nullable = false)
    private String patternName;

    @Column(nullable=false)
    private String patternType;

    private float rating;

    private String patternLink;

    private String difficulty;

    private String description;

    private String supplies;

    @Column(nullable = false)
    private LocalDateTime date;

    @OneToMany(mappedBy = "pattern")
    @OrderBy("likes DESC")
    @JsonManagedReference
    private List<PatternsComments> comments;

    private int numRatings;

    public Patterns() {
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public String getPatternName() {
        return patternName;
    }

    public void setPatternName(String patternName) {
        this.patternName = patternName;
    }

    public String getPatternType() {
        return patternType;
    }

    public void setPatternType(String patternType) {
        this.patternType = patternType;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getPatternLink() {
        return patternLink;
    }

    public void setPatternLink(String patternLink) {
        this.patternLink = patternLink;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSupplies() {
        return supplies;
    }

    public void setSupplies(String supplies) {
        this.supplies = supplies;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public List<PatternsComments> getComments() {
        return comments;
    }

    public void setComments(List<PatternsComments> comments) {
        this.comments = comments;
    }

    public void addComments(PatternsComments comment){
        this.comments.add(comment);
    }

    public int getNumRatings() {
        return numRatings;
    }

    public void setNumRatings(int numRatings) {
        this.numRatings = numRatings;
    }
}
