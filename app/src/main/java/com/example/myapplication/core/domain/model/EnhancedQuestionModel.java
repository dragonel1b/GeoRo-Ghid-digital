package com.example.myapplication.core.domain.model;

import java.util.Arrays;
import java.util.List;

/**
 * Model îmbunătățit pentru întrebări cu categorii, dificultăți și metadate suplimentare
 */
public class EnhancedQuestionModel extends GameQuestionModel {
    
    public enum Category {
        HISTORY("Istorie", "🏛️", "#8B4513"),
        GEOGRAPHY("Geografie", "🗺️", "#228B22"), 
        CULTURE("Cultură", "🎭", "#4B0082"), 
        ARCHITECTURE("Arhitectură", "🏰", "#CD853F"),
        GASTRONOMY("Gastronomie", "🍽️", "#FF6347"),
        LEGENDS("Legende", "🐉", "#8B008B"),
        PERSONALITIES("Personalități", "👑", "#DAA520"),
        NATURE("Natură", "🌲", "#006400"),
        GENERAL("General", "📚", "#666666");
        
        public final String displayName;
        public final String emoji;
        public final String color;
        
        Category(String displayName, String emoji, String color) {
            this.displayName = displayName;
            this.emoji = emoji;
            this.color = color;
        }
    }
    
    public enum Difficulty {
        EASY(1, "Ușor", "#4CAF50"),
        MEDIUM(2, "Mediu", "#FF9800"), 
        HARD(3, "Greu", "#F44336"),
        EXPERT(4, "Expert", "#9C27B0");
        
        public final int level;
        public final String displayName;
        public final String color;
        
        Difficulty(int level, String displayName, String color) {
            this.level = level;
            this.displayName = displayName;
            this.color = color;
        }
    }
    
    private Category category;
    private Difficulty difficulty;
    private String[] tags;
    private String imageUrl;
    private String audioUrl;
    private int estimatedTimeSeconds;
    private String hint;
    private String detailedExplanation;
    private boolean isMultimedia;
    private String sourceReference;
    private String id;
    
    /**
     * Constructor complet pentru întrebări îmbunătățite
     */
    public EnhancedQuestionModel(String question, String correctAnswer, List<String> incorrectAnswers,
                                int imageResourceId, String fact, Category category, 
                                Difficulty difficulty, String[] tags) {
        super(question, correctAnswer, incorrectAnswers, imageResourceId, fact);
        this.category = category;
        this.difficulty = difficulty;
        this.tags = tags != null ? tags : new String[0];
        this.estimatedTimeSeconds = calculateEstimatedTime();
        this.isMultimedia = imageResourceId != 0 || (imageUrl != null && !imageUrl.isEmpty());
        this.id = generateId();
    }
    
    /**
     * Constructor cu parametri minimali
     */
    public EnhancedQuestionModel(String question, String correctAnswer, List<String> incorrectAnswers,
                                Category category, Difficulty difficulty) {
        this(question, correctAnswer, incorrectAnswers, 0, "", category, difficulty, null);
    }
    
    /**
     * Convertește din QuestionModel simplu
     */
    public static EnhancedQuestionModel fromGameQuestionModel(GameQuestionModel original, 
                                                         Category category, Difficulty difficulty) {
        return new EnhancedQuestionModel(
            original.getQuestion(),
            original.getCorrectAnswer(),
            original.getIncorrectAnswers(),
            original.getImageResourceId(),
            original.getFact(),
            category,
            difficulty,
            null
        );
    }
    
    /**
     * Calculează timpul estimat bazat pe dificultate și complexitate
     */
    private int calculateEstimatedTime() {
        int baseTime = 20; // 20 secunde bază
        
        // Ajustare bazată pe dificultate
        switch (difficulty) {
            case EASY:
                baseTime = 15;
                break;
            case MEDIUM:
                baseTime = 20;
                break;
            case HARD:
                baseTime = 25;
                break;
            case EXPERT:
                baseTime = 30;
                break;
        }
        
        // Ajustare bazată pe lungimea întrebării
        int questionLength = getQuestion().length();
        if (questionLength > 100) {
            baseTime += 5;
        }
        
        // Ajustare pentru multimedia
        if (isMultimedia) {
            baseTime += 5;
        }
        
        return baseTime;
    }
    
