package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class BoardsListActivity extends AppCompatActivity {

    private RecyclerView recyclerViewBoards;
    private BoardAdapter boardAdapter;
    private List<BoardModel> boardList = new ArrayList<>();

    private String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boards_list);

        SessionManager session = SessionManager.getInstance();
        username = session.getLoggedInUsername();

        recyclerViewBoards = findViewById(R.id.recyclerBoards);
        recyclerViewBoards.setLayoutManager(new LinearLayoutManager(this));

        boardAdapter = new BoardAdapter(boardList, this);
        recyclerViewBoards.setAdapter(boardAdapter);

        View btnCreateBoard = findViewById(R.id.btnCreateBoard);
        btnCreateBoard.setOnClickListener(v -> {
            CreateBoardDialog dialog = new CreateBoardDialog(BoardsListActivity.this, this::loadBoards);
            dialog.show();
        });

        loadBoards();
    }

    private void loadBoards() {
        String url = BASE_URL + "/board/user/" + username;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> updateBoardList(response),
                error -> System.out.println("Error loading boards: " + error)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void updateBoardList(JSONArray response) {
        boardList.clear();
        boardList.addAll(JsonParser.parseBoards(response));
        boardAdapter.notifyDataSetChanged();
    }
}