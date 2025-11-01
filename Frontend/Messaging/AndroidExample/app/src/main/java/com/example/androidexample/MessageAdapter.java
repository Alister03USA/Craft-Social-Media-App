package com.example.androidexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int LEFT = 0;
    private static final int RIGHT = 1;

    private final List<MessageItem> data;
    private final String currentUser;

    public MessageAdapter(List<MessageItem> data, String currentUser) {
        this.data = data;
        this.currentUser = currentUser;
    }

    @Override
    public int getItemViewType(int position) {
        MessageItem m = data.get(position);
        return currentUser.equals(m.getSender()) ? RIGHT : LEFT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == RIGHT) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_right, parent, false);
            return new RightHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_left, parent, false);
            return new LeftHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position) {
        MessageItem m = data.get(position);
        if (vh instanceof RightHolder) {
            ((RightHolder) vh).msg.setText(m.getContent());
            ((RightHolder) vh).time.setText(m.getTimestamp());
        } else {
            ((LeftHolder) vh).name.setText(m.getSender());
            ((LeftHolder) vh).msg.setText(m.getContent());
            ((LeftHolder) vh).time.setText(m.getTimestamp());
        }
    }

    @Override public int getItemCount() { return data.size(); }

    static class LeftHolder extends RecyclerView.ViewHolder {
        TextView name, msg, time;
        LeftHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.leftName);
            msg  = itemView.findViewById(R.id.leftMsg);
            time = itemView.findViewById(R.id.leftTime);
        }
    }

    static class RightHolder extends RecyclerView.ViewHolder {
        TextView msg, time;
        RightHolder(@NonNull View itemView) {
            super(itemView);
            msg  = itemView.findViewById(R.id.rightMsg);
            time = itemView.findViewById(R.id.rightTime);
        }
    }
}