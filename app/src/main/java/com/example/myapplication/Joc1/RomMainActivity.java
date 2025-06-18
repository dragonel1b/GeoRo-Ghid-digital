package com.example.myapplication.Joc1;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.RomApp.LoginActivity;
import com.example.myapplication.RomApplication;
import com.example.myapplication.TaraTara.TaraTaraVremOstasi;
import com.example.myapplication.security.SecurityManager;
import com.example.myapplication.utils.TransitionHelper;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class RomMainActivity extends AppCompatActivity {
    private static final String TAG = "RomMainActivity";
    
    private TextView fuelText, moneyText, foodText, culturePointsText;
    private MaterialCardView resourcePanel;
    private FloatingActionButton achievementsFab;
    private RomGameState gameState;
    private Handler uiHandler = new Handler(Looper.getMainLooper());
    private SecurityManager securityManager;
    private FirebaseAuth mAuth;

    // City data
    private static final String[] CITY_NAMES = {
        "Sibiu", "Cluj", "Brașov", "București", "Iași", "Timișoara",
        "Constanța", "Oradea", "Sinaia", "Suceava", "Alba Iulia", "Târgu Mureș"
    };
    
    private static final int[] CITY_IMAGES = {
        R.drawable.ic_attraction, // Using placeholder icons, replace with actual city images
        R.drawable.ic_attraction,
        R.drawable.ic_attraction,
        R.drawable.ic_attraction,
        R.drawable.ic_attraction,
        R.drawable.ic_attraction,
        R.drawable.ic_attraction,
        R.drawable.ic_attraction,
        R.drawable.ic_attraction,
        R.drawable.ic_attraction,
        R.drawable.ic_attraction,
        R.drawable.ic_attraction
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            // Inițializăm Firebase Auth
            mAuth = FirebaseAuth.getInstance();
            
            // Verificăm dacă utilizatorul are cont creat
            if (mAuth.getCurrentUser() == null) {
                // Utilizatorul nu are cont creat, afișăm un mesaj și redirecționăm către LoginActivity
                Toast.makeText(this, "Trebuie să ai un cont creat pentru a accesa această secțiune", Toast.LENGTH_LONG).show();
                
                // Creăm intent pentru activitatea de login
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish(); // Închidem activitatea curentă
                return; // Ieșim din metoda onCreate pentru a nu continua inițializarea
            }
            
            // Get security manager from application
            securityManager = ((RomApplication) getApplication()).getSecurityManager();
            
            // Perform security audit
            if (!securityManager.performSecurityAudit()) {
                Log.w(TAG, "Security audit failed, showing warning to user");
                showSecurityWarning();
            }
            
            setContentView(R.layout.activity_rom_main);

            // Initialize game state
            gameState = RomGameState.getInstance();
            gameState.initialize(this);

            // Initialize views
            initializeViews();

            // Setup RecyclerView for destinations
            setupDestinationsRecyclerView();

            // Load and display resources
            updateResourceDisplay();

            // Apply animations
            applyEntryAnimations();
            
            // Setup click listener for achievements FAB
            setupAchievementsFAB();
            
        } catch (Exception e) {
            // Handle exceptions through the security manager
            securityManager.handleException(this, e, 
                    getString(R.string.error_initializing_activity), 
                    true);
        }
    }

    private void showSecurityWarning() {
        // Show a security warning to the user
        Snackbar.make(
                findViewById(android.R.id.content),
                "Security warning: Your device may be compromised. Some features may be limited.",
                Snackbar.LENGTH_LONG
        ).show();
    }

    private void initializeViews() {
        fuelText = findViewById(R.id.fuelText);
        moneyText = findViewById(R.id.moneyText);
        foodText = findViewById(R.id.foodText);
        culturePointsText = findViewById(R.id.culturePointsText);
        resourcePanel = findViewById(R.id.resourcePanel);
        achievementsFab = findViewById(R.id.achievementsFab);
    }
    
    private void setupAchievementsFAB() {
        achievementsFab.setOnClickListener(v -> {
            // Aplicăm un efect de feedback tactil
            v.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .withEndAction(() -> {
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start();
                    openAchievements();
                })
                .start();
        });
    }

    private void setupDestinationsRecyclerView() {
        // This entire method is not needed anymore as destinationsRecyclerView is removed
        // Just add a log statement to document the change
        Log.d(TAG, "setupDestinationsRecyclerView: RecyclerView not used in current UI version");
    }

    private void updateResourceDisplay() {
        try {
            fuelText.setText(getString(R.string.rom_fuel_label, (int) gameState.getEsentaCalatoriei()));
            moneyText.setText(getString(R.string.rom_money_label, (int) gameState.getMonedeDacice()));
            foodText.setText(getString(R.string.rom_food_label, (int) gameState.getMerinde()));
            culturePointsText.setText(String.valueOf(gameState.getPuncteIntelepte()));
        } catch (Exception e) {
            securityManager.handleException(this, e, 
                    "Failed to update resources display.", false);
        }
    }

    private void applyEntryAnimations() {
        // Animație de intrare pentru panoul resurselor
        resourcePanel.setAlpha(0f);
        resourcePanel.setTranslationY(-50f);
        resourcePanel.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(300)
                .start();
                
        // Animație pentru FAB
        achievementsFab.setScaleX(0f);
        achievementsFab.setScaleY(0f);
        achievementsFab.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setStartDelay(600)
                .start();
                
        // Remove RecyclerView animation
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        // Update UI for the new orientation - removed destinationsRecyclerView reference
        Log.d(TAG, "Configuration changed: " + (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ? "landscape" : "portrait"));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save any needed instance state here if necessary
        // The gameState is a singleton, so we don't need to save it here
        Log.d(TAG, "onSaveInstanceState called");
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore any necessary state here
        Log.d(TAG, "onRestoreInstanceState called");
        updateResourceDisplay(); // Refresh display with current data
    }

    public void openMapActivity(View view) {
        animateButtonClick(view);
        try {
            Intent intent = new Intent(this, RomMapActivity.class);
            // Validate intent before use
            if (securityManager.validateIntent(intent)) {
                TransitionHelper.startActivityWithSharedElement(this, intent, view, "map_transition");
            } else {
                showErrorMessage("Cannot open map. Invalid intent detected.");
            }
        } catch (Exception e) {
            securityManager.handleException(this, e, 
                    "Failed to open map.", false);
        }
    }

    public void openTarataravremostasiActivity(View view) {
        animateButtonClick(view);
        try {
            Intent intent = new Intent(this, TaraTaraVremOstasi.class);
            // Validate intent before use
            if (securityManager.validateIntent(intent)) {
                TransitionHelper.startActivityWithFade(this, intent);
            } else {
                showErrorMessage("Cannot open activity. Invalid intent detected.");
            }
        } catch (Exception e) {
            securityManager.handleException(this, e, 
                    "Failed to open TaraTaraVremOstasi.", false);
        }
    }

    public void startQuiz(View view) {
        animateButtonClick(view);
        try {
            Intent intent = new Intent(this, RomQuizActivity.class);
            // Validate intent before use
            if (securityManager.validateIntent(intent)) {
                TransitionHelper.startActivityWithSlide(this, intent);
            } else {
                showErrorMessage("Cannot start quiz. Invalid intent detected.");
            }
        } catch (Exception e) {
            securityManager.handleException(this, e, 
                    "Failed to start quiz.", false);
        }
    }

    public void startQuestMode(View view) {
        animateButtonClick(view);
        try {
            Intent intent = new Intent(this, RomQuestActivity.class);
            // Validate intent before use
            if (securityManager.validateIntent(intent)) {
                TransitionHelper.startActivityWithFade(this, intent);
            } else {
                showErrorMessage("Cannot start quest mode. Invalid intent detected.");
            }
        } catch (Exception e) {
            securityManager.handleException(this, e, 
                    "Failed to start quest mode.", false);
        }
    }

    public void startCulinaryMode(View view) {
        animateButtonClick(view);
        try {
            // Try to find the class by name to avoid compile-time dependency
            Class<?> culinaryClass = Class.forName("com.example.myapplication.recipe.ui.RecipeListActivity");
            Intent intent = new Intent(this, culinaryClass);
            
            // Validate intent before use
            if (securityManager.validateIntent(intent)) {
                TransitionHelper.startActivityWithFade(this, intent);
            } else {
                showErrorMessage("Cannot start culinary mode. Invalid intent detected.");
            }
        } catch (ClassNotFoundException e) {
            // Show a friendly message if the culinary activity isn't available
            showErrorMessage("Culinary mode is not available in this version.");
            Log.e(TAG, "Culinary class not found", e);
        } catch (Exception e) {
            securityManager.handleException(this, e, 
                    "Failed to start culinary mode.", false);
        }
    }


    public void startCulinaryWelcome(View view) {
        animateButtonClick(view);
        try {
            // Try to find the class by name to avoid compile-time dependency
            Class<?> culinaryWelcomeClass = Class.forName("com.example.myapplication.recipe.ui.RecipeWelcomeActivity");
            Intent intent = new Intent(this, culinaryWelcomeClass);
            
            // Validate intent before use
            if (securityManager.validateIntent(intent)) {
                TransitionHelper.startActivityWithFade(this, intent);
            } else {
                showErrorMessage("Cannot start culinary welcome. Invalid intent detected.");
            }
        } catch (ClassNotFoundException e) {
            // Redirect to the main culinary activity instead
            showErrorMessage("Welcome activity not available, starting main culinary mode.");
            startCulinaryMode(view);
        } catch (Exception e) {
            securityManager.handleException(this, e, 
                    "Failed to start culinary welcome.", false);
        }
    }

    public void startMinigame(View view) {
        animateButtonClick(view);
        try {
            Intent intent = new Intent(this, MinigameOpenWorldActivity.class);
            // Validate intent before use
            if (securityManager.validateIntent(intent)) {
                TransitionHelper.startActivityWithSlide(this, intent);
            } else {
                showErrorMessage("Cannot start minigame. Invalid intent detected.");
            }
        } catch (ActivityNotFoundException e) {
            securityManager.handleException(this, e, 
                    "Nu s-a putut porni minijocul. Încercați din nou.", false);
        } catch (Exception e) {
            securityManager.handleException(this, e, 
                    "A apărut o eroare neașteptată.", true);
        }
    }

    private void animateButtonClick(View view) {
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start())
                .start();
    }

    private void showErrorMessage(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    public void openAchievements() {
        try {
            Intent intent = new Intent(this, RomAchievementsActivity.class);
            // Validate intent before use
            if (securityManager.validateIntent(intent)) {
                TransitionHelper.startActivityWithFade(this, intent);
            } else {
                showErrorMessage("Cannot open achievements. Invalid intent detected.");
            }
        } catch (Exception e) {
            securityManager.handleException(this, e, 
                    "Failed to open achievements.", false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Perform security check on resume
        securityManager.performSecurityAudit();
        
        updateResourceDisplay();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Remove any callbacks to prevent memory leaks
        if (uiHandler != null) {
            uiHandler.removeCallbacksAndMessages(null);
        }
    }
    
    // DestinationItem data class
    private static class DestinationItem {
        final String name;
        final int imageResId;
        
        DestinationItem(String name, int imageResId) {
            this.name = name;
            this.imageResId = imageResId;
        }
    }
    
    // RecyclerView adapter for destinations
    private class DestinationsAdapter extends RecyclerView.Adapter<DestinationsAdapter.ViewHolder> {
        private final List<DestinationItem> items;
        
        DestinationsAdapter(List<DestinationItem> items) {
            this.items = items;
        }
        
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            MaterialCardView cardView = new MaterialCardView(parent.getContext());
            
            // Set card layout parameters
            int cardMargin = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
            int cardPadding = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
            float cardRadius = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());
            float cardElevation = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
                    
            RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 
                    (int) getResources().getDimension(R.dimen.destination_card_height));
            layoutParams.setMargins(cardMargin, cardMargin, cardMargin, cardMargin);
            cardView.setLayoutParams(layoutParams);
            
            // Style the card
            cardView.setCardBackgroundColor(getResources().getColor(R.color.rom_card_background, getTheme()));
            cardView.setRadius(cardRadius);
            cardView.setCardElevation(cardElevation);
            cardView.setContentPadding(cardPadding, cardPadding, cardPadding, cardPadding);
            
            // Add ripple effect
            cardView.setClickable(true);
            cardView.setFocusable(true);
            
            // Add content layout
            LinearLayout contentLayout = new LinearLayout(parent.getContext());
            contentLayout.setOrientation(LinearLayout.VERTICAL);
            contentLayout.setGravity(Gravity.CENTER);
            contentLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            
            // Add image view for city image
            ImageView imageView = new ImageView(parent.getContext());
            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                    (int) getResources().getDimension(R.dimen.icon_size_large),
                    (int) getResources().getDimension(R.dimen.icon_size_large));
            imageView.setLayoutParams(imageParams);
            
            // Add text view for city name
            TextView textView = new TextView(parent.getContext());
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            textParams.topMargin = (int) getResources().getDimension(R.dimen.margin_medium);
            textView.setLayoutParams(textParams);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_size_normal));
            textView.setTextColor(getResources().getColor(android.R.color.black, getTheme()));
            
            // Add views to layout
            contentLayout.addView(imageView);
            contentLayout.addView(textView);
            
            cardView.addView(contentLayout);
            
            return new ViewHolder(cardView, imageView, textView);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DestinationItem item = items.get(position);
            holder.imageView.setImageResource(item.imageResId);
            holder.textView.setText(item.name);
            
            // Set click listener for the card with animation
            holder.cardView.setOnClickListener(v -> {
                try {
                    // Animație de feedback pentru click
                    v.animate()
                            .scaleX(0.95f)
                            .scaleY(0.95f)
                            .setDuration(100)
                            .withEndAction(() -> {
                                v.animate()
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .setDuration(100)
                                        .start();
                                
                                try {
                                    // Ensure city name is safe before displaying
                                    String safeCityName = securityManager.sanitizeInput(item.name);
                                    
                                    // Afișează un Snackbar în loc de Toast pentru o experiență mai bună
                                    Snackbar.make(findViewById(android.R.id.content), 
                                            "Călătorești spre " + safeCityName, 
                                            Snackbar.LENGTH_SHORT).show();
                                    
                                    // Example: Add some culture points when visiting a city
                                    gameState.addPuncteIntelepte(5, RomMainActivity.this);
                                    updateResourceDisplay();
                                    
                                    // Animație de actualizare pentru scorul afișat
                                    culturePointsText.animate()
                                            .scaleX(1.2f)
                                            .scaleY(1.2f)
                                            .setDuration(200)
                                            .withEndAction(() -> {
                                                culturePointsText.animate()
                                                        .scaleX(1f)
                                                        .scaleY(1f)
                                                        .setDuration(200)
                                                        .start();
                                            })
                                            .start();
                                } catch (Exception e) {
                                    securityManager.handleException(RomMainActivity.this, e, 
                                            "Error processing destination selection.", false);
                                }
                            })
                            .start();
                } catch (Exception e) {
                    securityManager.handleException(RomMainActivity.this, e, 
                            "Error with animation.", false);
                }
            });
        }
        
        @Override
        public int getItemCount() {
            return items.size();
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            final MaterialCardView cardView;
            final ImageView imageView;
            final TextView textView;
            
            ViewHolder(MaterialCardView cardView, ImageView imageView, TextView textView) {
                super(cardView);
                this.cardView = cardView;
                this.imageView = imageView;
                this.textView = textView;
            }
        }
    }
    public void openRecipesActivity(View view) {
        // Redirectăm către funcționalitatea culinară existentă
        startCulinaryMode(view);
    }

    /**
     * Deschide activitatea de profil utilizator
     * @param view View-ul care a declanșat acțiunea
     */
    public void openUserProfile(View view) {
        animateButtonClick(view);
        try {
            Intent intent = new Intent(this, com.example.myapplication.ui.UserProfileActivity.class);
            // Validate intent before use
            if (securityManager.validateIntent(intent)) {
                TransitionHelper.startActivityWithSlide(this, intent);
            } else {
                showErrorMessage("Cannot open user profile. Invalid intent detected.");
            }
        } catch (Exception e) {
            securityManager.handleException(this, e, 
                    "Failed to open user profile.", false);
        }
    }

    /**
     * Deschide activitatea de clasament (leaderboard)
     * @param view View-ul care a declanșat acțiunea
     */
    public void openLeaderboard(View view) {
        animateButtonClick(view);
        try {
            Intent intent = new Intent(this, com.example.myapplication.ui.LeaderboardActivity.class);
            // Validate intent before use
            if (securityManager.validateIntent(intent)) {
                TransitionHelper.startActivityWithSlide(this, intent);
            } else {
                showErrorMessage("Cannot open leaderboard. Invalid intent detected.");
            }
        } catch (Exception e) {
            securityManager.handleException(this, e, 
                    "Failed to open leaderboard.", false);
        }
    }
}