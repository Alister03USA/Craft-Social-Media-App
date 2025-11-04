package com.example.androidexample;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

public class CreateGroupPostActivity extends AppCompatActivity {

    private Button selectImageButton, postButton, backButton;
    private ImageView imagePreview;
    private EditText postTextInput;
    private Uri selectedUri;
    private String groupName;
    private ActivityResultLauncher<String> getContentLauncher;

    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group_post);

        groupName = getIntent().getStringExtra("groupName");

        selectImageButton = findViewById(R.id.selectImageButton);
        postButton = findViewById(R.id.postButton);
        backButton = findViewById(R.id.backToGroupButton);
        imagePreview = findViewById(R.id.imagePreview);
        postTextInput = findViewById(R.id.postTextInput);

        backButton.setOnClickListener(v -> finish());

        getContentLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedUri = uri;
                        imagePreview.setImageURI(uri);
                    }
                });

        selectImageButton.setOnClickListener(v -> getContentLauncher.launch("image/*"));
        postButton.setOnClickListener(v -> uploadPost());
    }

    private void uploadPost() {
        String content = postTextInput.getText().toString().trim();
        String username = SessionManager.getInstance().getLoggedInUsername();
        long groupId = getIntent().getLongExtra("groupId", -1);

        if (groupId == -1) {
            Toast.makeText(this, "Group ID missing", Toast.LENGTH_SHORT).show();
            return;
        }

        if (content.isEmpty() && selectedUri == null) {
            Toast.makeText(this, "Please write something or add an image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Case 1: Image selected (may also have text)
        if (selectedUri != null) {
            byte[] imageData = convertUriToBytes(selectedUri);
            if (imageData == null) {
                Toast.makeText(this, "Failed to read image", Toast.LENGTH_SHORT).show();
                return;
            }

            String fileName = getFileNameFromUri(selectedUri);
            String mimeType = getContentResolver().getType(selectedUri);
            if (mimeType == null) mimeType = "image/jpeg";

            String uploadUrl = BASE_URL + "/groupMessage/" + groupId + "/" + username + "/upload";

            MultipartRequest request = new MultipartRequest(
                    Request.Method.POST,
                    uploadUrl,
                    "file",
                    fileName,
                    mimeType,
                    imageData,
                    response -> {
                        try {
                            JSONObject json = new JSONObject(response);
                            long messageId = json.optLong("messageId", -1);
                            String imageGetUrl = messageId != -1
                                    ? BASE_URL + "/groupMessage/image/" + messageId
                                    : json.optString("filePath", "");

                            // ✅ Send text if it exists
                            if (!content.isEmpty()) {
                                WebSocketManager ws = WebSocketManager.getInstance();
                                ws.sendMessage(content);
                            }

                            Toast.makeText(this, "Posted!", Toast.LENGTH_SHORT).show();
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Upload parse error", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show();
                    }
            );

            VolleySingleton.getInstance(this).addToRequestQueue(request);

        } else {
            // Case 2: Text-only post
            WebSocketManager ws = WebSocketManager.getInstance();
            ws.sendMessage(content);

            Toast.makeText(this, "Posted!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String name = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        name = cursor.getString(index);
                    }
                }
            }
        }
        if (name == null) {
            name = uri.getLastPathSegment();
            if (name == null) name = "uploaded_file";
        }
        return name;
    }

    private byte[] convertUriToBytes(Uri uri) {
        try (InputStream is = getContentResolver().openInputStream(uri);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = is.read(buf)) != -1) baos.write(buf, 0, len);
            return baos.toByteArray();
        } catch (Exception e) { e.printStackTrace(); return null; }
    }
}
