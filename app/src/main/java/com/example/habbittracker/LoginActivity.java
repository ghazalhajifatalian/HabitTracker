package com.example.habbittracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText etEmail, etPassword;
    MaterialButton btnLogin, btnGoRegister, btnGoogle;
    View loadingOverlay;

    FirebaseAuth auth;
    FirebaseFirestore db;
    GoogleSignInClient googleSignInClient;

    private static final int RC_SIGN_IN = 1001;

    // ================= LIFECYCLE =================
    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            goToMain();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        boolean registered = getIntent().getBooleanExtra("REGISTER_SUCCESS", false);

        if (registered) {
            showError("اکانت با موفقیت ساخته شد - وارد شوید");
        }

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoRegister = findViewById(R.id.btnGoRegister);
        btnGoogle = findViewById(R.id.btnGoogle);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        setupGoogle();

        btnLogin.setOnClickListener(v -> loginWithEmail());
        btnGoogle.setOnClickListener(v -> loginWithGoogle());
        btnGoRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
    }

    // ================= UI CONTROL =================
    private void setLoading(boolean loading) {
        loadingOverlay.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        btnGoogle.setEnabled(!loading);
        btnGoRegister.setEnabled(!loading);
    }

    private void shakeInputs() {
        etEmail.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
        etPassword.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
    }

    // ================= EMAIL LOGIN =================
    private void loginWithEmail() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            shakeInputs();
            showError("همه فیلدها الزامی هستند");
            return;
        }

        setLoading(true);

        auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(r -> goToMain())
                .addOnFailureListener(e -> {
                    setLoading(false);
                    shakeInputs();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // ================= GOOGLE LOGIN =================
    private void setupGoogle() {
        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void loginWithGoogle() {
        setLoading(true);
        startActivityForResult(googleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            try {
                GoogleSignInAccount account =
                        GoogleSignIn.getSignedInAccountFromIntent(data)
                                .getResult(ApiException.class);

                firebaseAuthWithGoogle(account.getIdToken());

            } catch (ApiException e) {
                setLoading(false);
                showError("ورود با گوگل ناموفق بود");
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

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential =
                GoogleAuthProvider.getCredential(idToken, null);

        auth.signInWithCredential(credential)
                .addOnSuccessListener(result -> {

                    FirebaseUser user = auth.getCurrentUser();
                    String uid = user.getUid();

                    db.collection("users").document(uid).get()
                            .addOnSuccessListener(doc -> {

                                if (!doc.exists()) {
                                    Map<String, Object> data = new HashMap<>();
                                    data.put("name", user.getDisplayName());
                                    data.put("email", user.getEmail());
                                    data.put("points", 0);
                                    data.put("createdAt", System.currentTimeMillis());

                                    db.collection("users")
                                            .document(uid)
                                            .set(data)
                                            .addOnSuccessListener(v -> goToMain())
                                            .addOnFailureListener(e -> {
                                                setLoading(false);
                                                Toast.makeText(this,
                                                        "خطا در ذخیره اطلاعات",
                                                        Toast.LENGTH_SHORT).show();
                                            });
                                } else {
                                    goToMain();
                                }
                            })
                            .addOnFailureListener(e -> {
                                setLoading(false);
                                Toast.makeText(this,
                                        "خطا در دریافت اطلاعات",
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // ================= NAVIGATION =================
    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

    }
}
