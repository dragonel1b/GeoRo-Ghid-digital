package com.example.myapplication.RomApp;

import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.widget.CheckBox;
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
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
    private MaterialButton loginButton, skipButton;
    private CircularProgressIndicator progressBar;
    private Animation fadeIn, scaleUp, slideInUp, buttonBounce, shake, iconHoverScale, confettiAnim;
    private AnimationDrawable gradientAnimation;
    private AnimationDrawable confettiDrawable;
    private int loginAttempts = 0;
    private FirebaseAuth mAuth;

    private SharedPreferences sharedPref;
    private CheckBox rememberMeCheckbox;
    
    // Flag pentru a verifica dacă utilizatorul vine de la submiterea unei sugestii
    private boolean isFromSuggestion = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verificăm dacă utilizatorul vine de la trimiterea unei sugestii
        isFromSuggestion = getIntent().getBooleanExtra("FROM_SUGGESTION", false);

        sharedPref = getSharedPreferences("login_prefs", MODE_PRIVATE);
        boolean rememberMe = sharedPref.getBoolean("remember_me", false);

        if (rememberMe) {
            // Skip login if remembered
            startActivity(new Intent(this, UserActivity.class));
            finish();
        }
        setContentView(R.layout.activity_log_in);

        mAuth = FirebaseAuth.getInstance();

        initializeViews();
        initializeAnimations();
        setupGradientBackground();
        setupConfetti();
        setupClickListeners();
        startEntryAnimations();

        // Verificăm dacă activitatea a fost lansată din TuristiActivity
        boolean fromTuristi = getIntent().getBooleanExtra("FROM_TURISTI", false);
        
        // Afișăm un mesaj specific dacă utilizatorul vine după trimiterea unei sugestii
        if (isFromSuggestion) {
            showSuggestionThanksMessage();
        } else if (fromTuristi) {
            // Afișăm un mesaj de bun venit diferit dacă vine direct din ghid
            welcomeAfterGuide();
        }
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
        
        // Inițializăm butonul Skip
        skipButton = findViewById(R.id.buttonSkip);
        if (skipButton == null) {
            // Dacă butonul nu există în layout, îl creăm programatic
            skipButton = new MaterialButton(this);
            skipButton.setId(View.generateViewId());
            skipButton.setText("Mai târziu");
            skipButton.setTextColor(getResources().getColor(android.R.color.white));
            skipButton.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            skipButton.setAlpha(0.7f);
            
            // Adăugăm butonul în layout sub loginButton
            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.topToBottom = loginButton.getId();
            layoutParams.startToStart = loginButton.getId();
            layoutParams.endToEnd = loginButton.getId();
            layoutParams.topMargin = 16;
            
            mainLayout.addView(skipButton, layoutParams);
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
        logoImage.startAnimation(scaleUp);
        welcomeText.startAnimation(slideInUp);

        // Delay the fade-in of input fields
        emailLayout.setAlpha(0f);
        passwordLayout.setAlpha(0f);
        loginButton.setAlpha(0f);
        signUpLink.setAlpha(0f);
        forgotPasswordLink.setAlpha(0f);
        skipButton.setAlpha(0f);

        emailLayout.animate().alpha(1f).setDuration(1000).setStartDelay(500);
        passwordLayout.animate().alpha(1f).setDuration(1000).setStartDelay(700);
        loginButton.animate().alpha(1f).setDuration(1000).setStartDelay(900);
        skipButton.animate().alpha(0.7f).setDuration(1000).setStartDelay(900);
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
                // Navigate to UserActivity after successful login with clear task
                Intent intent = new Intent(LoginActivity.this, UserActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("FROM_SUCCESSFUL_LOGIN", true); // Indicăm că login-ul a fost cu succes
                startActivity(intent);
                overridePendingTransition(R.anim.zoom_exit, R.anim.fade_out);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    private void setupClickListeners() {
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox);

        loginButton.setOnClickListener(v -> {
            v.startAnimation(buttonBounce);
            v.setEnabled(false); // Disable button during login attempt

            // Save remember me preference before attempting login
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("remember_me", rememberMeCheckbox.isChecked());
            editor.apply();

            attemptLogin();

            // Re-enable button after short delay
            v.postDelayed(() -> v.setEnabled(true), 2000);
        });

        signUpLink.setOnClickListener(v -> {
            v.startAnimation(buttonBounce);
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_slide_in, R.anim.page_swipe_blur);
            finish();
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
        
        // Adăugăm listener pentru butonul "Mai târziu"
        skipButton.setOnClickListener(v -> {
            v.startAnimation(buttonBounce);
            showSkipDialog();
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
    
    /**
     * Procesează încercarea de conectare, verificând datele introduse și
     * autentificând utilizatorul cu Firebase
     */
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
        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);

        // Attempt Firebase login
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Show confetti and then navigate
                        progressBar.setVisibility(View.GONE);
                        // Save successful login state if "Remember Me" was checked
                        if (rememberMeCheckbox.isChecked()) {
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putBoolean("remember_me", true);
                            editor.apply();
                        }
                        
                        // Save the user ID in UserPrefs for PointsManager to use
                        String userId = mAuth.getCurrentUser().getUid();
                        SharedPreferences userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        userPrefs.edit()
                            .putString("current_user_id", userId)
                            .apply();
                            
                        showSuccessConfetti();
                    } else {
                        // Login failed
                        progressBar.setVisibility(View.GONE);
                        loginButton.setEnabled(true);
                        String error = "Authentication failed";
                        if (task.getException() != null) {
                            error = task.getException().getMessage();
                            if (error.contains("password is invalid")) {
                                error = "Invalid password";
                            } else if (error.contains("no user record")) {
                                error = "Account not found";
                            } else if (error.contains("network error")) {
                                error = "Network error - check connection";
                            }
                        }
                        Snackbar.make(mainLayout, error, Snackbar.LENGTH_LONG).show();
                        emailLayout.startAnimation(shake);
                        passwordLayout.startAnimation(shake);
                    }
                });
    }
    
    /**
     * Afișează un dialog de confirmare când utilizatorul alege să continue fără login
     */
    private void showSkipDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Atenție")
               .setMessage("Dacă continui fără cont, nu vei putea folosi toate utilitățile aplicației, iar progresul tău nu va putea fi salvat. Funcționalitățile de explorare și hărți interactive vor fi limitate. Ești sigur?")
               .setCancelable(false)
               .setPositiveButton("Da, continuă", (dialog, id) -> {
                   // Redirecționează către UserActivity fără autentificare
                   skipToMainActivity();
               })
               .setNegativeButton("Nu, mă întorc", (dialog, id) -> {
                   dialog.dismiss();
               });
        
        // Creează un efect de pulsare pentru dialog
        androidx.appcompat.app.AlertDialog alert = builder.create();
        alert.show();
        
        // Personalizează butonul de confirmare
        alert.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.rom_warning));
    }
    
    /**
     * Navighează către UserActivity fără autentificare
     */
    private void skipToMainActivity() {
        // Save a default user ID for anonymous users
        SharedPreferences userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userPrefs.edit()
            .putString("current_user_id", "anonymous_" + System.currentTimeMillis())
            .apply();
            
        Intent intent = new Intent(LoginActivity.this, UserActivity.class);
        intent.putExtra("SKIP_LOGIN", true);
        startActivity(intent);
        
        // Afișăm un mesaj Toast despre limitările modului fără autentificare
        Toast.makeText(this, "Funcționalitățile aplicației vor fi limitate în modul fără autentificare", Toast.LENGTH_LONG).show();
        
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
    
    /**
     * Afișează un mesaj de mulțumire după trimiterea unei sugestii
     */
    private void showSuggestionThanksMessage() {
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            Snackbar.make(rootView, 
                "Mulțumim pentru sugestie! Conectează-te pentru a-ți salva progresul.", 
                Snackbar.LENGTH_LONG)
                .show();
        }
    }
    
    /**
     * Personalizează mesajul de bun venit când utilizatorul vine din ghid
     */
    private void welcomeAfterGuide() {
        TextView welcomeText = findViewById(R.id.textViewLogin);
        if (welcomeText != null) {
            welcomeText.setText("Continuă aventura!");
        }
    }
    
    @Override
    public void onBackPressed() {
        // Verificăm dacă utilizatorul a venit din TuristiActivity
        boolean fromTuristi = getIntent().getBooleanExtra("FROM_TURISTI", false);
        
        if (fromTuristi) {
            // Îl trimitem înapoi la ghid
            Intent intent = new Intent(this, TuristiActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        } else {
            // Comportament normal
            super.onBackPressed();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
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
