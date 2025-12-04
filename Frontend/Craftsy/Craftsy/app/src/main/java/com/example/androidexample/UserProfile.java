package com.example.androidexample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;

public class UserProfile extends BaseActivity {

    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";

    private EditText displayName, username, bio, email, password, craftSpecialties;
    private ImageView editProfileImage;
    private byte[] newProfileImageData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showProfileView();
    }

    // ---------------------- VIEW MODE ----------------------
    private void showProfileView() {
        setContentView(R.layout.activity_user_profile);
        setupBottomNavigation(R.id.nav_my_profile);

        SessionManager session = SessionManager.getInstance();
        String usernameValue = session.getLoggedInUsername();
        String displayNameValue = session.getDisplayName() != null ? session.getDisplayName() : usernameValue;

        TextView usernameTv = findViewById(R.id.username);
        TextView displayNameTv = findViewById(R.id.displayName);
        TextView bioTv = findViewById(R.id.bio);
        TextView craftTv = findViewById(R.id.CraftSpecialties);
        ImageView profileImage = findViewById(R.id.profileImage);

        usernameTv.setText(usernameValue);
        displayNameTv.setText(displayNameValue);

        String bioStr = session.getBio();
        if (bioStr == null || bioStr.isEmpty() || bioStr.equals("null")) {
            bioTv.setVisibility(View.GONE);
        } else {
            bioTv.setVisibility(View.VISIBLE);
            bioTv.setText(bioStr);
        }

        String craftStr = session.getCraftSpecialties();
        if (craftStr == null || craftStr.isEmpty() || craftStr.equals("null")) {
            craftTv.setVisibility(View.GONE);
        } else {
            craftTv.setVisibility(View.VISIBLE);
            craftTv.setText(craftStr);
        }

        // -------- Corrected image loading using imageId --------
        long imageId = session.getProfileImageId();
        Log.d("PROFILE_DEBUG", "showProfileView imageId = " + imageId);
        loadProfileImage(profileImage, imageId);

        fetchFollowersAndFollowing(usernameValue);
        fetchUserPoints(usernameValue);
        loadUserPosts(usernameValue);

        findViewById(R.id.editProfile).setOnClickListener(v -> showEditProfile());
        findViewById(R.id.notifButton).setOnClickListener(v -> startActivity(new Intent(this, NotificationCenterActivity.class)));
        findViewById(R.id.btnPointsCenter).setOnClickListener(v -> startActivity(new Intent(this, PointsCenterActivity.class)));
        findViewById(R.id.btnSavedBoards).setOnClickListener(v -> {
            Intent i = new Intent(this, BoardsListActivity.class);
            i.putExtra("username", usernameValue);
            startActivity(i);
        });
    }

    private void loadProfileImage(ImageView profileImage, long imageId) {
        if (imageId <= 0) {
            Log.d("PROFILE_DEBUG", "No profile imageId stored. Using default pic.");
            profileImage.setImageResource(R.drawable.profile);
            return;
        }

        String url = BASE_URL + "/images/" + imageId;

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                res -> {
                    String filePath = res.optString("filePath", "");
                    if (filePath.isEmpty()) {
                        profileImage.setImageResource(R.drawable.profile);
                        return;
                    }

                    String filename = filePath.substring(filePath.lastIndexOf("/") + 1);
                    String fullUrl = BASE_URL + "/uploads/" + filename;

                    Log.d("PROFILE_DEBUG", "Loading profile image from: " + fullUrl);

                    Glide.with(this)
                            .load(fullUrl)
                            .placeholder(R.drawable.profile)
                            .circleCrop()
                            .into(profileImage);
                },
                err -> {
                    Log.e("PROFILE_DEBUG", "Error loading profile pic");
                    profileImage.setImageResource(R.drawable.profile);
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    // ---------------------- EDIT MODE ----------------------
    private void showEditProfile() {
        setContentView(R.layout.activity_user_profile_edit);

        SessionManager session = SessionManager.getInstance();

        displayName = findViewById(R.id.edit_display_name);
        username = findViewById(R.id.edit_username);
        bio = findViewById(R.id.edit_bio);
        email = findViewById(R.id.edit_email);
        password = findViewById(R.id.edit_password);
        craftSpecialties = findViewById(R.id.edit_craft_specialties);
        editProfileImage = findViewById(R.id.editProfileImage);

        username.setText(session.getLoggedInUsername());
        displayName.setText(session.getDisplayName());
        bio.setText(session.getBio());
        email.setText(session.getEmail());
        password.setText(session.getPassword());
        craftSpecialties.setText(session.getCraftSpecialties());

        loadProfileImage(editProfileImage, session.getProfileImageId());

        findViewById(R.id.btn_change_profile_image).setOnClickListener(v -> {
            Intent pick = new Intent(Intent.ACTION_PICK);
            pick.setType("image/*");
            startActivityForResult(pick, 101);
        });

        findViewById(R.id.btn_save_profile).setOnClickListener(v -> saveProfile());

        findViewById(R.id.logout).setOnClickListener(v -> {
            session.logout();
            Intent intent = new Intent(UserProfile.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    // ------------------ SAVE PROFILE ------------------
    private void saveProfile() {
        JSONObject data = new JSONObject();
        try {
            data.put("displayName", displayName.getText().toString().trim());
            data.put("username", username.getText().toString().trim());
            data.put("bio", bio.getText().toString().trim());
            data.put("craftSpecialties", craftSpecialties.getText().toString().trim());
            data.put("email", email.getText().toString().trim());
            data.put("password", password.getText().toString().trim());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (newProfileImageData != null) {
            uploadProfileImageThenUpdate(data);
        } else {
            sendProfileUpdate(data);
        }
    }

    private void uploadProfileImageThenUpdate(JSONObject data) {
        MultipartRequest upload = new MultipartRequest(
                Request.Method.POST,
                BASE_URL + "/images",
                "image",
                "profile.jpg",
                "image/jpeg",
                newProfileImageData,
                res -> {
                    try {
                        JSONObject obj = new JSONObject(res);
                        long id = obj.getLong("id");
                        String filePath = obj.getString("filePath");

                        JSONObject img = new JSONObject();
                        img.put("id", id);
                        img.put("filePath", filePath);

                        data.put("image", img);
                        sendProfileUpdate(data);

                    } catch (Exception ex) {
                        Toast.makeText(this, "Image upload parse error", Toast.LENGTH_SHORT).show();
                    }
                },
                err -> Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show()
        );

        VolleySingleton.getInstance(this).addToRequestQueue(upload);
    }

    private void sendProfileUpdate(JSONObject data) {
        JSONObject filtered = new JSONObject();

        Iterator<String> keys = data.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = data.opt(key);

            if (value == null) continue;
            if (value instanceof String && ((String) value).isEmpty()) continue;

            try {
                filtered.put(key, value);
            } catch (JSONException ignored) {}
        }

        String url = BASE_URL + "/user/" + SessionManager.getInstance().getLoggedInUsername();

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                filtered,
                res -> {
                    updateSessionData(res);
                    Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show();
                    showProfileView();
                },
                err -> Toast.makeText(this, "Update failed", Toast.LENGTH_LONG).show()
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void updateSessionData(JSONObject res) {
        SessionManager s = SessionManager.getInstance();

        s.setLoggedInUsername(res.optString("username"));
        s.setDisplayName(res.optString("displayName"));
        s.setBio(res.optString("bio"));
        s.setEmail(res.optString("email"));
        s.setPassword(res.optString("password"));
        s.setCraftSpecialties(res.optString("craftSpecialties"));

        JSONObject img = res.optJSONObject("image");
        if (img != null) {
            long id = img.optLong("id", -1);
            if (id > 0) s.setProfileImageId(id);
        }
    }

    // ------------------ IMAGE PICKER ------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            Glide.with(this)
                    .load(imageUri)
                    .placeholder(R.drawable.profile)
                    .into(editProfileImage);

            Glide.with(this)
                    .asBitmap()
                    .load(imageUri)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            resource.compress(Bitmap.CompressFormat.JPEG, 90, baos);
                            newProfileImageData = baos.toByteArray();
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {}
                    });
        }
    }

    // ---------------------- FOLLOWERS ----------------------
    private void fetchFollowersAndFollowing(String username) {
        String followersUrl = BASE_URL + "/" + username + "/followers";
        String followingUrl = BASE_URL + "/" + username + "/following";

        JsonArrayRequest followersReq = new JsonArrayRequest(
                Request.Method.GET,
                followersUrl,
                null,
                res -> ((TextView) findViewById(R.id.followersCount)).setText(res.length() + " Followers"),
                err -> ((TextView) findViewById(R.id.followersCount)).setText("0 Followers")
        );

        JsonArrayRequest followingReq = new JsonArrayRequest(
                Request.Method.GET,
                followingUrl,
                null,
                res -> ((TextView) findViewById(R.id.followingCount)).setText(res.length() + " Following"),
                err -> ((TextView) findViewById(R.id.followingCount)).setText("0 Following")
        );

        VolleySingleton.getInstance(this).addToRequestQueue(followersReq);
        VolleySingleton.getInstance(this).addToRequestQueue(followingReq);
    }

    // ---------------------- POSTS ----------------------
    private void loadUserPosts(String username) {
        GridLayout grid = findViewById(R.id.postsGrid);
        grid.removeAllViews();

        String url = BASE_URL + "/feed/home/" + username;

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                res -> {
                    for (int i = 0; i < res.length(); i++) {

                        JSONObject post = res.optJSONObject(i);
                        if (post == null) continue;

                        JSONArray images = post.optJSONArray("images");
                        if (images == null || images.length() == 0) continue;

                        long imageId = images.optJSONObject(0).optLong("id", -1);
                        if (imageId <= 0) continue;

                        ImageView iv = new ImageView(this);
                        int size = getResources().getDisplayMetrics().widthPixels / 3;
                        iv.setLayoutParams(new GridLayout.LayoutParams());
                        iv.getLayoutParams().width = size;
                        iv.getLayoutParams().height = size;
                        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);

                        grid.addView(iv);
                        loadPostImage(iv, imageId);

                        String projectName = post.optString("projectName", null);
                        iv.setOnClickListener(v -> {
                            Intent intent = new Intent(UserProfile.this, UserPostsDetailActivity.class);
                            intent.putExtra("projectName", projectName);
                            intent.putExtra("username", username);
                            startActivity(intent);
                        });
                    }

                    if (grid.getChildCount() == 0) {
                        TextView msg = new TextView(this);
                        msg.setText("No posts yet.");
                        msg.setTextSize(16f);
                        msg.setPadding(0, 24, 0, 24);
                        grid.addView(msg);
                    }
                },
                err -> Toast.makeText(this, "Failed to load posts", Toast.LENGTH_SHORT).show()
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void loadPostImage(ImageView iv, long imageId) {
        String url = BASE_URL + "/images/" + imageId;

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                res -> {
                    String filePath = res.optString("filePath", "");
                    if (filePath.isEmpty()) {
                        iv.setImageResource(R.drawable.ic_post_placeholder);
                        return;
                    }

                    String filename = filePath.substring(filePath.lastIndexOf("/") + 1);
                    String fullUrl = BASE_URL + "/uploads/" + filename;

                    Glide.with(this)
                            .load(fullUrl)
                            .centerCrop()
                            .into(iv);
                },
                err -> iv.setImageResource(R.drawable.ic_post_placeholder)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    // ------------------ POINTS ------------------
    private void fetchUserPoints(String username) {
        String url = BASE_URL + "/points/" + username;

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                res -> {
                    try {
                        int total = res.optInt("totalPoints", 0);
                        String tier = res.optString("currentTier", "BEGINNER");

                        ((TextView) findViewById(R.id.pointsText)).setText("Points: " + total);
                        ((TextView) findViewById(R.id.tierText)).setText("Tier: " + tier);

                        ProgressBar bar = findViewById(R.id.tierProgress);
                        int progress = 0;

                        if (tier.equals("BEGINNER")) progress = (int) (total / 100.0 * 100);
                        else if (tier.equals("INTERMEDIATE")) progress = (int) ((total - 100) / 100.0 * 100);
                        else if (tier.equals("EXPERT")) progress = (int) ((total - 200) / 100.0 * 100);
                        else if (tier.equals("CHAMPION")) progress = 100;

                        bar.setProgress(progress);

                        ImageView badge = findViewById(R.id.tierBadge);

                        if (tier.equals("BEGINNER")) badge.setImageResource(R.drawable.badge_beginner);
                        else if (tier.equals("INTERMEDIATE")) badge.setImageResource(R.drawable.badge_intermediate);
                        else if (tier.equals("EXPERT")) badge.setImageResource(R.drawable.badge_expert);
                        else badge.setImageResource(R.drawable.badge_champion);

                    } catch (Exception ignored) {}
                },
                err -> Log.e("POINTS_ERROR", "Failed to load points")
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }
}