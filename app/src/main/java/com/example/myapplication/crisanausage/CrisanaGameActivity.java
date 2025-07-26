package com.example.myapplication.crisanausage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.AppCompatSpinner;
import android.widget.ArrayAdapter;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import com.example.myapplication.R;
import com.example.myapplication.RomApp.PointsManager;
import com.example.myapplication.models.EnhancedQuestionModel;
import com.example.myapplication.utils.RegionGameEnhancer;
import com.example.myapplication.utils.HapticFeedbackType;
import com.example.myapplication.utils.SyncManager;
import com.example.myapplication.models.QuestionModel;
import com.example.myapplication.repository.FirestoreQuestionRepository;
import com.example.myapplication.Joc1.AchievementManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Date;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.auth.FirebaseUser;
import android.widget.Spinner;

public class CrisanaGameActivity extends AppCompatActivity {
    private static final String TAG = "CrisanaGameActivity";
    private static final String REGION = "crisana";
    private static final String GAME_TYPE = "quiz";
    
    // Enhanced UI Components
    private TextView questionText;
    private TextView scoreText;
    private TextView timerText;
    private TextView questionNumberText;
    private Button option1Button;
    private Button option2Button;
    private Button option3Button;
    private Button option4Button;
    private Button nextButton;
    private CardView feedbackCard;
    private TextView feedbackText;
    private ProgressBar timeProgressBar;

    // Enhanced lifeline buttons
    private ImageButton fiftyFiftyButton;
    private ImageButton hintButton;
    private ImageButton skipQuestionButton;
    private ImageButton quitButton;

    // Enhanced game systems using RegionGameEnhancer
    private RegionGameEnhancer gameEnhancer;
    private RegionGameEnhancer.GameConstants gameConstants;
    
    // Crișana-specific managers
    private DifficultyManager difficultyManager;
    private GameModeManager gameModeManager;
    private PlayerProgressTracker progressTracker;
    private AchievementManager achievementManager;
    private SyncManager syncManager;
    
    // Enhanced question management
    private List<Question> questions;
    private List<EnhancedQuestionModel> enhancedQuestions;
    private List<QuestionModel> firestoreQuestions;
    
    // Enhanced game state variables
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int correctAnswers = 0;
    private int maxStreak = 0;
    private long totalTime = 0;
    private long questionStartTime = 0;
    private boolean answerSelected = false;
    
    // Lifeline states
    private boolean fiftyFiftyUsed = false;
    private boolean hintUsed = false;
    private boolean skipQuestionUsed = false;
    
    // Enhanced timer and UI
    private CountDownTimer timer;
    private long timeRemainingMs;
    private PointsManager pointsManager;
    private SharedPreferences sharedPreferences;
    private Random random = new Random();
    private Handler handler = new Handler(Looper.getMainLooper());
    private FirestoreQuestionRepository questionRepository;
    private boolean isDataLoaded = false;
    
    // Constants - now dynamic based on game mode/difficulty
    private static final String PREFS_NAME = "CrisanaGamePrefs";
    private static final String HIGH_SCORE_KEY = "highScoreCrisana";
    private static final long ANSWER_FEEDBACK_DELAY_MS = 1000;

    private AlertDialog exitDialog; // Referință la dialogul de ieșire

