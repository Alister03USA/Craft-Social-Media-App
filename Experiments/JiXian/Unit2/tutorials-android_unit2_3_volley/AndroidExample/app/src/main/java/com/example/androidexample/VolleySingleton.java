package com.example.androidexample;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Singleton class for managing network requests using Volley.
 * Ensures only one instance of RequestQueue and ImageLoader exists throughout the app.
 */
public class VolleySingleton {

    private static VolleySingleton instance; // Holds the single instance of VolleySingleton
    private RequestQueue requestQueue; // Manages network requests
    private ImageLoader imageLoader; // Handles image caching and loading
    private static Context ctx; // Stores application context to prevent memory leaks

    /**
     * Private constructor to prevent direct instantiation.
     * Initializes RequestQueue and ImageLoader.
     *
     * @param context Application context
     */
    private VolleySingleton(Context context) {
        ctx = context;
        requestQueue = getRequestQueue(); // Initialize the request queue

        // Initialize ImageLoader with an LruCache for caching images
        imageLoader = new ImageLoader(requestQueue,
                new ImageLoader.ImageCache() {
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
     * Returns the singleton instance of VolleySingleton.
     * Ensures only one instance exists (Thread-safe).
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
     * Returns the RequestQueue instance. Initializes if null.
     * Uses application context to avoid memory leaks.
     *
     * @return RequestQueue instance
     */
    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    /**
     * Adds a request to the Volley RequestQueue.
     *
     * @param req Request to be added
     * @param <T> Generic type of request
     */
    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    /**
     * Returns the ImageLoader instance for caching and loading images.
     *
     * @return ImageLoader instance
     */
    public ImageLoader getImageLoader() {
        return imageLoader;
    }
}

