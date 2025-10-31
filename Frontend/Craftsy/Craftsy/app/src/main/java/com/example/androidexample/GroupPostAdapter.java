package com.example.androidexample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class GroupPostAdapter extends RecyclerView.Adapter<GroupPostAdapter.PostViewHolder> {

    private final List<GroupPostModel> posts;

    public GroupPostAdapter(List<GroupPostModel> posts) { this.posts = posts; }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_post_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        GroupPostModel post = posts.get(position);
        holder.username.setText(post.getUsername());
        holder.text.setText(post.getText());

        if (post.getImageUri() != null && !post.getImageUri().isEmpty()) {
            new Thread(() -> {
                try {
                    InputStream is = (InputStream) new URL(post.getImageUri()).getContent();
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    holder.image.post(() -> holder.image.setImageBitmap(bitmap));
                } catch (Exception e) {
                    e.printStackTrace();
                    holder.image.post(() -> holder.image.setImageResource(R.drawable.ic_groups));
                }
            }).start();
        } else {
            holder.image.setImageResource(R.drawable.ic_groups);
        }
    }

    @Override
    public int getItemCount() { return posts.size(); }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView username, text;
        ImageView image;

        PostViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.postUsername);
            text = itemView.findViewById(R.id.postText);
            image = itemView.findViewById(R.id.postImage);
        }
    }
}
