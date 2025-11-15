package com.example.androidexample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.ByteArrayOutputStream;


import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

public class UserProfile extends BaseActivity {

    private EditText displayName, username, bio, email, password, craftSpecialties;
    private ImageView editProfileImage;
    private byte[] newProfileImageData = null; // holds image bytes if user selects a new image


    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showProfileView();
    }

    /**
     * ------------------- FETCH FOLLOWERS / FOLLOWING COUNTS -------------------
     **/
    private void fetchFollowersAndFollowing(String username) {
        String followersUrl = BASE_URL + "/" + username + "/followers";
        JsonArrayRequest followersRequest = new JsonArrayRequest(
                Request.Method.GET, followersUrl, null,
                response -> {
                    TextView followersTv = findViewById(R.id.followersCount);
                    followersTv.setText(response.length() + " Followers");
                },
                error -> {
                    TextView followersTv = findViewById(R.id.followersCount);
                    followersTv.setText("0 Followers");
                }
        );
        VolleySingleton.getInstance(this).addToRequestQueue(followersRequest);

        String followingUrl = BASE_URL + "/" + username + "/following";
        JsonArrayRequest followingRequest = new JsonArrayRequest(
                Request.Method.GET, followingUrl, null,
                response -> {
                    TextView followingTv = findViewById(R.id.followingCount);
                    followingTv.setText(response.length() + " Following");
                },
                error -> {
                    TextView followingTv = findViewById(R.id.followingCount);
                    followingTv.setText("0 Following");
                }
        );
        VolleySingleton.getInstance(this).addToRequestQueue(followingRequest);
    }


    /**
     * ------------------- VIEW MODE -------------------
     **/
    private void showProfileView() {
        setContentView(R.layout.activity_user_profile);
        setupBottomNavigation(R.id.nav_my_profile);

        TextView usernameTv = findViewById(R.id.username);
        TextView displayNameTv = findViewById(R.id.displayName);
        TextView bioTv = findViewById(R.id.bio);
        TextView followersTv = findViewById(R.id.followersCount);
        TextView followingTv = findViewById(R.id.followingCount);
        TextView craftSpecialtiesTv = findViewById(R.id.CraftSpecialties);
        ImageView profileImageView = findViewById(R.id.profileImage); // NEW: profile image
        ImageView tierBadge = findViewById(R.id.tierBadge);
        TextView pointsText = findViewById(R.id.pointsText);
        TextView tierText = findViewById(R.id.tierText);
        ProgressBar tierProgress = findViewById(R.id.tierProgress);

        SessionManager session = SessionManager.getInstance();

        String usernameValue = session.getLoggedInUsername();
        String displayNameValue = session.getDisplayName();
        if (displayNameValue == null || displayNameValue.isEmpty())
            displayNameValue = usernameValue;

        usernameTv.setText(usernameValue);
        displayNameTv.setText(displayNameValue);

        String bio = session.getBio();
        String craftSpecialties = session.getCraftSpecialties();

        if (bio == null || bio.equals("null") || bio.isEmpty()) {
            bioTv.setVisibility(View.GONE);
        } else {
            bioTv.setVisibility(View.VISIBLE);
            bioTv.setText(bio);
        }

        if (craftSpecialties == null || craftSpecialties.equals("null") || craftSpecialties.isEmpty()) {
            craftSpecialtiesTv.setVisibility(View.GONE);
        } else {
            craftSpecialtiesTv.setVisibility(View.VISIBLE);
            craftSpecialtiesTv.setText(craftSpecialties);
        }

        // 🔹 NEW: Load profile image from session
        String profileImageFilename = session.getProfileImageUrl();
        if (profileImageFilename != null && !profileImageFilename.isEmpty()) {
            String fullUrl = BASE_URL + "/uploads/" + profileImageFilename;
            Glide.with(this)
                    .load(fullUrl)
                    .placeholder(R.drawable.profile)
                    .error(R.drawable.profile)
                    .circleCrop()
                    .into(profileImageView);
        } else {
            profileImageView.setImageResource(R.drawable.profile);
        }

        fetchFollowersAndFollowing(usernameValue);
        fetchUserPoints(usernameValue);

        // 🔹 NEW: Load posts thumbnails grid for this user
        loadUserPosts(usernameValue);

        Button editButton = findViewById(R.id.editProfile);
        Button notifButton = findViewById(R.id.notifButton);
        Button pointsCenterBtn = findViewById(R.id.btnPointsCenter);

        notifButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfile.this, NotificationCenterActivity.class);
            startActivity(intent);
        });

        pointsCenterBtn.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfile.this, PointsCenterActivity.class);
            startActivity(intent);
        });

        editButton.setOnClickListener(v -> showEditProfile());
    }

    /**
     * ------------------- LOAD USER POSTS -------------------
     **/
    private void loadUserPosts(String username) {
        GridLayout postsGrid = findViewById(R.id.postsGrid);
        postsGrid.removeAllViews();

        String url = BASE_URL + "/feed/home/" + username; // only user's posts

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject post = response.optJSONObject(i);
                        if (post == null) continue;

                        JSONArray images = post.optJSONArray("images");
                        if (images == null || images.length() == 0) {
                            // Skip posts with no images to avoid blank spaces
                            continue;
                        }

                        JSONObject img = images.optJSONObject(0);
                        if (img == null) continue;
                        long imageId = img.optLong("id", -1);
                        if (imageId <= 0) continue;

                        // Create ImageView only if there is an image
                        ImageView imageView = new ImageView(this);
                        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                        params.width = getResources().getDisplayMetrics().widthPixels / 3;
                        params.height = params.width;
                        imageView.setLayoutParams(params);
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        imageView.setBackgroundColor(getResources().getColor(R.color.blue_background));
                        postsGrid.addView(imageView);
                        String projectName = post.optString("projectName", null);


                        // Load image
                        loadImageIntoView(imageView, imageId);

                        // Click listener
                        imageView.setOnClickListener(v -> {


                            // Inside imageView.setOnClickListener(...)
                            Intent intent = new Intent(UserProfile.this, UserPostsDetailActivity.class);
                            intent.putExtra("projectName", projectName);
                            intent.putExtra("username", username); // keep this!

                            startActivity(intent);


                        });
                    }

                    // If no posts were added, show a placeholder or message
                    if (postsGrid.getChildCount() == 0) {
                        TextView emptyMsg = new TextView(this);
                        emptyMsg.setText("No posts yet.");
                        emptyMsg.setTextSize(16f);
                        emptyMsg.setPadding(0, 24, 0, 24);
                        postsGrid.addView(emptyMsg);
                    }

                },
                error -> Toast.makeText(this, "Failed to load posts", Toast.LENGTH_SHORT).show()
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }


    private void loadImageIntoView(ImageView imageView, long imageId) {
        String url = BASE_URL + "/images/" + imageId; // first get metadata

        JsonObjectRequest metadataRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    String filePath = response.optString("filePath", "");
                    if (filePath != null && !filePath.isEmpty()) {
                        String filename = filePath.substring(filePath.lastIndexOf("/") + 1);
                        String fullUrl = BASE_URL + "/uploads/" + filename;

                        // ✅ Load image with Glide
                        Glide.with(this)
                                .load(fullUrl)
                                .placeholder(R.drawable.ic_post_placeholder)
                                .error(R.drawable.ic_post_placeholder)
                                .centerCrop()
                                .into(imageView);
                    } else {
                        imageView.setImageResource(R.drawable.ic_post_placeholder);
                    }
                },
                error -> imageView.setImageResource(R.drawable.ic_post_placeholder)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(metadataRequest);
    }


    /**
     * ------------------- EDIT MODE -------------------
     **/
    private void showEditProfile() {
        setContentView(R.layout.activity_user_profile_edit);

        displayName = findViewById(R.id.edit_display_name);
        username = findViewById(R.id.edit_username);
        bio = findViewById(R.id.edit_bio);
        email = findViewById(R.id.edit_email);
        password = findViewById(R.id.edit_password);
        craftSpecialties = findViewById(R.id.edit_craft_specialties);

        SessionManager session = SessionManager.getInstance();

        String usernameValue = session.getLoggedInUsername();
        String displayNameValue = session.getDisplayName();
        if (displayNameValue == null || displayNameValue.isEmpty())
            displayNameValue = usernameValue;

        username.setText(usernameValue);
        displayName.setText(displayNameValue);
        bio.setText(session.getBio());
        email.setText(session.getEmail());
        password.setText(session.getPassword());
        craftSpecialties.setText(session.getCraftSpecialties());

        Button saveButton = findViewById(R.id.btn_save_profile);
        saveButton.setOnClickListener(v -> saveProfile());
        editProfileImage = findViewById(R.id.editProfileImage);
        Button changeImageButton = findViewById(R.id.btn_change_profile_image);

// Load current profile image from session or default
        String currentImageUrl = session.getProfileImageUrl(); // make sure SessionManager has this
        if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(BASE_URL + "/uploads/" + currentImageUrl)
                    .placeholder(R.drawable.profile)
                    .into(editProfileImage);
        } else {
            editProfileImage.setImageResource(R.drawable.profile);
        }

