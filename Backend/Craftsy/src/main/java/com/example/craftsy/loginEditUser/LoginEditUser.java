package com.example.craftsy.loginEditUser;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class LoginEditUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;
    private String displayName;
    private String bio;
    private String craftSpecialties;
    private int followers;
    private int following;
    private String email;
    private String password;

    public LoginEditUser() {
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getCraftSpecialties() {
        return craftSpecialties;
    }

    public void setCraftSpecialties(String craftSpecialties) {
        this.craftSpecialties = craftSpecialties;
    }

    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public int getFollowing() {
        return following;
    }

    public void setFollowing(int following) {
        this.following = following;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
