package com.example.androidexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final List<ChatMessage> messageList;

    public ChatAdapter(List<ChatMessage> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ✅ Correct layout reference (was item_chat_message)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message_bubble, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);

        if (message.isSent()) {
            // My message → show on right
            holder.rightMessage.setVisibility(View.VISIBLE);
            holder.leftMessage.setVisibility(View.GONE);
            holder.rightMessage.setText(message.getMessage());
        } else {
            // Received message → show on left
            holder.leftMessage.setVisibility(View.VISIBLE);
            holder.rightMessage.setVisibility(View.GONE);
            holder.leftMessage.setText(message.getSender() + ": " + message.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView leftMessage, rightMessage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            leftMessage = itemView.findViewById(R.id.leftMessage);
            rightMessage = itemView.findViewById(R.id.rightMessage);
        }
    }
}