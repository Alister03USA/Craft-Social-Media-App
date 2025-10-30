package com.example.androidexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {

    public interface OnItemClick { void onClick(ConversationItem convo); }

    private final List<ConversationItem> data;
    private final OnItemClick listener;

    public ConversationAdapter(List<ConversationItem> data, OnItemClick listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        ConversationItem c = data.get(pos);
        h.name.setText(c.getName());
        h.last.setText(c.getLastMessage());
        h.time.setText(c.getTimestamp());
        h.itemView.setOnClickListener(v -> listener.onClick(c));
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, last, time;
        ImageView profile;
        ViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.convoName);
            last = v.findViewById(R.id.convoLastMessage);
            time = v.findViewById(R.id.convoTimestamp);
            profile = v.findViewById(R.id.convoProfile);
        }
    }
}