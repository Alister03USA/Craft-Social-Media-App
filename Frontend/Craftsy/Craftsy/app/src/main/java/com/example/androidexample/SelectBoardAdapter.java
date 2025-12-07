package com.example.androidexample;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SelectBoardAdapter extends RecyclerView.Adapter<SelectBoardAdapter.VH> {

    private List<BoardModel> boards;
    private Context context;
    private SelectBoardDialog.SelectBoardCallback callback;
    private Dialog dialog;

    public SelectBoardAdapter(List<BoardModel> boards, Context context,
                              SelectBoardDialog.SelectBoardCallback callback, Dialog dialog) {
        this.boards = boards;
        this.context = context;
        this.callback = callback;
        this.dialog = dialog;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_select_board, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        BoardModel board = boards.get(position);
        holder.txtBoardName.setText(board.getBoardName());

        holder.itemView.setOnClickListener(v -> {
            callback.onBoardSelected(board);
            dialog.dismiss();
        });
    }

    @Override
    public int getItemCount() {
        return boards.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtBoardName;

        VH(@NonNull View itemView) {
            super(itemView);
            txtBoardName = itemView.findViewById(R.id.txtSelectBoardName);
        }
    }
}