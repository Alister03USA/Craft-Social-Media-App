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
 * ✅ Unified VolleyMultipartRequest
 * Works for Craftsy uploads (images, videos, tutorials)
 * Compatible with Spring Boot @RequestParam("file") and text params.
 */
public class VolleyMultipartRequest extends Request<NetworkResponse> {

    private static final String LINE_FEED = "\r\n";
    private static final String TWO_HYPHENS = "--";
    private final String boundary = "----CraftsyBoundary" + UUID.randomUUID();

    private final Response.Listener<NetworkResponse> mListener;
    private final Response.ErrorListener mErrorListener;

    private final Map<String, String> textParams;
    private final Map<String, DataPart> fileParams;

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
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            // 🧾 Text fields
            if (textParams != null && !textParams.isEmpty()) {
                for (Map.Entry<String, String> entry : textParams.entrySet()) {
                    bos.write((TWO_HYPHENS + boundary + LINE_FEED).getBytes());
                    bos.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINE_FEED).getBytes());
                    bos.write(("Content-Type: text/plain; charset=UTF-8" + LINE_FEED).getBytes());
                    bos.write(LINE_FEED.getBytes());
                    bos.write(entry.getValue().getBytes());
                    bos.write(LINE_FEED.getBytes());
                }
            }

            // 📦 File fields
            if (fileParams != null && !fileParams.isEmpty()) {
                for (Map.Entry<String, DataPart> entry : fileParams.entrySet()) {
                    DataPart dp = entry.getValue();
                    bos.write((TWO_HYPHENS + boundary + LINE_FEED).getBytes());
                    bos.write(("Content-Disposition: form-data; name=\"" + entry.getKey()
                            + "\"; filename=\"" + dp.getFileName() + "\"" + LINE_FEED).getBytes());
                    bos.write(("Content-Type: " + dp.getType() + LINE_FEED).getBytes());
                    bos.write(LINE_FEED.getBytes());
                    bos.write(dp.getContent());
                    bos.write(LINE_FEED.getBytes());
                }
            }

            // 🧩 End boundary
            bos.write((TWO_HYPHENS + boundary + TWO_HYPHENS + LINE_FEED).getBytes());
        } catch (IOException e) {
            throw new AuthFailureError("Multipart body build error: " + e.getMessage());
        }
        return bos.toByteArray();
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        mListener.onResponse(response);
    }

    @Override
    public void deliverError(VolleyError error) {
        mErrorListener.onErrorResponse(error);
    }

    /** 🧠 Binary data holder for files */
    public static class DataPart {
        private final String fileName;
        private final byte[] content;
        private final String type;

        public DataPart(String fileName, byte[] content, String type) {
            this.fileName = fileName;
            this.content = content;
            this.type = type;
        }

        public String getFileName() { return fileName; }
        public byte[] getContent() { return content; }
        public String getType() { return type; }
    }
}