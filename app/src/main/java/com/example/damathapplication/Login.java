package com.example.damathapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class Login extends AppCompatActivity {

    ImageButton startGameButton, backButton_login;
    EditText player1Name, player2Name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        backButton_login = findViewById(R.id.backButton_login);
        startGameButton = findViewById(R.id.startGameButton);
        player1Name = findViewById(R.id.player1Name);
        player2Name = findViewById(R.id.player2Name);

        // Set an OnClickListener for the button
        if (startGameButton != null) {
            startGameButton.setOnClickListener(v -> {
                // Create an Intent to start the MainActivity
                Intent intent = new Intent(Login.this, MainActivity.class);
                intent.putExtra("player1Name", player1Name.getText().toString());
                intent.putExtra("player2Name", player2Name.getText().toString());
                startActivity(intent);
            });
        } else {
            Toast.makeText(this, "Oops! No definitive class to find", Toast.LENGTH_SHORT).show();
        }
        backButton_login.setOnClickListener(v -> {
            startActivity(new Intent(Login.this, HomePageActivity.class));
        });
    }
}