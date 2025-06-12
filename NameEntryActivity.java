package com.example.tsarbomb; // Adjust to your package name

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class NameEntryActivity extends AppCompatActivity {

    private EditText nameInput;
    private Button startGameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_entry);

        nameInput = findViewById(R.id.name_input);
        startGameButton = findViewById(R.id.start_game_button);

        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String playerName = nameInput.getText().toString().trim();

                Intent intent = new Intent(NameEntryActivity.this, MainActivity.class);
                intent.putExtra("PLAYER_NAME", playerName);
                startActivity(intent);
                finish(); // Optional: Close NameEntryActivity
            }
        });
    }
}