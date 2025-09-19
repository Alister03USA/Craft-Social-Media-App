package com.example.androidexample;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class EventDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        TextView nameTxt = findViewById(R.id.event_name_txt);
        TextView dateTxt = findViewById(R.id.event_date_txt);
        TextView descTxt = findViewById(R.id.event_desc_txt);
        Button backBtn = findViewById(R.id.back_home_btn);

        // Get values passed from EventCreationActivity
        String name = getIntent().getStringExtra("event_name");
        String date = getIntent().getStringExtra("event_date");
        String desc = getIntent().getStringExtra("event_desc");

        nameTxt.setText(name);
        dateTxt.setText(date);
        descTxt.setText(desc);

        // Back simply finishes this Activity → returns to EventCreationActivity
        backBtn.setOnClickListener(v -> finish());
    }
}