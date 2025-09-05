package coms309.craftsy.Model;

import java.util.ArrayList;
import java.util.List;

public class User {

    private String userName;
    private String displayName;
    private String bio;
    private String profilePicUrl;
    private String[] craftingSpecialties;
    private List<String> followers =  new ArrayList<>();;
    private List<String> following = new ArrayList<>();;

    public User() {
        this.followers = new ArrayList<>();
        this.following = new ArrayList<>();
    }

    public User(String username, String displayName, String bio, String profilePicUrl, String[]craftingSpecialties) {
        this.userName = username;
        this.displayName = displayName;
        this.bio = bio;
        this.profilePicUrl = profilePicUrl;
        this.craftingSpecialties = craftingSpecialties;
        this.followers = new ArrayList<>();
        this.following = new ArrayList<>();
    }

    // Getters and setters
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getProfilePicUrl() { return profilePicUrl; }
    public void setProfilePicUrl(String profilePicUrl) { this.profilePicUrl = profilePicUrl; }
    public String[] getCraftingSpecialties() { return craftingSpecialties; }
    public void setCraftingSpecialties(String[] craftingSpecialties) { this.craftingSpecialties = craftingSpecialties; }

    public List<String> getFollowers() { return followers; }
    public void setFollowers(List<String> followers) { this.followers = followers; }

    public List<String> getFollowing() { return following; }
    public void setFollowing(List<String> following) { this.following = following; }


}
