package com.example.androidexample;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.MediaController;
import androidx.appcompat.app.AppCompatActivity;

public class TutorialDetailActivity extends AppCompatActivity {

    private TextView title, desc;
    private VideoView video;
    private ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial_detail);

        title = findViewById(R.id.detailTitle);
        desc = findViewById(R.id.detailDescription);
        video = findViewById(R.id.detailVideo);
        image = findViewById(R.id.detailImage);

        title.setText("Sample Tutorial");
        desc.setText("Shows sample video");

        String demoUrl = "https://www.w3schools.com/html/mov_bbb.mp4";
        video.setVideoURI(Uri.parse(demoUrl));
        MediaController mc = new MediaController(this);
        mc.setAnchorView(video);
        video.setMediaController(mc);
        video.start();
    }
}