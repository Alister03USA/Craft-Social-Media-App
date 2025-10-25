package com.example.androidexample;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * Adapter for the main feed and search results feed.
 */
public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder> {

    private final Context context;
    private final List<FeedItem> feedList;
    private final String fromScreen;  // "feed" or "search"

    public FeedAdapter(Context context, List<FeedItem> feedList, String fromScreen) {
        this.context = context;
        this.feedList = feedList;
        this.fromScreen = fromScreen;
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

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, FeedDetailActivity.class);
            intent.putExtra("from", fromScreen);
            intent.putExtra("username", item.getUsername());
            intent.putExtra("projectName", item.getProjectName());
            intent.putExtra("projectDesc", item.getProjectDesc());
            intent.putExtra("projectType", item.getProjectType());
            intent.putExtra("supplies", item.getSupplies());
            intent.putExtra("visibility", item.getVisibility());
            intent.putExtra("date", item.getDate());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return feedList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView projectName, username, projectDesc, projectType, visibility;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            projectName = itemView.findViewById(R.id.textProjectName);
            username = itemView.findViewById(R.id.textUsername);
            projectDesc = itemView.findViewById(R.id.textProjectDesc);
            projectType = itemView.findViewById(R.id.textProjectType);
            visibility = itemView.findViewById(R.id.textMeta);
        }
    }
}