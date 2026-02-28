package com.example.weighttrackingapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite helper for the Weight Tracking App.
 *
 * Tables:
 *  - users(username unique, password)
 *  - daily_weights(userId, dateEntered, weightValue)
 *  - goal_weights(userId unique, goalWeightValue)
 *
 * NOTE: For the course project we store the password as plaintext.
 * In production you should store a salted hash instead.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "weight_tracker.db";
    private static final int DB_VERSION = 1;

    // Users
    public static final String T_USERS = "users";
    public static final String C_USER_ID = "userId";
    public static final String C_USERNAME = "username";
    public static final String C_PASSWORD = "password";

    // Daily weights
    public static final String T_WEIGHTS = "daily_weights";
    public static final String C_WEIGHT_ID = "weightId";
    public static final String C_DATE = "dateEntered";
    public static final String C_WEIGHT_VALUE = "weightValue";

    // Goal weights
    public static final String T_GOALS = "goal_weights";
    public static final String C_GOAL_ID = "goalId";
    public static final String C_GOAL_VALUE = "goalWeightValue";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + T_USERS + " (" +
                        C_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        C_USERNAME + " TEXT NOT NULL UNIQUE," +
                        C_PASSWORD + " TEXT NOT NULL" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE " + T_WEIGHTS + " (" +
                        C_WEIGHT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        C_USER_ID + " INTEGER NOT NULL," +
                        C_DATE + " TEXT NOT NULL," +
                        C_WEIGHT_VALUE + " REAL NOT NULL," +
                        "FOREIGN KEY(" + C_USER_ID + ") REFERENCES " + T_USERS + "(" + C_USER_ID + ")" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE " + T_GOALS + " (" +
                        C_GOAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        C_USER_ID + " INTEGER NOT NULL UNIQUE," +
                        C_GOAL_VALUE + " REAL NOT NULL," +
                        "FOREIGN KEY(" + C_USER_ID + ") REFERENCES " + T_USERS + "(" + C_USER_ID + ")" +
                        ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Simple reset strategy for course project.
        db.execSQL("DROP TABLE IF EXISTS " + T_GOALS);
        db.execSQL("DROP TABLE IF EXISTS " + T_WEIGHTS);
        db.execSQL("DROP TABLE IF EXISTS " + T_USERS);
        onCreate(db);
    }

    // -------------------------
    // Users (login + create)
    // -------------------------

    /**
     * Returns the userId if the username exists and password matches. Otherwise -1.
     */
    public long login(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(
                T_USERS,
                new String[]{C_USER_ID, C_PASSWORD},
                C_USERNAME + "=?",
                new String[]{username},
                null, null, null
        );

        long userId = -1;
        if (c.moveToFirst()) {
            String stored = c.getString(c.getColumnIndexOrThrow(C_PASSWORD));
            if (stored.equals(password)) {
                userId = c.getLong(c.getColumnIndexOrThrow(C_USER_ID));
            }
        }
        c.close();
        return userId;
    }

    /**
     * Creates a new user. Returns new userId, or -1 if username already exists.
     */
    public long createUser(String username, String password) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(C_USERNAME, username);
        cv.put(C_PASSWORD, password);

        try {
            return db.insertOrThrow(T_USERS, null, cv);
        } catch (Exception e) {
            return -1;
        }
    }

    // -------------------------
    // Goal weight
    // -------------------------

    public Double getGoalForUser(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(
                T_GOALS,
                new String[]{C_GOAL_VALUE},
                C_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null
        );

        Double goal = null;
        if (c.moveToFirst()) {
            goal = c.getDouble(c.getColumnIndexOrThrow(C_GOAL_VALUE));
        }
        c.close();
        return goal;
    }

    /**
     * Insert or update a goal weight for the user.
     */
    public boolean upsertGoal(long userId, double goalWeight) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(C_USER_ID, userId);
        cv.put(C_GOAL_VALUE, goalWeight);

        // Try update first
        int rows = db.update(
                T_GOALS,
                cv,
                C_USER_ID + "=?",
                new String[]{String.valueOf(userId)}
        );

        if (rows > 0) return true;

        // Otherwise insert
        return db.insert(T_GOALS, null, cv) != -1;
    }

    // -------------------------
    // Daily weights (CRUD)
    // -------------------------

    public long addWeight(long userId, String dateEntered, double weightValue) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(C_USER_ID, userId);
        cv.put(C_DATE, dateEntered);
        cv.put(C_WEIGHT_VALUE, weightValue);
        return db.insert(T_WEIGHTS, null, cv);
    }

    public boolean updateWeight(long weightId, double newValue) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(C_WEIGHT_VALUE, newValue);
        return db.update(
                T_WEIGHTS,
                cv,
                C_WEIGHT_ID + "=?",
                new String[]{String.valueOf(weightId)}
        ) > 0;
    }

    public boolean deleteWeight(long weightId) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(
                T_WEIGHTS,
                C_WEIGHT_ID + "=?",
                new String[]{String.valueOf(weightId)}
        ) > 0;
    }

    public List<WeightEntry> getAllWeightsForUser(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(
                T_WEIGHTS,
                new String[]{C_WEIGHT_ID, C_DATE, C_WEIGHT_VALUE},
                C_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null,
                C_DATE + " DESC"
        );

        List<WeightEntry> out = new ArrayList<>();
        while (c.moveToNext()) {
            long id = c.getLong(c.getColumnIndexOrThrow(C_WEIGHT_ID));
            String date = c.getString(c.getColumnIndexOrThrow(C_DATE));
            double val = c.getDouble(c.getColumnIndexOrThrow(C_WEIGHT_VALUE));
            out.add(new WeightEntry(id, date, val));
        }
        c.close();
        return out;
    }
}
