package com.example.damathapplication;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private TextView player1ScoreTextView;
    private TextView player2ScoreTextView;
    private TextView turnIndicatorTextView;
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
    }

    private void initializeViews() {
        player1ScoreTextView = findViewById(R.id.player1Score);
        player2ScoreTextView = findViewById(R.id.player2Score);
        turnIndicatorTextView = findViewById(R.id.turnIndicator);
        gridBoard = findViewById(R.id.gridBoard);
    }

    private void setupPlayerInfo() {
        player1ScoreTextView.setText("Player 1: 0");
        player2ScoreTextView.setText("Player 2: 0");
        turnIndicatorTextView.setText("Player 1's Turn");
    }

    private void setupGameBoard() {
        // Calculate tile size based on screen width
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int totalMarginPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 32, displayMetrics); // 16dp start + 16dp end
        int tileSizePx = (screenWidth - totalMarginPx) / 10;

        // Clear any existing views in the grid
        gridBoard.removeAllViews();

        // Initialize random generator for operator tiles
        Random random = new Random();
        int[][] operatorIndices = new int[8][8]; // To store operator indices for adjacency check

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                ImageView tile = new ImageView(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = tileSizePx;
                params.height = tileSizePx;
                tile.setLayoutParams(params);
                tile.setScaleType(ImageView.ScaleType.FIT_CENTER);

                if ((row == 0 && col == 0) || (row == 0 && col == 9) ||
                        (row == 9 && col == 0) || (row == 9 && col == 9)) {
                    // Four corners
                    tile.setImageResource(R.drawable.tile_white);
                } else if (row == 0 && col > 0 && col < 9) {
                    // Top number labels
                    tile.setImageResource(numberTiles[col - 1]);
                } else if (col == 0 && row > 0 && row < 9) {
                    // Left number labels
                    tile.setImageResource(numberTiles[row - 1]);
                } else if (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                    // Game area
                    int gameRow = row - 1;
                    int gameCol = col - 1;
                    if (gameRow % 2 == 0 && gameCol % 2 == 0) {
                        // Even row and column: black tile
                        tile.setImageResource(R.drawable.tile_black);
                    } else {
                        // Operator tile with no adjacent duplicates
                        int operatorIndex;
                        int attempts = 0;
                        do {
                            operatorIndex = random.nextInt(operatorTiles.length);
                            attempts++;
                        } while (hasAdjacentSameOperator(operatorIndices, gameRow, gameCol, operatorIndex) && attempts < 10);
                        operatorIndices[gameRow][gameCol] = operatorIndex;
                        tile.setImageResource(operatorTiles[operatorIndex]);
                    }
                } else {
                    // Remaining positions (if any)
                    tile.setImageResource(R.drawable.tile_black);
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
        if (row > 0 && operatorIndices[row - 1][col] == operatorIndex) {
            return true;
        }
        return false;
    }
}
