package com.example.androidexample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class TutorialUploadActivity extends AppCompatActivity {

    private EditText title, desc, cat, url;
    private Uri fileUri;

    private final ActivityResultLauncher<String> picker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    fileUri = uri;
                    Toast.makeText(this, "Selected: " + uri.getLastPathSegment(), Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial_upload);

        title = findViewById(R.id.tutorialTitle);
        desc  = findViewById(R.id.tutorialDesc);
        cat   = findViewById(R.id.tutorialCategory);
        url   = findViewById(R.id.tutorialUrl);
        Button choose = findViewById(R.id.selectFileBtn);
        Button uploadFile = findViewById(R.id.uploadFileBtn);
        Button uploadUrl  = findViewById(R.id.uploadUrlBtn);

        choose.setOnClickListener(v -> picker.launch("video/*"));
        uploadFile.setOnClickListener(v ->
                Toast.makeText(this, "Simulate file upload for " + title.getText(), Toast.LENGTH_SHORT).show());
        uploadUrl.setOnClickListener(v ->
                Toast.makeText(this, "Simulate URL upload: " + url.getText(), Toast.LENGTH_SHORT).show());
    }
}