package com.example.craftsy.patterns;

import com.example.craftsy.SignUpDelete.Entity.Users;
import jakarta.persistence.*;

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
}
