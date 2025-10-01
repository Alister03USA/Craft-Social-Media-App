package com.example.androidexample;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class OutsideUserProfile extends AppCompatActivity {

    private Button followButton;
    private enum FollowState { NOT_FOLLOWING, PENDING, FOLLOWING }
    private FollowState currentState = FollowState.NOT_FOLLOWING;

    // Replace with your actual backend base URL
    private static final String BASE_URL = "https://f3669296-71cb-4a83-9a6f-76b5355210c2.mock.pstmn.io/FollowAnotherUser";

    // This would normally be passed in via Intent or loaded from backend
    private int viewedUserId = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outside_user_profile);

        followButton = findViewById(R.id.btn_follow);

        // TODO: fetch actual state from backend (NOT_FOLLOWING, PENDING, FOLLOWING)
        updateFollowButton();

        followButton.setOnClickListener(v -> {
            switch (currentState) {
                case NOT_FOLLOWING:
                    sendFollowRequest();
                    break;
                case FOLLOWING:
                    unfollowUser();
                    break;
                case PENDING:
                    Toast.makeText(this, "Request already sent", Toast.LENGTH_SHORT).show();
                    break;
            }
        });
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

    private void sendFollowRequest() {
        //String url = BASE_URL + "/followRequest";
        String url = BASE_URL;

        JSONObject body = new JSONObject();
        try {
            body.put("targetUserId", viewedUserId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    Toast.makeText(this, "Follow request sent", Toast.LENGTH_SHORT).show();
                    currentState = FollowState.PENDING;
                    updateFollowButton();
                },
                error -> {
                    Toast.makeText(this, "Error sending follow request", Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void unfollowUser() {
        String url = BASE_URL + "/unfollow";

        JSONObject body = new JSONObject();
        try {
            body.put("targetUserId", viewedUserId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    Toast.makeText(this, "Unfollowed user", Toast.LENGTH_SHORT).show();
                    currentState = FollowState.NOT_FOLLOWING;
                    updateFollowButton();
                },
                error -> {
                    Toast.makeText(this, "Error unfollowing user", Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}

