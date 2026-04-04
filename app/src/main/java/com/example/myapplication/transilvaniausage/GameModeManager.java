package com.example.myapplication.transilvaniausage;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.myapplication.core.domain.model.EnhancedQuestionModel;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Manager pentru diferite moduri de joc în quiz-ul Transilvania
 */
public class GameModeManager {
    
    public enum GameMode {
        CLASSIC("Quiz Clasic", 10, 30, false, "🎯", 
               "Joc standard cu 10 întrebări și 30 secunde per întrebare"),
        
        LIGHTNING("Lightning Round", 20, 15, false, "⚡", 
                 "Răspunde rapid la 20 întrebări în 15 secunde fiecare"),
        
        MARATHON("Maraton", 50, 45, false, "🏃", 
                "Test de rezistență cu 50 întrebări și 45 secunde per întrebare"),
        
        SURVIVAL("Supraviețuire", -1, 20, true, "💀", 
                "Continuă până la prima greșeală - o singură viață!"),
        
        TIMED_CHALLENGE("Provocare Cronometrată", 15, 10, false, "⏰", 
                       "Cursă contra cronometru - 15 întrebări în 10 secunde"),
        
        CATEGORY_FOCUS("Focus Categorie", 15, 30, false, "📚", 
                      "Concentrează-te pe o singură categorie de întrebări"),
        
        MIXED_DIFFICULTY("Dificultate Mixtă", 12, 25, false, "🎲", 
                        "Întrebări de toate nivelurile într-un singur quiz"),
        
        BLITZ("Blitz", 30, 8, false, "💨", 
              "Super rapid - 30 întrebări în 8 secunde fiecare!"),
        
        EXPERT_CHALLENGE("Provocarea Expertului", 8, 60, false, "🎓", 
                        "Doar întrebări expert cu timp extins pentru gândire");
        
        public final String displayName;
        public final int questionCount; // -1 pentru unlimited
        public final int timePerQuestion; // secunde
        public final boolean isEliminationMode;
        public final String emoji;
        public final String description;
        
        GameMode(String displayName, int questionCount, int timePerQuestion, 
                boolean isEliminationMode, String emoji, String description) {
            this.displayName = displayName;
            this.questionCount = questionCount;
            this.timePerQuestion = timePerQuestion;
            this.isEliminationMode = isEliminationMode;
            this.emoji = emoji;
            this.description = description;
        }
    }
    
    private Context context;
    private SharedPreferences prefs;
    private GameMode currentGameMode;
    private EnhancedQuestionModel.Category focusCategory;
    private int currentQuestionIndex;
    private boolean isGameActive;
    
