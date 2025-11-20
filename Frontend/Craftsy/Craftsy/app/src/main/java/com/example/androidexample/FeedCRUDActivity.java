package com.example.androidexample;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Activity that supports Create, Update, and Delete actions for feed posts.
 * Users can upload images, edit post details, or delete posts entirely.
 * Communicates with backend endpoints through Volley requests.
 *
 * @author Ji Xian Fu
 */
public class FeedCRUDActivity extends AppCompatActivity {

    private static final String TAG = "FeedCRUDActivity";
    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";
    private static final int PICK_IMAGE_REQUEST = 101;

    private EditText editProjectName, editDesc, editType, editSupplies, editVisibility;
    private Button buttonSave, buttonDelete, buttonBack, btnChooseImage;
    private ImageView imagePreview;
    private Uri selectedImageUri;

    private String loggedInUsername;
    private String mode;

    /**
     * Called when the activity is created.
     * Initializes UI components, reads intent extras, and configures screen
     * based on mode (create, edit, or delete).
     *
     * @param savedInstanceState previous state if activity recreated
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_crud);

        editProjectName = findViewById(R.id.editProjectName);
        editDesc = findViewById(R.id.editDesc);
        editType = findViewById(R.id.editType);
        editSupplies = findViewById(R.id.editSupplies);
        editVisibility = findViewById(R.id.editVisibility);
        buttonSave = findViewById(R.id.buttonSave);
        buttonDelete = findViewById(R.id.buttonDelete);
        buttonBack = findViewById(R.id.buttonBack);
        btnChooseImage = findViewById(R.id.btnChooseImage);
        imagePreview = findViewById(R.id.imagePreview);

        mode = getIntent().getStringExtra("mode");
        loggedInUsername = getIntent().getStringExtra("username");

        if (getIntent().hasExtra("projectName")) {
            editProjectName.setText(getIntent().getStringExtra("projectName"));
        }
        if (getIntent().hasExtra("projectDesc")) {
            editDesc.setText(getIntent().getStringExtra("projectDesc"));
        }
        if (getIntent().hasExtra("projectType")) {
            editType.setText(getIntent().getStringExtra("projectType"));
        }
        if (getIntent().hasExtra("supplies")) {
            editSupplies.setText(getIntent().getStringExtra("supplies"));
        }
        if (getIntent().hasExtra("visibility")) {
            editVisibility.setText(getIntent().getStringExtra("visibility"));
        }

        buttonBack.setOnClickListener(v -> finish());
        btnChooseImage.setOnClickListener(v -> openImageChooser());

        if ("edit".equals(mode)) {
            buttonSave.setText("Update Post");
            buttonDelete.setEnabled(false);
            btnChooseImage.setEnabled(false);
            buttonSave.setOnClickListener(v -> updateFeed(editProjectName.getText().toString()));
        } else if ("delete".equals(mode)) {
            disableInputs();
            btnChooseImage.setEnabled(false);
            buttonSave.setEnabled(false);
            buttonDelete.setText("Confirm Delete");
            String projectName = getIntent().getStringExtra("projectName");
            buttonDelete.setOnClickListener(v -> deleteFeed(projectName));
        } else {
            buttonDelete.setEnabled(false);
            buttonSave.setOnClickListener(v -> {
                if (selectedImageUri != null) uploadImageAndCreateFeed();
                else createFeed(null);
            });
        }
    }

    /**
     * Disables UI inputs when user is in delete confirmation mode.
     * Keeps values visible but not editable.
     */
    private void disableInputs() {
        editProjectName.setEnabled(false);
        editDesc.setEnabled(false);
        editType.setEnabled(false);
        editSupplies.setEnabled(false);
        editVisibility.setEnabled(false);
    }

