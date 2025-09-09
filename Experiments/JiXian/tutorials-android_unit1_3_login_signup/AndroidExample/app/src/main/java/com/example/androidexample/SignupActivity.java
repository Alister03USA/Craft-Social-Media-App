package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {

    private EditText usernameEdt, passwordEdt, confirmEdt;
    private Button signupBtn, loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        usernameEdt = findViewById(R.id.signup_username_edt);
        passwordEdt = findViewById(R.id.signup_password_edt);
        confirmEdt = findViewById(R.id.signup_confirm_edt);
        signupBtn = findViewById(R.id.signup_signup_btn);
        loginBtn = findViewById(R.id.signup_login_btn);

        // Login button goes to LoginActivity
        loginBtn.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Signup button validation
        signupBtn.setOnClickListener(v -> {
            String username = usernameEdt.getText().toString().trim();
            String password = passwordEdt.getText().toString();
            String confirm = confirmEdt.getText().toString();

            if (username.isEmpty()) {
                Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirm)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Success → pass username back to MainActivity
            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });
    }
}
