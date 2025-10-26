package com.example.androidexample;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import java.util.List;

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

        // 🔹 UPDATED for backend image loading (Volley ImageRequest)
        if (pattern.getPatternImage() != null && !pattern.getPatternImage().isEmpty()) {
            String imageUrl = pattern.getPatternImage();

            ImageRequest imageRequest = new ImageRequest(
                    imageUrl,
                    new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap response) {
                            holder.patternImage.setImageBitmap(response);
                        }
                    },
                    0, 0,
                    ImageView.ScaleType.CENTER_CROP,
                    Bitmap.Config.RGB_565,
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("PatternAdapter", "Image load failed: " + error.getMessage());
                            holder.patternImage.setImageResource(R.drawable.craftsy_image_placeholder); // fallback drawable
                        }
                    }
            );

            VolleySingleton.getInstance(context).addToRequestQueue(imageRequest);
        } else {
            holder.patternImage.setImageResource(R.drawable.craftsy_image_placeholder); // default placeholder
        }

        // 🔹 Open pattern details screen when item is clicked
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
            intent.putExtra("username", pattern.getUsername()); // 🔹 passes backend username
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
