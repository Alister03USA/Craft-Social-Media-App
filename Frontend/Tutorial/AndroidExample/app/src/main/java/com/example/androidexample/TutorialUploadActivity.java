package com.example.androidexample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import java.util.HashMap;
import java.util.Map;

public class TutorialUploadActivity extends AppCompatActivity {

    private static final int PICK_VIDEO_REQUEST = 1;
    private ImageButton uploadIcon;
    private EditText titleInput, descInput, categoryInput, urlInput;
    private Switch uploadSwitch;
    private Button postBtn;
    private Uri selectedUri;
    private boolean isUrl = false;

    private static final String FILE_URL = "http://coms-3090-028.class.las.iastate.edu:8080/tutorial/uploadFile";
    private static final String URL_URL = "http://coms-3090-028.class.las.iastate.edu:8080/tutorial/uploadUrl";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial_upload);

        uploadIcon = findViewById(R.id.uploadIcon);
        titleInput = findViewById(R.id.tutorialTitleInput);
        descInput = findViewById(R.id.tutorialDescriptionInput);
        categoryInput = findViewById(R.id.tutorialCategoryInput);
        urlInput = findViewById(R.id.urlInput);
        uploadSwitch = findViewById(R.id.uploadModeSwitch);
        postBtn = findViewById(R.id.postTutorialBtn);

        uploadSwitch.setOnCheckedChangeListener((v, checked) -> {
            isUrl = checked;
            urlInput.setVisibility(checked ? EditText.VISIBLE : EditText.GONE);
            uploadIcon.setVisibility(checked ? ImageButton.GONE : ImageButton.VISIBLE);
        });

        uploadIcon.setOnClickListener(v -> pickFile());
        postBtn.setOnClickListener(v -> {
            if (isUrl) uploadByUrl();
            else uploadByFile();
        });
    }

    private void pickFile() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("video/*");
        startActivityForResult(i, PICK_VIDEO_REQUEST);
    }

    @Override
    protected void onActivityResult(int req, int res, @Nullable Intent data) {
        super.onActivityResult(req, res, data);
        if (req == PICK_VIDEO_REQUEST && res == RESULT_OK && data != null)
            selectedUri = data.getData();
    }

    private void uploadByFile() {
        if (selectedUri == null) {
            Toast.makeText(this, "Select a video", Toast.LENGTH_SHORT).show();
            return;
        }
        VolleyMultipartRequest req = new VolleyMultipartRequest(Request.Method.POST, FILE_URL,
                r -> Toast.makeText(this, "Uploaded!", Toast.LENGTH_SHORT).show(),
                e -> Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("username", "Fuji");
                p.put("title", titleInput.getText().toString());
                p.put("description", descInput.getText().toString());
                p.put("category", categoryInput.getText().toString());
                return p;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> p = new HashMap<>();
                p.put("file", new DataPart("video.mp4", FileUtils.getFileDataFromUri(getApplicationContext(), selectedUri)));
                return p;
            }
        };
        Volley.newRequestQueue(this).add(req);
    }

    private void uploadByUrl() {
        StringRequest req = new StringRequest(Request.Method.POST, URL_URL,
                r -> Toast.makeText(this, "Uploaded via URL!", Toast.LENGTH_SHORT).show(),
                e -> Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("username", "Fuji");
                p.put("title", titleInput.getText().toString());
                p.put("description", descInput.getText().toString());
                p.put("category", categoryInput.getText().toString());
                p.put("fileUrl", urlInput.getText().toString());
                return p;
            }
        };
        Volley.newRequestQueue(this).add(req);
    }
}