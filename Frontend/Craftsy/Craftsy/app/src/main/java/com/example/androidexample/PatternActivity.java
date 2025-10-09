package com.example.androidexample;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

public class PatternActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PatternAdapter adapter;
    private List<Pattern> patterns = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern);

        recyclerView = findViewById(R.id.patternsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PatternAdapter(this, patterns);
        recyclerView.setAdapter(adapter);

        fetchPatterns();
    }

    private void fetchPatterns() {
        String url = "https://8501387c-656b-4916-bfeb-16a5077c0947.mock.pstmn.io/patterns";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            Pattern pattern = new Pattern(
                                    obj.getInt("id"),
                                    obj.getString("imageUrl"),
                                    (float) obj.getDouble("rating")
                            );
                            patterns.add(pattern);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    adapter.notifyDataSetChanged();
                },
                error -> Toast.makeText(PatternActivity.this, "Error fetching patterns: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}
