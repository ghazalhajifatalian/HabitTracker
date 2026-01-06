package com.example.habbittracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;


public class StatsFragment extends Fragment {

    TextView txtStreakCount, txtTodayProgress, txtWeekProgress;
    TextView txtBestHabit, txtWorstDay;

    List<Habit> habits;
    String today;

    BarChart barChartWeek;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        txtStreakCount = view.findViewById(R.id.txtStreakCount);
        txtTodayProgress = view.findViewById(R.id.txtTodayProgress);
        txtWeekProgress = view.findViewById(R.id.txtWeekProgress);
        txtBestHabit = view.findViewById(R.id.txtBestHabit);
        txtWorstDay = view.findViewById(R.id.txtWorstDay);

        barChartWeek = view.findViewById(R.id.barChartWeek);


        habits = LocalStorageHelper.loadHabits(requireContext());

        today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Calendar.getInstance().getTime());

        loadStats();

        return view;
    }

    private void loadStats() {
        loadStreak();
        loadTodayStats();
        loadWeekStats();
        loadBestHabit();
        loadWorstDay();
        loadWeeklyChart();

    }

    private void loadStreak() {
        int streak = LocalStorageHelper.loadStreak(requireContext());
        txtStreakCount.setText(streak + " 🔥");
    }

    private void loadTodayStats() {
        int total = 0;
        int done = 0;

        for (Habit h : habits) {
            if (h.isActiveToday(Calendar.getInstance().get(Calendar.DAY_OF_WEEK))) {
                total++;
                if (h.doneDates.contains(today)) done++;
            }
        }

        txtTodayProgress.setText(done + " از " + total + " انجام شد");
    }

    private void loadWeekStats() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -6);

        int total = 0;
        int done = 0;

        for (int i = 0; i < 7; i++) {
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(cal.getTime());

            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

            for (Habit h : habits) {
                if (h.isActiveToday(dayOfWeek)) {
                    total++;
                    if (h.doneDates.contains(date)) done++;
                }
            }
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        int percent = total == 0 ? 0 : (done * 100 / total);
        txtWeekProgress.setText(percent + "٪ پایبندی");
    }

    private void loadBestHabit() {
        Habit best = null;

        for (Habit h : habits) {
            if (best == null || h.doneDates.size() > best.doneDates.size()) {
                best = h;
            }
        }

        if (best != null) {
            txtBestHabit.setText(best.name + " (" + best.doneDates.size() + " بار)");
        }
    }

    private void loadWorstDay() {
        Map<Integer, Integer> countMap = new HashMap<>();

        for (Habit h : habits) {
            for (String date : h.doneDates) {
                try {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date));
                    int day = cal.get(Calendar.DAY_OF_WEEK);
                    countMap.put(day, countMap.getOrDefault(day, 0) + 1);
                } catch (Exception ignored) {}
            }
        }

        int worstDay = -1;
        int min = Integer.MAX_VALUE;

        for (int day = 1; day <= 7; day++) {
            int count = countMap.getOrDefault(day, 0);
            if (count < min) {
                min = count;
                worstDay = day;
            }
        }

        txtWorstDay.setText(getDayName(worstDay));
    }

    private String getDayName(int day) {
        switch (day) {
            case Calendar.SATURDAY: return "شنبه";
            case Calendar.SUNDAY: return "یکشنبه";
            case Calendar.MONDAY: return "دوشنبه";
            case Calendar.TUESDAY: return "سه‌شنبه";
            case Calendar.WEDNESDAY: return "چهارشنبه";
            case Calendar.THURSDAY: return "پنجشنبه";
            case Calendar.FRIDAY: return "جمعه";
            default: return "—";
        }
    }

    private void loadWeeklyChart() {
        List<BarEntry> entries = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -6);

        for (int i = 0; i < 7; i++) {
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(cal.getTime());

            int doneCount = 0;
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

            for (Habit h : habits) {
                if (h.isActiveToday(dayOfWeek) && h.doneDates.contains(date)) {
                    doneCount++;
                }
            }

            entries.add(new BarEntry(i, doneCount));
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        BarDataSet dataSet = new BarDataSet(entries, "عادت‌های انجام‌شده");
        dataSet.setColor(getResources().getColor(R.color.primary));

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.9f);

        barChartWeek.setData(data);
        barChartWeek.setFitBars(true);
        barChartWeek.getDescription().setEnabled(false);

        XAxis xAxis = barChartWeek.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        barChartWeek.invalidate();
    }

    
}
