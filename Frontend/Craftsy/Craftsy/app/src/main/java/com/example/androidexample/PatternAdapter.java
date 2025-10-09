package com.example.androidexample;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import android.content.Context;


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

       // holder.patternImagePlaceholder.setBackgroundColor(Color.LTGRAY);


        holder.patternRating.setRating(pattern.getRating());

        holder.submitButton.setOnClickListener(v -> {
            float rating = holder.patternRating.getRating();
            String comment = holder.patternComment.getText().toString();

            submitPatternFeedback(pattern.getId(), rating, comment);

            holder.patternComment.setText("");
        });
    }

    @Override
    public int getItemCount() {
        return patterns.size();
    }

    public static class PatternViewHolder extends RecyclerView.ViewHolder {
        ImageView patternImage;
        RatingBar patternRating;
        EditText patternComment;
        Button submitButton;

        public PatternViewHolder(@NonNull View itemView) {
            super(itemView);
            patternImage = itemView.findViewById(R.id.patternImage);
            patternRating = itemView.findViewById(R.id.patternRating);
            patternComment = itemView.findViewById(R.id.patternComment);
            submitButton = itemView.findViewById(R.id.submitButton);
        }
    }

    private void submitPatternFeedback(int patternId, float rating, String comment) {
        String url = "https://yourbackend.com/patterns/" + patternId + "/feedback";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("rating", rating);
            jsonBody.put("comment", comment);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> Toast.makeText(context, "Feedback submitted!", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(context).add(request);
    }
}

