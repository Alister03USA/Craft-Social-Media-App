package com.example.androidexample;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

public class VolleyMultipartRequest extends Request<NetworkResponse> {

    private final Response.Listener<NetworkResponse> listener;

    public VolleyMultipartRequest(int method, String url,
                                  Response.Listener<NetworkResponse> listener,
                                  Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.listener = listener;
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        listener.onResponse(response);
    }

    // ✅ Must declare "throws AuthFailureError" to match Volley signature
    @Override
    public byte[] getBody() throws AuthFailureError {
        return null; // Will be overridden by anonymous class at runtime
    }

    @Override
    public String getBodyContentType() {
        return "application/octet-stream";
    }
}