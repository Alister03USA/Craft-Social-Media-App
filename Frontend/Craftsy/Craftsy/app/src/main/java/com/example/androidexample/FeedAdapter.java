package com.example.androidexample;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder> {

    private final Context context;
    private final List<FeedItem> feedList;
    private final String fromScreen;
    private final String loggedInUser;

    public FeedAdapter(Context context, List<FeedItem> feedList, String fromScreen, String loggedInUser) {
        this.context = context;
        this.feedList = feedList;
        this.fromScreen = fromScreen;
        this.loggedInUser = loggedInUser;
    }

    @NonNull
    @Override
    public FeedAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.feed_item, parent, false);
        return new FeedAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedAdapter.ViewHolder holder, int position) {
        FeedItem item = feedList.get(position);

        holder.projectName.setText(item.getProjectName());
        holder.username.setText("@" + item.getUsername());
        holder.projectDesc.setText(item.getProjectDesc());
        holder.projectType.setText(item.getProjectType());
        holder.visibility.setText(item.getVisibility());

        // Load image manually
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            try {
                URL url = new URL(item.getImageUrl());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                holder.projectImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                Log.e("FeedAdapter", "Image load failed: " + e.getMessage());
                holder.projectImage.setImageResource(R.drawable.ic_post_placeholder);
            }
        } else {
            holder.projectImage.setImageResource(R.drawable.ic_post_placeholder);
        }

        // 🔹 Show Edit/Delete buttons only for own posts
        if (item.getUsername().equalsIgnoreCase(loggedInUser)) {
            holder.buttonEdit.setVisibility(View.VISIBLE);
            holder.buttonDelete.setVisibility(View.VISIBLE);
        } else {
            holder.buttonEdit.setVisibility(View.GONE);
            holder.buttonDelete.setVisibility(View.GONE);
        }

        // 🔹 Edit post
        holder.buttonEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, FeedCRUDActivity.class);
            intent.putExtra("mode", "edit");
            intent.putExtra("username", loggedInUser);
            intent.putExtra("projectName", item.getProjectName());
            intent.putExtra("projectDesc", item.getProjectDesc());
            intent.putExtra("projectType", item.getProjectType());
            intent.putExtra("supplies", item.getSupplies());
            intent.putExtra("visibility", item.getVisibility());
            context.startActivity(intent);
        });

        // 🔹 Delete post
        holder.buttonDelete.setOnClickListener(v -> {
            Intent intent = new Intent(context, FeedCRUDActivity.class);
            intent.putExtra("mode", "delete");
            intent.putExtra("username", loggedInUser);
            intent.putExtra("projectName", item.getProjectName());
            intent.putExtra("projectDesc", item.getProjectDesc());
            intent.putExtra("projectType", item.getProjectType());
            intent.putExtra("supplies", item.getSupplies());
            intent.putExtra("visibility", item.getVisibility());
            context.startActivity(intent);
        });

        // 🔹 View details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, FeedDetailActivity.class);
            intent.putExtra("username", item.getUsername());
            intent.putExtra("projectName", item.getProjectName());
            intent.putExtra("projectDesc", item.getProjectDesc());
            intent.putExtra("projectType", item.getProjectType());
            intent.putExtra("supplies", item.getSupplies());
            intent.putExtra("visibility", item.getVisibility());
            intent.putExtra("date", item.getDate());
            intent.putExtra("imageUrl", item.getImageUrl());
            intent.putExtra("loggedInUser", loggedInUser);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return feedList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView projectName, username, projectDesc, projectType, visibility;
        ImageView projectImage;
        Button buttonEdit, buttonDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            projectName = itemView.findViewById(R.id.textProjectName);
            username = itemView.findViewById(R.id.textUsername);
            projectDesc = itemView.findViewById(R.id.textProjectDesc);
            projectType = itemView.findViewById(R.id.textProjectType);
            visibility = itemView.findViewById(R.id.textMeta);
            projectImage = itemView.findViewById(R.id.imageProject);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}