package com.example.androidexample;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public abstract class BaseActivity extends AppCompatActivity {

    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;

    // BroadcastReceiver for in-app notifications
    private BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String title = intent.getStringExtra("title");
            String message = intent.getStringExtra("message");

            // Show in-app notification as a Snackbar
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Start WebSocket service if username is set
        String username = SessionManager.getInstance().getLoggedInUsername();
        if (username != null && !username.isEmpty()) {
            Intent serviceIntent = new Intent(this, WebSocketNotificationService.class);
            startService(serviceIntent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION);
            }
        }

        // Register BroadcastReceiver for in-app notifications
        registerReceiver(notificationReceiver, new IntentFilter("NEW_NOTIFICATION"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unregister BroadcastReceiver
        unregisterReceiver(notificationReceiver);
    }

    protected void setupBottomNavigation(int selectedItemId) {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Safety check: if the layout doesn’t include a bottom nav, don’t crash
        if (bottomNav == null) {
            return;
        }

        bottomNav.setSelectedItemId(selectedItemId);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedItemId) return true;

            if (id == R.id.myFeed) {
                Intent intent = new Intent(this, FeedActivity.class);
                String loggedInUsername = SessionManager.getInstance().getLoggedInUsername();
                intent.putExtra("username", loggedInUsername);
                startActivity(intent);
            }
            else if (id == R.id.nav_search) {
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
            }
            else if (id == R.id.nav_groups) {
                // TODO: Add groups navigation later
                // Intent intent = new Intent(this, GroupsActivity.class);
                // startActivity(intent);
            }
            else if (id == R.id.pattern) {
                Intent intent = new Intent(this, PatternActivity.class);
                startActivity(intent);
            }
            else if (id == R.id.nav_my_profile) {
                Intent intent = new Intent(this, UserProfile.class);
                startActivity(intent);
            }

            overridePendingTransition(0, 0);
            return true;
        });
    }
}