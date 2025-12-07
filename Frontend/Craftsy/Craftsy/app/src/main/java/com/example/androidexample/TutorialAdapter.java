package com.example.androidexample;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import java.util.List;

public class TutorialAdapter extends RecyclerView.Adapter<TutorialAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(TutorialItem item);
    }

    private final Context context;
    private final List<TutorialItem> tutorialList;
    private final OnItemClickListener listener;

    private Long boardId; // null unless in BoardDetailActivity

    // Normal constructor (search, feed tutorial list)
    public TutorialAdapter(Context context, List<TutorialItem> tutorialList, OnItemClickListener listener) {
        this.context = context;
        this.tutorialList = tutorialList;
        this.listener = listener;
        this.boardId = null;
    }

    // Board remove-mode constructor
    public TutorialAdapter(Context context, List<TutorialItem> tutorialList, Long boardId) {
        this.context = context;
        this.tutorialList = tutorialList;
        this.listener = null;
        this.boardId = boardId;
    }

    @NonNull
    @Override
    public TutorialAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.tutorial_item, parent, false);
        return new TutorialAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TutorialAdapter.ViewHolder holder, int position) {
        TutorialItem item = tutorialList.get(position);

        holder.title.setText(item.getTitle());
        holder.category.setText(item.getCategory() != null ? item.getCategory() : "");
        holder.username.setText(item.getUsername() != null ? "@" + item.getUsername() : "@Unknown");
        holder.description.setText(item.getDescription() != null ? item.getDescription() : "");

        // Normal click opens detail
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TutorialDetailActivity.class);
            intent.putExtra("id", item.getId());
            intent.putExtra("title", item.getTitle());
            intent.putExtra("description", item.getDescription());
            intent.putExtra("category", item.getCategory());
            intent.putExtra("fileUrl", item.getFileURL());
            intent.putExtra("username", item.getUsername());
            context.startActivity(intent);
        });

        // Remove button only for BoardDetailActivity mode
        if (boardId != null) {
            holder.removeButton.setVisibility(View.VISIBLE);

            holder.removeButton.setOnClickListener(v -> {
                String url = "http://coms-3090-028.class.las.iastate.edu:8080/board/"
                        + boardId + "/tutorial/" + item.getId() + "/delete";

                JsonObjectRequest req = new JsonObjectRequest(
                        Request.Method.PUT,
                        url,
                        null,
                        response -> {
                            tutorialList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, tutorialList.size());
                            Toast.makeText(context, "Tutorial removed", Toast.LENGTH_SHORT).show();
                        },
                        error -> Toast.makeText(context, "Failed to remove", Toast.LENGTH_SHORT).show()
                );

                VolleySingleton.getInstance(context).addToRequestQueue(req);
            });
        }
    }

    @Override
    public int getItemCount() {
        return tutorialList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, category, username, description;
        Button removeButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tutorialTitle);
            category = itemView.findViewById(R.id.tutorialCategory);
            username = itemView.findViewById(R.id.tutorialUsername);
            description = itemView.findViewById(R.id.tutorialDescription);
            removeButton = itemView.findViewById(R.id.buttonRemoveTutorial);
        }
    }
}