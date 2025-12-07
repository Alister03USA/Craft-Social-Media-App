package com.example.androidexample;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import java.util.List;

public class PatternAdapter extends RecyclerView.Adapter<PatternAdapter.PatternViewHolder> {

    private Context context;
    private List<Pattern> patterns;

    // board mode variables
    private boolean isBoardMode = false;
    private long boardId;

    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";

    public PatternAdapter(Context context, List<Pattern> patterns) {
        this.context = context;
        this.patterns = patterns;
    }

    // constructor for board mode
    public PatternAdapter(Context context, List<Pattern> patterns, long boardId) {
        this.context = context;
        this.patterns = patterns;
        this.boardId = boardId;
        this.isBoardMode = true;
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

    /* =========================
       HIDE IMAGE IN BOARD MODE
       ========================= */
        if (isBoardMode) {
            holder.patternImage.setVisibility(View.GONE);
        } else {
            holder.patternImage.setVisibility(View.VISIBLE);

            // backend image loading
            if (pattern.getPatternImage() != null && !pattern.getPatternImage().isEmpty()) {
                String imageUrl = pattern.getPatternImage();

                ImageRequest imageRequest = new ImageRequest(
                        imageUrl,
                        response -> holder.patternImage.setImageBitmap(response),
                        0, 0,
                        ImageView.ScaleType.CENTER_CROP,
                        Bitmap.Config.RGB_565,
                        error -> {
                            Log.e("PatternAdapter", "Image load failed: " + error.getMessage());
                            holder.patternImage.setImageResource(R.drawable.craftsy_image_placeholder);
                        }
                );

                VolleySingleton.getInstance(context).addToRequestQueue(imageRequest);
            } else {
                holder.patternImage.setImageResource(R.drawable.craftsy_image_placeholder);
            }
        }

        // normal mode: open pattern detail
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PatternDetailActivity.class);
            intent.putExtra("patternName", pattern.getPatternName());
            intent.putExtra("ownerUsername", pattern.getUsername());
            context.startActivity(intent);
        });

        // board mode remove button
        if (isBoardMode) {
            holder.btnRemovePattern.setVisibility(View.VISIBLE);
            holder.btnRemovePattern.setOnClickListener(v -> removePatternFromBoard(pattern, holder.getAdapterPosition()));
        } else {
            holder.btnRemovePattern.setVisibility(View.GONE);
        }
    }

    private void removePatternFromBoard(Pattern pattern, int position) {
        String url = BASE_URL + "/board/" + boardId + "/pattern/" + pattern.getUsername() + "/" + pattern.getPatternName() + "/delete";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                null,
                response -> {
                    Toast.makeText(context, "Removed from board", Toast.LENGTH_SHORT).show();
                    patterns.remove(position);
                    notifyItemRemoved(position);
                },
                error -> Toast.makeText(context, "Failed to remove", Toast.LENGTH_SHORT).show()
        );

        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    @Override
    public int getItemCount() {
        return patterns.size();
    }

    public static class PatternViewHolder extends RecyclerView.ViewHolder {

        ImageView patternImage;
        RatingBar patternRating;
        TextView patternName, patternType, patternDifficulty;
        Button btnRemovePattern;

        public PatternViewHolder(@NonNull View itemView) {
            super(itemView);

            patternImage = itemView.findViewById(R.id.patternImage);
            patternRating = itemView.findViewById(R.id.patternRating);
            patternName = itemView.findViewById(R.id.patternName);
            patternType = itemView.findViewById(R.id.patternType);
            patternDifficulty = itemView.findViewById(R.id.patternDifficulty);
            btnRemovePattern = itemView.findViewById(R.id.btnRemovePattern);
        }
    }
}