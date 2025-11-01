package com.example.androidexample;

import android.webkit.MimeTypeMap;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * MultipartRequest for uploading any file type to backend.
 */
public class MultipartRequest extends Request<String> {

    private final Response.Listener<String> listener;
    private final byte[] fileData;
    private final String fileName;
    private final String fieldName;
    private final String mimeType;
    private final String boundary = "apiclient-" + System.currentTimeMillis();

    public MultipartRequest(
            int method,
            String url,
            String fieldName,
            String fileName,
            String mimeType,
            byte[] fileData,
            Response.Listener<String> listener,
            Response.ErrorListener errorListener
    ) {
        super(method, url, errorListener);
        this.listener = listener;
        this.fileData = fileData;
        this.fileName = fileName;
        this.fieldName = fieldName;
        this.mimeType = mimeType;
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data; boundary=" + boundary;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            dos.writeBytes("--" + boundary + "\r\n");
            dos.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"\r\n");
            dos.writeBytes("Content-Type: " + mimeType + "\r\n\r\n");
            dos.write(fileData);
            dos.writeBytes("\r\n");
            dos.writeBytes("--" + boundary + "--\r\n");
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bos.toByteArray();
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (Exception e) {
            parsed = new String(response.data);
        }
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(String response) {
        listener.onResponse(response);
    }
}
