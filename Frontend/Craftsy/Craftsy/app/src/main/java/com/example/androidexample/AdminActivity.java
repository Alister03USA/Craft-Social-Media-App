package com.example.androidexample;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";

    private long groupId;
    private String groupName;
    private String adminUsername;

    private EditText addMemberInput;
    private Button addMemberBtn;
    private RecyclerView membersRv;
    private MemberAdapter memberAdapter;
    private final List<MemberModel> members = new ArrayList<>();

    private EditText transferAdminInput;
    private Button transferAdminBtn;

    private EditText updateNameInput, updateDescInput, updateCraftInput;
    private Button updateGroupBtn, deleteGroupBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin); // create layout below

        groupId = getIntent().getLongExtra("groupId", -1);
        groupName = getIntent().getStringExtra("groupName");
        SessionManager session = SessionManager.getInstance();
        adminUsername = session.getLoggedInUsername();



        addMemberInput = findViewById(R.id.adminAddMemberInput);
        addMemberBtn = findViewById(R.id.adminAddMemberBtn);
        membersRv = findViewById(R.id.adminMembersRv);

        transferAdminInput = findViewById(R.id.adminTransferInput);
        transferAdminBtn = findViewById(R.id.adminTransferBtn);

        updateNameInput = findViewById(R.id.adminUpdateName);
        updateDescInput = findViewById(R.id.adminUpdateDesc);
        updateCraftInput = findViewById(R.id.adminUpdateCraft);
        updateGroupBtn = findViewById(R.id.adminUpdateGroupBtn);
        deleteGroupBtn = findViewById(R.id.adminDeleteGroupBtn);

        membersRv.setLayoutManager(new LinearLayoutManager(this));
        memberAdapter = new MemberAdapter(members, usernameToRemove -> confirmRemoveMember(usernameToRemove));
        membersRv.setAdapter(memberAdapter);
        ImageButton backBtn = findViewById(R.id.adminBackBtn);
        backBtn.setOnClickListener(v -> finish());



        addMemberBtn.setOnClickListener(v -> {
            String usernameToAdd = addMemberInput.getText().toString().trim();
            if (TextUtils.isEmpty(usernameToAdd)) {
                Toast.makeText(this, "Enter a username", Toast.LENGTH_SHORT).show();
                return;
            }
            addMember(usernameToAdd);
        });

        transferAdminBtn.setOnClickListener(v -> {
            String newAdmin = transferAdminInput.getText().toString().trim();
            if (TextUtils.isEmpty(newAdmin)) { Toast.makeText(this, "Enter username", Toast.LENGTH_SHORT).show(); return; }
            transferAdmin(newAdmin);
        });

        updateGroupBtn.setOnClickListener(v -> {
            JSONObject body = new JSONObject();
            try {
                if (!TextUtils.isEmpty(updateNameInput.getText())) body.put("groupName", updateNameInput.getText().toString());
                if (!TextUtils.isEmpty(updateDescInput.getText())) body.put("description", updateDescInput.getText().toString());
                if (!TextUtils.isEmpty(updateCraftInput.getText())) body.put("craft", updateCraftInput.getText().toString());
            } catch (JSONException e) { e.printStackTrace(); }
            updateGroup(body);
        });

        deleteGroupBtn.setOnClickListener(v -> confirmDeleteGroup());

        fetchMembers();
    }

    private void fetchMembers() {
        String url = BASE_URL + "/" + groupId + "/members";
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        members.clear();
                        for (int i = 0; i < response.getJSONArray("members").length(); i++) {
                            JSONObject obj = response.getJSONArray("members").getJSONObject(i);
                            long id = obj.optLong("id");
                            String username = obj.optString("username");
                            String email = obj.optString("email", "");
                            members.add(new MemberModel(id, username, email));
                        }
                        memberAdapter.notifyDataSetChanged();
                    } catch (Exception e) { Log.e("Admin", "Parse members", e); }
                },
                error -> {
                    Log.e("Admin.fetchMembers", "Error", error);
                    Toast.makeText(this, "Failed to update group", Toast.LENGTH_SHORT).show();
                });


        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void addMember(String usernameToAdd) {
        String url = BASE_URL + "/" + groupId + "/" + adminUsername + "/add-member/" + usernameToAdd;
        StringRequest req = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(this, "Member added", Toast.LENGTH_SHORT).show();
                    addMemberInput.setText("");
                    fetchMembers();
                },
                error -> {
                    Log.e("Admin.addMember", "Error", error);
                    Toast.makeText(this, "Failed to add member", Toast.LENGTH_SHORT).show();
                });
        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void confirmRemoveMember(String usernameToRemove) {
        new AlertDialog.Builder(this)
                .setTitle("Remove member")
                .setMessage("Remove " + usernameToRemove + " from group?")
                .setPositiveButton("Remove", (d, w) -> removeMember(usernameToRemove))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void removeMember(String usernameToRemove) {
        String url = BASE_URL + "/" + groupId + "/" + adminUsername + "/removeMember/" + usernameToRemove;
        StringRequest req = new StringRequest(Request.Method.DELETE, url,
                response -> {
                    Toast.makeText(this, "Member removed", Toast.LENGTH_SHORT).show();
                    fetchMembers();
                },
                error -> {
                    Log.e("Admin.removeMember", "Error", error);
                    Toast.makeText(this, "Failed to remove member", Toast.LENGTH_SHORT).show();
                });
        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void transferAdmin(String newAdmin) {
        String url = BASE_URL + "/" + adminUsername + "/" + groupId + "/transfer-admin/" + newAdmin;
        StringRequest req = new StringRequest(Request.Method.PUT, url,
                response -> {
                    Toast.makeText(this, "Admin transferred", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Log.e("Admin.transfer", "Error", error);
                    Toast.makeText(this, "Failed to transfer admin", Toast.LENGTH_SHORT).show();
                });
        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void updateGroup(JSONObject body) {
        String url = BASE_URL + "/" + adminUsername + "/update/" + groupId;
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, url, body,
                response -> {
                    Toast.makeText(this, "Group updated", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Log.e("Admin.update", "Error", error);
                    Toast.makeText(this, "Failed to update group", Toast.LENGTH_SHORT).show();
                });
        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void confirmDeleteGroup() {
        new AlertDialog.Builder(this)
                .setTitle("Delete group")
                .setMessage("Are you sure you want to delete this group? This cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> deleteGroup())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteGroup() {
        String url = BASE_URL + "/" + adminUsername + "/delete/" + groupId;
        StringRequest req = new StringRequest(Request.Method.DELETE, url,
                response -> {
                    Toast.makeText(this, "Group deleted", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    Log.e("Admin.delete", "Error", error);
                    Toast.makeText(this, "Failed to update group", Toast.LENGTH_SHORT).show();
                });
        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }



}