    public GameModeManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences("GameModePrefs", Context.MODE_PRIVATE);
        this.currentGameMode = GameMode.CLASSIC; // Default
        this.currentQuestionIndex = 0;
        this.isGameActive = false;
    }
    
    /**
     * Inițializează un mod de joc specific
     */
    public void initializeGameMode(GameMode mode, EnhancedQuestionModel.Category category) {
        this.currentGameMode = mode;
        this.focusCategory = category;
        this.currentQuestionIndex = 0;
        this.isGameActive = true;
        
        // Salvează modul selectat
        prefs.edit()
            .putString("last_game_mode", mode.name())
            .putString("focus_category", category != null ? category.name() : "")
            .apply();
    }
    
    /**
     * Filtrează întrebările bazat pe modul de joc curent
     */
    public List<EnhancedQuestionModel> filterQuestionsForGameMode(
            List<EnhancedQuestionModel> allQuestions) {
        
        List<EnhancedQuestionModel> filteredQuestions = new ArrayList<>(allQuestions);
        
        switch (currentGameMode) {
            case CATEGORY_FOCUS:
                if (focusCategory != null) {
                    filteredQuestions = filteredQuestions.stream()
                        .filter(q -> q.getCategory() == focusCategory)
                        .collect(Collectors.toList());
                }
                break;
                
            case EXPERT_CHALLENGE:
                filteredQuestions = filteredQuestions.stream()
                    .filter(q -> q.getDifficulty() == EnhancedQuestionModel.Difficulty.EXPERT)
                    .collect(Collectors.toList());
                break;
                
            case LIGHTNING:
            case BLITZ:
                // Prioritizează întrebări mai ușoare pentru modurile rapide
                filteredQuestions = filteredQuestions.stream()
                    .filter(q -> q.getDifficulty() == EnhancedQuestionModel.Difficulty.EASY ||
                               q.getDifficulty() == EnhancedQuestionModel.Difficulty.MEDIUM)
                    .collect(Collectors.toList());
                break;
                
            case MIXED_DIFFICULTY:
                // Asigură o distribuție echilibrată de dificultăți
                filteredQuestions = balanceDifficultyDistribution(filteredQuestions);
                break;
                
            default:
                // Pentru alte moduri, folosește toate întrebările
                break;
        }
        
        // Amestecă întrebările
        Collections.shuffle(filteredQuestions);
        
        // Limitează numărul bazat pe modul de joc
        if (currentGameMode.questionCount > 0 && 
            filteredQuestions.size() > currentGameMode.questionCount) {
            filteredQuestions = filteredQuestions.subList(0, currentGameMode.questionCount);
        }
        
        return filteredQuestions;
    }
    
    /**
     * Balansează distribuția dificultăților pentru modul mixt
     */
    private List<EnhancedQuestionModel> balanceDifficultyDistribution(
            List<EnhancedQuestionModel> questions) {
        
        List<EnhancedQuestionModel> balanced = new ArrayList<>();
        
        // Grupează întrebările pe dificultăți
        List<EnhancedQuestionModel> easy = questions.stream()
            .filter(q -> q.getDifficulty() == EnhancedQuestionModel.Difficulty.EASY)
            .collect(Collectors.toList());
        List<EnhancedQuestionModel> medium = questions.stream()
            .filter(q -> q.getDifficulty() == EnhancedQuestionModel.Difficulty.MEDIUM)
            .collect(Collectors.toList());
        List<EnhancedQuestionModel> hard = questions.stream()
            .filter(q -> q.getDifficulty() == EnhancedQuestionModel.Difficulty.HARD)
            .collect(Collectors.toList());
        List<EnhancedQuestionModel> expert = questions.stream()
            .filter(q -> q.getDifficulty() == EnhancedQuestionModel.Difficulty.EXPERT)
            .collect(Collectors.toList());
        
        // Adaugă în proporții echilibrate (30% easy, 30% medium, 25% hard, 15% expert)
        int targetCount = currentGameMode.questionCount > 0 ? currentGameMode.questionCount : 12;
        
        Collections.shuffle(easy);
        Collections.shuffle(medium);
        Collections.shuffle(hard);
        Collections.shuffle(expert);
        
        // Calculează câte întrebări din fiecare dificultate
        int easyCount = Math.min(easy.size(), (int)(targetCount * 0.3));
        int mediumCount = Math.min(medium.size(), (int)(targetCount * 0.3));
        int hardCount = Math.min(hard.size(), (int)(targetCount * 0.25));
        int expertCount = Math.min(expert.size(), (int)(targetCount * 0.15));
        
        // Adaugă întrebările
        balanced.addAll(easy.subList(0, easyCount));
        balanced.addAll(medium.subList(0, mediumCount));
        balanced.addAll(hard.subList(0, hardCount));
        balanced.addAll(expert.subList(0, expertCount));
        
        return balanced;
    }
    
    /**
     * Verifică dacă jocul trebuie să se termine bazat pe mod
     */
    public boolean shouldEndGame(boolean lastAnswerCorrect, int wrongAnswers) {
        switch (currentGameMode) {
            case SURVIVAL:
                // În modul supraviețuire, jocul se termină la primul răspuns greșit
                return !lastAnswerCorrect;
                
            default:
                // Pentru alte moduri, verifică dacă s-au terminat întrebările
                return false;
        }
    }
    
    /**
     * Calculează punctajul bazat pe modul de joc
     */
    public int calculateModeBonus(int baseScore, boolean isCorrect, long timeSpent) {
        if (!isCorrect) return 0;
        
        int bonus = 0;
        
        switch (currentGameMode) {
            case LIGHTNING:
            case BLITZ:
                // Bonus pentru viteză în modurile rapide
                if (timeSpent < 5000) { // sub 5 secunde
                    bonus = baseScore; // 100% bonus
                } else if (timeSpent < 8000) { // sub 8 secunde
                    bonus = baseScore / 2; // 50% bonus
                }
                break;
                
            case EXPERT_CHALLENGE:
                // Bonus pentru întrebări expert
                bonus = baseScore * 2; // 200% bonus
                break;
                
            case SURVIVAL:
                // Bonus crescător pentru fiecare întrebare corectă consecutivă
                bonus = baseScore * (currentQuestionIndex / 5); // Bonus de 20% la fiecare 5 întrebări
                break;
                
            case MARATHON:
                // Bonus de perseverență
                if (currentQuestionIndex > 25) { // după jumătate din maraton
                    bonus = baseScore / 4; // 25% bonus
                }
                break;
                
            default:
                bonus = 0;
                break;
        }
        
        return bonus;
    }
    
    /**
     * Obține timpul per întrebare pentru modul curent
     */
    public int getTimePerQuestion() {
        return currentGameMode.timePerQuestion * 1000; // convertește în milisecunde
    }
    
    /**
     * Verifică dacă lifeline-urile sunt permise în modul curent
     */
    public boolean areLifelinesAllowed() {
        switch (currentGameMode) {
            case SURVIVAL:
            case BLITZ:
            case TIMED_CHALLENGE:
                return false; // Modurile de provocare nu permit lifeline-uri
            default:
                return true;
        }
    }
    
    /**
     * Obține numărul maxim de lifeline-uri pentru modul curent
     */
    public int getMaxLifelines() {
        if (!areLifelinesAllowed()) return 0;
        
        switch (currentGameMode) {
            case LIGHTNING:
                return 1; // Doar unul în lightning
            case EXPERT_CHALLENGE:
                return 2; // Două pentru expert
            case MARATHON:
                return 5; // Mai multe pentru maraton
            default:
                return 3; // Standard
        }
    }
    
    /**
     * Avansează la următoarea întrebare
     */
    public void nextQuestion() {
        currentQuestionIndex++;
    }
    
    /**
     * Verifică dacă jocul este complet
     */
    public boolean isGameComplete(int totalQuestions) {
        if (currentGameMode.questionCount == -1) {
            return false; // Modurile infinite nu se termină automat
        }
        return currentQuestionIndex >= currentGameMode.questionCount;
    }
    
    /**
     * Resetează jocul
     */
    public void resetGame() {
        currentQuestionIndex = 0;
        isGameActive = false;
    }
    
    /**
     * Obține statistici specifice modului de joc
     */
    public String getGameModeStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("Mod: ").append(currentGameMode.displayName).append("\n");
        stats.append("Întrebări: ");
        
        if (currentGameMode.questionCount == -1) {
            stats.append("Nelimitate");
        } else {
            stats.append(currentQuestionIndex).append("/").append(currentGameMode.questionCount);
        }
        
        stats.append("\nTimp per întrebare: ").append(currentGameMode.timePerQuestion).append("s");
        
        if (focusCategory != null) {
            stats.append("\nCategorie: ").append(focusCategory.displayName);
        }
        
        return stats.toString();
    }
    
    // Getters
    public GameMode getCurrentGameMode() {
        return currentGameMode;
    }
    
    public EnhancedQuestionModel.Category getFocusCategory() {
        return focusCategory;
    }
    
    public int getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }
    
    public boolean isGameActive() {
        return isGameActive;
    }
} 