package com.example.androidexample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

public class UserProfile extends BaseActivity {

    private EditText displayName, username, bio, email, password, craftSpecialties;


    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showProfileView();
    }

    /** ------------------- FETCH FOLLOWERS / FOLLOWING COUNTS ------------------- **/
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


    /** ------------------- VIEW MODE ------------------- **/
    private void showProfileView() {
        setContentView(R.layout.activity_user_profile);
        setupBottomNavigation(R.id.nav_my_profile);

        TextView usernameTv = findViewById(R.id.username);
        TextView displayNameTv = findViewById(R.id.displayName);
        TextView bioTv = findViewById(R.id.bio);
        TextView followersTv = findViewById(R.id.followersCount);
        TextView followingTv = findViewById(R.id.followingCount);
        TextView craftSpecialtiesTv = findViewById(R.id.CraftSpecialties);

        SessionManager session = SessionManager.getInstance();

        String usernameValue = session.getLoggedInUsername();
        String displayNameValue = session.getDisplayName();
        if (displayNameValue == null || displayNameValue.isEmpty()) displayNameValue = usernameValue;

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

        fetchFollowersAndFollowing(usernameValue);

        // 🔹 NEW: Load posts thumbnails grid for this user
        loadUserPosts(usernameValue);

        Button editButton = findViewById(R.id.editProfile);
        Button notifButton = findViewById(R.id.notifButton);
        notifButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfile.this, NotificationCenterActivity.class);
            startActivity(intent);
        });

        editButton.setOnClickListener(v -> showEditProfile());

    }

    /** ------------------- LOAD USER POSTS ------------------- **/
    private void loadUserPosts(String username) {
        GridLayout postsGrid = findViewById(R.id.postsGrid);
        postsGrid.removeAllViews();

        String url = BASE_URL + "/feed/" + username;
        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject post = response.optJSONObject(i);
                        if (post == null) continue;

                        ImageView imageView = new ImageView(this);
                        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                        params.width = getResources().getDisplayMetrics().widthPixels / 3;
                        params.height = params.width;
                        imageView.setLayoutParams(params);
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        imageView.setBackgroundColor(getResources().getColor(R.color.blue_background));
                        postsGrid.addView(imageView);

                        JSONArray images = post.optJSONArray("images");
                        if (images != null && images.length() > 0) {
                            JSONObject img = images.optJSONObject(0);
                            if (img != null) {
                                long imageId = img.optLong("id", -1);
                                if (imageId > 0) {
                                    loadImageIntoView(imageView, imageId);
                                }
                            }
                        }

                        String projectName = post.optString("projectName", "");

                        // 🔹 Updated click listener to open UserPostsDetailActivity
                        imageView.setOnClickListener(v -> {
                            long postId = post.optLong("id", -1); // get the post's ID
                            if (postId != -1) {
                                Intent intent = new Intent(UserProfile.this, UserPostsDetailActivity.class);
                                intent.putExtra("postId", postId); // pass the post ID
                                startActivity(intent);
                            } else {
                                Toast.makeText(UserProfile.this, "Post not found", Toast.LENGTH_SHORT).show();
                            }
                        });

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


    /** ------------------- EDIT MODE ------------------- **/
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
        if (displayNameValue == null || displayNameValue.isEmpty()) displayNameValue = usernameValue;

        username.setText(usernameValue);
        displayName.setText(displayNameValue);
        bio.setText(session.getBio());
        email.setText(session.getEmail());
        password.setText(session.getPassword());
        craftSpecialties.setText(session.getCraftSpecialties());

        Button saveButton = findViewById(R.id.btn_save_profile);
        saveButton.setOnClickListener(v -> saveProfile());

        Button logout = findViewById(R.id.logout);
        logout.setOnClickListener(v -> {
            session.logout();
            Intent intent = new Intent(UserProfile.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    /** ------------------- SAVE PROFILE ------------------- **/
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

        String url = BASE_URL + "/user/" + SessionManager.getInstance().getLoggedInUsername();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                profileData,
                response -> {
                    Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();

                    SessionManager session = SessionManager.getInstance();
                    session.setLoggedInUsername(user);
                    session.setDisplayName(name);
                    session.setBio(biography);
                    session.setEmail(mail);
                    session.setPassword(pass);
                    session.setCraftSpecialties(craftType);

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
}
