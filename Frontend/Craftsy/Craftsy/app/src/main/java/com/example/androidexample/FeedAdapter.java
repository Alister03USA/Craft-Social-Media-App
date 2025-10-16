package com.example.androidexample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {

    private final LayoutInflater inflater;
    private final List<FeedItem> items;

    public FeedAdapter(Context context, List<FeedItem> items) {
        this.inflater = LayoutInflater.from(context);
        this.items = items;
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.feed_item, parent, false);
        return new FeedViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder h, int position) {
        FeedItem it = items.get(position);
        h.textUsername.setText(it.getUsername());
        h.textProjectName.setText(it.getProjectName());
        h.textProjectDesc.setText(it.getProjectDesc());
        h.textProjectType.setText(it.getProjectType());
        h.textSupplies.setText(it.getSupplies());
        h.textMeta.setText(it.getVisibility() + " • " + it.getDate());
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class FeedViewHolder extends RecyclerView.ViewHolder {
        final TextView textUsername;
        final TextView textProjectName;
        final TextView textProjectDesc;
        final TextView textProjectType;
        final TextView textSupplies;
        final TextView textMeta;

        FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            textUsername    = itemView.findViewById(R.id.textUsername);
            textProjectName = itemView.findViewById(R.id.textProjectName);
            textProjectDesc = itemView.findViewById(R.id.textProjectDesc);
            textProjectType = itemView.findViewById(R.id.textProjectType);
            textSupplies    = itemView.findViewById(R.id.textSupplies);
            textMeta        = itemView.findViewById(R.id.textMeta);
        }
    }
}