package com.example.myapplication.core.domain.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Model class for quiz questions stored in Firestore
 */
public class FirestoreQuestionModel {
    @DocumentId
    private String id;
    private String question;
    private String correctAnswer;
    private List<String> incorrectAnswers;
    private String fact;
    private String hint;
    private String imageUrl;
    private String region;
    private String gameType;
    
    // Empty constructor needed for Firestore
    public FirestoreQuestionModel() {
    }
    
    /**
     * Constructor for a question with all fields
     */
    public FirestoreQuestionModel(String question, String correctAnswer, List<String> incorrectAnswers, 
                          String fact, String hint, String imageUrl, String region, String gameType) {
        this.question = question;
        this.correctAnswer = correctAnswer;
        this.incorrectAnswers = incorrectAnswers;
        this.fact = fact;
        this.hint = hint;
        this.imageUrl = imageUrl;
        this.region = region;
        this.gameType = gameType;
    }
    
    /**
     * Constructor for a question with minimal fields
     */
    public FirestoreQuestionModel(String question, String correctAnswer, List<String> incorrectAnswers, 
                          String region, String gameType) {
        this(question, correctAnswer, incorrectAnswers, "", "", "", region, gameType);
    }
    
    /**
     * Convert from local QuestionModel to FirestoreQuestionModel
     */
    @Exclude
    public static FirestoreQuestionModel fromGameQuestionModel(GameQuestionModel model, String region, String gameType) {
        List<String> incorrectAnswers = model.getIncorrectAnswers();
        return new FirestoreQuestionModel(
            model.getQuestion(),
            model.getCorrectAnswer(),
            incorrectAnswers,
            model.getFact(),
            "", // hint - nu există în QuestionModel
            "", // imageUrl - folosim local resource ID în QuestionModel
            region,
            gameType
        );
    }
    
    /**
     * Convert to a Map for Firestore
     */
    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("question", question);
        map.put("correctAnswer", correctAnswer);
        map.put("incorrectAnswers", incorrectAnswers);
        map.put("fact", fact);
        map.put("hint", hint);
        map.put("imageUrl", imageUrl);
        map.put("region", region);
        map.put("gameType", gameType);
        return map;
    }
    
    /**
     * Convert to local QuestionModel
     */
    @Exclude
    public GameQuestionModel toGameQuestionModel() {
        return new GameQuestionModel(
            question,
            correctAnswer,
            incorrectAnswers,
            0, // imageResourceId - nu avem echivalent în Firestore, folosim URL
            fact
        );
    }

    // Getters and setters
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public List<String> getIncorrectAnswers() {
        return incorrectAnswers;
    }

    public void setIncorrectAnswers(List<String> incorrectAnswers) {
        this.incorrectAnswers = incorrectAnswers;
    }

    public String getFact() {
        return fact;
    }

    public void setFact(String fact) {
        this.fact = fact;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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
} 