package com.example.androidexample;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adapter for RecyclerView that displays tutorial items in TutorialFeed.
 * Properly forwards tutorial details to TutorialDetailActivity.
 */
public class TutorialAdapter extends RecyclerView.Adapter<TutorialAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(TutorialItem item);
    }

    private final Context context;
    private final List<TutorialItem> tutorialList;
    private final OnItemClickListener listener;

    public TutorialAdapter(Context context, List<TutorialItem> tutorialList, OnItemClickListener listener) {
        this.context = context;
        this.tutorialList = tutorialList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.tutorial_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TutorialItem item = tutorialList.get(position);

        holder.title.setText(item.getTitle());
        holder.category.setText(item.getCategory() != null ? item.getCategory() : "");
        holder.username.setText(item.getUsername() != null ? "@" + item.getUsername() : "@Unknown");
        holder.description.setText(item.getDescription() != null ? item.getDescription() : "");

        // --- Click to open TutorialDetailActivity ---
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TutorialDetailActivity.class);
            intent.putExtra("id", item.getId());
            intent.putExtra("title", item.getTitle());
            intent.putExtra("description", item.getDescription());
            intent.putExtra("category", item.getCategory());
            intent.putExtra("fileUrl", item.getFileURL());  // ✅ now matches backend JSON "fileURL"
            intent.putExtra("username", item.getUsername());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return tutorialList.size();
    }

    // --- ViewHolder ---
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, category, username, description;
        ImageView thumbnail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tutorialTitle);
            category = itemView.findViewById(R.id.tutorialCategory);
            username = itemView.findViewById(R.id.tutorialUsername);
            description = itemView.findViewById(R.id.tutorialDescription);
            thumbnail = itemView.findViewById(R.id.tutorialThumbnail);
        }
    }
}