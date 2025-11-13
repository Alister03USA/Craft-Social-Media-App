package com.example.androidexample;

import org.json.JSONObject;

public class EventModel {
    private long id;
    private String name;
    private String craftType;
    private String description;
    private String eventDate;
    private String imageUrl;
    private String location;

    public static EventModel fromJson(JSONObject obj) {
        EventModel e = new EventModel();
        e.id = obj.optLong("eventID");
        e.name = obj.optString("eventName");
        e.craftType = obj.optString("craftType");
        e.description = obj.optString("description");
        e.eventDate = obj.optString("eventDate");
        e.imageUrl = obj.optString("image");
        e.location = obj.optString("location");
        return e;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getCraftType() { return craftType; }
    public String getDescription() { return description; }
    public String getEventDate() { return eventDate; }
    public String getImageUrl() { return imageUrl; }
    public String getLocation() { return location; }
}
