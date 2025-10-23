package com.example.androidexample;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class VolleyMultipartRequest extends Request<byte[]> {

    private final Response.Listener<byte[]> listener;
    private final Response.ErrorListener errorListener;
    private final String boundary = "apiclient-" + System.currentTimeMillis();
    private final String twoHyphens = "--";
    private final String lineEnd = "\r\n";

    public VolleyMultipartRequest(int method, String url, Response.Listener<byte[]> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.listener = listener;
        this.errorListener = errorListener;
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data;boundary=" + boundary;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            for (Map.Entry<String, String> e : getParams().entrySet()) {
                bos.write((twoHyphens + boundary + lineEnd).getBytes());
                bos.write(("Content-Disposition: form-data; name=\"" + e.getKey() + "\"" + lineEnd).getBytes());
                bos.write(("Content-Type: text/plain; charset=UTF-8" + lineEnd).getBytes());
                bos.write((lineEnd + e.getValue() + lineEnd).getBytes());
            }

            for (Map.Entry<String, DataPart> e : getByteData().entrySet()) {
                DataPart dp = e.getValue();
                bos.write((twoHyphens + boundary + lineEnd).getBytes());
                bos.write(("Content-Disposition: form-data; name=\"" + e.getKey() + "\"; filename=\"" + dp.fileName + "\"" + lineEnd).getBytes());
                bos.write(("Content-Type: " + dp.type + lineEnd).getBytes());
                bos.write(lineEnd.getBytes());
                bos.write(dp.content);
                bos.write(lineEnd.getBytes());
            }

            bos.write((twoHyphens + boundary + twoHyphens + lineEnd).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bos.toByteArray();
    }

    @Override
    protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response.data, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(byte[] response) {
        listener.onResponse(response);
    }

    protected abstract Map<String, String> getParams() throws AuthFailureError;

    protected abstract Map<String, DataPart> getByteData();

    public static class DataPart {
        public final String fileName;
        public final byte[] content;
        public final String type;

        public DataPart(String fileName, byte[] content) {
            this(fileName, content, "application/octet-stream");
        }

        public DataPart(String fileName, byte[] content, String type) {
            this.fileName = fileName;
            this.content = content;
            this.type = type;
        }
    }
}