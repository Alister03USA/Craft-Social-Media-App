package com.example.androidexample;

import android.os.Bundle;
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
    private Button joinLeaveBtn;
    private boolean isMember = false;
    private String currentUsername;


    private TextView nameTv, descTv;
    private Button toggleMembersBtn;
    private RecyclerView membersRv;
    private MemberAdapter memberAdapter;
    private final List<MemberModel> members = new ArrayList<>();
    private boolean membersVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

        // Get Intent Data FIRST ✅
        groupId = getIntent().getLongExtra("groupId", -1);
        groupName = getIntent().getStringExtra("groupName");
        String groupDesc = getIntent().getStringExtra("groupDescription");

        // ✅ Setup toolbar AFTER retrieving name
        Toolbar toolbar = findViewById(R.id.groupDetailsToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(groupName);

        // Bind UI
        nameTv = findViewById(R.id.groupDetailsName);
        descTv = findViewById(R.id.groupDetailsDescription);
        toggleMembersBtn = findViewById(R.id.groupDetailsToggleMembers);
        membersRv = findViewById(R.id.groupDetailsMembersRv);

        nameTv.setText(groupName);
        descTv.setText(groupDesc != null ? groupDesc : "No description provided");

        membersRv.setLayoutManager(new LinearLayoutManager(this));
        memberAdapter = new MemberAdapter(members);
        membersRv.setAdapter(memberAdapter);
        membersRv.setVisibility(View.GONE);
        currentUsername = SessionManager.getInstance().getLoggedInUsername();
        joinLeaveBtn = findViewById(R.id.groupJoinLeaveBtn);

// Check membership on launch
        checkMembershipStatus();

        joinLeaveBtn.setOnClickListener(v -> {
            if (isMember) {
                leaveGroup();
            } else {
                joinGroup();
            }
        });


        toggleMembersBtn.setOnClickListener(v -> {
            membersVisible = !membersVisible;
            membersRv.setVisibility(membersVisible ? View.VISIBLE : View.GONE);
            toggleMembersBtn.setText(membersVisible ? "Hide Members" : "Show Members");
            if (membersVisible) fetchMembers();
        });
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
                        Log.e("GroupDetails", "Parse error", e);
                    }
                },
                error -> Log.e("GroupDetails", "Volley error", error)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
    private void checkMembershipStatus() {
        String url = BASE_URL + "/" + groupId + "/members";

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        isMember = false;
                        JSONArray arr = response.getJSONArray("members");
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject m = arr.getJSONObject(i);
                            if (currentUsername.equals(m.optString("username"))) {
                                isMember = true;
                                break;
                            }
                        }
                        updateJoinLeaveUI();
                    } catch (Exception e) {
                        Log.e("GroupDetails", "Parse membership", e);
                    }
                },
                error -> Log.e("GroupDetails", "Membership check error", error)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }
    private void updateJoinLeaveUI() {
        if (isMember) {
            joinLeaveBtn.setText("Leave Group");
        } else {
            joinLeaveBtn.setText("Request to Join");
        }
    }
    private void joinGroup() {
        String url = BASE_URL + "/" + currentUsername +  "/join/" + groupId ;

        StringRequest req = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(this, "Join request sent!", Toast.LENGTH_SHORT).show();
                    isMember = true;
                    updateJoinLeaveUI();
                },
                error -> {
                    Log.e("JoinGroup", "Error", error);
                    Toast.makeText(this, "Failed to join group", Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void leaveGroup() {
        String url = BASE_URL + "/" + currentUsername + "/" + groupId + "/leave";

        StringRequest req = new StringRequest(Request.Method.DELETE, url,
                response -> {
                    Toast.makeText(this, "Left group", Toast.LENGTH_SHORT).show();
                    isMember = false;
                    updateJoinLeaveUI();
                },
                error -> {
                    Log.e("LeaveGroup", "Error", error);
                    Toast.makeText(this, "Failed to leave group", Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

}
