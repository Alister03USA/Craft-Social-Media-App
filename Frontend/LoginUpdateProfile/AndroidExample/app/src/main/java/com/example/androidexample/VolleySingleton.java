package com.example.androidexample;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Singleton class for managing network requests using Volley.
 * Ensures only one instance of RequestQueue exists throughout the app.
 */
public class VolleySingleton {

    private static VolleySingleton instance; // Holds the single instance of VolleySingleton
    private RequestQueue requestQueue; // Manages network requests
    private static Context ctx; // Stores application context to prevent memory leaks

    /**
     * Private constructor to prevent direct instantiation.
     * Initializes RequestQueue.
     *
     * @param context Application context
     */
    private VolleySingleton(Context context) {
        ctx = context.getApplicationContext(); // Use app context to avoid leaks
        requestQueue = getRequestQueue();
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
}
