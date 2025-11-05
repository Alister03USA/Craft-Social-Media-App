package com.example.androidexample;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.VolleyError;

import java.util.HashMap;
import java.util.Map;

/**
 * ✅ Edit existing tutorial with multipart/form-data PUT.
 * Works with backend /tutorial/{id} endpoint (Spring Boot).
 */
public class EditTutorialActivity extends AppCompatActivity {

    private static final String TAG = "EditTutorialActivity";
    private static final String BASE_URL =
            "http://coms-3090-028.class.las.iastate.edu:8080/tutorial";

    private EditText editTitle, editCategory, editDescription;
    private Button btnSave, btnDelete;
    private ImageButton btnCancel;
    private ProgressDialog progressDialog;

    private long tutorialId = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_tutorial);

        editTitle = findViewById(R.id.editTitle);
        editCategory = findViewById(R.id.editCategory);
        editDescription = findViewById(R.id.editDescription);
        btnSave = findViewById(R.id.btnSaveChanges);
        btnDelete = findViewById(R.id.btnDeleteTutorial);
        btnCancel = findViewById(R.id.btnCancelEdit);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        // ✅ get correct tutorialId
        tutorialId = getIntent().getLongExtra("tutorialId",
                getIntent().getLongExtra("id", -1L));

        String title = getIntent().getStringExtra("title");
        String category = getIntent().getStringExtra("category");
        String description = getIntent().getStringExtra("description");

        editTitle.setText(title);
        editCategory.setText(category);
        editDescription.setText(description);

        Log.d(TAG, "Editing tutorialId=" + tutorialId);

        btnSave.setOnClickListener(v -> {
            if (tutorialId <= 0) {
                Toast.makeText(this, "Invalid tutorial id", Toast.LENGTH_LONG).show();
                return;
            }
            updateTutorial();
        });

        btnDelete.setOnClickListener(v -> showDeleteDialog());
        btnCancel.setOnClickListener(v -> finish());
    }

    /**
     * ✅ Update tutorial via multipart/form-data
     */
    private void updateTutorial() {
        progressDialog.setMessage("Saving changes...");
        progressDialog.show();

        Map<String, String> textParams = new HashMap<>();
        textParams.put("title", editTitle.getText().toString().trim());
        textParams.put("description", editDescription.getText().toString().trim());
        textParams.put("category", editCategory.getText().toString().trim());

        // No file update here, so just send text fields
        Map<String, VolleyMultipartRequest.DataPart> fileParams = new HashMap<>();

        VolleyMultipartRequest request = new VolleyMultipartRequest(
                Request.Method.PUT,
                BASE_URL + "/" + tutorialId,
                response -> handleUpdateSuccess(response),
                this::handleUpdateError,
                textParams,
                fileParams
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void handleUpdateSuccess(NetworkResponse response) {
        progressDialog.dismiss();
        Log.d(TAG, "✅ Update success: " + new String(response.data));
        Toast.makeText(this, "Tutorial updated successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void handleUpdateError(VolleyError error) {
        progressDialog.dismiss();
        String msg = (error.networkResponse != null)
                ? new String(error.networkResponse.data)
                : error.toString();
        Log.e(TAG, "❌ Update error: " + msg);
        Toast.makeText(this, "Update failed: " + msg, Toast.LENGTH_LONG).show();
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Tutorial")
                .setMessage("Are you sure you want to delete this tutorial?")
                .setPositiveButton("Delete", (dialog, which) -> deleteTutorial())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteTutorial() {
        progressDialog.setMessage("Deleting tutorial...");
        progressDialog.show();

        com.android.volley.toolbox.StringRequest deleteRequest =
                new com.android.volley.toolbox.StringRequest(
                        Request.Method.DELETE,
                        BASE_URL + "/" + tutorialId,
                        response -> {
                            progressDialog.dismiss();
                            Toast.makeText(this, "Tutorial deleted successfully!", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "✅ Deleted tutorial: " + tutorialId);
                            finish();
                        },
                        error -> {
                            progressDialog.dismiss();
                            Toast.makeText(this, "Delete failed: " + error.toString(), Toast.LENGTH_LONG).show();
                            Log.e(TAG, "❌ Delete error", error);
                        });

        VolleySingleton.getInstance(this).addToRequestQueue(deleteRequest);
    }
}