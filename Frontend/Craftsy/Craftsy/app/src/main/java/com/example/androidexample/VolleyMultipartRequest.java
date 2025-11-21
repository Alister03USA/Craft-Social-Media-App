package com.example.androidexample;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

public class VolleyMultipartRequest extends Request<NetworkResponse> {

    private static final String LINE_FEED = "\r\n";
    private static final String TWO_HYPHENS = "--";

    // RFC correct boundary
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
            // TEXT PARTS
            if (textParams != null) {
                for (Map.Entry<String, String> entry : textParams.entrySet()) {

                    bos.write((TWO_HYPHENS + boundary + LINE_FEED).getBytes(StandardCharsets.UTF_8));
                    bos.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINE_FEED).getBytes(StandardCharsets.UTF_8));
                    bos.write(("Content-Type: text/plain; charset=UTF-8" + LINE_FEED).getBytes(StandardCharsets.UTF_8));
                    bos.write(LINE_FEED.getBytes(StandardCharsets.UTF_8));

                    bos.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                    bos.write(LINE_FEED.getBytes(StandardCharsets.UTF_8));
                }
            }

            // FILE PARTS
            if (fileParams != null) {
                for (Map.Entry<String, DataPart> entry : fileParams.entrySet()) {
                    DataPart dp = entry.getValue();

                    // Start file part
                    bos.write((TWO_HYPHENS + boundary + LINE_FEED).getBytes(StandardCharsets.UTF_8));
                    bos.write(("Content-Disposition: form-data; name=\"" + entry.getKey()
                            + "\"; filename=\"" + dp.getFileName() + "\"" + LINE_FEED).getBytes(StandardCharsets.UTF_8));
                    bos.write(("Content-Type: " + dp.getType() + LINE_FEED).getBytes(StandardCharsets.UTF_8));
                    bos.write(("Content-Transfer-Encoding: binary" + LINE_FEED).getBytes(StandardCharsets.UTF_8));
                    bos.write(LINE_FEED.getBytes(StandardCharsets.UTF_8));

                    // File content
                    bos.write(dp.getContent());
                    bos.write(LINE_FEED.getBytes(StandardCharsets.UTF_8));
                }
            }

            // STRICT RFC FINAL BOUNDARY (NO CRLF AFTER)
            bos.write((TWO_HYPHENS + boundary + TWO_HYPHENS).getBytes(StandardCharsets.UTF_8));

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