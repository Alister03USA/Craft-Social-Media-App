package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.Response;
import com.android.volley.VolleyError;

public class MessagingHomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ConversationAdapter adapter;
    private List<ConversationItem> convoList = new ArrayList<>();
    private FloatingActionButton fab;
    private String username = "Fuji";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging_home);

        recyclerView = findViewById(R.id.conversationRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ConversationAdapter(convoList, convo -> openChat(convo));
        recyclerView.setAdapter(adapter);

        fab = findViewById(R.id.fabNewChat);
        fab.setOnClickListener(v -> Toast.makeText(this, "New chat coming soon", Toast.LENGTH_SHORT).show());

        loadConversations("JiXianFu");
    }

    private void openChat(ConversationItem convo) {
        Intent i = new Intent(this, DirectMessageActivity.class);
        i.putExtra("convoId", convo.getConvoId());
        i.putExtra("otherUser", convo.getName());
        startActivity(i);
    }

    private void loadConversations(String username) {
        String url = "http://coms-3090-028.class.las.iastate.edu:8080/messages/" + username;
        Log.d("MessagingHome", "Requesting: " + url);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d("MessagingHome", "Response: " + response.toString());
                    try {
                        convoList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject o = response.getJSONObject(i);
                            convoList.add(new ConversationItem(
                                    o.getString("convoId"),
                                    o.getString("name"),
                                    o.getString("lastMessage"),
                                    o.getString("timestamp")
                            ));
                        }
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        Log.e("MessagingHome", "Parse error", e);
                    }
                },
                error -> {
                    Log.e("MessagingHome", "Volley error", error);
                    Toast.makeText(this, "Failed to load conversations", Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(this).add(request);
    }
}