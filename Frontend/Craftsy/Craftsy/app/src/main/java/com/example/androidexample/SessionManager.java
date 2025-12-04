package com.example.androidexample;

public class SessionManager {

    private static SessionManager instance;

    private String loggedInUsername;
    private String displayName;
    private String bio;
    private String email;
    private String password;
    private String craftSpecialties;

    // Corrected from filename string to imageId long
    private long profileImageId = -1;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void logout() {
        loggedInUsername = null;
        displayName = null;
        bio = null;
        email = null;
        password = null;
        craftSpecialties = null;
        profileImageId = -1;
    }

    public String getLoggedInUsername() { return loggedInUsername; }
    public void setLoggedInUsername(String v) { loggedInUsername = v; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String v) { displayName = v; }

    public String getBio() { return bio; }
    public void setBio(String v) { bio = v; }

    public String getEmail() { return email; }
    public void setEmail(String v) { email = v; }

    public String getPassword() { return password; }
    public void setPassword(String v) { password = v; }

    public String getCraftSpecialties() { return craftSpecialties; }
    public void setCraftSpecialties(String v) { craftSpecialties = v; }

    public long getProfileImageId() { return profileImageId; }
    public void setProfileImageId(long id) { profileImageId = id; }
}