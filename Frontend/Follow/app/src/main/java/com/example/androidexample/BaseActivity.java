package com.example.androidexample;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.androidexample.R;


    public abstract class BaseActivity extends AppCompatActivity {

        protected void setupBottomNavigation(int selectedItemId) {
            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            bottomNav.setSelectedItemId(selectedItemId);

            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == selectedItemId) return true;

                if (id == R.id.nav_notifications) {
                    startActivity(new Intent(this, NotificationCenterActivity.class));
                }

                overridePendingTransition(0, 0);
                return true;
            });
            
        }
    }

