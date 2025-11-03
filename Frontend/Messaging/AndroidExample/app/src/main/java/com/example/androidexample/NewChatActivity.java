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

public class NewChatActivity extends AppCompatActivity {

    private static final String TAG = "NewChatActivity";
    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";

    private Spinner spinner;
    private Button btnCreate;
    private String currentUser = "Fuji";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);

        spinner = findViewById(R.id.spinnerOtherUser);
        btnCreate = findViewById(R.id.btnCreateChat);

        String passedUser = getIntent().getStringExtra("username");
        if (passedUser != null && !passedUser.isEmpty()) currentUser = passedUser;
        Log.d(TAG, "👤 Current user (sender): " + currentUser);

        String[] users = {"Fuji", "Quinn", "alister_gan", "kkeck"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, users);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        btnCreate.setOnClickListener(v -> {
            String otherUser = spinner.getSelectedItem().toString();
            if (otherUser.equals(currentUser)) {
                Toast.makeText(this, "Cannot message yourself", Toast.LENGTH_SHORT).show();
                return;
            }
            createDirectChat(otherUser);
        });
    }

    private void createDirectChat(String otherUser) {
        String url = BASE_URL + "/messages/create/" + currentUser + "/" + otherUser;
        Log.d(TAG, "🌍 POST " + url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null,
                response -> {
                    String convoId = response.optString("id", "");
                    Log.d(TAG, "✅ Convo created: " + convoId);
                    Intent i = new Intent(this, DirectMessagingActivity.class);
                    i.putExtra("convoId", convoId);
                    i.putExtra("chatName", otherUser);
                    i.putExtra("username", currentUser);
                    startActivity(i);
                    finish();
                },
                error -> Toast.makeText(this, "Server error creating chat", Toast.LENGTH_SHORT).show());
        Volley.newRequestQueue(this).add(request);
    }
}