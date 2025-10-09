package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    protected void setupBottomNavigation(int selectedItemId) {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // 🔹 Safety check: if the layout doesn’t include a bottom nav, don’t crash
        if (bottomNav == null) {
            return;
        }

        bottomNav.setSelectedItemId(selectedItemId);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedItemId) return true;

            if (id == R.id.nav_notif) {
                // 🔹 Open the NotificationCenterActivity
                Intent intent = new Intent(this, NotificationCenterActivity.class);
                startActivity(intent);
            }
            else if (id == R.id.nav_view_user) {
                // 🔹 Open OutsideUserProfile
                Intent intent = new Intent(this, OutsideUserProfile.class);

                // Pass info
                String loggedInUsername = SessionManager.getInstance().getLoggedInUsername();
                intent.putExtra("logged_in_username", loggedInUsername);
                intent.putExtra("username", "Fuji"); // Example

                startActivity(intent);
            }
            else if (id == R.id.nav_my_profile) {
                // 🔹 Open OutsideUserProfile
                Intent intent = new Intent(this, UserProfile.class);


                startActivity(intent);
            }

            overridePendingTransition(0, 0);
            return true;
        });
    }
}
