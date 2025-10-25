package com.example.androidexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private final List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView leftBubble, rightBubble;
        public ViewHolder(View view) {
            super(view);
            leftBubble = view.findViewById(R.id.leftMessage);
            rightBubble = view.findViewById(R.id.rightMessage);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message_bubble, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);
        if (msg.isMine()) {
            holder.leftBubble.setVisibility(View.GONE);
            holder.rightBubble.setVisibility(View.VISIBLE);
            holder.rightBubble.setText(msg.getMessage());
        } else {
            holder.rightBubble.setVisibility(View.GONE);
            holder.leftBubble.setVisibility(View.VISIBLE);
            holder.leftBubble.setText(msg.getSender() + ": " + msg.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}