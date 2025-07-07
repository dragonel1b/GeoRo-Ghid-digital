package com.example.myapplication.dobrogeausage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Manager pentru sistemul de dificultate adaptiv al quiz-ului Dobrogea
 * Tematică specifică: Marea Neagră, Delta Dunării, navigație
 */
public class DifficultyManager {
    private static final String TAG = "DobrogeaDifficultyManager";
    private static final String PREFS_NAME = "DobrogeaDifficulty";
    private static final String KEY_CURRENT_DIFFICULTY = "current_difficulty";
    private static final String KEY_TOTAL_GAMES = "total_games";
    private static final String KEY_TOTAL_CORRECT = "total_correct";
    private static final String KEY_AVERAGE_TIME = "average_time";
    
    public enum DifficultyLevel {
        SAILOR("Marinar Începător", 45000, 1.2f, 3, "⚓", "45s per întrebare - perfect pentru a învăța"),
        FISHERMAN("Pescar Experimentat", 30000, 1.0f, 3, "🎣", "30s per întrebare - cunoștințe de bază"),
        NAVIGATOR("Navigator Abil", 20000, 1.3f, 2, "🧭", "20s per întrebare - navigație avansată"),
        CAPTAIN("Căpitan Marin", 15000, 1.6f, 1, "👨‍✈️", "15s per întrebare - expertiza unui căpitan"),
        ADMIRAL("Amiral al Mării", 10000, 2.0f, 0, "⭐", "10s per întrebare - maestria absolută");
        
        public final String displayName;
        public final int timePerQuestion;
        public final float pointsMultiplier;
        public final int availableLifelines;
        public final String emoji;
        public final String description;
        
        DifficultyLevel(String displayName, int timePerQuestion, float pointsMultiplier, 
                       int availableLifelines, String emoji, String description) {
            this.displayName = displayName;
            this.timePerQuestion = timePerQuestion;
            this.pointsMultiplier = pointsMultiplier;
            this.availableLifelines = availableLifelines;
            this.emoji = emoji;
            this.description = description;
        }
    }
    
    private Context context;
    private SharedPreferences prefs;
    private DifficultyLevel currentDifficulty;
    
