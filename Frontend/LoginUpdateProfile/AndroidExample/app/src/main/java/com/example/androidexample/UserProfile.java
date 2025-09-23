package com.example.androidexample;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.androidexample.databinding.ActivityUserProfileBinding;

public class UserProfile extends AppCompatActivity {

    private ActivityUserProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate binding
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        // Save button click listener
        binding.btnSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProfile();
            }
        });
    }

    // Example method to save profile info
    private void saveProfile() {
        // Read EditText and Spinner values
        String displayName = binding.editDisplayName.getText().toString().trim();
        String username = binding.editUsername.getText().toString().trim();
        String bio = binding.editBio.getText().toString().trim();
        String gender = binding.spinnerGender.getSelectedItem().toString();

        // Example: show a toast (replace with actual saving logic)
        String message = "Saved: \nName: " + displayName + "\nUsername: " + username + "\nBio: " + bio + "\nGender: " + gender;
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
