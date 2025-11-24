package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageButton;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import android.content.Intent;
import org.json.JSONObject;

public class BoardDetailActivity extends AppCompatActivity {

    private long boardId;
    private String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";

    private TextView txtBoardName;
    private EditText txtDescription;

    private RecyclerView rvPatterns, rvProjects, rvTutorials;

    private PatternAdapter patternAdapter;
    private FeedAdapter projectAdapter;
    private TutorialAdapter tutorialAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_detail);

        boardId = getIntent().getLongExtra("boardId", -1);

        txtBoardName = findViewById(R.id.txtBoardName);
        txtDescription = findViewById(R.id.txtDescription);

        rvPatterns = findViewById(R.id.rvPatterns);
        rvProjects = findViewById(R.id.rvProjects);
        rvTutorials = findViewById(R.id.rvTutorials);

        rvPatterns.setLayoutManager(new LinearLayoutManager(this));
        rvProjects.setLayoutManager(new LinearLayoutManager(this));
        rvTutorials.setLayoutManager(new LinearLayoutManager(this));

        Button btnSaveDescription = findViewById(R.id.btnSaveDescription);
        Button btnDeleteBoard = findViewById(R.id.btnDeleteBoard);

        btnSaveDescription.setOnClickListener(v -> saveDescription());
        btnDeleteBoard.setOnClickListener(v -> deleteBoard());
        ImageButton btnBack = findViewById(R.id.btnBackBoardDetail);
        btnBack.setOnClickListener(v -> finish());
        loadBoard();
    }

    private void loadBoard() {
        String url = BASE_URL + "/board/" + boardId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> populateBoard(response),
                error -> System.out.println("Error loading board: " + error)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void populateBoard(JSONObject obj) {
        BoardModel board = JsonParser.parseBoard(obj);

        txtBoardName.setText(board.getBoardName());
        txtDescription.setText(board.getDescription());

        // Patterns
        patternAdapter = new PatternAdapter(this, board.getPatterns());
        rvPatterns.setAdapter(patternAdapter);

        // Feed posts (projects)
        String loggedInUser = SessionManager.getInstance().getLoggedInUsername();
        projectAdapter = new FeedAdapter(this, board.getProjects(), "boards", loggedInUser);
        rvProjects.setAdapter(projectAdapter);

        // Tutorials
        tutorialAdapter = new TutorialAdapter(this, board.getTutorials(), item -> {
            // open details if needed
            Intent intent = new Intent(this, TutorialDetailActivity.class);
            intent.putExtra("id", item.getId());
            startActivity(intent);
        });
        rvTutorials.setAdapter(tutorialAdapter);
    }

    private void saveDescription() {
        String url = BASE_URL + "/board/" + boardId + "/description";

        String body = txtDescription.getText().toString();

        JsonObjectRequest request;
        try {
            request = new JsonObjectRequest(
                    Request.Method.PUT,
                    url,
                    new org.json.JSONObject().put("description", body),
                    response -> System.out.println("Updated"),
                    error -> System.out.println("Error updating description")
            );
        } catch (org.json.JSONException e) {
            System.out.println("Error creating JSON body: " + e.getMessage());
            return;
        }

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void deleteBoard() {
        String url = BASE_URL + "/board/" + boardId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                response -> finish(),
                error -> System.out.println("Error deleting board")
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}