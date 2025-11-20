package com.example.androidexample;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Activity that allows a user to upload craft tutorials.
 * Supports uploading videos either from device storage or via an external URL,
 * along with fields such as title, description, category, and privacy settings.
 * Interacts with backend Spring Boot API using Volley multipart and POST requests.
 *
 * @author Ji Xian Fu
 */
public class TutorialUploadActivity extends AppCompatActivity {

    private static final String TAG = "UPLOAD_DEBUG";

    /** Base URL for tutorial related backend API endpoints. */
    private static final String BASE_URL =
            "http://coms-3090-028.class.las.iastate.edu:8080/tutorial";

    /** Request code for selecting a video file from storage. */
    private static final int PICK_VIDEO_REQUEST = 101;

    private EditText titleInput, descInput, categoryInput, urlInput;
    private ImageView btnSelectFile, btnUpload;
    private ProgressBar progressBar;
    private Switch switchPrivate;

    private Uri selectedFileUri;
    private String username;

    /**
     * Called when the activity is created.
     * Initializes UI components, sets click listeners, retrieves logged in username,
     * and prepares upload logic for file or URL based submissions.
     *
     * @param savedInstanceState previous state of Activity if recreated
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial_upload);

        username = SessionManager.getInstance().getLoggedInUsername();

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        titleInput = findViewById(R.id.inputTitle);
        descInput = findViewById(R.id.inputDescription);
        categoryInput = findViewById(R.id.inputCategory);
        urlInput = findViewById(R.id.inputUrl);
        btnSelectFile = findViewById(R.id.btnSelectFile);
        btnUpload = findViewById(R.id.btnUpload);
        progressBar = findViewById(R.id.progressBar);
        switchPrivate = findViewById(R.id.switchPrivate);

        btnSelectFile.setOnClickListener(v -> openFileChooser());

        btnUpload.setOnClickListener(v -> {
            if (!isNetworkConnected()) {
                Toast.makeText(this, "No Internet connection", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedFileUri != null) {
                uploadFileToBackend();
            } else if (!urlInput.getText().toString().trim().isEmpty()) {
                uploadUrlToBackend(urlInput.getText().toString().trim());
            } else {
                Toast.makeText(this, "Please select a file or paste a URL", Toast.LENGTH_SHORT).show();
            }
        });

        Log.d(TAG, "Logged in username = " + username);
    }

    /**
     * Opens the system file chooser so the user can select a video file.
     * Only video MIME types are allowed.
     */
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        startActivityForResult(Intent.createChooser(intent, "Select Video"), PICK_VIDEO_REQUEST);
    }

    /**
     * Handles activity results such as the user selecting a video file.
     *
     * @param requestCode the request identifier
     * @param resultCode the operation result
     * @param data file return data containing the selected Uri
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_VIDEO_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedFileUri = data.getData();

            if (selectedFileUri != null) {
                String name = getFileName(selectedFileUri);
                Toast.makeText(this, "Selected: " + name, Toast.LENGTH_SHORT).show();
                btnSelectFile.setImageResource(android.R.drawable.ic_menu_upload);

                Log.d(TAG, "Selected file URI = " + selectedFileUri);
                Log.d(TAG, "Selected file name = " + name);
            }
        }
    }

    /**
     * Extracts the file name for a given Uri using the content resolver.
     * Provides a fallback in case DISPLAY_NAME is not available.
     *
     * @param uri the Uri of the selected file
     * @return file name string
     */
    private String getFileName(Uri uri) {
        String result = null;

        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    result = cursor.getString(index);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "getFileName error: ", e);
        }

        if (result == null) {
            result = uri.getLastPathSegment();
            if (result == null || result.trim().isEmpty()) {
                result = "uploaded_video.mp4";
            }
        }

        return result;
    }

    /**
     * Determines the correct MIME type for a file based on its Uri.
     *
     * @param uri selected file Uri
     * @return resolved MIME type string
     */
    private String getMimeType(Uri uri) {
        String mime = getContentResolver().getType(uri);
        if (mime != null) return mime;

        String ext = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
    }

    /**
     * Uploads the selected video file to the backend using a multipart request.
     * Sends metadata such as title, description, and username as form parameters.
     */
    private void uploadFileToBackend() {
        progressBar.setVisibility(android.view.View.VISIBLE);

        try {
            byte[] fileData = getFileDataFromUri(selectedFileUri);
            String fileName = getFileName(selectedFileUri);
            String mimeType = getMimeType(selectedFileUri);

            Log.d(TAG, "Read file size: " + fileData.length);
            Log.d(TAG, "Uploading file: " + fileName);
            Log.d(TAG, "MIME type = " + mimeType);

            Map<String, String> formParams = getFormParams();
            Log.d(TAG, "Form params: " + formParams);

            VolleyMultipartRequest request = new VolleyMultipartRequest(
                    Request.Method.POST,
                    BASE_URL + "/uploadFile",
                    response -> {
                        progressBar.setVisibility(android.view.View.GONE);
                        String resp = new String(response.data, StandardCharsets.UTF_8);
                        Log.d(TAG, "Upload Success Response: " + resp);
                        Toast.makeText(this, "Upload success!", Toast.LENGTH_SHORT).show();
                    },
                    error -> {
                        progressBar.setVisibility(android.view.View.GONE);
                        NetworkResponse res = error.networkResponse;

                        if (res != null && res.data != null) {
                            String err = new String(res.data, StandardCharsets.UTF_8);
                            Log.e(TAG, "Server returned: " + err);
                            Toast.makeText(this, "Server error: " + err, Toast.LENGTH_LONG).show();
                        } else {
                            Log.e(TAG, "Upload failed: " + error);
                            Toast.makeText(this, "Upload failed", Toast.LENGTH_LONG).show();
                        }
                    },
                    formParams,
                    getByteData(fileName, fileData, mimeType)
            );

            VolleySingleton.getInstance(this).addToRequestQueue(request);

        } catch (Exception e) {
            progressBar.setVisibility(android.view.View.GONE);
            Log.e(TAG, "File read failed", e);
            Toast.makeText(this, "File read failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Converts the selected file from Uri into a byte array for uploading.
     *
     * @param uri source file location
     * @return byte array representing the file
     * @throws IOException if reading fails
     */
    private byte[] getFileDataFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        if (inputStream == null) throw new IOException("InputStream is NULL!");

        byte[] data = new byte[4096];
        int bytesRead;

        while ((bytesRead = inputStream.read(data)) != -1) {
            buffer.write(data, 0, bytesRead);
        }

        return buffer.toByteArray();
    }

    /**
     * Uploads a tutorial by providing an external URL instead of a local file.
     *
     * @param videoUrl direct link to online video content
     */
    private void uploadUrlToBackend(String videoUrl) {
        progressBar.setVisibility(android.view.View.VISIBLE);

        StringRequest request = new StringRequest(
                Request.Method.POST,
                BASE_URL + "/uploadUrl",
                response -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    Toast.makeText(this, "Uploaded via URL!", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    NetworkResponse res = error.networkResponse;

                    if (res != null && res.data != null) {
                        String err = new String(res.data, StandardCharsets.UTF_8);
                        Toast.makeText(this, "Server error: " + err, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Upload failed", Toast.LENGTH_LONG).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = getFormParams();
                params.put("fileUrl", videoUrl);
                return params;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    /**
     * Checks whether the device has an active network connection.
     *
     * @return true if connected, false if offline
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    /**
     * Collects form parameters that accompany the tutorial upload request.
     * These include username, title, description, category, and privacy flag.
     *
     * @return map of form parameters to send in request
     */
    private Map<String, String> getFormParams() {
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("title", titleInput.getText().toString().trim());
        params.put("description", descInput.getText().toString().trim());
        params.put("category", categoryInput.getText().toString().trim());
        params.put("isPrivate", switchPrivate.isChecked() ? "true" : "false");
        return params;
    }

    /**
     * Prepares multipart file data for video uploads.
     *
     * @param fileName the file name
     * @param fileData raw bytes
     * @param mimeType detected MIME type
     * @return map containing multipart data for request
     */
    private Map<String, VolleyMultipartRequest.DataPart> getByteData(
            String fileName, byte[] fileData, String mimeType) {

        Map<String, VolleyMultipartRequest.DataPart> params = new HashMap<>();
        params.put("file", new VolleyMultipartRequest.DataPart(fileName, fileData, mimeType));
        return params;
    }
}