    /**
     * Verifică dacă întrebarea se potrivește cu filtrele specificate
     */
    public boolean matchesFilters(List<Category> categories, List<Difficulty> difficulties, 
                                 String searchTerm) {
        // Verifică categoria
        if (categories != null && !categories.isEmpty() && !categories.contains(this.category)) {
            return false;
        }
        
        // Verifică dificultatea
        if (difficulties != null && !difficulties.isEmpty() && !difficulties.contains(this.difficulty)) {
            return false;
        }
        
        // Verifică termenul de căutare
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String searchLower = searchTerm.toLowerCase();
            boolean matches = getQuestion().toLowerCase().contains(searchLower) ||
                            getCorrectAnswer().toLowerCase().contains(searchLower) ||
                            (getFact() != null && getFact().toLowerCase().contains(searchLower));
            
            // Căutare în tag-uri
            if (!matches && tags != null) {
                for (String tag : tags) {
                    if (tag.toLowerCase().contains(searchLower)) {
                        matches = true;
                        break;
                    }
                }
            }
            
            return matches;
        }
        
        return true;
    }
    
    /**
     * Calculează scorul de relevanță pentru un termen de căutare
     */
    public int calculateRelevanceScore(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return 0;
        }
        
        String searchLower = searchTerm.toLowerCase();
        int score = 0;
        
        // Potrivire exactă în întrebare (scor maxim)
        if (getQuestion().toLowerCase().contains(searchLower)) {
            score += 100;
        }
        
        // Potrivire în răspuns corect
        if (getCorrectAnswer().toLowerCase().contains(searchLower)) {
            score += 50;
        }
        
        // Potrivire în explicație
        if (getFact() != null && getFact().toLowerCase().contains(searchLower)) {
            score += 25;
        }
        
        // Potrivire în tag-uri
        if (tags != null) {
            for (String tag : tags) {
                if (tag.toLowerCase().contains(searchLower)) {
                    score += 10;
                }
            }
        }
        
        return score;
    }
    
    // Getters și setters
    
    public Category getCategory() {
        return category;
    }
    
    public void setCategory(Category category) {
        this.category = category;
    }
    
    public Difficulty getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
        this.estimatedTimeSeconds = calculateEstimatedTime();
    }
    
    public String[] getTags() {
        return tags;
    }
    
    public void setTags(String[] tags) {
        this.tags = tags != null ? tags : new String[0];
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        this.isMultimedia = getImageResourceId() != 0 || (imageUrl != null && !imageUrl.isEmpty());
    }
    
    public String getAudioUrl() {
        return audioUrl;
    }
    
    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }
    
    public int getEstimatedTimeSeconds() {
        return estimatedTimeSeconds;
    }
    
    public String getHint() {
        return hint;
    }
    
    public void setHint(String hint) {
        this.hint = hint;
    }
    
    public String getDetailedExplanation() {
        return detailedExplanation;
    }
    
    public void setDetailedExplanation(String detailedExplanation) {
        this.detailedExplanation = detailedExplanation;
    }
    
    public boolean isMultimedia() {
        return isMultimedia;
    }
    
    public String getSourceReference() {
        return sourceReference;
    }
    
    public void setSourceReference(String sourceReference) {
        this.sourceReference = sourceReference;
    }
    
    /**
     * Returnează o reprezentare string pentru debugging
     */
    /**
     * Returnează ID-ul unic al întrebării
     */
    public String getId() {
        if (id == null) {
            id = generateId();
        }
        return id;
    }
    
    /**
     * Setează ID-ul întrebării
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Generează un ID unic pentru întrebare
     */
    private String generateId() {
        return String.valueOf(Math.abs(getQuestion().hashCode()));
    }
    
    @Override
    public String toString() {
        return "EnhancedQuestionModel{" +
                "id='" + getId() + '\'' +
                ", question='" + getQuestion() + '\'' +
                ", category=" + category +
                ", difficulty=" + difficulty +
                ", tags=" + Arrays.toString(tags) +
                ", estimatedTime=" + estimatedTimeSeconds +
                '}';
    }
} 