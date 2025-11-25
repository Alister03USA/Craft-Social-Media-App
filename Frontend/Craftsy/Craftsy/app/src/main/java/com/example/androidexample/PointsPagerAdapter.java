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
        if (position == 0) {
            return new LeaderboardFragment();
        } else {
            return new TierInfoFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;  // Leaderboard + Tiers
    }
}