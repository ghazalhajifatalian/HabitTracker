package com.example.habbittracker;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.logo);
        TextView text = findViewById(R.id.text);

        logo.setAlpha(0f);
        text.setAlpha(0f);

        logo.animate().alpha(1f).setDuration(1500).start();
        text.animate().alpha(1f).setDuration(1500).start();

        new Handler().postDelayed(() -> {

            FirebaseUser currentUser =
                    FirebaseAuth.getInstance().getCurrentUser();

            Intent intent;
            if (currentUser != null) {
                // کاربر لاگین است
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                // کاربر لاگین نیست
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();

        }, 2500);
    }
}
