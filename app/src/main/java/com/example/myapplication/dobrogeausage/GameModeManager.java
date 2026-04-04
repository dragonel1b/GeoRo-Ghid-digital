package com.example.myapplication.dobrogeausage;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.myapplication.core.domain.model.EnhancedQuestionModel;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Manager pentru diferite moduri de joc în quiz-ul Dobrogea
 * Tematică specifică: Marea Neagră, Delta Dunării, Constanța
 */
public class GameModeManager {
    
    public enum GameMode {
        CLASSIC("Quiz Clasic", 10, 30, false, "🌊", 
               "Joc standard cu 10 întrebări și 30 secunde per întrebare"),
        
        MARITIME("Aventura Marină", 15, 25, false, "⚓", 
                "Explorează tainele Mării Negre cu 15 întrebări maritime"),
        
        DELTA_EXPLORER("Exploratorul Deltei", 20, 35, false, "🦢", 
                      "Descoperă biodiversitatea Deltei Dunării - 20 întrebări"),
        
        LIGHTHOUSE_CHALLENGE("Provocarea Farului", 12, 20, false, "🗼", 
                           "Ghidează-te după farul cunoștințelor - 12 întrebări rapide"),
        
        FISHERMAN_WISDOM("Înțelepciunea Pescarului", 25, 40, false, "🎣", 
                        "Test de rezistență cu 25 întrebări despre tradiții locale"),
        
        STORM_SURVIVAL("Supraviețuirea Furtunii", -1, 15, true, "⛈️", 
                      "Rezistă furtunii cunoștințelor - o greșeală și pierzi!"),
        
        ARCHAEOLOGICAL_DIG("Săpăturile Arheologice", 18, 45, false, "🏛️", 
                          "Descoperă tezaurele antice ale Dobrogei - 18 întrebări"),
        
        SEAGULL_FLIGHT("Zborul Pescărușului", 30, 10, false, "🕊️", 
                      "Zboară rapid peste întrebări - 30 în 10 secunde fiecare!"),
        
        CAPTAIN_EXPERT("Căpitanul Expert", 8, 60, false, "👨‍✈️", 
                      "Doar pentru experți - 8 întrebări dificile cu timp extins");
        
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
        this.prefs = context.getSharedPreferences("DobrogeaGameModePrefs", Context.MODE_PRIVATE);
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
            case MARITIME:
                // Prioritizează întrebări despre mare, porturi, navigație
                filteredQuestions = filteredQuestions.stream()
                    .filter(q -> containsMaritimeKeywords(q.getQuestion()) || 
                               q.getCategory() == EnhancedQuestionModel.Category.GEOGRAPHY ||
                               q.getCategory() == EnhancedQuestionModel.Category.NATURE)
                    .collect(Collectors.toList());
                break;
                
            case DELTA_EXPLORER:
                // Focus pe biodiversitate, natură, Delta Dunării
                filteredQuestions = filteredQuestions.stream()
                    .filter(q -> containsDeltaKeywords(q.getQuestion()) ||
                               q.getCategory() == EnhancedQuestionModel.Category.NATURE ||
                               q.getCategory() == EnhancedQuestionModel.Category.GEOGRAPHY)
                    .collect(Collectors.toList());
                break;
                
            case ARCHAEOLOGICAL_DIG:
                // Focus pe istorie, arheologie, civilizații antice
                filteredQuestions = filteredQuestions.stream()
                    .filter(q -> q.getCategory() == EnhancedQuestionModel.Category.HISTORY ||
                               q.getCategory() == EnhancedQuestionModel.Category.ARCHITECTURE ||
                               containsArchaeologicalKeywords(q.getQuestion()))
                    .collect(Collectors.toList());
                break;
                
            case FISHERMAN_WISDOM:
                // Focus pe tradiții, cultură, gastronomie
                filteredQuestions = filteredQuestions.stream()
                    .filter(q -> q.getCategory() == EnhancedQuestionModel.Category.CULTURE ||
                               q.getCategory() == EnhancedQuestionModel.Category.GASTRONOMY ||
                               q.getCategory() == EnhancedQuestionModel.Category.LEGENDS)
                    .collect(Collectors.toList());
                break;
                
            case CAPTAIN_EXPERT:
                filteredQuestions = filteredQuestions.stream()
                    .filter(q -> q.getDifficulty() == EnhancedQuestionModel.Difficulty.EXPERT)
                    .collect(Collectors.toList());
                break;
                
