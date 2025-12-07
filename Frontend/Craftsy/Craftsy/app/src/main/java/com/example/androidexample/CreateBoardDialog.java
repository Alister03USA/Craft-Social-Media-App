package com.example.androidexample;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

public class CreateBoardDialog extends Dialog {

    private Context context;
    private Runnable onBoardCreated;

    private String BASE_URL = "http://coms-3090-028.class.las.iastate.edu:8080";
    private String username;

    public CreateBoardDialog(Context context, Runnable onBoardCreated) {
        super(context);
        this.context = context;
        this.onBoardCreated = onBoardCreated;

        SessionManager session = SessionManager.getInstance();
        username = session.getLoggedInUsername();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_create_board);

        EditText edtName = findViewById(R.id.edtBoardName);
        EditText edtDescription = findViewById(R.id.edtBoardDescription);

        Button btnCreate = findViewById(R.id.btnCreateBoardConfirm);
        Button btnCancel = findViewById(R.id.btnCreateBoardCancel);

        btnCancel.setOnClickListener(v -> dismiss());

        btnCreate.setOnClickListener(v -> {
            try {
                JSONObject body = new JSONObject();
                body.put("boardName", edtName.getText().toString());
                body.put("description", edtDescription.getText().toString());

                String url = BASE_URL + "/board/create/" + username;

                JsonObjectRequest req = new JsonObjectRequest(
                        Request.Method.POST,
                        url,
                        body,
                        response -> {
                            dismiss();
                            onBoardCreated.run();
                        },
                        error -> System.out.println("Board creation failed")
                );

                VolleySingleton.getInstance(context).addToRequestQueue(req);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}