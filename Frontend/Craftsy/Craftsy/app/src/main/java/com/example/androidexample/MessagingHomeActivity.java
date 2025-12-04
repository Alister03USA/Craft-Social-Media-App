package com.example.androidexample;

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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.widget.ImageView;

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
    private ImageButton MHbckButton;

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

        directAdapter = new ConversationAdapter(direct, this::openConversation, this::deleteConversation, this);
        groupAdapter = new ConversationAdapter(groups, this::openConversation, this::deleteConversation, this);

        rvDirect.setAdapter(directAdapter);
        rvGroups.setAdapter(groupAdapter);

        currentUsername = getIntent().getStringExtra("username");
        Log.d(TAG, "Active user: " + currentUsername);

        newChatBtn.setOnClickListener(v -> {
            Intent i = new Intent(this, NewChatActivity.class);
            i.putExtra("username", currentUsername);
            startActivity(i);
        });

        MHbckButton.setOnClickListener(v -> finish());

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { filter(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        fetchConversations();
    }

    /* ==================== IMAGE LOADING HELPER ==================== */
    public void loadProfilePic(long imageId, ImageView target) {
        if (imageId <= 0) {
            target.setImageResource(R.drawable.profile);
            return;
        }

        String lookupUrl = BASE_URL + "/images/" + imageId;

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                lookupUrl,
                null,
                res -> {
                    try {
                        String filePath = res.optString("filePath", "");
                        if (filePath.isEmpty()) {
                            target.setImageResource(R.drawable.profile);
                            return;
                        }

                        String filename = filePath.substring(filePath.lastIndexOf("/") + 1);
                        String fullUrl = BASE_URL + "/uploads/" + filename;

                        Glide.with(target.getContext())
                                .load(fullUrl)
                                .placeholder(R.drawable.profile)
                                .error(R.drawable.profile)
                                .circleCrop()
                                .into(target);

                    } catch (Exception e) {
                        target.setImageResource(R.drawable.profile);
                    }
                },
                err -> target.setImageResource(R.drawable.profile)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    /* ==================== FILTER ==================== */
    private void filter(String query) {
        query = query.trim().toLowerCase();
        direct.clear();
        groups.clear();

        for (ConversationItem c : all) {
            boolean match =
                    query.isEmpty()
                            || c.getDisplayName().toLowerCase().contains(query)
                            || c.getUsername().toLowerCase().contains(query)
                            || c.getConvoId().toLowerCase().contains(query);

            if (match) {
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

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                res -> {
                    progressBar.setVisibility(View.GONE);
                    parseConvoArray(res);
                },
                err -> {
                    progressBar.setVisibility(View.GONE);
                    fallbackStringRequest(url);
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void fallbackStringRequest(String url) {
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    try {
                        String clean = response.trim();
                        if (clean.startsWith("json")) clean = clean.substring(4).trim();
                        JSONArray arr = new JSONArray(clean);
                        parseConvoArray(arr);
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON fallback failed", e);
                    }
                },
                error -> Log.e(TAG, "Fallback failed", error)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    /* ==================== PARSE CONVERSATIONS ==================== */
    private void parseConvoArray(JSONArray arr) {
        all.clear();
        direct.clear();
        groups.clear();

        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject o = arr.getJSONObject(i);

                String convoId = o.optString("id", "");
                String displayName = o.optString("groupName", "");
                String last = "";

                JSONArray msgs = o.optJSONArray("messages");
                if (msgs != null && msgs.length() > 0) {
                    last = msgs.getJSONObject(msgs.length() - 1).optString("text", "");
                }

                boolean isGroup = convoId.startsWith("G-");
                long profileImageId = -1;
                String otherUser = "";

                if (!isGroup) {
                    JSONArray members = o.optJSONArray("members");
                    if (members != null) {
                        for (int m = 0; m < members.length(); m++) {
                            JSONObject mem = members.getJSONObject(m);
                            String uname = mem.optString("username", "");

                            if (!uname.equalsIgnoreCase(currentUsername)) {
                                otherUser = uname;

                                JSONObject imgObj = mem.optJSONObject("image");
                                if (imgObj != null) {
                                    profileImageId = imgObj.optLong("id", -1);
                                }

                                displayName = mem.optString("displayName",
                                        mem.optString("username", "User"));
                                break;
                            }
                        }
                    }
                }

                if (isGroup && (displayName == null || displayName.isEmpty() || displayName.equals("null"))) {
                    JSONArray members = o.optJSONArray("members");
                    if (members != null) {
                        StringBuilder sb = new StringBuilder();
                        for (int m = 0; m < members.length(); m++) {
                            String uname = members.getJSONObject(m).optString("displayName", "");
                            if (sb.length() > 0) sb.append(", ");
                            sb.append(uname);
                        }
                        displayName = sb.toString();
                    }
                }

                ConversationItem item =
                        new ConversationItem(
                                convoId,
                                displayName,
                                otherUser,
                                last,
                                "",
                                profileImageId
                        );

                all.add(item);
                if (isGroup) groups.add(item);
                else direct.add(item);

            } catch (JSONException e) {
                Log.e(TAG, "Parse error", e);
            }
        }

        directAdapter.notifyDataSetChanged();
        groupAdapter.notifyDataSetChanged();
    }

    /* ==================== OPEN CHAT ==================== */
    private void openConversation(ConversationItem item) {
        Intent i = new Intent(this, DirectMessagingActivity.class);
        i.putExtra("convoId", item.getConvoId());
        i.putExtra("chatName", item.getDisplayName());
        i.putExtra("otherUser", item.getUsername());
        i.putExtra("username", currentUsername);
        i.putExtra("profileImageId", item.getProfileImageId());
        startActivity(i);
    }

    /* ==================== DELETE CONVERSATION ==================== */
    private void deleteConversation(ConversationItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Conversation")
                .setMessage("Are you sure you want to delete this conversation?")
                .setPositiveButton("Delete", (d, i) -> {
                    String url = BASE_URL + "/messages/convo/" + item.getConvoId();

                    StringRequest req = new StringRequest(
                            Request.Method.DELETE,
                            url,
                            res -> {
                                Toast.makeText(this, "Conversation deleted", Toast.LENGTH_SHORT).show();
                                all.remove(item);
                                if (item.isGroup()) groups.remove(item);
                                else direct.remove(item);
                                directAdapter.notifyDataSetChanged();
                                groupAdapter.notifyDataSetChanged();
                            },
                            err -> Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show()
                    );

                    VolleySingleton.getInstance(this).addToRequestQueue(req);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}