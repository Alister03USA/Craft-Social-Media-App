package com.example.androidexample;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface MessageActions {
        void onReact(MessageItem m, String reactionType);
        void onRemoveReact(MessageItem m, String reactionType);
        void onReply(MessageItem m);
        void onLongPress(MessageItem m);
    }

    private static final int LEFT = 0;    // received
    private static final int RIGHT = 1;   // sent

    private final List<MessageItem> data;
    private final String me;
    private final MessageActions actions;

    private static final Map<String, String> reactionEmojiMap = new HashMap<>();
    static {
        reactionEmojiMap.put("like", "👍");
        reactionEmojiMap.put("love", "❤️");
        reactionEmojiMap.put("laugh", "😂");
        reactionEmojiMap.put("wow", "😮");
        reactionEmojiMap.put("sad", "😢");
        reactionEmojiMap.put("fire", "🔥");
    }

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

        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new LeftHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int pos) {
        MessageItem m = data.get(pos);
        if (h instanceof LeftHolder) ((LeftHolder) h).bind(m);
        else ((RightHolder) h).bind(m);
    }

    @Override
    public int getItemCount() { return data.size(); }

    // ============================================================
    // Base holder with common logic
    // ============================================================
    abstract class BaseHolder extends RecyclerView.ViewHolder {

        TextView tvMsg, tvMeta, tvReply, tvReacts;
        ImageButton btnReact, btnReply;
        ImageView imgContent;

        // NEW: reply preview fields
        TextView tvReplySender, tvReplySnippet;
        View replyBox;

        BaseHolder(@NonNull View v) {
            super(v);

            replyBox = v.findViewById(R.id.replyBox);
            tvReplySender = v.findViewById(R.id.tvReplySender);
            tvReplySnippet = v.findViewById(R.id.tvReplySnippet);
        }

        void bindCommon(MessageItem m) {

            // RESET UI
            tvReply.setText("");
            tvReply.setVisibility(View.GONE);
            tvMsg.setText("");
            imgContent.setVisibility(View.GONE);
            tvReacts.setVisibility(View.GONE);
            tvReacts.setText("");
            tvMeta.setText("");

            // -----------------------------------------
            // WHATSAPP STYLE REPLY PREVIEW
            // -----------------------------------------
            if (m.getReplyTo() != null && m.getReplySender() != null) {
                replyBox.setVisibility(View.VISIBLE);
                tvReplySender.setText(m.getReplySender());
                tvReplySnippet.setText(m.getReplySnippet());
            } else {
                replyBox.setVisibility(View.GONE);
            }

            // Text message
            if (!TextUtils.isEmpty(m.getContent())) {
                tvMsg.setText(m.getContent());
            }

            // Image message
            if (m.hasImage()) {
                imgContent.setVisibility(View.VISIBLE);
                Glide.with(itemView.getContext())
                        .load(m.getImageUrl())
                        .into(imgContent);
            }

            // Timestamp
            if (!TextUtils.isEmpty(m.getTimestamp())) {
                tvMeta.setText(m.getTimestamp());
            }

            // Reactions
            if (!m.getReactions().isEmpty()) {
                StringBuilder sb = new StringBuilder();

                for (String key : m.getReactions().keySet()) {
                    String emoji = reactionEmojiMap.getOrDefault(key, key);
                    int count = m.getReactions().get(key);

                    sb.append(emoji);
                    if (count > 1) sb.append(" x").append(count);
                    sb.append("  ");
                }

                tvReacts.setVisibility(View.VISIBLE);
                tvReacts.setText(sb.toString().trim());
            }

            btnReact.setOnClickListener(v -> showReactMenu(v, m));
            btnReply.setOnClickListener(v -> actions.onReply(m));

            itemView.setOnLongClickListener(v -> {
                actions.onLongPress(m);
                return true;
            });
        }

        private void showReactMenu(View anchor, MessageItem m) {
            PopupMenu pm = new PopupMenu(anchor.getContext(), anchor);
            MenuInflater mi = pm.getMenuInflater();
            mi.inflate(R.menu.menu_reactions, pm.getMenu());

            pm.setOnMenuItemClickListener((MenuItem item) -> {
                String type = mapMenuItemToType(item.getItemId());

                if (m.getReactions().containsKey(type)) {
                    actions.onRemoveReact(m, type);
                    m.getReactions().remove(type);
                } else {
                    m.getReactions().clear();
                    m.getReactions().put(type, 1);
                    actions.onReact(m, type);
                }

                notifyItemChanged(getBindingAdapterPosition());
                return true;
            });

            pm.show();
        }

        private String mapMenuItemToType(int id) {
            if (id == R.id.reaction_like) return "like";
            if (id == R.id.reaction_love) return "love";
            if (id == R.id.reaction_laugh) return "laugh";
            if (id == R.id.reaction_wow) return "wow";
            if (id == R.id.reaction_sad) return "sad";
            if (id == R.id.reaction_fire) return "fire";
            return "unknown";
        }
    }

    // ============================================================
    // RECEIVED MESSAGES HOLDER (LEFT)
    // ============================================================
    class LeftHolder extends BaseHolder {

        TextView tvSenderName;

        LeftHolder(@NonNull View v) {
            super(v);

            tvSenderName = v.findViewById(R.id.tvSenderName);

            tvMsg = v.findViewById(R.id.tvMsg);
            tvMeta = v.findViewById(R.id.tvMeta);
            tvReply = v.findViewById(R.id.tvReply);
            tvReacts = v.findViewById(R.id.tvReacts);
            btnReact = v.findViewById(R.id.btnReact);
            btnReply = v.findViewById(R.id.btnReply);
            imgContent = v.findViewById(R.id.imgContent);
        }

        void bind(MessageItem m) {
            tvSenderName.setText(m.getSender());
            tvSenderName.setVisibility(View.VISIBLE);
            bindCommon(m);
        }
    }

    // ============================================================
    // SENT MESSAGES HOLDER (RIGHT)
    // ============================================================
    class RightHolder extends BaseHolder {

        RightHolder(@NonNull View v) {
            super(v);

            tvMsg = v.findViewById(R.id.tvMsg);
            tvMeta = v.findViewById(R.id.tvMeta);
            tvReply = v.findViewById(R.id.tvReply);
            tvReacts = v.findViewById(R.id.tvReacts);
            btnReact = v.findViewById(R.id.btnReact);
            btnReply = v.findViewById(R.id.btnReply);
            imgContent = v.findViewById(R.id.imgContent);
        }

        void bind(MessageItem m) {
            bindCommon(m);
        }
    }
}