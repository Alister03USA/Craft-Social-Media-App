package com.example.androidexample;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class CommentsActivity extends AppCompatActivity {

    private RecyclerView commentRecycler;
    private EditText commentInput;
    private ImageButton sendBtn;
    private CommentAdapter adapter;
    private List<CommentModel> comments = new ArrayList<>();

    private long messageId;
    private String username;
    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        messageId = getIntent().getLongExtra("messageId", -1);
        username = SessionManager.getInstance().getLoggedInUsername();

        commentRecycler = findViewById(R.id.commentRecycler);
        commentInput = findViewById(R.id.commentInput);
        sendBtn = findViewById(R.id.commentSendBtn);

        adapter = new CommentAdapter(comments);
        commentRecycler.setLayoutManager(new LinearLayoutManager(this));
        commentRecycler.setAdapter(adapter);

        loadComments();

        sendBtn.setOnClickListener(v -> addComment());
    }

    private void loadComments() {
        String url = BASE_URL + "/groupMessage/" + messageId + "/comments";
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    comments.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject c = response.getJSONObject(i);
                            JSONObject sender = c.getJSONObject("sender");
                            comments.add(new CommentModel(
                                    sender.getString("username"),
                                    c.getString("comment")
                            ));
                        } catch (Exception ignore) {}
                    }
                    adapter.notifyDataSetChanged();
                },
                error -> {}
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void addComment() {
        String text = commentInput.getText().toString().trim();
        if (text.isEmpty()) return;

        String url = BASE_URL + "/comment/" + username + "/" + messageId + "/create";

        JSONObject body = new JSONObject();
        try {
            body.put("comment", text);
        } catch (JSONException ignored) {}

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
                response -> {
                    commentInput.setText("");
                    loadComments(); // refresh instantly
                },
                error -> {}
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}
