package com.example.myapplication.transilvaniausage;

import android.animation.AnimatorInflater;
import android.animation.StateListAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import com.example.myapplication.utils.SyncManager;

import com.example.myapplication.models.EnhancedQuestionModel;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.DynamicColors;
import com.example.myapplication.R;
import com.example.myapplication.RomApp.Transilvania;
import com.example.myapplication.RomApp.PointsManager;
import com.example.myapplication.models.QuestionModel;
import com.example.myapplication.model.QuizResult;
import com.example.myapplication.repository.FirestoreQuestionRepository;
import com.example.myapplication.utils.GameOverHelper;
import com.example.myapplication.Joc1.AchievementManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import java.util.Date;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.google.firebase.auth.FirebaseUser;
import java.util.HashMap;
import java.util.Map;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.content.Context;
import android.graphics.Paint;
import java.util.Arrays;

public class TransilvaniaGameActivity extends AppCompatActivity {
    private static final String TAG = "TransilvaniaGameActivity";
    private static final String REGION = "transilvania";
    private static final String GAME_TYPE = "quiz";
    
    private TextView questionTextView;
    private MaterialButton[] answerButtons;
    private TextView scoreTextView;
    private ProgressBar progressBar;
    private TextView timerTextView;
    private TextView streakTextView;
    private ImageView questionImage;
    private MaterialCardView fiftyFiftyButton;
    private MaterialCardView hintButton;
    private MaterialCardView skipQuestionButton;
    private MaterialCardView quitButton;
    private MaterialCardView[] answerCards;
    private MaterialButton finishButton;
    
    // Enhanced game state variables
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int streak = 0;
    private int maxStreak = 0;
    private int totalQuestions = 0;
    private int correctAnswers = 0;
    private long totalTime = 0;
    private long questionStartTime = 0;
    
    // Enhanced question management
    private List<QuestionModel> firestoreQuestions;
    private List<EnhancedQuestionModel> enhancedQuestions;
    
    // Enhanced game systems
    private DifficultyManager difficultyManager;
    private GameModeManager gameModeManager;
    private PlayerProgressTracker progressTracker;
    private AchievementManager achievementManager;
    private SyncManager syncManager;
    
    // Dynamic game constants based on difficulty and mode
    private int POINTS_PER_CORRECT_ANSWER = 10;
    private int BONUS_POINTS = 50;
    private int TIME_PER_QUESTION = 30000; // Will be updated based on mode/difficulty
    private static final int STREAK_BONUS_THRESHOLD = 3;
    
    // Existing managers
    private PointsManager pointsManager;
    private CountDownTimer timer;
    private boolean isFiftyFiftyUsed = false;
    private boolean isHintUsed = false;
    private boolean isSkipUsed = false;
    private int lifelinesUsed = 0;
    private Random random = new Random();
    private FirestoreQuestionRepository questionRepository;
    private boolean isDataLoaded = false;

    @Override
    protected void onResume() {
        super.onResume();
        
        // 🔄 CHECK FOR UPDATES: Verificăm dacă sunt actualizări în baza de date
        if (syncManager.isInternetAvailable() && isDataLoaded) {
            checkForQuestionUpdates();
        }
    }
    
    /**
     * 🔄 Verifică dacă există actualizări pentru întrebări în baza de date
     */
    private void checkForQuestionUpdates() {
        Log.d(TAG, "🔄 Verificăm actualizări pentru întrebări în baza de date");
        
        // Verificăm timestamp-ul ultimei actualizări din cache
        String cacheKey = "questions_cache_" + REGION + "_" + GAME_TYPE + "_timestamp";
        long lastCacheTime = getSharedPreferences("HybridStorage", MODE_PRIVATE).getLong(cacheKey, 0);
        long currentTime = System.currentTimeMillis();
        
        // Verificăm actualizări doar dacă au trecut mai mult de 30 de minute
        if (currentTime - lastCacheTime > 30 * 60 * 1000) { // 30 minute
            questionRepository.getQuestions(REGION, GAME_TYPE)
                .addOnSuccessListener(querySnapshot -> {
                    int onlineCount = querySnapshot.size();
                    int localCount = firestoreQuestions != null ? firestoreQuestions.size() : 0;
                    
                    if (onlineCount != localCount) {
                        Log.d(TAG, "🔄 Actualizări detectate: online=" + onlineCount + ", local=" + localCount);
                        showUpdateAvailableDialog();
                    } else {
                        // Actualizăm timestamp-ul pentru cache
                        getSharedPreferences("HybridStorage", MODE_PRIVATE)
                            .edit()
                            .putLong(cacheKey, currentTime)
                            .apply();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "🔄 Nu s-au putut verifica actualizările", e);
                });
        }
    }
    
