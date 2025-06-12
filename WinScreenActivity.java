package com.example.tsarbomb; // Adjust to your package name

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

public class WinScreenActivity extends AppCompatActivity {

    private static final String TAG = "WinScreenActivity"; // TAG para Logs

    private long elapsedTime; // Renomeei a variável para melhor clareza
    private String playerName;
    private Button playAgainButton;
    private Button viewLeaderboardButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_win_screen);

        Log.d(TAG, "onCreate() chamado");

        playAgainButton = findViewById(R.id.play_again_button);
        viewLeaderboardButton = findViewById(R.id.viewLeaderboardButton);
        TextView timeTextView = findViewById(R.id.timeTextView);

        Intent intent = getIntent();
        elapsedTime = intent.getLongExtra("ELAPSED_TIME", 0); // Recebe o tempo decorrido
        playerName = intent.getStringExtra("PLAYER_NAME");

        long minutes = (elapsedTime / 1000) / 60;
        long seconds = (elapsedTime / 1000) % 60;
        String timeFormatted = String.format("%02d:%02d", minutes, seconds);
        timeTextView.setText("Tempo: " + timeFormatted);


        playAgainButton.setOnClickListener(v -> {
            Intent nameEntryIntent = new Intent(WinScreenActivity.this, NameEntryActivity.class);
            startActivity(nameEntryIntent);
            finish();
        });

        viewLeaderboardButton.setOnClickListener(v -> {
            Intent leaderboardIntent = new Intent(WinScreenActivity.this, LeaderboardActivity.class);
            startActivity(leaderboardIntent);
        });
    }

    @Override protected void onStart() { super.onStart(); Log.d(TAG, "onStart() chamado"); }
    @Override protected void onResume() { super.onResume(); Log.d(TAG, "onResume() chamado"); }
    @Override protected void onPause() { super.onPause(); Log.d(TAG, "onPause() chamado"); }
    @Override protected void onStop() { super.onStop(); Log.d(TAG, "onStop() chamado"); }
    @Override protected void onDestroy() { super.onDestroy(); Log.d(TAG, "onDestroy() chamado"); } // Adicionar onDestroy para debug
}