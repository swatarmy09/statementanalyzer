package com.example.statementanalyzer.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.statementanalyzer.R;
import com.example.statementanalyzer.data.PreferenceManager;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2500;
    private ImageView logoImageView;
    private TextView appNameTextView;
    private TextView taglineTextView;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize views
        logoImageView = findViewById(R.id.logoImageView);
        appNameTextView = findViewById(R.id.appNameTextView);
        taglineTextView = findViewById(R.id.taglineTextView);

        // Initialize preference manager
        preferenceManager = new PreferenceManager(this);

        // Start animations
        startAnimations();

        // Navigate to next screen after delay
        new Handler().postDelayed(this::navigateToNextScreen, SPLASH_DURATION);
    }

    private void startAnimations() {
        // Logo animation - fade in and scale
        logoImageView.setAlpha(0f);
        logoImageView.setScaleX(0.5f);
        logoImageView.setScaleY(0.5f);

        ObjectAnimator logoFade = ObjectAnimator.ofFloat(logoImageView, View.ALPHA, 0f, 1f);
        logoFade.setDuration(1000);

        ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(logoImageView, View.SCALE_X, 0.5f, 1f);
        logoScaleX.setDuration(1000);
        logoScaleX.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(logoImageView, View.SCALE_Y, 0.5f, 1f);
        logoScaleY.setDuration(1000);
        logoScaleY.setInterpolator(new DecelerateInterpolator());

        logoFade.start();
        logoScaleX.start();
        logoScaleY.start();

        // App name animation - slide in from top
        appNameTextView.setTranslationY(-100f);
        appNameTextView.setAlpha(0f);

        appNameTextView.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(800)
                .setStartDelay(500)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // Tagline animation - fade in
        taglineTextView.setAlpha(0f);

        taglineTextView.animate()
                .alpha(1f)
                .setDuration(800)
                .setStartDelay(1000)
                .start();
    }

    private void navigateToNextScreen() {
        // Check if user has completed onboarding
        if (preferenceManager.isFirstLaunch()) {
            startActivity(new Intent(SplashActivity.this, OnboardingActivity.class));
        } else {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        }

        // Exit animation
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
}