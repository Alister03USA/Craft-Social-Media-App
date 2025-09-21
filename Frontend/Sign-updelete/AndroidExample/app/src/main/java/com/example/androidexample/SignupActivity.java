package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {
    private EditText editTextUsername, editTextPassword;
    private Button buttonSignup, buttonGoToDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSignup = findViewById(R.id.buttonSignup);
        buttonGoToDelete = findViewById(R.id.buttonGoToDelete);

        buttonSignup.setOnClickListener(v -> {
            String username = editTextUsername.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            if (!username.isEmpty() && !password.isEmpty()) {
                Toast.makeText(this, "User signed up: " + username, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            }
        });

        buttonGoToDelete.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, DeleteActivity.class);
            startActivity(intent);
        });
    }
}