    // --- HYBRID SYSTEM FIELDS ---
    private static final String DATA_SOURCE_PREF_KEY = "data_source_preference";
    private static final String CACHE_KEY = "questions_cache_" + REGION + "_" + GAME_TYPE;
    private static final String CACHE_TIMESTAMP_KEY = CACHE_KEY + "_timestamp";
    private static final long CACHE_EXPIRY_MS = 24 * 60 * 60 * 1000; // 24h
    private String dataSourcePreference = "ask_every_time";


    
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
                loadQuestionsFromDatabase();
            })
            .setNegativeButton("📱 Mai târziu", null)
            .show();
    }
    
    /**
     * 💾 Verifică preferința utilizatorului și încarcă în consecință
     */
    private void checkUserPreferenceAndLoad() {
        // Ensure syncManager is initialized
        if (syncManager == null) {
            syncManager = SyncManager.getInstance(this);
        }
        
        // Verificăm dacă există conexiune la internet
        boolean hasInternet = syncManager.isInternetAvailable();
        
        if (hasInternet) {
            // Încărcăm direct din baza de date
            Toast.makeText(this, "🌐 Încărcăm din baza de date...", Toast.LENGTH_SHORT).show();
            loadQuestionsFromDatabase();
        } else {
            // Nu există conexiune la internet
            showNoInternetDialog();
        }
    }
    
    /**
     * 📱 Afișează dialog când nu există internet pentru încărcarea din baza de date
     */
    private void showNoInternetDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("❌ Nu există conexiune la internet")
            .setMessage("Pentru regiunea Crișana, întrebările se încarcă exclusiv din baza de date.\n\n" +
                       "Vă rugăm să verificați conexiunea la internet și să încercați din nou.")
            .setPositiveButton("🔄 Reîncearcă", (dialog, which) -> {
                checkUserPreferenceAndLoad();
            })
            .setNegativeButton("❌ Închide", (dialog, which) -> {
                finish();
            })
            .setCancelable(false)
            .show();
    }
    
    /**
     * 🌐 Încarcă întrebări din baza de date
     */
    private void loadQuestionsFromDatabase() {
        // Afișăm un indicator de încărcare
        timeProgressBar.setVisibility(View.VISIBLE);
        
        Log.d(TAG, "🌐 Încărcăm din baza de date pentru " + REGION);
        
        if (!syncManager.isInternetAvailable()) {
            timeProgressBar.setVisibility(View.GONE);
            showNoInternetDialog();
            return;
        }
        
        questionRepository.getQuestions(REGION, GAME_TYPE)
            .addOnSuccessListener(querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    Log.d(TAG, "✅ Întrebări găsite în baza de date: " + querySnapshot.size());
                    
                    // Convertim documentele la obiecte QuestionModel
                    List<com.example.myapplication.model.QuestionModel> loadedQuestions = new ArrayList<>();
                    
                    for (int i = 0; i < querySnapshot.size(); i++) {
                        try {
                            Map<String, Object> data = querySnapshot.getDocuments().get(i).getData();
                            
                            if (data != null) {
                                String question = (String) data.get("question");
                                String correctAnswer = (String) data.get("correctAnswer");
                                
                                @SuppressWarnings("unchecked")
                                List<String> incorrectAnswers = (List<String>) data.get("incorrectAnswers");
                                
                                if (incorrectAnswers == null) {
                                    incorrectAnswers = new ArrayList<>();
                                }
                                
                                String fact = data.get("fact") != null ? (String) data.get("fact") : "";
                                
                                // Creăm lista de opțiuni
                                List<String> options = new ArrayList<>();
                                options.add(correctAnswer); // Adăugăm răspunsul corect primul
                                options.addAll(incorrectAnswers); // Adăugăm răspunsurile incorecte
                                
                                // Amestecăm opțiunile
                                Collections.shuffle(options);
                                
                                // Determinăm indexul răspunsului corect după amestecare
                                int correctIndex = options.indexOf(correctAnswer);
                                
                                // Creăm obiectul QuestionModel
                                com.example.myapplication.model.QuestionModel questionModel = 
                                    new com.example.myapplication.model.QuestionModel(
                                        question, 
                                        options, 
                                        correctIndex, 
                                        fact, 
                                        0, // imageResourceId
                                        REGION
                                    );
                                
                                // Adăugăm în lista de întrebări
                                loadedQuestions.add(questionModel);
                                
                                Log.d(TAG, "✅ Întrebare încărcată: " + question);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "❌ Eroare la procesarea întrebării", e);
                        }
                    }
                    
                    if (!loadedQuestions.isEmpty()) {
                        // Convert to models.QuestionModel for compatibility
                        List<QuestionModel> modelsQuestions = convertToModelsQuestionModels(loadedQuestions);
                        
                        firestoreQuestions = modelsQuestions;
                        enhancedQuestions = convertToEnhancedQuestions(modelsQuestions);
                        
                        // Convertim la formatul intern Question
                        convertFirestoreToLocalQuestions(modelsQuestions);
                        
                        // Salvăm în cache local
                        saveQuestionsToLocalCache(modelsQuestions);
                        
                        // Actualizăm timestamp-ul
                        String cacheKey = "questions_cache_" + REGION + "_" + GAME_TYPE + "_timestamp";
                        getSharedPreferences("HybridStorage", MODE_PRIVATE)
                            .edit()
                            .putLong(cacheKey, System.currentTimeMillis())
                            .apply();
                        
                        isDataLoaded = true;
                        displayQuestion();
                        startTimer();
                        
                    } else {
                        Log.w(TAG, "⚠️ Nu s-au găsit întrebări în baza de date");
                        showNoDatabaseQuestionsDialog();
                    }
                } else {
                    Log.w(TAG, "⚠️ Nu s-au găsit întrebări în baza de date");
                    showNoDatabaseQuestionsDialog();
                }
                
                timeProgressBar.setVisibility(View.GONE);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ Eroare la încărcarea din baza de date", e);
                timeProgressBar.setVisibility(View.GONE);
                showDatabaseErrorDialog();
            });
    }
    
    /**
     * 📋 Afișează dialog când nu există întrebări în baza de date
     */
    private void showNoDatabaseQuestionsDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("❌ Nu există întrebări în baza de date")
            .setMessage("Nu am găsit întrebări pentru regiunea Crișana în baza de date.\n\n" +
                       "Vă rugăm să verificați dacă:\n" +
                       "• Există întrebări adăugate în baza de date pentru Crișana\n" +
                       "• Aveți permisiunile necesare pentru accesarea bazei de date")
            .setPositiveButton("🔄 Reîncearcă", (dialog, which) -> {
                loadQuestionsFromDatabase();
            })
            .setNegativeButton("❌ Închide", (dialog, which) -> {
                finish();
            })
            .setCancelable(false)
            .show();
    }
    
    /**
     * 📱 Afișează dialog pentru erori de bază de date
     */
    private void showDatabaseErrorDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("❌ Eroare bază de date")
            .setMessage("Nu s-a putut conecta la baza de date.\n\n" +
                       "Vă rugăm să verificați:\n" +
                       "• Conexiunea la internet\n" +
                       "• Configurația Firebase\n" +
                       "• Permisiunile de acces")
            .setPositiveButton("🔄 Reîncearcă", (dialog, which) -> {
                loadQuestionsFromDatabase();
            })
            .setNegativeButton("❌ Închide", (dialog, which) -> {
                finish();
            })
            .setCancelable(false)
            .show();
    }
    
    /**
     * 💾 Salvează întrebările în cache local
     */
    private void saveQuestionsToLocalCache(List<QuestionModel> questions) {
        Log.d(TAG, "💾 Salvăm întrebările în cache local pentru utilizare offline");
        
        try {
            Gson gson = new Gson();
            String json = gson.toJson(questions);
            
            String cacheKey = "questions_cache_" + REGION + "_" + GAME_TYPE;
            getSharedPreferences("HybridStorage", MODE_PRIVATE)
                .edit()
                .putString(cacheKey, json)
                .apply();
            
            Log.d(TAG, "💾 ✅ Întrebări salvate în cache local: " + questions.size());
            
            // Salvăm datele de sincronizare
            Map<String, Object> syncData = new HashMap<>();
            syncData.put("count", questions.size());
            syncData.put("timestamp", System.currentTimeMillis());
            syncData.put("region", REGION);
            syncData.put("gameType", GAME_TYPE);
            
            if (syncManager != null) {
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
            Log.e(TAG, "💾 ❌ Eroare la salvarea în cache local", e);
        }
    }
    
    /**
     * 🔍 Convertește întrebările la formatul EnhancedQuestionModel
     */
    private List<EnhancedQuestionModel> convertToEnhancedQuestions(List<QuestionModel> questions) {
        List<EnhancedQuestionModel> enhancedQuestions = new ArrayList<>();
        
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
            
            enhancedQuestions.add(enhancedQuestion);
        }
        
        return enhancedQuestions;
    }
    
    /**
     * 🧠 Inferă categoria întrebării din text
     */
    private EnhancedQuestionModel.Category inferCategory(String questionText) {
        questionText = questionText.toLowerCase();
        
        if (questionText.contains("istorie") || questionText.contains("trecut") || 
            questionText.contains("anul") || questionText.contains("secol")) {
            return EnhancedQuestionModel.Category.HISTORY;
        }
        
        if (questionText.contains("geografie") || questionText.contains("munte") || 
            questionText.contains("râu") || questionText.contains("oraș")) {
            return EnhancedQuestionModel.Category.GEOGRAPHY;
        }
        
        if (questionText.contains("cultură") || questionText.contains("artă") || 
            questionText.contains("muzeu") || questionText.contains("festival")) {
            return EnhancedQuestionModel.Category.CULTURE;
        }
        
        if (questionText.contains("tradiție") || questionText.contains("obicei") || 
            questionText.contains("folclor") || questionText.contains("costum")) {
            return EnhancedQuestionModel.Category.CULTURE;
        }
        
        // Default
        return EnhancedQuestionModel.Category.GENERAL;
    }
    
    /**
     * 🧠 Inferă dificultatea întrebării
     */
    private EnhancedQuestionModel.Difficulty inferDifficulty(QuestionModel question) {
        // Lungimea întrebării poate indica complexitatea
        int questionLength = question.getQuestion().length();
        
        // Numărul de cuvinte
        int wordCount = question.getQuestion().split("\\s+").length;
        
        // Numărul de opțiuni
        int optionCount = question.getAnswers().size();
        
        if (questionLength > 150 || wordCount > 25) {
            return EnhancedQuestionModel.Difficulty.HARD;
        } else if (questionLength > 80 || wordCount > 15) {
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
        
        // Tag-uri pentru regiuni
        if (text.contains("oradea")) tags.add("oradea");
        if (text.contains("bihor")) tags.add("bihor");
        if (text.contains("arad")) tags.add("arad");
        if (text.contains("crișana")) tags.add("crisana");
        
        // Tag-uri pentru categorii
        if (text.contains("istorie")) tags.add("istorie");
        if (text.contains("geografie")) tags.add("geografie");
        if (text.contains("cultură")) tags.add("cultura");
        if (text.contains("tradiție")) tags.add("traditie");
        
        return tags.toArray(new String[0]);
    }
    
    /**
     * ❌ Afișează dialog când nu există întrebări valide
     */
    private void showNoQuestionsError() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("❌ Nu există întrebări")
            .setMessage("Nu am găsit întrebări valide pentru afișare.\n\n" +
                       "Vă rugăm să verificați conexiunea la internet și să încercați din nou.")
            .setPositiveButton("🔄 Reîncearcă", (dialog, which) -> {
                loadQuestionsFromDatabase();
            })
            .setNegativeButton("❌ Închide", (dialog, which) -> {
                finish();
            })
            .setCancelable(false)
            .show();
    }
    
    /**
     * 🔄 Convertește întrebările din Firestore la formatul intern
     */
    private void convertFirestoreToLocalQuestions(List<QuestionModel> firestoreQuestions) {
        questions = new ArrayList<>();
        
        for (QuestionModel firestoreQuestion : firestoreQuestions) {
            Question question = new Question(
                firestoreQuestion.getQuestion(),
                firestoreQuestion.getAnswers().toArray(new String[0]),
                firestoreQuestion.getCorrectAnswerIndex(),
                firestoreQuestion.getFact()
            );
            
            questions.add(question);
        }
        
        Log.d(TAG, "🔄 Convertite " + questions.size() + " întrebări la formatul intern");
    }
    
    /**
     * 🔄 Convertește un obiect model.QuestionModel la models.QuestionModel
     */
    private com.example.myapplication.models.QuestionModel convertToModelsQuestionModel(com.example.myapplication.model.QuestionModel modelQuestion) {
        List<String> incorrectAnswers = new ArrayList<>();
        List<String> options = modelQuestion.getOptions();
        int correctIndex = modelQuestion.getCorrectAnswerIndex();
        
        // Extract correct answer and incorrect answers
        String correctAnswer = "";
        if (options != null && correctIndex >= 0 && correctIndex < options.size()) {
            correctAnswer = options.get(correctIndex);
            
            // Add all options except the correct one to incorrectAnswers
            for (int i = 0; i < options.size(); i++) {
                if (i != correctIndex) {
                    incorrectAnswers.add(options.get(i));
                }
            }
        }
        
        return new com.example.myapplication.models.QuestionModel(
            modelQuestion.getQuestion(),
            correctAnswer,
            incorrectAnswers,
            modelQuestion.getImageResourceId(),
            modelQuestion.getExplanation()
        );
    }
    
    /**
     * 🔄 Convertește o listă de model.QuestionModel la models.QuestionModel
     */
    private List<com.example.myapplication.models.QuestionModel> convertToModelsQuestionModels(List<com.example.myapplication.model.QuestionModel> modelQuestions) {
        List<com.example.myapplication.models.QuestionModel> result = new ArrayList<>();
        if (modelQuestions != null) {
            for (com.example.myapplication.model.QuestionModel q : modelQuestions) {
                result.add(convertToModelsQuestionModel(q));
            }
        }
        return result;
    }

    /**
     * Actualizează scorul afișat
     */
    private void updateScore() {
        if (scoreText != null) {
            scoreText.setText(String.format(Locale.getDefault(), "Scor: %d", score));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crisana_game);
        
        Log.d(TAG, "🚀 CrisanaGameActivity onCreate - Inițializare");
        
        // Inițializăm componentele principale
        initializeEnhancedSystems();
        initializeCrisanaManagers();
        initializeViews();
        setupGameModeAndDifficulty();
        setupClickListeners();
        setupLifelines();
        
        // Afișăm un mesaj de bun venit
        Toast.makeText(this, "Bun venit la Quiz-ul Crișana!", Toast.LENGTH_SHORT).show();
        
        // Citim preferința sursei de date
        dataSourcePreference = sharedPreferences.getString(DATA_SOURCE_PREF_KEY, "ask_every_time");
        // Încărcăm întrebările folosind sistemul hibrid
        checkUserPreferenceAndLoadHybrid();
        
        // Actualizăm scorul inițial
        updateScore();
        
        // Începem să urmărim progresul jucătorului
        if (progressTracker != null) {
            // Incrementăm numărul de jocuri
            int totalGames = progressTracker.getTotalGames() + 1;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("total_games", totalGames);
            editor.apply();
        }
        
        Log.d(TAG, "✅ Inițializare completă");
    }

    /**
     * 🎮 Inițializează sistemele îmbunătățite
     */
    private void initializeEnhancedSystems() {
        // Inițializăm sistemele de joc
        gameEnhancer = new RegionGameEnhancer(this, REGION);
        gameConstants = new RegionGameEnhancer.GameConstants(30000, 10, 50, 3);
        
        // Inițializăm managerul de puncte
        pointsManager = PointsManager.getInstance(this);
        
        // Inițializăm managerul de sincronizare
        syncManager = SyncManager.getInstance(this);
        
        // Inițializăm repository-ul pentru întrebări
        questionRepository = FirestoreQuestionRepository.getInstance();
        
        // Inițializăm managerul de realizări
        achievementManager = AchievementManager.getInstance(this);
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
        
        // Inițializăm dificultatea
        difficultyManager.setManualDifficulty(DifficultyManager.DifficultyLevel.NORMAL);
        
        Log.d(TAG, "✅ Mod de joc și dificultate configurate: " + 
              gameModeManager.getCurrentGameMode() + ", " + 
              difficultyManager.getCurrentDifficulty());
    }

    /**
     * 🎮 Inițializează managerii specifici pentru Crișana
     */
    private void initializeCrisanaManagers() {
        // Inițializăm managerul de dificultate
        difficultyManager = new DifficultyManager(this);
        
        // Inițializăm managerul de mod de joc
        gameModeManager = new GameModeManager(this);
        
        // Inițializăm tracker-ul de progres
        progressTracker = new PlayerProgressTracker(this);
        
        // Încărcăm preferințele salvate
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }
    
    /**
     * 🎮 Inițializează elementele de interfață
     */
    private void initializeViews() {
        // Inițializăm elementele de interfață principale
        questionText = findViewById(R.id.questionText);
        scoreText = findViewById(R.id.scoreText);
        timerText = findViewById(R.id.timerText);
        questionNumberText = findViewById(R.id.questionNumber);
        option1Button = findViewById(R.id.option1Button);
        option2Button = findViewById(R.id.option2Button);
        option3Button = findViewById(R.id.option3Button);
        option4Button = findViewById(R.id.option4Button);
        nextButton = findViewById(R.id.nextButton);
        feedbackCard = findViewById(R.id.feedbackCard);
        feedbackText = findViewById(R.id.feedbackText);
        timeProgressBar = findViewById(R.id.timeProgressBar);
        
        // Încercăm să inițializăm butoanele pentru lifeline - acestea pot să nu existe în layout
        try {
            fiftyFiftyButton = findViewById(R.id.fiftyFiftyButton);
            hintButton = findViewById(R.id.hintButton);
            skipQuestionButton = findViewById(R.id.skipQuestionButton);
            quitButton = findViewById(R.id.quitButton);
        } catch (Exception e) {
            Log.w(TAG, "Unele butoane pentru lifeline nu au fost găsite în layout", e);
        }
        
        // Ascundem cardul de feedback inițial
        feedbackCard.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);
    }
    
    /**
     * 🎮 Configurează ascultătorii de click
     */
    private void setupClickListeners() {
        // Configurăm butoanele de opțiuni
        option1Button.setOnClickListener(v -> checkAnswer(0));
        option2Button.setOnClickListener(v -> checkAnswer(1));
        option3Button.setOnClickListener(v -> checkAnswer(2));
        option4Button.setOnClickListener(v -> checkAnswer(3));
        
        // Configurăm butonul de next
        nextButton.setOnClickListener(v -> {
            // Trecem la următoarea întrebare
            currentQuestionIndex++;
            if (currentQuestionIndex < questions.size()) {
                displayQuestion();
                startTimer();
            } else {
                finishGame();
            }
        });
        
        // Configurăm butonul de quit - verificăm dacă există
        if (quitButton != null) {
            quitButton.setOnClickListener(v -> showQuitConfirmation());
        }
    }
    
    /**
     * 🎮 Configurează lifeline-urile
     */
    private void setupLifelines() {
        // Încărcăm starea lifeline-urilor
        loadLifelineState();
        
        // Verificăm dacă butoanele există înainte de a configura click listeners
        
        // Configurăm butonul 50:50
        if (fiftyFiftyButton != null) {
            fiftyFiftyButton.setOnClickListener(v -> {
                if (!fiftyFiftyUsed) {
                    useFiftyFifty();
                } else {
                    Toast.makeText(this, "Ai folosit deja acest ajutor!", Toast.LENGTH_SHORT).show();
                }
            });
            fiftyFiftyButton.setAlpha(fiftyFiftyUsed ? 0.5f : 1.0f);
        }
        
        // Configurăm butonul de hint
        if (hintButton != null) {
            hintButton.setOnClickListener(v -> {
                if (!hintUsed) {
                    useHint();
                } else {
                    Toast.makeText(this, "Ai folosit deja acest ajutor!", Toast.LENGTH_SHORT).show();
                }
            });
            hintButton.setAlpha(hintUsed ? 0.5f : 1.0f);
        }
        
        // Configurăm butonul de skip
        if (skipQuestionButton != null) {
            skipQuestionButton.setOnClickListener(v -> {
                if (!skipQuestionUsed) {
                    useSkipQuestion();
                } else {
                    Toast.makeText(this, "Ai folosit deja acest ajutor!", Toast.LENGTH_SHORT).show();
                }
            });
            skipQuestionButton.setAlpha(skipQuestionUsed ? 0.5f : 1.0f);
        }
    }
    
    /**
     * 🎮 Folosește lifeline-ul 50:50
     */
    private void useFiftyFifty() {
        if (questions == null || currentQuestionIndex >= questions.size()) {
            return;
        }
        
        Question currentQuestion = questions.get(currentQuestionIndex);
        int correctAnswerIndex = currentQuestion.getCorrectAnswerIndex();
        
        // Creăm o listă de butoane pentru a le putea manipula mai ușor
        List<Button> optionButtons = Arrays.asList(
            option1Button, option2Button, option3Button, option4Button
        );
        
        // Găsim două butoane incorecte pentru a le ascunde
        List<Integer> incorrectIndices = new ArrayList<>();
        for (int i = 0; i < optionButtons.size(); i++) {
            if (i != correctAnswerIndex) {
                incorrectIndices.add(i);
            }
        }
        
        // Amestecăm indicii incorecți și păstrăm doar doi
        Collections.shuffle(incorrectIndices);
        incorrectIndices = incorrectIndices.subList(0, Math.min(2, incorrectIndices.size()));
        
        // Ascundem butoanele incorecte
        for (int index : incorrectIndices) {
            optionButtons.get(index).setVisibility(View.INVISIBLE);
        }
        
        // Marcăm lifeline-ul ca folosit
        fiftyFiftyUsed = true;
        fiftyFiftyButton.setAlpha(0.5f);
        
        // Salvăm starea lifeline-urilor
        saveLifelineState();
        
        // Adăugăm un efect de sunet și feedback haptic
        if (gameEnhancer != null) {
            gameEnhancer.performHapticFeedback(HapticFeedbackType.LIFELINE);
        }
        
        Toast.makeText(this, "Ai folosit ajutorul 50:50!", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 🎮 Folosește lifeline-ul de hint
     */
    private void useHint() {
        if (questions == null || currentQuestionIndex >= questions.size()) {
            return;
        }
        
        Question currentQuestion = questions.get(currentQuestionIndex);
        String explanation = currentQuestion.getExplanation();
        
        // Dacă nu există explicație, generăm un hint generic
        if (explanation == null || explanation.isEmpty()) {
            explanation = "Hint: Gândește-te la geografia și istoria regiunii Crișana.";
        } else {
            // Dacă există explicație, extragem doar prima propoziție ca hint
            int endOfFirstSentence = explanation.indexOf('.');
            if (endOfFirstSentence > 0) {
                explanation = explanation.substring(0, endOfFirstSentence + 1);
            }
            explanation = "Hint: " + explanation;
        }
        
        // Afișăm hint-ul
        Toast.makeText(this, explanation, Toast.LENGTH_LONG).show();
        
        // Marcăm lifeline-ul ca folosit
        hintUsed = true;
        hintButton.setAlpha(0.5f);
        
        // Salvăm starea lifeline-urilor
        saveLifelineState();
        
        // Adăugăm un efect de sunet și feedback haptic
        if (gameEnhancer != null) {
            gameEnhancer.performHapticFeedback(HapticFeedbackType.LIFELINE);
        }
    }
    
    /**
     * 🎮 Folosește lifeline-ul de skip
     */
    private void useSkipQuestion() {
        if (questions == null || currentQuestionIndex >= questions.size()) {
            return;
        }
        
        // Trecem la următoarea întrebare
        currentQuestionIndex++;
        
        if (currentQuestionIndex < questions.size()) {
            // Afișăm un mesaj de confirmare
            Toast.makeText(this, "Întrebare sărită!", Toast.LENGTH_SHORT).show();
            
            // Afișăm următoarea întrebare
            displayQuestion();
            startTimer();
        } else {
            // Dacă am ajuns la sfârșitul listei, terminăm jocul
            finishGame();
        }
        
        // Marcăm lifeline-ul ca folosit
        skipQuestionUsed = true;
        skipQuestionButton.setAlpha(0.5f);
        
        // Salvăm starea lifeline-urilor
        saveLifelineState();
        
        // Adăugăm un efect de sunet și feedback haptic
        if (gameEnhancer != null) {
            gameEnhancer.performHapticFeedback(HapticFeedbackType.LIFELINE);
        }
    }
    
    /**
     * 🎮 Afișează dialog de confirmare pentru quit
     */
    private void showQuitConfirmation() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("❓ Ești sigur?")
            .setMessage("Vrei să părăsești jocul?\n\nProgresul tău va fi salvat.")
            .setPositiveButton("✅ Da", (dialog, which) -> {
                finish();
            })
            .setNegativeButton("❌ Nu", null)
            .show();
    }
    
    /**
     * 💾 Salvează starea lifeline-urilor
     */
    private void saveLifelineState() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("fiftyFiftyUsed", fiftyFiftyUsed);
        editor.putBoolean("hintUsed", hintUsed);
        editor.putBoolean("skipQuestionUsed", skipQuestionUsed);
        editor.apply();
    }
    
    /**
     * 💾 Încarcă starea lifeline-urilor
     */
    private void loadLifelineState() {
        fiftyFiftyUsed = sharedPreferences.getBoolean("fiftyFiftyUsed", false);
        hintUsed = sharedPreferences.getBoolean("hintUsed", false);
        skipQuestionUsed = sharedPreferences.getBoolean("skipQuestionUsed", false);
    }

    private void initQuestions() {
        // Initialize questions from Firestore or local cache based on user preference
        questionRepository = FirestoreQuestionRepository.getInstance();
        checkUserPreferenceAndLoad();
    }
    
    /**
     * Convert regular questions to enhanced questions for advanced tracking
     */
    private void convertToEnhancedQuestions() {
        if (questions != null && !questions.isEmpty()) {
            enhancedQuestions = new ArrayList<>();
            for (Question q : questions) {
                enhancedQuestions.add(
                    RegionGameEnhancer.convertToEnhanced(
                        q.question,
                        Arrays.asList(q.options),
                        q.correctAnswerIndex,
                        q.explanation,
                        "crisana"
                    )
                );
            }
            
            // Filter questions based on game mode
            if (gameModeManager != null) {
                enhancedQuestions = gameModeManager.filterQuestionsForGameMode(enhancedQuestions);
            }
        } else if (firestoreQuestions != null && !firestoreQuestions.isEmpty()) {
            enhancedQuestions = convertToEnhancedQuestions(firestoreQuestions);
            
            // Filter questions based on game mode
            if (gameModeManager != null) {
                enhancedQuestions = gameModeManager.filterQuestionsForGameMode(enhancedQuestions);
            }
        }
    }

    private void displayQuestion() {
        if (questions == null || questions.isEmpty()) {
            Log.e(TAG, "❌ Lista de întrebări este nulă sau goală");
            showNoQuestionsError();
            return;
        }
        
        if (currentQuestionIndex >= questions.size()) {
            Log.e(TAG, "❌ Index invalid: " + currentQuestionIndex + " din " + questions.size());
            finishGame();
            return;
        }
        
        // Resetăm starea butoanelor
        option1Button.setVisibility(View.VISIBLE);
        option2Button.setVisibility(View.VISIBLE);
        option3Button.setVisibility(View.VISIBLE);
        option4Button.setVisibility(View.VISIBLE);
        
        option1Button.setEnabled(true);
        option2Button.setEnabled(true);
        option3Button.setEnabled(true);
        option4Button.setEnabled(true);
        
        option1Button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        option2Button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        option3Button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        option4Button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        
        // Ascundem feedback-ul și butonul Next
        feedbackCard.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);
        
        // Afișăm întrebarea curentă
        Question currentQuestion = questions.get(currentQuestionIndex);
        questionText.setText(currentQuestion.getQuestion());
        
        // Actualizăm numărul întrebării
        if (questionNumberText != null) {
            questionNumberText.setText(String.format("Întrebarea %d/%d", 
                currentQuestionIndex + 1, questions.size()));
        }
        
        // Afișăm opțiunile
        String[] options = currentQuestion.getOptions();
        if (options.length >= 1) option1Button.setText(options[0]);
        if (options.length >= 2) option2Button.setText(options[1]);
        if (options.length >= 3) option3Button.setText(options[2]);
        if (options.length >= 4) option4Button.setText(options[3]);
        
        // Actualizăm progress bar-ul
        timeProgressBar.setProgress(100);
        
        // Resetăm variabilele de stare
        answerSelected = false;
        questionStartTime = System.currentTimeMillis();
        
        Log.d(TAG, "📝 Întrebare afișată: " + (currentQuestionIndex + 1) + "/" + questions.size());
    }

    private void startTimer() {
        if (timer != null) {
            timer.cancel();
        }
        
        long timeForQuestion = gameConstants != null ? gameConstants.timePerQuestion : 20000;
        
        timer = new CountDownTimer(timeForQuestion, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemainingMs = millisUntilFinished;
                int progress = (int) (millisUntilFinished * 100 / timeForQuestion);
                timeProgressBar.setProgress(progress);
                timerText.setText(String.format(Locale.getDefault(), "%d", millisUntilFinished / 1000 + 1));
            }
            
            @Override
            public void onFinish() {
                if (!answerSelected) {
                    // Timp expirat, marcăm toate răspunsurile ca incorecte
                    checkAnswer(-1);
                }
            }
        }.start();
    }

    private void checkAnswer(int selectedOptionIndex) {
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
        
        boolean isCorrect = false;
        String explanation = "";
        int correctIndex = 0;
        
        // Handle different question sources
        if (enhancedQuestions != null && !enhancedQuestions.isEmpty() && currentQuestionIndex < enhancedQuestions.size()) {
            // Using enhanced questions
            EnhancedQuestionModel currentQuestion = enhancedQuestions.get(currentQuestionIndex);
            isCorrect = selectedOptionIndex == currentQuestion.getCorrectAnswerIndex();
            explanation = currentQuestion.getFact();
            correctIndex = currentQuestion.getCorrectAnswerIndex();
            // Track answer with progress tracker
            if (progressTracker != null) {
                progressTracker.trackAnswer(currentQuestion, isCorrect, questionTime);
            }
        } else if (questions != null && !questions.isEmpty() && currentQuestionIndex < questions.size()) {
            // Using traditional questions
            Question currentQuestion = questions.get(currentQuestionIndex);
            int correctAnswerIndex = currentQuestion.getCorrectAnswerIndex();
            isCorrect = selectedOptionIndex == correctAnswerIndex;
            explanation = currentQuestion.getExplanation();
            correctIndex = correctAnswerIndex;
            // Track answer with progress tracker
            if (progressTracker != null && enhancedQuestions != null && currentQuestionIndex < enhancedQuestions.size()) {
                progressTracker.trackAnswer(enhancedQuestions.get(currentQuestionIndex), isCorrect, questionTime);
            }
        } else {
            Log.e(TAG, "No valid questions available in checkAnswer");
            return;
        }
        
        // Disable all buttons
        Button[] buttons = {option1Button, option2Button, option3Button, option4Button};
        for (Button button : buttons) {
            button.setEnabled(false);
        }
        
        // Calcul progresiv al punctajului
        int pointsForQuestion = 0;
        int basePoints = gameConstants != null ? gameConstants.pointsPerCorrectAnswer : 10;
        int timePerQuestion = gameConstants != null ? gameConstants.timePerQuestion : 20000;
        long timeSpent = questionTime;
        int modeBonus = 0;
        int finalScore = 0;
        if (isCorrect) {
            correctAnswers++;
            if (gameEnhancer != null) {
                gameEnhancer.performHapticFeedback(HapticFeedbackType.CORRECT);
            }
            // Bonus de timp: cu cât răspunzi mai repede, cu atât primești mai multe puncte
            int timeBonus = (int) Math.max(0, (timePerQuestion - timeSpent) / 1000); // 1 punct/secunda ramasă
            // Bonus de mod de joc
            if (gameModeManager != null) {
                modeBonus = gameModeManager.calculateModeBonus(basePoints, true, timeSpent);
            }
            // Scor final cu dificultate
            if (difficultyManager != null) {
                finalScore = difficultyManager.calculateFinalScore(basePoints + timeBonus + modeBonus);
            } else {
                finalScore = basePoints + timeBonus + modeBonus;
            }
            pointsForQuestion = finalScore;
            score += pointsForQuestion;
            if (correctAnswers > maxStreak) {
                maxStreak = correctAnswers;
            }
            highlightButton(selectedOptionIndex, true);
            feedbackText.setText(String.format(Locale.getDefault(),
                    "Corect! +%d puncte\n\n%s",
                    pointsForQuestion,
                    explanation));
        } else {
            if (gameEnhancer != null) {
                gameEnhancer.performHapticFeedback(HapticFeedbackType.WRONG);
            }
            highlightButton(selectedOptionIndex, false);
            highlightButton(correctIndex, true);
            feedbackText.setText(String.format(Locale.getDefault(),
                    "Incorect!\n\n%s",
                    explanation));
        }
        updateScore();
        nextButton.setVisibility(View.VISIBLE);
    }

    private void showCorrectAnswer() {
        // If no answer was selected before the timer ran out
        answerSelected = true;
        
        // Reset correctAnswers streak
        correctAnswers = 0;
        
        // Disable all buttons
        Button[] buttons = {option1Button, option2Button, option3Button, option4Button};
        for (Button button : buttons) {
            button.setEnabled(false);
        }
        
        // Get correct answer index and explanation from the appropriate question source
        int correctIndex = 0;
        String explanation = "";
        
        if (enhancedQuestions != null && !enhancedQuestions.isEmpty() && currentQuestionIndex < enhancedQuestions.size()) {
            // Using enhanced questions
            EnhancedQuestionModel currentQuestion = enhancedQuestions.get(currentQuestionIndex);
            correctIndex = currentQuestion.getCorrectAnswerIndex();
            explanation = currentQuestion.getFact();
            
            // Track missed answer with progress tracker
            if (progressTracker != null) {
                progressTracker.trackAnswer(currentQuestion, false, 
                    gameConstants != null ? gameConstants.timePerQuestion : 20000);
            }
        } else if (questions != null && !questions.isEmpty() && currentQuestionIndex < questions.size()) {
            // Using traditional questions
            Question currentQuestion = questions.get(currentQuestionIndex);
            correctIndex = currentQuestion.getCorrectAnswerIndex();
            explanation = currentQuestion.getExplanation();
        } else {
            Log.e(TAG, "No valid questions available in showCorrectAnswer");
            return;
        }
        
        // Highlight the correct answer
        highlightButton(correctIndex, true);
        
        // Show feedback with explanation
        feedbackCard.setVisibility(View.VISIBLE);
        feedbackText.setText(String.format(Locale.getDefault(), 
                "Timpul a expirat!\n\n%s", 
                explanation));
        
        // Show next button
        nextButton.setVisibility(View.VISIBLE);
    }

    private void highlightButton(int buttonIndex, boolean correct) {
        Button button;
        switch (buttonIndex) {
            case 0:
                button = option1Button;
                break;
            case 1:
                button = option2Button;
                break;
            case 2:
                button = option3Button;
                break;
            case 3:
                button = option4Button;
                break;
            default:
                return;
        }
        
        if (correct) {
            button.setBackgroundResource(R.drawable.button_correct);
        } else {
            button.setBackgroundResource(R.drawable.button_incorrect);
        }
    }

    private void showGameSummary() {
        // Calculate final score percentage
        int maxScore = questions != null ? questions.size() * 30 : 0; // Maximum possible score (assuming 30 points per question)
        int percentage = maxScore > 0 ? (int) ((float) score / maxScore * 100) : 0;
        
        // Award points to player based on performance
        int pointsAwarded = score / 5; // Convert game score to points
        pointsManager.addPoints(this, "crisana", pointsAwarded);
        
        // Create and show summary dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Joc terminat!");
        builder.setMessage(String.format(Locale.getDefault(),
                "Scorul tău final: %d puncte\nPerformanță: %d%%\n\nAi primit %d puncte în aplicație!",
                score, percentage, pointsAwarded));
        
        builder.setPositiveButton("Înapoi la pagina regiunii", (dialog, which) -> {
            // Return to the Crisana activity with score
            Intent intent = new Intent();
            intent.putExtra("GAME_SCORE", pointsAwarded);
            setResult(RESULT_OK, intent);
            finish();
        });
        
        builder.setCancelable(false);
        builder.show();
    }

    private void showExitConfirmation() {
        if (exitDialog != null && exitDialog.isShowing()) return;
        exitDialog = new AlertDialog.Builder(this)
                .setTitle("Ieșire")
                .setMessage("Ești sigur că vrei să ieși? Progresul va fi pierdut.")
                .setPositiveButton("Da", (dialog, which) -> finish())
                .setNegativeButton("Nu", null)
                .create();
        if (!isFinishing() && !isDestroyed()) {
            exitDialog.show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // 🔄 CHECK FOR UPDATES: Verificăm dacă sunt actualizări în baza de date
        if (syncManager != null && syncManager.isInternetAvailable() && isDataLoaded) {
            checkForQuestionUpdates();
        }
        
        // Add null check for questions list to prevent NullPointerException
        if (!answerSelected && questions != null && currentQuestionIndex < questions.size()) {
            startTimer();
        }
    }

    @Override
    public void onBackPressed() {
        showExitConfirmation();
    }

    @Override
    protected void onDestroy() {
        if (exitDialog != null && exitDialog.isShowing()) {
            exitDialog.dismiss();
        }
        super.onDestroy();
    }

    // Question class to store question data
    private static class Question {
        private final String question;
        private final String[] options;
        private final int correctAnswerIndex;
        private final String explanation;

        Question(String question, String[] options, int correctAnswerIndex, String explanation) {
            this.question = question;
            this.options = options;
            this.correctAnswerIndex = correctAnswerIndex;
            this.explanation = explanation;
        }

        String getQuestion() {
            return question;
        }

        String[] getOptions() {
            return options;
        }

        int getCorrectAnswerIndex() {
            return correctAnswerIndex;
        }

        String getExplanation() {
            return explanation;
        }
    }

    /**
     * 🏁 Finalizează jocul și afișează rezultatele
     */
    private void finishGame() {
        // Oprim timer-ul dacă rulează
        if (timer != null) {
            timer.cancel();
        }
        
        // Calculăm statisticile finale
        int totalQuestions = questions != null ? questions.size() : 0;
        float accuracy = totalQuestions > 0 ? (float) correctAnswers / totalQuestions * 100 : 0;
        long averageTimePerQuestion = totalQuestions > 0 ? totalTime / totalQuestions : 0;
        
        // Salvăm rezultatele
        saveGameResults(score, correctAnswers, totalQuestions, accuracy, averageTimePerQuestion);
        
        // Lansăm activitatea de final
        Intent intent = new Intent(this, CrisanaGameOverActivity.class);
        intent.putExtra("score", score);
        intent.putExtra("totalQuestions", totalQuestions);
        intent.putExtra("correctAnswers", correctAnswers);
        intent.putExtra("maxStreak", maxStreak);
        intent.putExtra("accuracy", accuracy);
        intent.putExtra("averageTime", averageTimePerQuestion);
        startActivity(intent);
        finish();
    }
    
    /**
     * 💾 Salvează rezultatele jocului
     */
    private void saveGameResults(int finalScore, int correctAnswers, int totalQuestions, 
                               float accuracy, long averageTime) {
        // Salvăm în preferințe locale
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("last_score", finalScore);
        editor.putInt("last_correct", correctAnswers);
        editor.putInt("last_total", totalQuestions);
        editor.putFloat("last_accuracy", accuracy);
        editor.putLong("last_average_time", averageTime);
        editor.apply();
        
        // Actualizăm statisticile de progres
        if (progressTracker != null) {
            // Finalizăm jocul și actualizăm statisticile
            String gameMode = gameModeManager != null ? 
                gameModeManager.getCurrentGameMode().displayName : "Quiz Clasic";
            progressTracker.finishGame(gameMode, correctAnswers, totalQuestions, totalTime);
        }
        
        // Actualizăm dificultatea pentru jocurile viitoare
        if (difficultyManager != null) {
            difficultyManager.updateDifficultyAfterGame(correctAnswers, totalQuestions, totalTime);
        }
        
        // Verificăm realizări noi
        if (achievementManager != null) {
            if (accuracy >= 90) {
                achievementManager.checkCrisanaExpertAchievement(accuracy);
            }
            if (finalScore >= 1000) {
                achievementManager.checkCrisanaExplorerAchievement(finalScore);
            }
        }
        
        // Salvăm rezultatele și în Firebase dacă utilizatorul este autentificat
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            
            Map<String, Object> result = new HashMap<>();
            result.put("userId", user.getUid());
            result.put("region", REGION);
            result.put("gameType", GAME_TYPE);
            result.put("score", finalScore);
            result.put("correctAnswers", correctAnswers);
            result.put("totalQuestions", totalQuestions);
            result.put("accuracy", accuracy);
            result.put("averageTime", averageTime);
            result.put("timestamp", FieldValue.serverTimestamp());
            
            db.collection("quiz_results")
                .add(result)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "✅ Rezultat salvat în Firebase: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Eroare la salvarea rezultatului", e);
                });
        }
    }
    
    /**
     * Update Crișana-specific achievements
     */
    private void updateCrisanaAchievements() {
        if (achievementManager == null) return;
        
        int totalQuestions = enhancedQuestions != null ? enhancedQuestions.size() : questions.size();
        float accuracy = totalQuestions > 0 ? ((float) correctAnswers / totalQuestions) * 100 : 0;
        long averageTimePerQuestion = totalQuestions > 0 ? totalTime / totalQuestions : 0;
        
        // Crișana-specific achievements
        achievementManager.checkCrisanaMasterAchievement(correctAnswers, totalQuestions);
        achievementManager.checkCrisanaExpertAchievement(accuracy);
        achievementManager.checkCrisanaExplorerAchievement(score);
        achievementManager.checkCrisanaSpeedsterAchievement(averageTimePerQuestion);
        achievementManager.checkCrisanaStreakAchievement(maxStreak);
        achievementManager.checkCrisanaPerfectAchievement(correctAnswers, totalQuestions);
        achievementManager.checkCrisanaConsistencyAchievement(accuracy);
        achievementManager.checkCrisanaDedicationAchievement(totalQuestions);
        
        Log.d(TAG, "🏆 Crișana achievements updated");
    }
    
    /**
     * Save game result to Firestore leaderboard with enhanced statistics
     */
    private void saveGameResultToLeaderboard(int finalScore, int correctAnswers, int totalQuestions, 
                                           long averageTimePerQuestion, float accuracy) {
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
        result.put("averageTimePerQuestion", averageTimePerQuestion);
        result.put("maxStreak", maxStreak);
        result.put("region", "crisana");
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
        profileUpdate.put("lastGameRegion", "crisana");
        profileUpdate.put("lastGameTimestamp", FieldValue.serverTimestamp());
        profileUpdate.put("gamesPlayed", FieldValue.increment(1));
        
        // Update best score if this is better
        db.collection("user_profiles").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long currentBestScore = documentSnapshot.getLong("bestScoreCrisana");
                        if (currentBestScore == null || score > currentBestScore) {
                            profileUpdate.put("bestScoreCrisana", score);
                        }
                    } else {
                        profileUpdate.put("bestScoreCrisana", score);
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
        activity.put("region", "crisana");
        activity.put("score", score);
        activity.put("accuracy", accuracy);
        activity.put("timestamp", FieldValue.serverTimestamp());
        activity.put("correctAnswers", correctAnswers);
        activity.put("totalQuestions", questions != null ? questions.size() : 0);
        
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

    private String getAchievements() {
        List<String> achievements = new ArrayList<>();
        
        // Calculate accuracy with null check
        int questionsSize = questions != null ? questions.size() : 0;
        int percentage = questionsSize > 0 ? (correctAnswers * 100) / questionsSize : 0;
        
        // Achievement for accuracy
        if (percentage == 100) {
            achievements.add("Maestru al Crișanei (Toate răspunsurile corecte)");
        } else if (percentage >= 80) {
            achievements.add("Expert al Crișanei (" + correctAnswers + " din " + questionsSize + " corecte)");
        } else if (percentage >= 60) {
            achievements.add("Bun cunoscător al Crișanei (" + correctAnswers + " din " + questionsSize + " corecte)");
        }
        
        // Achievement for streak
        if (maxStreak >= 5) {
            achievements.add("Neînvins! Serie de " + maxStreak + " răspunsuri corecte consecutive");
        } else if (maxStreak >= 3) {
            achievements.add("Cărturar! Serie de " + maxStreak + " răspunsuri corecte consecutive");
        }
        
        // Achievement for score
        if (score >= questionsSize * 25) {
            achievements.add("Scor excepțional: " + score + " puncte");
        }
        
        if (achievements.isEmpty()) {
            return "Continuă să explorezi Crișana pentru a obține realizări!";
        }
        
        StringBuilder result = new StringBuilder();
        for (String achievement : achievements) {
            result.append("• ").append(achievement).append("\n");
        }
        
        return result.toString();
    }

    // --- HYBRID SYSTEM ENTRYPOINT ---
    private void checkUserPreferenceAndLoadHybrid() {
        showInitialSetupDialog();
    }

    // --- DIALOG INIȚIAL PENTRU SURSA ȘI NUMĂRUL DE ÎNTREBĂRI ---
    private void showInitialSetupDialog() {
        boolean hasInternet = syncManager.isInternetAvailable();
        boolean hasLocalCache = checkIfLocalCacheExists();
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
        // Dialog custom cu două alegeri
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_quiz_setup, null);
        Spinner sourceSpinner = dialogView.findViewById(R.id.sourceSpinner);
        Spinner numQuestionsSpinner = dialogView.findViewById(R.id.numQuestionsSpinner);
        ArrayAdapter<String> sourceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sources);
        sourceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sourceSpinner.setAdapter(sourceAdapter);
        ArrayAdapter<Integer> numAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, toIntegerList(numQuestionsOptions));
        numAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        numQuestionsSpinner.setAdapter(numAdapter);
        // Dialog
        new MaterialAlertDialogBuilder(this)
            .setTitle("Setări quiz")
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton("Start", (dialog, which) -> {
                int sourceIndex = sourceSpinner.getSelectedItemPosition();
                String selectedSource = sources[sourceIndex];
                int numQuestions = (Integer) numQuestionsSpinner.getSelectedItem();
                // Setează preferința sursei
                if (selectedSource.contains("Baza de Date")) {
                    saveDataSourcePreference("always_database");
                } else if (selectedSource.contains("Cache Local")) {
                    saveDataSourcePreference("always_cache");
                } else if (selectedSource.contains("Automat")) {
                    saveDataSourcePreference("auto");
                }
                // Salvează preferința pentru numărul de întrebări (în sharedPreferences sau ca field)
                sharedPreferences.edit().putInt("quiz_num_questions", numQuestions).apply();
                // Continuă cu logica hibridă
                continueHybridLoadWithNumQuestions(numQuestions);
            })
            .setNegativeButton("Anulează", (dialog, which) -> finish())
            .show();
    }
    private List<Integer> toIntegerList(int[] arr) {
        List<Integer> list = new ArrayList<>();
        for (int v : arr) list.add(v);
        return list;
    }
    private void continueHybridLoadWithNumQuestions(int numQuestions) {
        // Folosește logica hibridă, dar limitează la numQuestions după încărcare
        boolean hasInternet = syncManager.isInternetAvailable();
        boolean hasLocalCache = checkIfLocalCacheExists();
        switch (dataSourcePreference) {
            case "always_database":
                if (hasInternet) {
                    loadQuestionsFromDatabaseHybrid(numQuestions);
                } else {
                    showNoInternetForPreferredDatabaseDialog();
                }
                break;
            case "always_cache":
                if (hasLocalCache) {
                    loadQuestionsFromLocalCacheHybrid(numQuestions);
                } else {
                    showNoCacheForPreferredLocalDialog();
                }
                break;
            case "auto":
                if (hasInternet) {
                    loadQuestionsFromDatabaseHybrid(numQuestions);
                } else if (hasLocalCache) {
                    loadQuestionsFromLocalCacheHybrid(numQuestions);
                } else {
                    showDataSourceSelectionDialog();
                }
                break;
            case "ask_every_time":
            default:
                showDataSourceSelectionDialog();
                break;
        }
    }
    // Suprascriu metodele de încărcare pentru a limita la numQuestions
    private void loadQuestionsFromDatabaseHybrid(int numQuestions) {
        timeProgressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "🌐 Încărcăm din baza de date pentru " + REGION + " (hybrid)");
        if (!syncManager.isInternetAvailable()) {
            timeProgressBar.setVisibility(View.GONE);
            showNoInternetDialog();
            return;
        }
        questionRepository.getQuestions(REGION, GAME_TYPE)
            .addOnSuccessListener(querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    List<QuestionModel> loadedQuestions = new ArrayList<>();
                    for (int i = 0; i < querySnapshot.size(); i++) {
                        try {
                            Map<String, Object> data = querySnapshot.getDocuments().get(i).getData();
                            if (data != null) {
                                String question = (String) data.get("question");
                                String correctAnswer = (String) data.get("correctAnswer");
                                @SuppressWarnings("unchecked")
                                List<String> incorrectAnswers = (List<String>) data.get("incorrectAnswers");
                                if (incorrectAnswers == null) incorrectAnswers = new ArrayList<>();
                                String fact = data.get("fact") != null ? (String) data.get("fact") : "";
                                List<String> options = new ArrayList<>();
                                options.add(correctAnswer);
                                options.addAll(incorrectAnswers);
                                Collections.shuffle(options);
                                int correctIndex = options.indexOf(correctAnswer);
                                QuestionModel q = new QuestionModel(question, correctAnswer, incorrectAnswers, 0, fact);
                                loadedQuestions.add(q);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "❌ Eroare la procesarea întrebării", e);
                        }
                    }
                    if (!loadedQuestions.isEmpty()) {
                        // Limitează la numQuestions
                        if (loadedQuestions.size() > numQuestions) {
                            Collections.shuffle(loadedQuestions);
                            loadedQuestions = loadedQuestions.subList(0, numQuestions);
                        }
                        firestoreQuestions = loadedQuestions;
                        enhancedQuestions = convertToEnhancedQuestions(loadedQuestions);
                        convertFirestoreToLocalQuestions(loadedQuestions);
                        saveQuestionsToLocalCacheHybrid(loadedQuestions);
                        isDataLoaded = true;
                        displayQuestion();
                        startTimer();
                        Toast.makeText(this, "🌐 Întrebări încărcate din baza de date!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                Log.w(TAG, "⚠️ Nu s-au găsit întrebări în baza de date");
                loadQuestionsFromLocalCacheHybrid(numQuestions);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ Eroare la încărcarea din baza de date", e);
                loadQuestionsFromLocalCacheHybrid(numQuestions);
            });
    }
    private void loadQuestionsFromLocalCacheHybrid(int numQuestions) {
        Log.d(TAG, "💾 Încercăm să încărcăm din cache local (hybrid)");
        String cachedJson = sharedPreferences.getString(CACHE_KEY, null);
        if (cachedJson != null && !cachedJson.isEmpty()) {
            try {
                Gson gson = new Gson();
                Type type = new TypeToken<List<QuestionModel>>(){}.getType();
                List<QuestionModel> cachedQuestions = gson.fromJson(cachedJson, type);
                if (cachedQuestions != null && !cachedQuestions.isEmpty()) {
                    // Limitează la numQuestions
                    if (cachedQuestions.size() > numQuestions) {
                        Collections.shuffle(cachedQuestions);
                        cachedQuestions = cachedQuestions.subList(0, numQuestions);
                    }
                    firestoreQuestions = cachedQuestions;
                    enhancedQuestions = convertToEnhancedQuestions(cachedQuestions);
                    convertFirestoreToLocalQuestions(cachedQuestions);
                    isDataLoaded = true;
                    displayQuestion();
                    startTimer();
                    Toast.makeText(this, "📱 Întrebări încărcate din cache local!", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (Exception e) {
                Log.e(TAG, "Eroare la parsing cache local", e);
            }
        }
        Toast.makeText(this, "❌ Nu există întrebări valide în cache local!", Toast.LENGTH_LONG).show();
        showNoQuestionsError();
    }

    // --- SALVARE HIBRIDĂ REZULTAT QUIZ ---
    private void saveQuizResultToHybridStorage(int finalScore, int correctAnswers, int totalQuestions, long totalTime, float accuracy) {
        Map<String, Object> quizResultData = new HashMap<>();
        quizResultData.put("score", finalScore);
        quizResultData.put("correctAnswers", correctAnswers);
        quizResultData.put("totalQuestions", totalQuestions);
        quizResultData.put("maxStreak", maxStreak);
        quizResultData.put("totalTime", totalTime);
        quizResultData.put("accuracy", accuracy);
        quizResultData.put("region", REGION);
        quizResultData.put("gameType", GAME_TYPE);
        quizResultData.put("completedAt", System.currentTimeMillis());
        if (difficultyManager != null) {
            quizResultData.put("difficulty", difficultyManager.getCurrentDifficulty().name());
        }
        if (gameModeManager != null && gameModeManager.getCurrentGameMode() != null) {
            quizResultData.put("gameMode", gameModeManager.getCurrentGameMode().name());
        }
        String quizId = REGION + "_quiz_" + System.currentTimeMillis();
        if (syncManager != null) {
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
        }
    }

    // --- METODELE LIPSĂ ---
    
    /**
     * Verifică dacă există cache local valid
     */
    private boolean checkIfLocalCacheExists() {
        String cachedJson = sharedPreferences.getString(CACHE_KEY, null);
        long timestamp = sharedPreferences.getLong(CACHE_TIMESTAMP_KEY, 0);
        boolean notExpired = (System.currentTimeMillis() - timestamp) < CACHE_EXPIRY_MS;
        if (cachedJson != null && !cachedJson.isEmpty() && notExpired) {
            try {
                Gson gson = new Gson();
                Type type = new TypeToken<List<QuestionModel>>(){}.getType();
                List<QuestionModel> cachedQuestions = gson.fromJson(cachedJson, type);
                return cachedQuestions != null && !cachedQuestions.isEmpty();
            } catch (Exception e) {
                Log.e(TAG, "Eroare la parsing cache local", e);
            }
        }
        return false;
    }
    
    /**
     * Salvează preferința sursei de date
     */
    private void saveDataSourcePreference(String pref) {
        dataSourcePreference = pref;
        sharedPreferences.edit().putString(DATA_SOURCE_PREF_KEY, pref).apply();
    }
    
    /**
     * Dialog când utilizatorul preferă baza de date dar nu există internet
     */
    private void showNoInternetForPreferredDatabaseDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("❌ Nu există conexiune la internet")
            .setMessage("Preferința este baza de date, dar nu există conexiune. Încercați cache local?")
            .setPositiveButton("Cache Local", (dialog, which) -> {
                int numQuestions = sharedPreferences.getInt("quiz_num_questions", 10);
                loadQuestionsFromLocalCacheHybrid(numQuestions);
            })
            .setNegativeButton("Închide", (dialog, which) -> finish())
            .show();
    }
    
    /**
     * Dialog când utilizatorul preferă cache local dar nu există
     */
    private void showNoCacheForPreferredLocalDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("❌ Nu există cache local")
            .setMessage("Preferința este cache local, dar nu există date salvate. Încercați baza de date?")
            .setPositiveButton("Baza de date", (dialog, which) -> {
                int numQuestions = sharedPreferences.getInt("quiz_num_questions", 10);
                loadQuestionsFromDatabaseHybrid(numQuestions);
            })
            .setNegativeButton("Închide", (dialog, which) -> finish())
            .show();
    }
    
    /**
     * Dialog pentru selectarea sursei de date
     */
    private void showDataSourceSelectionDialog() {
        boolean hasInternet = syncManager.isInternetAvailable();
        boolean hasLocalCache = checkIfLocalCacheExists();
        int numQuestions = sharedPreferences.getInt("quiz_num_questions", 10);
        
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);
        dialogBuilder.setTitle("📚 Alegeți sursa întrebărilor");
        dialogBuilder.setCancelable(false);
        
        if (hasInternet && hasLocalCache) {
            dialogBuilder.setMessage("📊 Ambele surse sunt disponibile!\n\n🌐 Baza de Date: Întrebări actualizate\n📱 Cache Local: Încărcare rapidă\n🎯 Automat: Alege cel mai bun\n\nCe preferați?");
            dialogBuilder.setPositiveButton("🌐 Baza de Date", (dialog, which) -> {
                saveDataSourcePreference("always_database");
                loadQuestionsFromDatabaseHybrid(numQuestions);
            });
            dialogBuilder.setNegativeButton("📱 Cache Local", (dialog, which) -> {
                saveDataSourcePreference("always_cache");
                loadQuestionsFromLocalCacheHybrid(numQuestions);
            });
            dialogBuilder.setNeutralButton("🎯 Automat", (dialog, which) -> {
                saveDataSourcePreference("auto");
                continueHybridLoadWithNumQuestions(numQuestions);
            });
        } else if (hasInternet) {
            dialogBuilder.setMessage("🌐 Doar conexiune la internet disponibilă. Încărcăm din baza de date?");
            dialogBuilder.setPositiveButton("OK", (dialog, which) -> {
                saveDataSourcePreference("always_database");
                loadQuestionsFromDatabaseHybrid(numQuestions);
            });
        } else if (hasLocalCache) {
            dialogBuilder.setMessage("📱 Doar cache local disponibil. Încărcăm din cache?");
            dialogBuilder.setPositiveButton("OK", (dialog, which) -> {
                saveDataSourcePreference("always_cache");
                loadQuestionsFromLocalCacheHybrid(numQuestions);
            });
        } else {
            dialogBuilder.setMessage("❌ Nicio sursă disponibilă. Conectați-vă la internet sau jucați după ce ați descărcat întrebările.");
            dialogBuilder.setPositiveButton("Închide", (dialog, which) -> finish());
        }
        
        dialogBuilder.show();
    }
    
    /**
     * Salvează întrebările în cache local
     */
    private void saveQuestionsToLocalCacheHybrid(List<QuestionModel> questions) {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(questions);
            
            sharedPreferences.edit()
                .putString(CACHE_KEY, json)
                .putLong(CACHE_TIMESTAMP_KEY, System.currentTimeMillis())
                .apply();
            
            // Sincronizare cu SyncManager (opțional)
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