            case SEAGULL_FLIGHT:
                // Prioritizează întrebări mai ușoare pentru modul rapid
                filteredQuestions = filteredQuestions.stream()
                    .filter(q -> q.getDifficulty() == EnhancedQuestionModel.Difficulty.EASY ||
                               q.getDifficulty() == EnhancedQuestionModel.Difficulty.MEDIUM)
                    .collect(Collectors.toList());
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
     * Verifică dacă întrebarea conține cuvinte cheie maritime
     */
    private boolean containsMaritimeKeywords(String question) {
        String[] maritimeKeywords = {
            "mare", "port", "navă", "corabie", "pescuit", "farul", "constanța", 
            "mangalia", "mamaia", "litoral", "plajă", "navigație", "vapor"
        };
        String questionLower = question.toLowerCase();
        for (String keyword : maritimeKeywords) {
            if (questionLower.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Verifică dacă întrebarea conține cuvinte cheie despre deltă
     */
    private boolean containsDeltaKeywords(String question) {
        String[] deltaKeywords = {
            "delta", "dunăre", "pelican", "egretă", "stuf", "canal", "braț", 
            "tulcea", "sulina", "sfântu gheorghe", "chilia", "lebădă", "pescăruș"
        };
        String questionLower = question.toLowerCase();
        for (String keyword : deltaKeywords) {
            if (questionLower.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Verifică dacă întrebarea conține cuvinte cheie arheologice
     */
    private boolean containsArchaeologicalKeywords(String question) {
        String[] archaeologicalKeywords = {
            "tomis", "histria", "callatis", "tropaeum", "adamclisi", "cetate", 
            "ruine", "antic", "roman", "grec", "tezaur", "săpături"
        };
        String questionLower = question.toLowerCase();
        for (String keyword : archaeologicalKeywords) {
            if (questionLower.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Verifică dacă jocul trebuie să se termine bazat pe mod
     */
    public boolean shouldEndGame(boolean lastAnswerCorrect, int wrongAnswers) {
        switch (currentGameMode) {
            case STORM_SURVIVAL:
                // În modul supraviețuire, jocul se termină la primul răspuns greșit
                return !lastAnswerCorrect;
            default:
                return false;
        }
    }
    
    /**
     * Calculează bonusul specific modului de joc
     */
    public int calculateModeBonus(int baseScore, boolean isCorrect, long timeSpent) {
        if (!isCorrect) return 0;
        
        int bonus = 0;
        switch (currentGameMode) {
            case MARITIME:
                bonus = (int)(baseScore * 0.2f); // 20% bonus pentru aventura marină
                break;
            case DELTA_EXPLORER:
                bonus = (int)(baseScore * 0.25f); // 25% bonus pentru explorarea deltei
                break;
            case STORM_SURVIVAL:
                bonus = (int)(baseScore * 0.5f); // 50% bonus pentru supraviețuire
                break;
            case SEAGULL_FLIGHT:
                // Bonus bazat pe viteză
                if (timeSpent < 5000) bonus = (int)(baseScore * 0.3f);
                else if (timeSpent < 8000) bonus = (int)(baseScore * 0.15f);
                break;
            case CAPTAIN_EXPERT:
                bonus = (int)(baseScore * 0.4f); // 40% bonus pentru expert
                break;
            default:
                bonus = (int)(baseScore * 0.1f); // 10% bonus standard
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
            case STORM_SURVIVAL:
            case CAPTAIN_EXPERT:
                return false; // Modurile dificile nu permit lifeline-uri
            default:
                return true;
        }
    }
    
    /**
     * Obține numărul maxim de lifeline-uri pentru modul curent
     */
    public int getMaxLifelines() {
        switch (currentGameMode) {
            case SEAGULL_FLIGHT:
                return 1; // Doar un lifeline pentru modul rapid
            case MARITIME:
            case DELTA_EXPLORER:
                return 2; // Două lifeline-uri pentru modurile tematice
            default:
                return 3; // Trei lifeline-uri standard
        }
    }
    
    /**
     * Trece la următoarea întrebare
     */
    public void nextQuestion() {
        currentQuestionIndex++;
    }
    
    /**
     * Verifică dacă jocul este complet
     */
    public boolean isGameComplete(int totalQuestions) {
        return currentGameMode.questionCount > 0 && 
               currentQuestionIndex >= currentGameMode.questionCount;
    }
    
    /**
     * Resetează jocul
     */
    public void resetGame() {
        currentQuestionIndex = 0;
        isGameActive = false;
    }
    
    /**
     * Obține statisticile modului de joc
     */
    public String getGameModeStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("🌊 Mod: ").append(currentGameMode.displayName).append("\n");
        stats.append("📊 Progres: ").append(currentQuestionIndex);
        if (currentGameMode.questionCount > 0) {
            stats.append("/").append(currentGameMode.questionCount);
        }
        stats.append("\n");
        stats.append("⏱️ Timp per întrebare: ").append(currentGameMode.timePerQuestion).append("s\n");
        stats.append("🎯 Lifeline-uri: ").append(areLifelinesAllowed() ? "Da" : "Nu");
        
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