package com.example.myapplication.model;

import java.util.Date;

/**
 * Model pentru reprezentarea unei activități din profilul utilizatorului
 * Unifica diferitele tipuri de activități (quiz-uri, povești, etc.) într-un format comun
 */
public class ActivityItem {
    private String activityType;        // "transilvania_quiz", "story", etc.
    private String displayName;         // "Quiz Transilvania", "Povestea lui Dracula"
    private String description;         // Descriere detaliată
    private int score;                 // Scorul obținut
    private float accuracy;            // Acuratețea (%)
    private int correctAnswers;        // Răspunsuri corecte
    private int totalQuestions;        // Total întrebări
    private int maxStreak;             // Cea mai lungă serie
    private String region;             // "transilvania", "muntenia", etc.
    private String gameType;           // "quiz", "story", "exploration"
    private Date completedAt;          // Data completării
    private long duration;             // Durata în milisecunde
    private String iconResource;       // Numele resursei pentru iconiță
    private String colorTheme;         // Tema de culoare
    
    // Constructor gol necesar pentru Firestore
    public ActivityItem() {
    }
    
    // Constructor complet
    public ActivityItem(String activityType, String displayName, String description, 
                       int score, float accuracy, int correctAnswers, int totalQuestions,
                       int maxStreak, String region, String gameType, Date completedAt) {
        this.activityType = activityType;
        this.displayName = displayName;
        this.description = description;
        this.score = score;
        this.accuracy = accuracy;
        this.correctAnswers = correctAnswers;
        this.totalQuestions = totalQuestions;
        this.maxStreak = maxStreak;
        this.region = region;
        this.gameType = gameType;
        this.completedAt = completedAt;
    }
    
    // Getters și Setters
    
    public String getActivityType() {
        return activityType;
    }
    
    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getScore() {
        return score;
    }
    
    public void setScore(int score) {
        this.score = score;
    }
    
    public float getAccuracy() {
        return accuracy;
    }
    
    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
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
    
    public int getMaxStreak() {
        return maxStreak;
    }
    
    public void setMaxStreak(int maxStreak) {
        this.maxStreak = maxStreak;
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
    
    public Date getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }
    
    public long getDuration() {
        return duration;
    }
    
    public void setDuration(long duration) {
        this.duration = duration;
    }
    
    public String getIconResource() {
        return iconResource;
    }
    
    public void setIconResource(String iconResource) {
        this.iconResource = iconResource;
    }
    
    public String getColorTheme() {
        return colorTheme;
    }
    
    public void setColorTheme(String colorTheme) {
        this.colorTheme = colorTheme;
    }
    
    /**
     * Calculează durata în secunde
     */
    public long getDurationInSeconds() {
        return duration / 1000;
    }
    
    /**
     * Calculează durata în format text lizibil
     */
    public String getFormattedDuration() {
        long seconds = getDurationInSeconds();
        if (seconds < 60) {
            return seconds + " sec";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            return minutes + " min";
        } else {
            long hours = seconds / 3600;
            long remainingMinutes = (seconds % 3600) / 60;
            return hours + "h " + remainingMinutes + "m";
        }
    }
    
    /**
     * Returnează un scor procentual pentru comparație
     */
    public float getPercentageScore() {
        if (totalQuestions == 0) return 0;
        return ((float) correctAnswers / totalQuestions) * 100;
    }
    
    /**
     * Verifică dacă activitatea este un quiz
     */
    public boolean isQuizActivity() {
        return activityType != null && activityType.contains("quiz");
    }
    
    /**
     * Verifică dacă activitatea este o poveste
     */
    public boolean isStoryActivity() {
        return activityType != null && activityType.contains("story");
    }
    
    /**
     * Verifică dacă activitatea este din Transilvania
     */
    public boolean isTransilvaniaActivity() {
        return region != null && region.equals("transilvania");
    }
    
    /**
     * Returnează o descriere scurtă pentru afișare în listă
     */
    public String getShortDescription() {
        if (isQuizActivity()) {
            return correctAnswers + "/" + totalQuestions + " corecte • " + 
                   String.format("%.0f", accuracy) + "%";
        } else {
            return "Completat • " + score + " puncte";
        }
    }
    
    /**
     * Returnează culoarea asociată cu regiunea
     */
    public String getRegionColor() {
        if (region == null) return "#2196F3"; // Albastru implicit
        
        switch (region) {
            case "transilvania":
                return "#8B0000"; // Roșu burgundy
            case "muntenia":
                return "#FFD700"; // Galben auriu
            case "moldova":
                return "#228B22"; // Verde pădure
            case "dobrogea":
                return "#4169E1"; // Albastru regal
            case "banat":
                return "#FF4500"; // Portocaliu roșcat
            case "oltenia":
                return "#9932CC"; // Violet închis
            case "bucovina":
                return "#32CD32"; // Verde lime
            case "maramures":
                return "#FF6347"; // Roșu tomate
            case "crisana":
                return "#1E90FF"; // Albastru dodger
            default:
                return "#2196F3"; // Albastru implicit
        }
    }
    
    @Override
    public String toString() {
        return "ActivityItem{" +
                "activityType='" + activityType + '\'' +
                ", displayName='" + displayName + '\'' +
                ", score=" + score +
                ", accuracy=" + accuracy +
                ", region='" + region + '\'' +
                ", completedAt=" + completedAt +
                '}';
    }
} 