package com.example.damathapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private TextView player1ScoreTextView;
    private TextView player2ScoreTextView;
    private TextView turnIndicatorTextView;

    private String player1Username;
    private String player2Username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        player1Username = "Player 1";
        player2Username = "Player 2";

        // Initialize TextViews
        player1ScoreTextView = findViewById(R.id.player1Score);
        player2ScoreTextView = findViewById(R.id.player2Score);
        turnIndicatorTextView = findViewById(R.id.turnIndicator);

        // Set usernames in the UI
        player1ScoreTextView.setText(player1Username + ": 0");
        player2ScoreTextView.setText(player2Username + ": 0");
        turnIndicatorTextView.setText(player1Username + "'s Turn");

        // Continue with the rest of your game initialization...
    }
}