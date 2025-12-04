package com.example.androidexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.Holder> {

    private final List<ConversationItem> data;
    private final OnConvoClick listener;
    private final OnConvoDelete deleteListener;
    private final MessagingHomeActivity activity;

    public interface OnConvoClick {
        void onOpen(ConversationItem item);
    }

    public interface OnConvoDelete {
        void onDelete(ConversationItem item);
    }

    public ConversationAdapter(
            List<ConversationItem> data,
            OnConvoClick listener,
            OnConvoDelete deleteListener,
            MessagingHomeActivity activity
    ) {
        this.data = data;
        this.listener = listener;
        this.deleteListener = deleteListener;
        this.activity = activity;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int pos) {
        ConversationItem item = data.get(pos);

        h.name.setText(item.getDisplayName());
        h.last.setText(item.getLastMessage());
        h.time.setText(item.getTimestamp());

        if (item.isGroup()) {
            h.icon.setImageResource(R.drawable.ic_groups);
        } else {
            long id = item.getProfileImageId();

            if (id > 0) {
                activity.loadProfilePic(id, h.icon);
            } else {
                h.icon.setImageResource(R.drawable.profile);
            }
        }

        h.itemView.setOnClickListener(v -> listener.onOpen(item));
        h.btnDelete.setOnClickListener(v -> deleteListener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class Holder extends RecyclerView.ViewHolder {

        ImageView icon;
        TextView name, last, time;
        ImageButton btnDelete;

        Holder(@NonNull View v) {
            super(v);
            icon = v.findViewById(R.id.ivIcon);
            name = v.findViewById(R.id.tvName);
            last = v.findViewById(R.id.tvLast);
            time = v.findViewById(R.id.tvTime);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}