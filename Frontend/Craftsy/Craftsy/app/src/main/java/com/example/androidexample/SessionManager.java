package com.example.androidexample;

public class SessionManager {
    private static SessionManager instance;
    private String loggedInUsername;
    private String displayName;
    private String email;
    private String bio;
    private String craftSpecialties;
    private String password;
    private String targetUser;
    private String profileImageUrl; // NEW: store profile picture filename or URL

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // Getters and setters
    public String getLoggedInUsername() { return loggedInUsername; }
    public void setLoggedInUsername(String username) { this.loggedInUsername = username; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String name) { this.displayName = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getCraftSpecialties() { return craftSpecialties; }
    public void setCraftSpecialties(String craftSpecialties) { this.craftSpecialties = craftSpecialties; }

    public void setTargetUser(String targetUser) { this.targetUser = targetUser; }
    public String getTargetUser() { return targetUser; }

    // NEW: profile image getters and setters
    public void setProfileImageUrl(String imageUrl) { this.profileImageUrl = imageUrl; }
    public String getProfileImageUrl() { return profileImageUrl; }

    public void logout() {
        loggedInUsername = null;
        displayName = null;
        bio = null;
        email = null;
        password = null;
        craftSpecialties = null;
        profileImageUrl = null; // clear profile image
        targetUser = null;
        // If using SharedPreferences, also clear those:
        // prefs.edit().clear().apply();
    }
}
