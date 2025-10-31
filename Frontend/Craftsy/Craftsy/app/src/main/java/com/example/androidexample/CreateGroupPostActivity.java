package com.example.androidexample;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.android.volley.toolbox.JsonObjectRequest;


import java.io.*;

public class CreateGroupPostActivity extends AppCompatActivity {

    private Button selectImageButton, postButton, backButton;
    private ImageView imagePreview;
    private EditText postTextInput;
    private Uri selectedUri;
    private String groupName;
    private ActivityResultLauncher<String> getContentLauncher;

    private static final String IMAGE_UPLOAD_URL = "http://coms-3090-028.class.las.iastate.edu:8080/images";
    private static final String POST_URL_BASE = "http://coms-3090-028.class.las.iastate.edu:8080/groups";

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

        if (content.isEmpty() && selectedUri == null) {
            Toast.makeText(this, "Please write something or add an image", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedUri != null) {
            byte[] imageData = convertUriToBytes(selectedUri);
            if (imageData == null) { Toast.makeText(this, "Failed to read image", Toast.LENGTH_SHORT).show(); return; }

            String fileName = getFileNameFromUri(selectedUri);
            String mimeType = getContentResolver().getType(selectedUri);
            if (mimeType == null) mimeType = "image/jpeg";

            MultipartRequest request = new MultipartRequest(
                    Request.Method.POST,
                    IMAGE_UPLOAD_URL,
                    "image",
                    fileName,
                    mimeType,
                    imageData,
                    response -> {
                        try {
                            long imageId = new JSONObject(response).getLong("id");
                            createTextPost(username, content, imageId);
                        } catch (JSONException e) {
                            Toast.makeText(this, "Error parsing upload response", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
            );
            VolleySingleton.getInstance(this).addToRequestQueue(request);
        } else {
            createTextPost(username, content, null);
        }
    }

    private void createTextPost(String username, String content, Long imageId) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("username", username);
            jsonBody.put("content", content);
            if (imageId != null) {
                JSONArray images = new JSONArray();
                JSONObject img = new JSONObject();
                img.put("id", imageId);
                images.put(img);
                jsonBody.put("images", images);
            }

            String url = POST_URL_BASE + "/" + groupName + "/posts";

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST, url, jsonBody,
                    response -> {
                        Toast.makeText(this, "Post uploaded!", Toast.LENGTH_SHORT).show();
                        finish();
                    },
                    error -> Toast.makeText(this, "Failed to upload post", Toast.LENGTH_SHORT).show()
            );

            VolleySingleton.getInstance(this).addToRequestQueue(request);

        } catch (JSONException e) { e.printStackTrace(); }
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
