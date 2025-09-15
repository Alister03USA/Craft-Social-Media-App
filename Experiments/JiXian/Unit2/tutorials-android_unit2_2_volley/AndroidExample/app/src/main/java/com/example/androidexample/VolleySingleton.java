package com.example.androidexample;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Singleton class to manage a single instance of Volley RequestQueue and ImageLoader.
 * This ensures efficient network request handling throughout the application.
 */
public class VolleySingleton {

    // Static instance of the Singleton class
    private static VolleySingleton instance;

    // RequestQueue for handling network requests
    private RequestQueue requestQueue;

    // ImageLoader for handling image caching and loading
    private ImageLoader imageLoader;

    // Context reference to avoid memory leaks
    private static Context ctx;

    /**
     * Private constructor to prevent direct instantiation from other classes.
     * Initializes the request queue and image loader.
     */
    private VolleySingleton(Context context) {
        ctx = context;  // Store the application context
        requestQueue = getRequestQueue(); // Initialize request queue

        // Initialize ImageLoader with an LRU cache to store images efficiently
        imageLoader = new ImageLoader(requestQueue,
                new ImageLoader.ImageCache() {

                    // LRU (Least Recently Used) cache to store up to 20 images
                    private final LruCache<String, Bitmap> cache = new LruCache<>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url); // Retrieve image from cache
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap); // Store image in cache
                    }
                });
    }

    /**
     * Provides the global instance of VolleySingleton.
     * Ensures only one instance is created (Singleton pattern).
     *
     * @param context Application context
     * @return Singleton instance of VolleySingleton
     */
    public static synchronized VolleySingleton getInstance(Context context) {
        if (instance == null) {
            instance = new VolleySingleton(context);
        }
        return instance;
    }

    /**
     * Returns the RequestQueue instance, creating one if it doesn’t exist.
     * This ensures that a single request queue is used across the application.
     *
     * @return RequestQueue instance
     */
    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // Use application context to avoid leaking activity context
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    /**
     * Adds a request to the request queue for execution.
     *
     * @param req The request to be added to the queue
     * @param <T> The type of the request response
     */
    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req); // Add request to queue
    }

    /**
     * Returns the ImageLoader instance to load and cache images.
     *
     * @return ImageLoader instance
     */
    public ImageLoader getImageLoader() {
        return imageLoader;
    }
}
