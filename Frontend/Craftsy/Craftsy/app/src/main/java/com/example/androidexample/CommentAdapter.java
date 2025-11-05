package com.example.androidexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;



public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentHolder> {

    private final List<CommentModel> comments;

    public CommentAdapter(List<CommentModel> comments) {
        this.comments = comments;
    }

    @NonNull
    @Override
    public CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group_comment, parent, false);
        return new CommentHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentHolder holder, int position) {
        CommentModel c = comments.get(position);
        holder.username.setText(c.getUsername());
        holder.comment.setText(c.getComment());
    }

    @Override
    public int getItemCount() { return comments.size(); }

    static class CommentHolder extends RecyclerView.ViewHolder {
        TextView username, comment;
        CommentHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.commentUsername);
            comment = itemView.findViewById(R.id.commentText);
        }
    }
}
