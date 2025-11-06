package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MessagingHomeActivity extends AppCompatActivity {

    private static final String TAG = "MessagingHome";
    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";

    private RecyclerView rvDirect, rvGroups;
    private ConversationAdapter directAdapter, groupAdapter;
    private final List<ConversationItem> all = new ArrayList<>();
    private final List<ConversationItem> direct = new ArrayList<>();
    private final List<ConversationItem> groups = new ArrayList<>();

    private EditText searchBar;
    private ImageButton newChatBtn;
    private ProgressBar progressBar;
    private String currentUsername;
    private Button MHbckButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging_home);

        rvDirect = findViewById(R.id.rvDirect);
        rvGroups = findViewById(R.id.rvGroups);
        searchBar = findViewById(R.id.searchBar);
        newChatBtn = findViewById(R.id.newChatBtn);
        progressBar = findViewById(R.id.progressBar);
        MHbckButton = findViewById(R.id.MHbckButton);

        rvDirect.setLayoutManager(new LinearLayoutManager(this));
        rvGroups.setLayoutManager(new LinearLayoutManager(this));

        directAdapter = new ConversationAdapter(direct, this::openConversation, this::deleteConversation);
        groupAdapter = new ConversationAdapter(groups, this::openConversation, this::deleteConversation);
        rvDirect.setAdapter(directAdapter);
        rvGroups.setAdapter(groupAdapter);

        currentUsername = getIntent().getStringExtra("username");
        if (currentUsername == null || currentUsername.isEmpty()) {
            currentUsername = "Fuji";
            Toast.makeText(this, "⚠️ No user passed, using default 'Fuji'", Toast.LENGTH_SHORT).show();
        }
        Log.d(TAG, "👤 Active user: " + currentUsername);

        newChatBtn.setOnClickListener(v -> {
            Log.d(TAG, "🟢 Opening NewChatActivity for " + currentUsername);
            Intent i = new Intent(this, NewChatActivity.class);
            i.putExtra("username", currentUsername);
            startActivity(i);
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { filter(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });
        MHbckButton.setOnClickListener(v -> finish());


        fetchConversations();
    }

    /* ==================== FILTER ==================== */
    private void filter(String query) {
        query = query.trim().toLowerCase();
        direct.clear();
        groups.clear();
        for (ConversationItem c : all) {
            if (query.isEmpty() ||
                    c.getName().toLowerCase().contains(query) ||
                    c.getConvoId().toLowerCase().contains(query)) {
                if (c.isGroup()) groups.add(c);
                else direct.add(c);
            }
        }
        directAdapter.notifyDataSetChanged();
        groupAdapter.notifyDataSetChanged();
    }

    /* ==================== FETCH ==================== */
    private void fetchConversations() {
        progressBar.setVisibility(View.VISIBLE);
        String url = BASE_URL + "/messages/convos/" + currentUsername;
        Log.d(TAG, "🌍 GET " + url);

        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null,
                res -> {
                    progressBar.setVisibility(View.GONE);
                    parseConvoArray(res);
                },
                err -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "❌ JSON request failed, trying fallback", err);
                    fallbackStringRequest(url);
                });

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void fallbackStringRequest(String url) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        String clean = response.trim();
                        if (clean.startsWith("json")) clean = clean.substring(4).trim();
                        JSONArray arr = new JSONArray(clean);
                        parseConvoArray(arr);
                    } catch (JSONException e) { Log.e(TAG, "JSON fallback failed", e); }
                },
                error -> Log.e(TAG, "Fallback failed", error));
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    /* ==================== PARSE ==================== */
    private void parseConvoArray(JSONArray arr) {
        all.clear(); direct.clear(); groups.clear();
        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject o = arr.getJSONObject(i);
                String id = o.optString("id", "");
                String name = o.optString("groupName", "");

                //  If groupName is missing, build it from member names
                if (name == null || name.isEmpty() || name.equals("null")) {
                    JSONArray members = o.optJSONArray("members");
                    if (members != null && members.length() > 0) {
                        StringBuilder namesBuilder = new StringBuilder();
                        for (int m = 0; m < members.length(); m++) {
                            JSONObject member = members.getJSONObject(m);
                            String uname = member.optString("displayName", member.optString("username", ""));
                            if (!currentUsername.equalsIgnoreCase(uname)) {
                                if (namesBuilder.length() > 0) namesBuilder.append(", ");
                                namesBuilder.append(uname);
                            }
                        }
                        name = namesBuilder.toString().trim();
                    }
                }

                // Get last message preview
                String last = "";
                JSONArray msgs = o.optJSONArray("messages");
                if (msgs != null && msgs.length() > 0)
                    last = msgs.getJSONObject(msgs.length() - 1).optString("text", "");

                // Create item and categorize
                ConversationItem item = new ConversationItem(id, name, last, "");
                all.add(item);
                if (item.isGroup()) groups.add(item); else direct.add(item);

            } catch (JSONException e) {
                Log.e(TAG, "⚠️ Parse error", e);
            }
        }

        directAdapter.notifyDataSetChanged();
        groupAdapter.notifyDataSetChanged();
    }

    /* ==================== OPEN CHAT ==================== */
    private void openConversation(ConversationItem item) {
        Log.d(TAG, "💬 Opening conversation: " + item.getConvoId());
        Intent i = new Intent(this, DirectMessagingActivity.class);
        i.putExtra("convoId", item.getConvoId());
        i.putExtra("chatName", item.getName());
        i.putExtra("username", currentUsername);
        startActivity(i);
    }

    /* ==================== DELETE CONVO ==================== */
    private void deleteConversation(ConversationItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Conversation")
                .setMessage("Are you sure you want to delete this conversation?")
                .setPositiveButton("Delete", (d, i) -> {
                    String url = BASE_URL + "/messages/convo/" + item.getConvoId();
                    Log.d(TAG, "🗑 Deleting conversation: " + url);

                    StringRequest req = new StringRequest(Request.Method.DELETE, url,
                            res -> {
                                Toast.makeText(this, "Conversation deleted", Toast.LENGTH_SHORT).show();
                                all.remove(item);
                                if (item.isGroup()) groups.remove(item); else direct.remove(item);
                                directAdapter.notifyDataSetChanged();
                                groupAdapter.notifyDataSetChanged();
                            },
                            err -> {
                                Log.e(TAG, "❌ Failed to delete conversation", err);
                                Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show();
                            });
                    VolleySingleton.getInstance(this).addToRequestQueue(req);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}