    /**
     * 📢 Afișează dialog pentru actualizări disponibile
     */
    private void showUpdateAvailableDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("🆕 Actualizări Disponibile")
            .setMessage("Sunt disponibile întrebări noi în baza de date!\n\n" +
                       "Doriți să reîncărcați pentru a avea cele mai recente întrebări?")
            .setPositiveButton("🔄 Actualizează", (dialog, which) -> {
                Toast.makeText(this, "🔄 Reîncărcăm întrebările...", Toast.LENGTH_SHORT).show();
                loadQuestionsFromFirestore();
            })
            .setNegativeButton("📱 Mai târziu", null)
            .show();
    }
    
    /**
     * 💾 Verifică preferința utilizatorului și încarcă în consecință
     */
    private void checkUserPreferenceAndLoad() {
        SharedPreferences prefs = getSharedPreferences("TransilvaniaGamePrefs", MODE_PRIVATE);
        String savedPreference = prefs.getString("data_source_preference", "ask_every_time");
        boolean hasInternet = syncManager.isInternetAvailable();
        boolean hasLocalCache = checkIfLocalCacheExists();
        
        Log.d(TAG, "🔍 Verificăm preferința utilizatorului: " + savedPreference + 
              ", Internet: " + hasInternet + ", Cache: " + hasLocalCache);
        
        switch (savedPreference) {
            case "always_database":
                if (hasInternet) {
                    Toast.makeText(this, "🌐 Încărcăm din baza de date (preferință salvată)...", Toast.LENGTH_SHORT).show();
                    loadQuestionsFromDatabase();
                } else {
                    // Nu există internet, întrebăm ce să facă
                    showNoInternetForPreferredDatabaseDialog();
                }
                break;
                
            case "always_cache":
                if (hasLocalCache) {
                    Toast.makeText(this, "📱 Încărcăm din cache local (preferință salvată)...", Toast.LENGTH_SHORT).show();
                    loadQuestionsFromLocalCache();
                } else {
                    // Nu există cache, întrebăm ce să facă
                    showNoCacheForPreferredLocalDialog();
                }
                break;
                
            case "auto":
                Toast.makeText(this, "🎯 Alegere automată (preferință salvată)...", Toast.LENGTH_SHORT).show();
                loadQuestionsFromFirestore(); // Metoda originală cu logica automată
                break;
                
            case "ask_every_time":
            default:
                // Întrebăm utilizatorul
                showDataSourceSelectionDialog();
                break;
        }
    }
    
    /**
     * ❌ Dialog când utilizatorul preferă baza de date dar nu există internet
     */
    private void showNoInternetForPreferredDatabaseDialog() {
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);
        dialogBuilder.setTitle("❌ Fără internet");
        dialogBuilder.setCancelable(false);
        
        String baseMessage = "Preferați baza de date, dar nu există conexiune la internet.\n\n💡 Opțiuni disponibile:";
        
        if (checkIfLocalCacheExists()) {
            dialogBuilder.setMessage(baseMessage + 
                    "\n\n📱 Cache Local disponibil\n🔄 Așteptați internetul\n⚙️ Schimbați preferința");
            
            dialogBuilder.setPositiveButton("📱 Cache Local", (dialog, which) -> {
                loadQuestionsFromLocalCache();
            });
            
            dialogBuilder.setNeutralButton("⚙️ Schimbă preferința", (dialog, which) -> {
                showDataSourceSelectionDialogWithPreferences();
            });
        } else {
            dialogBuilder.setMessage(baseMessage + 
                    "\n\n❌ Nu există cache local\n🔄 Așteptați internetul\n⚙️ Schimbați preferința");
            
            dialogBuilder.setNeutralButton("⚙️ Schimbă preferința", (dialog, which) -> {
                showDataSourceSelectionDialogWithPreferences();
            });
        }
        
        dialogBuilder.setNegativeButton("🚪 Înapoi", (dialog, which) -> finish());
        dialogBuilder.show();
    }
    
    /**
     * ❌ Dialog când utilizatorul preferă cache local dar nu există
     */
    private void showNoCacheForPreferredLocalDialog() {
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);
        dialogBuilder.setTitle("📱 Cache indisponibil");
        dialogBuilder.setCancelable(false);
        
        String baseMessage = "Preferați cache local, dar nu există întrebări salvate.\n\n💡 Opțiuni disponibile:";
        
        if (syncManager.isInternetAvailable()) {
            dialogBuilder.setMessage(baseMessage + 
                    "\n\n🌐 Baza de Date disponibilă\n⚙️ Schimbați preferința");
            
            dialogBuilder.setPositiveButton("🌐 Baza de Date", (dialog, which) -> {
                loadQuestionsFromDatabase();
            });
        } else {
            dialogBuilder.setMessage(baseMessage + 
                    "\n\n❌ Nu există internet\n⚙️ Schimbați preferința");
        }
        
        dialogBuilder.setNeutralButton("⚙️ Schimbă preferința", (dialog, which) -> {
            showDataSourceSelectionDialogWithPreferences();
        });
        
        dialogBuilder.setNegativeButton("🚪 Înapoi", (dialog, which) -> finish());
        dialogBuilder.show();
    }
    
    /**
     * 🤔 Dialog pentru alegerea sursei de date
     */
    private void showDataSourceSelectionDialog() {
        showDataSourceSelectionDialogWithPreferences();
    }
    
    /**
     * 🤔 Dialog pentru alegerea sursei de date cu opțiuni de preferințe
     */
    private void showDataSourceSelectionDialogWithPreferences() {
        boolean hasInternet = syncManager.isInternetAvailable();
        boolean hasLocalCache = checkIfLocalCacheExists();
        
        Log.d(TAG, "🔍 Dialog de alegere - Internet: " + hasInternet + ", Cache: " + hasLocalCache);
        
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);
        dialogBuilder.setTitle("📚 Alegeți sursa întrebărilor");
        dialogBuilder.setCancelable(false);
        
        if (hasInternet && hasLocalCache) {
            // Ambele opțiuni disponibile - folosim butoane simple
            Log.d(TAG, "📊 Afișez dialog cu ambele opțiuni disponibile");
            
            dialogBuilder.setMessage("📊 Ambele surse sunt disponibile!\n\n" +
                    "🌐 Baza de Date: Întrebări actualizate\n" +
                    "📱 Cache Local: Încărcare rapidă\n" +
                    "🎯 Automat: Alege cel mai bun\n\n" +
                    "Ce preferați?");
            
            dialogBuilder.setPositiveButton("🌐 Baza de Date", (dialog, which) -> {
                Log.d(TAG, "✅ Utilizatorul a ales Baza de Date");
                dialog.dismiss();
                Toast.makeText(this, "🌐 Încărcăm din baza de date...", Toast.LENGTH_SHORT).show();
                loadQuestionsFromDatabase();
            });
            
            dialogBuilder.setNegativeButton("📱 Cache Local", (dialog, which) -> {
                Log.d(TAG, "✅ Utilizatorul a ales Cache Local");
                dialog.dismiss();
                Toast.makeText(this, "📱 Încărcăm din cache local...", Toast.LENGTH_SHORT).show();
                loadQuestionsFromLocalCache();
            });
            
            dialogBuilder.setNeutralButton("🎯 Automat", (dialog, which) -> {
                Log.d(TAG, "✅ Utilizatorul a ales Automat");
                dialog.dismiss();
                Toast.makeText(this, "🎯 Alegem automat cea mai bună opțiune...", Toast.LENGTH_SHORT).show();
                loadQuestionsFromFirestore();
            });
            
        } else if (hasInternet) {
            // Doar baza de date disponibilă
            Log.d(TAG, "🌐 Afișez dialog doar cu baza de date (nu există cache)");
            dialogBuilder.setMessage("🌐 Internet detectat!\n\n" +
                    "✅ Baza de Date Online:\n" +
                    "• Întrebări actualizate\n" +
                    "• Conținut nou și îmbunătățit\n" +
                    "• Se va crea cache pentru viitor\n\n" +
                    "⚠️ Cache local nu este disponibil.\n" +
                    "Acest joc va crea cache-ul pentru utilizare offline viitoare.");
            
            dialogBuilder.setPositiveButton("🌐 Începe jocul", (dialog, which) -> {
                Log.d(TAG, "✅ Utilizatorul a ales să înceapă cu baza de date");
                dialog.dismiss();
                Toast.makeText(this, "🌐 Încărcăm din baza de date...", Toast.LENGTH_SHORT).show();
                loadQuestionsFromDatabase();
            });
            
            dialogBuilder.setNegativeButton("🚪 Înapoi", (dialog, which) -> {
                finish();
            });
            
        } else if (hasLocalCache) {
            // Doar cache local disponibil
            Log.d(TAG, "📱 Afișez dialog doar cu cache local (nu există internet)");
            dialogBuilder.setMessage("📱 Mod Offline detectat!\n\n" +
                    "✅ Cache Local disponibil:\n" +
                    "• Joc rapid fără internet\n" +
                    "• Întrebări din sesiunile anterioare\n" +
                    "• Fără consum de date\n\n" +
                    "ℹ️ Pentru întrebări noi, conectați-vă la internet.");
            
            dialogBuilder.setPositiveButton("📱 Începe jocul", (dialog, which) -> {
                Log.d(TAG, "✅ Utilizatorul a ales să înceapă cu cache local");
                dialog.dismiss();
                Toast.makeText(this, "📱 Încărcăm din cache local...", Toast.LENGTH_SHORT).show();
                loadQuestionsFromLocalCache();
            });
            
            dialogBuilder.setNegativeButton("🚪 Înapoi", (dialog, which) -> {
                finish();
            });
            
        } else {
            // Nimic disponibil
            Log.d(TAG, "⚠️ Afișez dialog fără opțiuni (nu există internet sau cache)");
            dialogBuilder.setMessage("⚠️ Nu sunt disponibile întrebări!\n\n" +
                    "❌ Nu există conexiune la internet\n" +
                    "❌ Nu există cache local\n\n" +
                    "💡 Pentru a juca:\n" +
                    "• Conectați-vă la internet\n" +
                    "• Jucați o dată pentru a crea cache-ul\n" +
                    "• Apoi veți putea juca offline");
            
            dialogBuilder.setPositiveButton("🎮 Joacă Local", (dialog, which) -> {
                Log.d(TAG, "✅ Utilizatorul a ales să joace cu întrebări locale");
                dialog.dismiss();
                Toast.makeText(this, "🎮 Încep jocul cu întrebări locale...", Toast.LENGTH_SHORT).show();
                useLocalQuestionsDirectly();
            });
            
            dialogBuilder.setNeutralButton("🔄 Încearcă din nou", (dialog, which) -> {
                Log.d(TAG, "🔄 Utilizatorul încearcă din nou");
                dialog.dismiss();
                showDataSourceSelectionDialog(); // Reîncearcă
            });
            
            dialogBuilder.setNegativeButton("🚪 Înapoi", (dialog, which) -> {
                Log.d(TAG, "🚪 Utilizatorul a ales să iasă");
                finish();
            });
        }
        
        dialogBuilder.show();
    }
    
    /**
     * 🔍 Verifică dacă există cache local pentru întrebări
     */
    private boolean checkIfLocalCacheExists() {
        String cacheKey = "questions_cache_" + REGION + "_" + GAME_TYPE;
        String cachedJson = getSharedPreferences("HybridStorage", MODE_PRIVATE).getString(cacheKey, null);
        
        if (cachedJson != null && !cachedJson.isEmpty()) {
            try {
                com.google.gson.Gson gson = new com.google.gson.Gson();
                com.google.gson.reflect.TypeToken<Map<String, Object>> typeToken = 
                    new com.google.gson.reflect.TypeToken<Map<String, Object>>(){};
                Map<String, Object> cacheData = gson.fromJson(cachedJson, typeToken.getType());
                
                if (cacheData != null && cacheData.containsKey("questions")) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> questionMaps = (List<Map<String, Object>>) cacheData.get("questions");
                    boolean hasQuestions = questionMaps != null && !questionMaps.isEmpty();
                    
                    if (hasQuestions) {
                        long timestamp = cacheData.containsKey("timestamp") ? 
                            ((Number) cacheData.get("timestamp")).longValue() : 0;
                        long ageInHours = (System.currentTimeMillis() - timestamp) / (1000 * 60 * 60);
                        
                        Log.d(TAG, "🔍 Cache local găsit: " + questionMaps.size() + 
                              " întrebări, vârstă: " + ageInHours + " ore");
                        return true;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "🔍 Eroare la verificarea cache-ului local", e);
            }
        }
        
        Log.d(TAG, "🔍 Nu există cache local valid");
        return false;
    }
    
    /**
     * 💾 Salvează preferința utilizatorului
     */
    private void saveUserPreference(String preference) {
        SharedPreferences prefs = getSharedPreferences("TransilvaniaGamePrefs", MODE_PRIVATE);
        prefs.edit()
            .putString("data_source_preference", preference)
            .putLong("preference_saved_at", System.currentTimeMillis())
            .apply();
        
        Log.d(TAG, "💾 Preferință salvată: " + preference);
    }
    
    /**
     * 🔧 Resetează preferințele utilizatorului
     */
    private void resetUserPreferences() {
        SharedPreferences prefs = getSharedPreferences("TransilvaniaGamePrefs", MODE_PRIVATE);
        prefs.edit()
            .putString("data_source_preference", "ask_every_time")
            .remove("preference_saved_at")
            .apply();
        
        Log.d(TAG, "🔧 Preferințe resetate");
    }
    
    /**
     * 🌐 FORȚAT: Încarcă întrebările din baza de date (alegere explicită)
     */
    private void loadQuestionsFromDatabase() {
        // Afișăm un indicator de încărcare
        progressBar.setVisibility(View.VISIBLE);
        
        Log.d(TAG, "🌐 FORȚAT: Încărcăm din baza de date la cererea utilizatorului");
        
        if (!syncManager.isInternetAvailable()) {
            // Dacă nu există internet, afișăm eroare
            progressBar.setVisibility(View.GONE);
            new MaterialAlertDialogBuilder(this)
                .setTitle("❌ Nu există conexiune")
                .setMessage("Nu se poate conecta la baza de date.\n\n" +
                           "💡 Încercați:\n" +
                           "• Verificați conexiunea la internet\n" +
                           "• Folosiți cache local dacă este disponibil")
                .setPositiveButton("📱 Cache Local", (dialog, which) -> {
                    if (checkIfLocalCacheExists()) {
                        loadQuestionsFromLocalCache();
                    } else {
                        showOfflineNoQuestionsError();
                    }
                })
                .setNegativeButton("🔄 Încearcă din nou", (dialog, which) -> {
                    showDataSourceSelectionDialog();
                })
                .show();
            return;
        }
        
        // Adăugăm logging mai detaliat
        Log.d(TAG, "🔍 Încercăm să accesăm Firestore cu:");
        Log.d(TAG, "   📍 REGION: " + REGION);
        Log.d(TAG, "   🎮 GAME_TYPE: " + GAME_TYPE);
        Log.d(TAG, "   📂 Calea: regions/" + REGION + "/games/" + GAME_TYPE + "/questions");
        
        questionRepository.getQuestionsAsModels(REGION, GAME_TYPE)
            .thenAccept(loadedQuestions -> {
                runOnUiThread(() -> {
                    Log.d(TAG, "🔍 Răspuns primit din Firestore:");
                    Log.d(TAG, "   📊 loadedQuestions != null: " + (loadedQuestions != null));
                    if (loadedQuestions != null) {
                        Log.d(TAG, "   📊 loadedQuestions.size(): " + loadedQuestions.size());
                        Log.d(TAG, "   📊 loadedQuestions.isEmpty(): " + loadedQuestions.isEmpty());
                    }
                    
                    if (loadedQuestions != null && !loadedQuestions.isEmpty()) {
                        firestoreQuestions = loadedQuestions;
                        Log.d(TAG, "✅ Întrebări încărcate DIRECT din baza de date: " + firestoreQuestions.size());
                        
                        // ✅ CACHE LOCAL: Salvăm în cache pentru utilizare offline viitoare
                        saveQuestionsToLocalCache(loadedQuestions);
                        
                        // ✅ ÎMBUNĂTĂȚIRE: Convertește în enhanced questions și aplică filtre
                        enhancedQuestions = convertToEnhancedQuestions(firestoreQuestions);
                        enhancedQuestions = gameModeManager.filterQuestionsForGameMode(enhancedQuestions);
                        
                        // Actualizăm progress bar
                        progressBar.setMax(enhancedQuestions.size());
                        progressBar.setProgress(0);
                        progressBar.setVisibility(View.GONE);
                        
                        // Afișăm prima întrebare
                        isDataLoaded = true;
                        
                        // ✅ CORECTARE: Asigurăm că timer-ul este vizibil când încărcăm din baza de date
                        timerTextView.setVisibility(View.VISIBLE);
                        
                        // ✅ ÎMBUNĂTĂȚIRE: Începe sesiunea de tracking
                        progressTracker.startNewSession();
                        
                        displayQuestion();
                        updateScore();
                        startTimer();
                        
                        // Adăugăm indicator vizual pentru sursă
                        updateDataSourceIndicator("🌐 Baza de Date");
                        
                        // Notificăm utilizatorul
                        Toast.makeText(this, "✅ Întrebări încărcate din baza de date!", Toast.LENGTH_SHORT).show();
                    } else {
                        // Dacă nu avem întrebări în baza de date
                        Log.w(TAG, "⚠️ Baza de date este goală pentru Transilvania - încercăm migrarea automată");
                        
                        // În loc să afișăm dialog, încercăm direct migrarea
                        Toast.makeText(this, "🔄 Creez întrebări în baza de date...", Toast.LENGTH_LONG).show();
                        migrateQuestionsToFirestore();
                    }
                });
            })
            .exceptionally(e -> {
                runOnUiThread(() -> {
                    Log.e(TAG, "❌ Eroare detaliată la încărcarea din baza de date:", e);
                    Log.e(TAG, "   🔍 Tip eroare: " + e.getClass().getSimpleName());
                    Log.e(TAG, "   📝 Mesaj eroare: " + e.getMessage());
                    if (e.getCause() != null) {
                        Log.e(TAG, "   🔗 Cauza: " + e.getCause().getMessage());
                    }
                    
                    // Verificăm tipul erorii pentru a da soluții specifice
                    showEnhancedDatabaseErrorDialog(e);
                });
                return null;
            });
    }
    
    /**
     * ⚠️ Dialog pentru lipsa întrebărilor în baza de date
     */
    private void showNoDatabaseQuestionsDialog() {
        progressBar.setVisibility(View.GONE);
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("⚠️ Întrebări indisponibile")
            .setMessage("Nu există întrebări în baza de date pentru Transilvania.\n\n" +
                       "💡 Opțiuni disponibile:\n" +
                       "• Folosiți cache local (dacă există)\n" +
                       "• Creați întrebări prin migrare\n" +
                       "• Contactați administratorul")
            .setPositiveButton("📱 Cache Local", (dialog, which) -> {
                if (checkIfLocalCacheExists()) {
                    loadQuestionsFromLocalCache();
                } else {
                    showOfflineNoQuestionsError();
                }
            })
            .setNeutralButton("🔧 Migrare", (dialog, which) -> {
                Toast.makeText(this, "🔧 Creez întrebări...", Toast.LENGTH_SHORT).show();
                migrateQuestionsToFirestore();
            })
            .setNegativeButton("🚪 Înapoi", (dialog, which) -> finish())
            .show();
    }
    
    /**
     * ❌ Dialog pentru eroare de încărcare din baza de date
     */
    private void showDatabaseErrorDialog() {
        progressBar.setVisibility(View.GONE);
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("❌ Eroare de conexiune")
            .setMessage("Nu s-au putut încărca întrebările din baza de date.\n\n" +
                       "💡 Cauze posibile:\n" +
                       "• Probleme de conexiune\n" +
                       "• Server temporar indisponibil\n" +
                       "• Probleme de autentificare\n\n" +
                       "Ce doriți să faceți?")
            .setPositiveButton("📱 Cache Local", (dialog, which) -> {
                if (checkIfLocalCacheExists()) {
                    loadQuestionsFromLocalCache();
                } else {
                    showOfflineNoQuestionsError();
                }
            })
            .setNeutralButton("🔄 Încearcă din nou", (dialog, which) -> {
                loadQuestionsFromDatabase();
            })
            .setNegativeButton("🚪 Înapoi", (dialog, which) -> finish())
            .show();
    }
    
    /**
     * ❌ Dialog îmbunătățit pentru erori de baza de date cu diagnosticare
     */
    private void showEnhancedDatabaseErrorDialog(Throwable error) {
        progressBar.setVisibility(View.GONE);
        
        String errorType = error.getClass().getSimpleName();
        String errorMessage = error.getMessage();
        String diagnosis = "Eroare necunoscută";
        String recommendation = "";
        
        // Analizăm tipul erorii pentru a da soluții specifice
        if (errorMessage != null) {
            if (errorMessage.contains("PERMISSION_DENIED") || errorMessage.contains("permission")) {
                diagnosis = "🔒 Probleme de permisiuni Firebase";
                recommendation = "• Verificați configurarea Firebase\n• Verificați regulile Firestore\n• Creez întrebări prin migrare";
            } else if (errorMessage.contains("UNAVAILABLE") || errorMessage.contains("timeout")) {
                diagnosis = "🌐 Server Firebase indisponibil";
                recommendation = "• Verificați conexiunea la internet\n• Încercați din nou mai târziu\n• Folosiți cache local";
            } else if (errorMessage.contains("NOT_FOUND")) {
                diagnosis = "📁 Colecția nu există în Firestore";
                recommendation = "• Baza de date nu conține întrebări\n• Creez întrebări prin migrare automată";
            } else if (errorMessage.contains("UNAUTHENTICATED")) {
                diagnosis = "🔑 Probleme de autentificare";
                recommendation = "• Autentificați-vă în aplicație\n• Verificați configurarea Firebase Auth";
            }
        }
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("❌ Problemă cu baza de date")
            .setMessage("Nu s-au putut încărca întrebările din Firebase.\n\n" +
                       "🔍 Diagnosticare:\n" + diagnosis + "\n\n" +
                       "💡 Soluții recomandate:\n" + recommendation + "\n\n" +
                       "🛠️ Detalii tehnice:\n" + errorType + ": " + errorMessage)
            .setPositiveButton("🔧 Creez întrebări", (dialog, which) -> {
                Toast.makeText(this, "🔧 Încerc să creez întrebări în baza de date...", Toast.LENGTH_LONG).show();
                migrateQuestionsToFirestore();
            })
            .setNeutralButton("📱 Cache Local", (dialog, which) -> {
                if (checkIfLocalCacheExists()) {
                    loadQuestionsFromLocalCache();
                } else {
                    showOfflineNoQuestionsError();
                }
            })
            .setNegativeButton("🚪 Înapoi", (dialog, which) -> finish())
            .show();
    }
    
    /**
     * 📊 Actualizează indicatorul sursă de date în UI (doar în log, nu în UI)
     */
    private void updateDataSourceIndicator(String source) {
        // Doar log pentru debugging, nu afișăm în UI
        Log.d(TAG, "📊 Sursă de date: " + source);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply dynamic colors if available
        DynamicColors.applyToActivityIfAvailable(this);
        
        // Set theme before super.onCreate
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.Theme_MyApplication_Dark);
        } else {
            setTheme(R.style.Theme_MyApplication_Light);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transilvania_game);

        // Initialize enhanced systems
        initializeEnhancedSystems();

        initializeViews();
        pointsManager = PointsManager.getInstance(this);
        questionRepository = FirestoreQuestionRepository.getInstance();
        
        // Setup game mode and difficulty
        setupGameModeAndDifficulty();
        
        // 🤔 ALEGERE UTILIZATOR: Întrebăm utilizatorul să aleagă sursa de date
        // Verificăm dacă utilizatorul are o preferință salvată
        checkUserPreferenceAndLoad();
        
        setupLifelines();
        applyButtonStyles();
        setupAccessibility();
        
        // Adaugă un buton temporar pentru ștergerea cache-ului local
        Button clearCacheButton = new Button(this);
        clearCacheButton.setText("Șterge cache întrebări Transilvania");
        clearCacheButton.setOnClickListener(v -> {
            String cacheKey = "questions_cache_transilvania_quiz";
            getSharedPreferences("HybridStorage", MODE_PRIVATE).edit().remove(cacheKey).apply();
            Toast.makeText(this, "Cache local pentru întrebări Transilvania șters!", Toast.LENGTH_SHORT).show();
        });
        // Adaugă butonul la layout-ul principal (doar pentru test)
        ViewGroup rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            rootView.addView(clearCacheButton);
        }
    }
    
    /**
     * ✅ ÎMBUNĂTĂȚIRE: Inițializează sistemele îmbunătățite
     */
    private void initializeEnhancedSystems() {
        difficultyManager = new DifficultyManager(this);
        gameModeManager = new GameModeManager(this);
        progressTracker = new PlayerProgressTracker(this);
        achievementManager = AchievementManager.getInstance(this);
        syncManager = SyncManager.getInstance(this);
        
        // Set up achievement listener for notifications
        achievementManager.setAchievementUnlockedListener(achievement -> {
            runOnUiThread(() -> {
                Toast.makeText(this, "🏆 Achievement Unlocked: " + achievement.getTitle(), 
                             Toast.LENGTH_LONG).show();
                // Could add more sophisticated notification here
            });
        });
        
        // Update daily play streak
        achievementManager.updateTransilvaniaDailyPlayStreak();
        
        Log.d(TAG, "Enhanced systems initialized");
    }
    
    /**
     * ✅ ÎMBUNĂTĂȚIRE: Configurează modul de joc și dificultatea
     */
    private void setupGameModeAndDifficulty() {
        // Obține modul de joc din intent sau folosește default
        String gameMode = getIntent().getStringExtra("GAME_MODE");
        String focusCategory = getIntent().getStringExtra("FOCUS_CATEGORY");
        
        GameModeManager.GameMode mode = gameMode != null ? 
            GameModeManager.GameMode.valueOf(gameMode) : GameModeManager.GameMode.CLASSIC;
        
        EnhancedQuestionModel.Category category = focusCategory != null ?
            EnhancedQuestionModel.Category.valueOf(focusCategory) : null;
            
        // Inițializează modul de joc
        gameModeManager.initializeGameMode(mode, category);
        
        // Actualizează constantele bazate pe mod și dificultate
        DifficultyManager.DifficultyLevel difficulty = difficultyManager.getCurrentDifficulty();
        TIME_PER_QUESTION = Math.max(gameModeManager.getTimePerQuestion(), 
                                   difficulty.timePerQuestion);
        
        // Actualizează punctajul bazat pe dificultate
        POINTS_PER_CORRECT_ANSWER = (int)(10 * difficulty.pointsMultiplier);
        
        Log.d(TAG, "Game mode: " + mode.displayName + 
               ", Difficulty: " + difficulty.displayName + 
               ", Time per question: " + TIME_PER_QUESTION + "ms");
    }
    
    private void loadQuestionsFromFirestore() {
        // Afișăm un indicator de încărcare
        progressBar.setVisibility(View.VISIBLE);
        
        Log.d(TAG, "🔄 Loading questions from Firestore - REGION: " + REGION + ", GAME_TYPE: " + GAME_TYPE);
        
        // ✅ PRIORITATE: Verificăm mai întâi conexiunea la internet
        if (!syncManager.isInternetAvailable()) {
            Log.w(TAG, "❌ Nu există conexiune la internet - încărcăm din cache local");
            loadQuestionsFromLocalCache();
            return;
        }
        
        Log.d(TAG, "🌐 Internet disponibil - încărcăm DIRECT din Firebase Firestore");
        
        questionRepository.getQuestionsAsModels(REGION, GAME_TYPE)
            .thenAccept(loadedQuestions -> {
                runOnUiThread(() -> {
                    // Verificăm dacă avem întrebări din Firestore
                    if (loadedQuestions != null && !loadedQuestions.isEmpty()) {
                        firestoreQuestions = loadedQuestions;
                        Log.d(TAG, "✅ Întrebări încărcate DIRECT din Firestore: " + firestoreQuestions.size());
                        
                        // ✅ CACHE LOCAL: Salvăm în cache pentru utilizare offline
                        saveQuestionsToLocalCache(loadedQuestions);
                        
                        // ✅ ÎMBUNĂTĂȚIRE: Convertește în enhanced questions și aplică filtre
                        enhancedQuestions = convertToEnhancedQuestions(firestoreQuestions);
                        enhancedQuestions = gameModeManager.filterQuestionsForGameMode(enhancedQuestions);
                        
                        // Actualizăm progress bar
                        progressBar.setMax(enhancedQuestions.size());
                        progressBar.setProgress(0);
                        
                        // Afișăm prima întrebare
                        isDataLoaded = true;
                        
                        // ✅ CORECTARE: Asigurăm că timer-ul este vizibil când încărcăm din Firestore
                        timerTextView.setVisibility(View.VISIBLE);
                        
                        // ✅ ÎMBUNĂTĂȚIRE: Începe sesiunea de tracking
                        progressTracker.startNewSession();
                        
                        displayQuestion();
                        updateScore();
                        startTimer();
                    } else {
                        // Dacă nu avem întrebări în Firestore, încercăm din cache local
                        Log.w(TAG, "⚠️ Nu există întrebări în Firestore pentru " + REGION + " - verificăm cache local");
                        loadQuestionsFromLocalCache();
                    }
                });
            })
            .exceptionally(e -> {
                runOnUiThread(() -> {
                    Log.e(TAG, "❌ Eroare la încărcarea din Firestore - încerc din cache local", e);
                    
                    // Fallback la cache local în caz de eroare
                    loadQuestionsFromLocalCache();
                });
                return null;
            });
    }
    
    /**
     * 💾 CACHE LOCAL: Salvează întrebările în cache pentru utilizare offline
     */
    private void saveQuestionsToLocalCache(List<QuestionModel> questions) {
        // Convertim întrebările într-un format compatibil cu JSON/Firestore
        List<Map<String, Object>> questionMaps = new ArrayList<>();
        for (QuestionModel question : questions) {
            Map<String, Object> questionMap = new HashMap<>();
            questionMap.put("question", question.getQuestion());
            questionMap.put("correctAnswer", question.getCorrectAnswer());
            questionMap.put("incorrectAnswers", question.getIncorrectAnswers().toArray(new String[0])); // Folosim String[]
            questionMap.put("fact", question.getFact());
            questionMap.put("imageResourceId", question.getImageResourceId());
            questionMaps.add(questionMap);
        }
        
        Map<String, Object> cacheData = new HashMap<>();
        cacheData.put("questions", questionMaps);
        cacheData.put("region", REGION);
        cacheData.put("gameType", GAME_TYPE);
        cacheData.put("timestamp", System.currentTimeMillis());
        cacheData.put("count", questions.size());
        
        // Salvăm în sistemul hibrid pentru cache local
        syncManager.saveData("questions_cache", REGION + "_" + GAME_TYPE, cacheData, new SyncManager.SyncCallback() {
            @Override
            public void onSyncComplete(boolean success, String message) {
                if (success) {
                    Log.d(TAG, "💾 ✅ Questions cached locally: " + questions.size() + " questions");
                } else {
                    Log.w(TAG, "💾 ⚠️ Failed to cache questions locally: " + message);
                }
            }
        });
    }
    
    /**
     * 💾 CACHE LOCAL: Încarcă întrebările din cache local
     */
    private void loadQuestionsFromLocalCache() {
        Log.d(TAG, "💾 Încercăm să încărcăm din cache local pentru " + REGION + "_" + GAME_TYPE);
        
        // Încercăm să încărcăm din SharedPreferences (cache local)
        String cacheKey = "questions_cache_" + REGION + "_" + GAME_TYPE;
        String cachedJson = getSharedPreferences("HybridStorage", MODE_PRIVATE).getString(cacheKey, null);
        
        if (cachedJson != null && !cachedJson.isEmpty()) {
            try {
                // Parsăm datele din cache
                com.google.gson.Gson gson = new com.google.gson.Gson();
                com.google.gson.reflect.TypeToken<Map<String, Object>> typeToken = 
                    new com.google.gson.reflect.TypeToken<Map<String, Object>>(){};
                Map<String, Object> cacheData = gson.fromJson(cachedJson, typeToken.getType());
                
                if (cacheData != null && cacheData.containsKey("questions")) {
                    // Extragem lista de întrebări din cache
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> questionMaps = (List<Map<String, Object>>) cacheData.get("questions");
                    List<QuestionModel> cachedQuestions = new ArrayList<>();
                    
                    for (Map<String, Object> questionMap : questionMaps) {
                        // Reconstituim QuestionModel din Map
                        String question = (String) questionMap.get("question");
                        String correctAnswer = (String) questionMap.get("correctAnswer");
                        @SuppressWarnings("unchecked")
                        List<String> incorrectAnswersList = (List<String>) questionMap.get("incorrectAnswers");
                        String fact = (String) questionMap.get("fact");
                        
                        // Folosim noul constructor cu List<String> pentru compatibilitate Firebase
                        QuestionModel questionModel = new QuestionModel(question, correctAnswer, incorrectAnswersList, 0, fact);
                        cachedQuestions.add(questionModel);
                    }
                    
                    if (!cachedQuestions.isEmpty()) {
                        firestoreQuestions = cachedQuestions;
                        Log.d(TAG, "💾 ✅ Întrebări încărcate din cache local: " + firestoreQuestions.size());
                        
                        // ✅ ÎMBUNĂTĂȚIRE: Convertește în enhanced questions și aplică filtre
                        enhancedQuestions = convertToEnhancedQuestions(firestoreQuestions);
                        enhancedQuestions = gameModeManager.filterQuestionsForGameMode(enhancedQuestions);
                        
                        // Actualizăm progress bar
                        progressBar.setMax(enhancedQuestions.size());
                        progressBar.setProgress(0);
                        progressBar.setVisibility(View.GONE);
                        
                        // Afișăm prima întrebare
                        isDataLoaded = true;
                        
                        // ✅ CORECTARE: Asigurăm că timer-ul este vizibil când încărcăm din cache
                        timerTextView.setVisibility(View.VISIBLE);
                        
                        // ✅ ÎMBUNĂTĂȚIRE: Începe sesiunea de tracking
                        progressTracker.startNewSession();
                        
                        displayQuestion();
                        updateScore();
                        startTimer();
                        
                        // Notificăm utilizatorul că folosim cache-ul
                        Toast.makeText(this, "📱 Utilizez întrebări din cache (offline)", Toast.LENGTH_SHORT).show();
                        
                        // Adăugăm indicator vizual pentru sursă
                        updateDataSourceIndicator("📱 Cache Local");
                        return;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "💾 ❌ Eroare la parsarea cache-ului local", e);
            }
        }
        
        // Dacă nu avem cache local, încercăm migrarea ca ultimă soluție
        Log.w(TAG, "💾 ❌ Nu există cache local - încercăm migrarea ca ultimă soluție");
        handleNoQuestionsAvailable();
    }
    
    /**
     * 🚨 Gestionează cazul când nu sunt disponibile întrebări nicăieri
     */
    private void handleNoQuestionsAvailable() {
        progressBar.setVisibility(View.GONE);
        
        if (syncManager.isInternetAvailable()) {
            // Avem internet dar nu avem întrebări în Firestore - încercăm migrarea
            Log.d(TAG, "🔄 Internet disponibil - încercăm migrarea întrebărilor în Firestore");
            migrateQuestionsToFirestore();
        } else {
            // Nu avem internet și nici cache local - afișăm eroare
            showOfflineNoQuestionsError();
        }
    }
    
    /**
     * 📱 Afișează eroare pentru lipsa întrebărilor offline
     */
    private void showOfflineNoQuestionsError() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("📱 Mod Offline")
            .setMessage("Nu sunt disponibile întrebări pentru joc offline.\n\n" +
                       "💡 Pentru a juca:\n" +
                       "• Conectați-vă la internet\n" +
                       "• Jucați o dată pentru a descărca întrebările\n" +
                       "• Apoi veți putea juca și offline")
            .setPositiveButton("🔄 Încearcă din nou", (dialog, which) -> {
                loadQuestionsFromFirestore();
            })
            .setNegativeButton("🚪 Înapoi", (dialog, which) -> finish())
            .setCancelable(false)
            .show();
    }
    
    /**
     * Afișează eroare când nu se pot încărca întrebările din Firestore
     */
    private void showFirestoreError() {
        Toast.makeText(this, "Eroare la încărcarea întrebărilor din server. Verificați conexiunea internet.", 
                Toast.LENGTH_LONG).show();
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("Încărcare întrebări")
            .setMessage("Nu s-au putut încărca întrebările din baza de date. Doriți să încercați din nou?")
            .setPositiveButton("Încearcă din nou", (dialog, which) -> {
                // Încercăm din nou să încărcăm din Firestore
                loadQuestionsFromFirestore();
            })
            .setNegativeButton("Înapoi", (dialog, which) -> {
                // Închidem activitatea și ne întoarcem
                finish();
            })
            .setCancelable(false)
            .show();
    }
    
    /**
     * Migrează întrebările locale în Firestore pentru consistența bazei de date
     */
    private void migrateQuestionsToFirestore() {
        Log.d(TAG, "🔄 Începem migrarea întrebărilor locale în Firestore pentru " + REGION);
        progressBar.setVisibility(View.VISIBLE);
        
        // Creăm întrebările locale temporar doar pentru migrare
        List<QuestionModel> localQuestions = createLocalQuestionsForMigration();
        
        if (localQuestions.isEmpty()) {
            Log.e(TAG, "❌ Nu avem întrebări locale pentru migrare");
            progressBar.setVisibility(View.GONE);
            showNoQuestionsError();
            return;
        }
        
        Log.d(TAG, "📝 Pregătim " + localQuestions.size() + " întrebări pentru migrare");
        
        // Salvăm întrebările în Firestore
        questionRepository.addQuestions(localQuestions, REGION, GAME_TYPE)
            .thenAccept(voidResult -> {
                runOnUiThread(() -> {
                        Log.d(TAG, "✅ Migrare completă! Reîncărcăm din Firestore...");
                        Toast.makeText(this, "✅ Întrebări create! Reîncărcăm...", Toast.LENGTH_SHORT).show();
                        
                        // Reîncărcăm din Firestore acum că avem datele
                        // Folosim metoda automată pentru verificare completă
                        loadQuestionsFromFirestore();
                });
            })
            .exceptionally(e -> {
                runOnUiThread(() -> {
                    Log.e(TAG, "❌ Eroare la migrarea în Firestore", e);
                    progressBar.setVisibility(View.GONE);
                    
                    // În loc să afișăm doar eroare, oferim alternativă directă
                    showMigrationErrorWithAlternative(e);
                });
                return null;
            });
    }
    
    /**
     * ❌ Afișează eroare de migrare cu alternativă directă
     */
    private void showMigrationErrorWithAlternative(Throwable error) {
        String errorMessage = error.getMessage();
        String diagnosis = "Eroare la crearea întrebărilor în Firebase";
        
        if (errorMessage != null) {
            if (errorMessage.contains("PERMISSION_DENIED")) {
                diagnosis = "🔒 Nu am permisiuni să scriu în Firebase";
            } else if (errorMessage.contains("UNAUTHENTICATED")) {
                diagnosis = "🔑 Nu sunt autentificat în Firebase";
            }
        }
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("❌ Nu pot crea întrebări în baza de date")
            .setMessage(diagnosis + "\n\n" +
                       "💡 Soluții alternative:\n" +
                       "• Jucați cu întrebările locale (offline)\n" +
                       "• Verificați configurarea Firebase\n" +
                       "• Contactați administratorul\n\n" +
                       "🛠️ Eroare tehnică: " + errorMessage)
            .setPositiveButton("🎮 Joacă Local", (dialog, which) -> {
                Toast.makeText(this, "🎮 Încep jocul cu întrebări locale...", Toast.LENGTH_SHORT).show();
                useLocalQuestionsDirectly();
            })
            .setNeutralButton("📱 Cache", (dialog, which) -> {
                if (checkIfLocalCacheExists()) {
                    loadQuestionsFromLocalCache();
                } else {
                    Toast.makeText(this, "❌ Nu există cache local disponibil", Toast.LENGTH_SHORT).show();
                    useLocalQuestionsDirectly();
                }
            })
            .setNegativeButton("🚪 Înapoi", (dialog, which) -> finish())
            .show();
    }
    
    /**
     * 🎮 Folosește întrebările locale direct fără Firebase
     */
    private void useLocalQuestionsDirectly() {
        Log.d(TAG, "🎮 Folosim întrebările locale direct, fără Firebase");
        
        List<QuestionModel> localQuestions = createLocalQuestionsForMigration();
        
        if (localQuestions.isEmpty()) {
            Log.e(TAG, "❌ Nu avem întrebări locale");
            showNoQuestionsError();
            return;
        }
        
        // Setăm întrebările direct
        firestoreQuestions = localQuestions;
        Log.d(TAG, "✅ Întrebări locale încărcate: " + firestoreQuestions.size());
        
        // ✅ ÎMBUNĂTĂȚIRE: Convertește în enhanced questions și aplică filtre
        enhancedQuestions = convertToEnhancedQuestions(firestoreQuestions);
        enhancedQuestions = gameModeManager.filterQuestionsForGameMode(enhancedQuestions);
        
        // Actualizăm progress bar
        progressBar.setMax(enhancedQuestions.size());
        progressBar.setProgress(0);
        progressBar.setVisibility(View.GONE);
        
        // Afișăm prima întrebare
        isDataLoaded = true;
        
        // ✅ CORECTARE: Asigurăm că timer-ul este vizibil când folosim întrebări locale
        timerTextView.setVisibility(View.VISIBLE);
        
        // ✅ ÎMBUNĂTĂȚIRE: Începe sesiunea de tracking
        progressTracker.startNewSession();
        
        displayQuestion();
        updateScore();
        startTimer();
        
        // Adăugăm indicator vizual pentru sursă
        updateDataSourceIndicator("🎮 Întrebări Locale");
        
        // Notificăm utilizatorul
        Toast.makeText(this, "✅ Joc început cu întrebări locale!", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Creează întrebările locale temporar doar pentru migrarea în Firestore
     */
    private List<QuestionModel> createLocalQuestionsForMigration() {
        List<QuestionModel> questions = new ArrayList<>();
        
        // Întrebări despre Transilvania pentru migrare în Firestore
        questions.add(new QuestionModel(
            "Care este cel mai înalt vârf montan din Transilvania?",
            "Vârful Moldoveanu", 
            Arrays.asList("Vârful Omu", "Vârful Parâng", "Vârful Retezat"), 
            0, // Folosim 0 pentru imagini care lipsesc
            "Vârful Moldoveanu (2.544 m) este cel mai înalt vârf din România și din Transilvania."
        ));
        
        questions.add(new QuestionModel(
            "Care oraș din Transilvania este cunoscut ca 'Cetatea de pe Târnave'?",
            "Sighișoara", 
            Arrays.asList("Mediaș", "Sebeș", "Rupea"), 
            0,
            "Sighișoara este singura cetate medievală locuită din Transilvania, înscrisă în patrimoniul UNESCO."
        ));
        
        questions.add(new QuestionModel(
            "Care castel din Transilvania este cunoscut ca 'Castelul lui Dracula'?",
            "Castelul Bran", 
            Arrays.asList("Castelul Corvinilor", "Castelul Peleș", "Castelul Râșnov"), 
            0,
            "Castelul Bran este asociat cu legenda lui Dracula, deși Vlad Țepeș a locuit acolo doar scurt timp."
        ));
        
        questions.add(new QuestionModel(
            "Care este cea mai mare biserică gotică din Transilvania?",
            "Biserica Neagră din Brașov", 
            Arrays.asList("Catedrala din Cluj", "Biserica din Sibiu", "Catedrala din Alba Iulia"), 
            0,
            "Biserica Neagră din Brașov este cea mai mare biserică gotică din sud-estul Europei."
        ));
        
        questions.add(new QuestionModel(
            "În ce oraș din Transilvania s-a născut Vlad Țepeș?",
            "Sighișoara", 
            Arrays.asList("Brașov", "Cluj-Napoca", "Târgu Mureș"), 
            0,
            "Vlad Țepeș s-a născut în 1431 în Sighișoara, în casa care astăzi găzduiește un restaurant."
        ));
        
        questions.add(new QuestionModel(
            "Care universitate din Transilvania este cea mai veche?",
            "Universitatea Babeș-Bolyai din Cluj-Napoca", 
            Arrays.asList("Universitatea Transilvania din Brașov", "Universitatea din Sibiu", "Universitatea din Târgu Mureș"), 
            0,
            "Universitatea Babeș-Bolyai, fondată în 1581, este cea mai veche universitate din Transilvania."
        ));
        
        questions.add(new QuestionModel(
            "Care sat din Transilvania este cunoscut pentru bisericile sale fortificate?",
            "Viscri", 
            Arrays.asList("Biertan", "Prejmer", "Hărman"), 
            0,
            "Viscri este cunoscut pentru biserica sa fortificată din secolul XIII, restaurată cu sprijinul Prințului Charles."
        ));
        
        questions.add(new QuestionModel(
            "Care este cel mai mare lac natural din Transilvania?",
            "Lacul Sfânta Ana", 
            Arrays.asList("Lacul Roșu", "Lacul Balea", "Lacul Bucura"), 
            0,
            "Lacul Sfânta Ana este singurul lac de crater din România, situat în Munții Harghita."
        ));
        
        questions.add(new QuestionModel(
            "Care oraș din Transilvania este centrul regiunii Ținutul Secuiesc?",
            "Târgu Mureș", 
            Arrays.asList("Miercurea Ciuc", "Odorheiu Secuiesc", "Sfântu Gheorghe"), 
            0,
            "Târgu Mureș este cel mai mare oraș din centrul Transilvaniei și important centru cultural maghiar."
        ));
        
        questions.add(new QuestionModel(
            "Care mâncare tradițională este specifică gastronomiei transilvănene?",
            "Varza à la Cluj", 
            Arrays.asList("Papanași", "Mici", "Ciorbă de burtă"), 
            0,
            "Varza à la Cluj este un fel de mâncare tradițional din Transilvania, preparat cu varză acră și carne."
        ));
        
        Log.d(TAG, "Întrebări locale create pentru migrare: " + questions.size());
        return questions;
    }
    
    /**
     * ✅ ÎMBUNĂTĂȚIRE: Convertește întrebările simple în enhanced questions
     */
    private List<EnhancedQuestionModel> convertToEnhancedQuestions(List<QuestionModel> questions) {
        List<EnhancedQuestionModel> enhanced = new ArrayList<>();
        
        for (QuestionModel question : questions) {
            // Mapează întrebările la categorii bazate pe conținut
            EnhancedQuestionModel.Category category = inferCategory(question.getQuestion());
            EnhancedQuestionModel.Difficulty difficulty = inferDifficulty(question);
            
            EnhancedQuestionModel enhancedQuestion = EnhancedQuestionModel.fromQuestionModel(
                question, category, difficulty);
            
            // Adaugă tag-uri bazate pe conținut
            enhancedQuestion.setTags(generateTags(question));
            
            enhanced.add(enhancedQuestion);
        }
        
        Log.d(TAG, "Converted " + questions.size() + " questions to enhanced format");
        return enhanced;
    }
    
    /**
     * ✅ ÎMBUNĂTĂȚIRE: Inferă categoria unei întrebări bazată pe conținut
     */
    private EnhancedQuestionModel.Category inferCategory(String questionText) {
        String text = questionText.toLowerCase();
        
        if (text.contains("castel") || text.contains("biserică") || text.contains("cetate") || 
            text.contains("arhitectur")) {
            return EnhancedQuestionModel.Category.ARCHITECTURE;
        } else if (text.contains("vlad") || text.contains("dracula") || text.contains("țepeș") ||
                  text.contains("istorie") || text.contains("război")) {
            return EnhancedQuestionModel.Category.HISTORY;
        } else if (text.contains("munte") || text.contains("vârf") || text.contains("lac") ||
                  text.contains("râu") || text.contains("geografie")) {
            return EnhancedQuestionModel.Category.GEOGRAPHY;
        } else if (text.contains("mâncare") || text.contains("varza") || text.contains("gastronomie")) {
            return EnhancedQuestionModel.Category.GASTRONOMY;
        } else if (text.contains("legendă") || text.contains("mit") || text.contains("poveste")) {
            return EnhancedQuestionModel.Category.LEGENDS;
        } else if (text.contains("universitate") || text.contains("personalitate") || 
                  text.contains("născut")) {
            return EnhancedQuestionModel.Category.PERSONALITIES;
        } else if (text.contains("natură") || text.contains("pădure") || text.contains("animal")) {
            return EnhancedQuestionModel.Category.NATURE;
        } else {
            return EnhancedQuestionModel.Category.CULTURE; // Default
        }
    }
    
    /**
     * ✅ ÎMBUNĂTĂȚIRE: Inferă dificultatea unei întrebări
     */
    private EnhancedQuestionModel.Difficulty inferDifficulty(QuestionModel question) {
        String text = question.getQuestion().toLowerCase();
        int questionLength = text.length();
        
        // Întrebări scurte și directe sunt mai ușoare
        if (questionLength < 50 && 
            (text.contains("care") || text.contains("unde") || text.contains("când"))) {
            return EnhancedQuestionModel.Difficulty.EASY;
        }
        
        // Întrebări cu detalii specifice sau date exacte sunt mai grele
        if (text.contains("anul") || text.contains("secolul") || text.contains("exacte") ||
            text.contains("precisez") || questionLength > 120) {
            return EnhancedQuestionModel.Difficulty.HARD;
        }
        
        // Întrebări foarte specifice sau cu multiple elemente
        if (text.contains("dintre următoarele") && text.contains("nu") ||
            text.contains("toate") || text.contains("exclusiv")) {
            return EnhancedQuestionModel.Difficulty.EXPERT;
        }
        
        return EnhancedQuestionModel.Difficulty.MEDIUM; // Default
    }
    
    /**
     * ✅ ÎMBUNĂTĂȚIRE: Generează tag-uri pentru o întrebare
     */
    private String[] generateTags(QuestionModel question) {
        List<String> tags = new ArrayList<>();
        String text = question.getQuestion().toLowerCase();
        
        // Tag-uri geografice
        if (text.contains("transilvania")) tags.add("transilvania");
        if (text.contains("brașov")) tags.add("brașov");
        if (text.contains("cluj")) tags.add("cluj");
        if (text.contains("sibiu")) tags.add("sibiu");
        if (text.contains("sighișoara")) tags.add("sighișoara");
        
        // Tag-uri istorice
        if (text.contains("medieval")) tags.add("medieval");
        if (text.contains("secolul")) tags.add("istoric");
        if (text.contains("război")) tags.add("război");
        
        // Tag-uri culturale
        if (text.contains("unesco")) tags.add("unesco");
        if (text.contains("patrimoniu")) tags.add("patrimoniu");
        if (text.contains("tradițional")) tags.add("tradițional");
        
        return tags.toArray(new String[0]);
    }
    
    /**
     * Afișează eroare când nu există întrebări deloc
     */
    private void showNoQuestionsError() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Eroare")
            .setMessage("Nu există întrebări disponibile pentru Transilvania. Contactați administratorul aplicației.")
            .setPositiveButton("OK", (dialog, which) -> finish())
            .setCancelable(false)
            .show();
    }
    
    /**
     * Afișează eroare când migrarea a eșuat
     */
    private void showMigrationError() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Eroare de sincronizare")
            .setMessage("Nu s-au putut sincroniza întrebările cu baza de date. Verificați conexiunea internet și încercați din nou.")
            .setPositiveButton("Încearcă din nou", (dialog, which) -> {
                loadQuestionsFromFirestore();
            })
            .setNegativeButton("Înapoi", (dialog, which) -> finish())
            .setCancelable(false)
            .show();
    }

    private void initializeViews() {
        questionTextView = findViewById(R.id.questionTextView);
        answerButtons = new MaterialButton[]{
            findViewById(R.id.answerButton1),
            findViewById(R.id.answerButton2),
            findViewById(R.id.answerButton3),
            findViewById(R.id.answerButton4)
        };
        
        answerCards = new MaterialCardView[]{
            findViewById(R.id.answerCard1),
            findViewById(R.id.answerCard2),
            findViewById(R.id.answerCard3),
            findViewById(R.id.answerCard4)
        };
        
        scoreTextView = findViewById(R.id.scoreTextView);
        progressBar = findViewById(R.id.progressBar);
        timerTextView = findViewById(R.id.timerTextView);
        streakTextView = findViewById(R.id.streakTextView);
        questionImage = findViewById(R.id.questionImage);
        fiftyFiftyButton = findViewById(R.id.fiftyFiftyButton);
        hintButton = findViewById(R.id.hintButton);
        skipQuestionButton = findViewById(R.id.skipQuestionButton);
        quitButton = findViewById(R.id.quitButton);
        finishButton = findViewById(R.id.finishButton);
        
        // Îmbunătățiri pentru vizibilitate și stil text
        questionTextView.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        
        // Aplicăm stiluri pentru butoane
        for (MaterialButton button : answerButtons) {
            button.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
            button.setElevation(4f);
            button.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            
            // ✅ CORECTARE: Activez butoanele pentru click handling
            button.setClickable(true);
            button.setFocusable(true);
        }
        
        // Inițializare buton terminare
        finishButton.setOnClickListener(v -> finishGame());
        
        // Setup click pentru carduri
        for (int i = 0; i < answerCards.length; i++) {
            final int index = i;
            
            // Click listener pentru card
            answerCards[i].setOnClickListener(v -> {
                Log.d(TAG, "🖱️ Card clicked: " + index + ", isClickable: " + v.isClickable());
                if (v.isClickable()) {
                    Log.d(TAG, "🔄 Processing answer for card " + index);
                    checkAnswer(index, answerButtons[index].getText().toString());
                } else {
                    Log.w(TAG, "⚠️ Card " + index + " is not clickable!");
                }
            });
            
            // ✅ CORECTARE: Click listener și pentru buton (backup)
            answerButtons[i].setOnClickListener(v -> {
                Log.d(TAG, "🖱️ Button clicked: " + index + ", isEnabled: " + v.isEnabled());
                if (v.isEnabled() && answerCards[index].isClickable()) {
                    Log.d(TAG, "🔄 Processing answer for button " + index);
                    checkAnswer(index, answerButtons[index].getText().toString());
                } else {
                    Log.w(TAG, "⚠️ Button " + index + " is not enabled or card not clickable!");
                }
            });
        }
    }
    
    private void applyButtonStyles() {
        // Stilizăm butoanele pentru tema Transilvania
        for (int i = 0; i < answerButtons.length; i++) {
            MaterialButton button = answerButtons[i];
            MaterialCardView card = answerCards[i];
            
            // Activăm efectul de ripple pentru card
            card.setClickable(true);
            card.setFocusable(true);
            
            // Adaugă animație la apăsare
            card.setRippleColor(ContextCompat.getColorStateList(this, R.color.transilvania_primary_light));
            
            // Adaugăm shadow și efecte vizuale pentru butoane
            button.setElevation(4f);
            button.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            
            // Adaugă efect de touch feedback
            card.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(100).start();
                        break;
                    case android.view.MotionEvent.ACTION_UP:
                    case android.view.MotionEvent.ACTION_CANCEL:
                        v.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
                        break;
                }
                return false;
            });
        }
        
        // Adaugă efecte vizuale pentru butonul de finalizare
        finishButton.setRippleColor(ContextCompat.getColorStateList(this, R.color.transilvania_accent));
        finishButton.setElevation(8f);
        
        // Îmbunătățim aspectul vizual pentru butoanele de ajutor
        fiftyFiftyButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).start();
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
                    break;
            }
            return false;
        });
        
        skipQuestionButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).start();
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
                    break;
            }
            return false;
        });
    }

    private void setupLifelines() {
        fiftyFiftyButton.setOnClickListener(v -> {
            // Adăugăm efect vizual la apăsare
            Animation pulse = AnimationUtils.loadAnimation(this, R.anim.button_press);
            v.startAnimation(pulse);
            useFiftyFifty();
        });
        
        hintButton.setOnClickListener(v -> {
            // Adăugăm efect vizual la apăsare
            Animation pulse = AnimationUtils.loadAnimation(this, R.anim.button_press);
            v.startAnimation(pulse);
            showHint();
        });
        
        skipQuestionButton.setOnClickListener(v -> {
            // Adăugăm efect vizual la apăsare
            Animation pulse = AnimationUtils.loadAnimation(this, R.anim.button_press);
            v.startAnimation(pulse);
            skipQuestion();
        });
        
        quitButton.setOnClickListener(v -> {
            // Adăugăm efect vizual la apăsare
            Animation pulse = AnimationUtils.loadAnimation(this, R.anim.button_press);
            v.startAnimation(pulse);
            showConfirmQuitDialog();
        });
    }

    /**
     * Sets up accessibility features for UI components
     */
    private void setupAccessibility() {
        // Set content descriptions for better screen reader support
        ViewCompat.setAccessibilityHeading(questionTextView, true);
        
        // Parse the timer text to integer
        int timeValue;
        try {
            timeValue = Integer.parseInt(timerTextView.getText().toString());
        } catch (NumberFormatException e) {
            timeValue = 30; // Default value
        }
        timerTextView.setContentDescription(getString(R.string.timer_desc, timeValue));
        
        fiftyFiftyButton.setContentDescription(getString(R.string.fifty_fifty_desc));
        hintButton.setContentDescription(getString(R.string.hint_desc));
        skipQuestionButton.setContentDescription(getString(R.string.skip_question_desc));
        quitButton.setContentDescription("Încheie quiz-ul");
        
        // Set content descriptions for answer buttons based on their text
        for (int i = 0; i < answerButtons.length; i++) {
            MaterialButton button = answerButtons[i];
            button.setContentDescription(getString(R.string.answer_option_desc, (i+1), button.getText()));
        }
        
        // Ensure minimum touch target size for better accessibility
        for (MaterialCardView card : answerCards) {
            card.setMinimumHeight((int) (48 * getResources().getDisplayMetrics().density));
        }
    }

    private void startTimer() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new CountDownTimer(TIME_PER_QUESTION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                timerTextView.setText(String.valueOf(secondsRemaining));
                timerTextView.setContentDescription(getString(R.string.timer_desc, secondsRemaining));
                
                // Adăugăm efect vizual când timpul este sub 10 secunde
                if (millisUntilFinished <= 10000) {
                    Animation pulse = AnimationUtils.loadAnimation(TransilvaniaGameActivity.this, R.anim.pulse);
                    timerTextView.startAnimation(pulse);
                    timerTextView.setTextColor(ContextCompat.getColor(TransilvaniaGameActivity.this, R.color.transilvania_accent));
                } else {
                    timerTextView.setTextColor(ContextCompat.getColor(TransilvaniaGameActivity.this, R.color.transilvania_text));
                }
            }

            @Override
            public void onFinish() {
                handleTimeout();
            }
        }.start();
    }

    private void handleTimeout() {
        // ✅ ÎMBUNĂTĂȚIRE: Feedback îmbunătățit pentru timeout
        provideHapticFeedback(HapticFeedbackType.WRONG);
        
        // Dezactivăm toate cardurile
        for (MaterialCardView card : answerCards) {
            card.setClickable(false);
        }
        
        // ✅ ÎMBUNĂTĂȚIRE: Animație pentru timeout - fade out toate răspunsurile
        for (MaterialCardView card : answerCards) {
            card.animate()
                .alpha(0.5f)
                .setDuration(300)
                .start();
        }
        
        // Actualizăm statisticile pentru întrebarea ratată
        totalQuestions++;
        streak = 0;
        updateStreak();
        updateScore(); // Pentru progress bar
        
        // ✅ ÎMBUNĂTĂȚIRE: Afișăm răspunsul corect pentru timeout
        if (firestoreQuestions != null && !firestoreQuestions.isEmpty() && 
            currentQuestionIndex < firestoreQuestions.size()) {
            QuestionModel currentQuestion = firestoreQuestions.get(currentQuestionIndex);
            String correctAnswer = currentQuestion.getCorrectAnswer();
            String fact = currentQuestion.getFact();
            
            // Evidențiem răspunsul corect
            highlightCorrectAnswer(correctAnswer);
            
            // Dialog pentru timeout cu informație educațională
            String timeoutMessage = "✅ Răspunsul corect era: " + correctAnswer;
            
            if (fact != null && !fact.isEmpty()) {
                timeoutMessage += "\n\n📚 " + fact;
            }
            
            // ✅ CORECTARE: Verificăm dacă este ultima întrebare pentru timeout
            boolean isLastQuestion = (currentQuestionIndex + 1) >= getQuestionsCount();
            String continueButtonText = isLastQuestion ? "🏁 Vezi rezultate" : "➡️ Următoarea întrebare";
            
            MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this)
                .setTitle("⏰ Timp expirat")
                .setMessage(timeoutMessage)
                .setPositiveButton(continueButtonText, (dialog, which) -> {
                    if (isLastQuestion) {
                        // Pentru ultima întrebare, mergem direct la finalizare
                        currentQuestionIndex++; // Incrementăm pentru a marca finalul
                        finishGame();
                    } else {
                        // Pentru întrebări normale, continuăm la următoarea
                        moveToNextQuestion();
                    }
                })
                .setCancelable(false);
            
            // ✅ ÎMBUNĂTĂȚIRE: Adăugăm buton de încheiere pentru timeout-uri
            if (!isLastQuestion) {
                dialogBuilder.setNegativeButton("🚪 Încheie quiz", (dialog, which) -> {
                    showConfirmQuitDialog();
                });
            }
            
            dialogBuilder.show();
        } else {
            // Fallback dacă nu avem întrebări
            Toast.makeText(this, "⏰ Timpul a expirat!", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(() -> {
                // ✅ CORECTARE: Verificăm dacă este ultima întrebare și în fallback
                if ((currentQuestionIndex + 1) >= getQuestionsCount()) {
                    currentQuestionIndex++; // Incrementăm pentru a marca finalul
                    finishGame();
                } else {
                    moveToNextQuestion();
                }
            }, 1500);
        }
        
        Log.d(TAG, "⏰ Timeout for question " + (currentQuestionIndex + 1) + ", streak reset to 0");
    }

    private void useFiftyFifty() {
        if (isFiftyFiftyUsed) {
            Toast.makeText(this, "🚫 Ai folosit deja acest ajutor!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // ✅ ÎMBUNĂTĂȚIRE: Confirmare înainte de folosire
        new MaterialAlertDialogBuilder(this)
            .setTitle("🎯 Confirmare 50/50")
            .setMessage("Vrei să folosești ajutorul 50/50?\n\n" +
                       "🎲 Vor fi eliminate 2 răspunsuri greșite aleatoriu.")
            .setPositiveButton("✓ Da, folosește", (dialog, which) -> {
                executeFiftyFifty();
            })
            .setNegativeButton("✗ Nu", null)
            .show();
    }
    
    /**
     * 🎯 ÎMBUNĂTĂȚIRE: Execuția efectivă a 50/50 cu animații și feedback
     */
    private void executeFiftyFifty() {
        // ✅ ÎMBUNĂTĂȚIRE: Verificăm enhanced questions în loc de firestore questions
        if (enhancedQuestions == null || enhancedQuestions.isEmpty() || 
            currentQuestionIndex >= enhancedQuestions.size()) {
            Toast.makeText(this, "❌ Nu se poate aplica ajutorul pentru această întrebare", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ ÎMBUNĂTĂȚIRE: Obținem întrebarea curentă din enhanced questions
        EnhancedQuestionModel currentQuestion = enhancedQuestions.get(currentQuestionIndex);
        String correctAnswer = currentQuestion.getCorrectAnswer();
        
        // ✅ ÎMBUNĂTĂȚIRE: Track lifelines used
        lifelinesUsed++;
        
        List<Integer> wrongAnswerIndices = new ArrayList<>();
        
        // Găsim indexurile răspunsurilor greșite
        for (int i = 0; i < answerButtons.length; i++) {
            if (answerButtons[i].getVisibility() == View.VISIBLE && 
                !answerButtons[i].getText().toString().equals(correctAnswer)) {
                wrongAnswerIndices.add(i);
            }
        }

        // Dacă avem cel puțin 2 răspunsuri greșite, eliminăm 2
        if (wrongAnswerIndices.size() >= 2) {
            Collections.shuffle(wrongAnswerIndices);
            
            // ✅ ÎMBUNĂTĂȚIRE: Animații în secvență pentru eliminarea răspunsurilor
            for (int i = 0; i < 2; i++) {
                int index = wrongAnswerIndices.get(i);
                answerButtons[index].setEnabled(false);
                
                // Animație îmbunătățită cu staggered delay
                answerCards[index].animate()
                    .alpha(0.3f)
                    .scaleX(0.85f)
                    .scaleY(0.85f)
                    .setDuration(400)
                    .setStartDelay(i * 200) // delay pentru fiecare card
                    .start();
                    
                answerCards[index].setClickable(false);
                answerCards[index].setStrokeColor(ContextCompat.getColor(this, R.color.button_disabled_background));
                
                // Adăugăm strikethrough text effect
                answerButtons[index].setPaintFlags(answerButtons[index].getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                answerButtons[index].setTextColor(ContextCompat.getColor(this, R.color.button_disabled_text));
            }
            
            // ✅ ÎMBUNĂTĂȚIRE: Feedback haptical pentru lifeline
            provideHapticFeedback(HapticFeedbackType.LIFELINE);
        }

        // Marchează butonul ca utilizat
        isFiftyFiftyUsed = true;
        fiftyFiftyButton.setEnabled(false);
        
        // Animație pentru dezactivarea butonului
        fiftyFiftyButton.animate()
            .alpha(0.6f)
            .setDuration(300)
            .start();
            
        // ✅ ÎMBUNĂTĂȚIRE: Feedback vizual îmbunătățit
        Toast.makeText(this, "🎯 S-au eliminat două răspunsuri incorecte!", Toast.LENGTH_SHORT).show();
        
        Log.d(TAG, "✅ 50-50 lifeline used successfully for question " + (currentQuestionIndex + 1));
    }

    private void skipQuestion() {
        if (isSkipUsed) {
            Toast.makeText(this, "🚫 Ai folosit deja acest ajutor!", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ ÎMBUNĂTĂȚIRE: Confirmare înainte de skip cu avertizare
        new MaterialAlertDialogBuilder(this)
            .setTitle("⏭️ Confirmare Skip")
            .setMessage("Vrei să treci la următoarea întrebare?\n\n" +
                       "⚠️ Nu vei primi puncte pentru această întrebare.")
            .setPositiveButton("✓ Da, treci", (dialog, which) -> {
                executeSkipQuestion();
            })
            .setNegativeButton("✗ Nu", null)
            .show();
    }
    
    /**
     * ⏭️ ÎMBUNĂTĂȚIRE: Execuția efectivă a skip-ului
     */
    private void executeSkipQuestion() {
        // ✅ ÎMBUNĂTĂȚIRE: Feedback haptical pentru skip
        provideHapticFeedback(HapticFeedbackType.LIFELINE);
        
        // ✅ ÎMBUNĂTĂȚIRE: Track lifelines used
        lifelinesUsed++;
        
        // Marchează întrebarea ca skip-uită (fără puncte)
        totalQuestions++;
        
        // Actualizează statisticile
        updateScore();
        updateStreak();
        
        // Trece la următoarea întrebare
        moveToNextQuestion();
        
        isSkipUsed = true;
        skipQuestionButton.setEnabled(false);
        skipQuestionButton.animate()
            .alpha(0.6f)
            .setDuration(300)
            .start();
            
        Toast.makeText(this, "⏭️ Întrebarea a fost omisă!", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "✅ Skip lifeline used for question " + currentQuestionIndex);
    }

    private void displayQuestion() {
        if (!isDataLoaded) {
            Log.d(TAG, "Data not loaded yet, waiting...");
            return;
        }
        
        // ✅ ÎMBUNĂTĂȚIRE: Verificăm enhanced questions în loc de firestore questions
        if (enhancedQuestions == null || enhancedQuestions.isEmpty()) {
            Log.e(TAG, "No enhanced questions available");
            showNoQuestionsError();
            return;
        }
        
        // ✅ ÎMBUNĂTĂȚIRE: Verifică dacă jocul trebuie să se termine bazat pe modul
        if (currentQuestionIndex >= enhancedQuestions.size() || 
            gameModeManager.isGameComplete(enhancedQuestions.size())) {
            Log.d(TAG, "All questions completed, finishing game");
            finishGame();
            return;
        }
        
        // Reset card styles
        resetCardStyles();
        
        // Actualizăm progress bar
        progressBar.setProgress(currentQuestionIndex + 1);
        
        // ✅ ÎMBUNĂTĂȚIRE: Afișăm întrebarea curentă din enhanced questions
        EnhancedQuestionModel currentQuestion = enhancedQuestions.get(currentQuestionIndex);
        questionTextView.setText(currentQuestion.getQuestion());
        
        // ✅ ÎMBUNĂTĂȚIRE: Marchează timpul de început pentru tracking
        questionStartTime = System.currentTimeMillis();
        
        // ✅ ÎMBUNĂTĂȚIRE: Verifică și aplică restricțiile lifeline bazate pe modul și dificultate
        updateLifelinesAvailability();
        
        Log.d(TAG, "Displaying question " + (currentQuestionIndex + 1) + "/" + enhancedQuestions.size() + 
                ": " + currentQuestion.getQuestion() + 
                " [Category: " + currentQuestion.getCategory().displayName + 
                ", Difficulty: " + currentQuestion.getDifficulty().displayName + "]");
        
        // Obținem toate răspunsurile
        String[] allAnswers = currentQuestion.getAnswers().toArray(new String[0]);
        
        // Amestecăm răspunsurile pentru varietate
        List<String> shuffledAnswers = new ArrayList<>();
        for (String answer : allAnswers) {
            shuffledAnswers.add(answer);
        }
        Collections.shuffle(shuffledAnswers);
        
        // Setăm textul butoanelor
        for (int i = 0; i < answerButtons.length; i++) {
            if (i < shuffledAnswers.size()) {
                answerButtons[i].setText(shuffledAnswers.get(i));
                answerCards[i].setVisibility(View.VISIBLE);
                
                // Setăm content description pentru accesibilitate
                answerButtons[i].setContentDescription("Răspuns " + (i + 1) + ": " + shuffledAnswers.get(i));
            } else {
                answerCards[i].setVisibility(View.GONE);
            }
        }
        
        // Setăm imaginea dacă există
        if (currentQuestion.getImageResourceId() != 0) {
            try {
                questionImage.setImageResource(currentQuestion.getImageResourceId());
                questionImage.setVisibility(View.VISIBLE);
                questionImage.setContentDescription("Imagine pentru întrebarea: " + currentQuestion.getQuestion());
                
                Log.d(TAG, "Image loaded for question: " + currentQuestion.getImageResourceId());
            } catch (Exception e) {
                Log.w(TAG, "Could not load image for question: " + currentQuestion.getImageResourceId(), e);
                questionImage.setVisibility(View.GONE);
            }
        } else {
            questionImage.setVisibility(View.GONE);
        }
        
        // Activăm toate cardurile pentru răspuns
        for (MaterialCardView card : answerCards) {
            card.setClickable(true);
            card.setEnabled(true);
        }
        
        // Resetăm lifeline-urile pentru întrebarea nouă
        resetLifelinesForNewQuestion();
        
        // Resetăm timerul
        if (timer != null) {
            timer.cancel();
        }
        startTimer();
        
        Log.d(TAG, "Question displayed successfully from Firestore");
    }
    
    /**
     * ✅ ÎMBUNĂTĂȚIRE: Actualizează disponibilitatea lifeline-urilor bazată pe modul și dificultate
     */
    private void updateLifelinesAvailability() {
        boolean lifelinesAllowed = gameModeManager.areLifelinesAllowed();
        int maxLifelines = gameModeManager.getMaxLifelines();
        boolean canUseMore = difficultyManager.canUseLifeline(lifelinesUsed);
        
        // Activează/dezactivează lifeline-urile
        fiftyFiftyButton.setEnabled(lifelinesAllowed && canUseMore && !isFiftyFiftyUsed);
        hintButton.setEnabled(lifelinesAllowed && canUseMore && !isHintUsed);
        skipQuestionButton.setEnabled(lifelinesAllowed && canUseMore && !isSkipUsed);
        
        // Actualizează opacitatea vizuală
        fiftyFiftyButton.setAlpha(fiftyFiftyButton.isEnabled() ? 1.0f : 0.5f);
        hintButton.setAlpha(hintButton.isEnabled() ? 1.0f : 0.5f);
        skipQuestionButton.setAlpha(skipQuestionButton.isEnabled() ? 1.0f : 0.5f);
        
        Log.d(TAG, "Lifelines updated - Allowed: " + lifelinesAllowed + 
               ", Max: " + maxLifelines + ", Used: " + lifelinesUsed);
    }
    
    /**
     * ✅ ÎMBUNĂTĂȚIRE: Resetează lifeline-urile complet pentru întrebarea nouă
     */
    private void resetLifelinesForNewQuestion() {
        isFiftyFiftyUsed = false;
        isHintUsed = false;
        isSkipUsed = false;
        
        // ✅ CORECTARE: Resetează starea butoanelor complet
        fiftyFiftyButton.setEnabled(true);
        fiftyFiftyButton.setAlpha(1.0f);
        
        hintButton.setEnabled(true);
        hintButton.setAlpha(1.0f);
        
        skipQuestionButton.setEnabled(true);
        skipQuestionButton.setAlpha(1.0f);
        
        // ✅ ÎMBUNĂTĂȚIRE: Reset strike-through effects pentru răspunsuri
        for (int i = 0; i < answerButtons.length; i++) {
            answerButtons[i].setPaintFlags(answerButtons[i].getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            answerButtons[i].setTextColor(ContextCompat.getColor(this, R.color.transilvania_text));
        }
        
        Log.d(TAG, "✅ Lifelines reset for question " + (currentQuestionIndex + 1));
    }

    private void resetCardStyles() {
        for (int i = 0; i < answerCards.length; i++) {
            // ✅ CORECTARE: Reset complet al culorilor pentru a elimina bug-ul cu butonul roșu
            MaterialCardView card = answerCards[i];
            
            // Reset card background color to default (FIX pentru bug-ul principal)
            card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white));
            
            // Reset card properties
            card.setStrokeColor(ContextCompat.getColor(this, R.color.transilvania_primary_light));
            card.setAlpha(1.0f);
            card.setClickable(true);
            card.setElevation(6f);
            card.setTranslationZ(0f);
            card.setScaleX(1.0f);
            card.setScaleY(1.0f);
            
            // Reset all button properties
            answerButtons[i].setEnabled(true);
            answerButtons[i].setTextColor(ContextCompat.getColor(this, R.color.transilvania_text));
            
            // Apply consistent corner radius for Material Design 3 feel
            card.setRadius(getResources().getDimension(R.dimen.card_corner_radius));
            
            // Set state list animator for touch feedback
            StateListAnimator stateListAnimator = AnimatorInflater.loadStateListAnimator(
                this, android.R.animator.fade_in);
            card.setStateListAnimator(stateListAnimator);
            
            // Add subtle entrance animation with staggered delay
            card.setAlpha(0.4f);
            card.setScaleX(0.95f);
            card.setScaleY(0.95f);
            card.animate()
                .alpha(1.0f)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(300)
                .setStartDelay(i * 50) // staggered animation
                .setInterpolator(new FastOutSlowInInterpolator())
                .start();
        }
        
        // ✅ ÎMBUNĂTĂȚIRE: Reset lifelines pentru noua întrebare
        resetLifelinesForNewQuestion();
    }

    private void checkAnswer(int selectedAnswerIndex, String selectedAnswer) {
        // Dezactivăm cardurile pentru a preveni răspunsuri multiple
        for (MaterialCardView card : answerCards) {
            card.setClickable(false);
        }
        
        // ✅ ÎMBUNĂTĂȚIRE: Verificăm enhanced questions în loc de firestore questions
        if (enhancedQuestions == null || enhancedQuestions.isEmpty() || 
            currentQuestionIndex >= enhancedQuestions.size()) {
            Log.e(TAG, "No enhanced questions available for answer checking");
            return;
        }
        
        // ✅ ÎMBUNĂTĂȚIRE: Obținem întrebarea curentă din enhanced questions
        EnhancedQuestionModel currentQuestion = enhancedQuestions.get(currentQuestionIndex);
        String correctAnswer = currentQuestion.getCorrectAnswer();
        String fact = currentQuestion.getFact();
        boolean isCorrect = selectedAnswer.equals(correctAnswer);
        
        // ✅ ÎMBUNĂTĂȚIRE: Calculează timpul petrecut pentru această întrebare
        long timeSpent = System.currentTimeMillis() - questionStartTime;
        
        // ✅ ÎMBUNĂTĂȚIRE: Track răspunsul pentru analytics
        String questionId = currentQuestion.getQuestion().hashCode() + "_" + currentQuestion.getCategory().name();
        progressTracker.trackAnswer(questionId, isCorrect, timeSpent, 
                                   currentQuestion.getCategory(), 
                                   currentQuestion.getDifficulty());
        
        // ✅ ÎMBUNĂTĂȚIRE: Track răspunsul pentru achievements
        achievementManager.recordTransilvaniaQuizAnswer(isCorrect, currentQuestion.getCategory().name(), 
                                                       timeSpent / 1000.0f, streak + (isCorrect ? 1 : 0));
        
        Log.d(TAG, "Checking answer: '" + selectedAnswer + "' vs correct: '" + correctAnswer + 
                "' -> " + (isCorrect ? "CORRECT" : "WRONG"));
        
        // Anulăm timerul
        if (timer != null) {
            timer.cancel();
        }
        
        // Actualizăm statisticile
        totalQuestions++;
        
        // Aplicăm stilurile corespunzătoare pentru răspuns
        MaterialCardView selectedCard = answerCards[selectedAnswerIndex];
        
        if (isCorrect) {
            // ✅ ÎMBUNĂTĂȚIRE: Feedback pentru răspuns corect
            provideCorrectAnswerFeedback(selectedCard, selectedAnswerIndex);
            
            // ✅ ÎMBUNĂTĂȚIRE: Calculează punctajul cu bonusuri pentru mod și dificultate
            int basePoints = POINTS_PER_CORRECT_ANSWER;
            int modeBonus = gameModeManager.calculateModeBonus(basePoints, isCorrect, timeSpent);
            int finalScore = difficultyManager.calculateFinalScore(basePoints + modeBonus);
            
            score += finalScore;
            streak++;
            if (streak > maxStreak) {
                maxStreak = streak;
            }
            
            // Bonus pentru streak
            if (streak >= STREAK_BONUS_THRESHOLD) {
                score += BONUS_POINTS;
                showStreakBonus();
            }
            
            // ✅ ÎMBUNĂTĂȚIRE: Log detaliat al punctajului
            Log.d(TAG, "Score calculation - Base: " + basePoints + 
                   ", Mode bonus: " + modeBonus + 
                   ", Final (with difficulty): " + finalScore);
            
            correctAnswers++;
            
            // Actualizăm scorul și streak-ul
            updateScore();
            updateStreak();
            
            Log.d(TAG, "✅ Correct answer! Score: " + score + ", Streak: " + streak);
            
            // Afișăm informația suplimentară
            showAnswerDialog(fact, true);
            
        } else {
            // ❌ ÎMBUNĂTĂȚIRE: Feedback pentru răspuns greșit
            provideWrongAnswerFeedback(selectedCard, selectedAnswerIndex, correctAnswer);
            
            // Resetăm streak-ul
            streak = 0;
            updateStreak();
            
            Log.d(TAG, "❌ Wrong answer! Correct was: " + correctAnswer + ", Streak reset to 0");
            
            // Afișăm informația suplimentară
            showAnswerDialog(fact, false);
        }
    }
    
    /**
     * ✅ ÎMBUNĂTĂȚIRE: Feedback uniform pentru răspuns corect
     */
    private void provideCorrectAnswerFeedback(MaterialCardView selectedCard, int selectedAnswerIndex) {
        // Culoare verde pentru răspuns corect
        selectedCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.correct_answer));
        answerButtons[selectedAnswerIndex].setTextColor(ContextCompat.getColor(this, R.color.white));
        
        // Vibration scurtă pentru răspuns corect
        provideHapticFeedback(HapticFeedbackType.CORRECT);
        
        // Animație de succes
        Animation pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse);
        selectedCard.startAnimation(pulseAnimation);
        
        // Efect de confetti pentru răspuns corect
        animateCorrectAnswer(selectedCard);
    }
    
    /**
     * ❌ ÎMBUNĂTĂȚIRE: Feedback uniform pentru răspuns greșit
     */
    private void provideWrongAnswerFeedback(MaterialCardView selectedCard, int selectedAnswerIndex, String correctAnswer) {
        // Culoare roșie pentru răspuns greșit
        selectedCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.wrong_answer));
        answerButtons[selectedAnswerIndex].setTextColor(ContextCompat.getColor(this, R.color.white));
        
        // Vibration pentru răspuns greșit (mai lungă)
        provideHapticFeedback(HapticFeedbackType.WRONG);
        
        // Găsim și evidențiem răspunsul corect
        highlightCorrectAnswer(correctAnswer);
        
        // Animație de eroare
        Animation shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake);
        selectedCard.startAnimation(shakeAnimation);
    }
    
    /**
     * 🔍 ÎMBUNĂTĂȚIRE: Evidențiere răspuns corect
     */
    private void highlightCorrectAnswer(String correctAnswer) {
        for (int i = 0; i < answerButtons.length; i++) {
            if (answerButtons[i].getText().toString().equals(correctAnswer)) {
                answerCards[i].setCardBackgroundColor(ContextCompat.getColor(this, R.color.correct_answer));
                answerButtons[i].setTextColor(ContextCompat.getColor(this, R.color.white));
                
                // ✅ CORECTARE: Fac variabila final pentru lambda expression
                final int finalIndex = i;
                
                // Animație subtilă pentru răspunsul corect
                answerCards[finalIndex].animate()
                    .scaleX(1.05f)
                    .scaleY(1.05f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        answerCards[finalIndex].animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(200)
                            .start();
                    })
                    .start();
                break;
            }
        }
    }
    
    /**
     * 📱 ÎMBUNĂTĂȚIRE: Feedback haptical
     */
    private enum HapticFeedbackType {
        CORRECT, WRONG, LIFELINE
    }
    
    private void provideHapticFeedback(HapticFeedbackType type) {
        try {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    switch (type) {
                        case CORRECT:
                            // Vibration scurtă și plăcută pentru răspuns corect
                            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                            break;
                        case WRONG:
                            // Vibration dublă pentru răspuns greșit
                            vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0, 150, 100, 150}, -1));
                            break;
                        case LIFELINE:
                            // Vibration subtilă pentru lifeline
                            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
                            break;
                    }
                } else {
                    // Pentru versiunile mai vechi de Android
                    switch (type) {
                        case CORRECT:
                            vibrator.vibrate(100);
                            break;
                        case WRONG:
                            vibrator.vibrate(new long[]{0, 150, 100, 150}, -1);
                            break;
                        case LIFELINE:
                            vibrator.vibrate(50);
                            break;
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not provide haptic feedback", e);
        }
    }
    
    /**
     * 🎉 ÎMBUNĂTĂȚIRE: Animație pentru răspuns corect
     */
    private void animateCorrectAnswer(MaterialCardView card) {
        // Efect de glow verde
        card.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(150)
            .withEndAction(() -> {
                card.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(150)
                    .start();
            })
            .start();
    }
    
    /**
     * 💬 ÎMBUNĂTĂȚIRE: Dialog uniform pentru răspunsuri cu logică pentru ultima întrebare + buton încheiere
     */
    private void showAnswerDialog(String fact, boolean isCorrect) {
        if (fact != null && !fact.isEmpty()) {
            String title = isCorrect ? "✅ Răspuns corect!" : "❌ Răspuns greșit";
            String emoji = isCorrect ? "🎉" : "📚";
            
            // ✅ CORECTARE: Verificăm dacă este ultima întrebare
            boolean isLastQuestion = (currentQuestionIndex + 1) >= getQuestionsCount();
            String continueButtonText = isLastQuestion ? "🏁 Vezi rezultate" : "➡️ Următoarea întrebare";
            
            MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(emoji + " " + fact)
                .setPositiveButton(continueButtonText, (dialog, which) -> {
                    dialog.dismiss(); // Asigurăm că dialog-ul se închide mai întâi
                    if (isLastQuestion) {
                        // Pentru ultima întrebare, mergem direct la finalizare
                        currentQuestionIndex++; // Incrementăm pentru a marca finalul
                        finishGame();
                    } else {
                        // Pentru întrebări normale, continuăm la următoarea
                        Log.d(TAG, "🔄 User chose to continue to next question from dialog");
                        moveToNextQuestion();
                    }
                })
                .setCancelable(false);
            
            // ✅ ÎMBUNĂTĂȚIRE: Adăugăm buton de încheiere pentru toate întrebările
            if (!isLastQuestion) {
                dialogBuilder.setNegativeButton("🚪 Încheie quiz", (dialog, which) -> {
                    dialog.dismiss(); // Asigurăm că dialog-ul se închide mai întâi
                    // Confirmăm înainte de a încheia
                    showConfirmQuitDialog();
                });
            }
            
            dialogBuilder.show();
        } else {
            // Pentru cazurile fără fact, afișăm un dialog simplu cu opțiunile
            showQuickActionDialog();
        }
    }
    
    /**
     * 🚪 ÎMBUNĂTĂȚIRE: Dialog rapid pentru acțiuni când nu avem fact
     */
    private void showQuickActionDialog() {
        boolean isLastQuestion = (currentQuestionIndex + 1) >= getQuestionsCount();
        String continueButtonText = isLastQuestion ? "🏁 Vezi rezultate" : "➡️ Următoarea întrebare";
        
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this)
            .setTitle("Continuă quiz-ul?")
            .setMessage("Vrei să continui la următoarea întrebare?")
            .setPositiveButton(continueButtonText, (dialog, which) -> {
                dialog.dismiss(); // Asigurăm că dialog-ul se închide mai întâi
                if (isLastQuestion) {
                    currentQuestionIndex++; // Incrementăm pentru a marca finalul
                    finishGame();
                } else {
                    Log.d(TAG, "🔄 User chose to continue to next question from quick dialog");
                    moveToNextQuestion();
                }
            })
            .setCancelable(false);
        
        // Adăugăm buton de încheiere dacă nu e ultima întrebare
        if (!isLastQuestion) {
            dialogBuilder.setNegativeButton("🚪 Încheie quiz", (dialog, which) -> {
                dialog.dismiss(); // Asigurăm că dialog-ul se închide mai întâi
                showConfirmQuitDialog();
            });
        }
        
        dialogBuilder.show();
    }
    
    /**
     * 🚪 ÎMBUNĂTĂȚIRE: Dialog de confirmare pentru ieșirea din quiz
     */
    private void showConfirmQuitDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("🚪 Confirmare ieșire")
            .setMessage("Ești sigur că vrei să închei quiz-ul?\n\n" +
                       "📊 Progresul tău:\n" +
                       "• Întrebări răspunse: " + currentQuestionIndex + " din " + getQuestionsCount() + "\n" +
                       "• Scor curent: " + score + " puncte\n" +
                       "• Răspunsuri corecte: " + correctAnswers + "\n\n" +
                       "⚠️ Dacă ieși acum, progresul va fi salvat parțial.")
            .setPositiveButton("✓ Da, încheie", (dialog, which) -> {
                dialog.dismiss();
                // Salvăm progresul parțial și ieșim
                finishGame();
            })
            .setNegativeButton("✗ Nu, continuă", (dialog, which) -> {
                dialog.dismiss();
                // ✅ BUG FIX: Când utilizatorul alege să continue, asigurăm că cardurile sunt reactivate
                Log.d(TAG, "🔄 User chose to continue quiz from quit dialog");
                
                // Re-activăm cardurile pentru ca utilizatorul să poată răspunde
                for (MaterialCardView card : answerCards) {
                    card.setClickable(true);
                    card.setEnabled(true);
                }
                for (MaterialButton button : answerButtons) {
                    button.setEnabled(true);
                    button.setClickable(true);
                }
                
                // Repornește timer-ul dacă nu este activ
                if (timer == null) {
                    startTimer();
                }
            })
            .show();
    }

    private void moveToNextQuestion() {
        currentQuestionIndex++;
        
        Log.d(TAG, "🔄 Moving to next question: " + (currentQuestionIndex + 1) + "/" + getQuestionsCount());
        
        if (currentQuestionIndex < getQuestionsCount()) {
            // ✅ BUG FIX: Asigurăm că toate cardurile sunt re-activate și reset-ate complet
            resetCardStyles();
            
            // ✅ EXTRA SAFEGUARD: Re-activăm manual cardurile pentru siguranță
            for (MaterialCardView card : answerCards) {
                card.setClickable(true);
                card.setEnabled(true);
            }
            for (MaterialButton button : answerButtons) {
                button.setEnabled(true);
                button.setClickable(true);
            }
            
            // ✅ BUG FIX: Afișăm întrebarea și actualizăm UI-ul
            displayQuestion();
            updateScore(); // Actualizez progress bar-ul corect
            
            // ✅ BUG FIX: Repornesc timer-ul pentru următoarea întrebare
            startTimer();
            
            // ✅ DEBUG: Verificăm starea finală a cardurilor
            boolean cardsClickable = true;
            for (MaterialCardView card : answerCards) {
                if (!card.isClickable()) {
                    cardsClickable = false;
                    break;
                }
            }
            
            Log.d(TAG, "✅ Successfully moved to question " + (currentQuestionIndex + 1) + "/" + getQuestionsCount() + 
                    ", All cards clickable: " + cardsClickable + 
                    ", Timer active: " + (timer != null));
        } else {
            // ✅ CORECTARE: Mergem direct la finalizare în loc de showFinishButton
            Log.d(TAG, "🏁 Quiz completed! Starting finish game.");
            finishGame();
        }
    }

    private void updateScore() {
        // ✅ CORECTARE: Afișez doar scorul, fără indicator sursă
        scoreTextView.setText(String.valueOf(score));
        
        // ✅ BUG FIX: Calculez progress bar-ul corect (currentQuestionIndex + 1 pentru întrebarea curentă)
        int progress = Math.min(100, ((currentQuestionIndex + 1) * 100) / getQuestionsCount());
        progressBar.setProgress(progress);
        
        Log.d(TAG, "Score updated: " + score + ", Progress: " + progress + "% (" + 
                (currentQuestionIndex + 1) + "/" + getQuestionsCount() + ")");
    }

    private void updateStreak() {
        streakTextView.setText(String.valueOf(streak));
    }

    private String getAchievements() {
        List<String> achievements = new ArrayList<>();
        
        if (maxStreak >= 5) {
            achievements.add("Geniu Transilvănean (serie de " + maxStreak + " răspunsuri corecte)");
        } else if (maxStreak >= 3) {
            achievements.add("Cunoscător al Transilvaniei (serie de " + maxStreak + " răspunsuri corecte)");
        }
        
        if (correctAnswers == getQuestionsCount()) {
            achievements.add("Perfect! Toate răspunsurile corecte");
        } else if (correctAnswers >= getQuestionsCount() * 0.8) {
            achievements.add("Expert al Transilvaniei (" + correctAnswers + " din " + getQuestionsCount() + " corecte)");
        } else if (correctAnswers >= getQuestionsCount() * 0.5) {
            achievements.add("Bun cunoscător (" + correctAnswers + " din " + getQuestionsCount() + " corecte)");
        }
        
        if (achievements.isEmpty()) {
            return "Nicio realizare specială. Poți face mai bine data viitoare!";
        }
        
        StringBuilder result = new StringBuilder("Realizări:");
        for (String achievement : achievements) {
            result.append("\n• ").append(achievement);
        }
        
        return result.toString();
    }

    private void finishGame() {
        if (timer != null) {
            timer.cancel();
        }

        // ✅ ÎMBUNĂTĂȚIRE: Închide sesiunea de tracking și actualizează dificultatea
        progressTracker.endSession(score, gameModeManager.getCurrentGameMode());
        difficultyManager.updateDifficultyAfterGame(correctAnswers, getQuestionsCount(), totalTime);
        
        // ✅ ÎMBUNĂTĂȚIRE: Verifică pentru noi achievements
        List<String> newAchievements = progressTracker.checkForNewAchievements();
        if (!newAchievements.isEmpty()) {
            Log.d(TAG, "New achievements unlocked: " + newAchievements);
        }

        // ✅ ACHIEVEMENT TRACKING: Update Transilvania specific achievements
        achievementManager.incrementTransilvaniaQuizCompletions();
        
        // Check for perfect score
        if (correctAnswers == getQuestionsCount()) {
            achievementManager.recordTransilvaniaPerfectScore();
        } else {
            achievementManager.breakTransilvaniaPerfectStreak();
        }
        
        // Update game mode achievements
        String gameModeName = gameModeManager.getCurrentGameMode().name();
        achievementManager.incrementTransilvaniaGameModeCompletion(gameModeName);
        
        // Update difficulty unlock achievements
        DifficultyManager.DifficultyLevel currentDifficulty = difficultyManager.getCurrentDifficulty();
        if (currentDifficulty != DifficultyManager.DifficultyLevel.BEGINNER) { // If above Beginner
            String difficultyName = "";
            switch (currentDifficulty) {
                case NORMAL:
                    difficultyName = "intermediate";
                    break;
                case ADVANCED:
                    difficultyName = "advanced";
                    break;
                case EXPERT:
                    difficultyName = "expert";
                    break;
                case MASTER:
                    difficultyName = "master";
                    break;
            }
            if (!difficultyName.isEmpty()) {
                achievementManager.updateTransilvaniaDifficultyUnlock(difficultyName);
            }
        }
        
        // Refresh all achievements to check for any new unlocks
        achievementManager.refreshAllAchievements();

        // Adăugăm punctele în contul utilizatorului
        pointsManager.addPoints(this, "transilvania", score);
        
        // Salvăm rezultatul în sistemul hibrid (local + cloud)
        saveQuizResultToHybridStorage();
        
        // Salvăm rezultatul quiz-ului într-o structură organizată pentru user profile și leaderboard
        saveQuizResultToFirebase();
        
        // Calculăm noile achievement-uri deblocate
        List<AchievementManager.Achievement> newlyUnlocked = achievementManager.getUnlockedAchievements()
            .stream()
            .filter(achievement -> achievement.getRegion() != null && achievement.getRegion().equals("Transilvania"))
            .collect(java.util.stream.Collectors.toList());
        
        String achievementMessage = getAchievements();
        if (!newlyUnlocked.isEmpty()) {
            achievementMessage += "\n\n🏆 Achievement-uri noi deblocate: " + newlyUnlocked.size();
        }
        
        // ✅ ÎMBUNĂTĂȚIRE: Navigăm la activitatea de rezultate în loc să afișăm dialog
        Intent resultIntent = new Intent(this, TransilvaniaGameResultActivity.class);
        resultIntent.putExtra("score", score);
        resultIntent.putExtra("correctAnswers", correctAnswers);
        resultIntent.putExtra("totalQuestions", getQuestionsCount());
        resultIntent.putExtra("maxStreak", maxStreak);
        resultIntent.putExtra("totalTime", totalTime);
        resultIntent.putExtra("lifelinesUsed", lifelinesUsed);
        
        // Începem activitatea de rezultate și închidem activitatea curentă
        startActivity(resultIntent);
        finish();
    }
    
    /**
     * 🔄 SISTEM HIBRID: Salvează rezultatul quiz-ului în sistemul hibrid (local + cloud)
     */
    private void saveQuizResultToHybridStorage() {
        // Creăm datele pentru salvare hibridă
        Map<String, Object> quizResultData = new HashMap<>();
        quizResultData.put("score", score);
        quizResultData.put("correctAnswers", correctAnswers);
        quizResultData.put("totalQuestions", getQuestionsCount());
        quizResultData.put("maxStreak", maxStreak);
        quizResultData.put("totalTime", totalTime);
        quizResultData.put("accuracy", ((float) correctAnswers / getQuestionsCount()) * 100);
        quizResultData.put("region", REGION);
        quizResultData.put("gameType", GAME_TYPE);
        quizResultData.put("completedAt", System.currentTimeMillis());
        quizResultData.put("lifelinesUsed", lifelinesUsed);
        
        // Adăugăm date despre dificultate și mod de joc
        if (difficultyManager != null) {
            quizResultData.put("difficulty", difficultyManager.getCurrentDifficulty().name());
        }
        if (gameModeManager != null && gameModeManager.getCurrentGameMode() != null) {
            quizResultData.put("gameMode", gameModeManager.getCurrentGameMode().name());
        }
        
        // Generăm un ID unic pentru acest quiz
        String quizId = "transilvania_quiz_" + System.currentTimeMillis();
        
        // Salvăm în sistemul hibrid
        syncManager.saveData("quiz_results", quizId, quizResultData, new SyncManager.SyncCallback() {
            @Override
            public void onSyncComplete(boolean success, String message) {
                if (success) {
                    Log.d(TAG, "✅ Quiz result saved to hybrid storage: " + message);
                } else {
                    Log.w(TAG, "⚠️ Quiz result hybrid storage failed: " + message);
                }
            }
        });
        
        Log.d(TAG, "🔄 Quiz result submitted to hybrid storage system");
    }
    
    /**
     * Salvează rezultatul quiz-ului într-o structură organizată pentru user profile și leaderboard
     */
    private void saveQuizResultToFirebase() {
        // Verificăm dacă utilizatorul este autentificat
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.w(TAG, "Utilizatorul nu este autentificat, nu se poate salva rezultatul în clasament");
            Toast.makeText(this, "Trebuie să fii autentificat pentru a apărea în clasament", Toast.LENGTH_LONG).show();
            return;
        }
        
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        // Creăm obiectul QuizResult principal
        QuizResult quizResult = createQuizResult(userId);
        
        // Salvăm rezultatul în Firebase cu metode separate pentru organizare
        saveToQuizResults(quizResult);
        saveToUserActivityHistory(quizResult);
        saveToLeaderboardData(quizResult);
        
        // Actualizăm profilul utilizatorului
        updateUserProfileStats(quizResult);
        
        Log.d(TAG, "Transilvania Quiz Result saved - Score: " + score + ", Region: " + REGION + ", GameType: " + GAME_TYPE);
    }
    
    /**
     * Creează obiectul QuizResult cu toate datele necesare
     */
    private QuizResult createQuizResult(String userId) {
        QuizResult quizResult = new QuizResult();
        quizResult.setUserId(userId);
        quizResult.setScore(score);
        quizResult.setCorrectAnswers(correctAnswers);
        quizResult.setTotalQuestions(getQuestionsCount());
        quizResult.setMaxStreak(maxStreak);
        quizResult.setTotalTime(totalTime);
        quizResult.setRegion(REGION);
        quizResult.setGameType(GAME_TYPE);
        quizResult.setCompletedAt(new Date());
        
        // Adăugăm metadate specifice pentru Transilvania
        quizResult.setQuizId("transilvania_main_quiz_" + System.currentTimeMillis());
        
        return quizResult;
    }
    
    /**
     * Salvează în colecția principală quiz_results
     */
    private void saveToQuizResults(QuizResult quizResult) {
        FirestoreQuestionRepository.getInstance().saveQuizResult(quizResult)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "Quiz result saved to main collection with ID: " + documentReference.getId());
                Toast.makeText(this, "Rezultatul a fost adăugat în clasament!", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error saving quiz result to main collection", e);
                Toast.makeText(this, "Eroare la salvarea rezultatului în clasament", Toast.LENGTH_SHORT).show();
            });
    }
    
    /**
     * Salvează în colecția pentru activitatea recentă a utilizatorului
     */
    private void saveToUserActivityHistory(QuizResult quizResult) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Creăm un document pentru activitatea recentă
        Map<String, Object> activityData = new HashMap<>();
        activityData.put("userId", quizResult.getUserId());
        activityData.put("activityType", "transilvania_quiz");
        activityData.put("displayName", "Quiz Transilvania");
        activityData.put("score", quizResult.getScore());
        activityData.put("accuracy", quizResult.getAccuracy());
        activityData.put("correctAnswers", quizResult.getCorrectAnswers());
        activityData.put("totalQuestions", quizResult.getTotalQuestions());
        activityData.put("maxStreak", quizResult.getMaxStreak());
        activityData.put("region", REGION);
        activityData.put("gameType", GAME_TYPE);
        activityData.put("completedAt", quizResult.getCompletedAt());
        activityData.put("duration", totalTime);
        
        // Adăugăm detalii specifice pentru afișare în profil
        activityData.put("iconResource", "ic_transilvania");
        activityData.put("colorTheme", "transilvania_primary");
        activityData.put("description", "Quiz despre Transilvania - " + correctAnswers + "/" + getQuestionsCount() + " corecte");
        
        db.collection("user_activity_history")
            .document(quizResult.getUserId())
            .collection("recent_activities")
            .add(activityData)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "Activity saved to user history with ID: " + documentReference.getId());
                
                // Păstrăm doar ultimele 20 de activități pentru fiecare utilizator
                limitUserActivityHistory(quizResult.getUserId());
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error saving activity to user history", e);
            });
    }
    
    /**
     * Salvează datele pentru leaderboard (cel mai bun scor)
     */
    private void saveToLeaderboardData(QuizResult quizResult) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = quizResult.getUserId();
        
        // Verificăm mai întâi dacă e cel mai bun scor pentru această regiune
        db.collection("user_best_scores")
            .document(userId)
            .collection("regional_scores")
            .document(REGION + "_" + GAME_TYPE)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                boolean shouldUpdate = false;
                
                if (!documentSnapshot.exists()) {
                    shouldUpdate = true;
                    Log.d(TAG, "No existing score found for " + REGION + ", saving new best score");
                } else {
                    Long existingScore = documentSnapshot.getLong("score");
                    if (existingScore == null || quizResult.getScore() > existingScore) {
                        shouldUpdate = true;
                        Log.d(TAG, "New score (" + quizResult.getScore() + ") is better than existing (" + existingScore + ")");
                    }
                }
                
                if (shouldUpdate) {
                    updateBestScoreForLeaderboard(quizResult);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error checking existing best score", e);
                // În caz de eroare, salvăm oricum
                updateBestScoreForLeaderboard(quizResult);
            });
    }
    
    /**
     * Actualizează cel mai bun scor pentru leaderboard
     */
    private void updateBestScoreForLeaderboard(QuizResult quizResult) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        
        Map<String, Object> bestScoreData = new HashMap<>();
        bestScoreData.put("userId", quizResult.getUserId());
        bestScoreData.put("username", currentUser.getEmail());
        bestScoreData.put("displayName", currentUser.getDisplayName() != null ? 
                currentUser.getDisplayName() : "Explorator Transilvania");
        bestScoreData.put("profileImageUrl", currentUser.getPhotoUrl() != null ? 
                currentUser.getPhotoUrl().toString() : "");
        bestScoreData.put("score", quizResult.getScore());
        bestScoreData.put("accuracy", quizResult.getAccuracy());
        bestScoreData.put("maxStreak", quizResult.getMaxStreak());
        bestScoreData.put("region", REGION);
        bestScoreData.put("gameType", GAME_TYPE);
        bestScoreData.put("achievedAt", quizResult.getCompletedAt());
        bestScoreData.put("leaderboardCategory", "transilvania_quiz_masters");
        
        // Salvăm în colecția pentru cel mai bun scor personal
        db.collection("user_best_scores")
            .document(quizResult.getUserId())
            .collection("regional_scores")
            .document(REGION + "_" + GAME_TYPE)
            .set(bestScoreData)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Best score updated for user in " + REGION);
                
                // Actualizăm și în leaderboard-ul global
                updateGlobalLeaderboard(bestScoreData);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error updating best score", e);
            });
    }
    
    /**
     * Actualizează leaderboard-ul global
     */
    private void updateGlobalLeaderboard(Map<String, Object> bestScoreData) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String leaderboardId = REGION + "_" + GAME_TYPE;
        
        db.collection("leaderboards")
            .document(leaderboardId)
            .collection("entries")
            .document((String) bestScoreData.get("userId"))
            .set(bestScoreData)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Global leaderboard updated successfully");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error updating global leaderboard", e);
            });
    }
    
    /**
     * Actualizează statisticile generale ale utilizatorului
     */
    private void updateUserProfileStats(QuizResult quizResult) {
        pointsManager.addPoints(this, REGION, quizResult.getScore());
        
        // Actualizăm statisticile în profilul utilizatorului
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        com.google.firebase.firestore.DocumentReference userRef = db.collection("users").document(quizResult.getUserId());
        
        userRef.get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Actualizăm statisticile existente
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("lastActivity", new Date());
                    updates.put("lastRegionPlayed", REGION);
                    updates.put("totalTransilvaniaQuizzes", FieldValue.increment(1));
                    updates.put("totalTransilvaniaPoints", FieldValue.increment(quizResult.getScore()));
                    
                    userRef.update(updates)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "User profile stats updated"))
                        .addOnFailureListener(e -> Log.e(TAG, "Error updating user profile stats", e));
                }
            });
    }
    
    /**
     * Limitează istoricul de activități la ultimele 20
     */
    private void limitUserActivityHistory(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        db.collection("user_activity_history")
            .document(userId)
            .collection("recent_activities")
            .orderBy("completedAt", Query.Direction.DESCENDING)
            .limit(25) // Obținem cu 5 mai multe pentru a șterge excesul
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots.size() > 20) {
                    // Ștergem activitățile mai vechi de 20
                    for (int i = 20; i < queryDocumentSnapshots.size(); i++) {
                        queryDocumentSnapshots.getDocuments().get(i).getReference().delete();
                    }
                    Log.d(TAG, "Cleaned up old activities, kept latest 20");
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error cleaning up activity history", e);
            });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
        
        // 💾 CACHE UPDATE: Actualizăm cache-ul la ieșire dacă avem întrebări încărcate
        if (firestoreQuestions != null && !firestoreQuestions.isEmpty() && syncManager.isInternetAvailable()) {
            Log.d(TAG, "💾 Actualizez cache-ul local la ieșire cu " + firestoreQuestions.size() + " întrebări");
            saveQuestionsToLocalCache(firestoreQuestions);
        }
    }

    // New method to show finish button at the end
    private void showFinishButton() {
        // Hide all answer cards to avoid overlap
        for (MaterialCardView card : answerCards) {
            card.setVisibility(View.GONE);
        }
        
        // Set content description for accessibility
        finishButton.setContentDescription(getString(R.string.finish_game_desc));
        
        // Show a stylized completion message
        questionTextView.setText("Quiz complet! Felicitări!");
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeIn.setDuration(700);
        questionTextView.startAnimation(fadeIn);
        
        // Update the progress bar to show completion
        progressBar.setProgress(getQuestionsCount());
        progressBar.setContentDescription(getString(R.string.progress_desc, getQuestionsCount(), getQuestionsCount()));
        
        // Animate the progress bar
        progressBar.animate()
            .scaleY(1.2f)
            .setDuration(300)
            .withEndAction(() -> {
                progressBar.animate()
                    .scaleY(1.0f)
                    .setDuration(200)
                    .start();
            })
            .start();
        
        // Show finish button with enhanced animations
        finishButton.setVisibility(View.VISIBLE);
        Animation springOvershoot = AnimationUtils.loadAnimation(this, R.anim.spring_overshoot);
        finishButton.startAnimation(springOvershoot);
        
        // Add pulse animation after scale in
        new Handler().postDelayed(() -> {
            Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
            pulse.setRepeatCount(Animation.INFINITE);
            pulse.setRepeatMode(Animation.REVERSE);
            finishButton.startAnimation(pulse);
            
            // Add subtle elevation animation for Material Design depth effect
            float originalElevation = finishButton.getElevation();
            ValueAnimator elevationAnimator = ValueAnimator.ofFloat(originalElevation, originalElevation + 6f, originalElevation);
            elevationAnimator.setDuration(1500);
            elevationAnimator.setRepeatCount(ValueAnimator.INFINITE);
            elevationAnimator.setRepeatMode(ValueAnimator.REVERSE);
            elevationAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            elevationAnimator.addUpdateListener(animation -> {
                float value = (float) animation.getAnimatedValue();
                finishButton.setElevation(value);
            });
            elevationAnimator.start();
            
        }, 700);
        
        // Display the score with a celebratory animation
        String scoreMessage = "Scor final: " + score + " puncte";
        TextView scoreView = new TextView(this);
        scoreView.setText(scoreMessage);
        scoreView.setTextSize(20);
        scoreView.setTextColor(ContextCompat.getColor(this, R.color.transilvania_primary));
        scoreView.setTypeface(Typeface.DEFAULT_BOLD);
        scoreView.setGravity(Gravity.CENTER);
        
        // Add the score view below the question text
        ConstraintLayout layout = findViewById(R.id.main_constraint_layout);
        ConstraintSet constraintSet = new ConstraintSet();
        scoreView.setId(View.generateViewId());
        layout.addView(scoreView);
        
        constraintSet.clone(layout);
        constraintSet.connect(scoreView.getId(), ConstraintSet.TOP, questionTextView.getId(), ConstraintSet.BOTTOM, 24);
        constraintSet.connect(scoreView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        constraintSet.connect(scoreView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        constraintSet.applyTo(layout);
        
        // Animate the score view
        scoreView.setAlpha(0f);
        scoreView.setScaleX(0.7f);
        scoreView.setScaleY(0.7f);
        scoreView.animate()
            .alpha(1f)
            .scaleX(1.0f)
            .scaleY(1.0f)
            .setDuration(800)
            .setStartDelay(500)
            .setInterpolator(new OvershootInterpolator())
            .start();
        
        if (timer != null) {
            timer.cancel();
        }
    }

    /**
     * 💡 ÎMBUNĂTĂȚIRE: Shows a hint for the current question with confirmation
     */
    private void showHint() {
        if (isHintUsed) {
            Toast.makeText(this, "🚫 Ai folosit deja acest ajutor!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // ✅ ÎMBUNĂTĂȚIRE: Confirmare înainte de afișarea hint-ului
        new MaterialAlertDialogBuilder(this)
            .setTitle("💡 Confirmare Hint")
            .setMessage("Vrei să vezi un indiciu pentru această întrebare?\n\n" +
                       "🧠 Indiciul poate să te ajute să găsești răspunsul corect.")
            .setPositiveButton("✓ Da, arată", (dialog, which) -> {
                executeShowHint();
            })
            .setNegativeButton("✗ Nu", null)
            .show();
    }
    
    /**
     * 💡 ÎMBUNĂTĂȚIRE: Execuția efectivă a hint-ului
     */
    private void executeShowHint() {
        if (firestoreQuestions == null || firestoreQuestions.isEmpty() || 
            currentQuestionIndex >= firestoreQuestions.size()) {
            Toast.makeText(this, "❌ Nu există hint disponibil pentru această întrebare", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Obținem hint-ul din întrebarea curentă din Firestore
        QuestionModel currentQuestion = firestoreQuestions.get(currentQuestionIndex);
        String hint = currentQuestion.getFact();
        
        if (hint == null || hint.isEmpty()) {
            hint = "💡 Gândește-te la legăturile istorice și geografice ale Transilvaniei!";
        }
        
        // ✅ ÎMBUNĂTĂȚIRE: Dialog îmbunătățit pentru hint
        new MaterialAlertDialogBuilder(this)
            .setTitle("💡 Indiciu pentru întrebare")
            .setMessage("🧠 " + hint)
            .setPositiveButton("👍 Mulțumesc", null)
            .show();
        
        // ✅ ÎMBUNĂTĂȚIRE: Feedback haptical pentru hint
        provideHapticFeedback(HapticFeedbackType.LIFELINE);
        
        isHintUsed = true;
        hintButton.setEnabled(false);
        hintButton.animate()
            .alpha(0.6f)
            .setDuration(300)
            .start();
        
        Toast.makeText(this, "💡 Hint afișat!", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "✅ Hint displayed for question: " + currentQuestion.getQuestion());
    }

    /**
     * Loads the next question in the quiz
     */
    private void loadNextQuestion() {
        // Reset state for new question
        isFiftyFiftyUsed = false;
        isHintUsed = false;
        isSkipUsed = false;
        
        // Re-enable lifeline buttons
        fiftyFiftyButton.setEnabled(true);
        fiftyFiftyButton.setAlpha(1.0f);
        hintButton.setEnabled(true);
        hintButton.setAlpha(1.0f);
        skipQuestionButton.setEnabled(true);
        skipQuestionButton.setAlpha(1.0f);
        
        // Enable all answer buttons
        for (int i = 0; i < answerButtons.length; i++) {
            answerButtons[i].setEnabled(true);
            answerCards[i].setEnabled(true);
            answerCards[i].setClickable(true);
            answerCards[i].setAlpha(1.0f);
            answerCards[i].setScaleX(1.0f);
            answerCards[i].setScaleY(1.0f);
            answerCards[i].setStrokeColor(ContextCompat.getColor(this, R.color.transilvania_primary_light));
        }
        
        // Move to next question
        currentQuestionIndex++;
        displayQuestion();
    }

    /**
     * ✅ ÎMBUNĂTĂȚIRE: Returnează numărul total de întrebări din enhanced questions
     */
    private int getQuestionsCount() {
        if (enhancedQuestions != null) {
            return enhancedQuestions.size();
        }
        return 0; // Nu avem întrebări
    }

    private void showStreakBonus() {
        Toast.makeText(this, "Bonus serie: +" + BONUS_POINTS + " puncte!", Toast.LENGTH_SHORT).show();
        Animation bounceAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);
        streakTextView.startAnimation(bounceAnim);
    }
} 