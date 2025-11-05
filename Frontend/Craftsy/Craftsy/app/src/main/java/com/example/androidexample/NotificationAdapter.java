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

import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private Context context;
    private ArrayList<NotificationItem> notificationList;

    public NotificationAdapter(Context context, ArrayList<NotificationItem> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.notification_item, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationItem item = notificationList.get(position);
        holder.title.setText(item.getTitle());
        holder.message.setText(item.getMessage());

        if ("follow_request".equals(item.getType())) {
            holder.buttonsLayout.setVisibility(View.VISIBLE);

            holder.btnAccept.setOnClickListener(v ->
                    respondToFollowRequest(item.getId(), true, holder.getAdapterPosition())
            );

            holder.btnDecline.setOnClickListener(v ->
                    respondToFollowRequest(item.getId(), false, holder.getAdapterPosition())
            );
        } else {
            holder.buttonsLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView title, message;
        LinearLayout buttonsLayout;
        Button btnAccept, btnDecline;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.notificationTitle);
            message = itemView.findViewById(R.id.notificationMessage);
            buttonsLayout = itemView.findViewById(R.id.followerButtonsLayout);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnDecline = itemView.findViewById(R.id.btnDecline);
        }
    }

    private void respondToFollowRequest(int notificationId, boolean accepted, int position) {
        String url =
                "http://coms-3090-028.class.las.iastate.edu:8080/notifications/respond/"
                        + notificationId + "/" + accepted;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                null,
                response -> {
                    Toast.makeText(context,
                            accepted ? "Follow request accepted" : "Follow request declined",
                            Toast.LENGTH_SHORT).show();

                    notificationList.remove(position);
                    notifyItemRemoved(position);
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(context, "Failed to update follow request", Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }
}
