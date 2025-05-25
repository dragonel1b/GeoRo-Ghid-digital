package com.example.myapplication.RomApp;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.GridLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.myapplication.Joc1.RomMainActivity;
import com.example.myapplication.R;
import com.example.myapplication.RomApp.*;
import com.example.myapplication.Joc1.RomSplashActivity;
import com.google.android.material.button.MaterialButton;

public class UserActivity extends AppCompatActivity {
    private CardView welcomeCard;
    private GridLayout gridLayoutRegions;
    private MaterialButton logoutButton;
    private Handler handler = new Handler();
    private Handler colorHandler = new Handler();
    private ConstraintLayout mainLayout;
    private AnimationDrawable gradientAnimation;
    private static final int COLOR_CHANGE_DELAY = 15000; // 15 seconds
    private int currentColorSet = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Initialize views
        mainLayout = findViewById(R.id.mainWelcome);
        setupGradientBackground();
        welcomeCard = findViewById(R.id.welcomeCard);
        gridLayoutRegions = findViewById(R.id.gridLayoutRegions);
        logoutButton = findViewById(R.id.buttonWelcome);

        // Apply animations
        applyEntranceAnimations();

        // Set up button click animations
        setupButtonAnimations();

        // Start color cycling
        startColorCycling();
    }

    private void applyEntranceAnimations() {
        // Initially hide all buttons
        for (int i = 0; i < gridLayoutRegions.getChildCount(); i++) {
            gridLayoutRegions.getChildAt(i).setVisibility(View.INVISIBLE);
        }
        logoutButton.setVisibility(View.INVISIBLE);

        // Welcome card animation
        Animation cardAnim = AnimationUtils.loadAnimation(this, R.anim.welcome_card_enter);
        welcomeCard.startAnimation(cardAnim);

        // Staggered animations for grid buttons
        handler.postDelayed(() -> {
            for (int i = 0; i < gridLayoutRegions.getChildCount(); i++) {
                View child = gridLayoutRegions.getChildAt(i);
                child.setVisibility(View.VISIBLE);
                Animation anim = AnimationUtils.loadAnimation(this, R.anim.staggered_fade_slide);
                int delay = i * 100; // 100ms delay between each button
                handler.postDelayed(() -> child.startAnimation(anim), delay);
            }
        }, 500); // Start after welcome card animation

        // Logout button animation
        handler.postDelayed(() -> {
            logoutButton.setVisibility(View.VISIBLE);
            Animation buttonAnim = AnimationUtils.loadAnimation(this, R.anim.slide_in_up);
            logoutButton.startAnimation(buttonAnim);
        }, 1500); // After all grid buttons
    }

    private void startColorCycling() {
        MaterialButton[] buttons = {
                findViewById(R.id.buttonTransilvania),
                findViewById(R.id.buttonMoldova),
                findViewById(R.id.buttonBucovina),
                findViewById(R.id.buttonOltenia),
                findViewById(R.id.buttonDobrogea),
                findViewById(R.id.buttonMuntenia),
                findViewById(R.id.buttonBanat),
                findViewById(R.id.buttonCrisana),
                findViewById(R.id.buttonMaramures)
        };

        // Initial background setup
        updateButtonColors(buttons, 0);

        // Schedule periodic background changes
        colorHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                currentColorSet = (currentColorSet + 1) % 3;
                updateButtonColors(buttons, currentColorSet);
                colorHandler.postDelayed(this, COLOR_CHANGE_DELAY);
            }
        }, COLOR_CHANGE_DELAY);
    }

    private void updateButtonColors(MaterialButton[] buttons, int offset) {
        // Background drawables for each color
        int[] backgrounds = {
                R.drawable.button_blue_background,
                R.drawable.button_yellow_background,
                R.drawable.button_red_background
        };

        // Apply backgrounds with fade transition
        for (int i = 0; i < buttons.length; i++) {
            final MaterialButton button = buttons[i];
            final int colorIndex = (i / 3 + offset) % 3;
            final int backgroundRes = backgrounds[colorIndex];

            // Calculate delay for sequential animation
            int delay = i * 150; // 150ms delay between each button

            handler.postDelayed(() -> {
                // Fade out
                button.animate()
                        .alpha(0.3f)
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(500)  // Slower fade out
                        .withEndAction(() -> {
                            // Change background at lowest alpha
                            button.setBackground(getDrawable(backgroundRes));

                            // Fade back in
                            button.animate()
                                    .alpha(1.0f)
                                    .scaleX(1.0f)
                                    .scaleY(1.0f)
                                    .setDuration(500)  // Slower fade in
                                    .setStartDelay(100);  // Small pause before fade in
                        });
            }, delay);
        }
    }

    private void setupButtonAnimations() {
        // Load animations
        Animation bounceAnim = AnimationUtils.loadAnimation(this, R.anim.button_bounce);
        Animation pressAnim = AnimationUtils.loadAnimation(this, R.anim.button_press);

        for (int i = 0; i < gridLayoutRegions.getChildCount(); i++) {
            View child = gridLayoutRegions.getChildAt(i);
            if (child instanceof MaterialButton) {
                MaterialButton button = (MaterialButton) child;
                // Add combined press and bounce animation
                button.setOnClickListener(v -> {
                    // Start press animation
                    v.startAnimation(pressAnim);

                    // After press, do bounce and handle click
                    handler.postDelayed(() -> {
                        Animation bounceAnimation = AnimationUtils.loadAnimation(this, R.anim.button_bounce);
                        bounceAnimation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {}

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                handleRegionButtonClick(v);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {}
                        });
                        v.startAnimation(bounceAnimation);
                    }, 150); // Start after press animation
                });
            }
        }

        // Setup logout button with subtle animation
        logoutButton.setOnClickListener(v -> {
            v.startAnimation(pressAnim);
            handler.postDelayed(() -> handleLogout(), 150);
        });
    }

    private void handleRegionButtonClick(View view) {
        Intent intent = new Intent();
        String region = "";

        if (view.getId() == R.id.buttonTransilvania) {
            intent.setClass(this, Transilvania.class);
            region = "Transilvania";
        } else if (view.getId() == R.id.buttonMoldova) {
            intent.setClass(this, Moldova.class);
            region = "Moldova";
        } else if (view.getId() == R.id.buttonBucovina) {
            intent.setClass(this, Bucovina.class);
            region = "Bucovina";
        } else if (view.getId() == R.id.buttonOltenia) {
            intent.setClass(this, Oltenia.class);
            region = "Oltenia";
        } else if (view.getId() == R.id.buttonDobrogea) {
            intent.setClass(this, Dobrogea.class);
            region = "Dobrogea";
        } else if (view.getId() == R.id.buttonMuntenia) {
            intent.setClass(this, Muntenia.class);
            region = "Muntenia";
        } else if (view.getId() == R.id.buttonBanat) {
            intent.setClass(this, Banat.class);
            region = "Banat";
        } else if (view.getId() == R.id.buttonCrisana) {
            intent.setClass(this, Crisana.class);
            region = "Crișana";
        } else if (view.getId() == R.id.buttonMaramures) {
            intent.setClass(this, Maramures.class);
            region = "Maramureș";
        } else if (view.getId() == R.id.buttonRom) {
            intent.setClass(this, RomMainActivity.class);
            region = "Mini Game";
        }

        if (!region.isEmpty()) {
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    private void handleLogout() {
        // Implement logout logic
        finish();
    }

    private void setupGradientBackground() {
        mainLayout.setBackground(getDrawable(R.drawable.bg_gradient1));
        gradientAnimation = (AnimationDrawable) mainLayout.getBackground();
        gradientAnimation.setEnterFadeDuration(2000);
        gradientAnimation.setExitFadeDuration(4000);
        gradientAnimation.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gradientAnimation != null && !gradientAnimation.isRunning()) {
            gradientAnimation.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gradientAnimation != null && gradientAnimation.isRunning()) {
            gradientAnimation.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        colorHandler.removeCallbacksAndMessages(null);
    }
}
