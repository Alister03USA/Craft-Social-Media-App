package com.example.androidexample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONObject;

public class TierInfoFragment extends Fragment {

    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";

    private LinearLayout tierContainer;

    public TierInfoFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tier_info, container, false);

        tierContainer = view.findViewById(R.id.tierInfoContainer);

        loadTiers();

        return view;
    }

    private void loadTiers() {
        String url = BASE_URL + "/points/tiers";

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> showTierInfo(response),
                error -> Toast.makeText(getContext(), "Failed to load tier info", Toast.LENGTH_SHORT).show()
        );

        VolleySingleton.getInstance(getContext()).addToRequestQueue(req);
    }

    private void showTierInfo(JSONArray tiers) {
        tierContainer.removeAllViews();

        for (int i = 0; i < tiers.length(); i++) {
            JSONObject tier = tiers.optJSONObject(i);
            if (tier == null) continue;

            String name = tier.optString("name", "");
            int min = tier.optInt("minPoints", 0);
            int max = tier.optInt("maxPoints", 0);

            // Tier title
            TextView title = new TextView(getContext());
            title.setText(name + " : " + min + " - " + (max == Integer.MAX_VALUE ? "∞" : max));
            title.setTextSize(18f);
            title.setPadding(0, 15, 0, 5);
            title.setTypeface(null, android.graphics.Typeface.BOLD);

            tierContainer.addView(title);

            // Unlock description
            TextView desc = new TextView(getContext());
            desc.setTextSize(15f);
            desc.setPadding(20, 0, 0, 15);

            String unlocks = getUnlockInfo(name);
            desc.setText(unlocks);

            tierContainer.addView(desc);
        }
    }

    /**
     * Returns the unlock description for each tier
     */
    private String getUnlockInfo(String tierName) {
        switch (tierName.toUpperCase()) {

            case "BEGINNER":
                return "- Cannot post tutorials\n"
                        + "- Cannot view private tutorials\n"
                        + "- Cannot create groups";

            case "INTERMEDIATE":
                return "- Can view private tutorials";

            case "EXPERT":
                return "- Can upload tutorial videos/projects\n"
                        + "- Can create a group";

            case "CHAMPION":
                return "- Full access to all app features\n"
                        + "- Can create challenges";

            default:
                return "- No unlock info available";
        }
    }
}