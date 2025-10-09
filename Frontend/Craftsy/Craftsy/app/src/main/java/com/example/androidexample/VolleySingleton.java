package com.example.androidexample;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleySingleton {

    private static VolleySingleton instance;
    private RequestQueue requestQueue;
    private static Context ctx;

    // Private constructor ensures only one instance
    private VolleySingleton(Context context) {
        ctx = context.getApplicationContext();
        requestQueue = getRequestQueue();
    }

    // Public method to get the singleton instance
    public static synchronized VolleySingleton getInstance(Context context) {
        if (instance == null) {
            instance = new VolleySingleton(context);
        }
        return instance;
    }

    // Get the request queue (creates it if null)
    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // Use application context to avoid leaking activities
            requestQueue = Volley.newRequestQueue(ctx);
        }
        return requestQueue;
    }

    // Convenience method to add requests to the queue
    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
