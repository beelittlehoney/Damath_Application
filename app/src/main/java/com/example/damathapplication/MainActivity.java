package com.example.damathapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private TextView player1ScoreTextView, player2ScoreTextView, turnIndicatorTextView;
    private ImageButton homeButton;
    private GridLayout gridBoard;

    // Array of number tile resource IDs (tile_number_1 to tile_number_8)
    private final int[] numberTiles = {
            R.drawable.tile_number_1,
            R.drawable.tile_number_2,
            R.drawable.tile_number_3,
            R.drawable.tile_number_4,
            R.drawable.tile_number_5,
            R.drawable.tile_number_6,
            R.drawable.tile_number_7,
            R.drawable.tile_number_8
    };

    // Array of operator tile resource IDs
    private final int[] operatorTiles = {
            R.drawable.tile_add,
            R.drawable.tile_minus,
            R.drawable.tile_multiply,
            R.drawable.tile_divide
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupPlayerInfo();
        setupGameBoard();

        homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(view -> {
            Intent homeIntent = new Intent(MainActivity.this, HomePageActivity.class);
            startActivity(homeIntent);
            finish(); // Close the current activity
        });
    }

    private void initializeViews() {
        player1ScoreTextView = findViewById(R.id.player1Score);
        player2ScoreTextView = findViewById(R.id.player2Score);
        turnIndicatorTextView = findViewById(R.id.turnIndicator);
        gridBoard = findViewById(R.id.gridBoard);
        homeButton = findViewById(R.id.homeButton);
    }

    public void setupPlayerInfo() {
        Intent intent = getIntent();
        String player1Name = intent.getStringExtra("player1Name");
        String player2Name = intent.getStringExtra("player2Name");

        if (Objects.equals(player1Name, "")) {
            player1Name = "Player 1";
        }

        if (Objects.equals(player2Name, "")) {
            player2Name = "Player 2";
        }

        player1ScoreTextView.setText(player1Name + ": 0");
        player2ScoreTextView.setText(player2Name + ": 0");
        turnIndicatorTextView.setText(player1Name + "'s turn");
    }

    private void setupGameBoard() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int totalMarginPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 32, displayMetrics);
        int tileSizePx = (screenWidth - totalMarginPx) / 10;

        gridBoard.removeAllViews();
        Random random = new Random();

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                ImageView tile = new ImageView(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = tileSizePx;
                params.height = tileSizePx;
                tile.setLayoutParams(params);
                tile.setScaleType(ImageView.ScaleType.FIT_CENTER);

                if ((row == 0 || row == 9) && (col == 0 || col == 9)) {
                    // Corners
                    tile.setImageResource(R.drawable.tile_white);
                } else if (row == 0 || row == 9) {
                    // Top and bottom number labels
                    tile.setImageResource(numberTiles[col - 1]);
                } else if (col == 0 || col == 9) {
                    // Left and right number labels
                    tile.setImageResource(numberTiles[row - 1]);
                } else {
                    // 8x8 game board
                    int innerRow = row - 1;
                    int innerCol = col - 1;
                    if ((innerRow + innerCol) % 2 == 0) {
                        // Black tile
                        tile.setImageResource(R.drawable.tile_black);
                    } else {
                        // White tile with random operator
                        int operatorIndex = random.nextInt(operatorTiles.length);
                        tile.setImageResource(operatorTiles[operatorIndex]);
                    }
                }

                gridBoard.addView(tile);
            }
        }
    }

    private boolean hasAdjacentSameOperator(int[][] operatorIndices, int row, int col, int operatorIndex) {
        // Check left
        if (col > 0 && operatorIndices[row][col - 1] == operatorIndex) {
            return true;
        }
        // Check up
        return row > 0 && operatorIndices[row - 1][col] == operatorIndex;
    }
}
