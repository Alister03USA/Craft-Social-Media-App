package com.example.androidexample;

public final class ApiConfig {
    private ApiConfig() {}

    // ==== REST base ====
    // A) Real backend
    public static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";

    // B) Postman mock (keep this on while backend stabilizes)
    //public static final String BASE_URL = "https://2817a971-dad4-49a7-a285-90253ce5302e.mock.pstmn.io";

    // ==== WebSocket base ====
    // A) Real backend socket
    public static final String WS_BASE = "ws://coms-3090-028.class.las.iastate.edu:8080";

    // B) If you ever mock websockets locally, change here. (Leave unused for Postman.)
    // public static final String WS_BASE = "ws://coms-3090-028.class.las.iastate.edu:8080";

    // Logged-in user (swap this with your auth later)
    public static final String CURRENT_USERNAME = "Fuji";
}