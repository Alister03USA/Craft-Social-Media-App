package com.example.androidexample;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

public class OutsideUserProfile extends BaseActivity {

    private static final String TAG = "OutsideUserProfile";

    private Button followButton;
    private TextView displayNameTv, usernameTv, bioTv, followersTv, followingTv, craftSpecialtiesTv;
    private ImageView profileImageView;

    private enum FollowState { NOT_FOLLOWING, PENDING, FOLLOWING }
    private FollowState currentState = FollowState.NOT_FOLLOWING;

    private String viewedUsername;
    private String loggedInUsername;

    @Nullable
    private JSONObject viewedUserJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outside_user_profile);
        setupBottomNavigation(R.id.nav_search);

        followButton = findViewById(R.id.btn_follow);
        displayNameTv = findViewById(R.id.displayName);
        usernameTv = findViewById(R.id.username);
        bioTv = findViewById(R.id.bio);
        followersTv = findViewById(R.id.followersCount);
        followingTv = findViewById(R.id.followingCount);
        craftSpecialtiesTv = findViewById(R.id.craftSpecialties);
        profileImageView = findViewById(R.id.profileImage);
        ImageView tierBadge = findViewById(R.id.tierBadgeOutside);
        TextView pointsText = findViewById(R.id.pointsTextOutside);
        TextView tierText = findViewById(R.id.tierTextOutside);
        ProgressBar tierProgress = findViewById(R.id.tierProgressOutside);

        loggedInUsername = SessionManager.getInstance().getLoggedInUsername();
        if (loggedInUsername == null || loggedInUsername.isEmpty()) {
            Toast.makeText(this, "No logged-in user found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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

        boolean hasExtras = (displayName != null) || (bio != null) || (craftSpecialties != null);
        if (hasExtras) {
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
            fetchProfileViaSearch(viewedUsername);
        }

        // NEW: Always fetch real user profile to get image
        fetchUserDirect(viewedUsername);

        fetchFollowStatus(viewedUsername);
        fetchFollowersAndFollowing(viewedUsername);
        fetchUserPoints(viewedUsername, tierBadge, pointsText, tierText, tierProgress);

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

    /* ========================= NEW: Fetch full profile with image ========================= */
    private void fetchUserDirect(String username) {
        String url = "http://coms-3090-028.class.las.iastate.edu:8080/user/" + username;

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                res -> {
                    Log.d(TAG, "Full profile received: " + res);
                    viewedUserJson = res;
                    updateUIWithProfile(res);
                },
                err -> Log.e(TAG, "Failed to fetch full user profile", err)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    /* ========================= Fallback search (no images) ========================= */
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
                                best = obj;
                                break;
                            }
                        }

                        if (best == null && response.length() > 0) {
                            best = response.getJSONObject(0);
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

    /* ========================= Load image via imageId ========================= */
    private void loadOutsideProfileImage(ImageView target, long imageId) {
        if (imageId <= 0) {
            target.setImageResource(R.drawable.profile);
            return;
        }

        String url = "http://coms-3090-028.class.las.iastate.edu:8080/images/" + imageId;

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                res -> {
                    String path = res.optString("filePath", "");
                    if (path.isEmpty()) {
                        target.setImageResource(R.drawable.profile);
                        return;
                    }

                    String filename = path.substring(path.lastIndexOf("/") + 1);
                    String fullUrl = "http://coms-3090-028.class.las.iastate.edu:8080/uploads/" + filename;

                    Glide.with(this)
                            .load(fullUrl)
                            .placeholder(R.drawable.profile)
                            .circleCrop()
                            .into(target);
                },
                err -> target.setImageResource(R.drawable.profile)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    /* ========================= Follow Status ========================= */
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

                    if (viewedUserJson != null) {
                        updateUIWithProfile(viewedUserJson);
                    }
                },
                error -> Log.e(TAG, "Failed to load follow status", error)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(statusRequest);
    }

    /* ========================= Followers / Following ========================= */
    private void fetchFollowersAndFollowing(String targetUsername) {
        String followersUrl = "http://coms-3090-028.class.las.iastate.edu:8080/" + targetUsername + "/followers";
        JsonArrayRequest followersRequest = new JsonArrayRequest(
                Request.Method.GET,
                followersUrl,
                null,
                res -> followersTv.setText(res.length() + " Followers"),
                err -> followersTv.setText("0 Followers")
        );
        VolleySingleton.getInstance(this).addToRequestQueue(followersRequest);

        String followingUrl = "http://coms-3090-028.class.las.iastate.edu:8080/" + targetUsername + "/following";
        JsonArrayRequest followingRequest = new JsonArrayRequest(
                Request.Method.GET,
                followingUrl,
                null,
                res -> followingTv.setText(res.length() + " Following"),
                err -> followingTv.setText("0 Following")
        );
        VolleySingleton.getInstance(this).addToRequestQueue(followingRequest);
    }

    /* ========================= Follow / Unfollow ========================= */
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
                    Toast.makeText(this, "Error sending follow request", Toast.LENGTH_SHORT).show();
                    Log.e("FOLLOW_ERROR", "Request failed", error);
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
                res -> {
                    Toast.makeText(this, "Unfollowed user", Toast.LENGTH_SHORT).show();
                    currentState = FollowState.NOT_FOLLOWING;
                    updateFollowButton();
                },
                err -> Toast.makeText(this, "Error unfollowing user", Toast.LENGTH_SHORT).show()
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    /* ========================= UI Update ========================= */
    private void updateUIWithProfile(JSONObject userJson) {
        if (userJson == null) return;

        String displayName = userJson.optString("displayName", userJson.optString("username", ""));
        String username = userJson.optString("username", viewedUsername);
        String bio = userJson.optString("bio", "");
        String craftSpecialties = userJson.optString("craftSpecialties", "");

        displayNameTv.setText(displayName);
        usernameTv.setText(username);

        if (bio == null || bio.isEmpty() || bio.equals("null")) {
            bioTv.setVisibility(View.GONE);
        } else {
            bioTv.setVisibility(View.VISIBLE);
            bioTv.setText(bio);
        }

        if (craftSpecialties == null || craftSpecialties.isEmpty() || craftSpecialties.equals("null")) {
            craftSpecialtiesTv.setVisibility(View.GONE);
        } else {
            craftSpecialtiesTv.setVisibility(View.VISIBLE);
            craftSpecialtiesTv.setText(craftSpecialties);
        }

        long imageId = -1;
        JSONObject imgObj = userJson.optJSONObject("image");
        if (imgObj != null) {
            imageId = imgObj.optLong("id", -1);
        }

        loadOutsideProfileImage(profileImageView, imageId);
    }

    /* ========================= Follow Button UI ========================= */
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

    /* ========================= User Points ========================= */
    private void fetchUserPoints(String username, ImageView badge, TextView pointsTv, TextView tierTv, ProgressBar progressBar) {
        String url = "http://coms-3090-028.class.las.iastate.edu:8080/points/" + username;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                res -> {
                    try {
                        int totalPoints = res.optInt("totalPoints", 0);
                        String currentTier = res.optString("currentTier", "BEGINNER");
                        int pointsToNext = res.optInt("pointsToNextTier", 0);

                        pointsTv.setText("Points: " + totalPoints);
                        tierTv.setText("Tier: " + currentTier);

                        progressBar.setMax(100);
                        int progressValue;

                        if (pointsToNext == 0) {
                            progressValue = 100;
                        } else {
                            int required = currentTier.equals("BEGINNER") ? 100
                                    : currentTier.equals("INTERMEDIATE") ? 200
                                    : currentTier.equals("EXPERT") ? 300 : 0;

                            int lower = currentTier.equals("BEGINNER") ? 0
                                    : currentTier.equals("INTERMEDIATE") ? 100
                                    : currentTier.equals("EXPERT") ? 200 : 300;

                            progressValue = (int) ((totalPoints - lower) * 100.0 / (required - lower));
                        }

                        progressBar.setProgress(progressValue);

                        switch (currentTier) {
                            case "BEGINNER":
                                badge.setImageResource(R.drawable.badge_beginner);
                                break;
                            case "INTERMEDIATE":
                                badge.setImageResource(R.drawable.badge_intermediate);
                                break;
                            case "EXPERT":
                                badge.setImageResource(R.drawable.badge_expert);
                                break;
                            case "CHAMPION":
                                badge.setImageResource(R.drawable.badge_champion);
                                break;
                        }

                    } catch (Exception e) {
                        Log.e("POINTS", "Parse error", e);
                    }
                },
                err -> Log.e("POINTS", "Failed to fetch points", err)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}