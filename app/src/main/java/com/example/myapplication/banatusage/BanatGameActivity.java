package com.example.myapplication.banatusage;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import com.example.myapplication.R;
import com.example.myapplication.RomApp.Banat;
import com.example.myapplication.RomApp.PointsManager;
import com.example.myapplication.models.EnhancedQuestionModel;
import com.example.myapplication.banatusage.DifficultyManager;
import com.example.myapplication.banatusage.GameModeManager;
import com.example.myapplication.banatusage.PlayerProgressTracker;
import com.example.myapplication.utils.HapticFeedbackType;
import com.example.myapplication.utils.RegionGameEnhancer;
import com.example.myapplication.utils.SyncManager;
import com.example.myapplication.models.QuestionModel;
import com.example.myapplication.model.QuizResult;
import com.example.myapplication.repository.FirestoreQuestionRepository;
import com.example.myapplication.utils.GameOverHelper;
import com.example.myapplication.Joc1.AchievementManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Arrays;
import android.content.Context;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.os.Build;
import android.util.Log;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.Date;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.auth.FirebaseUser;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.widget.LinearLayout;

public class BanatGameActivity extends AppCompatActivity {
    private static final String TAG = "BanatGameActivity";
    private static final String REGION = "banat";
    private static final String GAME_TYPE = "quiz";
    
    // UI Components
    private TextView questionTextView;
    private MaterialButton[] answerButtons;
    private TextView scoreTextView;
    private ProgressBar progressBar;
    private TextView timerTextView;
    private TextView streakTextView;
    private ImageView questionImage;
    private ImageButton fiftyFiftyButton;
    private ImageButton hintButton;
    private ImageButton skipQuestionButton;
    private ImageButton quitButton;
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
    private boolean answerSelected = false;
    
    // Enhanced question management
    private List<Question> questions;
    private List<EnhancedQuestionModel> enhancedQuestions;
    private List<QuestionModel> firestoreQuestions;
    
    // Enhanced game systems using RegionGameEnhancer
    private RegionGameEnhancer gameEnhancer;
    private RegionGameEnhancer.GameConstants gameConstants;
    
    // Banat-specific managers
    private DifficultyManager difficultyManager;
    private GameModeManager gameModeManager;
    private PlayerProgressTracker progressTracker;
    private AchievementManager achievementManager;
    private SyncManager syncManager;
    
    // Dynamic game constants (now managed by RegionGameEnhancer)
    private int POINTS_PER_CORRECT_ANSWER = 10;
    private int BONUS_POINTS = 50;
    private int TIME_PER_QUESTION = 30000;
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

    // Add member variables to track active dialogs
    private AlertDialog dataSourceDialog = null;
    private AlertDialog noQuestionsDialog = null;
    private AlertDialog answerDialog = null;
    private AlertDialog internetDialog = null;
    private AlertDialog databaseDialog = null;
    private List<AlertDialog> activeDialogs = new ArrayList<>();

    // --- HYBRID SYSTEM FIELDS ---
    private static final String DATA_SOURCE_PREF_KEY = "data_source_preference";
    private static final String CACHE_KEY = "questions_cache_" + REGION + "_" + GAME_TYPE;
    private static final String CACHE_TIMESTAMP_KEY = CACHE_KEY + "_timestamp";
    private static final long CACHE_EXPIRY_MS = 24 * 60 * 60 * 1000; // 24h
    private String dataSourcePreference = "ask_every_time";

    private static class Question {
        String question;
        String[] answers;
        int correctAnswerIndex;
        int imageResourceId;
        String fact;

        Question(String question, String[] answers, int correctAnswerIndex, int imageResourceId, String fact) {
            this.question = question;
            this.answers = answers;
            this.correctAnswerIndex = correctAnswerIndex;
            this.imageResourceId = imageResourceId;
            this.fact = fact;
        }
    }

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
        Log.d(TAG, "🔍 Începe checkUserPreferenceAndLoad() pentru Banat");
        Log.d(TAG, "⏰ Timestamp: " + System.currentTimeMillis());
        
        if (syncManager == null) {
            Log.e(TAG, "❌ SyncManager este null în checkUserPreferenceAndLoad()");
            syncManager = SyncManager.getInstance(this);
            if (syncManager == null) {
                Log.d(TAG, "🔄 Încercăm fallback la întrebări hardcodate după încercare de reinițializare...");
                initializeHardcodedQuestions();
                displayQuestion();
                updateScore();
                startTimer();
                return;
            }
        }
        
        SharedPreferences prefs = getSharedPreferences("BanatGamePrefs", MODE_PRIVATE);
        String savedPreference = prefs.getString("data_source_preference", "ask_every_time");
        
        Log.d(TAG, "🔧 Verificare internet în checkUserPreferenceAndLoad()...");
        // Verificăm conexiunea la internet direct, fără a ne baza doar pe SyncManager
        boolean hasInternet = isInternetAvailableDirectCheck();
        Log.d(TAG, "🌐 Rezultat verificare internet direct: " + hasInternet);
        
        boolean hasLocalCache = checkIfLocalCacheExists();
        
        Log.d(TAG, "🔍 Verificăm preferința utilizatorului pentru Banat:");
        Log.d(TAG, "   📱 Preferință: " + savedPreference);
        Log.d(TAG, "   🌐 Internet: " + hasInternet);
        Log.d(TAG, "   📦 Cache local: " + hasLocalCache);
        Log.d(TAG, "   🎮 Game mode: " + (gameModeManager != null ? gameModeManager.getCurrentGameMode().displayName : "null"));
        Log.d(TAG, "   🎯 Difficulty: " + (difficultyManager != null ? difficultyManager.getCurrentDifficulty().name() : "null"));
        
        // Verificăm preferința salvată și acționăm în consecință
        switch (savedPreference) {
            case "always_database":
                if (hasInternet) {
                    Log.d(TAG, "🌐 Preferință: Baza de Date - Internet disponibil - încărcăm din baza de date");
                    loadQuestionsFromDatabase();
                } else {
                    Log.d(TAG, "🌐 Preferință: Baza de Date - Internet indisponibil - afișăm dialog de eroare");
                    showNoInternetForPreferredDatabaseDialog();
                }
                break;
                
            case "always_local":
                if (hasLocalCache) {
                    Log.d(TAG, "📱 Preferință: Cache Local - Cache disponibil - încărcăm din cache");
                    loadQuestionsFromLocalCache();
                } else {
                    Log.d(TAG, "📱 Preferință: Cache Local - Cache indisponibil - afișăm dialog de eroare");
                    showNoCacheForPreferredLocalDialog();
                }
                break;
                
            case "always_hardcoded":
                Log.d(TAG, "📚 Preferință: Întrebări Hardcodate - încărcăm întrebările hardcodate");
                initializeHardcodedQuestions();
                displayQuestion();
                updateScore();
                startTimer();
                Toast.makeText(this, "📱 Utilizez întrebări locale hardcodate", Toast.LENGTH_SHORT).show();
                break;
                
            case "ask_every_time":
            default:
                Log.d(TAG, "🎯 Afișăm întotdeauna dialogul de selecție pentru a permite utilizatorului să aleagă");
                showDataSourceSelectionDialogWithPreferences();
                break;
        }
        
