package com.example.androidexample;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CreateGroupActivity extends AppCompatActivity {

    private static final String TAG = "CreateGroupActivity";
    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";

    private ImageView groupImage;
    private EditText groupNameInput, groupDescriptionInput, groupCraftInput;
    private Switch privateSwitch;
    private Button createButton, selectImageButton;
    private Uri selectedImageUri;

    private ActivityResultLauncher<String> getContentLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());


        groupImage = findViewById(R.id.groupImage);
        groupNameInput = findViewById(R.id.groupNameInput);
        groupDescriptionInput = findViewById(R.id.groupDescriptionInput);
        groupCraftInput = findViewById(R.id.groupCraftInput);
        privateSwitch = findViewById(R.id.privateSwitch);
        createButton = findViewById(R.id.createGroupButton);
        selectImageButton = findViewById(R.id.selectImageButton);

        // ActivityResultLauncher for image picking (like your patterns flow)
        getContentLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        groupImage.setImageURI(uri);
                    }
                }
        );

        selectImageButton.setOnClickListener(v -> getContentLauncher.launch("image/*"));

        // When create pressed: if image selected -> upload first, otherwise create without image
        createButton.setOnClickListener(v -> {
            String groupName = groupNameInput.getText().toString().trim();

            if (groupName.isEmpty()) {
                Toast.makeText(this, "Please enter a group name", Toast.LENGTH_SHORT).show();
                return;
            }

            String username = SessionManager.getInstance().getLoggedInUsername();
            if (username == null || username.isEmpty()) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedImageUri != null) {
                uploadImageThenCreateGroup();
            } else {
                // use -1 to indicate no image
                createGroup(-1L);
            }
        });
    }

    /**
     * Upload the selected image to the backend using your MultipartRequest.
     * On success the backend should return{"id": <imageId>} which we pass to createGroup(imageId).
     * On failure we still call createGroup(-1) so the user can create a group without an image.
     */
    private void uploadImageThenCreateGroup() {
        if (selectedImageUri == null) {
            createGroup(-1L);
            return;
        }

        byte[] imageBytes = convertImageUriToBytes(selectedImageUri);
        if (imageBytes == null) {
            Toast.makeText(this, "Failed to read image", Toast.LENGTH_SHORT).show();
            createGroup(-1L);
            return;
        }

        String fileName = getFileName(selectedImageUri);
        String mimeType = getContentResolver().getType(selectedImageUri);
        if (mimeType == null) mimeType = "image/jpeg";

        String uploadUrl = BASE_URL + "/images"; // matches your earlier usage

        MultipartRequest uploadRequest = new MultipartRequest(
                Request.Method.POST,
                uploadUrl,
                "image",      // field name you've used previously (backend expects this)
                fileName,
                mimeType,
                imageBytes,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        long imageId = json.optLong("id", -1);
                        if (imageId == -1) {
                            Toast.makeText(this, "Upload returned invalid id, creating without image", Toast.LENGTH_SHORT).show();
                            createGroup(-1L);
                        } else {
                            createGroup(imageId);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Invalid upload response JSON", e);
                        Toast.makeText(this, "Image upload failed (invalid response)", Toast.LENGTH_SHORT).show();
                        createGroup(-1L);
                    }
                },
                error -> {
                    Log.e(TAG, "Image upload error", error);
                    Toast.makeText(this, "Image upload failed", Toast.LENGTH_LONG).show();
                    createGroup(-1L);
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(uploadRequest);
    }

    /**
     * Create group on backend. If imageId == -1 then no image was attached.
     */
    /**
     * Create group on backend. If imageId == -1 then no image was attached.
     */
    private void createGroup(long imageId) {
        String username = SessionManager.getInstance().getLoggedInUsername();
        if (username == null || username.isEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("groupName", groupNameInput.getText().toString().trim());
            jsonBody.put("description", groupDescriptionInput.getText().toString().trim());
            jsonBody.put("craft", groupCraftInput.getText().toString().trim());
            jsonBody.put("isPrivate", privateSwitch.isChecked()); // ✅ FIXED



        } catch (JSONException e) {
            Log.e(TAG, "Failed to build group JSON", e);
            Toast.makeText(this, "Failed to build request", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "/create"+ "/" + username;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {
                    Toast.makeText(this, "Group created!", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            String serverMessage = new String(error.networkResponse.data, "UTF-8");
                            Log.e(TAG, "Server response: " + serverMessage);
                        } catch (Exception decodeError) {
                            Log.e(TAG, "Failed to decode server error", decodeError);
                        }
                    }
                    Log.e(TAG, "Group creation error", error);
                    Toast.makeText(this, "Group creation failed", Toast.LENGTH_LONG).show();
                }

        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }


    /**
     * Convert the content URI into a byte[] using an InputStream - safe for large files.
     */
    private byte[] convertImageUriToBytes(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

            if (inputStream == null) return null;

            byte[] data = new byte[1024];
            int n;
            while ((n = inputStream.read(data)) >= 0) {
                buffer.write(data, 0, n);
            }
            return buffer.toByteArray();

        } catch (IOException e) {
            Log.e(TAG, "Image conversion error", e);
            return null;
        }
    }

    /**
     * Get filename from Uri, falling back to lastPathSegment.
     */
    private String getFileName(Uri uri) {
        if (uri == null) return "upload.jpg";

        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        return cursor.getString(index);
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed to read filename from cursor", e);
            }
        }

        String last = uri.getLastPathSegment();
        return (last != null) ? last : "upload.jpg";
    }
}
