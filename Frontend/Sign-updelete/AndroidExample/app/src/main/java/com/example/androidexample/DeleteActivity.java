package com.example.androidexample;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class DeleteActivity extends AppCompatActivity {

    private EditText editTextUsernameDelete;
    private Button buttonDelete;

    private RequestQueue requestQueue;
    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080/users/delete/{username}";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete);

        editTextUsernameDelete = findViewById(R.id.editTextUsernameDelete);
        buttonDelete = findViewById(R.id.buttonDelete);

        requestQueue = Volley.newRequestQueue(this);

        buttonDelete.setOnClickListener(v -> deleteUser());
    }

    private void deleteUser() {
        String username = editTextUsernameDelete.getText().toString().trim();

        if (username.isEmpty()) {
            Toast.makeText(this, "Enter a username to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + username;

        StringRequest request = new StringRequest(
                Request.Method.DELETE,
                url,
                response -> Toast.makeText(DeleteActivity.this, "User deleted successfully!", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(DeleteActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show()
        );

        requestQueue.add(request);
    }
}