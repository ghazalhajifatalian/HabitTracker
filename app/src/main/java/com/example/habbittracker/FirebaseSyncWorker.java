package com.example.habbittracker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseSyncWorker extends Worker {

    public FirebaseSyncWorker(@NonNull Context context,
                              @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        //  بررسی لاگین بودن کاربر
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return Result.failure(); // لاگین نیست : سینک نکن
        }

        String uid = user.getUid();

        // گرفتن دیتای لوکال
        List<Habit> habits =
                LocalStorageHelper.loadHabits(getApplicationContext());

        int streak =
                LocalStorageHelper.loadStreak(getApplicationContext());

        // آماده‌سازی دیتا
        Map<String, Object> data = new HashMap<>();
        data.put("habits", habits);
        data.put("streak", streak);
        data.put("updatedAt", System.currentTimeMillis());

        // ارسال به Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(uid)
                .set(data, SetOptions.merge());

        return Result.success();
    }
}
