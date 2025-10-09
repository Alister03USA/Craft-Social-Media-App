package com.example.androidexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {

    private List<FeedItem> feedList;

    public FeedAdapter(List<FeedItem> feedList) {
        this.feedList = feedList;
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.feed_item, parent, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        FeedItem item = feedList.get(position);
        holder.textUsername.setText(item.getUsername());
        holder.textProjectName.setText(item.getProjectName());
        holder.textProjectDesc.setText(item.getProjectDesc());
        holder.textProjectType.setText(item.getProjectType());
        holder.textSupplies.setText(item.getSupplies());
        holder.textVisibility.setText(item.getVisibility());
        holder.textDate.setText(item.getDate());
    }

    @Override
    public int getItemCount() {
        return feedList.size();
    }

    static class FeedViewHolder extends RecyclerView.ViewHolder {
        TextView textUsername, textProjectName, textProjectDesc, textProjectType, textSupplies, textVisibility, textDate;

        FeedViewHolder(View itemView) {
            super(itemView);
            textUsername = itemView.findViewById(R.id.textUsername);
            textProjectName = itemView.findViewById(R.id.textProjectName);
            textProjectDesc = itemView.findViewById(R.id.textProjectDesc);
            textProjectType = itemView.findViewById(R.id.textProjectType);
            textSupplies = itemView.findViewById(R.id.textSupplies);
            textVisibility = itemView.findViewById(R.id.textVisibility);
            textDate = itemView.findViewById(R.id.textDate);
        }
    }
}