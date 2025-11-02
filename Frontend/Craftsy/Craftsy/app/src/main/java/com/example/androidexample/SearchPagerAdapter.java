package com.example.androidexample;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class SearchPagerAdapter extends FragmentStateAdapter {

    public SearchPagerAdapter(@NonNull FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new UserSearchFragment();
            case 1: return new GroupSearchFragment();
            case 2: return new ProjectSearchFragment();
            case 3: return new TutorialSearchFragment();
            default: return new UserSearchFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}