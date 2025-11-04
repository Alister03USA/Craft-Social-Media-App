package com.example.androidexample;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

public class OutsideUserProfile extends BaseActivity {

    private static final String TAG = "OutsideUserProfile";

    private Button followButton;
    private TextView displayNameTv, usernameTv, bioTv, followersTv, followingTv, craftSpecialtiesTv;
    private ImageView profileImageView;

    private enum FollowState { NOT_FOLLOWING, PENDING, FOLLOWING }
    private FollowState currentState = FollowState.NOT_FOLLOWING;

    private String viewedUsername;       // target user
    private String loggedInUsername;     // from SessionManager
    @Nullable private JSONObject viewedUserJson; // last known profile info

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outside_user_profile);
        //setupBottomNavigation(R.id.nav_view_user);

        // UI refs
        followButton = findViewById(R.id.btn_follow);
        displayNameTv = findViewById(R.id.displayName);
        usernameTv = findViewById(R.id.username);
        bioTv = findViewById(R.id.bio);
        followersTv = findViewById(R.id.followersCount);
        followingTv = findViewById(R.id.followingCount);
        craftSpecialtiesTv = findViewById(R.id.craftSpecialties);
        profileImageView = findViewById(R.id.profileImage);

        // Logged-in user
        loggedInUsername = SessionManager.getInstance().getLoggedInUsername();
        if (loggedInUsername == null || loggedInUsername.isEmpty()) {
            Toast.makeText(this, "No logged-in user found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Pull everything we can from the Intent (sent by UserSearchFragment)
        viewedUsername = getIntent().getStringExtra("username");
        String displayName = getIntent().getStringExtra("displayName");
        String bio = getIntent().getStringExtra("bio");
        String craftSpecialties = getIntent().getStringExtra("craftSpecialties");

        Log.d(TAG, "Opening profile for: " + viewedUsername);

        if (viewedUsername == null || viewedUsername.trim().isEmpty()) {
            Toast.makeText(this, "Invalid user", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Prefer data from Intent (fast, already fetched)
        boolean hadExtras = (displayName != null) || (bio != null) || (craftSpecialties != null);
        if (hadExtras) {
            JSONObject fromExtras = new JSONObject();
            try {
                fromExtras.put("username", viewedUsername);
                if (displayName != null) fromExtras.put("displayName", displayName);
                if (bio != null) fromExtras.put("bio", bio);
                if (craftSpecialties != null) fromExtras.put("craftSpecialties", craftSpecialties);
            } catch (Exception ignored) {}
            viewedUserJson = fromExtras;
            updateUIWithProfile(viewedUserJson);
        } else {
            // Fallback: query search endpoint and pick the exact match
            fetchProfileViaSearch(viewedUsername);
        }

        // Always fetch relationship + counts
        fetchFollowStatus(viewedUsername);
        fetchFollowersAndFollowing(viewedUsername);

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

    /** ------------------- FALLBACK PROFILE FETCH VIA SEARCH ------------------- **/
    private void fetchProfileViaSearch(String targetUsername) {
        String url = "http://coms-3090-028.class.las.iastate.edu:8080/search/user?query=" + targetUsername;
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        JSONObject best = null;
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            if (targetUsername.equals(obj.optString("username"))) {
                                best = obj; break;
                            }
                        }
                        if (best == null && response.length() > 0) {
                            best = response.getJSONObject(0); // fallback to first
                        }
                        if (best != null) {
                            viewedUserJson = best;
                            updateUIWithProfile(best);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Profile parse error", e);
                    }
                },
                error -> Log.e(TAG, "Profile search fallback failed", error)
        );
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    /** ------------------- RELATIONSHIP / COUNTS ------------------- **/
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

                    updateFollowButton();

                    // refresh UI if we didn’t have extras earlier
                    if (viewedUserJson != null) {
                        updateUIWithProfile(viewedUserJson);
                    }
                },
                error -> Log.e(TAG, "Failed to load follow status", error)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(statusRequest);
    }

    private void fetchFollowersAndFollowing(String targetUsername) {
        // Followers
        String followersUrl = "http://coms-3090-028.class.las.iastate.edu:8080/" + targetUsername + "/followers";
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

    /** ------------------- FOLLOW / UNFOLLOW ------------------- **/
    private void sendFollowRequest(String followerUsername, String targetUsername) {
        String url = "http://coms-3090-028.class.las.iastate.edu:8080/" + followerUsername + "/follow/" + targetUsername;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                null,
                response -> {
                    String status = response.optString("status", "error");
                    String message = response.optString("message", "Follow request failed");
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    if ("success".equals(status)) {
                        currentState = FollowState.PENDING;
                        updateFollowButton();
                    }
                },
                error -> {
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

    private void unfollowUser(String followerUsername, String targetUsername) {
        String url = "http://coms-3090-028.class.las.iastate.edu:8080/" + followerUsername + "/unfollow/" + targetUsername;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,
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

    /** ------------------- UI ------------------- **/
    private void updateUIWithProfile(JSONObject userJson) {
        if (userJson == null) return;

        String displayName = userJson.optString("displayName", userJson.optString("username", ""));
        String username = userJson.optString("username", viewedUsername != null ? viewedUsername : "");
        String bio = userJson.optString("bio", "");
        String craftSpecialties = userJson.optString("craftSpecialties", "");

        displayNameTv.setText(displayName);
        usernameTv.setText(username);

        if (bio == null || "null".equalsIgnoreCase(bio) || bio.isEmpty()) {
            bioTv.setVisibility(View.GONE);
        } else {
            bioTv.setVisibility(View.VISIBLE);
            bioTv.setText(bio);
        }

        if (craftSpecialties == null || "null".equalsIgnoreCase(craftSpecialties) || craftSpecialties.isEmpty()) {
            craftSpecialtiesTv.setVisibility(View.GONE);
        } else {
            craftSpecialtiesTv.setVisibility(View.VISIBLE);
            craftSpecialtiesTv.setText(craftSpecialties);
        }
    }

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