    public DifficultyManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.currentDifficulty = DifficultyLevel.valueOf(
            prefs.getString(KEY_CURRENT_DIFFICULTY, DifficultyLevel.FISHERMAN.name())
        );
    }
    
    /**
     * Calculează nivelul de dificultate bazat pe performanța utilizatorului
     * Algoritm adaptat pentru tema marină
     */
    public DifficultyLevel calculateAdaptiveDifficulty(int correctAnswers, int totalQuestions, 
                                                      long averageTimePerQuestion) {
        if (totalQuestions < 5) {
            return DifficultyLevel.FISHERMAN; // Păstrăm nivelul de pescar pentru începători
        }
        
        float accuracy = (float) correctAnswers / totalQuestions;
        float timeRatio = (float) averageTimePerQuestion / 30000; // Raport față de 30s
        
        Log.d(TAG, "Calculating maritime difficulty - Accuracy: " + accuracy + ", Time ratio: " + timeRatio);
        
        // Algoritm de adaptare cu tematică marină
        if (accuracy >= 0.95f && timeRatio <= 0.4f) {
            return DifficultyLevel.ADMIRAL; // Maestria unui amiral
        } else if (accuracy >= 0.88f && timeRatio <= 0.6f) {
            return DifficultyLevel.CAPTAIN; // Expertiza unui căpitan
        } else if (accuracy >= 0.75f && timeRatio <= 0.75f) {
            return DifficultyLevel.NAVIGATOR; // Abilitatea unui navigator
        } else if (accuracy >= 0.60f) {
            return DifficultyLevel.FISHERMAN; // Experiența unui pescar
        } else {
            return DifficultyLevel.SAILOR; // Nivelul unui marinar începător
        }
    }
    
    /**
     * Actualizează dificultatea după un quiz completat
     */
    public void updateDifficultyAfterGame(int correctAnswers, int totalQuestions, 
                                         long totalTimeSpent) {
        // Actualizăm statisticile globale
        int totalGames = prefs.getInt(KEY_TOTAL_GAMES, 0) + 1;
        int totalCorrect = prefs.getInt(KEY_TOTAL_CORRECT, 0) + correctAnswers;
        long averageTime = calculateAverageTime(totalTimeSpent, totalQuestions);
        
        // Calculăm noua dificultate
        DifficultyLevel newDifficulty = calculateAdaptiveDifficulty(
            totalCorrect, totalGames * 10, averageTime);
        
        // Salvăm în SharedPreferences
        prefs.edit()
            .putString(KEY_CURRENT_DIFFICULTY, newDifficulty.name())
            .putInt(KEY_TOTAL_GAMES, totalGames)
            .putInt(KEY_TOTAL_CORRECT, totalCorrect)
            .putLong(KEY_AVERAGE_TIME, averageTime)
            .apply();
            
        currentDifficulty = newDifficulty;
        
        Log.d(TAG, "Maritime difficulty updated to: " + newDifficulty.displayName);
    }
    
    /**
     * Calculează timpul mediu per întrebare
     */
    private long calculateAverageTime(long totalTimeSpent, int totalQuestions) {
        if (totalQuestions == 0) return 30000;
        return totalTimeSpent / totalQuestions;
    }
    
    /**
     * Obține dificultatea curentă
     */
    public DifficultyLevel getCurrentDifficulty() {
        return currentDifficulty;
    }
    
    /**
     * Setează manual dificultatea (pentru setări utilizator)
     */
    public void setManualDifficulty(DifficultyLevel difficulty) {
        this.currentDifficulty = difficulty;
        prefs.edit().putString(KEY_CURRENT_DIFFICULTY, difficulty.name()).apply();
    }
    
    /**
     * Verifică dacă utilizatorul poate folosi toate lifeline-urile
     */
    public boolean canUseLifeline(int lifelinesUsed) {
        return lifelinesUsed < currentDifficulty.availableLifelines;
    }
    
    /**
     * Calculează punctajul final cu multiplicatorul de dificultate
     */
    public int calculateFinalScore(int baseScore) {
        return Math.round(baseScore * currentDifficulty.pointsMultiplier);
    }
    
    /**
     * Obține recomandări pentru utilizator bazate pe performanță
     * Adaptate pentru tema marină a Dobrogei
     */
    public String getPerformanceRecommendation() {
        int totalGames = prefs.getInt(KEY_TOTAL_GAMES, 0);
        int totalCorrect = prefs.getInt(KEY_TOTAL_CORRECT, 0);
        
        if (totalGames == 0) {
            return "🌊 Bine ai venit pe litoralul cunoștințelor! Începe cu nivelul Pescar și explorează tainele Dobrogei.";
        }
        
        float overallAccuracy = (float) totalCorrect / (totalGames * 10);
        
        if (overallAccuracy >= 0.9f) {
            return "⭐ Performanță de amiral! Cunoștințele tale despre Dobrogea sunt impresionante. Încearcă provocările expert!";
        } else if (overallAccuracy >= 0.8f) {
            return "👨‍✈️ Navighezi excelent prin cunoștințe! Ești gata să devii căpitan al quiz-ului Dobrogea.";
        } else if (overallAccuracy >= 0.7f) {
            return "🧭 Performanță bună de navigator! Continuă să explorezi pentru a-ți perfecționa cunoștințele maritime.";
        } else if (overallAccuracy >= 0.6f) {
            return "🎣 Ca un pescar experimentat, îți dezvolți abilitățile. Concentrează-te pe Delta Dunării și istoria antică.";
        } else {
            return "⚓ Fii ca un marinar perseverent! Explorează mai mult litoralul și delta pentru a-ți îmbunătăți cunoștințele.";
        }
    }
    
    /**
     * Obține sfaturi specifice pentru îmbunătățirea performanței
     */
    public String getImprovementTips() {
        float overallAccuracy = getCurrentAccuracy();
        
        if (overallAccuracy < 0.6f) {
            return "💡 Sfaturi pentru îmbunătățire:\n" +
                   "• Studiază mai mult despre Delta Dunării și biodiversitatea ei\n" +
                   "• Explorează istoria antică a Dobrogei (Tomis, Histria)\n" +
                   "• Învață despre porturile și navigația pe Marea Neagră\n" +
                   "• Folosește lifeline-urile pentru a învăța din greșeli";
        } else if (overallAccuracy < 0.8f) {
            return "🎯 Pentru a deveni căpitan:\n" +
                   "• Aprofundează cunoștințele despre arheologia dobrogeanǎ\n" +
                   "• Studiază tradițiile pescarilor și cultura locală\n" +
                   "• Încearcă modurile tematice (Maritime, Delta Explorer)\n" +
                   "• Exersează cu întrebări de dificultate crescută";
        } else {
            return "⭐ Pentru maestria de amiral:\n" +
                   "• Participă la provocări expert și fără lifeline-uri\n" +
                   "• Explorează detalii fine despre civilizațiile antice\n" +
                   "• Încearcă modurile extreme (Storm Survival, Captain Expert)\n" +
                   "• Îmbunătățește viteza de răspuns păstrând acuratețea";
        }
    }
    
    /**
     * Calculează acuratețea curentă
     */
    private float getCurrentAccuracy() {
        int totalGames = prefs.getInt(KEY_TOTAL_GAMES, 0);
        int totalCorrect = prefs.getInt(KEY_TOTAL_CORRECT, 0);
        
        if (totalGames == 0) return 0.0f;
        return (float) totalCorrect / (totalGames * 10);
    }
    
    /**
     * Obține progresul către următorul nivel
     */
    public String getProgressToNextLevel() {
        float accuracy = getCurrentAccuracy();
        DifficultyLevel nextLevel = getNextLevel();
        
        if (nextLevel == null) {
            return "🏆 Ai atins maestria de amiral! Felicitări!";
        }
        
        float requiredAccuracy = getRequiredAccuracyForLevel(nextLevel);
        float progress = (accuracy / requiredAccuracy) * 100;
        
        return String.format("📈 Progres către %s: %.1f%% (necesari %.1f%% acuratețe)", 
                           nextLevel.displayName, Math.min(progress, 100.0f), requiredAccuracy * 100);
    }
    
    /**
     * Obține următorul nivel de dificultate
     */
    private DifficultyLevel getNextLevel() {
        switch (currentDifficulty) {
            case SAILOR: return DifficultyLevel.FISHERMAN;
            case FISHERMAN: return DifficultyLevel.NAVIGATOR;
            case NAVIGATOR: return DifficultyLevel.CAPTAIN;
            case CAPTAIN: return DifficultyLevel.ADMIRAL;
            case ADMIRAL: return null;
            default: return DifficultyLevel.FISHERMAN;
        }
    }
    
    /**
     * Obține acuratețea necesară pentru un nivel
     */
    private float getRequiredAccuracyForLevel(DifficultyLevel level) {
        switch (level) {
            case FISHERMAN: return 0.6f;
            case NAVIGATOR: return 0.75f;
            case CAPTAIN: return 0.88f;
            case ADMIRAL: return 0.95f;
            default: return 0.6f;
        }
    }
    
    /**
     * Resetează statisticile de dificultate
     */
    public void resetDifficultyStats() {
        prefs.edit().clear().apply();
        currentDifficulty = DifficultyLevel.FISHERMAN;
    }
    
    /**
     * Obține statistici detaliate
     */
    public String getDetailedStats() {
        int totalGames = prefs.getInt(KEY_TOTAL_GAMES, 0);
        int totalCorrect = prefs.getInt(KEY_TOTAL_CORRECT, 0);
        long averageTime = prefs.getLong(KEY_AVERAGE_TIME, 30000);
        
        StringBuilder stats = new StringBuilder();
        stats.append("🌊 Statistici Maritime Dobrogea:\n\n");
        stats.append("🎯 Nivel curent: ").append(currentDifficulty.emoji).append(" ")
             .append(currentDifficulty.displayName).append("\n");
        stats.append("🎮 Jocuri jucate: ").append(totalGames).append("\n");
        stats.append("✅ Răspunsuri corecte: ").append(totalCorrect).append("\n");
        stats.append("📊 Acuratețe: ").append(String.format("%.1f%%", getCurrentAccuracy() * 100)).append("\n");
        stats.append("⏱️ Timp mediu: ").append(String.format("%.1fs", averageTime / 1000.0)).append("\n");
        stats.append("🔥 Multiplicator puncte: ").append(String.format("%.1fx", currentDifficulty.pointsMultiplier)).append("\n");
        stats.append("🆘 Lifeline-uri: ").append(currentDifficulty.availableLifelines);
        
        return stats.toString();
    }
} 