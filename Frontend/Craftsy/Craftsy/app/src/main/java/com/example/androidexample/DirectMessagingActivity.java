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

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request.Builder;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Activity responsible for handling direct messages and group messages.
 * Provides WebSocket real time communication, file sending, reply threading,
 * message reactions, and member management for group chats.
 *
 *@author Ji Xian Fu
 */
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

    /**
     * Called when the activity is created.
     * Initializes the UI, loads chat metadata, establishes socket connection,
     * fetches chat history, and sets up event listeners.
     *
     * @param b saved instance state if recreated
     */
    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_direct_message);

        convoId = getIntent().getStringExtra("convoId");
        chatName = getIntent().getStringExtra("chatName");
        currentUser = getIntent().getStringExtra("username");

        if (currentUser == null || currentUser.isEmpty()) {
            currentUser = "Fuji";
            Toast.makeText(this, "No active user detected. Using fallback Fuji", Toast.LENGTH_SHORT).show();
        }

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

    /**
     * Opens a system file picker to select images or PDF files.
     */
    private void openPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "application/pdf"});
        startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_FILE_REQUEST);
    }

    /**
     * Handles the result returned by file picker.
     *
     * @param req request code
     * @param res result code
     * @param data returned data containing file Uri
     */
    @Override
    protected void onActivityResult(int req, int res, @Nullable Intent data) {
        super.onActivityResult(req, res, data);
        if (req == PICK_FILE_REQUEST && res == RESULT_OK && data != null && data.getData() != null) {
            uploadImageToBackend(data.getData());
        }
    }

    /**
     * Uploads a selected file to the backend server.
     *
     * @param uri file location on device
     */
    private void uploadImageToBackend(Uri uri) {
        try {
            byte[] data = readBytesFromUri(uri);
            String fileName = getFileName(uri);

            String mime = getContentResolver().getType(uri);
            if (mime == null) {
                mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                        MimeTypeMap.getFileExtensionFromUrl(uri.toString()));
            }
            if (mime == null) mime = "image/jpeg";

            String url = BASE_URL + "/images";
            Map<String, String> textParams = new java.util.HashMap<>();
            Map<String, VolleyMultipartRequest.DataPart> fileParams = new java.util.HashMap<>();
            fileParams.put("image", new VolleyMultipartRequest.DataPart(fileName, data, mime));

            VolleyMultipartRequest req = new VolleyMultipartRequest(
                    Request.Method.POST,
                    url,
                    res -> {
                        try {
                            String body = new String(res.data);
                            JSONObject obj = new JSONObject(body);

                            long imageId = obj.optLong("id", -1);
                            String path = obj.optString("filePath", "");

                            if (imageId != -1 && !path.isEmpty()) {
                                String file = path.substring(path.lastIndexOf("/") + 1);
                                String imageUrl = BASE_URL + "/uploads/" + file;

                                addLiveMessage(new MessageItem(
                                        0, currentUser, "", now(), null, imageId, imageUrl
                                ));

                                socketSend("#image:" + imageId);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Parse upload", e);
                        }
                    },
                    err -> Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show(),
                    textParams,
                    fileParams
            );

            VolleySingleton.getInstance(this).addToRequestQueue(req);

        } catch (Exception e) {
            Log.e(TAG, "Upload error", e);
        }
    }

    /**
     * Fetches message history from backend for the current conversation.
     */
    private void fetchHistory() {
        progress.setVisibility(View.VISIBLE);

        String url = BASE_URL + "/messages/" + convoId;
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                res -> {
                    progress.setVisibility(View.GONE);
                    loadHistory(res);
                    parseMembers(res);
                },
                err -> {
                    progress.setVisibility(View.GONE);
                    Log.e(TAG, "History error", err);
                }
        );
        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    /**
     * Loads the user's message history into the RecyclerView.
     *
     * @param convo full JSON object containing messages
     */
    private void loadHistory(JSONObject convo) {
        messages.clear();

        try {
            JSONArray arr = convo.optJSONArray("messages");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    parseHistoryMessage(arr.getJSONObject(i), null);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "History parse", e);
        }

        Collections.sort(messages, (a, b) -> a.getTimestamp().compareTo(b.getTimestamp()));

        adapter.notifyDataSetChanged();
        recycler.scrollToPosition(Math.max(messages.size() - 1, 0));
    }

    /**
     * Recursively parses a message and its replies from history.
     *
     * @param o raw message json
     * @param parent optional parent message id if this is a reply
     */
    private void parseHistoryMessage(JSONObject o, Long parent) {
        try {
            long id = o.optLong("id", 0);
            String sender = o.optString("sender", "");
            String text = o.optString("text", "");
            String ts = o.optString("date", "");

            Long imgId = null;
            String imgUrl = null;

            JSONObject img = o.optJSONObject("image");
            if (img != null) {
                imgId = img.optLong("id", -1);
                String fp = img.optString("filePath", "");
                if (!fp.isEmpty()) {
                    String name = fp.substring(fp.lastIndexOf("/") + 1);
                    imgUrl = BASE_URL + "/uploads/" + name;
                }
            }

            if ((imgId == null || imgId == -1) && text.startsWith("#image:")) {
                try {
                    imgId = Long.parseLong(text.substring(7).trim());
                    String metaUrl = BASE_URL + "/images/" + imgId;

                    final long fId = id;
                    final String fSender = sender;
                    final String fTs = ts;
                    final Long fParent = parent;
                    final Long fImgId = imgId;

                    JsonObjectRequest imgReq = new JsonObjectRequest(
                            Request.Method.GET,
                            metaUrl,
                            null,
                            res -> {
                                String fp = res.optString("filePath", "");
                                if (!fp.isEmpty()) {
                                    String name = fp.substring(fp.lastIndexOf("/") + 1);
                                    String resolvedUrl = BASE_URL + "/uploads/" + name;

                                    messages.add(new MessageItem(fId, fSender, "",
                                            fTs, fParent, fImgId, resolvedUrl));

                                    adapter.notifyDataSetChanged();
                                }
                            },
                            err -> Log.e(TAG, "history img fallback failed", err)
                    );

                    VolleySingleton.getInstance(this).addToRequestQueue(imgReq);
                    return;

                } catch (Exception ex) {
                    Log.e(TAG, "bad fallback #image parse", ex);
                }
            }

            MessageItem m = new MessageItem(id, sender,
                    imgId != null ? "" : text,
                    ts,
                    parent,
                    imgId,
                    imgUrl);

            JSONObject react = o.optJSONObject("reactions");
            if (react != null) {
                Iterator<String> keys = react.keys();
                while (keys.hasNext()) {
                    String k = keys.next();
                    m.getReactions().put(k, react.optInt(k, 0));
                }
            }

            messages.add(m);

            JSONArray replies = o.optJSONArray("replies");
            if (replies != null) {
                for (int i = 0; i < replies.length(); i++) {
                    parseHistoryMessage(replies.getJSONObject(i), id);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "history parse error", e);
        }
    }

    /**
     * Inserts a newly received message into the list and scrolls to bottom.
     *
     * @param m message object
     */
    private void addLiveMessage(MessageItem m) {
        messages.add(m);
        adapter.notifyItemInserted(messages.size() - 1);
        recycler.scrollToPosition(messages.size() - 1);
    }

    /**
     * Sends a message through WebSocket. If replying, formats payload accordingly.
     */
    private void sendMessage() {
        String txt = etInput.getText().toString().trim();
        if (txt.isEmpty()) return;

        if (replyingTo != null)
            socketSend("#reply:" + replyingTo + ":" + txt);
        else
            socketSend(txt);

        addLiveMessage(new MessageItem(0, currentUser, highlightMentions(txt).toString(), now(), replyingTo));
        etInput.setText("");
        clearReplyPreview();
    }

    /**
     * Establishes a WebSocket connection for live messaging.
     */
    private void connectSocket() {
        try {
            String url = WS_BASE + "/chat/" + convoId + "/" + currentUser;
            OkHttpClient c = new OkHttpClient.Builder().build();
            okhttp3.Request r = new Builder().url(url).build();
            socket = c.newWebSocket(r, new LiveWsListener());
        } catch (Exception e) {
            Log.e(TAG, "WS error", e);
        }
    }

    /**
     * WebSocket listener for handling live incoming messages and live updates.
     */
    private class LiveWsListener extends WebSocketListener {

        /**
         * Handles text data received from the WebSocket.
         *
         * @param ws the active websocket
         * @param text received server payload
         */
        @Override
        public void onMessage(WebSocket ws, String text) {

            runOnUiThread(() -> {
                try {

                    if (text.matches("^\\d+:\\w+:\\d+$")) {
                        String[] p = text.split(":");
                        long id = Long.parseLong(p[0]);
                        String emoji = p[1];
                        int count = Integer.parseInt(p[2]);

                        for (MessageItem m : messages) {
                            if (m.getId() == id) {
                                m.getReactions().put(emoji, count);
                                break;
                            }
                        }
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    int idx = text.indexOf(": ");
                    String sender = idx > 0 ? text.substring(0, idx) : "unknown";
                    String body = idx > 0 ? text.substring(idx + 2) : text;

                    if (sender.equals(currentUser)) return;

                    if (body.startsWith("#image:")) {
                        long imgId = Long.parseLong(body.substring(7).trim());
                        fetchImageMetaLive(sender, imgId);
                        return;
                    }

                    if (body.matches("\\d+-.*")) {
                        int cut = body.indexOf("-");
                        long parentId = Long.parseLong(body.substring(0, cut));
                        String replyText = body.substring(cut + 1).trim();
                        addLiveMessage(new MessageItem(0, sender, replyText, now(), parentId));
                        return;
                    }

                    addLiveMessage(new MessageItem(0, sender, highlightMentions(body).toString(), now(), null));

                } catch (Exception e) {
                    Log.e(TAG, "WS parse", e);
                }
            });
        }
    }

    /**
     * Fetches metadata for an image referenced in a live incoming message.
     *
     * @param sender username of sender
     * @param imgId image identifier
     */
    private void fetchImageMetaLive(String sender, long imgId) {
        String url = BASE_URL + "/images/" + imgId;

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                res -> {
                    String fp = res.optString("filePath", "");
                    if (!fp.isEmpty()) {
                        String name = fp.substring(fp.lastIndexOf("/") + 1);
                        String fullUrl = BASE_URL + "/uploads/" + name;
                        addLiveMessage(new MessageItem(0, sender, "", now(), null, imgId, fullUrl));
                    }
                },
                err -> Log.e(TAG, "image fetch live", err)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    /**
     * Prepares the UI for replying to a specific message.
     *
     * @param m the message to reply to
     */
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

    /**
     * Prompts the user to enter a username to add to the group chat.
     */
    private void promptUserAdd() {
        AutoCompleteTextView input = new AutoCompleteTextView(this);
        input.setHint("Enter username to add");
        new AlertDialog.Builder(this)
                .setTitle("Add Member")
                .setView(input)
                .setPositiveButton("Add", (d, i) -> {
                    String u = input.getText().toString().trim();
                    if (!u.isEmpty()) modifyGroupMember(u, true);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Prompts the user to enter a username to remove from the group chat.
     */
    private void promptUserRemove() {
        AutoCompleteTextView input = new AutoCompleteTextView(this);
        input.setHint("Enter username to remove");
        new AlertDialog.Builder(this)
                .setTitle("Remove Member")
                .setView(input)
                .setPositiveButton("Remove", (d, i) -> {
                    String u = input.getText().toString().trim();
                    if (!u.isEmpty()) modifyGroupMember(u, false);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Sends a request to add or remove a group member.
     *
     * @param username target user
     * @param add true to add, false to remove
     */
    private void modifyGroupMember(String username, boolean add) {
        String endpoint = add ? "add" : "remove";
        String url = BASE_URL + "/messages/" + convoId + "/" + endpoint + "/" + username;
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                null,
                res -> {
                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                    parseMembers(res);
                },
                err -> Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
        );
        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    /**
     * Parses group member list for mention auto completion.
     *
     * @param convo raw conversation json
     */
    private void parseMembers(JSONObject convo) {
        memberUsernames.clear();
        try {
            JSONArray arr = convo.optJSONArray("members");
            if (arr == null) return;

            for (int i = 0; i < arr.length(); i++) {
                String u = arr.getJSONObject(i).optString("username", "");
                if (!u.equals(currentUser)) memberUsernames.add("@" + u);
            }
        } catch (Exception e) {
            Log.e(TAG, "member parse", e);
        }
    }

    /**
     * Sets up the watcher used to display mention dropdown when typing '@'.
     */
    private void setupMentionWatcher() {
        etInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                if (s.toString().endsWith("@")) showMentionDropdown();
            }
        });
    }

    /**
     * Shows a dropdown list of group members for mention tagging.
     */
    private void showMentionDropdown() {
        if (memberUsernames.isEmpty()) return;
        ArrayAdapter<String> ad = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, memberUsernames);
        etInput.setAdapter(ad);
        etInput.showDropDown();
    }

    /**
     * Clears the reply preview UI and resets reply state.
     */
    private void clearReplyPreview() {
        replyingTo = null;
        replyingToText = null;
        replyContainer.setVisibility(View.GONE);
        tvReplyPreview.setText("");
    }

    /**
     * Returns a timestamp string for messages.
     *
     * @return current timestamp
     */
    private String now() {
        if (Build.VERSION.SDK_INT >= 26)
            return java.time.LocalDateTime.now().toString();
        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * Highlights all '@username' mentions inside a string.
     *
     * @param text raw message text
     * @return formatted spannable string
     */
    private SpannableString highlightMentions(String text) {
        SpannableString s = new SpannableString(text);
        Matcher m = Pattern.compile("@\\w+").matcher(text);
        while (m.find()) {
            s.setSpan(new ForegroundColorSpan(Color.parseColor("#1565C0")), m.start(), m.end(), 0);
            s.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), m.start(), m.end(), 0);
        }
        return s;
    }

    /**
     * Reads a file's bytes from a given Uri.
     *
     * @param uri file uri
     * @return byte array or null
     */
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

    /**
     * Gets a display friendly file name for a Uri.
     *
     * @param uri target file uri
     * @return file name
     */
    private String getFileName(Uri uri) {
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

    /**
     * Sends a raw payload through the WebSocket.
     *
     * @param payload raw text to send
     */
    private void socketSend(String payload) {
        try {
            if (socket != null) socket.send(payload);
        } catch (Exception e) {
            Log.e(TAG, "ws send", e);
        }
    }

    /**
     * Sends reaction data for a specific message.
     *
     * @param m target message
     * @param type emoji type
     */
    @Override
    public void onReact(MessageItem m, String type) {
        socketSend("#react:" + m.getId() + ":" + type);
    }

    /**
     * Removes a reaction from a specific message.
     *
     * @param m target message
     * @param type emoji type
     */
    @Override
    public void onRemoveReact(MessageItem m, String type) {
        socketSend("#!react:" + m.getId() + ":" + type);
    }

    /**
     * Handles long press on a message. Displays delete confirmation dialog.
     *
     * @param m message to delete
     */
    @Override
    public void onLongPress(MessageItem m) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Message")
                .setMessage("Are you sure you want to delete this message?")
                .setPositiveButton("Delete", (d, i) -> deleteMessage(m))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Sends a DELETE request to remove a message permanently.
     *
     * @param m target message
     */
    private void deleteMessage(MessageItem m) {
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
}