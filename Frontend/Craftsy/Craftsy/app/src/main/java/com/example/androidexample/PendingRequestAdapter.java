package com.example.androidexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PendingRequestAdapter extends RecyclerView.Adapter<PendingRequestAdapter.Holder> {

    public interface OnAction {
        void onApprove(long requestId, String username);
        void onDecline(long requestId, String username);
    }

    private final List<PendingRequestModel> items;
    private final OnAction onAction;

    public PendingRequestAdapter(List<PendingRequestModel> items, OnAction onAction) {
        this.items = items;
        this.onAction = onAction;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pending_request, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        PendingRequestModel m = items.get(position);
        holder.usernameTv.setText(m.getUsername());
        holder.requestedAtTv.setText(
                m.getRequestedAt() != null && !m.getRequestedAt().isEmpty() ? m.getRequestedAt() : "N/A"
        );
        holder.approveBtn.setOnClickListener(v -> onAction.onApprove(m.getRequestId(), m.getUsername()));
        holder.declineBtn.setOnClickListener(v -> onAction.onDecline(m.getRequestId(), m.getUsername()));
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView usernameTv, requestedAtTv;
        Button approveBtn, declineBtn;
        Holder(@NonNull View itemView) {
            super(itemView);
            usernameTv = itemView.findViewById(R.id.pendingUsernameTv);
            requestedAtTv = itemView.findViewById(R.id.pendingRequestedAtTv);
            approveBtn = itemView.findViewById(R.id.pendingApproveBtn);
            declineBtn = itemView.findViewById(R.id.pendingDeclineBtn);
        }
    }
}
