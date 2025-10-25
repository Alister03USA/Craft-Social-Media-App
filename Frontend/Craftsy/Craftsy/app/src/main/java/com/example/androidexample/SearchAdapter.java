package com.example.androidexample;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private final List<SearchItem> searchList;
    private final Context context;
    private final String currentType;

    public SearchAdapter(Context context, List<SearchItem> searchList, String currentType) {
        this.context = context;
        this.searchList = searchList;
        this.currentType = currentType;
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
                    Intent intent = new Intent(context, OutsideUserProfile.class);
                    intent.putExtra("username", item.getUsername());
                    intent.putExtra("logged_in_username", SessionManager.getInstance().getLoggedInUsername());
                    context.startActivity(intent);
                    break;
                }

                case "projects":
                case "project": {
                    Intent intent = new Intent(context, FeedDetailActivity.class);
                    intent.putExtra("username", item.getUsername());
                    intent.putExtra("projectName", item.getTitle());
                    intent.putExtra("projectDesc", item.getDescription());
                    context.startActivity(intent);
                    break;
                }

                case "groups":
                case "group": {
                    // TODO: Uncomment once GroupDetailActivity is ready
                    /*
                    Intent intent = new Intent(context, GroupDetailActivity.class);
                    intent.putExtra("groupName", item.getTitle());
                    context.startActivity(intent);
                    */
                    break;
                }

                case "tutorials":
                case "tutorial": {
                    // TODO: Uncomment once TutorialDetailActivity is ready
                    /*
                    Intent intent = new Intent(context, TutorialDetailActivity.class);
                    intent.putExtra("tutorialId", item.getExtra());
                    context.startActivity(intent);
                    */
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