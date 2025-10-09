package com.example.androidexample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private ArrayList<NotificationItem> notificationList;
    private Context context;

    public NotificationAdapter(Context context, ArrayList<NotificationItem> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationItem item = notificationList.get(position);

        holder.title.setText(item.getTitle());
        holder.message.setText(item.getMessage());

        // Show buttons only for follower notifications
        if (item.getTitle().equalsIgnoreCase("New Follower")) {
            holder.buttonLayout.setVisibility(View.VISIBLE);

            holder.acceptButton.setOnClickListener(v -> {
                sendFollowerResponse(item.getId(), true, holder, position, item.getTitle());
            });

            holder.declineButton.setOnClickListener(v -> {
                sendFollowerResponse(item.getId(), false, holder, position, null);
            });
        } else {
            holder.buttonLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title, message;
        public LinearLayout buttonLayout;
        public Button acceptButton, declineButton;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.notificationTitle);
            message = itemView.findViewById(R.id.notificationMessage);
            buttonLayout = itemView.findViewById(R.id.followerButtonsLayout);
            acceptButton = itemView.findViewById(R.id.btnAccept);
            declineButton = itemView.findViewById(R.id.btnDecline);
        }
    }

    private void sendFollowerResponse(int notificationId, boolean accepted, ViewHolder holder, int position, String followerName) {
        //  Replace with your actual endpoint (example shown)
        String url = "http://coms-3090-028.class.las.iastate.edu:8080/notifications/respond/alister_gan/Quinn/true";

        // Prepare the request body
        JSONObject body = new JSONObject();
        try {
            body.put("accepted", accepted);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //  Create a PUT request
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                body,
                response -> {
                    Toast.makeText(context, "Response sent", Toast.LENGTH_SHORT).show();

                    //  Update UI after success
                    if (accepted) {
                        holder.buttonLayout.setVisibility(View.GONE);
                        if (followerName != null) {
                            holder.message.setText(followerName + " followed you");
                        }
                    } else {
                        notificationList.remove(position);
                        notifyItemRemoved(position);
                    }
                },
                error -> {
                    String errorMsg = "Failed to send response";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        errorMsg += ": " + new String(error.networkResponse.data);
                    }
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
                }
        );

        //  Add request to queue
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

}
