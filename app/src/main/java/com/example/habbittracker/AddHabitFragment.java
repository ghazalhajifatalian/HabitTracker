package com.example.habbittracker;

import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddHabitFragment extends Fragment {

    EditText edtHabitName, edtGoal, edtNote;
    TextView txtTime;
    Button btnSave;
    Spinner spinnerCategory;
    MaterialSwitch switchReminder;

    ChipGroup chipGroupDays;
    Chip chipSat, chipSun, chipMon, chipTue, chipWed, chipThu, chipFri;

    int hour = 9, minute = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_habit, container, false);

        edtHabitName = view.findViewById(R.id.edtHabitName);
        edtGoal = view.findViewById(R.id.edtGoal);
        edtNote = view.findViewById(R.id.edtNote);
        txtTime = view.findViewById(R.id.txtTime);
        btnSave = view.findViewById(R.id.btnSaveHabit);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        switchReminder = view.findViewById(R.id.switchReminder);

        chipGroupDays = view.findViewById(R.id.chipGroupDays);
        chipSat = view.findViewById(R.id.chipSat);
        chipSun = view.findViewById(R.id.chipSun);
        chipMon = view.findViewById(R.id.chipMon);
        chipTue = view.findViewById(R.id.chipTue);
        chipWed = view.findViewById(R.id.chipWed);
        chipThu = view.findViewById(R.id.chipThu);
        chipFri = view.findViewById(R.id.chipFri);

        txtTime.setText(String.format("%02d:%02d", hour, minute));
        txtTime.setOnClickListener(v -> showTimePicker());

        btnSave.setOnClickListener(v -> saveHabit());

        setupSpinner();

        return view;
    }

    private void setupSpinner() {
        String[] categories = {"سلامت", "فعالیت", "درس", "تفریح", "کلی"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void showTimePicker() {
        TimePickerDialog picker = new TimePickerDialog(requireContext(),
                (TimePicker view, int hourOfDay, int minute) -> {
                    hour = hourOfDay;
                    this.minute = minute;
                    txtTime.setText(String.format("%02d:%02d", hour, minute));
                }, hour, minute, true);
        picker.show();
    }

    private void saveHabit() {
        String name = edtHabitName.getText().toString().trim();
        String goalStr = edtGoal.getText().toString().trim();
        String note = edtNote.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem() != null
                ? spinnerCategory.getSelectedItem().toString()
                : "کلی";

        String time = String.format("%02d:%02d", hour, minute);
        boolean notifyEnabled = switchReminder.isChecked();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(goalStr)) {
            showToast("اسم و هدف را وارد کن", true);
            return;
        }

        int goal;
        try {
            goal = Integer.parseInt(goalStr);
        } catch (Exception e) {
            showToast("عدد هدف معتبر نیست", true);
            return;
        }

        Set<Integer> activeDays = new HashSet<>();
        if (chipSat.isChecked()) activeDays.add(7);
        if (chipSun.isChecked()) activeDays.add(1);
        if (chipMon.isChecked()) activeDays.add(2);
        if (chipTue.isChecked()) activeDays.add(3);
        if (chipWed.isChecked()) activeDays.add(4);
        if (chipThu.isChecked()) activeDays.add(5);
        if (chipFri.isChecked()) activeDays.add(6);

        if (activeDays.isEmpty()) {
            showToast("حداقل یک روز انتخاب کن", true);
            return;
        }

        Habit newHabit = new Habit(name, goal, activeDays, time, notifyEnabled, category, note);

        // Load all habits from storage
        List<Habit> habits = LocalStorageHelper.loadHabits(requireContext());
        if (habits == null) habits = new ArrayList<>();

        // Log habits before adding new one
        Log.d("AddHabitFragment", "All habits before adding new one: " + habits.size());
        for (Habit h : habits) {
            Log.d("AddHabitFragment", "Habit in list: " + h.name + ", activeDays=" + h.activeDays + ", time=" + h.time);
        }

        // Prevent duplicates by name
        boolean exists = false;
        for (Habit h : habits) {
            if (h.name.equals(newHabit.name)) {
                exists = true;
                Log.d("AddHabitFragment", "Habit already exists: " + newHabit.name);
                break;
            }
        }
        if (!exists) {
            habits.add(newHabit);
            Log.d("AddHabitFragment", "Adding new habit: " + newHabit.name);
        } else {
            showToast("این عادت قبلاً وجود دارد", true);
            return;
        }

        // Save habits
        LocalStorageHelper.saveHabits(requireContext(), habits);

        // Log habits after saving
        List<Habit> allHabits = LocalStorageHelper.loadHabits(requireContext());
        Log.d("AddHabitFragment", "All habits after saving: " + allHabits.size());
        for (Habit h : allHabits) {
            Log.d("AddHabitFragment", "Habit in storage: " + h.name +
                    ", activeDays=" + h.activeDays +
                    ", time=" + h.time +
                    ", notify=" + h.notifyEnabled);
        }

        // Schedule notification if needed
        if (notifyEnabled) {
            NotificationHelper.scheduleHabitReminder(requireContext(), newHabit);
        }

        showToast("عادت اضافه شد ✅", false);

        // Close fragment after short delay
        edtHabitName.postDelayed(() ->
                NavHostFragment.findNavController(this).navigateUp(), 400);
    }

    private void showToast(String msg, boolean error) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.green_toast, null);

        TextView text = layout.findViewById(R.id.txtToast);
        text.setText(msg);

        GradientDrawable bgDrawable = (GradientDrawable) layout.getBackground();
        bgDrawable.setColor(error
                ? Color.parseColor("#F44336")
                : Color.parseColor("#4CAF50"));

        Toast toast = new Toast(requireContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 150);
        toast.show();
    }
}
