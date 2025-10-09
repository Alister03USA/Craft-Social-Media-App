package com.example.androidexample;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class OutsideUserProfile extends BaseActivity {

    private Button followButton;
    private enum FollowState { NOT_FOLLOWING, PENDING, FOLLOWING }
    private FollowState currentState = FollowState.NOT_FOLLOWING;

    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080/alister_gan/follow/Fuji";

    // Replace this with whatever username you navigate to this profile with (Intent extra)
    private String viewedUsername = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outside_user_profile);

        followButton = findViewById(R.id.btn_follow);

        // Example: receive username from previous screen
        viewedUsername = "Fuji";
                //getIntent().getStringExtra("username");

        if (viewedUsername == null) {
            Toast.makeText(this, "No username provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Fetch user profile data when the page loads
        fetchUserProfile(viewedUsername);
        sendFollowRequest(viewedUsername,viewedUsername);

        followButton.setOnClickListener(v -> {
            switch (currentState) {
                case NOT_FOLLOWING:
                    sendFollowRequest(viewedUsername, viewedUsername);
                    break;
                case FOLLOWING:
                    unfollowUser(viewedUsername,);
                    break;
                case PENDING:
                    Toast.makeText(this, "Request already sent", Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    /** ------------------- FETCH USER PROFILE ------------------- **/
    private void fetchUserProfile(String username) {
        String url = "http://coms-3090-028.class.las.iastate.edu:8080/user/Quinn";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        // Simulated backend response example:
                        // {
                        //   "username": "john_doe",
                        //   "displayName": "John Doe",
                        //   "isFollowing": false,
                        //   "isPending": false
                        // }

                        boolean isFollowing = response.optBoolean("isFollowing", false);
                        boolean isPending = response.optBoolean("isPending", false);

                        if (isPending) {
                            currentState = FollowState.PENDING;
                        } else if (isFollowing) {
                            currentState = FollowState.FOLLOWING;
                        } else {
                            currentState = FollowState.NOT_FOLLOWING;
                        }

                        updateFollowButton();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing profile data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Failed to load profile info", Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    /** ------------------- FOLLOW REQUEST ------------------- **/
    private void sendFollowRequest(String followerUsername, String targetUsername) {
        // Construct the backend URL using path variables
        String url = "http://coms-3090-028.class.las.iastate.edu:8080/" + "katiekeck/follow/Quinn";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                null,
                response -> {
                    Toast.makeText(this, "Follow request sent", Toast.LENGTH_SHORT).show();
                    currentState = FollowState.PENDING;
                    updateFollowButton();
                },
                error -> {
                    Toast.makeText(this, "Error sending follow request", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }
        );

        // Add request to the Volley queue
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }


    /** ------------------- UNFOLLOW REQUEST ------------------- **/
    private void unfollowUser(String followerUsername, String targetUsername) {
        // Construct the backend URL using path variables
        String url = "http://coms-3090-028.class.las.iastate.edu:8080/" + followerUsername + "/unfollow/" + targetUsername;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                null, // no body, just like follow
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

        // Add request to the Volley queue
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
