package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JsonArrReqActivity extends AppCompatActivity {

    // UI components
    private Button btnJsonArrReq;  // Button to trigger JSON array request
    private ListAdapter adapter;   // Adapter for the ListView
    private ListView listView;     // ListView to display JSON data

    // URL to fetch JSON array data
    private static final String URL_JSON_ARRAY = "https://jsonplaceholder.typicode.com/users";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_json_arr_req);

        // Initialize UI elements
        btnJsonArrReq = findViewById(R.id.btnJsonArr);
        listView = findViewById(R.id.listView);

        // Initialize the adapter with an empty list (data will be added dynamically)
        adapter = new ListAdapter(this, new ArrayList<>());
        listView.setAdapter(adapter);

        // Set up click listener for the button
        btnJsonArrReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeJsonArrayReq(); // Trigger JSON array request when button is clicked
            }
        });
    }

    /**
     * Method to make a JSON array request using Volley
     */
    private void makeJsonArrayReq() {
        JsonArrayRequest jsonArrReq = new JsonArrayRequest(
                Request.Method.GET,  // HTTP request method (GET)
                URL_JSON_ARRAY,      // URL of the JSON array API
                null,                // Pass null as the request body since it's a GET request

                // Response listener for handling successful response
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("Volley Response", response.toString());

                        // Loop through the JSON array and extract required fields
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject jsonObject = response.getJSONObject(i);
                                String name = jsonObject.getString("name");   // Extract name
                                String email = jsonObject.getString("email"); // Extract email

                                // Create a ListItemObject and add it to the adapter
                                ListItemObject item = new ListItemObject(name, email);
                                adapter.add(item);

                            } catch (JSONException e) {
                                e.printStackTrace(); // Handle JSON parsing errors
                            }
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
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
//                headers.put("Authorization", "Bearer YOUR_ACCESS_TOKEN"); // Example authentication header
//                headers.put("Content-Type", "application/json"); // Example content-type header
                return headers;
            }

            // Override getParams() if you need to send request parameters
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
//                params.put("param1", "value1"); // Example parameter
//                params.put("param2", "value2"); // Example parameter
                return params;
            }
        };

        // Adding request to the Volley request queue for execution
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonArrReq);
    }
}
