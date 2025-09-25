package com.example.androidexample;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.text.InputType;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class UserProfile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Start in VIEW mode
        showProfileView();
    }

    private void showProfileView() {
        setContentView(R.layout.activity_user_profile);

        // Find "Edit User" button
        Button editButton = findViewById(R.id.login_login_btn);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfile();
            }
        });
    }

    private void showEditProfile() {
        setContentView(R.layout.activity_user_profile_edit);

        // 1) Make the window resize when the keyboard appears so ScrollView can scroll
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        // 2) Prevent an EditText from auto-stealing focus which can make it appear un-scrollable.
        //    Request focus on the ScrollView root instead.
        View scroll = findViewById(R.id.profile_scrollview);
        if (scroll != null) {
            scroll.setFocusableInTouchMode(true);
            scroll.requestFocus();
        }

        // Date of Birth field (opens DatePickerDialog). (safe-null checks)
        final EditText dobField = findViewById(R.id.edit_dob);
        if (dobField != null) {
            // ensure no keyboard opens for DOB and clicking shows the picker
            dobField.setInputType(InputType.TYPE_NULL);
            dobField.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDatePicker(dobField);
                }
            });
        }

        Button saveButton = findViewById(R.id.btn_save_profile);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfile();
            }
        });
    }

    private void saveProfile() {
        // Grab values from edit layout
        EditText displayName = findViewById(R.id.edit_display_name);
        EditText username = findViewById(R.id.edit_username);
        EditText bio = findViewById(R.id.edit_bio);
        Spinner gender = findViewById(R.id.spinner_gender);

        // New fields
        EditText email = findViewById(R.id.edit_email);
        EditText phone = findViewById(R.id.edit_phone);
        EditText dob = findViewById(R.id.edit_dob);
        EditText location = findViewById(R.id.edit_location);

        String name = displayName.getText().toString().trim();
        String user = username.getText().toString().trim();
        String biography = bio.getText().toString().trim();
        String gen = gender.getSelectedItem().toString();

        String mail = (email != null) ? email.getText().toString().trim() : "";
        String phoneNumber = (phone != null) ? phone.getText().toString().trim() : "";
        String birthdate = (dob != null) ? dob.getText().toString().trim() : "";
        String city = (location != null) ? location.getText().toString().trim() : "";

        // Build JSON body
        JSONObject profileData = new JSONObject();
        try {
            profileData.put("displayName", name);
            profileData.put("username", user);
            profileData.put("bio", biography);
            profileData.put("gender", gen);
            profileData.put("email", mail);
            profileData.put("phone", phoneNumber);
            profileData.put("dob", birthdate);
            profileData.put("location", city);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Replace with your backend endpoint
        String url = "http://10.0.2.2:8080/api/profile/update";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                profileData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(UserProfile.this,
                                "Profile saved successfully!",
                                Toast.LENGTH_SHORT).show();
                        // Go back to profile view after success
                        showProfileView();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(UserProfile.this,
                                "Error: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                }
        );

        // Add request to VolleySingleton queue
        //VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void showDatePicker(final EditText dobField) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int y, int m, int d) {
                        String selectedDate = (m + 1) + "/" + d + "/" + y;
                        dobField.setText(selectedDate);
                    }
                },
                year, month, day
        );
        dialog.show();
    }
}
