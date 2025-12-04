package com.example.androidexample;

import android.os.Bundle;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;

public class DirectMessagingActivity extends BaseMessagingActivity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_direct_message);

        convoId = getIntent().getStringExtra("convoId");
        chatName = getIntent().getStringExtra("chatName");
        currentUser = getIntent().getStringExtra("username");

        String otherUser = getIntent().getStringExtra("otherUser");
        long profileId = getIntent().getLongExtra("profileImageId", -1);

        setupBaseViews();
        setupRecycler();
        setupInput();

        tvTitle.setText(chatName);

        ImageView back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> finish());

        // Load direct chat profile pic
        if (profileId > 0) {
            loadProfileImageById(profileId);
        } else {
            fetchProfile(otherUser);
            topProfileImage.setImageResource(R.drawable.profile);
        }

        fetchHistory();
        connectSocket();
    }

    private void loadProfileImageById(long imgId) {
        String url = BASE_URL + "/images/" + imgId;

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                res -> {
                    try {
                        String fp = res.optString("filePath", "");
                        if (!fp.isEmpty()) {
                            String name = fp.substring(fp.lastIndexOf("/") + 1);
                            String full = BASE_URL + "/uploads/" + name;

                            Glide.with(this)
                                    .load(full)
                                    .circleCrop()
                                    .into(topProfileImage);
                        }
                    } catch (Exception ignored) {}
                },
                err -> {}
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }
}