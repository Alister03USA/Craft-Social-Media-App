package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class JsonObjReqActivity extends AppCompatActivity {

    // UI components
    private Button btnJsonObjReq;  // Button to trigger JSON object request
    private TextView tvName, tvEmail, tvPhone; // TextViews to display JSON data

    // URL to fetch JSON object data
    private static final String URL_JSON_OBJECT = "https://jsonplaceholder.typicode.com/users/1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_json_obj_req);

        // Initialize UI elements
        btnJsonObjReq = findViewById(R.id.btnJsonObj);
        tvName = findViewById(R.id.nameTv);
        tvEmail = findViewById(R.id.emailTv);
        tvPhone = findViewById(R.id.phoneTv);

        // Set up click listener for the button
        btnJsonObjReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeJsonObjReq(); // Trigger JSON object request when button is clicked
            }
        });
    }

    /**
     * Method to make a JSON object request using Volley
     */
    private void makeJsonObjReq() {
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                Request.Method.GET,  // HTTP request method (GET)
                URL_JSON_OBJECT,     // URL of the JSON object API
                null,                // Pass null as the request body since it's a GET request

                // Response listener for handling successful response
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Volley Response", response.toString());
                        try {
                            // Extract data from the JSON object
                            String name = response.getString("name");   // Extract name
                            String email = response.getString("email"); // Extract email
                            String phone = response.getString("phone"); // Extract phone

                            // Populate TextViews with the retrieved data
                            tvName.setText(name);
                            tvEmail.setText(email);
                            tvPhone.setText(phone);

                        } catch (JSONException e) {
                            e.printStackTrace(); // Handle JSON parsing errors
                        }
                    }
                },

                // Error listener for handling request failure
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley Error", error.toString()); // Log error details
                    }
                }
        ) {
            // Override getHeaders() if authentication headers are needed
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
//                headers.put("Authorization", "Bearer YOUR_ACCESS_TOKEN"); // Example authentication header
//                headers.put("Content-Type", "application/json"); // Example content-type header
                return headers;
            }

            // Override getParams() if you need to send request parameters (for POST/PUT)
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
//                params.put("param1", "value1"); // Example parameter
//                params.put("param2", "value2"); // Example parameter
                return params;
            }
        };

        // Adding request to the Volley request queue for execution
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjReq);
    }
}
