package com.example.androidexample;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
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

    private static final int LEFT = 0;
    private static final int RIGHT = 1;
    private static final String TAG = "MessageAdapter";

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

    abstract class BaseHolder extends RecyclerView.ViewHolder {
        TextView tvMsg, tvMeta, tvReply, tvReacts;
        ImageButton btnReact, btnReply;
        ImageView imgContent;
        TextView tvSender;

        BaseHolder(@NonNull View v) { super(v); }

        void bindCommon(MessageItem m) {
            // Show sender name for received messages in group chats
            if (tvSender != null) {
                if (!m.getSender().equals(me) && !TextUtils.isEmpty(m.getSender())) {
                    tvSender.setVisibility(View.VISIBLE);
                    tvSender.setText(m.getSender());
                } else {
                    tvSender.setVisibility(View.GONE);
                }
            }

            // Reply header
            if (m.getReplyTo() != null) {
                tvReply.setVisibility(View.VISIBLE);
                String parentText = findParentText(m.getReplyTo());
                if (!TextUtils.isEmpty(parentText)) {
                    if (parentText.length() > 40)
                        parentText = parentText.substring(0, 40) + "...";
                    SpannableString s = new SpannableString("Replying to \"" + parentText + "\"");
                    s.setSpan(new StyleSpan(Typeface.BOLD), 0, 12, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    tvReply.setText(s);
                } else tvReply.setText("Replying to message");
            } else {
                tvReply.setVisibility(View.GONE);
            }

            // Message text
            tvMsg.setText(m.getContent());

            // Image content
            if (m.hasImage()) {
                imgContent.setVisibility(View.VISIBLE);
                Glide.with(itemView.getContext())
                        .load(m.getImageUrl())
                        .placeholder(R.drawable.ic_post_placeholder)
                        .error(R.drawable.ic_post_placeholder)
                        .into(imgContent);
            } else {
                imgContent.setVisibility(View.GONE);
            }

            // Timestamp
            tvMeta.setText(m.getTimestamp());

            // Reactions
            Map<String, Integer> reacts = m.getReactions();
            if (!reacts.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, Integer> e : reacts.entrySet()) {
                    String emoji = reactionEmojiMap.getOrDefault(e.getKey(), e.getKey());
                    sb.append(emoji);
                    if (e.getValue() > 1) sb.append(" x").append(e.getValue());
                    sb.append("  ");
                }
                tvReacts.setVisibility(View.VISIBLE);
                tvReacts.setText(sb.toString().trim());
            } else tvReacts.setVisibility(View.GONE);

            // Buttons and long press
            btnReact.setOnClickListener(v -> showReactMenu(v, m));
            btnReply.setOnClickListener(v -> actions.onReply(m));
            itemView.setOnLongClickListener(v -> { actions.onLongPress(m); return true; });
        }

        private String findParentText(Long parentId) {
            if (parentId == null) return null;
            for (MessageItem msg : data) {
                if (msg.getId() == parentId)
                    return msg.getContent();
            }
            return null;
        }

        private void showReactMenu(View anchor, MessageItem m) {
            PopupMenu pm = new PopupMenu(anchor.getContext(), anchor);
            MenuInflater mi = pm.getMenuInflater();
            mi.inflate(R.menu.menu_reactions, pm.getMenu());

            pm.setOnMenuItemClickListener((MenuItem i) -> {
                String reactionType = mapMenuItemToType(i.getItemId());
                Log.d(TAG, "React pressed: messageId=" + m.getId() + " type=" + reactionType);

                if (m.getReactions().containsKey(reactionType)) {
                    actions.onRemoveReact(m, reactionType);
                    m.getReactions().remove(reactionType);
                } else {
                    m.getReactions().clear(); // one per user
                    m.getReactions().put(reactionType, 1);
                    actions.onReact(m, reactionType);
                }

                notifyItemChanged(getBindingAdapterPosition());
                return true;
            });
            pm.show();
        }

        private String mapMenuItemToType(int itemId) {
            if (itemId == R.id.reaction_like) return "like";
            else if (itemId == R.id.reaction_love) return "love";
            else if (itemId == R.id.reaction_laugh) return "laugh";
            else if (itemId == R.id.reaction_wow) return "wow";
            else if (itemId == R.id.reaction_sad) return "sad";
            else if (itemId == R.id.reaction_fire) return "fire";
            else return "unknown";
        }
    }

    class LeftHolder extends BaseHolder {
        LeftHolder(@NonNull View v) {
            super(v);
            tvSender = v.findViewById(R.id.tvSender);
            tvMsg = v.findViewById(R.id.tvMsg);
            tvMeta = v.findViewById(R.id.tvMeta);
            tvReply = v.findViewById(R.id.tvReply);
            tvReacts = v.findViewById(R.id.tvReacts);
            btnReact = v.findViewById(R.id.btnReact);
            btnReply = v.findViewById(R.id.btnReply);
            imgContent = v.findViewById(R.id.imgContent);
        }
        void bind(MessageItem m) { bindCommon(m); }
    }

    class RightHolder extends BaseHolder {
        RightHolder(@NonNull View v) {
            super(v);
            // tvSender is not used for sent messages
            tvMsg = v.findViewById(R.id.tvMsg);
            tvMeta = v.findViewById(R.id.tvMeta);
            tvReply = v.findViewById(R.id.tvReply);
            tvReacts = v.findViewById(R.id.tvReacts);
            btnReact = v.findViewById(R.id.btnReact);
            btnReply = v.findViewById(R.id.btnReply);
            imgContent = v.findViewById(R.id.imgContent);
        }
        void bind(MessageItem m) { bindCommon(m); }
    }
}