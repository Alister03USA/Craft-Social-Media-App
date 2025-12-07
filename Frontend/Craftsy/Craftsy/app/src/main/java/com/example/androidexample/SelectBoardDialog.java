package com.example.androidexample;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;

import java.util.ArrayList;
import java.util.List;

public class SelectBoardDialog extends Dialog {

    private Context context;
    private SelectBoardCallback callback;
    private RecyclerView rvBoards;

    private List<BoardModel> boardList = new ArrayList<>();
    private SelectBoardAdapter adapter;

    private String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";
    private String username;

    public interface SelectBoardCallback {
        void onBoardSelected(BoardModel board);
    }

    public SelectBoardDialog(Context context, SelectBoardCallback callback) {
        super(context);
        this.context = context;
        this.callback = callback;

        SessionManager session = SessionManager.getInstance();
        username = session.getLoggedInUsername();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_select_board);

        rvBoards = findViewById(R.id.rvDialogBoards);
        rvBoards.setLayoutManager(new LinearLayoutManager(context));

        adapter = new SelectBoardAdapter(boardList, context, callback, this);
        rvBoards.setAdapter(adapter);

        Button btnCancel = findViewById(R.id.btnCancelBoardSelect);
        btnCancel.setOnClickListener(v -> dismiss());

        loadBoards();
    }

    private void loadBoards() {
        String url = BASE_URL + "/board/user/" + username;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    boardList.clear();
                    boardList.addAll(JsonParser.parseBoards(response));
                    adapter.notifyDataSetChanged();
                },
                error -> System.out.println("Error loading boards in dialog")
        );

        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }
}