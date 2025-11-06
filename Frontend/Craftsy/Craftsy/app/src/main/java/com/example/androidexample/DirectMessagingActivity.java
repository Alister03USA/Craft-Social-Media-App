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
import android.text.TextUtils;
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
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import okhttp3.OkHttpClient;
import okhttp3.Request.Builder;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class DirectMessagingActivity extends AppCompatActivity implements MessageAdapter.MessageActions {

    private static final String TAG = "DirectMessage";
    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";
    private static final String WS_BASE = "ws://coms-3090-028.class.las.iastate.edu:8080";
    private static final int PICK_FILE_REQUEST = 100;

    private String convoId;
    private String chatName;
    private String currentUser;

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
    private ImageButton btnAddUser, btnRemoveUser;
    private View groupActionsContainer;

    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_direct_message);

        convoId = getIntent().getStringExtra("convoId");
        chatName = getIntent().getStringExtra("chatName");
        currentUser = getIntent().getStringExtra("username");

        if (currentUser == null || currentUser.isEmpty()) {
            currentUser = "Fuji"; // fallback for test
            Toast.makeText(this, "⚠️ No active user detected. Using fallback 'Fuji'", Toast.LENGTH_SHORT).show();
        }

        Log.d(TAG, "👤 Active user: " + currentUser + " | Chat: " + chatName);

        tvTitle = findViewById(R.id.tvTitle);
        etInput = findViewById(R.id.etInput);
        tvReplyPreview = findViewById(R.id.tvReplyPreview);
        btnSend = findViewById(R.id.btnSend);
        btnAttach = findViewById(R.id.btnAttach);
        btnScrollLatest = findViewById(R.id.btnScrollLatest);
        btnCancelReply = findViewById(R.id.btnCancelReply);
        progress = findViewById(R.id.progress);
        replyContainer = findViewById(R.id.replyContainer);
        groupActionsContainer = findViewById(R.id.groupActionsContainer);
        btnAddUser = findViewById(R.id.btnAddUser);
        btnRemoveUser = findViewById(R.id.btnRemoveUser);

        if (convoId != null && convoId.startsWith("G-")) {
            groupActionsContainer.setVisibility(View.VISIBLE);
            btnAddUser.setOnClickListener(v -> promptUserAdd());
            btnRemoveUser.setOnClickListener(v -> promptUserRemove());
        } else {
            groupActionsContainer.setVisibility(View.GONE);
        }

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

    /* ==================== FILE PICKER ==================== */
    private void openPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "application/pdf"});
        startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            uploadImageToBackend(uri);
        }
    }

    private void uploadImageToBackend(Uri uri) {

        try {

            final byte[] fileData = readBytesFromUri(uri);

            final String fileName = getFileName(uri);



            // ✅ detect MIME

            String tempType = getContentResolver().getType(uri);

            if (tempType == null) {

                tempType = MimeTypeMap.getSingleton()

                        .getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()));

            }

            final String mimeType = tempType != null ? tempType : "image/jpeg";



            String url = BASE_URL + "/images";

            Log.d(TAG, "📤 Uploading image to: " + url + " (" + fileName + ")");



            // ✅ Build params for Craftsy version of VolleyMultipartRequest

            Map<String, String> textParams = new java.util.HashMap<>();

            Map<String, VolleyMultipartRequest.DataPart> fileParams = new java.util.HashMap<>();

            fileParams.put("image", new VolleyMultipartRequest.DataPart(fileName, fileData, mimeType));



            VolleyMultipartRequest request = new VolleyMultipartRequest(

                    Request.Method.POST,

                    url,

                    response -> {

                        try {

                            String result = new String(response.data);

                            Log.d(TAG, "✅ Upload success: " + result);

                            JSONObject res = new JSONObject(result);

                            long imageId = res.optLong("id", -1);

                            String filePath = res.optString("filePath", "");



                            if (imageId != -1 && !TextUtils.isEmpty(filePath)) {

                                String uploadedFileName = filePath.substring(filePath.lastIndexOf("/") + 1);

                                String imageUrl = BASE_URL + "/uploads/" + uploadedFileName;



                                // Display locally immediately

                                messages.add(new MessageItem(0, currentUser, "", now(), null, imageId, imageUrl));

                                adapter.notifyItemInserted(messages.size() - 1);

                                recycler.scrollToPosition(messages.size() - 1);



                                // Notify backend socket

                                socketSend("#image:" + imageId);

                                Toast.makeText(this, "Image uploaded & displayed", Toast.LENGTH_SHORT).show();

                            } else {

                                Toast.makeText(this, "Image uploaded but missing server response", Toast.LENGTH_SHORT).show();

                            }

                        } catch (Exception e) {

                            Log.e(TAG, "⚠️ Parse error", e);

                        }

                    },

                    error -> {

                        Log.e(TAG, "❌ Upload failed", error);

                        Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();

                    },

                    textParams,

                    fileParams

            );



            VolleySingleton.getInstance(this).addToRequestQueue(request);



        } catch (Exception e) {

            Log.e(TAG, "💥 File upload error", e);

        }

    }

    /* ==================== MESSAGE LOGIC ==================== */
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

    /** ✅ Flatten replies recursively **/
    private void parseConversation(JSONObject convo) {
        messages.clear();
        try {
            JSONArray arr = convo.optJSONArray("messages");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject msg = arr.getJSONObject(i);
                    parseMessageWithReplies(msg, null);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "⚠️ Parse error", e);
        }
        adapter.notifyDataSetChanged();
        recycler.scrollToPosition(Math.max(messages.size() - 1, 0));
    }

    /** 🔁 Recursive flatten replies (enhanced for missing image metadata) **/
    private void parseMessageWithReplies(JSONObject o, Long parentId) {
        try {
            long id = o.optLong("id", 0);
            String sender = o.optString("sender", "");
            String text = o.optString("text", "");
            String ts = o.optString("date", "");

            Long imageId = null;
            String imageUrl = null;
            JSONObject imageObj = o.optJSONObject("image");

            if (imageObj != null) {
                imageId = imageObj.optLong("id", -1);
                String filePath = imageObj.optString("filePath", "");
                if (!TextUtils.isEmpty(filePath)) {
                    String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
                    imageUrl = BASE_URL + "/uploads/" + fileName;
                }
            } else if (text != null && text.startsWith("#image:")) {
                try {
                    long tempId = Long.parseLong(text.replace("#image:", "").trim());
                    String metaUrl = BASE_URL + "/images/" + tempId;
                    Log.d(TAG, "📦 Fetching image meta for history id=" + tempId);

                    JsonObjectRequest imgReq = new JsonObjectRequest(Request.Method.GET, metaUrl, null,
                            res -> {
                                String fp = res.optString("filePath", "");
                                if (!TextUtils.isEmpty(fp)) {
                                    String fn = fp.substring(fp.lastIndexOf("/") + 1);
                                    String iUrl = BASE_URL + "/uploads/" + fn;
                                    Log.d(TAG, "✅ History image resolved: " + iUrl);
                                    messages.add(new MessageItem(id, sender, "", ts, parentId, tempId, iUrl));
                                    adapter.notifyDataSetChanged();
                                }
                            },
                            err -> Log.e(TAG, "❌ Image meta fetch fail", err)
                    );
                    VolleySingleton.getInstance(this).addToRequestQueue(imgReq);
                } catch (Exception ex) {
                    Log.e(TAG, "⚠️ History image parse fail", ex);
                }
                text = "";
            }

            MessageItem m = new MessageItem(id, sender, highlightMentions(text).toString(), ts, parentId, imageId, imageUrl);

            JSONObject reactObj = o.optJSONObject("reactions");
            if (reactObj != null) {
                Iterator<String> keys = reactObj.keys();
                while (keys.hasNext()) {
                    String k = keys.next();
                    m.getReactions().put(k, reactObj.optInt(k, 0));
                }
            }

            messages.add(m);

            JSONArray repliesArr = o.optJSONArray("replies");
            if (repliesArr != null) {
                for (int i = 0; i < repliesArr.length(); i++) {
                    parseMessageWithReplies(repliesArr.getJSONObject(i), id);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "⚠️ Reply parse error", e);
        }
    }

    // everything else remains the same — keep your onMessage() (already correct), socketSend, and helper methods…
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
            Log.e(TAG, "⚠️ Member parse error", e);
        }
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

    private final class WsListener extends WebSocketListener {
        @Override
        public void onMessage(WebSocket ws, String t) {
            runOnUiThread(() -> {
                try {
                    Log.d(TAG, "🧩 WS RAW MESSAGE: " + t);

                    // ✅ Reaction updates
                    if (t.matches("^\\d+:\\w+:\\d+$")) {
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

                    // ✅ Normal message pattern: sender: body
                    int i = t.indexOf(": ");
                    String sender = i > 0 ? t.substring(0, i) : "unknown";
                    String body = i > 0 ? t.substring(i + 2) : t;
                    Log.d(TAG, "💬 Parsed sender=" + sender + " body=" + body);

                    if (sender.equals(currentUser)) {
                        Log.d(TAG, "Skipping self-message echo");
                        return;
                    }

                    // 🖼️ Handle image messages (#image:id)
                    if (body.startsWith("#image:")) {
                        try {
                            long imageId = Long.parseLong(body.substring(7).trim());
                            String url = BASE_URL + "/images/" + imageId;
                            Log.d(TAG, "🖼️ Fetching image metadata from " + url);

                            JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                                    res -> {
                                        String filePath = res.optString("filePath", "");
                                        if (!TextUtils.isEmpty(filePath)) {
                                            String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
                                            String imageUrl = BASE_URL + "/uploads/" + fileName;
                                            Log.d(TAG, "✅ Image resolved: " + imageUrl);
                                            messages.add(new MessageItem(0, sender, "", now(), null, imageId, imageUrl));
                                            adapter.notifyItemInserted(messages.size() - 1);
                                            recycler.scrollToPosition(messages.size() - 1);
                                        } else {
                                            Log.w(TAG, "⚠️ Missing filePath for image " + imageId);
                                        }
                                    },
                                    err -> Log.e(TAG, "❌ Failed to fetch image metadata", err)
                            );
                            VolleySingleton.getInstance(DirectMessagingActivity.this).addToRequestQueue(req);

                        } catch (Exception ex) {
                            Log.e(TAG, "⚠️ Image parse error", ex);
                        }
                        return;
                    }

                    // 💬 Handle replies
                    if (body.matches("\\d+-.*")) {
                        int idx = body.indexOf("-");
                        long parentId = Long.parseLong(body.substring(0, idx));
                        String replyText = body.substring(idx + 1).trim();
                        messages.add(new MessageItem(0, sender, replyText, now(), parentId));
                        adapter.notifyItemInserted(messages.size() - 1);
                        recycler.scrollToPosition(messages.size() - 1);
                        return;
                    }

                    // 🧾 Regular text message
                    messages.add(new MessageItem(0, sender, highlightMentions(body).toString(), now(), null));
                    adapter.notifyItemInserted(messages.size() - 1);
                    recycler.scrollToPosition(messages.size() - 1);

                } catch (Exception e) {
                    Log.e(TAG, "💥 WS parse error", e);
                }
            });
        }
    }

    /** Helper to map ID → real file name */
    private String findImageFilename(long id) {
        // Try common folders (both backend users)
        String[] dirs = {"/home/ages2023/uploads/", "/home/kkeck/uploads/"};
        for (String d : dirs) {
            // We don't know the exact file type — guess JPG
            return "image_" + id + ".jpg";
        }
        return "image_" + id + ".jpg";
    }

    private void promptUserAdd() {
        AutoCompleteTextView input = new AutoCompleteTextView(this);
        input.setHint("Enter username to add");
        new AlertDialog.Builder(this)
                .setTitle("Add Member")
                .setView(input)
                .setPositiveButton("Add", (d, i) -> {
                    String username = input.getText().toString().trim();
                    if (!username.isEmpty()) modifyGroupMember(username, true);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void promptUserRemove() {
        AutoCompleteTextView input = new AutoCompleteTextView(this);
        input.setHint("Enter username to remove");
        new AlertDialog.Builder(this)
                .setTitle("Remove Member")
                .setView(input)
                .setPositiveButton("Remove", (d, i) -> {
                    String username = input.getText().toString().trim();
                    if (!username.isEmpty()) modifyGroupMember(username, false);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void modifyGroupMember(String username, boolean add) {
        String endpoint = add ? "add" : "remove";
        String url = BASE_URL + "/messages/" + convoId + "/" + endpoint + "/" + username;
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, url, null,
                res -> {
                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                    parseMembers(res);
                },
                err -> {
                    Log.e(TAG, "❌ Modify member failed", err);
                    Toast.makeText(this, "Failed to modify member", Toast.LENGTH_SHORT).show();
                });
        VolleySingleton.getInstance(this).addToRequestQueue(req);
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
    }

    @Override
    public void onRemoveReact(MessageItem m, String reactionType) {
        socketSend("#!react:" + m.getId() + ":" + reactionType);
    }

    @Override
    public void onLongPress(MessageItem m) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Message")
                .setMessage("Are you sure you want to delete this message?")
                .setPositiveButton("Delete", (d, i) -> deleteMessage(m))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteMessage(MessageItem m) {
        String url = BASE_URL + "/messages/" + m.getId();
        StringRequest req = new StringRequest(Request.Method.DELETE, url,
                res -> {
                    Toast.makeText(this, "Message deleted", Toast.LENGTH_SHORT).show();
                    messages.remove(m);
                    adapter.notifyDataSetChanged();
                },
                err -> Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show());
        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void setupMentionWatcher() {
        etInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
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

    private byte[] readBytesFromUri(Uri uri) {
        try (InputStream in = getContentResolver().openInputStream(uri);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (in == null) return null;
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            return out.toByteArray();
        } catch (Exception e) {
            return null;
        }
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
}