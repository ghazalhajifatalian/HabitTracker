package com.example.habbittracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

public class NotificationHelper {

    private static final String TAG = "HabitReminder";

    public static void scheduleHabitReminder(Context context, Habit habit) {
        if (!habit.notifyEnabled || habit.time == null || habit.time.isEmpty()) return;

        String[] parts = habit.time.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        // Schedule for each active day
        for (int day : habit.activeDays) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_WEEK, day); // Sunday=1, Saturday=7
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            // If the time has already passed this week, schedule for next week
            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
            }

            Intent intent = new Intent(context, HabitReminderReceiver.class);
            intent.putExtra("habit_name", habit.name);
            intent.putExtra("habit_note", habit.note);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    habit.name.hashCode() + day, // unique per day
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                try {
                    // Log the scheduled alarm
                    Log.d(TAG, "Scheduling alarm for " + habit.name +
                            " on day " + day + " at " + hour + ":" + String.format("%02d", minute));

                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                } catch (SecurityException e) {
                    Log.e(TAG, "Cannot schedule exact alarm, permission missing!", e);
                }
            }
        }
    }
}
