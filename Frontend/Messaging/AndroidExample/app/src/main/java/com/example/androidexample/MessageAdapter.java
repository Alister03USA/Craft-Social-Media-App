package com.example.androidexample;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface MessageActions {
        void onReact(MessageItem m, String reaction);
        void onReply(MessageItem m);
        void onLongPress(MessageItem m);
    }

    private static final int LEFT = 0;
    private static final int RIGHT = 1;

    private final List<MessageItem> data;
    private final String me;
    private final MessageActions actions;

    public MessageAdapter(List<MessageItem> data, String currentUser, MessageActions actions) {
        this.data = data;
        this.me = currentUser;
        this.actions = actions;
    }

    @Override
    public int getItemViewType(int position) {
        return me.equals(data.get(position).getSender()) ? RIGHT : LEFT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == RIGHT) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new RightHolder(v);
        }
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message_received, parent, false);
        return new LeftHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int pos) {
        MessageItem m = data.get(pos);
        if (h instanceof LeftHolder) ((LeftHolder) h).bind(m);
        else ((RightHolder) h).bind(m);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    abstract class BaseHolder extends RecyclerView.ViewHolder {
        TextView tvMsg, tvMeta, tvReply, tvReacts;
        ImageButton btnReact, btnReply;

        BaseHolder(@NonNull View v) { super(v); }

        void bindCommon(MessageItem m) {
            // (1) Reply preview
            if (m.getReplyTo() != null) {
                tvReply.setVisibility(View.VISIBLE);
                SpannableString s = new SpannableString("Replying to • #" + m.getReplyTo());
                s.setSpan(new StyleSpan(Typeface.BOLD), 0, 11, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tvReply.setText(s);
            } else {
                tvReply.setVisibility(View.GONE);
            }

            // (2) Message + timestamp
            tvMsg.setText(m.getContent());
            tvMeta.setText(m.getTimestamp());

            // (3) Reactions display inline
            Map<String, Integer> reacts = m.getReactions();
            if (!reacts.isEmpty()) {
                StringJoiner joiner = new StringJoiner("  ");
                for (Map.Entry<String, Integer> e : reacts.entrySet()) {
                    joiner.add(e.getKey() + " " + e.getValue());
                }
                tvReacts.setVisibility(View.VISIBLE);
                tvReacts.setText(joiner.toString());
            } else {
                tvReacts.setVisibility(View.GONE);
            }

            // (4) Reaction and reply actions
            btnReact.setOnClickListener(v -> showReactMenu(v, m));
            btnReply.setOnClickListener(v -> actions.onReply(m));

            // (5) Long press → edit/delete placeholder
            itemView.setOnLongClickListener(v -> { actions.onLongPress(m); return true; });
        }

        private void showReactMenu(View anchor, MessageItem m) {
            PopupMenu pm = new PopupMenu(anchor.getContext(), anchor);
            MenuInflater mi = pm.getMenuInflater();
            mi.inflate(R.menu.menu_reactions, pm.getMenu());
            pm.setOnMenuItemClickListener((MenuItem i) -> {
                actions.onReact(m, String.valueOf(i.getTitle()));
                return true;
            });
            pm.show();
        }
    }

    class LeftHolder extends BaseHolder {
        LeftHolder(@NonNull View v) {
            super(v);
            tvMsg = v.findViewById(R.id.tvMsg);
            tvMeta = v.findViewById(R.id.tvMeta);
            tvReply = v.findViewById(R.id.tvReply);
            tvReacts = v.findViewById(R.id.tvReacts);
            btnReact = v.findViewById(R.id.btnReact);
            btnReply = v.findViewById(R.id.btnReply);
        }

        void bind(MessageItem m) { bindCommon(m); }
    }

    class RightHolder extends BaseHolder {
        RightHolder(@NonNull View v) {
            super(v);
            tvMsg = v.findViewById(R.id.tvMsg);
            tvMeta = v.findViewById(R.id.tvMeta);
            tvReply = v.findViewById(R.id.tvReply);
            tvReacts = v.findViewById(R.id.tvReacts);
            btnReact = v.findViewById(R.id.btnReact);
            btnReply = v.findViewById(R.id.btnReply);
        }

        void bind(MessageItem m) { bindCommon(m); }
    }
}