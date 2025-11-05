package com.example.androidexample;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    public interface OnGroupClickListener {
        void onGroupClick(GroupModel group);
    }

    private final List<GroupModel> groups;
    private final OnGroupClickListener listener;

    public GroupAdapter(List<GroupModel> groups, OnGroupClickListener listener) {
        this.groups = groups;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_item, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        GroupModel group = groups.get(position);
        holder.name.setText(group.getName());
        holder.description.setText(group.getDescription());

        // Existing click behaviour
        holder.itemView.setOnClickListener(v -> listener.onGroupClick(group));

        // Overflow menu (minimal addition) — launches GroupDetails or Admin
        holder.overflow.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(holder.itemView.getContext(), holder.overflow);
            MenuInflater inflater = popup.getMenuInflater();
            // we construct menu programmatically to avoid needing extra xml files
            popup.getMenu().add(0, 1, 0, "Group details");
            popup.getMenu().add(0, 2, 1, "Admin");

            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    Intent intent = new Intent(holder.itemView.getContext(), GroupDetailsActivity.class);
                    intent.putExtra("groupId", group.getId());
                    intent.putExtra("groupName", group.getName());
                    intent.putExtra("groupDescription", group.getDescription()); // <-- Add this

                    holder.itemView.getContext().startActivity(intent);
                    return true;
                } else if (item.getItemId() == 2) {
                    Intent intent = new Intent(holder.itemView.getContext(), AdminActivity.class);
                    intent.putExtra("groupId", group.getId());
                    intent.putExtra("groupName", group.getName());
                    holder.itemView.getContext().startActivity(intent);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() { return groups.size(); }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView name, description;
        ImageButton overflow;

        GroupViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.groupName);
            description = itemView.findViewById(R.id.groupDescription);
            overflow = itemView.findViewById(R.id.groupOptions);
        }
    }
}
