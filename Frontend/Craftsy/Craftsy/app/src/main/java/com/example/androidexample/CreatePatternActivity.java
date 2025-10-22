package com.example.androidexample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;

public class CreatePatternActivity extends AppCompatActivity {

    private Button selectImageButton, uploadPatternButton, backToFeedButton;
    private ImageView imagePreview;
    private EditText patternTitleInput, patternDescInput;
    private Uri selectedUri;

    private static final String UPLOAD_URL = "http://10.0.2.2:8080/patterns";

    private ActivityResultLauncher<String> getContentLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_pattern);

        selectImageButton = findViewById(R.id.selectImageButton);
        uploadPatternButton = findViewById(R.id.uploadPatternButton);
        backToFeedButton = findViewById(R.id.backToFeedButton);
        imagePreview = findViewById(R.id.imagePreview);
        patternTitleInput = findViewById(R.id.patternTitleInput);
        patternDescInput = findViewById(R.id.patternDescInput);

        // back button
        backToFeedButton.setOnClickListener(v -> finish());

        // gallery picker
        getContentLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedUri = uri;
                        imagePreview.setImageURI(uri);
                    }
                }
        );
        selectImageButton.setOnClickListener(v -> getContentLauncher.launch("image/*"));

        // upload handler
        uploadPatternButton.setOnClickListener(v -> uploadPattern());
    }

    private void uploadPattern() {
        String title = patternTitleInput.getText().toString().trim();
        String description = patternDescInput.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedUri != null) {
            //  Upload the image first
            byte[] imageData = convertImageUriToBytes(selectedUri);
            String imageUploadUrl = "http://10.0.2.2:8080/images";

            MultipartRequest imageUploadRequest = new MultipartRequest(
                    Request.Method.POST,
                    imageUploadUrl,
                    imageData,
                    response -> {
                        try {
                            JSONObject json = new JSONObject(response);
                            String imageUrl = json.getString("imageUrl"); // Expect backend returns { "imageUrl": "..." }

                            //  Now post the pattern data
                            createPatternPost(title, description, imageUrl);

                        } catch (JSONException e) {
                            Toast.makeText(this, "Error parsing image upload response", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Toast.makeText(this, "Image upload failed: " + error.getMessage(), Toast.LENGTH_LONG).show()
            );

            VolleySingleton.getInstance(this).addToRequestQueue(imageUploadRequest);

        } else {
            //  No image selected → just create the pattern post directly
            createPatternPost(title, description, null);
        }
    }
    private void createPatternPost(String title, String description, String imageUrl) {
        String patternPostUrl = "http://10.0.2.2:8080/patterns";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("patternName", title);
            jsonBody.put("description", description);
            jsonBody.put("username", "testUser");
            if (imageUrl != null) {
                jsonBody.put("patternImage", imageUrl);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                patternPostUrl,
                jsonBody,
                response -> {
                    Toast.makeText(this, "Pattern posted successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> Toast.makeText(this, "Pattern upload failed: " + error.getMessage(), Toast.LENGTH_LONG).show()
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }


    private byte[] convertImageUriToBytes(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            return byteBuffer.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
