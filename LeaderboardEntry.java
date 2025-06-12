package com.example.tsarbomb; // Adjust to your package name

public class LeaderboardEntry {
    private long id;
    private String playerName;
    private long disarmTime;

    public LeaderboardEntry(long id, String playerName, long disarmTime) {
        this.id = id;
        this.playerName = playerName;
        this.disarmTime = disarmTime;
    }

    public long getId() {
        return id;
    }

    public String getPlayerName() {
        return playerName;
    }

    public long getDisarmTime() {
        return disarmTime;
    }
}