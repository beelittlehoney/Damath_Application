package com.example.damathapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class Login extends AppCompatActivity {

    ImageButton startGameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        startGameButton = findViewById(R.id.startGameButton);

        // Set an OnClickListener for the button
        if (startGameButton != null) {
            startGameButton.setOnClickListener(v -> {
                // Create an Intent to start the HomePageActivity
                Intent intent = new Intent(Login.this, HomePageActivity.class);
                startActivity(intent);
            });
        } else {
            Toast.makeText(this, "Oops! No definitive class to find", Toast.LENGTH_SHORT).show();
        }
    }
}