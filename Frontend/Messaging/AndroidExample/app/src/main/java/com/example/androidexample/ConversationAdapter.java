package com.example.androidexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.Holder> {

    public interface OnConvoClick {
        void onOpen(ConversationItem item);
    }

    private final List<ConversationItem> data;
    private final OnConvoClick listener;

    public ConversationAdapter(List<ConversationItem> data, OnConvoClick listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int pos) {
        ConversationItem item = data.get(pos);
        h.name.setText(item.getName());
        h.last.setText(item.getLastMessage());
        h.time.setText(item.getTimestamp());
        h.icon.setImageResource(item.isGroup() ? R.drawable.ic_group : R.drawable.ic_user);
        h.itemView.setOnClickListener(v -> listener.onOpen(item));
    }

    @Override public int getItemCount() { return data.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name, last, time;
        Holder(@NonNull View v){
            super(v);
            icon = v.findViewById(R.id.ivIcon);
            name = v.findViewById(R.id.tvName);
            last = v.findViewById(R.id.tvLast);
            time = v.findViewById(R.id.tvTime);
        }
    }
}