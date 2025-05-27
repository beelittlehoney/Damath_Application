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
    private String currentTurn = "red"; // red starts first
    private String player1Name = "Player 1";
    private String player2Name = "Player 2";
    private FrameLayout selectedPieceParent = null;
    private ImageView selectedPiece = null;
    private ImageButton homeButton;
    private GridLayout gridBoard;
    private int selectedRow = -1;
    private int selectedCol = -1;

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
            finish();
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
        player1Name = intent.getStringExtra("player1Name");
        player2Name = intent.getStringExtra("player2Name");

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
                        int operatorTileId = operatorTiles[operatorIndex];
                        tile.setImageResource(operatorTileId);
                        tile.setTag(R.id.tile_type_id, operatorTileId);
                    }
                    boardTiles[innerRow][innerCol] = tile;
                }

                tileContainer.setOnClickListener(v -> {
                    if (selectedPiece != null && tile.getDrawable() != null &&
                            isWhiteTile(tile)) {

                        int targetIndex = gridBoard.indexOfChild(v);
                        int targetRow = targetIndex / 10 - 1;
                        int targetCol = targetIndex % 10 - 1;

                        if (isValidDiagonalMove(selectedRow, selectedCol, targetRow, targetCol)) {
                            selectedPieceParent.removeView(selectedPiece);
                            ((FrameLayout) v).addView(selectedPiece);

                            selectedPiece.setTag(R.id.piece_row_tag, targetRow);
                            selectedPiece.setTag(R.id.piece_col_tag, targetCol);

                            selectedPiece.setAlpha(1.0f);
                            selectedPiece = null;
                            selectedPieceParent = null;

                            // Switch turn
                            currentTurn = currentTurn.equals("red") ? "blue" : "red";
                            turnIndicatorTextView.setText(
                                    (currentTurn.equals("red") ? player1Name : player2Name) + "'s turn");
                        }
                    }
                });

                // Important: Add tile image last so it's always behind the piece
                tileContainer.addView(tile, 0); // Add at index 0 (bottom)

                gridBoard.addView(tileContainer);
            }
        }


        // Fixed piece positions (row, col are 0-based)
        int[][] redPositions = {
            {0, 1}, {0, 3}, {0, 5}, {0, 7},
            {1, 0}, {1, 2}, {1, 4}, {1, 6},
            {2, 1}, {2, 3}, {2, 5}, {2, 7}
        };

        int[][] bluePositions = {
            {5, 0}, {5, 2}, {5, 4}, {5, 6},
            {6, 1}, {6, 3}, {6, 5}, {6, 7},
            {7, 0}, {7, 2}, {7, 4}, {7, 6}
        };

        // Generate red pieces: 1-9 + 3 random repeats
        List<Integer> redPieces = new ArrayList<>();
        for (int i = 1; i <= 9; i++) redPieces.add(i);
        for (int i = 0; i < 3; i++) redPieces.add(redPieces.get(random.nextInt(9)));
        Collections.shuffle(redPieces);

        // Generate blue pieces: 1-9 + 3 random repeats
        List<Integer> bluePieces = new ArrayList<>();
        for (int i = 1; i <= 9; i++) bluePieces.add(i);
        for (int i = 0; i < 3; i++) bluePieces.add(bluePieces.get(random.nextInt(9)));
        Collections.shuffle(bluePieces);

        // Place red pieces
        for (int i = 0; i < 12; i++) {
            int[] pos = redPositions[i];
            addPieceToTile(pos[0], pos[1], "red_piece_" + redPieces.get(i), tileSizePx);
        }

        // Place blue pieces
        for (int i = 0; i < 12; i++) {
            int[] pos = bluePositions[i];
            addPieceToTile(pos[0], pos[1], "blue_piece_" + bluePieces.get(i), tileSizePx);
        }
    }

    private boolean isWhiteTile(ImageView tile) {
        int id = (Integer) tile.getTag(R.id.tile_type_id); // fallback if we store tag
        for (int op : operatorTiles) {
            if (tile.getDrawable() != null && tile.getDrawable().getConstantState() != null &&
                    getResources().getDrawable(op).getConstantState().equals(tile.getDrawable().getConstantState())) {
                return true;
            }
        }
        return false;
    }

    private void addPieceToTile(int row, int col, String pieceName, int tileSizePx) {
        int resId = getResources().getIdentifier(pieceName, "drawable", getPackageName());
        String color = pieceName.startsWith("red") ? "red" : "blue";

        ImageView piece = new ImageView(this);
        piece.setLayoutParams(new FrameLayout.LayoutParams(tileSizePx, tileSizePx));
        piece.setScaleType(ImageView.ScaleType.FIT_CENTER);
        piece.setImageResource(resId);

        piece.setTag(R.id.piece_row_tag, row);
        piece.setTag(R.id.piece_col_tag, col);
        piece.setTag(R.id.piece_color_tag, color);

        piece.setOnClickListener(v -> {
            String pieceColor = (String) piece.getTag(R.id.piece_color_tag);
            if (!pieceColor.equals(currentTurn)) return; // Only allow turn owner's piece

            if (selectedPiece != null) {
                selectedPiece.setAlpha(1.0f);
            }
            selectedPiece = piece;
            selectedPieceParent = (FrameLayout) piece.getParent();
            selectedRow = (int) piece.getTag(R.id.piece_row_tag);
            selectedCol = (int) piece.getTag(R.id.piece_col_tag);
            selectedPiece.setAlpha(0.5f);
        });

        int gridIndex = (row + 1) * 10 + (col + 1);
        FrameLayout cell = (FrameLayout) gridBoard.getChildAt(gridIndex);
        cell.addView(piece);
    }

    private boolean isValidDiagonalMove(int fromRow, int fromCol, int toRow, int toCol) {
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        return rowDiff == 1 && colDiff == 1;
    }

    private boolean hasAdjacentSameOperator(int[][] operatorIndices, int row, int col, int operatorIndex) {
        if (col > 0 && operatorIndices[row][col - 1] == operatorIndex) {
            return true;
        }
        return row > 0 && operatorIndices[row - 1][col] == operatorIndex;
    }
}