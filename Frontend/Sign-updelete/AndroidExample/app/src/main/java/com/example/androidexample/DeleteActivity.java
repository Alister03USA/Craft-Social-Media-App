package com.example.androidexample;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class DeleteActivity extends AppCompatActivity {
    private EditText editTextUsernameDelete;
    private Button buttonDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete);

        editTextUsernameDelete = findViewById(R.id.editTextUsernameDelete);
        buttonDelete = findViewById(R.id.buttonDelete);

        buttonDelete.setOnClickListener(v -> {
            String usernameToDelete = editTextUsernameDelete.getText().toString().trim();
            if (!usernameToDelete.isEmpty()) {
                Toast.makeText(this, "Deleted user: " + usernameToDelete, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Enter username to delete", Toast.LENGTH_SHORT).show();
            }
        });
    }
}