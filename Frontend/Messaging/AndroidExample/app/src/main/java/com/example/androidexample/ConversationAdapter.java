package com.example.androidexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.Holder> {

    public interface OnConversationClick {
        void onClick(ConversationItem item);
    }

    private final List<ConversationItem> data;
    private final OnConversationClick onClick;

    public ConversationAdapter(List<ConversationItem> data, OnConversationClick onClick) {
        this.data = data;
        this.onClick = onClick;
    }

    @NonNull
    @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new Holder(v);
    }

    @Override public void onBindViewHolder(@NonNull Holder h, int pos) {
        ConversationItem it = data.get(pos);
        h.name.setText(it.getName());
        h.preview.setText(it.getLastMessage().isEmpty() ? "Tap to open" : it.getLastMessage());
        h.time.setText(it.getTimestamp());
        h.itemView.setOnClickListener(v -> onClick.onClick(it));
    }

    @Override public int getItemCount() { return data.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        TextView name, preview, time;
        Holder(@NonNull View itemView) {
            super(itemView);
            name   = itemView.findViewById(R.id.chatName);
            preview= itemView.findViewById(R.id.lastMessage);
            time   = itemView.findViewById(R.id.timeText);
        }
    }
}