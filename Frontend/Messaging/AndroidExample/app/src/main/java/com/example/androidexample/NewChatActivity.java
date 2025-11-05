package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NewChatActivity extends AppCompatActivity {

    private static final String TAG = "NewChatActivity";
    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";

    private AutoCompleteTextView autoUserSearch;
    private TextView tvSelectedUsers;
    private Button btnCreateDirect, btnCreateGroup;
    private String currentUser = "Fuji";

    private ArrayList<String> userList = new ArrayList<>();
    private ArrayList<String> selectedUsers = new ArrayList<>();
    private ArrayAdapter<String> userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);

        autoUserSearch = findViewById(R.id.autoUserSearch);
        tvSelectedUsers = findViewById(R.id.tvSelectedUsers);
        btnCreateDirect = findViewById(R.id.btnCreateDirect);
        btnCreateGroup = findViewById(R.id.btnCreateGroup);

        // Set current user (from intent or fallback)
        String passedUser = getIntent().getStringExtra("username");
        if (passedUser != null && !passedUser.isEmpty()) currentUser = passedUser;
        Log.d(TAG, "👤 Current user (sender): " + currentUser);

        // Setup adapter for user search
        userAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userList);
        autoUserSearch.setAdapter(userAdapter);
        autoUserSearch.setThreshold(1);

        // Handle focus to reopen dropdown
        autoUserSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && !userList.isEmpty()) {
                autoUserSearch.showDropDown();
            }
        });

        // Text change listener
        autoUserSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString().trim();
                if (text.length() >= 2) searchUsers(text);
            }
        });

        // When a username is selected
        autoUserSearch.setOnItemClickListener((parent, view, position, id) -> {
            String selected = userList.get(position);
            if (!selectedUsers.contains(selected) && !selected.equals(currentUser)) {
                selectedUsers.add(selected);
                updateSelectedUsers();
                Log.d(TAG, "✅ Added user: " + selected);
            }
            autoUserSearch.setText("");
            hideKeyboard();
        });

        // Create direct chat button
        btnCreateDirect.setOnClickListener(v -> {
            if (selectedUsers.size() != 1) {
                Toast.makeText(this, "Select exactly one user for a direct chat", Toast.LENGTH_SHORT).show();
                return;
            }
            createDirectChat(selectedUsers.get(0));
        });

        // Create group chat button
        btnCreateGroup.setOnClickListener(v -> {
            if (selectedUsers.size() < 2) {
                Toast.makeText(this, "Select at least 2 users for a group chat", Toast.LENGTH_SHORT).show();
                return;
            }
            createGroupChat(selectedUsers);
        });
    }

    private void updateSelectedUsers() {
        tvSelectedUsers.setText("Selected: " + String.join(", ", selectedUsers));
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(autoUserSearch.getWindowToken(), 0);
    }

    private void searchUsers(String query) {
        String url = BASE_URL + "/search/user?query=" + query;
        Log.d(TAG, "🔍 Searching users: " + url);

        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null,
                res -> {
                    userList.clear();
                    for (int i = 0; i < res.length(); i++) {
                        JSONObject obj = res.optJSONObject(i);
                        if (obj != null) {
                            String username = obj.optString("username");
                            if (!username.equals(currentUser)) userList.add(username);
                        }
                    }
                    userAdapter.notifyDataSetChanged();
                    if (!userList.isEmpty()) autoUserSearch.showDropDown();
                    Log.d(TAG, "✅ Found users: " + userList);
                },
                err -> {
                    Log.e(TAG, "❌ Search error", err);
                    Toast.makeText(this, "Search failed", Toast.LENGTH_SHORT).show();
                });
        Volley.newRequestQueue(this).add(req);
    }

    private void createDirectChat(String otherUser) {
        String url = BASE_URL + "/messages/create/" + currentUser + "/" + otherUser;
        Log.d(TAG, "🌍 POST " + url);

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, null,
                res -> {
                    String convoId = res.optString("id", "");
                    Log.d(TAG, "✅ Direct chat created: " + convoId);
                    openChat(convoId, otherUser);
                },
                err -> {
                    Log.e(TAG, "❌ Failed to create direct chat", err);
                    Toast.makeText(this, "Failed to create chat", Toast.LENGTH_SHORT).show();
                });
        Volley.newRequestQueue(this).add(req);
    }

    private void createGroupChat(List<String> members) {
        String url = BASE_URL + "/messages/group";
        JSONArray body = new JSONArray(members);
        Log.d(TAG, "🌍 POST " + url + " with body: " + body);

        JsonArrayRequest req = new JsonArrayRequest(Request.Method.POST, url, body,
                res -> {
                    try {
                        JSONObject groupObj = res.getJSONObject(0); // Expecting group response
                        String convoId = groupObj.optString("id", "");
                        Log.d(TAG, "✅ Group chat created: " + convoId);
                        openChat(convoId, "New Group Chat");
                    } catch (Exception e) {
                        Log.e(TAG, "⚠️ Parse error", e);
                        Toast.makeText(this, "Group created but failed to parse ID", Toast.LENGTH_SHORT).show();
                    }
                },
                err -> {
                    Log.e(TAG, "❌ Failed to create group chat", err);
                    Toast.makeText(this, "Group chat creation failed", Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(this).add(req);
    }

    private void openChat(String convoId, String chatName) {
        Intent i = new Intent(this, DirectMessagingActivity.class);
        i.putExtra("convoId", convoId);
        i.putExtra("chatName", chatName);
        i.putExtra("username", currentUser);
        startActivity(i);
        finish();
    }
}