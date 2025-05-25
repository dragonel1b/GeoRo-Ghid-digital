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

public class MainActivity extends AppCompatActivity {
    private ConstraintLayout mainLayout;
    private ImageView logoImage, confettiImage;
    private TextView welcomeText;
    private TextInputLayout emailLayout, passwordLayout;
    private EditText emailInput, passwordInput;
    private MaterialButton signUpButton;
    private TextView loginRedirectText;
    private CircularProgressIndicator progressBar;
    private Animation fadeIn, scaleUp, slideInUp, buttonBounce, shake, iconHoverScale, confettiAnim;
    private AnimationDrawable gradientAnimation;
    private AnimationDrawable confettiDrawable;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();

        initializeViews();
        initializeAnimations();
        setupGradientBackground();
        setupConfetti();
        setupClickListeners();
        startEntryAnimations();
    }

    private void initializeViews() {
        mainLayout = findViewById(R.id.mainSignUp);
        logoImage = findViewById(R.id.imageLogoSignUp);
        confettiImage = findViewById(R.id.confettiImageSignUp);
        welcomeText = findViewById(R.id.textViewSignUp);
        emailLayout = findViewById(R.id.textInputLayoutEmailSignUp);
        passwordLayout = findViewById(R.id.textInputLayoutPasswordSignUp);
        emailInput = findViewById(R.id.editTextEmailSignUp);
        passwordInput = findViewById(R.id.editTextPasswordSignUp);
        signUpButton = findViewById(R.id.buttonSignUp);
        loginRedirectText = findViewById(R.id.textViewRedirectLogin);
        progressBar = findViewById(R.id.progressBarSignUp);
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
        signUpButton.setAlpha(0f);
        loginRedirectText.setAlpha(0f);

        emailLayout.animate().alpha(1f).setDuration(1000).setStartDelay(500);
        passwordLayout.animate().alpha(1f).setDuration(1000).setStartDelay(700);
        signUpButton.animate().alpha(1f).setDuration(1000).setStartDelay(900);
        loginRedirectText.animate().alpha(1f).setDuration(1000).setStartDelay(1100);
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
                Intent intent = new Intent(MainActivity.this, UserActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.zoom_exit, R.anim.fade_out);
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    private void setupClickListeners() {
        signUpButton.setOnClickListener(v -> {
            v.startAnimation(buttonBounce);
            if (validateInput()) {
                createAccount();
            }
        });

        loginRedirectText.setOnClickListener(v -> {
            v.startAnimation(buttonBounce);
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_slide_in, R.anim.page_swipe_blur);
            finish();
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

    private boolean validateInput() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        boolean isValid = true;

        // Reset errors
        emailLayout.setError(null);
        passwordLayout.setError(null);

        if (email.isEmpty()) {
            emailLayout.setError("Email is required");
            emailLayout.startAnimation(shake);
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Please enter a valid email");
            emailLayout.startAnimation(shake);
            isValid = false;
        }

        if (password.isEmpty()) {
            passwordLayout.setError("Password is required");
            passwordLayout.startAnimation(shake);
            isValid = false;
        } else if (password.length() < 6) {
            passwordLayout.setError("Password must be at least 6 characters");
            passwordLayout.startAnimation(shake);
            isValid = false;
        }

        return isValid;
    }

    private void createAccount() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Show progress
        setLoading(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Show confetti and then navigate
                        setLoading(false);
                        showSuccessConfetti();
                    } else {
                        setLoading(false);
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() :
                                "Sign up failed";
                        Snackbar.make(mainLayout, "Error: " + errorMessage, Snackbar.LENGTH_LONG).show();
                        emailLayout.startAnimation(shake);
                        passwordLayout.startAnimation(shake);

                        // Trigger error vibration
                        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        if (vibrator != null && vibrator.hasVibrator()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE));
                            } else {
                                vibrator.vibrate(400);
                            }
                        }
                    }
                });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        signUpButton.setEnabled(!isLoading);
        emailInput.setEnabled(!isLoading);
        passwordInput.setEnabled(!isLoading);
        loginRedirectText.setEnabled(!isLoading);
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
