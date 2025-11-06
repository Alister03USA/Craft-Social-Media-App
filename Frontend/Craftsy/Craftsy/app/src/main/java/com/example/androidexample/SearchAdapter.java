package com.example.androidexample;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Universal adapter for Search tabs (Users, Groups, Projects, Tutorials)
 * Supports click listener for dynamic navigation (used in UserSearchFragment and ProjectSearchFragment).
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    // Unified listener interface for any search tab (user/project/group/tutorial)
    public interface OnSearchClickListener {
        void onItemClick(SearchItem item);
    }

    private final List<SearchItem> searchList;
    private final Context context;
    private final String currentType;
    private final OnSearchClickListener clickListener;

    // 🔹 Constructor with click listener (used by UserSearchFragment and ProjectSearchFragment)
    public SearchAdapter(Context context, List<SearchItem> searchList, String currentType,
                         OnSearchClickListener clickListener) {
        this.context = context;
        this.searchList = searchList;
        this.currentType = currentType;
        this.clickListener = clickListener;
    }

    // 🔹 Backward-compatible constructor for older fragments without click listener
    public SearchAdapter(Context context, List<SearchItem> searchList, String currentType) {
        this(context, searchList, currentType, null);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchItem item = searchList.get(position);

        holder.title.setText(item.getTitle());
        holder.desc.setText(item.getDescription());
        holder.type.setText(currentType.toUpperCase());

        holder.itemView.setOnClickListener(v -> {
            switch (currentType.toLowerCase()) {
                case "users":
                case "user": {
                    if (clickListener != null) {
                        clickListener.onItemClick(item);
                    } else {
                        Intent intent = new Intent(context, OutsideUserProfile.class);
                        intent.putExtra("username", item.getUsername());
                        intent.putExtra("logged_in_username", SessionManager.getInstance().getLoggedInUsername());
                        context.startActivity(intent);
                    }
                    break;
                }

                case "projects":
                case "project": {
                    if (clickListener != null) {
                        clickListener.onItemClick(item);
                    } else {
                        Log.d("SearchAdapter", "Clicked project: " + item.getTitle() + " by " + item.getUsername());
                        Intent intent = new Intent(context, FeedDetailActivity.class);
                        intent.putExtra("username", item.getUsername());
                        intent.putExtra("projectName", item.getTitle());
                        intent.putExtra("projectDesc", item.getDescription());
                        context.startActivity(intent);
                    }
                    break;
                }

                case "groups":
                case "group": {
                    if (clickListener != null) {
                        clickListener.onItemClick(item);
                    } else {
                        Intent intent = new Intent(context, GroupDetailsActivity.class);
                        intent.putExtra("groupId", Long.parseLong(item.getUsername()));
                        intent.putExtra("groupName", item.getTitle());
                        intent.putExtra("groupDescription", item.getDescription());
                        context.startActivity(intent);
                    }
                    break;
                }



                case "tutorials":
                case "tutorial": {
                    // Future support
                    break;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return searchList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, desc, type;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.resultTitle);
            desc = itemView.findViewById(R.id.resultDesc);
            type = itemView.findViewById(R.id.resultType);
        }
    }
}