package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

/**
 * NewChatActivity
 * ----------------
 * Creates a new Direct Conversation using the backend endpoint:
 * POST /messages/create/{sender}/{receiver}
 *
 * ⚙️ Dev Mode: you can choose any test user from spinner,
 * but in production it will use the logged-in user as sender.
 */
public class NewChatActivity extends AppCompatActivity {

    private static final String TAG = "NewChatActivity";

    private Spinner spinner;
    private Button btnCreate;
    private String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);

        spinner = findViewById(R.id.spinnerOtherUser);
        btnCreate = findViewById(R.id.btnCreateChat);

        // 🧩 Grab current user (logged-in)
        currentUser = getIntent().getStringExtra("username");
        if (currentUser == null || currentUser.isEmpty()) {
            currentUser = ApiConfig.CURRENT_USERNAME; // fallback
        }
        Log.d(TAG, "👤 Current user (sender): " + currentUser);

        // 🧪 Test users list for development
        String[] users = {"Fuji", "Quinn", "alister_gan", "kkeck"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, users);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // 🚀 On "Create Chat" click
        btnCreate.setOnClickListener(v -> {
            String otherUser = spinner.getSelectedItem().toString();
            if (otherUser.equals(currentUser)) {
                Toast.makeText(this, "Cannot message yourself", Toast.LENGTH_SHORT).show();
                return;
            }
            createDirectChat(otherUser);
        });
    }

    /**
     * 🌐 Calls backend to create a new direct conversation
     * POST /messages/create/{sender}/{receiver}
     */
    private void createDirectChat(String otherUser) {
        String url = ApiConfig.BASE_URL + "/messages/create/" + currentUser + "/" + otherUser;
        Log.d(TAG, "🌍 POST " + url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null,
                response -> {
                    try {
                        Log.d(TAG, "✅ Response: " + response);
                        String convoId = response.optString("id", response.optString("convoId", ""));
                        if (convoId == null || convoId.isEmpty()) {
                            Toast.makeText(this, "No convo ID returned", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Log.d(TAG, "💬 New convo created: " + convoId);
                        Intent i = new Intent(this, DirectMessagingActivity.class);
                        i.putExtra("convoId", convoId);
                        i.putExtra("chatName", otherUser);
                        startActivity(i);
                        finish();
                    } catch (Exception e) {
                        Log.e(TAG, "💥 Error parsing convo creation response", e);
                        Toast.makeText(this, "Failed to parse server response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "❌ Failed to create chat: " + error.getMessage(), error);
                    Toast.makeText(this, "Server error creating chat", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }
}