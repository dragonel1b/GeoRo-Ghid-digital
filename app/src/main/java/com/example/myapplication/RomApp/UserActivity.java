package com.example.myapplication.RomApp;

import android.annotation.SuppressLint;
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
import com.google.firebase.auth.FirebaseAuth;
import android.content.SharedPreferences;
import com.google.android.material.snackbar.Snackbar;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;

import java.util.Objects;
import android.util.Log;

public class UserActivity extends AppCompatActivity {
    private static final String TAG = "UserActivity";
    private CardView welcomeCard;
    private GridLayout gridLayoutRegions;
    private MaterialButton logoutButton;
    private Handler handler = new Handler();
    private Handler colorHandler = new Handler();
    private ConstraintLayout mainLayout;
    private AnimationDrawable gradientAnimation;
    private static final int COLOR_CHANGE_DELAY = 15000; // 15 seconds
    private int currentColorSet = 0;
    private boolean isUserLoggedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Initialize views first
        mainLayout = findViewById(R.id.mainWelcome);
        setupGradientBackground();
        welcomeCard = findViewById(R.id.welcomeCard);
        gridLayoutRegions = findViewById(R.id.gridLayoutRegions);
        logoutButton = findViewById(R.id.buttonWelcome);

        // Check authentication status with a slight delay to allow Firebase to initialize
        checkAuthenticationStatus();

        // Inițializăm butoanele pentru clasament și profil
        setupActionButtons();

        // Apply animations
        applyEntranceAnimations();

        // Set up button click animations
        setupButtonAnimations();

