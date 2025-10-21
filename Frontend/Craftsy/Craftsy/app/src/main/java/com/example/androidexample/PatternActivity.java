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
        String url = "https://fdfe903c-6cbc-44e4-9457-0888ef0861b2.mock.pstmn.io/api/patterns";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    patterns.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            Pattern pattern = new Pattern(
                                    obj.getInt("id"),
                                    obj.getString("patternName"),
                                    obj.getString("username"),
                                    obj.getString("patternType"),
                                    (float) obj.getDouble("rating"),
                                    obj.getString("patternImage"),
                                    obj.getString("patternLink"),
                                    obj.getString("difficulty"),
                                    obj.getString("description"),
                                    obj.getString("supplies"),
                                    obj.getString("date")
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
