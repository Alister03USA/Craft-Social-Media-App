package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONObject;

public class OutsideUserProfile extends BaseActivity {

    private Button followButton;
    private TextView displayNameTv, usernameTv, bioTv, followersTv, followingTv;
    private ImageView profileImageView;

    private enum FollowState { NOT_FOLLOWING, PENDING, FOLLOWING }
    private FollowState currentState = FollowState.NOT_FOLLOWING;

    private String viewedUsername;       // target user
    private String loggedInUsername;     // follower, from singleton
    private JSONObject viewedUserJson;   // profile JSON of viewed user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outside_user_profile);
        setupBottomNavigation(R.id.nav_view_user); // highlight Notifications


        // UI references
        followButton = findViewById(R.id.btn_follow);
        displayNameTv = findViewById(R.id.displayName);
        usernameTv = findViewById(R.id.username);
        bioTv = findViewById(R.id.bio);
        followersTv = findViewById(R.id.followersCount);
        followingTv = findViewById(R.id.followingCount);
        profileImageView = findViewById(R.id.profileImage);
        fetchFollowersAndFollowing("alister_gan");
        sendFollowRequest("alister_gan","Fuji" );

        // Get logged-in user
        loggedInUsername = SessionManager.getInstance().getLoggedInUsername();
        if (loggedInUsername == null) {
            Toast.makeText(this, "No logged-in user found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get the viewed user
        viewedUsername = "alister_gan";

        // Fetch the profile of the viewed user
        fetchViewedUserProfile(viewedUsername);

        // Follow/unfollow button click
        followButton.setOnClickListener(v -> {
            switch (currentState) {
                case NOT_FOLLOWING:
                    sendFollowRequest(loggedInUsername, viewedUsername);
                    break;
                case FOLLOWING:
                    unfollowUser(loggedInUsername, viewedUsername);
                    break;
                case PENDING:
                    Toast.makeText(this, "Request already sent", Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    /** ------------------- FETCH VIEWED USER PROFILE ------------------- **/
    private void fetchViewedUserProfile(String targetUsername) {
        // Build URL dynamically
        String userUrl = "http://coms-3090-028.class.las.iastate.edu:8080/login/alister_gan/Alistergan_123";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                userUrl,
                null,
                response -> {
                    try {
                        // Save JSON for later use
                        viewedUserJson = response;

                        // Extract values from JSON
                        String displayName = response.optString("displayName", targetUsername);
                        String bio = response.optString("bio", "");
                        String craftSpecialties = response.optString("craftSpecialties", "");

                        // Update UI elements in your ScrollView
                        displayNameTv.setText(displayName);      // TextView with id displayName
                        usernameTv.setText(targetUsername);      // TextView with id username
                        bioTv.setText(bio);                      // TextView with id bio

                        // If you had a TextView for craft specialties, update it:
                        // craftSpecialtiesTv.setText(craftSpecialties);

                        // Optional: load profile image if backend provides URL
                        // String profileImageUrl = response.optString("profileImageUrl", "");
                        // Glide.with(this).load(profileImageUrl).into(profileImageView);

                        // Once profile loads, fetch follow status & counts
                        fetchFollowStatus(targetUsername);
                        fetchFollowersAndFollowing(targetUsername);

                    } catch (Exception e) {
                        Toast.makeText(this, "Profile parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                },
                error -> {
                    String msg = "Profile load failed: ";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        msg += new String(error.networkResponse.data);
                    } else {
                        msg += error.getMessage();
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }
        );

        // Add request to Volley queue
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }


    private void fetchFollowStatus(String targetUsername) {
        String statusUrl = "http://coms-3090-028.class.las.iastate.edu:8080/"
                + loggedInUsername + "/profile/" + targetUsername;

        JsonObjectRequest statusRequest = new JsonObjectRequest(
                Request.Method.GET,
                statusUrl,
                null,
                statusResponse -> {
                    boolean isFollowing = statusResponse.optBoolean("isFollowing", false);
                    boolean isPending = statusResponse.optBoolean("isPending", false);

                    if (isPending) currentState = FollowState.PENDING;
                    else if (isFollowing) currentState = FollowState.FOLLOWING;
                    else currentState = FollowState.NOT_FOLLOWING;

                    // Update follow button UI
                    updateFollowButton();

                    // Update profile UI
                    updateUIWithProfile(viewedUserJson);

                    // Fetch followers and following counts
                    fetchFollowersAndFollowing(viewedUsername);

                },
                error -> Toast.makeText(this, "Failed to load follow status", Toast.LENGTH_SHORT).show()
        );

        VolleySingleton.getInstance(this).addToRequestQueue(statusRequest);
    }

    /** ------------------- FOLLOWERS / FOLLOWING ------------------- **/
    private void fetchFollowersAndFollowing(String targetUsername) {
        // Followers
        String followersUrl = "http://coms-3090-028.class.las.iastate.edu:8080/alister_gan/followers";
        JsonArrayRequest followersRequest = new JsonArrayRequest(
                Request.Method.GET,
                followersUrl,
                null,
                response -> followersTv.setText(response.length() + " Followers"),
                error -> followersTv.setText("0 Followers")
        );
        VolleySingleton.getInstance(this).addToRequestQueue(followersRequest);

        // Following
        String followingUrl = "http://coms-3090-028.class.las.iastate.edu:8080/" + targetUsername + "/following";
        JsonArrayRequest followingRequest = new JsonArrayRequest(
                Request.Method.GET,
                followingUrl,
                null,
                response -> followingTv.setText(response.length() + " Following"),
                error -> followingTv.setText("0 Following")
        );
        VolleySingleton.getInstance(this).addToRequestQueue(followingRequest);
    }

    /** ------------------- POPULATE UI ------------------- **/
    private void updateUIWithProfile(JSONObject userJson) {
        String displayName = userJson.optString("displayName", userJson.optString("username", ""));
        String username = userJson.optString("username", "");
        String bio = userJson.optString("bio", "");

        displayNameTv.setText(displayName);
        usernameTv.setText(username);
        bioTv.setText(bio);

        // TODO: load profile image if backend provides URL
    }

    /** ------------------- FOLLOW REQUEST ------------------- **/
    private void sendFollowRequest(String followerUsername, String targetUsername) {
        String url = "http://coms-3090-028.class.las.iastate.edu:8080/" +SessionManager.getInstance().getLoggedInUsername() +"/follow/"+ SessionManager.getInstance().gettargetUser();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                null,  // no body needed for a follow request
                response -> {
                    String status = response.optString("status", "error");
                    String message = response.optString("message", "Follow request failed");

                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                    if (status.equals("success")) {
                        currentState = FollowState.PENDING;
                        updateFollowButton();
                    }
                },
                error -> {
                    // <-- REPLACE THIS BLOCK WITH:
                    if (error.networkResponse != null) {
                        Log.e("FOLLOW_ERROR", "Status: " + error.networkResponse.statusCode);
                        Log.e("FOLLOW_ERROR", "Body: " + new String(error.networkResponse.data));
                    } else {
                        Log.e("FOLLOW_ERROR", "Volley error: ", error);
                    }
                    Toast.makeText(this, "Error sending follow request", Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }



    /** ------------------- UNFOLLOW REQUEST ------------------- **/
    private void unfollowUser(String followerUsername, String targetUsername) {
        String url = "http://coms-3090-028.class.las.iastate.edu:8080/" + followerUsername + "/unfollow/" + targetUsername;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,  // 🔹 DELETE method for unfollow
                url,
                null,
                response -> {
                    Toast.makeText(this, "Unfollowed user", Toast.LENGTH_SHORT).show();
                    currentState = FollowState.NOT_FOLLOWING;
                    updateFollowButton();
                },
                error -> {
                    Toast.makeText(this, "Error unfollowing user", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    /** ------------------- UI UPDATE ------------------- **/
    private void updateFollowButton() {
        switch (currentState) {
            case NOT_FOLLOWING:
                followButton.setText("Follow");
                followButton.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
                followButton.setTextColor(getResources().getColor(android.R.color.white));
                break;
            case FOLLOWING:
                followButton.setText("Unfollow");
                followButton.setBackgroundColor(getResources().getColor(android.R.color.white));
                followButton.setTextColor(getResources().getColor(android.R.color.black));
                break;
            case PENDING:
                followButton.setText("Pending Request");
                followButton.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                followButton.setTextColor(getResources().getColor(android.R.color.white));
                break;
        }
    }
}
