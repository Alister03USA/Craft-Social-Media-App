package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EventCreationActivity extends AppCompatActivity {

    private EditText nameInput, dateInput, descInput;
    private Button createEventBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_creation);

        nameInput = findViewById(R.id.event_name_input);
        dateInput = findViewById(R.id.event_date_input);
        descInput = findViewById(R.id.event_desc_input);
        createEventBtn = findViewById(R.id.create_event_btn);

        createEventBtn.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String date = dateInput.getText().toString().trim();
            String desc = descInput.getText().toString().trim();

            if (name.isEmpty() || date.isEmpty() || desc.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(EventCreationActivity.this, EventDetailActivity.class);
                intent.putExtra("event_name", name);
                intent.putExtra("event_date", date);
                intent.putExtra("event_desc", desc);
                startActivity(intent);
            }
        });
    }
}