package com.example.myapplication.core.domain.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 * Model pentru stocarea rezultatelor quiz-urilor în Firestore
 */
public class QuizResult {
    @DocumentId
    private String id;
    private String userId;
    private String quizId;
    private String region;
    private String gameType;
    private int score;
    private int correctAnswers;
    private int totalQuestions;
    private int timeSpentSeconds;
    private int maxStreak;
    private long totalTime; // Timpul total în milisecunde
    @ServerTimestamp
    private Date completedAt;
    private List<QuestionAnswer> answers;
    private int lifelinesUsed;
    private float accuracy;
    private java.util.Map<String, Object> metadata;
    
    /**
     * Constructor gol necesar pentru Firestore
     */
    public QuizResult() {
        // Constructor gol necesar pentru Firestore
    }
    
    /**
     * Constructor complet pentru QuizResult
     */
    public QuizResult(String userId, String quizId, String region, String gameType, 
                     int score, int correctAnswers, int totalQuestions, int timeSpentSeconds) {
        this.userId = userId;
        this.quizId = quizId;
        this.region = region;
        this.gameType = gameType;
        this.score = score;
        this.correctAnswers = correctAnswers;
        this.totalQuestions = totalQuestions;
        this.timeSpentSeconds = timeSpentSeconds;
        this.answers = new ArrayList<>();
    }
    
    /**
     * Adaugă un răspuns la lista de răspunsuri
     */
    public void addAnswer(QuestionAnswer answer) {
        if (answers == null) {
            answers = new ArrayList<>();
        }
        answers.add(answer);
    }
    
    // Getters și setters
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getQuizId() {
        return quizId;
    }

    public void setQuizId(String quizId) {
        this.quizId = quizId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getTimeSpentSeconds() {
        return timeSpentSeconds;
    }

    public void setTimeSpentSeconds(int timeSpentSeconds) {
        this.timeSpentSeconds = timeSpentSeconds;
    }
    
    public int getMaxStreak() {
        return maxStreak;
    }

    public void setMaxStreak(int maxStreak) {
        this.maxStreak = maxStreak;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }

    public List<QuestionAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<QuestionAnswer> answers) {
        this.answers = answers;
    }
    
    /**
     * Calculează acuratețea răspunsurilor (procentaj)
     */
    public float getAccuracy() {
        if (totalQuestions == 0) return 0;
        return ((float) correctAnswers / totalQuestions) * 100;
    }
    
    /**
     * Calculează timpul mediu per întrebare în secunde
     */
    public float getAverageTimePerQuestion() {
        if (totalQuestions == 0) return 0;
        return (float) timeSpentSeconds / totalQuestions;
    }

    public int getLifelinesUsed() { return lifelinesUsed; }
    public void setLifelinesUsed(int lifelinesUsed) { this.lifelinesUsed = lifelinesUsed; }

    public float getAccuracyValue() { return accuracy; }
    public void setAccuracy(float accuracy) { this.accuracy = accuracy; }

    public java.util.Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(java.util.Map<String, Object> metadata) { this.metadata = metadata; }
    
    // Additional methods for enhanced functionality
    
    /**
     * Get timestamp (alias for completedAt)
     */
    public Date getTimestamp() {
        return completedAt;
    }
    
    /**
     * Set timestamp (alias for completedAt)
     */
    public void setTimestamp(Date timestamp) {
        this.completedAt = timestamp;
    }
    
    /**
     * Get difficulty level
     */
    public String getDifficulty() {
        if (metadata != null && metadata.containsKey("difficulty")) {
            return (String) metadata.get("difficulty");
        }
        return "UNKNOWN";
    }
    
    /**
     * Set difficulty level
     */
    public void setDifficulty(String difficulty) {
        if (metadata == null) {
            metadata = new java.util.HashMap<>();
        }
        metadata.put("difficulty", difficulty);
    }
    
    /**
     * Get game mode
     */
    public String getGameMode() {
        if (metadata != null && metadata.containsKey("gameMode")) {
            return (String) metadata.get("gameMode");
        }
        return "UNKNOWN";
    }
    
    /**
     * Set game mode
     */
    public void setGameMode(String gameMode) {
        if (metadata == null) {
            metadata = new java.util.HashMap<>();
        }
        metadata.put("gameMode", gameMode);
    }
    
    /**
     * Get detailed statistics
     */
    public java.util.Map<String, Object> getDetailedStats() {
        if (metadata != null && metadata.containsKey("detailedStats")) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> stats = (java.util.Map<String, Object>) metadata.get("detailedStats");
            return stats;
        }
        return new java.util.HashMap<>();
    }
    
    /**
     * Set detailed statistics
     */
    public void setDetailedStats(java.util.Map<String, Object> detailedStats) {
        if (metadata == null) {
            metadata = new java.util.HashMap<>();
        }
        metadata.put("detailedStats", detailedStats);
    }
} 