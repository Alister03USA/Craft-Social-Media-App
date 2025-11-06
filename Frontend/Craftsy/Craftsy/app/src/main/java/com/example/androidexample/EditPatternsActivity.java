package com.example.androidexample;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

public class EditPatternsActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";

    private EditText editDescription;
    private Button savePatternButton;
    private String patternName, username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_patterns);

        editDescription = findViewById(R.id.editDescription);
        savePatternButton = findViewById(R.id.savePatternButton);

        patternName = getIntent().getStringExtra("patternName");
        username = SessionManager.getInstance().getLoggedInUsername();

        // Pre-fill description if passed from details
        String currentDescription = getIntent().getStringExtra("description");
        if (currentDescription != null) {
            editDescription.setText(currentDescription);
        }

        savePatternButton.setOnClickListener(v -> updateDescription());
    }

    private void updateDescription() {
        String url = BASE_URL + "/patterns/" + username + "/" + patternName;

        StringRequest request = new StringRequest(
                Request.Method.PUT, url,
                response -> {
                    Toast.makeText(this, "Description updated!", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public byte[] getBody() {
                return editDescription.getText().toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "text/plain; charset=utf-8";
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}
