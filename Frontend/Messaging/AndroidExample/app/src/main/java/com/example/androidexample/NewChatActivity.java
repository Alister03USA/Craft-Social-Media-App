package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;

public class NewChatActivity extends AppCompatActivity {

    private Spinner spinner;
    private Button btnCreate;
    private String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);

        currentUser = getIntent().getStringExtra("username");
        spinner = findViewById(R.id.spinnerOtherUser);
        btnCreate = findViewById(R.id.btnCreateChat);

        // Test usernames
        String[] users = {"Fuji", "Quinn", "alister_gan", "kkeck"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, users);
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
        String url = "http://coms-3090-028.class.las.iastate.edu:8080/messages/create/"
                + currentUser + "/" + otherUser;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null,
                response -> {
                    try {
                        String convoId = response.getString("id");
                        Intent i = new Intent(this, DirectMessagingActivity.class);
                        i.putExtra("convoId", convoId);
                        i.putExtra("otherUser", otherUser);
                        startActivity(i);
                        finish();
                    } catch (Exception e) {
                        Toast.makeText(this, "Parse error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Failed to create chat", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }
}