package com.example.androidexample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;



/**
 * Temporary placeholder for Group Search
 * ✅ Keeps SearchPagerAdapter stable
 * ✅ Prevents crashes when tabs change
 * ✅ Easy to fill in later with real functionality
 */
public class GroupSearchFragment extends Fragment implements SearchableTab {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // TODO: Replace this with actual RecyclerView UI later
        return inflater.inflate(R.layout.fragment_group_search, container, false);
    }

    @Override
    public void refreshResults(String query) {
        // TODO: Implement real search with backend request later
        // This is just a stub so the app does not crash
    }
}
