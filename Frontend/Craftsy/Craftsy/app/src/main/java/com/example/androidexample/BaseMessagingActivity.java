package com.example.androidexample;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public abstract class BaseMessagingActivity extends AppCompatActivity implements MessageAdapter.MessageActions {

    protected static final String TAG = "MessagingBase";
    protected static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";
    protected static final String WS_BASE = "ws://coms-3090-028.class.las.iastate.edu:8080";
    protected static final int PICK_FILE_REQUEST = 100;

    public static final Map<String, String> profileCache = new HashMap<>();

    protected String convoId;
    protected String chatName;
    protected String currentUser;

    protected RecyclerView recycler;
    protected MessageAdapter adapter;
    protected List<MessageItem> messages = new ArrayList<>();
    protected List<String> memberUsernames = new ArrayList<>();

    protected AutoCompleteTextView etInput;
    protected TextView tvReplyPreview, tvTitle;
    protected ImageButton btnSend, btnAttach, btnScrollLatest, btnCancelReply;
    protected ProgressBar progress;
    protected View replyContainer;
    protected ImageView topProfileImage;

    protected WebSocket socket;
    protected Long replyingTo = null;
    protected String replyingToText = null;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
    }

    protected void setupBaseViews() {
        recycler = findViewById(R.id.recycler);
        etInput = findViewById(R.id.etInput);
        tvReplyPreview = findViewById(R.id.tvReplyPreview);
        tvTitle = findViewById(R.id.tvTitle);
        btnSend = findViewById(R.id.btnSend);
        btnAttach = findViewById(R.id.btnAttach);
        btnScrollLatest = findViewById(R.id.btnScrollLatest);
        btnCancelReply = findViewById(R.id.btnCancelReply);
        progress = findViewById(R.id.progress);
        replyContainer = findViewById(R.id.replyContainer);
        topProfileImage = findViewById(R.id.topProfileImage);
    }

    protected void setupRecycler() {
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        recycler.setLayoutManager(lm);

        adapter = new MessageAdapter(messages, currentUser, this);
        recycler.setAdapter(adapter);
    }

    protected void setupInput() {
        btnSend.setOnClickListener(v -> sendMessage());
        btnAttach.setOnClickListener(v -> openPicker());
        btnScrollLatest.setOnClickListener(v ->
                recycler.scrollToPosition(Math.max(messages.size() - 1, 0)));
        btnCancelReply.setOnClickListener(v -> clearReplyPreview());

        etInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                if (s.toString().endsWith("@")) showMentionDropdown();
            }
        });
    }

    protected void connectSocket() {
        try {
            String url = WS_BASE + "/chat/" + convoId + "/" + currentUser;
            OkHttpClient client = new OkHttpClient.Builder().build();
            okhttp3.Request req = new okhttp3.Request.Builder().url(url).build();
            socket = client.newWebSocket(req, new LiveWsListener());
        } catch (Exception e) {
            Log.e(TAG, "WS connect error", e);
        }
    }

    private class LiveWsListener extends WebSocketListener {
        @Override
        public void onMessage(WebSocket ws, String text) {
            runOnUiThread(() -> parseIncomingWs(text));
        }
    }

    protected void parseIncomingWs(String text) {
        Log.d("WS-RAW", "Received WS: " + text);
        try {
            if (text.matches("^\\d+:\\w+:\\d+$")) {
                String[] p = text.split(":");
                long id = Long.parseLong(p[0]);
                String type = p[1];
                int count = Integer.parseInt(p[2]);

                for (MessageItem m : messages) {
                    if (m.getId() == id) {
                        m.getReactions().put(type, count);
                        adapter.notifyDataSetChanged();
                        return;
                    }
                }
                return;
            }

            int idx = text.indexOf(": ");
            String sender = idx > 0 ? text.substring(0, idx) : "unknown";
            String body = idx > 0 ? text.substring(idx + 2) : text;

            if (sender.equals(currentUser)) return;

            if (!profileCache.containsKey(sender)) fetchProfile(sender);

            if (body.startsWith("#image:")) {
                long imgId = Long.parseLong(body.substring(7).trim());
                resolveImageIdToUrl(imgId, url -> {
                    if (url != null) {
                        addLiveMessage(new MessageItem(0, sender, "", now(), null, imgId, url));
                    }
                });
                return;
            }

            if (body.matches("\\d+-.*")) {
                int cut = body.indexOf("-");
                long parentId = Long.parseLong(body.substring(0, cut));
                String replyText = body.substring(cut + 1).trim();

                MessageItem m = new MessageItem(0, sender, replyText, now(), parentId);

                // >>> WHATSAPP REPLY ENRICHMENT FOR LIVE MESSAGE
                enrichReplyFields(m);

                addLiveMessage(m);
                return;
            }

            addLiveMessage(new MessageItem(
                    0, sender, highlightMentions(body).toString(), now(), null
            ));

        } catch (Exception e) {
            Log.e(TAG, "WS parse error", e);
        }
    }

    protected void fetchHistory() {
        progress.setVisibility(View.VISIBLE);

        String url = BASE_URL + "/messages/" + convoId;

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                res -> {
                    progress.setVisibility(View.GONE);
                    parseMembers(res);
                    loadHistory(res);
                },
                err -> {
                    progress.setVisibility(View.GONE);
                    Log.e(TAG, "History load fail", err);
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    protected void loadHistory(JSONObject convo) {
        messages.clear();

        try {
            JSONArray arr = convo.optJSONArray("messages");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++)
                    parseHistoryMessage(arr.getJSONObject(i), null);
            }
        } catch (Exception e) {
            Log.e(TAG, "History parse error", e);
        }

        // >>> WHATSAPP REPLY ENRICHMENT FOR ALL HISTORY MESSAGES
        for (MessageItem m : messages) {
            enrichReplyFields(m);
        }

        Collections.sort(messages, (a, b) -> a.getTimestamp().compareTo(b.getTimestamp()));
        adapter.notifyDataSetChanged();
        recycler.scrollToPosition(messages.size() - 1);
    }

    protected void parseHistoryMessage(JSONObject o, Long parent) {
        try {
            long id = o.optLong("id");
            String sender = o.optString("sender", "");
            String text = o.optString("text", "");
            String ts = o.optString("date", "");

            Long imgId = null;
            String imgUrl = null;

            JSONObject imgObj = o.optJSONObject("image");
            if (imgObj != null) {
                imgId = imgObj.optLong("id", -1);
                String fp = imgObj.optString("filePath", "");
                if (imgId != -1 && !fp.isEmpty()) {
                    String name = fp.substring(fp.lastIndexOf("/") + 1);
                    imgUrl = BASE_URL + "/uploads/" + name;
                }
            }

            MessageItem m = new MessageItem(id, sender,
                    imgId != null ? "" : text, ts, parent, imgId, imgUrl);

            JSONObject react = o.optJSONObject("reactions");
            if (react != null) {
                Iterator<String> keys = react.keys();
                while (keys.hasNext()) {
                    String k = keys.next();
                    m.getReactions().put(k, react.optInt(k));
                }
            }

            messages.add(m);

            JSONArray replies = o.optJSONArray("replies");
            if (replies != null) {
                for (int i = 0; i < replies.length(); i++)
                    parseHistoryMessage(replies.getJSONObject(i), id);
            }

        } catch (Exception e) {
            Log.e(TAG, "history msg error", e);
        }
    }

    // =====================================================================
    // WHATSAPP REPLY LOGIC: fill replySender + replySnippet
    // =====================================================================
    protected void enrichReplyFields(MessageItem m) {
        if (m.getReplyTo() == null) return;

        for (MessageItem parent : messages) {
            if (parent.getId() == m.getReplyTo()) {
                m.setReplySender(parent.getSender());

                String original = parent.getContent();
                if (original == null || original.isEmpty()) original = "(image)";

                String snippet = original.length() > 40
                        ? original.substring(0, 40) + "..."
                        : original;

                m.setReplySnippet(snippet);
                return;
            }
        }
    }

    protected void fetchProfile(String username) {
        String url = BASE_URL + "/search/user?query=" + username;

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                res -> {
                    try {
                        JSONObject match = null;
                        for (int i = 0; i < res.length(); i++) {
                            JSONObject u = res.getJSONObject(i);
                            if (username.equalsIgnoreCase(u.optString("username"))) {
                                match = u;
                                break;
                            }
                        }

                        if (match == null) return;

                        String imgUrl = match.optString("imageURL", null);
                        if (imgUrl == null || imgUrl.equals("null") || imgUrl.isEmpty()) return;

                        String metaUrl = BASE_URL + imgUrl;

                        JsonObjectRequest metaReq = new JsonObjectRequest(
                                Request.Method.GET,
                                metaUrl,
                                null,
                                meta -> {
                                    String fp = meta.optString("filePath", "");
                                    if (!fp.isEmpty()) {
                                        String filename = fp.substring(fp.lastIndexOf("/") + 1);
                                        String full = BASE_URL + "/uploads/" + filename;
                                        profileCache.put(username, full);
                                    }
                                },
                                err -> Log.e(TAG, "meta fail: " + username)
                        );

                        VolleySingleton.getInstance(this).addToRequestQueue(metaReq);

                    } catch (Exception e) {
                        Log.e(TAG, "profile parse", e);
                    }
                },
                err -> Log.e(TAG, "profile fail (search): " + username)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    protected void resolveImageIdToUrl(long imgId, ImageUrlCallback cb) {
        String meta = BASE_URL + "/images/" + imgId;

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                meta,
                null,
                res -> {
                    String fp = res.optString("filePath", "");
                    if (!fp.isEmpty()) {
                        String name = fp.substring(fp.lastIndexOf("/") + 1);
                        cb.onResolved(BASE_URL + "/uploads/" + name);
                    } else cb.onResolved(null);
                },
                err -> cb.onResolved(null)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    interface ImageUrlCallback {
        void onResolved(String url);
    }

    protected void sendMessage() {
        String txt = etInput.getText().toString().trim();
        if (txt.isEmpty()) return;

        if (replyingTo != null)
            socketSend(replyingTo + "-" + txt);
        else
            socketSend(txt);

        MessageItem m = new MessageItem(
                0, currentUser, highlightMentions(txt).toString(), now(), replyingTo
        );

        // >>> enrich local echo too
        enrichReplyFields(m);

        addLiveMessage(m);

        etInput.setText("");
        clearReplyPreview();
    }

    protected void addLiveMessage(MessageItem m) {
        messages.add(m);
        adapter.notifyItemInserted(messages.size() - 1);
        recycler.scrollToPosition(messages.size() - 1);
    }

    protected void socketSend(String payload) {
        try {
            if (socket != null) socket.send(payload);
        } catch (Exception e) {
            Log.e(TAG, "ws send error", e);
        }
    }

    @Override
    public void onReply(MessageItem m) {
        replyingTo = m.getId();
        replyingToText = m.getContent();

        String shortText = replyingToText != null && replyingToText.length() > 40
                ? replyingToText.substring(0, 40) + "..."
                : replyingToText;

        replyContainer.setVisibility(View.VISIBLE);
        tvReplyPreview.setText("Replying to \"" + shortText + "\"");
    }

    protected void clearReplyPreview() {
        replyingTo = null;
        replyingToText = null;
        replyContainer.setVisibility(View.GONE);
        tvReplyPreview.setText("");
    }

    @Override
    public void onReact(MessageItem m, String type) {
        socketSend("#react:" + m.getId() + ":" + type);
    }

    @Override
    public void onRemoveReact(MessageItem m, String type) {
        socketSend("#!react:" + m.getId() + ":" + type);
    }

    @Override
    public void onLongPress(MessageItem m) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Message")
                .setMessage("Delete this message?")
                .setPositiveButton("Delete", (d, i) -> deleteMessage(m))
                .setNegativeButton("Cancel", null)
                .show();
    }

    protected void deleteMessage(MessageItem m) {
        String url = BASE_URL + "/messages/" + m.getId();

        StringRequest req = new StringRequest(
                Request.Method.DELETE,
                url,
                res -> {
                    messages.remove(m);
                    adapter.notifyDataSetChanged();
                },
                err -> Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show()
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    protected void parseMembers(JSONObject convo) {
        memberUsernames.clear();

        try {
            JSONArray arr = convo.optJSONArray("members");
            if (arr == null) return;

            for (int i = 0; i < arr.length(); i++) {
                JSONObject mem = arr.getJSONObject(i);
                String uname = mem.optString("username", "");

                if (!uname.equals(currentUser))
                    memberUsernames.add("@" + uname);

                String fp = mem.optString("profileImageUrl", null);
                if (fp != null && !fp.equals("null") && !fp.isEmpty()) {
                    String filename = fp.substring(fp.lastIndexOf("/") + 1);
                    profileCache.put(uname, BASE_URL + "/uploads/" + filename);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "parseMembers", e);
        }
    }

    protected void showMentionDropdown() {
        if (memberUsernames.isEmpty()) return;
        ArrayAdapter<String> ad = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, memberUsernames
        );
        etInput.setAdapter(ad);
        etInput.showDropDown();
    }

    protected SpannableString highlightMentions(String t) {
        SpannableString s = new SpannableString(t);
        Matcher m = Pattern.compile("@\\w+").matcher(t);

        while (m.find()) {
            s.setSpan(new ForegroundColorSpan(Color.parseColor("#1565C0")),
                    m.start(), m.end(), 0);
            s.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                    m.start(), m.end(), 0);
        }
        return s;
    }

    protected void openPicker() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("*/*");
        i.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "application/pdf"});
        startActivityForResult(Intent.createChooser(i, "Select File"), PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int r, int res, @Nullable Intent data) {
        super.onActivityResult(r, res, data);
        if (r == PICK_FILE_REQUEST && res == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) uploadFile(uri);
        }
    }

    protected void uploadFile(Uri uri) {
        try {
            byte[] bytes = readBytesFromUri(uri);
            String fileName = getFileName(uri);

            String mime = getContentResolver().getType(uri);
            if (mime == null)
                mime = MimeTypeMap.getSingleton()
                        .getMimeTypeFromExtension(
                                MimeTypeMap.getFileExtensionFromUrl(uri.toString()));

            if (mime == null) mime = "image/jpeg";

            String url = BASE_URL + "/images";

            Map<String, String> t = new HashMap<>();
            Map<String, VolleyMultipartRequest.DataPart> f = new HashMap<>();
            f.put("image", new VolleyMultipartRequest.DataPart(fileName, bytes, mime));

            VolleyMultipartRequest req = new VolleyMultipartRequest(
                    Request.Method.POST,
                    url,
                    res -> {
                        try {
                            JSONObject obj = new JSONObject(new String(res.data));
                            long imgId = obj.optLong("id", -1);
                            String fp = obj.optString("filePath", "");

                            if (imgId != -1 && !fp.isEmpty()) {
                                String name = fp.substring(fp.lastIndexOf("/") + 1);
                                String imageUrl = BASE_URL + "/uploads/" + name;

                                MessageItem m = new MessageItem(
                                        0, currentUser, "", now(), null, imgId, imageUrl
                                );

                                addLiveMessage(m);
                                socketSend("#image:" + imgId);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Upload parse", e);
                        }
                    },
                    err -> Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show(),
                    t,
                    f
            );

            VolleySingleton.getInstance(this).addToRequestQueue(req);

        } catch (Exception e) {
            Log.e(TAG, "uploadFile error", e);
        }
    }

    protected byte[] readBytesFromUri(Uri uri) {
        try (InputStream in = getContentResolver().openInputStream(uri);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) > 0)
                out.write(buf, 0, n);

            return out.toByteArray();

        } catch (Exception e) {
            return null;
        }
    }

    protected String getFileName(Uri uri) {
        String res = null;

        if ("content".equals(uri.getScheme())) {
            try (Cursor c = getContentResolver().query(uri, null, null, null, null)) {
                if (c != null && c.moveToFirst()) {
                    int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (idx >= 0) res = c.getString(idx);
                }
            }
        }

        if (res == null) {
            res = uri.getPath();
            int cut = res != null ? res.lastIndexOf('/') : -1;
            if (cut != -1) res = res.substring(cut + 1);
        }

        return res;
    }

    protected String now() {
        if (Build.VERSION.SDK_INT >= 26)
            return java.time.LocalDateTime.now().toString();
        return String.valueOf(System.currentTimeMillis());
    }
}