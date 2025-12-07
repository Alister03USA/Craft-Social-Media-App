package com.example.androidexample;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class PointsPagerAdapter extends FragmentStateAdapter {

    public PointsPagerAdapter(@NonNull FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new LeaderboardFragment();
            case 1:
                return new TierInfoFragment();
            case 2:
                return new PointHistoryFragment();
            default:
                return new LeaderboardFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;  // Leaderboard + Tiers + Point History
    }
}