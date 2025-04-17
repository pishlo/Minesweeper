package com.example.minesweeper;

import android.app.AlertDialog;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.minesweeper.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int NUM_ROWS = 5;
    private static final int NUM_COLS = 5;
    private final Cell[][] cells = new Cell[NUM_ROWS][NUM_COLS];
    private final Button[][] buttons = new Button[NUM_ROWS][NUM_COLS];

    private boolean gameOver = true;
    private int safeTilesRevealed = 0;
    private int totalSafeTiles;
    private int totalMines;
    private float totalGems;
    private float currentBet = 0;
    private double multiplier = 1.0;
    private int gamesPlayed = 0;

    private String username;

    private TextView mineCounter;
    private TextView gemCounter;
    private Button cashOutButton;
    private Button changeDifficultyButton;
    private EditText betInput;
    private Button placeBetButton;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        totalGems = getIntent().getFloatExtra("totalGems", 100f);
        totalMines = getIntent().getIntExtra("mineCount", 3);
        username = getIntent().getStringExtra("username");
        totalSafeTiles = (NUM_ROWS * NUM_COLS) - totalMines;

        mineCounter = findViewById(R.id.mineCounter);
        gemCounter = findViewById(R.id.gemCounter);
        cashOutButton = findViewById(R.id.resetButton);
        betInput = findViewById(R.id.betInput);
        placeBetButton = findViewById(R.id.placeBetButton);
        changeDifficultyButton = findViewById(R.id.changeDifficultyButton);
        GridLayout gameGrid = findViewById(R.id.gameGrid);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        for (int row = 0; row < NUM_ROWS; row++) {
            for (int col = 0; col < NUM_COLS; col++) {
                Button tile = new Button(this);
                tile.setText("");
                tile.setMinHeight(0);
                tile.setMinimumHeight(0);
                tile.setMinWidth(0);
                tile.setMinimumWidth(0);
                tile.setPadding(0, 0, 0, 0);
                tile.setTextSize(16);
                // Update background and text color
                tile.setBackgroundColor(getResources().getColor(R.color.surface_light));
                tile.setTextColor(getResources().getColor(R.color.text_primary));

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 130;
                params.height = 130;
                params.setMargins(4, 4, 4, 4);
                tile.setLayoutParams(params);

                final int r = row;
                final int c = col;

                tile.setOnClickListener(v -> {
                    if (gameOver) return;

                    Cell cell = cells[r][c];
                    if (cell.isRevealed) return;

                    cell.isRevealed = true;
                    Button clickedTile = buttons[r][c];
                    clickedTile.setEnabled(false);
                    clickedTile.setBackgroundColor(getResources().getColor(R.color.surface_dark)); // Slightly darker when revealed

                    if (cell.hasMine) {
                        clickedTile.setText("\uD83D\uDCA3");
                        clickedTile.setTextColor(getResources().getColor(R.color.error_red));
                        gameOver = true;
                        revealAllMines();
                        showGameOverDialog();
                    } else {
                        clickedTile.setText("\uD83D\uDC8E");
                        clickedTile.setTextColor(getResources().getColor(R.color.gems_color)); // Gem color
                        safeTilesRevealed++;
                        updateMultiplier();

                        if (safeTilesRevealed == totalSafeTiles) {
                            gameOver = true;
                            showWinDialog();
                        }
                    }
                });

                buttons[row][col] = tile;
                gameGrid.addView(tile);
            }
        }

        placeBetButton.setOnClickListener(v -> {
            String betText = betInput.getText().toString();
            if (betText.isEmpty()) {
                Toast.makeText(this, "Enter a bet amount", Toast.LENGTH_SHORT).show();
                return;
            }

            float bet = Float.parseFloat(betText);
            if (bet <= 0 || bet > totalGems) {
                Toast.makeText(this, "Invalid bet. You have " + totalGems + " gems.", Toast.LENGTH_SHORT).show();
                return;
            }

            currentBet = bet;
            totalGems -= currentBet;
            gamesPlayed++;
            updateGemCounter();
            syncGemsToServer();
            resetBoard();
            gameOver = false;

            vibratePlaceBet();
        });

        cashOutButton.setOnClickListener(v -> {
            if (gameOver || safeTilesRevealed == 0) return;

            float reward = (float) (currentBet * multiplier);
            totalGems += reward;

            new AlertDialog.Builder(this)
                    .setTitle("Cash Out")
                    .setMessage("You revealed " + safeTilesRevealed + " gems!\nMultiplier: " + String.format("%.2f", multiplier) +
                            "\nPayout: " + String.format("%.2f", reward) + " gems")
                    .setPositiveButton("OK", (dialog, which) -> {
                        gameOver = true;
                        updateGemCounter();
                        syncGemsToServer();
                    })
                    .show();
        });

        changeDifficultyButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Change Difficulty")
                    .setMessage("You've played " + gamesPlayed + " game(s).\nGo back to difficulty selection?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        Intent intent = new Intent(this, StartActivity.class);
                        intent.putExtra("gamesPlayed", gamesPlayed);
                        intent.putExtra("totalGems", totalGems);
                        intent.putExtra("username", username);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        updateGemCounter();
        resetBoard();
    }

    private void updateMultiplier() {
        int totalTiles = NUM_ROWS * NUM_COLS;
        int unrevealedTiles = totalTiles - safeTilesRevealed;
        int mines = totalMines;

        if (unrevealedTiles <= mines) {
            multiplier = 100.0;
            return;
        }

        multiplier = (double) (totalTiles - mines) / (double) (unrevealedTiles - mines);
    }

    private void resetBoard() {
        gameOver = false;
        safeTilesRevealed = 0;
        multiplier = 1.0;

        for (int row = 0; row < NUM_ROWS; row++) {
            for (int col = 0; col < NUM_COLS; col++) {
                cells[row][col] = new Cell();
                Button tile = buttons[row][col];
                tile.setText("");
                tile.setEnabled(true);
                tile.setBackgroundColor(getResources().getColor(R.color.surface_light));
                tile.setTextColor(getResources().getColor(R.color.text_primary));
            }
        }

        int minesPlaced = 0;
        while (minesPlaced < totalMines) {
            int randRow = (int) (Math.random() * NUM_ROWS);
            int randCol = (int) (Math.random() * NUM_COLS);
            if (!cells[randRow][randCol].hasMine) {
                cells[randRow][randCol].hasMine = true;
                minesPlaced++;
            }
        }

        mineCounter.setText("Mines: " + totalMines);
    }

    private void revealAllMines() {
        for (int row = 0; row < NUM_ROWS; row++) {
            for (int col = 0; col < NUM_COLS; col++) {
                Cell cell = cells[row][col];
                Button tile = buttons[row][col];
                tile.setEnabled(false);
                if (cell.hasMine) {
                    tile.setText("\uD83D\uDCA3");
                    tile.setTextColor(getResources().getColor(R.color.error_red)); // Make mines stand out
                }
            }
        }
    }

    private void showGameOverDialog() {
        vibrateShort();

        new AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage("You hit a mine and lost your bet of " + currentBet + " gems.")
                .setCancelable(false)
                .setPositiveButton("Try Again", (dialog, which) -> {
                    gameOver = true;
                    updateGemCounter();
                    syncGemsToServer();
                })
                .show();
    }

    private void showWinDialog() {
        float reward = (float) (currentBet * multiplier);
        totalGems += reward;

        vibrateWin();

        new AlertDialog.Builder(this)
                .setTitle("You Win! \uD83C\uDF89")
                .setMessage("You found all gems!\nMultiplier: " + String.format("%.2f", multiplier) +
                        "\nPayout: " + String.format("%.2f", reward) + " gems")
                .setCancelable(false)
                .setPositiveButton("Play Again", (dialog, which) -> {
                    gameOver = true;
                    updateGemCounter();
                    syncGemsToServer();
                })
                .show();
    }


    private void updateGemCounter() {
        gemCounter.setText(String.format("Gems: %.2f", totalGems));
    }

    private void syncGemsToServer() {
        if (username == null) return;
        User user = new User(username, "");
        user.setGems(totalGems);

        ApiClient.getApiService().updateGems(user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (!response.isSuccessful()) {
                    Log.e("UPDATE_GEMS", "Failed to sync gems: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("UPDATE_GEMS", "Error syncing gems: " + t.getMessage());
            }
        });
    }

    private void vibrateShort() {

        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null) return;

        Log.d("VIBRATION", "Triggered: Game Over (Short)");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(400);
        }
    }

    private void vibrateWin() {

        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null) return;

        Log.d("VIBRATION", "Triggered: Win (Waveform)");

        long[] pattern = {0, 200, 100, 300, 100, 400};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
        } else {
            vibrator.vibrate(pattern, -1);
        }
    }

    private void vibratePlaceBet() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null) return;

        Log.d("VIBRATION", "Triggered: Place Bet (Short)");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(100);
        }
    }
}
