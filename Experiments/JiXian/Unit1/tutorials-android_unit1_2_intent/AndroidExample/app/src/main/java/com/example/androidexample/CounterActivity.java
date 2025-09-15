package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CounterActivity extends AppCompatActivity {

    private TextView numberText;
    private Button increaseBtn, decreaseBtn, resultBtn;
    private int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter);

        numberText = findViewById(R.id.number);
        increaseBtn = findViewById(R.id.increase_btn);
        decreaseBtn = findViewById(R.id.decrease_btn);
        resultBtn = findViewById(R.id.result_btn);

        // Get start value from intent
        count = getIntent().getIntExtra("start_value", 0);
        numberText.setText(String.valueOf(count));

        increaseBtn.setOnClickListener(v -> {
            count++;
            numberText.setText(String.valueOf(count));
        });

        decreaseBtn.setOnClickListener(v -> {
            count--;
            numberText.setText(String.valueOf(count));
        });

        resultBtn.setOnClickListener(v -> {
            Intent intent = new Intent(CounterActivity.this, ResultActivity.class);
            intent.putExtra("final_value", count);
            startActivity(intent);
        });
    }
}