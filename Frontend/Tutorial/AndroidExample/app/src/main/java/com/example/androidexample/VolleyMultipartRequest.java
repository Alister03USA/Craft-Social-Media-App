package com.example.androidexample;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

public abstract class VolleyMultipartRequest extends Request<byte[]> {

    private final String boundary = "apiclient-" + System.currentTimeMillis();
    private final String mimeType = "multipart/form-data;boundary=" + boundary;
    private final Response.Listener<byte[]> mListener;
    private final Response.ErrorListener mErrorListener;

    public VolleyMultipartRequest(int method, String url,
                                  Response.Listener<byte[]> listener,
                                  Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
        mErrorListener = errorListener;
    }

    public static class DataPart {
        public final String fileName; public final byte[] content; public final String type;
        public DataPart(String name, byte[] content) { this(name, content, "application/octet-stream"); }
        public DataPart(String name, byte[] content, String type) { this.fileName = name; this.content = content; this.type = type; }
    }

    protected abstract Map<String, String> getParams() throws AuthFailureError;
    protected abstract Map<String, DataPart> getByteData() throws AuthFailureError;

    @Override public String getBodyContentType() { return mimeType; }

    @Override public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            // text params
            for (Map.Entry<String, String> e : getParams().entrySet()) {
                bos.write(("--" + boundary + "\r\n").getBytes());
                bos.write(("Content-Disposition: form-data; name=\"" + e.getKey() + "\"\r\n\r\n").getBytes());
                bos.write((e.getValue() + "\r\n").getBytes());
            }
            // file params
            for (Map.Entry<String, DataPart> e : getByteData().entrySet()) {
                DataPart dp = e.getValue();
                bos.write(("--" + boundary + "\r\n").getBytes());
                bos.write(("Content-Disposition: form-data; name=\"" + e.getKey() + "\"; filename=\"" + dp.fileName + "\"\r\n").getBytes());
                bos.write(("Content-Type: " + dp.type + "\r\n\r\n").getBytes());
                bos.write(dp.content);
                bos.write("\r\n".getBytes());
            }
            bos.write(("--" + boundary + "--").getBytes());
        } catch (IOException ioe) { ioe.printStackTrace(); }
        return bos.toByteArray();
    }

    @Override protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response.data, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override protected void deliverResponse(byte[] response) { mListener.onResponse(response); }
}