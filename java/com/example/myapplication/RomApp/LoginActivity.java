package com.example.myapplication.RomApp;

import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.example.myapplication.R;

public class LoginActivity extends AppCompatActivity {
    private ConstraintLayout mainLayout;
    private ImageView logoImage, confettiImage;
    private TextView welcomeText, signUpLink, forgotPasswordLink;
    private TextInputLayout emailLayout, passwordLayout;
    private EditText emailInput, passwordInput;
    private MaterialButton loginButton;
    private CircularProgressIndicator progressBar;
    private Animation fadeIn, scaleUp, slideInUp, buttonBounce, shake, iconHoverScale, confettiAnim;
    private AnimationDrawable gradientAnimation;
    private AnimationDrawable confettiDrawable;
    private int loginAttempts = 0;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        mAuth = FirebaseAuth.getInstance();

        initializeViews();
        initializeAnimations();
        setupGradientBackground();
        setupConfetti();
        setupClickListeners();
        startEntryAnimations();
    }

    private void initializeViews() {
        mainLayout = findViewById(R.id.mainLogin);
        logoImage = findViewById(R.id.imageLogo);
        confettiImage = findViewById(R.id.confettiImage);
        welcomeText = findViewById(R.id.textViewLogin);
        emailLayout = findViewById(R.id.textInputLayoutEmail);
        passwordLayout = findViewById(R.id.textInputLayoutPassword);
        emailInput = findViewById(R.id.editTextEmail);
        passwordInput = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.buttonLogin);
        signUpLink = findViewById(R.id.textViewSignUp);
        forgotPasswordLink = findViewById(R.id.textViewForgotPassword);
        progressBar = findViewById(R.id.progressBarLogin);
    }

    private void initializeAnimations() {
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        slideInUp = AnimationUtils.loadAnimation(this, R.anim.slide_in_up);
        buttonBounce = AnimationUtils.loadAnimation(this, R.anim.button_bounce);
        shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        iconHoverScale = AnimationUtils.loadAnimation(this, R.anim.icon_hover_scale);
        confettiAnim = AnimationUtils.loadAnimation(this, R.anim.confetti_success);
    }

    private void setupGradientBackground() {
        mainLayout.setBackground(getDrawable(R.drawable.bg_gradient1));
        gradientAnimation = (AnimationDrawable) mainLayout.getBackground();
        gradientAnimation.setEnterFadeDuration(2000);
        gradientAnimation.setExitFadeDuration(4000);
        gradientAnimation.start();
    }

    private void setupConfetti() {
        confettiImage.setBackgroundResource(R.drawable.confetti_animation);
        confettiDrawable = (AnimationDrawable) confettiImage.getBackground();
        confettiImage.setVisibility(View.INVISIBLE);
    }

    private void startEntryAnimations() {
        logoImage.startAnimation(scaleUp);
        welcomeText.startAnimation(slideInUp);

        // Delay the fade-in of input fields
        emailLayout.setAlpha(0f);
        passwordLayout.setAlpha(0f);
        loginButton.setAlpha(0f);
        signUpLink.setAlpha(0f);
        forgotPasswordLink.setAlpha(0f);

        emailLayout.animate().alpha(1f).setDuration(1000).setStartDelay(500);
        passwordLayout.animate().alpha(1f).setDuration(1000).setStartDelay(700);
        loginButton.animate().alpha(1f).setDuration(1000).setStartDelay(900);
        signUpLink.animate().alpha(1f).setDuration(1000).setStartDelay(1100);
        forgotPasswordLink.animate().alpha(1f).setDuration(1000).setStartDelay(1100);

        // Start pulsing animation for forgot password link
        startForgotPasswordPulse();
    }

    private void startForgotPasswordPulse() {
        forgotPasswordLink.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .alpha(0.7f)
                .setDuration(1000)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        forgotPasswordLink.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .alpha(1f)
                                .setDuration(1000)
                                .withEndAction(this)
                                .start();
                    }
                }).start();
    }

    private void showSuccessConfetti() {
        confettiImage.setVisibility(View.VISIBLE);
        confettiImage.startAnimation(confettiAnim);
        confettiDrawable.start();

        confettiAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                // Navigate to UserActivity after confetti
                Intent intent = new Intent(LoginActivity.this, UserActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.zoom_exit, R.anim.fade_out);
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> {
            v.startAnimation(buttonBounce);
            attemptLogin();
        });

        signUpLink.setOnClickListener(v -> {
            v.startAnimation(buttonBounce);
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_slide_in, R.anim.page_swipe_blur);
        });

        forgotPasswordLink.setOnClickListener(v -> {
            v.startAnimation(iconHoverScale);
            String email = emailInput.getText().toString().trim();
            if (email.isEmpty()) {
                emailLayout.setError("Enter email to reset password");
                emailLayout.startAnimation(shake);
                return;
            }
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Snackbar.make(mainLayout, "Password reset email sent", Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(mainLayout, "Failed to send reset email", Snackbar.LENGTH_LONG).show();
                        }
                    });
        });

        // Add touch feedback to input fields
        emailInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                emailLayout.animate().scaleX(1.02f).scaleY(1.02f).setDuration(200);
            } else {
                emailLayout.animate().scaleX(1f).scaleY(1f).setDuration(200);
            }
        });

        passwordInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                passwordLayout.animate().scaleX(1.02f).scaleY(1.02f).setDuration(200);
            } else {
                passwordLayout.animate().scaleX(1f).scaleY(1f).setDuration(200);
            }
        });
    }

    private void attemptLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Reset errors
        emailLayout.setError(null);
        passwordLayout.setError(null);

        // Validate input
        if (email.isEmpty()) {
            emailLayout.setError("Email is required");
            emailLayout.startAnimation(shake);
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Please enter a valid email");
            emailLayout.startAnimation(shake);
            return;
        }

        if (password.isEmpty()) {
            passwordLayout.setError("Password is required");
            passwordLayout.startAnimation(shake);
            return;
        }

        if (password.length() < 6) {
            passwordLayout.setError("Password must be at least 6 characters");
            passwordLayout.startAnimation(shake);
            return;
        }

        loginAttempts++;
        if (loginAttempts >= 3) {
            // Trigger vibration for too many attempts
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(400);
                }
            }
        }

        // Show progress
        loginButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        // Attempt Firebase login
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Show confetti and then navigate
                        progressBar.setVisibility(View.GONE);
                        showSuccessConfetti();
                    } else {
                        // Login failed
                        progressBar.setVisibility(View.GONE);
                        loginButton.setEnabled(true);
                        Snackbar.make(mainLayout, "Authentication failed", Snackbar.LENGTH_LONG).show();
                        emailLayout.startAnimation(shake);
                        passwordLayout.startAnimation(shake);
                    }
                });
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
        if (confettiDrawable != null && confettiDrawable.isRunning()) {
            confettiDrawable.stop();
        }
    }
}
