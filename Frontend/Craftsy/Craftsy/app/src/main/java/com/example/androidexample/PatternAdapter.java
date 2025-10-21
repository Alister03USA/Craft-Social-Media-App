package com.example.androidexample;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import android.content.Context;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;





public class PatternAdapter extends RecyclerView.Adapter<PatternAdapter.PatternViewHolder> {

    private Context context;
    private List<Pattern> patterns;

    public PatternAdapter(Context context, List<Pattern> patterns) {
        this.context = context;
        this.patterns = patterns;
    }

    @NonNull
    @Override
    public PatternViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pattern, parent, false);
        return new PatternViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatternViewHolder holder, int position) {
        Pattern pattern = patterns.get(position);

        holder.patternName.setText(pattern.getPatternName());
        holder.patternType.setText("Type: " + pattern.getPatternType());
        holder.patternDifficulty.setText("Difficulty: " + pattern.getDifficulty());
        holder.patternRating.setRating(pattern.getRating());

        // Load image manually (no Glide)
        new Thread(() -> {
            try {
                URL url = new URL(pattern.getPatternImage());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);

                new Handler(Looper.getMainLooper()).post(() -> holder.patternImage.setImageBitmap(bitmap));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // Card click listener
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PatternDetailActivity.class);
            intent.putExtra("patternName", pattern.getPatternName());
            intent.putExtra("patternType", pattern.getPatternType());
            intent.putExtra("difficulty", pattern.getDifficulty());
            intent.putExtra("rating", pattern.getRating());
            intent.putExtra("image", pattern.getPatternImage());
            intent.putExtra("description", pattern.getDescription());
            intent.putExtra("supplies", pattern.getSupplies());
            intent.putExtra("link", pattern.getPatternLink());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return patterns.size();
    }

    public static class PatternViewHolder extends RecyclerView.ViewHolder {
        ImageView patternImage;
        RatingBar patternRating;
        TextView patternName, patternType, patternDifficulty;

        public PatternViewHolder(@NonNull View itemView) {
            super(itemView);
            patternImage = itemView.findViewById(R.id.patternImage);
            patternRating = itemView.findViewById(R.id.patternRating);
            patternName = itemView.findViewById(R.id.patternName);
            patternType = itemView.findViewById(R.id.patternType);
            patternDifficulty = itemView.findViewById(R.id.patternDifficulty);
        }
    }
}
