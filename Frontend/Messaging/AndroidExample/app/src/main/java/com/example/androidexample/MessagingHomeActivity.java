package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Lists all conversations for CURRENT_USER using:
 *   GET http://coms-3090-028.class.las.iastate.edu:8080/messages/convos/{username}
 * Backend returns an array of Conversation objects (direct or group).
 */
public class MessagingHomeActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";
    private static final String CURRENT_USER = "Fuji"; // TODO: bind to your auth/session

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private EditText searchBar;
    private ImageButton newChatBtn;

    private ConversationAdapter adapter;
    private final List<ConversationItem> conversationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging_home);

        recyclerView = findViewById(R.id.recyclerConvos);
        progressBar  = findViewById(R.id.progressBar);
        searchBar    = findViewById(R.id.searchBar);
        newChatBtn   = findViewById(R.id.newChatBtn);

        adapter = new ConversationAdapter(conversationList, this::openConversation);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        newChatBtn.setOnClickListener(v -> {
            Intent i = new Intent(this, SelectUserActivity.class);
            startActivity(i);
        });

        fetchConversations();
    }

    private void fetchConversations() {
        progressBar.setVisibility(View.VISIBLE);
        String url = BASE_URL + "/messages/convos/" + CURRENT_USER;
        Log.d("MessagingHome", "Requesting: " + url);

        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null,
                resp -> {
                    progressBar.setVisibility(View.GONE);
                    conversationList.clear();
                    parseConversations(resp);
                },
                err -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("MessagingHome", "Volley error", err);
                    Toast.makeText(this, "Failed to load conversations", Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void parseConversations(JSONArray arr) {
        try {
            for (int i = 0; i < arr.length(); i++) {
                JSONObject c = arr.getJSONObject(i);

                String convoId = c.optString("id", "");
                String title   = "Conversation";
                String lastTs  = c.optString("lastMessage", ""); // LocalDateTime string
                String preview = "";

                // Try to compute a displayable name:
                if (convoId.startsWith("D-")) {
                    // pick the other member's username
                    JSONArray members = c.optJSONArray("members");
                    if (members != null) {
                        for (int m = 0; m < members.length(); m++) {
                            JSONObject u = members.getJSONObject(m);
                            String uname = u.optString("username", "");
                            if (!CURRENT_USER.equals(uname)) {
                                title = uname;
                                break;
                            }
                        }
                    }
                } else if (convoId.startsWith("G-")) {
                    title = c.optString("groupName", "Group Chat");
                }

                // Optional: try to grab last message text (if backend sends messages array)
                JSONArray messages = c.optJSONArray("messages");
                if (messages != null && messages.length() > 0) {
                    JSONObject last = messages.getJSONObject(messages.length() - 1);
                    preview = last.optString("text", "");
                }

                conversationList.add(new ConversationItem(convoId, title, preview, lastTs));
            }
            adapter.notifyDataSetChanged();
            Log.d("MessagingHome", "✅ Loaded " + conversationList.size() + " conversations");
        } catch (JSONException e) {
            Log.e("MessagingHome", "Parse error", e);
        }
    }

    private void openConversation(ConversationItem item) {
        Intent i = new Intent(this, DirectMessagingActivity.class);
        i.putExtra("convoId", item.getConvoId());
        i.putExtra("chatName", item.getName());
        startActivity(i);
    }
}