package com.example.androidexample;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
public class SecurityQuestionActivity extends AppCompatActivity {

    private TextView messageText;   // define message textview variable
    String[] item = {"What street did you grow up on?", "What is your mother's maiden name?", "What is your favorite movie?"};
    private EditText SecurityEditText;
    private EditText confirmSecurityEditText;
    private Button signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_securityquestions);             // link to Main activity XML
        SecurityEditText = findViewById(R.id.securityquestionanswer);
        confirmSecurityEditText = findViewById(R.id.confirmanswer);
        signupButton = findViewById(R.id.signup_security_btn);




        Spinner dropdown = findViewById(R.id.spinner1);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, item);

        dropdown.setAdapter(adapter);
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* grab strings from user inputs */

                String answer = SecurityEditText.getText().toString();
                String confirm = confirmSecurityEditText.getText().toString();

                if (answer.equals(confirm)){
                    Intent intent = new Intent(SecurityQuestionActivity.this, MainActivity.class);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(getApplicationContext(), "Answers don't match", Toast.LENGTH_LONG).show();
                }
            }
        });







    }}
