package com.example.androidexample;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class GroupDetailsActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";

    private long groupId;
    private String groupName;
    private boolean isMember = false;
    private boolean isPrivateGroup = true; // assume private by default (list doesn't include "private")
    private boolean requestPending = false;
    private Button joinLeaveBtn;
    private String currentUsername;

    private TextView nameTv, descTv;
    private Button toggleMembersBtn;
    private RecyclerView membersRv;
    private MemberAdapter memberAdapter;
    private final List<MemberModel> members = new ArrayList<>();
    private boolean membersVisible = false;
    private SharedPreferences prefs;

    // Polling for approval
    private final Handler pollHandler = new Handler();
    private Runnable pollRunnable;
    private static final long POLL_INTERVAL_MS = 5000; // 5 seconds
    private static final long POLL_TIMEOUT_MS = 60_000; // stop after 60 seconds
    private long pollStartTime = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

        prefs = getSharedPreferences("joinRequests", MODE_PRIVATE);

        groupId = getIntent().getLongExtra("groupId", -1);
        groupName = getIntent().getStringExtra("groupName");
        String groupDesc = getIntent().getStringExtra("groupDescription");

        // If caller had a boolean privacy flag (rare), respect it; otherwise we keep default true
        isPrivateGroup = getIntent().getBooleanExtra("isPrivate", true);

        Toolbar toolbar = findViewById(R.id.groupDetailsToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(groupName);

        nameTv = findViewById(R.id.groupDetailsName);
        descTv = findViewById(R.id.groupDetailsDescription);
        toggleMembersBtn = findViewById(R.id.groupDetailsToggleMembers);
        membersRv = findViewById(R.id.groupDetailsMembersRv);
        joinLeaveBtn = findViewById(R.id.groupJoinLeaveBtn);

        currentUsername = SessionManager.getInstance().getLoggedInUsername();

        nameTv.setText(groupName);
        descTv.setText(groupDesc != null ? groupDesc : "No description provided");

        membersRv.setLayoutManager(new LinearLayoutManager(this));
        memberAdapter = new MemberAdapter(members);
        membersRv.setAdapter(memberAdapter);
        membersRv.setVisibility(View.GONE);

        // initial membership and UI
        checkMembershipStatus();

        joinLeaveBtn.setOnClickListener(v -> {
            if (isMember) {
                leaveGroup();
            } else if (isPrivateGroup) {
                sendJoinRequest();
            } else {
                joinPublicGroup();
            }
        });

        toggleMembersBtn.setOnClickListener(v -> {
            membersVisible = !membersVisible;
            membersRv.setVisibility(membersVisible ? RecyclerView.VISIBLE : RecyclerView.GONE);
            toggleMembersBtn.setText(membersVisible ? "Hide Members" : "Show Members");
            if (membersVisible) fetchMembers();
        });
    }

    private void checkMembershipStatus() {
        String url = BASE_URL + "/" + groupId + "/members";

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        boolean found = false;
                        JSONArray arr = response.getJSONArray("members");
                        for (int i = 0; i < arr.length(); i++) {
                            if (currentUsername.equals(arr.getJSONObject(i).optString("username"))) {
                                found = true;
                                break;
                            }
                        }
                        isMember = found;
                        if (isMember) {
                            // if user is now a member, clear any pending flag
                            prefs.edit().remove(key()).apply();
                            stopPollForApproval();
                        }
                        updateJoinLeaveUI();
                    } catch (Exception e) {
                        Log.e("GroupDetails", "Parse membership", e);
                    }
                },
                error -> {
                    Log.e("GroupDetails", "Membership check error", error);
                    // Keep UI safe — if membership can't be confirmed, assume not a member
                    isMember = false;
                    updateJoinLeaveUI();
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void updateJoinLeaveUI() {
        requestPending = prefs.getBoolean(key(), requestPending);

        if (isMember) {
            joinLeaveBtn.setText("Leave Group");
            joinLeaveBtn.setEnabled(true);
            return;
        }

        if (isPrivateGroup) {
            if (requestPending) {
                joinLeaveBtn.setText("Request Sent");
                joinLeaveBtn.setEnabled(false);
            } else {
                joinLeaveBtn.setText("Request to Join");
                joinLeaveBtn.setEnabled(true);
            }
            return;
        }

        joinLeaveBtn.setText("Join Group");
        joinLeaveBtn.setEnabled(true);
    }

    private String key() {
        return groupId + "_" + currentUsername;
    }

    private void joinPublicGroup() {
        String url = BASE_URL + "/" + currentUsername + "/join/" + groupId;

        StringRequest req = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(this, "Joined group!", Toast.LENGTH_SHORT).show();
                    isMember = true;
                    prefs.edit().remove(key()).apply();
                    updateJoinLeaveUI();
                },
                error -> {
                    Log.e("JoinGroup", "Error joining public group", error);
                    Toast.makeText(this, "Join failed", Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void sendJoinRequest() {
        String url = BASE_URL + "/" + currentUsername + "/join/" + groupId;

        StringRequest req = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(this, "Join request sent!", Toast.LENGTH_SHORT).show();
                    requestPending = true;
                    prefs.edit().putBoolean(key(), true).apply();
                    updateJoinLeaveUI();

                    // Start polling members endpoint to detect admin approval
                    startPollForApproval();
                },
                error -> {
                    Log.e("JoinRequest", "Error sending join request", error);
                    Toast.makeText(this, "Request failed", Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void startPollForApproval() {
        stopPollForApproval(); // ensure no duplicate
        pollStartTime = System.currentTimeMillis();
        pollRunnable = new Runnable() {
            @Override
            public void run() {
                // stop if timed out
                if (System.currentTimeMillis() - pollStartTime > POLL_TIMEOUT_MS) {
                    stopPollForApproval();
                    return;
                }
                // check membership status (which will update UI if approved)
                checkMembershipStatus();
                // continue polling if still pending and not member
                if (!isMember && prefs.getBoolean(key(), false)) {
                    pollHandler.postDelayed(this, POLL_INTERVAL_MS);
                }
            }
        };
        pollHandler.postDelayed(pollRunnable, POLL_INTERVAL_MS);
    }

    private void stopPollForApproval() {
        if (pollRunnable != null) {
            pollHandler.removeCallbacks(pollRunnable);
            pollRunnable = null;
        }
    }

    private void leaveGroup() {
        // Backend endpoint: DELETE /{username}/leave/{groupId}
        String url = BASE_URL + "/" + currentUsername + "/leave/" + groupId;

        StringRequest req = new StringRequest(Request.Method.DELETE, url,
                response -> {
                    Toast.makeText(this, "Left group", Toast.LENGTH_SHORT).show();
                    isMember = false;
                    requestPending = false;
                    prefs.edit().remove(key()).apply();
                    updateJoinLeaveUI();
                },
                error -> {
                    Log.e("LeaveGroup", "Error leaving group", error);
                    Toast.makeText(this, "Failed to leave group", Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void fetchMembers() {
        String url = BASE_URL + "/" + groupId + "/members";

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        members.clear();
                        JSONArray arr = response.getJSONArray("members");
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject m = arr.getJSONObject(i);
                            members.add(new MemberModel(
                                    m.optLong("id"),
                                    m.optString("username"),
                                    m.optString("email", "")
                            ));
                        }
                        memberAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        Log.e("GroupDetails", "Parse members", e);
                    }
                },
                error -> Log.e("GroupDetails", "Fetch members failed", error)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // refresh membership when returning to screen (in case admin approved while user navigated away)
        checkMembershipStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPollForApproval();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