// Handle picking new image
        changeImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 101); // 101 = request code
        });


        Button logout = findViewById(R.id.logout);
        logout.setOnClickListener(v -> {
            session.logout();
            Intent intent = new Intent(UserProfile.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    /**
     * ------------------- SAVE PROFILE -------------------
     **/
    private void saveProfile() {
        String name = displayName.getText().toString().trim();
        String user = username.getText().toString().trim();
        String biography = bio.getText().toString().trim();
        String mail = email.getText().toString().trim();
        String pass = password.getText().toString().trim();
        String craftType = craftSpecialties.getText().toString().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Username and password are required!", Toast.LENGTH_LONG).show();
            return;
        }

        JSONObject profileData = new JSONObject();
        try {
            profileData.put("displayName", name);
            profileData.put("username", user);
            profileData.put("bio", biography);
            profileData.put("craftSpecialties", craftType);
            profileData.put("email", mail);
            profileData.put("password", pass);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating profile JSON", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newProfileImageData != null) {
            MultipartRequest uploadRequest = new MultipartRequest(
                    Request.Method.POST,
                    BASE_URL + "/images",
                    "image",
                    "profile.jpg",
                    "image/jpeg",
                    newProfileImageData,
                    response -> {
                        Log.d("UPLOAD_RESPONSE", response);
                        try {
                            if (!response.trim().startsWith("{")) {
                                Toast.makeText(this, "Unexpected response from server", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            JSONObject res = new JSONObject(response);
                            String filePath = res.getString("filePath");
                            String filename = filePath.substring(filePath.lastIndexOf("/") + 1);
                            profileData.put("image", filename);
                            sendProfileUpdate(profileData); // only once
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Failed to parse image upload response", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
            );
            VolleySingleton.getInstance(this).addToRequestQueue(uploadRequest);
        } else {
            sendProfileUpdate(profileData); // no image, just send JSON
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                // Get URI of selected image
                Uri imageUri = data.getData();

                // Load into ImageView with Glide
                Glide.with(this)
                        .asBitmap() // ensures we get a Bitmap if needed later
                        .load(imageUri)
                        .placeholder(R.drawable.profile)
                        .into(editProfileImage);

                // Optional: get byte[] if you need to upload it
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
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                            }
                        });

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendProfileUpdate(JSONObject profileData) {
        JSONObject filteredProfileData = new JSONObject();
        Iterator<String> keys = profileData.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (key == null) continue; // skip null keys
            Object value = profileData.opt(key); // use opt() to preserve type
            try {
                if (value == null || value.equals("null") || (value instanceof String && ((String) value).isEmpty())) {
                    // Skip empty values OR store as JSONObject.NULL if you want
                    // filteredProfileData.put(key, JSONObject.NULL);
                    continue;
                } else {
                    filteredProfileData.put(key, value);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        String url = BASE_URL + "/user/" + SessionManager.getInstance().getLoggedInUsername();
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                filteredProfileData,
                response -> {
                    Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();
                    SessionManager session = SessionManager.getInstance();
                    session.setLoggedInUsername(filteredProfileData.optString("username"));
                    session.setDisplayName(filteredProfileData.optString("displayName"));
                    session.setBio(filteredProfileData.optString("bio"));
                    session.setEmail(filteredProfileData.optString("email"));
                    session.setPassword(filteredProfileData.optString("password"));
                    session.setCraftSpecialties(filteredProfileData.optString("craftSpecialties"));
                    session.setProfileImageUrl(filteredProfileData.optString("image"));
                    showProfileView();
                },
                error -> {
                    String message = "Server error saving profile";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        message = new String(error.networkResponse.data);
                    }
                    Toast.makeText(this, "Error saving profile: " + message, Toast.LENGTH_LONG).show();
                }
        );
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }


    private void fetchUserPoints(String username) {
        String url = BASE_URL + "/points/" + username;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        int totalPoints = response.optInt("totalPoints", 0);
                        String currentTier = response.optString("currentTier", "BEGINNER");
                        int pointsToNextTier = response.optInt("pointsToNextTier", 0);

                        TextView pointsText = findViewById(R.id.pointsText);
                        TextView tierText = findViewById(R.id.tierText);
                        ProgressBar tierProgress = findViewById(R.id.tierProgress);
                        ImageView tierBadge = findViewById(R.id.tierBadge);

                        pointsText.setText("Points: " + totalPoints);
                        tierText.setText("Tier: " + currentTier);

                        int progress = 0;
                        if (currentTier.equals("BEGINNER")) {
                            progress = (int) ((totalPoints / 100.0) * 100);
                        } else if (currentTier.equals("INTERMEDIATE")) {
                            progress = (int) (((totalPoints - 100) / 100.0) * 100);
                        } else if (currentTier.equals("EXPERT")) {
                            progress = (int) (((totalPoints - 200) / 100.0) * 100);
                        } else if (currentTier.equals("CHAMPION")) {
                            progress = 100;
                        }
                        tierProgress.setProgress(progress);

                        switch (currentTier) {
                            case "BEGINNER":
                                tierBadge.setImageResource(R.drawable.badge_beginner);
                                break;
                            case "INTERMEDIATE":
                                tierBadge.setImageResource(R.drawable.badge_intermediate);
                                break;
                            case "EXPERT":
                                tierBadge.setImageResource(R.drawable.badge_expert);
                                break;
                            case "CHAMPION":
                                tierBadge.setImageResource(R.drawable.badge_champion);
                                break;
                            default:
                                tierBadge.setImageResource(R.drawable.badge_beginner);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.e("POINTS_ERROR", "Failed to load points", error)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}