package com.example.myapplication.RomApp;

import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Context;
import android.content.Intent;
import android.widget.CheckBox;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.widget.Button;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.example.myapplication.R;

public class MainActivity extends AppCompatActivity {
    private ConstraintLayout mainLayout;
    private ImageView logoImage, confettiImage;
    private TextView welcomeText;
    private TextInputLayout emailLayout, passwordLayout, confirmPasswordLayout;
    private EditText emailInput, passwordInput, confirmPasswordInput;
    private Button signUpButton;
    private TextView loginRedirectText;
    private CircularProgressIndicator progressBar;
    private Animation fadeIn, scaleUp, slideInUp, buttonBounce, shake, iconHoverScale, confettiAnim;
    private AnimationDrawable gradientAnimation;
    private AnimationDrawable confettiDrawable;
    private FirebaseAuth mAuth;
    private CheckBox termsCheckbox;
    private ProgressBar passwordStrengthBar;

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
        try {
            mainLayout = findViewById(R.id.mainSignUp);
            logoImage = findViewById(R.id.imageLogoSignUp);
            confettiImage = findViewById(R.id.confettiImageSignUp);
            welcomeText = findViewById(R.id.textViewSignUp);
            emailLayout = findViewById(R.id.textInputLayoutEmailSignUp);
            passwordLayout = findViewById(R.id.textInputLayoutPasswordSignUp);
            confirmPasswordLayout = findViewById(R.id.textInputLayoutConfirmPassword);
            emailInput = findViewById(R.id.editTextEmailSignUp);
            passwordInput = findViewById(R.id.editTextPasswordSignUp);
            confirmPasswordInput = findViewById(R.id.editTextConfirmPassword);
            signUpButton = findViewById(R.id.buttonSignUp);
            progressBar = findViewById(R.id.progressBarSignUp);
            termsCheckbox = findViewById(R.id.termsCheckbox);
            passwordStrengthBar = findViewById(R.id.passwordStrengthBar);

            // Verify all critical views
            if (mainLayout == null) throw new NullPointerException("mainLayout not found");
            if (logoImage == null) throw new NullPointerException("logoImage not found");
            if (welcomeText == null) throw new NullPointerException("welcomeText not found");
            if (emailLayout == null) throw new NullPointerException("emailLayout not found");
            if (passwordLayout == null) throw new NullPointerException("passwordLayout not found");
            if (confirmPasswordLayout == null) throw new NullPointerException("confirmPasswordLayout not found");
            if (signUpButton == null) throw new NullPointerException("signUpButton not found");

            // Initialize login redirect text view
            loginRedirectText = findViewById(R.id.textViewRedirectLogin);
            if (loginRedirectText == null) {
                Log.e("Navigation", "Login redirect TextView not found!");
            }
        } catch (NullPointerException e) {
            Log.e("ViewInit", "Critical view initialization failed", e);
            throw e; // Re-throw to fail fast
        }
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
        try {
            // Start animations only if views are available
            if (logoImage != null) logoImage.startAnimation(scaleUp);
            if (welcomeText != null) welcomeText.startAnimation(slideInUp);

            // Safely handle fade-in animations
            if (emailLayout != null) {
                emailLayout.setAlpha(0f);
                emailLayout.animate().alpha(1f).setDuration(1000).setStartDelay(500);
            }
            if (passwordLayout != null) {
                passwordLayout.setAlpha(0f);
                passwordLayout.animate().alpha(1f).setDuration(1000).setStartDelay(700);
            }
            if (signUpButton != null) {
                signUpButton.setAlpha(0f);
                signUpButton.animate().alpha(1f).setDuration(1000).setStartDelay(900);
            }

            TextView loginRedirect = findViewById(R.id.textViewRedirectLogin);
            if (loginRedirect != null) {
                loginRedirect.setAlpha(0f);
                loginRedirect.animate().alpha(1f).setDuration(1000).setStartDelay(1100);
            }
        } catch (Exception e) {
            Log.e("Animation", "Error during entry animations", e);
        }
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
            v.setEnabled(false); // Disable during sign up

            if (validateInput()) {
                createAccount();
            }

            // Re-enable after delay if validation fails
            v.postDelayed(() -> v.setEnabled(true), 2000);
        });

        TextView loginRedirect = findViewById(R.id.textViewRedirectLogin);
        loginRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Navigation", "Login redirect clicked - Starting LoginActivity");
                try {
                    v.startAnimation(buttonBounce);
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    Log.d("Navigation", "Intent created: " + intent.toString());
                    startActivity(intent);
                    Log.d("Navigation", "Activity started");
                    overridePendingTransition(R.anim.fade_slide_in, R.anim.page_swipe_blur);
                    finish();
                    Log.d("Navigation", "MainActivity finished");
                } catch (Exception e) {
                    Log.e("Navigation", "Error navigating to LoginActivity", e);
                }
            }
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
        
        // Add password strength check
        passwordInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePasswordStrength(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
        
        // Add confirm password validation in real-time
        confirmPasswordInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (passwordInput.getText().length() > 0 && s.length() > 0) {
                    if (!s.toString().equals(passwordInput.getText().toString())) {
                        confirmPasswordLayout.setError("Passwords do not match");
                    } else {
                        confirmPasswordLayout.setError(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private boolean validateInput() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();
        boolean isValid = true;

        // Reset errors
        emailLayout.setError(null);
        passwordLayout.setError(null);
        confirmPasswordLayout.setError(null);

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
        
        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            confirmPasswordLayout.setError("Please confirm your password");
            confirmPasswordLayout.startAnimation(shake);
            isValid = false;
        } else if (!confirmPassword.equals(password)) {
            confirmPasswordLayout.setError("Passwords do not match");
            confirmPasswordLayout.startAnimation(shake);
            isValid = false;
        }
        
        // Check terms and conditions
        if (!termsCheckbox.isChecked()) {
            termsCheckbox.startAnimation(shake);
            Snackbar.make(mainLayout, "Please accept the terms and conditions", Snackbar.LENGTH_SHORT).show();
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
        confirmPasswordInput.setEnabled(!isLoading);
        loginRedirectText.setEnabled(!isLoading);
        termsCheckbox.setEnabled(!isLoading);
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

    private void updatePasswordStrength(String password) {
        int score = 0;
        
        if (password.length() > 0) {
            // Basic score based on length
            score = Math.min(20, password.length() * 4);
            
            // Add score for complexity
            if (password.matches(".*[A-Z].*")) score += 20; // Uppercase
            if (password.matches(".*[a-z].*")) score += 20; // Lowercase
            if (password.matches(".*[0-9].*")) score += 20; // Digits
            if (password.matches(".*[^A-Za-z0-9].*")) score += 20; // Special characters
            
            // Update progress bar
            passwordStrengthBar.setProgress(score);
            
            // Update color based on strength
            if (score < 40) {
                passwordStrengthBar.setProgressTintList(android.content.res.ColorStateList.valueOf(
                        getResources().getColor(R.color.rom_error)));
            } else if (score < 70) {
                passwordStrengthBar.setProgressTintList(android.content.res.ColorStateList.valueOf(
                        getResources().getColor(R.color.rom_warning)));
            } else {
                passwordStrengthBar.setProgressTintList(android.content.res.ColorStateList.valueOf(
                        getResources().getColor(R.color.rom_success)));
            }
        } else {
            passwordStrengthBar.setProgress(0);
        }
    }
}
