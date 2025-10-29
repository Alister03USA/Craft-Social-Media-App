package com.example.androidexample;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * ✅ Custom Volley request that supports multipart/form-data
 * so that we can send both file and text fields together.
 * Works with Spring Boot @RequestParam for file upload.
 */
public class VolleyMultipartRequest extends Request<NetworkResponse> {

    private final String boundary = "----AndroidFormBoundary" + UUID.randomUUID();
    private static final String LINE_FEED = "\r\n";

    private final Response.Listener<NetworkResponse> mListener;
    private final Response.ErrorListener mErrorListener;

    // Text and file parts
    private final Map<String, String> textParams;
    private final Map<String, DataPart> fileParams;

    /**
     * Constructor
     */
    public VolleyMultipartRequest(
            int method,
            String url,
            Response.Listener<NetworkResponse> listener,
            Response.ErrorListener errorListener,
            Map<String, String> textParams,
            Map<String, DataPart> fileParams
    ) {
        super(method, url, errorListener);
        this.mListener = listener;
        this.mErrorListener = errorListener;
        this.textParams = textParams;
        this.fileParams = fileParams;
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data; boundary=" + boundary;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            // ✳️ Append text fields
            if (textParams != null && !textParams.isEmpty()) {
                for (Map.Entry<String, String> entry : textParams.entrySet()) {
                    outputStream.write(("--" + boundary + LINE_FEED).getBytes());
                    outputStream.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINE_FEED).getBytes());
                    outputStream.write(("Content-Type: text/plain; charset=UTF-8" + LINE_FEED + LINE_FEED).getBytes());
                    outputStream.write(entry.getValue().getBytes());
                    outputStream.write(LINE_FEED.getBytes());
                }
            }

            // ✳️ Append file fields
            if (fileParams != null && !fileParams.isEmpty()) {
                for (Map.Entry<String, DataPart> entry : fileParams.entrySet()) {
                    DataPart dataPart = entry.getValue();
                    outputStream.write(("--" + boundary + LINE_FEED).getBytes());
                    outputStream.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"; filename=\"" + dataPart.getFileName() + "\"" + LINE_FEED).getBytes());
                    outputStream.write(("Content-Type: " + dataPart.getType() + LINE_FEED + LINE_FEED).getBytes());
                    outputStream.write(dataPart.getContent());
                    outputStream.write(LINE_FEED.getBytes());
                }
            }

            // ✳️ End boundary
            outputStream.write(("--" + boundary + "--" + LINE_FEED).getBytes());

        } catch (IOException e) {
            throw new AuthFailureError("Error while creating multipart request body: " + e.getMessage());
        }

        return outputStream.toByteArray();
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        try {
            return Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e) {
            return Response.error(new VolleyError("Failed to parse network response"));
        }
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        mListener.onResponse(response);
    }

    @Override
    public void deliverError(VolleyError error) {
        mErrorListener.onErrorResponse(error);
    }

    /**
     * ✅ Helper class representing a file (binary) part
     */
    public static class DataPart {
        private final String fileName;
        private final byte[] content;
        private final String type;

        public DataPart(String fileName, byte[] content, String type) {
            this.fileName = fileName;
            this.content = content;
            this.type = type;
        }

        public String getFileName() {
            return fileName;
        }

        public byte[] getContent() {
            return content;
        }

        public String getType() {
            return type;
        }
    }
}