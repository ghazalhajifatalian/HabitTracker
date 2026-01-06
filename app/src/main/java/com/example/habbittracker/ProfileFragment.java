package com.example.habbittracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

public class ProfileFragment extends Fragment {

    ShapeableImageView imgProfile;
    TextView txtUserName, txtUserEmail, txtHabitCount, txtStreak, txtPoints;
    Button btnEditProfile, btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        imgProfile = view.findViewById(R.id.imgProfile);
        txtUserName = view.findViewById(R.id.txtUserName);
        txtUserEmail = view.findViewById(R.id.txtUserEmail);
        txtHabitCount = view.findViewById(R.id.txtHabitCount);
        txtStreak = view.findViewById(R.id.txtStreak);
        txtPoints = view.findViewById(R.id.txtPoints);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnLogout = view.findViewById(R.id.btnLogout);

        loadUserInfo();

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            // Call from the hosting Activity
            requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            requireActivity().finish();
        });


        btnEditProfile.setOnClickListener(v -> {
            EditProfileBottomSheet sheet = new EditProfileBottomSheet();

            sheet.setOnProfileUpdatedListener(() -> {
                loadUserInfo();
            });

            sheet.show(getParentFragmentManager(), "EditProfile");
        });



        return view;
    }

    private void loadUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            txtUserName.setText(user.getDisplayName() != null ? user.getDisplayName() : "دوست من");
            txtUserEmail.setText(user.getEmail());

            if (user.getPhotoUrl() != null) {
                Picasso.get().load(user.getPhotoUrl()).into(imgProfile);
            }

            // Load habits info from LocalStorage
            int habitCount = LocalStorageHelper.loadHabits(requireContext()).size();
            int streak = LocalStorageHelper.loadStreak(requireContext());
            int points = 0;

            txtHabitCount.setText(String.valueOf(habitCount));
            txtStreak.setText(String.valueOf(streak));
            txtPoints.setText(String.valueOf(points));
        }
    }
}
