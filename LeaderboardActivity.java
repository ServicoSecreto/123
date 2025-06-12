package com.example.tsarbomb; // Substitua com o seu nome de pacote

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.Query;

public class LeaderboardActivity extends AppCompatActivity {

    private static final String TAG = "LeaderboardActivity"; // Adicione uma TAG
    private DatabaseReference mDatabaseLeaderboardRef; // Nome mais específico
    private ValueEventListener leaderboardListener; // Referência para o listener

    private WebView myWebView; // Declare a WebView no nível da classe

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        Log.d(TAG, "onCreate() chamado");

        myWebView = findViewById(R.id.leaderboardWebView); // Inicialize aqui
        myWebView.setWebViewClient(new WebViewClient());
        myWebView.getSettings().setJavaScriptEnabled(true);

        // Inicializa a referência para o nó 'leaderboard', mas NÃO anexe o listener aqui
        mDatabaseLeaderboardRef = FirebaseDatabase.getInstance().getReference().child("leaderboard");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() chamado - Anexando listener...");

        // --- Anexar o listener aqui ---
        leaderboardListener = mDatabaseLeaderboardRef.orderByChild("time")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange() chamado. DataSnapshot: " + dataSnapshot.toString());

                        StringBuilder htmlBuilder = new StringBuilder();
                        htmlBuilder.append("<html><head><style>");
                        htmlBuilder.append("body { font-family: Arial, sans-serif; background-color: #f2f2f2; }");
                        htmlBuilder.append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }");
                        htmlBuilder.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
                        htmlBuilder.append("th { background-color: #4CAF50; color: white; }");
                        htmlBuilder.append("tr:nth-child(even) { background-color: #f9f9f9; }");
                        htmlBuilder.append("</style></head><body>");
                        htmlBuilder.append("<h1>Placar</h1>");
                        if (!dataSnapshot.hasChildren()) {
                            htmlBuilder.append("<p>Nenhuma pontuação salva ainda.</p>");
                        } else {
                            htmlBuilder.append("<table><tr><th>#</th><th>Nome</th><th>Tempo</th></tr>"); // Adicionada coluna para o ranking
                            int rank = 1; // Inicializa o contador de ranking
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Log.d(TAG, "Processando Snapshot: " + snapshot.toString());
                                String name = snapshot.child("name").getValue(String.class);
                                Long time = snapshot.child("time").getValue(Long.class);
                                Log.d(TAG, "Nome: " + name + ", Tempo: " + time);
                                if (name != null && time != null) {
                                    long minutes = (time / 1000) / 60;
                                    long seconds = (time / 1000) % 60;
                                    String formattedTime = String.format("%02d:%02d", minutes, seconds);
                                    htmlBuilder.append("<tr><td>").append(rank).append("º</td><td>").append(name).append("</td><td>").append(formattedTime).append("</td></tr>"); // Adiciona o ranking
                                    rank++; // Incrementa o ranking
                                } else {
                                    Log.w(TAG, "Dados incompletos encontrados: " + snapshot.getKey());
                                }
                            }
                            htmlBuilder.append("</table>");
                        }
                        htmlBuilder.append("</body></html>");
                        myWebView.loadDataWithBaseURL(null, htmlBuilder.toString(), "text/html", "utf-8", null);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "Erro ao carregar o placar.", databaseError.toException());
                        myWebView.loadDataWithBaseURL(null, "<html><body><h1>Erro ao carregar o placar: " + databaseError.getMessage() + "</h1></body></html>", "text/html", "utf-8", null);
                    }
                });
        // ---------------------------------------------
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() chamado - Removendo listener...");

        // --- Remover o listener aqui ---
        if (leaderboardListener != null) {
            mDatabaseLeaderboardRef.removeEventListener(leaderboardListener);
            Log.d(TAG, "Listener removido.");
        }
        // -----------------------------
    }

    // Adicione outros métodos de ciclo de vida para melhor debug, se necessário
    @Override protected void onResume() { super.onResume(); Log.d(TAG, "onResume() chamado"); }
    @Override protected void onPause() { super.onPause(); Log.d(TAG, "onPause() chamado"); }
    @Override protected void onDestroy() { super.onDestroy(); Log.d(TAG, "onDestroy() chamado"); }

}