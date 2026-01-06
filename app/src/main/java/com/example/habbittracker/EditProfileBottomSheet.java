package com.example.habbittracker;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class EditProfileBottomSheet extends BottomSheetDialogFragment {

    TextInputLayout inputName;
    TextInputEditText edtName;
    MaterialButton btnSave, btnCancel;
    private OnProfileUpdatedListener listener;

    public interface OnProfileUpdatedListener {
        void onProfileUpdated();
    }

    public void setOnProfileUpdatedListener(OnProfileUpdatedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.bottomsheet_edit_profile, container, false);

        inputName = view.findViewById(R.id.inputName);
        edtName = view.findViewById(R.id.edtName);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getDisplayName() != null) {
            edtName.setText(user.getDisplayName());
        }

        btnSave.setOnClickListener(v -> saveProfile());
        btnCancel.setOnClickListener(v -> dismiss());

        return view;
    }

    private void saveProfile() {
        String name = edtName.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            inputName.setError("نام نمی‌تواند خالی باشد");
            return;
        }

        inputName.setError(null);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        UserProfileChangeRequest request =
                new UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build();

        user.updateProfile(request).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(requireContext(),
                        "پروفایل ویرایش شد",
                        Toast.LENGTH_SHORT).show();
                dismiss();
            }
            if (listener != null) {
                listener.onProfileUpdated();
            }
            dismiss();
        });


    }
}
