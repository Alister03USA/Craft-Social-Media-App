package coms309.craftsy.model;

import java.util.Arrays;

public class User {
    private String userName;
    private String displayName;
    private String bio;
    private String profilePicUrl;
    private String[] craftingSpecialties;


    // empty constructor
    public User(){}

    // Allows creating a user object with all attributes at once
    public User(String userName, String displayName, String bio, String profilePicUrl, String[] craftingSpecialties) {
        this.userName = userName;
        this.displayName = displayName;
        this.bio = bio;
        this.profilePicUrl = profilePicUrl;
        this.craftingSpecialties = craftingSpecialties;
    }


    // Getters and Setters
    public String getUsername() { return userName; }
    public void setUsername(String username) { this.userName = username; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getProfilePicUrl() { return profilePicUrl; }
    public void setProfilePicUrl(String profilePicUrl) { this.profilePicUrl = profilePicUrl; }

    public String[] getCraftingSpecialties() { return craftingSpecialties; }
    public void setCraftingSpecialties(String[] craftingSpecialties) { this.craftingSpecialties = craftingSpecialties; }


    @Override
    public String toString() {
        return "User{" +
                "username='" + userName + '\'' +
                ", displayName='" + displayName + '\'' +
                ", bio='" + bio + '\'' +
                ", profilePicUrl='" + profilePicUrl + '\'' +
                ", craftingSpecialties=" + Arrays.toString(craftingSpecialties) +
                '}';
    }




}
