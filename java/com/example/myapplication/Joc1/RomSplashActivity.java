package com.example.myapplication.Joc1;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.myapplication.R;
import com.example.myapplication.RomApp.MainActivity;

public class RomSplashActivity extends AppCompatActivity {
    private static final long SPLASH_DELAY = 6000; // 6 seconds total duration
    private boolean isLogoAnimating = false;
    private Handler handler;
    private Runnable navigationRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rom_splash);

        // Initialize game state
        RomGameState.getInstance().initialize(this);

        // Initialize handler and navigation runnable
        handler = new Handler();
        navigationRunnable = () -> navigateToMain();

        // Initialize views
        ConstraintLayout rootLayout = findViewById(R.id.root);
        ImageView logoImage = findViewById(R.id.splashLogo);
        TextView titleText = findViewById(R.id.splashTitle);
        TextView subtitleText = findViewById(R.id.splashSubtitle);
        TextView taglineText = findViewById(R.id.splashTagline);
        TextView versionText = findViewById(R.id.versionText);
        FrameLayout flagContainer = findViewById(R.id.flagContainer);
        LinearLayout flagStrip = findViewById(R.id.flagStrip);

        // Start background animation
        AnimationDrawable gradientAnimation = (AnimationDrawable) rootLayout.getBackground();
        gradientAnimation.setEnterFadeDuration(2000);
        gradientAnimation.setExitFadeDuration(4000);
        gradientAnimation.start();

        // Load animations
        Animation splashScaleUp = AnimationUtils.loadAnimation(this, R.anim.splash_scale_up);
        Animation logoRotate = AnimationUtils.loadAnimation(this, R.anim.logo_rotate);
        Animation flagWave = AnimationUtils.loadAnimation(this, R.anim.flag_wave);
        Animation textTrembling = AnimationUtils.loadAnimation(this, R.anim.text_trembling);

        // Start with logo animation
        logoImage.setVisibility(View.VISIBLE);
        logoImage.startAnimation(splashScaleUp);

        // Smooth fade-in for texts with trembling
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(1000);
        fadeIn.setFillAfter(true);

        // Title animation
        titleText.setVisibility(View.VISIBLE);
        titleText.startAnimation(fadeIn);
        handler.postDelayed(() -> titleText.startAnimation(textTrembling), 1000);

        // Subtitle animation
        handler.postDelayed(() -> {
            subtitleText.setVisibility(View.VISIBLE);
            subtitleText.startAnimation(fadeIn);
            handler.postDelayed(() -> subtitleText.startAnimation(textTrembling), 1000);
        }, 500);

        // Tagline animation
        handler.postDelayed(() -> {
            taglineText.setVisibility(View.VISIBLE);
            taglineText.startAnimation(fadeIn);
            handler.postDelayed(() -> taglineText.startAnimation(textTrembling), 1000);
        }, 1000);

        // Flag animation
        handler.postDelayed(() -> {
            flagContainer.setVisibility(View.VISIBLE);
            flagContainer.startAnimation(fadeIn);
            flagStrip.startAnimation(flagWave);
        }, 1500);

        // Version text animation
        handler.postDelayed(() -> {
            versionText.setVisibility(View.VISIBLE);
            versionText.startAnimation(fadeIn);
            handler.postDelayed(() -> versionText.startAnimation(textTrembling), 1000);
        }, 2000);

        // Enhanced logo interactivity
        logoImage.setOnClickListener(v -> {
            if (!isLogoAnimating) {
                isLogoAnimating = true;
                Animation rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_rotate);
                rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        isLogoAnimating = false;
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                v.startAnimation(rotateAnimation);
            }
        });

        // Set up navigation
        handler.postDelayed(this::navigateToMain, SPLASH_DELAY);
    }

    private void navigateToMain() {
        Intent intent = new Intent(RomSplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacks(navigationRunnable);
        }
    }
}
