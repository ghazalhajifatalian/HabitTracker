package com.example.habbittracker;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    TextInputEditText etName, etEmail, etPassword;
    MaterialButton btnRegister, btnGoogle, btnBack;
    View loadingOverlay;

    FirebaseAuth auth;
    FirebaseFirestore db;
    GoogleSignInClient googleClient;

    private static final int RC_SIGN_UP = 2001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnBack = findViewById(R.id.btnBack);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        setupGoogle();

        btnRegister.setOnClickListener(v -> registerWithEmail());
        btnGoogle.setOnClickListener(v -> registerWithGoogle());
        btnBack.setOnClickListener(v -> finishWithAnim());
    }

    private void setLoading(boolean loading) {
        loadingOverlay.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!loading);
        btnGoogle.setEnabled(!loading);
        btnBack.setEnabled(!loading);
    }

    private void registerWithEmail() {
        setLoading(true);

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            shake();
            setLoading(false);
            showError("همه فیلدها الزامی هستند");
            return;
        }

        auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(r -> createUserInFirestore(
                        auth.getCurrentUser().getUid(), name, email))
                .addOnFailureListener(e -> {
                    setLoading(false);
                    shake();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void setupGoogle() {
        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
        googleClient = GoogleSignIn.getClient(this, gso);
    }

    private void registerWithGoogle() {
        setLoading(true);
        startActivityForResult(googleClient.getSignInIntent(), RC_SIGN_UP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_UP) {
            try {
                GoogleSignInAccount account =
                        GoogleSignIn.getSignedInAccountFromIntent(data)
                                .getResult(ApiException.class);

                AuthCredential credential =
                        GoogleAuthProvider.getCredential(account.getIdToken(), null);

                auth.signInWithCredential(credential)
                        .addOnSuccessListener(r -> {
                            FirebaseUser u = auth.getCurrentUser();
                            createUserInFirestore(u.getUid(),
                                    u.getDisplayName(),
                                    u.getEmail());
                        });
            } catch (ApiException e) {
                setLoading(false);
                showError("ثبت‌نام با گوگل ناموفق بود");
            }
        }
    }

    private void showError(String msg) {

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.green_toast, null);

        TextView text = layout.findViewById(R.id.txtToast);
        text.setText(msg);

        Toast toast = new Toast(this);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 150);
        toast.show();
    }

    private void createUserInFirestore(String uid, String name, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("points", 0);
        user.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(uid).set(user)
                .addOnSuccessListener(v ->
                        new Handler(getMainLooper()).postDelayed(this::goLogin, 1000))
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void goLogin() {
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("REGISTER_SUCCESS", true);

        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }


    private void finishWithAnim() {
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void shake() {
        etName.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
        etEmail.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
        etPassword.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

    }
}
