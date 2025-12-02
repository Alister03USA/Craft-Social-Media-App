package com.example.androidexample;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class PointsCenterActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private PointsPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_points_center);

        tabLayout = findViewById(R.id.tabLayoutPoints);
        viewPager = findViewById(R.id.viewPagerPoints);

        pagerAdapter = new PointsPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Leaderboard");
                    break;
                case 1:
                    tab.setText("Tiers");
                    break;
                case 2:
                    tab.setText("History"); // new tab
                    break;
            }
        }).attach();
    }
}