package com.example.androidexample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;

import java.util.List;

public class ReviewsAdapter extends ArrayAdapter<Review> {
    private Context context;
    private List<Review> reviews;
    private LikeClickListener likeClickListener;

    public interface LikeClickListener {
        void onLikeClicked(Review review);
    }

    public ReviewsAdapter(@NonNull Context context, List<Review> reviews, LikeClickListener listener) {
        super(context, 0, reviews);
        this.context = context;
        this.reviews = reviews;
        this.likeClickListener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Review review = reviews.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.review_item, parent, false);
        }

        TextView reviewText = convertView.findViewById(R.id.reviewText);
        TextView reviewDate = convertView.findViewById(R.id.reviewDate);
        Button likeButton = convertView.findViewById(R.id.likeButton);

        reviewText.setText(review.text);
        reviewDate.setText(review.date);
        likeButton.setText("❤️ " + review.likes);

        likeButton.setOnClickListener(v -> {
            if (likeClickListener != null) {
                likeClickListener.onLikeClicked(review);
            }
        });

        return convertView;
    }
}
