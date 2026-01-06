package com.example.habbittracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    RecyclerView recyclerView;
    List<Habit> habitList = new ArrayList<>();
    List<Habit> allHabits = new ArrayList<>(); // فیلد کلاس
    TextView txtStreak, txtWelcome;

    int streak;
    String today;
    int todayOfWeek;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        txtWelcome = view.findViewById(R.id.txtWelcome);
        txtStreak = view.findViewById(R.id.txtStreak);
        recyclerView = view.findViewById(R.id.recyclerHabits);

        today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());
        todayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String name = (user != null && user.getDisplayName() != null)
                ? user.getDisplayName()
                : "دوست من";
        txtWelcome.setText("سلام " + name + " 👋 خوش اومدی");

        // بارگذاری همه عادت‌ها
        allHabits.clear();
        allHabits.addAll(LocalStorageHelper.loadHabits(requireContext()));

        habitList.clear();
        for (Habit h : allHabits) {
            if (h.isActiveToday(todayOfWeek)) {
                habitList.add(h);
            }
        }

        // First run defaults
        if (LocalStorageHelper.isFirstRun(requireContext())) {
            habitList.add(new Habit("No Smoking", 5, null, "09:00", false, "سلامت", ""));
            habitList.add(new Habit("Drink Water", 5, null, "09:00", false, "سلامت", ""));
            LocalStorageHelper.saveHabits(requireContext(), habitList);
            LocalStorageHelper.setFirstRunFalse(requireContext());
        }

        syncTodayState();

        streak = LocalStorageHelper.loadStreak(requireContext());

        updateStreakOncePerDay();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new HabitAdapter(habitList, today, () -> onHabitChanged()));

        updateStreakUI();
        return view;
    }

    private void syncTodayState() {
        for (Habit h : habitList) {
            h.doneToday = h.doneDates.contains(today);
        }
        LocalStorageHelper.saveHabits(requireContext(), allHabits);
    }

    private void onHabitChanged() {
        if (isAllTodayHabitsDone()) updateStreakOncePerDay();
        updateStreakUI();
    }

    private boolean isAllTodayHabitsDone() {
        for (Habit h : habitList) {
            if (h.isActiveToday(todayOfWeek) && !h.doneDates.contains(today)) return false;
        }
        return true;
    }

    private void updateStreakOncePerDay() {
        String lastDate = LocalStorageHelper.loadLastStreakDate(requireContext());
        if (!today.equals(lastDate)) {
            if (isAllTodayHabitsDone()) {
                streak++;
            } else {
                streak = 0;
            }
            LocalStorageHelper.saveStreak(requireContext(), streak);
            LocalStorageHelper.saveLastStreakDate(requireContext(), today);
        }
    }

    private void updateStreakUI() {
        txtStreak.setText(streak + " روز متوالی 🔥");
    }
}
