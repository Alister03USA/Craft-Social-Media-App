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

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // Getters and setters
    public String getLoggedInUsername() { return loggedInUsername; }
    public void settargetUser(String targetUser) { this.targetUser = targetUser; }
    public String gettargetUser() { return targetUser; }
    public void setLoggedInUsername(String username) { this.loggedInUsername = username; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String name) { this.displayName = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public void setPassword (String password) {this.password = password;}
    public String getPassword () {return password;}

    public String getCraftSpecialties() { return craftSpecialties; }
    public void setCraftSpecialties(String craftSpecialties) { this.craftSpecialties = craftSpecialties; }
    public void logout() {
        loggedInUsername = null;
        displayName = null;
        bio = null;
        email = null;
        password = null;
        craftSpecialties = null;
        // If you're using SharedPreferences, also clear those:
        // prefs.edit().clear().apply();
    }

}
