package com.example.minesweeper;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity {

    private float totalGems = 100f;
    private String username = "Player";
    private TextView usernameView, gemsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // Get extras
        Intent intent = getIntent();
        totalGems = intent.getFloatExtra("totalGems", 100f);
        username = intent.getStringExtra("username");

        // Bind views
        usernameView = findViewById(R.id.usernameView);
        gemsView = findViewById(R.id.gemsView);

        // Update UI
        usernameView.setText("Welcome, " + username + "!");
        gemsView.setText(String.format("Gems: %.2f 💎", totalGems));

        // Buttons
        Button easyButton = findViewById(R.id.easyButton);
        Button mediumButton = findViewById(R.id.mediumButton);
        Button hardButton = findViewById(R.id.hardButton);
        Button customButton = findViewById(R.id.customButton);

        Log.d("START", "Gems received: " + totalGems);

        easyButton.setOnClickListener(v -> startGame(1));
        mediumButton.setOnClickListener(v -> startGame(3));
        hardButton.setOnClickListener(v -> startGame(5));
        customButton.setOnClickListener(v -> showCustomInputDialog());
    }

    private void startGame(int mineCount) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("mineCount", mineCount);
        intent.putExtra("totalGems", totalGems);

        Log.d("START", "Passing username: " + username);
        startActivity(intent);
    }

    private void showCustomInputDialog() {
        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        new AlertDialog.Builder(this)
                .setTitle("Custom Mines")
                .setMessage("Enter number of mines (1-24):")
                .setView(input)
                .setPositiveButton("Start", (dialog, which) -> {
                    int count = Integer.parseInt(input.getText().toString());
                    if (count > 0 && count < 25) {
                        startGame(count);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
