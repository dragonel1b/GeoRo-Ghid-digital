package com.example.myapplication.core.domain.model;

/**
 * Model pentru stocarea răspunsurilor la întrebări în cadrul unui quiz
 */
public class QuestionAnswer {
    private String questionId;
    private String selectedAnswer;
    private boolean isCorrect;
    private int timeToAnswerMs;
    
    /**
     * Constructor gol necesar pentru Firestore
     */
    public QuestionAnswer() {
        // Constructor gol necesar pentru Firestore
    }
    
    /**
     * Constructor complet pentru QuestionAnswer
     */
    public QuestionAnswer(String questionId, String selectedAnswer, boolean isCorrect, int timeToAnswerMs) {
        this.questionId = questionId;
        this.selectedAnswer = selectedAnswer;
        this.isCorrect = isCorrect;
        this.timeToAnswerMs = timeToAnswerMs;
    }
    
    // Getters și setters
    
    public String getQuestionId() {
        return questionId;
    }
    
    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }
    
    public String getSelectedAnswer() {
        return selectedAnswer;
    }
    
    public void setSelectedAnswer(String selectedAnswer) {
        this.selectedAnswer = selectedAnswer;
    }
    
    public boolean isCorrect() {
        return isCorrect;
    }
    
    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }
    
    public int getTimeToAnswerMs() {
        return timeToAnswerMs;
    }
    
    public void setTimeToAnswerMs(int timeToAnswerMs) {
        this.timeToAnswerMs = timeToAnswerMs;
    }
    
    /**
     * Convertește timpul de răspuns din milisecunde în secunde
     */
    public float getTimeToAnswerSeconds() {
        return timeToAnswerMs / 1000f;
    }
} 