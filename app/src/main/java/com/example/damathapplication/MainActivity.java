package com.example.damathapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

        // Track white tile positions for placing pieces
        List<int[]> whiteTilePositions = new ArrayList<>();
        ImageView[][] boardTiles = new ImageView[8][8];

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = tileSizePx;
                params.height = tileSizePx;

                FrameLayout tileContainer = new FrameLayout(this);
                tileContainer.setLayoutParams(params);

                ImageView tile = new ImageView(this);
                tile.setLayoutParams(new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));
                tile.setScaleType(ImageView.ScaleType.FIT_CENTER);

                if ((row == 0 || row == 9) && (col == 0 || col == 9)) {
                    tile.setImageResource(R.drawable.tile_white);
                } else if (row == 0 || row == 9) {
                    tile.setImageResource(numberTiles[col - 1]);
                } else if (col == 0 || col == 9) {
                    tile.setImageResource(numberTiles[row - 1]);
                } else {
                    int innerRow = row - 1;
                    int innerCol = col - 1;
                    if ((innerRow + innerCol) % 2 == 0) {
                        tile.setImageResource(R.drawable.tile_black);
                    } else {
                        int operatorIndex = random.nextInt(operatorTiles.length);
                        tile.setImageResource(operatorTiles[operatorIndex]);
                        whiteTilePositions.add(new int[]{innerRow, innerCol});
                    }
                    boardTiles[innerRow][innerCol] = tile;
                }

                tileContainer.addView(tile);
                gridBoard.addView(tileContainer);
            }
        }

        // Shuffle and assign 9 white tiles to each player
        Collections.shuffle(whiteTilePositions);
        List<int[]> bluePositions = whiteTilePositions.subList(0, 9);
        List<int[]> redPositions = whiteTilePositions.subList(9, 18);

        for (int i = 0; i < 9; i++) {
            int[] bluePos = bluePositions.get(i);
            int[] redPos = redPositions.get(i);

            addPieceToTile(bluePos[0], bluePos[1], "blue_piece_" + (i + 1), tileSizePx);
            addPieceToTile(redPos[0], redPos[1], "red_piece_" + (i + 1), tileSizePx);
        }
    }

    private void addPieceToTile(int row, int col, String pieceName, int tileSizePx) {
        int resId = getResources().getIdentifier(pieceName, "drawable", getPackageName());

        ImageView piece = new ImageView(this);
        piece.setLayoutParams(new FrameLayout.LayoutParams(tileSizePx, tileSizePx));
        piece.setScaleType(ImageView.ScaleType.FIT_CENTER);
        piece.setImageResource(resId);
        piece.setOnClickListener(v -> {
            // TODO: Handle piece click logic
        });

        // Find the correct index in gridBoard for the 10x10 layout
        int gridIndex = (row + 1) * 10 + (col + 1);
        FrameLayout cell = (FrameLayout) gridBoard.getChildAt(gridIndex);
        cell.addView(piece);
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
