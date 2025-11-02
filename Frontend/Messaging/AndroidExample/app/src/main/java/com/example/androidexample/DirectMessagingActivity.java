package com.example.androidexample;

import static com.example.androidexample.ApiConfig.BASE_URL;
import static com.example.androidexample.ApiConfig.CURRENT_USERNAME;
import static com.example.androidexample.ApiConfig.WS_BASE;

import android.content.ContentResolver;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.WebSocket;
import okhttp3.Request.Builder;
import okhttp3.WebSocketListener;

public class DirectMessagingActivity extends AppCompatActivity implements MessageAdapter.MessageActions {

    private static final String TAG = "DirectMessage";

    private String convoId;
    private String chatName;
    private RecyclerView recycler;
    private MessageAdapter adapter;
    private final List<MessageItem> messages = new ArrayList<>();
    private final List<String> memberUsernames = new ArrayList<>();

    private AutoCompleteTextView etInput;
    private TextView tvTitle, tvReplyPreview;
    private ImageButton btnSend, btnAttach, btnScrollLatest;
    private ProgressBar progress;
    private View replyContainer;
    private Long replyingTo = null;
    private WebSocket socket;

    private final ActivityResultLauncher<String[]> pickDoc =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), this::onFilePicked);

    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_direct_message);

        convoId = getIntent().getStringExtra("convoId");
        chatName = getIntent().getStringExtra("chatName");

        tvTitle = findViewById(R.id.tvTitle);
        etInput = findViewById(R.id.etInput);
        tvReplyPreview = findViewById(R.id.tvReplyPreview);
        btnSend = findViewById(R.id.btnSend);
        btnAttach = findViewById(R.id.btnAttach);
        btnScrollLatest = findViewById(R.id.btnScrollLatest);
        progress = findViewById(R.id.progress);
        replyContainer = findViewById(R.id.replyContainer);
        ImageView btnBack = findViewById(R.id.btnBack);

        tvTitle.setText(chatName);
        btnBack.setOnClickListener(v -> finish());

        recycler = findViewById(R.id.recycler);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        recycler.setLayoutManager(lm);

        adapter = new MessageAdapter(messages, CURRENT_USERNAME, this);
        recycler.setAdapter(adapter);

        btnSend.setOnClickListener(v -> sendMessage());
        btnAttach.setOnClickListener(v -> openPicker());
        btnScrollLatest.setOnClickListener(v -> recycler.scrollToPosition(Math.max(messages.size() - 1, 0)));

        setupMentionWatcher();
        fetchHistory();
        connectSocket();
    }

    private void openPicker() {
        pickDoc.launch(new String[]{"image/*", "application/pdf"});
    }

    private void onFilePicked(Uri uri) {
        if (uri == null) return;
        try {
            String fileName = getFileName(uri);
            String mimeType = getContentResolver().getType(uri);
            Log.d(TAG, "📎 Picked file: " + fileName + " (" + mimeType + ")");
            uploadFileToBackend(uri, fileName, mimeType);
        } catch (Exception e) {
            Log.e(TAG, "❌ File pick error", e);
            Toast.makeText(this, "File selection failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadFileToBackend(Uri uri, String fileName, String mimeType) {
        try {
            progress.setVisibility(View.VISIBLE);
            boolean isGroup = convoId.startsWith("G-");
            String url = isGroup ? BASE_URL + "/messages/" + convoId + "/pic" : BASE_URL + "/upload";

            byte[] fileBytes = readBytesFromUri(uri);
            if (fileBytes == null) return;

            VolleyMultipartRequest req = new VolleyMultipartRequest(Request.Method.PUT, url,
                    r -> {
                        progress.setVisibility(View.GONE);
                        Log.d(TAG, "✅ Uploaded file: " + fileName);
                        String msg = "[Attachment] " + fileName;
                        messages.add(new MessageItem(0, CURRENT_USERNAME, msg, now(), null));
                        adapter.notifyItemInserted(messages.size() - 1);
                        recycler.scrollToPosition(messages.size() - 1);
                        socketSend(msg);
                    },
                    e -> {
                        progress.setVisibility(View.GONE);
                        Log.e(TAG, "❌ Upload error", e);
                    }) {
                @Override
                public byte[] getBody() throws AuthFailureError { return fileBytes; }
                @Override
                public String getBodyContentType() { return mimeType != null ? mimeType : "application/octet-stream"; }
            };
            Volley.newRequestQueue(this).add(req);
        } catch (Exception e) {
            Log.e(TAG, "💥 Upload exception", e);
        }
    }

    private byte[] readBytesFromUri(Uri uri) throws IOException {
        try (InputStream in = getContentResolver().openInputStream(uri);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (in == null) return null;
            byte[] buf = new byte[4096]; int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            return out.toByteArray();
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor c = getContentResolver().query(uri, null, null, null, null)) {
                if (c != null && c.moveToFirst()) {
                    int nameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = c.getString(nameIndex);
                    } else {
                        Log.w(TAG, "⚠️ DISPLAY_NAME column not found in cursor");
                        result = uri.getLastPathSegment(); // fallback
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) result = result.substring(cut + 1);
        }
        return result;
    }

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

                    SpannableString styled = highlightMentions(text);
                    MessageItem m = new MessageItem(id, sender, styled.toString(), ts, parent);

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
                    if (!TextUtils.isEmpty(u) && !u.equals(CURRENT_USERNAME))
                        memberUsernames.add("@" + u);
                }
            }
            Log.d(TAG, "👥 Mentions: " + memberUsernames);
        } catch (Exception e) {
            Log.e(TAG, "⚠️ Error parsing members", e);
        }
    }

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

    private void connectSocket() {
        try {
            String url = WS_BASE + "/chat/" + convoId + "/" + CURRENT_USERNAME;
            OkHttpClient client = new OkHttpClient.Builder().build();
            okhttp3.Request r = new Builder().url(url).build();
            socket = client.newWebSocket(r, new WsListener());
            Log.i(TAG, "🔗 Connected WS: " + url);
        } catch (Exception e) {
            Log.e(TAG, "💥 Socket connect fail", e);
        }
    }

    private void sendMessage() {
        String text = etInput.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;
        if (replyingTo != null) socketSend("#reply:" + replyingTo + ":" + text);
        else socketSend(text);

        SpannableString styled = highlightMentions(text);
        messages.add(new MessageItem(0, CURRENT_USERNAME, styled.toString(), now(), replyingTo));
        adapter.notifyItemInserted(messages.size() - 1);
        recycler.scrollToPosition(messages.size() - 1);
        etInput.setText("");
        clearReplyPreview();
    }

    private void socketSend(String payload) {
        try {
            if (socket != null) socket.send(payload);
        } catch (Exception e) { Log.e(TAG, "send failed", e); }
    }

    @Override public void onReact(MessageItem m, String r) { socketSend("#react:" + m.getId() + ":" + r); }
    @Override public void onReply(MessageItem m) {
        replyingTo = (m.getId() == 0 ? System.currentTimeMillis() : m.getId());
        replyContainer.setVisibility(View.VISIBLE);
        tvReplyPreview.setText("Replying to • #" + replyingTo);
    }
    @Override public void onLongPress(MessageItem m) { Log.d(TAG, "🟨 Long pressed " + m.getContent()); }

    private void clearReplyPreview() { replyingTo = null; replyContainer.setVisibility(View.GONE); tvReplyPreview.setText(""); }

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

    private final class WsListener extends WebSocketListener {
        @Override public void onMessage(WebSocket ws, String t) {
            runOnUiThread(() -> {
                try {
                    if (t.startsWith("#")) return;
                    if (t.split(":").length == 3) {
                        String[] p = t.split(":");
                        long id = Long.parseLong(p[0]);
                        String emoji = p[1];
                        int count = Integer.parseInt(p[2]);
                        for (MessageItem mi : messages) {
                            if (mi.getId() == id) { mi.getReactions().put(emoji, count); break; }
                        }
                        adapter.notifyDataSetChanged();
                        return;
                    }
                    if (t.contains("-")) {
                        int idx = t.indexOf("-");
                        long parent = Long.parseLong(t.substring(0, idx));
                        String msg = t.substring(idx + 1);
                        messages.add(new MessageItem(0, "•reply•", msg, now(), parent));
                        adapter.notifyItemInserted(messages.size() - 1);
                        recycler.scrollToPosition(messages.size() - 1);
                        return;
                    }
                    int i = t.indexOf(": ");
                    String sender = i > 0 ? t.substring(0, i) : "unknown";
                    String body = i > 0 ? t.substring(i + 2) : t;
                    messages.add(new MessageItem(0, sender, body, now(), null));
                    adapter.notifyItemInserted(messages.size() - 1);
                    recycler.scrollToPosition(messages.size() - 1);
                } catch (Exception e) { Log.e(TAG, "WS parse error", e); }
            });
        }
    }

    @Override protected void onDestroy() { super.onDestroy(); if (socket != null) socket.close(1000, "bye"); }
}