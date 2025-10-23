package com.example.androidexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TutorialAdapter extends RecyclerView.Adapter<TutorialAdapter.VH> {

    public interface OnItemClick {
        void onClick(TutorialItem item);
    }

    private final List<TutorialItem> data;
    private final OnItemClick callback;

    public TutorialAdapter(List<TutorialItem> data, OnItemClick callback) {
        this.data = data;
        this.callback = callback;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tutorial_item, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        TutorialItem item = data.get(position);
        holder.title.setText(item.getTitle());
        holder.description.setText(item.getDescription());
        holder.category.setText(item.getCategory());

        holder.itemView.setOnClickListener(v -> callback.onClick(item));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, description, category;
        ImageView thumbnail;

        VH(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tutorialTitle);
            description = itemView.findViewById(R.id.tutorialDescription);
            category = itemView.findViewById(R.id.tutorialCategory);
            thumbnail = itemView.findViewById(R.id.tutorialThumbnail);
        }
    }
}