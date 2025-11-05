package com.example.androidexample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {

    private final List<Review> reviews;
    private final Context context;
    private final OnLikeClickListener likeClickListener;

    public interface OnLikeClickListener {
        void onLikeClick(Review review);
    }

    public ReviewsAdapter(Context context, List<Review> reviews, OnLikeClickListener listener) {
        this.context = context;
        this.reviews = reviews;
        this.likeClickListener = listener;
    }

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.review_item, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {
        holder.bind(reviews.get(position));
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView reviewText, reviewDate, likeButton;

        public ReviewViewHolder(View itemView) {
            super(itemView);
            reviewText = itemView.findViewById(R.id.reviewText);
            reviewDate = itemView.findViewById(R.id.reviewDate);
            likeButton = itemView.findViewById(R.id.likeButton);
        }

        public void bind(Review review) {
            reviewText.setText(review.text);
            reviewDate.setText(review.date);
            likeButton.setText("❤️ " + review.likes);
            likeButton.setOnClickListener(v -> likeClickListener.onLikeClick(review));
        }
    }
}
