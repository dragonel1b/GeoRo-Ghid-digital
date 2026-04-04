package com.example.myapplication.core.domain.model;

/**
 * Model pentru progresul utilizatorului într-o regiune specifică
 */
public class RegionProgress {
    private String regionKey;
    private String regionName;
    private int iconResource;
    private int quizzesCompleted;
    private int bestScore;
    private float averageAccuracy;
    private boolean hasCompletedQuizzes;

    public RegionProgress() {
        // Constructor gol pentru Firebase
    }

    public RegionProgress(String regionKey, String regionName, int iconResource) {
        this.regionKey = regionKey;
        this.regionName = regionName;
        this.iconResource = iconResource;
        this.quizzesCompleted = 0;
        this.bestScore = 0;
        this.averageAccuracy = 0.0f;
        this.hasCompletedQuizzes = false;
    }

    // Getters
    public String getRegionKey() {
        return regionKey;
    }

    public String getRegionName() {
        return regionName;
    }

    public int getIconResource() {
        return iconResource;
    }

    public int getQuizzesCompleted() {
        return quizzesCompleted;
    }

    public int getBestScore() {
        return bestScore;
    }

    public float getAverageAccuracy() {
        return averageAccuracy;
    }

    public boolean hasCompletedQuizzes() {
        return hasCompletedQuizzes;
    }

    // Setters
    public void setRegionKey(String regionKey) {
        this.regionKey = regionKey;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public void setIconResource(int iconResource) {
        this.iconResource = iconResource;
    }

    public void setQuizzesCompleted(int quizzesCompleted) {
        this.quizzesCompleted = quizzesCompleted;
        this.hasCompletedQuizzes = quizzesCompleted > 0;
    }

    public void setBestScore(int bestScore) {
        this.bestScore = bestScore;
    }

    public void setAverageAccuracy(float averageAccuracy) {
        this.averageAccuracy = averageAccuracy;
    }

    public void setHasCompletedQuizzes(boolean hasCompletedQuizzes) {
        this.hasCompletedQuizzes = hasCompletedQuizzes;
    }

    /**
     * Actualizează progresul cu un nou rezultat de quiz
     */
    public void updateWithQuizResult(int score, float accuracy) {
        this.quizzesCompleted++;
        this.hasCompletedQuizzes = true;
        
        // Actualizează cel mai bun scor
        if (score > this.bestScore) {
            this.bestScore = score;
        }
        
        // Calculează acuratețea medie (simplificată)
        this.averageAccuracy = (this.averageAccuracy * (quizzesCompleted - 1) + accuracy) / quizzesCompleted;
    }

    @Override
    public String toString() {
        return "RegionProgress{" +
                "regionKey='" + regionKey + '\'' +
                ", regionName='" + regionName + '\'' +
                ", quizzesCompleted=" + quizzesCompleted +
                ", bestScore=" + bestScore +
                ", averageAccuracy=" + averageAccuracy +
                ", hasCompletedQuizzes=" + hasCompletedQuizzes +
                '}';
    }
} 