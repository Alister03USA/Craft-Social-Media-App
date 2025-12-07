package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
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

    private String currentUser;

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

        /* ====================== GET CURRENT USER ====================== */
        currentUser = getIntent().getStringExtra("username");
        if (currentUser == null || currentUser.isEmpty()) {
            currentUser = SessionManager.getInstance().getLoggedInUsername();
        }
        Log.d(TAG, "Current logged-in user: " + currentUser);

        /* ====================== SETUP ADAPTER ====================== */
        userAdapter = new ArrayAdapter<>(this, R.layout.dropdown_item, R.id.dropdown_text, userList);
        autoUserSearch.setAdapter(userAdapter);
        autoUserSearch.setThreshold(1);

        /* ====================== USER SEARCH LISTENER ====================== */
        autoUserSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString().trim();
                if (text.length() >= 2) searchUsers(text);
            }
        });

        /* ====================== ON USER PICK ====================== */
        autoUserSearch.setOnItemClickListener((parent, view, position, id) -> {
            String selected = userAdapter.getItem(position);
            if (selected == null) return;

            if (!selected.equals(currentUser) && !selectedUsers.contains(selected)) {
                selectedUsers.add(selected);
                updateSelectedUsers();
                Log.d(TAG, "Added user: " + selected);
            }

            autoUserSearch.setText("");
            hideKeyboard();
        });

        /* ====================== CREATE DIRECT ====================== */
        btnCreateDirect.setOnClickListener(v -> {
            if (selectedUsers.size() != 1) {
                Toast.makeText(this, "Select exactly one user for a direct chat", Toast.LENGTH_SHORT).show();
                return;
            }
            createDirectChat(selectedUsers.get(0));
        });

        /* ====================== CREATE GROUP ====================== */
        btnCreateGroup.setOnClickListener(v -> {
            if (selectedUsers.size() < 2) {
                Toast.makeText(this, "Select at least 2 users for a group chat", Toast.LENGTH_SHORT).show();
                return;
            }
            createGroupChat(selectedUsers);
        });
    }

    /* ====================== UI HELPERS ====================== */

    private void updateSelectedUsers() {
        tvSelectedUsers.setText("Selected: " + String.join(", ", selectedUsers));
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(autoUserSearch.getWindowToken(), 0);
    }

    /* ====================== SEARCH USERS ====================== */

    private void searchUsers(String query) {
        String url = BASE_URL + "/search/user?query=" + query;
        Log.d(TAG, "Searching users: " + url);

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                res -> {
                    userList.clear();

                    for (int i = 0; i < res.length(); i++) {
                        JSONObject obj = res.optJSONObject(i);
                        if (obj != null) {
                            String username = obj.optString("username");
                            if (!username.equals(currentUser)) {
                                userList.add(username);
                            }
                        }
                    }

                    userAdapter.clear();
                    userAdapter.addAll(userList);
                    userAdapter.notifyDataSetChanged();

                    if (!userList.isEmpty()) autoUserSearch.showDropDown();
                },
                err -> Log.e(TAG, "Search error", err)
        );

        Volley.newRequestQueue(this).add(req);
    }

    /* ====================== CREATE DIRECT ====================== */

    private void createDirectChat(String otherUser) {
        String url = BASE_URL + "/messages/create/" + currentUser + "/" + otherUser;

        Log.d(TAG, "POST " + url);

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST,
                url,
                null,
                res -> {
                    String convoId = res.optString("id", "");
                    Log.d(TAG, "Direct chat created: " + convoId);
                    openChat(convoId, otherUser);
                },
                err -> {
                    Log.e(TAG, "Direct chat creation failed", err);
                    Toast.makeText(this, "Failed to create direct chat", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(req);
    }

    /* ====================== CREATE GROUP ====================== */

    private void createGroupChat(List<String> members) {
        // ALWAYS include the creator
        if (!members.contains(currentUser)) {
            members.add(currentUser);
        }

        String url = BASE_URL + "/messages/group";
        JSONArray body = new JSONArray(members);

        Log.d(TAG, "POST " + url + " Body: " + body);

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST,
                url,
                null,
                res -> {
                    String convoId = res.optString("id", "");
                    Log.d(TAG, "Group chat created: " + convoId);
                    openChat(convoId, "New Group Chat");
                },
                err -> {
                    Log.e(TAG, "Group chat creation failed", err);
                    Toast.makeText(this, "Failed to create group chat", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public byte[] getBody() {
                return body.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        Volley.newRequestQueue(this).add(req);
    }

    /* ====================== OPEN CHAT ====================== */

    private void openChat(String convoId, String chatName) {
        Intent i = new Intent(this, DirectMessagingActivity.class);
        i.putExtra("convoId", convoId);
        i.putExtra("chatName", chatName);
        i.putExtra("username", currentUser);
        startActivity(i);
        finish();
    }
}