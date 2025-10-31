package com.example.androidexample;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CreateGroupActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView groupImage;
    private EditText groupNameInput, groupDescriptionInput, groupCraftInput;
    private Switch privateSwitch;
    private Button createButton, selectImageButton;
    private Bitmap selectedBitmap;
    private ProgressDialog progressDialog;

    private static final String BACKEND_URL = "http://coms-3090-028.class.las.iastate.edu:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        groupImage = findViewById(R.id.groupImage);
        groupNameInput = findViewById(R.id.groupNameInput);
        groupDescriptionInput = findViewById(R.id.groupDescriptionInput);
        groupCraftInput = findViewById(R.id.groupCraftInput);
        privateSwitch = findViewById(R.id.privateSwitch);
        createButton = findViewById(R.id.createGroupButton);
        selectImageButton = findViewById(R.id.selectImageButton);

        selectImageButton.setOnClickListener(v -> openImagePicker());
        createButton.setOnClickListener(v -> uploadImageThenCreateGroup());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                groupImage.setImageBitmap(selectedBitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImageThenCreateGroup() {
        if (selectedBitmap == null) {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = ProgressDialog.show(this, "Uploading", "Please wait...", true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] imageBytes = baos.toByteArray();

        // Upload the image to backend
        MultipartRequest uploadRequest = new MultipartRequest(
                Request.Method.POST,
                BACKEND_URL + "/images",
                "file",
                "groupImage.jpg",
                "image/jpeg",
                imageBytes,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        long imageId = json.getLong("id");
                        createGroup(imageId);
                    } catch (JSONException e) {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Image upload failed: Invalid JSON", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Image upload failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(uploadRequest);
    }

    private void createGroup(long imageId) {
        String username = SessionManager.getInstance().getLoggedInUsername();

        if (username == null || username.isEmpty()) {
            progressDialog.dismiss();
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject groupJson = new JSONObject();
        try {
            groupJson.put("groupName", groupNameInput.getText().toString().trim());
            groupJson.put("description", groupDescriptionInput.getText().toString().trim());
            groupJson.put("craft", groupCraftInput.getText().toString().trim());
            groupJson.put("private", privateSwitch.isChecked());
            groupJson.put("imageId", imageId);
        } catch (JSONException e) {
            e.printStackTrace();
            progressDialog.dismiss();
            Toast.makeText(this, "Failed to build JSON request", Toast.LENGTH_SHORT).show();
            return;
        }

        String createUrl = BACKEND_URL + "/" + username + "/create";

        JsonObjectRequest createRequest = new JsonObjectRequest(
                Request.Method.POST,
                createUrl,
                groupJson,
                response -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Group created successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Group creation failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(createRequest);
    }
}
