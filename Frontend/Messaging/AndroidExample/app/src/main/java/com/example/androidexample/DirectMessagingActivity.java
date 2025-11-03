package com.example.androidexample;

import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request.Builder;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class DirectMessagingActivity extends AppCompatActivity implements MessageAdapter.MessageActions {

    private static final String TAG = "DirectMessage";

    // ✅ Backend endpoints
    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";
    private static final String WS_BASE = "ws://coms-3090-028.class.las.iastate.edu:8080";

    private String convoId;
    private String chatName;
    private String currentUser; // ✅ dynamically assigned now

    private RecyclerView recycler;
    private MessageAdapter adapter;
    private final List<MessageItem> messages = new ArrayList<>();
    private final List<String> memberUsernames = new ArrayList<>();

    private AutoCompleteTextView etInput;
    private TextView tvTitle, tvReplyPreview;
    private ImageButton btnSend, btnAttach, btnScrollLatest, btnCancelReply;
    private ProgressBar progress;
    private View replyContainer;

    private Long replyingTo = null;
    private String replyingToText = null;
    private WebSocket socket;

    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_direct_message);

        convoId = getIntent().getStringExtra("convoId");
        chatName = getIntent().getStringExtra("chatName");
        currentUser = getIntent().getStringExtra("username"); // ✅ always passed from previous activity

        if (currentUser == null || currentUser.isEmpty()) {
            Toast.makeText(this, "⚠️ No active user detected. Using test fallback.", Toast.LENGTH_SHORT).show();
            currentUser = "Fuji"; // fallback only if missing
        }

        Log.d(TAG, "👤 Active user in chat: " + currentUser + " | Chat: " + chatName);

        tvTitle = findViewById(R.id.tvTitle);
        etInput = findViewById(R.id.etInput);
        tvReplyPreview = findViewById(R.id.tvReplyPreview);
        btnSend = findViewById(R.id.btnSend);
        btnAttach = findViewById(R.id.btnAttach);
        btnScrollLatest = findViewById(R.id.btnScrollLatest);
        btnCancelReply = findViewById(R.id.btnCancelReply);
        progress = findViewById(R.id.progress);
        replyContainer = findViewById(R.id.replyContainer);
        ImageView btnBack = findViewById(R.id.btnBack);

        tvTitle.setText(chatName);
        btnBack.setOnClickListener(v -> finish());

        recycler = findViewById(R.id.recycler);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        recycler.setLayoutManager(lm);

        adapter = new MessageAdapter(messages, currentUser, this);
        recycler.setAdapter(adapter);

        btnSend.setOnClickListener(v -> sendMessage());
        btnAttach.setOnClickListener(v -> openPicker());
        btnScrollLatest.setOnClickListener(v -> recycler.scrollToPosition(Math.max(messages.size() - 1, 0)));
        btnCancelReply.setOnClickListener(v -> clearReplyPreview());

        setupMentionWatcher();
        fetchHistory();
        connectSocket();
    }

    /* ==================== FETCH HISTORY ==================== */
    private void fetchHistory() {
        progress.setVisibility(View.VISIBLE);
        String url = BASE_URL + "/messages/" + convoId;
        Log.d(TAG, "🌍 GET " + url);

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                res -> {
                    progress.setVisibility(View.GONE);
                    parseConversation(res);
                    parseMembers(res);
                },
                err -> {
                    progress.setVisibility(View.GONE);
                    Log.e(TAG, "❌ Volley error", err);
                });
        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void parseConversation(JSONObject convo) {
        messages.clear();
        try {
            JSONArray arr = convo.optJSONArray("messages");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    long id = o.optLong("id", 0);
                    String sender = o.optString("sender", "");
                    String text = o.optString("text", "");
                    String ts = o.optString("date", "");
                    Long parent = o.isNull("parentMessage") ? null :
                            (o.optJSONObject("parentMessage") != null
                                    ? o.optJSONObject("parentMessage").optLong("id", 0)
                                    : null);

                    MessageItem m = new MessageItem(id, sender, highlightMentions(text).toString(), ts, parent);
                    JSONObject reactObj = o.optJSONObject("reactions");
                    if (reactObj != null) {
                        Iterator<String> keys = reactObj.keys();
                        while (keys.hasNext()) {
                            String k = keys.next();
                            m.getReactions().put(k, reactObj.optInt(k, 0));
                        }
                    }
                    messages.add(m);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "⚠️ parse error", e);
        }

        adapter.notifyDataSetChanged();
        recycler.scrollToPosition(Math.max(messages.size() - 1, 0));
    }

    private void parseMembers(JSONObject convo) {
        memberUsernames.clear();
        try {
            JSONArray arr = convo.optJSONArray("members");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    String u = arr.getJSONObject(i).optString("username", "");
                    if (!TextUtils.isEmpty(u) && !u.equals(currentUser))
                        memberUsernames.add("@" + u);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "⚠️ Error parsing members", e);
        }
    }

    /* ==================== REPLY LOGIC ==================== */
    @Override
    public void onReply(MessageItem m) {
        replyingTo = m.getId();
        replyingToText = m.getContent();

        String shortText = replyingToText != null && replyingToText.length() > 40
                ? replyingToText.substring(0, 40) + "..." : replyingToText;

        replyContainer.setVisibility(View.VISIBLE);
        tvReplyPreview.setText("Replying to \"" + shortText + "\"");
    }

    private void clearReplyPreview() {
        replyingTo = null;
        replyingToText = null;
        replyContainer.setVisibility(View.GONE);
        tvReplyPreview.setText("");
    }

    private void sendMessage() {
        String text = etInput.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        if (replyingTo != null)
            socketSend("#reply:" + replyingTo + ":" + text);
        else
            socketSend(text);

        messages.add(new MessageItem(0, currentUser, highlightMentions(text).toString(), now(), replyingTo));
        adapter.notifyItemInserted(messages.size() - 1);
        recycler.scrollToPosition(messages.size() - 1);
        etInput.setText("");
        clearReplyPreview();
    }

    /* ==================== SOCKET ==================== */
    private void connectSocket() {
        try {
            String url = WS_BASE + "/chat/" + convoId + "/" + currentUser;
            OkHttpClient client = new OkHttpClient.Builder().build();
            okhttp3.Request r = new Builder().url(url).build();
            socket = client.newWebSocket(r, new WsListener());
            Log.i(TAG, "🔗 Connected WS: " + url);
        } catch (Exception e) {
            Log.e(TAG, "💥 Socket connect fail", e);
        }
    }

    private void socketSend(String payload) {
        try {
            if (socket != null) socket.send(payload);
        } catch (Exception e) {
            Log.e(TAG, "send failed", e);
        }
    }

    @Override
    public void onReact(MessageItem m, String reactionType) {
        socketSend("#react:" + m.getId() + ":" + reactionType);
        Log.d(TAG, "💥 Sent reaction: " + reactionType + " for message " + m.getId());
    }

    @Override
    public void onRemoveReact(MessageItem m, String reactionType) {
        socketSend("#!react:" + m.getId() + ":" + reactionType);
        Log.d(TAG, "🗑 Removed reaction: " + reactionType + " for message " + m.getId());
    }

    @Override
    public void onLongPress(MessageItem m) {
        Log.d(TAG, "🟨 Long pressed " + m.getContent());
    }

    private final class WsListener extends WebSocketListener {
        @Override
        public void onMessage(WebSocket ws, String t) {
            runOnUiThread(() -> {
                try {
                    if (!t.startsWith("#") && t.split(":").length == 3) {
                        String[] p = t.split(":");
                        long id = Long.parseLong(p[0]);
                        String emoji = p[1];
                        int count = Integer.parseInt(p[2]);
                        for (MessageItem mi : messages) {
                            if (mi.getId() == id) {
                                mi.getReactions().put(emoji, count);
                                break;
                            }
                        }
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    if (t.contains("-")) {
                        int idx = t.indexOf("-");
                        long parent = Long.parseLong(t.substring(0, idx));
                        String msg = t.substring(idx + 1);
                        messages.add(new MessageItem(0, "•reply•", highlightMentions(msg).toString(), now(), parent));
                        adapter.notifyItemInserted(messages.size() - 1);
                        recycler.scrollToPosition(messages.size() - 1);
                        return;
                    }

                    int i = t.indexOf(": ");
                    String sender = i > 0 ? t.substring(0, i) : "unknown";
                    String body = i > 0 ? t.substring(i + 2) : t;

// ✅ Prevent duplicate: ignore our own echoed messages
                    if (sender.equals(currentUser)) {
                        Log.d(TAG, " Skipping duplicate message from self: " + body);
                        return;
                    }

                    messages.add(new MessageItem(0, sender, highlightMentions(body).toString(), now(), null));
                    adapter.notifyItemInserted(messages.size() - 1);
                    recycler.scrollToPosition(messages.size() - 1);
                } catch (Exception e) {
                    Log.e(TAG, "WS parse error", e);
                }
            });
        }
    }

    /* ==================== UTILITIES ==================== */
    private void setupMentionWatcher() {
        etInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                if (s.toString().endsWith("@")) showMentionDropdown();
            }
        });
    }

    private void showMentionDropdown() {
        if (memberUsernames.isEmpty()) return;
        ArrayAdapter<String> ad = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, memberUsernames);
        etInput.setAdapter(ad);
        etInput.showDropDown();
    }

    private String now() {
        if (Build.VERSION.SDK_INT >= 26) return java.time.LocalDateTime.now().toString();
        return String.valueOf(System.currentTimeMillis());
    }

    private SpannableString highlightMentions(String text) {
        SpannableString s = new SpannableString(text);
        Matcher m = Pattern.compile("@\\w+").matcher(text);
        while (m.find()) {
            s.setSpan(new ForegroundColorSpan(Color.parseColor("#1565C0")), m.start(), m.end(), 0);
            s.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), m.start(), m.end(), 0);
        }
        return s;
    }

    private void openPicker() {
        Toast.makeText(this, "Attachment disabled for this test build", Toast.LENGTH_SHORT).show();
    }

    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor c = getContentResolver().query(uri, null, null, null, null)) {
                if (c != null && c.moveToFirst()) {
                    int nameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) result = c.getString(nameIndex);
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result != null ? result.lastIndexOf('/') : -1;
            if (cut != -1) result = result.substring(cut + 1);
        }
        return result;
    }

    private byte[] readBytesFromUri(Uri uri) {
        try (InputStream in = getContentResolver().openInputStream(uri);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (in == null) return null;
            byte[] buf = new byte[4096]; int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            return out.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (socket != null) socket.close(1000, "bye");
    }
}