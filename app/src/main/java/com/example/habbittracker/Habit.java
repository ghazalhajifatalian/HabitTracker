package com.example.habbittracker;

import java.util.HashSet;
import java.util.Set;

public class Habit {

    public String name;
    public int goal;
    public int progress;

    public boolean doneToday;

    // yyyy-MM-dd
    public Set<String> doneDates;

    // Calendar.DAY_OF_WEEK (1..7)
    public Set<Integer> activeDays;

    public boolean notifyEnabled;  // fixed: properly initialized
    public String time;            // e.g., "09:00"
    public String category;        // e.g., "Health"
    public String note;            // motivational note / optional

    // Main constructor
    public Habit(String name, int goal, Set<Integer> activeDays,
                 String time, boolean notifyEnabled, String category, String note) {
        this.name = name;
        this.goal = goal;
        this.progress = 0;
        this.doneToday = false;          // always start as not done
        this.time = time != null ? time : "09:00";
        this.notifyEnabled = notifyEnabled; // fixed here
        this.category = category != null ? category : "General";
        this.note = note != null ? note : "";
        this.activeDays = activeDays != null ? activeDays : new HashSet<>();
        if (activeDays == null || activeDays.isEmpty()) {
            for (int i = 1; i <= 7; i++) this.activeDays.add(i); // all days by default
        }
        this.doneDates = new HashSet<>();
    }

    // Default constructor for Gson / serialization
    public Habit() {
        this.name = "";
        this.goal = 0;
        this.progress = 0;
        this.doneToday = false;
        this.doneDates = new HashSet<>();
        this.activeDays = new HashSet<>();
        for (int i = 1; i <= 7; i++) this.activeDays.add(i); // all days by default
        this.notifyEnabled = false;
        this.time = "09:00";
        this.category = "کلی";
        this.note = "";
    }

    // Check if the habit is active on a specific day (Calendar.DAY_OF_WEEK: 1=Sunday,...,7=Saturday)
    public boolean isActiveToday(int dayOfWeek) {
        return activeDays.contains(dayOfWeek);
    }

    public String getActiveDaysString() {
        String[] dayNames = {"یکشنبه","دوشنبه","سه شنبه","چهارشنبه","پنحشنبه","جمعه","شنبه"};
        StringBuilder sb = new StringBuilder();
        for (int day : activeDays) {
            sb.append(dayNames[day-1]).append(" ");
        }
        return sb.toString().trim();
    }

}

