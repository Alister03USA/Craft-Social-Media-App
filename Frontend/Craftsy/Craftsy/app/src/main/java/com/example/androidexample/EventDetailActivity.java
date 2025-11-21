package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import org.json.JSONObject;
import com.android.volley.Response;
import com.android.volley.VolleyError;


/**
 * Displays detailed information about a specific event, including its name, host,
 * date, craft type, description, and image. Allows users to RSVP "yes" or "no"
 * and navigate to the event's comments section.
 * @author Quinn Weidenaar
 */
public class EventDetailActivity extends BaseActivity {

    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";

    private TextView name, host, date, craftType, desc;
    private ImageView eventImage;
    private Button rsvpYesBtn, rsvpNoBtn, viewCommentsBtn;
    private long eventID;

    /**
     * Initializes the activity, loads event details, and sets up button listeners.
     *
     * @param savedInstanceState previously saved state, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        eventID = getIntent().getLongExtra("eventID", -1);
        name = findViewById(R.id.detailTitle);
        date = findViewById(R.id.detailDate);
        craftType = findViewById(R.id.detailTime);
        desc = findViewById(R.id.detailDescription);
        host = findViewById(R.id.detailLocation);
        eventImage = findViewById(R.id.detailImage);
        rsvpYesBtn = findViewById(R.id.btnRsvpYes);
        rsvpNoBtn = findViewById(R.id.btnRsvpNo);
        viewCommentsBtn = findViewById(R.id.btnViewComments);

        fetchEventDetails();

        rsvpYesBtn.setOnClickListener(v -> rsvp("yes"));
        rsvpNoBtn.setOnClickListener(v -> rsvp("no"));
        viewCommentsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, CommentsActivity.class);
            intent.putExtra("eventID", eventID);
            startActivity(intent);
        });
    }

    /**
     * Sends a GET request to the backend to retrieve event details using the event ID.
     */
    private void fetchEventDetails() {
        String url = BASE_URL + "/event/" + eventID;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                this::populateEvent,
                error -> Log.e("EventDetail", "Error fetching event", error)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    /**
     * Populates the UI with event data retrieved from the backend.
     *
     * @param obj the JSON object containing event details
     */
    private void populateEvent(JSONObject obj) {
        try {
            name.setText(obj.optString("eventName"));
            date.setText("Date: " + obj.optString("eventDate"));
            host.setText("Host: " + obj.getJSONObject("eventHost").optString("username"));
            craftType.setText("Craft: " + obj.optString("craftType"));
            desc.setText(obj.optString("description"));

            Glide.with(this)
                    .load(obj.optString("image"))
                    .placeholder(R.drawable.feed_card_background)
                    .into(eventImage);
        } catch (Exception e) {
            Log.e("EventDetail", "Parse error", e);
        }
    }

    /**
     * Sends an RSVP update (yes or no) for the logged-in user to the backend.
     *
     * @param status the RSVP status ("yes" or "no")
     */
    private void rsvp(String status) {
        String username = SessionManager.getInstance().getLoggedInUsername();
        String url = BASE_URL + "/event/" + eventID + "/" + username + "/" + status;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                null,
                response -> Log.d("RSVP", "RSVP updated"),
                error -> Log.e("RSVP", "Error", error)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}