        // Start color cycling
        startColorCycling();
    }

    /**
     * Verifică starea de autentificare cu un mic întârziat pentru a permite Firebase să se inițializeze
     */
    private void checkAuthenticationStatus() {
        // Verificăm dacă utilizatorul a sărit peste autentificare
        boolean skipLogin = getIntent().getBooleanExtra("SKIP_LOGIN", false);
        boolean fromSuccessfulLogin = getIntent().getBooleanExtra("FROM_SUCCESSFUL_LOGIN", false);
        
        Log.d(TAG, "Checking authentication status - skipLogin: " + skipLogin + ", fromSuccessfulLogin: " + fromSuccessfulLogin);
        
        if (skipLogin) {
            Log.d(TAG, "User skipped login - setting limited functionality");
            isUserLoggedIn = false;
            showLimitedFunctionalityWarning();
            return;
        }
        
        if (fromSuccessfulLogin) {
            // Dacă vine dintr-un login cu succes, marcăm ca fiind autentificat
            Log.d(TAG, "User came from successful login - setting authenticated");
            isUserLoggedIn = true;
            updateUIForLoggedInUser();
            return;
        }

        // Pentru alte cazuri, verificăm cu Firebase cu un mic întârziat
        Log.d(TAG, "Checking Firebase auth status with delay...");
        handler.postDelayed(() -> {
            isUserLoggedIn = FirebaseAuth.getInstance().getCurrentUser() != null;
            Log.d(TAG, "Firebase auth check result: " + isUserLoggedIn);
            
            if (!isUserLoggedIn) {
                Log.d(TAG, "User not authenticated - showing limited functionality");
                showLimitedFunctionalityWarning();
            } else {
                Log.d(TAG, "User authenticated - updating UI for logged in user");
                updateUIForLoggedInUser();
            }
        }, 500); // Așteaptă 500ms pentru ca Firebase să își stabilească sesiunea
    }

    /**
     * Inițializează și configurează butoanele pentru clasament și profil
     */
    private void setupActionButtons() {
        MaterialButton leaderboardButton = findViewById(R.id.leaderboardButton);
        MaterialButton profileButton = findViewById(R.id.profileButton);
        
        // Adăugăm animații pentru butoane
        Animation pressAnim = AnimationUtils.loadAnimation(this, R.anim.button_press);
        
        if (leaderboardButton != null) {
            leaderboardButton.setOnClickListener(v -> {
                v.startAnimation(pressAnim);
                handler.postDelayed(() -> openLeaderboard(), 150);
            });
        }
        
        if (profileButton != null) {
            profileButton.setOnClickListener(v -> {
                v.startAnimation(pressAnim);
                handler.postDelayed(() -> openUserProfile(), 150);
            });
        }
    }
    
    /**
     * Deschide activitatea de clasament (leaderboard)
     */
    private void openLeaderboard() {
        Log.d(TAG, "Opening leaderboard - User logged in: " + isUserLoggedIn);
        
        // Verificăm dacă utilizatorul este autentificat pentru funcționalități complete
        if (!isUserLoggedIn) {
            Log.d(TAG, "User not logged in - showing login required dialog for Leaderboard");
            // Afișăm dialog de avertizare/login pentru utilizatorii neautentificați
            showLoginRequiredDialog("Clasament");
            return;
        }
        
        try {
            Log.d(TAG, "Starting LeaderboardActivity");
            // Creăm intent-ul și lansăm activitatea pe un thread separat
            // pentru a evita violările StrictMode
            Intent intent = new Intent(this, com.example.myapplication.ui.LeaderboardActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } catch (Exception e) {
            Log.e(TAG, "Error opening leaderboard", e);
            // Folosim Snackbar în loc de Toast pentru a evita operațiuni pe disc
            // Toast-urile pot cauza DiskReadViolation în StrictMode
            Snackbar.make(findViewById(android.R.id.content), 
                    "Eroare la deschiderea clasamentului", 
                    Snackbar.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Deschide activitatea de profil utilizator
     */
    private void openUserProfile() {
        Log.d(TAG, "Opening user profile - User logged in: " + isUserLoggedIn);
        
        // Verificăm dacă utilizatorul este autentificat pentru a accesa profilul
        if (!isUserLoggedIn) {
            Log.d(TAG, "User not logged in - showing login required dialog for Profile");
            // Afișăm dialog de avertizare/login pentru utilizatorii neautentificați
            showLoginRequiredDialog("Profil");
            return;
        }
        
        try {
            Log.d(TAG, "Starting UserProfileActivity");
            // Creăm intent-ul și lansăm activitatea
            Intent intent = new Intent(this, com.example.myapplication.ui.UserProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } catch (Exception e) {
            Log.e(TAG, "Error opening user profile", e);
            // Folosim Snackbar în loc de Toast pentru a evita operațiuni pe disc
            Snackbar.make(findViewById(android.R.id.content), 
                    "Eroare la deschiderea profilului", 
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    private void applyEntranceAnimations() {
        // Initially hide all buttons
        for (int i = 0; i < gridLayoutRegions.getChildCount(); i++) {
            gridLayoutRegions.getChildAt(i).setVisibility(View.INVISIBLE);
        }
        logoutButton.setVisibility(View.INVISIBLE);
        
        // Ascundem inițial și butoanele de clasament și profil
        View bottomButtonsLayout = findViewById(R.id.bottomButtonsLayout);
        if (bottomButtonsLayout != null) {
            bottomButtonsLayout.setVisibility(View.INVISIBLE);
        }

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

        // Animăm butoanele de clasament și profil după butoanele regiunilor
        handler.postDelayed(() -> {
            if (bottomButtonsLayout != null) {
                bottomButtonsLayout.setVisibility(View.VISIBLE);
                Animation slideUpAnim = AnimationUtils.loadAnimation(this, R.anim.slide_in_up);
                bottomButtonsLayout.startAnimation(slideUpAnim);
            }
        }, 1200); // După ce majoritatea butoanelor de regiuni au apărut

        // Logout button animation (ultimul element animat)
        handler.postDelayed(() -> {
            logoutButton.setVisibility(View.VISIBLE);
            Animation buttonAnim = AnimationUtils.loadAnimation(this, R.anim.slide_in_up);
            logoutButton.startAnimation(buttonAnim);
        }, 1500); // After all other buttons
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

    @SuppressLint("UseCompatLoadingForDrawables")
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
                            button.setBackground(Objects.requireNonNull(getDrawable(backgroundRes)));

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

        // Obținem ID-ul butonului apăsat pentru a determina regiunea
        int id = view.getId();
        if (id == R.id.buttonTransilvania) {
            intent.setClass(this, Transilvania.class);
            region = "Transilvania";
        } else if (id == R.id.buttonMoldova) {
            intent.setClass(this, Moldova.class);
            region = "Moldova";
        } else if (id == R.id.buttonBucovina) {
            intent.setClass(this, Bucovina.class);
            region = "Bucovina";
        } else if (id == R.id.buttonOltenia) {
            intent.setClass(this, Oltenia.class);
            region = "Oltenia";
        } else if (id == R.id.buttonDobrogea) {
            intent.setClass(this, Dobrogea.class);
            region = "Dobrogea";
        } else if (id == R.id.buttonMuntenia) {
            intent.setClass(this, Muntenia.class);
            region = "Muntenia";
        } else if (id == R.id.buttonBanat) {
            intent.setClass(this, Banat.class);
            region = "Banat";
        } else if (id == R.id.buttonCrisana) {
            intent.setClass(this, Crisana.class);
            region = "Crișana";
        } else if (id == R.id.buttonMaramures) {
            intent.setClass(this, Maramures.class);
            region = "Maramureș";
        } else if (id == R.id.buttonRom) {
            // Pentru joc, verificăm doar dacă utilizatorul are cont creat
            if (!isUserLoggedIn) {
                // Utilizatorul nu are cont creat, afișăm un dialog de avertizare
                showLoginRequiredDialog("Mini Game");
                return; // Nu continuăm cu navigarea
            }
            intent.setClass(this, RomMainActivity.class);
            region = "Mini Game";
        } else {
            return;
        }
        
        // Pentru regiuni, permitem accesul tuturor utilizatorilor
        if (id != R.id.buttonRom) {
            Toast.makeText(this, "Explorezi regiunea " + region, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Accesezi " + region, Toast.LENGTH_SHORT).show();
        }
        
        // Adăugăm tranziție elegantă
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void showLoginRequiredDialog(String region) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.RomDialogStyle);
        builder.setTitle("Autentificare necesară")
               .setMessage("Pentru a explora regiunea " + region + " și pentru a accesa toate funcționalitățile aplicației, trebuie să fii conectat. Doriți să vă conectați acum?")
               .setPositiveButton("Conectare", (dialog, which) -> {
                   // Redirecționăm utilizatorul către ecranul de autentificare
                   Intent loginIntent = new Intent(UserActivity.this, LoginActivity.class);
                   startActivity(loginIntent);
                   overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
               })
               .setNegativeButton("Anulare", (dialog, which) -> {
                   dialog.dismiss();
                   // Afișăm un mesaj suplimentar despre limitările modului neautentificat
                   Toast.makeText(UserActivity.this, "Funcționalități limitate în modul neautentificat", Toast.LENGTH_SHORT).show();
               })
               .setIcon(android.R.drawable.ic_dialog_alert)
               .show();
    }

    private void handleLogout() {
        // Clear any saved preferences
        SharedPreferences sharedPref = getSharedPreferences("login_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("remember_me", false);
        editor.apply();

        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut();

        // Redirect to login screen
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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
        
        // Verificăm din nou starea de autentificare (în cazul în care utilizatorul s-a conectat între timp)
        boolean previousState = isUserLoggedIn;
        boolean currentFirebaseState = FirebaseAuth.getInstance().getCurrentUser() != null;
        
        Log.d(TAG, "onResume - Previous state: " + previousState + ", Current Firebase state: " + currentFirebaseState + ", Current app state: " + isUserLoggedIn);
        
        // Actualizăm starea locală cu starea reală din Firebase
        isUserLoggedIn = currentFirebaseState;
        
        // Dacă starea s-a schimbat (utilizatorul s-a conectat), actualizăm interfața
        if (!previousState && isUserLoggedIn) {
            Log.d(TAG, "User state changed from not authenticated to authenticated - updating UI");
            updateUIForLoggedInUser();
            
            // Sincronizăm punctele cu Firebase
            syncPointsWithFirebase();
        } else if (previousState && !isUserLoggedIn) {
            Log.d(TAG, "User state changed from authenticated to not authenticated - showing limited functionality");
            Log.d(TAG, "This likely happened due to logout from another activity (e.g., UserProfileActivity)");
            showLimitedFunctionalityWarning();
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
    
    /**
     * Afișează un banner de avertizare când utilizatorul folosește aplicația fără autentificare
     */
    private void showLimitedFunctionalityWarning() {
        Log.d(TAG, "Showing limited functionality warning");
        
        // Afișăm cardView-ul de avertizare
        CardView warningBanner = findViewById(R.id.warningBanner);
        if (warningBanner != null) {
            warningBanner.setVisibility(View.VISIBLE);
            
            // Animăm apariția banner-ului
            warningBanner.setAlpha(0f);
            warningBanner.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .setStartDelay(500)
                    .start();
            
            // Configurăm butonul de conectare din banner
            Button loginFromWarningButton = findViewById(R.id.loginFromWarningButton);
            if (loginFromWarningButton != null) {
                loginFromWarningButton.setOnClickListener(v -> {
                    Intent intent = new Intent(UserActivity.this, LoginActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                });
            }
        }
        
        // Afișăm un avertisment global pentru utilizatorii neautentificați
        Snackbar.make(mainLayout, 
                "Acces limitat! Conectați-vă pentru a utiliza toate funcționalitățile aplicației.", 
                Snackbar.LENGTH_LONG)
                .setAction("Conectare", v -> {
                    Intent loginIntent = new Intent(UserActivity.this, LoginActivity.class);
                    startActivity(loginIntent);
                })
                .show();
        
        // Schimbăm textul butonului de logout în "Conectare"
        if (logoutButton != null) {
            logoutButton.setText("Conectare");
            
            // Modificăm comportamentul butonului pentru a deschide ecranul de login
            logoutButton.setOnClickListener(v -> {
                v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
                handler.postDelayed(() -> {
                    Intent intent = new Intent(UserActivity.this, LoginActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                }, 150);
            });
        }
        
        Log.d(TAG, "Limited functionality warning shown successfully");
    }
    
    /**
     * Actualizează interfața pentru utilizatorii autentificați
     */
    private void updateUIForLoggedInUser() {
        Log.d(TAG, "Updating UI for logged in user");
        
        // Ascundem banner-ul de avertizare
        CardView warningBanner = findViewById(R.id.warningBanner);
        if (warningBanner != null) {
            warningBanner.setVisibility(View.GONE);
        }
        
        // Actualizăm textul butonului de deconectare
        if (logoutButton != null) {
            logoutButton.setText("Deconectare");
            
            // Resetăm comportamentul butonului pentru deconectare
            logoutButton.setOnClickListener(v -> {
                v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
                handler.postDelayed(() -> handleLogout(), 150);
            });
        }
        
        // Afișăm un mesaj de bun venit
        Toast.makeText(this, "Bine ai revenit! Acum ai acces la toate funcționalitățile.", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "UI updated for logged in user successfully");
    }
    
    /**
     * Sincronizează punctele utilizatorului cu Firebase
     */
    private void syncPointsWithFirebase() {
        // Mai întâi încărcăm punctele din Firebase
        PointsManager.getInstance(this).loadPointsFromFirebase(this);
        
        // Apoi sincronizăm punctele locale cu Firebase
        handler.postDelayed(() -> {
            PointsManager.getInstance(this).syncAllPointsWithFirebase(this);
        }, 2000); // Întârziere pentru a permite încărcarea punctelor din Firebase
    }
}
