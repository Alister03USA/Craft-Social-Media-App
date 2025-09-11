package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText startNumberInput;
    private Button startCounterBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startNumberInput = findViewById(R.id.start_number_input);
        startCounterBtn = findViewById(R.id.start_counter_btn);

        startCounterBtn.setOnClickListener(v -> {
            String input = startNumberInput.getText().toString().trim();

            if (input.isEmpty()) {
                Toast.makeText(this, "Please enter a number", Toast.LENGTH_SHORT).show();
            } else {
                int startValue = Integer.parseInt(input);
                Intent intent = new Intent(MainActivity.this, CounterActivity.class);
                intent.putExtra("start_value", startValue);
                startActivity(intent);
            }
        });
    }
}