package com.example.damathapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    private TextView player1ScoreTextView, player2ScoreTextView, turnIndicatorTextView, solutionTextView;
    private FrameLayout[][] tileContainers = new FrameLayout[8][8];
    private MediaPlayer winMusicPlayer;
    private String currentTurn;
    private String player1Name = "Player 1";
    private String player2Name = "Player 2";
    private int player1Score = 0, player2Score = 0;
    private FrameLayout selectedPieceParent = null;
    private ImageView selectedPiece = null;
    private GridLayout gridBoard;
    private int selectedRow = -1, selectedCol = -1;
    private SoundPool soundPool;
    private int moveSoundId;
    private LinearLayout moveHistoryContent1, moveHistoryContent2;
    private final int[] numberTiles = {
            R.drawable.tile_number_1, R.drawable.tile_number_2, R.drawable.tile_number_3, R.drawable.tile_number_4,
            R.drawable.tile_number_5, R.drawable.tile_number_6, R.drawable.tile_number_7, R.drawable.tile_number_8
    };
    private final int[] operatorTiles = {
            R.drawable.tile_add, R.drawable.tile_minus, R.drawable.tile_multiply, R.drawable.tile_divide
    };
    private enum StrategyType {
        NONE, KING_BOUND, CENTER_CONTROL, HIGH_VALUE_CAPTURE, ADVANTAGEOUS_TRADE
    }
    private Handler handler = new Handler();
    private final Random random = new Random();
    private final String[] operators = {"+", "-", "×", "÷"};
    private final int[][] colorSets = {
            {Color.argb(80, 255, 82, 82), Color.argb(80, 255, 215, 64)},
            {Color.argb(80, 100, 255, 218), Color.argb(80, 68, 138, 255)},
            {Color.argb(80, 179, 136, 255), Color.argb(80, 255, 128, 171)},
            {Color.argb(80, 105, 240, 174), Color.argb(80, 255, 255, 141)}
    };
    private final int[] textSizes = {36, 42, 48, 54};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupSound();
        setupPlayerInfo();
        setupGameBoard();
        setupHomeButton();
        setupMoveHistoryUI();
    }

    private void initializeViews() {
        player1ScoreTextView = findViewById(R.id.player1Score);
        player2ScoreTextView = findViewById(R.id.player2Score);
        turnIndicatorTextView = findViewById(R.id.turnIndicator);
        solutionTextView = findViewById(R.id.solutionTextView);
        gridBoard = findViewById(R.id.gridBoard);
        moveHistoryContent1 = findViewById(R.id.moveHistoryContent1);
        moveHistoryContent2 = findViewById(R.id.moveHistoryContent2);
    }

    private void setupMoveHistoryUI() {
        moveHistoryContent1.removeAllViews();
        moveHistoryContent2.removeAllViews();
    }

    private void addMoveToHistory(String color, int attackerResId, int capturedResId, int operatorResId) {
        // Get the values from the resource IDs
        int attackerValue = extractValueFromResourceId(attackerResId);
        int capturedValue = extractValueFromResourceId(capturedResId);
        int result = evaluateOperation(attackerValue, capturedValue, operatorResId);

        // Create image views for the move components
        ImageView attackerView = new ImageView(this);
        attackerView.setImageResource(attackerResId);
        attackerView.setLayoutParams(new LinearLayout.LayoutParams(80, 80));

        ImageView operatorView = new ImageView(this);
        operatorView.setImageResource(operatorResId);
        operatorView.setLayoutParams(new LinearLayout.LayoutParams(80, 80));

        ImageView capturedView = new ImageView(this);
        capturedView.setImageResource(capturedResId);
        capturedView.setLayoutParams(new LinearLayout.LayoutParams(80, 80));

        // Create equals sign view
        TextView equalsView = new TextView(this);
        equalsView.setText("=");
        equalsView.setTextSize(20);
        equalsView.setGravity(Gravity.CENTER);
        equalsView.setLayoutParams(new LinearLayout.LayoutParams(40, 80));

        // Create result view - FIXED TO SHOW FULL RESULT
        TextView resultView = new TextView(this);
        resultView.setText(String.valueOf(result));
        resultView.setTextSize(20);
        resultView.setGravity(Gravity.CENTER);
        resultView.setLayoutParams(new LinearLayout.LayoutParams(80, 80)); // Increased width

        // Create container for the move
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);

        // Add views to container
        container.addView(attackerView);
        container.addView(operatorView);
        container.addView(capturedView);
        container.addView(equalsView);
        container.addView(resultView);

        // Add to the appropriate player's history
        if ("red".equals(color)) {
            moveHistoryContent1.addView(container, 0);
        } else {
            moveHistoryContent2.addView(container, 0);
        }
    }

    // Helper method to extract value from resource ID
    private int extractValueFromResourceId(int resId) {
        String name = getResources().getResourceEntryName(resId);
        return Integer.parseInt(name.replaceAll("[^0-9]", ""));
    }

    private void setupSound() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build();

        moveSoundId = soundPool.load(this, R.raw.soundeffects, 1);
    }

    private void setupHomeButton() {
        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(view -> {
            // Create a custom dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Game Menu");

            // Inflate the custom layout
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_game_menu, null);
            builder.setView(dialogView);

            // Initialize buttons from the custom layout
            Button btnMainMenu = dialogView.findViewById(R.id.btnMainMenu);
            Button btnPlayAgain = dialogView.findViewById(R.id.btnPlayAgain);
            Button btnCancel = dialogView.findViewById(R.id.btnCancel);

            // Create and show the dialog
            AlertDialog dialog = builder.create();
            dialog.show();

            // Set click listeners for the buttons
            btnMainMenu.setOnClickListener(v -> {
                dialog.dismiss();
                startActivity(new Intent(MainActivity.this, HomePageActivity.class));
                finish();
            });

            btnPlayAgain.setOnClickListener(v -> {
                dialog.dismiss();
                recreate(); // Restart the current activity
            });

            btnCancel.setOnClickListener(v -> {
                dialog.dismiss();
            });
        });
    }

    private void setupPlayerInfo() {
        Intent intent = getIntent();
        player1Name = Optional.ofNullable(intent.getStringExtra("player1Name")).filter(s -> !s.isEmpty()).orElse("Player 1");
        player2Name = Optional.ofNullable(intent.getStringExtra("player2Name")).filter(s -> !s.isEmpty()).orElse("Player 2");

        player1ScoreTextView.setText(player1Name + ": 0");
        player2ScoreTextView.setText(player2Name + ": 0");

        currentTurn = new Random().nextBoolean() ? "red" : "blue";
        turnIndicatorTextView.setText((currentTurn.equals("red") ? player1Name : player2Name) + "'s turn");
        solutionTextView.setText("");
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
                View highlightOverlay = new View(this);
                highlightOverlay.setLayoutParams(new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));
                highlightOverlay.setBackgroundResource(0);
                highlightOverlay.setTag("highlight");

                tileContainer.addView(highlightOverlay);

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
                    if (selectedPiece != null) {
                        FrameLayout targetCell = (FrameLayout) v;

                        int targetIndex = gridBoard.indexOfChild(v);
                        int toRow = targetIndex / 10 - 1;
                        int toCol = targetIndex % 10 - 1;

                        if (toRow < 0 || toRow >= 8 || toCol < 0 || toCol >= 8) return;

                        int rowDiff = toRow - selectedRow;
                        int colDiff = toCol - selectedCol;

                        ImageView capturedPiece = null;
                        int scoreGained = 0;
                        String solutionText = "";

                        boolean isCapture = Math.abs(rowDiff) == 2 && Math.abs(colDiff) == 2;
                        boolean isPromoted = selectedPiece.getTag(R.id.promoted_tag) != null;
                        boolean isForward = ("red".equals(currentTurn) && toRow > selectedRow) ||
                                ("blue".equals(currentTurn) && toRow < selectedRow);

                        if (isCapture) {
                            if (!isPromoted && !isForward) {
                                clearHighlights();
                                selectedPiece.setAlpha(1.0f);
                                selectedPiece = null;
                                selectedPieceParent = null;
                                return;
                            }

                            int midRow = (selectedRow + toRow) / 2;
                            int midCol = (selectedCol + toCol) / 2;
                            FrameLayout midCell = tileContainers[midRow][midCol];

                            ImageView midPiece = null;
                            for (int i = 0; i < midCell.getChildCount(); i++) {
                                View child = midCell.getChildAt(i);
                                if (child instanceof ImageView && child.getTag(R.id.piece_color_tag) != null) {
                                    midPiece = (ImageView) child;
                                    break;
                                }
                            }

                            if (midPiece != null) {
                                String midColor = (String) midPiece.getTag(R.id.piece_color_tag);
                                String currentColor = (String) selectedPiece.getTag(R.id.piece_color_tag);

                                if (!midColor.equals(currentColor)) {
                                    int a = extractPieceValue(selectedPiece);
                                    int b = extractPieceValue(midPiece);

                                    ImageView tileImage = (ImageView) midCell.getChildAt(0);
                                    int opId = (Integer) tileImage.getTag(R.id.tile_type_id);

                                    String operatorSymbol = getOperatorSymbol(opId);
                                    scoreGained = evaluateOperation(a, b, opId);
                                    solutionText = a + " " + operatorSymbol + " " + b + " = " + scoreGained;

                                    if (currentColor.equals("red")) {
                                        player1Score += scoreGained;
                                        player1ScoreTextView.setText(player1Name + ": " + player1Score);
                                    } else {
                                        player2Score += scoreGained;
                                        player2ScoreTextView.setText(player2Name + ": " + player2Score);
                                    }

                                    checkWinCondition();

                                    String opponentColor = currentColor.equals("red") ? "blue" : "red";
                                    if (countPieces(opponentColor) == 0) {
                                        showWinDialog(currentColor.equals("red") ? player1Name : player2Name);
                                        return;
                                    }

                                    addMoveToHistory(currentColor,
                                            (Integer) selectedPiece.getTag(R.id.tile_type_id),
                                            (Integer) midPiece.getTag(R.id.tile_type_id),
                                            opId);

                                    midCell.removeView(midPiece);
                                    capturedPiece = midPiece;
                                }
                            }
                        }

                        boolean isRegularDiagonal = isValidDiagonalMove(selectedRow, selectedCol, toRow, toCol);

                        if ((isRegularDiagonal && (isPromoted || isForward)) || isCapture) {
                            // 🧹 Remove crown before moving
                            for (int i = 0; i < selectedPieceParent.getChildCount(); i++) {
                                View child = selectedPieceParent.getChildAt(i);
                                if ("crown".equals(child.getTag())) {
                                    selectedPieceParent.removeView(child);
                                    break;
                                }
                            }

                            selectedPieceParent.removeView(selectedPiece);
                            targetCell.addView(selectedPiece);

                            int oldSelectedRow = selectedRow;
                            int oldSelectedCol = selectedCol;

                            selectedPiece.setTag(R.id.piece_row_tag, toRow);
                            selectedPiece.setTag(R.id.piece_col_tag, toCol);

                            selectedRow = toRow;
                            selectedCol = toCol;
                            selectedPieceParent = targetCell;

                            // 👑 Promote if reaching last row
                            if (("red".equals(currentTurn) && toRow == 7) ||
                                    ("blue".equals(currentTurn) && toRow == 0)) {
                                selectedPiece.setTag(R.id.promoted_tag, true);
                            }

                            // ✅ Always re-add crown if promoted
                            if (selectedPiece.getTag(R.id.promoted_tag) != null) {
                                // Remove existing crown (defensive)
                                for (int i = 0; i < targetCell.getChildCount(); i++) {
                                    View child = targetCell.getChildAt(i);
                                    if ("crown".equals(child.getTag())) {
                                        targetCell.removeView(child);
                                        break;
                                    }
                                }

                                ImageView crown = new ImageView(this);
                                crown.setImageResource(R.drawable.crown);

                                FrameLayout.LayoutParams paramsCrown = new FrameLayout.LayoutParams(
                                        FrameLayout.LayoutParams.WRAP_CONTENT,
                                        FrameLayout.LayoutParams.WRAP_CONTENT
                                );
                                paramsCrown.gravity = Gravity.TOP | Gravity.END;
                                paramsCrown.setMargins(0, 4, 4, 0);
                                crown.setLayoutParams(paramsCrown);
                                crown.setTag("crown");

                                targetCell.addView(crown);
                            }

                            clearHighlights();

                            if (soundPool != null && moveSoundId != 0) {
                                soundPool.play(moveSoundId, 1f, 1f, 0, 0, 1f);
                            }

                            solutionTextView.setText(isCapture && !solutionText.isEmpty() ?
                                    "Last Score: " + solutionText : "");

                            StrategyType strategy = analyzeMoveStrategy(
                                    selectedPiece, toRow, toCol,
                                    oldSelectedRow, oldSelectedCol,
                                    isCapture, capturedPiece, scoreGained);

                            showStrategyPopup(strategy);
                            checkWinCondition();

                            if (isCapture && hasMoreCaptures(selectedPiece, toRow, toCol)) {
                                selectedPiece.setAlpha(0.5f);
                                highlightValidCaptures(toRow, toCol);
                            } else {
                                selectedPiece.setAlpha(1.0f);
                                selectedPiece = null;
                                selectedPieceParent = null;

                                currentTurn = currentTurn.equals("red") ? "blue" : "red";
                                turnIndicatorTextView.setText(
                                        (currentTurn.equals("red") ? player1Name : player2Name) + "'s turn");
                            }
                        }
                    }
                });

                tileContainer.addView(tile, 0);
                gridBoard.addView(tileContainer);

                if (row > 0 && row < 9 && col > 0 && col < 9) {
                    tileContainers[row - 1][col - 1] = tileContainer;
                }
            }
        }

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

        List<Integer> redPieces = new ArrayList<>();
        for (int i = 1; i <= 9; i++) redPieces.add(i);
        for (int i = 0; i < 3; i++) redPieces.add(redPieces.get(random.nextInt(9)));
        Collections.shuffle(redPieces);

        List<Integer> bluePieces = new ArrayList<>();
        for (int i = 1; i <= 9; i++) bluePieces.add(i);
        for (int i = 0; i < 3; i++) bluePieces.add(bluePieces.get(random.nextInt(9)));
        Collections.shuffle(bluePieces);

        for (int i = 0; i < 12; i++) {
            int[] pos = redPositions[i];
            addPieceToTile(pos[0], pos[1], "red_piece_" + redPieces.get(i), tileSizePx);
        }

        for (int i = 0; i < 12; i++) {
            int[] pos = bluePositions[i];
            addPieceToTile(pos[0], pos[1], "blue_piece_" + bluePieces.get(i), tileSizePx);
        }
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
            if (!pieceColor.equals(currentTurn)) return;

            if (selectedPiece != null) {
                selectedPiece.setAlpha(1.0f);
                clearHighlights();
            }

            selectedPiece = piece;
            selectedPieceParent = (FrameLayout) piece.getParent();
            selectedRow = (int) piece.getTag(R.id.piece_row_tag);
            selectedCol = (int) piece.getTag(R.id.piece_col_tag);
            selectedPiece.setAlpha(0.5f);

            highlightValidMoves(selectedRow, selectedCol);
        });

        int gridIndex = (row + 1) * 10 + (col + 1);
        FrameLayout cell = (FrameLayout) gridBoard.getChildAt(gridIndex);
        cell.addView(piece);

        piece.setTag(R.id.tile_type_id, resId);
    }

    private boolean isValidDiagonalMove(int fromRow, int fromCol, int toRow, int toCol) {
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        return rowDiff == 1 && colDiff == 1;
    }

    private void highlightValidMoves(int row, int col) {
        clearHighlights();

        String color = (String) selectedPiece.getTag(R.id.piece_color_tag);
        boolean isPromoted = selectedPiece.getTag(R.id.promoted_tag) != null;

        int[][] directions;
        if (isPromoted) {
            directions = new int[][]{{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        } else if ("red".equals(color)) {
            directions = new int[][]{{1, 1}, {1, -1}};
        } else {
            directions = new int[][]{{-1, 1}, {-1, -1}};
        }

        for (int[] dir : directions) {
            int dRow = dir[0], dCol = dir[1];
            int moveRow = row + dRow;
            int moveCol = col + dCol;

            if (isInBounds(moveRow, moveCol)) {
                FrameLayout target = tileContainers[moveRow][moveCol];
                boolean tileFree = true;
                for (int i = 0; i < target.getChildCount(); i++) {
                    View child = target.getChildAt(i);
                    if (child instanceof ImageView && child.getTag(R.id.piece_color_tag) != null) {
                        tileFree = false;
                        break;
                    }
                }
                if (tileFree) {
                    highlightTile(target, false);
                }
            }

            int midRow = row + dRow;
            int midCol = col + dCol;
            int jumpRow = row + 2 * dRow;
            int jumpCol = col + 2 * dCol;

            if (isInBounds(midRow, midCol) && isInBounds(jumpRow, jumpCol)) {
                FrameLayout midCell = tileContainers[midRow][midCol];
                FrameLayout jumpCell = tileContainers[jumpRow][jumpCol];

                ImageView midPiece = null;
                for (int i = 0; i < midCell.getChildCount(); i++) {
                    View child = midCell.getChildAt(i);
                    if (child instanceof ImageView && child.getTag(R.id.piece_color_tag) != null) {
                        midPiece = (ImageView) child;
                        break;
                    }
                }

                boolean isOpponent = midPiece != null && !((String) midPiece.getTag(R.id.piece_color_tag)).equals(color);
                boolean jumpEmpty = true;
                for (int i = 0; i < jumpCell.getChildCount(); i++) {
                    View child = jumpCell.getChildAt(i);
                    if (child instanceof ImageView && child.getTag(R.id.piece_color_tag) != null) {
                        jumpEmpty = false;
                        break;
                    }
                }

                if (isOpponent && jumpEmpty) {
                    highlightTile(jumpCell, true);
                    highlightTile(midCell, true);

                    ImageView midTileImage = (ImageView) midCell.getChildAt(0);
                    int operatorResId = (Integer) midTileImage.getTag(R.id.tile_type_id);

                    int operatorIcon = -1;
                    if (operatorResId == R.drawable.tile_add) operatorIcon = R.drawable.operator_add;
                    else if (operatorResId == R.drawable.tile_minus) operatorIcon = R.drawable.operator_minus;
                    else if (operatorResId == R.drawable.tile_multiply) operatorIcon = R.drawable.operator_multiply;
                    else if (operatorResId == R.drawable.tile_divide) operatorIcon = R.drawable.operator_divide;

                    if (operatorIcon != -1) {
                        ImageView operatorView = new ImageView(this);
                        operatorView.setImageResource(operatorIcon);

                        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.WRAP_CONTENT,
                                FrameLayout.LayoutParams.WRAP_CONTENT
                        );
                        params.topMargin = 10;
                        params.leftMargin = 50;
                        operatorView.setLayoutParams(params);
                        operatorView.setTag("temp_operator");

                        midCell.addView(operatorView);
                    }
                }
            }
        }
    }

    private void highlightValidCaptures(int row, int col) {
        String color = (String) selectedPiece.getTag(R.id.piece_color_tag);
        boolean isPromoted = selectedPiece.getTag(R.id.promoted_tag) != null;

        int[][] directions;
        if (isPromoted) {
            directions = new int[][]{{1, 1}, {1, -1}, {-1, 1}, {-1, -1}}; // all directions
        } else if ("red".equals(color)) {
            directions = new int[][]{{1, 1}, {1, -1}}; // forward only
        } else {
            directions = new int[][]{{-1, 1}, {-1, -1}}; // forward only
        }

        for (int[] dir : directions) {
            int midRow = row + dir[0];
            int midCol = col + dir[1];
            int jumpRow = row + 2 * dir[0];
            int jumpCol = col + 2 * dir[1];

            if (isInBounds(midRow, midCol) && isInBounds(jumpRow, jumpCol)) {
                FrameLayout midCell = tileContainers[midRow][midCol];
                FrameLayout jumpCell = tileContainers[jumpRow][jumpCol];

                ImageView midPiece = null;
                for (int i = 0; i < midCell.getChildCount(); i++) {
                    View child = midCell.getChildAt(i);
                    if (child instanceof ImageView && child.getTag(R.id.piece_color_tag) != null) {
                        midPiece = (ImageView) child;
                        break;
                    }
                }

                boolean isOpponent = midPiece != null &&
                        !((String) midPiece.getTag(R.id.piece_color_tag)).equals(color);
                boolean jumpEmpty = jumpCell.getChildCount() <= 2;

                if (isOpponent && jumpEmpty) {
                    highlightTile(jumpCell, true);
                }
            }
        }
    }

    private void clearHighlights() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                FrameLayout cell = tileContainers[r][c];
                if (cell != null) {
                    for (int i = 0; i < cell.getChildCount(); i++) {
                        View child = cell.getChildAt(i);
                        Object tag = child.getTag();

                        if ("highlight".equals(tag)) {
                            child.setBackgroundResource(0);
                        }

                        if ("temp_operator".equals(tag)) {
                            cell.removeView(child);
                            i--;
                        }
                    }
                }
            }
        }
    }

    private int extractPieceValue(ImageView piece) {
        int resId = (Integer) piece.getTag(R.id.tile_type_id);
        String name = getResources().getResourceEntryName(resId);
        return Integer.parseInt(name.replaceAll("[^0-9]", ""));
    }

    private int evaluateOperation(int a, int b, int operatorResId) {
        if (operatorResId == R.drawable.tile_add) return a + b;
        if (operatorResId == R.drawable.tile_minus) return a - b;
        if (operatorResId == R.drawable.tile_multiply) return a * b;
        if (operatorResId == R.drawable.tile_divide) return b != 0 ? a / b : 0;
        return 0;
    }

    private String getOperatorSymbol(int operatorResId) {
        if (operatorResId == R.drawable.tile_add) return "+";
        if (operatorResId == R.drawable.tile_minus) return "-";
        if (operatorResId == R.drawable.tile_multiply) return "x";
        if (operatorResId == R.drawable.tile_divide) return "/";
        return "?";
    }

    private boolean isInBounds(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    private void highlightTile(FrameLayout tile, boolean isCapture) {
        for (int i = 0; i < tile.getChildCount(); i++) {
            View child = tile.getChildAt(i);
            if ("highlight".equals(child.getTag())) {
                child.setBackgroundResource(isCapture ? R.drawable.tile_highlight_green : R.drawable.tile_highlight_yellow);
            }
        }
    }

    private boolean hasMoreCaptures(ImageView piece, int row, int col) {
        String color = (String) piece.getTag(R.id.piece_color_tag);
        boolean isPromoted = piece.getTag(R.id.promoted_tag) != null;

        int[][] directions;
        if (isPromoted) {
            directions = new int[][]{{1, 1}, {1, -1}, {-1, 1}, {-1, -1}}; // all directions
        } else if ("red".equals(color)) {
            directions = new int[][]{{1, 1}, {1, -1}}; // forward only
        } else {
            directions = new int[][]{{-1, 1}, {-1, -1}}; // forward only
        }

        for (int[] dir : directions) {
            int midRow = row + dir[0];
            int midCol = col + dir[1];
            int jumpRow = row + 2 * dir[0];
            int jumpCol = col + 2 * dir[1];

            if (isInBounds(midRow, midCol) && isInBounds(jumpRow, jumpCol)) {
                FrameLayout midCell = tileContainers[midRow][midCol];
                FrameLayout jumpCell = tileContainers[jumpRow][jumpCol];

                ImageView midPiece = null;
                for (int i = 0; i < midCell.getChildCount(); i++) {
                    View child = midCell.getChildAt(i);
                    if (child instanceof ImageView && child.getTag(R.id.piece_color_tag) != null) {
                        midPiece = (ImageView) child;
                        break;
                    }
                }

                boolean isOpponent = midPiece != null &&
                        !((String) midPiece.getTag(R.id.piece_color_tag)).equals(color);
                boolean jumpEmpty = jumpCell.getChildCount() <= 2;

                if (isOpponent && jumpEmpty) {
                    return true;
                }
            }
        }

        return false;
    }

    private StrategyType analyzeMoveStrategy(ImageView movedPiece, int newRow, int newCol,
                                             int oldRow, int oldCol,
                                             boolean isCapture, ImageView capturedPiece, int scoreGained) {

        String movedPieceColor = (String) movedPiece.getTag(R.id.piece_color_tag);

        if ((movedPieceColor.equals("red") && newRow == 7) ||
                (movedPieceColor.equals("blue") && newRow == 0)) {
            return StrategyType.KING_BOUND;
        }

        boolean isNewPositionCentral = (newRow >= 2 && newRow <= 5 && newCol >= 2 && newCol <= 5);
        boolean wasOldPositionNonCentral = !(oldRow >= 2 && oldRow <= 5 && oldCol >= 2 && oldCol <= 5);
        if (isNewPositionCentral && wasOldPositionNonCentral) {
            return StrategyType.CENTER_CONTROL;
        }

        if (isCapture && scoreGained > 20) {
            return StrategyType.HIGH_VALUE_CAPTURE;
        }

        if (isCapture) {
            if ((movedPieceColor.equals("red") && player1Score > player2Score) ||
                    (movedPieceColor.equals("blue") && player2Score > player1Score)) {
                return StrategyType.ADVANTAGEOUS_TRADE;
            }
        }

        return StrategyType.NONE;
    }

    private void showStrategyPopup(StrategyType strategy) {
        String message = "";
        switch (strategy) {
            case KING_BOUND:
                message = "King Bound!";
                break;
            case CENTER_CONTROL:
                message = "Center Control!";
                break;
            case HIGH_VALUE_CAPTURE:
                message = "High Value Capture!";
                break;
            case ADVANTAGEOUS_TRADE:
                message = "Advantageous Trade!";
                break;
            case NONE:
                return;
        }

        if (!message.isEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }

        if (winMusicPlayer != null) {
            winMusicPlayer.release();
            winMusicPlayer = null;
        }
    }

    private int countPieces(String color) {
        int count = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                FrameLayout cell = tileContainers[r][c];
                if (cell != null) {
                    for (int i = 0; i < cell.getChildCount(); i++) {
                        View child = cell.getChildAt(i);
                        if (child instanceof ImageView && color.equals(child.getTag(R.id.piece_color_tag))) {
                            count++;
                        }
                    }
                }
            }
        }
        return count;
    }

    private void startOperatorRain() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                createFallingOperator();
                handler.postDelayed(this, 800);
            }
        }, 500);
    }

    private void stopOperatorRain() {
        handler.removeCallbacksAndMessages(null);
    }

    private void createFallingOperator() {
        final TextView operator = new TextView(this);
        operator.setText(operators[random.nextInt(operators.length)]);
        operator.setTextSize(textSizes[random.nextInt(textSizes.length)]);

        int[] colorSet = colorSets[random.nextInt(colorSets.length)];
        operator.setTextColor(colorSet[random.nextInt(colorSet.length)]);
        operator.setAlpha(0.7f);
        operator.setElevation(1);

        ViewGroup rootView = (ViewGroup) getWindow().getDecorView().getRootView();
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rootView.addView(operator, params);

        int startX = random.nextInt(rootView.getWidth() - 300) + 150;
        operator.setX(startX);
        operator.setY(-100);

        operator.animate()
                .setDuration(1500)
                .setUpdateListener(animation -> {
                    float fraction = animation.getAnimatedFraction();
                    int newColor = blendColors(
                            colorSet[0],
                            colorSet[1],
                            (float) (0.5 + 0.5 * Math.sin(fraction * Math.PI))
                    );
                    operator.setTextColor(newColor);
                })
                .start();

        operator.animate()
                .y(rootView.getHeight())
                .rotation(random.nextFloat() * 30 - 15)
                .setDuration(5000 + random.nextInt(3000))
                .withEndAction(() -> {
                    if (operator.getParent() != null) {
                        ((ViewGroup) operator.getParent()).removeView(operator);
                    }
                })
                .start();
    }

    private int blendColors(int color1, int color2, float ratio) {
        final float inverseRatio = 1f - ratio;
        float a = (Color.alpha(color1) * inverseRatio) + (Color.alpha(color2) * ratio);
        float r = (Color.red(color1) * inverseRatio) + (Color.red(color2) * ratio);
        float g = (Color.green(color1) * inverseRatio) + (Color.green(color2) * ratio);
        float b = (Color.blue(color1) * inverseRatio) + (Color.blue(color2) * ratio);
        return Color.argb((int) a, (int) r, (int) g, (int) b);
    }

    private void showWinDialog(String winnerName) {
        // Start the win animation
        startOperatorRain();

        // Play win music
        winMusicPlayer = MediaPlayer.create(this, R.raw.backgroundmusic);
        winMusicPlayer.setLooping(true);
        winMusicPlayer.start();

        new AlertDialog.Builder(this)
                .setTitle("🎉 Game Over!")
                .setMessage(winnerName + " wins the game!")
                .setCancelable(false)
                .setPositiveButton("Restart", (dialog, which) -> {
                    stopOperatorRain();
                    if (winMusicPlayer != null) {
                        winMusicPlayer.stop();
                        winMusicPlayer.release();
                        winMusicPlayer = null;
                    }
                    new Handler().postDelayed(() -> recreate(), 200);
                })
                .setNegativeButton("Main Menu", (dialog, which) -> {
                    stopOperatorRain();
                    if (winMusicPlayer != null) {
                        winMusicPlayer.stop();
                        winMusicPlayer.release();
                        winMusicPlayer = null;
                    }
                    // Go back to HomePageActivity instead of exiting
                    startActivity(new Intent(MainActivity.this, HomePageActivity.class));
                    finish();
                })
                .show();
    }

    private void checkWinCondition() {
        if (countPieces("red") == 0) {
            showWinDialog(player2Name);
        } else if (countPieces("blue") == 0) {
            showWinDialog(player1Name);
        } else if (player1Score >= 200) {
            showWinDialog(player1Name);
        } else if (player2Score >= 200) {
            showWinDialog(player2Name);
        }
    }
}