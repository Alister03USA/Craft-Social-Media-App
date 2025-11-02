package com.example.androidexample;

import static com.example.androidexample.ApiConfig.BASE_URL;
import static com.example.androidexample.ApiConfig.CURRENT_USERNAME;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

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

/**
 * MessagingHomeActivity
 * ---------------------
 * Displays all Direct + Group Conversations for the current user.
 * Supports both backend JSON and mockserver "json [ ... ]" string responses.
 * Logs each step for easy debugging via Logcat.
 */
public class MessagingHomeActivity extends AppCompatActivity {

    private static final String TAG = "MessagingHome";

    private RecyclerView rvDirect, rvGroups;
    private ConversationAdapter directAdapter, groupAdapter;
    private final List<ConversationItem> all = new ArrayList<>();
    private final List<ConversationItem> direct = new ArrayList<>();
    private final List<ConversationItem> groups = new ArrayList<>();

    private EditText searchBar;
    private ImageButton newChatBtn;
    private ProgressBar progressBar;

    private String currentUsername = CURRENT_USERNAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging_home);

        rvDirect = findViewById(R.id.rvDirect);
        rvGroups = findViewById(R.id.rvGroups);
        searchBar = findViewById(R.id.searchBar);
        newChatBtn = findViewById(R.id.newChatBtn);
        progressBar = findViewById(R.id.progressBar);

        rvDirect.setLayoutManager(new LinearLayoutManager(this));
        rvGroups.setLayoutManager(new LinearLayoutManager(this));

        directAdapter = new ConversationAdapter(direct, this::openConversation);
        groupAdapter = new ConversationAdapter(groups, this::openConversation);
        rvDirect.setAdapter(directAdapter);
        rvGroups.setAdapter(groupAdapter);

        // ✅ Get logged-in or test user
        String fromIntent = getIntent().getStringExtra("username");
        if (fromIntent != null && !fromIntent.isEmpty()) {
            currentUsername = fromIntent;
        }
        Log.d(TAG, "👤 Active user: " + currentUsername);

        // ✅ Launch NewChatActivity when clicking "New Chat"
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

        fetchConversations();
    }

    /** 🔍 Filters search results in both lists */
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

    /** 🌐 Fetches all conversations from backend */
    private void fetchConversations() {
        progressBar.setVisibility(View.VISIBLE);
        String url = BASE_URL + "/messages/convos/" + currentUsername;
        Log.d(TAG, "🌍 GET " + url);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    Log.d(TAG, "✅ JSON Array response: " + response.length());
                    parseConvoArray(response);
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "❌ Volley JSON parse error, switching to fallback", error);
                    fallbackStringRequest(url);
                });

        VolleySingleton.getInstance(this).addToRequestQueue(jsonArrayRequest);
    }

    /** 🧩 Fallback handler for "json [ ... ]" string response */
    private void fallbackStringRequest(String url) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        Log.d(TAG, "📦 Raw response: " + response);
                        String clean = response.trim();

                        if (clean.startsWith("json")) {
                            clean = clean.substring(4).trim();
                        }

                        JSONArray arr = new JSONArray(clean);
                        parseConvoArray(arr);
                    } catch (JSONException e) {
                        Log.e(TAG, "💥 JSON parsing failed in fallback", e);
                    }
                },
                error -> Log.e(TAG, "❌ StringRequest fallback failed", error));

        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    /** 🧠 Parses array of conversations */
    private void parseConvoArray(JSONArray arr) {
        all.clear(); direct.clear(); groups.clear();

        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject o = arr.getJSONObject(i);
                String id = o.optString("id", "");
                String name = o.optString("groupName", "");

                if (name.isEmpty()) {
                    JSONArray members = o.optJSONArray("members");
                    if (members != null) {
                        for (int m = 0; m < members.length(); m++) {
                            String u = members.getJSONObject(m).optString("username");
                            if (!currentUsername.equals(u)) {
                                name = u;
                                break;
                            }
                        }
                    }
                }

                String last = "";
                JSONArray msgs = o.optJSONArray("messages");
                if (msgs != null && msgs.length() > 0) {
                    JSONObject lm = msgs.getJSONObject(msgs.length() - 1);
                    last = lm.optString("text", "");
                }

                ConversationItem item = new ConversationItem(id, name, last, "");
                all.add(item);
                if (item.isGroup()) groups.add(item);
                else direct.add(item);

            } catch (JSONException e) {
                Log.e(TAG, "⚠️ Error parsing conversation index " + i, e);
            }
        }

        directAdapter.notifyDataSetChanged();
        groupAdapter.notifyDataSetChanged();
        Log.d(TAG, "✅ Loaded " + all.size() + " total conversations");
    }

    /** 💬 Opens a selected conversation */
    private void openConversation(ConversationItem item) {
        Log.d(TAG, "💬 Opening conversation: " + item.getConvoId());
        Intent i = new Intent(this, DirectMessagingActivity.class);
        i.putExtra("convoId", item.getConvoId());
        i.putExtra("chatName", item.getName());
        startActivity(i);
    }
}