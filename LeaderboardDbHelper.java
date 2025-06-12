package com.example.tsarbomb; // Adjust to your package name

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "leaderboard.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_LEADERBOARD = "leaderboard";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_PLAYER_NAME = "player_name";
    private static final String COLUMN_DISARM_TIME = "disarm_time";

    private static final String SQL_CREATE_LEADERBOARD_TABLE =
            "CREATE TABLE " + TABLE_LEADERBOARD + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PLAYER_NAME + " TEXT NOT NULL, " +
                    COLUMN_DISARM_TIME + " INTEGER NOT NULL);";

    public LeaderboardDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_LEADERBOARD_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LEADERBOARD);
        onCreate(db);
    }

    public long insertScore(String playerName, long disarmTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PLAYER_NAME, playerName);
        values.put(COLUMN_DISARM_TIME, disarmTime);
        long newRowId = db.insert(TABLE_LEADERBOARD, null, values);
        db.close();
        return newRowId;
    }

    public List<LeaderboardEntry> getAllScores() {
        List<LeaderboardEntry> leaderboardEntries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {
                COLUMN_ID,
                COLUMN_PLAYER_NAME,
                COLUMN_DISARM_TIME
        };
        String sortOrder = COLUMN_DISARM_TIME + " ASC";

        Cursor cursor = db.query(
                TABLE_LEADERBOARD,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );

        if (cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndexOrThrow(COLUMN_ID);
                int nameIndex = cursor.getColumnIndexOrThrow(COLUMN_PLAYER_NAME);
                int timeIndex = cursor.getColumnIndexOrThrow(COLUMN_DISARM_TIME);
                long id = cursor.getLong(idIndex);
                String name = cursor.getString(nameIndex);
                long time = cursor.getLong(timeIndex);
                leaderboardEntries.add(new LeaderboardEntry(id, name, time));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return leaderboardEntries;
    }
}