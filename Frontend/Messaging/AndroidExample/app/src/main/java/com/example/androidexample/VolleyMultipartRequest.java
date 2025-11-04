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

public class VolleyMultipartRequest extends Request<NetworkResponse> {

    private final Response.Listener<NetworkResponse> listener;
    private final Response.ErrorListener errorListener;
    private static final String boundary = "----CraftsyBoundary" + System.currentTimeMillis();
    private static final String lineEnd = "\r\n";
    private static final String twoHyphens = "--";

    public VolleyMultipartRequest(int method, String url,
                                  Response.Listener<NetworkResponse> listener,
                                  Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.listener = listener;
        this.errorListener = errorListener;
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        listener.onResponse(response);
    }

    @Override
    public void deliverError(com.android.volley.VolleyError error) {
        errorListener.onErrorResponse(error);
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data;boundary=" + boundary;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            Map<String, DataPart> data = getByteData();
            if (data != null && !data.isEmpty()) {
                for (Map.Entry<String, DataPart> entry : data.entrySet()) {
                    writeDataPart(bos, entry.getValue(), entry.getKey());
                }
            }
            bos.write((twoHyphens + boundary + twoHyphens + lineEnd).getBytes("UTF-8"));
        } catch (IOException e) {
            throw new AuthFailureError("Error building multipart body: " + e.getMessage());
        }
        return bos.toByteArray();
    }

    protected Map<String, DataPart> getByteData() throws AuthFailureError {
        return new HashMap<>();
    }

    private void writeDataPart(ByteArrayOutputStream bos, DataPart dataFile, String inputName) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(twoHyphens).append(boundary).append(lineEnd);
        sb.append("Content-Disposition: form-data; name=\"").append(inputName)
                .append("\"; filename=\"").append(dataFile.getFileName()).append("\"").append(lineEnd);
        sb.append("Content-Type: ").append(dataFile.getType()).append(lineEnd);
        sb.append(lineEnd);
        bos.write(sb.toString().getBytes("UTF-8"));
        bos.write(dataFile.getContent());
        bos.write(lineEnd.getBytes("UTF-8"));
    }

    public static class DataPart {
        private final String fileName;
        private final byte[] content;
        private final String type;

        public DataPart(String name, byte[] data, String type) {
            this.fileName = name;
            this.content = data;
            this.type = type;
        }

        public String getFileName() { return fileName; }
        public byte[] getContent() { return content; }
        public String getType() { return type; }
    }
}