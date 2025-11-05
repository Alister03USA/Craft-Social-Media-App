package com.example.androidexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.VH> {

    public interface RemoveCallback { void onRemove(String username); }

    private final List<MemberModel> members;
    private final RemoveCallback removeCallback; // may be null for simple view

    public MemberAdapter(List<MemberModel> members) { this(members, null); }

    public MemberAdapter(List<MemberModel> members, RemoveCallback removeCallback) {
        this.members = members;
        this.removeCallback = removeCallback;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        MemberModel m = members.get(position);
        holder.username.setText(m.getUsername());
        holder.email.setText(m.getEmail());
        if (removeCallback != null) {
            holder.removeBtn.setVisibility(View.VISIBLE);
            holder.removeBtn.setOnClickListener(v -> removeCallback.onRemove(m.getUsername()));
        } else {
            holder.removeBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return members.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView username, email;
        Button removeBtn;
        VH(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.memberUsername);
            email = itemView.findViewById(R.id.memberEmail);
            removeBtn = itemView.findViewById(R.id.memberRemoveBtn);
        }
    }
}
