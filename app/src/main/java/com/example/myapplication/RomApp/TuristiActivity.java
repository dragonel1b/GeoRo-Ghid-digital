package com.example.myapplication.RomApp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.myapplication.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

public class TuristiActivity extends AppCompatActivity {

    private TextInputEditText suggestionInput;
    private Button submitSuggestionButton;
    private Button continueToMainButton;
    private TextView welcomeText;
    private CardView welcomeCard;
    
    private static final String PREFS_NAME = "AppSuggestions";
    private static final String SUGGESTIONS_KEY = "user_suggestions";
    private static final String FIRST_TIME_KEY = "first_time_visitor";
    
    private boolean isFromSplash = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_turisti);
        
        // Verificăm dacă activitatea a fost lansată din splash screen
        isFromSplash = getIntent().getBooleanExtra("FROM_SPLASH", false);
        
        // Inițializăm componentele UI
        suggestionInput = findViewById(R.id.suggestionInput);
        submitSuggestionButton = findViewById(R.id.submitSuggestionButton);
        welcomeText = findViewById(R.id.textViewSubtitle);
        
        // Adăugăm butonul pentru a continua către MainActivity
        setupContinueButton();
        
        // Get the root ConstraintLayout
        ConstraintLayout rootLayout = findViewById(R.id.root);
        if (rootLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
        
        // Configurăm butonul de trimitere a sugestiilor
        if (submitSuggestionButton != null) {
            submitSuggestionButton.setOnClickListener(v -> {
                saveSuggestion();
            });
        }
        
        // Afișăm un mesaj special pentru utilizatorii care vin din splash screen
        if (isFromSplash) {
            displayWelcomeMessage();
        }
        
        // Aplicăm animații pentru elementele din interfață
        animateInterfaceElements();
        
        // Verificăm dacă este prima vizită a utilizatorului
        checkFirstTimeVisit();
    }
    
    /**
     * Configurează butonul de continuare către MainActivity
     */
    private void setupContinueButton() {
        continueToMainButton = findViewById(R.id.buttonBack);
        if (continueToMainButton != null) {
            continueToMainButton.setText("Continuă către aplicație");
            continueToMainButton.setOnClickListener(v -> {
                navigateToMainActivity();
            });
        }
    }
    
    /**
     * Navighează către MainActivity cu animație
     */
    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        // Adăugăm un flag care indică că vine din activitatea Turiști
        intent.putExtra("FROM_TURISTI", true);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        
        // Închidem această activitate doar dacă vine din splash
        if (isFromSplash) {
            finish();
        }
    }
    
    /**
     * Afișează un mesaj de bun venit personalizat
     */
    private void displayWelcomeMessage() {
        if (welcomeText != null) {
            welcomeText.setText("Bine ai venit în aplicația noastră! Descoperă frumusețile României");
            
            // Aplicăm o animație pentru textul de bun venit
            Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse_animation);
            welcomeText.startAnimation(pulse);
        }
        
        // Afișăm un Snackbar cu mesaj de bun venit
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            Snackbar.make(rootView, "Bine ai venit! Descoperă ce poți face în aplicație", Snackbar.LENGTH_LONG)
                    .setAction("OK", v -> {})
                    .show();
        }
    }
    
    /**
     * Verifică dacă este prima vizită a utilizatorului
     */
    private void checkFirstTimeVisit() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstTime = prefs.getBoolean(FIRST_TIME_KEY, true);
        
        if (isFirstTime) {
            // Este prima vizită, salvăm acest lucru
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(FIRST_TIME_KEY, false);
            editor.apply();
            
            // Afișăm un mesaj special pentru prima vizită
            Toast.makeText(this, "Bun venit! Aceasta este prima ta vizită în aplicație.", Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Animează elementele din interfață pentru o experiență mai interactivă
     */
    private void animateInterfaceElements() {
        // Animăm cardurile din interfață
        animateCard(R.id.headerCard, 100);
        animateCard(R.id.introCard, 200);
        animateCard(R.id.exploreFeatureCard, 300);
        animateCard(R.id.photoFeatureCard, 400);
        animateCard(R.id.notesFeatureCard, 500);
        animateCard(R.id.pointsFeatureCard, 600);
        animateCard(R.id.suggestionCard, 700);
    }
    
    /**
     * Animează un card cu o întârziere specifică
     */
    private void animateCard(int cardId, long delay) {
        CardView card = findViewById(cardId);
        if (card != null) {
            card.setAlpha(0f);
            card.setTranslationY(50f);
            card.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(delay)
                .setDuration(500)
                .start();
        }
    }
    
    /**
     * Salvează sugestia utilizatorului și afișează un mesaj de confirmare
     */
    private void saveSuggestion() {
        if (suggestionInput != null && suggestionInput.getText() != null) {
            String suggestion = suggestionInput.getText().toString().trim();
            
            if (!suggestion.isEmpty()) {
                // Salvăm sugestia în SharedPreferences
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                String existingSuggestions = prefs.getString(SUGGESTIONS_KEY, "");
                
                // Adăugăm noua sugestie la cele existente
                String newSuggestions;
                if (existingSuggestions.isEmpty()) {
                    newSuggestions = suggestion;
                } else {
                    newSuggestions = existingSuggestions + "||" + suggestion;
                }
                
                // Salvăm lista actualizată
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(SUGGESTIONS_KEY, newSuggestions);
                editor.apply();
                
                // Resetăm câmpul de input și afișăm un mesaj de confirmare
                suggestionInput.setText("");
                
                // Folosim Snackbar în loc de Toast pentru un aspect mai modern
                View rootView = findViewById(android.R.id.content);
                if (rootView != null) {
                    Snackbar snackbar = Snackbar.make(rootView, "Mulțumim pentru sugestie! O vom analiza curând.", Snackbar.LENGTH_LONG);
                    snackbar.setAction("OK", v -> {
                        // După ce utilizatorul confirmă, îl redirecționăm către login
                        redirectToLogin();
                    });
                    snackbar.addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            super.onDismissed(snackbar, event);
                            // După ce Snackbar dispare, îl redirecționăm către login
                            redirectToLogin();
                        }
                    });
                    snackbar.show();
                } else {
                    Toast.makeText(this, "Mulțumim pentru sugestie! O vom analiza curând.", Toast.LENGTH_LONG).show();
                    // Redirecționăm către login după un scurt delay
                    new Handler().postDelayed(this::redirectToLogin, 2000);
                }
            } else {
                Toast.makeText(this, "Te rugăm să introduci o sugestie înainte de a o trimite.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    /**
     * Redirecționează utilizatorul către activitatea de login
     */
    private void redirectToLogin() {
        // Verificăm dacă nu suntem deja în proces de tranzacție
        if (isFinishing()) return;
        
        // Creăm intent pentru activitatea de logare
        Intent intent = new Intent(this, LoginActivity.class);
        // Adăugăm un flag care indică că vine de la submiterea unei sugestii
        intent.putExtra("FROM_SUGGESTION", true);
        // Adăugăm și flag-ul care indică că vine din activitatea Turiști
        intent.putExtra("FROM_TURISTI", true);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        
        // Nu închidem această activitate, pentru a permite utilizatorului să revină dacă apasă Back
    }
    
    @Override
    public void onBackPressed() {
        // Verificăm dacă utilizatorul a venit din splash screen
        if (isFromSplash) {
            // Dacă a venit din splash, atunci îl redirecționăm către LoginActivity când apasă Back
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("FROM_TURISTI", true);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            // Nu închidem această activitate, pentru a permite utilizatorului să revină
        } else {
            // Comportament normal pentru Back
            super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }

    /**
     * Metodă apelată de butonul Înapoi pentru a reveni la ecranul anterior
     * (Păstrată pentru compatibilitate, dar redenumită ca buton de continuare)
     */
    public void goBack(View view) {
        // Dacă utilizatorul a venit din splash, îl trimitem la login
        if (isFromSplash) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("FROM_TURISTI", true);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        } else {
            // Dacă nu a venit din splash, îl trimitem la MainActivity
            navigateToMainActivity();
        }
    }
}