package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SelectUserActivity extends AppCompatActivity {

    private static final String TAG = "SelectUserActivity";
    private ListView listUsers;
    private static final String CURRENT_USER = "Fuji"; // dev mode
    private final List<String> mockUsers = Arrays.asList("Fuji", "Quinn", "alister_gan", "kkeck");
    private final List<String> displayUsers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_user);

        listUsers = findViewById(R.id.listUsers);

        // ✅ Filter list to include Fuji (current user), show all available mock users
        displayUsers.clear();
        displayUsers.addAll(mockUsers);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, displayUsers
        );
        listUsers.setAdapter(adapter);

        listUsers.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            String selectedUser = displayUsers.get(position);
            Log.d(TAG, "🟦 Selected user: " + selectedUser);

            // ✅ Launch MessagingHomeActivity for that user
            Intent intent = new Intent(this, MessagingHomeActivity.class);
            intent.putExtra("username", selectedUser);
            Log.d(TAG, "➡️ Launching MessagingHomeActivity for user: " + selectedUser);
            startActivity(intent);
            finish();
        });
    }
}