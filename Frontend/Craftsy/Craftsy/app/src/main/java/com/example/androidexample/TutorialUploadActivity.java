package com.example.androidexample;

import android.app.Activity;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
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

public class TutorialUploadActivity extends AppCompatActivity {

    private static final String TAG = "TutorialUploadActivity";
    private static final String BASE_URL =
            "http://coms-3090-028.class.las.iastate.edu:8080/tutorial";
    private static final int PICK_VIDEO_REQUEST = 101;

    private EditText titleInput, descInput, categoryInput, urlInput;
    private ImageView btnSelectFile, btnUpload;
    private ProgressBar progressBar;
    private Switch switchPrivate;
    private Uri selectedFileUri;
    private String username;

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
            if (selectedFileUri != null) uploadFileToBackend();
            else if (!urlInput.getText().toString().trim().isEmpty())
                uploadUrlToBackend(urlInput.getText().toString().trim());
            else
                Toast.makeText(this, "Please select a file or paste a URL", Toast.LENGTH_SHORT).show();
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        startActivityForResult(Intent.createChooser(intent, "Select Video"), PICK_VIDEO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_VIDEO_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedFileUri = data.getData();

            if (selectedFileUri != null) {
                String name = getFileName(selectedFileUri);
                Toast.makeText(this, "Selected: " + name, Toast.LENGTH_SHORT).show();
                btnSelectFile.setImageResource(android.R.drawable.ic_menu_upload);
            }
        }
    }

    private String getFileName(Uri uri) {
        String result = null;

        try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                result = cursor.getString(nameIndex);
            }
        }
        return result != null ? result : "video.mp4";
    }

    private void uploadFileToBackend() {
        progressBar.setVisibility(android.view.View.VISIBLE);

        try {
            byte[] fileData = getFileDataFromUri(selectedFileUri);
            String fileName = getFileName(selectedFileUri);

            VolleyMultipartRequest request = new VolleyMultipartRequest(
                    Request.Method.POST,
                    BASE_URL + "/uploadFile",
                    response -> {
                        progressBar.setVisibility(android.view.View.GONE);
                        Toast.makeText(this, "Upload success!", Toast.LENGTH_SHORT).show();
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
                    },
                    getFormParams(),
                    getByteData(fileName, fileData)
            );

            VolleySingleton.getInstance(this).addToRequestQueue(request);

        } catch (IOException e) {
            e.printStackTrace();
            progressBar.setVisibility(android.view.View.GONE);
            Toast.makeText(this, "File read failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private byte[] getFileDataFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int bytesRead;

        while ((bytesRead = inputStream.read(data)) != -1)
            buffer.write(data, 0, bytesRead);

        return buffer.toByteArray();
    }

    private void uploadUrlToBackend(String videoUrl) {
        progressBar.setVisibility(android.view.View.VISIBLE);

        StringRequest request = new StringRequest(
                Request.Method.POST,
                BASE_URL + "/uploadUrl",
                response -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    Toast.makeText(this, "Tutorial uploaded via URL!", Toast.LENGTH_SHORT).show();
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
                })
        {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = getFormParams();
                params.put("fileUrl", videoUrl);
                return params;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    private Map<String, String> getFormParams() {
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("title", titleInput.getText().toString().trim());
        params.put("description", descInput.getText().toString().trim());
        params.put("category", categoryInput.getText().toString().trim());
        params.put("isPrivate", switchPrivate.isChecked() ? "true" : "false");
        return params;
    }

    private Map<String, VolleyMultipartRequest.DataPart> getByteData(String fileName, byte[] fileData) {
        Map<String, VolleyMultipartRequest.DataPart> params = new HashMap<>();
        params.put("file", new VolleyMultipartRequest.DataPart(fileName, fileData, "video/mp4"));
        return params;
    }
}