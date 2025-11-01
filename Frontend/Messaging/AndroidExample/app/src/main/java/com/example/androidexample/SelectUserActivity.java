package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;

public class SelectUserActivity extends AppCompatActivity {

    private Spinner spinner;
    private Button btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_user);

        spinner = findViewById(R.id.spinnerUsers);
        btnContinue = findViewById(R.id.btnContinue);

        // Test usernames
        String[] users = {"Fuji", "Quinn", "alister_gan", "kkeck"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, users);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        btnContinue.setOnClickListener(v -> {
            String selectedUser = spinner.getSelectedItem().toString();
            Intent i = new Intent(this, MessagingHomeActivity.class);
            i.putExtra("username", selectedUser);
            startActivity(i);
        });
    }
}