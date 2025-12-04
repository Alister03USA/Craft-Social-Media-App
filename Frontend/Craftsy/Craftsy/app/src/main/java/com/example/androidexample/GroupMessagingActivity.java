package com.example.androidexample;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.util.Map;

public class GroupMessagingActivity extends BaseMessagingActivity {

    private ImageButton btnMenu;
    @Override
    protected void fetchHistory() {
        String url = BASE_URL + "/messages/" + convoId;

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                res -> {
                    parseMembers(res);
                    loadGroupPicture(res);   // <- NEW
                    loadHistory(res);
                },
                err -> Log.e("GroupHistory", "fail", err)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_message);

        convoId = getIntent().getStringExtra("convoId");
        chatName = getIntent().getStringExtra("chatName");
        currentUser = getIntent().getStringExtra("username");

        setupBaseViews();
        setupRecycler();
        setupInput();

        tvTitle.setText(chatName);

        ImageView back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> finish());

        // group icon stays default unless picture is set
        ImageView groupIcon = findViewById(R.id.groupIcon);

        btnMenu = findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(v -> showGroupMenu());

        fetchHistory();
        connectSocket();
    }

    /* ==========================================================
     * POPUP MENU
     * ========================================================== */
    private void showGroupMenu() {
        PopupMenu menu = new PopupMenu(this, btnMenu);
        menu.getMenuInflater().inflate(R.menu.menu_group_actions, menu.getMenu());

        menu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.menu_change_pic) {
                openPickerForGroupPic();
                return true;
            }
            if (id == R.id.menu_add_user) {
                promptUserAdd();
                return true;
            }
            if (id == R.id.menu_remove_user) {
                promptUserRemove();
                return true;
            }
            return false;
        });

        menu.show();
    }

    /* ==========================================================
     * CHANGE GROUP PIC
     * ========================================================== */
    private void openPickerForGroupPic() {
        Intent pick = new Intent(Intent.ACTION_GET_CONTENT);
        pick.setType("image/*");
        startActivityForResult(Intent.createChooser(pick, "Choose Group Picture"), 2001);
    }

    @Override
    protected void onActivityResult(int req, int res, @Nullable Intent data) {
        super.onActivityResult(req, res, data);

        if (req == 2001 && res == RESULT_OK && data != null) {
            Uri img = data.getData();
            if (img != null) uploadNewGroupPicture(img);
        }
    }

    private void uploadNewGroupPicture(Uri uri) {
        try {
            byte[] bytes = readBytesFromUri(uri);
            String fileName = getFileName(uri);

            String mime = getContentResolver().getType(uri);
            if (mime == null) mime = "image/jpeg";

            String uploadUrl = BASE_URL + "/images";

            VolleyMultipartRequest req = new VolleyMultipartRequest(
                    Request.Method.POST,
                    uploadUrl,
                    response -> {
                        try {
                            JSONObject obj = new JSONObject(new String(response.data));
                            long imgId = obj.optLong("id", -1);
                            if (imgId != -1) updateGroupPic(imgId);
                        } catch (Exception ignored) {}
                    },
                    err -> Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show(),
                    null,
                    Map.of("image", new VolleyMultipartRequest.DataPart(fileName, bytes, mime))
            );

            VolleySingleton.getInstance(this).addToRequestQueue(req);

        } catch (Exception e) {
            Toast.makeText(this, "Select a valid image", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateGroupPic(long imgId) {
        String url = BASE_URL + "/messages/" + convoId + "/pic";

        JSONObject body = new JSONObject();
        try {
            body.put("id", imgId);
        } catch (Exception ignored) {}

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                body,
                res -> Toast.makeText(this, "Group picture updated", Toast.LENGTH_SHORT).show(),
                err -> Toast.makeText(this, "Failed to update picture", Toast.LENGTH_SHORT).show()
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    /* ==========================================================
     * ADD / REMOVE USER
     * ========================================================== */
    private void promptUserAdd() {
        AutoCompleteTextView input = new AutoCompleteTextView(this);
        input.setHint("Username");

        new AlertDialog.Builder(this)
                .setTitle("Add User")
                .setView(input)
                .setPositiveButton("Add", (d, i) -> {
                    String u = input.getText().toString().trim();
                    if (!u.isEmpty()) modifyGroupMember(u, true);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void promptUserRemove() {
        AutoCompleteTextView input = new AutoCompleteTextView(this);
        input.setHint("Username");

        new AlertDialog.Builder(this)
                .setTitle("Remove User")
                .setView(input)
                .setPositiveButton("Remove", (d, i) -> {
                    String u = input.getText().toString().trim();
                    if (!u.isEmpty()) modifyGroupMember(u, false);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void modifyGroupMember(String username, boolean add) {
        String url = BASE_URL + "/messages/" + convoId + "/" + (add ? "add" : "remove") + "/" + username;

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
    private void loadGroupPicture(JSONObject convo) {
        try {
            JSONObject picObj = convo.optJSONObject("groupPic");
            if (picObj == null) return;

            long imgId = picObj.optLong("id", -1);
            String fp = picObj.optString("filePath", "");

            if (imgId == -1 || fp.isEmpty()) return;

            String filename = fp.substring(fp.lastIndexOf("/") + 1);
            String fullUrl = BASE_URL + "/uploads/" + filename;

            ImageView groupIcon = findViewById(R.id.groupIcon);

            Glide.with(this)
                    .load(fullUrl)
                    .placeholder(R.drawable.ic_groups)
                    .error(R.drawable.ic_groups)
                    .circleCrop()
                    .into(groupIcon);

        } catch (Exception e) {
            Log.e("GroupImage", "load failed", e);
        }
    }
}