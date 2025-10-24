package com.example.androidexample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

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
        holder.category.setText(item.getCategory());
        holder.username.setText("@" + item.getUsername());
        holder.description.setText(item.getDescription());

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return tutorialList.size();
    }

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