package com.example.androidexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.Holder> {
    private final List<MessageItem> data;
    private final String currentUser;

    public MessageAdapter(List<MessageItem> data, String currentUser) {
        this.data = data;
        this.currentUser = currentUser;
    }

    @Override
    public int getItemViewType(int position) {
        return data.get(position).isMine() ? 1 : 0;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = viewType == 1 ? R.layout.item_message_sent : R.layout.item_message_received;
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int i) {
        h.text.setText(data.get(i).getText());
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        TextView text;
        Holder(View v) {
            super(v);
            text = v.findViewById(R.id.textMessage);
        }
    }
}