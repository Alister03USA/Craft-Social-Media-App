package com.example.androidexample;

public class PendingRequestModel {
    private final long requestId;
    private final String username;
    private final String requestedAt;

    public PendingRequestModel(long requestId, String username, String requestedAt) {
        this.requestId = requestId;
        this.username = username;
        this.requestedAt = requestedAt;
    }

    public long getRequestId() { return requestId; }
    public String getUsername() { return username; }
    public String getRequestedAt() { return requestedAt; }
}
