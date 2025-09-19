package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // Default API URL for demonstration
    private String url = "https://jsonplaceholder.typicode.com/users";
    private Spinner spMethod;
    private EditText etUrl, etRequest;
    private TextView tvResponse;
    private Button btnSend;
    private String method;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        etUrl = findViewById(R.id.etUrl);
        etRequest = findViewById(R.id.etRequest);
        tvResponse = findViewById(R.id.tvResponse);
        btnSend = findViewById(R.id.sendBtn);
        spMethod = findViewById(R.id.spMethod);

        etUrl.setText(url); // Set default URL in the input field

        // Initialize Spinner for selecting request method
        String[] methods = new String[]{"GET", "POST"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, methods);
        spMethod.setAdapter(adapter);
        spMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                method = (String) parent.getItemAtPosition(position);
                if (method.equals("GET")) {
                    etRequest.setText(""); // Clear input field for GET
                    etRequest.setEnabled(false); // Disable input for GET requests
                } else {
                    etRequest.setHint("Enter JSON object here:");
                    etRequest.setEnabled(true); // Enable input for POST requests
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                method = "GET"; // Default to GET if nothing is selected
            }
        });

        // Set onClickListener for the send button
        btnSend.setOnClickListener(v -> {
            url = etUrl.getText().toString().trim(); // Get user-entered URL
            if (method.equals("GET")) getRequest();
            else postRequest();
        });
    }

    // Method to handle GET request
    private void getRequest() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> tvResponse.setText("Response: " + response), // Display response
                error -> tvResponse.setText("Error: " + error.toString())) { // Handle error
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return new HashMap<>(); // Return empty headers
            }
        };
        Volley.newRequestQueue(this).add(stringRequest); // Add request to queue
    }

    // Method to handle POST request
    private void postRequest() {
        Log.d("PostRequest", "Preparing request...");
        JSONObject postBody;
        try {
            postBody = new JSONObject(etRequest.getText().toString()); // Convert input to JSON object
            Log.d("PostRequest", "Request body: " + postBody.toString());
        } catch (JSONException e) {
            Log.e("PostRequest", "Invalid JSON format", e);
            Toast.makeText(this, "Invalid JSON format", Toast.LENGTH_SHORT).show();
            return; // Exit if JSON is invalid
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, postBody,
                response -> {
                    Log.d("PostRequest", "Response received: " + response.toString());
                    tvResponse.setText("Response: " + response.toString()); // Display response
                },
                error -> {
                    Log.e("PostRequest", "Error response received", error);
                    tvResponse.setText("Error: " + (error.getMessage() != null ? error.getMessage() : "Unknown error")); // Handle error
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json"); // Set content type
                Log.d("PostRequest", "Headers set: " + headers.toString());
                return headers;
            }
        };
        Log.d("PostRequest", "Sending request...");
        Volley.newRequestQueue(this).add(request); // Add request to queue
    }
}
