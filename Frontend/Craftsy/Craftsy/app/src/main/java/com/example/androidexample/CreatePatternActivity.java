package com.example.androidexample;

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
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;

public class CreatePatternActivity extends AppCompatActivity {

    private Button selectImageButton, uploadPatternButton, backToFeedButton;
    private ImageView imagePreview;
    private EditText patternTitleInput, patternDescInput, patternTypeInput;
    private Spinner difficultySpinner;
    private Uri selectedUri;


    private static final String IMAGE_UPLOAD_URL = "http://coms-3090-028.class.las.iastate.edu:8080/images";
    private static final String PATTERN_BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080/patterns";

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
        patternTypeInput = findViewById(R.id.patternTypeInput);
        difficultySpinner = findViewById(R.id.difficultySpinner);

        // Difficulty options
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Easy", "Moderate", "Difficult"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(adapter);

        // Back button
        backToFeedButton.setOnClickListener(v -> finish());

        // Gallery picker
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


        // Upload handler
        uploadPatternButton.setOnClickListener(v -> uploadPattern());
    }

    private void uploadPattern() {
        final String title = patternTitleInput.getText().toString().trim();
        final String description = patternDescInput.getText().toString().trim();

        SessionManager session = SessionManager.getInstance();
        final String username = (session.getLoggedInUsername() != null) ? session.getLoggedInUsername() : "testUser";

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }

        //  Force user to select an image
        if (selectedUri == null) {
            Toast.makeText(this, "You must select an image to post a pattern", Toast.LENGTH_SHORT).show();
            return; // stop here
        }

        // Existing image upload logic
        final byte[] fileData = convertImageUriToBytes(selectedUri);
        if (fileData == null) {
            Toast.makeText(this, "Failed to read image", Toast.LENGTH_SHORT).show();
            return;
        }

        final String fileName = getFileNameFromUri(selectedUri);
        final String mimeType = (getContentResolver().getType(selectedUri) != null) ?
                getContentResolver().getType(selectedUri) : "image/jpeg";

        MultipartRequest request = new MultipartRequest(
                Request.Method.POST,
                IMAGE_UPLOAD_URL,
                "image",
                fileName,
                mimeType,
                fileData,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        long imageId = json.getLong("id");
                        createPatternPost(username, title, description, imageId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing image upload response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Image upload failed", Toast.LENGTH_LONG).show()
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }


    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) result = cursor.getString(index);
                }
            }
        }
        if (result == null) result = uri.getLastPathSegment();
        return result;
    }

    private byte[] convertImageUriToBytes(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream()) {

            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            return byteBuffer.toByteArray();
        } catch (IOException e) {
            Log.e("CreatePatternActivity", "Error converting image to bytes", e);
            return null;
        }
    }

    private void createPatternPost(String username, String title, String description, Long imageId) {
        String patternType = patternTypeInput.getText().toString().trim();
        if(patternType.isEmpty()) patternType = "Knitting"; // default

        String difficulty = difficultySpinner.getSelectedItem().toString();

        JSONObject jsonBody = new JSONObject();
        try {
            JSONObject userObj = new JSONObject();
            userObj.put("username", username);
            jsonBody.put("user", userObj);

            jsonBody.put("patternName", title);
            jsonBody.put("patternType", patternType);
            jsonBody.put("rating", 0);
            jsonBody.put("patternLink", "");
            jsonBody.put("difficulty", difficulty);
            jsonBody.put("description", description);
            jsonBody.put("supplies", "Needles, Yarn");
            jsonBody.put("numRatings", 0);

            if (imageId != null) {
                JSONObject imageObj = new JSONObject();
                imageObj.put("id", imageId);
                JSONArray imagesArray = new JSONArray();
                imagesArray.put(imageObj);
                jsonBody.put("images", imagesArray);
            }

        } catch (JSONException e) {
            Log.e("CreatePatternActivity", "Error building JSON body", e);
        }

        String url = PATTERN_BASE_URL + "/" + username;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {
                    Toast.makeText(this, "Pattern posted successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    Log.e("CreatePatternActivity", "Pattern upload failed", error);
                    Toast.makeText(this, "Pattern upload failed", Toast.LENGTH_LONG).show();
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

}
