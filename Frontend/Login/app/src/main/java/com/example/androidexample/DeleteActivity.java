package com.example.androidexample;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

public class DeleteActivity extends AppCompatActivity {

    private EditText etDeleteUsername;
    private Button btnConfirmDelete;

    private static final String DELETE_URL = "http://coms-3090-028.class.las.iastate.edu:8080/users/delete/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete);

        etDeleteUsername = findViewById(R.id.etDeleteUsername);
        btnConfirmDelete = findViewById(R.id.btnConfirmDelete);

        btnConfirmDelete.setOnClickListener(v -> {
            String username = etDeleteUsername.getText().toString().trim();
            if (username.isEmpty()) {
                Toast.makeText(this, "Enter username to delete", Toast.LENGTH_SHORT).show();
            } else {
                deleteUser(username);
            }
        });
    }

    private void deleteUser(String username) {
        String url = DELETE_URL + username;

        StringRequest request = new StringRequest(
                Request.Method.DELETE, url,
                response -> Toast.makeText(this, "User deleted", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(this, "Delete failed: " + error.toString(), Toast.LENGTH_LONG).show()
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}