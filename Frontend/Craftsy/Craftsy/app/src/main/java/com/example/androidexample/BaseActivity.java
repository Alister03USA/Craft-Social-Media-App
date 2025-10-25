package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

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

            if (id == R.id.nav_notif) {
                // Open Notification Center
                Intent intent = new Intent(this, NotificationCenterActivity.class);
                startActivity(intent);
            }
            else if (id == R.id.nav_view_user) {
                // Open another user’s profile
                Intent intent = new Intent(this, OutsideUserProfile.class);
                String loggedInUsername = SessionManager.getInstance().getLoggedInUsername();
                intent.putExtra("logged_in_username", loggedInUsername);
                intent.putExtra("username", "Fuji"); // example
                startActivity(intent);
            }
            else if (id == R.id.nav_my_profile) {
                // Open logged-in user’s profile
                Intent intent = new Intent(this, UserProfile.class);
                startActivity(intent);
            }
            else if (id == R.id.myFeed) {
                // Open Feed Activity
                Intent intent = new Intent(this, FeedActivity.class);
                String loggedInUsername = SessionManager.getInstance().getLoggedInUsername();
                intent.putExtra("username", loggedInUsername);
                startActivity(intent);
            }
            else if (id == R.id.nav_search) {
                // Open Search Activity
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
            }

            overridePendingTransition(0, 0);
            return true;
        });
    }
}