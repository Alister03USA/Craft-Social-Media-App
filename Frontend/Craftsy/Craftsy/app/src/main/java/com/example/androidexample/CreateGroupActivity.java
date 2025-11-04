package com.example.androidexample;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class CreateGroupActivity extends AppCompatActivity {

    private EditText groupNameInput, groupDescriptionInput, groupCraftInput;
    private Switch privateSwitch;
    private Button createButton;

    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        groupNameInput = findViewById(R.id.groupNameInput);
        groupDescriptionInput = findViewById(R.id.groupDescriptionInput);
        groupCraftInput = findViewById(R.id.groupCraftInput);
        privateSwitch = findViewById(R.id.privateSwitch);
        createButton = findViewById(R.id.createGroupButton);

        createButton.setOnClickListener(v -> {
            String groupName = groupNameInput.getText().toString().trim();
            if (groupName.isEmpty()) {
                Toast.makeText(this, "Please enter a group name", Toast.LENGTH_SHORT).show();
                return;
            }

            String username = SessionManager.getInstance().getLoggedInUsername();
            if (username == null || username.isEmpty()) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            createGroup(username);
        });
    }

    private void createGroup(String username) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("groupName", groupNameInput.getText().toString().trim());
            jsonBody.put("description", groupDescriptionInput.getText().toString().trim());
            jsonBody.put("craft", groupCraftInput.getText().toString().trim());
            jsonBody.put("isPrivate", privateSwitch.isChecked());
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to build request", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "/create/" + username;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {
                    Toast.makeText(this, "Group created!", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    Toast.makeText(this, "Group creation failed", Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}
