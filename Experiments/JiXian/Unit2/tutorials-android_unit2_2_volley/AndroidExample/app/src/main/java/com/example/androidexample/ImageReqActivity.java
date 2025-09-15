package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

public class ImageReqActivity extends AppCompatActivity {

    // UI components
    private Button btnImageReq;  // Button to trigger image request
    private ImageView imageView; // ImageView to display the downloaded image

    // URL of the image to be fetched
    public static final String URL_IMAGE = "http://sharding.org/outgoing/temp/testimg3.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_req);

        // Initialize UI elements
        btnImageReq = (Button) findViewById(R.id.btnImageReq);
        imageView = (ImageView) findViewById(R.id.imgView);

        // Set up click listener for the button
        btnImageReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeImageRequest(); // Trigger image request when button is clicked
            }
        });
    }

    /**
     * Method to make an image request using Volley
     */
    private void makeImageRequest() {

        ImageRequest imageRequest = new ImageRequest(
                URL_IMAGE, // URL to fetch the image from

                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        // On successful response, set the downloaded image to the ImageView
                        imageView.setImageBitmap(response);
                    }
                },

                0, // Width: 0 means use the original image width
                0, // Height: 0 means use the original image height
                ImageView.ScaleType.FIT_XY, // Scale type to fit image in ImageView
                Bitmap.Config.RGB_565, // Bitmap configuration (uses less memory than ARGB_8888)

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Log the error message if the request fails
                        Log.e("Volley Error", error.toString());
                    }
                }
        );

        // Add the request to the Volley request queue for execution
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(imageRequest);
    }

}
