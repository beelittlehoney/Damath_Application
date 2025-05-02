package com.example.damathapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class HomePageActivity extends AppCompatActivity {

    private ImageButton playButton;
    private ImageButton howToPlayButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        playButton = findViewById(R.id.playButton);
        howToPlayButton = findViewById(R.id.howToPlayButton);

        playButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, Login.class);
            startActivity(intent);
            finish();
        });

        howToPlayButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, HowToPlayActivity.class);
            startActivity(intent);
            finish();
        });
    }
}