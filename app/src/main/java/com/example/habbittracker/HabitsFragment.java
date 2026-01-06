package com.example.habbittracker;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class HabitsFragment extends Fragment {

    RecyclerView recyclerView;
    HabitAdapter habitAdapter;
    List<Habit> habitList = new ArrayList<>();
    ChipGroup chipGroupDays;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_habits, container, false);

        recyclerView = view.findViewById(R.id.recyclerHabits);
        chipGroupDays = view.findViewById(R.id.chipGroupDays);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        habitList = LocalStorageHelper.loadHabits(requireContext());
        for (Habit h : habitList) {
            Log.d("HabitsFragment", "Loaded habit: " + h.name + ", days=" + h.activeDays);
        }
        habitAdapter = new HabitAdapter(habitList, "", null);
        recyclerView.setAdapter(habitAdapter);

        setupDayFilter();

        return view;
    }

    private void setupDayFilter() {
        for (int i = 0; i < chipGroupDays.getChildCount(); i++) {
            View child = chipGroupDays.getChildAt(i);
            if (child instanceof Chip) {
                ((Chip) child).setOnCheckedChangeListener((button, isChecked) -> filterHabits());
            }
        }
    }


    private void filterHabits() {
        List<Habit> allHabits = LocalStorageHelper.loadHabits(requireContext());
        Log.d("HabitsFragment", "All habits before filtering: " + allHabits);
        List<Habit> filtered = new ArrayList<>();

        // Collect selected days
        List<Integer> selectedDays = new ArrayList<>();
        if (((Chip) getView().findViewById(R.id.chipSun)).isChecked()) selectedDays.add(1);
        if (((Chip) getView().findViewById(R.id.chipMon)).isChecked()) selectedDays.add(2);
        if (((Chip) getView().findViewById(R.id.chipTue)).isChecked()) selectedDays.add(3);
        if (((Chip) getView().findViewById(R.id.chipWed)).isChecked()) selectedDays.add(4);
        if (((Chip) getView().findViewById(R.id.chipThu)).isChecked()) selectedDays.add(5);
        if (((Chip) getView().findViewById(R.id.chipFri)).isChecked()) selectedDays.add(6);
        if (((Chip) getView().findViewById(R.id.chipSat)).isChecked()) selectedDays.add(7);

        Log.d("HabitsFragment", "Selected days: " + selectedDays);

        // If none selected, show all
        if (selectedDays.isEmpty()) {
            habitAdapter.updateList(allHabits);
            return;
        }

        // Filter habits that match any selected day
        for (Habit h : allHabits) {
            for (int day : selectedDays) {
                if (h.isActiveToday(day)) {
                    filtered.add(h);
                    break; // habit matches at least one day
                }
            }
        }

        habitAdapter.updateList(filtered);
    }
}
