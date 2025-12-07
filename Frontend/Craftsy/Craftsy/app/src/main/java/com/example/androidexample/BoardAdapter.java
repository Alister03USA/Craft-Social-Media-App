package com.example.androidexample;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.BoardViewHolder> {

    private List<BoardModel> boards;
    private Context context;

    public BoardAdapter(List<BoardModel> boards, Context context) {
        this.boards = boards;
        this.context = context;
    }

    @NonNull
    @Override
    public BoardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_board, parent, false);
        return new BoardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BoardViewHolder holder, int position) {
        BoardModel board = boards.get(position);

        holder.txtName.setText(board.getBoardName());
        holder.txtDescription.setText(board.getDescription());

        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, BoardDetailActivity.class);
            i.putExtra("boardId", board.getId());
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return boards.size();
    }

    static class BoardViewHolder extends RecyclerView.ViewHolder {

        TextView txtName, txtDescription;

        public BoardViewHolder(@NonNull View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txtBoardName);
            txtDescription = itemView.findViewById(R.id.txtBoardDescription);
        }
    }
}