package com.example.tsarbomb; // AJUSTE PARA O NOME DO SEU PACOTE

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextView tvTimer;
    private ImageView ivTask1Status, ivTask2Status, ivTask3Status, ivTask4Status; // Adicionado ivTask4Status
    private Button btnAction;
    private View vRedAlertOverlay;

    private LinearLayout llTask1;
    private TextView tvTask1Instructions;
    private LinearLayout llColorSequenceDisplay;
    private Button btnColorRed, btnColorGreen, btnColorBlue;
    private List<Integer> correctColorSequence;
    private List<Integer> playerColorSequence;
    private static final int SEQUENCE_COLORS_COUNT = 3;
    private static final int SEQUENCE_LENGTH = 4;
    private static final int FLASH_INTERVAL_MS = 800;
    private Handler handler = new Handler();
    private Runnable playSequenceRunnable;
    private int currentSequenceIndex;

    private LinearLayout llTask2;
    private TextView tvTask2Instructions;
    private TextView tvMathProblem;
    private EditText etMathAnswer;
    private Button btnSubmitMathAnswer;
    private int mathAnswer;
    private static final int MATH_MAX_NUM = 20;
    private static final boolean INCLUDE_MULT_DIV = true;

    private LinearLayout llTask3;
    private TextView tvTask3Instructions;
    private TextView tvWireHint;
    private LinearLayout llWiresContainer;
    private List<Button> wireButtons;
    private int correctWireIndex;
    private static final int NUMBER_OF_WIRES = 3;
    private int[] wireOriginalColors = new int[NUMBER_OF_WIRES];

    private LinearLayout llTask4; // Adicionado llTask4
    private TextView tvTask4Instructions; // Adicionado tvTask4Instructions
    private Button btnPattern1, btnPattern2, btnPattern3, btnPattern4, btnPattern5, btnPattern6, btnPattern7, btnPattern8, btnPattern9; // Adicionados botões do padrão
    private List<Button> patternButtons; // Lista para os botões do padrão
    private List<Integer> correctPatternSequence;
    private List<Integer> playerPatternSequence;
    private static final int PATTERN_GRID_SIZE = 3;
    private static final int PATTERN_SEQUENCE_LENGTH = 5;
    private int currentPatternIndex;
    private Runnable playPatternSequenceRunnable;

    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = 40000;
    private boolean gameStarted = false;
    private boolean bombDisarmed = false;

    private boolean[] tasksCompleted = new boolean[4]; // Atualizado para 4 tarefas
    private int currentTaskIndex;
    private static final long PENALTY_TIME_MS = 3000;

    private Vibrator vibrator;
    private Random random = new Random();

    private DatabaseReference mDatabase;

    public static class LeaderboardEntry {
        public String name;
        public long time;

        public LeaderboardEntry() {
            // Default constructor required for calls to DataSnapshot.getValue(LeaderboardEntry.class)
        }

        public LeaderboardEntry(String name, long time) {
            this.name = name;
            this.time = time;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTimer = findViewById(R.id.tvTimer);
        ivTask1Status = findViewById(R.id.ivTask1Status);
        ivTask2Status = findViewById(R.id.ivTask2Status);
        ivTask3Status = findViewById(R.id.ivTask3Status);
        ivTask4Status = findViewById(R.id.ivTask4Status); // Encontrado ivTask4Status
        btnAction = findViewById(R.id.btnAction);
        vRedAlertOverlay = findViewById(R.id.vRedAlertOverlay);

        llTask1 = findViewById(R.id.llTask1);
        tvTask1Instructions = findViewById(R.id.tvTask1Instructions);
        llColorSequenceDisplay = findViewById(R.id.llColorSequenceDisplay);
        btnColorRed = findViewById(R.id.btnColorRed);
        btnColorGreen = findViewById(R.id.btnColorGreen);
        btnColorBlue = findViewById(R.id.btnColorBlue);

        btnColorRed.setTag(Color.RED);
        btnColorGreen.setTag(Color.GREEN);
        btnColorBlue.setTag(Color.BLUE);

        llTask2 = findViewById(R.id.llTask2);
        tvTask2Instructions = findViewById(R.id.tvTask2Instructions);
        tvMathProblem = findViewById(R.id.tvMathProblem);
        etMathAnswer = findViewById(R.id.etMathAnswer);
        btnSubmitMathAnswer = findViewById(R.id.btnSubmitMathAnswer);

        llTask3 = findViewById(R.id.llTask3);
        tvTask3Instructions = findViewById(R.id.tvTask3Instructions);
        tvWireHint = findViewById(R.id.tvWireHint);
        llWiresContainer = findViewById(R.id.llWiresContainer);

        wireButtons = new ArrayList<>();
        wireButtons.add(findViewById(R.id.btnWire1));
        wireButtons.add(findViewById(R.id.btnWire2));
        wireButtons.add(findViewById(R.id.btnWire3));


        wireOriginalColors[0] = ContextCompat.getColor(this, R.color.white);
        wireOriginalColors[1] = ContextCompat.getColor(this, R.color.white);
        wireOriginalColors[2] = ContextCompat.getColor(this, R.color.white);

        llTask4 = findViewById(R.id.llTask4); // Encontrado llTask4
        tvTask4Instructions = findViewById(R.id.tvTask4Instructions); // Encontrado tvTask4Instructions
        btnPattern1 = findViewById(R.id.btnPattern1); // Encontrado btnPattern1
        btnPattern2 = findViewById(R.id.btnPattern2); // Encontrado btnPattern2
        btnPattern3 = findViewById(R.id.btnPattern3); // Encontrado btnPattern3
        btnPattern4 = findViewById(R.id.btnPattern4); // Encontrado btnPattern4
        btnPattern5 = findViewById(R.id.btnPattern5); // Encontrado btnPattern5
        btnPattern6 = findViewById(R.id.btnPattern6); // Encontrado btnPattern6
        btnPattern7 = findViewById(R.id.btnPattern7); // Encontrado btnPattern7
        btnPattern8 = findViewById(R.id.btnPattern8); // Encontrado btnPattern8
        btnPattern9 = findViewById(R.id.btnPattern9); // Encontrado btnPattern9

        patternButtons = Arrays.asList(btnPattern1, btnPattern2, btnPattern3, btnPattern4, btnPattern5, btnPattern6, btnPattern7, btnPattern8, btnPattern9);
        for (Button button : patternButtons) {
            button.setOnClickListener(v -> onPatternButtonClick((Button) v));
            button.setTag(patternButtons.indexOf(button)); // Define um tag com o índice do botão
        }

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        hideAllTasks();
        updateTaskStatusIndicators();
        updateTimerText();
        vRedAlertOverlay.setVisibility(View.GONE);

        btnAction.setText("Iniciar Missão!");
        btnAction.setOnClickListener(v -> startGame());

        View.OnClickListener colorButtonListener = v -> onColorButtonClick((Button) v);
        btnColorRed.setOnClickListener(colorButtonListener);
        btnColorGreen.setOnClickListener(colorButtonListener);
        btnColorBlue.setOnClickListener(colorButtonListener);

        btnSubmitMathAnswer.setOnClickListener(v -> checkMathAnswer());

        View.OnClickListener wireButtonListener = v -> onWireButtonClick((Button) v);
        for (Button button : wireButtons) {
            button.setOnClickListener(wireButtonListener);
        }

        // Inicializa o Firebase Realtime Database
        mDatabase = FirebaseDatabase.getInstance().getReference();


    }

    private void startGame() {
        gameStarted = true;
        bombDisarmed = false;
        timeLeftInMillis = 40000;
        Arrays.fill(tasksCompleted, false);

        resetTask1();
        resetTask2();
        resetTask3();
        resetTask4(); // Reseta a Tarefa 4

        currentTaskIndex = 0;
        showCurrentTask();

        btnAction.setVisibility(View.GONE);
        updateTaskStatusIndicators();
        startTimer();
    }

    private void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerText();
                if (timeLeftInMillis < 10000) {
                    if (vRedAlertOverlay.getVisibility() == View.GONE || vRedAlertOverlay.getAnimation() == null || vRedAlertOverlay.getAnimation().hasEnded()) {
                        vRedAlertOverlay.setVisibility(View.VISIBLE);
                        Animation blink = AnimationUtils.loadAnimation(MainActivity.this, R.anim.red_alert_blink);
                        vRedAlertOverlay.startAnimation(blink);
                    }
                    if ((millisUntilFinished / 500) % 2 == 0) {
                        tvTimer.setTextColor(Color.parseColor("#FF0000"));
                    } else {
                        tvTimer.setTextColor(Color.parseColor("#FF6347"));
                    }
                } else {
                    tvTimer.setTextColor(Color.parseColor("#FF6347"));
                }
            }

            @Override
            public void onFinish() {
                onFinishGame(false);
            }
        }.start();
    }

    private void applyTimePenalty() {
        timeLeftInMillis -= PENALTY_TIME_MS;
        if (timeLeftInMillis < 0) {
            timeLeftInMillis = 0;
        }
        updateTimerText();
        Toast.makeText(this, "PENALIDADE! -" + (PENALTY_TIME_MS / 1000) + "s", Toast.LENGTH_SHORT).show();
        if (timeLeftInMillis <= 0) {
            onFinishGame(false);
        }
    }

    private void onFinishGame(boolean disarmed) {
        gameStarted = false;
        bombDisarmed = disarmed;

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        handler.removeCallbacksAndMessages(null);
        vRedAlertOverlay.setVisibility(View.GONE);
        vRedAlertOverlay.clearAnimation();

        long scoreTime = timeLeftInMillis;
        String playerName = getIntent().getStringExtra("PLAYER_NAME");
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Jogador Anônimo";
        }

        if (disarmed) {
            tvTimer.setText("DESARMADA!");
            tvTimer.setTextColor(Color.GREEN);
            handleWin(scoreTime, playerName);
        } else {
            tvTimer.setVisibility(View.GONE);
            Intent gameOverIntent = new Intent(MainActivity.this, GameOverActivity.class);
            startActivity(gameOverIntent);
            finish();
        }

        btnAction.setText("Tentar Novamente?");
        btnAction.setVisibility(View.VISIBLE);
        btnAction.setOnClickListener(v -> startGame());
    }

    private void handleWin(long disarmTime, String playerName) {
        Log.d("MainActivity", "Bomba desarmada! Tempo restante: " + disarmTime + ", Jogador: " + playerName);

        if (disarmTime > 0 && playerName != null && !playerName.isEmpty()) {
            DatabaseReference leaderboardRef = mDatabase.child("leaderboard");
            long initialTime = 40000; // Tempo inicial em milissegundos
            long elapsedTime = initialTime - disarmTime;

            leaderboardRef.push().setValue(new LeaderboardEntry(playerName, elapsedTime))
                    .addOnSuccessListener(aVoid -> {
                        Log.d("MainActivity", "Pontuação salva no Firebase com sucesso! Tempo decorrido: " + elapsedTime);
                        Intent winIntent = new Intent(MainActivity.this, WinScreenActivity.class);
                        winIntent.putExtra("ELAPSED_TIME", elapsedTime);
                        winIntent.putExtra("PLAYER_NAME", playerName);
                        startActivity(winIntent, null);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("MainActivity", "Erro ao salvar pontuação no Firebase", e);
                        Intent winIntent = new Intent(MainActivity.this, WinScreenActivity.class);
                        winIntent.putExtra("ELAPSED_TIME", elapsedTime);
                        winIntent.putExtra("PLAYER_NAME", playerName);
                        startActivity(winIntent, null);
                        finish();
                    });
        } else {
            Log.w("MainActivity", "Dados incompletos para salvar a pontuação.");
            Intent winIntent = new Intent(MainActivity.this, WinScreenActivity.class);
            winIntent.putExtra("ELAPSED_TIME", 0L);
            winIntent.putExtra("PLAYER_NAME", playerName);
            startActivity(winIntent, null);
            finish();
        }
    }

    private void updateTimerText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        tvTimer.setText(timeFormatted);
    }

    private void updateTaskStatusIndicators() {
        ivTask1Status.setImageResource(tasksCompleted[0] ? R.drawable.ic_task_completed : R.drawable.ic_task_incomplete);
        ivTask2Status.setImageResource(tasksCompleted[1] ? R.drawable.ic_task_completed : R.drawable.ic_task_incomplete);
        ivTask3Status.setImageResource(tasksCompleted[2] ? R.drawable.ic_task_completed : R.drawable.ic_task_incomplete);
        ivTask4Status.setImageResource(tasksCompleted[3] ? R.drawable.ic_task_completed : R.drawable.ic_task_incomplete); // Atualizado para 4 tarefas
    }

    private void hideAllTasks() {
        llTask1.setVisibility(View.GONE);
        llTask2.setVisibility(View.GONE);
        llTask3.setVisibility(View.GONE);
        llTask4.setVisibility(View.GONE); // Adicionado llTask4
    }

    private void showCurrentTask() {
        hideAllTasks();

        for (int i = 0; i < tasksCompleted.length; i++) {
            if (!tasksCompleted[i]) {
                currentTaskIndex = i;

                Animation fadeInAnim = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
                fadeInAnim.setDuration(500);

                switch (currentTaskIndex) {
                    case 0:
                        llTask1.setVisibility(View.VISIBLE);
                        llTask1.startAnimation(fadeInAnim);
                        handler.postDelayed(playSequenceRunnable, 500);
                        break;
                    case 1:
                        llTask2.setVisibility(View.VISIBLE);
                        llTask2.startAnimation(fadeInAnim);
                        generateMathProblem();
                        break;
                    case 2:
                        llTask3.setVisibility(View.VISIBLE);
                        llTask3.startAnimation(fadeInAnim);
                        generateWireProblem();
                        break;
                    case 3: // Nova tarefa
                        llTask4.setVisibility(View.VISIBLE);
                        llTask4.startAnimation(fadeInAnim);
                        startPatternChallenge(); // Método para iniciar o desafio do padrão
                        break;
                }
                return;
            }
        }
        onFinishGame(true);
    }

    private void completeCurrentTask() {
        tasksCompleted[currentTaskIndex] = true;
        updateTaskStatusIndicators();
        Toast.makeText(this, "Tarefa " + (currentTaskIndex + 1) + " Concluída!", Toast.LENGTH_SHORT).show();

        ImageView currentStatusIcon = null;
        if (currentTaskIndex == 0) currentStatusIcon = ivTask1Status;
        else if (currentTaskIndex == 1) currentStatusIcon = ivTask2Status;
        else if (currentTaskIndex == 2) currentStatusIcon = ivTask3Status;
        else if (currentTaskIndex == 3) currentStatusIcon = ivTask4Status; // Adicionado ivTask4Status

        if (currentStatusIcon != null) {
            Animation growShrink = AnimationUtils.loadAnimation(this, R.anim.status_icon_pulse);
            currentStatusIcon.startAnimation(growShrink);
        }

        boolean allTasksDone = true;
        for (boolean completed : tasksCompleted) {
            if (!completed) {
                allTasksDone = false;
                break;
            }
        }

        if (allTasksDone) {
            onFinishGame(true);
        } else {
            showCurrentTask();
        }
    }

    private void resetTask1() {
        playerColorSequence = new ArrayList<>();
        generateColorSequence();
        tvTask1Instructions.setText("Tarefa 1: Repita a sequência de cores!");
        llColorSequenceDisplay.removeAllViews();
        for (int i = 0; i < SEQUENCE_LENGTH; i++) {
            View colorDot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(60, 60);
            params.setMargins(10, 0, 10, 0);
            colorDot.setLayoutParams(params);
            colorDot.setBackgroundResource(R.drawable.rounded_panel_background);
            colorDot.setBackgroundColor(Color.parseColor("#424242"));
            llColorSequenceDisplay.addView(colorDot);
        }
        enableColorButtons(false);
    }

    private void generateColorSequence() {
        correctColorSequence = new ArrayList<>();
        int[] colors = {Color.RED, Color.GREEN, Color.BLUE};
        for (int i = 0; i < SEQUENCE_LENGTH; i++) {
            Log.d("MainActivity", "Sequência Tarefa 1: " + colors[random.nextInt(SEQUENCE_COLORS_COUNT)]);
            correctColorSequence.add(colors[random.nextInt(SEQUENCE_COLORS_COUNT)]);
        }

        currentSequenceIndex = 0;
        playSequenceRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentSequenceIndex < correctColorSequence.size()) {
                    View dot = llColorSequenceDisplay.getChildAt(currentSequenceIndex);
                    int originalDotColor = Color.parseColor("#424242");
                    int flashColor = correctColorSequence.get(currentSequenceIndex);
                    dot.setBackgroundColor(flashColor);
                    Animation flashAnim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.button_click_feedback);
                    dot.startAnimation(flashAnim);
                    handler.postDelayed(() -> dot.setBackgroundColor(originalDotColor), FLASH_INTERVAL_MS - 200);
                    currentSequenceIndex++;
                    handler.postDelayed(this, FLASH_INTERVAL_MS);
                } else {
                    enableColorButtons(true);
                    Toast.makeText(MainActivity.this, "Agora, repita a sequência!", Toast.LENGTH_SHORT).show();
                    playerColorSequence.clear();
                }
            }
        };
    }

    private void enableColorButtons(boolean enable) {
        btnColorRed.setEnabled(enable);
        btnColorGreen.setEnabled(enable);
        btnColorBlue.setEnabled(enable);
    }

    private void onColorButtonClick(Button clickedButton) {
        if (!gameStarted || tasksCompleted[0] || !llTask1.isShown() || !btnColorRed.isEnabled()) {
            return;
        }

        if (vibrator != null && vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(100);
            }
        }

        int clickedColor = (int) clickedButton.getTag();
        playerColorSequence.add(clickedColor);

        Animation clickAnim = AnimationUtils.loadAnimation(this, R.anim.button_click_feedback);
        clickedButton.startAnimation(clickAnim);

        int currentClickIndex = playerColorSequence.size() - 1;

        if (currentClickIndex < correctColorSequence.size()) {
            if (playerColorSequence.get(currentClickIndex).equals(correctColorSequence.get(currentClickIndex))) {
                clickedButton.setBackgroundColor(clickedColor);
                handler.postDelayed(() -> clickedButton.setBackgroundColor(clickedColor), 300);
            } else {
                clickedButton.setBackgroundColor(clickedColor);
                handler.postDelayed(() -> clickedButton.setBackgroundColor(clickedColor), 300);
                Toast.makeText(this, "Erro na sequência! Reiniciando Tarefa 1.", Toast.LENGTH_SHORT).show();
                playerColorSequence.clear();
                applyTimePenalty();
                resetTask1();
                showCurrentTask();
                return;
            }
        } else {
            clickedButton.setBackgroundColor(clickedColor);
            handler.postDelayed(() -> clickedButton.setBackgroundColor(clickedColor), 300);
            Toast.makeText(this, "Cliques demais! Reiniciando Tarefa 1.", Toast.LENGTH_SHORT).show();
            playerColorSequence.clear();
            applyTimePenalty();
            resetTask1();
            showCurrentTask();
            return;
        }

        if (playerColorSequence.size() == correctColorSequence.size()) {
            checkColorSequence();
        }
    }

    private void checkColorSequence() {
        if (playerColorSequence.equals(correctColorSequence)) {
            Toast.makeText(this, "Sequência de Cores Correta!", Toast.LENGTH_SHORT).show();
            enableColorButtons(false);
            completeCurrentTask();
        } else {
            Toast.makeText(this, "Sequência incorreta! Tente novamente na Tarefa 1!", Toast.LENGTH_SHORT).show();
            playerColorSequence.clear();
            applyTimePenalty();
            resetTask1();
            showCurrentTask();
        }
    }

    private void resetTask2() {
        generateMathProblem();
        tvTask2Instructions.setText("Tarefa 2: Resolva o problema de matemática!");
        etMathAnswer.setText("");
    }

    private void generateMathProblem() {
        int num1, num2;
        String operator;
        int opChoice = random.nextInt(INCLUDE_MULT_DIV ? 4 : 2);

        switch (opChoice) {
            case 0: // Adição
                num1 = random.nextInt(MATH_MAX_NUM) + 1;
                num2 = random.nextInt(MATH_MAX_NUM) + 1;
                operator = "+";
                mathAnswer = num1 + num2;
                break;
            case 1: // Subtração
                num1 = random.nextInt(MATH_MAX_NUM) + 1;
                num2 = random.nextInt(MATH_MAX_NUM) + 1;
                if (num1 < num2) {
                    int temp = num1;
                    num1 = num2;
                    num2 = temp;
                }
                operator = "-";
                mathAnswer = num1 - num2;
                break;
            case 2: // Multiplicação
                num1 = random.nextInt(10) + 1;
                num2 = random.nextInt(10) + 1;
                operator = "*";
                mathAnswer = num1 * num2;
                break;
            case 3: // Divisão
                num2 = random.nextInt(9) + 2;
                num1 = num2 * (random.nextInt(MATH_MAX_NUM / num2) + 1);
                operator = "/";
                mathAnswer = num1 / num2;
                break;
            default: // Fallback para adição
                num1 = random.nextInt(MATH_MAX_NUM) + 1;
                num2 = random.nextInt(MATH_MAX_NUM) + 1;
                operator = "+";
                mathAnswer = num1 + num2;
                break;
        }
        tvMathProblem.setText(num1 + " " + operator + " " + num2 + " = ?");
    }

    private void checkMathAnswer() {
        if (!gameStarted || tasksCompleted[1] || !llTask2.isShown()) {
            return;
        }

        String enteredText = etMathAnswer.getText().toString().trim();
        if (enteredText.isEmpty()) {
            Toast.makeText(this, "Por favor, digite uma resposta.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int playerAnswer = Integer.parseInt(enteredText);
            if (playerAnswer == mathAnswer) {
                Toast.makeText(this, "Problema de Matemática Correto!", Toast.LENGTH_SHORT).show();
                completeCurrentTask();
            } else {
                Toast.makeText(this, "Resposta incorreta! Tente novamente na Tarefa 2!", Toast.LENGTH_SHORT).show();
                etMathAnswer.setText("");
                applyTimePenalty();
                generateMathProblem();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Formato de número inválido.", Toast.LENGTH_SHORT).show();
            etMathAnswer.setText("");
            applyTimePenalty();
            generateMathProblem();
        }
    }

    private void resetTask3() {
        tvTask3Instructions.setText("Tarefa 3: Cor CERTA!");
        tvWireHint.setVisibility(View.GONE);
        enableWireButtons(true);
        for (Button button : wireButtons) {
            button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.white));
        }
    }

    private void generateWireProblem() {
        tvWireHint.setVisibility(View.VISIBLE);
        enableWireButtons(true);

        if (correctColorSequence != null && correctColorSequence.size() >= 3) {
            List<Integer> wireColors = new ArrayList<>();
            List<Integer> task1Colors = correctColorSequence.subList(0, 3);

            // Garante que tenhamos 3 cores distintas
            if (new HashSet<>(task1Colors).size() == 3) {
                wireColors.addAll(task1Colors);
            } else {
                // Se não forem distintas, pega as cores padrão (vermelho, verde, azul)
                wireColors.add(Color.RED);
                wireColors.add(Color.GREEN);
                wireColors.add(Color.BLUE);
                java.util.Collections.shuffle(wireColors); // Embaralha para não ficar sempre na mesma ordem
            }

            correctWireIndex = -1;

            for (int i = 0; i < wireButtons.size(); i++) {
                if (i < wireColors.size()) {
                    wireButtons.get(i).setBackgroundColor(wireColors.get(i));
                    if (wireColors.get(i).equals(correctColorSequence.get(1))) {
                        correctWireIndex = i;
                    }
                }
            }

            if (correctWireIndex != -1) {
                String hintText = "Corte o fio com a cor que foi a SEGUNDA na sequência da Tarefa 1.";
                tvWireHint.setText("Dica: " + hintText);
            } else {
                tvWireHint.setText("Erro ao determinar o fio correto.");
            }

        } else {
            tvWireHint.setText("Erro: Sequência de cores da Tarefa 1 não gerada corretamente.");
        }
    }

    private void onWireButtonClick(Button clickedButton) {
        if (!gameStarted || tasksCompleted[2] || !llTask3.isShown()) {
            return;
        }

        int clickedWireIndex = wireButtons.indexOf(clickedButton);

        if (clickedWireIndex == correctWireIndex) {
            Toast.makeText(this, "Cor CERTO cortado! Bom trabalho!", Toast.LENGTH_SHORT).show();
            clickedButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.light_green));
            enableWireButtons(false);
            completeCurrentTask();
        } else {
            Toast.makeText(this, "Cor ERRADO! GRANDE ERRO!", Toast.LENGTH_LONG).show();
            clickedButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.red));

            if (vibrator != null && vibrator.hasVibrator()) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(500);
                }
            }

            enableWireButtons(false);
            applyTimePenalty();
            applyTimePenalty();

            handler.postDelayed(() -> {
                resetTask3();
                generateWireProblem();
            }, 1000);
        }
    }

    private void enableWireButtons(boolean enable) {
        for (Button button : wireButtons) {
            button.setEnabled(enable);
            if (enable) {
                button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.white));
            }
        }
    }

    private void resetTask4() {
        tvTask4Instructions.setText("Tarefa 4: Repita o padrão visual!");
        playerPatternSequence = new ArrayList<>();
        generatePatternSequence();
        enablePatternButtons(false);
        for (Button button : patternButtons) {
            button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray));
        }
    }

    private void generatePatternSequence() {
        correctPatternSequence = new ArrayList<>();
        for (int i = 0; i < PATTERN_SEQUENCE_LENGTH; i++) {
            correctPatternSequence.add(random.nextInt(PATTERN_GRID_SIZE * PATTERN_GRID_SIZE)); // Números de 0 a 8 para a grade 3x3
        }

        currentPatternIndex = 0;
        playPatternSequenceRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentPatternIndex < correctPatternSequence.size()) {
                    int buttonIndex = correctPatternSequence.get(currentPatternIndex);
                    if (buttonIndex >= 0 && buttonIndex < patternButtons.size()) {
                        Button button = patternButtons.get(buttonIndex);
                        int originalColor = ContextCompat.getColor(MainActivity.this, R.color.gray);
                        int flashColor = ContextCompat.getColor(MainActivity.this, R.color.yellow); // Cor de destaque
                        button.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.yellow));
                        Animation flashAnim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.button_click_feedback);
                        button.startAnimation(flashAnim);
                        handler.postDelayed(() -> button.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.gray)), FLASH_INTERVAL_MS);
                        currentPatternIndex++;
                        handler.postDelayed(this, FLASH_INTERVAL_MS + 200); // Pequeno delay entre os flashes
                    }
                } else {
                    enablePatternButtons(true);
                    Toast.makeText(MainActivity.this, "Agora, repita o padrão!", Toast.LENGTH_SHORT).show();
                    playerPatternSequence.clear();
                }
            }
        };
    }

    private void startPatternChallenge() {
        resetTask4();
        handler.postDelayed(playPatternSequenceRunnable, 500);
    }

    private void onPatternButtonClick(Button button) {
        if (!gameStarted || tasksCompleted[3] || !llTask4.isShown() || !btnPattern1.isEnabled()) {
            return;
        }

        if (vibrator != null && vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(100);
            }
        }

        int clickedIndex = (int) button.getTag();
        playerPatternSequence.add(clickedIndex);
        Animation clickAnim = AnimationUtils.loadAnimation(this, R.anim.button_click_feedback);
        button.startAnimation(clickAnim);

        if (playerPatternSequence.size() <= correctPatternSequence.size()) {
            if (playerPatternSequence.get(playerPatternSequence.size() - 1).equals(correctPatternSequence.get(playerPatternSequence.size() - 1))) {
                // Correto até agora
                button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.light_green));
                handler.postDelayed(() -> button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray)), 300);
                if (playerPatternSequence.size() == correctPatternSequence.size()) {
                    Toast.makeText(this, "Padrão Correto!", Toast.LENGTH_SHORT).show();
                    enablePatternButtons(false);
                    completeCurrentTask();
                }
            } else {
                // Incorreto
                button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.red));
                handler.postDelayed(() -> button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray)), 300);
                Toast.makeText(this, "Padrão Incorreto! Reiniciando Tarefa 4.", Toast.LENGTH_SHORT).show();
                playerPatternSequence.clear();
                applyTimePenalty();
                resetTask4();
                startPatternChallenge();
            }
        }
    }

    private void enablePatternButtons(boolean enable) {
        for (Button button : patternButtons) {
            button.setEnabled(enable);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        handler.removeCallbacksAndMessages(null);
        vRedAlertOverlay.clearAnimation();
        vRedAlertOverlay.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameStarted && !bombDisarmed && timeLeftInMillis > 0) {
            startTimer();
            if (currentTaskIndex == 0 && !tasksCompleted[0] && playSequenceRunnable != null) {
                resetTask1();
                handler.postDelayed(playSequenceRunnable, 500);
            }
            if (currentTaskIndex == 3 && !tasksCompleted[3] && playPatternSequenceRunnable != null) {
                resetTask4();
                handler.postDelayed(playPatternSequenceRunnable, 500);
            }
            if (timeLeftInMillis < 10000) {
                vRedAlertOverlay.clearAnimation();
                vRedAlertOverlay.setVisibility(View.VISIBLE);
                Animation blink = AnimationUtils.loadAnimation(MainActivity.this, R.anim.red_alert_blink);
                vRedAlertOverlay.startAnimation(blink);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        handler.removeCallbacksAndMessages(null);
        vRedAlertOverlay.clearAnimation();
    }
}