package com.example.androidexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PointHistoryAdapter extends RecyclerView.Adapter<PointHistoryAdapter.ViewHolder> {

    private final List<PointHistoryItem> list;

    public PointHistoryAdapter(List<PointHistoryItem> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_point_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        PointHistoryItem item = list.get(pos);
        holder.points.setText("+ " + item.getPoints());
        holder.action.setText(item.getAction());
        holder.refId.setText("Ref: " + item.getReferenceId());
        holder.date.setText(item.getDate());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView points, action, refId, date;
        public ViewHolder(@NonNull View v) {
            super(v);
            points = v.findViewById(R.id.historyPoints);
            action = v.findViewById(R.id.historyAction);
            refId = v.findViewById(R.id.historyRefId);
            date = v.findViewById(R.id.historyDate);
        }
    }
}