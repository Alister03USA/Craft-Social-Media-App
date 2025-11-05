package com.example.androidexample;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NotificationCenterActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private ArrayList<NotificationItem> notificationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_center);
        // Setup bottom navigation
        setupBottomNavigation(R.id.nav_group);

        recyclerView = findViewById(R.id.recyclerViewNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(this, notificationList);
        recyclerView.setAdapter(adapter);

        fetchNotifications();


    }


    private void fetchNotifications() {
        String url = "http://coms-3090-028.class.las.iastate.edu:8080/notifications/"
                + SessionManager.getInstance().getLoggedInUsername();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    notificationList.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            int id = obj.getInt("id");
                            String title = obj.getString("title");
                            String message = obj.getString("message");

                            // Get sender username if present
                            JSONObject senderObj = obj.optJSONObject("sender");
                            String senderUsername = senderObj != null ? senderObj.getString("username") : null;

                            // Automatically detect follow requests
                            String type = "general";
                            if ((title != null && title.toLowerCase().contains("follow"))
                                    || (message != null && message.toLowerCase().contains("follow"))) {
                                type = "follow_request";
                            }

                            notificationList.add(new NotificationItem(id, title, message, type, senderUsername));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    adapter.notifyDataSetChanged();
                },
                error -> Toast.makeText(NotificationCenterActivity.this, "Error loading notifications", Toast.LENGTH_SHORT).show()
        );

        VolleySingleton.getInstance(this).addToRequestQueue(jsonArrayRequest);
    }

}