    /**
     * Opens file chooser to allow user to select an image from device storage.
     */
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    /**
     * Handles the result from image selection and previews the chosen image.
     *
     * @param requestCode the type of request
     * @param resultCode result status
     * @param data contains selected image data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                imagePreview.setImageURI(selectedImageUri);
                Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Uploads selected image to backend before creating a new feed post.
     * After upload is complete, backend image id is used inside createFeed.
     */
    private void uploadImageAndCreateFeed() {
        try {
            byte[] fileData = readBytesFromUri(selectedImageUri);
            String fileName = getFileName(selectedImageUri);
            Log.d(TAG, "Preparing upload: " + fileName + " (" + fileData.length + " bytes)");

            VolleyMultipartRequest request = new VolleyMultipartRequest(
                    Request.Method.POST,
                    BASE_URL + "/images",
                    response -> {
                        try {
                            String resp = new String(response.data, StandardCharsets.UTF_8);
                            Log.d(TAG, "Server response: " + resp);
                            JSONObject obj = new JSONObject(resp);
                            long imageId = obj.getLong("id");
                            createFeed(imageId);
                        } catch (Exception e) {
                            Log.e(TAG, "Parse error", e);
                        }
                    },
                    error -> {
                        String msg = "";
                        if (error.networkResponse != null && error.networkResponse.data != null)
                            msg = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                        Log.e(TAG, "Upload failed: " + msg, error);
                        Toast.makeText(this, "Upload failed: " + msg, Toast.LENGTH_LONG).show();
                    },
                    new HashMap<>(),
                    getImagePart(fileName, fileData)
            );
            VolleySingleton.getInstance(this).addToRequestQueue(request);
        } catch (IOException e) {
            Log.e(TAG, "Image read failed", e);
        }
    }

    /**
     * Packages the multipart image data for upload.
     *
     * @param fileName chosen file name
     * @param fileData raw byte content of the image
     * @return map containing multipart data for Volley request
     */
    private Map<String, VolleyMultipartRequest.DataPart> getImagePart(String fileName, byte[] fileData) {
        Map<String, VolleyMultipartRequest.DataPart> map = new HashMap<>();
        map.put("image", new VolleyMultipartRequest.DataPart(fileName, fileData, "image/jpeg"));
        return map;
    }

    /**
     * Reads bytes from a file Uri using content resolver.
     *
     * @param uri source file Uri
     * @return byte array of file contents
     * @throws IOException when read fails
     */
    private byte[] readBytesFromUri(Uri uri) throws IOException {
        try (InputStream input = getContentResolver().openInputStream(uri);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int n;
            while ((n = input.read(buffer)) >= 0) output.write(buffer, 0, n);
            return output.toByteArray();
        }
    }

    /**
     * Attempts to extract the name of a file represented by a Uri.
     *
     * @param uri Uri of the selected image
     * @return readable file name
     */
    private String getFileName(Uri uri) {
        String result = null;
        try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) result = cursor.getString(nameIndex);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving file name: " + e.getMessage());
        }
        return result != null ? result : "image.jpg";
    }

    /**
     * Creates a new feed post using provided details and optional image.
     *
     * @param imageId backend id of uploaded image, or null if no image attached
     */
    private void createFeed(Long imageId) {
        JSONObject json = new JSONObject();
        try {
            json.put("projectName", editProjectName.getText().toString());
            json.put("projectDesc", editDesc.getText().toString());
            json.put("projectType", editType.getText().toString());
            json.put("supplies", editSupplies.getText().toString());
            json.put("visibility", editVisibility.getText().toString());

            if (imageId != null) {
                JSONArray arr = new JSONArray();
                JSONObject img = new JSONObject();
                img.put("id", imageId);
                arr.put(img);
                json.put("images", arr);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = BASE_URL + "/feed/" + loggedInUsername;
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, json,
                response -> {
                    Toast.makeText(this, "Post added successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    Log.e(TAG, "Post add failed", error);
                    Toast.makeText(this, "Post add failed", Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    /**
     * Sends a PUT request to update an existing feed post.
     *
     * @param projectName the name of the project being updated
     */
    private void updateFeed(String projectName) {
        JSONObject json = new JSONObject();
        try {
            json.put("projectDesc", editDesc.getText().toString());
            json.put("projectType", editType.getText().toString());
            json.put("supplies", editSupplies.getText().toString());
            json.put("visibility", editVisibility.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = BASE_URL + "/feed/" + loggedInUsername + "/" + projectName;
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, url, json,
                response -> {
                    Toast.makeText(this, "Post updated", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show());
        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    /**
     * Deletes a feed post using project name as identifier.
     * Sends a DELETE request to backend.
     *
     * @param projectName project to delete
     */
    private void deleteFeed(String projectName) {
        if (projectName == null || projectName.trim().isEmpty()) {
            Toast.makeText(this, "Project name is missing — cannot delete.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "/feed/" + loggedInUsername + "/" + projectName.trim();
        Log.d(TAG, "DELETE → " + url);

        com.android.volley.toolbox.StringRequest req = new com.android.volley.toolbox.StringRequest(
                Request.Method.DELETE, url,
                response -> {
                    Toast.makeText(this, "Post deleted!", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    Log.e(TAG, "Delete failed: " + error);
                    Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }
}