package com.example.habbittracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

    List<Habit> habitList;
    Runnable onHabitChanged;
    String today;

    public HabitAdapter(List<Habit> habitList, String today, Runnable onHabitChanged) {
        this.habitList = habitList;
        this.today = today;
        this.onHabitChanged = onHabitChanged;
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_habit, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit habit = habitList.get(position);

        holder.txtName.setText(habit.name);
        holder.txtTime.setText("⏰ " + habit.time);
        holder.txtCategory.setText("📂 " + habit.category);
        holder.txtNote.setText(habit.note);

        holder.txtDays.setText(habit.getActiveDaysString());

        holder.checkDone.setOnCheckedChangeListener(null);
        holder.checkDone.setChecked(habit.doneToday);

        holder.checkDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            Habit h = habitList.get(pos);

            if (isChecked && !h.doneToday) {
                h.doneToday = true;
                h.doneDates.add(today);
                h.progress++;
            } else if (!isChecked && h.doneToday) {
                h.doneToday = false;
                h.doneDates.remove(today);
                if (h.progress > 0) h.progress--;
            }

            LocalStorageHelper.saveHabits(buttonView.getContext(), habitList);
            notifyItemChanged(pos);
            if (onHabitChanged != null) onHabitChanged.run();
        });

        holder.btnDelete.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            habitList.remove(pos);
            LocalStorageHelper.saveHabits(v.getContext(), habitList);
            notifyItemRemoved(pos);
            if (onHabitChanged != null) onHabitChanged.run();
        });
    }

    @Override
    public int getItemCount() {
        return habitList.size();
    }

    static class HabitViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtTime, txtCategory, txtNote, txtDays;
        CheckBox checkDone;
        ImageView btnDelete;

        public HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtHabitName);
            txtTime = itemView.findViewById(R.id.txtHabitTime);
            txtDays = itemView.findViewById(R.id.txtHabitDay);
            txtCategory = itemView.findViewById(R.id.txtHabitCategory);
            txtNote = itemView.findViewById(R.id.txtHabitNote);
            checkDone = itemView.findViewById(R.id.checkDone);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    public void updateList(List<Habit> newList) {
        this.habitList = newList;
        notifyDataSetChanged();
    }



}