        Log.d(TAG, "✅ checkUserPreferenceAndLoad() completat");
    }
    
    /**
     * Verifică direct conexiunea la internet fără a se baza doar pe SyncManager
     */
    private boolean isInternetAvailableDirectCheck() {
        ConnectivityManager connectivityManager = 
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager == null) {
            Log.e(TAG, "❌ ConnectivityManager este null");
            return false;
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) {
                Log.d(TAG, "❌ Rețea activă nu există");
                return false;
            }
            
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            boolean hasInternet = capabilities != null && 
                   (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
            
            Log.d(TAG, "🌐 Verificare directă internet (API 23+): " + hasInternet);
            Log.d(TAG, "   - WIFI: " + (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)));
            Log.d(TAG, "   - CELLULAR: " + (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)));
            Log.d(TAG, "   - ETHERNET: " + (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)));
            
            return hasInternet;
        } else {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            boolean hasInternet = activeNetworkInfo != null && activeNetworkInfo.isConnected();
            
            Log.d(TAG, "🌐 Verificare directă internet (API <23): " + hasInternet);
            return hasInternet;
        }
    }
    
    /**
     * 📱 Afișează dialog când nu există internet pentru preferința de baza de date
     */
    private void showNoInternetForPreferredDatabaseDialog() {
        Log.d(TAG, "❌ Afișăm dialog: Nu există internet pentru preferința de baza de date");
        
        String[] options = {
            "📱 Cache Local",
            "🔍 Test Internet",
            "🗄️ Test Baza de Date",
            "🚀 Forțează Încărcare"
        };
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("🌐 Nu există conexiune la internet")
            .setMessage("Preferința ta este să încarci din baza de date, dar nu există conexiune la internet.\n\n" +
                       "Dacă ești sigur că ai WiFi activ dar aplicația nu îl detectează, poți forța încărcarea din baza de date.")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: // Cache Local
                        Log.d(TAG, "📱 Utilizatorul a ales: Cache Local din dialogul de eroare");
                        if (checkIfLocalCacheExists()) {
                            loadQuestionsFromLocalCache();
                        } else {
                            showOfflineNoQuestionsError();
                        }
                        break;
                    case 1: // Test Internet
                        Log.d(TAG, "🔍 Utilizatorul a ales: Test Internet din dialogul de eroare");
                        testInternetConnection();
                        break;
                    case 2: // Test Baza de Date
                        Log.d(TAG, "🗄️ Utilizatorul a ales: Test Baza de Date din dialogul de eroare");
                        testDatabaseConnection();
                        break;
                    case 3: // Forțează Încărcare
                        Log.d(TAG, "🚀 Utilizatorul a ales: Forțează Încărcare din dialogul de eroare");
                        forceLoadFromFirebase();
                        break;
                }
            })
            .setNegativeButton("❌ Anulează", null)
            .show();
    }
    
    /**
     * 📱 Afișează dialog când nu există cache pentru preferința locală
     */
    private void showNoCacheForPreferredLocalDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("📱 Cache indisponibil")
            .setMessage("Preferința ta este să folosești cache local, dar nu există întrebări salvate pe dispozitiv.\n\n" +
                       "Ce vrei să faci?\n\n" +
                       "🌐 **Baza de Date**: Încearcă să încarci din internet (dacă există conexiune)\n" +
                       "⚙️ **Schimbă Preferința**: Alege o altă sursă de date pentru viitor")
            .setPositiveButton("🌐 Baza de Date", (dialog, which) -> {
                if (syncManager.isInternetAvailable()) {
                    loadQuestionsFromDatabase();
                } else {
                    showOfflineNoQuestionsError();
                }
            })
            .setNegativeButton("⚙️ Schimbă Preferința", (dialog, which) -> {
                showDataSourceSelectionDialogWithPreferences();
            })
            .show();
    }
    
    /**
     * 📋 Afișează dialog pentru selectarea sursei de date
     */
    private void showDataSourceSelectionDialog() {
        showDataSourceSelectionDialogWithPreferences();
    }
    
    /**
     * 📋 Afișează dialog pentru selectarea sursei de date cu opțiuni de preferință
     */
    private void showDataSourceSelectionDialogWithPreferences() {
        Log.d(TAG, "📋 Afișăm dialog de selecție sursă date cu preferințe");
        
        String[] options = {
            "🌐 Baza de Date (Firebase)",
            "📱 Cache Local",
            "📚 Întrebări Hardcodate",
            "🚀 Forțează Încărcare (Ignoră verificarea conexiunii)"
        };
        
        if (dataSourceDialog != null && dataSourceDialog.isShowing()) {
            dataSourceDialog.dismiss();
        }
        
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
            .setTitle("🎯 Sursa de Date pentru Întrebări")
            .setMessage("Alege de unde să încarci întrebările pentru quiz-ul Banat:\n\n" +
                       "🌐 **Baza de Date (Firebase)**:\n" +
                       "• Cele mai recente și actualizate întrebări\n" +
                       "• Necesită conexiune la internet\n" +
                       "• Conțin întrebări noi și îmbunătățite\n\n" +
                       "📱 **Cache Local**:\n" +
                       "• Întrebări salvate local pe dispozitiv\n" +
                       "• Funcționează fără internet\n" +
                       "• Încărcare rapidă\n\n" +
                       "📚 **Întrebări Hardcodate**:\n" +
                       "• Întrebări incluse în aplicație\n" +
                       "• Funcționează întotdeauna, chiar și fără internet\n" +
                       "• Număr limitat de întrebări\n\n" +
                       "🚀 **Forțează Încărcare**:\n" +
                       "• Ignoră verificarea conexiunii\n" +
                       "• Folosește dacă WiFi-ul nu este detectat corect")
            .setItems(options, (dialog, which) -> {
                Log.d(TAG, "👆 Utilizatorul a selectat opțiunea: " + which);
                String preference = "";
                switch (which) {
                    case 0: // Database
                        Log.d(TAG, "🌐 Utilizatorul a ales: Baza de Date");
                        Log.d(TAG, "🔍 Verificare SyncManager: " + (syncManager != null ? "✅ Disponibil" : "❌ Null"));
                        
                        preference = "always_database";
                        
                        Log.d(TAG, "🔧 Verificare internet în dialog pentru baza de date...");
                        // Folosim verificarea directă a internetului în loc de syncManager
                        boolean hasInternet = isInternetAvailableDirectCheck();
                        Log.d(TAG, "🌐 Rezultat verificare directă internet în dialog: " + hasInternet);
                        
                        if (hasInternet) {
                            Log.d(TAG, "✅ Internet disponibil - salvăm preferința și încărcăm din baza de date");
                            Log.d(TAG, "💾 Salvăm preferința: " + preference);
                            saveUserPreference(preference);
                            Log.d(TAG, "🚀 Începe încărcarea din baza de date...");
                            loadQuestionsFromDatabase();
                        } else {
                            Log.d(TAG, "❌ Nu există internet în dialog - afișăm dialog de eroare");
                            Log.d(TAG, "⚠️ Verificarea directă a internetului returnează false");
                            showNoInternetForPreferredDatabaseDialog();
                        }
                        break;
                    case 1: // Local cache
                        Log.d(TAG, "📱 Utilizatorul a ales: Cache Local");
                        preference = "always_local";
                        boolean hasCache = checkIfLocalCacheExists();
                        Log.d(TAG, "📦 Verificare cache local: " + hasCache);
                        if (hasCache) {
                            Log.d(TAG, "✅ Cache local disponibil - salvăm preferința și încărcăm din cache");
                            saveUserPreference(preference);
                            loadQuestionsFromLocalCache();
                        } else {
                            Log.d(TAG, "❌ Nu există cache local - afișăm dialog de eroare");
                            showNoCacheForPreferredLocalDialog();
                        }
                        break;
                    case 2: // Hardcoded questions
                        Log.d(TAG, "📚 Utilizatorul a ales: Întrebări Hardcodate");
                        preference = "always_hardcoded";
                        saveUserPreference(preference);
                        initializeHardcodedQuestions();
                        displayQuestion();
                        updateScore();
                        startTimer();
                        Toast.makeText(this, "📱 Utilizez întrebări locale hardcodate", Toast.LENGTH_SHORT).show();
                        break;
                    case 3: // Force Load
                        Log.d(TAG, "🚀 Utilizatorul a ales: Forțează Încărcare");
                        Toast.makeText(this, "🚀 Se forțează încărcarea din baza de date...", Toast.LENGTH_SHORT).show();
                        forceLoadFromFirebase();
                        break;
                }
            })
            .setNeutralButton("⚙️ Resetează Preferințele", (dialog, which) -> {
                Log.d(TAG, "⚙️ Utilizatorul a ales să reseteze preferințele");
                resetUserPreferences();
                Toast.makeText(this, "✅ Preferințele au fost resetate", Toast.LENGTH_SHORT).show();
                // Afișăm din nou dialogul pentru a permite o nouă alegere
                showDataSourceSelectionDialog();
            })
            .setNegativeButton("❌ Anulează", (dialog, which) -> {
                Log.d(TAG, "❌ Utilizatorul a anulat - încercăm fallback la cache local");
                // Fallback la cache local dacă există
                if (checkIfLocalCacheExists()) {
                    loadQuestionsFromLocalCache();
                } else {
                    // Fallback la întrebări hardcodate
                    initializeHardcodedQuestions();
                    displayQuestion();
                    updateScore();
                    startTimer();
                    Toast.makeText(this, "📱 Utilizez întrebări locale hardcodate", Toast.LENGTH_SHORT).show();
                }
            });
        
        dataSourceDialog = builder.create();
        activeDialogs.add(dataSourceDialog);
        dataSourceDialog.show();
    }
    
    /**
     * ✅ Verifică dacă există cache local
     */
    private boolean checkIfLocalCacheExists() {
        String cacheKey = "questions_cache_" + REGION + "_" + GAME_TYPE;
        String cachedJson = getSharedPreferences("HybridStorage", MODE_PRIVATE).getString(cacheKey, null);
        return cachedJson != null && !cachedJson.isEmpty();
    }
    
    /**
     * 💾 Salvează preferința utilizatorului
     */
    private void saveUserPreference(String preference) {
        getSharedPreferences("BanatGamePrefs", MODE_PRIVATE)
            .edit()
            .putString("data_source_preference", preference)
            .apply();
    }
    
    /**
     * 🔄 Resetează preferințele utilizatorului
     */
    private void resetUserPreferences() {
        getSharedPreferences("BanatGamePrefs", MODE_PRIVATE)
            .edit()
            .clear()
            .apply();
    }
    
    /**
     * 🌐 Încarcă întrebările din baza de date (Firebase)
     */
    private void loadQuestionsFromDatabase() {
        Log.d(TAG, "🌐 loadQuestionsFromDatabase() - Începe încărcarea din baza de date");
        
        // Afișăm un indicator de încărcare
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "📊 ProgressBar setat la VISIBLE");
        
        Log.d(TAG, "🌐 FORȚAT: Încărcăm din baza de date la cererea utilizatorului");
        
        // Verificare internet cu logging detaliat
        Log.d(TAG, "🔧 Verificare internet în loadQuestionsFromDatabase()...");
        // Folosim verificarea directă a internetului în loc de syncManager
        boolean hasInternet = isInternetAvailableDirectCheck();
        Log.d(TAG, "🌐 Rezultat verificare directă internet în loadQuestionsFromDatabase: " + hasInternet);
        
        if (!hasInternet) {
            Log.d(TAG, "❌ Nu există internet în loadQuestionsFromDatabase - afișăm dialog de eroare");
            // Dacă nu există internet, afișăm eroare
            progressBar.setVisibility(View.GONE);
            Log.d(TAG, "📊 ProgressBar setat la GONE");
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
        Log.d(TAG, "   🔧 QuestionRepository: " + (questionRepository != null ? "✅ Disponibil" : "❌ Null"));
        
        if (questionRepository == null) {
            Log.e(TAG, "❌ QuestionRepository este null - nu se pot încărca întrebări");
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "❌ Eroare: Nu se poate accesa baza de date", Toast.LENGTH_LONG).show();
            return;
        }
        
        Log.d(TAG, "🚀 Apelăm questionRepository.getQuestionsAsModels()...");
        questionRepository.getQuestionsAsModels(REGION, GAME_TYPE)
            .thenAccept(loadedQuestions -> {
                Log.d(TAG, "📥 Callback thenAccept apelat cu loadedQuestions: " + (loadedQuestions != null ? loadedQuestions.size() : "null"));
                runOnUiThread(() -> {
                    Log.d(TAG, "🔄 runOnUiThread executat");
                    progressBar.setVisibility(View.GONE);
                    Log.d(TAG, "📊 ProgressBar setat la GONE");
                    
                    if (loadedQuestions != null && !loadedQuestions.isEmpty()) {
                        Log.d(TAG, "✅ Întrebări încărcate din baza de date: " + loadedQuestions.size());
                        
                        firestoreQuestions = loadedQuestions;
                        enhancedQuestions = convertToEnhancedQuestions(loadedQuestions);
                        
                        // Salvăm în cache local
                        Log.d(TAG, "💾 Salvăm întrebările în cache local...");
                        saveQuestionsToLocalCache(loadedQuestions);
                        
                        // Actualizăm timestamp-ul
                        String cacheKey = "questions_cache_" + REGION + "_" + GAME_TYPE + "_timestamp";
                        getSharedPreferences("HybridStorage", MODE_PRIVATE)
                            .edit()
                            .putLong(cacheKey, System.currentTimeMillis())
                            .apply();
                        Log.d(TAG, "⏰ Timestamp actualizat pentru cache");
                        
                        isDataLoaded = true;
                        Log.d(TAG, "✅ isDataLoaded setat la true");
                        Log.d(TAG, "📋 Apelăm displayQuestion()...");
                        displayQuestion();
                        Log.d(TAG, "📊 Apelăm updateScore()...");
                        updateScore();
                        Log.d(TAG, "⏰ Apelăm startTimer()...");
                        startTimer();
                        
                    } else {
                        Log.w(TAG, "⚠️ Nu s-au găsit întrebări în baza de date");
                        showNoDatabaseQuestionsDialog();
                    }
                });
            })
            .exceptionally(throwable -> {
                Log.e(TAG, "❌ Eroare la încărcarea din baza de date", throwable);
                runOnUiThread(() -> {
                    Log.d(TAG, "🔄 runOnUiThread pentru eroare executat");
                    progressBar.setVisibility(View.GONE);
                    Log.d(TAG, "📊 ProgressBar setat la GONE pentru eroare");
                    Log.e(TAG, "❌ Eroare la încărcarea din baza de date", throwable);
                    showDatabaseErrorDialog();
                });
                return null;
            });
        
        Log.d(TAG, "✅ loadQuestionsFromDatabase() - Apelul către Firestore inițiat");
    }
    
    /**
     * 📢 Afișează dialog când nu există întrebări în baza de date
     */
    private void showNoDatabaseQuestionsDialog() {
        Log.d(TAG, "❌ Afișăm dialog: Nu există întrebări în baza de date");
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("❌ Nu există întrebări în baza de date")
            .setMessage("Nu am găsit întrebări pentru " + REGION + " în baza de date.\n\n" +
                       "Ce dorești să faci?\n\n" +
                       "📱 **Întrebări locale**: Folosește întrebările hardcodate\n" +
                       "🔄 **Reîncearcă**: Încearcă din nou să încarci din baza de date\n" +
                       "❌ **Anulează**: Închide jocul")
            .setPositiveButton("📱 Întrebări locale", (dialog, which) -> {
                Log.d(TAG, "📱 Utilizatorul a ales: Întrebări locale din dialogul de eroare");
                initializeHardcodedQuestions();
                displayQuestion();
                updateScore();
                startTimer();
                Toast.makeText(this, "📱 Utilizez întrebări locale hardcodate", Toast.LENGTH_SHORT).show();
            })
            .setNeutralButton("🔄 Reîncearcă", (dialog, which) -> {
                Log.d(TAG, "🔄 Utilizatorul a ales: Reîncearcă din dialogul de eroare");
                loadQuestionsFromFirestore();
            })
            .setNegativeButton("❌ Anulează", (dialog, which) -> {
                Log.d(TAG, "❌ Utilizatorul a ales: Anulează din dialogul de eroare");
                finish();
            })
            .setCancelable(false)
            .show();
    }
    
    /**
     * ❌ Afișează dialog pentru erori de baza de date
     */
    private void showDatabaseErrorDialog() {
        if (databaseDialog != null && databaseDialog.isShowing()) {
            databaseDialog.dismiss();
        }
        
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
            .setTitle("❌ Eroare de Conexiune")
            .setMessage("Nu s-a putut conecta la baza de date.\n\n" +
                       "Cauze posibile:\n" +
                       "• Nu există conexiune la internet\n" +
                       "• Probleme cu serverul Firebase\n" +
                       "• Configurare incorectă\n\n" +
                       "Ce vrei să faci?\n\n" +
                       "📱 **Cache Local**: Încearcă să încarci din cache local\n" +
                       "📚 **Întrebări Locale**: Folosește întrebările incluse în aplicație\n" +
                       "🔄 **Încearcă din nou**: Verifică din nou conexiunea")
            .setPositiveButton("📱 Cache Local", (dialog, which) -> {
                if (checkIfLocalCacheExists()) {
                    loadQuestionsFromLocalCache();
                } else {
                    showOfflineNoQuestionsError();
                }
            })
            .setNeutralButton("📚 Întrebări Locale", (dialog, which) -> {
                Log.d(TAG, "📚 Utilizatorul a ales să folosească întrebările locale în showDatabaseErrorDialog()");
                initializeQuestions();
                isDataLoaded = true;
                displayQuestion();
                updateScore();
                startTimer();
            })
            .setNegativeButton("🔄 Încearcă din nou", (dialog, which) -> {
                loadQuestionsFromDatabase();
            });
        
        databaseDialog = builder.create();
        activeDialogs.add(databaseDialog);
        databaseDialog.show();
    }
    
    /**
     * 💾 Salvează întrebările în cache local
     */
    private void saveQuestionsToLocalCache(List<QuestionModel> questions) {
        Map<String, Object> cacheData = new HashMap<>();
        cacheData.put("questions", questions);
        cacheData.put("timestamp", System.currentTimeMillis());
        cacheData.put("region", REGION);
        cacheData.put("gameType", GAME_TYPE);
        
        syncManager.saveData("questions_cache", REGION + "_" + GAME_TYPE, cacheData, new SyncManager.SyncCallback() {
            @Override
            public void onSyncComplete(boolean success, String message) {
                if (success) {
                    Log.d(TAG, "�� ✅ Questions cached locally: " + questions.size() + " questions");
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
        
        if (cachedJson != null) {
            try {
                // Parsăm JSON-ul pentru a extrage întrebările
                Gson gson = new Gson();
                Type type = new TypeToken<Map<String, Object>>(){}.getType();
                Map<String, Object> cacheData = gson.fromJson(cachedJson, type);
                
                List<QuestionModel> cachedQuestions = new ArrayList<>();
                if (cacheData.containsKey("questions")) {
                    String questionsJson = gson.toJson(cacheData.get("questions"));
                    Type listType = new TypeToken<List<QuestionModel>>(){}.getType();
                    cachedQuestions = gson.fromJson(questionsJson, listType);
                }
                
                if (cachedQuestions != null && !cachedQuestions.isEmpty()) {
                    Log.d(TAG, "✅ Întrebări încărcate din cache local: " + cachedQuestions.size());
                    
                    firestoreQuestions = cachedQuestions;
                    enhancedQuestions = convertToEnhancedQuestions(cachedQuestions);
                    
                    // Convertim la formatul local Question pentru compatibilitate
                    questions = convertFirestoreToLocalQuestions(cachedQuestions);
                    totalQuestions = questions.size();
                    progressBar.setMax(totalQuestions);
                    
                    Log.d(TAG, "✅ Convertit la întrebări locale din cache: " + questions.size() + " întrebări");
                    
                    isDataLoaded = true;
                    displayQuestion();
                    updateScore();
                    startTimer();
                    
                    // Notificăm utilizatorul că folosim cache-ul
                    Toast.makeText(this, "📱 Utilizez întrebări din cache (offline)", Toast.LENGTH_SHORT).show();
                    
                } else {
                    Log.w(TAG, "⚠️ Cache local gol sau corupt - folosim întrebările hardcodate");
                    initializeHardcodedQuestions();
                    displayQuestion();
                    updateScore();
                    startTimer();
                    
                    // Notificăm utilizatorul că folosim întrebări hardcodate
                    Toast.makeText(this, "📱 Utilizez întrebări locale hardcodate", Toast.LENGTH_SHORT).show();
                }
                
            } catch (Exception e) {
                Log.e(TAG, "❌ Eroare la parsarea cache-ului local", e);
                Log.e(TAG, "Detalii eroare:", e);
                
                // Folosim întrebări hardcodate în caz de eroare
                Log.d(TAG, "🔄 Fallback la întrebări hardcodate după eroare de parsare");
                initializeHardcodedQuestions();
                displayQuestion();
                updateScore();
                startTimer();
                
                // Notificăm utilizatorul
                Toast.makeText(this, "📱 Utilizez întrebări locale hardcodate (eroare cache)", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.w(TAG, "⚠️ Nu există cache local - folosim întrebări hardcodate");
            initializeHardcodedQuestions();
            displayQuestion();
            updateScore();
            startTimer();
            
            // Notificăm utilizatorul
            Toast.makeText(this, "📱 Utilizez întrebări locale hardcodate (no cache)", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 🔄 Convertește întrebările din Firestore la formatul îmbunătățit
     */
    private List<EnhancedQuestionModel> convertToEnhancedQuestions(List<QuestionModel> questions) {
        List<EnhancedQuestionModel> enhanced = new ArrayList<>();
        
        for (QuestionModel question : questions) {
            EnhancedQuestionModel enhancedQuestion = new EnhancedQuestionModel(
                question.getQuestion(),
                question.getCorrectAnswer(),
                question.getIncorrectAnswers(),
                question.getImageResourceId(),
                question.getFact(),
                inferCategory(question.getQuestion()),
                inferDifficulty(question),
                generateTags(question)
            );
            
            enhanced.add(enhancedQuestion);
        }
        
        return enhanced;
    }
    
    /**
     * 🏷️ Inferă categoria din textul întrebării
     */
    private EnhancedQuestionModel.Category inferCategory(String questionText) {
        String text = questionText.toLowerCase();
        
        if (text.contains("oraș") || text.contains("orașul") || text.contains("cetate") || 
            text.contains("cetatea") || text.contains("palat") || text.contains("palatul")) {
            return EnhancedQuestionModel.Category.ARCHITECTURE;
        } else if (text.contains("râu") || text.contains("râul") || text.contains("munte") || 
                   text.contains("deal") || text.contains("pădure") || text.contains("lac")) {
            return EnhancedQuestionModel.Category.NATURE;
        } else if (text.contains("istorie") || text.contains("istoric") || text.contains("bătălie") || 
                   text.contains("război") || text.contains("revoluție")) {
            return EnhancedQuestionModel.Category.HISTORY;
        } else if (text.contains("tradiție") || text.contains("folclor") || text.contains("dans") || 
                   text.contains("muzică") || text.contains("costum")) {
            return EnhancedQuestionModel.Category.CULTURE;
        } else if (text.contains("mâncare") || text.contains("bucătărie") || text.contains("rețetă") || 
                   text.contains("vin") || text.contains("bere")) {
            return EnhancedQuestionModel.Category.GASTRONOMY;
        } else {
            return EnhancedQuestionModel.Category.GENERAL;
        }
    }
    
    /**
     * 🎯 Inferă dificultatea din întrebare
     */
    private EnhancedQuestionModel.Difficulty inferDifficulty(QuestionModel question) {
        String text = question.getQuestion().toLowerCase();
        int optionsCount = question.getAnswers().size();
        
        // Criterii pentru dificultate
        boolean hasComplexTerms = text.contains("siderurgic") || text.contains("navigabil") || 
                                 text.contains("balnear") || text.contains("rezervație");
        boolean hasMultipleOptions = optionsCount > 4;
        boolean hasLongQuestion = text.length() > 100;
        
        if (hasComplexTerms || hasMultipleOptions || hasLongQuestion) {
            return EnhancedQuestionModel.Difficulty.HARD;
        } else if (text.length() > 60) {
            return EnhancedQuestionModel.Difficulty.MEDIUM;
        } else {
            return EnhancedQuestionModel.Difficulty.EASY;
        }
    }
    
    /**
     * 🏷️ Generează tag-uri pentru întrebare
     */
    private String[] generateTags(QuestionModel question) {
        List<String> tags = new ArrayList<>();
        String text = question.getQuestion().toLowerCase();
        
        // Adăugăm tag-uri bazate pe conținut
        if (text.contains("timișoara")) tags.add("timișoara");
        if (text.contains("reșița")) tags.add("reșița");
        if (text.contains("băile herculane")) tags.add("băile_herculane");
        if (text.contains("banat")) tags.add("banat");
        if (text.contains("râu") || text.contains("bega")) tags.add("râuri");
        if (text.contains("munte")) tags.add("munți");
        if (text.contains("pădure")) tags.add("natură");
        
        return tags.toArray(new String[0]);
    }
    
    /**
     * ❌ Afișează dialog pentru erori de migrare
     */
    private void showMigrationError() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("❌ Eroare de Migrare")
            .setMessage("Nu s-au putut migra întrebările în baza de date.\n\n" +
                       "Jocul va continua cu întrebările locale.")
            .setPositiveButton("✅ OK", null)
            .show();
    }
    
    /**
     * 🔄 Încarcă întrebările din Firestore
     */
    private void loadQuestionsFromFirestore() {
        // Afișăm un indicator de încărcare
        progressBar.setVisibility(View.VISIBLE);
        
        Log.d(TAG, "🔄 Loading questions from Firestore - REGION: " + REGION + ", GAME_TYPE: " + GAME_TYPE);
        
        // ✅ PRIORITATE: Verificăm mai întâi conexiunea la internet
        boolean hasInternet = syncManager.isInternetAvailable();
        Log.d(TAG, "🌐 Verificare internet pentru Banat: " + hasInternet);
        
        if (!hasInternet) {
            Log.w(TAG, "❌ Nu există conexiune la internet - încărcăm din cache local");
            loadQuestionsFromLocalCache();
            return;
        }
        
        Log.d(TAG, "🌐 Internet disponibil - încărcăm DIRECT din Firebase Firestore");
        
        questionRepository.getQuestionsAsModels(REGION, GAME_TYPE)
            .thenAccept(loadedQuestions -> {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    
                    if (loadedQuestions != null && !loadedQuestions.isEmpty()) {
                        Log.d(TAG, "✅ Întrebări încărcate din Firestore: " + loadedQuestions.size());
                        
                        firestoreQuestions = loadedQuestions;
                        enhancedQuestions = convertToEnhancedQuestions(loadedQuestions);
                        
                        // Convertim la formatul local Question pentru compatibilitate
                        questions = convertFirestoreToLocalQuestions(loadedQuestions);
                        totalQuestions = questions.size();
                        progressBar.setMax(totalQuestions);
                        
                        Log.d(TAG, "✅ Convertit la întrebări locale: " + questions.size() + " întrebări");
                        
                        // Salvăm în cache local
                        saveQuestionsToLocalCache(loadedQuestions);
                        
                        isDataLoaded = true;
                        displayQuestion();
                        updateScore();
                        startTimer();
                        
                    } else {
                        Log.w(TAG, "⚠️ Nu s-au găsit întrebări în Firestore");
                        showNoDatabaseQuestionsDialog();
                    }
                });
            })
            .exceptionally(throwable -> {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "❌ Eroare la încărcarea din Firestore", throwable);
                    showDatabaseErrorDialog();
                });
                return null;
            });
    }
    
    /**
     * ❌ Afișează dialog pentru erori când nu există întrebări
     */
    private void showNoQuestionsError() {
        Log.d(TAG, "❌ Afișăm dialog: Nu există întrebări valide pentru afișare");
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("❌ Nu există întrebări valide")
            .setMessage("Nu am găsit întrebări valide pentru afișare.\n\n" +
                       "Ce dorești să faci?\n\n" +
                       "📱 **Întrebări locale**: Folosește întrebările incluse în aplicație\n" +
                       "🔄 **Reîncearcă**: Încearcă din nou să încarci întrebări\n" +
                       "❌ **Anulează**: Închide jocul")
            .setPositiveButton("📱 Întrebări locale", (dialog, which) -> {
                Log.d(TAG, "📱 Utilizatorul a ales: Întrebări locale din dialogul de eroare");
                initializeHardcodedQuestions();
                displayQuestion();
                updateScore();
                startTimer();
                Toast.makeText(this, "📱 Utilizez întrebări locale hardcodate", Toast.LENGTH_SHORT).show();
            })
            .setNeutralButton("🔄 Reîncearcă", (dialog, which) -> {
                Log.d(TAG, "🔄 Utilizatorul a ales: Reîncearcă din dialogul de eroare");
                checkUserPreferenceAndLoad();
            })
            .setNegativeButton("❌ Anulează", (dialog, which) -> {
                Log.d(TAG, "❌ Utilizatorul a ales: Anulează din dialogul de eroare");
                finish();
            })
            .setCancelable(false)
            .show();
    }
    
    /**
     * 🔍 Testează conexiunea la internet și afișează informații detaliate
     */
    private void testInternetConnection() {
        Log.d(TAG, "🔍 Testează conexiunea la internet...");
        
        // Testăm conexiunea la internet folosind ambele metode pentru comparație
        boolean hasInternetSync = syncManager.isInternetAvailable();
        boolean hasInternetDirect = isInternetAvailableDirectCheck();
        
        Log.d(TAG, "🌐 Rezultat test internet (SyncManager): " + hasInternetSync);
        Log.d(TAG, "🌐 Rezultat test internet (Direct): " + hasInternetDirect);
        
        // Testăm și cu ping real
        syncManager.testInternetConnectionWithPing(new SyncManager.SyncCallback() {
            @Override
            public void onSyncComplete(boolean success, String message) {
                runOnUiThread(() -> {
                    Log.d(TAG, "🌐 Test ping completat: " + success + " - " + message);
                    
                    new MaterialAlertDialogBuilder(BanatGameActivity.this)
                        .setTitle("🌐 Test Conexiune Internet")
                        .setMessage("Rezultate test conexiune:\n\n" +
                                   "📡 Conexiune detectată (SyncManager): " + (hasInternetSync ? "✅ Da" : "❌ Nu") + "\n" +
                                   "📡 Conexiune detectată (Direct): " + (hasInternetDirect ? "✅ Da" : "❌ Nu") + "\n" +
                                   "🌐 Ping test: " + (success ? "✅ Funcțional" : "❌ Eșuat") + "\n" +
                                   "📋 Detalii: " + message + "\n\n" +
                                   "Doriți să testați și conexiunea la baza de date?")
                        .setPositiveButton("🗄️ Test Baza de Date", (dialog, which) -> {
                            testDatabaseConnection();
                        })
                        .setNegativeButton("❌ Închide", null)
                        .show();
                });
            }
        });
    }
    
    /**
     * 🗄️ Testează conexiunea la baza de date și verifică dacă există întrebări
     */
    private void testDatabaseConnection() {
        Log.d(TAG, "🗄️ Testează conexiunea la baza de date...");
        
        // Afișăm un progress indicator
        progressBar.setVisibility(View.VISIBLE);
        
        // Testăm dacă există întrebări în baza de date
        questionRepository.hasQuestions(REGION, GAME_TYPE)
            .thenAccept(hasQuestions -> {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    
                    Log.d(TAG, "🗄️ Test baza de date completat: " + hasQuestions);
                    
                    // Folosim verificarea directă a internetului
                    boolean hasInternet = isInternetAvailableDirectCheck();
                    
                    new MaterialAlertDialogBuilder(BanatGameActivity.this)
                        .setTitle("🗄️ Test Baza de Date")
                        .setMessage("Rezultate test baza de date:\n\n" +
                                   "🌐 Conexiune: " + (hasInternet ? "✅ Disponibilă" : "❌ Indisponibilă") + "\n" +
                                   "📊 Întrebări găsite: " + (hasQuestions ? "✅ Da" : "❌ Nu") + "\n" +
                                   "📍 Regiune: " + REGION + "\n" +
                                   "🎮 Tip joc: " + GAME_TYPE + "\n\n" +
                                   "Doriți să încărcați întrebările din baza de date?")
                        .setPositiveButton("📥 Încarcă Din Baza de Date", (dialog, which) -> {
                            forceLoadFromDatabase();
                        })
                        .setNegativeButton("❌ Închide", null)
                        .show();
                });
            })
            .exceptionally(throwable -> {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "❌ Eroare la testarea bazei de date", throwable);
                    
                    new MaterialAlertDialogBuilder(BanatGameActivity.this)
                        .setTitle("❌ Eroare Test Baza de Date")
                        .setMessage("Nu s-a putut testa conexiunea la baza de date:\n\n" +
                                   "🔍 Eroare: " + throwable.getMessage() + "\n\n" +
                                   "Cauze posibile:\n" +
                                   "• Nu există conexiune la internet\n" +
                                   "• Probleme cu Firebase\n" +
                                   "• Configurare incorectă")
                        .setPositiveButton("🔄 Încearcă Din Nou", (dialog, which) -> {
                            testDatabaseConnection();
                        })
                        .setNegativeButton("❌ Închide", null)
                        .show();
                });
                return null;
            });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set theme before super.onCreate
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.Theme_MyApplication_Dark);
        } else {
            setTheme(R.style.Theme_MyApplication_Light);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banat_game);

        Log.d(TAG, "🚀 BanatGameActivity onCreate() - Începe inițializarea");
        
        // Initialize repositories and managers
        questionRepository = FirestoreQuestionRepository.getInstance();
        syncManager = SyncManager.getInstance(this);
        pointsManager = PointsManager.getInstance(this);
        
        // Initialize enhanced systems first
        initializeEnhancedSystems();
        
        // Setup game mode and initialize views
        setupGameModeAndDifficulty();
        initializeViews();
        applyButtonStyles();
        setupLifelines();
        
        // Initialize questions from Firestore or local
        initializeQuestions();
        
        // Dialog setup pentru sursă și număr întrebări
        showInitialSetupDialog();
        
        Log.d(TAG, "✅ BanatGameActivity onCreate() - Inițializare completă");
    }
    
    /**
     * Initialize enhanced game systems using RegionGameEnhancer
     */
    private void initializeEnhancedSystems() {
        try {
            Log.d(TAG, "🔧 Inițializare sisteme îmbunătățite pentru Banat");
            
            // Initialize the game enhancer
            gameEnhancer = new RegionGameEnhancer(this, "Banat");
            
            // Initialize game mode from intent
            gameEnhancer.initializeGameMode(getIntent());
            
            // Get updated game constants
            gameConstants = gameEnhancer.updateGameConstants();
            
            // Update local constants for backward compatibility
            if (gameConstants != null) {
                TIME_PER_QUESTION = (int) gameConstants.timePerQuestion;
                POINTS_PER_CORRECT_ANSWER = gameConstants.pointsPerCorrectAnswer;
            }
            
            // Initialize legacy managers for specific Banat features
            difficultyManager = new DifficultyManager(this);
            gameModeManager = new GameModeManager(this);
            progressTracker = new PlayerProgressTracker(this);
            achievementManager = AchievementManager.getInstance(this);
            pointsManager = PointsManager.getInstance(this);
            
            Log.d(TAG, "🔧 Inițializare SyncManager...");
            syncManager = SyncManager.getInstance(this);
            Log.d(TAG, "✅ SyncManager inițializat: " + (syncManager != null));
            
            questionRepository = FirestoreQuestionRepository.getInstance();
            
            // Test internet connection immediately after SyncManager initialization
            if (syncManager != null) {
                boolean hasInternet = syncManager.isInternetAvailable();
                Log.d(TAG, "🌐 Test internet după inițializare SyncManager: " + hasInternet);
            }
            
            // Set up achievement listener for notifications
            achievementManager.setAchievementUnlockedListener(achievement -> {
                runOnUiThread(() -> {
                    Toast.makeText(this, "🏆 Achievement Unlocked: " + achievement.getTitle(), 
                                 Toast.LENGTH_LONG).show();
                    // Could add more sophisticated notification here
                });
            });
            
            // Update daily play streak
            achievementManager.updateBanatDailyPlayStreak();
            
            Log.d(TAG, "✅ Enhanced systems initialized successfully for Banat");
            Log.d(TAG, "🎮 Game constants - Time: " + TIME_PER_QUESTION + "ms, Points: " + POINTS_PER_CORRECT_ANSWER);
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error initializing enhanced systems for Banat", e);
            // Fallback to basic values
            TIME_PER_QUESTION = 30000;
            POINTS_PER_CORRECT_ANSWER = 10;
            difficultyManager = new DifficultyManager(this);
            gameModeManager = new GameModeManager(this);
        }
    }
    
    /**
     * ✅ ÎMBUNĂTĂȚIRE: Configurează modul de joc și dificultatea
     */
    private void setupGameModeAndDifficulty() {
        // Inițializăm modul de joc din intent sau folosim default
        String modeName = getIntent().getStringExtra("GAME_MODE");
        if (modeName != null) {
            try {
                GameModeManager.GameMode selectedMode = GameModeManager.GameMode.valueOf(modeName);
                gameModeManager.initializeGameMode(selectedMode, null);
            } catch (Exception e) {
                Log.w(TAG, "Invalid game mode in intent: " + modeName, e);
                gameModeManager.initializeGameMode(GameModeManager.GameMode.CLASSIC, null);
            }
        } else {
            gameModeManager.initializeGameMode(GameModeManager.GameMode.CLASSIC, null);
        }
        
        // Citim preferința pentru sursa de date din intent
        String dataSourcePreference = getIntent().getStringExtra("data_source_preference");
        if (dataSourcePreference != null && !dataSourcePreference.isEmpty()) {
            // Salvăm preferința în SharedPreferences pentru a fi folosită în viitor
            SharedPreferences prefs = getSharedPreferences("BanatGamePrefs", MODE_PRIVATE);
            prefs.edit().putString("data_source_preference", dataSourcePreference).apply();
            
            Log.d(TAG, "📱 Preferință sursă date primită din intent: " + dataSourcePreference);
        }
        
        Log.d(TAG, "Game mode initialized: " + gameModeManager.getCurrentGameMode().displayName);
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
            
            // Disable default button behavior to prevent display issues
            button.setClickable(false);
            button.setFocusable(false);
        }
        
        // Inițializare buton terminare
        finishButton.setOnClickListener(v -> finishGame());
        
        // Setup click pentru carduri
        for (int i = 0; i < answerCards.length; i++) {
            final int index = i;
            answerCards[i].setOnClickListener(v -> {
                if (v.isClickable()) {
                    checkAnswer(index, answerButtons[index].getText().toString());
                }
            });
        }
    }
    
    private void applyButtonStyles() {
        // Stilizăm butoanele pentru tema Banat
        for (int i = 0; i < answerButtons.length; i++) {
            MaterialButton button = answerButtons[i];
            MaterialCardView card = answerCards[i];
            
            // Activăm efectul de ripple pentru card
            card.setClickable(true);
            card.setFocusable(true);
            
            // Adaugă animație la apăsare
            card.setRippleColor(ContextCompat.getColorStateList(this, R.color.banat_primary_light));
        }
        
        // Adaugă efecte vizuale pentru butonul de finalizare
        finishButton.setRippleColor(ContextCompat.getColorStateList(this, R.color.banat_accent));
    }

    private void setupLifelines() {
        // 50:50 lifeline
        fiftyFiftyButton.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
            useFiftyFifty();
        });
        
        // Hint lifeline
        if (hintButton != null) {
            hintButton.setOnClickListener(v -> {
                v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
                useHint();
            });
        }
        
        // Skip question lifeline
        skipQuestionButton.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
            skipQuestion();
        });
        
        // Quit button
        if (quitButton != null) {
            quitButton.setOnClickListener(v -> showQuitConfirmation());
        }
        
        // Adăugăm un long press pe quit button pentru a testa conexiunea
        if (quitButton != null) {
            quitButton.setOnLongClickListener(v -> {
                Log.d(TAG, "🔍 Long press pe quit button - test conexiune internet");
                testInternetConnection();
                return true;
            });
        }
    }

    private void startTimer() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new CountDownTimer(TIME_PER_QUESTION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerTextView.setText(String.valueOf(millisUntilFinished / 1000));
                
                // Adăugăm efect vizual când timpul este sub 10 secunde
                if (millisUntilFinished <= 10000) {
                    Animation pulse = AnimationUtils.loadAnimation(BanatGameActivity.this, R.anim.pulse);
                    timerTextView.startAnimation(pulse);
                    timerTextView.setTextColor(ContextCompat.getColor(BanatGameActivity.this, R.color.banat_accent));
                } else {
                    timerTextView.setTextColor(ContextCompat.getColor(BanatGameActivity.this, R.color.banat_text));
                }
            }

            @Override
            public void onFinish() {
                handleTimeout();
            }
        }.start();
    }

    private void handleTimeout() {
        Toast.makeText(this, "Timpul a expirat!", Toast.LENGTH_SHORT).show();
        streak = 0;
        updateStreak();
        moveToNextQuestion();
    }

    private void useFiftyFifty() {
        if (isFiftyFiftyUsed) {
            Toast.makeText(this, "Ai folosit deja acest ajutor!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Haptic feedback for lifeline usage
        if (gameEnhancer != null) {
            gameEnhancer.performHapticFeedback(HapticFeedbackType.LIFELINE);
        }

        Question currentQuestion = questions.get(currentQuestionIndex);
        List<Integer> wrongAnswers = new ArrayList<>();
        for (int i = 0; i < currentQuestion.answers.length; i++) {
            if (i != currentQuestion.correctAnswerIndex) {
                wrongAnswers.add(i);
            }
        }
        Collections.shuffle(wrongAnswers);
        
        // Dezactivăm două răspunsuri greșite cu feedback vizual
        for (int i = 0; i < 2; i++) {
            int index = wrongAnswers.get(i);
            answerButtons[index].setEnabled(false);
            answerCards[index].setAlpha(0.5f);
            answerCards[index].setClickable(false);
        }

        isFiftyFiftyUsed = true;
        fiftyFiftyButton.setEnabled(false);
        fiftyFiftyButton.setAlpha(0.5f);
        
        Log.d(TAG, "50:50 lifeline used for question " + currentQuestionIndex);
    }
    
    /**
     * Use hint lifeline - provides intelligent hints based on question category
     */
    private void useHint() {
        if (isHintUsed) {
            Toast.makeText(this, "Ai folosit deja indiciul!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Mark hint as used
        isHintUsed = true;
        if (hintButton != null) {
            hintButton.setEnabled(false);
            hintButton.setAlpha(0.5f);
        }
        
        // Get current enhanced question for category-based hint
        if (enhancedQuestions != null && currentQuestionIndex < enhancedQuestions.size() && gameEnhancer != null) {
            EnhancedQuestionModel enhancedQuestion = enhancedQuestions.get(currentQuestionIndex);
            gameEnhancer.showHint(enhancedQuestion.getCategory());
            gameEnhancer.performHapticFeedback(HapticFeedbackType.LIFELINE);
        } else {
            // Fallback hint for Banat
            new MaterialAlertDialogBuilder(this)
                    .setTitle("💡 Indiciu")
                    .setMessage("Indiciu: Gândește-te la istoria, geografia și tradițiile specifice Banatului - regiunea de vest a României cu influențe multiculturale.")
                    .setPositiveButton("Am înțeles", null)
                    .show();
        }
        
        Log.d(TAG, "Hint used for question " + currentQuestionIndex);
    }
    
    /**
     * Show quit confirmation dialog with current statistics
     */
    private void showQuitConfirmation() {
        if (gameEnhancer != null) {
            gameEnhancer.showQuitConfirmation(currentQuestionIndex, questions.size(), score, correctAnswers, this::finish);
        } else {
            // Fallback confirmation
            new MaterialAlertDialogBuilder(this)
                    .setTitle("🚪 Ieșire din Quiz")
                    .setMessage("Ești sigur că vrei să ieși? Progresul va fi pierdut!")
                    .setPositiveButton("Da, ieși", (dialog, which) -> finish())
                    .setNegativeButton("Continuă", null)
                    .show();
        }
    }

    private void skipQuestion() {
        if (isSkipUsed) {
            Toast.makeText(this, "Ai folosit deja acest ajutor!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Haptic feedback for lifeline usage
        if (gameEnhancer != null) {
            gameEnhancer.performHapticFeedback(HapticFeedbackType.LIFELINE);
        }

        isSkipUsed = true;
        skipQuestionButton.setEnabled(false);
        skipQuestionButton.setAlpha(0.5f);
        
        // Opțional, putem da un mic bonus pentru săritura
        score += 5;
        updateScore();
        
        Toast.makeText(this, "Întrebarea a fost sărita!", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Skip lifeline used for question " + currentQuestionIndex);
        
        moveToNextQuestion();
    }
    
    private void initializeQuestions() {
        Log.d(TAG, "📋 Începem inițializarea întrebărilor");
        
        // Initialize questions from Firestore or local cache based on user preference
        questionRepository = FirestoreQuestionRepository.getInstance();
        
        if (isDataLoaded) {
            Log.d(TAG, "✅ Întrebările sunt deja încărcate, trecem la displayQuestion()");
            displayQuestion();
        } else {
            Log.d(TAG, "🔄 Verificăm preferința utilizatorului și încărcăm întrebările");
            checkUserPreferenceAndLoad();
        }
    }
    
    /**
     * Convert regular questions to enhanced questions for advanced tracking
     */
    private void convertToEnhancedQuestions() {
        if (questions != null && !questions.isEmpty()) {
            // Convert traditional questions to enhanced format
            enhancedQuestions = new ArrayList<>();
            for (Question q : questions) {
                enhancedQuestions.add(
                    RegionGameEnhancer.convertToEnhanced(
                        q.question,
                        Arrays.asList(q.answers),
                        q.correctAnswerIndex,
                        q.fact,
                        REGION
                    )
                );
            }
            
            // Filter questions based on game mode if needed
            if (gameModeManager != null) {
                enhancedQuestions = gameModeManager.filterQuestionsForGameMode(enhancedQuestions);
            }
        } else if (firestoreQuestions != null && !firestoreQuestions.isEmpty()) {
            // Convert Firestore questions to enhanced format
            enhancedQuestions = convertToEnhancedQuestions(firestoreQuestions);
            
            // Filter questions based on game mode
            if (gameModeManager != null) {
                enhancedQuestions = gameModeManager.filterQuestionsForGameMode(enhancedQuestions);
            }
        }
        
        // Update total questions count
        if (enhancedQuestions != null) {
            totalQuestions = enhancedQuestions.size();
            if (progressBar != null) {
                progressBar.setMax(totalQuestions);
            }
        }
    }
    
    /**
     * Convertește întrebările din Firestore la formatul local Question
     */
    private List<Question> convertFirestoreToLocalQuestions(List<QuestionModel> firestoreQuestions) {
        List<Question> localQuestions = new ArrayList<>();
        
        for (QuestionModel firestoreQuestion : firestoreQuestions) {
            // Convertim răspunsurile la array
            String[] answers = new String[firestoreQuestion.getAnswers().size()];
            for (int i = 0; i < firestoreQuestion.getAnswers().size(); i++) {
                answers[i] = firestoreQuestion.getAnswers().get(i);
            }
            
            // Găsim indexul răspunsului corect
            int correctAnswerIndex = -1;
            for (int i = 0; i < answers.length; i++) {
                if (answers[i].equals(firestoreQuestion.getCorrectAnswer())) {
                    correctAnswerIndex = i;
                    break;
                }
            }
            
            // Creăm întrebarea locală
            Question localQuestion = new Question(
                firestoreQuestion.getQuestion(),
                answers,
                correctAnswerIndex,
                firestoreQuestion.getImageResourceId(),
                firestoreQuestion.getFact()
            );
            
            localQuestions.add(localQuestion);
        }
        
        // Amestecăm întrebările pentru a fi prezentate în ordine aleatorie
        Collections.shuffle(localQuestions);
        
        return localQuestions;
    }
    
    private void displayQuestion() {
        Log.d(TAG, "📋 displayQuestion() - Începe afișarea întrebării");
        Log.d(TAG, "📊 Status întrebări:");
        Log.d(TAG, "   - Questions list: " + (questions != null ? "✅ Not null" : "❌ Null"));
        Log.d(TAG, "   - Enhanced questions: " + (enhancedQuestions != null ? "✅ Not null" : "❌ Null"));
        Log.d(TAG, "   - Questions size: " + (questions != null ? questions.size() : "N/A"));
        Log.d(TAG, "   - Current index: " + currentQuestionIndex);
        Log.d(TAG, "   - Total questions: " + totalQuestions);
        Log.d(TAG, "   - isDataLoaded: " + isDataLoaded);
        
        // Reset state for new question
        answerSelected = false;
        resetCardStyles();
        
        // Set question start time for tracking
        questionStartTime = System.currentTimeMillis();
        
        // Update progress
        progressBar.setProgress(currentQuestionIndex + 1);
        
        // Handle different question sources
        if (enhancedQuestions != null && !enhancedQuestions.isEmpty() && currentQuestionIndex < enhancedQuestions.size()) {
            // Using enhanced questions from Firestore
            EnhancedQuestionModel currentQuestion = enhancedQuestions.get(currentQuestionIndex);
            questionTextView.setText(currentQuestion.getQuestion());
            scoreTextView.setText(String.format("Scor: %d", score));
            
            List<String> options = currentQuestion.getAnswers();
            
            // Set options with null check
            if (options != null && options.size() >= 4) {
                for (int i = 0; i < Math.min(options.size(), 4); i++) {
                    answerButtons[i].setText(options.get(i));
                }
            } else {
                Log.e(TAG, "Options list is null or has less than 4 items");
            }
            
            // Set image if available
            if (currentQuestion.getImageResourceId() > 0) {
                try {
                    questionImage.setImageResource(currentQuestion.getImageResourceId());
                    questionImage.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    Log.e(TAG, "Error loading image", e);
                    questionImage.setVisibility(View.GONE);
                }
            } else {
                questionImage.setVisibility(View.GONE);
            }
        } else if (questions != null && !questions.isEmpty() && currentQuestionIndex < questions.size()) {
            // Using traditional local questions
            Question currentQuestion = questions.get(currentQuestionIndex);
            questionTextView.setText(currentQuestion.question);
            scoreTextView.setText(String.format("Scor: %d", score));
            
            // Set options
            for (int i = 0; i < Math.min(currentQuestion.answers.length, 4); i++) {
                answerButtons[i].setText(currentQuestion.answers[i]);
            }
            
            // Set image if available
            if (currentQuestion.imageResourceId != 0) {
                try {
                    questionImage.setImageResource(currentQuestion.imageResourceId);
                    questionImage.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    Log.e(TAG, "Error loading image", e);
                    questionImage.setVisibility(View.GONE);
                }
            } else {
                questionImage.setVisibility(View.GONE);
            }
        } else {
            Log.e(TAG, "❌ Nu există întrebări valide pentru afișare");
            showNoQuestionsError();
            return;
        }
        
        // Update streak display
        if (streak >= STREAK_BONUS_THRESHOLD) {
            streakTextView.setText(String.format("Streak: %d 🔥", streak));
            streakTextView.setVisibility(View.VISIBLE);
        } else {
            streakTextView.setVisibility(View.GONE);
        }
        
        // Show finish button if this is the last question
        if (currentQuestionIndex == getTotalQuestions() - 1) {
            showFinishButton();
        } else {
            finishButton.setVisibility(View.GONE);
        }
        
        // Start the timer
        startTimer();
    }
    
    private void resetCardStyles() {
        for (MaterialCardView card : answerCards) {
            card.setStrokeColor(ContextCompat.getColor(this, R.color.banat_border));
            card.setStrokeWidth(2);
            card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.banat_card_background));
            card.setClickable(true);
            card.setAlpha(1.0f);
        }
        
        for (MaterialButton button : answerButtons) {
            button.setEnabled(true);
        }
    }
    
    private void checkAnswer(int selectedAnswerIndex, String selectedAnswer) {
        // Prevent multiple answers
        if (answerSelected) {
            return;
        }
        answerSelected = true;
        
        if (timer != null) {
            timer.cancel();
        }

        // Calculate time taken for this question
        long questionTime = System.currentTimeMillis() - questionStartTime;
        totalTime += questionTime;
        
        // Variables to store question data
        boolean isCorrect = false;
        String fact = "";
        String correctAnswer = "";
        int correctIndex = -1;
        
        // Handle different question sources
        if (enhancedQuestions != null && !enhancedQuestions.isEmpty() && currentQuestionIndex < enhancedQuestions.size()) {
            // Using enhanced questions from Firestore
            EnhancedQuestionModel currentQuestion = enhancedQuestions.get(currentQuestionIndex);
            correctIndex = currentQuestion.getCorrectAnswerIndex();
            isCorrect = (selectedAnswerIndex == correctIndex);
            fact = currentQuestion.getFact(); // Utilizăm getFact() în loc de getExplanation()
            
            // Get correct answer text
            if (currentQuestion.getAnswers() != null && correctIndex >= 0 && 
                correctIndex < currentQuestion.getAnswers().size()) {
                correctAnswer = currentQuestion.getAnswers().get(correctIndex);
            }
            
            // Track answer with progress tracker
            if (progressTracker != null) {
                progressTracker.trackAnswer(
                    currentQuestion.getId() != null ? currentQuestion.getId() : "q_" + currentQuestionIndex,
                    isCorrect, 
                    questionTime,
                    currentQuestion.getCategory() != null ? currentQuestion.getCategory() : EnhancedQuestionModel.Category.GENERAL,
                    currentQuestion.getDifficulty() != null ? currentQuestion.getDifficulty() : EnhancedQuestionModel.Difficulty.MEDIUM
                );
            }
        } else if (questions != null && !questions.isEmpty() && currentQuestionIndex < questions.size()) {
            // Using traditional local questions
            Question currentQuestion = questions.get(currentQuestionIndex);
            correctIndex = currentQuestion.correctAnswerIndex;
            isCorrect = (selectedAnswerIndex == correctIndex);
            fact = currentQuestion.fact;
            
            // Get correct answer text
            if (currentQuestion.answers != null && correctIndex >= 0 && 
                correctIndex < currentQuestion.answers.length) {
                correctAnswer = currentQuestion.answers[correctIndex];
            }
        } else {
            Log.e(TAG, "No valid questions available in checkAnswer");
            return;
        }
        
        // Feedback and scoring
        if (isCorrect) {
            // Correct answer
            streak++;
            correctAnswers++;
            
            // Haptic feedback for correct answer
            if (gameEnhancer != null) {
                gameEnhancer.performHapticFeedback(HapticFeedbackType.CORRECT);
            }
            
            // Points calculation based on time
            int timeFactor = (int) (TIME_PER_QUESTION / 1000 - questionTime / 1000);
            int timeBonus = Math.max(0, timeFactor); // No negative time bonus
            int questionPoints = POINTS_PER_CORRECT_ANSWER + timeBonus;
            
            // Streak bonus
            if (streak >= STREAK_BONUS_THRESHOLD) {
                questionPoints += BONUS_POINTS;
                
                // Show streak bonus animation
                Animation pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse_animation);
                streakTextView.startAnimation(pulseAnimation);
            }
            
            // Update score
            score += questionPoints;
            updateScore();
            
            // Update max streak
            maxStreak = Math.max(maxStreak, streak);
            
            // Animate the selected card for feedback
            MaterialCardView selectedCard = answerCards[selectedAnswerIndex];
            selectedCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.correct_answer));
            
            // Show correct answer dialog with fact
            showAnswerDialog(fact, true);
        } else {
            // Wrong answer - reset streak
            streak = 0;
            updateStreak();
            
            // Haptic feedback for wrong answer
            if (gameEnhancer != null) {
                gameEnhancer.performHapticFeedback(HapticFeedbackType.WRONG);
            }
            
            // Animate the selected card for feedback
            MaterialCardView selectedCard = answerCards[selectedAnswerIndex];
            selectedCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.wrong_answer));
            
            // Highlight correct answer
            if (correctIndex >= 0 && correctIndex < answerCards.length) {
                answerCards[correctIndex].setCardBackgroundColor(
                    ContextCompat.getColor(this, R.color.correct_answer));
            }
            
            // Show wrong answer dialog with fact
            showAnswerDialog(fact, false);
        }
        
        // Disable all buttons
        for (MaterialButton button : answerButtons) {
            button.setEnabled(false);
        }
    }
    
    private void moveToNextQuestion() {
        currentQuestionIndex++;
        
        // Check if all questions have been answered
        if (currentQuestionIndex >= getTotalQuestions()) {
            finishGame();
        } else {
            resetCardStyles();
            displayQuestion();
        }
    }
    
    private void updateScore() {
        scoreTextView.setText("Scor: " + score);
    }
    
    private void updateStreak() {
        streakTextView.setText("Serie: " + streak);
    }
    
    private String getAchievements() {
        StringBuilder achievements = new StringBuilder();
        
        // Adăugăm diferite realizări bazate pe performanță
        if (correctAnswers == totalQuestions) {
            achievements.append("🏆 Perfect! Ai răspuns corect la toate întrebările!\n");
        } else if (correctAnswers >= totalQuestions * 0.8) {
            achievements.append("🥇 Excelent! Ai un nivel ridicat de cunoștințe despre Banat!\n");
        } else if (correctAnswers >= totalQuestions * 0.6) {
            achievements.append("🥈 Bine! Ai cunoștințe solide despre Banat!\n");
        } else if (correctAnswers >= totalQuestions * 0.4) {
            achievements.append("🥉 Acceptabil! Ai câteva cunoștințe despre Banat!\n");
        } else {
            achievements.append("Încearcă din nou! Mai ai de învățat despre Banat!\n");
        }
        
        if (maxStreak >= 5) {
            achievements.append("🔥 Serie impresionantă: " + maxStreak + " răspunsuri corecte consecutive!\n");
        }
        
        if (!isFiftyFiftyUsed || !isSkipUsed) {
            achievements.append("💪 Ai terminat jocul fără să folosești toate ajutoarele!\n");
        }
        
        achievements.append("\nScor final: ").append(score).append(" puncte");
        return achievements.toString();
    }
    
    private void finishGame() {
        Log.d(TAG, "🏁 finishGame() - Începe finalizarea jocului");
        Log.d(TAG, "📊 Statistici finale pentru finalizare:");
        Log.d(TAG, "   - Score: " + score);
        Log.d(TAG, "   - Correct answers: " + correctAnswers);
        Log.d(TAG, "   - Total questions: " + totalQuestions);
        Log.d(TAG, "   - Max streak: " + maxStreak);
        Log.d(TAG, "   - Total time: " + totalTime + "ms");
        
        if (timer != null) {
            timer.cancel();
            Log.d(TAG, "⏰ Timer oprit");
        }
        
        // --- Ascunde toate elementele de quiz pentru un final curat ---
        if (questionTextView != null) questionTextView.setVisibility(View.GONE);
        if (scoreTextView != null) scoreTextView.setVisibility(View.GONE);
        if (timerTextView != null) timerTextView.setVisibility(View.GONE);
        if (streakTextView != null) streakTextView.setVisibility(View.GONE);
        if (questionImage != null) questionImage.setVisibility(View.GONE);
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (finishButton != null) finishButton.setVisibility(View.GONE);
        if (answerButtons != null) for (MaterialButton btn : answerButtons) btn.setVisibility(View.GONE);
        if (answerCards != null) for (MaterialCardView card : answerCards) card.setVisibility(View.GONE);
        if (fiftyFiftyButton != null) fiftyFiftyButton.setVisibility(View.GONE);
        if (hintButton != null) hintButton.setVisibility(View.GONE);
        if (skipQuestionButton != null) skipQuestionButton.setVisibility(View.GONE);
        if (quitButton != null) quitButton.setVisibility(View.GONE);
        // ---
        
        // Calculate final score with difficulty multiplier
        int finalScore = difficultyManager != null ? difficultyManager.calculateFinalScore(score) : score;
        Log.d(TAG, "📈 Scor final calculat: " + finalScore + " (original: " + score + ")");
        
        // Calculate accuracy
        float accuracy = totalQuestions > 0 ? (float) correctAnswers / totalQuestions : 0;
        long averageTime = totalQuestions > 0 ? totalTime / totalQuestions : 0;
        Log.d(TAG, "📊 Acuratețe: " + (accuracy * 100) + "%");
        Log.d(TAG, "⏱️ Timp mediu per întrebare: " + averageTime + "ms");
        
        // Update progress tracker
        if (progressTracker != null) {
            progressTracker.endSession(finalScore, gameModeManager.getCurrentGameMode());
            Log.d(TAG, "📈 Progress tracker actualizat");
        } else {
            Log.w(TAG, "⚠️ ProgressTracker este null");
        }
        
        // Update difficulty based on performance
        if (difficultyManager != null) {
            difficultyManager.updateDifficultyAfterGame(correctAnswers, totalQuestions, totalTime);
            Log.d(TAG, "🎯 Difficulty manager actualizat");
        } else {
            Log.w(TAG, "⚠️ DifficultyManager este null");
        }
        
        // Update achievements
        if (achievementManager != null) {
            updateBanatAchievements(finalScore, accuracy, correctAnswers, totalQuestions);
            Log.d(TAG, "🏆 Achievement-uri actualizate");
        } else {
            Log.w(TAG, "⚠️ AchievementManager este null");
        }
        
        // Adăugăm punctele în contul utilizatorului
        if (pointsManager != null) {
            pointsManager.addPoints(this, "banat", score);
            Log.d(TAG, "✅ Puncte adăugate pentru Banat: " + score);
        } else {
            Log.w(TAG, "⚠️ PointsManager este null - nu se pot adăuga puncte");
        }
        
        // Salvăm rezultatul în sistemul hibrid (local + cloud)
        Log.d(TAG, "💾 Salvăm rezultatul în sistemul hibrid...");
        saveQuizResultToHybridStorage();
        
        // Salvăm rezultatul quiz-ului într-o structură organizată pentru user profile și leaderboard
        Log.d(TAG, "🔥 Salvăm rezultatul în Firebase...");
        saveQuizResultToFirebase();
        
        // Navigate to result activity
        Log.d(TAG, "🎯 Navigăm către BanatGameResultActivity...");
        Intent intent = new Intent(this, BanatGameResultActivity.class);
        intent.putExtra("SCORE", finalScore);
        intent.putExtra("CORRECT_ANSWERS", correctAnswers);
        intent.putExtra("TOTAL_QUESTIONS", totalQuestions);
        intent.putExtra("ACCURACY", accuracy);
        intent.putExtra("AVERAGE_TIME", averageTime);
        intent.putExtra("MAX_STREAK", maxStreak);
        
        // Adăugăm verificări pentru manager-e care ar putea fi null
        if (gameModeManager != null && gameModeManager.getCurrentGameMode() != null) {
            intent.putExtra("GAME_MODE", gameModeManager.getCurrentGameMode().name());
            Log.d(TAG, "🎮 Game mode extras: " + gameModeManager.getCurrentGameMode().name());
        } else {
            intent.putExtra("GAME_MODE", "CLASSIC");
            Log.d(TAG, "🎮 Game mode extras: CLASSIC (fallback)");
        }
        
        if (difficultyManager != null && difficultyManager.getCurrentDifficulty() != null) {
            intent.putExtra("DIFFICULTY", difficultyManager.getCurrentDifficulty().name());
            Log.d(TAG, "🎯 Difficulty extras: " + difficultyManager.getCurrentDifficulty().name());
        } else {
            intent.putExtra("DIFFICULTY", "BEGINNER");
            Log.d(TAG, "🎯 Difficulty extras: BEGINNER (fallback)");
        }
        
        Log.d(TAG, "🚀 Pornim BanatGameResultActivity cu intent extras:");
        Log.d(TAG, "   - SCORE: " + finalScore);
        Log.d(TAG, "   - CORRECT_ANSWERS: " + correctAnswers);
        Log.d(TAG, "   - TOTAL_QUESTIONS: " + totalQuestions);
        Log.d(TAG, "   - ACCURACY: " + accuracy);
        Log.d(TAG, "   - AVERAGE_TIME: " + averageTime);
        Log.d(TAG, "   - MAX_STREAK: " + maxStreak);
        
        startActivity(intent);
        Log.d(TAG, "✅ BanatGameResultActivity pornit cu succes");
        finish();
        Log.d(TAG, "✅ finishGame() completat cu succes");
    }
    
    /**
     * 🏆 Actualizează achievement-urile pentru Banat
     */
    private void updateBanatAchievements(int finalScore, float accuracy, int correctAnswers, int totalQuestions) {
        // Update quiz completion achievements
        achievementManager.incrementBanatQuizCompletions();
        
        // Update perfect score achievements
        if (accuracy >= 1.0f) {
            achievementManager.recordBanatPerfectScore();
        }
        
        // Update difficulty achievements
        DifficultyManager.DifficultyLevel currentDifficulty = difficultyManager.getCurrentDifficulty();
        if (currentDifficulty != DifficultyManager.DifficultyLevel.BEGINNER) {
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
                achievementManager.updateBanatDifficultyUnlock(difficultyName);
            }
        }
        
        // Update game mode achievements
        String gameMode = gameModeManager.getCurrentGameMode().name().toLowerCase();
        achievementManager.incrementBanatGameModeCompletion(gameMode);
        
        // Refresh all achievements to check for any new unlocks
        achievementManager.refreshAllAchievements();
    }
    
    /**
     * 💾 Salvează rezultatul quiz-ului în sistemul hibrid
     */
    private void saveQuizResultToHybridStorage() {
        QuizResult quizResult = createQuizResult();
        
        Map<String, Object> quizResultData = new HashMap<>();
        quizResultData.put("id", quizResult.getId());
        quizResultData.put("userId", quizResult.getUserId());
        quizResultData.put("region", quizResult.getRegion());
        quizResultData.put("gameType", quizResult.getGameType());
        quizResultData.put("score", quizResult.getScore());
        quizResultData.put("correctAnswers", quizResult.getCorrectAnswers());
        quizResultData.put("totalQuestions", quizResult.getTotalQuestions());
        quizResultData.put("accuracy", quizResult.getAccuracy());
        quizResultData.put("maxStreak", quizResult.getMaxStreak());
        quizResultData.put("timestamp", quizResult.getTimestamp());
        
        syncManager.saveData("quiz_results", quizResult.getId(), quizResultData, new SyncManager.SyncCallback() {
            @Override
            public void onSyncComplete(boolean success, String message) {
                if (success) {
                    Log.d(TAG, "✅ Quiz result saved to hybrid storage");
                } else {
                    Log.w(TAG, "⚠️ Failed to save quiz result to hybrid storage: " + message);
                }
            }
        });
    }
    
    /**
     * 🔥 Salvează rezultatul quiz-ului în Firebase
     */
    private void saveQuizResultToFirebase() {
        QuizResult quizResult = createQuizResult();
        
        // Save to quiz_results collection
        saveToQuizResults(quizResult);
        
        // Save to user activity history
        saveToUserActivityHistory(quizResult);
        
        // Save to leaderboard data
        saveToLeaderboardData(quizResult);
        
        // Update user profile stats
        updateUserProfileStats(quizResult);
    }
    
    /**
     * 📊 Creează un obiect QuizResult
     */
    private QuizResult createQuizResult() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : "anonymous";
        
        QuizResult quizResult = new QuizResult();
        quizResult.setId(userId + "_" + System.currentTimeMillis());
        quizResult.setUserId(userId);
        quizResult.setRegion("banat");
        quizResult.setGameType("quiz");
        quizResult.setScore(score);
        quizResult.setCorrectAnswers(correctAnswers);
        quizResult.setTotalQuestions(totalQuestions);
        quizResult.setAccuracy((float) correctAnswers / totalQuestions);
        quizResult.setTotalTime(totalTime);
        quizResult.setMaxStreak(maxStreak);
        quizResult.setGameMode(gameModeManager.getCurrentGameMode().name());
        quizResult.setDifficulty(difficultyManager.getCurrentDifficulty().name());
        quizResult.setTimestamp(new Date());
        
        return quizResult;
    }
    
    /**
     * 💾 Salvează în colecția quiz_results
     */
    private void saveToQuizResults(QuizResult quizResult) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        db.collection("quiz_results")
            .document(quizResult.getId())
            .set(quizResult)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "✅ Quiz result saved to quiz_results collection");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ Failed to save quiz result", e);
            });
    }
    
    /**
     * 📝 Salvează în istoricul activității utilizatorului
     */
    private void saveToUserActivityHistory(QuizResult quizResult) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        
        if (currentUser == null) return;
        
        Map<String, Object> activityData = new HashMap<>();
        activityData.put("type", "quiz_completion");
        activityData.put("region", "banat");
        activityData.put("score", quizResult.getScore());
        activityData.put("accuracy", quizResult.getAccuracy());
        activityData.put("timestamp", FieldValue.serverTimestamp());
        
        db.collection("users")
            .document(currentUser.getUid())
            .collection("activity_history")
            .add(activityData)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "✅ Activity saved to user history");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ Failed to save activity to user history", e);
            });
    }
    
    /**
     * 🏆 Salvează în datele pentru leaderboard
     */
    private void saveToLeaderboardData(QuizResult quizResult) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        
        if (currentUser == null) return;
        
        Map<String, Object> leaderboardData = new HashMap<>();
        leaderboardData.put("userId", currentUser.getUid());
        leaderboardData.put("userName", currentUser.getDisplayName() != null ? 
                           currentUser.getDisplayName() : "Utilizator");
        leaderboardData.put("score", quizResult.getScore());
        leaderboardData.put("accuracy", quizResult.getAccuracy());
        leaderboardData.put("region", "banat");
        leaderboardData.put("gameMode", quizResult.getGameMode());
        leaderboardData.put("difficulty", quizResult.getDifficulty());
        leaderboardData.put("timestamp", FieldValue.serverTimestamp());
        
        db.collection("leaderboard")
            .document("banat")
            .collection("scores")
            .add(leaderboardData)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "✅ Score saved to leaderboard");
                updateBestScoreForLeaderboard(quizResult);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ Failed to save score to leaderboard", e);
            });
    }
    
    /**
     * 🏆 Actualizează cel mai bun scor pentru leaderboard
     */
    private void updateBestScoreForLeaderboard(QuizResult quizResult) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        
        if (currentUser == null) return;
        
        db.collection("leaderboard")
            .document("banat")
            .collection("best_scores")
            .document(currentUser.getUid())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (!documentSnapshot.exists() || 
                    documentSnapshot.getLong("score") < quizResult.getScore()) {
                    
                    Map<String, Object> bestScoreData = new HashMap<>();
                    bestScoreData.put("userId", currentUser.getUid());
                    bestScoreData.put("userName", currentUser.getDisplayName() != null ? 
                                    currentUser.getDisplayName() : "Utilizator");
                    bestScoreData.put("score", quizResult.getScore());
                    bestScoreData.put("accuracy", quizResult.getAccuracy());
                    bestScoreData.put("timestamp", FieldValue.serverTimestamp());
                    
                    db.collection("leaderboard")
                        .document("banat")
                        .collection("best_scores")
                        .document(currentUser.getUid())
                        .set(bestScoreData)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "✅ Best score updated for leaderboard");
                            updateGlobalLeaderboard(bestScoreData);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "❌ Failed to update best score", e);
                        });
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ Failed to check best score", e);
            });
    }
    
    /**
     * 🌍 Actualizează leaderboard-ul global
     */
    private void updateGlobalLeaderboard(Map<String, Object> bestScoreData) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        db.collection("global_leaderboard")
            .document("banat")
            .collection("scores")
            .add(bestScoreData)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "✅ Score added to global leaderboard");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ Failed to add score to global leaderboard", e);
            });
    }
    
    /**
     * 👤 Actualizează statisticile profilului utilizatorului
     */
    private void updateUserProfileStats(QuizResult quizResult) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        
        if (currentUser == null) return;
        
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("banat_quiz_completions", FieldValue.increment(1));
        updateData.put("banat_total_score", FieldValue.increment(quizResult.getScore()));
        updateData.put("banat_best_score", quizResult.getScore()); // Will be updated only if higher
        updateData.put("last_banat_quiz", FieldValue.serverTimestamp());
        
        db.collection("users")
            .document(currentUser.getUid())
            .update(updateData)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "✅ User profile stats updated");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ Failed to update user profile stats", e);
            });
    }
    
    /**
     * Save game result to Firestore leaderboard with enhanced statistics
     */
    private void saveGameResultToLeaderboard(int finalScore, int correctAnswers, int totalQuestions, 
                                           long averageTime, float accuracy) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        
        if (currentUser == null) {
            Log.w(TAG, "User not authenticated, skipping leaderboard save");
            return;
        }
        
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Prepare result data
        Map<String, Object> result = new HashMap<>();
        result.put("userId", currentUser.getUid());
        result.put("userName", currentUser.getDisplayName() != null ? 
                   currentUser.getDisplayName() : "Utilizator");
        result.put("score", finalScore);
        result.put("correctAnswers", correctAnswers);
        result.put("totalQuestions", totalQuestions);
        result.put("accuracy", accuracy);
        result.put("averageTimePerQuestion", averageTime);
        result.put("maxStreak", maxStreak);
        result.put("region", "banat");
        result.put("gameMode", gameEnhancer != null && gameEnhancer.getGameModeManager() != null ? 
                   gameEnhancer.getGameModeManager().getCurrentGameMode().name() : "CLASSIC");
        result.put("difficulty", gameEnhancer != null && gameEnhancer.getDifficultyManager() != null ? 
                   gameEnhancer.getDifficultyManager().getCurrentDifficulty().name() : "MEDIUM");
        result.put("timestamp", FieldValue.serverTimestamp());
        result.put("gameType", GAME_TYPE);
        
        // Save to main quiz_results collection
        db.collection("quiz_results")
                .add(result)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Game result saved to leaderboard: " + documentReference.getId());
                    updateUserProfile(currentUser.getUid(), finalScore, accuracy);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving game result to leaderboard", e);
                });
    }
    
    /**
     * Update user profile with latest game statistics
     */
    private void updateUserProfile(String userId, int score, float accuracy) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        Map<String, Object> profileUpdate = new HashMap<>();
        profileUpdate.put("lastGameScore", score);
        profileUpdate.put("lastGameAccuracy", accuracy);
        profileUpdate.put("lastGameRegion", "banat");
        profileUpdate.put("lastGameTimestamp", FieldValue.serverTimestamp());
        profileUpdate.put("gamesPlayed", FieldValue.increment(1));
        
        // Update best score if this is better
        db.collection("user_profiles").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long currentBestScore = documentSnapshot.getLong("bestScoreBanat");
                        if (currentBestScore == null || score > currentBestScore) {
                            profileUpdate.put("bestScoreBanat", score);
                        }
                    } else {
                        profileUpdate.put("bestScoreBanat", score);
                    }
                    
                    // Apply the update
                    db.collection("user_profiles").document(userId)
                            .set(profileUpdate, com.google.firebase.firestore.SetOptions.merge())
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "User profile updated successfully");
                                updateUserActivityHistory(userId, score, accuracy);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error updating user profile", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking current best score", e);
                });
    }
    
    /**
     * Update user activity history with recent game
     */
    private void updateUserActivityHistory(String userId, int score, float accuracy) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        Map<String, Object> activity = new HashMap<>();
        activity.put("type", "quiz_completed");
        activity.put("region", "banat");
        activity.put("score", score);
        activity.put("accuracy", accuracy);
        activity.put("timestamp", FieldValue.serverTimestamp());
        activity.put("correctAnswers", correctAnswers);
        activity.put("totalQuestions", totalQuestions);
        
        db.collection("user_profiles").document(userId)
                .update("recentActivities", FieldValue.arrayUnion(activity))
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User activity history updated");
                    // Limit recent activities to last 20 entries
                    limitUserActivityHistory(userId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating user activity history", e);
                });
    }
    
    /**
     * Limit user activity history to prevent unlimited growth
     */
    private void limitUserActivityHistory(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        db.collection("user_profiles").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> activities = 
                            (List<Map<String, Object>>) documentSnapshot.get("recentActivities");
                        
                        if (activities != null && activities.size() > 20) {
                            // Keep only the last 20 activities
                            List<Map<String, Object>> limitedActivities = 
                                activities.subList(Math.max(0, activities.size() - 20), activities.size());
                            
                            db.collection("user_profiles").document(userId)
                                    .update("recentActivities", limitedActivities)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "User activity history limited to 20 entries");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error limiting user activity history", e);
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking user activity history size", e);
                });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Cancel timer if running
        if (timer != null) {
            timer.cancel();
        }
        
        // Dismiss all active dialogs
        for (AlertDialog dialog : activeDialogs) {
            if (dialog != null && dialog.isShowing()) {
                try {
                    dialog.dismiss();
                } catch (Exception e) {
                    Log.e(TAG, "Error dismissing dialog", e);
                }
            }
        }
        
        // Clear the list
        activeDialogs.clear();
    }
    
    private void showFinishButton() {
        questionTextView.setText("Ai terminat toate întrebările!");
        questionImage.setVisibility(View.GONE);
        
        for (MaterialCardView card : answerCards) {
            card.setVisibility(View.GONE);
        }
        
        finishButton.setVisibility(View.VISIBLE);
        timerTextView.setVisibility(View.GONE);
        
        if (timer != null) {
            timer.cancel();
        }
    }

    /**
     * 🚀 Forțează încărcarea din baza de date fără verificarea internetului
     */
    private void forceLoadFromDatabase() {
        Log.d(TAG, "🚀 Forțăm încărcarea din baza de date fără verificarea internetului");
        
        // Afișăm un indicator de încărcare
        progressBar.setVisibility(View.VISIBLE);
        
        // Adăugăm logging mai detaliat
        Log.d(TAG, "🔍 Încercăm să accesăm Firestore cu:");
        Log.d(TAG, "   📍 REGION: " + REGION);
        Log.d(TAG, "   🎮 GAME_TYPE: " + GAME_TYPE);
        Log.d(TAG, "   📂 Calea: regions/" + REGION + "/games/" + GAME_TYPE + "/questions");
        
        questionRepository.getQuestionsAsModels(REGION, GAME_TYPE)
            .thenAccept(loadedQuestions -> {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    
                    if (loadedQuestions != null && !loadedQuestions.isEmpty()) {
                        Log.d(TAG, "✅ Întrebări încărcate din baza de date (forțat): " + loadedQuestions.size());
                        
                        firestoreQuestions = loadedQuestions;
                        enhancedQuestions = convertToEnhancedQuestions(loadedQuestions);
                        
                        // Salvăm în cache local
                        saveQuestionsToLocalCache(loadedQuestions);
                        
                        // Actualizăm timestamp-ul
                        String cacheKey = "questions_cache_" + REGION + "_" + GAME_TYPE + "_timestamp";
                        getSharedPreferences("HybridStorage", MODE_PRIVATE)
                            .edit()
                            .putLong(cacheKey, System.currentTimeMillis())
                            .apply();
                        
                        isDataLoaded = true;
                        displayQuestion();
                        updateScore();
                        startTimer();
                        
                    } else {
                        Log.w(TAG, "⚠️ Nu s-au găsit întrebări în baza de date (forțat)");
                        showNoDatabaseQuestionsDialog();
                    }
                });
            })
            .exceptionally(throwable -> {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "❌ Eroare la încărcarea din baza de date (forțat)", throwable);
                    showDatabaseErrorDialog();
                });
                return null;
            });
    }
    
    /**
     * 🚀 Forțează încărcarea din Firebase chiar dacă detectarea internetului nu funcționează
     */
    private void forceLoadFromFirebase() {
        Log.d(TAG, "🚀 Forțăm încărcarea din Firebase - ignorăm detectarea internetului");
        
        // Afișăm un indicator de încărcare
        progressBar.setVisibility(View.VISIBLE);
        
        // Afișăm un mesaj de încărcare
        Toast.makeText(this, "🚀 Se forțează încărcarea din Firebase...", Toast.LENGTH_SHORT).show();
        
        // Încercăm direct din Firebase fără să verificăm internetul
        try {
            // Verificăm dacă repository-ul este inițializat
            if (questionRepository == null) {
                questionRepository = FirestoreQuestionRepository.getInstance();
                if (questionRepository == null) {
                    Log.e(TAG, "❌ Nu s-a putut inițializa questionRepository");
                    Toast.makeText(this, "❌ Eroare la inițializarea repository-ului", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
            }
            
            questionRepository.getQuestionsAsModels(REGION, GAME_TYPE)
                .thenAccept(loadedQuestions -> {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        
                        if (loadedQuestions != null && !loadedQuestions.isEmpty()) {
                            Log.d(TAG, "✅ Întrebări încărcate din Firebase (forțat): " + loadedQuestions.size());
                            
                            firestoreQuestions = loadedQuestions;
                            enhancedQuestions = convertToEnhancedQuestions(loadedQuestions);
                            
                            isDataLoaded = true;
                            displayQuestion();
                            updateScore();
                            startTimer();
                            
                            // Salvăm în cache pentru următoarea dată
                            saveQuestionsToLocalCache(loadedQuestions);
                            
                            Toast.makeText(this, "✅ " + loadedQuestions.size() + " întrebări încărcate din Firebase!", Toast.LENGTH_SHORT).show();
                            
                        } else {
                            Log.w(TAG, "⚠️ Nu s-au găsit întrebări în Firebase - folosim locale");
                            Toast.makeText(this, "⚠️ Nu s-au găsit întrebări în Firebase. Folosim întrebările locale.", Toast.LENGTH_LONG).show();
                            initializeQuestions();
                            isDataLoaded = true;
                            displayQuestion();
                            updateScore();
                            startTimer();
                        }
                    });
                })
                .exceptionally(throwable -> {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "❌ Eroare la încărcarea din Firebase (forțat)", throwable);
                        
                        // Afișăm detalii despre eroare
                        new MaterialAlertDialogBuilder(this)
                            .setTitle("❌ Eroare la încărcarea din Firebase")
                            .setMessage("Detalii eroare: " + throwable.getMessage() + "\n\n" +
                                       "Cauze posibile:\n" +
                                       "• Nu există conexiune la internet\n" +
                                       "• Probleme cu Firebase\n" +
                                       "• Configurare incorectă\n\n" +
                                       "Doriți să folosiți întrebările locale?")
                            .setPositiveButton("✅ Da, folosește întrebări locale", (dialog, which) -> {
                                initializeQuestions();
                                isDataLoaded = true;
                                displayQuestion();
                                updateScore();
                                startTimer();
                            })
                            .setNegativeButton("🔄 Încearcă din nou", (dialog, which) -> {
                                forceLoadFromFirebase();
                            })
                            .show();
                    });
                    return null;
                });
        } catch (Exception e) {
            Log.e(TAG, "❌ Excepție la forțarea încărcării din Firebase", e);
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "❌ Eroare: " + e.getMessage(), Toast.LENGTH_LONG).show();
            
            // Fallback la întrebări locale
            initializeQuestions();
            isDataLoaded = true;
            displayQuestion();
            updateScore();
            startTimer();
        }
    }

    /**
     * Returns the total number of questions from either enhanced or traditional questions
     */
    private int getTotalQuestions() {
        if (enhancedQuestions != null && !enhancedQuestions.isEmpty()) {
            return enhancedQuestions.size();
        } else if (questions != null && !questions.isEmpty()) {
            return questions.size();
        }
        return 0;
    }

    /**
     * Show dialog with answer explanation
     */
    private void showAnswerDialog(String fact, boolean isCorrect) {
        if (answerDialog != null && answerDialog.isShowing()) {
            answerDialog.dismiss();
        }
        
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        
        if (isCorrect) {
            builder.setTitle("✅ Corect!");
            builder.setIcon(android.R.drawable.ic_menu_info_details);
        } else {
            builder.setTitle("❌ Incorect!");
            builder.setIcon(android.R.drawable.ic_dialog_alert);
        }
        
        // Show the fact/explanation
        builder.setMessage(fact);
        
        // Add continue button
        builder.setPositiveButton("Continuă", (dialog, which) -> {
            moveToNextQuestion();
        });
        
        // Create and show the dialog
        answerDialog = builder.create();
        activeDialogs.add(answerDialog);
        answerDialog.show();
        
        // Auto-continue after a delay if user doesn't tap
        new Handler().postDelayed(() -> {
            if (answerDialog != null && answerDialog.isShowing()) {
                try {
                    answerDialog.dismiss();
                    moveToNextQuestion();
                } catch (Exception e) {
                    Log.e(TAG, "Error dismissing dialog", e);
                }
            }
        }, 5000); // 5 second timeout
    }

    /**
     * 📱 Afișează dialog când nu există întrebări offline
     */
    private void showOfflineNoQuestionsError() {
        Log.d(TAG, "❌ Afișăm dialog: Nu există întrebări offline");
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("❌ Nu există întrebări offline")
            .setMessage("Nu am găsit întrebări salvate pentru " + REGION + " și nu există conexiune la internet.\n\n" +
                       "Ce dorești să faci?\n\n" +
                       "📱 **Întrebări locale**: Folosește întrebările incluse în aplicație\n" +
                       "🔄 **Verifică conexiunea**: Testează din nou conexiunea la internet\n" +
                       "❌ **Anulează**: Închide jocul")
            .setPositiveButton("📱 Întrebări locale", (dialog, which) -> {
                Log.d(TAG, "📱 Utilizatorul a ales: Întrebări locale din dialogul offline");
                initializeHardcodedQuestions();
                displayQuestion();
                updateScore();
                startTimer();
                Toast.makeText(this, "📱 Utilizez întrebări locale hardcodate", Toast.LENGTH_SHORT).show();
            })
            .setNeutralButton("🔄 Verifică conexiunea", (dialog, which) -> {
                Log.d(TAG, "🔄 Utilizatorul a ales: Verifică conexiunea din dialogul offline");
                testInternetConnection();
            })
            .setNegativeButton("❌ Anulează", (dialog, which) -> {
                Log.d(TAG, "❌ Utilizatorul a ales: Anulează din dialogul offline");
                finish();
            })
            .setCancelable(false)
            .show();
    }

    /**
     * Inițializează întrebări hardcodate pentru Banat ca fallback
     */
    private void initializeHardcodedQuestions() {
        Log.d(TAG, "📱 Inițializez întrebări hardcodate pentru Banat");
        
        List<QuestionModel> hardcodedQuestions = new ArrayList<>();
        
        // Întrebări despre Banat
        hardcodedQuestions.add(new QuestionModel(
            "Care este cel mai mare oraș din regiunea Banat?",
            "Timișoara", 
            Arrays.asList("Arad", "Reșița", "Lugoj"), 
            R.drawable.timisoara, 
            "Timișoara este cel mai mare oraș din Banat și al treilea ca mărime din România."
        ));
        
        hardcodedQuestions.add(new QuestionModel(
            "Care este cel mai înalt vârf montan din Banat?",
            "Vârful Peleaga", 
            Arrays.asList("Vârful Parâng", "Vârful Retezat", "Vârful Gugu"), 
            R.drawable.banat_cities,
            "Vârful Peleaga din Munții Retezat are 2.509 metri și este cel mai înalt din Banat."
        ));
        
        hardcodedQuestions.add(new QuestionModel(
            "Care stațiune din Banat este cunoscută pentru apele termale?",
            "Băile Herculane", 
            Arrays.asList("Buziaș", "Lipova", "Sânnicolau Mare"), 
            R.drawable.herculane, 
            "Băile Herculane este una dintre cele mai vechi stațiuni din lume, cunoscută încă din perioada romană."
        ));
        
        hardcodedQuestions.add(new QuestionModel(
            "Ce festival de muzică renumit are loc anual în Timișoara?",
            "Festivalul Plai", 
            Arrays.asList("Electric Castle", "Untold", "Neversea"), 
            R.drawable.timisoara, 
            "Festivalul Plai este un festival multicultural care promovează diversitatea culturală și muzicală."
        ));
        
        hardcodedQuestions.add(new QuestionModel(
            "În ce an a devenit Timișoara primul oraș european iluminat electric?",
            "1884", 
            Arrays.asList("1872", "1896", "1902"), 
            R.drawable.timisoara, 
            "În 1884, Timișoara a devenit primul oraș european cu iluminat electric stradal."
        ));
        
        hardcodedQuestions.add(new QuestionModel(
            "Care este cel mai vechi parc din Timișoara?",
            "Parcul Central", 
            Arrays.asList("Parcul Rozelor", "Parcul Botanic", "Parcul Copiilor"), 
            R.drawable.timisoara, 
            "Parcul Central (Parcul Anton Scudier) este cel mai vechi parc din Timișoara, amenajat în 1850."
        ));
        
        hardcodedQuestions.add(new QuestionModel(
            "Ce râu trece prin Timișoara?",
            "Bega", 
            Arrays.asList("Timiș", "Mureș", "Caraș"), 
            R.drawable.timisoara, 
            "Râul Bega traversează Timișoara și este un important canal navigabil."
        ));
        
        hardcodedQuestions.add(new QuestionModel(
            "Care este numele cetății medievale din Timișoara?",
            "Cetatea Timișoarei", 
            Arrays.asList("Cetatea Aradului", "Cetatea Severinului", "Cetatea Făgetului"), 
            R.drawable.timisoara, 
            "Cetatea Timișoarei a fost construită în secolul al XIII-lea și a jucat un rol important în istoria regiunii."
        ));
        
        hardcodedQuestions.add(new QuestionModel(
            "Care este cel mai important eveniment istoric din Timișoara din 1989?",
            "Revoluția Română", 
            Arrays.asList("Unirea Banatului cu România", "Vizita Împăratului Franz Joseph", "Marea Adunare Națională"), 
            R.drawable.timisoara, 
            "Revoluția Română din 1989 a început la Timișoara, fiind scânteia care a dus la căderea regimului comunist."
        ));
        
        hardcodedQuestions.add(new QuestionModel(
            "Ce titlu a primit Timișoara în anul 2023?",
            "Capitală Culturală Europeană", 
            Arrays.asList("Oraș Verde European", "Capitală Regională", "Centru Tehnologic European"), 
            R.drawable.timisoara, 
            "Timișoara a fost desemnată Capitală Culturală Europeană pentru anul 2023."
        ));
        
        Log.d(TAG, "✅ Întrebări hardcodate create: " + hardcodedQuestions.size());
        
        // Convertim întrebările hardcodate în formatul necesar
        firestoreQuestions = hardcodedQuestions;
        enhancedQuestions = convertToEnhancedQuestions(hardcodedQuestions);
        questions = convertFirestoreToLocalQuestions(hardcodedQuestions);
        totalQuestions = questions.size();
        
        if (progressBar != null) {
            progressBar.setMax(totalQuestions);
        }
        
        isDataLoaded = true;
    }

    // --- DIALOG INIȚIAL PENTRU SURSA ȘI NUMĂRUL DE ÎNTREBĂRI ---
    private void showInitialSetupDialog() {
        boolean hasInternet = isInternetAvailableDirectCheck();
        boolean hasLocalCache = checkIfLocalCacheExistsHybrid();
        String[] sources;
        if (hasInternet && hasLocalCache) {
            sources = new String[]{"🌐 Baza de Date", "📱 Cache Local", "🎯 Automat"};
        } else if (hasInternet) {
            sources = new String[]{"🌐 Baza de Date"};
        } else if (hasLocalCache) {
            sources = new String[]{"📱 Cache Local"};
        } else {
            sources = new String[]{"❌ Nicio sursă disponibilă"};
        }
        int[] numQuestionsOptions = {5, 10, 15, 20, 30, 50};
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_quiz_setup, null);
        android.widget.Spinner sourceSpinner = dialogView.findViewById(R.id.sourceSpinner);
        android.widget.Spinner numQuestionsSpinner = dialogView.findViewById(R.id.numQuestionsSpinner);
        android.widget.ArrayAdapter<String> sourceAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sources);
        sourceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sourceSpinner.setAdapter(sourceAdapter);
        android.widget.ArrayAdapter<Integer> numAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, toIntegerList(numQuestionsOptions));
        numAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        numQuestionsSpinner.setAdapter(numAdapter);
        new MaterialAlertDialogBuilder(this)
            .setTitle("Setări quiz")
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton("Start", (dialog, which) -> {
                int sourceIndex = sourceSpinner.getSelectedItemPosition();
                String selectedSource = sources[sourceIndex];
                int numQuestions = (Integer) numQuestionsSpinner.getSelectedItem();
                if (selectedSource.contains("Baza de Date")) {
                    saveDataSourcePreferenceHybrid("always_database");
                } else if (selectedSource.contains("Cache Local")) {
                    saveDataSourcePreferenceHybrid("always_cache");
                } else if (selectedSource.contains("Automat")) {
                    saveDataSourcePreferenceHybrid("auto");
                }
                getSharedPreferences("BanatGamePrefs", MODE_PRIVATE).edit().putInt("quiz_num_questions", numQuestions).apply();
                continueHybridLoadWithNumQuestions(numQuestions);
            })
            .setNegativeButton("Anulează", (dialog, which) -> finish())
            .show();
    }
    private java.util.List<Integer> toIntegerList(int[] arr) {
        java.util.List<Integer> list = new java.util.ArrayList<>();
        for (int v : arr) list.add(v);
        return list;
    }
    private void continueHybridLoadWithNumQuestions(int numQuestions) {
        boolean hasInternet = isInternetAvailableDirectCheck();
        boolean hasLocalCache = checkIfLocalCacheExistsHybrid();
        switch (dataSourcePreference) {
            case "always_database":
                if (hasInternet) {
                    loadQuestionsFromDatabaseHybrid(numQuestions);
                } else {
                    showNoInternetForPreferredDatabaseDialogHybrid(numQuestions);
                }
                break;
            case "always_cache":
                if (hasLocalCache) {
                    loadQuestionsFromLocalCacheHybrid(numQuestions);
                } else {
                    showNoCacheForPreferredLocalDialogHybrid(numQuestions);
                }
                break;
            case "auto":
                if (hasInternet) {
                    loadQuestionsFromDatabaseHybrid(numQuestions);
                } else if (hasLocalCache) {
                    loadQuestionsFromLocalCacheHybrid(numQuestions);
                } else {
                    showDataSourceSelectionDialogHybrid(numQuestions);
                }
                break;
            case "ask_every_time":
            default:
                showDataSourceSelectionDialogHybrid(numQuestions);
                break;
        }
    }
    // --- METODE HIBRID ---
    private boolean checkIfLocalCacheExistsHybrid() {
        String cachedJson = getSharedPreferences("HybridStorage", MODE_PRIVATE).getString(CACHE_KEY, null);
        long timestamp = getSharedPreferences("HybridStorage", MODE_PRIVATE).getLong(CACHE_TIMESTAMP_KEY, 0);
        boolean notExpired = (System.currentTimeMillis() - timestamp) < CACHE_EXPIRY_MS;
        if (cachedJson != null && !cachedJson.isEmpty() && notExpired) {
            try {
                Gson gson = new Gson();
                Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
                Map<String, Object> cacheData = gson.fromJson(cachedJson, mapType);
                if (cacheData != null && cacheData.containsKey("questions")) {
                    String questionsJson = gson.toJson(cacheData.get("questions"));
                    Type listType = new TypeToken<List<QuestionModel>>(){}.getType();
                    List<QuestionModel> cachedQuestions = gson.fromJson(questionsJson, listType);
                    return cachedQuestions != null && !cachedQuestions.isEmpty();
                }
            } catch (Exception e) {
                Log.e(TAG, "Eroare la parsing cache local", e);
            }
        }
        return false;
    }
    private void saveDataSourcePreferenceHybrid(String pref) {
        dataSourcePreference = pref;
        getSharedPreferences("BanatGamePrefs", MODE_PRIVATE).edit().putString(DATA_SOURCE_PREF_KEY, pref).apply();
    }
    private void showNoInternetForPreferredDatabaseDialogHybrid(int numQuestions) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("❌ Nu există conexiune la internet")
            .setMessage("Preferința este baza de date, dar nu există conexiune. Încercați cache local?")
            .setPositiveButton("Cache Local", (dialog, which) -> loadQuestionsFromLocalCacheHybrid(numQuestions))
            .setNegativeButton("Închide", (dialog, which) -> finish())
            .show();
    }
    private void showNoCacheForPreferredLocalDialogHybrid(int numQuestions) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("❌ Nu există cache local")
            .setMessage("Preferința este cache local, dar nu există date salvate. Încercați baza de date?")
            .setPositiveButton("Baza de date", (dialog, which) -> loadQuestionsFromDatabaseHybrid(numQuestions))
            .setNegativeButton("Închide", (dialog, which) -> finish())
            .show();
    }
    private void showDataSourceSelectionDialogHybrid(int numQuestions) {
        boolean hasInternet = isInternetAvailableDirectCheck();
        boolean hasLocalCache = checkIfLocalCacheExistsHybrid();
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);
        dialogBuilder.setTitle("📚 Alegeți sursa întrebărilor");
        dialogBuilder.setCancelable(false);
        if (hasInternet && hasLocalCache) {
            dialogBuilder.setMessage("📊 Ambele surse sunt disponibile!\n\n🌐 Baza de Date: Întrebări actualizate\n📱 Cache Local: Încărcare rapidă\n🎯 Automat: Alege cel mai bun\n\nCe preferați?");
            dialogBuilder.setPositiveButton("🌐 Baza de Date", (dialog, which) -> {
                saveDataSourcePreferenceHybrid("always_database");
                loadQuestionsFromDatabaseHybrid(numQuestions);
            });
            dialogBuilder.setNegativeButton("📱 Cache Local", (dialog, which) -> {
                saveDataSourcePreferenceHybrid("always_cache");
                loadQuestionsFromLocalCacheHybrid(numQuestions);
            });
            dialogBuilder.setNeutralButton("🎯 Automat", (dialog, which) -> {
                saveDataSourcePreferenceHybrid("auto");
                continueHybridLoadWithNumQuestions(numQuestions);
            });
        } else if (hasInternet) {
            dialogBuilder.setMessage("🌐 Doar conexiune la internet disponibilă. Încărcăm din baza de date?");
            dialogBuilder.setPositiveButton("OK", (dialog, which) -> {
                saveDataSourcePreferenceHybrid("always_database");
                loadQuestionsFromDatabaseHybrid(numQuestions);
            });
        } else if (hasLocalCache) {
            dialogBuilder.setMessage("📱 Doar cache local disponibil. Încărcăm din cache?");
            dialogBuilder.setPositiveButton("OK", (dialog, which) -> {
                saveDataSourcePreferenceHybrid("always_cache");
                loadQuestionsFromLocalCacheHybrid(numQuestions);
            });
        } else {
            dialogBuilder.setMessage("❌ Nicio sursă disponibilă. Conectați-vă la internet sau jucați după ce ați descărcat întrebările.");
            dialogBuilder.setPositiveButton("Închide", (dialog, which) -> finish());
        }
        dialogBuilder.show();
    }
    private void loadQuestionsFromDatabaseHybrid(int numQuestions) {
        progressBar.setVisibility(View.VISIBLE);
        if (!isInternetAvailableDirectCheck()) {
            progressBar.setVisibility(View.GONE);
            showNoInternetForPreferredDatabaseDialogHybrid(numQuestions);
            return;
        }
        questionRepository.getQuestionsAsModels(REGION, GAME_TYPE)
            .thenAccept(loadedQuestions -> {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (loadedQuestions != null && !loadedQuestions.isEmpty()) {
                        List<QuestionModel> limitedQuestions = loadedQuestions;
                        if (loadedQuestions.size() > numQuestions) {
                            limitedQuestions = new ArrayList<>(loadedQuestions);
                            Collections.shuffle(limitedQuestions);
                            limitedQuestions = limitedQuestions.subList(0, numQuestions);
                        }
                        firestoreQuestions = limitedQuestions;
                        enhancedQuestions = convertToEnhancedQuestions(limitedQuestions);
                        questions = convertFirestoreToLocalQuestions(limitedQuestions);
                        totalQuestions = questions.size();
                        progressBar.setMax(totalQuestions);
                        saveQuestionsToLocalCacheHybrid(limitedQuestions);
                        isDataLoaded = true;
                        displayQuestion();
                        updateScore();
                        startTimer();
                        Toast.makeText(this, "🌐 Întrebări încărcate din baza de date!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    loadQuestionsFromLocalCacheHybrid(numQuestions);
                });
            })
            .exceptionally(e -> {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    loadQuestionsFromLocalCacheHybrid(numQuestions);
                });
                return null;
            });
    }
    private void loadQuestionsFromLocalCacheHybrid(int numQuestions) {
        String cachedJson = getSharedPreferences("HybridStorage", MODE_PRIVATE).getString(CACHE_KEY, null);
        if (cachedJson != null && !cachedJson.isEmpty()) {
            try {
                Gson gson = new Gson();
                Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
                Map<String, Object> cacheData = gson.fromJson(cachedJson, mapType);
                if (cacheData != null && cacheData.containsKey("questions")) {
                    String questionsJson = gson.toJson(cacheData.get("questions"));
                    Type listType = new TypeToken<List<QuestionModel>>(){}.getType();
                    List<QuestionModel> cachedQuestions = gson.fromJson(questionsJson, listType);
                    if (cachedQuestions != null && !cachedQuestions.isEmpty()) {
                        List<QuestionModel> limitedQuestions = cachedQuestions;
                        if (cachedQuestions.size() > numQuestions) {
                            limitedQuestions = new ArrayList<>(cachedQuestions);
                            Collections.shuffle(limitedQuestions);
                            limitedQuestions = limitedQuestions.subList(0, numQuestions);
                        }
                        firestoreQuestions = limitedQuestions;
                        enhancedQuestions = convertToEnhancedQuestions(limitedQuestions);
                        questions = convertFirestoreToLocalQuestions(limitedQuestions);
                        totalQuestions = questions.size();
                        progressBar.setMax(totalQuestions);
                        isDataLoaded = true;
                        displayQuestion();
                        updateScore();
                        startTimer();
                        Toast.makeText(this, "📱 Întrebări încărcate din cache local!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Eroare la parsing cache local", e);
            }
        }
        Toast.makeText(this, "❌ Nu există întrebări valide în cache local!", Toast.LENGTH_LONG).show();
        initializeHardcodedQuestions();
        displayQuestion();
        updateScore();
        startTimer();
    }
    private void saveQuestionsToLocalCacheHybrid(List<QuestionModel> questions) {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(questions);
            getSharedPreferences("HybridStorage", MODE_PRIVATE)
                .edit()
                .putString(CACHE_KEY, json)
                .putLong(CACHE_TIMESTAMP_KEY, System.currentTimeMillis())
                .apply();
            if (syncManager != null) {
                Map<String, Object> syncData = new HashMap<>();
                syncData.put("count", questions.size());
                syncData.put("timestamp", System.currentTimeMillis());
                syncData.put("region", REGION);
                syncData.put("gameType", GAME_TYPE);
                syncManager.saveData("questions_cache", REGION + "_" + GAME_TYPE, syncData, new SyncManager.SyncCallback() {
                    @Override
                    public void onSyncComplete(boolean success, String message) {
                        if (success) {
                            Log.d(TAG, "🔄 ✅ Sincronizare completă: " + message);
                        } else {
                            Log.w(TAG, "🔄 ⚠️ Sincronizare incompletă: " + message);
                        }
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "💾 ❌ Eroare la salvarea în cache local (hybrid)", e);
        }
    }
} 