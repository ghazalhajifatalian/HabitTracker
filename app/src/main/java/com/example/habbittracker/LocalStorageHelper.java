package com.example.habbittracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LocalStorageHelper {

    private static final String PREFS = "habit_prefs";

    private static final String KEY_HABITS = "habits";
    private static final String KEY_STREAK = "streak";
    private static final String KEY_LAST_DATE = "last_date";
    private static final String KEY_FIRST_RUN = "first_run";
    private static final String KEY_LAST_STREAK_DATE = "last_streak_date";

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    // ---------- Habits ----------
    public static synchronized void saveHabits(Context context, List<Habit> habits) {
        if (habits == null) return;
        String json = new Gson().toJson(habits);
        Log.d("LocalStorageHelper", "Saving habits, count=" + habits.size());
        prefs(context).edit().putString(KEY_HABITS, json).apply();
    }

    public static List<Habit> loadHabits(Context context) {
        String json = prefs(context).getString(KEY_HABITS, null);
        if (json == null) {
            Log.d("LocalStorageHelper", "No habits found in storage");
            return new ArrayList<>();
        }

        Type type = new TypeToken<List<Habit>>() {}.getType();
        List<Habit> habits = new Gson().fromJson(json, type);

        // Log all habits being loaded
        for (Habit h : habits) {
            Log.d("LocalStorageHelper", "Loaded habit: " + h.name + ", activeDays=" + h.activeDays +
                    ", time=" + h.time + ", notify=" + h.notifyEnabled);
        }

        return habits;
    }

    // ---------- First Run ----------
    public static boolean isFirstRun(Context context) {
        return prefs(context).getBoolean(KEY_FIRST_RUN, true);
    }

    public static void setFirstRunFalse(Context context) {
        prefs(context).edit().putBoolean(KEY_FIRST_RUN, false).apply();
    }

    // ---------- Streak ----------
    public static void saveStreak(Context context, int streak) {
        prefs(context).edit().putInt(KEY_STREAK, streak).apply();
    }

    public static int loadStreak(Context context) {
        return prefs(context).getInt(KEY_STREAK, 0);
    }

    // ---------- Dates ----------
    public static void saveLastDate(Context context, String date) {
        prefs(context).edit().putString(KEY_LAST_DATE, date).apply();
    }

    public static String loadLastDate(Context context) {
        return prefs(context).getString(KEY_LAST_DATE, "");
    }

    // 🔥 VERY IMPORTANT: streak date
    public static void saveLastStreakDate(Context context, String date) {
        prefs(context).edit().putString(KEY_LAST_STREAK_DATE, date).apply();
    }

    public static String loadLastStreakDate(Context context) {
        return prefs(context).getString(KEY_LAST_STREAK_DATE, "");
    }
}
