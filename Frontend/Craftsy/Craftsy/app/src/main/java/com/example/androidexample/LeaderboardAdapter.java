package com.example.androidexample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class LeaderboardAdapter extends BaseAdapter {

    private Context context;
    private List<LeaderboardUser> users;
    private LayoutInflater inflater;

    public LeaderboardAdapter(Context context, List<LeaderboardUser> users) {
        this.context = context;
        this.users = users;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = inflater.inflate(R.layout.item_leaderboard_user, parent, false);

        TextView usernameTv = row.findViewById(R.id.lb_username);
        TextView pointsTv = row.findViewById(R.id.lb_points);
        ImageView badgeIv = row.findViewById(R.id.lb_badge);

        LeaderboardUser user = users.get(position);

        usernameTv.setText(user.username);
        pointsTv.setText(user.totalPoints + " points");

        switch (user.tier) {
            case "BEGINNER":
                badgeIv.setImageResource(R.drawable.badge_beginner);
                break;
            case "INTERMEDIATE":
                badgeIv.setImageResource(R.drawable.badge_intermediate);
                break;
            case "EXPERT":
                badgeIv.setImageResource(R.drawable.badge_expert);
                break;
            case "CHAMPION":
                badgeIv.setImageResource(R.drawable.badge_champion);
                break;
        }

        return row;
    }
}