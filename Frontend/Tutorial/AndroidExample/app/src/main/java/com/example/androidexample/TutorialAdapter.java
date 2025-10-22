package com.example.androidexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TutorialAdapter extends RecyclerView.Adapter<TutorialAdapter.VH> {

    public interface OnClick { void onClick(long id); }

    private final List<TutorialItem> data;
    private final OnClick listener;

    public TutorialAdapter(List<TutorialItem> data, OnClick listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tutorial_item, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        TutorialItem t = data.get(pos);
        h.title.setText(t.getTitle());
        h.desc.setText(t.getDescription());
        h.cat.setText(t.getCategory());
        h.itemView.setOnClickListener(v -> listener.onClick(t.getId()));
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, desc, cat; ImageView thumb;
        VH(View v) {
            super(v);
            title = v.findViewById(R.id.tutorialTitleText);
            desc  = v.findViewById(R.id.tutorialDescText);
            cat   = v.findViewById(R.id.tutorialCategoryText);
            thumb = v.findViewById(R.id.tutorialThumbnail);
        }
    }
}