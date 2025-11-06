package com.example.androidexample;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class GroupPostAdapter extends RecyclerView.Adapter<GroupPostAdapter.PostViewHolder> {

    private final List<GroupPostModel> posts;
    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";

    public GroupPostAdapter(List<GroupPostModel> posts) {
        this.posts = posts;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        GroupPostModel post = posts.get(position);

        holder.username.setText(post.getUsername());

        String content = post.getContent();
        if (content != null && !content.trim().isEmpty()) {
            holder.text.setVisibility(View.VISIBLE);
            holder.text.setText(content);
        } else {
            holder.text.setVisibility(View.GONE);
        }

        String mediaUrl = post.getMediaUrl();
        if (mediaUrl != null && !mediaUrl.trim().isEmpty()) {
            holder.image.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(mediaUrl.startsWith("http") ? mediaUrl :
                            BASE_URL + "/groupMessage/image/" + post.getMessageId())
                    .into(holder.image);
        } else holder.image.setVisibility(View.GONE);

        // --- CLICK TO OPEN COMMENTS ACTIVITY ---
        holder.viewComments.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), CommentsActivity.class);
            intent.putExtra("messageId", post.getMessageId());
            intent.putExtra("groupId", post.getGroupId());
            holder.itemView.getContext().startActivity(intent);
        });


    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView username, text, viewComments;
        ImageView image;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.groupPostUsername);
            text = itemView.findViewById(R.id.groupPostText);
            image = itemView.findViewById(R.id.groupPostImage);
            viewComments = itemView.findViewById(R.id.groupPostViewComments);
        }
    }

    @Override
    public int getItemCount() {
        return posts != null ? posts.size() : 0;
    }
}
